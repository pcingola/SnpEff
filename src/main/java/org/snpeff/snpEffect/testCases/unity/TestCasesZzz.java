package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/covid19/";
	}

	/**
	 * TODO: Merge into 'TestCasesIntegrationCovid19'
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		verbose = true;
		debug = true;
		String genome = "test_NC_045512_01";
		SnpEffectPredictor sep = build(genome);

	}
}
