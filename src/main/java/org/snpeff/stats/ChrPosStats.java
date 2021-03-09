package org.snpeff.stats;

import org.snpeff.stats.plot.GoogleHistogram;
import org.snpeff.util.Log;

/**
 * How many changes per position do we have in a chromosome.
 * Summary by dividing the chromosome into MAX_BINS bins
 *
 * @author pcingola
 */
public class ChrPosStats {

	public static boolean debug = false;

	int maxBins = 300; // Max number of points to show in a plot (plots to sample)
	String name; // Chromosome name
	int length; // Chromosome length;
	int factor = 1;
	int count[];
	int total;

	public ChrPosStats(String chrName, int length) {
		name = chrName;
		this.length = length;
		total = 0;

		// Multiplier factor
		factor = 1;
		for (factor = 1; (length / factor) > maxBins; factor *= 10);

		int len = length / factor + 1; // May be the chromosome is smaller then 'MAX_POINTS' (e.g. when you have small contigs)
		init(len);
	}

	String factorStr() {
		if (factor > 1000000000) return factor / 1000000000 + "Gb";
		if (factor > 1000000) return factor / 1000000 + "Mb";
		if (factor > 1000) return factor / 1000 + "Kb";
		return factor + "b";
	}

	public int getCount(int idx) {
		return count[idx];
	}

	public int getTotal() {
		return total;
	}

	void init(int len) {
		// Initialize count
		count = new int[len];
		for (int i = 0; i < count.length; i++)
			count[i] = 0;
	}

	public int[] posArray() {
		int pos[] = new int[count.length];
		for (int i = 0; i < pos.length; i++)
			pos[i] = i * factor;
		return pos;
	}

	/**
	 * Use 'num' as a sample
	 * @param num
	 */
	public void sample(int position) {
		// Ignore counts for zero or one-length chromosomes
		if (length <= 1) { return; }

		int i = position / factor;
		if ((i >= 0) && (i < count.length)) {
			count[i]++;
			total++;
		} else if (debug) Log.debug("Error counting samples on chromosome '" + name + "'. Position '" + position + "' => count[" + i + "]  (count.length: " + count.length + ", factor: " + factor + ", chrLength: " + length + ").");
	}

	public int size() {
		return count.length;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name + ", Position,");

		// Position
		int pos[] = posArray();
		for (int i = 0; i < count.length; i++)
			sb.append(pos[i] + ",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		// Counts
		sb.append(name + ",Count,");
		for (int i = 0; i < count.length; i++)
			sb.append(count[i] + ",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		return sb.toString();
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	public String toStringHistoPlot(String title, String xAxisLabel, String yAxisLabel) {
		int pos[] = posArray(); // Create data arrays
		GoogleHistogram gghisto = new GoogleHistogram(pos, count, title, xAxisLabel, yAxisLabel + "/" + factorStr()); // Create histogram
		return gghisto.toURLString();
	}
}
