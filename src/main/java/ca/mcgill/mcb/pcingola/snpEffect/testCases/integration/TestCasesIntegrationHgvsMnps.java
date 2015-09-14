package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesIntegrationHgvsMnps extends TestCasesIntegrationBase {

	public TestCasesIntegrationHgvsMnps() {
		super();
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
