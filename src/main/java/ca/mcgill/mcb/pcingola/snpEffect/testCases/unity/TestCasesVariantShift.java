package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesVariantShift extends TestCasesBase {

	public TestCasesVariantShift() {
		super();
	}

	@Override
	protected void init() {
		super.init();

		minExons = 2;

		randSeed = 20141128;
		initRand();
	}

	/**
	 * Shift by one position
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Change exon's sequence
		verbose = true;
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 754, "", "T", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Shift variant
		if (verbose) Gpr.debug("Variant (before): " + variant);
		Variant variantShifted = variant.shiftLeft();
		if (verbose) Gpr.debug("Variant (after): " + variantShifted);

		// Check that shifted variant is oK
		Assert.assertFalse(variant == variantShifted);
		Assert.assertEquals(756, variantShifted.getStart());
		Assert.assertEquals("", variantShifted.getReference());
		Assert.assertEquals("T", variantShifted.getAlt());
	}

	/**
	 * No shift
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		// Change exon's sequence
		verbose = true;
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 754, "", "A", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Shift variant
		if (verbose) Gpr.debug("Variant (before): " + variant);
		Variant variantShifted = variant.shiftLeft();
		if (verbose) Gpr.debug("Variant (after): " + variantShifted);

		// Check that shifted variant is the same object
		Assert.assertTrue(variant == variantShifted);
	}

	/**
	 * Shift by one position
	 */
	@Test
	public void test_03() {
		Gpr.debug("Test");

		// Change exon's sequence
		verbose = true;
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 1025, "", "G", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Shift variant
		if (verbose) Gpr.debug("Variant (before): " + variant);
		Variant variantShifted = variant.shiftLeft();
		if (verbose) Gpr.debug("Variant (after): " + variantShifted);

		// Check that shifted variant is oK
		Assert.assertFalse(variant == variantShifted);
		Assert.assertEquals(1030, variantShifted.getStart());
		Assert.assertEquals("", variantShifted.getReference());
		Assert.assertEquals("G", variantShifted.getAlt());
	}

}
