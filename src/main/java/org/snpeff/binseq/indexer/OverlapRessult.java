package org.snpeff.binseq.indexer;

import org.snpeff.binseq.BinarySequence;

/**
* An object used to store overlap parameters
*/
public class OverlapRessult<T extends BinarySequence> {

	public int start = -1;
	public int bestScore = 0;

	// Information about the best matching sequence
	public T bestSequence = null;
	public long bestReference = 0;
	public int bestId = 0;
	public boolean reverseWC = false;

	@Override
	public String toString() {
		return "bestScore:" + bestScore + ", start:" + start + ", bestSequence:" + (bestSequence != null ? bestSequence.toString() : "NULL");
	}
}
