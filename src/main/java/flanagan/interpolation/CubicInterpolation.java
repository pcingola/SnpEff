/**********************************************************
*
*   CubicInterpolation.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1) using a cubic interploation procedure
**
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	15-16 January 2011
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/CubicInterpolation.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2011   Michael Thomas Flanagan
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

import java.util.ArrayList;

import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.math.ArrayMaths;

public class CubicInterpolation{

    	private int nPoints = 0;   	                            // no. of x tabulated points
    	private double[] x = null;   	                        // x in tabulated function f(x1,x2)
    	private double[] y = null;  	                        // y=f(x) tabulated function
    	private double[] dydx = null;  	                        // dy/dx
        private boolean derivCalculated = false;                // = true when the derivatives have been calculated or entered
        private CubicSpline cs = null;                          // cubic spline used in calculating the derivatives
        private double incrX = 0;                               // x increment used in calculating the derivatives

        double[][] coeff = null;                                // cubic coefficients

    	private double xx = Double.NaN;                        // value of x at which an interpolated y value is required

        // Weights used in calculating the grid square coefficients
        private double[][] weights =   {{1.0,0.0,0.0,0.0},{0.0,0.0,1.0,0.0},{-3.0,3.0,-2.0,-1.0},{2.0,-2.0,1.0,1.0}};

    	private int[] xIndices = null;                         // x data indices before ordering

    	private double xMin = 0.0;                              // minimum value of x
    	private double xMax = 0.0;                              // maximum value of x

    	private double interpolatedValue = Double.NaN;          // interpolated value of y
    	private double interpolatedDydx = Double.NaN;           // interpolated value of dydx

        private boolean numerDiffFlag = true;                   // = true:  if numerical differentiation performed h1 and h2 calculated using delta
                                                                // = false: if numerical differentiation performed h1 and h2 calculated only provided data points
        private static double delta = 1e-3;                     // fractional step factor used in calculating the derivatives
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = false;           // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)

    	// Constructor without derivatives
    	public CubicInterpolation(double[] x, double[] y, int numerDiffOption){
            // set numerical differencing option
            if(numerDiffOption==0){
                this.numerDiffFlag = false;
            }
            else{
                if(numerDiffOption==1){
                    this.numerDiffFlag = true;
                }
                else{
                    throw new IllegalArgumentException("The numerical differencing option, " + numerDiffOption + ", must be 0 or 1");
                }
            }

       	    // initialize the data
            this.initialize(Conv.copy(x), Conv.copy(y));

        	// calculate the derivatives
        	this.calcDeriv();

        	// calculate grid coefficients for all grid squares
        	this.gridCoefficients();
    	}

    	// Constructor with derivatives
    	public CubicInterpolation(double[] x, double[] y, double[] dydx){

      	    // initialize the data
            this.initialize(Conv.copy(x), Conv.copy(y), Conv.copy(dydx));

        	// calculate grid coefficients for all grid squares
        	this.gridCoefficients();
    	}

    	// Initialize the data
    	private void initialize(double[] x, double[] y){
    	    this.initialize(x, y, null, false);
    	}

    	private void initialize(double[] x, double[] y, double[] dydx){
    	    this.initialize(x, y, dydx, true);
    	}

    	private void initialize(double[] x, double[] y, double[] dydx, boolean flag){
    	    int nl = 3;
    	    if(flag)nl = 2;
         	int nPoints=x.length;
        	if(nPoints!=y.length)throw new IllegalArgumentException("Arrays x and y-row are of different length " + nPoints + " " + y.length);
          	if(nPoints<nl)throw new IllegalArgumentException("The data matrix must have a minimum size of " + nl + " X " + nl);

            // order data
            ArrayMaths am = new ArrayMaths(x);
            am = am.sort();
            this.xIndices = am.originalIndices();
            x = am.array();
            double[] hold = new double[nPoints];

            for(int i=0; i<nPoints; i++){
    	        hold[i] = y[this.xIndices[i]];
    	    }
            for(int i=0; i<nPoints; i++){
    	        y[i] = hold[i];
    	    }

    	    if(flag){
                for(int i=0; i<nPoints; i++){
    	            hold[i] = dydx[this.xIndices[i]];
      	        }
                for(int i=0; i<nPoints; i++){
    	            dydx[i] = hold[i];
    	        }
            }

    	    // check for identical x values
    	    for(int i=1; i<nPoints; i++){
    	        if(x[i]==x[i-1]){
    	            System.out.println("x["+this.xIndices[i]+"] and x["+this.xIndices[i+1]+"] are identical, " +  x[i]);
    	            System.out.println("The y values have been averaged and one point has been deleted");
    	            y[i-1] = (y[i-1] + y[i])/2.0;
    	            for(int j=i; j<nPoints-1; j++){
    	                x[j]=x[j+1];
    	                y[j]=y[j+1];
    	                this.xIndices[j]=this.xIndices[j+1];
    	            }
    	            if(flag){
    	                dydx[i-1] = (dydx[i-1] + dydx[i])/2.0;
    	                for(int j=i; j<nPoints-1; j++){
    	                    dydx[j]=dydx[j+1];
    	                }
    	            }
    	            nPoints--;
    	        }
    	    }

    	    // assign variables
    	    this.nPoints = nPoints;
    	    this.x = new double[this.nPoints];
        	this.y = new double[this.nPoints];
    	    this.dydx = new double[this.nPoints];

        	for(int i=0; i<this.nPoints; i++){
            	this.x[i]=x[i];
                this.y[i]=y[i];
            }
            if(flag){
                for(int j=0; j<this.nPoints; j++){
                   this.dydx[j]=dydx[j];
                }
        	    this.derivCalculated = true;
            }

	        // limits
        	this.xMin = Fmath.minimum(this.x);
        	this.xMax = Fmath.maximum(this.x);

        	if(!flag && this.numerDiffFlag){
        	    // numerical difference increments
        	    double range = this.xMax - this.xMin;
        	    double averageSeparation = range/this.nPoints;
        	    double minSep = this.x[1] - this.x[0];
        	    double minimumSeparation = minSep;
        	    for(int i=2; i<this.nPoints; i++){
        	        minSep = this.x[i] - this.x[i-1];
        	        if(minSep<minimumSeparation)minimumSeparation = minSep;
        	    }

                this.incrX = range*CubicInterpolation.delta;
                double defaultIncr = minimumSeparation;
                if(minimumSeparation<averageSeparation/10.0)defaultIncr = averageSeparation/10.0;
                if(this.incrX>averageSeparation)this.incrX = defaultIncr;
            }
        }

        // Calculate the derivatives
        private void calcDeriv(){

            if(this.numerDiffFlag){
                // Numerical differentiation using delta and interpolation
                this.cs = new CubicSpline(this.x, this.y);
    	        double[] xjp1 = new double[this.nPoints];
    	        double[] xjm1 = new double[this.nPoints];
    	        for(int i=0; i<this.nPoints; i++){
    	            xjp1[i] = this.x[i] + this.incrX;
    	            if(xjp1[i]>this.x[this.nPoints-1])xjp1[i] = this.x[this.nPoints-1];
    	            xjm1[i] = this.x[i] - this.incrX;
    	            if(xjm1[i]<this.x[0])xjm1[i] = this.x[0];
    	        }

    	        for(int i=0; i<this.nPoints; i++){
    	            this.dydx[i] = (cs.interpolate(xjp1[i]) - cs.interpolate(xjm1[i]))/(xjp1[i] - xjm1[i]);
                }
            }
            else{
                // Numerical differentiation using provided data points
                int iip =0;
                int iim =0;
                for(int i=0; i<this.nPoints; i++){
                    iip = i+1;
    	            if(iip>=this.nPoints)iip = this.nPoints-1;
    	            iim = i-1;
    	            if(iim<0)iim = 0;
    	            this.dydx[i] = (this.y[iip] - this.y[iim])/(this.x[iip] - this.x[iim]);
                }
            }
	    	this.derivCalculated = true;
    	}

    	// Grid coefficients
    	private void gridCoefficients(){

    	    double[] xt = new double[4];
    	    this.coeff = new double[this.nPoints][4];
    	    double d1 = 0.0;
    	    for(int i=0; i<this.nPoints-1; i++){
    	        d1 = this.x[i+1] - this.x[i];
    	        xt[0] = this.y[i];
    	        xt[1] = this.y[i+1];
    	        xt[2] = this.dydx[i]*d1;
    	        xt[3] = this.dydx[i+1]*d1;

                double xh = 0.0;
    	        for(int k=0; k<4; k++){
    	            for(int kk=0; kk<4; kk++){
    	                xh += this.weights[k][kk]*xt[kk];
    	            }
    	            this.coeff[i][k] = xh;
    	            xh = 0.0;
    	        }
    	    }
    	}

    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x,x2)
    	public double interpolate(double xx){
    	    // check that xx and xx2 are within the limits
    	    if(xx<x[0]){
    	        if(xx>=x[0]-CubicInterpolation.potentialRoundingError){
    	            xx=this.x[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx + " is outside the limits, " + x[0] + " - " + x[this.nPoints-1]);
    	        }
    	    }

    	    if(xx>this.x[this.nPoints-1]){
    	        if(xx<=this.x[this.nPoints-1]+CubicInterpolation.potentialRoundingError){
    	            xx=this.x[this.nPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx + " is outside the limits, " + this.x[0] + " - " + this.x[this.nPoints-1]);
    	        }
    	    }

    	    // assign variables
    	    this.xx = xx;

            // Find grid surrounding the interpolation point
            int gridn = 0;
            int counter = 1;
            boolean test = true;
            while(test){
                if(xx<this.x[counter]){
                    gridn = counter - 1;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=this.nPoints){
                        gridn = this.nPoints-2;
                        test = false;
                    }
                }
            }

            // interpolation
            double xNormalised = (xx - x[gridn])/(x[gridn+1] - x[gridn]);
            this.interpolatedValue = 0.0;
            for(int i=0; i<4; i++){
                this.interpolatedValue += this.coeff[gridn][i]*Math.pow(xNormalised, i);
            }
            this.interpolatedDydx = 0.0;
            for(int i=1; i<4; i++){
                this.interpolatedDydx += i*this.coeff[gridn][i]*Math.pow(xNormalised, i-1);
            }

            return this.interpolatedValue;
        }

        // Return last interpolated value and the interpolated gradients
        public double[] getInterpolatedValues(){
            double[] ret = new double[3];
            ret[0] = this.interpolatedValue;
            ret[1] = this.interpolatedDydx;
            ret[2] = this.xx;
            return ret;
        }

        // Return grid point values of dydx
        public double[] getGridDydx(){
            double[] ret = new double[this.nPoints];
            for(int i=0; i<this.nPoints; i++){
                ret[this.xIndices[i]] = this.dydx[i];
            }
            return ret;
        }

        // Reset the numerical differentiation incremental factor delta
    	public static void resetDelta(double delta){
            CubicInterpolation.delta = delta;
        }

    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            CubicInterpolation.roundingCheck = false;
            CubicInterpolation.potentialRoundingError = 0.0;
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            CubicInterpolation.potentialRoundingError = potentialRoundingError;
        }

   	    // Get minimum limit
    	public double getXmin(){
    	    return this.xMin;
    	}

    	// Get maximum limit
    	public double getXmax(){
    	    return this.xMax;
    	}

    	// Get limits to x
    	public double[] getLimits(){
    	    double[] limits = {this.xMin, this.xMax};
    	    return limits;
    	}

    	// Display limits to x
    	public void displayLimits(){
    	    System.out.println(" ");
    	    System.out.println("The limits to the x array are " + this.xMin + " and " + this.xMax);
    	    System.out.println(" ");
    	}

}

