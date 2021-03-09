package org.snpeff.probablility;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.snpeff.util.Log;

/**
 *
 * Calculate hypergeometric distribution using an optimized algorithm
 * that avoids problems with big factorials.
 *
 * Also calculated Fisher's exact test
 *
 * In general everything is expressed in a 2x2 'contingency' table
 * form sequence of 'n' draws from a finite population without
 * replacement
 *					drawn		not drawn		|	total
 *	defective 		k			D - k			|	D
 *	nondefective	n - k		N + k - n - D	|	N - D
 *					----------------------------+----------
 *	total 			n			N - n 			|	N
 *
 * This can be viewed as if we have a bag of N marbles
 * having D white marbles, and we take a sample of n
 * marbles(without replacement)
 *
 * N : Total marbles
 * D : White marbles => N-D : Black marbles
 * n : marbles drawn => N-n : not drawn
 * k : white marbles drawn
 *
 * @author pcingola
 *
 */

public class Hypergeometric {

	/** Singleton */
	private static Hypergeometric hypergeometric = null;
	/** A small number */
	public static double EPSILON = 1E-20;

	/**
	 * Cache results for Sum[ log(i) ]
	 * WARNING: This cache will grow forever
	 */
	double sumLog[] = { 0.0 };

	public static Hypergeometric get() {
		if (hypergeometric == null) hypergeometric = new Hypergeometric();
		return hypergeometric;
	}

	public static void main(String[] args) {
		int numTests = 1000;
		Random rand = new Random(20110122);

		Date start = new Date();
		for (int i = 0; i < numTests; i++) {
			int N = rand.nextInt(100000) + 1;
			int D = rand.nextInt(N) + 1;
			int n = rand.nextInt(N) + 1;
			int k = rand.nextInt(Math.min(n, D));
			Hypergeometric.get().hypergeometric(k, N, D, n);
		}
		Date end = new Date();
		Log.debug("Elapsed:" + (end.getTime() - start.getTime()));

	}

	private Hypergeometric() {
	}

	/**
	 * Check if hypergeometric paramters are correct
	 * @param k
	 * @param N
	 * @param D
	 * @param n
	 * @return
	 */
	public boolean checkHypergeometricParams(int k, int N, int D, int n) {
		if ((k < 0) || (N < 0) || (D < 0) || (n < 0)) return false;

		/* Change of variables
		 *					drawn		not drawn			|	total
		 *	defective 		a = k		b = D - k			|	D
		 *	nondefective	c = n - k	d = N + k - n - D	|	N - D
		 *					--------------------------------+----------
		 *	total 			n			N - n 				|	N
		 */
		int a, b, c, d;
		a = k;
		b = D - k;
		c = n - k;
		d = N + k - n - D;

		// Check values
		if ((a < 0) || (b < 0) || (c < 0) || (d < 0) || (N < 0)) return false;
		return true;
	}

	/**
	 * See http://en.wikipedia.org/wiki/Hypergeometric_distribution
	 * @param k : white marbles drawn
	 * @param N : Total marbles
	 * @param D : White marbles => N-D : Black marbles
	 * @param n : marbles drawn => N-n : not drawn
	 * @return Hypergeometric distribution
	 *
	 * References:
	 * 		http://en.wikipedia.org/wiki/Fisher%27s_exact_test
	 * 		http://en.wikipedia.org/wiki/Hypergeometric_distribution
	 */
	public double hypergeometric(int k, int N, int D, int n) {
		double hypergeometric = 1;
		double numeratorLog = 0, denominatorLog = 0;
		int a, b, c, d;

		/* Change of variables
		 *					drawn		not drawn			|	total
		 *	defective 		a = k		b = D - k			|	D
		 *	nondefective	c = n - k	d = N + k - n - D	|	N - D
		 *					--------------------------------+----------
		 *	total 			n			N - n 				|	N
		 */
		a = k;
		b = D - k;
		c = n - k;
		d = N + k - n - D;
		int ab = a + b, cd = c + d, ac = a + c, bd = b + d;

		// Check values
		if ((a < 0) || (b < 0) || (c < 0) || (d < 0) || (N < 0) || (D < 0) || (n < 0) || (k < 0)) {
			Log.debug("WARNING: Invalid values. k:" + k + ", N:" + N + ", D:" + D + ", n:" + n + "\t=> a:" + a + ", b:" + b + ", c:" + c + ", d:" + d);
			return 0;
		}

		/*
		 * Here we calculate de formula
		 * 		Hyper() = (a+b)! (c+d)! (a+c)! (b+d)! / ( N! a! b! c! d! )
		 *              =     D! (N-D)!     n! (N-n)! / ( N! a! b! c! d! )
		 * by representing numerator and denominator by a
		 * multiplicity, e.g.:
		 * 		If for i=7, count=3 we need to multiply by 7^3.
		 * 		If for i=9, count=-2 we need to divide by 9^2.
		 */

		denominatorLog += sumLog(N);
		denominatorLog += sumLog(a);
		denominatorLog += sumLog(b);
		denominatorLog += sumLog(c);
		denominatorLog += sumLog(d);

		numeratorLog += sumLog(ab);
		numeratorLog += sumLog(cd);
		numeratorLog += sumLog(ac);
		numeratorLog += sumLog(bd);

		double hypergeometricLog = numeratorLog - denominatorLog;
		hypergeometric = Math.exp(hypergeometricLog);

		// A probability can't be negative
		if (hypergeometric < 0) throw new RuntimeException("Negative cumulativeHG = " + hypergeometric + "\n\t\t\t\t\tcalculating hypergeometric(" + k + ", " + N + ", " + D + ", " + n + ")");

		if ((hypergeometricLog < 0.0) && (hypergeometric == 0.0)) return Double.MIN_VALUE;
		return hypergeometric;
	}

	/**
	 * Update array size
	 * @param n
	 */
	synchronized void newSumLog(int n) {
		if (n >= sumLog.length) {
			// Copy and resize
			double sumLogNew[] = Arrays.copyOf(sumLog, n + 1);

			// Calc new values
			for (int i = sumLog.length; i < sumLogNew.length; i++)
				sumLogNew[i] = sumLogNew[i - 1] + Math.log(i);

			sumLog = sumLogNew;
		}
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
	public String toR(int k, int N, int D, int n) {
		return "dhyper( " + k + ", " + D + ", " + (N - D) + ", " + n + " )";
	}

}
