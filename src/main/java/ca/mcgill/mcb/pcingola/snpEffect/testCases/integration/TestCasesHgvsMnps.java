package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesHgvsMnps extends TestCasesBase {

	public TestCasesHgvsMnps() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		shiftHgvs = true;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_mnps.vcf";
		compareHgvs(genome, vcf, false);
	}

}
