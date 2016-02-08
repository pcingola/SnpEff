package org.snpeff.binseq.comparator;

import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.coder.DnaCoder;

/**
 * Compares two subsequences of DNA (DnaSequence)
 * 
 * @author pcingola
 */
public class DnaSubsequenceComparator<T extends DnaSequence> extends SubsequenceComparator<T> {

	boolean differentLengthsAreEqual;
	int maxDifferentBases = 0; // Maximum number of differences on a sequence (score is zero)

	public DnaSubsequenceComparator(boolean differentLengthsAreEqual) {
		this.differentLengthsAreEqual = differentLengthsAreEqual;
	}

	public DnaSubsequenceComparator(boolean differentLengthsAreEqual, int maxDifferentBases) {
		this.differentLengthsAreEqual = differentLengthsAreEqual;
		this.maxDifferentBases = maxDifferentBases;
	}

	@Override
	public int compare(DnaSequence seq1, int index1, DnaSequence seq2, int index2) {
		if( index1 >= seq1.length() ) return -1;
		if( index2 >= seq2.length() ) return 1;

		int i1 = index1, i2 = index2;
		for( ; (i1 < seq1.length()) && (i2 < seq2.length()); i1++, i2++ ) {
			int base1 = seq1.getCode(i1);
			int base2 = seq2.getCode(i2);
			int comp = base1 - base2;
			if( comp != 0 ) return comp;
		}

		if( differentLengthsAreEqual ) return 0;

		// Compare remaining length
		int comp = (seq1.length() - i1) - (seq2.length() - i2);
		return comp;
	}

	@Override
	public int score(DnaSequence seq1, int index1, DnaSequence seq2, int index2) {
		// Can we use a 'fast' algorithm?
		if( index1 == 0 ) return DnaCoder.get().score(seq1.getCodes(), seq2.getCodes(), index2, Math.min(seq1.length(), seq2.length() - index2), maxDifferentBases);
		if( index2 == 0 ) return DnaCoder.get().score(seq2.getCodes(), seq1.getCodes(), index1, Math.min(seq2.length(), seq1.length() - index1), maxDifferentBases);
		// OK, use 'slow' algorithm
		return scoreSlow(seq1, index1, seq2, index2);
	}

	/**
	 * This method is used when none of the indexes is zero. Also used for debugging and comparison
	 * 
	 * @param seq1
	 * @param index1
	 * @param seq2
	 * @param index2
	 * @return
	 */
	public int scoreSlow(DnaSequence seq1, int index1, DnaSequence seq2, int index2) {
		if( index1 >= seq1.length() ) return 0;
		if( index2 >= seq2.length() ) return 0;

		int score = 0, i1 = index1, i2 = index2, countDiff = 0;
		for( ; (i1 < seq1.length()) && (i2 < seq2.length()); i1++, i2++ ) {
			int base1 = seq1.getCode(i1);
			int base2 = seq2.getCode(i2);
			int comp = base1 - base2;
			if( comp != 0 ) {
				countDiff++;
				if( countDiff > maxDifferentBases ) return 0;
			}
			score++;
		}

		return score;
	}

}
