/*      Class LowPassPassive
*
*       This class contains the constructor to create an instance of
*       a low pass filter:
*           V(out) = V(in)/(1 +RCjomega)
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: 21 May 2005
*       Updated: 2 July 2006, 6 April 2008, 2 December 2008, 2-7 November 2009
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/LowPassPassive.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*       Copyright (c) 2005 - 2009 Michael Thomas Flanagan
*
*       PERMISSION TO COPY:
*
*       Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is
*       provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
*       and associated documentation or publications.
*
*       Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice,
*       this list of conditions and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
*       Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
*       the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission
*       from the Michael Thomas Flanagan:
*
*       Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
*       Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
*       or its derivatives.
*
***************************************************************************************/

package flanagan.control;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;

public class LowPassPassive extends BlackBox{

    private double resistance = 0.0D;       // Resistance value, R ohms
    private double capacitance = 0.0D;      // Capacitance value, C farads
    private double timeConstant = 0.0D;     // Time constant, RC seconds
    private boolean setR = false;           // = true when resistance set
    private boolean setC = false;           // = true when capacitance set

    // Constructor

    // Sets time constant to unity and the order to unity
    public LowPassPassive(){
        super("PassiveLowPass");
        super.sPoles = Complex.oneDarray(1);
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
        this.timeConstant = 1.0D;
    }

    public void setResistance(double res){
        this.resistance = res;
        this.timeConstant = res*this.capacitance;
        this.calcPolesZerosS();
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
        this.setR = true;
    }

    public void setCapacitance(double cap){
        this.capacitance = cap;
        this.timeConstant = cap*this.resistance;
        this.calcPolesZerosS();
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
        this.setC = true;
    }

    public void setTimeConstant(double tau){
        this.timeConstant = tau;
        this.calcPolesZerosS();
        super.sDenom = ComplexPoly.rootsToPoly(this.sPoles);
        super.addDeadTimeExtras();
    }

    public double getResistance(){
        if(this.setR){
            return this.resistance;
        }
        else{
            System.out.println("Class; LowPassPassive, method: getResistance");
            System.out.println("No resistance has been entered; zero returned");
            return 0.0D;
        }
    }

    public double getCapacitance(){
        if(this.setC){
            return this.capacitance;
        }
        else{
            System.out.println("Class; LowPassPassive, method: getCapacitance");
            System.out.println("No capacitance has been entered; zero returned");
            return 0.0D;
        }
    }

    public double getTimeConstant(){
        return this.timeConstant;
    }

    // Calculate the zeros and poles in the s-domain
    protected void calcPolesZerosS(){
            super.sPoles[0].setReal(-this.timeConstant);
            super.sNumerScaleFactor = super.sNumer.coeffCopy(0);
            super.sDenomScaleFactor = BlackBox.scaleFactor(super.sDenom, super.sPoles);
    }

    // Deep copy
    public LowPassPassive copy(){
        if(this==null){
            return null;
        }
        else{
            LowPassPassive bb = new LowPassPassive();
            this.copyBBvariables(bb);

            bb.resistance = this.resistance;
            bb.capacitance = this.capacitance;
            bb.timeConstant = this.timeConstant;
            bb.setR = this.setR;
            bb.setC = this.setC;
            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}