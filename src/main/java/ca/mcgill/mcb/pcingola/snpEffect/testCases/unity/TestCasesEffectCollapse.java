package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Splice sites variants
 *
 * @author pcingola
 */
public class TestCasesEffectCollapse extends TestCasesBase {

	public TestCasesEffectCollapse() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20141205;
		minExons = 2;
		spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
		spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
		spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		verbose = true;
		if (verbose) Gpr.debug("Transcript:" + transcript);

		// Create a variant that hits splice_region and creates a non_syn
		Variant variant = new Variant(chromosome, 811, "A", "T");

		// Calculate variant
		VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

		// Check that there is only one effect
		if (verbose) {
			System.err.println("Variant: " + variant);
			for (VariantEffect veff : veffs)
				System.err.println("\tEff: " + veff.effect(false, true, true, false));
		}
		Assert.assertEquals(1, veffs.size());
	}
}
