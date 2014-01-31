/*
*   Class   Conv
*
*   USAGE:  Methods for:
*       Recasting variable type with exception throwing not present in standard java recasts
*       Conversion of physical entities from one set of units to another
*       Copying of an object
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    April 2008
*   AMENDED: September 2009, 9-20 January 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Conv.html
*
*   Copyright (c) 2011
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

package flanagan.math;

import java.math.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import flanagan.math.Fmath;
import flanagan.math.Polynomial;
import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;
import flanagan.analysis.ErrorProp;
import flanagan.complex.ComplexErrorProp;
import flanagan.circuits.Phasor;


public class Conv{

    private static  int type = -1;      // 0 double, 1 Double, 2 long, 3 Long, 4 float, 5 Float, 6 int, 7 Integer, 8 short, 9 Short, 10 byte, 11 Byte
                                // 12 BigDecimal, 13 BigInteger, 14 Complex, 15 Phasor, 16 char, 17 Character, 18 String
    private static  String[] typeName = {"double", "Double", "long", "Long", "float", "Float", "int", "Integer", "short", "Short", "byte", "Byte", "BigDecimal", "BigInteger", "Complex", "Phasor", "char", "Character", "String"};

    private static  double max_float_as_double  = (double)Float.MAX_VALUE;
    private static  double max_long_as_double   = (double)Long.MAX_VALUE;
    private static  double max_long_as_float    = (float)Long.MAX_VALUE;
    private static  double max_int_as_double    = (double)Integer.MAX_VALUE;
    private static  double max_int_as_float     = (float)Integer.MAX_VALUE;
    private static  double max_int_as_long      = (long)Integer.MAX_VALUE;
    private static  double max_short_as_double  = (double)Short.MAX_VALUE;
    private static  double max_short_as_long    = (long)Short.MAX_VALUE;
    private static  double max_short_as_float   = (float)Short.MAX_VALUE;
    private static  double max_short_as_int     = (int)Short.MAX_VALUE;
    private static  double max_byte_as_double   = (double)Byte.MAX_VALUE;
    private static  double max_byte_as_float    = (float)Byte.MAX_VALUE;
    private static  double max_byte_as_long     = (long)Byte.MAX_VALUE;
    private static  double max_byte_as_int      = (int)Byte.MAX_VALUE;
    private static  double max_byte_as_short    = (short)Byte.MAX_VALUE;

    private static boolean suppressMessage = false;    // if true lack of precision messages are suppressed
    private static boolean suppressMessageAM = false;  // for use with ArrayMaths - allows suppression for all instances of ArrayMaths

    // CONSTRUCTORS
    public Conv(){
    }

    // LOSS OF PRECISION MESSAGE
    // Suppress loss of precision messages
    public static void suppressMessages(){
        Conv.suppressMessage = true;
    }

    // Restore loss of precision messages
    public static void restoreMessages(){
        if(!Conv.suppressMessageAM)Conv.suppressMessage = false;
    }

    //  For use of ArrayMaths - suppression for all ArrayMaths instances
    public static void suppressMessagesAM(){
        Conv.suppressMessageAM = true;
    }

    // For use of ArrayMaths - restore total loss of precision messages
    public static void restoreMessagesAM(){
        Conv.suppressMessageAM = false;
    }

    // RECAST
    // double and Double -> . . .
    public static float convert_double_to_float(double x){
        if(x>max_float_as_double)throw new IllegalArgumentException("double is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_double_to_float: possible loss of precision");
        return (new Double(x)).floatValue();
    }

    public static Float convert_double_to_Float(double x){
        if(x>max_float_as_double)throw new IllegalArgumentException("double is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_double_to_Float: possible loss of precision");
        return new Float((new Double(x)).floatValue());
    }

    public static float convert_Double_to_float(Double xx){
        double x = xx.doubleValue();
        if(x>max_float_as_double)throw new IllegalArgumentException("Double is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_Double_to_float: possible loss of precision");
        return xx.floatValue();
    }

    public static Float convert_Double_to_Float(Double xx){
        double x = xx.doubleValue();
        if(x>max_float_as_double)throw new IllegalArgumentException("Double is too large to be recast as Float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_Double_to_Float: possible loss of precision");
        return new Float(x);
    }

    public static long convert_double_to_long(double x){
        if(x>max_long_as_double)throw new IllegalArgumentException("double is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return (new Double(x)).longValue();
    }

    public static Long convert_double_to_Long(double x){
        if(x>max_long_as_double)throw new IllegalArgumentException("double is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new Long((new Double(x)).longValue());
    }

    public static long convert_Double_to_long(Double xx){
        double x = xx.doubleValue();
        if(x>max_long_as_double)throw new IllegalArgumentException("Double is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return xx.longValue();
    }

    public static Long convert_Double_to_Long(Double xx){
        double x = xx.doubleValue();
        if(x>max_long_as_double)throw new IllegalArgumentException("Double is too large to be recast as Long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return new Long(xx.longValue());
    }

    public static int convert_double_to_int(double x){
        if(x>max_int_as_double)throw new IllegalArgumentException("double is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return (new Double(x)).intValue();
    }

    public static Integer convert_double_to_Integer(double x){
        if(x>max_int_as_double)throw new IllegalArgumentException("double is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new Integer((new Double(x)).intValue());
    }

    public static int convert_Double_to_int(Double xx){
        double x = xx.doubleValue();
        if(x>max_int_as_double)throw new IllegalArgumentException("Double is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return xx.intValue();
    }

    public static Integer convert_Double_to_Integer(Double xx){
        double x = xx.doubleValue();
        if(x>max_int_as_double)throw new IllegalArgumentException("Double is too large to be recast as Integer");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return new Integer(xx.intValue());
    }

    public static short convert_double_to_short(double x){
        if(x>max_short_as_double)throw new IllegalArgumentException("double is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return (new Double(x)).shortValue();
    }

    public static Short convert_double_to_Short(double x){
        if(x>max_short_as_double)throw new IllegalArgumentException("double is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new Short((new Double(x)).shortValue());
    }

    public static short convert_Double_to_short(Double xx){
        double x = xx.doubleValue();
        if(x>max_short_as_double)throw new IllegalArgumentException("Double is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return xx.shortValue();
    }

    public static Short convert_Double_to_Short(Double xx){
        double x = xx.doubleValue();
        if(x>max_short_as_double)throw new IllegalArgumentException("Double is too large to be recast as Short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return new Short(xx.shortValue());
    }

    public static byte convert_double_to_byte(double x){
        if(x>max_byte_as_double)throw new IllegalArgumentException("double is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return (new Double(x)).byteValue();
    }

    public static Byte convert_double_to_Byte(double x){
        if(x>max_byte_as_double)throw new IllegalArgumentException("double is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new Byte((new Double(x)).byteValue());
    }

    public static byte convert_Double_to_byte(Double xx){
        double x = xx.doubleValue();
        if(x>max_byte_as_double)throw new IllegalArgumentException("Double is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return xx.byteValue();
    }

    public static Byte convert_Double_to_Byte(Double xx){
        double x = xx.doubleValue();
        if(x>max_byte_as_double)throw new IllegalArgumentException("Double is too large to be recast as Byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Double is not, arithmetically, an integer");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_double_to_BigDecimal(double x){
        return new BigDecimal(x);
    }

    public static BigDecimal convert_Double_to_BigDecimal(Double xx){
        return new BigDecimal(xx.doubleValue());
    }

    public static BigInteger convert_double_to_BigInteger(double x){
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new BigInteger(Double.toString(x));
    }

    public static BigInteger convert_Double_to_BigInteger(Double xx){
        double x = xx.doubleValue();
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return new BigInteger(Double.toString(x));
    }

    // float and Float -> . . .
    public static double convert_float_to_double(float x){
        return (new Float(x)).doubleValue();
    }

    public static Double convert_float_to_Double(float x){
        return new Double((new Float(x)).doubleValue());
    }

    public static double convert_Float_to_double(Float xx){
        return xx.doubleValue();
    }

    public static Double convert_Float_to_Double(Float xx){
        return new Double(xx.doubleValue());
    }

    public static long convert_float_to_long(float x){
        if(x>max_long_as_float)throw new IllegalArgumentException("float is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return (new Float(x)).longValue();
    }

    public static Long convert_float_to_Long(float x){
        if(x>max_long_as_float)throw new IllegalArgumentException("float is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return new Long((new Float(x)).longValue());
    }

    public static long convert_Float_to_long(Float xx){
        float x = xx.floatValue();
        if(x>max_long_as_float)throw new IllegalArgumentException("Float is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return xx.longValue();
    }

    public static Long convert_Float_to_Long(Float xx){
        float x = xx.floatValue();
        if(x>max_long_as_float)throw new IllegalArgumentException("Float is too large to be recast as Long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return new Long(xx.longValue());
    }

    public static int convert_float_to_int(float x){
        if(x>max_int_as_float)throw new IllegalArgumentException("double is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("double is not, arithmetically, an integer");
        return (new Float(x)).intValue();
    }

    public static Integer convert_float_to_Integer(float x){
        if(x>max_int_as_float)throw new IllegalArgumentException("float is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return new Integer((new Float(x)).intValue());
    }

    public static int convert_Float_to_int(Float xx){
        float x = xx.floatValue();
        if(x>max_int_as_float)throw new IllegalArgumentException("Float is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return xx.intValue();
    }

    public static Integer convert_Float_to_Integer(Float xx){
        float x = xx.floatValue();
        if(x>max_int_as_float)throw new IllegalArgumentException("Float is too large to be recast as Integer");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return new Integer(xx.intValue());
    }

    public static short convert_float_to_short(float x){
        if(x>max_short_as_float)throw new IllegalArgumentException("float is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return (new Float(x)).shortValue();
    }

    public static Short convert_float_to_Short(float x){
        if(x>max_short_as_float)throw new IllegalArgumentException("float is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return new Short((new Float(x)).shortValue());
    }

    public static short convert_Float_to_short(Float xx){
        float x = xx.floatValue();
        if(x>max_short_as_float)throw new IllegalArgumentException("Float is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return xx.shortValue();
    }

    public static Short convert_Float_to_Short(Float xx){
        float x = xx.floatValue();
        if(x>max_short_as_float)throw new IllegalArgumentException("Float is too large to be recast as Short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return new Short(xx.shortValue());
    }

    public static byte convert_float_to_byte(float x){
        if(x>max_byte_as_float)throw new IllegalArgumentException("float is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return (new Float(x)).byteValue();
    }

    public static Byte convert_float_to_Byte(float x){
        if(x>max_byte_as_float)throw new IllegalArgumentException("float is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return new Byte((new Float(x)).byteValue());
    }

    public static byte convert_Float_to_byte(Float xx){
        float x = xx.floatValue();
        if(x>max_byte_as_float)throw new IllegalArgumentException("Float is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return xx.byteValue();
    }

    public static Byte convert_Float_to_Byte(Float xx){
        float x = xx.floatValue();
        if(x>max_byte_as_float)throw new IllegalArgumentException("Float is too large to be recast as Byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_float_to_BigDecimal(float x){
        return new BigDecimal((double)x);
    }

    public static BigDecimal convert_Float_to_BigDecimal(Float xx){
        return new BigDecimal(xx.doubleValue());
    }

    public static BigInteger convert_double_to_BigInteger(float x){
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("float is not, arithmetically, an integer");
        return new BigInteger(Float.toString(x));
    }

    public static BigInteger convert_Float_to_BigInteger(Float xx){
        double x = xx.doubleValue();
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("Float is not, arithmetically, an integer");
        return new BigInteger(Double.toString(x));
    }


    // long and Long -> . . .
    public static double convert_long_to_double(long x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_long_to_double: possible loss of precision");
        return (new Long(x)).doubleValue();
    }

    public static Double convert_long_to_Double(long x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_long_to_Double: possible loss of precision");
        return new Double((new Long(x)).doubleValue());
    }

    public static double convert_Long_to_double(Long xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Long_to_double: possible loss of precision");
        return xx.doubleValue();
    }

    public static Double convert_Long_to_Double(Long xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Long_to_Double: possible loss of precision");
        return new Double(xx.doubleValue());
    }

    public static float convert_long_to_float(long x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_long_to_float: possible loss of precision");
        return (new Long(x)).floatValue();
    }

    public static Float convert_long_to_Float(long x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_long_to_Float: possible loss of precision");
        return new Float((new Long(x)).floatValue());
    }

    public static float convert_Long_to_float(Long xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Long_to_float: possible loss of precision");
        return xx.floatValue();
    }

    public static Float convert_Long_to_Float(Long xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Long_to_Float: possible loss of precision");
        return new Float(xx.floatValue());
    }

    public static int convert_long_to_int(long x){
        if(x>max_int_as_long)throw new IllegalArgumentException("long is too large to be recast as int");
        return (new Float(x)).intValue();
    }

    public static Integer convert_long_to_Integer(long x){
        if(x>max_int_as_long)throw new IllegalArgumentException("long is too large to be recast as Integer");
        return new Integer((new Long(x)).intValue());
    }

    public static int convert_Long_to_int(Long xx){
        long x = xx.longValue();
        if(x>max_int_as_long)throw new IllegalArgumentException("Long is too large to be recast as int");
        return xx.intValue();
    }

    public static Integer convert_Long_to_Integer(Long xx){
        long x = xx.longValue();
        if(x>max_int_as_long)throw new IllegalArgumentException("Long is too large to be recast as Integer");
        return new Integer(xx.intValue());
    }

    public static short convert_long_to_short(long x){
        if(x>max_short_as_long)throw new IllegalArgumentException("long is too large to be recast as short");
        return (new Long(x)).shortValue();
    }

    public static Short convert_long_to_Short(long x){
        if(x>max_short_as_long)throw new IllegalArgumentException("long is too large to be recast as Short");
        return new Short((new Long(x)).shortValue());
    }

    public static short convert_Long_to_short(Long xx){
        long x = xx.longValue();
        if(x>max_short_as_long)throw new IllegalArgumentException("Long is too large to be recast as short");
        return xx.shortValue();
    }

    public static Short convert_Long_to_Short(Long xx){
        long x = xx.longValue();
        if(x>max_short_as_long)throw new IllegalArgumentException("Long is too large to be recast as Short");
        return new Short(xx.shortValue());
    }

    public static byte convert_long_to_byte(long x){
        if(x>max_byte_as_long)throw new IllegalArgumentException("long is too large to be recast as byte");
        return (new Long(x)).byteValue();
    }

    public static Byte convert_long_to_Byte(long x){
        if(x>max_byte_as_long)throw new IllegalArgumentException("long is too large to be recast as Byte");
        return new Byte((new Long(x)).byteValue());
    }

    public static byte convert_Long_to_byte(Long xx){
        long x = xx.longValue();
        if(x>max_byte_as_long)throw new IllegalArgumentException("Long is too large to be recast as byte");
        return xx.byteValue();
    }

    public static Byte convert_Long_to_Byte(Long xx){
        long x = xx.longValue();
        if(x>max_byte_as_long)throw new IllegalArgumentException("Long is too large to be recast as Byte");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_long_to_BigDecimal(long x){
        return new BigDecimal((new Long(x)).toString());
    }

    public static BigDecimal convert_Long_to_BigDecimal(Long xx){
        return new BigDecimal(xx.toString());
    }

    public static BigInteger convert_long_to_BigInteger(long x){
        return new BigInteger(Long.toString(x));
    }

    public static BigInteger convert_Long_to_BigInteger(Long xx){
        double x = xx.doubleValue();
        return new BigInteger(xx.toString());
    }

    // int and Integer -> . . .
    public static double convert_int_to_double(int x){
        return (new Integer(x)).doubleValue();
    }

    public static Double convert_int_to_Double(int x){
        return new Double((new Integer(x)).doubleValue());
    }

    public static double convert_Integer_to_double(Integer xx){
        return xx.doubleValue();
    }

    public static Double convert_Integer_to_Double(Integer xx){
        return new Double(xx.doubleValue());
    }

    public static float convert_int_to_float(int x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_int_to_float: possible loss of precision");
        return (new Integer(x)).floatValue();
    }

    public static Float convert_int_to_Float(int x){
        if(!suppressMessage)System.out.println("Class Conv: method convert_int_to_Float: possible loss of precision");
        return new Float((new Integer(x)).floatValue());
    }

    public static float convert_Integer_to_float(Integer xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Integer_to_float: possible loss of precision");
        return xx.floatValue();
    }

    public static Float convert_Integer_to_Float(Integer xx){
        if(!suppressMessage)System.out.println("Class Conv: method convert_Integer_to_Float: possible loss of precision");
        return new Float(xx.floatValue());
    }

    public static long convert_int_to_long(int x){
        return (new Integer(x)).longValue();
    }

    public static Long convert_int_to_Long(int x){
        return new Long((new Integer(x)).longValue());
    }

    public static long convert_Integer_to_long(Integer xx){
        return xx.longValue();
    }

    public static Long convert_Integer_to_Long(Integer xx){
        return new Long(xx.longValue());
    }

    public static short convert_int_to_short(int x){
        if(x>max_short_as_int)throw new IllegalArgumentException("int is too large to be recast as short");
        return (new Integer(x)).shortValue();
    }

    public static Short convert_int_to_Short(int x){
        if(x>max_short_as_int)throw new IllegalArgumentException("int is too large to be recast as Short");
        return new Short((new Integer(x)).shortValue());
    }

    public static short convert_Integer_to_short(Integer xx){
        int x = xx.intValue();
        if(x>max_short_as_int)throw new IllegalArgumentException("Integer is too large to be recast as short");
        return xx.shortValue();
    }

    public static Short convert_Integer_to_Short(Integer xx){
        int x = xx.intValue();
        if(x>max_short_as_int)throw new IllegalArgumentException("Integer is too large to be recast as Short");
        return new Short(xx.shortValue());
    }

    public static byte convert_int_to_byte(int x){
        if(x>max_byte_as_int)throw new IllegalArgumentException("int is too large to be recast as byte");
        return (new Integer(x)).byteValue();
    }

    public static Byte convert_int_to_Byte(int x){
        if(x>max_byte_as_int)throw new IllegalArgumentException("int is too large to be recast as Byte");
        return new Byte((new Integer(x)).byteValue());
    }

    public static byte convert_Integer_to_byte(Integer xx){
        int x = xx.intValue();
        if(x>max_byte_as_int)throw new IllegalArgumentException("Integer is too large to be recast as byte");
        return xx.byteValue();
    }

    public static Byte convert_Integer_to_Byte(Integer xx){
        int x = xx.intValue();
        if(x>max_byte_as_int)throw new IllegalArgumentException("Integer is too large to be recast as Byte");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_int_to_BigDecimal(int x){
        return new BigDecimal((new Integer(x)).toString());
    }

    public static BigDecimal convert_Integer_to_BigDecimal(Integer xx){
        return new BigDecimal(xx.toString());
    }

    public static BigInteger convert_int_to_BigInteger(int x){
        return new BigInteger(Long.toString(x));
    }

    public static BigInteger convert_Integer_to_BigInteger(Integer xx){
        return new BigInteger(xx.toString());
    }

    // short and Short -> . . .
    public static double convert_short_to_double(short x){
        return (new Short(x)).doubleValue();
    }

    public static Double convert_short_to_Double(short x){
        return new Double((new Short(x)).doubleValue());
    }

    public static double convert_Short_to_double(Short xx){
        return xx.doubleValue();
    }

    public static Double convert_Short_to_Double(Short xx){
        return new Double(xx.doubleValue());
    }

    public static float convert_short_to_float(short x){
        return (new Short(x)).floatValue();
    }

    public static Float convert_short_to_Float(short x){
        return new Float((new Short(x)).floatValue());
    }

    public static float convert_Short_to_float(Short xx){
        return xx.floatValue();
    }

    public static Float convert_Short_to_Float(Short xx){
        return new Float(xx.floatValue());
    }

    public static long convert_short_to_long(short x){
        return (new Short(x)).longValue();
    }

    public static Long convert_short_to_Long(short x){
        return new Long((new Short(x)).longValue());
    }

    public static long convert_Short_to_long(Short xx){
        return xx.longValue();
    }

    public static Long convert_Short_to_Long(Short xx){
        return new Long(xx.longValue());
    }

    public static int convert_short_to_int(short x){
        return (new Short(x)).intValue();
    }

    public static Integer convert_short_to_Integer(short x){
        return new Integer((new Short(x)).intValue());
    }

    public static int convert_Short_to_int(Short xx){
        return xx.intValue();
    }

    public static Integer convert_Short_to_Integer(Short xx){
        return new Integer(xx.intValue());
    }

    public static byte convert_short_to_byte(short x){
        if(x>max_byte_as_short)throw new IllegalArgumentException("short is too large to be recast as byte");
        return (new Short(x)).byteValue();
    }

    public static Byte convert_short_to_Byte(short x){
        if(x>max_byte_as_short)throw new IllegalArgumentException("short is too large to be recast as Byte");
        return new Byte((new Short(x)).byteValue());
    }

    public static byte convert_Short_to_byte(Short xx){
        int x = xx.shortValue();
        if(x>max_byte_as_short)throw new IllegalArgumentException("Short is too large to be recast as byte");
        return xx.byteValue();
    }

    public static Byte convert_Short_to_Byte(Short xx){
        int x = xx.shortValue();
        if(x>max_byte_as_short)throw new IllegalArgumentException("Short is too large to be recast as Byte");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_short_to_BigDecimal(short x){
        return new BigDecimal((new Short(x)).toString());
    }

    public static BigDecimal convert_Short_to_BigDecimal(Short xx){
        return new BigDecimal(xx.toString());
    }

    public static BigInteger convert_short_to_BigInteger(short x){
        return new BigInteger(Short.toString(x));
    }

    public static BigInteger convert_Short_to_BigInteger(Short xx){
        return new BigInteger(xx.toString());
    }

    // byte and Byte -> . . .
    public static double convert_byte_to_double(byte x){
        return (new Byte(x)).doubleValue();
    }

    public static Double convert_byte_to_Double(byte x){
        return new Double((new Byte(x)).doubleValue());
    }

    public static double convert_Byte_to_double(Byte xx){
        return xx.doubleValue();
    }

    public static Double convert_Byte_to_Double(Byte xx){
        return new Double(xx.doubleValue());
    }

    public static float convert_byte_to_float(byte x){
        return (new Byte(x)).floatValue();
    }

    public static Float convert_byte_to_Float(byte x){
        return new Float((new Byte(x)).floatValue());
    }

    public static float convert_Byte_to_float(Byte xx){
        return xx.floatValue();
    }

    public static Float convert_Byte_to_Float(Byte xx){
        return new Float(xx.floatValue());
    }

    public static long convert_byte_to_long(byte x){
        return (new Byte(x)).longValue();
    }

    public static Long convert_byte_to_Long(byte x){
        return new Long((new Byte(x)).longValue());
    }

    public static long convert_Byte_to_long(Byte xx){
        return xx.longValue();
    }

    public static Long convert_Byte_to_Long(Byte xx){
        return new Long(xx.longValue());
    }

    public static int convert_byte_to_int(byte x){
        return (new Byte(x)).intValue();
    }

    public static Integer convert_byte_to_Integer(byte x){
        return new Integer((new Byte(x)).intValue());
    }

    public static int convert_Byte_to_int(Byte xx){
        return xx.intValue();
    }

    public static Integer convert_Byte_to_Integer(Byte xx){
        return new Integer(xx.intValue());
    }

    public static short convert_byte_to_short(byte x){
        return (new Byte(x)).shortValue();
    }

    public static Short convert_byte_to_Short(byte x){
        return new Short((new Byte(x)).shortValue());
    }

    public static short convert_Byte_to_short(Byte xx){
        return xx.shortValue();
    }

    public static Short convert_Byte_to_Short(Byte xx){
        return new Short(xx.shortValue());
    }

    public static BigDecimal convert_byte_to_BigDecimal(byte x){
        return new BigDecimal((new Byte(x)).toString());
    }

    public static BigDecimal convert_Byte_to_BigDecimal(Byte xx){
        return new BigDecimal(xx.toString());
    }

    public static BigInteger convert_byte_to_BigInteger(byte x){
        return new BigInteger(Byte.toString(x));
    }

    public static BigInteger convert_Byte_to_BigInteger(Byte xx){
        return new BigInteger(xx.toString());
    }


    // BigDecimal -> . . .
    public static double convert_BigDecimal_to_double(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as double");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigDecimal_to_double: possible loss of precision");
        return x;
    }

    public static Double convert_BigDecimal_to_Double(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as double");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigDecimal_to_double: possible loss of precision");
        return new Double(x);
    }

    public static float convert_BigDecimal_to_float(BigDecimal xx){
        float x = xx.floatValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigDecimal_to_float: possible loss of precision");
        return x;
    }

    public static Float convert_BigDecimal_to_Float(BigDecimal xx){
        float x = xx.floatValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigDecimal_to_float: possible loss of precision");
        return new Float(x);
    }

    public static long convert_BigDecimal_to_long(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as long");
        if(x>max_long_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return xx.longValue();
    }

    public static Long convert_BigDecimal_to_Long(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as Long");
        if(x>max_long_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as Long");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return new Long(xx.longValue());
    }

    public static int convert_BigDecimal_to_int(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as int");
        if(x>max_int_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as int");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return xx.intValue();
    }

    public static Integer convert_BigDecimal_to_Integer(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as Integer");
        if(x>max_int_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as Integer");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return new Integer(xx.intValue());
    }

    public static short convert_BigDecimal_to_short(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as short");
        if(x>max_short_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return xx.shortValue();
    }

    public static Short convert_BigDecimal_to_Short(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as Short");
        if(x>max_short_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as Short");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return new Short(xx.shortValue());
    }

    public static byte convert_BigDecimal_to_byte(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as byte");
        if(x>max_byte_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return xx.byteValue();
    }

    public static Byte convert_BigDecimal_to_Byte(BigDecimal xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigDecimal is too large to be recast as Byte");
        if(x>max_byte_as_double)throw new IllegalArgumentException("BigDecimal is too large to be recast as Byte");
        if(!Fmath.isInteger(x))throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
        return new Byte(xx.byteValue());
    }

    public static BigInteger convert_BigDecimal_to_BigInteger(BigDecimal xx){
        String ss = xx.toString();
        int posDot = ss.indexOf('.');
        int posExp = ss.indexOf('E');
        String tt = null;

        if(posDot==-1){
            return xx.toBigInteger();
        }
        else{
            if(posExp==-1){
                tt = ss.substring(posDot+1);
            }
            else{
                tt = ss.substring(posDot+1, posExp);
            }
            int n = tt.length();
            boolean test1 = true;
            boolean test2 = true;
            int ii=0;
            while(test1){
                if(tt.charAt(ii)!='0'){
                    test1 = false;
                    test2 = false;
                }
                else{
                    ii++;
                    if(ii==n)test1 = false;
                }
            }
            if(test2){
                return xx.toBigInteger();
            }
            else{
                throw new IllegalArgumentException("BigDecimal is not, arithmetically, an integer");
            }
        }
    }



    // BigInteger -> . . .
    public static double convert_BigInteger_to_double(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as double");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigInteger_to_double: possible loss of precision");
        return x;
    }

    public static Double convert_BigInteger_to_Double(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as double");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigInteger_to_double: possible loss of precision");
        return new Double(x);
    }

    public static float convert_BigInteger_to_float(BigInteger xx){
        float x = xx.floatValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigInteger_to_float: possible loss of precision");
        return x;
    }

    public static Float convert_BigInteger_to_Float(BigInteger xx){
        float x = xx.floatValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as float");
        if(!suppressMessage)System.out.println("Class Conv: method convert_BigInteger_to_float: possible loss of precision");
        return new Float(x);
    }

    public static long convert_BigInteger_to_long(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as long");
        if(x>max_long_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as long");
        return xx.longValue();
    }

    public static Long convert_BigInteger_to_Long(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as Long");
        if(x>max_long_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as Long");
        return new Long(xx.longValue());
    }

    public static int convert_BigInteger_to_int(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as int");
        if(x>max_int_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as int");
        return xx.intValue();
    }

    public static Integer convert_BigInteger_to_Integer(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as Integer");
        if(x>max_int_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as Integer");
        return new Integer(xx.intValue());
    }

    public static short convert_BigInteger_to_short(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as short");
        if(x>max_short_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as short");
        return xx.shortValue();
    }

    public static Short convert_BigInteger_to_Short(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as Short");
        if(x>max_short_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as Short");
        return new Short(xx.shortValue());
    }

    public static byte convert_BigInteger_to_byte(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as byte");
        if(x>max_byte_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as byte");
        return xx.byteValue();
    }

    public static Byte convert_BigInteger_to_Byte(BigInteger xx){
        double x = xx.doubleValue();
        if(Fmath.isInfinity(x))throw new IllegalArgumentException("BigInteger is too large to be recast as Byte");
        if(x>max_byte_as_double)throw new IllegalArgumentException("BigInteger is too large to be recast as Byte");
        return new Byte(xx.byteValue());
    }

    public static BigDecimal convert_BigInteger_to_BigDecimal(BigInteger xx){
        return new BigDecimal(xx);
    }

    // Complex -> Phasor
    public static Phasor convert_Complex_to_Phasor(Complex xx){
        double mag = xx.abs();
        double phase = xx.argDeg();
        return new Phasor(mag, phase);
    }

    // Phasor -> Complex
    public static Complex convert_Phasor_to_Complex(Phasor xx){
        return xx.toComplex();
    }

    // COPY

        // COPY A ONE DIMENSIONAL ARRAY OF double
        public static double[] copy(double[] array){
            if(array==null)return null;
            int n = array.length;
            double[] copy = new double[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF float
        public static float[] copy(float[] array){
            if(array==null)return null;
            int n = array.length;
            float[] copy = new float[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF int
        public static int[] copy(int[] array){
            if(array==null)return null;
            int n = array.length;
            int[] copy = new int[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF long
        public static long[] copy(long[] array){
            if(array==null)return null;
            int n = array.length;
            long[] copy = new long[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF double
        public static double[][] copy(double[][] array){
            if(array==null)return null;
            int n = array.length;
            double[][] copy = new double[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF float
        public static float[][] copy(float[][] array){
            if(array==null)return null;
            int n = array.length;
            float[][] copy = new float[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF int
        public static int[][] copy(int[][] array){
            if(array==null)return null;
            int n = array.length;
            int[][] copy = new int[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF long
        public static long[][] copy(long[][] array){
            if(array==null)return null;
            int n = array.length;
            long[][] copy = new long[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF double
        public static double[][][] copy(double[][][] array){
            if(array==null)return null;
            int n = array.length;
            double[][][] copy = new double[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new double[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }


        // COPY A THREE DIMENSIONAL ARRAY OF float
        public static float[][][] copy(float[][][] array){
            if(array==null)return null;
            int n = array.length;
            float[][][] copy = new float[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new float[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF int
        public static int[][][] copy(int[][][] array){
            if(array==null)return null;
            int n = array.length;
            int[][][] copy = new int[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new int[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF long
        public static long[][][] copy(long[][][] array){
            if(array==null)return null;
            int n = array.length;
            long[][][] copy = new long[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new long[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF double
        public static double[][][][] copy(double[][][][] array){
            if(array==null)return null;
            int n = array.length;
            double[][][][] copy = new double[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new double[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new double[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new double[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF float
        public static float[][][][] copy(float[][][][] array){
            if(array==null)return null;
            int n = array.length;
            float[][][][] copy = new float[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new float[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new float[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new float[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF int
        public static int[][][][] copy(int[][][][] array){
            if(array==null)return null;
            int n = array.length;
            int[][][][] copy = new int[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new int[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new int[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new int[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF long
        public static long[][][][] copy(long[][][][] array){
            if(array==null)return null;
            int n = array.length;
            long[][][][] copy = new long[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new long[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new long[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new long[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF String
        public static String[] copy(String[] array){
            if(array==null)return null;
            int n = array.length;
            String[] copy = new String[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF String
        public static String[][] copy(String[][] array){
            if(array==null)return null;
            int n = array.length;
            String[][] copy = new String[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF String
        public static String[][][] copy(String[][][] array){
            if(array==null)return null;
            int n = array.length;
            String[][][] copy = new String[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new String[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF String
        public static String[][][][] copy(String[][][][] array){
            if(array==null)return null;
            int n = array.length;
            String[][][][] copy = new String[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new String[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new String[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new String[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }




        // COPY A ONE DIMENSIONAL ARRAY OF boolean
        public static boolean[] copy(boolean[] array){
            if(array==null)return null;
            int n = array.length;
            boolean[] copy = new boolean[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF boolean
        public static boolean[][] copy(boolean[][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][] copy = new boolean[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF boolean
        public static boolean[][][] copy(boolean[][][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][][] copy = new boolean[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new boolean[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF boolean
        public static boolean[][][][] copy(boolean[][][][] array){
            if(array==null)return null;
            int n = array.length;
            boolean[][][][] copy = new boolean[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new boolean[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new boolean[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new boolean[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }



        // COPY A ONE DIMENSIONAL ARRAY OF char
        public static char[] copy(char[] array){
            if(array==null)return null;
            int n = array.length;
            char[] copy = new char[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF char
        public static char[][] copy(char[][] array){
            if(array==null)return null;
            int n = array.length;
            char[][] copy = new char[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF char
        public static char[][][] copy(char[][][] array){
            if(array==null)return null;
            int n = array.length;
            char[][][] copy = new char[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new char[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF char
        public static char[][][][] copy(char[][][][] array){
            if(array==null)return null;
            int n = array.length;
            char[][][][] copy = new char[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new char[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new char[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new char[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Complex
        public static Complex[] copy(Complex[] array){
            if(array==null)return null;
            int n = array.length;
            Complex[] copy = new Complex[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Complex
        public static Complex[][] copy(Complex[][] array){
            if(array==null)return null;
            int n = array.length;
            Complex[][] copy = new Complex[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Complex[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Complex
        public static Complex[][][] copy(Complex[][][] array){
            if(array==null)return null;
            int n = array.length;
            Complex[][][] copy = new Complex[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Complex[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Complex[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Complex
        public static Complex[][][][] copy(Complex[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Complex[][][][] copy = new Complex[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Complex[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Complex[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Complex[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }


        // COPY A ONE DIMENSIONAL ARRAY OF ComplexPoly
        public static ComplexPoly[] copy(ComplexPoly[] array){
            if(array==null)return null;
            int n = array.length;
            ComplexPoly[] copy = new ComplexPoly[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF ComplexPoly
        public static ComplexPoly[][] copy(ComplexPoly[][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexPoly[][] copy = new ComplexPoly[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexPoly[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF ComplexPoly
        public static ComplexPoly[][][] copy(ComplexPoly[][][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexPoly[][][] copy = new ComplexPoly[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexPoly[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ComplexPoly[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF ComplexPoly
        public static ComplexPoly[][][][] copy(ComplexPoly[][][][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexPoly[][][][] copy = new ComplexPoly[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexPoly[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ComplexPoly[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new ComplexPoly[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Polynomial
        public static Polynomial[] copy(Polynomial[] array){
            if(array==null)return null;
            int n = array.length;
            Polynomial[] copy = new Polynomial[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Polynomial
        public static Polynomial[][] copy(Polynomial[][] array){
            if(array==null)return null;
            int n = array.length;
            Polynomial[][] copy = new Polynomial[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Polynomial[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Polynomial
        public static Polynomial[][][] copy(Polynomial[][][] array){
            if(array==null)return null;
            int n = array.length;
            Polynomial[][][] copy = new Polynomial[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Polynomial[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Polynomial[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Polynomial
        public static Polynomial[][][][] copy(Polynomial[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Polynomial[][][][] copy = new Polynomial[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Polynomial[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Polynomial[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Polynomial[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF BigDecimal
        public static BigDecimal[] copy(BigDecimal[] array){
            if(array==null)return null;
            int n = array.length;
            BigDecimal[] copy = new BigDecimal[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF BigDecimal
        public static BigDecimal[][] copy(BigDecimal[][] array){
            if(array==null)return null;
            int n = array.length;
            BigDecimal[][] copy = new BigDecimal[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigDecimal[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF BigDecimal
        public static BigDecimal[][][] copy(BigDecimal[][][] array){
            if(array==null)return null;
            int n = array.length;
            BigDecimal[][][] copy = new BigDecimal[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigDecimal[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new BigDecimal[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF BigDecimal
        public static BigDecimal[][][][] copy(BigDecimal[][][][] array){
            if(array==null)return null;
            int n = array.length;
            BigDecimal[][][][] copy = new BigDecimal[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigDecimal[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new BigDecimal[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new BigDecimal[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }




        // COPY A ONE DIMENSIONAL ARRAY OF BigInteger
        public static BigInteger[] copy(BigInteger[] array){
            if(array==null)return null;
            int n = array.length;
            BigInteger[] copy = new BigInteger[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF BigInteger
        public static BigInteger[][] copy(BigInteger[][] array){
            if(array==null)return null;
            int n = array.length;
            BigInteger[][] copy = new BigInteger[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigInteger[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF BigInteger
        public static BigInteger[][][] copy(BigInteger[][][] array){
            if(array==null)return null;
            int n = array.length;
            BigInteger[][][] copy = new BigInteger[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigInteger[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new BigInteger[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF BigInteger
        public static BigInteger[][][][] copy(BigInteger[][][][] array){
            if(array==null)return null;
            int n = array.length;
            BigInteger[][][][] copy = new BigInteger[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new BigInteger[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new BigInteger[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new BigInteger[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF ErrorProp
        public static ErrorProp[] copy(ErrorProp[] array){
            if(array==null)return null;
            int n = array.length;
            ErrorProp[] copy = new ErrorProp[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF ErrorProp
        public static ErrorProp[][] copy(ErrorProp[][] array){
            if(array==null)return null;
            int n = array.length;
            ErrorProp[][] copy = new ErrorProp[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ErrorProp[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF ErrorProp
        public static ErrorProp[][][] copy(ErrorProp[][][] array){
            if(array==null)return null;
            int n = array.length;
            ErrorProp[][][] copy = new ErrorProp[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ErrorProp[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ErrorProp[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF ErrorProp
        public static ErrorProp[][][][] copy(ErrorProp[][][][] array){
            if(array==null)return null;
            int n = array.length;
            ErrorProp[][][][] copy = new ErrorProp[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ErrorProp[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ErrorProp[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new ErrorProp[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }


        // COPY A ONE DIMENSIONAL ARRAY OF ComplexErrorProp
        public static ComplexErrorProp[] copy(ComplexErrorProp[] array){
            if(array==null)return null;
            int n = array.length;
            ComplexErrorProp[] copy = new ComplexErrorProp[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF ComplexErrorProp
        public static ComplexErrorProp[][] copy(ComplexErrorProp[][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexErrorProp[][] copy = new ComplexErrorProp[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexErrorProp[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF ComplexErrorProp
        public static ComplexErrorProp[][][] copy(ComplexErrorProp[][][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexErrorProp[][][] copy = new ComplexErrorProp[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexErrorProp[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ComplexErrorProp[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF ComplexErrorProp
        public static ComplexErrorProp[][][][] copy(ComplexErrorProp[][][][] array){
            if(array==null)return null;
            int n = array.length;
            ComplexErrorProp[][][][] copy = new ComplexErrorProp[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new ComplexErrorProp[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new ComplexErrorProp[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new ComplexErrorProp[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }



        // COPY A ONE DIMENSIONAL ARRAY OF Phasor
        public static Phasor[] copy(Phasor[] array){
            if(array==null)return null;
            int n = array.length;
            Phasor[] copy = new Phasor[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i].copy();
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Phasor
        public static Phasor[][] copy(Phasor[][] array){
            if(array==null)return null;
            int n = array.length;
            Phasor[][] copy = new Phasor[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Phasor[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j].copy();
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Phasor
        public static Phasor[][][] copy(Phasor[][][] array){
            if(array==null)return null;
            int n = array.length;
            Phasor[][][] copy = new Phasor[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Phasor[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Phasor[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k].copy();
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Phasor
        public static Phasor[][][][] copy(Phasor[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Phasor[][][][] copy = new Phasor[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Phasor[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Phasor[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Phasor[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk].copy();
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF short
        public static short[] copy(short[] array){
            if(array==null)return null;
            int n = array.length;
            short[] copy = new short[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF short
        public static short[][] copy(short[][] array){
            if(array==null)return null;
            int n = array.length;
            short[][] copy = new short[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new short[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF short
        public static short[][][] copy(short[][][] array){
            if(array==null)return null;
            int n = array.length;
            short[][][] copy = new short[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new short[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new short[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF short
        public static short[][][][] copy(short[][][][] array){
            if(array==null)return null;
            int n = array.length;
            short[][][][] copy = new short[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new short[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new short[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new short[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }




        // COPY A ONE DIMENSIONAL ARRAY OF byte
        public static byte[] copy(byte[] array){
            if(array==null)return null;
            int n = array.length;
            byte[] copy = new byte[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF byte
        public static byte[][] copy(byte[][] array){
            if(array==null)return null;
            int n = array.length;
            byte[][] copy = new byte[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new byte[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF byte
        public static byte[][][] copy(byte[][][] array){
            if(array==null)return null;
            int n = array.length;
            byte[][][] copy = new byte[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new byte[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new byte[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF byte
        public static byte[][][][] copy(byte[][][][] array){
            if(array==null)return null;
            int n = array.length;
            byte[][][][] copy = new byte[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new byte[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new byte[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new byte[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }


        // COPY A ONE DIMENSIONAL ARRAY OF Double
        public static Double[] copy(Double[] array){
            if(array==null)return null;
            int n = array.length;
            Double[] copy = new Double[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Double
        public static Double[][] copy(Double[][] array){
            if(array==null)return null;
            int n = array.length;
            Double[][] copy = new Double[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Double[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Double
        public static Double[][][] copy(Double[][][] array){
            if(array==null)return null;
            int n = array.length;
            Double[][][] copy = new Double[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Double[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Double[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Double
        public static Double[][][][] copy(Double[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Double[][][][] copy = new Double[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Double[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Double[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Double[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Float
        public static Float[] copy(Float[] array){
            if(array==null)return null;
            int n = array.length;
            Float[] copy = new Float[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Float
        public static Float[][] copy(Float[][] array){
            if(array==null)return null;
            int n = array.length;
            Float[][] copy = new Float[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Float[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Float
        public static Float[][][] copy(Float[][][] array){
            if(array==null)return null;
            int n = array.length;
            Float[][][] copy = new Float[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Float[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Float[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Float
        public static Float[][][][] copy(Float[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Float[][][][] copy = new Float[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Float[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Float[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Float[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Long
        public static Long[] copy(Long[] array){
            if(array==null)return null;
            int n = array.length;
            Long[] copy = new Long[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Long
        public static Long[][] copy(Long[][] array){
            if(array==null)return null;
            int n = array.length;
            Long[][] copy = new Long[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Long[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Long
        public static Long[][][] copy(Long[][][] array){
            if(array==null)return null;
            int n = array.length;
            Long[][][] copy = new Long[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Long[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Long[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Long
        public static Long[][][][] copy(Long[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Long[][][][] copy = new Long[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Long[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Long[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Long[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Integer
        public static Integer[] copy(Integer[] array){
            if(array==null)return null;
            int n = array.length;
            Integer[] copy = new Integer[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Integer
        public static Integer[][] copy(Integer[][] array){
            if(array==null)return null;
            int n = array.length;
            Integer[][] copy = new Integer[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Integer[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Integer
        public static Integer[][][] copy(Integer[][][] array){
            if(array==null)return null;
            int n = array.length;
            Integer[][][] copy = new Integer[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Integer[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Integer[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Integer
        public static Integer[][][][] copy(Integer[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Integer[][][][] copy = new Integer[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Integer[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Integer[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Integer[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }


        // COPY A ONE DIMENSIONAL ARRAY OF Short
        public static Short[] copy(Short[] array){
            if(array==null)return null;
            int n = array.length;
            Short[] copy = new Short[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Short
        public static Short[][] copy(Short[][] array){
            if(array==null)return null;
            int n = array.length;
            Short[][] copy = new Short[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Short[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Short
        public static Short[][][] copy(Short[][][] array){
            if(array==null)return null;
            int n = array.length;
            Short[][][] copy = new Short[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Short[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Short[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Short
        public static Short[][][][] copy(Short[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Short[][][][] copy = new Short[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Short[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Short[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Short[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }

        // COPY A ONE DIMENSIONAL ARRAY OF Byte
        public static Byte[] copy(Byte[] array){
            if(array==null)return null;
            int n = array.length;
            Byte[] copy = new Byte[n];
            for(int i=0; i<n; i++){
                copy[i] = array[i];
            }
            return copy;
        }

        // COPY A TWO DIMENSIONAL ARRAY OF Byte
        public static Byte[][] copy(Byte[][] array){
            if(array==null)return null;
            int n = array.length;
            Byte[][] copy = new Byte[n][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Byte[m];
                for(int j=0; j<m; j++){
                    copy[i][j] = array[i][j];
                }
            }
            return copy;
        }

        // COPY A THREE DIMENSIONAL ARRAY OF Byte
        public static Byte[][][] copy(Byte[][][] array){
            if(array==null)return null;
            int n = array.length;
            Byte[][][] copy = new Byte[n][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Byte[m][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Byte[l];
                    for(int k=0; k<l;k++)copy[i][j][k] = array[i][j][k];
                }
            }
            return copy;
        }

        // COPY A FOUR DIMENSIONAL ARRAY OF Byte
        public static Byte[][][][] copy(Byte[][][][] array){
            if(array==null)return null;
            int n = array.length;
            Byte[][][][] copy = new Byte[n][][][];
            for(int i=0; i<n; i++){
                int m = array[i].length;
                copy[i] = new Byte[m][][];
                for(int j=0; j<m; j++){
                    int l = array[i][j].length;
                    copy[i][j] = new Byte[l][];
                    for(int k=0; k<l;k++){
                        int ll = array[i][j][k].length;
                        copy[i][j][k] = new Byte[ll];
                        for(int kk=0; kk<ll;kk++){
                            copy[i][j][k][kk] = array[i][j][k][kk];
                        }
                    }
                }
            }
            return copy;
        }




        // COPY OF AN OBJECT
        // An exception will be thrown if an attempt to copy a non-serialisable object is made.
        // Taken, with minor changes,  from { Java Techniques }
        // http://javatechniques.com/blog/
        public static Object copy(Object obj){
            if(obj==null)return null;
            return Conv.copyObject(obj);
        }

        // COPY OF AN OBJECT
        // An exception will be thrown if an attempt to copy a non-serialisable object is made.
        // Taken, with minor changes,  from { Java Techniques }
        // http://javatechniques.com/blog/
        public static Object copyObject(Object obj){
            if(obj==null)return null;
            Object objCopy = null;
            try {
                // Write the object out to a byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                oos.close();
                // Make an input stream from the byte array and
                // read a copy of the object back in.
                ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
                objCopy = ois.readObject();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            catch(ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            return objCopy;
        }

    // UNIT CONVERSIONS

        // Converts radians to degrees
        public static double radToDeg(double rad){
            return  rad*180.0D/Math.PI;
        }

        // Converts degrees to radians
        public static double degToRad(double deg){
            return  deg*Math.PI/180.0D;
        }

        // Converts frequency (Hz) to radial frequency
        public static double frequencyToRadialFrequency(double frequency){
            return  2.0D*Math.PI*frequency;
        }

        // Converts radial frequency to frequency (Hz)
        public static double radialFrequencyToFrequency(double radial){
            return  radial/(2.0D*Math.PI);
        }

        // Converts electron volts(eV) to corresponding wavelength in nm
        public static double evToNm(double ev){
            return  1e+9*Fmath.C_LIGHT/(-ev*Fmath.Q_ELECTRON/Fmath.H_PLANCK);
        }

        // Converts wavelength in nm to matching energy in eV
        public static double nmToEv(double nm)
        {
            return  Fmath.C_LIGHT/(-nm*1e-9)*Fmath.H_PLANCK/Fmath.Q_ELECTRON;
        }

        // Converts moles per litre to percentage weight by volume
        public static double molarToPercentWeightByVol(double molar, double molWeight){
            return  molar*molWeight/10.0D;
        }

        // Converts percentage weight by volume to moles per litre
        public static double percentWeightByVolToMolar(double perCent, double molWeight){
            return  perCent*10.0D/molWeight;
        }

        // Converts Celsius to Kelvin
        public static double celsiusToKelvin(double cels){
            return  cels-Fmath.T_ABS;
        }

        // Converts Kelvin to Celsius
        public static double kelvinToCelsius(double kelv){
            return  kelv+Fmath.T_ABS;
        }

        // Converts Celsius to Fahrenheit
        public static double celsiusToFahren(double cels){
            return  cels*(9.0/5.0)+32.0;
        }

        // Converts Fahrenheit to Celsius
        public static double fahrenToCelsius(double fahr){
            return  (fahr-32.0)*5.0/9.0;
        }

        // Converts calories to Joules
        public static double calorieToJoule(double cal){
            return  cal*4.1868;
        }

        // Converts Joules to calories
        public static double jouleToCalorie(double joule){
            return  joule*0.23884;
        }

        // Converts grams to ounces
        public static double gramToOunce(double gm){
            return  gm/28.3459;
        }

        // Converts ounces to grams
        public static double ounceToGram(double oz){
            return  oz*28.3459;
        }

        // Converts kilograms to pounds
        public static double kgToPound(double kg){
            return  kg/0.4536;
        }

        // Converts pounds to kilograms
        public static double poundToKg(double pds){
            return  pds*0.4536;
        }

        // Converts kilograms to tons
        public static double kgToTon(double kg){
            return  kg/1016.05;
        }

        // Converts tons to kilograms
        public static double tonToKg(double tons){
            return  tons*1016.05;
        }

        // Converts millimetres to inches
        public static double millimetreToInch(double mm){
            return  mm/25.4;
        }

        // Converts inches to millimetres
        public static double inchToMillimetre(double in){
            return  in*25.4;
        }

        // Converts feet to metres
        public static double footToMetre(double ft){
            return  ft*0.3048;
        }

        // Converts metres to feet
        public static double metreToFoot(double metre){
            return  metre/0.3048;
        }

        // Converts yards to metres
        public static double yardToMetre(double yd){
            return  yd*0.9144;
        }

        // Converts metres to yards
        public static double metreToYard(double metre){
            return  metre/0.9144;
        }

        // Converts miles to kilometres
        public static double mileToKm(double mile){
            return  mile*1.6093;
        }

        // Converts kilometres to miles
        public static double kmToMile(double km){
            return  km/1.6093;
        }

        // Converts UK gallons to litres
        public static double gallonToLitre(double gall){
            return  gall*4.546;
        }

        // Converts litres to UK gallons
        public static double litreToGallon(double litre){
            return  litre/4.546;
        }

        // Converts UK quarts to litres
        public static double quartToLitre(double quart){
            return  quart*1.137;
        }

        // Converts litres to UK quarts
        public static double litreToQuart(double litre){
            return  litre/1.137;
        }

        // Converts UK pints to litres
        public static double pintToLitre(double pint){
            return  pint*0.568;
        }

        // Converts litres to UK pints
        public static double litreToPint(double litre){
            return  litre/0.568;
        }

        // Converts UK gallons per mile to litres per kilometre
        public static double gallonPerMileToLitrePerKm(double gallPmile){
            return  gallPmile*2.825;
        }

        // Converts litres per kilometre to UK gallons per mile
        public static double litrePerKmToGallonPerMile(double litrePkm){
            return  litrePkm/2.825;
        }

        // Converts miles per UK gallons to kilometres per litre
        public static double milePerGallonToKmPerLitre(double milePgall){
            return  milePgall*0.354;
        }

        // Converts kilometres per litre to miles per UK gallons
        public static double kmPerLitreToMilePerGallon(double kmPlitre){
            return  kmPlitre/0.354;
        }

        // Converts UK fluid ounce to American fluid ounce
        public static double fluidOunceUKtoUS(double flOzUK){
            return  flOzUK*0.961;
        }

        // Converts American fluid ounce to UK fluid ounce
        public static double fluidOunceUStoUK(double flOzUS){
            return  flOzUS*1.041;
        }

        // Converts UK pint to American liquid pint
        public static double pintUKtoUS(double pintUK){
            return  pintUK*1.201;
        }

        // Converts American liquid pint to UK pint
        public static double pintUStoUK(double pintUS){
            return  pintUS*0.833;
        }

        // Converts UK quart to American liquid quart
        public static double quartUKtoUS(double quartUK){
            return  quartUK*1.201;
        }

        // Converts American liquid quart to UK quart
        public static double quartUStoUK(double quartUS){
            return  quartUS*0.833;
        }

        // Converts UK gallon to American gallon
        public static double gallonUKtoUS(double gallonUK){
            return  gallonUK*1.201;
        }

        // Converts American gallon to UK gallon
        public static double gallonUStoUK(double gallonUS){
            return  gallonUS*0.833;
        }

        // Converts UK pint to American cup
        public static double pintUKtoCupUS(double pintUK){
            return  pintUK/0.417;
        }

        // Converts American cup to UK pint
        public static double cupUStoPintUK(double cupUS){
            return  cupUS*0.417;
        }

        // Calculates body mass index (BMI) from height (m) and weight (kg)
        public static double calcBMImetric(double height, double weight){
            return  weight/(height*height);
        }

        // Calculates body mass index (BMI) from height (ft) and weight (lbs)
        public static double calcBMIimperial(double height, double weight){
                height = Fmath.footToMetre(height);
                weight = Fmath.poundToKg(weight);
            return  weight/(height*height);
        }

        // Calculates weight (kg) to give a specified BMI for a given height (m)
        public static double calcWeightFromBMImetric(double bmi, double height){
            return bmi*height*height;
        }

        // Calculates weight (lbs) to give a specified BMI for a given height (ft)
        public static double calcWeightFromBMIimperial(double bmi, double height){
            height = Fmath.footToMetre(height);
            double weight = bmi*height*height;
            weight = Fmath.kgToPound(weight);
            return  weight;
        }

        // Returns milliseconds since 0 hours 0 minutes 0 seconds on 1 Jan 1970
        public static long dateToJavaMilliSecondsUK(int year, int month, int day, String dayOfTheWeek, int hour, int min, int sec, int millisec){

            TimeAndDate tad = new TimeAndDate();
            long ms = tad.dateToJavaMilliSecondsUK(year, month, day, dayOfTheWeek, hour, min, sec, millisec);

            return ms;
        }

}
