package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test NextProt databases
 *
 * @author pcingola
 */
public class TestCasesIntegrationNextProt extends TestCasesIntegrationBase {

	public TestCasesIntegrationNextProt() {
		super();
	}

	@Test
	public void test_01_build() {
		Gpr.debug("Test");
		String args[] = { "buildNextProt", "testHg3770Chr22", "tests/nextProt" };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertEquals(true, ok);
	}

	@Test
	public void test_02_ann() {
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'HIGH' impact, but
		// since the database we build in test_01_build is small, there
		// are not enough stats.
		checkNextProt("testHg3770Chr22" //
				, "tests/test_nextProt_02.vcf"//
				, "amino_acid_modification:N-acetylglycine"//
				, EffectImpact.LOW //
				, true //
		);
	}

	@Test
	public void test_02_eff() {
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'HIGH' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, "tests/test_nextProt_02.vcf" //
				, "amino_acid_modification:N-acetylglycine" //
				, EffectImpact.LOW //
				, false //
		);
	}

	@Test
	public void test_03_ann() {
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, "tests/test_nextProt_03.vcf" //
				, "amino_acid_modification:Phosphoserine" //
				, EffectImpact.MODERATE //
				, true //
		);
	}

	@Test
	public void test_03_eff() {
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
		// since the database we build in test_01_build is small, there are
		// not enough stats.
		checkNextProt("testHg3770Chr22" //
				, "tests/test_nextProt_03.vcf" //
				, "amino_acid_modification:Phosphoserine" //
				, EffectImpact.MODERATE //
				, false //
		);
	}

	@Test
	public void test_04_parse() {
		Gpr.debug("Test");
		String vcfFile = "tests/test.nextProt_paren.vcf";
		int count = 0;
		for (VcfEntry ve : new VcfFileIterator(vcfFile)) {
			for (VcfEffect eff : ve.getVcfEffects()) {
				if (verbose) System.out.println(eff);
				if (eff.hasEffectType(EffectType.NEXT_PROT)) count++;
			}
		}

		if (verbose) System.out.println("Count: " + count);
		Assert.assertTrue(count > 0);

	}
}
