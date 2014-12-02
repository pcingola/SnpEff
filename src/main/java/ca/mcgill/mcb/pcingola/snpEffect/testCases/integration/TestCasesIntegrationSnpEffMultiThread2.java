package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Invoke multi thread integration test
 * 
 * WARNING: JUnit doesn't seem to work if you use multi-threading....
 * 
 * @author pcingola
 */
public class TestCasesIntegrationSnpEffMultiThread2 extends IntegrationTest {

	@Test
	public void test_02_multi_thread() {
		Gpr.debug("Test");
		String expectedOutputFile = "tests/test.chr1.eff.vcf.gz";
		String args[] = { "eff", "-t", "-classic", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", "tests/test.chr1.vcf.gz" };
		command(new SnpEff(args), expectedOutputFile);
	}

}
