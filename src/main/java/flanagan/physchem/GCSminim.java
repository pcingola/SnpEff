/*
*   Classes GCSminim    (for Class GouyChapmanStern)
*
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:       10 December 2004
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/GouyChapmanStern.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) December 2004
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

package flanagan.physchem;

import flanagan.math.*;

class GCSminim implements MinimisationFunction{

    public double psiDelta = 0.0D;              // current value of psi(delta) [diffuseLayerPotential]
    public double tempK = 25.0D - Fmath.T_ABS;  // temperature [degrees Kelvin]
    public double surfaceSiteDensity = 0.0D;    // surface adsorption site density
    public double surfaceArea= 0.0D;            // surface area
    public double volume = 0.0D;                // electrolyte volume
    public int nonZeroAssocK = 0;               // number of non-zero association constants
    public double[] assocK = null;              // surface site association constants
    public double[] initConcn = null;           // initial ion concentrations
    public double[] charges = null;             // ion charges
    public int[] indexK = null;                 // indices of the non-zero associaton constants

    // Constructor
    public GCSminim(){
    }

    // method that calculates the sum of the squares of g(siteConcn[i])
    // Equations 16a and 16b in the on-line documentation
    // x[0] transfers the current estimates of the adsorbed ion concentrations
    public double function(double[] x){
        double gFunction = 0.0D;
        double arg =0.0D;
        int ii = 0;
        double convFac = this.surfaceArea/this.volume;
        double expTerm = psiDelta*Fmath.Q_ELECTRON/(Fmath.K_BOLTZMANN*this.tempK);
        double innerSumTerm =0.0D;

        for(int i=0; i<this.nonZeroAssocK; i++){
            innerSumTerm += x[0];
        }
        innerSumTerm = this.surfaceSiteDensity  - innerSumTerm;

        for(int i=0; i<this.nonZeroAssocK; i++){
            ii = indexK[i];
            arg = this.assocK[ii]*(this.initConcn[ii] - x[i]*convFac)*Math.exp(expTerm*this.charges[ii])*innerSumTerm - x[0];
            gFunction += arg*arg;
        }
        return gFunction;
    }
}