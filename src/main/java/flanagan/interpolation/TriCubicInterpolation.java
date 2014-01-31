/**********************************************************
*
*   TriCubicInterpolation.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2,x3) using a tricubic interploation procedure
**
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	12-15 January 2011
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/TriCubicInterpolation.html
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
import flanagan.math.Matrix;
import flanagan.math.ArrayMaths;

public class TriCubicInterpolation{

        // unit cube: front face anticlockwise then backface anticlockwise from bottom left corner
        int[][] unitCube = {{0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};

    	private int lPoints = 0;   	                            // no. of x1 tabulated points
    	private int mPoints = 0;   	                            // no. of x2 tabulated points
    	private int nPoints = 0;   	                            // no. of x3 tabulated points
    	private double[] x1 = null;   	                        // x1 in tabulated function f(x1,x2,x3)
    	private double[] x2 = null;                             // x2 in tabulated function f(x1,x2,x3)
    	private double[] x3 = null;                             // x3 in tabulated function f(x1,x2,x3)
    	private double[][][] y = null;  	                    // y=f(x1,x2,x3) tabulated function

    	private double[][][] dydx1 = null;  	                // dy/dx1
    	private double[][][] dydx2 = null;  	                // dy/dx2
    	private double[][][] dydx3 = null;  	                // dy/dx3
    	private double[][][] d2ydx1dx2 = null;  	            // d2y/dx1dx2
    	private double[][][] d2ydx1dx3 = null;  	            // d2y/dx1dx3
    	private double[][][] d2ydx2dx3 = null;  	            // d2y/dx2dx3
    	private double[][][] d3ydx1dx2dx3 = null;  	            // d3y/dx1dx2dx3
        private boolean derivCalculated = false;                // = true when the derivatives have been calculated or entered
        private TriCubicSpline tcs = null;                      // TriCubic spline used in calculating the derivatives
        private double incrX1 = 0;                              // x1 increment used in calculating the derivatives
        private double incrX2 = 0;                              // x2 increment used in calculating the derivatives
        private double incrX3 = 0;                              // x3 increment used in calculating the derivatives

    	private double xx1 = Double.NaN;                        // value of x1 at which an interpolated y value is required
    	private double xx2 = Double.NaN;                        // value of x2 at which an interpolated y value is required
    	private double xx3 = Double.NaN;                        // value of x3 at which an interpolated y value is required

        private ArrayList<Object> coeff = new ArrayList<Object>();  // grid cube coefficients

        // Weights used in calculating the grid cube coefficients
        private double[][] weights = new double[64][64];

    	private int[] x1indices = null;                         // x1 data indices before ordering
    	private int[] x2indices = null;                         // x2 data indices before ordering
    	private int[] x3indices = null;                         // x3 data indices before ordering

    	private double[] xMin = new double[3];                  // minimum values of x1, x2 and x3
    	private double[] xMax = new double[3];                  // maximum values of x1, x2 and x3

    	private double interpolatedValue = Double.NaN;          // interpolated value of y
    	private double interpolatedDydx1 = Double.NaN;          // interpolated value of dydx1
    	private double interpolatedDydx2 = Double.NaN;          // interpolated value of dydx2
    	private double interpolatedDydx3 = Double.NaN;          // interpolated value of dydx3
    	private double interpolatedD2ydx1dx2 = Double.NaN;      // interpolated value of d2ydx1dx2
    	private double interpolatedD2ydx1dx3 = Double.NaN;      // interpolated value of d2ydx1dx3
    	private double interpolatedD2ydx2dx3 = Double.NaN;      // interpolated value of d2ydx2dx3
    	private double interpolatedD3ydx1dx2dx3 = Double.NaN;   // interpolated value of d3ydx1dx2d3

        private boolean numerDiffFlag = true;                   // = true:  if numerical differentiation performed h1 and h2 calculated using delta
                                                                // = false: if numerical differentiation performed h1 and h2 calculated only provided data points

        private static double delta = 1e-3;                     // fractional step factor used in calculating the derivatives
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = false;           // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)


    	// Constructor without derivatives
    	// numerDiffOption = 0 -> numerical differencing using only supplied data points
    	// numerDiffOption = 1 -> numerical differencing using interpolation
    	public TriCubicInterpolation(double[] x1, double[] x2, double[] x3, double[][][] y, int numerDiffOption){
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
            this.initialize(Conv.copy(x1), Conv.copy(x2),  Conv.copy(x3), Conv.copy(y));

        	// calculate the derivatives
        	this.calcDeriv();

        	// calculate grid coefficients for all grid cubes
        	this.gridCoefficients();
    	}

    	// Constructor with derivatives
    	public TriCubicInterpolation(double[] x1, double[] x2, double[] x3, double[][][] y, double[][][] dydx1, double[][][] dydx2, double[][][] dydx3, double[][][] d2ydx1dx2, double[][][] d2ydx1dx3, double[][][] d2ydx2dx3, double[][][] d3ydx1dx2dx3){

      	    // initialize the data
            this.initialize(Conv.copy(x1), Conv.copy(x2), Conv.copy(x3), Conv.copy(y), Conv.copy(dydx1), Conv.copy(dydx2), Conv.copy(dydx3), Conv.copy(d2ydx1dx2), Conv.copy(d2ydx1dx3), Conv.copy(d2ydx2dx3), Conv.copy(d3ydx1dx2dx3));

        	// calculate grid coefficients for all grid cubes
        	this.gridCoefficients();
    	}

    	// Initialize the data
    	private void initialize(double[] x1, double[] x2, double[] x3, double[][][] y){
    	    this.initialize(x1, x2, x3, y, null, null, null, null, null, null, null, false);
    	}

    	private void initialize(double[] x1, double[] x2, double[] x3, double[][][] y, double[][][] dydx1, double[][][] dydx2, double[][][] dyd3, double[][][] d2ydx1dx2, double[][][] d2ydx1dx3, double[][][] d2ydx2dx3, double[][][] d3ydx1dx2dx3){

    	    this.initialize(x1, x2, x3, y, dydx1, dydx2, dydx3, d2ydx1dx2, d2ydx1dx3,  d2ydx2dx3,  d3ydx1dx2dx3, true);
    	}

    	private void initialize(double[] x1, double[] x2, double[] x3, double[][][] y, double[][][] dydx1, double[][][] dydx2, double[][][] dyd3, double[][][] d2ydx1dx2, double[][][] d2ydx1dx3, double[][][] d2ydx2dx3, double[][][] d3ydx1dx2dx3, boolean flag){
         	int lPoints=x1.length;
        	int mPoints=x2.length;
        	int nPoints=x3.length;
        	if(lPoints!=y.length)throw new IllegalArgumentException("Array x1 and y-row are of different length " + lPoints + " " + y.length);
        	if(mPoints!=y[0].length)throw new IllegalArgumentException("Array x2 and y-column are of different length "+ mPoints + " " + y[0].length);
        	if(nPoints!=y[0][0].length)throw new IllegalArgumentException("Array x3 and y-column are of different length "+ nPoints + " " + y[0][0].length);
          	if(lPoints<2 || mPoints<2 || nPoints<2 )throw new IllegalArgumentException("The data matrix must have a minimum size of 2 X 2 X 2");

            // Calculate weighting matrix
            this.calcWeights();

            // order data
            ArrayMaths am = new ArrayMaths(x1);
            am = am.sort();
            this.x1indices = am.originalIndices();
            x1 = am.array();
            double[][][] hold = new double[lPoints][mPoints][nPoints];
            double[][][] hold1 = null;
            double[][][] hold2 = null;
            double[][][] hold12 = null;

            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                hold[i][j][k] = y[this.x1indices[i]][j][k];
    	            }
    	        }
    	    }
            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                y[i][j][k] = hold[i][j][k];
    	            }
    	        }
    	    }

    	    if(flag){
                hold1 = new double[lPoints][mPoints][nPoints];
                hold2 = new double[lPoints][mPoints][nPoints];
                hold12 = new double[lPoints][mPoints][nPoints];
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    hold1[i][j][k] = dydx1[this.x1indices[i]][j][k];
    	                    hold2[i][j][k] = dydx2[this.x1indices[i]][j][k];
    	                    hold12[i][j][k] = d2ydx1dx2[this.x1indices[i]][j][k];
    	                }
    	            }
    	        }
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    dydx1[i][j][k] = hold1[i][j][k];
    	                    dydx2[i][j][k] = hold2[i][j][k];
    	                    d2ydx1dx2[i][j][k] = hold12[i][j][k];
    	                }
    	            }
    	        }
            }

    	    am = new ArrayMaths(x2);
            am = am.sort();
            this.x2indices = am.originalIndices();
            x2 = am.array();

            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                hold[i][j][k] = y[i][this.x2indices[j]][k];
    	            }
    	        }
    	    }
            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                y[i][j][k] = hold[i][j][k];
    	            }
    	        }
    	    }

    	    if(flag){
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    hold1[i][j][k] = dydx1[i][this.x2indices[j]][k];
    	                    hold2[i][j][k] = dydx2[i][this.x2indices[j]][k];
    	                    hold12[i][j][k] = d2ydx1dx2[i][this.x2indices[j]][k];
    	                }
    	            }
    	        }
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    dydx1[i][j][k] = hold1[i][j][k];
    	                    dydx2[i][j][k] = hold2[i][j][k];
    	                    d2ydx1dx2[i][j][k] = hold12[i][j][k];
    	                }
    	            }
    	        }
    	    }

            am = new ArrayMaths(x3);
            am = am.sort();
            this.x3indices = am.originalIndices();
            x3 = am.array();

            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                hold[i][j][k] = y[i][j][this.x3indices[k]];
    	            }
    	        }
    	    }
            for(int i=0; i<lPoints; i++){
    	        for(int j=0; j<mPoints; j++){
    	            for(int k=0; k<nPoints; k++){
    	                y[i][j][k] = hold[i][j][k];
    	            }
    	        }
    	    }

    	    if(flag){
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    hold1[i][j][k] = dydx1[i][j][this.x3indices[k]];
    	                    hold2[i][j][k] = dydx2[i][j][this.x3indices[k]];
    	                    hold12[i][j][k] = d2ydx1dx2[i][j][this.x3indices[k]];
    	                }
    	            }
    	        }
                for(int i=0; i<lPoints; i++){
    	            for(int j=0; j<mPoints; j++){
    	                for(int k=0; k<nPoints; k++){
    	                    dydx1[i][j][k] = hold1[i][j][k];
    	                    dydx2[i][j][k] = hold2[i][j][k];
    	                    d2ydx1dx2[i][j][k] = hold12[i][j][k];
    	                }
    	            }
    	        }
    	    }

    	    // check for identical x1 values
    	    for(int i=1; i<lPoints; i++){
    	        if(x1[i]==x1[i-1]){
    	            System.out.println("x1["+this.x1indices[i]+"] and x1["+this.x1indices[i+1]+"] are identical, " +  x1[i]);
    	            double sep = (Fmath.maximum(x1) - Fmath.minimum(x1))/0.5e-3;
    	            x1[i-1] -= sep;
    	            x1[i]+= sep;
    	            System.out.println("They have been separated by" + 2*sep);
    	        }
    	    }

    	    // check for identical x2 values
    	    for(int i=1; i<mPoints; i++){
    	        if(x2[i]==x2[i-1]){
    	            System.out.println("x2["+this.x2indices[i]+"] and x2["+this.x2indices[i+1]+"] are identical, " +  x2[i]);
    	            double sep = (Fmath.maximum(x2) - Fmath.minimum(x2))/0.5e-3;
    	            x2[i-1] -= sep;
    	            x2[i]+= sep;
    	            System.out.println("They have been separated by" + 2*sep);
    	        }
    	    }

    	    // check for identical x3 values
    	    for(int i=1; i<nPoints; i++){
    	        if(x3[i]==x3[i-1]){
    	            System.out.println("x3["+this.x3indices[i]+"] and x3["+this.x3indices[i+1]+"] are identical, " +  x3[i]);
    	            double sep = (Fmath.maximum(x3) - Fmath.minimum(x3))/0.5e-3;
    	            x3[i-1] -= sep;
    	            x3[i]+= sep;
    	            System.out.println("They have been separated by" + 2*sep);
    	        }
    	    }

    	    // assign variables
    	    this.lPoints = lPoints;
    	    this.mPoints = mPoints;
    	    this.nPoints = nPoints;
    	    this.x1 = new double[this.lPoints];
        	this.x2 = new double[this.mPoints];
        	this.x3 = new double[this.nPoints];
        	this.y = new double[this.lPoints][this.mPoints][this.nPoints];
    	    this.dydx1 = new double[this.lPoints][this.mPoints][this.nPoints];
    	    this.dydx2 = new double[this.lPoints][this.mPoints][this.nPoints];
    	    this.dydx3 = new double[this.lPoints][this.mPoints][this.nPoints];
    	    this.d2ydx1dx2 = new double[this.lPoints][this.mPoints][this.nPoints];
    	    this.d2ydx1dx3 = new double[this.lPoints][this.mPoints][this.nPoints];
            this.d2ydx2dx3 = new double[this.lPoints][this.mPoints][this.nPoints];
            this.d3ydx1dx2dx3 = new double[this.lPoints][this.mPoints][this.nPoints];

        	for(int i=0; i<this.lPoints; i++){
            	this.x1[i]=x1[i];
        	}
        	for(int j=0; j<this.mPoints; j++){
            	this.x2[j]=x2[j];
        	}
        	for(int k=0; k<this.nPoints; k++){
            	this.x3[k]=x3[k];
        	}
        	for(int i =0; i<this.lPoints; i++){
            	for(int j=0; j<this.mPoints; j++){
            	    for(int k=0; k<this.nPoints; k++){
                	    this.y[i][j][k]=y[i][j][k];
            		}
                }
        	}

        	if(flag){
        	    for(int i =0; i<this.lPoints; i++){
            	    for(int j=0; j<this.mPoints; j++){
            	        for(int k=0; k<this.nPoints; k++){
                            this.dydx1[i][j][k]=dydx1[i][j][k];
                            this.dydx2[i][j][k]=dydx2[i][j][k];
                            this.dydx3[i][j][k]=dydx3[i][j][k];
                            this.d2ydx1dx2[i][j][k]=d2ydx1dx2[i][j][k];
                            this.d2ydx1dx3[i][j][k]=d2ydx1dx3[i][j][k];
                            this.d2ydx2dx3[i][j][k]=d2ydx2dx3[i][j][k];
                            this.d3ydx1dx2dx3[i][j][k]=d3ydx1dx2dx3[i][j][k];
            		    }
                    }
        	    }
        	    this.derivCalculated = true;
        	}

	        // limits
        	this.xMin[0] = Fmath.minimum(this.x1);
        	this.xMax[0] = Fmath.maximum(this.x1);
        	this.xMin[1] = Fmath.minimum(this.x2);
        	this.xMax[1] = Fmath.maximum(this.x2);
        	this.xMin[2] = Fmath.minimum(this.x3);
        	this.xMax[2] = Fmath.maximum(this.x3);


            if(!flag && this.numerDiffFlag){
        	    // numerical difference increments
        	    double range1 = this.xMax[0] - this.xMin[0];
        	    double range2 = this.xMax[1] - this.xMin[1];
        	    double range3 = this.xMax[2] - this.xMin[2];
        	    double averageSeparation1 = range1/this.lPoints;
        	    double averageSeparation2 = range2/this.mPoints;
        	    double averageSeparation3 = range3/this.nPoints;

        	    double minSep = this.x1[1] - this.x1[0];
        	    double minimumSeparation1 = minSep;
        	    for(int i=2; i<this.lPoints; i++){
        	        minSep = this.x1[i] - this.x1[i-1];
        	        if(minSep<minimumSeparation1)minimumSeparation1 = minSep;
        	    }
        	    minSep = this.x2[1] - this.x2[0];
        	    double minimumSeparation2 = minSep;
        	    for(int i=2; i<this.mPoints; i++){
        	        minSep = this.x2[i] - this.x2[i-1];
        	        if(minSep<minimumSeparation2)minimumSeparation2 = minSep;
        	    }
        	    minSep = this.x3[1] - this.x3[0];
        	    double minimumSeparation3 = minSep;
        	    for(int i=2; i<this.nPoints; i++){
        	        minSep = this.x3[i] - this.x3[i-1];
        	        if(minSep<minimumSeparation3)minimumSeparation3 = minSep;
        	    }

                this.incrX1 = range1*TriCubicInterpolation.delta;
                double defaultIncr = minimumSeparation1;
                if(minimumSeparation1<averageSeparation1/10.0)defaultIncr = averageSeparation1/10.0;
                if(this.incrX1>averageSeparation1)this.incrX1 = defaultIncr;
                this.incrX2 = range2*TriCubicInterpolation.delta;
                defaultIncr = minimumSeparation2;
                if(minimumSeparation2<averageSeparation2/10.0)defaultIncr = averageSeparation2/10.0;
                if(this.incrX2>averageSeparation2)this.incrX2 = defaultIncr;
                this.incrX3 = range3*TriCubicInterpolation.delta;
                defaultIncr = minimumSeparation3;
                if(minimumSeparation3<averageSeparation3/10.0)defaultIncr = averageSeparation3/10.0;
                if(this.incrX3>averageSeparation3)this.incrX3 = defaultIncr;
            }
        }


        // Calculate the weighting matrix
        private void calcWeights(){
            int kk = 0;
            // substitute unit cube corners in y
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            this.weights[kk][n] = Math.pow(this.unitCube[m][0], i)* Math.pow(this.unitCube[m][1], j)*Math.pow(this.unitCube[m][2],k);
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in dy/dx1
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(i==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = i*Math.pow(this.unitCube[m][0], i-1)* Math.pow(this.unitCube[m][1], j)*Math.pow(this.unitCube[m][2], k);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in dy/dx2
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(j==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = j*Math.pow(this.unitCube[m][0], i)* Math.pow(this.unitCube[m][1], j-1)*Math.pow(this.unitCube[m][2], k);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in dy/dx3
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(k==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = k*Math.pow(this.unitCube[m][0], i)* Math.pow(this.unitCube[m][1], j)*Math.pow(this.unitCube[m][2], k-1);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in d2y/dx1dx2
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(i==0 || j==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = i*j*Math.pow(this.unitCube[m][0], i-1)* Math.pow(this.unitCube[m][1], j-1)* Math.pow(this.unitCube[m][2], k);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in d2y/dx1dx3
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(i==0 || k==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = i*k*Math.pow(this.unitCube[m][0], i-1)* Math.pow(this.unitCube[m][1], j)* Math.pow(this.unitCube[m][2], k-1);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in d2y/dx2dx3
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(j==0 || k==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = j*k*Math.pow(this.unitCube[m][0], i)* Math.pow(this.unitCube[m][1], j-1)* Math.pow(this.unitCube[m][2], k-1);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }
            // substitute unit cube corners in d3y/dx1dx2dx3
            for(int m=0; m<8; m++){
                int n = 0;
                for(int i=0; i<4; i++){
                    for(int j=0; j<4; j++){
                        for(int k=0; k<4; k++){
                            if(i==0 || j==0 || k==0){
                                this.weights[kk][n] = 0.0;
                            }
                            else{
                                this.weights[kk][n] = i*j*k*Math.pow(this.unitCube[m][0], i-1)* Math.pow(this.unitCube[m][1], j-1)* Math.pow(this.unitCube[m][2], k-1);
                            }
                            n++;
                        }
                    }
                }
                kk++;
            }

            // invert the above calculated matrix
            Matrix mat = new Matrix(this.weights);
            mat = mat.inverse();
            this.weights = mat.getArrayCopy();
        }


        // Calculate the derivatives
        private void calcDeriv(){

            if(this.numerDiffFlag){

                // Numerical differentiation using delta and interpolation
                this.tcs = new TriCubicSpline(this.x1, this.x2, this.x3, this.y);

    	        double[] x1jp1 = new double[this.lPoints];
    	        double[] x1jm1 = new double[this.lPoints];
    	        double[] x2jp1 = new double[this.mPoints];
    	        double[] x2jm1 = new double[this.mPoints];
    	        double[] x3jp1 = new double[this.nPoints];
    	        double[] x3jm1 = new double[this.nPoints];

    	        for(int i=0; i<this.lPoints; i++){
    	            x1jp1[i] = this.x1[i] + this.incrX1;
    	            if(x1jp1[i]>this.x1[this.lPoints-1])x1jp1[i]=this.x1[this.lPoints-1];
    	            x1jm1[i] = this.x1[i] - this.incrX1;
    	            if(x1jm1[i]<this.x1[0])x1jm1[i]=this.x1[0];
    	        }
    	        for(int i=0; i<this.mPoints; i++){
    	            x2jp1[i] = this.x2[i] + this.incrX2;
    	            if(x2jp1[i]>this.x2[this.mPoints-1])x2jp1[i]=this.x2[this.mPoints-1];
    	            x2jm1[i] = this.x2[i] - this.incrX2;
    	            if(x2jm1[i]<this.x2[0])x2jm1[i]=this.x2[0];
    	        }
    	        for(int i=0; i<this.nPoints; i++){
    	            x3jp1[i] = this.x3[i] + this.incrX3;
    	            if(x3jp1[i]>this.x3[this.nPoints-1])x3jp1[i]=this.x3[this.nPoints-1];
    	            x3jm1[i] = this.x3[i] - this.incrX3;
    	            if(x3jm1[i]<this.x3[0])x3jm1[i]=this.x3[0];
    	        }

    	        for(int i=0; i<this.lPoints; i++){
    	            for(int j=0; j<this.mPoints; j++){
    	                for(int k=0; k<this.nPoints; k++){
    	                    this.dydx1[i][j][k] = (tcs.interpolate(x1jp1[i],x2[j],x3[k]) - tcs.interpolate(x1jm1[i],x2[j],x3[k]))/(x1jp1[i] - x1jm1[i]);
    	                    this.dydx2[i][j][k] = (tcs.interpolate(x1[i],x2jp1[j],x3[k]) - tcs.interpolate(x1[i],x2jm1[j],x3[k]))/(x2jp1[j] - x2jm1[j]);
    	                    this.dydx3[i][j][k] = (tcs.interpolate(x1[i],x2[j],x3jp1[k]) - tcs.interpolate(x1[i],x2[j],x3jm1[k]))/(x3jp1[k] - x3jm1[k]);
    	                    this.d2ydx1dx2[i][j][k] = (tcs.interpolate(x1jp1[i],x2jp1[j],x3[k]) - tcs.interpolate(x1jp1[i],x2jm1[j],x3[k]) - tcs.interpolate(x1jm1[i],x2jp1[j],x3[k]) + tcs.interpolate(x1jm1[i],x2jm1[j],x3[k]))/((x1jp1[i] - x1jm1[i])*(x2jp1[j] - x2jm1[j]));
    	                    this.d2ydx1dx3[i][j][k] = (tcs.interpolate(x1jp1[i],x2[j],x3jp1[k]) - tcs.interpolate(x1jp1[i],x2[j],x3jm1[k]) - tcs.interpolate(x1jm1[i],x2[j],x3jp1[k]) + tcs.interpolate(x1jm1[i],x2[j],x3jm1[k]))/((x1jp1[i] - x1jm1[i])*(x3jp1[k] - x3jm1[k]));
                       	    this.d2ydx2dx3[i][j][k] = (tcs.interpolate(x1[i],x2jp1[j],x3jp1[k]) - tcs.interpolate(x1[i],x2jp1[j],x3jm1[k]) - tcs.interpolate(x1[i],x2jm1[j],x3jp1[k]) + tcs.interpolate(x1[i],x2jm1[j],x3jm1[k]))/((x2jp1[j] - x2jm1[j])*(x3jp1[k] - x3jm1[k]));
                            this.d3ydx1dx2dx3[i][j][k] = ((tcs.interpolate(x1jp1[i],x2jp1[j],x3jp1[k]) - tcs.interpolate(x1jp1[i],x2jm1[j],x3jp1[k]) - tcs.interpolate(x1jm1[i],x2jp1[j],x3jp1[k]) + tcs.interpolate(x1jm1[i],x2jm1[j],x3jp1[k])) - (tcs.interpolate(x1jp1[i],x2jp1[j],x3jm1[k]) - tcs.interpolate(x1jp1[i],x2jm1[j],x3jm1[k]) - tcs.interpolate(x1jm1[i],x2jp1[j],x3jm1[k]) + tcs.interpolate(x1jm1[i],x2jm1[j],x3jm1[k])))/((x1jp1[i] - x1jm1[i])*(x2jp1[j] - x2jm1[j])*(x3jp1[k] - x3jm1[k]));
                        }
                    }
                }
            }
            else{
                // Numerical differentiation using only provided data points
                int iip = 0;
                int iim = 0;
                int jjp = 0;
                int jjm = 0;
                int kkp = 0;
                int kkm = 0;
                for(int i=0; i<this.lPoints; i++){
                    iip = i+1;
    	            if(iip>=this.lPoints)iip = this.lPoints-1;
    	            iim = i-1;
    	            if(iim<0)iim = 0;
    	            for(int j=0; j<this.mPoints; j++){
    	                jjp = j+1;
    	                if(jjp>=this.mPoints)jjp = this.mPoints-1;
    	                jjm = j-1;
    	                if(jjm<0)jjm = 0;
    	                for(int k=0; k<this.nPoints; k++){
    	                    kkp = k+1;
    	                    if(kkp>=this.nPoints)kkp = this.nPoints-1;
            	            kkm = k-1;
    	                    if(kkm<0)kkm = 0;
    	                    this.dydx1[i][j][k] = (this.y[iip][j][k] - this.y[iim][j][k])/(this.x1[iip] - this.x1[iim]);
    	                    this.dydx2[i][j][k] = (this.y[i][jjp][k] - this.y[i][jjm][k])/(this.x2[jjp] - this.x2[jjm]);
    	                    this.dydx3[i][j][k] = (this.y[i][j][kkp] - this.y[i][j][kkm])/(this.x3[kkp] - this.x3[kkm]);
                            this.d2ydx1dx2[i][j][k] = (this.y[iip][jjp][k] - this.y[iip][jjm][k] - this.y[iim][jjp][k] + this.y[iim][jjm][k])/((this.x1[iip] - this.x1[iim])*(this.x2[jjp] - this.x2[jjm]));
                            this.d2ydx1dx3[i][j][k] = (this.y[iip][j][kkp] - this.y[iip][j][kkm] - this.y[iim][j][kkp] + this.y[iim][j][kkm])/((this.x1[iip] - this.x1[iim])*(this.x3[kkp] - this.x3[kkm]));
                            this.d2ydx2dx3[i][j][k] = (this.y[i][jjp][kkp] - this.y[i][jjp][kkm] - this.y[i][jjm][kkp] + this.y[i][jjm][kkm])/((this.x2[jjp] - this.x2[jjm])*(this.x3[kkp] - this.x3[kkm]));
                            this.d2ydx1dx2[i][j][k] = (this.y[iip][jjp][kkp] - this.y[iip][jjm][kkp] - this.y[iim][jjp][kkp] + this.y[iim][jjm][kkp] - this.y[iip][jjp][kkm] + this.y[iip][jjm][kkm] + this.y[iim][jjp][kkm] - this.y[iim][jjm][kkm])/((this.x1[iip] - this.x1[iim])*(this.x2[jjp] - this.x2[jjm])*(this.x3[kkp] - this.x3[kkm]));
                        }
                    }
                }
            }

	    	this.derivCalculated = true;
    	}

    	// Grid coefficients
    	private void gridCoefficients(){

    	    double[] yt = new double[8];
    	    double[] dydx1t = new double[8];
    	    double[] dydx2t = new double[8];
    	    double[] dydx3t = new double[8];
    	    double[] d2ydx1dx2t = new double[8];
    	    double[] d2ydx1dx3t = new double[8];
    	    double[] d2ydx2dx3t = new double[8];
    	    double[] d3ydx1dx2dx3t = new double[8];
    	    double[] ct = new double[64];
    	    double[] xt = new double[64];
    	    double d1 = 0.0;
    	    double d2 = 0.0;
    	    double d3 = 0.0;
    	    for(int i=0; i<this.lPoints-1; i++){
    	        d1 = this.x1[i+1] - this.x1[i];
    	        for(int j=0; j<this.mPoints-1; j++){
    	            d2 = this.x2[j+1] - this.x2[j];
    	            for(int k=0; k<this.nPoints-1; k++){
    	                d3 = this.x3[k+1] - this.x3[k];
                        double[][][] cc = new double[4][4][4];
    	                coeff.add(new Double(d1));
    	                coeff.add(new Double(this.x1[i]));
    	                coeff.add(new Double(d2));
    	                coeff.add(new Double(this.x2[j]));
    	                coeff.add(new Double(d3));
    	                coeff.add(new Double(this.x3[k]));

    	                for(int ii=0; ii<8; ii++){
    	                    yt[ii] = this.y[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    dydx1t[ii] = this.dydx1[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    dydx2t[ii] = this.dydx2[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    dydx3t[ii] = this.dydx3[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    d2ydx1dx2t[ii] = this.d2ydx1dx2[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    d2ydx1dx3t[ii] = this.d2ydx1dx3[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    d2ydx2dx3t[ii] = this.d2ydx2dx3[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                    d3ydx1dx2dx3t[ii] = this.d3ydx1dx2dx3[i+unitCube[ii][0]][j+unitCube[ii][1]][k+unitCube[ii][2]];
    	                }

    	                for(int k2=0; k2<8; k2++){
    	                    xt[k2] = yt[k2];
    	                    xt[k2+8] = dydx1t[k2]*d1;
    	                    xt[k2+16] = dydx2t[k2]*d2;
    	                    xt[k2+24] = dydx3t[k2]*d3;
    	                    xt[k2+32] = d2ydx1dx2t[k2]*d1*d2;
    	                    xt[k2+40] = d2ydx1dx3t[k2]*d1*d3;
    	                    xt[k2+48] = d2ydx2dx3t[k2]*d2*d3;
    	                    xt[k2+56] = d3ydx1dx2dx3t[k2]*d1*d2*d3;
    	                }

                        double xh = 0.0;
    	                for(int k2=0; k2<64; k2++){
    	                    for(int kk=0; kk<64; kk++){
    	                        xh += this.weights[k2][kk]*xt[kk];
    	                    }
    	                    ct[k2] = xh;
    	                    xh = 0.0;
    	                }
    	                int counter = 0;
    	                for(int k2=0; k2<4; k2++){
    	                    for(int kk=0; kk<4; kk++){
    	                        for(int kkk=0; kkk<4; kkk++){
    	                            cc[k2][kk][kkk] = ct[counter++];
    	                        }
    	                    }
    	                }

    	                // Add grid coefficient array to ArrayList
    	                coeff.add(cc);
    	            }
    	        }
    	    }
    	}

    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x1,x2)
    	public double interpolate(double xx1, double xx2, double xx3){
    	    // check that xx1 and xx2 are within the limits
    	    if(xx1<x1[0]){
    	        if(xx1>=x1[0]-TriCubicInterpolation.potentialRoundingError){
    	            xx1=this.x1[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx1 + " is outside the limits, " + x1[0] + " - " + x1[this.lPoints-1]);
    	        }
    	    }
    	    if(xx2<x2[0]){
    	        if(xx2>=x2[0]-TriCubicInterpolation.potentialRoundingError){
    	            xx2=this.x2[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx2 + " is outside the limits, " + x2[0] + " - " + x2[this.mPoints-1]);
    	        }
    	    }
    	    if(xx3<x3[0]){
    	        if(xx3>=x3[0]-TriCubicInterpolation.potentialRoundingError){
    	            xx3=this.x3[0];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx1 + " is outside the limits, " + x3[0] + " - " + x3[this.nPoints-1]);
    	        }
    	    }


    	    if(xx1>this.x1[this.lPoints-1]){
    	        if(xx1<=this.x1[this.lPoints-1]+TriCubicInterpolation.potentialRoundingError){
    	            xx1=this.x1[this.lPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx1 + " is outside the limits, " + this.x1[0] + " - " + this.x1[this.lPoints-1]);
    	        }
    	    }
    	    if(xx2>this.x2[this.mPoints-1]){
    	        if(xx2<=this.x2[this.mPoints-1]+TriCubicInterpolation.potentialRoundingError){
    	            xx2=this.x2[this.mPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx2 + " is outside the limits, " + this.x2[0] + " - " + this.x2[this.mPoints-1]);
    	        }
    	    }
    	    if(xx3>this.x3[this.nPoints-1]){
    	        if(xx3<=this.x3[this.nPoints-1]+TriCubicInterpolation.potentialRoundingError){
    	            xx3=this.x3[this.mPoints-1];
    	        }
    	        else{
    	            throw new IllegalArgumentException(xx3 + " is outside the limits, " + this.x3[0] + " - " + this.x3[this.nPoints-1]);
    	        }
    	    }


    	    // assign variables
    	    this.xx1 = xx1;
    	    this.xx2 = xx2;
    	    this.xx3 = xx3;

            // Find grid surrounding the interpolation point
            int gridn =0;
            double distance1  = ((Double)coeff.get(7*gridn)).doubleValue();
            double x1lower  = ((Double)coeff.get(7*gridn+1)).doubleValue();
            double distance2  = ((Double)coeff.get(7*gridn+2)).doubleValue();
            double x2lower  = ((Double)coeff.get(7*gridn+3)).doubleValue();
            double distance3  = ((Double)coeff.get(7*gridn+4)).doubleValue();
            double x3lower  = ((Double)coeff.get(7*gridn+5)).doubleValue();
            boolean test = true;
            while(test){
                boolean test1 = false;
                boolean test2 = false;
                boolean test3 = false;
                if(xx1>=x1lower && xx1<=(x1lower+distance1))test1=true;
                if(xx2>=x2lower && xx2<=(x2lower+distance2))test2=true;
                if(xx3>=x3lower && xx3<=(x3lower+distance3))test3=true;
                if(test1 && test2 && test3){
                    test = false;
                }
                else{
                    gridn++;
                    distance1  = ((Double)coeff.get(7*gridn)).doubleValue();
                    x1lower  = ((Double)coeff.get(7*gridn+1)).doubleValue();
                    distance2  = ((Double)coeff.get(7*gridn+2)).doubleValue();
                    x2lower  = ((Double)coeff.get(7*gridn+3)).doubleValue();
                    distance3  = ((Double)coeff.get(7*gridn+4)).doubleValue();
                    x3lower  = ((Double)coeff.get(7*gridn+5)).doubleValue();
                }
            }
            double[][][] gCoeff = (double[][][])coeff.get(7*gridn+6);
            double x1Normalised = (xx1 - x1lower)/distance1;
            double x2Normalised = (xx2 - x2lower)/distance2;
            double x3Normalised = (xx3 - x3lower)/distance3;

            // interpolation
            this.interpolatedValue = 0.0;           // interpolated value of y
            for(int i=0; i<4; i++){
                for(int j=0; j<4; j++){
                    for(int k=0; k<4; k++){
                        this.interpolatedValue += gCoeff[i][j][k]*Math.pow(x1Normalised, i)*Math.pow(x2Normalised, j)*Math.pow(x3Normalised, k);
                    }
                 }
            }
            this.interpolatedDydx1 = 0.0;           // interpolated value of dy/dx1
            for(int i=1; i<4; i++){
                for(int j=0; j<4; j++){
                    for(int k=0; k<4; k++){
                        this.interpolatedDydx1 += i*gCoeff[i][j][k]*Math.pow(x1Normalised, i-1)*Math.pow(x2Normalised, j)*Math.pow(x3Normalised, k);
                    }
                }
            }
    	    this.interpolatedDydx2 = 0.0;           // interpolated value of dydx2
            for(int i=0; i<4; i++){
                for(int j=1; j<4; j++){
                    for(int k=0; k<4; k++){
                        this.interpolatedDydx2 += j*gCoeff[i][j][k]*Math.pow(x1Normalised, i)*Math.pow(x2Normalised, j-1)*Math.pow(x3Normalised, k);
                    }
                }
            }
            this.interpolatedDydx3 = 0.0;           // interpolated value of dydx3
            for(int i=0; i<4; i++){
                for(int j=1; j<4; j++){
                    for(int k=0; k<4; k++){
                        this.interpolatedDydx2 += k*gCoeff[i][j][k]*Math.pow(x1Normalised, i)*Math.pow(x2Normalised, j)*Math.pow(x3Normalised, k-1);
                    }
                }
            }
    	    this.interpolatedD2ydx1dx2 = 0.0;       // interpolated value of d2y/dx1dx2
            for(int i=1; i<4; i++){
                for(int j=1; j<4; j++){
                    for(int k=0; k<4; k++){
                        this.interpolatedD2ydx1dx2 += i*j*gCoeff[i][j][k]*Math.pow(x1Normalised, i-1)*Math.pow(x2Normalised, j-1)*Math.pow(x3Normalised, k);
                    }
                }
            }

            return this.interpolatedValue;
        }

        // Return last interpolated value and the interpolated gradients
        public double[] getInterpolatedValues(){
            double[] ret = new double[11];
            ret[0] = this.interpolatedValue;
            ret[1] = this.interpolatedDydx1;
            ret[2] = this.interpolatedDydx2;
            ret[3] = this.interpolatedDydx3;
            ret[4] = this.interpolatedD2ydx1dx2;
            ret[5] = this.interpolatedD2ydx1dx3;
            ret[6] = this.interpolatedD2ydx2dx3;
            ret[7] = this.interpolatedD3ydx1dx2dx3;
            ret[8] = this.xx1;
            ret[9] = this.xx2;
            ret[10] = this.xx3;
            return ret;
        }

        // Return grid point values of dydx1
        public double[][][] getGridDydx1(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.dydx1[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of dydx2
        public double[][][] getGridDydx2(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.dydx2[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of dydx3
        public double[][][] getGridDydx3(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.dydx3[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of d2ydx1dx2
        public double[][][] getGridD2ydx1dx2(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.d2ydx1dx2[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of d2ydx1dx3
        public double[][][] getGridD2ydx1dx3(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.d2ydx1dx3[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of d2ydx2dx3
        public double[][][] getGridD2ydx2dx3(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.d2ydx2dx3[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Return grid point values of d3ydx1dx2dx3
        public double[][][] getGridD3ydx1dx2dx3(){
            double[][][] ret = new double[this.lPoints][this.mPoints][this.nPoints];
            for(int i=0; i<this.lPoints; i++){
                for(int j=0; j<this.mPoints; j++){
                    for(int k=0; k<this.nPoints; k++){
                        ret[this.x1indices[i]][this.x2indices[j]][this.x3indices[k]] = this.d3ydx1dx2dx3[i][j][k];
                    }
                }
            }
            return ret;
        }

        // Reset the numerical differentiation incremental factor delta
    	public static void resetDelta(double delta){
            TriCubicInterpolation.delta = delta;
        }

    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            TriCubicInterpolation.roundingCheck = false;
            TriCubicInterpolation.potentialRoundingError = 0.0;
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            TriCubicInterpolation.potentialRoundingError = potentialRoundingError;
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
    	    double[] limits = {xMin[0], xMax[0], xMin[1], xMax[1], xMin[2], xMax[2]};
    	    return limits;
    	}

    	// Display limits to x
    	public void displayLimits(){
    	    System.out.println(" ");
    	    for(int i=0; i<3; i++){
    	        System.out.println("The limits to the x array x" + (i+1) + " are " + xMin[i] + " and " + xMax[i]);
    	    }
    	    System.out.println(" ");
    	}

}

