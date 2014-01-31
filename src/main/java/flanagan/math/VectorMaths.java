/*  Class VectorMaths
*
*   Defines a vector and includes the methods needed
*   for standard vector manipulations, e.g. addition,
*   dot product, cross product and related procedures.
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	November - December 2011
*   UPDATES:    5 December 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/VectorMaths.html
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

import flanagan.circuits.Phasor;

public class VectorMaths{
   
    Point point0 = null;                                    // origin coordinates
    Point point1 = null;                                    // final coordinates
    int nDimensionsEntered = 0;                             // dimensions of the cartesian space
   
    // CONSTRUCTORS
    // final point coordinates provided as array
    // initial point - cartesian origin
    public VectorMaths(double[] coord){
        this.setVector(coord);  
    }
    
    // coordinates provided as array
    // coord0 = initial point, coord1 = final point
    public VectorMaths(double[] coord0, double[] coord1){  
        this.setVector(coord0, coord1); 
    }
    
    // final coordinates provided as x, y and z values
    // initial point - cartesian origin
    public VectorMaths(double xPoint, double yPoint, double zPoint){
        this.setVector(xPoint, yPoint, zPoint); 
    }
    
    // final coordinates provided as x and y values (z set to 0.0)
    // initial point - cartesian origin
    public VectorMaths(double xPoint, double yPoint){
        this.setVector(xPoint, yPoint);
    }
    
    // final coordinates provided as x value (y and z set to 0.0)
    // initial point - cartesian origin
    public VectorMaths(double xPoint){
        this.setVector(xPoint);
    }
    
    // final coordinates provided as a Point
    // initial point - cartesian origin
    public VectorMaths(Point point1){
        this.setVector(point1);
    }
    
    // coordinates provided as Point
    // initial point - point0, final point - point1
    public VectorMaths(Point point0, Point point1){ 
        this.setVector(point0, point1);
    }
    
    // SET VECTOR COORDINATES
    // coordinates provided as array
    // initial point - cartesian origin
    public void setVector(double[] coord){
        this.nDimensionsEntered = coord.length;
        if(this.nDimensionsEntered>3)throw new IllegalArgumentException("VectorMaths will not handle dimensional spaces greater than 3-dimensional");
        double[] ccc1 = new double[3];
        for(int i=0; i<this.nDimensionsEntered; i++)ccc1[i] = coord[i];
        for(int i=this.nDimensionsEntered; i<3; i++)ccc1[i] = 0.0;
        this.point1 = new Point(ccc1);  
        double[] hold = new double[3];
        this.point0 = new Point(hold);  
    }
    
    // coordinates provided as array
    // coord0 = initial point, coord1 = final point
    public void setVector(double[] coord0, double[] coord1){  
        this.nDimensionsEntered = coord0.length;  
        int n = coord1.length;
        if(n!=this.nDimensionsEntered)throw new IllegalArgumentException("The two dimensions, " + this.nDimensionsEntered + " and " + n + ", should be equal"); 
        if(this.nDimensionsEntered>3)throw new IllegalArgumentException("VectorMaths will not handle dimensional spaces greater than 3-dimensional");
        double[] ccc0 = new double[3];
        double[] ccc1 = new double[3];
        for(int i=0; i<this.nDimensionsEntered; i++){
            ccc0[i] = coord0[i];
            ccc1[i] = coord1[i];
        }
        for(int i=this.nDimensionsEntered; i<3; i++){
            ccc0[i] = 0.0;
            ccc1[i] = 0.0;
        }
        this.point0 = new Point(ccc0); 
        this.point1 = new Point(ccc1);  
    }
    
    // final coordinates provided as x, y and z values
    // initial point - cartesian origin
    public void setVector(double xPoint, double yPoint, double zPoint){
        this.point1 = new Point(xPoint, yPoint, zPoint);  
        this.nDimensionsEntered = 3;
        double[] hold = {0.0, 0.0, 0.0};
        this.point0 = new Point(hold);
    }
    
    // final coordinates provided as x and y values (z set to 0.0)
    // initial point - cartesian origin
    public void setVector(double xPoint, double yPoint){
        this.point1 = new Point(xPoint, yPoint, 0.0);  
        this.nDimensionsEntered = 2;
        double[] hold = {0.0, 0.0, 0.0};
        this.point0 = new Point(hold);
    }
    
    // final coordinates provided as x value (y and z set to 0.0)
    // initial point - cartesian origin
    public void setVector(double xPoint){
        this.point1 = new Point(xPoint, 0.0, 0.0);  
        this.nDimensionsEntered = 1;
        double[] hold = {0.0, 0.0, 0.0};
        this.point0 = new Point(hold);
    }
    
    // final coordinates provided as a Point
    // initial point - cartesian origin
    public void setVector(Point point1){
        int n = point1.getPointDimensions();
        if(n>3)throw new IllegalArgumentException("VectorMaths will not handle dimensional spaces greater than 3-dimensional");
        this.nDimensionsEntered = n;
        if(n==3){
            this.point1 = point1;
        }
        else{
            double[] ccc0 = point1.getPointCoordinates();
            double[] ccc1 = new double[3];
            for(int i=0; i<n; i++)ccc1[i] = ccc0[i];
            for(int i=n; i<3; i++)ccc1[i] = 0.0;
            this.point1 = new Point(ccc1);
        }
        double[] cc = new double[3];
        for(int i=0; i<3; i++)cc[i] = 0.0;
        this.point0 = new Point(cc);
    }
    
    // coordinates provided as Point
    // initial point - point0, final point - point1
    public void setVector(Point point0, Point point1){ 
        this.nDimensionsEntered = point0.getPointDimensions();  
        int n = point1.getPointDimensions();
        if(n!=this.nDimensionsEntered)throw new IllegalArgumentException("The dimensions of the two points, " + this.nDimensionsEntered + " and " + n + ", should be equal"); 
        if(this.nDimensionsEntered>3)throw new IllegalArgumentException("VectorMaths will not handle dimensional spaces greater than 3-dimensional");
        if(n==3){
            this.point0 = point0.copy();
            this.point1 = point1.copy();
        }
        else{
            double[] cccc0 = point0.getPointCoordinates();
            double[] cccc1 = point1.getPointCoordinates();
            double[] ccc0 = new double[3];
            double[] ccc1 = new double[3];
            for(int i=0; i<n; i++){
                ccc0[i] = cccc0[i];
                ccc1[i] = cccc1[i];
            }
            for(int i=n; i<3; i++){
                ccc0[i] = 0.0;
                ccc1[i] = 0.0;
            }
            this.point0 = new Point(ccc0);
            this.point1 = new Point(ccc1);
        }
    }
    
    // GET VECTOR Points
    public Point[] getVector(){
        Point[] vec = Point.oneDarray(2);
        vec[0] = point0;
        vec[1] = point1;
        return vec; 
    }
    
    // GET INITIAL POINT COORDINATES
    public Point getInitialPoint(){
        return this.point0; 
    }
    
    // GET FINAL POINT COORDINATES
    public Point getFinalPoint(){
        return this.point1; 
    }
    
    // GET DIMENSIONS
    public int getDimensionsEntered(){
        return this.nDimensionsEntered; 
    }
    
    // DEEP COPY
    // Instance method
    public VectorMaths copy(){
        Point point00 = this.point0.copy();
        Point point11 = this.point1.copy();   
        VectorMaths vm = new VectorMaths(point00, point11);
        vm.nDimensionsEntered = this.nDimensionsEntered; 
        return vm;
    }
   
    // EQUALITY TESTS
    // Test for identical vectors
    // Instance method - returns true if the two vectors are identical - false if they are not
    public boolean isEqual(VectorMaths vec2){
       boolean test = true;
       Point p2p1 = vec2.getInitialPoint();
       Point p2p2 = vec2.getFinalPoint();
       if(!this.point0.isEqual(p2p1)){
           test = false;
       }
       else{
           if(!this.point1.isEqual(p2p2)){
               test = false;
           }
       }          
       return test;     
    }
    
    // Test for identical vectors
    // Static method - returns true if the two vectors are identical - false if they are not
    public static boolean isEqual(VectorMaths vec1, VectorMaths vec2){
       boolean test = true;
       Point p1p1 = vec1.getInitialPoint();
       Point p1p2 = vec1.getFinalPoint();
       Point p2p1 = vec2.getInitialPoint();
       Point p2p2 = vec2.getFinalPoint();
       if(!p1p1.isEqual(p2p1)){
           test = false;
       }
       else{
           if(!p1p2.isEqual(p2p2)){
               test = false;
           }
       }          
       return test;     
    }   
   
    // ADDITION
    public VectorMaths plus(VectorMaths vec2){
       double[] p1p1 = this.getInitialPoint().getPointCoordinates();
       double[] p1p2 = this.getFinalPoint().getPointCoordinates();
       double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
       double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
       int n0 = 3;
       double[] p3p1 = new double[n0];
       double[] p3p2 = new double[n0];
       for(int i=0; i<n0; i++){
           p3p1[i] = p1p1[i] + p2p1[i]; 
           p3p2[i] = p1p2[i] + p2p2[i]; 
       }
       return new VectorMaths(p3p1, p3p2);
   }
   
    public static VectorMaths plus(VectorMaths vec1, VectorMaths vec2){
       double[] p1p1 = vec1.getInitialPoint().getPointCoordinates();
       double[] p1p2 = vec1.getFinalPoint().getPointCoordinates();
       double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
       double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
       int n0 = 3;
       double[] p3p1 = new double[n0];
       double[] p3p2 = new double[n0];
       for(int i=0; i<n0; i++){
           p3p1[i] = p1p1[i] + p2p1[i]; 
           p3p2[i] = p1p2[i] + p2p2[i]; 
       }
       return new VectorMaths(p3p1, p3p2);
   }
    
    public void plusEquals(VectorMaths vec2){
       double[] p1p1 = this.getInitialPoint().getPointCoordinates();
       double[] p1p2 = this.getFinalPoint().getPointCoordinates();
       double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
       double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
       int n0 = 3;
       double[] p3p1 = new double[n0];
       double[] p3p2 = new double[n0];
       for(int i=0; i<n0; i++){
           p3p1[i] = p1p1[i] + p2p1[i]; 
           p3p2[i] = p1p2[i] + p2p2[i]; 
       }
       this.setVector(p3p1, p3p2);
   }

   // SUBTRACTION
   public VectorMaths minus(VectorMaths vec2){
       double[] p1p1 = this.getInitialPoint().getPointCoordinates();
       double[] p1p2 = this.getFinalPoint().getPointCoordinates();
       double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
       double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
       int n0 = 3;
       double[] p3p1 = new double[n0];
       double[] p3p2 = new double[n0];
       for(int i=0; i<n0; i++){
           p3p1[i] = p1p1[i] - p2p1[i]; 
           p3p2[i] = p1p2[i] - p2p2[i]; 
       }
       return new VectorMaths(p3p1, p3p2);
   }
   
    public static VectorMaths minus(VectorMaths vec1, VectorMaths vec2){
       double[] p1p1 = vec1.getInitialPoint().getPointCoordinates();
       double[] p1p2 = vec1.getFinalPoint().getPointCoordinates();
       double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
       double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
       int n0 = 3;
       double[] p3p1 = new double[n0];
       double[] p3p2 = new double[n0];
       for(int i=0; i<n0; i++){
           p3p1[i] = p1p1[i] - p2p1[i]; 
           p3p2[i] = p1p2[i] - p2p2[i]; 
       }
       return new VectorMaths(p3p1, p3p2);
   }
    
    public void minusEquals(VectorMaths vec2){
        double[] p1p1 = this.getInitialPoint().getPointCoordinates();
        double[] p1p2 = this.getFinalPoint().getPointCoordinates();
        double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
        double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
        int n0 = 3;
        double[] p3p1 = new double[n0];
        double[] p3p2 = new double[n0];
        for(int i=0; i<n0; i++){
            p3p1[i] = p1p1[i] - p2p1[i]; 
            p3p2[i] = p1p2[i] - p2p2[i]; 
        }
        this.setVector(p3p1, p3p2);
    }

    // MULTIPLY BY A SCALAR
    // Multiply by a scalar - instance method
    public VectorMaths times(double constant){
        int n =3;
        double[] cc0 = new double[n];
        double[] cc1 = new double[n];
        double[] ccc0 = this.getInitialPoint().getPointCoordinates();
        double[] ccc1 = this.getInitialPoint().getPointCoordinates();
        for(int i=0; i<n; i++){
            cc0[i] = ccc0[i] + constant;
            cc1[i] = ccc1[i] + constant;
        }
        return new VectorMaths(cc0, cc1);
    }
    
    // Multiply by a scalar - static method
    public static VectorMaths times(VectorMaths vec, double constant){
        int n = 3;
        double[] cc0 = new double[n];
        double[] cc1 = new double[n];
        double[] ccc0 = vec.getInitialPoint().getPointCoordinates();
        double[] ccc1 = vec.getInitialPoint().getPointCoordinates();
        for(int i=0; i<n; i++){
            cc0[i] = ccc0[i] + constant;
            cc1[i] = ccc1[i] + constant;
        }
        return new VectorMaths(cc0, cc1);
    }
   
     // Multiply by a scalar: *= equivalent
    public void timesEquals(double constant){
        int n = 3;
        double[] cc0 = new double[n];
        double[] cc1 = new double[n];
        double[] ccc0 = this.getInitialPoint().getPointCoordinates();
        double[] ccc1 = this.getInitialPoint().getPointCoordinates();
        for(int i=0; i<n; i++){
            cc0[i] = ccc0[i] + constant;
            cc1[i] = ccc1[i] + constant;
        }
        this.setVector(cc0, cc1);
    }
    
    // DOT PRODUCT
    // Instance method
    public double dot(VectorMaths vec2){
        
        double[] p1p1 = this.getInitialPoint().getPointCoordinates();
        double[] p1p2 = this.getFinalPoint().getPointCoordinates();
        double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
        double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
        double sum = 0.0;
        for(int i=0; i<3; i++){
            sum += (p1p2[i] - p1p1[i])*(p2p2[i] - p2p1[i]);
        }
        return sum;
    }    
        
    // Dot Product - static method
    public static double dot(VectorMaths vec1, VectorMaths vec2){
   
        double[] p1p1 = vec1.getInitialPoint().getPointCoordinates();
        double[] p1p2 = vec1.getFinalPoint().getPointCoordinates();
        double[] p2p1 = vec2.getInitialPoint().getPointCoordinates();
        double[] p2p2 = vec2.getFinalPoint().getPointCoordinates();
        double sum = 0.0;
        for(int i=0; i<3; i++){
            sum += (p1p2[i] - p1p1[i])*(p2p2[i] - p2p1[i]);
        }
        return sum;
    }    
    
    // CROSS PRODUCT
    // Returns the cross product for three dimensional space
    // instance method
    public VectorMaths cross(VectorMaths vec2){
        
        double[] pp1p1 = this.getInitialPoint().getPointCoordinates();
        double[] pp1p2 = this.getFinalPoint().getPointCoordinates();
        double[] pp2p1 = vec2.getInitialPoint().getPointCoordinates();
        double[] pp2p2 = vec2.getFinalPoint().getPointCoordinates();

        double[] q1 = new double[3];
        double[] q2 = new double[3];
        double[] q3 = new double[3];
        
        for(int i=0; i<3; i++){
            q1[i] = pp1p2[i] - pp1p1[i]; 
            q2[i] = pp2p2[i] - pp2p1[i]; 
        }
        
        for(int i=0; i<3; i++){
            q3[0] = q1[1]*q2[2] - q1[2]*q2[1];
            q3[1] = q1[2]*q2[0] - q1[0]*q2[2];
            q3[2] = q1[0]*q2[1] - q1[1]*q2[0];
        }
        VectorMaths vec3 = new VectorMaths(q3);
        return vec3;
    }
    
    // Returns the cross product for three dimensional space
    // static method
    public static VectorMaths cross(VectorMaths vec1, VectorMaths vec2){
  
        double[] pp1p1 = vec1.getInitialPoint().getPointCoordinates();
        double[] pp1p2 = vec1.getFinalPoint().getPointCoordinates();
        double[] pp2p1 = vec2.getInitialPoint().getPointCoordinates();
        double[] pp2p2 = vec2.getFinalPoint().getPointCoordinates();
        
        double[] q1 = new double[3];
        double[] q2 = new double[3];
        double[] q3 = new double[3];
        
        for(int i=0; i<3; i++){
            q1[i] = pp1p2[i] - pp1p1[i]; 
            q2[i] = pp2p2[i] - pp2p1[i]; 
        }
        
        for(int i=0; i<3; i++){
            q3[0] = q1[1]*q2[2] - q1[2]*q2[1];
            q3[1] = q1[2]*q2[0] - q1[0]*q2[2];
            q3[2] = q1[0]*q2[1] - q1[1]*q2[0];
        }
        VectorMaths vec3 = new VectorMaths(q3);
        return vec3;
    }
    
    // LENGTH
    public double length(){
        double[] p1 = this.getInitialPoint().getPointCoordinates();
        double[] p2 = this.getFinalPoint().getPointCoordinates();

        double sum = 0;
        for(int i=0; i<3; i++){
            sum += (p2[i] - p1[i])*(p2[i] - p1[i]);
        }
        return Math.sqrt(sum);
    }
    
    public double magnitude(){
        return this.length();
    }
    
    public double norm(){
        return this.length();
    }
    
    // ANGLE
    // Returns the angle between two vectors as radians
    // Instance method
    public double angleRadians(VectorMaths vec2){
        double norm1 = this.norm();
        double norm2 = vec2.norm();
        double dotProduct = this.dot(vec2);
        double cosTheta = dotProduct/(norm1*norm2);
        return Math.acos(cosTheta);
    }
    
    // Returns the angle between two vectors as degrees
    // Instance method
    public double angleDegrees(VectorMaths vec2){
        return Math.toDegrees(this.angleRadians(vec2));
    }
    
    // Returns the angle between two vectors as radians
    // Static method
    public static double angleRadians(VectorMaths vec1, VectorMaths vec2){
        double norm1 = vec1.norm();
        double norm2 = vec2.norm();
        double dotProduct = VectorMaths.dot(vec1, vec2);
        double cosTheta = dotProduct/(norm1*norm2);
        return Math.acos(cosTheta);
    }
    
    // Returns the angle between two vectors as degrees
    // Static method
    public static double angleDegrees(VectorMaths vec1, VectorMaths vec2){
        return Math.toDegrees(VectorMaths.angleRadians(vec1, vec2));
    }
        
    // CONVERT TO Phasor
    // Only for 1D or 2D entered points
    public Phasor toPhasor(){
        double[] co0 = this.point0.getPointCoordinates();
        double[] co1 = this.point1.getPointCoordinates();
        double mag = 0.0;
        double phase = 0.0;
        switch(this.nDimensionsEntered){
            case 1: mag = Math.abs(co1[0] - co0[0]);
                    phase = 0.0;
                    break;
            case 2: mag = Point.distance(this.point0, this.point1);
                    phase = Math.toDegrees(Math.atan2((co1[1] - co0[1]),(co1[0] - co0[0])));
                    break;
            default: throw new IllegalArgumentException("Entered dimensions must be either 1 or 2");
        }
        return new Phasor(mag, phase);      
    }
    
    
    
}
