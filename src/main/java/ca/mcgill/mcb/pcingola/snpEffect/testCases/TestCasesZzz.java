package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	//	boolean debug = false;
	//	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		shiftHgvs = true;
	}

	@Test
	public void test_hgvs_walk_and_roll_3() {
		Gpr.debug("Test");
		String genome = "testHg19Chr13";
		String vcf = "tests/z.vcf";
		compareHgvs(genome, vcf, true);
	}

}
