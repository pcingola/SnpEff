/*      Class PropIntDeriv
*
*       This class contains the constructor to create an instance of
*       a proportional plus integral plus Derivative (PID) controller and
*       the methods needed to use this controller in control loops in the
*       time domain, Laplace transform s domain or the z-transform z domain.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*       Updated: 17 April 2003, 3 May 2005, 2 July 2006, 27 February 2008, 6 April 2008, 7 November 2009
*                24 May 2010
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/PropIntDeriv.html
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


public class PropIntDeriv extends BlackBox{
    private double kp = 1.0D;           //  proportional gain
    private double ti = Double.POSITIVE_INFINITY; //  integral time constant
    private double ki = 0.0D;           //  integral gain
    private double td = 0.0D;           //  derivative time constant
    private double kd = 0.0D;           //  derivative gain

    // Constructor - unit proportional gain, zero integral gain, zero derivative gain
    public PropIntDeriv(){
        super("PropIntDeriv");
        super.setSnumer(new ComplexPoly(0.0D, 1.0D, 0.0D));
        super.setSdenom(new ComplexPoly(0.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Set the proportional gain
    public void setKp(double kp){
        this.kp=kp;
        super.sNumer.resetCoeff(1, new Complex(kp, 0.0));
        super.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Set the integral gain
    public void setKi(double ki){
        this.ki=ki;
        this.ti=this.kp/ki;
        super.sNumer.resetCoeff(0, new Complex(ki, 0.0));
        super.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Set the integral time constant
    public void setTi(double ti){
        this.ti=ti;
        this.ki=this.kp/ti;
        super.sNumer.resetCoeff(0, new Complex(ki, 0.0));
        super.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Set the derivative gain
    public void setKd(double kd){
        this.kd=kd;
        this.td=kd/this.kp;
        super.sNumer.resetCoeff(2, new Complex(kd, 0.0));
        super.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Set the derivative time constant
    public void setTd(double td){
        this.td=td;
        this.kd=this.kp*td;
        super.sNumer.resetCoeff(2, new Complex(kd, 0.0));
        super.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Get the proprtional gain
    public double getKp(){
        return this.kp;
    }

    // Get the integral gain
    public double getKi(){
        return this.ki;
    }

    // Get the integral time constant
    public double getTi(){
        return this.ti;
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
        if(super.deltaT==0.0D)System.out.println("z-transform attempted in PropIntDeriv with a zero sampling period");
        super.deadTimeWarning("zTransform");
        if(super.ztransMethod==0){
            this.mapstozAdHoc();
        }
        else{
            double kit = this.ki*super.deltaT;
            double kdt = this.kd/super.deltaT;
            Complex[] coef = Complex.oneDarray(3);
            coef[0].reset(0.0D,0.0D);
            coef[1].reset(-1.0D,0.0D);
            coef[2].reset(1.0D,0.0D);
            super.zDenom.resetPoly(coef);
            switch(this.integMethod){
                // Trapezium rule
                case 0: coef[0].reset(kdt,0.0D);
                        coef[1].reset(kit/2.0D-2.0D*kdt-this.kp,0.0D);
                        coef[2].reset(this.kp+kit/2.0D+kdt,0.0);
                        super.zNumer.resetPoly(coef);
                        break;
                // Backward rectangular rule
                case 1: coef[0].reset(kdt,0.0D);
                        coef[1].reset(-2.0D*kdt-this.kp,0.0D);
                        coef[2].reset(this.kp+kit+kdt,0.0);
                        super.zNumer.resetPoly(coef);
                        break;
                // Foreward tectangular rule
                case 2: coef[0].reset(kdt,0.0D);
                        coef[1].reset(kit-2.0D*kdt-this.kp,0.0D);
                        coef[2].reset(this.kp+kdt,0.0);
                        super.zNumer.resetPoly(coef);
                        break;
                default:    System.out.println("Integration method option in PropIntDeriv must be 0,1 or 2");
                        System.out.println("It was set at "+integMethod);
                        System.out.println("z-transform not performed");
            }
        }
        super.zZeros = super.zNumer.roots();
        super.zPoles = super.zDenom.roots();
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.setDeltaT(deltaT);
        this.zTransform();
    }

    // Calculate the pole and the zero in the z-domain for an already set sampling period
    public void calcPolesZerosZ(){
        if(super.deltaT==0.0D)System.out.println("z-pole and z-zero calculation attempted in PropIntDeriv.calcPolesZerosZ( with a zero sampling period");
        this.zTransform();
        super.zPoles[0].reset(0.0D, 0.0D);
        super.zPoles[1].reset(1.0D, 0.0D);
        super.zZeros = super.zNumer.roots();
    }

    // Calculate the pole and the zero in the z-domain setting the  sampling period
    public void calcPolesZerosZ(double deltaT){
        this.deltaT = deltaT;
        this.calcPolesZerosZ();
    }

    // Plots the time course for a step input
    public void stepInput(double stepMag, double finalTime){

        // Calculate time course outputs
        int n = 50;                             // number of points on plot
        double incrT = finalTime/(double)(n-1); // plotting increment
        double cdata[][] = new double [2][n];   // plotting array
        double sum = 0.0D;                      // integration sum

        cdata[0][0]=0.0D;
        for(int i=1; i<n; i++){
            cdata[0][i]=cdata[0][i-1]+incrT;
        }
        double kpterm = this.kp*stepMag;
        for(int i=0; i<n; i++){
            sum += ki*incrT*stepMag;
            cdata[1][i] = kpterm + sum;
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
        pg.plot();
    }

    // Plots the time course for a unit step input
    public void stepInput(double finalTime){
        this.stepInput(1.0D, finalTime);
    }

    // Plots the time course for an nth order ramp input (at^n)
    public void rampInput(double rampGradient, int rampOrder, double finalTime){

        // Check if really a step input
        if(rampOrder==0){
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
                sum += ki*rampGradient*(Math.pow(cdata[0][i],rampOrder+1) - Math.pow(cdata[0][i-1],rampOrder+1))/(double)(rampOrder+1);
                cdata[1][i] = this.kp*rampGradient*Math.pow(cdata[0][i],rampOrder) + sum;
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
        super.sValue = sValue;
        super.inputS = iinput;
        Complex term1 = Complex.plusOne();
        Complex term2 = Complex.plusOne();
        Complex term3 = Complex.plusOne();
        term1 = term1.times(this.kp);
        term2 = term2.times(this.ki);
        term2 = term2.over(this.sValue);
        term3 = term3.times(this.kd);
        term3 = term3.times(super.sValue);
        Complex term = term1.plus(term2.plus(term3));
        super.outputS = term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;    }

    //  Get the s-domain output for the stored input and  s-value.
    public Complex getOutputS(){
        Complex term1 = Complex.plusOne();
        Complex term2 = Complex.plusOne();
        Complex term3 = Complex.plusOne();
        term1 = term1.times(this.kp);
        term2 = term2.times(this.ki);
        term2 = term2.over(this.sValue);
        term3 = term3.times(this.kd);
        term3 = term3.times(super.sValue);
        Complex term = term1.plus(term2.plus(term3));
        super.outputS = term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
   }


    //  Calculate the current time domain output for a given input and given time
    //  resets deltaT
    public void calcOutputT(double ttime, double inp){
        super.setInputT(ttime, inp);
        this.calcOutputT();
    }

    //  Calculate the output for the stored sampled input and time
    public void calcOutputT(){
        super.deadTimeWarning("zTransform");
        // proportional term
        super.outputT[super.sampLen-1]=this.kp*super.inputT[super.sampLen-1];
        // + integral term
        if(super.forgetFactor==1.0D){
            switch(super.integMethod){
                // trapezium Rule
                case 0: super.integrationSum += (super.inputT[super.sampLen-1]+super.inputT[super.sampLen-2])*super.deltaT/2.0D;
                        break;
                // backward rectangular rule
                case 1: super.integrationSum += super.inputT[super.sampLen-1]*super.deltaT;
                        break;
                // foreward rectangular rule
                case 2: super.integrationSum += super.inputT[super.sampLen-2]*super.deltaT;
                        break;
                default:    System.out.println("Integration method option in PropInt must be 0,1 or 2");
                            System.out.println("It was set at "+super.integMethod);
                            System.out.println("getOutput not performed");
            }
        }
        else{
            switch(super.integMethod){
                // trapezium Rule
                case 0: super.integrationSum=0.0D;
                        for(int i=1; i<super.sampLen; i++){
                            super.integrationSum+=Math.pow(super.forgetFactor, super.sampLen-1-i)*(super.inputT[i-1]+super.inputT[i])*super.deltaT/2.0D;
                        };
                        break;
                // backward rectangular rule
                case 1:  super.integrationSum=0.0D;
                        for(int i=1; i<sampLen; i++){
                            super.integrationSum+=Math.pow(super.forgetFactor, super.sampLen-1-i)*(super.inputT[i])*super.deltaT;
                        };
                        break;
                // foreward rectangular rule
                case 2: super.integrationSum=0.0D;
                        for(int i=1; i<super.sampLen; i++){
                            super.integrationSum+=Math.pow(super.forgetFactor, super.sampLen-1-i)*(super.inputT[i-1])*super.deltaT;
                        };
                        break;
                default:    System.out.println("Integration method option in PropInt must be 0,1 or 2");
                            System.out.println("It was set at "+super.integMethod);
                            System.out.println("getOutput not performed");
            }
        }
        super.outputT[super.sampLen-1] += this.ki*super.integrationSum;
        // + derivative term
        super.outputT[sampLen-1] += this.kd*(super.inputT[sampLen-1]-super.inputT[sampLen-2])/super.deltaT;
    }


    // Deep copy
    public PropIntDeriv copy(){
        if(this==null){
            return null;
        }
        else{
            PropIntDeriv bb = new PropIntDeriv();
            this.copyBBvariables(bb);

            bb.kp = this.kp;
            bb.ti = this.ti;
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
