/*
*   Class FileOutput
*
*   Methods for writing doubles, floats, BigDecimals,
*   integers, long integers, short integers, bytes,
*   Strings, chars, booleans, Complex, Phasor,
*   ErrorProp and ComplexErrorProp to a text file.
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    July 2002
*   UPDATED: 26 April 2004, 22 January 2006, 27 June 2007, 21-23 July 2007, 1 February 2009
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/FileOutput.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2009
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

package flanagan.io;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import flanagan.complex.*;
import flanagan.circuits.Phasor;
import flanagan.analysis.ErrorProp;

public class FileOutput{

        // Instance variables
        private String filename = "";       // output file name
        private FileWriter fwoutput = null; // instance of FileWriter
        private PrintWriter output = null;  // instance of PrintWriter
        private boolean append = false;     // true data appended to a file, false new file
        private char app = 'w';             // 'w' new file - overwrites an existing file of the same name
                                            // 'a' append to existing file, creates a new file if file does not exist
                                            // 'n' adds a number to file name. If file name of that number exists creates a file with next highest number added to name
        private boolean fileExists = true;  // = false if file of output filename does not already exist

        // Constructors

        public FileOutput(String filename, char app){
                this.filename = filename;
                this.app = app;
                this.setFilenames(filename, app);
        }

        public FileOutput(String filename, String apps){
                this.filename = filename;
                this.app = apps.charAt(0);
                this.setFilenames(filename, app);
        }

        public FileOutput(String filename){
                this.filename = filename;
                this.app = 'w';
                this.setFilenames(filename, app);
        }

        // Methods

        // Set the file names
        private void setFilenames(String filename, char app){
                BufferedReader input0;
                try{
                    input0 = new BufferedReader(new FileReader(filename));
                }catch(FileNotFoundException e){
                    this.fileExists = false;
                }

                if(this.app == 'n'){
                        boolean test = true;
                        int i = 0;
                        BufferedReader input;
                        String ext = "";
                        String filename0 = "";

                        int idot=filename.indexOf('.');
                        if(idot!=-1){
                                ext += filename.substring(idot);
                                filename0 += filename.substring(0,idot);
                        }
                        else{
                                filename0 += filename;
                        }

                        while(test){
                                i++;
                                filename=filename0+String.valueOf(i)+ext;
                                try{
                                        input = new BufferedReader(new FileReader(filename));
                                }catch(FileNotFoundException e){
                                        test=false;
                                        this.filename=filename;
                                }
                        }
                }

                if(this.app == 'a'){
                        this.append=true;
                }
                else{
                        this.append = false;
                }
                try{
                        fwoutput = new FileWriter(filename, this.append);
                }
                catch(IOException e){
                        System.out.println(e);
                }

                this.output = new PrintWriter(new BufferedWriter(fwoutput));
        }

        // Return filename
        public String getFilename(){
            return this.filename;
        }

        // Return file already exists check
        // returns true if it does, false if it does not
        public boolean checkFileAlreadyExists(){
            return this.fileExists;
        }


        // PRINT WITH NO FOLLOWING SPACE OR CHARACTER AND NO LINE RETURN

        // Prints character, no line return
        public final synchronized void print(char ch){
                this.output.print(ch);
        }

        // Prints character, no line return, fixed field length
        public final synchronized void print(char ch, int f){
                String ss ="";
                ss = ss + ch;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints string, no line return
        public final synchronized void print(String word){
                this.output.print(word);
        }


        // Prints string, no line return, fixed field length
        public final synchronized void print(String word, int f){
                String ss ="";
                ss = ss + word;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }
        // Prints double, no line return
        public final synchronized void print(double dd){
                this.output.print(dd);
        }

        // Prints double, no line return, fixed field length
        public final synchronized void print(double dd, int f){
                String ss ="";
                ss = ss + dd;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints float, no line return
        public final synchronized void print(float ff){
                this.output.print(ff);
        }

        // Prints float, no line return, fixed field length
        public final synchronized void print(float ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints BigDecimal, no line return
        public final synchronized void print(BigDecimal big){
                this.output.print(big.toString());
        }

        // Prints BigDecimal big, no line return, fixed field length
        public final synchronized void print(BigDecimal big , int f){
                String ss ="";
                ss = ss + big.toString();
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints BigInteger, no line return
        public final synchronized void print(BigInteger big){
                this.output.print(big.toString());
        }

        // Prints BigInteger big, no line return, fixed field length
        public final synchronized void print(BigInteger big , int f){
                String ss ="";
                ss = ss + big.toString();
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints Complex, no line return
        public final synchronized void print(Complex ff){
                this.output.print(ff.toString());
        }

        // Prints Complex, no line return, fixed field length
        public final synchronized void print(Complex ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints Phasor, no line return
        public final synchronized void print(Phasor ff){
                this.output.print(ff.toString());
        }

        // Prints Phasor, no line return, fixed field length
        public final synchronized void print(Phasor ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints ErrorProp, no line return
        public final synchronized void print(ErrorProp ff){
                this.output.print(ff.toString());
        }

        // Prints ErrorProp, no line return, fixed field length
        public final synchronized void print(ErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints ComplexErrorProp, no line return
        public final synchronized void print(ComplexErrorProp ff){
                this.output.print(ff.toString());
        }

        // Prints ComplexErrorProp, no line return, fixed field length
        public final synchronized void print(ComplexErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints int, no line return
        public final synchronized void print(int ii){
                this.output.print(ii);
        }

        // Prints int, no line return, fixed field length
        public final synchronized void print(int ii, int f){
                String ss ="";
                ss = ss + ii;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints long integer, no line return
        public final synchronized void print(long ll){
                this.output.print(ll);
        }

        // Prints long integer, no line return, fixed field length
        public final synchronized void print(long ll, int f){
                String ss ="";
                ss = ss + ll;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints short integer, no line return
        public final synchronized void print(short ss){
                this.output.print(ss);
        }

        // Prints short integer, no line return, fixed field length
        public final synchronized void print(short si, int f){
                String ss ="";
                ss = ss + si;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints byte integer, no line return
        public final synchronized void print(byte by){
                this.output.print(by);
        }

        // Prints short integer, no line return, fixed field length
        public final synchronized void print(byte by, int f){
                String ss ="";
                ss = ss + by;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints boolean, no line return
        public final synchronized void print(boolean bb){
                this.output.print(bb);
        }

        // Prints boolean, no line return, fixed field length
        public final synchronized void print(boolean bb, int f){
                String ss ="";
                ss = ss + bb;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
        }

        // Prints array of doubles, no line return
        public final synchronized void print(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of floats, no line return
        public final synchronized void print(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of BigDecimal, no line return
        public final synchronized void print(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of BigInteger, no line return
        public final synchronized void print(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of int, no line return
        public final synchronized void print(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of short, no line return
        public final synchronized void print(short[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of byte, no line return
        public final synchronized void print(byte[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of char, no line return
        public final synchronized void print(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of boolean, no line return
        public final synchronized void print(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of Strings, no line return
        public final synchronized void print(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of Complex, no line return
        public final synchronized void print(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of Phasor, no line return
        public final synchronized void print(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }

        // Prints array of ErrorProp, no line return
        public final synchronized void print(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                   this.output.print(array[i]);
                }
        }

        // Prints array of ComplexErrorProp, no line return
        public final synchronized void print(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                }
        }


        // Prints array of doubles, no line return, fixed field length
        public final synchronized void print(double[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of floats, no line return, fixed field length
        public final synchronized void print(float[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of BigDecimal, no line return, fixed field length
        public final synchronized void print(BigDecimal[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of BigInteger, no line return, fixed field length
        public final synchronized void print(BigInteger[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of long, no line return, fixed field length
        public final synchronized void print(long[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of int, no line return, fixed field length
        public final synchronized void print(int[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of short, no line return, fixed field length
        public final synchronized void print(short[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of byte, no line return, fixed field length
        public final synchronized void print(byte[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of char, no line return, fixed field length
        public final synchronized void print(char[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of boolean, no line return, fixed field length
        public final synchronized void print(boolean[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of Strings, no line return, fixed field length
        public final synchronized void print(String[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of Complex, no line return, fixed field length
        public final synchronized void print(Complex[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of Phasor, no line return, fixed field length
        public final synchronized void print(Phasor[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of ErrorProp, no line return, fixed field length
        public final synchronized void print(ErrorProp[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints array of ComplexErrorProp, no line return, fixed field length
        public final synchronized void print(ComplexErrorProp[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                }
        }

        // Prints date and time  (no line return);
        public final synchronized void dateAndTime(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
        }

        // Prints file title (title), date and time  (no line return);
        public final synchronized void dateAndTime(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
        }

        // PRINT WITH SPACE (NO LINE RETURN)
        // Prints character plus space, no line return
        public final synchronized void printsp(char ch){
                this.output.print(ch);
                this.output.print(" ");
        }

        // Prints string plus space, no line return
        public final synchronized void printsp(String word){
                this.output.print(word + " ");
        }

        // Prints double plus space, no line return
        public final synchronized void printsp(double dd){
                this.output.print(dd);
                this.output.print(" ");
        }

        // Prints float plus space, no line return
        public final synchronized void printsp(float ff){
                this.output.print(ff);
                this.output.print(" ");
        }

        // Prints BigDecimal plus space, no line return
        public final synchronized void printsp(BigDecimal big){
                this.output.print(big.toString());
                this.output.print(" ");
        }

        // Prints BigInteger plus space, no line return
        public final synchronized void printsp(BigInteger big){
                this.output.print(big.toString());
                this.output.print(" ");
        }

        // Prints Complex plus space, no line return
        public final synchronized void printsp(Complex ff){
                this.output.print(ff.toString());
                this.output.print(" ");
        }

        // Prints Phasor plus space, no line return
        public final synchronized void printsp(Phasor ff){
                this.output.print(ff.toString());
                this.output.print(" ");
        }

        // Prints ErrorProp plus space, no line return
        public final synchronized void printsp(ErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(" ");
        }

        // Prints ComplexErrorProp plus space, no line return
        public final synchronized void printsp(ComplexErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(" ");
        }

        // Prints int plus space, no line return
        public final synchronized void printsp(int ii){
                this.output.print(ii);
                this.output.print(" ");
        }

        // Prints long integer plus space, no line return
        public final synchronized void printsp(long ll){
                this.output.print(ll);
                this.output.print(" ");
        }

        // Prints short integer plus space, no line return
        public final synchronized void printsp(short ss){
                this.output.print(ss);
                this.output.print(" ");
        }

        // Prints byte integer plus space, no line return
        public final synchronized void printsp(byte by){
                this.output.print(by);
                this.output.print(" ");
        }

        // Prints boolean plus space, no line return
        public final synchronized void printsp(boolean bb){
                this.output.print(bb);
                this.output.print(" ");
        }

        // Prints  space, no line return
        public final synchronized void printsp(){
                this.output.print(" ");
        }

        // Prints array of doubles, separated by spaces
        public final synchronized void printsp(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of floats, separated by spaces
        public final synchronized void printsp(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of BigDecimal, separated by spaces
        public final synchronized void printsp(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of BigInteger, separated by spaces
        public final synchronized void printsp(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of long, separated by spaces
        public final synchronized void printsp(long[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of int, separated by spaces
        public final synchronized void printsp(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of char, separated by spaces
        public final synchronized void printsp(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of short, separated by spaces
        public final synchronized void printsp(short[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of byte, separated by spaces
        public final synchronized void printsp(byte[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of boolean, separated by spaces
        public final synchronized void printsp(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of Strings, separated by spaces
        public final synchronized void printsp(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of Complex, separated by spaces
        public final synchronized void printsp(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of Phasor, separated by spaces
        public final synchronized void printsp(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }

        // Prints array of ErrorProp, separated by spaces
        public final synchronized void printsp(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                   this.output.print(array[i]);
                   this.output.print(" ");
                }
        }

        // Prints array of ComplexErrorProp, separated by spaces
        public final synchronized void printsp(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(" ");
                }
        }


        // Prints date and time (plus space, no line return);
        public final synchronized void dateAndTimesp(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(" ");
        }

        // Prints file title (title), date and time  (no line return);
        public final synchronized void dateAndTimesp(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(" ");
        }

        // PRINT WITH LINE RETURN
        // Prints character with line return
        public final synchronized void println(char ch){
                this.output.println(ch);
        }

        // Prints string with line return
        public final synchronized void println(String word){
                this.output.println(word);
        }

        // Prints double with line return
        public final synchronized void println(double dd){
                this.output.println(dd);
        }

        // Prints float with line return
        public final synchronized void println(float ff){
                this.output.println(ff);
        }

        // Prints BigDecimal with line return
        public final synchronized void println(BigDecimal big){
                this.output.println(big.toString());
        }

        // Prints BigInteger with line return
        public final synchronized void println(BigInteger big){
                this.output.println(big.toString());
        }

        // Prints Complex with line return
        public final synchronized void println(Complex ff){
                this.output.println(ff.toString());
        }

        // Prints Phasor with line return
        public final synchronized void println(Phasor ff){
                this.output.println(ff.toString());
        }

        // Prints ErrorProp with line return
        public final synchronized void println(ErrorProp ff){
                this.output.println(ff.toString());
        }

        // Prints ComplexErrorProp with line return
        public final synchronized void println(ComplexErrorProp ff){
                this.output.println(ff.toString());
        }

        // Prints int with line return
        public final synchronized void println(int ii){
                this.output.println(ii);
        }

        // Prints long integer with line return
        public final synchronized void println(long ll){
                this.output.println(ll);
        }

        // Prints short integer with line return
        public final synchronized void println(short ss){
                this.output.println(ss);
        }

        // Prints byte integer with line return
        public final synchronized void println(byte by){
                this.output.println(by);
        }

        // Prints boolean with line return
        public final synchronized void println(boolean bb){
                this.output.println(bb);
        }

        // Prints  line return
        public final synchronized void println(){
                this.output.println("");
        }

        // Prints array of doubles, each followed by a line return
        public final synchronized void println(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of floats, each followed by a line return
        public final synchronized void println(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                 }
        }

        // Prints array of BigDecimal, each followed by a line return
        public final synchronized void println(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                 }
        }

        // Prints array of BigInteger, each followed by a line return
        public final synchronized void println(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                 }
        }

        // Prints array of long, each followed by a line return
        public final synchronized void println(long[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of int, each followed by a line return
        public final synchronized void println(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of short, each followed by a line return
        public final synchronized void println(short[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of byte, each followed by a line return
        public final synchronized void println(byte[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of char, each followed by a line return
        public final synchronized void println(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of boolean, each followed by a line return
        public final synchronized void println(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of Strings, each followed by a line return
        public final synchronized void println(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of Complex, each followed by a line return
        public final synchronized void println(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of Phasor, each followed by a line return
        public final synchronized void println(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }

        // Prints array of ErrorProp, each followed by a line return
        public final synchronized void println(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                   this.output.println(array[i]);
                }
        }

        // Prints array of ComplexErrorProp, each followed by a line return
        public final synchronized void println(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.println(array[i]);
                }
        }


        // Prints date and time as date-month-year hour:minute:second (with line return);
        public final synchronized void dateAndTimeln(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+this.filename+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.println(day);
        }

        // Prints file title (title), date and time (with line return);
        public final synchronized void dateAndTimeln(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.println(day);
        }

        // PRINT WITH FOLLOWING TAB, NO LINE RETURN
        // Prints character plus tab, no line return
        public final synchronized void printtab(char ch){
                this.output.print(ch);
                this.output.print("\t");
        }

        // Prints character plus tab, no line return, fixed field length
        public final synchronized void printtab(char ch, int f){
                String ss ="";
                ss = ss + ch;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints string plus tab, no line return
        public final synchronized void printtab(String word){
                this.output.print(word + "\t");
        }

        // Prints string plus tab, no line return, fixed field length
        public final synchronized void printtab(String word, int f){
            String ss ="";
                ss = ss + word;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints double plus tab, no line return
        public final synchronized void printtab(double dd){
                this.output.print(dd);
                this.output.print("\t");
        }

        // Prints double plus tab, fixed field length, fixed field length
        public final synchronized void printtab(double dd, int f){
                String ss ="";
                ss = ss + dd;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints float plus tab, no line return
        public final synchronized void printtab(float ff){
                this.output.print(ff);
                this.output.print("\t");
        }

        // Prints float plus tab, no line return, fixed field length
        public final synchronized void printtab(float ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints BigDecimal plus tab, no line return
        public final synchronized void printtab(BigDecimal big){
                this.output.print(big.toString());
                this.output.print("\t");
        }

        // Prints BigDecimal plus tab, no line return, fixed field length
        public final synchronized void printtab(BigDecimal big, int f){
                String ss ="";
                ss = ss + big.toString();
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints BigInteger plus tab, no line return
        public final synchronized void printtab(BigInteger big){
                this.output.print(big.toString());
                this.output.print("\t");
        }

        // Prints BigInteger plus tab, no line return, fixed field length
        public final synchronized void printtab(BigInteger big, int f){
                String ss ="";
                ss = ss + big.toString();
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints Complex plus tab, no line return
        public final synchronized void printtab(Complex ff){
                this.output.print(ff.toString());
                this.output.print("\t");
        }

        // Prints Phasor plus tab, no line return
        public final synchronized void printtab(Phasor ff){
                this.output.print(ff.toString());
                this.output.print("\t");
        }

        // Prints Complex plus tab, no line return, fixed field length
        public final synchronized void printtab(Complex ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints Phasor plus tab, no line return, fixed field length
        public final synchronized void printtab(Phasor ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints ErrorProp plus tab, no line return
        public final synchronized void printtab(ErrorProp ff){
                this.output.print(ff.toString());
                this.output.print("\t");
        }

        // Prints ErrorProp plus tab, no line return, fixed field length
        public final synchronized void printtab(ErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints ComplexErrorProp plus tab, no line return
        public final synchronized void printtab(ComplexErrorProp ff){
                this.output.print(ff.toString());
                this.output.print("\t");
        }

        // Prints ComplexErrorProp plus tab, no line return, fixed field length
        public final synchronized void printtab(ComplexErrorProp ff, int f){
                String ss ="";
                ss = ss + ff;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints int plus tab, no line return
        public final synchronized void printtab(int ii){
                this.output.print(ii);
                this.output.print("\t");
        }

        // Prints int plus tab, no line return, fixed field length
        public final synchronized void printtab(int ii, int f){
               String ss ="";
                ss = ss + ii;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints long integer plus tab, no line return
        public final synchronized void printtab(long ll){
                this.output.print(ll);
                this.output.print("\t");
        }

        // Prints long integer plus tab, no line return, fixed field length
        public final synchronized void printtab(long ll, int f){
               String ss ="";
                ss = ss + ll;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints short integer plus tab, no line return
        public final synchronized void printtab(short ss){
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints short integer plus tab, no line return, fixed field length
        public final synchronized void printtab(short si, int f){
               String ss ="";
                ss = ss + si;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints byte integer plus tab, no line return
        public final synchronized void printtab(byte by){
                this.output.print(by);
                this.output.print("\t");
        }

        // Prints byte integer plus tab, no line return, fixed field length
        public final synchronized void printtab(byte by, int f){
               String ss ="";
                ss = ss + by;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints boolean plus tab, no line return
        public final synchronized void printtab(boolean bb){
                this.output.print(bb);
                this.output.print("\t");
        }

        // Prints boolean plus tab, no line return, fixed field length
        public final synchronized void printtab(boolean bb, int f){
                String ss ="";
                ss = ss + bb;
                ss = FileOutput.setField(ss,f);
                this.output.print(ss);
                this.output.print("\t");
        }

        // Prints tab, no line return
        public final synchronized void printtab(){
                this.output.print("\t");
        }

        // Prints array of doubles, tab, no line return, fixed field length
        public final synchronized void printtab(double[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of floats, tab, no line return, fixed field length
        public final synchronized void printtab(float[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of BigDecimal, tab, no line return, fixed field length
        public final synchronized void printtab(BigDecimal[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i].toString();
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of BigInteger, tab, no line return, fixed field length
        public final synchronized void printtab(BigInteger[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i].toString();
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }


        // Prints array of long, tab, no line return, fixed field length
        public final synchronized void printtab(long[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of int, tab, no line return, fixed field length
        public final synchronized void printtab(int[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of short, tab, no line return, fixed field length
        public final synchronized void printtab(short[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of byte, tab, no line return, fixed field length
        public final synchronized void printtab(byte[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of char, tab, no line return, fixed field length
        public final synchronized void printtab(char[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of boolean, tab, no line return, fixed field length
        public final synchronized void printtab(boolean[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of Strings, tab, no line return, fixed field length
        public final synchronized void printtab(String[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of Complex, tab, no line return, fixed field length
        public final synchronized void printtab(Complex[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of Phasor, tab, no line return, fixed field length
        public final synchronized void printtab(Phasor[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of ErrorProp, tab, no line return, fixed field length
        public final synchronized void printtab(ErrorProp[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of ComplexErrorProp, tab, no line return, fixed field length
        public final synchronized void printtab(ComplexErrorProp[] array, int f){
                int n = array.length;
                for(int i=0; i<n; i++){
                    String ss ="";
                    ss = ss + array[i];
                    ss = FileOutput.setField(ss,f);
                    this.output.print(ss);
                    this.output.print("\t");
                }
        }

        // Prints array of doubles, tab, no line return
        public final synchronized void printtab(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of floats, tab, no line return
        public final synchronized void printtab(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of BigDecimal, tab, no line return
        public final synchronized void printtab(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print("\t");
                }
        }

        // Prints array of BigInteger, tab, no line return
        public final synchronized void printtab(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print("\t");
                }
        }

        // Prints array of long, tab, no line return
        public final synchronized void printtab(long[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of int, tab, no line return
        public final synchronized void printtab(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of char, tab, no line return
        public final synchronized void printtab(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of boolean, tab, no line return
        public final synchronized void printtab(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of Strings, tab, no line return
        public final synchronized void printtab(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of Complex, tab, no line return
        public final synchronized void printtab(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of Phasor, tab, no line return
        public final synchronized void printtab(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of ErrorProp, tab, no line return
        public final synchronized void printtab(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }

        // Prints array of ComplexErrorProp, tab, no line return
        public final synchronized void printtab(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print("\t");
                }
        }


        // Prints date and time (plus tab, no line return);
        public final synchronized void dateAndTimetab(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print("\t");
        }

        // Prints file title (title), date and time (plus tab, no line return);
        public final synchronized void dateAndTimetab(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print("\t");
        }

        // PRINT FOLLOWED BY A COMMA, NO LINE RETURN
        // Prints character plus comma, no line return
        public final synchronized void printcomma(char ch){
                this.output.print(ch);
                this.output.print(",");
        }

        // Prints string plus comma, no line return
        public final synchronized void printcomma(String word){
                this.output.print(word + ",");
        }

        // Prints double plus comma, no line return
        public final synchronized void printcomma(double dd){
                this.output.print(dd);
                this.output.print(",");
        }

        // Prints float plus comma, no line return
        public final synchronized void printcomma(float ff){
                this.output.print(ff);
                this.output.print(",");
        }

        // Prints BigDecimal plus comma, no line return
        public final synchronized void printcomma(BigDecimal big){
                this.output.print(big.toString());
                this.output.print(",");
        }

        // Prints BigInteger plus comma, no line return
        public final synchronized void printcomma(BigInteger big){
                this.output.print(big.toString());
                this.output.print(",");
        }

        // Prints Complex plus comma, no line return
        public final synchronized void printcomma(Complex ff){
                this.output.print(ff.toString());
                this.output.print(",");
        }

        // Prints Phasor plus comma, no line return
        public final synchronized void printcomma(Phasor ff){
                this.output.print(ff.toString());
                this.output.print(",");
        }

        // Prints ErrorProp plus comma, no line return
        public final synchronized void printcomma(ErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(",");
        }

        // Prints ComplexErrorProp plus comma, no line return
        public final synchronized void printcomma(ComplexErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(",");
        }

        // Prints int plus comma, no line return
        public final synchronized void printcomma(int ii){
                this.output.print(ii);
                this.output.print(",");
        }

        // Prints long integer plus comma, no line return
        public final synchronized void printcomma(long ll){
                this.output.print(ll);
                this.output.print(",");
        }

        // Prints boolean plus comma, no line return
        public final synchronized void printcomma(boolean bb){
                this.output.print(bb);
                this.output.print(",");
        }

        // Prints short integer plus comma, no line return
        public final synchronized void printcomma(short ss){
                this.output.print(ss);
                this.output.print(",");
        }

        // Prints byte integer plus comma, no line return
        public final synchronized void printcomma(byte by){
                this.output.print(by);
                this.output.print(",");
        }

        // Prints comma, no line return
        public final synchronized void printcomma(){
                this.output.print(",");
        }

                // Prints array of doubles, each separated by a comma
        public final synchronized void printcomma(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of floats, each separated by a comma
        public final synchronized void printcomma(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of BigDecimal, each separated by a comma
        public final synchronized void printcomma(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print(",");
                }
        }

        // Prints array of BigInteger, each separated by a comma
        public final synchronized void printcomma(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print(",");
                }
        }

        // Prints array of long, each separated by a comma
        public final synchronized void printcomma(long[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of int, each separated by a comma
        public final synchronized void printcomma(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of short, each separated by a comma
        public final synchronized void printcomma(short[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of byte, each separated by a comma
        public final synchronized void printcomma(byte[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of char, each separated by a comma
        public final synchronized void printcomma(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of boolean, each separated by a comma
        public final synchronized void printcomma(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of Strings, each separated by a comma
        public final synchronized void printcomma(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of Complex, each separated by a comma
        public final synchronized void printcomma(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of Phasor, each separated by a comma
        public final synchronized void printcomma(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of ErrorProp, each separated by a comma
        public final synchronized void printcomma(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }

        // Prints array of ComplexErrorProp, each separated by a comma
        public final synchronized void printcomma(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(",");
                }
        }


        // Prints date and time (plus comma, no line return);
        public final synchronized void dateAndTimecomma(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(",");
        }

        // Prints file title (title), date and time (plus comma, no line return);
        public final synchronized void dateAndTimecomma(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(",");
        }

        // PRINT FOLLOWED BY A SEMICOLON, NO LINE RETURN
        // Prints character plus semicolon, no line return
        public final synchronized void printsc(char ch){
                this.output.print(ch);
                this.output.print(";");
        }

        // Prints string plus semicolon, no line return
        public final synchronized void printsc(String word){
                this.output.print(word + ";");
        }

        // Prints double plus semicolon, no line return
        public final synchronized void printsc(double dd){
                this.output.print(dd);
                this.output.print(";");
        }

        // Prints float plus semicolon, no line return
        public final synchronized void printsc(float ff){
                this.output.print(ff);
                this.output.print(";");
        }

        // Prints BigDecimal plus semicolon, no line return
        public final synchronized void printsc(BigDecimal big){
                this.output.print(big.toString());
                this.output.print(";");
        }

        // Prints BigInteger plus semicolon, no line return
        public final synchronized void printsc(BigInteger big){
                this.output.print(big.toString());
                this.output.print(";");
        }

        // Prints Complex plus semicolon, no line return
        public final synchronized void printsc(Complex ff){
                this.output.print(ff.toString());
                this.output.print(";");
        }

        // Prints Phasor plus semicolon, no line return
        public final synchronized void printsc(Phasor ff){
                this.output.print(ff.toString());
                this.output.print(";");
        }

        // Prints ErrorProp plus semicolon, no line return
        public final synchronized void printsc(ErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(";");
        }

        // Prints ComplexErrorProp plus semicolon, no line return
        public final synchronized void printsc(ComplexErrorProp ff){
                this.output.print(ff.toString());
                this.output.print(";");
        }

        // Prints int plus semicolon, no line return
        public final synchronized void printsc(int ii){
                this.output.print(ii);
                this.output.print(";");
        }

        // Prints long integer plus semicolon, no line return
        public final synchronized void printsc(long ll){
                this.output.print(ll);
                this.output.print(";");
        }

        // Prints short integer plus semicolon, no line return
        public final synchronized void printsc(short ss){
                this.output.print(ss);
                this.output.print(";");
        }

        // Prints byte integer plus semicolon, no line return
        public final synchronized void printsc(byte by){
                this.output.print(by);
                this.output.print(";");
        }

        // Prints boolean plus semicolon, no line return
        public final synchronized void printsc(boolean bb){
                this.output.print(bb);
                this.output.print(";");
        }

        // Prints  semicolon, no line return
        public final synchronized void printsc(){
                this.output.print(";");
        }

        // Prints array of doubles, each separated by a semicolon
        public final synchronized void printsc(double[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of floats, each separated by a semicolon
        public final synchronized void printsc(float[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of BigDecimal, each separated by a semicolon
        public final synchronized void printsc(BigDecimal[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print(";");
                }
        }

        // Prints array of BigInteger, each separated by a semicolon
        public final synchronized void printsc(BigInteger[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i].toString());
                    this.output.print(";");
                }
        }

        // Prints array of long, each separated by a semicolon
        public final synchronized void printsc(long[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of short, each separated by a semicolon
        public final synchronized void printsc(short[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of byte, each separated by a semicolon
        public final synchronized void printsc(byte[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of int, each separated by a semicolon
        public final synchronized void printsc(int[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of char, each separated by a semicolon
        public final synchronized void printsc(char[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of boolean, each separated by a semicolon
        public final synchronized void printsc(boolean[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of Strings, each separated by a semicolon
        public final synchronized void printsc(String[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of Complex, each separated by a semicolon
        public final synchronized void printsc(Complex[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of Phasor, each separated by a semicolon
        public final synchronized void printsc(Phasor[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of ErrorProp, each separated by a semicolon
        public final synchronized void printsc(ErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }

        // Prints array of ComplexErrorProp, each separated by a semicolon
        public final synchronized void printsc(ComplexErrorProp[] array){
                int n = array.length;
                for(int i=0; i<n; i++){
                    this.output.print(array[i]);
                    this.output.print(";");
                }
        }


        // Prints date and time (plus semicolon, no line return);
        public final synchronized void dateAndTimesc(){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(";");
        }

        // Prints file title (title), date and time (plus semicolon, no line return);
        public final synchronized void dateAndTimesc(String title){
                Date d = new Date();
                String day = DateFormat.getDateInstance().format(d);
                String tim = DateFormat.getTimeInstance().format(d);

                this.output.print("This file, "+title+", was created at ");
                this.output.print(tim);
                this.output.print(" on ");
                this.output.print(day);
                this.output.print(";");
        }

        // Close file
        public final synchronized void close(){
                this.output.close();
        }

        // Print a 2-D array of doubles to a text file, no file title provided
        public static void printArrayToText(double[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of doubles to a text file, file title provided
        public static void printArrayToText(String title, double[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of doubles to a text file, no file title provided
        public static void printArrayToText(double[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of doubles to a text file, file title provided
        public static void printArrayToText(String title, double[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of floats to a text file, no file title provided
        public static void printArrayToText(float[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of floats to a text file, file title provided
        public static void printArrayToText(String title, float[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of floats to a text file, no file title provided
        public static void printArrayToText(float[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of float to a text file, file title provided
        public static void printArrayToText(String title, float[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of BigDecimal to a text file, no file title provided
        public static void printArrayToText(BigDecimal[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of BigDecimal to a text file, file title provided
        public static void printArrayToText(String title, BigDecimal[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j].toString());
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of BigInteger to a text file, no file title provided
        public static void printArrayToText(BigInteger[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }

        // Print a 2-D array of BigInteger to a text file, file title provided
        public static void printArrayToText(String title, BigInteger[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j].toString());
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of BigDecimal to a text file, no file title provided
        public static void printArrayToText(BigDecimal[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of BigDecimal to a text file, file title provided
        public static void printArrayToText(String title, BigDecimal[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i].toString());
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of BigInteger to a text file, no file title provided
        public static void printArrayToText(BigInteger[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of BigInteger to a text file, file title provided
        public static void printArrayToText(String title, BigInteger[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i].toString());
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of ints to a text file, no file title provided
        public static void printArrayToText(int[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of ints to a text file, file title provided
        public static void printArrayToText(String title, int[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of ints to a text file, no file title provided
        public static void printArrayToText(int[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of int to a text file, file title provided
        public static void printArrayToText(String title, int[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of longs to a text file, no file title provided
        public static void printArrayToText(long[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of longs to a text file, file title provided
        public static void printArrayToText(String title, long[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }




        // Print a 1-D array of longs to a text file, no file title provided
        public static void printArrayToText(long[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of long to a text file, file title provided
        public static void printArrayToText(String title, long[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of shorts to a text file, no file title provided
        public static void printArrayToText(short[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of shorts to a text file, file title provided
        public static void printArrayToText(String title, short[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }

        // Print a 1-D array of shorts to a text file, no file title provided
        public static void printArrayToText(short[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of short to a text file, file title provided
        public static void printArrayToText(String title, short[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }


        // Print a 2-D array of bytes to a text file, no file title provided
        public static void printArrayToText(byte[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of bytes to a text file, file title provided
        public static void printArrayToText(String title, byte[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of bytes to a text file, no file title provided
        public static void printArrayToText(byte[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of byte to a text file, file title provided
        public static void printArrayToText(String title, byte[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }


        // Print a 2-D array of Strings to a text file, no file title provided
        public static void printArrayToText(String[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of Strings to a text file, file title provided
        public static void printArrayToText(String title, String[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of Strings to a text file, no file title provided
        public static void printArrayToText(String[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of String to a text file, file title provided
        public static void printArrayToText(String title, String[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }
         // Print a 2-D array of chars to a text file, no file title provided
        public static void printArrayToText(char[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of chars to a text file, file title provided
        public static void printArrayToText(String title, char[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of chars to a text file, no file title provided
        public static void printArrayToText(char[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of char to a text file, file title provided
        public static void printArrayToText(String title, char[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }
        // Print a 2-D array of booleans to a text file, no file title provided
        public static void printArrayToText(boolean[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of booleans to a text file, file title provided
        public static void printArrayToText(String title, boolean[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of booleans to a text file, no file title provided
        public static void printArrayToText(boolean[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of boolean to a text file, file title provided
        public static void printArrayToText(String title, boolean[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of Complex to a text file, no file title provided
        public static void printArrayToText(Complex[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }

        // Print a 2-D array of Complex to a text file, file title provided
        public static void printArrayToText(String title, Complex[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of Complex to a text file, no file title provided
        public static void printArrayToText(Complex[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of Complex to a text file, file title provided
        public static void printArrayToText(String title, Complex[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of Phasor to a text file, no file title provided
        public static void printArrayToText(Phasor[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }

        // Print a 2-D array of Phasor to a text file, file title provided
        public static void printArrayToText(String title, Phasor[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of Phasor to a text file, no file title provided
        public static void printArrayToText(Phasor[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of Phasor to a text file, file title provided
        public static void printArrayToText(String title, Phasor[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        // Print a 2-D array of ErrorProp to a text file, no file title provided
        public static void printArrayToText(ErrorProp[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of ErrorProp to a text file, file title provided
        public static void printArrayToText(String title, ErrorProp[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of ErrorProp to a text file, no file title provided
        public static void printArrayToText(ErrorProp[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of ErrorProp to a text file, file title provided
        public static void printArrayToText(String title, ErrorProp[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();

        }

        // Print a 2-D array of ComplexErrorProp to a text file, no file title provided
        public static void printArrayToText(ComplexErrorProp[][] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 2-D array of ComplexErrorProp to a text file, file title provided
        public static void printArrayToText(String title, ComplexErrorProp[][] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            int ncol = 0;
            for(int i=0; i<nrow; i++){
                ncol=array[i].length;
                for(int j=0; j<ncol; j++){
                    fo.printtab(array[i][j]);
                }
                fo.println();
            }
            fo.println("End of file.");
            fo.close();
        }


        // Print a 1-D array of ComplexErrorProp to a text file, no file title provided
        public static void printArrayToText(ComplexErrorProp[] array){
            String title = "ArrayToText.txt";
            printArrayToText(title, array);
        }


        // Print a 1-D array of ComplexErrorProp to a text file, file title provided
        public static void printArrayToText(String title, ComplexErrorProp[] array){
            FileOutput fo = new FileOutput(title, 'n');
            fo.dateAndTimeln(title);
            int nrow = array.length;
            for(int i=0; i<nrow; i++){
                fo.printtab(array[i]);
            }
            fo.println();
            fo.println("End of file.");
            fo.close();
        }

        private static String setField(String ss, int f){
             char sp =  ' ';
                int n = ss.length();
                if(f>n){
                    for(int i=n+1; i<=f; i++){
                        ss=ss+sp;
                    }
                }
                return ss;
        }

    // Delete a file
    public static void deleteFile(String fileName){

        // deletion flag
        boolean flag = true;

        // Create an instance of File representing the named file
        File file0 = new File(fileName);

        // Check that the file exists
        if(!file0.exists()){
            System.out.println("Method deleteFile: no file or directory of the name " + fileName + " found");
            flag = false;
        }

        // Check whether FileName is write protected
        if(!file0.canWrite()){
            System.out.println("Method deleteFile: " + fileName + " is write protected and cannot be deleted");
            flag = false;
        }

        // Check, if fileName is a directory, that it is empty
        if(file0.isDirectory()){
            String[] dirFiles = file0.list();
            if (dirFiles.length > 0){
                System.out.println("Method deleteFile: " + fileName + " is a directory which is not empty; no action was taken");
                flag = false;
            }
        }

        // Delete fileName
        boolean deleteDone = file0.delete();

        if (!deleteDone){
            System.out.println("Method deleteFile: deletion of the file " + fileName + " failed");
        }
    }

}
