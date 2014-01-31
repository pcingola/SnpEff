/*      Class PropDeriv
*
*       This class contains the constructor to create an instance of
*       a Proportional plus Derivative(PD) controller and the methods
*       needed to use this controller in control loops in the time
*       domain, Laplace transform s domain or the z-transform z domain.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*       Updated: 17 April 2003, 2 May 2005, 2 July 2006, 6 April 2008, 30 October 2009, 7 November 2009
*                24 May 2010
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/PropDeriv.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2006 - 2010  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/


package flanagan.control;

import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.plot.Plot;
import flanagan.plot.PlotGraph;

public class PropDeriv extends BlackBox{
    private double kp = 1.0D;           //  proportional gain
    private double td = 0.0D;           //  derivative time constant
    private double kd = 0.0D;           //  derivative gain

    // Constructor - unit proportional gain, zero derivative gain
    public PropDeriv(){
        super("PropDeriv");
        super.sNumerDeg = 1;
        super.sDenomDeg = 0;
        super.setSnumer(new ComplexPoly(1.0D, 0.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();

    }

    // Set the proportional gain
    public void setKp(double kp){
        this.kp = kp;
        super.sNumer.resetCoeff(0, new Complex(this.kp, 0.0D));
        if(super.sZeros==null)super.sZeros = Complex.oneDarray(1);
        super.sZeros[0].reset(-this.kp/this.kd, 0.0D);
        super.addDeadTimeExtras();
    }

    // Set the derivative gain
    public void setKd(double kd){
        this.kd=kd;
        this.td=kd/this.kp;
        super.sNumer.resetCoeff(1, new Complex(this.kd, 0.0D));
        if(super.sZeros==null)super.sZeros = Complex.oneDarray(1);
        super.sZeros[0].reset(-this.kp/this.kd, 0.0D);
        super.addDeadTimeExtras();
    }

    // Set the derivative time constant
    public void setTd(double td){
        this.td=td;
        this.kd=this.td*this.kp;
        if(super.sZeros==null)super.sZeros = Complex.oneDarray(1);
        super.sNumer.resetCoeff(1, new Complex(this.kd, 0.0D));
        super.sZeros[0].reset(-this.kp/this.kd, 0.0D);
        super.addDeadTimeExtras();
    }

    // Get the proprtional gain
    public double getKp(){
        return this.kp;
    }

    // Get the derivative gain
    public double getKd(){
        return this.kd;
    }

    // Get the derivative time constant
    public double getTd(){
        return this.td;
    }

    // Perform z transform using an already set delta T
    public void zTransform(){
        if(super.deltaT==0.0D)System.out.println("z-transform attempted in PropDeriv with a zero sampling period");
        super.deadTimeWarning("zTransform");
        if(ztransMethod==0){
            this.mapstozAdHoc();
        }
        else{
            super.zNumerDeg = 1;
            super.zDenomDeg = 1;
            super.zNumer = new ComplexPoly(-this.kd, this.kp*super.deltaT + this.kd);
            super.zDenom = new ComplexPoly(0.0D, super.deltaT);
            super.zZeros = Complex.oneDarray(1);
            super.zZeros[0].reset(this.kd/(this.kp*super.deltaT + this.kd),0.0D);
            super.zPoles = Complex.oneDarray(1);
            super.zPoles[0].reset(0.0D, 0.0D);
        }
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.setDeltaT(deltaT);
        super.deadTimeWarning("zTransform");
        this.zTransform();
    }

    // Plots the time course for a step input
    public void stepInput(double stepMag, double finalTime){

        // Calculate time course outputs
        int n = 51;                             // number of points on plot
        double incrT = finalTime/(double)(n-2); // plotting increment
        double cdata[][] = new double [2][n];   // plotting array

        cdata[0][0]=0.0D;
        cdata[1][0]=0.0D;
        for(int i=2; i<n; i++){
            cdata[0][i]=cdata[0][i-1]+incrT;
        }
        double kpterm = this.kp*stepMag;
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

    // Plots the time course for a unit step input
    public void stepInput(double finalTime){
        this.stepInput(1.0D, finalTime);
    }

    // Plots the time course for an nth order ramp input (at^n)
    public void rampInput(double rampGradient, int rampOrder, double finalTime){

        if(rampOrder==0){
            // Check if really a step input (rampOrder, n = 0)
            this.stepInput(rampGradient, finalTime);
        }
        else{
            // Calculate time course outputs
            int n = 50;                             // number of points on plot
            double incrT = finalTime/(double)(n-1); // plotting increment
            double cdata[][] = new double [2][n];   // plotting array
            double sum = 0.0D;                      // integration sum

            cdata[0][0]=0.0D;
            cdata[1][0]=0.0D;
            for(int i=1; i<n; i++){
                cdata[0][i]=cdata[0][i-1]+incrT;
                cdata[1][i] = rampGradient*Math.pow(cdata[0][i],rampOrder-1)*(this.kp*cdata[0][i] + this.kd);
            }
            if(super.deadTime!=0.0D)for(int i=0; i<n; i++)cdata[0][i] += super.deadTime;


            // Plot
            PlotGraph pg = new PlotGraph(cdata);

            pg.setGraphTitle("Ramp (a.t^n) Input Transient:   ramp gradient (a) = "+rampGradient + " ramp order (n) = " + rampOrder);
            pg.setGraphTitle2(this.getName());
            pg.setXaxisLegend("Time");
            pg.setXaxisUnitsName("s");
            pg.setYaxisLegend("Output");
            pg.setPoint(0);
            pg.plot();
        }
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

    //  Get the s-domain output for a given s-value and a given input.
    public Complex getOutputS(Complex sValue, Complex iinput){
        super.sValue=sValue;
        super.inputS=iinput;
        Complex term = this.sValue.times(this.kd);
        term = term.plus(this.kp);
        super.outputS=term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    //  Get the s-domain output for the stored input and  s-value.
    public Complex getOutputS(){
        Complex term = this.sValue.times(this.kd);
        term = term.plus(this.kp);
        super.outputS=term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    //  Calculate the current time domain output for a given input and given time
    //  resets deltaT
    public void calcOutputT(double ttime, double inp){
        super.setInputT(ttime, inp);
        this.calcOutputT();
    }

    //  Get the output for the stored sampled input, time and deltaT.
    public void calcOutputT(){
        // proportional term
        super.outputT[super.sampLen-1] = this.kp*super.inputT[sampLen-1];
        // + derivative term
        super.outputT[super.sampLen-1] += this.kd*(super.inputT[super.sampLen-1]-super.inputT[super.sampLen-2])/super.deltaT;
    }

    // Get the s-domain poles
    public Complex[] getSpoles(){
        System.out.println("PD controller has no s-domain poles");
        return null;
    }
    // Deep copy
    public PropDeriv copy(){
        if(this==null){
            return null;
        }
        else{
            PropDeriv bb = new PropDeriv();
            this.copyBBvariables(bb);

            bb.kp = this.kp;
            bb.td = this.td;
            bb.kd = this.kd;

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}