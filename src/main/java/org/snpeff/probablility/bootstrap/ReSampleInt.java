package org.snpeff.probablility.bootstrap;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.Random;

/**
 * Re-sample statistic
 * 
 * Statistic is a sum of a set of integer numbers (e.g. ranks). 
 * The statistic is sampled and PDF/CDF can be calculated from empirical random re-sampling.
 * 
 * @author pablocingolani
 */
public class ReSampleInt {

	static int SHOW_EVERY = 10000;
	static int SHOW_EVERY_LINE = 100 * 10000;

	boolean verbose = false;
	int sampleSize;
	int scores[]; // All possible scores
	TIntIntHashMap pdf; // Probability density function (in a hash)
	Random rand = new Random();
	int total = Integer.MIN_VALUE;

	public ReSampleInt(int scores[], int sampleSize) {
		this.sampleSize = sampleSize;
		this.scores = scores;
	}

	/**
	 * Cummulative distribution for this number
	 * @param number
	 * @return
	 */
	public double cdf(int number) {
		int sum = 0;

		for( int k : sortKeys() ) {
			sum += pdf.get(k);
			if( k >= number ) return ((double) sum) / ((double) total());
		}

		return 1.0;
	}

	/**
	 * Clear previous statistics
	 */
	protected void clearStats() {
		pdf = new TIntIntHashMap();
	}

	/**
	 * Random sample and evaluate
	 */
	protected int evaluate() {
		int sum = 0;
		for( int i = 0; i < sampleSize; i++ )
			sum += scores[rand.nextInt(scores.length)];
		return sum;
	}

	public int max() {
		int skeys[] = sortKeys();
		return skeys[skeys.length - 1];
	}

	public int min() {
		return sortKeys()[0];
	}

	/**
	 * Smallest number whose CDF is larger than a given quantile. I.e. x such that P[ X <= x ] >= Quantile
	 * 
	 * 
	 * @param quantile
	 * @return
	 */
	public int quantile(double quantile) {
		if( (quantile < 0) || (quantile > 1) ) throw new RuntimeException("Quantile out of range. Should be in [0, 1] range, value: " + quantile);
		int keys[] = sortKeys();

		int total = total();
		int sum = 0;
		for( int k : keys ) {
			sum += pdf.get(k);
			double p = ((double) sum) / ((double) total);
			if( p >= quantile ) return k;
		}

		return Integer.MAX_VALUE;
	}

	/**
	 * Re-sample 'iterations' times
	 * 
	 * @param iterations
	 */
	public void resample(int iterations) {
		clearStats();
		for( int i = 1; i <= iterations; i++ ) {
			int score = evaluate();
			int count = pdf.get(score) + 1;
			pdf.put(score, count);

			if( verbose ) {
				if( i % SHOW_EVERY == 0 ) {
					if( i % SHOW_EVERY_LINE == 0 ) System.err.println('.');
					else System.err.print('.');
				}
			}
		}
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	int[] sortKeys() {
		int keys[] = pdf.keys();
		Arrays.sort(keys);
		return keys;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Total: " + pdf.size() + " entries:\n");
		int sum = 0;
		for( int k : sortKeys() ) {
			sb.append("\t" + k + "\t:\t" + pdf.get(k) + "\n");
			sum += pdf.get(k);
		}

		sb.append("\tSum\t:\t" + sum + "\n");

		return sb.toString();
	}

	int total() {
		if( total > Integer.MIN_VALUE ) return total;

		total = 0;
		for( int k : sortKeys() )
			total += pdf.get(k);

		return total;
	}
}
