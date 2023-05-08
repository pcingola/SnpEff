package org.snpeff.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.BinarySequence;
import org.snpeff.binseq.DnaAndQualitySequence;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.coder.DnaCoder;
import org.snpeff.binseq.comparator.DnaQualSubsequenceComparator;
import org.snpeff.binseq.comparator.DnaSubsequenceComparator;
import org.snpeff.fastq.FastqVariant;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCasesDnaOverlap {

	public static boolean verbose = false;

	/**
	 * Create random changes in a sequence
	 */
	String change(String sequence, int numChanges, Random rand) {
		HashSet<Integer> changedPos = new HashSet<>();
		char[] chars = sequence.toCharArray();

		for (int i = 0; i < numChanges;) {
			int pos = rand.nextInt(chars.length);

			if (!changedPos.contains(pos)) { // Already changed?
				int newCode = rand.nextInt() & 0x03;
				char newBase = DnaCoder.get().toBase(newCode);

				if (chars[pos] != newBase) { // Base is different?
					chars[pos] = newBase;
					changedPos.add(pos);
					i++;
				}
			}
		}

		return new String(chars);
	}

	/**
	 * Perform a test of DnaCode.copyBases() method
	 */
	void dnaCoderCopyBases(int seqLenSrc, int seqLenDst, Random rand) {
		String srcStr = randSeq(seqLenSrc, rand);
		String dstStr = randSeq(seqLenDst, rand);

		// Create sequence
		DnaSequence src = new DnaSequence(srcStr);
		DnaSequence dst = null;
		dst = new DnaSequence(dstStr);

		long signature = src.getCodes()[0];

		// Random start & length
		int srcStart = rand.nextInt(src.length());
		int dstStart = rand.nextInt(dst.length());
		int len = rand.nextInt((Math.min(src.length() - srcStart, dst.length() - dstStart))) + 1;

		// Sub strings to compare
		String subStr = srcStr.substring(srcStart, srcStart + len);
		String dstBefore = dst.getSequence().substring(0, dstStart);
		String dstAfter = dst.getSequence().substring(dstStart + len);

		// Copy test
		DnaCoder.get().copyBases(src.getCodes(), srcStart, dst.getCodes(), dstStart, len);
		String subStrDst = dst.getSequence().substring(dstStart, dstStart + len);
		String dstBefore2 = dst.getSequence().substring(0, dstStart);
		String dstAfter2 = dst.getSequence().substring(dstStart + len);

		if (!subStr.equals(subStrDst)) { throw new RuntimeException("Substrings do not match! signature=" + Long.toHexString(signature) + " \n\t" + subStr + "\n\t" + subStrDst); }
		if (!dstBefore2.equals(dstBefore)) { throw new RuntimeException("Substrings 'before' do not match! signature=" + Long.toHexString(signature) + "\n\tdstBefore:\t" + dstBefore + "\n\tdstBefore2:\t" + dstBefore2); }
		if (!dstAfter2.equals(dstAfter)) { throw new RuntimeException("Substrings 'after' do not match! signature=" + Long.toHexString(signature) + "\n\tdstAfter:\t" + dstAfter + "\n\tdstAfter2:\t" + dstAfter2); }
	}

	/**
	 * Overlap two sequences
	 */
	void overlap(String seq1, String seq2, int start, String result, String resultQ) {
		overlapDnaSequence(seq1, seq2, start, result);
		overlapDnaAndQualitySequence(seq1, seq2, start, result, resultQ);
	}

	/**
	 * Test an overlap between two DnaAndQualitySequences
	 */
	void overlapDnaAndQualitySequence(String seq1, String seq2, int start, String result, String resultQ) {
		// Overlapping with DnaAndQualitySequence
		DnaAndQualitySequence s1 = new DnaAndQualitySequence(seq1, q(seq1.length(), 2), FastqVariant.FASTQ_SANGER);
		DnaAndQualitySequence s2 = new DnaAndQualitySequence(seq2, q(seq2.length(), 3), FastqVariant.FASTQ_SANGER);
		DnaAndQualitySequence s3 = s1.overlap(s2, start);

		assertEquals(result, s3.getSequence());
		if (resultQ != null) assertEquals(resultQ, s3.getQuality());
	}

	/**
	 * Test an overlap between two DnaSequences
	 */
	void overlapDnaSequence(String seq1, String seq2, int start, String result) {
		// Overlapping with DnaSequence
		DnaSequence s1 = new DnaSequence(seq1);
		DnaSequence s2 = new DnaSequence(seq2);
		BinarySequence s3 = s1.overlap(s2, start);
		assertEquals(result, s3.getSequence());
	}

	/**
	 * Create random sequences and overlap them
	 */
	void overlapRandTest(int maxLen, int minLen, Random rand) {
		// First sequence
		int len1 = rand.nextInt(maxLen) + minLen;
		String seq1 = randSeq(len1, rand);

		// Second sequence
		int over = rand.nextInt(len1 - minLen + 1);
		int start = over;
		String overlap = "", nonOverlap = "", seq2 = "", result = "";
		int len2 = rand.nextInt(maxLen - over);
		if (rand.nextBoolean()) {
			// Second sequence (overlapping at the end of the first one)
			overlap = seq1.substring(over);
			nonOverlap = randSeq(len2, rand);
			seq2 = overlap + nonOverlap;
			result = seq1 + nonOverlap; // Expected result
		} else {
			// Second sequence (overlapping at the beginning of the first one)
			overlap = seq1.substring(0, over);
			nonOverlap = randSeq(len2, rand);
			seq2 = nonOverlap + overlap;
			start = -nonOverlap.length();
			result = nonOverlap + seq1;
			// Expected result
		}

		overlap(seq1, seq2, start, result, null);
	}

	/**
	 * Create a quality string 'len' bases long
	 */

	String q(int len, int quality) {
		char q[] = new char[len];
		for (int i = 0; i < len; i++)
			q[i] = (char) ('!' + quality);
		return new String(q);
	}

	/**
	 * Create a random sequence of length 'len'
	 */
	String randSeq(int len, Random rand) {
		StringBuilder sb = new StringBuilder();
		// Create a random sequence
		for (int i = 0; i < len; i++) {
			int r = rand.nextInt() & 0x03;
			sb.append(DnaCoder.get().toBase(r));
		}
		return sb.toString();
	}

	void score(String seq1, String seq2, int start, int threshold, int result) {
		scoreDnaSequence(seq1, seq2, start, threshold, result);
		scoreDnaAndQualitySequence(seq1, seq2, start, threshold, result);
	}

	void scoreDnaAndQualitySequence(String seq1, String seq2, int start, int threshold, int result) {
		// Overlapping with DnaAndQualitySequence
		DnaAndQualitySequence s1 = new DnaAndQualitySequence(seq1);
		DnaAndQualitySequence s2 = new DnaAndQualitySequence(seq2);
		DnaQualSubsequenceComparator compartor = new DnaQualSubsequenceComparator(true, threshold);

		int idx1 = (start >= 0 ? start : 0);
		int idx2 = (start >= 0 ? 0 : -start);
		int score = compartor.score(s1, idx1, s2, idx2);

		assertEquals(result, score);
	}

	void scoreDnaSequence(String seq1, String seq2, int start, int threshold, int result) {
		// Overlapping with DnaSequence
		DnaSequence s1 = new DnaSequence(seq1);
		DnaSequence s2 = new DnaSequence(seq2);
		DnaSubsequenceComparator<DnaSequence> compartor = new DnaSubsequenceComparator<DnaSequence>(true, threshold);

		int idx1 = (start >= 0 ? start : 0);
		int idx2 = (start >= 0 ? 0 : -start);
		int score = compartor.score(s1, idx1, s2, idx2);

		assertEquals(result, score);
	}

	/**
	 * Test DnaCoder.score() method
	 */
	void scoreRandTest(int maxLen, int minLen, Random rand, DnaCoder dnaCoder, DnaSubsequenceComparator<DnaSequence> comparator) {
		// First sequence
		int len1 = rand.nextInt(maxLen) + minLen;
		String seq1 = randSeq(len1, rand);

		// Second sequence
		int over = rand.nextInt(len1 - minLen + 1);
		int start = over;
		String overlap = "", nonOverlap = "", seq2 = "";
		int len2 = rand.nextInt(maxLen - over);
		if (rand.nextBoolean()) {
			// Second sequence (overlapping at the end of the first one)
			overlap = seq1.substring(over);
			nonOverlap = randSeq(len2, rand);
			seq2 = overlap + nonOverlap;
		} else {
			// Second sequence (overlapping at the beginning of the first one)
			overlap = seq1.substring(0, over);
			nonOverlap = randSeq(len2, rand);
			seq2 = nonOverlap + overlap;
			start = -nonOverlap.length();
		}

		// Score should be zero for any 'start' after overlap
		DnaSequence s1 = new DnaSequence(seq1);
		DnaSequence s2 = new DnaSequence(seq2);
		int threshold = 0;
		boolean found = !(over > 0); // false, except if overlap is empty
		for (int i = 0; (i < 10) || (!found); i++) { // Iterate until we find a non-zero score (but not less than 10 iterations)
			int score1 = 0, score2 = 0, len = 0, starti;
			if (start > 0) {
				starti = rand.nextInt(s1.length());
				len = Math.min(seq2.length(), seq1.length() - starti);
				score1 = dnaCoder.score(s2.getCodes(), s1.getCodes(), starti, len, threshold);
				score2 = comparator.scoreSlow(s2, 0, s1, starti);
			} else {
				starti = rand.nextInt(s2.length());
				len = Math.min(seq1.length(), seq2.length() - starti);
				score1 = dnaCoder.score(s1.getCodes(), s2.getCodes(), starti, len, threshold);
				score2 = comparator.scoreSlow(s1, 0, s2, starti);
			}

			if (score1 != score2) throw new RuntimeException("Scores do not match!\n\tscore1: " + score1 + "\n\tscore2: " + score2 + "\n\tstarti: " + starti + "\n\tstart: " + start + "\n\tlen: " + len);
			if (score1 > 0) found = true;
		}
	}

	/**
	 * Create random sequences and calculate score (using a threshold)
	 */
	void scoreRandTestThreshold(int maxLen, int minLen, Random rand, int threshold, int overlapChanges) {
		// First sequence
		int len1 = rand.nextInt(maxLen) + minLen;
		String seq1 = randSeq(len1, rand);
		if (verbose) Log.info("\nseq1:\t" + seq1);

		// Second sequence
		int over = rand.nextInt(len1 - minLen + 1);
		int start = over;
		String overlap = "", nonOverlap = "", seq2 = "";
		int expectedScore = 0;
		int len2 = rand.nextInt(maxLen - over);
		if (rand.nextBoolean()) {
			// Second sequence (overlapping at the end of the first one)
			overlap = seq1.substring(over);
			overlapChanges = Math.min(overlapChanges, overlap.length());
			overlap = change(overlap, overlapChanges, rand);
			if (verbose) Log.info("over:\t" + overlap);
			nonOverlap = randSeq(len2, rand);
			seq2 = overlap + nonOverlap;
		} else {
			// Second sequence (overlapping at the beginning of the first one)
			overlap = seq1.substring(0, over);
			overlapChanges = Math.min(overlapChanges, overlap.length());
			overlap = change(overlap, overlapChanges, rand);
			if (verbose) Log.info("over:\t" + overlap);
			nonOverlap = randSeq(len2, rand);
			seq2 = nonOverlap + overlap;
			start = -nonOverlap.length();
		}
		if (verbose) Log.info("seq2:\t" + seq2);

		// Expected result
		expectedScore = overlapChanges <= threshold ? overlap.length() - overlapChanges : 0;
		if (verbose) Log.info("start:\t" + start + "\tthreshold: " + threshold + "\toverlapChanges: " + overlapChanges + "\tscore: " + expectedScore);

		// Caclualte
		score(seq1, seq2, start, threshold, expectedScore);
	}

	@Test
	public void test_07_overlap() {
		Log.debug("Test");
		overlap(//
				"catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg"// First sequence
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" // Second sequence
				, -47 // Start
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg" // Expected result
				, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&#################################################################");
	}

	@Test
	public void test_08_overlap() {
		Log.debug("Test");
		overlap(//
				"tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" // First sequence 
				, "catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg"// Second sequence
				, 47 // Start
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg" // Expected result
				, "###############################################&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" //
		);
	}

	@Test
	public void test_09_overlap() {
		Log.debug("Test");
		overlap(//
				"tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" // First sequence 
				, "catagaaaccaacagccatataactggtagctttaagcggctc"// Second sequence
				, 47 // Start
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" // Expected result
				, "###############################################&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" //
		);
	}

	@Test
	public void test_10_overlap() {
		Log.debug("Test");
		overlap(//
				"catagaaaccaacagccatataactggtagctttaagcggctc"// 
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" //
				, -47 // Start
				, "tttagcagcaaggtccatatctgactttttgttaacgtatttagccacatagaaaccaacagccatataactggtagctttaagcggctc" // Expected result
				, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" //
		);
	}

	@Test
	public void test_11_overlap() {
		Log.debug("Test");
		overlap(//
				"catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg"// 
				, "catagaaaccaacagccatataactggtagctttaagcggctc" //
				, 0 // Start
				, "catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg" // Expected result
				, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&#################################################################" //
		);
	}

	@Test
	public void test_12_overlap() {
		Log.debug("Test");
		overlap(//
				"catagaaaccaacagccatataactggtagctttaagcggctc" //  
				, "catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg"//
				, 0 // Start
				, "catagaaaccaacagccatataactggtagctttaagcggctcacctttagcatcaacaggccacaaccaaccagaacgtgaaaaagcgtcctgcgtgtagcgaactg" // Expected result
				, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" //
		);
	}

	@Test
	public void test_13_overlap() {
		Log.debug("Test");
		overlap(//
				"caggagcaggaaagcgagggtatcctacaaagtccagcgtaccataaacgcaagcctcaacgcagcgacgagcacgagagcggtcagtagcaatccaaac" //  
				, "aaagtccagcgtaccataaacgcaagcctcaacgcagcgacgagcacgagagcggtcagtagcaatccaa"//
				, 28 // Start
				, "caggagcaggaaagcgagggtatcctacaaagtccagcgtaccataaacgcaagcctcaacgcagcgacgagcacgagagcggtcagtagcaatccaaac" // Expected result
				, "############################&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&##" //
		);
	}

	@Test
	public void test_15_DnaCoder_copy_1() {
		Log.debug("Test");
		DnaCoder dnaCoder = DnaCoder.get();

		// Rand
		long seed = 20100809;
		Random rand = new Random(seed);

		int numTests = 10000;
		int maxLen = 1000;
		for (int i = 1; i < numTests; i++) {
			int seqlen = rand.nextInt(maxLen) + 10;
			String srcStr = randSeq(seqlen, rand);
			String dstStr = randSeq(seqlen, rand);

			// Create sequences
			DnaSequence src = new DnaSequence(srcStr);
			DnaSequence dst = new DnaSequence(dstStr);

			// Random start & length
			int srcStart = rand.nextInt(src.length());
			int dstStart = srcStart;
			int len = rand.nextInt((Math.min(src.length() - srcStart, dst.length() - dstStart)));

			// Sub strings to compare
			String subStr = srcStr.substring(srcStart, srcStart + len);
			String dstBefore = dst.getSequence().substring(0, dstStart);
			String dstAfter = dst.getSequence().substring(dstStart + len);

			if (verbose) {
				System.out.println("\n-----------------------------------------------------------------------------------------------");
				System.out.println("src:\t" + src);
				System.out.println("dst:\t" + dst);
				System.out.println("sub:\t" + subStr);
				System.out.println("bef:\t" + dstBefore);
				System.out.println("aft:\t" + dstAfter);
			}

			// Copy test
			dnaCoder.copyBases(src.getCodes(), dst.getCodes(), srcStart, len);

			String subStrDst = dst.getSequence().substring(dstStart, dstStart + len);
			String dstBefore2 = dst.getSequence().substring(0, dstStart);
			String dstAfter2 = dst.getSequence().substring(dstStart + len);

			if (verbose) {
				System.out.println("\ndst:\t" + dst);
				System.out.println("sub:\t" + subStrDst);
			}

			if (!subStr.equals(subStrDst)) { throw new RuntimeException("Substrings do not match!"); }
			if (!dstBefore2.equals(dstBefore)) { throw new RuntimeException("Substrings 'before' do not match!\n\tdstBefore:\t" + dstBefore + "\n\tdstBefore2:\t" + dstBefore2); }
			if (!dstAfter2.equals(dstAfter)) { throw new RuntimeException("Substrings 'after' do not match!"); }
			Gpr.showMarkStderr(i, 1000);
		}
	}

	@Test
	public void test_16_DnaCoder_copy_2() {
		Log.debug("Test");
		long seed = 20100812;
		Random rand = new Random(seed);

		int numTests = 100000;
		int maxLen, minLen = 10;

		System.err.print("\nDnaCoder.copyBases test:");
		for (int numWords = 1; numWords < 10; numWords++) {
			System.err.print("\n\tMax words: " + numWords + "\t");
			for (int i = 1; i < numTests; i++) {
				maxLen = 32 * numWords;
				int seqlensrc = Math.max(rand.nextInt(maxLen), minLen);
				int seqlendst = Math.max(rand.nextInt(maxLen), minLen);

				dnaCoderCopyBases(seqlensrc, seqlendst, rand);
				Gpr.showMarkStderr(i, 10000);
			}
		}
		System.err.print("\nDone.\n");
	}

	@Test
	public void test_17_overlap_rand() {
		Log.debug("Test");
		int numTests = 10;
		int minLen = 10;
		System.err.print("\nOverlap random test:\n");
		Random rand = new Random(20100812);
		for (int maxlen = 10, i = 1; maxlen < 10000; maxlen += 10, i++) {
			for (int it = 0; it < numTests; it++)
				overlapRandTest(maxlen, minLen, rand);
			Gpr.showMarkStderr(i, 1);
		}
		System.err.print("\nDone.\n");
	}

	@Test
	public void test_18_DnaCoder_score_rand() {
		Log.debug("Test");
		int numTests = 10;
		int minLen = 10;

		System.err.println("DnaCoder.score test:");
		DnaCoder dnaCoder = DnaCoder.get();
		DnaSubsequenceComparator<DnaSequence> comparator = new DnaSubsequenceComparator<DnaSequence>(true);

		Random rand = new Random(20100812);
		for (int maxlen = 10, i = 1; maxlen < 10000; maxlen += 10, i++) {
			for (int it = 0; it < numTests; it++)
				scoreRandTest(maxlen, minLen, rand, dnaCoder, comparator);
			Gpr.showMarkStderr(i, 1);
		}
		System.err.println("Done.");
	}

	@Test
	public void test_19_score_threshold_rand() {
		Log.debug("Test");
		int thresholdMax = 5;
		int changesMax = 6;
		int minLen = thresholdMax + changesMax + 10; // Big enough so that we don't run into problems creating random changes

		System.err.print("\nScore (threshold) random test:\n");
		Random rand = new Random(20100821);
		for (int maxLen = 10, i = 1; maxLen < 10000; maxLen += 10, i++) {
			for (int threshold = 0; threshold < thresholdMax; threshold++) {
				for (int changes = 0; changes < changesMax; changes++) {
					scoreRandTestThreshold(maxLen, minLen, rand, threshold, changes);
				}
			}
			Gpr.showMarkStderr(i, 1);
		}
		System.err.print("\nDone.\n");
	}
}
