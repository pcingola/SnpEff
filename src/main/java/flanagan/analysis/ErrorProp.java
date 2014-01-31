/*
*   Class   ErrorProp
*
*   Defines an object consisting of a variable and its associated standard
*   deviation and the class includes the methods for propagating the error
*   in standard arithmetic operations for both correlated and uncorrelated
*   errors.
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    October 2002
*   UPDATE:  26 April 2004, 19 January 2005
*
*   See ComplexErrorProp for the propogation of errors in complex arithmetic
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ErrorProp.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2008  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
*   Redistributions of this source code, or parts of, must retain the above
*   copyright notice, this list of conditions and the following disclaimer.
*
*   Redistribution in binary form of all or parts of this class, must reproduce
*   the above copyright, this list of conditions and the following disclaimer in
*   the documentation and/or other materials provided with the distribution.
*
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all
*   copies and associated documentation or publications.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.analysis;

import flanagan.math.Fmath;

public class ErrorProp{

        // DATA VARIABLES
        private double value = 0.0D;   // number value
        private double error = 0.0D;   // its standard deviation or an estimate of its standard deviation


        // CONSTRUCTORS
        // default constructor
        public ErrorProp(){
                value = 0.0;
                error = 0.0;
        }

        // constructor with value and error initialised
        public ErrorProp(double value, double error){
                this.value = value;
                this.error = error;
        }

        // constructor with value initialised
        public ErrorProp(double value){
                this.value = value;
                this.error = 0.0;
        }

        // PUBLIC METHODS

        // SET VALUES
        // Set the value of value
                public void setValue(double value){
                this.value = value;
        }

        // Set the value of error
                public void setError(double error){
                this.error = error;
        }

        // Set the values of value and error
                public void reset(double value, double error){
                this.value = value;
                this.error = error;
        }

        // GET VALUES
        // Get the value of value
                public double getValue(){
                return value;
        }

        // Get the value of error
        public double getError(){
                return error;
        }

        //PRINT AN ERROR NUMBER
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

        // TRUNCATION
        // Rounds the mantissae of both the value and error parts of Errorprop to prec places
        public static ErrorProp truncate(ErrorProp x, int prec){
                if(prec<0)return x;

                double xV = x.getValue();
                double xS = x.getError();
                ErrorProp y = new ErrorProp();

                xV = Fmath.truncate(xV, prec);
                xS = Fmath.truncate(xS, prec);

                y.reset(xV, xS);

                return y;
        }

        // instance method
        public  ErrorProp truncate(int prec){
                if(prec<0)return this;

                double xV = this.getValue();
                double xS = this.getError();
                ErrorProp y = new ErrorProp();

                xV = Fmath.truncate(xV, prec);
                xS = Fmath.truncate(xS, prec);

                y.reset(xV, xS);

                return y;
        }

        // CONVERSIONS
        // Format an ErrorProp number as a string
        // Overides java.lang.String.toString()
        public String toString(){
                return this.value+", error = "+this.error;
        }

        // Format an ErrorProp number as a string
        // See static method above for comments
        public static String toString(ErrorProp aa){
                return aa.value+", error = "+aa.error;
        }

        // Return a HASH CODE for the ErrorProp number
        // Overides java.lang.Object.hashCode()
        public int hashCode(){
                long lvalue = Double.doubleToLongBits(this.value);
                long lerror = Double.doubleToLongBits(this.error);
                int hvalue = (int)(lvalue^(lvalue>>>32));
                int herror = (int)(lerror^(lerror>>>32));
                return 7*(hvalue/10)+3*(herror/10);
        }


        // ARRAYS

        // Create a one dimensional array of ErrorProp objects of length n
        // all values = 0 and all error's = 0
        public static ErrorProp[] oneDarray(int n){
                ErrorProp[] a =new ErrorProp[n];
                for(int i=0; i<n; i++){
                        a[i]=ErrorProp.zero();
                }
                return a;
        }

        // Create a one dimensional array of ErrorProp objects of length n
        // all values = a and all error's = b
        public static ErrorProp[] oneDarray(int n, double a, double b){
                ErrorProp[] c =new ErrorProp[n];
                for(int i=0; i<n; i++){
                        c[i]=ErrorProp.zero();
                        c[i].reset(a, b);
                }
                return c;
        }

        // Create a one dimensional array of ErrorProp objects of length n
        // all = the ErrorProp number named constant
        public static ErrorProp[] oneDarray(int n, ErrorProp constant){
                ErrorProp[] c =new ErrorProp[n];
                for(int i=0; i<n; i++){
                        c[i]=ErrorProp.copy(constant);
                }
                return c;
        }

        // Create a two dimensional array of ErrorProp objects of dimensions n and m
        // all values = zero and all error's = zero
        public static ErrorProp[][] twoDarray(int n, int m){
                ErrorProp[][] a =new ErrorProp[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                a[i][j]=ErrorProp.zero();
                        }
                }
                return a;
        }

        // Create a two dimensional array of ErrorProp objects of dimensions n and m
        // all values = a and all error's = b
        public static ErrorProp[][] twoDarray(int n, int m, double a, double b){
                ErrorProp[][] c =new ErrorProp[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                c[i][j]=ErrorProp.zero();
                                c[i][j].reset(a, b);
                        }
                }
                return c;
        }

        // Create a two dimensional array of ErrorProp objects of dimensions n and m
        // all  =  the ErrorProp number named constant
        public static ErrorProp[][] twoDarray(int n, int m, ErrorProp constant){
                ErrorProp[][] c =new ErrorProp[n][m];
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                c[i][j]=ErrorProp.copy(constant);
                        }
                }
                return c;
        }

        // COPY
        // Copy a single ErrorProp number [static method]
        public static ErrorProp copy(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = a.value;
                b.error = a.error;
                return b;
        }

        // Copy a single ErrorProp number [instance method]
        public ErrorProp copy(){
                ErrorProp b = new ErrorProp();
                b.value = this.value;
                b.error = this.error;
                return b;
        }

        // Clone a single ErrorProp number
        public Object clone(){
                ErrorProp b = new ErrorProp();
                b.value = this.value;
                b.error = this.error;
                return (Object) b;
        }


        // Copy a 1D array of ErrorProp numbers (deep copy)
        public static ErrorProp[] copy(ErrorProp[] a){
                int n =a.length;
                ErrorProp[] b = ErrorProp.oneDarray(n);
                for(int i=0; i<n; i++){
                        b[i]=ErrorProp.copy(a[i]);
                }
                return b;
        }

        // Copy a 2D array of ErrorProp numbers (deep copy)
        public static ErrorProp[][] copy(ErrorProp[][] a){
                int n =a.length;
                int m =a[0].length;
                ErrorProp[][] b = ErrorProp.twoDarray(n, m);
                for(int i=0; i<n; i++){
                        for(int j=0; j<m; j++){
                                b[i][j]=ErrorProp.copy(a[i][j]);
                        }
                }
                return b;
        }

        // ADDITION
        // Add two ErrorProp numbers with correlation [instance method]
        public ErrorProp plus(ErrorProp a, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value=a.value+this.value;
                c.error = hypotWithCov(a.error, this.error, corrCoeff);
                return c;
        }

        // Add two ErrorProp numbers with correlation [static method]
        public static ErrorProp plus(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value=a.value+b.value;
                c.error = hypotWithCov(a.error, b.error, corrCoeff);
                return c;
        }

        //Add a ErrorProp number to this ErrorProp number with no, i.e. zero, correlation [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp plus(ErrorProp a ){
                ErrorProp b = new ErrorProp();
                b.value = this.value + a.value;
                b.error = hypotWithCov(a.error, this.error, 0.0D);
                return b;
        }

        // Add two ErrorProp numbers with no, i.e. zero, correlation term [static method]
        public static ErrorProp plus(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a.value+b.value;
                c.error = hypotWithCov(a.error, b.error, 0.0D);
                return c;
        }

        // Add an error free double number to this ErrorProp number [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp plus(double a ){
                ErrorProp b = new ErrorProp();
                b.value = this.value + a;
                b.error = Math.abs(this.error);
                return b;
        }

        //Add a ErrorProp number to an error free double [static method]
        public static ErrorProp plus(double a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a+b.value;
                c.error=Math.abs(b.error);
                return c;
        }

       //Add an error free double number to an error free double and return sum as ErrorProp [static method]
        public static ErrorProp plus(double a, double b){
                ErrorProp c = new ErrorProp();
                c.value=a+b;
                c.error=0.0D;
                return c;
        }


        // Add a ErrorProp number to this ErrorProp number and replace this with the sum
        // with correlation term
        public void plusEquals(ErrorProp a, double corrCoeff){
                this.value+=a.value;
                this.error = hypotWithCov(a.error, this.error, corrCoeff);
        }

        // Add a ErrorProp number to this ErrorProp number and replace this with the sum
        // with no, i.e. zero, correlation term
        public void plusEquals(ErrorProp a){
                this.value+=a.value;
                this.error = Math.sqrt(a.error*a.error + this.error*this.error);
                this.error = hypotWithCov(a.error, this.error, 0.0D);
        }

        //Add double number to this ErrorProp number and replace this with the sum
        public void plusEquals(double a ){
                this.value+=a;
                this.error=Math.abs(this.error);
        }

        // SUBTRACTION
        // Subtract an ErrorProp number from this ErrorProp number with correlation [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp minus(ErrorProp a, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value=this.value-a.value;
                c.error = hypotWithCov(this.error, a.error, -corrCoeff);
                return c;
        }

        // Subtract ErrorProp number b from ErrorProp number a with correlation [static method]
        public static ErrorProp minus(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value=a.value-b.value;
                c.error = hypotWithCov(a.error, b.error, -corrCoeff);
                return c;
        }

        // Subtract a ErrorProp number from this ErrorProp number with no, i.e. zero, correlation [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp minus(ErrorProp a ){
                ErrorProp b = new ErrorProp();
                b.value = this.value - a.value;
                b.error = hypotWithCov(a.error, this.error, 0.0D);
                return b;
        }

        // Subtract ErrorProp number b from ErrorProp number a with no, i.e. zero, correlation term [static method]
        public static ErrorProp minus(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a.value-b.value;
                c.error = hypotWithCov(a.error, b.error, 0.0D);
                return c;
        }

        // Subtract an error free double number from this ErrorProp number [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp minus(double a ){
                ErrorProp b = new ErrorProp();
                b.value = this.value - a;
                b.error = Math.abs(this.error);
                return b;
        }

        // Subtract a ErrorProp number b from an error free double a [static method]
        public static ErrorProp minus(double a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a-b.value;
                c.error=Math.abs(b.error);
                return c;
        }

        //Subtract an error free double number b from an error free double a and return sum as ErrorProp [static method]
        public static ErrorProp minus(double a, double b){
                ErrorProp c = new ErrorProp();
                c.value=a-b;
                c.error=0.0D;
                return c;
        }

        // Subtract a ErrorProp number to this ErrorProp number and replace this with the sum
        // with correlation term
        public void minusEquals(ErrorProp a, double corrCoeff){
                this.value-=a.value;
                this.error = hypotWithCov(a.error, this.error, -corrCoeff);
        }

        // Subtract a ErrorProp number from this ErrorProp number and replace this with the sum
        // with no, i.e. zero, correlation term
        public void minusEquals(ErrorProp a){
                this.value-=a.value;
                this.error = hypotWithCov(a.error, this.error, 0.0D);
        }

        // Subtract a double number from this ErrorProp number and replace this with the sum
        public void minusEquals(double a){
                this.value-=a;
                this.error=Math.abs(this.error);
        }

        //      MULTIPLICATION
        //Multiply two ErrorProp numbers with correlation [instance method]
        public ErrorProp times(ErrorProp a, double corrCoeff){
                ErrorProp c = new ErrorProp();
                double cov = corrCoeff*a.error*this.error;
                c.value=a.value*this.value;
                if(a.value==0.0D){
                    c.error=a.error*this.value;
                }
                else{
                    if(this.value==0.0D){
                        c.error=this.error*a.value;
                    }
                    else{
                        c.error = Math.abs(c.value)*hypotWithCov(a.error/a.value, this.error/this.value, corrCoeff);
                    }
                }
                return c;
        }

        //Multiply this ErrorProp number by a ErrorProp number [instance method]
        // with no, i.e. zero, correlation
        // this ErrorProp number remains unaltered
        public ErrorProp times(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value=this.value*a.value;
                if(a.value==0.0D){
                    b.error=a.error*this.value;
                }
                else{
                    if(this.value==0.0D){
                        b.error=this.error*a.value;
                    }
                    else{
                        b.error = Math.abs(b.value)*hypotWithCov(a.error/a.value, this.error/this.value, 0.0D);
                    }
                }
                return b;
        }

        //Multiply this ErrorProp number by a double [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp times(double a){
                ErrorProp b = new ErrorProp();
                b.value=this.value*a;
                b.error=Math.abs(this.error*a);
                return b;
        }


        //Multiply two ErrorProp numbers with correlation [static method]
        public static ErrorProp times(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                double cov = corrCoeff*a.error*b.error;
                c.value=a.value*b.value;
                if(a.value==0.0D){
                    c.error=a.error*b.value;
                }
                else{
                    if(b.value==0.0D){
                        c.error=b.error*a.value;
                    }
                    else{
                        c.error = Math.abs(c.value)*hypotWithCov(a.error/a.value, b.error/b.value, corrCoeff);
                    }
                }
                return c;
        }

        //Multiply two ErrorProp numbers with no, i.e. zero, correlation [static method]
        public static ErrorProp times(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a.value*b.value;
                if(a.value==0.0D){
                    c.error=a.error*b.value;
                }
                else{
                    if(b.value==0.0D){
                        c.error=b.error*a.value;
                    }
                    else{
                        c.error = Math.abs(c.value)*hypotWithCov(a.error/a.value, b.error/b.value, 0.0D);
                    }
                }
                return c;
        }

        //Multiply a double by a ErrorProp number [static method]
        public static ErrorProp times(double a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value=a*b.value;
                c.error=Math.abs(a*b.error);
                return c;
        }

        //Multiply a double number by a double and return product as ErrorProp [static method]
        public static ErrorProp times(double a, double b){
                ErrorProp c = new ErrorProp();
                c.value=a*b;
                c.error=0.0;
                return c;
        }

        //Multiply this ErrorProp number by an ErrorProp number and replace this by the product
        // with correlation
        public void timesEquals(ErrorProp a, double corrCoeff){
                ErrorProp b = new ErrorProp();
                double cov = corrCoeff*this.error*a.error;
                b.value = this.value*a.value;
                if(a.value==0.0D){
                    b.error=a.error*this.value;
                }
                else{
                    if(this.value==0.0D){
                        b.error=this.error*a.value;
                    }
                    else{
                        b.error = Math.abs(b.value)*hypotWithCov(a.error/a.value, this.error/this.value, corrCoeff);
                    }
                }

                this.value = b.value;
                this.error = b.error;
        }

        //Multiply this ErrorProp number by an ErrorProp number and replace this by the product
        // with no, i.e. zero, correlation
        public void timesEquals(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = this.value*a.value;
                if(a.value==0.0D){
                    b.error=a.error*this.value;
                }
                else{
                    if(this.value==0.0D){
                        b.error=this.error*a.value;
                    }
                    else{
                        b.error = Math.abs(b.value)*hypotWithCov(a.error/a.value, this.error/this.value, 0.0D);
                    }
                }

                this.value = b.value;
                this.error = b.error;
        }

        //Multiply this ErrorProp number by a double and replace this by the product
        public void timesEquals(double a){
                this.value=this.value*a;
                this.error=Math.abs(this.error*a);
        }

        // DIVISION
        // Division of this ErrorProp number by a ErrorProp number [instance method]
        // this ErrorProp number remains unaltered
        // with correlation
        public ErrorProp over(ErrorProp a, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value = this.value/a.value;
                if(this.value==0.0D){
                    c.error=this.error*a.value;
                }
                else{
                    c.error = Math.abs(c.value)*hypotWithCov(this.error/this.value, a.error/a.value, -corrCoeff);
                }
                return c;
        }

        // Division of two ErrorProp numbers a/b [static method]
        // with correlation
        public static ErrorProp over(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value = a.value/b.value;
                if(a.value==0.0D){
                    c.error=a.error*b.value;
                }
                else{
                    c.error = Math.abs(c.value)*hypotWithCov(a.error/a.value, b.error/b.value, -corrCoeff);
                }
                return c;
        }

        // Division of this ErrorProp number by a ErrorProp number [instance method]
        // this ErrorProp number remains unaltered
        // with no, i.e. zero, correlation
        public ErrorProp over(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = this.value/a.value;
                b.error = Math.abs(b.value)*hypotWithCov(a.error/a.value, this.error/this.value, 0.0);
                if(this.value==0.0D){
                    b.error=this.error*b.value;
                }
                else{
                    b.error = Math.abs(b.value)*hypotWithCov(a.error/a.value, this.error/this.value, 0.0);
                }
                return b;
        }

        // Division of two ErrorProp numbers a/b [static method]
        // with no, i.e. zero, correlation
        public static ErrorProp over(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = a.value/b.value;
                if(a.value==0.0D){
                    c.error=a.error*b.value;
                }
                else{
                    c.error = Math.abs(c.value)*hypotWithCov(a.error/a.value, b.error/b.value, 0.0D);
                }

                return c;
        }

        //Division of this ErrorProp number by a double [instance method]
        // this ErrorProp number remains unaltered
        public ErrorProp over(double a){
                ErrorProp b = new ErrorProp();
                b.value=this.value/a;
                b.error=Math.abs(this.error/a);
                return b;
        }


        // Division of a double, a, by a ErrorProp number, b  [static method]
        public static ErrorProp over(double a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = a/b.value;
                c.error = Math.abs(a*b.error/(b.value*b.value));
                return c;
        }

        // Divide a double number by a double and return quotient as ErrorProp [static method]
        public static ErrorProp over(double a, double b){
                ErrorProp c = new ErrorProp();
                c.value=a/b;
                c.error=0.0;
                return c;
        }

        // Division of this ErrorProp number by a ErrorProp number and replace this by the quotient
        // with no, i.r. zero, correlation
        public void overEquals(ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = this.value/b.value;
                if(this.value==0.0D){
                    c.error=this.error*b.value;
                }
                else{
                    c.error = Math.abs(c.value)*hypotWithCov(this.error/this.value, b.error/b.value, 0.0D);
                }
                this.value = c.value;
                this.error = c.error;
        }

        // Division of this ErrorProp number by a ErrorProp number and replace this by the quotient
        // with correlation
        public void overEquals(ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value = this.value/b.value;
                if(this.value==0.0D){
                    c.error=this.error*b.value;
                }
                else{
                    c.error = Math.abs(c.value)*hypotWithCov(this.error/this.value, b.error/b.value, -corrCoeff);
                }
                this.value = c.value;
                this.error = c.error;
        }

        //Division of this ErrorProp number by a double and replace this by the quotient
        public void overEquals(double a){
                this.value=this.value/a;
                this.error=Math.abs(this.error/a);
        }

        // RECIPROCAL
               // Returns the reciprocal (1/a) of a ErrorProp number (a) [instance method]
        public ErrorProp inverse(){
                ErrorProp b = ErrorProp.over(1.0D, this);
                return b;
        }

        // Returns the reciprocal (1/a) of a ErrorProp number (a) [static method]
        public static ErrorProp inverse(ErrorProp a){
                ErrorProp b = ErrorProp.over(1.0, a);
                return b;
        }

        //FURTHER MATHEMATICAL FUNCTIONS

        // Returns the length of the hypotenuse of a and b i.e. sqrt(a*a + b*b)
        // where a and b are ErrorProp [without unecessary overflow or underflow]
        // with correlation
        public static ErrorProp hypot(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value = Fmath.hypot(a.value, b.value);
                c.error = Math.abs(hypotWithCov(a.error*a.value, b.error*b.value, corrCoeff)/c.value);
                return c;
        }

        // Returns the length of the hypotenuse of a and b i.e. sqrt(a*a + b*b)
        // where a and b are ErrorProp [without unecessary overflow or underflow]
        // with no, i.e. zero, correlation
        public static ErrorProp hypot(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = Fmath.hypot(a.value, b.value);
                c.error = Math.abs(hypotWithCov(a.error*a.value, b.error*b.value, 0.0D)/c.value);
                return c;
        }

        //Absolute value [static method]
        public static ErrorProp abs(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.abs(a.value);
                b.error = Math.abs(a.error);
                return b;
        }

        //Absolute value  [instance method]
        public ErrorProp abs(){
                ErrorProp b = new ErrorProp();
                b.value = Math.abs(this.value);
                b.error = Math.abs(this.error);
                return b;
        }

        // Exponential
        public static ErrorProp exp(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.exp(a.value);
                b.error = Math.abs(b.value*a.error);
                return b;
        }

        // Natural log
        public static ErrorProp log(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.log(a.value);
                b.error = Math.abs(a.error/a.value);
                return b;
        }

        // log to base 10
        public static ErrorProp log10(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.log10(a.value);
                b.error = Math.abs(a.error/(a.value*Math.log(10.0D)));
                return b;
        }

        //Roots
        // Square root
        public static ErrorProp sqrt(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.sqrt(a.value);
                b.error = Math.abs(a.error/(2.0D*a.value));
                return b;
        }

        // The nth root (n = integer > 1)
        public static ErrorProp nthRoot(ErrorProp a, int n){
                if(n==0)throw new ArithmeticException("Division by zero (n = 0 - infinite root) attempted in ErrorProp.nthRoot");
                ErrorProp b = new ErrorProp();
                b.value = Math.pow(a.value, 1/n);
                b.error = Math.abs(a.error*Math.pow(a.value, 1/n-1)/((double)n));
                return b;
        }

        //Powers
        //Square [instance method]
        public ErrorProp square(){
                ErrorProp a = new ErrorProp(this.value, this.error);
                return a.times(a, 1.0D);
        }

        //Square [static method]
        public static ErrorProp square(ErrorProp a){
                return a.times(a,1.0D);
        }

        // returns an ErrorProp number raised to an error free power
        public static ErrorProp pow(ErrorProp a, double b){
                ErrorProp c = new ErrorProp();
                c.value = Math.pow(a.value, b);
                c.error = Math.abs(b*Math.pow(a.value, b-1.0));
                return c;
        }

        // returns an error free number raised to an ErrorProp power
        public static ErrorProp pow(double a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = Math.pow(a, b.value);
                c.error = Math.abs(c.value*Math.log(a)*b.error);
                return c;
        }

        // returns a ErrorProp number raised to a ErrorProp power
        // with correlation
        public static ErrorProp pow(ErrorProp a, ErrorProp b, double corrCoeff){
                ErrorProp c = new ErrorProp();
                c.value = Math.pow(a.value, b.value);
                c.error = hypotWithCov(a.error*b.value*Math.pow(a.value, b.value-1.0), b.error*Math.log(a.value)*Math.pow(a.value, b.value), corrCoeff);
                return c;
        }

        // returns a ErrorProp number raised to a ErrorProp power
        // with zero correlation
        public static ErrorProp pow(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                c.value = Math.pow(a.value, b.value);
                c.error = hypotWithCov(a.error*b.value*Math.pow(a.value, b.value-1.0), b.error*Math.log(a.value)*Math.pow(a.value, b.value), 0.0D);
                return c;
        }

        // ErrorProp trigonometric functions

        //Sine of an ErrorProp number
        public static ErrorProp sin(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.sin(a.value);
                b.error = Math.abs(a.error*Math.cos(a.value));
                return b;
        }

        //Cosine of an ErrorProp number
        public static ErrorProp cos(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.cos(a.value);
                b.error =  Math.abs(a.error*Math.sin(a.value));
                return b;
        }

        //Tangent of an ErrorProp number
        public static ErrorProp tan(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.tan(a.value);
                b.error = Math.abs(a.error*Fmath.square(Fmath.sec(a.value)));
                return b;
        }

        //Hyperbolic sine of a ErrorProp number
        public static ErrorProp sinh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.sinh(a.value);
                b.error = Math.abs(a.error*Fmath.cosh(a.value));
                return b;
        }

        //Hyperbolic cosine of a ErrorProp number
        public static ErrorProp cosh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.cosh(a.value);
                b.error = Math.abs(a.error*Fmath.sinh(a.value));
                return b;
        }

        //Hyperbolic tangent of a ErrorProp number
        public static ErrorProp tanh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.tanh(a.value);
                b.error = Math.abs(a.error*Fmath.square(Fmath.sech(a.value)));
                return b;
        }

        //Inverse sine of a ErrorProp number
        public static ErrorProp asin(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.asin(a.value);
                b.error = Math.abs(a.error/Math.sqrt(1.0D - a.value*a.value));
                return b;
        }

        //Inverse cosine of a ErrorProp number
        public static ErrorProp acos(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.acos(a.value);
                b.error = Math.abs(a.error/Math.sqrt(1.0D - a.value*a.value));
                return b;
        }

        //Inverse tangent of a ErrorProp number
        public static ErrorProp atan(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Math.atan(a.value);
                b.error = Math.abs(a.error/(1.0D + a.value*a.value));
                return b;
        }

        //Inverse tangent (atan2) of a ErrorProp number - no correlation
        public static ErrorProp atan2(ErrorProp a, ErrorProp b){
                ErrorProp c = new ErrorProp();
                ErrorProp d = a.over(b);
                c.value = Math.atan2(a.value, b.value);
                c.error = Math.abs(d.error/(1.0D + d.value*d.value));
                return c;
        }
        //Inverse tangent (atan2) of a ErrorProp number - correlation
        public static ErrorProp atan2(ErrorProp a, ErrorProp b, double rho){
                ErrorProp c = new ErrorProp();
                ErrorProp d = a.over(b, rho);
                c.value = Math.atan2(a.value, b.value);
                c.error = Math.abs(d.error/(1.0D + d.value*d.value));
                return c;
        }

        //Inverse hyperbolic sine of a ErrorProp number
        public static ErrorProp asinh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.asinh(a.value);
                b.error = Math.abs(a.error/Math.sqrt(a.value*a.value + 1.0D));
                return b;
        }

        //Inverse hyperbolic cosine of a ErrorProp number
        public static ErrorProp acosh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.acosh(a.value);
                b.error = Math.abs(a.error/Math.sqrt(a.value*a.value - 1.0D));
                return b;
        }

        //Inverse hyperbolic tangent of a ErrorProp number
        public static ErrorProp atanh(ErrorProp a){
                ErrorProp b = new ErrorProp();
                b.value = Fmath.atanh(a.value);
                b.error = Math.abs(a.error/(1.0D - a.value*a.value));
                return b;
        }

        // SOME USEFUL NUMBERS
        // returns the number zero (0) with zero error
        public static ErrorProp zero(){
                ErrorProp c = new ErrorProp();
                c.value=0.0D;
                c.error=0.0D;
                return c;
        }

        // returns the number one (+1) with zero error
        public static ErrorProp plusOne(){
                ErrorProp c = new ErrorProp();
                c.value=1.0D;
                c.error=0.0D;
                return c;
        }

        // returns the number minus one (-1) with zero error
        public static ErrorProp minusOne(){
                ErrorProp c = new ErrorProp();
                c.value=-1.0D;
                c.error=0.0D;
                return c;
        }

        // Private methods
        // Safe calculation of sqrt(a*a + b*b + 2*r*a*b)
        private static double hypotWithCov(double a, double b, double r){
        double pre=0.0D, ratio=0.0D, sgn=0.0D;

                if(a==0.0D && b==0.0D)return 0.0D;
                if(Math.abs(a)>Math.abs(b)){
                        pre = Math.abs(a);
                        ratio = b/a;
                        sgn = Fmath.sign(a);
                }
                else{
                        pre = Math.abs(b);
                        ratio = a/b;
                        sgn = Fmath.sign(b);
                }
                return pre*Math.sqrt(1.0D + ratio*(ratio + 2.0D*r*sgn));
        }
}

