package org.snpeff.probablility;

import org.apache.commons.math3.special.Gamma;

/**
 *
 * Calculate Fisher's exact test (based on hypergeometric distribution)
 *
 * @author pcingola
 *
 */
public class FisherExactTest {

	/** Singleton */
	private static FisherExactTest fisherExactTest = null;

	Hypergeometric hd;

	public static FisherExactTest get() {
		if (fisherExactTest == null) fisherExactTest = new FisherExactTest();
		return fisherExactTest;
	}

	private FisherExactTest() {
		hd = Hypergeometric.get();
	}

	/**
	 * Can ChiSquare approximation be used? A rule of the thumb says it can be
	 * used if every expected frequency is more than 10
	 *
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @return Chi-Square approximation
	 */
	public boolean canUseChiSquareApproximation(int k, int N, int D, int n) {
		/*
		 * Use different names for contingency table:
		 * See 'Global functional profiling of gene expression', Draghici et. al, table 2 (page 103)
		 *
		 * 					drawn		not drawn		|	total
		 *	defective 		n11			n12				|	N1d
		 *	nondefective	n21			n22				|	N2d
		 *					----------------------------+----------
		 *	total 			Nd1			Nd2				|	Ndd
		 */
		double n11 = k;
		double n12 = D - k;
		double n21 = n - k;
		double n22 = N + k - n - D;
		double N1d = n11 + n12; // 'd' stands for 'dot'
		double N2d = n21 + n22;
		double Nd1 = n11 + n21;
		double Nd2 = n12 + n22;
		double Ndd = N;

		// Expected frecuencies
		double E11 = (N1d * Nd1) / Ndd;
		double E12 = (N1d * Nd2) / Ndd;
		double E21 = (N2d * Nd1) / Ndd;
		double E22 = (N2d * Nd2) / Ndd;

		// Is every expected frecuency more than 10?
		if ((E11 < 10) || (E12 < 10) || (E21 < 10) || (E22 < 10)) return false;

		// Ok
		return true;
	}

	/**
	 * Chi-Square approximation for Fisher's exact test
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @return Chi-Square approximation
	 */
	public double chiSquareApproximation(int k, int N, int D, int n) {
		/*
		 * Use different names for contingency table:
		 * See 'Global functional profiling of gene expression', Draghici et. al, table 2 (page 103)
		 *
		 * 					drawn		not drawn		|	total
		 *	defective 		n11			n12				|	N1d
		 *	nondefective	n21			n22				|	N2d
		 *					----------------------------+----------
		 *	total 			Nd1			Nd2				|	Ndd
		 */
		double n11 = k;
		double n12 = D - k;
		double n21 = n - k;
		double n22 = N + k - n - D;
		double N1d = n11 + n12; // 'd' stands for 'dot'
		double N2d = n21 + n22;
		double Nd1 = n11 + n21;
		double Nd2 = n12 + n22;
		double Ndd = N;
		if ((N1d != D) || (Nd1 != n) || (Nd2 != (N - n)) || (N2d != (N - D))) throw new RuntimeException("ERROR: This should never happen!");
		double chiSquare = (Ndd * Math.pow((Math.abs(n11 * n22 - n12 * n21)), 2)) / (N1d * N2d * Nd1 * Nd2);

		// Estimation is: 1 - chisquare_cdf( ChiSquare, 1)
		// Degrees of freedom = 1
		// return 1 - flanagan.analysis.Stat.chiSquareCDF(chiSquare, 1);
		return chiSquareCDFComplementary(chiSquare, 1);
	}

	/**
	 * Chi-Square Cumulative Distribution Function
	 * probability that an observed chi-square value
	 * for a correct model should be less than chiSquare
	 * nu  =  the degrees of freedom
	 *
	 * @param chiSquare
	 * @param nu
	 * @return
	 */
	public double chiSquareCDF(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return Gamma.regularizedGammaP(nu / 2.0D, chiSquare / 2.0D);
	}

	/**
	 * Chi-Square Complementary of Cumulative Distribution Function: 1 -  chiSquareCDF(x, nu)
	 * probability that an observed chi-square value
	 * for a correct model should be greater than chiSquare
	 * nu  =  the degrees of freedom
	 *
	 * @param chiSquare
	 * @param nu
	 * @return
	 */
	public double chiSquareCDFComplementary(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return Gamma.regularizedGammaQ(nu / 2.0D, chiSquare / 2.0D);
	}

	/**
	 * Fisher's exact test for 'k' or less (lower tail)
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @return
	 */
	public double fisherExactTestDown(int k, int N, int D, int n) {
		// Zero marbles drawn? => Nothing to calculate
		if (n == 0) {
			if (k == 1) return 1;
			return 0.0;
		}

		double cumulativeHG = 0;
		int minTest = Math.max(n + D - N, 0);
		for (int i = minTest; i < k; i++)
			cumulativeHG += hd.hypergeometric(i, N, D, n);
		return cumulativeHG;
	}

	/**
	 * Fisher's exact test for less than 'k' (lower tail)
	 * It also compares to a 'threshold' value to speedup the process. Whenever
	 * cumulative probability is over the threshold, 1.0 is returned
	 *
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @param theshold  Threshold value
	 * @return Cumulative probability or 1.0 (if cumulative is over the threshold)
	 */
	public double fisherExactTestDown(int k, int N, int D, int n, double threshold) {
		// Trivial case
		if ((k == 1) && (n == 0)) return 1;
		if ((k == 1) && (D == 0)) return 1;
		if (k == 0) return 0;

		k--; // Lower tail does not include 'k'

		// If 'k' is less then the mean, then it's the p-value will be more than 0.5
		// So we can just return '1.0' if the threshold is lower than 0.5
		double mean = mean(k, N, D, n);
		if ((k > mean) && (threshold <= 0.5)) return 1;

		double cumulativeHG = 0;
		int maxTest = Math.min(n, D);

		if (k >= maxTest) return 1;

		// Calculate extreme values
		double hypergeom = hd.hypergeometric(k, N, D, n);
		if (hypergeom <= 0) return Double.MIN_NORMAL; // Underflow? (report minimum double)

		// Calculate cumulative hypergeometric
		cumulativeHG += hypergeom;
		for (int i = k - 1; i >= 0; i--) {

			if (hypergeom <= 0) {
				return cumulativeHG; // Zero? => Underflow (report current sum)
			} else {
				// We can use the previous value to speed up this calculation
				// Calculate hypergeometric(k, N, D, n) / hypergeometric(k+1, N, D, n) = (D-k) (n-k) / ((k+1) (N+k+1-n-D))
				int kk = i + 1; // Previous value for 'k'
				double num = ((double) kk) * ((double) (N + kk - n - D));
				double den = ((double) (D - i)) * ((double) (n - i));
				hypergeom *= num / den;
			}

			// Cummulative distribution
			cumulativeHG += hypergeom;

			// Above threshold? => just return 1.0
			if (cumulativeHG >= threshold) return 1.0;
		}

		return Math.min(cumulativeHG, 1.0);
	}

	/**
	 * Fisher's exact test for 'k' or more (upper tail)
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @return
	 */
	public double fisherExactTestUp(int k, int N, int D, int n) {
		if (k == 0) return 1.0; // This line speeds up a lot of cases
		if (n == 0) return 0.0; // Zero marbles drawn? => Nothing to calculate

		double cumulativeHG = 0;
		int maxTest = Math.min(n, D);
		for (int i = k; i <= maxTest; i++)
			cumulativeHG += hd.hypergeometric(i, N, D, n);

		return Math.min(cumulativeHG, 1.0);
	}

	/**
	 * Fisher's exact test for 'k' or more
	 * It also compares to a 'threshold' value to speedup the process. Whenever
	 * cumulative probability is over the threshold, 1.0 is returned
	 *
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @param theshold  Threshold value
	 * @return Cumulative probability or 1.0 (if cumulative is over the threshold)
	 */
	public double fisherExactTestUp(int k, int N, int D, int n, double threshold) {
		if (k == 0) return 1; // This line speeds up a lot of cases

		// If 'k' is less then the mean, then it's the p-value will be more then the threshold (if threshold is above 1/2)
		double mean = mean(k, N, D, n);
		if ((k < mean) && (threshold < 0.5)) return 1;

		double cumulativeHG = 0;
		int maxTest = Math.min(n, D);

		// Calculate extreme values
		double hypergeom = hd.hypergeometric(k, N, D, n);
		if (hypergeom <= 0) return Double.MIN_NORMAL; // Underflow? (report minimum double)

		// Calculate cumulative hypergeometric
		cumulativeHG += hypergeom;
		for (int i = k + 1; i <= maxTest; i++) {

			if (hypergeom <= 0) {
				return cumulativeHG; // Zero? => Underflow (report current sum)
			} else {
				// We can use the previous value to speed up this calculation
				// Calculate hypergeometric(k, N, D, n) / hypergeometric(k+1, N, D, n) = (D-k) (n-k) / ((k+1) (N+k+1-n-D))
				int kk = i - 1; // Previous value for 'k'
				double num = ((double) (D - kk)) * ((double) (n - kk));
				double den = ((double) i) * ((double) (N + i - n - D));
				hypergeom *= num / den;
			}

			// Cummulative distribution
			cumulativeHG += hypergeom;

			// Above threshold? => just return 1.0
			if (cumulativeHG >= threshold) return 1.0;
		}

		return Math.min(cumulativeHG, 1.0);
	}

	/**
	 * Calculate the mean
	 * References: http://en.wikipedia.org/wiki/Hypergeometric_distribution
	 */
	public double mean(int k, int N, int D, int n) {
		return ((double) n) * ((double) D) / (N);
	}

	public double pValueDown(int k, int N, int D, int n) {
		return fisherExactTestDown(k + 1, N, D, n, 1.0);
	}

	/**
	 * Pvalue for 'k' or less
	 * Note: Includes 'k'
	 *
	 * It also compares to a 'threshold' value to speedup the process.
	 * Whenever cumulative probability is over the threshold, 1.0 is returned
	 * This is useful when we are interested on very small p-values
	 *
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @param theshold  Threshold value
	 * @return Cumulative probability or 1.0 (if cumulative is over the threshold)
	 */
	public double pValueDown(int k, int N, int D, int n, double threshold) {
		return fisherExactTestDown(k + 1, N, D, n, threshold);
	}

	public double pValueUp(int k, int N, int D, int n) {
		return fisherExactTestUp(k, N, D, n, 1.0);
	}

	/**
	 * Fisher's exact test for more than 'k'
	 * It also compares to a 'threshold' value to speedup the process.
	 * Whenever cumulative probability is over the threshold, 1.0 is returned
	 * This is useful when we are interested on very small p-values
	 *
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @param theshold  Threshold value
	 * @return Cumulative probability or 1.0 (if cumulative is over the threshold)
	 */
	public double pValueUp(int k, int N, int D, int n, double threshold) {
		return fisherExactTestUp(k, N, D, n, threshold);
	}

	/**
	 * Convert values to Fisher's 'R' command
	 * @return
	 */
	public String toR(int k, int N, int D, int n, boolean lowerTail) {
		return "phyper( " + k + ", " + D + ", " + (N - D) + ", " + n + ", lower.tail = " + Boolean.toString(lowerTail).toUpperCase() + " )";
	}

	/**
	 * Calculate the variance
	 * References: http://en.wikipedia.org/wiki/Hypergeometric_distribution
	 */
	public double variance(int k, int N, int D, int n) {
		return (((double) n) * ((double) D) * (N - n) * (N - n)) * ((N - D)) / (((double) N) * N * (N - 1));
	}

}
