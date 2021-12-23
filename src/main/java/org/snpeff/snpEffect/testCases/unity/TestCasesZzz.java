package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Variant;
import org.snpeff.nextProt.NextProtMarkerFactory;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
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


//	@Test
//	public void test_16() {
//		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
//		Log.debug("Test");
//
//		// TODO: Create veff (syn, low, etc)
//		// TODO: Create nextprot (high conserved)
//		// TODO: Variant effect
//		// TODO: Check result
//		throw new RuntimeException("Unimplemented test");
//	}
//
//	@Test
//	public void test_17() {
//		// TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
//		Log.debug("Test");
//
//		// TODO: Create veff (InDel, etc)
//		// TODO: Create nextprot (high conserved)
//		// TODO: Variant effect
//		// TODO: Check result
//		throw new RuntimeException("Unimplemented test");
//	}

}
