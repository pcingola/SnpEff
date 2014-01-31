/*
*   Pva Class
*
*   Methods for returning the physical properties of
*   aqueous polyvinylalcohol (pva) solutions:
*   viscosity, density, refractive index,
*   diffusion coefficient and specific volume.
*
*   Reference:  Pritchard, J. G., Poly(vinyl alcohol) :  basic properties and
*               uses, Macdonald & Co, London, 1970.
*               UCL DMS Watson Library location: CHEMISTRY D 297 PRI.
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: July 2003
*   Updated: 1 July 2003 and 2 May 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Pva.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) May 2004    Michael Thomas Flanagan
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

import flanagan.math.Fmath;

public class Pva{

    // METHODS
    // Returns the viscosity (Pa s) of aqueous pva solutions as a function of the
    // g/l pva concentration (concn), the temperature [Celsius](temp) and the
    // molecualr weight of the pva (molwt).
    // Empirical equation from Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    public static double viscosity(double concn, double molwt, double temp){

	    double 	intVisc30, intVisc20, intViscT, spViscT, waterViscT, viscosity, concndlpg;

	    intVisc30 = 4.53e-4*Math.pow(molwt, 0.64);
	    intVisc20 = intVisc30*1.07;
	    intViscT  = intVisc20*Math.pow(1.07, -(temp-20)/10);
	    concndlpg = concn/10.0;
	    spViscT   = concndlpg*(intViscT + 0.201*concndlpg*Math.pow(intViscT, 2.28));
	    waterViscT = Water.viscosity(temp);
	    viscosity  = (spViscT + 1.0)*waterViscT;

	    return viscosity;
    }

    // Returns the density (kg/m^3) of aqueous pva solutions, at 30 C, as a function of
    // g/l pva concentration (concn) and the molecular weight
    // Empirical equation from Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    public static double density(double concn, double molwt){

	    double  density;

	    concn=concn/10.0;
	    density = 1000*(0.99565 +(0.00248 - 1.09/molwt)*concn + (0.000064 - 0.39/molwt)*concn*concn);

	    return density;
    }

    // Returns the specific volume (kg/m^3) of pva in aqueous solution
    // Data     Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    public static double specificVolume(){
	    return 0.000765;
    }

    // returns the diffusion coefficient (m^2 s^-1) of pva in aqueous solution
    // as a function of the g/l pva concentration (concn), pva molecular weight (molwt)
    // and temperature (temp) [Celsius].
    public static double diffCoeff(double concn, double molwt, double temp){

	    double diffcoef, f, viscosity, specvol, vol, radius, tempa;

	    tempa = temp-Fmath.T_ABS;

	    viscosity = Pva.viscosity(concn, molwt, temp);
	    specvol = Pva.specificVolume();
	    vol = molwt*specvol/(Fmath.N_AVAGADRO*1000);
	    radius=Math.pow(3.0*vol/(4.0*Math.PI),1.0/3.0);

	    f=6.0*Math.PI*viscosity*radius;
        diffcoef=Fmath.K_BOLTZMANN*tempa/f;

	    return diffcoef;
    }

    // Returns the refractive index of pva solutions as a function of g/l pva concentration
    // Data		Poly(vinyl alcohol): basic properties and uses by J G Pritchard (1970)
    //			pva refractive index increment fitted to modified Cauchy equation:
    //				dn = 1 + a1*(1 + b1/(lambda*lambda))
    // concn	g/l concentration of pva
    // temp     temperature (degree Celsius) (t = 30C for original pva increment calculation)
    // wavl     wavelength in metres
    public static double refractIndex(double concn, double wavl, double temp)
    {
	    double  refind, rfwater, rfincr;
	    double	a1=-0.998419, b1=-1.87778e-17;

	    rfwater = Water.refractIndex(wavl, temp);
	    rfincr = 1.0 + a1*(1.0 + b1/Math.pow(wavl, 2));
	    refind = rfwater + rfincr*concn/10.0;

	    return refind;
    }

}