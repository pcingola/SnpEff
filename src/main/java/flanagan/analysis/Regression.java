/*
*   Class Regression
*
*   Contains methods for simple linear regression
*   (straight line), for multiple linear regression,
*   for fitting data to a polynomial and for non-linear
*   regression (Nelder and Mead Simplex method) for both user
*   supplied functions and for a wide range of standard functions
*
*   The sum of squares function needed by the non-linear regression methods
*   non-linear regression methods is supplied by means of the interfaces,
*   RegressionFunction or RegressionFunction2
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	February 2002
*   MODIFIED:   5-6 July 2004,
*               7 January 2006,  28 July 2006, 9 August 2006, 4 November 2006, 21 November 2006, 21 December 2006,
*               14 April 2007, 9 June 2007, 25 July 2007, 23/24 August 2007, 14 September 2007, 28 December 2007,
*               18-26 March 2008, 7 April 2008, 27 April 2008, 10/12/19 May 2008,  28 July 2008, 29 August 2008, 5 September 2008, 6 October 2008, 
*               13-15 October 2009, 13 November 2009, 10 December 2009, 20 December 2009, 
*               12 January 2010, 18-25 May 2010, 9 July 2010, 10-16 August 2010, 21-29 October 2010, 2-7 November 2010, 
*               2 January 2011, 20-31 January 2011, 2-7 February 2011, 21-27 February 2011, 30 March 2011, 13 November 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
* Copyright (c) 2002 - 2011 Michael Thomas Flanagan
*
* PERMISSION TO COPY:
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


package flanagan.analysis;

import java.util.*;
import javax.swing.JOptionPane;
import flanagan.math.*;
import flanagan.io.*;
import flanagan.plot.Plot;
import flanagan.plot.PlotGraph;
import flanagan.analysis.*;
import flanagan.circuits.Impedance;
import flanagan.interpolation.CubicSpline;


// Regression class
public class Regression{

    protected int nData0=0;        		            // number of y data points inputted (in a single array if multiple y arrays)
    protected int nData=0;                          // number of y data points (nData0 times the number of y arrays)
    protected int nXarrays=1;     		            // number of x arrays
    protected int nYarrays=1;     		            // number of y arrays
    protected int nParam=0;       		            // number of unknown parameters to be estimated
                                    		        //  multiple linear (a + b.x1 +c.x2 + . . ., = nXarrays + 1
                                    		        //  polynomial fitting; = polynomial degree + 1
                                    		        //  generalised linear; = nXarrays
                                    		        //  simplex = no. of parameters to be estimated
    protected int degreesOfFreedom=0; 		        // degrees of freedom = nData - nParam
    protected double[][]  xData=null;      	        // x  data values
    protected double[]    yData=null;      	        // y  data values
    protected double[]    yCalc=null;      	        // calculated y values using the regrssion coefficients
    protected double[]    weight=null;     	        // weighting factors
    protected double[]    residual=null;   	        // residuals
    protected double[]    residualW=null;  	        // weighted residuals
    protected boolean     weightOpt=false;          // weighting factor option
                                                   	// = true; weights supplied
                                                   	// = false; weigths set to unity in regression
                                                   	//          average error used in statistacal methods
                                                   	// if any weight[i] = zero,
                                                   	//                    weighOpt is set to false and
                                                   	//                    all weights set to unity
    protected int weightFlag=0;                     // weighting flag  - weightOpt = false, weightFlag = 0;  weightOpt = true, weightFlag = 1
    protected String[] weightWord = {"", "Weighted "};

    protected double[]  best = null;                // best estimates vector of the unknown parameters
    protected double[]  bestSd =null; 	            // standard deviation estimates of the best estimates of the unknown parameters
	protected double[]  pseudoSd = null;            // Pseudo-nonlinear sd
    protected double[]  tValues = null;             // t-values of the best estimates
    protected double[]  pValues = null;             // p-values of the best estimates
    protected double fixedInterceptL = 0.0;         // Fixed intercept (linear regression)
    protected double fixedInterceptP = 0.0;         // Fixed intercept (polynomial fitting)

    protected double  yMean=Double.NaN;             // mean of y data
    protected double  yWeightedMean=Double.NaN;     // weighted mean of y data
    protected double  chiSquare=Double.NaN;         // chi  square (observed-calculated)^2/variance; weighted error sum of squares
    protected double  reducedChiSquare=Double.NaN;  // reduced chi square
    protected double  sumOfSquaresError=Double.NaN; // Sum of the squares of the residuals; unweighted error sum of squares
    protected double  sumOfSquaresTotal=Double.NaN; // Total sum of the squares
    protected double  sumOfSquaresRegrn=Double.NaN; // Regression sum of the squares

    protected double  lastSSnoConstraint=0.0D;      // Last sum of the squares of the residuals with no constraint penalty
	protected double[][]  covar=null;               // Covariance matrix
	protected double[][]  corrCoeff=null;           // Correlation coefficient matrix
	protected double xyR = Double.NaN;              // correlation coefficient between x and y data (y = a + b.x only)
    protected double yyR = Double.NaN;              // correlation coefficient between y calculted and y data (all regressions)
    protected double multR = Double.NaN;            // coefficient of determination
    protected double adjustedR = Double.NaN;        // adjusted coefficient of determination
    protected double multipleF = Double.NaN;        // coefficient of determination: F-ratio
    protected double multipleFprob = Double.NaN;    // coefficient of determination: F-ratio probability

	protected String[] paraName = null;   	        // names of parameters, eg, mean, sd; c[0], c[1], c[2] . . .
	protected int prec = 4;               	        // number of places to which double variables are truncated on output to text files
	protected int field = 13;             	        // field width on output to text files

    protected int lastMethod=-1;          	        // code indicating the last regression procedure attempted
                                                 	// = 0 multiple linear regression, y = a + b.x1 +c.x2 . . .
                                                 	// = 1 polynomial fitting, y = a +b.x +c.x^2 . . .
                                                	// = 2 generalised multiple linear y = a.f1(x) + b.f2(x) . . .
                                                	// = 3 Nelder and Mead simplex
                                                	// = 4 Fit to a Gaussian distribution (see also 38 below)
                                                 	// = 5 Fit to a Lorentzian distribution
                                                    // = 6 Fit to a Poisson distribution
                                                	// = 7 Fit to a Two Parameter Gumbel distribution (minimum order statistic)
                                                	// = 8 Fit to a Two Parameter Gumbel distribution (maximum order statistic)
                                                	// = 9 Fit to a One Parameter Gumbel distribution (minimum order statistic)
                                                	// = 10 Fit to One Parameter Gumbel distribution (maximum order statistic)
                                                	// = 11 Fit to a Standard Gumbel distribution (minimum order statistic)
                                           	        // = 12 Fit to a Standard Gumbel distribution (maximum order statistic)
                                                    // = 13 Fit to a Three parameter Frechet distribution
                                                    // = 14 Fit to a Two Parameter Frechet distribution
                                                    // = 15 Fit to a Standard Frechet distribution
                                                    // = 16 Fit to a Three parameter Weibull distribution
                                                    // = 17 Fit to a Two Parameter Weibull distribution
                                                    // = 18 Fit to a Standard Weibull distribution
                                                    // = 19 Fit to a Two Parameter Exponential distribution
                                                    // = 20 Fit to a One Parameter Exponential distribution
                                                    // = 21 Fit to a Standard Parameter Exponential distribution
                                                    // = 22 Fit to a Rayleigh distribution
                                                    // = 23 Fit to a Two Parameter Pareto distribution
                                                    // = 24 Fit to a One Parameter Pareto distribution
                                                    // = 25 Fit to a Sigmoidal Threshold Function
                                                    // = 26 Fit to a rectangular Hyperbola
                                                    // = 27 Fit to a scaled Heaviside Step Function
                                                    // = 28 Fit to a Hills/Sips Sigmoid
                                                    // = 29 Fit to a Shifted Pareto distribution
                                                    // = 30 Fit to a Logistic distribution
                                                    // = 31 Fit to a Beta distribution - [0, 1] interval
                                                    // = 32 Fit to a Beta distribution - [min, max] interval
                                                    // = 33 Fit to a Three Parameter Gamma distribution
                                                    // = 34 Fit to a Standard Gamma distribution
                                                    // = 35 Fit to an Erlang distribution
                                                    // = 36 Fit to a two parameter log-normal distribution
                                                    // = 37 Fit to a three parameter log-normal distribution
                                                    // = 38 Fit to a Gaussian distribution  [allows fixed p-arameters] (see also 4 above)
                                                    // = 39 Fit to a EC50 dose response curve (four parameter logistic)
                                                    // = 40 Fit to a EC50 dose response curve - top and bottom fixed
                                                    // = 41 Fit to a EC50 dose response curve (four parameter logistic) - bottom constrained
                                                    // = 42 Five parameter logistic function - top and bottom fixed (see also method 51)
                                                    // = 43 Fit to a simple exponential, A.exp(Bx)
                                                    // = 44 Fit to multiple exponentials
                                                    // = 45 Fit to a A(1 - exp(Bx))
                                                    // = 46 Fit to a constant
                                                    // = 47 Linear fit with fixed intercept
                                                    // = 48 Polynomial fit with a fixed intercept
                                                    // = 49 Multiple Gaussians
                                                    // = 50 Non-integer polynomial
                                                    // = 51 Five parameter logistic function (see also method 42 above)
                                                    // = 52 Shifted rectangular hyperbola

    protected boolean bestPolyFlag = false;         // = true if bestPolynomial called
    protected int bestPolynomialDegree = 0;         // degree of best polynomial fit
    protected double fProbSignificance = 0.05;      // significance level used in F-test in bestPolynomial method
    protected ArrayList<Object> bestPolyArray = new ArrayList<Object>();   // array storing history of bestPolynomial search pathway

    protected boolean userSupplied = true;          // = true  - user supplies the initial estimates for non-linear regression
                                                    // = false - the initial estimates for non-linear regression are calculated internally

    protected double kayValue = 0.0D;               // rate parameter value in Erlang distribution (method 35)

    protected boolean frechetWeibull = true;        // Frechet Weibull switch - if true Frechet, if false Weibull
    protected boolean linNonLin = true;             // if true linear method, if false non-linear method
    protected boolean trueFreq = false;   	        // true if xData values are true frequencies, e.g. in a fit to Gaussian
                                        	        // false if not
                                        	        // if true chiSquarePoisson (see above) is also calculated
    protected String xLegend = "x axis values";     // x axis legend in X-Y plot
    protected String yLegend = "y axis values";     // y axis legend in X-Y plot
    protected String graphTitle = " ";              // user supplied graph title
    protected String graphTitle2 = " ";             // second line graph title
    protected boolean legendCheck = false;          // = true if above legends overwritten by user supplied legends
    protected boolean supressPrint = false;         // = true if print results is to be supressed
    protected boolean supressYYplot= false;         // = true if plot of experimental versus calculated is to be supressed
    protected boolean supressErrorMessages= false;  // = true if some designated error messages are to be supressed

    // Non-linear members
    protected boolean nlrStatus=true; 	            // Status of non-linear regression on exiting regression method
                                		            // = true  -  convergence criterion was met
                                		            // = false -  convergence criterion not met - current estimates returned
    protected int scaleOpt=0;     		            //  if = 0; no scaling of initial estimates
                                		            //  if = 1; initial simplex estimates scaled to unity
                                		            //  if = 2; initial estimates scaled by user provided values in scale[]
                                		            //  (default = 0)
    protected double[] scale = null;  	            // values to scale initial estimate (see scaleOpt above)
    protected boolean zeroCheck = false; 	        // true if any best estimate value is zero
                                       		        // if true the scale factor replaces the best estimate in numerical differentiation
    protected boolean penalty = false; 	            // true if single parameter penalty function is included
    protected boolean sumPenalty = false; 	        // true if multiple parameter penalty function is included
    protected int nConstraints = 0; 		        // number of single parameter constraints
    protected int nSumConstraints = 0; 		        // number of multiple parameter constraints
    protected int maxConstraintIndex = -1;          // maximum index of constrained parameter/s
    protected double constraintTolerance = 1e-4;    // tolerance in constraining parameter/s to a fixed value
    protected ArrayList<Object> penalties = new ArrayList<Object>();        // constrant method index,
                                                                            //  number of single parameter constraints,
                                                                            //  then repeated for each constraint:
                                                                            //  penalty parameter index,
                                                                            //  below or above constraint flag,
                                                                            //  constraint boundary value
    protected ArrayList<Object> sumPenalties = new ArrayList<Object>();     // constraint method index,
                                                                            //  number of multiple parameter constraints,
                                                                            //  then repeated for each constraint:
                                                                            //  number of parameters in summation
                                                                            //  penalty parameter indices,
                                                                            //  summation signs
                                                                            //  below or above constraint flag,
                                                                            //  constraint boundary value
    protected int[] penaltyCheck = null;  	        // = -1 values below the single constraint boundary not allowed
                                        	        // = +1 values above the single constraint boundary not allowed
    protected int[] sumPenaltyCheck = null;  	    // = -1 values below the multiple constraint boundary not allowed
                                        	        // = +1 values above the multiple constraint boundary not allowed
    protected double penaltyWeight = 1.0e30;        // weight for the penalty functions
    protected int[] penaltyParam = null;   	        // indices of paramaters subject to single parameter constraint
    protected int[][] sumPenaltyParam = null;       // indices of paramaters subject to multiple parameter constraint
    protected double[][] sumPlusOrMinus = null;     // valueall before each parameter in multiple parameter summation
    protected int[] sumPenaltyNumber = null;        // number of paramaters in each multiple parameter constraint

    protected double[] constraints = null; 	        // single parameter constraint values
    protected double[] sumConstraints = null;       // multiple parameter constraint values
    protected int constraintMethod = 0;             // constraint method number
                                                    // =0: cliff to the power two (only method at present)

    protected boolean scaleFlag = true;             // if true ordinate scale factor, Ao, included as unknown in fitting to special functions
                                                    // if false Ao set to unity (default value) or user provided value (in yScaleFactor)
    protected double yScaleFactor = 1.0D;           // y axis factor - set if scaleFlag (above) = false
    protected int nMax = 3000;    		            // Nelder and Mead simplex maximum number of iterations allowed
    protected int minIter = 300;    		        // Nelder and Mead simplex minimum number of iterations required
    protected int nIter = 0;      		            // Nelder and Mead simplex number of iterations performed
    protected int konvge = 3;     		            // Nelder and Mead simplex number of restarts allowed
    protected int kRestart = 0;       	            // Nelder and Mead simplex number of restarts taken
    protected double fMin = -1.0D;    	            // Nelder and Mead simplex minimum value
    protected double fTol = 1e-9;     	            // Nelder and Mead simplex convergence tolerance factor
    protected double rCoeff = 1.0D;   	            // Nelder and Mead simplex reflection coefficient
    protected double eCoeff = 2.0D;   	            // Nelder and Mead simplex extension coefficient
    protected double cCoeff = 0.5D;   	            // Nelder and Mead simplex contraction coefficient
    protected double[] startH = null; 	            // Nelder and Mead simplex unscaled initial estimates
    protected double[] stepH = null;   	            // Nelder and Mead simplex unscaled initial step values
    protected double[] startSH = null; 	            // Nelder and Mead simplex scaled initial estimates
    protected double[] stepSH = null;   	        // Nelder and Mead simplex scaled initial step values
    protected double dStep = 0.5D;    	            // Nelder and Mead simplex default step value
    protected double[][] grad = null; 	            // Non-linear regression gradients
	protected double delta = 1e-4;    	            // Fractional step in numerical differentiation
	protected double deltaBeale = 1e-3;    	        // Fractional step in calculation of Beale's nonlinearity

	protected boolean invertFlag=true; 	            // Hessian Matrix ('linear' non-linear statistics) check
	                                 	            //   true matrix successfully inverted, false inversion failed
	protected boolean posVarFlag=true; 	            // Hessian Matrix ('linear' non-linear statistics) check
	                                 	            //   true - all variances are positive; false - at least one is negative
    protected int minTest = 0;    		            // Nelder and Mead minimum test
                                		            //  = 0; tests simplex sd < fTol
                                		            //  = 1; tests reduced chi suare or sum of squares < mean of abs(y values)*fTol
    protected double simplexSd = 0.0D;    	        // simplex standard deviation
    protected boolean statFlag = true;    	        // if true - statistical method called
                                        	        // if false - no statistical analysis
    protected boolean plotOpt = true;               // if true - plot of calculated values is cubic spline interpolation between the calculated values
                                                    // if false - calculated values linked by straight lines (accomodates Poiwsson distribution plots)
    protected boolean multipleY = false;            // = true if y variable consists of more than set of data each needing a different calculation in RegressionFunction
                                                    // when set to true - the index of the y value is passed to the function in Regression function

    protected boolean ignoreDofFcheck = false;      // when set to true, the check on whether degrees of freedom are greater than zero is ignored

    protected double[] values = null;               // values entered into gaussianFixed
    protected boolean[] fixed = null;               // true if above values[i] is fixed, false if it is not

    protected int nGaussians = 0;                   // Number of Gaussian distributions in multiple Gaussian fitting
    protected double[] multGaussFract = null;       // Best estimates of multiple Gaussian fractional contributions
    protected double[] multGaussFractErrors = null; // Errors in the estmated of multiple Gaussian fractional contributions
    protected double[] multGaussCoeffVar = null;    // Coefficients of variation of multiple Gaussian fractional contributions
    protected double[] multGaussTvalue = null;      // t-values for multiple Gaussian fractional contributions
    protected double[] multGaussPvalue = null;      // p-values for multiple Gaussian fractional contributions
    protected double multGaussScale = 1.0;          // Scale factor for multiple Gaussian fractional contributions
    protected double multGaussScaleError = 0.0;     // error in the scale factor for multiple Gaussian fractional contributions
    protected double multGaussScaleCoeffVar = 0.0;  // coeff. of var. in the scale factor for multiple Gaussian fractional contributions
    protected double multGaussScaleTvalue = 0.0;    // t-value of the scale factor for multiple Gaussian fractional contributions
    protected double multGaussScalePvalue = 0.0;    // p-value of the scale factor for multiple Gaussian fractional contributions

    protected boolean plotWindowCloseChoice = false;// if false:    closing window terminates program
                                                    // if true:     closing window does not terminate program

    protected double minimumY = 0;                  // minimum y-value
    protected double minimumYindex = 0;             // index of minimum y-value
    protected double maximumY = 0;                  // maximum y-value
    protected double maximumYindex = 0;             // index of maximum y-value
    protected double bottom = 0;                    // supplied logistic /EC50 curve bottom
    protected double top = 0;                       // supplied logistic /EC50 curve top

    protected double bottomS = 0.0;                 // calculated bottom value of sigmoid curves
    protected double bottomSindex = 0.0;            // index of the calculated bottom value of a sigmoid curve
    protected double topS = 0.0;                    // calculated top value of a sigmoid curve
    protected double topSindex = 0.0;               // index of the calculated top value of a sigmoid curve
    protected int midPointLowerIndex = 0;           // lower index of the mid point of a sigmoid curve
    protected int midPointUpperIndex = 0;           // upper index of the mid point of a sigmoid curve
    protected double midPointXvalue = 0.0;          // x-value of the mid point of a sigmoid curve
    protected double midPointYvalue = 0.0;          // y-value of the mid point of a sigmoid curve
    protected int directionFlag = 0;                // = 1,  gradient of a sigmoid curve is positive
                                                    // = -1, gradient of a sigmoid curve is negative

    protected double dDurbinWatson = Double.NaN;    // Durbin-Watson d statistic
    protected boolean dDurbinWatsonDone = false;    // = true when Durbin-Watson d calculated


    // HISTOGRAM CONSTRUCTION
    //  Tolerance used in including an upper point in last histogram bin when it is outside due to riunding erors
    protected static double histTol = 1.0001D;

    //CONSTRUCTORS

    // Default constructor - primarily facilitating the subclass ImpedSpecRegression
    public Regression(){
	}

    // Constructor with data with x as 2D array and weights provided
    public Regression(double[][] xData, double[] yData, double[] weight){

        int n=weight.length;
        this.nData0 = yData.length;
        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(Conv.copy(xData), Conv.copy(yData), Conv.copy(weight));
	}

	// Constructor with data with x and y as 2D arrays and weights provided
    public Regression(double[][] xxData, double[][] yyData, double[][] wWeight){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        double[][] xData = new double[nY1*nY2][nX1];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[ii][i] = xxData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }
        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

	// Constructor with data with x as 1D array and weights provided
    public Regression(double[] xxData, double[] yData, double[] weight){
        this.nData0 = yData.length;
        int n = xxData.length;
        int m = weight.length;
        double[][] xData = new double[1][n];
        for(int i=0; i<n; i++){
            xData[0][i]=xxData[i];
        }

        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(Conv.copy(xData), Conv.copy(yData), Conv.copy(weight));
	}

	// Constructor with data with x as 1D array and y as 2D array and weights provided
    public Regression(double[] xxData, double[][] yyData, double[][] wWeight){

        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }
        int n = xxData.length;
        if(n!=nY2)throw new IllegalArgumentException("x and y data lengths must be the same");
        double[][] xData = new double[1][nY1*n];
        ii=0;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                ii++;
            }
        }

        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

    // Constructor with data with x as 2D array and no weights provided
    public Regression(double[][] xData, double[] yData){
        this.nData0 = yData.length;
        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        this.weightFlag = 0;
        for(int i=0; i<n; i++)weight[i]=1.0D;

        setDefaultValues(Conv.copy(xData), Conv.copy(yData), weight);
	}

    // Constructor with data with x and y as 2D arrays and no weights provided
    public Regression(double[][] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        if(nY1!=nX1)throw new IllegalArgumentException("Multiple xData and yData arrays of different overall dimensions not supported");
        double[][] xData = new double[1][nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[0][ii] = xxData[i][j];
                ii++;
            }
        }

        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;
        this.weightFlag = 0;

        setDefaultValues(xData, yData, weight);
	}

    // Constructor with data with x as 1D array and no weights provided
    public Regression(double[] xxData, double[] yData){
        this.nData0 = yData.length;
        int n = xxData.length;
        double[][] xData = new double[1][n];
        double[] weight = new double[n];

        for(int i=0; i<n; i++)xData[0][i]=xxData[i];

        this.weightOpt=false;
        this.weightFlag = 0;
        for(int i=0; i<n; i++)weight[i]=1.0D;

        setDefaultValues(xData, Conv.copy(yData), weight);
	}

	// Constructor with data with x as 1D array and y as a 2D array and no weights provided
    public Regression(double[] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                ii++;
            }
        }

        double[][] xData = new double[1][nY1*nY2];
        double[] weight = new double[nY1*nY2];

        ii=0;
        int n = xxData.length;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                weight[ii]=1.0D;
                ii++;
            }
        }
        this.weightOpt=false;
        this.weightFlag = 0;

        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data as a single array that has to be binned
	// bin width and value of the low point of the first bin provided
    public Regression(double[] xxData, double binWidth, double binZero){
        double[][] data = Regression.histogramBins(Conv.copy(xxData), binWidth, binZero);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
            this.weightFlag = 1;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
            this.weightFlag = 0;
        }
        setDefaultValues(xData, yData, weight);
	}

	// Constructor with data as a single array that has to be binned
	// bin width provided
    public Regression(double[] xxData, double binWidth){
        double[][] data = Regression.histogramBins(Conv.copy(xxData), binWidth);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
            this.weightFlag = 1;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
            this.weightFlag = 0;
        }
        setDefaultValues(xData, yData, weight);
	}

    // Check entered weights for zeros.
    // If more than 40% are zero or less than zero, all weights replaced by unity
    // If less than 40% are zero or less than zero, the zero or negative weights are replaced by the average of their nearest neighbours
    protected double[] checkForZeroWeights(double[] weight){
        this.weightOpt=true;
        int nZeros = 0;
        int n=weight.length;

        for(int i=0; i<n; i++)if(weight[i]<=0.0)nZeros++;
        double perCentZeros = 100.0*(double)nZeros/(double)n;
        if(perCentZeros>40.0){
            System.out.println(perCentZeros + "% of the weights are zero or less; all weights set to 1.0");
            for(int i=0; i<n; i++)weight[i]=1.0D;
            this.weightOpt = false;
        }
        else{
            if(perCentZeros>0.0D){
                for(int i=0; i<n; i++){
                    if(weight[i]<=0.0){
                        if(i==0){
                            int ii=1;
                            boolean test = true;
                            while(test){
                                if(weight[ii]>0.0D){
                                    double ww = weight[0];
                                    weight[0] = weight[ii];
                                    System.out.println("weight at point " + i + ", " + ww + ", replaced by "+ weight[i]);
                                    test = false;
                                }
                            else{
                                    ii++;
                                }
                            }
                        }
                        if(i==(n-1)){
                            int ii=n-2;
                            boolean test = true;
                            while(test){
                                if(weight[ii]>0.0D){
                                    double ww = weight[i];
                                    weight[i] = weight[ii];
                                    System.out.println("weight at point " + i + ", " + ww + ", replaced by "+ weight[i]);
                                    test = false;
                                }
                                else{
                                    ii--;
                                }
                            }
                        }
                        if(i>0 && i<(n-2)){
                            double lower = 0.0;
                            double upper = 0.0;
                            int ii=i-1;
                            boolean test = true;
                            while(test){
                                if(weight[ii]>0.0D){
                                    lower = weight[ii];
                                    test = false;
                                }
                                else{
                                    ii--;
                                    if(ii==0)test = false;
                                }
                            }
                            ii=i+1;
                            test = true;
                            while(test){
                                if(weight[ii]>0.0D){
                                    upper = weight[ii];
                                    test = false;
                                }
                                else{
                                    ii++;
                                    if(ii==(n-1))test = false;
                                }
                            }
                            double ww = weight[i];
                            if(lower==0.0){
                                weight[i] = upper;
                            }
                            else{
                                if(upper==0.0){
                                    weight[i] = lower;
                                }
                                else{
                                    weight[i] = (lower + upper)/2.0;
                                }
                            }
                            System.out.println("weight at point " + i + ", " + ww + ", replaced by "+ weight[i]);
                        }
                    }
                }
            }
        }
        return weight;
	}

	// Enter data methods
	// Enter data with x as 2D array and weights provided
    public void enterData(double[][] xData, double[] yData, double[] weight){

        int n=weight.length;
        this.nData0 = yData.length;
        this.weightOpt=true;
        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

	// Enter data with x and y as 2D arrays and weights provided
    public void enterData(double[][] xxData, double[][] yyData, double[][] wWeight){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        double[][] xData = new double[nY1*nY2][nX1];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[ii][i] = xxData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }

        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

	// Enter data with x as 1D array and weights provided
    public void enterData(double[] xxData, double[] yData, double[] weight){
        this.nData0 = yData.length;
        int n = xxData.length;
        int m = weight.length;
        double[][] xData = new double[1][n];
        for(int i=0; i<n; i++){
            xData[0][i]=xxData[i];
        }

        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

	// Enter data with x as 1D array and y as 2D array and weights provided
    public void enterData(double[] xxData, double[][] yyData, double[][] wWeight){

        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        double[] weight = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                weight[ii] = wWeight[i][j];
                ii++;
            }
        }
        int n = xxData.length;
        if(n!=nY2)throw new IllegalArgumentException("x and y data lengths must be the same");
        double[][] xData = new double[1][nY1*n];
        ii=0;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                ii++;
            }
        }

        weight = this.checkForZeroWeights(weight);
        if(this.weightOpt)this.weightFlag = 1;
        this.setDefaultValues(xData, yData, weight);
	}

    // Enter data with x as 2D array and no weights provided
    public void enterData(double[][] xData, double[] yData){
        this.nData0 = yData.length;
        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;
        this.weightFlag = 0;
        setDefaultValues(xData, yData, weight);
	}

    // Enter data with x and y as 2D arrays and no weights provided
    public void enterData(double[][] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2 = yyData[0].length;
        this.nData0 = nY2;
        int nX1 = xxData.length;
        int nX2 = xxData[0].length;
        double[] yData = new double[nY1*nY2];
        double[][] xData = new double[nY1*nY2][nX1];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            int nX = xxData[i].length;
            if(nY!=nX)throw new IllegalArgumentException("multiple y arrays must be of the same length as the x array length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                xData[ii][i] = xxData[i][j];
                ii++;
            }
        }

        int n = yData.length;
        double[] weight = new double[n];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;
        this.weightFlag = 0;

        setDefaultValues(xData, yData, weight);
	}

    // Enter data with x as 1D array and no weights provided
    public void enterData(double[] xxData, double[] yData){
        this.nData0 = yData.length;
        int n = xxData.length;
        double[][] xData = new double[1][n];
        double[] weight = new double[n];

        for(int i=0; i<n; i++)xData[0][i]=xxData[i];

        this.weightOpt=false;
        for(int i=0; i<n; i++)weight[i]=1.0D;
        this.weightFlag = 0;

        setDefaultValues(xData, yData, weight);
	}

	// Enter data with x as 1D array and y as a 2D array and no weights provided
    public void enterData(double[] xxData, double[][] yyData){
        this.multipleY = true;
        int nY1 = yyData.length;
        this.nYarrays = nY1;
        int nY2= yyData[0].length;
        this.nData0 = nY2;
        double[] yData = new double[nY1*nY2];
        int ii=0;
        for(int i=0; i<nY1; i++){
            int nY = yyData[i].length;
            if(nY!=nY2)throw new IllegalArgumentException("multiple y arrays must be of the same length");
            for(int j=0; j<nY2; j++){
                yData[ii] = yyData[i][j];
                ii++;
            }
        }

        double[][] xData = new double[1][nY1*nY2];
        double[] weight = new double[nY1*nY2];

        ii=0;
        int n = xxData.length;
        for(int j=0; j<nY1; j++){
            for(int i=0; i<n; i++){
                xData[0][ii]=xxData[i];
                weight[ii]=1.0D;
                ii++;
            }
        }
        this.weightOpt=false;
        this.weightFlag = 0;

        this.setDefaultValues(xData, yData, weight);
	}

	// Enter data as a single array that has to be binned
	// bin width and value of the low point of the first bin provided
    public void enterData(double[] xxData, double binWidth, double binZero){
        double[][] data = Regression.histogramBins(xxData, binWidth, binZero);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
            this.weightFlag = 1;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
            this.weightFlag = 0;
        }
        setDefaultValues(xData, yData, weight);
	}

	// Enter data as a single array that has to be binned
	// bin width provided
    public void enterData(double[] xxData, double binWidth){
        double[][] data = Regression.histogramBins(xxData, binWidth);
        int n = data[0].length;
        this.nData0 = n;
        double[][] xData = new double[1][n];
        double[] yData = new double[n];
        double[] weight = new double[n];
        for(int i=0; i<n; i++){
            xData[0][i]=data[0][i];
            yData[i]=data[1][i];
        }
        boolean flag = setTrueFreqWeights(yData, weight);
        if(flag){
            this.trueFreq=true;
            this.weightOpt=true;
            this.weightFlag = 0;
        }
        else{
            this.trueFreq=false;
            this.weightOpt=false;
            this.weightFlag = 0;
        }
        setDefaultValues(xData, yData, weight);
	}


    protected static boolean setTrueFreqWeights(double[] yData, double[] weight){
        int nData=yData.length;
        boolean flag = true;
        boolean unityWeight=false;

        // Set all weights to square root of frequency of occurence
        for(int ii=0; ii<nData; ii++){
            weight[ii]=Math.sqrt(Math.abs(yData[ii]));
        }

        // Check for zero weights and take average of neighbours as weight if it is zero
        for(int ii=0; ii<nData; ii++){
            double last = 0.0D;
            double next = 0.0D;
            if(weight[ii]==0){
                // find previous non-zero value
                boolean testLast = true;
                int iLast = ii - 1;
                while(testLast){
                    if(iLast<0){
                        testLast = false;
                    }
                    else{
                        if(weight[iLast]==0.0D){
                            iLast--;
                        }
                        else{
                            last = weight[iLast];
                            testLast = false;
                        }
                    }
                }

                // find next non-zero value
                boolean testNext = true;
                int iNext = ii + 1;
                while(testNext){
                    if(iNext>=nData){
                        testNext = false;
                    }
                    else{
                        if(weight[iNext]==0.0D){
                            iNext++;
                        }
                        else{
                            next = weight[iNext];
                            testNext = false;
                        }
                    }
                }

                // Take average
                weight[ii]=(last + next)/2.0D;
            }
        }
        return flag;
    }

    // Set data and default values
    protected void setDefaultValues(double[][] xData, double[] yData, double[] weight){
        this.nData = yData.length;
        this.nXarrays = xData.length;
        this.nParam = this.nXarrays;
        this.yData = new double[nData];
        this.yCalc = new double[nData];
        this.weight = new double[nData];
        this.residual = new double[nData];
        this.residualW = new double[nData];
        this.xData = new double[nXarrays][nData];
        int n=weight.length;
        if(n!=this.nData)throw new IllegalArgumentException("The weight and the y data lengths do not agree");
        for(int i=0; i<this.nData; i++){
            this.yData[i]=yData[i];
            this.weight[i]=weight[i];
        }
        for(int j=0; j<this.nXarrays; j++){
            n=xData[j].length;
            if(n!=this.nData)throw new IllegalArgumentException("An x [" + j + "] length " + n + " and the y data length, " + this.nData + ", do not agree");
            for(int i=0; i<this.nData; i++){
                this.xData[j][i]=xData[j][i];
            }
        }

        // minimimum and maximum y-values
        this.minimumY = this.yData[0];
        this.minimumYindex = 0;
        this.maximumY = this.yData[0];
        this.maximumYindex = 0;
        for(int i=0; i<this.nData; i++){
            if(this.yData[i]<this.minimumY){
                this.minimumY = this.yData[i];
                this.minimumYindex  = i;
            }
            if(this.yData[i]>this.maximumY){
                this.maximumY = this.yData[i];
                this.maximumYindex  = i;
            }
        }

        // Reassess non-linear tolerance fTol
        // this.reassessFtol();
	}

	// Reassess non-linear regression tolerance in light of the data
	private void reassessFtol(){

	    // Mean exponent of y-values
	    double meanExp = Math.pow(10, Math.floor(Math.log10((this.minimumY + this.maximumY)/2.0)));

	    // Calculate new fTol
	    double fTolnew = meanExp*this.fTol;
	    if(fTolnew<this.fTol)this.fTol = fTolnew;
	}


	// Set standard deviation, variance and covariance denominators to n
    public static void setDenominatorToN(){
        Stat.setStaticDenominatorToN();
    }

    // Set standard deviation, variance and covariance denominators to n
    public static void setDenominatorToNminusOne(){
        Stat.setStaticDenominatorToNminusOne();
    }

    // Reset value of cfMaxIter used in contFract method in Stat called by the regularised incomplete beta function methods in Stat
    // These are called from Regression, e.g. in the calculation of p-Values
    public static void resetCFmaxIter(int cfMaxIter){
        Stat.resetCFmaxIter(cfMaxIter);
    }

    // Get value of cfMaxIter used in contFract method in Stat called by the regularised incomplete beta function methods in Stat
    // These are called from Regression, e.g. in the calculation of p-Values
    public static int getCFmaxIter(){
        return Stat.getCFmaxIter();
    }

    // Reset value of cfTol used in contFract method in Stat called by the regularised incomplete beta function methods in Stat
    // These are called from Regression, e.g. in the calculation of p-Values
    public static void resetCFtolerance(double cfTol){
        Stat.resetCFtolerance(cfTol);
    }

    // Get value of cfTol used in contFract method in Stat called by the regularised incomplete beta function methods in Stat
    // These are called from Regression, e.g. in the calculation of p-Values
    public static double getCFtolerance(){
        return Stat.getCFtolerance();
    }

	// Supress printing of results
	public void supressPrint(){
	    this.supressPrint = true;
	}

	// Supress plot of calculated versus experimental values
	public void supressYYplot(){
	    this.supressYYplot = true;
	}

    // Supress convergence and chiSquare error messages
	public void supressErrorMessages(){
	    this.supressErrorMessages = true;
	}

    // Ignore check on whether degrtees of freedom are greater than zero
    public void ignoreDofFcheck(){
        this.ignoreDofFcheck = true;
    }

    // Supress the statistical analysis
    public void supressStats(){
        this.statFlag = false;
    }

    // Reinstate statistical analysis
    public void reinstateStats(){
        this.statFlag = true;
    }

    // Reset window close option
    // argument = 1: closing plot window also terminates the program
    // argument = 2: closing plot window leaves program running
    public void setCloseChoice(int closeChoice){
        switch(closeChoice){
            case 1: this.plotWindowCloseChoice = false;
                    break;
            case 2: this.plotWindowCloseChoice = true;
                    break;
            default: throw new IllegalArgumentException("Option " + closeChoice + " not recognised");
        }
    }

    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to unity
    public void setYscaleOption(boolean flag){
        this.scaleFlag=flag;
        if(flag==false)this.yScaleFactor = 1.0D;
    }

    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to unity
    // retained for backward compatibility
    public void setYscale(boolean flag){
        this.scaleFlag=flag;
        if(flag==false)this.yScaleFactor = 1.0D;
    }

    // Reset the ordinate scale factor option
    // true - Ao is unkown to be found by regression procedure
    // false - Ao set to given value
    public void setYscaleFactor(double scale){
        this.scaleFlag=false;
        this.yScaleFactor = scale;
    }

    // Get the ordinate scale factor option
    // true - Ao is unkown
    // false - Ao set to unity
    public boolean getYscaleOption(){
        return this.scaleFlag;
    }

    // Get the ordinate scale factor option
    // true - Ao is unkown
    // false - Ao set to unity
    // retained to ensure backward compatibility
    public boolean getYscale(){
        return this.scaleFlag;
    }

    // Reset the true frequency test, trueFreq
    // true if yData values are true frequencies, e.g. in a fit to Gaussian; false if not
    // if true chiSquarePoisson (see above) is also calculated
    public void setTrueFreq(boolean trFr){
        boolean trFrOld = this.trueFreq;
        this.trueFreq = trFr;
        if(trFr){
            boolean flag = setTrueFreqWeights(this.yData, this.weight);
            if(flag){
                this.trueFreq=true;
                this.weightOpt=true;
            }
            else{
                this.trueFreq=false;
                this.weightOpt=false;
            }
        }
        else{
            if(trFrOld){
                for(int i=0; i<this.weight.length; i++){
                    weight[i]=1.0D;
                }
                this.weightOpt=false;
            }
        }
    }

    // Get the true frequency test, trueFreq
    public boolean getTrueFreq(){
        return this.trueFreq;
    }

    // Reset the x axis legend
    public void setXlegend(String legend){
        this.xLegend = legend;
        this.legendCheck=true;
    }

    // Reset the y axis legend
    public void setYlegend(String legend){
        this.yLegend = legend;
        this.legendCheck=true;
    }

     // Set the title
    public void setTitle(String title){
        this.graphTitle = title;
    }

    // Fit to a constant
    // y = a
    public void constant(){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 46;
        this.linNonLin = true;
        this.nParam = 1;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];

        this.best[0] = Stat.mean(this.yData, this.weight);
        this.bestSd[0] = Stat.standardDeviation(this.yData, this.weight);
        this.tValues[0] = this.best[0]/this.bestSd[0];

		double atv = Math.abs(this.tValues[0]);
        if(atv!=atv){
		    this.pValues[0] = Double.NaN;
	    }
		else{
		    this.pValues[0] = 1.0 - Stat.studentTcdf(-atv, atv, this.degreesOfFreedom);
        }

		this.sumOfSquaresError = 0.0;
		this.chiSquare = 0.0;
		for(int i=0; i<this.nData; i++){
		    this.yCalc[i] = best[0];
		    this.residual[i] = this.yCalc[i] - this.yData[i];
		    this.residualW[i] = this.residual[i]/this.weight[i];
		    this.sumOfSquaresError += this.residual[i]*this.residual[i];
		    this.chiSquare += this.residualW[i]*this.residualW[i];
		}
        this.reducedChiSquare = this.chiSquare/this.degreesOfFreedom;
        this.calcDurbinWatson();
    }

    // Fit to a constant
    // plus plot and output file
    // y = a
    // legends provided
    public void constantPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.constant();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Fit to a constant
    // plus plot and output file
    // y = a
    // no legends provided
    public void constantPlot(){
        this.constant();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    public void linear(){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 0;
        this.linNonLin = true;
        this.nParam = this.nXarrays+1;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nParam][this.nData];

        for(int j=0; j<nData; j++)aa[0][j]=1.0D;
        for(int i=1; i<nParam; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=this.xData[i-1][j];
            }
        }
        this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];
        this.generalLinear(aa);
        if(!this.ignoreDofFcheck)this.generalLinearStats(aa);
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // legends provided
    public void linearPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.linear();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // no legends provided
    public void linearPlot(){
        this.linear();
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // a fixed (argument: intercept)
    public void linear(double intercept){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 47;
        this.fixedInterceptL = intercept;
        this.linNonLin = true;
        this.nParam = this.nXarrays;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nParam][this.nData];

        for(int j=0; j<nData; j++)this.yData[j] -= intercept;
        for(int i=0; i<nParam; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=this.xData[i][j];
            }
        }
        this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];
        this.generalLinear(aa);
        if(!this.ignoreDofFcheck)this.generalLinearStats(aa);
        for(int j=0; j<nData; j++){
            this.yData[j] += intercept;
            this.yCalc[j] += intercept;
        }

    }

    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // a fixed (argument: intercept)
    // legends provided
    public void linearPlot(double intercept, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.linear(intercept);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Multiple linear regression with intercept (including y = ax + b)
    // plus plot and output file
    // y = a + b.x1 + c.x2 + d.x3 + . . .
    // a fixed (argument: intercept)
    // no legends provided
    public void linearPlot(double intercept){
        this.linear(intercept);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }


    // Polynomial fitting
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    public void polynomial(int deg){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        if(this.nXarrays>1)throw new IllegalArgumentException("This class will only perform a polynomial regression on a single x array");
        if(deg<1)throw new IllegalArgumentException("Polynomial degree must be greater than zero");
        this.lastMethod = 1;
        this.linNonLin = true;
        this.nParam =  deg+1;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nParam][this.nData];

        for(int j=0; j<nData; j++)aa[0][j]=1.0D;
        for(int j=0; j<nData; j++)aa[1][j]=this.xData[0][j];

        for(int i=2; i<nParam; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=Math.pow(this.xData[0][j],i);
            }
        }
        this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];
        this.generalLinear(aa);
        if(!this.ignoreDofFcheck)this.generalLinearStats(aa);
    }

    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // legends provided
    public void polynomialPlot(int n, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.polynomial(n);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // No legends provided
    public void polynomialPlot(int n){
        this.polynomial(n);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Polynomial fitting
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // a is fixed (argument: intercept)
    public void polynomial(int deg, double intercept){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        if(this.nXarrays>1)throw new IllegalArgumentException("This class will only perform a polynomial regression on a single x array");
        if(deg<1)throw new IllegalArgumentException("Polynomial degree must be greater than zero");
        this.lastMethod = 48;
        this.fixedInterceptP = intercept;
        this.linNonLin = true;
        this.nParam =  deg;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        double[][] aa = new double[this.nParam][this.nData];

        for(int j=0; j<nData; j++)this.yData[j] -= intercept;
        for(int j=0; j<nData; j++)aa[0][j]=this.xData[0][j];

        for(int i=1; i<nParam; i++){
            for(int j=0; j<nData; j++){
                aa[i][j]=Math.pow(this.xData[0][j],i+1);
            }
        }
        this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];
        this.generalLinear(aa);
        if(!this.ignoreDofFcheck)this.generalLinearStats(aa);
        for(int j=0; j<nData; j++){
            this.yData[j] += intercept;
            this.yCalc[j] += intercept;
        }
    }

    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // a is fixed (argument: intercept)
    // legends provided
    public void polynomialPlot(int n, double intercept, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.polynomial(n, intercept);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Polynomial fitting plus plot and output file
    // y = a + b.x + c.x^2 + d.x^3 + . . .
    // a is fixed (argument: intercept)
    // No legends provided
    public void polynomialPlot(int n, double intercept){
        this.polynomial(n, intercept);
        if(!this.supressPrint)this.print();
        int flag = this.plotXY();
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Best polynomial
    // Finds the best polynomial fit
    public ArrayList<Object> bestPolynomial(){
        return polynomialBest(0);
    }

    // Internal method finding the best polynomial fit
    public ArrayList<Object> polynomialBest(int flag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        if(this.nXarrays>1)throw new IllegalArgumentException("This class will only perform a polynomial regression on a single x array");
        this.bestPolyFlag = true;
        this.linNonLin = true;

        ArrayList<Object> array0 = null;
        ArrayList<Double> array1 = new ArrayList<Double>();

        int deg = 0;
        int degBest = 0;
        int degMax = this.nData-2;
        int nComp = 0;
        double fRatio = 0.0;
        double fProb = 0.0;
        double fSign = 0.0;

        boolean test0 = true;

        // Regression for a constant value
        Regression reg = new Regression(xData[0], yData);
        reg.constant();
        double chi = reg.getChiSquare();
        double chiLast = chi;

        // Regression for a straight line
        deg++;
        if(deg>degMax){
            test0 = false;
            System.out.println("Method bestPolynomial requires at least three data points: null  returned");
            return null;
        }
        else{
            reg.linear();
            chi = reg.getChiSquare();

            // first comparison
            array0 = Regression.testOfAdditionalTerms_ArrayList(chiLast, 0, chi, 1, nData, this.fProbSignificance);
            nComp++;
            fRatio = ((Double)array0.get(0)).doubleValue();
            fProb = ((Double)array0.get(1)).doubleValue();
            fSign = ((Double)array0.get(8)).doubleValue();

            array1.add(new Double(chiLast));
            array1.add(new Double(chi));
            array1.add(new Double(fRatio));
            array1.add(new Double(fProb));
            array1.add(new Double(fSign));

            // First comparison test
            if(fRatio<fSign){
                test0 = false;
                degBest = 0;
            }
        }
        // Iterate polynomial fittings and comparisons
        deg++;
        if(deg>degMax)test0 = false;

        while(test0){
            chiLast = chi;

            // polynomial regression
            reg.polynomial(deg);
            chi = reg.getChiSquare();

            // comparison
            array0 = Regression.testOfAdditionalTerms_ArrayList(chiLast, deg-1, chi, deg, nData, this.fProbSignificance);
            nComp++;
            fRatio = ((Double)array0.get(0)).doubleValue();
            fProb = ((Double)array0.get(1)).doubleValue();
            fSign = ((Double)array0.get(8)).doubleValue();

            array1.add(new Double(chiLast));
            array1.add(new Double(chi));
            array1.add(new Double(fRatio));
            array1.add(new Double(fProb));
            array1.add(new Double(fSign));

            // comparison test
            if(fRatio<fSign){
                test0 = false;
                degBest = deg - 1;
            }
            deg++;
            if(deg>degMax)test0 = false;
        }

        this.bestPolynomialDegree = degBest;

        // Repack ArrayList
        int[] deg0s = new int[nComp];
        int[] deg1s = new int[nComp];
        double[] chi0s = new double[nComp];
        double[] chi1s = new double[nComp];
        double[] fRatios = new double[nComp];
        double[] fProbs = new double[nComp];
        double[] fSigns = new double[nComp];


        for(int i=0; i<nComp; i++){
            deg0s[i] = i;
            deg1s[i] = i+1;
            chi0s[i] = (array1.get(5*i)).doubleValue();
            chi1s[i] = (array1.get(5*i+1)).doubleValue();
            fRatios[i] = (array1.get(5*i+2)).doubleValue();
            fProbs[i] = (array1.get(5*i+3)).doubleValue();
            fSigns[i] = (array1.get(5*i+4)).doubleValue();
        }


        this.bestPolyArray.clear();
        this.bestPolyArray.add(new Integer(this.bestPolynomialDegree));
        this.bestPolyArray.add(new Integer(nComp));
        this.bestPolyArray.add(deg0s);
        this.bestPolyArray.add(deg1s);
        this.bestPolyArray.add(chi0s);
        this.bestPolyArray.add(chi1s);
        this.bestPolyArray.add(fRatios);
        this.bestPolyArray.add(fProbs);
        this.bestPolyArray.add(fSigns);
        this.bestPolyArray.add(this.fProbSignificance);

        // check for zero chi square
        boolean testZero = true;
        int ii = 0;
        while(testZero){
            if(chi0s[ii]==0.0){
                this.bestPolynomialDegree = ii;
                testZero = false;
            }
            else{
                ii++;
                if(ii>=nComp)testZero = false;
            }
        }
        
        switch(flag){
        case 0: // No plot
                switch(this.bestPolynomialDegree){
                    case 0: this.constant();
                            break;
                    case 1: this.linear();
                            break;
                    default: this.polynomial(this.bestPolynomialDegree);
                }
                break;
        case 1: // Plot
                switch(this.bestPolynomialDegree){
                    case 0: this.constantPlot();
                            break;
                    case 1: this.linearPlot();
                            break;
                    default: this.polynomialPlot(this.bestPolynomialDegree);
                }
        }

        return this.bestPolyArray;

    }


    // Best polynomial
    // Finds the best polynomial fit
    // plus plot and output file
    // Legends provided
    public ArrayList<Object> bestPolynomialPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        return this.polynomialBest(1);
    }

    // Best polynomial
    // Finds the best polynomial fit
    // plus plot and output file
    // No legends provided
    public ArrayList<Object> bestPolynomialPlot(){
        return this.polynomialBest(1);
    }

    // Best polynomial
    // Finds the best polynomial fit
    // Fixed intercept
    public ArrayList<Object> bestPolynomial(double fixedIntercept){
        this.fixedInterceptP = fixedIntercept;
        return polynomialBest(fixedIntercept, 0);
    }


    // Internal method finding the best polynomial fit
    // Fixed intercept
    public ArrayList<Object> polynomialBest(double intercept, int flag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        if(this.nXarrays>1)throw new IllegalArgumentException("This class will only perform a polynomial regression on a single x array");
        this.bestPolyFlag = true;
        this.linNonLin = true;

        ArrayList<Object> array0 = null;
        ArrayList<Double> array1 = new ArrayList<Double>();

        int deg = 1;
        int degBest = 1;
        int degMax = this.nData-2;
        int nComp = 0;
        double fRatio = 0.0;
        double fProb = 0.0;
        double fSign = 0.0;

        boolean test0 = true;
        // Regression for a straight line
        Regression reg = new Regression(xData[0], yData);
        double chi = 0.0;
        double chiLast = 0.0;
        if(deg>degMax){
            test0 = false;
            System.out.println("Method bestPolynomial requires at least three data points: null  returned");
            return null;
        }
        else{
            reg.linear(intercept);
            chi = reg.getChiSquare();
            
        }

        // Iterate polynomial fittings and comparisons
        deg++;
        if(deg>degMax)test0 = false;
        if(!test0 && degBest==0)degBest=1;
        while(test0){
            chiLast = chi;

            // polynomial regression
            reg.polynomial(deg, intercept);
            chi = reg.getChiSquare();

            // comparison
            array0 = Regression.testOfAdditionalTerms_ArrayList(chiLast, deg-1, chi, deg, nData, this.fProbSignificance);
            nComp++;
            fRatio = ((Double)array0.get(0)).doubleValue();
            fProb = ((Double)array0.get(1)).doubleValue();
            fSign = ((Double)array0.get(8)).doubleValue();

            array1.add(new Double(chiLast));
            array1.add(new Double(chi));
            array1.add(new Double(fRatio));
            array1.add(new Double(fProb));
            array1.add(new Double(fSign));

            // comparison test
            if(fRatio<fSign){
                test0 = false;
                degBest = deg - 1;
            }
            deg++;
            if(deg>degMax)test0 = false;
        }

        this.bestPolynomialDegree = degBest;

        // Repack ArrayList
        int[] deg0s = new int[nComp];
        int[] deg1s = new int[nComp];
        double[] chi0s = new double[nComp];
        double[] chi1s = new double[nComp];
        double[] fRatios = new double[nComp];
        double[] fProbs = new double[nComp];
        double[] fSigns = new double[nComp];


        for(int i=0; i<nComp; i++){
            deg0s[i] = i+1;
            deg1s[i] = i+2;
            chi0s[i] = (array1.get(5*i)).doubleValue();
            chi1s[i] = (array1.get(5*i+1)).doubleValue();
            fRatios[i] = (array1.get(5*i+2)).doubleValue();
            fProbs[i] = (array1.get(5*i+3)).doubleValue();
            fSigns[i] = (array1.get(5*i+4)).doubleValue();
        }


        this.bestPolyArray.clear();
        this.bestPolyArray.add(new Integer(this.bestPolynomialDegree));
        this.bestPolyArray.add(new Integer(nComp));
        this.bestPolyArray.add(deg0s);
        this.bestPolyArray.add(deg1s);
        this.bestPolyArray.add(chi0s);
        this.bestPolyArray.add(chi1s);
        this.bestPolyArray.add(fRatios);
        this.bestPolyArray.add(fProbs);
        this.bestPolyArray.add(fSigns);
        this.bestPolyArray.add(this.fProbSignificance);

        // check for zero chi square
        boolean testZero = true;
        int ii = 0;
        while(testZero){
            if(chi0s[ii]==0.0){
                this.bestPolynomialDegree = ii+1;
                testZero = false;
            }
            else{
                ii++;
                if(ii>=nComp)testZero = false;
            }
        }
        
        switch(flag){
        case 0: // No plot
                switch(this.bestPolynomialDegree){
                    case 1: this.linear(intercept);
                            break;
                    default: this.polynomial(this.bestPolynomialDegree, intercept);
                }
                break;
        case 1: // Plot
                switch(this.bestPolynomialDegree){
                    case 1: this.linearPlot(intercept);
                            break;
                    default: this.polynomialPlot(this.bestPolynomialDegree, intercept);
                }
        }

        return this.bestPolyArray;

    }

    // Best polynomial
    // Finds the best polynomial fit with a fixed intercept
    // plus plot and output file
    // Legends provided
    public ArrayList<Object> bestPolynomialPlot(double fixedIntercept, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        return this.polynomialBest(fixedIntercept, 1);
    }

    // Best polynomial
    // Finds the best polynomial fit
    // plus plot and output file
    // No legends provided
    public ArrayList<Object> bestPolynomialPlot(double fixedIntercept){
        return this.polynomialBest(fixedIntercept, 1);
    }

    // Set significance level used in bestPolynomial F-test
    public void setFtestSignificance(double signif){
        this.fProbSignificance = signif;
    }

    // get significance level used in bestPolynomial F-test
    public double getFtestSignificance(double signif){
        return this.fProbSignificance;
    }

    // Method for fitting data to a non-integer polynomial
    // y = a[0] + a[1].x + a[2].x^a[n+1] +  a[3].x^a[n+2] + . . . + a[n].x^a[2n-1]
    // nTerms = n+1;
    // No plotting
    public void nonIntegerPolynomial(int nTerms){
        this.fitNonIntegerPolynomial(nTerms, 0);
    }

    public void nonIntegerPolynomial(){
        this.fitNonIntegerPolynomial(3, 0);
    }


    // Method for fitting data to a non-integer polynomial
    // y = a[0] + a[1].x + a[2].x^a[n+1] +  a[3].x^a[n+2] + . . . + a[n].x^a[2n-1]
    // nTerms = n+1;
    // with plotting
    public void nonIntegerPolynomialPlot(int nTerms){
        this.fitNonIntegerPolynomial(nTerms, 1);
    }

    public void nonIntegerPolynomialPlot(){
        this.fitNonIntegerPolynomial(3, 1);
    }

    // Method for fitting data to a non-integer polynomial
    // y = a[0] + a[1].x + a[2].x^a[n+1] +  a[3].x^a[n+2] + . . . + a[n].x^a[2n-1]
    // nTerms = n+1;
    // with plotting and user supplied legends
    public void nonIntegerPolynomialPlot(int nTerms, String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.fitNonIntegerPolynomial(nTerms, 1);
    }

    public void nonIntegerPolynomialPlot(String xLegend, String yLegend){
    this.xLegend = xLegend;
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.fitNonIntegerPolynomial(3, 1);
    }

    // Internal method for fitting data to a non-integer polynomial
    // y = a[0] + a[1].x + a[2].x^a[n+1] +  a[3].x^a[n+2] + . . . + a[n].x^a[2n-1]
    // nTerms = n+1;
    protected void fitNonIntegerPolynomial(int nTerms, int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod = 50;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    int deg = nTerms - 1;
	    this.nParam = 2*nTerms - 1;
	    this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // Estimate of parameters
        Regression reg = new Regression(this.xData[0], this.yData, this.weight);

        reg.polynomial(deg);

        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];

        double[] best = reg.getBestEstimates();
        double sum = 0.0;
        for(int i=0; i<nTerms; i++){
            start[i] = best[i];
            sum += start[i];
        }
        sum = sum/nTerms;
        for(int i=0; i<nTerms; i++){
            step[i] = start[i]*0.1;
            if(step[i]==0.0)step[i] = 0.1*sum;
        }
        double ii = 1.0;
        for(int i=nTerms; i<this.nParam; i++){
            start[i] = ii;
            step[i] = start[i]*0.1;
            ii += 1.0;
        }

        // Nelder and Mead Simplex Regression
        NonIntegerPolyFunction f = new NonIntegerPolyFunction();
        f.setNterms(nTerms);
        Object regFun = (Object)f;
        this.nelderMead(regFun, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // Generalised linear regression
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    public void linearGeneral(){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.lastMethod = 2;

        this.linNonLin = true;
        this.nParam = this.nXarrays;
        this.degreesOfFreedom = this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
	    this.best = new double[this.nParam];
        this.bestSd = new double[this.nParam];
        this.tValues = new double[this.nParam];
        this.pValues = new double[this.nParam];
        this.generalLinear(this.xData);
        if(!this.ignoreDofFcheck)this.generalLinearStats(this.xData);
    }

    // Generalised linear regression plus plot and output file
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    // legends provided
    public void linearGeneralPlot(String xLegend, String yLegend){
        this.xLegend = xLegend;
        this.yLegend = yLegend;
        this.legendCheck = true;
        this.linearGeneral();
        if(!this.supressPrint)this.print();
        if(!this.supressYYplot)this.plotYY();
    }

    // Generalised linear regression plus plot and output file
    // y = a.f1(x) + b.f2(x) + c.f3(x) + . . .
    // No legends provided
    public void linearGeneralPlot(){
        this.linearGeneral();
        if(!this.supressPrint)this.print();
        if(!this.supressYYplot)this.plotYY();
    }

	// Generalised linear regression (protected method called by linear(), linearGeneral() and polynomial())
    protected void generalLinear(double[][] xd){
        if(this.nData<=this.nParam && !this.ignoreDofFcheck)throw new IllegalArgumentException("Number of unknown parameters is greater than or equal to the number of data points");
    	double sde=0.0D, sum=0.0D, yCalctemp=0.0D;
        double[][] a = new double[this.nParam][this.nParam];
        double[][] h = new double[this.nParam][this.nParam];
        double[]b = new double[this.nParam];
        double[]coeff = new double[this.nParam];

         // set statistic arrays to NaN if df check ignored
        if(this.ignoreDofFcheck){
            this.bestSd = new double[this.nParam];
	        this.pseudoSd = new double[this.nParam];
            this.tValues = new double[this.nParam];
            this.pValues = new double[this.nParam];

            this.covar = new double[this.nParam][this.nParam];
	        this.corrCoeff = new double[this.nParam][this.nParam];
	        for(int i=0; i<this.nParam; i++){
	            this.bestSd[i] = Double.NaN;
	            this.pseudoSd[i] = Double.NaN;
	            for(int j=0; j<this.nParam; j++){
                    this.covar[i][j] = Double.NaN;
	                this.corrCoeff[i][j] = Double.NaN;
	            }
	        }
	    }

		for (int i=0; i<nParam; ++i){
			sum=0.0D ;
			for (int j=0; j<nData; ++j){
				sum += this.yData[j]*xd[i][j]/Fmath.square(this.weight[j]);
			}
			b[i]=sum;
		}
		for (int i=0; i<nParam; ++i){
			for (int j=0; j<nParam; ++j){
				sum=0.0;
				for (int k=0; k<nData; ++k){
					sum += xd[i][k]*xd[j][k]/Fmath.square(this.weight[k]);
				}
				a[j][i]=sum;
			}
		}
		Matrix aa = new Matrix(a);
		if(this.supressErrorMessages)aa.supressErrorMessage();
		coeff = aa.solveLinearSet(b);

	    for(int i=0; i<this.nParam; i++){
		    this.best[i] = coeff[i];
        }
	}

    // Generalised linear regression statistics (protected method called by linear(), linearGeneral() and polynomial())
    protected void generalLinearStats(double[][] xd){

	    double sde=0.0D, sum=0.0D, yCalctemp=0.0D;
        double[][] a = new double[this.nParam][this.nParam];
        double[][] h = new double[this.nParam][this.nParam];
        double[][] stat = new double[this.nParam][this.nParam];
        double[][] cov = new double[this.nParam][this.nParam];
        this.covar = new double[this.nParam][this.nParam];
        this.corrCoeff = new double[this.nParam][this.nParam];
        double[]coeffSd = new double[this.nParam];
        double[]coeff = new double[this.nParam];

        for(int i=0; i<this.nParam; i++){
            coeff[i] = this.best[i];
        }

		this.chiSquare=0.0D;
		this.sumOfSquaresError=0.0D;
		for (int i=0; i< nData; ++i){
			yCalctemp=0.0;
			for (int j=0; j<nParam; ++j){
				yCalctemp += coeff[j]*xd[j][i];
			}
			this.yCalc[i] = yCalctemp;
			yCalctemp -= this.yData[i];
			this.residual[i]=yCalctemp;
			this.residualW[i]=yCalctemp/weight[i];
			this.chiSquare += Fmath.square(yCalctemp/this.weight[i]);
			this.sumOfSquaresError += Fmath.square(yCalctemp);
		}
		this.reducedChiSquare = this.chiSquare/(this.degreesOfFreedom);
		double varY = this.sumOfSquaresError/(this.degreesOfFreedom);
		double sdY = Math.sqrt(varY);

        if(this.sumOfSquaresError==0.0D){
             for(int i=0; i<this.nParam;i++){
                coeffSd[i]=0.0D;
		        for(int j=0; j<this.nParam;j++){
		            this.covar[i][j]=0.0D;
		            if(i==j){
		                this.corrCoeff[i][j]=1.0D;
		            }
		            else{
		                this.corrCoeff[i][j]=0.0D;
		            }
		        }
		    }
        }
        else{
	        for (int i=0; i<this.nParam; ++i){
	    	    for (int j=0; j<this.nParam; ++j){
	    		    sum=0.0;
	    		    for (int k=0; k<this.nData; ++k){
	    		        if (weightOpt){
	    		            sde = weight[k];
	    	            }
	                    else{
	    		            sde = sdY;
	                    }
                        sum += xd[i][k]*xd[j][k]/Fmath.square(sde);
                    }
	                h[j][i]=sum;
	    	    }
	        }
		    Matrix hh = new Matrix(h);
		    if(this.supressErrorMessages)hh.supressErrorMessage();
		    hh = hh.inverse();
		    stat = hh.getArrayCopy();
		    for (int j=0; j<nParam; ++j){
		        coeffSd[j] = Math.sqrt(stat[j][j]);
		    }

	        for(int i=0; i<this.nParam;i++){
		        for(int j=0; j<this.nParam;j++){
		            this.covar[i][j]=stat[i][j];
		        }
		    }

		    for(int i=0; i<this.nParam;i++){
		        for(int j=0; j<this.nParam;j++){
		            if(i==j){
		                this.corrCoeff[i][j] = 1.0D;
		            }
		            else{
		                this.corrCoeff[i][j]=covar[i][j]/(coeffSd[i]*coeffSd[j]);
                    }
                }
		    }
		}

	    for(int i=0; i<this.nParam; i++){
		    this.bestSd[i] = coeffSd[i];
		    this.tValues[i] = this.best[i]/this.bestSd[i];
		    double atv = Math.abs(this.tValues[i]);
		    if(atv!=atv){
		        this.pValues[i] = Double.NaN;
		    }
		    else{
		        this.pValues[i] = 1.0 - Stat.studentTcdf(-atv, atv, this.degreesOfFreedom);
		    }
        }

        // Linear correlation coefficient
        if(this.nXarrays==1 && this.nYarrays==1){
            this.xyR = Stat.corrCoeff(this.xData[0], this.yData, this.weight);
        }
        this.yyR = Stat.corrCoeff(this.yCalc, this.yData, this.weight);

        // Coefficient of determination
        this.yMean = Stat.mean(this.yData);
        this.yWeightedMean = Stat.mean(this.yData, this.weight);

        this.sumOfSquaresTotal = 0.0;
        for(int i=0; i<this.nData; i++){
            this.sumOfSquaresTotal += Fmath.square((this.yData[i] - this.yWeightedMean)/weight[i]);
        }

        this.sumOfSquaresRegrn = this.sumOfSquaresTotal - this.chiSquare;
        if(this.sumOfSquaresRegrn<0.0)this.sumOfSquaresRegrn=0.0;

        this.multR = this.sumOfSquaresRegrn/this.sumOfSquaresTotal;

        // Calculate adjusted multiple coefficient of determination
        this.adjustedR = Double.NaN;
        this.multipleF = Double.NaN;
        if((this.nData - this.nParam - 1)>0){
            this.adjustedR = 1.0 - (1.0 - multR)*(this.nData - 1 )/(this.nData - this.nParam - 1);
        }

        // F-ratio
        this.multipleF = multR*(this.nData-this.nParam-1.0)/((1.0D-this.multR)*this.nParam);
        if(this.multipleF>=0.0)this.multipleFprob = Stat.fTestProb(this.multipleF, this.nXarrays, this.nData-this.nParam-1);

        // Durbin Watson d statistic
        this.calcDurbinWatson();

	}


    // Nelder and Mead Simplex Simplex Non-linear Regression
    protected void nelderMead(Object regFun, double[] start, double[] step, double fTol, int nMax){
        int np = start.length;          // number of unknown parameters;
        if(this.maxConstraintIndex>=np)throw new IllegalArgumentException("You have entered more constrained parameters ("+this.maxConstraintIndex+") than minimisation parameters (" + np + ")");
        this.nlrStatus = true;          // -> false if convergence criterion not met
        this.nParam = np;               // number of parameters whose best estimates are to be determined
        int nnp = np+1;                 // number of simplex apices
        this.lastSSnoConstraint=0.0D;   // last sum of squares without a penalty constraint being applied

        if(this.scaleOpt<2)this.scale = new double[np];     // scaling factors
        if(scaleOpt==2 && scale.length!=start.length)throw new IllegalArgumentException("scale array and initial estimate array are of different lengths");
        if(step.length!=start.length)throw new IllegalArgumentException("step array length " + step.length + " and initial estimate array length " + start.length + " are of different");

        // check for zero step sizes
        for(int i=0; i<np; i++)if(step[i]==0.0D)throw new IllegalArgumentException("step " + i+ " size is zero");

        // Check minimum number of iterations required is not greater than the maximum number of iterations allowed
        if(this.minIter>this.nMax)this.nMax=this.minIter;

        // set statistic arrays to NaN if degrees of freedom check ignored
        if(this.ignoreDofFcheck){
            this.bestSd = new double[this.nParam];
	        this.pseudoSd = new double[this.nParam];
            this.tValues = new double[this.nParam];
            this.pValues = new double[this.nParam];

            this.covar = new double[this.nParam][this.nParam];
	        this.corrCoeff = new double[this.nParam][this.nParam];;
	        for(int i=0; i<this.nParam; i++){
	            this.bestSd[i] = Double.NaN;
	            this.pseudoSd[i] = Double.NaN;
	            for(int j=0; j<this.nParam; j++){
                    this.covar[i][j] = Double.NaN;
	                this.corrCoeff[i][j] = Double.NaN;
	            }
	        }
	    }

	    // set up arrays
	    this.startH = new double[np];           // holding array of unscaled initial start values
	    this.stepH = new double[np];            // unscaled initial step values
	    this.startSH = new double[np];          // holding array of scaled initial start values
	    this.stepSH = new double[np];           // scaled initial step values
	    double[]pmin = new double[np];          // Nelder and Mead Pmin
	    this.best = new double[np];             // best estimates array
        this.bestSd = new double[np];           // sd of best estimates array
        this.tValues = new double[np];          // t-value of best estimates array
        this.pValues = new double[np];          // p-value of best estimates array

	    double[][] pp = new double[nnp][nnp];   //Nelder and Mead P
	    double[] yy = new double[nnp];          //Nelder and Mead y
	    double[] pbar = new double[nnp];        //Nelder and Mead P with bar superscript
	    double[] pstar = new double[nnp];       //Nelder and Mead P*
	    double[] p2star = new double[nnp];      //Nelder and Mead P**

        // mean of absolute values of yData (for testing for minimum)
        double yabsmean=0.0D;
        for(int i=0; i<this.nData; i++)yabsmean += Math.abs(yData[i]);
        yabsmean /= this.nData;

        // Set any single parameter constraint parameters
        if(this.penalty){
            Integer itemp = (Integer)this.penalties.get(1);
            this.nConstraints = itemp.intValue();
            this.penaltyParam = new int[this.nConstraints];
            this.penaltyCheck = new int[this.nConstraints];
            this.constraints = new double[this.nConstraints];
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nConstraints;i++){
                itemp = (Integer)this.penalties.get(j);
                this.penaltyParam[i] = itemp.intValue();
                j++;
                itemp = (Integer)this.penalties.get(j);
                this.penaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.penalties.get(j);
                this.constraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Set any multiple parameters constraint parameters
        if(this.sumPenalty){
            Integer itemp = (Integer)this.sumPenalties.get(1);
            this.nSumConstraints = itemp.intValue();
            this.sumPenaltyParam = new int[this.nSumConstraints][];
            this.sumPlusOrMinus = new double[this.nSumConstraints][];
            this.sumPenaltyCheck = new int[this.nSumConstraints];
            this.sumPenaltyNumber = new int[this.nSumConstraints];
            this.sumConstraints = new double[this.nSumConstraints];
            int[] itempArray = null;
            double[] dtempArray = null;
            Double dtemp = null;
            int j=2;
            for(int i=0;i<this.nSumConstraints;i++){
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyNumber[i] = itemp.intValue();
                j++;
                itempArray = (int[])this.sumPenalties.get(j);
                this.sumPenaltyParam[i] = itempArray;
                j++;
                dtempArray = (double[])this.sumPenalties.get(j);
                this.sumPlusOrMinus[i] = dtempArray;
                j++;
                itemp = (Integer)this.sumPenalties.get(j);
                this.sumPenaltyCheck[i] = itemp.intValue();
                j++;
                dtemp = (Double)this.sumPenalties.get(j);
                this.sumConstraints[i] = dtemp.doubleValue();
                j++;
            }
        }

        // Store unscaled start and step values
        for(int i=0; i<np; i++){
            step[i] = Math.abs(step[i]);
            this.startH[i]=start[i];
            this.stepH[i]=step[i];
        }

        // scale initial estimates and step sizes
        if(this.scaleOpt>0){
            boolean testzero=false;
            for(int i=0; i<np; i++)if(start[i]==0.0D)testzero=true;
            if(testzero){
                System.out.println("Neler and Mead Simplex: a start value of zero precludes scaling");
                System.out.println("Regression performed without scaling");
                this.scaleOpt=0;
            }
        }
        switch(this.scaleOpt){
            case 0: // No scaling carried out
                    for(int i=0; i<np; i++)scale[i]=1.0D;
                    break;
            case 1: // All parameters scaled to unity
                    for(int i=0; i<np; i++){
                        scale[i]=1.0/start[i];
                        step[i]=step[i]/start[i];
                        start[i]=1.0D;
                    }
                    break;
            case 2: // Each parameter scaled by a user provided factor
                    for(int i=0; i<np; i++){
                        step[i]*=scale[i];
                        start[i]*= scale[i];
                    }
                    break;
            default: throw new IllegalArgumentException("Scaling factor option " + this.scaleOpt + " not recognised");
        }

        // set class member values
        this.fTol=fTol;
        this.nMax=nMax;
        this.nIter=0;
        for(int i=0; i<np; i++){
            this.startSH[i] = start[i];
            this.stepSH[i] = step[i];
            this.scale[i] = scale[i];
        }

	    // initial simplex
	    double sho=0.0D;
	    for (int i=0; i<np; ++i){
 	        sho=start[i];
	 	    pstar[i]=sho;
		    p2star[i]=sho;
		    pmin[i]=sho;
	    }

	    int jcount=this.konvge;  // count of number of restarts still available

	    for (int i=0; i<np; ++i){
	        pp[i][nnp-1]=start[i];
	    }
	    yy[nnp-1]=this.sumSquares(regFun, start);
	    for (int j=0; j<np; ++j){
		    start[j]=start[j]+step[j];

		    for (int i=0; i<np; ++i)pp[i][j]=start[i];
		    yy[j]=this.sumSquares(regFun, start);
		    start[j]=start[j]-step[j];
	    }

	    // loop over allowed number of iterations

        double  ynewlo=0.0D;    // current value lowest y
	    double 	ystar = 0.0D;   // Nelder and Mead y*
	    double  y2star = 0.0D;  // Nelder and Mead y**
	    double  ylo = 0.0D;     // Nelder and Mead y(low)
	    double  fMin;           // function value at minimum

	    int ilo=0;              // index of lowest apex
	    int ihi=0;              // index of highest apex
	    int ln=0;               // counter for a check on low and high apices
	    boolean test = true;    // test becomes false on reaching minimum

	     // variables used in calculating the variance of the simplex at a putative minimum
	    double 	curMin = 00D;   // sd of the values at the simplex apices
	    double  sumnm = 0.0D;   // for calculating the mean of the apical values
	    double  zn = 0.0D;      // for calculating the summation of their differences from the mean
	    double  summnm = 0.0D;  // for calculating the variance

	    while(test){
	        // Determine h
	        ylo=yy[0];
	        ynewlo=ylo;
    	    ilo=0;
	        ihi=0;
	        for (int i=1; i<nnp; ++i){
		        if (yy[i]<ylo){
			        ylo=yy[i];
			        ilo=i;
		        }
		        if (yy[i]>ynewlo){
			        ynewlo=yy[i];
			        ihi=i;
		        }
	        }
	        // Calculate pbar
	        for (int i=0; i<np; ++i){
		        zn=0.0D;
		        for (int j=0; j<nnp; ++j){
			        zn += pp[i][j];
		        }
		        zn -= pp[i][ihi];
		        pbar[i] = zn/np;
	        }

	        // Calculate p=(1+alpha).pbar-alpha.ph {Reflection}
	        for (int i=0; i<np; ++i)pstar[i]=(1.0 + this.rCoeff)*pbar[i]-this.rCoeff*pp[i][ihi];

	        // Calculate y*
	        ystar=this.sumSquares(regFun, pstar);

	        ++this.nIter;

	        // check for y*<yi
	        if(ystar < ylo){
                // Calculate p**=(1+gamma).p*-gamma.pbar {Extension}
	            for (int i=0; i<np; ++i)p2star[i]=pstar[i]*(1.0D + this.eCoeff)-this.eCoeff*pbar[i];
	            // Calculate y**
	            y2star=this.sumSquares(regFun, p2star);
	            ++this.nIter;
                if(y2star < ylo){
                    // Replace ph by p**
		            for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                yy[ihi] = y2star;
	            }
	            else{
	                //Replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }
	        else{
	            // Check y*>yi, i!=h
		        ln=0;
	            for (int i=0; i<nnp; ++i)if (i!=ihi && ystar > yy[i]) ++ln;
	            if (ln==np ){
	                // y*>= all yi; Check if y*>yh
                    if(ystar<=yy[ihi]){
                        // Replace ph by p*
	                    for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                    yy[ihi]=ystar;
	                }
	                // Calculate p** =beta.ph+(1-beta)pbar  {Contraction}
	                for (int i=0; i<np; ++i)p2star[i]=this.cCoeff*pp[i][ihi] + (1.0 - this.cCoeff)*pbar[i];
	                // Calculate y**
	                y2star=this.sumSquares(regFun, p2star);
	                ++this.nIter;
	                // Check if y**>yh
	                if(y2star>yy[ihi]){
	                    //Replace all pi by (pi+pl)/2

	                    for (int j=0; j<nnp; ++j){
		                    for (int i=0; i<np; ++i){
			                    pp[i][j]=0.5*(pp[i][j] + pp[i][ilo]);
			                    pmin[i]=pp[i][j];
		                    }
		                    yy[j]=this.sumSquares(regFun, pmin);
	                    }
	                    this.nIter += nnp;
	                }
	                else{
	                    // Replace ph by p**
		                for (int i=0; i<np; ++i)pp[i][ihi] = p2star[i];
	                    yy[ihi] = y2star;
	                }
	            }
	            else{
	                // replace ph by p*
	                for (int i=0; i<np; ++i)pp[i][ihi]=pstar[i];
	                yy[ihi]=ystar;
	            }
	        }

            // test for convergence
            // calculte sd of simplex and determine the minimum point
            sumnm=0.0;
	        ynewlo=yy[0];
	        ilo=0;
	        for (int i=0; i<nnp; ++i){
	            sumnm += yy[i];
	            if(ynewlo>yy[i]){
	                ynewlo=yy[i];
	                ilo=i;
	            }
	        }
	        sumnm /= (double)(nnp);
	        summnm=0.0;
	        for (int i=0; i<nnp; ++i){
		        zn=yy[i]-sumnm;
	            summnm += zn*zn;
	        }
	        curMin=Math.sqrt(summnm/np);

	        // test simplex sd
	        switch(this.minTest){
	            case 0: // terminate if the standard deviation of the sum of squares [unweighted data] or of the chi square values [weighted data]
	                    // at the apices of the simplex is less than the tolerance, fTol
                        if(curMin<fTol && this.nIter>this.minIter)test=false;
                        break;
	            case 1: // terminate if the reduced chi square [weighted data] or the reduced sum of squares [unweighted data] at the lowest apex
	                    // of the simplex is less than the mean of the absolute values of the dependent variable (y values) multiplied by the tolerance, fTol.
                        if(Math.sqrt(ynewlo/this.degreesOfFreedom)<yabsmean*fTol && this.nIter>this.minIter)test=false;
                        break;
                default: throw new IllegalArgumentException("Simplex standard deviation test option " + this.minTest + " not recognised");
		    }
            this.sumOfSquaresError=ynewlo;
	        if(!test){
	            // temporary store of best estimates
	            for (int i=0; i<np; ++i)pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
	            // store simplex sd
	            this.simplexSd = curMin;
	            // test for restart
	            --jcount;
	            if(jcount>0){
	                test=true;
	   	            for (int j=0; j<np; ++j){
		                pmin[j]=pmin[j]+step[j];
		                for (int i=0; i<np; ++i)pp[i][j]=pmin[i];
		                yy[j]=this.sumSquares(regFun, pmin);
		                pmin[j]=pmin[j]-step[j];
	                 }
	            }
	        }

            // test for reaching allowed number of iterations
	        if(test && this.nIter>this.nMax){
	            if(!this.supressErrorMessages){
	                System.out.println("Maximum iteration number reached, in Regression.simplex(...)");
	                System.out.println("without the convergence criterion being satisfied");
	                System.out.println("Current parameter estimates and sum of squares values returned");
	            }
	            this.nlrStatus = false;
	            // store current estimates
	            for (int i=0; i<np; ++i)pmin[i]=pp[i][ilo];
	            yy[nnp-1]=ynewlo;
                test=false;
            }

        }

        // final store of the best estimates, function value at the minimum and number of restarts
	    for (int i=0; i<np; ++i){
            pmin[i] = pp[i][ilo];
            this.best[i] = pmin[i]/this.scale[i];
            this.scale[i]=1.0D; // unscale for statistical methods
        }
    	this.fMin=ynewlo;
    	this.kRestart=this.konvge-jcount;

        // perform statistical analysis if possible and requested
        if(statFlag){
            if(!this.ignoreDofFcheck)pseudoLinearStats(regFun);
        }
        else{
            for (int i=0; i<np; ++i){
                this.bestSd[i] = Double.NaN;
            }
        }
	}

    // Nelder and Mead Simplex Simplex Non-linear Regression
    public void simplex(RegressionFunction g, double[] start, double[] step, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMax);
    }


    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        Object regFun = (Object)g;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
    public void simplex(RegressionFunction g, double[] start, double[] step, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
    public void simplex(RegressionFunction g, double[] start, double[] step, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        double fToll = this.fTol;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  tolerance
    public void simplexPlot(RegressionFunction g, double[] start, double[] step, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
	// Default  maximum iterations
    public void simplex(RegressionFunction g, double[] start, double[] step){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  tolerance
	// Default  maximum iterations
    public void simplexPlot(RegressionFunction g, double[] start, double[] step){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, step);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fTol, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, double fTol, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, double fTol){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start, int nMax){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex(RegressionFunction g, double[] start){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplex2 should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex Non-linear Regression
    // plus plot and output file
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot(RegressionFunction g, double[] start){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays\nsimplexPlot2 should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex(g, start);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }



    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMax);
    }


    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
    public void simplex2(RegressionFunction2 g, double[] start, double[] step, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        double fToll = this.fTol;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  tolerance
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  tolerance
	// Default  maximum iterations
    public void simplex2(RegressionFunction2 g, double[] start, double[] step){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        double fToll = this.fTol;
        int nMaxx = this.nMax;
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, step, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  tolerance
	// Default  maximum iterations
    public void simplexPlot2(RegressionFunction2 g, double[] start, double[] step){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, step);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fTol, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, double fTol, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, fTol, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        int nMaxx = this.nMax;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fTol, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
	// Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, double fTol){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, fTol);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fToll, nMax);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    // Default  tolerance
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start, int nMax){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start, nMax);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

	// Nelder and Mead simplex
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplex2(RegressionFunction2 g, double[] start){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        Object regFun = (Object)g;
        int n=start.length;
        int nMaxx = this.nMax;
        double fToll = this.fTol;
        double[] stepp = new double[n];
        for(int i=0; i<n;i++)stepp[i]=this.dStep*start[i];
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.degreesOfFreedom = this.nData - start.length;
        this.nelderMead(regFun, start, stepp, fToll, nMaxx);
    }

    // Nelder and Mead Simplex Simplex2 Non-linear Regression
    // plus plot and output file
    // Default  tolerance
    // Default  maximum iterations
	// Default step option - all step[i] = dStep
    public void simplexPlot2(RegressionFunction2 g, double[] start){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.lastMethod=3;
        this.userSupplied = true;
        this.linNonLin = false;
        this.zeroCheck = false;
        this.simplex2(g, start);
        if(!this.supressPrint)this.print();
        int flag = 0;
        if(this.xData.length<2)flag = this.plotXY2(g);
        if(flag!=-2 && !this.supressYYplot)this.plotYY();
    }

    // Calculate the sum of squares of the residuals for non-linear regression
	protected double sumSquares(Object regFun, double[] x){
	    RegressionFunction g1 = null;
	    RegressionFunction2 g2 = null;
	    if(this.multipleY){
            g2 = (RegressionFunction2)regFun;
        }
        else{
            g1 = (RegressionFunction)regFun;
        }

	    double ss = -3.0D;
	    double[] param = new double[this.nParam];
	    double[] xd = new double[this.nXarrays];
	    // rescale for calcultion of the function
	    for(int i=0; i<this.nParam; i++)param[i]=x[i]/this.scale[i];

        // single parameter penalty functions
        double tempFunctVal = this.lastSSnoConstraint;
        boolean test=true;
        if(this.penalty){
            int k=0;
            for(int i=0; i<this.nConstraints; i++){
                k = this.penaltyParam[i];
                switch(penaltyCheck[i]){
                    case -1: // parameter constrained to lie above a given constraint value
                             if(param[k]<constraints[i]){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(constraints[i]-param[k]);
                                test=false;
                             }
                             break;
                    case 0:  // parameter constrained to lie within a given tolerance about a constraint value
                             if(param[k]<constraints[i]*(1.0-this.constraintTolerance)){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(constraints[i]*(1.0-this.constraintTolerance)-param[k]);
                                test=false;
                             }
                             if(param[k]>constraints[i]*(1.0+this.constraintTolerance)){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]*(1.0+this.constraintTolerance));
                                test=false;
                             }
                             break;
                    case 1:  // parameter constrained to lie below a given constraint value
                             if(param[k]>constraints[i]){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(param[k]-constraints[i]);
	                            test=false;
	                         }
	                         break;
                    default: throw new IllegalArgumentException("The " + i + "th penalty check " + penaltyCheck[i] + " not recognised");

                }
            }
        }

        // multiple parameter penalty functions
        if(this.sumPenalty){
            int kk = 0;
            double pSign = 0;
            for(int i=0; i<this.nSumConstraints; i++){
                double sumPenaltySum = 0.0D;
                for(int j=0; j<this.sumPenaltyNumber[i]; j++){
                    kk = this.sumPenaltyParam[i][j];
                    pSign = this.sumPlusOrMinus[i][j];
                    sumPenaltySum += param[kk]*pSign;
                }
                switch(this.sumPenaltyCheck[i]){
                    case -1: // designated 'parameter sum' constrained to lie above a given constraint value
                             if(sumPenaltySum<sumConstraints[i]){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumConstraints[i]-sumPenaltySum);
                                test=false;
                             }
                             break;
                    case 0:  // designated 'parameter sum' constrained to lie within a given tolerance about a given constraint value
                             if(sumPenaltySum<sumConstraints[i]*(1.0-this.constraintTolerance)){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumConstraints[i]*(1.0-this.constraintTolerance)-sumPenaltySum);
                                test=false;
                             }
                             if(sumPenaltySum>sumConstraints[i]*(1.0+this.constraintTolerance)){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]*(1.0+this.constraintTolerance));
                                test=false;
                             }
                             break;
                    case 1:  // designated 'parameter sum' constrained to lie below a given constraint value
                             if(sumPenaltySum>sumConstraints[i]){
                                ss = tempFunctVal + this.penaltyWeight*Fmath.square(sumPenaltySum-sumConstraints[i]);
	                            test=false;
                             }
                             break;
                    default: throw new IllegalArgumentException("The " + i + "th summation penalty check " + sumPenaltyCheck[i] + " not recognised");
                }
            }
        }

        // call function calculation and calculate the sum of squares if constraints have not intervened
        if(test){
            ss = 0.0D;
            for(int i=0; i<this.nData; i++){
                for(int j=0; j<nXarrays; j++)xd[j]=this.xData[j][i];
                if(!this.multipleY){
                    ss += Fmath.square((this.yData[i] - g1.function(param, xd))/this.weight[i]);
                }
                else{
                    ss += Fmath.square((this.yData[i] - g2.function(param, xd, i))/this.weight[i]);
                }

            }
            this.lastSSnoConstraint = ss;

        }

	    // return sum of squares
	    return ss;
	}


	// add a single parameter constraint boundary for the non-linear regression
	public void addConstraint(int paramIndex, int conDir, double constraint){
	    this.penalty=true;

        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.penalties.isEmpty())this.penalties.add(new Integer(this.constraintMethod));

		// add constraint
	    if(penalties.size()==1){
		    this.penalties.add(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.penalties.get(1)).intValue();
            nPC++;
            this.penalties.set(1, new Integer(nPC));
		}
		this.penalties.add(new Integer(paramIndex));
 	    this.penalties.add(new Integer(conDir));
 	    this.penalties.add(new Double(constraint));
 	    if(paramIndex>this.maxConstraintIndex)this.maxConstraintIndex = paramIndex;
 	}


    // add a multiple parameter constraint boundary for the non-linear regression
	public void addConstraint(int[] paramIndices, int[] plusOrMinus, int conDir, double constraint){
	    ArrayMaths am = new ArrayMaths(plusOrMinus);
	    double[] dpom = am.getArray_as_double();
	    addConstraint(paramIndices, dpom, conDir, constraint);
	}

    // add a multiple parameter constraint boundary for the non-linear regression
	public void addConstraint(int[] paramIndices, double[] plusOrMinus, int conDir, double constraint){
	    int nCon = paramIndices.length;
	    int nPorM = plusOrMinus.length;
	    if(nCon!=nPorM)throw new IllegalArgumentException("num of parameters, " + nCon + ", does not equal number of parameter signs, " + nPorM);
	    this.sumPenalty=true;

        // First element reserved for method number if other methods than 'cliff' are added later
		if(this.sumPenalties.isEmpty())this.sumPenalties.add(new Integer(this.constraintMethod));

    	// add constraint
		if(sumPenalties.size()==1){
		    this.sumPenalties.add(new Integer(1));
		}
		else{
		    int nPC = ((Integer)this.sumPenalties.get(1)).intValue();
            nPC++;
            this.sumPenalties.set(1, new Integer(nPC));
		}
		this.sumPenalties.add(new Integer(nCon));
		this.sumPenalties.add(paramIndices);
		this.sumPenalties.add(plusOrMinus);
 	    this.sumPenalties.add(new Integer(conDir));
 	    this.sumPenalties.add(new Double(constraint));
 	    ArrayMaths am = new ArrayMaths(paramIndices);
 	    int maxI = am.getMaximum_as_int();
 	    if(maxI>this.maxConstraintIndex)this.maxConstraintIndex = maxI;
 	}


	// remove all constraint boundaries for the non-linear regression
	public void removeConstraints(){

	    // check if single parameter constraints already set
	    if(!this.penalties.isEmpty()){
		    int m=this.penalties.size();

		    // remove single parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.penalties.remove(i);
		    }
		}
		this.penalty = false;
		this.nConstraints = 0;

	    // check if mutiple parameter constraints already set
	    if(!this.sumPenalties.isEmpty()){
		    int m=this.sumPenalties.size();

		    // remove multiple parameter constraints
    		for(int i=m-1; i>=0; i--){
		        this.sumPenalties.remove(i);
		    }
		}
		this.sumPenalty = false;
		this.nSumConstraints = 0;
		this.maxConstraintIndex = -1;
	}


	// Reset the tolerance used in a fixed value constraint
	public void setConstraintTolerance(double tolerance){
	    this.constraintTolerance = tolerance;
	}


	//  linear statistics applied to a non-linear regression
    protected int pseudoLinearStats(Object regFun){
	    double	f1 = 0.0D, f2 = 0.0D, f3 = 0.0D, f4 = 0.0D; // intermdiate values in numerical differentiation
	    int	flag = 0;       // returned as 0 if method fully successful;
	                        // negative if partially successful or unsuccessful: check posVarFlag and invertFlag
	                        //  -1  posVarFlag or invertFlag is false;
	                        //  -2  posVarFlag and invertFlag are false
	    int np = this.nParam;

	    double[] f = new double[np];
    	double[] pmin = new double[np];
    	double[] coeffSd = new double[np];
    	double[] xd = new double[this.nXarrays];
	    double[][]stat = new double[np][np];
	    pseudoSd = new double[np];

	    Double temp = null;

	    this.grad = new double[np][2];
	    this.covar = new double[np][np];
        this.corrCoeff = new double[np][np];

        // get best estimates
	    pmin = Conv.copy(best);

        // gradient both sides of the minimum
        double hold0 = 1.0D;
        double hold1 = 1.0D;
	    for (int i=0;i<np; ++i){
		    for (int k=0;k<np; ++k){
			    f[k]=pmin[k];
		    }
		    hold0=pmin[i];
            if(hold0==0.0D){
                hold0=this.stepH[i];
                this.zeroCheck=true;
            }
		    f[i]=hold0*(1.0D - this.delta);
	        this.lastSSnoConstraint=this.sumOfSquaresError;
		    f1=sumSquares(regFun, f);
		    f[i]=hold0*(1.0 + this.delta);
	        this.lastSSnoConstraint=this.sumOfSquaresError;
		    f2=sumSquares(regFun, f);
		    this.grad[i][0]=(this.fMin-f1)/Math.abs(this.delta*hold0);
		    this.grad[i][1]=(f2-this.fMin)/Math.abs(this.delta*hold0);
	    }

        // second patial derivatives at the minimum
	    this.lastSSnoConstraint=this.sumOfSquaresError;
	    for (int i=0;i<np; ++i){
		    for (int j=0;j<np; ++j){
			    for (int k=0;k<np; ++k){
				    f[k]=pmin[k];
			    }
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.stepH[i];
                    this.zeroCheck=true;
                }
			    f[i]=hold0*(1.0 + this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.stepH[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 + this.delta/2.0D);
        	    this.lastSSnoConstraint=this.sumOfSquaresError;
			    f1=sumSquares(regFun, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.stepH[i];
                    this.zeroCheck=true;
                }
 			    f[i]=hold0*(1.0 - this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.stepH[j];
                    this.zeroCheck=true;
                }
 		        f[j]=hold0*(1.0 + this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquaresError;
			    f2=sumSquares(regFun, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.stepH[i];
                    this.zeroCheck=true;
                }
    		    f[i]=hold0*(1.0 + this.delta/2.0D);
    		    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.stepH[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 - this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquaresError;
			    f3=sumSquares(regFun, f);
			    f[i]=pmin[i];
			    f[j]=pmin[j];
			    hold0=f[i];
                if(hold0==0.0D){
                    hold0=this.stepH[i];
                    this.zeroCheck=true;
                }
			    f[i]=hold0*(1.0 - this.delta/2.0D);
			    hold0=f[j];
                if(hold0==0.0D){
                    hold0=this.stepH[j];
                    this.zeroCheck=true;
                }
			    f[j]=hold0*(1.0 - this.delta/2.0D);
	            this.lastSSnoConstraint=this.sumOfSquaresError;
			    f4=sumSquares(regFun, f);
			    stat[i][j]=(f1-f2-f3+f4)/(this.delta*this.delta);
		    }
	    }

        double ss=0.0D;
        double sc=0.0D;
	    for(int i=0; i<this.nData; i++){
            for(int j=0; j<nXarrays; j++)xd[j]=this.xData[j][i];
            if(this.multipleY){
	            this.yCalc[i] = ((RegressionFunction2)regFun).function(pmin, xd, i);
	        }
	        else{
	            this.yCalc[i] = ((RegressionFunction)regFun).function(pmin, xd);
	        }
	        this.residual[i] = this.yCalc[i]-this.yData[i];
	        ss += Fmath.square(this.residual[i]);
	        this.residualW[i] = this.residual[i]/this.weight[i];
	        sc += Fmath.square(this.residualW[i]);
	    }
	    this.sumOfSquaresError = ss;
	    double varY = ss/(this.nData-np);
	    double sdY = Math.sqrt(varY);
	    this.chiSquare=sc;
	    this.reducedChiSquare=sc/(this.nData-np);

        // calculate reduced sum of squares
        double red=1.0D;
        if(!this.weightOpt && !this.trueFreq)red=this.sumOfSquaresError/(this.nData-np);

        // calculate pseudo errors  -  reduced sum of squares over second partial derivative
        for(int i=0; i<np; i++){
            pseudoSd[i] = (2.0D*this.delta*red*Math.abs(pmin[i]))/(grad[i][1]-grad[i][0]);
            if(pseudoSd[i]>=0.0D){
                pseudoSd[i] = Math.sqrt(pseudoSd[i]);
            }
            else{
                pseudoSd[i] = Double.NaN;
            }
        }

        // calculate covariance matrix
	    if(np==1){
	        hold0=pmin[0];
            if(hold0==0.0D)hold0=this.stepH[0];
	        stat[0][0]=1.0D/stat[0][0];
		    this.covar[0][0] = stat[0][0]*red*hold0*hold0;
		    if(covar[0][0]>=0.0D){
			    coeffSd[0]=Math.sqrt(this.covar[0][0]);
			    corrCoeff[0][0]=1.0D;
			}
	        else{
			    coeffSd[0]=Double.NaN;
			    corrCoeff[0][0]=Double.NaN;
			    this.posVarFlag=false;
			}
		}
		else{
            Matrix cov = new Matrix(stat);
		    if(this.supressErrorMessages)cov.supressErrorMessage();
		    double determinant = cov.determinant();
		    if(determinant==0){
		        this.invertFlag=false;
		    }
		    else{
                cov = cov.inverse();
                this.invertFlag = cov.getMatrixCheck();
            }
            if(this.invertFlag==false)flag--;
            stat = cov.getArrayCopy();

	        this.posVarFlag=true;
	        if (this.invertFlag){
		        for (int i=0; i<np; ++i){
		            hold0=pmin[i];
                    if(hold0==0.0D)hold0=this.stepH[i];
			        for (int j=i; j<np;++j){
			            hold1=pmin[j];
                        if(hold1==0.0D)hold1=this.stepH[j];
				        this.covar[i][j] = 2.0D*stat[i][j]*red*hold0*hold1;
				        this.covar[j][i] = this.covar[i][j];
			        }
			        if(covar[i][i]>=0.0D){
			            coeffSd[i]=Math.sqrt(this.covar[i][i]);
			        }
			        else{
			            coeffSd[i]=Double.NaN;
			            this.posVarFlag=false;
			        }
		        }

		        for (int i=0; i<np; ++i){
			        for (int j=0; j<np; ++j){
			            if((coeffSd[i]!= Double.NaN) && (coeffSd[j]!= Double.NaN)){
			                this.corrCoeff[i][j] = this.covar[i][j]/(coeffSd[i]*coeffSd[j]);
			            }
			            else{
			                this.corrCoeff[i][j]= Double.NaN;
			            }
			        }
		        }
 	        }
 	        else{
		        for (int i=0; i<np; ++i){
			        for (int j=0; j<np;++j){
			            this.covar[i][j] = Double.NaN;
			            this.corrCoeff[i][j] = Double.NaN;
			        }
			        coeffSd[i]=Double.NaN;
		        }
		    }
		}
	    if(this.posVarFlag==false)flag--;

	    for(int i=0; i<this.nParam; i++){
		    this.bestSd[i] = coeffSd[i];
		    this.tValues[i] = this.best[i]/this.bestSd[i];
		    double atv = Math.abs(this.tValues[i]);
		    if(atv!=atv){
		        this.pValues[i] = Double.NaN;
		    }
		    else{
		        this.pValues[i] = 1.0 - Stat.studentTcdf(-atv, atv, this.degreesOfFreedom);
		    }
	    }

        if(this.nXarrays==1 && this.nYarrays==1){
            this.xyR = Stat.corrCoeff(this.xData[0], this.yData, this.weight);
        }
        this.yyR = Stat.corrCoeff(this.yCalc, this.yData, this.weight);

        // Coefficient of determination
        this.yMean = Stat.mean(this.yData);
        this.yWeightedMean = Stat.mean(this.yData, this.weight);

        this.sumOfSquaresTotal = 0.0;
        for(int i=0; i<this.nData; i++){
            this.sumOfSquaresTotal += Fmath.square((this.yData[i] - this.yWeightedMean)/weight[i]);
        }

        this.sumOfSquaresRegrn = this.sumOfSquaresTotal - this.chiSquare;
        if(this.sumOfSquaresRegrn<0.0)this.sumOfSquaresRegrn=0.0;

        this.multR = this.sumOfSquaresRegrn/this.sumOfSquaresTotal;

        // Calculate adjusted multiple coefficient of determination
        this.adjustedR = Double.NaN;
        this.multipleF = Double.NaN;
        if((this.nData - this.nXarrays - 1)>0){
            this.adjustedR = 1.0 - (1.0 - this.multR)*(this.nData - 1 )/(this.nData - this.nXarrays - 1);
        }

        // F-ratio
        this.multipleF = this.multR*(this.nData-this.nXarrays-1.0)/((1.0D-this.multR)*this.nXarrays);
        if(this.multipleF>=0.0)this.multipleFprob = Stat.fTestProb(this.multipleF, this.nXarrays, this.nData-this.nXarrays-1);

        // Durbin-Watson d statistic
        this.calcDurbinWatson();

        return flag;

	}

	// Calculate the DurbinWatson d statistic
	protected void calcDurbinWatson(){
	    double dNumer = 0.0;
	    double temp = 0.0;
	    for(int i=1; i<this.nData; i++){
	        temp = this.residual[i]-this.residual[i-1];
	        dNumer += temp*temp;
	    }
	    double dDenom = 0.0;
	    for(int i=0; i<this.nData; i++)dDenom += this.residual[i]*this.residual[i];
	    this.dDurbinWatson = dNumer/dDenom;
	    this.dDurbinWatsonDone = true;
	}

	// Return the DurbinWatson d statistic
	protected double getDurbinWatsonD(){
	    if(!this.dDurbinWatsonDone)this.calcDurbinWatson();
	    return this.dDurbinWatson;
	}


	// Print the results of the regression
	// File name provided
	// prec = truncation precision
	public void print(String filename, int prec){
	    this.prec = prec;
	    this.print(filename);
	}

	// Print the results of the regression
	// No file name provided
	// prec = truncation precision
	public void print(int prec){
	    this.prec = prec;
		String filename="RegressionOutput.txt";
        this.print(filename);
	}

    // Print the results of the regression
	// File name provided
	// default value for truncation precision
	public void print(String filename){
	    if(filename.indexOf('.')==-1)filename = filename+".txt";
	    FileOutput fout = new FileOutput(filename, 'n');
	    fout.dateAndTimeln(filename);
	    fout.println(this.graphTitle);
	    paraName = new String[this.nParam];
	    if(lastMethod==38)paraName = new String[3];
	    if(this.bestPolyFlag)fout.println("This is the best fit found by the method bestPolynomial");
        if(weightOpt){
            fout.println("Weighted Least Squares Minimisation");
        }
        else{
            fout.println("Unweighted Least Squares Minimisation");
        }
        switch(this.lastMethod){
	        case 0: fout.println("Linear Regression with intercept");
                    fout.println("y = c[0] + c[1]*x1 + c[2]*x2 +c[3]*x3 + . . .");
                    for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 1: fout.println("Polynomial (with degree = " + (nParam-1) + "), Fitting: Linear Regression");
	                fout.println("y = c[0] + c[1]*x + c[2]*x^2 +c[3]*x^3 + . . .");
	                for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 2: fout.println("Generalised linear regression");
	                fout.println("y = c[0]*f1(x) + c[1]*f2(x) + c[2]*f3(x) + . . .");
	                for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 3: fout.println("Nelder and Mead Simplex Non-linear Regression");
	                fout.println("y = f(x1, x2, x3 . . ., c[0], c[1], c[2] . . .");
	                fout.println("y is non-linear with respect to the c[i]");
	                for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.nonLinearPrint(fout);
	                break;
	        case 4: fout.println("Fitting to a Normal (Gaussian) distribution");
	                fout.println("y = (yscale/(sd.sqrt(2.pi)).exp(0.5.square((x-mean)/sd))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mean";
	                paraName[1]="sd";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 5: fout.println("Fitting to a Lorentzian distribution");
	                fout.println("y = (yscale/pi).(gamma/2)/((x-mean)^2+(gamma/2)^2)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mean";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
	                break;
            case 6: fout.println("Fitting to a Poisson distribution");
	                fout.println("y = yscale.mu^k.exp(-mu)/mu!");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mean";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 7: fout.println("Fitting to a Two Parameter Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale/sigma)*exp((x - mu)/sigma))*exp(-exp((x-mu)/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 8: fout.println("Fitting to a Two Parameter Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale/sigma)*exp(-(x - mu)/sigma))*exp(-exp(-(x-mu)/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 9: fout.println("Fitting to a One Parameter Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(x/sigma))*exp(-exp(x/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
		            paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 10: fout.println("Fitting to a One Parameter Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(-x/sigma))*exp(-exp(-x/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
		            paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 11: fout.println("Fitting to a Standard Minimum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(x))*exp(-exp(x))");
	                fout.println("Linear regression used to fit y = yscale*z where z = exp(x))*exp(-exp(x)))");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.linearPrint(fout);
                    break;
            case 12: fout.println("Fitting to a Standard Maximum Order Statistic Gumbel [Type 1 Extreme Value] Distribution");
	                fout.println("y = (yscale)*exp(-x))*exp(-exp(-x))");
	                fout.println("Linear regression used to fit y = yscale*z where z = exp(-x))*exp(-exp(-x)))");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.linearPrint(fout);
                    break;
	        case 13: fout.println("Fitting to a Three Parameter Frechet [Type 2 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*((x - mu)/sigma)^(-gamma-1)*exp(-((x-mu)/sigma)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 14: fout.println("Fitting to a Two parameter Frechet [Type2  Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*(x/sigma)^(-gamma-1)*exp(-(x/sigma)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="sigma";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 15: fout.println("Fitting to a Standard Frechet [Type 2 Extreme Value] Distribution");
	                fout.println("y = yscale.gamma*(x)^(-gamma-1)*exp(-(x)^-gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="gamma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
	        case 16: fout.println("Fitting to a Three parameter Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*((x - mu)/sigma)^(gamma-1)*exp(-((x-mu)/sigma)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 17: fout.println("Fitting to a Two parameter Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.(gamma/sigma)*(x/sigma)^(gamma-1)*exp(-(x/sigma)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="sigma";
	                paraName[1]="gamma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 18: fout.println("Fitting to a Standard Weibull [Type 3 Extreme Value] Distribution");
	                fout.println("y = yscale.gamma*(x)^(gamma-1)*exp(-(x)^gamma");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="gamma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
		    case 19: fout.println("Fitting to a Two parameter Exponential Distribution");
	                fout.println("y = (yscale/sigma)*exp(-(x-mu)/sigma)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
		            if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 20: fout.println("Fitting to a One parameter Exponential Distribution");
	                fout.println("y = (yscale/sigma)*exp(-x/sigma)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
  	        case 21: fout.println("Fitting to a Standard Exponential Distribution");
	                fout.println("y = yscale*exp(-x)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                if(this.scaleFlag)paraName[0]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 22: fout.println("Fitting to a Rayleigh Distribution");
	                fout.println("y = (yscale/sigma)*(x/sigma)*exp(-0.5*(x/sigma)^2)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="sigma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 23: fout.println("Fitting to a Two Parameter Pareto Distribution");
	                fout.println("y = yscale*(alpha*beta^alpha)/(x^(alpha+1))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 24: fout.println("Fitting to a One Parameter Pareto Distribution");
	                fout.println("y = yscale*(alpha)/(x^(alpha+1))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 25: fout.println("Fitting to a Sigmoidal Threshold Function");
	                fout.println("y = yscale/(1 + exp(-slopeTerm(x - theta)))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="slope term";
	                paraName[1]="theta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
             case 26: fout.println("Fitting to a Rectangular Hyperbola");
	                fout.println("y = yscale.x/(theta + x)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="theta";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 27: fout.println("Fitting to a Scaled Heaviside Step Function");
	                fout.println("y = yscale.H(x - theta)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="theta";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 28: fout.println("Fitting to a Hill/Sips Sigmoid");
	                fout.println("y = yscale.x^n/(theta^n + x^n)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="theta";
	                paraName[1]="n";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 29: fout.println("Fitting to a Shifted Pareto Distribution");
	                fout.println("y = yscale*(alpha*beta^alpha)/((x-theta)^(alpha+1))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                paraName[2]="theta";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
	        case 30: fout.println("Fitting to a Logistic distribution");
	                fout.println("y = yscale*exp(-(x-mu)/beta)/(beta*(1 + exp(-(x-mu)/beta))^2");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="beta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 31: fout.println("Fitting to a Beta distribution - [0, 1] interval");
	                fout.println("y = yscale*x^(alpha-1)*(1-x)^(beta-1)/B(alpha, beta)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 32: fout.println("Fitting to a Beta distribution - [min, max] interval");
	                fout.println("y = yscale*(x-min)^(alpha-1)*(max-x)^(beta-1)/(B(alpha, beta)*(max-min)^(alpha+beta-1)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                paraName[2]="min";
	                paraName[3]="max";
	                if(this.scaleFlag)paraName[4]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 33: fout.println("Fitting to a Three Parameter Gamma distribution");
	                fout.println("y = yscale*((x-mu)/beta)^(gamma-1)*exp(-(x-mu)/beta)/(beta*Gamma(gamma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="beta";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 34: fout.println("Fitting to a Standard Gamma distribution");
	                fout.println("y = yscale*x^(gamma-1)*exp(-x)/Gamma(gamma)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="gamma";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 35: fout.println("Fitting to an Erang distribution");
	                fout.println("y = yscale*lambda^k*x^(k-1)*exp(-x*lambda)/(k-1)!");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="lambda";
	                if(this.scaleFlag)paraName[1]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 36: fout.println("Fitting to a two parameter log-normal distribution");
	                fout.println("y = (yscale/(x.sigma.sqrt(2.pi)).exp(0.5.square((log(x)-muu)/sigma))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mu";
	                paraName[1]="sigma";
	                if(this.scaleFlag)paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 37: fout.println("Fitting to a three parameter log-normal distribution");
	                fout.println("y = (yscale/((x-alpha).beta.sqrt(2.pi)).exp(0.5.square((log(x-alpha)/gamma)/beta))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="alpha";
	                paraName[1]="beta";
	                paraName[2]="gamma";
	                if(this.scaleFlag)paraName[3]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 38: fout.println("Fitting to a Normal (Gaussian) distribution with fixed parameters");
	                fout.println("y = (yscale/(sd.sqrt(2.pi)).exp(0.5.square((x-mean)/sd))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="mean";
	                paraName[1]="sd";
	                paraName[2]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 39: fout.println("Fitting to a EC50 dose response curve (four parameter logistic)");
	                fout.println("y = top + (bottom - top)/(1 + (x/EC50)^HillSlope)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="top";
	                paraName[1]="bottom";
	                paraName[2]="EC50";
	                paraName[3]="Hill Slope";
                    this.nonLinearPrint(fout);
                    break;
            case 40: fout.println("Fitting to a EC50 dose response curve (four parameter logistic)");
	                fout.println("y = top + (bottom - top)/(1 + (x/EC50)^HillSlope) [top and bottom fixed]");
	                fout.println("bottom = " + this.bottom);
	                fout.println("top =    " + this.top);
	                fout.println();
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="EC50";
	                paraName[1]="Hill Slope";
                    this.nonLinearPrint(fout);
                    break;
            case 41: fout.println("Fitting to a EC50 dose response curve - bottom constrained to be zero or positive");
	                fout.println("y = top + (bottom - top)/(1 + (x/EC50)^HillSlope)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="top";
	                paraName[1]="bottom";
	                paraName[2]="EC50";
	                paraName[3]="Hill Slope";
                    this.nonLinearPrint(fout);
                    break;
            case 42: fout.println("Fitting to a five parameter logistic");
	                fout.println("y = top + (bottom - top)/(1 + (x/C50)^HillSlope)^asymm [top and bottom fixed]");
	                fout.println("bottom = " + this.bottom);
	                fout.println("top =    " + this.top);
	                fout.println();
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="C50";
	                paraName[1]="HillSlope";
	                paraName[2]="asymm";
                    this.nonLinearPrint(fout);
                    break;
            case 43: fout.println("Fitting to an exponential");
	                fout.println("y = yscale.exp(A.x)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="A";
	                if(this.scaleFlag)paraName[1]="y scale";
	                this.nonLinearPrint(fout);
                    break;
            case 44: fout.println("Fitting to multiple exponentials");
	                fout.println("y = Sum[Ai.exp(Bi.x)], i=1 to n");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                for(int i=0;i<this.nParam;i+=2){
	                    this.paraName[i]="A["+(i+1)+"]";
	                    this.paraName[i+1]="B["+(i+1)+"]";
	                }
	                this.nonLinearPrint(fout);
                    break;
            case 45: fout.println("Fitting to one minus an exponential");
	                fout.println("y = A(1 - exp(B.x)");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="A";
	                paraName[1]="B";
	                this.nonLinearPrint(fout);
                    break;
            case 46: fout.println("Fitting to a constant");
	                fout.println("y = a");
	                fout.println("Stat weighted mean used to fit the data");
	                paraName[0]="a";
	                this.linearPrint(fout);
                    break;
            case 47: fout.println("Linear Regression with fixed intercept");
                    fout.println("y = fixed intercept + c[0]*x1 + c[1]*x2 +c[2]*x3 + . . .     ");
                    for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 48: fout.println("Polynomial (with degree = " + nParam + ") and fixed intercept, Fitting: Linear Regression");
	                fout.println("y = fixed intercept + c[0]*x + c[1]*x^2 +c[2]*x^3 + . . .");
	                for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.linearPrint(fout);
	                break;
	        case 49: fout.println("Fitting multiple Gaussian distributions");
	        	    fout.println("y = Sum(A[i]/(sd[i].sqrt(2.pi)).exp(0.5.square((x-mean[i])/sd[i])) = yscale.Sum(f[i]/(sd[i].sqrt(2.pi)).exp(0.5.square((x-mean[i])/sd[i]))");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                for(int i=0; i<this.nGaussians; i++){
	                    paraName[3*i]="mean[" + i + "]";
	                    paraName[3*i+1]="sd[" + i + "]";
	                    paraName[3*i+2]="A[" + i + "]";
	                }
	                if(this.scaleFlag)paraName[3*this.nGaussians]="y scale";
                    this.nonLinearPrint(fout);
                    break;
            case 50: fout.println("Fitting to a non-integer polynomial");
	                fout.println("y = c[0] + c[1]*x + c[2]*x^c[3]");
	                for(int i=0;i<this.nParam;i++)this.paraName[i]="c["+i+"]";
                    this.nonLinearPrint(fout);
	                break;
	        case 51: fout.println("Five parameter logistic function");
	                fout.println("y = top + (bottom - top)/((1 + (x/y50)^HillSlope)^asymm");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="top";
	                paraName[1]="bottom";
	                paraName[2]="y50";
	                paraName[3]="HillSlope";
	                paraName[3]="asymm";
                    this.nonLinearPrint(fout);
                    break;
           case 52: fout.println("Fitting to a Shifted Rectangular Hyperbola");
	                fout.println("y = A.x/(theta + x) + alpha");
	                fout.println("Nelder and Mead Simplex used to fit the data");
	                paraName[0]="theta";
	                paraName[1]="alpha";
	                paraName[2]="A";
                    this.nonLinearPrint(fout);
                    break;

            default: throw new IllegalArgumentException("Method number (this.lastMethod) not found");

		    }

		fout.close();
	}

	// Print the results of the regression
	// No file name provided
	public void print(){
		    String filename="RegressOutput.txt";
		    this.print(filename);
	}

	// protected method - print linear regression output
	protected void linearPrint(FileOutput fout){

	    if(this.legendCheck){
            fout.println();
            fout.println("x1 = " + this.xLegend);
            fout.println("y  = " + this.yLegend);
 	    }

        fout.println();
        if(this.lastMethod==47)fout.println("Fixed Intercept = " + this.fixedInterceptL);
        if(this.lastMethod==48)fout.println("Fixed Intercept = " + this.fixedInterceptP);
        fout.printtab(" ", this.field);
        fout.printtab("Best", this.field);
        fout.printtab("Error", this.field);
        fout.printtab("Coefficient of", this.field);
        fout.printtab("t-value  ", this.field);
        fout.println("p-value");

        fout.printtab(" ", this.field);
        fout.printtab("Estimate", this.field);
        fout.printtab("        ", this.field);
        fout.printtab("variation (%)", this.field);
        fout.printtab("t ", this.field);
        fout.println("P > |t|");

        for(int i=0; i<this.nParam; i++){
            fout.printtab(this.paraName[i], this.field);
            fout.printtab(Fmath.truncate(best[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(bestSd[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(Math.abs(bestSd[i]*100.0D/best[i]),this.prec), this.field);
            fout.printtab(Fmath.truncate(tValues[i],this.prec), this.field);
            fout.println(Fmath.truncate((pValues[i]),this.prec));
        }
        fout.println();

        int ii=0;
        if(this.lastMethod<2)ii=1;
        for(int i=0; i<this.nXarrays; i++){
            fout.printtab("x"+String.valueOf(i+ii), this.field);
        }
        fout.printtab("y(expl)", this.field);
        fout.printtab("y(calc)", this.field);
        fout.printtab("weight", this.field);
        fout.printtab("residual", this.field);
        fout.println("residual");

        for(int i=0; i<this.nXarrays; i++){
            fout.printtab(" ", this.field);
        }
        fout.printtab(" ", this.field);
        fout.printtab(" ", this.field);
        fout.printtab(" ", this.field);
        fout.printtab("(unweighted)", this.field);
        fout.println("(weighted)");


        for(int i=0; i<this.nData; i++){
            for(int j=0; j<this.nXarrays; j++){
                fout.printtab(Fmath.truncate(this.xData[j][i],this.prec), this.field);
            }
            fout.printtab(Fmath.truncate(this.yData[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yCalc[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.weight[i],this.prec), this.field);
            fout.printtab(Fmath.truncate(this.residual[i],this.prec), this.field);
            fout.println(Fmath.truncate(this.residualW[i],this.prec));
         }
        fout.println();
        fout.println("Sum of squares " + Fmath.truncate(this.sumOfSquaresError, this.prec));
		if(this.trueFreq){
		    fout.printtab("Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.chiSquare,this.prec));
            fout.printtab("Reduced Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
            fout.printtab("Chi Square (Poissonian bins) Probability");
		    fout.println(Fmath.truncate((1.0D-Stat.chiSquareProb(this.chiSquare, this.nData-this.nXarrays)),this.prec));
		}
		else{
		    if(weightOpt){
	            fout.printtab("Chi Square");
		        fout.println(Fmath.truncate(this.chiSquare,this.prec));
                fout.printtab("Reduced Chi Square");
		        fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
		    }
		}
	    fout.println(" ");
	    if(this.lastMethod!=46){
	        if(this.nXarrays==1 && this.nYarrays==1 && this.lastMethod!=47 && this.lastMethod!=48){
	            fout.println("Correlation: x - y data");
		        fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient (R)");
	            fout.println(Fmath.truncate(this.xyR,this.prec));
	            if(Math.abs(this.xyR)<=1.0D){
		            fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient Probability");
		            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.xyR, this.nData-2),this.prec));
                }
            }

    	    fout.println(" ");
    	    fout.println("Correlation: y(experimental) - y(calculated)");
            fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient");
	        fout.println(Fmath.truncate(this.yyR, this.prec));
	        if(Math.abs(this.yyR)<=1.0D){
		        fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient Probability");
	            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.yyR, this.nData-2),this.prec));
            }
            fout.println();
            if(this.chiSquare!=0.0D){
                fout.println("Correlation coefficients");
                fout.printtab(" ", this.field);
                for(int i=0; i<this.nParam;i++){
                    fout.printtab(paraName[i], this.field);
                }
                fout.println();

                for(int j=0; j<this.nParam;j++){
                    fout.printtab(paraName[j], this.field);
                    for(int i=0; i<this.nParam;i++){
                        fout.printtab(Fmath.truncate(this.corrCoeff[i][j], this.prec), this.field);
                    }
                    fout.println();
                }
            }
        }

        fout.println(" ");
        fout.printtab("Degrees of freedom");
		fout.println(this.nData - this.nParam);
        fout.printtab("Number of data points");
		fout.println(this.nData);
        fout.printtab("Number of estimated paramaters");
		fout.println(this.nParam);
        fout.println();
        fout.printtab("Durbin-Watson d statistic");
        fout.println(Fmath.truncate(this.getDurbinWatsonD(), this.prec));
        fout.println();

        if(this.bestPolyFlag){

            fout.println("Method bestPolynomial search history");
            fout.println("F-probability significance level: " + this.fProbSignificance + " (" + this.fProbSignificance*100.0 + " %)");
            fout.println("Degree of best fit polynomial " + this.bestPolynomialDegree);
            fout.println(" ");

            fout.print("Polynomial degree", 2*field);
            fout.print("chi square", 2*field);
            fout.print("F-ratio", field);
            fout.print("F-probability", field+2);
            fout.println("F-value at the");

            fout.print("comparison", 2*field);
            fout.print("comparison", 2*field);
            fout.print("   ", field);
            fout.print("   ", field+2);
            fout.println("significance level");

            int nAttempts = (Integer)this.bestPolyArray.get(1);
            int[] deg0s = (int[])this.bestPolyArray.get(2);
            int[] deg1s = (int[])this.bestPolyArray.get(3);
            double[] chi0s = (double[])this.bestPolyArray.get(4);
            double[] chi1s = (double[])this.bestPolyArray.get(5);
            double[] fRatios = (double[])this.bestPolyArray.get(6);
            double[] fProbs = (double[])this.bestPolyArray.get(7);
            double[] fSigns = (double[])this.bestPolyArray.get(8);


            for(int i=0; i<nAttempts; i++){
                fout.print(deg0s[i], field);
                fout.print(deg1s[i], field);
                fout.print(Fmath.truncate(chi0s[i], this.prec), field);
                fout.print(Fmath.truncate(chi1s[i], this.prec), field);
                fout.print(Fmath.truncate(fRatios[i], this.prec), field);
                fout.print(Fmath.truncate(fProbs[i], this.prec), field+2);
                fout.println(Fmath.truncate(fSigns[i], this.prec));

            }

        }

        fout.println();
        fout.println("Coefficient of determination,   =                   " + Fmath.truncate(this.multR, this.prec));
        fout.println("Adjusted Coefficient of determination,    =         " + Fmath.truncate(this.adjustedR, this.prec));
        fout.println("Coefficient of determination, F-ratio =             " + Fmath.truncate(this.multipleF, this.prec));
        fout.println("Coefficient of determination, F-ratio probability = " + Fmath.truncate(this.multipleFprob, this.prec));
        fout.println("Total (weighted) sum of squares  =                  " + Fmath.truncate(this.sumOfSquaresTotal, this.prec));
        fout.println("Regression (weighted) sum of squares  =             " + Fmath.truncate(this.sumOfSquaresRegrn, this.prec));
        fout.println("Error (weighted) sum of squares  =                  " + Fmath.truncate(this.chiSquare, this.prec));

        fout.println();
        fout.println("End of file");

		fout.close();
	}


	// protected method - print non-linear regression output
	protected void nonLinearPrint(FileOutput fout){
	    if(this.userSupplied){
            fout.println();
            fout.println("Initial estimates were supplied by the user");
        }
        else{
            fout.println("Initial estimates were calculated internally");
 	    }

 	    switch(this.scaleOpt){
 	        case 1: fout.println();
                    fout.println("Initial estimates were scaled to unity within the regression");
                    break;
            case 2: fout.println();
                    fout.println("Initial estimates were scaled with user supplied scaling factors within the regression");
                    break;
        }

        if(this.legendCheck){
            fout.println();
            fout.println("x1 = " + this.xLegend);
            fout.println("y  = " + this.yLegend);
 	    }

        fout.println();
        if(!this.nlrStatus){
            fout.println("Convergence criterion was not satisfied");
            fout.println("The following results are, or are derived from, the current estimates on exiting the regression method");
            fout.println();
        }

        fout.println("Estimated parameters");
        fout.println("The statistics are obtained assuming that the model behaves as a linear model about the minimum.");
        fout.println("The Hessian matrix is calculated as the numerically derived second derivatives of chi square with respect to all pairs of parameters.");
        if(this.zeroCheck)fout.println("The best estimate/s equal to zero were replaced by the step size in the numerical differentiation!!!");
        fout.println("Consequentlty treat the statistics with great caution");
        if(!this.posVarFlag){
            fout.println("Covariance matrix contains at least one negative diagonal element");
            fout.println(" - all variances are dubious");
            fout.println(" - may not be at a minimum or the model may be so non-linear that the linear approximation in calculating the statisics is invalid");
        }
        if(!this.invertFlag){
            fout.println("Hessian matrix is singular");
            fout.println(" - variances cannot be calculated");
            fout.println(" - may not be at a minimum  or the model may be so non-linear that the linear approximation in calculating the statisics is invalid");
        }

        fout.println(" ");
        if(!this.scaleFlag){
            fout.println("The ordinate scaling factor [yscale, Ao] has been set equal to " + this.yScaleFactor);
            fout.println(" ");
        }
        if(lastMethod==35){
            fout.println("The integer rate parameter, k, was varied in unit steps to obtain a minimum sum of squares");
            fout.println("This value of k was " + this.kayValue);
            fout.println(" ");
        }

        fout.printtab(" ", this.field);
        if(this.invertFlag){
            fout.printtab("Best", this.field);
            fout.printtab("Estimate of", this.field);
            fout.printtab("Coefficient", this.field);
            fout.printtab("t-value", this.field);
            fout.println("p-value");
        }
        else{
            fout.println("Best");
        }

        if(this.invertFlag){
            fout.printtab(" ", this.field);
            fout.printtab("estimate", this.field);
            fout.printtab("the error", this.field);
            fout.printtab("of", this.field);
            fout.printtab("t", this.field);
            fout.println("P > |t|");
        }
        else{
            fout.printtab(" ", this.field);
            fout.println("estimate");
        }

        if(this.invertFlag){
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.println("variation (%)");
        }
        else{
            fout.println("   ");
        }

        if(this.lastMethod==38){
            int nT = 3;
            int ii = 0;
            for(int i=0; i<nT; i++){
                fout.printtab(this.paraName[i], this.field);
                if(this.fixed[i]){
                    fout.printtab(this.values[i]);
                    fout.println(" fixed parameter");
                }
                else{
                    if(this.invertFlag){
                        fout.printtab(Fmath.truncate(best[ii],this.prec), this.field);
                        fout.printtab(Fmath.truncate(bestSd[ii],this.prec), this.field);
                        fout.printtab(Fmath.truncate(Math.abs(bestSd[ii]*100.0D/best[ii]),this.prec), this.field);
                        fout.printtab(Fmath.truncate(tValues[ii],this.prec), this.field);
                        fout.println(Fmath.truncate(pValues[ii],this.prec));
                    }
                    else{
                        fout.println(Fmath.truncate(best[ii],this.prec));
                    }
                    ii++;
                }
            }
        }
        else{
            for(int i=0; i<this.nParam; i++){
                if(this.invertFlag){
                    fout.printtab(this.paraName[i], this.field);
                    fout.printtab(Fmath.truncate(best[i],this.prec), this.field);
                    fout.printtab(Fmath.truncate(bestSd[i],this.prec), this.field);
                    fout.printtab(Fmath.truncate(Math.abs(bestSd[i]*100.0D/best[i]),this.prec), this.field);
                    fout.printtab(Fmath.truncate(tValues[i],this.prec), this.field);
                    fout.println(Fmath.truncate(pValues[i],this.prec));
                }
                else{
                    fout.printtab(this.paraName[i], this.field);
                    fout.println(Fmath.truncate(best[i],this.prec));
                }
            }
        }
        fout.println();

        fout.printtab(" ", this.field);
        fout.printtab("Best", this.field);
        fout.printtab("Pre-min", this.field);
        fout.printtab("Post-min", this.field);
        fout.printtab("Initial", this.field);
        fout.printtab("Fractional", this.field);
        fout.println("Scaling");

        fout.printtab(" ", this.field);
        fout.printtab("estimate", this.field);
        fout.printtab("gradient", this.field);
        fout.printtab("gradient", this.field);
        fout.printtab("estimate", this.field);
        fout.printtab("step", this.field);
        fout.println("factor");


        if(this.lastMethod==38){
            int nT = 3;
            int ii = 0;
            for(int i=0; i<nT; i++){
                fout.printtab(this.paraName[i], this.field);
                if(this.fixed[i]){
                    fout.printtab(this.values[i]);
                    fout.println(" fixed parameter");
                }
                else{
                    fout.printtab(Fmath.truncate(best[ii],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.grad[ii][0],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.grad[ii][1],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.startH[ii],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.stepH[ii],this.prec), this.field);
                    fout.println(Fmath.truncate(this.scale[ii],this.prec));
                    ii++;
                }
            }
        }
        else{
            for(int i=0; i<this.nParam; i++){
                fout.printtab(this.paraName[i], this.field);
                fout.printtab(Fmath.truncate(best[i],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.grad[i][0],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.grad[i][1],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.startH[i],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.stepH[i],this.prec), this.field);
                fout.println(Fmath.truncate(this.scale[i],this.prec));
            }
        }
        fout.println();



        ErrorProp ePeak = null;
        ErrorProp eYscale = null;
        if(this.scaleFlag){
            switch(this.lastMethod){
            case 4: ErrorProp eSigma = new ErrorProp(best[1], bestSd[1]);
                    eYscale = new ErrorProp(best[2]/Math.sqrt(2.0D*Math.PI), bestSd[2]/Math.sqrt(2.0D*Math.PI));
                    ePeak = eYscale.over(eSigma);
                    fout.printsp("Calculated estimate of the peak value = ");
                    fout.println(ErrorProp.truncate(ePeak, prec));
                    break;
            case 5: ErrorProp eGamma = new ErrorProp(best[1], bestSd[1]);
                    eYscale = new ErrorProp(2.0D*best[2]/Math.PI, 2.0D*bestSd[2]/Math.PI);
                    ePeak = eYscale.over(eGamma);
                    fout.printsp("Calculated estimate of the peak value = ");
                    fout.println(ErrorProp.truncate(ePeak, prec));
                    break;

            }
        }
        if(this.lastMethod==25){
            fout.printsp("Calculated estimate of the maximum gradient = ");
            if(this.scaleFlag){
                fout.println(Fmath.truncate(best[0]*best[2]/4.0D, prec));
            }
            else{
                fout.println(Fmath.truncate(best[0]*this.yScaleFactor/4.0D, prec));
            }

        }
        if(this.lastMethod==28){
            fout.printsp("Calculated estimate of the maximum gradient = ");
            if(this.scaleFlag){
                fout.println(Fmath.truncate(best[1]*best[2]/(4.0D*best[0]), prec));
            }
            else{
                fout.println(Fmath.truncate(best[1]*this.yScaleFactor/(4.0D*best[0]), prec));
            }
            fout.printsp("Calculated estimate of the Ka, i.e. theta raised to the power n = ");
            fout.println(Fmath.truncate(Math.pow(best[0], best[1]), prec));
        }
        fout.println();

        if(this.lastMethod==49){
            fout.println("A[i] values converted to fractional contributions, f[i], and a scaling factor, yscale");
            fout.printtab(" ", this.field);
            if(this.invertFlag){
                fout.printtab("Best", this.field);
                fout.printtab("Estimate of", this.field);
                fout.printtab("Coefficient", this.field);
                fout.printtab("t-value", this.field);
                fout.println("p-value");
            }
            else{
                fout.println("Best");
            }

            if(this.invertFlag){
                fout.printtab(" ", this.field);
                fout.printtab("estimate", this.field);
                fout.printtab("the error", this.field);
                fout.printtab("of", this.field);
                fout.printtab("t", this.field);
                fout.println("P > |t|");
            }
            else{
                fout.printtab(" ", this.field);
                fout.println("estimate");
            }

            if(this.invertFlag){
                fout.printtab(" ", this.field);
                fout.printtab(" ", this.field);
                fout.printtab(" ", this.field);
                fout.println("variation (%)");
            }
            else{
                fout.println("   ");
            }

            for(int i=0; i<this.nGaussians; i++){
                if(this.invertFlag){
                    fout.printtab("f[" + i + "]", this.field);
                    fout.printtab(Fmath.truncate(this.multGaussFract[i],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.multGaussFractErrors[i],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.multGaussCoeffVar[i],this.prec), this.field);
                    fout.printtab(Fmath.truncate(this.multGaussTvalue[i],this.prec), this.field);
                    fout.println(Fmath.truncate(this.multGaussPvalue[i],this.prec));
                }
                else{
                    fout.printtab("f[" + i + "]", this.field);
                    fout.println(Fmath.truncate(this.multGaussFract[i],this.prec));
                }
            }
        }
        if(this.invertFlag){
            fout.printtab("yscale", this.field);
            fout.printtab(Fmath.truncate(this.multGaussScale,this.prec), this.field);
            fout.printtab(Fmath.truncate(this.multGaussScaleError,this.prec), this.field);
            fout.printtab(Fmath.truncate(this.multGaussScaleCoeffVar,this.prec), this.field);
            fout.printtab(Fmath.truncate(this.multGaussScaleTvalue,this.prec), this.field);
            fout.println(Fmath.truncate(this.multGaussScalePvalue,this.prec));
        }
        else{
            fout.printtab("yscale", this.field);
            fout.println(Fmath.truncate(this.multGaussScale,this.prec));
        }
        fout.println();

        int kk=0;
        for(int j=0; j<nYarrays; j++){
            if(this.multipleY)fout.println("Y array " + j);

            for(int i=0; i<this.nXarrays; i++){
                fout.printtab("x"+String.valueOf(i), this.field);
            }

            fout.printtab("y(expl)", this.field);
            fout.printtab("y(calc)", this.field);
            fout.printtab("weight", this.field);
            fout.printtab("residual", this.field);
            fout.println("residual");

            for(int i=0; i<this.nXarrays; i++){
                fout.printtab(" ", this.field);
            }
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab(" ", this.field);
            fout.printtab("(unweighted)", this.field);
            fout.println("(weighted)");
            for(int i=0; i<this.nData0; i++){
                for(int jj=0; jj<this.nXarrays; jj++){
                    fout.printtab(Fmath.truncate(this.xData[jj][kk],this.prec), this.field);
                }
                fout.printtab(Fmath.truncate(this.yData[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.yCalc[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.weight[kk],this.prec), this.field);
                fout.printtab(Fmath.truncate(this.residual[kk],this.prec), this.field);
                fout.println(Fmath.truncate(this.residualW[kk],this.prec));
                kk++;
            }
            fout.println();
        }

	    fout.printtab("Sum of squares of the unweighted residuals");
		fout.println(Fmath.truncate(this.sumOfSquaresError,this.prec));
	    if(this.trueFreq){
		    fout.printtab("Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.chiSquare,this.prec));
            fout.printtab("Reduced Chi Square (Poissonian bins)");
		    fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
            fout.printtab("Chi Square (Poissonian bins) Probability");
		    fout.println(Fmath.truncate(1.0D-Stat.chiSquareProb(this.reducedChiSquare,this.degreesOfFreedom),this.prec));
		}
		else{
		    if(weightOpt){
	            fout.printtab("Chi Square");
		        fout.println(Fmath.truncate(this.chiSquare,this.prec));
                fout.printtab("Reduced Chi Square");
		        fout.println(Fmath.truncate(this.reducedChiSquare,this.prec));
		    }
		}

        fout.println(" ");

        if(this.nXarrays==1 && this.nYarrays==1){
	        fout.println("Correlation: x - y data");
		    fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient (R)");
	        fout.println(Fmath.truncate(this.xyR,this.prec));
	        if(Math.abs(this.xyR)<=1.0D){
		        fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient Probability");
		        fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.xyR, this.nData-2),this.prec));
            }
        }

    	fout.println(" ");
    	fout.println("Correlation: y(experimental) - y(calculated)");
        fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient");
	    fout.println(Fmath.truncate(this.yyR, this.prec));
	    if(Math.abs(this.yyR)<=1.0D){
		    fout.printtab(this.weightWord[this.weightFlag] + "Linear Correlation Coefficient Probability");
		    fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(this.yyR, this.nData-2),this.prec));
        }

    	fout.println(" ");
        fout.printtab("Degrees of freedom");
		fout.println(this.degreesOfFreedom);
        fout.printtab("Number of data points");
		fout.println(this.nData);
        fout.printtab("Number of estimated paramaters");
		fout.println(this.nParam);

        fout.println();

        if(this.posVarFlag && this.invertFlag && this.chiSquare!=0.0D){
            fout.println("Parameter - parameter correlation coefficients");
            fout.printtab(" ", this.field);
            for(int i=0; i<this.nParam;i++){
                fout.printtab(paraName[i], this.field);
            }
            fout.println();

            for(int j=0; j<this.nParam;j++){
                fout.printtab(paraName[j], this.field);
                for(int i=0; i<this.nParam;i++){
                    fout.printtab(Fmath.truncate(this.corrCoeff[i][j], this.prec), this.field);
                }
                fout.println();
            }
            fout.println();
        }


        fout.println();
        fout.println("Coefficient of determination, R =                   " + Fmath.truncate(this.multR, this.prec));
        fout.println("Adjusted Coefficient of determination, R' =         " + Fmath.truncate(this.adjustedR, this.prec));
        fout.println("Coefficient of determination, F-ratio =             " + Fmath.truncate(this.multipleF, this.prec));
        fout.println("Coefficient of determination, F-ratio probability = " + Fmath.truncate(this.multipleFprob, this.prec));
        fout.println("Total (weighted) sum of squares  =                  " + Fmath.truncate(this.sumOfSquaresTotal, this.prec));
        fout.println("Regression (weighted) sum of squares  =             " + Fmath.truncate(this.sumOfSquaresRegrn, this.prec));
        fout.println("Error (weighted) sum of squares  =                  " + Fmath.truncate(this.chiSquare, this.prec));

        fout.println();

        fout.println();
        fout.printtab("Durbin-Watson d statistic");
        fout.println(Fmath.truncate(this.getDurbinWatsonD(), this.prec));
        fout.println();


        fout.println();
        fout.printtab("Number of iterations taken");
        fout.println(this.nIter);
        fout.printtab("Maximum number of iterations allowed");
        fout.println(this.nMax);
        fout.printtab("Number of restarts taken");
        fout.println(this.kRestart);
        fout.printtab("Maximum number of restarts allowed");
        fout.println(this.konvge);
        fout.printtab("Standard deviation of the simplex at the minimum");
        fout.println(Fmath.truncate(this.simplexSd, this.prec));
        fout.printtab("Convergence tolerance");
        fout.println(this.fTol);
        switch(minTest){
            case 0: fout.println("simplex sd < the tolerance times the mean of the absolute values of the y values");
                    break;
            case 1: fout.println("simplex sd < the tolerance");
                    break;
            case 2: fout.println("simplex sd < the tolerance times the square root(sum of squares/degrees of freedom");
                    break;
        }
        fout.println("Step used in numerical differentiation to obtain Hessian matrix");
        fout.println("d(parameter) = parameter*"+this.delta);

        fout.println();
        fout.println("End of file");
		fout.close();
	}

	// plot calculated y against experimental y
	// title provided
    public void plotYY(String title){
        this.graphTitle = title;
        int ncurves = 2;
        int npoints = this.nData0;
        double[][] data = PlotGraph.data(ncurves, npoints);

        int kk = 0;
        for(int jj=0; jj<this.nYarrays; jj++){

            // fill first curve with experimental versus best fit values
            for(int i=0; i<nData0; i++){
                data[0][i]=this.yData[kk];
                data[1][i]=this.yCalc[kk];
                kk++;
            }

            // Create a title
            String title0 = this.setGandPtitle(this.graphTitle);
            if(this.multipleY)title0 = title0 + "y array " + jj;
            String title1 = "Calculated versus experimental y values";

            // Calculate best fit straight line between experimental and best fit values
            Regression yyRegr = new Regression(this.yData, this.yCalc, this.weight);
            yyRegr.linear();
            double[] coef = yyRegr.getCoeff();
            data[2][0]=Fmath.minimum(this.yData);
            data[3][0]=coef[0]+coef[1]*data[2][0];
            data[2][1]=Fmath.maximum(this.yData);
            data[3][1]=coef[0]+coef[1]*data[2][1];

            PlotGraph pg = new PlotGraph(data);
            if(plotWindowCloseChoice){
                pg.setCloseChoice(2);
            }
            else{
                pg.setCloseChoice(1);
            }

            pg.setGraphTitle(title0);
            pg.setGraphTitle2(title1);
            pg.setXaxisLegend("Experimental y value");
            pg.setYaxisLegend("Calculated y value");
            int[] popt = {1, 0};
            pg.setPoint(popt);
            int[] lopt = {0, 3};
            pg.setLine(lopt);

            pg.plot();
        }
    }

    //Creates a title
    protected String setGandPtitle(String title){
        String title1 = "";
        switch(this.lastMethod){
	        case 0: title1 = "Linear regression (with intercept): "+title;
	                break;
	        case 1: title1 = "Linear(polynomial with degree = " + (nParam-1) + ") regression: "+title;
	                break;
	        case 2: title1 = "General linear regression: "+title;
	                break;
	        case 3: title1 = "Non-linear (simplex) regression: "+title;
	                break;
	        case 4: title1 = "Fit to a Gaussian distribution: "+title;
	                break;
	        case 5: title1 = "Fit to a Lorentzian distribution: "+title;
	                break;
	        case 6:title1 = "Fit to a Poisson distribution: "+title;
	                break;
		    case 7: title1 = "Fit to a Two Parameter Minimum Order Statistic Gumbel distribution: "+title;
	                break;
            case 8: title1 = "Fit to a two Parameter Maximum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 9: title1 = "Fit to a One Parameter Minimum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 10: title1 = "Fit to a One Parameter Maximum Order Statistic Gumbel distribution: "+title;
	                break;
            case 11: title1 = "Fit to a Standard Minimum Order Statistic Gumbel distribution: "+title;
	                break;
            case 12: title1 = "Fit to a Standard Maximum Order Statistic Gumbel distribution: "+title;
	                break;
	        case 13:title1 = "Fit to a Three Parameter Frechet distribution: "+title;
	                break;
	        case 14:title1 = "Fit to a Two Parameter Frechet distribution: "+title;
	                break;
	        case 15:title1 = "Fit to a Standard Frechet distribution: "+title;
	                break;
	        case 16:title1 = "Fit to a Three Parameter Weibull distribution: "+title;
	                break;
	        case 17:title1 = "Fit to a Two Parameter Weibull distribution: "+title;
	                break;
	        case 18:title1 = "Fit to a Standard Weibull distribution: "+title;
	                break;
	        case 19:title1 = "Fit to a Two Parameter Exponential distribution: "+title;
	                break;
	        case 20:title1 = "Fit to a One Parameter Exponential distribution: "+title;
	                break;
	        case 21:title1 = "Fit to a Standard exponential distribution: "+title;
	                break;
	        case 22:title1 = "Fit to a Rayleigh distribution: "+title;
	                break;
	        case 23:title1 = "Fit to a Two Parameter Pareto distribution: "+title;
	                break;
	        case 24:title1 = "Fit to a One Parameter Pareto distribution: "+title;
	                break;
	        case 25:title1 = "Fit to a Sigmoid Threshold Function: "+title;
	                break;
	        case 26:title1 = "Fit to a Rectangular Hyperbola: "+title;
	                break;
	        case 27:title1 = "Fit to a Scaled Heaviside Step Function: "+title;
	                break;
	        case 28:title1 = "Fit to a Hill/Sips Sigmoid: "+title;
	                break;
	        case 29:title1 = "Fit to a Shifted Pareto distribution: "+title;
	                break;
	        case 30:title1 = "Fit to a Logistic distribution: "+title;
                    break;
            case 31:title1 = "Fit to a Beta distribution - interval [0, 1]: "+title;
                    break;
            case 32:title1 = "Fit to a Beta distribution - interval [min, max]: "+title;
                    break;
            case 33:title1 = "Fit to a Three Parameter Gamma distribution]: "+title;
                    break;
            case 34:title1 = "Fit to a Standard Gamma distribution]: "+title;
                    break;
            case 35:title1 = "Fit to an Erlang distribution]: "+title;
                    break;
            case 36:title1 = "Fit to an two parameter log-normal distribution]: "+title;
                    break;
            case 37:title1 = "Fit to an three parameter log-normal distribution]: "+title;
                    break;
            case 38: title1 = "Fit to a Gaussian distribution with fixed parameters: "+title;
	                break;
	        case 39: title1 = "Fit to a EC50 dose response curve (four parameter logistic): "+title;
	                break;
	        case 40: title1 = "Fit to a EC50 dose response curve (top and bottom fixed): "+title;
	                break;
	        case 41: title1 = "Fit to a EC50 dose response curve - bottom constrained [>= 0]: "+title;
	                break;
	        case 42: title1 =  "Fitting to a five parameter logistic function (top and bottom fixed)";
                    break;
	        case 43: title1 = "Fit to an exponential yscale.exp(A.x): "+title;
	                break;
	        case 44: title1 = "Fit to multiple exponentials sum[Ai.exp(Bi.x)]: "+title;
	                break;
	        case 45: title1 = "Fit to an exponential A.(1 - exp(B.x): "+title;
	                break;
	        case 46: title1 = "Fit to a constant a: "+title;
	                break;
	        case 47: title1 = "Linear regression (with fixed intercept): "+title;
	                break;
	        case 48: title1 = "Linear(polynomial with degree = " + (nParam-1) + " and fixed intercept) regression: "+title;
	                break;
	        case 49: title1 =  "Fitting multiple Gaussian distributions";
                    break;
            case 50: title1 =  "Fitting to a non-integer polynomial";
                    break;
            case 51: title1 =  "Fitting to a five parameter logistic function";
                    break;
	        case 52: title1 = "Fit to a shifted Rectangular Hyperbola: "+title;
	                break;
            default: title1 = " "+title;
	    }
	    return title1;
    }

	// plot calculated y against experimental y
	// no title provided
    public void plotYY(){
        plotYY(this.graphTitle);
    }

    // plot experimental x against experimental y and against calculated y
    // linear regression data
	// title provided
    protected int plotXY(String title){
        this.graphTitle = title;
        int flag=0;
        if(!this.linNonLin && this.nParam>0){
            System.out.println("You attempted to use Regression.plotXY() for a non-linear regression without providing the function reference (pointer) in the plotXY argument list");
            System.out.println("No plot attempted");
            flag=-1;
            return flag;
        }
        flag = this.plotXYlinear(title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // Linear regression data
	// no title provided
    public int plotXY(){
        int flag = plotXY(this.graphTitle);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// title provided
	// matching simplex
    protected int plotXY(RegressionFunction g, String title){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y array\nplotXY2 should have been called");
        Object regFun = (Object)g;
        int flag = this.plotXYnonlinear(regFun, title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// title provided
	// matching simplex2
    protected int plotXY2(RegressionFunction2 g, String title){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nsimplex should have been called");
        this.graphTitle = title;
        Object regFun = (Object)g;
        int flag = this.plotXYnonlinear(regFun, title);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// no title provided
	// matches simplex
    protected int plotXY(RegressionFunction g){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y array\nplotXY2 should have been called");
        Object regFun = (Object)g;
        int flag = this.plotXYnonlinear(regFun, this.graphTitle);
        return flag;
    }

    // plot experimental x against experimental y and against calculated y
    // non-linear regression data
	// no title provided
	// matches simplex2
    protected int plotXY2(RegressionFunction2 g){
        if(!this.multipleY)throw new IllegalArgumentException("This method cannot handle singly dimensioned y array\nplotXY should have been called");
        Object regFun = (Object)g;
        int flag = this.plotXYnonlinear(regFun, this.graphTitle);
        return flag;
    }

    // Add legends option
    public void addLegends(){
        int ans = JOptionPane.showConfirmDialog(null, "Do you wish to add your own legends to the x and y axes", "Axis Legends", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if(ans==0){
            this.xLegend = JOptionPane.showInputDialog("Type the legend for the abscissae (x-axis) [first data set]" );
            this.yLegend = JOptionPane.showInputDialog("Type the legend for the ordinates (y-axis) [second data set]" );
            this.legendCheck = true;
        }
    }

    // protected method for plotting experimental x against experimental y and against calculated y
	// Linear regression
	// title provided
    protected int plotXYlinear(String title){
        this.graphTitle = title;
        int flag=0;  //Returned as 0 if plot data can be plotted, -1 if not, -2 if tried multiple regression plot
        if(this.nXarrays>1){
            System.out.println("You attempted to use Regression.plotXY() for a multiple regression");
            System.out.println("No plot attempted");
            flag=-2;
            return flag;
        }

        int ncurves = 2;
        int npoints = 200;
        if(npoints<this.nData0)npoints=this.nData0;
        if(this.lastMethod==11 || this.lastMethod==12 || this.lastMethod==21)npoints=this.nData0;
        double[][] data = PlotGraph.data(ncurves, npoints);
        double xmin =Fmath.minimum(xData[0]);
        double xmax =Fmath.maximum(xData[0]);
        double inc = (xmax - xmin)/(double)(npoints - 1);
        String title1 = " ";
        String title2 = " ";

        for(int i=0; i<nData0; i++){
            data[0][i] = this.xData[0][i];
            data[1][i] = this.yData[i];
        }

        data[2][0]=xmin;
        for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
        if(this.nParam==0){
            switch(this.lastMethod){
	        case 11: title1 = "No regression: Minimum Order Statistic Standard Gumbel (y = exp(x)exp(-exp(x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        case 12: title1 = "No regression:  Maximum Order Statistic Standard Gumbel (y = exp(-x)exp(-exp(-x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        case 21: title1 = "No regression:  Standard Exponential (y = exp(-x)): "+this.graphTitle;
                    title2 = " points - experimental values;   line - theoretical curve;   no parameters to be estimated";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.yCalc[i];
	                break;
	        }

        }
        else{
	        switch(this.lastMethod){
	        case 0: title1 = "Linear regression  (y = a + b.x): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = best[0] + best[1]*data[2][i];
	                break;
	        case 1: title1 = "Linear (polynomial with degree = " + (nParam-1) + ") regression: "+this.graphTitle;
	                title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++){
                        double sum=best[0];
                        for(int j=1; j<this.nParam; j++)sum+=best[j]*Math.pow(data[2][i],j);
                        data[3][i] = sum;
                    }
	                break;
	        case 2: title1 = "Linear regression  (y = a.x): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(this.nXarrays==1){
	                    if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                    for(int i=0; i<npoints; i++)data[3][i] = best[0]*data[2][i];
	                }
	                else{
	                    System.out.println("Regression.plotXY(linear): lastMethod, "+lastMethod+",cannot be plotted in two dimensions");
	                    System.out.println("No plot attempted");
	                    flag=-1;
	                }
	                break;
	        case 11: title1 = "Linear regression: Minimum Order Statistic Standard Gumbel (y = a.z where z = exp(x)exp(-exp(x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = best[0]*Math.exp(data[2][i])*Math.exp(-Math.exp(data[2][i]));
	                break;
	        case 12: title1 = "Linear regression:  Maximum Order Statistic Standard Gumbel (y = a.z where z=exp(-x)exp(-exp(-x))): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = best[0]*Math.exp(-data[2][i])*Math.exp(-Math.exp(-data[2][i]));
	                break;
	        case 46: title1 = "Linear regression:  Fit to a constant (y = a): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = best[0];
	                break;
	        case 47: title1 = "Linear regression  (y = fixed intercept + b.x): "+this.graphTitle;
                    title2 = " points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++)data[3][i] = this.fixedInterceptL + best[0]*data[2][i];
	                break;
	        case 48: title1 = "Linear (polynomial with degree = " + nParam + ") regression: "+this.graphTitle;
	                title2 = "Fixed intercept;   points - experimental values;   line - best fit curve";
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";
	                for(int i=0; i<npoints; i++){
                        double sum=this.fixedInterceptP;
                        for(int j=0; j<this.nParam; j++)sum+=best[j]*Math.pow(data[2][i],j+1);
                        data[3][i] = sum;
                    }
	                break;

	        default: System.out.println("Regression.plotXY(linear): lastMethod, "+lastMethod+", either not recognised or cannot be plotted in two dimensions");
	                System.out.println("No plot attempted");
	                flag=-1;
	                return flag;
	        }
	    }

        PlotGraph pg = new PlotGraph(data);
        if(plotWindowCloseChoice){
            pg.setCloseChoice(2);
        }
        else{
            pg.setCloseChoice(1);
        }

        pg.setGraphTitle(title1);
        pg.setGraphTitle2(title2);
        pg.setXaxisLegend(this.xLegend);
        pg.setYaxisLegend(this.yLegend);
        int[] popt = {1,0};
        pg.setPoint(popt);
        int[] lopt = {0,3};
        pg.setLine(lopt);
        if(weightOpt)pg.setErrorBars(0,this.weight);
        pg.plot();

        return flag;
	}

    // protected method for plotting experimental x against experimental y and against calculated y
	// Non-linear regression
	// title provided
    public int plotXYnonlinear(Object regFun, String title){
        this.graphTitle = title;
        RegressionFunction g1 = null;
	    RegressionFunction2 g2 = null;
	    if(this.multipleY){
            g2 = (RegressionFunction2)regFun;
        }
        else{
            g1 = (RegressionFunction)regFun;
        }

        int flag=0;  //Returned as 0 if plot data can be plotted, -1 if not

        if(this.lastMethod<3){
	        System.out.println("Regression.plotXY(non-linear): lastMethod, "+lastMethod+", either not recognised or cannot be plotted in two dimensions");
	        System.out.println("No plot attempted");
	        flag=-1;
	        return flag;
	    }

	    if(this.nXarrays>1){
	        System.out.println("Multiple Linear Regression with more than one independent variable cannot be plotted in two dimensions");
            System.out.println("plotYY() called instead of plotXY()");
            this.plotYY(title);
            flag=-2;
        }
	    else{
	        if(this.multipleY){
	            int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                String title1, title2;
                int kk=0;
                double[] wWeight = new double[this.nData0];
                for(int jj=0; jj<this.nYarrays; jj++){
                    double[][] data = PlotGraph.data(ncurves, npoints);
                    for(int i=0; i<this.nData0; i++){
                        data[0][i] = this.xData[0][kk];
                        data[1][i] = this.yData[kk];
                        wWeight[i] = this.weight[kk];
                        kk++;
                    }
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g2.function(best, xd, jj*this.nData0);
                    }

                    // Create a title
 	                title1 = this.setGandPtitle(title);
    	            title2 = " points - experimental values;   line - best fit curve;  y data array " + jj;
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                    PlotGraph pg = new PlotGraph(data);
                    if(plotWindowCloseChoice){
                        pg.setCloseChoice(2);
                    }
                    else{
                        pg.setCloseChoice(1);
                    }

                    pg.setGraphTitle(title1);
                    pg.setGraphTitle2(title2);
                    pg.setXaxisLegend(this.xLegend);
                    pg.setYaxisLegend(this.yLegend);
                    int[] popt = {1,0};
                    pg.setPoint(popt);
                    int[] lopt = {0,3};
                    pg.setLine(lopt);
                    if(weightOpt)pg.setErrorBars(0,wWeight);

                    pg.plot();
                }
	        }
	        else{
                int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                if(this.lastMethod==6)npoints=this.nData0;
                String title1, title2;
                double[][] data = PlotGraph.data(ncurves, npoints);
                for(int i=0; i<this.nData0; i++){
                    data[0][i] = this.xData[0][i];
                    data[1][i] = this.yData[i];
                }
                if(this.lastMethod==6){
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        data[2][i]=data[0][i];
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(best, xd);
                    }
                }
                else{
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(best, xd);
                    }
                }

                // Create a title
 	            title1 = this.setGandPtitle(title);
    	        title2 = " points - experimental values;   line - best fit curve";
	            if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                PlotGraph pg = new PlotGraph(data);
                if(plotWindowCloseChoice){
                    pg.setCloseChoice(2);
                }
                else{
                    pg.setCloseChoice(1);
                }

                pg.setGraphTitle(title1);
                pg.setGraphTitle2(title2);
                pg.setXaxisLegend(this.xLegend);
                pg.setYaxisLegend(this.yLegend);
                int[] popt = {1,0};
                pg.setPoint(popt);
                int[] lopt = {0,3};
                pg.setLine(lopt);

                if(weightOpt)pg.setErrorBars(0,this.weight);

                pg.plot();
	        }
	    }
        return flag;
	}

    // protected method for plotting experimental x against experimental y and against calculated y
	// Non-linear regression
	// all parameters fixed
    public int plotXYfixed(Object regFun, String title){
        this.graphTitle = title;
        RegressionFunction g1 = null;
	    RegressionFunction2 g2 = null;
	    if(this.multipleY){
            g2 = (RegressionFunction2)regFun;
        }
        else{
            g1 = (RegressionFunction)regFun;
        }

        int flag=0;  //Returned as 0 if plot data can be plotted, -1 if not

        if(this.lastMethod<3){
	        System.out.println("Regression.plotXY(non-linear): lastMethod, "+lastMethod+", either not recognised or cannot be plotted in two dimensions");
	        System.out.println("No plot attempted");
	        flag=-1;
	        return flag;
	    }


	    if(this.nXarrays>1){
	        System.out.println("Multiple Linear Regression with more than one independent variable cannot be plotted in two dimensions");
            System.out.println("plotYY() called instead of plotXY()");
            this.plotYY(title);
            flag=-2;
        }
	    else{
	        if(this.multipleY){
	            int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                String title1, title2;
                int kk=0;
                double[] wWeight = new double[this.nData0];
                for(int jj=0; jj<this.nYarrays; jj++){
                    double[][] data = PlotGraph.data(ncurves, npoints);
                    for(int i=0; i<this.nData0; i++){
                        data[0][i] = this.xData[0][kk];
                        data[1][i] = this.yData[kk];
                        wWeight[i] = this.weight[kk];
                        kk++;
                    }
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g2.function(this.values, xd, jj*this.nData0);
                    }

                    // Create a title
 	                title1 = this.setGandPtitle(title);
    	            title2 = " points - experimental values;   line - best fit curve;  y data array " + jj;
	                if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                    PlotGraph pg = new PlotGraph(data);
                    if(plotWindowCloseChoice){
                        pg.setCloseChoice(2);
                    }
                    else{
                        pg.setCloseChoice(1);
                    }

                    pg.setGraphTitle(title1);
                    pg.setGraphTitle2(title2);
                    pg.setXaxisLegend(this.xLegend);
                    pg.setYaxisLegend(this.yLegend);
                    int[] popt = {1,0};
                    pg.setPoint(popt);
                    int[] lopt = {0,3};
                    pg.setLine(lopt);
                    if(weightOpt)pg.setErrorBars(0,wWeight);

                    pg.plot();
                }
	        }
	        else{
                int ncurves = 2;
                int npoints = 200;
                if(npoints<this.nData0)npoints=this.nData0;
                if(this.lastMethod==6)npoints=this.nData0;
                String title1, title2;
                double[][] data = PlotGraph.data(ncurves, npoints);
                for(int i=0; i<this.nData0; i++){
                    data[0][i] = this.xData[0][i];
                    data[1][i] = this.yData[i];
                }
                if(this.lastMethod==6){
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        data[2][i]=data[0][i];
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(this.values, xd);
                    }
                }
                else{
                    double xmin =Fmath.minimum(xData[0]);
                    double xmax =Fmath.maximum(xData[0]);
                    double inc = (xmax - xmin)/(double)(npoints - 1);
                    data[2][0]=xmin;
                    for(int i=1; i<npoints; i++)data[2][i] = data[2][i-1] + inc;
                    double[] xd = new double[this.nXarrays];
                    for(int i=0; i<npoints; i++){
                        xd[0] = data[2][i];
                        data[3][i] = g1.function(this.values, xd);
                    }
                }

                // Create a title
 	            title1 = this.setGandPtitle(title);
    	        title2 = " points - experimental values;   line - best fit curve";
	            if(weightOpt)title2 = title2 +";   error bars - weighting factors";

                PlotGraph pg = new PlotGraph(data);
                if(plotWindowCloseChoice){
                    pg.setCloseChoice(2);
                }
                else{
                    pg.setCloseChoice(1);
                }


                pg.setGraphTitle(title1);
                pg.setGraphTitle2(title2);
                pg.setXaxisLegend(this.xLegend);
                pg.setYaxisLegend(this.yLegend);
                int[] popt = {1,0};
                pg.setPoint(popt);
                int[] lopt = {0,3};
                pg.setLine(lopt);

                if(weightOpt)pg.setErrorBars(0,this.weight);

                pg.plot();
	        }
	    }
        return flag;
	}


    // Get the non-linear regression status
    // true if convergence was achieved
    // false if convergence not achieved before maximum number of iterations
    //  current values then returned
    public boolean getNlrStatus(){
        return this.nlrStatus;
    }

    // Reset scaling factors (scaleOpt 0 and 1, see below for scaleOpt 2)
    public void setScale(int n){
        if(n<0 || n>1)throw new IllegalArgumentException("The argument must be 0 (no scaling) 1(initial estimates all scaled to unity) or the array of scaling factors");
        this.scaleOpt=n;
    }

    // Reset scaling factors (scaleOpt 2, see above for scaleOpt 0 and 1)
    public void setScale(double[] sc){
        this.scale=sc;
        this.scaleOpt=2;
    }

    // Get scaling factors
    public double[] getScale(){
        return this.scale;
    }

	// Reset the non-linear regression convergence test option
	public void setMinTest(int n){
	    if(n<0 || n>1)throw new IllegalArgumentException("minTest must be 0 or 1");
	    this.minTest=n;
	}

    // Get the non-linear regression convergence test option
	public int getMinTest(){
	    return this.minTest;
	}

	// Get the simplex sd at the minimum
	public double getSimplexSd(){
	    return this.simplexSd;
	}

    // Get the best estimates of the unknown parameters
	public double[] getBestEstimates(){
	    return Conv.copy(best);
	}

	// Get the best estimates of the unknown parameters
	public double[] getCoeff(){
	    return Conv.copy(best);
	}

    // Get the estimates of the standard deviations of the best estimates of the unknown parameters
	public double[] getbestestimatesStandardDeviations(){
	    return Conv.copy(bestSd);
	}

	// Get the estimates of the errors of the best estimates of the unknown parameters
	public double[] getBestEstimatesStandardDeviations(){
	    return Conv.copy(bestSd);
	}

	// Get the estimates of the errors of the best estimates of the unknown parameters
	public double[] getCoeffSd(){
	    return Conv.copy(bestSd);
	}

	// Get the estimates of the errors of the best estimates of the unknown parameters
	public double[] getBestEstimatesErrors(){
	    return Conv.copy(bestSd);
	}

	// Get the unscaled initial estimates of the unknown parameters
	public double[] getInitialEstimates(){
	    return Conv.copy(startH);
	}

	// Get the scaled initial estimates of the unknown parameters
	public double[] getScaledInitialEstimates(){
	    return Conv.copy(startSH);
	}

    // Get the unscaled initial step sizes
	public double[] getInitialSteps(){
	    return Conv.copy(stepH);
	}

	// Get the scaled initial step sizesp
	public double[] getScaledInitialSteps(){
	    return Conv.copy(stepSH);
	}

	// Get the cofficients of variations of the best estimates of the unknown parameters
	public double[] getCoeffVar(){
	    double[] coeffVar = new double[this.nParam];

	    for(int i=0; i<this.nParam; i++){
    	    coeffVar[i]=bestSd[i]*100.0D/best[i];
 	    }
	    return coeffVar;
	}

	// Get the pseudo-estimates of the errors of the best estimates of the unknown parameters
	public double[] getPseudoSd(){
	    return Conv.copy(pseudoSd);
	}

    // Get the pseudo-estimates of the errors of the best estimates of the unknown parameters
	public double[] getPseudoErrors(){
	    return Conv.copy(pseudoSd);
	}

    // Get the t-values of the best estimates
	public double[] getTvalues(){
	    return Conv.copy(tValues);
	}

	// Get the p-values of the best estimates
	public double[] getPvalues(){
	    return Conv.copy(pValues);
	}


	// Get the inputted x values
	public double[][] getXdata(){
	    return Conv.copy(xData);
	}

    // Get the inputted y values
	public double[] getYdata(){
	    return Conv.copy(yData);
	}

	// Get the calculated y values
	public double[] getYcalc(){
	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=this.yCalc[i];
	    return temp;
	}

	// Get the unweighted residuals, y(experimental) - y(calculated)
	public double[] getResiduals(){
	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=this.yData[i]-this.yCalc[i];
	    return temp;
	}

	// Get the weighted residuals, (y(experimental) - y(calculated))/weight
	public double[] getWeightedResiduals(){
	    double[] temp = new double[this.nData];
	    for(int i=0; i<this.nData; i++)temp[i]=(this.yData[i]-this.yCalc[i])/weight[i];
	    return temp;
	}

	// Get the unweighted sum of squares of the residuals
	public double getSumOfSquares(){
	    return this.sumOfSquaresError;
	}

	public double getSumOfUnweightedResidualSquares(){
	    return this.sumOfSquaresError;
	}

	// Get the weighted sum of squares of the residuals
	// returns sum of squares if no weights have been entered
    public double getSumOfWeightedResidualSquares(){
	    return this.chiSquare;
	}

	// Get the chi square estimate
	// returns sum of squares if no weights have been entered
	public double getChiSquare(){
		return this.chiSquare;
	}

	// Get the reduced chi square estimate
	// Returns reduced sum of squares if no weights have been entered
	public double getReducedChiSquare(){
	    return this.reducedChiSquare;
	}

	// Get the total weighted sum of squares
    public double getTotalSumOfWeightedSquares(){
	    return this.sumOfSquaresTotal;
	}

    // Get the regression weighted sum of squares
    public double getRegressionSumOfWeightedSquares(){
	    return this.sumOfSquaresRegrn;
	}

    // Get the Coefficient of Determination
    public double getCoefficientOfDetermination(){
        return this.multR;
    }

    // Get the Coefficient of Determination
    // Retained for backward compatibility
    public double getSampleR(){
        return this.multR;
    }

    // Get the Adjusted Coefficient of Determination
    public double getAdjustedCoefficientOfDetermination(){
        return this.adjustedR;
    }

    // Get the Coefficient of Determination F-ratio
    public double getCoeffDeterminationFratio(){
        return this.multipleF;
    }

    // Get the Coefficient of Determination F-ratio probability
    public double getCoeffDeterminationFratioProb(){
        return this.multipleFprob;
    }


	// Get the covariance matrix
	public double[][] getCovMatrix(){
	    return this.covar;
	}

	// Get the correlation coefficient matrix
	public double[][] getCorrCoeffMatrix(){
	    return this.corrCoeff;
	}

	// Get the number of iterations in nonlinear regression
	public int getNiter(){
	    return this.nIter;
	}


	// Set the maximum number of iterations allowed in nonlinear regression
	public void setNmax(int nmax){
	    this.nMax = nmax;
	}

	// Get the maximum number of iterations allowed in nonlinear regression
	public int getNmax(){
	    return this.nMax;
	}

	// Set the minimum number of iterations required in nonlinear regression
	public void setNmin(int nmin){
	    this.minIter = nmin;
	}

	// Get the minimum number of iterations required in nonlinear regression
	public int getNmin(){
	    return this.minIter;
	}

	// Get the number of restarts in nonlinear regression
	public int getNrestarts(){
	    return this.kRestart;
	}

    // Set the maximum number of restarts allowed in nonlinear regression
	public void setNrestartsMax(int nrs){
	    this.konvge = nrs;
	}

	// Get the maximum number of restarts allowed in nonlinear regression
	public int getNrestartsMax(){
	    return this.konvge;
	}

	// Get the degrees of freedom
	public double getDegFree(){
	    return (this.degreesOfFreedom);
	}

	// Reset the Nelder and Mead reflection coefficient [alpha]
	public void setNMreflect(double refl){
	    this.rCoeff = refl;
	}

	// Get the Nelder and Mead reflection coefficient [alpha]
	public double getNMreflect(){
	    return this.rCoeff;
	}

    // Reset the Nelder and Mead extension coefficient [beta]
	public void setNMextend(double ext){
	    this.eCoeff = ext;
	}
	// Get the Nelder and Mead extension coefficient [beta]
	public double getNMextend(){
	    return this.eCoeff;
	}

	// Reset the Nelder and Mead contraction coefficient [gamma]
	public void setNMcontract(double con){
	    this.cCoeff = con;
	}

	// Get the Nelder and Mead contraction coefficient [gamma]
	public double getNMcontract(){
	    return cCoeff;
	}

	// Set the non-linear regression tolerance
	public void setTolerance(double tol){
	    this.fTol = tol;
	    // this.reassessFtol();
	}


	// Get the non-linear regression tolerance
	public double getTolerance(){
	    return this.fTol;
	}

	// Get the non-linear regression pre and post minimum gradients
	public double[][] getGrad(){
	    return this.grad;
	}

	// Set the non-linear regression fractional step size used in numerical differencing
	public void setDelta(double delta){
	    this.delta = delta;
	}

	// Get the non-linear regression fractional step size used in numerical differencing
	public double getDelta(){
	    return this.delta;
	}

	// Get the non-linear regression statistics Hessian matrix inversion status flag
	public boolean getInversionCheck(){
	    return this.invertFlag;
	}

	// Get the non-linear regression statistics Hessian matrix inverse diagonal status flag
	public boolean getPosVarCheck(){
	    return this.posVarFlag;
	}


    // Test of an additional terms  {extra sum of squares]
    // Enter reduced model parameters first and full model parameters second
    // returns as a Vector:
    //  F-ratio
    //  F-ratio probability
    //  a boolean which = false if model order retained, = true if model order reversed;
    //  chi-square of the reduced model
    //  number of parameters of the reduced model
    //  chi-square of the full model
    //  number of parameters of the full model
    //  F-ratio at the chosen significance level (program default value = 0.05)
    public static Vector<Object> testOfAdditionalTerms(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTerms(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }

    public static Vector<Object> testOfAdditionalTerms(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints, double significance){
        ArrayList<Object> res = Regression.testOfAdditionalTerms_ArrayList(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints);
        Vector<Object> ret = null;
        if(res!=null){
            int n = ret.size();
            ret = new Vector<Object>(n);
            for(int i=0; i<n; i++)ret.addElement(res.get(i));
        }
        return ret;
    }

    // Test of an additional terms  {extra sum of squares]
    public static Vector<Object> testOfAdditionalTerms_Vector(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTerms(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }

    public static Vector<Object> testOfAdditionalTerms_Vector(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints, double significance){
        return Regression.testOfAdditionalTerms(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, significance);
    }

    // Test of an additional terms  {extra sum of squares]
    // Enter reduced model parameters first and full model parameters second
    // returns as an ArrayList:
    //  F-ratio
    //  F-ratio probability
    //  a boolean which = false if model order retained, = true if model order reversed;
    //  chi-square of the reduced model
    //  number of parameters of the reduced model
    //  chi-square of the full model
    //  number of parameters of the full model
    //  F-ratio at the chosen significance level (program default value = 0.05)
    public static ArrayList<Object> testOfAdditionalTerms_ArrayList(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTerms_ArrayList(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }

    public static ArrayList<Object> testOfAdditionalTerms_ArrayList(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints, double significance){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTerms");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        // Probability
        double fProb = 1.0D;
        if(chiSquareR>chiSquareF){
            fProb = Stat.fTestProb(fRatio, degFreedomD, degFreedomF);
        }

        // Return arraylist
        ArrayList<Object> arrayl = new ArrayList<Object>();
        arrayl.add(new Double(fRatio));
        arrayl.add(new Double(fProb));
        arrayl.add(new Boolean(reversed));
        arrayl.add(new Double(chiSquareR));
        arrayl.add(new Integer(nParametersR));
        arrayl.add(new Double(chiSquareF));
        arrayl.add(new Integer(nParametersF));
        arrayl.add(new Integer(nPoints));
        arrayl.add(new Double(Stat.fTestValueGivenFprob(significance, degFreedomD, degFreedomF)));

        return arrayl;
    }

    // Test of an additional terms  {extra sum of squares]
    // return F-ratio only
    public static double testOfAdditionalTermsFratio(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTermsFratio(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }

    public static double testOfAdditionalTermsFratio(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints,  double significance){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTermsFratio");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        return fRatio;
    }


    // Test of an additional terms  {extra sum of squares]
    // return F-distribution probablity only
    public static double testOfAdditionalTermsFprobability(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTermsFprobability(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }

    public static double testOfAdditionalTermsFprobability(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints, double significance){
        int degFreedomR = nPoints - nParametersR;
        int degFreedomF = nPoints - nParametersF;

        // Check that model 2 has the lowest degrees of freedom
        boolean reversed = false;
        if(degFreedomR<degFreedomF){
            reversed = true;
            double holdD = chiSquareR;
            chiSquareR = chiSquareF;
            chiSquareF = holdD;
            int holdI = nParametersR;
            nParametersR = nParametersF;
            nParametersF = holdI;
            degFreedomR = nPoints - nParametersR;
            degFreedomF = nPoints - nParametersF;
            System.out.println("package flanagan.analysis; class Regression; method testAdditionalTermsFprobability");
            System.out.println("the order of the chi-squares has been reversed to give a second chi- square with the lowest degrees of freedom");
        }
        int degFreedomD = degFreedomR - degFreedomF;

        // F ratio
        double numer = (chiSquareR - chiSquareF)/degFreedomD;
        double denom = chiSquareF/degFreedomF;
        double fRatio = numer/denom;

        // Probability
        double fProb = 1.0D;
        if(chiSquareR>chiSquareF){
            fProb = Stat.fTestProb(fRatio, degFreedomD, degFreedomF);
        }

        return fProb;
    }

    // name typo: kept for compatibility
    public static double testOfAdditionalTermsFprobabilty(double chiSquareR, int nParametersR, double chiSquareF, int nParametersF, int nPoints){
        return Regression.testOfAdditionalTermsFprobability(chiSquareR, nParametersR, chiSquareF, nParametersF, nPoints, 0.05);
    }


    // FIT TO SPECIAL FUNCTIONS
	// Fit to a Poisson distribution
	public void poisson(){
	    this.userSupplied = false;
	    this.fitPoisson(0);
	}

	// Fit to a Poisson distribution
	public void poissonPlot(){
	    this.userSupplied = false;
	    this.fitPoisson(1);
	}

	protected void fitPoisson(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=6;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // Check all abscissae are integers
	    for(int i=0; i<this.nData; i++){
	        if(xData[0][i]-Math.floor(xData[0][i])!=0.0D)throw new IllegalArgumentException("all abscissae must be, mathematically, integer values");
	    }

	    // Calculate  x value at peak y (estimate of the distribution mode)
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Double tempd = null;
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate peak value
	    tempd = (Double)ret1.get(4);
	    double peak = tempd.doubleValue();

	    // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = mean;
        if(this.scaleFlag){
            start[1] = peak/(Math.exp(mean*Math.log(mean)-Stat.logFactorial(mean))*Math.exp(-mean));
        }
        step[0] = 0.1D*start[0];
        if(step[0]==0.0D){
            ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[1] = 0.1D*start[1];

	    // Nelder and Mead Simplex Regression
        PoissonFunction f = new PoissonFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;

        Object regFun2 = (Object) f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            this.plotOpt=false;
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
	}


    // FIT TO A NORMAL (GAUSSIAN) DISTRIBUTION

	// Fit to a Gaussian
	public void gaussian(){
	    this.userSupplied = false;
	    this.fitGaussian(0);
	}

	public void normal(){
	    this.userSupplied = false;
	    this.fitGaussian(0);
	}

	// Fit to a Gaussian
	public void gaussianPlot(){
	    this.userSupplied = false;
	    this.fitGaussian(1);
	}

    // Fit to a Gaussian
	public void normalPlot(){
	    this.userSupplied = false;
	    this.fitGaussian(1);
	}

    // Fit data to a Gaussian (normal) probability function
	protected void fitGaussian(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=4;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGaussian(): This implementation of the Gaussian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the Gaussian mean)
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the sd
	    double sd = Math.sqrt(2.0D)*halfWidth(xData[0], yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();
	    ym=ym*sd*Math.sqrt(2.0D*Math.PI);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = mean;
        start[1] = sd;
        if(this.scaleFlag){
            start[2] = ym;
        }
        step[0] = 0.1D*sd;
        step[1] = 0.1D*start[1];
        if(step[1]==0.0D){
            ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[1]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[2] = 0.1D*start[2];

	    // Nelder and Mead Simplex Regression
        GaussianFunction f = new GaussianFunction();
        this.addConstraint(1,-1, 0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;

        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }

	}

	// Fit data to a Gaussian (normal) probability function
    // with option to fix some of the parameters
    // parameter order - mean, sd, scale factor
	public void gaussian(double[] initialEstimates, boolean[] fixed){
	    this.userSupplied = true;
	    this.fitGaussianFixed(initialEstimates, fixed, 0);
	}

	// Fit to a Gaussian
	// with option to fix some of the parameters
    // parameter order - mean, sd, scale factor
	public void normal(double[] initialEstimates, boolean[] fixed){
	    this.userSupplied = true;
	    this.fitGaussianFixed(initialEstimates, fixed, 0);
	}

	// Fit to a Gaussian
	// with option to fix some of the parameters
    // parameter order - mean, sd, scale factor
	public void gaussianPlot(double[] initialEstimates, boolean[] fixed){
	    this.userSupplied = true;
	    this.fitGaussianFixed(initialEstimates, fixed, 1);
	}

    // Fit to a Gaussian
    // with option to fix some of the parameters
    // parameter order - mean, sd, scale factor
	public void normalPlot(double[] initialEstimates, boolean[] fixed){
	    this.userSupplied = true;
	    this.fitGaussianFixed(initialEstimates, fixed, 1);
	}


	// Fit data to a Gaussian (normal) probability function
    // with option to fix some of the parameters
    // parameter order - mean, sd, scale factor
	protected void fitGaussianFixed(double[] initialEstimates, boolean[] fixed, int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=38;
	    this.values = initialEstimates;
	    this.fixed = fixed;
	    this.scaleFlag=true;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGaussian(): This implementation of the Gaussian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

        // Create instance of GaussianFunctionFixed
        GaussianFunctionFixed f = new GaussianFunctionFixed();
        f.fixed = fixed;
        f.param = initialEstimates;

        // Determine unknowns
        int nT = this.nParam;
        for(int i=0; i<this.nParam; i++)if(fixed[i])nT--;
        if(nT==0){
            if(plotFlag==0){
                throw new IllegalArgumentException("At least one parameter must be available for variation by the Regression procedure or GauasianPlot should have been called and not Gaussian");
            }
            else{
                plotFlag = 3;
            }
        }

        double[] start = new double[nT];
        double[] step = new double[nT];
        boolean[] constraint = new boolean[nT];

        // Fill arrays needed by the Simplex
        double xMin = Fmath.minimum(xData[0]);
        double xMax = Fmath.maximum(xData[0]);
        double yMax = Fmath.maximum(yData);
        if(initialEstimates[2]==0.0D){
            if(fixed[2]){
                throw new IllegalArgumentException("Scale factor has been fixed at zero");
            }
            else{
              initialEstimates[2] = yMax;
            }
        }
        int ii = 0;
        for(int i=0; i<this.nParam; i++){
            if(!fixed[i]){
                start[ii] = initialEstimates[i];
                step[ii] = start[ii]*0.1D;
                if(step[ii]==0.0D)step[ii] = (xMax - xMin)*0.1D;
                constraint[ii] = false;
                if(i==1)constraint[ii] = true;
                ii++;
            }
        }
        this.nParam = nT;

	    // Nelder and Mead Simplex Regression
	    for(int i=0; i<this.nParam; i++){
            if(constraint[i])this.addConstraint(i,-1, 0.0D);
        }
        Object regFun2 = (Object)f;
        if(plotFlag!=3)this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(plotFlag==3){
            // Plot results
            int flag = this.plotXYfixed(regFun2, "Gaussian distribution - all parameters fixed");
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}

	// Fit to multiple Gaussians
	public void multipleGaussiansPlot(int nGaussians, double[] initMeans, double[] initSDs, double[] initFracts){
        if(initMeans.length!=nGaussians)throw new IllegalArgumentException("length of initial means array, " + initMeans.length + ", does not equal the number of Gaussians, " + nGaussians);
        if(initSDs.length!=nGaussians)throw new IllegalArgumentException("length of initial standard deviations array, " + initSDs.length + ", does not equal the number of Gaussians, " + nGaussians);
        if(initFracts.length!=nGaussians)throw new IllegalArgumentException("length of initial fractional weights array, " + initFracts.length + ", does not equal the number of Gaussians, " + nGaussians);
        double sum = 0.0;
        for(int i=0; i<nGaussians; i++)sum += initFracts[i];
        if(sum!=1.0){
            System.out.println("Regression method multipleGaussiansPlot: the sum of the initial estimates of the fractional weights, " + sum + ", does not equal 1.0");
            System.out.println("Program continued using the supplied fractional weights");
        }
	    this.fitMultipleGaussians(nGaussians, initMeans, initSDs, initFracts, 1);
	}


    // Fit data to multiple Gaussian (normal) probability functions
	protected void fitMultipleGaussians(int nGaussians, double[] initMeans, double[] initSDs, double[] initFracts, int plotFlag){
	    this.nGaussians = nGaussians;
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=49;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3*this.nGaussians;
	    boolean scaleFlagHold = this.scaleFlag;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
        int nMaxHold = this.nMax;
        if(this.nMax<10000)this.nMax = 10000;

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGaussian(): This implementation of the Gaussian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the Gaussian mean)
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the sd
	    double sd = Math.sqrt(2.0D)*halfWidth(xData[0], yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();


        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        int counter = 0;
        for(int i=0; i<nGaussians; i++){
            start[counter] = initMeans[i];
            step[counter] = Math.abs(0.1D*start[counter]);
            start[counter+1] = initSDs[i];
            step[counter+1] = Math.abs(0.1D*start[counter+1]);
            if(step[counter+1]==0.0D){
                ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	        Double tempdd = null;
	            tempdd = (Double)ret0.get(2);
	 	        double xmax = tempdd.doubleValue();
	 	        if(xmax==0.0D){
	 	            tempdd = (Double)ret0.get(0);
	 	            xmax = tempdd.doubleValue();
	 	        }
	            step[counter+1]=Math.abs(xmax*0.1D);
	        }
	        start[counter+2] = initFracts[i]*Math.sqrt(2.0*Math.PI)*start[counter+1]*ym;
	        step[counter+2] = Math.abs(0.1D*start[counter+2]);
            counter += 3;
        }

        // Nelder and Mead Simplex Regression
        MultipleGaussianFunction f = new MultipleGaussianFunction();

        f.scaleOption = this.scaleFlag;
        double ysf = this.yScaleFactor;
        if(!this.scaleFlag)ysf = 1.0;
        f.scaleFactor = ysf;
        f.nGaussians = this.nGaussians;

        // Add constraints
        for(int i=0; i<this.nGaussians; i++){
            this.addConstraint(3*i+1,-1, 0.0D);
            this.addConstraint(3*i+2,-1, 0.0D);
        }

        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        this.multGaussFract = new double[this.nGaussians];
        this.multGaussFractErrors = new double[this.nGaussians];
        this.multGaussCoeffVar = new double[this.nGaussians];
        this.multGaussTvalue = new double[this.nGaussians];
        this.multGaussPvalue= new double[this.nGaussians];
        for(int i=0; i<nGaussians; i++){
            this.multGaussFractErrors[i] = Double.NaN;
            this.multGaussCoeffVar[i] = Double.NaN;
            this.multGaussTvalue[i] = Double.NaN;
            this.multGaussPvalue[i] = Double.NaN;
        }
        this.multGaussScaleError = Double.NaN;
        this.multGaussScaleCoeffVar = Double.NaN;
        this.multGaussScaleTvalue = Double.NaN;
        this.multGaussScalePvalue = Double.NaN;
        this.multGaussScaleTvalue = Double.NaN;
        this.multGaussScalePvalue = Double.NaN;

        if(this.invertFlag){
            ErrorProp[] multGaussErrorProp = new ErrorProp[this.nGaussians];
            ErrorProp sum = new ErrorProp(0.0, 0.0);
            for(int i=0; i<nGaussians; i++){
                multGaussErrorProp[i] = new ErrorProp(this.best[3*i+2], this.bestSd[3*i+2]);
                sum = sum.plus(multGaussErrorProp[i]);
            }
            ErrorProp epScale = new ErrorProp(0.0, 0.0);
            for(int i=0; i<nGaussians; i++){
                ErrorProp epFract =  multGaussErrorProp[i].over(sum);
                this.multGaussFract[i] = (epFract).getValue();
                this.multGaussFractErrors[i] = (epFract).getError();
                epScale = epScale.plus(multGaussErrorProp[i].over(epFract));
                this.multGaussCoeffVar[i] = 100.0*this.multGaussFractErrors[i]/this.multGaussFract[i];
                this.multGaussTvalue[i] = this.multGaussFract[i]/this.multGaussFractErrors[i];
                double atv = Math.abs(this.multGaussTvalue[i]);
		        if(atv!=atv){
		            this.multGaussPvalue[i] = Double.NaN;
		        }
		        else{
		            this.multGaussPvalue[i] = 1.0 - Stat.studentTcdf(-atv, atv, this.degreesOfFreedom);
		        }
            }
            epScale = epScale.over(this.nGaussians);
            this.multGaussScale = epScale.getValue();
            this.multGaussScaleError = epScale.getError();
            this.multGaussScaleCoeffVar = 100.0*this.multGaussScaleError/this.multGaussScale;
            this.multGaussScaleTvalue = this.multGaussScale/this.multGaussScaleError;
            double atv = Math.abs(this.multGaussScaleTvalue);
		    if(atv!=atv){
		        this.multGaussScalePvalue = Double.NaN;
		    }
		    else{
		        this.multGaussScalePvalue = 1.0 - Stat.studentTcdf(-atv, atv, this.degreesOfFreedom);
		    }
        }

        else{
            double sum = 0.0;
            for(int i=0; i<nGaussians; i++){
                sum += best[3*i+2];
            }
            this.multGaussScale = 0.0;
            for(int i=0; i<nGaussians; i++){
                this.multGaussFract[i] =  best[3*i+2]/sum;
                this.multGaussScale += this.multGaussFract[i];
            }
            this.multGaussScale /= this.nGaussians;
        }

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
        this.nMax = nMaxHold;
        this.scaleFlag = scaleFlagHold;

	}


    // FIT TO LOG-NORMAL DISTRIBUTIONS (TWO AND THREE PARAMETERS)

    // TWO PARAMETER LOG-NORMAL DISTRIBUTION
    // Fit to a two parameter log-normal distribution
	public void logNormal(){
	    this.fitLogNormalTwoPar(0);
	}

	public void logNormalTwoPar(){
	    this.fitLogNormalTwoPar(0);
	}

    // Fit to a two parameter log-normal distribution and plot result
	public void logNormalPlot(){
	    this.fitLogNormalTwoPar(1);
	}

    public void logNormalTwoParPlot(){
	    this.fitLogNormalTwoPar(1);
	}

    // Fit data to a two parameterlog-normal probability function
	protected void fitLogNormalTwoPar(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=36;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitLogNormalTwoPar(): This implementation of the two parameter log-nprmal distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the mu
	    double mu = 0.0D;
	    for(int i=0; i<this.nData; i++)mu += Math.log(xData[0][i]);
	    mu /= this.nData;

	    // Calculate estimate of sigma
	    double sigma = 0.0D;
	    for(int i=0; i<this.nData; i++)sigma += Fmath.square(Math.log(xData[0][i]) - mu);
	    sigma = Math.sqrt(sigma/this.nData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();
	    ym=ym*Math.exp(mu - sigma*sigma/2);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = mu;
        start[1] = sigma;
        if(this.scaleFlag){
            start[2] = ym;
        }
        step[0] = 0.1D*start[0];
        step[1] = 0.1D*start[1];
        if(step[0]==0.0D){
            ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
	    if(step[0]==0.0D){
	        ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[1]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[2] = 0.1D*start[2];

	    // Nelder and Mead Simplex Regression
        LogNormalTwoParFunction f = new LogNormalTwoParFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}


    // THREE PARAMETER LOG-NORMAL DISTRIBUTION
    // Fit to a three parameter log-normal distribution
	public void logNormalThreePar(){
	    this.fitLogNormalThreePar(0);
	}

    // Fit to a three parameter log-normal distribution and plot result
    public void logNormalThreeParPlot(){
	    this.fitLogNormalThreePar(1);
	}

    // Fit data to a three parameter log-normal probability function
	protected void fitLogNormalThreePar(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=37;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=4;
	    if(!this.scaleFlag)this.nParam=3;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitLogNormalThreePar(): This implementation of the three parameter log-normal distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the gamma
	    double gamma = 0.0D;
	    for(int i=0; i<this.nData; i++)gamma += xData[0][i];
	    gamma /= this.nData;

	    // Calculate estimate of beta
	    double beta = 0.0D;
	    for(int i=0; i<this.nData; i++)beta += Fmath.square(Math.log(xData[0][i]) - Math.log(gamma));
	    beta = Math.sqrt(beta/this.nData);

	    // Calculate estimate of alpha
	    ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	Double tempdd = null;
	 	tempdd = (Double)ret0.get(0);
	 	double xmin = tempdd.doubleValue();
	    tempdd = (Double)ret0.get(2);
	 	double xmax = tempdd.doubleValue();
	    double alpha = xmin - (xmax - xmin)/100.0D;;
	    if(xmin==0.0D)alpha -= (xmax - xmin)/100.0D;


	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();
	    ym=ym*(gamma+alpha)*Math.exp(- beta*beta/2);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = alpha;
        start[1] = beta;
        start[2] = gamma;
        if(this.scaleFlag){
            start[3] = ym;
        }
        step[0] = 0.1D*start[0];
        step[1] = 0.1D*start[1];
        step[2] = 0.1D*start[2];
        for(int i=0; i<3; i++){
            if(step[i]==0.0D)step[i]=xmax*0.1D;
        }
        if(this.scaleFlag)step[3] = 0.1D*start[3];

	    // Nelder and Mead Simplex Regression
        LogNormalThreeParFunction f = new LogNormalThreeParFunction();
        this.addConstraint(0,+1,xmin);
        this.addConstraint(1,-1,0.0D);
        this.addConstraint(2,-1,0.0D);

        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}


    // FIT TO A LORENTZIAN DISTRIBUTION

    // Fit data to a lorentzian
	public void lorentzian(){
	    this.fitLorentzian(0);
	}

	public void lorentzianPlot(){
	    this.fitLorentzian(1);
	}

	protected void fitLorentzian(int allTest){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=5;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitLorentzian(): This implementation of the Lorentzian distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the distribution mode)
	    ArrayList ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mean = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = halfWidth(xData[0], yData);

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();
	    ym=ym*sd*Math.PI/2.0D;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = mean;
        start[1] = sd*0.9D;
        if(this.scaleFlag){
            start[2] = ym;
         }
        step[0] = 0.2D*sd;
        if(step[0]==0.0D){
            ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        step[1] = 0.2D*start[1];
        if(this.scaleFlag)step[2] = 0.2D*start[2];

	    // Nelder and Mead Simplex Regression
        LorentzianFunction f = new LorentzianFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }

	}


	// Static method allowing fitting of a data array to one or several of the above distributions
	public static void fitOneOrSeveralDistributions(double[] array){

        int numberOfPoints = array.length;          // number of points
        double maxValue = Fmath.maximum(array);     // maximum value of distribution
        double minValue = Fmath.minimum(array);     // minimum value of distribution
        double span = maxValue - minValue;          // span of distribution

        // Calculation of number of bins and bin width
        int numberOfBins = (int)Math.ceil(Math.sqrt(numberOfPoints));
        double binWidth = span/numberOfBins;
        double averagePointsPerBin = (double)numberOfPoints/(double)numberOfBins;

        // Option for altering bin width
        String comment = "Maximum value:  " + maxValue + "\n";
        comment += "Minimum value:  " + minValue + "\n";
        comment += "Suggested bin width:  " + binWidth + "\n";
        comment += "Giving an average points per bin:  " + averagePointsPerBin + "\n";
        comment += "If you wish to change the bin width enter the new value below \n";
        comment += "and click on OK\n";
        comment += "If you do NOT wish to change the bin width simply click on OK";
        binWidth = Db.readDouble(comment, binWidth);

        // Create output file
        comment = "Input the name of the output text file\n";
        comment += "[Do not forget the extension, e.g.   .txt]";
        String outputTitle = Db.readLine(comment, "fitOneOrSeveralDistributionsOutput.txt");
        FileOutput fout = new FileOutput(outputTitle, 'n');
        fout.println("Fitting a set of data to one or more distributions");
        fout.println("Class Regression/Stat: method fitAllDistributions");
        fout.dateAndTimeln();
        fout.println();
        fout.printtab("Number of points: ");
        fout.println(numberOfPoints);
        fout.printtab("Minimum value: ");
        fout.println(minValue);
        fout.printtab("Maximum value: ");
        fout.println(maxValue);
        fout.printtab("Number of bins: ");
        fout.println(numberOfBins);
        fout.printtab("Bin width: ");
        fout.println(binWidth);
        fout.printtab("Average number of points per bin: ");
        fout.println(averagePointsPerBin);
        fout.println();

        // Choose distributions and perform regression
        String[] comments = {"Gaussian Distribution", "Two parameter Log-normal Distribution", "Three parameter Log-normal Distribution", "Logistic Distribution", "Lorentzian Distribution",  "Type 1 Extreme Distribution - Gumbel minimum order statistic", "Type 1 Extreme Distribution - Gumbel maximum order statistic", "Type 2 Extreme Distribution - Frechet", "Type 3 Extreme Distribution - Weibull", "Type 3 Extreme Distribution - Exponential Distribution", "Type 3 Extreme Distribution - Rayleigh Distribution", "Pareto Distribution", "Beta Distribution", "Gamma Distribution", "Erlang Distribution", "exit"};
        String[] boxTitles = {" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "exit"};
        String headerComment = "Choose next distribution to be fitted by clicking on box number";
        int defaultBox = 1;
        boolean testDistType = true;
        Regression reg = null;
        double[] coeff = null;
        while(testDistType){
                int opt =  Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                switch(opt){
                    case 1: // Gaussian
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.gaussianPlot();
                            coeff = reg.getCoeff();
                            fout.println("NORMAL (GAUSSIAN) DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Mean [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Standard deviation [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 2: // Two parameter Log-normal
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.logNormalTwoParPlot();
                            coeff = reg.getCoeff();
                            fout.println("LOG-NORMAL DISTRIBUTION (two parameter statistic)");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Shape parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 3: // Three parameter Log-normal
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.logNormalThreeParPlot();
                            coeff = reg.getCoeff();
                            fout.println("LOG-NORMAL DISTRIBUTION (three parameter statistic)");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [alpha] ");
                            fout.println(coeff[0]);
                            fout.printtab("Shape parameter [beta] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scale parameter [gamma] ");
                            fout.println(coeff[2]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[3]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 4: // Logistic
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.logisticPlot();
                            coeff = reg.getCoeff();
                            fout.println("LOGISTIC DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [beta] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 5: // Lorentzian
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.lorentzianPlot();
                            coeff = reg.getCoeff();
                            fout.println("LORENTZIAN DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Mean [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Half-height parameter [Gamma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 6: // Gumbel [minimum]
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.gumbelMinPlot();
                            coeff = reg.getCoeff();
                            fout.println("TYPE 1 (GUMBEL) EXTREME DISTRIBUTION [MINIMUM ORDER STATISTIC]");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 7: // Gumbel [maximum]
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.gumbelMaxPlot();
                            coeff = reg.getCoeff();
                            fout.println("TYPE 1 (GUMBEL) EXTREME DISTRIBUTION [MAXIMUM ORDER STATISTIC]");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 8: // Frechet
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.frechetPlot();
                            coeff = reg.getCoeff();
                            fout.println("TYPE 2 (FRECHET) EXTREME DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Shape parameter [gamma] ");
                            fout.println(coeff[2]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[3]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 9: // Weibull
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.weibullPlot();
                            coeff = reg.getCoeff();
                            fout.println("TYPE 3 (WEIBULL) EXTREME DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Shape parameter [gamma] ");
                            fout.println(coeff[2]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[3]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 10: // Exponential
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.exponentialPlot();
                            coeff = reg.getCoeff();
                            fout.println("EXPONENTIAL DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [sigma] ");
                            fout.println(coeff[1]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[2]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 11: // Rayleigh
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.rayleighPlot();
                            coeff = reg.getCoeff();
                            fout.println("RAYLEIGH DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Scale parameter [beta] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[1]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 12: // Pareto
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.paretoThreeParPlot();
                            coeff = reg.getCoeff();
                            fout.println("PARETO DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Shape parameter [alpha] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [beta] ");
                            fout.println(coeff[1]);
                            fout.printtab("Threshold parameter [theta] ");
                            fout.println(coeff[2]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[3]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 13: // Beta
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.betaMinMaxPlot();
                            coeff = reg.getCoeff();
                            fout.println("BETA DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Shape parameter [alpha] ");
                            fout.println(coeff[0]);
                            fout.printtab("Shape parameter [beta] ");
                            fout.println(coeff[1]);
                            fout.printtab("minimum limit [min] ");
                            fout.println(coeff[2]);
                            fout.printtab("maximum limit [max] ");
                            fout.println(coeff[3]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[4]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 14: // Gamma
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.gammaPlot();
                            coeff = reg.getCoeff();
                            fout.println("GAMMA DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Location parameter [mu] ");
                            fout.println(coeff[0]);
                            fout.printtab("Scale parameter [beta] ");
                            fout.println(coeff[1]);
                            fout.printtab("Shape parameter [gamma] ");
                            fout.println(coeff[2]);
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[3]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 15: // Erlang
                            reg = new Regression(array, binWidth);
                            reg.supressPrint();
                            reg.erlangPlot();
                            coeff = reg.getCoeff();
                            fout.println("ERLANG DISTRIBUTION");
                            fout.println("Best Estimates:");
                            fout.printtab("Shape parameter [lambda] ");
                            fout.println(coeff[0]);
                            fout.printtab("Rate parameter [k] ");
                            fout.println(reg.getKayValue());
                            fout.printtab("Scaling factor [Ao] ");
                            fout.println(coeff[1]);
                            Regression.regressionDetails(fout, reg);
                            break;
                    case 16: // exit
                    default: fout.close();
                             testDistType = false;
                }
            }
    }

    // Output method for fitOneOrSeveralDistributions
    protected static void regressionDetails(FileOutput fout, Regression reg){
        fout.println();
        fout.println("Regression details:");
        fout.printtab("Chi squared: ");
        fout.println(reg.getChiSquare());
        fout.printtab("Reduced chi squared: ");
        fout.println(reg.getReducedChiSquare());
        fout.printtab("Sum of squares: ");
        fout.println(reg.getSumOfSquares());
        fout.printtab("Degrees of freedom: ");
        fout.println(reg.getDegFree());
        fout.printtab("Number of iterations: ");
        fout.println(reg.getNiter());
        fout.printtab("maximum number of iterations allowed: ");
        fout.println(reg.getNmax());
        fout.println();
        fout.println();
    }

    // Get the x-y Correlation Coefficient
    public double getXYcorrCoeff(){
       return this.xyR;
    }

    // Get the y-y Correlation Coefficient
    public double getYYcorrCoeff(){
       return this.yyR;
    }

    // check data arrays for sign, maximum, minimum and peak
 	protected static ArrayList<Object> dataSign(double[] data){

        ArrayList<Object> ret = new ArrayList<Object>();
        int n = data.length;

	    double max=data[0];     // maximum
	    int maxi=0;             // index of above
	    double min=data[0];     // minimum
	    int mini=0;             // index of above
	    double peak=0.0D;       // peak: larger of maximum and any abs(negative minimum)
	    int peaki=-1;           // index of above
	    int signFlag=-1;        // 0 all positive; 1 all negative; 2 positive and negative
	    double shift=0.0D;      // shift to make all positive if a mixture of positive and negative
	    double mean = 0.0D;     // mean value
	    int signCheckZero=0;    // number of zero values
	    int signCheckNeg=0;     // number of positive values
	    int signCheckPos=0;     // number of negative values

	    for(int i=0; i<n; i++){
	        mean =+ data[i];
	        if(data[i]>max){
	            max=data[i];
	            maxi=i;
	        }
	        if(data[i]<min){
	            min=data[i];
	            mini=i;
	        }
	        if(data[i]==0.0D)signCheckZero++;
	        if(data[i]>0.0D)signCheckPos++;
	        if(data[i]<0.0D)signCheckNeg++;
	    }
	    mean /= (double)n;

	    if((signCheckZero+signCheckPos)==n){
	        peak=max;
	        peaki=maxi;
	        signFlag=0;
	    }
	    else{
	        if((signCheckZero+signCheckNeg)==n){
	            peak=min;
	            peaki=mini;
	            signFlag=1;
	        }
	        else{
	            peak=max;
	            peaki=maxi;
	            if(-min>max){
	                peak=min;
	                peak=mini;
	            }
	            signFlag=2;
	            shift=-min;
	        }
	    }

	    // transfer results to the ArrayList
	    ret.add(new Double(min));
	    ret.add(new Integer(mini));
	    ret.add(new Double(max));
	    ret.add(new Integer(maxi));
	    ret.add(new Double(peak));
	    ret.add(new Integer(peaki));
	    ret.add(new Integer(signFlag));
	    ret.add(new Double(shift));
	    ret.add(new Double(mean));
	    ret.add(new Integer(signCheckZero));
	    ret.add(new Integer(signCheckPos));
	    ret.add(new Integer(signCheckNeg));


	    return ret;
	}

    public void frechet(){
	    this.fitFrechet(0, 0);
	}

	public void frechetPlot(){
	    this.fitFrechet(1, 0);
	}

	public void frechetTwoPar(){
	    this.fitFrechet(0, 1);
	}

	public void frechetTwoParPlot(){
	    this.fitFrechet(1, 1);
	}

	public void frechetStandard(){
	    this.fitFrechet(0, 2);
	}

	public void frechetStandardPlot(){
	    this.fitFrechet(1, 2);
	}

    protected void fitFrechet(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=13;
	                this.nParam=4;
	                break;
	        case 1: this.lastMethod=14;
	                this.nParam=3;
	                break;
	        case 2: this.lastMethod=15;
	                this.nParam=2;
	                break;
        }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;
        this.frechetWeibull=true;
        this.fitFrechetWeibull(allTest, typeFlag);
    }

    // method for fitting data to either a Frechet or a Weibull distribution
    protected void fitFrechetWeibull(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();
	 	tempd = (Double)retY.get(8);
	 	double mean = tempd.doubleValue();

	 	// check for infinity
	    boolean testInf = true;
	    double dof = this.degreesOfFreedom;
	    while(testInf){
	 	    if(this.infinityCheck(yPeak, peaki)){
	 	        dof--;
	            if(dof<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("The effective degrees of freedom have been reduced to zero");
 	            retY = Regression.dataSign(yData);
	            tempd = (Double)retY.get(4);
	            yPeak = tempd.doubleValue();
	            tempi = (Integer)retY.get(5);
	 	        peaki = tempi.intValue();
	 	        tempd = (Double)retY.get(8);
	 	        mean = tempd.doubleValue();
	 	    }
	 	    else{
	 	        testInf = false;
	 	    }
	 	}

 	    // check sign of y data
 	    String ss = "Weibull";
	    if(this.frechetWeibull)ss = "Frechet";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    ArrayList<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.get(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.get(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mode')
		double distribMode = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMode, ss);

	    //Calculate loglog v log transforms
	    if(this.frechetWeibull){
	        for(int i=0; i<this.nData; i++){
	            cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumY[i] = cumYe[i].getValue();
	            cumW[i] = cumYe[i].getError();
	        }
	    }
	    else{
	        for(int i=0; i<this.nData; i++){
	            cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	            cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumYe[i] = ErrorProp.log(cumYe[i]);
	            cumY[i] = cumYe[i].getValue();
	            cumW[i] = cumYe[i].getError();
	        }
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	                xData[0][i] = cumX[i];
	                yData[i] = cumY[i];
	                weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for semi-linearised Frechet or Weibull
		// disable statistical analysis
		boolean statFlagHold = this.statFlag;
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        for(int i=0; i<this.nParam; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        double[] gammamin = null;
        double gammat = 0;
        switch(typeFlag){
    	    case 0:
                    start[0] = xMin - Math.abs(0.1D*xMin);          //mu
                    start[1] = sd;                                  //sigma
                    start[2] = 4.0;                                 // gamma
                    // step sizes
                    step[0] = 0.2D*start[0];
                    if(step[0]==0.0D){
                        ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.get(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.get(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.2D*start[1];
                    step[2] = 0.5D*start[2];
                    this.addConstraint(0,+1,xMin);
                    this.addConstraint(1,-1,0.0D);
                    this.addConstraint(2,-1,0.0D);
                    break;
    	    case 1: start[0] = sd;                //sigma
    	            start[1] = 4.0;               // gamma
                    // step sizes
                    step[0] = 0.2D*start[0];
                    step[1] = 0.5D*start[1];
                    this.addConstraint(0,-1,0.0D);
                    this.addConstraint(1,-1,0.0D);
                    break;
    	    case 2: start[0] = 4.0;               // gamma
                    // step size
                    step[0] = 0.5D*start[0];
                    this.addConstraint(0,-1,0.0D);
                    break;
        }

        // Create instance of loglog function and perform regression
        if(this.frechetWeibull){
            FrechetFunctionTwo f = new FrechetFunctionTwo();
            f.typeFlag = typeFlag;
            Object regFun2 = (Object)f;
        System.out.println("pppp " + start[0] + "   " + start[1] + "   " + start[2]);

            this.nelderMead(regFun2, start, step, this.fTol, this.nMax);
        }
        else{
            WeibullFunctionTwo f = new WeibullFunctionTwo();
            f.typeFlag = typeFlag;
            Object regFun2 = (Object)f;
            this.nelderMead(regFun2, start, step, this.fTol, this.nMax);
        }

	    // Get best estimates of loglog regression
	    double[] ests = Conv.copy(this.best);

	    // Nelder and Mead Simplex Regression for Frechet or Weibull
	    // using best estimates from loglog regression as initial estimates

		// re-enable statistical analysis if statFlag was set to true
		this.statFlag = statFlagHold;

	    // restore data reversing the loglog transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 0: start[0] = ests[0];         //mu
                    start[1] = ests[1];         //sigma
                    start[2] = ests[2];         //gamma
                    if(this.scaleFlag){
                        start[3] = 1.0/yScale;      //y axis scaling factor
                     }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.get(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.get(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    step[2] = 0.1D*start[2];
                    if(this.scaleFlag){
                        step[3] = 0.1D*start[3];
                    }
                   break;
            case 1: start[0] = ests[0];         //sigma
                    start[1] = ests[1];         //gamma
                    if(this.scaleFlag){
                        start[2] = 1.0/yScale;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 2: start[0] = ests[0];         //gamma
                    if(this.scaleFlag){
                        start[1] = 1.0/yScale;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
        }

        // Create instance of Frechet function and perform regression
        if(this.frechetWeibull){
            FrechetFunctionOne ff = new FrechetFunctionOne();
            ff.typeFlag = typeFlag;
            ff.scaleOption = this.scaleFlag;
            ff.scaleFactor = this.yScaleFactor;
            Object regFun3 = (Object)ff;
            this.nelderMead(regFun3, start, step, this.fTol, this.nMax);
            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();
                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }
        else{
            WeibullFunctionOne ff = new WeibullFunctionOne();
            ff.typeFlag = typeFlag;
            ff.scaleOption = this.scaleFlag;
            ff.scaleFactor = this.yScaleFactor;
            Object regFun3 = (Object)ff;
            this.nelderMead(regFun3, start, step, this.fTol, this.nMax);
            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();
                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }

        // restore data
        this.weightOpt = weightOptHold;
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}

	// Check for y value = infinity
	public boolean infinityCheck(double yPeak, int peaki){
	    boolean flag=false;
	 	if(yPeak == 1.0D/0.0D || yPeak == -1.0D/0.0D){
	 	    int ii = peaki+1;
	 	    if(peaki==this.nData-1)ii = peaki-1;
	 	    this.xData[0][peaki]=this.xData[0][ii];
	 	    this.yData[peaki]=this.yData[ii];
 	        this.weight[peaki]=this.weight[ii];
 	        System.out.println("An infinty has been removed at point "+peaki);
	 	    flag = true;
 	    }
 	    return flag;
    }

    // reverse sign of y values if negative
    public void reverseYsign(String ss){
	        System.out.println("This implementation of the " + ss + " distributions takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                this.yData[i] = -this.yData[i];
	        }
	}

    // check y values for all y are very small value
    public double checkYallSmall(double yPeak, String ss){
	    double magScale = 1.0D;
	    double recipYpeak = Fmath.truncate(1.0/yPeak, 4);
        if(yPeak<1e-4){
            System.out.println(ss + " fitting: The ordinate axis (y axis) has been rescaled by "+recipYpeak+" to reduce rounding errors");
            for(int i=0; i<this.nData; i++){
                this.yData[i]*=recipYpeak;
                if(this.weightOpt)this.weight[i]*=recipYpeak;
            }
            magScale=recipYpeak;
        }
        return magScale;
    }

    // Calculate cumulative values for distributions with a single independent variable
    // Entered parameters
    // peaki - index of the y value peak
    // yPeak - y value of the y peak
    // distribMode - x value at peak y (estimate of the 'distribution mode')
    // ss - name of the distribution to be fitted, e.g. "Frechet"
    // Returns:
    // return statement - an estimate of the scaling factor
    // cumX - x data as a one dimensional array with zero values replaced by average of adjacent values
    // cumY - cumulative y values
    // cumW - cumulative y weight values
    // cumYe - cumulative Y values as ErrorProp
    public double calculateCumulativeValues(double[] cumX, double[] cumY, double[] cumW, ErrorProp[] cumYe, int peaki, double yPeak, double distribMode, String ss){

        // Put independent values into a one-dimensional array
        cumX[0]= this.xData[0][0];
	    for(int i=1; i<this.nData; i++){
            cumX[i] = this.xData[0][i];
	    }

        // Create an array of ErrorProps from the independent values and their weights
	    ErrorProp[] yE = ErrorProp.oneDarray(this.nData);
	    for(int i=0; i<this.nData; i++){
            yE[i].reset(this.yData[i], this.weight[i]);
	    }

	    // check on shape of data for first step of cumulative calculation
	    if(peaki!=0){
	        if(peaki==this.nData-1){
	            System.out.println("The data does not cover a wide enough range of x values to fit to a " + ss + " distribution with any accuracy");
	            System.out.println("The regression will be attempted but you should treat any result with great caution");
	        }
	        if(this.yData[0]<this.yData[1]*0.5D && this.yData[0]>distribMode*0.02D){
	            ErrorProp x0 = new ErrorProp(0.0D, 0.0D);
	            x0 = yE[0].times(this.xData[0][1]-this.xData[0][0]);
	            x0 = x0.over(yE[1].minus(yE[0]));
	            x0 = ErrorProp.minus(this.xData[0][0],x0);
	            if(this.yData[0]>=0.9D*yPeak)x0=(x0.plus(this.xData[0][0])).over(2.0D);
		        if(x0.getValue()<0.0D)x0.reset(0.0D, 0.0D);
	            cumYe[0] = yE[0].over(2.0D);
	            cumYe[0] = cumYe[0].times(ErrorProp.minus(this.xData[0][0], x0));
	        }
	        else{
	            cumYe[0].reset(0.0D, this.weight[0]);
	        }
	    }
	    else{
	        cumYe[0].reset(0.0D, this.weight[0]);

	    }

	    // cumulative calculation for rest of the points (trapezium approximation)
	    for(int i=1; i<this.nData; i++){
	        cumYe[i] = yE[i].plus(yE[i-1]);
            cumYe[i] = cumYe[i].over(2.0D);
	        cumYe[i] = cumYe[i].times(this.xData[0][i]-this.xData[0][i-1]);
	        cumYe[i] = cumYe[i].plus(cumYe[i-1]);
		}

	    // check on shape of data for final step of cumulative calculation
	    ErrorProp cumYtotal = cumYe[this.nData-1].copy();
	    if(peaki==this.nData-1){
	        cumYtotal = cumYtotal.times(2.0D);
	    }
	    else{
	        if(this.yData[this.nData-1]<yData[this.nData-2]*0.5D && yData[this.nData-1]>distribMode*0.02D){
	            ErrorProp xn = new ErrorProp();
	            xn = yE[this.nData-1].times(this.xData[0][this.nData-2]-this.xData[0][this.nData-1]);
	            xn = xn.over(yE[this.nData-2].minus(yE[this.nData-1]));
	            xn = ErrorProp.minus(this.xData[0][this.nData-1], xn);
	            if(this.yData[0]>=0.9D*yPeak)xn=(xn.plus(this.xData[0][this.nData-1])).over(2.0D);
	            cumYtotal =  cumYtotal.plus(ErrorProp.times(0.5D,(yE[this.nData-1].times(xn.minus(this.xData[0][this.nData-1])))));
	        }
	    }

	    // Fill cumulative Y and W arrays
	    for(int i=0; i<this.nData; i++){
	        cumY[i]=cumYe[i].getValue();
	        cumW[i]=cumYe[i].getError();
	    }

	    // estimate y scaling factor
	    double yScale = 1.0D/cumYtotal.getValue();
	    for(int i=0; i<this.nData; i++){
	        cumYe[i]=cumYe[i].over(cumYtotal);
	    }

	    // check for zero and negative  values
	    int jj = 0;
	    boolean test = true;
	    for(int i=0; i<this.nData; i++){
	        if(cumYe[i].getValue()<=0.0D){
	            if(i<=jj){
	                test=true;
	                jj = i;
	                while(test){
	                    jj++;
	                    if(jj>=this.nData)throw new ArithmeticException("all zero cumulative data!!");
	                    if(cumYe[jj].getValue()>0.0D){
	                        cumYe[i]=cumYe[jj].copy();
	                        cumX[i]=cumX[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==this.nData-1){
	                    cumYe[i]=cumYe[i-1].copy();
	                    cumX[i]=cumX[i-1];
	                }
	                else{
	                    cumYe[i]=cumYe[i-1].plus(cumYe[i+1]);
	                    cumYe[i]=cumYe[i].over(2.0D);
	                    cumX[i]=(cumX[i-1]+cumX[i+1])/2.0D;
	                }
	            }
	        }
	    }

	    // check for unity value
		jj = this.nData-1;
	    for(int i=this.nData-1; i>=0; i--){
	        if(cumYe[i].getValue()>=1.0D){
	            if(i>=jj){
	                test=true;
	                jj = this.nData-1;
	                while(test){
	                    jj--;
	                    if(jj<0)throw new ArithmeticException("all unity cumulative data!!");
	                    if(cumYe[jj].getValue()<1.0D){
	                        cumYe[i]=cumYe[jj].copy();
	                        cumX[i]=cumX[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==0){
	                    cumYe[i]=cumYe[i+1].copy();
	                    cumX[i]=cumX[i+1];
	                }
	                else{
	                    cumYe[i]=cumYe[i-1].plus(cumYe[i+1]);
	                    cumYe[i]=cumYe[i].over(2.0D);
	                    cumX[i]=(cumX[i-1]+cumX[i+1])/2.0D;
	                }
	            }
	        }
	    }

	    return yScale;
	}

    public void weibull(){
	    this.fitWeibull(0, 0);
	}

	public void weibullPlot(){
	    this.fitWeibull(1, 0);
	}

	public void weibullTwoPar(){
	    this.fitWeibull(0, 1);
	}

	public void weibullTwoParPlot(){
	    this.fitWeibull(1, 1);
	}

	public void weibullStandard(){
	    this.fitWeibull(0, 2);
	}

	public void weibullStandardPlot(){
	    this.fitWeibull(1, 2);
	}

    protected void fitWeibull(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=16;
	                this.nParam=4;
	                break;
	        case 1: this.lastMethod=17;
	                this.nParam=3;
	                break;
	        case 2: this.lastMethod=18;
	                this.nParam=2;
	                break;
        }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;
        this.frechetWeibull=false;
        this.fitFrechetWeibull(allTest, typeFlag);
    }

	public void gumbelMin(){
	    this.fitGumbel(0, 0);
	}

	public void gumbelMinPlot(){
	    this.fitGumbel(1, 0);
	}

	public void gumbelMax(){
	    this.fitGumbel(0, 1);
	}
	public void gumbelMaxPlot(){
	    this.fitGumbel(1, 1);
	}

	public void gumbelMinOnePar(){
	    this.fitGumbel(0, 2);
	}

	public void gumbelMinOneParPlot(){
	    this.fitGumbel(1, 2);
	}

    public void gumbelMaxOnePar(){
	    this.fitGumbel(0, 3);
	}

	public void gumbelMaxOneParPlot(){
	    this.fitGumbel(1, 3);
	}

	public void gumbelMinStandard(){
	    this.fitGumbel(0, 4);
	}

	public void gumbelMinStandardPlot(){
	    this.fitGumbel(1, 4);
	}

	public void gumbelMaxStandard(){
	    this.fitGumbel(0, 5);
	}

	public void gumbelMaxStandardPlot(){
	    this.fitGumbel(1, 5);
	}

    // No parameters set for estimation
    // Correlation coefficient and plot
    protected void noParameters(String ss){
        System.out.println(ss+" Regression");
        System.out.println("No parameters set for estimation");
        System.out.println("Theoretical curve obtained");
	    String filename1="RegressOutput.txt";
	    String filename2="RegressOutputN.txt";
	    FileOutput fout = new FileOutput(filename1, 'n');
	    System.out.println("Results printed to the file "+filename2);
	    fout.dateAndTimeln(filename1);
        fout.println("No parameters set for estimation");
        switch(this.lastMethod){
            case 11:     fout.println("Minimal Standard Gumbel p(x) = exp(x)exp(-exp(x))");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(this.xData[0][i])*Math.exp(-Math.exp(this.xData[0][i]));
                        break;
            case 12:    fout.println("Maximal Standard Gumbel p(x) = exp(-x)exp(-exp(-x))");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(-this.xData[0][i])*Math.exp(-Math.exp(-this.xData[0][i]));
                        break;
            case 21:    fout.println("Standard Exponential p(x) = exp(-x)");
                        for(int i=0; i<this.nData; i++)this.yCalc[i]=Math.exp(-this.xData[0][i]);
                        break;
        }
        this.sumOfSquaresError = 0.0D;
        this.chiSquare = 0.0D;
        double temp = 0.0D;
         for(int i=0; i<this.nData; i++){
            temp = Fmath.square(this.yData[i]-this.yCalc[i]);
            this.sumOfSquaresError += temp;
            this.chiSquare += temp/Fmath.square(this.weight[i]);
        }
        double corrCoeff = Stat.corrCoeff(this.yData, this.yCalc);
        fout.printtab("Correlation Coefficient");
        fout.println(Fmath.truncate(corrCoeff, this.prec));
        if(Math.abs(corrCoeff)<=1.0D){
            fout.printtab("Correlation Coefficient Probability");
            fout.println(Fmath.truncate(1.0D-Stat.linearCorrCoeffProb(corrCoeff, this.degreesOfFreedom-1), this.prec));
        }

        fout.printtab("Sum of Squares");
        fout.println(Fmath.truncate(this.sumOfSquaresError, this.prec));
        if(this.weightOpt || this.trueFreq){
            fout.printtab("Chi Square");
            fout.println(Fmath.truncate(this.chiSquare, this.prec));
            fout.printtab("chi square probability");
            fout.println(Fmath.truncate(Stat.chiSquareProb(this.chiSquare, this.degreesOfFreedom-1), this.prec));
        }
        fout.println(" ");

        fout.printtab("x", this.field);
        fout.printtab("p(x) [expl]", this.field);
        fout.printtab("p(x) [calc]", this.field);
        fout.println("residual");

        for(int i=0; i<this.nData; i++){
            fout.printtab(Fmath.truncate(this.xData[0][i], this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yData[i], this.prec), this.field);
            fout.printtab(Fmath.truncate(this.yCalc[i], this.prec), this.field);
            fout.println(Fmath.truncate(this.yData[i]-this.yCalc[i], this.prec));
       }
       fout.close();
       this.plotXY();
       if(!this.supressYYplot)this.plotYY();
    }

	protected void fitGumbel(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=7;
	                this.nParam=3;
	                break;
	        case 1: this.lastMethod=8;
	                this.nParam=3;
	                break;
	        case 2: this.lastMethod=9;
	                this.nParam=2;
	                break;
	        case 3: this.lastMethod=10;
	                this.nParam=2;
	                break;
	        case 4: this.lastMethod=11;
	                this.nParam=1;
	                break;
            case 5: this.lastMethod=12;
	                this.nParam=1;
	                break;
	    }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;
	    this.zeroCheck = false;
		this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
	    if(this.nParam==0){
	        this.noParameters("Gumbel");
	    }
	    else{


	        // order data into ascending order of the abscissae
            Regression.sort(this.xData[0], this.yData, this.weight);

	        // check sign of y data
	        Double tempd=null;
	        ArrayList<Object> retY = Regression.dataSign(yData);
	        tempd = (Double)retY.get(4);
	        double yPeak = tempd.doubleValue();
	        boolean yFlag = false;

	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGumbel(): This implementation of the Gumbel distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // check  x data
	    ArrayList<Object> retX = Regression.dataSign(xData[0]);
	 	Integer tempi = null;

        // Calculate  x value at peak y (estimate of the 'distribution mode')
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();
	    double distribMode = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = halfWidth(xData[0], yData);

	    // Nelder and Mead Simplex Regression for Gumbel
        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        switch(typeFlag){
            case 0:
            case 1:
                    start[0] = distribMode;                     //mu
                    start[1] = sd*Math.sqrt(6.0D)/Math.PI;      //sigma
                    if(this.scaleFlag){
                        start[2] = yPeak*start[1]*Math.exp(1);      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.get(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.get(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];

	                // Add constraints
                    this.addConstraint(1,-1,0.0D);
                    break;
            case 2:
            case 3:
                    start[0] = sd*Math.sqrt(6.0D)/Math.PI;      //sigma
                    if(this.scaleFlag){
                        start[1] = yPeak*start[0]*Math.exp(1);      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
	                // Add constraints
                    this.addConstraint(0,-1,0.0D);
                    break;
            case 4:
            case 5:
                    if(this.scaleFlag){
                        start[0] = yPeak*Math.exp(1);               //y axis scaling factor
                        step[0] = 0.1D*start[0];
                    }
                    break;
        }

        // Create instance of Gumbel function
        GumbelFunction ff = new GumbelFunction();

        // Set minimum type / maximum type option
        ff.typeFlag = typeFlag;

        // Set ordinate scaling option
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;

        if(typeFlag<4){

            // Perform simplex regression
            Object regFun3 = (Object)ff;
            this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

            if(allTest==1){
                // Print results
                if(!this.supressPrint)this.print();

                // Plot results
                int flag = this.plotXY(ff);
                if(flag!=-2 && !this.supressYYplot)this.plotYY();
            }
        }
        else{
            // calculate exp exp term
            double[][] xxx = new double[1][this.nData];
            double aa=1.0D;
            if(typeFlag==5)aa=-1.0D;
            for(int i=0; i<this.nData; i++){
                xxx[0][i]=Math.exp(aa*this.xData[0][i])*Math.exp(-Math.exp(aa*this.xData[0][i]));
            }

            // perform linear regression
            this.linNonLin = true;
            this.generalLinear(xxx);

            if(!this.supressPrint)this.print();
            if(!this.supressYYplot)this.plotYY();
            this.plotXY();

            this.linNonLin = false;

        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
        }
	}

	// sort elements x, y and w arrays of doubles into ascending order of the x array
    // using selection sort method
    protected static void sort(double[] x, double[] y, double[] w){
            int index = 0;
            int lastIndex = -1;
            int n = x.length;
            double holdx = 0.0D;
            double holdy = 0.0D;
            double holdw = 0.0D;

            while(lastIndex < n-1){
                index = lastIndex+1;
                for(int i=lastIndex+2; i<n; i++){
                    if(x[i]<x[index]){
                        index=i;
                    }
                }
                lastIndex++;
                holdx=x[index];
                x[index]=x[lastIndex];
                x[lastIndex]=holdx;
                holdy=y[index];
                y[index]=y[lastIndex];
                y[lastIndex]=holdy;
                holdw=w[index];
                w[index]=w[lastIndex];
                w[lastIndex]=holdw;
            }
    }

    // returns rough estimate of half-height width
    protected static double halfWidth(double[] xData, double[] yData){
            // Find index of maximum value and calculate half maximum height
            double ymax = yData[0];
            int imax = 0;
            int n = xData.length;

            for(int i=1; i<n; i++){
                if(yData[i]>ymax){
                    ymax=yData[i];
                    imax=i;
                }
            }
            ymax /= 2.0D;

            // Find index of point at half maximum value on the low side of the maximum
            double halfXlow=-1.0D;
            double halfYlow=-1.0D;
            double temp = -1.0D;
            int ihl=-1;
            if(imax>0){
                ihl=imax-1;
                halfYlow=Math.abs(ymax-yData[ihl]);
                for(int i=imax-2; i>=0; i--){
                    temp=Math.abs(ymax-yData[i]);
                    if(temp<halfYlow){
                        halfYlow=temp;
                        ihl=i;
                    }
                }
                halfXlow=Math.abs(xData[ihl]-xData[imax]);
            }

            // Find index of point at half maximum value on the high side of the maximum
            double halfXhigh=-1.0D;
            double halfYhigh=-1.0D;
            temp = -1.0D;
            int ihh=-1;
            if(imax<n-1){
                ihh=imax+1;
                halfYhigh=Math.abs(ymax-yData[ihh]);
                for(int i=imax+2; i<n; i++){
                    temp=Math.abs(ymax-yData[i]);
                    if(temp<halfYhigh){
                        halfYhigh=temp;
                        ihh=i;
                    }
                }
                halfXhigh=Math.abs(xData[ihh]-xData[imax]);
            }

            // Calculate width at half height
            double halfw = 0.0D;
            if(ihl!=-1)halfw += halfXlow;
            if(ihh!=-1)halfw += halfXhigh;

            return halfw;
    }

    //  FIT TO A SIMPLE EXPOPNENTIAL

    // method for fitting data to a simple exponential
    public void exponentialSimple(){
        fitsexponentialSimple(0);
    }

    // method for fitting data to a simple exponential
    public void exponentialSimplePlot(){
        fitsexponentialSimple(1);
    }

    // method for fitting data to a simple exponential
    protected void fitsexponentialSimple(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=43;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    if(!this.scaleFlag)this.nParam=1;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of yscale and A - linear transform
	    int nLen = this.yData.length;
	    int nLin = nLen;
	    boolean[] zeros = new boolean[nLen];
	    for(int i=0; i<nLen; i++){
	        zeros[i] = true;
	        if(this.xData[0][i]<=0.0D||this.yData[i]<=0.0D){
	            zeros[i] = false;
	            nLin--;
	        }
	    }
	    double[] xlin = new double[nLin];
	    double[] ylin = new double[nLin];
	    double[] wlin = new double[nLin];
	    int counter = 0;
	    for(int i=0; i<nLen; i++){
	        if(zeros[i]){
	            xlin[counter] = Math.log(this.xData[0][i]);
	            ylin[counter] = Math.log(this.yData[i]);
	            wlin[counter] = Math.abs(this.weight[i]/this.yData[i]);
	            counter++;
	        }
	    }

	    Regression reglin = new Regression(xlin, ylin, wlin);
        double[] start = new double[nParam];
	    double[] step = new double[nParam];
	    if(this.scaleFlag){
	        reglin.linear();
	        double[] coeff = reglin.getBestEstimates();
	        double[] errrs = reglin.getBestEstimatesErrors();

            // initial estimates
            start[0] = coeff[1];
            start[1] = Math.exp(coeff[0]);

            // initial step sizes
            step[0] = errrs[1]/2.0;
            step[1] = errrs[0]*start[0]/2.0;
            if(step[0]<=0.0 || Fmath.isNaN(step[0]))step[0] = Math.abs(start[0]*0.1);
            if(step[1]<=0.0 || Fmath.isNaN(step[1]))step[1] = Math.abs(start[1]*0.1);
        }
        else{
	        reglin.linearGeneral();
	        double[] coeff = reglin.getBestEstimates();
	        double[] errrs = reglin.getBestEstimatesErrors();

            // initial estimates
            start[0] = coeff[1];

            // initial step sizes
            step[0] = errrs[1]/2.0;
            if(step[0]<=0.0 || Fmath.isNaN(step[0]))step[0] = Math.abs(start[0]*0.1);
        }

        // Nelder and Mead Simplex Regression
        ExponentialSimpleFunction f = new ExponentialSimpleFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }


    //  FIT TO MULTIPLE EXPOPNENTIALS

    // method for fitting data to mutiple exponentials
    // initial estimates calculated internally
    public void exponentialMultiple(int nExps){
	    this.userSupplied = false;
        fitsexponentialMultiple(nExps,0);
    }

    // method for fitting data to a multiple exponentials
    // initial estimates calculated internally
    public void exponentialMultiplePlot(int nExps){
	    this.userSupplied = false;
        fitsexponentialMultiple(nExps, 1);
    }

    // method for fitting data to mutiple exponentials
    // user supplied initial estimates
    public void exponentialMultiple(int nExps, double[] AandBs){
	    this.userSupplied = true;
        fitsexponentialMultiple(nExps, 0, AandBs);
    }

    // method for fitting data to a multiple exponentials
    // user supplied initial estimates
    public void exponentialMultiplePlot(int nExps, double[] AandBs){
	    this.userSupplied = true;
        fitsexponentialMultiple(nExps, 1, AandBs);
    }

    // method for fitting data to a multiple exponentials
    // initial estimates calculated internally
    protected void fitsexponentialMultiple(int nExps, int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=44;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2*nExps;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of yscale and A - linear transform
	    int nLen = this.yData.length;
	    int nLin = nLen;
	    boolean[] zeros = new boolean[nLen];
	    for(int i=0; i<nLen; i++){
	        zeros[i] = true;
	        if(this.xData[0][i]<=0.0D||this.yData[i]<=0.0D){
	            zeros[i] = false;
	            nLin--;
	        }
	    }
	    double[] xlin = new double[nLin];
	    double[] ylin = new double[nLin];
	    double[] wlin = new double[nLin];
	    int counter = 0;
	    for(int i=0; i<nLen; i++){
	        if(zeros[i]){
	            xlin[counter] = Math.log(this.xData[0][i]);
	            ylin[counter] = Math.log(this.yData[i]);
	            wlin[counter] = Math.abs(this.weight[i]/this.yData[i]);
	            counter++;
	        }
	    }

	    Regression reglin = new Regression(xlin, ylin, wlin);
        double[] start = new double[nParam];
	    double[] step = new double[nParam];

	    reglin.linear();
	    double[] coeff = reglin.getBestEstimates();
	    double[] errrs = reglin.getBestEstimatesErrors();

        for(int i=0; i<this.nParam; i+=2){
            // initial estimates
            start[i] = Math.exp(coeff[0])/this.nParam;
            start[i+1] = coeff[1];

            // initial step sizes
            step[i] = errrs[0]*start[i]/2.0;
            step[i+1] = errrs[1]/2.0;
            if(step[i]<=0.0 || Fmath.isNaN(step[i]))step[i] = Math.abs(start[i]*0.1);
            if(step[i+1]<=0.0 || Fmath.isNaN(step[i+1]))step[i+1] = Math.abs(start[i+1]*0.1);
        }

        // Nelder and Mead Simplex Regression
        ExponentialMultipleFunction f = new ExponentialMultipleFunction();
        f.nExps = this.nParam;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // method for fitting data to a multiple exponentials
    // user supplied initial estimates calculated
    protected void fitsexponentialMultiple(int nExps, int plotFlag, double[] aAndBs){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=44;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2*nExps;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        double[] start = new double[nParam];
	    double[] step = new double[nParam];

        for(int i=0; i<this.nParam; i+=2){
            // initial estimates
            start[i] = aAndBs[i];

            // initial step sizes
            step[i] = Math.abs(start[i]*0.1);
        }

        // Nelder and Mead Simplex Regression
        ExponentialMultipleFunction f = new ExponentialMultipleFunction();
        f.nExps = this.nParam;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    //  FIT TO ONE MINUS A SIMPLE EXPOPNENTIAL

    // method for fitting data to 1 - exponential
    public void oneMinusExponential(){
        fitsoneMinusExponential(0);
    }

    // method for fitting data to 1 - exponential
    public void oneMinusExponentialPlot(){
        fitsoneMinusExponential(1);
    }

    // method for fitting data to 1 - exponential
    protected void fitsoneMinusExponential(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=45;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // initial step sizes
	    ArrayMaths am = new ArrayMaths(this.yData);
	    double maxY = am.maximum();
	    double minY = am.minimum();
	    double testDirection = 1.0;
	    double maxYhalf = maxY/2.0;

	    if(Math.abs(minY)>Math.abs(maxY)){
	        testDirection = -1.0;
	        maxY = minY;
	        maxYhalf = minY/2.0;
	    }
	    double timeHalf = Double.NaN;
	    boolean test = true;
	    int ii=0;
	    while(test){
	        if(this.yData[ii]==maxYhalf){
	            timeHalf = this.xData[0][ii] - this.xData[0][0];
	            test = false;
	        }
	        else{
	            if(this.yData[ii]<maxYhalf && this.yData[ii+1]>maxYhalf){
	                timeHalf = (this.xData[0][ii] + this.xData[0][ii+1])/2.0 - this.xData[0][0];
    	            test = false;
    	        }
    	        else{
    	           if(this.yData[ii]>maxYhalf && this.yData[ii+1]<maxYhalf){
	                    timeHalf = (this.xData[0][ii] + this.xData[0][ii+1])/2.0 - this.xData[0][0];
    	                test = false;
    	            }
    	            else{
    	                ii++;
    	                if(ii>=this.nData-1)test = false;
    	            }
    	        }
    	    }
    	}

	    if(timeHalf!=timeHalf){
	        timeHalf = am.maximumDifference();
	    }

	    double guessB = -testDirection/timeHalf;
	    double[] start = {maxY, guessB};
	    double[] step = {Math.abs(start[0]/5.0), Math.abs(start[1]/5.0)};

        // Nelder and Mead Simplex Regression
        OneMinusExponentialFunction f = new OneMinusExponentialFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);
        double ss0 = this.sumOfSquaresError;
        double[] bestEstimates0 = this.best;

        // Repeat with A and B guess of opposite sign
        start[0] = -maxY;
        start[1] = -guessB;
        step[0] = Math.abs(start[0]/5.0);
        step[1] = Math.abs(start[1]/5.0);
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        // Choose better result
        if(this.sumOfSquaresError>ss0){
            start[0] = bestEstimates0[0];
            start[1] = bestEstimates0[1];
            step[0] = Math.abs(start[0]/20.0);
            step[1] = Math.abs(start[1]/20.0);
            this.nelderMead(regFun2, start, step, this.fTol, this.nMax);
        }

        // Plotting
        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    //  FIT TO AN EXPOPNENTIAL DISTRIBUTION

    public void exponential(){
	    this.fitExponential(0, 0);
	}

	public void exponentialPlot(){
	    this.fitExponential(1, 0);
	}

	public void exponentialOnePar(){
	    this.fitExponential(0, 1);
	}

	public void exponentialOneParPlot(){
	    this.fitExponential(1, 1);
	}

	public void exponentialStandard(){
	    this.fitExponential(0, 2);
	}

	public void exponentialStandardPlot(){
	    this.fitExponential(1, 2);
	}

    protected void fitExponential(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=19;
	                this.nParam=3;
	                break;
	        case 1: this.lastMethod=20;
	                this.nParam=2;
	                break;
	        case 2: this.lastMethod=21;
	                this.nParam=1;
	                break;
        }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;
   	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
	    if(this.nParam==0){
	        this.noParameters("Exponential");
	    }
	    else{

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();

 	    // check sign of y data
 	    String ss = "Exponential";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    ArrayList<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.get(0);
	    double xMin = tempd.doubleValue();

        // estimate of sigma
        double yE = yPeak/Math.exp(1.0D);
        if(this.yData[0]<yPeak)yE = (yPeak+yData[0])/(2.0D*Math.exp(1.0D));
        double yDiff = Math.abs(yData[0]-yE);
        double yTest = 0.0D;
        int iE = 0;
        for(int i=1; i<this.nData; i++){
            yTest=Math.abs(this.yData[i]-yE);
            if(yTest<yDiff){
                yDiff=yTest;
                iE=i;
            }
        }
        double sigma = this.xData[0][iE]-this.xData[0][0];

	    // Nelder and Mead Simplex Regression
	    double[] start = new double[this.nParam];
	    double[] step = new double[this.nParam];

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 0: start[0] = xMin*0.9;    //mu
                    start[1] = sigma;       //sigma
                    if(this.scaleFlag){
                        start[2] = yPeak*sigma; //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(step[0]==0.0D){
                        ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	                Double tempdd = null;
	                    tempdd = (Double)ret0.get(2);
	 	                double xmax = tempdd.doubleValue();
	 	                if(xmax==0.0D){
	 	                    tempdd = (Double)ret0.get(0);
	 	                    xmax = tempdd.doubleValue();
	 	                }
	                    step[0]=xmax*0.1D;
	                }
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 1: start[0] = sigma;       //sigma
                    if(this.scaleFlag){
                        start[1] = yPeak*sigma; //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
            case 2: if(this.scaleFlag){
                        start[0] = yPeak;       //y axis scaling factor
                        step[0] = 0.1D*start[0];
                    }
                    break;
        }

        // Create instance of Exponential function and perform regression
        ExponentialFunction ff = new ExponentialFunction();
        ff.typeFlag = typeFlag;
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	    }
	}

    // check for zero and negative  values
    public void checkZeroNeg(double [] xx, double[] yy, double[] ww){
	    int jj = 0;
	    boolean test = true;
	    for(int i=0; i<this.nData; i++){
	        if(yy[i]<=0.0D){
	            if(i<=jj){
	                test=true;
	                jj = i;
	                while(test){
	                    jj++;
	                    if(jj>=this.nData)throw new ArithmeticException("all zero cumulative data!!");
	                    if(yy[jj]>0.0D){
	                        yy[i]=yy[jj];
	                        xx[i]=xx[jj];
	                        ww[i]=ww[jj];
	                        test=false;
	                    }
	                }
	            }
	            else{
	                if(i==this.nData-1){
	                    yy[i]=yy[i-1];
	                    xx[i]=xx[i-1];
	                    ww[i]=ww[i-1];
	                }
	                else{
	                    yy[i]=(yy[i-1] + yy[i+1])/2.0D;
	                    xx[i]=(xx[i-1] + xx[i+1])/2.0D;
	                    ww[i]=(ww[i-1] + ww[i+1])/2.0D;
	                }
	            }
	        }
	    }
	}

	public void rayleigh(){
	    this.fitRayleigh(0, 0);
	}

	public void rayleighPlot(){
	    this.fitRayleigh(1, 0);
	}

    protected void fitRayleigh(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
    	this.lastMethod=22;
    	this.userSupplied = false;
	    this.nParam=2;
	    if(!this.scaleFlag)this.nParam=this.nParam-1;
   	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");


        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();

 	    // check sign of y data
 	    String ss = "Rayleigh";
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // minimum value of x
	    ArrayList<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.get(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.get(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mode')
		double distribMode = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMode, ss);

	    //Calculate log  transform
	    for(int i=0; i<this.nData; i++){
	        cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	        cumYe[i] = ErrorProp.over(1.0D, cumYe[i]);
	        cumYe[i] = ErrorProp.log(cumYe[i]);
	        cumY[i] = cumYe[i].getValue();
	        cumW[i] = cumYe[i].getError();
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	        xData[0][i] = cumX[i];
	        yData[i] = cumY[i];
	        weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for semi-linearised Rayleigh
		// disable statistical analysis
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        for(int i=0; i<this.nParam; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        start[0] = sd;                //sigma
        step[0] = 0.2D;
        this.addConstraint(0,-1,0.0D);

        // Create instance of log function and perform regression
        RayleighFunctionTwo f = new RayleighFunctionTwo();
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

	    // Get best estimates of log regression
	    double[] ests = Conv.copy(this.best);

		// enable statistical analysis
		this.statFlag=true;

	    // restore data reversing the loglog transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        start[0] = ests[0];         //sigma
        if(this.scaleFlag){
            start[1] = 1.0/yScale;      //y axis scaling factor
        }
        step[0] = 0.1D*start[0];
        if(this.scaleFlag)step[1] = 0.1D*start[1];


        // Create instance of Rayleigh function and perform regression
        RayleighFunctionOne ff = new RayleighFunctionOne();
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}

	// Shifted Pareto
	public void paretoShifted(){
	    this.fitPareto(0, 3);
	}

    public void paretoThreePar(){
	    this.fitPareto(0, 3);
	}

	public void paretoShiftedPlot(){
	    this.fitPareto(1, 3);
	}
	public void paretoThreeParPlot(){
	    this.fitPareto(1, 3);
	}

    // Two Parameter Pareto
	public void paretoTwoPar(){
	    this.fitPareto(0, 2);
	}
	// Deprecated
	public void pareto(){
	    this.fitPareto(0, 2);
	}

	public void paretoTwoParPlot(){
	    this.fitPareto(1, 2);
	}
	// Deprecated
    public void paretoPlot(){
	    this.fitPareto(1, 2);
	}

    // One Parameter Pareto
	public void paretoOnePar(){
	    this.fitPareto(0, 1);
	}

	public void paretoOneParPlot(){
	    this.fitPareto(1, 1);
	}

    // method for fitting data to a Pareto distribution
    protected void fitPareto(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
	        case 3: this.lastMethod=29;
	                this.nParam=4;
	                break;
    	    case 2: this.lastMethod=23;
	                this.nParam=3;
	                break;
	        case 1: this.lastMethod=24;
	                this.nParam=2;
	                break;
	    }

	    if(!this.scaleFlag)this.nParam=this.nParam-1;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");
 	    String ss = "Pareto";

        // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // check y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    Integer tempi = null;
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();

	 	// check for infinity
	 	if(this.infinityCheck(yPeak, peaki)){
 	        retY = Regression.dataSign(yData);
	        tempd = (Double)retY.get(4);
	        yPeak = tempd.doubleValue();
	        tempi = null;
	        tempi = (Integer)retY.get(5);
	 	    peaki = tempi.intValue();
	 	}

 	    // check sign of y data
 	    boolean ySignFlag = false;
	    if(yPeak<0.0D){
	        this.reverseYsign(ss);
	        retY = Regression.dataSign(this.yData);
	        yPeak = -yPeak;
	        ySignFlag = true;
	    }

        // check y values for all very small values
        boolean magCheck=false;
        double magScale = this.checkYallSmall(yPeak, ss);
        if(magScale!=1.0D){
            magCheck=true;
            yPeak=1.0D;
        }

	    // minimum value of x
	    ArrayList<Object> retX = Regression.dataSign(this.xData[0]);
        tempd = (Double)retX.get(0);
	    double xMin = tempd.doubleValue();

	    // maximum value of x
        tempd = (Double)retX.get(2);
	    double xMax = tempd.doubleValue();

        // Calculate  x value at peak y (estimate of the 'distribution mode')
		double distribMode = xData[0][peaki];

	    // Calculate an estimate of the half-height width
	    double sd = Math.log(2.0D)*halfWidth(xData[0], yData);

	    // Save x-y-w data
	    double[] xx = new double[this.nData];
	    double[] yy = new double[this.nData];
	    double[] ww = new double[this.nData];

	    for(int i=0; i<this.nData; i++){
	        xx[i]=this.xData[0][i];
	        yy[i]=this.yData[i];
	        ww[i]=this.weight[i];
	    }

	    // Calculate the cumulative probability and return ordinate scaling factor estimate
	    double[] cumX = new double[this.nData];
	    double[] cumY = new double[this.nData];
	    double[] cumW = new double[this.nData];
	    ErrorProp[] cumYe = ErrorProp.oneDarray(this.nData);
        double yScale = this.calculateCumulativeValues(cumX, cumY, cumW, cumYe, peaki, yPeak, distribMode, ss);

	    //Calculate l - cumlative probability
	    for(int i=0; i<this.nData; i++){
	        cumYe[i] = ErrorProp.minus(1.0D,cumYe[i]);
	        cumY[i] = cumYe[i].getValue();
	        cumW[i] = cumYe[i].getError();
        }

        // Fill data arrays with transformed data
        for(int i =0; i<this.nData; i++){
	                xData[0][i] = cumX[i];
	                yData[i] = cumY[i];
	                weight[i] = cumW[i];
	    }
	    boolean weightOptHold = this.weightOpt;
	    this.weightOpt=true;

		// Nelder and Mead Simplex Regression for Pareto estimated cdf
		// disable statistical analysis
		this.statFlag=false;

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        for(int i=0; i<this.nParam; i++){
            start[i]=1.0D;
            step[i]=0.2D;
        }
        switch(typeFlag){
            case 3: start[0] = 2;           //alpha
                    start[1] = xMin*0.9D;   //beta
                    if(xMin<0){             //theta
                        start[2] = -xMin*1.1D;
                    }
                    else{
                        start[2] = xMin*0.01;
                    }
                    if(start[1]<0.0D)start[1]=0.0D;
                    step[0] = 0.2D*start[0];
                    step[1] = 0.2D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
	                this.addConstraint(0,-1,0.0D);
	                this.addConstraint(1,-1,0.0D);
                    this.addConstraint(1,+1,xMin);
                    break;
    	    case 2: if(xMin<0)System.out.println("Method: FitParetoTwoPar/FitParetoTwoParPlot\nNegative data values present\nFitParetoShifted/FitParetoShiftedPlot would have been more appropriate");
    	            start[0] = 2;           //alpha
                    start[1] = xMin*0.9D;   //beta
                    if(start[1]<0.0D)start[1]=0.0D;
                    step[0] = 0.2D*start[0];
                    step[1] = 0.2D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
	                this.addConstraint(0,-1,0.0D);
	                this.addConstraint(1,-1,0.0D);
                    break;
    	    case 1: if(xMin<0)System.out.println("Method: FitParetoOnePar/FitParetoOneParPlot\nNegative data values present\nFitParetoShifted/FitParetoShiftedPlot would have been more appropriate");
    	            start[0] = 2;                //alpha
                    step[0] = 0.2D*start[0];
                    this.addConstraint(0,-1,0.0D);
                    this.addConstraint(1,-1,0.0D);
                    break;
        }

        // Create instance of cdf function and perform regression
        ParetoFunctionTwo f = new ParetoFunctionTwo();
        f.typeFlag = typeFlag;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

	    // Get best estimates of cdf regression
	    double[] ests = Conv.copy(this.best);

	    // Nelder and Mead Simplex Regression for Pareto
	    // using best estimates from cdf regression as initial estimates

		// enable statistical analysis
		this.statFlag=true;

	    // restore data reversing the cdf transform but maintaining any sign reversals
	    this.weightOpt=weightOptHold;
	    for(int i =0; i<this.nData; i++){
	        xData[0][i] = xx[i];
	        yData[i] = yy[i];
	        weight[i] = ww[i];
	    }

        // Fill arrays needed by the Simplex
        switch(typeFlag){
            case 3: start[0] = ests[0];                         //alpha
                    if(start[0]<=0.0D){
                        if(start[0]==0.0D){
                            start[0]=1.0D;
                        }
                        else{
                            start[0] = Math.min(1.0D,-start[0]);
                        }
                    }
                    start[1] = ests[1];                         //beta
                    if(start[1]<=0.0D){
                        if(start[1]==0.0D){
                            start[1]=1.0D;
                        }
                        else{
                            start[1] = Math.min(1.0D,-start[1]);
                        }
                    }
                    start[2] = ests[2];
                    if(this.scaleFlag){
                        start[3] = 1.0/yScale;    //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 2: start[0] = ests[0];                         //alpha
                    if(start[0]<=0.0D){
                        if(start[0]==0.0D){
                            start[0]=1.0D;
                        }
                        else{
                            start[0] = Math.min(1.0D,-start[0]);
                        }
                    }
                    start[1] = ests[1];                         //beta
                    if(start[1]<=0.0D){
                        if(start[1]==0.0D){
                            start[1]=1.0D;
                        }
                        else{
                            start[1] = Math.min(1.0D,-start[1]);
                        }
                    }
                    if(this.scaleFlag){
                        start[2] = 1.0/yScale;    //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(step[1]==0.0D){
                        double xmax = xMax;
	 	                if(xmax==0.0D){
	 	                    xmax = xMin;
	 	                }
	                    step[1]=xmax*0.1D;
	                }
                    if(this.scaleFlag)step[2] = 0.1D*start[2];
                    break;
            case 1: start[0] = ests[0];                         //alpha
                    if(start[0]<=0.0D){
                        if(start[0]==0.0D){
                            start[0]=1.0D;
                        }
                        else{
                            start[0] = Math.min(1.0D,-start[0]);
                        }
                    }
                    if(this.scaleFlag){
                        start[1] = 1.0/yScale;    //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    break;
         }

        // Create instance of Pareto function and perform regression
        ParetoFunctionOne ff = new ParetoFunctionOne();
        ff.typeFlag = typeFlag;
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();
            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        // restore data
        this.weightOpt = weightOptHold;
	    if(magCheck){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i] = yy[i]/magScale;
	            if(this.weightOpt)this.weight[i] = ww[i]/magScale;
	        }
	    }
	    if(ySignFlag){
	        for(int i =0; i<this.nData; i++){
	            this.yData[i]=-this.yData[i];
	        }
	    }
	}


	// method for fitting data to a sigmoid threshold function
    public void sigmoidThreshold(){
        fitSigmoidThreshold(0);
    }

    // method for fitting data to a sigmoid threshold function with plot and print out
    public void sigmoidThresholdPlot(){
        fitSigmoidThreshold(1);
    }


    // method for fitting data to a sigmoid threshold function
    protected void fitSigmoidThreshold(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=25;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
        double theta0 = xData[0][thetaPos];

	    // estimate of slope
	    double thetaSlope1 = 2.0D*(yData[nLen-1] - theta0)/(xData[0][nLen-1] - xData[0][thetaPos]);
	    double thetaSlope2 = 2.0D*theta0/(xData[0][thetaPos] - xData[0][nLen-1]);
	    double thetaSlope = Math.max(thetaSlope1, thetaSlope2);

        // initial estimates
        double[] start = new double[nParam];
        start[0] = 4.0D*thetaSlope;
        if(dirFlag==1){
            start[0] /= yymax;
        }
        else{
            start[0] /= yymin;
        }
        start[1] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[2] = yymax;
            }
            else{
                start[2] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);
        if(step[1]==0.0D)step[1] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[2]==0.0D)step[2] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        SigmoidThresholdFunction f = new SigmoidThresholdFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }
    // method for fitting data to a Hill/Sips Sigmoid
    public void sigmoidHillSips(){
        fitsigmoidHillSips(0);
    }

    // method for fitting data to a Hill/Sips Sigmoid with plot and print out
    public void sigmoidHillSipsPlot(){
        fitsigmoidHillSips(1);
    }

    // method for fitting data to a Hill/Sips Sigmoid
    protected void fitsigmoidHillSips(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=28;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
        int nLen = this.yData.length;
        this.midPoint();
       	double theta0 = this.midPointYvalue;

        // initial estimates
        double[] start = new double[nParam];
        start[0] = theta0;
        if(this.directionFlag==1){
            start[1] = 1;
        }
        else{
            start[1] = -1;
        }
        if(this.scaleFlag){
            start[2] = this.top - this.bottom;
        }

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<this.nParam; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (this.xData[0][nLen-1] - this.xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[2]==0.0D)step[2] = 0.1*(this.yData[nLen-1] - this.yData[0]);

        // Nelder and Mead Simplex Regression
        SigmoidHillSipsFunction f = new SigmoidHillSipsFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }



    // method for fitting data to a EC50 dose response curve (four parameter logistic)
    public void ec50(){
        fitEC50(0);
    }

    // method for fitting data to a EC50 dose response curve with plot and print out (four parameter logistic)
    public void ec50Plot(){
        fitEC50(1);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic)
    // bottom constrained to zero or positive values
    public void ec50constrained(){
        fitEC50(2);
    }

    // method for fitting data to a EC50 dose response curve with plot and print out (four parameter logistic)
    // bottom constrained to zero or positive values
    public void ec50constrainedPlot(){
        fitEC50(3);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic)
    public void fourParameterLogistic(){
        fitEC50(0);
    }

    // method for fitting data to a EC50 dose response curve with plot and print out (four parameter logistic)
    public void fourParameterLogisticPlot(){
        fitEC50(1);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic)
    // bottom constrained to zero or positive values
    public void fourParameterLogisticConstrained(){
        fitEC50(2);
    }

    // method for fitting data to a EC50 dose response curve with plot and print out (four parameter logistic)
    // bottom constrained to zero or positive values
    public void fourParameterLogisticConstrainedPlot(){
        fitEC50(3);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic)
    protected void fitEC50(int cpFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        int plotFlag = 0;
        boolean constrained = false;
        this.userSupplied = false;
	    switch(cpFlag){
	        case 0: this.lastMethod= 39;
	                plotFlag = 0;
	                break;
	        case 1: this.lastMethod= 39;
	                plotFlag = 1;
	                break;
	        case 2: this.lastMethod= 41;
	                plotFlag = 0;
	                constrained = true;
	                break;
	        case 3: this.lastMethod= 41;
	                plotFlag = 1;
	                constrained = true;
	                break;
	    }

	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=4;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // Initial estimate of EC50
        int nLen = this.yData.length;
        this.midPoint();

	    // estimate of slope
	    double hillSlope = 1.0;
        if(this.directionFlag==-1)hillSlope = -1.0;

        // initial estimates
        double[] start = new double[nParam];
        start[0] = this.topS;
        start[1] = this.bottomS;
        start[2] = this.midPointXvalue;
        start[3] = hillSlope;

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++){
            step[i] = 0.1*Math.abs(start[i]);
        }

        if(step[0]==0.0D)step[0] = 0.1*(yData[nLen-1] - yData[0]);
        if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);
        if(step[2]==0.0D)step[2] = 0.05*(xData[0][nLen-1] - xData[0][0]);
        if(step[3]==0.0D)step[3] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);

        // Constrained option
        if(constrained)this.addConstraint(0, -1, 0.0D);

        // Nelder and Mead Simplex Regression
        EC50Function f = new EC50Function();
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic) with top and bottom fixed
    // no plotting
    protected void ec50(double bottom, double top){
        this.lastMethod = 40;
        this.fitEC50(bottom, top, 0);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic) with top and bottom fixed
    // no plotting
    protected void fourParameterLogistic(double bottom, double top){
        this.lastMethod = 40;
        this.fitEC50(bottom, top, 0);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic) with top and bottom fixed
    // plotting
    protected void ec50Plot(double bottom, double top){
        this.lastMethod = 40;
        this.fitEC50(bottom, top, 1);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic) with top and bottom fixed
    // plotting
    protected void fourParameterLogisticPlot(double bottom, double top){
        this.lastMethod = 40;
        this.fitEC50(bottom, top, 1);
    }

    // method for fitting data to a EC50 dose response curve (four parameter logistic) with top and bottom fixed
    // common fitting method
    private void fitEC50(double bottom, double top, int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");

        this.bottom = bottom;
        this.top = top;

	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // Initial estimate of EC50
        int nLen = this.yData.length;
        this.midPoint();

	    // estimate of slope
	    double hillSlope = 1.0;
        if(this.directionFlag==-1)hillSlope = -1.0;

        // initial estimates
        double[] start = new double[nParam];
        start[0] = this.midPointXvalue;
        start[1] = hillSlope;

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++){
            step[i] = 0.1*Math.abs(start[i]);
        }

        if(step[0]==0.0D)step[0] = 0.05*(xData[0][nLen-1] - xData[0][0]);
        if(step[1]==0.0D)step[1] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        EC50FixedFunction f = new EC50FixedFunction();
        f.setBottom(this.bottom);
        f.setTop(this.top);

        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }


    // Estimate mid point of sigmoid curves
    private void midPoint(){
	    // Estimate of bottom and top
	    this.bottomS = Fmath.minimum(this.yData);
	    this.topS = Fmath.maximum(this.yData);
        this.bottomSindex = 0;
        this.topSindex = 0;
        int nLen = this.yData.length;
        int ii = 0;
        boolean test = true;
        while(test){
            if(this.bottomS==this.yData[ii]){
                this.bottomSindex = ii;
                test = false;
            }
            else{
                ii++;
                if(ii>=nLen)throw new IllegalArgumentException("This should not be possible - check coding");
            }
        }
        test = true;
        ii = 0;
        while(test){
            if(this.topS==this.yData[ii]){
                this.topSindex = ii;
                test = false;
            }
            else{
                ii++;
                if(ii>=nLen)throw new IllegalArgumentException("This should not be possible - check coding");
            }
        }
        this.directionFlag = 1;
        if(this.topSindex<this.bottomSindex)this.directionFlag = -1;

	    // Estimate of midpoint
	    double yyymid = this.topS - (this.topS - this.bottomS)/2.0D;
	    this.midPointYvalue = yyymid;
	    double yyxmidl = this.xData[0][0];
	    ii = 0;
	    if(this.directionFlag==1){
	        test = true;
	        while(test){
	            if(this.yData[ii]>=yyymid){
	                yyxmidl = this.xData[0][ii];
	                test = false;
	            }
	            else{
	                ii++;
	                if(ii>=nLen){
	                    yyxmidl = Stat.mean(this.xData[0]);
	                    ii=nLen-1;
                        test = false;
                    }
	            }
	        }
	        double yyxmidh = this.xData[0][nLen-1];
	        int jj = nLen-1;
	        test = true;
	        while(test){
	            if(this.yData[jj]<=yyymid){
	                yyxmidh = this.xData[0][jj];
	                test = false;
	            }
	            else{
	                jj--;
	                if(jj<0){
	                    yyxmidh = Stat.mean(this.xData[0]);
	                    jj=1;
                        test = false;
                    }
	            }
	        }
	        if(ii<jj){
	            int jjh = jj;
	            jj = ii;
	            ii = jjh;
	        }
	        this.midPointLowerIndex = jj;
	        this.midPointUpperIndex = ii;
	        this.midPointXvalue = (this.xData[0][ii]+this.xData[0][jj])/2.0;
	    }
	    else{
	        ii = 0;
	        test = true;
	        while(test){
	            if(this.yData[ii]<=yyymid){
	                yyxmidl = this.xData[0][ii];
	                test = false;
	            }
	            else{
	                ii++;
	                if(ii>=nLen){
	                    yyxmidl = Stat.mean(this.xData[0]);
	                    ii=nLen-1;
                        test = false;
                    }
	            }
	        }
	        double yyxmidh = this.xData[0][nLen-1];
	        int jj = nLen-1;
	        test = true;
	        while(test){
	            if(this.yData[jj]>=yyymid){
	                yyxmidh = this.xData[0][jj];
	                test = false;
	            }
	            else{
	                jj--;
	                if(jj<0){
	                    yyxmidh = Stat.mean(this.xData[0]);
	                    jj=1;
                        test = false;
                    }
	            }
	        }
	        if(ii>jj){
	            int jjh = jj;
	            jj = ii;
	            ii = jjh;
	        }

	        if(ii<jj){
	            int jjh = jj;
	            jj = ii;
	            ii = jjh;
	        }
	        this.midPointLowerIndex = jj;
	        this.midPointUpperIndex = ii;
	        this.midPointXvalue = (this.xData[0][ii]+this.xData[0][jj])/2.0;
	    }
    }

    // method for fitting data to a five parameter logistic function
    public void fiveParameterLogistic(){
        fitfiveParameterLogistic(0);
    }

    // method for fitting data to five parameter logistic function
    public void fiveParameterLogisticPlot(){
        fitfiveParameterLogistic(1);
    }

    // method for fitting data to a five parameter logistic function
    protected void fitfiveParameterLogistic(int cpFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        int plotFlag = 0;
        boolean constrained = false;
        this.userSupplied = false;
	    this.lastMethod= 51;

	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam = 5;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // Initial estimate of y50
        int nLen = this.yData.length;
        this.midPoint();

         // estimate of slope
        double hillSlope = 1.0;
        if(this.directionFlag==-1 && hillSlope>=0)hillSlope = -1.0;

        // initial estimates
        double[] start = new double[nParam];
        start[0] = this.topS;
        start[1] = this.bottomS;
        start[2] = this.midPointXvalue;
        start[3] = hillSlope;
        start[4] = 1.0;

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++){
            step[i] = 0.1*Math.abs(start[i]);
        }
        if(step[0]==0.0D)step[0] = 0.1*(yData[nLen-1] - yData[0]);
        if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);
        if(step[2]==0.0D)step[2] = 0.05*(xData[0][nLen-1] - xData[0][0]);
        if(step[3]==0.0D)step[3] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        Logistic5Function f = new Logistic5Function();
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // method for fitting data to five parameter logistic with top and bottom fixed
    // no plotting
    protected void fiveParameterLogistic(double bottom, double top){
        this.lastMethod = 42;
        this.fitFiveParameterLogistic(bottom, top, 0);
    }

    // method for fitting data to five parameter logistic with top and bottom fixed
    // plotting
    protected void fiveParameterLogisticPlot(double bottom, double top){
        this.lastMethod = 42;
        this.fitFiveParameterLogistic(bottom, top, 1);
    }

    // method for fitting data to five parameter logistic with top and bottom fixed
    // common fitting method
    private void fitFiveParameterLogistic(double bottom, double top, int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");

	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // Initial estimate of C50
        int nLen = this.yData.length;
        this.midPoint();

	    // estimate of slope
	    double hillSlope = 1.0;
        if(this.directionFlag==-1)hillSlope = -1.0;

        // initial estimates
        double[] start = new double[nParam];
        start[0] = this.midPointXvalue;
        start[1] = hillSlope;
        start[2] = 1.0;

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++){
            step[i] = 0.1*Math.abs(start[i]);
        }

        if(step[0]==0.0D)step[0] = 0.05*(xData[0][nLen-1] - xData[0][0]);
        if(step[1]==0.0D)step[1] = 0.1*(xData[0][nLen-1] - xData[0][0])/(yData[nLen-1] - yData[0]);
        if(step[2]==0.0D)step[2] = 0.1;

        // Nelder and Mead Simplex Regression
        Logistic5FixedFunction f = new Logistic5FixedFunction();
        f.setBottom(bottom);
        f.setTop(top);

        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }


	// method for fitting data to a rectangular hyberbola
    public void rectangularHyperbola(){
        fitRectangularHyperbola(0);
    }

    // method for fitting data to a rectangular hyberbola with plot and print out
    public void rectangularHyperbolaPlot(){
        fitRectangularHyperbola(1);
    }

    // method for fitting data to a rectangular hyperbola
    protected void fitRectangularHyperbola(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=26;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    if(!this.scaleFlag)this.nParam=1;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nParam];
        start[0] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[1] = yymax;
            }
            else{
                start[1] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        RectangularHyperbolaFunction f = new RectangularHyperbolaFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // method for fitting data to a shifted rectangular hyberbola
    public void shiftedRectangularHyperbola(){
        fitShiftedRectangularHyperbola(0);
    }

    // method for fitting data to a shifted rectangular hyberbola with plot and print out
    public void shiftedRectangularHyperbolaPlot(){
        fitShiftedRectangularHyperbola(1);
    }

    // method for fitting data to a shifted rectangular hyperbola
    protected void fitShiftedRectangularHyperbola(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod = 52;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    boolean sfhold = this.scaleFlag;
	    this.scaleFlag = false;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nParam];
        start[0] = theta0;
        start[1] = this.yData[0];
        if(dirFlag==1){
                start[2] = yymax;
        }
        else{
                start[2] = yymin;
        }

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(step[1]==0.0D)step[1] = Math.abs(yData[nLen-1] - yData[0])/20.0D;
        if(step[2]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        ShiftedRectangularHyperbolaFunction f = new ShiftedRectangularHyperbolaFunction();
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
        this.scaleFlag = sfhold;
    }


    // method for fitting data to a scaled Heaviside Step Function
    public void stepFunction(){
        fitStepFunction(0);
    }

    // method for fitting data to a scaled Heaviside Step Function with plot and print out
    public void stepFunctionPlot(){
        fitStepFunction(1);
    }

    // method for fitting data to a scaled Heaviside Step Function
    protected void fitStepFunction(int plotFlag){

        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=27;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=2;
	    if(!this.scaleFlag)this.nParam=1;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

	    // Estimate  of theta
	    double yymin = Fmath.minimum(this.yData);
	    double yymax = Fmath.maximum(this.yData);
	    int dirFlag = 1;
	    if(yymin<0)dirFlag=-1;
	    double yyymid = (yymax - yymin)/2.0D;
	    double yyxmidl = xData[0][0];
	    int ii = 1;
	    int nLen = this.yData.length;
	    boolean test = true;
	    while(test){
	        if(this.yData[ii]>=dirFlag*yyymid){
	            yyxmidl = xData[0][ii];
	            test = false;
	        }
	        else{
	            ii++;
	            if(ii>=nLen){
	                yyxmidl = Stat.mean(this.xData[0]);
	                ii=nLen-1;
                    test = false;
                }
	        }
	    }
	    double yyxmidh = xData[0][nLen-1];
	    int jj = nLen-1;
	    test = true;
	    while(test){
	        if(this.yData[jj]<=dirFlag*yyymid){
	            yyxmidh = xData[0][jj];
	            test = false;
	        }
	        else{
	            jj--;
	            if(jj<0){
	                yyxmidh = Stat.mean(this.xData[0]);
	                jj=1;
                    test = false;
                }
	        }
	    }
	    int thetaPos = (ii+jj)/2;
	    double theta0 = xData[0][thetaPos];

        // initial estimates
        double[] start = new double[nParam];
        start[0] = theta0;
        if(this.scaleFlag){
            if(dirFlag==1){
                start[1] = yymax;
            }
            else{
                start[1] = yymin;
            }
        }

        // initial step sizes
        double[] step = new double[nParam];
        for(int i=0; i<nParam; i++)step[i] = 0.1*start[i];
        if(step[0]==0.0D)step[0] = (xData[0][nLen-1] - xData[0][0])/20.0D;
        if(this.scaleFlag)if(step[1]==0.0D)step[1] = 0.1*(yData[nLen-1] - yData[0]);

        // Nelder and Mead Simplex Regression
        StepFunctionFunction f = new StepFunctionFunction();
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }
    }

    // Fit to a Logistic
	public void logistic(){
	    this.fitLogistic(0);
	}

	// Fit to a Logistic
	public void logisticPlot(){

	    this.fitLogistic(1);
	}

    // Fit data to a Logistic probability function
	protected void fitLogistic(int plotFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=30;
	    this.userSupplied = false;
	    this.linNonLin = false;
	    this.zeroCheck = false;
	    this.nParam=3;
	    if(!this.scaleFlag)this.nParam=2;
	    this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitLogistic(): This implementation of the Logistic distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	                yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // Calculate  x value at peak y (estimate of the Logistic mean)
	    ArrayList<Object> ret1 = Regression.dataSign(yData);
	 	Integer tempi = null;
	    tempi = (Integer)ret1.get(5);
	 	int peaki = tempi.intValue();
	    double mu = xData[0][peaki];

	    // Calculate an estimate of the beta
	    double beta = Math.sqrt(6.0D)*halfWidth(xData[0], yData)/Math.PI;

	    // Calculate estimate of y scale
	    tempd = (Double)ret1.get(4);
	    double ym = tempd.doubleValue();
	    ym=ym*beta*Math.sqrt(2.0D*Math.PI);

        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = mu;
        start[1] = beta;
        if(this.scaleFlag){
            start[2] = ym;
        }
        step[0] = 0.1D*beta;
        step[1] = 0.1D*start[1];
        if(step[1]==0.0D){
            ArrayList<Object> ret0 = Regression.dataSign(xData[0]);
	 	    Double tempdd = null;
	        tempdd = (Double)ret0.get(2);
	 	    double xmax = tempdd.doubleValue();
	 	    if(xmax==0.0D){
	 	        tempdd = (Double)ret0.get(0);
	 	        xmax = tempdd.doubleValue();
	 	    }
	        step[0]=xmax*0.1D;
	    }
        if(this.scaleFlag)step[2] = 0.1D*start[2];

	    // Nelder and Mead Simplex Regression
        LogisticFunction f = new LogisticFunction();
        this.addConstraint(1,-1,0.0D);
        f.scaleOption = this.scaleFlag;
        f.scaleFactor = this.yScaleFactor;
        Object regFun2 = (Object)f;
        this.nelderMead(regFun2, start, step, this.fTol, this.nMax);

        if(plotFlag==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(f);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }

	}

    public void beta(){
	    this.fitBeta(0, 0);
	}

	public void betaPlot(){
	    this.fitBeta(1, 0);
	}

	public void betaMinMax(){
	    this.fitBeta(0, 1);
	}

	public void betaMinMaxPlot(){
	    this.fitBeta(1, 1);
	}

    protected void fitBeta(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=31;
	                this.nParam=3;
	                break;
	        case 1: this.lastMethod=32;
	                this.nParam=5;
	                break;
        }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;

	    this.zeroCheck = false;
		this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitBeta(): This implementation of the Beta distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	            yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // check  x data
	    ArrayList<Object> retX = Regression.dataSign(xData[0]);
	 	Integer tempi = null;

        // Calculate  x value at peak y (estimate of the 'distribution mode')
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();
	    double distribMode = xData[0][peaki];

	    // minimum value
	    tempd = (Double)retX.get(0);
	    double minX = tempd.doubleValue();
	    // maximum value
	    tempd = (Double)retX.get(2);
	    double maxX = tempd.doubleValue();
	    // mean value
	    tempd = (Double)retX.get(8);
	    double meanX = tempd.doubleValue();


	    // test that data is within range
	    if(typeFlag==0){
	        if(minX<0.0D){
	            System.out.println("Regression: beta: data points must be greater than or equal to 0");
	            System.out.println("method betaMinMax used in place of method beta");
	            typeFlag = 1;
	            this.lastMethod=32;
	            this.nParam=5;
	        }
	        if(maxX>1.0D){
	            System.out.println("Regression: beta: data points must be less than or equal to 1");
	            System.out.println("method betaMinMax used in place of method beta");
	            typeFlag = 1;
	            this.lastMethod=32;
	            this.nParam=5;
	        }
        }

	    // Calculate an estimate of the alpha, beta and scale factor
	    double dMode = distribMode;
	    double dMean = meanX;
	    if(typeFlag==1){
	        dMode = (distribMode - minX*0.9D)/(maxX*1.2D - minX*0.9D);
	        dMean = (meanX - minX*0.9D)/(maxX*1.2D - minX*0.9D);
        }
	    double alphaGuess = 2.0D*dMode*dMean/(dMode - dMean);
	    if(alphaGuess<1.3)alphaGuess = 1.6D;
	    double betaGuess = alphaGuess*(1.0D - dMean)/dMean;
	    if(betaGuess<=1.3)betaGuess = 1.6D;
	    double scaleGuess = 0.0D;
	    if(typeFlag==0){
	        scaleGuess = yPeak/Stat.betaPDF(alphaGuess, betaGuess, distribMode);
	    }
	    else{
	        scaleGuess = yPeak/Stat.betaPDF(minX, maxX, alphaGuess, betaGuess, distribMode);
        }
        if(scaleGuess<0)scaleGuess=1;


	    // Nelder and Mead Simplex Regression for Gumbel
        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        switch(typeFlag){
            case 0: start[0] = alphaGuess;          //alpha
                    start[1] = betaGuess;           //beta
                    if(this.scaleFlag){
                        start[2] = scaleGuess;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    if(this.scaleFlag)step[2] = 0.1D*start[2];

	                // Add constraints
                    this.addConstraint(0,-1,1.0D);
                    this.addConstraint(1,-1,1.0D);
                    break;
            case 1: start[0] = alphaGuess;          //alpha
                    start[1] = betaGuess;           //beta
                    start[2] = 0.9D*minX;           // min
                    start[3] = 1.1D*maxX;           // max
                    if(this.scaleFlag){
                        start[4] = scaleGuess;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    step[2] = 0.1D*start[2];
                    step[3] = 0.1D*start[3];
                    if(this.scaleFlag)step[4] = 0.1D*start[4];

	                // Add constraints
                    this.addConstraint(0,-1,1.0D);
                    this.addConstraint(1,-1,1.0D);
                    this.addConstraint(2,+1,minX);
                    this.addConstraint(3,-1,maxX);
                    break;

        }

        // Create instance of Beta function
        BetaFunction ff = new BetaFunction();

        // Set minimum maximum type option
        ff.typeFlag = typeFlag;

        // Set ordinate scaling option
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;

        // Perform simplex regression
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}

    public void gamma(){
	    this.fitGamma(0, 0);
	}

	public void gammaPlot(){
	    this.fitGamma(1, 0);
	}

	public void gammaStandard(){
	    this.fitGamma(0, 1);
	}

	public void gammaStandardPlot(){
	    this.fitGamma(1, 1);
	}

    protected void fitGamma(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
        this.userSupplied = false;
	    switch(typeFlag){
    	    case 0: this.lastMethod=33;
	                this.nParam=4;
	                break;
	        case 1: this.lastMethod=34;
	                this.nParam=2;
	                break;
        }
	    if(!this.scaleFlag)this.nParam=this.nParam-1;

	    this.zeroCheck = false;
		this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGamma(): This implementation of the Gamma distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	            yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // check  x data
	    ArrayList<Object> retX = Regression.dataSign(xData[0]);
	 	Integer tempi = null;

        // Calculate  x value at peak y (estimate of the 'distribution mode')
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();
	    double distribMode = xData[0][peaki];

	    // minimum value
	    tempd = (Double)retX.get(0);
	    double minX = tempd.doubleValue();
	    // maximum value
	    tempd = (Double)retX.get(2);
	    double maxX = tempd.doubleValue();
	    // mean value
	    tempd = (Double)retX.get(8);
	    double meanX = tempd.doubleValue();


	    // test that data is within range
	    if(typeFlag==1){
	        if(minX<0.0D){
	            System.out.println("Regression: gammaStandard: data points must be greater than or equal to 0");
	            System.out.println("method gamma used in place of method gammaStandard");
	            typeFlag = 0;
	            this.lastMethod=33;
	            this.nParam=2;
	        }
        }

	    // Calculate an estimate of the mu, beta, gamma and scale factor
	    double muGuess = 0.8D*minX;
	    if(muGuess==0.0D)muGuess = -0.1D;
	    double betaGuess = meanX - distribMode;
	    if(betaGuess<=0.0D)betaGuess = 1.0D;
	    double gammaGuess = (meanX + muGuess)/betaGuess;
	    if(typeFlag==1)gammaGuess = meanX;
	    if(gammaGuess<=0.0D)gammaGuess = 1.0D;
	    double scaleGuess = 0.0D;
	    if(typeFlag==0){
	        scaleGuess = yPeak/Stat.gammaPDF(muGuess, betaGuess, gammaGuess, distribMode);
	    }
	    else{
	        scaleGuess = yPeak/Stat.gammaPDF(gammaGuess, distribMode);
        }
        if(scaleGuess<0)scaleGuess=1;


	    // Nelder and Mead Simplex Regression for Gamma
        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        switch(typeFlag){
            case 1: start[0] = gammaGuess;          //gamma
                    if(this.scaleFlag){
                        start[1] = scaleGuess;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];

	                // Add constraints
                    this.addConstraint(0,-1,0.0D);
                    break;
            case 0: start[0] = muGuess;             // mu
                    start[1] = betaGuess;           // beta
                    start[2] = gammaGuess;          // gamma
                    if(this.scaleFlag){
                        start[3] = scaleGuess;      //y axis scaling factor
                    }
                    step[0] = 0.1D*start[0];
                    step[1] = 0.1D*start[1];
                    step[2] = 0.1D*start[2];
                    if(this.scaleFlag)step[3] = 0.1D*start[3];

	                // Add constraints
                    this.addConstraint(1,-1,0.0D);
                    this.addConstraint(2,-1,0.0D);
                    break;
        }

        // Create instance of Gamma function
        GammaFunction ff = new GammaFunction();

        // Set type option
        ff.typeFlag = typeFlag;

        // Set ordinate scaling option
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;

        // Perform simplex regression
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(ff);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}

    // Fit to an Erlang Distribution
    public void erlang(){
	    this.fitErlang(0, 0);
	}

	public void erlangPlot(){
	    this.fitErlang(1, 0);
	}

    protected void fitErlang(int allTest, int typeFlag){
        if(this.multipleY)throw new IllegalArgumentException("This method cannot handle multiply dimensioned y arrays");
	    this.lastMethod=35;
	    this.userSupplied = false;
	    int nParam0 = 2;    // number of erlang terms
	    int nParam1 = 4;    // number of gamma terms - initial estimates procedure
	    this.nParam = nParam1;
	    if(!this.scaleFlag)this.nParam=this.nParam-1;

	    this.zeroCheck = false;
		this.degreesOfFreedom=this.nData - this.nParam;
	    if(this.degreesOfFreedom<1 && !this.ignoreDofFcheck)throw new IllegalArgumentException("Degrees of freedom must be greater than 0");

	    // order data into ascending order of the abscissae
        Regression.sort(this.xData[0], this.yData, this.weight);

        // check sign of y data
	    Double tempd=null;
	    ArrayList<Object> retY = Regression.dataSign(yData);
	    tempd = (Double)retY.get(4);
	    double yPeak = tempd.doubleValue();
	    boolean yFlag = false;
	    if(yPeak<0.0D){
	        System.out.println("Regression.fitGamma(): This implementation of the Erlang distribution takes only positive y values\n(noise taking low values below zero are allowed)");
	        System.out.println("All y values have been multiplied by -1 before fitting");
	        for(int i =0; i<this.nData; i++){
	            yData[i] = -yData[i];
	        }
	        retY = Regression.dataSign(yData);
	        yFlag=true;
	    }

	    // check  x data
	    ArrayList<Object> retX = Regression.dataSign(xData[0]);
	 	Integer tempi = null;

        // Calculate  x value at peak y (estimate of the 'distribution mode')
	    tempi = (Integer)retY.get(5);
	 	int peaki = tempi.intValue();
	    double distribMode = xData[0][peaki];

	    // minimum value
	    tempd = (Double)retX.get(0);
	    double minX = tempd.doubleValue();
	    // maximum value
	    tempd = (Double)retX.get(2);
	    double maxX = tempd.doubleValue();
	    // mean value
	    tempd = (Double)retX.get(8);
	    double meanX = tempd.doubleValue();


	    // test that data is within range
        if(minX<0.0D)throw new IllegalArgumentException("data points must be greater than or equal to 0");

        // FIT TO GAMMA DISTRIBUTION TO OBTAIN INITIAL ESTIMATES
	    // Calculate an estimate of the mu, beta, gamma and scale factor
	    double muGuess = 0.8D*minX;
	    if(muGuess==0.0D)muGuess = -0.1D;
	    double betaGuess = meanX - distribMode;
	    if(betaGuess<=0.0D)betaGuess = 1.0D;
	    double gammaGuess = (meanX + muGuess)/betaGuess;
	    if(typeFlag==1)gammaGuess = meanX;
	    if(gammaGuess<=0.0D)gammaGuess = 1.0D;
	    double scaleGuess = 0.0D;
        scaleGuess = yPeak/Stat.gammaPDF(muGuess, betaGuess, gammaGuess, distribMode);
        if(scaleGuess<0)scaleGuess=1;


	    // Nelder and Mead Simplex Regression for Gamma
        // Fill arrays needed by the Simplex
        double[] start = new double[this.nParam];
        double[] step = new double[this.nParam];
        start[0] = muGuess;             // mu
        start[1] = betaGuess;           // beta
        start[2] = gammaGuess;          // gamma
        if(this.scaleFlag)start[3] = scaleGuess;      //y axis scaling factor

        step[0] = 0.1D*start[0];
        step[1] = 0.1D*start[1];
        step[2] = 0.1D*start[2];
        if(this.scaleFlag)step[3] = 0.1D*start[3];

	    // Add constraints
        this.addConstraint(1,-1,0.0D);
        this.addConstraint(2,-1,0.0D);

        // Create instance of Gamma function
        GammaFunction ff = new GammaFunction();

        // Set type option
        ff.typeFlag = typeFlag;

        // Set ordinate scaling option
        ff.scaleOption = this.scaleFlag;
        ff.scaleFactor = this.yScaleFactor;

        // Perform simplex regression
        Object regFun3 = (Object)ff;
        this.nelderMead(regFun3, start, step, this.fTol, this.nMax);

        // FIT TO ERLANG DISTRIBUTION USING GAMMA BEST ESTIMATES AS INITIAL ESTIMATES
        // AND VARYING RATE PARAMETER BY UNIT STEPS
        this.removeConstraints();

        // Initial estimates
        double[] bestGammaEst = this.getCoeff();

        // Swap from Gamma dimensions to Erlang dimensions
        this.nParam = nParam0;
        start = new double[this.nParam];
        step = new double[this.nParam];
        if(bestGammaEst[3]<0.0)bestGammaEst[3] *= -1.0;

        // initial estimates
        start[0] = 1.0D/bestGammaEst[1];                    // lambda
        if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor

        step[0] = 0.1D*start[0];
        if(this.scaleFlag)step[1] = 0.1D*start[1];

	    // Add constraints
        this.addConstraint(0,-1,0.0D);

        // fix initial integer rate parameter
        double kay0 = Math.round(bestGammaEst[2]);
        double kay = kay0;

        // Create instance of Erlang function
        ErlangFunction ef = new ErlangFunction();

        // Set ordinate scaling option
        ef.scaleOption = this.scaleFlag;
        ef.scaleFactor = this.yScaleFactor;
        ef.kay = kay;

        // Fit stepping up
        boolean testKay = true;
        double ssMin = Double.NaN;
        double upSS = Double.NaN;
        double upKay = Double.NaN;
        double kayFinal = Double.NaN;
        int iStart = 1;
        int ssSame = 0;

        while(testKay){

            // Perform simplex regression
            Object regFun4 = (Object)ef;

            this.nelderMead(regFun4, start, step, this.fTol, this.nMax);
            double sumOfSquaresError = this.getSumOfSquares();
            if(iStart==1){
                iStart = 2;
                ssMin = sumOfSquaresError;
                kay = kay + 1;
                start[0] = 1.0D/bestGammaEst[1];                    // lambda
                if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor
                step[0] = 0.1D*start[0];
                if(this.scaleFlag)step[1] = 0.1D*start[1];
                this.addConstraint(0,-1,0.0D);
                ef.kay = kay;
            }
            else{
                if(sumOfSquaresError<=ssMin){
                    if(sumOfSquaresError==ssMin){
                        ssSame++;
                        if(ssSame==10){
                            upSS = ssMin;
                            upKay = kay - 5;
                            testKay = false;
                        }
                    }
                    ssMin = sumOfSquaresError;
                    kay = kay + 1;
                    start[0] = 1.0D/bestGammaEst[1];                    // lambda
                    if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor
                    step[0] = 0.1D*start[0];
                    if(this.scaleFlag)step[1] = 0.1D*start[1];
                    this.addConstraint(0,-1,0.0D);
                    ef.kay = kay;
                }
                else{
                    upSS = ssMin;
                    upKay = kay - 1;
                    testKay = false;
                }
            }
        }

        if(kay0==1){
            kayFinal = upKay;
        }
        else{

            // Fit stepping down
            iStart = 1;
            testKay = true;
            ssMin = Double.NaN;
            double downSS = Double.NaN;
            double downKay = Double.NaN;
            // initial estimates
            start[0] = 1.0D/bestGammaEst[1];                    // lambda
            if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor
            step[0] = 0.1D*start[0];
            if(this.scaleFlag)step[1] = 0.1D*start[1];
	        // Add constraints
            this.addConstraint(0,-1,0.0D);
            kay = kay0;
            ef.kay = kay;

            while(testKay){

                // Perform simplex regression
                Object regFun5 = (Object)ef;

                this.nelderMead(regFun5, start, step, this.fTol, this.nMax);
                double sumOfSquaresError = this.getSumOfSquares();
                if(iStart==1){
                    iStart = 2;
                    ssMin = sumOfSquaresError;
                    kay = kay - 1;
                    if(Math.rint(kay)<1L){
                        downSS = ssMin;
                        downKay = kay + 1;
                        testKay = false;
                    }
                    else{
                        start[0] = 1.0D/bestGammaEst[1];                    // lambda
                        if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor
                        step[0] = 0.1D*start[0];
                        if(this.scaleFlag)step[1] = 0.1D*start[1];
                        this.addConstraint(0,-1,0.0D);
                        ef.kay = kay;
                    }
                }
                else{
                    if(sumOfSquaresError<=ssMin){
                        ssMin = sumOfSquaresError;
                        kay = kay - 1;
                        if(Math.rint(kay)<1L){
                            downSS = ssMin;
                            downKay = kay + 1;
                            testKay = false;
                        }
                        else{
                            start[0] = 1.0D/bestGammaEst[1];                    // lambda
                            if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor
                            step[0] = 0.1D*start[0];
                            if(this.scaleFlag)step[1] = 0.1D*start[1];
                            this.addConstraint(0,-1,0.0D);
                            ef.kay = kay;
                        }
                    }
                    else{
                        downSS = ssMin;
                        downKay = kay + 1;
                        testKay = false;
                    }
                }

            }
            if(downSS<upSS){
                kayFinal = downKay;
            }
            else{
                kayFinal = upKay;
            }

        }

        // Penultimate fit
        // initial estimates
        start[0] = 1.0D/bestGammaEst[1];                    // lambda
        if(this.scaleFlag)start[1] = bestGammaEst[3];       //y axis scaling factor

        step[0] = 0.1D*start[0];
        if(this.scaleFlag)step[1] = 0.1D*start[1];

	    // Add constraints
        this.addConstraint(0,-1,0.0D);

        // Set function variables
        ef.scaleOption = this.scaleFlag;
        ef.scaleFactor = this.yScaleFactor;
        ef.kay = Math.round(kayFinal);
        this.kayValue = Math.round(kayFinal);

        // Perform penultimate regression
        Object regFun4 = (Object)ef;

        this.nelderMead(regFun4, start, step, this.fTol, this.nMax);
        double[] coeff = getCoeff();

        // Final fit

        // initial estimates
        start[0] = coeff[0];                            // lambda
        if(this.scaleFlag)start[1] = coeff[1];          //y axis scaling factor

        step[0] = 0.1D*start[0];
        if(this.scaleFlag)step[1] = 0.1D*start[1];

	    // Add constraints
        this.addConstraint(0,-1,0.0D);

        // Set function variables
        ef.scaleOption = this.scaleFlag;
        ef.scaleFactor = this.yScaleFactor;
        ef.kay = Math.round(kayFinal);
        this.kayValue = Math.round(kayFinal);

        // Perform final regression
       Object regFun5 = (Object)ef;

        this.nelderMead(regFun5, start, step, this.fTol, this.nMax);

        if(allTest==1){
            // Print results
            if(!this.supressPrint)this.print();

            // Plot results
            int flag = this.plotXY(ef);
            if(flag!=-2 && !this.supressYYplot)this.plotYY();
        }

        if(yFlag){
            // restore data
            for(int i=0; i<this.nData-1; i++){
                this.yData[i]=-this.yData[i];
            }
        }
	}

	// return Erlang rate parameter (k) value
	public double getKayValue(){
	    return this.kayValue;
	}


    // HISTOGRAM METHODS
        // Distribute data into bins to obtain histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in histogramBins");
                return histogramBins(newData, binWidth, binZero);
            }
            else{
                 return histogramBins(data, binWidth, binZero);

            }
        }

        // Distribute data into bins to obtain histogram
        // zero bin position provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero){
            double dmax = Fmath.maximum(data);
            int nBins = (int) Math.ceil((dmax - binZero)/binWidth);
            if(binZero+nBins*binWidth>dmax)nBins++;
            int nPoints = data.length;
            int[] dataCheck = new int[nPoints];
            for(int i=0; i<nPoints; i++)dataCheck[i]=0;
            double[]binWall = new double[nBins+1];
            binWall[0]=binZero;
            for(int i=1; i<=nBins; i++){
                binWall[i] = binWall[i-1] + binWidth;
            }
            double[][] binFreq = new double[2][nBins];
            for(int i=0; i<nBins; i++){
                binFreq[0][i]= (binWall[i]+binWall[i+1])/2.0D;
                binFreq[1][i]= 0.0D;
            }
            boolean test = true;

            for(int i=0; i<nPoints; i++){
                test=true;
                int j=0;
                while(test){
                    if(j==nBins-1){
                        if(data[i]>=binWall[j] && data[i]<=binWall[j+1]*(1.0D + Regression.histTol)){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    else{
                        if(data[i]>=binWall[j] && data[i]<binWall[j+1]){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    if(test){
                        if(j==nBins-1){
                            test=false;
                        }
                        else{
                            j++;
                        }
                    }
                }
            }
            int nMissed=0;
            for(int i=0; i<nPoints; i++)if(dataCheck[i]==0){
                nMissed++;
                System.out.println("p " + i + " " + data[i] + " " + binWall[0] + " " + binWall[nBins]);
            }
            if(nMissed>0)System.out.println(nMissed+" data points, outside histogram limits, excluded in histogramBins");
            return binFreq;
        }

        // Distribute data into bins to obtain histogram
        // zero bin position calculated
        public static double[][] histogramBins(double[] data, double binWidth){

            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            double binZero = dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double histoSpan = ((double)nBins)*binWidth;
            double rem = histoSpan - span;
            if(rem>=0){
                binZero -= rem/2.0D;
            }
            else{
                if(Math.abs(rem)/span>Regression.histTol){
                    // readjust binWidth
                    boolean testBw = true;
                    double incr = Regression.histTol/nBins;
                    int iTest = 0;
                    while(testBw){
                       binWidth += incr;
                       histoSpan = ((double)nBins)*binWidth;
                        rem = histoSpan - span;
                        if(rem<0){
                            iTest++;
                            if(iTest>1000){
                                testBw = false;
                                System.out.println("histogram method could not encompass all data within histogram\nContact Michael thomas Flanagan");
                            }
                        }
                        else{
                            testBw = false;
                        }
                    }
                }
            }

            return Regression.histogramBins(data, binWidth, binZero);
        }

}

//  CLASSES TO EVALUATE THE SPECIAL FUNCTIONS

// Class to evaluate the Gausian (normal) function y = (yscale/sd.sqrt(2.pi)).exp(-0.5[(x - xmean)/sd]^2).
class GaussianFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double y = (yScale/(p[1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square((x[0]-p[0])/p[1]));
        return y;
    }
}

// Class to evaluate the Gausian (normal) function y = (yscale/sd.sqrt(2.pi)).exp(-0.5[(x - xmean)/sd]^2).
// Some parameters may be fixed
class GaussianFunctionFixed implements RegressionFunction{

    public double[] param = new double[3];
    public boolean[] fixed = new boolean[3];

    public double function(double[] p, double[] x){

        int ii = 0;
        for(int i=0; i<3; i++){
            if(!fixed[i]){
                param[i] = p[ii];
                ii++;
            }
        }

        double y = (param[2]/(param[1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square((x[0]-param[0])/param[1]));
        return y;
    }
}

// Class to evaluate the  multiple Gausian (normal) function y = Sum[(A(i)/sd(i).sqrt(2.pi)).exp(-0.5[(x - xmean(i))/sd(i)]^2)].
class MultipleGaussianFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int nGaussians = 1;

    public double function(double[] p, double[] x){
        double y = 0.0;
        int counter = 0;
        for(int i=0; i<nGaussians; i++){
            y += (p[counter+2]/(p[counter+1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square((x[0]-p[counter])/p[counter+1]));
            counter += 3;
        }
        return y;
    }
}


// Class to evaluate the two parameter log-normal function y = (yscale/x.sigma.sqrt(2.pi)).exp(-0.5[(log(x) - mu)/sd]^2).
class LogNormalTwoParFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double y = (yScale/(x[0]*p[1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square((Math.log(x[0])-p[0])/p[1]));
        return y;
    }
}

// Class to evaluate the three parameter log-normal function y = (yscale/(x-alpha).beta.sqrt(2.pi)).exp(-0.5[(log((x-alpha)/gamma)/sd]^2).
class LogNormalThreeParFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[3];
        double y = (yScale/((x[0]-p[0])*p[1]*Math.sqrt(2.0D*Math.PI)))*Math.exp(-0.5D*Fmath.square(Math.log((x[0]-p[0])/p[2])/p[1]));
        return y;
    }
}


// Class to evaluate the Lorentzian function
// y = (yscale/pi).(gamma/2)/((x - mu)^2+(gamma/2)^2).
class LorentzianFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double y = (yScale/Math.PI)*(p[1]/2.0D)/(Fmath.square(x[0]-p[0])+Fmath.square(p[1]/2.0D));
        return y;
    }
}

// Class to evaluate the Poisson function
// y = yscale.(mu^k).exp(-mu)/k!.
class PoissonFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[1];
        double y = yScale*Math.pow(p[0],x[0])*Math.exp(-p[0])/Stat.factorial(x[0]);
        return y;
    }
}

// Class to evaluate the Gumbel function
class GumbelFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0; // set to 0 -> Minimum Mode Gumbel
                            // reset to 1 -> Maximum Mode Gumbel
                            // reset to 2 -> one parameter Minimum Mode Gumbel
                            // reset to 3 -> one parameter Maximum Mode Gumbel
                            // reset to 4 -> standard Minimum Mode Gumbel
                            // reset to 5 -> standard Maximum Mode Gumbel

    public double function(double[] p, double[] x){
        double y=0.0D;
        double arg=0.0D;
        double yScale = scaleFactor;

        switch(this.typeFlag){
            case 0:
            // y = yscale*(1/gamma)*exp((x-mu)/gamma)*exp(-exp((x-mu)/gamma))
                arg = (x[0]-p[0])/p[1];
                if(scaleOption)yScale = p[2];
                y = (yScale/p[1])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
                break;
            case 1:
            // y = yscale*(1/gamma)*exp((mu-x)/gamma)*exp(-exp((mu-x)/gamma))
                arg = (p[0]-x[0])/p[1];
                if(scaleOption)yScale = p[2];
                y = (yScale/p[1])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
                break;
             case 2:
            // y = yscale*(1/gamma)*exp((x)/gamma)*exp(-exp((x)/gamma))
                arg = x[0]/p[0];
                if(scaleOption)yScale = p[1];
                y = (yScale/p[0])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
                break;
            case 3:
            // y = yscale*(1/gamma)*exp((-x)/gamma)*exp(-exp((-x)/gamma))
                arg = -x[0]/p[0];
                if(scaleOption)yScale = p[1];
                y = (yScale/p[0])*Math.exp(arg)*Math.exp(-(Math.exp(arg)));
                break;
            case 4:
            // y = yscale*exp(x)*exp(-exp(x))
                if(scaleOption)yScale = p[0];
                y = yScale*Math.exp(x[0])*Math.exp(-(Math.exp(x[0])));
                break;
            case 5:
            // y = yscale*exp(-x)*exp(-exp(-x))
                if(scaleOption)yScale = p[0];
                y = yScale*Math.exp(-x[0])*Math.exp(-(Math.exp(-x[0])));
                break;
        }
        return y;
    }
}

// Class to evaluate the Frechet function
// y = yscale.(gamma/sigma)*((x - mu)/sigma)^(-gamma-1)*exp(-((x-mu)/sigma)^-gamma
class FrechetFunctionOne implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0; // set to 0 -> Three Parameter Frechet
                            // reset to 1 -> Two Parameter Frechet
                            // reset to 2 -> Standard Frechet

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 0: if(x[0]>=p[0]){
                        double arg = (x[0] - p[0])/p[1];
                        if(scaleOption)yScale = p[3];
                        y = yScale*(p[2]/p[1])*Math.pow(arg,-p[2]-1.0D)*Math.exp(-Math.pow(arg,-p[2]));
                    }
                    break;
            case 1: if(x[0]>=0.0D){
                        double arg = x[0]/p[0];
                        if(scaleOption)yScale = p[2];
                        y = yScale*(p[1]/p[0])*Math.pow(arg,-p[1]-1.0D)*Math.exp(-Math.pow(arg,-p[1]));
                    }
                    break;
            case 2: if(x[0]>=0.0D){
                        double arg = x[0];
                        if(scaleOption)yScale = p[1];
                        y = yScale*p[0]*Math.pow(arg,-p[0]-1.0D)*Math.exp(-Math.pow(arg,-p[0]));
                    }
                    break;
        }
        return y;
    }
}

// Class to evaluate the semi-linearised Frechet function
// log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
class FrechetFunctionTwo implements RegressionFunction{

    public int typeFlag = 0; // set to 0 -> Three Parameter Frechet
                            // reset to 1 -> Two Parameter Frechet
                            // reset to 2 -> Standard Frechet

    public double function(double[] p, double[] x){
        double y=0.0D;
        switch(typeFlag){
            case 0: y = -p[2]*Math.log(Math.abs(x[0]-p[0])/p[1]);
                    break;
            case 1: y = -p[1]*Math.log(Math.abs(x[0])/p[0]);
                    break;
            case 2: y = -p[0]*Math.log(Math.abs(x[0]));
                    break;
        }

        return y;
    }
}

// Class to evaluate the Weibull function
// y = yscale.(gamma/sigma)*((x - mu)/sigma)^(gamma-1)*exp(-((x-mu)/sigma)^gamma
class WeibullFunctionOne implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0; // set to 0 -> Three Parameter Weibull
                            // reset to 1 -> Two Parameter Weibull
                            // reset to 2 -> Standard Weibull

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 0: if(x[0]>=p[0]){
                        double arg = (x[0] - p[0])/p[1];
                        if(scaleOption)yScale = p[3];
                        y = yScale*(p[2]/p[1])*Math.pow(arg,p[2]-1.0D)*Math.exp(-Math.pow(arg,p[2]));
                    }
                    break;
            case 1: if(x[0]>=0.0D){
                        double arg = x[0]/p[0];
                        if(scaleOption)yScale = p[2];
                        y = yScale*(p[1]/p[0])*Math.pow(arg,p[1]-1.0D)*Math.exp(-Math.pow(arg,p[1]));
                    }
                    break;
            case 2: if(x[0]>=0.0D){
                        double arg = x[0];
                        if(scaleOption)yScale = p[1];
                        y = yScale*p[0]*Math.pow(arg,p[0]-1.0D)*Math.exp(-Math.pow(arg,p[0]));
                    }
                    break;
        }
        return y;
    }
}

// Class to evaluate the semi-linearised Weibull function
// log(log(1/(1-Cumulative y) = gamma*log((x-mu)/sigma)
class WeibullFunctionTwo implements RegressionFunction{

    public int typeFlag = 0; // set to 0 -> Three Parameter Weibull
                            // reset to 1 -> Two Parameter Weibull
                            // reset to 2 -> Standard Weibull

    public double function(double[] p, double[] x){
        double y=0.0D;
        switch(typeFlag){
            case 0: y = p[2]*Math.log(Math.abs(x[0]-p[0])/p[1]);
                    break;
            case 1: y = p[1]*Math.log(Math.abs(x[0])/p[0]);
                    break;
            case 2: y = p[0]*Math.log(Math.abs(x[0]));
            break;
        }

        return y;
    }
}

// Class to evaluate the Rayleigh function
// y = (yscale/sigma)*(x/sigma)*exp(-0.5((x-mu)/sigma)^2
class RayleighFunctionOne implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[1];
        if(x[0]>=0.0D){
            double arg = x[0]/p[0];
            y = (yScale/p[0])*arg*Math.exp(-0.5D*Math.pow(arg,2));
        }
        return y;
    }
}


// Class to evaluate the semi-linearised Rayleigh function
// log(1/(1-Cumulative y) = 0.5*(x/sigma)^2
class RayleighFunctionTwo implements RegressionFunction{

    public double function(double[] p, double[] x){
        double y = 0.5D*Math.pow(x[0]/p[0],2);
        return y;
    }
}

// class to evaluate a simple exponential function
class ExponentialSimpleFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[1];
        double y = yScale*Math.exp(p[0]*x[0]);
        return y;
     }
}

// class to evaluate multiple exponentials function
class ExponentialMultipleFunction implements RegressionFunction{

    public int nExps = 0;

    public double function(double[] p, double[] x){
        double y = 0;
        for(int i=0; i<nExps; i+=2){
            y += p[i]*Math.exp(p[i+1]*x[0]);
        }
        return y;
     }
}

// class to evaluate 1 - exponential function
class OneMinusExponentialFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[0];
        double y = yScale*(1 - Math.exp(p[1]*x[0]));
        return y;
     }
}

// class to evaluate a exponential distribution function
class ExponentialFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0; // set to 0 -> Two Parameter Exponential
                            // reset to 1 -> One Parameter Exponential
                            // reset to 2 -> Standard Exponential

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 0: if(x[0]>=p[0]){
                        if(scaleOption)yScale = p[2];
                        double arg = (x[0] - p[0])/p[1];
                        y = (yScale/p[1])*Math.exp(-arg);
                    }
                    break;
            case 1: if(x[0]>=0.0D){
                        double arg = x[0]/p[0];
                        if(scaleOption)yScale = p[1];
                        y = (yScale/p[0])*Math.exp(-arg);
                    }
                    break;
            case 2: if(x[0]>=0.0D){
                        double arg = x[0];
                        if(scaleOption)yScale = p[0];
                        y = yScale*Math.exp(-arg);
                    }
                    break;
        }
        return y;
    }
}

// class to evaluate a Pareto scaled pdf
class ParetoFunctionOne implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0;    // set to 3 -> Shifted Pareto
                                // set to 2 -> Two Parameter Pareto
                                // set to 1 -> One Parameter Pareto

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 3: if(x[0]>=p[1]+p[2]){
                        if(scaleOption)yScale = p[3];
                        y = yScale*p[0]*Math.pow(p[1],p[0])/Math.pow((x[0]-p[2]),p[0]+1.0D);
                    }
                    break;
            case 2: if(x[0]>=p[1]){
                        if(scaleOption)yScale = p[2];
                        y = yScale*p[0]*Math.pow(p[1],p[0])/Math.pow(x[0],p[0]+1.0D);
                    }
                    break;
            case 1: if(x[0]>=1.0D){
                        double arg = x[0]/p[0];
                        if(scaleOption)yScale = p[1];
                        y = yScale*p[0]/Math.pow(x[0],p[0]+1.0D);
                    }
                    break;
        }
        return y;
    }
}

// class to evaluate a Pareto cdf
class ParetoFunctionTwo implements RegressionFunction{

    public int typeFlag = 0;    // set to 3 -> Shifted Pareto
                                // set to 2 -> Two Parameter Pareto
                                // set to 1 -> One Parameter Pareto

    public double function(double[] p, double[] x){
        double y = 0.0D;
        switch(typeFlag){
            case 3: if(x[0]>=p[1]+p[2]){
                        y = 1.0D - Math.pow(p[1]/(x[0]-p[2]),p[0]);
                    }
                    break;
            case 2: if(x[0]>=p[1]){
                        y = 1.0D - Math.pow(p[1]/x[0],p[0]);
                    }
                    break;
            case 1: if(x[0]>=1.0D){
                        y = 1.0D - Math.pow(1.0D/x[0],p[0]);
                    }
                    break;
         }
        return y;
    }
}

// class to evaluate a Sigmoidal threshold function
class SigmoidThresholdFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double y = yScale/(1.0D + Math.exp(-p[0]*(x[0] - p[1])));
        return y;
     }
}

// class to evaluate a Rectangular Hyberbola
class RectangularHyperbolaFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[1];
        double y = yScale*x[0]/(p[0] + x[0]);
        return y;
     }

}

// class to evaluate a Shifted Rectangular Hyberbola
class ShiftedRectangularHyperbolaFunction implements RegressionFunction{

    public double function(double[] p, double[] x){
        double y = p[2]*x[0]/(p[0] + x[0]) + p[1];
        return y;
     }
}

// class to evaluate a scaled Heaviside Step Function
class StepFunctionFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[1];
        double y = 0.0D;
        if(x[0]>p[0])y = yScale;
        return y;
     }
}

// class to evaluate a Hill or Sips sigmoidal function
class SigmoidHillSipsFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;

    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double xterm = Math.pow(x[0],p[1]);
        double y = yScale*xterm/(Math.pow(p[0], p[1]) + xterm);
        return y;
     }
}

// Class to evaluate the Logistic probability function y = yscale*exp(-(x-mu)/beta)/(beta*(1 + exp(-(x-mu)/beta))^2.
class LogisticFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public double function(double[] p, double[] x){
        double yScale = scaleFactor;
        if(scaleOption)yScale = p[2];
        double y = yScale*Fmath.square(Fmath.sech((x[0] - p[0])/(2.0D*p[1])))/(4.0D*p[1]);
        return y;
    }
}

// class to evaluate a Beta scaled pdf
class BetaFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0;    // set to 0 -> Beta Distibution - [0, 1] interval
                                // set to 1 -> Beta Distibution - [min, max] interval

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 0: if(scaleOption)yScale = p[2];
                    y = yScale*Math.pow(x[0],p[0]-1.0D)*Math.pow(1.0D-x[0],p[1]-1.0D)/Stat.betaFunction(p[0],p[1]);
                    break;
            case 1: if(scaleOption)yScale = p[4];
                    y = yScale*Math.pow(x[0]-p[2],p[0]-1.0D)*Math.pow(p[3]-x[0],p[1]-1.0D)/Stat.betaFunction(p[0],p[1]);
                    y = y/Math.pow(p[3]-p[2],p[0]+p[1]-1.0D);
                    break;
        }
        return y;
    }
}

// class to evaluate a Gamma scaled pdf
class GammaFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public int typeFlag = 0;    // set to 0 -> Three parameter Gamma Distribution
                                // set to 1 -> Standard Gamma Distribution

    public double function(double[] p, double[] x){
        double y = 0.0D;
        boolean test = false;
        double yScale = scaleFactor;

        switch(typeFlag){
            case 0: if(scaleOption)yScale = p[3];
                    double xTerm = (x[0] - p[0])/p[1];
                    y = yScale*Math.pow(xTerm,p[2]-1.0D)*Math.exp(-xTerm)/(p[1]*Stat.gammaFunction(p[2]));
                    break;
            case 1: if(scaleOption)yScale = p[1];
                    y = yScale*Math.pow(x[0],p[0]-1.0D)*Math.exp(-x[0])/Stat.gammaFunction(p[0]);
                    break;
        }
        return y;
    }
}

// class to evaluate a Erlang scaled pdf
// rate parameter is fixed
class ErlangFunction implements RegressionFunction{
    public boolean scaleOption = true;
    public double scaleFactor = 1.0D;
    public double kay = 1.0D;   // rate parameter

    public double function(double[] p, double[] x){
        boolean test = false;
        double yScale = scaleFactor;

        if(scaleOption)yScale = p[1];

        double y = kay*Math.log(p[0]) + (kay - 1)*Math.log(x[0]) - x[0]*p[0] - Fmath.logFactorial(kay - 1);
        y = yScale*Math.exp(y);

        return y;
    }
}

// class to evaluate a EC50 function (four point logistic)
class EC50Function implements RegressionFunction{

    public double function(double[] p, double[] x){
        double y = p[0] + (p[1] - p[0])/(1.0D + Math.pow(x[0]/p[2], p[3]));
        return y;
    }
}

// class to evaluate a EC50 function (four point logistic)
// top and bottom fixed
class EC50FixedFunction implements RegressionFunction{
    private double bottom = 0.0;
    private double top = 0.0;

    public double function(double[] p, double[] x){
        double y = top + (bottom - top)/(1.0D + Math.pow(x[0]/p[0], p[1]));
        return y;
    }

    public void setBottom(double bottom){
        this.bottom = bottom;
    }

    public void setTop(double top){
        this.top = top;
    }

}

// class to evaluate a five parameter logistic function
class Logistic5Function implements RegressionFunction{

    public double function(double[] p, double[] x){
        double y = p[0] + (p[1] - p[0])/Math.pow((1.0D + Math.pow(x[0]/p[2], p[3])),p[4]);
        return y;
    }
}

// class to evaluate a five parameter logistic function
// top and bottom fixed
class Logistic5FixedFunction implements RegressionFunction{
    private double bottom = 0.0;
    private double top = 0.0;

    public double function(double[] p, double[] x){
        double y = top + (bottom - top)/Math.pow((1.0D + Math.pow(x[0]/p[0], p[1])),p[2]);
        return y;
    }

    public void setBottom(double bottom){
        this.bottom = bottom;
    }

    public void setTop(double top){
        this.top = top;
    }

}

// class to evaluate a Non-Integer Polynomial function
// y = a[0] + a[1].x + a[2].x^a[n+1] +  a[3].x^a[n+2] + . . . + a[n].x^a[2n-1]
// nTerms = n+1;
class NonIntegerPolyFunction implements RegressionFunction{

    private int nTerms = 0;

    public double function(double[] p, double[] x){
        double y = p[0];
        for(int i=1; i<this.nTerms; i++){
            y += p[i]*Math.pow(x[0], p[this.nTerms+i-1]);
        }
        return y;
    }

    public void setNterms(int nTerms){
        this.nTerms = nTerms;
    }
}

