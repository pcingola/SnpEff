package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz {

	boolean verbose = false;
	long randSeed = 20100629;
	String genomeName = "testCase";

	public TestCasesIntegrationZzz() {
		super();
	}

	/**
	 * Rare Amino acid
	 */
	@Test
	public void test_30_RareAa() {
		Gpr.debug("Test");
		String genomeName = "testHg3765Chr22";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/rareAa.txt", null, true);
	}

}
