package org.snpeff.spliceSites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Intron;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.SpliceSiteBranchU12;
import org.snpeff.interval.Transcript;
import org.snpeff.motif.Pwm;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.util.Tuple;

/**
 * Analyze sequences from splice sites
 *
 * @author pcingola
 */
public class SpliceTypes {

	public static int MAX_SPLICE_SIZE = 10;
	public static int SIZE_BRANCH = 60;
	public static final double THRESHOLD_ENTROPY = 0.05;
	public static final int THRESHOLD_COUNT = 100;
	public static final double THRESHOLD_P = 0.95;

	boolean verbose = false;
	boolean debug = false;
	String genomeFasta;
	Config config;
	HashMap<String, String> donorsByIntron = new HashMap<String, String>();
	HashMap<String, String> acceptorsByIntron = new HashMap<String, String>();
	HashMap<String, String> branchByIntron = new HashMap<String, String>();
	ArrayList<String> donorAccPairDonor = new ArrayList<String>();
	ArrayList<String> donorAccPairAcc = new ArrayList<String>();
	AutoHashMap<String, List<SpliceSiteBranchU12>> branchU12ByDonorAcc = new AutoHashMap<String, List<SpliceSiteBranchU12>>(new ArrayList<SpliceSiteBranchU12>());
	HashMap<String, Integer> donorAcc = new HashMap<String, Integer>();
	AcgtTree acgtTreeDonors = new AcgtTree();
	AcgtTree acgtTreeAcc = new AcgtTree();
	Pwm pwmU12;
	TranscriptSet transcriptSet;
	double thresholdPDonor;
	double thresholdEntropyDonor;
	double thresholdPAcc;
	double thresholdEntropyAcc;
	double thresholdU12Score;

	public SpliceTypes(Config config) {
		this.config = config;
	}

	/**
	 * Find acceptors for this donor
	 */
	void acc4donor(String donorSeq) {
		// Create a new tree using all these sequences
		AcgtTree tree = new AcgtTree();
		for (String key : donorsByIntron.keySet()) {
			String donor = donorsByIntron.get(key);
			if (donor.startsWith(donorSeq)) {
				String acc = GprSeq.reverse(acceptorsByIntron.get(key));
				if (acc.indexOf('N') < 0) tree.add(acc);
			}
		}

		// Show them
		for (String accSeq : tree.findNodeNames(thresholdEntropyAcc, thresholdPAcc, THRESHOLD_COUNT)) {
			if (accSeq.length() > 1) {
				accSeq = GprSeq.reverse(accSeq);
				add(donorSeq, accSeq);
			}
		}
	}

	/**
	 * Add a donor-acceptor pair
	 */
	void add(String donor, String acceptor) {
		String key = String.format("%-10s\t%10s", donor, acceptor);
		int count = countDonorAcc(donor, acceptor);
		if (count >= THRESHOLD_COUNT) donorAcc.put(key, count);
	}

	/**
	 * Calculate the best U12 score.
	 * If the score is higher than 'thresholdU12Score' then add the chr:pos data to a list
	 *
	 * @return A Tuple<Double, Integer> having the best score and best position
	 */
	public Tuple<Double, Integer> addBestU12Score(Transcript tr, String chrSeq, String donorAcceptor, int intronStart, int intronEnd) {
		// Get branch site string: SIZE_BRANCH bases before intron ends.
		String branchStr = seqBranch(tr, chrSeq, intronStart, intronEnd);

		// Calculate best score and position (position in 'branchStr')
		Tuple<Double, Integer> bestU12 = bestU12Score(branchStr);

		// Calculate chomosome position
		int bestU12Start = bestU12.second, bestU12End;
		if (tr.isStrandPlus()) {
			bestU12Start = intronEnd - SpliceTypes.SIZE_BRANCH + bestU12Start;
			bestU12End = bestU12Start + pwmU12.length();
		} else {
			bestU12Start = intronStart + SpliceTypes.SIZE_BRANCH - bestU12Start - pwmU12.length() - 1;
			bestU12End = bestU12Start + pwmU12.length();
		}

		// Add to a collection
		Intron intron = tr.findIntron(bestU12Start);
		SpliceSiteBranchU12 ssu12 = new SpliceSiteBranchU12(intron, bestU12Start, bestU12End, tr.isStrandMinus(), "");
		addBranchU12(donorAcceptor, ssu12);

		return bestU12;
	}

	/**
	 * Add a SpliceSiteBranchU12 for this donor-Acceptor pair
	 */
	void addBranchU12(String donorAcceptor, SpliceSiteBranchU12 ssu12) {
		branchU12ByDonorAcc.getOrCreate(donorAcceptor).add(ssu12);
	}

	/**
	 * Analyze and create conserved splice sites donor-acceptor pairs.
	 */
	public boolean analyzeAndCreate() {
		if (verbose) Log.info("Splice site sequence conservation analysis: Start");
		load(); // Load data
		spliceSequences(); // Find splice sequences
		spliceDonoAcceptorPairs(); // Find donor acceptor pairs
		createSpliceSites();
		if (verbose) Log.info("Splice site sequence conservation analysis: Done.");
		return true;
	}

	/**
	 * Find the index of the donor-acceptor pair that best matches these intron sequences.
	 */
	int bestMatchIndex(String intronSeqDonor, String intronSeqAcc) {
		if ((intronSeqDonor == null) || (intronSeqAcc == null)) return -1;
		int maxLenDa = -1, idx = -1;

		for (int i = 0; i < donorAccPairDonor.size(); i++) {
			String don = donorAccPairDonor.get(i);
			String ac = donorAccPairAcc.get(i);
			if (intronSeqDonor.startsWith(don) && intronSeqAcc.endsWith(ac)) {
				int lenda = don.length() + ac.length();
				if (lenda > maxLenDa) {
					maxLenDa = lenda;
					idx = i;
				}
			}
		}

		return idx;
	}

	/**
	 * Find the best score for PWM matrix in U12 branch points
	 * @param seq
	 * @return A Tuple<Double, Integer> having the best score and best position
	 */
	public Tuple<Double, Integer> bestU12Score(String seq) {
		int max = seq.length() - pwmU12.length();
		double bestScore = 0;
		int bestPos = -1;
		for (int i = 0; i < max; i++) {
			String sub = seq.substring(i, i + pwmU12.length());
			if (sub.indexOf('N') < 0) {
				double score = pwmU12.score(sub);
				if (bestScore < score) {
					bestScore = score;
					bestPos = i;
				}
			}
		}

		return new Tuple<Double, Integer>(bestScore, bestPos);
	}

	/**
	 * Calculate threshold of U12 PWM scores
	 * Pick the score that gives a 'thresholdU12Percentile'.
	 * E.g. branchU12Threshold(0.95) gives the 95% percentile threshold
	 */
	public double branchU12Threshold(double thresholdU12Percentile) {
		Log.info("Finding U12 PWM score distribution and threshold.");
		ArrayList<Double> scores = new ArrayList<Double>();

		//for (String branch : branchesList) {
		for (String branch : branchByIntron.values()) {
			Tuple<Double, Integer> best = bestU12Score(branch);
			double bestScore = best.first;
			scores.add(bestScore);
		}

		// Get quantile
		Collections.sort(scores);
		int index = (int) (thresholdU12Percentile * scores.size());
		thresholdU12Score = scores.get(index);
		return thresholdU12Score;
	}

	/**
	 * Count how many entries that have both 'donor' and 'acceptor'
	 */
	int countDonorAcc(String donor, String acceptor) {
		int count = 0;
		for (String key : donorsByIntron.keySet()) {
			String d = donorsByIntron.get(key);
			String a = acceptorsByIntron.get(key);

			if (d.startsWith(donor) && a.endsWith(acceptor)) count++;
		}
		return count;
	}

	/**
	 * Create one fasta file for each donor-acceptor pair
	 */
	public void createSpliceFasta(String outputDir) {
		if (verbose) Log.info("Creating FASTA files for each dono-acceptor pair.");

		for (int i = 0; i < getDonorAccPairSize(); i++) {
			String d = getDonor(i);
			String a = getAcceptor(i);
			String fastaFile = outputDir + "/" + config.getGenome().getId() + "." + d + "-" + a + ".fa";
			createSpliceFasta(fastaFile, d, a);
		}
	}

	/**
	 * Add entries that have both 'donor' and 'acceptor'
	 */
	void createSpliceFasta(String fastaFile, String donor, String acceptor) {
		StringBuilder fasta = new StringBuilder();

		for (String intronKey : getIntronKeySet()) {
			String d = getDonorByIntron(intronKey);
			String a = getAcceptorsByIntron(intronKey);

			if (d.startsWith(donor) && a.endsWith(acceptor)) {
				String branch = getAcceptorsByIntron(intronKey);
				fasta.append(">" + intronKey + "\n" + d + "-" + a + "\n");
			}
		}

		// Write fasta file
		if (verbose) Log.info("\tWriting fasta sequences to file: " + fastaFile);
		Gpr.toFile(fastaFile, fasta);
	}

	/**
	 * Create Splice sites
	 */
	void createSpliceSites() {
		if (verbose) Log.info("\tCreating splice sites.");

		int count = 0;
		for (Transcript tr : transcriptSet)
			for (Intron intron : tr.introns())
				createSpliceSites(intron);

		if (verbose) Log.info("\tCreated : " + count + " splice sites.");
	}

	/**
	 * Create splice sites
	 */
	int createSpliceSites(Intron intron) {
		int start = intron.getStart();
		int end = intron.getEndClosed();
		String key = intron.getChromosomeName() + ":" + start + "-" + end;
		String donor = donorsByIntron.get(key);
		String acc = acceptorsByIntron.get(key);

		if ((donor == null) || (acc == null)) return 0; // May be we skipped this transcript

		int idx = bestMatchIndex(donor, acc);
		int dist = end - start - 1;

		int count = 0;
		if (idx > 0) {
			// Create donor and acceptor
			String donorConserved = donorAccPairDonor.get(idx);
			String accConserved = donorAccPairAcc.get(idx);
			if (debug) System.err.println("\tCreating splice sites:\t" + donor + "-" + acc + "\tConserved:\t" + donorConserved + "-" + accConserved);

			if (donorConserved.length() > SpliceSite.CORE_SPLICE_SITE_SIZE) {
				intron.createSpliceSiteDonor(Math.min(donorConserved.length(), dist));
				count++;
			}

			if (accConserved.length() > SpliceSite.CORE_SPLICE_SITE_SIZE) {
				intron.createSpliceSiteAcceptor(Math.min(accConserved.length(), dist));
				count++;
			}
		}

		return count;
	}

	/**
	 * Find donors for this acceptor
	 */
	void donor4acc(String accSeq) {
		// Create a new tree using all these sequences
		AcgtTree tree = new AcgtTree();
		for (String key : acceptorsByIntron.keySet()) {
			String acc = GprSeq.reverse(acceptorsByIntron.get(key));
			if (acc.endsWith(accSeq)) {
				String donor = donorsByIntron.get(key);
				if (donor.indexOf('N') < 0) tree.add(donor);
			}
		}

		// Show them
		for (String donorSeq : tree.findNodeNames(thresholdEntropyDonor, thresholdPDonor, THRESHOLD_COUNT))
			if (donorSeq.length() > 1) add(donorSeq, accSeq);
	}

	/**
	 * Find an probability threshold using THRESHOLD_P quantile
	 */
	double findEntropyThreshold(AcgtTree tree) {
		List<Double> values = tree.entropyAll(THRESHOLD_COUNT);
		Collections.sort(values);
		int index = (int) (values.size() * THRESHOLD_ENTROPY);
		return values.get(index);
	}

	/**
	 * Find an probability threshold using THRESHOLD_P quantile
	 */
	double findPthreshold(AcgtTree tree) {
		List<Double> values = tree.pAll(THRESHOLD_COUNT);
		Collections.sort(values);
		int index = (int) (values.size() * THRESHOLD_P);
		return values.get(index);
	}

	public String getAcceptor(int i) {
		return donorAccPairAcc.get(i);
	}

	public String getAcceptorsByIntron(String intronKey) {
		return acceptorsByIntron.get(intronKey);
	}

	public String getBranchByIntron(String intronKey) {
		return branchByIntron.get(intronKey);
	}

	/**
	 * Add a SpliceSiteBranchU12 for this donor-Acceptor pair
	 */
	public List<SpliceSiteBranchU12> getBranchU12(String donorAcceptor) {
		return branchU12ByDonorAcc.getOrCreate(donorAcceptor);
	}

	public String getDonor(int i) {
		return donorAccPairDonor.get(i);
	}

	public int getDonorAccPairSize() {
		return donorAccPairDonor.size();
	}

	public String getDonorByIntron(String intronKey) {
		return donorsByIntron.get(intronKey);
	}

	public Set<String> getIntronKeySet() {
		return donorsByIntron.keySet();
	}

	/**
	 * Lad data from files
	 */
	void load() {
		String u12file = config.getDirData() + "/spliceSites/u12_branch.pwm";
		if (verbose) Log.info("\tLoading U12 PWM form file '" + u12file + "'");
		pwmU12 = new Pwm(u12file);

		// Load predictor?
		if (config.getSnpEffectPredictor() == null) {
			if (verbose) Log.info("\tLoading: " + config.getGenome().getGenomeName());
			config.loadSnpEffectPredictor();
			if (verbose) Log.info("\tdone.");
		}

		// Create transcript set?
		if (transcriptSet == null) {
			transcriptSet = new TranscriptSet(config.getGenome());
			transcriptSet.setVerbose(verbose);
			transcriptSet.setDebug(debug);
			transcriptSet.filter();
		}
	}

	/**
	 * Get acceptor sequence
	 */
	String seqAcceptor(Transcript tr, String chrSeq, int intronStart, int intronEnd) {
		if ((intronEnd - intronStart) < MAX_SPLICE_SIZE) return "";

		if (tr.isStrandPlus()) {
			int splAccStart = intronEnd - MAX_SPLICE_SIZE;
			int splAccEnd = intronEnd + MAX_SPLICE_SIZE;
			return chrSeq.substring(splAccStart, splAccEnd + 1).toUpperCase();
		}

		// Negative strand
		int splAccStart = intronStart - MAX_SPLICE_SIZE;
		int splAccEnd = intronStart + MAX_SPLICE_SIZE;
		return GprSeq.reverseWc(chrSeq.substring(splAccStart, splAccEnd + 1).toUpperCase());
	}

	/**
	 * Get branch sequence (a few bases before intron ends)
	 */
	String seqBranch(Transcript tr, String chrSeq, int intronStart, int intronEnd) {
		if ((intronEnd - intronStart) < SIZE_BRANCH) return "";

		if (tr.isStrandPlus()) {
			int splBranchStart = intronEnd - SIZE_BRANCH + 1;
			int splBranchEnd = intronEnd;
			return chrSeq.substring(splBranchStart, splBranchEnd).toUpperCase();
		}

		// Negative strand
		int splBranchStart = intronStart + 1;
		int splBranchEnd = intronStart + SIZE_BRANCH;
		return GprSeq.reverseWc(chrSeq.substring(splBranchStart, splBranchEnd).toUpperCase());
	}

	/**
	 * Get donor sequence
	 */
	String seqDonor(Transcript tr, String chrSeq, int intronStart, int intronEnd) {
		if ((intronEnd - intronStart) < MAX_SPLICE_SIZE) return "";

		if (tr.isStrandPlus()) {
			int splDonorStart = intronStart - MAX_SPLICE_SIZE;
			int splDonorEnd = intronStart + MAX_SPLICE_SIZE;
			return chrSeq.substring(splDonorStart, splDonorEnd + 1).toUpperCase();
		}

		// Negative strand
		int splDonorStart = intronEnd - MAX_SPLICE_SIZE;
		int splDonorEnd = intronEnd + MAX_SPLICE_SIZE;

		return GprSeq.reverseWc(chrSeq.substring(splDonorStart, splDonorEnd + 1).toUpperCase());
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setGenomeFasta(String genomeFasta) {
		this.genomeFasta = genomeFasta;
	}

	public void setTranscriptSet(TranscriptSet transcriptSet) {
		this.transcriptSet = transcriptSet;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Find donor-acceptor pairs
	 */
	void spliceDonoAcceptorPairs() {
		//---
		// Create trees
		//---
		if (verbose) Log.info("\tFinding donor-acceptor pairs: Creating quaternary trees");
		for (String donor : donorsByIntron.values())
			if (donor.indexOf('N') < 0) acgtTreeDonors.add(donor);

		for (String acc : acceptorsByIntron.values())
			if (acc.indexOf('N') < 0) acgtTreeAcc.add(GprSeq.reverse(acc));

		//---
		// Find donor - acceptor pairs
		//---
		if (verbose) Log.info("\tCalculate thresholds");
		thresholdPDonor = findPthreshold(acgtTreeDonors);
		thresholdEntropyDonor = findEntropyThreshold(acgtTreeDonors);
		thresholdPAcc = findPthreshold(acgtTreeAcc);
		thresholdEntropyAcc = findEntropyThreshold(acgtTreeAcc);

		if (verbose) Log.info("\tDonors Thresholds:\t\tEntropy: " + thresholdEntropyDonor + "\t\tProbability: " + thresholdPDonor);
		for (String seq : acgtTreeDonors.findNodeNames(thresholdEntropyDonor, thresholdPDonor, THRESHOLD_COUNT)) {
			if (seq.length() > 1) acc4donor(seq);
		}

		if (verbose) Log.info("\tFind acceptors");
		if (verbose) Log.info("\tAcceptors Thresholds:\t\tEntropy: " + thresholdEntropyAcc + "\t\tProbability: " + thresholdPAcc);
		for (String seq : acgtTreeAcc.findNodeNames(thresholdEntropyAcc, thresholdPAcc, THRESHOLD_COUNT)) {
			if (seq.length() > 1) donor4acc(GprSeq.reverse(seq));
		}

		//---
		// Show all donor - acc pairs (sort by number of matches)
		//---
		if (verbose) Log.info("\tAdd Donor - Acceptors pairs: ");
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(donorAcc.keySet());
		Collections.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return donorAcc.get(arg1) - donorAcc.get(arg0);
			}
		});

		for (String key : keys) {
			if (donorAcc.get(key) > THRESHOLD_COUNT) {
				String da[] = key.trim().split("\\s+");
				donorAccPairDonor.add(da[0]);
				donorAccPairAcc.add(da[1]);

				if (verbose) Log.info("\t\t\t" + donorAcc.get(key) + "\t" + key);
			}
		}
	}

	/**
	 * Find splice sequences for this genome
	 */
	void spliceSequences() {
		if (genomeFasta == null) genomeFasta = config.getFileNameGenomeFasta();
		if (verbose) Log.info("\tFinding splice sequences. Reading fasta file: " + genomeFasta);

		// Iterate over all chromosomes
		FastaFileIterator ffi = new FastaFileIterator(genomeFasta);
		for (String chrSeq : ffi) {
			String chrName = Chromosome.simpleName(ffi.getName());
			spliceSequences(chrName, chrSeq);
		}
	}

	/**
	 * Find splice sequences for this chromosome
	 */
	void spliceSequences(String chrName, String chrSeq) {
		int countEx = 0, countTr = 0;

		for (Transcript tr : transcriptSet.getByChromo(chrName)) {
			Exon exPrev = null;
			for (Exon ex : tr.sortedStrand()) {
				countEx++;

				if (exPrev != null) { // Not for first exon (it has no 'previous' intron)
					int start, end;
					if (tr.isStrandPlus()) {
						start = exPrev.getEndClosed();
						end = ex.getStart();
					} else {
						start = ex.getEndClosed();
						end = exPrev.getStart();
					}

					// Already added? (do not add twice)
					spliceSequences(tr, chrName, chrSeq, start, end);
				}

				exPrev = ex;
			}

			countTr++;
		}

		if (verbose) Log.info("\t\tChromosome: " + chrName //
				+ "\tTranscripts: " + countTr //
				+ "\tExons: " + countEx //
				+ "\tTotal Splice sites: " + donorsByIntron.size() //
		);
	}

	/**
	 * Find splice sequences for this intron
	 */
	void spliceSequences(Transcript tr, String chrName, String chrSeq, int intronStart, int intronEnd) {
		// Do not repeat
		String key = chrName + ":" + intronStart + "-" + intronEnd;
		if (donorsByIntron.containsKey(key)) return;

		String donorStr = seqDonor(tr, chrSeq, intronStart, intronEnd);
		String accStr = seqAcceptor(tr, chrSeq, intronStart, intronEnd);
		String branchStr = seqBranch(tr, chrSeq, intronStart, intronEnd);

		String intronSeqDonor = donorStr.isEmpty() ? "" : donorStr.substring(MAX_SPLICE_SIZE + 1);
		String intronSeqAcc = accStr.isEmpty() ? "" : accStr.substring(0, MAX_SPLICE_SIZE);

		// Add to arrays
		donorsByIntron.put(key, intronSeqDonor);
		acceptorsByIntron.put(key, intronSeqAcc);
		branchByIntron.put(key, branchStr);
	}

}
