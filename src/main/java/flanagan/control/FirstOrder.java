/*      Class FirstOrder
*
*       This class contains the constructor to create an instance of
*       a first order process,
*           a.d(output)/dt + b.output  =  c.input
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*       Updated: 20 April 2003, 3 May 2005, 3 April 2006, 2 July 2006, 6 April 2008,
*       2 December 2008, 2-7 November 2009, 23 May 2010, 24 May 2010
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/FirstOrder.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
* Copyright (c) 2002 - 2010  Michael Thomas Flanagan
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

import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.plot.*;

public class FirstOrder extends BlackBox{

    private double aConst = 1.0D;  // a constant in differential equation above
    private double bConst = 1.0D;  // b constant in differential equation above
    private double cConst = 1.0D;  // c constant in differential equation above

    // Constructor
    // Sets all constants to unity
    public FirstOrder(){
        super("FirstOrder");
        super.sPoles = Complex.oneDarray(1);
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Constructor
    // within constants set from argument list
    public FirstOrder(double aa, double bb, double cc){
        super("FirstOrder");
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        super.sPoles = Complex.oneDarray(1);
        super.setSnumer(new ComplexPoly(this.cConst));
        super.setSdenom(new ComplexPoly(this.bConst, this.aConst));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Set coefficients
    public void setCoeff(double aa, double bb, double cc){
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        Complex[] num = Complex.oneDarray(1);
        num[0].reset(this.cConst, 0.0);
        super.sNumer.resetPoly(num);
        Complex[] den = Complex.oneDarray(2);
        den[0].reset(this.bConst, 0.0);
        den[1].reset(this.aConst, 0.0);
        super.sDenom.resetPoly(den);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setA(double aa){
        this.aConst = aa;
        Complex co = new Complex(this.aConst, 0.0);
        super.sDenom.resetCoeff(1, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setB(double bb){
        this.bConst = bb;
        Complex co = new Complex(this.bConst, 0.0);
        super.sDenom.resetCoeff(0, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setC(double cc){
        this.cConst = cc;
        Complex co = new Complex(this.cConst, 0.0);
        super.sNumer.resetCoeff(0, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Get coefficients
    public double getA(){
        return this.aConst;
    }

    public double getB(){
        return this.bConst;
    }

    public double getC(){
        return this.cConst;
    }

    // Get time constant
    public double getTimeConstant(){
        return this.aConst/this.bConst;
    }

    // Calculate the zeros and poles in the s-domain
    protected void calcPolesZerosS(){
        super.sPoles = Complex.oneDarray(1);
        super.sPoles[0].setReal(-bConst/aConst);
        if(super.sNumerSet)super.sNumerScaleFactor = super.sNumer.coeffCopy(0);
        if(super.sDenomSet)super.sDenomScaleFactor = BlackBox.scaleFactor(super.sDenom, super.sPoles);

    }

    // Plots the time course for a step input
    public void stepInput(double stepMag, double finalTime){

        if(this.bConst/this.aConst==0.0){
            // Calculate time course outputs
            int n = 51;                             // number of points on plot
            double incrT = finalTime/(double)(n-2); // plotting increment
            double cdata[][] = new double [2][n];   // plotting array

            cdata[0][0]=0.0D;
            cdata[0][1]=0.0D;
            for(int i=2; i<n; i++){
                cdata[0][i]=cdata[0][i-1]+incrT;
            }
            double kpterm = this.cConst*stepMag/this.bConst;
            cdata[1][0]=0.0D;
            for(int i=1; i<n; i++){
                cdata[1][i] = kpterm;
            }
            if(super.deadTime!=0.0D)for(int i=0; i<n; i++)cdata[0][i] += super.deadTime;

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
            super.stepInput(stepMag, finalTime);
        }
    }


    // Perform z transform using an already set delta T
    public void zTransform(){
        if(super.deltaT==0.0D)System.out.println("z-transform attempted in FirstOrder with a zero sampling period");
        super.deadTimeWarning("zTransform");
        if(ztransMethod==0){
            this.mapstozAdHoc();
        }
        else{
            Complex[] ncoef = null;
            Complex[] dcoef = null;
            switch(this.integMethod){
                // Trapezium rule
                case 0: ncoef = Complex.oneDarray(2);
                        ncoef[0].reset(this.deltaT*this.cConst,0.0D);
                        ncoef[1].reset(this.deltaT*this.cConst,0.0D);
                        super.zNumer=new ComplexPoly(1);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=1;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(this.bConst*this.deltaT - 2*this.aConst,0.0D);
                        dcoef[1].reset(this.bConst*this.deltaT + 2*this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zZeros = Complex.oneDarray(1);
                        super.zZeros[0].reset(-1.0D, 0.0D);
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset((2.0D*this.aConst-super.deltaT*this.bConst)/(2.0D*this.aConst+super.deltaT*this.bConst), 0.0D);
                        break;
                // Backward rectangulr rule
                case 1: ncoef = Complex.oneDarray(2);
                        ncoef[0].reset(0.0D,0.0D);
                        ncoef[1].reset(this.cConst*this.deltaT,0.0D);
                        super.zNumer=new ComplexPoly(1);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=1;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(this.bConst*this.deltaT + this.aConst,0.0D);
                        dcoef[1].reset(this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zZeros = Complex.oneDarray(1);
                        super.zZeros[0].reset(0.0D, 0.0D);
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset(this.aConst/(super.deltaT*this.bConst+this.aConst), 0.0D);
                        break;
                // Foreward rectangular rule
                case 2: ncoef = Complex.oneDarray(1);
                        ncoef[0].reset(this.cConst*this.deltaT,0.0D);
                        super.zNumer=new ComplexPoly(0);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=0;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(-this.aConst,0.0D);
                        dcoef[1].reset(this.bConst*this.deltaT - this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset(this.aConst/(super.deltaT*this.bConst-this.aConst), 0.0D);
                        break;
                default:    System.out.println("Integration method option in FirstOrder must be 0,1 or 2");
                            System.out.println("It was set at "+integMethod);
                            System.out.println("z-transform not performed");
            }
        }
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.setDeltaT(deltaT);
        this.zTransform();
    }

    //  Get the s-domain output for a given s-value and a given input.
    public Complex getOutputS(Complex sValue, Complex iinput){
        super.sValue=sValue;
        super.inputS=iinput;
        return this.getOutputS();
    }

    //  Get the s-domain output for the stored input and  s-value.
    public Complex getOutputS(){
        Complex num = Complex.plusOne();
        num = num.times(this.cConst);
        Complex den = new Complex();
        den = this.sValue.times(this.aConst);
        den = den.plus(this.bConst);
        Complex term = new Complex();
        term = num.over(den);
        super.outputS = term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    //  Calculate the time domain output for a given input and given time
    public double calcOutputT(double time, double input){
        super.setInputT(time, input);
        return calcOutputT();
    }

    // Calculates the time domain output for the stoted input and time
    public double calcOutputT(){
        super.deadTimeWarning("calcOutputT()");
        return super.getCurrentOutputT();
    }

    // Get the s-domain zeros
    public Complex[] getSzeros(){
        System.out.println("This standard first order process (class FirstOrder) has no s-domain zeros");
        return null;
    }


    // Deep copy
    public FirstOrder copy(){
        if(this==null){
            return null;
        }
        else{
            FirstOrder bb = new FirstOrder();
            this.copyBBvariables(bb);

            bb.aConst = this.aConst;
            bb.bConst = this.bConst;
            bb.cConst = this.cConst;

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}