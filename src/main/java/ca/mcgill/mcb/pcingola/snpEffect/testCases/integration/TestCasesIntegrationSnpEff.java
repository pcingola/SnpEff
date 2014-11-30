package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

/**
 * Invoke all integration test cases 
 * 
 * @author pcingola
 */
public class TestCasesIntegrationSnpEff extends IntegrationTest {

	@Test
	public void test_01() {
		String expectedOutputFile = "tests/test.chr1.1line.out.vcf";
		String args[] = { "eff", "-v", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	@Test
	public void test_01_multi_thread() {
		String expectedOutputFile = "tests/test.chr1.1line.out.multi-thread.vcf";
		String args[] = { "eff", "-v", "-t", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	@Test
	public void test_03() {
		String expectedOutputFile = "tests/test.chr1.out.vcf.gz";
		String args[] = { "eff", "-v", "-noStats", "testHg3763Chr1", "tests/test.chr1.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
