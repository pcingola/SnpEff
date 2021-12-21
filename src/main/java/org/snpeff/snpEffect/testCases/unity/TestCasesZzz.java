package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test playground
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/covid19/";
	}

	@Test
	public void test_01() {
		Log.debug("Test");
		verbose = true;
		debug = true;
	}
}
