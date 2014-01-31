/*
*   Class   FileChooser
*
*   Methods for selecting and opening for reading a file through a dialogue box
*   All folders and files may be displayed or a specific extension, e.g. txt,
*   may be set (the extension filter uses the class FileTypeFilter which is
*   the SUN JAVA filter, ExampleFileFilter, retitled)
*
*   This is a sub-class of FileInput from which it inherits all the read methods
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       17 July 2004
*   REVISED:    11 June 2005 - Made a subclass of FileInput
*               30 November 2005, 2 July 2006, 20 September 2006, 7 July 2008, 31 October 2010, 13 December 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/FileChooser.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2006 - 2010   Michael Thomas Flanagan
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

import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.*;

public class FileChooser extends FileInput{

    private File file;              // file fo be selected
    private String path = null;     // path to directory from which the file is selected
                                    //      e.g. "C:\\Java\\flanagan
                                    //      default (path=null) - current directory
    private String extn = null;     // file type extension of files to be displayed
                                    //      default (extn=null) - all file types displayed
    // constructor
    // opens home directory
    public FileChooser(){
        super();
        this.path = System.getProperty("user.dir");
    }

    // constructor
    // opens directory given by path
    public FileChooser(String path){
        super();
        this.path = path;
    }

    // use JFileChooser to select the required file
    // uses default prompt ("Select File")
    public String selectFile(){
        return this.selectFile("Select File");
    }

    // use a JFileChooser to select the required file
    // display user supplied prompt
    public String selectFile(String prompt){

        JFileChooser chooser = new JFileChooser(this.path);

        if(this.extn!=null){
            // Add filter
	        FileTypeFilter f = new FileTypeFilter();
            f.addExtension(extn);
            f.setDescription(extn + " files");
            chooser.setFileFilter(f);
        }
        else{
            // enable all files displayed option
            chooser.setAcceptAllFileFilterUsed(true);
        }

	    chooser.setDialogTitle(prompt);
	    chooser.showOpenDialog(null);
	    file = chooser.getSelectedFile();
	    if(file==null){
	        super.fileName = null;
	        super.stemName = null;
	        super.extension = null;
	        super.pathName = null;
	        super.dirPath = null;
	        super.fileFound=false;
	    }
	    else{
	        super.pathName = file.toString();
	        super.fileName = file.getName();
	        super.dirPath = (file.getParentFile()).toString();
	        int posDot = super.fileName.indexOf('.');
	        if(posDot==-1){
                super.stemName = super.fileName;
                super.extension = "";
            }
            else{
                super.stemName = super.fileName.substring(0, posDot);
                super.extension = super.fileName.substring(posDot);
                this.extn = super.extension;
            }

	        try{
                super.input = new BufferedReader(new FileReader(super.pathName));
            }catch(java.io.FileNotFoundException e){
                System.out.println(e);
                super.fileFound=false;
            }
	    }

	    return super.fileName;
	}

    // set path
    public void setPath(String path){
        this.path = path;
    }

    // get path
    public String getPath(){
        return this.path;
    }

    // set extension - display files with extension extn
    public void setExtension(String extn){
        this.extn = extn;
    }

     //  display all file extensions
    public void setAllExtensions(){
        this.extn = null;
    }

    // get extension used
    public String getExtension(){
        return this.extn;
    }

    // Displays dialogue box asking if you wish to exit program
    // Answering yes end program
    public static final synchronized void endProgram(){

        int ans = JOptionPane.showConfirmDialog(null, "Do you wish to end the program", "End Program", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(ans==0){
            System.exit(0);
        }
        else{
            JOptionPane.showMessageDialog(null, "Now you must press the appropriate escape key/s, e.g. Ctrl C, to exit this program");
        }
    }
}
