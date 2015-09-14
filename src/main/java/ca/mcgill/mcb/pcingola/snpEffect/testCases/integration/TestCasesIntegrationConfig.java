package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesIntegrationConfig {

	boolean debug = false;
	boolean verbose = false;

	/**
	 * Check that config file can be overriden by command line options
	 */
	@Test
	public void test_01_ConfigOverride() {
		Gpr.debug("Test");

		// Create command
		String repo = "http://nonsense.url/test/zzz";
		String args[] = { //
				"-configOption" //
				, Config.KEY_DATABASE_REPOSITORY + "=" + repo //
				, "testHg3775Chr22" //
				, "tests/test_ann_01.vcf" //
		};

		// Create command and run
		SnpEff cmd = new SnpEff(args);
		cmd.setSupressOutput(!verbose);
		cmd.setVerbose(verbose);
		cmd.setDebug(debug);
		cmd.run();

		// Check that config option really changed
		if (verbose) System.out.println("Repository: " + cmd.getConfig().getDatabaseRepository());
		Assert.assertEquals(repo, cmd.getConfig().getDatabaseRepository());
	}

}
