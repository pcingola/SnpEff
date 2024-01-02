package org.snpeff.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snpeff.SnpEff;
import org.snpeff.align.SmithWaterman;
import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.collections.AutoHashMap;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.genBank.EmblFile;
import org.snpeff.genBank.Feature;
import org.snpeff.genBank.Feature.Type;
import org.snpeff.genBank.Features;
import org.snpeff.genBank.FeaturesFile;
import org.snpeff.genBank.GenBankFile;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryEmbl;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGenBank;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Command line: Read protein sequences from a file and compare them to the ones calculated from our data structures
 *
 * Note: This is done in order to see potential incompatibility
 *       errors between genome sequence and annotation.
 *
 * @author pcingola
 */
public class SnpEffCmdProtein extends SnpEff {

	public static boolean onlyOneError = false; // This is used in some test-cases
	public static double MAX_ERROR_RATE = 0.06; // Maximum allowed error is 6% (otherwise test fails)

	boolean checkNumOk = true;
	boolean codonTables;
	boolean storeAlignments; // Store alignments (used for some test cases)
	double maxErrorRate = MAX_ERROR_RATE; // Maximum acceptable error rate
	int totalErrors = 0;
	int totalOk = 0;
	int totalWarnings = 0;
	int totalNotFound = 0;
	String configFile = Config.DEFAULT_CONFIG_FILE;
	String proteinFile = "";
	Map<String, String> proteinById; // Protein sequence by ID (transcript ID, protein ID, etc.)
	AutoHashMap<String, List<Transcript>> trByChromo;
	HashMap<String, SmithWaterman> alignmentByTrId = new HashMap<>();

	public SnpEffCmdProtein() {
	}

	public SnpEffCmdProtein(Config config, String proteinFile) {
		this.config = config;
		this.proteinFile = proteinFile;
	}

	void add(String trId, String seq, int lineNum, boolean check) {
		// Repeated transcript Id? => Check that Protein is the same
		if (check && (proteinById.get(trId) != null) && (!proteinById.get(trId).equals(seq))) //
			System.err.println("ERROR: Different protein for the same transcript ID. This should never happen!!!"//
					+ "\n\tLine number   : " + lineNum //
					+ "\n\tTranscript ID : '" + trId + "'"//
					+ "\n\tProtein       : " + proteinById.get(trId) //
					+ "\n\tProtein (new) : " + seq //
			);

		// Use whole trId
		proteinById.put(trId, seq); // Add it to the hash
		if (debug) Log.debug("Adding proteinById{'" + trId + "'} :\t" + seq);
	}

	/**
	 * Check proteins using all possible codon tables
	 */
	boolean checkCodonTables() {
		if (verbose) Log.info("Comparing Proteins...");

		createTrByChromo(); // Create lists of transcripts by chromosome

		// For each chromosome...
		var ok = true;
		for (Chromosome chromo : genome) {
			String chr = chromo.getId();

			// Check against each codon table
			for (CodonTable codonTable : CodonTables.getInstance()) {
				setCodonTable(chromo, codonTable);
				var errorRate = proteinCompare(chr, false, false);
				ok |= (errorRate < maxErrorRate);
			}
		}

		if (verbose) Log.info("done");
		return ok;
	}

	/**
	 * Check proteins
	 */
	boolean checkProteins() {
		if (verbose) Log.info("Comparing Proteins...");

		if (codonTables) {
			// Compare proteins using ALL codon tables
			return checkCodonTables();
		} else {
			// Compare proteins
			var errorRate = proteinCompare(null, true, true);
			return errorRate <= maxErrorRate;
		}
	}

	void createTrByChromo() {
		trByChromo = new AutoHashMap<>(new ArrayList<Transcript>());

		for (Gene gene : genome.getGenes()) {
			for (Transcript tr : gene) {
				String chr = tr.getChromosomeName();
				trByChromo.getOrCreate(chr).add(tr);
			}
		}
	}

	/**
	 * Compare two protein sequences
	 */
	boolean equals(String protein, String proteinRef) {
		if (protein.isEmpty() && proteinRef.isEmpty()) return true;

		protein = proteinFormat(protein);
		proteinRef = proteinFormat(proteinRef);

		if (protein.equals(proteinRef)) return true;

		// Remove last AA in Ref sequence (e.g. when last AA in reference by is 'unknown')
		String refUnk = "";
		if (proteinRef.length() > 0) {
			refUnk = proteinRef.substring(0, proteinRef.length() - 1);
			if (protein.equals(refUnk)) return true;
		}

		// Replace First AA by 'Met'? Start codon may be translated as Met even if it normally encodes other AA.
		// Compare everything but start codon.
		String proteinNoStart = "", proteinRefNoStart = "";
		if ((protein.length() > 0) && (proteinRef.length() > 0)) {
			proteinNoStart = protein.substring(1);
			proteinRefNoStart = proteinRef.substring(1);
			if (proteinNoStart.equals(proteinRefNoStart)) return true;
		}

		// Rare amino acid translations (U)
		String proteinU = protein.replace('*', 'U');
		if (proteinU.equals(proteinRef) || proteinU.equals(refUnk)) return true;

		String proteinNoStartU = proteinNoStart.replace('*', 'U');
		if (proteinNoStartU.equals(proteinRefNoStart)) return true;

		String proteinUnknownAa = replaceUnknownAa(proteinNoStartU, proteinRefNoStart);
		if (proteinUnknownAa.equals(proteinRefNoStart)) return true;

		return false;
	}

	/**
	 * Show an error message that actually helps to solve the problem
	 */
	void fatalErrorNoTranscriptsChecked() {
		StringBuilder sb = new StringBuilder();

		// Show some transcript IDs
		int maxTrIds = 20;
		sb.append("Transcript IDs from database (sample):\n" + sampleTrIds(maxTrIds));
		sb.append("Transcript IDs from database (fasta file):\n" + sampleTrIdsFasta(maxTrIds));
		Log.fatalError("No proteins checked. This is might be caused by differences in FASTA file transcript IDs respect to database's transcript's IDs.\n" + sb);
	}

	public HashMap<String, SmithWaterman> getAlignmentByTrId() {
		return alignmentByTrId;
	}

	public int getTotalErrors() {
		return totalErrors;
	}

	public int getTotalNotFound() {
		return totalNotFound;
	}

	public int getTotalOk() {
		return totalOk;
	}

	public int getTotalWarnings() {
		return totalWarnings;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {

			String arg = args[i];

			// Argument starts with '-'?
			if (isOpt(arg)) {
				if (arg.equalsIgnoreCase("-codonTables")) codonTables = true;
				else usage("Unknown option '" + arg + "'"); // Options
			} else if (genomeVer.isEmpty()) genomeVer = arg;
			else if (proteinFile.isEmpty()) proteinFile = arg;
			else usage("Unknown parameter '" + arg + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if (proteinFile.isEmpty()) usage("Missing protein_file parameter");
	}

	/**
	 * Compare list of proteins
	 */
	double proteinCompare(String chr, boolean addTotals, boolean updateTranscriptAaCheck) {
		List<Transcript> trList = null;

		// No chromosome name specified? => Use all transcripts
		if (chr == null) {
			trList = new ArrayList<>();
			for (Gene g : genome.getGenes())
				for (Transcript tr : g)
					trList.add(tr);
		} else trList = trByChromo.get(chr);

		// No transcripts in the list? We are done
		if (trList.isEmpty()) return 0;

		int i = 1;

		if (verbose) {
			// Show labels
			System.err.println("\tLabels:");
			System.err.println("\t\t'+' : OK");
			System.err.println("\t\t'.' : Missing");
			System.err.println("\t\t'*' : Error");
			System.out.print((chr != null ? chr : "") + "\t");
		}

		// Check each transcript
		int countNotFound = 0, countOk = 0, countErrors = 0;
		for (Transcript tr : trList) {
			char status = ' ';
			String protein = tr.protein();
			String proteinReference = proteinById.get(tr.getId());

			// Not found using transcript ID? Try protein ID
			if( proteinReference == null && tr.hasProteinId()) {
				proteinReference = proteinById.get(tr.getProteinId());
            }

			if (proteinReference == null) {
				if (tr.isProteinCoding()) {
					status = '.';
					if (debug) Log.warningln("Cannot find Protein for transcript " + tr.getId());
				}
			} else if (equals(protein, proteinReference)) {
				status = '+';
			} else {
				status = '*';

				if (debug || storeAlignments || onlyOneError) {
					protein = proteinFormat(protein);
					proteinReference = proteinFormat(proteinReference);

					SmithWaterman sw = new SmithWaterman(protein, proteinReference);
					if (Math.max(protein.length(), proteinReference.length()) < SnpEffCmdCds.MAX_ALIGN_LENGTH) sw.align();

					if (storeAlignments) alignmentByTrId.put(tr.getId(), sw);

					int maxScore = Math.min(protein.length(), proteinReference.length());
					int score = sw.getAlignmentScore();

					if (debug || onlyOneError) {
						Log.infoln("\nERROR: Proteins do not match for transcript " + tr.getId() //
								+ "\tStrand:" + (tr.isStrandPlus() ? "+" : "-") //
								+ "\tExons: " + tr.numChilds() //
								+ "\n" //
								+ String.format("\tSnpEff protein     (%6d) : '%s'\n", protein.length(), protein) //
								+ String.format("\tReference protein  (%6d) : '%s'\n", proteinReference.length(), proteinReference) //
								+ "\tAlignment (Snpeff protein vs Reference protein)." //
								+ "\tScore: " + score //
								+ "\tMax. possible score: " + maxScore //
								+ "\tDiff: " + (maxScore - score) //
								+ "\n" + sw //
						);
						Log.info("Transcript details:\n" + tr);
					}
				}

				if (onlyOneError) {
					Log.info("Transcript details:\n" + tr);
					throw new RuntimeException("DIE");
				}
			}

			// Update counters
			boolean ok = false;
			switch (status) {
			case '.':
				countNotFound++;
				break;

			case '+':
				countOk++;
				ok = true;
				if (debug) System.out.println("\nFASTA_PROTEIN_OK\t>" + tr.getId() + "\nFASTA_PROTEIN_OK\t" + proteinReference);
				break;

			case '*':
				countErrors++;
				if (debug) System.out.println("\nFASTA_PROTEIN_ERROR\t>" + tr.getId() + "\nFASTA_PROTEIN_ERROR\t" + proteinReference);
				break;

			case ' ':
				break;

			default:
				throw new RuntimeException("Unknown status '" + status + "'");
			}

			// Update transcript status
			if (ok && updateTranscriptAaCheck) tr.setAaCheck(true);

			// Show a mark
			if (verbose && (status != ' ')) {
				System.out.print(status);
				i++;
				if (i % 100 == 0) System.out.print("\n\t");
			}
		}

		// Relative error rate
		double errorRate = ((double) countErrors) / ((double) (countErrors + countOk));
		if (verbose) Log.info("\n");
		System.out.println("\tProtein check:" //
				+ "\t" + genome.getVersion() //
				+ (chr != null ? "\tChromosome: " + chr : "") //
				+ (chr != null ? "\tCodon table: " + CodonTables.getInstance().getTable(genome, chr).getName() : "") //
				+ "\tOK: " + countOk //
				+ "\tNot found: " + countNotFound //
				+ "\tErrors: " + countErrors //
				+ "\tError percentage: " + (100 * errorRate) + "%" //
		);

		// Add to totals
		if (addTotals) {
			totalNotFound += countNotFound;
			totalOk += countOk;
			totalErrors += countErrors;
		} else if (checkNumOk && totalOk <= 0) {
			fatalErrorNoTranscriptsChecked();
		}

		return errorRate;
	}

	/**
	 * Format proteins to make them easier to compare
	 */
	String proteinFormat(String protein) {
		if (protein.isEmpty()) return "";

		// We use upper case letters
		protein = protein.toUpperCase();

		// Stop codon is trimmed
		int idxLast = protein.length() - 1;
		char lastChar = protein.charAt(idxLast);
		if ((lastChar == '*') || (lastChar == '?')) protein = protein.substring(0, idxLast);

		// We use '?' as unknown protein
		protein = protein.replace('X', '?');

		// Remove staring '?' codons
		if (protein.startsWith("?")) protein = protein.substring(1);

		return protein;
	}

	/**
	 * Read a file that has all proteins in fasta format
	 */
	void readProteinFile() {
		if (verbose) Log.info("Reading proteins from file '" + proteinFile + "'...");
		proteinById = new HashMap<>();

		if (proteinFile.endsWith("txt") || proteinFile.endsWith("txt.gz")) readProteinFileTxt();
		else if (proteinFile.endsWith(SnpEffPredictorFactoryGenBank.EXTENSION_GENBANK)) readProteinFileGenBank();
		else if (proteinFile.endsWith(SnpEffPredictorFactoryEmbl.EXTENSION_EMBL)) readProteinFileEmbl();
		else readProteinFileFasta();

		if (verbose) Log.info("done (" + proteinById.size() + " Proteins).");
	}

	/**
	 * Read proteins from EMBL file
	 */
	void readProteinFileEmbl() {
		FeaturesFile featuresFile = new EmblFile(proteinFile);
		readProteinFileFeatures(featuresFile);
	}

	/**
	 * Read Proteins from a file
	 * Format: Tab-separated format, containing "sequence \t transcriptId"
	 */
	void readProteinFileFasta() {
		// Load file
		FastaFileIterator ffi = new FastaFileIterator(proteinFile);
		for (String seq : ffi) {
			String fastaId = ffi.getIdFromFastaHeader(); // Note that this could be transcript_id or protein_id
			add(fastaId, seq, ffi.getLineNum(), true);

			// Also try processing header line using different separators
			List<String> ids = ffi.fastaHeader2Ids();
			for (String id : ids) {
				// We don't check for uniqueness here since many items in this
				// list are tokens that are expected to be repeated
				add(id, seq, ffi.getLineNum(), false);
			}
		}
	}

	/**
	 * Read sequences from features file
	 */
	void readProteinFileFeatures(FeaturesFile featuresFile) {
		for (Features features : featuresFile) {
			String trIdPrev = null;

			for (Feature f : features.getFeatures()) { // Find all CDS
				if (f.getType() == Type.GENE) {
					// Clean up trId
					trIdPrev = null;
				} else if (f.getType() == Type.MRNA) {
					// Save trId, so that next CDS record can find it
					trIdPrev = f.getTranscriptId();
				} else if (f.getType() == Type.CDS) { // Add CDS 'translation' record
					// Try using the transcript ID found in the previous record
					String trId = trIdPrev;
					if (trId == null) trId = f.getTranscriptId();

					String seq = f.getAasequence();

					if (debug) Log.debug(trId + "\t" + seq);
					if ((trId != null) && (seq != null)) add(trId, seq, -1, true);
				}
			}
		}
	}

	/**
	 * Read proteins from geneBank file
	 */
	void readProteinFileGenBank() {
		FeaturesFile featuresFile = new GenBankFile(proteinFile);
		readProteinFileFeatures(featuresFile);
	}

	/**
	 * Read Proteins from a file
	 * Format: Tab-separated format, containing "sequence \t transcriptId"
	 */
	void readProteinFileTxt() {
		// Load file
		String proteinData = Gpr.readFile(proteinFile);
		String proteinLines[] = proteinData.split("\n");

		// Parse each line
		int lineNum = 1;
		for (String proteinLine : proteinLines) {
			// Split tab separated fields
			String field[] = proteinLine.split("\\s+");

			// Parse fields
			if (field.length >= 2) {
				// OK Parse fields
				String seq = field[1].trim();
				String trId = field[0].trim();

				add(trId, seq, lineNum, true);
			}

			lineNum++;
		}
	}

	/**
	 * Replace unknown AA using the reference sequence
	 * @return A new protein sequence including AAs from the Reference sequence in "Unknown sites"
	 */
	String replaceUnknownAa(String protein, String proteinRef) {
		char p[] = protein.toCharArray();
		char pref[] = proteinRef.toCharArray();
		if (p.length != pref.length) return protein; // Nothing done
		for (int i = 0; i < p.length; i++) {
			if (p[i] == '?' && pref[i] != '?') p[i] = pref[i];
		}
		return new String(p);
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		if (verbose) Log.info("Checking database using protein sequences");

		loadConfig(); // Load config
		if (proteinById == null) readProteinFile(); // Read proteins
		loadDb(); // Load database
		return checkProteins(); // Compare proteins
	}

	/**
	 * Show same Transcript IDs
	 */
	String sampleTrIds(int maxTrIds) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (Gene g : config.getGenome().getGenes())
			for (Transcript tr : g) {
				sb.append("\t'" + tr.getId() + "'\n");
				if (count++ > maxTrIds) return sb.toString();
			}
		return sb.toString();
	}

	/**
	 * Show same Transcript IDs from FASTA file
	 */
	String sampleTrIdsFasta(int maxTrIds) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (String trid : proteinById.keySet()) {
			sb.append("\t'" + trid + "'\n");
			if (count++ > maxTrIds) return sb.toString();
		}
		return sb.toString();
	}

	public void setCheckNumOk(boolean checkNumOk) {
		this.checkNumOk = checkNumOk;
	}

	/**
	 * Set codon table for a given chromosome
	 */
	void setCodonTable(Chromosome chromo, CodonTable codonTable) {
		CodonTables.getInstance().set(genome, chromo, codonTable); // Set codon tables

		// Reset all protein translations for this chromosome
		for (Transcript tr : trByChromo.get(chromo.getId()))
			tr.resetCache();
	}

	public void setProteinByTrId(Map<String, String> proteinById) {
		this.proteinById = proteinById;
	}

	public void setMaxErrorRate(double maxErrorRate) { this.maxErrorRate = maxErrorRate; }

	public void setStoreAlignments(boolean storeAlignments) {
		this.storeAlignments = storeAlignments;
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff protein [options] genome_version proteing_file");
		System.err.println("\nOptions:");
		System.err.println("\t-codonTables    : Try all codon tables on each chromosome and calculate error rates.");
		System.exit(-1);
	}
}
