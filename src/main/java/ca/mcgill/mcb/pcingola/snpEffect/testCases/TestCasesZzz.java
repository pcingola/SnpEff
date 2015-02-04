package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
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

	@Test
	public void test_05_Annotation_EndOfChromosome() {
		Gpr.debug("Test");

		verbose = true;

		// Create a variant: Insertion after last chromosome base
		Variant variant = new Variant(genome.getChromosome("1"), 2001, "", "TTT", "");
		VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

		// Check output
		if (verbose) System.out.println("Number of effects: " + veffs.size());
		Assert.assertEquals(1, veffs.size());

		VariantEffect veff = veffs.get(0);
		if (verbose) System.out.println("Effect type : " + veff.getEffectType() + "\t" + veff.getEffectTypeString(true));
		Assert.assertEquals(EffectType.CHROMOSOME_ELONGATION, veff.getEffectType());
	}

}
