package org.snpeff.stats;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

import java.io.Serializable;
import java.util.Arrays;

import org.snpeff.stats.plot.GoogleHistogram;
import org.snpeff.stats.plot.GooglePlotInt;

/**
 * A simple class that does some basic statistics on integer numbers
 * 
 * @author pcingola
 */
public class IntStats implements Serializable {

	int maxBins = Integer.MAX_VALUE;
	TIntIntHashMap counters; // Counts instances of every number that appears in the distribution (i.e. histogram)
	boolean needUpdate;
	StatsProcedure statsProcedure;

	public static String toStringTabTitle() {
		return "Count\tSum\tMax\tMin\tMean\tMedian\tStdDev";
	}

	public IntStats() {
		counters = new TIntIntHashMap();
		needUpdate = true;
		statsProcedure = new StatsProcedure(0);
	}

	public int getCount() {
		if (needUpdate) update();
		return statsProcedure.count;
	}

	public int getCount(int sample) {
		return counters.get(sample);
	}

	public int getMax() {
		if (needUpdate) update();
		return statsProcedure.maxKey;
	}

	public int getMaxBins() {
		return maxBins;
	}

	public int getMaxCount() {
		if (needUpdate) update();
		return statsProcedure.maxValue;
	}

	public double getMean() {
		if (needUpdate) update();
		if (statsProcedure.count <= 0) return 0;
		return statsProcedure.sum / ((double) statsProcedure.count);
	}

	/**
	 * Calculate the median
	 * @return
	 */
	public double getMedian() {
		return getQuantile(0.5);
	}

	public int getMin() {
		if (needUpdate) update();
		return statsProcedure.minKey;
	}

	public int getMinCount() {
		if (needUpdate) update();
		return statsProcedure.minValue;
	}

	/**
	 * Get value at a given quantile
	 * @param quantile
	 * @return
	 */
	public double getQuantile(double quantile) {
		if (needUpdate) update();

		// Sort keys
		int keys[] = counters.keys();
		Arrays.sort(keys);

		// Iterate over all sorted keys
		int count = 0, median = 0, prevKey = 0, threshold = (int) (quantile * statsProcedure.count);
		boolean isOdd = (statsProcedure.count % 2 == 1);
		for (int i = 0; i < keys.length; i++) {
			int key = keys[i];
			int countKey = counters.get(key);
			count += countKey;
			median = key;

			if (count == threshold) {
				if (isOdd) return key;
				else {
					int nextKey = keys[i + 1];
					return (key + nextKey) / 2.0;
				}
			}

			if (count > threshold) {
				if (countKey > 1) return key;
				return (key + prevKey) / 2.0;
			}
			prevKey = key;
		}
		return median;
	}

	/**
	 * Calculate the standard deviation
	 * @return
	 */
	public double getStd() {
		if (needUpdate) update();
		return Math.sqrt(statsProcedure.variance());
	}

	public long getSum() {
		if (needUpdate) update();
		return statsProcedure.sum;
	}

	/**
	 * Do we have any data
	 * @return
	 */
	public boolean isValidData() {
		if (needUpdate) update();
		return statsProcedure.minKey <= statsProcedure.maxKey; // If this doesn't hold, it means that there was no data at all
	}

	/**
	 * Use 'num' as a sample
	 * @param num
	 */
	public void sample(int num) {
		// Update counters
		int count = counters.get(num) + 1;
		counters.put(num, count);

		needUpdate = true;
	}

	public void setMaxBins(int maxBins) {
		this.maxBins = maxBins;
	}

	@Override
	public String toString() {
		return "\tCount  : " + getCount() //
				+ "\n\tSum    : " + getSum() //
				+ "\n\tMax    : " + getMax() //
				+ "\n\tMin    : " + getMin() //
				+ "\n\tMean   : " + getMean() //
				+ "\n\tMedian : " + getMedian() //
				+ "\n\tStdDev : " + getStd() //
		;
	}

	/**
	 * Show a histogram as a list of numbers
	 * @return
	 */
	public String toStringCounts() {
		StringBuilder countsb = new StringBuilder();

		// Sort keys
		int keys[] = counters.keys();
		Arrays.sort(keys);

		// Iterate over all sorted keys
		for (int i = 0; i < keys.length; i++) {
			int key = keys[i];
			int count = counters.get(key);
			countsb.append((i > 0 ? "," : "") + count);
		}

		return countsb.toString();
	}

	/**
	 * Show a histogram as a list of numbers
	 * @return
	 */
	public String toStringHisto() {
		return "\t\tValues:\t" + toStringValues() + "\n\t\tCounts:\t" + toStringCounts() + "\n";
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	public String toStringPlot(String title, String xAxisLabel, boolean barChart) {
		// Create data arrays
		int values[] = counters.keys();
		Arrays.sort(values);
		int counts[] = new int[values.length];
		for (int i = 0; i < values.length; i++)
			counts[i] = counters.get(values[i]);

		// Create histogram
		GooglePlotInt ggplot;
		if (barChart) {
			GoogleHistogram gbar = new GoogleHistogram(values, counts, title, xAxisLabel, "Count");
			gbar.setMaxBins(maxBins);
			ggplot = gbar;
		} else ggplot = new GooglePlotInt(values, counts, title, xAxisLabel, "Count");

		return ggplot.toURLString();
	}

	public String toStringTab() {
		return getCount() + "\t" + getSum() + "\t" + getMax() + "\t" + getMin() + "\t" + getMean() + "\t" + getMedian() + "\t" + getStd();
	}

	/**
	 * Histogram values
	 * @return
	 */
	public String toStringValues() {
		StringBuilder keysb = new StringBuilder();

		// Sort keys
		int keys[] = counters.keys();
		Arrays.sort(keys);

		// Iterate over all sorted keys
		for (int i = 0; i < keys.length; i++) {
			int key = keys[i];
			keysb.append((i > 0 ? "," : "") + key);
		}

		return keysb.toString();
	}

	void update() {
		// Requires two iterations to calculate variance (single iteration naive algorithm is numerically unstable)
		statsProcedure = new StatsProcedure(0);
		counters.forEachEntry(statsProcedure);

		// Second iteration, we use the mean value from the first
		statsProcedure = new StatsProcedure(statsProcedure.mean());
		counters.forEachEntry(statsProcedure);
		needUpdate = false;
	}
}

/**
 * A class for iterating the Map and performing basic statistics
 */
class StatsProcedure implements TIntIntProcedure, Serializable {

	int minKey = Integer.MAX_VALUE;
	int maxKey = Integer.MIN_VALUE;
	int minValue = Integer.MAX_VALUE;
	int maxValue = Integer.MIN_VALUE;
	long sum = 0;
	double variance = 0;
	double mean = 0, s2sum = 0;
	int count = 0;

	StatsProcedure(double mean) {
		this.mean = mean;
	}

	@Override
	public boolean execute(int key, int value) {
		// Keep in mind that 'value' is the number of times that 'key' appears

		// Key stats
		if (key < minKey) minKey = key;
		if (key > maxKey) maxKey = key;

		// Value stats
		if (value < minValue) minValue = value;
		if (value > maxValue) maxValue = value;

		sum += key * value;
		count += value;

		// WARNING: Requires two iterations for variance.
		double s2 = (key - mean) * (key - mean) * value;
		s2sum += s2;

		return true;
	}

	double mean() {
		if (count <= 0) mean = 0;
		else mean = sum / ((double) count);
		return mean;
	}

	double variance() {
		if (maxKey == minKey) return 0;
		if (count <= 0) return 0;
		return s2sum / (count - 1);
	}
}
