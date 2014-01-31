/*
*   Class   FileInput
*
*   Methods for entering doubles, floats, BigDecimal,
*   integers, long integers, short integers, bytes,
*   booleans, Complexes, Phasors, lines (as String),
*   words (as String) and chars from a text file.
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       July 2002
*   REVISED:    25 July 2004, 11 June 2005, 13 September 2005, 30 November 2005, 1 July 2006
*               20 September 2006, 27 June 2007, 21-23 July 2007, 26 February 2008, 4 April 2008
*               7 July 2008, 8 July 2008, 2 February 2009, 13 November 2010, 13 December 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/FileInput.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2010 Michael Thomas Flanagan
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
import java.util.ArrayList;

import flanagan.complex.Complex;
import flanagan.circuits.Phasor;

public class FileInput{

        // Instance variables
        protected String fileName = " ";            //input file name
        protected String stemName = " ";            //input file name without its extension
        protected String extension = " ";           //input file name extension
        protected String pathName = " ";            //input file path name
        protected String dirPath = " ";             //path to directory containing input file
        protected String fullLine = " ";            //current line in input file
        protected String fullLineT = " ";           //current line in input file trimmed of trailing spaces
        protected BufferedReader input = null;      //instance of BufferedReader
        protected boolean testFullLine = false;     //false if fullLine is empty
        protected boolean testFullLineT = false;    //false if fullLineT is empty
        protected boolean eof = false;              //true if reading beyond end of file attempted
        protected boolean fileFound = true;         //true if file named is found
        protected boolean inputType = false;        //false in input type is a String with inclusion of punctuation
                                                    //true if input type is numeric or separated char, i.e. double, float, int, long, char
        protected boolean charType = false;         //true if input type is a separated char
        protected boolean space = true;             //if true -  a space is treated as a delimiter in reading a line of text
        protected boolean supressMessage = false;   //if true -  read beyond end of file message suppressed
        protected boolean wordMethod = false;       // true if readWord called
        protected String holdingWord = "HoldingWordMLPYGV";    // word used to hold an identified space that needs retaining on calling readWord

        // Constructor
        // Constructor to enable sub-class FileChooser to function
        public FileInput(){
        }

        // constructor for instances of this class
        public FileInput(String pathName){
                this.pathName = pathName;
                int posSlash = pathName.indexOf("//");
                int posBackSlash = pathName.indexOf("\\");
                if(posSlash!=-1 || posBackSlash!=-1){
                    File file = new File(this.pathName);
                    this.fileName = file.getName();
                    this.dirPath = (file.getParentFile()).toString();
                }
                int posDot = this.fileName.indexOf('.');
	            if(posDot==-1){
                    this.stemName = this.fileName;
                }
                else{
                    this.stemName = this.fileName.substring(0, posDot);
                    this.extension = this.fileName.substring(posDot);
                }

                try{
                        this.input = new BufferedReader(new FileReader(this.pathName));
                }catch(java.io.FileNotFoundException e){
                        System.out.println(e);
                        fileFound=false;
                }
        }

        // Methods

        // Get file path
        public String getPathName(){
            return this.pathName;
        }

        // Get file name
        public String getFileName(){
            return this.fileName;
        }

        // get file name without the extension
        public String getStemName(){
            return this.stemName;
        }

        // get file name extension
        public String getExtension(){
            return this.extension;
        }

        // Get path to directory containing the file
        public String getDirPath(){
            return this.dirPath;
        }

        // Removes a space to the list of delimiters on reading a line of text
        public void removeSpaceAsDelimiter(){
            this.space = false;
        }

        // Restores a space from the list of delimiters on reading a line of text
        public void restoreSpaceAsDelimiter(){
            this.space = true;
        }

        // Copy the file
        public final synchronized void copy(String copyFilename){
            FileOutput fout = new FileOutput(copyFilename);
            int nLines = this.numberOfLines();
            for(int i=0; i<nLines; i++){
                String cline = this.readLine();
                fout.println(cline);
            }
            fout.close();
        }

        // Reads a double from the file
        public final synchronized double readDouble(){
                this.inputType = true;
                String word="";
                double dd=0.0D;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)dd = Double.parseDouble(word.trim());

                return dd;
        }

        // Reads a float from the file
        public final synchronized float readFloat(){
                this.inputType = true;
                String word="";
                float ff=0.0F;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ff = Float.parseFloat(word.trim());

                return ff;
        }


        // Reads a BigDecimal from the file
        public final synchronized BigDecimal readBigDecimal(){
                this.inputType = true;
                String word="";
                BigDecimal big = null;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)big = new BigDecimal(word.trim());

                return big;
        }

        // Reads an integer (int) from the file
        public final synchronized int readInt(){
                this.inputType = true;
                String word="";
                int ii=0;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ii = Integer.parseInt(word.trim());

                return ii;
        }

        // Reads a long integer from the file
        public final synchronized long readLong(){
                this.inputType = true;
                String word="";
                long ll=0L;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ll = Long.parseLong(word.trim());

                return ll;
        }

        // Reads a BigInteger from the file
        public final synchronized BigInteger readBigInteger(){
                this.inputType = true;
                String word="";
                BigInteger big = null;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)big = new BigInteger(word.trim());

                return big;
        }

        // Reads a short integer from the file
        public final synchronized short readShort(){
                this.inputType = true;
                String word="";
                short ss=0;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ss = Short.parseShort(word.trim());

                return ss;
        }

        // Reads a byte integer from the file
        public final synchronized byte readByte(){
                this.inputType = true;
                String word="";
                byte bb=0;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                    if(!eof)bb = Byte.parseByte(word.trim());

                return bb;
        }

        // Reads a Complex from the file
        // accepts strings 'real''sign''x''imag' and 'real''sign''x''.''imag'
        // where x may be i or j
        // and sign may be + or -
        // e.g.  2+j3, 2+i3,
        // no spaces are allowed within the complex number
        // e.g.  2 + j3, 2 + i3 are NOT allowed
        public final synchronized Complex readComplex(){

                this.inputType = true;
                String word="";
                Complex cc = null;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)cc = Complex.parseComplex(word.trim());
                return cc;
        }

        // Reads a Phasor from the file
        // accepts strings 'magnitude'<'phase', 'magnitude'<'phase'deg, 'magnitude'<'phase'rad
        // e.g. 1.23<34.1deg, -0.67<-56.7, 6.8e2<-0.22rad
        public final synchronized Phasor readPhasor(){

                this.inputType = true;
                String word="";
                Phasor ph = null;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)ph = Phasor.parsePhasor(word.trim());
                return ph;
        }

        // Reads a boolean from the file
        public final synchronized boolean readBoolean(){

                boolean retB = true;
                String retS = this.readWord();
                if(retS.equals("false") || retS.equals("FALSE")){
                    retB = false;
                }
                else{
                    if(retS.equals("true") || retS.equals("TRUE")){
                        retB = true;
                    }
                    else{
                        throw new IllegalArgumentException("attempted input neither true nor false");
                    }
                }
                return retB;
        }

        // Reads a word (a string between all separators) from the file
        public final synchronized String readWord(){
                this.inputType = true;
                this.wordMethod = true;
                String word="";

                if(!this.testFullLineT) this.enterLine();
                if(this.fullLine.equals("")){
                    word=" ";
                }else
                {
                    word = nextWord();
                    if(word.equals(""))word=" ";
                    if(word.trim().equals(this.holdingWord))word=" ";
                }

                return word;
        }

        // Reads a word (a string between spaces) from the file
        public final synchronized String readWordSaceOnly(){
                this.inputType = false;
                String word="";

                if(!this.testFullLineT) this.enterLine();
                if(this.fullLine.equals("")){
                    word="";
                }else
                {
                    word = nextWord();
                    if(word.equals(""))word=" ";
                }

                return word;
        }

        // Public method for reading a line from the file
        public final synchronized String readLine(){
            this.inputType = false;
            return this.readLineL();
        }

        // Protected method for reading a line from the file
        protected final synchronized String readLineL(){
                String line="";
                try{
                        line = input.readLine();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
               if(line==null){
                    if(!this.supressMessage)System.out.println("Attempt to read beyond the end of the file");
                    eof=true;
                    line="";
                }
                return line;
        }

        // Reads a character, preceeded and followed by space, comma, colon or comma, from the file
        public final synchronized char readChar(){
                inputType = true;
                charType = true;
                String word="";
                char ch=' ';

                if(!this.testFullLine) this.enterLine();
                word = nextWord();
                if(word.length()!=1)throw new IllegalArgumentException("attempt to read more than one character into type char");
                if(!eof)ch = word.charAt(0);
                return ch;
        }

        // Close file
        public final synchronized void close(){
            if(fileFound){
                try{
                        input.close();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
            }
        }

        // Get the end of line status.
        public boolean eol(){
            boolean eol = false;
            if(!this.testFullLineT)eol = true;
            return eol;
        }

        // Get the end of file status, eof.
        public boolean eof(){
            return eof;
        }

        // Get the file existence status, fileFound.
        public boolean fileFound(){
            return fileFound;
        }

        // enters a line from the file into the fullLine and fullLineT strings
        protected final synchronized void enterLine(){
                int i=0;

                this.fullLine=this.readLineL();

                // check for readWord reading allowed spaces
                if(this.wordMethod)this.checkWordSpaces();

                this.fullLineT=this.fullLine;
                if(!this.fullLine.equals("")){
                    i=this.fullLineT.length()-1;
                    while(this.fullLineT.charAt(i)==' ' && i>=0){
                            this.fullLineT=this.fullLineT.substring(0,i);
                            i--;
                    }
                }
        }

       // Check for space/s only or "" between non-space separators if method readWord called
       // substitute holding word if found
       public void checkWordSpaces(){

                int len=this.fullLine.length();

                // Check for space/s only or "" between non-space separators if method readWord called
                // substitute holding word if found
                if(this.wordMethod){
                    boolean test3 = false;
                    ArrayList<Integer> al1 = new ArrayList<Integer>();
                    ArrayList<Integer> al2 = new ArrayList<Integer>();
                    for(int i=0; i<len; i++){
                        test3 = false;
                        if(this.fullLine.charAt(i)=='\t')test3=true;
                        if(this.fullLine.charAt(i)==',')test3=true;
                        if(this.fullLine.charAt(i)==':')test3=true;
                        if(this.fullLine.charAt(i)==';')test3=true;
                        if(test3)al1.add(new Integer(i));
                    }
                    int nal1 = al1.size();

                    if(nal1>0){
                        boolean test4 = true;
                        boolean test5 = true;
                        boolean test6 = false;
                        int ii = 0;
                        int jj = 0;
                        int kk = 0;
                        while(test4){
                            test5 = true;
                            test6 = false;
                            jj =((Integer)al1.get(ii)).intValue();
                            if(jj==0){
                                test6 = true;
                            }
                            else{
                                if(jj==len-1){
                                    test6 = true;
                                    test4 = false;
                                }
                                else{
                                    if(ii==nal1-1){
                                        test5 = true;
                                        for(int i=jj+1; i<len; i++){
                                            if(this.fullLine.charAt(i)!=' '){
                                                test5 = false;
                                            }
                                            if(!test5)break;
                                        }
                                        if(test5)test6 = true;
                                        test4 = false;

                                    }
                                    else{
                                        kk  =((Integer)al1.get(ii+1)).intValue();
                                        test5 = true;
                                        for(int i=jj+1; i<kk; i++){
                                            if(this.fullLine.charAt(i)!=' '){
                                                test5 = false;
                                            }
                                            if(!test5)break;
                                        }
                                        if(test5)test6 = true;
                                        ii++;
                                    }
                                }
                            }
                            if(test6){
                                al2.add(new Integer(jj+1));
                            }
                            if(ii>=nal1)test4 = false;
                        }

                        int nal2 = al2.size();
                        if(nal2>0){
                            for(int i=nal2-1; i>=0; i--){
                                int mm = ((Integer)al2.get(i)).intValue();
                                if(mm>=len-1){
                                    this.fullLine += this.holdingWord;
                                }
                                else{
                                    if(mm==1){
                                        this.fullLine = this.holdingWord + this.fullLine;
                                    }
                                    else{
                                        String sub1 = this.fullLine.substring(0,mm);
                                        String sub2 = this.fullLine.substring(mm);
                                        this.fullLine = sub1 + this.holdingWord + sub2;
                                    }
                                }
                            }
                        }
                    }
                }
        }

        // reads the next word (a string between delimiters) from the String fullLine
        protected final synchronized String nextWord(){

                this.testFullLine=true;
                this.testFullLineT=true;
                String  word = "";
                int     posspa=-1, postab=-1, possp=-1, poscom=-1, poscol=-1, possem=-1;
                boolean test = true;
                int len=this.fullLine.length();


                // strip end of the word of any spaces, tabs or, if numerical input, commas, colons or semicolons
                boolean test0 = true;
                boolean test1 = false;
                int pend =this.fullLine.length();
                while(test0){
                    pend--;
                    if(this.fullLine.charAt(pend)==' ')test1=true;
                    if(this.fullLine.charAt(pend)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(pend)==',')test1=true;
                        if(this.fullLine.charAt(pend)==':')test1=true;
                        if(this.fullLine.charAt(pend)==';')test1=true;
                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(0,pend);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // strip front of the word of any leading spaces, tabs or, if numerical input, commas, colons or semicolons
                test0 = true;
                test1 = false;
                while(test0){
                    if(this.fullLine.charAt(0)==' ')test1=true;
                    if(this.fullLine.charAt(0)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(0)==',')test1=true;
                        if(this.fullLine.charAt(0)==':')test1=true;
                        if(this.fullLine.charAt(0)==';')test1=true;

                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(1);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // find first space (if space allowed as delimiter), tab or, if numeric, comma, colon or semicolon
                int lenPlus = this.fullLine.length() + 10;
                if(this.space)posspa=this.fullLine.indexOf(' ');
                postab=this.fullLine.indexOf('\t');
                int firstMin = lenPlus;
                int secondMin = lenPlus;
                int thirdMin = lenPlus;
                if(this.space){
                    if(posspa==-1 && postab==-1){
                        firstMin = lenPlus;
                    }
                    else{
                        if(posspa==-1){
                            firstMin = postab;
                        }
                        else{
                            if(postab==-1){
                                firstMin = posspa;
                            }
                            else{
                                firstMin = Math.min(posspa, postab);
                            }
                        }
                    }
                }
                else{
                    if(postab!=-1){
                        firstMin = postab;
                    }
                }
                if(this.inputType){
                    poscom=this.fullLine.indexOf(',');
                    poscol=this.fullLine.indexOf(':');
                    possem=this.fullLine.indexOf(';');
                    if(poscom==-1 && poscol==-1){
                        secondMin = lenPlus;
                    }
                    else{
                        if(poscom==-1){
                            secondMin = poscol;
                        }
                        else{
                            if(poscol==-1){
                                secondMin = poscom;
                            }
                            else{
                                secondMin = Math.min(poscom, poscol);
                            }
                        }
                    }
                    if(possem==-1){
                        thirdMin = lenPlus;
                    }
                    else{
                        thirdMin = possem;
                    }
                    secondMin = Math.min(secondMin, thirdMin);
                    firstMin = Math.min(firstMin, secondMin);

                }


                // remove first word first word from string
                if(firstMin==lenPlus){
                        word=this.fullLine;
                        this.fullLine="";
                        this.testFullLine=false;
                }
                else{
                        word=this.fullLine.substring(0,firstMin);

                        if(firstMin+1>this.fullLine.length()){
                                this.fullLine="";
                                this.testFullLine=false;
                        }
                        else{
                                this.fullLine=this.fullLine.substring(firstMin+1);
                                if(this.fullLine.length()==0)this.testFullLine=false;
                        }
                }
                if(this.testFullLineT){
                        if(!this.testFullLine){
                                this.testFullLineT=false;
                                this.fullLineT="";
                         }
                         else{
                                if(firstMin+1>this.fullLineT.length()){
                                        this.fullLineT="";
                                        this.testFullLineT=false;
                                }
                        }
                }

                // return first word of the supplied string
                return word;
        }

        // reads the next char from the String fullLine
        protected final synchronized char nextCharInString(){
                this.testFullLine=true;
                char  ch=' ';
                boolean test = true;

                ch=this.fullLine.charAt(0);
                this.fullLine=this.fullLine.substring(1);
                if(this.fullLine.length()==0)this.testFullLine=false;
                if(this.testFullLineT){
                        this.fullLineT=this.fullLineT.substring(1);
                        if(this.fullLineT.length()==0)this.testFullLineT=false;
                }

                return ch;
        }

        // set supressMessage to true
        public void setSupressMessageToTrue(){
            this.supressMessage = true;
        }

        // set supressMessage to false
        public void setSupressMessageToFalse(){
            this.supressMessage = false;
        }

        // Returns the number of lines in the file
        public final synchronized int numberOfLines(){
            FileInput fin = new FileInput(this.pathName);
            fin.setSupressMessageToTrue();
            boolean test = true;
            int nLines = 0;
            while(test){
                String inputLine = fin.readLineL();
                if(fin.eof){
                    test = false;
                }
                else{
                    if(nLines==Integer.MAX_VALUE){
                        System.out.println("Class FileInput; method numberOfLines; The number of lines is greater than the maximum integer value, " + Integer.MAX_VALUE);
                        System.out.println("-1 returned");
                        nLines = -1;
                    }
                    else{
                        nLines++;
                    }
                }
            }
            fin.close();
            return nLines;
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
