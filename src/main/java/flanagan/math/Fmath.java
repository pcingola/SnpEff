/*
*   Class   Fmath
*
*   USAGE:  Mathematical class that supplements java.lang.Math and contains:
*               the main physical constants
*               trigonemetric functions absent from java.lang.Math
*               some useful additional mathematical functions
*               some conversion functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002
*   AMENDED: 6 January 2006, 12 April 2006, 5 May 2006, 28 July 2006, 27 December 2006,
*            29 March 2007, 29 April 2007, 2,9,15 & 26 June 2007, 20 October 2007, 4-6 December 2007
*            27 February 2008, 25 April 2008, 26 April 2008, 13 May 2008, 25/26 May 2008, 3-7 July 2008
*            11 November 2010, 9-18 January 2011, 13 August 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Fmath.html
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


import java.util.ArrayList;
import java.util.Vector;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Fmath{

        // PHYSICAL CONSTANTS

        public static final double N_AVAGADRO = 6.0221419947e23;        /*      mol^-1          */
        public static final double K_BOLTZMANN = 1.380650324e-23;       /*      J K^-1          */
        public static final double H_PLANCK = 6.6260687652e-34;         /*      J s             */
        public static final double H_PLANCK_RED = H_PLANCK/(2*Math.PI); /*      J s             */
        public static final double C_LIGHT = 2.99792458e8;              /*      m s^-1          */
        public static final double R_GAS = 8.31447215;                  /*      J K^-1 mol^-1   */
        public static final double F_FARADAY = 9.6485341539e4;          /*      C mol^-1        */
        public static final double T_ABS = -273.15;                     /*      Celsius         */
        public static final double Q_ELECTRON = -1.60217646263e-19;     /*      C               */
        public static final double M_ELECTRON = 9.1093818872e-31;       /*      kg              */
        public static final double M_PROTON = 1.6726215813e-27;         /*      kg              */
        public static final double M_NEUTRON = 1.6749271613e-27;        /*      kg              */
        public static final double EPSILON_0 = 8.854187817e-12;         /*      F m^-1          */
        public static final double MU_0 = Math.PI*4e-7;                 /*      H m^-1 (N A^-2) */
        public static final double ETA_0 = MU_0*C_LIGHT;                /*      Ohms            */


        // MATHEMATICAL CONSTANTS
        public static final double EULER_CONSTANT_GAMMA = 0.5772156649015627;
        public static final double PI = Math.PI;                        /*  3.141592653589793D  */
        public static final double E = Math.E;                          /*  2.718281828459045D  */

        // HashMap for 'arithmetic integer' recognition nmethod
        private static final Map<Object,Object> integers = new HashMap<Object,Object>();
        static{
            integers.put(Integer.class, BigDecimal.valueOf(Integer.MAX_VALUE));
            integers.put(Long.class, BigDecimal.valueOf(Long.MAX_VALUE));
            integers.put(Byte.class, BigDecimal.valueOf(Byte.MAX_VALUE));
            integers.put(Short.class, BigDecimal.valueOf(Short.MAX_VALUE));
            integers.put(BigInteger.class, BigDecimal.valueOf(-1));
        }

        // METHODS

        // LOGARITHMS
        // Log to base 10 of a double number
        public static double log10(double a){
            return Math.log(a)/Math.log(10.0D);
        }

        // Log to base 10 of a float number
        public static float log10(float a){
            return (float) (Math.log((double)a)/Math.log(10.0D));
        }

        // Base 10 antilog of a double
        public static double antilog10(double x){
            return Math.pow(10.0D, x);
        }

        // Base 10 antilog of a float
        public static float antilog10(float x){
            return (float)Math.pow(10.0D, (double)x);
        }

        // Log to base e of a double number
        public static double log(double a){
            return Math.log(a);
        }

        // Log to base e of a float number
        public static float log(float a){
            return (float)Math.log((double)a);
        }

        // Base e antilog of a double
        public static double antilog(double x){
            return Math.exp(x);
        }

        // Base e antilog of a float
        public static float antilog(float x){
            return (float)Math.exp((double)x);
        }

        // Log to base 2 of a double number
        public static double log2(double a){
            return Math.log(a)/Math.log(2.0D);
        }

        // Log to base 2 of a float number
        public static float log2(float a){
            return (float) (Math.log((double)a)/Math.log(2.0D));
        }

        // Base 2 antilog of a double
        public static double antilog2(double x){
            return Math.pow(2.0D, x);
        }

        // Base 2 antilog of a float
        public static float antilog2(float x){
            return (float)Math.pow(2.0D, (double)x);
        }

        // Log to base b of a double number and double base
        public static double log10(double a, double b){
            return Math.log(a)/Math.log(b);
        }

        // Log to base b of a double number and int base
        public static double log10(double a, int b){
            return Math.log(a)/Math.log((double)b);
        }

        // Log to base b of a float number and flaot base
        public static float log10(float a, float b){
            return (float) (Math.log((double)a)/Math.log((double)b));
        }

        // Log to base b of a float number and int base
        public static float log10(float a, int b){
            return (float) (Math.log((double)a)/Math.log((double)b));
        }

        // SQUARES
        // Square of a double number
        public static double square(double a){
            return a*a;
        }

        // Square of a float number
        public static float square(float a){
            return a*a;
        }

        // Square of a BigDecimal number
        public static BigDecimal square(BigDecimal a){
            return a.multiply(a);
        }

        // Square of an int number
        public static int square(int a){
            return a*a;
        }

        // Square of a long number
        public static long square(long a){
            return a*a;
        }

        // Square of a BigInteger number
        public static BigInteger square(BigInteger a){
            return a.multiply(a);
        }

        // FACTORIALS
        // factorial of n
        // argument and return are integer, therefore limited to 0<=n<=12
        // see below for long and double arguments
        public static int factorial(int n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>12)throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
            int f = 1;
            for(int i=2; i<=n; i++)f*=i;
            return f;
        }

        // factorial of n
        // argument and return are long, therefore limited to 0<=n<=20
        // see below for double argument
        public static long factorial(long n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>20)throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
            long f = 1;
            long iCount = 2L;
            while(iCount<=n){
                f*=iCount;
                iCount += 1L;
            }
            return f;
        }

        // factorial of n
        // Argument is of type BigInteger
        public static BigInteger factorial(BigInteger n){
            if(n.compareTo(BigInteger.ZERO)==-1)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            BigInteger one = BigInteger.ONE;
            BigInteger f = one;
            BigInteger iCount = new BigInteger("2");
            while(iCount.compareTo(n)!=1){
                f = f.multiply(iCount);
                iCount = iCount.add(one);
            }
            one = null;
            iCount = null;
            return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
            if(n<0.0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 1.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f*=iCount;
                iCount += 1.0D;
            }
            return f;
        }

        // factorial of n
        // Argument is of type BigDecimal but must be, numerically, an integer
        public static BigDecimal factorial(BigDecimal n){
            if(n.compareTo(BigDecimal.ZERO)==-1 || !Fmath.isInteger(n))throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            BigDecimal one = BigDecimal.ONE;
            BigDecimal f = one;
            BigDecimal iCount = new BigDecimal(2.0D);
            while(iCount.compareTo(n)!=1){
                f = f.multiply(iCount);
                iCount = iCount.add(one);
            }
            one = null;
            iCount = null;
            return f;
        }



        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
            if(n<0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            for(int i=2; i<=n; i++)f+=Math.log(i);
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(long n){
            if(n<0L)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            long iCount = 2L;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1L;
            }
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1.0D;
            }
            return f;
        }


        // SIGN
        /*      returns -1 if x < 0 else returns 1   */
        //  double version
        public static double sign(double x){
            if (x<0.0){
                return -1.0;
            }
            else{
                return 1.0;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        //  float version
        public static float sign(float x){
            if (x<0.0F){
                return -1.0F;
            }
            else{
                return 1.0F;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        //  int version
        public static int sign(int x){
            if (x<0){
                return -1;
            }
            else{
                return 1;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        // long version
        public static long sign(long x){
            if (x<0){
                return -1;
            }
            else{
                return 1;
            }
        }

        // ADDITIONAL TRIGONOMETRIC FUNCTIONS

        // Returns the length of the hypotenuse of a and b
        // i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
        // double version
        public static double hypot(double aa, double bb){
            double amod=Math.abs(aa);
            double bmod=Math.abs(bb);
            double cc = 0.0D, ratio = 0.0D;
            if(amod==0.0){
                cc=bmod;
            }
            else{
                if(bmod==0.0){
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

        // Returns the length of the hypotenuse of a and b
        // i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
        // float version
        public static float hypot(float aa, float bb){
            return (float) hypot((double) aa, (double) bb);
        }

        // Angle (in radians) subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double angle(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){

            double ccos = Fmath.cos(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.acos(ccos);
        }

        // Angle (in radians) between sides sideA and sideB given all side lengths of a triangle
        public static double angle(double sideAC, double sideBC, double sideAB){

            double ccos = Fmath.cos(sideAC, sideBC, sideAB);
            return Math.acos(ccos);
        }

        // Sine of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double sin(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double angle = Fmath.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.sin(angle);
        }

        // Sine of angle between sides sideA and sideB given all side lengths of a triangle
        public static double sin(double sideAC, double sideBC, double sideAB){
            double angle = Fmath.angle(sideAC, sideBC, sideAB);
            return Math.sin(angle);
        }

        // Sine given angle in radians
        // for completion - returns Math.sin(arg)
        public static double sin(double arg){
            return Math.sin(arg);
        }

        // Inverse sine
        // Fmath.asin Checks limits - Java Math.asin returns NaN if without limits
        public static double asin(double a){
            if(a<-1.0D && a>1.0D) throw new IllegalArgumentException("Fmath.asin argument (" + a + ") must be >= -1.0 and <= 1.0");
            return Math.asin(a);
        }

        // Cosine of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double cos(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double sideAC = Fmath.hypot(xAtA - xAtC, yAtA - yAtC);
            double sideBC = Fmath.hypot(xAtB - xAtC, yAtB - yAtC);
            double sideAB = Fmath.hypot(xAtA - xAtB, yAtA - yAtB);
            return Fmath.cos(sideAC, sideBC, sideAB);
        }

        // Cosine of angle between sides sideA and sideB given all side lengths of a triangle
        public static double cos(double sideAC, double sideBC, double sideAB){
            return 0.5D*(sideAC/sideBC + sideBC/sideAC - (sideAB/sideAC)*(sideAB/sideBC));
        }

         // Cosine given angle in radians
         // for completion - returns Java Math.cos(arg)
        public static double cos(double arg){
            return Math.cos(arg);
        }

        // Inverse cosine
        // Fmath.asin Checks limits - Java Math.asin returns NaN if without limits
        public static double acos(double a){
            if(a<-1.0D || a>1.0D) throw new IllegalArgumentException("Fmath.acos argument (" + a + ") must be >= -1.0 and <= 1.0");
            return Math.acos(a);
        }

        // Tangent of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double tan(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double angle = Fmath.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.tan(angle);
        }

        // Tangent of angle between sides sideA and sideB given all side lengths of a triangle
        public static double tan(double sideAC, double sideBC, double sideAB){
            double angle = Fmath.angle(sideAC, sideBC, sideAB);
            return Math.tan(angle);
        }

        // Tangent given angle in radians
        // for completion - returns Math.tan(arg)
        public static double tan(double arg){
            return Math.tan(arg);
        }

        // Inverse tangent
        // for completion - returns Math.atan(arg)
        public static double atan(double a){
            return Math.atan(a);
        }

        // Inverse tangent - ratio numerator and denominator provided
        // for completion - returns Math.atan2(arg)
        public static double atan2(double a, double b){
            return Math.atan2(a, b);
        }

        // Cotangent
        public static double cot(double a){
            return 1.0D/Math.tan(a);
        }

        // Inverse cotangent
        public static double acot(double a){
            return Math.atan(1.0D/a);
        }

        // Inverse cotangent - ratio numerator and denominator provided
        public static double acot2(double a, double b){
            return Math.atan2(b, a);
        }

        // Secant
        public static double sec(double a){
            return 1.0/Math.cos(a);
        }

        // Inverse secant
        public static double asec(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("asec argument (" + a + ") must be >= 1 or <= -1");
            return Math.acos(1.0/a);
        }

        // Cosecant
        public static double csc(double a){
            return 1.0D/Math.sin(a);
        }

        // Inverse cosecant
        public static double acsc(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("acsc argument (" + a + ") must be >= 1 or <= -1");
            return Math.asin(1.0/a);
        }

        // Exsecant
        public static double exsec(double a){
            return (1.0/Math.cos(a)-1.0D);
        }

        // Inverse exsecant
        public static double aexsec(double a){
            if(a<0.0D && a>-2.0D) throw new IllegalArgumentException("aexsec argument (" + a + ") must be >= 0.0 and <= -2");
            return Math.asin(1.0D/(1.0D + a));
        }

        // Versine
        public static double vers(double a){
            return (1.0D - Math.cos(a));
        }

        // Inverse  versine
        public static double avers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("avers argument (" + a + ") must be <= 2 and >= 0");
            return Math.acos(1.0D - a);
        }

        // Coversine
        public static double covers(double a){
            return (1.0D - Math.sin(a));
        }

        // Inverse coversine
        public static double acovers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("acovers argument (" + a + ") must be <= 2 and >= 0");
            return Math.asin(1.0D - a);
        }

        // Haversine
        public static double hav(double a){
            return 0.5D*Fmath.vers(a);
        }

        // Inverse haversine
        public static double ahav(double a){
            if(a<0.0D && a>1.0D) throw new IllegalArgumentException("ahav argument (" + a + ") must be >= 0 and <= 1");
            return Fmath.acos(1.0D - 2.0D*a);
        }

        // Unnormalised sinc (unnormalised sine cardinal)   sin(x)/x
        public static double sinc(double a){
            if(Math.abs(a)<1e-40){
                return 1.0D;
            }
            else{
                return Math.sin(a)/a;
            }
        }

        // Normalised sinc (normalised sine cardinal)  sin(pi.x)/(pi.x)
        public static double nsinc(double a){
            if(Math.abs(a)<1e-40){
                return 1.0D;
            }
            else{
                return Math.sin(Math.PI*a)/(Math.PI*a);
            }
        }

        //Hyperbolic sine of a double number
        public static double sinh(double a){
            return 0.5D*(Math.exp(a)-Math.exp(-a));
        }

        // Inverse hyperbolic sine of a double number
        public static double asinh(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return sgn*Math.log(a+Math.sqrt(a*a+1.0D));
        }

        //Hyperbolic cosine of a double number
        public static double cosh(double a){
            return 0.5D*(Math.exp(a)+Math.exp(-a));
        }

        // Inverse hyperbolic cosine of a double number
        public static double acosh(double a){
            if(a<1.0D) throw new IllegalArgumentException("acosh real number argument (" + a + ") must be >= 1");
            return Math.log(a+Math.sqrt(a*a-1.0D));
        }

        //Hyperbolic tangent of a double number
        public static double tanh(double a){
            return sinh(a)/cosh(a);
        }

        // Inverse hyperbolic tangent of a double number
        public static double atanh(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            if(a>1.0D) throw new IllegalArgumentException("atanh real number argument (" + sgn*a + ") must be >= -1 and <= 1");
            return 0.5D*sgn*(Math.log(1.0D + a)-Math.log(1.0D - a));
        }

        //Hyperbolic cotangent of a double number
        public static double coth(double a){
            return 1.0D/tanh(a);
        }

        // Inverse hyperbolic cotangent of a double number
        public static double acoth(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            if(a<1.0D) throw new IllegalArgumentException("acoth real number argument (" + sgn*a + ") must be <= -1 or >= 1");
            return 0.5D*sgn*(Math.log(1.0D + a)-Math.log(a - 1.0D));
        }

        //Hyperbolic secant of a double number
        public static double sech(double a){
                return 1.0D/cosh(a);
        }

        // Inverse hyperbolic secant of a double number
        public static double asech(double a){
            if(a>1.0D || a<0.0D) throw new IllegalArgumentException("asech real number argument (" + a + ") must be >= 0 and <= 1");
            return 0.5D*(Math.log(1.0D/a + Math.sqrt(1.0D/(a*a) - 1.0D)));
        }

        //Hyperbolic cosecant of a double number
        public static double csch(double a){
                return 1.0D/sinh(a);
        }

        // Inverse hyperbolic cosecant of a double number
        public static double acsch(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return 0.5D*sgn*(Math.log(1.0/a + Math.sqrt(1.0D/(a*a) + 1.0D)));
        }

    // DETERMINING PRECISION i.e. number of mantissa places
    public static int checkPrecision(double number){
        boolean test = true;
        int prec = 0;
        if(Fmath.isNaN(number))test=false;
        if(Fmath.isPlusInfinity(number))test=false;
        if(Fmath.isMinusInfinity(number))test=false;
        while(test){
            if(number==Fmath.truncate(number, prec)){
                test = false;
            }
            else{
                prec++;
                if(prec>20)test=false;
            }
        }
        return prec;
    }

    public static int checkPrecision(float number){
        return checkPrecision((double)number);
    }


    // MANTISSA ROUNDING (TRUNCATING)
    // returns a value of xDouble truncated to trunc decimal places
    public static double truncate(double xDouble, int trunc){
        double xTruncated = xDouble;
        if(!Fmath.isNaN(xDouble)){
            if(!Fmath.isPlusInfinity(xDouble)){
                if(!Fmath.isMinusInfinity(xDouble)){
                    if(xDouble!=0.0D){
                        String xString = ((new Double(xDouble)).toString()).trim();
                        xTruncated = Double.parseDouble(truncateProcedure(xString, trunc));
                    }
                }
            }
        }
        return xTruncated;
    }

    // returns a value of xFloat truncated to trunc decimal places
    public static float truncate(float xFloat, int trunc){
        float xTruncated = xFloat;
        if(!Fmath.isNaN(xFloat)){
            if(!Fmath.isPlusInfinity(xFloat)){
                if(!Fmath.isMinusInfinity(xFloat)){
                    if(xFloat!=0.0D){
                        String xString = ((new Float(xFloat)).toString()).trim();
                        xTruncated = Float.parseFloat(truncateProcedure(xString, trunc));
                    }
                }
            }
        }
        return xTruncated;
    }

    // private method for truncating a float or double expressed as a String
    private static String truncateProcedure(String xValue, int trunc){

        String xTruncated = xValue;
        String xWorking = xValue;
        String exponent = " ";
        String first = "+";
        int expPos = xValue.indexOf('E');
        int dotPos = xValue.indexOf('.');
        int minPos = xValue.indexOf('-');

        if(minPos!=-1){
            if(minPos==0){
                xWorking = xWorking.substring(1);
                first = "-";
                dotPos--;
                expPos--;
            }
        }
        if(expPos>-1){
            exponent = xWorking.substring(expPos);
            xWorking = xWorking.substring(0,expPos);
        }
        String xPreDot = null;
        String xPostDot = "0";
        String xDiscarded = null;
        String tempString = null;
        double tempDouble = 0.0D;
        if(dotPos>-1){
            xPreDot = xWorking.substring(0,dotPos);
            xPostDot = xWorking.substring(dotPos+1);
            int xLength = xPostDot.length();
            if(trunc<xLength){
                xDiscarded = xPostDot.substring(trunc);
                tempString = xDiscarded.substring(0,1) + ".";
                if(xDiscarded.length()>1){
                    tempString += xDiscarded.substring(1);
                }
                else{
                    tempString += "0";
                }
                tempDouble = Math.round(Double.parseDouble(tempString));

                if(trunc>0){
                    if(tempDouble>=5.0){
                        int[] xArray = new int[trunc+1];
                        xArray[0] = 0;
                        for(int i=0; i<trunc; i++){
                            xArray[i+1] = Integer.parseInt(xPostDot.substring(i,i+1));
                        }
                        boolean test = true;
                        int iCounter = trunc;
                        while(test){
                            xArray[iCounter] += 1;
                            if(iCounter>0){
                                if(xArray[iCounter]<10){
                                    test = false;
                                }
                                else{
                                    xArray[iCounter]=0;
                                    iCounter--;
                                }
                            }
                            else{
                                test = false;
                            }
                        }
                        int preInt = Integer.parseInt(xPreDot);
                        preInt += xArray[0];
                        xPreDot = (new Integer(preInt)).toString();
                        tempString = "";
                        for(int i=1; i<=trunc; i++){
                            tempString += (new Integer(xArray[i])).toString();
                        }
                        xPostDot = tempString;
                    }
                    else{
                        xPostDot = xPostDot.substring(0, trunc);
                    }
                }
                else{
                    if(tempDouble>=5.0){
                        int preInt = Integer.parseInt(xPreDot);
                        preInt++;
                        xPreDot = (new Integer(preInt)).toString();
                    }
                    xPostDot = "0";
                }
            }
            xTruncated = first + xPreDot.trim() + "." + xPostDot.trim() + exponent;
        }
        return xTruncated.trim();
    }

        // Returns true if x is infinite, i.e. is equal to either plus or minus infinity
        // x is double
        public static boolean isInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY || x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is infinite, i.e. is equal to either plus or minus infinity
        // x is float
        public static boolean isInfinity(float x){
            boolean test=false;
            if(x==Float.POSITIVE_INFINITY || x==Float.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is plus infinity
        // x is double
        public static boolean isPlusInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is plus infinity
        // x is float
        public static boolean isPlusInfinity(float x){
            boolean test=false;
            if(x==Float.POSITIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is minus infinity
        // x is double
        public static boolean isMinusInfinity(double x){
            boolean test=false;
            if(x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is minus infinity
        // x is float
        public static boolean isMinusInfinity(float x){
            boolean test=false;
            if(x==Float.NEGATIVE_INFINITY)test=true;
            return test;
        }


        // Returns true if x is 'Not a Number' (NaN)
        // x is double
        public static boolean isNaN(double x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }

        // Returns true if x is 'Not a Number' (NaN)
        // x is float
        public static boolean isNaN(float x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are double
        // x may be float within range, PLUS_INFINITY, NEGATIVE_INFINITY, or NaN
        // NB!! This method treats two NaNs as equal
        public static boolean isEqual(double x, double y){
            boolean test=false;
            if(Fmath.isNaN(x)){
                if(Fmath.isNaN(y))test=true;
            }
            else{
                if(Fmath.isPlusInfinity(x)){
                    if(Fmath.isPlusInfinity(y))test=true;
                }
                else{
                    if(Fmath.isMinusInfinity(x)){
                        if(Fmath.isMinusInfinity(y))test=true;
                    }
                    else{
                        if(x==y)test=true;
                    }
                }
            }
            return test;
        }

        // Returns true if x equals y
        // x and y are float
        // x may be float within range, PLUS_INFINITY, NEGATIVE_INFINITY, or NaN
        // NB!! This method treats two NaNs as equal
        public static boolean isEqual(float x, float y){
            boolean test=false;
            if(Fmath.isNaN(x)){
                if(Fmath.isNaN(y))test=true;
            }
            else{
                if(Fmath.isPlusInfinity(x)){
                    if(Fmath.isPlusInfinity(y))test=true;
                }
                else{
                    if(Fmath.isMinusInfinity(x)){
                        if(Fmath.isMinusInfinity(y))test=true;
                    }
                    else{
                        if(x==y)test=true;
                    }
                }
            }
            return test;
        }

        // Returns true if x equals y
        // x and y are int
        public static boolean isEqual(int x, int y){
            boolean test=false;
            if(x==y)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are char
        public static boolean isEqual(char x, char y){
            boolean test=false;
            if(x==y)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are Strings
        public static boolean isEqual(String x, String y){
            boolean test=false;
            if(x.equals(y))test=true;
            return test;
        }

        // IS EQUAL WITHIN LIMITS
        // Returns true if x equals y within limits plus or minus limit
        // x and y are double
        public static boolean isEqualWithinLimits(double x, double y, double limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are float
        public static boolean isEqualWithinLimits(float x, float y, float limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are long
        public static boolean isEqualWithinLimits(long x, long y, long limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are int
        public static boolean isEqualWithinLimits(int x, int y, int limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are BigDecimal
        public static boolean isEqualWithinLimits(BigDecimal x, BigDecimal y, BigDecimal limit){
            boolean test=false;
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are BigInteger
        public static boolean isEqualWithinLimits(BigInteger x, BigInteger y, BigInteger limit){
            boolean test=false;
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            return test;
        }


        // IS EQUAL WITHIN A PERCENTAGE
        // Returns true if x equals y within a percentage of the mean
        // x and y are double
        public static boolean isEqualWithinPerCent(double x, double y, double perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are float
        public static boolean isEqualWithinPerCent(float x, float y, float perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0F);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are long, percentage provided as double
        public static boolean isEqualWithinPerCent(long x, long y, double perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are long, percentage provided as int
        public static boolean isEqualWithinPerCent(long x, long y, long perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*(double)perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are int, percentage provided as double
        public static boolean isEqualWithinPerCent(int x, int y, double perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are int, percentage provided as int
        public static boolean isEqualWithinPerCent(int x, int y, int perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*(double)perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDecimal
        public static boolean isEqualWithinPerCent(BigDecimal x, BigDecimal y, BigDecimal perCent){
            boolean test=false;
            BigDecimal limit = (x.add(y)).multiply(perCent).multiply(new BigDecimal("0.005"));
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDInteger, percentage provided as BigDecimal
        public static boolean isEqualWithinPerCent(BigInteger x, BigInteger y, BigDecimal perCent){
            boolean test=false;
            BigDecimal xx = new BigDecimal(x);
            BigDecimal yy = new BigDecimal(y);
            BigDecimal limit = (xx.add(yy)).multiply(perCent).multiply(new BigDecimal("0.005"));
            if(((xx.subtract(yy)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            xx = null;
            yy = null;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDInteger, percentage provided as BigInteger
        public static boolean isEqualWithinPerCent(BigInteger x, BigInteger y, BigInteger perCent){
            boolean test=false;
            BigDecimal xx = new BigDecimal(x);
            BigDecimal yy = new BigDecimal(y);
            BigDecimal pc = new BigDecimal(perCent);
            BigDecimal limit = (xx.add(yy)).multiply(pc).multiply(new BigDecimal("0.005"));
            if(((xx.subtract(yy)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            xx = null;
            yy = null;
            pc = null;
            return test;
        }

        // COMPARISONS
        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are double
        public static int compare(double x, double y){
            Double X = new Double(x);
            Double Y = new Double(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are int
        public static int compare(int x, int y){
            Integer X = new Integer(x);
            Integer Y = new Integer(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are long
        public static int compare(long x, long y){
            Long X = new Long(x);
            Long Y = new Long(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are float
        public static int compare(float x, float y){
            Float X = new Float(x);
            Float Y = new Float(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are short
        public static int compare(byte x, byte y){
            Byte X = new Byte(x);
            Byte Y = new Byte(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are short
        public static int compare(short x, short y){
            Short X = new Short(x);
            Short Y = new Short(y);
            return X.compareTo(Y);
        }

        // COMPARE ARRAYS
        // Returns true if arrays identical, false if not
        // arrays - double[]
        public static boolean compare(double[] array1, double[] array2){
            boolean ret = true;
            int n = array1.length;
            int m = array2.length;
            if(n!=m){
                ret = false;
            }
            else{
                for(int i=0; i<n; i++){
                    if(array1[i]!=array2[i]){
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        // Returns true if arrays identical, false if not
        // arrays - float[]
        public static boolean compare(float[] array1, float[] array2){
            boolean ret = true;
            int n = array1.length;
            int m = array2.length;
            if(n!=m){
                ret = false;
            }
            else{
                for(int i=0; i<n; i++){
                    if(array1[i]!=array2[i]){
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        // Returns true if arrays identical, false if not
        // arrays - int[]
        public static boolean compare(int[] array1, int[] array2){
            boolean ret = true;
            int n = array1.length;
            int m = array2.length;
            if(n!=m){
                ret = false;
            }
            else{
                for(int i=0; i<n; i++){
                    if(array1[i]!=array2[i]){
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        // Returns true if arrays identical, false if not
        // arrays - long[]
        public static boolean compare(long[] array1, long[] array2){
            boolean ret = true;
            int n = array1.length;
            int m = array2.length;
            if(n!=m){
                ret = false;
            }
            else{
                for(int i=0; i<n; i++){
                    if(array1[i]!=array2[i]){
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        // IS AN INTEGER
        // Returns true if x is, arithmetically, an integer
        // Returns false if x is not, arithmetically, an integer
        public static boolean isInteger(double x){
            boolean retn = false;
            double xfloor = Math.floor(x);
            if((x - xfloor)==0.0D) retn = true;
            return retn;
        }

        // Returns true if all elements in the array x are, arithmetically, integers
        // Returns false if any element in the array x is not, arithmetically, an integer
        public static boolean isInteger(double[] x){
            boolean retn = true;
            boolean test = true;
            int ii = 0;
            while(test){
                double xfloor = Math.floor(x[ii]);
                if((x[ii] - xfloor)!=0.0D){
                    retn = false;
                    test = false;
                }
                else{
                    ii++;
                    if(ii==x.length)test=false;
                }
            }
            return retn;
        }

        // Returns true if x is, arithmetically, an integer
        // Returns false if x is not, arithmetically, an integer
        public static boolean isInteger(float x){
            boolean ret = false;
            float xfloor = (float)Math.floor(x);
            if((x - xfloor)==0.0F) ret = true;
            return ret;
        }


        // Returns true if all elements in the array x are, arithmetically, integers
        // Returns false if any element in the array x is not, arithmetically, an integer
        public static boolean isInteger(float[] x){
            boolean retn = true;
            boolean test = true;
            int ii = 0;
            while(test){
                float xfloor = (float)Math.floor(x[ii]);
                if((x[ii] - xfloor)!=0.0D){
                    retn = false;
                    test = false;
                }
                else{
                    ii++;
                    if(ii==x.length)test=false;
                }
            }
            return retn;
        }

        public static boolean isInteger (Number numberAsObject){
            boolean test = integers.containsKey(numberAsObject.getClass());
            if(!test){
                if(numberAsObject instanceof Double){
                    double dd = numberAsObject.doubleValue();
                    test = Fmath.isInteger(dd);
                }
                if(numberAsObject instanceof Float){
                    float dd = numberAsObject.floatValue();
                    test = Fmath.isInteger(dd);
                }
                if(numberAsObject instanceof BigDecimal){
                    double dd = numberAsObject.doubleValue();
                    test = Fmath.isInteger(dd);
                }
            }
            return test;
        }

        public static boolean isInteger (Number[] numberAsObject){
            boolean testall = true;
            for(int i=0; i<numberAsObject.length; i++){
                boolean test = integers.containsKey(numberAsObject[i].getClass());
                if(!test){
                    if(numberAsObject[i] instanceof Double){
                        double dd = numberAsObject[i].doubleValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                    if(numberAsObject[i] instanceof Float){
                        float dd = numberAsObject[i].floatValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                    if(numberAsObject[i] instanceof BigDecimal){
                        double dd = numberAsObject[i].doubleValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                }
            }
            return testall;
        }

        // IS EVEN
        // Returns true if x is an even number, false if x is an odd number
        // x is int
        public static boolean isEven(int x){
            boolean test=false;
            if(x%2 == 0.0D)test=true;
            return test;
        }

        // Returns true if x is an even number, false if x is an odd number
        // x is float but must hold an integer value
        public static boolean isEven(float x){
            double y=Math.floor(x);
            if(((double)x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=false;
            y=Math.floor(x/2.0F);
            if(((double)(x/2.0F)-y) == 0.0D)test=true;
            return test;
        }

        // Returns true if x is an even number, false if x is an odd number
        // x is double but must hold an integer value
        public static boolean isEven(double x){
            double y=Math.floor(x);
            if((x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=false;
            y=Math.floor(x/2.0F);
            if((x/2.0D-y) == 0.0D)test=true;
            return test;
        }

        // IS ODD
        // Returns true if x is an odd number, false if x is an even number
        // x is int
        public static boolean isOdd(int x){
            boolean test=true;
            if(x%2 == 0.0D)test=false;
            return test;
        }

        // Returns true if x is an odd number, false if x is an even number
        // x is float but must hold an integer value
        public static boolean isOdd(float x){
            double y=Math.floor(x);
            if(((double)x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=true;
            y=Math.floor(x/2.0F);
            if(((double)(x/2.0F)-y) == 0.0D)test=false;
            return test;
        }

        // Returns true if x is an odd number, false if x is an even number
        // x is double but must hold an integer value
        public static boolean isOdd(double x){
            double y=Math.floor(x);
            if((x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=true;
            y=Math.floor(x/2.0F);
            if((x/2.0D-y) == 0.0D)test=false;
            return test;
        }

        // LEAP YEAR
        // Returns true if year (argument) is a leap year
        public static boolean leapYear(int year){
            boolean test = false;

            if(year%4 != 0){
                 test = false;
            }
            else{
                if(year%400 == 0){
                    test=true;
                }
                else{
                    if(year%100 == 0){
                        test=false;
                    }
                    else{
                        test=true;
                    }
                }
            }
            return test;
        }

        // COMPUTER TIME
        // Returns milliseconds since 0 hours 0 minutes 0 seconds on 1 Jan 1970
        public static long dateToJavaMilliS(int year, int month, int day, int hour, int min, int sec){

            long[] monthDays = {0L, 31L, 28L, 31L, 30L, 31L, 30L, 31L, 31L, 30L, 31L, 30L, 31L};
            long ms = 0L;

            long yearDiff = 0L;
            int yearTest = year-1;
            while(yearTest>=1970){
                yearDiff += 365;
                if(Fmath.leapYear(yearTest))yearDiff++;
                yearTest--;
            }
            yearDiff *= 24L*60L*60L*1000L;

            long monthDiff = 0L;
            int monthTest = month -1;
            while(monthTest>0){
                monthDiff += monthDays[monthTest];
                if(Fmath.leapYear(year))monthDiff++;
                monthTest--;
            }

            monthDiff *= 24L*60L*60L*1000L;

            ms = yearDiff + monthDiff + day*24L*60L*60L*1000L + hour*60L*60L*1000L + min*60L*1000L + sec*1000L;

            return ms;
        }

        // DEPRECATED METHODS
        // Several methods have been revised and moved to classes ArrayMaths, Conv or PrintToScreen

        // ARRAY MAXIMUM  (deprecated - see ArryMaths class)
        // Maximum of a 1D array of doubles, aa
        public static double maximum(double[] aa){
            int n = aa.length;
            double aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of floats, aa
        public static float maximum(float[] aa){
            int n = aa.length;
            float aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of ints, aa
        public static int maximum(int[] aa){
            int n = aa.length;
            int aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of longs, aa
        public static long maximum(long[] aa){
            long n = aa.length;
            long aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Minimum of a 1D array of doubles, aa
        public static double minimum(double[] aa){
            int n = aa.length;
            double aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of floats, aa
        public static float minimum(float[] aa){
            int n = aa.length;
            float aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // ARRAY MINIMUM (deprecated - see ArryMaths class)
        // Minimum of a 1D array of ints, aa
        public static int minimum(int[] aa){
            int n = aa.length;
            int aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of longs, aa
        public static long minimum(long[] aa){
            long n = aa.length;
            long aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // MAXIMUM DISTANCE BETWEEN ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Maximum distance between elements of a 1D array of doubles, aa
        public static double maximumDifference(double[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of floats, aa
        public static float maximumDifference(float[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of long, aa
        public static long maximumDifference(long[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of ints, aa
        public static int maximumDifference(int[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }


        // MINIMUM DISTANCE BETWEEN ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Minimum distance between elements of a 1D array of doubles, aa
        public static double minimumDifference(double[] aa){
            double[] sorted = Fmath.selectionSort(aa);
            double n = aa.length;
            double diff = sorted[1] - sorted[0];
            double minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of floats, aa
        public static float minimumDifference(float[] aa){
            float[] sorted = Fmath.selectionSort(aa);
            float n = aa.length;
            float diff = sorted[1] - sorted[0];
            float minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of longs, aa
        public static long minimumDifference(long[] aa){
            long[] sorted = Fmath.selectionSort(aa);
            long n = aa.length;
            long diff = sorted[1] - sorted[0];
            long minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of ints, aa
        public static int minimumDifference(int[] aa){
            int[] sorted = Fmath.selectionSort(aa);
            int n = aa.length;
            int diff = sorted[1] - sorted[0];
            int minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // REVERSE ORDER OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Reverse the order of the elements of a 1D array of doubles, aa
        public static double[] reverseArray(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of floats, aa
        public static float[] reverseArray(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of ints, aa
        public static int[] reverseArray(int[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of longs, aa
        public static long[] reverseArray(long[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of char, aa
        public static char[] reverseArray(char[] aa){
            int n = aa.length;
            char[] bb = new char[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // ABSOLUTE VALUE OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // return absolute values of an array of doubles
        public static double[] arrayAbs(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of floats
        public static float[] arrayAbs(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of long
        public static long[] arrayAbs(long[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of int
        public static int[] arrayAbs(int[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // MULTIPLY ARRAY ELEMENTS BY A CONSTANT  (deprecated - see ArryMaths class)
        // multiply all elements by a constant double[] by double -> double[]
        public static double[] arrayMultByConstant(double[] aa, double constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[i]*constant;
            }
            return bb;
        }

        // multiply all elements by a constant int[] by double -> double[]
        public static double[] arrayMultByConstant(int[] aa, double constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i]*constant;
            }
            return bb;
        }
        // multiply all elements by a constant double[] by int -> double[]
        public static double[] arrayMultByConstant(double[] aa, int constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[i]*(double)constant;
            }
            return bb;
        }

        // multiply all elements by a constant int[] by int -> double[]
        public static double[] arrayMultByConstant(int[] aa, int constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)(aa[i]*constant);
            }
            return bb;
        }

        // LOG10 OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Log to base 10 of all elements of an array of doubles
        public static double[] log10Elements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.log10(aa[i]);
            return bb;
        }

         // Log to base 10 of all elements of an array of floats
        public static float[] log10Elements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.log10(aa[i]);
            return bb;
        }

        // NATURAL LOG OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Log to base e of all elements of an array of doubles
        public static double[] lnElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.log10(aa[i]);
            return bb;
        }

         // Log to base e of all elements of an array of floats
        public static float[] lnElements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.log10(aa[i]);
            return bb;
        }

        // SQUARE ROOT OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Square root all elements of an array of doubles
        public static double[] squareRootElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.sqrt(aa[i]);
            return bb;
        }

         // Square root all elements of an array of floats
        public static float[] squareRootElements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.sqrt(aa[i]);
            return bb;
        }

        // POWER OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Raise all elements of an array of doubles to a double power
        public static double[] raiseElementsToPower(double[] aa, double power){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of doubles to an int power
        public static double[] raiseElementsToPower(double[] aa, int power){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of floats to a float power
        public static float[] raiseElementsToPower(float[] aa, float power){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of floats to an int power
        public static float[] raiseElementsToPower(float[] aa, int power){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.pow(aa[i], power);
            return bb;
        }

        // INVERT ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // invert all elements of an array of doubles
        public static double[] invertElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = 1.0D/aa[i];
            return bb;
        }

        // invert all elements of an array of floats
        public static float[] invertElements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = 1.0F/aa[i];
            return bb;
        }


        // FIND INDICES OF ARRAY ELEMENTS EQUAL TO A VALUE  (deprecated - see ArryMaths class)
        // finds the indices of the elements equal to a given value in an array of doubles
        // returns null if none found
        public static int[] indicesOf(double[] array, double value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of floats
        // returns null if none found
        public static int[] indicesOf(float[] array, float value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of longs
        // returns null if none found
        public static int[] indicesOf(long[] array, long value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of ints
        // returns null if none found
        public static int[] indicesOf(int[] array, int value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of shorts
        // returns null if none found
        public static int[] indicesOf(short[] array, short value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of bytes
        // returns null if none found
        public static int[] indicesOf(byte[] array, byte value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of chars
        // returns null if none found
        public static int[] indicesOf(char[] array, char value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of Strings
        // returns null if none found
        public static int[] indicesOf(String[] array, String value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i].equals(value)){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // finds the indices of the elements equal to a given value in an array of Objectss
        // returns null if none found
        public static int[] indicesOf(Object[] array, Object value){
            int[] indices = null;
            int numberOfIndices = 0;
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<array.length; i++){
                if(array[i].equals(value)){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
            return indices;
        }

        // FIND FIRST INDEX OF ARRAY ELEMENT EQUAL TO A VALUE  (deprecated - see ArryMaths class)
        // finds the index of the first occurence of the element equal to a given value in an array of doubles
        // returns -1 if none found
        public static int indexOf(double[] array, double value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of floats
        // returns -1 if none found
        public static int indexOf(float[] array, float value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of longs
        // returns -1 if none found
        public static int indexOf(long[] array, long value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of ints
        // returns -1 if none found
        public static int indexOf(int[] array, int value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of bytes
        // returns -1 if none found
        public static int indexOf(byte[] array, byte value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of shorts
        // returns -1 if none found
        public static int indexOf(short[] array, short value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of chars
        // returns -1 if none found
        public static int indexOf(char[] array, char value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of Strings
        // returns -1 if none found
        public static int indexOf(String[] array, String value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter].equals(value)){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // finds the index of the first occurence of the element equal to a given value in an array of Objects
        // returns -1 if none found
        public static int indexOf(Object[] array, Object value){
            int index = -1;
            boolean test = true;
            int counter = 0;
            while(test){
                if(array[counter].equals(value)){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=array.length)test = false;
                }
            }
            return index;
        }

        // FIND  VALUE OF AND FIND VALUE OF ARRAY ELEMENTS NEAREST TO A VALUE  (deprecated - see ArryMaths class)
        // finds the value of nearest element value in array to the argument value
        public static double nearestElementValue(double[] array, double value){
            double diff = Math.abs(array[0] - value);
            double nearest = array[0];
            for(int i=1; i<array.length; i++){
                if(Math.abs(array[i] - value)<diff){
                    diff = Math.abs(array[i] - value);
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest element value in array to the argument value
        public static int nearestElementIndex(double[] array, double value){
            double diff = Math.abs(array[0] - value);
            int nearest = 0;
            for(int i=1; i<array.length; i++){
                if(Math.abs(array[i] - value)<diff){
                    diff = Math.abs(array[i] - value);
                    nearest = i;
                }
            }
            return nearest;
        }

        // finds the value of nearest lower element value in array to the argument value
        public static double nearestLowerElementValue(double[] array, double value){
            double diff0 = 0.0D;
            double diff1 = 0.0D;
            double nearest = 0.0D;
            int ii = 0;
            boolean test = true;
            double min = array[0];
            while(test){
                if(array[ii]<min)min = array[ii];
                if((value - array[ii])>=0.0D){
                    diff0 = value - array[ii];
                    nearest = array[ii];
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = min;
                        diff0 = min - value;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = value - array[i];
                if(diff1>=0.0D && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest lower element value in array to the argument value
        public static int nearestLowerElementIndex(double[] array, double value){
            double diff0 = 0.0D;
            double diff1 = 0.0D;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            double min = array[0];
            int minI = 0;
            while(test){
                if(array[ii]<min){
                    min = array[ii];
                    minI = ii;
                }
                if((value - array[ii])>=0.0D){
                    diff0 = value - array[ii];
                    nearest = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = minI;
                        diff0 = min - value;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = value - array[i];
                if(diff1>=0.0D && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = i;
                }
            }
            return nearest;
        }

        // finds the value of nearest higher element value in array to the argument value
        public static double nearestHigherElementValue(double[] array, double value){
            double diff0 = 0.0D;
            double diff1 = 0.0D;
            double nearest = 0.0D;
            int ii = 0;
            boolean test = true;
            double max = array[0];
            while(test){
                if(array[ii]>max)max = array[ii];
                if((array[ii] - value )>=0.0D){
                    diff0 = value - array[ii];
                    nearest = array[ii];
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = max;
                        diff0 = value - max;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = array[i]- value;
                if(diff1>=0.0D && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest higher element value in array to the argument value
        public static int nearestHigherElementIndex(double[] array, double value){
            double diff0 = 0.0D;
            double diff1 = 0.0D;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            double max = array[0];
            int maxI = 0;
            while(test){
                if(array[ii]>max){
                    max = array[ii];
                    maxI = ii;
                }
                if((array[ii] - value )>=0.0D){
                    diff0 = value - array[ii];
                    nearest = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = maxI;
                        diff0 = value - max;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = array[i]- value;
                if(diff1>=0.0D && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = i;
                }
            }
            return nearest;
        }


        // finds the value of nearest element value in array to the argument value
        public static int nearestElementValue(int[] array, int value){
            int diff = Math.abs(array[0] - value);
            int nearest = array[0];
            for(int i=1; i<array.length; i++){
               if(Math.abs(array[i] - value)<diff){
                    diff = Math.abs(array[i] - value);
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest element value in array to the argument value
        public static int nearestElementIndex(int[] array, int value){
            int diff = Math.abs(array[0] - value);
            int nearest = 0;
            for(int i=1; i<array.length; i++){
                if(Math.abs(array[i] - value)<diff){
                    diff = Math.abs(array[i] - value);
                    nearest = i;
                }
            }
            return nearest;
        }

        // finds the value of nearest lower element value in array to the argument value
        public static int nearestLowerElementValue(int[] array, int value){
            int diff0 = 0;
            int diff1 = 0;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            int min = array[0];
            while(test){
                if(array[ii]<min)min = array[ii];
                if((value - array[ii])>=0){
                    diff0 = value - array[ii];
                    nearest = array[ii];
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = min;
                        diff0 = min - value;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = value - array[i];
                if(diff1>=0 && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest lower element value in array to the argument value
        public static int nearestLowerElementIndex(int[] array, int value){
            int diff0 = 0;
            int diff1 = 0;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            int min = array[0];
            int minI = 0;
            while(test){
                if(array[ii]<min){
                    min = array[ii];
                    minI = ii;
                }
                if((value - array[ii])>=0){
                    diff0 = value - array[ii];
                    nearest = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = minI;
                        diff0 = min - value;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = value - array[i];
                if(diff1>=0 && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = i;
                }
            }
            return nearest;
        }

        // finds the value of nearest higher element value in array to the argument value
        public static int nearestHigherElementValue(int[] array, int value){
            int diff0 = 0;
            int diff1 = 0;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            int max = array[0];
            while(test){
                if(array[ii]>max)max = array[ii];
                if((array[ii] - value )>=0){
                    diff0 = value - array[ii];
                    nearest = array[ii];
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = max;
                        diff0 = value - max;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = array[i]- value;
                if(diff1>=0 && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = array[i];
                }
            }
            return nearest;
        }

        // finds the index of nearest higher element value in array to the argument value
        public static int nearestHigherElementIndex(int[] array, int value){
            int diff0 = 0;
            int diff1 = 0;
            int nearest = 0;
            int ii = 0;
            boolean test = true;
            int max = array[0];
            int maxI = 0;
            while(test){
                if(array[ii]>max){
                    max = array[ii];
                    maxI = ii;
                }
                if((array[ii] - value )>=0){
                    diff0 = value - array[ii];
                    nearest = ii;
                    test = false;
                }
                else{
                    ii++;
                    if(ii>array.length-1){
                        nearest = maxI;
                        diff0 = value - max;
                        test = false;
                    }
                }
            }
            for(int i=0; i<array.length; i++){
                diff1 = array[i]- value;
                if(diff1>=0 && diff1<diff0 ){
                    diff0 = diff1;
                    nearest = i;
                }
            }
            return nearest;
        }

        // SUM OF ALL ELEMENTS  (deprecated - see ArryMaths class)
        // Sum of all array elements - double array
        public static double arraySum(double[]array){
            double sum = 0.0D;
            for(double i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - float array
        public static float arraySum(float[]array){
            float sum = 0.0F;
            for(float i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - int array
        public static int arraySum(int[]array){
            int sum = 0;
            for(int i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - long array
        public static long arraySum(long[]array){
            long sum = 0L;
            for(long i:array)sum += i;
            return sum;
        }

        // Sum of all positive array elements - long array
        public static long arrayPositiveElementsSum(long[]array){
            long sum = 0L;
            for(long i:array)if(i>0)sum += i;
            return sum;
        }


        // PRODUCT OF ALL ELEMENTS  (deprecated - see ArryMaths class)
        // Product of all array elements - double array
        public static double arrayProduct(double[]array){
            double product = 1.0D;
            for(double i:array)product *= i;
            return product;
        }

        // Product of all array elements - float array
        public static float arrayProduct(float[]array){
            float product = 1.0F;
            for(float i:array)product *= i;
            return product;
        }

        // Product of all array elements - int array
        public static int arrayProduct(int[]array){
            int product = 1;
            for(int i:array)product *= i;
            return product;
        }

        // Product of all array elements - long array
        public static long arrayProduct(long[]array){
            long product = 1L;
            for(long i:array)product *= i;
            return product;
        }

        // CONCATENATE TWO ARRAYS  (deprecated - see ArryMaths class)
        // Concatenate two double arrays
        public static double[] concatenate(double[] aa, double[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            double[] cc = new double[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }
            return cc;
        }

        // Concatenate two float arrays
        public static float[] concatenate(float[] aa, float[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            float[] cc = new float[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two int arrays
        public static int[] concatenate(int[] aa, int[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            int[] cc = new int[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two long arrays
        public static long[] concatenate(long[] aa, long[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            long[] cc = new long[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two short arrays
        public static short[] concatenate(short[] aa, short[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            short[] cc = new short[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }
            return cc;
        }

        // Concatenate two byte arrays
        public static byte[] concatenate(byte[] aa, byte[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            byte[] cc = new byte[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two char arrays
        public static char[] concatenate(char[] aa, char[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            char[] cc = new char[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
           }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two String arrays
        public static String[] concatenate(String[] aa, String[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            String[] cc = new String[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
            }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // Concatenate two Object arrays
        public static Object[] concatenate(Object[] aa, Object[] bb){
            int aLen = aa.length;
            int bLen = bb.length;
            int cLen = aLen + bLen;
            Object[] cc = new Object[cLen];
            for(int i=0; i<aLen; i++){
                cc[i] = aa[i];
             }
            for(int i=0; i<bLen; i++){
                cc[i+aLen] = bb[i];
            }

            return cc;
        }

        // RECAST ARRAY TYPE  (deprecated - see Conv class)
        // recast an array of float as doubles
        public static double[] floatTOdouble(float[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i];
            }
            return bb;
        }

        // recast an array of int as double
        public static double[] intTOdouble(int[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i];
            }
            return bb;
        }

        // recast an array of int as float
        public static float[] intTOfloat(int[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = (float)aa[i];
            }
            return bb;
        }

        // recast an array of int as long
        public static long[] intTOlong(int[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = (long)aa[i];
            }
            return bb;
        }

        // recast an array of long as double
        // BEWARE POSSIBLE LOSS OF PRECISION
        public static double[] longTOdouble(long[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i];
            }
            return bb;
        }

        // recast an array of long as float
        // BEWARE POSSIBLE LOSS OF PRECISION
        public static float[] longTOfloat(long[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = (float)aa[i];
            }
            return bb;
        }

        // recast an array of short as double
        public static double[] shortTOdouble(short[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i];
            }
            return bb;
        }

         // recast an array of short as float
        public static float[] shortTOfloat(short[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = (float)aa[i];
            }
            return bb;
        }

        // recast an array of short as long
        public static long[] shortTOlong(short[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = (long)aa[i];
            }
            return bb;
        }

        // recast an array of short as int
        public static int[] shortTOint(short[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = (int)aa[i];
            }
            return bb;
        }

        // recast an array of byte as double
        public static double[] byteTOdouble(byte[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (int)aa[i];
            }
            return bb;
        }

        // recast an array of byte as float
        public static float[] byteTOfloat(byte[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = (float)aa[i];
            }
            return bb;
        }

        // recast an array of byte as long
        public static long[] byteTOlong(byte[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = (long)aa[i];
            }
            return bb;
        }

        // recast an array of byte as int
        public static int[] byteTOint(byte[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = (int)aa[i];
            }
            return bb;
        }

        // recast an array of byte as short
        public static short[] byteTOshort(byte[] aa){
            int n = aa.length;
            short[] bb = new short[n];
            for(int i=0; i<n; i++){
               bb[i] = (short)aa[i];
            }
            return bb;
        }

        // recast an array of double as int
        // BEWARE OF LOSS OF PRECISION
        public static int[] doubleTOint(double[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = (int)aa[i];
            }
            return bb;
        }

        // PRINT ARRAY TO SCREEN (deprecated - see PrintToScreen class)
        // print an array of doubles to screen
        // No line returns except at the end
        public static void print(double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of doubles to screen
        // with line returns
        public static void println(double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of floats to screen
        // No line returns except at the end
        public static void print(float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of floats to screen
        // with line returns
        public static void println(float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of ints to screen
        // No line returns except at the end
        public static void print(int[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of ints to screen
        // with line returns
        public static void println(int[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of longs to screen
        // No line returns except at the end
        public static void print(long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of longs to screen
        // with line returns
        public static void println(long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of char to screen
        // No line returns except at the end
        public static void print(char[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of char to screen
        // with line returns
        public static void println(char[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of String to screen
        // No line returns except at the end
        public static void print(String[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Strings to screen
        // with line returns
        public static void println(String[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of shorts to screen
        // No line returns except at the end
        public static void print(short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of shorts to screen
        // with line returns
        public static void println(short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of bytes to screen
        // No line returns except at the end
        public static void print(byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of bytes to screen
        // with line returns
        public static void println(byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }


        // print a 2D array of doubles to screen
        public static void print(double[][] aa){
            for(int i=0; i<aa.length; i++){
                Fmath.print(aa[i]);
            }
        }

        // SORT ELEMENTS OF ARRAY  (deprecated - see ArryMaths class)
        // sort elements in an array of doubles into ascending order
        // using selection sort method
        // returns Vector containing the original array, the sorted array
        //  and an array of the indices of the sorted array
        public static Vector<Object> selectSortVector(double[] aa){
            ArrayList<Object> list = Fmath.selectSortArrayList(aa);
            Vector<Object> ret = null;
            if(list!=null){
                int n = list.size();
                ret = new Vector<Object>(n);
                for(int i=0; i<n; i++)ret.addElement(list.get(i));
            }
            return ret;
        }


        // sort elements in an array of doubles into ascending order
        // using selection sort method
        // returns ArrayList containing the original array, the sorted array
        //  and an array of the indices of the sorted array
        public static ArrayList<Object> selectSortArrayList(double[] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            double holdb = 0.0D;
            int holdi = 0;
            double[] bb = new double[n];
            int[] indices = new int[n];
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }
            ArrayList<Object> arrayl = new ArrayList<Object>();
            arrayl.add(aa);
            arrayl.add(bb);
            arrayl.add(indices);
            return arrayl;
        }

        // sort elements in an array of doubles into ascending order
        // using selection sort method
        public static double[] selectionSort(double[] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            double hold = 0.0D;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                hold=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=hold;
            }
            return bb;
        }

        // sort elements in an array of floats into ascending order
        // using selection sort method
        public static float[] selectionSort(float[] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            float hold = 0.0F;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                hold=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=hold;
            }
            return bb;
        }

        // sort elements in an array of ints into ascending order
        // using selection sort method
        public static int[] selectionSort(int[] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int hold = 0;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                hold=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=hold;
            }
            return bb;
        }

        // sort elements in an array of longs into ascending order
        // using selection sort method
        public static long[] selectionSort(long[] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            long hold = 0L;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                hold=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=hold;
            }
            return bb;
        }

        // sort elements in an array of doubles into ascending order
        // using selection sort method
        // aa - the original array - not altered
        // bb - the sorted array
        // indices - an array of the original indices of the sorted array
        public static void selectionSort(double[] aa, double[] bb, int[] indices){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            double holdb = 0.0D;
            int holdi = 0;
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }
        }

        // sort the elements of an array of doubles into ascending order with matching switches in an array of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
        public static void selectionSort(double[] aa, double[] bb, double[] cc, double[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            double holdx = 0.0D;
            double holdy = 0.0D;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
        }

        // sort the elements of an array of floats into ascending order with matching switches in an array of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
        public static void selectionSort(float[] aa, float[] bb, float[] cc, float[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            float holdx = 0.0F;
            float holdy = 0.0F;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an longs of doubles into ascending order with matching switches in an array of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
       public static void selectionSort(long[] aa, long[] bb, long[] cc, long[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            long holdx = 0L;
            long holdy = 0L;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an array of ints into ascending order with matching switches in an array of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
       public static void selectionSort(int[] aa, int[] bb, int[] cc, int[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            int holdx = 0;
            int holdy = 0;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an array of doubles into ascending order with matching switches in an array of long of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
       public static void selectionSort(double[] aa, long[] bb, double[] cc, long[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            double holdx = 0.0D;
            long holdy = 0L;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
        }

        // sort the elements of an array of long into ascending order with matching switches in an array of double of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
        public static void selectionSort(long[] aa, double[] bb, long[] cc, double[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            long holdx = 0L;
            double holdy = 0.0D;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an array of doubles into ascending order with matching switches in an array of int of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
       public static void selectionSort(double[] aa, int[] bb, double[] cc, int[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            double holdx = 0.0D;
            int holdy = 0;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an array of int into ascending order with matching switches in an array of double of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
       public static void selectionSort(int[] aa, double[] bb, int[] cc, double[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            int holdx = 0;
            double holdy = 0.0D;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }

        // sort the elements of an array of long into ascending order with matching switches in an array of int of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
         public static void selectionSort(long[] aa, int[] bb, long[] cc, int[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            long holdx = 0L;
            int holdy = 0;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
        }

        // sort the elements of an array of int into ascending order with matching switches in an array of long of the length
        // using selection sort method
        // array determining the order is the first argument
        // matching array  is the second argument
        // sorted arrays returned as third and fourth arguments respectively
        public static void selectionSort(int[] aa, long[] bb, int[] cc, long[] dd){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(n!=m)throw new IllegalArgumentException("First argument array, aa, (length = " + n + ") and the second argument array, bb, (length = " + m + ") should be the same length");
            int nn = cc.length;
            if(nn<n)throw new IllegalArgumentException("The third argument array, cc, (length = " + nn + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int mm = dd.length;
            if(mm<m)throw new IllegalArgumentException("The fourth argument array, dd, (length = " + mm + ") should be at least as long as the second argument array, bb, (length = " + m + ")");

            int holdx = 0;
            long holdy = 0L;


            for(int i=0; i<n; i++){
                cc[i]=aa[i];
                dd[i]=bb[i];
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(cc[i]<cc[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=cc[index];
                cc[index]=cc[lastIndex];
                cc[lastIndex]=holdx;
                holdy=dd[index];
                dd[index]=dd[lastIndex];
                dd[lastIndex]=holdy;
            }
       }


        // sort elements in an array of doubles (first argument) into ascending order
        // using selection sort method
        // returns the sorted array as second argument
        //  and an array of the indices of the sorted array as the third argument
        // same as corresponding selectionSort - retained for backward compatibility
        public static void selectSort(double[] aa, double[] bb, int[] indices){
            int index = 0;
            int lastIndex = -1;
            int n = aa.length;
            int m = bb.length;
            if(m<n)throw new IllegalArgumentException("The second argument array, bb, (length = " + m + ") should be at least as long as the first argument array, aa, (length = " + n + ")");
            int k = indices.length;
            if(m<n)throw new IllegalArgumentException("The third argument array, indices, (length = " + k + ") should be at least as long as the first argument array, aa, (length = " + n + ")");

            double holdb = 0.0D;
            int holdi = 0;
            for(int i=0; i<n; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }
        }

        // COPY A ONE DIMENSIONAL ARRAY OF double
        public static double[] copy(double[] array){
            if(array==null)return null;
            int n = array.length;
            double[] copy = new double[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF float
        public static float[] copy(float[] array){
            if(array==null)return null;
            int n = array.length;
            float[] copy = new float[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF int
        public static int[] copy(int[] array){
            if(array==null)return null;
            int n = array.length;
            int[] copy = new int[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF long
        public static long[] copy(long[] array){
            if(array==null)return null;
            int n = array.length;
            long[] copy = new long[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF double
        public static double[][] copy(double[][] array){
            if(array==null)return null;
            int n = array.length;
            double[][] copy = new double[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF float
        public static float[][] copy(float[][] array){
            if(array==null)return null;
            int n = array.length;
            float[][] copy = new float[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF int
        public static int[][] copy(int[][] array){
            if(array==null)return null;
            int n = array.length;
            int[][] copy = new int[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF long
        public static long[][] copy(long[][] array){
            if(array==null)return null;
            int n = array.length;
            long[][] copy = new long[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF double
        public static double[][][] copy(double[][][] array){
            if(array==null)return null;
            int n = array.length;
            double[][][] copy = new double[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new double[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }


        // COPY A THREE DIMENSIONAL ARRAY OF float
        public static float[][][] copy(float[][][] array){
            if(array==null)return null;
            int n = array.length;
            float[][][] copy = new float[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new float[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF int
        public static int[][][] copy(int[][][] array){
            if(array==null)return null;
            int n = array.length;
            int[][][] copy = new int[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new int[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF long
        public static long[][][] copy(long[][][] array){
            if(array==null)return null;
            int n = array.length;
            long[][][] copy = new long[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new long[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF double
        public static double[][][][] copy(double[][][][] array){
            if(array==null)return null;
            int n = array.length;
            double[][][][] copy = new double[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new double[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new double[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF float
        public static float[][][][] copy(float[][][][] array){
            if(array==null)return null;
            int n = array.length;
            float[][][][] copy = new float[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new float[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new float[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF int
        public static int[][][][] copy(int[][][][] array){
            if(array==null)return null;
            int n = array.length;
            int[][][][] copy = new int[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new int[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new int[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF long
        public static long[][][][] copy(long[][][][] array){
            if(array==null)return null;
            int n = array.length;
            long[][][][] copy = new long[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new long[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new long[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

                // COPY A ONE DIMENSIONAL ARRAY OF String
        public static String[] copy(String[] array){
            if(array==null)return null;
            int n = array.length;
            String[] copy = new String[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF String
        public static String[][] copy(String[][] array){
            if(array==null)return null;
            int n = array.length;
            String[][] copy = new String[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF String
        public static String[][][] copy(String[][][] array){
            if(array==null)return null;
            int n = array.length;
            String[][][] copy = new String[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new String[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF String
        public static String[][][][] copy(String[][][][] array){
            if(array==null)return null;
            int n = array.length;
            String[][][][] copy = new String[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new String[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new String[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }




        // COPY A ONE DIMENSIONAL ARRAY OF boolean
        public static boolean[] copy(boolean[] array){
            if(array==null)return null;
            int n = array.length;
            boolean[] copy = new boolean[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF boolean
        public static boolean[][] copy(boolean[][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][] copy = new boolean[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF boolean
        public static boolean[][][] copy(boolean[][][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][][] copy = new boolean[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new boolean[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF boolean
        public static boolean[][][][] copy(boolean[][][][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][][][] copy = new boolean[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new boolean[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new boolean[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }



        // COPY A ONE DIMENSIONAL ARRAY OF char
        public static char[] copy(char[] array){
            if(array==null)return null;
            int n = array.length;
            char[] copy = new char[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF char
        public static char[][] copy(char[][] array){
            if(array==null)return null;
            int n = array.length;
            char[][] copy = new char[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF char
        public static char[][][] copy(char[][][] array){
            if(array==null)return null;
            int n = array.length;
            char[][][] copy = new char[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new char[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF char
        public static char[][][][] copy(char[][][][] array){
            if(array==null)return null;
            int n = array.length;
            char[][][][] copy = new char[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new char[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new char[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }



        // COPY OF AN OBJECT (deprecated - see Conv class)
        // Returns a copy of the object
        // An exception will be thrown if an attempt to copy a non-serialisable object is made.
        // Taken, with minor changes,  from { Java Techniques }
        // http://javatechniques.com/blog/
        public static Object copy(Object obj){
            if(obj==null)return null;
            return Fmath.copyObject(obj);
        }

        // COPY OF AN OBJECT (deprecated - see Conv class)
        // Returns a copy of the object
        // An exception will be thrown if an attempt to copy a non-serialisable object is made.
        // Taken, with minor changes,  from { Java Techniques }
        // http://javatechniques.com/blog/
        public static Object copyObject(Object obj){
            if(obj==null)return null;
            Object objCopy = null;
            try {
                // Write the object out to a byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                oos.close();
                // Make an input stream from the byte array and
                // read a copy of the object back in.
                ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
                objCopy = ois.readObject();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            catch(ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            return objCopy;
        }

        // UNIT CONVERSIONS (deprecated - see Conv class)

        // Converts radians to degrees
        public static double radToDeg(double rad){
            return  rad*180.0D/Math.PI;
        }

        // Converts degrees to radians
        public static double degToRad(double deg){
            return  deg*Math.PI/180.0D;
        }

        // Converts frequency (Hz) to radial frequency
        public static double frequencyToRadialFrequency(double frequency){
            return  2.0D*Math.PI*frequency;
        }

        // Converts radial frequency to frequency (Hz)
        public static double radialFrequencyToFrequency(double radial){
            return  radial/(2.0D*Math.PI);
        }

        // Converts electron volts(eV) to corresponding wavelength in nm
        public static double evToNm(double ev){
            return  1e+9*C_LIGHT/(-ev*Q_ELECTRON/H_PLANCK);
        }

        // Converts wavelength in nm to matching energy in eV
        public static double nmToEv(double nm)
        {
            return  C_LIGHT/(-nm*1e-9)*H_PLANCK/Q_ELECTRON;
        }

        // Converts moles per litre to percentage weight by volume
        public static double molarToPercentWeightByVol(double molar, double molWeight){
            return  molar*molWeight/10.0D;
        }

        // Converts percentage weight by volume to moles per litre
        public static double percentWeightByVolToMolar(double perCent, double molWeight){
            return  perCent*10.0D/molWeight;
        }

        // Converts Celsius to Kelvin
        public static double celsiusToKelvin(double cels){
            return  cels-T_ABS;
        }

        // Converts Kelvin to Celsius
        public static double kelvinToCelsius(double kelv){
            return  kelv+T_ABS;
        }

        // Converts Celsius to Fahrenheit
        public static double celsiusToFahren(double cels){
            return  cels*(9.0/5.0)+32.0;
        }

        // Converts Fahrenheit to Celsius
        public static double fahrenToCelsius(double fahr){
            return  (fahr-32.0)*5.0/9.0;
        }

        // Converts calories to Joules
        public static double calorieToJoule(double cal){
            return  cal*4.1868;
        }

        // Converts Joules to calories
        public static double jouleToCalorie(double joule){
            return  joule*0.23884;
        }

        // Converts grams to ounces
        public static double gramToOunce(double gm){
            return  gm/28.3459;
        }

        // Converts ounces to grams
        public static double ounceToGram(double oz){
            return  oz*28.3459;
        }

        // Converts kilograms to pounds
        public static double kgToPound(double kg){
            return  kg/0.4536;
        }

        // Converts pounds to kilograms
        public static double poundToKg(double pds){
            return  pds*0.4536;
        }

        // Converts kilograms to tons
        public static double kgToTon(double kg){
            return  kg/1016.05;
        }

        // Converts tons to kilograms
        public static double tonToKg(double tons){
            return  tons*1016.05;
        }

        // Converts millimetres to inches
        public static double millimetreToInch(double mm){
            return  mm/25.4;
        }

        // Converts inches to millimetres
        public static double inchToMillimetre(double in){
            return  in*25.4;
        }

        // Converts feet to metres
        public static double footToMetre(double ft){
            return  ft*0.3048;
        }

        // Converts metres to feet
        public static double metreToFoot(double metre){
            return  metre/0.3048;
        }

        // Converts yards to metres
        public static double yardToMetre(double yd){
            return  yd*0.9144;
        }

        // Converts metres to yards
        public static double metreToYard(double metre){
            return  metre/0.9144;
        }

        // Converts miles to kilometres
        public static double mileToKm(double mile){
            return  mile*1.6093;
        }

        // Converts kilometres to miles
        public static double kmToMile(double km){
            return  km/1.6093;
        }

        // Converts UK gallons to litres
        public static double gallonToLitre(double gall){
            return  gall*4.546;
        }

        // Converts litres to UK gallons
        public static double litreToGallon(double litre){
            return  litre/4.546;
        }

        // Converts UK quarts to litres
        public static double quartToLitre(double quart){
            return  quart*1.137;
        }

        // Converts litres to UK quarts
        public static double litreToQuart(double litre){
            return  litre/1.137;
        }

        // Converts UK pints to litres
        public static double pintToLitre(double pint){
            return  pint*0.568;
        }

        // Converts litres to UK pints
        public static double litreToPint(double litre){
            return  litre/0.568;
        }

        // Converts UK gallons per mile to litres per kilometre
        public static double gallonPerMileToLitrePerKm(double gallPmile){
            return  gallPmile*2.825;
        }

        // Converts litres per kilometre to UK gallons per mile
        public static double litrePerKmToGallonPerMile(double litrePkm){
            return  litrePkm/2.825;
        }

        // Converts miles per UK gallons to kilometres per litre
        public static double milePerGallonToKmPerLitre(double milePgall){
            return  milePgall*0.354;
        }

        // Converts kilometres per litre to miles per UK gallons
        public static double kmPerLitreToMilePerGallon(double kmPlitre){
            return  kmPlitre/0.354;
        }

        // Converts UK fluid ounce to American fluid ounce
        public static double fluidOunceUKtoUS(double flOzUK){
            return  flOzUK*0.961;
        }

        // Converts American fluid ounce to UK fluid ounce
        public static double fluidOunceUStoUK(double flOzUS){
            return  flOzUS*1.041;
        }

        // Converts UK pint to American liquid pint
        public static double pintUKtoUS(double pintUK){
            return  pintUK*1.201;
        }

        // Converts American liquid pint to UK pint
        public static double pintUStoUK(double pintUS){
            return  pintUS*0.833;
        }

        // Converts UK quart to American liquid quart
        public static double quartUKtoUS(double quartUK){
            return  quartUK*1.201;
        }

        // Converts American liquid quart to UK quart
        public static double quartUStoUK(double quartUS){
            return  quartUS*0.833;
        }

        // Converts UK gallon to American gallon
        public static double gallonUKtoUS(double gallonUK){
            return  gallonUK*1.201;
        }

        // Converts American gallon to UK gallon
        public static double gallonUStoUK(double gallonUS){
            return  gallonUS*0.833;
        }

        // Converts UK pint to American cup
        public static double pintUKtoCupUS(double pintUK){
            return  pintUK/0.417;
        }

        // Converts American cup to UK pint
        public static double cupUStoPintUK(double cupUS){
            return  cupUS*0.417;
        }

        // Calculates body mass index (BMI) from height (m) and weight (kg)
        public static double calcBMImetric(double height, double weight){
            return  weight/(height*height);
        }

        // Calculates body mass index (BMI) from height (ft) and weight (lbs)
        public static double calcBMIimperial(double height, double weight){
                height = Fmath.footToMetre(height);
                weight = Fmath.poundToKg(weight);
            return  weight/(height*height);
        }

        // Calculates weight (kg) to give a specified BMI for a given height (m)
        public static double calcWeightFromBMImetric(double bmi, double height){
            return bmi*height*height;
        }

        // Calculates weight (lbs) to give a specified BMI for a given height (ft)
        public static double calcWeightFromBMIimperial(double bmi, double height){
            height = Fmath.footToMetre(height);
            double weight = bmi*height*height;
            weight = Fmath.kgToPound(weight);
            return  weight;
        }
}

