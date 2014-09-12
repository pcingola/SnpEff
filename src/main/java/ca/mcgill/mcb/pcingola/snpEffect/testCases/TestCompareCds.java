package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Compare our CDS (from transcript sequences) to sequences downloaded from database
 *
 * @author pcingola
 */
public class TestCompareCds {

	public static boolean debug = false;
	public static boolean quiet = false | debug;
	public static boolean verbose = false | debug;
	public static boolean onlyOneError = false;
	public static double maxErrorPercentage = 0.01; // Maximum allowed error is 1% (otherwise test fails)

	Config config;
	Map<String, String> cdsByTrId;
	int totalErrors = 0;
	int totalOk = 0;
	int totalWarnings = 0;
	int totalNotFound = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = "snpEff.config";
		String genomeToTest = "";

		// Parse command line arguments
		if (args.length <= 0) {
			System.err.println("Usage: TestCaseCompareCds genomeToTest\n");
			System.exit(-1);
		}

		// Command line argument parsing
		if (args[0].equals("-c")) {
			configFile = args[1];
			genomeToTest = args[2];
		} else if (args[0].equals("-d")) {
			debug = verbose = true;
			genomeToTest = args[1];
		} else {
			genomeToTest = args[0];
		}

		TestCompareCds.test(genomeToTest, configFile);
	}

	public static void test(String genomeVersion) {
		test(genomeVersion, "snpEff.config");
	}

	/**
	 * Execute a test for a given genome version
	 */
	public static void test(String genomeVersion, String configFile) {
		if (!quiet) System.out.println("CDS test for " + genomeVersion);
		TestCompareCds testCompareCds = new TestCompareCds(genomeVersion, configFile);
		testCompareCds.readCdsFile();
		double perc = testCompareCds.cdsCompare();
		if (perc > maxErrorPercentage) throw new RuntimeException("Too many errors. Percentaje : " + (100 * perc) + "%");
	}

	public TestCompareCds(String genomeVersion, String configFile) {
		config = new Config(genomeVersion, configFile);
		cdsByTrId = new HashMap<String, String>();

		try {
			if (!quiet) System.out.print("Loading predictor " + config.getGenome().getVersion() + " ");
			config.loadSnpEffectPredictor();
			if (!quiet) System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compare all CDS
	 */
	public double cdsCompare() {
		int i = 1;
		for (Gene gint : config.getGenome().getGenes())
			for (Transcript tint : gint) {
				String cds = tint.cds().toUpperCase();
				String cdsAndUtrs = tint.mRna().toUpperCase();
				String cdsReference = cdsByTrId.get(tint.getId());

				if ((cdsReference == null) || (cds.length() <= 0)) {
					if (verbose) System.err.println("\nWARNING:Cannot find CDS for transcript " + tint.getId());
					totalNotFound++;
					if (verbose && !quiet) System.out.print('.');
				} else if (cds.equals(cdsReference)) {
					totalOk++;
					if (verbose && !quiet) System.out.print('+');

					// Sanity check: Start and stop codons
					if ((cds != null) && (cds.length() >= 3)) {
						CodonTable ctable = CodonTables.getInstance().getTable(config.getGenome(), tint.getChromosomeName());
						String startCodon = cds.substring(0, 3);
						if (!ctable.isStart(startCodon)) {
							if (verbose) System.err.println("\nWARNING: CDS for transcript '" + tint.getId() + "' does not start with a start codon:\n\t" + startCodon + "\t" + cds);
							totalWarnings++;
						}
						String stopCodon = cds.substring(cds.length() - 3, cds.length());
						if (!ctable.isStop(stopCodon)) {
							if (verbose) System.err.println("\nWARNING: CDS for transcript '" + tint.getId() + "' does not end with a stop codon:\n\t" + stopCodon + "\t" + cds);
							totalWarnings++;
						}
					}
				} else if (cdsAndUtrs.equals(cdsReference)) {
					totalOk++;
					if (verbose && !quiet) System.out.print('-');
					// Do not check start and stop codons (they will be wrong)
				} else {
					if (verbose || onlyOneError) System.err.println("\nERROR:CDS do not match for transcript " + tint.getId() + "\tStrand:" + tint.getStrand() + "\tExons: " + tint.numChilds() + "\n\tsnpEff (" + cds.length() + "):\t" + cds.toLowerCase() + "\n\tReference(" + cdsReference.length() + "):\t" + cdsReference.toLowerCase() + "\nTranscript details:\n" + tint);
					else if (!quiet) System.out.print('*');

					totalErrors++;

					if (onlyOneError) {
						System.err.println("Transcript details:\n" + tint);
						throw new RuntimeException("DIE");
					}
				}

				if (verbose && !quiet && (i % 100 == 0)) System.out.println("");
				i++;
			}

		double perc = ((double) totalErrors) / ((double) (totalErrors + totalOk));
		if (!quiet) System.out.println("");
		System.out.println(config.getGenome().getVersion() + "\tOK: " + totalOk + "\tWarnings: " + totalWarnings + "\tNot found: " + totalNotFound + "\tErrors: " + totalErrors + "\tError percentage: " + (100 * perc) + "%");
		return perc;
	}

	/**
	 * Read a file with all CDS.
	 * Format : Fasta
	 * @param cdsFileName
	 */
	void readCdsFastaFile(String cdsFastaFileName) {
		int lineNum = 1;
		if (!quiet) System.out.print("Reading file:" + cdsFastaFileName + "\t");

		FastaFileIterator ffi = new FastaFileIterator(cdsFastaFileName);
		for (String cds : ffi) {
			// Transcript ID
			String trid = GprSeq.readId(ffi.getHeader());

			// Repeated transcript Id? => Check that CDS is the same
			if ((cdsByTrId.get(trid) != null) && (!cdsByTrId.get(trid).equals(cds))) System.err.println("ERROR: Different CDS for the same transcript ID. This should never happen!!!\n\tLine number: " + lineNum + "\n\tTranscript ID:\t" + trid + "\n\tCDS:\t\t" + cdsByTrId.get(trid) + "\n\tCDS (new):\t" + cds);

			cdsByTrId.put(trid, cds);
			lineNum++;
		}
		if (!quiet) System.out.println("done reading " + cdsByTrId.size() + " CDSs");
	}

	void readCdsFile() {
		// Try tab separated file
		String cdsFileName = config.getDirDataVersion() + "/cds.txt.gz";
		File file = new File(cdsFileName);
		if (file.exists()) {
			readCdsFile(cdsFileName);
			return;
		}

		// Try fasta file
		String cdsFastaFileName = config.getDirDataVersion() + "/cds.fa.gz";
		file = new File(cdsFastaFileName);
		if (file.exists()) {
			readCdsFastaFile(cdsFastaFileName);
			return;
		}

		// Error!
		throw new RuntimeException("Neither '" + cdsFileName + "' nor '" + cdsFastaFileName + "' file exists!");
	}

	/**
	 * Read a file with all CDS.
	 * Format : "transcriptID \t cdsSequence \n"
	 * @param cdsFileName
	 */
	void readCdsFile(String cdsFileName) {
		int lineNum = 1;
		if (!quiet) System.out.print("Reading file:" + cdsFileName + "\t");
		String cdsFile = Gpr.readFile(cdsFileName);
		String cdsLines[] = cdsFile.split("\n");
		for (String line : cdsLines) {
			String records[] = line.split("\\t");
			String trid = records[0].trim();
			String cds = records[1].trim().toUpperCase();

			// Repeated transcript Id? => Check that CDS is the same
			if ((cdsByTrId.get(trid) != null) && (!cdsByTrId.get(trid).equals(cds))) System.err.println("ERROR: Different CDS for the same transcript ID. This should never happen!!!\n\tLine number: " + lineNum + "\n\tTranscript ID:\t" + trid + "\n\tCDS:\t\t" + cdsByTrId.get(trid) + "\n\tCDS (new):\t" + cds);

			cdsByTrId.put(trid, cds);
			lineNum++;
		}
		if (!quiet) System.out.println("done reading " + cdsByTrId.size() + " CDSs");
	}
}
