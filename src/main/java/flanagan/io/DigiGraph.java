/*
*   Class   DigiGraph
*
*   Class to digitize a graph presented as a gif, jpg or png
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	 September 2006
*   UPDATE:  8 October 2006, 2 November 2006, 12 May 2008, 5 July 2008, 3 December 2008
*
*   DOCUMENTATION:
*   See Michael T Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/DigiGraph.html
*
*   Copyright (c) 2006 - 2008
*
*   PERMISSION TO COPY:
*
*   Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
*   provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
*   and associated documentation or publications.
*
*   Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice,
*   this list of conditions and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
*   Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
*   the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission
*   from the Michael Thomas Flanagan:
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
*   Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
*   or its derivatives.
*
*****************************************************************************************************************************************************/

package flanagan.io;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.JFrame;

import flanagan.io.*;
import flanagan.interpolation.CubicSpline;
import flanagan.math.Fmath;
import flanagan.plot.PlotGraph;

public class DigiGraph extends Canvas implements MouseListener{

    private Image pic = null;               // image containing graph to be digitised
    private String imagePath = null;        // path, i.e. address\name, of the .png, .gif or .jpg containing graph
    private String imageName = null;        // name of the .gif, .png or .jpg file containing graph
    private String extension = null;        // extension of file name, e.g.  gif, png or jpg

    private String outputFile = null;       // output file (containing digitisation values) name
    private FileOutput fout = null;         // output file (containing digitisation values) reference
    private int trunc = 16;                 // number of decimal places in output data

    private String path = "C:";             // path for file selection window
    private int windowWidth = 0;     	    // width of the window for the graph in pixels
    private int windowHeight = 0;    	    // height of the window for the graph in pixels
    private int closeChoice = 1;    	    // =1 clicking on close icon causes window to close
                                 		    //    and the the program is exited.
                                  		    // =2 clicking on close icon causes window to close
                                            //    leaving the program running.

    private int xPos = 0;                   // mouse x-axis position (in pixels)on last click
    private int yPos = 0;                   // mouse y-axis position (in pixels)on last click
    private int button = 0;                 // mouse button last clicked
                                            // = 0; no button clicked
                                            // = 1; left mouse button last clicked
                                            // (= 2; middle mouse button last clicked)
                                            // = 3; right mouse button last clicked
    private int sumX = 0;                   // sum of xPos in calculation of a calibration point
    private int sumY = 0;                   // sum of yPos in calculation of a calibration point
    private int iSum = 0;                   // number of xPos and yPos in calculation of a calibration point
    private boolean mouseEntered = false;   //  = true when mouse enters object
                                            //  = false when mouse leaves object
    private double lowYvalue = 0.0;         // Y-axis value (entered as double) of the clicked low Y-axis value
    private double lowYaxisXpixel = 0.0;    // X-axis pixel number of the clicked known low Y-axis value
    private double lowYaxisYpixel = 0.0;    // Y-axis pixel number of the clicked known low Y-axis value
    private double highYvalue = 0.0;        // Y-axis value (entered as double) of the clicked high Y-axis value
    private double highYaxisXpixel = 0.0;   // X-axis pixel number of the clicked known high Y-axis value
    private double highYaxisYpixel = 0.0;   // Y-axis pixel number of the clicked known high Y-axis value
    private double lowXvalue = 0.0;         // X-axis value (entered as double) of the clicked low X-axis value
    private double lowXaxisXpixel = 0.0;    // X-axis pixel number of the clicked known low X-axis value
    private double lowXaxisYpixel = 0.0;    // Y-axis pixel number of the clicked known low X-axis value
    private double highXvalue = 0.0;        // X-axis value (entered as double) of the clicked high X-axis value
    private double highXaxisXpixel = 0.0;   // X-axis pixel number of the clicked known high X-axis value
    private double highXaxisYpixel = 0.0;   // Y-axis pixel number of the clicked known high X-axis value

    private ArrayList<Integer> xAndYvalues = new ArrayList<Integer>();
                                            // ArrayList holding clicked point xPos and yPos values
    private int iCounter = 0;               // counter for clicks or sum of clicks on first for calibration points
    private double angleXaxis = 0.0;        // clockwise angle from normal of x-axis (degrees)
    private double angleYaxis = 0.0;        // clockwise angle from normal of y-axis (degrees)
    private double angleMean = 0.0;         // mean clockwise angle of axes from normal (degrees)
    private double angleTolerance = 0.0;    // tolerance in above angle before a rotation of all points performed
                                            //  default option is to rotate if angle is not zero
    private boolean rotationDone = false;   // = false: no rotation of points performed
                                            // = true:  all points have been rotated

    private double[] xPosPixel = null;      // x pixel values converted to double
    private double[] yPosPixel = null;      // y pixel values converted to double
    private double[] xPositions = null;     // Digitized and scaled x values
    private double[] yPositions = null;     // Digitized and scaled y values
    private int nData = 0;                  // Number of points digitized (excluding calibration points)

    private int nInterpPoints = 0;          // Nnumber of interpolation points
    private boolean interpOpt = false;      // = true if interpolation requested
    private double[] xInterp = null;        // Interpolated x values
    private double[] yInterp = null;        // Interpolated y values
    private boolean plotOpt = true;         // = false if plot of interpolated data not required

    private boolean noIdentical = true;     // = true - all identical points stripped to one instance of the identical points
                                            // = false - all identical points retained

    private int imageFormat = 0;            // = 0 no image file loaded
                                            // = 1 GIF format
                                            // = 2 JPEG format
                                            // = 3 PNG format

    private boolean digitizationDone = false;   // = true when digitization complete

    private boolean noYlow = true;          // = false when lower y-axis calibration point has been entered
    private boolean noXlow = true;          // = false when lower x-axis calibration point has been entered
    private boolean noYhigh = true;         // = false when higher y-axis calibration point has been entered
    private boolean noXhigh = true;         // = false when higher x-axis calibration point has been entered

    private boolean resize = false;         // = true if image is resized

    // Create the window object
    private JFrame window = new JFrame("Michael T Flanagan's digitizing program - DigiGraph");

    // Constructors
    // image to be selected from a file select window
    // window opens on default setting
    public DigiGraph(){
        super();

        // Set graph digitizing window size
        setWindowSize();

        // select image
        selectImage();

        // set image
        setImage();

        // Name outputfile
        outputFileChoice();

        // Add the MouseListener
        addMouseListener(this);
    }

    // image to be selected from a file select window
    // window opens on path (windowPath) provided
    public DigiGraph(String windowPath){
        super();

        // Set graph digitizing window size
        setWindowSize();

        // Set window path
        this.path = windowPath;

        // select image
        selectImage();

        // set image
        setImage();

        // Name outputfile
        outputFileChoice();

        // Add the MouseListener
        addMouseListener(this);
    }

    // Set graph digitizing window size
    private void setWindowSize(){

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();     // get computer screen size
        this.windowWidth = screenSize.width - 30;     	                        // width of the window for the graph in pixels
        this.windowHeight = screenSize.height - 40;     	                    // height of the window for the graph in pixels
    }

    // Select graph image
    private void selectImage(){

        // Identify computer
        String computerName = null;
        try{
            InetAddress localaddress = InetAddress.getLocalHost();
            computerName = localaddress.getHostName();
        }
        catch(UnknownHostException e){
            System.err.println("Cannot detect local host : " + e);
        }

        // Set path to file selection window
        // Replace "name" by your "computer's name" and C:\\DigiGraphDirectory by the path to the directory containing the image to be digitized
        // Default path is to the C:\ directory or system default directory if no C drive present
        if(computerName.equals("name"))this.path =  "C:\\DigiGraphDirectory";

        // select image file
        FileChooser fc = new FileChooser(this.path);
        this.imageName = fc.selectFile();
        if(!fc.fileFound()){
            System.out.println("Class DigiGraph: No successful selection of an image file occurred");
            System.exit(0);
        }
        this.imagePath = fc.getPathName();

        int lastDot = this.imagePath.lastIndexOf('.');
        this.extension =  this.imagePath.substring(lastDot+1);
        if(this.extension.equalsIgnoreCase("gif"))imageFormat=1;
        if(this.extension.equalsIgnoreCase("jpg"))imageFormat=2;
        if(this.extension.equalsIgnoreCase("jpeg"))imageFormat=2;
        if(this.extension.equalsIgnoreCase("jpe"))imageFormat=2;
        if(this.extension.equalsIgnoreCase("jfif"))imageFormat=2;
        if(this.extension.equalsIgnoreCase("png"))imageFormat=3;
    }

    // Set graph image
    private void setImage(){
        this.pic = Toolkit.getDefaultToolkit().getImage(this.imagePath);
    }

    // Name outputfile and set number of decimal placess in the output data
    private void outputFileChoice(){
        int posdot = this.imagePath.lastIndexOf('.');
        this.outputFile = this.imagePath.substring(0, posdot) + "_digitized.txt";
        this.outputFile = Db.readLine("Enter output file name ", this.outputFile);
        this.fout = new FileOutput(this.outputFile);
        this.trunc = Db.readInt("Enter number of decimal places required in output data ", this.trunc);
    }

    // Reset the number of decimal places in the output data
    public void setTruncation(int trunc){
        this.trunc = trunc;
    }

    // Reset tolerance in axis rotation before applying rotation (degrees)
    public void setRotationTolerance(double tol){
        this.angleTolerance = tol;
    }

    // Reset option of plotting the data
    // Prevents a plot of the digitized data and the interpolated data, if interpolation optiion chosen,
    // from being displayed
    public void noPlot(){
        this.plotOpt = false;;
    }

     // Reset path for selection window
    public void setPath(String path){
        this.path = path;
    }

    // Reset height of graph window (pixels)
    public void setWindowHeight(int windowHeight){
        this.windowHeight = windowHeight;
    }

    // Reset width of graph window (pixels)
    public void setWindowWidth(int windowWidth){
        this.windowWidth = windowWidth;
    }

    // Reset close choice
    public void setCloseChoice(int choice){
        this.closeChoice = choice;
    }

    // Reset stripping of identical points option
    // Keep all identical points
    public void keepIdenticalPoints(){
        this.noIdentical = false;
    }

    // The paint method to display the graph.
    public void paint(Graphics g){

        // Call graphing method
        graph(g);
    }

    // Set up the window, show graph and digitize
    public void digitize(){

        // Set the initial size of the graph window
        this.window.setSize(this.windowWidth, this.windowHeight);

        // Set background colour
        this.window.getContentPane().setBackground(Color.white);

        // Choose close box
        if(this.closeChoice==1){
            this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        else{
            this.window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }

        // Add graph canvas
        this.window.getContentPane().add("Center", this);

        // Set the window up
        this.window.pack();
        this.window.setResizable(true);
        this.window.toFront();

        // Show the window
        this.window.setVisible(true);
    }


    // Set up the window, show graph and digitize (alternate spelling)
    public void digitise(){
        this.digitize();
    }


    // Display graph and get coordinates
    private void graph(Graphics g){

        // Display graph to be digitized
        g.drawImage(this.pic, 10, 30, this);
        if(!this.resize){
            g.drawString("RIGHT click anywhere on the screen", 5, 10);
            int width = this.pic.getWidth(null);
            int height = this.pic.getHeight(null);
            System.out.println(width + " xxx " + height);
            g.drawString("  ", 5, 10);
            double factor = (double)(windowHeight-30)/(double)height;
            if((int)(width*factor)>(windowWidth-10))factor = (double)(windowWidth-10)/(double)width;
            height = (int)((height-30)*factor*0.95);
            width = (int)((width-10)*factor+0.95);
            this.pic = this.pic.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            g.drawImage(this.pic, 10, 30, this);
            this.resize=true;
        }

        // Displays cross at pixel coordinates clicked
        boolean test=true;
        if(this.xPos==0 && this.yPos==0)test=false;
        if(test)cursorDoneSign(g, xPos, yPos);

        // Shows action required at top left of window
        // and opens dialog box to input calibration points
        if(!this.digitizationDone){
            switch(this.iCounter){
                case 0: g.drawString("RIGHT click on lower Y-axis calibration point", 5, 10);
                        break;
                case 1: if(this.noYlow){
                            this.lowYvalue = Db.readDouble("Enter lower Y-axis calibration value");
                            this.noYlow = false;
                        }
                        g.drawString("RIGHT click on higher Y-axis calibration point", 5, 10);
                        break;
                case 2: if(this.noYhigh){
                            this.highYvalue = Db.readDouble("Enter higher Y-axis calibration value");
                            this.noYhigh = false;
                        }
                        g.drawString("RIGHT click on lower X-axis calibration point", 5, 10);
                        break;
                case 3: if(this.noXlow){
                            this.lowXvalue = Db.readDouble("Enter lower X-axis calibration value");
                            this.noXlow = false;
                        }
                        g.drawString("RIGHT click on higher X-axis calibration point", 5, 10);
                        break;
                case 4: if(this.noXhigh){
                            this.highXvalue = Db.readDouble("Enter higher X-axis calibration value");
                            this.noXhigh = false;
                        }
                        g.drawString("LEFT click on points to be digitized [right click when finished digitizing]", 5, 10);
                        break;
                default:g.drawString("LEFT click on points to be digitized [right click when finished digitizing]", 5, 10);
            }
        }
        else{
            g.drawString("You may now close this window", 5, 10);
        }
    }

    private void cursorDoneSign(Graphics g, int x, int y){
        g.drawLine(x-5, y, x+5, y);
        g.drawLine(x, y-5, x, y+5);
        g.fillOval(x-3, y-3, 7, 7);
    }

    // This method will be called when the mouse has been clicked.
    public void mouseClicked(MouseEvent me) {

        if(!this.digitizationDone){
            switch(this.iCounter){
                // Low y-axis calibration point
                case 0: this.xPos = me.getX();
                    this.yPos = me.getY();

                    // identify left (1) or right (3) hand mouse click
                    this.button = me.getButton();
                    // add to sum
                    if(this.button==1){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                    }
                    else if(this.button==3){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                        this.lowYaxisXpixel = (double)this.sumX/(double)this.iSum;
                        this.lowYaxisYpixel = (double)this.windowHeight - (double)this.sumY/(double)this.iSum;
                        this.iCounter++;
                        this.sumX = 0;
                        this.sumY = 0;
                        this.iSum = 0;
                    }

                    break;
                // High y-axis calibration point
                case 1: this.xPos = me.getX();
                    this.yPos = me.getY();

                    // identify left (1) or right (3) hand mouse click
                    this.button = me.getButton();
                    // add to sum
                    if(this.button==1){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                    }
                    else if(this.button==3){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                        this.highYaxisXpixel = (double)this.sumX/(double)this.iSum;
                        this.highYaxisYpixel = (double)this.windowHeight - (double)this.sumY/(double)this.iSum;
                        this.iCounter++;
                        this.sumX = 0;
                        this.sumY = 0;
                        this.iSum = 0;
                    }
                    break;
                // Low x-axis calibration point
                case 2: this.xPos = me.getX();
                    this.yPos = me.getY();

                    // identify left (1) or right (3) hand mouse click
                    this.button = me.getButton();
                    // add to sum
                    if(this.button==1){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                    }
                    else if(this.button==3){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                        this.lowXaxisXpixel = (double)this.sumX/(double)this.iSum;
                        this.lowXaxisYpixel = (double)this.windowHeight - (double)this.sumY/(double)this.iSum;
                        this.iCounter++;
                        this.sumX = 0;
                        this.sumY = 0;
                        this.iSum = 0;
                    }
                    break;
                // High x-axis calibration point
                case 3: this.xPos = me.getX();
                    this.yPos = me.getY();

                    // identify left (1) or right (3) hand mouse click
                    this.button = me.getButton();
                    // add to sum

                    PixelGrabber pixelGrabber=new PixelGrabber(pic, this.xPos, this.yPos, 1, 1, false);

                    if(this.button==1){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                    }
                    else if(this.button==3){
                        this.sumX += this.xPos;
                        this.sumY += this.yPos;
                        this.iSum++;
                        this.highXaxisXpixel = (double)this.sumX/(double)this.iSum;
                        this.highXaxisYpixel = (double)this.windowHeight - (double)this.sumY/(double)this.iSum;
                        this.iCounter++;
                        this.sumX = 0;
                        this.sumY = 0;
                        this.iSum = 0;
                    }
                    break;
                // Data points
                default:
                    this.xPos = me.getX();
                    this.yPos = me.getY();

                    // identify left (1) or right (3) hand mouse click
                    this.button = me.getButton();
                    if(this.button==1){
                        this.xAndYvalues.add(new Integer(this.xPos));
                        this.xAndYvalues.add(new Integer(this.yPos));
                    }

                    // close file if right button clicked
                    if(this.button==3 && this.xAndYvalues.size()/2!=0){
                        this.outputData();
                        this.digitizationDone = true;
                    }
            }
        }

        //show the results of the click
        repaint();
    }

    // Output data to file and to graph
    private void outputData(){

        // dimension arrays
        this.nData = this.xAndYvalues.size()/2;
        System.out.println("nData " + this.nData);
        this.xPositions = new double[this.nData];
        this.yPositions = new double[this.nData];
        this.xPosPixel = new double[this.nData];
        this.yPosPixel = new double[this.nData];

        int ii = 0;
        // Convert pixel values to doubles
        for(int i=0; i<this.nData; i++){
            int xx = this.xAndYvalues.get(ii);
            ii++;
            int yy = this.xAndYvalues.get(ii);
            ii++;
            this.xPosPixel[i] =  (double)xx;
            this.yPosPixel[i] =  (double)this.windowHeight - (double)yy;
        }

        // Check if graph axes are to be rotated and, if so, rotate
        this.checkForRotation();

        // Scale the pixel values to true values
        for(int i=0; i<this.nData; i++){
            this.xPositions[i] =  this.lowXvalue + (this.xPosPixel[i] - this.lowXaxisXpixel)*(this.highXvalue - this.lowXvalue)/(this.highXaxisXpixel - this.lowXaxisXpixel);
            this.yPositions[i] =  this.lowYvalue + (this.yPosPixel[i] - this.lowYaxisYpixel)*(this.highYvalue - this.lowYvalue)/(this.highYaxisYpixel - this.lowYaxisYpixel);
        }

        // Check for identical points and remove one of all pairs of such points
        if(this.noIdentical)this.checkForIdenticalPoints();

        // Request to increase number of data points using a cubic spline interpolation
        String message = "Do you wish to increase number of data points\n";
        message += "using cubic spline interpolation?";
        boolean opt = Db.noYes(message);
        if(opt){
            this.nInterpPoints = Db.readInt("Enter number of interpolation points", 200);
            interpolation();
            this.interpOpt = true;
        }
        else{
            if(plotOpt)plotDigitisedPoints();
        }

        // Output digitized data
        this.fout.println("Digitization output for DigiGraph class (M. T. Flanagan Java Library)");
        this.fout.println();
        this.fout.dateAndTimeln();
        this.fout.println();
        this.fout.println("Image used in the digitization:                 " +  this.imageName);
        this.fout.println("Location of the image used in the digitization: " +  this.imagePath);
        this.fout.println();
        this.fout.println("X-axis skew angle    " + Fmath.truncate(this.angleXaxis, 4) + " degrees");
        this.fout.println("Y-axis skew angle    " + Fmath.truncate(this.angleYaxis, 4) + " degrees");
        this.fout.println("Axes mean skew angle " + Fmath.truncate(this.angleMean, 4) + " degrees");
        if(this.rotationDone){
            this.fout.println("Axes and all points rotated to bring axes to normal position");
        }
        else{
            this.fout.println("No rotation of axes or points performed");
        }
        this.fout.println();
        this.fout.println("Number of digitized points: " + this.nData);
        this.fout.println();
        this.fout.printtab("X-value");
        this.fout.println("Y-value");

        for(int i=0; i<this.nData; i++){
            this.fout.printtab(Fmath.truncate(this.xPositions[i], trunc));
            this.fout.println(Fmath.truncate(this.yPositions[i], trunc));
        }
        this.fout.println();

        // Output interpolated data if calculated
        if(this.interpOpt){
            this.fout.println();
            this.fout.println("Interpolated data (cubic spline)");
            this.fout.println();
            this.fout.println("Number of interpolated points: " + this.nInterpPoints);
            this.fout.println();
            this.fout.printtab("X-value");
            this.fout.println("Y-value");
            for(int i=0; i<this.nInterpPoints; i++){
                this.fout.printtab(Fmath.truncate(this.xInterp[i], trunc));
                this.fout.println(Fmath.truncate(this.yInterp[i], trunc));
            }
        }

        this.fout.close();
    }

    // Check for axes rotation
    private void checkForRotation(){
        double tangent = (this.highYaxisXpixel - this.lowYaxisXpixel)/(this.highYaxisYpixel - this.lowYaxisYpixel);
        this.angleYaxis = Math.toDegrees(Math.atan(tangent));
        tangent = (this.lowXaxisYpixel - this.highXaxisYpixel)/(this.highXaxisXpixel - this.lowXaxisXpixel);
        this.angleXaxis = Math.toDegrees(Math.atan(tangent));
        this.angleMean = (this.angleXaxis + this.angleYaxis)/2.0;
        double absMean = Math.abs(this.angleMean);
        if(absMean!=0.0 && absMean>this.angleTolerance)performRotation();
    }

    // Rotate axes and all points
    private void performRotation(){
        // Find pixel zero-zero origin
        double tangentX = (this.highXaxisYpixel - this.lowXaxisYpixel)/(this.highXaxisXpixel - this.lowXaxisXpixel);
        double interceptX = this.highXaxisYpixel - tangentX*this.highXaxisXpixel;
        double tangentY = (this.highYaxisYpixel - this.lowYaxisYpixel)/(this.highYaxisXpixel - this.lowYaxisXpixel);
        double interceptY = this.highYaxisYpixel - tangentY*this.highYaxisXpixel;
        double originX = (interceptX - interceptY)/(tangentY - tangentX);
        double originY = tangentY*originX + interceptY;

        // Rotate axes calibration points
        double angleMeanRad = Math.toRadians(this.angleMean);
        double cosphi = Math.cos(-angleMeanRad);
        double sinphi = Math.sin(-angleMeanRad);
        double highXaxisXpixelR = (this.highXaxisXpixel-originX)*cosphi + (this.highXaxisYpixel-originY)*sinphi + originX;
        double highXaxisYpixelR = -(this.highXaxisXpixel-originX)*sinphi + (this.highXaxisYpixel-originY)*cosphi + originY;
        double lowXaxisXpixelR = (this.lowXaxisXpixel-originX)*cosphi + (this.lowXaxisYpixel-originY)*sinphi + originX;
        double lowXaxisYpixelR = -(this.lowXaxisXpixel-originX)*sinphi + (this.lowXaxisYpixel-originY)*cosphi + originY;
        double highYaxisXpixelR = (this.highYaxisXpixel-originX)*cosphi + (this.highYaxisYpixel-originY)*sinphi + originX;
        double highYaxisYpixelR = -(this.highYaxisXpixel-originX)*sinphi + (this.highYaxisYpixel-originY)*cosphi + originY;
        double lowYaxisXpixelR = -(this.lowYaxisXpixel-originX)*cosphi + (this.lowYaxisYpixel-originY)*sinphi + originX;
        double lowYaxisYpixelR = (this.lowYaxisXpixel-originX)*sinphi + (this.lowYaxisYpixel-originY)*cosphi + originY;

        this.highXaxisXpixel = highXaxisXpixelR;
        this.highXaxisYpixel = highXaxisYpixelR;
        this.lowXaxisXpixel = lowXaxisXpixelR;
        this.lowXaxisYpixel = lowXaxisYpixelR;
        this.highYaxisXpixel = highYaxisXpixelR;
        this.highYaxisYpixel = highYaxisYpixelR;
        this.lowYaxisXpixel = lowYaxisXpixelR;
        this.lowYaxisYpixel = lowYaxisYpixelR;

        // Rotate data points
        for(int i=0; i<this.nData; i++){
            double xx = (this.xPosPixel[i]-originX)*cosphi + (this.yPosPixel[i]-originY)*sinphi + originX;
            double yy = -(this.xPosPixel[i]-originX)*sinphi + (this.yPosPixel[i]-originY)*cosphi + originY;
            this.xPosPixel[i] = xx;
            this.yPosPixel[i] = yy;
        }

        this.rotationDone = true;
    }

    // This is called when the mouse has been pressed
    // since it is empty nothing happens here.
    public void mousePressed (MouseEvent me) {}

    // This is called when the mouse has been released
    // since it is empty nothing happens here.
    public void mouseReleased (MouseEvent me) {}

    // This is executed when the mouse enters the object.
    // It will only be executed again when the mouse has left and then re-entered.
    public void mouseEntered (MouseEvent me) {
        this.mouseEntered = true;
        repaint();
    }

    // This is executed when the mouse leaves the object.
    public void mouseExited (MouseEvent me) {
        this.mouseEntered = false;
        repaint();
    }

    // Performs a cubic spline interpolation on the digitized points
    private void interpolation(){
        // Dimension interpolation arrasys
        this.xInterp = new double[this.nInterpPoints];
        this.yInterp = new double[this.nInterpPoints];

        // Calculate x-axis interpolation points
        double incr = (this.xPositions[this.nData-1] - this.xPositions[0])/(this.nInterpPoints - 1);
        this.xInterp[0] = this.xPositions[0];
        for(int i=1; i<this.nInterpPoints-1; i++){
            this.xInterp[i] = this.xInterp[i-1] + incr;
        }
        this.xInterp[this.nInterpPoints-1] = this.xPositions[this.nData-1];

        CubicSpline cs = new CubicSpline(this.xPositions, this.yPositions);

        // Interpolate y values
        for(int i=0; i<this.nInterpPoints; i++)this.yInterp[i] = cs.interpolate(this.xInterp[i]);

        // Plot interpolated curve
        if(this.plotOpt){
            int nMax = Math.max(this.nInterpPoints, this.nData);
            double[][] plotData = PlotGraph.data(2, nMax);

            plotData[0] = this.xPositions;
            plotData[1] = this.yPositions;
            plotData[2] = this.xInterp;
            plotData[3] = this.yInterp;

            PlotGraph pg = new PlotGraph(plotData);

            pg.setGraphTitle("Cubic Spline Interpolation of Digitised Points");
            pg.setGraphTitle2(this.imagePath);

            pg.setXaxisLegend("x");
            pg.setYaxisLegend("y");

            int[] lineOpt = {0, 3};
            pg.setLine(lineOpt);
            int[] pointOpt = {4, 0};
            pg.setPoint(pointOpt);

            pg.plot();

        }
    }

    // Checks for and removes all but one of identical points
    public void checkForIdenticalPoints(){
    	    int nP = this.nData;
    	    boolean test1 = true;
    	    int ii = 0;
    	    while(test1){
    	        boolean test2 = true;
    	        int jj = ii+1;
    	        while(test2){
    	            System.out.println("ii " + ii + "  jj  " + jj);
    	            if(this.xPositions[ii]==this.xPositions[jj] && this.yPositions[ii]==this.yPositions[jj]){
    	                System.out.print("Class DigiGraph: two identical points, " + this.xPositions[ii] + ", " + this.yPositions[ii]);
    	                System.out.println(", in data array at indices " + ii + " and " +  jj + ", one point removed");

    	                for(int i=jj; i<nP; i++){
    	                    this.xPositions[i-1] = this.xPositions[i];
    	                    this.yPositions[i-1] = this.yPositions[i];
    	                }
    	                nP--;
    	                if((nP-1)==ii)test2 = false;
    	            }
    	            else{
    	                jj++;
    	                if(jj>=nP)test2 = false;
    	            }
    	        }
    	        ii++;
    	        if(ii>=nP-1)test1 = false;
    	    }

    	    // Repack arrays if points deleted
    	    if(nP!=this.nData){
    	        double[] holdX = new double[nP];
    	        double[] holdY = new double[nP];
    	        for(int i=0; i<nP; i++){
    	            holdX[i] = this.xPositions[i];
    	            holdY[i] = this.yPositions[i];
    	        }
    	        this.xPositions = holdX;
    	        this.yPositions = holdY;
    	        this.nData = nP;
    	    }
    }

    // Plots the digitized points
    private void plotDigitisedPoints(){

            // Plot interpolated curve
            double[][] plotData = PlotGraph.data(1, this.nData);

            plotData[0] = this.xPositions;
            plotData[1] = this.yPositions;

            PlotGraph pg = new PlotGraph(plotData);

            pg.setGraphTitle("Plot of the Digitised Points");
            pg.setGraphTitle2(this.imagePath);

            pg.setXaxisLegend("x");
            pg.setYaxisLegend("y");

            int[] lineOpt = {0};
            pg.setLine(lineOpt);
            int[] pointOpt = {4};
            pg.setPoint(pointOpt);

            pg.plot();
    }
}



