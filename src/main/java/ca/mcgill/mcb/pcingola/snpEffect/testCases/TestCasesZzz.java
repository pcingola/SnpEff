package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Annotating LOF / NMD using a geneName that contains spaces triggers 
	 * an Exception (it shouldn't happen)
	 */
	public void test_26_Annotating_LOF_Spaces() {
		String vcfFileName = "tests/vcf_genes_spaces.vcf";
		String genomeName = "test_ENSG00000158062_spaces";

		// Prepare a command line
		String args[] = { "-noLog", genomeName, vcfFileName };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setDebug(debug);

		// This should run OK
		snpEff.run();
	}

}
