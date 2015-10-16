package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBaseApply;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 *
 */
public class TestCasesZzz extends TestCasesBaseApply {

	public TestCasesZzz() {
		super();
	}

	/**
	 * Variant before exon
	 */
	@Test
	public void test_apply_variant_01() {
		Gpr.debug("Test");
		Variant variant = new Variant(transcript.getParent(), 290, "T", "A");
		checkApplySnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
	}

}
