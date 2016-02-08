package org.snpeff.overlap;

import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.coder.DnaCoder;
import org.snpeff.util.Gpr;

public class OverlapDnaSeq extends Overlap<DnaSequence> {

	public static boolean debug = false;
	private static BasesChangeCounter basesChangeCounter = new BasesChangeCounter(DnaCoder.get());
	private static int basesPerWord = DnaCoder.get().basesPerWord();
	private static int bitsPerBase = DnaCoder.get().bitsPerBase();

	SequenceRotator rotator;
	int minOverlap = 0; // THe minimum number of bases that should overlap between the sequences

	public int getMinOverlap() {
		return minOverlap;
	}

	@Override
	public int overlap(DnaSequence seq1, DnaSequence seq2) {
		// Initialize
		bestScore = Integer.MAX_VALUE;
		bestOffset = 0;
		rotator = new SequenceRotator(seq1);

		// Rotating right
		int lenRight = seq2.length() - minOverlap + 1;
		int lenLeft = seq1.length() - minOverlap + 1;
		int len = Math.max(lenRight, lenLeft);

		// Nothing to overlap?
		if (len <= 0) {
			bestScore = Math.min(seq1.length(), seq2.length());
			bestOffset = 0;
		}

		// Calculate best possible overlap
		for (int off = 0; off < len; off++) {
			int score = Integer.MAX_VALUE;

			// Compare to right rotation
			if (off < lenRight) score = overlapScore(seq1, seq2, off);
			if (score < bestScore) {
				bestScore = score;
				bestOffset = off;
				if (score == 0) return score; // Can't get lower than zero
			}

			// Compare to left rotation
			if (off < lenLeft) score = overlapScore(seq1, seq2, -off);
			if (score < bestScore) {
				bestScore = score;
				bestOffset = -off;
				if (score == 0) return score; // Can't get lower than zero
			}
		}

		return bestScore;
	}

	int overlapScore(DnaSequence seq1, DnaSequence seq2, int offsetSeq1) {
		// Calculate offset
		int offset = Math.abs(offsetSeq1);
		int offsetLongs = offset / basesPerWord;
		int offsetBases = offset % basesPerWord;

		// Adjust if offset is negative
		if ((offsetSeq1 < 0) && (offsetBases != 0)) {
			offsetLongs += 1;
			offsetBases = basesPerWord - offsetBases;
		}

		// Rotate sequence 1
		DnaSequence rotated = rotator.rotate(offsetBases);

		// Get sequences as 'longs'
		long longs1[] = rotated.getCodes();
		long longs2[] = seq2.getCodes();

		// Sequence comparison lengths
		int score = 0, len1 = 0, len2 = 0, i1 = 0, i2 = 0;
		if (offsetSeq1 >= 0) {
			// Rotated sequence has an offset (respect to seq2)
			len1 = longs1.length;
			len2 = longs2.length;
			i2 = offsetLongs;
		} else {
			// Seq2 has an offset (respect to rotated sequence)
			len1 = longs1.length;
			len2 = longs2.length;
			i1 = offsetLongs;
		}

		if (debug) System.out.println("Offset:" + offsetSeq1 + "\toffsetLongs: " + offsetLongs + "\toffsetBases:" + offsetBases + "\nseq1 : " + rotated + "\nseq2 : " + seq2 + "\nlen1:" + len1 + "\tlen2:" + len2);

		// Calculate scores
		for (; (i1 < len1) && (i2 < len2); i1++, i2++) {
			// Get binary values (long)
			long b1 = (i1 >= 0 ? longs1[i1] : 0);
			long b2 = (i2 >= 0 ? longs2[i2] : 0);
			long mask = 0xffffffffffffffffL;

			// First must have a 'mask'
			if ((i1 == 0) && (offsetBases != 0)) mask &= ~(0x8000000000000000L >> (bitsPerBase * offsetBases - 1));

			// Last word (seq1) must have a mask
			if (i1 >= len1 - 1) {
				int lastBits = bitsPerBase * ((seq1.length() + offsetSeq1) % basesPerWord);
				if (lastBits != 0) mask &= 0x8000000000000000L >> (lastBits - 1);
			}

			// Last word (seq2) must have a mask
			if (i2 >= len2 - 1) {
				int lastBits = bitsPerBase * (seq2.length() % basesPerWord);
				if (lastBits != 0) mask &= 0x8000000000000000L >> (lastBits - 1);
			}

			// Any mask? => Apply it
			if (mask != 0xffffffffffffffffL) {
				if (debug) System.out.println("\tmask: " + Gpr.bin64(mask));
				b1 &= mask;
				b2 &= mask;
			}

			// Compare
			long xor = b1 ^ b2;
			int changed = basesChangeCounter.changed(xor);
			score += changed;
			if (debug) System.out.println("\tb1  : " + Gpr.bin64(b1) + "\n\tb2  : " + Gpr.bin64(b2) + "\n\tXOR : " + Gpr.bin64(xor) + "\n\ti: " + i1 + "\tChanged: " + changed + "\tScore: " + score);
		}

		return score;
	}

	public void setMinOverlap(int minOverlap) {
		if (minOverlap < 0) throw new RuntimeException("Minimum overlap must be a non-negative number: " + minOverlap);
		this.minOverlap = minOverlap;
	}
}
