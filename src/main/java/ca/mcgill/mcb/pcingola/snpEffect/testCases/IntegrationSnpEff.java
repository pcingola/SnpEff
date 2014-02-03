package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

/**
 * Invoke all integration test cases 
 * 
 * @author pcingola
 */
public class IntegrationSnpEff extends IntegrationTest {

	public void test_01() {
		String expectedOutputFile = "tests/test.chr1.1line.out.vcf";
		String args[] = { "eff", "-v", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	public void test_01_multi_thread() {
		String expectedOutputFile = "tests/test.chr1.1line.out.multi-thread.vcf";
		String args[] = { "eff", "-v", "-t", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	public void test_02() {
		String expectedOutputFile = "tests/test.chr1.1line.out.txt";
		String args[] = { "eff", "-v", "-o", "txt", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	public void test_03() {
		String expectedOutputFile = "tests/test.chr1.out.vcf.gz";
		String args[] = { "eff", "-v", "-noStats", "testHg3763Chr1", "tests/test.chr1.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
