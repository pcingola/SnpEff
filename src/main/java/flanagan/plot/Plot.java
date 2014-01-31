/*
*   Class   Plot
*
*   Superclass for the plotting subclasses:
*       PlotGraph and PlotPoleZero
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	 February 2002
*   REVISED: 20 July 2005, 7 July 2008, 27 July 2008, 11 August 2008, 5 February 2011, 21 February 2011
*
*   Copyright (c) 2002 - 2008
*
*   DOCUMENTATION
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PlotGraph.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
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

import java.awt.*;
import java.io.Serializable;

import flanagan.math.Fmath;
import flanagan.math.ArrayMaths;
import flanagan.interpolation.CubicSpline;

public class Plot extends Canvas implements Serializable{

        protected static final long serialVersionUID = 1L;  // serial version unique identifier

    	protected double[][] data = null;               // data to be plotted
                                        	            // data[i][] i = 0, 2, 4 . . .  x values
                                        	            // data[i][] i = 1, 3, 5 . . .  y values for x[i-1][]
    	protected double[][] copy = null;               // copy of original data to be plotted
    	protected int nCurves = 0;                      // number of curves
    	protected int[] nPoints = null;                 // number of points points on curve each curve
    	protected int nmPoints = 0;                     // number of points points on curve with most points
    	protected int niPoints = 200;                   // number of cubic spline interpolation points
    	protected int[] pointOpt = null;                // point plotting option for each curve
                                        	            // pointOpt = 0: no points plotted
                                        	            // pointOpt = i where i = 1,2,3,4,5,6,7,8: points plotted
                                            	        // default options
                                            		        // curve 1 - open circles
                                            		        // curve 2 - open squares
                                            		        // curve 3 - open diamonds
                                            		        // curve 4 - filled circles
                                            		        // curve 5 - filled squares
                                            		        // curve 6 - filled diamonds
                                            		        // curve 7 - x crosses
                                            		        // curve 8 - + crosses
                                            		        // further curves - above sequence repeated
    	protected int[] pointSize = null;               // point size in pixels for each curve
    	protected int npTypes = 8;                      // number of point types
    	protected boolean[] errorBar = null;            // true - error bar plotted, flase no error bar plotted - default = false
    	protected double[][] errors = null;             // error bar values - should be an estimate of the sd of the variable
    	protected double[][] errorsCopy = null;         // copy of error bar values
    	protected int[] lineOpt = null;                 // line drawing option for each curve
                                            	            // lineOpt = 0: no line plotted
                                            	            // lineOpt = 1: cubic spline interpolation line plotted as a continuous line
                                            	            // lineOpt = 2: cubic spline interpolation line plotted as a dashed line
                                            	            // lineOpt = 3: line plotted by joining points
                                            	            // lineOpt = 4: dashed line plotted by joining points
                                            	            // default - lineOpt = 1
    	protected int[] dashLength = null;              // dash length in lineOpt = 2
    	protected boolean[] minMaxOpt = null;           // true - curve included in maximum and minimum axes value calculation
    	protected boolean[] trimOpt = null;             // true - curve trimmed to fit axes rectangle

    	protected int fontSize = 14;                    // text font size
    	protected int xLen = 625;                       // length of the x axis in pixels
   	    protected int yLen = 375;                       // length of the y axis in pixels
    	protected int xBot = 100;                       // x coordinate of the bottom of the x axis in pixels
    	protected int xTop = xBot+xLen;                 // x coordinate of the top of the x axis in pixels
    	protected int yTop = 110;                       // y coordinate of the top of the y axis in pixels
    	protected int yBot = yTop+yLen;                 // y coordinate of the bottom of the y axis in pixels

    	protected double xLow = 0;                      // scaled lower limit data value of the x axis
    	protected double xHigh = 0;                     // scaled upper limit data value of the x axis
    	protected double yLow = 0;                      // scaled lower limit data value of the y axis
    	protected double yHigh = 0;                     // scaled upper limit data value of the y axis
    	protected int xFac = 0;                         // decadic exponent of x axis scaling factor
    	protected int yFac = 0;                         // decadic exponent of y axis scaling factor
    	protected int xTicks = 0;                       // number of x axis ticks
    	protected int yTicks = 0;                       // number of y axis ticks

    	protected double xMin = 0.0D;                   // minimum x data value
    	protected double xMax = 0.0D;                   // maximum x data value
    	protected double yMin = 0.0D;                   // minimum y data value
    	protected double yMax = 0.0D;                   // maximum y data value

    	protected double xOffset = 0.0D;                // xaxis data value offset
   	    protected double yOffset = 0.0D;                // y axis data value offset
    	protected boolean noXoffset = false;            // no x axis offset allowed if true
   	    protected boolean noYoffset = false;            // no y axis offset allowed if true
    	protected double xLowFac = 0.75D;               // x axis data setting low factor
    	protected double yLowFac = 0.75D;               // y axis data setting low factor

    	protected String graphTitle  = "  ";            // graph title
    	protected String graphTitle2 = "  ";            // graph title (secondline)
    	protected String xAxisLegend = "  ";            // x axis legend title
    	protected String xAxisUnits  = "  ";            // x axis unit name, e.g.  V, ohm
    	protected String yAxisLegend = "  ";            // y axis legend title
    	protected String yAxisUnits  = "  ";            // x axis unit name

    	protected boolean xZero = false;                // if true - a (x=0) zero line is required
    	protected boolean yZero = false;                // if true - a (y=0) zero line required
    	protected boolean noXunits = true;              // if true - no x axis units
    	protected boolean noYunits = true;              // if true - no y axis units

    	protected double[] xAxisNo = new double[50];    // x axis legend numbers as double
    	protected double[] yAxisNo = new double[50];    // y axis legend numbers as double
    	protected String[] xAxisChar = new String[50];  // x axis legend numbers as char
    	protected String[] yAxisChar = new String[50];  // y axis legend numbers as char
    	protected int[] axisTicks = new int[50];        // no of ticks for scaled lengths

    	protected static double dataFill = Double.NaN;  // value used to initialise data array by Plot.data()


        // Constructor
    	//One 2-dimensional data arrays
    	public Plot(double[][] data){
        	this.initialise(data);
   	    }

   	    // Constructor
    	//Two 1-dimensional data arrays
    	public Plot(double[] xdata, double[] ydata){
    	    int xl = xdata.length;
    	    int yl = ydata.length;
    	    if(xl!=yl)throw new IllegalArgumentException("x-data length is not equal to the y-data length");
    	    double[][] data = new double[2][xl];
    	    for(int i=0; i<xl; i++){
    	        data[0][i] = xdata[i];
    	        data[1][i] = ydata[i];
    	    }
        	this.initialise(data);
   	    }

    	// Initialisation
    	private void initialise(double[][] cdata){

            // Calculate number of curves
        	this.nCurves  = cdata.length/2;

        	// Initialize 1D class arrays
        	this.nPoints  = new int[nCurves];
        	this.lineOpt  = new int[nCurves];
        	this.dashLength  = new int[nCurves];
        	this.trimOpt  = new boolean[nCurves];
        	this.minMaxOpt = new boolean[nCurves];
        	this.pointOpt = new int[nCurves];
        	this.pointSize = new int[nCurves];
        	this.errorBar = new boolean[nCurves];

            // Calculate maximum number of points on a single curve
        	this.nmPoints = 0;
        	int ll = 0;
        	for(int i=0; i<2*nCurves; i++){
            		if((ll=cdata[i].length)>nmPoints)nmPoints=ll;
        	}

        	// Initialize class 2D arrays
        	this.data = new double[2*nCurves][nmPoints];
        	this.copy = new double[2*nCurves][nmPoints];
        	this.errors = new double[nCurves][nmPoints];
        	this.errorsCopy = new double[nCurves][nmPoints];


            // Calculate curve lengths
            // and check all individual curves have an equal number of  abscissae and ordinates
        	int k = 0, l1 = 0, l2 = 0;
        	boolean testlen=true;
        	for(int i=0; i<nCurves; i++){
        	        k=2*i;
            		testlen=true;
            		l1=cdata[k].length;
            		l2=cdata[k+1].length;
            		if(l1!=l2)throw new IllegalArgumentException("an x and y array length differ");
            		nPoints[i]=l1;

            }

            // Remove both abscissae and ordinates for points equal to dataFill
        	k=0;
        	boolean testopt=true;
            for(int i=0; i<nCurves; i++){
                testlen=true;
                l1=nPoints[i];
                 while(testlen){
                    if(l1<0)throw new IllegalArgumentException("curve array index  "+k+ ": blank array");
                    if(cdata[k][l1-1]!=cdata[k][l1-1]){
                        if(cdata[k+1][l1-1]!=cdata[k+1][l1-1]){
                            l1--;
                            testopt=false;
                        }
                        else{
                            testlen=false;
                        }
                    }
                    else{
                        testlen=false;
                    }
                }
                nPoints[i]=l1;
                k+=2;
            }

            // Sort arrays into ascending order
            k = 0;
            for(int i=0; i<nCurves; i++){
                double[][] xxx = new double[2][nPoints[i]];
                for(int j=0; j<nPoints[i]; j++){
                    xxx[0][j] = cdata[k][j];
                    xxx[1][j] = cdata[k+1][j];
                }
                xxx = doubleSelectionSort(xxx);
                for(int j=0; j<nPoints[i]; j++){
                    cdata[k][j] = xxx[0][j];
                    cdata[k+1][j] = xxx[1][j];
                }
                k += 2;
            }

            // initialize class data variables
        	k=0;
        	int kk=1;
        	for(int i=0; i<nCurves; i++){

        	        // reverse order if all abscissae are in descending order
        	        int rev = 1;
        	        for(int j=1; j<nPoints[i]; j++){
                  		if(cdata[k][j]<cdata[k][j-1])rev++;
            		}
            		if(rev==nPoints[i]){
            		    double[] hold = new double[nPoints[i]];
           		        for(int j=0; j<nPoints[i]; j++)hold[j] = cdata[k][j];
            		    for(int j=0; j<nPoints[i]; j++)cdata[k][j] = hold[nPoints[i]-j-1];
            		    for(int j=0; j<nPoints[i]; j++)hold[j] = cdata[k+1][j];
            		    for(int j=0; j<nPoints[i]; j++)cdata[k+1][j] = hold[nPoints[i]-j-1];
            		}

            		// copy arrays
            		for(int j=0; j<nPoints[i]; j++){
            		    this.data[k][j]=cdata[k][j];
                		this.data[k+1][j]=cdata[k+1][j];
                  		this.copy[k][j]=cdata[k][j];
                		this.copy[k+1][j]=cdata[k+1][j];
            		}

            		this.lineOpt[i] = 1;
            		this.dashLength[i] = 5;
            		this.trimOpt[i] = false;
            		if(this.lineOpt[i]==1)trimOpt[i] = true;
            		this.minMaxOpt[i]=true;
            		this.pointSize[i]= 6;
            		this.errorBar[i]= false;
            		this.pointOpt[i] = kk;
            		k+=2;
            		kk++;
            		if(kk>npTypes)kk = 1;
        	}
    	}

        // sort x elements into ascending order with matching switches of y elements
        // using selection sort method
        public static double[][] doubleSelectionSort(double[][] aa){
            int index = 0;
            int lastIndex = -1;
            int n = aa[0].length;
            double holdx = 0.0D;
            double holdy = 0.0D;
            double[][] bb = new double[2][n];
            for(int i=0; i<n; i++){
                bb[0][i]=aa[0][i];
                bb[1][i]=aa[1][i];
            }


            while(lastIndex != n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(bb[0][i]<bb[0][index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=bb[0][index];
                bb[0][index]=bb[0][lastIndex];
                bb[0][lastIndex]=holdx;
                holdy=bb[1][index];
                bb[1][index]=bb[1][lastIndex];
                bb[1][lastIndex]=holdy;

            }
            return bb;
        }


    	//Create a data array initialised to dataFill;
    	public static double[][] data(int n, int m){
        	double[][] d = new double[2*n][m];
        	for(int i=0; i<2*n; i++){
            		for(int j=0; j<m; j++){
                		d[i][j]=dataFill;
            		}
        	}
        	return d;
    	}

    	//Change the value used to initialise the datarray
    	public static void setDataFillValue(double dataFill){
    	    Plot.dataFill=dataFill;
        }

    	//Get the value used to initialise the datarray
    	public static double getDataFillValue(){
    	    return Plot.dataFill;
        }

    	// Enter primary graph title
    	public void setGraphTitle(String graphTitle){
        	this.graphTitle=graphTitle;
    	}

    	// Enter second line to graph title
    	public void setGraphTitle2(String graphTitle2){
        	this.graphTitle2=graphTitle2;
    	}

    	// Enter x axis legend
    	public void setXaxisLegend(String xAxisLegend){
        	this.xAxisLegend=xAxisLegend;
    	}

    	// Enter y axis legend
    	public void setYaxisLegend(String yAxisLegend){
        	this.yAxisLegend=yAxisLegend;
    	}

    	// Enter x axis unit name
    	public void setXaxisUnitsName(String xAxisUnits){
        	this.xAxisUnits=xAxisUnits;
        	this.noXunits=false;
    	}

    	// Enter y axis unit name
    	public void setYaxisUnitsName(String yAxisUnits){
        	this.yAxisUnits=yAxisUnits;
         	this.noYunits=false;
    	}

    	// Get pixel length of the x axis
    	public int getXaxisLen(){
        	return this.xLen;
    	}

    	// Get pixel length of the y axis
    	public int getYaxisLen(){
        	return this.yLen;
    	}

    	// Get pixel start of the x axis
    	public int getXlow(){
        	return this.xBot;
    	}

    	// Get pixel end of the y axis
    	public int getYhigh(){
        	return this.yTop;
    	}

    	// Get point size in pixels
    	public int[] getPointsize(){
        	return this.pointSize;
    	}

    	// Get dash length in pixels
    	public int[] getDashlength(){
        	return this.dashLength;
    	}

    	// Get the x axis low factor
    	public double getXlowFac(){
        	return 1.0D-this.xLowFac;
    	}

    	// Get the y axis low factor
    	public double getYlowFac(){
        	return 1.0D-this.yLowFac;
    	}

    	// Get the x axis minimum value
    	public double getXmin(){
        	return this.xMin;
    	}

    	// Get the x axis maximum value
    	public double getXmax(){
        	return this.xMax;
    	}

    	// Get the y axis minimum value
    	public double getYmin(){
        	return this.yMin;
    	}

    	// Get the y axis maximum value
    	public double getYmax(){
        	return this.yMax;
    	}

    	// get line plotting option
    	public int[] getLine(){
        	return this.lineOpt;
    	}

    	// Get point plotting options
    	public int[] getPoint(){
        	return this.pointOpt;
    	}

    	// Get the number of points to be used in the cubic spline interpolation
    	public int getNiPoints(){
        	return this.niPoints;
    	}

    	// Get font size
    	public int getFontSize(){
        	return this.fontSize;
    	}

    	// Reset pixel length of the x axis
    	public void setXaxisLen(int xLen){
        	this.xLen=xLen;
        	this.update();
    	}

    	// Reset pixel length of the y axis
    	public void setYaxisLen(int yLen){
        	this.yLen=yLen;
        	this.update();
    	}

    	// Reset pixel start of the x axis
    	public void setXlow(int xBot){
        	this.xBot=xBot;
        	this.update();
    	}

    	// Reset pixel end of the y axis
    	public void setYhigh(int yTop){
        	this.yTop=yTop;
        	this.update();
    	}

    	// Reset the x axis low factor
    	public void setXlowFac(double xLowFac){
        	this.xLowFac=1.0D-xLowFac;
    	}

    	// Reset the y axis low factor
    	public void setYlowFac(double yLowFac){
        	this.yLowFac=1.0D-yLowFac;
    	}

    	// Reset the x axis offset option
    	public void setNoXoffset(boolean noXoffset){
        	this.noXoffset=noXoffset;
    	}

    	// Reset the y axis offset option
    	public void setNoYoffset(boolean noYoffset){
        	this.noYoffset=noYoffset;
    	}

    	// Reset both the x and y axis offset options to the same optio
    	public void setNoOffset(boolean nooffset){
        	this.noXoffset=nooffset;
        	this.noYoffset=nooffset;
    	}

    	// Get the x axis offset option
    	public boolean getNoXoffset(){
        	return this.noXoffset;
    	}

    	// RGet the y axis offset option
    	public boolean getNoYoffset(){
        	return this.noYoffset;
    	}

    	// Update axis pixel position parameters
    	protected void update(){
        	this.xTop = this.xBot + this.xLen;
        	this.yBot = this.yTop + this.yLen;
    	}

    	// Overwrite line plotting option with different options for individual curves
    	public void setLine(int[] lineOpt){
        	int n=lineOpt.length;
        	if(n!=nCurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<n; i++)if(lineOpt[i]<0 || lineOpt[i]>4)throw new IllegalArgumentException("lineOpt must be 0, 1, 2, 3 or 4");
        	this.lineOpt=lineOpt;

        	// check if data supports cubic spline interpolation if lineOpt = 1 or 2
        	for(int i=0; i<this.lineOpt.length; i++){
        	    if(this.lineOpt[i]==1 || this.lineOpt[i]==2){
        	        // check if some points reverse direction
        	        boolean test0 = false;
        	        for(int j=1; j<this.nPoints[i]; j++){
        	            if(data[i][j]<data[i][j-1])test0=true;
        	        }
        	        if(test0){
        	            // check if y all in ascending order
        	            int rev = 1;
        	            for(int j=1; j<nPoints[i]; j++){
                  		    if(data[2*i][j]>data[2*i][j-1])rev++;
            		    }
            		    if(rev==nPoints[i]){
            		        lineOpt[i]=-lineOpt[i];
            		    }
            		    else{
            		        // check if y all in descending order
        	                rev = 1;
        	                for(int j=1; j<nPoints[i]; j++){
                  		        if(data[2*i][j]<data[2*i][j-1])rev++;
            		        }
            		        if(rev==nPoints[i]){
            		            // reverse order of y
           		                double[] hold = new double[nPoints[i]];
           		                for(int j=0; j<nPoints[i]; j++)hold[j] = data[i][j];
            		            for(int j=0; j<nPoints[i]; j++)data[i][j] = hold[nPoints[i]-j-1];
            		            for(int j=0; j<nPoints[i]; j++)hold[j] = data[2*i][j];
            		            for(int j=0; j<nPoints[i]; j++)data[2*i][j] = hold[nPoints[i]-j-1];
            		            this.lineOpt[i] = - lineOpt[i];
            		        }
        	                else{
        	                    System.out.println("Curve "+i+" will not support interpolation");
        	                    System.out.println("Straight connecting line option used");
        	                    if(this.lineOpt[i]==1) this.lineOpt[i] = 3;
        	                    if(this.lineOpt[i]==2) this.lineOpt[i] = 4;
        	                }
        	            }
        	        }
        	    }
        	}
    	}

    	// Overwrite line plotting option with a single option for all curves
    	public void setLine(int slineOpt){
        	if(slineOpt<0 || slineOpt>3)throw new IllegalArgumentException("lineOpt must be 0, 1, 2 or 3");
        	for(int i=0; i<this.nCurves; i++)this.lineOpt[i]=slineOpt;
    	}

    	// Overwrite dash length with different options for individual curves
    	public void setDashLength(int[] dashLength){
        	if(dashLength.length!=nCurves)throw new IllegalArgumentException("input array of wrong length");
        	this.dashLength=dashLength;
    	}

    	// Overwrite dashLength with a single option for all curves
    	public void setDashLength(int sdashLength){
        	for(int i=0; i<this.nCurves; i++)this.dashLength[i]=sdashLength;
    	}

    	// Overwrite point plotting option with different options for individual curves
    	public void setPoint(int[] pointOpt){
        	int n=pointOpt.length;
        	if(n!=nCurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<n; i++)if(pointOpt[i]<0 || pointOpt[i]>8)throw new IllegalArgumentException("pointOpt must be 0, 1, 2, 3, 4, 5, 6, 7, or 8");
        	this.pointOpt=pointOpt;
    	}

    	// Overwrite point plotting option with a single option for all curves
    	public void setPoint(int spointOpt){
        	if(spointOpt<0 || spointOpt>8)throw new IllegalArgumentException("pointOpt must be 0, 1, 2, 3, 4, 5, 6, 7, or 8");
        	for(int i=0; i<this.nCurves; i++)this.pointOpt[i]=spointOpt;
    	}

    	// Overwrite point size with different options for individual curves
    	public void setPointSize(int[] mpointSize){
         	if(mpointSize.length!=nCurves)throw new IllegalArgumentException("input array of wrong length");
        	for(int i=0; i<this.nCurves; i++){
            		if(mpointSize[i]!=(mpointSize[i]/2)*2)mpointSize[i]++;
            		this.pointSize[i]=mpointSize[i];
        	}
    	}

    	// Overwrite point size with a single option for all curves
    	public void setPointSize(int spointSize){
        	if(spointSize%2!=0)spointSize++;
        	for(int i=0; i<this.nCurves; i++)this.pointSize[i]=spointSize;
    	}

    	// Set errorBar values
    	// Must set each curve individually
    	// nc is the curve identifier (remember curves start at 0)
    	// err are the error bar values which should be an estimate of the standard devition of the experimental point
    	public void setErrorBars(int nc, double[] err){
        	if(err.length!=this.nPoints[nc])throw new IllegalArgumentException("input array of wrong length");
        	this.errorBar[nc] = true;
        	for(int i=0; i<this.nPoints[nc]; i++){
            		this.errors[nc][i] = err[i];
            		this.errorsCopy[nc][i] = err[i];
        	}
    	}

    	// overwrite the number of points to be used in the cubic spline interpolation
    	public void setNiPoints(int niPoints){
        	this.niPoints=niPoints;
    	}

    	// overwrite the font size
    	public void setFontSize(int fontSize){
        	this.fontSize=fontSize;
    	}

    	// overwrite the trim option
    	public void setTrimOpt(boolean[] trim){
        	this.trimOpt=trim;
    	}

    	// overwrite the minMaxOpt option
    	public void setMinMaxOpt(boolean[] minmax){
        	this.minMaxOpt=minmax;
    	}

    	// Calculate scaling  factors
   	    public static int scale(double mmin, double mmax){
        	int fac=0;
        	double big=0.0D;
        	boolean test=false;

        	if(mmin>=0.0 && mmax>0.0){
            		big=mmax;
            		test=true;
        	}
        	else{
            		if(mmin<0.0 && mmax<=0.0){
                		big=-mmin;
                		test=true;
            		}
            		else{
                		if(mmax>0.0 && mmin<0.0){
                    			big=Math.max(mmax, -mmin);
                    			test=true;
                		}
            		}
        	}

        	if(test){
            		if(big>100.0){
                		while(big>1.0){
                    			big/=10.0;
                    			fac--;
                		}
            		}
            		if(big<=0.01){
                		while(big<=0.10){
                    			big*=10.0;
                    			fac++;
                		}
            		}
        	}
        	return fac;
    	}

    	// Set low value on axis
    	public static void limits(double low, double high, double lowfac, double[]limits){

            double facl = 1.0D;
            double fach = 1.0D;
            if(Math.abs(low)<1.0D)facl=10.0D;
            if(Math.abs(low)<0.1D)facl=100.0D;
            if(Math.abs(high)<1.0D)fach=10.0D;
            if(Math.abs(high)<0.1D)fach=100.0D;

        	double ld=Math.floor(10.0*low*facl)/facl;
        	double hd=Math.ceil(10.0*high*fach)/fach;

        	if(ld>=0.0D && hd>0.0D){
            		if(ld<lowfac*hd){
                		ld=0.0;
            		}
        	}
        	if(ld<0.0D && hd<=0.0D){
            		if(-hd <= -lowfac*ld){
                		hd=0.0;
             		}
        	}
        	limits[0] = ld/10.0;
        	limits[1] = hd/10.0;
    	}

    	// Calculate axis offset value
   	    public static double offset(double low, double high){

        	double diff = high - low;
        	double sh = Fmath.sign(high);
        	double sl = Fmath.sign(low);
        	double offset=0.0D;
        	int eh=0, ed=0;

        	if(sh == sl){
            		ed=(int)Math.floor(Fmath.log10(diff));
            		if(sh==1){
                		eh=(int)Math.floor(Fmath.log10(high));
                		if(eh-ed>1)offset = Math.floor(low*Math.pow(10, -ed))*Math.pow(10,ed);
            		}
            		else{
                		eh=(int)Math.floor(Fmath.log10(Math.abs(low)));
                		if(eh-ed>1)offset = Math.floor(high*Math.pow(10, -ed))*Math.pow(10,ed);
            		}
        	}
        	return offset;
    	}


    	// Calculate scaling and offset values for both axes
    	public void axesScaleOffset(){

        	double[] limit = new double[2];

        	// tranfer data from copy to enable redrawing
        	int k=0;
        	for(int i=0; i<nCurves; i++){
            		for(int j=0; j<nPoints[i]; j++){
                		this.data[k][j]=this.copy[k][j];
                		this.data[k+1][j]=this.copy[k+1][j];
                		this.errors[i][j]=this.errorsCopy[i][j];
                		if(this.errorBar[i])this.errors[i][j]+=this.data[k+1][j];
            		}
            		k+=2;
        	}

        	// Find mimium and maximum data values
        	minMax();

       		// Calculate x axis offset values and subtract it from the data
        	if(!noXoffset)this.xOffset=offset(this.xMin, this.xMax);
        	if(this.xOffset!=0.0){
            		k=0;
            		for(int i=0; i<this.nCurves; i++){
                		for(int j=0; j<this.nPoints[i]; j++){
                    			this.data[k][j] -= this.xOffset;
                		}
                		k+=2;
            		}
            		this.xMin -= this.xOffset;
            		this.xMax -= this.xOffset;
        	}

        	// Calculate y axis offset values and subtract it from the data
        	if(!noYoffset)this.yOffset=offset(this.yMin, this.yMax);
        	if(this.yOffset!=0.0){
            		k=1;
            		for(int i=0; i<this.nCurves; i++){
                		for(int j=0; j<this.nPoints[i]; j++){
                    			this.data[k][j] -= this.yOffset;
                    			if(this.errorBar[i])this.errors[i][j] -= this.yOffset;
                		}
                		k+=2;
            		}
            		this.yMin -= this.yOffset;
            		this.yMax -= this.yOffset;
        	}

        	// Calculate x axes scale values and scale data
        	this.xFac = scale(this.xMin, this.xMax);
        	if(this.xFac!=0){
            		k=0;
            		for(int i=0; i<this.nCurves; i++){
                		for(int j=0; j<this.nPoints[i]; j++){
                    			this.data[k][j] *= Math.pow(10, this.xFac+1);
                		}
                		k+=2;
            		}
            		this.xMin *= Math.pow(10, this.xFac+1);
            		this.xMax *= Math.pow(10, this.xFac+1);
        	}

        	// Calculate y axes scale values and scale data
        	this.yFac = scale(this.yMin, this.yMax);
        	if(this.yFac!=0){
            		k=1;
            		for(int i=0; i<this.nCurves; i++){
                		for(int j=0; j<this.nPoints[i]; j++){
                    			this.data[k][j] *= Math.pow(10, yFac+1);
                    			if(this.errorBar[i])this.errors[i][j] *= Math.pow(10, this.yFac+1);
                		}
                		k+=2;
            		}
            		this.yMin *= Math.pow(10, this.yFac+1);
            		this.yMax *= Math.pow(10, this.yFac+1);
        	}

        	// Calculate scaled low and high values
        	// x axis
        	limits(this.xMin, this.xMax, this.xLowFac, limit);
        	this.xLow  = limit[0];
        	this.xHigh = limit[1];
        	if(xLow<0 && xHigh>0)xZero=true;
        	// y axis
        	limits(this.yMin, this.yMax, this.yLowFac, limit);
        	this.yLow  = limit[0];
        	this.yHigh = limit[1];
        	if(yLow<0 && yHigh>0)yZero=true;

        	// Calculate tick parameters
        	// x axis
        	this.xTicks = ticks(this.xLow, this.xHigh, this.xAxisNo, this.xAxisChar);
        	this.xHigh = this.xAxisNo[this.xTicks-1];
        	if(this.xLow!=this.xAxisNo[0]){
        	    if(this.xOffset!=0.0D){
        	        this.xOffset = this.xOffset - this.xLow + this.xAxisNo[0];
        	    }
        	    this.xLow = this.xAxisNo[0];
        	}
        	// y axis
        	this.yTicks = ticks(this.yLow, this.yHigh, this.yAxisNo, this.yAxisChar);
        	this.yHigh = this.yAxisNo[this.yTicks-1];
        	if(this.yLow!=this.yAxisNo[0]){
        	    if(this.yOffset!=0.0D){
        	        this.yOffset = this.yOffset - this.yLow + this.yAxisNo[0];
        	    }
        	    this.yLow = this.yAxisNo[0];
        	}

    	}

    	// Calculate axis ticks and tick values
    	public static int ticks(double low, double high, double[] tickval, String[] tickchar){

        	// Find range
            int[] trunc = {1, 1, 1, 2, 3};
            double[] scfac1 = {1.0, 10.0, 1.0, 0.1, 0.01};
            double[] scfac2 = {1.0, 1.0, 0.1, 0.01, 0.001};

            double rmax = Math.abs(high);
            double temp = Math.abs(low);
            if(temp>rmax)rmax = temp;
            int range = 0;
            if(rmax<=100.0D){
                range = 1;
            }
            if(rmax<=10.0D){
                range = 2;
            }
            if(rmax<=1.0D){
                range = 3;
            }
            if(rmax<=0.1D){
                range = 4;
            }
            if(rmax>100.0D || rmax<0.01)range = 0;

        	// Calculate number of ticks
        	double inc = 0.0D;
        	double bot = 0.0D;
        	double top = 0.0D;
        	int sgn = 0;
        	int dirn = 0;
        	if(high>0.0D && low>=0.0D){
        	    inc = Math.ceil((high-low)/scfac1[range])*scfac2[range];
        	    dirn = 1;
        	    bot = low;
        	    top = high;
        	    sgn = 1;
        	}
        	else{
        	    if(high<=0 && low<0.0D){
        	        inc = Math.ceil((high-low)/scfac1[range])*scfac2[range];
        	        dirn = -1;
        	        bot = high;
        	        top = low;
        	        sgn = -1;
        	    }
        	    else{
        	        double up = Math.abs(Math.ceil(high));
        	        double down = Math.abs(Math.floor(low));
        	        int np = 0;
        	        if(up>=down){
        	            dirn = 2;
        	            np = (int)Math.rint(10.0*up/(up+down));
        	            inc = Math.ceil((high*10/np)/scfac1[range])*scfac2[range];
                        bot = 0.0D;
        	            top = high;
        	            sgn = 1;
        	        }
        	        else{
        	            dirn = -2;
        	            np = (int)Math.rint(10.0D*down/(up+down));
        	            inc = Math.ceil((Math.abs(low*10/np))/scfac1[range])*scfac2[range];
        	            bot = 0.0D;
        	            top = low;
        	            sgn = -1;
        	        }
        	    }
        	}

            int nticks = 1;
            double sum = bot;
            boolean test = true;
            while(test){
                sum = sum + sgn*inc;
                nticks++;
                if(Math.abs(sum)>=Math.abs(top))test=false;
            }

        	// Calculate tick values
        	int npExtra = 0;
        	double[] ttickval = null;;
        	switch(dirn){
        	    case 1:     ttickval = new double[nticks];
        	                tickval[0]=Fmath.truncate(low, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]+inc, trunc[range]);
        	                }
        	                break;
        	    case -1:    ttickval = new double[nticks];
        	                ttickval[0]=Fmath.truncate(high, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            ttickval[i]  = Fmath.truncate(ttickval[i-1]-inc, trunc[range]);
        	                }
        	                ttickval = Fmath.reverseArray(ttickval);
        	                for(int i=0; i<nticks; i++)tickval[i] = ttickval[i];
        	                break;
        	    case 2:     npExtra = (int)Math.ceil(-low/inc);
        	                nticks += npExtra;
        	                ttickval = new double[nticks];
         	                tickval[0]=Fmath.truncate(-npExtra*inc, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            tickval[i]  = Fmath.truncate(tickval[i-1]+inc, trunc[range]);
        	                }
           	                break;
           	    case -2:    npExtra = (int)Math.ceil(high/inc);
        	                nticks += npExtra;
        	                ttickval = new double[nticks];
        	                ttickval[0]=Fmath.truncate(npExtra*inc, trunc[range]);
        	                for(int i=1; i<nticks; i++){
            		            ttickval[i]  = Fmath.truncate(ttickval[i-1]-inc, trunc[range]);
        	                }
        	                ttickval = Fmath.reverseArray(ttickval);
        	                for(int i=0; i<nticks; i++)tickval[i] = ttickval[i];
        	                break;
           	}

            // ensure a zero value is truly zero and not a zero with rounding errors, e.g. 1e-17
           	ArrayMaths am = new ArrayMaths(tickval);
            double max = am.maximum();
            double min = Math.abs(am.minimum());
            boolean testZero = true;
            int counter = 0;
            while(testZero){
                if(Math.abs(tickval[counter])<max*1e-4 || Math.abs(tickval[counter])<min*1e-4){
                    tickval[counter] = 0.0;
                    testZero = false;
                }
                else{
                    counter++;
                    if(counter>=nticks)testZero = false;
                }
            }

            // set String form of tick values
        	for(int i=0; i<nticks; i++){
            		tickchar[i] = String.valueOf(tickval[i]);
            		tickchar[i] = tickchar[i].trim();
        	}

        	return nticks;
    	}

    	// Find minimum and maximum x and y values
    	public void minMax(){
        	boolean test  = true;

        	int ii=0;
        	while(test){
            		if(this.minMaxOpt[ii]){
                		test=false;
                		this.xMin=this.data[2*ii][0];
                		this.xMax=this.data[2*ii][0];
                		this.yMin=this.data[2*ii+1][0];
                		if(this.errorBar[ii])this.yMin=2.0D*this.yMin-this.errors[ii][0];
                 		this.yMax=this.data[2*ii+1][0];
                		if(this.errorBar[ii])this.yMax=errors[ii][0];
            		}
            		else{
                		ii++;
                		if(ii>nCurves)throw new IllegalArgumentException("At least one curve must be included in the maximum/minimum calculation");
            		}
        	}

        	int k=0;
        	double yMint=0.0D, yMaxt=0.0D;
        	for(int i=0; i<this.nCurves; i++){
            		if(minMaxOpt[i]){
                		for(int j=0; j<this.nPoints[i]; j++){
                    			if(this.xMin>this.data[k][j])this.xMin=this.data[k][j];
                    			if(this.xMax<this.data[k][j])this.xMax=this.data[k][j];
                    			yMint=this.data[k+1][j];
                   		        if(errorBar[i])yMint=2.0D*yMint-errors[i][j];
                    			if(this.yMin>yMint)this.yMin=yMint;
                    			yMaxt=this.data[k+1][j];
                   		        if(errorBar[i])yMaxt=errors[i][j];
                    			if(this.yMax<yMaxt)this.yMax=yMaxt;
                		}
            		}
            		k+=2;
        	}

        	if(this.xMin==this.xMax){
            		if(this.xMin==0.0D){
                		this.xMin=0.1D;
                		this.xMax=0.1D;
            		}
            		else{
                		if(this.xMin<0.0D){
                    			this.xMin=this.xMin*1.1D;
                		}
                		else{
                    			this.xMax=this.xMax*1.1D;
                		}
            		}
        	}

        	if(this.yMin==this.yMax){
            		if(this.yMin==0.0D){
                		this.yMin=0.1D;
                		this.yMax=0.1D;
            		}
            		else{
                		if(this.yMin<0.0D){
                    			this.yMin=this.yMin*1.1D;
                		}
                		else{
                    			this.yMax=this.yMax*1.1D;
                		}
            		}
        	}
    	}

    	// Convert offset value to a string and reformat if in E format
    	protected static String offsetString(double offset){
        	String stroffset = String.valueOf(offset);
        	String substr1="", substr2="", substr3="";
        	String zero ="0";
        	int posdot = stroffset.indexOf('.');
        	int posexp = stroffset.indexOf('E');

		    if(posexp==-1){
            		return stroffset;
        	}
        	else{
           		substr1 = stroffset.substring(posexp+1);
           		int n = Integer.parseInt(substr1);
           		substr1 = stroffset.substring(0,posexp);
           		if(n>=0){
                		for(int i=0; i<n; i++){
                			substr1 = substr1 + zero;
                		}
                		return substr1;
           		}
           		else{
                		substr2 = substr1.substring(0, posdot+1);
                		substr3 = substr1.substring(posdot+1);
                		for(int i=0; i<-n; i++){
                			substr2 = substr1 + zero;
                		}
                		substr2 = substr2 + substr3;
                		return substr2;
           		}
        	}
    	}

    	// check whether point in line segment is to be drawn
    	public boolean printCheck(boolean trim, int xoldpoint, int xnewpoint, int yoldpoint, int ynewpoint){

        	boolean btest2=true;

        	if(trim){
            		if(xoldpoint<xBot)btest2=false;
            		if(xoldpoint>xTop)btest2=false;
	            	if(xnewpoint<xBot)btest2=false;
        	    	if(xnewpoint>xTop)btest2=false;
            		if(yoldpoint>yBot)btest2=false;
	            	if(yoldpoint<yTop)btest2=false;
        	    	if(ynewpoint>yBot)btest2=false;
            		if(ynewpoint<yTop)btest2=false;
        	}

        	return btest2;
    	}

    	// Draw graph
    	public void graph(Graphics g){

        	// Set font type and size
        	g.setFont(new Font("serif", Font.PLAIN, this.fontSize));
        	FontMetrics fm = g.getFontMetrics();

        	// calculation of all graphing parameters and data scaling
        	axesScaleOffset();

        	// Draw title, legends and axes
        	String xoffstr = offsetString(xOffset);
        	String yoffstr = offsetString(yOffset);
        	String bunit1 = "  /( ";
        	String bunit2 = " )";
        	String bunit3 = "  / ";
        	String bunit4 = " ";
        	String bunit5 = " x 10";
        	String bunit6 = "10";
        	String nounit = " ";
        	String xbrack1 = bunit1;
        	String xbrack2 = bunit2;
        	String xbrack3 = bunit5;
        	if(this.xFac==0){
        	    xbrack1 = bunit3;
        	    xbrack2 = "";
        	    xbrack3 = "";
            }
            String ybrack1 = bunit1;
        	String ybrack2 = bunit2;
        	String ybrack3 = bunit5;
        	if(this.yFac==0){
        	    ybrack1 = bunit3;
        	    ybrack2 = "";
        	    ybrack3 = "";
            }
         	if(noXunits){
        	    if(xFac==0){
            		xbrack1=nounit;
            		xbrack2=nounit;
            		xbrack3=nounit;
            	}
            	else{
            	    xbrack1=bunit3;
            		xbrack2=bunit4;
            		xbrack3=bunit6;
        		}
        	}
        	if(noYunits){
        	    if(yFac==0){
            		ybrack1=nounit;
            		ybrack2=nounit;
            		ybrack3=nounit;
            	}
            	else{
            	    ybrack1=bunit3;
            	    ybrack2=bunit4;
            	    ybrack3=bunit6;
            	}
        	}

        	double xLen=xTop-xBot;
        	double yLen=yBot-yTop;

        	// Print title
        	String sp = " + ", sn = " - ";
        	String ss=sn;
        	g.drawString(this.graphTitle+" ", 15,15);
        	g.drawString(this.graphTitle2+" ", 15,35);
        	if(this.xOffset<0){
            		ss=sp;
            		xOffset=-xOffset;
        	}

        	// Print legends
	        int sw=0;
        	String ssx="", ssy="", sws1="", sws2="";
        	if(this.xFac==0 && this.xOffset==0){
                	g.drawString(this.xAxisLegend+xbrack1+this.xAxisUnits+xbrack2, xBot-4,yBot+32);
        	}
        	else{
            		if(this.xOffset==0){
                		ssx = this.xAxisLegend + xbrack1 + this.xAxisUnits + xbrack3;
	                	sw = fm.stringWidth(ssx);
        	        	g.drawString(ssx, xBot-4,yBot+42);
                		sws1=String.valueOf(-this.xFac-1);
                		g.drawString(sws1, xBot-4+sw+1,yBot+32);
                		sw += fm.stringWidth(sws1);
                		g.drawString(xbrack2, xBot-4+sw+1,yBot+42);
            		}
            		else{
                		if(this.xFac==0){
                    			g.drawString(this.xAxisLegend + ss + xoffstr + xbrack1+this.xAxisUnits+xbrack2, xBot-4,yBot+30);
                		}
                		else{
                    			ssx = this.xAxisLegend + ss + xoffstr + xbrack1+this.xAxisUnits+xbrack3;
                    			sw = fm.stringWidth(ssx);
                    			g.drawString(ssx, xBot-4,yBot+37);
                    			sws1 = String.valueOf(-this.xFac-1);
                    			g.drawString(sws1, xBot-4+sw+1,yBot+32);
                    			sw += fm.stringWidth(sws1);
                    			g.drawString(xbrack2, xBot-4+sw+1,yBot+37);
                		}
            		}
        	}

        	ss=sn;
        	if(yOffset<0){
            		ss=sp;
            		yOffset=-yOffset;
        	}

        	if(yFac==0 && yOffset==0){
            		g.drawString(this.yAxisLegend+" ", 15,yTop-25);
            		g.drawString(ybrack1+this.yAxisUnits+ybrack2, 15,yTop-10);
        	}
        	else{
            		if(yOffset==0){
                		g.drawString(this.yAxisLegend, 15,yTop-35);
	                	sws1 = ybrack1+this.yAxisUnits + ybrack3;
        	        	g.drawString(sws1, 15,yTop-15);
                		sw = fm.stringWidth(sws1);
                		sws2=String.valueOf(-this.yFac-1);
	                	g.drawString(sws2, 15+sw+1,yTop-20);
        	        	sw += fm.stringWidth(sws2);
                		g.drawString(ybrack2, 15+sw+1,yTop-15);
           		}
            		else{
                		if(yFac==0){
                    			g.drawString(this.yAxisLegend + ss + yoffstr, 15,yTop-25);
                    			g.drawString(ybrack1+this.yAxisUnits+ybrack2, 15,yTop-10);
                		}
                		else{
		                    ssy = this.yAxisLegend + ss + yoffstr;
        			  	    g.drawString(ssy, 15,yTop-35);
 			                sws1 = ybrack1+this.yAxisUnits + ybrack3;
                    			g.drawString(sws1, 15,yTop-15);
                    			sw = fm.stringWidth(sws1);
	                    		sws2=String.valueOf(-this.yFac-1);
        	            		g.drawString(sws2, 15+sw+1,yTop-20);
                	    		sw += fm.stringWidth(sws2);
                		    	g.drawString(ybrack2, 15+sw+1,yTop-15);
                		}
            		}
        	}

	        // Draw axes
	        int zdif=0, zold=0, znew=0, zzer=0;
        	double csstep=0.0D;
	        double xdenom=(xHigh-xLow);
        	double ydenom=(yHigh-yLow);

        	g.drawLine(xBot, yBot, xTop, yBot);
        	g.drawLine(xBot, yTop, xTop, yTop);
        	g.drawLine(xBot, yBot, xBot, yTop);
        	g.drawLine(xTop, yBot, xTop, yTop);


        	// Draw zero lines if drawn axes are not at zero and a zero value lies on an axis
        	if(xZero){
            		zdif=8;
            		zzer=xBot+(int)(((0.0-xLow)/xdenom)*xLen);
            		g.drawLine(zzer,yTop,zzer,yTop+8);
            		g.drawLine(zzer,yBot,zzer,yBot-8);
            		zold=yTop;
            		while(zold+zdif<yBot){
                		znew=zold+zdif;
                		g.drawLine(zzer, zold, zzer, znew);
                		zold=znew+zdif;
            		}
        	}

        	if(yZero){
            		zdif=8;
            		zzer=yBot-(int)(((0.0-yLow)/ydenom)*yLen);
            		g.drawLine(xBot,zzer,xBot+8,zzer);
            		g.drawLine(xTop,zzer,xTop-8,zzer);
            		zold=xBot;
            		while(zold+zdif<xTop){
                		znew=zold+zdif;
                		g.drawLine(zold, zzer, znew, zzer);
                		zold=znew+zdif;
            		}
        	}

         	// Draw tick marks and axis numbers
            int xt=0;
        	//double xtep=(double)(xTop-xBot)/((double)(this.xTicks-1));
        	for(int ii=0; ii<this.xTicks; ii++)
        	{
        	        xt=xBot+(int)(((this.xAxisNo[ii]-xLow)/xdenom)*xLen);
              		g.drawLine(xt,yBot,xt,yBot-8);
            		g.drawLine(xt,yTop,xt,yTop+8);
            		g.drawString(xAxisChar[ii]+" ",xt-4,yBot+18);
        	}

        	int yt=0;
        	int yCharLenMax=yAxisChar[0].length();
        	for(int ii=1; ii<this.yTicks; ii++)if(yAxisChar[ii].length()>yCharLenMax)yCharLenMax=yAxisChar[ii].length();
        	int shift = (yCharLenMax-3)*5;
        	double ytep=(double)(-yTop+yBot)/((double)(this.yTicks-1));
        	for(int ii=0; ii<this.yTicks; ii++)
        	{
            		yt=yBot-(int)Math.round(ii*ytep);
            		yt=yBot-(int)(((this.yAxisNo[ii]-yLow)/ydenom)*yLen);
            		g.drawLine(xBot,yt,xBot+8,yt);
            		g.drawLine(xTop,yt,xTop-8,yt);
            		g.drawString(yAxisChar[ii]+" ",xBot-30-shift,yt+4);
        	}

        	int dsum=0; // dashed line counter
        	boolean dcheck=true; // dashed line check

        	// Draw curves
        	int kk=0;
		    int xxp=0, yyp=0, yype=0;
		    int xoldpoint=0, xnewpoint=0, yoldpoint=0, ynewpoint=0;
        	int ps=0, psh=0, nxpoints=0;
        	double ics[]= new double[niPoints];
        	boolean btest2=true;

        	for(int i=0; i<this.nCurves; i++){
            		// cubic spline interpolation option
            		nxpoints=this.nPoints[i];
            		double xcs[]= new double[nxpoints];
            		double ycs[]= new double[nxpoints];

     	       		if(lineOpt[i]==1 || lineOpt[i]==2){
                		CubicSpline cs = new CubicSpline(this.nPoints[i]);
                		for(int ii=0; ii<nxpoints; ii++){
                    			xcs[ii]=this.data[kk][ii];
                		}
                		csstep=(xcs[nxpoints-1]-xcs[0])/(niPoints-1);
                		ics[0]=xcs[0];
                		for(int ii=1; ii<niPoints; ii++){
                    			ics[ii]=ics[ii-1]+csstep;
                		}
                		ics[niPoints-1] = xcs[nxpoints-1];
                 		for(int ii=0; ii<nxpoints; ii++){
                    			ycs[ii]=this.data[kk+1][ii];
                		}

                		cs.resetData(xcs, ycs);
                		cs.calcDeriv();
                		xoldpoint=xBot+(int)(((xcs[0]-xLow)/xdenom)*xLen);
                		yoldpoint=yBot-(int)(((ycs[0]-yLow)/ydenom)*yLen);
                		for(int ii=1; ii<niPoints; ii++){
                     			xnewpoint=xBot+(int)(((ics[ii]-xLow)/xdenom)*xLen);
                    			ynewpoint=yBot-(int)(((cs.interpolate(ics[ii])-yLow)/ydenom)*yLen);
                    			btest2=printCheck(trimOpt[i], xoldpoint, xnewpoint, yoldpoint, ynewpoint);
                    			if(btest2){
                        			if(this.lineOpt[i]==2){
                            				dsum++;
                            				if(dsum>dashLength[i]){
                                				dsum=0;
                                				if(dcheck){
                                    					dcheck=false;
                                				}
								                else{
                                    					dcheck=true;
                                				}
                            				}
                        			}
                        			if(dcheck)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			}
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}

            		if(lineOpt[i]==-1 || lineOpt[i]==-2){
                		CubicSpline cs = new CubicSpline(this.nPoints[i]);
                		for(int ii=0; ii<nxpoints; ii++){
                    			xcs[ii]=this.data[kk][ii];
                		}
                		for(int ii=0; ii<nxpoints; ii++){
                    			ycs[ii]=this.data[kk+1][ii];
                		}
                		csstep=(ycs[nxpoints-1]-ycs[0])/(niPoints-1);
                		ics[0]=ycs[0];
                		for(int ii=1; ii<niPoints; ii++){
                    			ics[ii]=ics[ii-1]+csstep;
                		}
                		ics[niPoints-1] = ycs[nxpoints-1];

                		cs.resetData(ycs, xcs);
                		cs.calcDeriv();
                		xoldpoint=xBot+(int)(((xcs[0]-xLow)/xdenom)*xLen);
                		yoldpoint=yBot-(int)(((ycs[0]-yLow)/ydenom)*yLen);
                		for(int ii=1; ii<niPoints; ii++){
                    			ynewpoint=yBot+(int)(((ics[ii]-yLow)/ydenom)*yLen);
                    			xnewpoint=xBot-(int)(((cs.interpolate(ics[ii])-xLow)/xdenom)*xLen);
                    			btest2=printCheck(trimOpt[i], xoldpoint, xnewpoint, yoldpoint, ynewpoint);
                    			if(btest2){
                        			if(this.lineOpt[i]==2){
                            				dsum++;
                            				if(dsum>dashLength[i]){
                                				dsum=0;
                                				if(dcheck){
                                    					dcheck=false;
                                				}
								else{
                                    					dcheck=true;
                                				}
                            				}
                        			}
                        			if(dcheck)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			}
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}

	            	if(lineOpt[i]==3){
                		// Join points option
                		dsum=0;
                		dcheck=true;
                		xoldpoint=xBot+(int)((((this.data[kk][0])-xLow)/xdenom)*xLen);
                		yoldpoint=yBot-(int)((((this.data[kk+1][0])-yLow)/ydenom)*yLen);
                		for(int ii=1; ii<nxpoints; ii++){
                    			xnewpoint=xBot+(int)((((this.data[kk][ii])-xLow)/xdenom)*xLen);
                    			ynewpoint=yBot-(int)((((this.data[kk+1][ii])-yLow)/ydenom)*yLen);
                    			btest2=printCheck(trimOpt[i], xoldpoint, xnewpoint, yoldpoint, ynewpoint);
                    			if(btest2)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}

                    if(lineOpt[i]==4){

                	    // Join points with dotted line option

                		dsum=0;
                		dcheck=true;
                		xoldpoint=xBot+(int)((((this.data[kk][0])-xLow)/xdenom)*xLen);
                		yoldpoint=yBot-(int)((((this.data[kk+1][0])-yLow)/ydenom)*yLen);
                		for(int ii=1; ii<nxpoints; ii++){
                		        dsum++;
                		        if(dsum>dashLength[i]){
                                    dsum=0;
                                    if(dcheck){
                                        dcheck=false;
                                    }
								    else{
                                        dcheck=true;
                                    }
                                }
                    			xnewpoint=xBot+(int)((((this.data[kk][ii])-xLow)/xdenom)*xLen);
                    			ynewpoint=yBot-(int)((((this.data[kk+1][ii])-yLow)/ydenom)*yLen);
                    			btest2=printCheck(trimOpt[i], xoldpoint, xnewpoint, yoldpoint, ynewpoint);
                    			if(dcheck)g.drawLine(xoldpoint,yoldpoint,xnewpoint,ynewpoint);
                    			xoldpoint=xnewpoint;
                    			yoldpoint=ynewpoint;
                		}
            		}


            		// Plot points
            		if(pointOpt[i]>0){
                		for(int ii=0; ii<nxpoints; ii++){
                    			ps=this.pointSize[i];
                    			psh=ps/2;
                     			xxp=xBot+(int)(((this.data[kk][ii]-xLow)/xdenom)*xLen);
                    			yyp=yBot-(int)(((this.data[kk+1][ii]-yLow)/ydenom)*yLen);
                    			switch(pointOpt[i]){
                        			case 1: g.drawOval(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 2: g.drawRect(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 3: g.drawLine(xxp-psh, yyp, xxp, yyp+psh);
                            				g.drawLine(xxp, yyp+psh, xxp+psh, yyp);
                            				g.drawLine(xxp+psh, yyp, xxp, yyp-psh);
                            				g.drawLine(xxp, yyp-psh, xxp-psh, yyp);
                            				break;
                          			case 4: g.fillOval(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 5: g.fillRect(xxp-psh, yyp-psh, ps, ps);
                            				break;
                        			case 6: for(int jj=0; jj<psh; jj++)g.drawLine(xxp-jj, yyp-psh+jj, xxp+jj, yyp-psh+jj);
                            				for(int jj=0; jj<=psh; jj++)g.drawLine(xxp-psh+jj, yyp+jj, xxp+psh-jj, yyp+jj);
                            				break;
                        			case 7: g.drawLine(xxp-psh, yyp-psh, xxp+psh, yyp+psh);
                            				g.drawLine(xxp-psh, yyp+psh, xxp+psh, yyp-psh);
                            				break;
                        			case 8: g.drawLine(xxp-psh, yyp, xxp+psh, yyp);
                            				g.drawLine(xxp, yyp+psh, xxp, yyp-psh);
                            				break;
                        			default:g.drawLine(xxp-psh, yyp-psh, xxp+psh, yyp+psh);
                            				g.drawLine(xxp-psh, yyp+psh, xxp+psh, yyp-psh);
                            				break;
                    			}

					if(this.errorBar[i]){
                        			yype=yBot-(int)(((errors[i][ii]-yLow)/ydenom)*yLen);
                        			g.drawLine(xxp, yyp, xxp, yype);
                        			g.drawLine(xxp-4, yype, xxp+4, yype);
                        			yype=2*yyp-yype;
                        			g.drawLine(xxp, yyp, xxp, yype);
                        			g.drawLine(xxp-4, yype, xxp+4, yype);
                    			}
                		}
            		}
            		kk+=2;
        	}
    	}

        // Return the serial version unique identifier
        public static long getSerialVersionUID(){
            return Plot.serialVersionUID;
        }
}
