/*
*   Class   KeyboardInput
*
*   Methods for entering
*       double, float, BigDecimal,
*       int, long, bigInteger, short, byte,
*       String, char, boolean,
*       Complex and Phasor variables
*   from the key board
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    July 2002
*   REVISED: 26 July 2004, 26 June 2007, 21-23 July 2007
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/KeyboardInput.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) July 2002, July 2007
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

package flanagan.io;

import java.io.*;
import java.math.*;

import flanagan.complex.Complex;
import flanagan.circuits.Phasor;

public class KeyboardInput
{
        // Data variable - buffered stream for the keyboard
        private BufferedReader input = null;

        // Constructor
        public KeyboardInput(){

                this.input = new BufferedReader(new InputStreamReader(System.in));
        }

        // Reads a double from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized double readDouble(String mess){
                String line="";
                double d=0.0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                d = Double.parseDouble(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid double\nRe-enter the number");
                        }
                }

                return d;
        }

        // Reads a double from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized double readDouble(String mess, double dflt){
                String line="";
                double d=0.0D;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                d = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        d = Double.parseDouble(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid double\nRe-enter the number");
                                }
                        }
                }
                return d;
        }

        // Reads a double from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized double readDouble(){
                String line="";
                double d=0.0D;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                d = Double.parseDouble(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid double\nRe-enter the number");
                        }
                }

                return d;
        }

        // Reads a float from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized float readFloat(String mess){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                f = Float.parseFloat(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid float\nRe-enter the number");
                        }
                }

                return f;
        }

        // Reads a float from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized float readFloat(String mess, float dflt){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                f = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        f = Float.parseFloat(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid float\nRe-enter the number");
                                }
                        }
                }
                return f;
        }

        // Reads a float from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized float readFloat(){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                f = Float.parseFloat(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid float\nRe-enter the number");
                        }
                }

                return f;
        }

        // Reads a BigDecimal from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess){
                String line="";
                BigDecimal big = null;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                big = new BigDecimal(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                        }
                }

                return big;
        }


        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a BigDecimal default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, BigDecimal dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt.toString() + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }

        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a double default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, double dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;
                Double dfltD = new Double(dflt);
                String dfltM = dfltD.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigDecimal(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a float default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, float dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;
                Float dfltF = new Float(dflt);
                String dfltM = dfltF.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigDecimal(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a long default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, long dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;
                Long dfltL = new Long(dflt);
                String dfltM = dfltL.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigDecimal(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a int default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, int dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;
                Integer dfltI = new Integer(dflt);
                String dfltM = dfltI.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigDecimal(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigDecimal from the keyboard with a prompt message and the return
        // of a String default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(String mess, String dflt){
                String line="";
                BigDecimal big = null;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigDecimal(dflt);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigDecimal(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                                }
                        }
                }
                return big;
        }

        // Reads a BigDecimal from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized BigDecimal readBigDecimal(){
                String line="";
                BigDecimal big = null;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                big = new BigDecimal(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid BigDecimal\nRe-enter the number");
                        }
                }

                return big;
        }

        // Reads an int (integer) from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized int readInt(String mess){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ii = Integer.parseInt(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid int\nRe-enter the number");
                        }
                }

                return ii;
        }

        // Reads an int (integer) from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized int readInt(String mess, int dflt){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                ii = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        ii = Integer.parseInt(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid int\nRe-enter the number");
                                }
                        }
                }
                return ii;
        }

        // Reads an int (integer) from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized int readInt(){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ii = Integer.parseInt(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid int\nRe-enter the number");
                        }
                }

                return ii;
        }

        // Reads a long integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized long readLong(String mess){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ll = Long.parseLong(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid long\nRe-enter the number");
                        }
                }

                return ll;
        }

        // Reads a long integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized long readLong(String mess, long dflt){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                ll = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        ll = Long.parseLong(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid long\nRe-enter the number");
                                }
                        }
                }
                return ll;
        }

        // Reads a long integer from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized long readLong(){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ll = Long.parseLong(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid long\nRe-enter the number");
                        }
                }

                return ll;
        }

        // Reads a BigInteger from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(String mess){
                String line="";
                BigInteger big = null;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                big = new BigInteger(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                        }
                }

                return big;
        }


        // Reads a BigInteger from the keyboard with a prompt message and the return
        // of a BigInteger default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(String mess, BigInteger dflt){
                String line="";
                BigInteger big = null;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt.toString() + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigInteger(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                                }
                        }
                }
                return big;
        }

        // Reads a BigInteger from the keyboard with a prompt message and the return
        // of a long default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(String mess, long dflt){
                String line="";
                BigInteger big = null;
                boolean finish = false;
                Long dfltL = new Long(dflt);
                String dfltM = dfltL.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigInteger(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigInteger(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigInteger from the keyboard with a prompt message and the return
        // of a int default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(String mess, int dflt){
                String line="";
                BigInteger big = null;
                boolean finish = false;
                Integer dfltI = new Integer(dflt);
                String dfltM = dfltI.toString();

                System.out.print(mess + " [default value = " + dfltM + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigInteger(dfltM);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigInteger(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                                }
                        }
                }
                return big;
        }


        // Reads a BigInteger from the keyboard with a prompt message and the return
        // of a String default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(String mess, String dflt){
                String line="";
                BigInteger big = null;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                big = new BigInteger(dflt);
                                finish = true;
                        }
                        else{
                                try{
                                        big = new BigInteger(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                                }
                        }
                }
                return big;
        }

        // Reads a BigInteger from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized BigInteger readBigInteger(){
                String line="";
                BigInteger big = null;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                big = new BigInteger(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid BigInteger\nRe-enter the number");
                        }
                }

                return big;
        }


        // Reads a short integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized short readShort(String mess){
                String line="";
                short ss = 0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ss = Short.parseShort(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid short\nRe-enter the number");
                        }
                }

                return ss;
        }

        // Reads a short integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized short readShort(String mess, short dflt){
                String line="";
                short ss = 0;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                ss = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        ss = Short.parseShort(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid short\nRe-enter the number");
                                }
                        }
                }
                return ss;
        }

        // Reads a short integer from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized short readShort(){
                String line="";
                short ss = 0;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ss = Short.parseShort(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid short\nRe-enter the number");
                        }
                }

                return ss;
        }

        // Reads a byte integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized byte readByte(String mess){
                String line="";
                byte bb = 0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                bb = Byte.parseByte(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid byte\nRe-enter the number");
                        }
                }

                return bb;
        }

        // Reads a byte integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized byte readByte(String mess, byte dflt){
                String line="";
                byte bb = 0;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                bb = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        bb = Byte.parseByte(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid byte\nRe-enter the number");
                                }
                        }
                }
                return bb;
        }

        // Reads a byte integer from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized byte readByte(){
                String line="";
                byte bb = 0;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                bb = Byte.parseByte(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid byte\nRe-enter the number");
                        }
                }

                return bb;
        }

        // Reads a long integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized char readChar(String mess){
                String line="";
                char ch=' ';
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a long integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized char readChar(String mess, char dflt){
                String line="";
                char ch=' ';
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a char from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized char readChar(){
                String line = "";
                char ch = ' ';
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a boolean from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized boolean readBoolean(String mess){
                String line="";
                boolean b=false;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a boolean from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized boolean readBoolean(String mess, boolean dflt){
                String line="";
                boolean b=false;
                boolean finish = false;
                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a boolean from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized boolean readBoolean(){
                String line="";
                boolean b=false;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a Complex from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess){
                return Complex.readComplex(mess);
        }

        // Reads a Complex from the keyboard with a prompt message and Stringdefault value option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess, String dflt){
                return Complex.readComplex(mess, dflt);
        }

        // Reads a Complex from the keyboard with a prompt message and Complexdefault value option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess, Complex dflt){
                return Complex.readComplex(mess, dflt);
        }


        // Reads a Complex from the keyboard
        // No prompt
        // No default option
        // Input terminated by new line return
        public final synchronized Complex readComplex(){
                return Complex.readComplex();
        }

        // Reads a Phasor from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized Phasor readPhasor(String mess){
                return Phasor.readPhasor(mess);
        }

        // Reads a Phasor from the keyboard with a prompt message and Stringdefault value option
        // Input terminated by new line return
        public final synchronized Phasor readPhasor(String mess, String dflt){
                return Phasor.readPhasor(mess, dflt);
        }

        // Reads a Phasor from the keyboard with a prompt message and Phasordefault value option
        // Input terminated by new line return
        public final synchronized Phasor readPhasor(String mess, Phasor dflt){
                return Phasor.readPhasor(mess, dflt);
        }


        // Reads a Phasor from the keyboard
        // No prompt
        // No default option
        // Input terminated by new line return
        public final synchronized Phasor readPhasor(){
                return Phasor.readPhasor();
        }

        // Reads a line from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized String readLine(String mess){
                System.out.print(mess + " ");
                System.out.flush();

                return this.enterLine();
        }

        // Reads a line from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized String readLine(String mess, String dflt){
                String line = "";
                System.out.print(mess + " [default option = " + dflt + "] ");
                System.out.flush();

                line = this.enterLine();
                if(line.length()==0)line = dflt;

                return line;
        }

        // Reads a line from the keyboard
        // No prompt message, no default option
        // Input terminated by new line return
        private final synchronized String readLine(){

                return this.enterLine();
        }

        // Enters a line from the keyboard
        // Private method called by public methods
        private final synchronized String enterLine(){
                String line = "";

                try{
                        line = input.readLine();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }

                return line;
        }
}
