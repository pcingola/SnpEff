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

	@Test
	public void test_05_parse() {
		// TODO: Parse glycosilation site
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_06_parse() {
		// TODO: Parse single AA annotation spanning an exon boundary
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_07_parse() {
		// TODO: Parse multiple AA annotation spanning two exon boundaries
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_08_parse() {
		// TODO: Parse disulphide bond annotations (start end is not an interval, it's the interaction points)
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_09() {
		// TODO: Test annotaion
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_10() {
		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_11() {
		// TODO: Test annotation highly conserved (non-synonymous change) => EffectImpact.HIGH;
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_12() {
		// TODO: Test annotation highly conserved (INDEL) => EffectImpact.HIGH;
		Log.debug("Test");
		throw new RuntimeException("Unimplemented test");
	}

}
