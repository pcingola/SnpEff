package org.snpeff.binseq.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.snpeff.binseq.BinarySequence;
import org.snpeff.binseq.comparator.SequenceReference;
import org.snpeff.binseq.comparator.SubsequenceComparator;

/**
 * A collection of sequences that are indexed using some algorithm
 * 
 * Note: The ID is just the position in the array. That is why the array 
 * should not change the order of the elements (only append new ones or 
 * replace old ones).
 * 
 * @author pcingola
 */
public class SequenceIndexer<T extends BinarySequence> implements Comparator<Long>, Iterable<T> {

	ArrayList<T> sequences;
	SubsequenceComparator<T> subsequenceComparator;

	public SequenceIndexer(SubsequenceComparator<T> subsequenceComparator) {
		sequences = new ArrayList<T>();
		this.subsequenceComparator = subsequenceComparator;
	}

	/**
	 * Add a collection of sequences
	 * @param sequences
	 */
	public void add(Collection<T> sequences) {
		this.sequences.addAll(sequences);
	}

	/**
	 * Add a sequence to this index
	 * @param sequence
	 * @return Index to this sequence (a number that can be used to retrieve this sequence)
	 */
	public int add(T sequence) {
		sequences.add(sequence);
		return sequences.size() - 1;
	}

	/**
	 * Compare two references
	 * @param reference1
	 * @param reference2
	 * @return
	 */
	@Override
	public int compare(Long reference1, Long reference2) {
		int seqIdx1 = SequenceReference.getSeqIdx(reference1);
		T seq1 = sequences.get(seqIdx1);
		int start1 = SequenceReference.getStart(reference1);

		int seqIdx2 = SequenceReference.getSeqIdx(reference2);
		T seq2 = sequences.get(seqIdx2);
		int start2 = SequenceReference.getStart(reference2);

		int comp = subsequenceComparator.compare(seq1, start1, seq2, start2);
		return comp;
	}

	/**
	 * Get sequence in entry number 'idx'
	 * @param idx
	 * @return
	 */
	public T get(int idx) {
		return sequences.get(idx);
	}

	@Override
	public Iterator<T> iterator() {
		return sequences.iterator();
	}

	/**
	 * Number of sequences in this index
	 * @return
	 */
	public int size() {
		return sequences.size();
	}

}
