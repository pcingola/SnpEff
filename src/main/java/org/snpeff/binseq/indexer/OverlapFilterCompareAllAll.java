package org.snpeff.binseq.indexer;

import org.snpeff.binseq.DnaSequenceId;

/**
 * Only allow overlaps between sequences mapped to same/different partition
 */

public class OverlapFilterCompareAllAll implements OverlapFilter<DnaSequenceId> {

	public OverlapFilterCompareAllAll() {}

	/**
	 * Consider overlap between two sequences?
	 * @param seq1
	 * @param seq2
	 * @return true if sequences should be considered for overlapping
	 */
	@Override
	public boolean considerOverlap(DnaSequenceId seq1, DnaSequenceId seq2) {
		return seq1.getId() != seq2.getId();
	}
}
