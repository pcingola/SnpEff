/*      Class Prop
*
*       This class contains the constructor to create an instance of
*       a Proportional gain controller and the methods
*       needed to use this controller in control loops in the time
*       domain, Laplace transform s domain or the z-transform z domain.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*       Updated: 20 April 2003, 3 May 2005, July 2006, 6 April 2008, 30 October 2009, 7 November 2009
*                24 May 2010
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/Prop.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2010  Michael Thomas Flanagan
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

public class Prop extends BlackBox{
    private double kp = 1.0D;           //  proportional gain

    // Constructor - unit proportional gain
    public Prop(){
        super("Prop");
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
        super.sNumerScaleFactor = Complex.plusOne();
        super.sDenomScaleFactor = Complex.plusOne();
    }

    // Constructor - set P gain
    public Prop(double kp){
        super("Prop");
        this.kp=kp;
        super.setSnumer(new ComplexPoly(this.kp));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
        super.sNumerScaleFactor = new Complex(kp, 0.0);
        super.sDenomScaleFactor = Complex.plusOne();
    }

    // Set the proportional gain
    public void setKp(double kp){
        this.kp=kp;
        Complex num = new Complex(this.kp, 0.0D);
        super.sNumer.resetCoeff(0, num);
        super.addDeadTimeExtras();
        super.sNumerScaleFactor = new Complex(kp, 0.0);
    }

    // Get the proprtional gain
    public double getKp(){
        return this.kp;
    }

    // Perform z transform using an already set delta T
    public void zTransform(){
        super.zNumerDeg = 0;
        super.zDenomDeg = 0;
        super.zNumer = new ComplexPoly(this.kp);
        super.zDenom = new ComplexPoly(1.0D);
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.setDeltaT(deltaT);
        this.zTransform();
    }

    // Plots the time course for a step input
    public void stepInput(double stepMag, double finalTime){

        // Calculate time course outputs
        int n = 51;                             // number of points on plot
        double incrT = finalTime/(double)(n-2); // plotting increment
        double cdata[][] = new double [2][n];   // plotting array

        cdata[0][0]=0.0D;
        cdata[0][1]=0.0D;
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
                cdata[1][i] = rampGradient*Math.pow(cdata[0][i],rampOrder)*this.kp;
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
        super.outputS=super.inputS.times(this.kp);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    //  Get the s-domain output for the stored input and  s-value.
    public Complex getOutputS(){
        super.outputS=super.inputS.times(this.kp);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }


    //  Calculate the current time domain output for a given input and given time
    //  resets deltaT
    public void calcOutputT(double ttime, double inp){
        super.setInputT(ttime, inp);
        this.calcOutputT();
    }

    //  Get the output for the stored sampled input and time.
    public void calcOutputT(){
        // proportional term
        super.outputT[super.sampLen-1] = this.kp*super.inputT[super.sampLen-1];
    }

    // Get the s-domain zeros
    public Complex[] getZerosS(){
        System.out.println("Proportional gain controller has no s-domain zeros");
        return null;
    }

    // Get the s-domain poles
    public Complex[] getPolesS(){
        System.out.println("Proportional gain controller has no s-domain poles");
        return null;
    }

    // Deep copy
    public Prop copy(){
        if(this==null){
            return null;
        }
        else{
            Prop bb = new Prop();
            this.copyBBvariables(bb);

            bb.kp = this.kp;
            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}