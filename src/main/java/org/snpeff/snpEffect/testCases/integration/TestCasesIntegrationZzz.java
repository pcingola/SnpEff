package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	long randSeed = 20100629;
	String genomeName = "testCase";

	public TestCasesIntegrationZzz() {
		super();
	}

//	@Test
//	public void test_01_build() {
//		Log.debug("Test");
//		String args[] = { "buildNextProt", "testHg3770Chr22", path("nextProt") };
//		SnpEff snpEff = new SnpEff(args);
//		snpEff.setVerbose(verbose);
//		snpEff.setSupressOutput(!verbose);
//		boolean ok = snpEff.run();
//		Assert.assertEquals(true, ok);
//	}

//	@Test
//	public void test_02_ann() {
//		Log.debug("Test");
//		verbose = true;
//		// Note: Normally this EffectImpact should be 'HIGH' impact, but
//		// since the database we build in test_01_build is small, there
//		// are not enough stats.
//		checkNextProt("testHg3770Chr22" //
//				, path("test_nextProt_02.vcf")//
//				, "modified-residue_N-acetylglycine"//
//				, VariantEffect.EffectImpact.LOW //
//				, true //
//		);
//	}
//
//	@Test
//	public void test_02_eff() {
//		Log.debug("Test");
//		verbose = true;
//		// Note: Normally this EffectImpact should be 'HIGH' impact, but
//		// since the database we build in test_01_build is small, there are
//		// not enough stats.
//		checkNextProt("testHg3770Chr22" //
//				, path("test_nextProt_02.vcf") //
//				, "modified-residue_N-acetylglycine" //
//				, VariantEffect.EffectImpact.LOW //
//				, false //
//		);
//	}
//
//	@Test
//	public void test_03_ann() {
//		Log.debug("Test");
//		verbose = true;
//		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
//		// since the database we build in test_01_build is small, there are
//		// not enough stats.
//		checkNextProt("testHg3770Chr22" //
//				, path("test_nextProt_03.vcf") //
//				, "modified-residue_Phosphoserine" //
//				, VariantEffect.EffectImpact.MODERATE //
//				, true //
//		);
//	}
//
//	@Test
//	public void test_03_eff() {
//		Log.debug("Test");
//		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
//		// since the database we build in test_01_build is small, there are
//		// not enough stats.
//		checkNextProt("testHg3770Chr22" //
//				, path("test_nextProt_03.vcf") //
//				, "modified-residue_Phosphoserine" //
//				, VariantEffect.EffectImpact.MODERATE //
//				, false //
//		);
//	}

	@Test
	public void test_04_parse() {
		Log.debug("Test");
		verbose = true;
		String vcfFile = path("test.nextProt_paren.vcf");
		int count = 0;
		for (VcfEntry ve : new VcfFileIterator(vcfFile)) {
			for (VcfEffect eff : ve.getVcfEffects()) {
				if (verbose) Log.info(eff);
				if (eff.hasEffectType(EffectType.NEXT_PROT)) count++;
			}
		}

		if (verbose) Log.info("Count: " + count);
		Assert.assertTrue(count > 0);
	}
}
