/**********************************************************
*
*   Class CubicSpline
*
*   Class for performing an interpolation using a cubic spline
*   setTabulatedArrays and interpolate adapted, with modification to
*   an object-oriented approach, from Numerical Recipes in C (http://www.nr.com/)
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	May 2002
*   UPDATE: 29 April 2005, 17 February 2006, 21 September 2006, 4 December 2007
*           24 March 2008 (Thanks to Peter Neuhaus, Florida Institute for Human and Machine Cognition)
*           21 September 2008
*           14 January 2009 - point deletion and check for 3 points reordered (Thanks to Jan Sacha, Vrije Universiteit Amsterdam)
*           31 October 2009, 11-14 January 2010
*           31 August 2010 - overriding natural spline error, introduced in earlier update, corrected (Thanks to Dagmara Oszkiewicz, University of Helsinki)
*           2 February 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/CubicSpline.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2010  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
*   Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
*   provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
*   and associated documentation or publications.
*
*   Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice,
*   this list of conditions and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
*   Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
*   the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission
*   from the Michael Thomas Flanagan:
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
*   Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
*   or its derivatives.
*
***************************************************************************************/


package flanagan.interpolation;

import flanagan.math.Fmath;
import flanagan.math.Conv;

public class CubicSpline{

    	private int nPoints = 0;                                    // no. of tabulated points
    	private int nPointsOriginal = 0;                            // no. of tabulated points after any deletions of identical points
    	private double[] y = null;                                  // y=f(x) tabulated function
    	private double[] x = null;                                  // x in tabulated function f(x)
    	private double yy = Double.NaN;                             // interpolated value of y
    	private double dydx = Double.NaN;                           // interpolated value of the first derivative, dy/dx
    	private int[]newAndOldIndices;                              // record of indices on ordering x into ascending order
    	private double xMin = Double.NaN;                           // minimum x value
    	private double xMax = Double.NaN;                           // maximum x value
    	private double range = Double.NaN;                          // xMax - xMin
    	private double[] d2ydx2 = null;                             // second derivatives of y
    	private double yp1 = Double.NaN;                            // second derivative at point one
                                    	                            // default value = NaN (natural spline)
    	private double ypn = Double.NaN;                            // second derivative at point n
                                    	                            // default value = NaN (natural spline)
        private boolean derivCalculated = false;                    // = true when the derivatives have been calculated

        private boolean checkPoints = false;                        // = true when points checked for identical values
        private static boolean supress = false;                     // if true: warning messages supressed

        private static boolean averageIdenticalAbscissae = false;   // if true: the the ordinate values for identical abscissae are averaged
                                                                    // if false: the abscissae values are separated by 0.001 of the total abscissae range;
        private static double potentialRoundingError = 5e-15;       // potential rounding error used in checking wheter a value lies within the interpolation bounds
        private static boolean roundingCheck = true;                // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit


    	// Constructors
    	// Constructor with data arrays initialised to arrays x and y
    	public CubicSpline(double[] x, double[] y){
        	this.nPoints=x.length;
        	this.nPointsOriginal = this.nPoints;
        	if(this.nPoints!=y.length)throw new IllegalArgumentException("Arrays x and y are of different length "+ this.nPoints + " " + y.length);
        	if(this.nPoints<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	this.x = new double[nPoints];
        	this.y = new double[nPoints];
        	this.d2ydx2 = new double[nPoints];
        	for(int i=0; i<this.nPoints; i++){
            		this.x[i]=x[i];
            		this.y[i]=y[i];
        	}
        	this.orderPoints();
    	    this.checkForIdenticalPoints();
	    	this.calcDeriv();

    	}

    	// Constructor with data arrays initialised to zero
    	// Primarily for use by BiCubicSpline
    	public CubicSpline(int nPoints){
        	this.nPoints=nPoints;
        	this.nPointsOriginal = this.nPoints;
        	if(this.nPoints<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	this.x = new double[nPoints];
        	this.y = new double[nPoints];
        	this.d2ydx2 = new double[nPoints];
   	    }

    	// METHODS
    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any poit lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            CubicSpline.roundingCheck = false;
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            CubicSpline.potentialRoundingError = potentialRoundingError;
        }

    	// Resets the x y data arrays - primarily for use in BiCubicSpline
    	public void resetData(double[] x, double[] y){
        	this.nPoints = this.nPointsOriginal;
        	if(x.length!=y.length)throw new IllegalArgumentException("Arrays x and y are of different length");
        	if(this.nPoints!=x.length)throw new IllegalArgumentException("Original array length not matched by new array length");

        	for(int i=0; i<this.nPoints; i++){
            		this.x[i]=x[i];
            		this.y[i]=y[i];
        	}
        	this.orderPoints();
        	this.checkForIdenticalPoints();
    	}

        // Reset the default handing of identical abscissae with different ordinates
        // from the default option of separating the two relevant abscissae by 0.001 of the range
        // to averaging the relevant ordinates
    	public static void averageIdenticalAbscissae(){
    	    CubicSpline.averageIdenticalAbscissae = true;
    	}

    	// Sort points into an ascending abscissa order
    	public void orderPoints(){
    	    double[] dummy = new double[nPoints];
    	    this.newAndOldIndices = new int[nPoints];
    	    // Sort x into ascending order storing indices changes
    	    Fmath.selectionSort(this.x, dummy, this.newAndOldIndices);
    	    // Sort x into ascending order and make y match the new order storing both new x and new y
    	    Fmath.selectionSort(this.x, this.y, this.x, this.y);

    	    // Minimum and maximum values and range
    	    this.xMin = Fmath.minimum(this.x);
    	    this.xMax = Fmath.maximum(this.x);
    	    range = xMax - xMin;
    	}

    	// get the maximum value
    	public double getXmax(){
    	    return this.xMax;
    	}

     	// get the minimum value
    	public double getXmin(){
    	    return this.xMin;
    	}

     	// get the limits of x
    	public double[] getLimits(){
    	    double[] limits = {this.xMin, this.xMax};
    	    return limits;
    	}

    	// print to screen the limis of x
    	public void displayLimits(){
    	    System.out.println("\nThe limits of the abscissae (x-values) are " + this.xMin + " and " + this.xMax +"\n");
    	}


    	// Checks for and removes all but one of identical points
    	// Checks and appropriately handles identical abscissae with differing ordinates
    	public void checkForIdenticalPoints(){
    	    int nP = this.nPoints;
    	    boolean test1 = true;
    	    int ii = 0;
    	    while(test1){
    	        boolean test2 = true;
    	        int jj = ii+1;
    	        while(test2){
    	            if(this.x[ii]==this.x[jj]){
    	                if(this.y[ii]==this.y[jj]){
    	                    if(!CubicSpline.supress){
    	                        System.out.print("CubicSpline: Two identical points, " + this.x[ii] + ", " + this.y[ii]);
    	                        System.out.println(", in data array at indices " + this.newAndOldIndices[ii] + " and " +  this.newAndOldIndices[jj] + ", latter point removed");
                            }
                            double[] xx = new double[this.nPoints-1];
                            double[] yy = new double[this.nPoints-1];
                            int[] naoi = new int[this.nPoints-1];
                            for(int i=0; i<jj; i++){
                                xx[i] = this.x[i];
                                yy[i] = this.y[i];
                                naoi[i] = this.newAndOldIndices[i];
                            }
                            for(int i=jj; i<this.nPoints-1; i++){
                                xx[i] = this.x[i+1];
                                yy[i] = this.y[i+1];
                                naoi[i] = this.newAndOldIndices[i+1];
                            }
                            this.nPoints--;
                            this.x = Conv.copy(xx);
                            this.y = Conv.copy(yy);
                            this.newAndOldIndices = Conv.copy(naoi);
    	                }
    	                else{
    	                    if(CubicSpline.averageIdenticalAbscissae==true){
    	                        if(!CubicSpline.supress){
    	                            System.out.print("CubicSpline: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + x[ii] + ": " + y[ii] + ", " + y[jj]);
    	                            System.out.println(", average of the ordinates taken");
    	                        }
    	                        this.y[ii] = (this.y[ii] + this.y[jj])/2.0D;
    	                        double[] xx = new double[this.nPoints-1];
                                double[] yy = new double[this.nPoints-1];
                                int[] naoi = new int[this.nPoints-1];
    	                        for(int i=0; i<jj; i++){
                                    xx[i] = this.x[i];
                                    yy[i] = this.y[i];
                                    naoi[i] = this.newAndOldIndices[i];
                                }
                                for(int i=jj; i<this.nPoints-1; i++){
                                    xx[i] = this.x[i+1];
                                    yy[i] = this.y[i+1];
                                    naoi[i] = this.newAndOldIndices[i+1];
                                }
                                this.nPoints--;
                                this.x = Conv.copy(xx);
                                this.y = Conv.copy(yy);
                                this.newAndOldIndices = Conv.copy(naoi);
    	                    }
    	                    else{
    	                        double sepn = range*0.0005D;
    	                        if(!CubicSpline.supress)System.out.print("CubicSpline: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + x[ii] + ": " + y[ii] + ", " + y[jj]);
    	                        boolean check = false;
    	                        if(ii==0){
    	                            if(x[2]-x[1]<=sepn)sepn = (x[2]-x[1])/2.0D;
    	                            if(this.y[0]>this.y[1]){
    	                                if(this.y[1]>this.y[2]){
    	                                    check = stay(ii, jj, sepn);
                                        }
    	                                else{
    	                                    check = swap(ii, jj, sepn);
    	                                }
    	                            }
    	                            else{
    	                                if(this.y[2]<=this.y[1]){
    	                                    check = swap(ii, jj, sepn);
    	                                }
    	                                else{
    	                                    check = stay(ii, jj, sepn);
    	                                }
    	                            }
    	                        }
    	                        if(jj==this.nPoints-1){
    	                            if(x[nP-2]-x[nP-3]<=sepn)sepn = (x[nP-2]-x[nP-3])/2.0D;
    	                            if(this.y[ii]<=this.y[jj]){
    	                                if(this.y[ii-1]<=this.y[ii]){
    	                                    check = stay(ii, jj, sepn);
    	                                }
    	                                else{
    	                                    check = swap(ii, jj, sepn);
    	                                }
    	                            }
    	                            else{
    	                                if(this.y[ii-1]<=this.y[ii]){
    	                                    check = swap(ii, jj, sepn);
    	                                }
    	                                else{
    	                                    check = stay(ii, jj, sepn);
    	                                }
    	                            }
    	                        }
    	                        if(ii!=0 && jj!=this.nPoints-1){
    	                            if(x[ii]-x[ii-1]<=sepn)sepn = (x[ii]-x[ii-1])/2;
    	                            if(x[jj+1]-x[jj]<=sepn)sepn = (x[jj+1]-x[jj])/2;
                                    if(this.y[ii]>this.y[ii-1]){
    	                                if(this.y[jj]>this.y[ii]){
    	                                    if(this.y[jj]>this.y[jj+1]){
    	                                        if(this.y[ii-1]<=this.y[jj+1]){
    	                                            check = stay(ii, jj, sepn);
    	                                        }
    	                                        else{
    	                                            check = swap(ii, jj, sepn);
    	                                        }
    	                                    }
    	                                    else{
    	      	                                 check = stay(ii, jj, sepn);
    	                                    }
    	                                }
    	                                else{
    	                                    if(this.y[jj+1]>this.y[jj]){
    	                                        if(this.y[jj+1]>this.y[ii-1] && this.y[jj+1]>this.y[ii-1]){
    	                                            check = stay(ii, jj, sepn);
    	                                        }
    	                                    }
    	                                    else{
    	                                       check = swap(ii, jj, sepn);
    	                                    }
    	                                }
    	                            }
    	                            else{
    	                                if(this.y[jj]>this.y[ii]){
    	                                    if(this.y[jj+1]>this.y[jj]){
    	                                        check = stay(ii, jj, sepn);
    	                                    }
    	                                }
    	                                else{
    	                                    if(this.y[jj+1]>this.y[ii-1]){
    	                                        check = stay(ii, jj, sepn);
    	                                    }
    	                                    else{
    	                                        check = swap(ii, jj, sepn);
    	                                    }
    	                                }
    	                            }
    	                        }

    	                        if(check==false){
    	                            check = stay(ii, jj, sepn);
    	                        }
    	                        if(!CubicSpline.supress)System.out.println(", the two abscissae have been separated by a distance " + sepn);
                                jj++;
     	                    }
    	                }
    	                if((this.nPoints-1)==ii)test2 = false;
    	            }
    	            else{
    	                jj++;
    	            }
    	            if(jj>=this.nPoints)test2 = false;
    	        }
    	        ii++;
    	        if(ii>=this.nPoints-1)test1 = false;
    	    }
    	    if(this.nPoints<3)throw new IllegalArgumentException("Removal of duplicate points has reduced the number of points to less than the required minimum of three data points");

    	    this.checkPoints = true;
    	}

    	// Swap method for checkForIdenticalPoints procedure
    	private boolean swap(int ii, int jj, double sepn){
    	    this.x[ii] += sepn;
    	    this.x[jj] -= sepn;
    	    double hold = this.x[ii];
    	    this.x[ii] = this.x[jj];
    	    this.x[jj] = hold;
    	    hold = this.y[ii];
    	    this.y[ii] = this.y[jj];
    	    this.y[jj] = hold;
    	    return true;
    	}

    	// Stay method for checkForIdenticalPoints procedure
    	private boolean stay(int ii, int jj, double sepn){
    	    this.x[ii] -= sepn;
    	    this.x[jj] += sepn;
    	    return true;
    	}

    	// Supress warning messages in the identifiaction of duplicate points
    	public static void supress(){
    	    CubicSpline.supress = true;
    	}

    	// Unsupress warning messages in the identifiaction of duplicate points
    	public static void unsupress(){
    	    CubicSpline.supress = false;
    	}

    	// Returns a new CubicSpline setting array lengths to n and all array values to zero with natural spline default
    	// Primarily for use in BiCubicSpline
    	public static CubicSpline zero(int n){
        	if(n<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	CubicSpline aa = new CubicSpline(n);
        	return aa;
    	}

    	// Create a one dimensional array of cubic spline objects of length n each of array length m
    	// Primarily for use in BiCubicSpline
    	public static CubicSpline[] oneDarray(int n, int m){
        	if(m<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	CubicSpline[] a =new CubicSpline[n];
	    	for(int i=0; i<n; i++){
	        	a[i]=CubicSpline.zero(m);
        	}
        	return a;
    	}

    	// Enters the first derivatives of the cubic spline at
    	// the first and last point of the tabulated data
    	// Overrides a natural spline
    	public void setDerivLimits(double yp1, double ypn){
        	this.yp1=yp1;
        	this.ypn=ypn;
        	this.calcDeriv();
    	}

    	// Resets a natural spline
    	// Use above - this kept for backward compatibility
    	public void setDerivLimits(){
        	this.yp1=Double.NaN;
        	this.ypn=Double.NaN;
        	this.calcDeriv();
    	}

    	// Enters the first derivatives of the cubic spline at
    	// the first and last point of the tabulated data
    	// Overrides a natural spline
    	// Use setDerivLimits(double yp1, double ypn) - this kept for backward compatibility
    	public void setDeriv(double yp1, double ypn){
        	this.yp1=yp1;
        	this.ypn=ypn;
        	this.calcDeriv();
    	}

    	// Returns the internal array of second derivatives
    	public double[] getDeriv(){
    	    if(!this.derivCalculated)this.calcDeriv();
        	return this.d2ydx2;
    	}

    	// Sets the internal array of second derivatives
    	// Used primarily with BiCubicSpline
    	public void setDeriv(double[] deriv){
    	    this.d2ydx2 = deriv;
    	    this.derivCalculated = true;
    	}

    	//  Calculates the second derivatives of the tabulated function
    	//  for use by the cubic spline interpolation method (.interpolate)
    	//  This method follows the procedure in Numerical Methods C language procedure for calculating second derivatives
    	public void calcDeriv(){
	    	double	p=0.0D,qn=0.0D,sig=0.0D,un=0.0D;
	    	double[] u = new double[nPoints];

	    	if (Double.isNaN(this.yp1)){
	    		d2ydx2[0]=u[0]=0.0;
	    	}
	    	else{
			    this.d2ydx2[0] = -0.5;
		    	u[0]=(3.0/(this.x[1]-this.x[0]))*((this.y[1]-this.y[0])/(this.x[1]-this.x[0])-this.yp1);
	    	}

	    	for(int i=1;i<=this.nPoints-2;i++){
		    	sig=(this.x[i]-this.x[i-1])/(this.x[i+1]-this.x[i-1]);
		    	p=sig*this.d2ydx2[i-1]+2.0;
		    	this.d2ydx2[i]=(sig-1.0)/p;
		    	u[i]=(this.y[i+1]-this.y[i])/(this.x[i+1]-this.x[i]) - (this.y[i]-this.y[i-1])/(this.x[i]-this.x[i-1]);
		    	u[i]=(6.0*u[i]/(this.x[i+1]-this.x[i-1])-sig*u[i-1])/p;
	    	}

	    	if (Double.isNaN(this.ypn)){
		    	qn=un=0.0;
	    	}
	    	else{
		    	qn=0.5;
		    	un=(3.0/(this.x[nPoints-1]-this.x[this.nPoints-2]))*(this.ypn-(this.y[this.nPoints-1]-this.y[this.nPoints-2])/(this.x[this.nPoints-1]-x[this.nPoints-2]));
	    	}

	    	this.d2ydx2[this.nPoints-1]=(un-qn*u[this.nPoints-2])/(qn*this.d2ydx2[this.nPoints-2]+1.0);
	    	for(int k=this.nPoints-2;k>=0;k--){
		    	this.d2ydx2[k]=this.d2ydx2[k]*this.d2ydx2[k+1]+u[k];
	    	}
	    	this.derivCalculated = true;
    	}

    	//  INTERPOLATE
    	//  Returns an interpolated value of y for a value of x from a tabulated function y=f(x)
    	//  after the data has been entered via a constructor.
    	//  The derivatives are calculated, bt calcDeriv(), on the first call to this method ands are
    	//  then stored for use on all subsequent calls
    	public double interpolate(double xx){

    	    // Check for violation of interpolation bounds
        	if (xx<this.x[0]){
        	    // if violation is less than potntial rounding error - amend to lie with bounds
        	    if(CubicSpline.roundingCheck && Math.abs(x[0]-xx)<=Math.pow(10, Math.floor(Math.log10(Math.abs(this.x[0]))))*CubicSpline.potentialRoundingError){
        	        xx = x[0];
        	    }
        	    else{
        	        throw new IllegalArgumentException("x ("+xx+") is outside the range of data points ("+x[0]+" to "+x[this.nPoints-1] + ")");
        	    }
	    	}
	    	if (xx>this.x[this.nPoints-1]){
        	    if(CubicSpline.roundingCheck && Math.abs(xx-this.x[this.nPoints-1])<=Math.pow(10, Math.floor(Math.log10(Math.abs(this.x[this.nPoints-1]))))*CubicSpline.potentialRoundingError){
        	        xx = this.x[this.nPoints-1];
                }
                else{
        	        throw new IllegalArgumentException("x ("+xx+") is outside the range of data points ("+x[0]+" to "+x[this.nPoints-1] + ")");
                }
            }

            double h=0.0D,b=0.0D,a=0.0D, yy=0.0D;
	    	int k=0;
	    	int klo=0;
	    	int khi=this.nPoints-1;
	    	while (khi-klo > 1){
		    	k=(khi+klo) >> 1;
		    	if(this.x[k] > xx){
			    	khi=k;
		    	}
		    	else{
			    	klo=k;
		    	}
	    	}
	    	h=this.x[khi]-this.x[klo];

	    	if (h == 0.0){
	        	throw new IllegalArgumentException("Two values of x are identical: point "+klo+ " ("+this.x[klo]+") and point "+khi+ " ("+this.x[khi]+")" );
	    	}
	    	else{
	        	a=(this.x[khi]-xx)/h;
	        	b=(xx-this.x[klo])/h;
	        	this.yy = a*this.y[klo]+b*this.y[khi]+((a*a*a-a)*this.d2ydx2[klo]+(b*b*b-b)*this.d2ydx2[khi])*(h*h)/6.0;
	        	this.dydx = (y[khi] - y[klo])/h - ((3*a*a - 1.0)*this.d2ydx2[klo] - (3*b*b - 1.0)*this.d2ydx2[khi])*h/6.0;
	    	}
	    	return this.yy;
    	}


    	//  Returns an interpolated value of y  and of the first derivative dy/dx for a value of x from a tabulated function y=f(x)
    	//  after the data has been entered via a constructor.
    	public double[] interpolate_for_y_and_dydx(double xx){
    	    this.interpolate(xx);
    	    double[] ret = {this.yy, this.dydx};
    	    return ret;
    	}


    	//  Returns an interpolated value of y for a value of x (xx) from a tabulated function y=f(x)
    	//  after the derivatives (deriv) have been calculated independently of calcDeriv().
    	public static double interpolate(double xx, double[] x, double[] y, double[] deriv){

        	if(((x.length != y.length) || (x.length != deriv.length)) || (y.length != deriv.length)){
            		throw new IllegalArgumentException("array lengths are not all equal");
        	}
	    	int n = x.length;
        	double  h=0.0D, b=0.0D, a=0.0D, yy = 0.0D;

            int k=0;
	    	int klo=0;
	    	int khi=n-1;
        	while(khi-klo > 1){
		    	k=(khi+klo) >> 1;
		    	if(x[k] > xx){
			    	khi=k;
		    	}
	        	else{
	            		klo=k;
		    	}
        	}
        	h=x[khi]-x[klo];

	    	if (h == 0.0){
	        	throw new IllegalArgumentException("Two values of x are identical");
	    	}
	    	else{
	        	a=(x[khi]-xx)/h;
            	b=(xx-x[klo])/h;
	        	yy=a*y[klo]+b*y[khi]+((a*a*a-a)*deriv[klo]+(b*b*b-b)*deriv[khi])*(h*h)/6.0;
	    	}
	    	return yy;
	}

}
