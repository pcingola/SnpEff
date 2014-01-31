/*
*   Class   Complex
*
*   Defines a complex number as an object and includes
*   the methods needed for standard complex arithmetic
*
*   See class ComplexMatrix for complex matrix manipulations
*   See class ComplexPoly for complex polynomial manipulations
*   See class ComplexErrorProp for the error propogation in complex arithmetic
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    February 2002
*   UPDATED: 1 August 2006, 29 April 2007, 15,21,22 June 2007, 22 November 2007
*            20 May 2008, 26 August 2008, 9 November 2009, 6 june 2010
*
*   DOCUMENTATION:
*   See Michael T Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Complex.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2009    Michael Thomas Flanagan
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


package flanagan.complex;

import flanagan.math.Fmath;

public class Complex{

        private double real = 0.0D;         // Real part of a complex number
        private double imag = 0.0D;         // Imaginary part of a complex number
        private static char jori = 'j';     // i or j in a + j.b or a + i.b representaion
                                            // default value = j
        private static boolean infOption = true;  // option determining how infinity is handled
                                            // if true (default option):
                                            //  multiplication with either complex number with either part = infinity returns infinity
                                            //      unless the one complex number is zero in both parts
                                            //  division by a complex number with either part = infinity returns zero
                                            //      unless the dividend is also infinite in either part
                                            // if false:
                                            //      standard arithmetic performed


/*********************************************************/

        // CONSTRUCTORS
        // default constructor - real and imag = zero
        public Complex()
        {
                this.real = 0.0D;
                this.imag = 0.0D;
        }

        // constructor - initialises both real and imag
        public Complex(double real, double imag)
        {
                this.real = real;
                this.imag = imag;
        }

        // constructor - initialises  real, imag = 0.0
        public Complex(double real)
        {
                this.real = real;
                this.imag = 0.0D;
        }

        // constructor - initialises both real and imag to the values of an existing Complex
        public Complex(Complex c)
        {
                this.real = c.real;
                this.imag = c.imag;
        }

/*********************************************************/

        // PUBLIC METHODS

        // SET VALUES
        // Set the value of real
        public void setReal(double real){
        this.real = real;
        }
        // Set the value of imag
        public void setImag(double imag){
                this.imag = imag;
        }

        // Set the values of real and imag
        public void reset(double real, double imag){
                this.real = real;
                this.imag = imag;
        }

        // Set real and imag given the modulus and argument (in radians)
        public void polarRad(double mod, double arg){
                this.real = mod*Math.cos(arg);
                this.imag = mod*Math.sin(arg);
        }

        // Set real and imag given the modulus and argument (in radians)
        // retained for compatibility
        public void polar(double mod, double arg){
                this.real = mod*Math.cos(arg);
                this.imag = mod*Math.sin(arg);
        }

        // Set real and imag given the modulus and argument (in degrees)
        public void polarDeg(double mod, double arg){
                arg = Math.toRadians(arg);
                this.real = mod*Math.cos(arg);
                this.imag = mod*Math.sin(arg);
        }

        // GET VALUES
        // Get the value of real
        public double getReal(){
                return real;
        }

        // Get the value of imag
        public double getImag(){
                return imag;
        }

        // INPUT AND OUTPUT

        // READ A COMPLEX NUMBER
        // Read a complex number from the keyboard console after a prompt message
        // in a String format compatible with Complex.parse,
        // e.g 2+j3, 2 + j3, 2+i3, 2 + i3
        // prompt = Prompt message to vdu
        public static final synchronized Complex readComplex(String prompt)
        {
                int ch = ' ';
                String cstring = "";
                boolean done = false;

                System.out.print(prompt + " ");
                System.out.flush();

                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n')
                                        done = true;
                                else
                                        cstring = cstring + (char) ch;
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Complex.parseComplex(cstring);
        }

        // Read a complex number from the keyboard console after a prompt message (with String default option)
        // in a String format compatible with Complex.parse,
        // e.g 2+j3, 2 + j3, 2+i3, 2 + i3
        // prompt = Prompt message to vdu
        // dflt = default value
        public static final synchronized Complex readComplex(String prompt, String dflt)
        {
                int ch = ' ';
                String cstring = "";
                boolean done = false;

                System.out.print(prompt + " [default value = " + dflt + "]  ");
                System.out.flush();

                int i=0;
                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n' || (char)ch =='\r'){
                                        if(i==0){
                                            cstring = dflt;
                                            if((char)ch == '\r')ch = System.in.read();
                                        }
                                        done = true;
                                }
                                else{
                                        cstring = cstring + (char) ch;
                                        i++;
                                 }
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Complex.parseComplex(cstring);
        }

        // Read a complex number from the keyboard console after a prompt message (with Complex default option)
        // in a String format compatible with Complex.parse,
        // e.g 2+j3, 2 + j3, 2+i3, 2 + i3
        // prompt = Prompt message to vdu
        // dflt = default value
        public static final synchronized Complex readComplex(String prompt, Complex dflt)
        {
                int ch = ' ';
                String cstring = "";
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
                                        cstring = cstring + (char) ch;
                                        i++;
                                 }
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Complex.parseComplex(cstring);
        }



        // Read a complex number from the keyboard console without a prompt message
        // in a String format compatible with Complex.parse,
        // e.g 2+j3, 2 + j3, 2+i3, 2 + i3
        // prompt = Prompt message to vdu
        public static final synchronized Complex readComplex()
        {
                int ch = ' ';
                String cstring = "";
                boolean done = false;

                System.out.print(" ");
                System.out.flush();

                while (!done){
                        try{
                                ch = System.in.read();
                                if (ch < 0 || (char)ch == '\n')
                                        done = true;
                                else
                                        cstring = cstring + (char) ch;
                        }
                        catch(java.io.IOException e){
                                done = true;
                        }
                }
                return Complex.parseComplex(cstring);
        }

        // PRINT A COMPLEX NUMBER
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

        // PRINT AN ARRAY OF COMLEX NUMBERS
        // Print an array to terminal window with text (message) and a line return
        public static void println(String message, Complex[] aa){
            System.out.println(message);
            for(int i=0; i<aa.length; i++){
                    System.out.println(aa[i].toString() + "  ");
            }
        }

        // Print an array to terminal window without text (message) but with a line return
        public static void println(Complex[] aa){
            for(int i=0; i<aa.length; i++){
                    System.out.println(aa[i].toString() + "  ");
            }
        }

        // Print an array to terminal window with text (message) but no line returns except at the end
        public static void print(String message, Complex[] aa){
            System.out.print(message+ " ");
            for(int i=0; i<aa.length; i++){
                    System.out.print(aa[i].toString() + "   ");
            }
            System.out.println();
        }

        // Print an array to terminal window without text (message) but with no line returns except at the end
        public static void print(Complex[] aa){
            for(int i=0; i<aa.length; i++){
                    System.out.print(aa[i].toString() + "  ");
            }
            System.out.println();
        }

        // TRUNCATION
        // Rounds the mantissae of both the real and imaginary parts of Complex to prec places
        // Static method
        public static Complex truncate(Complex x, int prec){
                if(prec<0)return x;

                double xR = x.getReal();
                double xI = x.getImag();
                Complex y = new Complex();

                xR = Fmath.truncate(xR, prec);
                xI = Fmath.truncate(xI, prec);

                y.reset(xR, xI);

                return y;
        }

        // instance method
        public Complex truncate(int prec){
                if(prec<0)return this;

                double xR = this.getReal();
                double xI = this.getImag();
                Complex y = new Complex();

                xR = Fmath.truncate(xR, prec);
                xI = Fmath.truncate(xI, prec);

                y.reset(xR, xI);

                return y;
        }


        // CONVERSIONS
        // Format a complex number as a string, a + jb or a + ib[instance method]
        // < value of real > < + or - > < j or i> < value of imag >
        // Choice of j or i is set by Complex.seti() or Complex.setj()
        // j is the default option for j or i
        // Overides java.lang.String.toString()
        public String toString(){
                char ch='+';
                if(this.imag<0.0D)ch='-';
                return this.real+" "+ch+" "+jori+Math.abs(this.imag);
        }

        // Format a complex number as a string, a + jb or a + ib [static method]
        // See static method above for comments
        public static String toString(Complex aa){
                char ch='+';
                if(aa.imag<0.0D)ch='-';
                return aa.real+" "+ch+jori+Math.abs(aa.imag);
        }

        // Sets the representation of the square root of minus one to j in Strings
        public static void setj(){
                jori = 'j';
        }

        // Sets the representation of the square root of minus one to i in Strings
        public static void seti(){
                jori = 'i';
        }

        // Returns the representation of the square root of minus one (j or i) set for Strings
        public static char getjori(){
            return jori;
        }

        // Parse a string to obtain Complex
        // accepts strings 'real''s''sign''s''x''imag'
        // where x may be i or j and s may be no spaces or any number of spaces
        // and sign may be + or -
        // e.g.  2+j3, 2 + j3, 2+i3, 2 + i3
        public static Complex parseComplex(String ss){
                Complex aa = new Complex();
                ss = ss.trim();
                double first = 1.0D;
                if(ss.charAt(0)=='-'){
                    first = -1.0D;
                    ss = ss.substring(1);
                }

                int i = ss.indexOf('j');
                if(i==-1){
                        i = ss.indexOf('i');
                }
                if(i==-1)throw new NumberFormatException("no i or j found");

                int imagSign=1;
                int j = ss.indexOf('+');

                if(j==-1){
                j = ss.indexOf('-');
                if(j>-1) imagSign=-1;
                }
                if(j==-1)throw new NumberFormatException("no + or - found");

                int r0=0;
                int r1=j;
                int i0=i+1;
                int i1=ss.length();
                String sreal=ss.substring(r0,r1);
                String simag=ss.substring(i0,i1);
                aa.real=first*Double.parseDouble(sreal);
                aa.imag=imagSign*Double.parseDouble(simag);
                return aa;
        }

        // Same method as parseComplex
        // Overides java.lang.Object.valueOf()
        public static Complex valueOf(String ss){
                return Complex.parseComplex(ss);
        }

        // Return a HASH CODE for the Complex number
        // Overides java.lang.Object.hashCode()
        public int hashCode()
        {
                long lreal = Double.doubleToLongBits(this.real);
                long limag = Double.doubleToLongBits(this.imag);
                int hreal = (int)(lreal^(lreal>>>32));
                int himag = (int)(limag^(limag>>>32));
                return 7*(hreal/10)+3*(himag/10);
        }

        // SWAP
        // Swaps two complex numbers
        public static void swap(Complex aa, Complex bb){
                double holdAreal = aa.real;
                double holdAimag = aa.imag;
                aa.reset(bb.real, bb.imag);
                bb.reset(holdAreal, holdAimag);
        }


        // ARRAYS

        // Create a one dimensional array of Complex objects of length n
        // all real = 0 and all imag = 0
        public static Complex[] oneDarray(int n){
                Complex[] a =new Complex[n];
                for(int i=0; i<n; i++){
                        a[i]=Complex.zero();
                }
                return a;
        }

        // Create a one dimensional array of Complex objects of length n
        // all real = a and all imag = b
        public static Complex[] oneDarray(int n, double a, double b){
                Complex[] c =new Complex[n];
                for(int i=0; i<n; i++){
                        c[i]=Complex.zero();
                        c[i].reset(a, b);
                }
                return c;
        }

        // Arithmetic mean of a one dimensional array of complex numbers
        public static Complex mean(Complex[] aa){
                int n = aa.length;
                Complex sum = new Complex(0.0D, 0.0D);
                for(int i=0; i<n; i++){
                        sum = sum.plus(aa[i]);
                }
                return sum.over((double)n);
        }

        // Create a one dimensional array of Complex objects of length n
        // all = the Complex constant
        public static Complex[] oneDarray(int n, Complex constant){
                Complex[] c =new Complex[n];
                for(int i=0; i<n; i++){
                        c[i]=Complex.copy(constant);
                }
                return c;
        }

        // Create a two dimensional array of Complex objects of dimensions n and m
        // all real = zero and all imag = zero
        public static Complex[][] twoDarray(int n, int m){
                Complex[][] a =new Complex[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                a[i][j]=Complex.zero();
                        }
                }
                return a;
        }

        // Create a two dimensional array of Complex objects of dimensions n and m
        // all real = a and all imag = b
        public static Complex[][] twoDarray(int n, int m, double a, double b){
                Complex[][] c =new Complex[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                c[i][j]=Complex.zero();
                                c[i][j].reset(a, b);
                        }
                }
                return c;
        }

        // Create a two dimensional array of Complex objects of dimensions n and m
        // all  =  the Complex constant
        public static Complex[][] twoDarray(int n, int m, Complex constant){
                Complex[][] c =new Complex[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                c[i][j]=Complex.copy(constant);
                        }
                }
                return c;
        }

        // Create a three dimensional array of Complex objects of dimensions n,  m and l
        // all real = zero and all imag = zero
        public static Complex[][][] threeDarray(int n, int m, int l){
                Complex[][][] a =new Complex[n][m][l];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        a[i][j][k]=Complex.zero();
                                }
                        }
                }
                return a;
        }

        // Create a three dimensional array of Complex objects of dimensions n, m and l
        // all real = a and all imag = b
        public static Complex[][][] threeDarray(int n, int m, int l, double a, double b){
                Complex[][][] c =new Complex[n][m][l];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        c[i][j][k]=Complex.zero();
                                        c[i][j][k].reset(a, b);
                                }
                        }
                }
                return c;
        }

        // Create a three dimensional array of Complex objects of dimensions n, m and l
        // all  =  the Complex constant
        public static Complex[][][] threeDarray(int n, int m, int l, Complex constant){
                Complex[][][] c =new Complex[n][m][l];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        c[i][j][k]=Complex.copy(constant);
                                }
                        }
                }
                return c;
        }

        // COPY
        // Copy a single complex number [static method]
        public static Complex copy(Complex a){
            if(a==null){
                return null;
            }
            else{
                Complex b = new Complex();
                b.real=a.real;
                b.imag=a.imag;
                return b;
            }
        }

        // Copy a single complex number [instance method]
        public Complex copy(){
            if(this==null){
                return null;
            }
            else{
                Complex b = new Complex();
                b.real=this.real;
                b.imag=this.imag;
                return b;
            }
        }


        // Copy a 1D array of complex numbers (deep copy)
        // static metod
        public static Complex[] copy(Complex[] a){
            if(a==null){
                return null;
            }
            else{
                int n =a.length;
                Complex[] b = Complex.oneDarray(n);
                for(int i=0; i<n; i++){
                        b[i]=Complex.copy(a[i]);
                }
                return b;
            }
        }

        // Copy a 2D array of complex numbers (deep copy)
        public static Complex[][] copy(Complex[][] a){
            if(a==null){
                return null;
            }
            else{
                int n =a.length;
                int m =a[0].length;
                Complex[][] b = Complex.twoDarray(n, m);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                b[i][j]=Complex.copy(a[i][j]);
                        }
                }
                return b;
            }
        }

        // Copy a 3D array of complex numbers (deep copy)
        public static Complex[][][] copy(Complex[][][] a){
            if(a==null){
                return null;
            }
            else{
                int n = a.length;
                int m = a[0].length;
                int l = a[0][0].length;
                Complex[][][] b = Complex.threeDarray(n, m, l);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                for(int k=0; k<l; k++){
                                        b[i][j][k]=Complex.copy(a[i][j][k]);
                                }
                        }
                }
                return b;
            }
        }

        // CLONE
        // Overrides Java.Object method clone
        // Copy a single complex number [instance method]
        public Object clone(){
            Object ret = null;

            if(this!=null){
                    Complex b = new Complex();
                    b.real=this.real;
                    b.imag=this.imag;
                    ret = (Object)b;
            }

            return ret;
        }

        // ADDITION
        // Add two Complex numbers [static method]
        public static Complex plus(Complex a, Complex b){
                Complex c = new Complex();
                c.real=a.real+b.real;
                c.imag=a.imag+b.imag;
                return c;
        }

        // Add a double to a Complex number [static method]
        public static Complex plus(Complex a, double b){
                Complex c = new Complex();
                c.real=a.real+b;
                c.imag=a.imag;
                return c;
        }

        // Add a Complex number to a double [static method]
        public static Complex plus(double a, Complex b){
                Complex c = new Complex();
                c.real=a+b.real;
                c.imag=b.imag;
                return c;
        }

        // Add a double number to a double and return sum as Complex [static method]
        public static Complex plus(double a, double b){
                Complex c = new Complex();
                c.real=a+b;
                c.imag=0.0D;
                return c;
        }

        // Add a Complex number to this Complex number [instance method]
        // this Complex number remains unaltered
        public Complex plus(Complex a ){
                Complex b = new Complex();
                b.real=this.real + a.real;
                b.imag=this.imag + a.imag;
                return b;
        }

        // Add double number to this Complex number [instance method]
        // this Complex number remains unaltered
        public Complex plus(double a ){
                Complex b = new Complex();
                b.real = this.real + a;
                b.imag = this.imag;
                return b;
        }

        // Add a Complex number to this Complex number and replace this with the sum
        public void plusEquals(Complex a ){
                this.real+=a.real;
                this.imag+=a.imag;
        }

        // Add double number to this Complex number and replace this with the sum
        public void plusEquals(double a ){
                this.real+=a;
                this.imag=this.imag;
        }

        //  SUBTRACTION
        // Subtract two Complex numbers [static method]
        public static Complex minus (Complex a, Complex b){
                Complex c = new Complex();
                c.real=a.real-b.real;
                c.imag=a.imag-b.imag;
                return c;
        }

        // Subtract a double from a Complex number [static method]
        public static Complex minus(Complex a, double b){
                Complex c = new Complex();
                c.real=a.real-b;
                c.imag=a.imag;
                return c;
        }

        // Subtract a Complex number from a double [static method]
        public static Complex minus(double a, Complex b){
                Complex c = new Complex();
                c.real=a-b.real;
                c.imag=-b.imag;
                return c;
        }

        // Subtract a double number to a double and return difference as Complex [static method]
        public static Complex minus(double a, double b){
                Complex c = new Complex();
                c.real=a-b;
                c.imag=0.0D;
                return c;
        }

        // Subtract a Complex number from this Complex number [instance method]
        // this Complex number remains unaltered
        public Complex minus(Complex a ){
                Complex b = new Complex();
                b.real=this.real-a.real;
                b.imag=this.imag-a.imag;
                return b;
        }

        // Subtract a double number from this Complex number [instance method]
        // this Complex number remains unaltered
        public Complex minus(double a ){
                Complex b = new Complex();
                b.real=this.real-a;
                b.imag=this.imag;
                return b;
                }

        // Subtract this Complex number from a double number [instance method]
        // this Complex number remains unaltered
        public Complex transposedMinus(double a ){
                Complex b = new Complex();
                b.real=a - this.real;
                b.imag=this.imag;
                return b;
        }

        // Subtract a Complex number from this Complex number and replace this by the difference
        public void minusEquals(Complex a ){
                this.real-=a.real;
                this.imag-=a.imag;
        }

        // Subtract a double number from this Complex number and replace this by the difference
        public void minusEquals(double a ){
                this.real-=a;
                this.imag=this.imag;
        }

        // MULTIPLICATION
        // Sets the infinity handling option in multiplication and division
        // infOption -> true; standard arithmetic overriden - see above (instance variable definitions) for details
        // infOption -> false: standard arithmetic used
        public static void setInfOption(boolean infOpt){
                Complex.infOption = infOpt;
        }

        // Sets the infinity handling option in multiplication and division
        // opt = 0:   infOption -> true; standard arithmetic overriden - see above (instance variable definitions) for details
        // opt = 1:   infOption -> false: standard arithmetic used
        public static void setInfOption(int opt){
                if(opt<0 || opt>1)throw new IllegalArgumentException("opt must be 0 or 1");
                Complex.infOption = true;
                if(opt==1)Complex.infOption = false;
        }

        // Gets the infinity handling option in multiplication and division
        // infOption -> true; standard arithmetic overriden - see above (instance variable definitions) for details
        // infOption -> false: standard arithmetic used
        public static boolean getInfOption(){
                return Complex.infOption;
        }

        // Multiply two Complex numbers [static method]
        public static Complex times(Complex a, Complex b){
                Complex c = new Complex(0.0D, 0.0D);
                if(Complex.infOption){
                    if(a.isInfinite() && !b.isZero()){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                    if(b.isInfinite() && !a.isZero()){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                }

                c.real=a.real*b.real-a.imag*b.imag;
                c.imag=a.real*b.imag+a.imag*b.real;
                return c;
        }

        // Multiply a Complex number by a double [static method]
        public static Complex times(Complex a, double b){
                Complex c = new Complex();
                if(Complex.infOption){
                    if(a.isInfinite() && b!=0.0D){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                    if(Fmath.isInfinity(b) && !a.isZero()){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                }
                c.real=a.real*b;
                c.imag=a.imag*b;
                return c;
        }

        // Multiply a double by a Complex number [static method]
        public static Complex times(double a, Complex b){
                Complex c = new Complex();
                if(Complex.infOption){
                    if(b.isInfinite() && a!=0.0D){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                    if(Fmath.isInfinity(a) && !b.isZero()){
                        c.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return c;
                    }
                }

                c.real=a*b.real;
                c.imag=a*b.imag;
                return c;
        }

        // Multiply a double number to a double and return product as Complex [static method]
        public static Complex times(double a, double b){
                Complex c = new Complex();
                c.real=a*b;
                c.imag=0.0D;
                return c;
        }

        // Multiply this Complex number by a Complex number [instance method]
        // this Complex number remains unaltered
        public Complex times(Complex a){
                Complex b = new Complex();
                if(Complex.infOption){
                    if(this.isInfinite() && !a.isZero()){
                        b.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return b;
                    }
                    if(a.isInfinite() && !this.isZero()){
                        b.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return b;
                    }
                }

                b.real=this.real*a.real-this.imag*a.imag;
                b.imag=this.real*a.imag+this.imag*a.real;
                return b;
        }

        // Multiply this Complex number by a double [instance method]
        // this Complex number remains unaltered
        public Complex times(double a){
                Complex b = new Complex();
                if(Complex.infOption){
                    if(this.isInfinite() && a!=0.0D){
                        b.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return b;
                    }
                    if(Fmath.isInfinity(a) && !this.isZero()){
                        b.reset(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                        return b;
                    }
                }

                b.real=this.real*a;
                b.imag=this.imag*a;
                return b;
        }

        // Multiply this Complex number by a Complex number and replace this by the product
        public void timesEquals(Complex a){
                Complex b = new Complex();
                boolean test = true;
                if(Complex.infOption){
                    if((this.isInfinite() && !a.isZero()) || (a.isInfinite() && !this.isZero())){
                        this.real = Double.POSITIVE_INFINITY;
                        this.imag = Double.POSITIVE_INFINITY;
                        test = false;
                    }
                }
                if(test){
                    b.real=a.real*this.real-a.imag*this.imag;
                    b.imag=a.real*this.imag+a.imag*this.real;
                    this.real=b.real;
                    this.imag=b.imag;
               }
        }

        // Multiply this Complex number by a double and replace this by the product
        public void timesEquals(double a){
                boolean test = true;
                if(Complex.infOption){
                    if((this.isInfinite() && a!=0.0D) || (Fmath.isInfinity(a) && !this.isZero())){
                        this.real = Double.POSITIVE_INFINITY;
                        this.imag = Double.POSITIVE_INFINITY;
                        test = false;
                    }
                }
                if(test){
                    this.real=this.real*a;
                    this.imag=this.imag*a;
                }
        }


        // DIVISION
        // Division of two Complex numbers a/b [static method]
        public static Complex over(Complex a, Complex b){
                Complex c = new Complex(0.0D,0.0D);
                if(Complex.infOption && !a.isInfinite() && b.isInfinite())return c;

                double denom = 0.0D, ratio = 0.0D;
                if(a.isZero()){
                        if(b.isZero()){
                                c.real=Double.NaN;
                                c.imag=Double.NaN;
                        }
                        else{
                                c.real=0.0D;
                                c.imag=0.0D;
                        }
                }
                else{
                        if(Math.abs(b.real)>=Math.abs(b.imag)){
                                ratio=b.imag/b.real;
                                denom=b.real+b.imag*ratio;
                                c.real=(a.real+a.imag*ratio)/denom;
                                c.imag=(a.imag-a.real*ratio)/denom;
                        }
                        else{
                                ratio=b.real/b.imag;
                                denom=b.real*ratio+b.imag;
                                c.real=(a.real*ratio+a.imag)/denom;
                                c.imag=(a.imag*ratio-a.real)/denom;
                        }
                }
                return c;
        }

        // Division of a Complex number, a, by a double, b [static method]
        public static Complex over(Complex a, double b){
                Complex c = new Complex(0.0D, 0.0D);
                if(Complex.infOption && Fmath.isInfinity(b))return c;

                c.real=a.real/b;
                c.imag=a.imag/b;
                return c;
        }

        // Division of a double, a, by a Complex number, b  [static method]
        public static Complex over(double a, Complex b){
                Complex c = new Complex();
                if(Complex.infOption && !Fmath.isInfinity(a) && b.isInfinite())return c;

                double denom, ratio;

                if(a==0.0D){
                        if(b.isZero()){
                                c.real=Double.NaN;
                                c.imag=Double.NaN;
                        }
                        else{
                                c.real=0.0D;
                                c.imag=0.0D;
                        }
                }
                else{
                        if(Math.abs(b.real)>=Math.abs(b.imag)){
                                ratio=b.imag/b.real;
                                denom=b.real+b.imag*ratio;
                                c.real=a/denom;
                                c.imag=-a*ratio/denom;
                        }
                        else{
                                ratio=b.real/b.imag;
                                denom=b.real*ratio+b.imag;
                                c.real=a*ratio/denom;
                                c.imag=-a/denom;
                        }
                }
                return c;
        }

        // Divide a double number by a double and return quotient as Complex [static method]
        public static Complex over(double a, double b){
                Complex c = new Complex();
                c.real=a/b;
                c.imag=0.0;
                return c;
        }

        // Division of this Complex number by a Complex number [instance method]
        // this Complex number remains unaltered
        public Complex over(Complex a){
                Complex b = new Complex(0.0D, 0.0D);
                if(Complex.infOption && !this.isInfinite() && a.isInfinite())return b;

                double denom = 0.0D, ratio = 0.0D;
                if(Math.abs(a.real)>=Math.abs(a.imag)){
                        ratio=a.imag/a.real;
                        denom=a.real+a.imag*ratio;
                        b.real=(this.real+this.imag*ratio)/denom;
                        b.imag=(this.imag-this.real*ratio)/denom;
                }
                else
                {
                        ratio=a.real/a.imag;
                        denom=a.real*ratio+a.imag;
                        b.real=(this.real*ratio+this.imag)/denom;
                        b.imag=(this.imag*ratio-this.real)/denom;
                }
                return b;
        }

        // Division of this Complex number by a double [instance method]
        // this Complex number remains unaltered
        public Complex over(double a){
                Complex b = new Complex(0.0D, 0.0D);

                b.real=this.real/a;
                b.imag=this.imag/a;
                return b;
        }

        // Division of a double by this Complex number [instance method]
        // this Complex number remains unaltered
        public Complex transposedOver(double a){
                Complex c = new Complex(0.0D, 0.0D);
                if(Complex.infOption && !Fmath.isInfinity(a) && this.isInfinite())return c;

                double denom = 0.0D, ratio = 0.0D;
                if(Math.abs(this.real)>=Math.abs(this.imag)){
                        ratio=this.imag/this.real;
                        denom=this.real+this.imag*ratio;
                        c.real=a/denom;
                        c.imag=-a*ratio/denom;
                }
                else
                {
                        ratio=this.real/this.imag;
                        denom=this.real*ratio+this.imag;
                        c.real=a*ratio/denom;
                        c.imag=-a/denom;
                }
                return c;
        }

        // Division of this Complex number by a Complex number and replace this by the quotient
        public void overEquals(Complex b){
                Complex c = new Complex(0.0D, 0.0D);

                boolean test = true;
                if(Complex.infOption && !this.isInfinite() && b.isInfinite()){
                        this.real = 0.0D;
                        this.imag = 0.0D;
                        test=false;
                }
               if(test){
                    double denom = 0.0D, ratio = 0.0D;
                    if(Math.abs(b.real)>=Math.abs(b.imag)){
                        ratio=b.imag/b.real;
                        denom=b.real+b.imag*ratio;
                        c.real=(this.real+this.imag*ratio)/denom;
                        c.imag=(this.imag-this.real*ratio)/denom;
                    }
                    else
                    {
                        ratio=b.real/b.imag;
                        denom=b.real*ratio+b.imag;
                        c.real=(this.real*ratio+this.imag)/denom;
                        c.imag=(this.imag*ratio-this.real)/denom;
                    }
                    this.real = c.real;
                    this.imag = c.imag;
                }
        }

        // Division of this Complex number by a double and replace this by the quotient
        public void overEquals(double a){
                this.real=this.real/a;
                this.imag=this.imag/a;
        }

        // RECIPROCAL
        // Returns the reciprocal (1/a) of a Complex number (a) [static method]
        public static Complex inverse(Complex a){
                Complex b = new Complex(0.0D, 0.0D);
                if(Complex.infOption && a.isInfinite())return b;

                b = Complex.over(1.0D, a);
                return b;
        }

        // Returns the reciprocal (1/a) of a Complex number (a) [instance method]
        public Complex inverse(){
                Complex b = new Complex(0.0D, 0.0D);
                b = Complex.over(1.0D, this);
                return b;
        }

        // FURTHER MATHEMATICAL FUNCTIONS

        // Negates a Complex number [static method]
        public static Complex negate(Complex a){
                Complex c = new Complex();
                c.real=-a.real;
                c.imag=-a.imag;
                return c;
        }

        // Negates a Complex number [instance method]
        public Complex negate(){
                Complex c = new Complex();
                c.real=-this.real;
                c.imag=-this.imag;
                return c;
        }

        // Absolute value (modulus) of a complex number [static method]
        public static double abs(Complex a){
                double rmod = Math.abs(a.real);
                double imod = Math.abs(a.imag);
                double ratio = 0.0D;
                double res = 0.0D;

                if(rmod==0.0D){
                res=imod;
                }
                else{
                if(imod==0.0D){
                        res=rmod;
                }
                        if(rmod>=imod){
                                ratio=a.imag/a.real;
                                res=rmod*Math.sqrt(1.0D + ratio*ratio);
                        }
                        else{
                                ratio=a.real/a.imag;
                                res=imod*Math.sqrt(1.0D + ratio*ratio);
                        }
                }
                return res;
        }

        // Absolute value (modulus) of a complex number [instance method]
        public double abs(){
                double rmod = Math.abs(this.real);
                double imod = Math.abs(this.imag);
                double ratio = 0.0D;
                double res = 0.0D;

                if(rmod==0.0D){
                        res=imod;
                }
                else{
                        if(imod==0.0D){
                                res=rmod;
                        }
                        if(rmod>=imod){
                                ratio=this.imag/this.real;
                                res=rmod*Math.sqrt(1.0D + ratio*ratio);
                        }
                        else
                        {
                                ratio=this.real/this.imag;
                                res=imod*Math.sqrt(1.0D + ratio*ratio);
                        }
                }
                return res;
        }


        // Square of the absolute value (modulus) of a complex number [static method]
        public static double squareAbs(Complex a){
                return a.real*a.real + a.imag*a.imag;
        }

        // Square of the absolute value (modulus) of a complex number [instance method]
        public double squareAbs(){
                return this.real*this.real + this.imag*this.imag;
        }

        // Argument of a complex number (in radians) [static method]
        public static double arg(Complex a){
                return Math.atan2(a.imag, a.real);
        }

        // Argument of a complex number (in radians)[instance method]
        public double arg(){
                return Math.atan2(this.imag, this.real);
        }

        // Argument of a complex number (in radians) [static method]
        public static double argRad(Complex a){
                return Math.atan2(a.imag, a.real);
        }

        // Argument of a complex number (in radians)[instance method]
        public double argRad(){
                return Math.atan2(this.imag, this.real);
        }

        // Argument of a complex number (in degrees) [static method]
        public static double argDeg(Complex a){
                return Math.toDegrees(Math.atan2(a.imag, a.real));
        }

        // Argument of a complex number (in degrees)[instance method]
        public double argDeg(){
                return Math.toDegrees(Math.atan2(this.imag, this.real));
        }

        // Complex conjugate of a complex number [static method]
        public static Complex conjugate(Complex a){
                Complex c = new Complex();
                c.real=a.real;
                c.imag=-a.imag;
                return c;
        }

        // Complex conjugate of a complex number [instance method]
        public Complex conjugate(){
                Complex c = new Complex();
                c.real=this.real;
                c.imag=-this.imag;
                return c;
        }

        // Returns the length of the hypotenuse of a and b i.e. sqrt(abs(a)*abs(a)+abs(b)*abs(b))
        // where a and b are Complex [without unecessary overflow or underflow]
        public static double hypot(Complex aa, Complex bb){
                double amod=Complex.abs(aa);
                double bmod=Complex.abs(bb);
                double cc = 0.0D, ratio = 0.0D;

                if(amod==0.0D){
                        cc=bmod;
                }
                else{
                        if(bmod==0.0D){
                                cc=amod;
                        }
                        else{
                                if(amod>=bmod){
                                        ratio=bmod/amod;
                                        cc=amod*Math.sqrt(1.0 + ratio*ratio);
                                }
                                else{
                                        ratio=amod/bmod;
                                        cc=bmod*Math.sqrt(1.0 + ratio*ratio);
                                }
                        }
                }
                return cc;
        }

        // Exponential of a complex number (instance method)
        public  Complex exp(){
            return Complex.exp(this);
        }

        // Exponential of a complex number (static method)
        public static Complex exp(Complex aa){
                Complex z = new Complex();

                double a = aa.real;
                double b = aa.imag;

                if(b==0.0D){
                        z.real=Math.exp(a);
                        z.imag=0.0D;
                }
                else{
                        if(a==0D){
                                z.real=Math.cos(b);
                                z.imag=Math.sin(b);
                        }
                        else{
                                double c=Math.exp(a);
                                z.real=c*Math.cos(b);
                                z.imag=c*Math.sin(b);
                        }
                }
                return z;
        }

        // Exponential of a real number returned as a complex number
        public static Complex exp(double aa){
                Complex bb = new Complex(aa, 0.0D);
                return Complex.exp(bb);
        }

        // Returns exp(j*arg) where arg is real (a double)
        public static Complex expPlusJayArg(double arg){
                Complex argc = new Complex(0.0D, arg);
                return Complex.exp(argc);
        }

        // Returns exp(-j*arg) where arg is real (a double)
        public static Complex expMinusJayArg(double arg){
                Complex argc = new Complex(0.0D, -arg);
                return Complex.exp(argc);
        }

        // Principal value of the natural log of an Complex number (instance method)
        public Complex log(){

                double a=this.real;
                double b=this.imag;
                Complex c = new Complex();

                c.real=Math.log(Complex.abs(this));
                c.imag=Math.atan2(b,a);

                return c;
        }

        // Principal value of the natural log of an Complex number
        public static Complex log(Complex aa ){

                double a=aa.real;
                double b=aa.imag;
                Complex c = new Complex();

                c.real=Math.log(Complex.abs(aa));
                c.imag=Math.atan2(b,a);

                return c;
        }

        // Roots
        // Principal value of the square root of a complex number (instance method)
        public Complex sqrt(){
            return Complex.sqrt(this);
        }


        // Principal value of the square root of a complex number
        public static Complex sqrt(Complex aa ){
                double a=aa.real;
                double b=aa.imag;
                Complex c = new Complex();

                if(b==0.0D){
                        if(a>=0.0D){
                                c.real=Math.sqrt(a);
                                c.imag=0.0D;
                        }
                        else{
                                c.real=0.0D;
                                c.imag= Math.sqrt(-a);
                        }
                }
                else{
                        double w, ratio;
                        double amod=Math.abs(a);
                        double bmod=Math.abs(b);
                        if(amod>=bmod){
                                ratio=b/a;
                                w=Math.sqrt(amod)*Math.sqrt(0.5D*(1.0D + Math.sqrt(1.0D + ratio*ratio)));
                        }
                        else{
                                ratio=a/b;
                                w=Math.sqrt(bmod)*Math.sqrt(0.5D*(Math.abs(ratio) + Math.sqrt(1.0D + ratio*ratio)));
                        }
                        if(a>=0.0){
                                c.real=w;
                                c.imag=b/(2.0D*w);
                        }
                        else{
                                if(b>=0.0){
                                        c.imag=w;
                                        c.real=b/(2.0D*c.imag);
                                }
                                else{
                                        c.imag=-w;
                                        c.real=b/(2.0D*c.imag);
                                }
                        }
                }
                return c;
        }

        // Principal value of the nth root of a complex number (n = integer > 1) [instance method]
        public Complex nthRoot(int n){
            return Complex.nthRoot(this, n);
        }


        // Principal value of the nth root of a complex number (n = integer > 1) [static method]
        public static Complex nthRoot(Complex aa, int n ){
                Complex c = new Complex();
                if(n==0){
                    c = new Complex(Double.POSITIVE_INFINITY, 0.0);
                }
                else{
                    if(n==1){
                        c = aa;
                    }
                    else{
                        c = Complex.exp((Complex.log(aa)).over((double)n));
                    }
                }

                return c;
        }

        // Powers
        // Square of a complex number (static method)
        public static Complex square(Complex aa){
                Complex c = new Complex();
                c.real= aa.real*aa.real-aa.imag*aa.imag;
                c.imag= 2.0D*aa.real*aa.imag;
                return c;
        }

        // Square of a complex number (instance method)
        public  Complex square(){
            return this.times(this);
        }

        // returns a Complex number raised to a Complex power (instance method)
        public Complex pow(Complex b ){
                Complex c = new Complex();
                if(this.isZero()){
                    if(b.imag==0){
                        if(b.real==0){
                            c = new Complex(1.0, 0.0);
                        }
                        else{
                            if(b.real>0.0){
                                c = new Complex(0.0, 0.0);
                            }
                            else{
                                if(b.real<0.0){
                                    c = new Complex(Double.POSITIVE_INFINITY, 0.0);
                                }
                            }
                        }
                    }
                    else{
                        c = Complex.exp(b.times(Complex.log(this)));
                    }
                }
                else{
                    c = Complex.exp(b.times(Complex.log(this)));
                }

                return c;
        }

        // returns a Complex number raised to a Complex power
        public static Complex pow(Complex a, Complex b ){
                Complex c = new Complex();
                if(a.isZero()){
                    if(b.imag==0){
                        if(b.real==0){
                            c = new Complex(1.0, 0.0);
                        }
                        else{
                            if(a.real>0.0){
                                c = new Complex(0.0, 0.0);
                            }
                            else{
                                if(a.real<0.0){
                                    c = new Complex(Double.POSITIVE_INFINITY, 0.0);
                                }
                            }
                        }
                    }
                    else{
                        c=Complex.exp(b.times(Complex.log(a)));
                    }
                }
                else{
                    c=Complex.exp(b.times(Complex.log(a)));
                }

                return c;
        }

        // returns a Complex number raised to a double power [instance method]
        public Complex pow(double b){
                return  powDouble(this, b);
        }

        // returns a Complex number raised to a double power
        public static Complex pow(Complex a, double b){
                    return  powDouble(a, b);
        }

        // returns a Complex number raised to an integer, i.e. int, power [instance method]
        public Complex pow(int n ){
                double b = (double) n;
                return  powDouble(this, b);
        }

        // returns a Complex number raised to an integer, i.e. int, power
        public static Complex pow(Complex a, int n ){
                double b = (double) n;
                return  powDouble(a, b);
        }

        // returns a double raised to a Complex power
        public static Complex pow(double a, Complex b ){
                Complex c = new Complex();
                if(a==0){
                    if(b.imag==0){
                        if(b.real==0){
                            c = new Complex(1.0, 0.0);
                        }
                        else{
                            if(b.real>0.0){
                                c = new Complex(0.0, 0.0);
                            }
                            else{
                                if(b.real<0.0){
                                    c = new Complex(Double.POSITIVE_INFINITY, 0.0);
                                }
                            }
                        }
                    }
                    else{
                        double z = Math.pow(a, b.real);
                        c=Complex.exp(Complex.times(Complex.plusJay(), b.imag*Math.log(a)));
                        c=Complex.times(z, c);
                    }
                }
                else{
                    double z = Math.pow(a, b.real);
                    c=Complex.exp(Complex.times(Complex.plusJay(), b.imag*Math.log(a)));
                    c=Complex.times(z, c);
                }

                return c;

         }

        // Complex trigonometric functions

        // Sine of an Complex number
        public Complex sin(){
            return Complex.sin(this);
        }

        public static Complex sin(Complex aa ){
                Complex c = new Complex();
                double a = aa.real;
                double b = aa.imag;
                c.real = Math.sin(a)*Fmath.cosh(b);
                c.imag = Math.cos(a)*Fmath.sinh(b);
                return c;
        }

        // Cosine of an Complex number
        public Complex cos(){
            return Complex.cos(this);
        }

        public static Complex cos(Complex aa ){
                Complex c = new Complex();
                double a = aa.real;
                double b = aa.imag;
                c.real= Math.cos(a)*Fmath.cosh(b);
                c.imag= -Math.sin(a)*Fmath.sinh(b);
                return c;
        }

        // Secant of an Complex number
        public Complex sec(){
            return Complex.sec(this);
        }

        public static Complex sec(Complex aa ){
                Complex c = new Complex();
                double a = aa.real;
                double b = aa.imag;
                c.real= Math.cos(a)*Fmath.cosh(b);
                c.imag= -Math.sin(a)*Fmath.sinh(b);
                return c.inverse();
        }

        // Cosecant of an Complex number
        public Complex csc(){
            return Complex.csc(this);
        }

        public static Complex csc(Complex aa ){
                Complex c = new Complex();
                double a = aa.real;
                double b = aa.imag;
                c.real = Math.sin(a)*Fmath.cosh(b);
                c.imag = Math.cos(a)*Fmath.sinh(b);
                return c.inverse();
        }

        // Tangent of an Complex number
        public Complex tan(){
            return Complex.tan(this);
        }

        public static Complex tan(Complex aa ){
                Complex c = new Complex();
                double denom = 0.0D;
                double a = aa.real;
                double b = aa.imag;

                Complex x = new Complex(Math.sin(a)*Fmath.cosh(b), Math.cos(a)*Fmath.sinh(b));
                Complex y = new Complex(Math.cos(a)*Fmath.cosh(b), -Math.sin(a)*Fmath.sinh(b));
                c=Complex.over(x, y);
                return c;
        }

        // Cotangent of an Complex number
        public Complex cot(){
            return Complex.cot(this);
        }

        public static Complex cot(Complex aa ){
                Complex c = new Complex();
                double denom = 0.0D;
                double a = aa.real;
                double b = aa.imag;

                Complex x = new Complex(Math.sin(a)*Fmath.cosh(b), Math.cos(a)*Fmath.sinh(b));
                Complex y = new Complex(Math.cos(a)*Fmath.cosh(b), -Math.sin(a)*Fmath.sinh(b));
                c=Complex.over(y, x);
                return c;
        }

        // Exsecant of an Complex number
        public Complex exsec(){
            return Complex.exsec(this);
        }

        public static Complex exsec(Complex aa ){
                return Complex.sec(aa).minus(1.0D);
        }

        // Versine of an Complex number
        public Complex vers(){
            return Complex.vers(this);
        }

        public static Complex vers(Complex aa ){
                return Complex.plusOne().minus(Complex.cos(aa));
        }

        // Coversine of an Complex number
        public Complex covers(){
            return Complex.covers(this);
        }

        public static Complex covers(Complex aa ){
                return Complex.plusOne().minus(Complex.sin(aa));
        }

        // Haversine of an Complex number
        public Complex hav(){
            return Complex.hav(this);
        }

        public static Complex hav(Complex aa ){
                return Complex.vers(aa).over(2.0D);
        }

        // Hyperbolic sine of a Complex number
        public Complex sinh(){
            return Complex.sinh(this);
        }

        public static Complex sinh(Complex a ){
                Complex c = new Complex();
                c=a.times(plusJay());
                c=(Complex.minusJay()).times(Complex.sin(c));
                return c;
        }

        // Hyperbolic cosine of a Complex number
        public Complex cosh(){
            return Complex.cosh(this);
        }

        public static Complex cosh(Complex a ){
                Complex c = new Complex();
                c=a.times(Complex.plusJay());
                c=Complex.cos(c);
                return c;
        }

        // Hyperbolic tangent of a Complex number
        public Complex tanh(){
            return Complex.tanh(this);
        }

        public static Complex tanh(Complex a ){
                Complex c = new Complex();
                c = (Complex.sinh(a)).over(Complex.cosh(a));
                return c;
        }

        // Hyperbolic cotangent of a Complex number
        public Complex coth(){
            return Complex.coth(this);
        }

        public static Complex coth(Complex a ){
                Complex c = new Complex();
                c = (Complex.cosh(a)).over(Complex.sinh(a));
                return c;
        }

        // Hyperbolic secant of a Complex number
        public Complex sech(){
            return Complex.sech(this);
        }

        public static Complex sech(Complex a ){
                Complex c = new Complex();
                c = (Complex.cosh(a)).inverse();
                return c;
        }

        // Hyperbolic cosecant of a Complex number
        public Complex csch(){
            return Complex.csch(this);
        }

        public static Complex csch(Complex a ){
                Complex c = new Complex();
                c = (Complex.sinh(a)).inverse();
                return c;
        }


        // Inverse sine of a Complex number
        public Complex asin(){
            return Complex.asin(this);
        }

        public static Complex asin(Complex a ){
                Complex c = new Complex();
                c=Complex.sqrt(Complex.minus(1.0D, Complex.square(a)));
                c=(Complex.plusJay().times(a)).plus(c);
                c=Complex.minusJay().times(Complex.log(c));
                return c;
        }

        // Inverse cosine of a Complex number
        public Complex acos(){
            return Complex.acos(this);
        }

        public static Complex acos(Complex a ){
                Complex c = new Complex();
                c=Complex.sqrt(Complex.minus(Complex.square(a),1.0));
                c=a.plus(c);
                c=Complex.minusJay().times(Complex.log(c));
                return c;
        }

        // Inverse tangent of a Complex number
        public Complex atan(){
            return Complex.atan(this);
        }

        public static Complex atan(Complex a ){
                Complex c = new Complex();
                Complex d = new Complex();

                c=Complex.plusJay().plus(a);
                d=Complex.plusJay().minus(a);
                c=c.over(d);
                c=Complex.log(c);
                c=Complex.plusJay().times(c);
                c=c.over(2.0D);
                return c;
        }

        // Inverse cotangent of a Complex number
        public Complex acot(){
            return Complex.acot(this);
        }

        public static Complex acot(Complex a ){
            return Complex.atan(a.inverse());
        }

        // Inverse secant of a Complex number
        public Complex asec(){
            return Complex.asec(this);
        }

        public static Complex asec(Complex a ){
            return Complex.acos(a.inverse());
        }

        // Inverse cosecant of a Complex number
        public Complex acsc(){
            return Complex.acsc(this);
        }

        public static Complex acsc(Complex a ){
            return Complex.asin(a.inverse());
        }

        // Inverse exsecant of a Complex number
        public Complex aexsec(){
            return Complex.aexsec(this);
        }

        public static Complex aexsec(Complex a ){
            Complex c = a.plus(1.0D);
            return Complex.asin(c.inverse());
        }

        // Inverse versine of a Complex number
        public Complex avers(){
            return Complex.avers(this);
        }

        public static Complex avers(Complex a ){
            Complex c = Complex.plusOne().plus(a);
            return Complex.acos(c);
        }

        // Inverse coversine of a Complex number
        public Complex acovers(){
            return Complex.acovers(this);
        }

        public static Complex acovers(Complex a ){
            Complex c = Complex.plusOne().plus(a);
            return Complex.asin(c);
        }

        // Inverse haversine of a Complex number
        public Complex ahav(){
            return Complex.ahav(this);
        }

        public static Complex ahav(Complex a ){
            Complex c = Complex.plusOne().minus(a.times(2.0D));
            return Complex.acos(c);
        }

        // Inverse hyperbolic sine of a Complex number
        public Complex asinh(){
            return Complex.asinh(this);
        }

        public static Complex asinh(Complex a ){
                Complex c = new Complex(0.0D, 0.0D);
                c=Complex.sqrt(Complex.square(a).plus(1.0D));
                c=a.plus(c);
                c=Complex.log(c);

                return c;
        }

        // Inverse hyperbolic cosine of a Complex number
        public Complex acosh(){
            return Complex.acosh(this);
        }

        public static Complex acosh(Complex a ){
                Complex c = new Complex();
                c=Complex.sqrt(Complex.square(a).minus(1.0D));
                c=a.plus(c);
                c=Complex.log(c);
                return c;
        }

        // Inverse hyperbolic tangent of a Complex number
        public Complex atanh(){
            return Complex.atanh(this);
        }

        public static Complex atanh(Complex a ){
                Complex c = new Complex();
                Complex d = new Complex();
                c=Complex.plusOne().plus(a);
                d=Complex.plusOne().minus(a);
                c=c.over(d);
                c=Complex.log(c);
                c=c.over(2.0D);
                return c;
        }

        // Inverse hyperbolic cotangent of a Complex number
        public Complex acoth(){
            return Complex.acoth(this);
        }

        public static Complex acoth(Complex a ){
                Complex c = new Complex();
                Complex d = new Complex();
                c=Complex.plusOne().plus(a);
                d=a.plus(1.0D);
                c=c.over(d);
                c=Complex.log(c);
                c=c.over(2.0D);
                return c;
        }

        // Inverse hyperbolic secant of a Complex number
        public Complex asech(){
            return Complex.asech(this);
        }

        public static Complex asech(Complex a ){
                Complex c = a.inverse();
                Complex d = (Complex.square(a)).minus(1.0D);
                return Complex.log(c.plus(Complex.sqrt(d)));
        }

        // Inverse hyperbolic cosecant of a Complex number
        public Complex acsch(){
            return Complex.acsch(this);
        }

        public static Complex acsch(Complex a ){
                Complex c = a.inverse();
                Complex d = (Complex.square(a)).plus(1.0D);
                return Complex.log(c.plus(Complex.sqrt(d)));
        }




        // LOGICAL FUNCTIONS
        // Returns true if the Complex number has a zero imaginary part, i.e. is a real number
        public static boolean isReal(Complex a){
                boolean test = false;
                if(Math.abs(a.imag)==0.0D)test = true;
                return test;
        }

        public static boolean isReal(Complex[] a){
                boolean test = true;
                int n = a.length;
                for(int i=0; i<n; i++){
                    if(Math.abs(a[i].imag)!=0.0D)test = false;
                }
                return test;
        }

        public boolean isReal(){
                boolean test = false;
                if(Math.abs(this.imag)==0.0D)test = true;
                return test;
        }

        // Returns true if the Complex number has a zero imaginary part within the limit lim, i.e. is a real number
        public static boolean isReal(Complex a, double lim){
                boolean test = false;
                if(Math.abs(a.imag)<=Math.abs(lim))test = true;
                return test;
        }

        public static boolean isReal(Complex[] a, double lim){
                boolean test = true;
                int n = a.length;
                for(int i=0; i<n; i++){
                    if(Math.abs(a[i].imag)>Math.abs(lim))test = false;
                }
                return test;
        }

        public boolean isReal(double lim){
                boolean test = false;
                if(Math.abs(this.imag)<=Math.abs(lim))test = true;
                return test;
        }

        // Returns true if the Complex number has a zero imaginary part less than the percentage precent ofv the real part
        public static boolean isRealPerCent(Complex a, double percent){
                boolean test = false;
                if(Math.abs(a.imag*100.0/a.real)<=Math.abs(percent))test = true;
                return test;
        }

        public static boolean isRealPerCent(Complex[] a, double percent){
                boolean test = true;
                int n = a.length;
                for(int i=0; i<n; i++){
                    if(Math.abs(a[i].imag*100.0/a[i].real)>Math.abs(percent))test = false;
                }
                return test;
        }

        public boolean isRealperCent(double percent){
                boolean test = false;
                if(Math.abs(this.imag*100.0/this.real)<=Math.abs(percent))test = true;
                return test;
        }

        // Returns true if the Complex number has a zero real and a zero imaginary part
        // i.e. has a zero modulus
        public static boolean isZero(Complex a){
                boolean test = false;
                if(Math.abs(a.real)==0.0D && Math.abs(a.imag)==0.0D)test = true;
                return test;
        }

        public boolean isZero(){
                boolean test = false;
                if(Math.abs(this.real)==0.0D && Math.abs(this.imag)==0.0D)test = true;
                return test;
        }

        // Returns true if either the real or the imaginary part of the Complex number
        // is equal to plus infinity
        public boolean isPlusInfinity(){
                boolean test = false;
                if(this.real==Double.POSITIVE_INFINITY || this.imag==Double.POSITIVE_INFINITY)test = true;
                return test;
        }

        public static boolean isPlusInfinity(Complex a){
                boolean test = false;
                if(a.real==Double.POSITIVE_INFINITY || a.imag==Double.POSITIVE_INFINITY)test = true;
                return test;
        }

        // Returns true if either the real or the imaginary part of the Complex number
        // is equal to minus infinity
        public boolean isMinusInfinity(){
                boolean test = false;
                if(this.real==Double.NEGATIVE_INFINITY || this.imag==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }

        public static boolean isMinusInfinity(Complex a){
                boolean test = false;
                if(a.real==Double.NEGATIVE_INFINITY || a.imag==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }


        // Returns true if either the real or the imaginary part of the Complex number
        // is equal to either infinity or minus plus infinity
        public static boolean isInfinite(Complex a){
        boolean test = false;
                if(a.real==Double.POSITIVE_INFINITY || a.imag==Double.POSITIVE_INFINITY)test = true;
                if(a.real==Double.NEGATIVE_INFINITY || a.imag==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }

        public boolean isInfinite(){
                boolean test = false;
                if(this.real==Double.POSITIVE_INFINITY || this.imag==Double.POSITIVE_INFINITY)test = true;
                if(this.real==Double.NEGATIVE_INFINITY || this.imag==Double.NEGATIVE_INFINITY)test = true;
                return test;
        }

        // Returns true if the Complex number is NaN (Not a Number)
        // i.e. is the result of an uninterpretable mathematical operation
        public static boolean isNaN(Complex a){
                boolean test = false;
                if(a.real!=a.real || a.imag!=a.imag)test = true;
                return test;
        }

        public boolean isNaN(){
                boolean test = false;
                if(this.real!=this.real || this.imag!=this.imag)test = true;
                return test;
        }

        // Returns true if two Complex number are identical
        // Follows the Sun Java convention of treating all NaNs as equal
        // i.e. does not satisfies the IEEE 754 specification
        // but does let hashtables operate properly
        public boolean equals(Complex a){
                boolean test = false;
                if(this.isNaN()&&a.isNaN()){
                        test=true;
                }
                else{
                        if(this.real == a.real && this.imag == a.imag)test = true;
                }
                return test;
        }

        public boolean isEqual(Complex a){
                boolean test = false;
                if(this.isNaN()&&a.isNaN()){
                        test=true;
                }
                else{
                        if(this.real == a.real && this.imag == a.imag)test = true;
                }
                return test;
        }


        public static boolean isEqual(Complex a, Complex b){
                boolean test = false;
                if(isNaN(a)&&isNaN(b)){
                        test=true;
                }
                else{
                        if(a.real == b.real && a.imag == b.imag)test = true;
                }
                return test;
        }



        // returns true if the differences between the real and imaginary parts of two complex numbers
        // are less than fract times the larger real and imaginary part
        public boolean equalsWithinLimits(Complex a, double fract){
            return isEqualWithinLimits(a, fract);
        }

        public boolean isEqualWithinLimits(Complex a, double fract){
            boolean test = false;

            double rt = this.getReal();
            double ra = a.getReal();
            double it = this.getImag();
            double ia = a.getImag();
            double rdn = 0.0D;
            double idn = 0.0D;
            double rtest = 0.0D;
            double itest = 0.0D;

            if(rt==0.0D && it==0.0D && ra==0.0D && ia==0.0D)test=true;
            if(!test){
                rdn=Math.abs(rt);
                if(Math.abs(ra)>rdn)rdn=Math.abs(ra);
                if(rdn==0.0D){
                    rtest=0.0;
                }
                else{
                    rtest=Math.abs(ra-rt)/rdn;
                }
                idn=Math.abs(it);
                if(Math.abs(ia)>idn)idn=Math.abs(ia);
                if(idn==0.0D){
                    itest=0.0;
                }
                else{
                    itest=Math.abs(ia-it)/idn;
                }
                if(rtest<fract && itest<fract)test=true;
            }

            return test;
        }

        public static boolean isEqualWithinLimits(Complex a, Complex b, double fract){
            boolean test = false;

            double rb = b.getReal();
            double ra = a.getReal();
            double ib = b.getImag();
            double ia = a.getImag();
            double rdn = 0.0D;
            double idn = 0.0D;

            if(ra==0.0D && ia==0.0D && rb==0.0D && ib==0.0D)test=true;
            if(!test){
                rdn=Math.abs(rb);
                if(Math.abs(ra)>rdn)rdn=Math.abs(ra);
                idn=Math.abs(ib);
                if(Math.abs(ia)>idn)idn=Math.abs(ia);
                if(Math.abs(ra-rb)/rdn<fract && Math.abs(ia-ia)/idn<fract)test=true;
            }

            return test;
        }

        // SOME USEFUL NUMBERS
        // returns the number zero (0) as a complex number
        public static Complex zero(){
                Complex c = new Complex();
                c.real=0.0D;
                c.imag=0.0D;
                return c;
        }

        // returns the number one (+1) as a complex number
        public static Complex plusOne(){
                Complex c = new Complex();
                c.real=1.0D;
                c.imag=0.0D;
                return c;
        }

        // returns the number minus one (-1) as a complex number
        public static Complex minusOne(){
                Complex c = new Complex();
                c.real=-1.0D;
                c.imag=0.0D;
                return c;
        }

        // returns plus j
        public static Complex plusJay(){
                Complex c = new Complex();
                c.real=0.0D;
                c.imag=1.0D;
                return c;
        }

        // returns minus j
        public static Complex minusJay(){
                Complex c = new Complex();
                c.real=0.0D;
                c.imag=-1.0D;
                return c;
        }

        // returns pi as a Complex number
        public static Complex pi(){
                Complex c = new Complex();
                c.real=Math.PI;
                c.imag=0.0D;
                return c;
        }

        // returns 2.pi.j
        public static Complex twoPiJay(){
                Complex c = new Complex();
                c.real=0.0D;
                c.imag=2.0D*Math.PI;
                return c;
        }

        // infinity + infinity.j
        public static Complex plusInfinity(){
                Complex c = new Complex();
                c.real=Double.POSITIVE_INFINITY;
                c.imag=Double.POSITIVE_INFINITY;
                return c;
        }

        // -infinity - infinity.j
        public static Complex minusInfinity(){
                Complex c = new Complex();
                c.real=Double.NEGATIVE_INFINITY;
                c.imag=Double.NEGATIVE_INFINITY;
                return c;
        }

        // PRIVATE METHODS
        // returns a Complex number raised to a double power
        // this method is used for calculation within this class file
        // see above for corresponding public method
        private static Complex powDouble(Complex a, double b){
                Complex z = new Complex();
                double re=a.real;
                double im=a.imag;

                if(a.isZero()){
                    if(b==0.0){
                        z = new Complex(1.0, 0.0);
                    }
                    else{
                        if(b>0.0){
                            z = new Complex(0.0, 0.0);
                        }
                        else{
                            if(b<0.0){
                                z = new Complex(Double.POSITIVE_INFINITY, 0.0);
                            }
                        }
                    }
                }
                else{
                    if(im==0.0D && re>0.0D){
                        z.real=Math.pow(re, b);
                        z.imag=0.0D;
                    }
                    else{
                        if(re==0.0D){
                            z=Complex.exp(Complex.times(b, Complex.log(a)));
                        }
                        else{
                            double c=Math.pow(re*re+im*im, b/2.0D);
                            double th=Math.atan2(im, re);
                            z.real=c*Math.cos(b*th);
                            z.imag=c*Math.sin(b*th);
                        }
                    }
                }
                return z;
        }

}
