package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.overlap.OverlapDnaSeq;

public class TestCasesOverlap extends TestCase {

	/**
	 * Overlap two sequences and check results 
	 * @param seq1
	 * @param seq2
	 * @param score
	 * @param offset
	 */
	void checkOverlap(String seq1, String seq2, int score, int offset) {
		DnaSequence bs1 = new DnaSequence(seq1), bs2 = new DnaSequence(seq2);
		OverlapDnaSeq obs = new OverlapDnaSeq();
		obs.setMinOverlap(1); // At least 1 base overlap!
		obs.overlap(bs1, bs2);
		assertEquals(score, obs.getBestScore());
		assertEquals(offset, obs.getBestOffset());
	}

	public void test_binSeq_01() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_02() {
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_03() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	public void test_binSeq_04() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_05() {
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_06() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	public void test_binSeq_07() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "ttttacgt", 0, 4);
	}

	public void test_binSeq_08() {
		checkOverlap("ttttacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, -4);
	}

	public void test_binSeq_09() {
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgttttt", 0, -60);
	}

	public void test_binSeq_10() {
		checkOverlap("acgttttt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 60);
	}

	// Empty sequence
	public void test_binSeq_11() {
		checkOverlap("", "", 0, 0);
	}

	// No possible overlap (using minOverlap=1)
	public void test_binSeq_12() {
		checkOverlap("aaaaaaaaaaaaaaaa", "tttttttttttttttt", 1, 15);
	}

	public void test_binSeq_13() {
		checkOverlap("ttttttttttttttttttttttttttttttttaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccccccccccccccccccccccccccccccccggggggggggggggggggggggggggggggggtttttttttttttttttttttttttttttttt", 0, -32);
	}

}
