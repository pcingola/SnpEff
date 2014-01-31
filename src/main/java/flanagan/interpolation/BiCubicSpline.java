/**********************************************************
*
*   BiCubicSpline.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2) using a natural bicubic spline
*   Assumes second derivatives at end points = 0 (natural spine)
*
*   See BiCubicSplineFast.java for a faster running version
*       (http://www.ee.ucl.ac.uk/~mflanaga/java/BiCubicSplineFast.html)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	May 2002
*   UPDATE: 20 May 2003, 17 February 2006, 27 July 2007, 4 December 2007, 21 September 2008, 31 October 2009, 5 January 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/BiCubicSpline.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2011   Michael Thomas Flanagan
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

public class BiCubicSpline{

    	private int nPoints = 0;   	                            // no. of x1 tabulated points
    	private int mPoints = 0;   	                            // no. of x2 tabulated points
    	private int nPointsT = 0;   	                        // no. of transposed x1 tabulated points
    	private int mPointsT = 0;   	                        // no. of transposed x2 tabulated points
    	private double[][] y = null;  	                        // y=f(x1,x2) tabulated function
    	private double[][] yT = null;  	                        // transposed y=f(x1,x2) tabulated function, i.e. y=f(x2,x1)
    	private double[] x1 = null;   	                        // x1 in tabulated function f(x1,x2)
    	private double[] x2 = null;                             // x2 in tabulated function f(x1,x2)
    	private double xx1 = Double.NaN;                        // value of x1 at which an interpolated y value is required
    	private double xx2 = Double.NaN;                        // value of x2 at which an interpolated y value is required
    	private double[] xMin = new double[2];                  // minimum values of x1 and x2
    	private double[] xMax = new double[2];                  // maximum values of x1 and x2
    	private double[][] d2ydx2inner = null;                  // second derivatives of first called array of cubic splines
    	private double[][] d2ydx2innerT = null;                 // second derivatives of first called transposed array of cubic splines
    	private CubicSpline csn[] = null;                       // nPoints array of CubicSpline instances
    	private CubicSpline csm = null;                         // CubicSpline instance
    	private CubicSpline csnT[] = null;                      // mPoints array of transposed CubicSpline instances
    	private CubicSpline csmT = null;                        // transposed CubicSpline instance
    	private double interpolatedValue = Double.NaN;          // interpolated value for the original 2D matrix
    	private double interpolatedValueTranspose = Double.NaN; // interpolated value for the transposed 2D matrix
    	private double interpolatedValueMean = Double.NaN;      // mean interpolated value
        private boolean derivCalculated = false;                // = true when the first called cubic spline derivatives have been calculated
        private boolean averageIdenticalAbscissae = false;      // if true: the the ordinate values for identical abscissae are averaged
                                                                // If false: the abscissae values are separated by 0.001 of the total abscissae range;
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = true;            // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)


    	// Constructor
    	// Constructor with data arrays initialised to arrays x and y
    	public BiCubicSpline(double[] x1, double[] x2, double[][] y){
        	this.nPoints=x1.length;
        	this.mPoints=x2.length;
        	this.nPointsT=this.mPoints;
        	this.mPointsT=this.nPoints;
        	if(this.nPoints!=y.length)throw new IllegalArgumentException("Arrays x1 and y-row are of different length " + this.nPoints + " " + y.length);
        	if(this.mPoints!=y[0].length)throw new IllegalArgumentException("Arrays x2 and y-column are of different length "+ this.mPoints + " " + y[0].length);
          	if(this.nPoints<3 || this.mPoints<3)throw new IllegalArgumentException("The data matrix must have a minimum size of 3 X 3");

        	this.csm = new CubicSpline(this.nPoints);
        	this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
        	this.csmT = new CubicSpline(this.mPoints);
        	this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);
        	this.x1 = new double[this.nPoints];
        	this.x2 = new double[this.mPoints];
        	this.y = new double[this.nPoints][this.mPoints];
        	this.yT = new double[this.nPointsT][this.mPointsT];
        	this.d2ydx2inner = new double[this.nPoints][this.mPoints];
        	this.d2ydx2innerT = new double[this.nPointsT][this.mPointsT];
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
        	for(int i =0; i<this.nPointsT; i++){
            		for(int j=0; j<this.mPointsT; j++){
                		this.yT[i][j]=y[j][i];
            		}
        	}

        	double[] yTempn = new double[mPoints];
	    	for(int i=0; i<this.nPoints; i++){
	        	for(int j=0; j<mPoints; j++)yTempn[j]=y[i][j];
	        	this.csn[i].resetData(x2,yTempn);
	        	this.csn[i].calcDeriv();
	        	this.d2ydx2inner[i]=this.csn[i].getDeriv();
	    	}

	    	double[] yTempnT = new double[mPointsT];
	    	for(int i=0; i<this.nPointsT; i++){
	        	for(int j=0; j<mPointsT; j++)yTempnT[j]=yT[i][j];
	        	this.csnT[i].resetData(x1,yTempnT);
	        	this.csnT[i].calcDeriv();
	        	this.d2ydx2innerT[i]=this.csnT[i].getDeriv();
	    	}
	    	this.derivCalculated = true;
    	}

    	// Constructor with data arrays initialised to zero
    	// Primarily for use by TriCubicSpline
        public BiCubicSpline(int nP, int mP){
        	this.nPoints=nP;
        	this.mPoints=mP;
          	if(this.nPoints<3 || this.mPoints<3)throw new IllegalArgumentException("The data matrix must have a minimum size of 3 X 3");
            this.nPointsT=mP;
        	this.mPointsT=nP;
        	this.csm = new CubicSpline(this.nPoints);
        	this.csmT = new CubicSpline(this.nPointsT);
        	if(!this.roundingCheck)this.csm.noRoundingErrorCheck();

        	this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
        	this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);

        	this.x1 = new double[this.nPoints];
        	this.x2 = new double[this.mPoints];
        	this.y = new double[this.nPoints][this.mPoints];
        	this.yT = new double[this.nPointsT][this.mPointsT];
        	this.d2ydx2inner = new double[this.nPoints][this.mPoints];
        	this.d2ydx2innerT = new double[this.nPointsT][this.mPointsT];

    	}

    	//  METHODS

    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            BiCubicSpline.roundingCheck = false;
            CubicSpline.noRoundingErrorCheck();
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            BiCubicSpline.potentialRoundingError = potentialRoundingError;
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

  	    // Resets the x1, x2, y data arrays
  	    // Primarily for use in TiCubicSpline
    	public void resetData(double[] x1, double[] x2, double[][] y){
        	if(x1.length!=y.length)throw new IllegalArgumentException("Arrays x1 and y row are of different length");
        	if(x2.length!=y[0].length)throw new IllegalArgumentException("Arrays x2 and y column are of different length");
        	if(this.nPoints!=x1.length)throw new IllegalArgumentException("Original array length not matched by new array length");
        	if(this.mPoints!=x2.length)throw new IllegalArgumentException("Original array length not matched by new array length");

        	for(int i=0; i<this.nPoints; i++){
                this.x1[i]=x1[i];
            }

            for(int i=0; i<this.mPoints; i++){
                this.x2[i]=x2[i];
            }

            for(int i=0; i<this.nPoints; i++){
             	for(int j=0; j<this.mPoints; j++){
                    this.y[i][j]=y[i][j];
                    this.yT[j][i]=y[i][j];
                }
        	}

        	this.csm = new CubicSpline(this.nPoints);
        	this.csn = CubicSpline.oneDarray(this.nPoints, this.mPoints);
        	double[] yTempn = new double[mPoints];

	    	for(int i=0; i<this.nPoints; i++){
	        	for(int j=0; j<mPoints; j++)yTempn[j]=y[i][j];
	        	this.csn[i].resetData(x2,yTempn);
	        	this.csn[i].calcDeriv();
	        	this.d2ydx2inner[i]=this.csn[i].getDeriv();
	    	}

	    	this.csmT = new CubicSpline(this.nPointsT);
        	this.csnT = CubicSpline.oneDarray(this.nPointsT, this.mPointsT);
        	double[] yTempnT = new double[mPointsT];

	    	for(int i=0; i<this.nPointsT; i++){
	        	for(int j=0; j<mPointsT; j++)yTempnT[j]=yT[i][j];
	        	this.csnT[i].resetData(x1,yTempnT);
	        	this.csnT[i].calcDeriv();
	        	this.d2ydx2innerT[i]=this.csnT[i].getDeriv();
	    	}

	    	this.derivCalculated = true;

    	}

    	// Returns a new BiCubicSpline setting internal array size to nP x mP and all array values to zero with natural spline default
    	// Primarily for use in this.oneDarray for TiCubicSpline
    	public static BiCubicSpline zero(int nP, int mP){
        	if(nP<3 || mP<3)throw new IllegalArgumentException("A minimum of three x three data points is needed");
        	BiCubicSpline aa = new BiCubicSpline(nP, mP);
        	return aa;
    	}

    	// Create a one dimensional array of BiCubicSpline objects of length nP each of internal array size mP x lP
    	// Primarily for use in TriCubicSpline
    	public static BiCubicSpline[] oneDarray(int nP, int mP, int lP){
        	if(mP<3 || lP<3)throw new IllegalArgumentException("A minimum of three x three data points is needed");
        	BiCubicSpline[] a =new BiCubicSpline[nP];
	    	for(int i=0; i<nP; i++){
	        	a[i]=BiCubicSpline.zero(mP, lP);
        	}
        	return a;
    	}


    	// Get inner matrix of derivatives
    	// Primarily used by TriCubicSpline
    	public double[][] getDeriv(){
    	    return this.d2ydx2inner;
    	}

    	// Get inner matrix of transpose derivatives
    	// Primarily used by TriCubicSpline
    	public double[][] getDerivTranspose(){
    	    return this.d2ydx2innerT;
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

    	// Set inner matrix of derivatives
    	// Primarily used by TriCubicSpline
    	public void setDeriv(double[][] d2ydx2){
    	    this.d2ydx2inner = d2ydx2;
    	    this.derivCalculated = true;
    	}

    	// Set inner matrix of transpose derivatives
    	// Primarily used by TriCubicSpline
    	public void setDerivTranspose(double[][] d2ydx2){
    	    this.d2ydx2innerT = d2ydx2;
    	    this.derivCalculated = true;
    	}

    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x1,x2)
    	public double interpolate(double xx1, double xx2){

    	    this.xx1 = xx1;
    	    this.xx2 = xx2;

	    	double[] yTempm = new double[this.nPoints];

	    	for (int i=0;i<this.nPoints;i++){
		    	yTempm[i]=this.csn[i].interpolate(xx2);
	    	}
	    	this.csm.resetData(x1,yTempm);
	    	this.interpolatedValue = this.csm.interpolate(xx1);

	    	double[] yTempmT = new double[this.nPointsT];

            for (int i=0;i<this.nPointsT;i++){
		    	yTempmT[i]=this.csnT[i].interpolate(xx1);
	    	}
	    	this.csmT.resetData(x2,yTempmT);
	    	this.interpolatedValueTranspose = this.csmT.interpolate(xx2);

	    	this.interpolatedValueMean = (this.interpolatedValue + this.interpolatedValueTranspose)/2.0;
	    	return this.interpolatedValueMean;
    	}

    	// Returns mean interpolated value, interpolated value for data as entered, interpolated value for transposed matrix, xx1 value and xx2 value
    	public double[] getInterpolatedValues(){
    	    double[] ret = {this.interpolatedValueMean, this.interpolatedValue, this.interpolatedValueTranspose, this.xx1, this.xx2};
    	    return ret;
    	}



}

