/*
*   Class PlotGraph
*
*   A class that creates a window and displays within that window
*   a graph of one or more x-y data sets
*
*   This class extends Plot (also from Michael Thomas Flanagan's Library)
*
*   For use if you are incorporating a plot into your own Java program
*   See Plotter for a free standing graph plotting application
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	 February 2002
*   UPDATED:  22 April 2004 and 14 August 2004, 7 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PlotGraph.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2008
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

package flanagan.plot;

// Include the windowing libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JFrame;
import java.io.Serializable;

// Declare a class that creates a window capable of being drawn to
public class PlotGraph extends Plot implements Serializable{

        protected static final long serialVersionUID = 1L;  // serial version unique identifier

    	protected int graphWidth = 800;     	// width of the window for the graph in pixels
    	protected int graphHeight = 600;    	// height of the window for the graph in pixels
    	protected int closeChoice = 1;    	    // =1 clicking on close icon causes window to close
                                  		        //    and the the program is exited.
                                  		        // =2 clicking on close icon causes window to close
                                  		        //    leaving the program running.
    	// Create the window object
    	protected JFrame window = new JFrame("Michael T Flanagan's plotting program - PlotGraph");

    	// Constructor
    	// One 2-dimensional data arrays
    	public PlotGraph(double[][] data){
        	super(data);
   	    }

   	    // Constructor
    	//Two 1-dimensional data arrays
    	public PlotGraph(double[] xData, double[] yData){
        	super(xData, yData);
   	    }

    	// Rescale the y dimension of the graph window and graph
    	public void rescaleY(double yScaleFactor)
    	{
        	this.graphHeight=(int)Math.round((double)graphHeight*yScaleFactor);
        	super.yLen=(int)Math.round((double)super.yLen*yScaleFactor);
        	super.yTop=(int)Math.round((double)super.yTop*yScaleFactor);
        	super.yBot=super.yTop + super.yLen;
    	}

    	// Rescale the x dimension of the graph window and graph
    	public void rescaleX(double xScaleFactor)
    	{
        	this.graphWidth=(int)Math.round((double)graphWidth*xScaleFactor);
        	super.xLen=(int)Math.round((double)super.xLen*xScaleFactor);
        	super.xBot=(int)Math.round((double)super.xBot*xScaleFactor);
        	super.xTop=super.xBot + super.xLen;
    	}

    	// Get pixel width of the PlotGraph window
    	public int getGraphWidth(){
        	return this.graphWidth;
    	}

    	// Get pixel height of the PlotGraph window
    	public int getGraphHeight(){
        	return this.graphHeight;
    	}

    	// Reset height of graph window (pixels)
    		public void setGraphHeight(int graphHeight){
        	this.graphHeight=graphHeight;
    	}

    	// Reset width of graph window (pixels)
     		public void setGraphWidth(int graphWidth){
        	this.graphWidth=graphWidth;
    	}

    	// Get close choice
    	public int getCloseChoice(){
        	return this.closeChoice;
    	}

    	// Reset close choice
    	public void setCloseChoice(int choice){
        	this.closeChoice = choice;
     	}

    	// The paint method to draw the graph.
    	public void paint(Graphics g){

        	// Rescale - needed for redrawing if graph window is resized by dragging
        	double newGraphWidth = this.getSize().width;
        	double newGraphHeight = this.getSize().height;
        	double xScale = newGraphWidth/(double)this.graphWidth;
        	double yScale = newGraphHeight/(double)this.graphHeight;
        	rescaleX(xScale);
        	rescaleY(yScale);

        	// Call graphing method
        	graph(g);
    	}

    	// Set up the window and show graph
    	public void plot(){
        	// Set the initial size of the graph window
        	setSize(this.graphWidth, this.graphHeight);

        	// Set background colour
        	window.getContentPane().setBackground(Color.white);

        	// Choose close box
        	if(this.closeChoice==1){
            		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	}
        	else{
            		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        	}

        	// Add graph canvas
        	window.getContentPane().add("Center", this);

        	// Set the window up
        	window.pack();
        	window.setResizable(true);
        	window.toFront();

        	// Show the window
        	window.setVisible(true);
    	}

        // Displays dialogue box asking if you wish to exit program
        // Answering yes end program - will simultaneously close the graph windows
        public void endProgram(){

                int ans = JOptionPane.showConfirmDialog(null, "Do you wish to end the program\n"+"This will also close the graph window or windows", "End Program", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(ans==0){
                    System.exit(0);
                }
                else{
                    String message = "Now you must press the appropriate escape key/s, e.g. Ctrl C, to exit this program\n";
                    if(this.closeChoice==1)message += "or close a graph window";
                    JOptionPane.showMessageDialog(null, message);
                }
        }

        // Return the serial version unique identifier
        public static long getSerialVersionUID(){
            return PlotGraph.serialVersionUID;
        }

}

