package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	/**
	 * Some difficult HGVS test case
	 */
	@Test
	public void test_zzz() {
		Gpr.debug("Test");

		String genome = "testHg19Chr1";
		String vcf = "tests/hgvs_zzz.vcf";

		compareHgvs(genome, vcf);
	}

}
