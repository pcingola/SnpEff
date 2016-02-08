package org.snpeff.binseq.indexer;

import org.snpeff.binseq.DnaSequenceId;

/**
 * Only allow sequences with different IDs to be overlapped
 */
public class OverlapFilterDnaId implements OverlapFilter<DnaSequenceId> {

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
