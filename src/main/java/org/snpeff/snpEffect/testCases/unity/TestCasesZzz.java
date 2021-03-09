package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Log;

import junit.framework.Assert;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/covid19/";
	}

	/**
	 * TODO: Merge into 'TestCasesIntegrationCovid19'
	 */
	@Test
	public void test_01() {
		Log.debug("Test");
		verbose = true;
		debug = true;
		String genome = "test_NC_045512_01";
		SnpEffCmdBuild buildCmd = buildGetBuildCmd(genome);

		// Make sure all proteins are OK
		SnpEffCmdProtein protCmd = buildCmd.getSnpEffCmdProtein();
		Assert.assertEquals(2, protCmd.getTotalOk());
		Assert.assertEquals(0, protCmd.getTotalErrors());
		Assert.assertEquals(0, protCmd.getTotalWarnings());
		Assert.assertEquals(0, protCmd.getTotalNotFound());
	}
}
