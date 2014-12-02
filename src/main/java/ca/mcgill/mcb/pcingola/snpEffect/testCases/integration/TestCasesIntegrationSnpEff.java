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

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.1line.out.classic.vcf";
		String args[] = { "eff", "-classic", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.1line.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.eff.vcf.gz";
		String args[] = { "eff", "-classic", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.vcf.gz" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
