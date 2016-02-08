package org.snpeff.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;
import org.snpeff.util.Gpr;
import org.snpeff.util.IubString;

public class TestCasesIubString {

	protected boolean debug = false;
	protected boolean verbose = false || debug;

	protected String expand(String seq) {
		IubString iubString = new IubString(seq);

		StringBuilder sb = new StringBuilder();
		for (String str : iubString) {
			if (verbose) System.out.println(str);
			sb.append(str + " ");
		}

		return sb.toString().trim();

	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		String expected = "A C G T";
		String out = expand("N");

		Assert.assertEquals(expected, out);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");

		String expected = "ACGA ACGC ACGG ACGT";
		String out = expand("ACGN");

		Assert.assertEquals(expected, out);
	}

	@Test
	public void test_03() {
		Gpr.debug("Test");

		String expected = "ACGA " //
				+ "CCGA " //
				+ "ACGC " //
				+ "CCGC " //
				+ "ACGG " //
				+ "CCGG " //
				+ "ACGT " //
				+ "CCGT" //
		;

		String out = expand("MCGN");

		Assert.assertEquals(expected, out);
	}

	@Test
	public void test_04() {
		Gpr.debug("Test");

		String expected = "" //
				+ "ACGA " //
				+ "CCGA " //
				+ "AGGA " //
				+ "CGGA " //
				+ "ACGC " //
				+ "CCGC " //
				+ "AGGC " //
				+ "CGGC " //
				+ "ACGG " //
				+ "CCGG " //
				+ "AGGG " //
				+ "CGGG " //
				+ "ACGT " //
				+ "CCGT " //
				+ "AGGT " //
				+ "CGGT" //
		;

		String out = expand("MSGN");

		Assert.assertEquals(expected, out);
	}

}
