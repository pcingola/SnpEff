package org.snpeff.fastq;

/**
 * Simple maipulation of fastq sequences
 * @author pcingola
 */
public class FastqTools {

	/**
	 * Create an array of integers describing quality
	 */
	public static int[] qualtityArray(Fastq fastq) {
		char qc[] = fastq.getQuality().toCharArray();
		int q[] = new int[qc.length];

		int min = (fastq.getVariant().isSanger() ? 33 : 64);
		for( int i = 0; i < qc.length; i++ )
			q[i] = qc[i] - min;

		return q;
	}

}
