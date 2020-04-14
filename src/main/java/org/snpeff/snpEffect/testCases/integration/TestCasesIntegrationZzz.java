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
		String genome = "test_NC_045512_01";
		SnpEffCmdBuild buildCmd = buildGetBuildCmd(genome);

		// Make sure all proteins are OK
		SnpEffCmdProtein protCmd = buildCmd.getSnpEffCmdProtein();
		Assert.assertEquals(2, protCmd.getTotalOk());
		Assert.assertEquals(0, protCmd.getTotalErrors());
		Assert.assertEquals(0, protCmd.getTotalWarnings());
		Assert.assertEquals(0, protCmd.getTotalNotFound());
	}

	//	@Test
	//	public void test_02_ann() {
	//		Gpr.debug("Test");
	//		// Note: Normally this EffectImpact should be 'HIGH' impact, but
	//		// since the database we build in test_01_build is small, there
	//		// are not enough stats.
	//		checkNextProt("testHg3770Chr22" //
	//				, path("test_nextProt_02.vcf")//
	//				, "amino_acid_modification:N-acetylglycine"//
	//				, EffectImpact.LOW //
	//				, true //
	//		);
	//	}
	//
	//	@Test
	//	public void test_03_ann() {
	//		Gpr.debug("Test");
	//		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
	//		// since the database we build in test_01_build is small, there are
	//		// not enough stats.
	//		checkNextProt("testHg3770Chr22" //
	//				, path("test_nextProt_03.vcf") //
	//				, "amino_acid_modification:Phosphoserine" //
	//				, EffectImpact.MODERATE //
	//				, true //
	//		);
	//	}

}
