package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
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

	@Test
	public void test01() {
		initSnpEffPredictor();

		for (Gene g : genome.getGenes())
			Gpr.debug("\tGene: " + g);

		Variant variant = new Variant(chromosome, 950, 1200, "");
		variant.setVariantType(VariantType.INV);

		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

		// Checknumber of results
		Assert.assertEquals(true, effects.size() >= 1);

		for (VariantEffect eff : effects) {
			Gpr.debug("\t" + eff);
		}

		//
		//		// Checknumber of results
		//		Assert.assertEquals(true, effects.size() == 1);
		//		if (debug) System.out.println(effects);
		//
		//		// Check effect
		//		VariantEffect effect = effects.get();
		//		String effStr = effectStr(effect);
		//		if (debug) System.out.println("\tPos: " + pos //
		//				+ "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
		//				+ "\t" + variant //
		//				+ "\tCodon: " + codon + " -> " + newCodon //
		//				+ "\tAA: " + aa + " -> " + newAa //
		//				+ "\tEffect: " + effStr);
		//
		//		// Check effect
		//		Assert.assertEquals(effectExpected, effStr);
		//

		//		VariantEffect eff = effs.get();
		//		HgvsDna hgvsc = new HgvsDna(eff);
		//		String hgvsDna = hgvsc.toString();
		//		HgvsProtein hgvsp = new HgvsProtein(eff);
		//		String hgvsProt = hgvsp.toString();

		//		if (expectedHgvsC != null) Assert.assertEquals("HGVS.c notation does not match", expectedHgvsC, hgvsDna);
		//		if (expectedHgvsP != null) Assert.assertEquals("HGVS.p notation does not match", expectedHgvsP, hgvsProt);

	}
}
