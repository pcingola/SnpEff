package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesBase {

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

	@Override
	protected void init() {
		super.init();
		randSeed = 20100629;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		System.err.println("");
	}

}
