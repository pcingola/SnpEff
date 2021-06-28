package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

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
		Log.debug("Test");
		verbose = true;
		debug = true;
		Assert.assertEquals("", VcfEntry.cleanUnderscores(""));
		Assert.assertEquals("", VcfEntry.cleanUnderscores("_"));
		Assert.assertEquals("", VcfEntry.cleanUnderscores("__"));
		Assert.assertEquals("a", VcfEntry.cleanUnderscores("_a"));
		Assert.assertEquals("a", VcfEntry.cleanUnderscores("a_"));
		Assert.assertEquals("a", VcfEntry.cleanUnderscores("_a_"));
		Assert.assertEquals("a_z", VcfEntry.cleanUnderscores("a__z"));
		Assert.assertEquals("a_b_c_d", VcfEntry.cleanUnderscores("a_b_c_d_"));
		Assert.assertEquals("a1_b2_c3_d4", VcfEntry.cleanUnderscores("_a1__b2__c3__d4__"));
		Assert.assertEquals("a1_b2c3_d4", VcfEntry.cleanUnderscores("_____a1_b2c3_______d4________"));
	}
}
