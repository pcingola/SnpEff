package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.snpEffect.commandLine.SnpEff;
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
		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
		String args[] = { "eff", "-t", "-classic", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
