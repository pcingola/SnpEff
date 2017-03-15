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

	@Test
	public void testCase_04_AthalianaTair10_AT5G66790() {
		Gpr.debug("Test");
		String genome = "testAthalianaTair10"; //"athalianaTair10";
		String gff3File = "tests/AT5G66790.gff3";
		String resultFile = "tests/AT5G66790.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}
}
