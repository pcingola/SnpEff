/*
*   RefrIndex Class  (deprecated)
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
*   Created: July 2003
*   Updated: 1 July 2003, 1 May 2004,
*   REPLACED:28 February 2006 - Replaced by RefractiveIndex in the optics section of this library
*                               The methods in RefrIndex now call the corresponding methods in RefractiveIndex
*                               RefrIndex has been retained to ensure backward compatibility
*
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RefractiveIndex.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RefrIndex.html (deprecated)
*
*   Copyright (c) July 2003 - May 2004   Michael Thomas Flanagan
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

package flanagan.physprop;

import flanagan.optics.RefractiveIndex;
import flanagan.complex.Complex;

public class RefrIndex{

    //  METHODS
	// Returns a complex refractive index given the real part of the refractive index (riReal),
	// the extinction coefficient (extCoeff)and the concentration of the absorbing species (concn)
	// and the wavelength (wavl).  For a pure material with a given absorption coefficient set concn
	// to unity and extCoeff to the value of the absorption coefficient.
	// The units of the concentration, the absorption coefficient and the wavelength should match.
	public static Complex absToComplex(double riReal, double extCoeff, double concn, double wavl){
	    return RefractiveIndex.absToComplex(riReal, extCoeff, concn, wavl);
	}

	// Returns the absorption coefficient ( units - reciprocal metres) that corresponds
	// to the imaginary part of a complex refractive index (riImag) at a wavelength wavl (metres)
	public static double imagToAbs(double riImag, double wavl){
	    return RefractiveIndex.imagToAbs(riImag, wavl);
	}

    // Returns the complex refractive index of GOLD for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Data - P.B. Johnson and R.W. Christy, Phys. Rev. B. 6(12) 4370-4379, 1972
    public static Complex gold(double wavelength){
	    return RefractiveIndex.gold(wavelength);
    }

    // Returns the complex refractive index of SILVER for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Data - P.B. Johnson and R.W. Christy, Phys. Rev. B. 6(12) 4370-4379, 1972
    public static Complex silver(double wavelength){
	    return RefractiveIndex.silver(wavelength);
    }

    // Returns the real refractive index of FUSED QUARTZ for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double quartz(double wavelength){
	    return RefractiveIndex.quartz(wavelength);
    }

    // Returns the real refractive index of LAF78847 CROWN GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double crownGlass(double wavelength){
	    return RefractiveIndex.crownGlass(wavelength);
    }

    // Returns the real refractive index of PILKINGTON PERMABLOC FLOAT GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double floatGlass(double wavelength){

	    return RefractiveIndex.floatGlass(wavelength);
    }

    // Returns the real refractive index of CHANCE POPPER MICROSCOPE SLIDE GLASS for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double microSlideGlass(double wavelength){
	    return RefractiveIndex.microscopeSlideGlass(wavelength);
    }

    // Returns the real refractive index of POLYMETHACRYLATE for a given wavelength (in metres)
    // Interpolation - natural cubic spline
    // Extrapolation - Cauchy equation
    public static double polymethacrylate(double wavelength){
	    return RefractiveIndex.polymethacrylate(wavelength);
    }

    // Returns the real refractive index of AIR for a given wavelength (in metres)
    // Interpolation - uses the Cauchy equation (see Born & Wolf section 2.3)
    // Extrapolation - uses the Cauchy equation - may be invalid and will be invalid within an absorption band
    public static double air(double wavelength){
		return RefractiveIndex.air(wavelength);
    }

    // Returns the real refractive index of WATER
    // for a given wavelength (in metres)and a given temperature (Celsius)
    // Interpolation - natural bicubic spline
    public static double water(double wavelength, double temperature){
	    return RefractiveIndex.water(wavelength, temperature);
    }

    // Returns the refractive index of pva solutions as a function of g/l pva concentration
    // Data		Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    //			pva refractive index increment fitted to modified Cauchy equation:
    //				dn = 1 + a1*(1 + b1/(lambda*lambda))
    // concn	g/l concentration of pva
    // temp     temperature (degree Celsius) (t = 30C for original pva increment calculation)
    // wavl     wavelength in metres
    public static double pva(double concn, double wavl, double temp){
	    return RefractiveIndex.pva(concn, wavl, temp);
    }

    // Returns the refractive index of a NaCl solution for a given wavelength (in metres),
    // a given temperature (degrees Celcius) and a given NaCl concentration (M)
    // Interpolation - bicubic spline
    public static double saline(double concentration, double wavelength, double temperature){
	    return RefractiveIndex.saline(concentration, wavelength, temperature);
    }

    // Returns the refractive index of sucrose solutions as a function of g/l sucrose concentration
    // Wavelength - sodium D line 589.3 nm
    // Interpolation - natural cubic spline
    // Extrapolation above 1208.2g/l Lorenz-lorenz  equation based on
    //  average refraction of sucrose calculated from experimental data
    // Data -  Rubber Handbook
    public static double sucrose(double concentration, double temperature){
	    return RefractiveIndex.water(concentration, temperature);
    }

    // Returns the refractive index of a mixture of material A and material B,
    //  using the Lorenz-Lorentz equation, given the refractive index of A (na), of B (nb),
    //  the molecular wight of A (molwta), of B (molwtb), the mole fraction of A (molfracta),
    //  and the density of A (densa), of B (densb) and of the mixture (densab).
    public static double lorenzLorentz(double na, double nb, double molwta, double molwtb, double molfracta, double densa, double densb, double densab){
        return RefractiveIndex.lorenzLorentz(na, nb, molwta, molwtb, molfracta, densa, densb, densab);
    }


    // Returns the refractive index of a mixture of n materials,
    //  using the Lorenz-Lorentz equation, given an array of the refractive indices (ni),
    //  an array of the molecular wights (molwt), an array the mole fractions (molfract),
    //  and an array of the densities (dens) and the density of the mixture (densmix).
    public static double lorenzLorentz(double[] ni, double[] molwt, double[] molfract, double[] dens, double densmix){
         return RefractiveIndex.lorenzLorentz(ni, molwt, molfract, dens, densmix);
    }

}

