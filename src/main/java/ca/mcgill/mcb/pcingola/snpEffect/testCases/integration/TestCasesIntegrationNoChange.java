package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case where VCF entries has no sequence change (either REF=ALT or ALT=".")
 *
 * @author pcingola
 */
public class TestCasesIntegrationNoChange extends TestCasesIntegrationBase {

	public TestCasesIntegrationNoChange() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3766Chr1", "./tests/test.no_change.vcf" };
		checkNoChange(args);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3766Chr1", "./tests/test.no_change_02.vcf" };
		checkNoChange(args);
	}

}
