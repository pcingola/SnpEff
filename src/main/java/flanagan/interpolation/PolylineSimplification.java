/*  Class PolylineSimplification
*
*   Simplifies a polyline (a polygonal chain) using the algorithm of Douglas and Peucker
*   This is a Java implementation modelled on softSurfer's C++ implementation
*   See http://softsurfer.com/Archive/algorithm_0205/algorithm_0205.htm#Implementation
* 
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	November - December 2011
*   UPDATES:    5 December 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PolyLineSimplification.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2011  Michael Thomas Flanagan
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
import java.lang.reflect.Array;

import flanagan.math.*;
import flanagan.plot.*;

public class PolylineSimplification{
    
    private Point[] originalPoints = null;              // array of original points
    private int nPoints = 0;                            // number of original points
    private Point[] simplifiedPoints = null;            // array of final points
    private int[] simplifiedIndices = null;             // original indices of simplified curve points
    private int nSimplifiedPoints = 0;                  // number of final points
    private int pointDimension = 0;                     // Point dimension
    private double tolerance = 0.0;                     // tolerance required by Douglas Peucker algorithm
    private double toleranceSquared = 0.0;              // above tolerance squared
    private boolean tolerenceEntered = false;           // = true when tolerance entered
    private boolean simplifyDone = false;               // = true when simplification performed
  
    // CONSTRUCTORS 
    // Constructor - Polyline coordinates supplied as an array of Point
    public PolylineSimplification(Point[] points){
        this.originalPoints = points;
        int n[] = Point.getArrayDimensions(points);
        this.nPoints = n[0];
        this.pointDimension = n[1];
        if(this.pointDimension>3 || this.pointDimension<2)throw new IllegalArgumentException("This method will not operate on dimensions greater than 3");
        this.simplifyDone = false;
    }
    
    // Constructor - Polyline coordinates supplied as two 1-dimensional arrays of doubles
    public PolylineSimplification(double[] xpoints, double[] ypoints){
        this.pointDimension = 2;
        this.nPoints = xpoints.length;
        if(this.nPoints!=ypoints.length)throw new IllegalArgumentException("The number of x-coordinate points, " + this.nPoints + ", must equal the number of y-coordinate points, " + ypoints.length);
        this.originalPoints = Point.oneDarray(xpoints, ypoints);
        this.simplifyDone = false;    
    }
    
       // Constructor - Polyline coordinates supplied as three 1-dimensional arrays of doubles
    public PolylineSimplification(double[] xpoints, double[] ypoints, double[] zpoints){
        this.pointDimension = 3;
        this.nPoints = xpoints.length;
        if(this.nPoints!=ypoints.length)throw new IllegalArgumentException("The number of x-coordinate points, " + this.nPoints + ", must equal the number of y-coordinate points, " + ypoints.length);
        if(this.nPoints!=zpoints.length)throw new IllegalArgumentException("The number of x-coordinate points, " + this.nPoints + ", must equal the number of z-coordinate points, " + zpoints.length);
        this.originalPoints = Point.oneDarray(xpoints, ypoints, zpoints);
        this.simplifyDone = false;    
    }
    
    // Constructor - Polyline coordinates supplied as a multidimensional array of doubles
    public PolylineSimplification(double[][] points){
        this.pointDimension = points.length;
        this.nPoints = points[0].length;
        this.originalPoints = Point.oneDarray(points);
        if(this.pointDimension>3 || this.pointDimension<2)throw new IllegalArgumentException("This method will not operate on dimensions greater than 3");
        this.simplifyDone = false;    
    }
    
    // Constructor - Polyline coordinates supplied as a multidimensional array of doubles dclared as an Object
    public PolylineSimplification(Object points){
    	Object internalArrays = Fmath.copyObject(points);
    	this.nPoints = 1;
        while(!((internalArrays  =  Array.get(internalArrays, 0)) instanceof Double))this.nPoints++;
        double[][] hold = (double[][])points;
        this.pointDimension = hold.length;
        this.originalPoints = Point.oneDarray(hold);
        if(this.pointDimension>3 || this.pointDimension<2)throw new IllegalArgumentException("This method will not operate on dimensions greater than 3 or less than 2");
        this.simplifyDone = false;
    }
    
    // DOUGLAS PEUKER POLYLINE SIMPLIFICATION
    // Douglas Peucker polyline simplification with supplied tolerance - replaces any preset tolerance
    public Point[] douglasPeucker(double tolerance){
        this.tolerance = tolerance;
        this.toleranceSquared = tolerance*tolerance;
        this.tolerenceEntered = true;
        return douglasPeucker();  
    }
   
    // Douglas Peucker polyline simplification with a preset tolerance
    public Point[] douglasPeucker() {
        int i = 0;
        int k = 0;                                              // counters
        int toleratedCounter = 0;;                              // tolerated points counter
        Point[] buffer = Point.oneDarray(this.nPoints);         // vertex buffer
        int[] indicesBuffer = new int[this.nPoints];            // vertex index buffer
        boolean[] pointMarker = new boolean[this.nPoints];      // point marker buffer
        if(this.pointDimension==2)this.toThreeDimO();           // 2D to 3D to facilitate the use of VectorMaths

        // STAGE 1.  Vertex Reduction within the supplied tolerance
        buffer[0] = this.originalPoints[0].copy(); 
        indicesBuffer[0] = 0;
        for (i = k = 1; i < this.nPoints; i++) {
            if(Point.distanceSquared(this.originalPoints[i], this.originalPoints[toleratedCounter]) < this.toleranceSquared)continue;
            buffer[k] = this.originalPoints[i].copy();
            indicesBuffer[k] = i;
            toleratedCounter = i;
            k++;
        }
        if (toleratedCounter < this.nPoints - 1){
            buffer[k] = this.originalPoints[this.nPoints - 1].copy();
            indicesBuffer[k] = this.nPoints - 1;
            k++;
        }   
        
        // STAGE 2.  Douglas-Peucker polyline simplification
        pointMarker[0] = true;
        pointMarker[k - 1] = true;       // mark the first and last vertices
        douglasPeuckerSimplificationRoutine(buffer, 0, k - 1, pointMarker);

        // collect marked verics to a Point array
        ArrayList<Point> results = new ArrayList<Point>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (i = 0; i < k; i++) {
            if (pointMarker[i]){
                results.add(buffer[i]);
                indices.add(new Integer(indicesBuffer[i]));
            }
        }
        this.nSimplifiedPoints = results.size();
        this.simplifiedPoints = Point.oneDarray(this.nSimplifiedPoints);
        this.simplifiedIndices = new int[this.nSimplifiedPoints];
        for(i=0; i<this.nSimplifiedPoints; i++){
            this.simplifiedPoints[i] = results.get(i);
            this.simplifiedIndices[i] = ((Integer)indices.get(i)).intValue();
        }
        
        // Remove duplicate points
        Point[] hold0 = Point.copy(this.simplifiedPoints);
        int[] hold1 = (int[])this.simplifiedIndices.clone();
        for(i=0; i<this.nSimplifiedPoints-1; i++){
            for(int j=i+1; j<this.nSimplifiedPoints; j++){
                if(Point.isEqual(hold0[i], hold0[j])){
                    for(int p=j; p<this.nSimplifiedPoints-1; p++){
                        hold0[p] = hold0[p+1];
                        hold1[p] = hold1[p+1];
                    }
                    this.nSimplifiedPoints--;
                }
            }
        }
        this.simplifiedPoints = Point.oneDarray(this.nSimplifiedPoints);
        //this.simplifiedIndices = new int[this.nSimplifiedPoints];
        for(i=0; i<this.nSimplifiedPoints; i++){
            this.simplifiedPoints[i] = hold0[i];
            this.simplifiedIndices[i] = hold1[i];
        }
        
        // Return to entered dimensions
        if(this.pointDimension==2){
            this.toTwoDimO();
            this.toTwoDimS();
        }
        this.simplifyDone = true;
        return this.simplifiedPoints;         
    }

    // The Douglas-Peucker recursive simplification routine
    private void douglasPeuckerSimplificationRoutine(Point[] bpoints, int j, int k, boolean[] pointMarker){
      if (k <= j + 1)return;                    // there is nothing to simplify
        
      // check for adequate approximation by segment S from v[j] to v[k]
      int farthesti = j;                        // index of vertex farthest from S
      double farthestDistance2 = 0.0;           // distance squared of farthest vertex
      Point[] segment = Point.oneDarray(2);     // define segment
      segment[0] = bpoints[j];
      segment[1] = bpoints[k];
      VectorMaths vjk = new VectorMaths(bpoints[j], bpoints[k]);
      double dist2jk = bpoints[j].distanceSquared(bpoints[k]);
      VectorMaths seg0i = null;
      Point perplBase = null;               // base of perpendicular point under consideration to S
      double lratio = 0.0;
      double svang = 0.0; 
      double dist2ps = 0.0;                 // dv2 = distance of point under consideration to to S squared

      for (int i = j + 1; i < k; i++) {
        seg0i = new VectorMaths(segment[0],bpoints[i]);
        svang = seg0i.dot(vjk);
        if (svang <= 0){
            dist2ps = Point.distanceSquared(bpoints[i], segment[0]);
        }
        else{
            if (dist2jk <= svang){
                dist2ps = Point.distanceSquared(bpoints[i], segment[1]);
            }
            else{
                lratio = svang/dist2jk;
                VectorMaths vlr = vjk.times(lratio);
                VectorMaths s0vlr = (new VectorMaths(segment[0])).plus(vlr);
                perplBase = s0vlr.getFinalPoint();
                dist2ps = Point.distanceSquared(bpoints[i], perplBase);
            }
        }
        // test with current max distance squared
        if (dist2ps <= farthestDistance2)continue;
        // v[i] is a new max vertex
        farthesti = i;
        farthestDistance2 = dist2ps;
      }
      if (farthestDistance2 > this.toleranceSquared){        // error is worse than the tolerance
        // split the polyline at the farthest vertex from S
        pointMarker[farthesti] = true;      
        // recursively simplify the two subpolylines
        douglasPeuckerSimplificationRoutine(bpoints, j, farthesti, pointMarker);  
        douglasPeuckerSimplificationRoutine(bpoints, farthesti, k, pointMarker); 
      }
    }
    
    // Fill out original points to 3D to facilitate use of VectorMaths
    private void toThreeDimO(){
        for(int i=0; i<this.nPoints; i++)this.originalPoints[i].toThreeD();
    }
    
    // Return original data points to 2D
    private void toTwoDimO(){
        for(int i=0; i<this.nPoints; i++)this.originalPoints[i].toTwoD();
    }
    
    // Return simplified data points to 2D
    private void toTwoDimS(){
        for(int i=0; i<this.nSimplifiedPoints; i++)this.simplifiedPoints[i].toTwoD();
    }
    
    //  Get the simplified polyline as an array of double[]s
    public double[][] simplifiedCurveCoordinates(){
        if(!this.simplifyDone){
            if(!this.tolerenceEntered){
                throw new IllegalArgumentException("No tolerance has been entered");
            }
            else{
                this.douglasPeucker();
            }
        }
        return Point.getArrayCoordinates(this.simplifiedPoints);
    }
    
    //  Get the simplified polyline as an array of Point
    public Point[] simplifiedCurve(){
        if(!this.simplifyDone){
            if(!this.tolerenceEntered){
                throw new IllegalArgumentException("No tolerance has been entered");
            }
            else{
                this.douglasPeucker();
            }
        }
        return this.simplifiedPoints;
    }
    
    // Get the number of simplified curve points
    public int numberOfSimplifiedCurvePoints(){
        return this.nSimplifiedPoints;
    }
   
    // Get the original indices of simplified curve points
    public int[] simplifiedCurveIndices(){
        return this.simplifiedIndices;
    }
    
    // TOLERANCE
    // Set the tolerance required by the Douglas Peucker algorithm
    public void setTolerance(double tolerance){
        this.tolerance = tolerance;
        this.toleranceSquared = tolerance*tolerance;
        this.tolerenceEntered = true;
    }
      
    // Get the tolerance required by the Douglas Peucker algorithm
    public double getTolerance(){
        if(!this.tolerenceEntered)System.out.println("No tolerance has been entered; 0.0 returned");
        return this.tolerance;
    }
        
    // PLOT 
    // Only for 2D simplifications
    // Plots both the original and the simplified curves
    public void plot(){
        if(this.pointDimension!=2)throw new IllegalArgumentException("Plot will only function for an array of 2D points");
        double[][] cc0 = (double[][])(Point.getArrayCoordinates(this.originalPoints));
        double[][] cc1 = (double[][])this.simplifiedCurveCoordinates();
        double[][] data = new double[4][];
        data[0] = cc0[0];
        data[1] = cc0[1];
        data[2] = cc1[0];
        data[3] = cc1[1];
        PlotGraph pg = new PlotGraph(data);
        int[] lineOpt = {3,3};
        pg.setLine(lineOpt);
        pg.setXaxisLegend("x-coordinate");
        pg.setYaxisLegend("y-coordinate");
        pg.setGraphTitle("Polyline Simplification: tolerance = "+this.tolerance);
        pg.setGraphTitle2("circles = original data, squares = simplified curve");
        pg.plot();
        
        
        
        
        
    }
        
}
