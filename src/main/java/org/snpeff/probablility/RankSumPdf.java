package org.snpeff.probablility;

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.snpeff.util.Log;

/**
 *
 * Calculate rank sum probability distribution function (pdf) and cumulative distribution function (cdf).
 * Note: This class assumes that ranks can be repeated (selecting with replacement)
 *
 * @author Pablo Cingolani
 *
 */

public class RankSumPdf {

	/** Cache size roughly (N * NT)^2 */
	public static final int CACHE_MAX_N = 40;
	public static final int CACHE_MAX_NT = 40;

	public static final Apfloat BAD = new Apfloat(-1);

	/** Cache statistics */
	static int cacheHit, cacheMiss;

	/** A cache to speedup calculations  cache[n][nt][r] */
	static Apfloat[][][] cachePdf, cacheCdf;

	/** A small number */
	public static double SMALL = 1E-20;

	//-------------------------------------------------------------------------
	// Static init
	//-------------------------------------------------------------------------

	static {
		cacheInit();
		cacheHit = cacheMiss = 0;
	}

	//-------------------------------------------------------------------------
	// Static methods
	//-------------------------------------------------------------------------

	/**
	 * Get a cached result
	 */
	private static Apfloat cacheGetCdf(int n, int nt, int r) {
		return cacheCdf[n][nt][r];
	}

	/**
	 * Get a cached result
	 */
	private static Apfloat cacheGetPdf(int n, int nt, int r) {
		return cachePdf[n][nt][r];
	}

	/**
	 * Initialize cache
	 */
	private static void cacheInit() {
		System.out.println("RankSumPdf: Initializing cache");

		cachePdf = new Apfloat[CACHE_MAX_N + 1][CACHE_MAX_NT + 1][];
		cacheCdf = new Apfloat[CACHE_MAX_N + 1][CACHE_MAX_NT + 1][];

		// Initialize (memory and reset)
		for (int n = 1; n <= CACHE_MAX_N; n++)
			for (int nt = 1; nt <= CACHE_MAX_NT; nt++) {
				int maxRankSum = n * nt;

				cachePdf[n][nt] = new Apfloat[maxRankSum + 1];
				cacheCdf[n][nt] = new Apfloat[maxRankSum + 1];

				// Initialize values for this N & NT combination
				for (int rs = 1; rs <= maxRankSum; rs++) {
					cachePdf[n][nt][rs] = BAD;
					cacheCdf[n][nt][rs] = BAD;
				}
			}

		// Initialize: Calculate pdf/cdf values
		System.out.print("Initializing rankSum Pdf/Cdf caches:");
		for (int n = 1; n <= CACHE_MAX_N; n++) {
			System.out.print('.');
			for (int nt = 1; nt < n; nt++) {

				int maxRankSum = n * nt;
				Apfloat c = new Apfloat(0);

				for (int r = 1; r <= maxRankSum; r++) {
					Apfloat p = pdf(n, nt, r);
					c = c.add(p);
					cacheSetCdf(n, nt, r, c);
				}
			}
		}
		System.out.println("done");
	}

	/**
	 * Set a result in cache
	 * @param r
	 * @param nt
	 * @param p
	 */
	private static void cacheSetCdf(int n, int nt, int r, Apfloat p) {
		cacheCdf[n][nt][r] = p;
		cacheMiss++;
	}

	/**
	 * Set a result in cache
	 * @param r
	 * @param nt
	 * @param p
	 */
	private static void cacheSetPdf(int n, int nt, int r, Apfloat p) {
		cachePdf[n][nt][r] = p;
		cacheMiss++;
	}

	/**
	 * Is the number in the cache
	 * @return true if it is
	 */
	public static boolean canBeCached(int n, int nt) {
		return ((n <= CACHE_MAX_N) && (nt <= CACHE_MAX_NT));
	}

	/**
	 * Cumulative density function (cdf)
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is less or equal to 'r'
	 */
	public static Apfloat cdf(int n, int nt, int r) {
		// Check variable's limits
		if ((nt <= 0) || (nt > n)) return Apcomplex.ZERO;
		if (n <= 0) return Apcomplex.ZERO;
		long max = maxRankSum(n, nt);
		long min = minRankSum(n, nt);
		if (r < min) return Apcomplex.ZERO;
		if (r >= max) return Apcomplex.ONE;

		// Approximate by normal distribution?
		if (!canBeCached(n, nt)) return cdfNormal(n, nt, r);

		// Is it in the cache?
		Apfloat cdf = cacheGetCdf(n, nt, r);
		if (cdf.compareTo(Apcomplex.ZERO) >= 0) {
			cacheHit++;
			return cdf;
		}

		// Sum pdf
		Apfloat sum = new Apfloat(0);
		for (int i = 1; i <= r; i++)
			sum = sum.add(pdf(n, nt, i));

		// Cache result
		cacheSetCdf(n, nt, r, sum);

		return sum;
	}

	/**
	 * Normal approximation to rankSum CDF
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is less or equal to 'r'
	 */
	public static Apfloat cdfNormal(int n, int nt, int r) {
		double mu = mean(n, nt);
		double sigma = sigma(n, nt);
		Apfloat cdf = NormalDistribution.cdf(r, mu, sigma);
		return cdf;
	}

	/**
	 * Is the value OK? (i.e. not 'BAD')
	 *
	 * @param p
	 * @return
	 */
	public static boolean isOk(Apfloat p) {
		return (p.compareTo(RankSumPdf.BAD) != 0);
	}

	//-------------------------------------------------------------------------
	// Main
	//-------------------------------------------------------------------------
	public static void main(String[] args) {
		System.out.println("Begin: RankSumPdf");

		for (double x = 0.0; x > -100; x -= 1.0) {
			Apfloat cdf = NormalDistribution.cdf(x, 0.0, 1.0);
			Log.debug("x: " + x + "\tcdf: " + cdf + "\tcdfOri: " + DistLib.normal.cumulative(x, 0.0, 1.0) + "\t" + new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0).density(x));
		}

		System.out.println("End: RankSumPdf");
	}

	/**
	 * Maximum possible rank sum
	 * @param n
	 * @param nt
	 * @return
	 */
	public static long maxRankSum(int n, int nt) {
		return ((long) nt) * ((long) n);
	}

	/**
	 * Mean value for a given N and N_T
	 * @param n
	 * @param nt
	 * @return
	 */
	public static double mean(int n, int nt) {
		return (nt) * ((n) + 1.0) / 2.0;
	}

	/**
	 * Minimum possible rank sum
	 * @param n
	 * @param nt
	 * @return
	 */
	public static long minRankSum(int n, int nt) {
		return nt;
	}

	/**
	 * Probability density function (pdf)
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public static Apfloat pdf(int n, int nt, int r) {
		if ((nt <= 0) || (nt > n)) return Apcomplex.ZERO;
		if (n <= 0) return Apcomplex.ZERO;
		long max = maxRankSum(n, nt);
		long min = minRankSum(n, nt);
		if (r < min) return Apcomplex.ZERO;
		if (r > max) return Apcomplex.ZERO;

		// Cut conditions
		if (nt == 1) {
			double p = 1.0 / (n);
			return new Apfloat(p); // For NT=1
		}

		// Approximate by normal distribution?
		if (!canBeCached(n, nt)) return pdfNormal(n, nt, r);

		// Is it in the cache?
		Apfloat pdf = cacheGetPdf(n, nt, r);
		if (pdf.compareTo(Apcomplex.ZERO) >= 0) {
			cacheHit++;
			return pdf;
		}

		// Perform recursion & sum
		Apfloat sum = new Apfloat(0);
		int maxSum = Math.max(Math.min(r - nt + 1, n), 1);
		for (int i = 1; i <= maxSum; i++)
			sum.add(pdf(n, nt - 1, r - i));
		Apfloat p = sum.divide(new Apfloat(n));

		// Cache result
		cacheSetPdf(n, nt, r, p);

		return (p);
	}

	/**
	 * Normal approximation to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public static Apfloat pdfNormal(int n, int nt, int r) {
		double mu = mean(n, nt);
		double sigma = sigma(n, nt);
		return NormalDistribution.pdf(n, mu, sigma);
	}

	/**
	 * Wrapper to Sqrt(variance)
	 * @param n
	 * @param nt
	 * @return
	 */
	public static double sigma(int n, int nt) {
		return Math.sqrt(RankSumPdf.variance(n, nt));
	}

	/**
	 * Variance for a given N and N_T
	 * @param n
	 * @param nt
	 * @return
	 */
	public static double variance(int n, int nt) {
		double dn = (n);
		return (nt) * (dn * dn - 1.0) / 12.0;
	}

}
