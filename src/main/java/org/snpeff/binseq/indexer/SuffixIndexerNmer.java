package org.snpeff.binseq.indexer;

import java.util.Iterator;

import org.snpeff.binseq.BinarySequence;
import org.snpeff.binseq.comparator.SequenceReference;
import org.snpeff.binseq.comparator.SubsequenceComparator;
import org.snpeff.collections.HashLongLongArray;
import org.snpeff.nmer.Nmer;
import org.snpeff.util.Gpr;

/**
 * Index all suffixes of all the sequences (it indexes using Nmers).
 * 
 * Note: Under the current structure, only exact overlap matches are allowed
 * 
 * @author pcingola
 *
 * @param <T>
 */

public class SuffixIndexerNmer<T extends BinarySequence> extends SequenceIndexer<T> {

	int nmerSize;
	Nmer nmer;
	HashLongLongArray hash;
	OverlapFilter<T> overlapFilter = null;

	public SuffixIndexerNmer(SubsequenceComparator<T> subsequenceComparator, int nmerSize) {
		super(subsequenceComparator);

		// Create Nmer
		this.nmerSize = nmerSize;
		nmer = new Nmer(nmerSize);

		// Create and initialize hash
		hash = new HashLongLongArray();

		// Add a null sequence as the first sequence. This is done because 
		// TroveCollections represent 0 as 'EMPTY'. This means that we cannot 
		// index sequence number zero (the first sequence).
		sequences.add(null);
	}

	/**
	 * Add a sequence to this index
	 * @param sequence
	 * @return Index to this sequence (a number that can be used to retrieve this sequence)
	 */
	@Override
	public int add(T sequence) {
		sequences.add(sequence);
		int idx = sequences.size() - 1;
		indexAllSuffix(sequence, idx);
		return idx;
	}

	/**
	 * Find best overlap for 'sequence'
	 * @param sequence
	 * @return An overlap result
	 */
	@SuppressWarnings("unchecked")
	public OverlapRessult<T> findBestOverlap(T sequence) {
		// Find an overlapping sequence 
		OverlapRessult<T> result = new OverlapRessult<T>();
		findOverlap(sequence, result);

		if (result.bestScore < sequence.length()) { // We calculate the reverseWC score (unless we already have an optimal score)
			// Find an overlapping sequence for reverseWC
			T sequenceRwc = (T) sequence.reverseWc();
			OverlapRessult<T> resultRwc = new OverlapRessult<T>();
			resultRwc.reverseWC = true;
			findOverlap(sequenceRwc, resultRwc);

			// If reverseWc has a better match => use reverseWc 
			if ((result.bestSequence == null) || (result.bestScore < resultRwc.bestScore)) {
				result = resultRwc; // Swap results
				sequence = sequenceRwc; // Swap sequences
			}
		}

		return result;
	}

	/**
	 * Finds the best overlaps for a sequence
	 * @param sequence
	 * @return
	 */
	boolean findOverlap(T sequence, OverlapRessult<T> result) {
		int max = sequence.length() - nmerSize;

		// Initialize nmer
		nmer.setNmer(0); // Reset
		for (int i = 0; i < nmerSize; i++)
			nmer.rol(sequence.getBase(i));

		// Compare all suffixes of this sequence
		for (int i = 0; true; i++) {
			// Any references that match this Nmer?
			long bucket[] = hash.getBucket(nmer.getNmer());

			// If the sequence is shorter than the best score, there is no point comparing any more (the score will be lower)
			if (result.bestScore >= (sequence.length() - i)) break;

			// Found something: Compare all buckets
			if (bucket != null) {
				int len = hash.getBucketLength(nmer.getNmer());

				// Analyze all references in this bucket
				for (int r = 0; r < len; r++) {
					long ref = bucket[r];
					int seqIdx = SequenceReference.getSeqIdx(ref);
					int start = SequenceReference.getStart(ref);
					T seq = get(seqIdx);

					if ((overlapFilter == null) || overlapFilter.considerOverlap(sequence, seq)) {
						// We only want perfect sequence overlaps (either sequence has to have a zero index)
						if ((i != 0) && (start != 0)) {
							// Skip this sub-sequence comparison
						} else if ((i == 0) && (result.bestScore >= (seq.length() - start))) { // If the overlap is shorter than the bestScore, there is no point comparing them (the score will be lower)
							// Skip this sub-sequence comparison
						} else if ((start == 0) && (result.bestScore >= (sequence.length() - i))) { // If the overlap is shorter than the bestScore, there is no point comparing them (the score will be lower)
							// Skip this sub-sequence comparison
						} else {
							// Compare subsequences
							int score = subsequenceComparator.score(sequence, i, seq, start);
							if (score > result.bestScore) {
								result.bestScore = score;
								result.bestSequence = seq;
								result.bestReference = ref;
								result.bestId = seqIdx;
								result.start = start - i;
							}

							if (score == sequence.length()) return true;// Already found best possible score => Don't look any more
						}
					}
				}
			}

			if (i >= max) break; // Are we done?
			nmer.rol(sequence.getBase(i + nmerSize)); // Prepare for next iteration
		}

		return result.bestSequence != null;
	}

	public OverlapFilter<T> getOverlapFilter() {
		return overlapFilter;
	}

	/**
	 * Index all possible suffix of 'sequence'
	 * @param sequence
	 */
	void indexAllSuffix(T sequence, int seqIds) {
		int max = sequence.length() - nmerSize;

		// Initialize nmer
		nmer.setNmer(0); // Reset
		for (int i = 0; i < nmerSize; i++)
			nmer.rol(sequence.getBase(i));

		// Create all references to suffixes of this sequence
		for (int start = 0; true; start++) {
			long ref = SequenceReference.getReference(seqIds, start);
			hash.put(nmer.getNmer(), ref);
			if (start >= max) break;
			// Prepare for next iteration
			nmer.rol(sequence.getBase(start + nmerSize));
		}
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> it = sequences.iterator();
		it.next(); // The first sequence is 'null', skip it
		return it;
	}

	/**
	 * Find the best possible overlap and join the sequences or just add add the sequence to the index
	 * @param sequence
	 * @return true if an overlap was found and false if no overlap was found
	 */
	@SuppressWarnings("unchecked")
	public boolean overlap(T sequence) {
		// Find best overlapping sequence 
		OverlapRessult<T> result = findBestOverlap(sequence);
		if (result.bestSequence == null) return false; // Nothing found? => return

		// If sequence is fully included in "result.bestSequence", then the overlap is already done (nothing to do)
		if ((result.start >= 0) && ((result.start + sequence.length()) <= result.bestSequence.length())) return true;

		// Overlap sequence
		T overlapSeq = (T) result.bestSequence.overlap(sequence, result.start);

		// Replace old sequence with new sequence
		replaceSequenceOverlap(result.bestSequence, overlapSeq, result.bestId, result.start);
		return true;
	}

	public void printSequences() {
		for (T seq : this)
			System.out.println(seq.getSequence());
	}

	/**
	 * Update all suffixes from an overlapping sequence
	 * Create all references to suffixes for sequenceNew (replace suffixes from sequenceOri if possible)
	 */
	void replaceSequenceOverlap(T sequenceOri, T sequenceNew, int seqIdx, int start) {
		int max = Math.max(sequenceNew.length(), sequenceOri.length()) - nmerSize;

		if (start >= 0) {
			int startIdx = sequenceOri.length() - nmerSize;

			// Initialzie Nmer
			for (int i = 0, j = startIdx; i < nmerSize; i++, j++)
				nmer.rol(sequenceNew.getBase(j));

			// This is a 'new' part of sequenceNew => Create new index entries
			for (int idx = startIdx; idx < max; idx++) {
				long ref = SequenceReference.getReference(seqIdx, idx);
				hash.put(nmer.getNmer(), ref);

				nmer.rol(sequenceNew.getBase(idx + nmerSize)); // Update nmer
			}
		} else {
			// Initialzie Nmer
			for (int i = 0; i < nmerSize; i++)
				nmer.rol(sequenceNew.getBase(i));

			// Start < 0
			int idxOri = 0;
			for (int idx = 0; idx < max; idx++) {
				long ref = SequenceReference.getReference(seqIdx, idx);
				if ((0 <= idx) && (idx < (-start))) {
					hash.put(nmer.getNmer(), ref); // This is a 'new' part of sequenceNew => Create new index entries
				} else {
					// This is an 'old' part of sequenceNew => Update index entries (if they exist)
					long refOld = SequenceReference.getReference(seqIdx, idxOri);
					hash.replace(nmer.getNmer(), refOld, ref);
					idxOri++;
				}

				// Update nmer
				nmer.rol(sequenceNew.getBase(idx + nmerSize));
			}
		}

		// Replace sequence in 'sequences'
		sequences.set(seqIdx, sequenceNew);
	}

	/**
	 * Perform consistency checks
	 */
	public void sanityCheck() {
		//---
		// Check that all indexes are there (it's like re-indexing all the sequences)
		//---
		// System.out.println("Sanity check (Sequence -> Nmers): ");

		// For each sequence Id....
		int k = 1;
		for (int seqIdx = 1; seqIdx < sequences.size(); seqIdx++) {
			T sequence = sequences.get(seqIdx);

			int max = sequence.length() - nmerSize;

			// Initialize nmer
			nmer.setNmer(0); // Reset
			for (int i = 0; i < nmerSize; i++)
				nmer.rol(sequence.getBase(i));

			// For each Nmer
			for (int start = 0; start < max; start++) {
				long ref = SequenceReference.getReference(seqIdx, start);
				if (!hash.contains(nmer.getNmer(), ref)) throw new RuntimeException("ERROR: Cannot find reference:\n\tReference: " + ref + "\tsequence.id: " + seqIdx + "\tindex: " + start + "\n\tNmer: " + nmer + "(" + nmer.getNmer() + ")\n\tSequence: " + sequence.getSequence());

				nmer.rol(sequence.getBase(start + nmerSize));
				Gpr.showMarkStderr(k++, 10000);
			}
		}

		//---
		// Check that all reference have the right sequenceId:index
		//---
		//System.out.println("\nSanity check (Nmers -> Sequence): ");
		k = 1;
		for (long nmerLong : hash.keys()) {
			long[] bucket = hash.getBucket(nmerLong);
			if (bucket == null) {
				if (nmerLong != 0) throw new RuntimeException("ERROR: Nmer does not have any bucket!\n\tNmer: " + nmerLong);
			} else {
				int len = hash.getLatestBucketLength();

				// For each reference in this bucket...
				for (int i = 0; i < len; i++) {
					long ref = bucket[i];
					int seqIdx = SequenceReference.getSeqIdx(ref);
					int start = SequenceReference.getStart(ref);
					T sequence = get(seqIdx);

					// Compare nmer
					String nmerStr = sequence.getSequence().substring(start, start + nmer.length());
					nmer.setNmer(nmerLong);
					if (!nmer.toString().equalsIgnoreCase(nmerStr)) throw new RuntimeException("ERROR: Reference does not match Nmer:\n\tNnmer: " + nmerLong + "\tReference: " + ref + "\tsequence.id: " + seqIdx + "\tindex: " + start + "\n\tNmer: " + nmer + "\n\tSequence: " + sequence.getSequence());

					Gpr.showMarkStderr(k++, 10000);
				}
			}
		}
	}

	public void setOverlapFilter(OverlapFilter<T> overlapFilter) {
		this.overlapFilter = overlapFilter;
	}

	@Override
	public String toString() {
		long tot = 0;
		int max = 0;
		StringBuilder sb = new StringBuilder();
		for (T seq : this) {
			tot += seq.length();
			max = Math.max(max, seq.length());
			// sb.append(seq.getSequence() + "\n");
		}

		if (sequences.size() > 0) sb.append("Max sequence length: " + max + "\tAvg sequence length: " + (tot / sequences.size()));
		sb.append("\tHash stats: " + hash.toString());
		return sb.toString();
	}

	public String toStringSequences() {
		StringBuilder sb = new StringBuilder();
		for (T seq : this)
			sb.append(seq.getSequence() + "\n");
		return sb.toString();
	}
}
