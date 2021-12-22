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
	public void test_14() {
		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
		Log.debug("Test");

		// TODO: Create veff (non-syn, etc)
		// TODO: Create nextprot (high conserved)
		// TODO: Variant effect
		// TODO: Check result
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_15() {
		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
		Log.debug("Test");

		// TODO: Create veff (stop_gained, low, etc)
		// TODO: Create nextprot (high conserved)
		// TODO: Variant effect
		// TODO: Check result
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_16() {
		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
		Log.debug("Test");

		// TODO: Create veff (syn, low, etc)
		// TODO: Create nextprot (high conserved)
		// TODO: Variant effect
		// TODO: Check result
		throw new RuntimeException("Unimplemented test");
	}

	@Test
	public void test_17() {
		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
		Log.debug("Test");

		// TODO: Create veff (InDel, etc)
		// TODO: Create nextprot (high conserved)
		// TODO: Variant effect
		// TODO: Check result
		throw new RuntimeException("Unimplemented test");
	}

}
