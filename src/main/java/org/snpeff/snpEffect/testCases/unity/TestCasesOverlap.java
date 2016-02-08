package org.snpeff.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.overlap.OverlapDnaSeq;
import org.snpeff.util.Gpr;

public class TestCasesOverlap {

	/**
	 * Overlap two sequences and check results
	 */
	void checkOverlap(String seq1, String seq2, int score, int offset) {
		DnaSequence bs1 = new DnaSequence(seq1), bs2 = new DnaSequence(seq2);
		OverlapDnaSeq obs = new OverlapDnaSeq();
		obs.setMinOverlap(1); // At least 1 base overlap!
		obs.overlap(bs1, bs2);
		Assert.assertEquals(score, obs.getBestScore());
		Assert.assertEquals(offset, obs.getBestOffset());
	}

	@Test
	public void test_binSeq_01() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	@Test
	public void test_binSeq_02() {
		Gpr.debug("Test");
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	@Test
	public void test_binSeq_03() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	@Test
	public void test_binSeq_04() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	@Test
	public void test_binSeq_05() {
		Gpr.debug("Test");
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	@Test
	public void test_binSeq_06() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	@Test
	public void test_binSeq_07() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "ttttacgt", 0, 4);
	}

	@Test
	public void test_binSeq_08() {
		Gpr.debug("Test");
		checkOverlap("ttttacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, -4);
	}

	@Test
	public void test_binSeq_09() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgttttt", 0, -60);
	}

	@Test
	public void test_binSeq_10() {
		Gpr.debug("Test");
		checkOverlap("acgttttt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 60);
	}

	// Empty sequence
	@Test
	public void test_binSeq_11() {
		Gpr.debug("Test");
		checkOverlap("", "", 0, 0);
	}

	// No possible overlap (using minOverlap=1)
	@Test
	public void test_binSeq_12() {
		Gpr.debug("Test");
		checkOverlap("aaaaaaaaaaaaaaaa", "tttttttttttttttt", 1, 15);
	}

	@Test
	public void test_binSeq_13() {
		Gpr.debug("Test");
		checkOverlap("ttttttttttttttttttttttttttttttttaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccccccccccccccccccccccccccccccccggggggggggggggggggggggggggggggggtttttttttttttttttttttttttttttttt", 0, -32);
	}

}
