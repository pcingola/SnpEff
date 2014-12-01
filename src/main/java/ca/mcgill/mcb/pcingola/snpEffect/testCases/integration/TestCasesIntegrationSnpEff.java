package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Invoke all integration test cases 
 * 
 * @author pcingola
 */
public class TestCasesIntegrationSnpEff extends IntegrationTest {

	//	@Test
	//	public void test_01() {
	//		Gpr.debug("Test");
	//		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
	//		String args[] = { "eff", "-classic", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
	//		command(new SnpEff(args), expectedOutputFile);
	//	}

	//	@Test
	//	public void test_01_multi_thread() {
	//		Gpr.debug("Test");
	//		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
	//		String args[] = { "eff", "-t", "-classic", "-noStats", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
	//		command(new SnpEff(args), expectedOutputFile);
	//	}

	@Test
	public void test_03() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.eff.vcf";
		String args[] = { "eff", "-v", "-classic", "-noStats", "testHg3763Chr1", "tests/test.chr1.vcf.gz" };
		command(new SnpEff(args), expectedOutputFile);
	}

	@Test
	public void test_03_multi_thread() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.eff.vcf";
		String args[] = { "eff", "-v", "-t", "-classic", "-noStats", "testHg3763Chr1", "tests/test.chr1.vcf.gz" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
