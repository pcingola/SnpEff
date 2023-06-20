package org.snpeff.probablility;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import DistLib.Constants;

/**
 * Calculate Normal distribution (PDF & CDF) using more precision if required
 * 
 * @author pcingola
 *
 */
public class NormalDistribution {

	static final double SIXTEN = 1.6; /* Magic Cutoff */
	public static final double MAX_NORM_DOUBLE = 700;

	public static Apfloat pdf(double x, double mu, double sigma) {
		double z = ((x - mu) * (x - mu)) / (2.0 * sigma * sigma);
		if (z > MAX_NORM_DOUBLE) return pdfApfloat(x, mu, sigma);
		double pdf = DistLib.normal.density(x, mu, sigma);
		return new Apfloat(pdf);
	}

	public static Apfloat pdfApfloat(double x, double mu, double sigma) {
		Apfloat apk = new Apfloat(1.0 / (sigma * Math.sqrt(2.0 * Math.PI)));
		Apfloat zAp = new Apfloat(-((x - mu) * (x - mu)) / (2.0 * sigma * sigma));
		Apfloat pdfAp = ApfloatMath.exp(zAp).multiply(apk);
		return pdfAp;
	}

	/**  DESCRIPTION 
	 *    The main computation evaluates near-minimax approximations derived
	 *    from those in "Rational Chebyshev approximations for the error
	 *    function" by W. J. Cody, Math. Comp., 1969, 631-637.  This
	 *    transportable program uses rational functions that theoretically
	 *    approximate the normal distribution function to at least 18
	 *    significant decimal digits.  The accuracy achieved depends on the
	 *    arithmetic system, the compiler, the intrinsic functions, and
	 *    proper selection of the machine-dependent constants.
	 *
	 *  REFERENCE
	 *
	 *    Cody, W. D. (1993).
	 *    ALGORITHM 715: SPECFUN - A Portable FORTRAN Package of
	 *    Special Function Routines and Test Drivers".
	 *    ACM Transactions on Mathematical Software. 19, 22-32.
	 */

	public static Apfloat cdf(double x, double mu, double sigma) {
		final double c[] = { 0.39894151208813466764, 8.8831497943883759412, 93.506656132177855979, 597.27027639480026226, 2494.5375852903726711, 6848.1904505362823326, 11602.651437647350124, 9842.7148383839780218, 1.0765576773720192317e-8 };
		final double d[] = { 22.266688044328115691, 235.38790178262499861, 1519.377599407554805, 6485.558298266760755, 18615.571640885098091, 34900.952721145977266, 38912.003286093271411, 19685.429676859990727 };
		final double p[] = { 0.21589853405795699, 0.1274011611602473639, 0.022235277870649807, 0.001421619193227893466, 2.9112874951168792e-5, 0.02307344176494017303 };
		final double q[] = { 1.28426009614491121, 0.468238212480865118, 0.0659881378689285515, 0.00378239633202758244, 7.29751555083966205e-5 };
		final double a[] = { 2.2352520354606839287, 161.02823106855587881, 1067.6894854603709582, 18154.981253343561249, 0.065682337918207449113 };
		final double b[] = { 47.20258190468824187, 976.09855173777669322, 10260.932208618978205, 45507.789335026729956 };

		double xden, temp, xnum, result, ccum;
		// double del, min, eps, xsq;
		double del, eps, xsq;
		double y;
		int i;

		/* Note: The structure of these checks has been */
		/* carefully thought through.  For example, if x == mu */
		/* and sigma == 0, we still get the correct answer. */

		if (Double.isNaN(x) || Double.isNaN(mu) || Double.isNaN(sigma)) return new Apfloat(x + mu + sigma);
		if (sigma < 0) throw new java.lang.ArithmeticException("Math Error: DOMAIN");

		x = (x - mu) / sigma;

		if (Double.isInfinite(x)) {
			if (x < 0) return Apfloat.ZERO;
			else return Apfloat.ONE;
		}

		eps = Constants.DBL_EPSILON * 0.5;
		// min = Double.MIN_VALUE;
		y = java.lang.Math.abs(x);

		if (y <= 0.66291) {
			xsq = 0.0;
			if (y > eps) {
				xsq = x * x;
			}
			xnum = a[4] * xsq;
			xden = xsq;
			for (i = 1; i <= 3; ++i) {
				xnum = (xnum + a[i - 1]) * xsq;
				xden = (xden + b[i - 1]) * xsq;
			}
			result = x * (xnum + a[3]) / (xden + b[3]);
			temp = result;
			result = 0.5 + temp;
			ccum = 0.5 - temp;
		} else if (y <= Constants.M_SQRT_32) {

			/* Evaluate pnorm for 0.66291 <= |z| <= sqrt(32) */
			xnum = c[8] * y;
			xden = y;
			for (i = 1; i <= 7; ++i) {
				xnum = (xnum + c[i - 1]) * y;
				xden = (xden + d[i - 1]) * y;
			}
			result = (xnum + c[7]) / (xden + d[7]);
			/*!* 	xsq = floor(y * SIXTEN) / SIXTEN; *!*/
			xsq = java.lang.Math.floor(y * SIXTEN) / SIXTEN;
			del = (y - xsq) * (y + xsq);
			/*!* 	result = exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * result; *!*/
			result = java.lang.Math.exp(-xsq * xsq * 0.5) * java.lang.Math.exp(-del * 0.5) * result;
			ccum = 1.0 - result;
			if (x > 0.0) {
				temp = result;
				result = ccum;
				ccum = temp;
			}
		} else if (y < 38) {
			/* Evaluate pnorm for sqrt(32) < |z| < 38 */
			result = 0.0;
			xsq = 1.0 / (x * x);
			xnum = p[5] * xsq;
			xden = xsq;
			for (i = 1; i <= 4; ++i) {
				xnum = (xnum + p[i - 1]) * xsq;
				xden = (xden + q[i - 1]) * xsq;
			}
			result = xsq * (xnum + p[4]) / (xden + q[4]);
			result = (Constants.M_1_SQRT_2PI - result) / y;
			/*!* 	xsq = floor(x * SIXTEN) / SIXTEN; *!*/
			xsq = java.lang.Math.floor(x * SIXTEN) / SIXTEN;
			del = (x - xsq) * (x + xsq);
			/*!* 	result = exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * result; *!*/
			result = java.lang.Math.exp(-xsq * xsq * 0.5) * java.lang.Math.exp(-del * 0.5) * result;
			ccum = 1.0 - result;
			if (x > 0.0) {
				temp = result;
				result = ccum;
				ccum = temp;
			}
		} else {
			Apfloat res = cdfApfloat(x, y, p, q);
			return res;
		}

		return new Apfloat(result);
	}

	/**
	 * CDF using apfloat
	 * @param x
	 * @param y
	 * @param p
	 * @param q
	 * @return
	 */
	public static Apfloat cdfApfloat(double x, double y, double p[], double q[]) {
		Apfloat xAp = new Apfloat(x);
		Apfloat mhalf = new Apfloat(-0.5); // -1/2

		/* Evaluate pnorm for sqrt(32) < |z| < 50 */
		Apfloat result = new Apfloat(0.0);
		Apfloat xsq = new Apfloat(1.0 / (x * x));
		Apfloat xnum = xsq.multiply(new Apfloat(p[5]));
		Apfloat xden = xsq;
		for (int i = 1; i <= 4; ++i) {
			xnum = xnum.add(new Apfloat(p[i - 1])).multiply(xsq);
			xden = xden.add(new Apfloat(q[i - 1])).multiply(xsq);
		}
		result = xsq.multiply((xnum.add(new Apfloat(p[4]))).divide(xden.add(new Apfloat(q[4]))));
		result = new Apfloat(Constants.M_1_SQRT_2PI).subtract(result).divide(new Apfloat(y));
		/*!* 	xsq = floor(x * SIXTEN) / SIXTEN; *!*/
		xsq = new Apfloat(java.lang.Math.floor(x * SIXTEN) / SIXTEN);
		Apfloat del = xAp.subtract(xsq).multiply(xAp.add(xsq));

		/*!* 	result = exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * result; *!*/
		result = ApfloatMath.exp(xsq.multiply(xsq).multiply(mhalf)).multiply(ApfloatMath.exp(del.multiply(mhalf))).multiply(result);
		Apfloat ccum = Apfloat.ONE.subtract(result);
		if (x > 0.0) {
			Apfloat temp = result;
			result = ccum;
			ccum = temp;
		}

		return result;
	}

}
