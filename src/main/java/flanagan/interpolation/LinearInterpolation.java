/**********************************************************
*
*   Class LinearInterpolation
*
*   Class for performing a linear interpolation
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	February 2011
*   UPDATE:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/LinearInterpolation.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2011  Michael Thomas Flanagan
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

public class LinearInterpolation{

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

        private boolean checkPoints = false;                        // = true when points checked for identical values
        private static boolean supress = false;                     // if true: warning messages supressed

        private static boolean averageIdenticalAbscissae = false;   // if true: the the ordinate values for identical abscissae are averaged
                                                                    // if false: the abscissae values are separated by 0.001 of the total abscissae range;
        private static double potentialRoundingError = 5e-15;       // potential rounding error used in checking wheter a value lies within the interpolation bounds
        private static boolean roundingCheck = true;                // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit


    	// Constructor with data arrays initialised to arrays x and y
    	public LinearInterpolation(double[] x, double[] y){
        	this.nPoints=x.length;
        	this.nPointsOriginal = this.nPoints;
        	if(this.nPoints!=y.length)throw new IllegalArgumentException("Arrays x and y are of different length "+ this.nPoints + " " + y.length);
        	if(this.nPoints<3)throw new IllegalArgumentException("A minimum of three data points is needed");
        	this.x = new double[nPoints];
        	this.y = new double[nPoints];
        	for(int i=0; i<this.nPoints; i++){
            		this.x[i]=x[i];
            		this.y[i]=y[i];
        	}
        	this.orderPoints();
    	    this.checkForIdenticalPoints();
    	}

    	// METHODS
    	// Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any poit lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            LinearInterpolation.roundingCheck = false;
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            LinearInterpolation.potentialRoundingError = potentialRoundingError;
        }

        // Reset the default handing of identical abscissae with different ordinates
        // from the default option of separating the two relevant abscissae by 0.001 of the range
        // to averaging the relevant ordinates
    	public static void averageIdenticalAbscissae(){
    	    LinearInterpolation.averageIdenticalAbscissae = true;
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
    	                    if(!LinearInterpolation.supress){
    	                        System.out.print("LinearInterpolation: Two identical points, " + this.x[ii] + ", " + this.y[ii]);
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
    	                    if(LinearInterpolation.averageIdenticalAbscissae==true){
    	                        if(!LinearInterpolation.supress){
    	                            System.out.print("LinearInterpolation: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + x[ii] + ": " + y[ii] + ", " + y[jj]);
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
    	                        if(!LinearInterpolation.supress)System.out.print("LinearInterpolation: Two identical points on the absicca (x-axis) with different ordinate (y-axis) values, " + x[ii] + ": " + y[ii] + ", " + y[jj]);
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
    	                        if(!LinearInterpolation.supress)System.out.println(", the two abscissae have been separated by a distance " + sepn);
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
    	    LinearInterpolation.supress = true;
    	}

    	// Unsupress warning messages in the identifiaction of duplicate points
    	public static void unsupress(){
    	    LinearInterpolation.supress = false;
    	}

    	//  INTERPOLATE
    	//  Returns an interpolated value of y for a value of x from a tabulated function y=f(x)
    	//  after the data has been entered via a constructor.
    	public double interpolate(double xx){

    	    // Check for violation of interpolation bounds
        	if (xx<this.x[0]){
        	    // if violation is less than potntial rounding error - amend to lie with bounds
        	    if(LinearInterpolation.roundingCheck && Math.abs(this.x[0]-xx)<=Math.pow(10, Math.floor(Math.log10(Math.abs(this.x[0]))))*LinearInterpolation.potentialRoundingError){
        	        xx = x[0];
        	    }
        	    else{
        	        throw new IllegalArgumentException("x ("+xx+") is outside the range of data points ("+this.x[0]+" to "+this.x[this.nPoints-1] + ")");
        	    }
	    	}
	    	if (xx>this.x[this.nPoints-1]){
        	    if(LinearInterpolation.roundingCheck && Math.abs(xx-this.x[this.nPoints-1])<=Math.pow(10, Math.floor(Math.log10(Math.abs(this.x[this.nPoints-1]))))*LinearInterpolation.potentialRoundingError){
        	        xx = this.x[this.nPoints-1];
                }
                else{
        	        throw new IllegalArgumentException("x ("+xx+") is outside the range of data points ("+this.x[0]+" to "+this.x[this.nPoints-1] + ")");
                }
            }

            boolean flag = true;
            for(int i=0; i<this.nPoints; i++){
                if(xx==this.x[i]){
                     yy = this.y[i];
                     flag = false;
                }
                if(!flag)break;
            }
            if(flag){
                for(int i=1; i<this.nPoints; i++){
                    if(xx<this.x[i]){
                        yy = this.y[i] - (this.y[i] - this.y[i-1])*(this.x[i] - xx)/(this.x[i] - this.x[i-1]);
                        flag = false;
                    }
                    if(!flag)break;
                }
            }

	    	return this.yy;
    	}

}
