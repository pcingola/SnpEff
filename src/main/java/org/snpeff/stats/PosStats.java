package org.snpeff.stats;

import java.util.Random;

import org.snpeff.interval.Marker;

/**
 * How many changes per position do we have in a chromosome.
 * Summary by dividing the chromosome into MAX_BINS bins
 * 
 * @author pcingola
 */
public class PosStats extends ChrPosStats {

	public static final int DEFAULT_BINS = 101;

	int maxIndex = 0;

	public PosStats() {
		super("", DEFAULT_BINS);
		name = "";
		maxBins = DEFAULT_BINS;
		init(maxBins);
		factor = 1;
	}

	public PosStats(String name) {
		super(name, DEFAULT_BINS);
		this.name = name;
		maxBins = DEFAULT_BINS;
		init(maxBins);
		factor = 1;
	}

	public PosStats(String name, int maxBins) {
		super(name, maxBins);
		this.name = name;
		this.maxBins = maxBins;
		init(maxBins);
	}

	/**
	 * Create random counts (used for debugging)
	 * @param maxLen
	 * @param countMax
	 */
	public void rand(int maxLen, int countMax) {
		Random rand = new Random();
		double lambda = 0.95;
		int prev = 0;
		for (int i = 0; i < Math.min(maxLen, count.length); i++) {
			double factor1 = i / ((double) maxLen);
			double factor2 = (maxLen - i) / ((double) maxLen);
			double factor = factor1 * factor2 / 0.25;
			count[i] = (int) (lambda * prev + (1 - lambda) * factor * rand.nextInt(countMax));
			prev = count[i];
		}
		maxIndex = maxLen;
	}

	/**
	 * Use 'num' as a sample
	 */
	public void sample(Marker marker, Marker markerReference) {
		if (!markerReference.intersects(marker)) return;

		int j = 0;
		int start = Math.max(marker.getStart(), markerReference.getStart());
		int end = Math.min(marker.getEndClosed(), markerReference.getEndClosed());

		double step = ((double) markerReference.size()) / count.length;
		if (step <= 0) step = 1; // This should never happen!

		// Increment all 'bins' covered by this 'read'
		if (markerReference.isStrandPlus()) {
			double pos = start;
			int jmin = (int) Math.round((start - markerReference.getStart()) / step);

			for (j = jmin; (pos <= end) && (j < count.length); pos += step, j++)
				count[j]++;
		} else {
			double pos = end;
			int jmin = (int) Math.round((markerReference.getEndClosed() - end) / step);

			for (j = jmin; (start <= pos) && (j < count.length); pos -= step, j++)
				count[j]++;
		}

		// Update maxIndex
		maxIndex = Math.max(maxIndex, j - 1);
	}

	@Override
	public int size() {
		return maxIndex + 1;
	}

}
