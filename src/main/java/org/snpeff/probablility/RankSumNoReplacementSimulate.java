package org.snpeff.probablility;

/**
 * 
 * Calculate rank sum probability distribution function (pdf) and cumulative distribution function (cdf).
 * Note: This class assumes that ranks cannot be repeated (selecting without replacement)
 * 
 * @author Pablo Cingolani
 *
 */

public class RankSumNoReplacementSimulate {

	//-------------------------------------------------------------------------
	// Variables
	//-------------------------------------------------------------------------

	int n;
	int numberOfSamples;
	long rankSum[][]; // Rank sum
	int samples[][]; // Randomlly selected ranks

	//-------------------------------------------------------------------------
	// Main
	//-------------------------------------------------------------------------
	public static void main(String[] args) {
		System.out.println("Begin: RankSumNoReplacementSimulate");
		int numberOfSamples = 100000;

		for( int n = 2; true; n++ ) {
			// Generate random rank sums
			RankSumNoReplacementSimulate rss = new RankSumNoReplacementSimulate(numberOfSamples, n);

			// Show mean / variance for all values
			for( int nt = 1; nt <= rss.n; nt++ ) {
				double mu = rss.mean(nt);
				double muSample = rss.sampleMean(nt);
				double muErr = (mu != 0 ? Math.abs(mu - muSample) / mu : 0);
				double sigmaSample = Math.sqrt(rss.sampleVariance(nt));
				double sigma = Math.sqrt(rss.variance(nt));
				double simgaErr = (sigma != 0 ? Math.abs(sigma - sigmaSample) / sigma : 0);
				System.out.println("N:" + n + "\tNT:" + nt + "\tmu:" + mu + "\tmuSample:" + muSample + "\terr%:" + muErr + "\t|\tsigma:" + sigma + "\tsigmaSample:" + sigmaSample + "\terr%:" + simgaErr);
			}
		}

		// System.out.println("End: RankSumNoReplacementSimulate");
	}

	//-------------------------------------------------------------------------
	// Methods
	//-------------------------------------------------------------------------

	public RankSumNoReplacementSimulate(int numberOfSamples, int n) {
		this.numberOfSamples = numberOfSamples;
		this.n = n;
		samples = new int[numberOfSamples][n];
		rankSum = new long[numberOfSamples][n];
		initSamples();
		randomizeSamples();
		rankSum();
	}

	public int getN() {
		return n;
	}

	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	public long[][] getRankSum() {
		return rankSum;
	}

	public int[][] getSamples() {
		return samples;
	}

	/**
	 * Initialize samples (ranks: 1 to N)
	 */
	public void initSamples() {
		for( int ns = 0; ns < numberOfSamples; ns++ )
			for( int i = 0; i < n; i++ ) {
				samples[ns][i] = i + 1;
				rankSum[ns][i] = 0;
			}
	}

	public double mean(int nt) {
		double mean = ((nt * (n + 1))) / 2.0;
		return mean;
	}

	/**
	 * Shufle samples (random ranks)
	 */
	public void randomizeSamples() {
		for( int ns = 0; ns < numberOfSamples; ns++ )
			for( int i = 0; i < n; i++ ) {
				// Select 2 samples (1 of them is random)
				int r1 = i;
				int r2 = (int) (Math.random() * n);

				// Swap these 2 samples
				int tmp = samples[ns][r1];
				samples[ns][r1] = samples[ns][r2];
				samples[ns][r2] = tmp;
			}
	}

	/**
	 * Calculate rankSums
	 */
	public void rankSum() {
		// For all samples, calculate sample mean
		for( int ns = 0; ns < numberOfSamples; ns++ ) {
			long rs = 0;
			for( int i = 0; i < n; i++ ) {
				rs += samples[ns][i];
				rankSum[ns][i] = rs;
			}
		}

	}

	/**
	 * Calculate the mean rankSum for a given 'N_T' (number of ranks to add)
	 * @param nt : number of ranks to add
	 * @return Mean rank sum value
	 */
	public double sampleMean(int nt) {
		if( (nt < 1) || (nt > n) ) return 0;

		double mean = 0;
		for( int ns = 0; ns < numberOfSamples; ns++ )
			mean += rankSum[ns][nt - 1];
		mean /= numberOfSamples;

		return mean;
	}

	/**
	 * Calculate the variance of rankSum for a given 'N_T' (number of ranks to add)
	 * @param nt : number of ranks to add
	 * @return Variance rank sum value
	 */
	public double sampleVariance(int nt) {
		if( (nt < 1) || (nt > n) ) return 0;

		double mean = mean(nt);
		double sum = 0;
		for( int ns = 0; ns < numberOfSamples; ns++ ) {
			double d = rankSum[ns][nt - 1] - mean;
			sum += d * d;
		}
		double var = sum / ((numberOfSamples - 1));

		return var;
	}

	public void setN(int n) {
		this.n = n;
	}

	public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}

	public void setRankSum(long[][] rankSum) {
		this.rankSum = rankSum;
	}

	public void setSamples(int[][] samples) {
		this.samples = samples;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("numberOfSamples:" + numberOfSamples + "\tN:" + n + "\nSamples:\n");

		// Show samples
		for( int ns = 0; ns < numberOfSamples; ns++ ) {
			sb.append(ns + ":\t");
			for( int i = 0; i < n; i++ )
				sb.append(samples[ns][i] + " ");
			sb.append("\n");
		}

		// Show rankSums
		sb.append("\nRankSums:\n");
		for( int ns = 0; ns < numberOfSamples; ns++ ) {
			sb.append(ns + ":\t");
			for( int i = 0; i < n; i++ )
				sb.append(rankSum[ns][i] + " ");
			sb.append("\n");
		}
		return sb.toString();
	}

	public double variance(int nt) {
		double var = 0;
		double dnt = nt;
		double dn = n;
		double mu = mean(nt);
		for( int i = 1; i <= n; i++ )
			for( int j = 1; j <= n; j++ ) {
				double di = i;
				double dj = j;
				if( i == j ) var += di * dj * dnt / dn;
				else var += di * dj * dnt * (dnt - 1) / (dn * (dn - 1));
			}

		var -= mu * mu;
		return var;
	}

}
