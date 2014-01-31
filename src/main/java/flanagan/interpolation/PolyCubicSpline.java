/**********************************************************
*
*   PolyCubicSpline.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2, x3 .... xn) using a natural cubic splines
*   Assumes second derivatives at end points = 0 (natural spines)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    15 February 2006,
*   UPDATES:    9 June 2007, 27 July 2007, 4 December 2007, 21 September 2008, 12 October 2009, 31 October 2009
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PolyCubicSpline.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2006 - 2009  Michael Thomas Flanagan
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
import java.lang.reflect.Array;

public class PolyCubicSpline{

    	private int nDimensions = 0;   	                        // number of the dimensions of the tabulated points array, y=f(x1,x2,x3 . . xn), i.e. n
    	private Object fOfX = null;                             // tabulated values of y = f(x1,x2,x3 . . fn)
    	                                                        // as a multidimensional array of double [x1 length][x2 length] ... [xn length]
    	private Object internalDeriv = null;                    // Object to store second derivatives
    	                                                        // as a multidimensional array of Objects the innermost three layers being double arrays
    	private Object xArrays = null;                          // The variable arrays x1, x2, x3 . . . xn
    	                                                        // packed as an Object as a multidimensional array of double [][]
    	                                                        // where xArrays[0] = array of x1 values, xArrays[1] = array of x2 values etc
    	private Object method = null;                           // interpolation method
    	private double[][] xArray = null;                       // The variable arrays x1, x2, x3 . . . xn
        private double[] csArray = null;                        // array for final cubic spline interpolation
        private PolyCubicSpline[] pcs = null;                   // array of PolyCubicSplines for use with recursive step
        private int dimOne = 0;                                 // xArray dimension in a recursive step

        private double yValue = 0.0D;                           // returned interpolated value
    	private double[] xMin = null;                           // minimum values of the x arrays
    	private double[] xMax = null;                           // maximum values of the x arrays
    	private boolean calculationDone = false;                // = true when derivatives calculated
        private boolean averageIdenticalAbscissae = false;      // if true: the the ordinate values for identical abscissae are averaged
                                                                // If false: the abscissae values are separated by 0.001 of the total abscissae range;
        private static double potentialRoundingError = 5e-15;   // potential rounding error used in checking wheter a value lies within the interpolation bounds (static value)
        private static boolean roundingCheck = true;            // = true: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit (static value)


    	// Constructor
    	public PolyCubicSpline(Object xArrays, Object fOfX){

    	    this.fOfX = Fmath.copyObject(fOfX);
    	    this.xArrays = Fmath.copyObject(xArrays);

    	    // Calculate fOfX array dimension number
    	    Object internalArrays = Fmath.copyObject(fOfX);
    	    this.nDimensions = 1;
            while(!((internalArrays  =  Array.get(internalArrays, 0)) instanceof Double))this.nDimensions++;

            // Repack xArrays as 2 dimensional array if entered a single dimensioned array for a simple cubic spline
            if(this.xArrays instanceof double[] && this.nDimensions == 1){
                double[][] xArraysTemp = new double[1][];
                xArraysTemp[0] = (double[])this.xArrays;
                this.xArrays = (Object)xArraysTemp;
            }
            else{
               if(!(this.xArrays instanceof double[][]))throw new IllegalArgumentException("xArrays should be a two dimensional array of doubles");
            }

            // x -arrays and their limits
            this.xArray = (double[][])this.xArrays;
            this.limits();

            // Select interpolation method
            switch(this.nDimensions){
                case 0: throw new IllegalArgumentException("data array must have at least one dimension");
                case 1:     // If fOfX is one dimensional perform simple cubic spline
                            CubicSpline cs = new CubicSpline(this.xArray[0], (double[])this.fOfX);
                            if(this.averageIdenticalAbscissae)cs.averageIdenticalAbscissae();
                            this.internalDeriv = (Object)(cs.getDeriv());
                            this.method = (Object)cs;
                            this.calculationDone = true;
                            break;
                case 2:     // If fOfX is two dimensional perform bicubic spline
                            BiCubicSpline bcs = new BiCubicSpline(this.xArray[0], this.xArray[1], (double[][])this.fOfX);
                            if(this.averageIdenticalAbscissae)bcs.averageIdenticalAbscissae();
                            this.internalDeriv = (Object)(bcs.getDeriv());
                            this.method = (Object)bcs;
                            this.calculationDone = true;
                            break;
                case 3:     // If fOfX is three dimensional perform tricubic spline
                            TriCubicSpline tcs = new TriCubicSpline(xArray[0], xArray[1], xArray[2], (double[][][])this.fOfX);
                            if(this.averageIdenticalAbscissae)tcs.averageIdenticalAbscissae();
                            this.internalDeriv = (Object)(tcs.getDeriv());
                            this.method = (Object)tcs;
                            this.calculationDone = true;
                            break;
                case 4:     // If fOfX is four dimensional perform quadricubic spline
                            QuadriCubicSpline qcs = new QuadriCubicSpline(xArray[0], xArray[1], xArray[2], xArray[3], (double[][][][])this.fOfX);
                            if(this.averageIdenticalAbscissae)qcs.averageIdenticalAbscissae();
                            this.internalDeriv = (Object)(qcs.getDeriv());
                            this.method = (Object)qcs;
                            this.calculationDone = true;
                            break;
                default:    // If fOfX is greater than four dimensional, recursively call PolyCubicSpline
                            //  with, as arguments, the n1 fOfX sub-arrays, each of (number of dimensions - 1) dimensions,
                            //  where n1 is the number of x1 variables.
                            Object obj = fOfX;
                            this.dimOne = Array.getLength(obj);
                            this.csArray = new double [dimOne];
                            double[][] newXarrays= new double[this.nDimensions-1][];
                            for(int i=0; i<this.nDimensions-1; i++){
                                newXarrays[i] = xArray[i+1];
                            }

                            Object[] objDeriv = new Object[dimOne];
                            if(calculationDone)objDeriv = (Object[])this.internalDeriv;
                            this.pcs = new PolyCubicSpline[dimOne];
                            for(int i=0; i<dimOne; i++){
                                Object objT = (Object)Array.get(obj, i);
                                this.pcs[i] = new PolyCubicSpline(newXarrays, objT);
                                if(this.averageIdenticalAbscissae)pcs[i].averageIdenticalAbscissae();
                                if(this.calculationDone)pcs[i].setDeriv(objDeriv[i]);
                                if(!this.calculationDone)objDeriv[i] = pcs[i].getDeriv();
                            }
                            this.internalDeriv = (Object)objDeriv;
                            this.calculationDone = true;
            }

        }

        // Reset rounding error check option
    	// Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
    	// This method causes this check to be ignored and an exception to be thrown if any point lies outside the interpolation bounds
    	public static void noRoundingErrorCheck(){
            PolyCubicSpline.roundingCheck = false;
            QuadriCubicSpline.noRoundingErrorCheck();
            TriCubicSpline.noRoundingErrorCheck();
            BiCubicSpline.noRoundingErrorCheck();
            CubicSpline.noRoundingErrorCheck();
        }

        // Reset potential rounding error value
        // Default option: points outside the interpolation bounds by less than the potential rounding error rounded to the bounds limit
        // The default value for the potential rounding error is 5e-15*times the 10^exponent of the value outside the bounds
	    // This method allows the 5e-15 to be reset
    	public static void potentialRoundingError(double potentialRoundingError){
            PolyCubicSpline.potentialRoundingError = potentialRoundingError;
            QuadriCubicSpline.potentialRoundingError(potentialRoundingError);
            TriCubicSpline.potentialRoundingError(potentialRoundingError);
            BiCubicSpline.potentialRoundingError(potentialRoundingError);
            CubicSpline.potentialRoundingError(potentialRoundingError);
        }

        // Limits to x arrays
        private void limits(){
            this.xMin = new double[this.nDimensions];
            this.xMax = new double[this.nDimensions];
            for(int i=0; i<this.nDimensions; i++){
                this.xMin[i] = Fmath.minimum(xArray[i]);
                this.xMax[i] = Fmath.maximum(xArray[i]);
            }
        }

   	    // Get minimum limits
    	public double[] getXmin(){
    	    return this.xMin;
    	}

    	// Get maximum limits
    	public double[] getXmax(){
    	    return this.xMax;
    	}

    	// Get number of dimensions
    	public int getNumberOfDimensions(){
    	    return this.nDimensions;
    	}

    	// Get limits to x
    	public double[] getLimits(){
    	    double[] limits = new double[2*this.nDimensions];
    	    int j = 0;
    	    for(int i=0; i<this.nDimensions; i++){
                limits[j] = this.xMin[i];
                j++;
                limits[j] = this.xMax[i];
                j++;
            }
    	    return limits;
    	}

    	// Display limits to x
    	public void displayLimits(){
    	    System.out.println(" ");
    	    for(int i=0; i<this.nDimensions; i++){
    	        System.out.println("The limits to the x array " + i + " are " + xMin[i] + " and " + xMax[i]);
    	    }
    	    System.out.println(" ");
    	}

    	// Reset the default handing of identical abscissae with different ordinates
        // from the default option of separating the two relevant abscissae by 0.001 of the range
        // to avraging the relevant ordinates
    	public void averageIdenticalAbscissae(){
    	    this.averageIdenticalAbscissae = true;
    	}

    	//  Interpolation method
    	public double interpolate(double[] unknownCoord){

    	    int nUnknown = unknownCoord.length;
    	    if(nUnknown!=this.nDimensions)throw new IllegalArgumentException("Number of unknown value coordinates, " + nUnknown + ", does not equal the number of tabulated data dimensions, " + this.nDimensions);

            switch(this.nDimensions){
                case 0:     throw new IllegalArgumentException("data array must have at least one dimension");
                case 1:     // If fOfX is one dimensional perform simple cubic spline
                            this.yValue = ((CubicSpline)(this.method)).interpolate(unknownCoord[0]);
                            break;
                case 2:     // If fOfX is two dimensional perform bicubic spline
                            this.yValue = ((BiCubicSpline)(this.method)).interpolate(unknownCoord[0], unknownCoord[1]);
                            break;
                case 3:     // If fOfX is three dimensional perform tricubic spline
                            this.yValue = ((TriCubicSpline)(this.method)).interpolate(unknownCoord[0], unknownCoord[1], unknownCoord[2]);
                            break;
                case 4:     // If fOfX is four dimensional perform quadricubic spline
                            this.yValue = ((QuadriCubicSpline)(this.method)).interpolate(unknownCoord[0], unknownCoord[1], unknownCoord[2], unknownCoord[2]);
                            break;
                default:    // If fOfX is greater than four dimensional, recursively call PolyCubicSpline
                            //  with, as arguments, the n1 fOfX sub-arrays, each of (number of dimensions - 1) dimensions,
                            //  where n1 is the number of x1 variables.
                            double[] newCoord = new double[this.nDimensions-1];
                            for(int i=0; i<this.nDimensions-1; i++){
                                newCoord[i] = unknownCoord[i+1];
                            }
                            for(int i=0; i<this.dimOne; i++){
                                csArray[i] = pcs[i].interpolate(newCoord);
                            }

                            // Perform simple cubic spline on the array of above returned interpolates
                            CubicSpline ncs = new CubicSpline(this.xArray[0], this.csArray);
            	    	    this.yValue = ncs.interpolate(unknownCoord[0]);
            }

            return this.yValue;
    	}

    	// Set derivatives (internal array)
    	public void setDeriv(Object internalDeriv){
    	    this.internalDeriv = internalDeriv;
    	}

    	// Get derivatives (internal array)
    	public Object getDeriv(){
    	    return this.internalDeriv;
    	}


}

