package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsProtein;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	protected void checkEffects(Variant variant, EffectType expEffs[], String expHgvsp[], String expHgvsc[], EffectImpact expectedImpact) {
		// Convert to sets
		Set<EffectType> expectedEffs = new HashSet<>();
		if (expEffs != null) {
			for (EffectType et : expEffs)
				expectedEffs.add(et);
		}

		Set<String> expectedHgvsp = new HashSet<>();
		if (expHgvsp != null) {
			for (String h : expHgvsp)
				expectedHgvsp.add(h);
		}

		Set<String> expectedHgvsc = new HashSet<>();
		if (expHgvsc != null) {
			for (String h : expHgvsc)
				expectedHgvsc.add(h);
		}

		// Initialize
		initSnpEffPredictor();

		if (verbose) {
			Gpr.debug("Variant: " + variant);
			for (Gene g : genome.getGenes()) {
				Gpr.debug("\tGene: " + g.getId());
				for (Transcript tr : g)
					Gpr.debug(tr + "\n\n" + tr.toStringAsciiArt(true));
			}
		}

		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

		// Checknumber of results
		Assert.assertEquals(true, effects.size() >= 1);

		Set<EffectType> effs = new HashSet<>();
		Set<String> hgvscs = new HashSet<>();
		Set<String> hgvsps = new HashSet<>();
		boolean impactOk = false;
		for (VariantEffect varEff : effects) {
			effs.addAll(varEff.getEffectTypes());

			HgvsDna hgvsc = new HgvsDna(varEff);
			String hgvsDna = hgvsc.toString();
			hgvscs.add(hgvsDna);

			HgvsProtein hgvsp = new HgvsProtein(varEff);
			String hgvsProt = hgvsp.toString();
			hgvsps.add(hgvsProt);

			impactOk |= (varEff.getEffectImpact() == expectedImpact);

			if (verbose) Gpr.debug("Effect: " + varEff.toStr() //
					+ "\n\tHGVS.c: " + hgvsDna //
					+ "\n\tHGVS.p: " + hgvsProt //
			);
		}

		// Check effects
		Assert.assertTrue("Effects do not match" //
				+ "\n\tExpected : " + expectedEffs //
				+ "\n\tFound    : " + effs//
				, effs.containsAll(expectedEffs) //
		);

		// Check impact
		Assert.assertTrue("Effect impact '" + expectedImpact + "' not found", impactOk);

		// Check HGVS.c
		Assert.assertTrue("HGVS.c do not match" //
				+ "\n\tExpected : " + expectedHgvsc //
				+ "\n\tFound    : " + hgvscs//
				, hgvscs.containsAll(expectedHgvsc) //
		);

		// Check HGVS.p
		Assert.assertTrue("HGVS.p do not match" //
				+ "\n\tExpected : " + expectedHgvsp //
				+ "\n\tFound    : " + hgvsps//
				, hgvsps.containsAll(expectedHgvsp) //
		);

	}

	@Override
	protected void init() {
		randSeed = 20151205;
		genomeName = "testCase";
		addUtrs = false;
		onlyPlusStrand = true;
		onlyMinusStrand = false;
		numGenes = 2;
		maxGeneLen = 1000;
		maxTranscripts = 1;
		maxExons = 5;
		minExons = 2;
		shiftHgvs = false;

		initRand();
	}

	//	/**
	//	 * Inversion: Whole gene / whole transcript
	//	 */
	//	@Test
	//	public void test01() {
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 950, 1200, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		EffectType expEffs[] = { EffectType.TRANSCRIPT_INVERSION };
	//		String expHgvsc[] = { "c.-7_*43inv" };
	//		EffectImpact expectedImpact = EffectImpact.LOW;
	//
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//	}
	//
	//	/**
	//	 * Inversion: One coding exon
	//	 */
	//	@Test
	//	public void test02() {
	//		Variant variant = new Variant(chromosome, 1040, 1100, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		verbose = true;
	//		EffectType expEffs[] = { EffectType.EXON_INVERSION };
	//		String expHgvsc[] = { "c.33-5_45+43inv" };
	//		EffectImpact expectedImpact = EffectImpact.HIGH;
	//
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//	}
	//
	//	/**
	//	 * Inversion: Two coding exons
	//	 */
	//	@Test
	//	public void test03() {
	//		Variant variant = new Variant(chromosome, 1040, 1160, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		EffectType expEffs[] = { EffectType.EXON_INVERSION };
	//		String expHgvsc[] = { "c.33-5_*3inv" };
	//		EffectImpact expectedImpact = EffectImpact.HIGH;
	//
	//		verbose = true;
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//	}
	//
	//	/**
	//	 * Inversion: Part of one coding exon
	//	 */
	//	@Test
	//	public void test04() {
	//		Variant variant = new Variant(chromosome, 1040, 1050, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		EffectType expEffs[] = { EffectType.EXON_INVERSION_PARTIAL };
	//		String expHgvsc[] = { "c.33-5_38inv" };
	//		EffectImpact expectedImpact = EffectImpact.HIGH;
	//
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//	}

	//	/**
	//	 * Inversion: Part of two coding exon
	//	 */
	//	@Test
	//	public void test05() {
	//		Variant variant = new Variant(chromosome, 1050, 1150, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		EffectType expEffs[] = { EffectType.EXON_INVERSION_PARTIAL };
	//		String expHgvsc[] = { "c.38_48inv" };
	//		EffectImpact expectedImpact = EffectImpact.HIGH;
	//
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//	}

	/**
	 * Inversion: Two genes
	 */
	@Test
	public void test06() {
		Variant variant = new Variant(chromosome, 1050, 2150, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.EXON_INVERSION_PARTIAL };
		String expHgvsc[] = { "c.38_48inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	}

	//	/**
	//		 * Inversion: Part of two genes (fusions) cutting on introns
	//		 */
	//	@Test
	//	public void test07() {
	//		Variant variant = new Variant(chromosome, 1100, 2075, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//		 * Inversion: Part of two genes (fusions) cutting exons
	//		 */
	//	@Test
	//	public void test08() {
	//		Variant variant = new Variant(chromosome, 1050, 2120, "");
	//		variant.setVariantType(VariantType.INV);
	//	}

	//	/**
	//	 * Inversion: Part of two genes (fusions) cutting exons
	//	 */
	//	@Test
	//	public void test09() {
	//		Variant variant = new Variant(chromosome, 991, 1020, "");
	//		variant.setVariantType(VariantType.INV);
	//
	//		EffectType expEffs[] = { EffectType.INTRON };
	//		String expHgvsc[] = { "c.32+3_33-25inv" };
	//		EffectImpact expectedImpact = EffectImpact.MODIFIER;
	//
	//		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact);
	//
	//	}

}
