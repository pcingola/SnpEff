package org.snpeff.binseq.comparator;

import org.snpeff.binseq.DnaAndQualitySequence;

/**
 * Compares two subsequences of DNA (DnaAndQualitySequence)
 * 
 * @author pcingola
 */
public class DnaQualSubsequenceComparator extends SubsequenceComparator<DnaAndQualitySequence> {

	boolean differentLengthsAreEqual;
	int maxDifferentBases = 0; // Maximum number of differences on a sequence (score is zero)

	public DnaQualSubsequenceComparator(boolean differentLengthsAreEqual) {
		this.differentLengthsAreEqual = differentLengthsAreEqual;
	}

	public DnaQualSubsequenceComparator(boolean differentLengthsAreEqual, int maxDifferentBases) {
		this.differentLengthsAreEqual = differentLengthsAreEqual;
		this.maxDifferentBases = maxDifferentBases;
	}

	@Override
	public int compare(DnaAndQualitySequence seq1, int index1, DnaAndQualitySequence seq2, int index2) {
		if( index1 >= seq1.length() ) return -1;
		if( index2 >= seq2.length() ) return 1;

		byte s1[] = seq1.getCodes(), s2[] = seq2.getCodes();
		int i1 = index1, i2 = index2;
		for( ; (i1 < s1.length) && (i2 < s2.length); i1++, i2++ ) {
			int base1 = s1[i1] & 0x3;
			int base2 = s2[i2] & 0x3;
			int comp = base1 - base2;
			if( comp != 0 ) return comp;
		}

		if( differentLengthsAreEqual ) return 0;

		// Compare remaining length
		int comp = (s1.length - i1) - (s2.length - i2);
		return comp;
	}

	@Override
	public int score(DnaAndQualitySequence seq1, int index1, DnaAndQualitySequence seq2, int index2) {
		int score = 0;
		if( index1 >= seq1.length() ) return 0;
		if( index2 >= seq2.length() ) return 0;

		byte s1[] = seq1.getCodes(), s2[] = seq2.getCodes();
		int i1 = index1, i2 = index2, countDiff = 0;
		for( ; (i1 < s1.length) && (i2 < s2.length); i1++, i2++ ) {
			int base1 = s1[i1] & 0x3;
			int base2 = s2[i2] & 0x3;
			int comp = base1 - base2;

			if( comp != 0 ) {
				countDiff++;
				if( countDiff > maxDifferentBases ) return 0;
			}

			score++;
		}

		return score - countDiff;
	}

}
