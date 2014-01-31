/*
*   Class Minimization
*   IDENTICAL TO class Minimisation - created simply to accommodate alternate spelling
*
*   Contains methods for finding the values of the
*   function parameters that minimize that function
*   using the Nelder and Mead Simplex method.
*
*   The function needed by the minimization method
*   is supplied by though the interface, MinimizationFunction
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    April 2003
*   MODIFIED:   29 December 2005, 18 February 2006, 28 December 2007, 10/12 May 2008
*
*   DOCUMENTATION
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Minimisation.html
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
***************************************************************************************/

package flanagan.math;

import java.util.*;
import flanagan.math.Fmath;
import flanagan.io.FileOutput;

// Minimization/Minimisation class
public class Minimization extends Minimisation{

    //Constructors
    public Minimization(){
        super();
        super.iseOption = false;
	}
}
