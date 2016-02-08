package org.snpeff.coverage;

import java.io.Serializable;

/**
 * Base by base coverage (one chromsome)
 * 
 * @author pcingola
 */
public class CoverageChr implements Serializable {

	private static final long serialVersionUID = -5620938926858131251L;
	short count[];

	public CoverageChr(int len) {
		count = new short[len];
	}

	/**
	 * Calculate Coverage per base
	 * @return Average coverage per base
	 */
	public double avgCoverage(int start, int end) {
		return ((double) coverage(start, end)) / ((double) (end - start + 1));
	}

	/**
	 * Calculate Coverage per base
	 * @return Average coverage per base
	 */
	public long coverage(int start, int end) {
		// Calculate the average coverage
		long sum = 0;
		for( int i = start; i <= end; i++ )
			sum += count[i];

		return sum;
	}

	public short[] getCount() {
		return count;
	}

	/**
	 * Increment a region
	 * @param start
	 * @param end
	 */
	public void inc(int start, int end) {
		for( int i = start; i <= end; i++ )
			if( count[i] < Short.MAX_VALUE ) count[i]++;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < count.length; i++ )
			if( count[i] != 0 ) sb.append(i + "\t" + count[i] + "\n");
		return sb.toString();
	}
}
