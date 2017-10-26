package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	public TestCasesIntegrationZzz() {
		super();
	}

	/**
	 * Cancer mutation is reversion to the REF base
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");
		verbose = true;
		String file = "tests/integration/cancer/test.cancer_05.vcf";
		snpEffectCancer(file, null, "testHg19Chr17", false, "p.Arg72Pro", "c.215G>C", "G-C", "NM_000546.5");
	}

}
