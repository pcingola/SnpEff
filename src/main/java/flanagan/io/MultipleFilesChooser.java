/*
*   Class   MultipleFilesChooser
*
*   Methods for selecting and opening for reading several files through a dialogue box
*   All folders and files may be displayed or a specific extension, e.g. txt,
*   may be set (the extension filter uses the class FileTypeFilter which is
*   the SUN JAVA filter, ExampleFileFilter, retitled)
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       28 November 2005
*   AMENDED:    11 August 2006, 31 October 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/MultipleFilesChooser.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2005 - 2010
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
import flanagan.io.FileInput;

public class MultipleFilesChooser{

    private String[] fileNames = null;      // file names selected
    private String[] pathNames = null;      // path names selected
    private String[] dirNames = null;       // directory path of files selected
    private String[] stemNames = null;      // file names minus the extension

    private FileInput[] fileObjects = null; // instances of the FileInput object corresponding to each File selected
    private int nFiles = 0;                 // Number of files selected
    private String path = null;             // path to directory from which the files are selected
                                            //      e.g. "C:\\Java\\flanagan
                                            //      default (path=null) - current directory
    private String extn = null;             // file type extension of files to be displayed
                                            //      default (extn=null) - all file types displayed
    // constructor
    // opens home directory
    public MultipleFilesChooser(){
        this.path = System.getProperty("user.dir");
    }

    // constructor
    // opens directory given by path
    public MultipleFilesChooser(String path){
        this.path = path;
    }

    // use JFileChooser to select the required file
    // uses default prompt ("Select File")
    public FileInput[] selectFiles(){
        return this.selectFiles("Select File");
    }

    // use a JFileChooser to select the required file
    // display user supplied prompt
    public FileInput[] selectFiles(String prompt){
        JFileChooser chooser = new JFileChooser(this.path);
        chooser.setMultiSelectionEnabled(true);
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
	    File[] files = chooser.getSelectedFiles();
	    this.nFiles = files.length;
        this.fileObjects = new FileInput[nFiles];
        this.fileNames = new String[nFiles];
        this.stemNames = new String[nFiles];
        this.pathNames = new String[nFiles];
        this.dirNames = new String[nFiles];

	    for(int i=0; i<nFiles; i++){
	        this.fileNames[i] = files[i].getName();
	        this.pathNames[i] = files[i].toString();
	        this.dirNames[i]  = (files[i].getParentFile()).toString();
	        this.fileObjects[i] = new FileInput(this.pathNames[i]);
	        int posDot = this.fileNames[i].indexOf('.');
	        if(posDot==-1){
                this.stemNames[i] = this.fileNames[i];
            }
            else{
                this.stemNames[i] = this.fileNames[i].substring(0, posDot);
            }
	    }

	    return this.fileObjects;
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

    // get number of files selected
    public int getNumberOfFiles(){
        return this.nFiles;
    }

    // get file names
    public String[] getFileNames(){
        return this.fileNames;
    }

    // get file names without the extensions
    public String[] getStemNames(){
        return this.stemNames;
    }

    // get file paths
    public String[] getPathNames(){
        return this.pathNames;
    }

    // get file directories
    public String[] getDirPaths(){
        return this.dirNames;
    }

    // close all files that have been opened
    public final synchronized void close(){
        for(int i=0; i<this.nFiles; i++){
            this.fileObjects[i].close();
        }
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
