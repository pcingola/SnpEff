/*  Class Point
*
*   Defines a point in multidimensional space
*   Several methods using Points are only valid for 1, 2 or 3-dimensional space
* 
*   Required by the class Vector
*    
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	November - December 2011
*   UPDATES:    5 December 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Point.html
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

package flanagan.math;

import java.math.*;

public class Point{
    
    private double[] point = null;          // cartesian coordinates
    private int nDimensions = 0;            // number of dimensions  
    
    // CONSTUCTORS
    // 3D zero point
    public Point(){
        this.point = new double[3];
        this.point[0] = 0.0;
        this.point[1] = 0.0;
        this.point[2] = 0.0;
        this.nDimensions = 3;
    }
    
    // All coordinates supplid as an array of doubles
    public Point(double[] coordinates){
        this.setPoint(coordinates);
    }
    
    // All coordinates supplid as an array of float
    public Point(float[] coordinates){
        this.setPoint(coordinates);
    }
    
    // All coordinates supplid as an array of long
    public Point(long[] coordinates){
        this.setPoint(coordinates);
    }
    
        // All coordinates supplid as an array of int
    public Point(int[] coordinates){
        this.setPoint(coordinates);
    }
    
        // All coordinates supplid as an array of short
    public Point(short[] coordinates){
        this.setPoint(coordinates);
    }
    
    // All coordinates supplid as an array of BigDecimal
    public Point(BigDecimal[] coordinates){
        this.setPoint(coordinates);
    }
    
    // All coordinates supplid as an array of BigInteger
    public Point(BigInteger[] coordinates){
        this.setPoint(coordinates);
    }
    
    // 1D point
    public Point(double xPoint){
        this.point = new double[1];
        this.point[0] = xPoint;
        this.nDimensions = 1;
    }
    
    // 2D point
    public Point(double xPoint, double yPoint){
        this.point = new double[2];
        this.point[0] = xPoint;
        this.point[1] = yPoint;
        this.nDimensions = 2;
    }  

    // 3D point
    public Point(double xPoint, double yPoint, double zPoint){
        this.point = new double[3];
        this.point[0] = xPoint;
        this.point[1] = yPoint;
        this.point[2] = zPoint;
        this.nDimensions = 3;
    }   
    
    // RESET COORDINATES
    // All coordinates supplied as an array of doubles
    public final void setPoint(double[] coordinates){
        this.point = (double[])coordinates.clone(); 
        this.nDimensions = this.point.length;
    }
    
    // All coordinates supplied as an array of float
    public final void setPoint(float[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // All coordinates supplied as an array of long
    public final void setPoint(long[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // All coordinates supplied as an array of int
    public final void setPoint(int[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // All coordinates supplied as an array of short
    public final void setPoint(short[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // All coordinates supplied as an array of BigDecimal
    public final void setPoint(BigDecimal[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // All coordinates supplied as an array of Big Integer
    public final void setPoint(BigInteger[] coordinates){
        this.nDimensions = coordinates.length;
        ArrayMaths am = new ArrayMaths(coordinates);
        this.point = am.array_as_double(); 
    }
    
    // 1D point
    public void setPoint(double xPoint){
        this.point = new double[1];
        this.point[0] = xPoint;
        this.nDimensions = 1;
    }
    
    // 2D point
    public void setPoint(double xPoint, double yPoint){
        this.point = new double[2];
        this.point[0] = xPoint;
        this.point[1] = yPoint;
        this.nDimensions = 2;
    }  

    // 3D point
    public void setPoint(double xPoint, double yPoint, double zPoint){
        this.point = new double[3];
        this.point[0] = xPoint;
        this.point[1] = yPoint;
        this.point[2] = zPoint;
        this.nDimensions = 3;
    }   
    
    // ONE D ARRAY OF n POINTS 
    // all points are 3D zero points 
    public static Point[] oneDarray(int n){
        Point[] pa = new Point[n];
        for(int i=0; i<n; i++){
            pa[i] = new Point(0.0, 0.0, 0.0);
        }
        return pa;
    }
    
    // all points are 1D
    // value set to entered array
    public static Point[] oneDarray(double[] xx){
        int n = xx.length;
        Point[] pa = new Point[n];
        for(int i=0; i<n; i++){
            pa[i] = new Point(xx[i]);
        }
        return pa;
    }
    
    // all points are 2D
    // value set to entered arrays
    public static Point[] oneDarray(double[] xx, double[] yy){
        int n = xx.length;
        int m = yy.length;
        if(m!=n)throw new IllegalArgumentException("the length of the xx array, " + n + ", and the lengh of the yy array, " + m + ", must be equal");
        
        Point[] pa = new Point[n];
        for(int i=0; i<n; i++){
            pa[i] = new Point(xx[i], yy[i]);
        }
        return pa;
    }
       
    // all points are 3D
    // value set to entered arrays
    public static Point[] oneDarray(double[] xx, double[] yy, double[] zz){
        int n = xx.length;
        int m = yy.length;
        int l = zz.length;
        if(m!=n)throw new IllegalArgumentException("the length of the xx array, " + n + ", and the lengh of the yy array, " + m + ", must be equal");
        if(n!=l)throw new IllegalArgumentException("the length of the xx array, " + n + ", and the lengh of the zz array, " + l + ", must be equal");
      
        Point[] pa = new Point[n];
        for(int i=0; i<n; i++){
            pa[i] = new Point(xx[i], yy[i], zz[i]);
        }
        return pa;
    }
    
    // all points are the dimension entered
    // value set to entered arrays of doubles
    public static Point[] oneDarray(double[][] points){
        int n = points.length;
        int m = points[0].length;
        for(int i=1; i<n; i++){
            if(m!=points[i].length)throw new IllegalArgumentException("the dimesions of all the points must be identical");
        }
        Point[] pa = new Point[n];
        double[] cc = new double[m];
        for(int i=0; i<n; i++){
            for(int j=0; j<m; j++){
                cc[j] = points[i][j];
            } 
            pa[i] = new Point(cc);
        }
        return pa;
        
    }
    
    // all points are the dimension entered
    // value set to entered arrays of float
    public static Point[] oneDarray(float[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
    
    // all points are the dimension entered
    // value set to entered arrays of long
    public static Point[] oneDarray(long[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
    
    // all points are the dimension entered
    // value set to entered arrays of int
    public static Point[] oneDarray(int[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
    
    // all points are the dimension entered
    // value set to entered arrays of short
    public static Point[] oneDarray(short[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
    
    // all points are the dimension entered
    // value set to entered arrays of BigDecimal
    public static Point[] oneDarray(BigDecimal[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
    
    // all points are the dimension entered
    // value set to entered arrays of BigInteger
    public static Point[] oneDarray(BigInteger[][] points){
        int n = points.length;
        int m = points[0].length;
        double[][] hold = new double[n][m];
        ArrayMaths am = null;
        for(int i=0; i<n; i++){
            am = new ArrayMaths(points[i]);
            hold[i] = am.array_as_double();
        }
        return Point.oneDarray(hold);
    }
        
    // GET COORDINATES
    // Get Point coordinates
    public double[] getPointCoordinates(){
        return (double[])this.point.clone();
    }
    
    // Get array coordinates
    public static double[][] getArrayCoordinates(Point[] array){
        int n = array.length;
        int m = array[0].getPointDimensions();
        double[][] cc = new double[m][n];
        for(int i=0; i<n; i++){
           for(int j=0; j<m; j++){
               cc[j][i] = (array[i].getPointCoordinates())[j];
           }
        }
        return cc;
    }
        
    // GET DIMENSIONS
    // Get point dimension
    public int getPointDimensions(){
        return this.nDimensions;
    } 
    
    // Get array dimension
    public static int[] getArrayDimensions(Point[] array){
        int[] n = new int[2]; 
        n[0] = array.length;
        n[1] = array[0].getPointDimensions();
        return n;
    } 
    
    // DISTANCE BETWEEN POINTS
    // Distance between two points squared - instance method
    // Meaningful for 2 and 3-dimensaional space
    public double distanceSquared(Point p2){
        int n2 = p2.nDimensions;
        if(n2!=this.nDimensions)throw new IllegalArgumentException("The dimensions of the two points, " + this.nDimensions + " and " + n2 + ", should be equal");
        if(n2>3)System.out.println("Methods distance and distanceSquared are only meaningful for dimensions of 3 or less");
        double hold = 0.0;
        double sum = 0.0;
        for(int i=0; i<n2; i++){
            hold = this.point[i] - p2.point[i];
            sum += hold*hold;
        }
        return sum;
    }
    
    // Distance between two points squared  - static method
    // Meaningful for 2 and 3-dimensaional space
    public static double distanceSquared(Point p1, Point p2){
        int n1 = p1.nDimensions;
        int n2 = p2.nDimensions;
        if(n2!=n1)throw new IllegalArgumentException("The dimensions of the two points, " + n1 + " and " + n2 + ", should be equal");
        if(n2>3)System.out.println("Methods distance and distanceSquared are only meaningful for dimensions of 3 or less");
        double hold = 0.0;
        double sum = 0.0;
        for(int i=0; i<n2; i++){
            hold = p1.point[i] - p2.point[i];
            sum += hold*hold;
        }
        return sum;
    }
    
    // Distance between two points - instance method
    // Meaningful for 2 and 3-dimensaional space
    public double distance(Point p2){
        double dist2 = this.distanceSquared(p2);
        return Math.sqrt(dist2);
    }
    
    // Distance between two points squared - static method
    // Meaningful for 2 and 3-dimensaional space
    public static double distance(Point p1, Point p2){
        double dist2 = Point.distanceSquared(p1, p2);
        return Math.sqrt(dist2);
    }
   
    
    // CENTRE OF A CONSTELLATION OF POINTS
    public static Point centre(Point[] pp){
        int n = pp.length;
        int m = pp[0].nDimensions;
        int l = 0;
        for(int i=1; i<n; i++){
            l = pp[i].nDimensions;
            if(l!=m)throw new IllegalArgumentException("All points must have the same number of dimensions");
        }
        double[] cc = new double[m];
        for(int i=0; i<n; i++){
            for(int j=0; j<m; j++){
                cc[j] += pp[i].point[j];
            }
        }
        for(int j=0; j<m; j++)cc[j] /= n;
           
        return new Point(cc);
    }
 
    // SHIFT
    // Shift all point coordinates by a constant
    public Point shift(double constant){
        Point pp = new Point();
        for(int i=0; i<this.nDimensions; i++){
            pp.setPoint(this.point[i] + constant);
        }
        return null;
    }
    
    // EQUALITY TESTS
    // Equality test - returns true if the two points are identical - false if they are not
    // Instance method
    public boolean isEqual(Point point2){
        boolean test = true;
        int n1 = point2.getPointDimensions();
        if(n1!=this.nDimensions){
            test = false;
        }
        else{
            double[] p0 = this.getPointCoordinates();
            double[] p1 = point2.getPointCoordinates();
            for(int i=0; i<this.nDimensions; i++){
                if(p0[i]!=p1[i]){
                    test = false;
                    break;
                }
            }
        }
        
        return test;
    }
    
    // Equality test - returns true if the two points are identical - false if they are not
    // Static method
    public static boolean isEqual(Point point1, Point point2){
        boolean test = true;
        int n1 = point1.getPointDimensions();
        int n2 = point2.getPointDimensions();
        if(n1!=n2){
            test = false;
        }
        else{
            double[] p0 = point1.getPointCoordinates();
            double[] p1 = point2.getPointCoordinates();
            for(int i=0; i<n1; i++){
                if(p0[i]!=p1[i]){
                    test = false;
                    break;
                }
            }
        }
        return test;
    }
    
    // DEEP COPIES
    // Copy a Point - instance method
    public Point copy(){  
        double[] pc = new double[this.nDimensions];
        for(int i=0; i<this.nDimensions; i++)pc[i] = this.point[i];
        Point pp = new Point(pc);
        return pp;    
    }
    
   // Copy a Point - static method
    public static Point copy(Point p){  
        int n = p.getPointDimensions();
        double[] pc = new double[n];
        for(int i=0; i<n; i++)pc[i] = p.point[i];
        Point pp = new Point(pc);
        return pp;    
    }
            
    // Copy a one D array of Points - static method
    public static Point[] copy(Point[] pp){
        int n = pp.length;
        Point[] pq = Point.oneDarray(n);
        for(int i=0; i<n; i++){
            pq[i] = pp[i].copy();
        }
        return pq;
    }
    
    // CHANGE DIMENSIONS
    
    
    // Convert to 3D
    public void toThreeD(){
        double[] hold0 = (double[])this.point.clone();
        double[] hold1 = new double[3];
        if(this.nDimensions>3){
            System.out.println("Method toThreeD:  Dimensions are greater than three so instance cannot be convertd to a three D Point");
        }
        else{
            if(this.nDimensions<3){
                for(int i=0; i<this.nDimensions; i++)hold1[i] = hold0[i];
                for(int i=this.nDimensions; i<3; i++)hold1[i] = 0.0; 
                this.point = hold1;
                this.nDimensions = 3;
            }
        }
    }
    
    // Convert to 2D
    public void toTwoD(){
        double[] hold0 = (double[])this.point.clone();
        double[] hold1 = new double[2];
        boolean test1 = true;
        for(int i=2; i<this.nDimensions; i++){
            if(hold0[i]!=0.0)test1 = false;
        }
        if(test1){
            for(int i=0; i<2; i++)hold1[i] = hold0[i]; 
            this.point = hold1;
            this.nDimensions = 2;
        }
        else{
            throw new IllegalArgumentException("There are non-zero values in the coordinate positions greater than 2D");
        }
        
    }
    
    // CONVERT TO A VectorMaths
    public VectorMaths toVectorMaths(){
        if(this.nDimensions>3)throw new IllegalArgumentException("VectorMaths is restricted to 2 or 3-dimensional space");
        VectorMaths vec = new VectorMaths(this.point);
        return vec;
    }
}
