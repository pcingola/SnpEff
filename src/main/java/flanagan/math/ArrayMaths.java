/*
*   Class   ArrayMaths
*
*   USAGE:  One dimensional arrays:  mathematical manipulations and inter-conversions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       April 2008
*   AMENDED:    22-30 May 2008, 4 June 2008, 27-28 June 2007, 2-4 July 2008,
*               8 July 2008, 25 July 2008, 4 September 2008, 13 December 2008
*               29 September 2010, 29 November 2010, 
*               18 January 2011, 30 November 2011, 10 December 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ArrayMaths.html
*
*   Copyright (c) 2008 - 2011
*
*   PERMISSION TO COPY:
*
*   Redistributions of this source code, or parts of, must retain the above
*   copyright notice, this list of conditions and the following disclaimer.
*
*   Redistribution in binary form of all or parts of this class, must reproduce
*   the above copyright, this list of conditions and the following disclaimer in
*   the documentation and/or other materials provided with the distribution.
*
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all
*   copies and associated documentation or publications.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.math;


import java.math.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import flanagan.complex.Complex;
import flanagan.complex.ComplexMatrix;
import flanagan.circuits.Phasor;
import flanagan.circuits.PhasorMatrix;
import flanagan.math.Conv;
import flanagan.analysis.Stat;
import flanagan.io.PrintToScreen;
import flanagan.plot.*;

public class ArrayMaths{

    protected ArrayList<Object> array = null;    // internal array

    protected int length = 0;       // array length
    protected int type = -1;        // 0 double, 1 Double, 2 float, 3 Float, 4 long, 5 Long, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
                                    // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character, 18 String
    protected int[] originalTypes = null;     // list of entered types in the array

    protected String[] typeName = {"double", "Double", "float", "Float", "long", "Long", "int", "Integer", "short", "Short", "byte", "Byte", "BigDecimal", "BigInteger", "Complex", "Phasor", "char", "Character", "String"};

    protected ArrayList<Object> summ = new ArrayList<Object>(1);        // sum of all elements
    protected ArrayList<Object> productt = new ArrayList<Object>(1);    // product of all elements

    protected int[] sortedIndices = null;                               // sorted indices

    protected ArrayList<Object> minmax = new ArrayList<Object>(2);      // element at 0 -  maximum value
                                                                        // element at 1 -  minimum value
    protected int maxIndex = -1;                        // index of the maximum value array element
    protected int minIndex = -1;                        // index of the minimum value array element

    protected boolean sumDone = false;                  // = true whem array sum has been found
    protected boolean productDone = false;              // = true whem array product has been found

    protected boolean sumlongToDouble = false;          // = true whem long has been converted to Double to avoid overflow in summation
    protected boolean productlongToDouble = false;      // = true whem long has been converted to Double to avoid overflow in multiplication

    protected boolean suppressMessages = false;         // = true when suppress 'possible loss of precision' messages has been set

    public String[] words = null;                       // words in alphabetic search method
    public String[] sortedWords = null;                 // sorted words in alphabetic search method
    protected char[][] wordChar = null;                 // word characters in alphabetic search method
    protected char[][] holdWordChar = null;             // holding array of word characters in alphabetic search method
    protected int[] wordOrder = null;                   // word order indices in alphabetic search method
    protected int[] holdWordOrder = null;               // holding array of word order indices in alphabetic search method
    protected int nWords = 0;                           // number of words in alphabetic search method
    protected int nLength = 0;                          // length of longest word in alphabetic search method

    // HashMap for 'arithmetic integer' recognition nmethod
    protected static final Map<Object,Object> integers = new HashMap<Object,Object>();
    static{
        integers.put(Integer.class, BigDecimal.valueOf(Integer.MAX_VALUE));
        integers.put(Long.class, BigDecimal.valueOf(Long.MAX_VALUE));
        integers.put(Byte.class, BigDecimal.valueOf(Byte.MAX_VALUE));
        integers.put(Short.class, BigDecimal.valueOf(Short.MAX_VALUE));
        integers.put(BigInteger.class, BigDecimal.valueOf(-1));
    }

    // CONSTRUCTORS
    protected ArrayMaths(){
        this.array = new ArrayList<Object>();
    }

    public ArrayMaths(double[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 0;
        for(int i=0; i<this.length; i++)this.array.add(new Double(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Double[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 1;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(long[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 4;
        for(int i=0; i<this.length; i++)this.array.add(new Long(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Long[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 5;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

        public ArrayMaths(float[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 2;
        for(int i=0; i<this.length; i++)this.array.add(new Float(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Float[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 3;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(int[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 6;
        for(int i=0; i<this.length; i++)this.array.add(new Integer(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Integer[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 7;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(short[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 8;
        for(int i=0; i<this.length; i++)this.array.add(new Short(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Short[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 9;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(byte[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 10;
        for(int i=0; i<this.length; i++)this.array.add(new Byte(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Byte[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 11;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(BigDecimal[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 12;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(BigInteger[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 13;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Complex[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 14;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
    }

    public ArrayMaths(Phasor[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 15;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
    }

    public ArrayMaths(char[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 16;
        for(int i=0; i<this.length; i++)this.array.add(new Character(array[i]));
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Character[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 17;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(String[] array){
        this.length = array.length;
        this.array = new ArrayList<Object>(this.length);
        this.type = 18;
        for(int i=0; i<this.length; i++)this.array.add(array[i]);
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
    }

    public ArrayMaths(Object[] array){
        this.length = array.length;
        this.originalTypes = new int[this.length];
        ArrayList<Object> arrayl = new ArrayList<Object>(this.length);
        for(int i=0; i<this.length; i++)arrayl.add(array[i]);
        ArrayMaths am = new ArrayMaths(arrayl);
        this.array = am.getArray_as_ArrayList();
        this.minmax = am.minmax;
        this.minIndex = am.minIndex;
        this.maxIndex = am.maxIndex;
        this.originalTypes = am.originalTypes;
    }

    public ArrayMaths(Stat arrayst){
        this.array = arrayst.getArray_as_ArrayList();
        this.length = this.array.size();
        this.type = arrayst.typeIndex();
        this.originalTypes = new int[this.length];
        for(int i=0; i<this.length; i++)this.originalTypes[i] = this.type;
        this.minmax();
    }

    public ArrayMaths(Vector<Object> arrayv){
        this.length = arrayv.size();
        this.originalTypes = new int[this.length];
        this.array = new ArrayList<Object>(this.length);

        for(int i=0; i<this.length; i++){
            this.originalTypes[i] = -1;
            if(arrayv.elementAt(i) instanceof Double)this.originalTypes[i] = 1;
            if(arrayv.elementAt(i) instanceof Float)this.originalTypes[i] = 3;
            if(arrayv.elementAt(i) instanceof Long)this.originalTypes[i] = 5;
            if(arrayv.elementAt(i) instanceof Integer)this.originalTypes[i] = 7;
            if(arrayv.elementAt(i) instanceof Short)this.originalTypes[i] = 9;
            if(arrayv.elementAt(i) instanceof Byte)this.originalTypes[i] =11;
            if(arrayv.elementAt(i) instanceof BigDecimal)this.originalTypes[i] = 12;
            if(arrayv.elementAt(i) instanceof BigInteger)this.originalTypes[i] = 13;
            if(arrayv.elementAt(i) instanceof Complex)this.originalTypes[i] = 14;
            if(arrayv.elementAt(i) instanceof Phasor)this.originalTypes[i] = 15;
            if(arrayv.elementAt(i) instanceof Character)this.originalTypes[i] = 17;
            if(arrayv.elementAt(i) instanceof String)this.originalTypes[i] = 18;
            if(this.originalTypes[i]==-1)throw new IllegalArgumentException("Object at " + i + " not recognised as one allowed by this class");
        }

        int testType = -1;
        for(int i=0; i<this.length; i++)if(this.originalTypes[i]==18)testType = 0;
        for(int i=0; i<this.length; i++)if(this.originalTypes[i]==14)testType = 1;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==15)testType = 2;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==12)testType = 3;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==13)testType = 4;
        if(testType==4)for(int i=0; i<this.length; i++)if(this.originalTypes[i]<=3)testType = 3;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]<=3)testType = 5;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]>3 && this.originalTypes[i]<12)testType = 6;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==17)testType = 7;
        if(testType==-1)throw new IllegalArgumentException("It should not be possible to reach this exception - main Object type not identified");
        switch(testType){
            case 0: this.type = 18;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayv.elementAt(i);
                                this.array.add(hold1.toString());
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(hold3.toString());
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(hold5.toString());
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(hold7.toString());
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(hold9.toString());
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(hold11.toString());
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayv.elementAt(i);
                                this.array.add(hold12.toString());
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayv.elementAt(i);
                                this.array.add(hold13.toString());
                                break;
                        case 14: Complex hold14 = (Complex)arrayv.elementAt(i);
                                this.array.add(hold14.toString());
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayv.elementAt(i);
                                this.array.add(hold15.toString());
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(hold17.toString());
                                break;
                        case 18: String hold18 = (String)arrayv.elementAt(i);
                                this.array.add(hold18);
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 1: this.type = 14;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayv.elementAt(i);
                                this.array.add(new Complex(hold1.doubleValue()));
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(new Complex(hold3.doubleValue()));
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(new Complex(hold5.doubleValue()));
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(new Complex(hold7.doubleValue()));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(new Complex(hold9.doubleValue()));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(new Complex(hold11.doubleValue()));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayv.elementAt(i);
                                this.array.add(new Complex(hold12.doubleValue()));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayv.elementAt(i);
                                this.array.add(new Complex(hold13.doubleValue()));
                                break;
                        case 14: Complex hold14 = (Complex)arrayv.elementAt(i);
                                this.array.add(hold14);
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Phasor_to_Complex(hold15));
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new Complex((double)((int)hold17.charValue())));
                                break;
                        case 18: String hold18 = (String)arrayv.elementAt(i);
                                this.array.add(new Complex(Double.parseDouble(hold18)));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 2: this.type = 15;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold1.doubleValue()));
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold3.doubleValue()));
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold5.doubleValue()));
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold7.doubleValue()));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold9.doubleValue()));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold11.doubleValue()));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold12.doubleValue()));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayv.elementAt(i);
                                this.array.add(new Phasor(hold13.doubleValue()));
                                break;
                        case 14: Complex hold14 = (Complex)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Complex_to_Phasor(hold14));
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayv.elementAt(i);
                                this.array.add(hold15);
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new Phasor((double)((int)hold17.charValue())));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 3: this.type = 12;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Double_to_BigDecimal(hold1));
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Float_to_BigDecimal(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Long_to_BigDecimal(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Integer_to_BigDecimal(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Short_to_BigDecimal(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Byte_to_BigDecimal(hold11));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayv.elementAt(i);
                                this.array.add(hold12);
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayv.elementAt(i);
                                this.array.add(Conv.convert_BigInteger_to_BigDecimal(hold13));
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new BigDecimal(hold17.toString()));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 4: this.type = 13;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Double_to_BigInteger(hold1));
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Float_to_BigInteger(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Long_to_BigInteger(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Integer_to_BigInteger(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Short_to_BigInteger(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Byte_to_BigInteger(hold11));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayv.elementAt(i);
                                this.array.add(Conv.convert_BigDecimal_to_BigInteger(hold12));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayv.elementAt(i);
                                this.array.add(hold13);
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new BigInteger(hold17.toString()));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 5: this.type = 1;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayv.elementAt(i);
                                this.array.add(hold1);
                                break;
                        case 3: Float hold3 = (Float)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Float_to_Double(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Long_to_Double(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Integer_to_Double(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Short_to_Double(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Byte_to_Double(hold11));
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new Double(Double.parseDouble(hold17.toString())));
                                break;
                       default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 6: this.type = 7;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 5: Long hold5 = (Long)arrayv.elementAt(i);
                                this.array.add(hold5);
                                break;
                        case 7: Integer hold7 = (Integer)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Integer_to_Long(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Short_to_Long(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayv.elementAt(i);
                                this.array.add(Conv.convert_Byte_to_Long(hold11));
                                break;
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(new Long((long)((int)hold17.charValue())));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 7: this.type = 7;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 17: Character hold17 = (Character)arrayv.elementAt(i);
                                this.array.add(hold17);
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
             default: throw new IllegalArgumentException("Dominant array data type not identified by this method");
        }

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16: this.minmax();
         }
    }


    public ArrayMaths(ArrayList<Object> arrayl){
        this.length = arrayl.size();
        this.originalTypes = new int[this.length];
        this.array = new ArrayList<Object>(this.length);

        for(int i=0; i<this.length; i++){
            this.originalTypes[i] = -1;
            if(arrayl.get(i) instanceof Double)this.originalTypes[i] = 1;
            if(arrayl.get(i) instanceof Float)this.originalTypes[i] = 3;
            if(arrayl.get(i) instanceof Long)this.originalTypes[i] = 5;
            if(arrayl.get(i) instanceof Integer)this.originalTypes[i] = 7;
            if(arrayl.get(i) instanceof Short)this.originalTypes[i] = 9;
            if(arrayl.get(i) instanceof Byte)this.originalTypes[i] =11;
            if(arrayl.get(i) instanceof BigDecimal)this.originalTypes[i] = 12;
            if(arrayl.get(i) instanceof BigInteger)this.originalTypes[i] = 13;
            if(arrayl.get(i) instanceof Complex)this.originalTypes[i] = 14;
            if(arrayl.get(i) instanceof Phasor)this.originalTypes[i] = 15;
            if(arrayl.get(i) instanceof Character)this.originalTypes[i] = 17;
            if(arrayl.get(i) instanceof String)this.originalTypes[i] = 18;
            if(this.originalTypes[i]==-1)throw new IllegalArgumentException("Object at " + i + " not recognised as one allowed by this class");
        }

        int testType = -1;
        for(int i=0; i<this.length; i++)if(this.originalTypes[i]==18)testType = 0;
        for(int i=0; i<this.length; i++)if(this.originalTypes[i]==14)testType = 1;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==15)testType = 2;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==12)testType = 3;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==13)testType = 4;
        if(testType==4)for(int i=0; i<this.length; i++)if(this.originalTypes[i]<=3)testType = 3;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]<=3)testType = 5;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]>3 && this.originalTypes[i]<12)testType = 6;
        if(testType==-1)for(int i=0; i<this.length; i++)if(this.originalTypes[i]==17)testType = 7;
        if(testType==-1)throw new IllegalArgumentException("It should not be possible to reach this exception - main Object type not identified");
        switch(testType){
            case 0: this.type = 18;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayl.get(i);
                                this.array.add(hold1.toString());
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(hold3.toString());
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(hold5.toString());
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(hold7.toString());
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(hold9.toString());
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(hold11.toString());
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayl.get(i);
                                this.array.add(hold12.toString());
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayl.get(i);
                                this.array.add(hold13.toString());
                                break;
                        case 14: Complex hold14 = (Complex)arrayl.get(i);
                                this.array.add(hold14.toString());
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayl.get(i);
                                this.array.add(hold15.toString());
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(hold17.toString());
                                break;
                        case 18: String hold18 = (String)arrayl.get(i);
                                this.array.add(hold18);
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 1: this.type = 14;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayl.get(i);
                                this.array.add(new Complex(hold1.doubleValue()));
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(new Complex(hold3.doubleValue()));
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(new Complex(hold5.doubleValue()));
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(new Complex(hold7.doubleValue()));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(new Complex(hold9.doubleValue()));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(new Complex(hold11.doubleValue()));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayl.get(i);
                                this.array.add(new Complex(hold12.doubleValue()));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayl.get(i);
                                this.array.add(new Complex(hold13.doubleValue()));
                                break;
                        case 14: Complex hold14 = (Complex)arrayl.get(i);
                                this.array.add(hold14);
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayl.get(i);
                                this.array.add(Conv.convert_Phasor_to_Complex(hold15));
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new Complex((double)((int)hold17.charValue())));
                                break;
                        case 18: String hold18 = (String)arrayl.get(i);
                                this.array.add(new Complex(Double.parseDouble(hold18)));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 2: this.type = 15;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1= (Double)arrayl.get(i);
                                this.array.add(new Phasor(hold1.doubleValue()));
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(new Phasor(hold3.doubleValue()));
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(new Phasor(hold5.doubleValue()));
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(new Phasor(hold7.doubleValue()));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(new Phasor(hold9.doubleValue()));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(new Phasor(hold11.doubleValue()));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayl.get(i);
                                this.array.add(new Phasor(hold12.doubleValue()));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayl.get(i);
                                this.array.add(new Phasor(hold13.doubleValue()));
                                break;
                        case 14: Complex hold14 = (Complex)arrayl.get(i);
                                this.array.add(Conv.convert_Complex_to_Phasor(hold14));
                                break;
                        case 15: Phasor hold15 = (Phasor)arrayl.get(i);
                                this.array.add(hold15);
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new Phasor((double)((int)hold17.charValue())));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 3: this.type = 12;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayl.get(i);
                                this.array.add(Conv.convert_Double_to_BigDecimal(hold1));
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(Conv.convert_Float_to_BigDecimal(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(Conv.convert_Long_to_BigDecimal(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(Conv.convert_Integer_to_BigDecimal(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(Conv.convert_Short_to_BigDecimal(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(Conv.convert_Byte_to_BigDecimal(hold11));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayl.get(i);
                                this.array.add(hold12);
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayl.get(i);
                                this.array.add(Conv.convert_BigInteger_to_BigDecimal(hold13));
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new BigDecimal(hold17.toString()));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 4: this.type = 13;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayl.get(i);
                                this.array.add(Conv.convert_Double_to_BigInteger(hold1));
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(Conv.convert_Float_to_BigInteger(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(Conv.convert_Long_to_BigInteger(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(Conv.convert_Integer_to_BigInteger(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(Conv.convert_Short_to_BigInteger(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(Conv.convert_Byte_to_BigInteger(hold11));
                                break;
                        case 12: BigDecimal hold12 = (BigDecimal)arrayl.get(i);
                                this.array.add(Conv.convert_BigDecimal_to_BigInteger(hold12));
                                break;
                        case 13: BigInteger hold13 = (BigInteger)arrayl.get(i);
                                this.array.add(hold13);
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new BigInteger(hold17.toString()));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 5: this.type = 1;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 1: Double hold1 = (Double)arrayl.get(i);
                                this.array.add(hold1);
                                break;
                        case 3: Float hold3 = (Float)arrayl.get(i);
                                this.array.add(Conv.convert_Float_to_Double(hold3));
                                break;
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(Conv.convert_Long_to_Double(hold5));
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(Conv.convert_Integer_to_Double(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(Conv.convert_Short_to_Double(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(Conv.convert_Byte_to_Double(hold11));
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new Double(Double.parseDouble(hold17.toString())));
                                break;
                       default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 6: this.type = 7;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 5: Long hold5 = (Long)arrayl.get(i);
                                this.array.add(hold5);
                                break;
                        case 7: Integer hold7 = (Integer)arrayl.get(i);
                                this.array.add(Conv.convert_Integer_to_Long(hold7));
                                break;
                        case 9: Short hold9 = (Short)arrayl.get(i);
                                this.array.add(Conv.convert_Short_to_Long(hold9));
                                break;
                        case 11: Byte hold11 = (Byte)arrayl.get(i);
                                this.array.add(Conv.convert_Byte_to_Long(hold11));
                                break;
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(new Long((long)((int)hold17.charValue())));
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
            case 7: this.type = 7;
                    for(int i=0; i<this.length; i++){
                        switch(this.originalTypes[i]){
                        case 17: Character hold17 = (Character)arrayl.get(i);
                                this.array.add(hold17);
                                break;
                        default: throw new IllegalArgumentException("Data type not identified by this method");
                        }
                    }
                    break;
             default: throw new IllegalArgumentException("Dominant array data type not identified by this method");
        }

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16: this.minmax();
         }
    }

    // ARRAY LENGTH
    // retuns array length
    public int length(){
        return this.length;
    }


    // ARRAY TYPE
    // retuns array type as the index:
    // 0 double, 1 Double, 2 float, 3 Float, 4 long, 5 Long, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
    // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character or 18 String
    public int typeIndex(){
        return this.type;
    }

    // retuns array type as the name:
    // 0 double, 1 Double, 2 float, 3 Float, 4 long, 5 Long, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
    // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character or 18 String
    public String arrayType(){
        return (this.typeName[this.type] + "[]");
    }

    // retuns original array types, before conversion to common type if array entered as mixed types, as the names:
    // 0 double, 1 Double, 2 float, 3 Float, 4 long, 5 Long, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
    // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character or 18 String
    public String[] originalArrayTypes(){
        String[] ss = new String[this.length];
        for(int i=0; i<this.length; i++)ss[i] = this.typeName[this.originalTypes[i]];
        return ss;
    }

    // DEEP COPY
    // Copy to a new instance of ArrayMaths
    public ArrayMaths copy(){
        ArrayMaths am = new ArrayMaths();

        am.length = this.length;
        am.maxIndex = this.maxIndex;
        am.minIndex = this.minIndex;
        am.sumDone = this.sumDone;
        am.productDone = this.productDone;
        am.sumlongToDouble = this.sumlongToDouble;
        am.productlongToDouble = this.productlongToDouble;
        am.type = this.type;
        if(this.originalTypes==null){
            am.originalTypes = null;
        }
        else{
            am.originalTypes = Conv.copy(this.originalTypes);
        }
        if(this.sortedIndices==null){
            am.sortedIndices = null;
        }
        else{
            am.sortedIndices = Conv.copy(this.sortedIndices);
        }
        am.suppressMessages = this.suppressMessages;
        am.minmax = new ArrayList<Object>();
        if(this.minmax.size()!=0){
            switch(this.type){
            case 0:
            case 1: double dd = ((Double)this.minmax.get(0)).doubleValue();
                    am.minmax.add(new Double(dd));
                    dd = ((Double)this.minmax.get(1)).doubleValue();
                    am.minmax.add(new Double(dd));
                    break;
            case 4:
            case 5: long ll= ((Long)this.minmax.get(0)).longValue();
                    am.minmax.add(new Double(ll));
                    ll = ((Long)this.minmax.get(1)).longValue();
                    am.minmax.add(new Long(ll));
                    break;
            case 2:
            case 3: float ff = ((Float)this.minmax.get(0)).floatValue();
                    am.minmax.add(new Double(ff));
                    ff = ((Float)this.minmax.get(1)).floatValue();
                    am.minmax.add(new Double(ff));
                    break;
            case 6:
            case 7: int ii = ((Integer)this.minmax.get(0)).intValue();
                    am.minmax.add(new Integer(ii));
                    ii = ((Double)this.minmax.get(1)).intValue();
                    am.minmax.add(new Integer(ii));
                    break;
            case 8:
            case 9: short ss = ((Short)this.minmax.get(0)).shortValue();
                    am.minmax.add(new Short(ss));
                    ss = ((Double)this.minmax.get(1)).shortValue();
                    am.minmax.add(new Short((ss)));
                    break;
            case 10:
            case 11: byte bb = ((Byte)this.minmax.get(0)).byteValue();
                    am.minmax.add(new Byte(bb));
                    ss = ((Byte)this.minmax.get(1)).byteValue();
                    am.minmax.add(new Byte((bb)));
                    break;
            case 12: BigDecimal bd = (BigDecimal)this.minmax.get(0);
                    am.minmax.add(bd);
                    bd = (BigDecimal)this.minmax.get(1);
                    am.minmax.add(bd);
                    bd = null;
                    break;
            case 13: BigInteger bi = (BigInteger)this.minmax.get(0);
                    am.minmax.add(bi);
                    bi = (BigInteger)this.minmax.get(1);
                    am.minmax.add(bi);
                    bi = null;
                    break;
            case 16:
            case 17: int iii = ((Integer)this.minmax.get(0)).intValue();
                    am.minmax.add(new Integer(iii));
                    iii = ((Double)this.minmax.get(1)).intValue();
                    am.minmax.add(new Integer(iii));
                    break;
            }
        }

        am.summ = new ArrayList<Object>();
        if(this.summ.size()!=0){
            switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: double dd = ((Double)summ.get(0)).doubleValue();
                    am.summ.add(new Double(dd));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        double dd2 = ((Double)summ.get(0)).doubleValue();
                        am.summ.add(new Double(dd2));
                    }
                    else{
                        long ll = ((Long)summ.get(0)).longValue();
                        am.summ.add(new Long(ll));
                    }
                    break;
            case 12: BigDecimal bd = (BigDecimal)summ.get(0);
                    am.summ.add(bd);
                    break;
            case 13:  BigInteger bi = (BigInteger)summ.get(0);
                    am.summ.add(bi);
                    break;
            case 14:  Complex cc = (Complex)summ.get(0);
                    am.summ.add(cc);
                    break;
            case 15:  Phasor pp = (Phasor)summ.get(0);
                    am.summ.add(pp);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
            }
        }

        am.productt = new ArrayList<Object>();
        if(this.productt.size()!=0){
            switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: double dd = ((Double)productt.get(0)).doubleValue();
                    am.productt.add(new Double(dd));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        double dd2 = ((Double)productt.get(0)).doubleValue();
                        am.productt.add(new Double(dd2));
                    }
                    else{
                        long ll = ((Long)productt.get(0)).longValue();
                        am.productt.add(new Long(ll));
                    }
                    break;
            case 12: BigDecimal bd = (BigDecimal)productt.get(0);
                    am.productt.add(bd);
                    break;
            case 13:  BigInteger bi = (BigInteger)productt.get(0);
                    am.productt.add(bi);
                    break;
            case 14:  Complex cc = (Complex)productt.get(0);
                    am.productt.add(cc);
                    break;
            case 15:  Phasor pp = (Phasor)productt.get(0);
                    am.productt.add(pp);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
            }
        }


        switch(this.type){
            case 0:
            case 1: double[] dd = Conv.copy(this.getArray_as_double());
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]));
                    break;
            case 2:
            case 3: float[] ff = Conv.copy(this.getArray_as_float());
                    for(int i=0; i<this.length; i++)am.array.add(new Float(ff[i]));
                    break;
            case 4:
            case 5: long[] ll = Conv.copy(this.getArray_as_long());
                    for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]));
                    break;
            case 6:
            case 7: int[] ii = Conv.copy(this.getArray_as_int());
                    for(int i=0; i<this.length; i++)am.array.add(new Integer(ii[i]));
                    break;
            case 8:
            case 9: short[] ss = Conv.copy(this.getArray_as_short());
                    for(int i=0; i<this.length; i++)am.array.add(new Short(ss[i]));
                    break;
            case 10:
            case 11: byte[] bb = Conv.copy(this.getArray_as_byte());
                    for(int i=0; i<this.length; i++)am.array.add(new Byte(bb[i]));
                    break;
            case 12: BigDecimal[] bd = Conv.copy(this.getArray_as_BigDecimal());
                    for(int i=0; i<this.length; i++)am.array.add(bd[i]);
                    break;
            case 13: BigInteger[] bi = Conv.copy(this.getArray_as_BigInteger());
                    for(int i=0; i<this.length; i++)am.array.add(bi[i]);
                    break;
            case 14: Complex[] ccc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(ccc[i].copy());
                    break;
            case 15: Phasor[] ppp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(ppp[i].copy());
                    break;
            case 16:
            case 17: char[] cc = Conv.copy(this.getArray_as_char());
                    for(int i=0; i<this.length; i++)am.array.add(new Character(cc[i]));
                    break;
            case 18: String[] sss = Conv.copy(this.getArray_as_String());
                    for(int i=0; i<this.length; i++)am.array.add(sss[i]);
                    break;
        }

        return am;
    }

    
    // POSSIBLE LOSS OF PRECISION MESSAGE
    // Suppress possible loss of precisicion messages in an instance of ArrayMaths
    public void suppressMessages(){
        this.suppressMessages = true;
    }

    // Restore possible loss of precisicion messages in an instance of ArrayMaths
    public void restoreMessages(){
        this.suppressMessages = false;
    }

    // Suppress possible loss of precisicion messages for all instances in an application
    public static void suppressMessagesTotal(){
        Conv.suppressMessagesAM();
    }

    // Restore possible loss of precisicion messages
    public static void restoreMessagesTotal(){
        Conv.restoreMessagesAM();
    }

    // INTERNAL ARRAY
    // return internal array as double
    public double[] array(){
        return this.getArray_as_double();
    }

    public double[] array_as_double(){
        return this.getArray_as_double();
    }

    public double[] getArray_as_double(){
        if(this.suppressMessages)Conv.suppressMessages();
        double[] retArray = new double[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = ((Double)this.array.get(i)).doubleValue();
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_double((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_double((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_double((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_double((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_double((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = Double.valueOf(((String)this.array.get(i)));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Double
    public Double[] array_as_Double(){
        return this.getArray_as_Double();
    }

    public Double[] getArray_as_Double(){
        if(this.suppressMessages)Conv.suppressMessages();
        Double[] retArray = new Double[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = (Double)this.array.get(i);
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_Double((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_Double((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_Double((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_Double((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_Double((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Double((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Double((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Double((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_Double((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Float
    public Float[] array_as_Float(){
        return this.getArray_as_Float();
    }

    public Float[] getArray_as_Float(){
        if(this.suppressMessages)Conv.suppressMessages();
        Float[] retArray = new Float[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_Float((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = (Float)this.array.get(i);
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_Float((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_Float((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_Float((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_Float((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Float((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Float((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Float((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_Float((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15:  throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as float
    public float[] array_as_float(){
        return this.getArray_as_float();
    }

    public float[] getArray_as_float(){
        if(this.suppressMessages)Conv.suppressMessages();
        float[] retArray = new float[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_float((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = ((Float)this.array.get(i)).floatValue();
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_float((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_float((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_float((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_float((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_float((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_float((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (new Float((String)this.array.get(i))).floatValue();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_float((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as long
    public long[] array_as_long(){
        return this.getArray_as_long();
    }

    public long[] getArray_as_long(){
        if(this.suppressMessages)Conv.suppressMessages();
        long[] retArray = new long[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_long((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_long((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = ((Long)this.array.get(i)).longValue();
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_long((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_long((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_long((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_long((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_long((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (new Long((String)this.array.get(i))).longValue();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_long((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15:  throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Long
    public Long[] array_as_Long(){
        return this.getArray_as_Long();
    }

    public Long[] getArray_as_Long(){
        if(this.suppressMessages)Conv.suppressMessages();
        Long[] retArray = new Long[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_Long((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_Long((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = (Long)this.array.get(i);
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_Long((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_Long((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_Long((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Long((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Long((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Long((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_Long((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return internal array as Integer
    public Integer[] array_as_Integer(){
        return this.getArray_as_Integer();
    }

    public Integer[] getArray_as_Integer(){
        if(this.suppressMessages)Conv.suppressMessages();
        Integer[] retArray = new Integer[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_Integer((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_Integer((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_Integer((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = (Integer)this.array.get(i);
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_Integer((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_Integer((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Integer((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Integer((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Integer((String)this.array.get(i));
                    break;
             case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = new Integer((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Integer is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as int
    public int[] array_as_int(){
        return this.getArray_as_int();
    }

    public int[] getArray_as_int(){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] retArray = new int[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_int((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_int((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_int((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = ((Integer)this.array.get(i)).intValue();
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_int((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_int((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_int((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_int((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (new Integer((String)this.array.get(i))).intValue();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = (int)((Character)this.array.get(i)).charValue();
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to int is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as short
    public short[] array_as_short(){
        return this.getArray_as_short();
    }

    public short[] getArray_as_short(){
        if(this.suppressMessages)Conv.suppressMessages();
        short[] retArray = new short[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_short((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_short((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_short((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_short((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = ((Short)this.array.get(i)).shortValue();
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_short((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_short((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_short((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (new Short((String)this.array.get(i))).shortValue();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_short((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return internal array as Short
    public Short[] array_as_Short(){
        return this.getArray_as_Short();
    }

    public Short[] getArray_as_Short(){
        if(this.suppressMessages)Conv.suppressMessages();
        Short[] retArray = new Short[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_Short((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_Short((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_Short((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_Short((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = (Short)this.array.get(i);
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_Short((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Short((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Short((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Short((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_Short((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as byte
    public byte[] array_as_byte(){
        return this.getArray_as_byte();
    }

    public byte[] getArray_as_byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        byte[] retArray = new byte[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_byte((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_byte((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_byte((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_byte((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_byte((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = ((Byte)this.array.get(i)).byteValue();
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_byte((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_byte((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (new Byte((String)this.array.get(i))).byteValue();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_byte((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Byte
    public Byte[] array_as_Byte(){
        return this.getArray_as_Byte();
    }

    public Byte[] getArray_as_Byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        Byte[] retArray = new Byte[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_Byte((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_Byte((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_Byte((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_Byte((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_Byte((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = (Byte)this.array.get(i);
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_Byte((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_Byte((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new Byte((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_Byte((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as BigDecimal
    public BigDecimal[] array_as_BigDecimal(){
        return this.getArray_as_BigDecimal();
    }

    public BigDecimal[] getArray_as_BigDecimal(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigDecimal[] retArray = new BigDecimal[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_BigDecimal((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_BigDecimal((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_BigDecimal((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_BigDecimal((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_BigDecimal((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_BigDecimal((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = (BigDecimal)this.array.get(i);
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new BigDecimal((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_BigDecimal((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to BigDecimal is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as BigInteger
    public BigInteger[] array_as_BigInteger(){
        return this.getArray_as_BigInteger();
    }

    public BigInteger[] getArray_as_BigInteger(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigInteger[] retArray = new BigInteger[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Double_to_BigInteger((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Float_to_BigInteger((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Long_to_BigInteger((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Integer_to_BigInteger((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Short_to_BigInteger((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Byte_to_BigInteger((Byte)this.array.get(i));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = (BigInteger)this.array.get(i);
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = new BigInteger((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_int_to_BigInteger((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to BigInteger is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Complex
    public Complex[] array_as_Complex(){
        return this.getArray_as_Complex();
    }

    public Complex[] getArray_as_Complex(){
        if(this.suppressMessages)Conv.suppressMessages();
        Complex[] retArray = Complex.oneDarray(this.length);
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = new Complex(((Double)this.array.get(i)).doubleValue());
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = new Complex(((Float)this.array.get(i)).doubleValue());
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_Long_to_double((Long)this.array.get(i)));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_Integer_to_double((Integer)this.array.get(i)));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_Short_to_double((Short)this.array.get(i)));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_Byte_to_double((Byte)this.array.get(i)));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i)));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i)));
                    break;
            case 14: for(int i=0; i<this.length; i++)retArray[i] = (Complex)this.array.get(i);
                    break;
            case 15: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Phasor_to_Complex((Phasor)this.array.get(i));
                    break;
            case 18: for(int i=0; i<this.length; i++){
                        String ss = (String)this.array.get(i);
                        if(ss.indexOf('i')!=-1 || ss.indexOf('j')!=-1){
                            retArray[i] = Complex.valueOf(ss);
                        }
                        else{
                            retArray[i] = new Complex(Double.valueOf(ss));
                        }
                    }
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = new Complex(Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue()));
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Phasor
    public Phasor[] array_as_Phasor(){
        return this.getArray_as_Phasor();
    }

    public Phasor[] getArray_as_Phasor(){
        if(this.suppressMessages)Conv.suppressMessages();
        Phasor[] retArray = Phasor.oneDarray(this.length);
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(((Double)this.array.get(i)).doubleValue());
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(((Float)this.array.get(i)).doubleValue());
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_Long_to_double((Long)this.array.get(i)));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_Integer_to_double((Integer)this.array.get(i)));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_Short_to_double((Short)this.array.get(i)));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_Byte_to_double((Byte)this.array.get(i)));
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i)));
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i)));
                    break;
            case 14: for(int i=0; i<this.length; i++)retArray[i] = Conv.convert_Complex_to_Phasor((Complex)this.array.get(i));
                    break;
            case 15: for(int i=0; i<this.length; i++)retArray[i] = (Phasor)this.array.get(i);
                    break;
            case 18: for(int i=0; i<this.length; i++){
                        String ss = ((String)this.array.get(i)).trim();
                        if(ss.indexOf('<')!=-1 || ss.indexOf('L')!=-1){
                            retArray[i] = Phasor.valueOf(ss);
                        }
                        else{
                            retArray[i] = new Phasor(Double.valueOf(ss));
                        }
                    }
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = new Phasor(Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue()));
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Character
    public Character[] array_as_Character(){
        return this.getArray_as_Character();
    }

    public Character[] getArray_as_Character(){
        if(this.suppressMessages)Conv.suppressMessages();
        Character[] retArray = new Character[this.length];
        switch(this.type){
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = new Character((char)(((Integer)this.array.get(i)).intValue()));
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = (Character)this.array.get(i);
                    break;
            case 18: boolean test = true;
                    String[] ss = new String[this.length];
                    for(int i=0; i<this.length; i++){
                        ss[i] = ((String)this.array.get(i)).trim();
                        if(ss[i].length()>1){
                            test=false;
                            break;
                        }
                    }
                    if(test){
                        for(int i=0; i<this.length; i++)retArray[i] = new Character(ss[i].charAt(0));
                    }
                    else{
                        throw new IllegalArgumentException("The String array elements are too long to be converted to Character");
                    }
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to char is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return internal array as char
    public char[] array_as_char(){
        return this.getArray_as_char();
    }

    public char[] getArray_as_char(){
        if(this.suppressMessages)Conv.suppressMessages();
        char[] retArray = new char[this.length];
        switch(this.type){
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = (char)(((Integer)this.array.get(i)).intValue());
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = (((Character)this.array.get(i)).charValue());
                    break;
            case 18: boolean test = true;
                    String[] ss = new String[this.length];
                    for(int i=0; i<this.length; i++){
                        ss[i] = ((String)this.array.get(i)).trim();
                        if(ss[i].length()>1){
                            test=false;
                            break;
                        }
                    }
                    if(test){
                        for(int i=0; i<this.length; i++)retArray[i] = (ss[i].charAt(0));
                    }
                    else{
                        throw new IllegalArgumentException("The String array elements are too long to be converted to char");
                    }
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to char is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as String
    public String[] array_as_String(){
        return this.getArray_as_String();
    }

    public String[] getArray_as_String(){
        if(this.suppressMessages)Conv.suppressMessages();
        String[] retArray = new String[this.length];
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)retArray[i] = ((Double)this.array.get(i)).toString();
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)retArray[i] = ((Float)this.array.get(i)).toString();
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)retArray[i] = ((Long)this.array.get(i)).toString();
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)retArray[i] = ((Integer)this.array.get(i)).toString();
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)retArray[i] = ((Short)this.array.get(i)).toString();
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)retArray[i] = ((Byte)this.array.get(i)).toString();
                    break;
            case 12: for(int i=0; i<this.length; i++)retArray[i] = ((BigDecimal)this.array.get(i)).toString();
                    break;
            case 13: for(int i=0; i<this.length; i++)retArray[i] = ((BigInteger)this.array.get(i)).toString();
                    break;
            case 14: for(int i=0; i<this.length; i++)retArray[i] = ((Complex)this.array.get(i)).toString();
                    break;
            case 15: for(int i=0; i<this.length; i++)retArray[i] = ((Phasor)this.array.get(i)).toString();
                    break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)retArray[i] = ((Character)this.array.get(i)).toString();
                    break;
            case 18: for(int i=0; i<this.length; i++)retArray[i] = (String)this.array.get(i);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return internal array as Object
    public Object[] array_as_Object(){
        return this.getArray_as_Object();
    }

    public Object[] getArray_as_Object(){
        Object[] arrayo= new Object[this.length];
        for(int i=0; i<this.length; i++)arrayo[i] = this.array.get(i);
        return arrayo;
    }

    // return internal array as Vector
    public Vector array_as_Vector(){
        return this.getArray_as_Vector();
    }

    public Vector<Object> getArray_as_Vector(){
        Vector<Object> vec = new Vector<Object>(this.length);
        for(int i=0; i<this.length; i++)vec.addElement(array.get(i));
        return vec;
    }

    // return internal array as ArrayList
    public ArrayList array_as_ArrayList(){
        return this.getArray_as_ArrayList();
    }

    public ArrayList<Object> getArray_as_ArrayList(){
        ArrayList<Object> arrayl = new ArrayList<Object>(this.length);
        for(int i=0; i<this.length; i++)arrayl.add(array.get(i));
        return arrayl;
    }

    // return internal array as a Row Matrix, Matrix.rowMatrix
    public Matrix array_as_Matrix_rowMatrix(){
        return this.getArray_as_Matrix_rowMatrix();
    }

    public Matrix getArray_as_Matrix_rowMatrix(){
        if(this.suppressMessages)Conv.suppressMessages();
        Matrix mat = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 18:
            case 17: double[] dd = getArray_as_double();
                    mat = Matrix.rowMatrix(dd);
                    break;
            case 14: throw new IllegalArgumentException("Complex array cannot be converted to Matrix.rowMatrix - use method getArray_as_Complex_rowMatrix");
            case 15: throw new IllegalArgumentException("Phasor array cannot be converted to Matrix.rowMatrix - use method getArray_as_Phasor_rowMatrix");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return mat;
    }

    // return internal array as a Column Matrix, Matrix.columnMatrix
    public Matrix array_as_Matrix_columnMatrix(){
        return this.getArray_as_Matrix_columnMatrix();
    }

    public Matrix getArray_as_Matrix_columnMatrix(){
        if(this.suppressMessages)Conv.suppressMessages();
        Matrix mat = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 18:
            case 17: double[] dd = getArray_as_double();
                    mat = Matrix.columnMatrix(dd);
                    break;
            case 14: throw new IllegalArgumentException("Complex array cannot be converted to Matrix.columnMatrix - use method getArray_as_Complex_columnMatrix");
            case 15: throw new IllegalArgumentException("Phasor array cannot be converted to Matrix.columnMatrix - use method getArray_as_Phasor_columnMatrix");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return mat;
    }

    // return internal array as a Complex Row Matix, Complex.rowMatrix
    public ComplexMatrix array_as_Complex_rowMatrix(){
        return this.getArray_as_Complex_rowMatrix();
    }

    public ComplexMatrix getArray_as_Complex_rowMatrix(){
        Complex[] cc = this.getArray_as_Complex();
        ComplexMatrix mat = ComplexMatrix.rowMatrix(cc);
        return mat;
    }

    // return internal array as a Complex Column Matrix, Complex.columnMatrix
    public ComplexMatrix array_as_Complex_columnMatrix(){
        return this.getArray_as_Complex_columnMatrix();
    }

    public ComplexMatrix getArray_as_Complex_columnMatrix(){
        Complex[] cc = this.getArray_as_Complex();
        ComplexMatrix mat = ComplexMatrix.columnMatrix(cc);
        return mat;
    }

    // return v as a Phasor Row Matix, Phasor.rowMatrix
    public PhasorMatrix array_as_Phasor_rowMatrix(){
        return this.getArray_as_Phasor_rowMatrix();
    }

    public PhasorMatrix getArray_as_Phasor_rowMatrix(){
        Phasor[] cc = this.getArray_as_Phasor();
        PhasorMatrix mat = PhasorMatrix.rowMatrix(cc);
        return mat;
    }

    // return internal array as a Phasor Column Matrix, Phasor.columnMatrix
    public PhasorMatrix array_as_Phasor_columnMatrix(){
        return this.getArray_as_Phasor_columnMatrix();
    }

    public PhasorMatrix getArray_as_Phasor_columnMatrix(){
        Phasor[] cc = this.getArray_as_Phasor();
        PhasorMatrix mat = PhasorMatrix.columnMatrix(cc);
        return mat;
    }

    // return array of moduli of a Complex internal array
    public double[] array_as_modulus_of_Complex(){
        Complex[] cc = this.getArray_as_Complex();
        double[] mod = new double[this.length];
        for(int i=0; i<this.length; i++)mod[i] = cc[i].abs();
        return mod;
    }

    // return array of real parts of a Complex internal array
    public double[] array_as_real_part_of_Complex(){
        return this.getArray_as_real_part_of_Complex();
    }

    public double[] getArray_as_real_part_of_Complex(){
        Complex[] cc = this.getArray_as_Complex();
        double[] real = new double[this.length];
        for(int i=0; i<this.length; i++)real[i] = cc[i].getReal();
        return real;
    }

    // return array of imaginary parts of a Complex internal array
    public double[] array_as_imaginary_part_of_Complex(){
        return this.getArray_as_imaginay_part_of_Complex();
    }

    public double[] getArray_as_imaginay_part_of_Complex(){
        Complex[] cc = this.getArray_as_Complex();
        double[] imag = new double[this.length];
        for(int i=0; i<this.length; i++)imag[i] = cc[i].getImag();
        return imag;
    }

    // return array of magnitudes of a Phasor internal array
    public double[] array_as_magnitude_of_Phasor(){
        return this.getArray_as_magnitude_of_Phasor();
    }

    public double[] getArray_as_magnitude_of_Phasor(){
        Phasor[] pp = this.getArray_as_Phasor();
        double[] magn = new double[this.length];
        for(int i=0; i<this.length; i++)magn[i] = pp[i].getMagnitude();
        return magn;
    }

    // return array of phases (in degrees) of a Phasor internal array
    public double[] array_as_degrees_phase_of_Phasor(){
        return this.getArray_as_degrees_phase_of_Phasor();
    }

    public double[] getArray_as_degrees_phase_of_Phasor(){
        Phasor[] pp = this.getArray_as_Phasor();
        double[] phased = new double[this.length];
        for(int i=0; i<this.length; i++)phased[i] = pp[i].getPhaseInDegrees();
        return phased;
    }

    // return array of phases (in radians) of a Phasor internal array
    public double[] array_as_radians_phase_of_Phasor(){
        return this.getArray_as_radians_phase_of_Phasor();
    }

    public double[] getArray_as_radians_phase_of_Phasor(){
        Phasor[] pp = this.getArray_as_Phasor();
        double[] phaser = new double[this.length];
        for(int i=0; i<this.length; i++)phaser[i] = pp[i].getPhaseInRadians();
        return phaser;
    }



    // GET A SUB-ARRAY
    // first index of sub-array = start, last index of sub-array = end
    // return sub-array as double
    public double[] subarray_as_double(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        double[] retArray = new double[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = ((Double)this.array.get(i)).doubleValue();
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_double((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_double((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_double((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_double((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_double((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = Double.valueOf(((String)this.array.get(i)));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Double
    public Double[] subarray_as_Double(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Double[] retArray = new Double[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = (Double)this.array.get(i);
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_Double((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_Double((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_Double((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_Double((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_Double((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Double((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Double((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Double((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_Double((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Float
    public Float[] subarray_as_Float(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Float[] retArray = new Float[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_Float((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = (Float)this.array.get(i);
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_Float((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_Float((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_Float((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_Float((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Float((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Float((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Float((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_Float((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15:  throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as float
    public float[] subarray_as_float(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        float[] retArray = new float[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_float((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = ((Float)this.array.get(i)).floatValue();
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_float((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_float((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_float((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_float((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_float((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_float((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (new Float((String)this.array.get(i))).floatValue();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_float((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as long
    public long[] subarray_as_long(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        long[] retArray = new long[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_long((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_long((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = ((Long)this.array.get(i)).longValue();
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_long((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_long((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_long((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_long((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_long((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (new Long((String)this.array.get(i))).longValue();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_long((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15:  throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Long
    public Long[] subarray_as_Long(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Long[] retArray = new Long[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_Long((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_Long((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = (Long)this.array.get(i);
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_Long((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_Long((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_Long((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Long((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Long((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Long((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_Long((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return sub-array as Integer
    public Integer[] subarray_as_Integer(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Integer[] retArray = new Integer[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_Integer((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_Integer((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_Integer((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = (Integer)this.array.get(i);
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_Integer((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_Integer((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Integer((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Integer((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Integer((String)this.array.get(i));
                    break;
             case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = new Integer((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Integer is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as int
    public int[] subarray_as_int(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        int[] retArray = new int[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_int((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_int((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_int((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = ((Integer)this.array.get(i)).intValue();
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_int((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_int((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_int((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_int((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (new Integer((String)this.array.get(i))).intValue();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = (int)((Character)this.array.get(i)).charValue();
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to int is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as short
    public short[] subarray_as_short(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        short[] retArray = new short[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_short((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_short((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_short((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_short((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = ((Short)this.array.get(i)).shortValue();
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_short((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_short((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_short((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (new Short((String)this.array.get(i))).shortValue();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_short((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return sub-array as Short
    public Short[] subarray_as_Short(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Short[] retArray = new Short[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_Short((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_Short((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_Short((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_Short((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = (Short)this.array.get(i);
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_Short((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Short((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Short((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Short((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_Short((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as byte
    public byte[] subarray_as_byte(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        byte[] retArray = new byte[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_byte((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_byte((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_byte((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_byte((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_byte((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = ((Byte)this.array.get(i)).byteValue();
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_byte((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_byte((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (new Byte((String)this.array.get(i))).byteValue();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_byte((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Byte
    public Byte[] subarray_as_Byte(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Byte[] retArray = new Byte[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_Byte((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_Byte((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_Byte((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_Byte((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_Byte((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = (Byte)this.array.get(i);
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_Byte((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_Byte((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new Byte((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_Byte((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to Byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as BigDecimal
    public BigDecimal[] subarray_as_BigDecimal(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        BigDecimal[] retArray = new BigDecimal[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_BigDecimal((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_BigDecimal((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_BigDecimal((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_BigDecimal((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_BigDecimal((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_BigDecimal((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = (BigDecimal)this.array.get(i);
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new BigDecimal((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_BigDecimal((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to BigDecimal is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as BigInteger
    public BigInteger[] subarray_as_BigInteger(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        BigInteger[] retArray = new BigInteger[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Double_to_BigInteger((Double)this.array.get(i));
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Float_to_BigInteger((Float)this.array.get(i));
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Long_to_BigInteger((Long)this.array.get(i));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Integer_to_BigInteger((Integer)this.array.get(i));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Short_to_BigInteger((Short)this.array.get(i));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Byte_to_BigInteger((Byte)this.array.get(i));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.array.get(i));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = (BigInteger)this.array.get(i);
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = new BigInteger((String)this.array.get(i));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_int_to_BigInteger((int)((Character)this.array.get(i)).charValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to BigInteger is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Complex
    public Complex[] subarray_as_Complex(int start, int end){

        if(this.suppressMessages)Conv.suppressMessages();
        Complex[] retArray = Complex.oneDarray(this.length);
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(((Double)this.array.get(i)).doubleValue());
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(((Float)this.array.get(i)).doubleValue());
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_Long_to_double((Long)this.array.get(i)));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_Integer_to_double((Integer)this.array.get(i)));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_Short_to_double((Short)this.array.get(i)));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_Byte_to_double((Byte)this.array.get(i)));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i)));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i)));
                    break;
            case 14: for(int i=start; i<=end; i++)retArray[i-start] = (Complex)this.array.get(i);
                    break;
            case 15: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Phasor_to_Complex((Phasor)this.array.get(i));
                    break;
            case 18: for(int i=start; i<=end; i++){
                        String ss = (String)this.array.get(i);
                        if(ss.indexOf('i')!=-1 || ss.indexOf('j')!=-1){
                            retArray[i-start] = Complex.valueOf(ss);
                        }
                        else{
                            retArray[i-start] = new Complex(Double.valueOf(ss));
                        }
                    }
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = new Complex(Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue()));
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Phasor
    public Phasor[] subarray_as_Phasor(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Phasor[] retArray = Phasor.oneDarray(this.length);
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(((Double)this.array.get(i)).doubleValue());
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(((Float)this.array.get(i)).doubleValue());
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_Long_to_double((Long)this.array.get(i)));
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_Integer_to_double((Integer)this.array.get(i)));
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_Short_to_double((Short)this.array.get(i)));
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_Byte_to_double((Byte)this.array.get(i)));
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_BigDecimal_to_double((BigDecimal)this.array.get(i)));
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_BigInteger_to_double((BigInteger)this.array.get(i)));
                    break;
            case 14: for(int i=start; i<=end; i++)retArray[i-start] = Conv.convert_Complex_to_Phasor((Complex)this.array.get(i));
                    break;
            case 15: for(int i=start; i<=end; i++)retArray[i-start] = (Phasor)this.array.get(i);
                    break;
            case 18: for(int i=start; i<=end; i++){
                        String ss = ((String)this.array.get(i)).trim();
                        if(ss.indexOf('<')!=-1 || ss.indexOf('L')!=-1){
                            retArray[i-start] = Phasor.valueOf(ss);
                        }
                        else{
                            retArray[i-start] = new Phasor(Double.valueOf(ss));
                        }
                    }
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = new Phasor(Conv.convert_int_to_double((int)((Character)this.array.get(i)).charValue()));
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Character
    public Character[] subarray_as_Character(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Character[] retArray = new Character[end-start+1];
        switch(this.type){
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = new Character((char)(((Integer)this.array.get(i)).intValue()));
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = (Character)this.array.get(i);
                    break;
            case 18: boolean test = true;
                    String[] ss = new String[end-start+1];
                    for(int i=start; i<=end; i++){
                        ss[i-start] = ((String)this.array.get(i)).trim();
                        if(ss[i-start].length()>1){
                            test=false;
                            break;
                        }
                    }
                    if(test){
                        for(int i=start; i<=end; i++)retArray[i-start] = new Character(ss[i-start].charAt(0));
                    }
                    else{
                        throw new IllegalArgumentException("The String array elements are too long to be converted to Character");
                    }
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to char is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }


    // return sub-array as char
    public char[] subarray_as_char(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        char[] retArray = new char[end-start+1];
        switch(this.type){
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = (char)(((Integer)this.array.get(i)).intValue());
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = (((Character)this.array.get(i)).charValue());
                    break;
            case 18: boolean test = true;
                    String[] ss = new String[end-start+1];
                    for(int i=start; i<=end; i++){
                        ss[i-start] = ((String)this.array.get(i)).trim();
                        if(ss[i-start].length()>1){
                            test=false;
                            break;
                        }
                    }
                    if(test){
                        for(int i=start; i<=end; i++)retArray[i-start] = (ss[i-start].charAt(0));
                    }
                    else{
                        throw new IllegalArgumentException("The String array elements are too long to be converted to char");
                    }
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a conversion to char is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as String
    public String[] subarray_as_String(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        String[] retArray = new String[end-start+1];
        switch(this.type){
            case 0:
            case 1: for(int i=start; i<=end; i++)retArray[i-start] = ((Double)this.array.get(i)).toString();
                    break;
            case 2:
            case 3: for(int i=start; i<=end; i++)retArray[i-start] = ((Float)this.array.get(i)).toString();
                    break;
            case 4:
            case 5: for(int i=start; i<=end; i++)retArray[i-start] = ((Long)this.array.get(i)).toString();
                    break;
            case 6:
            case 7: for(int i=start; i<=end; i++)retArray[i-start] = ((Integer)this.array.get(i)).toString();
                    break;
            case 8:
            case 9: for(int i=start; i<=end; i++)retArray[i-start] = ((Short)this.array.get(i)).toString();
                    break;
            case 10:
            case 11: for(int i=start; i<=end; i++)retArray[i-start] = ((Byte)this.array.get(i)).toString();
                    break;
            case 12: for(int i=start; i<=end; i++)retArray[i-start] = ((BigDecimal)this.array.get(i)).toString();
                    break;
            case 13: for(int i=start; i<=end; i++)retArray[i-start] = ((BigInteger)this.array.get(i)).toString();
                    break;
            case 14: for(int i=start; i<=end; i++)retArray[i-start] = ((Complex)this.array.get(i)).toString();
                    break;
            case 15: for(int i=start; i<=end; i++)retArray[i-start] = ((Phasor)this.array.get(i)).toString();
                    break;
            case 16:
            case 17: for(int i=start; i<=end; i++)retArray[i-start] = ((Character)this.array.get(i)).toString();
                    break;
            case 18: for(int i=start; i<=end; i++)retArray[i-start] = (String)this.array.get(i);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return retArray;
    }

    // return sub-array as Object
    public Object[] subarray_as_Object(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Object[] arrayo= new Object[end-start+1];
        for(int i=start; i<=end; i++)arrayo[i-start] = this.array.get(i);
        return arrayo;
    }

    // return sub-array as Vector
    public Vector<Object> subarray_as_Vector(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Vector<Object> vec = new Vector<Object>(end-start+1);
        for(int i=start; i<=end; i++)vec.addElement(array.get(i));
        return vec;
    }

    // return sub-array as ArrayList
    public ArrayList<Object> subarray_as_ArrayList(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        ArrayList<Object> arrayl = new ArrayList<Object>(end-start+1);
        for(int i=start; i<=end; i++)arrayl.add(array.get(i));
        return arrayl;
    }

    // return sub-array as a Row Matrix, Matrix.rowMatrix
    public Matrix subarray_as_Matrix_rowMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Matrix mat = null;
        double[] retArray = new double[end-start+1];
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 18:
            case 17: double[] dd = getArray_as_double();
                    for(int i=start; i<=end; i++)retArray[i-start] = dd[i];
                    mat = Matrix.rowMatrix(retArray);
                    break;
            case 14: throw new IllegalArgumentException("Complex array cannot be converted to Matrix.rowMatrix - use method subarray_as_Complex_rowMatrix");
            case 15: throw new IllegalArgumentException("Phasor array cannot be converted to Matrix.rowMatrix - use method subarray_as_Phasor_rowMatrix");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return mat;
    }

    // return sub-array as a Column Matrix, Matrix.columnMatrix
    public Matrix subarray_as_Matrix_columnMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        if(this.suppressMessages)Conv.suppressMessages();
        Matrix mat = null;
        double[] retArray = new double[end-start+1];
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 18:
            case 17: double[] dd = getArray_as_double();
                    for(int i=start; i<=end; i++)retArray[i-start] = dd[i];
                    mat = Matrix.columnMatrix(retArray);
                    break;
            case 14: throw new IllegalArgumentException("Complex array cannot be converted to Matrix.columnMatrix - use method subarray_as_Complex_columnMatrix");
            case 15: throw new IllegalArgumentException("Phasor array cannot be converted to Matrix.columnMatrix - use method subarray_as_Phasor_columnMatrix");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return mat;
    }

    // return sub-array as a Complex Row Matix, Complex.rowMatrix
    public ComplexMatrix subarray_as_Complex_rowMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Complex[] cc = this.getArray_as_Complex();
        Complex[] retArray = new Complex[end-start+1];
        for(int i=start; i<=end; i++)retArray[i-start] = cc[i];
        ComplexMatrix mat = ComplexMatrix.rowMatrix(retArray);
        return mat;
    }

    // return sub-array as a Complex Column Matrix, Complex.columnMatrix
    public ComplexMatrix subarray_as_Complex_columnMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Complex[] cc = this.getArray_as_Complex();
        Complex[] retArray = new Complex[end-start+1];
        for(int i=start; i<=end; i++)retArray[i-start] = cc[i];
        ComplexMatrix mat = ComplexMatrix.columnMatrix(retArray);
        return mat;
    }

    // return sub-array as a Phasor Row Matix, Phasor.rowMatrix
    public PhasorMatrix subarray_as_Phasor_rowMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Phasor[] pp = this.getArray_as_Phasor();
        Phasor[] retArray = new Phasor[end-start+1];
        for(int i=start; i<=end; i++)retArray[i-start] = pp[i];
        PhasorMatrix mat = PhasorMatrix.rowMatrix(retArray);
        return mat;
    }

    // return sub-array as a Phasor Column Matrix, Phasor.columnMatrix
    public PhasorMatrix subarray_as_Phasor_columnMatrix(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Phasor[] pp = this.getArray_as_Phasor();
        Phasor[] retArray = new Phasor[end-start+1];
        for(int i=start; i<=end; i++)retArray[i-start] = pp[i];
        PhasorMatrix mat = PhasorMatrix.columnMatrix(retArray);
        return mat;
    }

    // return array of the moduli of a Complex sub-array
    public double[] subarray_as_modulus_of_Complex(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Complex[] cc = this.getArray_as_Complex();
        double[] real = new double[end-start+1];
        for(int i=start; i<=end; i++)real[i-start] = cc[i].abs();
        return real;
    }

    // return array of real parts of a Complex sub-array
    public double[] subarray_as_real_part_of_Complex(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Complex[] cc = this.getArray_as_Complex();
        double[] real = new double[end-start+1];
        for(int i=start; i<=end; i++)real[i-start] = cc[i].getReal();
        return real;
    }

    // return array of imaginary parts of a Complex sub-array
    public double[] subarray_as_imaginay_part_of_Complex(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Complex[] cc = this.getArray_as_Complex();
        double[] imag = new double[end-start+1];
        for(int i=start; i<=end; i++)imag[i-start] = cc[i].getImag();
        return imag;
    }

    // return array of magnitudes of a Phasor sub-array
    public double[] subarray_as_magnitude_of_Phasor(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Phasor[] pp = this.getArray_as_Phasor();
        double[] magn = new double[end-start+1];
        for(int i=start; i<=end; i++)magn[i-start] = pp[i].getMagnitude();
        return magn;
    }

    // return array of phases (in degrees) of a Phasor sub-array
    public double[] subarray_as_degrees_phase_of_Phasor(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Phasor[] pp = this.getArray_as_Phasor();
        double[] phased = new double[end-start+1];
        for(int i=start; i<=end; i++)phased[i-start] = pp[i].getPhaseInDegrees();
        return phased;
    }

    // return array of phases (in radians) of a Phasor sub-array
    public double[] subarray_as_radians_phase_of_Phasor(int start, int end){
        if(end>=this.length)throw new IllegalArgumentException("end, " + end + ", is greater than the highest index, " + (this.length-1));
        Phasor[] pp = this.getArray_as_Phasor();
        double[] phaser = new double[end-start+1];
        for(int i=start; i<=end; i++)phaser[i-start] = pp[i].getPhaseInRadians();
        return phaser;
    }



    // MAXIMUM AND MINIMUM
    // protected method to call search method for maximum and minimum values
    // called by public methods
    protected void minmax(){
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(this.getArray_as_Object(), this.minmax, maxminIndices, this.typeName, this.type);
        this.maxIndex = maxminIndices[0];
        this.minIndex = maxminIndices[1];
    }

    // protected method that finds the maximum and minimum values
    // called by protected method minmax which is called by public methods
    protected static void findMinMax(Object[] arrayo, ArrayList<Object> minmaxx, int[] maxminIndices, String[] aTypeName, int aType){
        int maxIndexx = 0;
        int minIndexx = 0;
        int arraylength = arrayo.length;
        switch(aType){
            case 0:
            case 1: double[] arrayD = new double[arraylength];
                    for(int i=0; i<arraylength; i++)arrayD[i] = ((Double)arrayo[i]).doubleValue();
                    double amaxD=arrayD[0];
                    double aminD=arrayD[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayD[i]>amaxD){
                            amaxD = arrayD[i];
                            maxIndexx = i;
                        }
                        if(arrayD[i]<aminD){
                            aminD = arrayD[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Double(amaxD));
                    minmaxx.add(new Double(aminD));
                    break;
            case 4:
            case 5: long[] arrayL = new long[arraylength];
                    for(int i=0; i<arraylength; i++)arrayL[i] = ((Long)arrayo[i]).longValue();
                    long amaxL=arrayL[0];
                    long aminL=arrayL[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayL[i]>amaxL){
                            amaxL = arrayL[i];
                            maxIndexx = i;
                        }
                        if(arrayL[i]<aminL){
                            aminL = arrayL[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Long(amaxL));
                    minmaxx.add(new Long(aminL));
                    break;
            case 2:
            case 3: float[] arrayF = new float[arraylength];
                    for(int i=0; i<arraylength; i++)arrayF[i] = ((Float)arrayo[i]).floatValue();
                    float amaxF=arrayF[0];
                    float aminF=arrayF[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayF[i]>amaxF){
                            amaxF = arrayF[i];
                            maxIndexx = i;
                        }
                        if(arrayF[i]<aminF){
                            aminF = arrayF[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Float(amaxF));
                    minmaxx.add(new Float(aminF));
                    break;
            case 6:
            case 7: int[] arrayI = new int[arraylength];
                    for(int i=0; i<arraylength; i++)arrayI[i] = ((Integer)arrayo[i]).intValue();
                    int amaxI=arrayI[0];
                    int aminI=arrayI[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayI[i]>amaxI){
                            amaxI = arrayI[i];
                            maxIndexx = i;
                        }
                        if(arrayI[i]<aminI){
                            aminI = arrayI[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Integer(amaxI));
                    minmaxx.add(new Integer(aminI));
                    break;
            case 8:
            case 9: short[] arrayS = new short[arraylength];
                    for(int i=0; i<arraylength; i++)arrayS[i] = ((Short)arrayo[i]).shortValue();
                    short amaxS=arrayS[0];
                    short aminS=arrayS[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayS[i]>amaxS){
                            amaxS = arrayS[i];
                            maxIndexx = i;
                        }
                        if(arrayS[i]<aminS){
                            aminS = arrayS[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Short(amaxS));
                    minmaxx.add(new Short(aminS));
                    break;
            case 10:
            case 11: byte[] arrayB = new byte[arraylength];
                    for(int i=0; i<arraylength; i++)arrayB[i] = ((Byte)arrayo[i]).byteValue();
                    byte amaxB=arrayB[0];
                    byte aminB=arrayB[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayB[i]>amaxB){
                            amaxB = arrayB[i];
                            maxIndexx = i;
                        }
                        if(arrayB[i]<aminB){
                            aminB = arrayB[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Byte(amaxB));
                    minmaxx.add(new Byte(aminB));
                    break;
            case 12: BigDecimal[] arrayBD = new BigDecimal[arraylength];
                    for(int i=0; i<arraylength; i++)arrayBD[i] = (BigDecimal)arrayo[i];
                    BigDecimal amaxBD = arrayBD[0];
                    BigDecimal aminBD = arrayBD[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayBD[i].compareTo(amaxBD)==1){
                            amaxBD = arrayBD[i];
                            maxIndexx = i;
                        }
                        if(arrayBD[i].compareTo(aminBD)==-1){
                            aminBD = arrayBD[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(amaxBD);
                    minmaxx.add(aminBD);
                    break;
            case 13: BigInteger[] arrayBI= new BigInteger[arraylength];
                    for(int i=0; i<arraylength; i++)arrayBI[i] = (BigInteger)arrayo[i];
                    BigInteger amaxBI = arrayBI[0];
                    BigInteger aminBI = arrayBI[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayBI[i].compareTo(amaxBI)==1){
                            amaxBI = arrayBI[i];
                            maxIndexx = i;
                        }
                        if(arrayBI[i].compareTo(aminBI)==-1){
                            aminBI = arrayBI[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(amaxBI);
                    minmaxx.add(aminBI);
                    break;
            case 16:
            case 17: int[] arrayInt = new int[arraylength];
                    for(int i=0; i<arraylength; i++)arrayInt[i] = (int)(((Character)arrayo[i]).charValue());
                    int amaxInt=arrayInt[0];
                    int aminInt=arrayInt[0];
                    maxIndexx = 0;
                    minIndexx = 0;
                    for(int i=1; i<arraylength; i++){
                        if(arrayInt[i]>amaxInt){
                            amaxInt = arrayInt[i];
                            maxIndexx = i;
                        }
                        if(arrayInt[i]<aminInt){
                            aminInt = arrayInt[i];
                            minIndexx = i;
                        }
                    }
                    minmaxx.add(new Character((char)amaxInt));
                    minmaxx.add(new Character((char)aminInt));
                    break;
            case 14:
            case 15:
            case 18: System.out.println("ArrayMaths:  getMaximum_... or getMinimum_... (findMinMax): the " + aTypeName[aType] + " is not a numerical type for which a maximum or a minimum is meaningful/supported");
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        maxminIndices[0] = maxIndexx;
        maxminIndices[1] = minIndexx;
    }

    // Return maximum
    public double maximum(){
        return this.getMaximum_as_double();
    }

    public double maximum_as_double(){
        return this.getMaximum_as_double();
    }

    public double getMaximum(){
        return this.getMaximum_as_double();
    }

    public double getMaximum_as_double(){
        if(this.suppressMessages)Conv.suppressMessages();
        double max = 0.0D;
        switch(this.type){
            case 0:
            case 1: max = ((Double)this.minmax.get(0)).doubleValue();
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_double((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_double((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_double((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_double((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_double((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_double((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_double((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    public Double maximum_as_Double(){
        return this.getMaximum_as_Double();
    }

    public Double getMaximum_as_Double(){
        if(this.suppressMessages)Conv.suppressMessages();
        Double max = new Double(0.0D);
        switch(this.type){
            case 0:
            case 1: max = (Double)this.minmax.get(0);
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_Double((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_Double((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_Double((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_Double((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_Double((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Double((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Double((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Float maximum_as_Float(){
        return this.getMaximum_as_Float();
    }

    public Float getMaximum_as_Float(){
        if(this.suppressMessages)Conv.suppressMessages();
        Float max = new Float(0.0D);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_Float((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = (Float)this.minmax.get(0);
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_Float((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_Float((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_Float((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_Float((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Float((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Float((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }


    // Return maximum
    public float maximum_as_float(){
        return this.getMaximum_as_float();
    }

    public float getMaximum_as_float(){
        if(this.suppressMessages)Conv.suppressMessages();
        float max = 0.0F;
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_float((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = ((Float)this.minmax.get(0)).floatValue();
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_float((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_float((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_float((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_float((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_float((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_float((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public long maximum_as_long(){
        return this.getMaximum_as_long();
    }

    public long getMaximum_as_long(){
        if(this.suppressMessages)Conv.suppressMessages();
        long max = 0L;
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_long((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_long((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = ((Long)this.minmax.get(0)).longValue();
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_long((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_long((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_long((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_long((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_long((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Long maximum_as_Long(){
        return this.getMaximum_as_Long();
    }

    public Long getMaximum_as_Long(){
        if(this.suppressMessages)Conv.suppressMessages();
        Long max = new Long(0L);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_Long((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_Long((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = (Long)this.minmax.get(0);
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_Long((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_Long((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_Long((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Long((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Long((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public int maximum_as_int(){
        return this.getMaximum_as_int();
    }

    public int getMaximum_as_int(){
        if(this.suppressMessages)Conv.suppressMessages();
        int max = 0;
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_int((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_int((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_int((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = ((Integer)this.minmax.get(0)).intValue();
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_int((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_int((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_int((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_int((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Integer maximum_as_Integer(){
        return this.getMaximum_as_Integer();
    }

    public Integer getMaximum_as_Integer(){
        if(this.suppressMessages)Conv.suppressMessages();
        Integer max = new Integer(0);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_Integer((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_Integer((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_Integer((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = (Integer)this.minmax.get(0);
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_Integer((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_Integer((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Integer((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Integer((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public short maximum_as_short(){
        return this.getMaximum_as_short();
    }

    public short getMaximum_as_short(){
        if(this.suppressMessages)Conv.suppressMessages();
        short max = 0;
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_short((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_short((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_short((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_short((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = ((Short)this.minmax.get(0)).shortValue();
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_short((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_short((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_short((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Short maximum_as_Short(){
        return this.getMaximum_as_Short();
    }

    public Short getMaximum_as_Short(){
        if(this.suppressMessages)Conv.suppressMessages();
        Short max = new Short((short)0);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_Short((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_Short((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_Short((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_Short((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = (Short)this.minmax.get(0);
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_Short((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Short((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Short((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public byte maximum_as_byte(){
        return this.getMaximum_as_byte();
    }

    public byte getMaximum_as_byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        byte max = 0;
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_byte((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_byte((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_byte((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_byte((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_byte((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = ((Byte)this.minmax.get(0)).byteValue();
                    break;
            case 12: max = Conv.convert_BigDecimal_to_byte((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_byte((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Byte maximum_as_Byte(){
        return this.getMaximum_as_Byte();
    }

    public Byte getMaximum_as_Byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        Byte max = new Byte((byte)0);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_Byte((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_Byte((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_Byte((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_Byte((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_Byte((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = (Byte)this.minmax.get(0);
                    break;
            case 12: max = Conv.convert_BigDecimal_to_Byte((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = Conv.convert_BigInteger_to_Byte((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public BigDecimal maximum_as_BigDecimal(){
        return this.getMaximum_as_BigDecimal();
    }

    public BigDecimal getMaximum_as_BigDecimal(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigDecimal max = new BigDecimal(0.0D);
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_BigDecimal((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_BigDecimal((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_BigDecimal((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_BigDecimal((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_BigDecimal((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_BigDecimal((Byte)this.minmax.get(0));
                    break;
            case 12: max = (BigDecimal)this.minmax.get(0);
                    break;
            case 13: max = Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.minmax.get(0));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public BigInteger maximum_as_BigInteger(){
        return this.getMaximum_as_BigInteger();
    }

    public BigInteger getMaximum_as_BigInteger(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigInteger max = new BigInteger("0");
        switch(this.type){
            case 0:
            case 1: max = Conv.convert_Double_to_BigInteger((Double)this.minmax.get(0));
                    break;
            case 2:
            case 3: max = Conv.convert_Float_to_BigInteger((Float)this.minmax.get(0));
                    break;
            case 4:
            case 5: max = Conv.convert_Long_to_BigInteger((Long)this.minmax.get(0));
                    break;
            case 6:
            case 7: max = Conv.convert_Integer_to_BigInteger((Integer)this.minmax.get(0));
                    break;
            case 8:
            case 9: max = Conv.convert_Short_to_BigInteger((Short)this.minmax.get(0));
                    break;
            case 10:
            case 11: max = Conv.convert_Byte_to_BigInteger((Byte)this.minmax.get(0));
                    break;
            case 12: max = Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.minmax.get(0));
                    break;
            case 13: max = (BigInteger)this.minmax.get(0);
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }


    // Return maximum
    public char maximum_as_char(){
        return this.getMaximum_as_char();
    }

    public char getMaximum_as_char(){
    if(this.suppressMessages)Conv.suppressMessages();
        char max = '\u0000';
        switch(this.type){
            case 6:
            case 7: max = (char)((Integer)this.minmax.get(1)).intValue();
                    break;
            case 16:
            case 17: max = ((Character)this.minmax.get(1)).charValue();
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a char type maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return maximum
    public Character maximum_as_Character(){
        return this.getMaximum_as_Character();
    }

    public Character getMaximum_as_Character(){
        if(this.suppressMessages)Conv.suppressMessages();
        Character max = new Character('\u0000');
        switch(this.type){
            case 6:
            case 7: max = new Character((char)((Integer)this.minmax.get(1)).intValue());
                    break;
            case 16:
            case 17: max = (Character)this.minmax.get(1);
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a Character type maximum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return max;
    }

    // Return minimum
    public double minimum(){
        return this.getMinimum_as_double();
    }

    public double minimum_as_double(){
        return this.getMinimum_as_double();
    }

    public double getMinimum(){
        return this.getMinimum_as_double();
    }

    public double getMinimum_as_double(){
        if(this.suppressMessages)Conv.suppressMessages();
        double min = 0.0D;
        switch(this.type){
            case 0:
            case 1: min = ((Double)this.minmax.get(1)).doubleValue();
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_double((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_double((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_double((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_double((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_double((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_double((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_double((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Double minimum_as_Double(){
        return this.getMinimum_as_Double();
    }

    public Double getMinimum_as_Double(){
        if(this.suppressMessages)Conv.suppressMessages();
        Double min = new Double(0.0D);
        switch(this.type){
            case 0:
            case 1: min = (Double)this.minmax.get(1);
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_Double((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_Double((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_Double((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_Double((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_Double((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Double((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Double((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Float minimum_as_Float(){
        return this.getMinimum_as_Float();
    }

    public Float getMinimum_as_Float(){
        if(this.suppressMessages)Conv.suppressMessages();
        Float min = new Float(0.0D);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_Float((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = (Float)this.minmax.get(1);
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_Float((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_Float((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_Float((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_Float((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Float((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Float((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public float minimum_as_float(){
        return this.getMinimum_as_float();
    }

    public float getMinimum_as_float(){
        if(this.suppressMessages)Conv.suppressMessages();
        float min = 0.0F;
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_float((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = ((Float)this.minmax.get(1)).floatValue();
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_float((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_float((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_float((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_float((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_float((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_float((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public long minimum_as_long(){
        return this.getMinimum_as_long();
    }

    public long getMinimum_as_long(){
        if(this.suppressMessages)Conv.suppressMessages();
        long min = 0L;
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_long((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_long((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = ((Long)this.minmax.get(1)).longValue();
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_long((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_long((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_long((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_long((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_long((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Long minimum_as_Long(){
        return this.getMinimum_as_Long();
    }

    public Long getMinimum_as_Long(){
        if(this.suppressMessages)Conv.suppressMessages();
        Long min = new Long(0L);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_Long((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_Long((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = (Long)this.minmax.get(1);
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_Long((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_Long((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_Long((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Long((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Long((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public int minimum_as_int(){
        return this.getMinimum_as_int();
    }

    public int getMinimum_as_int(){
        if(this.suppressMessages)Conv.suppressMessages();
        int min = 0;
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_int((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_int((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_int((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = ((Integer)this.minmax.get(1)).intValue();
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_int((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_int((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_int((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_int((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Integer minimum_as_Integer(){
        return this.getMinimum_as_Integer();
    }

    public Integer getMinimum_as_Integer(){
        if(this.suppressMessages)Conv.suppressMessages();
        Integer min = new Integer(0);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_Integer((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_Integer((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_Integer((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = (Integer)this.minmax.get(1);
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_Integer((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_Integer((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Integer((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Integer((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public short minimum_as_short(){
        return this.getMinimum_as_short();
    }

    public short getMinimum_as_short(){
        if(this.suppressMessages)Conv.suppressMessages();
        short min = 0;
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_short((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_short((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_short((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_short((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = ((Short)this.minmax.get(1)).shortValue();
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_short((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_short((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_short((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Short minimum_as_Short(){
        return this.getMinimum_as_Short();
    }

    public Short getMinimum_as_Short(){
        if(this.suppressMessages)Conv.suppressMessages();
        Short min = new Short((short)0);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_Short((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_Short((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_Short((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_Short((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = (Short)this.minmax.get(1);
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_Short((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Short((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Short((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public byte minimum_as_byte(){
        return this.getMinimum_as_byte();
    }

    public byte getMinimum_as_byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        byte min = 0;
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_byte((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_byte((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_byte((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_byte((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_byte((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = ((Byte)this.minmax.get(1)).byteValue();
                    break;
            case 12: min = Conv.convert_BigDecimal_to_byte((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_byte((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Byte minimum_as_Byte(){
        return this.getMinimum_as_Byte();
    }

    public Byte getMinimum_as_Byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        Byte min = new Byte((byte)0);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_Byte((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_Byte((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_Byte((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_Byte((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_Byte((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = (Byte)this.minmax.get(1);
                    break;
            case 12: min = Conv.convert_BigDecimal_to_Byte((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = Conv.convert_BigInteger_to_Byte((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public BigDecimal minimum_as_BigDecimal(){
        return this.getMinimum_as_BigDecimal();
    }

    public BigDecimal getMinimum_as_BigDecimal(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigDecimal min = new BigDecimal(0.0D);
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_BigDecimal((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_BigDecimal((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_BigDecimal((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_BigDecimal((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_BigDecimal((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_BigDecimal((Byte)this.minmax.get(1));
                    break;
            case 12: min = (BigDecimal)this.minmax.get(1);
                    break;
            case 13: min = Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.minmax.get(1));
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public BigInteger minimum_as_BigInteger(){
        return this.getMinimum_as_BigInteger();
    }

    public BigInteger getMinimum_as_BigInteger(){
        if(this.suppressMessages)Conv.suppressMessages();
        BigInteger min = new BigInteger("0");
        switch(this.type){
            case 0:
            case 1: min = Conv.convert_Double_to_BigInteger((Double)this.minmax.get(1));
                    break;
            case 2:
            case 3: min = Conv.convert_Float_to_BigInteger((Float)this.minmax.get(1));
                    break;
            case 4:
            case 5: min = Conv.convert_Long_to_BigInteger((Long)this.minmax.get(1));
                    break;
            case 6:
            case 7: min = Conv.convert_Integer_to_BigInteger((Integer)this.minmax.get(1));
                    break;
            case 8:
            case 9: min = Conv.convert_Short_to_BigInteger((Short)this.minmax.get(1));
                    break;
            case 10:
            case 11: min = Conv.convert_Byte_to_BigInteger((Byte)this.minmax.get(1));
                    break;
            case 12: min = Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.minmax.get(1));
                    break;
            case 13: min = (BigInteger)this.minmax.get(1);
                    break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public char minimum_as_char(){
        return this.getMinimum_as_char();
    }

    public char getMinimum_as_char(){
        if(this.suppressMessages)Conv.suppressMessages();
        char min = '\u0000';
        switch(this.type){
            case 6:
            case 7: min = (char)((Integer)this.minmax.get(1)).intValue();
                    break;
            case 16:
            case 17: min = ((Character)this.minmax.get(1)).charValue();
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a char type minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return minimum
    public Character minimum_as_Character(){
        return this.getMinimum_as_Character();
    }

    public Character getMinimum_as_Character(){
        if(this.suppressMessages)Conv.suppressMessages();
        Character min = new Character('\u0000');
        switch(this.type){
            case 6:
            case 7: min = new Character((char)((Integer)this.minmax.get(1)).intValue());
                    break;
            case 16:
            case 17: min = (Character)this.minmax.get(1);
                    break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 18: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a Character type minimum is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return min;
    }

    // Return index of the maximum
    public int maximumIndex(){
        return this.maxIndex;
    }

    public int getMaximumIndex(){
        return this.maxIndex;
    }

    // Return index of the minimum
    public int minimumIndex(){
        return this.minIndex;
    }

    public int getMinimumIndex(){
        return this.minIndex;
    }

    // Returns true if all array elements are, arithmetically, integers,
    // returns false if any array element is not, arithmetically, an integer
    public boolean isInteger(){
        boolean test = false;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: double[] arrayd = this.getArray_as_double();
                    test = Fmath.isInteger(arrayd);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 16:
            case 17: test = true;
                    break;
            case 12: BigDecimal[] arraybd = this.getArray_as_BigDecimal();
                    test = Fmath.isInteger(arraybd);
                    break;
            case 14:
            case 15: test = false;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        return test;
    }



    // ADDITION
    // add a constant to the elements of the internal array
    public ArrayMaths plus(double constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + constant));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal(constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).add(new BigDecimal(constant));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus(constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Complex(constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Double.toString(constant));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be added to a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Double constant){
        return this.plus(constant.doubleValue());
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(double[] arrayD){
        if(this.length!=arrayD.length)throw new IllegalArgumentException("The length of the argument array, " + arrayD.length + ", and the length of this instance internal array, " + this.length + ", must be equal");
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + arrayD[i]));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal(arrayD[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).add(new BigDecimal(arrayD[i]));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus(arrayD[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor(arrayD[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Double.toString(arrayD[i]));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be added to a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    public ArrayMaths plus(Double[] arrayD){
        int nArg = arrayD.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        double[] arrayd = new double[this.length];
        for(int i=0; i<this.length; i++)arrayd[i] = arrayD[i].doubleValue();
        return this.plus(arrayd);
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(float constant){
        double constantd = constant;
        return this.plus(constantd);
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Float constant){
        return this.plus(constant.floatValue());
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(float[] arrayF){
        if(this.length!=arrayF.length)throw new IllegalArgumentException("The length of the argument array, " + arrayF.length + ", and the length of this instance internal array, " + this.length + ", must be equal");
         double[] arrayD = new double[this.length];
         for(int i=0; i<this.length; i++)arrayD[i] = (double)arrayF[i];
         return this.plus(arrayD);
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Float[] arrayF){
        int nArg = arrayF.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        double[] arrayd = new double[this.length];
        for(int i=0; i<this.length; i++)arrayd[i] = arrayF[i].doubleValue();
        return this.plus(arrayd);
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(long constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)constant));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Long.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Long.toString(constant));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be added to a char");
            case 17: throw new IllegalArgumentException("a long cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Long constant){
        long constantl =  constant.longValue();
        return this.plus(constantl);
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(long[] arrayL){
        int nArg = arrayL.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)arrayL[i]));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max1 =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayL);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max1)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + arrayL[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)arrayL[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)arrayL[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Long.toString(arrayL[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)arrayL[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)arrayL[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Long.toString(arrayL[i]));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be added to a char");
            case 17: throw new IllegalArgumentException("a long cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Long[] arrayL){
        int nArg = arrayL.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        long[] arrayl = new long[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayL[i].longValue();
        return this.plus(arrayl);
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(int constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(constant));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be added to a char");
            case 17: throw new IllegalArgumentException("an int cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();

        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Integer constant){
        int constantl =  constant.intValue();
        return this.plus(constantl);
    }


    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(int[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    int maxi2 = am22.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString(arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(arrayI[i]));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be added to a char");
            case 17: throw new IllegalArgumentException("an int cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Integer[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        int[] arrayl = new int[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].intValue();
        return this.plus(arrayl);
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(short constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + (long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + (int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(constant));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a short cannot be added to a char");
            case 17: throw new IllegalArgumentException("a short cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Short constant){
        short constantl = constant.shortValue();
        return this.plus(constantl);
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(short[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + (long)arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    short maxi2 = am22.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + (int)arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString((int)arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(arrayI[i]));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a short cannot be added to a char");
            case 17: throw new IllegalArgumentException("a short cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Short[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        short[] arrayl = new short[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].shortValue();
        return this.plus(arrayl);
    }


    // add a constant to the elements of the internal array
    public ArrayMaths plus(BigDecimal constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(constant));
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(constant.doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(new Phasor(constant.doubleValue())));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ constant.toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be added to a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(byte constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + (long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + (int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(constant));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be added to a char");
            case 17: throw new IllegalArgumentException("a byte cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Byte constant){
        byte constantl =  constant.byteValue();
        return this.plus(constantl);
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(byte[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] + (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] + (long)arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    byte maxi2 = am22.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] + (int)arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] + (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.add(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.add(new BigInteger((Integer.toString((int)arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).plus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).plus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ Integer.toString(arrayI[i]));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be added to a char");
            case 17: throw new IllegalArgumentException("a byte cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Byte[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        byte[] arrayl = new byte[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].byteValue();
        return this.plus(arrayl);
    }


    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(BigDecimal[] arrayBD){
        int nArg = arrayBD.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(arrayBD[i]));
                    Conv.restoreMessages();
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(arrayBD[i].doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(new Phasor(arrayBD[i].doubleValue())));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ arrayBD[i].toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be added to a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(BigInteger constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal constantBD = Conv.convert_BigInteger_to_BigDecimal(constant);
                    BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(constantBD));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].add(constant));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(Conv.convert_BigInteger_to_double(constant)));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(new Phasor(Conv.convert_BigInteger_to_double(constant))));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ constant.toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be added to a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(BigInteger[] arrayBI){
        int nArg = arrayBI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(Conv.convert_BigInteger_to_BigDecimal(arrayBI[i])));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].add(arrayBI[i]));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(Conv.convert_BigInteger_to_double(arrayBI[i])));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(new Phasor(Conv.convert_BigInteger_to_double(arrayBI[i]))));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ arrayBI[i].toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be added to a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Complex constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(constant));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(Conv.convert_Complex_to_Phasor(constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ constant.toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be added to a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Complex[] arrayC){
        int nArg = arrayC.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(arrayC[i]));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(Conv.convert_Complex_to_Phasor(arrayC[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ arrayC[i].toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be added to a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }


    // add a constant to the elements of the internal array
    public ArrayMaths plus(Phasor constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(constant));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(Conv.convert_Phasor_to_Complex(constant)));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ constant.toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be added to a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Phasor[] arrayP){
        int nArg = arrayP.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].plus(arrayP[i]));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].plus(Conv.convert_Phasor_to_Complex(arrayP[i])));
                    am.type = this.type;
                    break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(i)+ arrayP[i].toString());
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be added to a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be added to a Character");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(String constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + constant);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }


    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(String[] arraySt){
        int nArg = arraySt.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + arraySt[i]);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(char constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + constant);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(char[] arrayCh){
        int nArg = arrayCh.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + arrayCh[i]);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add a constant to the elements of the internal array
    public ArrayMaths plus(Character constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + constant);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Character[] arrayCh){
        int nArg = arrayCh.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss = this.getArray_as_String();
                    for(int i=0; i<this.length; i++)am.array.add(ss[i] + arrayCh[i]);
                    am.type = 18;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        return am;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Vector<Object> vec){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am1 = new ArrayMaths();
        ArrayMaths am2 = new ArrayMaths(vec);

        switch(am2.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = am2.getArray_as_double();
                    am1 = this.plus(dd);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: long[] ll = am2.getArray_as_long();
                    am1 = this.plus(ll);
                    break;
            case 12: BigDecimal[] bd = am2.getArray_as_BigDecimal();
                    am1 = this.plus(bd);
                    break;
            case 13: BigInteger[] bi = am2.getArray_as_BigInteger();
                    am1 = this.plus(bi);
                    break;
            case 14: Complex[] cc = am2.getArray_as_Complex();
                    am1 = this.plus(cc);
                    break;
            case 15: Phasor[] pp = am2.getArray_as_Phasor();
                    am1 = this.plus(pp);
                    break;
            case 16:
            case 17: Character[] ct = am2.getArray_as_Character();
                    am1 = this.plus(ct);
                    break;
            case 18: String[] st = am2.getArray_as_String();
                    am1 = this.plus(st);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am1.getArray_as_Object(), am1.minmax, maxminIndices, am1.typeName, am1.type);
        am1.maxIndex = maxminIndices[0];
        am1.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am1;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(ArrayList<Object> list){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am1 = new ArrayMaths();
        ArrayMaths am2 = new ArrayMaths(list);

        switch(am2.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = am2.getArray_as_double();
                    am1 = this.plus(dd);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: long[] ll = am2.getArray_as_long();
                    am1 = this.plus(ll);
                    break;
            case 12: BigDecimal[] bd = am2.getArray_as_BigDecimal();
                    am1 = this.plus(bd);
                    break;
            case 13: BigInteger[] bi = am2.getArray_as_BigInteger();
                    am1 = this.plus(bi);
                    break;
            case 14: Complex[] cc = am2.getArray_as_Complex();
                    am1 = this.plus(cc);
                    break;
            case 15: Phasor[] pp = am2.getArray_as_Phasor();
                    am1 = this.plus(pp);
                    break;
            case 16:
            case 17: Character[] ct = am2.getArray_as_Character();
                    am1 = this.plus(ct);
                    break;
            case 18: String[] st = am2.getArray_as_String();
                    am1 = this.plus(st);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am1.getArray_as_Object(), am1.minmax, maxminIndices, am1.typeName, am1.type);
        am1.maxIndex = maxminIndices[0];
        am1.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am1;
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(ArrayMaths arrayM){
        ArrayList<Object> arrayl = arrayM.getArray_as_ArrayList();
        return this.plus(arrayl);
    }

    // add the elements of an array to the elements of the internal array
    public ArrayMaths plus(Stat arrayS){
        ArrayList<Object> arrayl = arrayS.getArray_as_ArrayList();
        return this.plus(arrayl);
    }

    // SUBTRACTION
    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(double constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - constant));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal(constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).subtract(new BigDecimal(constant));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus(constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a double or float cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Double constant){
        return this.minus(constant.doubleValue());
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(double[] arrayD){
        if(this.length!=arrayD.length)throw new IllegalArgumentException("The length of the argument array, " + arrayD.length + ", and the length of this instance internal array, " + this.length + ", must be equal");
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - arrayD[i]));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal(arrayD[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).subtract(new BigDecimal(arrayD[i]));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus(arrayD[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor(arrayD[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a double or float cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Double[] arrayD){
        int nArg = arrayD.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        double[] arrayd = new double[this.length];
        for(int i=0; i<this.length; i++)arrayd[i] = arrayD[i].doubleValue();
        return this.minus(arrayd);
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(float constant){
        double constantd = constant;
        return this.minus(constantd);
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Float constant){
        return this.minus(constant.floatValue());
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(float[] arrayF){
        if(this.length!=arrayF.length)throw new IllegalArgumentException("The length of the argument array, " + arrayF.length + ", and the length of this instance internal array, " + this.length + ", must be equal");
         double[] arrayD = new double[this.length];
         for(int i=0; i<this.length; i++)arrayD[i] = (double)arrayF[i];
         return this.minus(arrayD);
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Float[] arrayF){
        int nArg = arrayF.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        double[] arrayd = new double[this.length];
        for(int i=0; i<this.length; i++)arrayd[i] = arrayF[i].doubleValue();
        return this.minus(arrayd);
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(long constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)constant));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Long.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a long cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a long cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Long constant){
        long constantl =  constant.longValue();
        return this.minus(constantl);
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(long[] arrayL){
        int nArg = arrayL.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)arrayL[i]));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max1 =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayL);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max1)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - arrayL[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)arrayL[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)arrayL[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Long.toString(arrayL[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)arrayL[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)arrayL[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a long cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a long cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Long[] arrayL){
        int nArg = arrayL.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        long[] arrayl = new long[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayL[i].longValue();
        return this.minus(arrayl);
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(int constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("an int cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("an int cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Integer constant){
        int constantl =  constant.intValue();
        return this.minus(constantl);
    }


    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(int[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    int maxi2 = am22.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString(arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("an int cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("an int cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Integer[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        int[] arrayl = new int[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].intValue();
        return this.minus(arrayl);
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(short constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - (long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - (int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a short cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a short cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a short cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Short constant){
        short constantl =  constant.shortValue();
        return this.minus(constantl);
    }


    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(short[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - (long)arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    short maxi2 = am22.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - (int)arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString((int)arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a long cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a short cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Short[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        short[] arrayl = new short[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].shortValue();
        return this.minus(arrayl);
    }


    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(BigDecimal constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].subtract(constant));
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(constant.doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(new Phasor(constant.doubleValue())));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a BigDecimal cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(byte constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - (long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - (int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a byte cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a byte cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Byte constant){
        byte constantl =  constant.byteValue();
        return this.minus(constantl);
    }


    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(byte[] arrayI){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i] - (double)arrayI[i]));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    ArrayMaths am2 = new ArrayMaths(arrayI);
                    long max2 = am2.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=max2){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i] - (long)arrayI[i]));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    ArrayMaths am22 = new ArrayMaths(arrayI);
                    byte maxi2 = am22.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    if((Integer.MAX_VALUE-maxi)>=maxi2){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i] - (int)arrayI[i]));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i] - (double)arrayI[i]));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.subtract(new BigDecimal((double)arrayI[i]));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.subtract(new BigInteger((Integer.toString((int)arrayI[i]))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).minus((double)arrayI[i]));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).minus(new Phasor((double)arrayI[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a byte cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a byte cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Byte[] arrayI){
        int nArg = arrayI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");
        byte[] arrayl = new byte[this.length];
        for(int i=0; i<this.length; i++)arrayl[i] = arrayI[i].byteValue();
        return this.minus(arrayl);
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(BigDecimal[] arrayBD){
        int nArg = arrayBD.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(arrayBD[i]));
                    Conv.restoreMessages();
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(arrayBD[i].doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(new Phasor(arrayBD[i].doubleValue())));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a BigDecimalcannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(BigInteger constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal constantBD = Conv.convert_BigInteger_to_BigDecimal(constant);
                    BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(constantBD));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].subtract(constant));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(Conv.convert_BigInteger_to_double(constant)));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(new Phasor(Conv.convert_BigInteger_to_double(constant))));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(BigInteger[] arrayBI){
        int nArg = arrayBI.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(Conv.convert_BigInteger_to_BigDecimal(arrayBI[i])));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].add(arrayBI[i]));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(Conv.convert_BigInteger_to_double(arrayBI[i])));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(new Phasor(Conv.convert_BigInteger_to_double(arrayBI[i]))));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a BigInteger cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Complex constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(constant));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(Conv.convert_Complex_to_Phasor(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a Complex cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Complex[] arrayC){
        int nArg = arrayC.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(arrayC[i]));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(Conv.convert_Complex_to_Phasor(arrayC[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a Complex cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // subtract a constant from the elements of the internal array
    public ArrayMaths minus(Phasor constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(constant));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(Conv.convert_Phasor_to_Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a Phasor cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }


    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Phasor[] arrayP){
        int nArg = arrayP.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].minus(arrayP[i]));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].minus(Conv.convert_Phasor_to_Complex(arrayP[i])));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be subtracted from a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be subtracted from a Character");
            case 18: throw new IllegalArgumentException("a Phasor cannot be subtracted from a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Vector<Object> vec){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am1 = new ArrayMaths();
        ArrayMaths am2 = new ArrayMaths(vec);

        switch(am2.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = am2.getArray_as_double();
                    am1 = this.minus(dd);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: long[] ll = am2.getArray_as_long();
                    am1 = this.minus(ll);
                    break;
            case 12: BigDecimal[] bd = am2.getArray_as_BigDecimal();
                    am1 = this.minus(bd);
                    break;
            case 13: BigInteger[] bi = am2.getArray_as_BigInteger();
                    am1 = this.minus(bi);
                    break;
            case 14: Complex[] cc = am2.getArray_as_Complex();
                    am1 = this.minus(cc);
                    break;
            case 15: Phasor[] pp = am2.getArray_as_Phasor();
                    am1 = this.minus(pp);
                    break;
            case 16: throw new IllegalArgumentException("ArrayList/char subtraction not allowed");
            case 17: throw new IllegalArgumentException("ArrayList/Character subtraction not allowed");
            case 18: throw new IllegalArgumentException("ArrayList/String subtraction not allowed");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am1.getArray_as_Object(), am1.minmax, maxminIndices, am1.typeName, am1.type);
        am1.maxIndex = maxminIndices[0];
        am1.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am1;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(ArrayList<Object> list){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am1 = new ArrayMaths();
        ArrayMaths am2 = new ArrayMaths(list);

        switch(am2.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = am2.getArray_as_double();
                    am1 = this.minus(dd);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: long[] ll = am2.getArray_as_long();
                    am1 = this.minus(ll);
                    break;
            case 12: BigDecimal[] bd = am2.getArray_as_BigDecimal();
                    am1 = this.minus(bd);
                    break;
            case 13: BigInteger[] bi = am2.getArray_as_BigInteger();
                    am1 = this.minus(bi);
                    break;
            case 14: Complex[] cc = am2.getArray_as_Complex();
                    am1 = this.minus(cc);
                    break;
            case 15: Phasor[] pp = am2.getArray_as_Phasor();
                    am1 = this.minus(pp);
                    break;
            case 16: throw new IllegalArgumentException("ArrayList/char subtraction not allowed");
            case 17: throw new IllegalArgumentException("ArrayList/Character subtraction not allowed");
            case 18: throw new IllegalArgumentException("ArrayList/String subtraction not allowed");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am1.getArray_as_Object(), am1.minmax, maxminIndices, am1.typeName, am1.type);
        am1.maxIndex = maxminIndices[0];
        am1.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am1;
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(ArrayMaths arrayM){
        ArrayList<Object> arrayl = arrayM.getArray_as_ArrayList();
        return this.minus(arrayl);
    }

    // Subtract the elements of an array from the elements of the internal array
    public ArrayMaths minus(Stat arrayS){
        ArrayList<Object> arrayl = arrayS.getArray_as_ArrayList();
        return this.minus(arrayl);
    }

    // MULTIPLICATION
    // multiply the elements of the internal array by a constant
    public ArrayMaths times(double constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*constant));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.multiply(new BigDecimal(constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).multiply(new BigDecimal(constant));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).times(constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).times(new Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a double or float cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Double constant){
        return this.times(constant.doubleValue());
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(float constant){
        double constantd = constant;
        return this.times(constantd);
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Float constant){
        return this.times(constant.floatValue());
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(long constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*(double)constant));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]*constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.multiply(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.multiply(new BigInteger((Long.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).times((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).times(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a long cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a long cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Long constant){
        long constantl =  constant.longValue();
        return this.times(constantl);
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(int constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*(double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]*constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i]*constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.multiply(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.multiply(new BigInteger((Integer.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).times((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).times(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("an int cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("an int cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Integer constant){
        int constantl =  constant.intValue();
        return this.times(constantl);
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(short constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*(double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]*(long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i]*(int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.multiply(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.multiply(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).times((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).times(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a short cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a short cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a short cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Short constant){
        short constantl =  constant.shortValue();
        return this.times(constantl);
    }


    // multiply the elements of the internal array by a constant
    public ArrayMaths times(BigDecimal constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].multiply(constant));
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(constant.doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(new Phasor(constant.doubleValue())));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a BigDecimal cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(byte constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*(double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]*(long)constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    if((Integer.MAX_VALUE-maxi)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i]*(int)constant));
                        am.type = 6;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i]*(double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.multiply(new BigDecimal((double)constant));
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.multiply(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).times((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).times(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a byte cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a byte cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Byte constant){
        byte constantl =  constant.byteValue();
        return this.times(constantl);
    }


    // multiply the elements of the internal array by a constant
    public ArrayMaths times(BigInteger constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal constantBD = Conv.convert_BigInteger_to_BigDecimal(constant);
                    BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(constantBD));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].multiply(constant));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(Conv.convert_BigInteger_to_double(constant)));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(new Phasor(Conv.convert_BigInteger_to_double(constant))));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a BigInteger cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }


    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Complex constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(constant));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(Conv.convert_Complex_to_Phasor(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a Complex cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }


    // multiply the elements of the internal array by a constant
    public ArrayMaths times(Phasor constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(constant));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(Conv.convert_Phasor_to_Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be multiplied by a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be multiplied by a Character");
            case 18: throw new IllegalArgumentException("a Phasor cannot be multiplied by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }



    // DIVISION
    // divide the elements of the internal array by a constant
    public ArrayMaths over(double constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]/constant));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.divide(new BigDecimal(constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = this.type;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigDecimal hold2 = (new BigDecimal(hold1)).divide(new BigDecimal(constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 12;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).over(constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).over(new Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a double or float cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a double or float cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a double or float cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Double constant){
        return this.over(constant.doubleValue());
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(float constant){
        double constantd = constant;
        return this.over(constantd);
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Float constant){
        return this.over(constant.floatValue());
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(long constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]/(double)constant));
                    am.type = 0;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    if((Long.MAX_VALUE-max)>=constant){
                        for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]/constant));
                        am.type = 4;
                    }
                    else{
                       for(int i=0; i<this.length; i++)am.array.add(new Double((double)ll[i]/(double)constant));
                       am.type = 0;
                    }
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.divide(new BigDecimal((double)constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.divide(new BigInteger((Long.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).over((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).over(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a long cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a long cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a long cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Long constant){
        long constantl =  constant.longValue();
        return this.over(constantl);
    }


    // divide the elements of the internal array by a constant
    public ArrayMaths over(int constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]/(double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]/constant));
                    am.type = 4;
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:int maxi =  this.getMaximum_as_int();
                    int[] lll = this.getArray_as_int();
                    for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i]/constant));
                    am.type = 6;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.divide(new BigDecimal((double)constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.divide(new BigInteger((Integer.toString(constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).over((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).over(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("an int cannot be divided by a char");
            case 17: throw new IllegalArgumentException("an int cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("an int cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Integer constant){
        int constantl =  constant.intValue();
        return this.over(constantl);
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(short constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]/(double)constant));
                    am.type = 0;
                    break;
            case 4:long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]/(long)constant));
                    am.type = 4;
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:short maxi =  this.getMaximum_as_short();
                    short[] lll = this.getArray_as_short();
                    for(int i=0; i<this.length; i++)am.array.add(new Integer(lll[i]/(int)constant));
                    am.type = 6;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.divide(new BigDecimal((double)constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.divide(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).over((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).over(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a short cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a short cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a short cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Short constant){
        short constantl =  constant.shortValue();
        return this.over(constantl);
    }


    // divide the elements of the internal array by a constant
    public ArrayMaths over(BigDecimal constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].divide(constant, BigDecimal.ROUND_HALF_UP));
                    am.type = 12;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].over(constant.doubleValue()));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].over(new Phasor(constant.doubleValue())));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigDecimal cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a BigDecimal cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a BigDecimal cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(byte constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;

        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3: double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]/(double)constant));
                    am.type = 0;
                    break;
            case 4: long max =  this.getMaximum_as_long();
                    long[] ll = this.getArray_as_long();
                    for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]/(long)constant));
                    am.type = 4;
                    break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:byte maxi =  this.getMaximum_as_byte();
                    byte[] lll = this.getArray_as_byte();
                    for(int i=0; i<this.length; i++)am.array.add(new Double((double)lll[i]/(double)constant));
                    am.type = 0;
                    break;
            case 12: for(int i=0; i<this.length; i++){
                        BigDecimal hold1 = (BigDecimal)(this.array.get(i));
                        hold1 = hold1.divide(new BigDecimal((double)constant), BigDecimal.ROUND_HALF_UP);
                        am.array.add(hold1);
                        hold1 = null;
                    }
                    am.type = 12;
                    break;
            case 13: for(int i=0; i<this.length; i++){
                        BigInteger hold1 = (BigInteger)(this.array.get(i));
                        BigInteger hold2 = hold1.divide(new BigInteger((Integer.toString((int)constant))));
                        am.array.add(hold2);
                        hold1 = null;
                        hold2 = null;
                    }
                    am.type = 13;
                    break;
            case 14: for(int i=0; i<this.length; i++)am.array.add(((Complex)this.array.get(i)).over((double)constant));
                    am.type = this.type;
                    break;
            case 15: for(int i=0; i<this.length; i++)am.array.add(((Phasor)this.array.get(i)).over(new Phasor((double)constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a byte cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a byte cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a byte cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Byte constant){
        byte constantl =  constant.byteValue();
        return this.over(constantl);
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(BigInteger constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal constantBD = Conv.convert_BigInteger_to_BigDecimal(constant);
                    BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].add(constantBD));
                    am.type = 12;
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].divide(constant));
                    am.type = 13;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].over(Conv.convert_BigInteger_to_double(constant)));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].over(new Phasor(Conv.convert_BigInteger_to_double(constant))));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a BigInteger cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a BigInteger cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a BigInteger cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Complex constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].over(constant));
                    am.type = 14;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].over(Conv.convert_Complex_to_Phasor(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Complex cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a Complex cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a Complex cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // divide the elements of the internal array by a constant
    public ArrayMaths over(Phasor constant){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].over(constant));
                    am.type = 15;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].over(Conv.convert_Phasor_to_Complex(constant)));
                    am.type = this.type;
                    break;
            case 16: throw new IllegalArgumentException("a Phasor cannot be divided by a char");
            case 17: throw new IllegalArgumentException("a Phasor cannot be divided by a Character");
            case 18: throw new IllegalArgumentException("a Phasor cannot be divided by a String");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // TRUNCATION AND ROUNDING OF ARRAY ELEMENTS
    // Returns new ArrayMaths with array elements truncated to n decimal places
    public ArrayMaths truncate(int n){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:double[] dd = this.getArray_as_double();
                   for(int i=0; i<this.length; i++)am.array.add(new Double(Fmath.truncate(dd[i], n)));
                   am.type = this.type;
                   break;
            case 2:
            case 3:float[] ff = this.getArray_as_float();
                   for(int i=0; i<this.length; i++)am.array.add(new Float(Fmath.truncate(ff[i], n)));
                   am.type = this.type;
                   break;
            case 4:
            case 5:long[] ll = this.getArray_as_long();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]));
                   am.type = this.type;
                   break;
            case 6:
            case 7:int[] ii = this.getArray_as_int();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ii[i]));
                   am.type = this.type;
                   break;
            case 8:
            case 9:short[] ss = this.getArray_as_short();
                   for(int i=0; i<this.length; i++)am.array.add(new Short(ss[i]));
                   am.type = this.type;
                   break;
            case 10:
            case 11:byte[] bb = this.getArray_as_byte();
                   for(int i=0; i<this.length; i++)am.array.add(new Byte(bb[i]));
                   am.type = this.type;
                   break;
            case 12:BigDecimal[] bd = this.getArray_as_BigDecimal();
                   for(int i=0; i<this.length; i++)am.array.add(bd[i].setScale(n, BigDecimal.ROUND_HALF_UP));
                   am.type = this.type;
                   break;
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                   for(int i=0; i<this.length; i++)am.array.add(bi[i]);
                   am.type = this.type;
                   break;
            case 14:Complex[] co = this.getArray_as_Complex();
                   for(int i=0; i<this.length; i++)am.array.add(Complex.truncate(co[i], n));
                   am.type = this.type;
                   break;
            case 15:Phasor[] ph = this.getArray_as_Phasor();
                   for(int i=0; i<this.length; i++)am.array.add(Phasor.truncate(ph[i], n));
                   am.type = this.type;
                   break;
            case 16:
            case 17:char[] ch = this.getArray_as_char();
                   for(int i=0; i<this.length; i++)am.array.add(new Character(ch[i]));
                   am.type = this.type;
                   break;
            case 18: String[] st = this.getArray_as_String();
                   for(int i=0; i<this.length; i++)am.array.add(st[i]);
                   am.type = this.type;
                   break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }


    // Returns new ArrayMaths with array elements rounded down to the nearest lower integer
    public ArrayMaths floor(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:double[] dd = this.getArray_as_double();
                   for(int i=0; i<this.length; i++)am.array.add(new Double(Math.floor(dd[i])));
                   am.type = this.type;
                   break;
            case 2:
            case 3:float[] ff = this.getArray_as_float();
                   for(int i=0; i<this.length; i++)am.array.add(new Float(Math.floor(ff[i])));
                   am.type = this.type;
                   break;
            case 4:
            case 5:long[] ll = this.getArray_as_long();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]));
                   am.type = this.type;
                   break;
            case 6:
            case 7:int[] ii = this.getArray_as_int();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ii[i]));
                   am.type = this.type;
                   break;
            case 8:
            case 9:short[] ss = this.getArray_as_short();
                   for(int i=0; i<this.length; i++)am.array.add(new Short(ss[i]));
                   am.type = this.type;
                   break;
            case 10:
            case 11:byte[] bb = this.getArray_as_byte();
                   for(int i=0; i<this.length; i++)am.array.add(new Byte(bb[i]));
                   am.type = this.type;
                   break;
            case 12:BigDecimal[] bd = this.getArray_as_BigDecimal();
                   for(int i=0; i<this.length; i++)am.array.add(bd[i].setScale(0, BigDecimal.ROUND_DOWN));
                   am.type = this.type;
                   break;
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                   for(int i=0; i<this.length; i++)am.array.add(bi[i]);
                   am.type = this.type;
                   break;
            case 14:Complex[] co = this.getArray_as_Complex();
                   for(int i=0; i<this.length; i++)am.array.add(new Complex(Math.floor(co[i].getReal()), Math.floor(co[i].getImag())));
                   am.type = this.type;
                   break;
            case 15:Phasor[] ph = this.getArray_as_Phasor();
                   for(int i=0; i<this.length; i++)am.array.add(new Phasor(Math.floor(ph[i].getMagnitude()), Math.floor(ph[i].getPhaseInDegrees())));
                   am.type = this.type;
                   break;
            case 16:
            case 17:char[] ch = this.getArray_as_char();
                   for(int i=0; i<this.length; i++)am.array.add(new Character(ch[i]));
                   am.type = this.type;
                   break;
            case 18: String[] st = this.getArray_as_String();
                   for(int i=0; i<this.length; i++)am.array.add(st[i]);
                   am.type = this.type;
                   break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }


    // Returns new ArrayMaths with array elements rounded up to the nearest higher integer
    public ArrayMaths ceil(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:double[] dd = this.getArray_as_double();
                   for(int i=0; i<this.length; i++)am.array.add(new Double(Math.ceil(dd[i])));
                   am.type = this.type;
                   break;
            case 2:
            case 3:float[] ff = this.getArray_as_float();
                   for(int i=0; i<this.length; i++)am.array.add(new Float(Math.ceil(ff[i])));
                   am.type = this.type;
                   break;
            case 4:
            case 5:long[] ll = this.getArray_as_long();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]));
                   am.type = this.type;
                   break;
            case 6:
            case 7:int[] ii = this.getArray_as_int();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ii[i]));
                   am.type = this.type;
                   break;
            case 8:
            case 9:short[] ss = this.getArray_as_short();
                   for(int i=0; i<this.length; i++)am.array.add(new Short(ss[i]));
                   am.type = this.type;
                   break;
            case 10:
            case 11:byte[] bb = this.getArray_as_byte();
                   for(int i=0; i<this.length; i++)am.array.add(new Byte(bb[i]));
                   am.type = this.type;
                   break;
            case 12:BigDecimal[] bd = this.getArray_as_BigDecimal();
                   for(int i=0; i<this.length; i++)am.array.add(bd[i].setScale(0, BigDecimal.ROUND_UP));
                   am.type = this.type;
                   break;
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                   for(int i=0; i<this.length; i++)am.array.add(bi[i]);
                   am.type = this.type;
                   break;
            case 14:Complex[] co = this.getArray_as_Complex();
                   for(int i=0; i<this.length; i++)am.array.add(new Complex(Math.ceil(co[i].getReal()), Math.ceil(co[i].getImag())));
                   am.type = this.type;
                   break;
            case 15:Phasor[] ph = this.getArray_as_Phasor();
                   for(int i=0; i<this.length; i++)am.array.add(new Phasor(Math.ceil(ph[i].getMagnitude()), Math.ceil(ph[i].getPhaseInDegrees())));
                   am.type = this.type;
                   break;
            case 16:
            case 17:char[] ch = this.getArray_as_char();
                   for(int i=0; i<this.length; i++)am.array.add(new Character(ch[i]));
                   am.type = this.type;
                   break;
            case 18: String[] st = this.getArray_as_String();
                   for(int i=0; i<this.length; i++)am.array.add(st[i]);
                   am.type = this.type;
                   break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }


    // Returns new ArrayMaths with array elements rounded to a value that is closest in value to the element and is equal to a mathematical integer.
    public ArrayMaths rint(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:double[] dd = this.getArray_as_double();
                   for(int i=0; i<this.length; i++)am.array.add(new Double(Math.rint(dd[i])));
                   am.type = this.type;
                   break;
            case 2:
            case 3:float[] ff = this.getArray_as_float();
                   for(int i=0; i<this.length; i++)am.array.add(new Float(Math.rint(ff[i])));
                   am.type = this.type;
                   break;
            case 4:
            case 5:long[] ll = this.getArray_as_long();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ll[i]));
                   am.type = this.type;
                   break;
            case 6:
            case 7:int[] ii = this.getArray_as_int();
                   for(int i=0; i<this.length; i++)am.array.add(new Long(ii[i]));
                   am.type = this.type;
                   break;
            case 8:
            case 9:short[] ss = this.getArray_as_short();
                   for(int i=0; i<this.length; i++)am.array.add(new Short(ss[i]));
                   am.type = this.type;
                   break;
            case 10:
            case 11:byte[] bb = this.getArray_as_byte();
                   for(int i=0; i<this.length; i++)am.array.add(new Byte(bb[i]));
                   am.type = this.type;
                   break;
            case 12:BigDecimal[] bd = this.getArray_as_BigDecimal();
                   for(int i=0; i<this.length; i++)am.array.add(bd[i].setScale(0, BigDecimal.ROUND_HALF_EVEN));
                   am.type = this.type;
                   break;
            case 13:BigInteger[] bi = this.getArray_as_BigInteger();
                   for(int i=0; i<this.length; i++)am.array.add(bi[i]);
                   am.type = this.type;
                   break;
            case 14:Complex[] co = this.getArray_as_Complex();
                   for(int i=0; i<this.length; i++)am.array.add(new Complex(Math.rint(co[i].getReal()), Math.rint(co[i].getImag())));
                   am.type = this.type;
                   break;
            case 15:Phasor[] ph = this.getArray_as_Phasor();
                   for(int i=0; i<this.length; i++)am.array.add(new Phasor(Math.rint(ph[i].getMagnitude()), Math.rint(ph[i].getPhaseInDegrees())));
                   am.type = this.type;
                   break;
            case 16:
            case 17:char[] ch = this.getArray_as_char();
                   for(int i=0; i<this.length; i++)am.array.add(new Character(ch[i]));
                   am.type = this.type;
                   break;
            case 18: String[] st = this.getArray_as_String();
                   for(int i=0; i<this.length; i++)am.array.add(st[i]);
                   am.type = this.type;
                   break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }


    // ARRAY REVERSAL
    // Returns new ArrayMaths with array elements reversed
    public ArrayMaths reverse(){
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        am.type = this.type;
        am.sortedIndices = new int[this.length];

        for(int i=0; i<this.length; i++){
            am.array.add(this.array.get(this.length - i - 1));
            am.sortedIndices[i] = this.length - i - 1;
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        return am;
    }


    // ARRAY LOGARITHMS
    // Returns new ArrayMaths with array elements converted to their natural logs
    public ArrayMaths log(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.log(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.log(cc[i]));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.log(pp[i]));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

   // Returns new ArrayMaths with array elements converted to log to the base 2
    public ArrayMaths log2(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Fmath.log2(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.log(cc[i].over(Math.log(2.0))));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.log(pp[i].over(Math.log(2.0))));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements converted to log10(array element)
   public ArrayMaths log10(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.log10(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.log(cc[i].over(Math.log(10.0))));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.log(pp[i].over(Math.log(10))));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }
    // Returns new ArrayMaths with array elements converted to antilog10(element)
    public ArrayMaths antilog10(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.pow(10.0, dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.pow(10.0, cc[i]));
                    am.type = this.type;
                    break;
            case 15: Complex[] pp = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Conv.convert_Complex_to_Phasor(Complex.pow(10.0, pp[i])));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements, x, converted to x.log2(x)
    public ArrayMaths xLog2x(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*Fmath.log2(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(Complex.log(cc[i].over(Math.log(2)))));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(Phasor.log(pp[i].over(Math.log(2)))));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements, x, converted to x.loge(x)
    public ArrayMaths xLogEx(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*Math.log(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(Complex.log(cc[i])));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(Phasor.log(pp[i])));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements, x, converted to x.log10(x)
    public ArrayMaths xLog10x(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(dd[i]*Math.log10(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(cc[i].times(Complex.log(cc[i].over(Math.log(10)))));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].times(Phasor.log(pp[i].over(Math.log(10)))));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements, x, converted to -x.log2(x)
    public ArrayMaths minusxLog2x(){
        ArrayMaths am = this.xLog2x();
        return am.negate();
    }

    // Returns new ArrayMaths with array elements, x, converted to -x.loge(x)
    public ArrayMaths minusxLogEx(){
        ArrayMaths am = this.xLogEx();
        return am.negate();
    }

    // Returns new ArrayMaths with array elements, x, converted to -x.log10(x)
    public ArrayMaths minusxLog10x(){
        ArrayMaths am = this.xLog10x();
        return am.negate();
    }

    // SQUARE ROOTS
    // Returns new ArrayMaths with array elements converted to their square roots
    public ArrayMaths sqrt(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.sqrt(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.sqrt(cc[i]));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.sqrt(pp[i]));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements converted to 1.0/sqrt(element)
    public ArrayMaths oneOverSqrt(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(1.0D/Math.sqrt(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add((Complex.sqrt(cc[i])).inverse());
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add((Phasor.sqrt(pp[i])).inverse());
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // VARIOUS TRANSFORMATION OF ARRAY ELEMENTS
    // Returns new ArrayMaths with array elements converted to their absolute values
    public ArrayMaths abs(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.abs(dd[i])));
                     am.type = this.type;
                     break;
            case 2:
            case 3: float[] ff = this.getArray_as_float();
                     for(int i=0; i<this.length; i++)am.array.add(new Float(Math.abs(ff[i])));
                     am.type = this.type;
                     break;
            case 4:
            case 5: long[] ll = this.getArray_as_long();
                     for(int i=0; i<this.length; i++)am.array.add(new Long(Math.abs(ll[i])));
                     am.type = this.type;
                     break;
            case 6:
            case 7: int[] ii1 = this.getArray_as_int();
                     for(int i=0; i<this.length; i++)am.array.add(new Integer(Math.abs(ii1[i])));
                     am.type = this.type;
                     break;
            case 8:
            case 9: int[] ii2 = this.getArray_as_int();
                     for(int i=0; i<this.length; i++)am.array.add(new Short((short)Math.abs(ii2[i])));
                     am.type = this.type;
                     break;
            case 10:
            case 11: int[] ii3 = this.getArray_as_int();
                     for(int i=0; i<this.length; i++)am.array.add(new Byte((byte)Math.abs(ii3[i])));
                     am.type = this.type;
                     break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     for(int i=0; i<this.length; i++)am.array.add(bd[i].abs());
                     am.type = this.type;
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     for(int i=0; i<this.length; i++)am.array.add(bi[i].abs());
                     am.type = this.type;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.abs(cc[i]));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(pp[i].abs());
                    am.type = 15;
                    break;
            case 16:
            case 17: int[] ii4 = this.getArray_as_int();
                     for(int i=0; i<this.length; i++)am.array.add(new Integer(Math.abs(ii4[i])));
                     am.type = this.type;
                     break;
            case 18: double[] dd2 = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.abs(dd2[i])));
                     am.type = 1;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }


    // Returns new ArrayMaths with array elements converted to exp(element)
    public ArrayMaths exp(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.exp(dd[i])));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.exp(cc[i]));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.exp(pp[i]));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements converted to 1.0/element
    public ArrayMaths invert(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(1.0D/dd[i]));
                     am.type = 1;
                     break;
            case 12:
            case 13: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     for(int i=0; i<this.length; i++)am.array.add((BigDecimal.ONE).divide(bd[i], BigDecimal.ROUND_HALF_UP));
                     am.type = 12;
                     bd = null;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                     for(int i=0; i<this.length; i++)am.array.add((Complex.plusOne()).over(cc[i]));
                     am.type = 14;
                     break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                     for(int i=0; i<this.length; i++)am.array.add((Phasor.plusOne()).over(pp[i]));
                     am.type = 15;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }


    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(int n){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.pow(dd[i], n)));
                     am.type = 1;
                     break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     BigDecimal bdpow = BigDecimal.ONE;
                     for(int i=0; i<this.length; i++){
                        for(int j=0; j<n; j++)bdpow = bdpow.multiply(bd[i]);
                        am.array.add(bdpow);
                     }
                     bd = null;
                     bdpow = null;
                     am.type = 12;
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     for(int i=0; i<this.length; i++)am.array.add(bi[i].pow(n));
                     am.type = 13;
                     bi = null;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.pow(cc[i], n));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.pow(pp[i], n));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(double n){
        if(this.suppressMessages)Conv.suppressMessages();

        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.pow(dd[i], n)));
                     am.type = 1;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.pow(cc[i], n));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.pow(pp[i], n));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(float n){
        double nn = (double)n;
        return this.pow(nn);
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(long n){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:
            case 18: double[] dd = this.getArray_as_double();
                     for(int i=0; i<this.length; i++)am.array.add(new Double(Math.pow(dd[i], n)));
                     am.type = 1;
                     break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     BigDecimal bdpow = BigDecimal.ONE;
                     for(int i=0; i<this.length; i++){
                        long j = 0L;
                        while(j<n){
                            bdpow = bdpow.multiply(bd[i]);
                        }
                        am.array.add(bdpow);
                     }
                     bd = null;
                     bdpow = null;
                     am.type = 12;
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     BigInteger bipow = BigInteger.ONE;
                     for(int i=0; i<this.length; i++){
                        long j = 0L;
                        while(j<n){
                            bipow = bipow.multiply(bi[i]);
                        }
                        am.array.add(bipow);
                     }
                     bi = null;
                     bipow = null;
                     am.type = 13;
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    for(int i=0; i<this.length; i++)am.array.add(Complex.pow(cc[i], n));
                    am.type = this.type;
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    for(int i=0; i<this.length; i++)am.array.add(Phasor.pow(pp[i], n));
                    am.type = 15;
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(short n){
        int ii = (int)n;
        return this.pow(ii);
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(byte n){
        int ii = n;
        return this.pow(ii);
    }

    // Returns new ArrayMaths with array elements raised to a power n
    public ArrayMaths pow(Number n){
        boolean test = integers.containsKey(n.getClass());
        if(test){
            if(n instanceof Long){
                return this.pow(n.longValue());
            }
            else{
                if(n instanceof BigInteger){
                    return this.pow(n.doubleValue());
                }
                else{
                    return this.pow(n.intValue());
                }
            }
        }
        else{
            return this.pow(n.doubleValue());
        }
    }

    // Returns new ArrayMaths with array elements converted to -element
    public ArrayMaths negate(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        switch(this.type){
            case 0:
            case 1:
            case 16:
            case 17:
            case 18:double[] dd = this.getArray_as_double();
                    for(int i=0; i<this.length; i++)am.array.add(new Double(-dd[i]));
                    am.type = 1;
                    break;
            case 2:
            case 3: float[] ff = this.getArray_as_float();
                    for(int i=0; i<this.length; i++)am.array.add(new Float(-ff[i]));
                    am.type = 3;
                    break;
            case 4:
            case 5: long[] ll = this.getArray_as_long();
                    for(int i=0; i<this.length; i++)am.array.add(new Long(-ll[i]));
                    am.type = 5;
                    break;
            case 6:
            case 7: int[] ii = this.getArray_as_int();
                    for(int i=0; i<this.length; i++)am.array.add(new Integer(-ii[i]));
                    am.type = 7;
                    break;
            case 8:
            case 9: short[] ss = this.getArray_as_short();
                    for(int i=0; i<this.length; i++)am.array.add(new Short((short)(-ss[i])));
                    am.type = 9;
                    break;
            case 10:
            case 11: byte[] bb = this.getArray_as_byte();
                    for(int i=0; i<this.length; i++)am.array.add(new Byte((byte)(-bb[i])));
                    am.type = 11;
                    break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    for(int i=0; i<this.length; i++)am.array.add(bd[i].negate());
                    am.type = 12;
                    bd = null;
                    break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                    for(int i=0; i<this.length; i++)am.array.add(bi[i].negate());
                    am.type = 13;
                    bi = null;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                     for(int i=0; i<this.length; i++)am.array.add(cc[i].negate());
                     am.type = 14;
                     break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                     for(int i=0; i<this.length; i++)am.array.add(pp[i].negate());
                     am.type = 15;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];
        Conv.restoreMessages();
        return am;
    }



    // protected method for calcuating the sum of the array elements
    // called by the public methods
    protected void calcSum(){
        if(this.suppressMessages)Conv.suppressMessages();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: double[] dd = this.getArray_as_double();
                    double sum = 0.0D;
                    for(int i=0; i<this.length; i++)sum += dd[i];
                    this.summ.add(new Double(sum));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:long[] ll = this.getArray_as_long();
                    long suml = 0L;
                    boolean test = false;
                    for(int i=0; i<this.length; i++){
                        if(Long.MAX_VALUE-suml<ll[i])test=true;
                        suml += ll[i];
                    }
                    if(test){
                        double[] dd2 = this.getArray_as_double();
                        double sum2 = 0.0D;
                        for(int i=0; i<this.length; i++)sum2 += dd2[i];
                        this.summ.add(new Double(sum2));
                        this.sumlongToDouble = true;
                    }
                    else{
                        this.summ.add(new Long(suml));
                    }
                    break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    BigDecimal sumbd = new BigDecimal(0.0D);
                    for(int i=0; i<this.length; i++)sumbd.add(bd[i]);
                    this.summ.add(sumbd);
                    bd = null;
                    sumbd = null;
                    break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                    BigInteger sumbi = BigInteger.ZERO;
                    for(int i=0; i<this.length; i++)sumbi.add(bi[i]);
                    this.summ.add(sumbi);
                    bi = null;
                    sumbi = null;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    Complex sumcc = Complex.zero();
                            for(int i=0; i<this.length; i++)sumcc.plus(cc[i]);
                        this.summ.add(sumcc);
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    Phasor sumpp = Phasor.zero();
                    for(int i=0; i<this.length; i++)sumpp.plus(pp[i]);
                    this.summ.add(sumpp);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        this.sumDone = true;
        Conv.restoreMessages();
    }


    // returns the sum of the array elements as a double
    public double sum(){
        return this.getSum_as_double();
    }

    public double sum_as_double(){
        return this.getSum_as_double();
    }

    public double getSum(){
        return this.getSum_as_double();
    }

    public double getSum_as_double(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        double sum = 0.0D;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = ((Double)this.summ.get(0)).doubleValue();
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = ((Double)this.summ.get(0)).doubleValue();
                    }
                    else{
                        sum = Conv.convert_Long_to_double((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_double((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_double((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a Double
    public Double sum_as_Double(){
        return this.getSum_as_Double();
    }

    public Double getSum_as_Double(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Double sum = new Double(0.0D);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = (Double)this.summ.get(0);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = (Double)this.summ.get(0);
                    }
                    else{
                        sum = Conv.convert_Long_to_Double((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Double((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Double((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a float
    public float sum_as_float(){
        return this.getSum_as_float();
    }

    public float getSum_as_float(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        float sum = 0.0F;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_float((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_float((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_float((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_float((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_float((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a Float
    public Float sum_as_Float(){
        return this.getSum_as_Float();
    }

    public Float getSum_as_Float(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Float sum = new Float(0.0F);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_Float((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_Float((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_Float((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Float((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Float((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as a long
    public long sum_as_long(){
        return this.getSum_as_long();
    }

    public long getSum_as_long(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        long sum = 0L;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_long((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_long((Double)this.summ.get(0));
                    }
                    else{
                        sum = (Long)this.summ.get(0);
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_long((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_long((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a Long
    public Long sum_as_Long(){
        return this.getSum_as_Long();
    }

    public Long getSum_as_Long(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Long sum = new Long(0L);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_Long((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_Long((Double)this.summ.get(0));
                    }
                    else{
                        sum = (Long)this.summ.get(0);
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Long((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Long((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as an int
    public int sum_as_int(){
        return this.getSum_as_int();
    }

    public int getSum_as_int(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        int sum = 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_int((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_int((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_int((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_int((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_int((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as int is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as an Integer
    public Integer sum_as_Integer(){
        return this.getSum_as_Integer();
    }

    public Integer getSum_as_Integer(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Integer sum = new Integer(0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_Integer((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_Integer((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_Integer((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Integer((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Integer((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Integer is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a short
    public short sum_as_short(){
        return this.getSum_as_short();
    }

    public short getSum_as_short(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        short sum = 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_short((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_short((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_short((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_short((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_short((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a Short
    public Short sum_as_Short(){
        return this.getSum_as_Short();
    }

    public Short getSum_as_Short(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Short sum = new Short((short)0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_Short((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_Short((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_Short((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Short((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Short((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as a byte
    public byte sum_as_byte(){
        return this.getSum_as_byte();
    }

    public byte getSum_as_byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        byte sum = 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_byte((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_byte((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_byte((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_byte((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_byte((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a Byte
    public Byte sum_as_Byte(){
        return this.getSum_as_Byte();
    }

    public Byte getSum_as_Byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Byte sum = new Byte((byte)0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_Byte((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_Byte((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_Byte((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_Byte((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = Conv.convert_BigInteger_to_Byte((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as a BigDecimal
    public BigDecimal sum_as_BigDecimal(){
        return this.getSum_as_BigDecimal();
    }

    public BigDecimal getSum_as_BigDecimal(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        BigDecimal sum = new BigDecimal(0.0D);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_BigDecimal((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_BigDecimal((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_BigDecimal((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = (BigDecimal)this.summ.get(0);
                    break;
            case 13: sum = Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.summ.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as BigDecimal is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a BigInteger
    public BigInteger sum_as_BigInteger(){
        return this.getSum_as_BigInteger();
    }

    public BigInteger getSum_as_BigInteger(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        BigInteger sum = BigInteger.ZERO;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Conv.convert_Double_to_BigInteger((Double)this.summ.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Conv.convert_Double_to_BigInteger((Double)this.summ.get(0));
                    }
                    else{
                        sum = Conv.convert_Long_to_BigInteger((Long)this.summ.get(0));
                    }
                    break;
            case 12: sum = Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.summ.get(0));
                    break;
            case 13: sum = (BigInteger)this.summ.get(0);
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as BigInteger is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as a Complex
    public Complex sum_as_Complex(){
        return this.getSum_as_Complex();
    }

    public Complex getSum_as_Complex(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Complex sum = Complex.zero();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = new Complex(((Double)this.summ.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = new Complex(((Double)this.summ.get(0)).doubleValue());
                    }
                    else{
                        sum = new Complex(((Long)this.summ.get(0)).doubleValue());
                    }
                    break;
            case 12: sum = new Complex(((BigDecimal)this.summ.get(0)).doubleValue());
                    break;
            case 13: sum = new Complex(((BigInteger)this.summ.get(0)).doubleValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Complex is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }


    // returns the sum of the array elements as a Phasor
    public Phasor sum_as_Phasor(){
        return this.getSum_as_Phasor();
    }

   public Phasor getSum_as_Phasor(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        Phasor sum = Phasor.zero();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = new Phasor(((Double)this.summ.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = new Phasor(((Double)this.summ.get(0)).doubleValue());
                    }
                    else{
                        sum = new Phasor(((Long)this.summ.get(0)).doubleValue());
                    }
                    break;
            case 12: sum = new Phasor(((BigDecimal)this.summ.get(0)).doubleValue());
                    break;
            case 13: sum = new Phasor(((BigInteger)this.summ.get(0)).doubleValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as Phasor is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // returns the sum of the array elements as a String
    public String sum_as_String(){
        return this.getSum_as_String();
    }

    public String getSum_as_String(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.sumDone)this.calcSum();
        String sum = " ";
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: sum = Double.toString(((Double)this.summ.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.sumlongToDouble){
                        sum = Double.toString(((Double)this.summ.get(0)).doubleValue());
                    }
                    else{
                        sum = Double.toString(((Long)this.summ.get(0)).doubleValue());
                    }
                    break;
            case 12: sum = ((BigDecimal)this.summ.get(0)).toString();
                    break;
            case 13: sum = ((BigInteger)this.summ.get(0)).toString();
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a sum as String is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return sum;
    }

    // protected method for calcuating the product of the array elements
    // called by the public methods
    protected void calcProduct(){
        if(this.suppressMessages)Conv.suppressMessages();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: double[] dd = this.getArray_as_double();
                    double product= 1.0D;
                    for(int i=0; i<this.length; i++)product*= dd[i];
                    this.productt.add(new Double(product));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:long[] ll = this.getArray_as_long();
                    long productl = 1L;
                    boolean test = false;
                    for(int i=0; i<this.length; i++){
                        if(Long.MAX_VALUE/productl<ll[i])test=true;
                        productl += ll[i];
                    }
                    if(test){
                        double[] dd2 = this.getArray_as_double();
                        double product2 = 1.0D;
                        for(int i=0; i<this.length; i++)product2 *= dd2[i];
                        this.productt.add(new Double(product2));
                        this.sumlongToDouble = true;
                    }
                    else{
                        this.productt.add(new Long(productl));
                    }
                    break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                    BigDecimal productbd = new BigDecimal(1.0D);
                    for(int i=0; i<this.length; i++)productbd.multiply(bd[i]);
                    this.productt.add(productbd);
                    bd = null;
                    productbd = null;
                    break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                    BigInteger productbi = BigInteger.ONE;
                    for(int i=0; i<this.length; i++)productbi.multiply(bi[i]);
                    this.productt.add(productbi);
                    bi = null;
                    productbi = null;
                    break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                    Complex productcc = Complex.plusOne();
                    for(int i=0; i<this.length; i++)productcc.times(cc[i]);
                    this.productt.add(productcc);
                    break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                    Phasor productpp = Phasor.plusOne();
                    for(int i=0; i<this.length; i++)productpp.times(pp[i]);
                    this.productt.add(productpp);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        this.productDone = true;
        Conv.restoreMessages();
    }

    // returns the product of the array elements as a double
    public double product(){
        return this.getProduct_as_double();
    }

    public double product_as_double(){
        return this.getProduct_as_double();
    }

    public double getProduct(){
        return this.getProduct_as_double();
    }

    public double getProduct_as_double(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        double product= 0.0D;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= ((Double)this.productt.get(0)).doubleValue();
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= ((Double)this.productt.get(0)).doubleValue();
                    }
                    else{
                        product= Conv.convert_Long_to_double((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_double((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_double((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Double
    public Double product_as_Double(){
        return this.getProduct_as_Double();
    }

    public Double getProduct_as_Double(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Double product= new Double(0.0D);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= (Double)this.productt.get(0);
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= (Double)this.productt.get(0);
                    }
                    else{
                        product= Conv.convert_Long_to_Double((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Double((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Double((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Double is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }


    // returns the product of the array elements as a float
    public float product_as_float(){
        return this.getProduct_as_float();
    }

    public float getProduct_as_float(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        float product= 0.0F;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_float((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_float((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_float((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_float((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_float((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Float
    public Float product_as_Float(){
        return this.getProduct_as_Float();
    }

    public Float getProduct_as_Float(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Float product= new Float(0.0F);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_Float((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_Float((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_Float((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Float((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Float((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Float is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }


    // returns the product of the array elements as a long
    public long product_as_long(){
        return this.getProduct_as_long();
    }

    public long getProduct_as_long(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        long product= 0L;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_long((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_long((Double)this.productt.get(0));
                    }
                    else{
                        product= (Long)this.productt.get(0);
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_long((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_long((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Long
    public Long product_as_Long(){
        return this.getProduct_as_Long();
    }

    public Long getProduct_as_Long(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Long product= new Long(0L);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_Long((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_Long((Double)this.productt.get(0));
                    }
                    else{
                        product= (Long)this.productt.get(0);
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Long((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Long((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Long is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as an int
    public int product_as_int(){
        return this.getProduct_as_int();
    }

    public int getProduct_as_int(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        int product= 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_int((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_int((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_int((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_int((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_int((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas int is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as an Integer
    public Integer product_as_Integer(){
        return this.getProduct_as_Integer();
    }

    public Integer getProduct_as_Integer(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Integer product= new Integer(0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_Integer((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_Integer((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_Integer((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Integer((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Integer((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Integer is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a short
    public short product_as_short(){
        return this.getProduct_as_short();
    }

    public short getProduct_as_short(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        short product= 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_short((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_short((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_short((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_short((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_short((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Short
    public Short product_as_Short(){
        return this.getProduct_as_Short();
    }

    public Short getProduct_as_Short(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Short product= new Short((short)0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_Short((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_Short((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_Short((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Short((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Short((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Short is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a byte
    public byte product_as_byte(){
        return this.getProduct_as_byte();
    }

    public byte getProduct_as_byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        byte product= 0;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_byte((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_byte((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_byte((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_byte((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_byte((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Byte
    public Byte product_as_Byte(){
        return this.getProduct_as_Byte();
    }

    public Byte getProduct_as_Byte(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Byte product= new Byte((byte)0);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_Byte((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_Byte((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_Byte((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_Byte((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= Conv.convert_BigInteger_to_Byte((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Byte is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }


    // returns the product of the array elements as a BigDecimal
    public BigDecimal product_as_BigDecimal(){
        return this.getProduct_as_BigDecimal();
    }

    public BigDecimal getProduct_as_BigDecimal(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        BigDecimal product= new BigDecimal(0.0D);
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_BigDecimal((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_BigDecimal((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_BigDecimal((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= (BigDecimal)this.productt.get(0);
                    break;
            case 13: product= Conv.convert_BigInteger_to_BigDecimal((BigInteger)this.productt.get(0));
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas BigDecimal is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a BigInteger
    public BigInteger product_as_BigInteger(){
        return this.getProduct_as_BigInteger();
    }

    public BigInteger getProduct_as_BigInteger(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        BigInteger product= BigInteger.ZERO;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Conv.convert_Double_to_BigInteger((Double)this.productt.get(0));
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Conv.convert_Double_to_BigInteger((Double)this.productt.get(0));
                    }
                    else{
                        product= Conv.convert_Long_to_BigInteger((Long)this.productt.get(0));
                    }
                    break;
            case 12: product= Conv.convert_BigDecimal_to_BigInteger((BigDecimal)this.productt.get(0));
                    break;
            case 13: product= (BigInteger)this.productt.get(0);
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas BigInteger is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Complex
    public Complex product_as_Complex(){
        return this.getProduct_as_Complex();
    }

    public Complex getProduct_as_Complex(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Complex product= Complex.zero();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= new Complex(((Double)this.productt.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= new Complex(((Double)this.productt.get(0)).doubleValue());
                    }
                    else{
                        product= new Complex(((Long)this.productt.get(0)).doubleValue());
                    }
                    break;
            case 12: product= new Complex(((BigDecimal)this.productt.get(0)).doubleValue());
                    break;
            case 13: product= new Complex(((BigInteger)this.productt.get(0)).doubleValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Complex is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a Phasor
    public Phasor product_as_Phasor(){
        return this.getProduct_as_Phasor();
    }

    public Phasor getProduct_as_Phasor(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        Phasor product= Phasor.zero();
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= new Phasor(((Double)this.productt.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= new Phasor(((Double)this.productt.get(0)).doubleValue());
                    }
                    else{
                        product= new Phasor(((Long)this.productt.get(0)).doubleValue());
                    }
                    break;
            case 12: product= new Phasor(((BigDecimal)this.productt.get(0)).doubleValue());
                    break;
            case 13: product= new Phasor(((BigInteger)this.productt.get(0)).doubleValue());
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas Phasor is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // returns the product of the array elements as a String
    public String product_as_String(){
        return this.getProduct_as_String();
    }

   public String getProduct_as_String(){
        if(this.suppressMessages)Conv.suppressMessages();
        if(!this.productDone)this.calcProduct();
        String product= " ";
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 18: product= Double.toString(((Double)this.productt.get(0)).doubleValue());
                    break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: if(this.productlongToDouble){
                        product= Double.toString(((Double)this.productt.get(0)).doubleValue());
                    }
                    else{
                        product= Double.toString(((Long)this.productt.get(0)).doubleValue());
                    }
                    break;
            case 12: product= ((BigDecimal)this.productt.get(0)).toString();
                    break;
            case 13: product= ((BigInteger)this.productt.get(0)).toString();
                    break;
            case 14:
            case 15: throw new IllegalArgumentException("The " + this.typeName[this.type] + " is not a numerical type for which a productas String is meaningful/supported");
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }

        Conv.restoreMessages();
        return product;
    }

    // randomize the order of the elements in the internal array
    public ArrayMaths randomize(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        am.type = this.type;
        PsRandom ran = new PsRandom();
        am.sortedIndices = ran.uniqueIntegerArray(this.length-1);

        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)am.array.add((Double)this.array.get(am.sortedIndices[i]));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)am.array.add((Float)this.array.get(am.sortedIndices[i]));
                    break;
            case 4:
            case 5:for(int i=0; i<this.length; i++)am.array.add((Long)this.array.get(am.sortedIndices[i]));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)am.array.add((Integer)this.array.get(am.sortedIndices[i]));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)am.array.add((Short)this.array.get(am.sortedIndices[i]));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)am.array.add((Byte)this.array.get(am.sortedIndices[i]));
                     break;
            case 12: for(int i=0; i<this.length; i++)am.array.add((BigDecimal)this.array.get(am.sortedIndices[i]));
                     break;
            case 13: for(int i=0; i<this.length; i++)am.array.add((BigInteger)this.array.get(am.sortedIndices[i]));
                     break;
            case 14: for(int i=0; i<this.length; i++)am.array.add((Complex)this.array.get(am.sortedIndices[i]));
                     break;
            case 15: for(int i=0; i<this.length; i++)am.array.add((Phasor)this.array.get(am.sortedIndices[i]));
                     break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)am.array.add((Character)this.array.get(am.sortedIndices[i]));
                     break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(am.sortedIndices[i]));
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // randomize the order of the elements in the internal array
    public ArrayMaths randomise(){
        return this.randomize();
    }


    // sort the array into ascending order
    // saves sorted array in this instance
    public void sortEquals(){
        ArrayMaths am = this.sort();
        this.array = am.array;
        this.sortedIndices = am.sortedIndices;
        this.maxIndex = am.maxIndex;
        this.minIndex = am.minIndex;   
    }    
    
    // sort the array into ascending order
    // returns sorted array in a new ArrayMaths
    public ArrayMaths sort(){
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        am.type = this.type;
        am.sortedIndices = new int[this.length];

        switch(this.type){
            case 0:
            case 1: double[] dd1 = this.getArray_as_double();
                    am.sortedIndices = this.sortWithIndices(dd1);
                    for(int i=0; i<this.length; i++)am.array.add((Double)this.array.get(am.sortedIndices[i]));
                    break;
            case 2:
            case 3: double[] dd2 = this.getArray_as_double();
                    am.sortedIndices = this.sortWithIndices(dd2);
                    for(int i=0; i<this.length; i++)am.array.add((Float)this.array.get(am.sortedIndices[i]));
                    break;
            case 4:
            case 5: long[] ll1 = this.getArray_as_long();
                    am.sortedIndices = this.sortWithIndices(ll1);
                    for(int i=0; i<this.length; i++)am.array.add((Long)this.array.get(am.sortedIndices[i]));
                    break;
            case 6:
            case 7: long[] ll2 = this.getArray_as_long();
                    am.sortedIndices = this.sortWithIndices(ll2);
                    for(int i=0; i<this.length; i++)am.array.add((Integer)this.array.get(am.sortedIndices[i]));
                    break;
            case 8:
            case 9: long[] ll3 = this.getArray_as_long();
                    am.sortedIndices = this.sortWithIndices(ll3);
                    for(int i=0; i<this.length; i++)am.array.add((Short)this.array.get(am.sortedIndices[i]));
                    break;
            case 10:
            case 11: long[] ll4 = this.getArray_as_long();
                     am.sortedIndices = this.sortWithIndices(ll4);
                     for(int i=0; i<this.length; i++)am.array.add((Byte)this.array.get(am.sortedIndices[i]));
                     break;
            case 12: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     am.sortedIndices = this.sortWithIndices(bd);
                     for(int i=0; i<this.length; i++)am.array.add((BigDecimal)this.array.get(am.sortedIndices[i]));
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     am.sortedIndices = this.sortWithIndices(bi);
                     for(int i=0; i<this.length; i++)am.array.add((BigInteger)this.array.get(am.sortedIndices[i]));
                     break;
            case 14: ArrayMaths am2 = this.abs();
                     double[] cc = am2.getArray_as_double();
                     am.sortedIndices = this.sortWithIndices(cc);
                     for(int i=0; i<this.length; i++)am.array.add((Complex)this.array.get(am.sortedIndices[i]));
                     break;
            case 15: ArrayMaths am3 = this.abs();
                     double[] pp = am3.getArray_as_double();
                     am.sortedIndices = this.sortWithIndices(pp);
                     for(int i=0; i<this.length; i++)am.array.add((Phasor)this.array.get(am.sortedIndices[i]));
                     break;
            case 16:
            case 17: long[]ii = this.getArray_as_long();
                     am.sortedIndices = this.sortWithIndices(ii);
                     for(int i=0; i<this.length; i++)am.array.add((Character)this.array.get(am.sortedIndices[i]));
                     break;
            case 18: String[] ww = this.getArray_as_String();
                     ArrayList<Object> al = this.alphabeticSort(ww);
                     String[] www = (String[])al.get(0);
                     for(int i=0; i<this.length; i++)am.array.add((String)www[i]);
                     am.sortedIndices =  (int[])al.get(1);
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        if(this.type!=18){
            int[] maxminIndices = new int[2];
            ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
            am.maxIndex = maxminIndices[0];
            am.minIndex = maxminIndices[1];
        }

        Conv.restoreMessages();
        return am;
    }

    // alphabetic sort
    public ArrayList<Object> alphabeticSort(String[] words){

        this.words = words;
        this.nWords = words.length;

        this.sortedWords = new String[this.nWords];
        for(int i=0; i<this.nWords; i++)this.sortedWords[i] = this.words[i];

        this.alphabeticSortIndices(words);

        for(int i=0; i<this.nWords; i++){
            sortedWords[i] = words[wordOrder[i]];
        }

        ArrayList<Object> ret = new ArrayList<Object>();
        ret.add(sortedWords);
        ret.add(wordOrder);

        return ret;
    }

    // get sorted indices
    public void alphabeticSortIndices(String[] words){

        // copy and pad to longest word
        String[] copy = new String[this.nWords];
        this.nLength = 0;
        int nn = 0;
        for(int i=0; i<this.nWords; i++){
            nn = words[i].length();
            if(nn>this.nLength)this.nLength = nn;
            copy[i] = words[i].toLowerCase();
        }

        // convert to character arrays
        this.wordChar = new char[this.nWords][this.nLength];
        this.holdWordChar = new char[this.nWords][this.nLength];
        for(int i=0; i<this.nWords; i++){
            char[] holdc = copy[i].toCharArray();
            for(int j=0; j<copy[i].length(); j++){
                this.wordChar[i][j] = holdc[j];
                this.holdWordChar[i][j] = holdc[j];
            }
            for(int j=copy[i].length(); j<this.nLength; j++){
                this.wordChar[i][j] = ' ';
                this.holdWordChar[i][j] = ' ';
            }
        }

        // set word order indices
        this.wordOrder = new int[this.nWords];
        this.holdWordOrder = new int[this.nWords];
        for(int i=0; i<this.nWords; i++){
            this.wordOrder[i] = i;
            this.holdWordOrder[i] = i;
        }

        // sort characters
        if(this.nWords>1)this.indexSort(0, 0, this.nWords-1);
    }

    public void indexSort(int ii, int iStart, int iEnd){

        int n = iEnd - iStart + 1;
        char[] colChar = new char[n];
        int[] colInt = new int[n];
        for(int i=0; i<n; i++){
            colChar[i] = this.wordChar[i + iStart][ii];
            colInt[i] = this.wordOrder[i + iStart];
        }

        int[] indChar = this.sortByColumnInitial(colChar);

        int[] holdi = new int[n];
        for(int i=0; i<n; i++){
            holdi[i] = colInt[indChar[i]];
        }



        // rearrange words and their indices
        for(int i=0; i<n; i++){
            this.holdWordOrder[i+iStart] = holdi[i];
            this.holdWordChar[i+iStart] = this.wordChar[indChar[i]+iStart];
        }
        for(int i=0; i<this.nWords; i++){
            this.wordChar[i] = this.holdWordChar[i];
            this.wordOrder[i] = this.holdWordOrder[i];
        }


        ArrayList<Integer> al = null;
        if(ii<this.nLength-1){

            int[] test = new int[n+1];
            int one = 1;
            test[0] = one;
            int jj = 1;
            for(int i=iStart+1; i<=iEnd; i++){
                if((int)this.wordChar[i][ii]==(int)this.wordChar[i-1][ii]){
                    test[jj] = one;
                }
                else{
                    one = -one;
                    test[jj] = one;
                }
                jj++;
            }

            al = new ArrayList<Integer>();
            jj = iStart;
            al.add(new Integer(jj));
            for(int i=1; i<=n; i++){
                jj++;
                if(test[i]!=test[i-1]){
                    al.add(new Integer(jj-1));
                    al.add(new Integer(jj));
                }
            }
            al.add(new Integer(iEnd));

            int nSub = al.size();
            for(int i=0; i<nSub; i+=2){
                int vs = (al.get(i)).intValue();
                int ve = (al.get(i+1)).intValue();
                if((ve-vs+1)>1)this.indexSort(ii+1, vs, ve);
            }
        }
    }

    // sort by initial letter
    public int[] sortByColumnInitial(char[] initials){
        int n = initials.length;
        int[] intChar = new int[n];
        for(int i=0; i<n; i++)intChar[i] = (int)initials[i];
        ArrayMaths ami = new ArrayMaths(intChar);
        ArrayMaths isorted = ami.sort();
        int[] iarraysorted = isorted.array_as_int();
        int[] iindices = isorted.originalIndices();

        return iindices;
    }
    
    // sort the array into ascending order
    // saves sorted array in this instance
    public void sortEquals(int[] indices){
        ArrayMaths am = this.sort(indices);
        this.array = am.array;
        this.sortedIndices = am.sortedIndices;
        this.maxIndex = am.maxIndex;
        this.minIndex = am.minIndex;   
    }

    // order an array to a given sequence of indices
    // sorted array returned in a new instance of ArrayMaths
    public ArrayMaths sort(int[] indices){
        int nArg = indices.length;
        if(this.length!=nArg)throw new IllegalArgumentException("The argument array [length = " + nArg + "], must be of the same length as this instance array [length = " + this.length +"]");

        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = this.length;
        am.type = this.type;
        am.sortedIndices = indices;
        switch(this.type){
            case 0:
            case 1: for(int i=0; i<this.length; i++)am.array.add((Double)this.array.get(am.sortedIndices[i]));
                    break;
            case 2:
            case 3: for(int i=0; i<this.length; i++)am.array.add((Float)this.array.get(am.sortedIndices[i]));
                    break;
            case 4:
            case 5: for(int i=0; i<this.length; i++)am.array.add((Long)this.array.get(am.sortedIndices[i]));
                    break;
            case 6:
            case 7: for(int i=0; i<this.length; i++)am.array.add((Integer)this.array.get(am.sortedIndices[i]));
                    break;
            case 8:
            case 9: for(int i=0; i<this.length; i++)am.array.add((Short)this.array.get(am.sortedIndices[i]));
                    break;
            case 10:
            case 11: for(int i=0; i<this.length; i++)am.array.add((Byte)this.array.get(am.sortedIndices[i]));
                     break;
            case 12: for(int i=0; i<this.length; i++)am.array.add((BigDecimal)this.array.get(am.sortedIndices[i]));
                     break;
            case 13: for(int i=0; i<this.length; i++)am.array.add((BigInteger)this.array.get(am.sortedIndices[i]));
                     break;
            case 14: for(int i=0; i<this.length; i++)am.array.add((Complex)this.array.get(am.sortedIndices[i]));
                     break;
            case 15: for(int i=0; i<this.length; i++)am.array.add((Phasor)this.array.get(am.sortedIndices[i]));
                     break;
            case 16:
            case 17: for(int i=0; i<this.length; i++)am.array.add((Character)this.array.get(am.sortedIndices[i]));
                     break;
            case 18: for(int i=0; i<this.length; i++)am.array.add((String)this.array.get(am.sortedIndices[i]));
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        if(this.type!=18)ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }


    // sort elements in an array into ascending order
    // using selection sort method
    // returns indices of the sorted array
    protected int[] sortWithIndices(double[] aa){

            int index = 0;
            int lastIndex = -1;
            double holdb = 0.0D;
            int holdi = 0;
            double[] bb = new double[this.length];
            int[] indices = new int[this.length];
            for(int i=0; i<this.length; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != this.length-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<this.length; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }
            return indices;
    }

    // protected method for obtaining original indices of a sorted array
    // called by public sort methods
    protected int[] sortWithIndices(long[] aa){
            int index = 0;
            int lastIndex = -1;
            long holdb = 0L;
            int holdi = 0;
            long[] bb = new long[this.length];
            int[] indices = new int[this.length];
            for(int i=0; i<this.length; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != this.length-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<this.length; i++){
                    if(bb[i]<bb[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }
            return indices;
    }

    // protected method for obtaining original indices of a sorted array
    // called by public sort methods
    protected int[] sortWithIndices(BigDecimal[] aa){
            int index = 0;
            int lastIndex = -1;
            BigDecimal holdb = BigDecimal.ZERO;
            int holdi = 0;
            BigDecimal[] bb = new BigDecimal[this.length];
            int[] indices = new int[this.length];
            for(int i=0; i<this.length; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != this.length-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<this.length; i++){
                     if(bb[i].compareTo(bb[index])==-1){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }

            holdb = null;
            return indices;
    }

    // protected method for obtaining original indices of a sorted array
    // called by public sort methods
    protected int[] sortWithIndices(BigInteger[] aa){
            int index = 0;
            int lastIndex = -1;
            BigInteger holdb = BigInteger.ZERO;
            int holdi = 0;
            BigInteger[] bb = new BigInteger[this.length];
            int[] indices = new int[this.length];
            for(int i=0; i<this.length; i++){
                bb[i]=aa[i];
                indices[i]=i;
            }

            while(lastIndex != this.length-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<this.length; i++){
                     if(bb[i].compareTo(bb[index])==-1){
                        index=i;
                    }
                }
                lastIndex++;
                holdb=bb[index];
                bb[index]=bb[lastIndex];
                bb[lastIndex]=holdb;
                holdi=indices[index];
                indices[index]=indices[lastIndex];
                indices[lastIndex]=holdi;
            }

            holdb = null;
            return indices;
    }


    // return original indices of sorted array
    public int[] originalIndices(){
        if(this.sortedIndices==null)System.out.println("method: originalIndices: array has not been sorted: null returned");
        return this.sortedIndices;
    }


    // concatenates two arrays
    public ArrayMaths concatenate(double[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: double[] yy = this.getArray_as_double();
                     double[] zz = new double[am.length];
                     for(int i=0; i<this.length; i++)zz[i] = yy[i];
                     for(int i=0; i<xlength; i++)zz[i+this.length] = xx[i];
                     for(int i=0; i<am.length; i++)am.array.add(new Double(zz[i]));
                     am.type = 1;
                     break;
            case 12:
            case 13: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

     public ArrayMaths concatenate(Double[] xx){
        double[] dd = new double[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].doubleValue();
        return this.concatenate(dd);
     }


    // concatenates two arrays
    public ArrayMaths concatenate(float[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:double[] yy = this.getArray_as_double();
                   double[] zz = new double[am.length];
                   for(int i=0; i<this.length; i++)zz[i] = yy[i];
                   for(int i=0; i<xlength; i++)zz[i+this.length] = (double)xx[i];
                   for(int i=0; i<am.length; i++)am.array.add(new Double(zz[i]));
                   am.type = 1;
                   break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: float[] ff = this.getArray_as_float();
                     float[] gg = new float[am.length];
                     for(int i=0; i<this.length; i++)gg[i] = ff[i];
                     for(int i=0; i<xlength; i++)gg[i+this.length] = xx[i];
                     for(int i=0; i<am.length; i++)am.array.add(new Float(gg[i]));
                     am.type = 3;
                     break;
            case 12:
            case 13: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Float[] xx){
        float[] dd = new float[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].floatValue();
        return this.concatenate(dd);
     }

    // concatenates two arrays
    public ArrayMaths concatenate(long[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:double[] yy = this.getArray_as_double();
                   double[] zz = new double[am.length];
                   for(int i=0; i<this.length; i++)zz[i] = yy[i];
                   for(int i=0; i<xlength; i++)zz[i+this.length] = (double)xx[i];
                   for(int i=0; i<am.length; i++)am.array.add(new Double(zz[i]));
                   am.type = 1;
                   break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17: long[] ll = this.getArray_as_long();
                     long[] mm = new long[am.length];
                     for(int i=0; i<this.length; i++)mm[i] = ll[i];
                     for(int i=0; i<xlength; i++)mm[i+this.length] = xx[i];
                     for(int i=0; i<am.length; i++)am.array.add(new Long(mm[i]));
                     am.type = 3;
                     break;
            case 12: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 13: BigInteger[] bi1 = this.getArray_as_BigInteger();
                     am2 = new ArrayMaths(xx);
                     BigInteger[] bi2 = am2.getArray_as_BigInteger();
                     BigInteger[] bia = new BigInteger[am.length];
                     for(int i=0; i<this.length; i++)bia[i] = bi1[i];
                     for(int i=0; i<xlength; i++)bia[i+this.length] = bi2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bia[i]);
                     bi1 = null;
                     bi2 = null;
                     bia = null;
                     am.type = 13;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Long[] xx){
        long[] dd = new long[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].longValue();
        return this.concatenate(dd);
     }

    // concatenates two arrays
    public ArrayMaths concatenate(int[] xx){
        long[] dd = new long[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = (long)xx[i];
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Integer[] xx){
        int[] dd = new int[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].intValue();
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(short[] xx){
        long[] dd = new long[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = (long)xx[i];
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Short[] xx){
        short[] dd = new short[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].shortValue();
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(byte[] xx){
        long[] dd = new long[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = (long)xx[i];
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Byte[] xx){
        byte[] dd = new byte[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].byteValue();
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(BigDecimal[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

      public ArrayMaths concatenate(BigInteger[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 12: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 16:
            case 17: BigInteger[] bi1 = this.getArray_as_BigInteger();
                     am2 = new ArrayMaths(xx);
                     BigInteger[] bi2 = am2.getArray_as_BigInteger();
                     BigInteger[] bia = new BigInteger[am.length];
                     for(int i=0; i<this.length; i++)bia[i] = bi1[i];
                     for(int i=0; i<xlength; i++)bia[i+this.length] = bi2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bia[i]);
                     bi1 = null;
                     bi2 = null;
                     bia = null;
                     am.type = 13;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Complex[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:  Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Phasor[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:  Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
             case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(String[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(char[] xx){
        int xlength = xx.length;
        if(this.suppressMessages)Conv.suppressMessages();
        ArrayMaths am = new ArrayMaths();
        am.array = new ArrayList<Object>();
        am.length = xx.length + this.length;
        ArrayMaths am2 = null;
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:double[] yy = this.getArray_as_double();
                   double[] zz = new double[am.length];
                   for(int i=0; i<this.length; i++)zz[i] = yy[i];
                   for(int i=0; i<xlength; i++)zz[i+this.length] = (double)xx[i];
                   for(int i=0; i<am.length; i++)am.array.add(new Double(zz[i]));
                   am.type = 1;
                   break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11: long[] ll = this.getArray_as_long();
                     long[] mm = new long[am.length];
                     for(int i=0; i<this.length; i++)mm[i] = ll[i];
                     for(int i=0; i<xlength; i++)mm[i+this.length] = xx[i];
                     for(int i=0; i<am.length; i++)am.array.add(new Long(mm[i]));
                     am.type = 3;
                     break;
            case 12: BigDecimal[] bd1 = this.getArray_as_BigDecimal();
                     am2 = new ArrayMaths(xx);
                     BigDecimal[] bd2 = am2.getArray_as_BigDecimal();
                     BigDecimal[] bda = new BigDecimal[am.length];
                     for(int i=0; i<this.length; i++)bda[i] = bd1[i];
                     for(int i=0; i<xlength; i++)bda[i+this.length] = bd2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bda[i]);
                     bd1 = null;
                     bd2 = null;
                     bda = null;
                     am.type = 12;
                     break;
            case 13: BigInteger[] bi1 = this.getArray_as_BigInteger();
                     am2 = new ArrayMaths(xx);
                     BigInteger[] bi2 = am2.getArray_as_BigInteger();
                     BigInteger[] bia = new BigInteger[am.length];
                     for(int i=0; i<this.length; i++)bia[i] = bi1[i];
                     for(int i=0; i<xlength; i++)bia[i+this.length] = bi2[i];
                     for(int i=0; i<am.length; i++)am.array.add(bia[i]);
                     bi1 = null;
                     bi2 = null;
                     bia = null;
                     am.type = 13;
                     break;
            case 14: Complex[] cc1 = this.getArray_as_Complex();
                     am2 = new ArrayMaths(xx);
                     Complex[] cc2 = am2.getArray_as_Complex();
                     Complex[] cca = new Complex[am.length];
                     for(int i=0; i<this.length; i++)cca[i] = cc1[i];
                     for(int i=0; i<xlength; i++)cca[i+this.length] = cc2[i];
                     for(int i=0; i<am.length; i++)am.array.add(cca[i]);
                     am.type = 14;
                     break;
            case 15: Phasor[] pp1 = this.getArray_as_Phasor();
                     am2 = new ArrayMaths(xx);
                     Phasor[] pp2 = am2.getArray_as_Phasor();
                     Phasor[] ppa = new Phasor[am.length];
                     for(int i=0; i<this.length; i++)ppa[i] = pp1[i];
                     for(int i=0; i<xlength; i++)ppa[i+this.length] = pp2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ppa[i]);
                     am.type = 15;
                     break;
            case 16:
            case 17: char[] ch = this.getArray_as_char();
                     char[] dh = new char[am.length];
                     for(int i=0; i<this.length; i++)dh[i] = ch[i];
                     for(int i=0; i<xlength; i++)dh[i+this.length] = xx[i];
                     for(int i=0; i<am.length; i++)am.array.add(new Character(dh[i]));
                     am.type = 1;
                     break;
            case 18: String[] ss1 = this.getArray_as_String();
                     am2 = new ArrayMaths(xx);
                     String[] ss2 = am2.getArray_as_String();
                     String[] ssa = new String[am.length];
                     for(int i=0; i<this.length; i++)ssa[i] = ss1[i];
                     for(int i=0; i<xlength; i++)ssa[i+this.length] = ss2[i];
                     for(int i=0; i<am.length; i++)am.array.add(ssa[i]);
                     am.type = 18;
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Character[] xx){
        char[] dd = new char[xx.length];
        for(int i=0; i<xx.length; i++)dd[i] = xx[i].charValue();
        return this.concatenate(dd);
    }

    // concatenates two arrays
    public ArrayMaths concatenate(ArrayMaths xx){
        if(this.suppressMessages)Conv.suppressMessages();
        int type = xx.type;
        ArrayMaths am = new ArrayMaths();
        switch(xx.type){
            case 0:
            case 1: double[] dd = xx.getArray_as_double();
                    am = this.concatenate(dd);
                    break;
            case 2:
            case 3: float[] ff = xx.getArray_as_float();
                    am = this.concatenate(ff);
                    break;
            case 4:
            case 5: long[] ll = xx.getArray_as_long();
                    am = this.concatenate(ll);
                    break;
            case 6:
            case 7: int[] ii = xx.getArray_as_int();
                    am = this.concatenate(ii);
                    break;
            case 8:
            case 9: short[] ss = xx.getArray_as_short();
                    am = this.concatenate(ss);
                    break;
            case 10:
            case 11: byte[] bb = xx.getArray_as_byte();
                    am = this.concatenate(bb);
                    break;
            case 12: BigDecimal[] bd = xx.getArray_as_BigDecimal();
                     am = this.concatenate(bd);
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     am = this.concatenate(bi);
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                     am = this.concatenate(cc);
                     break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                     am = this.concatenate(pp);
                     break;
            case 16:
            case 17: char[] ct = this.getArray_as_char();
                     am = this.concatenate(ct);
                     break;
            case 18: String[] st = this.getArray_as_String();
                     am = this.concatenate(st);
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }

    // concatenates two arrays
    public ArrayMaths concatenate(Stat xx){
        if(this.suppressMessages)Conv.suppressMessages();
        int type = xx.type;
        ArrayMaths am = new ArrayMaths();
        switch(xx.type){
            case 0:
            case 1: double[] dd = xx.getArray_as_double();
                    am = this.concatenate(dd);
                    break;
            case 2:
            case 3: float[] ff = xx.getArray_as_float();
                    am = this.concatenate(ff);
                    break;
            case 4:
            case 5: long[] ll = xx.getArray_as_long();
                    am = this.concatenate(ll);
                    break;
            case 6:
            case 7: int[] ii = xx.getArray_as_int();
                    am = this.concatenate(ii);
                    break;
            case 8:
            case 9: short[] ss = xx.getArray_as_short();
                    am = this.concatenate(ss);
                    break;
            case 10:
            case 11: byte[] bb = xx.getArray_as_byte();
                    am = this.concatenate(bb);
                    break;
            case 12: BigDecimal[] bd = xx.getArray_as_BigDecimal();
                     am = this.concatenate(bd);
                     break;
            case 13: BigInteger[] bi = this.getArray_as_BigInteger();
                     am = this.concatenate(bi);
                     break;
            case 14: Complex[] cc = this.getArray_as_Complex();
                     am = this.concatenate(cc);
                     break;
            case 15: Phasor[] pp = this.getArray_as_Phasor();
                     am = this.concatenate(pp);
                     break;
            case 16:
            case 17: char[] ct = this.getArray_as_char();
                     am = this.concatenate(ct);
                     break;
            case 18: String[] st = this.getArray_as_String();
                     am = this.concatenate(st);
                     break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
        int[] maxminIndices = new int[2];
        ArrayMaths.findMinMax(am.getArray_as_Object(), am.minmax, maxminIndices, am.typeName, am.type);
        am.maxIndex = maxminIndices[0];
        am.minIndex = maxminIndices[1];

        Conv.restoreMessages();
        return am;
    }


    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(double value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==0 || this.type==1){
            double[] arrayc = this.getArray_as_double();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare double or Double with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Double value){
        double val = value.doubleValue();
        return this.indexOf(val);
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(float value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==2 || this.type==3){
            float[] arrayc = this.getArray_as_float();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare float or Float with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Float value){
        float val = value.floatValue();
        return this.indexOf(val);
    }


    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(long value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==4 || this.type==5){
            long[] arrayc = this.getArray_as_long();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare long or Long with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Long value){
        long val = value.longValue();
        return this.indexOf(val);
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(int value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==6 || this.type==7){
            int[] arrayc = this.getArray_as_int();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare int or Integer with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Integer value){
        int val = value.intValue();
        return this.indexOf(val);
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(short value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==8 || this.type==9){
            short[] arrayc = this.getArray_as_short();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare short or Short with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Short value){
        short val = value.shortValue();
        return this.indexOf(val);
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(byte value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==10 || this.type==11){
            byte[] arrayc = this.getArray_as_byte();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare byte or Byte with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Byte value){
        byte val = value.byteValue();
        return this.indexOf(val);
    }


    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(char value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==16 || this.type==17){
            char[] arrayc = this.getArray_as_char();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter]==value){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare char or Character with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Character value){
        char val = value.charValue();
        return this.indexOf(val);
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(String value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==18){
            String[] arrayc = this.getArray_as_String();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter].equals(value)){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare String with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Complex value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==14){
            Complex[] arrayc = this.getArray_as_Complex();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter].equals(value)){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare Complex with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(Phasor value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==15){
            Phasor[] arrayc = this.getArray_as_Phasor();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter].equals(value)){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare Phasor with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(BigDecimal value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==12){
            BigDecimal[] arrayc = this.getArray_as_BigDecimal();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter].compareTo(value)==0){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigDecimal with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the element equal to a given value in an array
    // returns -1 if none found
    public int indexOf(BigInteger value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = -1;
        if(this.type==13){
            BigInteger[] arrayc = this.getArray_as_BigInteger();
            boolean test = true;
            int counter = 0;
            while(test){
                if(arrayc[counter].compareTo(value)==0){
                    index = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter>=arrayc.length)test = false;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigInteger with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }


    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(double value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==0 || this.type==1){
            double[] arrayc = this.getArray_as_double();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare double or Double with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Double value){
        double val = value.doubleValue();
        return this.indicesOf(val);
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(float value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==2 || this.type==3){
            float[] arrayc = this.getArray_as_float();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare float or Float with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Float value){
        float val = value.floatValue();
        return this.indicesOf(val);
    }


    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(long value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==4 || this.type==5){
            long[] arrayc = this.getArray_as_long();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare long or Long with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Long value){
        long val = value.longValue();
        return this.indicesOf(val);
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(int value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==6 || this.type==7){
            int[] arrayc = this.getArray_as_int();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare int or Integer with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Integer value){
        int val = value.intValue();
        return this.indicesOf(val);
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(short value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==8 || this.type==9){
            short[] arrayc = this.getArray_as_short();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare short or Short with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Short value){
        short val = value.shortValue();
        return this.indicesOf(val);
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(byte value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==10 || this.type==11){
            byte[] arrayc = this.getArray_as_byte();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare byte or Byte with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Byte value){
        byte val = value.byteValue();
        return this.indicesOf(val);
    }


    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(char value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==16 || this.type==17){
            char[] arrayc = this.getArray_as_char();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i]==value){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare char or Character with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Character value){
        char val = value.charValue();
        return this.indicesOf(val);
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(String value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==18){
            String[] arrayc = this.getArray_as_String();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i].equals(value)){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare String with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Complex value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==14){
            Complex[] arrayc = this.getArray_as_Complex();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i].equals(value)){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare Complex with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(Phasor value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==15){
            Phasor[] arrayc = this.getArray_as_Phasor();
            ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i].equals(value)){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare Phasor with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(BigDecimal value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==12){
            BigDecimal[] arrayc = this.getArray_as_BigDecimal();
              ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i].compareTo(value)==0){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigDecimal with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds all indices of the occurences of the element equal to a given value in an array
    // returns null if none found
    public int[] indicesOf(BigInteger value){
        if(this.suppressMessages)Conv.suppressMessages();
        int[] indices = null;
        int numberOfIndices = 0;
        if(this.type==13){
            BigInteger[] arrayc = this.getArray_as_BigInteger();
              ArrayList<Integer> arrayl = new ArrayList<Integer>();
            for(int i=0; i<this.length; i++){
                if(arrayc[i].compareTo(value)==0){
                    numberOfIndices++;
                    arrayl.add(new Integer(i));
                }
            }
            if(numberOfIndices!=0){
                indices = new int[numberOfIndices];
                for(int i=0; i<numberOfIndices; i++){
                    indices[i] = (arrayl.get(i)).intValue();
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigInteger with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return indices;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(double value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==0 || this.type==1){
            double[] arrayc = this.getArray_as_double();
            double diff = Math.abs(arrayc[0] - value);
            double nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = Math.abs(arrayc[i] - value);
                    index = i;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare double or Double with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Double value){
        double val = value.doubleValue();
        return this.nearestIndex(val);
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(float value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==2 || this.type==3){
            float[] arrayc = this.getArray_as_float();
            float diff = Math.abs(arrayc[0] - value);
            float nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = Math.abs(arrayc[i] - value);
                    index = i;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare float or Float with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Float value){
        float val = value.floatValue();
        return this.nearestIndex(val);
    }


    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(long value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==4 || this.type==5){
            long[] arrayc = this.getArray_as_long();
            long diff = Math.abs(arrayc[0] - value);
            long nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = Math.abs(arrayc[i] - value);
                    index = i;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare long or Long with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Long value){
        long val = value.longValue();
        return this.nearestIndex(val);
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(int value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==6 || this.type==7){
            int[] arrayc = this.getArray_as_int();
            int diff = Math.abs(arrayc[0] - value);
            int nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = Math.abs(arrayc[i] - value);
                    index = i;
                }
            }

        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare int or Integer with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Integer value){
        int val = value.intValue();
        return this.nearestIndex(val);
    }


    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(short value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==8 || this.type==9){
            short[] arrayc = this.getArray_as_short();
            short diff = (short)Math.abs(arrayc[0] - value);
            short nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = (short)Math.abs(arrayc[i] - value);
                    index = i;
                }
            }

        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare short or Short with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Short value){
        short val = value.shortValue();
        return this.nearestIndex(val);
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(byte value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==10 || this.type==11){
            byte[] arrayc = this.getArray_as_byte();
            byte diff = (byte)Math.abs(arrayc[0] - value);
            byte nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = (byte)Math.abs(arrayc[i] - value);
                    index = i;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare byte or Byte with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Byte value){
        byte val = value.byteValue();
        return this.nearestIndex(val);
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(char value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==16 || this.type==17){
            int[] arrayc = this.getArray_as_int();
            int diff = Math.abs(arrayc[0] - value);
            int nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(Math.abs(arrayc[i] - value)<diff){
                    diff = Math.abs(arrayc[i] - value);
                    index = i;
                }
            }
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare char or Character with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(Character value){
        char val = value.charValue();
        return this.nearestIndex(val);
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(BigDecimal value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==12){
            BigDecimal[] arrayc = this.getArray_as_BigDecimal();
            BigDecimal diff =  (arrayc[0].subtract(value)).abs();
            BigDecimal nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(((arrayc[i].subtract(value)).abs( )).compareTo(diff)==-1){
                    diff = (arrayc[i].subtract(value)).abs();
                    index = i;
                }
            }
            arrayc = null;
            diff = null;
            nearest = null;
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigDecimal with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the index of the first occurence of the nearest element to a given value in an array
    public int nearestIndex(BigInteger value){
        if(this.suppressMessages)Conv.suppressMessages();
        int index = 0;
        if(this.type==12){
            BigInteger[] arrayc = this.getArray_as_BigInteger();
            BigInteger diff =  (arrayc[0].subtract(value)).abs();
            BigInteger nearest = arrayc[0];
            for(int i=1; i<arrayc.length; i++){
                if(((arrayc[i].subtract(value)).abs( )).compareTo(diff)==-1){
                    diff = (arrayc[i].subtract(value)).abs();
                    index = i;
                }
            }
            arrayc = null;
            diff = null;
            nearest = null;
        }
        else{
            throw new IllegalArgumentException("Only comparisons between the same data types are supported - you are attempting to compare BigInteger with " + this.typeName[this.type]);
        }
        Conv.restoreMessages();
        return index;
    }

    // finds the value of the nearest element to a given value in an array
    public double nearestValue(double value){
        int index = this.nearestIndex(value);
        double ret = ((Double)(this.array.get(index))).doubleValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Double nearestValue(Double value){
        int index = this.nearestIndex(value);
        Double ret = (Double)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public float nearestValue(float value){
        int index = this.nearestIndex(value);
        float ret = ((Float)(this.array.get(index))).floatValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Float nearestValue(Float value){
        int index = this.nearestIndex(value);
        Float ret = (Float)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public long nearestValue(long value){
        int index = this.nearestIndex(value);
        long ret = ((Long)(this.array.get(index))).longValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Long nearestValue(Long value){
        int index = this.nearestIndex(value);
        Long ret = (Long)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public int nearestValue(int value){
        int index = this.nearestIndex(value);
        int ret = ((Integer)(this.array.get(index))).intValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Integer nearestValue(Integer value){
        int index = this.nearestIndex(value);
        Integer ret = (Integer)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public short nearestValue(short value){
        int index = this.nearestIndex(value);
        short ret = ((Short)(this.array.get(index))).shortValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Short nearestValue(Short value){
        int index = this.nearestIndex(value);
        Short ret = (Short)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public byte nearestValue(byte value){
        int index = this.nearestIndex(value);
        byte ret = ((Byte)(this.array.get(index))).byteValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Byte nearestValue(Byte value){
        int index = this.nearestIndex(value);
        Byte ret = (Byte)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public char nearestValue(char value){
        int index = this.nearestIndex(value);
        char ret = ((Character)(this.array.get(index))).charValue();
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public Character nearestValue(Character value){
        int index = this.nearestIndex(value);
        Character ret = (Character)(this.array.get(index));
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public BigDecimal nearestValue(BigDecimal value){
        int index = this.nearestIndex(value);
        BigDecimal ret = (BigDecimal)this.array.get(index);
        return ret;
    }

    // finds the value of the nearest element to a given value in an array
    public BigInteger nearestValue(BigInteger value){
        int index = this.nearestIndex(value);
        BigInteger ret = (BigInteger)this.array.get(index);
        return ret;
    }

    // return maximum difference, i.e. range
    public double maximumDifference(){
        return this.getMaximumDifference_as_double();
    }

    public double maximumDifference_as_double(){
        return this.getMaximumDifference_as_double();
    }

    public double getMaximumDifference(){
        return this.getMaximumDifference_as_double();
    }

    public double getMaximumDifference_as_double(){
        double diff = 0.0D;
        if(this.type==0 || this.type==1){
            double max = this.getMaximum_as_double();
            double min = this.getMinimum_as_double();
            diff = max - min;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as double or Double the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public Double maximumDifference_as_Double(){
        return this.getMaximumDifference_as_Double();
    }

    public Double getMaximumDifference_as_Double(){
        return new Double(this.getMaximumDifference_as_double());
    }

    // return maximum difference, i.e. range
    public float maximumDifference_as_float(){
        return this.getMaximumDifference_as_float();
    }

    public float getMaximumDifference_as_float(){
        float diff = 0.0F;
        if(this.type==2 || this.type==3){
            float max = this.getMaximum_as_float();
            float min = this.getMinimum_as_float();
            diff = max - min;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as float or Float the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public Float maximumDifference_as_Float(){
        return this.getMaximumDifference_as_Float();
    }

    public Float getMaximumDifference_as_Float(){
        return new Float(this.getMaximumDifference_as_float());
    }

    // return maximum difference, i.e. range
    public long maximumDifference_as_long(){
        return this.getMaximumDifference_as_long();
    }

    public long getMaximumDifference_as_long(){
        long diff = 0L;
        if(this.type==4 || this.type==5){
            long max = this.getMaximum_as_long();
            long min = this.getMinimum_as_long();
            diff = max - min;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as long or Long the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
     public Long maximumDifference_as_Long(){
        return this.getMaximumDifference_as_Long();
    }

    public Long getMaximumDifference_as_Long(){
        return new Long(this.getMaximumDifference_as_long());
    }

    // return maximum difference, i.e. range
    public int maximumDifference_as_int(){
        return this.getMaximumDifference_as_int();
    }

    public int getMaximumDifference_as_int(){
        int diff = 0;
        if(this.type==6 || this.type==7){
            int max = this.getMaximum_as_int();
            int min = this.getMinimum_as_int();
            diff = max - min;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as int or Integer the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public Integer maximumDifference_as_Integer(){
        return this.getMaximumDifference_as_Integer();
    }

    public Integer getMaximumDifference_as_Integer(){
        return new Integer(this.getMaximumDifference_as_int());
    }

    // return maximum difference, i.e. range
    public short maximumDifference_as_short(){
        return this.getMaximumDifference_as_short();
    }

    public short getMaximumDifference_as_short(){
        short diff = (short) 0;
        if(this.type==8 || this.type==9){
            short max = this.getMaximum_as_short();
            short min = this.getMinimum_as_short();
            diff = (short)(max - min);
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as short or Short the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public Short maximumDifference_as_Short(){
        return this.getMaximumDifference_as_Short();
    }

    public Short getMaximumDifference_as_Short(){
        return new Short(this.getMaximumDifference_as_short());
    }

    // return maximum difference, i.e. range
    public byte maximumDifference_as_byte(){
        return this.getMaximumDifference_as_byte();
    }

    public byte getMaximumDifference_as_byte(){
        byte diff = (byte) 0;
        if(this.type==10 || this.type==11){
            byte max = this.getMaximum_as_byte();
            byte min = this.getMinimum_as_byte();
            diff = (byte)(max - min);
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as byte or Byte the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public Byte maximumDifference_as_Byte(){
        return this.getMaximumDifference_as_Byte();
    }

    public Byte getMaximumDifference_as_Byte(){
        return new Byte(this.getMaximumDifference_as_byte());
    }

    // return maximum difference, i.e. range
    public BigDecimal maximumDifference_as_BigDecimal(){
        return this.getMaximumDifference_as_BigDecimal();
    }

    public BigDecimal getMaximumDifference_as_BigDecimal(){
        BigDecimal diff = BigDecimal.ZERO;
        if(this.type==12){
            BigDecimal max = this.getMaximum_as_BigDecimal();
            BigDecimal min = this.getMinimum_as_BigDecimal();
            diff = max.subtract(min);
            max = null;
            min = null;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as BigDecimal the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return maximum difference, i.e. range
    public BigInteger maximumDifference_as_BigInteger(){
        return this.getMaximumDifference_as_BigInteger();
    }

    public BigInteger getMaximumDifference_as_BigInteger(){
        BigInteger diff = BigInteger.ZERO;
        if(this.type==13){
            BigInteger max = this.getMaximum_as_BigInteger();
            BigInteger min = this.getMinimum_as_BigInteger();
            diff = max.subtract(min);
            max = null;
            min = null;
        }
        else{
            throw new IllegalArgumentException("Maximum difference may only be returned as the same type as the type of the internal array - you are trying to return as BigInteger the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public double minimumDifference(){
        return this.getMinimumDifference_as_double();
    }

    public double minimumDifference_as_double(){
        return this.getMinimumDifference_as_double();
    }

    public double getMinimumDifference(){
        return this.getMinimumDifference_as_double();
    }

    public double getMinimumDifference_as_double(){
        double diff = 0.0D;
        if(this.type==0 || this.type==1){
            ArrayMaths am = this.sort();
            double[] sorted = am.getArray_as_double();
            diff = sorted[1] - sorted[0];
            double minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as double or Double the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Double minimumDifference_as_Double(){
        return this.getMinimumDifference_as_Double();
    }

    public Double getMinimumDifference_as_Double(){
        return new Double(this.getMinimumDifference_as_double());
    }

    // return minimum difference
    public float minimumDifference_as_float(){
        return this.getMinimumDifference_as_float();
    }

    public float getMinimumDifference_as_float(){
        float diff = 0.0F;
        if(this.type==2 || this.type==3){
            ArrayMaths am = this.sort();
            float[] sorted = am.getArray_as_float();
            diff = sorted[1] - sorted[0];
            float minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as float or Float the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Float minimumDifference_as_Float(){
        return this.getMinimumDifference_as_Float();
    }

    public Float getMinimumDifference_as_Float(){
        return new Float(this.getMinimumDifference_as_float());
    }

    // return minimum difference
    public long minimumDifference_as_long(){
        return this.getMinimumDifference_as_long();
    }

    public long getMinimumDifference_as_long(){
        long diff = 0L;
        if(this.type==4 || this.type==5){
            ArrayMaths am = this.sort();
            long[] sorted = am.getArray_as_long();
            diff = sorted[1] - sorted[0];
            long minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as long or Long the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Long minimumDifference_as_Long(){
        return this.getMinimumDifference_as_Long();
    }

    public Long getMinimumDifference_as_Long(){
        return new Long(this.getMinimumDifference_as_long());
    }

    // return minimum difference
    public int minimumDifference_as_int(){
        return this.getMinimumDifference_as_int();
    }

    public int getMinimumDifference_as_int(){
        int diff = 0;
        if(this.type==6 || this.type==7){
            ArrayMaths am = this.sort();
            int[] sorted = am.getArray_as_int();
            diff = sorted[1] - sorted[0];
            int minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as int or Integer the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Integer minimumDifference_as_Integer(){
        return this.getMinimumDifference_as_Integer();
    }

    public Integer getMinimumDifference_as_Integer(){
        return new Integer(this.getMinimumDifference_as_int());
    }

    // return minimum difference
    public short minimumDifference_as_short(){
        return this.getMinimumDifference_as_short();
    }

    public short getMinimumDifference_as_short(){
        short diff = (short)0;
        if(this.type==8 || this.type==9){
            ArrayMaths am = this.sort();
            short[] sorted = am.getArray_as_short();
            diff = (short)(sorted[1] - sorted[0]);
            short minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = (short)(sorted[i+1] - sorted[i]);
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as short or Short the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Short minimumDifference_as_Short(){
        return this.getMinimumDifference_as_Short();
    }

    public Short getMinimumDifference_as_Short(){
        return new Short(this.getMinimumDifference_as_short());
    }


    // return minimum difference
    public byte minimumDifference_as_byte(){
        return this.getMinimumDifference_as_byte();
    }

    public byte getMinimumDifference_as_byte(){
        byte diff = (byte)0;
        if(this.type==10 || this.type==11){
            ArrayMaths am = this.sort();
            byte[] sorted = am.getArray_as_byte();
            diff = (byte)(sorted[1] - sorted[0]);
            byte minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = (byte)(sorted[i+1] - sorted[i]);
                if(diff<minDiff)minDiff = diff;
            }
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as byte or Byte the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public Byte minimumDifference_as_Byte(){
        return this.getMinimumDifference_as_Byte();
    }

    public Byte getMinimumDifference_as_Byte(){
        return new Byte(this.getMinimumDifference_as_byte());
    }

    // return minimum difference
    public BigDecimal minimumDifference_as_BigDecimal(){
        return this.getMinimumDifference_as_BigDecimal();
    }

    public BigDecimal getMinimumDifference_as_BigDecimal(){
        BigDecimal diff = BigDecimal.ZERO;
        if(this.type==12){
            ArrayMaths am = this.sort();
            BigDecimal[] sorted = am.getArray_as_BigDecimal();
            diff = sorted[1].subtract(sorted[0]);
            BigDecimal minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = (sorted[i+1].subtract(sorted[i]));
                if(diff.compareTo(minDiff)==-1)minDiff = diff;
            }
            sorted = null;
            minDiff = null;
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as BigDecimal the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // return minimum difference
    public BigInteger minimumDifference_as_BigInteger(){
        return this.getMinimumDifference_as_BigInteger();
    }

    public BigInteger getMinimumDifference_as_BigInteger(){
        BigInteger diff = BigInteger.ZERO;
        if(this.type==12){
            ArrayMaths am = this.sort();
            BigInteger[] sorted = am.getArray_as_BigInteger();
            diff = sorted[1].subtract(sorted[0]);
            BigInteger minDiff = diff;
            for(int i=1; i<this.length-1; i++){
                diff = (sorted[i+1].subtract(sorted[i]));
                if(diff.compareTo(minDiff)==-1)minDiff = diff;
            }
            sorted = null;
            minDiff = null;
        }
        else{
            throw new IllegalArgumentException("Minimum difference may only be returned as the same type as the type of the internal array - you are trying to return as BigInteger the difference for a " + this.typeName[this.type] +"[] array");
        }
        return diff;
    }

    // Print array to screen with no line returns
    public void print(){
        switch(this.type){
            case 0:
            case 1: Double[] dd = getArray_as_Double();
                    PrintToScreen.print(dd);
                    break;
            case 2:
            case 3: Float[] ff = getArray_as_Float();
                    PrintToScreen.print(ff);
                    break;
            case 4:
            case 5: Long[] ll = getArray_as_Long();
                    PrintToScreen.print(ll);
                    break;
            case 6:
            case 7: Integer[] ii = getArray_as_Integer();
                    PrintToScreen.print(ii);
                    break;
            case 8:
            case 9: Short[] ss = getArray_as_Short();
                    PrintToScreen.print(ss);
                    break;
            case 10:
            case 11: Byte[] bb = getArray_as_Byte();
                    PrintToScreen.print(bb);
                    break;
            case 12: BigDecimal[] bd = getArray_as_BigDecimal();
                    PrintToScreen.print(bd);
                    bd = null;
                    break;
            case 13: BigInteger[] bi = getArray_as_BigInteger();
                    PrintToScreen.print(bi);
                    bi = null;
                    break;
            case 14: Complex[] cc = getArray_as_Complex();
                    PrintToScreen.print(cc);
                    break;
            case 15: Phasor[] pp = getArray_as_Phasor();
                    PrintToScreen.print(pp);
                    break;
            case 16:
            case 17: Character[] ct = getArray_as_Character();
                    PrintToScreen.print(ct);
                    break;
            case 18: String[] st = getArray_as_String();
                    PrintToScreen.print(st);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
    }

    // Print array to screen with  line returns
    public void println(){
        switch(this.type){
            case 0:
            case 1: Double[] dd = getArray_as_Double();
                    PrintToScreen.println(dd);
                    break;
            case 2:
            case 3: Float[] ff = getArray_as_Float();
                    PrintToScreen.println(ff);
                    break;
            case 4:
            case 5: Long[] ll = getArray_as_Long();
                    PrintToScreen.println(ll);
                    break;
            case 6:
            case 7: Integer[] ii = getArray_as_Integer();
                    PrintToScreen.println(ii);
                    break;
            case 8:
            case 9: Short[] ss = getArray_as_Short();
                    PrintToScreen.println(ss);
                    break;
            case 10:
            case 11: Byte[] bb = getArray_as_Byte();
                    PrintToScreen.println(bb);
                    break;
            case 12: BigDecimal[] bd = getArray_as_BigDecimal();
                    PrintToScreen.println(bd);
                    bd = null;
                    break;
            case 13: BigInteger[] bi = getArray_as_BigInteger();
                    PrintToScreen.println(bi);
                    bi = null;
                    break;
            case 14: Complex[] cc = getArray_as_Complex();
                    PrintToScreen.println(cc);
                    break;
            case 15: Phasor[] pp = getArray_as_Phasor();
                    PrintToScreen.println(pp);
                    break;
            case 16:
            case 17: Character[] ct = getArray_as_Character();
                    PrintToScreen.println(ct);
                    break;
            case 18: String[] st = getArray_as_String();
                    PrintToScreen.println(st);
                    break;
            default: throw new IllegalArgumentException("Data type not identified by this method");
        }
    }

    // Convert array to Double if not Complex, Phasor,  BigDecimal or BigInteger
    // Convert to BigDecimal if BigInteger
    // Convert Phasor to Complex
    public void convertToHighest(){
        switch(this.type){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 16:
            case 17:
            case 18: Double[] dd = this.getArray_as_Double();
                     this.array.clear();
                     for(int i=0; i<this.length; i++)this.array.add(dd[i]);
                     this.type = 1;
                     break;
            case 12:
            case 13: BigDecimal[] bd = this.getArray_as_BigDecimal();
                     this.array.clear();
                     for(int i=0; i<this.length; i++)this.array.add(bd[i]);
                     this.type = 12;
                     bd = null;
                     break;
            case 14:
            case 15: Complex[] cc = this.getArray_as_Complex();
                     this.array.clear();
                     for(int i=0; i<this.length; i++)this.array.add(cc[i]);
                     this.type = 14;
                     break;

        }
    }


    // plot the array
    public void plot(int n){
        if(n>2)throw new IllegalArgumentException("Argument n, " + n +", must be less than 3");

        double[] xAxis = new double[this.length];
        for(int i=0; i<this.length; i++)xAxis[i] = i;
        double[] yAxis = this.getArray_as_double();


        PlotGraph pg = new PlotGraph(xAxis, yAxis);
        pg.setGraphTitle("ArrayMaths plot method");
        pg.setXaxisLegend("Array element index");
        pg.setYaxisLegend("Array element value");
        pg.setPoint(1);
        switch(n){
            case 0: pg.setLine(0);
                    pg.setGraphTitle2("Points only - no line");
                    break;
            case 1: pg.setLine(3);
                    pg.setGraphTitle2("Points joined by straight lines");
                    break;
            case 2: pg.setLine(1);
                    pg.setGraphTitle2("Points joined by cubic spline interpolated line");
                    break;
            default: throw new IllegalArgumentException("Should not be possible to get here!!!");
        }
        pg.plot();
    }
}







