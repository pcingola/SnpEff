/*
*   Class   Phasor
*
*   Defines a Phasor and includes
*   the methods needed for standard Phasor arithmetic
*
*   See PhasorMatrix for creation and manipulatioin of matrices of phasors
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    4 July 2007
*   AMENDED: 17 april 2008, 1-5 December 2011
*
*   DOCUMENTATION:
*   See Michael T Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Phasor.html
*
*   Copyright (c) 2007 - 2008    Michael Thomas Flanagan
*
* PERMISSION TO COPY:
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

package flanagan.circuits;

import flanagan.complex.Complex;
import flanagan.math.Fmath;
import flanagan.math.VectorMaths;

public class Phasor{

        private double magnitude = 0.0D;            // magnitude of the Phasor
        private double phaseInDeg = 0.0D;           // phase of the Phasor in radians
        private double phaseInRad = 0.0D;           // phase of the Phasor in degrees
        private Complex rectangular = new Complex(0.0, 0.0);    // rectangular complex equivalent of the Phasor

        // frequency - static to prevent inappropriate combination of phasors
        private static double frequency = Double.NaN;   // frequency in Hz
        private static double omega = Double.NaN;       // radial frequency



        // CONSTRUCTORS
        // default constructor
        public Phasor(){
        }

        // Constructor setting magnitude and phase in degrees
        public Phasor(double magnitude, double phase){
                this.magnitude = magnitude;
                this.phaseInDeg = phase;
                this.phaseInRad = Math.toRadians(phase);
                this.rectangular.polar(this.magnitude, this.phaseInRad);
        }

        // Constructor setting magnitude only
        public Phasor(double magnitude){
                this.magnitude = magnitude;
                this.rectangular.polar(this.magnitude, this.phaseInRad);
        }

        // SET VALUES
        // Set the frequency in Hz - as a static variable
        public static void setFrequency(double freq){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = freq;
                    Phasor.omega = 2.0D*Math.PI*freq;

                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + freq);
                }
        }

        // Set the radial frequency - as a static variable
        public static void setRadialFrequency(double omega){
                if(Fmath.isNaN(Phasor.omega)){
                    Phasor.omega = omega;
                    Phasor.frequency = Phasor.omega/(2.0D*Math.PI);
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the radial frequency, omega, " + Phasor.omega + ", that differs from the one you are now attempting to enter, " + omega);
                }
        }

        // Set the value of magnitude
        public void setMagnitude(double magnitude){
                this.magnitude = magnitude;
                this.rectangular.polar(this.magnitude, this.phaseInRad);
        }

        // Set the value of phase in degrees
        public void setPhaseInDegrees(double phase){
                this.phaseInDeg = phase;
                this.phaseInRad = Math.toRadians(phase);
                this.rectangular.polar(this.magnitude, this.phaseInRad);
        }


        // Set the values of magnitude and phase in degrees
        public void reset(double magnitude, double phaseInDegrees){
                this.magnitude = magnitude;
                this.phaseInDeg = phaseInDegrees;
                this.phaseInRad = Math.toRadians(phaseInDegrees);
                this.rectangular.polar(this.magnitude, this.phaseInRad);
        }


        // GET VALUES
        // Get the frequency in Hz
        public static double getFrequency(){
                return Phasor.frequency;
        }

        // Get the radial frequency
        public static double setRadialFrequency(){
                return Phasor.omega;
        }

        // Get the value of magnitude
        public double getMagnitude(){
                return this.magnitude;
        }

        // Get the value of phase in degrees
        public double getPhaseInDegrees(){
                return this.phaseInDeg;
        }

        // Get the value of phase in radians
        public double getPhaseInRadians(){
                return this.phaseInRad;
        }

        // Get the real part of the Phasor
        public double getReal(){
            return this.magnitude*Math.cos(this.phaseInRad);
        }

        // Get the imaginary part of the Phasor
        public double getImag(){
            return this.magnitude*Math.sin(this.phaseInRad);
        }

        // CONVERSIONS

        // converts rectangular complex variable to a Phasor
        public static Phasor toPhasor(Complex cc){
                Phasor ph = new Phasor();
                ph.magnitude = cc.abs();
                ph.phaseInRad = cc.argRad();
                ph.phaseInDeg = cc.argDeg();
                ph.rectangular = cc;
                return ph;
        }

        // converts the Phasor to a rectangular complex variable
        public Complex toRectangular(){
                Complex cc = new Complex();
                cc.polar(this.magnitude, this.phaseInRad);
                return cc;
        }

        // converts the Phasor to a rectangular complex variable - static method
        public static Complex toRectangular(Phasor ph){
                Complex cc = new Complex();
                cc.polar(ph.magnitude, ph.phaseInRad);
                return cc;
        }

        // converts the Phasor to a rectangular complex variable
        public Complex toComplex(){
                Complex cc = new Complex();
                cc.polar(this.magnitude, this.phaseInRad);
                return cc;
        }

        // converts the Phasor to a rectangular complex variable - static method
        public static Complex toComplex(Phasor ph){
                Complex cc = new Complex();
                cc.polar(ph.magnitude, ph.phaseInRad);
                return cc;
        }
        
        // converts the phasor to VectorMaths vector = instance method
        public VectorMaths toVectorMaths(){
            double x = this.magnitude*Math.sin(this.phaseInRad); 
            double y = this.magnitude*Math.cos(this.phaseInRad); 
            VectorMaths vec = new VectorMaths(x, y);
            return vec;
        }
        
        // converts the phasor to VectorMaths vector = instance method
        public static VectorMaths toVectorMaths(Phasor ph){
            double x = ph.magnitude*Math.sin(ph.phaseInRad); 
            double y = ph.magnitude*Math.cos(ph.phaseInRad); 
            VectorMaths vec = new VectorMaths(x, y);
            return vec;
        }

        // Format a phasor number as a string, 'magnitude''<''phase''deg' - phase in degrees
        // Overides java.lang.String.toString()
        // instance method
        public String toString(){
                return this.magnitude + "<" + this.phaseInDeg + "deg";
        }

        // Format a phasor number as a string, 'magnitude''<''phase''deg' - phase in degrees
        // Overides java.lang.String.toString()
        // static method
        public static String toString(Phasor ph){
                return ph.magnitude + "<" + ph.phaseInDeg + "deg";
        }


        // Parse a string to obtain Phasor
        // accepts strings:
        // 'magnitude''<''phase' - phase in degrees
        // 'magnitude''L''phase' - phase in degrees
        // 'magnitude''<''phase''deg' - phase in degrees
        // 'magnitude''L''phase''deg' - phase in degrees
        // 'magnitude''<''phase''rad' - phase in radians
        // 'magnitude''L''phase''rad' - phase in radians
        public static Phasor parsePhasor(String ss){
                Phasor ph = new Phasor();
                ss = ss.trim();
                int anglePos = ss.indexOf('<');
                if(anglePos==-1){
                    anglePos = ss.indexOf('L');
                    if(anglePos==-1)throw new IllegalArgumentException("no angle symbol, <, in the string, ss");
                }
                int degPos = ss.indexOf('d');
                if(degPos==-1)degPos = ss.indexOf('D');
                int radPos = ss.indexOf('r');
                if(radPos==-1)degPos = ss.indexOf('R');
                String mag = ss.substring(0,anglePos);
                ph.magnitude = Double.parseDouble(mag);
                String phas = null;
                if(degPos!=-1){
                    phas = ss.substring(anglePos+1, degPos);
                    ph.phaseInDeg = Double.parseDouble(mag);
                    ph.phaseInRad = Math.toRadians(ph.phaseInDeg);
                }
                if(degPos==-1 && radPos==-1){
                    phas = ss.substring(anglePos+1);
                    ph.phaseInDeg = Double.parseDouble(phas);
                    ph.phaseInRad = Math.toRadians(ph.phaseInDeg);
                }
                if(radPos!=-1){
                    phas = ss.substring(anglePos+1, radPos);
                    ph.phaseInRad = Double.parseDouble(phas);
                    ph.phaseInDeg = Math.toDegrees(ph.phaseInRad);
                }
                ph.rectangular.polar(ph.magnitude, ph.phaseInRad);

                return ph;
        }

        // Same method as parsePhasor
        // Overides java.lang.Object.valueOf()
        public static Phasor valueOf(String ss){
                return Phasor.parsePhasor(ss);
        }

        // INPUT AND OUTPUT

        // READ A PHASOR
        // Read a Phasor from the keyboard console after a prompt message
        // in a String format compatible with Phasor.parse,
        // 'magnitude'<'phase', 'magnitude'<'phase'deg, 'magnitude'<'phase'rad
        // e.g. 1.23<34.1deg, -0.67<-56.7, 6.8e2<-0.22rad
        // prompt = Prompt message to vdu
        public static final synchronized Phasor readPhasor(String prompt){
                int ch = ' ';
                String phstring = "";
                boolean done = false;

                System.out.print(prompt + " ");
                System.out.flush();

                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n')
                                        done = true;
                                else
                                        phstring = phstring + (char) ch;
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Phasor.parsePhasor(phstring);
        }

        // Read a Phasor from the keyboard console after a prompt message (with String default option)
        // in a String format compatible with Phasor.parse,
        // 'magnitude'<'phase', 'magnitude'<'phase'deg, 'magnitude'<'phase'rad
        // e.g. 1.23<34.1deg, -0.67<-56.7, 6.8e2<-0.22rad
        // prompt = Prompt message to vdu
        // dflt = default value
        public static final synchronized Phasor readPhasor(String prompt, String dflt){
                int ch = ' ';
                String phstring = "";
                boolean done = false;

                System.out.print(prompt + " [default value = " + dflt + "]  ");
                System.out.flush();

                int i=0;
                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n' || (char)ch =='\r'){
                                        if(i==0){
                                            phstring = dflt;
                                            if((char)ch == '\r')ch = System.in.read();
                                        }
                                        done = true;
                                }
                                else{
                                        phstring = phstring + (char) ch;
                                        i++;
                                 }
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Phasor.parsePhasor(phstring);
        }

        // Read a Phasor from the keyboard console after a prompt message (with Phasor default option)
        // in a String format compatible with Phasor.parse,
        // 'magnitude'<'phase', 'magnitude'<'phase'deg, 'magnitude'<'phase'rad
        // e.g. 1.23<34.1deg, -0.67<-56.7, 6.8e2<-0.22rad
        // prompt = Prompt message to vdu
        // dflt = default value
        public static final synchronized Phasor readPhasor(String prompt, Phasor dflt)
        {
                int ch = ' ';
                String phstring = "";
                boolean done = false;

                System.out.print(prompt + " [default value = " + dflt + "]  ");
                System.out.flush();

                int i=0;
                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n' || (char)ch =='\r'){
                                        if(i==0){
                                            if((char)ch == '\r')ch = System.in.read();
                                            return dflt;
                                        }
                                        done = true;
                                }
                                else{
                                        phstring = phstring + (char) ch;
                                        i++;
                                 }
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Phasor.parsePhasor(phstring);
        }

        // Read a Phasor from the keyboard console without a prompt message
        // in a String format compatible with Phasor.parse,
        // 'magnitude'<'phase', 'magnitude'<'phase'deg, 'magnitude'<'phase'rad
        // e.g. 1.23<34.1deg, -0.67<-56.7, 6.8e2<-0.22rad
        public static final synchronized Phasor readPhasor(){
                int ch = ' ';
                String phstring = "";
                boolean done = false;

                System.out.print(" ");
                System.out.flush();

                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n')
                                        done = true;
                                else
                                        phstring = phstring + (char) ch;
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Phasor.parsePhasor(phstring);
        }


        // PRINT A PHASOR
        // Print to terminal window with text (message) and a line return
        public void println(String message){
                System.out.println(message + " " + this.toString());
        }

        // Print to terminal window without text (message) but with a line return
        public void println(){
                System.out.println(" " + this.toString());
        }

        // Print to terminal window with text (message) but without line return
        public void print(String message){
                System.out.print(message + " " + this.toString());
        }

        // Print to terminal window without text (message) and without line return
        public void print(){
                System.out.print(" " + this.toString());
        }

        // PRINT AN ARRAY OF PHASORS
        // Print an array to terminal window with text (message) and a line return
        public static void println(String message, Phasor[] aa){
            System.out.println(message);
            for(int i=0; i<aa.length; i++){
                    System.out.println(aa[i].toString() + "  ");
            }
        }

        // Print an array to terminal window without text (message) but with a line return
        public static void println(Phasor[] aa){
            for(int i=0; i<aa.length; i++){
                    System.out.println(aa[i].toString() + "  ");
            }
        }

        // Print an array to terminal window with text (message) but no line returns except at the end
        public static void print(String message, Phasor[] aa){
            System.out.print(message+ " ");
            for(int i=0; i<aa.length; i++){
                    System.out.print(aa[i].toString() + "   ");
            }
            System.out.println();
        }

        // Print an array to terminal window without text (message) but with no line returns except at the end
        public static void print(Phasor[] aa){
            for(int i=0; i<aa.length; i++){
                    System.out.print(aa[i].toString() + "  ");
            }
            System.out.println();
        }

        // TRUNCATION
        // Rounds the mantissae of both the magnitude and phase to prec places - instance
        public Phasor truncate(int prec){
                if(prec<0)return this;

                double xMa = this.magnitude;
                double xPd = this.phaseInDeg;
                double xPr = this.phaseInRad;
                Complex xRect = this.rectangular;

                Phasor y = new Phasor();

                y.magnitude = Fmath.truncate(xMa, prec);
                y.phaseInDeg = Fmath.truncate(xPd, prec);
                y.phaseInRad = Fmath.truncate(xPr, prec);
                y.rectangular = Complex.truncate(xRect, prec);

                return y;
        }

        // Rounds the mantissae of both the magnitude and phase to prec places - static
        public static Phasor truncate(Phasor ph, int prec){
                if(prec<0)return ph;

                double xMa = ph.magnitude;
                double xPd = ph.phaseInDeg;
                double xPr = ph.phaseInRad;
                Complex xRect = ph.rectangular;

                Phasor y = new Phasor();

                y.magnitude = Fmath.truncate(xMa, prec);
                y.phaseInDeg = Fmath.truncate(xPd, prec);
                y.phaseInRad = Fmath.truncate(xPr, prec);
                y.rectangular = Complex.truncate(xRect, prec);

                return y;
        }


        // HASH  CODE

        // Return a HASH CODE for the Phasor
        // Overides java.lang.Object.hashCode()
        public int hashCode(){
                long lmagnt = Double.doubleToLongBits(this.magnitude);
                long lphase = Double.doubleToLongBits(this.phaseInDeg);
                int hmagnt = (int)(lmagnt^(lmagnt>>>32));
                int hphase = (int)(lphase^(lphase>>>32));
                return 6*(hmagnt/10)+4*(hphase/10);
        }


        // ARRAYS

        // Create a one dimensional array of Phasors of length n
        // all magnitudes = 1 and all phases = 0
        public static Phasor[] oneDarray(int n){
                Phasor[] a = new Phasor[n];
                Phasor b = new Phasor();
                b.reset(1.0, 0.0);
                for(int i=0; i<n; i++){
                        a[i] = b;
                }
                return a;
        }

        // Create a one dimensional array of Phasor objects of length n
        // all magnitudes = a and all phases = b
        public static Phasor[] oneDarray(int n, double a, double b){
                Phasor[] phArray = new Phasor[n];
                Phasor ph = new Phasor();
                ph.reset(a, b);
                for(int i=0; i<n; i++){
                        phArray[i] = ph;
                }
                return phArray;
        }


        // Create a one dimensional array of Phasors of length n
        // all = the Phasor constant
        public static Phasor[] oneDarray(int n, Phasor constant){
                Phasor[] ph =new Phasor[n];
                for(int i=0; i<n; i++){
                        ph[i] = constant.copy();
                }
                return ph;
        }

        // Create a two dimensional array of Phasors of dimensions n and m
        // all magnitudes = unity and all phases = zero
        public static Phasor[][] twoDarray(int n, int m){
                Phasor[][] phArray = new Phasor[n][m];
                Phasor ph = new Phasor();
                ph.reset(1.0, 0.0);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                phArray[i][j] = ph;
                        }
                }
                return phArray;
        }

        // Create a two dimensional array of Phasors of dimensions n and m
        // all magnitudes = a and all phases = b
        public static Phasor[][] twoDarray(int n, int m, double a, double b){
                Phasor[][] phArray = new Phasor[n][m];
                Phasor ph = new Phasor();
                ph.reset(a, b);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                phArray[i][j] = ph;
                        }
                }
                return phArray;
        }

        // Create a two dimensional array of Phasors of dimensions n and m
        // all  =  the Phasor constant
        public static Phasor[][] twoDarray(int n, int m, Phasor constant){
                Phasor[][]phArray =new Phasor[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                phArray[i][j] = constant.copy();
                        }
                }
                return phArray;

        }

        // Create a three dimensional array of Phasorss of dimensions n,  m and l
        // all magnitudes = unity and all phaes = zero
        public static Phasor[][][] threeDarray(int n, int m, int l){
                Phasor[][][] phArray = new Phasor[n][m][l];
                Phasor ph = new Phasor();
                ph.reset(1.0, 0.0);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        phArray[i][j][k] = ph;
                                }
                        }
                }
                return phArray;
        }

        // Create a three dimensional array of Phasorss of dimensions n, m and l
        // all magnitudes = a and all phases = b
        public static Phasor[][][] threeDarray(int n, int m, int l, double a, double b){
                Phasor[][][] phArray = new Phasor[n][m][l];
                Phasor ph = new Phasor();
                ph.reset(a, b);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        phArray[i][j][k] = ph;
                                }
                        }
                }
                return phArray;
        }

        // Create a three dimensional array of Phasors of dimensions n, m and l
        // all  =  the Phasor constant
        public static Phasor[][][] threeDarray(int n, int m, int l, Phasor constant){
                Phasor[][][] phArray = new Phasor[n][m][l];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        phArray[i][j][k] = constant.copy();
                                }
                        }
                }
                return phArray;
        }

        // COPY

        // Copy a single Phasor  [instance method]
        public Phasor copy(){
            if(this==null){
                return null;
            }
            else{
                Phasor b = new Phasor();
                b.magnitude = this.magnitude;
                b.phaseInDeg = this.phaseInDeg;
                b.phaseInRad = this.phaseInRad;
                return b;
            }
        }

        // Copy a single Phasor  [static method]
        public static Phasor copy(Phasor ph){
            if(ph==null){
                return null;
            }
            else{
                Phasor b = new Phasor();
                b.magnitude = ph.magnitude;
                b.phaseInDeg = ph.phaseInDeg;
                b.phaseInRad = ph.phaseInRad;
                return b;
            }
        }

        // Copy a 1D array of Phasors (deep copy)
        public static Phasor[] copy(Phasor[] a){
            if(a==null){
                return null;
            }
            else{
                int n =a.length;
                Phasor[] b = Phasor.oneDarray(n);
                for(int i=0; i<n; i++){
                        b[i] = a[i].copy();
                }
                return b;
            }
        }

        // Copy a 2D array of Phasors (deep copy)
        public static Phasor[][] copy(Phasor[][] a){
            if(a==null){
                return null;
            }
            else{
                int n =a.length;
                int m =a[0].length;
                Phasor[][] b = Phasor.twoDarray(n, m);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                b[i][j] = a[i][j].copy();
                        }
                }
                return b;
            }
        }

        // Copy a 3D array of Phasors (deep copy)
        public static Phasor[][][] copy(Phasor[][][] a){
            if(a==null){
                return null;
            }
            else{
                int n = a.length;
                int m = a[0].length;
                int l = a[0][0].length;
                Phasor[][][] b = Phasor.threeDarray(n, m, l);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        b[i][j][k] = a[i][j][k].copy();
                                }
                        }
                }
                return b;
            }
        }

        // CLONE
        // Overrides Java.Object method clone
        // Copy a single Phasor
        public Object clone(){
            Object ret = null;

            if(this!=null){
                Phasor b = new Phasor();
                b.magnitude = this.magnitude;
                b.phaseInDeg = this.phaseInDeg;
                b.phaseInRad = this.phaseInRad;
                ret = (Object)b;
            }

            return ret;
        }

        // ADDITION

        // Add a Phasor to this Phasor
        // this Phasor remains unaltered
        public Phasor plus(Phasor ph){
                Complex com1 = this.toRectangular();
                Complex com2 = ph.toRectangular();
                Complex com3 = com1.plus(com2);
                return Phasor.toPhasor(com3);
        }

        // Add a complex number to this Phasor
        // this Phasor remains unaltered
        public Phasor plus(Complex com1){
                Phasor ph = new Phasor();
                Complex com2 = this.toRectangular();
                Complex com3 = com1.plus(com2);
                return Phasor.toPhasor(com3);
        }

        // Add a Phasor to this Phasor and replace this with the sum
        public void plusEquals(Phasor ph1 ){
                Complex com1 = this.toRectangular();
                Complex com2 = ph1.toRectangular();
                Complex com3 = com1.plus(com2);
                Phasor ph2 = Phasor.toPhasor(com3);
                this.magnitude = ph2.magnitude;
                this.phaseInDeg = ph2.phaseInDeg;
                this.phaseInRad = ph2.phaseInRad;
        }

        // Add complex number to this Phasor and replace this with the sum
        public void plusEquals(Complex com1 ){
                Complex com2 = this.toRectangular();
                Complex com3 = com1.plus(com2);
                Phasor ph2 = Phasor.toPhasor(com3);
                this.magnitude += ph2.magnitude;
                this.phaseInDeg += ph2.phaseInDeg;
                this.phaseInRad += ph2.phaseInRad;
        }

        // SUBTRACTION

        // Subtract a Phasor from this Phasor
        // this Phasor remains unaltered
        public Phasor minus(Phasor ph){
                Complex com1 = this.toRectangular();
                Complex com2 = ph.toRectangular();
                Complex com3 = com1.minus(com2);
                return Phasor.toPhasor(com3);
        }

        // Subtract a complex number from this Phasor
        // this Phasor remains unaltered
        public Phasor minus(Complex com1){
                Phasor ph = new Phasor();
                Complex com2 = this.toRectangular();
                Complex com3 = com1.minus(com2);
                return Phasor.toPhasor(com3);
        }

       // Subtract a Phasor from this Phasor and replace this with the difference
        public void minusEquals(Phasor ph1 ){
                Complex com1 = this.toRectangular();
                Complex com2 = ph1.toRectangular();
                Complex com3 = com1.plus(com2);
                Phasor ph2 = Phasor.toPhasor(com3);
                this.magnitude = ph2.magnitude;
                this.phaseInDeg = ph2.phaseInDeg;
                this.phaseInRad = ph2.phaseInRad;
        }

        // Subtract a complex number from this Phasor and replace this with the difference
        public void minusEquals(Complex com1 ){
                Complex com2 = this.toRectangular();
                Complex com3 = com1.plus(com2);
                Phasor ph2 = Phasor.toPhasor(com3);
                this.magnitude = ph2.magnitude;
                this.phaseInDeg = ph2.phaseInDeg;
                this.phaseInRad = ph2.phaseInRad;
        }

        // MULTIPLICATION

        // Multiplies a Phasor by this Phasor
        // this Phasor remains unaltered
        public Phasor times(Phasor ph1){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude*ph1.magnitude;
                double pha = this.phaseInDeg  + ph1.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Multiplies this Phasor by a Complex number
        // this Phasor remains unaltered
        public Phasor times(Complex com1){
                Phasor ph1 = Phasor.toPhasor(com1);
                Phasor ph2 = new Phasor();
                double mag = this.magnitude*ph1.magnitude;
                double pha = this.phaseInDeg  + ph1.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Multiplies this Phasor by a double
        // this Phasor remains unaltered
        public Phasor times(double constant){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude*constant;
                double pha = this.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Multiplies this Phasor by an int
        // this Phasor remains unaltered
        public Phasor times(int constant){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude*constant;
                double pha = this.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Multiplies this Phasor by exp(omega.time)
        // this Phasor remains unaltered
        public Phasor timesExpOmegaTime(double omega, double time){
                if(Fmath.isNaN(Phasor.omega)){
                    Phasor.omega = omega;
                    Phasor.frequency = Phasor.omega/(2.0D*Math.PI);
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the radial frequency, omega, " + Phasor.omega + ", that differs from the one you are now attempting to enter, " + omega);
                }
                Phasor ph2 = new Phasor();
                ph2.reset(this.magnitude, this.phaseInDeg + Math.toDegrees(omega*time));
                return ph2;
        }

        // Multiplies this Phasor by exp(2.pi.frequency.time)
        // this Phasor remains unaltered
        public Phasor timesExpTwoPiFreqTime(double frequency, double time){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Phasor ph2 = new Phasor();
                ph2.reset(this.magnitude, this.phaseInDeg + Math.toDegrees(2.0D*Math.PI*frequency*time));
                return ph2;
        }

        // Multiply a Phasor by this Phasor and replace this with the product
        public void timesEquals(Phasor ph1 ){
                this.magnitude *= ph1.magnitude;
                this.phaseInDeg += ph1.phaseInDeg;
                this.phaseInRad += ph1.phaseInRad;
        }

        // Multiply a complex number by this Phasor and replace this with the product
        public void timesEquals(Complex com1 ){
                Phasor ph1 = Phasor.toPhasor(com1);
                this.magnitude *= ph1.magnitude;
                this.phaseInDeg += ph1.phaseInDeg;
                this.phaseInRad += ph1.phaseInRad;
        }

        // Multiply a double by this Phasor and replace this with the product
        public void timesEquals(double constant ){
                this.magnitude *= constant;
        }

        // Multiply an int by this Phasor and replace this with the product
        public void timesEquals(int constant ){
                this.magnitude *= (double)constant;
        }

        // Multiply exp(omega.time) by this Phasor and replace this with the product
        public void timesEqualsOmegaTime(double omega, double time ){
                if(Fmath.isNaN(Phasor.omega)){
                    Phasor.omega = omega;
                    Phasor.frequency = Phasor.omega/(2.0D*Math.PI);
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for radial frequency, omega, " + Phasor.omega + ", that differs from the one you are now attempting to enter, " + omega);
                }
                this.phaseInRad += omega*time;
                this.phaseInDeg = Math.toDegrees(this.phaseInRad);
        }

        // Multiply exp(2.pi.frequency.time) by this Phasor and replace this with the product
        public void timesEqualsTwoPiFreqTime(double frequency, double time ){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                this.phaseInRad += 2.0D*Math.PI*frequency*time;
                this.phaseInDeg = Math.toDegrees(this.phaseInRad);
        }

        // DIVISION

        // Divides this Phasor by a Phasor
        // this Phasor remains unaltered
        public Phasor over(Phasor ph1){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude/ph1.magnitude;
                double pha = this.phaseInDeg - ph1.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Divides this Phasor by a Complex number
        // this Phasor remains unaltered
        public Phasor over(Complex com1){
                Phasor ph1 = Phasor.toPhasor(com1);
                Phasor ph2 = new Phasor();
                double mag = this.magnitude/ph1.magnitude;
                double pha = this.phaseInDeg - ph1.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Divides this Phasor by a double
        // this Phasor remains unaltered
        public Phasor over(double constant){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude/constant;
                double pha = this.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Divides this Phasor by an int
        // this Phasor remains unaltered
        public Phasor over(int constant){
                Phasor ph2 = new Phasor();
                double mag = this.magnitude/constant;
                double pha = this.phaseInDeg;
                ph2.reset(mag, pha);
                return ph2;
        }

        // Divide this Phasor by a Phasor and replace this with the quotient
        public void overEquals(Phasor ph1 ){
                this.magnitude /= ph1.magnitude;
                this.phaseInDeg -= ph1.phaseInDeg;
                this.phaseInRad -= ph1.phaseInRad;
        }

        // Divide this Phasor by a complex number and replace this with the quotient
        public void overEquals(Complex com1 ){
                Phasor ph1 = Phasor.toPhasor(com1);
                this.magnitude /= ph1.magnitude;
                this.phaseInDeg -= ph1.phaseInDeg;
                this.phaseInRad -= ph1.phaseInRad;
        }

        // Divide this Phasor by a double and replace this with the quotient
        public void overEquals(double constant ){
                this.magnitude /= constant;
        }

        // Divide this Phasor by an int and replace this with the quotient
        public void overEquals(int constant ){
                this.magnitude /= (double)constant;
        }

        // FURTHER MATHEMATICAL FUNCTIONS

        // Return the absolute value of the magnitude
        // changes the sign of the magnitude
        public double abs(){
                return Math.abs(this.magnitude);
        }

        // Return the phase in radians
        // identical method to getPhaseInRadians()
        public double argInRadians(){
                return this.phaseInRad;
        }

        // Return the phase in degrees
        // identical method to getPhaseInDegrees()
        public double argInDegrees(){
                return this.phaseInDeg;
        }

        // negates a Phasor
        // changes the sign of the magnitude
        public Phasor negate(){
                Phasor ph = new Phasor();
                ph.reset(-this.magnitude, this.phaseInDeg);
                return ph;
        }

        // returns the complex conjugate of the Phasor
        public Phasor conjugate(){
                Phasor ph = new Phasor();
                ph.reset(this.magnitude, -this.phaseInDeg);
                return ph;
        }

        // inverts the Phasor
        public Phasor inverse(){
                Phasor ph = new Phasor();
                ph.reset(1.0D/this.magnitude, -this.phaseInDeg);
                return ph;
        }

        // Roots
        // square root of a Phasor
        public static Phasor sqrt(Phasor ph1){
                Phasor ph2 = new Phasor();
                ph2.reset(Math.sqrt(ph1.magnitude), ph1.phaseInDeg/2.0D);
                return ph2;
        }

        // nth root of a Phasor
        public static Phasor nthRoot(Phasor ph1, int n){
                if(n<=0)throw new IllegalArgumentException("The root, " + n + ", must be greater than zero");
                Phasor ph2 = new Phasor();
                ph2.reset(Math.pow(ph1.magnitude, 1.0/n), ph1.phaseInDeg/n);
                return ph2;
        }

        // Powers
        // square of a Phasor
        public static Phasor square(Phasor ph1){
                Phasor ph2 = new Phasor();
                ph2.reset(Fmath.square(ph1.magnitude), 2.0D*ph1.phaseInDeg);
                return ph2;
        }

        // nth power of a Phasor - int n
        public static Phasor pow(Phasor ph1, int n){
                Phasor ph2 = new Phasor();
                ph2.reset(Math.pow(ph1.magnitude, n), n*ph1.phaseInDeg);
                return ph2;
        }

        // nth power of a Phasor - double n
        public static Phasor pow(Phasor ph1, double n){
                Phasor ph2 = new Phasor();
                ph2.reset(Math.pow(ph1.magnitude, n), n*ph1.phaseInDeg);
                return ph2;
        }

        // nth power of a Phasor - Complex n
        public static Phasor pow(Phasor ph1, Complex n){
                Complex com1 = ph1.toRectangular();
                Complex com2 = Complex.pow(com1, n);
                Phasor ph2 = Phasor.toPhasor(com2);
                return ph2;
        }

        // nth power of a Phasor - Phasor n
        public static Phasor pow(Phasor ph1, Phasor n){
                Complex com1 = ph1.toRectangular();
                Complex comn = n.toRectangular();
                Complex com2 = Complex.pow(com1, comn);
                Phasor ph2 = Phasor.toPhasor(com2);
                return ph2;
        }


        // Exponential of a Phasor
        public static Phasor exp(Phasor ph1){
            Complex com = ph1.toRectangular();
            com = Complex.exp(com);
            Phasor ph2 = Phasor.toPhasor(com);
            return ph2;
        }

        // Natural log of a Phasor
        public static Phasor log(Phasor ph1){
            Complex com = new Complex(Math.log(ph1.magnitude), ph1.phaseInDeg);
            Phasor ph2 = Phasor.toPhasor(com);;
            return ph2;
        }

        // Trigonometric Functions
        // sine
        public Phasor sin(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.sin(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.sin(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // cosine
        public Phasor cos(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.cos(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.cos(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // tangent
        public Phasor tan(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.tan(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.tan(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // cotangent
        public Phasor cot(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.cot(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.cot(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // secant
        public Phasor sec(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.sec(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.sec(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // cosecant
        public Phasor csc(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.csc(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.csc(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // exssecant
        public Phasor exsec(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.exsec(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.exsec(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // versine
        public Phasor vers(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.vers(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.vers(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // coversine
        public Phasor covers(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.covers(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.covers(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // haversine
        public Phasor hav(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.hav(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.hav(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // hyperbolic sine
        public Phasor sinh(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.sinh(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.sinh(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // hyperbolic cosine
        public Phasor cosh(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.cosh(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.cosh(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // hyperbolic secant
        public Phasor sech(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.sech(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.sech(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // hyperbolic cosecant
        public Phasor csch(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.csch(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.csch(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }
        // Inverse Trigonometric Functions
        // inverse sine
        public Phasor asin(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.asin(ph1.getMagnitude()), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.asin(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse cosine
        public Phasor acos(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.acos(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acos(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse tangent
        public Phasor atan(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Math.atan(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.atan(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse cotangent
        public Phasor acot(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.acot(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acot(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse secant
        public Phasor asec(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.asec(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.asec(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse cosecant
        public Phasor acsc(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.acsc(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acsc(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse exssecant
        public Phasor aexsec(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.aexsec(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.aexsec(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse versine
        public Phasor avers(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.avers(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.avers(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse coversine
        public Phasor acovers(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.acovers(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acovers(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse haversine
        public Phasor ahav(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.ahav(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.ahav(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse hyperbolic sine
        public Phasor asinh(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.asinh(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.asinh(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse hyperbolic cosine
        public Phasor acosh(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.acosh(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acosh(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse hyperbolic secant
        public Phasor asech(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.asech(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.asech(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }

        // inverse hyperbolic cosecant
        public Phasor acsch(Phasor ph1){
            Phasor ph2 = new Phasor();
            if(ph1.phaseInDeg==0.0){
                ph2.reset(Fmath.acsch(ph1.magnitude), 0.0D);
            }
            else{
                Complex com = ph1.toRectangular();
                com = Complex.acsch(com);
                ph2 = Phasor.toPhasor(com);
            }

            return ph2;
        }


        // LOGICAL FUNCTIONS
        // Returns true if the Phasor has a zero phase, i.e. is a real number
        public boolean isReal(){
                boolean test = false;
                if(Math.abs(this.phaseInDeg)==0.0D)test = true;
                return test;
        }

        // Returns true if the Phasor has a zero magnitude
        // or a phase equal to minus infinity
        public boolean isZero(){
                boolean test = false;
                if(Math.abs(this.magnitude)==0.0D || this.phaseInDeg==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }

        // Returns true if either the magnitude or the phase of the Phasor
        // is equal to plus infinity
        public boolean isPlusInfinity(){
                boolean test = false;
                if(this.magnitude==Double.POSITIVE_INFINITY || this.phaseInDeg==Double.POSITIVE_INFINITY)test = true;
                return test;
        }


        // Returns true if the magnitude of the Phasor
        // is equal to minus infinity
        public boolean isMinusInfinity(){
                boolean test = false;
                if(this.magnitude==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }

        // Returns true if the Phasor is NaN (Not a Number)
        // i.e. is the result of an uninterpretable mathematical operation
        public boolean isNaN(){
                boolean test = false;
                if(this.magnitude!=this.magnitude || this.phaseInDeg!=this.phaseInDeg)test = true;
                return test;
        }

        // Returns true if two Phasor are identical
        // Follows the Sun Java convention of treating all NaNs as equal
        // i.e. does not satisfies the IEEE 754 specification
        // but does let hashtables operate properly
        public boolean equals(Phasor a){
                boolean test = false;
                if(this.isNaN() && a.isNaN()){
                        test=true;
                }
                else{
                        if(this.magnitude == a.magnitude && this.phaseInDeg == a.phaseInDeg)test = true;
                }
                return test;
        }



        // returns true if the differences between the magnitudes and phases of two Phasors
        // are less than fract times the larger magnitude or phase
        public boolean equalsWithinLimits(Phasor a, double fract){

            boolean test = false;

            double mt = this.magnitude;
            double ma = a.magnitude;
            double pt = this.phaseInDeg;
            double pa = a.phaseInDeg;
            double mdn = 0.0D;
            double pdn = 0.0D;
            double mtest = 0.0D;
            double ptest = 0.0D;

            if(mt==0.0D && pt==0.0D && ma==0.0D && pa==0.0D)test=true;
            if(!test){
                mdn=Math.abs(mt);
                if(Math.abs(ma)>mdn)mdn=Math.abs(ma);
                if(mdn==0.0D){
                    mtest=0.0;
                }
                else{
                    mtest=Math.abs(ma-mt)/mdn;
                }
                pdn=Math.abs(pt);
                if(Math.abs(pa)>pdn)pdn=Math.abs(pa);
                if(pdn==0.0D){
                    ptest=0.0;
                }
                else{
                    ptest=Math.abs(pa-pt)/pdn;
                }
                if(mtest<fract && ptest<fract)test=true;
            }

            return test;
        }

        // SOME USEFUL NUMBERS
        // returns the number zero (0) as a Phasor
        // zero magnituded and zero phase
        public static Phasor zero(){
                Phasor ph = new Phasor();
                ph.magnitude = 0.0D;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular.polar(ph.magnitude, ph.phaseInRad);
                return ph;
        }

        // returns the number one (+1) as a Phasor
        // magnitude = 1, phase = zero
        public static Phasor plusOne(){
                Phasor ph = new Phasor();
                ph.magnitude = 1.0D;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular.polar(ph.magnitude, ph.phaseInRad);
                return ph;
        }

        // returns the number minus one (-1) as a Phasor
        // magnitude = -1, phase = zero
        public static Phasor minusOne(){
                Phasor ph = new Phasor();
                ph.magnitude = -1.0D;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular.polar(ph.magnitude, ph.phaseInRad);
                return ph;
        }

        // returns the a phasor of given magnitude with zero phase
        // magnitude = -1, phase = zero
        public static Phasor magnitudeZeroPhase(double mag){
                Phasor ph = new Phasor();
                ph.magnitude = mag;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular.polar(ph.magnitude, ph.phaseInRad);
                return ph;
        }

        // infinity
        // magnitude = plus infinity, phase = 0
        public static Phasor plusInfinity(){
                Phasor ph = new Phasor();
                ph.magnitude = Double.POSITIVE_INFINITY;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                return ph;
        }

        // -infinity
        // magnitude = minus infinity, phase = 0
        public static Phasor minusInfinity(){
                Phasor ph = new Phasor();
                ph.magnitude = Double.NEGATIVE_INFINITY;
                ph.phaseInDeg = 0.0D;
                ph.phaseInRad = 0.0D;
                ph.rectangular = new Complex(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
                return ph;
        }

        // Resistance as a phasor
        public static Phasor resistancePhasor(double resistance){
                Phasor ph = new Phasor(resistance);
                return ph;
        }

        // inductance as a phasor
        public static Phasor inductancePhasor(double inductance, double frequency){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Complex com = Impedance.inductanceImpedance(inductance, Phasor.omega);
                Phasor ph = new Phasor();
                return ph.toPhasor(com);
        }

        // capacitance as a phasor
        public static Phasor capacitancePhasor(double capacitance, double frequency){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Complex com = Impedance.capacitanceImpedance(capacitance, Phasor.omega);
                Phasor ph = new Phasor();
                return ph.toPhasor(com);
        }

        // infinite warburg impedance as a phasor
        public static Phasor infiniteWarburgPhasor(double sigma, double frequency){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Complex com = Impedance.infiniteWarburgImpedance(sigma, Phasor.omega);
                Phasor ph = new Phasor();
                return ph.toPhasor(com);
        }

        // finite warburg impedance as a phasor
        public static Phasor finiteWarburgPhasor(double sigma, double delta, double frequency){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Complex com = Impedance.finiteWarburgImpedance(sigma, delta, Phasor.omega);
                Phasor ph = new Phasor();
                return ph.toPhasor(com);
        }

        // constant phase elelemnt a phasor
        public static Phasor constantPhaseElementPhasor(double sigma, double alpha, double frequency){
                if(Fmath.isNaN(Phasor.frequency)){
                    Phasor.frequency = frequency;
                    Phasor.omega = Phasor.frequency*2.0D*Math.PI;
                }
                else{
                    throw new IllegalArgumentException("You have already entered a value for the frequency, " + Phasor.frequency + ", that differs from the one you are now attempting to enter, " + frequency);
                }
                Complex com = Impedance.constantPhaseElementImpedance(sigma, alpha, Phasor.omega);
                Phasor ph = new Phasor();
                return ph.toPhasor(com);
        }
}