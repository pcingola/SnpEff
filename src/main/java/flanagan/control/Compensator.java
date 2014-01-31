/*      Class Compensator
*
*       This class contains the constructor to create an instance of
*       a generalised compensator,
*           K(a + s)/(b + s)
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: 14 May 2005
*       Updates: 13 April 2006, 1 July 2006, 6 April 2008, 2 December 2008, 2-7 November 2009
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/Compensator.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*       Copyright (c) 2002 - 2009  Michael Thomas Flanagan
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

public class Compensator extends BlackBox{

    private double kConst = 1.0D;  // K constant in compensator equation above
    private double aConst = 1.0D;  // a constant in compensator equation above
    private double bConst = 1.0D;  // b constant in compensator equation above

    // Constructor - all constants = 1
    public Compensator(){
        super("Compensator");
        super.sZeros = Complex.oneDarray(1);
        super.sPoles = Complex.oneDarray(1);
        super.setSnumer(new ComplexPoly(1.0D, 1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Constructor
    // constants set from argument list
    public Compensator(double kk, double aa, double bb){
        super("Compensator");
        this.aConst = aa;
        this.bConst = bb;
        this.kConst = kk;
        super.sZeros = Complex.oneDarray(1);
        super.sPoles = Complex.oneDarray(1);
        super.setSnumer(new ComplexPoly(this.aConst*kConst, kConst));
        super.setSdenom(new ComplexPoly(this.bConst, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    public void setCoeff(double kk, double aa, double bb){
        this.aConst = aa;
        this.bConst = bb;
        this.kConst = kk;
        Complex[] num = Complex.oneDarray(2);
        num[0].reset(this.aConst*this.kConst, 0.0D);
        num[1].reset(this.kConst, 0.0D);
        super.sNumer.resetPoly(num);
        Complex[] den = Complex.oneDarray(2);
        den[0].reset(this.bConst, 0.0D);
        den[1].reset(1.0D, 0.0D);
        super.sDenom.resetPoly(den);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setK(double kk){
        this.kConst = kk;
        Complex co = new Complex(this.aConst*this.kConst, 0.0);
        super.sNumer.resetCoeff(0, co);
        co = new Complex(this.kConst, 0.0);
        super.sNumer.resetCoeff(1, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setA(double aa){
        this.aConst = aa;
        Complex co = new Complex(this.aConst*this.kConst, 0.0);
        super.sNumer.resetCoeff(0, co);
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

    public double getA(){
        return this.aConst;
    }

    public double getB(){
        return this.bConst;
    }

    public double getK(){
        return this.kConst;
    }

    // Calculate the zeros and poles in the s-domain
    public void calcPolesZerosS(){
        super.sZeros[0].setReal(-aConst);
        super.sPoles[0].setReal(-bConst);
        super.sNumerScaleFactor = BlackBox.scaleFactor(super.sNumer, super.sZeros);
        super.sDenomScaleFactor = BlackBox.scaleFactor(super.sDenom, super.sPoles);

    }

    // Deep copy
    public Compensator copy(){
        if(this==null){
            return null;
        }
        else{
            Compensator bb = new Compensator();
            this.copyBBvariables(bb);

            bb.kConst = this.kConst;
            bb.aConst = this.aConst;
            bb.bConst = this.bConst;

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}