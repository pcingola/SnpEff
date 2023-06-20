package org.snpeff.probablility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.snpeff.util.Log;

/**
 *
 * Calculate rank sum probability distribution function (pdf) and cumulative distribution function (cdf).
 * Note: This class assumes that ranks cannot be repeated (selecting without replacement)
 *
 * @author Pablo Cingolani
 *
 */

public class RankSumNoReplacementPdf {

	/**
	 * Algorithm type
	 * @author pcingola
	 *
	 */
	enum Algorithm {
		EXACT, UNIFORM, TRIANGULAR, NORMAL
	};

	/** Cache size roughly (N * NT)^2 */
	public static int CACHE_MAX_N = 30;
	public static String DEFAULT_CACHE_FILE = "/pdf_rank_sum_no_replacement.txt";
	public static int warnCDF = 0;

	private static RankSumNoReplacementPdf rankSumNoReplacementPdf = null;
	String cacheFile; // Cache file
	int cacheHit, cacheMiss; // Cache statistics
	HashMap<String, Apfloat> cachePdf, cacheCdf; // A cache to speedup calculations  cache[n][nt][r]

	public static RankSumNoReplacementPdf get() {
		if (rankSumNoReplacementPdf == null) rankSumNoReplacementPdf = new RankSumNoReplacementPdf();
		return rankSumNoReplacementPdf;
	}

	private RankSumNoReplacementPdf() {
		cacheInit();
		cacheHit = cacheMiss = 0;
		cacheFile = DEFAULT_CACHE_FILE;
		readCacheFile();
	}

	private RankSumNoReplacementPdf(String cacheFile) {
		cacheInit();
		cacheHit = cacheMiss = 0;
		this.cacheFile = cacheFile;
		readCacheFile();
	}

	private Apfloat cacheGetCdf(int n, int nt, long r) {
		String key = cacheKey(n, nt, r, 1, 0);
		Apfloat prob = cacheCdf.get(key);
		if (prob != null) {
			cacheHit++;
			return prob;
		}
		cacheMiss++;
		return RankSumPdf.BAD;
	}

	private Apfloat cacheGetPdf(int n, int nt, long r, long rmin, int out) {
		String key = cacheKey(n, nt, r, rmin, out);
		Apfloat prob = cachePdf.get(key);
		if (prob != null) {
			cacheHit++;
			return prob;
		}
		cacheMiss++;
		return RankSumPdf.BAD;
	}

	/**
	 * Initialize cache
	 */
	private void cacheInit() {
		cachePdf = new HashMap<String, Apfloat>();
		cacheCdf = new HashMap<String, Apfloat>();
	}

	/**
	 * Generate a hash key
	 * @param n
	 * @param nt
	 * @param r
	 * @param rmin
	 * @param out
	 * @return
	 */
	private String cacheKey(int n, int nt, long r, long rmin, int out) {
		return n + "_" + nt + "_" + r + "_" + rmin + "_" + out;
	}

	/**
	 * Deletes all values such that N=n, NT <= nt, rmin > 1 or out > 0
	 * i.e. deletes intermediate results not likely to be used again
	 *
	 * @param n
	 * @param nt
	 * @return
	 */
	private void cachePrune(int n) {
		HashMap<String, Apfloat> newCachePdf = new HashMap<String, Apfloat>();
		for (String key : cachePdf.keySet()) {
			// Parse key
			String field[] = key.split("_");
			int keyN = Integer.parseInt(field[0]);
			long keyRmin = Long.parseLong(field[3]);
			int keyOut = Integer.parseInt(field[4]);

			// Delete this entry?
			if ((keyN == n) && ((keyRmin > 1) || (keyOut > 0))) {
				// Delete entry: Do not copy to 'newCachePdf'
			} else newCachePdf.put(key, cachePdf.get(key));
		}

		cachePdf = newCachePdf;
	}

	/**
	 * Set a result in cache
	 * @param r
	 * @param nt
	 * @param p
	 */
	private void cacheSetCdf(int n, int nt, long r, Apfloat cdf) {
		if (canBeCached(n, nt)) {
			String key = cacheKey(n, nt, r, 1, 0);
			cacheCdf.put(key, cdf);
		}
	}

	public void cacheSetPdf(int n, int nt, long r, double pdf) {
		cacheSetPdf(n, nt, r, 1, 0, new Apfloat(pdf));
	}

	/**
	 * Set a result in cache
	 * @param r
	 * @param nt
	 * @param p
	 */
	private void cacheSetPdf(int n, int nt, long r, long rmin, int out, Apfloat pdf) {
		if (canBeCached(n, nt)) {
			String key = cacheKey(n, nt, r, rmin, out);
			cachePdf.put(key, pdf);
		}
	}

	/**
	 * Is the number in the cache
	 * @return true if it is
	 */
	public boolean canBeCached(int n, int nt) {
		return ((1 <= n) && (n <= CACHE_MAX_N) && (1 <= nt) && (nt <= CACHE_MAX_N));
	}

	/**
	 * Probability of getting a rank sum less or equal to 'r' when adding the ranks
	 * of 'nt' selected items. Items are ranked '1..n' (from 1 to 'n')
	 *
	 * Note: Approximated when 'n > 30'
	 *
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return
	 */
	public Apfloat cdf(int n, int nt, long r) {
		Apfloat cdf;
		long minR = minRankSum(n, nt);
		long maxR = maxRankSum(n, nt);

		// Check variable's limits
		if ((nt <= 0) || (nt > n)) return Apcomplex.ZERO;
		if (n <= 0) return Apcomplex.ZERO;
		if (r < minR) return Apcomplex.ZERO;
		if (r >= maxR) return Apcomplex.ONE;

		// If we select all the numbers in the rank, the rank sum has only one possible value (minRankSum = maxRankSum)
		if (n == nt) {
			if (minR <= r) return Apcomplex.ONE;
			return Apcomplex.ZERO;
		}

		// What's the probability distribution if there is no ranked items? => the rank sum is always 0
		if (nt == 0) {
			if (0 <= r) return Apcomplex.ONE; // P( r_sum <= r ) = P( 0 <= r ) = 1.0
			return Apcomplex.ZERO;
		}

		Algorithm algorithm;

		// Calculate CDF
		if (n <= CACHE_MAX_N) { // Small 'n' values => Use exact calculation
			cdf = cdfExact(n, nt, r);
			algorithm = Algorithm.EXACT;
		} else if ((nt == 1) || (nt == n - 1)) { // If NT is 1 or N-1 => Uniform distribution
			cdf = cdfUniform(n, nt, r);
			algorithm = Algorithm.UNIFORM;
		} else if ((nt == 2) || (nt == n - 2)) { // If NT is 2 or N-2 => Triangular distribution
			cdf = cdfTriangle(n, nt, r);
			algorithm = Algorithm.TRIANGULAR;
		} else { // Use Normal approximation
			cdf = cdfNormal(n, nt, r);
			algorithm = Algorithm.NORMAL;
		}

		// Sanity check
		// Note: CDF cannot be 0.0 because those conditions were checked at the begining of this method
		if ((cdf.compareTo(Apcomplex.ZERO) <= 0) || (cdf.compareTo(Apcomplex.ONE) > 1.0)) {
			warnCDF++;
			if (warnCDF < 100) Log.debug("Warning! CDF should be greater then zero for (algorith: " + algorithm + "):\tN = " + n + "\tNT = " + nt + "\tR = " + r + "\tminRankSum = " + minR + "\tmean = " + mean(n, nt) + "\tsigma = " + sigma(n, nt));
			throw new RuntimeException("Warning! CDF should be greater then zero for (algorith: " + algorithm + "):\tN = " + n + "\tNT = " + nt + "\tR = " + r + "\tminRankSum = " + minR + "\tmean = " + mean(n, nt) + "\tsigma = " + sigma(n, nt));
		}

		return cdf;
	}

	/**
	 * Cumulative density function (cdf)
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is less or equal to 'r'
	 */
	public Apfloat cdfExact(int n, int nt, long r) {
		// Is it in the cache?
		Apfloat cdf = cacheGetCdf(n, nt, r);
		if (RankSumPdf.isOk(cdf)) {
			cacheHit++;
			return (cdf);
		}

		// cdf = Sum pdf
		cdf = new Apfloat(0);
		for (int i = 1; i <= r; i++)
			cdf = cdf.add(pdfExact(n, nt, i));

		// Cache result
		cacheSetCdf(n, nt, r, cdf);

		return (cdf);
	}

	/**
	 * Normal approximation to rankSum CDF
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is less or equal to 'r'
	 */
	public Apfloat cdfNormal(int n, int nt, long r) {
		double mu = mean(n, nt);
		double sigma = Math.sqrt(variance(n, nt));
		return NormalDistribution.cdf(r, mu, sigma);
	}

	/**
	 * Uniform 'approximation' to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param dr : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat cdfTriangle(int n, int nt, long r) {
		if ((nt != 2) && (nt != n - 2)) throw new RuntimeException("Triangle approximation is only valid fot 'nt = {2, N-2}'!");
		double dr = r;

		double rMin = minRankSum(n, nt) - 1;
		if (dr <= rMin) return Apcomplex.ZERO;

		double rMax = maxRankSum(n, nt) + 1;
		if (dr >= rMax) return Apcomplex.ONE;

		double mean = mean(n, nt);

		double cdf;
		if (dr <= mean) cdf = (((dr - rMin) * (dr - rMin)) / ((rMax - rMin) * (mean - rMin)));
		else cdf = (1.0 - ((rMax - dr) * (rMax - dr)) / ((rMax - rMin) * (rMax - mean)));

		return new Apfloat(cdf);
	}

	/**
	 * Uniform 'approximation' to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat cdfUniform(int n, int nt, long r) {
		if ((nt != 1) && (nt != n - 1)) throw new RuntimeException("Uniform approximation is only valid fot 'nt = {1, N-1}'!");

		double rMin = minRankSum(n, nt);
		if (r < rMin) return Apcomplex.ZERO;

		double rMax = maxRankSum(n, nt);
		if (r > rMax) return Apcomplex.ONE;

		double cdf = ((r) - rMin + 1) / (n);
		return new Apfloat(cdf);
	}

	/**
	 * Create a cache file
	 * @param fileName
	 */
	public void createCacheFile() {
		Date start = new Date();

		try {
			BufferedWriter outFile = new BufferedWriter(new FileWriter(cacheFile));

			for (int n = 1; n <= CACHE_MAX_N; n++) {
				System.out.print("N: " + n + "\t");
				for (int nt = 1; nt <= n; nt++) {
					System.out.print('.');
					for (long r = 1; r <= (n * nt); r++) {
						// Calculate pdf
						Apfloat pdf = pdfExact(n, nt, r);

						// Write it to file
						outFile.write(n + "\t" + nt + "\t" + r + "\t" + pdf + "\n");
					}
				}

				// Keep cache size as small as possible
				cachePrune(n);

				// How long did it take so far?
				Date now = new Date();
				long elapsed = (now.getTime() - start.getTime()) / 1000;
				System.out.println("Elapsed: " + elapsed + "s\t" + toStringCache());
			}

			outFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getCacheFile() {
		return cacheFile;
	}

	/**
	 * Maximum possible rank sum
	 * @param n
	 * @param nt
	 * @return
	 */
	public long maxRankSum(int n, int nt) {
		double dn = n;
		double dnt = nt;
		return ((long) (dnt * (dn - dnt)) + minRankSum(n, nt));
	}

	/**
	 * Mean value for a given N and N_T
	 * @param n
	 * @param nt
	 * @return
	 */
	public double mean(int n, int nt) {
		double dn = n;
		double dnt = nt;
		double mean = ((dnt * (dn + 1))) / 2.0;
		return mean;
	}

	/**
	 * Minimum possible rank sum
	 * @param n
	 * @param nt
	 * @return
	 */
	public long minRankSum(int n, int nt) {
		double dnt = nt;
		return (long) ((dnt + 1) * (dnt / 2));
	}

	/**
	 * Probability of getting a rank sum equal to 'r' when adding the ranks
	 * of 'nt' selected items. Items are ranked '1..n' (from 1 to 'n')
	 *
	 * Note: Approximated when 'n > 30'
	 *
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return
	 */
	public Apfloat pdf(int n, int nt, long r) {
		long minR = minRankSum(n, nt);
		long maxR = maxRankSum(n, nt);

		// Check variable's limits
		if ((nt <= 0) || (nt > n)) return Apcomplex.ZERO;
		if (n <= 0) return Apcomplex.ZERO;
		if (r < minR) return Apcomplex.ZERO;
		if (r >= maxR) return Apcomplex.ONE;

		// If we select all the numbers in the rank, the rank sum has only one possible value (minRankSum = maxRankSum)
		if (n == nt) {
			if (minR <= r) return Apcomplex.ONE;
			return Apcomplex.ZERO;
		}

		// What's the probability distribution if there is no ranked items? => the rank sum is always 0
		if (nt == 0) {
			if (0 <= r) return Apcomplex.ONE; // P( r_sum <= r ) = P( 0 <= r ) = 1.0
			return Apcomplex.ZERO;
		}

		// Apply corresponding algorithm
		Apfloat pdf;
		Algorithm algorithm;

		if (n <= CACHE_MAX_N) {
			pdf = pdfExact(n, nt, r); // Small 'n' values => Use exact calculation
			algorithm = Algorithm.EXACT;
		} else if ((nt == 1) || (nt == n - 1)) {
			pdf = pdfUniform(n, nt, r); // If NT is 1 or N-1 => Uniform distribution
			algorithm = Algorithm.UNIFORM;
		} else if ((nt == 2) || (nt == n - 2)) {
			pdf = pdfTriangle(n, nt, r); // If NT is 2 or N-2 => Triangular distribution
			algorithm = Algorithm.TRIANGULAR;
		} else {
			pdf = pdfNormal(n, nt, r); // Use Normal approximation
			algorithm = Algorithm.NORMAL;
		}

		// Sanity check
		// Note: PDF cannot be 0.0 because those conditions were checked at the begining of this method
		if ((pdf.compareTo(Apcomplex.ZERO) <= 0) || (pdf.compareTo(Apcomplex.ONE) > 1.0)) throw new RuntimeException("Warning! PDF should be greater then zero for (algorith: " + algorithm + "):\tN = " + n + "\tNT = " + nt + "\tR = " + r + "\tminRankSum = " + minR + "\tmean = " + mean(n, nt) + "\tsigma = " + sigma(n, nt));

		return pdf;
	}

	/**
	 * Probability of getting a rank sum equal to 'r' when adding the ranks
	 * of 'nt' selected items. Items are ranked '1..n' (from 1 to 'n')
	 * Note: Exact calculation
	 * Wrapper to 'real' pdf function
	 *
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return
	 */
	public Apfloat pdfExact(int n, int nt, long r) {
		return pdfExact(n, nt, r, 1, 0);
	}

	/**
	 * Probability density function (pdf): Exact calculation
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat pdfExact(int n, int nt, long r, long rmin, int out) {
		// Quick sanity checks (variable's limits)
		long minR = (int) ((nt + 1) * ((double) nt) / 2);
		long minR2 = nt * (rmin - 1) + minR;
		long maxR = nt * (n - nt) + minR;
		if ((r < minR2) || (r > maxR) || (r < rmin)) return Apcomplex.ZERO;
		if ((nt <= 0) || (nt > n)) return Apcomplex.ZERO;
		if (n <= 0) return Apcomplex.ZERO;
		if (n < rmin) return Apcomplex.ZERO;

		// Cut conditions
		if (nt == 1) {
			double p = 1.0 / (n - out);
			return new Apfloat(p); // For NT=1
		}

		// Is it cached?
		Apfloat p = cacheGetPdf(n, nt, r, rmin, out);
		if (RankSumPdf.isOk(p)) return p;

		// Perform recursion & sum
		Apfloat sum = new Apfloat(0);
		long rmax = n;
		if (rmax > (r - 1)) rmax = r - 1;
		for (long i = rmin; i < rmax; i++) {
			Apfloat p1 = pdfExact(n, 1, i, i, out);
			Apfloat p2 = pdfExact(n, nt - 1, r - i, i + 1, out + 1);
			sum = sum.add(p1.multiply(p2).multiply(new Apfloat(nt)));
		}
		p = sum;

		// Let's cache it
		cacheSetPdf(n, nt, r, rmin, out, p);

		return p;
	}

	/**
	 * Normal approximation to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat pdfNormal(int n, int nt, long r) {
		double mu = mean(n, nt);
		double sigma = Math.sqrt(variance(n, nt));
		return NormalDistribution.pdf(r, mu, sigma);
	}

	/**
	 * Uniform 'approximation' to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param dr : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat pdfTriangle(int n, int nt, long r) {
		if ((nt != 2) && (nt != n - 2)) throw new RuntimeException("Triangle approximation is only valid fot 'nt = {2, N-2}'!");
		double dr = r;

		double rMin = minRankSum(n, nt) - 1;
		if (dr <= rMin) return Apcomplex.ZERO;

		double rMax = maxRankSum(n, nt) + 1;
		if (dr >= rMax) return Apcomplex.ZERO;

		double mean = mean(n, nt);

		double pdf;
		if (dr <= mean) pdf = (2.0 * (dr - rMin) / ((rMax - rMin) * (mean - rMin)));
		else pdf = (2 * (rMax - dr) / ((rMax - rMin) * (rMax - mean)));
		return new Apfloat(pdf);
	}

	/**
	 * Uniform 'approximation' to rank sum statistic
	 * @param n : Maximum rank number
	 * @param nt : Number of elements in the sum
	 * @param r : rank sum value
	 * @return The probability that selecting 'nt' elements out of 'n' ranked elements, the rank sum is equal to 'r'
	 */
	public Apfloat pdfUniform(int n, int nt, long r) {
		if ((nt != 1) && (nt != n - 1)) throw new RuntimeException("Uniform approximation is only valid fot 'nt = {1, N-1}'!");

		double rMin = minRankSum(n, nt);
		if (r < rMin) return Apcomplex.ZERO;

		double rMax = maxRankSum(n, nt);
		if (r > rMax) return Apcomplex.ZERO;

		double pdf = 1.0 / (n);
		return new Apfloat(pdf);
	}

	/**
	 * Read cache file
	 */
	public void readCacheFile() {
		try {
			BufferedReader inFile = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("rank_sum_no_replacement.prob")));

			String line;
			for (int lineNum = 1; (line = inFile.readLine()) != null; lineNum++) {
				String fields[] = line.split("\t");
				if (fields.length == 4) {
					int n = Integer.parseInt(fields[0]);
					int nt = Integer.parseInt(fields[1]);
					long r = Long.parseLong(fields[2]);
					double pdf = Double.parseDouble(fields[3]);
					Apfloat pdfAp = new Apfloat(pdf);

					cacheSetPdf(n, nt, r, 1, 0, pdfAp);
				} else throw new RuntimeException("\nError: Unexpected number of fields (" + fields.length + " fields, line " + lineNum + "): '" + line + "'");
			}

			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCacheFile(String cacheFile) {
		this.cacheFile = cacheFile;
	}

	/**
	 * Wrapper to Sqrt(variance)
	 * @param n
	 * @param nt
	 * @return
	 */
	public double sigma(int n, int nt) {
		return Math.sqrt(variance(n, nt));
	}

	/**
	 * Some cache statistics
	 * @return
	 */
	public String toStringCache() {
		double perc = 0;
		if (cacheHit > 0) perc = ((int) (10000 * (((double) cacheMiss) / ((double) cacheHit)))) / 100;
		return "Cache size: " + cachePdf.size() + "\tMiss/Hit: " + cacheMiss + " / " + cacheHit + " ( " + perc + "% )";
	}

	/**
	 * Variance for a given N and N_T
	 * @param n
	 * @param nt
	 * @return
	 */
	public double variance(int n, int nt) {
		double dn = n, dnt = nt;
		double var = 0;
		double mu = mean(n, nt);
		double kr = (dn + 1) * (2 * dn + 1) * dn / 6;
		double krrp = dn / 2 * (dn + 1);
		krrp = krrp * krrp - kr;
		var = (dnt * (dnt - 1)) / (dn * (dn - 1)) * krrp + dnt / dn * kr - mu * mu;
		return var;
	}

	/**
	 * Variance for a given N and N_T (slow method, only used for debugging)
	 * @param n
	 * @param nt
	 * @return
	 */
	public double varianceSlow(int n, int nt) {
		double var = 0;
		double dnt = nt;
		double dn = n;
		double mu = mean(n, nt);
		for (int i = 1; i <= n; i++)
			for (int j = 1; j <= n; j++) {
				double di = i;
				double dj = j;
				if (i == j) var += di * dj * dnt / dn;
				else var += di * dj * dnt * (dnt - 1) / (dn * (dn - 1));
			}

		var -= mu * mu;
		return var;
	}

}
