/**********************************************************
*
*   BiCubicSplinePartialDerivative.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2) using a natural bicubic spline
*   Assumes second derivatives at end points = 0 (natural spine)
*   Calculates the interpolated value of y and its first derivative with respect to x1
*
*   Primarily for use with BiCubicSplineFirstDerivative
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	12 January 2010  - modified version of BiCubicSpline (2003 - 2008)
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/BiCubicSplineFirstDerivative.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2010   Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
* Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
* provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
* and associated documentation or publications.
*
* Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice, this list of conditions
* and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
* Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
* the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission from the Michael Thomas Flanagan:
*
* Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
* Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
* or its derivatives.
*
***************************************************************************************/

package flanagan.interpolation;

import flanagan.math.Fmath;

public class BiCubicSplinePartialDerivative{

    	private int nPoints = 0;   	                            // no. of x1 tabulated points
    	private int mPoints = 0;   	                            // no. of x2 tabulated points
    	private double[][] y = null;  	                        // y=f(x1,x2) tabulated function
    	private double[] x1 = null;   	                        // x1 in tabulated function f(x1,x2)
    	private double[] x2 = null;                             // x2 in tabulated function f(x1,x2)
    	private double[] xMin = new double[2];                  // minimum values of x1 and x2
    	private double[] xMax = new double[2];                  // maximum values of x1 and x2
    	private CubicSpline csn[] = null;                       // nPoints array of CubicSpline instances
    	private CubicSpline csm = null;                         // CubicSpline instance
        private boolean derivCalculated = false;                // = true when the first called cubic spline derivatives have been calculated
        private boolean averageIdenticalAbscissae = false;      // if true: the the ordinate values for identical abscissae are averaged
                                                                // If false: the abscissae values are separated by 0.001 of the total abscissae range;
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = true;            // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)


    	// Constructor
    	// Constructor with data arrays initialised to arrays x and y
    	public BiCubicSplinePartialDerivative(double[] x1, double[] x2, double[][] y){
        	this.nPoints=x1.length;
        	this.mPoints=x2.length;
        	if(this.nPoints!=y.length)throw new IllegalArgumentException("Arrays x1 and y-row are of different length " + this.nPoints + " " + y.length);
        	if(this.mPoints!=y[0].length)throw new IllegalArgumentException("Arrays x2 and y-column are of different length "+ this.mPoints + " " + y[0].length);
          	if(this.nPoints<3 || this.mPoints<3)throw new IllegalArgumentException("The data matrix must have a minimum size of 3 X 3");

        	this.csm = new CubicSpline(this.nPoints);
        	this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
        	this.x1 = new double[this.nPoints];
        	this.x2 = new double[this.mPoints];
        	this.y = new double[this.nPoints][this.mPoints];
        	for(int i=0; i<this.nPoints; i++){
            		this.x1[i]=x1[i];
        	}
        	this.xMin[0] = Fmath.minimum(this.x1);
        	this.xMax[0] = Fmath.maximum(this.x1);
        	for(int j=0; j<this.mPoints; j++){
            		this.x2[j]=x2[j];
        	}
        	this.xMin[1] = Fmath.minimum(this.x2);
        	this.xMax[1] = Fmath.maximum(this.x2);
        	for(int i =0; i<this.nPoints; i++){
            		for(int j=0; j<this.mPoints; j++){
                		this.y[i][j]=y[i][j];
            		}
        	}

        	double[] yTempn = new double[mPoints];

	    	for(int i=0; i<this.nPoints; i++){
	        	for(int j=0; j<mPoints; j++)yTempn[j]=y[i][j];
	        	this.csn[i].resetData(x2,yTempn);
	        	this.csn[i].calcDeriv();
	    	}
	    	this.derivCalculated = true;
    	}

    	//  METHODS

    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            BiCubicSplinePartialDerivative.roundingCheck = false;
            CubicSpline.noRoundingErrorCheck();
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            BiCubicSplinePartialDerivative.potentialRoundingError = potentialRoundingError;
            CubicSpline.potentialRoundingError(potentialRoundingError);
        }

        // Reset the default handing of identical abscissae with different ordinates
        // from the default option of separating the two relevant abscissae by 0.001 of the range
        // to avraging the relevant ordinates
    	public void averageIdenticalAbscissae(){
    	    this.averageIdenticalAbscissae = true;
    	    for(int i=0; i<this.csn.length; i++)this.csn[i].averageIdenticalAbscissae();
    	    this.csm.averageIdenticalAbscissae();
    	}

   	    // Get minimum limits
    	public double[] getXmin(){
    	    return this.xMin;
    	}

    	// Get maximum limits
    	public double[] getXmax(){
    	    return this.xMax;
    	}

    	// Get limits to x
    	public double[] getLimits(){
    	    double[] limits = {xMin[0], xMax[0], xMin[1], xMax[1]};
    	    return limits;
    	}

    	// Display limits to x
    	public void displayLimits(){
    	    System.out.println(" ");
    	    for(int i=0; i<2; i++){
    	        System.out.println("The limits to the x array " + i + " are " + xMin[i] + " and " + xMax[i]);
    	    }
    	    System.out.println(" ");
    	}


    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x1,x2)
    	public double[] interpolate(double xx1, double xx2){

	    	double[] yTempm = new double[this.nPoints];

	    	for (int i=0;i<this.nPoints;i++){
		    	yTempm[i]=this.csn[i].interpolate(xx2);
	    	}
	    	this.csm.resetData(x1,yTempm);
	    	return this.csm.interpolate_for_y_and_dydx(xx1);
    	}
}

