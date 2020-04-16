package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	long randSeed = 20100629;
	String genomeName = "testCase";

	public TestCasesIntegrationZzz() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		verbose = debug = true;
		String genome = "test_NC_045512_01";
		SnpEffCmdBuild buildCmd = buildGetBuildCmd(genome);

		// Make sure all proteins are OK
		SnpEffCmdProtein protCmd = buildCmd.getSnpEffCmdProtein();
		Assert.assertEquals(5, protCmd.getTotalOk());
		Assert.assertEquals(0, protCmd.getTotalErrors());
		Assert.assertEquals(0, protCmd.getTotalWarnings());
	}

}
