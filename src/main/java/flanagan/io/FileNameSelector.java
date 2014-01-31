/*
*   Class   FileNameSelector
*
*   Methods for selecting the name and path of a file for use in a program
*   The selected file is NOT opened
*
*   See FileChooser for a class that contains methods that select and OPEN a file
*
*   All folders and files may be displayed or a specific extension, e.g. txt,
*   may be set (the extension filter uses the class FileTypeFilter which is
*   the SUN JAVA filter, ExampleFileFilter, retitled)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       13 September 2005
*   REVISED:    30 November 2005
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/FileNameSelector.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) September 2005
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

public class FileNameSelector{

    private File file;              // file whose name is to be selected
    private String path = null;     // path to directory from which the file is selected
                                    //      e.g. "C:\\Java\\flanagan
                                    //      default (path=null) - home directory
    private String extn = null;     // file type extension of files to be displayed
                                    //      default (extn=null) - all file types displayed
    private String fileName = null; // selected file name
    private String stemName = null; // selected file name without its extension
	private String pathName = null; // selected file name path name
	private String dirPath = null;  // path to directory containing selectede file name
	private boolean fileFound=false;// true if file named is found

    // constructor
    // opens home directory
    public FileNameSelector(){
    }

    // constructor
    // opens directory given by path
    public FileNameSelector(String path){
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
	        this.fileName = null;
	        this.stemName = null;
	        this.pathName = null;
	        this.dirPath = null;
	        this.fileFound=false;
	    }
	    else{
	        this.pathName = file.toString();
	        this.fileName = file.getName();
	        this.dirPath = (file.getParentFile()).toString();
	        int posDot = this.fileName.indexOf('.');
	        if(posDot==-1){
                this.stemName = this.fileName;
            }
            else{
                this.stemName = this.fileName.substring(0, posDot);
            }
	    }

	    return this.fileName;
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

    // get extension
    public String getExtension(){
        return this.extn;
    }

    // Get file path
    public String getPathName(){
        return this.pathName;
    }

    // Get file name
    public String getFileName(){
        return this.fileName;
    }

    // Get file name without its extension
    public String getStemName(){
        return this.stemName;
    }

    // Get path to directory containing the file
    public String getDirPath(){
        return this.dirPath;
    }

    // Get the file existence status, fileFound.
    public boolean fileFound(){
        return fileFound;
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
