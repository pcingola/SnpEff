/*
*   Interface MaximizationFunction
*
*   This interface provides the abstarct method for the function to be
*   maximized by the methods in the class, Maximization
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    12 May 2008 to match maximisationFunction (April 2003)

*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Maximisation.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2008
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
****************************************************************************************/

package flanagan.math;

// Interface for Maximization class
// Calculates value of function to be maximized
public interface MaximizationFunction{

    double function(double[]param);
}