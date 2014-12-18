package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGff3;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesBuild {

	boolean verbose = false;

	@Test
	public void test_01_chromoNamesDoNotMatch_Gff() {
		Gpr.debug("Test");

		String genome = "testChromoNamesDoNotMatch";
		String gff = "tests/testChromoNamesDoNotMatch.genes.gff";

		// Expected error message
		String expectedError = "Error reading file 'tests/testChromoNamesDoNotMatch.genes.gff'" //
				+ "\njava.lang.RuntimeException: FATAL ERROR: Most Exons do not have sequences!\n" //
				+ "There might be differences in the chromosome names used in the genes file ('tests/testChromoNamesDoNotMatch.genes.gff')\n" //
				+ "and the chromosme names used in the 'reference sequence' file.\n" //
				+ "Please check that chromosome names in both files match.\n" //
				+ "\tChromosome names missing in 'reference sequence' file:\t'1'\n" //
				+ "\tChromosome names missing in 'genes' file             :\t'1ZZZ'\n" //"
		;

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 sefGff = new SnpEffPredictorFactoryGff3(config);
		sefGff.setFileName(gff);
		sefGff.setVerbose(verbose);

		// Run: We expect an error
		try {
			sefGff.create();
		} catch (Throwable t) {
			String errmsg = t.getMessage().substring(0, expectedError.length());
			if (verbose) t.printStackTrace();
			Assert.assertEquals(expectedError, errmsg);
			return;
		}
		throw new RuntimeException("Expected error not found!");
	}

	@Test
	public void test_02_chromoNamesDoNotMatch_GffFasta() {
		Gpr.debug("Test");

		String genome = "testChromoNamesDoNotMatch";
		String gff = "tests/testChromoNamesDoNotMatch.genes.no_fasta.gff";
		String fasta = "tests/testChromoNamesDoNotMatch.fa";

		// Expected error message
		String expectedError = "Error reading file 'tests/testChromoNamesDoNotMatch.genes.no_fasta.gff'" //
				+ "\njava.lang.RuntimeException: FATAL ERROR: Most Exons do not have sequences!\n" //
				+ "There might be differences in the chromosome names used in the genes file ('tests/testChromoNamesDoNotMatch.genes.no_fasta.gff')\n" //
				+ "and the chromosme names used in the 'reference sequence' file ('tests/testChromoNamesDoNotMatch.fa').\n" //
				+ "Please check that chromosome names in both files match.\n" //
				+ "\tChromosome names missing in 'reference sequence' file:\t'1'\n" //
				+ "\tChromosome names missing in 'genes' file             :\t'1ZZZ'\n" //"
		;

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 sefGff = new SnpEffPredictorFactoryGff3(config);
		sefGff.setFileName(gff);
		sefGff.setFastaFile(fasta);
		sefGff.setVerbose(verbose);

		// Run: We expect an error
		try {
			sefGff.create();
		} catch (Throwable t) {
			int len = Math.min(t.getMessage().length(), expectedError.length());
			String errmsg = t.getMessage().substring(0, len);
			if (verbose) t.printStackTrace();
			Assert.assertEquals(expectedError, errmsg);
			return;
		}
		throw new RuntimeException("Expected error not found!");
	}

}
