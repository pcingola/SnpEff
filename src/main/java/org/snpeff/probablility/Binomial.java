package org.snpeff.probablility;

/**
 * 
 * Calculate binomial distribution 
 * 
 * References http://en.wikipedia.org/wiki/Binomial_distribution
 * 
 * @author pcingola
 */

public class Binomial {

	/** A small number */
	public static double EPSILON = 1E-20;

	/** Singleton */
	private static Binomial binomial = null;

	/**
	 * Cache results for Sum[ log(i) ]
	 * WARNING: This cache will grow forever
	 */
	double sumLog[] = { 0.0 };

	public static Binomial get() {
		if (binomial == null) binomial = new Binomial();
		return binomial;
	}

	/**
	 * Cumulative probability function
	 * 
	 * P( K <= k )
	 * 
	 * @param p : probability of a success in a single Bernoulli trial
	 * @param k : Number of successes
	 * @param n : Number of trials
	 * @return
	 */
	public double cdf(double p, int k, int n) {
		if (k < 0) return 0;
		if (k >= n) return 1.0;

		double cdf = 0;
		for (int i = 0; i <= k; i++)
			cdf += pdf(p, i, n);

		cdf = Math.min(1.0, cdf);
		return cdf;
	}

	/**
	 * Cumulative probability function, upper tail
	 * 
	 * P( K > k )
	 * 
	 * @param p : probability of a success in a single Bernoulli trial
	 * @param k : Number of successes
	 * @param n : Number of trials
	 * @return
	 */
	public double cdfUp(double p, int k, int n) {
		if (k < 0) return 1.0;
		if (k >= n) return 0;

		double cdf = 0;
		// Sum smaller numbers first
		for (int i = n; i > k; i--)
			cdf += pdf(p, i, n);

		cdf = Math.min(1.0, cdf);
		return cdf;
	}

	/**
	 * Cumulative probability function, upper tail
	 * 
	 * P( K >= k )
	 * 
	 * @param p : probability of a success in a single Bernoulli trial
	 * @param k : Number of successes
	 * @param n : Number of trials
	 * @return
	 */
	public double cdfUpEq(double p, int k, int n) {
		if (k < 0) return 1.0;
		if (k > n) return 0;

		double cdf = cdfUp(p, k, n) + pdf(p, k, n);
		cdf = Math.min(1.0, cdf);
		return cdf;
	}

	/**
	 * Update array size
	 * @param n
	 */
	synchronized void newSumLog(int n) {
		if (n >= sumLog.length) {
			double sumLogOld[] = sumLog;
			double sumLogNew[] = new double[n + 1];

			// Copy old values
			for (int i = 0; i < sumLogOld.length; i++)
				sumLogNew[i] = sumLogOld[i];

			// Calc new values
			for (int i = sumLogOld.length; i < sumLogNew.length; i++)
				sumLogNew[i] = sumLogNew[i - 1] + Math.log(i);

			sumLog = sumLogNew;
		}
	}

	/**
	 * Probability density function
	 * @param p : probability of a success in a single Bernoulli trial
	 * @param k : Number of successes
	 * @param n : Number of trials
	 * @return
	 */
	public double pdf(double p, int k, int n) {
		if ((k < 0) || (k > n)) return 0;
		return Math.exp(pdfLog(p, k, n));
	}

	public double pdfLog(double p, int k, int n) {
		if ((k < 0) || (k > n)) return 0;
		if (k == n) return k * Math.log(p);
		return sumLog(n) - (sumLog(k) + sumLog(n - k)) + k * Math.log(p) + (n - k) * Math.log(1 - p);
	}

	/**
	 * Calculate the sum of logs and store results in cache
	 * @param n
	 * @return Sum_{i \in 1..n}[ log(i) ] 
	 */
	double sumLog(int n) {
		// Not in the array? => update size
		if (n >= sumLog.length) newSumLog(n);
		return sumLog[n];
	}

	/**
	 * Convert values to 'R' command 
	 * @return
	 */
	public String toR(double p, int k, int n) {
		return "dbinom( " + k + ", " + n + ", " + p + " )";
	}

}
