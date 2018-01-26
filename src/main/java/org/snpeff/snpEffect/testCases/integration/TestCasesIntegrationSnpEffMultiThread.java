package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.util.Gpr;

/**
 * Invoke multi thread integration test
 * 
 * WARNING: JUnit doesn't seem to work if you use multi-threading....
 * 
 * @author pcingola
 */
public class TestCasesIntegrationSnpEffMultiThread extends IntegrationTest {

	@Test
	public void test_01_multi_thread() {
		Gpr.debug("Test");
		// FIXME: Mutithreading mode is broken: Move to Java streams + lambdas
		if (Math.random() < 2.0) throw new RuntimeException("Mutithreading mode is broken: Move to Java streams + lambdas");

		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
		String args[] = { "eff", "-t", "-classic", "-noHgvs", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		SnpEff snpeff = new SnpEff(args);
		snpeff.setVerbose(verbose);
		command(snpeff, expectedOutputFile);
	}

}
