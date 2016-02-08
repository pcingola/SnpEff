package org.snpeff.overlap;

import org.snpeff.binseq.coder.Coder;

/**
 * Counts how many bases changed, given an XOR between two longs
 * 
 * @author pcingola
 */
public class BasesChangeCounter {

	private static int CACHED_BASES = 8;
	private static int CACHED_BITS = 2 * CACHED_BASES;
	private static int MASK = (1 << CACHED_BITS) - 1;

	Coder coder;
	int changed[];

	public BasesChangeCounter(Coder coder) {
		this.coder = coder;

		changed = new int[1 << CACHED_BITS];

		for( int i = 0; i < changed.length; i++ ) {
			int count = 0;
			for( int ii = 0; ii < CACHED_BASES; ii++ ) {
				int j = (i >> (coder.bitsPerBase() * ii)) & ((int) coder.mask(0));
				if( j != 0 ) count++;
			}

			changed[i] = count;
		}
	}

	int changed(long xor) {
		if( xor == 0 ) return 0;

		int changedBases = 0;
		for( int i = 0; i < 4; i++ ) {
			long j = (xor >>> (i * CACHED_BITS)) & MASK;
			changedBases += changed[(int) j];
		}
		return changedBases;
	}

}
