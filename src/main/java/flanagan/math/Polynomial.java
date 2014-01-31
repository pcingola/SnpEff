/*
*   Class   Polynomial
*
*   Defines a polynomial
*   y = a[0] + a[1].x + a[2].x^2 + a[3].3 + . . . + a[n].x^n
*   where x and all a[i] are real
*   and deg is the degree of the polynomial, i.e. n,
*   and includes the methods associated with polynomials,
*
*   See clas ComplexPoly for Complex Polynomials
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   See class Complex for standard complex arithmetic
*
*   DATE:    6 June 2010 (adapted from ComplexPoly [February 2002])
*   UPDATED: 21 January 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Polynomial.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*
*   Copyright (c) 2002 - 2011
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.math;

import flanagan.io.FileOutput;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.analysis.Stat;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;

import java.util.ArrayList;
import java.math.*;


public class Polynomial{

        private int deg = 0;                // Degree of the polynomial
        private int degwz = 0;              // Degree of the polynomial with zero roots removed
        private double[] coeff;             // Array of polynomial coefficients
        private double[] coeffwz;           // Array of polynomial coefficients with zero roots removed

        private boolean suppressRootsErrorMessages = false;  // = true if suppression of 'null returned' error messages in roots is required

        // CONSTRUCTORS
        public Polynomial(int n){
                this.deg = n;
                this.coeff = new double[n+1];
        }

        // Coefficients are double
        public Polynomial(double[] aa){
                this.deg =aa.length-1;
                this.coeff = new double[this.deg+1];
                for(int i=0; i<=this.deg; i++){
                        this.coeff[i]=aa[i];
                }
        }

        // Coefficients are real (float)
        public Polynomial(float[] aa){
                this.deg =aa.length-1;
                coeff = new double[this.deg+1];
                for(int i=0; i<=deg; i++){
                        this.coeff[i] = (double)aa[i];
                }
        }

        // Coefficients are long
        public Polynomial(long[] aa){
                this.deg =aa.length-1;
                coeff = new double[this.deg+1];
                for(int i=0; i<=deg; i++){
                        this.coeff[i] = (double)aa[i];
                }
        }

        // Coefficients are int
        public Polynomial(int[] aa){
                this.deg =aa.length-1;
                coeff = new double[this.deg+1];
                for(int i=0; i<=deg; i++){
                        this.coeff[i] = (double)aa[i];
                }
        }

        // Coefficients are in an a ArrayList - each element may be any relevant type -
        // The ArrayList must contain the individual coefficients as irs elements and not as an array
        public Polynomial(ArrayList<Object> aa){
                this.deg = aa.size()-1;
                coeff = new double[this.deg+1];
                for(int i=0; i<=deg; i++){
                    int code = this.getTypeCode((Object)aa.get(i));
                    switch(code){
                        case 1: // Byte
                                this.coeff[i] = (double)((Byte)aa.get(i));
                                break;
                        case 2: // Short
                                this.coeff[i] = (double)((Short)aa.get(i));
                                break;
                        case 3: // Integer
                                this.coeff[i] = (double)((Integer)aa.get(i));
                                break;
                        case 4: // Long
                                this.coeff[i] = (double)((Long)aa.get(i));
                                break;
                        case 5: // Float
                                this.coeff[i] = (double)((Float)aa.get(i));
                                break;
                        case 6: // Double
                                this.coeff[i] = (double)((Double)aa.get(i));
                                break;
                        case 7: // BigInteger
                                this.coeff[i] = (double)((BigInteger)aa.get(i)).doubleValue();
                                break;
                        case 8:// BigDecimal
                                this.coeff[i] = (double)((BigDecimal)aa.get(i)).doubleValue();
                                break;
                        default: throw new IllegalArgumentException("Type code, " + code + ", not recognised");
                    }
                }
        }

        // Returns a code indicating the type of oObject passed as the argument
        // Called by ArrayList constructor
        private int getTypeCode(Object obj){

                int code = 0;

                if(obj instanceof Byte){
                    code = 1;
                }
                else{
                    if(obj instanceof Short){
                        code = 2;
                    }
                    else{
                        if(obj instanceof Integer){
                            code = 3;
                        }
                        else{
                            if(obj instanceof Long){
                                code = 4;
                            }
                            else{
                                if(obj instanceof Float){
                                    code = 5;
                                }
                                else{
                                    if(obj instanceof Double){
                                        code = 6;
                                    }
                                    else{
                                        if(obj instanceof BigInteger){
                                            code = 7;
                                        }
                                        else{
                                            if(obj instanceof BigDecimal){
                                                code = 8;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return code;
        }


        // Single constant -  double
        // y = aa
        // needed in class Loop
        public Polynomial(double aa){
                this.deg = 0;
                coeff = new double[1];
                this.coeff[0] = aa;
        }

        // Straight line - coefficients are double
        // y = aa + bb.x
        public Polynomial(double aa, double bb){
                this.deg = 1;
                coeff = new double[2];
                this.coeff[0] = aa;
                this.coeff[1] = bb;
        }

        // Quadratic - coefficients are double
        // y = aa + bb.x + cc.x^2
        public Polynomial(double aa, double bb, double cc){
                this.deg = 2;
                coeff = new double[3];
                this.coeff[0] = aa;
                this.coeff[1] = bb;
                this.coeff[2] = cc;
        }


        // Cubic - coefficients are double
        // y = aa + bb.x + cc.x^2 + dd.x^3
        public Polynomial(double aa, double bb, double cc, double dd){
                this.deg = 3;
                coeff = new double[4];
                this.coeff[0] = aa;
                this.coeff[1] = bb;
                this.coeff[2] = cc;
                this.coeff[3] = dd;
        }


        // METHODS

        // Returns a Polynomial given the polynomial's roots
        public static Polynomial rootsToPoly(double[] roots){
            if(roots==null)return null;

            int pdeg = roots.length;

            double[] rootCoeff = new double[2];
            rootCoeff[0] = -roots[0];
            rootCoeff[1] = 1.0;
            Polynomial rPoly = new Polynomial(rootCoeff);
            for(int i=1; i<pdeg; i++){
                    rootCoeff[0] = -roots[i];
                    Polynomial cRoot = new Polynomial(rootCoeff);
                    rPoly = rPoly.times(cRoot);
            }
            return rPoly;
        }

        // Reset the polynomial  with double[]
        public void resetPoly(double [] aa){
                if((this.deg+1)!=aa.length)throw new IllegalArgumentException("array lengths do not match");
                for(int i=0; i<this.deg; i++){
                        this.coeff[i] = aa[i];
                }
        }

         // Reset the polynomial  with ArrayList
        public void resetPoly(ArrayList<Object> aa){
                if((this.deg+1)!=aa.size())throw new IllegalArgumentException("array lengths do not match");
                for(int i=0; i<=deg; i++){
                    int code = this.getTypeCode((Object)aa.get(i));
                    switch(code){
                        case 1: // Byte
                                this.coeff[i] = (double)((Byte)aa.get(i));
                                break;
                        case 2: // Short
                                this.coeff[i] = (double)((Short)aa.get(i));
                                break;
                        case 3: // Integer
                                this.coeff[i] = (double)((Integer)aa.get(i));
                                break;
                        case 4: // Long
                                this.coeff[i] = (double)((Long)aa.get(i));
                                break;
                        case 5: // Float
                                this.coeff[i] = (double)((Float)aa.get(i));
                                break;
                        case 6: // Double
                                this.coeff[i] = (double)((Double)aa.get(i));
                                break;
                        case 7: // BigInteger
                                this.coeff[i] = ((BigInteger)aa.get(i)).doubleValue();
                                break;
                        case 8: // BigDecimal
                                this.coeff[i] = ((BigDecimal)aa.get(i)).doubleValue();
                                break;
                        default: throw new IllegalArgumentException("Type code, " + code + ", not recognised");
                    }
                }
        }


        // Reset a coefficient
        public void resetCoeff(int i, double aa){
                this.coeff[i] = aa;
        }

        // Return a copy of this Polynomial [instance method]
        public Polynomial copy(){
            if(this==null){
                return null;
            }
            else{
                Polynomial aa = new Polynomial(this.deg);
                for(int i=0; i<=this.deg; i++){
                        aa.coeff[i] = this.coeff[i];
                }
                aa.deg = this.deg;
                aa.degwz = this.degwz;
                aa.coeffwz = Conv.copy(this.coeffwz);
                return aa;
            }
        }

        // Return a copy of this Polynomial [static]
        public static Polynomial copy(Polynomial bb){
            if(bb==null){
                return null;
            }
            else{
                Polynomial aa = new Polynomial(bb.deg);
                for(int i=0; i<=bb.deg; i++){
                        aa.coeff[i] = bb.coeff[i];
                }
                aa.deg = bb.deg;
                aa.degwz = bb.degwz;
                aa.coeffwz = Conv.copy(bb.coeffwz);
                return aa;
            }
        }

        // Clone a Polynomial
        public Object clone(){
                return (Object) this.copy();
        }

        // Return a copy of the polynomial coefficients
        public double[] coefficientsCopy(){
                double[] aa = new double[this.deg+1];
                for(int i=0; i<=this.deg; i++){
                        aa[i] = this.coeff[i];
                }
                return aa;
        }

        // Return a reference to the polynomial coefficients array
        public double[] coefficientsReference(){
                return this.coeff;
        }

        // Return a coefficient
        public double getCoefficient(int i){
                return this.coeff[i];
        }

        // Return the degree
        public int getDeg(){
                return this.deg;
        }


        // Convert to a String of the form a[0] + a[1].x + a[2].x^2  etc.
        public String toString(){
                String ss = "";
                ss =  ss + this.coeff[0];
                if(this.deg>0)ss = ss + " + (" + this.coeff[1] + ").x";
                for(int i=2; i<=this.deg; i++){
                    ss = ss + " + (" + this.coeff[i] + ").x^" + i;
                }
                return ss;
        }

        // Convert to a ComplexPoly
        public ComplexPoly toComplexPoly(){
            ComplexPoly cp = new ComplexPoly(this);
            return cp;
        }


        // Print the polynomial to screen
        public void print(){
                System.out.print(this.toString());
        }

        // Print the polynomial to screen with line return
        public void println(){
                System.out.println(this.toString());
        }

        // Print the polynomial to a text file with title
        public void printToText(String title){
                title = title + ".txt";
                FileOutput fout = new FileOutput(title, 'n');

                fout.println("Output File for a Polynomial");
                fout.dateAndTimeln();
                fout.println();
                fout.print("Polynomial degree is ");
                fout.println(this.deg);
                fout.println();
                fout.println("The coefficients are ");

                for(int i=0;i<=this.deg;i++){
                        fout.println(this.coeff[i]);
                }
                fout.println();
                fout.println("End of file.");
                fout.close();
        }

        // Print the polynomial to a text file without a given title
        public void printToText(){
                String title = "PolynomialOut";
                printToText(title);
        }

        // TRANSFORM
        // Transform a real polynomial to an s-Domain polynomial ratio
        // Returns ArrayList:
        // First element: numerator as ComplexPoly
        // Second element: denominator as ComplexPoly
        // Instance method
        public ArrayList<ComplexPoly> sTransform(){
            return Polynomial.sTransform(this.coeff);
        }

        // TRANSFORM
        // Transform a real polynomial to an s-Domain polynomial ratio
        // Argument:  Polynomial  a0 + a1.x + a2.x^2 + ....
        // Returns ArrayList:
        // First element: numerator as ComplexPoly
        // Second element: denominator as ComplexPoly
        // Static method
        public static ArrayList<ComplexPoly> sTransform(Polynomial poly){
            return Polynomial.sTransform(poly.coefficientsCopy());
        }


        // TRANSFORM
        // Transform a real polynomial to an s-Domain polynomial ratio
        // Argument:  coefficients of the real polynomial  a0 + a1.x + a2.x^2 + ....
        // Returns ArrayList:
        // First element: numerator as ComplexPoly
        // Second element: denominator as ComplexPoly
        // Static method
        public static ArrayList<ComplexPoly> sTransform(double[] coeff){
            int n = coeff.length;
            ComplexPoly[] sNum = new ComplexPoly[n];                    // numerator of each transformed term
            ComplexPoly[] sDen = new ComplexPoly[n];                    // denomenator of each transformed term
            ComplexPoly sNumer = null;                                  // numerator of the completely transformed polynomial
            ComplexPoly sDenom = new ComplexPoly(Complex.plusOne());    // denomenator of the completely transformed polynomial

            // s-Transform of each term of the polynomial
            for(int i=0; i<n; i++){
                sNum[i] = new ComplexPoly(new Complex(coeff[i]*Fmath.factorial(i), 0));
                sDen[i] = new ComplexPoly(i+1);
                sDen[i].resetCoeff(i+1, Complex.plusOne());
            }

            // create a common denomenator
            sDenom = sDen[n-1];

            // create a common numerator
            for(int i=0; i<n-1; i++){
                sNum[i] = sNum[i].times(sDen[n-i-2]);
            }
            sNumer = sNum[0];
            for(int i=1; i<n; i++)sNumer = sNumer.plus(sNum[i]);

            // Output arrayList
            ArrayList<ComplexPoly> al = new ArrayList<ComplexPoly>();
            al.add(sNumer);
            al.add(sDenom);

            return al;
        }



        // LOGICAL TESTS
        // Check if two polynomials are identical
        public boolean equals(Polynomial cp){
            return isEqual(cp);
        }

        public boolean isEqual(Polynomial cp){
            boolean ret = false;
            int nDegThis = this.getDeg();
            int nDegCp = cp.getDeg();
            if(nDegThis==nDegCp){
                boolean test = true;
                int i=0;
                while(test){
                    if(this.coeff[i]!=cp.getCoefficient(i)){
                        test = false;
                    }
                    else{
                        i++;
                        if(i>nDegCp){
                            test = false;
                            ret = true;
                        }
                    }
                }
            }
            return ret;
        }

        // Check if two polynomials are identical (static)
        public static boolean isEqual(Polynomial cp1, Polynomial cp2){
            boolean ret = false;
            int nDegCp1 = cp1.getDeg();
            int nDegCp2 = cp2.getDeg();
            if(nDegCp1==nDegCp2){
                boolean test = true;
                int i=0;
                while(test){
                    if(cp1.getCoefficient(i)!=cp2.getCoefficient(i)){
                        test = false;
                    }
                    else{
                        i++;
                        if(i>nDegCp1){
                            test = false;
                            ret = true;
                        }
                    }
                }
            }
            return ret;
        }

        // ADDITION OF TWO POLYNOMIALS
        // Addition,  instance method
        public Polynomial plus(Polynomial b){

                Polynomial c = null;
                if(b.deg<=this.deg){
                        c = new Polynomial(this.deg);
                        for(int i=b.deg+1; i<=this.deg; i++)c.coeff[i] = this.coeff[i];
                        for(int i=0; i<=b.deg; i++)c.coeff[i] = this.coeff[i] + b.coeff[i];
                }
                else{
                        c = new Polynomial(b.deg);
                        for(int i=this.deg+1; i<=b.deg; i++)c.coeff[i] = b.coeff[i];
                        for(int i=0; i<=this.deg; i++)c.coeff[i] =  this.coeff[i] + b.coeff[i];
                }
                return c;
        }

        // Addition,  static method
        public static Polynomial plus(Polynomial a, Polynomial b){
                Polynomial c = null;
                if(b.deg<=a.deg){
                        c = new Polynomial(a.deg);
                        for(int i=b.deg+1; i<=a.deg; i++)c.coeff[i] = a.coeff[i];
                        for(int i=0; i<=b.deg; i++)c.coeff[i] = a.coeff[i] + b.coeff[i];
                }
                else{
                        c = new Polynomial(b.deg);
                        for(int i=a.deg+1; i<=b.deg; i++)c.coeff[i] = b.coeff[i];
                        for(int i=0; i<=a.deg; i++)c.coeff[i] =  a.coeff[i] + b.coeff[i];
                }
                return c;
        }

        // Addition of a double,  instance method
        public Polynomial plus(double bb){
                Polynomial b = new Polynomial(bb);
                return this.plus(b);
        }

        // Addition of a double,  static method
        public static Polynomial plus(Polynomial a, double bb){
                Polynomial b = new Polynomial(bb);
                return Polynomial.plus(a, b);
        }

        // Addition of an int,  instance method
        public Polynomial plus(int bb){
                Polynomial b = new Polynomial((double) bb);
                return this.plus(b);
        }

        // Addition of an int,  static method
        public static Polynomial plus(Polynomial a, int bb){
                Polynomial b = new Polynomial((double)bb);
                return Polynomial.plus(a, b);
        }

        // SUBTRACTION OF TWO POLYNOMIALS
        // Subtraction,  instance method
        public Polynomial minus(Polynomial b){

                Polynomial c = null;
                if(b.deg<=this.deg){
                        c = new Polynomial(this.deg);
                        for(int i=b.deg+1; i<=this.deg; i++)c.coeff[i] = this.coeff[i];
                        for(int i=0; i<=b.deg; i++)c.coeff[i] = this.coeff[i] - b.coeff[i];
                }
                else{
                        c = new Polynomial(b.deg);
                        for(int i=this.deg+1; i<=b.deg; i++)c.coeff[i] = -b.coeff[i];
                        for(int i=0; i<=this.deg; i++)c.coeff[i] =  this.coeff[i] - b.coeff[i];
                }
                return c;
        }

        // Subtraction of a double,  instance method
        public Polynomial minus(double bb){
                Polynomial b = new Polynomial(bb);
                return this.minus(b);
        }

        // Subtraction of an int,  instance method
        public Polynomial minus(int bb){
                Polynomial b = new Polynomial((double)bb);
                return this.minus(b);
        }

        // Subtraction,  static method
        public static Polynomial minus(Polynomial a, Polynomial b){
                Polynomial c = null;
                if(b.deg<=a.deg){
                        c = new Polynomial(a.deg);
                        for(int i=b.deg+1; i<=a.deg; i++)c.coeff[i] = a.coeff[i];
                        for(int i=0; i<=b.deg; i++)c.coeff[i] = a.coeff[i] - b.coeff[i];
                }
                else{
                        c = new Polynomial(b.deg);
                        for(int i=a.deg+1; i<=b.deg; i++)c.coeff[i] = -b.coeff[i];
                        for(int i=0; i<=a.deg; i++)c.coeff[i] =  a.coeff[i] - b.coeff[i];
                }
                return c;
        }

        // Subtraction  of a double,  static method
        public static Polynomial minus(Polynomial a, double bb){
            Polynomial b = new Polynomial(bb);
            return Polynomial.minus(a, b);
        }

        // Subtraction  of a int,  static method
        public static Polynomial minus(Polynomial a, int bb){
            Polynomial b = new Polynomial((double)bb);
            return Polynomial.minus(a, b);
        }


        // MULTIPLICATION OF TWO POLYNOMIALS
        // Multiplication,  instance method
        public Polynomial times(Polynomial b){
                int n = this.deg + b.deg;
                Polynomial c = new Polynomial(n);
                for(int i=0; i<=this.deg; i++){
                        for(int j=0; j<=b.deg; j++){
                                c.coeff[i+j] += (this.coeff[i]*b.coeff[j]);
                        }
                }
                return c;
        }

        // Multiplication,  static method
        public static Polynomial times(Polynomial a, Polynomial b){
                int n = a.deg + b.deg;
                Polynomial c = new Polynomial(n);
                for(int i=0; i<=a.deg; i++){
                        for(int j=0; j<=b.deg; j++){
                                c.coeff[i+j] += (a.coeff[i]*b.coeff[j]);
                        }
                }
                return c;
        }

        // Multiplication by a double,  instance method
        public Polynomial times(double bb){
                Polynomial c = new Polynomial(this.deg);
                for(int i=0; i<=this.deg; i++){
                        c.coeff[i] = this.coeff[i]*bb;
                }
                return c;
        }

        // Multiplication by a double,  static method
        public static Polynomial times(Polynomial a, double bb){
                Polynomial c = new Polynomial(a.deg);
                for(int i=0; i<=a.deg; i++){
                        c.coeff[i] = a.coeff[i]*bb;
                }
                return c;
        }

        // Multiplication by an int,  instance method
        public Polynomial times(int bb){
                Polynomial c = new Polynomial(this.deg);
                for(int i=0; i<=this.deg; i++){
                        c.coeff[i] = this.coeff[i]*bb;
                }
                return c;
        }

        // Multiplication by an int,  static method
        public static Polynomial times(Polynomial a, int bb){
                Polynomial c = new Polynomial(a.deg);
                for(int i=0; i<=a.deg; i++){
                        c.coeff[i] = a.coeff[i]*bb;
                }
                return c;
        }


        // DERIVATIVES
        // Return the coefficients, as a new Polynomial,  of the nth derivative
        public Polynomial nthDerivative(int n){
                Polynomial dnydxn;

                if(n>this.deg){
                        dnydxn = new Polynomial(0.0);
                }
                else{
                        dnydxn = new Polynomial(this.deg-n);
                        double[] nc = new double[this.deg - n + 1];

                        int k = this.deg - n;
                        for(int i=this.deg; i>n-1; i--){
                                nc[k] = this.coeff[i];
                                for(int j=0; j<n; j++){
                                        nc[k] = nc[k]*(i-j);
                                }
                                k--;
                        }
                        dnydxn = new Polynomial(nc);
                }
                return dnydxn;
        }

        // EVALUATION OF A POLYNOMIAL AND ITS DERIVATIVES
        // Evaluate the polynomial
        public double evaluate(double x){
                double y = 0.0;
                if(this.deg==0){
                        y = this.coeff[0];
                }
                else{
                        y = this.coeff[this.deg];
                        for(int i=this.deg-1; i>=0; i--){
                                y = y*x + this.coeff[i];
                        }
                }
                return y;
        }

        // Evaluate the nth derivative of the polynomial
        public double nthDerivEvaluate(int n, double x){
                double dnydxn = 0.0;
                double[] nc = new double[this.deg+1];

                if(n==0)
                {
                        dnydxn=this.evaluate(x);
                        System.out.println("n = 0 in Polynomial.nthDerivative");
                        System.out.println("polynomial itself evaluated and returned");
                }
                else{
                        Polynomial nthderiv = this.nthDerivative(n);
                        dnydxn = nthderiv.evaluate(x);
                }
                return dnydxn;
        }



        // ROOTS OF POLYNOMIALS
        // For general details of root searching and a discussion of the rounding errors
        // see Numerical Recipes, The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/

        // Calculate the roots (real or double) of a polynomial (real or double)
        // polish = true ([for deg>3 see laguerreAll(...)]
        // initial root estimates are all zero [for deg>3 see laguerreAll(...)]
        public Complex[] roots(){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.roots();
        }

        // Calculate the roots - as above with the exception that the error messages are suppressed
        // Required by BlackBox
        public Complex[] rootsNoMessages(){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.rootsNoMessages();
        }

        // Calculate the roots (real or double) of a polynomial (real or double)
        // initial root estimates are all zero [for deg>3 see laguerreAll(...)]
        // for polish  see laguerreAll(...)[for deg>3]
        public Complex[] roots(boolean polish){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.roots(polish);
        }

        // Calculate the roots (real or double) of a polynomial (real or double)
        // for estx  see laguerreAll(...)[for deg>3] - initial estimate of first root
        // polish = true  see laguerreAll(...)[for deg>3]
        public Complex[] roots(double estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.roots(new Complex(estx, 0.0));
        }

        public Complex[] roots(Complex estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.roots(estx);
        }

        // Calculate the roots (real or complex) of a polynomial (real or complex)
        public Complex[] roots(boolean polish, double estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.roots(new Complex(estx, 0.0));
        }

        // ROOTS OF A QUADRATIC EQUATION
        // ax^2 + bx + c = 0
        // roots returned in root[]
        // 4ac << b*b accomodated by these methods
        public static Complex[] quadratic(double c, double b, double a){
            return ComplexPoly.quadratic(new Complex(c, 0.0), new Complex(b, 0.0), new Complex(a, 0.0));
        }

        // ROOTS OF A CUBIC EQUATION
        // ddx^3 + ccx^2 + bbx + aa = 0
        // roots returned in root[]
        public static Complex[] cubic(double aa, double bb, double cc, double dd){
            return ComplexPoly.cubic(new Complex(aa, 0.0), new Complex(bb, 0.0), new Complex(cc, 0.0), new Complex(dd, 0.0));
        }


        // LAGUERRE'S METHOD FOR double ROOTS OF A double POLYNOMIAL

        // Laguerre method for one of the roots
        // Following the procedure in Numerical Recipes for C [Reference above]
        // estx     estimate of the root
        // coeff[]  coefficients of the polynomial
        // m        degree of the polynomial
        public static Complex laguerre(double estx, double[] pcoeff, int m){
            ArrayMaths am = new ArrayMaths(pcoeff);
            return ComplexPoly.laguerre(new Complex(estx, 0.0), am.array_as_Complex(), m);
        }

        public static Complex laguerre(Complex estx, double[] pcoeff, int m){
            ArrayMaths am = new ArrayMaths(pcoeff);
            return ComplexPoly.laguerre(estx, am.array_as_Complex(), m);
        }

        // Finds all roots of a double polynomial by successive calls to laguerre
        // Following the procedure in Numerical Recipes for C [Reference above]
        // Initial estimates are all zero, polish=true
        public Complex[] laguerreAll(){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll();
        }

        //  Initial estimates estx, polish=true
        public Complex[] laguerreAll(double estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll(new Complex(estx, 0.0));
        }

        public Complex[] laguerreAll(Complex estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll(estx);
        }

        //  Initial estimates are all zero.
        public Complex[] laguerreAll(boolean polish){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll(polish);
        }

        // Finds all roots of a double polynomial by successive calls to laguerre
        //  Initial estimates are estx
        public Complex[] laguerreAll(boolean polish, double estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll(polish, new Complex(estx, 0.0));
        }

        public Complex[] laguerreAll(boolean polish, Complex estx){
            ComplexPoly cp = new ComplexPoly(this);
            return cp.laguerreAll(polish, estx);
        }
}

