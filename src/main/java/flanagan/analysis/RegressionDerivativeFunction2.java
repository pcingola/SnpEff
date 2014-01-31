/*
*   Interface RegressionFunction2
*
*   The sum of squares function, for multiple y array option, needed
*   by the non-linear regression methods in the class Regression
*   is supplied by means of this interface, RegressionFunction2
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    October 2008
*   MODIFIED:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2008
*
*   PERMISSION TO COPY:
*
*   Redistributions of this source code, or parts of, must retain the above
*   copyright notice, this list of conditions and the following disclaimer.
*
*   Redistribution in binary form of all or parts of this class, must reproduce
*   the above copyright, this list of conditions and the following disclaimer in
*   the documentation and/or other materials provided with the distribution.
*
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all
*   copies and associated documentation or publications.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/


package flanagan.analysis;

// Interface for Regression class
// Derivative function for non-linear regression methods
// i = index of parameter a in dy/da[i]
// k = index of the data x value
public interface RegressionDerivativeFunction2{
    double function(double[]param, double[] x, int i, int k);
}
