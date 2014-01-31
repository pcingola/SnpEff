/*
*   Class   PrintToScreen
*
*   USAGE:  Methods for writing one and two dimensional arrays to the sceen
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       13  April 2008  (Most methods taken from existing classes to make a separate print to screen class)
*   AMENDED:    11 August 2008, 14 September 2008, 18 January 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PrintToScreen.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2008 - 2011 Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
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

package flanagan.io;

import java.math.*;

import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.math.ArrayMaths;
import flanagan.complex.Complex;
import flanagan.circuits.Phasor;

public class PrintToScreen{

        // 1D ARRAYS

        // print an array of doubles to screen
        // No line returns except at the end
        public static void print(double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of doubles to screen with truncation
        // No line returns except at the end
        public static void print(double[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.print(Fmath.truncate(aa[i], trunc)+"   ");
            }
            System.out.println();
        }

        // print an array of doubles to screen
        // with line returns
        public static void println(double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of doubles to screen with truncation
        // with line returns
        public static void println(double[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.println(Fmath.truncate(aa[i], trunc)+"   ");
            }
        }

        // print an array of floats to screen
        // No line returns except at the end
        public static void print(float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of floats to screen with truncation
        // No line returns except at the end
        public static void print(float[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.print(Fmath.truncate(aa[i], trunc)+"   ");
            }
            System.out.println();
        }

        // print an array of floats to screen
        // with line returns
        public static void println(float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of floats to screen with truncation
        // with line returns
        public static void println(float[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.println(Fmath.truncate(aa[i], trunc)+"   ");
            }
        }

        // print an array of ints to screen
        // No line returns except at the end
        public static void print(int[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of ints to screen
        // with line returns
        public static void println(int[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of longs to screen
        // No line returns except at the end
        public static void print(long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of longs to screen
        // with line returns
        public static void println(long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of shorts to screen
        // No line returns except at the end
        public static void print(short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of shorts to screen
        // with line returns
        public static void println(short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of char to screen
        // No line returns except at the end
        public static void print(char[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of char to screen
        // with line returns
        public static void println(char[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }


        // print an array of bytes to screen
        // No line returns except at the end
        public static void print(byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of bytes to screen
        // with line returns
        public static void println(byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }


        // print an array of Doubles to screen
        // No line returns except at the end
        public static void print(Double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Doubles to screen with truncation
        // No line returns except at the end
        public static void print(Double[] aa, int trunc){
            ArrayMaths am = new ArrayMaths(aa);
            am = am.truncate(trunc);
            Double[] aaa = am.array_as_Double();
            for(int i=0; i<aa.length; i++){
                System.out.print(aaa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Doubles to screen
        // with line returns
        public static void println(Double[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Doubles to screen with truncation
        // with line returns
        public static void println(Double[] aa, int trunc){
            ArrayMaths am = new ArrayMaths(aa);
            am = am.truncate(trunc);
            Double[] aaa = am.array_as_Double();
            for(int i=0; i<aa.length; i++){
                System.out.println(aaa[i]+"   ");
            }
        }

        // print an array of Floats to screen
        // No line returns except at the end
        public static void print(Float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Floats to screen
        // No line returns except at the end
        public static void print(Float[] aa, int trunc){
            ArrayMaths am = new ArrayMaths(aa);
            am = am.truncate(trunc);
            Float[] aaa = am.array_as_Float();
            for(int i=0; i<aa.length; i++){
                System.out.print(aaa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Floats to screen
        // with line returns
        public static void println(Float[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Floats to screen with truncation
        // with line returns
        public static void println(Float[] aa, int trunc){
            ArrayMaths am = new ArrayMaths(aa);
            am = am.truncate(trunc);
            Float[] aaa = am.array_as_Float();
            for(int i=0; i<aa.length; i++){
                System.out.println(aaa[i]+"   ");
            }
        }

        // print an array of Integers to screen
        // No line returns except at the end
        public static void print(Integer[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Integers to screen
        // with line returns
        public static void println(Integer[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Longs to screen
        // No line returns except at the end
        public static void print(Long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Longs to screen
        // with line returns
        public static void println(Long[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Shorts to screen
        // No line returns except at the end
        public static void print(Short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Shorts to screen
        // with line returns
        public static void println(Short[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Character to screen
        // No line returns except at the end
        public static void print(Character[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Character to screen
        // with line returns
        public static void println(Character[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }


        // print an array of Bytes to screen
        // No line returns except at the end
        public static void print(Byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Bytes to screen
        // with line returns
        public static void println(Byte[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of String to screen
        // No line returns except at the end
        public static void print(String[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Strings to screen
        // with line returns
        public static void println(String[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Complex to screen
        // No line returns except at the end
        public static void print(Complex[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Complex to screen with truncation
        // No line returns except at the end
        public static void print(Complex[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.print(Complex.truncate(aa[i], trunc)+"   ");
            }
            System.out.println();
        }

        // print an array of Complex to screen
        // with line returns
        public static void println(Complex[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Complex to screen with truncation
        // with line returns
        public static void println(Complex[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.println(Complex.truncate(aa[i], trunc)+"   ");
            }
        }


        // print an array of Phasor to screen
        // No line returns except at the end
        public static void print(Phasor[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of Phasor to screen with truncation
        // No line returns except at the end
        public static void print(Phasor[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.print(Phasor.truncate(aa[i], trunc)+"   ");
            }
            System.out.println();
        }

        // print an array of Phasor to screen
        // with line returns
        public static void println(Phasor[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of Phasor to screen with truncation
        // with line returns
        public static void println(Phasor[] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                System.out.println(Phasor.truncate(aa[i], trunc)+"   ");
            }
        }


        // print an array of BigDecimal to screen
        // No line returns except at the end
        public static void print(BigDecimal[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of BigDecimal to screen
        // with line returns
        public static void println(BigDecimal[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of BigInteger to screen
        // No line returns except at the end
        public static void print(BigInteger[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of BigInteger to screen
        // with line returns
        public static void println(BigInteger[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }

        // print an array of boolean to screen
        // No line returns except at the end
        public static void print(boolean[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.print(aa[i]+"   ");
            }
            System.out.println();
        }

        // print an array of boolean to screen
        // with line returns
        public static void println(boolean[] aa){
            for(int i=0; i<aa.length; i++){
                System.out.println(aa[i]+"   ");
            }
        }


        // 2D ARRAYS

        // print a 2D array of doubles to screen
        public static void print(double[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of doubles to screen with truncation
        public static void print(double[][] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i], trunc);
            }
        }

        // print a 2D array of floats to screen
        public static void print(float[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of floats to screen with truncation
        public static void print(float[][] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i], trunc);
            }
        }

        // print a 2D array of ints to screen
        public static void print(int[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of longs to screen
        public static void print(long[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }
        // print a 2D array of chars to screen
        public static void print(char[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of bytes to screen
        public static void print(byte[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of shorts to screen
        public static void print(short[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Doubles to screen
        public static void print(Double[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Doubles to screen with truncation
        public static void print(Double[][] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i], trunc);
            }
        }

        // print a 2D array of Floats to screen
        public static void print(Float[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Floats to screen with truncation
        public static void print(Float[][] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i], trunc);
            }
        }


        // print a 2D array of Integers to screen
        public static void print(Integer[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Longs to screen
        public static void print(Long[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }
        // print a 2D array of Characters to screen
        public static void print(Character[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Bytes to screen
        public static void print(Byte[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Shorts to screen
        public static void print(Short[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Strings to screen
        public static void print(String[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of Complex to screen
        public static void print(Complex[][] aa){
            for(int i=0; i<aa.length; i++){
                Complex.print(aa[i]);
            }
        }

        // print a 2D array of Complex to screen with truncation
        public static void print(Complex[][] aa, int trunc){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i], trunc);
            }
        }

        // print a 2D array of Phasor to screen
        public static void print(Phasor[][] aa){
            for(int i=0; i<aa.length; i++){
                Phasor.print(aa[i]);
            }
        }

        // print a 2D array of Phasor to screen
        public static void print(Phasor[][] aa, int trunc){

            Phasor[][] aam = Conv.copy(aa);
            for(int i=0; i<aam.length; i++){
                for(int j=0; j<aam[i].length; j++){
                    aam[i][j] = Phasor.truncate(aam[i][j], trunc);
                }
            }
            for(int i=0; i<aa.length; i++){
                Phasor.print(aam[i]);
            }
        }

        // print a 2D array of BigDecimal to screen
        public static void print(BigDecimal[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of BigInteger to screen
        public static void print(BigInteger[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }

        // print a 2D array of boolean to screen
        public static void print(boolean[][] aa){
            for(int i=0; i<aa.length; i++){
                PrintToScreen.print(aa[i]);
            }
        }
}



