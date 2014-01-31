/*
*   Class   Stat
*
*   USAGE:  Statistical functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002 as part of Fmath
*   AMENDED: 12 May 2003 Statistics separated out from Fmath as a new class
*   DATE:    18 June 2005, 5 January 2006, 25 April 2006, 12, 21 November 2006
*            4 December 2006 (renaming of cfd and pdf methods - older version also retained)
*            31 December 2006, March 2007, 14 April 2007, 19 October 2007, 27 February 2008
*            29 March 2008, 7 April 2008, 29 April 2008 - 13 May 2008, 22-31 May 2008,
*            4-10 June 2008, 27 June 2008, 2-5 July 2008, 23 July 2008, 31 July 2008,
*            2-4 August 2008,  20 August 2008, 5-10 September 2008, 19 September 2008,
*            28-30 September 2008 (probability Plot moved to separate class, ProbabilityPlot)
*            4-5 October 2008,  8-13 December 2008, 14 June 2009, 13-23 October 2009,
*            8 February 2010, 18-25 May 2010, 2 November 2010, 4 December 2010, 19-25 January 2011
*            10 February 2011, 30 March 2011, 16 July 2011, 30 November 2011, 4 January 2012
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Stat.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2011 Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
*   Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
*   provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
*   and associated documentation or publications.
*
*   Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice,
+   this list of conditions and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
*   Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
*   the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission
*   from the Michael Thomas Flanagan:
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
*   Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
*   or its derivatives.
*
***************************************************************************************/

package flanagan.analysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import flanagan.circuits.Phasor;
import flanagan.complex.Complex;
import flanagan.integration.IntegralFunction;
import flanagan.integration.Integration;
import flanagan.interpolation.CubicSpline;
import flanagan.math.ArrayMaths;
import flanagan.math.Conv;
import flanagan.math.Fmath;
import flanagan.math.PsRandom;
import flanagan.plot.Plot;
import flanagan.plot.PlotGraph;
import flanagan.roots.RealRoot;
import flanagan.roots.RealRootFunction;

// Class to evaluate the Beta distribution function
class BetaFunct implements RealRootFunction {
	public double alpha = 0.0D;
	public double beta = 0.0D;
	public double min = 0.0D;
	public double max = 0.0D;
	public double cfd = 0.0D;

	@Override
	public double function(double x) {

		double y = cfd - Stat.betaCDF(min, max, alpha, beta, x);

		return y;
	}
}

// CLASSES NEEDED BY METHODS IN THE ABOVE Stat CLASS

// Class to evaluate the chi-square distribution function
class ChiSquareFunct implements RealRootFunction {

	public double cfd = 0.0D;
	public int nu = 0;

	@Override
	public double function(double x) {

		double y = cfd - Stat.chiSquareCDF(x, nu);

		return y;
	}
}

// Class to evaluate the linear correlation coefficient probablity function
// Needed in calls to Integration.gaussQuad
class CorrCoeff implements IntegralFunction {

	public double a;

	@Override
	public double function(double x) {
		double y = Math.pow((1.0D - x * x), a);
		return y;
	}
}

// Class to evaluate complementary regularised incomplte gamma function
class CrigFunct implements IntegralFunction {

	private double a = 0.0D;
	private double b = 0.0D;

	@Override
	public double function(double x) {
		double y = -x + (a - 1.0) * Math.log(x) - b;
		y = Math.exp(y);
		return y;
	}

	public void setA(double a) {
		this.a = a;
	}

	public void setB(double b) {
		this.b = b;
	}
}

// Class to evaluate the  Engset load equation
class EngsetLoad implements RealRootFunction {

	public double blockingProbability = 0.0D;
	public double totalResources = 0.0D;
	public double numberOfSources = 0.0D;

	@Override
	public double function(double x) {
		return blockingProbability - Stat.engsetProbability(x, totalResources, numberOfSources);
	}
}

// Class to evaluate the Engset probability equation
class EngsetProb implements RealRootFunction {

	public double offeredTraffic = 0.0D;
	public double totalResources = 0.0D;
	public double numberOfSources = 0.0D;

	@Override
	public double function(double x) {
		double mTerm = offeredTraffic / (numberOfSources - offeredTraffic * (1.0D - x));
		double pNumer = Stat.logFactorial(numberOfSources - 1) - Stat.logFactorial(totalResources) - Stat.logFactorial(numberOfSources - 1 - totalResources);
		double pDenom = 0.0D;
		double iDenom = 0.0D;
		double iCount = 0.0D;
		double pTerm = 0.0D;

		while (iCount <= totalResources) {
			iDenom = Stat.logFactorial(numberOfSources - 1) - Stat.logFactorial(iCount) - Stat.logFactorial(numberOfSources - 1 - iCount);
			iDenom += (iCount - totalResources) * Math.log(mTerm);
			pDenom += Math.exp(iDenom);
			iCount += 1.0D;
		}
		pTerm = Math.exp(pNumer - Math.log(pDenom));

		return x - pTerm;
	}
}

// Class to evaluate the Erlang B equation
class ErlangBfunct implements RealRootFunction {

	public double blockingProbability = 0.0D;
	public double totalResources = 0.0D;

	@Override
	public double function(double x) {
		return blockingProbability - Stat.erlangBprobability(x, totalResources);
	}
}

// Class to evaluate the Erlang C equation
class ErlangCfunct implements RealRootFunction {

	public double nonZeroDelayProbability = 0.0D;
	public double totalResources = 0.0D;

	@Override
	public double function(double x) {
		return nonZeroDelayProbability - Stat.erlangCprobability(x, totalResources);
	}
}

// Class to evaluate the F-distribution function
class FdistribtionFunct implements RealRootFunction {

	public double cfd = 0.0D;
	public int nu1 = 0;
	public int nu2 = 0;

	@Override
	public double function(double x) {

		double y = cfd - (1.0 - Stat.fCompCDF(x, nu1, nu2));
		// double y = cfd - Stat.fCompCDF(x, nu1, nu2);

		return y;
	}
}

// Class to evaluate the Gamma distribution function
class GammaFunct implements RealRootFunction {
	public double mu = 0.0D;
	public double beta = 0.0D;
	public double gamma = 0.0D;
	public double cfd = 0.0D;

	@Override
	public double function(double x) {

		double y = cfd - Stat.gammaCDF(mu, beta, gamma, x);

		return y;
	}
}

// Class to evaluate the normal distribution function
class GaussianFunct implements RealRootFunction {

	public double cfd = 0.0D;
	public double mean = 0.0D;
	public double sd = 0.0;

	@Override
	public double function(double x) {

		double y = cfd - Stat.gaussianCDF(mean, sd, x);

		return y;
	}
}

// Class to evaluate inverse gamma function
class InverseGammaFunct implements RealRootFunction {

	public double gamma = 0.0D;

	@Override
	public double function(double x) {

		double y = gamma - Stat.gamma(x);

		return y;
	}
}

// Class to evaluate the three parameter log-normal distribution function
class LogNormalThreeParFunct implements RealRootFunction {

	public double cfd = 0.0D;
	public double alpha = 0.0D;
	public double beta = 0.0D;
	public double gamma = 0.0D;

	@Override
	public double function(double x) {

		double y = cfd - Stat.logNormalThreeParCDF(alpha, beta, gamma, x);

		return y;
	}

}

// Class to evaluate the two parameter log-normal distribution function
class LogNormalTwoParFunct implements RealRootFunction {

	public double cfd = 0.0D;
	public double mu = 0.0D;
	public double sigma = 0.0D;

	@Override
	public double function(double x) {

		double y = cfd - Stat.logNormalCDF(mu, sigma, x);

		return y;
	}
}

public class Stat extends ArrayMaths {

	// INSTANCE VARIABLES
	private boolean nFactorOptionI = false; // = true  varaiance, covariance and standard deviation denominator = n
											// = false varaiance, covariance and standard deviation denominator = n-1
	private boolean nFactorReset = false; // = true when instance method resetting the denominator is called

	private boolean nEffOptionI = true; // = true  n replaced by effective sample number
										// = false n used as sample number
	private boolean nEffReset = false; // = true when instance method resetting the nEff choice called

	private boolean weightingOptionI = true; // = true  'little w' weights (uncertainties) used
												// = false 'big W' weights (multiplicative factors) used
	private boolean weightingReset = false; // = true when instance method resetting the nEff choice called

	private ArrayMaths amWeights = null; // weights as ArrayMaths
	private boolean weightsSupplied = false; // = true if weights entered

	private ArrayList<Object> upperOutlierDetails = new ArrayList<Object>(); // upper outlier search details
	// element 0 - number of ouliers   (Integer)
	// element 1 - outliers   (double[])
	// element 2 - outlier indices   (inmoved
	private boolean upperDone = false; // = true when upper oulier search ct[])
										// element 3 - array with ouliers reompleted even if no upper outliers found
	private ArrayList<Object> lowerOutlierDetails = new ArrayList<Object>(); // lower outlier search details
	// element 0 - number of ouliers   (Integer)
	// element 1 - outliers   (double[])
	// element 2 - outlier indices   (int[])
	// element 3 - array with ouliers removed
	private boolean lowerDone = false; // = true when lower oulier search completed even if no upper outliers found

	// STATIC VARIABLES
	private static boolean nFactorOptionS = false; // = true  varaiance, covariance and standard deviation denominator = n
													// = false varaiance and standard deviation denominator = n-1
	private static boolean nEffOptionS = true; // = true  n replaced by effective sample number
												// = false n used as sample number

	private static boolean weightingOptionS = true; // = true  'little w' weights (uncertainties) used
	// = false 'big W' weights (multiplicative factors) used

	// maximum number of iterations allowed in the contFract method
	private static int cfMaxIter = 500;

	// tolerance used in the contFract method
	private static double cfTol = 1.0e-8;

	// A small number close to the smallest representable floating point number
	public static final double FPMIN = 1e-300;

	private static boolean igSupress = false; // if true error messages in incompleteGammaSeries
												//  and incompleteGammaFract supressed

	// PRIVATE MEMBERS FOR USE IN GAMMA FUNCTION METHODS AND HISTOGRAM CONSTRUCTION METHODS

	// GAMMA FUNCTIONS
	//  Lanczos Gamma Function approximation - N (number of coefficients -1)
	private static int lgfN = 6;
	//  Lanczos Gamma Function approximation - Coefficients
	private static double[] lgfCoeff = { 1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5 };
	//  Lanczos Gamma Function approximation - small gamma
	private static double lgfGamma = 5.0;
	//  Maximum number of iterations allowed in Incomplete Gamma Function calculations
	private static int igfiter = 1000;
	//  Tolerance used in terminating series in Incomplete Gamma Function calculations
	private static double igfeps = 1e-8;

	// HISTOGRAM CONSTRUCTION
	//  Tolerance used in including an upper point in last histogram bin when it is outside due to rounding erors
	private static double histTol = 1.0001D;

	// Beta function
	// retained for compatibility reasons
	public static double beta(double z, double w) {
		return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
	}

	// beta distribution cdf
	public static double betaCDF(double alpha, double beta, double limit) {
		return betaCDF(0.0D, 1.0D, alpha, beta, limit);
	}

	// beta distribution pdf
	public static double betaCDF(double min, double max, double alpha, double beta, double limit) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		if (limit < min) throw new IllegalArgumentException("limit, " + limit + ", must be greater than or equal to the minimum value, " + min);
		if (limit > max) throw new IllegalArgumentException("limit, " + limit + ", must be less than or equal to the maximum value, " + max);
		return Stat.regularisedBetaFunction(alpha, beta, (limit - min) / (max - min));
	}

	// Beta function
	public static double betaFunction(double z, double w) {
		return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
	}

	// beta distribution mean
	public static double betaMean(double alpha, double beta) {
		return betaMean(0.0D, 1.0D, alpha, beta);
	}

	// beta distribution mean
	public static double betaMean(double min, double max, double alpha, double beta) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		return min + alpha * (max - min) / (alpha + beta);
	}

	// beta distribution mode
	public static double betaMode(double alpha, double beta) {
		return betaMode(0.0D, 1.0D, alpha, beta);
	}

	// beta distribution mode
	public static double betaMode(double min, double max, double alpha, double beta) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");

		double mode = Double.NaN;
		if (alpha > 1) {
			if (beta > 1) {
				mode = min + (alpha + beta) * (max - min) / (alpha + beta - 2);
			} else {
				mode = max;
			}
		} else {
			if (alpha == 1) {
				if (beta > 1) {
					mode = min;
				} else {
					if (beta == 1) {
						mode = Double.NaN;
					} else {
						mode = max;
					}
				}
			} else {
				if (beta >= 1) {
					mode = min;
				} else {
					System.out.println("Class Stat; method betaMode; distribution is bimodal wirh modes at " + min + " and " + max);
					System.out.println("NaN returned");
				}
			}
		}
		return mode;
	}

	// beta distribution pdf
	public static double betaPDF(double alpha, double beta, double x) {
		return betaPDF(0.0D, 1.0D, alpha, beta, x);
	}

	// beta distribution pdf
	public static double betaPDF(double min, double max, double alpha, double beta, double x) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		if (x < min) throw new IllegalArgumentException("x, " + x + ", must be greater than or equal to the minimum value, " + min);
		if (x > max) throw new IllegalArgumentException("x, " + x + ", must be less than or equal to the maximum value, " + max);
		double pdf = Math.pow(x - min, alpha - 1) * Math.pow(max - x, beta - 1) / Math.pow(max - min, alpha + beta - 1);
		return pdf / Stat.betaFunction(alpha, beta);
	}

	// Returns an array of Beta random deviates - clock seed
	public static double[] betaRand(double min, double max, double alpha, double beta, int n) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		PsRandom psr = new PsRandom();
		return psr.betaArray(min, max, alpha, beta, n);
	}

	// Returns an array of Beta random deviates - user supplied seed
	public static double[] betaRand(double min, double max, double alpha, double beta, int n, long seed) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter,  beta, " + beta + "must be greater than zero");
		PsRandom psr = new PsRandom(seed);
		return psr.betaArray(min, max, alpha, beta, n);
	}

	// Returns an array of Beta random deviates - clock seed
	public static double[] betaRand(double alpha, double beta, int n) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		PsRandom psr = new PsRandom();
		return psr.betaArray(alpha, beta, n);
	}

	// Returns an array of Beta random deviates - user supplied seed
	public static double[] betaRand(double alpha, double beta, int n, long seed) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter,  beta, " + beta + "must be greater than zero");
		PsRandom psr = new PsRandom(seed);
		return psr.betaArray(alpha, beta, n);
	}

	// beta distribution standard deviation
	public static double betaStandardDeviation(double alpha, double beta) {
		return betaStandDev(alpha, beta);
	}

	// beta distribution standard deviation
	public static double betaStandardDeviation(double min, double max, double alpha, double beta) {
		return betaStandDev(min, max, alpha, beta);
	}

	// beta distribution standard deviation
	public static double betaStandDev(double alpha, double beta) {
		return betaStandDev(0.0D, 1.0D, alpha, beta);
	}

	// beta distribution standard deviation
	public static double betaStandDev(double min, double max, double alpha, double beta) {
		if (alpha <= 0.0D) throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
		if (beta <= 0.0D) throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
		return ((max - min) / (alpha + beta)) * Math.sqrt(alpha * beta / (alpha + beta + 1));
	}

	// Binary Shannon Entropy returned as bits
	public static double binaryShannonEntropy(double p) {
		if (p > 1.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be less than or equal to 1");
		if (p < 0.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be greater than or equal to 0");
		double entropy = 0.0D;
		if (p > 0.0D && p < 1.0D) {
			entropy = -p * Fmath.log2(p) - (1 - p) * Fmath.log2(1 - p);
		}
		return entropy;
	}

	// Binary Shannon Entropy returned as bits
	public static double binaryShannonEntropyBit(double p) {
		return binaryShannonEntropy(p);
	}

	// Binary Shannon Entropy returned as dits
	public static double binaryShannonEntropyDit(double p) {
		if (p > 1.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be less than or equal to 1");
		if (p < 0.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be greater than or equal to 0");
		double entropy = 0.0D;
		if (p > 0.0D && p < 1.0D) {
			entropy = -p * Math.log10(p) - (1 - p) * Math.log10(1 - p);
		}
		return entropy;

	}

	// Binary Shannon Entropy returned as nats (nits)
	public static double binaryShannonEntropyNat(double p) {
		if (p > 1.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be less than or equal to 1");
		if (p < 0.0) throw new IllegalArgumentException("The probabiliy, " + p + ",  must be greater than or equal to 0");
		double entropy = 0.0D;
		if (p > 0.0D && p < 1.0D) {
			entropy = -p * Math.log(p) - (1 - p) * Math.log(1 - p);
		}
		return entropy;
	}

	// Returns a binomial mass probabilty function
	public static double binomial(double p, int n, int k) {
		if (k < 0 || n < 0) throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
		if (k > n) throw new IllegalArgumentException("\nk is greater than n");
		return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n - k))) * Math.pow(p, k) * Math.pow(1.0D - p, n - k);
	}

	// Returns the binomial cumulative distribution function
	public static double binomialCDF(double p, int n, int k) {
		if (p < 0.0D || p > 1.0D) throw new IllegalArgumentException("\np must lie between 0 and 1");
		if (k < 0 || n < 0) throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
		if (k > n) throw new IllegalArgumentException("\nk is greater than n");
		return Stat.regularisedBetaFunction(k, n - k + 1, p);
	}

	// Returns a binomial Coefficient as a double
	public static double binomialCoeff(int n, int k) {
		if (k < 0 || n < 0) throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
		if (k > n) throw new IllegalArgumentException("\nk is greater than n");
		return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n - k)));
	}

	// Returns a binomial mass probabilty function
	public static double binomialPDF(double p, int n, int k) {
		if (k < 0 || n < 0) throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
		if (k > n) throw new IllegalArgumentException("\nk is greater than n");
		return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n - k))) * Math.pow(p, k) * Math.pow(1.0D - p, n - k);
	}

	// Returns the binomial cumulative distribution function
	public static double binomialProb(double p, int n, int k) {
		if (p < 0.0D || p > 1.0D) throw new IllegalArgumentException("\np must lie between 0 and 1");
		if (k < 0 || n < 0) throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
		if (k > n) throw new IllegalArgumentException("\nk is greater than n");
		return Stat.regularisedBetaFunction(k, n - k + 1, p);
	}

	// Chi-Square Statistic
	public static double chiSquare(double[] observed, double[] expected, double[] variance) {
		int nObs = observed.length;
		int nExp = expected.length;
		int nVar = variance.length;
		if (nObs != nExp) throw new IllegalArgumentException("observed array length does not equal the expected array length");
		if (nObs != nVar) throw new IllegalArgumentException("observed array length does not equal the variance array length");
		double chi = 0.0D;
		for (int i = 0; i < nObs; i++) {
			chi += Fmath.square(observed[i] - expected[i]) / variance[i];
		}
		return chi;
	}

	// Chi-Square Cumulative Distribution Function
	// probability that an observed chi-square value for a correct model should be less than chiSquare
	// nu  =  the degrees of freedom
	public static double chiSquareCDF(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return Stat.incompleteGamma(nu / 2.0D, chiSquare / 2.0D);
	}

	// Chi-Square Statistic for Poisson distribution for frequency data
	// and Poisson distribution for each bin
	// double arguments
	public static double chiSquareFreq(double[] observedFreq, double[] expectedFreq) {
		int nObs = observedFreq.length;
		int nExp = expectedFreq.length;
		if (nObs != nExp) throw new IllegalArgumentException("observed array length does not equal the expected array length");
		double chi = 0.0D;
		for (int i = 0; i < nObs; i++) {
			chi += Fmath.square(observedFreq[i] - expectedFreq[i]) / expectedFreq[i];
		}
		return chi;
	}

	// Chi-Square Statistic for Poisson distribution for frequency data
	// and Poisson distribution for each bin
	// int arguments
	public static double chiSquareFreq(int[] observedFreq, int[] expectedFreq) {
		int nObs = observedFreq.length;
		int nExp = expectedFreq.length;
		if (nObs != nExp) throw new IllegalArgumentException("observed array length does not equal the expected array length");
		double[] observ = new double[nObs];
		double[] expect = new double[nObs];
		for (int i = 0; i < nObs; i++) {
			observ[i] = observedFreq[i];
			expect[i] = expectedFreq[i];
		}

		return chiSquareFreq(observ, expect);
	}

	// Chi-Square Inverse Cumulative Distribution Function
	public static double chiSquareInverseCDF(int nu, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");

		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = 0.0;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {

				// Create instance of the class holding the chiSquare cfd function
				ChiSquareFunct chi = new ChiSquareFunct();

				// set function variables
				chi.nu = nu;

				// required tolerance
				double tolerance = 1e-12;

				// lower bound
				double lowerBound = 0.0;

				// upper bound
				double upperBound = nu + 10.0 * Math.sqrt(2.0 * nu);

				// Create instance of RealRoot
				RealRoot realR = new RealRoot();

				// Set extension limits
				realR.noLowerBoundExtension();

				// Set tolerance
				realR.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR.resetNaNexceptionToTrue();
				realR.supressLimitReachedMessage();
				realR.supressNaNmessage();

				//  set function cfd variable
				chi.cfd = prob;

				// call root searching method
				icdf = realR.bisect(chi, lowerBound, upperBound);
			}
		}

		return icdf;
	}

	// Chi-Square Distribution Mean
	// nu  =  the degrees of freedom
	public static double chiSquareMean(int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return nu;
	}

	// Chi-Square Distribution Mean
	// nu  =  the degrees of freedom
	public static double chiSquareMode(int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		double mode = 0.0D;
		if (nu >= 2) mode = nu - 2.0D;
		return mode;
	}

	// Chi-Square Probability Density Function
	// nu  =  the degrees of freedom
	public static double chiSquarePDF(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		double dnu = nu;
		return Math.pow(0.5D, dnu / 2.0D) * Math.pow(chiSquare, dnu / 2.0D - 1.0D) * Math.exp(-chiSquare / 2.0D) / Stat.gammaFunction(dnu / 2.0D);
	}

	// retained for compatability
	public static double chiSquareProb(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return Stat.incompleteGamma(nu / 2.0D, chiSquare / 2.0D);
	}

	// Returns an array of Chi-Square random deviates - clock seed
	public static double[] chiSquareRand(int nu, int n) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		PsRandom psr = new PsRandom();
		return psr.chiSquareArray(nu, n);
	}

	// Returns an array of Chi-Square random deviates - user supplied seed
	public static double[] chiSquareRand(int nu, int n, long seed) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		PsRandom psr = new PsRandom(seed);
		return psr.chiSquareArray(nu, n);
	}

	// Chi-Square Distribution Standard Deviation
	// nu  =  the degrees of freedom
	public static double chiSquareStandardDeviation(int nu) {
		return chiSquareStandDev(nu);
	}

	// Chi-Square Distribution Standard Deviation
	// nu  =  the degrees of freedom
	public static double chiSquareStandDev(int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		double dnu = nu;
		return Math.sqrt(2.0D * dnu);
	}

	// Coefficient of variation of an array of BigDecimals
	public static double coefficientOfVariation(BigDecimal[] array) {
		return 100.0D * Stat.standardDeviation(array) / Math.abs(Stat.mean(array).doubleValue());
	}

	// Weighted coefficient of variation of an array of BigDecimals
	public static double coefficientOfVariation(BigDecimal[] array, BigDecimal[] weight) {
		int n = array.length;
		if (n != weight.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + weight.length + " are different");

		return 100.0D * Stat.standardDeviation(array, weight) / Math.abs(Stat.mean(array, weight).doubleValue());
	}

	// Coefficient of variation of an array of BigInteger
	public static double coefficientOfVariation(BigInteger[] array) {
		return 100.0D * Stat.standardDeviation(array) / Math.abs(Stat.mean(array).doubleValue());
	}

	// Weighted coefficient of variation of an array of BigInteger
	public static double coefficientOfVariation(BigInteger[] array, BigInteger[] weight) {
		int n = array.length;
		if (n != weight.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + weight.length + " are different");

		return 100.0D * Stat.standardDeviation(array, weight) / Math.abs(Stat.mean(array, weight).doubleValue());
	}

	// Coefficient of variation of an array of doubles
	public static double coefficientOfVariation(double[] array) {
		return 100.0D * Stat.standardDeviation(array) / Math.abs(Stat.mean(array));
	}

	// Weighted coefficient of variation of an array of doubles
	public static double coefficientOfVariation(double[] array, double[] weight) {
		int n = array.length;
		if (n != weight.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + weight.length + " are different");

		return 100.0D * Stat.standardDeviation(array, weight) / Math.abs(Stat.mean(array, weight));
	}

	// Coefficient of variation of an array of float
	public static float coefficientOfVariation(float[] array) {
		return 100.0F * Stat.standardDeviation(array) / Math.abs(Stat.mean(array));
	}

	// Weighted coefficient of variation of an array of float
	public static float coefficientOfVariation(float[] array, float[] weight) {
		int n = array.length;
		if (n != weight.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + weight.length + " are different");

		return 100.0F * Stat.standardDeviation(array, weight) / Math.abs(Stat.mean(array, weight));
	}

	// Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
	public static double complementaryRegularisedGammaFunction(double a, double x) {
		if (a < 0.0D || x < 0.0D) throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");

		boolean oldIgSupress = Stat.igSupress;
		Stat.igSupress = true;
		double igf = 1.0D;

		if (x != 0.0D) {
			if (x == 1.0D / 0.0D) {
				igf = 0.0D;
			} else {
				if (x < a + 1.0D) {
					// Series representation
					igf = 1.0 - Stat.incompleteGammaSer(a, x);
				} else {
					// Continued fraction representation
					igf = 1.0 - Stat.incompleteGammaFract(a, x);
				}
			}
		}
		if (igf > 1.0) igf = 1.0;
		Stat.igSupress = oldIgSupress;
		return igf;
	}

	// Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
	public static double complementaryRegularizedGammaFunction(double a, double x) {
		return complementaryRegularisedGammaFunction(a, x);
	}

	// Incomplete fraction summation used in the method regularisedBetaFunction
	// modified Lentz's method
	public static double contFract(double a, double b, double x) {
		double aplusb = a + b;
		double aplus1 = a + 1.0D;
		double aminus1 = a - 1.0D;
		double c = 1.0D;
		double d = 1.0D - aplusb * x / aplus1;
		if (Math.abs(d) < Stat.FPMIN) d = FPMIN;
		d = 1.0D / d;
		double h = d;
		double aa = 0.0D;
		double del = 0.0D;
		int i = 1, i2 = 0;
		boolean test = true;
		while (test) {
			i2 = 2 * i;
			aa = i * (b - i) * x / ((aminus1 + i2) * (a + i2));
			d = 1.0D + aa * d;
			if (Math.abs(d) < Stat.FPMIN) d = FPMIN;
			c = 1.0D + aa / c;
			if (Math.abs(c) < Stat.FPMIN) c = FPMIN;
			d = 1.0D / d;
			h *= d * c;
			aa = -(a + i) * (aplusb + i) * x / ((a + i2) * (aplus1 + i2));
			d = 1.0D + aa * d;
			if (Math.abs(d) < Stat.FPMIN) d = FPMIN;
			c = 1.0D + aa / c;
			if (Math.abs(c) < Stat.FPMIN) c = FPMIN;
			d = 1.0D / d;
			del = d * c;
			h *= del;
			i++;
			if (Math.abs(del - 1.0D) < Stat.cfTol) test = false;
			if (i > Stat.cfMaxIter) {
				test = false;
				System.out.println("Maximum number of iterations (" + Stat.cfMaxIter + ") exceeded in Stat.contFract in Stat.incompleteBeta");
			}
		}
		return h;

	}

	public static double[] convertBigWtoLittleW(BigDecimal[] bigW) {
		ArrayMaths am1 = new ArrayMaths(bigW);
		ArrayMaths am2 = am1.oneOverSqrt();
		return am2.getArray_as_double();
	}

	public static double[] convertBigWtoLittleW(BigInteger[] bigW) {
		ArrayMaths am1 = new ArrayMaths(bigW);
		ArrayMaths am2 = am1.oneOverSqrt();
		return am2.getArray_as_double();
	}

	public static Complex[] convertBigWtoLittleW(Complex[] bigW) {
		ArrayMaths am1 = new ArrayMaths(bigW);
		ArrayMaths am2 = am1.oneOverSqrt();
		return am2.getArray_as_Complex();
	}

	// CONVERSION OF WEIGHTING FACTORS
	// Converts weighting facors Wi to wi, i.e. to 1/sqrt(Wi)
	public static double[] convertBigWtoLittleW(double[] bigW) {
		ArrayMaths am1 = new ArrayMaths(bigW);
		ArrayMaths am2 = am1.oneOverSqrt();
		return am2.getArray_as_double();
	}

	public static float[] convertBigWtoLittleW(float[] bigW) {
		ArrayMaths am1 = new ArrayMaths(bigW);
		ArrayMaths am2 = am1.oneOverSqrt();
		return am2.getArray_as_float();
	}

	// Calculate correlation coefficient
	// x y data as double
	public static double corrCoeff(double[] xx, double[] yy) {

		int nData = xx.length;
		if (yy.length != nData) throw new IllegalArgumentException("array lengths must be equal");
		// means
		double mx = 0.0D;
		double my = 0.0D;
		for (int i = 0; i < nData; i++) {
			mx += xx[i];
			my += yy[i];
		}
		mx /= nData;
		my /= nData;

		// calculate sample variances
		double s2xx = 0.0D;
		double s2yy = 0.0D;
		double s2xy = 0.0D;
		for (int i = 0; i < nData; i++) {
			s2xx += Fmath.square(xx[i] - mx);
			s2yy += Fmath.square(yy[i] - my);
			s2xy += (xx[i] - mx) * (yy[i] - my);
		}

		// calculate corelation coefficient
		double sampleR = s2xy / Math.sqrt(s2xx * s2yy);

		// Check for rounding error
		if (sampleR > 1.0) {
			if (Fmath.isEqualWithinLimits(sampleR, 1.0, 0.001)) {
				sampleR = 1.0;
			}
		}
		if (sampleR < -1.0) {
			if (Fmath.isEqualWithinLimits(Math.abs(sampleR), 1.0, 0.001)) {
				sampleR = -1.0;
			}
		}

		return sampleR;
	}

	// Calculate weighted correlation coefficient
	// x y data and weights w as double
	public static double corrCoeff(double[] x, double[] y, double[] w) {
		int n = x.length;
		if (y.length != n) throw new IllegalArgumentException("x and y array lengths must be equal");
		if (w.length != n) throw new IllegalArgumentException("x and weight array lengths must be equal");

		double sxy = Stat.covariance(x, y, w);
		double sx = Stat.variance(x, w);
		double sy = Stat.variance(y, w);
		double sampleR = sxy / Math.sqrt(sx * sy);

		// Check for rounding error
		if (sampleR > 1.0) {
			if (Fmath.isEqualWithinLimits(sampleR, 1.0, 0.001)) {
				sampleR = 1.0;
			}
		}
		if (sampleR < -1.0) {
			if (Fmath.isEqualWithinLimits(Math.abs(sampleR), 1.0, 0.001)) {
				sampleR = -1.0;
			}
		}

		return sampleR;
	}

	// Calculate correlation coefficient
	// x y data as float
	public static float corrCoeff(float[] x, float[] y) {
		int nData = x.length;
		if (y.length != nData) throw new IllegalArgumentException("array lengths must be equal");
		int n = x.length;
		double[] xx = new double[n];
		double[] yy = new double[n];
		for (int i = 0; i < n; i++) {
			xx[i] = x[i];
			yy[i] = y[i];
		}
		return (float) Stat.corrCoeff(xx, yy);
	}

	// Calculate correlation coefficient
	// Binary data x and y
	// Input is the frequency matrix, F, elements, f(i,j)
	// f(0,0) - element00 - frequency of x and y both = 1
	// f(0,1) - element01 - frequency of x = 0 and y = 1
	// f(1,0) - element10 - frequency of x = 1 and y = 0
	// f(1,1) - element11 - frequency of x and y both = 0
	public static double corrCoeff(int element00, int element01, int element10, int element11) {
		double sampleR = (element00 * element11 - element01 * element10) / Math.sqrt(((element00 + element01) * (element10 + element11) * (element00 + element10) * (element01 + element11)));

		// Check for rounding error
		if (sampleR > 1.0) {
			if (Fmath.isEqualWithinLimits(sampleR, 1.0, 0.001)) {
				sampleR = 1.0;
			}
		}
		if (sampleR < -1.0) {
			if (Fmath.isEqualWithinLimits(Math.abs(sampleR), 1.0, 0.001)) {
				sampleR = -1.0;
			}
		}

		return sampleR;
	}

	// Calculate correlation coefficient
	// x y data as int
	public static double corrCoeff(int[] x, int[] y) {
		int n = x.length;
		if (y.length != n) throw new IllegalArgumentException("array lengths must be equal");

		double[] xx = new double[n];
		double[] yy = new double[n];
		for (int i = 0; i < n; i++) {
			xx[i] = x[i];
			yy[i] = y[i];
		}
		return Stat.corrCoeff(xx, yy);
	}

	// Calculate correlation coefficient
	// Binary data x and y
	// Input is the frequency matrix, F
	// F(0,0) - frequency of x and y both = 1
	// F(0,1) - frequency of x = 0 and y = 1
	// F(1,0) - frequency of x = 1 and y = 0
	// F(1,1) - frequency of x and y both = 0
	public static double corrCoeff(int[][] freqMatrix) {
		double element00 = freqMatrix[0][0];
		double element01 = freqMatrix[0][1];
		double element10 = freqMatrix[1][0];
		double element11 = freqMatrix[1][1];
		double sampleR = ((element00 * element11 - element01 * element10)) / Math.sqrt(((element00 + element01) * (element10 + element11) * (element00 + element10) * (element01 + element11)));

		// Check for rounding error
		if (sampleR > 1.0) {
			if (Fmath.isEqualWithinLimits(sampleR, 1.0, 0.001)) {
				sampleR = 1.0;
			}
		}
		if (sampleR < -1.0) {
			if (Fmath.isEqualWithinLimits(Math.abs(sampleR), 1.0, 0.001)) {
				sampleR = -1.0;
			}
		}

		return sampleR;
	}

	// Linear correlation coefficient single probablity
	public static double corrCoeffPdf(double rCoeff, int nu) {
		if (Math.abs(rCoeff) > 1.0D) throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

		double a = (nu - 2.0D) / 2.0D;
		double y = Math.pow((1.0D - Fmath.square(rCoeff)), a);

		double preterm = Math.exp(Stat.logGamma((nu + 1.0D) / 2.0) - Stat.logGamma(nu / 2.0D)) / Math.sqrt(Math.PI);

		return preterm * y;
	}

	// Linear correlation coefficient single probablity
	public static double corrCoeffPDF(double rCoeff, int nu) {
		if (Math.abs(rCoeff) > 1.0D) throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

		double a = (nu - 2.0D) / 2.0D;
		double y = Math.pow((1.0D - Fmath.square(rCoeff)), a);

		double preterm = Math.exp(Stat.logGamma((nu + 1.0D) / 2.0) - Stat.logGamma(nu / 2.0D)) / Math.sqrt(Math.PI);

		return preterm * y;
	}

	// Linear correlation coefficient cumulative probablity
	public static double corrCoeffProb(double rCoeff, int nu) {
		if (Math.abs(rCoeff) > 1.0D) throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

		// Create instances of the classes holding the function evaluation methods
		CorrCoeff cc = new CorrCoeff();

		// Assign values to constant in the function
		cc.a = (nu - 2.0D) / 2.0D;

		double integral = Integration.gaussQuad(cc, Math.abs(rCoeff), 1.0D, 128);

		double preterm = Math.exp(Stat.logGamma((nu + 1.0D) / 2.0) - Stat.logGamma(nu / 2.0D)) / Math.sqrt(Math.PI);

		return preterm * integral;
	}

	// Covariance of two 1D arrays of doubles, xx and yy
	public static double covariance(double[] xx, double[] yy) {
		int n = xx.length;
		if (n != yy.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;

		double sumx = 0.0D, meanx = 0.0D;
		double sumy = 0.0D, meany = 0.0D;
		for (int i = 0; i < n; i++) {
			sumx += xx[i];
			sumy += yy[i];
		}
		meanx = sumx / n;
		meany = sumy / n;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += (xx[i] - meanx) * (yy[i] - meany);
		}
		return sum / (denom);
	}

	// Weighted covariance of two 1D arrays of doubles, xx and yy with weights ww
	public static double covariance(double[] xx, double[] yy, double[] ww) {
		int n = xx.length;
		if (n != yy.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
		if (n != ww.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of weight array, " + yy.length + " are different");
		double nn = Stat.effectiveSampleNumber(ww);
		double nterm = nn / (nn - 1.0);
		if (Stat.nFactorOptionS) nterm = 1.0;
		double sumx = 0.0D, sumy = 0.0D, sumw = 0.0D, meanx = 0.0D, meany = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumx += xx[i] * weight[i];
			sumy += yy[i] * weight[i];
			sumw += weight[i];
		}
		meanx = sumx / sumw;
		meany = sumy / sumw;

		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += weight[i] * (xx[i] - meanx) * (yy[i] - meany);
		}
		return sum * nterm / sumw;
	}

	// Covariance of two 1D arrays of floats, xx and yy
	public static float covariance(float[] xx, float[] yy) {
		int n = xx.length;
		if (n != yy.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
		float denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;

		float sumx = 0.0F, meanx = 0.0F;
		float sumy = 0.0F, meany = 0.0F;
		for (int i = 0; i < n; i++) {
			sumx += xx[i];
			sumy += yy[i];
		}
		meanx = sumx / n;
		meany = sumy / n;
		float sum = 0.0F;
		for (int i = 0; i < n; i++) {
			sum += (xx[i] - meanx) * (yy[i] - meany);
		}
		return sum / (denom);
	}

	// Covariance of two 1D arrays of ints, xx and yy
	public static double covariance(int[] xx, int[] yy) {
		int n = xx.length;
		if (n != yy.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;

		double sumx = 0.0D, meanx = 0.0D;
		double sumy = 0.0D, meany = 0.0D;
		for (int i = 0; i < n; i++) {
			sumx += xx[i];
			sumy += yy[i];
		}
		meanx = sumx / n;
		meany = sumy / n;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += (xx[i] - meanx) * (yy[i] - meany);
		}
		return sum / (denom);
	}

	// Covariance of two 1D arrays of ints, xx and yy
	public static double covariance(long[] xx, long[] yy) {
		int n = xx.length;
		if (n != yy.length) throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;

		double sumx = 0.0D, meanx = 0.0D;
		double sumy = 0.0D, meany = 0.0D;
		for (int i = 0; i < n; i++) {
			sumx += xx[i];
			sumy += yy[i];
		}
		meanx = sumx / n;
		meany = sumy / n;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += (xx[i] - meanx) * (yy[i] - meany);
		}
		return sum / (denom);
	}

	// Guassian quadrature estimation of the complementary regularised incomplete gamma function
	private static double crigfGaussQuad(double a, double x) {
		double sum = 0.0;

		// set increment details
		double upper = 100.0 * a;
		double range = upper - x;
		double incr = 0;
		if (upper > x && range > 100) {
			incr = range / 1000;
		} else {
			upper = x + 100.0;
			range = 100.0;
			incr = 0.1;
		}
		int nIncr = (int) Math.round(range / incr);
		incr = range / nIncr;

		// Instantiate integration function
		CrigFunct f1 = new CrigFunct();
		f1.setA(a);
		f1.setB(Stat.logGammaFunction(a));

		// Instantiate Integration
		Integration intgn1 = new Integration(f1);
		double xx = x;
		double yy = x + incr;
		intgn1.setLimits(xx, yy);

		// Perform quadrature
		sum = intgn1.gaussQuad(64);
		for (int i = 1; i < nIncr; i++) {
			xx = yy;
			yy = xx + incr;
			intgn1.setLimits(xx, yy);
			sum += intgn1.gaussQuad(64);
		}
		return sum;
	}

	public static BigDecimal curtosis(BigDecimal[] aa) {
		return Stat.kurtosis(aa);
	}

	public static BigDecimal curtosis(BigInteger[] aa) {
		return Stat.kurtosis(aa);
	}

	public static double curtosis(double[] aa) {
		return Stat.kurtosis(aa);
	}

	public static float curtosis(float[] aa) {
		return Stat.kurtosis(aa);
	}

	public static double curtosis(int[] aa) {
		return Stat.kurtosis(aa);
	}

	public static double curtosis(long[] aa) {
		return Stat.kurtosis(aa);
	}

	public static BigDecimal curtosisExcess(BigDecimal[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static BigDecimal curtosisExcess(BigInteger[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double curtosisExcess(double[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static float curtosisExcess(float[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double curtosisExcess(int[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double curtosisExcess(long[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	// Calculation of the sample number (BigDecimal)
	public static BigDecimal effectiveSampleNumber(BigDecimal[] ww) {
		BigDecimal[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_BigDecimal();
		}
		int n = weight.length;

		BigDecimal nEff = new BigDecimal(new Integer(n).toString());
		if (Stat.nEffOptionS) {
			BigDecimal sumw2 = BigDecimal.ZERO;
			BigDecimal sum2w = BigDecimal.ZERO;
			for (int i = 0; i < n; i++) {
				sum2w = sum2w.add(weight[i]);
				sumw2 = sumw2.add(weight[i].multiply(weight[i]));
			}
			sum2w = sum2w.multiply(sum2w);
			nEff = sum2w.divide(sumw2, BigDecimal.ROUND_HALF_UP);
			sumw2 = null;
			sum2w = null;
			weight = null;
		}
		return nEff;
	}

	public static BigDecimal effectiveSampleNumber(BigInteger[] ww) {
		ArrayMaths am = new ArrayMaths(ww);
		BigDecimal[] www = am.array_as_BigDecimal();
		return Stat.effectiveSampleNumber(www);
	}

	// Calculation of the sample number (Complex)
	public static Complex effectiveSampleNumber(Complex[] ww) {
		Complex[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_Complex();
		}
		int n = weight.length;

		Complex nEff = new Complex(n, 0.0);
		if (Stat.nEffOptionS) {
			Complex sumw2 = Complex.zero();
			Complex sum2w = Complex.zero();
			for (int i = 0; i < n; i++) {
				sum2w = sum2w.plus(weight[i]);
				sumw2 = sumw2.plus(weight[i].times(weight[i]));
			}
			sum2w = sum2w.times(sum2w);
			nEff = sum2w.over(sumw2);
		}
		return nEff;
	}

	// Calculation of the effective sample number (double)
	public static double effectiveSampleNumber(double[] ww) {
		double[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array();
		}
		int n = weight.length;

		double nEff = n;
		if (Stat.nEffOptionS) {
			double sum2w = 0.0D;
			double sumw2 = 0.0D;
			for (int i = 0; i < n; i++) {
				sum2w += weight[i];
				sumw2 += weight[i] * weight[i];
			}
			sum2w *= sum2w;
			nEff = sum2w / sumw2;
		}
		return nEff;
	}

	// Calculation of the sample number (float)
	public static float effectiveSampleNumber(float[] ww) {
		float[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_float();
		}
		int n = weight.length;

		float nEff = n;
		if (Stat.nEffOptionS) {
			float sum2w = 0.0F;
			float sumw2 = 0.0F;
			for (int i = 0; i < n; i++) {
				sum2w += weight[i];
				sumw2 += weight[i] * weight[i];
			}
			sum2w *= sum2w;
			nEff = sum2w / sumw2;
		}
		return nEff;
	}

	// Calculation of the sample number (Complex  - Conjugate formula)
	public static double effectiveSampleNumberConjugateCalcn(Complex[] ww) {
		Complex[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_Complex();
		}
		int n = weight.length;

		double nEff = Double.NaN;
		if (Stat.nEffOptionS) {
			Complex sumw2 = Complex.zero();
			Complex sum2w = Complex.zero();
			for (int i = 0; i < n; i++) {
				sum2w = sum2w.plus(weight[i]);
				sumw2 = sumw2.plus(weight[i].times(weight[i].conjugate()));
			}
			sum2w = sum2w.times(sum2w.conjugate());
			nEff = sum2w.getReal() / sumw2.getReal();
		}
		return nEff;
	}

	// Engset equation
	// returns the maximum total traffic in Erlangs
	// blockingProbability:    probablility that a customer will be rejected due to lack of resources
	// totalResouces:   total number of resources in the system
	// numberOfSources: number of sources
	public static double engsetLoad(double blockingProbability, double totalResources, double numberOfSources) {
		if (totalResources < 1) throw new IllegalArgumentException("Total resources, " + totalResources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(totalResources)) throw new IllegalArgumentException("Total resources, " + totalResources + ", must be, arithmetically, an integer");
		if (numberOfSources < 1) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(numberOfSources)) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be, arithmetically, an integer");
		if (totalResources > numberOfSources - 1) throw new IllegalArgumentException("total resources, " + totalResources + ", must be less than or  equal to the number of sources minus one, " + (numberOfSources - 1));

		// Create instance of the class holding the Engset Load equation
		EngsetLoad eLfunc = new EngsetLoad();

		// Set instance variables
		eLfunc.blockingProbability = blockingProbability;
		eLfunc.totalResources = totalResources;
		eLfunc.numberOfSources = numberOfSources;

		// lower bound
		double lowerBound = 0.0D;
		// upper bound
		double upperBound = numberOfSources * 0.999999999;
		// required tolerance
		double tolerance = 1e-6;

		// Create instance of RealRoot
		RealRoot realR = new RealRoot();

		// Set tolerance
		realR.setTolerance(tolerance);

		// Set bounds limits
		realR.noLowerBoundExtension();
		realR.noUpperBoundExtension();

		// Supress error message if iteration limit reached
		realR.supressLimitReachedMessage();

		// call root searching method
		double root = realR.bisect(eLfunc, lowerBound, upperBound);

		return root;
	}

	public static double engsetLoad(double blockingProbability, int totalResources, int numberOfSources) {
		return engsetLoad(blockingProbability, (double) totalResources, (double) numberOfSources);
	}

	public static double engsetLoad(double blockingProbability, long totalResources, long numberOfSources) {
		return engsetLoad(blockingProbability, (double) totalResources, (double) numberOfSources);
	}

	// returns the probablility that a customer will be rejected due to lack of resources
	// offeredTraffic:  total offeredtraffic in Erlangs
	// totalResouces:   total number of resources in the system
	// numberOfSources: number of sources
	public static double engsetProbability(double offeredTraffic, double totalResources, double numberOfSources) {
		if (totalResources < 1) throw new IllegalArgumentException("Total resources, " + totalResources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(totalResources)) throw new IllegalArgumentException("Total resources, " + totalResources + ", must be, arithmetically, an integer");
		if (numberOfSources < 1) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(numberOfSources)) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be, arithmetically, an integer");
		if (totalResources > numberOfSources - 1) throw new IllegalArgumentException("total resources, " + totalResources + ", must be less than or  equal to the number of sources minus one, " + (numberOfSources - 1));
		if (offeredTraffic >= numberOfSources) throw new IllegalArgumentException("Number of sources, " + numberOfSources + ", must be greater than the offered traffic, " + offeredTraffic);

		double prob = 0.0D;
		if (totalResources == 0.0D) {
			prob = 1.0D;
		} else {
			if (offeredTraffic == 0.0D) {
				prob = 0.0D;
			} else {
				// Set boundaries to the probability
				double lowerBound = 0.0D;
				double upperBound = 1.0D;

				// Create instance of Engset Probability Function
				EngsetProb engProb = new EngsetProb();

				// Set function variables
				engProb.offeredTraffic = offeredTraffic;
				engProb.totalResources = totalResources;
				engProb.numberOfSources = numberOfSources;

				// Perform a root search
				RealRoot eprt = new RealRoot();

				// Supress error message if iteration limit reached
				eprt.supressLimitReachedMessage();

				prob = eprt.bisect(engProb, lowerBound, upperBound);
			}
		}
		return prob;
	}

	public static double engsetProbability(double offeredTraffic, int totalResources, int numberOfSources) {
		return engsetProbability(offeredTraffic, (double) totalResources, (double) numberOfSources);
	}

	public static double engsetProbability(double offeredTraffic, long totalResources, long numberOfSources) {
		return engsetProbability(offeredTraffic, (double) totalResources, (double) numberOfSources);
	}

	// Engset equation
	// returns the resources bracketing a blocking probability for a given total traffic and number of sources
	// blockingProbability:    probablility that a customer will be rejected due to lack of resources
	// totalResouces:   total number of resources in the system
	// numberOfSources: number of sources
	public static double[] engsetResources(double blockingProbability, double offeredTraffic, double numberOfSources) {
		if (numberOfSources < 1) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(numberOfSources)) throw new IllegalArgumentException("number of sources, " + numberOfSources + ", must be, arithmetically, an integer");

		double[] ret = new double[9];
		long counter = 1;
		double lastProb = Double.NaN;
		double prob = Double.NaN;
		boolean test = true;
		while (test) {
			prob = Stat.engsetProbability(offeredTraffic, counter, numberOfSources);
			if (prob <= blockingProbability) {

				ret[0] = counter;
				ret[1] = prob;
				ret[2] = Stat.engsetLoad(blockingProbability, counter, numberOfSources);
				ret[3] = (counter - 1);
				ret[4] = lastProb;
				ret[5] = Stat.engsetLoad(blockingProbability, (counter - 1), numberOfSources);
				ret[6] = blockingProbability;
				ret[7] = offeredTraffic;
				ret[8] = numberOfSources;
				test = false;
			} else {
				lastProb = prob;
				counter++;
				if (counter > (long) numberOfSources - 1L) {
					System.out.println("Method engsetResources: no solution found below the (sources-1), " + (numberOfSources - 1));
					for (int i = 0; i < 8; i++)
						ret[i] = Double.NaN;
					test = false;
				}
			}
		}
		return ret;
	}

	public static double[] engsetResources(double blockingProbability, double totalTraffic, int numberOfSources) {
		return Stat.engsetResources(blockingProbability, totalTraffic, (double) numberOfSources);
	}

	public static double[] engsetResources(double blockingProbability, double totalTraffic, long numberOfSources) {
		return Stat.engsetResources(blockingProbability, totalTraffic, (double) numberOfSources);
	}

	// Engset equation
	// returns the number of sources bracketing a blocking probability for a given total traffic and given resources
	// blockingProbability:    probablility that a customer will be rejected due to lack of resources
	// totalResouces:   total number of resources in the system
	// numberOfSources: number of sources
	public static double[] engsetSources(double blockingProbability, double offeredTraffic, double resources) {
		if (resources < 1) throw new IllegalArgumentException("resources, " + resources + ", must be an integer greater than or equal to 1");
		if (!Fmath.isInteger(resources)) throw new IllegalArgumentException("resources, " + resources + ", must be, arithmetically, an integer");

		double[] ret = new double[9];
		long counter = (long) resources + 1L;
		double lastProb = Double.NaN;
		double prob = Double.NaN;
		boolean test = true;
		while (test) {
			prob = Stat.engsetProbability(offeredTraffic, resources, counter);
			if (prob >= blockingProbability) {

				ret[0] = counter;
				ret[1] = prob;
				ret[2] = Stat.engsetLoad(blockingProbability, resources, counter);
				ret[3] = (counter - 1L);
				ret[4] = lastProb;
				if ((counter - 1L) >= (long) (resources + 1L)) {
					ret[5] = Stat.engsetLoad(blockingProbability, resources, (counter - 1L));
				} else {
					ret[5] = Double.NaN;
				}
				ret[6] = blockingProbability;
				ret[7] = offeredTraffic;
				ret[8] = resources;
				test = false;
			} else {
				lastProb = prob;
				counter++;
				if (counter >= Long.MAX_VALUE) {
					System.out.println("Method engsetResources: no solution found below " + Long.MAX_VALUE + "sources");
					for (int i = 0; i < 8; i++)
						ret[i] = Double.NaN;
					test = false;
				}
			}
		}
		return ret;
	}

	public static double[] engsetSources(double blockingProbability, double totalTraffic, int resources) {
		return Stat.engsetSources(blockingProbability, totalTraffic, (double) resources);
	}

	public static double[] engsetSources(double blockingProbability, double totalTraffic, long resources) {
		return Stat.engsetSources(blockingProbability, totalTraffic, (double) resources);
	}

	// Error Function
	public static double erf(double x) {
		double erf = 0.0D;
		if (x != 0.0) {
			if (x == 1.0D / 0.0D) {
				erf = 1.0D;
			} else {
				if (x >= 0) {
					erf = Stat.incompleteGamma(0.5, x * x);
				} else {
					erf = -Stat.incompleteGamma(0.5, x * x);
				}
			}
		}
		return erf;
	}

	// Complementary Error Function
	public static double erfc(double x) {
		double erfc = 1.0D;
		if (x != 0.0) {
			if (x == 1.0D / 0.0D) {
				erfc = 0.0D;
			} else {
				if (x >= 0) {
					erfc = 1.0D - Stat.incompleteGamma(0.5, x * x);
				} else {
					erfc = 1.0D + Stat.incompleteGamma(0.5, x * x);
				}
			}
		}
		return erfc;
	}

	// Erlang B equation
	// returns the maximum total traffic in Erlangs
	// blockingProbability:    probablility that a customer will be rejected due to lack of resources
	// totalResouces:   total number of resources in the system
	public static double erlangBload(double blockingProbability, double totalResources) {

		// Create instance of the class holding the Erlang B equation
		ErlangBfunct eBfunc = new ErlangBfunct();

		// Set instance variables
		eBfunc.blockingProbability = blockingProbability;
		eBfunc.totalResources = totalResources;

		// lower bound
		double lowerBound = 0.0D;
		// upper bound   // arbitrary - may be extended by bisects automatic extension
		double upperBound = 20.0;
		// required tolerance
		double tolerance = 1e-6;

		// Create instance of RealRoot
		RealRoot realR = new RealRoot();

		// Set tolerance
		realR.setTolerance(tolerance);

		// Set bounds limits
		realR.noLowerBoundExtension();

		// Supress error message if iteration limit reached
		realR.supressLimitReachedMessage();

		// call root searching method
		double root = realR.bisect(eBfunc, lowerBound, upperBound);

		return root;
	}

	public static double erlangBload(double blockingProbability, int totalResources) {
		return erlangBload(blockingProbability, (double) totalResources);
	}

	public static double erlangBload(double blockingProbability, long totalResources) {
		return erlangBload(blockingProbability, (double) totalResources);
	}

	// Erlang B equation
	// Integer or non-integer number of servers
	// returns the probablility that a customer will be rejected due to lack of resources
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangBprobability(double totalTraffic, double totalResources) {
		if (totalTraffic < 0) throw new IllegalArgumentException("Total traffic, " + totalTraffic + ", must be greater than or equal to zero");
		if (totalResources < 0) throw new IllegalArgumentException("Total resources, " + totalResources + ", must be greater than or equal to zero");

		double prob = 0.0D;
		if (totalResources == 0.0D) {
			prob = 1.0;
		} else {
			if (totalTraffic == 0.0D) {
				prob = 0.0;
			} else {
				if (Fmath.isInteger(totalResources)) {
					double iCount = 1.0D;
					prob = 1.0D;
					double hold = 0.0D;
					while (iCount <= totalResources) {
						hold = prob * totalTraffic;
						prob = hold / (iCount + hold);
						iCount += 1.0D;
					}
				} else {
					prob = Stat.erlangBprobabilityNIR(totalTraffic, totalResources);
				}
			}
		}
		return prob;
	}

	// Erlang B equation
	// Integer number of servers
	// returns the probablility that a customer will be rejected due to lack of resources
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangBprobability(double totalTraffic, int totalResources) {
		return erlangBprobability(totalTraffic, (double) totalResources);
	}

	// Erlang B equation
	// Integer number of servers
	// returns the probablility that a customer will be rejected due to lack of resources
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangBprobability(double totalTraffic, long totalResources) {
		return erlangBprobability(totalTraffic, (double) totalResources);
	}

	// Erlang B equation
	// Non-Integer number of servers
	// returns the probablility that a customer will be rejected due to lack of resources
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangBprobabilityNIR(double totalTraffic, double totalResources) {

		double prob = 0.0D; // blocking probability

		// numerator
		double lognumer = totalResources * Math.log(totalTraffic) - totalTraffic;
		// denominator (incomplete Gamma Function)
		double oneplustr = 1.0D + totalResources;
		double crigf = Stat.complementaryRegularisedGammaFunction(oneplustr, totalTraffic);
		if (crigf != crigf || crigf == 0.0) {
			int low = (int) Math.floor(totalResources) - 2;
			if (low < 0) low = 0;
			int ni = 6;
			int[] tr = new int[ni];
			double[] trd = new double[ni];
			double[] pp = new double[ni];
			for (int i = 0; i < ni; i++) {
				tr[i] = low + i;
				trd[i] = tr[i];
				pp[i] = Stat.erlangBprobability(totalTraffic, tr[i]);
			}
			CubicSpline cs = new CubicSpline(trd, pp);
			prob = cs.interpolate(totalResources);
		} else {
			double logdenom = Math.log(crigf) + Stat.logGammaFunction(oneplustr);
			prob = Math.exp(lognumer - logdenom);
		}
		return prob;
	}

	// Non-Integer number of servers
	// returns the probablility that a customer will be rejected due to lack of resources
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	// Retained for compatibility
	public static double erlangBprobabilityNonIntRes(double totalTraffic, double totalResources) {
		return Stat.erlangBprobability(totalTraffic, totalResources);
	}

	// Erlang B equation
	// returns the resources bracketing a blocking probability for a given total traffic
	// blockingProbability:    probablility that a customer will be rejected due to lack of resources
	// totalResouces:   total number of resources in the system
	public static double[] erlangBresources(double blockingProbability, double totalTraffic) {

		double[] ret = new double[8];
		long counter = 1;
		double lastProb = Double.NaN;
		double prob = Double.NaN;
		boolean test = true;
		while (test) {
			prob = Stat.erlangBprobability(totalTraffic, counter);
			if (prob <= blockingProbability) {
				ret[0] = counter;
				ret[1] = prob;
				ret[2] = Stat.erlangBload(blockingProbability, counter);
				ret[3] = (counter - 1);
				ret[4] = lastProb;
				ret[5] = Stat.erlangBload(blockingProbability, counter - 1);
				ret[6] = blockingProbability;
				ret[7] = totalTraffic;
				test = false;
			} else {
				lastProb = prob;
				counter++;
				if (counter == Integer.MAX_VALUE) {
					System.out.println("Method erlangBresources: no solution found below " + Long.MAX_VALUE + "resources");
					for (int i = 0; i < 8; i++)
						ret[i] = Double.NaN;
					test = false;
				}
			}
		}
		return ret;
	}

	public static double erlangCDF(double lambda, double kay, double upperLimit) {
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		return gammaCDF(0.0D, 1.0D / lambda, kay, upperLimit);
	}

	// Erlang distribution
	// Cumulative distribution function
	public static double erlangCDF(double lambda, int kay, double upperLimit) {
		return gammaCDF(0.0D, 1.0D / lambda, kay, upperLimit);
	}

	public static double erlangCDF(double lambda, long kay, double upperLimit) {
		return gammaCDF(0.0D, 1.0D / lambda, kay, upperLimit);
	}

	// Erlang C equation
	// returns the maximum total traffic in Erlangs
	// nonZeroDelayProbability:    probablility that a customer will receive a non-zero delay in obtaining obtaining a resource
	// totalResouces:   total number of resources in the system
	public static double erlangCload(double nonZeroDelayProbability, double totalResources) {

		// Create instance of the class holding the Erlang C equation
		ErlangCfunct eCfunc = new ErlangCfunct();

		// Set instance variables
		eCfunc.nonZeroDelayProbability = nonZeroDelayProbability;
		eCfunc.totalResources = totalResources;

		// lower bound
		double lowerBound = 0.0D;
		// upper bound
		double upperBound = 10.0D;
		// required tolerance
		double tolerance = 1e-6;

		// Create instance of RealRoot
		RealRoot realR = new RealRoot();

		// Set tolerance
		realR.setTolerance(tolerance);

		// Supress error message if iteration limit reached
		realR.supressLimitReachedMessage();

		// Set bounds limits
		realR.noLowerBoundExtension();

		// call root searching method
		double root = realR.bisect(eCfunc, lowerBound, upperBound);

		return root;
	}

	public static double erlangCload(double nonZeroDelayProbability, int totalResources) {
		return erlangCload(nonZeroDelayProbability, (double) totalResources);
	}

	public static double erlangCload(double nonZeroDelayProbability, long totalResources) {
		return erlangCload(nonZeroDelayProbability, (double) totalResources);
	}

	// Erlang C equation
	// returns the probablility that a customer will receive a non-zero delay in obtaining obtaining a resource
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangCprobability(double totalTraffic, double totalResources) {

		double prob = 0.0D;
		if (totalTraffic > 0.0D) {

			double probB = Stat.erlangBprobability(totalTraffic, totalResources);
			prob = 1.0 + (1.0 / probB - 1.0) * (totalResources - totalTraffic) / totalResources;
			prob = 1.0 / prob;

		}
		return prob;
	}

	public static double erlangCprobability(double totalTraffic, int totalResources) {
		return erlangCprobability(totalTraffic, (double) totalResources);
	}

	public static double erlangCprobability(double totalTraffic, long totalResources) {
		return erlangCprobability(totalTraffic, (double) totalResources);
	}

	// Erlang C equation
	// returns the resources bracketing a non-zer delay probability for a given total traffic
	// nonZeroDelayProbability:    probablility that a customer will receive a non-zero delay in obtaining obtaining a resource
	// totalResouces:   total number of resources in the system
	public static double[] erlangCresources(double nonZeroDelayProbability, double totalTraffic) {

		double[] ret = new double[8];
		long counter = 1;
		double lastProb = Double.NaN;
		double prob = Double.NaN;
		boolean test = true;
		while (test) {
			prob = Stat.erlangCprobability(totalTraffic, counter);
			if (prob <= nonZeroDelayProbability) {
				ret[0] = counter;
				ret[1] = prob;
				ret[2] = Stat.erlangCload(nonZeroDelayProbability, counter);
				ret[3] = (counter - 1);
				ret[4] = lastProb;
				ret[5] = Stat.erlangCload(nonZeroDelayProbability, counter - 1);
				ret[6] = nonZeroDelayProbability;
				ret[7] = totalTraffic;
				test = false;
			} else {
				lastProb = prob;
				counter++;
				if (counter == Integer.MAX_VALUE) {
					System.out.println("Method erlangCresources: no solution found below " + Long.MAX_VALUE + "resources");
					for (int i = 0; i < 8; i++)
						ret[i] = Double.NaN;
					test = false;
				}
			}
		}
		return ret;
	}

	public static double erlangMean(double lambda, double kay) {
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return kay / lambda;
	}

	// Erlang distribution
	// mean
	public static double erlangMean(double lambda, int kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return kay / lambda;
	}

	public static double erlangMean(double lambda, long kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return kay / lambda;
	}

	public static double erlangMode(double lambda, double kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		double mode = Double.NaN;
		if (kay >= 1) mode = (kay - 1.0D) / lambda;
		return mode;
	}

	// erlang distribution
	// mode
	public static double erlangMode(double lambda, int kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		double mode = Double.NaN;
		if (kay >= 1) mode = (kay - 1.0D) / lambda;
		return mode;
	}

	public static double erlangMode(double lambda, long kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		double mode = Double.NaN;
		if (kay >= 1) mode = (kay - 1.0D) / lambda;
		return mode;
	}

	// returns the probablility that m resources (connections) are busy
	// totalTraffic:    total traffic in Erlangs
	// totalResouces:   total number of resources in the system
	public static double erlangMprobability(double totalTraffic, double totalResources, double em) {
		double prob = 0.0D;
		if (totalTraffic > 0.0D) {

			double numer = totalResources * Math.log(em) - Fmath.logFactorial(em);
			double denom = 1.0D;
			double lastTerm = 1.0D;
			for (int i = 1; i <= totalResources; i++) {
				lastTerm = lastTerm * totalTraffic / i;
				denom += lastTerm;
			}
			denom = Math.log(denom);
			prob = numer - denom;
			prob = Math.exp(prob);
		}
		return prob;
	}

	public static double erlangMprobability(double totalTraffic, int totalResources, int em) {
		return erlangMprobability(totalTraffic, (double) totalResources, (double) em);
	}

	public static double erlangMprobability(double totalTraffic, long totalResources, long em) {
		return erlangMprobability(totalTraffic, (double) totalResources, (double) em);
	}

	public static double erlangPDF(double lambda, double kay, double x) {
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");

		return gammaPDF(0.0D, 1.0D / lambda, kay, x);
	}

	// Erlang distribution
	// probablity density function
	public static double erlangPDF(double lambda, int kay, double x) {
		return gammaPDF(0.0D, 1.0D / lambda, kay, x);
	}

	public static double erlangPDF(double lambda, long kay, double x) {
		return gammaPDF(0.0D, 1.0D / lambda, kay, x);
	}

	public static double[] erlangRand(double lambda, double kay, int n) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		return gammaRand(0.0D, 1.0D / lambda, kay, n);
	}

	public static double[] erlangRand(double lambda, double kay, int n, long seed) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		return gammaRand(0.0D, 1.0D / lambda, kay, n, seed);
	}

	// Returns an array of Erlang random deviates - clock seed
	public static double[] erlangRand(double lambda, int kay, int n) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return gammaRand(0.0D, 1.0D / lambda, kay, n);
	}

	// Returns an array of Erlang random deviates - user supplied seed
	public static double[] erlangRand(double lambda, int kay, int n, long seed) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return gammaRand(0.0D, 1.0D / lambda, kay, n, seed);
	}

	public static double[] erlangRand(double lambda, long kay, int n) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return gammaRand(0.0D, 1.0D / lambda, kay, n);
	}

	public static double[] erlangRand(double lambda, long kay, int n, long seed) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return gammaRand(0.0D, 1.0D / lambda, kay, n, seed);
	}

	// standard deviation
	public static double erlangStandardDeviation(double lambda, double kay) {
		return erlangStandDev(lambda, kay);
	}

	// Erlang distribution
	// standard deviation
	public static double erlangStandardDeviation(double lambda, int kay) {
		return erlangStandDev(lambda, kay);
	}

	// standard deviation
	public static double erlangStandardDeviation(double lambda, long kay) {
		return erlangStandDev(lambda, kay);
	}

	public static double erlangStandDev(double lambda, double kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		if (kay - Math.round(kay) != 0.0D) throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
		return Math.sqrt(kay) / lambda;
	}

	// standard deviation
	public static double erlangStandDev(double lambda, int kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return Math.sqrt(kay) / lambda;
	}

	public static double erlangStandDev(double lambda, long kay) {
		if (kay < 1) throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
		return Math.sqrt(kay) / lambda;
	}

	public static BigDecimal excessCurtosis(BigDecimal[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static BigDecimal excessCurtosis(BigInteger[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessCurtosis(double[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static float excessCurtosis(float[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessCurtosis(int[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessCurtosis(long[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static BigDecimal excessKurtosis(BigDecimal[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static BigDecimal excessKurtosis(BigInteger[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessKurtosis(double[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static float excessKurtosis(float[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessKurtosis(int[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	public static double excessKurtosis(long[] aa) {
		return Stat.kurtosisExcess(aa);
	}

	// Exponential probability density function
	public static double exponential(double mu, double sigma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = Math.exp(-arg) / sigma;
		}
		return y;
	}

	// Exponential cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double exponentialCDF(double mu, double sigma, double upperlimit) {
		double arg = (upperlimit - mu) / sigma;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-arg);
		return y;
	}

	// Exponential cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double exponentialCDF(double mu, double sigma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-arg1);
		if (arg2 >= 0.0D) term2 = -Math.exp(-arg2);
		return term2 - term1;
	}

	// Exponential Inverse Cumulative Density Function
	public static double exponentialInverseCDF(double mu, double sigma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = mu;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu - sigma * (Math.log(1.0 - prob));
			}
		}

		return icdf;
	}

	// Exponential mean
	public static double exponentialMean(double mu, double sigma) {
		return mu + sigma;
	}

	// Exponential median
	public static double exponentialMedian(double mu, double sigma) {
		return mu + sigma * Math.log(2.0D);
	}

	// Exponential mode
	public static double exponentialMode(double mu) {
		return mu;
	}

	// Exponential order statistic medians (n points)
	public static double[] exponentialOrderStatisticMedians(double mu, double sigma, int n) {
		double[] eosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			eosm[i] = Stat.inverseExponentialCDF(mu, sigma, uosm[i]);
		}
		return eosm;
	}

	// Exponential probability density function
	public static double exponentialPDF(double mu, double sigma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = Math.exp(-arg) / sigma;
		}
		return y;
	}

	// Exponential cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double exponentialProb(double mu, double sigma, double upperlimit) {
		double arg = (upperlimit - mu) / sigma;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-arg);
		return y;
	}

	// Exponential cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double exponentialProb(double mu, double sigma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-arg1);
		if (arg2 >= 0.0D) term2 = -Math.exp(-arg2);
		return term2 - term1;
	}

	// Returns an array of Exponential random deviates - clock seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] exponentialRand(double mu, double sigma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = mu - Math.log(1.0D - rr.nextDouble()) * sigma;
		}
		return ran;
	}

	// Returns an array of Exponential random deviates - user supplied seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] exponentialRand(double mu, double sigma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = mu - Math.log(1.0D - rr.nextDouble()) * sigma;
		}
		return ran;
	}

	// Exponential standard deviation
	public static double exponentialStandardDeviation(double sigma) {
		return sigma;
	}

	// Exponential standard deviation
	public static double exponentialStandDev(double sigma) {
		return sigma;
	}

	// factorial of n
	// Argument is of type BigDecimal but must be, numerically, an integer
	public static BigDecimal factorial(BigDecimal n) {
		if (n.compareTo(BigDecimal.ZERO) == -1 || !Fmath.isInteger(n)) throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		BigDecimal one = BigDecimal.ONE;
		BigDecimal f = one;
		BigDecimal iCount = new BigDecimal(2.0D);
		while (iCount.compareTo(n) != 1) {
			f = f.multiply(iCount);
			iCount = iCount.add(one);
		}
		one = null;
		iCount = null;
		return f;
	}

	// factorial of n
	// Argument is of type BigInteger
	public static BigInteger factorial(BigInteger n) {
		if (n.compareTo(BigInteger.ZERO) == -1) throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		BigInteger one = BigInteger.ONE;
		BigInteger f = one;
		BigInteger iCount = new BigInteger("2");
		while (iCount.compareTo(n) != 1) {
			f = f.multiply(iCount);
			iCount = iCount.add(one);
		}
		one = null;
		iCount = null;
		return f;
	}

	// factorial of n
	// Argument is of type double but must be, numerically, an integer
	// factorial returned as double but is, numerically, should be an integer
	// numerical rounding may makes this an approximation after n = 21
	public static double factorial(double n) {
		if (n < 0 || (n - Math.floor(n)) != 0) throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		double f = 1.0D;
		double iCount = 2.0D;
		while (iCount <= n) {
			f *= iCount;
			iCount += 1.0D;
		}
		return f;
	}

	// factorial of n
	// argument and return are integer, therefore limited to 0<=n<=12
	// see below for long and double arguments
	public static int factorial(int n) {
		if (n < 0) throw new IllegalArgumentException("n must be a positive integer");
		if (n > 12) throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
		int f = 1;
		for (int i = 2; i <= n; i++)
			f *= i;
		return f;
	}

	// factorial of n
	// argument and return are long, therefore limited to 0<=n<=20
	// see below for double argument
	public static long factorial(long n) {
		if (n < 0) throw new IllegalArgumentException("n must be a positive integer");
		if (n > 20) throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
		long f = 1;
		long iCount = 2L;
		while (iCount <= n) {
			f *= iCount;
			iCount += 1L;
		}
		return f;
	}

	// Returns the F-distribution probabilty for degrees of freedom df1, df2
	// numerator and denominator variances provided
	public static double fCompCDF(double var1, int df1, double var2, int df2) {
		if (df1 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu1, " + df1 + ", must be greater than zero");
		if (df2 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu2, " + df2 + ", must be greater than zero");
		if (var1 < 0) throw new IllegalArgumentException("the variance, var1" + var1 + ", must be greater than or equal to zero");
		if (var1 <= 0) throw new IllegalArgumentException("the variance, var2" + var2 + ", must be greater than zero");
		double fValue = var1 / var2;
		double ddf1 = df1;
		double ddf2 = df2;
		double x = ddf2 / (ddf2 + ddf1 * fValue);
		return Stat.regularisedBetaFunction(df2 / 2.0D, df1 / 2.0D, x);
	}

	// Returns the F-distribution probabilty for degrees of freedom df1, df2
	// F ratio provided
	public static double fCompCDF(double fValue, int df1, int df2) {
		if (df1 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu1, " + df1 + ", must be greater than zero");
		if (df2 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu2, " + df2 + ", must be greater than zero");
		if (fValue < 0) throw new IllegalArgumentException("the F-ratio, " + fValue + ", must be greater than or equal to zero");
		double ddf1 = df1;
		double ddf2 = df2;
		double x = ddf2 / (ddf2 + ddf1 * fValue);
		return Stat.regularisedBetaFunction(df2 / 2.0D, df1 / 2.0D, x);
	}

	// F-distribution Inverse Cumulative Distribution Function
	public static double fDistributionInverseCDF(int nu1, int nu2, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");

		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = 0.0;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {

				// Create instance of the class holding the F-distribution cfd function
				FdistribtionFunct fdistn = new FdistribtionFunct();

				// set function variables
				fdistn.nu1 = nu1;
				fdistn.nu2 = nu2;

				// required tolerance
				double tolerance = 1e-12;

				// lower bound
				double lowerBound = 0.0;

				// upper bound
				double upperBound = 2.0;

				// Create instance of RealRoot
				RealRoot realR = new RealRoot();

				// Set extension limits
				realR.noLowerBoundExtension();

				// Set tolerance
				realR.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR.resetNaNexceptionToTrue();
				realR.supressLimitReachedMessage();
				realR.supressNaNmessage();

				//  set function cfd  variable
				fdistn.cfd = prob;

				// call root searching method
				icdf = realR.bisect(fdistn, lowerBound, upperBound);
			}
		}
		return icdf;
	}

	// F-distribution order statistic medians (n points)
	public static double[] fDistributionOrderStatisticMedians(int nu1, int nu2, int n) {
		double[] gosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			gosm[i] = Stat.fDistributionInverseCDF(nu1, nu2, uosm[i]);
		}
		Stat st = new Stat(gosm);
		double mean = st.mean();
		double sigma = st.standardDeviation();
		gosm = Stat.scale(gosm, mean, sigma);
		return gosm;
	}

	// Fit a data set to one, several or all of the above distributions (static)
	public static void fitOneOrSeveralDistributions(double[] array) {
		Regression.fitOneOrSeveralDistributions(array);
	}

	// Returns an array of F-distribution random deviates - clock seed
	public static double[] fRand(int nu1, int nu2, int n) {
		if (nu1 <= 0) throw new IllegalArgumentException("The degrees of freedom [nu1], " + nu1 + ", must be greater than zero");
		if (nu2 <= 0) throw new IllegalArgumentException("The degrees of freedom [nu2], " + nu2 + ", must be greater than zero");
		PsRandom psr = new PsRandom();
		return psr.fArray(nu1, nu2, n);
	}

	// Returns an array of F-distribution random deviates - user supplied seed
	public static double[] fRand(int nu1, int nu2, int n, long seed) {
		if (nu1 <= 0) throw new IllegalArgumentException("The degrees of freedom [nu1], " + nu1 + ", must be greater than zero");
		if (nu2 <= 0) throw new IllegalArgumentException("The degrees of freedom [nu2], " + nu2 + ", must be greater than zero");
		PsRandom psr = new PsRandom(seed);
		return psr.fArray(nu1, nu2, n);
	}

	// Frechet probability density function
	public static double frechet(double mu, double sigma, double gamma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (gamma / sigma) * Math.pow(arg, -gamma - 1.0D) * Math.exp(-Math.pow(arg, -gamma));
		}
		return y;
	}

	// Frechet cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double frechetCDF(double mu, double sigma, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = Math.exp(-Math.pow(arg1, -gamma));
		if (arg2 >= 0.0D) term2 = Math.exp(-Math.pow(arg2, -gamma));
		return term2 - term1;
	}

	// Frechet Inverse Cumulative Density Function
	// Standard
	public static double frechetInverseCDF(double gamma, double prob) {
		return frechetInverseCDF(0.0D, 1.0D, gamma, prob);
	}

	// Frechet Inverse Cumulative Density Function
	// Two parameter
	public static double frechetInverseCDF(double sigma, double gamma, double prob) {
		return frechetInverseCDF(0.0D, sigma, gamma, prob);
	}

	// Frechet Inverse Cumulative Density Function
	// Three parameter
	public static double frechetInverseCDF(double mu, double sigma, double gamma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu + sigma * Math.pow(Math.log(1.0 / prob), -1.0 / gamma);
			}
		}

		return icdf;
	}

	// Frechet mean
	public static double frechetMean(double mu, double sigma, double gamma) {
		double y = Double.NaN;
		if (gamma > 1.0D) {
			y = mu + sigma * Stat.gamma(1.0D - 1.0D / gamma);
		}
		return y;
	}

	// Frechet mode
	public static double frechetMode(double mu, double sigma, double gamma) {
		return mu + sigma * Math.pow(gamma / (1.0D + gamma), 1.0D / gamma);
	}

	// Frechet order statistic medians (n points)
	// Three parameters
	public static double[] frechetOrderStatisticMedians(double mu, double sigma, double gamma, int n) {
		double[] fosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			fosm[i] = Stat.frechetInverseCDF(mu, sigma, gamma, uosm[i]);
		}
		return fosm;
	}

	// Frechet order statistic medians (n points)
	// Two parameters
	public static double[] frechetOrderStatisticMedians(double sigma, double gamma, int n) {
		return frechetOrderStatisticMedians(0.0D, sigma, gamma, n);
	}

	// Frechet order statistic medians (n points)
	// Standard
	public static double[] frechetOrderStatisticMedians(double gamma, int n) {
		return frechetOrderStatisticMedians(0.0D, 1.0D, gamma, n);
	}

	// Frechet probability density function
	public static double frechetPDF(double mu, double sigma, double gamma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (gamma / sigma) * Math.pow(arg, -gamma - 1.0D) * Math.exp(-Math.pow(arg, -gamma));
		}
		return y;
	}

	// Frechet cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double frechetProb(double mu, double sigma, double gamma, double upperlimit) {
		double arg = (upperlimit - mu) / sigma;
		double y = 0.0D;
		if (arg > 0.0D) y = Math.exp(-Math.pow(arg, -gamma));
		return y;
	}

	// Frechet cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double frechetProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = Math.exp(-Math.pow(arg1, -gamma));
		if (arg2 >= 0.0D) term2 = Math.exp(-Math.pow(arg2, -gamma));
		return term2 - term1;
	}

	// Returns an array of Frechet (Type II EVD) random deviates - clock seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] frechetRand(double mu, double sigma, double gamma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow((1.0D / (Math.log(1.0D / rr.nextDouble()))), 1.0D / gamma) * sigma + mu;
		}
		return ran;
	}

	// Returns an array of Frechet (Type II EVD) random deviates - user supplied seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] frechetRand(double mu, double sigma, double gamma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow((1.0D / (Math.log(1.0D / rr.nextDouble()))), 1.0D / gamma) * sigma + mu;
		}
		return ran;
	}

	// Frechet standard deviation
	public static double frechetStandardDeviation(double sigma, double gamma) {
		return frechetStandDev(sigma, gamma);
	}

	// Frechet standard deviation
	public static double frechetStandDev(double sigma, double gamma) {
		double y = Double.NaN;
		if (gamma > 2.0D) {
			y = Stat.gamma(1.0D - 2.0D / gamma) - Fmath.square(Stat.gamma(1.0D - 1.0D / gamma));
			y = sigma * Math.sqrt(y);
		}
		return y;
	}

	// Bisection procedure for calculating and F-test value corresponding
	//   to a given F-test probability
	private static double fTestBisect(double fProb, double fTestLow, double fTestHigh, int df1, int df2, int endTest) {

		double funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
		double funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
		double fTestMid = 0.0D;
		double funcMid = 0.0;
		int nExtensions = 0;
		int nIter = 1000; // iterations allowed
		double check = fProb * 1e-6; // tolerance for bisection
		boolean test0 = true; // test for extending bracket
		boolean test1 = true; // test for bisection procedure
		while (test0) {
			if (funcLow * funcHigh > 0.0D) {
				if (endTest < 0) {
					nExtensions++;
					if (nExtensions > 100) {
						System.out.println("Class: Stats\nMethod: fTestBisect\nProbability higher than range covered\nF-test value is less than " + fTestLow);
						System.out.println("This value was returned");
						fTestMid = fTestLow;
						test0 = false;
						test1 = false;
					}
					fTestLow /= 10.0D;
					funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
				} else {
					nExtensions++;
					if (nExtensions > 100) {
						System.out.println("Class: Stats\nMethod: fTestBisect\nProbability lower than range covered\nF-test value is greater than " + fTestHigh);
						System.out.println("This value was returned");
						fTestMid = fTestHigh;
						test0 = false;
						test1 = false;
					}
					fTestHigh *= 10.0D;
					funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
				}
			} else {
				test0 = false;
			}

			int i = 0;
			while (test1) {
				fTestMid = (fTestLow + fTestHigh) / 2.0D;
				funcMid = fProb - Stat.fTestProb(fTestMid, df1, df2);
				if (Math.abs(funcMid) < check) {
					test1 = false;
				} else {
					i++;
					if (i > nIter) {
						System.out.println("Class: Stats\nMethod: fTestBisect\nmaximum number of iterations exceeded\ncurrent value of F-test value returned");
						test1 = false;
					}
					if (funcMid * funcHigh > 0) {
						funcHigh = funcMid;
						fTestHigh = fTestMid;
					} else {
						funcLow = funcMid;
						fTestLow = fTestMid;
					}
				}
			}
		}
		return fTestMid;
	}

	// retained fot compatibility
	public static double fTestProb(double var1, int df1, double var2, int df2) {
		if (df1 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu1, " + df1 + ", must be greater than zero");
		if (df2 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu2, " + df2 + ", must be greater than zero");
		if (var1 < 0) throw new IllegalArgumentException("the variance, var1" + var1 + ", must be greater than or equal to zero");
		if (var1 <= 0) throw new IllegalArgumentException("the variance, var2" + var2 + ", must be greater than zero");
		double fValue = var1 / var2;
		double ddf1 = df1;
		double ddf2 = df2;
		double x = ddf2 / (ddf2 + ddf1 * fValue);
		return Stat.regularisedBetaFunction(df2 / 2.0D, df1 / 2.0D, x);
	}

	// retained fot compatibility
	public static double fTestProb(double fValue, int df1, int df2) {
		if (df1 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu1, " + df1 + ", must be greater than zero");
		if (df2 <= 0) throw new IllegalArgumentException("the degrees of freedom, nu2, " + df2 + ", must be greater than zero");
		if (fValue < 0) throw new IllegalArgumentException("the F-ratio, " + fValue + ", must be greater than or equal to zero");
		double ddf1 = df1;
		double ddf2 = df2;
		double x = ddf2 / (ddf2 + ddf1 * fValue);
		return Stat.regularisedBetaFunction(df2 / 2.0D, df1 / 2.0D, x);
	}

	// Returns the F-test value corresponding to a F-distribution probabilty, fProb,
	//   for degrees of freedom df1, df2
	public static double fTestValueGivenFprob(double fProb, int df1, int df2) {

		// Create an array F-test value array
		int fTestsNum = 100; // length of array
		double[] fTestValues = new double[fTestsNum];
		fTestValues[0] = 0.0001D; // lowest array value
		fTestValues[fTestsNum - 1] = 10000.0D; // highest array value
		// calculate array increment - log scale
		double diff = (Fmath.log10(fTestValues[fTestsNum - 1]) - Fmath.log10(fTestValues[0])) / (fTestsNum - 1);
		// Fill array
		for (int i = 1; i < fTestsNum - 1; i++) {
			fTestValues[i] = Math.pow(10.0D, (Fmath.log10(fTestValues[i - 1]) + diff));
		}

		// calculate F test probability array corresponding to F-test value array
		double[] fTestProb = new double[fTestsNum];
		for (int i = 0; i < fTestsNum; i++) {
			fTestProb[i] = Stat.fTestProb(fTestValues[i], df1, df2);
		}

		// calculate F-test value for provided probability
		// using bisection procedure
		double fTest0 = 0.0D;
		double fTest1 = 0.0D;
		double fTest2 = 0.0D;

		// find bracket for the F-test probabilities and calculate F-Test value from above arrays
		boolean test0 = true;
		boolean test1 = true;
		int i = 0;
		int endTest = 0;
		while (test0) {
			if (fProb == fTestProb[i]) {
				fTest0 = fTestValues[i];
				test0 = false;
				test1 = false;
			} else {
				if (fProb > fTestProb[i]) {
					test0 = false;
					if (i > 0) {
						fTest1 = fTestValues[i - 1];
						fTest2 = fTestValues[i];
						endTest = -1;
					} else {
						fTest1 = fTestValues[i] / 10.0D;
						fTest2 = fTestValues[i];
					}
				} else {
					i++;
					if (i > fTestsNum - 1) {
						test0 = false;
						fTest1 = fTestValues[i - 1];
						fTest2 = 10.0D * fTestValues[i - 1];
						endTest = 1;
					}
				}
			}
		}

		// call bisection method
		if (test1) fTest0 = fTestBisect(fProb, fTest1, fTest2, df1, df2, endTest);

		return fTest0;
	}

	// Gamma function
	// Lanczos approximation (6 terms)
	// retained for backward compatibity
	public static double gamma(double x) {

		double xcopy = x;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];
		double fg = 0.0D;

		if (x >= 0.0) {
			first = Math.pow(first, x + 0.5) * Math.exp(-first);
			for (int i = 1; i <= lgfN; i++)
				second += lgfCoeff[i] / ++xcopy;
			fg = first * Math.sqrt(2.0 * Math.PI) * second / x;
		} else {
			fg = -Math.PI / (x * Stat.gamma(-x) * Math.sin(Math.PI * x));
		}
		return fg;
	}

	// Gamma distribution - standard
	// Cumulative distribution function
	public static double gammaCDF(double gamma, double upperLimit) {
		if (upperLimit < 0.0D) throw new IllegalArgumentException("The upper limit, " + upperLimit + "must be equal to or greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		return regularisedGammaFunction(gamma, upperLimit);
	}

	// Gamma distribution - three parameter
	// Cumulative distribution function
	public static double gammaCDF(double mu, double beta, double gamma, double upperLimit) {
		if (upperLimit < mu) throw new IllegalArgumentException("The upper limit, " + upperLimit + "must be equal to or greater than the location parameter, " + mu);
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		double xx = (upperLimit - mu) / beta;
		return regularisedGammaFunction(gamma, xx);
	}

	// Gamma function
	// Lanczos approximation (6 terms)
	public static double gammaFunction(double x) {

		double xcopy = x;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];
		double fg = 0.0D;

		if (x >= 0.0) {
			first = Math.pow(first, x + 0.5) * Math.exp(-first);
			for (int i = 1; i <= lgfN; i++)
				second += lgfCoeff[i] / ++xcopy;
			fg = first * Math.sqrt(2.0 * Math.PI) * second / x;
		} else {
			fg = -Math.PI / (x * Stat.gamma(-x) * Math.sin(Math.PI * x));
		}
		return fg;
	}

	// Return Gamma function minimum
	// First element contains the Gamma Function minimum value
	// Second element contains the x value at which the minimum occors
	public static double[] gammaFunctionMinimum() {
		double[] ret = { 0.8856031944108839, 1.4616321399961483 };
		return ret;
	}

	// Gamma distribution - three parameter
	// mean
	public static double gammaMean(double mu, double beta, double gamma) {
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		return gamma * beta - mu;
	}

	// Gamma distribution - three parameter
	// mode
	public static double gammaMode(double mu, double beta, double gamma) {
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		double mode = Double.NaN;
		if (gamma >= 1.0D) mode = (gamma - 1.0D) * beta - mu;
		return mode;
	}

	// Gamma distribution - standard
	// probablity density function
	public static double gammaPDF(double gamma, double x) {
		if (x < 0.0D) throw new IllegalArgumentException("The variable, x, " + x + "must be equal to or greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		return Math.pow(x, gamma - 1) * Math.exp(-x) / gammaFunction(gamma);
	}

	// Gamma distribution - three parameter
	// probablity density function
	public static double gammaPDF(double mu, double beta, double gamma, double x) {
		if (x < mu) throw new IllegalArgumentException("The variable, x, " + x + "must be equal to or greater than the location parameter, " + mu);
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		double xx = (x - mu) / beta;
		return Math.pow(xx, gamma - 1) * Math.exp(-xx) / (beta * gammaFunction(gamma));
	}

	// Returns an array of Gamma random deviates - clock seed
	public static double[] gammaRand(double mu, double beta, double gamma, int n) {
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		PsRandom psr = new PsRandom();
		return psr.gammaArray(mu, beta, gamma, n);
	}

	// Returns an array of Gamma random deviates - user supplied seed
	public static double[] gammaRand(double mu, double beta, double gamma, int n, long seed) {
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		PsRandom psr = new PsRandom(seed);
		return psr.gammaArray(mu, beta, gamma, n);
	}

	// Gamma distribution - three parameter
	// standard deviation
	public static double gammaStandardDeviation(double mu, double beta, double gamma) {
		return gammaStandDev(mu, beta, gamma);
	}

	// Gamma distribution - three parameter
	// standard deviation
	public static double gammaStandDev(double mu, double beta, double gamma) {
		if (beta <= 0.0D) throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
		if (gamma <= 0.0D) throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
		return Math.sqrt(gamma) * beta;
	}

	// Gaussian (normal) probability density function
	// mean  =  the mean, sd = standard deviation
	public static double gaussian(double mean, double sd, double x) {
		return Math.exp(-Fmath.square((x - mean) / sd) / 2.0) / (sd * Math.sqrt(2.0D * Math.PI));
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mean  =  the mean, sd = standard deviation
	public static double gaussianCDF(double mean, double sd, double upperlimit) {
		return normalCDF(mean, sd, upperlimit);
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mean  =  the mean, sd = standard deviation
	public static double gaussianCDF(double mean, double sd, double lowerlimit, double upperlimit) {
		return Stat.normalCDF(mean, sd, upperlimit) - Stat.normalCDF(mean, sd, lowerlimit);
	}

	// Gaussian Inverse Cumulative Distribution Function
	// Standardized
	public static double gaussianInverseCDF(double prob) {
		return gaussianInverseCDF(0.0D, 1.0D, prob);
	}

	// Gaussian Inverse Cumulative Distribution Function
	public static double gaussianInverseCDF(double mean, double sd, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");

		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {

				// Create instance of the class holding the gaussian cfd function
				GaussianFunct gauss = new GaussianFunct();

				// set function variables
				gauss.mean = mean;
				gauss.sd = sd;

				// required tolerance
				double tolerance = 1e-12;

				// lower bound
				double lowerBound = mean - 10.0 * sd;

				// upper bound
				double upperBound = mean + 10.0 * sd;

				// Create instance of RealRoot
				RealRoot realR = new RealRoot();

				// Set extension limits
				// none

				// Set tolerance
				realR.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR.resetNaNexceptionToTrue();
				realR.supressLimitReachedMessage();
				realR.supressNaNmessage();

				//  set function cfd  variable
				gauss.cfd = prob;

				// call root searching method
				icdf = realR.bisect(gauss, lowerBound, upperBound);
			}
		}

		return icdf;
	}

	// Gaussian (normal) order statistic medians (n points)
	public static double[] gaussianOrderStatisticMedians(double mean, double sigma, int n) {
		double[] gosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			gosm[i] = Stat.inverseGaussianCDF(mean, sigma, uosm[i]);
		}
		gosm = Stat.scale(gosm, mean, sigma);
		return gosm;
	}

	// Gaussian (normal) order statistic medians for a mean of zero and a standard deviation 0f unity (n points)
	public static double[] gaussianOrderStatisticMedians(int n) {
		return Stat.gaussianOrderStatisticMedians(0.0, 1.0, n);
	}

	// Gaussian (normal) probability  density function
	// mean  =  the mean, sd = standard deviation
	public static double gaussianPDF(double mean, double sd, double x) {
		return Math.exp(-Fmath.square((x - mean) / sd) / 2.0) / (sd * Math.sqrt(2.0D * Math.PI));
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mean  =  the mean, sd = standard deviation
	public static double gaussianProb(double mean, double sd, double upperlimit) {
		return normalCDF(mean, sd, upperlimit);
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mean  =  the mean, sd = standard deviation
	public static double gaussianProb(double mean, double sd, double lowerlimit, double upperlimit) {
		return Stat.normalCDF(mean, sd, upperlimit) - Stat.normalCDF(mean, sd, lowerlimit);
	}

	// Returns an array of Gaussian (normal) random deviates - clock seed
	// mean  =  the mean, sd = standard deviation, length of array
	public static double[] gaussianRand(double mean, double sd, int n) {
		return normalRand(mean, sd, n);
	}

	// Returns an array of Gaussian (normal) random deviates - user provided seed
	// mean  =  the mean, sd = standard deviation, length of array
	public static double[] gaussianRand(double mean, double sd, int n, long seed) {
		return normalRand(mean, sd, n, seed);
	}

	public static double generalisedEntropyOneNat(double[] p, double q, double r) {
		return generalizedEntropyOneNat(p, q, r);
	}

	// Generalised mean of a 1D array of BigDecimal, aa
	public static double generalisedMean(BigDecimal[] aa, BigDecimal m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalisedMean(dd, m.doubleValue());
	}

	// weighted generalized mean of a 1D array of BigDecimal, aa
	public static double generalisedMean(BigDecimal[] aa, BigDecimal[] ww, BigDecimal m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		ArrayMaths am1 = new ArrayMaths(aa);
		double[] dd = am1.getArray_as_double();
		ArrayMaths am2 = new ArrayMaths(ww);
		double[] wd = am2.getArray_as_double();
		return generalisedMean(dd, wd, m.doubleValue());
	}

	// weighted generalized mean of a 1D array of BigDecimal, aa
	public static double generalisedMean(BigDecimal[] aa, BigDecimal[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		ArrayMaths am1 = new ArrayMaths(aa);
		double[] dd = am1.getArray_as_double();
		ArrayMaths am2 = new ArrayMaths(ww);
		double[] wd = am2.getArray_as_double();
		return generalisedMean(dd, wd, m);
	}

	// Generalised mean of a 1D array of BigDecimal, aa
	public static double generalisedMean(BigDecimal[] aa, double m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalisedMean(dd, m);
	}

	// Generalised mean of a 1D array of BigInteger, aa
	public static double generalisedMean(BigInteger[] aa, BigInteger m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalisedMean(dd, m.doubleValue());
	}

	// weighted generalized mean of a 1D array of BigInteger, aa
	public static double generalisedMean(BigInteger[] aa, BigInteger[] ww, BigInteger m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		ArrayMaths am1 = new ArrayMaths(aa);
		double[] dd = am1.getArray_as_double();
		ArrayMaths am2 = new ArrayMaths(ww);
		double[] wd = am2.getArray_as_double();
		return generalisedMean(dd, wd, m.doubleValue());
	}

	// weighted generalized mean of a 1D array of BigInteger, aa
	public static double generalisedMean(BigInteger[] aa, BigInteger[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		ArrayMaths am1 = new ArrayMaths(aa);
		double[] dd = am1.getArray_as_double();
		ArrayMaths am2 = new ArrayMaths(ww);
		double[] wd = am2.getArray_as_double();
		return generalisedMean(dd, wd, m);
	}

	// Generalised mean of a 1D array of BigInteger, aa
	public static double generalisedMean(BigInteger[] aa, double m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalisedMean(dd, m);
	}

	// ARITMETIC MEANS (STATIC)

	// Generalised mean of a 1D array of Complex, aa
	public static Complex generalisedMean(Complex[] aa, Complex m) {
		int n = aa.length;
		Complex sum = Complex.zero();
		if (m.equals(Complex.zero())) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(aa[i]));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.pow(aa[i], m));
			}
			return Complex.pow(sum.over(n), Complex.plusOne().over(m));
		}
	}

	// weighted generalized mean of a 1D array of Complex, aa
	public static Complex generalisedMean(Complex[] aa, Complex[] ww, Complex m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		Complex sum = Complex.zero();
		Complex sumw = Complex.zero();
		Complex[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw = sumw.plus(weight[i]);
		}

		if (m.equals(Complex.zero())) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(weight[i].times(aa[i])).over(sumw));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(weight[i].times(Complex.pow(aa[i], m)));
			}
			return Complex.pow(sum.over(sumw), Complex.plusOne().over(m));
		}
	}

	// weighted generalized mean of a 1D array of Complex, aa
	public static Complex generalisedMean(Complex[] aa, Complex[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		Complex sum = Complex.zero();
		Complex sumw = Complex.zero();
		Complex[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw = sumw.plus(weight[i]);
		}

		if (m == 0.0D) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(weight[i].times(aa[i])).over(sumw));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(weight[i].times(Complex.pow(aa[i], m)));
			}
			return Complex.pow(sum.over(sumw), 1.0D / m);
		}
	}

	// Generalised mean of a 1D array of Complex, aa
	public static Complex generalisedMean(Complex[] aa, double m) {
		int n = aa.length;
		Complex sum = Complex.zero();
		if (m == 0.0D) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(aa[i]));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.pow(aa[i], m));
			}
			return Complex.pow(sum.over(n), 1.0D / m);
		}
	}

	// Generalised mean of a 1D array of doubles, aa
	public static double generalisedMean(double[] aa, double m) {
		int n = aa.length;
		double sum = 0.0D;
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += Math.log(aa[i]);
			}
			return Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += Math.pow(aa[i], m);
			}
			return Math.pow(sum / n, 1.0D / m);
		}
	}

	// weighted generalized mean of a 1D array of doubles, aa
	public static double generalisedMean(double[] aa, double[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		double sum = 0.0D;
		double sumw = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw += weight[i];
		}

		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += Math.log(aa[i] * weight[i] / sumw);
			}
			return Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += weight[i] * Math.pow(aa[i], m);
			}
			return Math.pow(sum / sumw, 1.0D / m);
		}
	}

	// Generalised mean of a 1D array of floats, aa
	public static float generalisedMean(float[] aa, float m) {
		int n = aa.length;
		float sum = 0.0F;
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += (float) Math.log(aa[i]);
			}
			return (float) Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += Math.pow(aa[i], m);
			}
			return (float) Math.pow(sum / n, 1.0F / m);
		}
	}

	// weighted generalized mean of a 1D array of floats, aa
	public static float generalisedMean(float[] aa, float[] ww, float m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		float sum = 0.0F;
		float sumw = 0.0F;
		float[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw += weight[i];
		}
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += (float) Math.log(aa[i]);
			}
			return (float) Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += Math.pow(aa[i], m);
			}
			return (float) Math.pow(sum / sumw, 1.0F / m);
		}
	}

	// GENERALIZED ENTROPY (STATIC METHOD)
	public static double generalizedEntropyOneNat(double[] p, double q, double r) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0D) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0D) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);
		if (r == 0.0D) {
			return Stat.renyiEntropyNat(p, q);
		} else {
			if (r == 1.0D) {
				return Stat.tsallisEntropyNat(p, q);
			} else {
				if (q == 1.0D) {
					double[] tsen = new double[10];
					double[] tsqq = new double[10];
					double qq = 0.995;
					for (int i = 0; i < 5; i++) {
						ArrayMaths am1 = am.pow(qq);
						tsen[i] = (1.0D - Math.pow(am1.getSum_as_double(), r)) / (r * (qq - 1.0));
						tsqq[i] = qq;
						qq += 0.001;
					}
					qq = 1.001;
					for (int i = 5; i < 10; i++) {
						ArrayMaths am1 = am.pow(qq);
						tsen[i] = (1.0D - Math.pow(am1.getSum_as_double(), r)) / (r * (qq - 1.0));
						tsqq[i] = qq;
						qq += 0.001;
					}
					Regression reg = new Regression(tsqq, tsen);
					reg.polynomial(2);
					double[] param = reg.getCoeff();
					return param[0] + param[1] + param[2];
				} else {
					am = am.pow(q);
					return (1.0D - Math.pow(am.getSum_as_double(), r)) / (r * (q - 1.0));
				}
			}
		}
	}

	// generalized mean of a 1D array of BigDecimal, aa
	public static double generalizedMean(BigDecimal[] aa, BigDecimal m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalizedMean(dd, m.doubleValue());
	}

	// generalized mean of a 1D array of BigDecimal, aa
	public static double generalizedMean(BigDecimal[] aa, double m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalizedMean(dd, m);
	}

	// generalized mean of a 1D array of BigInteger, aa
	public static double generalizedMean(BigInteger[] aa, BigInteger m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalizedMean(dd, m.doubleValue());
	}

	// generalized mean of a 1D array of BigInteger, aa
	public static double generalizedMean(BigInteger[] aa, double m) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] dd = am.getArray_as_double();
		return generalizedMean(dd, m);
	}

	// generalized mean of a 1D array of Complex, aa
	public static Complex generalizedMean(Complex[] aa, Complex m) {
		int n = aa.length;
		Complex sum = Complex.zero();
		if (m.equals(Complex.zero())) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(aa[i]));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.pow(aa[i], m));
			}
			return Complex.pow(sum.over(n), Complex.plusOne().over(m));
		}
	}

	// generalized mean of a 1D array of Complex, aa
	public static Complex generalizedMean(Complex[] aa, double m) {
		int n = aa.length;
		Complex sum = Complex.zero();
		if (m == 0.0D) {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.log(aa[i]));
			}
			return Complex.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum = sum.plus(Complex.pow(aa[i], m));
			}
			return Complex.pow(sum.over(n), 1.0D / m);
		}
	}

	// generalized mean of a 1D array of doubles, aa
	public static double generalizedMean(double[] aa, double m) {
		int n = aa.length;
		double sum = 0.0D;
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += Math.log(aa[i]);
			}
			return Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += Math.pow(aa[i], m);
			}
			return Math.pow(sum / n, 1.0D / m);
		}
	}

	// generalized mean of a 1D array of floats, aa
	public static float generalizedMean(float[] aa, float m) {
		int n = aa.length;
		float sum = 0.0F;
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				sum += (float) Math.log(aa[i]);
			}
			return (float) Math.exp(sum);
		} else {
			for (int i = 0; i < n; i++) {
				sum += Math.pow(aa[i], m);
			}
			return (float) Math.pow(sum / n, 1.0F / m);
		}
	}

	// Geometric mean of a 1D array of BigDecimal, aa
	public static double geometricMean(BigDecimal[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++)
			sum += Math.log(aa[i].doubleValue());
		return Math.exp(sum / n);
	}

	// Weighted geometric mean of a 1D array of BigDecimal, aa
	public static double geometricMean(BigDecimal[] aa, BigDecimal[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths weighting = new ArrayMaths(Stat.invertAndSquare(ww));
		double[] weight = weighting.array();

		double sumW = 0.0D;
		for (int i = 0; i < n; i++) {
			sumW += weight[i];
		}
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += Math.log(aa[i].doubleValue()) * weight[i];
		}
		return Math.exp(sum / sumW);
	}

	// Geometric mean of a 1D array of BigInteger, aa
	public static double geometricMean(BigInteger[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++)
			sum += Math.log(aa[i].doubleValue());
		return Math.exp(sum / n);
	}

	// Weighted geometric mean of a 1D array of BigDecimal, aa
	public static double geometricMean(BigInteger[] aa, BigInteger[] ww) {
		ArrayMaths amaa = new ArrayMaths(aa);
		ArrayMaths amww = new ArrayMaths(ww);
		return geometricMean(amaa.array_as_BigDecimal(), amww.array_as_BigDecimal());
	}

	// Geometric mean of a 1D array of Complex, aa
	public static Complex geometricMean(Complex[] aa) {
		int n = aa.length;
		Complex sum = Complex.zero();
		for (int i = 0; i < n; i++)
			sum = sum.plus(Complex.log(aa[i]));
		return Complex.exp(sum.over(n));
	}

	// Weighted geometric mean of a 1D array of Complexs, aa
	public static Complex geometricMean(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		Complex sumW = Complex.zero();
		Complex[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW = sumW.plus(weight[i]);
		}
		Complex sum = Complex.zero();
		for (int i = 0; i < n; i++) {
			sum = sum.plus(Complex.log(aa[i]).times(weight[i]));
		}
		return Complex.exp(sum.over(sumW));
	}

	// Geometric mean of a 1D array of doubles, aa
	public static double geometricMean(double[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++)
			sum += Math.log(aa[i]);
		return Math.exp(sum / n);
	}

	// GEOMETRIC MEANS (STATIC)

	// Weighted geometric mean of a 1D array of double, aa
	public static double geometricMean(double[] aa, double[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		double sumW = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW += weight[i];
		}
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += Math.log(aa[i]) * weight[i];
		}
		return Math.exp(sum / sumW);
	}

	// Geometric mean of a 1D array of floats, aa
	public static float geometricMean(float[] aa) {
		int n = aa.length;
		float sum = 0.0F;
		for (int i = 0; i < n; i++)
			sum += (float) Math.log(aa[i]);
		return (float) Math.exp(sum / n);
	}

	// Weighted geometric mean of a 1D array of floats, aa
	public static float geometricMean(float[] aa, float[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		float sumW = 0.0F;
		float[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW += weight[i];
		}
		float sum = 0.0F;
		for (int i = 0; i < n; i++) {
			sum += (float) Math.log(aa[i]) * weight[i];
		}
		return (float) Math.exp(sum / sumW);
	}

	// Get value of cfMaxIter used in contFract method above
	public static int getCFmaxIter() {
		return cfMaxIter;
	}

	// Get value of cfTol used in contFract method above
	public static double getCFtolerance() {
		return cfTol;
	}

	// Return the nearest smallest representable floating point number to zero with mantissa rounded to 1.0
	public static double getFpmin() {
		return Stat.FPMIN;
	}

	// Return the maximum number of iterations allowed in the calculation of the incomplete gamma functions
	public static int getIncGammaMaxIter() {
		return Stat.igfiter;
	}

	// Return the tolerance used in the calculation of the incomplete gamm functions
	public static double getIncGammaTol() {
		return Stat.igfeps;
	}

	// Return the Lanczos coeeficients
	public static double[] getLanczosCoeff() {
		int n = Stat.getLanczosN() + 1;
		double[] coef = new double[n];
		for (int i = 0; i < n; i++) {
			coef[i] = Stat.lgfCoeff[i];
		}
		return coef;
	}

	// Return the Lanczos constant gamma
	public static double getLanczosGamma() {
		return Stat.lgfGamma;
	}

	// HARMONIC MEANS (STATIC)

	// Return the Lanczos constant N (number of coeeficients + 1)
	public static int getLanczosN() {
		return Stat.lgfN;
	}

	// Maximum Gumbel probability density function
	public static double gumbelMax(double mu, double sigma, double x) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(x - mu) / sigma;
		return (1.0D / sigma) * Math.exp(arg) * Math.exp(-Math.exp(arg));
	}

	// Maximum Gumbel cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	public static double gumbelMaxCDF(double mu, double sigma, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(upperlimit - mu) / sigma;
		return 1.0D - Math.exp(-Math.exp(arg));
	}

	// Maximum Gumbel cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double gumbelMaxCDF(double mu, double sigma, double lowerlimit, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = -Math.exp(-Math.exp(arg1));
		double term2 = -Math.exp(-Math.exp(arg2));
		return term2 - term1;
	}

	// Gumbel (maximum order statistic) Inverse Cumulative Density Function
	public static double gumbelMaxInverseCDF(double mu, double sigma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu - sigma * Math.log(Math.log(1.0 / prob));
			}
		}

		return icdf;
	}

	// Maximum Gumbel mean
	public static double gumbelMaxMean(double mu, double sigma) {
		return mu + sigma * Fmath.EULER_CONSTANT_GAMMA;
	}

	// Maximum Gumbel median
	public static double gumbelMaxMedian(double mu, double sigma) {
		return mu - sigma * Math.log(Math.log(2.0D));
	}

	// Maximum Gumbel mode
	public static double gumbelMaxMode(double mu, double sigma) {
		return mu;
	}

	// Gumbel (maximum order statistic) order statistic medians (n points)
	public static double[] gumbelMaxOrderStatisticMedians(double mu, double sigma, int n) {
		double[] gmosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			gmosm[i] = Stat.gumbelMaxInverseCDF(mu, sigma, uosm[i]);
		}
		return gmosm;
	}

	// Maximum Gumbel probability density function
	public static double gumbelMaxPDF(double mu, double sigma, double x) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(x - mu) / sigma;
		return (1.0D / sigma) * Math.exp(arg) * Math.exp(-Math.exp(arg));
	}

	// GENERALIZED MEANS [POWER MEANS] (STATIC METHODS)

	// Maximum Gumbel cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	public static double gumbelMaxProb(double mu, double sigma, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(upperlimit - mu) / sigma;
		return 1.0D - Math.exp(-Math.exp(arg));
	}

	// Maximum Gumbel cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double gumbelMaxProb(double mu, double sigma, double lowerlimit, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = -Math.exp(-Math.exp(arg1));
		double term2 = -Math.exp(-Math.exp(arg2));
		return term2 - term1;
	}

	// Returns an array of maximal Gumbel (Type I EVD) random deviates - clock seed
	// mu  =  location parameter, sigma = scale parameter, n = length of array
	public static double[] gumbelMaxRand(double mu, double sigma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = mu - Math.log(Math.log(1.0D / (1.0D - rr.nextDouble()))) * sigma;
		}
		return ran;
	}

	// Returns an array of maximal Gumbel (Type I EVD) random deviates - user supplied seed
	// mu  =  location parameter, sigma = scale parameter, n = length of array
	public static double[] gumbelMaxRand(double mu, double sigma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = mu - Math.log(Math.log(1.0D / (1.0D - rr.nextDouble()))) * sigma;
		}
		return ran;
	}

	// Maximum Gumbel standard deviation
	public static double gumbelMaxStandardDeviation(double sigma) {
		return sigma * Math.PI / Math.sqrt(6.0D);
	}

	// Maximum Gumbel standard deviation
	public static double gumbelMaxStandDev(double sigma) {
		return sigma * Math.PI / Math.sqrt(6.0D);
	}

	// Minimum Gumbel probability density function
	public static double gumbelMin(double mu, double sigma, double x) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = (x - mu) / sigma;
		return (1.0D / sigma) * Math.exp(arg) * Math.exp(-Math.exp(arg));
	}

	// Minimum Gumbel cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double gumbelMinCDF(double mu, double sigma, double lowerlimit, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg1 = -(lowerlimit - mu) / sigma;
		double arg2 = -(upperlimit - mu) / sigma;
		double term1 = Math.exp(-Math.exp(arg1));
		double term2 = Math.exp(-Math.exp(arg2));
		return term2 - term1;
	}

	// Gumbel (minimum order statistic) Inverse Cumulative Density Function
	public static double gumbelMinInverseCDF(double mu, double sigma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu + sigma * Math.log(Math.log(1.0 / (1.0 - prob)));
			}
		}

		return icdf;
	}

	// Minimum Gumbel mean
	public static double gumbelMinMean(double mu, double sigma) {
		return mu - sigma * Fmath.EULER_CONSTANT_GAMMA;
	}

	// Minimum Gumbel median
	public static double gumbelMinMedian(double mu, double sigma) {
		return mu + sigma * Math.log(Math.log(2.0D));
	}

	// Minimum Gumbel mode
	public static double gumbelMinMode(double mu, double sigma) {
		return mu;
	}

	// Gumbel (minimum order statistic) order statistic medians (n points)
	public static double[] gumbelMinOrderStatisticMedians(double mu, double sigma, int n) {
		double[] gmosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			gmosm[i] = Stat.gumbelMinInverseCDF(mu, sigma, uosm[i]);
		}
		return gmosm;
	}

	// Minimum Gumbel probability density function
	public static double gumbelMinPDF(double mu, double sigma, double x) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = (x - mu) / sigma;
		return (1.0D / sigma) * Math.exp(arg) * Math.exp(-Math.exp(arg));
	}

	// Minimum Gumbel cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	public static double gumbelMinProb(double mu, double sigma, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(upperlimit - mu) / sigma;
		return Math.exp(-Math.exp(arg));
	}

	// Minimum Gumbel cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double gumbelMinProb(double mu, double sigma, double lowerlimit, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg1 = -(lowerlimit - mu) / sigma;
		double arg2 = -(upperlimit - mu) / sigma;
		double term1 = Math.exp(-Math.exp(arg1));
		double term2 = Math.exp(-Math.exp(arg2));
		return term2 - term1;
	}

	// WEIGHTED GENERALIZED MEANS

	// Minimum Gumbel cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	public static double gumbelMinProbCDF(double mu, double sigma, double upperlimit) {
		if (sigma < 0.0D) throw new IllegalArgumentException("sigma must be positive");
		double arg = -(upperlimit - mu) / sigma;
		return Math.exp(-Math.exp(arg));
	}

	// Returns an array of minimal Gumbel (Type I EVD) random deviates - clock seed
	// mu  =  location parameter, sigma = scale parameter, n = length of array
	public static double[] gumbelMinRand(double mu, double sigma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.log(Math.log(1.0D / (1.0D - rr.nextDouble()))) * sigma + mu;
		}
		return ran;
	}

	// Returns an array of minimal Gumbel (Type I EVD) random deviates - user supplied seed
	// mu  =  location parameter, sigma = scale parameter, n = length of array
	public static double[] gumbelMinRand(double mu, double sigma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.log(Math.log(1.0D / (1.0D - rr.nextDouble()))) * sigma + mu;
		}
		return ran;
	}

	// Minimum Gumbel standard deviation
	public static double gumbelMinStandardDeviation(double sigma) {
		return sigma * Math.PI / Math.sqrt(6.0D);
	}

	// Minimum Gumbel standard deviation
	public static double gumbelMinStandDev(double sigma) {
		return sigma * Math.PI / Math.sqrt(6.0D);
	}

	// Harmonic mean of a 1D array of BigDecimal, aa
	public static BigDecimal harmonicMean(BigDecimal[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++)
			sum = sum.add(BigDecimal.ONE.divide(aa[i], BigDecimal.ROUND_HALF_UP));
		sum = (new BigDecimal((double) n)).divide(sum, BigDecimal.ROUND_HALF_UP);
		return sum;
	}

	// Weighted harmonic mean of a 1D array of BigDecimal, aa
	public static BigDecimal harmonicMean(BigDecimal[] aa, BigDecimal[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal sumW = BigDecimal.ZERO;
		BigDecimal[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW = sumW.add(weight[i]);
		}
		for (int i = 0; i < n; i++)
			sum = sum.add(weight[i].divide(aa[i], BigDecimal.ROUND_HALF_UP));
		sum = sumW.divide(sum, BigDecimal.ROUND_HALF_UP);
		sumW = null;
		weight = null;
		return sum;
	}

	// Harmonic mean of a 1D array of BigInteger, aa
	public static BigDecimal harmonicMean(BigInteger[] aa) {
		int n = aa.length;
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] bd = am.getArray_as_BigDecimal();
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++)
			sum = sum.add(BigDecimal.ONE.divide(bd[i], BigDecimal.ROUND_HALF_UP));
		sum = (new BigDecimal((double) n)).divide(sum, BigDecimal.ROUND_HALF_UP);
		bd = null;
		return sum;
	}

	// Weighted harmonic mean of a 1D array of BigInteger, aa
	public static BigDecimal harmonicMean(BigInteger[] aa, BigInteger[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		ArrayMaths wm = new ArrayMaths(ww);
		return harmonicMean(am.getArray_as_BigDecimal(), wm.getArray_as_BigDecimal());
	}

	// Harmonic mean of a 1D array of Complex, aa
	public static Complex harmonicMean(Complex[] aa) {
		int n = aa.length;
		Complex sum = Complex.zero();
		for (int i = 0; i < n; i++)
			sum = sum.plus(Complex.plusOne().over(aa[i]));
		sum = (new Complex(n)).over(sum);
		return sum;
	}

	// Weighted harmonic mean of a 1D array of Complex, aa
	public static Complex harmonicMean(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		Complex sum = Complex.zero();
		Complex sumW = Complex.zero();
		Complex[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW = sumW.plus(weight[i]);
		}
		for (int i = 0; i < n; i++)
			sum = sum.plus(weight[i].over(aa[i]));
		return sumW.over(sum);
	}

	// Harmonic mean of a 1D array of doubles, aa
	public static double harmonicMean(double[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++)
			sum += 1.0D / aa[i];
		return n / sum;
	}

	// Weighted harmonic mean of a 1D array of doubles, aa
	public static double harmonicMean(double[] aa, double[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		double sum = 0.0D;
		double sumW = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW += weight[i];
		}
		for (int i = 0; i < n; i++)
			sum += weight[i] / aa[i];
		return sumW / sum;
	}

	// Harmonic mean of a 1D array of floats, aa
	public static float harmonicMean(float[] aa) {
		int n = aa.length;
		float sum = 0.0F;
		for (int i = 0; i < n; i++)
			sum += 1.0F / aa[i];
		return n / sum;
	}

	// Weighted harmonic mean of a 1D array of floats, aa
	public static float harmonicMean(float[] aa, float[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		float sum = 0.0F;
		float sumW = 0.0F;
		float[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumW += weight[i];
		}
		for (int i = 0; i < n; i++)
			sum += weight[i] / aa[i];
		return sumW / sum;
	}

	// Distribute data into bins to obtain histogram
	// zero bin position calculated
	public static double[][] histogramBins(double[] data, double binWidth) {

		double dmin = Fmath.minimum(data);
		double dmax = Fmath.maximum(data);
		double span = dmax - dmin;
		double binZero = dmin;
		int nBins = (int) Math.ceil(span / binWidth);
		double histoSpan = nBins * binWidth;
		double rem = histoSpan - span;
		if (rem >= 0) {
			binZero -= rem / 2.0D;
		} else {
			if (Math.abs(rem) / span > histTol) {
				// readjust binWidth
				boolean testBw = true;
				double incr = histTol / nBins;
				int iTest = 0;
				while (testBw) {
					binWidth += incr;
					histoSpan = nBins * binWidth;
					rem = histoSpan - span;
					if (rem < 0) {
						iTest++;
						if (iTest > 1000) {
							testBw = false;
							System.out.println("histogram method could not encompass all data within histogram\nContact Michael thomas Flanagan");
						}
					} else {
						testBw = false;
					}
				}
			}
		}

		return Stat.histogramBins(data, binWidth, binZero);
	}

	// INTERQUARTILE MEANS

	// Distribute data into bins to obtain histogram
	// zero bin position provided
	public static double[][] histogramBins(double[] data, double binWidth, double binZero) {
		double dmax = Fmath.maximum(data);
		int nBins = (int) Math.ceil((dmax - binZero) / binWidth);
		if (binZero + nBins * binWidth > dmax) nBins++;
		int nPoints = data.length;
		int[] dataCheck = new int[nPoints];
		for (int i = 0; i < nPoints; i++)
			dataCheck[i] = 0;
		double[] binWall = new double[nBins + 1];
		binWall[0] = binZero;
		for (int i = 1; i <= nBins; i++) {
			binWall[i] = binWall[i - 1] + binWidth;
		}
		double[][] binFreq = new double[2][nBins];
		for (int i = 0; i < nBins; i++) {
			binFreq[0][i] = (binWall[i] + binWall[i + 1]) / 2.0D;
			binFreq[1][i] = 0.0D;
		}
		boolean test = true;

		for (int i = 0; i < nPoints; i++) {
			test = true;
			int j = 0;
			while (test) {
				if (j == nBins - 1) {
					if (data[i] >= binWall[j] && data[i] <= binWall[j + 1] * (1.0D + Stat.histTol)) {
						binFreq[1][j] += 1.0D;
						dataCheck[i] = 1;
						test = false;
					}
				} else {
					if (data[i] >= binWall[j] && data[i] < binWall[j + 1]) {
						binFreq[1][j] += 1.0D;
						dataCheck[i] = 1;
						test = false;
					}
				}
				if (test) {
					if (j == nBins - 1) {
						test = false;
					} else {
						j++;
					}
				}
			}
		}
		int nMissed = 0;
		for (int i = 0; i < nPoints; i++)
			if (dataCheck[i] == 0) {
				nMissed++;
				System.out.println("p " + i + " " + data[i] + " " + binWall[0] + " " + binWall[nBins]);
			}
		if (nMissed > 0) System.out.println(nMissed + " data points, outside histogram limits, excluded in Stat.histogramBins");
		return binFreq;
	}

	// Distribute data into bins to obtain histogram
	// zero bin position and upper limit provided
	public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper) {
		int n = 0; // new array length
		int m = data.length; // old array length;
		for (int i = 0; i < m; i++)
			if (data[i] <= binUpper) n++;
		if (n != m) {
			double[] newData = new double[n];
			int j = 0;
			for (int i = 0; i < m; i++) {
				if (data[i] <= binUpper) {
					newData[j] = data[i];
					j++;
				}
			}
			System.out.println((m - n) + " data points, above histogram upper limit, excluded in Stat.histogramBins");
			return histogramBins(newData, binWidth, binZero);
		} else {
			return histogramBins(data, binWidth, binZero);

		}
	}

	// Distribute data into bins to obtain histogram and plot the histogram
	// zero bin position calculated
	public static double[][] histogramBinsPlot(double[] data, double binWidth) {
		String xLegend = null;
		return Stat.histogramBinsPlot(data, binWidth, xLegend);
	}

	// Distribute data into bins to obtain histogram and plot the histogram
	// zero bin position provided
	public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero) {
		String xLegend = null;
		return histogramBinsPlot(data, binWidth, binZero, xLegend);
	}

	// ROOT MEAN SQUARES

	// Distribute data into bins to obtain histogram and plot histogram
	// zero bin position and upper limit provided
	public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper) {
		String xLegend = null;
		return histogramBinsPlot(data, binWidth, binZero, binUpper, xLegend);
	}

	// Distribute data into bins to obtain histogram and plot histogram
	// zero bin position, upper limit and x-axis legend provided
	public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper, String xLegend) {
		int n = 0; // new array length
		int m = data.length; // old array length;
		for (int i = 0; i < m; i++)
			if (data[i] <= binUpper) n++;
		if (n != m) {
			double[] newData = new double[n];
			int j = 0;
			for (int i = 0; i < m; i++) {
				if (data[i] <= binUpper) {
					newData[j] = data[i];
					j++;
				}
			}
			System.out.println((m - n) + " data points, above histogram upper limit, excluded in Stat.histogramBins");
			return histogramBinsPlot(newData, binWidth, binZero, xLegend);
		} else {
			return histogramBinsPlot(data, binWidth, binZero, xLegend);

		}
	}

	// Distribute data into bins to obtain histogram and plot the histogram
	// zero bin position and x-axis legend provided
	public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, String xLegend) {
		double[][] results = histogramBins(data, binWidth, binZero);
		int nBins = results[0].length;
		int nPoints = nBins * 3 + 1;
		double[][] cdata = Plot.data(1, nPoints);
		cdata[0][0] = binZero;
		cdata[1][0] = 0.0D;
		int k = 1;
		for (int i = 0; i < nBins; i++) {
			cdata[0][k] = cdata[0][k - 1];
			cdata[1][k] = results[1][i];
			k++;
			cdata[0][k] = cdata[0][k - 1] + binWidth;
			cdata[1][k] = results[1][i];
			k++;
			cdata[0][k] = cdata[0][k - 1];
			cdata[1][k] = 0.0D;
			k++;
		}

		PlotGraph pg = new PlotGraph(cdata);
		pg.setGraphTitle("Histogram:  Bin Width = " + binWidth);
		pg.setLine(3);
		pg.setPoint(0);
		pg.setYaxisLegend("Frequency");
		if (xLegend != null) pg.setXaxisLegend(xLegend);
		pg.plot();

		return results;
	}

	// Distribute data into bins to obtain histogram and plot the histogram
	// zero bin position calculated, x-axis legend provided
	public static double[][] histogramBinsPlot(double[] data, double binWidth, String xLegend) {
		double dmin = Fmath.minimum(data);
		double dmax = Fmath.maximum(data);
		double span = dmax - dmin;
		int nBins = (int) Math.ceil(span / binWidth);
		double rem = nBins * binWidth - span;
		double binZero = dmin - rem / 2.0D;
		return Stat.histogramBinsPlot(data, binWidth, binZero, xLegend);
	}

	// WEIGHTED ROOT MEAN SQUARES

	// Suppress error message in incomplete gamma series and incomplete gamma fraction methods supressed
	public static void igSupress() {
		Stat.igSupress = true;
	}

	// Regularised Incomplete Beta function
	// Continued Fraction approximation (see Numerical recipies for details of method)
	// retained for compatibility reasons
	public static double incompleteBeta(double z, double w, double x) {
		return regularisedBetaFunction(z, w, x);
	}

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	// Retained for backward compatibility
	public static double incompleteGamma(double a, double x) {
		return regularisedGammaFunction(a, x);
	}

	// Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
	// Retained for backward compatibility
	public static double incompleteGammaComplementary(double a, double x) {
		return complementaryRegularisedGammaFunction(a, x);
	}

	// MEDIANS

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	// Continued Fraction representation of the function - valid for x >= a + 1
	// This method follows the general procedure used in Numerical Recipes for C,
	// The Art of Scientific Computing
	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	// Cambridge University Press,   http://www.nr.com/
	public static double incompleteGammaFract(double a, double x) {
		if (a < 0.0D || x < 0.0D) throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
		if (x < a + 1) throw new IllegalArgumentException("\nx < a+1   Use Series Representation");

		double igf = 0.0D;

		if (x != 0.0D) {

			int i = 0;
			double ii = 0;
			boolean check = true;

			double loggamma = Stat.logGamma(a);
			double numer = 0.0D;
			double incr = 0.0D;
			double denom = x - a + 1.0D;
			double first = 1.0D / denom;
			double term = 1.0D / FPMIN;
			double prod = first;

			while (check) {
				++i;
				ii = i;
				numer = -ii * (ii - a);
				denom += 2.0D;
				first = numer * first + denom;
				if (Math.abs(first) < Stat.FPMIN) {
					first = Stat.FPMIN;
				}
				term = denom + numer / term;
				if (Math.abs(term) < Stat.FPMIN) {
					term = Stat.FPMIN;
				}
				first = 1.0D / first;
				incr = first * term;
				prod *= incr;
				if (Math.abs(incr - 1.0D) < igfeps) check = false;
				if (i >= Stat.igfiter) {
					check = false;
					igf = Double.NaN;
					if (!Stat.igSupress) {
						System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaFract().");
						System.out.println("NaN returned.\nIncrement - 1 = " + String.valueOf(incr - 1) + ".");
						System.out.println("Tolerance =  " + String.valueOf(igfeps));
					}
				}
			}
			igf = 1.0D - Math.exp(-x + a * Math.log(x) - loggamma) * prod;
		}

		return igf;
	}

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	// Series representation of the function - valid for x < a + 1
	public static double incompleteGammaSer(double a, double x) {
		if (a < 0.0D || x < 0.0D) throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
		if (x >= a + 1) throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");

		double igf = 0.0D;

		if (x != 0.0D) {

			int i = 0;
			boolean check = true;

			double acopy = a;
			double sum = 1.0 / a;
			double incr = sum;
			double loggamma = Stat.logGamma(a);

			while (check) {
				++i;
				++a;
				incr *= x / a;
				sum += incr;
				if (Math.abs(incr) < Math.abs(sum) * Stat.igfeps) {
					igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
					check = false;
				}
				if (i >= Stat.igfiter) {
					check = false;
					igf = Double.NaN;
					if (!Stat.igSupress) {
						System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaSer().");
						System.out.println("NaN returned.\nIncrement = " + String.valueOf(incr) + ".");
						System.out.println("Sum = " + String.valueOf(sum) + ".\nTolerance =  " + String.valueOf(igfeps));
					}
				}
			}
		}

		return igf;
	}

	// Interquartile mean of a 1D array of BigDecimal, aa
	public static BigDecimal interQuartileMean(BigDecimal[] aa) {
		int n = aa.length;
		if (n < 4) throw new IllegalArgumentException("At least 4 array elements needed");
		ArrayMaths am = new ArrayMaths(aa);
		ArrayMaths as = am.sort();
		BigDecimal[] bb = as.getArray_as_BigDecimal();
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = n / 4; i < 3 * n / 4; i++)
			sum = sum.add(bb[i]);
		sum = sum.multiply(new BigDecimal(2.0D / n));
		bb = null;
		return sum;
	}

	// Interquartile mean of a 1D array of BigInteger, aa
	public static BigDecimal interQuartileMean(BigInteger[] aa) {
		int n = aa.length;
		if (n < 4) throw new IllegalArgumentException("At least 4 array elements needed");
		ArrayMaths am = new ArrayMaths(aa);
		ArrayMaths as = am.sort();
		BigDecimal[] bb = as.getArray_as_BigDecimal();
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = n / 4; i < 3 * n / 4; i++)
			sum = sum.add(bb[i]);
		sum = sum.multiply(new BigDecimal(2.0D / n));
		bb = null;
		return sum;
	}

	// Interquartile mean of a 1D array of doubles, aa
	public static double interQuartileMean(double[] aa) {
		int n = aa.length;
		if (n < 4) throw new IllegalArgumentException("At least 4 array elements needed");
		double[] bb = Fmath.selectionSort(aa);
		double sum = 0.0D;
		for (int i = n / 4; i < 3 * n / 4; i++)
			sum += bb[i];
		return 2.0 * sum / (n);
	}

	// Interquartile mean of a 1D array of floats, aa
	public static float interQuartileMean(float[] aa) {
		int n = aa.length;
		if (n < 4) throw new IllegalArgumentException("At least 4 array elements needed");
		float[] bb = Fmath.selectionSort(aa);
		float sum = 0.0F;
		for (int i = n / 4; i < 3 * n / 4; i++)
			sum += bb[i];
		return 2.0F * sum / (n);
	}

	// Chi Square Inverse Cumulative Distribution Function
	public static double inverseChiSquareCDF(int nu, double prob) {
		return chiSquareInverseCDF(nu, prob);
	}

	// Exponential Inverse Cumulative Density Function
	public static double inverseExponentialCDF(double mu, double sigma, double prob) {
		return exponentialInverseCDF(mu, sigma, prob);
	}

	// Inverse Gamma Function
	public static double[] inverseGammaFunction(double gamma) {
		double gammaMinimum = 0.8856031944108839;
		double iGammaMinimum = 1.4616321399961483;
		if (gamma < gammaMinimum) throw new IllegalArgumentException("Entered argument (gamma) value, " + gamma + ", must be equal to or greater than 0.8856031944108839 - this method does not handle the negative domain");

		double[] igamma = new double[2];

		// required tolerance
		double tolerance = 1e-12;

		// x value between 0 and 1.4616321399961483
		if (gamma == 1.0) {
			igamma[0] = 1.0;
		} else {
			if (gamma == gammaMinimum) {
				igamma[0] = iGammaMinimum;
			} else {
				// Create instance of the class holding the gamma inverse function
				InverseGammaFunct gif1 = new InverseGammaFunct();

				// Set inverse gamma function variable
				gif1.gamma = gamma;

				//  lower bounds
				double lowerBound1 = 0.0;

				// upper bound
				double upperBound1 = iGammaMinimum;

				// Create instance of RealRoot
				RealRoot realR1 = new RealRoot();

				// Set extension limits
				realR1.noBoundsExtensions();

				// Set tolerance
				realR1.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR1.resetNaNexceptionToTrue();
				realR1.supressLimitReachedMessage();
				realR1.supressNaNmessage();

				// call root searching method
				igamma[0] = realR1.bisect(gif1, lowerBound1, upperBound1);
			}
		}

		// x value above 1.4616321399961483
		if (gamma == 1.0) {
			igamma[1] = 2.0;
		} else {
			if (gamma == gammaMinimum) {
				igamma[1] = iGammaMinimum;
			} else {
				// Create instance of the class holding the gamma inverse function
				InverseGammaFunct gif2 = new InverseGammaFunct();

				// Set inverse gamma function variable
				gif2.gamma = gamma;

				//  bounds
				double lowerBound2 = iGammaMinimum;
				double upperBound2 = 2.0;
				double ii = 2.0;
				double gii = Stat.gamma(ii);
				if (gamma > gii) {
					boolean test = true;
					while (test) {
						ii += 1.0;
						gii = Stat.gamma(ii);
						if (gamma <= gii) {
							upperBound2 = ii;
							lowerBound2 = ii - 1.0;
							test = false;
						}
					}
				}

				// Create instance of RealRoot
				RealRoot realR2 = new RealRoot();

				// Set extension limits
				realR2.noBoundsExtensions();

				// Set tolerance
				realR2.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR2.resetNaNexceptionToTrue();
				realR2.supressLimitReachedMessage();
				realR2.supressNaNmessage();

				// call root searching method
				igamma[1] = realR2.bisect(gif2, lowerBound2, upperBound2);
			}
		}

		return igamma;
	}

	// Gaussian Inverse Cumulative Distribution Function
	// Standardized
	public static double inverseGaussianCDF(double prob) {
		return gaussianInverseCDF(0.0D, 1.0D, prob);
	}

	// Gaussian Inverse Cumulative Distribution Function
	public static double inverseGaussianCDF(double mean, double sd, double prob) {
		return gaussianInverseCDF(mean, sd, prob);
	}

	// Gaussian Inverse Cumulative Distribution Function
	// Standardized
	public static double inverseNormalCDF(double prob) {
		return gaussianInverseCDF(0.0D, 1.0D, prob);
	}

	// STANDARD DEVIATIONS  (STATIC METHODS)

	// Gaussian Inverse Cumulative Distribution Function
	public static double inverseNormalCDF(double mean, double sd, double prob) {
		return gaussianInverseCDF(mean, sd, prob);
	}

	// Pareto Inverse Cumulative Density Function
	public static double inverseParetoCDF(double alpha, double beta, double prob) {
		return paretoInverseCDF(alpha, beta, prob);
	}

	// Rayleigh Inverse Cumulative Density Function
	public static double inverseRayleighCDF(double beta, double prob) {
		return rayleighInverseCDF(beta, prob);
	}

	public static double inverseWeibullCDF(double gamma, double prob) {
		return weibullInverseCDF(0.0D, 1.0D, gamma, prob);
	}

	public static double inverseWeibullCDF(double sigma, double gamma, double prob) {
		return weibullInverseCDF(0.0, sigma, gamma, prob);
	}

	// Weibull Inverse Cumulative Disrtibution Function
	public static double inverseWeibullCDF(double mu, double sigma, double gamma, double prob) {
		return weibullInverseCDF(mu, sigma, gamma, prob);
	}

	private static BigDecimal[] invertAndSquare(BigDecimal[] ww) {
		BigDecimal[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_BigDecimal();
		}
		return weight;
	}

	private static Complex[] invertAndSquare(Complex[] ww) {
		Complex[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_Complex();
		}
		return weight;
	}

	// private weighting calculation
	// returns weight w
	// litte w to one over little w squared if uncertainties used
	private static double[] invertAndSquare(double[] ww) {
		double[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array();
		}
		return weight;
	}

	private static float[] invertAndSquare(float[] ww) {
		float[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_float();
		}
		return weight;
	}

	// Kutosis of a 1D array of BigDecimal
	public static BigDecimal kurtosis(BigDecimal[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			BigDecimal hold = aa[i].subtract(mean);
			sum = sum.add(hold.multiply(hold.multiply(hold.multiply(hold))));
		}
		sum = sum.divide(new BigDecimal(denom), BigDecimal.ROUND_HALF_UP);
		mean = Stat.variance(aa);
		if (mean.doubleValue() == 0.0) {
			sum = new BigDecimal(2.0 / denom);
		} else {
			sum = sum.divide(mean.multiply(mean), BigDecimal.ROUND_HALF_UP);
		}
		mean = null;
		return sum;
	}

	// Kutosis of a 1D array of BigInteger
	public static BigDecimal kurtosis(BigInteger[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] bd = am.array_as_BigDecimal();
		return Stat.kurtosis(bd);
	}

	// KURTOSIS
	// Static Methods
	// Kutosis of a 1D array of doubles
	public static double kurtosis(double[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 4);
		}

		sum = sum / denom;
		double ret = sum / Fmath.square(Stat.variance(aa));
		if (Fmath.isNaN(ret)) ret = 2.0 / denom;
		return ret;
	}

	// Kutosis of a 1D array of floats
	public static float kurtosis(float[] aa) {
		int n = aa.length;
		float denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		float sum = 0.0F;
		float mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 4);
		}
		sum = sum / denom;
		float ret = sum / Fmath.square(Stat.variance(aa));
		if (Fmath.isNaN(ret)) ret = 2.0F / denom;
		return ret;
	}

	// Kutosis of a 1D array of int
	public static double kurtosis(int[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 4);
		}
		sum = sum / denom;
		double ret = sum / Fmath.square(Stat.variance(aa));
		if (Fmath.isNaN(ret)) ret = 2.0 / denom;
		return ret;
	}

	// Kutosis of a 1D array of long
	public static double kurtosis(long[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 4);
		}
		sum = sum / denom;
		double ret = sum / Fmath.square(Stat.variance(aa));
		if (Fmath.isNaN(ret)) ret = 2.0 / denom;
		return ret;
	}

	// Kutosis excess of a 1D array of BigDecimal
	public static BigDecimal kurtosisExcess(BigDecimal[] aa) {
		return Stat.kurtosis(aa).subtract(new BigDecimal("3.0"));
	}

	// Kutosis excess of a 1D array of BigInteger
	public static BigDecimal kurtosisExcess(BigInteger[] aa) {
		return Stat.kurtosis(aa).subtract(new BigDecimal("3.0"));
	}

	// Kutosis excess of a 1D array of doubles
	public static double kurtosisExcess(double[] aa) {
		return Stat.kurtosis(aa) - 3.0D;
	}

	// Kutosis excess of a 1D array of floats
	public static float kurtosisExcess(float[] aa) {
		return Stat.kurtosis(aa) - 3.0F;
	}

	// VOLATILITIES

	// Kutosis excess of a 1D array of int
	public static double kurtosisExcess(int[] aa) {
		return Stat.kurtosis(aa) - 3.0D;
	}

	// Kutosis excess of a 1D array of long
	public static double kurtosisExcess(long[] aa) {
		return Stat.kurtosis(aa) - 3.0D;
	}

	// Linear correlation coefficient single probablity
	// old name calls renamed method
	public static double linearCorrCoeff(double rCoeff, int nu) {
		return Stat.corrCoeffPDF(rCoeff, nu);
	}

	// Linear correlation coefficient cumulative probablity
	// old name calls renamed method
	public static double linearCorrCoeffProb(double rCoeff, int nu) {
		return corrCoeffProb(rCoeff, nu);
	}

	// log to base e of the factorial of n
	// Argument is of type double but must be, numerically, an integer
	// log[e](factorial) returned as double
	// numerical rounding may makes this an approximation
	public static double logFactorial(double n) {
		if (n < 0 || (n - Math.floor(n)) != 0) throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		double f = 0.0D;
		double iCount = 2.0D;
		while (iCount <= n) {
			f += Math.log(iCount);
			iCount += 1.0D;
		}
		return f;
	}

	// log to base e of the factorial of n
	// log[e](factorial) returned as double
	// numerical rounding may makes this an approximation
	public static double logFactorial(int n) {
		if (n < 0) throw new IllegalArgumentException("\nn, " + n + ", must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		double f = 0.0D;
		for (int i = 2; i <= n; i++)
			f += Math.log(i);
		return f;
	}

	// log to base e of the factorial of n
	// Argument is of type double but must be, numerically, an integer
	// log[e](factorial) returned as double
	// numerical rounding may makes this an approximation
	public static double logFactorial(long n) {
		if (n < 0) throw new IllegalArgumentException("\nn, " + n + ", must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
		double f = 0.0D;
		long iCount = 2L;
		while (iCount <= n) {
			f += Math.log(iCount);
			iCount += 1L;
		}
		return f;
	}

	// log to base e of the Gamma function
	// Lanczos approximation (6 terms)
	// Retained for backward compatibility
	public static double logGamma(double x) {
		double xcopy = x;
		double fg = 0.0D;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];

		if (x >= 0.0) {
			first -= (x + 0.5) * Math.log(first);
			for (int i = 1; i <= lgfN; i++)
				second += lgfCoeff[i] / ++xcopy;
			fg = Math.log(Math.sqrt(2.0 * Math.PI) * second / x) - first;
		} else {
			fg = Math.PI / (Stat.gamma(1.0D - x) * Math.sin(Math.PI * x));

			if (fg != 1.0 / 0.0 && fg != -1.0 / 0.0) {
				if (fg < 0) {
					throw new IllegalArgumentException("\nThe gamma function is negative");
				} else {
					fg = Math.log(fg);
				}
			}
		}
		return fg;
	}

	// COEFFICIENT OF VARIATION

	// log to base e of the Gamma function
	// Lanczos approximation (6 terms)
	public static double logGammaFunction(double x) {
		double xcopy = x;
		double fg = 0.0D;
		double first = x + lgfGamma + 0.5;
		double second = lgfCoeff[0];

		if (x >= 0.0) {
			first -= (x + 0.5) * Math.log(first);
			for (int i = 1; i <= lgfN; i++)
				second += lgfCoeff[i] / ++xcopy;
			fg = Math.log(Math.sqrt(2.0 * Math.PI) * second / x) - first;
		} else {
			fg = Math.PI / (Stat.gamma(1.0D - x) * Math.sin(Math.PI * x));

			if (fg != 1.0 / 0.0 && fg != -1.0 / 0.0) {
				if (fg < 0) {
					throw new IllegalArgumentException("\nThe gamma function is negative");
				} else {
					fg = Math.log(fg);
				}
			}
		}
		return fg;
	}

	// Logistic probability density function
	// mu  =  location parameter, beta = scale parameter
	public static double logistic(double mu, double beta, double x) {
		return Fmath.square(Fmath.sech((x - mu) / (2.0D * beta))) / (4.0D * beta);
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mu  =  location parameter, beta = scale parameter
	public static double logisticCDF(double mu, double beta, double upperlimit) {
		return 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mu  =  location parameter, beta = scale parameter
	public static double logisticCDF(double mu, double beta, double lowerlimit, double upperlimit) {
		double arg1 = 0.5D * (1.0D + Math.tanh((lowerlimit - mu) / (2.0D * beta)));
		double arg2 = 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
		return arg2 - arg1;
	}

	// WEIGHTED COEFFICIENT OF VARIATION

	// Logistic Inverse Cumulative Density Function
	public static double logisticInverseCDF(double mu, double beta, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu - beta * Math.log(1.0 / prob - 1.0);
			}
		}

		return icdf;
	}

	// Logistic distribution mean
	public static double logisticMean(double mu) {
		return mu;
	}

	// Logistic distribution median
	public static double logisticMedian(double mu) {
		return mu;
	}

	// Logistic distribution mode
	public static double logisticMode(double mu) {
		return mu;
	}

	// Logistic order statistic medians (n points)
	public static double[] logisticOrderStatisticMedians(double mu, double beta, int n) {
		double[] losm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			losm[i] = Stat.logisticInverseCDF(mu, beta, uosm[i]);
		}
		return losm;
	}

	// Logistic probability density function density function
	// mu  =  location parameter, beta = scale parameter
	public static double logisticPDF(double mu, double beta, double x) {
		return Fmath.square(Fmath.sech((x - mu) / (2.0D * beta))) / (4.0D * beta);
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mu  =  location parameter, beta = scale parameter
	public static double logisticProb(double mu, double beta, double upperlimit) {
		return 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mu  =  location parameter, beta = scale parameter
	public static double logisticProb(double mu, double beta, double lowerlimit, double upperlimit) {
		double arg1 = 0.5D * (1.0D + Math.tanh((lowerlimit - mu) / (2.0D * beta)));
		double arg2 = 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
		return arg2 - arg1;
	}

	// Returns an array of logistic distribution random deviates - clock seed
	// mu  =  location parameter, beta = scale parameter
	public static double[] logisticRand(double mu, double beta, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = 2.0D * beta * Fmath.atanh(2.0D * rr.nextDouble() - 1.0D) + mu;
		}
		return ran;
	}

	// Returns an array of Logistic random deviates - user provided seed
	// mu  =  location parameter, beta = scale parameter
	public static double[] logisticRand(double mu, double beta, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = 2.0D * beta * Fmath.atanh(2.0D * rr.nextDouble() - 1.0D) + mu;
		}
		return ran;
	}

	// Logistic distribution standard deviation
	public static double logisticStandardDeviation(double beta) {
		return logisticStandDev(beta);
	}

	// Logistic distribution standard deviation
	public static double logisticStandDev(double beta) {
		return Math.sqrt(Fmath.square(Math.PI * beta) / 3.0D);
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mu  =  location parameter, beta = scale parameter
	public static double logisticTwoParCDF(double mu, double beta, double upperlimit) {
		return 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
	}

	// Logistic cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mu  =  location parameter, beta = scale parameter
	public static double logisticTwoParCDF(double mu, double beta, double lowerlimit, double upperlimit) {
		double arg1 = 0.5D * (1.0D + Math.tanh((lowerlimit - mu) / (2.0D * beta)));
		double arg2 = 0.5D * (1.0D + Math.tanh((upperlimit - mu) / (2.0D * beta)));
		return arg2 - arg1;
	}

	// Logistic Inverse Cumulative Density Function
	public static double logisticTwoParInverseCDF(double mu, double beta, double prob) {
		return logisticInverseCDF(mu, beta, prob);
	}

	// Logistic distribution mean
	public static double logisticTwoParMean(double mu) {
		return mu;
	}

	// Logistic distribution median
	public static double logisticTwoParMedian(double mu) {
		return mu;
	}

	// Logistic distribution mode
	public static double logisticTwoParMode(double mu) {
		return mu;
	}

	// Logistic order statistic medians (n points)
	public static double[] logisticTwoParOrderStatisticMedians(double mu, double beta, int n) {
		double[] losm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			losm[i] = Stat.logisticInverseCDF(mu, beta, uosm[i]);
		}
		return losm;
	}

	// Logistic probability density function density function
	// mu  =  location parameter, beta = scale parameter
	public static double logisticTwoParPDF(double mu, double beta, double x) {
		return Fmath.square(Fmath.sech((x - mu) / (2.0D * beta))) / (4.0D * beta);
	}

	// Returns an array of logistic distribution random deviates - clock seed
	// mu  =  location parameter, beta = scale parameter
	public static double[] logisticTwoParRand(double mu, double beta, int n) {
		return logisticRand(mu, beta, n);
	}

	// Returns an array of Logistic random deviates - user provided seed
	// mu  =  location parameter, beta = scale parameter
	public static double[] logisticTwoParRand(double mu, double beta, int n, long seed) {
		return logisticRand(mu, beta, n, seed);
	}

	// Logistic distribution standard deviation
	public static double logisticTwoParStandardDeviation(double beta) {
		return Math.sqrt(Fmath.square(Math.PI * beta) / 3.0D);
	}

	// Two parameter log-normal cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double logNormalCDF(double mu, double sigma, double upperLimit) {
		if (sigma < 0) throw new IllegalArgumentException("The parameter sigma, " + sigma + ", must be greater than or equal to zero");
		if (upperLimit <= 0) {
			return 0.0D;
		} else {
			return 0.5D * (1.0D + Stat.erf((Math.log(upperLimit) - mu) / (sigma * Math.sqrt(2))));
		}
	}

	// Two parameter log-normal cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double logNormalCDF(double mu, double sigma, double lowerLimit, double upperLimit) {
		if (sigma < 0) throw new IllegalArgumentException("The parameter sigma, " + sigma + ", must be greater than or equal to zero");
		if (upperLimit < lowerLimit) throw new IllegalArgumentException("The upper limit, " + upperLimit + ", must be greater than the " + lowerLimit);

		double arg1 = 0.0D;
		double arg2 = 0.0D;
		double cdf = 0.0D;

		if (lowerLimit != upperLimit) {
			if (upperLimit > 0.0D) arg1 = 0.5D * (1.0D + Stat.erf((Math.log(upperLimit) - mu) / (sigma * Math.sqrt(2))));
			if (lowerLimit > 0.0D) arg2 = 0.5D * (1.0D + Stat.erf((Math.log(lowerLimit) - mu) / (sigma * Math.sqrt(2))));
			cdf = arg1 - arg2;
		}

		return cdf;
	}

	// Log-Normal Inverse Cumulative Distribution Function
	// Two parameter
	public static double logNormalInverseCDF(double mu, double sigma, double prob) {
		double alpha = 0.0;
		double beta = sigma;
		double gamma = Math.exp(mu);

		return logNormalInverseCDF(alpha, beta, gamma, prob);
	}

	// Log-Normal Inverse Cumulative Distribution Function
	// Three parameter
	public static double logNormalInverseCDF(double alpha, double beta, double gamma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");

		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = alpha;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {

				// Create instance of the class holding the Log-Normal cfd function
				LogNormalThreeParFunct lognorm = new LogNormalThreeParFunct();

				// set function variables
				lognorm.alpha = alpha;
				lognorm.beta = beta;
				lognorm.gamma = gamma;

				// required tolerance
				double tolerance = 1e-12;

				// lower bound
				double lowerBound = alpha;

				// upper bound
				double upperBound = Stat.logNormalThreeParMean(alpha, beta, gamma) + 5.0 * Stat.logNormalThreeParStandardDeviation(alpha, beta, gamma);

				// Create instance of RealRoot
				RealRoot realR = new RealRoot();

				// Set extension limits
				realR.noLowerBoundExtension();

				// Set tolerance
				realR.setTolerance(tolerance);

				// Supress error messages and arrange for NaN to be returned as root if root not found
				realR.resetNaNexceptionToTrue();
				realR.supressLimitReachedMessage();
				realR.supressNaNmessage();

				//  set function cfd  variable
				lognorm.cfd = prob;

				// call root searching method
				icdf = realR.bisect(lognorm, lowerBound, upperBound);
			}
		}

		return icdf;
	}

	// Two parameter log-normal mean
	public static double logNormalMean(double mu, double sigma) {
		return Math.exp(mu + sigma * sigma / 2.0D);
	}

	// Two parameter log-normal median
	public static double logNormalMedian(double mu) {
		return Math.exp(mu);
	}

	// Two parameter log-normal mode
	public static double logNormalMode(double mu, double sigma) {
		return Math.exp(mu - sigma * sigma);
	}

	// LogNormal order statistic medians (n points)
	// Three parametrs
	public static double[] logNormalOrderStatisticMedians(double alpha, double beta, double gamma, int n) {
		double[] lnosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			lnosm[i] = Stat.logNormalThreeParInverseCDF(alpha, beta, gamma, uosm[i]);
		}
		lnosm = Stat.scale(lnosm, Stat.logNormalThreeParMean(alpha, beta, gamma), Stat.logNormalThreeParStandardDeviation(alpha, beta, gamma));
		return lnosm;
	}

	// LogNormal order statistic medians (n points)
	// Two parametrs
	public static double[] logNormalOrderStatisticMedians(double mu, double sigma, int n) {
		double alpha = 0.0;
		double beta = sigma;
		double gamma = Math.exp(mu);

		return logNormalOrderStatisticMedians(alpha, beta, gamma, n);
	}

	// Two parameter log-normal probability density function
	public static double logNormalPDF(double mu, double sigma, double x) {
		if (sigma < 0) throw new IllegalArgumentException("The parameter sigma, " + sigma + ", must be greater than or equal to zero");
		if (x < 0) {
			return 0.0D;
		} else {
			return Math.exp(-0.5D * Fmath.square((Math.log(x) - mu) / sigma)) / (x * sigma * Math.sqrt(2.0D * Math.PI));
		}
	}

	// Returns an array of two parameter log-normal random deviates - clock seed
	public static double[] logNormalRand(double mu, double sigma, int n) {
		if (n <= 0) throw new IllegalArgumentException("The number of random deviates required, " + n + ", must be greater than zero");
		if (sigma < 0) throw new IllegalArgumentException("The parameter sigma, " + sigma + ", must be greater than or equal to zero");
		PsRandom psr = new PsRandom();
		return psr.logNormalArray(mu, sigma, n);
	}

	// Returns an array of two parameter log-normal random deviates - user supplied seed
	public static double[] logNormalRand(double mu, double sigma, int n, long seed) {
		if (n <= 0) throw new IllegalArgumentException("The number of random deviates required, " + n + ", must be greater than zero");
		if (sigma < 0) throw new IllegalArgumentException("The parameter sigma, " + sigma + ", must be greater than or equal to zero");
		PsRandom psr = new PsRandom(seed);
		return psr.logNormalArray(mu, sigma, n);
	}

	// Two parameter log-normal standard deviation
	public static double logNormalStandardDeviation(double mu, double sigma) {
		return logNormalStandDev(mu, sigma);
	}

	// Two parameter log-normal standard deviation
	public static double logNormalStandDev(double mu, double sigma) {
		double sigma2 = sigma * sigma;
		return Math.sqrt((Math.exp(sigma2) - 1.0D) * Math.exp(2.0D * mu + sigma2));
	}

	// Three parameter log-normal cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double logNormalThreeParCDF(double alpha, double beta, double gamma, double upperLimit) {
		if (beta < 0) throw new IllegalArgumentException("The parameter beta, " + beta + ", must be greater than or equal to zero");
		if (upperLimit <= alpha) {
			return 0.0D;
		} else {
			return 0.5D * (1.0D + Stat.erf(Math.log((upperLimit - alpha) / gamma) / (beta * Math.sqrt(2))));
		}
	}

	// Three parameter log-normal cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double logNormalThreeParCDF(double alpha, double beta, double gamma, double lowerLimit, double upperLimit) {
		if (beta < 0) throw new IllegalArgumentException("The parameter beta, " + beta + ", must be greater than or equal to zero");
		if (upperLimit < lowerLimit) throw new IllegalArgumentException("The upper limit, " + upperLimit + ", must be greater than the " + lowerLimit);

		double arg1 = 0.0D;
		double arg2 = 0.0D;
		double cdf = 0.0D;

		if (lowerLimit != upperLimit) {
			if (upperLimit > alpha) arg1 = 0.5D * (1.0D + Stat.erf(Math.log((upperLimit - alpha) / gamma) / (beta * Math.sqrt(2))));
			if (lowerLimit > alpha) arg2 = 0.5D * (1.0D + Stat.erf(Math.log((lowerLimit - alpha) / gamma) / (beta * Math.sqrt(2))));
			cdf = arg1 - arg2;
		}

		return cdf;
	}

	// Log-Normal Inverse Cumulative Distribution Function
	// Three parameter
	public static double logNormalThreeParInverseCDF(double alpha, double beta, double gamma, double prob) {
		return logNormalInverseCDF(alpha, beta, gamma, prob);
	}

	// Three parameter log-normal mean
	public static double logNormalThreeParMean(double alpha, double beta, double gamma) {
		return gamma * Math.exp(beta * beta / 2.0D) + alpha;
	}

	// Three parameter log-normal median
	public static double logNormalThreeParMedian(double alpha, double gamma) {
		return gamma + alpha;
	}

	// Three parameter log-normal mode
	public static double logNormalThreeParMode(double alpha, double beta, double gamma) {
		return gamma * Math.exp(-beta * beta) + alpha;
	}

	// LogNormal order statistic medians (n points)
	// Three parametrs
	public static double[] logNormalThreeParOrderStatisticMedians(double alpha, double beta, double gamma, int n) {
		return Stat.logNormalOrderStatisticMedians(alpha, beta, gamma, n);
	}

	// Three parameter log-normal probability density function
	public static double logNormalThreeParPDF(double alpha, double beta, double gamma, double x) {
		if (beta < 0) throw new IllegalArgumentException("The parameter beta, " + beta + ", must be greater than or equal to zero");
		if (x <= alpha) {
			return 0.0D;
		} else {
			return Math.exp(-0.5D * Fmath.square(Math.log((x - alpha) / gamma) / beta)) / ((x - gamma) * beta * Math.sqrt(2.0D * Math.PI));
		}
	}

	// Returns an array of three parameter log-normal random deviates - clock seed
	public static double[] logNormalThreeParRand(double alpha, double beta, double gamma, int n) {
		if (n <= 0) throw new IllegalArgumentException("The number of random deviates required, " + n + ", must be greater than zero");
		if (beta < 0) throw new IllegalArgumentException("The parameter beta, " + beta + ", must be greater than or equal to zero");
		PsRandom psr = new PsRandom();
		return psr.logNormalThreeParArray(alpha, beta, gamma, n);
	}

	// Returns an array of three parameter log-normal random deviates - user supplied seed
	public static double[] logNormalThreeParRand(double alpha, double beta, double gamma, int n, long seed) {
		if (n <= 0) throw new IllegalArgumentException("The number of random deviates required, " + n + ", must be greater than zero");
		if (beta < 0) throw new IllegalArgumentException("The parameter beta, " + beta + ", must be greater than or equal to zero");
		PsRandom psr = new PsRandom(seed);
		return psr.logNormalThreeParArray(alpha, beta, gamma, n);
	}

	// Three parameter log-normal standard deviation
	public static double logNormalThreeParStandardDeviation(double alpha, double beta, double gamma) {
		return logNormalThreeParStandDev(alpha, beta, gamma);
	}

	// Three parameter log-normal standard deviation
	public static double logNormalThreeParStandDev(double alpha, double beta, double gamma) {
		double beta2 = beta * beta;
		return Math.sqrt((Math.exp(beta2) - 1.0D) * Math.exp(2.0D * Math.log(gamma) + beta2));
	}

	public static double logNormalTwoParCDF(double mu, double sigma, double upperLimit) {
		return logNormalCDF(mu, sigma, upperLimit);
	}

	public static double logNormalTwoParCDF(double mu, double sigma, double lowerLimit, double upperLimit) {
		return logNormalCDF(mu, sigma, lowerLimit, upperLimit);
	}

	// Log-Normal Inverse Cumulative Distribution Function
	// Two parameter
	public static double logNormaltwoParInverseCDF(double mu, double sigma, double prob) {
		double alpha = 0.0;
		double beta = sigma;
		double gamma = Math.exp(mu);

		return logNormalInverseCDF(alpha, beta, gamma, prob);
	}

	public static double logNormalTwoParMean(double mu, double sigma) {
		return Math.exp(mu + sigma * sigma / 2.0D);
	}

	public static double logNormalTwoParMedian(double mu) {
		return Math.exp(mu);
	}

	public static double logNormalTwoParMode(double mu, double sigma) {
		return Math.exp(mu - sigma * sigma);
	}

	// LogNormal order statistic medians (n points)
	// Two parametrs
	public static double[] logNormalTwoParOrderStatisticMedians(double mu, double sigma, int n) {
		return Stat.logNormalOrderStatisticMedians(mu, sigma, n);
	}

	public static double logNormalTwoParPDF(double mu, double sigma, double x) {
		return logNormalPDF(mu, sigma, x);
	}

	public static double[] logNormalTwoParRand(double mu, double sigma, int n) {
		return logNormalRand(mu, sigma, n);
	}

	public static double[] logNormalTwoParRand(double mu, double sigma, int n, long seed) {
		return logNormalRand(mu, sigma, n, seed);
	}

	public static double logNormalTwoParStandardDeviation(double mu, double sigma) {
		return logNormalTwoParStandDev(mu, sigma);
	}

	public static double logNormalTwoParStandDev(double mu, double sigma) {
		double sigma2 = sigma * sigma;
		return Math.sqrt((Math.exp(sigma2) - 1.0D) * Math.exp(2.0D * mu + sigma2));
	}

	// Lorentzian probability density function
	public static double lorentzian(double mu, double gamma, double x) {
		double arg = gamma / 2.0D;
		return (1.0D / Math.PI) * arg / (Fmath.square(mu - x) + arg * arg);
	}

	// Lorentzian cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double lorentzianCDF(double mu, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (upperlimit - mu) / (gamma / 2.0D);
		double arg2 = (lowerlimit - mu) / (gamma / 2.0D);
		return (1.0D / Math.PI) * (Math.atan(arg1) - Math.atan(arg2));
	}

	// Lorentzian Inverse Cumulative Density Function
	public static double lorentzianInverseCDF(double mu, double gamma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = Double.NEGATIVE_INFINITY;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu + gamma * Math.tan(Math.PI * (prob - 0.5)) / 2.0;
			}
		}

		return icdf;
	}

	// Lorentzian order statistic medians (n points)
	public static double[] lorentzianOrderStatisticMedians(double mu, double gamma, int n) {
		double[] losm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			losm[i] = Stat.lorentzianInverseCDF(mu, gamma, uosm[i]);
		}
		return losm;
	}

	// Lorentzian probability density function
	public static double lorentzianPDF(double mu, double gamma, double x) {
		double arg = gamma / 2.0D;
		return (1.0D / Math.PI) * arg / (Fmath.square(mu - x) + arg * arg);
	}

	// Lorentzian cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	public static double lorentzianProb(double mu, double gamma, double upperlimit) {
		double arg = (upperlimit - mu) / (gamma / 2.0D);
		return (1.0D / Math.PI) * (Math.atan(arg) + Math.PI / 2.0);
	}

	// Lorentzian cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double lorentzianProb(double mu, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (upperlimit - mu) / (gamma / 2.0D);
		double arg2 = (lowerlimit - mu) / (gamma / 2.0D);
		return (1.0D / Math.PI) * (Math.atan(arg1) - Math.atan(arg2));
	}

	// Returns an array of Lorentzian random deviates - clock seed
	// mu  =  the mean, gamma = half-height width, length of array
	public static double[] lorentzianRand(double mu, double gamma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.tan((rr.nextDouble() - 0.5) * Math.PI);
			ran[i] = ran[i] * gamma / 2.0 + mu;
		}
		return ran;
	}

	// Returns an array of Lorentzian random deviates - user provided seed
	// mu  =  the mean, gamma = half-height width, length of array
	public static double[] lorentzianRand(double mu, double gamma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.tan((rr.nextDouble() - 0.5) * Math.PI);
			ran[i] = ran[i] * gamma / 2.0 + mu;
		}
		return ran;
	}

	// Anscombe test for a lower outlier as Vector
	public static Vector<Object> lowerOutliersAnscombe(BigDecimal[] values, BigDecimal constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a lower outlier as Vector
	public static Vector<Object> lowerOutliersAnscombe(BigInteger[] values, BigInteger constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a lower outlier as Vector
	public static Vector<Object> lowerOutliersAnscombe(double[] values, double constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a lower outlier
	public static ArrayList<Object> lowerOutliersAnscombeAsArrayList(BigDecimal[] values, BigDecimal constant) {

		Stat am = new Stat(values);
		BigDecimal[] copy0 = am.getArray_as_BigDecimal();
		BigDecimal[] copy1 = am.getArray_as_BigDecimal();
		int nValues = values.length;
		ArrayList<Object> outers = new ArrayList<Object>();
		int nOutliers = 0;
		boolean test = true;
		while (test) {
			BigDecimal mean = am.mean_as_BigDecimal();
			BigDecimal variance = am.variance_as_BigDecimal();
			BigDecimal min = am.getMinimum_as_BigDecimal();
			int minIndex = am.getMinimumIndex();
			BigDecimal statistic = (mean.subtract(min)).divide(variance, BigDecimal.ROUND_HALF_UP);
			if (statistic.compareTo(constant.multiply(constant)) == 1) {
				outers.add(min);
				outers.add(new Integer(minIndex));
				nOutliers++;
				copy1 = new BigDecimal[nValues - 1];
				for (int i = minIndex; i < nValues - 1; i++)
					copy1[i] = copy0[i + 1];

				nValues--;
				am = new Stat(Conv.copy(copy1));
			} else {
				mean = null;
				variance = null;
				statistic = null;
				copy0 = null;
				test = false;
			}
		}

		BigDecimal[] outliers = null;
		int[] outIndices = null;

		if (nOutliers > 0) {
			outliers = new BigDecimal[nOutliers];
			outIndices = new int[nOutliers];
			for (int i = 0; i < nOutliers; i++) {
				outliers[i] = ((BigDecimal) outers.get(2 * i));
				outIndices[i] = ((Integer) outers.get(2 * i + 1)).intValue();
			}
		}

		ArrayList<Object> ret = new ArrayList<Object>();
		ret.add(new Integer(nOutliers));
		ret.add(outliers);
		ret.add(outIndices);
		ret.add(copy1);
		return ret;
	}

	// Anscombe test for a lower outlier
	public static ArrayList<Object> lowerOutliersAnscombeAsArrayList(BigInteger[] values, BigInteger constant) {
		ArrayMaths am = new ArrayMaths(values);
		BigDecimal[] bd = am.getArray_as_BigDecimal();
		BigDecimal cd = new BigDecimal(constant);
		return Stat.lowerOutliersAnscombeAsArrayList(bd, cd);
	}

	// Anscombe test for a lower outlier
	public static ArrayList<Object> lowerOutliersAnscombeAsArrayList(double[] values, double constant) {

		Stat am = new Stat(values);
		double[] copy0 = am.getArray_as_double();
		double[] copy1 = am.getArray_as_double();
		int nValues = values.length;
		ArrayList<Object> outers = new ArrayList<Object>();
		int nOutliers = 0;
		boolean test = true;

		while (test) {
			double mean = am.mean_as_double();
			double standDev = am.standardDeviation_as_double();
			double min = am.getMinimum_as_double();
			int minIndex = am.getMinimumIndex();
			double statistic = (mean - min) / standDev;
			if (statistic > constant) {
				outers.add(new Double(min));
				outers.add(new Integer(minIndex));
				nOutliers++;
				copy1 = new double[nValues - 1];
				for (int i = minIndex; i < nValues - 1; i++)
					copy1[i] = copy0[i + 1];

				nValues--;
				am = new Stat(Conv.copy(copy1));
			} else {
				test = false;
			}
		}

		double[] outliers = null;
		int[] outIndices = null;

		if (nOutliers > 0) {
			outliers = new double[nOutliers];
			outIndices = new int[nOutliers];
			for (int i = 0; i < nOutliers; i++) {
				outliers[i] = ((Double) outers.get(2 * i)).doubleValue();
				outIndices[i] = ((Integer) outers.get(2 * i + 1)).intValue();
			}
		}

		ArrayList<Object> ret = new ArrayList<Object>();
		ret.add(new Integer(nOutliers));
		ret.add(outliers);
		ret.add(outIndices);
		ret.add(copy1);
		return ret;
	}

	// Anscombe test for a lower outlier - output as Vector
	public static Vector<Object> lowerOutliersAnscombeAsVector(BigDecimal[] values, BigDecimal constant) {
		ArrayList<Object> res = Stat.lowerOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// Anscombe test for a lower outlier - output as Vector
	public static Vector<Object> lowerOutliersAnscombeAsVector(BigInteger[] values, BigInteger constant) {
		ArrayList<Object> res = Stat.lowerOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// Anscombe test for a lower outlier - output as Vector
	public static Vector<Object> lowerOutliersAnscombeAsVector(double[] values, double constant) {
		ArrayList<Object> res = Stat.lowerOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// Arithmetic mean of a 1D array of BigDecimal, aa
	public static BigDecimal mean(BigDecimal[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sum = sum.add(aa[i]);
		}
		return sum.divide(new BigDecimal((double) n), BigDecimal.ROUND_HALF_UP);
	}

	// Weighted arithmetic mean of a 1D array of BigDecimal, aa
	public static BigDecimal mean(BigDecimal[] aa, BigDecimal[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		BigDecimal[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_BigDecimal();
		}

		BigDecimal sumx = BigDecimal.ZERO;
		BigDecimal sumw = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sumx = sumx.add(aa[i].multiply(weight[i]));
			sumw = sumw.add(weight[i]);
		}
		sumx = sumx.divide(sumw, BigDecimal.ROUND_HALF_UP);
		sumw = null;
		weight = null;
		return sumx;
	}

	// Arithmetic mean of a 1D array of BigInteger, aa
	public static BigDecimal mean(BigInteger[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal bi = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			bi = new BigDecimal(aa[i]);
			sum = sum.add(bi);
		}
		bi = null;
		return sum.divide(new BigDecimal((double) n), BigDecimal.ROUND_HALF_UP);
	}

	// Weighted arithmetic mean of a 1D array of BigInteger, aa
	public static BigDecimal mean(BigInteger[] aa, BigInteger[] ww) {
		ArrayMaths amaa = new ArrayMaths(aa);
		ArrayMaths amww = new ArrayMaths(ww);

		return mean(amaa.array_as_BigDecimal(), amww.array_as_BigDecimal());
	}

	// Arithmetic mean of a 1D array of byte, aa
	public static double mean(byte[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// Arithmetic mean of a 1D array of Complex, aa
	public static Complex mean(Complex[] aa) {
		int n = aa.length;
		Complex sum = new Complex(0.0D, 0.0D);
		for (int i = 0; i < n; i++) {
			sum = sum.plus(aa[i]);
		}
		return sum.over(n);
	}

	// Weighted arithmetic mean of a 1D array of Complex, aa
	public static Complex mean(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		Complex[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_Complex();
		}
		Complex sumx = Complex.zero();
		Complex sumw = Complex.zero();
		for (int i = 0; i < n; i++) {
			sumx = sumx.plus(aa[i].times(weight[i]));
			sumw = sumw.plus(weight[i]);
		}
		return sumx.over(sumw);
	}

	// Arithmetic mean of a 1D array of doubles, aa
	public static double mean(double[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// WEIGHTED ARITHMETIC MEANS (STATIC)
	// Weighted arithmetic mean of a 1D array of doubles, aa
	public static double mean(double[] aa, double[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		double[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array();
		}
		double sumx = 0.0D;
		double sumw = 0.0D;
		for (int i = 0; i < n; i++) {
			sumx += aa[i] * weight[i];
			sumw += weight[i];
		}
		return sumx / sumw;
	}

	// Arithmetic mean of a 1D array of floats, aa
	public static float mean(float[] aa) {
		int n = aa.length;
		float sum = 0.0F;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// Weighted arithmetic mean of a 1D array of floats, aa
	public static float mean(float[] aa, float[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		float[] weight = Conv.copy(ww);
		if (Stat.weightingOptionS) {
			ArrayMaths am = new ArrayMaths(ww);
			am = am.pow(2);
			am = am.invert();
			weight = am.array_as_float();
		}

		float sumx = 0.0F;
		float sumw = 0.0F;
		for (int i = 0; i < n; i++) {
			sumx += aa[i] * weight[i];
			sumw += weight[i];
		}
		return sumx / sumw;
	}

	// Arithmetic mean of a 1D array of int, aa
	public static double mean(int[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// Arithmetic mean of a 1D array of int, aa
	public static double mean(long[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// Arithmetic mean of a 1D array of short, aa
	public static double mean(short[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i];
		}
		return sum / n;
	}

	// Confidence limit of a mean
	// Static method
	public static double[] meanConfidenceLimits(double mean, double sd, double prob) {
		double[] cl = new double[2];
		double probn = prob / 2.0 + 0.5;
		double z = Stat.gaussianInverseCDF(mean, sd, probn);
		cl[0] = 2.0 * mean - z;
		cl[1] = z;
		return cl;
	}

	// STANDARD ERROR OF THE MEAN

	// Median of a 1D array of BigDecimal, aa
	public static BigDecimal median(BigDecimal[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		BigDecimal med = BigDecimal.ZERO;
		ArrayMaths bm = new ArrayMaths(aa);
		bm.sort();
		BigDecimal[] bb = bm.getArray_as_BigDecimal();
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1].add(bb[nOverTwo])).divide(new BigDecimal(2.0D), BigDecimal.ROUND_HALF_UP);
		}
		bb = null;
		return med;
	}

	// Median of a 1D array of BigInteger, aa
	public static BigInteger median(BigInteger[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		BigInteger med = BigInteger.ZERO;
		ArrayMaths bm = new ArrayMaths(aa);
		bm.sort();
		BigInteger[] bb = bm.getArray_as_BigInteger();
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1].add(bb[nOverTwo])).divide(new BigInteger("2"));
		}
		bb = null;
		return med;
	}

	// Median of a 1D array of doubles, aa
	public static double median(double[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		double med = 0.0D;
		double[] bb = Fmath.selectionSort(aa);
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1] + bb[nOverTwo]) / 2.0D;
		}

		return med;
	}

	// Median of a 1D array of floats, aa
	public static float median(float[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		float med = 0.0F;
		float[] bb = Fmath.selectionSort(aa);
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1] + bb[nOverTwo]) / 2.0F;
		}

		return med;
	}

	// Median of a 1D array of int, aa
	public static double median(int[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		double med = 0.0D;
		int[] bb = Fmath.selectionSort(aa);
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1] + bb[nOverTwo]) / 2.0D;
		}

		return med;
	}

	// Median of a 1D array of long, aa
	public static double median(long[] aa) {
		int n = aa.length;
		int nOverTwo = n / 2;
		double med = 0.0D;
		long[] bb = Fmath.selectionSort(aa);
		if (Fmath.isOdd(n)) {
			med = bb[nOverTwo];
		} else {
			med = (bb[nOverTwo - 1] + bb[nOverTwo]) / 2.0D;
		}

		return med;
	}

	// Median skewness of a 1D array of BigDecimal
	public static double medianSkewness(BigDecimal[] aa) {
		BigDecimal mean = Stat.mean(aa);
		BigDecimal median = Stat.median(aa);
		double sd = Stat.standardDeviation(aa);
		return 3.0 * (mean.subtract(median)).doubleValue() / sd;
	}

	// Median skewness of a 1D array of doubles
	public static double medianSkewness(double[] aa) {
		double mean = Stat.mean(aa);
		double median = Stat.median(aa);
		double sd = Stat.standardDeviation(aa);
		return 3.0 * (mean - median) / sd;
	}

	// Median skewness of a 1D array of floats
	public static float medianSkewness(float[] aa) {
		float mean = Stat.mean(aa);
		float median = Stat.median(aa);
		float sd = Stat.standardDeviation(aa);
		return 3.0F * (mean - median) / sd;
	}

	// Median skewness of a 1D array of int
	public static double medianSkewness(int[] aa) {
		double mean = Stat.mean(aa);
		double median = Stat.median(aa);
		double sd = Stat.standardDeviation(aa);
		return 3.0 * (mean - median) / sd;
	}

	// Median skewness of a 1D array of long
	public static double medianSkewness(long[] aa) {
		double mean = Stat.mean(aa);
		double median = Stat.median(aa);
		double sd = Stat.standardDeviation(aa);
		return 3.0 * (mean - median) / sd;
	}

	// Moment skewness of a 1D array of BigDecimal
	public static double momentSkewness(BigDecimal[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal mean = Stat.mean(aa);
		double sd = Stat.standardDeviation(aa);
		for (int i = 0; i < n; i++) {
			BigDecimal hold = aa[i].subtract(mean);
			sum = sum.add(hold.multiply(hold.multiply(hold)));
		}
		sum = sum.multiply(new BigDecimal(1.0 / denom));
		return sum.doubleValue() / Math.pow(sd, 3);
	}

	// SKEWNESS
	// Static Methods
	// Moment skewness of a 1D array of doubles
	public static double momentSkewness(double[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 3);
		}
		sum = sum / denom;
		return sum / Math.pow(Stat.standardDeviation(aa), 3);
	}

	// Moment skewness of a 1D array of floats
	public static float momentSkewness(float[] aa) {
		int n = aa.length;
		float denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		float sum = 0.0F;
		float mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 3);
		}
		sum = sum / denom;
		return sum / ((float) Math.pow(Stat.standardDeviation(aa), 3));
	}

	// Moment skewness of a 1D array of int
	public static double momentSkewness(int[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 3);
		}
		sum = sum / denom;
		return sum / Math.pow(Stat.standardDeviation(aa), 3);
	}

	// Moment skewness of a 1D array of long
	public static double momentSkewness(long[] aa) {
		int n = aa.length;
		double denom = (n - 1);
		if (Stat.nFactorOptionS) denom = n;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Math.pow((aa[i] - mean), 3);
		}
		sum = sum / denom;
		return sum / Math.pow(Stat.standardDeviation(aa), 3);
	}

	// Gaussian (normal) probability density function
	// mean  =  the mean, sd = standard deviation
	public static double normal(double mean, double sd, double x) {
		return Math.exp(-Fmath.square((x - mean) / sd) / 2.0) / (sd * Math.sqrt(2.0D * Math.PI));
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mean  =  the mean, sd = standard deviation
	public static double normalCDF(double mean, double sd, double upperlimit) {
		double prob = Double.NaN;
		if (upperlimit == Double.POSITIVE_INFINITY) {
			prob = 1.0;
		} else {
			if (upperlimit == Double.NEGATIVE_INFINITY) {
				prob = 0.0;
			} else {
				double arg = (upperlimit - mean) / (sd * Math.sqrt(2.0));
				prob = (1.0D + Stat.erf(arg)) / 2.0D;
			}
		}
		if (Fmath.isNaN(prob)) {
			if (upperlimit > mean) {
				prob = 1.0;
			} else {
				prob = 0.0;
			}
		}
		return prob;
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mean  =  the mean, sd = standard deviation
	public static double normalCDF(double mean, double sd, double lowerlimit, double upperlimit) {
		return Stat.normalCDF(mean, sd, upperlimit) - Stat.normalCDF(mean, sd, lowerlimit);
	}

	// Gaussian Inverse Cumulative Distribution Function
	// Standardized
	public static double normalInverseCDF(double prob) {
		return gaussianInverseCDF(0.0D, 1.0D, prob);
	}

	// COVARIANCE

	// Gaussian Inverse Cumulative Distribution Function
	public static double normalInverseCDF(double mean, double sd, double prob) {
		return gaussianInverseCDF(mean, sd, prob);
	}

	public static double[] normalOrderStatisticMedians(double mean, double sigma, int n) {
		return Stat.gaussianOrderStatisticMedians(mean, sigma, n);
	}

	public static double[] normalOrderStatisticMedians(int n) {
		return Stat.gaussianOrderStatisticMedians(0.0, 1.0, n);
	}

	// Gaussian (normal) probability density function
	// mean  =  the mean, sd = standard deviation
	public static double normalPDF(double mean, double sd, double x) {
		return Math.exp(-Fmath.square((x - mean) / sd) / 2.0) / (sd * Math.sqrt(2.0D * Math.PI));
	}

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value less than the upperlimit
	// mean  =  the mean, sd = standard deviation
	public static double normalProb(double mean, double sd, double upperlimit) {
		if (upperlimit == Double.POSITIVE_INFINITY) {
			return 1.0;
		} else {
			if (upperlimit == Double.NEGATIVE_INFINITY) {
				return 0.0;
			} else {
				double arg = (upperlimit - mean) / (sd * Math.sqrt(2.0));
				return (1.0D + Stat.erf(arg)) / 2.0D;
			}
		}
	}

	// CORRELATION COEFFICIENT

	// Gaussian (normal) cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	// mean  =  the mean, sd = standard deviation
	public static double normalProb(double mean, double sd, double lowerlimit, double upperlimit) {
		return Stat.normalCDF(mean, sd, upperlimit) - Stat.normalCDF(mean, sd, lowerlimit);
	}

	// Returns an array of Gaussian (normal) random deviates - clock seed
	// mean  =  the mean, sd = standard deviation, length of array
	public static double[] normalRand(double mean, double sd, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = rr.nextGaussian();
		}
		ran = Stat.standardize(ran);
		for (int i = 0; i < n; i++) {
			ran[i] = ran[i] * sd + mean;
		}
		return ran;
	}

	// Returns an array of Gaussian (normal) random deviates - user provided seed
	// mean  =  the mean, sd = standard deviation, length of array
	public static double[] normalRand(double mean, double sd, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = rr.nextGaussian();
		}
		ran = Stat.standardize(ran);
		for (int i = 0; i < n; i++) {
			ran[i] = ran[i] * sd + mean;
		}
		return ran;
	}

	// Pareto probability density function
	public static double pareto(double alpha, double beta, double x) {
		double y = 0.0D;
		if (x >= beta) {
			y = alpha * Math.pow(beta, alpha) / Math.pow(x, alpha + 1.0D);
		}
		return y;
	}

	// Pareto cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double paretoCDF(double alpha, double beta, double upperlimit) {
		double y = 0.0D;
		if (upperlimit >= beta) y = 1.0D - Math.pow(beta / upperlimit, alpha);
		return y;
	}

	// Pareto cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double paretoCDF(double alpha, double beta, double lowerlimit, double upperlimit) {
		double term1 = 0.0D, term2 = 0.0D;
		if (lowerlimit >= beta) term1 = -Math.pow(beta / lowerlimit, alpha);
		if (upperlimit >= beta) term2 = -Math.pow(beta / upperlimit, alpha);
		return term2 - term1;
	}

	// Pareto Inverse Cumulative Density Function
	public static double paretoInverseCDF(double alpha, double beta, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = beta;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = beta / Math.pow((1.0 - prob), 1.0 / alpha);
			}
		}

		return icdf;
	}

	// Pareto mean
	public static double paretoMean(double alpha, double beta) {
		double y = Double.NaN;
		if (alpha > 1.0D) y = alpha * beta / (alpha - 1);
		return y;
	}

	// Pareto mode
	public static double paretoMode(double beta) {
		return beta;
	}

	// Pareto order statistic medians (n points)
	public static double[] paretoOrderStatisticMedians(double alpha, double beta, int n) {
		double[] posm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			posm[i] = Stat.inverseParetoCDF(alpha, beta, uosm[i]);
		}
		return posm;
	}

	// Pareto probability density function
	public static double paretoPDF(double alpha, double beta, double x) {
		double y = 0.0D;
		if (x >= beta) {
			y = alpha * Math.pow(beta, alpha) / Math.pow(x, alpha + 1.0D);
		}
		return y;
	}

	// Pareto cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double paretoProb(double alpha, double beta, double upperlimit) {
		double y = 0.0D;
		if (upperlimit >= beta) y = 1.0D - Math.pow(beta / upperlimit, alpha);
		return y;
	}

	// Pareto cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double paretoProb(double alpha, double beta, double lowerlimit, double upperlimit) {
		double term1 = 0.0D, term2 = 0.0D;
		if (lowerlimit >= beta) term1 = -Math.pow(beta / lowerlimit, alpha);
		if (upperlimit >= beta) term2 = -Math.pow(beta / upperlimit, alpha);
		return term2 - term1;
	}

	// Returns an array of Pareto random deviates - clock seed
	public static double[] paretoRand(double alpha, double beta, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow(1.0D - rr.nextDouble(), -1.0D / alpha) * beta;
		}
		return ran;
	}

	// Returns an array of Pareto random deviates - user supplied seed
	public static double[] paretoRand(double alpha, double beta, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow(1.0D - rr.nextDouble(), -1.0D / alpha) * beta;
		}
		return ran;
	}

	// Pareto standard deviation
	public static double paretoStandardDeviation(double alpha, double beta) {
		double y = Double.NaN;
		if (alpha > 1.0D) y = alpha * Fmath.square(beta) / (Fmath.square(alpha - 1) * (alpha - 2));
		return y;
	}

	// Pareto standard deviation
	public static double paretoStandDev(double alpha, double beta) {
		double y = Double.NaN;
		if (alpha > 1.0D) y = alpha * Fmath.square(beta) / (Fmath.square(alpha - 1) * (alpha - 2));
		return y;
	}

	// Poisson Probability Density Function
	// k is an integer greater than or equal to zero
	// mean  = mean of the Poisson distribution
	public static double poisson(int k, double mean) {
		if (k < 0) throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
		return Math.pow(mean, k) * Math.exp(-mean) / Stat.factorial((double) k);
	}

	// Poisson Cumulative Distribution Function
	// probability that a number of Poisson random events will occur between 0 and k (inclusive)
	// k is an integer greater than equal to 1
	// mean  = mean of the Poisson distribution
	public static double poissonCDF(int k, double mean) {
		if (k < 1) throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
		return Stat.incompleteGammaComplementary(k, mean);
	}

	// Poisson Probability Density Function
	// k is an integer greater than or equal to zero
	// mean  = mean of the Poisson distribution
	public static double poissonPDF(int k, double mean) {
		if (k < 0) throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
		return Math.pow(mean, k) * Math.exp(-mean) / Stat.factorial((double) k);
	}

	// Poisson Cumulative Distribution Function
	// probability that a number of Poisson random events will occur between 0 and k (inclusive)
	// k is an integer greater than equal to 1
	// mean  = mean of the Poisson distribution
	public static double poissonProb(int k, double mean) {
		if (k < 1) throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
		return Stat.incompleteGammaComplementary(k, mean);
	}

	// Returns an array of Poisson random deviates - clock seed
	// mean  =  the mean,  n = length of array
	// follows the ideas of Numerical Recipes
	public static double[] poissonRand(double mean, int n) {

		Random rr = new Random();
		double[] ran = poissonRandCalc(rr, mean, n);
		return ran;
	}

	// Returns an array of Poisson random deviates - user provided seed
	// mean  =  the mean,  n = length of array
	// follows the ideas of Numerical Recipes
	public static double[] poissonRand(double mean, int n, long seed) {

		Random rr = new Random(seed);
		double[] ran = poissonRandCalc(rr, mean, n);
		return ran;
	}

	// Calculates and returns an array of Poisson random deviates
	private static double[] poissonRandCalc(Random rr, double mean, int n) {
		double[] ran = new double[n];
		double oldm = -1.0D;
		double expt = 0.0D;
		double em = 0.0D;
		double term = 0.0D;
		double sq = 0.0D;
		double lnMean = 0.0D;
		double yDev = 0.0D;

		if (mean < 12.0D) {
			for (int i = 0; i < n; i++) {
				if (mean != oldm) {
					oldm = mean;
					expt = Math.exp(-mean);
				}
				em = -1.0D;
				term = 1.0D;
				do {
					++em;
					term *= rr.nextDouble();
				} while (term > expt);
				ran[i] = em;
			}
		} else {
			for (int i = 0; i < n; i++) {
				if (mean != oldm) {
					oldm = mean;
					sq = Math.sqrt(2.0D * mean);
					lnMean = Math.log(mean);
					expt = lnMean - Stat.logGamma(mean + 1.0D);
				}
				do {
					do {
						yDev = Math.tan(Math.PI * rr.nextDouble());
						em = sq * yDev + mean;
					} while (em < 0.0D);
					em = Math.floor(em);
					term = 0.9D * (1.0D + yDev * yDev) * Math.exp(em * lnMean - Stat.logGamma(em + 1.0D) - expt);
				} while (rr.nextDouble() > term);
				ran[i] = em;
			}
		}
		return ran;
	}

	// Returns the A(t|n) distribution probabilty
	public static double probAtn(double tValue, int df) {
		double ddf = df;
		double x = ddf / (ddf + tValue * tValue);
		return 1.0D - Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, x);
	}

	// Returns the P-value for a given Student's t value and degrees of freedom
	public static double pValue(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		double abst = Math.abs(tValue);
		return 1.0 - Stat.studentTcdf(-abst, abst, df);
	}

	// HISTOGRAMS

	// Quartile skewness of a 1D array of BigDecimal
	public static BigDecimal quartileSkewness(BigDecimal[] aa) {
		int n = aa.length;
		BigDecimal median50 = Stat.median(aa);
		int start1 = 0;
		int start2 = 0;
		int end1 = n / 2 - 1;
		int end2 = n - 1;
		if (Fmath.isOdd(n)) {
			start2 = end1 + 2;
		} else {
			start2 = end1 + 1;
		}
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] first = am.subarray_as_BigDecimal(start1, end1);
		BigDecimal[] last = am.subarray_as_BigDecimal(start2, end2);
		BigDecimal median25 = Stat.median(first);
		BigDecimal median75 = Stat.median(last);
		BigDecimal ret1 = (median25.subtract(median50.multiply(new BigDecimal(2.0)))).add(median75);
		BigDecimal ret2 = median75.subtract(median25);
		BigDecimal ret = ret1.divide(ret2, BigDecimal.ROUND_HALF_UP);
		if (Fmath.isNaN(ret.doubleValue())) ret = new BigDecimal(1.0D);
		first = null;
		last = null;
		median25 = null;
		median50 = null;
		median75 = null;
		ret1 = null;
		ret2 = null;
		return ret;
	}

	// Quartile skewness of a 1D array of BigInteger
	public static BigDecimal quartileSkewness(BigInteger[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] bd = am.array_as_BigDecimal();
		return Stat.quartileSkewness(bd);
	}

	// Quartile skewness of a 1D array of double
	public static double quartileSkewness(double[] aa) {
		int n = aa.length;
		double median50 = Stat.median(aa);
		int start1 = 0;
		int start2 = 0;
		int end1 = n / 2 - 1;
		int end2 = n - 1;
		if (Fmath.isOdd(n)) {
			start2 = end1 + 2;
		} else {
			start2 = end1 + 1;
		}
		ArrayMaths am = new ArrayMaths(aa);
		double[] first = am.subarray_as_double(start1, end1);
		double[] last = am.subarray_as_double(start2, end2);
		double median25 = Stat.median(first);
		double median75 = Stat.median(last);

		double ret = (median25 - 2.0 * median50 + median75) / (median75 - median25);
		if (Fmath.isNaN(ret)) ret = 1.0;
		return ret;
	}

	// Quartile skewness of a 1D array of float
	public static float quartileSkewness(float[] aa) {
		int n = aa.length;
		float median50 = Stat.median(aa);
		int start1 = 0;
		int start2 = 0;
		int end1 = n / 2 - 1;
		int end2 = n - 1;
		if (Fmath.isOdd(n)) {
			start2 = end1 + 2;
		} else {
			start2 = end1 + 1;
		}
		ArrayMaths am = new ArrayMaths(aa);
		float[] first = am.subarray_as_float(start1, end1);
		float[] last = am.subarray_as_float(start2, end2);
		float median25 = Stat.median(first);
		float median75 = Stat.median(last);

		float ret = (median25 - 2.0F * median50 + median75) / (median75 - median25);
		if (Fmath.isNaN(ret)) ret = 1.0F;
		return ret;
	}

	// Quartile skewness of a 1D array of int
	public static double quartileSkewness(int[] aa) {
		int n = aa.length;
		double median50 = Stat.median(aa);
		int start1 = 0;
		int start2 = 0;
		int end1 = n / 2 - 1;
		int end2 = n - 1;
		if (Fmath.isOdd(n)) {
			start2 = end1 + 2;
		} else {
			start2 = end1 + 1;
		}
		ArrayMaths am = new ArrayMaths(aa);
		double[] first = am.subarray_as_double(start1, end1);
		double[] last = am.subarray_as_double(start2, end2);
		double median25 = Stat.median(first);
		double median75 = Stat.median(last);

		double ret = (median25 - 2.0 * median50 + median75) / (median75 - median25);
		if (Fmath.isNaN(ret)) ret = 1.0;
		return ret;
	}

	// Quartile skewness of a 1D array of long
	public static double quartileSkewness(long[] aa) {
		int n = aa.length;
		double median50 = Stat.median(aa);
		int start1 = 0;
		int start2 = 0;
		int end1 = n / 2 - 1;
		int end2 = n - 1;
		if (Fmath.isOdd(n)) {
			start2 = end1 + 2;
		} else {
			start2 = end1 + 1;
		}
		ArrayMaths am = new ArrayMaths(aa);
		double[] first = am.subarray_as_double(start1, end1);
		double[] last = am.subarray_as_double(start2, end2);
		double median25 = Stat.median(first);
		double median75 = Stat.median(last);

		double ret = (median25 - 2.0 * median50 + median75) / (median75 - median25);
		if (Fmath.isNaN(ret)) ret = 1.0;
		return ret;
	}

	// Rayleigh probability density function
	public static double rayleigh(double beta, double x) {
		double arg = x / beta;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (arg / beta) * Math.exp(-arg * arg / 2.0D) / beta;
		}
		return y;
	}

	// Rayleigh cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double rayleighCDF(double beta, double upperlimit) {
		double arg = (upperlimit) / beta;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-arg * arg / 2.0D);
		return y;
	}

	// Rayleigh cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double rayleighCDF(double beta, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit) / beta;
		double arg2 = (upperlimit) / beta;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-arg1 * arg1 / 2.0D);
		if (arg2 >= 0.0D) term2 = -Math.exp(-arg2 * arg2 / 2.0D);
		return term2 - term1;
	}

	// Rayleigh Inverse Cumulative Density Function
	public static double rayleighInverseCDF(double beta, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = 0.0;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = beta * (Math.sqrt(-Math.log(1.0 - prob)));
			}
		}

		return icdf;
	}

	// GAMMA DISTRIBUTION AND GAMMA FUNCTIONS

	// Rayleigh mean
	public static double rayleighMean(double beta) {
		return beta * Math.sqrt(Math.PI / 2.0D);
	}

	// Rayleigh median
	public static double rayleighMedian(double beta) {
		return beta * Math.sqrt(Math.log(2.0D));
	}

	// Rayleigh mode
	public static double rayleighMode(double beta) {
		return beta;
	}

	// Rayleigh order statistic medians (n points)
	public static double[] rayleighOrderStatisticMedians(double beta, int n) {
		double[] rosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			rosm[i] = Stat.inverseRayleighCDF(beta, uosm[i]);
		}
		return rosm;
	}

	// Rayleigh probability density function
	public static double rayleighPDF(double beta, double x) {
		double arg = x / beta;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (arg / beta) * Math.exp(-arg * arg / 2.0D) / beta;
		}
		return y;
	}

	// Rayleigh cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double rayleighProb(double beta, double upperlimit) {
		double arg = (upperlimit) / beta;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-arg * arg / 2.0D);
		return y;
	}

	// Rayleigh cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double rayleighProb(double beta, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit) / beta;
		double arg2 = (upperlimit) / beta;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-arg1 * arg1 / 2.0D);
		if (arg2 >= 0.0D) term2 = -Math.exp(-arg2 * arg2 / 2.0D);
		return term2 - term1;
	}

	// Returns an array of Rayleigh random deviates - clock seed
	// beta = scale parameter, n = length of array
	public static double[] rayleighRand(double beta, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.sqrt(-2.0D * Math.log(1.0D - rr.nextDouble())) * beta;
		}
		return ran;
	}

	// Returns an array of Rayleigh random deviates - user supplied seed
	// beta = scale parameter, n = length of array
	public static double[] rayleighRand(double beta, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.sqrt(-2.0D * Math.log(1.0D - rr.nextDouble())) * beta;
		}
		return ran;
	}

	// Rayleigh standard deviation
	public static double rayleighStandardDeviation(double beta) {
		return beta * Math.sqrt(2.0D - Math.PI / 2.0D);
	}

	// Rayleigh standard deviation
	public static double rayleighStandDev(double beta) {
		return beta * Math.sqrt(2.0D - Math.PI / 2.0D);
	}

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	// Retained for backward compatibility
	public static double regIncompleteGamma(double a, double x) {
		return regularisedGammaFunction(a, x);
	}

	// Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
	// Retained for backward compatibility
	public static double regIncompleteGammaComplementary(double a, double x) {
		return complementaryRegularisedGammaFunction(a, x);
	}

	// Regularised Incomplete Beta function
	// Continued Fraction approximation (see Numerical recipies for details of method)
	public static double regularisedBetaFunction(double z, double w, double x) {
		if (x < 0.0D || x > 1.0D) throw new IllegalArgumentException("Argument x, " + x + ", must be lie between 0 and 1 (inclusive)");
		double ibeta = 0.0D;
		if (x == 0.0D) {
			ibeta = 0.0D;
		} else {
			if (x == 1.0D) {
				ibeta = 1.0D;
			} else {
				// Term before continued fraction
				ibeta = Math.exp(Stat.logGamma(z + w) - Stat.logGamma(z) - logGamma(w) + z * Math.log(x) + w * Math.log(1.0D - x));
				// Continued fraction
				if (x < (z + 1.0D) / (z + w + 2.0D)) {
					ibeta = ibeta * Stat.contFract(z, w, x) / z;
				} else {
					// Use symmetry relationship
					ibeta = 1.0D - ibeta * Stat.contFract(w, z, 1.0D - x) / w;
				}
			}
		}
		return ibeta;
	}

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	public static double regularisedGammaFunction(double a, double x) {
		if (a < 0.0D || x < 0.0D) throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");

		boolean oldIgSupress = Stat.igSupress;
		Stat.igSupress = true;
		double igf = 0.0D;

		if (x != 0) {
			if (x < a + 1.0D) {
				// Series representation
				igf = incompleteGammaSer(a, x);
			} else {
				// Continued fraction representation
				igf = incompleteGammaFract(a, x);
			}
			if (igf != igf) igf = 1.0 - Stat.crigfGaussQuad(a, x);
		}
		if (igf < 0.0) igf = 0.0;
		Stat.igSupress = oldIgSupress;
		return igf;
	}

	// Regularised Incomplete Beta function
	// Continued Fraction approximation (see Numerical recipies for details of method)
	public static double regularizedBetaFunction(double z, double w, double x) {
		return regularisedBetaFunction(z, w, x);
	}

	// Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
	public static double regularizedGammaFunction(double a, double x) {
		return regularisedGammaFunction(a, x);
	}

	// RENYI ENTROPY
	// Renyi Entropy returned as bits
	public static double renyiEntropy(double[] p, double alpha) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);
		if (alpha < 0.0D) throw new IllegalArgumentException("alpha, " + alpha + ", must be greater than or equal to 0");
		double entropy = 0.0;
		if (alpha == 0.0D) {
			entropy = Fmath.log2(p.length);
		} else {
			if (alpha == 1.0D) {
				entropy = Stat.shannonEntropy(p);
			} else {
				if (Fmath.isPlusInfinity(alpha)) {
					entropy = -Fmath.log2(max);
				} else {
					if (alpha <= 3000) {
						am = am.pow(alpha);
						boolean testUnderFlow = false;
						if (am.getMaximum_as_double() == Double.MIN_VALUE) testUnderFlow = true;
						entropy = Fmath.log2(am.getSum_as_double()) / (1.0D - alpha);
						if (Fmath.isPlusInfinity(entropy) || testUnderFlow) {
							entropy = -Fmath.log2(max);
							double entropyMin = entropy;
							System.out.println("Stat: renyiEntropy/renyiEntopyBit: underflow or overflow in calculating the entropy");
							boolean test1 = true;
							boolean test2 = true;
							boolean test3 = true;
							int iter = 0;
							double alpha2 = alpha / 2.0;
							double entropy2 = 0.0;
							while (test3) {
								while (test1) {
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Fmath.log2(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										alpha2 /= 2.0D;
										iter++;
										if (iter == 100000) {
											test1 = false;
											test2 = false;
										}
									} else {
										test1 = false;
									}
								}
								double alphaTest = alpha2 + 40.0D * alpha / 1000.0D;
								ArrayMaths am3 = new ArrayMaths(p);
								am3 = am3.pow(alphaTest);
								double entropy3 = Fmath.log2(am3.getSum_as_double()) / (1.0D - alphaTest);
								if (!Fmath.isPlusInfinity(entropy3)) {
									test3 = false;
								} else {
									alpha2 /= 2.0D;
								}
							}
							double entropyLast = entropy2;
							double alphaLast = alpha2;
							ArrayList<Double> extrap = new ArrayList<Double>();
							if (test2) {
								double diff = alpha2 / 1000.0D;
								test1 = true;
								while (test1) {
									extrap.add(new Double(alpha2));
									extrap.add(new Double(entropy2));
									entropyLast = entropy2;
									alphaLast = alpha2;
									alpha2 += diff;
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Fmath.log2(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										test1 = false;
										entropy2 = entropyLast;
										alpha2 = alphaLast;
									}
								}
							}
							int nex = extrap.size() / 2 - 20;
							double[] alphaex = new double[nex];
							double[] entroex = new double[nex];
							int ii = -1;
							for (int i = 0; i < nex; i++) {
								alphaex[i] = (extrap.get(++ii)).doubleValue();
								entroex[i] = Math.log((extrap.get(++ii)).doubleValue() - entropyMin);
							}
							Regression reg = new Regression(alphaex, entroex);
							reg.linear();
							double[] param = reg.getCoeff();
							entropy = Math.exp(param[0] + param[1] * alpha) + entropyMin;

							System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
							System.out.println("Lowest calculable value =  " + (Math.exp(entroex[nex - 1]) + entropyMin) + ", alpha = " + alphaex[nex - 1]);
							System.out.println("Minimum entropy value =  " + entropyMin + ", alpha = infinity");
						}
					} else {
						entropy = -Fmath.log2(max);
						System.out.println("Stat: renyiEntropy/renyiEntropyBit: underflow or overflow in calculating the entropy");
						System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
					}
				}
			}
		}
		return entropy;
	}

	// Renyi Entropy returned as bits
	public static double renyiEntropyBit(double[] p, double alpha) {
		return renyiEntropy(p, alpha);
	}

	// Renyi Entropy returned as dits
	public static double renyiEntropyDit(double[] p, double alpha) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);
		if (alpha < 0.0D) throw new IllegalArgumentException("alpha, " + alpha + ", must be greater than or equal to 0");
		double entropy = 0.0;
		if (alpha == 0.0D) {
			entropy = Math.log10(p.length);
		} else {
			if (alpha == 1.0D) {
				entropy = Stat.shannonEntropy(p);
			} else {
				if (Fmath.isPlusInfinity(alpha)) {
					entropy = -Math.log10(max);
				} else {
					if (alpha <= 3000) {
						am = am.pow(alpha);
						boolean testUnderFlow = false;
						if (am.getMaximum_as_double() == Double.MIN_VALUE) testUnderFlow = true;
						entropy = Math.log10(am.getSum_as_double()) / (1.0D - alpha);
						if (Fmath.isPlusInfinity(entropy) || testUnderFlow) {
							entropy = -Math.log10(max);
							double entropyMin = entropy;
							System.out.println("Stat: renyiEntropyDit: underflow or overflow in calculating the entropy");
							boolean test1 = true;
							boolean test2 = true;
							boolean test3 = true;
							int iter = 0;
							double alpha2 = alpha / 2.0;
							double entropy2 = 0.0;
							while (test3) {
								while (test1) {
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Math.log10(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										alpha2 /= 2.0D;
										iter++;
										if (iter == 100000) {
											test1 = false;
											test2 = false;
										}
									} else {
										test1 = false;
									}
								}
								double alphaTest = alpha2 + 40.0D * alpha / 1000.0D;
								ArrayMaths am3 = new ArrayMaths(p);
								am3 = am3.pow(alphaTest);
								double entropy3 = Math.log10(am3.getSum_as_double()) / (1.0D - alphaTest);
								if (!Fmath.isPlusInfinity(entropy3)) {
									test3 = false;
								} else {
									alpha2 /= 2.0D;
								}
							}
							double entropyLast = entropy2;
							double alphaLast = alpha2;
							ArrayList<Double> extrap = new ArrayList<Double>();
							if (test2) {
								double diff = alpha2 / 1000.0D;
								test1 = true;
								while (test1) {
									extrap.add(new Double(alpha2));
									extrap.add(new Double(entropy2));
									entropyLast = entropy2;
									alphaLast = alpha2;
									alpha2 += diff;
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Math.log10(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										test1 = false;
										entropy2 = entropyLast;
										alpha2 = alphaLast;
									}
								}
							}
							int nex = extrap.size() / 2 - 20;
							double[] alphaex = new double[nex];
							double[] entroex = new double[nex];
							int ii = -1;
							for (int i = 0; i < nex; i++) {
								alphaex[i] = (extrap.get(++ii)).doubleValue();
								entroex[i] = Math.log10((extrap.get(++ii)).doubleValue() - entropyMin);
							}
							Regression reg = new Regression(alphaex, entroex);
							reg.linear();
							double[] param = reg.getCoeff();
							entropy = Math.exp(param[0] + param[1] * alpha) + entropyMin;

							System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
							System.out.println("Lowest calculable value =  " + (Math.exp(entroex[nex - 1]) + entropyMin) + ", alpha = " + alphaex[nex - 1]);
							System.out.println("Minimum entropy value =  " + entropyMin + ", alpha = infinity");
						}
					} else {
						entropy = -Math.log10(max);
						System.out.println("Stat: renyiEntropyDit: underflow or overflow in calculating the entropy");
						System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
					}
				}
			}
		}
		return entropy;
	}

	// Renyi Entropy returned as nats
	public static double renyiEntropyNat(double[] p, double alpha) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);
		if (alpha < 0.0D) throw new IllegalArgumentException("alpha, " + alpha + ", must be greater than or equal to 0");
		double entropy = 0.0;
		if (alpha == 0.0D) {
			entropy = Math.log(p.length);
		} else {
			if (alpha == 1.0D) {
				entropy = Stat.shannonEntropy(p);
			} else {
				if (Fmath.isPlusInfinity(alpha)) {
					entropy = -Math.log(max);
				} else {
					if (alpha <= 3000) {
						am = am.pow(alpha);
						boolean testUnderFlow = false;
						if (am.getMaximum_as_double() == Double.MIN_VALUE) testUnderFlow = true;
						entropy = Math.log(am.getSum_as_double()) / (1.0D - alpha);
						if (Fmath.isPlusInfinity(entropy) || testUnderFlow) {
							entropy = -Math.log(max);
							double entropyMin = entropy;
							System.out.println("Stat: renyiEntropyNat: underflow or overflow in calculating the entropy");
							boolean test1 = true;
							boolean test2 = true;
							boolean test3 = true;
							int iter = 0;
							double alpha2 = alpha / 2.0;
							double entropy2 = 0.0;
							while (test3) {
								while (test1) {
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Math.log(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										alpha2 /= 2.0D;
										iter++;
										if (iter == 100000) {
											test1 = false;
											test2 = false;
										}
									} else {
										test1 = false;
									}
								}
								double alphaTest = alpha2 + 40.0D * alpha / 1000.0D;
								ArrayMaths am3 = new ArrayMaths(p);
								am3 = am3.pow(alphaTest);
								double entropy3 = Math.log(am3.getSum_as_double()) / (1.0D - alphaTest);
								if (!Fmath.isPlusInfinity(entropy3)) {
									test3 = false;
								} else {
									alpha2 /= 2.0D;
								}
							}
							double entropyLast = entropy2;
							double alphaLast = alpha2;
							ArrayList<Double> extrap = new ArrayList<Double>();
							if (test2) {
								double diff = alpha2 / 1000.0D;
								test1 = true;
								while (test1) {
									extrap.add(new Double(alpha2));
									extrap.add(new Double(entropy2));
									entropyLast = entropy2;
									alphaLast = alpha2;
									alpha2 += diff;
									ArrayMaths am2 = new ArrayMaths(p);
									am2 = am2.pow(alpha2);
									entropy2 = Math.log(am2.getSum_as_double()) / (1.0D - alpha2);
									if (Fmath.isPlusInfinity(entropy2)) {
										test1 = false;
										entropy2 = entropyLast;
										alpha2 = alphaLast;
									}
								}
							}
							int nex = extrap.size() / 2 - 20;
							double[] alphaex = new double[nex];
							double[] entroex = new double[nex];
							int ii = -1;
							for (int i = 0; i < nex; i++) {
								alphaex[i] = (extrap.get(++ii)).doubleValue();
								entroex[i] = Math.log((extrap.get(++ii)).doubleValue() - entropyMin);
							}
							Regression reg = new Regression(alphaex, entroex);
							reg.linear();
							double[] param = reg.getCoeff();
							entropy = Math.exp(param[0] + param[1] * alpha) + entropyMin;

							System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
							System.out.println("Lowest calculable value =  " + (Math.exp(entroex[nex - 1]) + entropyMin) + ", alpha = " + alphaex[nex - 1]);
							System.out.println("Minimum entropy value =  " + entropyMin + ", alpha = infinity");
						}
					} else {
						entropy = -Math.log(max);
						System.out.println("Stat: renyiEntropyNat: underflow or overflow in calculating the entropy");
						System.out.println("An interpolated entropy of " + entropy + " returned (see documentation for exponential interpolation)");
					}
				}
			}
		}
		return entropy;
	}

	// Reset value of cfMaxIter used in contFract method above
	public static void resetCFmaxIter(int cfMaxIter) {
		Stat.cfMaxIter = cfMaxIter;
	}

	// Reset value of cfTol used in contFract method above
	public static void resetCFtolerance(double cfTol) {
		Stat.cfTol = cfTol;
	}

	// Root mean square (rms) of a 1D array of BigDecimal, aa
	public static double rms(BigDecimal[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sum = sum.add(aa[i].multiply(aa[i]));
		}
		sum = sum.divide((new BigDecimal(n)), BigDecimal.ROUND_HALF_UP);
		double ret = Math.sqrt(sum.doubleValue());
		sum = null;
		return ret;
	}

	// Weighted root mean square (rms) of a 1D array of BigDecimal, aa
	public static double rms(BigDecimal[] aa, BigDecimal[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		BigDecimal sumw = BigDecimal.ZERO;
		BigDecimal[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw = sumw.add(weight[i]);
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sum = sum.add((aa[i].multiply(aa[i])).multiply(weight[i]));
		}
		sum = sum.divide(sumw, BigDecimal.ROUND_HALF_UP);
		double ret = Math.sqrt(sum.doubleValue());
		sum = null;
		weight = null;
		return ret;
	}

	// Root mean square (rms) of a 1D array of BigInteger, aa
	public static double rms(BigInteger[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal bd = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			bd = new BigDecimal(aa[i]);
			sum = sum.add(bd.multiply(bd));
		}
		sum = sum.divide((new BigDecimal(n)), BigDecimal.ROUND_HALF_UP);
		double ret = Math.sqrt(sum.doubleValue());
		bd = null;
		sum = null;
		return ret;
	}

	// Weighted root mean square (rms) of a 1D array of BigInteger, aa
	public static double rms(BigInteger[] aa, BigInteger[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		ArrayMaths amaa = new ArrayMaths(aa);
		ArrayMaths amww = new ArrayMaths(ww);
		return rms(amaa.array_as_BigDecimal(), amww.array_as_BigDecimal());
	}

	// Root mean square (rms) of a 1D array of doubles, aa
	public static double rms(double[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += aa[i] * aa[i];
		}
		return Math.sqrt(sum / n);
	}

	// Weighted root mean square (rms) of a 1D array of doubles, aa
	public static double rms(double[] aa, double[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		double sumw = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw += weight[i];
		}
		double sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += weight[i] * aa[i] * aa[i];
		}
		return Math.sqrt(sum / sumw);
	}

	// Root mean square (rms) of a 1D array of floats, aa
	public static float rms(float[] aa) {
		int n = aa.length;
		float sum = 0.0F;
		for (int i = 0; i < n; i++) {
			sum += aa[i] * aa[i];
		}
		sum /= n;

		return (float) Math.sqrt(sum);
	}

	// Weighted root mean square (rms) of a 1D array of floats, aa
	public static float rms(float[] aa, float[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		double sumw = 0.0F;
		float[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumw += weight[i];
		}
		float sum = 0.0F;
		for (int i = 0; i < n; i++) {
			sum += weight[i] * aa[i] * aa[i];
		}
		return (float) Math.sqrt(sum / sumw);
	}

	// Scale an array of BigDecimals to a new mean and new standard deviation
	public static double[] scale(BigDecimal[] aa, double mean, double sd) {
		double[] bb = Stat.standardize(aa);
		int n = aa.length;
		for (int i = 0; i < n; i++) {
			bb[i] = bb[i] * sd + mean;
		}

		return bb;
	}

	// Scale an array of BigIntegers to a new mean and new standard deviation
	public static double[] scale(BigInteger[] aa, double mean, double sd) {
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] bd = am.getArray_as_BigDecimal();

		return Stat.scale(bd, mean, sd);
	}

	// SCALING DATA
	// Scale an array of doubles to a new mean and new standard deviation
	public static double[] scale(double[] aa, double mean, double sd) {
		double[] bb = Stat.standardize(aa);
		int n = aa.length;
		for (int i = 0; i < n; i++) {
			bb[i] = bb[i] * sd + mean;
		}

		return bb;
	}

	// Scale an array of floats to a new mean and new standard deviation
	public static float[] scale(float[] aa, float mean, float sd) {
		float[] bb = Stat.standardize(aa);
		int n = aa.length;
		for (int i = 0; i < n; i++) {
			bb[i] = bb[i] * sd + mean;
		}

		return bb;
	}

	// Scale an array of longs to a new mean and new standard deviation
	public static double[] scale(int[] aa, double mean, double sd) {
		double[] bb = Stat.standardize(aa);
		int n = aa.length;
		for (int i = 0; i < n; i++) {
			bb[i] = bb[i] * sd + mean;
		}

		return bb;
	}

	// FACTORIALS

	// Scale an array of longs to a new mean and new standard deviation
	public static double[] scale(long[] aa, double mean, double sd) {
		double[] bb = Stat.standardize(aa);
		int n = aa.length;
		for (int i = 0; i < n; i++) {
			bb[i] = bb[i] * sd + mean;
		}

		return bb;
	}

	public static BigDecimal secondQuartile(BigDecimal[] aa) {
		return Stat.median(aa);
	}

	public static BigInteger secondQuartile(BigInteger[] aa) {
		return Stat.median(aa);
	}

	public static double secondQuartile(double[] aa) {
		return Stat.median(aa);
	}

	public static float secondQuartile(float[] aa) {
		return Stat.median(aa);
	}

	public static double secondQuartile(int[] aa) {
		return Stat.median(aa);
	}

	public static double secondQuartile(long[] aa) {
		return Stat.median(aa);
	}

	// Reset the maximum number of iterations allowed in the calculation of the incomplete gamma functions
	public static void setIncGammaMaxIter(int igfiter) {
		Stat.igfiter = igfiter;
	}

	// ERLANG DISTRIBUTION AND ERLANG EQUATIONS

	// Reset the tolerance used in the calculation of the incomplete gamma functions
	public static void setIncGammaTol(double igfeps) {
		Stat.igfeps = igfeps;
	}

	// DENOMINATOR CHOICE (STATIC)
	// Set standard deviation, variance and covariance denominators to n
	public static void setStaticDenominatorToN() {
		Stat.nFactorOptionS = true;
	}

	// Set standard deviation, variance and covariance denominators to n
	public static void setStaticDenominatorToNminusOne() {
		Stat.nFactorOptionS = false;
	}

	// STATIC METHODS
	// WEIGHTING CHOICE (STATIC)
	// Set weights to 'big W' - multiplicative factor
	public static void setStaticWeightsToBigW() {
		Stat.weightingOptionS = false;
	}

	// Set weights to 'little w' - uncertainties
	public static void setStaticWeightsToLittleW() {
		Stat.weightingOptionS = true;
	}

	// SHANNON ENTROPY (STATIC METHODS)
	// Shannon Entropy returned as bits
	public static double shannonEntropy(double[] p) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);

		return am.minusxLog2x().getSum_as_double();
	}

	// Shannon Entropy returned as bits
	public static double shannonEntropyBit(double[] p) {
		return shannonEntropy(p);
	}

	// Shannon Entropy returned as dits
	public static double shannonEntropyDit(double[] p) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);

		return am.minusxLog10x().getSum_as_double();
	}

	// Shannon Entropy returned as nats (nits)
	public static double shannonEntropyNat(double[] p) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);

		return am.minusxLogEx().getSum_as_double();
	}

	// Standard deviation of a 1D array of BigDecimals, aa
	public static double standardDeviation(BigDecimal[] aa) {
		return Math.sqrt(Stat.variance(aa).doubleValue());
	}

	// Weighted standard deviation of a 1D array of BigDecimal, aa
	public static double standardDeviation(BigDecimal[] aa, BigDecimal[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return Math.sqrt(Stat.variance(aa, ww).doubleValue());
	}

	// Standard deviation of a 1D array of BigIntegers, aa
	public static double standardDeviation(BigInteger[] aa) {
		return Math.sqrt(Stat.variance(aa).doubleValue());
	}

	// Weighted standard deviation of a 1D array of BigInteger, aa
	public static double standardDeviation(BigInteger[] aa, BigInteger[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return Math.sqrt(Stat.variance(aa, ww).doubleValue());
	}

	// Standard deviation of a 1D array of Complex, aa
	public static Complex standardDeviation(Complex[] aa) {
		return Complex.sqrt(Stat.variance(aa));
	}

	// Weighted standard deviation of a 1D array of Complex, aa
	public static Complex standardDeviation(Complex[] aa, Complex[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return Complex.sqrt(Stat.variance(aa, ww));
	}

	// Standard deviation of a 1D array of doubles, aa
	public static double standardDeviation(double[] aa) {
		return Math.sqrt(Stat.variance(aa));
	}

	// Weighted standard deviation of a 1D array of doubles, aa
	public static double standardDeviation(double[] aa, double[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return Math.sqrt(Stat.variance(aa, ww));
	}

	// Standard deviation of a 1D array of floats, aa
	public static float standardDeviation(float[] aa) {
		return (float) Math.sqrt(Stat.variance(aa));
	}

	// Weighted standard deviation of a 1D array of floats, aa
	public static float standardDeviation(float[] aa, float[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return (float) Math.sqrt(Stat.variance(aa, ww));
	}

	// Standard deviation of a 1D array of int, aa
	public static double standardDeviation(int[] aa) {
		return Math.sqrt(Stat.variance(aa));
	}

	// Standard deviation of a 1D array of long, aa
	public static double standardDeviation(long[] aa) {
		return Math.sqrt(Stat.variance(aa));
	}

	// Standard deviation of a 1D array of Complex, aa, conjugate formula
	public static double standardDeviationConjugateCalcn(Complex[] aa) {
		return Math.sqrt(Stat.varianceConjugateCalcn(aa));
	}

	// Weighted standard deviation of a 1D array of Complex, aa, using conjugate formula
	public static double standardDeviationConjugateCalcn(Complex[] aa, Complex[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		return Math.sqrt(Stat.varianceConjugateCalcn(aa, ww));
	}

	// Standard deviation of the imaginary parts of a 1D array of Complex aa
	public static double standardDeviationImaginaryParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(im);
		return standardDeviation;
	}

	// ERLANG CONNECTIONS BUSY, B AND C EQUATIONS

	// Weighted standard deviation of the imaginary parts of a 1D array of Complex aa
	public static double standardDeviationImaginaryParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_imaginary_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(im, wt);
		return standardDeviation;
	}

	// Standard deviation of the moduli of a 1D array of Complex aa
	public static double standardDeviationModuli(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		double standardDeviation = Stat.standardDeviation(rl);
		return standardDeviation;
	}

	// Weighted standard deviation of the moduli of a 1D array of Complex aa
	public static double standardDeviationModuli(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_modulus_of_Complex();
		double standardDeviation = Stat.standardDeviation(rl, wt);
		return standardDeviation;
	}

	// Standard deviation of the real parts of a 1D array of Complex aa
	public static double standardDeviationRealParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(rl);
		return standardDeviation;
	}

	// Weighted standard deviation of the real parts of a 1D array of Complex aa
	public static double standardDeviationRealParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_real_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(rl, wt);
		return standardDeviation;
	}

	// Standard error of the mean of a 1D array of BigDecimals, aa
	public static double standardError(BigDecimal[] aa) {
		return Math.sqrt(Stat.variance(aa).doubleValue() / aa.length);
	}

	// Standard error of the weighted mean of a 1D array of BigDecimal, aa
	public static double standardError(BigDecimal[] aa, BigDecimal[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		double effectiveNumber = (Stat.effectiveSampleNumber(ww)).doubleValue();
		return Math.sqrt(Stat.variance(aa, ww).doubleValue() / effectiveNumber);
	}

	// Standard error of the mean of a 1D array of BigIntegers, aa
	public static double standardError(BigInteger[] aa) {
		return Math.sqrt(Stat.variance(aa).doubleValue() / aa.length);
	}

	// Standard error of the weighted mean of a 1D array of BigInteger, aa
	public static double standardError(BigInteger[] aa, BigInteger[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		double effectiveNumber = (Stat.effectiveSampleNumber(ww)).doubleValue();
		return Math.sqrt(Stat.variance(aa, ww).doubleValue() / effectiveNumber);
	}

	// Standard error of the mean of a 1D array of Complex, aa
	public static Complex standardError(Complex[] aa) {
		return Complex.sqrt(Stat.variance(aa).over(aa.length));
	}

	// Standard error of the weighted mean of a 1D array of Complex, aa
	public static Complex standardError(Complex[] aa, Complex[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		Complex effectiveNumber = Stat.effectiveSampleNumber(ww);
		return Complex.sqrt((Stat.variance(aa, ww)).over(effectiveNumber));
	}

	// Standard error of the mean of a 1D array of doubles, aa
	public static double standardError(double[] aa) {
		return Math.sqrt(Stat.variance(aa) / aa.length);
	}

	// Standard error of the weighted mean of a 1D array of doubles, aa
	public static double standardError(double[] aa, double[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		double effectiveNumber = Stat.effectiveSampleNumber(ww);
		return Math.sqrt(Stat.variance(aa, ww) / effectiveNumber);
	}

	// Standard error of the mean of a 1D array of floats, aa
	public static float standardError(float[] aa) {
		return (float) Math.sqrt(Stat.variance(aa) / aa.length);
	}

	// Standard error of the weighted mean of a 1D array of floats, aa
	public static float standardError(float[] aa, float[] ww) {
		float effectiveNumber = Stat.effectiveSampleNumber(ww);
		return (float) Math.sqrt(Stat.variance(aa, ww) / effectiveNumber);
	}

	// Standard error of the mean of a 1D array of int, aa
	public static double standardError(int[] aa) {
		return Math.sqrt(Stat.variance(aa) / aa.length);
	}

	// Standard error of the mean of a 1D array of long, aa
	public static double standardError(long[] aa) {
		return Math.sqrt(Stat.variance(aa) / aa.length);
	}

	// Standard error of the mean of a 1D array of Complex, aa, conjugate formula
	public static double standardErrorConjugateCalcn(Complex[] aa) {
		return Math.sqrt(Stat.varianceConjugateCalcn(aa) / aa.length);
	}

	// Standard error of the weighted mean of a 1D array of Complex, aa, using conjugate calculation
	public static double standardErrorConjugateCalcn(Complex[] aa, Complex[] ww) {
		if (aa.length != ww.length) throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
		double effectiveNumber = Stat.effectiveSampleNumberConjugateCalcn(ww);
		return Math.sqrt(Stat.varianceConjugateCalcn(aa, ww) / effectiveNumber);
	}

	// ENGSET EQUATION

	// Standard error of the imaginary parts of a 1D array of Complex aa
	public static double standardErrorImaginaryParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		return Stat.standardError(im);
	}

	// Weighted standard error of the imaginary parts of a 1D array of Complex aa
	public static double standardErrorImaginaryParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_imaginary_part_of_Complex();
		return Stat.standardError(im, wt);
	}

	// Standard error of the moduli of a 1D array of Complex aa
	public static double standardErrorModuli(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		return Stat.standardError(rl);
	}

	// Weighted standard error of the moduli of a 1D array of Complex aa
	public static double standardErrorModuli(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_modulus_of_Complex();
		return Stat.standardError(rl, wt);
	}

	// Standard error of the real parts of a 1D array of Complex aa
	public static double standardErrorRealParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		return Stat.standardError(rl);
	}

	// Weighted standard error of the real parts of a 1D array of Complex aa
	public static double standardErrorRealParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_real_part_of_Complex();
		return Stat.standardError(rl, wt);
	}

	public static double[] standardise(BigDecimal[] aa) {
		return Stat.standardize(aa);
	}

	public static double[] standardise(BigInteger[] aa) {
		return Stat.standardize(aa);
	}

	public static double[] standardise(double[] aa) {
		return Stat.standardize(aa);
	}

	public static float[] standardise(float[] aa) {
		return Stat.standardize(aa);
	}

	public static double[] standardise(int[] aa) {
		return Stat.standardize(aa);
	}

	public static double[] standardise(long[] aa) {
		return Stat.standardize(aa);
	}

	// BETA DISTRIBUTIONS AND BETA FUNCTIONS

	// Standardization of an array of BigDecimals to a mean of 0 and a standard deviation of 1
	// converts to double
	public static double[] standardize(BigDecimal[] aa) {
		double mean0 = Stat.mean(aa).doubleValue();
		double sd0 = Stat.standardDeviation(aa);
		int n = aa.length;
		double[] bb = new double[n];
		if (sd0 == 0.0) {
			for (int i = 0; i < n; i++) {
				bb[i] = 1.0;
			}
		} else {
			for (int i = 0; i < n; i++) {
				bb[i] = (aa[i].doubleValue() - mean0) / sd0;
			}
		}
		return bb;
	}

	// Standardization of an array of BigIntegers to a mean of 0 and a standard deviation of 1
	// converts to double
	public static double[] standardize(BigInteger[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		BigDecimal[] bd = am.getArray_as_BigDecimal();

		return Stat.standardize(bd);
	}

	//STANDARDIZATION
	// Standardization of an array of doubles to a mean of 0 and a standard deviation of 1
	public static double[] standardize(double[] aa) {
		double mean0 = Stat.mean(aa);
		double sd0 = Stat.standardDeviation(aa);
		int n = aa.length;
		double[] bb = new double[n];
		if (sd0 == 0.0) {
			for (int i = 0; i < n; i++) {
				bb[i] = 1.0;
			}
		} else {
			for (int i = 0; i < n; i++) {
				bb[i] = (aa[i] - mean0) / sd0;
			}
		}
		return bb;
	}

	// Standardization of an array of floats to a mean of 0 and a standard deviation of 1
	public static float[] standardize(float[] aa) {
		float mean0 = Stat.mean(aa);
		float sd0 = Stat.standardDeviation(aa);
		int n = aa.length;
		float[] bb = new float[n];
		if (sd0 == 0.0) {
			for (int i = 0; i < n; i++) {
				bb[i] = 1.0F;
			}
		} else {
			for (int i = 0; i < n; i++) {
				bb[i] = (aa[i] - mean0) / sd0;
			}
		}
		return bb;
	}

	// Standardization of an array of ints to a mean of 0 and a standard deviation of 1
	// converts to double
	public static double[] standardize(int[] aa) {
		double mean0 = Stat.mean(aa);
		double sd0 = Stat.standardDeviation(aa);
		int n = aa.length;
		double[] bb = new double[n];
		if (sd0 == 0.0) {
			for (int i = 0; i < n; i++) {
				bb[i] = 1.0;
			}
		} else {
			for (int i = 0; i < n; i++) {
				bb[i] = (aa[i] - mean0) / sd0;
			}
		}
		return bb;
	}

	// Standardization of an array of longs to a mean of 0 and a standard deviation of 1
	// converts to double
	public static double[] standardize(long[] aa) {
		double mean0 = Stat.mean(aa);
		double sd0 = Stat.standardDeviation(aa);
		int n = aa.length;
		double[] bb = new double[n];
		if (sd0 == 0.0) {
			for (int i = 0; i < n; i++) {
				bb[i] = 1.0;
			}
		} else {
			for (int i = 0; i < n; i++) {
				bb[i] = (aa[i] - mean0) / sd0;
			}
		}
		return bb;
	}

	// Returns the Student's t probability density function
	public static double studentst(double tValue, int df) {
		return studentT(tValue, df);
	}

	// Returns the Student's t cumulative distribution function probability
	public static double studentstCDF(double tValue, int df) {
		return studentTcdf(tValue, df);
	}

	// Returns the Student's t mean,  df = degrees of freedom
	public static double studentstMean(int df) {
		return studentTmean(df);
	}

	// Returns the Student's t median
	public static double studentstMedian() {
		return 0.0;
	}

	// Returns the Student's t mode
	public static double studentstMode() {
		return 0.0;
	}

	// Returns the Student's t probability density function
	public static double studentstPDF(double tValue, int df) {
		return studentTpdf(tValue, df);
	}

	// Returns an array of Student's t random deviates - clock seed
	// nu  =  the degrees of freedom
	public static double[] studentstRand(int nu, int n) {
		return studentTRand(nu, n);
	}

	// Returns an array of a Student's t random deviates - user supplied seed
	// nu  =  the degrees of freedom
	public static double[] studentstRand(int nu, int n, long seed) {
		return studentTrand(nu, n, seed);
	}

	// Returns the Student's t standard deviation,  df = degrees of freedom
	public static double studentstStandardDeviation(int df) {
		return studentTstandDev(df);
	}

	// Returns the Student's t probability density function
	public static double studentT(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		double ddf = df;
		double dfterm = (ddf + 1.0D) / 2.0D;
		return ((Stat.gamma(dfterm) / Stat.gamma(ddf / 2)) / Math.sqrt(ddf * Math.PI)) * Math.pow(1.0D + tValue * tValue / ddf, -dfterm);
	}

	// Returns the Student's t cumulative distribution function probability
	public static double studentTcdf(double tValueLower, double tValueUpper, int df) {
		if (tValueLower != tValueLower) throw new IllegalArgumentException("argument tLowerValue is not a number (NaN)");
		if (tValueUpper != tValueUpper) throw new IllegalArgumentException("argument tUpperValue is not a number (NaN)");
		if (tValueUpper == Double.POSITIVE_INFINITY) {
			if (tValueLower == Double.NEGATIVE_INFINITY) {
				return 1.0;
			} else {
				if (tValueLower == Double.POSITIVE_INFINITY) {
					return 0.0;
				} else {
					return (1.0 - Stat.studentTcdf(tValueLower, df));
				}
			}
		} else {
			if (tValueLower == Double.NEGATIVE_INFINITY) {
				if (tValueUpper == Double.NEGATIVE_INFINITY) {
					return 0.0;
				} else {
					return Stat.studentTcdf(tValueUpper, df);
				}
			} else {
				return Stat.studentTcdf(tValueUpper, df) - Stat.studentTcdf(tValueLower, df);
			}
		}
	}

	// Returns the Student's t cumulative distribution function probability
	public static double studentTcdf(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		if (tValue == Double.POSITIVE_INFINITY) {
			return 1.0;
		} else {
			if (tValue == Double.NEGATIVE_INFINITY) {
				return 0.0;
			} else {
				double ddf = df;
				double x = ddf / (ddf + tValue * tValue);
				return 0.5D * (1.0D + (Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, x)) * Fmath.sign(tValue));
			}
		}
	}

	// Returns the Student's t cumulative distribution function probability
	public static double studentTCDF(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		if (tValue == Double.POSITIVE_INFINITY) {
			return 1.0;
		} else {
			if (tValue == Double.NEGATIVE_INFINITY) {
				return 0.0;
			} else {
				double ddf = df;
				double x = ddf / (ddf + tValue * tValue);
				return 0.5D * (1.0D + (Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, x)) * Fmath.sign(tValue));
			}
		}
	}

	// Returns the Student's t mean,  df = degrees of freedom
	public static double studentTmean(int df) {
		double mean = Double.NaN; // mean undefined for df = 1
		if (df > 1) mean = 0.0D;
		return mean;
	}

	// Returns the Student's t median
	public static double studentTmedian() {
		return 0.0;
	}

	// Returns the Student's t mode
	public static double studentTmode() {
		return 0.0;
	}

	// Returns the Student's t probability density function
	public static double studentTpdf(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		double ddf = df;
		double dfterm = (ddf + 1.0D) / 2.0D;
		return ((Stat.gamma(dfterm) / Stat.gamma(ddf / 2)) / Math.sqrt(ddf * Math.PI)) * Math.pow(1.0D + tValue * tValue / ddf, -dfterm);
	}

	// Returns the Student's t probability density function
	public static double studentTPDF(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");

		double ddf = df;
		double dfterm = (ddf + 1.0D) / 2.0D;
		return ((Stat.gamma(dfterm) / Stat.gamma(ddf / 2)) / Math.sqrt(ddf * Math.PI)) * Math.pow(1.0D + tValue * tValue / ddf, -dfterm);
	}

	// Returns the Student's t cumulative distribution function probability
	public static double studentTProb(double tValue, int df) {
		if (tValue != tValue) throw new IllegalArgumentException("argument tValue is not a number (NaN)");
		if (tValue == Double.POSITIVE_INFINITY) {
			return 1.0;
		} else {
			if (tValue == Double.NEGATIVE_INFINITY) {
				return 0.0;
			} else {
				double ddf = df;
				double x = ddf / (ddf + tValue * tValue);
				return 0.5D * (1.0D + (Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf / 2.0D, 0.5D, x)) * Fmath.sign(tValue));
			}
		}
	}

	// Returns an array of Student's t random deviates - clock seed
	// nu  =  the degrees of freedom
	public static double[] studentTrand(int nu, int n) {
		PsRandom psr = new PsRandom();
		return psr.studentTarray(nu, n);
	}

	// ERROR FUNCTIONS

	// Returns an array of a Student's t random deviates - user supplied seed
	// nu  =  the degrees of freedom
	public static double[] studentTrand(int nu, int n, long seed) {
		PsRandom psr = new PsRandom(seed);
		return psr.studentTarray(nu, n);
	}

	// Returns an array of Student's t random deviates - clock seed
	// nu  =  the degrees of freedom
	public static double[] studentTRand(int nu, int n) {
		PsRandom psr = new PsRandom();
		return psr.studentTarray(nu, n);
	}

	// NORMAL (GAUSSIAN) DISTRIBUTION

	// Returns an array of a Student's t random deviates - user supplied seed
	// nu  =  the degrees of freedom
	public static double[] studentTRand(int nu, int n, long seed) {
		PsRandom psr = new PsRandom(seed);
		return psr.studentTarray(nu, n);
	}

	// Returns the Student's t standard deviation,  df = degrees of freedom
	public static double studentTstandDev(int df) {
		double standDev = Double.POSITIVE_INFINITY;
		if (df > 2) standDev = Math.sqrt(df / (1 - df));
		return standDev;
	}

	// Subtract arithmetic mean of an array from data array elements
	public static BigDecimal[] subtractMean(BigDecimal[] array) {
		int n = array.length;
		BigDecimal mean = Stat.mean(array);
		BigDecimal[] arrayMinusMean = new BigDecimal[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i].subtract(mean);
		mean = null;
		return arrayMinusMean;
	}

	// Subtract weighted arirhmetic mean of an array from data array elements
	public static BigDecimal[] subtractMean(BigDecimal[] array, BigDecimal[] weights) {
		int n = array.length;
		BigDecimal mean = Stat.mean(array, weights);
		BigDecimal[] arrayMinusMean = new BigDecimal[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i].subtract(mean);
		mean = null;
		return arrayMinusMean;
	}

	// Subtract arithmetic mean of an array from data array elements
	public static BigDecimal[] subtractMean(BigInteger[] array) {
		int n = array.length;
		BigDecimal mean = Stat.mean(array);
		BigDecimal[] arrayMinusMean = new BigDecimal[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = (new BigDecimal(array[i])).subtract(mean);
		mean = null;
		return arrayMinusMean;
	}

	// Subtract weighted arirhmetic mean of an array from data array elements
	public static BigDecimal[] subtractMean(BigInteger[] array, BigInteger[] weights) {
		int n = array.length;
		BigDecimal mean = Stat.mean(array, weights);
		BigDecimal[] arrayMinusMean = new BigDecimal[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = (new BigDecimal(array[i])).subtract(mean);
		mean = null;
		return arrayMinusMean;
	}

	// Subtract arithmetic mean of an array from data array elements
	public static Complex[] subtractMean(Complex[] array) {
		int n = array.length;
		Complex mean = Stat.mean(array);
		Complex[] arrayMinusMean = new Complex[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i].minus(mean);

		return arrayMinusMean;
	}

	// Subtract weighted arirhmetic mean of an array from data array elements
	public static Complex[] subtractMean(Complex[] array, Complex[] weights) {
		int n = array.length;
		Complex mean = Stat.mean(array, weights);
		Complex[] arrayMinusMean = new Complex[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i].minus(mean);

		return arrayMinusMean;
	}

	// SUBTRACT THE MEAN (STATIC)
	// Subtract arithmetic mean of an array from data array elements
	public static double[] subtractMean(double[] array) {
		int n = array.length;
		double mean = Stat.mean(array);
		double[] arrayMinusMean = new double[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i] - mean;

		return arrayMinusMean;
	}

	// Subtract weighted arirhmetic mean of an array from data array elements
	public static double[] subtractMean(double[] array, double[] weights) {
		int n = array.length;
		double mean = Stat.mean(array, weights);
		double[] arrayMinusMean = new double[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i] - mean;

		return arrayMinusMean;
	}

	// Subtract arithmetic mean of an array from data array elements
	public static float[] subtractMean(float[] array) {
		int n = array.length;
		float mean = Stat.mean(array);
		float[] arrayMinusMean = new float[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i] - mean;

		return arrayMinusMean;
	}

	// Subtract weighted arirhmetic mean of an array from data array elements
	public static float[] subtractMean(float[] array, float[] weights) {
		int n = array.length;
		float mean = Stat.mean(array, weights);
		float[] arrayMinusMean = new float[n];
		for (int i = 0; i < n; i++)
			arrayMinusMean[i] = array[i] - mean;

		return arrayMinusMean;
	}

	// TSALLIS ENTROPY (STATIC METHODS)
	// Tsallis Entropy
	public static double tsallisEntropyNat(double[] p, double q) {
		ArrayMaths am = new ArrayMaths(p);
		double max = am.getMaximum_as_double();
		if (max > 1.0D) throw new IllegalArgumentException("All probabilites must be less than or equal to 1; the maximum supplied probabilty is " + max);
		double min = am.getMinimum_as_double();
		if (min < 0.0D) throw new IllegalArgumentException("All probabilites must be greater than or equal to 0; the minimum supplied probabilty is " + min);
		double total = am.getSum_as_double();
		if (!Fmath.isEqualWithinPerCent(total, 1.0D, 0.1D)) throw new IllegalArgumentException("the probabilites must add up to 1 within an error of 0.1%; they add up to " + total);

		if (q == 1.0D) {
			return Stat.shannonEntropyNat(p);
		} else {
			am = am.pow(q);
			return (1.0D - am.getSum_as_double()) / (q - 1.0D);

		}
	}

	// UNIFORM ORDER STATISTIC MEDIANS
	public static double[] uniformOrderStatisticMedians(int n) {
		double nn = n;
		double[] uosm = new double[n];
		uosm[n - 1] = Math.pow(0.5, 1.0 / nn);
		uosm[0] = 1.0 - uosm[n - 1];
		for (int i = 1; i < n - 1; i++) {
			uosm[i] = (i + 1 - 0.3175) / (nn + 0.365);
		}
		return uosm;
	}

	// Anscombe test for a upper outlier as Vector
	public static Vector<Object> upperOutliersAnscombe(BigDecimal[] values, BigDecimal constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a upper outlier as Vector
	public static Vector<Object> upperOutliersAnscombe(BigInteger[] values, BigInteger constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a upper outlier as Vector
	public static Vector<Object> upperOutliersAnscombe(double[] values, double constant) {
		return upperOutliersAnscombeAsVector(values, constant);
	}

	// Anscombe test for a upper outlier - output as ArrayList
	public static ArrayList<Object> upperOutliersAnscombeAsArrayList(BigDecimal[] values, BigDecimal constant) {

		Stat am = new Stat(values);
		BigDecimal[] copy0 = am.getArray_as_BigDecimal();
		BigDecimal[] copy1 = am.getArray_as_BigDecimal();
		int nValues = values.length;
		ArrayList<Object> outers = new ArrayList<Object>();
		int nOutliers = 0;
		boolean test = true;
		while (test) {
			BigDecimal mean = am.mean_as_BigDecimal();
			BigDecimal variance = am.variance_as_BigDecimal();
			BigDecimal max = am.getMaximum_as_BigDecimal();
			int maxIndex = am.getMaximumIndex();
			BigDecimal statistic = (max.subtract(mean)).divide(variance, BigDecimal.ROUND_HALF_UP);
			if (statistic.compareTo(constant.multiply(constant)) == 1) {
				outers.add(max);
				outers.add(new Integer(maxIndex));
				nOutliers++;
				copy1 = new BigDecimal[nValues - 1];
				for (int i = maxIndex; i < nValues - 1; i++)
					copy1[i] = copy0[i + 1];

				nValues--;
				am = new Stat(Conv.copy(copy1));
			} else {
				mean = null;
				variance = null;
				statistic = null;
				copy0 = null;
				test = false;
			}
		}

		BigDecimal[] outliers = null;
		int[] outIndices = null;

		if (nOutliers > 0) {
			outliers = new BigDecimal[nOutliers];
			outIndices = new int[nOutliers];
			for (int i = 0; i < nOutliers; i++) {
				outliers[i] = ((BigDecimal) outers.get(2 * i));
				outIndices[i] = ((Integer) outers.get(2 * i + 1)).intValue();
			}
		}

		ArrayList<Object> ret = new ArrayList<Object>(4);
		ret.add(new Integer(nOutliers));
		ret.add(outliers);
		ret.add(outIndices);
		ret.add(copy1);
		return ret;
	}

	// Anscombe test for a upper outlier - output as ArrayList
	public static ArrayList<Object> upperOutliersAnscombeAsArrayList(BigInteger[] values, BigInteger constant) {
		ArrayMaths am = new ArrayMaths(values);
		BigDecimal[] bd = am.getArray_as_BigDecimal();
		BigDecimal cd = new BigDecimal(constant);
		return Stat.upperOutliersAnscombeAsArrayList(bd, cd);
	}

	// Anscombe test for a upper outlier - output as ArrayList
	public static ArrayList<Object> upperOutliersAnscombeAsArrayList(double[] values, double constant) {

		Stat am = new Stat(values);
		double[] copy0 = am.getArray_as_double();
		double[] copy1 = am.getArray_as_double();
		int nValues = values.length;
		ArrayList<Object> outers = new ArrayList<Object>();
		int nOutliers = 0;
		boolean test = true;

		while (test) {
			double mean = am.mean_as_double();
			double standDev = am.standardDeviation_as_double();
			double max = am.getMaximum_as_double();
			int maxIndex = am.getMaximumIndex();
			double statistic = (max - mean) / standDev;
			if (statistic > constant) {
				outers.add(new Double(max));
				outers.add(new Integer(maxIndex));
				nOutliers++;
				copy1 = new double[nValues - 1];
				for (int i = maxIndex; i < nValues - 1; i++)
					copy1[i] = copy0[i + 1];

				nValues--;
				am = new Stat(Conv.copy(copy1));
			} else {
				test = false;
			}
		}

		double[] outliers = null;
		int[] outIndices = null;

		if (nOutliers > 0) {
			outliers = new double[nOutliers];
			outIndices = new int[nOutliers];
			for (int i = 0; i < nOutliers; i++) {
				outliers[i] = ((Double) outers.get(2 * i)).doubleValue();
				outIndices[i] = ((Integer) outers.get(2 * i + 1)).intValue();
			}
		}

		ArrayList<Object> ret = new ArrayList<Object>(4);
		ret.add(new Integer(nOutliers));
		ret.add(outliers);
		ret.add(outIndices);
		ret.add(copy1);
		return ret;
	}

	// Anscombe test for a upper outlier - output as Vector
	public static Vector<Object> upperOutliersAnscombeAsVector(BigDecimal[] values, BigDecimal constant) {
		ArrayList<Object> res = Stat.upperOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// Anscombe test for a upper outlier - output as Vector
	public static Vector<Object> upperOutliersAnscombeAsVector(BigInteger[] values, BigInteger constant) {
		ArrayList<Object> res = Stat.upperOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// Anscombe test for a upper outlier - output as Vector
	public static Vector<Object> upperOutliersAnscombeAsVector(double[] values, double constant) {
		ArrayList<Object> res = Stat.upperOutliersAnscombeAsArrayList(values, constant);
		Vector<Object> ret = null;
		if (res != null) {
			int n = res.size();
			ret = new Vector<Object>(n);
			for (int i = 0; i < n; i++)
				ret.add(res.get(i));
		}
		return ret;
	}

	// EFFECTIVE SAMPLE NUMBER
	// Repalce number of data points to the effective sample number in weighted calculations
	public static void useStaticEffectiveN() {
		Stat.nEffOptionS = true;
	}

	// Repalce the effective sample number in weighted calculations by the number of data points
	public static void useStaticTrueN() {
		Stat.nEffOptionS = false;
	}

	// VARIANCE
	// Static methods
	// Variance of a 1D array of BigDecimals, aa
	public static BigDecimal variance(BigDecimal[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			BigDecimal hold = aa[i].subtract(mean);
			sum = sum.add(hold.multiply(hold));
		}
		BigDecimal ret = sum.divide(new BigDecimal((double) (n - 1)), BigDecimal.ROUND_HALF_UP);
		if (Stat.nFactorOptionS) ret = sum.divide(new BigDecimal((double) n), BigDecimal.ROUND_HALF_UP);
		sum = null;
		mean = null;
		return ret;
	}

	public static BigDecimal variance(BigDecimal[] aa, BigDecimal[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		BigDecimal nn = Stat.effectiveSampleNumber(ww);
		BigDecimal nterm = nn.divide(nn.subtract(BigDecimal.ONE), BigDecimal.ROUND_HALF_UP);
		if (Stat.nFactorOptionS) nterm = BigDecimal.ONE;
		BigDecimal sumx = BigDecimal.ZERO;
		BigDecimal sumw = BigDecimal.ZERO;
		BigDecimal mean = BigDecimal.ZERO;
		BigDecimal[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumx = sumx.add(aa[i].multiply(weight[i]));
			sumw = sumw.add(weight[i]);
		}
		mean = sumx.divide(sumw, BigDecimal.ROUND_HALF_UP);
		sumx = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sumx = sumx.add(weight[i].multiply(aa[i].subtract(mean)).multiply(aa[i].subtract(mean)));
		}
		sumx = (sumx.multiply(nterm).divide(sumw, BigDecimal.ROUND_HALF_UP));
		sumw = null;
		mean = null;
		weight = null;
		nn = null;
		nterm = null;
		return sumx;
	}

	// Variance of a 1D array of BigIntegers, aa
	public static BigDecimal variance(BigInteger[] aa) {
		int n = aa.length;
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal mean = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			sum = sum.add(new BigDecimal(aa[i]));
		}
		mean = sum.divide(new BigDecimal((double) n), BigDecimal.ROUND_HALF_UP);
		sum = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			BigDecimal hold = new BigDecimal(aa[i]).subtract(mean);
			sum = sum.add(hold.multiply(hold));
		}
		BigDecimal ret = sum.divide(new BigDecimal((double) (n - 1)), BigDecimal.ROUND_HALF_UP);
		if (Stat.nFactorOptionS) ret = sum.divide(new BigDecimal((double) n), BigDecimal.ROUND_HALF_UP);
		sum = null;
		mean = null;
		return ret;
	}

	public static BigDecimal variance(BigInteger[] aa, BigInteger[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths aab = new ArrayMaths(aa);
		ArrayMaths wwb = new ArrayMaths(ww);
		return variance(aab.array_as_BigDecimal(), wwb.array_as_BigDecimal());
	}

	// Variance of a 1D array of Complex, aa
	public static Complex variance(Complex[] aa) {
		int n = aa.length;
		Complex sum = Complex.zero();
		Complex mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			Complex hold = new Complex(aa[i]).minus(mean);
			sum = sum.plus(hold.times(hold));
		}
		Complex ret = sum.over(new Complex((n - 1)));
		if (Stat.nFactorOptionS) ret = sum.over(new Complex(n));
		return ret;
	}

	// LOG-NORMAL DISTRIBUTIONS (TWO AND THEE PARAMETER DISTRIBUTIONS)

	// TWO PARAMETER LOG-NORMAL DISTRIBUTION

	// Weighted variance of a 1D array of Complex aa
	public static Complex variance(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		Complex nn = Stat.effectiveSampleNumber(ww);
		Complex nterm = nn.over(nn.minus(1.0));
		if (Stat.nFactorOptionS) nterm = Complex.plusOne();
		Complex sumx = Complex.zero();
		Complex sumw = Complex.zero();
		Complex mean = Complex.zero();
		Complex[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumx = sumx.plus(aa[i].times(weight[i]));
			sumw = sumw.plus(weight[i]);
		}
		mean = sumx.over(sumw);
		sumx = Complex.zero();
		for (int i = 0; i < n; i++) {
			Complex hold = aa[i].minus(mean);
			sumx = sumx.plus(weight[i].times(hold).times(hold));
		}
		return (sumx.times(nterm)).over(sumw);
	}

	// Variance of a 1D array of doubles, aa
	public static double variance(double[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		sum = 0.0D;
		for (int i = 0; i < n; i++) {
			sum += Fmath.square(aa[i] - mean);
		}
		double ret = sum / (n - 1);
		if (Stat.nFactorOptionS) ret = sum / n;
		return ret;
	}

	// Weighted variance of a 1D array of doubles, aa
	public static double variance(double[] aa, double[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		double nn = Stat.effectiveSampleNumber(ww);
		double nterm = nn / (nn - 1.0);
		if (Stat.nFactorOptionS) nterm = 1.0;

		double sumx = 0.0D, sumw = 0.0D, mean = 0.0D;
		double[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumx += aa[i] * weight[i];
			sumw += weight[i];
		}
		mean = sumx / sumw;
		sumx = 0.0D;
		for (int i = 0; i < n; i++) {
			sumx += weight[i] * Fmath.square(aa[i] - mean);
		}
		return sumx * nterm / sumw;
	}

	// Variance of a 1D array of floats, aa
	public static float variance(float[] aa) {
		int n = aa.length;
		float sum = 0.0F;
		float mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Fmath.square(aa[i] - mean);
		}
		float ret = sum / (n - 1);
		if (Stat.nFactorOptionS) ret = sum / n;
		return ret;
	}

	// Weighted variance of a 1D array of floats, aa
	public static float variance(float[] aa, float[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		float nn = Stat.effectiveSampleNumber(ww);
		float nterm = nn / (nn - 1.0F);
		if (Stat.nFactorOptionS) nterm = 1.0F;

		float sumx = 0.0F, sumw = 0.0F, mean = 0.0F;
		float[] weight = Stat.invertAndSquare(ww);
		for (int i = 0; i < n; i++) {
			sumx += aa[i] * weight[i];
			sumw += weight[i];
		}
		mean = sumx / sumw;
		sumx = 0.0F;
		for (int i = 0; i < n; i++) {
			sumx += weight[i] * Fmath.square(aa[i] - mean);
		}
		return sumx * nterm / sumw;
	}

	// Variance of a 1D array of int, aa
	public static double variance(int[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Fmath.square(aa[i] - mean);
		}
		double ret = sum / (n - 1);
		if (Stat.nFactorOptionS) ret = sum / n;
		return ret;
	}

	// Variance of a 1D array of long, aa
	public static double variance(long[] aa) {
		int n = aa.length;
		double sum = 0.0D;
		double mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			sum += Fmath.square(aa[i] - mean);
		}
		double ret = sum / (n - 1);
		if (Stat.nFactorOptionS) ret = sum / n;
		return ret;
	}

	// Variance of a 1D array of Complex, aa, using conjugate formula
	public static double varianceConjugateCalcn(Complex[] aa) {
		int n = aa.length;
		Complex sum = Complex.zero();
		Complex mean = Stat.mean(aa);
		for (int i = 0; i < n; i++) {
			Complex hold = new Complex(aa[i]).minus(mean);
			sum = sum.plus(hold.times(hold.conjugate()));
		}
		double ret = sum.getReal() / (n - 1);
		if (Stat.nFactorOptionS) ret = sum.getReal() / n;
		return ret;
	}

	// Weighted variance of a 1D array of Complex aa, using conjugate formula
	public static double varianceConjugateCalcn(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		double nn = Stat.effectiveSampleNumberConjugateCalcn(ww);
		double nterm = nn / (nn - 1.0);
		if (Stat.nFactorOptionS) nterm = 1.0;
		Complex sumx = Complex.zero();
		Complex sumw = Complex.zero();
		Complex sumwc = Complex.zero();
		Complex mean = Complex.zero();
		ArrayMaths st = new ArrayMaths(ww);
		st = st.invert();
		Complex[] weight = st.array_as_Complex();
		for (int i = 0; i < n; i++) {
			sumx = sumx.plus(aa[i].times(weight[i].times(weight[i])));
			sumw = sumw.plus(weight[i].times(weight[i]));
			sumwc = sumwc.plus(weight[i].times(weight[i].conjugate()));
		}
		mean = sumx.over(sumw);
		sumx = Complex.zero();

		for (int i = 0; i < n; i++) {
			Complex hold = aa[i].minus(mean);
			sumx = sumx.plus((weight[i].times(weight[i].conjugate())).times(hold).times(hold.conjugate()));
		}
		return nterm * ((sumx.times(nterm)).over(sumwc)).getReal();
	}

	// Variance of the imaginary parts of a 1D array of Complex aa
	public static double varianceImaginaryParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		double variance = Stat.variance(im);
		return variance;
	}

	// Weighted variance of the imaginary parts of a 1D array of Complex aa
	public static double varianceImaginaryParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] im = am.array_as_imaginary_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_imaginary_part_of_Complex();
		double variance = Stat.variance(im, wt);
		return variance;
	}

	// Variance of the moduli of a 1D array of Complex aa
	public static double varianceModuli(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		double variance = Stat.variance(rl);
		return variance;
	}

	// Weighted variance of the moduli of a 1D array of Complex aa
	public static double varianceModuli(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_modulus_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_modulus_of_Complex();
		double variance = Stat.variance(rl, wt);
		return variance;
	}

	// Variance of the real parts of a 1D array of Complex aa
	public static double varianceRealParts(Complex[] aa) {
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		double variance = Stat.variance(rl);
		return variance;
	}

	// Weighted variance of the real parts of a 1D array of Complex aa
	public static double varianceRealParts(Complex[] aa, Complex[] ww) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
		ArrayMaths am = new ArrayMaths(aa);
		double[] rl = am.array_as_real_part_of_Complex();
		ArrayMaths wm = new ArrayMaths(ww);
		double[] wt = wm.array_as_real_part_of_Complex();
		double variance = Stat.variance(rl, wt);
		return variance;
	}

	// volatility   log  (BigDecimal)
	public static double volatilityLogChange(BigDecimal[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		for (int i = 0; i < n; i++)
			change[i] = Math.log((array[i + 1].divide(array[i], BigDecimal.ROUND_HALF_UP)).doubleValue());
		return Stat.standardDeviation(change);
	}

	// volatility   log  (BigInteger)
	public static double volatilityLogChange(BigInteger[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		for (int i = 0; i < n; i++)
			change[i] = Math.log(((new BigDecimal(array[i + 1])).divide(new BigDecimal(array[i]), BigDecimal.ROUND_HALF_UP)).doubleValue());
		return Stat.standardDeviation(change);
	}

	// volatility   log  (doubles)
	public static double volatilityLogChange(double[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		for (int i = 0; i < n; i++)
			change[i] = Math.log(array[i + 1] / array[i]);
		return Stat.standardDeviation(change);
	}

	// volatility   log  (floats)
	public static float volatilityLogChange(float[] array) {
		int n = array.length - 1;
		float[] change = new float[n];
		for (int i = 0; i < n; i++)
			change[i] = (float) Math.log(array[i + 1] / array[i]);
		return Stat.standardDeviation(change);
	}

	// volatility   percentage (BigDecimal)
	public static double volatilityPerCentChange(BigDecimal[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		for (int i = 0; i < n; i++)
			change[i] = ((array[i + 1].add(array[i])).multiply((new BigDecimal(100.0D)).divide(array[i], BigDecimal.ROUND_HALF_UP))).doubleValue();
		return Stat.standardDeviation(change);
	}

	// volatility   percentage (Biginteger)
	public static double volatilityPerCentChange(BigInteger[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		ArrayMaths am = new ArrayMaths(array);
		BigDecimal[] bd = am.getArray_as_BigDecimal();
		for (int i = 0; i < n; i++)
			change[i] = ((bd[i + 1].add(bd[i])).multiply((new BigDecimal(100.0D)).divide(bd[i], BigDecimal.ROUND_HALF_UP))).doubleValue();
		bd = null;
		return Stat.standardDeviation(change);
	}

	// volatility   percentage (double)
	public static double volatilityPerCentChange(double[] array) {
		int n = array.length - 1;
		double[] change = new double[n];
		for (int i = 0; i < n; i++)
			change[i] = (array[i + 1] - array[i]) * 100.0D / array[i];
		return Stat.standardDeviation(change);
	}

	// volatility   percentage (float)
	public static double volatilityPerCentChange(float[] array) {
		int n = array.length - 1;
		float[] change = new float[n];
		for (int i = 0; i < n; i++)
			change[i] = (array[i + 1] - array[i]) * 100.0F / array[i];
		return Stat.standardDeviation(change);
	}

	// Weibull probability density function
	public static double weibull(double mu, double sigma, double gamma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (gamma / sigma) * Math.pow(arg, gamma - 1.0D) * Math.exp(-Math.pow(arg, gamma));
		}
		return y;
	}

	// THREE PARAMETER LOG-NORMAL DISTRIBUTION

	// Weibull cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double weibullCDF(double mu, double sigma, double gamma, double upperlimit) {
		double arg = (upperlimit - mu) / sigma;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-Math.pow(arg, gamma));
		return y;
	}

	// Weibull cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double weibullCDF(double mu, double sigma, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-Math.pow(arg1, gamma));
		if (arg2 >= 0.0D) term2 = -Math.exp(-Math.pow(arg2, gamma));
		return term2 - term1;
	}

	// Weibull Inverse Cumulative Density Function
	// Standard
	public static double weibullInverseCDF(double gamma, double prob) {
		return weibullInverseCDF(0.0D, 1.0D, gamma, prob);
	}

	// Weibull Inverse Cumulative Density Function
	// Two parameter
	public static double weibullInverseCDF(double sigma, double gamma, double prob) {
		return weibullInverseCDF(0.0D, sigma, gamma, prob);
	}

	// Weibull Inverse Cumulative Density Function
	// Three parameter
	public static double weibullInverseCDF(double mu, double sigma, double gamma, double prob) {
		if (prob < 0.0 || prob > 1.0) throw new IllegalArgumentException("Entered cdf value, " + prob + ", must lie between 0 and 1 inclusive");
		double icdf = 0.0D;

		if (prob == 0.0) {
			icdf = mu;
		} else {
			if (prob == 1.0) {
				icdf = Double.POSITIVE_INFINITY;
			} else {
				icdf = mu + sigma * (Math.pow(-Math.log(1.0 - prob), 1.0 / gamma));
			}
		}

		return icdf;
	}

	// Weibull mean
	public static double weibullMean(double mu, double sigma, double gamma) {
		return mu + sigma * Stat.gamma(1.0D / gamma + 1.0D);
	}

	// Weibull median
	public static double weibullMedian(double mu, double sigma, double gamma) {
		return mu + sigma * Math.pow(Math.log(2.0D), 1.0D / gamma);
	}

	// Weibull mode
	public static double weibullMode(double mu, double sigma, double gamma) {
		double y = mu;
		if (gamma > 1.0D) {
			y = mu + sigma * Math.pow((gamma - 1.0D) / gamma, 1.0D / gamma);
		}
		return y;
	}

	// Weibull order statistic medians (n points)
	// Three parameter
	public static double[] weibullOrderStatisticMedians(double mu, double sigma, double gamma, int n) {
		double[] wosm = new double[n];
		double[] uosm = uniformOrderStatisticMedians(n);
		for (int i = 0; i < n; i++) {
			wosm[i] = Stat.inverseWeibullCDF(mu, sigma, gamma, uosm[i]);
		}
		return wosm;
	}

	// Weibull order statistic medians for a mu of zero  (n points)
	// Two parameter
	public static double[] weibullOrderStatisticMedians(double sigma, double gamma, int n) {
		return Stat.weibullOrderStatisticMedians(0.0, sigma, gamma, n);
	}

	// Weibull order statistic medians for a mu of zero and a sigma of unity  (n points)
	// Standard
	public static double[] weibullOrderStatisticMedians(double gamma, int n) {
		return Stat.weibullOrderStatisticMedians(0.0, 1.0, gamma, n);
	}

	// Weibull probability density function
	public static double weibullPDF(double mu, double sigma, double gamma, double x) {
		double arg = (x - mu) / sigma;
		double y = 0.0D;
		if (arg >= 0.0D) {
			y = (gamma / sigma) * Math.pow(arg, gamma - 1.0D) * Math.exp(-Math.pow(arg, gamma));
		}
		return y;
	}

	// Weibull cumulative distribution function
	// probability that a variate will assume  a value less than the upperlimit
	public static double weibullProb(double mu, double sigma, double gamma, double upperlimit) {
		double arg = (upperlimit - mu) / sigma;
		double y = 0.0D;
		if (arg > 0.0D) y = 1.0D - Math.exp(-Math.pow(arg, gamma));
		return y;
	}

	// Weibull cumulative distribution function
	// probability that a variate will assume a value between the lower and  the upper limits
	public static double weibullProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit) {
		double arg1 = (lowerlimit - mu) / sigma;
		double arg2 = (upperlimit - mu) / sigma;
		double term1 = 0.0D, term2 = 0.0D;
		if (arg1 >= 0.0D) term1 = -Math.exp(-Math.pow(arg1, gamma));
		if (arg2 >= 0.0D) term2 = -Math.exp(-Math.pow(arg2, gamma));
		return term2 - term1;
	}

	// LOGISTIC DISTRIBUTION
	// TWO PARAMETERS (See below for three parameter distribution)

	// Returns an array of Weibull (Type III EVD) random deviates - clock seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] weibullRand(double mu, double sigma, double gamma, int n) {
		double[] ran = new double[n];
		Random rr = new Random();
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow(-Math.log(1.0D - rr.nextDouble()), 1.0D / gamma) * sigma + mu;
		}
		return ran;
	}

	// Returns an array of Weibull (Type III EVD) random deviates - user supplied seed
	// mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
	public static double[] weibullRand(double mu, double sigma, double gamma, int n, long seed) {
		double[] ran = new double[n];
		Random rr = new Random(seed);
		for (int i = 0; i < n; i++) {
			ran[i] = Math.pow(-Math.log(1.0D - rr.nextDouble()), 1.0D / gamma) * sigma + mu;
		}
		return ran;
	}

	// Weibull standard deviation
	public static double weibullStandardDeviation(double sigma, double gamma) {
		return weibullStandDev(sigma, gamma);
	}

	// Weibull standard deviation
	public static double weibullStandDev(double sigma, double gamma) {
		double y = Stat.gamma(2.0D / gamma + 1.0D) - Fmath.square(Stat.gamma(1.0D / gamma + 1.0D));
		return sigma * Math.sqrt(y);
	}

	// weighted generalised mean of a 1D array of BigDecimal, aa
	public static double weightedGeneralisedMean(BigDecimal[] aa, BigDecimal[] ww, BigDecimal m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of BigDecimal, aa
	public static double weightedGeneralisedMean(BigDecimal[] aa, BigDecimal[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of BigInteger, aa
	public static double weightedGeneralisedMean(BigInteger[] aa, BigInteger[] ww, BigInteger m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of BigInteger, aa
	public static double weightedGeneralisedMean(BigInteger[] aa, BigInteger[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of Complex, aa
	public static Complex weightedGeneralisedMean(Complex[] aa, Complex[] ww, Complex m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of Complex, aa
	public static Complex weightedGeneralisedMean(Complex[] aa, Complex[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of doubles, aa
	public static double weightedGeneralisedMean(double[] aa, double[] ww, double m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// weighted generalised mean of a 1D array of floats, aa
	public static float weightedGeneralisedMean(float[] aa, float[] ww, float m) {
		int n = aa.length;
		if (n != ww.length) throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");

		return generalisedMean(aa, ww, m);
	}

	// CONSTRUCTORS
	public Stat() {
		super();
	}

	public Stat(ArrayList<Object> xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(BigDecimal[] xx) {
		super(xx);
	}

	public Stat(BigInteger[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(byte[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Byte[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Complex[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(double[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Double[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(float[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Float[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(int[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Integer[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(long[] xx) {
		super(xx);
		this.convertToHighest();
	}

	// LORENTZIAN DISTRIBUTION (CAUCHY DISTRIBUTION)

	public Stat(Long[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Object[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Phasor[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(short[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Short[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(String[] xx) {
		super(xx);
		this.convertToHighest();
	}

	public Stat(Vector<Object> xx) {
		super(xx);
		this.convertToHighest();
	}

	// Returns an array of n Binomial pseudorandom deviates from a binomial - clock seed
	//  distribution of nTrial trials each of probablity, prob,
	//  after 	bndlev 	Numerical Recipes in C - W.H. Press et al. (Cambridge)
	//		            2nd edition 1992 p295.
	public double[] binomialRand(double prob, int nTrials, int n) {

		if (nTrials < n) throw new IllegalArgumentException("Number of deviates requested, " + n + ", must be less than the number of trials, " + nTrials);
		if (prob < 0.0D || prob > 1.0D) throw new IllegalArgumentException("The probablity provided, " + prob + ", must lie between 0 and 1)");

		double[] ran = new double[n]; // array of deviates to be returned
		Random rr = new Random(); // instance of Random

		double binomialDeviate = 0.0D; // the binomial deviate to be returned
		double deviateMean = 0.0D; // mean of deviate to be produced
		double testDeviate = 0.0D; // test deviate
		double workingProb = 0.0; // working value of the probability
		double logProb = 0.0; // working value of the probability
		double probOld = -1.0D; // previous value of the working probability
		double probC = -1.0D; // complementary value of the working probability
		double logProbC = -1.0D; // log of the complementary value of the working probability
		int nOld = -1; // previous value of trials counter
		double enTrials = 0.0D; // (double) trials counter
		double oldGamma = 0.0D; // a previous log Gamma function value
		double tanW = 0.0D; // a working tangent
		double hold0 = 0.0D; // a working holding variable
		int jj; // counter

		double probOriginalValue = prob;
		for (int i = 0; i < n; i++) {
			prob = probOriginalValue;
			workingProb = (prob <= 0.5D ? prob : 1.0 - prob); // distribution invariant on swapping prob for 1 - prob
			deviateMean = nTrials * workingProb;

			if (nTrials < 25) {
				// if number of trials greater than 25 use direct method
				binomialDeviate = 0.0D;
				for (jj = 1; jj <= nTrials; jj++)
					if (rr.nextDouble() < workingProb) ++binomialDeviate;
			} else if (deviateMean < 1.0D) {
				// if fewer than 1 out of 25 events - Poisson approximation is accurate
				double expOfMean = Math.exp(-deviateMean);
				testDeviate = 1.0D;
				for (jj = 0; jj <= nTrials; jj++) {
					testDeviate *= rr.nextDouble();
					if (testDeviate < expOfMean) break;
				}
				binomialDeviate = (jj <= nTrials ? jj : nTrials);

			} else {
				// use rejection method
				if (nTrials != nOld) {
					// if nTrials has changed compute useful quantities
					enTrials = nTrials;
					oldGamma = Stat.logGamma(enTrials + 1.0D);
					nOld = nTrials;
				}
				if (workingProb != probOld) {
					// if workingProb has changed compute useful quantities
					probC = 1.0 - workingProb;
					logProb = Math.log(workingProb);
					logProbC = Math.log(probC);
					probOld = workingProb;
				}

				double sq = Math.sqrt(2.0 * deviateMean * probC);
				do {
					do {
						double angle = Math.PI * rr.nextDouble();
						tanW = Math.tan(angle);
						hold0 = sq * tanW + deviateMean;
					} while (hold0 < 0.0D || hold0 >= (enTrials + 1.0D)); //rejection test
					hold0 = Math.floor(hold0); // integer value distribution
					testDeviate = 1.2D * sq * (1.0D + tanW * tanW) * Math.exp(oldGamma - Stat.logGamma(hold0 + 1.0D) - Stat.logGamma(enTrials - hold0 + 1.0D) + hold0 * logProb + (enTrials - hold0) * logProbC);
				} while (rr.nextDouble() > testDeviate); // rejection test
				binomialDeviate = hold0;
			}

			if (workingProb != prob) binomialDeviate = nTrials - binomialDeviate; // symmetry transformation

			ran[i] = binomialDeviate;
		}

		return ran;
	}

	// Returns an array of n Binomial pseudorandom deviates from a binomial - user supplied seed
	//  distribution of nTrial trials each of probablity, prob,
	//  after 	bndlev 	Numerical Recipes in C - W.H. Press et al. (Cambridge)
	//		            2nd edition 1992 p295.
	public double[] binomialRand(double prob, int nTrials, int n, long seed) {

		if (nTrials < n) throw new IllegalArgumentException("Number of deviates requested, " + n + ", must be less than the number of trials, " + nTrials);
		if (prob < 0.0D || prob > 1.0D) throw new IllegalArgumentException("The probablity provided, " + prob + ", must lie between 0 and 1)");

		double[] ran = new double[n]; // array of deviates to be returned
		Random rr = new Random(seed); // instance of Random

		double binomialDeviate = 0.0D; // the binomial deviate to be returned
		double deviateMean = 0.0D; // mean of deviate to be produced
		double testDeviate = 0.0D; // test deviate
		double workingProb = 0.0; // working value of the probability
		double logProb = 0.0; // working value of the probability
		double probOld = -1.0D; // previous value of the working probability
		double probC = -1.0D; // complementary value of the working probability
		double logProbC = -1.0D; // log of the complementary value of the working probability
		int nOld = -1; // previous value of trials counter
		double enTrials = 0.0D; // (double) trials counter
		double oldGamma = 0.0D; // a previous log Gamma function value
		double tanW = 0.0D; // a working tangent
		double hold0 = 0.0D; // a working holding variable
		int jj; // counter

		double probOriginalValue = prob;
		for (int i = 0; i < n; i++) {
			prob = probOriginalValue;
			workingProb = (prob <= 0.5D ? prob : 1.0 - prob); // distribution invariant on swapping prob for 1 - prob
			deviateMean = nTrials * workingProb;

			if (nTrials < 25) {
				// if number of trials greater than 25 use direct method
				binomialDeviate = 0.0D;
				for (jj = 1; jj <= nTrials; jj++)
					if (rr.nextDouble() < workingProb) ++binomialDeviate;
			} else if (deviateMean < 1.0D) {
				// if fewer than 1 out of 25 events - Poisson approximation is accurate
				double expOfMean = Math.exp(-deviateMean);
				testDeviate = 1.0D;
				for (jj = 0; jj <= nTrials; jj++) {
					testDeviate *= rr.nextDouble();
					if (testDeviate < expOfMean) break;
				}
				binomialDeviate = (jj <= nTrials ? jj : nTrials);

			} else {
				// use rejection method
				if (nTrials != nOld) {
					// if nTrials has changed compute useful quantities
					enTrials = nTrials;
					oldGamma = Stat.logGamma(enTrials + 1.0D);
					nOld = nTrials;
				}
				if (workingProb != probOld) {
					// if workingProb has changed compute useful quantities
					probC = 1.0 - workingProb;
					logProb = Math.log(workingProb);
					logProbC = Math.log(probC);
					probOld = workingProb;
				}

				double sq = Math.sqrt(2.0 * deviateMean * probC);
				do {
					do {
						double angle = Math.PI * rr.nextDouble();
						tanW = Math.tan(angle);
						hold0 = sq * tanW + deviateMean;
					} while (hold0 < 0.0D || hold0 >= (enTrials + 1.0D)); //rejection test
					hold0 = Math.floor(hold0); // integer value distribution
					testDeviate = 1.2D * sq * (1.0D + tanW * tanW) * Math.exp(oldGamma - Stat.logGamma(hold0 + 1.0D) - Stat.logGamma(enTrials - hold0 + 1.0D) + hold0 * logProb + (enTrials - hold0) * logProbC);
				} while (rr.nextDouble() > testDeviate); // rejection test
				binomialDeviate = hold0;
			}

			if (workingProb != prob) binomialDeviate = nTrials - binomialDeviate; // symmetry transformation

			ran[i] = binomialDeviate;
		}

		return ran;
	}

	// POISSON DISTRIBUTION

	//COEFFICIENT OF VARIATION
	public double coefficientOfVariation() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double coefficientOfVariation = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			coefficientOfVariation = Stat.coefficientOfVariation(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			coefficientOfVariation = Stat.coefficientOfVariation(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex coefficient of variation is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return coefficientOfVariation;
	}

	// CONVERSION OF WEIGHTING FACTORS (INSTANCE)
	// Converts weighting facors Wi to wi, i.e. to 1/sqrt(Wi)
	// DEPRECATED !!!
	public void convertBigWtoLittleW() {
		if (!weightsSupplied) {
			System.out.println("convertBigWtoLittleW: no weights have been supplied - all weights set to unity");
		} else {
			amWeights = amWeights.oneOverSqrt();
		}
	}

	// Convert array to Double if not Complex, Phasor,  BigDecimal or BigInteger
	// Convert to BigDecimal if BigInteger
	// Convert Phasor to Complex
	@Override
	public void convertToHighest() {

		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 16:
		case 17:
		case 18:
			Double[] dd = this.getArray_as_Double();
			array.clear();
			for (int i = 0; i < length; i++)
				array.add(dd[i]);
			double[] ww = new double[length];
			for (int i = 0; i < length; i++)
				ww[i] = 1.0D;
			amWeights = new ArrayMaths(ww);
			type = 1;
			break;
		case 12:
		case 13:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			array.clear();
			for (int i = 0; i < length; i++)
				array.add(bd[i]);
			BigDecimal[] wd = new BigDecimal[length];
			for (int i = 0; i < length; i++)
				wd[i] = BigDecimal.ONE;
			amWeights = new ArrayMaths(wd);
			type = 12;
			bd = null;
			break;
		case 14:
		case 15:
			Complex[] cc = this.getArray_as_Complex();
			array.clear();
			for (int i = 0; i < length; i++)
				array.add(cc[i]);
			Complex[] wc = new Complex[length];
			for (int i = 0; i < length; i++)
				wc[i] = Complex.plusOne();
			amWeights = new ArrayMaths(wc);
			type = 14;
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
	}

	private void convertWeights(ArrayMaths wm) {
		switch (type) {
		case 1:
			switch (wm.typeIndex()) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				Double[] w1 = wm.getArray_as_Double();
				amWeights = new ArrayMaths(w1);
				break;
			case 12:
			case 13:
				BigDecimal[] a2 = this.getArray_as_BigDecimal();
				for (int i = 0; i < length; i++)
					array.add(a2[i]);
				BigDecimal[] w2 = wm.getArray_as_BigDecimal();
				amWeights = new ArrayMaths(w2);
				a2 = null;
				w2 = null;
				break;
			case 14:
			case 15:
				Complex[] a3 = this.getArray_as_Complex();
				for (int i = 0; i < length; i++)
					array.add(a3[i]);
				Complex[] w3 = wm.getArray_as_Complex();
				amWeights = new ArrayMaths(w3);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");

			}
			break;
		case 12:
			switch (wm.typeIndex()) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				BigDecimal[] w4 = wm.getArray_as_BigDecimal();
				amWeights = new ArrayMaths(w4);
				w4 = null;
				break;
			case 12:
			case 13:
				BigDecimal[] w5 = wm.getArray_as_BigDecimal();
				amWeights = new ArrayMaths(w5);
				w5 = null;
				break;
			case 14:
			case 15:
				Complex[] a6 = this.getArray_as_Complex();
				for (int i = 0; i < length; i++)
					array.add(a6[i]);
				Complex[] w6 = wm.getArray_as_Complex();
				amWeights = new ArrayMaths(w6);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			break;
		case 14:
			Complex[] a7 = this.getArray_as_Complex();
			for (int i = 0; i < length; i++)
				array.add(a7[i]);
			Complex[] w7 = wm.getArray_as_Complex();
			amWeights = new ArrayMaths(w7);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
	}

	// DEEP COPY
	// Copy to a new instance of Stat
	@Override
	public Stat copy() {

		Stat am = new Stat();

		if (amWeights == null) {
			am.amWeights = null;
		} else {
			am.amWeights = amWeights;
		}
		am.weightsSupplied = weightsSupplied;
		am.upperOutlierDetails = new ArrayList<Object>();
		if (upperOutlierDetails.size() != 0) {
			Integer hold0 = (Integer) upperOutlierDetails.get(0);
			am.upperOutlierDetails.add(hold0);
			am.upperOutlierDetails.add(upperOutlierDetails.get(1));
			int[] hold2 = (int[]) upperOutlierDetails.get(2);
			am.upperOutlierDetails.add(hold2);
			am.upperOutlierDetails.add(upperOutlierDetails.get(3));
		}
		am.upperDone = upperDone;
		am.lowerOutlierDetails = new ArrayList<Object>();
		if (lowerOutlierDetails.size() != 0) {
			Integer hold0 = (Integer) lowerOutlierDetails.get(0);
			am.lowerOutlierDetails.add(hold0);
			am.lowerOutlierDetails.add(lowerOutlierDetails.get(1));
			int[] hold2 = (int[]) lowerOutlierDetails.get(2);
			am.lowerOutlierDetails.add(hold2);
			am.lowerOutlierDetails.add(lowerOutlierDetails.get(3));
		}
		am.lowerDone = lowerDone;

		am.length = length;
		am.maxIndex = maxIndex;
		am.minIndex = minIndex;
		am.sumDone = sumDone;
		am.productDone = productDone;
		am.sumlongToDouble = sumlongToDouble;
		am.productlongToDouble = productlongToDouble;
		am.type = type;
		if (originalTypes == null) {
			am.originalTypes = null;
		} else {
			am.originalTypes = Conv.copy(originalTypes);
		}
		if (sortedIndices == null) {
			am.sortedIndices = null;
		} else {
			am.sortedIndices = Conv.copy(sortedIndices);
		}
		am.suppressMessages = suppressMessages;
		am.minmax = new ArrayList<Object>();
		if (minmax.size() != 0) {
			switch (type) {
			case 0:
			case 1:
				double dd = ((Double) minmax.get(0)).doubleValue();
				am.minmax.add(new Double(dd));
				dd = ((Double) minmax.get(1)).doubleValue();
				am.minmax.add(new Double(dd));
				break;
			case 4:
			case 5:
				long ll = ((Long) minmax.get(0)).longValue();
				am.minmax.add(new Double(ll));
				ll = ((Long) minmax.get(1)).longValue();
				am.minmax.add(new Long(ll));
				break;
			case 2:
			case 3:
				float ff = ((Float) minmax.get(0)).floatValue();
				am.minmax.add(new Double(ff));
				ff = ((Float) minmax.get(1)).floatValue();
				am.minmax.add(new Double(ff));
				break;
			case 6:
			case 7:
				int ii = ((Integer) minmax.get(0)).intValue();
				am.minmax.add(new Integer(ii));
				ii = ((Double) minmax.get(1)).intValue();
				am.minmax.add(new Integer(ii));
				break;
			case 8:
			case 9:
				short ss = ((Short) minmax.get(0)).shortValue();
				am.minmax.add(new Short(ss));
				ss = ((Double) minmax.get(1)).shortValue();
				am.minmax.add(new Short((ss)));
				break;
			case 10:
			case 11:
				byte bb = ((Byte) minmax.get(0)).byteValue();
				am.minmax.add(new Byte(bb));
				ss = ((Byte) minmax.get(1)).byteValue();
				am.minmax.add(new Byte((bb)));
				break;
			case 12:
				BigDecimal bd = (BigDecimal) minmax.get(0);
				am.minmax.add(bd);
				bd = (BigDecimal) minmax.get(1);
				am.minmax.add(bd);
				bd = null;
				break;
			case 13:
				BigInteger bi = (BigInteger) minmax.get(0);
				am.minmax.add(bi);
				bi = (BigInteger) minmax.get(1);
				am.minmax.add(bi);
				bi = null;
				break;
			case 16:
			case 17:
				int iii = ((Integer) minmax.get(0)).intValue();
				am.minmax.add(new Integer(iii));
				iii = ((Double) minmax.get(1)).intValue();
				am.minmax.add(new Integer(iii));
				break;
			}
		}

		am.summ = new ArrayList<Object>();
		if (summ.size() != 0) {
			switch (type) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 18:
				double dd = ((Double) summ.get(0)).doubleValue();
				am.summ.add(new Double(dd));
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 16:
			case 17:
				if (sumlongToDouble) {
					double dd2 = ((Double) summ.get(0)).doubleValue();
					am.summ.add(new Double(dd2));
				} else {
					long ll = ((Long) summ.get(0)).longValue();
					am.summ.add(new Long(ll));
				}
				break;
			case 12:
				BigDecimal bd = (BigDecimal) summ.get(0);
				am.summ.add(bd);
				break;
			case 13:
				BigInteger bi = (BigInteger) summ.get(0);
				am.summ.add(bi);
				break;
			case 14:
				Complex cc = (Complex) summ.get(0);
				am.summ.add(cc);
				break;
			case 15:
				Phasor pp = (Phasor) summ.get(0);
				am.summ.add(pp);
				break;
			default:
				throw new IllegalArgumentException("Data type not identified by this method");
			}
		}

		am.productt = new ArrayList<Object>();
		if (productt.size() != 0) {
			switch (type) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 18:
				double dd = ((Double) productt.get(0)).doubleValue();
				am.productt.add(new Double(dd));
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 16:
			case 17:
				if (sumlongToDouble) {
					double dd2 = ((Double) productt.get(0)).doubleValue();
					am.productt.add(new Double(dd2));
				} else {
					long ll = ((Long) productt.get(0)).longValue();
					am.productt.add(new Long(ll));
				}
				break;
			case 12:
				BigDecimal bd = (BigDecimal) productt.get(0);
				am.productt.add(bd);
				break;
			case 13:
				BigInteger bi = (BigInteger) productt.get(0);
				am.productt.add(bi);
				break;
			case 14:
				Complex cc = (Complex) productt.get(0);
				am.productt.add(cc);
				break;
			case 15:
				Phasor pp = (Phasor) productt.get(0);
				am.productt.add(pp);
				break;
			default:
				throw new IllegalArgumentException("Data type not identified by this method");
			}
		}

		switch (type) {
		case 0:
		case 1:
			double[] dd = Conv.copy(this.getArray_as_double());
			for (int i = 0; i < length; i++)
				am.array.add(new Double(dd[i]));
			break;
		case 2:
		case 3:
			float[] ff = Conv.copy(this.getArray_as_float());
			for (int i = 0; i < length; i++)
				am.array.add(new Float(ff[i]));
			break;
		case 4:
		case 5:
			long[] ll = Conv.copy(this.getArray_as_long());
			for (int i = 0; i < length; i++)
				am.array.add(new Long(ll[i]));
			break;
		case 6:
		case 7:
			int[] ii = Conv.copy(this.getArray_as_int());
			for (int i = 0; i < length; i++)
				am.array.add(new Integer(ii[i]));
			break;
		case 8:
		case 9:
			short[] ss = Conv.copy(this.getArray_as_short());
			for (int i = 0; i < length; i++)
				am.array.add(new Short(ss[i]));
			break;
		case 10:
		case 11:
			byte[] bb = Conv.copy(this.getArray_as_byte());
			for (int i = 0; i < length; i++)
				am.array.add(new Byte(bb[i]));
			break;
		case 12:
			BigDecimal[] bd = Conv.copy(this.getArray_as_BigDecimal());
			for (int i = 0; i < length; i++)
				am.array.add(bd[i]);
			break;
		case 13:
			BigInteger[] bi = Conv.copy(this.getArray_as_BigInteger());
			for (int i = 0; i < length; i++)
				am.array.add(bi[i]);
			break;
		case 14:
			Complex[] ccc = this.getArray_as_Complex();
			for (int i = 0; i < length; i++)
				am.array.add(ccc[i].copy());
			break;
		case 15:
			Phasor[] ppp = this.getArray_as_Phasor();
			for (int i = 0; i < length; i++)
				am.array.add(ppp[i].copy());
			break;
		case 16:
		case 17:
			char[] cc = Conv.copy(this.getArray_as_char());
			for (int i = 0; i < length; i++)
				am.array.add(new Character(cc[i]));
			break;
		case 18:
			String[] sss = Conv.copy(this.getArray_as_String());
			for (int i = 0; i < length; i++)
				am.array.add(sss[i]);
			break;
		}

		return am;
	}

	public double curtosis() {
		return this.kurtosis_as_double();
	}

	public BigDecimal curtosis_as_BigDecimal() {
		return this.kurtosis_as_BigDecimal();
	}

	// CHI SQUARE DISTRIBUTION AND CHI SQUARE FUNCTIONS

	public double curtosis_as_double() {
		return this.kurtosis_as_double();
	}

	public double curtosisExcess() {
		return this.kurtosisExcess_as_double();
	}

	public BigDecimal curtosisExcess_as_BigDecimal() {
		return this.kurtosisExcess_as_BigDecimal();
	}

	public double curtosisExcess_as_double() {
		return this.kurtosisExcess_as_double();
	}

	// Return the effective sample number
	public double effectiveSampleNumber() {
		return this.effectiveSampleNumber_as_double();

	}

	public BigDecimal effectiveSampleNumber_as_BigDecimal() {
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		BigDecimal nEff = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			nEff = Stat.effectiveSampleNumber(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.weightingOptionS = holdW;
		return nEff;
	}

	public Complex effectiveSampleNumber_as_Complex() {
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		Complex nEff = Complex.zero();
		switch (type) {
		case 1:
		case 12:
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			nEff = Stat.effectiveSampleNumber(cc);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.weightingOptionS = holdW;
		return nEff;
	}

	public double effectiveSampleNumber_as_double() {
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double nEff = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			nEff = Stat.effectiveSampleNumber(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			nEff = Stat.effectiveSampleNumber(bd).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.weightingOptionS = holdW;
		return nEff;
	}

	public double excessCurtosis() {
		return this.kurtosisExcess_as_double();
	}

	public BigDecimal excessCurtosis_as_BigDecimal() {
		return this.kurtosisExcess_as_BigDecimal();
	}

	public double excessCurtosis_as_double() {
		return this.kurtosisExcess_as_double();
	}

	public double excessKurtosis() {
		return this.kurtosisExcess_as_double();
	}

	public BigDecimal excessKurtosis_as_BigDecimal() {
		return this.kurtosisExcess_as_BigDecimal();
	}

	public double excessKurtosis_as_double() {
		return kurtosisExcess_as_double();
	}

	// BINOMIAL DISTRIBUTION AND BINOMIAL COEFFICIENTS

	// Fit a data set to one, several or all of the above distributions (instance)
	public void fitOneOrSeveralDistributions() {
		double[] dd = this.getArray_as_double();
		Regression.fitOneOrSeveralDistributions(dd);
	}

	public double fPDF(double var1, int nu1, double var2, int nu2) {
		return fPDF(var1 / var2, nu1, nu2);
	}

	// F-distribution pdf
	public double fPDF(double fValue, int nu1, int nu2) {
		double numer = Math.pow(nu1 * fValue, nu1) * Math.pow(nu2, nu2);
		double dnu1 = nu1;
		double dnu2 = nu2;
		numer /= Math.pow(dnu1 * fValue + dnu2, dnu1 + dnu2);
		numer = Math.sqrt(numer);
		double denom = fValue * Stat.betaFunction(dnu1 / 2.0D, dnu2 / 2.0D);
		return numer / denom;
	}

	public double generalisedEntropyOneNat(double q, double r) {
		return generalizedEntropyOneNat(q, r);
	}

	public double generalisedMean(BigDecimal m) {
		return this.generalisedMean_as_double(m);
	}

	public double generalisedMean(double m) {
		return this.generalisedMean_as_double(m);
	}

	public Complex generalisedMean_as_Complex(Complex m) {
		Complex mean = Complex.zero();
		switch (type) {
		case 1:
		case 12:
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			mean = Stat.generalisedMean(cc, m);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	// F-DISTRIBUTION AND F-TEST

	public Complex generalisedMean_as_Complex(double m) {
		Complex mean = Complex.zero();
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = new Complex(Stat.generalisedMean(dd, m));
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = new Complex(Stat.generalisedMean(bd, m));
			bd = null;
			break;
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			mean = Stat.generalisedMean(cc, m);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double generalisedMean_as_double(BigDecimal m) {
		double mean = 0.0D;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.generalisedMean(bd, m);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double generalisedMean_as_double(double m) {
		double mean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = Stat.generalisedMean(dd, m);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.generalisedMean(bd, m);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	// GENERALIZED ENTROPY (INSTANCE METHODS)
	// return generalised entropy
	public double generalizedEntropyOneNat(double q, double r) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.generalizedEntropyOneNat(dd, q, r);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Generalized Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	public double generalizedMean(BigDecimal m) {
		return this.generalizedMean_as_double(m);
	}

	// GENERALIZED MEANS [POWER MEANS](INSTANCE)
	public double generalizedMean(double m) {
		return this.generalizedMean_as_double(m);
	}

	public Complex generalizedMean_as_Complex(Complex m) {
		Complex mean = Complex.zero();
		switch (type) {
		case 1:
		case 12:
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			mean = Stat.generalizedMean(cc, m);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public Complex generalizedMean_as_Complex(double m) {
		Complex mean = Complex.zero();
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = new Complex(Stat.generalizedMean(dd, m));
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = new Complex(Stat.generalizedMean(bd, m));
			bd = null;
			break;
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			mean = Stat.generalizedMean(cc, m);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double generalizedMean_as_double(BigDecimal m) {
		double mean = 0.0D;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.generalizedMean(bd, m);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double generalizedMean_as_double(double m) {
		double mean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = Stat.generalizedMean(dd, m);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.generalizedMean(bd, m);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;

	}

	// GEOMETRIC MEAN(INSTANCE)
	public double geometricMean() {
		return this.geometricMean_as_double();
	}

	public Complex geometricMean_as_Complex() {
		Complex gmean = Complex.zero();
		switch (type) {
		case 1:
		case 12:
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			gmean = Stat.geometricMean(cc);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return gmean;
	}

	// STUDENT'S T DISTRIBUTION

	public double geometricMean_as_double() {
		double gmean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			gmean = Stat.geometricMean(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			gmean = Stat.geometricMean(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot  be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");

		}
		return gmean;
	}

	// HARMONIC MEANS (INSTANCE)
	public double harmonicMean() {
		return this.harmonicMean_as_double();
	}

	public BigDecimal harmonicMean_as_BigDecimal() {

		BigDecimal mean = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.harmonicMean(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;

	}

	public Complex harmonicMean_as_Complex() {

		Complex mean = Complex.zero();
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = new Complex(Stat.harmonicMean(dd));
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = new Complex((Stat.harmonicMean(bd)).doubleValue());
			bd = null;
			break;
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			mean = Stat.harmonicMean(cc);
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;

	}

	public double harmonicMean_as_double() {

		double mean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = Stat.harmonicMean(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = (Stat.harmonicMean(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;

	}

	// INTERQUARTILE MEANS (INSTANCE)
	public double interQuartileMean() {
		return this.interQuartileMean_as_double();
	}

	public BigDecimal interQuartileMean_as_BigDecimal() {
		BigDecimal mean = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = Stat.interQuartileMean(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex interquartile mean is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double interQuartileMean_as_double() {
		double mean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			mean = Stat.interQuartileMean(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			mean = (Stat.interQuartileMean(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex interquartile mean is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	// KURTOSIS (INSTANCE METHODS)
	public double kurtosis() {
		return this.kurtosis_as_double();
	}

	public BigDecimal kurtosis_as_BigDecimal() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		BigDecimal kurtosis = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			kurtosis = Stat.kurtosis(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex kurtosis is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return kurtosis;
	}

	public double kurtosis_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double kurtosis = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			kurtosis = Stat.kurtosis(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			kurtosis = (Stat.kurtosis(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex kurtosis is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return kurtosis;
	}

	public double kurtosisExcess() {
		return this.kurtosisExcess_as_double();
	}

	public BigDecimal kurtosisExcess_as_BigDecimal() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		BigDecimal kurtosis = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			kurtosis = Stat.kurtosisExcess(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex kurtosis is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return kurtosis;
	}

	public double kurtosisExcess_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double kurtosis = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			kurtosis = Stat.kurtosisExcess(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			kurtosis = (Stat.kurtosisExcess(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex kurtosis is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return kurtosis;
	}

	public ArrayList<Object> lowerOutliersAnscombe(BigDecimal constant) {
		return this.lowerOutliersAnscombe_as_BigDecimal(constant);
	}

	public ArrayList<Object> lowerOutliersAnscombe(BigInteger constant) {
		return this.lowerOutliersAnscombe_as_BigDecimal(new BigDecimal(constant));
	}

	// Anscombe test for a lower outlier
	public ArrayList<Object> lowerOutliersAnscombe(double constant) {
		return this.lowerOutliersAnscombe_as_double(constant);
	}

	public ArrayList<Object> lowerOutliersAnscombe_as_BigDecimal(BigDecimal constant) {

		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			ArrayList<Object> ret = new ArrayList<Object>();
			ret = lowerOutliersAnscombeAsArrayList(dd, constant.doubleValue());
			lowerOutlierDetails.add(ret.get(0));
			Double[] dd1 = (Double[]) ret.get(1);
			ArrayMaths am1 = new ArrayMaths(dd1);
			lowerOutlierDetails.add(am1.getArray_as_BigDecimal());
			lowerOutlierDetails.add(ret.get(2));
			Double[] dd2 = (Double[]) ret.get(3);
			ArrayMaths am2 = new ArrayMaths(dd2);
			lowerOutlierDetails.add(am2.getArray_as_BigDecimal());
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			lowerOutlierDetails = lowerOutliersAnscombeAsArrayList(bd, constant);
			break;
		case 14:
			throw new IllegalArgumentException("Outlier detection of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		lowerDone = true;
		return lowerOutlierDetails;
	}

	public ArrayList<Object> lowerOutliersAnscombe_as_BigDecimal(BigInteger constant) {
		return this.lowerOutliersAnscombe_as_BigDecimal(new BigDecimal(constant));
	}

	// Anscombe test for a lower outlier
	public ArrayList<Object> lowerOutliersAnscombe_as_double(double constant) {

		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			lowerOutlierDetails = lowerOutliersAnscombeAsArrayList(dd, constant);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			ArrayList<Object> ret = new ArrayList<Object>();
			ret = lowerOutliersAnscombeAsArrayList(bd, new BigDecimal(constant));
			lowerOutlierDetails.add(ret.get(0));
			BigDecimal[] bd1 = (BigDecimal[]) ret.get(1);
			ArrayMaths am1 = new ArrayMaths(bd1);
			lowerOutlierDetails.add(am1.getArray_as_Double());
			lowerOutlierDetails.add(ret.get(2));
			BigDecimal[] bd2 = (BigDecimal[]) ret.get(3);
			ArrayMaths am2 = new ArrayMaths(bd2);
			lowerOutlierDetails.add(am2.getArray_as_Double());
			break;
		case 14:
			throw new IllegalArgumentException("Outlier detection of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		lowerDone = true;
		return lowerOutlierDetails;
	}

	// ARITMETIC MEANS (INSTANCE)
	public double mean() {
		return this.mean_as_double();

	}

	public BigDecimal mean_as_BigDecimal() {
		BigDecimal mean = BigDecimal.ZERO;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			double meand = 0.0D;
			for (int i = 0; i < length; i++)
				meand += dd[i];
			meand /= length;
			mean = new BigDecimal(meand);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			for (int i = 0; i < length; i++)
				mean = mean.add(bd[i]);
			mean = mean.divide(new BigDecimal((double) length), BigDecimal.ROUND_HALF_UP);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public Complex mean_as_Complex() {
		Complex mean = Complex.zero();
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			double meand = 0.0D;
			for (int i = 0; i < length; i++)
				meand += dd[i];
			meand /= length;
			mean = new Complex(meand);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			BigDecimal meanbd = BigDecimal.ZERO;
			for (int i = 0; i < length; i++)
				meanbd = meanbd.add(bd[i]);
			meanbd = meanbd.divide(new BigDecimal((double) length), BigDecimal.ROUND_HALF_UP);
			mean = new Complex(meanbd.doubleValue());
			bd = null;
			meanbd = null;
			break;
		case 14:
			Complex[] cc = this.getArray_as_Complex();
			for (int i = 0; i < length; i++)
				mean = mean.plus(cc[i]);
			mean = mean.over(new Complex(length));
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	public double mean_as_double() {
		double mean = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			for (int i = 0; i < length; i++) {
				mean += dd[i];
			}
			mean /= length;
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			BigDecimal meanbd = BigDecimal.ZERO;
			for (int i = 0; i < length; i++)
				meanbd = meanbd.add(bd[i]);
			meanbd = meanbd.divide(new BigDecimal((double) length), BigDecimal.ROUND_HALF_UP);
			mean = meanbd.doubleValue();
			bd = null;
			meanbd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return mean;
	}

	// Confidence limit of a mean
	// instance method
	public double[] meanConfidenceLimits(double prob) {
		double[] cl = new double[2];
		double probn = prob / 2.0 + 0.5;
		double mean = this.mean();
		double sd = this.standardDeviation();
		double z = Stat.gaussianInverseCDF(mean, sd, probn);
		cl[0] = 2.0 * mean - z;
		cl[1] = z;
		return cl;
	}

	// MEDIAN VALUE(INSTANCE)
	public double median() {
		return this.median_as_double();
	}

	// GUMBEL (TYPE I EXTREME VALUE) DISTRIBUTION

	public BigDecimal median_as_BigDecimal() {
		BigDecimal median = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			median = Stat.median(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex median value not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return median;
	}

	public double median_as_double() {
		double median = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			median = Stat.median(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			median = Stat.median(bd).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex median value not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return median;
	}

	// Median skewness
	public double medianSkewness() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double skewness = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			skewness = Stat.medianSkewness(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			skewness = Stat.medianSkewness(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex skewness is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return skewness;
	}

	public double medianSkewness_as_double() {
		return this.medianSkewness();
	}

	// SKEWNESS (INSTANCE METHODS)
	// Moment skewness
	public double momentSkewness() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double skewness = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			skewness = Stat.momentSkewness(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			skewness = Stat.momentSkewness(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex skewness is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return skewness;
	}

	public double momentSkewness_as_double() {
		return this.momentSkewness();
	}

	// quartile skewness as double
	public double quartileSkewness() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double skewness = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			skewness = Stat.quartileSkewness(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			skewness = Stat.quartileSkewness(bd).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex skewness is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return skewness;
	}

	// quartile skewness as BigDecimal
	public BigDecimal quartileSkewness_as_BigDecimal() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		BigDecimal skewness = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			skewness = Stat.quartileSkewness(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex skewness is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return skewness;
	}

	public double quartileSkewness_as_double() {
		return this.quartileSkewness();
	}

	// RENYI ENTROPY (INSTANCE METHODS)
	// return Renyi entropy as bits
	public double renyiEntropy(double alpha) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.renyiEntropy(dd, alpha);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Renyi Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Renyi entropy as bits
	public double renyiEntropyBit(double alpha) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.renyiEntropy(dd, alpha);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Renyi Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Renyi entropy as dits
	public double renyiEntropyDit(double alpha) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.renyiEntropyDit(dd, alpha);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Renyi Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Renyi entropy as nats
	public double renyiEntropyNat(double alpha) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.renyiEntropyNat(dd, alpha);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Renyi Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// ROOT MEAN SQUARE  (INSTANCE METHODS)
	public double rms() {
		double rms = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			rms = Stat.rms(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			rms = Stat.rms(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex root mean square is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return rms;
	}

	// SCALE (INSTANCE METHODS)
	// Scale the internal array to a new mean  and a new standard deviation
	public double[] scale(double mean, double sd) {
		double[] bb = null;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			bb = Stat.scale(dd, mean, sd);
			break;
		case 14:
			throw new IllegalArgumentException("Scaling of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return bb;
	}

	// Set standard deviation, variance and covariance denominators to n
	public void setDenominatorToN() {
		nFactorOptionI = true;
		nFactorReset = true;
	}

	// Set standard deviation, variance and covariance denominators to n-1
	public void setDenominatorToNminusOne() {
		nFactorOptionI = false;
		nFactorReset = true;
	}

	public void setWeights(ArrayList<Object> xx) {
		if (length != xx.size()) throw new IllegalArgumentException("Length of weights array, " + xx.size() + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(BigDecimal[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(BigInteger[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(byte[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Byte[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Complex[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	// ENTER AN ARRAY OF WEIGHTS
	public void setWeights(double[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Double[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(float[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Float[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(int[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Integer[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(long[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	// FRECHET (TYPE II EXTREME VALUE) DISTRIBUTION

	public void setWeights(Long[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Object[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Phasor[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(short[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Short[] xx) {
		if (length != xx.length) throw new IllegalArgumentException("Length of weights array, " + xx.length + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	public void setWeights(Vector<Object> xx) {
		if (length != xx.size()) throw new IllegalArgumentException("Length of weights array, " + xx.size() + ", must be the same as the length of the instance internal array, " + length);
		ArrayMaths wm = new ArrayMaths(xx);
		this.convertWeights(wm);
		weightsSupplied = true;
	}

	// INSTANCE METHODS
	// Set weights to 'big W' - multiplicative factor
	public void setWeightsToBigW() {
		weightingOptionI = false;
		weightingReset = true;
	}

	// Set weights to 'little w' - uncertainties
	public void setWeightsToLittleW() {
		weightingOptionI = true;
		weightingReset = true;
	}

	// SHANNON ENTROPY (INSTANCE METHODS)
	// return Shannon entropy as bits
	public double shannonEntropy() {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.shannonEntropy(dd);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Shannon Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Shannon entropy as bits
	public double shannonEntropyBit() {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.shannonEntropy(dd);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Shannon Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Shannon entropy as dits
	public double shannonEntropyDit() {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.shannonEntropyDit(dd);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Shannon Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// return Shannon entropy as nats
	public double shannonEntropyNat() {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.shannonEntropyNat(dd);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Shannon Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// STANDARD DEVIATIONS (INSTANCE METHODS)
	public double standardDeviation() {
		return this.standardDeviation_as_double();
	}

	public Complex standardDeviation_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		Complex variance = Complex.zero();
		Complex[] cc = this.getArray_as_Complex();
		variance = Stat.variance(cc);
		Stat.nFactorOptionS = hold;
		return Complex.sqrt(variance);
	}

	public double standardDeviation_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		Complex[] cc = this.getArray_as_Complex();
		double variance = Stat.varianceConjugateCalcn(cc);
		Stat.nFactorOptionS = hold;
		return Math.sqrt(variance);
	}

	public double standardDeviation_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		double variance = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			variance = Stat.variance(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			variance = (Stat.variance(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return Math.sqrt(variance);
	}

	public double standardDeviation_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] im = this.array_as_imaginary_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(im);
		Stat.nFactorOptionS = hold;
		return standardDeviation;
	}

	// WEIBULL (TYPE III EXTREME VALUE) DISTRIBUTION

	public double standardDeviation_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_modulus_of_Complex();
		double standardDeviation = Stat.standardDeviation(re);
		Stat.nFactorOptionS = hold;
		return standardDeviation;
	}

	public double standardDeviation_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_real_part_of_Complex();
		double standardDeviation = Stat.standardDeviation(re);
		Stat.nFactorOptionS = hold;
		return standardDeviation;
	}

	// STANDARD ERROR OF THE MEAN (INSTANCE METHODS)
	public double standardError() {
		return this.standardError_as_double();
	}

	public Complex standardError_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		Complex standardError = Complex.zero();
		Complex[] cc = this.getArray_as_Complex();
		standardError = Stat.standardError(cc);
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double standardError_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		Complex[] cc = this.getArray_as_Complex();
		double standardError = Stat.standardErrorConjugateCalcn(cc);
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double standardError_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		double standardError = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			standardError = Stat.standardError(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			standardError = Stat.standardError(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double standardError_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_imaginary_part_of_Complex();
		double standardError = Stat.standardError(re);
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double standardError_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_modulus_of_Complex();
		double standardError = Stat.standardError(re);
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double standardError_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_real_part_of_Complex();
		double standardError = Stat.standardError(re);
		Stat.nFactorOptionS = hold;
		return standardError;
	}

	public double[] standardise() {
		return standardize();
	}

	// STANDARDIZE (INSTANCE METHODS)
	// Standardization of the internal array to a mean of 0 and a standard deviation of 1
	public double[] standardize() {
		double[] bb = null;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			bb = Stat.standardize(dd);
			break;
		case 14:
			throw new IllegalArgumentException("Standardization of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return bb;
	}

	// SUBTRACT AN ARITMETIC MEAN FROM AN ARRAY (INSTANCE)
	public double[] subtractMean() {
		return this.subtractMean_as_double();
	}

	public BigDecimal[] subtractMean_as_BigDecimal() {
		BigDecimal[] arrayminus = new BigDecimal[length];
		switch (type) {
		case 1:
		case 12:
			BigDecimal meanb = this.mean_as_BigDecimal();
			ArrayMaths amb = this.minus(meanb);
			arrayminus = amb.getArray_as_BigDecimal();
			meanb = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return arrayminus;
	}

	public Complex[] subtractMean_as_Complex() {
		Complex[] arrayminus = new Complex[length];
		switch (type) {
		case 1:
			double meand = this.mean_as_double();
			ArrayMaths amd = this.minus(meand);
			arrayminus = amd.getArray_as_Complex();
			break;
		case 12:
			BigDecimal meanb = this.mean_as_BigDecimal();
			ArrayMaths amb = this.minus(meanb);
			arrayminus = amb.getArray_as_Complex();
			meanb = null;
			break;
		case 14:
			Complex meanc = this.mean_as_Complex();
			ArrayMaths amc = this.minus(meanc);
			arrayminus = amc.getArray_as_Complex();
			break;
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return arrayminus;
	}

	public double[] subtractMean_as_double() {
		double[] arrayminus = new double[length];
		switch (type) {
		case 1:
			double meand = this.mean_as_double();
			ArrayMaths amd = this.minus(meand);
			arrayminus = amd.getArray_as_double();
			break;
		case 12:
			BigDecimal meanb = this.mean_as_BigDecimal();
			ArrayMaths amb = this.minus(meanb);
			arrayminus = amb.getArray_as_double();
			meanb = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return arrayminus;
	}

	// SUBTRACT AN WEIGHTED ARITMETIC MEAN FROM AN ARRAY (INSTANCE)
	public double[] subtractWeightedMean() {
		return this.subtractWeightedMean_as_double();
	}

	public BigDecimal[] subtractWeightedMean_as_BigDecimal() {
		if (!weightsSupplied) {
			System.out.println("subtractWeightedMean_as_BigDecimal: no weights supplied - unweighted values returned");
			return this.subtractMean_as_BigDecimal();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			BigDecimal[] arrayminus = new BigDecimal[length];
			switch (type) {
			case 1:
			case 12:
				BigDecimal meanb = this.weightedMean_as_BigDecimal();
				ArrayMaths amb = this.minus(meanb);
				arrayminus = amb.getArray_as_BigDecimal();
				meanb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return arrayminus;
		}
	}

	public Complex[] subtractWeightedMean_as_Complex() {
		if (!weightsSupplied) {
			System.out.println("subtractWeightedMean_as_Complex: no weights supplied - unweighted values returned");
			return this.subtractMean_as_Complex();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			Complex[] arrayminus = new Complex[length];
			switch (type) {
			case 1:
				double meand = this.weightedMean_as_double();
				ArrayMaths amd = this.minus(meand);
				arrayminus = amd.getArray_as_Complex();
				break;
			case 12:
				BigDecimal meanb = this.weightedMean_as_BigDecimal();
				ArrayMaths amb = this.minus(meanb);
				arrayminus = amb.getArray_as_Complex();
				meanb = null;
				break;
			case 14:
				Complex meanc = this.weightedMean_as_Complex();
				ArrayMaths amc = this.minus(meanc);
				arrayminus = amc.getArray_as_Complex();
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return arrayminus;
		}
	}

	public double[] subtractWeightedMean_as_double() {
		if (!weightsSupplied) {
			System.out.println("subtractWeightedMean_as_double: no weights supplied - unweighted values returned");
			return this.subtractMean_as_double();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double[] arrayminus = new double[length];
			switch (type) {
			case 1:
				double meand = this.weightedMean_as_double();
				ArrayMaths amd = this.minus(meand);
				arrayminus = amd.getArray_as_double();
				break;
			case 12:
				BigDecimal meanb = this.weightedMean_as_BigDecimal();
				ArrayMaths amb = this.minus(meanb);
				arrayminus = amb.getArray_as_double();
				meanb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return arrayminus;
		}
	}

	// Return the true sample number
	public int trueSampleNumber() {
		return length;

	}

	public BigDecimal trueSampleNumber_as_BigDecimal() {
		return new BigDecimal(new Integer(length).toString());
	}

	public Complex trueSampleNumber_as_Complex() {
		return new Complex(length, 0.0);
	}

	// EXPONENTIAL DISTRIBUTION

	public double trueSampleNumber_as_double() {
		return length;
	}

	public int trueSampleNumber_as_int() {
		return length;
	}

	// TSALLIS ENTROPY (INSTANCE METHODS)
	// return Tsallis entropy
	public double tsallisEntropyNat(double q) {
		double entropy = 0.0D;
		switch (type) {
		case 1:
		case 12:
			double[] dd = this.getArray_as_double();
			entropy = Stat.tsallisEntropyNat(dd, q);
			break;
		case 14:
			throw new IllegalArgumentException("Complex Tsallis Entropy is not meaningful");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return entropy;
	}

	// Anscombe test for a upper outlier
	public ArrayList<Object> upperOutliersAnscombe(BigDecimal constant) {
		return this.upperOutliersAnscombe_as_BigDecimal(constant);
	}

	public ArrayList<Object> upperOutliersAnscombe(BigInteger constant) {
		return this.upperOutliersAnscombe_as_BigDecimal(new BigDecimal(constant));
	}

	// OUTLIER DETECTION (INSTANCE)
	// Anscombe test for a upper outlier
	public ArrayList<Object> upperOutliersAnscombe(double constant) {
		return this.upperOutliersAnscombe_as_double(constant);
	}

	// Anscombe test for a upper outlier
	public ArrayList<Object> upperOutliersAnscombe_as_BigDecimal(BigDecimal constant) {

		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			ArrayList<Object> ret = new ArrayList<Object>();
			ret = upperOutliersAnscombeAsArrayList(dd, constant.doubleValue());
			upperOutlierDetails.add(ret.get(0));
			Double[] dd1 = (Double[]) ret.get(1);
			ArrayMaths am1 = new ArrayMaths(dd1);
			upperOutlierDetails.add(am1.getArray_as_BigDecimal());
			upperOutlierDetails.add(ret.get(2));
			Double[] dd2 = (Double[]) ret.get(3);
			ArrayMaths am2 = new ArrayMaths(dd2);
			upperOutlierDetails.add(am2.getArray_as_BigDecimal());
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			upperOutlierDetails = upperOutliersAnscombeAsArrayList(bd, constant);
			break;
		case 14:
			throw new IllegalArgumentException("Outlier detection of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		upperDone = true;
		return upperOutlierDetails;
	}

	public ArrayList<Object> upperOutliersAnscombe_as_BigDecimal(BigInteger constant) {
		return this.upperOutliersAnscombe_as_BigDecimal(new BigDecimal(constant));
	}

	// Anscombe test for a upper outlier
	public ArrayList<Object> upperOutliersAnscombe_as_double(double constant) {

		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			upperOutlierDetails = upperOutliersAnscombeAsArrayList(dd, constant);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			ArrayList<Object> ret = new ArrayList<Object>();
			ret = upperOutliersAnscombeAsArrayList(bd, new BigDecimal(constant));
			upperOutlierDetails.add(ret.get(0));
			BigDecimal[] bd1 = (BigDecimal[]) ret.get(1);
			ArrayMaths am1 = new ArrayMaths(bd1);
			upperOutlierDetails.add(am1.getArray_as_Double());
			upperOutlierDetails.add(ret.get(2));
			BigDecimal[] bd2 = (BigDecimal[]) ret.get(3);
			ArrayMaths am2 = new ArrayMaths(bd2);
			upperOutlierDetails.add(am2.getArray_as_Double());
			break;
		case 14:
			throw new IllegalArgumentException("Outlier detection of Complex is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		upperDone = true;
		return upperOutlierDetails;
	}

	// Repalce number of data points to the effective sample number in weighted calculations
	public void useEffectiveN() {
		nEffOptionI = true;
		nEffReset = true;
	}

	// Repalce the effective sample number in weighted calculations by the number of data points
	public void useTrueN() {
		nEffOptionI = false;
		nEffReset = true;
	}

	// VARIANCES (INSTANCE METHODS)
	public double variance() {
		return this.variance_as_double();
	}

	public BigDecimal variance_as_BigDecimal() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		BigDecimal variance = BigDecimal.ZERO;
		switch (type) {
		case 1:
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			variance = Stat.variance(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return variance;
	}

	public Complex variance_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		Complex variance = Complex.zero();
		Complex[] cc = this.getArray_as_Complex();
		variance = Stat.variance(cc);
		Stat.nFactorOptionS = hold;
		return variance;
	}

	public double variance_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		Complex[] cc = this.getArray_as_Complex();
		double variance = Stat.varianceConjugateCalcn(cc);
		Stat.nFactorOptionS = hold;
		return variance;
	}

	public double variance_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double variance = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			variance = Stat.variance(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			variance = (Stat.variance(bd)).doubleValue();
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex cannot be converted to double");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		return variance;
	}

	// RAYLEIGH DISTRIBUTION

	public double variance_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] im = this.array_as_imaginary_part_of_Complex();
		double variance = Stat.variance(im);
		Stat.nFactorOptionS = hold;
		return variance;
	}

	public double variance_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_modulus_of_Complex();
		double variance = Stat.variance(re);
		Stat.nFactorOptionS = hold;
		return variance;
	}

	public double variance_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		double[] re = this.array_as_real_part_of_Complex();
		double variance = Stat.variance(re);
		Stat.nFactorOptionS = hold;
		return variance;
	}

	// VOLATILITY (INSTANCE METHODS)
	public double volatilityLogChange() {
		double volatilityLogChange = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			volatilityLogChange = Stat.volatilityLogChange(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			volatilityLogChange = Stat.volatilityLogChange(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex volatilty is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return volatilityLogChange;
	}

	public double volatilityPerCentChange() {
		double volatilityPerCentChange = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			volatilityPerCentChange = Stat.volatilityPerCentChange(dd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			volatilityPerCentChange = Stat.volatilityPerCentChange(bd);
			bd = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex volatilty is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		return volatilityPerCentChange;
	}

	public double weightedCoefficientOfVariation() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double coefficientOfVariation = 0.0D;
		switch (type) {
		case 1:
			double[] dd = this.getArray_as_double();
			double[] wd = amWeights.getArray_as_double();
			coefficientOfVariation = Stat.coefficientOfVariation(dd, wd);
			break;
		case 12:
			BigDecimal[] bd = this.getArray_as_BigDecimal();
			BigDecimal[] bw = amWeights.getArray_as_BigDecimal();
			coefficientOfVariation = Stat.coefficientOfVariation(bd, bw);
			bd = null;
			bw = null;
			break;
		case 14:
			throw new IllegalArgumentException("Complex coefficient of variation is not supported");
		default:
			throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
		}
		Stat.nFactorOptionS = hold;
		Stat.weightingOptionS = holdW;
		return coefficientOfVariation;
	}

	public double weightedGeneralisedMean(BigDecimal m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	public double weightedGeneralisedMean(double m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	public Complex weightedGeneralisedMean_as_Complex(Complex m) {
		return this.weightedGeneralizedMean_as_Complex(m);
	}

	public Complex weightedGeneralisedMean_as_Complex(double m) {
		return this.weightedGeneralizedMean_as_Complex(m);
	}

	public double weightedGeneralisedMean_as_double(BigDecimal m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	public double weightedGeneralisedMean_as_double(double m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	public double weightedGeneralizedMean(BigDecimal m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	// WEIGHTED GENERALIZED MEANS [WEIGHTED POWER MEANS](INSTANCE)
	public double weightedGeneralizedMean(double m) {
		return this.weightedGeneralizedMean_as_double(m);
	}

	public Complex weightedGeneralizedMean_as_Complex(Complex m) {
		Complex mean = Complex.zero();
		if (!weightsSupplied) {
			System.out.println("weightedGeneralizedMean_as_dComplex: no weights supplied - unweighted mean returned");
			return this.generalizedMean_as_Complex(m);
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			switch (type) {
			case 1:
			case 12:
			case 14:
				Complex[] cc = this.getArray_as_Complex();
				Complex[] cw = amWeights.getArray_as_Complex();
				mean = Stat.generalisedMean(cc, cw, m);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public Complex weightedGeneralizedMean_as_Complex(double m) {
		if (!weightsSupplied) {
			System.out.println("weightedGeneralizedMean_as_Complex: no weights supplied - unweighted mean returned");
			return this.generalizedMean_as_Complex(m);
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			Complex mean = Complex.zero();
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				mean = new Complex(Stat.generalisedMean(dd, ww, m));
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				mean = new Complex(Stat.generalisedMean(bd, wd, m));
				bd = null;
				break;
			case 14:
				Complex[] cc = this.getArray_as_Complex();
				Complex[] cw = amWeights.getArray_as_Complex();
				mean = Stat.generalisedMean(cc, cw, m);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	// PARETO DISTRIBUTION

	public double weightedGeneralizedMean_as_double(BigDecimal m) {
		if (!weightsSupplied) {
			System.out.println("weightedGeneralizedMean_as_double: no weights supplied - unweighted mean returned");
			return this.generalizedMean_as_double(m);
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double mean = 0.0D;
			switch (type) {
			case 1:
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				mean = Stat.generalisedMean(bd, wd, m);
				bd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public double weightedGeneralizedMean_as_double(double m) {
		if (!weightsSupplied) {
			System.out.println("weightedGeneralizedMean_as_double: no weights supplied - unweighted mean returned");
			return this.generalizedMean_as_double(m);
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double mean = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				mean = Stat.generalisedMean(dd, ww, m);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				mean = Stat.generalisedMean(bd, wd, m);
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	// WEIGHTED GEOMETRIC MEAN(INSTANCE)
	public double weightedGeometricMean() {
		return this.weightedGeometricMean_as_double();
	}

	public Complex weightedGeometricMean_as_Complex() {
		if (!weightsSupplied) {
			System.out.println("weightedGeometricMean_as_Complex: no weights supplied - unweighted value returned");
			return this.geometricMean_as_Complex();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			Complex gmean = Complex.zero();
			switch (type) {
			case 1:
			case 12:
			case 14:
				Complex[] cc = this.getArray_as_Complex();
				Complex[] ww = this.getArray_as_Complex();
				gmean = Stat.geometricMean(cc, ww);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return gmean;
		}
	}

	public double weightedGeometricMean_as_double() {
		if (!weightsSupplied) {
			System.out.println("weightedGeometricMean_as_double: no weights supplied - unweighted value returned");
			return this.geometricMean_as_double();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double gmean = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = this.getArray_as_double();
				gmean = Stat.geometricMean(dd, ww);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = this.getArray_as_BigDecimal();
				gmean = Stat.geometricMean(bd, wd);
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot  be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");

			}
			Stat.weightingOptionS = holdW;
			return gmean;
		}
	}

	// WEIGHTED HARMONIC MEANS (INSTANCE)
	public double weightedHarmonicMean() {
		return this.weightedHarmonicMean_as_double();
	}

	public BigDecimal weightedHarmonicMean_as_BigDecimal() {
		if (!weightsSupplied) {
			System.out.println("weightedHarmonicMean_as_BigDecimal: no weights supplied - unweighted mean returned");
			return this.harmonicMean_as_BigDecimal();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			BigDecimal mean = BigDecimal.ZERO;
			switch (type) {
			case 1:
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = Stat.harmonicMean(bd, wwb);
				bd = null;
				wwb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public Complex weightedHarmonicMean_as_Complex() {
		if (!weightsSupplied) {
			System.out.println("weightedHarmonicMean_as_Complex: no weights supplied - unweighted mean returned");
			return this.harmonicMean_as_Complex();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			Complex mean = Complex.zero();
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] wwd = amWeights.getArray_as_double();
				mean = new Complex(Stat.harmonicMean(dd, wwd));
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = new Complex((Stat.harmonicMean(bd, wwb)).doubleValue());
				bd = null;
				wwb = null;
				break;
			case 14:
				Complex[] cc = this.getArray_as_Complex();
				Complex[] wwc = amWeights.getArray_as_Complex();
				mean = Stat.harmonicMean(cc, wwc);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public double weightedHarmonicMean_as_double() {
		if (!weightsSupplied) {
			System.out.println("weightedHarmonicMean_as_double: no weights supplied - unweighted mean returned");
			return this.harmonicMean_as_double();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double mean = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] wwd = amWeights.getArray_as_double();
				mean = Stat.harmonicMean(dd, wwd);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = (Stat.harmonicMean(bd, wwb)).doubleValue();
				bd = null;
				wwb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	// WEIGHTED ARITMETIC MEANS (INSTANCE)
	public double weightedMean() {
		return this.weightedMean_as_double();
	}

	public BigDecimal weightedMean_as_BigDecimal() {
		if (!weightsSupplied) {
			System.out.println("weightedMean_as_BigDecimal: no weights supplied - unweighted mean returned");
			return this.mean_as_BigDecimal();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			BigDecimal mean = BigDecimal.ZERO;
			switch (type) {
			case 1:
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = Stat.mean(bd, wwb);
				bd = null;
				wwb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public Complex weightedMean_as_Complex() {
		if (!weightsSupplied) {
			System.out.println("weightedMean_as_Complex: no weights supplied - unweighted mean returned");
			return this.mean_as_Complex();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			Complex mean = Complex.zero();
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] wwd = amWeights.getArray_as_double();
				mean = new Complex(Stat.mean(dd, wwd));
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = new Complex((Stat.mean(bd, wwb)).doubleValue());
				bd = null;
				wwb = null;
				break;
			case 14:
				Complex[] cc = this.getArray_as_Complex();
				Complex[] wwc = amWeights.getArray_as_Complex();
				mean = Stat.mean(cc, wwc);
				break;
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	public double weightedMean_as_double() {
		if (!weightsSupplied) {
			System.out.println("weightedMean_as_double: no weights supplied - unweighted mean returned");
			return this.mean_as_double();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double mean = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] wwd = amWeights.getArray_as_double();
				mean = Stat.mean(dd, wwd);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wwb = amWeights.getArray_as_BigDecimal();
				mean = (Stat.mean(bd, wwb)).doubleValue();
				bd = null;
				wwb = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return mean;
		}
	}

	// WEIGHTED ROOT MEAN SQUARE  (INSTANCE METHODS)
	public double weightedRms() {
		if (!weightsSupplied) {
			System.out.println("weightedRms: no weights supplied - unweighted rms returned");
			return this.rms();
		} else {
			boolean holdW = Stat.weightingOptionS;
			if (weightingReset) {
				if (weightingOptionI) {
					Stat.weightingOptionS = true;
				} else {
					Stat.weightingOptionS = false;
				}
			}
			double rms = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				rms = Stat.rms(dd, ww);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				rms = Stat.rms(bd, wd);
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex root mean square is not supported");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			Stat.weightingOptionS = holdW;
			return rms;
		}
	}

	// WEIGHTED STANDARD DEVIATION (INSTANCE METHODS)
	public double weightedStandardDeviation() {
		return this.weightedStandardDeviation_as_double();
	}

	// FITTING DATA TO ABOVE DISTRIBUTIONS

	public Complex weightedStandardDeviation_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}

		Complex varr = Complex.zero();
		if (!weightsSupplied) {
			System.out.println("weightedtandardDeviationS_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_as_Complex();
		} else {
			Complex variance = Complex.zero();
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			variance = Stat.variance(cc, wc);
			varr = Complex.sqrt(variance);
		}
		Stat.nFactorOptionS = hold;
		Stat.weightingOptionS = holdW;
		return varr;

	}

	public double weightedStandardDeviation_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedtandardDeviationS_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_as_Complex_ConjugateCalcn();
		} else {
			double variance = Double.NaN;
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			variance = Stat.varianceConjugateCalcn(cc, wc);
			varr = Math.sqrt(variance);
		}
		Stat.nFactorOptionS = hold;
		Stat.weightingOptionS = holdW;
		return varr;

	}

	// OUTLIER TESTING (STATIC)

	public double weightedStandardDeviation_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}

		double varr = 0.0;
		if (!weightsSupplied) {
			System.out.println("weightedStandardDeviation_as_double: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_as_double();
		} else {
			double variance = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				variance = Stat.variance(dd, ww);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				variance = (Stat.variance(bd, wd)).doubleValue();
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			varr = Math.sqrt(variance);

		}
		Stat.nFactorOptionS = hold;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedStandardDeviation_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardDeviation_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_of_ComplexImaginaryParts();
		} else {
			double[] cc = this.array_as_imaginary_part_of_Complex();
			double[] wc = amWeights.array_as_imaginary_part_of_Complex();
			varr = Stat.standardDeviation(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedStandardDeviation_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardDeviation_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_of_ComplexModuli();
		} else {
			double[] cc = this.array_as_modulus_of_Complex();
			double[] wc = amWeights.array_as_modulus_of_Complex();
			varr = Stat.standardDeviation(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedStandardDeviation_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardDeviation_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardDeviation_of_ComplexRealParts();
		} else {
			double[] cc = this.array_as_real_part_of_Complex();
			double[] wc = amWeights.array_as_real_part_of_Complex();
			varr = Stat.standardDeviation(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	// WEIGHTED STANDARD ERROR OF THE MEAN (INSTANCE METHODS)
	public double weightedStandardError() {
		return this.weightedStandardError_as_double();
	}

	public double weightedStandardError_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}

		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}

		double standardError = 0.0;
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_double: no weights supplied - unweighted value returned");
			standardError = this.standardError_as_double();
		} else {
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				standardError = Stat.standardError(dd, ww);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				standardError = Stat.standardError(bd, wd);
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			standardError = Math.sqrt(standardError);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return standardError;
	}

	public double weightedStandardError_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardError_of_ComplexImaginaryParts();
		} else {
			double[] cc = this.array_as_imaginary_part_of_Complex();
			double[] wc = amWeights.array_as_imaginary_part_of_Complex();
			varr = Stat.standardError(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedStandardError_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardError_of_ComplexModuli();
		} else {
			double[] cc = this.array_as_modulus_of_Complex();
			double[] wc = amWeights.array_as_modulus_of_Complex();
			varr = Stat.standardError(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedStandardError_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_Complex: no weights supplied - unweighted value returned");
			varr = this.standardError_of_ComplexRealParts();
		} else {
			double[] cc = this.array_as_real_part_of_Complex();
			double[] wc = amWeights.array_as_real_part_of_Complex();
			varr = Stat.standardError(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public Complex weightedStandarError_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}

		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}

		Complex standardError = Complex.zero();
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_Complex: no weights supplied - unweighted value returned");
			standardError = this.standardError_as_Complex();
		} else {
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			standardError = Stat.standardError(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;

		return standardError;

	}

	public double weightedStandarError_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}

		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}

		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double standardError = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedStandardError_as_Complex: no weights supplied - unweighted value returned");
			standardError = this.standardError_as_Complex_ConjugateCalcn();
		} else {
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			standardError = Stat.standardErrorConjugateCalcn(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;

		return standardError;

	}

	// WEIGHTED VARIANCES (INSTANCE METHODS)
	public double weightedVariance() {
		return this.weightedVariance_as_double();
	}

	public BigDecimal weightedVariance_as_BigDecimal() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		BigDecimal varr = BigDecimal.ZERO;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_BigDecimal: no weights supplied - unweighted value returned");
			varr = this.variance_as_BigDecimal();
		} else {
			BigDecimal weightedVariance = BigDecimal.ZERO;
			switch (type) {
			case 1:
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				weightedVariance = Stat.variance(bd, wd);
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to BigDecimal");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			varr = weightedVariance;
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public Complex weightedVariance_as_Complex() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		Complex varr = Complex.zero();
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_Complex: no weights supplied - unweighted value returned");
			varr = this.variance_as_Complex();
		} else {
			Complex weightedVariance = Complex.zero();
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			weightedVariance = Stat.variance(cc, wc);
			varr = weightedVariance;
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedVariance_as_Complex_ConjugateCalcn() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_Complex: no weights supplied - unweighted value returned");
			varr = this.variance_as_Complex_ConjugateCalcn();
		} else {
			Complex[] cc = this.getArray_as_Complex();
			Complex[] wc = amWeights.getArray_as_Complex();
			varr = Stat.varianceConjugateCalcn(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedVariance_as_double() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}

		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_double: no weights supplied - unweighted value returned");
			varr = this.variance_as_double();
		} else {
			double weightedVariance = 0.0D;
			switch (type) {
			case 1:
				double[] dd = this.getArray_as_double();
				double[] ww = amWeights.getArray_as_double();
				weightedVariance = Stat.variance(dd, ww);
				break;
			case 12:
				BigDecimal[] bd = this.getArray_as_BigDecimal();
				BigDecimal[] wd = amWeights.getArray_as_BigDecimal();
				weightedVariance = (Stat.variance(bd, wd)).doubleValue();
				bd = null;
				wd = null;
				break;
			case 14:
				throw new IllegalArgumentException("Complex cannot be converted to double");
			default:
				throw new IllegalArgumentException("This type number, " + type + ", should not be possible here!!!!");
			}
			varr = weightedVariance;
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;

	}

	public double weightedVariance_of_ComplexImaginaryParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_Complex: no weights supplied - unweighted value returned");
			varr = this.variance_of_ComplexImaginaryParts();
		} else {
			double[] cc = this.array_as_imaginary_part_of_Complex();
			double[] wc = amWeights.array_as_imaginary_part_of_Complex();
			varr = Stat.variance(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedVariance_of_ComplexModuli() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_Complex: no weights supplied - unweighted value returned");
			varr = this.variance_of_ComplexModuli();
		} else {
			double[] cc = this.array_as_modulus_of_Complex();
			double[] wc = amWeights.array_as_modulus_of_Complex();
			varr = Stat.variance(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

	public double weightedVariance_of_ComplexRealParts() {
		boolean hold = Stat.nFactorOptionS;
		if (nFactorReset) {
			if (nFactorOptionI) {
				Stat.nFactorOptionS = true;
			} else {
				Stat.nFactorOptionS = false;
			}
		}
		boolean hold2 = Stat.nEffOptionS;
		if (nEffReset) {
			if (nEffOptionI) {
				Stat.nEffOptionS = true;
			} else {
				Stat.nEffOptionS = false;
			}
		}
		boolean holdW = Stat.weightingOptionS;
		if (weightingReset) {
			if (weightingOptionI) {
				Stat.weightingOptionS = true;
			} else {
				Stat.weightingOptionS = false;
			}
		}
		double varr = Double.NaN;
		if (!weightsSupplied) {
			System.out.println("weightedVariance_as_Complex: no weights supplied - unweighted value returned");
			varr = this.variance_of_ComplexRealParts();
		} else {
			double[] cc = this.array_as_real_part_of_Complex();
			double[] wc = amWeights.array_as_real_part_of_Complex();
			varr = Stat.variance(cc, wc);
		}
		Stat.nFactorOptionS = hold;
		Stat.nEffOptionS = hold2;
		Stat.weightingOptionS = holdW;
		return varr;
	}

}

// Class to evaluate the Student's t-function
class StudentTfunct implements RealRootFunction {
	public int nu = 0;
	public double cfd = 0.0D;

	@Override
	public double function(double x) {

		double y = cfd - Stat.studentTcdf(x, nu);

		return y;
	}
}