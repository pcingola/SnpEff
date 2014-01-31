/*          Class DtoA
*
*           This class contains constructor and methods that will
*           1.  Simulate a Digital to Analogue Convertor (DAC)
*           or
*           2. Simply act as a marker to be used in OpenPath and
*           ClosedLoop to indicate the presence of an DAC. In the
*           latter case the output is equal to the input plus any delay set.
*
*           This class is a subclass of the superclass BlackBox.
*
*           Author:  Michael Thomas Flanagan.
*
*           Created: 27 June 2003
*           Revised: 18 August 2003, 9 May 2005, April 2008, 7 November 2009, 18 January 2011
*
*           DOCUMENTATION:
*           See Michael T Flanagan's JAVA library on-line web page:
*           http://www.ee.ucl.ac.uk/~mflanaga/java/DtoA.html
*           http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2011   Michael Thomas Flanagan
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

import flanagan.complex.*;
import flanagan.math.Conv;

public class DtoA  extends BlackBox{

    private int nBits = 0;              // Number of bits, n
    private long maximumDecimal =  0;   // 2^n-1
    private double vRef = 0.0D;         // Reference voltage
    private int[] vBinary = null;       // array holding binary input
    private boolean trueDtoA = true;    // if true, a real DAC is simulated
                                        // if false, the instance is simply an DtoA marker
    private double outputVoltage = 0.0D;// output voltage
    private double voltageInput =0.0D;  // input as voltage - if this is the input the output is put equal to this input
    private String binaryInput = "";    // input as a binary String
    private long decimalInput = 0L;     // input as decimal representation of a binary String
    private boolean inputSet = false;   // = true when input is set

    // Constructor
    // Simulates a DAC
    public DtoA(int nBits, double vRef ){
        super("DtoA");
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
        this.nBits = nBits;
        this.vBinary = new int[nBits+1];
        this.maximumDecimal = (long)Math.pow(2, this.nBits)-1L;
        this.vRef = vRef;
        this.trueDtoA = true;
    }

    // Constructor
    // Simply marks an DtoA event
    public DtoA(){
        super("DtoA");
        this.trueDtoA = false;
        super.sNumerDeg = 0;
        super.sDenomDeg = 0;
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D));
        super.setZtransformMethod(1);
    }

    // Return the true DtoA option
    public boolean getTrueDtoAoption(){
        if(this.trueDtoA){
            System.out.println("This instance of DtoA is a true simulation of an ADC");
            System.out.println("getTrueDtoAoption has returned 'true'");
        }
        else{
            System.out.println("This instance of DtoA is not a true simulation of an ADC");
            System.out.println("It is simple an 'D to A marker'");
            System.out.println("getTrueDtoAoption has returned 'false'");
        }
        return this.trueDtoA;
    }

    // Set input entered as a Sting representing the binary inut in two's complement
    // of n+1 bits where n = this.nBits and the extra bit is the sign bit
    public void setInput(String input){
        this.binaryInput = input.trim();
        int len = this.binaryInput.length();
        if(len>this.nBits+1)throw new IllegalArgumentException("length of input String is greater than the DAC bit number plus one");
        if(len<this.nBits+1){
            System.out.println("Class - DtoA;  method - setInput(String)");
            System.out.println("The input String is less than DAC number of bits plus one");
            System.out.println("String assumed to represent a postive unsigned binary number");
            System.out.println("unfilled bits assigned zeros");
            for(int i=len; i<this.nBits+1; i++)this.binaryInput = '0'+this.binaryInput;
            len = this.nBits+1;
        }

        // Convert  String to int array
        int ii = 0;
        int jj = 0;
        char c =' ';
        for(int i=len-1; i>=0; i--){
            c = this.binaryInput.charAt(i);
            if(c=='1'){
                ii=1;
            }
            else{
                if(c=='0'){
                    ii = 0;
                }
                else{
                    throw new IllegalArgumentException("String input must be '0's or '1's");
                }
            }
            jj = len-i-1;
            this.vBinary[jj] = ii;
        }

        // Check if input is negative
        long sign = 1L;
        int[] vPosBinary = Conv.copy(this.vBinary);
        if(this.vBinary[len-1]==1){
            sign = -1L;
            vPosBinary = this.negateNegativeBinary(vPosBinary);
        }

        // convert positive binary to decimal equivalent
        this.decimalInput = DtoA.binaryToDecimal(vPosBinary);

        // adjust sign
        if(sign==-1L)this.decimalInput = -this.decimalInput;

        // convert to voltage
        this.outputVoltage = (this.decimalInput*this.vRef)/(this.maximumDecimal+1L);

        this.inputSet = true;
    }

    // Set input entered as an integer array representing the binary inut in two's complement
    // of n+1 bits where n = this.nBits and the extra bit is the sign bit
    // Zeroth array element is the least significant bit (LSB)
    public void setInput(int[] input){
        int len = input.length;
        if(len>this.nBits+1)throw new IllegalArgumentException("length of input array is greater than the DAC bit number plus  one");
        for(int i=0; i<len; i++)this.vBinary[i]=input[i];
        if(len<this.nBits+1){
            System.out.println("Class - DtoA;  method - setInput(String)");
            System.out.println("The input array is less than DAC number of bits plus one");
            System.out.println("Array assumed to represent a postive unsigned binary number");
            System.out.println("unfilled bits assigned zeros");
            for(int i=len; i<this.nBits+1; i++)this.vBinary[i] = 0;
            len = this.nBits+1;
        }

        // convert to String
        this.binaryInput="";
        for(int i=this.nBits; i>=0; i--){
            this.binaryInput = this.binaryInput + this.vBinary[i];
        }

        // Check if input is negative
        long sign = 1L;
        int[] vPosBinary = Conv.copy(this.vBinary);
        if(this.vBinary[len-1]==1){
            sign = -1L;
            vPosBinary = this.negateNegativeBinary(this.vBinary);
        }

        // convert positive binary to decimal equivalent
        this.decimalInput = DtoA.binaryToDecimal(vPosBinary);

        // adjust sign
        if(sign==-1L)this.decimalInput = -this.decimalInput;

         // convert to voltage
        this.outputVoltage = (this.decimalInput*this.vRef)/(this.maximumDecimal+1L);

        this.inputSet = true;
    }


    // Set input entered as a decimal equivalent the binary input
    public void setInput(long input){
        if(Math.abs(input)>this.maximumDecimal)throw new IllegalArgumentException("abs(input), "+input+", is greater than the maximum decimal representation, "+this.maximumDecimal+", allowed by the set number of bits, "+this.nBits);
        this.decimalInput = input;

        // convert to voltage
        this.outputVoltage = (input*this.vRef)/(this.maximumDecimal+1L);

        // convert decimal to binary
        long dec = this.decimalInput;
        int sign = 1;
        if(dec<0){
            sign = -1;
            dec = -dec;
        }

        for(int i=0; i<this.nBits+1; i++)this.vBinary[i] = 0;
        boolean test = true;
        int ii = 0;
        while(test){
            this.vBinary[ii] = (int) (dec % 2);
            dec = dec/2L;
            ii++;
            if(dec==0L)test = false;
        }

        // if decimal was  negative negate binary
        if(sign==-1L)this.vBinary = AtoD.negateBinary(this.vBinary);

        // convert to String
        this.binaryInput="";
        for(int i=this.nBits; i>=0; i--){
            this.binaryInput = this.binaryInput + this.vBinary[i];
        }

        this.inputSet = true;
    }

    // Enter input as a voltage
    public void setInput(double input){

        if(this.trueDtoA){
            if(Math.abs(input)>this.vRef){
                throw new IllegalArgumentException("The input voltage in this simulation of a DAC must be less than nor equal to the reference voltage\nIf you choose the constructor without an argument list, i.e. an instance of DtoA that is simply a DAC marker\nyou may imput any voltage and the output will be made equal to that voltage");
            }
            else{
                this.voltageInput=input;
                AtoD adc = new AtoD(this.nBits, this.vRef);
                adc.setInput(input);
                this.decimalInput = adc.decimalOutput();
                this.binaryInput = adc.binaryOutput();
                this.vBinary = adc.binaryArray();
            }
        }
        else{
            this.outputVoltage = input;
        }
        super.sNumer.resetCoeff(0, new Complex(this.outputVoltage/this.voltageInput, 0.0D));

        this.inputSet = true;
    }

    // Convert positive binary to decimal equivalent
    private static long binaryToDecimal(int[] binary){
        long decimal = 0L;
        for(int i=0; i<binary.length; i++){
            decimal += (long)(Math.pow(2,i)*binary[i]);
        }
        return decimal;
    }

    // Negate a ngative binary number
    // Two's complement
    private static int[] negateNegativeBinary(int[] binary){
        int nBin = binary.length;
        int[] negate = new int[nBin];
        int[] minusOne = new int[nBin];
        for(int i=0; i<nBin; i++){
            minusOne[i]=1;
            negate[i]=0;
        }
        // subtract one
        negate = DtoA.addBinary(negate, minusOne);
        // invert all bits
        for(int i=0; i<nBin; i++){
             if(binary[i] == 0)negate[i] = 1;
        }

        return negate;
    }

    // Add two binary numbers
    private static int[] addBinary(int[] aa, int[] bb){
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

    // Return output
    public double getOutput(){
        if(!this.inputSet)throw new IllegalArgumentException("No input has been entered");
        return this.outputVoltage;
    }

    // Return decimal input
    public long getDecimalInput(){
        if(!this.inputSet)throw new IllegalArgumentException("No input has been entered");
        if(!this.trueDtoA){
            System.out.println("Class - DtoA;  method - getDecimalInput");
            System.out.println("This instance of DtoA is not a true simulation of an DAC");
            System.out.println("It is simple an 'D to A marker'");
            System.out.println("getDecimalInput has returned 0L");
            this.decimalInput = 0L;
        }

        return this.decimalInput;
    }

    // Return binary input as a String
    public String getBinaryInput(){
        if(!this.inputSet)throw new IllegalArgumentException("No input has been entered");
        if(!this.trueDtoA){
            System.out.println("Class - DtoA;  method - getBinaryInput");
            System.out.println("This instance of DtoA is not a true simulation of an DAC");
            System.out.println("It is simple an 'D to A marker'");
            System.out.println("getBinaryInput has returned null");
            this.binaryInput = null;
        }

        return this.binaryInput;
    }

    // Return binary input as int array (zeroth element = LSD)
    public int[] getBinaryArray(){
        if(!this.inputSet)throw new IllegalArgumentException("No input has been entered");
        if(!this.trueDtoA){
            System.out.println("Class - DtoA;  method - getBinaryInput");
            System.out.println("This instance of DtoA is not a true simulation of an DAC");
            System.out.println("It is simple an 'D to A marker'");
            System.out.println("getBinaryArray has returned null");
            this.vBinary = null;
        }

        return this.vBinary;
    }

    // Deep copy
    public DtoA copy(){
        if(this==null){
            return null;
        }
        else{
            DtoA bb = new DtoA();
            this.copyBBvariables(bb);

            bb.nBits = this.nBits;
            bb.maximumDecimal = this.maximumDecimal;
            bb.vRef = this.vRef;
            bb.vBinary = Conv.copy(this.vBinary);
            bb.trueDtoA = this.trueDtoA;
            bb.outputVoltage = this.outputVoltage;
            bb.voltageInput = this.voltageInput;
            bb.binaryInput = this.binaryInput;
            bb.decimalInput = this.decimalInput;
            bb.inputSet = this.inputSet;

            return bb;
        }
    }

    // Clone - overrides Java.Object method clone
    public Object clone(){
        return (Object)this.copy();
    }
}