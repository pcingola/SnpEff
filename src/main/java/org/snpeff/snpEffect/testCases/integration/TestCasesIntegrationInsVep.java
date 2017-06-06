package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationInsVep extends TestCasesIntegrationBase {

	@Test
	public void test_03_InsVep() {
		Gpr.debug("Test");
		compareVep("testENST00000268124", "tests/integration/insVep/testENST00000268124_ins_vep.vcf", "ENST00000268124");
	}

	@Test
	public void test_04_InsVep() {
		Gpr.debug("Test");
		compareVep("testHg3770Chr22", "tests/integration/insVep/testENST00000445220_ins_vep.vcf", "ENST00000445220");
	}

}
