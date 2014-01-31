/**********************************************************
*
*   PolyCubicSplineFast.java
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2, x3 .... xn) using a natural cubic splines
*   Assumes second derivatives at end points = 0 (natural spines)
*   Stripped down version of PolyCubicSpline - all data checks have been removed for faster running
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	4 January 2010 (Stripped down version of PolyCubicSpline: 9 June 2007 - 31 October 2009)
*   UPDATES:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PolyCubicSplineFast.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2007 - 2010  Michael Thomas Flanagan
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

public class PolyCubicSplineFast{

    	private int nDimensions = 0;   	                        // number of the dimensions of the tabulated points array, y=f(x1,x2,x3 . . xn), i.e. n
    	private Object fOfX = null;                             // tabulated values of y = f(x1,x2,x3 . . fn)
    	                                                        // as a multidimensional array of double [x1 length][x2 length] ... [xn length]
    	private Object xArrays = null;                          // The variable arrays x1, x2, x3 . . . xn
    	                                                        // packed as an Object as a multidimensional array of double [][]
    	                                                        // where xArrays[0] = array of x1 values, xArrays[1] = array of x2 values etc
    	private Object method = null;                           // interpolation method
    	private double[][] xArray = null;                       // The variable arrays x1, x2, x3 . . . xn
        private double[] csArray = null;                        // array for final cubic spline interpolation
        private PolyCubicSplineFast[] pcs = null;               // array of PolyCubicSplineFasts for use with recursive step
        private int dimOne = 0;                                 // xArray dimension in a recursive step

        private double yValue = 0.0D;                           // returned interpolated value


    	// Constructor
    	public PolyCubicSplineFast(Object xArrays, Object fOfX){

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

            // Select interpolation method
            switch(this.nDimensions){
                case 0: throw new IllegalArgumentException("data array must have at least one dimension");
                case 1:     // If fOfX is one dimensional perform simple cubic spline
                            CubicSplineFast cs = new CubicSplineFast(this.xArray[0], (double[])this.fOfX);
                            this.method = (Object)cs;
                            break;
                case 2:     // If fOfX is two dimensional perform bicubic spline
                            BiCubicSplineFast bcs = new BiCubicSplineFast(this.xArray[0], this.xArray[1], (double[][])this.fOfX);
                            this.method = (Object)bcs;
                            break;
                default:    // If fOfX is greater than four dimensional, recursively call PolyCubicSplineFast
                            //  with, as arguments, the n1 fOfX sub-arrays, each of (number of dimensions - 1) dimensions,
                            //  where n1 is the number of x1 variables.
                            Object obj = fOfX;
                            this.dimOne = Array.getLength(obj);
                            this.csArray = new double [dimOne];
                            double[][] newXarrays= new double[this.nDimensions-1][];
                            for(int i=0; i<this.nDimensions-1; i++){
                                newXarrays[i] = xArray[i+1];
                            }

                            this.pcs = new PolyCubicSplineFast[dimOne];
                            for(int i=0; i<dimOne; i++){
                                Object objT = (Object)Array.get(obj, i);
                                this.pcs[i] = new PolyCubicSplineFast(newXarrays, objT);
                            }
            }
        }


    	//  Interpolation method
    	public double interpolate(double[] unknownCoord){

    	    int nUnknown = unknownCoord.length;
    	    if(nUnknown!=this.nDimensions)throw new IllegalArgumentException("Number of unknown value coordinates, " + nUnknown + ", does not equal the number of tabulated data dimensions, " + this.nDimensions);

            switch(this.nDimensions){
                case 0:     throw new IllegalArgumentException("data array must have at least one dimension");
                case 1:     // If fOfX is one dimensional perform simple cubic spline
                            this.yValue = ((CubicSplineFast)(this.method)).interpolate(unknownCoord[0]);
                            break;
                case 2:     // If fOfX is two dimensional perform bicubic spline
                            this.yValue = ((BiCubicSplineFast)(this.method)).interpolate(unknownCoord[0], unknownCoord[1]);
                            break;
                default:    // If fOfX is greater than two dimensional, recursively call PolyCubicSplineFast
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
                            CubicSplineFast ncs = new CubicSplineFast(this.xArray[0], this.csArray);
            	    	    this.yValue = ncs.interpolate(unknownCoord[0]);
            }

            return this.yValue;
    	}

}

