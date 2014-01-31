/*
*   Class RealRoot
*
*   Contains methods for finding a real root
*
*   The function whose root is to be determined is supplied
*   by means of an interface, RealRootFunction,
*   if no derivative required
*
*   The function whose root is to be determined is supplied
*   by means of an interface, RealRootDerivFunction,
*   as is the first derivative if a derivative is required
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:   18 May 2003
*   UPDATE: May 2003 - March 2008,  23-24 September 2008,  30 January 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RealRoot.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2010    Michael Thomas Flanagan
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

package flanagan.roots;

import java.util.*;
import flanagan.math.Fmath;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;


// RealRoot class
public class RealRoot{

    // INSTANCE VARIABLES

    private double root = Double.NaN;                           // root to be found
    private double tol = 1e-9;                                  // tolerance in determining convergence upon a root
    private int iterMax = 3000;                                 // maximum number of iterations allowed in root search
    private int iterN = 0;                                      // number of iterations taken in root search
    private double upperBound = 0;                              // upper bound for bisection and false position methods
    private double lowerBound = 0;                              // lower bound for bisection and false position methods
    private double estimate = 0;                                // estimate for Newton-Raphson method
    private int maximumBoundsExtension = 100;                   // number of times that the bounds may be extended
                                                                // by the difference separating them if the root is
                                                                // found not to be bounded
    private boolean noBoundExtensions = false;                  // = true if number of no extension to the  bounds allowed
    private boolean noLowerBoundExtensions = false;             // = true if number of no extension to the lower bound allowed
    private boolean noUpperBoundExtensions = false;             // = true if number of no extension to the upper bound allowed
    private boolean supressLimitReachedMessage = false;         // if true, supresses printing of the iteration limit reached message
    private boolean returnNaN = false;                          // if true exceptions resulting from a bound being NaN do not halt the prorgam but return NaN
                                                                // required by PsRandom and Stat classes calling RealRoot
    private boolean supressNaNmessage = false;                  // if = true the bound is NaN root returned as NaN message supressed

    // STATC VARIABLE

    private static int staticIterMax = 3000;                    // maximum number of iterations allowed in root search (static methods)

    private static int maximumStaticBoundsExtension = 100;      // number of times that the bounds may be extended
                                                                // by the difference separating them if the root is
                                                                // found not to be bounded (static methods)
    private static boolean noStaticBoundExtensions = false;     // = true if number of no extension to the  bounds allowed (static methods)
    private static boolean noStaticLowerBoundExtensions = false;// = true if number of no extension to the lower bound allowed (static methods)
    private static boolean noStaticUpperBoundExtensions = false;// = true if number of no extension to the upper bound allowed (static methods)
    private static boolean staticReturnNaN = false;             // if true exceptions resulting from a bound being NaN do not halt the prorgam but return NaN
                                                                // required by PsRandom and Stat classes calling RealRoot (static methods)
    private static double realTol = 1e-14;                      // tolerance as imag/real in deciding whether a root is real

    // CONSTRUCTOR
    public RealRoot(){
    }

    // INSTANCE METHODS

    // Set lower bound
    public void setLowerBound(double lower){
        this.lowerBound = lower;
    }

    // Set lower bound
    public void setUpperBound(double upper){
        this.upperBound = upper;
    }

    // Reset exception handling for NaN bound flag to true
    // when flag returnNaN = true exceptions resulting from a bound being NaN do not halt the prorgam but return NaN
    // required by PsRandom and Stat classes calling RealRoot
    public void resetNaNexceptionToTrue(){
        this.returnNaN = true;
    }

    // Reset exception handling for NaN bound flag to false
    // when flag returnNaN = false exceptions resulting from a bound being NaN  halts the prorgam
    // required by PsRandom and Stat classes calling RealRoot
    public void resetNaNexceptionToFalse(){
        this.returnNaN = false;
    }

    // Supress NaN bound message
    // if supressNaNmessage = true the bound is NaN root returned as NaN message supressed
    public void supressNaNmessage(){
        this.supressNaNmessage = true;
    }

    // Allow  NaN bound message
    // if supressNaNmessage = false the bound is NaN root returned as NaN message is written
    public void allowNaNmessage(){
        this.supressNaNmessage = false;
    }

    // Set estimate
    public void setEstimate(double estimate){
        this.estimate = estimate;
    }

    // Reset the default tolerance
    public void setTolerance(double tolerance){
        this.tol=tolerance;
    }

    // Get the default tolerance
    public double getTolerance(){
        return this.tol;
    }

    // Reset the maximum iterations allowed
    public void setIterMax(int imax){
        this.iterMax=imax;
    }

    // Get the maximum iterations allowed
    public int getIterMax(){
        return this.iterMax;
    }

    // Get the number of iterations taken
    public int getIterN(){
        return this.iterN;
    }

    // Reset the maximum number of bounds extensions
    public void setmaximumStaticBoundsExtension(int maximumBoundsExtension){
        this.maximumBoundsExtension=maximumBoundsExtension;
    }

    // Prevent extensions to the supplied bounds
    public void noBoundsExtensions(){
        this.noBoundExtensions = true;
        this.noLowerBoundExtensions = true;
        this.noUpperBoundExtensions = true;
    }

    // Prevent extension to the lower bound
    public void noLowerBoundExtension(){
        this.noLowerBoundExtensions = true;
        if(this.noUpperBoundExtensions)this.noBoundExtensions = true;
    }

    // Prevent extension to the upper bound
    public void noUpperBoundExtension(){
        this.noUpperBoundExtensions = true;
        if(this.noLowerBoundExtensions)this.noBoundExtensions = true;
    }

    // Supresses printing of the iteration limit reached message
    // USE WITH CARE - added only to accomadate a specific application using this class!!!!!
    public void supressLimitReachedMessage(){
        this.supressLimitReachedMessage = true;
    }

    // Combined bisection and Inverse Quadratic Interpolation method
    // bounds already entered
   	public double brent(RealRootFunction g){
        return this.brent(g, this.lowerBound, this.upperBound);
    }

    // Combined bisection and Inverse Quadratic Interpolation method
    // bounds supplied as arguments
   	public double brent(RealRootFunction g, double lower, double upper){
   	    this.lowerBound = lower;
   	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("Realroot: brent: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("Realroot: brent: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.brent: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    if(!this.supressNaNmessage)System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.brent: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        if(!this.supressNaNmessage)System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;  // mid point (bisect) or new x estimate (Inverse Quadratic Interpolation)
        double lastMidB = mid;          // last succesful mid point
        double fm = g.function(mid);
        double diff = mid-lower;        // difference between successive estimates of the root
        double fmB = fm;                // last succesful mid value function value
        double lastMid=mid;
        boolean lastMethod = true;      // true; last method = Inverse Quadratic Interpolation, false; last method = bisection method
        boolean nextMethod = true;      // true; next method = Inverse Quadratic Interpolation, false; next method = bisection method

	    // search
	    double rr=0.0D, ss=0.0D, tt=0.0D, pp=0.0D, qq=0.0D; // interpolation variables
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<this.tol){
	            testConv=false;
	            if(fm==0.0D){
	                this.root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<this.tol)this.root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull inverse quadratic interpolation
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // inverse quadratic interpolation failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // inverse quadratic interpolation
		            fl=g.function(lower);
	                fm=g.function(mid);
	                fu=g.function(upper);
	                rr=fm/fu;
	                ss=fm/fl;
	                tt=fl/fu;
	                pp=ss*(tt*(rr-tt)*(upper-mid)-(1.0D-rr)*(mid-lower));
	                qq=(tt-1.0D)*(rr-1.0D)*(ss-1.0D);
	                lastMid=mid;
	                diff=pp/qq;
	                mid=mid+diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                fm=g.function(mid);
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            this.iterN++;
            if(this.iterN>this.iterMax){
                if(!this.supressLimitReachedMessage){
                    if(!this.supressNaNmessage)System.out.println("Class: RealRoot; method: brent; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) + ", returned");
                    if(!this.supressNaNmessage)System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + this.tol);
                }
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // bisection method
    // bounds already entered
	public double bisect(RealRootFunction g){
	    return this.bisect(g, this.lowerBound, this.upperBound);
	}

    // bisection method
	public double bisect(RealRootFunction g, double lower, double upper){
   	    this.lowerBound = lower;
   	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
            double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: bisect: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: bisect: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }
        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.bisect: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    if(!this.supressNaNmessage)System.out.println(message);
                    return Double.NaN;

                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.bisect: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        if(!this.supressNaNmessage)System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = (lower+upper)/2.0D;    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<this.tol){
                testConv=false;
                this.root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = (lower+upper)/2.0D;
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            this.iterN++;
            if(this.iterN>this.iterMax){
                if(!this.supressLimitReachedMessage){
                    if(!this.supressNaNmessage)System.out.println("Class: RealRoot; method: bisect; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                    if(!this.supressNaNmessage)System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + this.tol);
                }
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // false position  method
    // bounds already entered
	public double falsePosition(RealRootFunction g){
	    return this.falsePosition(g, this.lowerBound, this.upperBound);
    }

    // false position  method
	public double falsePosition(RealRootFunction g, double lower, double upper){
	    this.lowerBound = lower;
	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
 	        double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: fals: ePositionlower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: falsePosition: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.falsePosition: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    if(!this.supressNaNmessage)System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.falsePosition: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        if(!this.supressNaNmessage)System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<this.tol){
                testConv=false;
                this.root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            this.iterN++;
            if(this.iterN>this.iterMax){
                if(!this.supressLimitReachedMessage){
                    if(!this.supressNaNmessage)System.out.println("Class: RealRoot; method: falsePostion; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                    if(!this.supressNaNmessage)System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + this.tol);
                }
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // Combined bisection and Newton Raphson method
    // bounds already entered
   	public double bisectNewtonRaphson(RealRootDerivFunction g){
   	    return this.bisectNewtonRaphson(g, this.lowerBound, this.upperBound);
   	}

    // Combined bisection and Newton Raphson method
    // default accuracy used
   	public double bisectNewtonRaphson(RealRootDerivFunction g, double lower, double upper){
	    this.lowerBound = lower;
	    this.upperBound = upper;

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double[] f = g.function(upper);
	    double fu=f[0];
	    // calculate the function value at the estimate of the lower bound of x
	    f = g.function(lower);
	    double fl=f[0];
	    if(Double.isNaN(fl)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: bisectNewtonRaphson: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: bisectNewtonRaphson: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(this.noBoundExtensions){
                    String message = "RealRoot.bisectNewtonRaphson: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    if(!this.supressNaNmessage)System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>this.maximumBoundsExtension){
                        String message = "RealRoot.bisectNewtonRaphson: root not bounded and maximum number of extension to bounds allowed, " + this.maximumBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        if(!this.supressNaNmessage)System.out.println(message);
                        return Double.NaN;
                    }
                    if(!this.noLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        f = g.function(lower);
                        fl = f[0];
                    }
                    if(!this.noUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        f = g.function(upper);
                        fu = f[0];
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        this.root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        this.root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;      // mid point (bisect) or new x estimate (Newton-Raphson)
        double lastMidB = mid;              // last succesful mid point
        f = g.function(mid);
        double diff = f[0]/f[1];            // difference between successive estimates of the root
        double fm = f[0];
        double fmB = fm;                    // last succesful mid value function value
        double lastMid=mid;
        mid = mid-diff;
        boolean lastMethod = true;          // true; last method = Newton Raphson, false; last method = bisection method
        boolean nextMethod = true;          // true; next method = Newton Raphson, false; next method = bisection method

	    // search
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<this.tol){
	            testConv=false;
	            if(fm==0.0D){
	                this.root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<this.tol)this.root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull Newton-Raphson
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // Newton Raphson failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // Newton-Raphson procedure
	                f=g.function(mid);
	                fm=f[0];
	                diff=f[0]/f[1];
	                lastMid=mid;
	                mid=mid-diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                f=g.function(mid);
	                fm=f[0];
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            this.iterN++;
            if(this.iterN>this.iterMax){
                if(!this.supressLimitReachedMessage){
                    if(!this.supressNaNmessage)System.out.println("Class: RealRoot; method: bisectNewtonRaphson; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                    if(!this.supressNaNmessage)System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + this.tol);
                }
                this.root = mid;
                testConv = false;
            }
        }
        return this.root;
    }

    // Newton Raphson method
    // estimate already entered
	public double newtonRaphson(RealRootDerivFunction g){
	    return this.newtonRaphson(g, this.estimate);

	}

    // Newton Raphson method
	public double newtonRaphson(RealRootDerivFunction g, double x){
	    this.estimate = x;
        boolean testConv = true;    // convergence test: becomes false on convergence
        this.iterN = 0;             // number of iterations
        double diff = 1e300;        // difference between the last two successive mid-pint x values

	    // calculate the function and derivative value at the initial estimate  x
	    double[] f = g.function(x);
	    if(Double.isNaN(f[0])){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: newtonRaphson: NaN returned as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("NaN returned as the function value");
	        }
	    }
        if(Double.isNaN(f[1])){
	        if(this.returnNaN){
	            if(!this.supressNaNmessage)System.out.println("RealRoot: newtonRaphson: NaN returned as the derivative function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("NaN returned as the derivative function value");
	        }
	    }


	    // search
        while(testConv){
            diff = f[0]/f[1];
            if(f[0]==0.0D || Math.abs(diff)<this.tol){
                this.root = x;
                testConv=false;
            }
            else{
                x -= diff;
                f = g.function(x);
	            if(Double.isNaN(f[0]))throw new ArithmeticException("NaN returned as the function value");
	            if(Double.isNaN(f[1]))throw new ArithmeticException("NaN returned as the derivative function value");
	            if(Double.isNaN(f[0])){
	                if(this.returnNaN){
	                    if(!this.supressNaNmessage)System.out.println("RealRoot: bisect: NaN as the function value - NaN returned as root");
	                    return Double.NaN;
	                }
	                else{
	                    throw new ArithmeticException("NaN as the function value");
	                }
	            }
                if(Double.isNaN(f[1])){
	                if(this.returnNaN){
	                    if(!this.supressNaNmessage)System.out.println("NaN as the function value - NaN returned as root");
	                    return Double.NaN;
	                }
	                else{
	                    throw new ArithmeticException("NaN as the function value");
	                }
	            }
            }
            this.iterN++;
            if(this.iterN>this.iterMax){
                if(!this.supressLimitReachedMessage){
                    if(!this.supressNaNmessage)System.out.println("Class: RealRoot; method: newtonRaphson; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(x, 4) + ", returned");
                    if(!this.supressNaNmessage)System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + this.tol);
                }
                this.root = x;
                testConv = false;
            }
        }
        return this.root;
    }

    // STATIC METHODS

    // Reset the maximum iterations allowed  for static methods
    public void setStaticIterMax(int imax){
        RealRoot.staticIterMax = imax;
    }

    // Get the maximum iterations allowed  for static methods
    public int getStaticIterMax(){
        return RealRoot.staticIterMax;
    }

    // Reset the maximum number of bounds extensions for static methods
    public void setStaticMaximumStaticBoundsExtension(int maximumBoundsExtension){
        RealRoot.maximumStaticBoundsExtension = maximumBoundsExtension;
    }

    // Prevent extensions to the supplied bounds for static methods
    public void noStaticBoundsExtensions(){
        RealRoot.noStaticBoundExtensions = true;
        RealRoot.noStaticLowerBoundExtensions = true;
        RealRoot.noStaticUpperBoundExtensions = true;
    }

    // Prevent extension to the lower bound for static methods
    public void noStaticLowerBoundExtension(){
        RealRoot.noStaticLowerBoundExtensions = true;
        if(RealRoot.noStaticUpperBoundExtensions)RealRoot.noStaticBoundExtensions = true;
    }

    // Prevent extension to the upper bound for static methods
    public void noStaticUpperBoundExtension(){
        RealRoot.noStaticUpperBoundExtensions = true;
        if(RealRoot.noStaticLowerBoundExtensions)RealRoot.noStaticBoundExtensions = true;
    }

    // Reset exception handling for NaN bound flag to true for static methods
    // when flag returnNaN = true exceptions resulting from a bound being NaN do not halt the prorgam but return NaN
    // required by PsRandom and Stat classes calling RealRoot
    public void resetStaticNaNexceptionToTrue(){
        this.staticReturnNaN = true;
    }

    // Reset exception handling for NaN bound flag to false for static methods
    // when flag returnNaN = false exceptions resulting from a bound being NaN  halts the prorgam
    // required by PsRandom and Stat classes calling RealRoot
    public void resetStaticNaNexceptionToFalse(){
        this.staticReturnNaN= false;
    }




    // Combined bisection and Inverse Quadratic Interpolation method
    // bounds supplied as arguments
   	public static double brent(RealRootFunction g, double lower, double upper, double tol){
	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        double root = Double.NaN;
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("Realroot: brent: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("Realroot: brent: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(RealRoot.noStaticBoundExtensions){
                    String message = "RealRoot.brent: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>RealRoot.maximumStaticBoundsExtension){
                        String message = "RealRoot.brent: root not bounded and maximum number of extension to bounds allowed, " + RealRoot.maximumStaticBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!RealRoot.noStaticLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!RealRoot.noStaticUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;      // mid point (bisect) or new x estimate (Inverse Quadratic Interpolation)
        double lastMidB = mid;              // last succesful mid point
        double fm = g.function(mid);
        double diff = mid-lower;            // difference between successive estimates of the root
        double fmB = fm;                    // last succesful mid value function value
        double lastMid=mid;
        boolean lastMethod = true;          // true; last method = Inverse Quadratic Interpolation, false; last method = bisection method
        boolean nextMethod = true;          // true; next method = Inverse Quadratic Interpolation, false; next method = bisection method

	    // search
	    double rr=0.0D, ss=0.0D, tt=0.0D, pp=0.0D, qq=0.0D; // interpolation variables
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<tol){
	            testConv=false;
	            if(fm==0.0D){
	                root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<tol)root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull inverse quadratic interpolation
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // inverse quadratic interpolation failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // inverse quadratic interpolation
		            fl=g.function(lower);
	                fm=g.function(mid);
	                fu=g.function(upper);
	                rr=fm/fu;
	                ss=fm/fl;
	                tt=fl/fu;
	                pp=ss*(tt*(rr-tt)*(upper-mid)-(1.0D-rr)*(mid-lower));
	                qq=(tt-1.0D)*(rr-1.0D)*(ss-1.0D);
	                lastMid=mid;
	                diff=pp/qq;
	                mid=mid+diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                fm=g.function(mid);
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            iterN++;
            if(iterN>RealRoot.staticIterMax){
                System.out.println("Class: RealRoot; method: brent; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) + ", returned");
                System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }


    // bisection method
    // tolerance supplied
	public static double bisect(RealRootFunction g, double lower, double upper, double tol){

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
            double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: bisect: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: bisect: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }
        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds = false;
            }
            else{
                if(RealRoot.noStaticBoundExtensions){
                    String message = "RealRoot.bisect: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;

                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>RealRoot.maximumStaticBoundsExtension){
                        String message = "RealRoot.bisect: root not bounded and maximum number of extension to bounds allowed, " + RealRoot.maximumStaticBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!RealRoot.noStaticLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!RealRoot.noStaticUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = (lower+upper)/2.0D;    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<tol){
                testConv=false;
                root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = (lower+upper)/2.0D;
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            iterN++;
            if(iterN>RealRoot.staticIterMax){
                System.out.println("Class: RealRoot; method: bisect; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }






    // false position  method
    // tolerance supplied
	public static double falsePosition(RealRootFunction g, double lower, double upper, double tol){

        // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");
	    if(upper<lower){
 	        double temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    double root = Double.NaN;   // variable to hold the returned root
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        double diff = 1e300;        // abs(difference between the last two successive mid-pint x values)

	    // calculate the function value at the estimate of the higher bound to x
	    double fu = g.function(upper);
	    // calculate the function value at the estimate of the lower bound of x
	    double fl = g.function(lower);
	    if(Double.isNaN(fl)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: fals: ePositionlower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: falsePosition: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(RealRoot.noStaticBoundExtensions){
                    String message = "RealRoot.falsePosition: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>RealRoot.maximumStaticBoundsExtension){
                        String message = "RealRoot.falsePosition: root not bounded and maximum number of extension to bounds allowed, " + RealRoot.maximumStaticBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!RealRoot.noStaticLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        fl = g.function(lower);
                    }
                    if(!RealRoot.noStaticUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        fu = g.function(upper);
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // start search
        double mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
        double lastMid = 1e300;             // previous mid-point
        double fm = g.function(mid);
        while(testConv){
            if(fm==0.0D || diff<tol){
                testConv=false;
                root=mid;
            }
            if(fm*fl>0.0D){
                lower = mid;
                fl=fm;
            }
            else{
                upper = mid;
                fu=fm;
            }
            lastMid = mid;
            mid = lower+(upper-lower)*Math.abs(fl)/(Math.abs(fl)+Math.abs(fu));    // mid-point
            fm = g.function(mid);
            diff = Math.abs(mid-lastMid);
            iterN++;
            if(iterN>RealRoot.staticIterMax){
                System.out.println("Class: RealRoot; method: falsePostion; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }






    // Combined bisection and Newton Raphson method
    // tolerance supplied
   	public static double bisectNewtonRaphson(RealRootDerivFunction g, double lower, double upper, double tol){

	    // check upper>lower
	    if(upper==lower)throw new IllegalArgumentException("upper cannot equal lower");

        double root = Double.NaN;
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        double temp = 0.0D;

        if(upper<lower){
 	        temp = upper;
	        upper = lower;
	        lower = temp;
	    }

	    // calculate the function value at the estimate of the higher bound to x
	    double[] f = g.function(upper);
	    double fu=f[0];
	    // calculate the function value at the estimate of the lower bound of x
	    f = g.function(lower);
	    double fl=f[0];
	    if(Double.isNaN(fl)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: bisectNewtonRaphson: lower bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("lower bound returned NaN as the function value");
	        }
	    }
        if(Double.isNaN(fu)){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: bisectNewtonRaphson: upper bound returned NaN as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("upper bound returned NaN as the function value");
	        }
	    }

        // check that the root has been bounded and extend bounds if not and extension allowed
        boolean testBounds = true;
        int numberOfBoundsExtension = 0;
        double initialBoundsDifference = (upper - lower)/2.0D;
        while(testBounds){
            if(fu*fl<=0.0D){
                testBounds=false;
            }
            else{
                if(RealRoot.noStaticBoundExtensions){
                    String message = "RealRoot.bisectNewtonRaphson: root not bounded and no extension to bounds allowed\n";
                    message += "NaN returned";
                    System.out.println(message);
                    return Double.NaN;
                }
                else{
                    numberOfBoundsExtension++;
                    if(numberOfBoundsExtension>RealRoot.maximumStaticBoundsExtension){
                        String message = "RealRoot.bisectNewtonRaphson: root not bounded and maximum number of extension to bounds allowed, " + RealRoot.maximumStaticBoundsExtension + ", exceeded\n";
                        message += "NaN returned";
                        System.out.println(message);
                        return Double.NaN;
                    }
                    if(!RealRoot.noStaticLowerBoundExtensions){
                        lower -= initialBoundsDifference;
                        f = g.function(lower);
                        fl = f[0];
                    }
                    if(!RealRoot.noStaticUpperBoundExtensions){
                        upper += initialBoundsDifference;
                        f = g.function(upper);
                        fu = f[0];
                    }
                }
            }
        }

	    // check initial values for true root value
	    if(fl==0.0D){
	        root=lower;
	        testConv = false;
	    }
	    if(fu==0.0D){
	        root=upper;
	        testConv = false;
	    }

	    // Function at mid-point of initial estimates
        double mid=(lower+upper)/2.0D;      // mid point (bisect) or new x estimate (Newton-Raphson)
        double lastMidB = mid;              // last succesful mid point
        f = g.function(mid);
        double diff = f[0]/f[1];            // difference between successive estimates of the root
        double fm = f[0];
        double fmB = fm;                    // last succesful mid value function value
        double lastMid=mid;
        mid = mid-diff;
        boolean lastMethod = true;          // true; last method = Newton Raphson, false; last method = bisection method
        boolean nextMethod = true;          // true; next method = Newton Raphson, false; next method = bisection method

	    // search
	    while(testConv){
	        // test for convergence
	        if(fm==0.0D || Math.abs(diff)<tol){
	            testConv=false;
	            if(fm==0.0D){
	                root=lastMid;
	            }
	            else{
	                if(Math.abs(diff)<tol)root=mid;
	            }
	        }
	        else{
	            lastMethod=nextMethod;
	            // test for succesfull Newton-Raphson
	            if(lastMethod){
	                if(mid<lower || mid>upper){
	                    // Newton Raphson failed
	                    nextMethod=false;
	                }
	                else{
	                    fmB=fm;
	                    lastMidB=mid;
	                }
	            }
	            else{
	                nextMethod=true;
	            }
		        if(nextMethod){
		            // Newton-Raphson procedure
	                f=g.function(mid);
	                fm=f[0];
	                diff=f[0]/f[1];
	                lastMid=mid;
	                mid=mid-diff;
	            }
	            else{
	                // Bisection procedure
	                fm=fmB;
	                mid=lastMidB;
	                if(fm*fl>0.0D){
	                    lower=mid;
	                    fl=fm;
	                }
	                else{
	                    upper=mid;
	                    fu=fm;
	                }
	                lastMid=mid;
	                mid=(lower+upper)/2.0D;
	                f=g.function(mid);
	                fm=f[0];
	                diff=mid-lastMid;
	                fmB=fm;
	                lastMidB=mid;
	            }
	        }
            iterN++;
            if(iterN>RealRoot.staticIterMax){
                System.out.println("Class: RealRoot; method: bisectNewtonRaphson; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(mid, 4) +  ", returned");
                System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + tol);
                root = mid;
                testConv = false;
            }
        }
        return root;
    }





    // Newton Raphson method
	public static double newtonRaphson(RealRootDerivFunction g, double x, double tol){
	    double root = Double.NaN;
        boolean testConv = true;    // convergence test: becomes false on convergence
        int iterN = 0;              // number of iterations
        double diff = 1e300;        // difference between the last two successive mid-pint x values

	    // calculate the function and derivative value at the initial estimate  x
	    double[] f = g.function(x);
	    if(Double.isNaN(f[0])){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: newtonRaphson: NaN returned as the function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("NaN returned as the function value");
	        }
	    }
        if(Double.isNaN(f[1])){
	        if(RealRoot.staticReturnNaN){
	            System.out.println("RealRoot: newtonRaphson: NaN returned as the derivative function value - NaN returned as root");
	            return Double.NaN;
	        }
	        else{
	            throw new ArithmeticException("NaN returned as the derivative function value");
	        }
	    }


	    // search
        while(testConv){
            diff = f[0]/f[1];
            if(f[0]==0.0D || Math.abs(diff)<tol){
                root = x;
                testConv=false;
            }
            else{
                x -= diff;
                f = g.function(x);
	            if(Double.isNaN(f[0]))throw new ArithmeticException("NaN returned as the function value");
	            if(Double.isNaN(f[1]))throw new ArithmeticException("NaN returned as the derivative function value");
	            if(Double.isNaN(f[0])){
	                if(RealRoot.staticReturnNaN){
	                    System.out.println("RealRoot: NewtonRaphson: NaN as the function value - NaN returned as root");
	                    return Double.NaN;
	                }
	                else{
	                    throw new ArithmeticException("NaN as the function value");
	                }
	            }
                if(Double.isNaN(f[1])){
	                if(RealRoot.staticReturnNaN){
	                    System.out.println("NaN as the function value - NaN returned as root");
	                    return Double.NaN;
	                }
	                else{
	                    throw new ArithmeticException("NaN as the function value");
	                }
	            }
            }
            iterN++;
            if(iterN>RealRoot.staticIterMax){
                System.out.println("Class: RealRoot; method: newtonRaphson; maximum number of iterations exceeded - root at this point, " + Fmath.truncate(x, 4) + ", returned");
                System.out.println("Last mid-point difference = " + Fmath.truncate(diff, 4) +  ", tolerance = " + tol);
                root = x;
                testConv = false;
            }
        }
        return root;
    }

    // ROOTS OF A QUADRATIC EQUATION
    // c + bx + ax^2 = 0
    // roots returned in root[]
    // 4ac << b*b accomodated by these methods
    // roots returned as Double in an ArrayList if roots are real
    // roots returned as Complex in an ArrayList if any root is complex
    public static ArrayList<Object> quadratic(double c, double b, double a){

            ArrayList<Object> roots = new ArrayList<Object>(2);

            double bsquared = b*b;
            double fourac = 4.0*a*c;
            if(bsquared<fourac){
                Complex[] croots = ComplexPoly.quadratic(c, b, a);
                roots.add("complex");
                roots.add(croots);
            }
            else{
                double[] droots = new double[2];
                double  bsign = Fmath.sign(b);
                double qsqrt = Math.sqrt(bsquared - fourac);
                qsqrt = -0.5*(b + bsign*qsqrt);
                droots[0] = qsqrt/a;
                droots[1] = c/qsqrt;
                roots.add("real");
                roots.add(droots);
            }
            return roots;
    }


    // ROOTS OF A CUBIC EQUATION
    // a + bx + cx^2 + dx^3 = 0
    // roots returned as Double in an ArrayList if roots are real
    // roots returned as Complex in an ArrayList if any root is complex
    public static ArrayList<Object> cubic(double a, double b, double c, double d){

            ArrayList<Object> roots = new ArrayList<Object>(2);

            double aa = c/d;
            double bb = b/d;
            double cc = a/d;
            double bigQ = (aa*aa - 3.0*bb)/9.0;
            double bigQcubed = bigQ*bigQ*bigQ;
            double bigR = (2.0*aa*aa*aa - 9.0*aa*bb + 27.0*cc)/54.0;
            double bigRsquared = bigR*bigR;

            if(bigRsquared>=bigQcubed){
                Complex[] croots = ComplexPoly.cubic(a, b, c, d);
                roots.add("complex");
                roots.add(croots);
            }
            else{
                double[] droots = new double[3];
                double theta = Math.acos(bigR/Math.sqrt(bigQcubed));
                double aover3 = aa/3.0;
                double qterm = -2.0*Math.sqrt(bigQ);

                droots[0] = qterm*Math.cos(theta/3.0) - aover3;
                droots[1] = qterm*Math.cos((theta + 2.0*Math.PI)/3.0) - aover3;
                droots[2] = qterm*Math.cos((theta - 2.0*Math.PI)/3.0) - aover3;
                roots.add("real");
                roots.add(droots);
            }
            return roots;
    }

          // ROOTS OF A POLYNOMIAL
        // For general details of root searching and a discussion of the rounding errors
        // see Numerical Recipes, The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/

        // Calculate the roots  of a real polynomial
        // initial root estimate is zero [for deg>3]
        // roots are not olished [for deg>3]
        public static ArrayList<Object> polynomial(double[] coeff){
                boolean polish=true;
                double estx = 0.0;
                return RealRoot.polynomial(coeff, polish, estx);
        }

        // Calculate the roots  of a real polynomial
        // initial root estimate is zero [for deg>3]
        // roots are polished [for deg>3]
        public static ArrayList<Object> polynomial(double[] coeff, boolean polish){
                double estx = 0.0;
                return RealRoot.polynomial (coeff, polish, estx);
        }

        // Calculate the roots  of a real polynomial
        // initial root estimate is estx [for deg>3]
        // roots are not polished [for deg>3]
        public static ArrayList<Object> polynomial(double[] coeff, double estx){
                boolean polish=true;
                return RealRoot.polynomial(coeff, polish, estx);
        }

        // Calculate the roots  of a real polynomial
        // initial root estimate is estx [for deg>3]
        // roots are polished [for deg>3]
        public static ArrayList<Object> polynomial (double[] coeff, boolean polish, double estx){

                int nCoeff = coeff.length;
                if(nCoeff<2)throw new IllegalArgumentException("a minimum of two coefficients is required");
                ArrayList<Object> roots = new ArrayList<Object>(nCoeff);
                boolean realRoots = true;

                // check for zero roots
                int nZeros=0;
                int ii=0;
                boolean testZero=true;
                while(testZero){
                    if(coeff[ii]==0.0){
                        nZeros++;
                        ii++;
                    }
                    else{
                        testZero=false;
                    }
                }

                // Repack coefficients
                int nCoeffWz = nCoeff - nZeros;
                double[] coeffWz = new double[nCoeffWz];
                if(nZeros>0){
                    for(int i=0; i<nCoeffWz; i++)coeffWz[i] = coeff[i+nZeros];
                }
                else{
                    for(int i=0; i<nCoeffWz; i++)coeffWz[i] = coeff[i];
                }

                // Calculate non-zero roots
                ArrayList<Object> temp = new ArrayList<Object>(2);
                double[] cdreal = null;
                switch(nCoeffWz){
                        case 0:
                        case 1: break;
                        case 2: temp.add("real");
                                double[] dtemp = {-coeffWz[0]/coeffWz[1]};
                                temp.add(dtemp);
                                break;
                        case 3: temp = RealRoot.quadratic(coeffWz[0],coeffWz[1],coeffWz[2]);
                                if(((String)temp.get(0)).equals("complex"))realRoots = false;
                                break;
                        case 4: temp = RealRoot.cubic(coeffWz[0],coeffWz[1],coeffWz[2], coeffWz[3]);
                                if(((String)temp.get(0)).equals("complex"))realRoots = false;
                                break;
                        default: ComplexPoly cp = new ComplexPoly(coeffWz);
                                Complex[] croots = cp.roots(polish, new Complex(estx, 0.0));
                                cdreal = new double[nCoeffWz-1];
                                int counter = 0;
                                for(int i=0; i<(nCoeffWz-1); i++){
                                    if(croots[i].getImag()/croots[i].getReal()<RealRoot.realTol){
                                        cdreal[i] = croots[i].getReal();
                                        counter++;
                                    }
                                }
                                if(counter==(nCoeffWz-1)){
                                    temp.add("real");
                                    temp.add(cdreal);
                                }
                                else{
                                    temp.add("complex");
                                    temp.add(croots);
                                    realRoots = false;
                                }
                }

                // Pack roots into returned ArrayList
                if(nZeros==0){
                    roots = temp;
                }
                else{
                    if(realRoots){
                        double[] dtemp1 = new double[nCoeff-1];
                        double[] dtemp2 = (double[])temp.get(1);
                        for(int i=0; i<nCoeffWz-1; i++)dtemp1[i] = dtemp2[i];
                        for(int i=0; i<nZeros; i++)dtemp1[i+nCoeffWz-1] = 0.0;
                        roots.add("real");
                        roots.add(dtemp1);
                    }
                    else{
                        Complex[] dtemp1 = Complex.oneDarray(nCoeff-1);
                        Complex[] dtemp2 = (Complex[])temp.get(1);
                        for(int i=0; i<nCoeffWz-1; i++)dtemp1[i] = dtemp2[i];
                        for(int i=0; i<nZeros; i++)dtemp1[i+nCoeffWz-1] = new Complex(0.0, 0.0);
                        roots.add("complex");
                        roots.add(dtemp1);
                    }
                }

                return roots;
        }

        // Reset the criterion for deciding a that a root, calculated as Complex, is real
        // Default option; imag/real <1e-14
        // this method allows thew value of 1e-14 to be reset
        public void resetRealTest(double ratio){
            RealRoot.realTol = ratio;
        }

}