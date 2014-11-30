package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

/**
 * Invoke multi thread integration test
 * 
 * WARNING: JUnit doesn't seem to work if you use multi-threading....
 * 
 * @author pcingola
 */
public class TestCasesIntegrationSnpEffMultiThread extends IntegrationTest {

	@Test
	public void test() {
		String expectedOutputFile = "tests/test.chr1.out.multi-thread.vcf.gz";
		String args[] = { "eff", "-v", "-t", "-noStats", "testHg3763Chr1", "tests/test.chr1.vcf" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
