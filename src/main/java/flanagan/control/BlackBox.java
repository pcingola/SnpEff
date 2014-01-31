/*      Class BlackBox
*
*       This class contains the constructor to create an instance of
*       a generalised BlackBox with a single input, single output
*       and a gain.   It contins the methods for obtaining the
*       transfer function in the s-domain and the z-domain.
*
*       This class is the superclass for several sub-classes,
*       e.g. Prop (P controller), PropDeriv (PD controller),
*       PropInt (PI controller), PropIntDeriv (PID controller),
*       FirstOrder, SecondOrder, AtoD (ADC), DtoA (DAC),
*       ZeroOrderHold, DelayLine, OpenLoop (Open Loop Path),
*       of use in control engineering.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*	    Updated: 17 July 2003, 18 May 2005, 6 April 2008, 6 October 2009, 30 October 2009,
*       2-9 November 2009, 20 January 2010, 23-25 May 2010, 3 June 2010, 18 January 2011
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/BlackBox.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
* Copyright (c) 2002 - 2011  Michael Thomas Flanagan
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


package flanagan.control;

import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.complex.*;
import flanagan.plot.Plot;
import flanagan.plot.PlotGraph;
import flanagan.plot.PlotPoleZero;
import flanagan.io.Db;
import flanagan.interpolation.CubicSpline;


public class BlackBox{

    protected int sampLen = 0;                              // Length of array of stored inputs, outputs and time
    protected double[] inputT = null;                       // Array of input signal in the time domain
    protected double[] outputT = null;                      // Array of output signal in the time domain
    protected double[] time = null;                         // Array of time at which inputs were taken (seconds)
    protected double forgetFactor = 1.0D;                   // Forgetting factor, e.g. in exponential forgetting of error values
    protected double deltaT = 0.0D;                         // Sampling time (seconds)
    protected double sampFreq = 0.0D;                       // Sampling frequency (Hz)
    protected Complex inputS = new Complex();               // Input signal in the s-domain
    protected Complex outputS = new Complex();              // Output signal in the s-domain
    protected Complex sValue = new Complex();               // Laplacian s
    protected Complex zValue = new Complex();               // z-transform z
    protected ComplexPoly sNumer = new ComplexPoly(1.0D);   // Transfer function numerator in the s-domain
    protected ComplexPoly sDenom = new ComplexPoly(1.0D);   // Transfer function denominator in the s-domain
    protected ComplexPoly zNumer = new ComplexPoly(1.0D);   // Transfer function numerator in the z-domain
    protected ComplexPoly zDenom = new ComplexPoly(1.0D);   // Transfer function denominator in the z-domain
    protected boolean sNumerSet = false;                    // = true when numerator entered
    protected boolean sDenomSet = false;                    // = true when denominator entered
    protected Complex sNumerScaleFactor = Complex.plusOne();// s-domain numerator/(product of s - zeros)
    protected Complex sDenomScaleFactor = Complex.plusOne();// s-domain denominator/(product of s - poles)
    protected Complex sNumerWorkingFactor = Complex.plusOne();// s-domain numerator/(product of s - zeros) at that point in the program
    protected Complex sDenomWorkingFactor = Complex.plusOne();// s-domain denominator/(product of s - poles) at that point in the program
    protected Complex[] sPoles = null;                      // Poles in the s-domain
    protected Complex[] sZeros = null;                      // Zeros in the s-domain
    protected Complex[] zPoles = null;                      // Poles in the z-domain
    protected Complex[] zZeros = null;                      // Zeros in the z-domain
    protected int sNumerDeg = 0;                            // Degree of transfer function numerator in the s-domain
    protected int sDenomDeg = 0;                            // Degree of transfer function denominator in the s-domain
    protected int zNumerDeg = 0;                            // Degree of transfer function numerator in the z-domain
    protected int zDenomDeg = 0;                            // Degree of transfer function denominator in the z-domain
    protected double deadTime = 0.0D;                       // Time delay between an input and the matching output [in s-domain = exp(-s.deadTime)]
    protected int orderPade = 2;                            // Order(1 to 4)of the pade approximation for exp(-sT)
                                                            //   default option = 2
    protected ComplexPoly sNumerPade = new ComplexPoly(1.0D);    // Transfer function numerator in the s-domain including Pade approximation
    protected ComplexPoly sDenomPade = new ComplexPoly(1.0D);    // Transfer function denominator in the s-domain including Pade approximation
    protected Complex[] sPolesPade = null;                  // Poles in the s-domain including Pade approximation
    protected Complex[] sZerosPade = null;                  // Zeros in the s-domain including Pade approximation
    protected int sNumerDegPade = 0;                        // Degree of transfer function numerator in the s-domain including Pade approximation
    protected int sDenomDegPade = 0;                        // Degree of transfer function denominator in the s-domain including Pade approximation
    protected boolean maptozero = true;                     // if true  infinity s zeros map to zero
                                                            // if false infinity s zeros map to minus one
    protected boolean padeAdded = false;                    // if true  Pade poles and zeros added
                                                            // if false No Pade poles and zeros added
    protected double integrationSum=0.0D;                   // Stored integration sum in numerical integrations
    protected int integMethod = 1;                          // numerical integration method
                                                            //  = 0   Trapezium Rule [default option]
                                                            //  = 1   Backward Rectangular Rule
                                                            //  = 2   Foreward Rectangular Rule
    protected int ztransMethod = 0;                         // z trasform method
                                                            //  = 0   s -> z mapping (ad hoc procedure) from the continuous time domain erived s domain functions
                                                            //  = 1   specific z transform, e.g. of a difference equation
    protected String name = "BlackBox";                     // Superclass or subclass name, e.g. pid, pd, firstorder.
                                                            // user may rename an instance of the superclass or subclass
    protected String fixedName = "BlackBox";                // Super class or subclass permanent name, e.g. pid, pd, firstorder.
                                                            // user must NOT change fixedName in any instance of the superclass or subclass
                                                            // fixedName is used as an identifier in classes such as OpenPath, ClosedLoop
    protected int nPlotPoints = 400;                        // number of points used tp lot response curves, e.g. step input response curve

    protected String[] subclassName = {"BlackBox", "OpenLoop", "ClosedLoop", "Prop", "PropDeriv", "PropInt", "PropIntDeriv", "FirstOrder", "SecondOrder", "Compensator", "LowPassPassive", "HighPassPassive", "Transducer", "DelayLine", "ZeroOrderHold", "AtoD", "DtoA"};
    protected int nSubclasses = subclassName.length;        // number of subclasses plus superclass
    protected int subclassIndex = 0;                        // = 0  BlackBox
                                                            // = 1  OpenLoop
                                                            // = 2  ClosedLoop
                                                            // = 3  Prop
                                                            // = 4  PropDeriv
                                                            // = 5  PropInt
                                                            // = 6  PropIntDeriv
                                                            // = 7  FirstOrder
                                                            // = 8  SecondOrder
                                                            // = 9  Compensator
                                                            // = 10 LowPassPassive
                                                            // = 11 HighPassPassive
                                                            // = 12 Transducer
                                                            // = 13 DelayLine
                                                            // = 14 ZeroOrderHold
                                                            // = 15 AtoD
                                                            // = 16 DtoA

    // Constructor
    public BlackBox(){
    }

    // Constructor with fixedName supplied
    // for use by subclasses
    public BlackBox(String name){
        this.name = name;
        this.fixedName = name;
        this.setSubclassIndex();
    }

    // Set subclass index
    protected void setSubclassIndex(){
        boolean test = true;
        int i = 0;
        while(test){
            if(this.fixedName.equals(subclassName[i])){
                this.subclassIndex = i;
                test = false;
            }
            else{
                i++;
                if(i>=this.nSubclasses){
                    System.out.println("Subclass name, " + this.fixedName + ", not recognised as a recorder subclass");
                    System.out.println("Subclass, " + this.fixedName + ", handled as BlackBox");
                    this.subclassIndex = i;
                    test = false;
                }
            }
        }
    }

    // Set the transfer function numerator in the s-domain
    // Enter as an array of real (double) coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setSnumer(double[] coeff){
        this.sNumerDeg = coeff.length-1;
        this.sNumer = new ComplexPoly(coeff);
        this.sNumerSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();
   }

    // Method to set extra terms to s-domain numerator and denominator and
    // to calculate extra zeros and poles if the dead time is not zero.
    protected void addDeadTimeExtras()
    {
        this.sNumerDegPade = this.sNumerDeg;
        this.sNumerPade = this.sNumer.copy();
        this.sDenomDegPade = this.sDenomDeg;
        this.sDenomPade = this.sDenom.copy();

        if(this.deadTime==0.0D){
            this.transferPolesZeros();
        }
        else{
            this.pade();
        }

    }

    // Set the transfer function numerator in the s-domain
    // Enter as an array of Complex coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setSnumer(Complex[] coeff){
        this.sNumerDeg = coeff.length-1;
        this.sNumer = new ComplexPoly(coeff);
        this.sNumerSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();


    }

    // Set the transfer function numerator in the s-domain
    // Enter as an existing instance of ComplexPoly
    public void setSnumer(ComplexPoly coeff){
        this.sNumerDeg = coeff.getDeg();
        this.sNumer = ComplexPoly.copy(coeff);
        this.sNumerSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();
    }

    // Set the transfer function denominator in the s-domain
    // Enter as an array of real (double) coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setSdenom(double[] coeff){
        this.sDenomDeg = coeff.length-1;
        this.sDenom = new ComplexPoly(coeff);
        this.sDenomSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();
    }

    // Set the transfer function denomonator in the s-domain
    // Enter as an array of Complex coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setSdenom(Complex[] coeff){
        this.sDenomDeg = coeff.length-1;
        this.sDenom = new ComplexPoly(coeff);
        this.sDenomSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();

    }

    // Set the transfer function denominator in the s-domain
    // Enter as an existing instance of ComplexPoly
    public void setSdenom(ComplexPoly coeff){
        this.sDenomDeg = coeff.getDeg();
        this.sDenom = coeff.copy();
        this.sDenomSet = true;
        this.calcPolesZerosS();
        this.addDeadTimeExtras();
    }

    // calculate constant converting product of root terms to the value of the polynomial
    public static Complex scaleFactor(ComplexPoly poly, Complex[] roots){
            int nRoots = roots.length;

            // calculate mean of the poles
            Complex mean = new Complex(0.0D, 0.0);
            for(int i=0; i<nRoots; i++)mean = mean.plus(roots[i]);
            mean = mean.over(nRoots);
            // check that mean != a root; increase mean by 1.5 till != any pole
            boolean test = true;
            int ii=0;
            while(test){
                if(mean.isEqual(roots[ii])){
                    if(mean.isEqual(Complex.zero())){
                        for(int i=0; i<nRoots; i++)mean = mean.plus(roots[i].abs());
                        if(mean.isEqual(Complex.zero()))mean=Complex.plusOne();
                    }
                    else{
                        mean = mean.times(1.5D);
                    }
                    ii=0;
                }
                else{
                    ii++;
                    if(ii>nRoots-1)test = false;
                }
            }

            // calculate product of roots-mean
            Complex product = new Complex(1.0D, 0.0);
            for(int i=0; i<nRoots; i++)product = product.times(mean.minus(roots[i]));

            // evaluate the polynomial at mean value
            Complex eval = poly.evaluate(mean);

            // Calculate scaleFactor
            return eval.over(product);
    }

    // Get numerator scale factor
    public Complex getSnumerScaleFactor(){
        if(this.sNumerScaleFactor==null)this.calcPolesZerosS();
        return this.sNumerScaleFactor;
    }

    // Get denominator scale factor
    public Complex getSdenomScaleFactor(){
        if(this.sDenomScaleFactor==null)this.calcPolesZerosS();
        return this.sDenomScaleFactor;
    }

    // Set the dead time
    public void setDeadTime(double deadtime){
        this.deadTime = deadtime;
        this.pade();
    }

    // Set the dead time and the Pade approximation order
    public void setDeadTime(double deadtime, int orderPade){
        this.deadTime = deadtime;
        if(orderPade>5){
            orderPade=4;
            System.out.println("BlackBox does not support Pade approximations above an order of 4");
            System.out.println("The order has been set to 4");
        }
        if(orderPade<1){
            orderPade=1;
            System.out.println("Pade approximation order was less than 1");
            System.out.println("The order has been set to 1");
        }
        this.orderPade = orderPade;
        this.pade();
    }

    // Set the Pade approximation order
    public void setPadeOrder(int orderPade){
        if(orderPade>5){
            orderPade=4;
            System.out.println("BlackBox does not support Pade approximations above an order of 4");
            System.out.println("The order has been set to 4");
        }
        if(orderPade<1){
            orderPade=2;
            System.out.println("Pade approximation order was less than 1");
            System.out.println("The order has been set to 2");
        }
        this.orderPade = orderPade;
        this.pade();
    }

    // Get the dead time
    public double getDeadTime(){
        return this.deadTime;
    }

    // Get the Pade approximation order
    public int getPadeOrder(){
        return this.orderPade;
    }

    // Resets the s-domain Pade inclusive numerator and denominator adding a Pade approximation
    // Also calculates and stores additional zeros and poles arising from the Pade approximation
    protected void pade(){
        ComplexPoly sNumerExtra = null;
        ComplexPoly sDenomExtra = null;
        Complex[] newZeros = null;
        Complex[] newPoles = null;
        switch(orderPade){
            case 1: this.sNumerDegPade = this.sNumerDeg + 1;
                    this.sDenomDegPade = this.sDenomDeg + 1;
                    this.sNumerPade = new ComplexPoly(sNumerDegPade);
                    this.sDenomPade = new ComplexPoly(sDenomDegPade);
                    sNumerExtra = new ComplexPoly(1.0D,  -this.deadTime/2.0D);
                    sDenomExtra = new ComplexPoly(1.0D,  this.deadTime/2.0D);
                    this.sNumerPade = this.sNumer.times(sNumerExtra);
                    this.sDenomPade = this.sDenom.times(sDenomExtra);
                    newZeros = Complex.oneDarray(1);
                    newZeros[0].reset(2.0/this.deadTime, 0.0D);
                    newPoles = Complex.oneDarray(1);
                    newPoles[0].reset(-2.0/this.deadTime, 0.0D);
                    break;
            case 2: this.sNumerDegPade = this.sNumerDeg + 2;
                    this.sDenomDegPade = this.sDenomDeg + 2;
                    this.sNumerPade = new ComplexPoly(sNumerDegPade);
                    this.sDenomPade = new ComplexPoly(sDenomDegPade);
                    sNumerExtra = new ComplexPoly(1.0D, -this.deadTime/2.0D, Math.pow(this.deadTime, 2)/12.0D);
                    sDenomExtra = new ComplexPoly(1.0D,  this.deadTime/2.0D, Math.pow(this.deadTime, 2)/12.0D);
                    this.sNumerPade = this.sNumer.times(sNumerExtra);
                    this.sDenomPade = this.sDenom.times(sDenomExtra);
                    newZeros = sNumerExtra.rootsNoMessages();
                    newPoles = sDenomExtra.rootsNoMessages();
                    break;
            case 3: this.sNumerDegPade = this.sNumerDeg + 3;
                    this.sDenomDegPade = this.sDenomDeg + 3;
                    this.sNumerPade = new ComplexPoly(sNumerDegPade);
                    this.sDenomPade = new ComplexPoly(sDenomDegPade);
                    double[] termn3 = new double[4];
                    termn3[0] = 1.0D;
                    termn3[1] = -this.deadTime/2.0D;
                    termn3[2] = Math.pow(this.deadTime, 2)/10.0D;
                    termn3[3] = -Math.pow(this.deadTime, 3)/120.0D;
                    sNumerExtra = new ComplexPoly(termn3);
                    this.sNumerPade = this.sNumer.times(sNumerExtra);
                    newZeros = sNumerExtra.rootsNoMessages();
                    double[] termd3 = new double[4];
                    termd3[0] = 1.0D;
                    termd3[1] = this.deadTime/2.0D;
                    termd3[2] = Math.pow(this.deadTime, 2)/10.0D;
                    termd3[3] = Math.pow(this.deadTime, 3)/120.0D;
                    sDenomExtra = new ComplexPoly(termd3);
                    this.sDenomPade = this.sDenom.times(sDenomExtra);
                    newPoles = sDenomExtra.rootsNoMessages();
                    break;
            case 4: this.sNumerDegPade = this.sNumerDeg + 4;
                    this.sDenomDegPade = this.sDenomDeg + 4;
                    this.sNumerPade = new ComplexPoly(sNumerDegPade);
                    this.sDenomPade = new ComplexPoly(sDenomDegPade);
                    double[] termn4 = new double[5];
                    termn4[0] = 1.0D;
                    termn4[1] = -this.deadTime/2.0D;
                    termn4[2] = 3.0D*Math.pow(this.deadTime, 2)/28.0D;
                    termn4[3] = -Math.pow(this.deadTime, 3)/84.0D;
                    termn4[4] = Math.pow(this.deadTime, 4)/1680.0D;
                    sNumerExtra = new ComplexPoly(termn4);
                    this.sNumerPade = this.sNumer.times(sNumerExtra);
                    newZeros = sNumerExtra.rootsNoMessages();
                    double[] termd4 = new double[5];
                    termd4[0] = 1.0D;
                    termd4[1] = this.deadTime/2.0D;
                    termd4[2] = 3.0D*Math.pow(this.deadTime, 2)/28.0D;
                    termd4[3] = Math.pow(this.deadTime, 3)/84.0D;
                    termd4[4] = Math.pow(this.deadTime, 4)/1680.0D;
                    sDenomExtra = new ComplexPoly(termd4);
                    this.sDenomPade = this.sDenom.times(sDenomExtra);
                    newPoles = sDenomExtra.rootsNoMessages();
                    break;
            default: this.orderPade = 2;
                    this.sNumerDegPade = this.sNumerDeg + 2;
                    this.sDenomDegPade = this.sDenomDeg + 2;
                    this.sNumerPade = new ComplexPoly(sNumerDegPade);
                    this.sDenomPade = new ComplexPoly(sDenomDegPade);
                    sNumerExtra = new ComplexPoly(1.0D, -this.deadTime/2.0D, Math.pow(this.deadTime, 2)/12.0D);
                    sDenomExtra = new ComplexPoly(1.0D,  this.deadTime/2.0D, Math.pow(this.deadTime, 2)/12.0D);
                    this.sNumerPade = this.sNumer.times(sNumerExtra);
                    this.sDenomPade = this.sDenom.times(sDenomExtra);
                    newZeros = sNumerExtra.rootsNoMessages();
                    newPoles = sDenomExtra.rootsNoMessages();
                    break;
        }

        // store zeros and poles arising from the Pade term
        if(this.sNumerPade!=null  && this.sNumerDegPade>0){
            sZerosPade = Complex.oneDarray(sNumerDegPade);
            for(int i=0; i<sNumerDeg; i++){
                sZerosPade[i] = sZeros[i].copy();
            }
            for(int i=0; i<this.orderPade; i++){
                sZerosPade[i+sNumerDeg] = newZeros[i].copy();
            }
        }

        if(this.sDenomPade!=null && this.sDenomDegPade>0){
            sPolesPade = Complex.oneDarray(sDenomDegPade);
            for(int i=0; i<sDenomDeg; i++){
                sPolesPade[i] = sPoles[i].copy();
            }
            for(int i=0; i<this.orderPade; i++){
                sPolesPade[i+sDenomDeg] = newPoles[i].copy();
            }
        }
        this.zeroPoleCancellation();
        this.padeAdded = true;
    }

    // Copies s-domain poles and zeros from the s-domain arrays to the s-domain Pade arrays
    // used when deadTime is zero
    protected void transferPolesZeros(){

        this.sNumerDegPade = this.sNumerDeg;
        this.sNumerPade = this.sNumer.copy();
        if(this.sNumerDeg>0 && this.sZeros!=null){
            this.sZerosPade = Complex.oneDarray(this.sNumerDeg);
            for(int i=0; i<this.sNumerDeg; i++)this.sZerosPade[i] = this.sZeros[i].copy();
        }

        this.sDenomDegPade = this.sDenomDeg;
        this.sDenomPade = this.sDenom.copy();
        if(this.sDenomDeg>0 && this.sPoles!=null){
            this.sPolesPade = Complex.oneDarray(this.sDenomDeg);
            for(int i=0; i<this.sDenomDeg; i++)this.sPolesPade[i] = this.sPoles[i].copy();
        }
        this.zeroPoleCancellation();
        this.padeAdded = true;

    }

    // Get the Pade approximation order
    public int orderPade(){
        return this.orderPade;
    }

    // Warning message if dead time greater than sampling period
    protected boolean deadTimeWarning(String method){
        boolean warning = false;    // warning true if dead time is greater than the sampling period
                                    // false if not
        if(this.deadTime>this.deltaT){
            System.out.println(this.name+"."+method+": The dead time is greater than the sampling period");
            System.out.println("Dead time:       "+this.deadTime);
            System.out.println("Sampling period: "+this.deltaT);
            System.out.println("!!! The results of this program may not be physically meaningful !!!");
            warning = true;
        }
        return warning;
    }

    // Perform z transform for a given delta T
    // Uses maptozAdHoc in this class but may be overridden in a subclass
    public void zTransform(double deltat){
        this.mapstozAdHoc(deltat);
    }

    // Perform z transform using an already set delta T
    // Uses maptozAdHoc in this class but may be overridden in a subclass
    public void zTransform(){
        this.mapstozAdHoc();
    }

    // Map s-plane zeros and poles of the transfer function onto the z-plane using the ad-hoc method
    //  for a given sampling period.
    //  References:
    //  John Dorsey, Continuous and Discrete Control Systems, pp 490-491, McGraw Hill (2002)
    //  J R Leigh, Applied Digital Control, pp 78-80, Prentice-Hall (1985)
    public void mapstozAdHoc(double deltaT){
        this.deltaT = deltaT;
        this.mapstozAdHoc();
    }

    // Map s-plane zeros and poles of the transfer function onto the z-plane using the ad-hoc method
    //  for an already set sampling period.
    //  References:
    //  John Dorsey, Continuous and Discrete Control Systems, pp 490-491, McGraw Hill (2002)
    //  J R Leigh, Applied Digital Control, pp 78-80, Prentice-Hall (1985)
    public void mapstozAdHoc(){

        this.deadTimeWarning("mapstozAdHoc");
        if(!this.padeAdded)this.transferPolesZeros();

        // Calculate z-poles
        this.zDenomDeg = this.sDenomDegPade;
        ComplexPoly root = new ComplexPoly(1);
        this.zDenom = new ComplexPoly(this.zDenomDeg);
        if(zDenomDeg>0){
            this.zPoles = Complex.oneDarray(this.zDenomDeg);
            for(int i=0; i<this.zDenomDeg; i++){
                zPoles[i]=Complex.exp(this.sPolesPade[i].times(this.deltaT));
            }
            this.zDenom = ComplexPoly.rootsToPoly(zPoles);
        }

        // Calculate z-zeros
        // number of zeros from infinity poles
        int infZeros = this.sDenomDegPade;
        // check that total zeros does not exceed total poles
        if(infZeros+this.sNumerDegPade>this.sDenomDegPade)infZeros=this.sDenomDegPade-this.sNumerDegPade;
        // total number of zeros
        this.zNumerDeg = this.sNumerDegPade + infZeros;
        this.zNumer = new ComplexPoly(zNumerDeg);
        this.zZeros = Complex.oneDarray(zNumerDeg);
        // zero values
        if(this.zNumerDeg>0){
            for(int i=0; i<this.sNumerDegPade; i++){
                zZeros[i]=Complex.exp(sZerosPade[i].times(this.deltaT));
            }
            if(infZeros>0){
                if(maptozero){
                    for(int i=this.sNumerDegPade; i<this.zNumerDeg; i++){
                        zZeros[i]=Complex.zero();
                    }
                }
                else{
                    for(int i=this.sNumerDegPade; i<this.zNumerDeg; i++){
                        zZeros[i]=Complex.minusOne();
                    }
                }
            }
            this.zNumer = ComplexPoly.rootsToPoly(this.zZeros);
        }

        // Match s and z steady state gains
        this.sValue=Complex.zero();
        this.zValue=Complex.plusOne();
        boolean testzeros = true;
        while(testzeros){
            testzeros = false;
            if(this.sDenomDegPade>0){
                for(int i=0; i<this.sDenomDegPade; i++){
                    if(this.sPolesPade[i].truncate(3).equals(this.sValue.truncate(3)))testzeros=true;
                }
            }
            if(!testzeros && this.sNumerDegPade>0){
                for(int i=0; i<this.sDenomDegPade; i++){
                    if(this.sZerosPade[i].truncate(3).equals(this.sValue.truncate(3)))testzeros=true;
                }
            }
            if(!testzeros && this.zDenomDeg>0){
                for(int i=0; i<this.zDenomDeg; i++){
                    if(this.zPoles[i].truncate(3).equals(this.zValue.truncate(3)))testzeros=true;
                }
            }
            if(!testzeros && this.zNumerDeg>0){
                for(int i=0; i<this.zDenomDeg; i++){
                    if(this.zZeros[i].truncate(3).equals(this.zValue.truncate(3)))testzeros=true;
                }
            }
            if(testzeros){
                this.sValue = this.sValue.plus(Complex.plusJay()).truncate(3);
                this.zValue = Complex.exp(this.sValue.times(this.deltaT).truncate(3));
            }
        }
        Complex gs = this.evalTransFunctS(this.sValue);
        Complex gz = this.evalTransFunctZ(this.zValue);
        Complex constant = gs.over(gz);
        ComplexPoly constantPoly = new ComplexPoly(constant);
        this.zNumer = this.zNumer.times(constantPoly);
    }

    // Set the map infinity zeros to zero or -1 option
    // maptozero:   if true  infinity s zeros map to zero
    //              if false infinity s zeros map to minus one
    // default value = false
    public void setMaptozero(boolean maptozero){
        this.maptozero = maptozero;
    }

    // Set the transfer function numerator in the z-domain
    // Enter as an array of real (double) coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setZnumer(double[] coeff){
        this.zNumerDeg = coeff.length-1;
        this.zNumer = new ComplexPoly(coeff);
        this.zZeros = this.zNumer.rootsNoMessages();
    }

    // Set the transfer function numerator in the z-domain
    // Enter as an array of Complex coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setZnumer(Complex[] coeff){
        this.zNumerDeg = coeff.length-1;
        this.zNumer = new ComplexPoly(coeff);
        this.zZeros = this.zNumer.rootsNoMessages();
    }

    // Set the transfer function numerator in the z-domain
    // Enter as an existing instance of ComplexPoly
    public void setZnumer(ComplexPoly coeff){
        this.zNumerDeg = coeff.getDeg();
        this.zNumer = ComplexPoly.copy(coeff);
        this.zZeros = this.zNumer.rootsNoMessages();
    }

    // Set the transfer function denominator in the z-domain
    // Enter as an array of real (double) coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setZdenom(double[] coeff){
        this.zDenomDeg = coeff.length-1;
        this.zDenom = new ComplexPoly(coeff);
        this.zPoles = this.zDenom.rootsNoMessages();
    }

    // Set the transfer function denomonatot in the z-domain
    // Enter as an array of Complex coefficients of the polynomial a + bs +c.s.s + d.s.s.s + ....
    public void setZdenom(Complex[] coeff){
        this.zDenomDeg = coeff.length-1;
        this.zDenom = new ComplexPoly(coeff);
        this.zPoles = this.zDenom.rootsNoMessages();
    }

    // Set the transfer function denominator in the z-domain
    // Enter as an existing instance of ComplexPoly
    public void setZdenom(ComplexPoly coeff){
        this.zDenomDeg = coeff.getDeg();
        this.zDenom = ComplexPoly.copy(coeff);
        this.zPoles = this.zDenom.rootsNoMessages();
    }

    // Set the sampling period
    public void setDeltaT(double deltaT ){
        if(this.deltaT==0.0){
            this.deltaT=deltaT;
            this.sampFreq=1.0D/this.deltaT;
            this.deadTimeWarning("setDeltaT");
        }
        else{

            String question = "BlackBox setDeltaT: Do you wish to replace the deltaT value, " + this.deltaT + " with " + deltaT;
            if(Db.yesNo(question)){
                this.deltaT=deltaT;
                this.sampFreq=1.0D/this.deltaT;
                this.deadTimeWarning("setDeltaT");
                if(this.time!=null){
                    int holdS = this.sampLen;
                    this.sampLen = (int)Math.round(time[this.sampLen-1]/this.deltaT);
                    double[] holdT = Conv.copy(time);
                    double[] holdI = Conv.copy(inputT);
                    this.time = new double[this.sampLen];
                    this.inputT = new double[this.sampLen];
                    CubicSpline cs = new CubicSpline(holdT, holdI);
                    this.time[0] = holdT[0];
                    this.inputT[0] = holdI[0];
                    for(int i=1; i<this.sampLen-1; i++){
                        this.time[i] = this.time[i-1] = this.deltaT;
                        this.inputT[i] = cs.interpolate(this.time[i]);
                    }
                    this.time[sampLen-1] = holdT[holdS];
                    this.inputT[sampLen-1] = holdI[holdS];
                }
            }
        }
    }

    // Set the forgetting factor
    public void setForgetFactor(double forget){
        this.forgetFactor = forget;
    }

    // Set the sampling frequency
    public void setSampFreq(double sfreq ){
        this.sampFreq=sfreq;
        this.setDeltaT(1.0D/sfreq);
    }

    // Set the Laplacian s value (s - Complex)
    public void setS(Complex s){
        this.sValue = Complex.copy(s);
    }

    // Set the Laplacian s value (s - real + imaginary parts)
    public void setS(double sr, double si){
        this.sValue.reset(sr,si);
    }

    // Set the Laplacian s value (s - imag, real = 0.0)
    public void setS(double si){
        this.sValue.reset(0.0D, si);
    }

    // Set the z-transform z value (z - Complex)
    public void setZ(Complex z){
        this.zValue = Complex.copy(z);
    }

    // Set the z-transform z value (z - real + imaginary parts)
    public void setZ(double zr, double zi){
        this.zValue.reset(zr,zi);
    }

    // Set the z transform method
    // 0 = s to z mapping (ad hoc procedure)
    // 1 = specific z transform, e.g. z transform of a difference equation
    public void setZtransformMethod(int ztransMethod){
        if(ztransMethod<0 || ztransMethod>1){
            System.out.println("z transform method option number " + ztransMethod + " not recognised");
            System.out.println("z tr methodansform option number set in BlackBox to the default value of 0 (s -> z ad hoc mapping)");
            this.integMethod = 0;
            }
        else{
            this.ztransMethod = ztransMethod;
        }
    }

    // Set the integration method  [number option]
    // 0 = trapezium, 1 = Backward rectangular, 2 = Foreward rectangular
    public void setIntegrateOption(int integMethod){
        if(integMethod<0 || integMethod>2){
            System.out.println("integration method option number " + integMethod + " not recognised");
            System.out.println("integration method option number set in BlackBox to the default value of 0 (trapezium rule)");
            this.integMethod = 0;
            }
        else{
            this.integMethod = integMethod;
        }
    }

    // Set the integration method  [String option]
    // trapezium; trapezium, tutin.  Backward rectangular; back  backward. Foreward rectangular; foreward, fore
    // Continuous time equivalent: continuous, cont
    public void setIntegrateOption(String integMethodS){
        if(integMethodS.equals("trapezium") || integMethodS.equals("Trapezium") ||integMethodS.equals("tutin") || integMethodS.equals("Tutin")){
            this.integMethod = 0;
        }
        else{
            if(integMethodS.equals("backward") || integMethodS.equals("Backward") ||integMethodS.equals("back") || integMethodS.equals("Back")){
                this.integMethod = 1;
            }
            else{
                if(integMethodS.equals("foreward") || integMethodS.equals("Foreward") ||integMethodS.equals("fore") || integMethodS.equals("Fore")){
                    this.integMethod = 2;
                }
                else{
                    System.out.println("integration method option  " + integMethodS + " not recognised");
                    System.out.println("integration method option number set in PID to the default value of 0 (trapezium rule)");
                    this.integMethod = 0;
                }
            }
        }
    }

    // Reset the length of the arrays storing the times, time domain inputs and time domain outputs
    public void setSampleLength(int samplen){
        if(samplen==0)throw new IllegalArgumentException("Entered sample length must be greater than zero");
        if(samplen==1)samplen=2;
        if(this.sampLen==0){
            this.sampLen = samplen;
            this.time = new double[samplen];
            this.inputT = new double[samplen];
            this.outputT = new double[samplen];
        }
        else{
            String question = "BlackBox setSampleLength: Do you wish to replace the sample length, " + this.sampLen + " with " + samplen;
            if(Db.yesNo(question)){
                int holdS = this.sampLen;
                this.sampLen=samplen;
                if(this.time!=null){
                    this.deltaT = this.time[holdS-1]/(samplen-1);
                    double[] holdT = Conv.copy(time);
                    double[] holdI = Conv.copy(inputT);
                    this.time = new double[this.sampLen];
                    this.inputT = new double[this.sampLen];
                    CubicSpline cs = new CubicSpline(holdT, holdI);
                    this.time[0] = holdT[0];
                    this.inputT[0] = holdI[0];
                    for(int i=1; i<this.sampLen-1; i++){
                        this.time[i] = this.time[i-1] = this.deltaT;
                        this.inputT[i] = cs.interpolate(this.time[i]);
                    }
                    this.time[sampLen-1] = holdT[holdS];
                    this.inputT[sampLen-1] = holdI[holdS];
                }
            }
        }
    }

    // Reset the name of the black box
    public void setName(String name){
        this.name=name;
    }

    // Enter a single current time domain time and input value
    public void setInputT(double ttime, double inputt){
        if(this.deltaT==0.0){
            this.time = new double[2];
            this.time[0] = 0.0;
            this.time[1] = ttime;
            this.inputT = new double[2];
            this.inputT[0] = inputt;
            this.inputT[1] = inputt;
            this.outputT = new double[2];
            this.sampLen = 2;
            // this.deltaT = ttime;
            // this.sampFreq = 1.0/this.deltaT;
        }
        else{
            double delta = this.deltaT;
            this.sampLen = (int)Math.round(ttime/delta);
            this.deltaT = ttime/sampLen;
            if(!Fmath.isEqualWithinLimits(this.deltaT, delta, delta*1e-3)){
                System.out.println("BlackBox setInputT method; deltaT has been reset from " + delta + " to " + this.deltaT);
            }
            this.sampFreq = 1.0/this.deltaT;
            this.time = new double[this.sampLen];
            this.time[this.sampLen-1]=ttime;
            this.inputT = new double[this.sampLen];
            this.inputT[this.sampLen-1]=inputt;
            this.outputT = new double[this.sampLen];
            for(int i=sampLen-2; i>0; i--){
                this.time[i]= this.time[i+1]-deltaT;
                this.inputT[i]=inputt;
            }
            this.time[0]=0.0;
            this.inputT[0]=inputt;
        }
    }

    // Enter a set of current time domain times and input values
    public void setInputT(double[] ttime, double[] inputt){
        int samplen = ttime.length;
        if(samplen!=inputt.length)throw new IllegalArgumentException("time and input arrays are of different lengths: " + samplen + ", " + inputt.length);
        if(samplen==1){
            this.setInputT(ttime[0], inputt[0]);
        }
        else{
            this.sampLen = samplen;
            this.time = ttime;
            this.inputT = inputt;
            this.outputT = new double[this.sampLen];
            this.deltaT = ttime[this.sampLen]/(this.sampLen - 1);
            this.sampFreq = 1.0/this.deltaT;
        }
    }



    // Reset s-domain input
    public void setInputS(Complex input){
        this.inputS=input;
    }

    // Reset all inputs, outputs and times to zero
    public void resetZero(){
        for(int i=0; i<this.sampLen-1; i++){
            this.outputT[i] = 0.0D;
            this.inputT[i]  = 0.0D;
            this.time[i]    = 0.0D;
        }
        this.outputS = Complex.zero();
        this.inputS  = Complex.zero();
        this.deltaT = 0.0;
        this.sampLen = 0;
    }

    // Calculate the zeros and poles in the s-domain
    // does not include Pade approximation term
    protected void calcPolesZerosS(){
        if(this.sNumer!=null){
            if(this.sNumer.getDeg()>0)this.sZeros = this.sNumer.rootsNoMessages();
            if(this.sZeros!=null){
                this.sNumerScaleFactor = BlackBox.scaleFactor(this.sNumer, this.sZeros);
            }
            else{
                this.sNumerScaleFactor = this.sNumer.coeffCopy(0);
            }
        }

        if(this.sDenom!=null){
            if(this.sDenom.getDeg()>0)this.sPoles = this.sDenom.rootsNoMessages();
            if(this.sPoles!=null){
                this.sDenomScaleFactor = BlackBox.scaleFactor(this.sDenom, this.sPoles);
            }
            else{
                this.sDenomScaleFactor = this.sDenom.coeffCopy(0);
            }
        }
        if(this.sNumerPade!=null){
            if(this.sNumerPade.getDeg()>0)this.sZerosPade = this.sNumerPade.rootsNoMessages();
        }
        if(this.sDenomPade!=null){
            if(this.sDenomPade.getDeg()>0)this.sPolesPade = this.sDenomPade.rootsNoMessages();
        }
    }

    // Eliminates identical poles and zeros in the s-domain
    protected void zeroPoleCancellation(){
        boolean check = false;
        boolean testI = true;
        boolean testJ = true;
        int i=0;
        int j=0;

        if(this.sNumerDegPade==0 || this.sDenomDegPade==0)testI=false;
        if(this.sZerosPade==null || this.sPolesPade==null)testI=false;
        while(testI){
            j=0;
            while(testJ){
                if(this.sZerosPade[i].isEqual(this.sPolesPade[j])){
                    for(int k=j+1; k<this.sDenomDegPade; k++)this.sPolesPade[k-1] = this.sPolesPade[k].copy();
                    this.sDenomDegPade--;
                    for(int k=i+1; k<this.sNumerDegPade; k++)this.sZerosPade[k-1] = this.sZerosPade[k].copy();
                    this.sNumerDegPade--;
                    check = true;
                    testJ=false;
                    i--;
                }
                else{
                    j++;
                    if(j>this.sDenomDegPade-1)testJ=false;
                }
            }
            i++;
            if(i>this.sNumerDegPade-1)testI=false;
        }
        if(check){
            if(this.sNumerDegPade==0){
                this.sNumerPade = new ComplexPoly(1.0D);
            }
            else{
                Complex[] holdn = Complex.oneDarray(sNumerDegPade);
                for(int ii=0; ii<sNumerDegPade; ii++)holdn[i] = this.sZerosPade[ii].copy();
                this.sZerosPade = holdn;
                this.sNumerPade = ComplexPoly.rootsToPoly(this.sZerosPade);
            }
            if(this.sDenomDegPade==0){
                this.sDenomPade = new ComplexPoly(1.0D);
            }
            else{
                Complex[] holdd = Complex.oneDarray(sDenomDegPade);
                for(int ii=0; ii<sDenomDegPade; ii++)holdd[i] = this.sPolesPade[ii].copy();
                this.sPolesPade = holdd;
                this.sDenomPade = ComplexPoly.rootsToPoly(this.sPolesPade);
            }
        }

        check = false;
        testI = true;
        testJ = true;
        i=0;
        j=0;

        if(this.sNumerDeg==0 || this.sDenomDeg==0)testI=false;
        if(this.sZeros==null || this.sPoles==null)testI=false;
        while(testI){
            j=0;
            while(testJ){
                if(this.sZeros[i].isEqual(this.sPoles[j])){
                    for(int k=j+1; k<this.sDenomDeg; k++)this.sPoles[k-1] = this.sPoles[k].copy();
                    this.sDenomDeg--;
                    for(int k=i+1; k<this.sNumerDeg; k++)this.sZeros[k-1] = this.sZeros[k].copy();
                    this.sNumerDeg--;
                    check = true;
                    testJ=false;
                    i--;
                }
                else{
                    j++;
                    if(j>this.sDenomDeg-1)testJ=false;
                }
            }
            i++;
            if(i>this.sNumerDeg-1)testI=false;
        }
        if(check){
            if(this.sNumerDeg==0){
                this.sNumer = new ComplexPoly(1.0D);
            }
            else{
                Complex[] holdn = Complex.oneDarray(sNumerDeg);
                for(int ii=0; ii<sNumerDeg; ii++)holdn[i] = this.sZeros[ii].copy();
                this.sZeros = holdn;
                this.sNumer = ComplexPoly.rootsToPoly(this.sZeros);
                this.sNumerWorkingFactor = this.sNumerScaleFactor;
            }
            if(this.sDenomDeg==0){
                this.sDenom = new ComplexPoly(1.0D);
            }
            else{
                Complex[] holdd = Complex.oneDarray(sDenomDeg);
                for(int ii=0; ii<sDenomDeg; ii++)holdd[i] = this.sPoles[ii].copy();
                this.sPoles = holdd;
                this.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
                this.sDenomWorkingFactor = this.sDenomScaleFactor;
            }
        }
    }

    // Get steadty state value for a unit step input
    public double getSeadyStateValue(){
        Complex num = this.sNumer.evaluate(Complex.zero());
        Complex den = this.sDenom.evaluate(Complex.zero());
        Complex ssc = num.over(den);
        double ssdr = ssc.getReal();
        double ssdi = ssc.getImag();
        if(Math.abs(ssdi)>Math.abs(ssdr)*0.01){
            System.out.println("method getSteadyStateValue: The imaginary part, " + ssdi + ", is greater than 1 per cent of the the real part, " + ssdr);
            System.out.println("Magnitude has  been returned");
        }
        return ssc.abs();
    }

     // Get steadty state value for a step input of magnitude, mag
    public double getSeadyStateValue(double mag){
        Complex num = this.sNumer.evaluate(Complex.zero());
        Complex den = this.sDenom.evaluate(Complex.zero());
        Complex ssc = num.over(den);
        double ssdr = ssc.getReal();
        double ssdi = ssc.getImag();
        if(Math.abs(ssdi)>Math.abs(ssdr)*0.01){
            System.out.println("method getSteadyStateValue: The imaginary part, " + ssdi + ", is greater than 1 per cent of the the real part, " + ssdr);
            System.out.println("Magnitude has  been returned");
        }
        return mag*ssc.abs();
    }



    // Evaluate the s-domain tranfer function for the present value of s
    // deadtime evaluated as exponential term
    public Complex evalTransFunctS(){
        if(!this.padeAdded)this.transferPolesZeros();
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return num.over(den).times(lagterm);
    }

    // Evaluate the s-domain tranfer function for a given Complex value of s
    public Complex evalTransFunctS(Complex sValue){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue = Complex.copy(sValue);
        Complex num = this.sNumer.evaluate(sValue);
        Complex den = this.sDenom.evaluate(sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return num.over(den).times(lagterm);
    }

    // Evaluate the s-domain tranfer function for a sine wave input at a given frequency (s^-1)
    public Complex evalTransFunctS(double freq){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue.reset(0.0D, 2.0D*Math.PI*freq);
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return num.over(den).times(lagterm);
    }

    // Evaluate the magnitude of the s-domain tranfer function for the present value of s
    public double evalMagTransFunctS(){
        if(!this.padeAdded)this.transferPolesZeros();
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).abs();
    }

    // Evaluate the magnitude of the s-domain tranfer function for a given Complex value of s
    public double evalMagTransFunctS(Complex sValue){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue = Complex.copy(sValue);
        Complex num = this.sNumer.evaluate(sValue);
        Complex den = this.sDenom.evaluate(sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).abs();
        }

    // Evaluate the magnitude of the s-domain tranfer function for a sine wave input at a given frequency (s^-1)
    public double evalMagTransFunctS(double freq){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue.reset(0.0D, 2.0D*Math.PI*freq);
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).abs();
    }

    // Evaluate the phase of the s-domain tranfer function for the present value of s
    public double evalPhaseTransFunctS(){
        if(!this.padeAdded)this.transferPolesZeros();
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).arg();
    }

    // Evaluate the phase of the s-domain tranfer function for a given Complex value of s
    public double evalPhaseTransFunctS(Complex sValue){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue = Complex.copy(sValue);
        Complex num = this.sNumer.evaluate(sValue);
        Complex den = this.sDenom.evaluate(sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).arg();
    }

    // Evaluate the phase of the s-domain tranfer function for a sine wave input at a given frequency (s^-1)
    public double evalPhaseTransFunctS(double freq){
        if(!this.padeAdded)this.transferPolesZeros();
        this.sValue.reset(0.0D, 2.0D*Math.PI*freq);
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        Complex lagterm = Complex.plusOne();
        if(this.deadTime!=0)lagterm = Complex.exp(this.sValue.times(-this.deadTime));
        return (num.over(den).times(lagterm)).arg();
    }

    // Evaluate the z-domain tranfer function for the present value of z
    public Complex evalTransFunctZ(){
        Complex num = this.zNumer.evaluate(this.zValue);
        Complex den = this.zDenom.evaluate(this.zValue);
        return num.over(den);
    }

    // Evaluate the z-domain tranfer function for a given Complex value of z
    public Complex evalTransFunctZ(Complex zValue){
        this.zValue = Complex.copy(zValue);
        Complex num = this.zNumer.evaluate(zValue);
        Complex den = this.zDenom.evaluate(zValue);
        return num.over(den);
    }

    // Evaluate the magnitude of the z-domain tranfer function for the present value of z
    public double evalMagTransFunctZ(){
        Complex num = this.zNumer.evaluate(this.zValue);
        Complex den = this.zDenom.evaluate(this.zValue);
        return num.over(den).abs();
    }

    // Evaluate the magnitude of the z-domain tranfer function for a given Complex value of z
    public double evalMagTransFunctZ(Complex zValue){
        this.zValue = Complex.copy(zValue);
        Complex num = this.zNumer.evaluate(zValue);
        Complex den = this.zDenom.evaluate(zValue);
        return num.over(den).abs();
    }

    // Evaluate the phase of the z-domain tranfer function for the present value of z
    public double evalPhaseTransFunctZ(){
        Complex num = this.zNumer.evaluate(this.zValue);
        Complex den = this.zDenom.evaluate(this.zValue);
        return num.over(den).arg();
    }

    // Evaluate the phase of the z-domain tranfer function for a given Complex value of z
    public double evalPhaseTransFunctZ(Complex zValue){
        this.zValue = Complex.copy(zValue);
        Complex num = this.zNumer.evaluate(zValue);
        Complex den = this.zDenom.evaluate(zValue);
        return num.over(den).arg();
    }

    // Get the integration method option
    public int getIntegMethod(){
        return this.integMethod;
    }

    // Get the z transform method option
    public int getZtransformMethod(){
        return this.ztransMethod;
    }

    // Get the length of the time, input (time domain) and output (time domain) arrays
    public int getSampleLength(){
        return this.sampLen;
    }

    // Get the forgetting factor
    public double getForgetFactor(){
        return this.forgetFactor;
    }

    //  Get the current time
    public double getCurrentTime(){
        return this.time[this.sampLen-1];
    }

    //  Get the  time array
    public double[] getTime(){
        return this.time;
    }

    //  Get the current time domain input
    public double getCurrentInputT(){
        return this.inputT[this.sampLen-1];
    }

    //  Get the time domain input array
    public double[] getInputT(){
        return this.inputT;
    }

    //  Get the s-domain input
    public Complex getInputS(){
        return this.inputS;
    }

    // Get the sampling period
    public double getDeltaT(){
        return this.deltaT;
    }

    // Get the sampling frequency
    public double getSampFreq(){
        return this.sampFreq;
    }

    //  Get the Laplacian s value
    public Complex getS(){
        return this.sValue;
    }

    //  Get the z-transform z value
    public Complex getZ(){
        return this.zValue;
    }

    //  Get the degree of the original s-domain numerator polynomial
    public int getSnumerDeg(){
        return this.sNumerDeg;
    }

    //  Get the degree of the s-domain numerator polynomial  after any dead time Pade approximation added
    public int getSnumerPadeDeg(){
        return this.sNumerDegPade;
    }

    //  Get the degree of the original s-domain denominator polynomial
    public int getSdenomDeg(){
        return this.sDenomDeg;
    }

    //  Get the degree of the s-domain denominator polynomial  after any dead time Pade approximation added
    public int getSdenomPadeDeg(){
        return this.sDenomDegPade;
    }

    //  Get the original s-domain numerator polynomial
    public ComplexPoly getSnumer(){
        return this.sNumer.times(this.sNumerWorkingFactor);
    }

    //  Get the s-domain numerator polynomial after any dead time Pade approximation added
    public ComplexPoly getSnumerPade(){
        return this.sNumerPade.times(this.sNumerWorkingFactor);
    }

    //  Get the original s-domain denominator polynomial
    public ComplexPoly getSdenom(){
        return this.sDenom.times(this.sDenomWorkingFactor);
    }

    //  Get the s-domain denominator polynomial after any dead time Pade approximation added
    public ComplexPoly getSdenomPade(){
        return this.sDenomPade.times(this.sDenomWorkingFactor);
    }

    //  Get the degree of the z-domain numerator polynomial
    public int getZnumerDeg(){
        return this.zNumerDeg;
    }

    //  Get the degree of the z-domain denominator polynomial
    public int getZdenomDeg(){
        return this.zDenomDeg;
    }

    //  Get the z-domain numerator polynomial
    public ComplexPoly getZnumer(){
        return this.zNumer;
    }

    //  Get the z-domain denominator polynomial
    public ComplexPoly getZdenom(){
        return this.zDenom;
    }

    //  Get the s-domain zeros without any Pade zeros
    public Complex[] getZerosS(){
        if(this.sZeros==null)this.calcPolesZerosS();
        if(this.sZeros==null){
                System.out.println("Method BlackBox.getZerosS:");
                System.out.println("There are either no s-domain zeros for this transfer function");
                System.out.println("or the s-domain numerator polynomial has not been set");
                System.out.println("null returned");
                return null;
        }
        else{
            return this.sZeros;
        }

    }

    //  Get the s-domain zeros plusany Pade zeros
    public Complex[] getZerosPadeS(){
        if(this.sZeros==null)this.calcPolesZerosS();
        if(!this.padeAdded)this.transferPolesZeros();
        if(this.sZerosPade==null){
                System.out.println("Method BlackBox.getZerosPadeS:");
                System.out.println("There are either no s-domain zeros for this transfer function");
                System.out.println("or the s-domain numerator polynomial has not been set");
                System.out.println("null returned");
                return null;
        }
        else{
            return this.sZerosPade;
        }
    }

    //  Get the s-domain poles without any Pade poles
    public Complex[] getPolesS(){
        if(this.sPoles==null)this.calcPolesZerosS();
        if(this.sPoles==null){
                System.out.println("Method BlackBox.getPolesS:");
                System.out.println("There are either no s-domain poles for this transfer function");
                System.out.println("or the s-domain denominator polynomial has not been set");
                System.out.println("null returned");
                return null;
        }
        else{
                return this.sPoles;
        }
    }

    //  Get the s-domain poles plus any Pade poles
    public Complex[] getPolesPadeS(){
        if(this.sPoles==null)this.calcPolesZerosS();
        if(!this.padeAdded)this.transferPolesZeros();
        if(this.sPolesPade==null){
                System.out.println("Method BlackBox.getPolesPadeS:");
                System.out.println("There are either no s-domain poles for this transfer function");
                System.out.println("or the s-domain denominator polynomial has not been set");
                System.out.println("null returned");
                return null;
        }
        else{
                return this.sPolesPade;
        }
    }


    //  Get the z-domain zeros
    public Complex[] getZerosZ(){
        if(this.zZeros==null){
            System.out.println("Method BlackBox.getZerosZ:");
            System.out.println("There are either no z-domain zeros for this transfer function");
            System.out.println("or the z-domain numerator polynomial has not been set");
            System.out.println("null returned");
            return null;
        }
        else{
            return this.zZeros;
        }
    }

    //  Get the z-domain poles
    public Complex[] getPolesZ(){
        if(this.zPoles==null){
            System.out.println("Method BlackBox.getPolesZ:");
            System.out.println("There are either no z-domain poles for this transfer function");
            System.out.println("or the z-domain denominator polynomial has not been set");
            System.out.println("null returned");
            return null;
        }
        else{
            return this.zPoles;
        }
    }

    // Get the map infinity zeros to zero or -1 option
    // maptozero:   if true  infinity s zeros map to zero
    //              if false infinity s zeros map to minus one
    public boolean getMaptozero(){
        return this.maptozero;
    }

    // Get the name of the black box
    public String getName(){
        return this.name;
    }

    // Plot the poles and zeros of the BlackBox transfer function in the s-domain
    // Excludes any Pade poles and zeros
    public void plotPoleZeroS(){
        if(this.sNumer==null)throw new IllegalArgumentException("s domain numerator has not been set");
        if(this.sDenom==null)throw new IllegalArgumentException("s domain denominator has not been set");
        PlotPoleZero ppz = new PlotPoleZero(this.sNumer, this.sDenom);
        ppz.setS();
        ppz.pzPlot(this.name);
    }

    // Plot the poles and zeros of the BlackBox transfer function in the s-domain
    // Includes Pade poles and zeros
    public void plotPoleZeroPadeS(){
        if(!this.padeAdded)this.transferPolesZeros();
        if(this.sNumerPade==null)throw new IllegalArgumentException("s domain numerator has not been set");
        if(this.sDenomPade==null)throw new IllegalArgumentException("s domain denominator has not been set");
        PlotPoleZero ppz = new PlotPoleZero(this.sNumerPade, this.sDenomPade);
        ppz.setS();
        ppz.pzPlot(this.name);
    }

    // Plot the poles and zeros of the BlackBox transfer function in the z-domain
    public void plotPoleZeroZ(){
        PlotPoleZero ppz = new PlotPoleZero(this.zNumer, this.zDenom);
        if(this.zNumer==null)throw new IllegalArgumentException("z domain numerator has not been set");
        if(this.zDenom==null)throw new IllegalArgumentException("z domain denominator has not been set");
        ppz.setZ();
        ppz.pzPlot(this.name);
    }

    // Bode plots for the magnitude and phase of the s-domain transfer function
    public void plotBode(double lowFreq, double highFreq){
        if(!this.padeAdded)this.transferPolesZeros();
        int nPoints = 100;
        double[][] cdata = new double[2][nPoints];
        double[] logFreqArray = new double[nPoints+1];
        double logLow = Fmath.log10(2.0D*Math.PI*lowFreq);
        double logHigh = Fmath.log10(2.0D*Math.PI*highFreq);
        double incr = (logHigh - logLow)/((double)nPoints-1.0D);
        double freqArray = lowFreq;
        logFreqArray[0]=logLow;
        for(int i=0; i<nPoints; i++){
            freqArray=Math.pow(10,logFreqArray[i]);
            cdata[0][i]=logFreqArray[i];
            cdata[1][i]=20.0D*Fmath.log10(this.evalMagTransFunctS(freqArray/(2.0*Math.PI)));
            logFreqArray[i+1]=logFreqArray[i]+incr;
        }

        PlotGraph pgmag = new PlotGraph(cdata);
        pgmag.setGraphTitle("Bode Plot = magnitude versus log10[radial frequency]");
        pgmag.setGraphTitle2(this.name);
        pgmag.setXaxisLegend("Log10[radial frequency]");
        pgmag.setYaxisLegend("Magnitude[Transfer Function]");
        pgmag.setYaxisUnitsName("dB");
        pgmag.setPoint(0);
        pgmag.setLine(3);
        pgmag.plot();
        for(int i=0; i<nPoints; i++){
            freqArray=Math.pow(10,logFreqArray[i]);
            cdata[0][i]=logFreqArray[i];
            cdata[1][i]=this.evalPhaseTransFunctS(freqArray)*180.0D/Math.PI;
        }
        PlotGraph pgphase = new PlotGraph(cdata);
        pgphase.setGraphTitle("Bode Plot = phase versus log10[radial frequency]");
        pgphase.setGraphTitle2(this.name);
        pgphase.setXaxisLegend("Log10[radial frequency]");
        pgphase.setYaxisLegend("Phase[Transfer Function]");
        pgphase.setYaxisUnitsName("degrees");
        pgphase.setPoint(0);
        pgmag.setLine(3);
        pgphase.plot();

    }

    //  Get the current time domain output for a given input and given time
    //  resets deltaT
    public double getCurrentOutputT(double ttime, double inp){
        this.setInputT(ttime, inp);
        return this.getCurrentOutputT();
    }

    //  Get the current time domain output for the stored input
    public double getCurrentOutputT(){
        if(!this.padeAdded)this.transferPolesZeros();

        ComplexPoly numerI = this.sNumerPade.times(new Complex(this.inputT[this.sampLen-1], 0.0));
        Complex[] polyC = {Complex.zero(), Complex.plusOne()};
        ComplexPoly polyH = new ComplexPoly(polyC);
        ComplexPoly denomI = this.sDenomPade.times(polyH);

        Complex[][] coeffT = BlackBox.inverseTransform(numerI, denomI, this.sNumerWorkingFactor, this.sDenomScaleFactor);

        Complex tempc = Complex.zero();
        for(int j=0; j<coeffT[0].length; j++){
            tempc.plusEquals(BlackBox.timeTerm(this.time[this.sampLen-1], coeffT[0][j], coeffT[1][j], coeffT[2][j]));
        }
        double outReal = tempc.getReal();
        double outImag = tempc.getImag();
        double temp;
        boolean outTest=true;
        if(outImag==0.0D)outTest=false;
        if(outTest){
            temp=Math.max(Math.abs(outReal),Math.abs(outImag));
            if(Math.abs((outReal-outImag)/temp)>1.e-5){
                outTest=false;
            }
            else{
                System.out.println("output in Blackbox.getCurrentOutputT() has a significant imaginary part");
                System.out.println("time = " + this.time[this.sampLen-1] + "    real = " + outReal + "   imag = " + outImag);
                System.out.println("Output equated to the real part");
            }
        }
        //for(int i=0; i<this.sampLen-2; i++)this.outputT[i]=this.outputT[i+1];
        this.outputT[this.sampLen-1] = outReal;
        return this.outputT[this.sampLen-1];
    }

    //  Get the time domain output array
    public double[] getOutputT(){
        return this.outputT;
    }

    //  Get the s-domain output for the stored input and s value.
    public Complex getOutputS(){
        if(!this.padeAdded)this.transferPolesZeros();
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        this.outputS =  num.over(den).times(this.inputS);
        if(this.deadTime!=0)this.outputS = this.outputS.times(Complex.exp(this.sValue.times(-this.deadTime)));
        return this.outputS;
    }

    //  Get the s-domain output for a given s value and  input.
    public Complex getOutputS(Complex svalue, Complex inputs){
        if(!this.padeAdded)this.transferPolesZeros();
        this.inputS = inputs;
        this.sValue = svalue;
        Complex num = this.sNumer.evaluate(this.sValue);
        Complex den = this.sDenom.evaluate(this.sValue);
        this.outputS =  num.over(den).times(this.inputS);
        if(this.deadTime!=0)this.outputS = this.outputS.times(Complex.exp(this.sValue.times(-this.deadTime)));
        return this.outputS;
    }

    // Reset the number of points used in plotting a response curve
    public void setNplotPoints(int nPoints){
        this.nPlotPoints = nPoints;
    }

    // Return the number of points used in plotting a response curve
    public int getNplotPoints(){
        return this.nPlotPoints;
    }

    // Plots the time course for an impulse input
    public void impulseInput(double impulseMag, double finalTime){
        if(!this.padeAdded)this.transferPolesZeros();

        // Multiply transfer function by impulse magnitude (impulseMag)
        ComplexPoly impulseN = new ComplexPoly(0);
        impulseN.resetCoeff(0, Complex.plusOne().times(impulseMag));
        ComplexPoly numerT = this.sNumerPade.times(impulseN);
        ComplexPoly denomT = this.sDenomPade.copy();
        String graphtitle1 = "Impulse Input Transient:   Impulse magnitude = "+impulseMag;
        String graphtitle2 = this.getName();
        BlackBox.transientResponse(this.nPlotPoints, finalTime, this.deadTime, numerT, denomT, graphtitle1, graphtitle2, this.sNumerWorkingFactor, this.sDenomScaleFactor);
    }

    // Plots the time course for a unit impulse input
    public void impulseInput(double finalTime){
        this.impulseInput(1.0D, finalTime);
    }

    // Plots the time course for a step input
    public void stepInput(double stepMag, double finalTime){
        Complex sNumer0 = this.sNumerPade.coeffCopy(0);
        Complex sDenom0 = this.sDenomPade.coeffCopy(0);
        boolean test0 = false;
        if(Complex.isReal(sNumer0) && Complex.isReal(sDenom0))test0=true;

        if(sNumerDeg==0 && sDenomDeg==0 && test0){
            // Calculate time course outputs
            int n = 51;                             // number of points on plot
            double incrT = finalTime/(double)(n-2); // plotting increment
            double cdata[][] = new double [2][n];   // plotting array

            cdata[0][0]=0.0D;
            cdata[0][1]=0.0D;
            for(int i=2; i<n; i++){
                cdata[0][i]=cdata[0][i-1]+incrT;
            }
            double kpterm = sNumer0.getReal()*stepMag/sDenom0.getReal();
            cdata[1][0]=0.0D;
            for(int i=1; i<n; i++){
                cdata[1][i] = kpterm;
            }
            if(this.deadTime!=0.0D)for(int i=0; i<n; i++)cdata[0][i] += this.deadTime;

            // Plot
            PlotGraph pg = new PlotGraph(cdata);

            pg.setGraphTitle("Step Input Transient:   Step magnitude = "+stepMag);
            pg.setGraphTitle2(this.getName());
            pg.setXaxisLegend("Time");
            pg.setXaxisUnitsName("s");
            pg.setYaxisLegend("Output");
            pg.setPoint(0);
            pg.setLine(3);
            pg.plot();

        }
        else{
            if(!this.padeAdded)this.transferPolesZeros();
            // Multiply transfer function by step magnitude (stepMag)/s
            ComplexPoly numerT = this.sNumer.times(stepMag);
            Complex[] polyC = {Complex.zero(), Complex.plusOne()};
            ComplexPoly polyH = new ComplexPoly(polyC);
            ComplexPoly denomT = this.sDenom.times(polyH);
            String graphtitle1 = "Step Input Transient:   Step magnitude = "+stepMag;
            String graphtitle2 = this.getName();

            BlackBox.transientResponse(this.nPlotPoints, finalTime, this.deadTime, numerT, denomT, graphtitle1, graphtitle2, this.sNumerWorkingFactor, this.sDenomScaleFactor);
        }
    }

    // Plots the time course for a unit step input
    public void stepInput(double finalTime){
        this.stepInput(1.0D, finalTime);
    }

    // Plots the time course for an nth order ramp input (a.t^n)
    public void rampInput(double rampGradient, int rampOrder, double finalTime){
        if(!this.padeAdded)this.transferPolesZeros();

        // Multiply transfer function by ramp input (rampGradient)(rampOrder!)/s^(ramporder+1)
        ComplexPoly numerT = this.sNumer.times(rampGradient*Fmath.factorial(rampOrder));
        Complex[] polyC = Complex.oneDarray(rampOrder+1);
        for(int i=0; i<rampOrder; i++)polyC[i] = Complex.zero();
        polyC[rampOrder] = Complex.plusOne();
        ComplexPoly polyH = new ComplexPoly(polyC);
        ComplexPoly denomT = this.sDenom.times(polyH);
        String graphtitle1 = "";
        if(rampGradient!=1.0D){
            if(rampOrder!=1){
                graphtitle1 += "nth order ramp (at^n) input transient:   a = "+rampGradient+"    n = "+rampOrder;
            }
            else{
                graphtitle1 += "First order ramp (at) input transient:   a = "+rampGradient;
            }
        }
        else{
            if(rampOrder!=1){
                graphtitle1 += "Unit ramp (t) input transient";
            }
            else{
                graphtitle1 += "nth order ramp (t^n) input transient:   n = "+rampOrder;
            }
        }
        String graphtitle2 = this.getName();
        BlackBox.transientResponse(this.nPlotPoints, finalTime, this.deadTime, numerT, denomT, graphtitle1, graphtitle2, this.sNumerWorkingFactor, this.sDenomScaleFactor);
    }

    // Plots the time course for an nth order ramp input (t^n)
    public void rampInput(int rampOrder, double finalTime){
        double rampGradient = 1.0D;
        this.rampInput(rampGradient, rampOrder, finalTime);
    }

    // Plots the time course for a first order ramp input (at)
    public void rampInput(double rampGradient, double finalTime){
        int rampOrder = 1;
        this.rampInput(rampGradient, rampOrder, finalTime);
    }

    // Plots the time course for a unit ramp input (t)
    public void rampInput(double finalTime){
        double rampGradient = 1.0D;
        int rampOrder = 1;
        this.rampInput(rampGradient, rampOrder, finalTime);
    }

    // Plots the time course for a given transfer function from time t = zero for a quiescent system
    // Denominator scaling factor calculated
    public static void transientResponse(int nPoints, double finalTime, double deadTime, ComplexPoly numerT, ComplexPoly denomT, String graphtitle1, String graphtitle2){
        Complex[] roots = denomT.rootsNoMessages();
        Complex magDenom = BlackBox.scaleFactor(denomT, roots);
        Complex magNumer = Complex.plusOne();
        BlackBox.transientResponse(nPoints, finalTime, deadTime, numerT, denomT, graphtitle1, graphtitle2, magNumer, magDenom);
    }

    // Plots the time course for a given transfer function from time t = zero for a quiescent system
    // Denominator scaling factor provided
    public static void transientResponse(int nPoints, double finalTime, double deadTime, ComplexPoly numerT, ComplexPoly denomT, String graphtitle1, String graphtitle2, Complex magN, Complex magD){
        // Obtain coefficients and constants of an partial fraction expansion


        Complex[][] coeffT = BlackBox.inverseTransform(numerT, denomT, magN, magD);

        // Calculate time course outputs
        int m = denomT.getDeg();                        // number of Aexp(-at) terms
        double incrT = finalTime/(double)(nPoints-1);   // plotting increment
        double cdata[][] = new double [2][nPoints];     // plotting array
        double temp = 0.0D;                             // working variable
        Complex tempc = new Complex();                  // working variable
        double outReal = 0.0D;                          // real part of output
        double outImag = 0.0D;                          // imaginary part of output (should be zero)
        boolean outTest = true;                         // false if outImag=zero

        cdata[0][0]=0.0D;
        for(int i=1; i<nPoints; i++){
            cdata[0][i]=cdata[0][i-1]+incrT;
        }
        for(int i=0; i<nPoints; i++){
            outTest= true;
            tempc = Complex.zero();
            for(int j=0; j<m; j++){
                    tempc.plusEquals(BlackBox.timeTerm(cdata[0][i], coeffT[0][j], coeffT[1][j], coeffT[2][j]));
             }
            outReal = tempc.getReal();
            outImag = tempc.getImag();
            if(outImag==0.0D)outTest=false;
            if(outTest){
                temp=Math.max(Math.abs(outReal),Math.abs(outImag));
                 if(Math.abs((outReal-outImag)/temp)>1.e-5){
                    outTest=false;
                }
                else{
                    System.out.println("output in Blackbox.stepInput has a significant imaginary part");
                    System.out.println("time = " + cdata[0][i] + "    real = " + outReal + "   imag = " + outImag);
                    System.out.println("Output equated to the real part");
                }
            }
            cdata[1][i]=outReal;
            cdata[0][i]+=deadTime;
        }

        // Plot
        PlotGraph pg = new PlotGraph(cdata);

        pg.setGraphTitle(graphtitle1);
        pg.setGraphTitle2(graphtitle2);
        pg.setXaxisLegend("Time");
        pg.setXaxisUnitsName("s");
        pg.setYaxisLegend("Output");
        pg.setPoint(0);
        pg.setLine(3);
        pg.setNoYoffset(true);
        if(deadTime<(cdata[0][nPoints-1]-cdata[0][0]))pg.setNoXoffset(true);
        pg.setXlowFac(0.0D);
        pg.setYlowFac(0.0D);
        pg.plot();
    }


    // Returns the output term for a given time, coefficient, constant and power
    // for output = A.time^(n-1).exp(constant*time)/(n-1)!
    // Complex arguments and return
    public static Complex timeTerm(double ttime, Complex coeff, Complex constant, Complex power){
        Complex ret = new Complex();
        int n = (int)power.getReal() - 1;
        ret = coeff.times(Math.pow(ttime,n));
        ret = ret.over(Fmath.factorial(n));
        ret = ret.times(Complex.exp(constant.times(ttime)));
        return ret;
    }

    // Returns the output term for a given time, coefficient, constant and power
    // for output = A.time^(n-1).exp(constant*time)/(n-1)!
    // Real arguments and return
    public static double timeTerm(double ttime, double coeff, double constant, int power){
        int n = power - 1;
        double ret = coeff*Math.pow(ttime,n);
        ret = ret/Fmath.factorial(n);
        ret = ret*(Math.exp(constant*ttime));
        return ret;
    }

        // Returns the output term for a given time, coefficient, constant and power
    // for output = A.time^(n-1).exp(constant*time)/(n-1)!
    // Real arguments and return - all double
    public static double timeTerm(double ttime, double coeff, double constant, double power){
        double n = power - 1;
        double ret = coeff*Math.pow(ttime,n);
        ret = ret/Fmath.factorial(n);
        ret = ret*(Math.exp(constant*ttime));
        return ret;
    }

    // Returns the coefficients A, the constant a and the power n  in the f(A.exp(-at),n) term for the
    // the inverse Laplace transform of a complex polynolial divided
    // by a complex polynomial expanded as partial fractions
    // A and a are returnd as a 2 x n double array were n is the number of terms
    // in the partial fraction.  the first row contains the A values, the second the a values
    // denominator scaling factor calculated
    public static double[][] inverseTransformToReal(ComplexPoly numer, ComplexPoly denom){
        Complex[][] com = inverseTransform(numer, denom);
        int n = com[0].length;
        double[][] ret = new double[3][n];
        for(int i=0; i<n;i++){
            ret[0][i] = com[0][i].getReal();
            if(Math.abs((ret[0][i]-com[0][i].getImag())/ret[0][i])>1.e-5){
                System.out.println("BlackBox inverseTransformToReal coefficient A[" + i + "] has a significant imaginary part: " + com[0][i]);
                System.out.println("A equated to the real part");
                System.out.println("inverseTransform method may be more appropriate");
            }
            ret[1][i] = com[1][i].getReal();
            if(Math.abs((ret[1][i]-com[1][i].getImag())/ret[1][i])>1.e-5){
                System.out.println("BlackBox inverseTransformToReal coefficient a[" + i + "] has a significant imaginary part: " + com[1][i]);
                System.out.println("a equated to the real part");
                System.out.println("inverseTransform method may be more appropriate");
            }
            ret[2][i] = com[2][i].getReal();
        }
        return ret;
    }



    // Returns the coefficients A, the constant a and the power n  in the f(A.exp(-at),n) term for the
    // the inverse Laplace transform of a complex polynolial divided
    // by a complex polynomial expanded as partial fractions
    // A and a are returnd as a 3 x n Complex array were n is the number of terms
    // in the partial fraction.  The first row contains the A values, the second the a values and the third the power of the denominator
    // denominator scaling factor calculated
    public static Complex[][] inverseTransform(ComplexPoly numer, ComplexPoly denom){
        Complex[] roots = denom.rootsNoMessages();
        Complex magDenom = BlackBox.scaleFactor(denom, roots);
        Complex magNumer = Complex.plusOne();
        return inverseTransform(numer, denom, magNumer, magDenom);
    }

    // Returns the coefficients A, the constant a and the power n  in the f(A.exp(-at),n) term for the
    // the inverse Laplace transform of a complex polynolial divided
    // by a complex polynomial expanded as partial fractions
    // A and a are returnd as a 3 x n Complex array were n is the number of terms
    // in the partial fraction.  The first row contains the A values, the second the a values and the third the power of the denominator
    // denominator scaling factor provided
    public static Complex[][] inverseTransform(ComplexPoly numer, ComplexPoly denom, Complex magNumer, Complex magDenom){

        int polesN = denom.getDeg();    // number of poles
        int zerosN = numer.getDeg();    // numer of zeros
        if(zerosN>=polesN)throw new IllegalArgumentException("The degree of the numerator is equal to or greater than the degree of the denominator");
        Complex[][] ret = Complex.twoDarray(3, polesN); // array for returning coefficients, constants and powers

        // Special case: input = A/(B + C)s
        if(polesN==1 && zerosN==0){
            Complex num  = numer.coeffCopy(0);
            Complex den0  = denom.coeffCopy(0);
            Complex den1  = denom.coeffCopy(1);
            ret[0][0] = num.over(den1);
            ret[1][0] = Complex.minusOne().times(den0.over(den1));
            ret[2][0] = new Complex(1.0, 0.0);
            return ret;
        }

        int nDifferentRoots = polesN;                   // number of roots of different values
        int nSetsIdenticalRoots = 0;                    // number of sets roots of identical value
        Complex[] poles = denom.rootsNoMessages();                // poles array
        int[] polePower = new int[polesN];              // power, n,  of each (s - root)^n term
        boolean[] poleSet = new boolean[polesN];        // true if root has been identified as either equal to another root
        int[] poleIdent = new int[polesN];              // same integer for identical (s-root) terms; integer = index of first case of that root
        int[] poleHighestPower = new int[polesN];       // highest pole power for that set of identical poles
        boolean[] termSet = new boolean[polesN];        // false if n in (s-root)^n is greater than 1 and less than maximum value of n for that root
        double identicalRootLimit = 1.0e-2;             // roots treated as identical if equal to one part in identicalRootLimit
        int[] numberInSet = new int[polesN];            // number of poles indentical to this pole including this pole

        // Find identical roots within identicalRootLimit and assign power n [ (s-a)^n] to all roots
        int power = 0;
        Complex identPoleAverage = new Complex();
        int lastPowerIndex=0;
        for(int i=0; i<polesN; i++)poleSet[i]=false;
        for(int i=0; i<polesN; i++)termSet[i]=true;
        for(int i=0; i<polesN; i++){
            if(!poleSet[i]){
                power=1;
                polePower[i]=1;
                poleHighestPower[i]= 1;
                poleIdent[i]=i;
                numberInSet[i]=1;
                identPoleAverage = poles[i];
                for(int j=i+1; j<polesN; j++){
                     if(!poleSet[j]){
                         if(poles[i].isEqualWithinLimits(poles[j],identicalRootLimit)){
                            poleIdent[j]=i;
                            polePower[j]=++power;
                            poleSet[j]=true;
                            poleSet[i]=true;
                            termSet[j]=false;
                            termSet[i]=false;
                            lastPowerIndex=j;
                            nDifferentRoots--;
                            identPoleAverage = identPoleAverage.plus(poles[j]);
                        }
                        else{
                            poleIdent[j]=j;
                            polePower[j]=1;
                        }
                    }
                }
            }

            if(poleSet[i]){
                nDifferentRoots--;
                nSetsIdenticalRoots++;

                // Set termSet to true if pole is recurring term with the highest power
                termSet[lastPowerIndex]=true;

                // Replace roots within identicalRootLimit with their average value
                identPoleAverage = identPoleAverage.over(power);
                for(int j=0; j<polesN; j++){
                    if(poleSet[j] && poleIdent[j]==i){
                        poles[j] = identPoleAverage;
                        poleHighestPower[i] = power;
                        numberInSet[j] = power;
                    }
                }
            }
        }

        // Calculate pole average
        Complex poleAverage = Complex.zero();
        Complex absPoleAverage = Complex.zero();
        for(int i=0; i<polesN; i++){
            poleAverage = poleAverage.plus(poles[i]);
            absPoleAverage = absPoleAverage.plus(poles[i].abs());
        }
        poleAverage = poleAverage.over(polesN);
        absPoleAverage = absPoleAverage.over(polesN);

        // Calculate pole substitute for identical substitution values
        Complex poleSubstitute = poleAverage;
        if(poleSubstitute.isZero())poleSubstitute = absPoleAverage;
        if(poleSubstitute.isZero())poleSubstitute = Complex.plusOne();

        // Choose initial set of s substitution values
        Complex[] subValues = Complex.oneDarray(polesN);
        boolean[] subSet = new boolean[polesN];
        for(int i=0; i<polesN; i++)subSet[i] = false;

        Complex[] shifts = null;
        Complex delta = new Complex(1.7, 0.0);   // root separation factor
        for(int i=0; i<polesN; i++)subValues[i] = poles[i].copy();
        int currentNumberInSet = 0;
        if(nSetsIdenticalRoots>0){
            for(int i=0; i<polesN; i++){
                if(numberInSet[i]>1  && !subSet[i]){
                    currentNumberInSet = numberInSet[i];
                    shifts = Complex.oneDarray(numberInSet[i]);
                    int centre = numberInSet[i]/2;
                    if(Fmath.isEven(numberInSet[i])){
                        for(int j=0; j<centre; j++){
                            shifts[centre+j] = delta.times((double)(j+1));
                            shifts[centre-1-j] = shifts[centre+j].times(-1.0);
                        }
                    }
                    else{
                        shifts[centre] = Complex.zero();
                        for(int j=0; j<centre; j++){
                            shifts[centre+1+j] = delta.times((double)(j+1));
                            shifts[centre-1-j] = shifts[centre+j].times(-1.0);
                        }
                    }
                    int kk = 0;
                    for(int j=0; j<polesN; j++){
                        if(!subSet[j] && numberInSet[j]==currentNumberInSet){
                            Complex incr = poles[j];
                            if(incr.isZero())incr = poleSubstitute;
                            subValues[j] = shifts[kk].times(incr);
                            subSet[j] = true;
                            kk++;

                        }
                    }
                }
            }
        }

        // Check for identical and very close substitution values
        boolean testii = true;
        int ii = 0;
        int nAttempts = 0;
        while(testii){
            int jj = ii + 1;
            boolean testjj = true;
            while(testjj){
                if(subValues[ii].isEqualWithinLimits(subValues[jj],identicalRootLimit)){
                    subValues[ii] = subValues[ii].plus(poleSubstitute.times((double)nAttempts));
                    nAttempts++;
                    ii=0;
                    testjj = false;
                    if(nAttempts>1000000)throw new IllegalArgumentException("a non repeating set of substitution values could not be foumd");
                }
                else{
                    jj++;
                }
                if(jj>=polesN)testjj = false;
            }
            ii++;
            if(ii>=polesN-1)testii = false;
        }

        // Set up the linear equations
        // Create vector and matrix arrays
        Complex[][] mat = Complex.twoDarray(polesN, polesN);
        Complex[] vec = Complex.oneDarray(polesN);

        // Fill vector
        for(int i=0; i<polesN; i++){
            if(zerosN>0){
                vec[i] = numer.evaluate(subValues[i]);
            }
            else{
                vec[i] = numer.coeffCopy(0);
            }
        }

        // fill matrix
        for(int i=0; i<polesN; i++){
            for(int j=0; j<polesN; j++){
                 Complex denomTerm = Complex.plusOne();
                int powerD = 0;
                for(int k=0; k<polesN; k++){
                    if(termSet[k]){
                        if(j!=k){
                            if(polePower[k]==1){
                                denomTerm = denomTerm.times(subValues[i].minus(poles[k]));
                            }
                            else{
                                denomTerm = denomTerm.times(Complex.pow(subValues[i].minus(poles[k]), polePower[k]));
                            }
                        }
                        else{
                            if(polePower[j]<poleHighestPower[j]){
                                powerD = poleHighestPower[j] - polePower[j];
                                if(powerD==1){
                                    denomTerm = denomTerm.times(subValues[i].minus(poles[k]));
                                }
                                else{
                                    if(powerD!=0){
                                        denomTerm = denomTerm.times(Complex.pow(subValues[i].minus(poles[k]), powerD));
                                    }
                                }
                            }
                        }
                    }
                }
                mat[i][j] = denomTerm;
            }

        }


        // Solve linear equations
        ComplexMatrix cmat = new ComplexMatrix(mat);
        Complex[] terms = cmat.solveLinearSet(vec);

        // fill ret for returning
        for(int i=0; i<polesN; i++){
          ret[0][i]=terms[i].times(magNumer).over(magDenom);
          ret[1][i]=poles[i];
          ret[2][i].reset(polePower[i],0.0D);
        }
        return ret;

    }

    // Deep copy
    public BlackBox copy(){
        if(this==null){
            return null;
        }
        else{
            BlackBox bb = new BlackBox();
            this.copyBBvariables(bb);
            return bb;
        }
    }

    // Copies BlackBox variables
    public void copyBBvariables(BlackBox bb){

            bb.sampLen = this.sampLen;
            bb.inputT = Conv.copy(this.inputT);
            bb.outputT = Conv.copy(this.outputT);
            bb.time = Conv.copy(this.time);
            bb.forgetFactor = this.forgetFactor;
            bb.deltaT = this.deltaT;
            bb.sampFreq = this.sampFreq;
            bb.inputS = this.inputS.copy();
            bb.outputS = this.outputS.copy();
            bb.sValue = this.sValue.copy();
            bb.zValue = this.zValue.copy();
            bb.sNumer = this.sNumer.copy();
            bb.sDenom = this.sDenom.copy();
            bb.zNumer = this.zNumer.copy();
            bb.zDenom = this.zDenom.copy();
            bb.sNumerSet = this.sNumerSet;
            bb.sDenomSet = this.sDenomSet;
            bb.sNumerScaleFactor = this.sNumerScaleFactor;
            bb.sDenomScaleFactor = this.sDenomScaleFactor;
            bb.sPoles = Complex.copy(this.sPoles);
            bb.sZeros = Complex.copy(this.sZeros);
            bb.zPoles = Complex.copy(this.zPoles);
            bb.zZeros = Complex.copy(this.zZeros);
            bb.sNumerDeg = this.sNumerDeg;
            bb.sDenomDeg = this.sDenomDeg;
            bb.zNumerDeg = this.zNumerDeg;
            bb.zDenomDeg = this.zDenomDeg;
            bb.deadTime = this.deadTime;
            bb.orderPade = this.orderPade;
            bb.sNumerPade = this.sNumerPade.copy();
            bb.sDenomPade = this.sDenomPade.copy();
            bb.sPolesPade = Complex.copy(this.sPolesPade);
            bb.sZerosPade = Complex.copy(this.sZerosPade);
            bb.sNumerDegPade = this.sNumerDegPade;
            bb.sDenomDegPade = this.sDenomDegPade;
            bb.maptozero = this.maptozero;
            bb.padeAdded = this.padeAdded;
            bb.integrationSum = this.integrationSum;
            bb.integMethod = this.integMethod;
            bb.ztransMethod = this.ztransMethod;
            bb.name = this.name;
            bb.fixedName = this.fixedName;
            bb.nPlotPoints = this.nPlotPoints;

    }


    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}

