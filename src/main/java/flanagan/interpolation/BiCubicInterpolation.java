/**********************************************************
*
*   BiCubicInterpolation.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2) using a bicubic interploation procedure
**
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	10-12 January 2011
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/BiCubicInterpolation.html
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

public class BiCubicInterpolation{

    	private int nPoints = 0;   	                            // no. of x1 tabulated points
    	private int mPoints = 0;   	                            // no. of x2 tabulated points
    	private double[] x1 = null;   	                        // x1 in tabulated function f(x1,x2)
    	private double[] x2 = null;                             // x2 in tabulated function f(x1,x2)
    	private double[][] y = null;  	                        // y=f(x1,x2) tabulated function

    	private double[][] dydx1 = null;  	                    // dy/dx1
    	private double[][] dydx2 = null;  	                    // dy/dx2
    	private double[][] d2ydx1dx2 = null;  	                // d2y/dx1dx2
        private boolean derivCalculated = false;                // = true when the derivatives have been calculated or entered
        private BiCubicSpline bcs = null;                       // bicubic spline used in calculating the derivatives
        private double incrX1 = 0;                              // x1 increment used in calculating the derivatives
        private double incrX2 = 0;                              // x2 increment used in calculating the derivatives

    	private double xx1 = Double.NaN;                        // value of x1 at which an interpolated y value is required
    	private double xx2 = Double.NaN;                        // value of x2 at which an interpolated y value is required

        private ArrayList<Object> coeff = new ArrayList<Object>();  // grid square coefficients

        // Weights used in calculating the grid square coefficients
        private double[][] weights =   {{1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
                                        {-3.0,0.0,0.0,3.0,0.0,0.0,0.0,0.0,-2.0,0.0,0.0,-1.0,0.0,0.0,0.0,0.0},
                                        {2.0,0.0,0.0,-2.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,-3.0,0.0,0.0,3.0,0.0,0.0,0.0,0.0,-2.0,0.0,0.0,-1.0},
                                        {0.0,0.0,0.0,0.0,2.0,0.0,0.0,-2.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0},
                                        {-3.0,3.0,0.0,0.0,-2.0,-1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,-3.0,3.0,0.0,0.0,-2.0,-1.0,0.0,0.0},
                                        {9.0,-9.0,9.0,-9.0,6.0,3.0,-3.0,-6.0,6.0,-6.0,-3.0,3.0,4.0,2.0,1.0,2.0},
                                        {-6.0,6.0,-6.0,6.0,-4.0,-2.0,2.0,4.0,-3.0,3.0,3.0,-3.0,-2.0,-1.0,-1.0,-2.0},
                                        {2.0,-2.0,0.0,0.0,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
                                        {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,-2.0,0.0,0.0,1.0,1.0,0.0,0.0},
                                        {-6.0,6.0,-6.0,6.0,-3.0,-3.0,3.0,3.0,-4.0,4.0,2.0,-2.0,-2.0,-2.0,-1.0,-1.0},
                                        {4.0,-4.0,4.0,-4.0,2.0,2.0,-2.0,-2.0,2.0,-2.0,-2.0,2.0,1.0,1.0,1.0,1.0}};

    	private int[] x1indices = null;                         // x1 data indices before ordering
    	private int[] x2indices = null;                         // x2 data indices before ordering

    	private double[] xMin = new double[2];                  // minimum values of x1 and x2
    	private double[] xMax = new double[2];                  // maximum values of x1 and x2

    	private double interpolatedValue = Double.NaN;          // interpolated value of y
    	private double interpolatedDydx1 = Double.NaN;          // interpolated value of dydx1
    	private double interpolatedDydx2 = Double.NaN;          // interpolated value of dydx2
    	private double interpolatedD2ydx1dx2 = Double.NaN;      // interpolated value of d2ydx1dx2

        private boolean numerDiffFlag = true;                   // = true:  if numerical differentiation performed h1 and h2 calculated using delta
                                                                // = false: if numerical differentiation performed h1 and h2 calculated only provided data points

        private static double delta = 1e-3;                     // fractional step factor used in calculating the derivatives
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = false;            // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)

    	// Constructor without derivatives
    	// numerDiffOption = 0 -> numerical differencing using only supplied data points
    	// numerDiffOption = 1 -> numerical differencing using interpolation
    	public BiCubicInterpolation(double[] x1, double[] x2, double[][] y, int numerDiffOption){
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
            this.initialize(Conv.copy(x1), Conv.copy(x2), Conv.copy(y));

        	// calculate the derivatives
        	this.calcDeriv();

        	// calculate grid coefficients for all grid squares
        	this.gridCoefficients();
    	}

    	// Constructor without derivatives
    	// Numerical differencing by interpolation
    	// Retained for compatability
    	public BiCubicInterpolation(double[] x1, double[] x2, double[][] y){
            // set numerical differencing option
            this.numerDiffFlag = true;

       	    // initialize the data
            this.initialize(Conv.copy(x1), Conv.copy(x2), Conv.copy(y));

        	// calculate the derivatives
        	this.calcDeriv();

        	// calculate grid coefficients for all grid squares
        	this.gridCoefficients();
    	}

    	// Constructor with derivatives
    	public BiCubicInterpolation(double[] x1, double[] x2, double[][] y, double[][] dydx1, double[][] dydx2, double[][] d2ydx1dx2){

      	    // initialize the data
            this.initialize(Conv.copy(x1), Conv.copy(x2), Conv.copy(y), Conv.copy(dydx1), Conv.copy(dydx2), Conv.copy(d2ydx1dx2));

        	// calculate grid coefficients for all grid squares
        	this.gridCoefficients();
    	}

    	// Initialize the data
    	private void initialize(double[] x1, double[] x2, double[][] y){
    	    this.initialize(x1, x2, y, null, null, null, false);
    	}

    	private void initialize(double[] x1, double[] x2, double[][] y, double[][] dydx1, double[][] dydx2, double[][] d2ydx1dx2){
    	    this.initialize(x1, x2, y, dydx1, dydx2, d2ydx1dx2, true);
    	}

    	private void initialize(double[] x1, double[] x2, double[][] y, double[][] dydx1, double[][] dydx2, double[][] d2ydx1dx2, boolean flag){
         	int nPoints=x1.length;
        	int mPoints=x2.length;
        	if(nPoints!=y.length)throw new IllegalArgumentException("Arrays x1 and y-row are of different length " + nPoints + " " + y.length);
        	if(mPoints!=y[0].length)throw new IllegalArgumentException("Arrays x2 and y-column are of different length "+ mPoints + " " + y[0].length);
          	if(nPoints<2 || mPoints<2)throw new IllegalArgumentException("The data matrix must have a minimum size of 2 X 2");

            // order data
            ArrayMaths am = new ArrayMaths(x1);
            am = am.sort();
            this.x1indices = am.originalIndices();
            x1 = am.array();
            double[][] hold = new double[nPoints][mPoints];
            double[][] hold1 = null;
            double[][] hold2 = null;
            double[][] hold12 = null;

            for(int i=0; i<nPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            hold[i][j] = y[this.x1indices[i]][j];
    	        }
    	    }
            for(int i=0; i<nPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            y[i][j] = hold[i][j];
    	        }
    	    }

    	    if(flag){
                hold1 = new double[nPoints][mPoints];
                hold2 = new double[nPoints][mPoints];
                hold12 = new double[nPoints][mPoints];
                for(int i=0; i<nPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                hold1[i][j] = dydx1[this.x1indices[i]][j];
    	                hold2[i][j] = dydx2[this.x1indices[i]][j];
    	                hold12[i][j] = d2ydx1dx2[this.x1indices[i]][j];
    	            }
    	        }
                for(int i=0; i<nPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                dydx1[i][j] = hold1[i][j];
    	                dydx2[i][j] = hold2[i][j];
    	                d2ydx1dx2[i][j] = hold12[i][j];
    	            }
    	        }
            }

    	    am = new ArrayMaths(x2);
            am = am.sort();
            this.x2indices = am.originalIndices();
            double[] xh = am.array();
            for(int i=0; i<mPoints; i++)x2[i] = xh[mPoints-1-i];
            for(int i=0; i<mPoints; i++){
    	        for(int j=0; j<nPoints; j++){
    	            hold[j][i] = y[j][this.x2indices[i]];
    	        }
    	    }
            for(int i=0; i<nPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            y[i][j] = hold[i][mPoints-1-j];
    	        }
    	    }
    	    if(flag){
    	        for(int i=0; i<mPoints; i++){
    	            for(int j=0; j<nPoints; j++){
    	                hold1[j][i] = dydx1[j][this.x2indices[i]];
    	                hold2[j][i] = dydx2[j][this.x2indices[i]];
    	                hold12[j][i] = d2ydx1dx2[j][this.x2indices[i]];
    	            }
    	        }
                for(int i=0; i<nPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                dydx1[i][j] = hold1[i][mPoints-1-j];
    	                dydx2[i][j] = hold2[i][mPoints-1-j];
    	                d2ydx1dx2[i][j] = hold12[i][mPoints-1-j];
    	            }
    	        }
    	    }

    	    // check for identical x1 values
    	    for(int i=1; i<nPoints; i++){
    	        if(x1[i]==x1[i-1]){
    	            System.out.println("x1["+this.x1indices[i]+"] and x1["+this.x1indices[i+1]+"] are identical, " +  x1[i]);
    	            System.out.println("The y values have been averaged and one point has been deleted");
    	            for(int j=i; j<nPoints-1; j++){
    	                x1[j]=x1[j+1];
    	                this.x1indices[j]=this.x1indices[j+1];
    	            }
    	            for(int j=0; j<mPoints; j++){
    	                y[i-1][j] = (y[i-1][j] + y[i][j])/2.0;
    	                for(int k=i; k<nPoints-1; k++)y[k][j]=y[k+1][j];
    	                if(flag){
    	                    dydx1[i-1][j] = (dydx1[i-1][j] + dydx1[i][j])/2.0;
    	                    dydx2[i-1][j] = (dydx2[i-1][j] + dydx2[i][j])/2.0;
    	                    d2ydx1dx2[i-1][j] = (d2ydx1dx2[i-1][j] + d2ydx1dx2[i][j])/2.0;
    	                    for(int k=i; k<nPoints-1; k++){
    	                        dydx1[k][j]=dydx1[k+1][j];
    	                        dydx2[k][j]=dydx2[k+1][j];
    	                        d2ydx1dx2[k][j]=d2ydx1dx2[k+1][j];
    	                    }
    	                }
    	            }
    	            nPoints--;
    	        }
    	    }
    	    // check for identical x2 values
    	    for(int i=1; i<mPoints; i++){
    	        if(x2[i]==x2[i-1]){
    	            System.out.println("x2["+this.x2indices[i]+"] and x2["+this.x2indices[i]+"] are identical, " +  x2[i]);
    	            System.out.println("The y values have been averaged and one point has been deleted");
    	            for(int j=i; j<mPoints-1; j++){
    	                x2[j]=x2[j+1];
    	                this.x2indices[j]=this.x2indices[j+1];
    	            }
    	            for(int j=0; j<nPoints; j++){
    	                y[j][i-1] = (y[j][i-1] + y[j][i])/2.0;
    	                for(int k=i; k<mPoints-1; k++)y[j][k]=y[j][k+1];
    	                if(flag){
    	                    dydx1[j][i-1] = (dydx1[j][i-1] + dydx1[j][i])/2.0;
    	                    dydx2[j][i-1] = (dydx2[j][i-1] + dydx2[j][i])/2.0;
    	                    d2ydx1dx2[j][i-1] = (d2ydx1dx2[j][i-1] + d2ydx1dx2[j][i])/2.0;
    	                    for(int k=i; k<nPoints-1; k++){
    	                        dydx1[j][k]=dydx1[j][k+1];
    	                        dydx2[j][k]=dydx2[j][k+1];
    	                        d2ydx1dx2[j][k]=d2ydx1dx2[j][k+1];
    	                    }
    	                }
    	            }
    	            mPoints--;
    	        }
    	    }

    	    // assign variables
    	    this.nPoints = nPoints;
    	    this.mPoints = mPoints;
    	    this.x1 = new double[this.nPoints];
        	this.x2 = new double[this.mPoints];
        	this.y = new double[this.nPoints][this.mPoints];
    	    this.dydx1 = new double[this.nPoints][this.mPoints];
    	    this.dydx2 = new double[this.nPoints][this.mPoints];
    	    this.d2ydx1dx2 = new double[this.nPoints][this.mPoints];

        	for(int i=0; i<this.nPoints; i++){
            	this.x1[i]=x1[i];
        	}
        	for(int j=0; j<this.mPoints; j++){
            	this.x2[j]=x2[j];
        	}
        	for(int i =0; i<this.nPoints; i++){
            	for(int j=0; j<this.mPoints; j++){
                	this.y[i][j]=y[i][j];
            	}
            	if(flag){
                    for(int j=0; j<this.mPoints; j++){
                        this.dydx1[i][j]=dydx1[i][j];
                        this.dydx2[i][j]=dydx2[i][j];
                        this.d2ydx1dx2[i][j]=d2ydx1dx2[i][j];
            		}
                }
        	}
        	if(flag)this.derivCalculated = true;

	        // limits
        	this.xMin[0] = Fmath.minimum(this.x1);
        	this.xMax[0] = Fmath.maximum(this.x1);
        	this.xMin[1] = Fmath.minimum(this.x2);
        	this.xMax[1] = Fmath.maximum(this.x2);

            if(!flag && this.numerDiffFlag){
        	    // numerical difference increments
        	    double range1 = this.xMax[0] - this.xMin[0];
        	    double range2 = this.xMax[1] - this.xMin[1];
        	    double averageSeparation1 = range1/this.nPoints;
        	    double averageSeparation2 = range2/this.mPoints;
        	    double minSep = this.x1[1] - this.x1[0];
        	    double minimumSeparation1 = minSep;
        	    for(int i=2; i<this.nPoints; i++){
        	        minSep = this.x1[i] - this.x1[i-1];
        	        if(minSep<minimumSeparation1)minimumSeparation1 = minSep;
        	    }
        	    minSep = this.x2[1] - this.x2[0];
        	    double minimumSeparation2 = minSep;
        	    for(int i=2; i<this.mPoints; i++){
        	        minSep = this.x2[i] - this.x2[i-1];
        	        if(minSep<minimumSeparation2)minimumSeparation2 = minSep;
        	    }

                this.incrX1 = range1*BiCubicInterpolation.delta;
                double defaultIncr = minimumSeparation1;
                if(minimumSeparation1<averageSeparation1/10.0)defaultIncr = averageSeparation1/10.0;
                if(this.incrX1>averageSeparation1)this.incrX1 = defaultIncr;
                this.incrX2 = range2*BiCubicInterpolation.delta;
                defaultIncr = minimumSeparation2;
                if(minimumSeparation2<averageSeparation2/10.0)defaultIncr = averageSeparation2/10.0;
                if(this.incrX2>averageSeparation2)this.incrX2 = defaultIncr;
            }
        }

        // Calculate the derivatives
        private void calcDeriv(){

            if(this.numerDiffFlag){
                // Numerical differentiation using delta and interpolation
                this.bcs = new BiCubicSpline(this.x1, this.x2, this.y);
    	        double yjp1k = 0.0;
    	        double yjm1k = 0.0;
    	        double[] x1jp1 = new double[this.nPoints];
    	        double[] x1jm1 = new double[this.nPoints];
    	        double[] x2jp1 = new double[this.mPoints];
    	        double[] x2jm1 = new double[this.mPoints];

    	        for(int i=0; i<this.nPoints; i++){
    	            x1jp1[i] = this.x1[i] + this.incrX1;
    	            if(x1jp1[i]>this.x1[this.nPoints-1])x1jp1[i]=this.x1[this.nPoints-1];
    	            x1jm1[i] = this.x1[i] - this.incrX1;
    	            if(x1jm1[i]<this.x1[0])x1jm1[i]=this.x1[0];
    	        }
    	        for(int i=0; i<this.mPoints; i++){
    	            x2jp1[i] = this.x2[i] + this.incrX2;
    	            if(x2jp1[i]>this.x2[0])x2jp1[i]=this.x2[0];
    	            x2jm1[i] = this.x2[i] - this.incrX2;
    	            if(x2jm1[i]<this.x2[this.mPoints-1])x2jm1[i]=this.x2[this.mPoints-1];

    	        }
    	        for(int i=0; i<this.nPoints; i++){
    	            for(int j=0; j<this.mPoints; j++){
    	                this.dydx1[i][j] = (bcs.interpolate(x1jp1[i],x2[j]) - bcs.interpolate(x1jm1[i],x2[j]))/(x1jp1[i] - x1jm1[i]);
    	                this.dydx2[i][j] = (bcs.interpolate(x1[i],x2jp1[j]) - bcs.interpolate(x1[i],x2jm1[j]))/(x2jp1[j] - x2jm1[j]);
    	                this.d2ydx1dx2[i][j] = (bcs.interpolate(x1jp1[i],x2jp1[j]) - bcs.interpolate(x1jp1[i],x2jm1[j]) - bcs.interpolate(x1jm1[i],x2jp1[j]) + bcs.interpolate(x1jm1[i],x2jm1[j]))/((x1jp1[i] - x1jm1[i])*(x2jp1[j] - x2jm1[j]));
                    }
                }
            }
            else{
                // Numerical differentiation using only provided data points
                int iip =0;
                int iim =0;
                int jjp =0;
                int jjm =0;
                for(int i=0; i<this.nPoints; i++){
                    iip = i+1;
    	            if(iip>=this.nPoints)iip = this.nPoints-1;
    	            iim = i-1;
    	            if(iim<0)iim = 0;
    	            for(int j=0; j<this.mPoints; j++){
    	                jjp = j+1;
    	                if(jjp>=this.mPoints)jjp = this.mPoints-1;
    	                jjm = j-1;
    	                if(jjm<0)jjm = 0;
    	                this.dydx1[i][j] = (this.y[iip][j] - this.y[iim][j])/(this.x1[iip] - this.x1[iim]);
    	                this.dydx2[i][j] = (this.y[i][jjp] - this.y[i][jjm])/(this.x2[jjp] - this.x2[jjm]);
    	                this.d2ydx1dx2[i][j] = (this.y[iip][jjp] - this.y[iip][jjm] - this.y[iim][jjp] + this.y[iim][jjm])/((this.x1[iip] - this.x1[iim])*(this.x2[jjp] - this.x2[jjm]));
                    }
                }
            }

	    	this.derivCalculated = true;
    	}

    	// Grid coefficients
    	private void gridCoefficients(){

    	    double[] yt = new double[4];
    	    double[] dydx1t = new double[4];
    	    double[] dydx2t = new double[4];
    	    double[] d2ydx1dx2t = new double[4];
    	    double[] ct = new double[16];
    	    double[] xt = new double[16];
    	    double d1 = 0.0;
    	    double d2 = 0.0;
    	    for(int i=0; i<this.nPoints-1; i++){
    	        d1 = this.x1[i+1] - this.x1[i];
    	        for(int j=0; j<this.mPoints-1; j++){

    	            // Calculate grid coefficients for 4-point grid square with point i,j at the top left corner
                    double[][] cc = new double[4][4];
    	            d2 = this.x2[j] - this.x2[j+1];
    	            coeff.add(new Double(d1));
    	            coeff.add(new Double(this.x1[i]));
    	            coeff.add(new Double(d2));
    	            coeff.add(new Double(this.x2[j+1]));
    	            yt[0] = this.y[i][j+1];
    	            dydx1t[0] = this.dydx1[i][j+1];
    	            dydx2t[0] = this.dydx2[i][j+1];
    	            d2ydx1dx2t[0] = this.d2ydx1dx2[i][j+1];
    	            yt[1] = this.y[i+1][j+1];
    	            dydx1t[1] = this.dydx1[i+1][j+1];
    	            dydx2t[1] = this.dydx2[i+1][j+1];
    	            d2ydx1dx2t[1] = this.d2ydx1dx2[i+1][j+1];
    	            yt[2] = this.y[i+1][j];
    	            dydx1t[2] = this.dydx1[i+1][j];
    	            dydx2t[2] = this.dydx2[i+1][j];
    	            d2ydx1dx2t[2] = this.d2ydx1dx2[i+1][j];
    	            yt[3] = this.y[i][j];
    	            dydx1t[3] = this.dydx1[i][j];
    	            dydx2t[3] = this.dydx2[i][j];
    	            d2ydx1dx2t[3] = this.d2ydx1dx2[i][j];

    	            for(int k=0; k<4; k++){
    	                xt[k] = yt[k];
    	                xt[k+4] = dydx1t[k]*d1;
    	                xt[k+8] = dydx2t[k]*d2;
    	                xt[k+12] = d2ydx1dx2t[k]*d1*d2;
    	            }

                    double xh = 0.0;
    	            for(int k=0; k<16; k++){
    	                for(int kk=0; kk<16; kk++){
    	                    xh += this.weights[k][kk]*xt[kk];
    	                }
    	                ct[k] = xh;
    	                xh = 0.0;
    	            }

    	            int counter = 0;
    	            for(int k=0; k<4; k++){
    	                for(int kk=0; kk<4; kk++){
    	                    cc[k][kk] = ct[counter++];
    	                }
    	            }

    	            // Add grid coefficient 4x4 array to ArrayList
    	            coeff.add(cc);
    	        }
    	    }
    	}

    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x1,x2)
    	public double interpolate(double xx1, double xx2){
    	    // check that xx1 and xx2 are within the limits
    	    if(xx1<x1[0]){
    	        if(xx1>=x1[0]-BiCubicInterpolation.potentialRoundingError){
    	            xx1=this.x1[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx1 + " is outside the limits, " + x1[0] + " - " + x1[this.nPoints-1]);
    	        }
    	    }
    	    if(xx2<this.x2[this.mPoints-1]){
    	        if(xx2>this.x2[this.mPoints-1]-BiCubicInterpolation.potentialRoundingError){
    	            xx2=this.x2[this.mPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx2 + " is outside the limits, " + this.x2[this.mPoints-1] + " - " + this.x2[0]);
    	        }
    	    }
    	    if(xx1>this.x1[this.nPoints-1]){
    	        if(xx1<=this.x1[this.nPoints-1]+BiCubicInterpolation.potentialRoundingError){
    	            xx1=this.x1[this.nPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx1 + " is outside the limits, " + this.x1[0] + " - " + this.x1[this.nPoints-1]);
    	        }
    	    }
    	    if(xx2>this.x2[0]){
    	        if(xx2<=this.x2[0]+BiCubicInterpolation.potentialRoundingError){
    	            xx2=this.x2[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx2 + " is outside the limits, " + this.x2[this.nPoints-1] + " - " + this.x2[0]);
    	        }
    	    }

    	    // assign variables
    	    this.xx1 = xx1;
    	    this.xx2 = xx2;

            // Find grid surrounding the interpolation point
            int grid1 = 0;
            int grid2 = 0;
            int counter = 1;
            boolean test = true;
            while(test){
                if(xx1<this.x1[counter]){
                    grid1 = counter - 1;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=this.nPoints){
                        grid1 = this.nPoints-2;
                        test = false;
                    }
                }
            }
            counter = 0;
            test = true;
            while(test){
                if(xx2>=this.x2[counter+1] && xx2<=this.x2[counter]){
                    grid2 = counter;
                    test = false;
                }
                else{
                    counter++;
                }
            }
            int gridn = grid1*(this.mPoints-1) + grid2;

            // grid details
            double distance1  = ((Double)coeff.get(5*gridn)).doubleValue();
            double x1lower  = ((Double)coeff.get(5*gridn+1)).doubleValue();
            double distance2  = ((Double)coeff.get(5*gridn+2)).doubleValue();
            double x2lower  = ((Double)coeff.get(5*gridn+3)).doubleValue();
            double[][] gCoeff = (double[][])coeff.get(5*gridn+4);
            double x1Normalised = (xx1 - x1lower)/distance1;
            double x2Normalised = (xx2 - x2lower)/distance2;

            // interpolation
            this.interpolatedValue = 0.0;           // interpolated value of y
            for(int i=0; i<4; i++){
                for(int j=0; j<4; j++){
                    this.interpolatedValue += gCoeff[i][j]*Math.pow(x1Normalised, i)*Math.pow(x2Normalised, j);
                 }
            }
            this.interpolatedDydx1 = 0.0;           // interpolated value of dy/dx1
            for(int i=1; i<4; i++){
                for(int j=0; j<4; j++){
                    this.interpolatedDydx1 += i*gCoeff[i][j]*Math.pow(x1Normalised, i-1)*Math.pow(x2Normalised, j);
                }
            }
    	    this.interpolatedDydx2 = 0.0;           // interpolated value of dydx2
            for(int i=0; i<4; i++){
                for(int j=1; j<4; j++){
                    this.interpolatedDydx2 += j*gCoeff[i][j]*Math.pow(x1Normalised, i)*Math.pow(x2Normalised, j-1);
                }
            }
    	    this.interpolatedD2ydx1dx2 = 0.0;       // interpolated value of d2y/dx1dx2
            for(int i=1; i<4; i++){
                for(int j=1; j<4; j++){
                    this.interpolatedD2ydx1dx2 += i*j*gCoeff[i][j]*Math.pow(x1Normalised, i-1)*Math.pow(x2Normalised, j-1);
                }
            }

            return this.interpolatedValue;
        }

        // Return last interpolated value and the interpolated gradients
        public double[] getInterpolatedValues(){
            double[] ret = new double[6];
            ret[0] = this.interpolatedValue;
            ret[1] = this.interpolatedDydx1;
            ret[2] = this.interpolatedDydx2;
            ret[3] = this.interpolatedD2ydx1dx2;
            ret[4] = this.xx1;
            ret[5] = this.xx2;
            return ret;
        }

        // Return grid point values of dydx1
        public double[][] getGridDydx1(){
            double[][] ret = new double[this.nPoints][this.mPoints];
            for(int i=0; i<this.nPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    ret[this.x1indices[i]][this.x2indices[j]] = this.dydx1[i][j];
                }
            }
            return ret;
        }

        // Return grid point values of dydx2
        public double[][] getGridDydx2(){
            double[][] ret = new double[this.nPoints][this.mPoints];
            for(int i=0; i<this.nPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    ret[this.x1indices[i]][this.x2indices[j]] = this.dydx2[i][j];
                }
            }
            return ret;
        }

        // Return grid point values of d2ydx1dx2
        public double[][] getGridD2ydx1dx2(){
            double[][] ret = new double[this.nPoints][this.mPoints];
            for(int i=0; i<this.nPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    ret[this.x1indices[i]][this.x2indices[j]] = this.d2ydx1dx2[i][j];
                }
            }
            return ret;
        }

        // Reset the numerical differentiation incremental factor delta
    	public static void resetDelta(double delta){
            BiCubicInterpolation.delta = delta;
        }

    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            BiCubicInterpolation.roundingCheck = false;
            BiCubicInterpolation.potentialRoundingError = 0.0;
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            BiCubicInterpolation.potentialRoundingError = potentialRoundingError;
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

}

