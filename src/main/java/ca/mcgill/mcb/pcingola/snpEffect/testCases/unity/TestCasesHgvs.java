package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsProtein;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;
import junit.framework.Assert;

/**
 *
 * Test case for basic HGV annotaions
 */
public class TestCasesHgvs extends TestCasesBase {

	public TestCasesHgvs() {
		super();
	}

	void checkHgvsProt(Variant variant, String expectedHgvsP) {
		prependSequenceToFirstExon("atgaaaatgggccatcagcagcagtgctgc"); // This is 'MKMGHQQQCC' as a DNA sequence

		if (verbose) {
			Gpr.debug("\nChromsome : " + chromoSequence //
					+ "\nTranscript:\n" + transcript //
					+ "\nVariant   : " + variant //
			);
		}

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();
		HgvsProtein hgvsp = new HgvsProtein(eff);
		String hgvsProt = hgvsp.toString();

		// Check result
		if (verbose) {
			Gpr.debug("Eff        : " + eff);
			Gpr.debug("HGVS (DNA) : '" + hgvsDna + "'");
			Gpr.debug("HGVS (Prot): '" + hgvsProt + "'");
		}

		Assert.assertEquals("HGVS.p notation does not match", expectedHgvsP, hgvsProt);
	}

	/**
	 * Test case: Use 1-letter AA change
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		Config.get().setHgvsOneLetterAA(true);
		Variant variant = new Variant(chromosome, 898, "C", "A", ""); // Add 'GHQ' amino acids
		checkHgvsProt(variant, "p.Q7K");
		Config.get().setHgvsOneLetterAA(false);
	}

	/**
	 * Test case: Use transcript ID
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		verbose = true;
		Config.get().setHgvsTrId(true);
		Variant variant = new Variant(chromosome, 898, "C", "A", ""); // Add 'GHQ' amino acids
		checkHgvsProt(variant, "transcript_0:p.Gln7Lys");
		Config.get().setHgvsTrId(false);
	}

}
