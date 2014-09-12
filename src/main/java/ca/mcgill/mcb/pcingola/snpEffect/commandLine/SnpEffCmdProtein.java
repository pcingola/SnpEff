package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.HashMap;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.SmithWaterman;
import ca.mcgill.mcb.pcingola.genBank.Feature;
import ca.mcgill.mcb.pcingola.genBank.Feature.Type;
import ca.mcgill.mcb.pcingola.genBank.Features;
import ca.mcgill.mcb.pcingola.genBank.FeaturesFile;
import ca.mcgill.mcb.pcingola.genBank.GenBankFile;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
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

	int totalErrors = 0;
	int totalOk = 0;
	int totalWarnings = 0;
	int totalNotFound = 0;
	String configFile = Config.DEFAULT_CONFIG_FILE;
	String proteinFile = "";
	HashMap<String, String> proteinByTrId;

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
	 * Check proteins
	 */
	void checkProteins() {
		double err = proteinCompare(); // Compare proteins

		if (err > MAX_ERROR_RATE) {
			HashMap<String, Double> errRates = new HashMap<String, Double>();
			for (CodonTable codonTable : CodonTables.getInstance()) {
				System.out.println(codonTable.getName());
				err = proteinCompare();

				config.getGenome().codonTable();
				errRates.put(codonTable.getName(), err);
			}

			for (String ct : errRates.keySet())
				System.out.println(ct + "\t\t" + errRates.get(ct));
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

			// Argument starts with '-'?
			if (args[i].startsWith("-")) usage("Unknow option '" + args[i] + "'"); // Options
			else if (genomeVer.isEmpty()) genomeVer = args[i];
			else if (proteinFile.isEmpty()) proteinFile = args[i];
			else usage("Unknow parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if (proteinFile.isEmpty()) usage("Missing protein_file parameter");
	}

	/**
	 * Compare all Protein
	 */
	double proteinCompare() {
		if (verbose) Timer.showStdErr("Comparing Proteins...");
		int i = 1;
		if (verbose) System.out.print('\t');

		for (Gene gene : config.getGenome().getGenes()) {

			for (Transcript tr : gene) {
				String protein = tr.protein();
				String proteinReference = proteinByTrId.get(tr.getId());

				if (proteinReference == null) {
					if (tr.isProteinCoding()) {
						totalNotFound++;
						if (debug) System.err.println("\nWARNING:Cannot find Protein for transcript " + tr.getId());
						else if (verbose) System.out.print('-');
					}
				} else if (equals(protein, proteinReference)) {
					totalOk++;
					tr.setAaCheck(true);
					if (verbose) System.out.print('+');
				} else {
					if (debug || onlyOneError) {
						protein = proteinFormat(protein);
						proteinReference = proteinFormat(proteinReference);

						SmithWaterman sw = new SmithWaterman(protein, proteinReference);
						if (Math.max(protein.length(), proteinReference.length()) < SnpEffCmdCds.MAX_ALIGN_LENGTH) sw.align();

						int maxScore = Math.min(protein.length(), proteinReference.length());
						int score = sw.getAligmentScore();
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

					} else if (verbose) System.out.print('*');

					totalErrors++;

					if (onlyOneError) {
						System.err.println("Transcript details:\n" + tr);
						throw new RuntimeException("DIE");
					}
				}

				// Show a mark
				if (verbose && (i % 100 == 0)) System.out.print("\n\t");
				i++;
			}
		}

		// Relative error rate
		double errorRate = ((double) totalErrors) / ((double) (totalErrors + totalOk));
		System.out.println("\n\tProtein check:" //
				+ "\t" + config.getGenome().getVersion() //
				+ "\tOK: " + totalOk //
				+ "\tWarnings: " + totalWarnings //
				+ "\tNot found: " + totalNotFound //
				+ "\tErrors: " + totalErrors //
				+ "\tError percentage: " + (100 * errorRate) + "%" //
		);

		if (verbose) Timer.showStdErr("done");
		return errorRate;
	}

	/**
	 * Format proteins to make them easier to compare
	 */
	String proteinFormat(String protein) {
		if (protein.isEmpty()) return "";

		// We use uppercase letters
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
		else readProteinFileFasta();

		if (verbose) Timer.showStdErr("done (" + proteinByTrId.size() + " Proteins).");
	}

	/**
	 * Read Proteins from a file
	 * Format: Tab-separated format, containing "sequence \t transcriptId"
	 */
	void readProteinFileFasta() {
		// Load file
		FastaFileIterator ffi = new FastaFileIterator(proteinFile);
		for (String seq : ffi) {
			String trId = ffi.getTranscriptId();
			add(trId, seq, ffi.getLineNum());
		}
	}

	/**
	 * Read proteins from geneBank file
	 */
	void readProteinFileGenBank() {
		FeaturesFile featuresFile = new GenBankFile(proteinFile);
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
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff protein [options] genome_version proteing_file");
		System.err.println("\nOptions:");
		System.err.println("\t-c , -config            : Specify config file");
		System.err.println("\t-noLog                  : Do not report usage statistics to server");
		System.err.println("\t-v , -verbose           : Verbose mode");
		System.exit(-1);
	}
}
