package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.util.Gpr;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnpEff extends IntegrationTest {

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
		String args[] = { "eff", "-classic", "-noHgvs", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
