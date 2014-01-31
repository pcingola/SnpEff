/*
*   RefractiveIndex Class
*
*   Methods for returning the refractive index, for a given wavelength,
*   of gold, silver, fused quartz, crown glass, float glass,
*   microscope slide glass, polymethacrylate, air,
*   water, sodium chloride solutions, sucrose solutions, PVA solutions.
*   Methods for calculating refractive index of mixtures and interconverting
*   absorption coefficients and imaginary refractive indices.
*
*   Methods for returning the physical properties of water:
*   viscosity, density, refractive index,
*   electrical permittivity, molecular weight.
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: 28 February 2006 REPLACING RefrIndex (which was created July 2003 and updated 1 July 2003, 1 May 2004)
*   Revision:   6 March 2006,  17 November 2006, 7 October 2007
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RefractiveIndex.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) February 2006   Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.optics;

import flanagan.interpolation.CubicSpline;
import flanagan.interpolation.BiCubicSpline;
import flanagan.complex.Complex;
import flanagan.physprop.*;

public class RefractiveIndex{

    private static double imagPlusMinus = -1.0D;    // = -1;  complex refractive index = n - jk [default option]
                                                    // = +1;  complex refractive index = n + jk

    //  METHODS

    // Resets complex refractive index as n + jk
    public static void setComlexImagAsPositive(){
        RefractiveIndex.imagPlusMinus = 1.0D;
    }

    // Resets complex refractive index as n - jk (default option)
    public static void setComlexImagAsNegative(){
        RefractiveIndex.imagPlusMinus = -1.0D;
    }

	// Returns a complex refractive index given the real part of the refractive index (riReal),
	// the extinction coefficient (extCoeff)and the concentration of the absorbing species (concn)
	// and the wavelength (wavl).  For a pure material with a given absorption coefficient set concn
	// to unity and extCoeff to the value of the absorption coefficient.
	// The units of the concentration, the absorption coefficient and the wavelength should match.
	public static Complex absToComplex(double riReal, double extCoeff, double concn, double wavl){
	    Complex ri = new Complex();
	    ri.reset(riReal, extCoeff*concn*wavl/(4.0D*Math.PI));
	    return ri;
	}

	// Returns the absorption coefficient ( units - reciprocal metres) that corresponds
	// to the imaginary part of a complex refractive index (riImag) at a wavelength wavl (metres)
	public static double imagToAbs(double riImag, double wavl){
	    return 4.0D*riImag*Math.PI/wavl;
	}

    // Returns the complex refractive index of GOLD for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Data - P.B. Johnson and R.W. Christy, Phys. Rev. B. 6(12) 4370-4379, 1972
    public static Complex gold(double wavelength){
        // wavelengths
  	    double[]	wavl = {1.87855e-007, 1.91629e-007, 1.9525e-007, 1.99331e-007, 2.03253e-007, 2.07331e-007, 2.11939e-007, 2.16377e-007, 2.214e-007, 2.26248e-007, 2.31314e-007, 2.37063e-007, 2.4263e-007, 2.48964e-007, 2.55111e-007, 2.6157e-007, 2.68946e-007, 2.76134e-007, 2.84367e-007, 2.92415e-007, 3.00932e-007, 3.10737e-007, 3.20372e-007, 3.31508e-007, 3.42497e-007, 3.5424e-007, 3.67905e-007, 3.81489e-007, 3.97385e-007, 4.1328e-007, 4.305e-007, 4.50851e-007, 4.71422e-007, 4.95936e-007, 5.20941e-007, 5.48602e-007, 5.82085e-007, 6.16836e-007, 6.5949e-007, 7.04455e-007, 7.56e-007, 8.21086e-007, 8.91972e-007, 9.84e-007, 1.08758e-006, 1.21553e-006, 1.39308e-006, 1.61018e-006, 1.93725e-006};
	    // refractive index - real part
	    double[]	rfInRe = {1.28, 1.32, 1.34, 1.33, 1.33, 1.3, 1.3, 1.3, 1.3, 1.31, 1.3, 1.32, 1.32, 1.33, 1.33, 1.35, 1.38, 1.43, 1.47, 1.49, 1.53, 1.53, 1.54, 1.48, 1.48, 1.5, 1.48, 1.46, 1.47, 1.46, 1.45, 1.38, 1.31, 1.04, 0.62, 0.43, 0.29, 0.21, 0.14, 0.13, 0.14, 0.16, 0.17, 0.22, 0.27, 0.35, 0.43, 0.56, 0.92};
	    // refractive index - imaginary part
	    double[]	rfInIm = {1.188, 1.203, 1.226, 1.251, 1.277, 1.304, 1.35, 1.387, 1.427, 1.46, 1.497, 1.536, 1.577, 1.631, 1.688, 1.749, 1.803, 1.847, 1.869, 1.878, 1.889, 1.893, 1.898, 1.883, 1.871, 1.866, 1.895, 1.933, 1.952, 1.958, 1.948, 1.914, 1.849, 1.833, 2.081, 2.455, 2.863, 3.272, 3.697, 4.103, 4.542, 5.083, 5.663, 6.35, 7.15, 8.145, 9.519, 11.21, 13.78};
	    // second derivatives - real part
        double[]	derivRe = {0, -1.17631e+015, -3.60547e+015, 2.92961e+015, -4.45568e+015, 3.84052e+015, -9.56586e+014, -8.80015e+013, 1.17669e+015, -2.14766e+015, 2.49889e+015, -1.81842e+015, 1.06253e+015, -8.99034e+014, 1.01496e+015, -2.29767e+014, 7.62849e+014, -4.44179e+014, -5.30697e+014, 8.3214e+014, -1.17757e+015, 8.04127e+014, -1.40022e+015, 1.06549e+015, 7.02889e+013, -3.99e+014, 3.29142e+013, 2.65475e+014, -2.19619e+014, 1.38062e+014, -3.11414e+014, 1.90132e+014, -4.37649e+014, -4.12661e+014, 6.75965e+014, -4.75615e+013, 9.68919e+013, -1.02252e+013, 5.115e+013, -3.33214e+011, 5.09764e+012, -7.56272e+012, 1.02639e+013, -4.28914e+012, 3.57066e+012, -2.76676e+012, 1.04546e+012, 2.55831e+012, 0};
	    // second derivatives - imaginary part
        double[]	derivIm = {0, 1.07557e+015, -4.54022e+014, 4.27299e+014, -5.01416e+014, 1.54402e+015, -9.99891e+014, 2.48281e+014, -4.98264e+014, 3.40549e+014, -2.67834e+014, 1.65108e+014, 2.31595e+014, 8.3984e+013, 1.49838e+014, -5.0561e+014, 3.84443e+013, -6.38396e+014, -1.55687e+014, 1.24515e+014, -2.15188e+014, 1.55368e+014, -3.38857e+014, 1.24305e+014, -1.79372e+013, 2.93519e+014, 4.26729e+013, -1.68238e+014, -1.7188e+013, -7.16957e+013, -4.22518e+013, -1.04677e+014, 2.39351e+013, 6.13432e+014, 8.33595e+013, -9.04645e+013, 2.22081e+013, -7.1846e+013, -1.13135e+013, -1.24725e+013, -3.07173e+012, 2.01135e+012, -1.58933e+013, 7.97278e+012, -1.02502e+012, -2.60397e+011, 3.57043e+011, 3.07012e+011, 0};
        int n = wavl.length;
        double  yRe, yIm;
        Complex	ri = new Complex();

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
		    // cubic spline interpolation - imaginary part
		    yIm=CubicSpline.interpolate(wavelength, wavl, rfInIm, derivIm);
		    ri.reset(yRe, RefractiveIndex.imagPlusMinus*yIm);
        }
	    else{
		    throw new IllegalArgumentException("Wavelength is outside the limits (187.86nm - 1937.2nm) of the tabulated data");
		}
	    return ri;
    }

    // Returns the complex refractive index of SILVER for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Data - P.B. Johnson and R.W. Christy, Phys. Rev. B. 6(12) 4370-4379, 1972
    public static Complex silver(double wavelength){
        // wavelengths
  	    double[]	wavl = {1.87855e-007, 1.91629e-007, 1.9525e-007, 1.99331e-007, 2.03253e-007, 2.07331e-007, 2.11939e-007, 2.16377e-007, 2.214e-007, 2.26248e-007, 2.31314e-007, 2.37063e-007, 2.4263e-007, 2.48964e-007, 2.55111e-007, 2.6157e-007, 2.68946e-007, 2.76134e-007, 2.84367e-007, 2.92415e-007, 3.00932e-007, 3.10737e-007, 3.20372e-007, 3.31508e-007, 3.42497e-007, 3.5424e-007, 3.67905e-007, 3.81489e-007, 3.97385e-007, 4.1328e-007, 4.305e-007, 4.50851e-007, 4.71422e-007, 4.95936e-007, 5.20941e-007, 5.48602e-007, 5.82085e-007, 6.16836e-007, 6.5949e-007, 7.04455e-007, 7.56e-007, 8.21086e-007, 8.91972e-007, 9.84e-007, 1.08758e-006, 1.21553e-006, 1.39308e-006, 1.61018e-006, 1.93725e-006};
	    // refractive index - real part
	    double[]	rfInRe = {1.07, 1.1, 1.12, 1.14, 1.15, 1.18, 1.2, 1.22, 1.25, 1.26, 1.28, 1.28, 1.3, 1.31, 1.33, 1.35, 1.38, 1.41, 1.41, 1.39, 1.34, 1.13, 0.81, 0.17, 0.14, 0.1, 0.07, 0.05, 0.05, 0.05, 0.04, 0.04, 0.05, 0.05, 0.05, 0.06, 0.05, 0.06, 0.05, 0.04, 0.03, 0.04, 0.04, 0.04, 0.04, 0.09, 0.13, 0.15, 0.24};
	    // refractive index - imaginary part
	    double[]	rfInIm = {1.212, 1.232, 1.255, 1.277, 1.296, 1.312, 1.325, 1.336, 1.342, 1.344, 1.357, 1.367, 1.378, 1.389, 1.393, 1.387, 1.372, 1.331, 1.264, 1.161, 0.964, 0.616, 0.392, 0.829, 1.142, 1.419, 1.657, 1.864, 2.07, 2.275, 2.462, 2.657, 2.869, 3.093, 3.324, 3.586, 3.858, 4.152, 4.483, 4.838, 5.242, 5.727, 6.312, 6.992, 7.795, 8.828, 10.1, 11.85, 14.08};
	    // second derivatives - real part
        double[]	derivRe = {0, -1.09443e+015, 4.50671e+014, -1.64535e+015, 2.64916e+015, -1.73921e+015, 2.84877e+014, 8.69272e+014, -1.77518e+015, 1.48932e+015, -1.89758e+015, 1.70681e+015, -1.10717e+015, 7.52799e+014, -2.81357e+014, 2.35816e+014, 1.51436e+014, -7.66855e+014, -3.01095e+014, 1.50003e+014, -2.68399e+015, 3.86776e+014, -6.17426e+015, 9.62736e+015, -2.62139e+015, 7.94178e+014, -1.68944e+014, 1.98255e+014, -3.52453e+013, -5.72818e+013, 5.05039e+013, 3.32047e+013, -4.0284e+013, 1.33103e+012, 3.42212e+013, -5.30981e+013, 4.73552e+013, -3.35549e+013, 9.74718e+012, -4.54863e+012, 1.1835e+013, -6.76495e+012, 2.08137e+012, -2.15834e+012, 6.30268e+012, -2.73772e+012, -7.13134e+011, 1.15139e+012, 0};
        // second derivatives - imaginary part
        double[]	derivIm = {0, 5.49257e+014, -4.99584e+014, -1.45243e+013, -2.5674e+014, -3.33755e+014, 5.01553e+013, -3.21087e+014, -3.68608e+014, 8.65942e+014, -4.85862e+014, 2.02151e+014, -6.51859e+013, -1.59369e+014, -3.45625e+014, 3.33754e+013, -7.21153e+014, -1.75627e+014, -4.86315e+014, -1.32704e+015, -1.65707e+015, -2.19007e+014, 1.01945e+016, -4.17064e+015, 5.88854e+014, -8.77761e+014, 4.82107e+013, -2.72544e+014, 1.09375e+014, -1.88393e+014, -8.63708e+013, 1.01638e+014, -1.07779e+014, 2.5244e+013, 2.97971e+013, -8.56009e+013, 4.64112e+013, -4.16531e+013, 1.48883e+013, -5.07977e+011, -1.77458e+013, 2.84057e+013, -2.4881e+013, 9.90519e+012, 5.74546e+012, -1.37589e+013, 1.24799e+013, -9.3404e+012, 0};
        int n = wavl.length;
        double  yRe, yIm;
        Complex	ri = new Complex();

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
		    // cubic spline interpolation - imaginary part
		    yIm=CubicSpline.interpolate(wavelength, wavl, rfInIm, derivIm);
		    ri.reset(yRe, RefractiveIndex.imagPlusMinus*yIm);
        }
	    else{
		    throw new IllegalArgumentException("Wavelength is outside the limits (187.86nm - 1937.2nm) of the tabulated data");
		}
	    return ri;
    }

    // Returns the real refractive index of FUSED QUARTZ for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double quartz(double wavelength){
        // wavelengths
  	    double[]	wavl = {185.0e-9, 214.0e-9, 275.0e-9, 361.0e-9, 509.0e-9, 589.0e-9, 656.0e-9};
  	    // refractive index - real part
	    double[]	rfInRe = {1.57464, 1.53386, 1.49634, 1.47503, 1.4619, 1.4583, 1.4564};
	    // second derivatives - real part
        double[]	derivRe = {0.0, 2.58206e+013, 1.62375e+012, 1.75944e+012, -5.81947e+010, 3.55464e+011, 0.0};
        // Cauchy coefficients
        double	a1=0.444046, b1=9.677366e-15;
        int n = wavl.length;

        double  yRe;

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
        }
	    else{
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefractiveIndex.quartz() is outside");
		    System.out.println("the experimental data limits (185.0 nm - 656.0 nm).   Extrapolation used");
		    System.out.println("the Caunchy equation which may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
		    yRe=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		}
	    return yRe;
    }

    // Returns the real refractive index of LAF78847 CROWN GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double crownGlass(double wavelength){
        // wavelengths
  	    double[]	wavl = {365.02e-9, 404.66e-9, 435.84e-9, 479.99e-9, 486.13e-9, 546.07e-9, 587.56e-9, 643.85e-9, 656.28e-9, 706.52e-9, 852.11e-9, 1014e-9};
  	    // refractive index - real part
	    double[]	rfInRe = {1.83028, 1.8169, 1.80916, 1.8009, 1.79994, 1.79227, 1.78831, 1.7841, 1.7833, 1.78048, 1.7746, 1.77018};
	    // second derivatives - real part
        double[]	derivRe = {0, 3.48108e+012, 1.37108e+012, 1.17265e+012, 9.68655e+011, 5.86009e+011, 4.3771e+011, 2.48861e+011, 3.01116e+011, 1.7006e+011, 8.74046e+010, 0};
        // Cauchy coefficients
        double	a1=0.762002, b1=1.18516e-14;
        int n = wavl.length;

        double  yRe;

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
        }
	    else{
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefractiveIndex.crownGlass() is outside");
		    System.out.println("the experimental data limits (365.02 nm - 1014.0 nm).   Extrapolation used");
		    System.out.println("the Caunchy equation which may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
		    yRe=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		}
	    return yRe;
    }

    // Returns the real refractive index of PILKINGTON PERMABLOC FLOAT GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double floatGlass(double wavelength){
        // wavelengths
  	    double[]	wavl = {543.5e-9, 594.1e-9, 604e-9, 611.9e-9, 632.8e-9};
  	    // refractive index - real part
	    double[]	rfInRe = {1.51958, 1.51707, 1.51671, 1.5163, 1.51553};
	    // second derivatives - real part
        double[]	derivRe = {0, 9.28695e+011, -3.3258e+012, 2.02454e+012, 0};
        // Cauchy coefficients
        double	a1=0.504167, b1=9.03525e-15;
        int n = wavl.length;

        double  yRe;

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
        }
	    else{
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefractiveIndex.floatGlass() is outside");
		    System.out.println("the experimental data limits (543.5 nm - 632.8 nm).   Extrapolation used");
		    System.out.println("the Caunchy equation which may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
		    yRe=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		}
	    return yRe;
    }

    // Returns the real refractive index of CHANCE POPPER MICROSCOPE SLIDE GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double microscopeSlideGlass(double wavelength){
        // wavelengths
  	    double[]	wavl = {543.5e-9, 594.1e-9, 604e-9, 611.9e-9, 632.8e-9};
  	    // refractive index - real part
	    double[]	rfInRe = {1.51436, 1.51184, 1.51144, 1.51111, 1.51027};
	    // second derivatives - real part
        double[]	derivRe = {0, 5.00315e+011, -4.19006e+011, 2.22131e+011, 0};
        // Cauchy coefficients
        double	a1=0.498854, b1=9.18748e-15;
        int n = wavl.length;

        double  yRe;

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
        }
	    else{
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefractiveIndex.microSlideGlass() is outside");
		    System.out.println("the experimental data limits (543.5 nm - 632.8 nm).   Extrapolation used");
		    System.out.println("the Caunchy equation which may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
		    yRe=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		}
	    return yRe;
    }

    // Returns the real refractive index of POLYMETHACRYLATE for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double polymethacrylate(double wavelength){
        // wavelengths
  	    double[]	wavl = {435.8e-9, 546.1e-9, 589.3e-9};
  	    // refractive index - real part
	    double[]	rfInRe = {1.502, 1.494, 1.492};
	    // second derivatives - real part
        double[]	derivRe = {0, 5.127e+011, 0};
        // Cauchy coefficients
        double	a1=0.498854, b1=9.18748e-15;
        int n = wavl.length;

        double  yRe;

	    if(wavelength>=wavl[0] && wavelength<=wavl[n-1]){
		    // cubic spline interpolation - real part
		    yRe=CubicSpline.interpolate(wavelength, wavl, rfInRe, derivRe);
        }
	    else{
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefrIndex.polymethacrylate() is outside");
		    System.out.println("the experimental data limits (435.8 nm - 589.3 nm).   Extrapolation used");
		    System.out.println("the Caunchy equation which may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
		    yRe=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		}
	    return yRe;
    }

    // Returns the real refractive index of AIR for a given wavelength (in metres)
    // Interpolation - uses the Cauchy equation (see Born & Wolf section 2.3)
    // Extrapolation - uses the Cauchy equation - may be invalid and will be invalid within an absorption band
    public static double air(double wavelength){
        // Cauchy coefficients
	    double  ri, a1=28.79e-5, b1=5.67e-11;

	    wavelength=wavelength*1e2;
	    if(wavelength<2.498e-5 || wavelength>7.594e-5){
		    System.out.println("Wavelength passed ("+wavelength*1e7+"nm) to RefractiveIndex.air() is outside");
		    System.out.println("the experimental data limits (249.8 nm - 759.4 nm).   Extrapolation using");
		    System.out.println("the Caunchy equation may not be valid at the wavelength requested,");
		    System.out.println(" especially if the wavelength is within an absorption band");
	    }
		ri=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		return ri;
    }

    // Returns the real refractive index of WATER
    // for a given wavelength (in metres)and a given temperature (Celsius)
    // Interpolation - natural bicubic spline
    public static double water(double wavelength, double temperature){
        // wavelengths
  	    double[]	wavl = {404.6e-9, 589.32e-9, 706.52e-9};
        // temperatures
  	    double[]	temp = {0,10,20,30,40,50,60,70,80,90,100};
  	    // refractive indices for the three wavelengths - real part
	    double[]	rfInRe1 = {1.34359, 1.34351, 1.34287, 1.3418, 1.34039, 1.33867, 1.33669, 1.33447, 1.33204, 1.32942, 1.32663};
	    double[]	rfInRe2 = {1.33346, 1.33341, 1.33283, 1.33184, 1.33052, 1.32892, 1.32707, 1.325, 1.32274, 1.32029, 1.31766};
	    double[]	rfInRe3 = {1.33086, 1.33073, 1.33007, 1.32903, 1.32766, 1.32603, 1.32417, 1.32209, 1.31983, 1.31739, 1.31481};
	    // second derivatives - real part
        double[]	derivRe1 = {0, -7.46454e-006, -3.74183e-006, -3.36815e-006, -3.18559e-006, -2.4895e-006, -2.4564e-006, -2.08489e-006, -1.80403e-006, -2.09899e-006, 0};
        double[]	derivRe2 = {0, -7.06563e-006, -3.53749e-006, -3.3844e-006, -2.72489e-006, -2.51602e-006, -2.21102e-006, -1.83991e-006, -1.82936e-006, -2.24266e-006, 0};
        double[]	derivRe3 = {0, -7.19933e-006, -3.00268e-006, -3.58995e-006, -2.43753e-006, -2.25994e-006, -2.32269e-006, -1.64928e-006, -1.88019e-006, -1.62995e-006, 0};
        int i;
        int n = wavl.length;
        int m = temp.length;
        double[] yt = new double[n];
        double  yRe;

	    if(wavelength<wavl[0] || wavelength>wavl[n-1]){
	        throw new IllegalArgumentException("Wavelength " + wavelength + " is out of experimental data bounds: " + wavl[0] + " to " + wavl[n-1]);
		}
	    if(temperature<temp[0] || temperature>temp[m-1]){
	        throw new IllegalArgumentException("Temperature " + temperature + " is out of experimental data bounds; "+ temp[0] + " to " + temp[m-1]);
		}

		// cubic spline interpolation with respect to temperature
		yt[0]=CubicSpline.interpolate(temperature, temp, rfInRe1, derivRe1);
		yt[1]=CubicSpline.interpolate(temperature, temp, rfInRe2, derivRe2);
		yt[2]=CubicSpline.interpolate(temperature, temp, rfInRe3, derivRe3);

		// cubic spline interpolation with respect to wavelength
		CubicSpline cs = new CubicSpline(wavl, yt);
		cs.calcDeriv();
		yRe = cs.interpolate(wavelength);

	    return yRe;
    }

    // Returns the refractive index of pva solutions as a function of g/l pva concentration
    // Data		Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    //			pva refractive index increment fitted to modified Cauchy equation:
    //				dn = 1 + a1*(1 + b1/(lambda*lambda))
    // concn	g/l concentration of pva
    // temp     temperature (degree Celsius) (t = 30C for original pva increment calculation)
    // wavl     wavelength in metres
    public static double pva(double concn, double wavl, double temp){
	    double  refind, rfwater, rfincr;
	    double	a1=-0.998419, b1=-1.87778e-17;

	    rfwater=water(wavl, temp);
	    rfincr = 1.0 + a1*(1.0 + b1/Math.pow(wavl, 2));
	    refind = rfwater + rfincr*concn/10.0;

	    return refind;
    }

    // Returns the refractive index of a NaCl solution for a given wavelength (in metres),
    // a given temperature (degrees Celcius) and a given NaCl concentration (M)
    // Interpolation - bicubic spline
    public static double saline(double concentration, double wavelength, double temperature){
	    double[] naclConcRi={0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10.1, 11.1, 12.1, 13.1, 14.1, 15.1, 16.2,
			17.2, 18.2, 19.2, 20.2, 21.3, 22.3, 23.3, 24.4, 25.4, 26.4, 27.5, 28.5, 29.5, 30.6, 31.6,
			32.7, 33.7, 34.8, 35.8, 36.9, 37.9, 39, 40, 41.1, 42.1, 43.2, 44.2, 45.3, 46.4, 47.4,
			48.5, 49.6, 50.6, 51.7, 53.8, 56, 58.1, 60.3, 62.5, 64.6, 66.8, 69, 71.2, 73.4,
			75.6, 77.8, 80, 82.2, 84.5, 86.7, 88.9, 91.2, 93.4, 95.7, 98, 100.2, 102.5, 104.8,
			107.1, 112.8, 118.6, 124.4, 130.3, 136.2, 142.1, 148.1, 154.1, 160.2, 166.3,
			178.6, 191.1, 203.7, 216.6, 229.6, 247.2, 256.1, 269.6, 283.3, 297.2, 311.3};
        double[] rfIn={1.333, 1.3332, 1.3333, 1.3335, 1.3337, 1.3339, 1.334, 1.3342, 1.3344, 1.3346,
			1.3347, 1.3349, 1.3351, 1.3353, 1.3354, 1.3356, 1.3358, 1.336, 1.3362, 1.3363,
			1.3365, 1.3367, 1.3369, 1.337, 1.3372, 1.3374, 1.3376, 1.3377, 1.3379, 1.3381,
			1.3383, 1.3384, 1.3386, 1.3388, 1.339, 1.3391, 1.3393, 1.3395, 1.3397, 1.3398,
			1.34, 1.3402, 1.3404, 1.3405, 1.3407, 1.3409, 1.3411, 1.3412, 1.3414, 1.3416,
			1.3418, 1.3421, 1.3425, 1.3428, 1.3432, 1.3435, 1.3439, 1.3442, 1.3446, 1.3449,
			1.3453, 1.3456, 1.346, 1.3463, 1.3467, 1.347, 1.3474, 1.3477, 1.3481, 1.3484,
			1.3488, 1.3491, 1.3495, 1.3498, 1.3502, 1.3505, 1.3514, 1.3523, 1.3532, 1.3541,
			1.3549, 1.3558, 1.3567, 1.3576, 1.3585, 1.3594, 1.3612, 1.363, 1.3648, 1.3666,
			1.3684, 1.3702, 1.3721, 1.3739, 1.3757, 1.3776, 1.3795};
	    double[] deriv = {0, -0.000204904, 0.000219616, -7.35613e-005, 7.4629e-005, -0.000224955, 0.00022519, -7.58054e-005, 7.80315e-005, -0.000236321, 0.000236336, -7.81129e-005, 7.61157e-005, -0.00022635, 0.000229284, -9.07847e-005, 3.90193e-005, 4.50731e-005, -0.000219312, 0.000232174, -0.000109384, 0.000107407, -0.000221696, 0.000179377, -3.70715e-005, 6.74765e-005, -0.000232834, 0.000232621, -6.63432e-005, 3.27522e-005, -0.000163915, 0.000161508, -2.13737e-005, 2.12013e-005, -0.000160693, 0.000160681, -2.11441e-005, 2.11468e-005, -0.000160694, 0.000160744, -2.1382e-005, 2.20772e-005, -0.00016403, 0.000173733, -6.79449e-005, 9.80467e-005, -0.000227966, 0.00018624, -2.11277e-005, -7.03714e-006, -5.30975e-005, 5.41898e-005, -5.48913e-005, 5.67056e-005, -6.30137e-005, 7.13824e-005, -7.17063e-005, 6.45891e-005, -6.26832e-005, 6.21769e-005, -6.20574e-005, 6.20859e-005, -6.23194e-005, 6.32246e-005, -6.66119e-005, 6.61361e-005, -6.07803e-005, 5.30183e-005, -5.13701e-005, 5.23148e-005, -5.76185e-005, 6.47375e-005, -6.44613e-005, 5.62732e-005, -4.721e-005, 1.91454e-005, -5.78664e-006, 1.31552e-006, 5.24579e-007, -6.04837e-006, 6.43247e-006, -2.44509e-006, 8.31233e-007, -8.79838e-007, 2.54193e-007, -1.36934e-007, -3.01506e-007, 2.07215e-007, -1.07068e-006, 2.48527e-006, -9.3358e-006, 1.82903e-005, -1.54847e-005, 3.70622e-006, -3.10434e-007, -1.30682e-007, 0};
        double[] wavlRockSalt = {185.0e-9, 589.0e-9, 884.0e-9, 1179.0e-9, 2357.0e-9, 3536.0e-9, 5893.0e-9, 8840.0e-9};
	    double[] rfInRockSalt = {1.893, 1.544, 1.534, 1.530, 1.526, 1.523, 1.516, 1.502};
	    double[] derivRockSalt = {0, 3.74404e+012, -8.62356e+011, 1.19054e+011, -3.00122e+010, 5.3764e+009, -2.20178e+009, 0};
	    double  refrIndReal, refrIndNacl20,  refInWater, reInRockSalt, reInRockSalt5893;
	    double	riWater20_589, moleFractNacl, moleFractWater;
	    double	densityNacl, densityWater, densityWater20, refrIncr;
	    double	n2, lor1, prelor1, a1, b1;
	    double	refrInd, molesWater, molesNacl, lorDens, concen;
	    int		nRi=97, nRockSalt=8;

	    concen=concentration*Saline.MOLWEIGHT;

	    //calculate the refractive index of pure water at the chosen temperature and wavelength
	    refInWater=Water.refractIndex(wavelength, temperature);

	    if(concentration==0.0){
	        refrInd = refInWater;
	    }
	    else{
            // calculate refractive increment

	        // check limits
		    if(wavelength<404.6e-9 || wavelength>706.52e-9){
		        throw new IllegalArgumentException("Wavelength outside the experimental data limits (404.6nm - 706.52nm)");
		    }
		    else
		    {
			    if(temperature<0.0 || temperature>100.0){
			        throw new IllegalArgumentException("Temperature " + temperature + " is outside the experimental data limits (0 C - 100 C)");
			    }
			    else
			    {
				    if(concen<naclConcRi[0] || concen>naclConcRi[nRi-1]){
				        throw new IllegalArgumentException("Concentration" + concen + " is outside the experimental data limits");
				    }
				    else{
				    //calculate refractive index of salt solution at 20C and 589.3
				        refrIndNacl20=CubicSpline.interpolate(concen, naclConcRi, rfIn, deriv);
				    }
			    }
		    }

		    //calculate density of the salt solution at 20C
		    densityNacl = Saline.density(concentration);

		    //density water at 20C
		    densityWater20=Water.density(20.0);

		    //calculate density of water at chosen temperature
		    densityWater = Water.density(temperature);

		    //calculate refractive index of water at 20C at wavelength 589.3nm
		    riWater20_589=Water.refractIndex(589.3e-9, 20.0);

		    //calculate mole fractions
		    molesWater = (densityNacl*1000 - concen)/Water.MOLWEIGHT;
		    molesNacl=concentration;
		    moleFractWater=molesWater/(molesWater + molesNacl);
		    moleFractNacl=molesNacl/(molesWater + molesNacl);

		    //refractive increment
		    prelor1=(Water.MOLWEIGHT*moleFractWater + Saline.MOLWEIGHT*moleFractNacl);
		    n2=refrIndNacl20*refrIndNacl20;
		    refrIncr=((n2-1.0)/(n2+2.0))*prelor1/densityNacl;

		    n2=riWater20_589*riWater20_589;
		    refrIncr-=(((n2-1.0)/(n2+2.0))*(Water.MOLWEIGHT*moleFractWater)/densityWater20);

		    //refractive index of rock salt at 589.3nm
		    reInRockSalt5893=1.516;

		    //calculate refractive index of rock salt at correct wavelength
		    if(wavelength>=wavlRockSalt[0] && wavelength<=wavlRockSalt[nRockSalt-1]){
		        reInRockSalt=CubicSpline.interpolate(wavelength, wavlRockSalt, rfInRockSalt, derivRockSalt);
		    }
		    else{
			    a1=0.515533;
			    b1=2.50204e-14;
			    reInRockSalt=1.0+a1*(1.0+b1/Math.pow(wavelength,2));
		    }

		    //scale refractive increment for wavelength
		    reInRockSalt *= reInRockSalt;
		    reInRockSalt5893 *= reInRockSalt5893;
		    refrIncr = refrIncr*((reInRockSalt-1.0)/(reInRockSalt+2.0))/((reInRockSalt5893-1.0)/(reInRockSalt5893+2.0));

		    //add refractive increment to Lorenz-Lorentz term for water for correct temperature and wavelength
		    n2=refInWater*refInWater;
		    lor1=(n2-1.0)/(n2+2.0)*(Water.MOLWEIGHT*moleFractWater)/densityWater;
		    lor1=lor1+refrIncr;
		    lorDens=(Water.MOLWEIGHT*moleFractWater*densityWater20/densityWater + Saline.MOLWEIGHT*moleFractNacl)*densityNacl/prelor1;
 		    lor1=(lor1/prelor1)*lorDens;
 		    lor1=(2.0*lor1 + 1.0)/(1.0 - lor1);
		    refrInd=Math.sqrt(lor1);
	    }
	    return refrInd;
    }

    // Returns the refractive index of sucrose solutions as a function of g/l sucrose concentration
    // Wavelength - sodium D line 589.3 nm
    // Interpolation - natural cubic spline
    // Extrapolation above 1208.2g/l Lorenz-lorenz  equation based on
    //  average refraction of sucrose calculated from experimental data
    // Data -  Rubber Handbook
    public static double sucrose(double concentration, double temperature){
	    double[]	concnG = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
	    double[]	refInd = {1.333, 1.3337, 1.3344, 1.3351, 1.3359, 1.3366, 1.3373, 1.3381, 1.3388, 1.3395, 1.3403, 1.341, 1.3418, 1.3425, 1.3433, 1.344, 1.3448, 1.3455, 1.3463, 1.3471, 1.3478, 1.3494, 1.3509, 1.3525, 1.3541, 1.3557, 1.3573, 1.3589, 1.3606, 1.3622, 1.3639, 1.3672, 1.3706, 1.3741, 1.3776, 1.3812, 1.3848, 1.3885, 1.3922, 1.396, 1.3999, 1.4038, 1.4078, 1.4118, 1.4159, 1.4201, 1.4243, 1.4286, 1.433, 1.4374, 1.4419, 1.4465, 1.4511, 1.4558, 1.4606, 1.4654, 1.4703, 1.4753, 1.4803, 1.4854, 1.4906, 1.4958, 1.501};
	    double[]    deriv = {0, 8.87219e-007, -3.54887e-006, 9.95698e-006, -9.31221e-006, 3.62993e-007, 7.86024e-006, -8.73591e-006, 1.22853e-006, 7.05021e-006, -9.99082e-006, 1.07237e-005, -1.07147e-005, 9.94585e-006, -1.0411e-005, 1.03381e-005, -9.58168e-006, 6.62868e-006, 9.93569e-007, -7.60108e-006, 5.46568e-006, -3.1357e-006, 2.02704e-006, -6.87809e-007, 2.17409e-008, -9.4373e-008, -3.16995e-007, 1.36235e-006, -1.83846e-006, 1.45463e-006, -7.98314e-007, 2.76693e-007, 1.46498e-007, -2.71393e-007, 2.2853e-007, -2.28325e-007, 1.58047e-007, -1.40698e-007, 3.72197e-008, 1.21286e-007, -1.69002e-007, 1.09589e-007, -1.50555e-007, 8.24317e-008, 3.46254e-008, -1.10233e-007, 3.66531e-008, 6.86736e-008, -1.23453e-007, 9.23933e-009, 1.0371e-007, -1.74702e-007, 7.44894e-008, 3.92428e-008, -1.41903e-007, 6.38698e-008, 3.62013e-008, -1.24325e-007, 4.47083e-008, 2.66887e-008, -7.19672e-008, -5.86665e-008, 0};
	    double      refind, refind2, refind3, sucvol, refracttot, refractwat, refractsuc=0.331335;
	    double[]	weight={5,	10,	15,	20,	30,	40,	50,	60,	70,	75};
		double[][]	cf={{-0.25,-0.27,-0.31,-0.31,-0.34,-0.35,-0.36,-0.37,-0.36,-0.36},{-0.21,-0.23,-0.26,-0.27,-0.29,-0.31,-0.31,-0.32,-0.31,-0.29},{-0.16,-0.18,-0.2,-0.2,-0.22,-0.23,-0.23,-0.23,-0.2,-0.17},{-0.11,-0.12,-0.14,-0.14,-0.15,-0.16,-0.16,-0.15,-0.12,-0.09},{-0.06,-0.07,-0.08,-0.08,-0.08,-0.09,-0.09,-0.08,-0.07,-0.05},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.06,0.07,0.07,0.07,0.07,0.07,0.07,0.07,0.07,0.07},{0.12,0.14,0.14,0.14,0.14,0.14,0.15,0.14,0.14,0.14},{0.18,0.2,0.2,0.21,0.21,0.21,0.23,0.21,0.22,0.22},{0.24,0.26,0.26,0.27,0.28,0.28,0.3,0.28,0.29,0.29},{0.3,0.32,0.32,0.34,0.36,0.36,0.38,0.36,0.36,0.37},{0.36,0.39,0.39,0.41,0.43,0.43,0.46,0.44,0.43,0.44},{0.43,0.46,0.46,0.48,0.5,0.51,0.55,0.52,0.5,0.51},{0.5,0.53,0.53,0.55,0.58,0.59,0.63,0.6,0.57,0.59},{0.57,0.6,0.61,0.62,0.66,0.67,0.71,0.68,0.65,0.67},{0.64,0.67,0.7,0.71,0.74,0.75,0.8,0.76,0.73,0.75}};
	    double[][]	corrfac = new double[16][10];
	    double[]	tempw = {15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
        double[]	corrfac5 = {-0.25,-0.21,-0.16,-0.11,-0.06,0.0,0.06,0.12,0.18,0.24,0.3,0.36,0.43,0.5,0.57,0.64};
	    double[]    derivcor = {0, 0.0157677, -0.00307078, -0.00348457, 0.017009, -0.00455161, 0.00119739, -0.00023797, -0.000245514, 0.00122003, -0.0046346, 0.0173184, -0.00463885, 0.00123703, -0.000309256, 0};
	    double      concg, concw, corrfactor;
	    double      wavelength = 5.893e-7;
	    int		    i,j, m, n=63;

	    for(i=0; i<16; i++){
		    for(j=0; j<10; j++)corrfac[i][j]=cf[i][j];
	    }

	    if(concentration>=concnG[0] && concentration<=concnG[n-1]){
    		refind=CubicSpline.interpolate(concentration, concnG, refInd, deriv);
	    }
	    else{
		    refractwat=(refInd[0]*refInd[0]-1.0)/(refInd[0]*refInd[0]+2.0);
		    sucvol=concentration*Sucrose.specificVolume(concentration)*1e3;
		    refracttot=(refractsuc*sucvol+refractwat*(1e3-sucvol))/1e3;
		    refind=(1.0+2.0*refracttot)/(1.0-refracttot);
	    }

	    if(temperature!=20.0){
		    concw=Sucrose.gperlToWeightpercent(concentration, temperature);
		    if(concw<5.0){
		        refind2 = Water.refractIndex(wavelength, temperature);
				if(concentration==0.0){
				    refind=refind2;
			     }
			     else{
				    corrfactor=CubicSpline.interpolate(temperature, tempw, corrfac5, derivcor);
				    concw=concw+corrfactor;
				    concg=Sucrose.weightpercentToGperl(concw, temperature);
    		        refind3=CubicSpline.interpolate(concg, concnG, refInd, deriv);
				    refind=refind2+(refind3-refind2)*concw/(5.0);
			    }
		    }
		    else{
			    BiCubicSpline bcs = new BiCubicSpline(tempw, weight, corrfac);
			    corrfactor = bcs.interpolate(temperature, concw);
			    concw=concw+corrfactor;
			    concg=Sucrose.weightpercentToGperl(concw, temperature);
			    refind=CubicSpline.interpolate(concg, concnG, refInd, deriv);
		    }
	    }
	    return refind;
    }

    // Returns the refractive index of a mixture of material A and material B,
    //  using the Lorenz-Lorentz equation, given the refractive index of A (na), of B (nb),
    //  the molecular wight of A (molwta), of B (molwtb), the mole fraction of A (molfracta),
    //  and the density of A (densa), of B (densb) and of the mixture (densab).
    public static double lorenzLorentz(double na, double nb, double molwta, double molwtb, double molfracta, double densa, double densb, double densab){
        double lla, llb, llab, molmassa, molmassb, molmassab, nab;

        molmassa = molfracta*molwta;
        molmassb = (1.0 - molfracta)*molwtb;
        lla = na*na;
        lla = ((lla - 1.0)/(lla + 2.0))*molmassa/densa;

        llb = nb*nb;
        llb = ((llb - 1.0)/(llb + 2.0))*molmassb/densb;

        llab = lla + llb;
        nab = llab*densab/(molmassa+molmassb);
        nab = (2.0*nab + 1.0)/(1.0 - nab);

        return Math.sqrt(nab);
    }


    // Returns the refractive index of a mixture of n materials,
    //  using the Lorenz-Lorentz equation, given an array of the refractive indices (ni),
    //  an array of the molecular wights (molwt), an array the mole fractions (molfract),
    //  and an array of the densities (dens) and the density of the mixture (densmix).
    public static double lorenzLorentz(double[] ni, double[] molwt, double[] molfract, double[] dens, double densmix){
        double ll, molmass, nimix, sum0=0, sum1=0.0;
        int i, n=ni.length;

        if(n != molwt.length || n != molfract.length || n != dens.length){
            throw new IllegalArgumentException("Array lengths differ");
        }
        for(i=0; i<n; i++)sum0+=molfract[i];
        if(Math.abs(1.0-sum0)>1e-5){
            throw new IllegalArgumentException("Mole fractions do not sum to unity");
        }

        sum0=0.0;
        for(i=0; i<n; i++){
            molmass = molfract[i]*molwt[i];
            ll = ni[i]*ni[i];
            ll = ((ll - 1.0)/(ll + 2.0))*molmass/dens[i];
            sum0 += ll;
            sum1 += molmass;
        }
        nimix = sum0*densmix/sum1;
        nimix = (2.0*nimix + 1.0)/(1.0 - nimix);

        return Math.sqrt(nimix);
    }

}

