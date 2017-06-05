package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
	}

	String effectStr(VariantEffect effect) {
		String effStr = effect.effect(true, true, true, false, false);
		String aaStr = effect.getAaChangeOld();
		int idx = effStr.indexOf('(');
		return effStr.substring(0, idx) + "(" + aaStr + ")";
	}

	/**
	 * Test Somatic vs Germline: Check HGVS notation "c."
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
		String file = "tests/integration/cancer/test_04.vcf";
		snpEffectCancer(file, null, "testHg19Chr22", false, "p.Gln133Leu", "c.398A>T", "A-T");
	}

}
