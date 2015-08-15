package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.mcgill.mcb.pcingola.align.SmithWaterman;
import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.collections.AutoHashMap;
import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.genBank.EmblFile;
import ca.mcgill.mcb.pcingola.genBank.Feature;
import ca.mcgill.mcb.pcingola.genBank.Feature.Type;
import ca.mcgill.mcb.pcingola.genBank.Features;
import ca.mcgill.mcb.pcingola.genBank.FeaturesFile;
import ca.mcgill.mcb.pcingola.genBank.GenBankFile;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGenBank;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

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
	public static double MAX_ERROR_RATE = 0.05; // Maximum allowed error is 1% (otherwise test fails)

	boolean codonTables;
	boolean storeAlignments; // Store alignments (used for some test cases)
	int totalErrors = 0;
	int totalOk = 0;
	int totalWarnings = 0;
	int totalNotFound = 0;
	String configFile = Config.DEFAULT_CONFIG_FILE;
	String proteinFile = "";
	HashMap<String, String> proteinByTrId;
	AutoHashMap<String, List<Transcript>> trByChromo;
	HashMap<String, SmithWaterman> alignmentByTrId = new HashMap<String, SmithWaterman>();

	/**
	 * Count number of differences between strings
	 */
	public static int diffCount(String s1, String s2) {
		int minLen = Math.min(s1.length(), s2.length());
		int count = 0;
		for (int j = 0; j < minLen; j++)
			if (s1.charAt(j) != s2.charAt(j)) count++;

		return count;
	}

	/**
	 * Show difference between two strings
	 */
	public static String diffStr(String s1, String s2) {
		// Create a string indicating differences
		int minLen = Math.min(s1.length(), s2.length());
		char diff[] = new char[minLen];
		for (int j = 0; j < minLen; j++) {
			if (s1.charAt(j) != s2.charAt(j)) {
				diff[j] = '|';
			} else diff[j] = ' ';

		}
		return new String(diff);
	}

	public SnpEffCmdProtein() {
	}

	public SnpEffCmdProtein(Config config) {
		this.config = config;
		proteinFile = config.getFileNameProteins();
	}

	public SnpEffCmdProtein(Config config, String proteinFile) {
		this.config = config;
		this.proteinFile = proteinFile;
	}

	public SnpEffCmdProtein(String genomeVer, String configFile, String proteinFile) {
		this.configFile = configFile;
		this.genomeVer = genomeVer;
		this.proteinFile = proteinFile;
	}

	void add(String trId, String seq, int lineNum) {
		// Repeated transcript Id? => Check that Protein is the same
		if ((proteinByTrId.get(trId) != null) && (!proteinByTrId.get(trId).equals(seq))) //
			System.err.println("ERROR: Different protein for the same transcript ID. This should never happen!!!"//
					+ "\n\tLine number   : " + lineNum //
					+ "\n\tTranscript ID : '" + trId + "'"//
					+ "\n\tProtein       : " + proteinByTrId.get(trId) //
					+ "\n\tProtein (new) : " + seq //
		);

		// Pick the first space separated string
		if (trId.indexOf(' ') > 0) trId = trId.split("\\s")[0];

		proteinByTrId.put(trId, seq); // Add it to the hash
		if (debug) Gpr.debug("Adding proteinByTrId{'" + trId + "'} :\t" + seq);
	}

	/**
	 * Check proteins using all possible codon tables
	 */
	void checkCodonTables() {
		if (verbose) Timer.showStdErr("Comparing Proteins...");

		createTrByChromo(); // Create lists of transcripts by chromosome

		// For each chromosome...
		for (Chromosome chromo : genome) {
			String chr = chromo.getId();

			// Check against each codon table
			for (CodonTable codonTable : CodonTables.getInstance()) {
				setCodonTable(chromo, codonTable);
				proteinCompare(chr, false);
			}
		}

		if (verbose) Timer.showStdErr("done");
	}

	/**
	 * Check proteins
	 */
	void checkProteins() {
		if (verbose) Timer.showStdErr("Comparing Proteins...");

		if (codonTables) {
			// Compare proteins using ALL codon tables
			checkCodonTables();
		} else {
			// Compare proteins
			proteinCompare(null, true);
		}
	}

	void createTrByChromo() {
		trByChromo = new AutoHashMap<String, List<Transcript>>(new ArrayList<Transcript>());

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

		// Remove last AA in Ref sequence (e.g. when las AA in reference by is 'unknown')
		String refUnk = "";
		if (proteinRef.length() > 0) {
			refUnk = proteinRef.substring(0, proteinRef.length() - 1);
			if (protein.equals(refUnk)) return true;
		}

		// Replace First AA by 'Met'? Start codon may be translated as Met even if it normally encodes other AA.
		// Compare everything but start codon.
		String proteinNoStart = "", refNoStart = "";
		if ((protein.length() > 0) && (proteinRef.length() > 0)) {
			proteinNoStart = protein.substring(1);
			refNoStart = proteinRef.substring(1);
			if (proteinNoStart.equals(refNoStart)) return true;
		}

		// Rare amino acid translations (U)
		String proteinU = protein.replace('*', 'U');
		if (proteinU.equals(proteinRef) || proteinU.equals(refUnk)) return true;

		String proteinNoStartU = proteinNoStart.replace('*', 'U');
		if (proteinNoStartU.equals(refNoStart)) return true;

		return false;
	}

	public HashMap<String, SmithWaterman> getAlignmentByTrId() {
		return alignmentByTrId;
	}

	public int getTotalErrors() {
		return totalErrors;
	}

	public int getTotalOk() {
		return totalOk;
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
	double proteinCompare(String chr, boolean addTotals) {
		List<Transcript> trList = null;

		// No chromosome name specified? => Use all transcripts
		if (chr == null) {
			trList = new ArrayList<Transcript>();
			for (Gene g : genome.getGenes())
				for (Transcript tr : g)
					trList.add(tr);
		} else trList = trByChromo.get(chr);

		// No transcripts in the list? We are done
		if (trList.isEmpty()) return 0;

		int i = 1;
		if (verbose) System.out.print((chr != null ? chr : "") + "\t");

		// Check each transcript
		int countNotFound = 0, countOk = 0, countErrors = 0, countWarnings = 0;
		for (Transcript tr : trList) {
			String protein = tr.protein();
			String proteinReference = proteinByTrId.get(tr.getId());

			if (proteinReference == null) {
				if (tr.isProteinCoding()) {
					countNotFound++;
					if (debug) System.err.println("\nWARNING:Cannot find Protein for transcript " + tr.getId());
					else if (verbose) System.out.print('-');
				}
			} else if (equals(protein, proteinReference)) {
				countOk++;
				if (addTotals) tr.setAaCheck(true);
				if (verbose) System.out.print('+');
			} else {
				if (debug || storeAlignments || onlyOneError) {
					protein = proteinFormat(protein);
					proteinReference = proteinFormat(proteinReference);

					SmithWaterman sw = new SmithWaterman(protein, proteinReference);
					if (Math.max(protein.length(), proteinReference.length()) < SnpEffCmdCds.MAX_ALIGN_LENGTH) sw.align();

					if (storeAlignments) alignmentByTrId.put(tr.getId(), sw);

					int maxScore = Math.min(protein.length(), proteinReference.length());
					int score = sw.getAlignmentScore();

					if (debug || onlyOneError) {
						System.err.println("\nERROR: Proteins do not match for transcript " + tr.getId() //
								+ "\tStrand:" + (tr.isStrandPlus() ? 1 : -1) //
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
						System.err.println("Transcript details:\n" + tr);
					}

				} else if (verbose) System.out.print('*');

				countErrors++;

				if (onlyOneError) {
					System.err.println("Transcript details:\n" + tr);
					throw new RuntimeException("DIE");
				}
			}

			// Show a mark
			if (verbose && (i % 100 == 0)) System.out.print("\n\t");
			i++;
		}

		// Relative error rate
		double errorRate = ((double) countErrors) / ((double) (countErrors + countOk));
		if (verbose) System.out.println("\n");
		System.out.println("\tProtein check:" //
				+ "\t" + genome.getVersion() //
				+ (chr != null ? "\tChromosome: " + chr : "") //
				+ (chr != null ? "\tCodon table: " + CodonTables.getInstance().getTable(genome, chr).getName() : "") //
				+ "\tOK: " + countOk //
				+ "\tWarnings: " + countWarnings //
				+ "\tNot found: " + countNotFound //
				+ "\tErrors: " + countErrors //
				+ "\tError percentage: " + (100 * errorRate) + "%" //
		);

		// Add to totals
		if (addTotals) {
			totalNotFound += countNotFound;
			totalOk += countOk;
			totalErrors += countErrors;
			totalWarnings += countWarnings;
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
		if (verbose) Timer.showStdErr("Reading proteins from file '" + proteinFile + "'...");
		proteinByTrId = new HashMap<String, String>();

		if (proteinFile.endsWith("txt") || proteinFile.endsWith("txt.gz")) readProteinFileTxt();
		else if (proteinFile.endsWith(SnpEffPredictorFactoryGenBank.EXTENSION_GENBANK)) readProteinFileGenBank();
		else if (proteinFile.endsWith(SnpEffPredictorFactoryEmbl.EXTENSION_EMBL)) readProteinFileEmbl();
		else readProteinFileFasta();

		if (verbose) Timer.showStdErr("done (" + proteinByTrId.size() + " Proteins).");
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
			// String trId = ffi.getTranscriptId();
			String trId = ffi.getName();
			add(trId, seq, ffi.getLineNum());
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

					if (debug) Gpr.debug(trId + "\t" + seq);
					if ((trId != null) && (seq != null)) add(trId, seq, -1);
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

				add(trId, seq, lineNum);
			}

			lineNum++;
		}
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		if (verbose) Timer.showStdErr("Checking database using protein sequences");

		loadConfig(); // Load config
		readProteinFile(); // Read proteins
		loadDb(); // Load database
		checkProteins(); // Compare proteins

		return true;
	}

	/**
	 * Set codon table for a given chromosome
	 */
	void setCodonTable(Chromosome chromo, CodonTable codonTable) {
		CodonTables.getInstance().set(genome, chromo, codonTable); // Set codon tables

		// Reset all protein translations for this chromosome
		for (Transcript tr : trByChromo.get(chromo.getId()))
			tr.resetCdsCache();
	}

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
