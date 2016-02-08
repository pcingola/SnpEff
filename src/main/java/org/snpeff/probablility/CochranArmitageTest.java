package org.snpeff.probablility;


/**
 * 
 * Calculate a Cochran-Armitage test
 * 
 * Reference: http://en.wikipedia.org/wiki/Cochran-Armitage_test_for_trend
 * 
 * The trend test is applied when the data take the form of a 2 x k contingency 
 * table. For example, if k = 3 we have
 * 
 * 			    B=1		B=2		B=3
 * 			A=1	N_11	N_12	N_13	R_1
 * 			A=2	N_21	N_22	N_23	R_2
 * 
 * The test statistic is:
 * 
 * 		T = sum_i[ t_i (N_1i R_2 - N_2i R_1]
 * 
 * @author pcingola
 *
 */

public class CochranArmitageTest {

	public static final double WEIGHT_DOMINANT[] = { 1.0, 1.0, 0.0 };
	public static final double WEIGHT_RECESSIVE[] = { 0.0, 1.0, 1.0 };
	public static final double WEIGHT_TREND[] = { 0.0, 1.0, 2.0 };

	/** Singleton */
	private static CochranArmitageTest cochranArmitageTest = new CochranArmitageTest();

	/** A small number */
	public static double EPSILON = 1E-20;

	public static CochranArmitageTest get() {
		return cochranArmitageTest;
	}

	private CochranArmitageTest() {
	}

	public double p(int N1[], int N2[], double weight[]) {
		double t = test(N1, N2, weight);
		if (t > 0) t = -t;
		double p = new org.apache.commons.math3.distribution.NormalDistribution(0, 1).cumulativeProbability(t);
		return p;
	}

	/**
	 * Calculate T value
	 * @param N1
	 * @param N2
	 * @param weight
	 * @return
	 */
	double t(int N1[], int N2[], double weight[]) {
		int R1 = 0, R2 = 0;
		int k = N1.length;

		for (int i = 0; i < k; i++) {
			R1 += N1[i];
			R2 += N2[i];
		}

		double t = 0;
		for (int i = 0; i < N1.length; i++)
			t += weight[i] * ((N1[i] * R2) - (N2[i] * R1));

		return t;
	}

	/**
	 * 
	 * Calculate CochranArmitageTest using the 
	 * following contingency table
	 * 
	 * 	    B=1		B=2		...		B=N
	 * 	A=1	N_11	N_12	...		N_1N	R_1
	 * 	A=2	N_21	N_22	...		N_2N	R_2
	 * 
	 * @param N1 : Values for the first row 
	 * @param N2 : Values for the second row
	 * @param weight : Weight values
	 * 
	 * @return
	 */
	public double test(int N1[], int N2[], double weight[]) {
		// Sanity checks
		if (N1.length != N2.length) throw new RuntimeException("Row length do not match: " + N1.length + " != " + N2.length);
		if (N1.length != weight.length) throw new RuntimeException("Weight length does not match data rows length: " + N1.length + " != " + weight.length);

		// Calculate T value
		double t = t(N1, N2, weight);
		double var = var(N1, N2, weight);

		return t / Math.sqrt(var);
	}

	/**
	 * Calculate variance
	 * @param N1
	 * @param N2
	 * @param weight
	 * @return
	 */
	double var(int N1[], int N2[], double weight[]) {
		int k = N1.length;

		// Calculate R1 and R2
		int R1 = 0, R2 = 0;
		for (int i = 0; i < k; i++) {
			R1 += N1[i];
			R2 += N2[i];
		}

		// Total
		int N = R1 + R2;

		// Calculate first sum (see reference)
		double sum1 = 0;
		for (int i = 0; i < k; i++) {
			int Ci = N1[i] + N2[i];
			sum1 += (weight[i] * weight[i]) * Ci * (N - Ci);
		}

		// Calculate second sum
		double sum2 = 0;
		for (int i = 0; i < (k - 1); i++) {
			int Ci = N1[i] + N2[i];
			for (int j = i + 1; j < k; j++) {
				int Cj = N1[j] + N2[j];
				sum2 += weight[i] * weight[j] * Ci * Cj;
			}
		}

		// Calculate variance
		double var = ((double) R1 * R2) / N * (sum1 - 2.0 * sum2);

		return var;
	}

}
