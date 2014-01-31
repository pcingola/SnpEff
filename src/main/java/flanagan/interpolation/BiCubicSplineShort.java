/**********************************************************
*
*   BiCubicSplineShort.java  (Deprecated)
*
*   See Class BiCubicSplineFast for replacement class
*   This class has been retained purely for compatibility purposes
*   and will not be updated
*
*   Class for performing an interpolation on the tabulated
*   function y = f(x1,x2) using a natural bicubic spline
*   Assumes second derivatives at end points = 0 (natural spine)
*   Stripped down version of BiCubicSpline - all data checks have been removed for faster running
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    26 December 2009 (Stripped down version of BiCubicSpline: May 2002 - 31 October 2009)
*   DEPRECATED: 31 December 2009
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/BiCubicSplineShort.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2009   Michael Thomas Flanagan
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

public class BiCubicSplineShort{

    	private int nPoints = 0;   	                            // no. of x1 tabulated points
    	private int mPoints = 0;   	                            // no. of x2 tabulated points
    	private double[][] y = null;  	                        // y=f(x1,x2) tabulated function
    	private double[] x1 = null;   	                        // x1 in tabulated function f(x1,x2)
    	private double[] x2 = null;                             // x2 in tabulated function f(x1,x2)
    	private CubicSplineShort csn[] = null;                  // nPoints array of CubicSpline instances
    	private CubicSplineShort csm = null;                    // CubicSpline instance


    	// Constructor
    	// Constructor with data arrays initialised to arrays x and y
    	public BiCubicSplineShort(double[] x1, double[] x2, double[][] y){
        	this.nPoints=x1.length;
        	this.mPoints=x2.length;

        	this.csm = new CubicSplineShort(this.nPoints);
        	this.csn = CubicSplineShort.oneDarray(this.nPoints, this.mPoints);
        	this.x1 = new double[this.nPoints];
        	this.x2 = new double[this.mPoints];
        	this.y = new double[this.nPoints][this.mPoints];
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
        	}

        	double[] yTempn = new double[mPoints];

	    	for(int i=0; i<this.nPoints; i++){
	        	for(int j=0; j<mPoints; j++)yTempn[j]=y[i][j];
	        	this.csn[i].resetData(x2,yTempn);
	        	this.csn[i].calcDeriv();
	    	}
    	}

    	//  METHOD

    	//	Returns an interpolated value of y for a value of x
    	//  	from a tabulated function y=f(x1,x2)
    	public double interpolate(double xx1, double xx2){

	    	double[] yTempm = new double[this.nPoints];

	    	for (int i=0;i<this.nPoints;i++){
		    	yTempm[i]=this.csn[i].interpolate(xx2);
	    	}
	    	this.csm.resetData(x1,yTempm);
	    	return this.csm.interpolate(xx1);
    	}
}

