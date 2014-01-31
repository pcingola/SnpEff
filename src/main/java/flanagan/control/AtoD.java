/*          Class AtoD
*
*           This class contains constructor and methods that will
*           1.  Simulate an Analogue to Digital Converter (ADC)
*               Range may be set to 0 to Vref or -Vref to +Vref
*               The former is the default value.
*               The quantization error for this ADC is a truncation error
*           or
*           2. Simply act as a marker to be used in OpenPath and
*           ClosedLoop to indicate the presence of an ADC.
*
*           In the latter case the output is equal to the input plus any delay set.
*
*           This class is a subclass of the superclass BlackBox.
*
*           Author:  Michael Thomas Flanagan.
*
*           Created: 27 June 2003
*           Revised: 18 August 2003, 5 May 2005, 2 July 2006, 6 April 2008
*
*           DOCUMENTATION:
*           See Michael T Flanagan's JAVA library on-line web page:
*           http://www.ee.ucl.ac.uk/~mflanaga/java/AtoD.html
*           http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2008   Michael Thomas Flanagan
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

package flanagan.control;

import flanagan.math.*;
import flanagan.complex.*;
import flanagan.control.*;

public class AtoD  extends BlackBox{

    private int nBits = 0;              // Number of bits, n
    private long maximumDecimal = 0;    // 2^n-1
    private double vRef = 0.0D;         // Reference voltage
    private int[] vBinary = null;       // array holding binary output
    private boolean trueAtoD = true;    // if true, a real ADC is simulated
                                        // if false, the instance is simply an AtoD marker
    private boolean range = true;       // if true, range = 0 to vRef
                                        // if false, range = -vRef/2 to +vRef/2
    private double voltageOutput = 0.0D;// if range = true: output voltage corresponding to input voltage truncated by quantiztion error
                                        // if range = false: output voltage equals input voltage
    private String binaryOutput = "";   // if range = true: Sting holding the binary representation of the output voltage
    private long decimalOutput = 0L;    // if range = true: decimal representation of the binary representation of the output
    private double sqnr = 0.0D;         // signal to quantisation noise ratio
    private double input = 0.0D;        // input
    private double inputC = 0.0D;       // input after any clipping
    private double shift = 0.0D;        // voltage shift (vRef/2) if range is -vRef/2 to +vRef/2
    private long decimalShift = 0L;     // voltage shift as decimal represention of its binary representation
    private boolean decCalcDone = false;   // = true when the decimal output has been calculated
    private boolean binCalcDone = false;   // = true when the binary output has been calculated
    private boolean inputSet = false;   // = true when the input has been entered

    private boolean firstCopy = true;           // check used by copy method

    // Constructor
    // Simulates an ADC
    public AtoD(int nBits, double vRef ){
        super("AtoD");
        if(nBits>63)throw new IllegalArgumentException("This program cannot accomadate an ADC simulation with a number of bits greater than 63");
        this.nBits = nBits;
        this.maximumDecimal = (long)Math.pow(2, this.nBits)-1L;
        this.vRef = vRef;
        this.vBinary = new int[nBits+1];
        this.trueAtoD = true;
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
    }

    // Constructor
    // Simply marks an AtoD event
    public AtoD(){
        super("AtoD");
        super.fixedName = "AtoD";
        super.sNumerDeg = 0;
        super.sDenomDeg = 0;
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.ztransMethod=1;
        super.setZtransformMethod(1);
    }

    // Reset range option
    // opt = 0 for range 0 to Vref (default option)
    // opt = 1 for range -Vref to + vref
    public void setRangeOption(int opt){
        if(opt<0 || opt>2)throw new IllegalArgumentException("argument must be either 0 or 1");
        if(opt==0)this.range = true;
        if(opt==1){
            this.range = false;
            this.shift = this.vRef/2.0D;
            this.decimalShift = this.maximumDecimal/2L;
        }
        if(this.inputSet)this.checkInput();
        this.decCalcDone = false;
    }

    // Return the range option
    public String getRange(){
        String ran = null;
        if(this.trueAtoD){
            if(this.range){
                ran = "0 to "+this.vRef;
            }
            else{
                ran = "-"+this.vRef/2 + " to "+this.vRef/2;
            }
        }
        else{
            System.out.println("Class AtoD; method getRange()");
            System.out.println("No range option set - this instance of AtoD is an 'ADC marker' only");
            System.out.println("getRangeOption has returned 'ADC marker only'");
            ran = "ADC marker only";
        }
        return ran;
    }

    // Return the true AtoD option
    public boolean getTrueAtoDoption(){
        if(this.trueAtoD){
            System.out.println("This instance of AtoD is a true simulation of an ADC");
            System.out.println("getTrueAtoDoption has returned 'true'");
        }
        else{
            System.out.println("This instance of AtoD is not a true simulation of an ADC");
            System.out.println("It is simple an 'A to D marker'");
            System.out.println("getTrueAtoDoption has returned 'false'");
        }
        return this.trueAtoD;
    }

    // Returns the reference voltage
    public double getVref(){
        if(!this.trueAtoD){
            System.out.println("No reference voltage set - this instance of AtoD is an 'ADC marker' only");
            System.out.println("getVref has returned 0.0 V");
        }
        return this.vRef;
    }

    // Set input
    public void setInput(double input){
        this.input = input;
        this.checkInput();
        this.inputSet=true;
    }

    // Check whether input in range
    public void checkInput(){
        this.inputC = input;
        if(this.trueAtoD){
            if(this.range){
                if(this.input<0.0D){
                    System.out.println("lower limit of the ADC range exceeded");
                    System.out.println("input voltage set to zero");
                    this.inputC=0.0D;
                }
                if(this.input>this.vRef){
                    System.out.println("upper limit of the ADC range exceeded");
                    System.out.println("input voltage set to "+this.vRef);
                    this.inputC=this.vRef;
                }
            }
            else{
                if(this.input<-this.vRef){
                    System.out.println("lower limit of the ADC range exceeded");
                    System.out.println("input voltage set to "+(-this.vRef/2));
                    this.inputC=-this.vRef/2.0D;
                }
                if(this.input>this.vRef){
                    System.out.println("upper limit of the ADC range exceeded");
                    System.out.println("input voltage set to "+this.vRef/2);
                    this.inputC=this.vRef/2.0D;
                }
            }
        }
        this.inputC += this.shift;
        this.decCalcDone = false;
        this.binCalcDone = false;
    }


    // Return decimal representation of the maximum binary number
    public long getMaximumDecimal(){
        if(!this.trueAtoD){
            System.out.println("This instance of AtoD is not a true simulation of an ADC");
            System.out.println("It is simple an 'A to D marker'");
            System.out.println("getTrueAtoDoption has returned 0");
        }
        return this.maximumDecimal;
    }

    // Return maximum quantization error
    public double maximumQuantizationError(){
        double error = 0.0D;
        if(this.trueAtoD){
            error = this.vRef/this.maximumDecimal;
        }
        else{
            System.out.println("This instance of AtoD is not a true simulation of an ADC");
            System.out.println("It is simple an 'A to D marker'");
            System.out.println("getMaxQuantizationError returns zero");
        }
        return error;
    }




    // Calculate output
    public void calcOutput(){

        if(this.trueAtoD){
            this.decimalOutput = (long)(Math.floor(((this.inputC)/this.vRef)*this.maximumDecimal))-this.decimalShift;
            this.voltageOutput = (this.vRef*this.decimalOutput)/this.maximumDecimal;
            this.sqnr = 20.0D*Fmath.log10(Math.abs((this.inputC-this.shift)/(this.inputC - this.shift  - this.voltageOutput)));
        }
        else{
            this.voltageOutput = this.input;
            this.sqnr = 1.0D/0.0D;
        }

        super.sNumer.resetCoeff(0, new Complex(this.voltageOutput/this.input, 0.0D));

        this.decCalcDone = true;
    }

    // Return SQNR (signal to quantization noise ratio)
    public double getSQNR(){
        if(!this.decCalcDone)this.calcOutput();
        if(!this.trueAtoD){
            System.out.println("This instance of AtoD is not a true simulation of an ADC");
            System.out.println("It is simple an 'A to D marker'");
            System.out.println("getSQNR returned INFINITY");
        }
        return this.sqnr;
    }

    // Return output voltage for the given input
    // output rescaled to input voltage but with quantization error
    public double voltageOutput(){
        if(!this.decCalcDone)this.calcOutput();
        return this.voltageOutput;
    }

    // Return decimal representation of the binary output voltage
    public long decimalOutput(){
        if(!this.decCalcDone)this.calcOutput();
        if(!this.trueAtoD){
            System.out.println("No formal A to D conversion performed - this instance of AtoD is an 'ADC marker' only");
            System.out.println("decimalOutput has returned 0");
        }

        return this.decimalOutput;
    }

    // Convert decimal to binary number of nBits length
    // Two's complement
    public static int[] decimalToBinary(long decimal, int nBits){
        // check sign and reverse if negative
        long decSign = 1L;
        if(decimal<0){
            decSign = -1L;
            decimal *= decSign;
        }

        // check nBits is long enough to accomodate decimal
        // if not extend nBits by powers of two
        long len = (long)Math.ceil(Math.log(decimal)/Math.log(2));
        if(nBits<len){
            boolean test=true;
            int ii=2;
            while(test){
                if(Math.pow(2, ii)>len){
                    nBits=ii;
                    test=false;
                }
            }
        }

        // convert positive decimal to binary
        int[] binary = new int[nBits];
        for(int i=0; i<nBits; i++)binary[i] = 0;
        boolean test = true;
        int ii = 0;
        while(test){
            binary[ii] = (int) (decimal % 2);
            decimal = decimal/2;
            ii++;
            if(decimal==0)test = false;
        }

        // if decimal was entered as negative negate binary
        if(decSign==-1L)binary = AtoD.negateBinary(binary);

        return binary;
    }

    // Negate a positive binary number
    // Two's complement
    public static int[] negateBinary(int[] binary){
        int nBinary = binary.length;
        int nBin = nBinary;

        // add bit if MSB = 1 and assign it zero to give a two's complement positive number
        if(binary[nBinary-1]==1)nBin += nBin;
        int[] negate = new int[nBin];
        int[] one = new int[nBin];
        for(int i=0; i<nBin; i++){
            one[i]=0;
            negate[i]=1;
        }
        one[0]=1;
        // invert all bits
        for(int i=0; i<nBinary; i++){
             if(binary[i] == 1)negate[i] = 0;
        }
        // add one
        negate = AtoD.addBinary(negate, one);

        return negate;
    }

    // Add two binary numbers
    public static int[] addBinary(int[] aa, int[] bb){
        int n = aa.length;
        int m = bb.length;
        int lenMax = n;
        int lenMin = m;
        if(m>n){
            lenMax = m;
            lenMin = n;
        }
        int[] addition = new int[lenMax];
        int carry = 0;
        int sum = 0;
        for(int i=0; i<lenMin; i++){
            sum = aa[i] + bb[i] + carry;
            switch(sum){
                case 0: addition[i] = 0;
                        carry = 0;
                        break;
                case 1: addition[i] = 1;
                        carry = 0;
                        break;
                case 2: addition[i] = 0;
                        carry = 1;
                        break;
                case 3: addition[i] = 1;
                        carry = 1;
                        break;
            }
        }

        return addition;
    }

    // Return binary representation of the output
    public String binaryOutput(){
        if(!this.decCalcDone)this.calcOutput();
        if(this.trueAtoD){
            int nBit = this.nBits+1;
            // shited output to binary
            long absDecOut = this.decimalOutput+this.decimalShift;
            this.vBinary = AtoD.decimalToBinary(absDecOut, nBit);

            if(this.shift>0.0D){
                // shift, if any, to binary
                int[] binaryShift = AtoD.decimalToBinary(this.decimalShift, nBit);

                // negate binary shift
                binaryShift = AtoD.negateBinary(binaryShift);

                // add binary to negated shift
                this.vBinary = AtoD.addBinary(this.vBinary, binaryShift);
            }

            // convert to String
            this.binaryOutput="";
            for(int i=nBit-1; i>=0; i--){
                this.binaryOutput = this.binaryOutput + this.vBinary[i];
            }

        }
        else{
            System.out.println("No formal A to D conversion performed - this instance of AtoD is an 'ADC marker' only");
            System.out.println("binaryOutput has returned 'null'");
        }

        this.binCalcDone = true;
        return this.binaryOutput;
    }

    // Return binary representation of the output as an int array
    // LSB is the zeroth element
    public int[] binaryArray(){

        if(this.trueAtoD){
            if(!this.binCalcDone)this.binaryOutput();
        }
        else{
            System.out.println("No formal A to D conversion performed - this instance of AtoD is an 'ADC marker' only");
            System.out.println("binaryOutput has returned 'null'");
        }

        return this.vBinary;
    }


    // Return quantization error
    public double quantizationError(){
        if(!this.decCalcDone)this.calcOutput();
        double error = 0.0D;
        if(this.trueAtoD){
            error = this.inputC - this.voltageOutput;
        }
        else{
            System.out.println("This instance of AtoD is not a true simulation of an ADC");
            System.out.println("It is simple an 'A to D marker'");
            System.out.println("getQuantizationError returns zero");
        }
        return error;
    }

    // Return any clipping error
    public double clippingError(){

        return this.inputC - this.input;
    }

    // Deep copy
    public AtoD copy(){
        if(this==null){
            return null;
        }
        else{
            AtoD bb = new AtoD();
            this.copyBBvariables(bb);

            bb.nBits = this.nBits;
            bb.maximumDecimal = this.maximumDecimal;
            bb.vRef = this.vRef;
            bb.vBinary = Conv.copy(this.vBinary);
            bb.trueAtoD = this.trueAtoD;
            bb.range = this.range;
            bb.voltageOutput = this.voltageOutput;
            bb.binaryOutput = this.binaryOutput;
            bb.decimalOutput = this.decimalOutput;
            bb.sqnr = this.sqnr;
            bb.input = this.input;
            bb.inputC = this.inputC;
            bb.shift = this.shift;
            bb.decimalShift = this.decimalShift;
            bb.decCalcDone = this.decCalcDone;
            bb.binCalcDone = this.binCalcDone;
            bb.inputSet = this.inputSet;

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}