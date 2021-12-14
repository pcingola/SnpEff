package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Log;

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
	public void test_01_build() {
		Log.debug("Test");
		verbose = true;
		String args[] = { "buildNextProt", "testHg3770Chr22", path("nextProt") };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertEquals(true, ok);
	}


}
