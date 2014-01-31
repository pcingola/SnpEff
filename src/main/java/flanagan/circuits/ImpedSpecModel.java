/*      Interface ImpedSpecModel
*
*       Needed for user supplied model circuits in the classes
*       ImpedSpecSimulation and ImpedSpecRegression
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    May 2007  (Derived from impedance spectroscopy programs, 2004 - 2007)
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecSimulation.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecRegression.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
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

import flanagan.complex.Complex;

public interface ImpedSpecModel{
    Complex modelImpedance(double[] parameters, double omega);
}