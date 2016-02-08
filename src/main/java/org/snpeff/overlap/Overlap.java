package org.snpeff.overlap;

import org.snpeff.binseq.BinarySequence;

/**
 * Calculates the best overlap between two sequences
 * 
 * Note: An overlap is a simple 'alignment' which can only contain gaps at the 
 *       beginning or at the end of the sequences.
 *       
 * @author pcingola
 */
public abstract class Overlap<S extends BinarySequence> {

	int bestOffset, bestScore;

	public int getBestOffset() {
		return bestOffset;
	}

	public int getBestScore() {
		return bestScore;
	}

	/**
	 * Calculate the best overlap between two sequences
	 * 
	 * 
	 * @param seq1
	 * @param seq1
	 * @return
	 */
	public abstract int overlap(S seq1, S seq2);
}
