package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test NextProt databases
 *
 * @author pcingola
 */
public class TestCasesIntegrationNextProt extends TestCasesIntegrationBase {

	@Test
	public void test_01_build() {
		Log.debug("Test");
		String args[] = { "buildNextProt", "testHg3770Chr22", path("nextProt") };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertEquals(true, ok);
	}

	@Test
	public void test_02_ann() {
		Log.debug("Test");
		// Note: Normally this EffectImpact should be 'HIGH' impact, but
		// since the database we build in test_01_build is small, there
		// are not enough stats.
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_02.vcf")//
				, "amino_acid_modification:N-acetylglycine"//
				, EffectImpact.LOW //
				, true //
		);
	}

	@Test
	public void test_02_eff() {
		Log.debug("Test");
		// Note: Normally this EffectImpact should be 'HIGH' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_02.vcf") //
				, "amino_acid_modification:N-acetylglycine" //
				, EffectImpact.LOW //
				, false //
		);
	}

	@Test
	public void test_03_ann() {
		Log.debug("Test");
		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_03.vcf") //
				, "amino_acid_modification:Phosphoserine" //
				, EffectImpact.MODERATE //
				, true //
		);
	}

	@Test
	public void test_03_eff() {
		Log.debug("Test");
		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_03.vcf") //
				, "amino_acid_modification:Phosphoserine" //
				, EffectImpact.MODERATE //
				, false //
		);
	}

	@Test
	public void test_04_parse() {
		Log.debug("Test");
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
