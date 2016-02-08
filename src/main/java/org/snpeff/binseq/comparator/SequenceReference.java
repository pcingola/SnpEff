package org.snpeff.binseq.comparator;

/**
 * A reference to a sequence. It's composed by a sequence index and a start position (both are integres stored in a long)
 * 
 * @author pcingola
 */
public class SequenceReference {

	public static long getReference(int sequenceIdx, int start) {
		return (((long) sequenceIdx) << 32) | start;
	}

	public static int getSeqIdx(long ref) {
		return (int) (ref >>> 32);
	}

	public static int getStart(long ref) {
		return (int) (ref & 0xffffffff);
	}
}
