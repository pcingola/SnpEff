package org.snpeff.binseq.comparator;

import org.snpeff.binseq.BinarySequence;

/**
 * Compare two subsequences (actually it compares two sequences from different starting points) 
 * 
 * @author pcingola
 *
 * @param <T>
 */
public abstract class SubsequenceComparator<T extends BinarySequence> {

	public abstract int compare(T seq1, int start1, T seq2, int start2);

	public abstract int score(T seq1, int start1, T seq2, int start2);
}
