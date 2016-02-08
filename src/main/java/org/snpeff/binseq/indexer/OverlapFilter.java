package org.snpeff.binseq.indexer;

import org.snpeff.binseq.BinarySequence;

/**
 * Indicate whether an overlap between two sequences should be considered or not
 */

public interface OverlapFilter<T extends BinarySequence> {

	/**
	 * Consider overlap between two sequences?
	 * @param seq1
	 * @param seq2
	 * @return true if sequences should be considered for overlapping
	 */
	public boolean considerOverlap(T seq1, T seq2);
}
