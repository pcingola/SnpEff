/*      Class ImpedSpecRegressionFunction1
*
*       This class acts as a container for a precompiled circuit model,
*       selected from class Impedance, for the class ImpedSpecRegression
*       which contains the non-linear regression procedures for fitting impedance
*       spectroscopy and electrochemical impedance spectroscopy data to a circuit model.
*
*       This class implements RegressionFunction2
*       The user supplied circuit model requires the interface ImpedSpecModel
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    May 2007
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecSimulation.html
*
*       Copyright (c) May 2007    Michael Thomas Flanagan
*
*       PERMISSION TO COPY:
*       Permission to use, copy and modify this software and its documentation for
*       NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*       to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*       Dr Michael Thomas Flanagan makes no representations about the suitability
*       or fitness of the software for any or for a particular purpose.
*       Michael Thomas Flanagan shall not be liable for any damages suffered
*       as a result of using, modifying or distributing this software or its derivatives.
*
****************************************************************************************/

package flanagan.circuits;

import flanagan.io.*;
import flanagan.complex.Complex;
import flanagan.analysis.RegressionFunction2;

public class ImpedSpecRegressionFunction1 extends Impedance implements RegressionFunction2{

    public int numberOfFrequencies = 0;     // number of frequencies
    public int modelNumber = 0;             // Impedance class model number

    public double function(double[] parameters, double[] omega, int pointN){

        Complex zVal = Impedance.modelImpedance(parameters, omega[0], this.modelNumber);  // call impedance calculation method

        if(pointN<this.numberOfFrequencies){
            return zVal.getReal();
        }
        else{
            return zVal.getImag();
        }
    }
}


