/*
*   Class   ProbabilityPlot
*
*   USAGE:  Probability Plots
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:   29-30 September 2008, 1-5 October 2008, 13-24 October 2009, 2 November 2010, 8 December 2010
*           7 December 2011, 4 January 2012
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ProbabilityPlot.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2009 Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
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
import java.math.*;

import flanagan.math.*;
import flanagan.plot.PlotGraph;
import flanagan.interpolation.CubicSpline;
import flanagan.io.PrintToScreen;

public class ProbabilityPlot{

        // INSTANCE VARIABLES
        private double[] array = null;                          // array of data
        private Stat arrayAsStat = null;                        // array of data as Stat
        private double[] sortedData = null;                     // data sorted into ascending order

        private double[] weights = null;                        // weights
        private boolean weighted = false;                       // = true if weighted regression to be performed

        private double mean = Double.NaN;                       // array mean
        private double standardDeviation = Double.NaN;          // array standard deviation
        private double minimum = Double.NaN;                    // array minimum
        private double maximum = Double.NaN;                    // array maximum
        private double range = Double.NaN;                      // array range
        private double halfWidth = Double.NaN;                  // rough estimate of peak width at half peak height
        private double peakPoint = Double.NaN;                  // rough estimate of peak position

        private int numberOfDataPoints = 0;                     // number of data points

        private double dataOffset = 0.0;                        // data offset if data shifted
        private boolean dataShifted = false;                    // = true if data offset

        private double[] initialEstimates = null;               // initial estimates used in last call to a probability plot method

        private int lastMethod = 0;                             // Last probability plot method called
                                                                //  0   Gaussian
                                                                //  1   Weibull (three parameter)
                                                                //  2   Exponential
                                                                //  3   Rayleigh
                                                                //  4   Pareto
                                                                //  5   Gumbel (minimum order statistic)
                                                                //  6   Gumbel (maximum order statistic)
                                                                //  7   Frechet
                                                                //  8   Logistic
                                                                //  9   Lorentzian  // TO BE ADDED
                                                                //  10  Log-Normal (three parameter) // TO BE ADDED
                                                                //  11  Log-Normal (two parameter // TO BE ADDED
                                                                //  12  Weibull (two parameter)
                                                                //  13  Weibull (standard)
                                                                //  14  Standard Gaussian
                                                                //  15  F-distribution

        private boolean supressPlot = false;                    // set to true to supress the display of the plot 
        private int gaussianNumberOfParameters = 2;             // number of Gaussian parameters
        private double[] gaussianOrderMedians = null;           // Gaussian order statistic medians
        private double[] gaussianParam = null;                  // Gaussian parameters obtained by the minimization procedure
        private double[] gaussianParamErrors = null;            // estimates of the errors of the Gaussian parameters obtained by the minimization procedure
        private double gaussianSumOfSquares = Double.NaN;       // sum of squares at Gaussian minimum
        private double gaussianUnweightedSumOfSquares = Double.NaN;  // unweighted sum of squares at Gaussian minimum
        private double[] gaussianLine = null;                   // Gaussian probability plot gradient and intercept
        private double[] gaussianLineErrors = null;             // estimated errors of the Gaussian probability plot gradient and intercept
        private double gaussianCorrCoeff = Double.NaN;          // Gaussian correlation coefficient of the probability plot
        private boolean gaussianDone = false;                   // = true after Gaussian probability plot drawn

        private int gaussianStandardNumberOfParameters = 0;     // number of Standard Gauss parameters
        private double[] gaussianStandardOrderMedians = null;   // Standard Gauss order statistic medians
        private double gaussianStandardSumOfSquares = Double.NaN;   // sum of squares at Standard Gauss minimum
        private double[] gaussianStandardLine = null;           // Standard Gauss probability plot gradient and intercept
        private double[] gaussianStandardLineErrors = null;     // estimated errors of the Standard Gauss probability plot gradient and intercept
        private double gaussianStandardCorrCoeff = Double.NaN;  // Standard Gauss correlation coefficient of the probability plot
        private boolean gaussianStandardDone = false;           // = true after Standard Gauss probability plot drawn

        private int exponentialNumberOfParameters = 2;          // number of Exponential parameters
        private double[] exponentialOrderMedians = null;        // Exponential order statistic medians
        private double[] exponentialParam = null;               // Exponential parameters obtained by the minimization procedure
        private double[] exponentialParamErrors = null;         // estimates of the errors of the Exponential parameters obtained by the minimization procedure
        private double exponentialSumOfSquares = Double.NaN;    // sum of squares at Exponential minimum
        private double[] exponentialLine = null;                // Exponential probability plot gradient and intercept
        private double[] exponentialLineErrors = null;          // estimated errors of the Exponential probability plot gradient and intercept
        private double exponentialCorrCoeff = Double.NaN;       // Exponential correlation coefficient of the probability plot
        private boolean exponentialDone = false;                // = true after Exponential probability plot drawn

        private int fDistributionNumberOfParameters = 0;        // number of F-distribution parameters
        private double[] fDistributionOrderMedians = null;      // F-distribution order statistic medians
        private double fDistributionSumOfSquares = Double.NaN;  // sum of squares at F-distribution minimum
        private double[] fDistributionLine = null;              // F-distribution probability plot gradient and intercept
        private double[] fDistributionLineErrors = null;        // estimated errors of the F-distribution probability plot gradient and intercept
        private double fDistributionCorrCoeff = Double.NaN;     // F-distribution correlation coefficient of the probability plot
        private boolean fDistributionDone = false;              // = true after F-distribution probability plot drawn

        private int frechetNumberOfParameters = 3;              // number of Frechet parameters
        private double[] frechetOrderMedians = null;            // Frechet order statistic medians
        private double[] frechetParam = null;                   // Frechet parameters obtained by the minimization procedure
        private double[] frechetParamErrors = null;             // estimates of the errors of the Frechet parameters obtained by the minimization procedure
        private double frechetSumOfSquares = Double.NaN;        // sum of squares at Frechet minimum
        private double[] frechetLine = null;                    // Frechet probability plot gradient and intercept
        private double[] frechetLineErrors = null;              // estimated errors of the Frechet probability plot gradient and intercept
        private double frechetCorrCoeff = Double.NaN;           // Frechet correlation coefficient of the probability plot
        private boolean frechetDone = false;                    // = true after Frechet probability plot drawn

        private int gumbelMinNumberOfParameters = 3;            // number of Gumbel (minimum order statistic) parameters
        private double[] gumbelMinOrderMedians = null;          // Gumbel (minimum order statistic) order statistic medians
        private double[] gumbelMinParam = null;                 // Gumbel (minimum order statistic) parameters obtained by the minimization procedure
        private double[] gumbelMinParamErrors = null;           // estimates of the errors of the Gumbel (minimum order statistic) parameters obtained by the minimization procedure
        private double gumbelMinSumOfSquares = Double.NaN;      // sum of squares at Gumbel (minimum order statistic) minimum
        private double[] gumbelMinLine = null;                  // Gumbel (minimum order statistic) probability plot gradient and intercept
        private double[] gumbelMinLineErrors = null;            // estimated errors of the Gumbel (minimum order statistic) probability plot gradient and intercept
        private double gumbelMinCorrCoeff = Double.NaN;         // Gumbel (minimum order statistic) correlation coefficient of the probability plot
        private boolean gumbelMinDone = false;                  // = true after Gumbel (minimum order statistic) probability plot drawn

        private int gumbelMaxNumberOfParameters = 3;            // number of Gumbel (maximum order statistic) parameters
        private double[] gumbelMaxOrderMedians = null;          // Gumbel (maximum order statistic) order statistic medians
        private double[] gumbelMaxParam = null;                 // Gumbel (maximum order statistic) parameters obtained by the maximization procedure
        private double[] gumbelMaxParamErrors = null;           // estimates of the errors of the Gumbel (maximum order statistic) parameters obtained by the maximization procedure
        private double gumbelMaxSumOfSquares = Double.NaN;      // sum of squares at Gumbel (maximum order statistic) maximum
        private double[] gumbelMaxLine = null;                  // Gumbel (maximum order statistic) probability plot gradient and intercept
        private double[] gumbelMaxLineErrors = null;            // estimated errors of the Gumbel (maximum order statistic) probability plot gradient and intercept
        private double gumbelMaxCorrCoeff = Double.NaN;         // Gumbel (maximum order statistic) correlation coefficient of the probability plot
        private boolean gumbelMaxDone = false;                  // = true after Gumbel (maximum order statistic) probability plot drawn

        private int logisticNumberOfParameters = 3;             // number of Logistic parameters
        private double[] logisticOrderMedians = null;           // Logistic order statistic medians
        private double[] logisticParam = null;                  // Logistic parameters obtained by the minimization procedure
        private double[] logisticParamErrors = null;            // estimates of the errors of the Logistic parameters obtained by the minimization procedure
        private double logisticSumOfSquares = Double.NaN;       // sum of squares at Logistic minimum
        private double[] logisticLine = null;                   // Logistic probability plot gradient and intercept
        private double[] logisticLineErrors = null;             // estimated errors of the Logistic probability plot gradient and intercept
        private double logisticCorrCoeff = Double.NaN;          // Logistic correlation coefficient of the probability plot
        private boolean logisticDone = false;                   // = true after Logistic probability plot drawn

        private int paretoNumberOfParameters = 2;               // number of Pareto parameters
        private double[] paretoOrderMedians = null;             // Pareto order statistic medians
        private double[] paretoParam = null;                    // Pareto parameters obtained by the minimization procedure
        private double[] paretoParamErrors = null;              // estimates of the errors of the Pareto parameters obtained by the minimization procedure
        private double paretoSumOfSquares = Double.NaN;         // sum of squares at Pareto minimum
        private double[] paretoLine = null;                     // Pareto probability plot gradient and intercept
        private double[] paretoLineErrors = null;               // estimated errors of the Pareto probability plot gradient and intercept
        private double paretoCorrCoeff = Double.NaN;            // Pareto correlation coefficient of the probability plot
        private boolean paretoDone = false;                     // = true after Pareto probability plot drawn

        private int rayleighNumberOfParameters = 2;             // number of Rayleigh parameters
        private double[] rayleighOrderMedians = null;           // Rayleigh order statistic medians
        private double[] rayleighParam = null;                  // Rayleigh parameters obtained by the minimization procedure
        private double[] rayleighParamErrors = null;            // estimates of the errors of the Rayleigh parameters obtained by the minimization procedure
        private double rayleighSumOfSquares = Double.NaN;       // sum of squares at Rayleigh minimum
        private double[] rayleighLine = null;                   // Rayleigh probability plot gradient and intercept
        private double[] rayleighLineErrors = null;             // estimated errors of the Rayleigh probability plot gradient and intercept
        private double rayleighCorrCoeff = Double.NaN;          // Rayleigh correlation coefficient of the probability plot
        private boolean rayleighDone = false;                   // = true after Rayleigh probability plot drawn

        private int weibullNumberOfParameters = 3;              // number of Three Parameter Weibull parameters
        private double[] weibullOrderMedians = null;            // Three Parameter Weibull order statistic medians
        private double[] weibullParam = null;                   // Three Parameter Weibull parameters obtained by the minimization procedure
        private double[] weibullParamErrors = null;             // estimates of the errors of the Three Parameter Weibull parameters obtained by the minimization procedure
        private double weibullSumOfSquares = Double.NaN;        // sum of squares at Three Parameter Weibull minimum
        private double[] weibullLine = null;                    // Three Parameter Weibull probability plot gradient and intercept
        private double[] weibullLineErrors = null;              // estimated errors of the Three Parameter Weibull probability plot gradient and intercept
        private double weibullCorrCoeff = Double.NaN;           // Three Parameter Weibull correlation coefficient of the probability plot
        private boolean weibullDone = false;                    // = true after Three Parameter Weibull probability plot drawn

        private int weibullTwoParNumberOfParameters = 2;        // number of Two Parameter Weibull parameters
        private double[] weibullTwoParOrderMedians = null;      // Two Parameter Weibull order statistic medians
        private double[] weibullTwoParParam = null;             // Two Parameter Weibull parameters obtained by the minimization procedure
        private double[] weibullTwoParParamErrors = null;       // estimates of the errors of the Two Parameter Weibull parameters obtained by the minimization procedure
        private double weibullTwoParSumOfSquares = Double.NaN;  // sum of squares at Two Parameter Weibull minimum
        private double[] weibullTwoParLine = null;              // Two Parameter Weibull probability plot gradient and intercept
        private double[] weibullTwoParLineErrors = null;        // estimated errors of the Two Parameter Weibull probability plot gradient and intercept
        private double weibullTwoParCorrCoeff = Double.NaN;     // Two Parameter Weibull correlation coefficient of the probability plot
        private boolean weibullTwoParDone = false;              // = true after Two Parameter Weibull probability plot drawn

        private int weibullStandardNumberOfParameters = 1;      // number of Standard Weibull parameters
        private double[] weibullStandardOrderMedians = null;    // Standard Weibull order statistic medians
        private double[] weibullStandardParam = null;           // Standard Weibull parameters obtained by the minimization procedure
        private double[] weibullStandardParamErrors = null;     // estimates of the errors of the Standard Weibull parameters obtained by the minimization procedure
        private double weibullStandardSumOfSquares = Double.NaN;// sum of squares at Standard Weibull minimum
        private double[] weibullStandardLine = null;            // Standard Weibull probability plot gradient and intercept
        private double[] weibullStandardLineErrors = null;      // estimated errors of the Standard Weibull probability plot gradient and intercept
        private double weibullStandardCorrCoeff = Double.NaN;   // Standard Weibull correlation coefficient of the probability plot
        private boolean weibullStandardDone = false;            // = true after Standard Weibull probability plot drawn
        private boolean probPlotDone = false;                   // = true after any probability plot drawn

        private double delta = 1e-3;                            // step fraction in numerical differentiation

        private boolean nFactorOptionI = false;                 // = true  variance, covariance and standard deviation denominator = n                                                                                // = false varaiance, covariance and standard deviation denominator = n-1
        private boolean nFactorReset = false;                   // = true when instance method resetting the denominator is called



        // CONSTRUCTORS
        public ProbabilityPlot(double[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Double[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(float[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Float[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(long[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Long[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(int[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Integer[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(short[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Short[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(byte[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Byte[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(BigDecimal[] xx){
             this.arrayAsStat = new Stat(xx);
             this.initialize();
        }

        public ProbabilityPlot(BigInteger[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Object[] xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Vector<Object> xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(ArrayList<Object> xx){
             this.arrayAsStat = new Stat(xx);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(ArrayMaths xx){
             double[] aa = xx.array();
             this.arrayAsStat = new Stat(aa);
             this.array = this.arrayAsStat.array();
             this.initialize();
        }

        public ProbabilityPlot(Stat xx){
             this.arrayAsStat = xx;
             this.array = this.arrayAsStat.array();
             this.initialize();
        }


        // INITIALIZATIONS
        private void initialize(){
            this.numberOfDataPoints = this.array.length;
            ArrayMaths sorted = arrayAsStat.sort();
            this.sortedData = sorted.array();
            this.mean = arrayAsStat.mean();
            this.standardDeviation = arrayAsStat.standardDeviation();
            this.minimum = arrayAsStat.minimum();
            this.maximum = arrayAsStat.maximum();
            this.range = this.maximum - this.minimum;
            this.weights = new double[this.numberOfDataPoints];
            for(int i=0; i<this.numberOfDataPoints; i++)weights[i] = 1.0;
        }

        // SUPRESS DISPLAY OF PLOT
        public void supressDisplay(){
            this.supressPlot = true;
        }
        
        public void restoreDisplay(){
            this.supressPlot = false;
        }
        
        // WEIGHTING OPTION
        // Set weighting option to weighted regression
        public void weightedRegression(){
            this.weighted = true;
        }

        // Set weighting option to unweighted regression
        public void unweightedRegression(){
            this.weighted = false;
        }

        // Get weighting option - String output
        public String getWeightingOption(){
            if(this.weighted){
                return "Weighted Regression";
            }
            else{
                return "Unweighted Regression";
            }
        }

        // Get weighting option - boolean output
        // returns true for weighted regression
        // returns false for unweighted regression
        public boolean getBooleanWeightingOption(){
            return this.weighted;
        }

        // DATA SHIFT
        // Tests if any negative or zero data points present and shifts data to positive domain
        // Called by probability plot methods requiring all positive, non-zero data
        private void negativeAndNonZeroDataShift(){
            this.dataShifted = false;
            if(this.minimum<=0){
                this.dataOffset = this.range*0.01 - this.minimum;
                this.dataShift();
            }
        }


        // Tests if any negative data points present and shifts data to positive domain
        // Called by probability plot methods requiring all positive, non-zero data
        private void negativeDataShift(){
            this.dataShifted = false;
            if(this.minimum<0.0){
                this.dataOffset = -this.minimum;
                this.dataShift();
            }
        }

        // Shifts data
        private void dataShift(){
            for(int i=0; i<this.numberOfDataPoints; i++){
                this.sortedData[i] += this.dataOffset;
            }
            this.minimum += this.dataOffset;
            this.maximum += this.dataOffset;
            this.mean += this.dataOffset;
            this.dataShifted = true;
        }

        // Returns data offset
        public double getdataOffset(){
            return this.dataOffset;
        }



        // rough estimate of the half-height peak width
        private double peakWidth(){

            this.halfWidth = 0.0;
            double[] interpData = null;
            int nInterp = 10000;

            // Interpolate to increase number of points to allow binning
            if(this.numberOfDataPoints>=1000){
                interpData = this.sortedData;
                nInterp = this.numberOfDataPoints;
            }
            else{
                double[] dataX = new double[this.numberOfDataPoints];
                for(int i=0; i<this.numberOfDataPoints; i++)dataX[i]=i;
                double incrI = ((double)(this.numberOfDataPoints-1))/(nInterp-1);

                interpData = new double[nInterp];
                CubicSpline cs = new CubicSpline(dataX, this.sortedData);
                double interp = 0.0;
                for(int i=0; i<nInterp-1; i++){
                    interpData[i]=cs.interpolate(interp);
                    interp += incrI;

                }
                interpData[nInterp-1] = (double)(this.numberOfDataPoints-1);
            }

            // Bin the data
            int nBins = 100;
            double[] binnedData = new double[nBins];
            double[] bins = new double[nBins];
            double binWidth = this.range/nBins;
            double binLower = this.minimum;
            double binUpper = binLower + binWidth;
            int counter = 0;
            for(int i=0; i<nBins; i++){
                bins[i] = (binUpper + binLower)/2.0;
                binnedData[i] = 0.0;
                boolean test = true;
                if(counter>=nInterp)test = false;
                while(test){
                    if(interpData[counter]<binUpper){
                        binnedData[i] += 1.0;
                    }
                    else{
                        test = false;
                    }
                    counter++;
                    if(counter>=nInterp)test = false;
                }
                binLower = binUpper;
                binUpper = binLower + binWidth;
            }
            if(counter<nInterp)binnedData[nBins-1] += (double)(nInterp-counter);

            // Identify peak
            ArrayMaths am = new ArrayMaths(binnedData);
            double maxI = am.maximum();
            int maxIindex = am.maximumIndex();
            this.peakPoint = bins[maxIindex];
            double halfHeight = maxI/2.0;
            double widthLower = 0.0;
            boolean lowerCheck = false;
            double widthUpper = 0.0;
            boolean upperCheck = false;

            // lower limit
            if(binnedData[0]==halfHeight){
                widthLower = bins[0];
                lowerCheck = true;
            }
            else{
                if(binnedData[0]<halfHeight){
                    if(maxIindex>=2){
                        double[] interpLy = new double[maxIindex+1];
                        double[] interpLx = new double[maxIindex+1];
                        for(int i=0; i<=maxIindex; i++){
                            interpLy[i] = binnedData[i];
                            interpLx[i] = bins[i];
                        }
                        CubicSpline csl = new CubicSpline(interpLx, interpLy);
                        double[] tempx = new double[100];
                        double[] tempy = new double[100];
                        double incr = (interpLx[maxIindex]-interpLx[0])/99;
                        double intr = interpLx[0];
                        for(int i=0; i<99; i++){
                            tempx[i] = intr;
                            tempy[i] = csl.interpolate(intr);
                            intr += incr;
                        }
                        tempy[99] = interpLy[maxIindex];
                        tempx[99] = interpLx[maxIindex];
                        boolean testt = true;
                        int ii = 0;
                        while(testt){
                            if(halfHeight<=tempy[ii]){
                                if(ii==0){
                                    widthLower = tempx[0];
                                    testt = false;
                                    lowerCheck = true;
                                }else{
                                    if(ii==99){
                                        widthLower = tempx[99];
                                        testt = false;
                                        lowerCheck = true;
                                    }
                                    else{
                                        widthLower = (tempx[ii] + tempx[ii-1])/2.0;
                                        testt = false;
                                        lowerCheck = true;
                                    }
                                }
                            }
                            ii++;
                            if(ii>=100)testt = false;
                        }
                    }
                    else{
                        if(maxIindex==2){
                            if(binnedData[1]>=halfHeight){
                                widthLower = bins[0] + (bins[1] - bins[0])*(halfHeight - binnedData[0])/(binnedData[1] - binnedData[0]);
                                lowerCheck = true;
                            }
                            else{
                                widthLower = bins[1] + (bins[2] - bins[1])*(halfHeight - binnedData[1])/(binnedData[2] - binnedData[1]);
                                lowerCheck = true;
                            }
                        }
                        else{
                            widthLower = bins[0] + (bins[1] - bins[0])*(halfHeight - binnedData[0])/(binnedData[1] - binnedData[0]);
                            lowerCheck = true;
                         }
                    }
                }
                else{
                    if(maxIindex>2){
                        if((binnedData[maxIindex]-binnedData[0])>halfHeight*0.5){
                            widthLower = bins[0] + (bins[1] - bins[0])*(halfHeight - binnedData[0])/(binnedData[1] - binnedData[0]);
                            lowerCheck = true;
                        }
                    }
                }
            }

            // upper limit
            int nTop = nBins - 1;
            int nDif = nBins - maxIindex;
            if(binnedData[nTop]==halfHeight){
                widthUpper = bins[nTop];
                upperCheck = true;
            }
            else{
                if(binnedData[nTop]<halfHeight){
                    if(nDif>=3){
                        double[] interpLy = new double[nDif];
                        double[] interpLx = new double[nDif];
                        int ii = 0;
                        for(int i=maxIindex; i<nBins; i++){
                            interpLy[ii] = binnedData[i];
                            interpLx[ii] = bins[i];
                            ii++;
                        }
                        CubicSpline csl = new CubicSpline(interpLx, interpLy);
                        double[] tempx = new double[100];
                        double[] tempy = new double[100];
                        double incr = (interpLx[nDif-1]-interpLx[0])/99;
                        double intr = interpLx[0];
                        for(int i=0; i<99; i++){
                            tempx[i] = intr;
                            tempy[i] = csl.interpolate(intr);
                            intr += incr;
                        }
                        tempy[99] = interpLy[nDif-1];
                        tempx[99] = interpLx[nDif-1];
                        boolean testt = true;
                        ii = 0;
                        while(testt){
                            if(halfHeight<=tempy[ii]){
                                if(ii==0){
                                    widthUpper = tempx[0];
                                    testt = false;
                                    upperCheck = true;
                                }else{
                                    if(ii==99){
                                        widthUpper = tempx[99];
                                        testt = false;
                                        upperCheck = true;
                                    }
                                    else{
                                        widthUpper = (tempx[ii] + tempx[ii-1])/2.0;
                                        testt = false;
                                        upperCheck = true;
                                    }
                                }
                            }
                            ii++;
                            if(ii>=100)testt = false;
                        }
                    }
                    else{
                        if(nDif==2){
                            if(binnedData[nTop-1]>=halfHeight){
                                widthUpper = bins[nTop-1] + (bins[nTop] - bins[nTop-1])*(halfHeight - binnedData[nTop-1])/(binnedData[nTop] - binnedData[nTop-1]);
                                upperCheck = true;
                            }
                            else{
                                widthUpper = bins[nTop-2] + (bins[nTop-1] - bins[nTop-2])*(halfHeight - binnedData[nTop-2])/(binnedData[nTop-1] - binnedData[nTop-2]);
                                upperCheck = true;
                            }
                        }
                        else{
                            widthUpper = bins[nTop-1] + (bins[nTop] - bins[nTop-1])*(halfHeight - binnedData[nTop-1])/(binnedData[nTop] - binnedData[nTop-1]);
                            upperCheck = true;
                        }
                    }
                }
                else{
                    if(nDif>2){
                        if((binnedData[maxIindex]-binnedData[nTop])>halfHeight*0.5){
                            widthUpper = bins[nTop-1] + (bins[nTop] - bins[nTop-1])*(halfHeight - binnedData[nTop-1])/(binnedData[nTop] - binnedData[nTop-1]);
                            upperCheck = true;
                        }
                    }
                }
            }

            // combine lower and upper half widths
            if(lowerCheck){
                if(upperCheck){
                    this.halfWidth = widthUpper - widthLower;
                }
                else{
                    this.halfWidth = (this.peakPoint - widthLower)*1.3;
                }
            }
            else{
                if(upperCheck){
                    this.halfWidth = (widthUpper - this.peakPoint)*1.3;
                }
                else{
                    System.out.println("Half height width could not be calculated - half range returned");
                    this.halfWidth = this.range/2.0;
                }

            }
            return this.halfWidth;
        }

        // GAUSSIAN PROBABILITY PLOT
        public void gaussianProbabilityPlot(){
            this.lastMethod = 0;

            // Check for suffient data points
            this.gaussianNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double meanest = this.mean;
            if(this.mean==0)meanest = this.standardDeviation/3.0;
            double[] start = {meanest, this.standardDeviation};
            this.initialEstimates = start;
            double[] step = {0.3*meanest, 0.3*this.standardDeviation};
            double tolerance = 1e-10;

            // Add constraint; sigma>0
            min.addConstraint(1, -1, 0);

            // Create an instance of GaussProbPlotFunc
            GaussProbPlotFunc gppf = new GaussProbPlotFunc();
            gppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu and sigma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(gppf, start, step, tolerance);

            // Get mu and sigma for best correlation coefficient
            this.gaussianParam = min.getBestEstimates();

            // Get mu and sigma errors for best correlation coefficient
            this.gaussianParamErrors = min.getBestEstimatesErrors();

            // Calculate Gaussian order statistic medians
            this.gaussianOrderMedians = Stat.gaussianOrderStatisticMedians(this.gaussianParam[0], this.gaussianParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Gaussian order statistic medians
            Regression reg = new Regression(this.gaussianOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.gaussianLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.gaussianLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.gaussianCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.gaussianOrderMedians;
            data[1] = this.sortedData;

            data[2] = this.gaussianOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.gaussianLine[0] + this.gaussianLine[1]*this.gaussianOrderMedians[i];
            }

            // Get sum of squares
            this.gaussianSumOfSquares = min.getSumOfSquares();

            if(!this.supressPlot){
                // Create instance of PlotGraph
                PlotGraph pg = new PlotGraph(data);
                int[] points = {4, 0};
                pg.setPoint(points);
                int[] lines = {0, 3};
                pg.setLine(lines);
                pg.setXaxisLegend("Gaussian Order Statistic Medians");
                pg.setYaxisLegend("Ordered Data Values");
                pg.setGraphTitle("Gaussian probability plot:   gradient = " + Fmath.truncate(this.gaussianLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.gaussianLine[0], 4) + ",  R = " + Fmath.truncate(this.gaussianCorrCoeff, 4));
                pg.setGraphTitle2("  mu = " + Fmath.truncate(this.gaussianParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.gaussianParam[1], 4));

               // Plot
                pg.plot();
            }
                
            this.gaussianDone = true;
        }

        public void normalProbabilityPlot(){
            this.gaussianProbabilityPlot();
        }

        // Return Gaussian mu
        public double gaussianMu(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParam[0];
        }

        // Return Gaussian mu error
        public double gaussianMuError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParamErrors[0];
        }

        // Return Gaussian sigma
        public double gaussianSigma(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParam[1];
        }

        // Return Gaussian sigma error
        public double gaussianSigmaError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParamErrors[1];
        }

        // Return the Gaussian gradient
        public double gaussianGradient(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLine[1];
        }

        // Return the error of the Gaussian gradient
        public double gaussianGradientError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLineErrors[1];
        }

        // Return the Gaussian intercept
        public double gaussianIntercept(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLine[0];
        }

        // Return the error of the Gaussian intercept
        public double gaussianInterceptError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLineErrors[0];
        }

        // Return the Gaussian correlation coefficient
        public double gaussianCorrelationCoefficient(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianCorrCoeff;
        }

        // Return the sum of squares at the Gaussian minimum
        public double gaussianSumOfSquares(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianSumOfSquares;
        }

        // Return the unweighted sum of squares at the Gaussian minimum
        public double gaussianUnweightedSumOfSquares(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianUnweightedSumOfSquares;
        }


        // Return Gaussian order statistic medians
        public double[] gaussianOrderStatisticMedians(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianOrderMedians;
        }


        public double normalMu(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParam[0];
        }

        // Return Gaussian mu error
        public double normalMuError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParamErrors[0];
        }

        // Return Gaussian sigma
        public double normalSigma(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParam[1];
        }

        // Return Gaussian sigma error
        public double normalSigmaError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianParamErrors[1];
        }

        // Return the Gaussian gradient
        public double normalGradient(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLine[1];
        }

        // Return the error of the Gaussian gradient
        public double normalGradientError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLineErrors[1];
        }

        // Return the Gaussian intercept
        public double normalIntercept(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLine[0];
        }

        // Return the error of the Gaussian intercept
        public double normalInterceptError(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianLineErrors[0];
        }

        // Return the Gaussian correlation coefficient
        public double normalCorrelationCoefficient(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianCorrCoeff;
        }

        // Return the sum of squares at the Gaussian minimum
        public double normalSumOfSquares(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianSumOfSquares;
        }

        // Return Gaussian order statistic medians
        public double[] normalOrderStatisticMedians(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianOrderMedians;
        }



        // STANDARD GAUSSIAN PROBABILITY PLOT
        public void gaussianStandardProbabilityPlot(){
            this.lastMethod = 14;

            // Check for suffient data points
            this.gaussianStandardNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Calculate Standard Gaussian order statistic medians
            this.gaussianStandardOrderMedians = Stat.gaussianOrderStatisticMedians(this.numberOfDataPoints);

            // Regression of the ordered data on the Standard Gaussian order statistic medians
            Regression reg = new Regression(this.gaussianStandardOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.gaussianStandardLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.gaussianStandardLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.gaussianStandardCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.gaussianStandardOrderMedians;
            data[1] = this.sortedData;

            data[2] = this.gaussianStandardOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.gaussianStandardLine[0] + this.gaussianStandardLine[1]*this.gaussianStandardOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Standard Gaussian Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Standard Gaussian probability plot:   gradient = " + Fmath.truncate(this.gaussianStandardLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.gaussianStandardLine[0], 4) + ",  R = " + Fmath.truncate(this.gaussianStandardCorrCoeff, 4));

            // Plot
            pg.plot();

            this.gaussianStandardDone = true;
        }

        public void normalStandardProbabilityPlot(){
            this.gaussianStandardProbabilityPlot();
        }

        // Return the Standard Gaussian gradient
        public double gaussianStandardGradient(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLine[1];
        }

        // Return the error of the Standard Gaussian gradient
        public double gaussianStandardGradientError(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLineErrors[1];
        }

        // Return the Standard Gaussian intercept
        public double gaussianStandardIntercept(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLine[0];
        }

        // Return the error of the Standard Gaussian intercept
        public double gaussianStandardInterceptError(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLineErrors[0];
        }

        // Return the Standard Gaussian correlation coefficient
        public double gaussianStandardCorrelationCoefficient(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardCorrCoeff;
        }

        // Return the sum of squares at the Standard Gaussian minimum
        public double gaussianStandardSumOfSquares(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardSumOfSquares;
        }

        // Return Standard Gaussian order statistic medians
        public double[] gaussianStandardOrderStatisticMedians(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardOrderMedians;
        }


        // Return the Standard Gaussian gradient
        public double normalStandardGradient(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLine[1];
        }

        // Return the error of the Standard Gaussian gradient
        public double normalstandardGradientError(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLineErrors[1];
        }

        // Return the Standard Gaussian intercept
        public double normalStandardInterceptError(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardLineErrors[0];
        }

        // Return the Standard Gaussian correlation coefficient
        public double normalStandardCorrelationCoefficient(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardCorrCoeff;
        }

        // Return the sum of squares at the Standard Gaussian minimum
        public double normalStandardSumOfSquares(){
            if(!this.gaussianStandardDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardSumOfSquares;
        }

        // Return Standard Gaussian order statistic medians
        public double[] normalStandardOrderStatisticMedians(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.gaussianStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gaussianStandardOrderMedians;
        }

        // LOGISTIC PROBABILITY PLOT
        public void logisticProbabilityPlot(){
            this.lastMethod = 8;

            // Check for suffient data points
            this.logisticNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double muest = mean;
            if(muest==0.0)muest = this.standardDeviation/3.0;
            double betaest = this.standardDeviation;
            double[] start = {muest, betaest};
            this.initialEstimates = start;
            double[] step = {0.3*muest, 0.3*betaest};
            double tolerance = 1e-10;

             // Add constraint; beta>0
            min.addConstraint(1, -1, 0);

            // Create an instance of LogisticProbPlotFunc
            LogisticProbPlotFunc lppf = new LogisticProbPlotFunc();
            lppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu and sigma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(lppf, start, step, tolerance);

            // Get mu and beta for best correlation coefficient
            this.logisticParam = min.getBestEstimates();

            // Get mu and beta errors for best correlation coefficient
            this.logisticParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.logisticSumOfSquares = min.getSumOfSquares();

            // Calculate Logistic order statistic medians
            this.logisticOrderMedians = Stat.logisticOrderStatisticMedians(this.logisticParam[0], this.logisticParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Logistic order statistic medians
            Regression reg = new Regression(this.logisticOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.logisticLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.logisticLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.logisticCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.logisticOrderMedians;
            data[1] = this.sortedData;

            data[2] = logisticOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.logisticLine[0] + this.logisticLine[1]*logisticOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Logistic Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Logistic probability plot:   gradient = " + Fmath.truncate(this.logisticLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.logisticLine[0], 4) + ",  R = " + Fmath.truncate(this.logisticCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.logisticParam[0], 4) + ", beta = "  +  Fmath.truncate(this.logisticParam[1], 4));

            // Plot
            pg.plot();

            this.logisticDone = true;
            this.probPlotDone = true;
        }

        // Return Logistic mu
        public double logisticMu(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticParam[0];
        }

        // Return Logistic mu error
        public double logisticMuError(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticParamErrors[0];
        }

        // Return Logistic beta
        public double logisticBeta(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticParam[1];
        }

        // Return Logistic beta error
        public double logisticBetaError(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticParamErrors[1];
        }

        // Return Logistic order statistic medians
        public double[] logisticOrderStatisticMedians(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticOrderMedians;
        }

        // Return the Logistic gradient
        public double logisticGradient(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticLine[1];
        }

        // Return the error of the Logistic gradient
        public double logisticGradientError(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticLineErrors[1];
        }

        // Return the Logistic intercept
        public double logisticIntercept(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticLine[0];
        }

        // Return the error of the Logistic intercept
        public double logisticInterceptError(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticLineErrors[0];
        }

        // Return the Logistic correlation coefficient
        public double logisticCorrelationCoefficient(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            }
            return this.logisticCorrCoeff;
        }

        // Return the sum of squares at the Logistic minimum
        public double logisticSumOfSquares(){
            if(!this.logisticDone){
                this.supressDisplay();
                this.logisticProbabilityPlot();
                this.restoreDisplay();
            };
            return this.logisticSumOfSquares;
        }



        // WEIBULL PROBABILITY PLOT
        //  Three parameter
        public void weibullProbabilityPlot(){
            this.lastMethod = 1;

            // Check for suffient data points
            this.weibullNumberOfParameters = 3;
            if(this.numberOfDataPoints<4)throw new IllegalArgumentException("There must be at least four data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);

            // Calculate initial estimates
            double[] start = new double[3];
            start[0] = this.minimum - 0.1*Math.abs(this.minimum);
            start[1] = this.peakWidth();
            if(start[1]==0)start[1] = this.range*0.1;
            start[2] = 4.0;
            this.initialEstimates = start;
            double[] step = {Math.abs(0.3*start[0]), Math.abs(0.3*start[1]), Math.abs(0.3*start[2])};
            if(step[0]==0)step[0] = this.range*0.01;
            if(step[1]==0)step[1] = this.range*0.01;

            double tolerance = 1e-10;

            // Add constraint; mu<minimum, sigma>0, gamma>0
            min.addConstraint(0, +1, minimum);
            min.addConstraint(1, -1, 0);
            min.addConstraint(2, -1, 0);

            // Create an instance of WeibullProbPlotFunc
            WeibullProbPlotFunc wppf = new WeibullProbPlotFunc();
            wppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu, sigma and gamma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(wppf, Conv.copy(start), step, tolerance);

            // Obtain best estimates or first minimisation
            double[] firstBests = min.getBestEstimates();

            // Get mu and sigma value errors
            double[] firstErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            double ss = min.getSumOfSquares();

            //Calculate new initial estimates
            double[] start2 = new double[this.weibullNumberOfParameters];
            start2[0] = 2.0*firstBests[0] - start[0];
            if(start2[0]>minimum)start2[0] = minimum*(1.0 - Math.abs(minimum)*0.05);
            step[0] = Math.abs(start2[0]*0.1);
            if(step[0]==0)step[0] = this.range*0.01;
            start2[1] = 2.0*firstBests[1] - start[1];
            if(start2[1]<=0.0)start2[1] = Math.abs(2.0*firstBests[1] - 0.98*start[1]);
            step[1] = Math.abs(start2[1]*0.1);
            start2[2] = 2.0*firstBests[2] - start[2];
            if(start2[1]<=0.0)start2[2] = Math.abs(2.0*firstBests[2] - 0.98*start[2]);
            step[2] = Math.abs(start2[2]*0.1);

            min.simplex(wppf, Conv.copy(start2), step, tolerance);

            // Get mu, sigma and gamma for best correlation coefficient
            this.weibullParam = min.getBestEstimates();

            // Get mu and sigma value errors
            this.weibullParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.weibullSumOfSquares = min.getSumOfSquares();

            if(ss<this.weibullSumOfSquares){
                this.weibullParam = firstBests;
                this.weibullParamErrors = firstErrors;
                this.weibullSumOfSquares = ss;
            }

            // Calculate Weibull order statistic medians
            this.weibullOrderMedians = Stat.weibullOrderStatisticMedians(this.weibullParam[0], this.weibullParam[1], this.weibullParam[2], this.numberOfDataPoints);

            // Regression of the ordered data on the Weibull order statistic medians
            Regression reg = new Regression(this.weibullOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.weibullLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.weibullLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.weibullCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.weibullOrderMedians;
            data[1] = this.sortedData;

            data[2] = weibullOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.weibullLine[0] + this.weibullLine[1]*weibullOrderMedians[i];
            }


            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Weibull Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Weibull probability plot:   gradient = " + Fmath.truncate(this.weibullLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.weibullLine[0], 4) + ",  R = " + Fmath.truncate(this.weibullCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.weibullParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.weibullParam[1], 4) + ", gamma = "  +  Fmath.truncate(this.weibullParam[2], 4));

            // Plot
            pg.plot();

            this.weibullDone = true;
            this.probPlotDone = true;
        }

        // WEIBULL PROBABILITY PLOT
        //  Three parameter
        public void weibullThreeParProbabilityPlot(){
            this.weibullProbabilityPlot();
        }

        // Return Weibull mu
        public double weibullMu(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParam[0];
        }

        // Return Weibull mu error
        public double weibullMuError(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParamErrors[0];
        }

        // Return Weibull sigma
        public double weibullSigma(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParam[1];
        }

        // Return Weibull sigma error
        public double weibullSigmaError(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParamErrors[1];
        }

        // Return Weibull gamma
        public double weibullGamma(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParam[2];
        }

        // Return Weibull gamma error
        public double weibullGammaError(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullParamErrors[2];
        }

        // Return Weibull order statistic medians
        public double[] weibullOrderStatisticMedians(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullOrderMedians;
        }

        // Return the Weibull gradient
        public double weibullGradient(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullLine[1];
        }

        // Return the error of the Weibull gradient
        public double weibullGradientError(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullLineErrors[1];
        }

        // Return the Weibull intercept
        public double weibullIntercept(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullLine[0];
        }

        // Return the error of the Weibull intercept
        public double weibullInterceptError(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullLineErrors[0];
        }

        // Return the Weibull correlation coefficient
        public double weibullCorrelationCoefficient(){
            if(!this.weibullDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullCorrCoeff;
        }

        // Return the sum of squares at the Weibull minimum
        public double weibullSumOfSquares(){
            if(!this.gaussianDone){
                this.supressDisplay();
                this.weibullProbabilityPlot();
                this.restoreDisplay();
            };
            return this.weibullSumOfSquares;
        }

        // WEIBULL PROBABILITY PLOT
        //  Two parameter  (mu = 0)
        public void weibullTwoParProbabilityPlot(){
             this.lastMethod = 12;

            // Check for negative x values
            if(this.sortedData[0]<0){
                System.out.println("Method weibullTwoParProbabilityPlot: negative x value found - weibullThreeParProbabilityPlot called");
                this.weibullThreeParProbabilityPlot();
            }

            // Check data for suffient points
            this.weibullTwoParNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);

            // Calculate initial estimates
            double[] start = new double[2];
            start[0] = this.peakWidth();
            start[1] = 4.0;
            this.initialEstimates = start;
            double[] step = {Math.abs(0.3*start[0]), Math.abs(0.3*start[1])};
            if(step[0]==0)step[0] = this.range*0.01;
            double tolerance = 1e-10;

            // Add constraint; sigma>0, gamma>0
            min.addConstraint(0, -1, 0);
            min.addConstraint(1, -1, 0);

            // Create an instance of WeibullTwoParProbPlotFunc
            WeibullTwoParProbPlotFunc wppf = new WeibullTwoParProbPlotFunc();
            wppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying sigma and gamma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(wppf, Conv.copy(start), step, tolerance);

            // Obtain best estimates or first minimisation
            double[] firstBests = min.getBestEstimates();

            // Get mu and sigma value errors
            double[] firstErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            double ss = min.getSumOfSquares();

            //Calculate new initial estimates
            double[] start2 = new double[this.weibullTwoParNumberOfParameters];
            start2[0] = 2.0*firstBests[0] - start[0];
            if(start2[0]>minimum)start2[0] = minimum*(1.0 - Math.abs(minimum)*0.05);
            step[0] = Math.abs(start2[0]*0.1);
            if(step[0]==0)step[0] = this.range*0.01;
            start2[1] = 2.0*firstBests[1] - start[1];
            if(start2[1]<=0.0)start2[1] = Math.abs(2.0*firstBests[1] - 0.98*start[1]);
            step[1] = Math.abs(start2[1]*0.1);

            min.simplex(wppf, Conv.copy(start2), step, tolerance);

            // Get sigma and gamma for best correlation coefficient
            this.weibullTwoParParam = min.getBestEstimates();

            // Get sigma and gamma value errors
            this.weibullTwoParParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.weibullTwoParSumOfSquares = min.getSumOfSquares();

            if(ss<this.weibullSumOfSquares){
                this.weibullTwoParParam = firstBests;
                this.weibullTwoParParamErrors = firstErrors;
                this.weibullTwoParSumOfSquares = ss;
            }

            // Calculate WeibullTwoPar order statistic medians
            this.weibullTwoParOrderMedians = Stat.weibullOrderStatisticMedians(this.weibullTwoParParam[0], this.weibullTwoParParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Weibull order statistic medians
            Regression reg = new Regression(this.weibullTwoParOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.weibullTwoParLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.weibullTwoParLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.weibullTwoParCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.weibullTwoParOrderMedians;
            data[1] = this.sortedData;

            data[2] = weibullTwoParOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.weibullTwoParLine[0] + this.weibullTwoParLine[1]*weibullTwoParOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Weibull Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Two Parameter Weibull probability plot:   gradient = " + Fmath.truncate(this.weibullTwoParLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.weibullTwoParLine[0], 4) + ",  R = " + Fmath.truncate(this.weibullTwoParCorrCoeff, 4));
            pg.setGraphTitle2("  mu = 0, sigma = "  +  Fmath.truncate(this.weibullTwoParParam[0], 4) + ", gamma = "  +  Fmath.truncate(this.weibullTwoParParam[1], 4));

            // Plot
            pg.plot();

            this.weibullTwoParDone = true;
            this.probPlotDone = true;
        }

        // Return Two Parameter Weibull sigma
        public double weibullTwoParSigma(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParParam[0];
        }

        // Return Two Parameter Weibull sigma error
        public double weibullTwoParSigmaError(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParParamErrors[0];
        }

        // Return Two Parameter Weibull gamma
        public double weibullTwoParGamma(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParParam[1];
        }

        // Return Two Parameter Weibull gamma error
        public double weibullTwoParGammaError(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParParamErrors[1];
        }

        // Return Two Parameter Weibull order statistic medians
        public double[] weibullTwoParOrderStatisticMedians(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParOrderMedians;
        }

        // Return the Two Parameter Weibull gradient
        public double weibullTwoParGradient(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParLine[1];
        }

        // Return the error of the Two Parameter Weibull gradient
        public double weibullTwoParGradientError(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParLineErrors[1];
        }

        // Return the Two Parameter Weibull intercept
        public double weibullTwoParIntercept(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParLine[0];
        }

        // Return the error of the Two Parameter Weibull intercept
        public double weibullTwoParInterceptError(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParLineErrors[0];
        }

        // Return the Two Parameter Weibull correlation coefficient
        public double weibullTwoParCorrelationCoefficient(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParCorrCoeff;
        }

        // Return the sum of squares at the Two Parameter Weibull minimum
        public double weibullTwoParSumOfSquares(){
            if(!this.weibullTwoParDone){
                this.supressDisplay();
                this.weibullTwoParProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullTwoParSumOfSquares;
        }

        // WEIBULL PROBABILITY PLOT
        // Standard (one parameter)  (mu = 0, sigma =1)
        public void weibullStandardProbabilityPlot(){
            this.lastMethod = 13;

            // Check for negative x values
            if(this.sortedData[0]<0){
                System.out.println("Method weibullStandardProbabilityPlot: negative x value found - weibullThreeParProbabilityPlot called");
                this.weibullThreeParProbabilityPlot();
            }

            // Check data for suffient points
            this.weibullStandardNumberOfParameters = 1;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);

            // Calculate initial estimates
            double[] start = new double[1];
            start[0] = 4.0;
            double[] step = {Math.abs(0.3*start[0])};
            this.initialEstimates = start;
            double tolerance = 1e-10;

            // Add constraint; gamma>0
            min.addConstraint(0, -1, 0);

            // Create an instance of WeibullStandardProbPlotFunc
            WeibullStandardProbPlotFunc wppf = new WeibullStandardProbPlotFunc();
            wppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying gamma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(wppf, Conv.copy(start), step, tolerance);

            // Obtain best estimates or first minimisation
            double[] firstBests = min.getBestEstimates();

            // Get mu and sigma value errors
            double[] firstErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            double ss = min.getSumOfSquares();

            //Calculate new initial estimates
            double[] start2 = new double[this.weibullStandardNumberOfParameters];
            start2[0] = 2.0*firstBests[0] - start[0];
            if(start2[0]>minimum)start2[0] = minimum*(1.0 - Math.abs(minimum)*0.05);
            step[0] = Math.abs(start2[0]*0.1);
            if(step[0]==0)step[0] = this.range*0.01;

            min.simplex(wppf, Conv.copy(start2), step, tolerance);

            // Get gamma for best correlation coefficient
            this.weibullStandardParam = min.getBestEstimates();

            // Get gamma value errors
            this.weibullStandardParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.weibullStandardSumOfSquares = min.getSumOfSquares();

            // Calculate Weibull Standard order statistic medians
            this.weibullStandardOrderMedians = Stat.weibullOrderStatisticMedians(this.weibullStandardParam[0], this.numberOfDataPoints);

            // Regression of the ordered data on the Weibull order statistic medians
            Regression reg = new Regression(this.weibullStandardOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.weibullStandardLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.weibullStandardLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.weibullStandardCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.weibullStandardOrderMedians;
            data[1] = this.sortedData;

            data[2] = weibullStandardOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.weibullStandardLine[0] + this.weibullStandardLine[1]*weibullStandardOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Weibull Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Standard Weibull probability plot:   gradient = " + Fmath.truncate(this.weibullStandardLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.weibullStandardLine[0], 4) + ",  R = " + Fmath.truncate(this.weibullStandardCorrCoeff, 4));
            pg.setGraphTitle2("  mu = 0, sigma = 1, gamma = "  +  Fmath.truncate(this.weibullStandardParam[0], 4));

            // Plot
            pg.plot();

            this.weibullStandardDone = true;
            this.probPlotDone = true;
        }

        // WEIBULL PROBABILITY PLOT
        // Standard (one parameter)  (mu = 0, sigma =1)
        public void weibullOneParProbabilityPlot(){
             this.weibullStandardProbabilityPlot();
        }

        // Return Standard Weibull gamma
        public double weibullStandardGamma(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardParam[0];
        }

        // Return Standard Weibull gamma error
        public double weibullStandardGammaError(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardParamErrors[0];
        }

        // Return Standard Weibull order statistic medians
        public double[] weibullStandardOrderStatisticMedians(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardOrderMedians;
        }

        // Return the Standard Weibull gradient
        public double weibullStandardGradient(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardLine[1];
        }

        // Return the error of the Standard Weibull gradient
        public double weibullStandardGradientError(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardLineErrors[1];
        }

        // Return the Standard Weibull intercept
        public double weibullStandardIntercept(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardLine[0];
        }

        // Return the error of the Standard Weibull intercept
        public double weibullStandardInterceptError(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardLineErrors[0];
        }

        // Return the Standard Weibull correlation coefficient
        public double weibullStandardCorrelationCoefficient(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardCorrCoeff;
        }

        // Return the sum of squares at the Standard Weibull minimum
        public double weibullStandardSumOfSquares(){
            if(!this.weibullStandardDone){
                this.supressDisplay();
                this.weibullStandardProbabilityPlot();
                this.restoreDisplay();
            }
            return this.weibullStandardSumOfSquares;
        }


        // EXPONENTIAL PROBABILITY PLOT
        public void exponentialProbabilityPlot(){
            this.lastMethod = 2;

            // Check for suffient data points
            this.exponentialNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double muest = minimum;
            if(muest==0.0)muest = this.standardDeviation/3.0;
            double sigmaest = this.standardDeviation;
            double[] start = {muest, sigmaest};
            this.initialEstimates = start;
            double[] step = {0.3*muest, 0.3*sigmaest};
            double tolerance = 1e-10;

             // Add constraint; sigma>0
            min.addConstraint(1, -1, 0);

            // Create an instance of ExponentialProbPlotFunc
            ExponentialProbPlotFunc eppf = new ExponentialProbPlotFunc();
            eppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu and sigma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(eppf, start, step, tolerance);

            // Get mu and sigma values
            this.exponentialParam = min.getBestEstimates();

            // Get mu and sigma value errors
            this.exponentialParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.exponentialSumOfSquares = min.getSumOfSquares();

            // Calculate Exponential order statistic medians (Weibull with gamma = 1)
            this.exponentialOrderMedians = Stat.weibullOrderStatisticMedians(this.exponentialParam[0], this.exponentialParam[1], 1.0, this.numberOfDataPoints);

            // Regression of the ordered data on the Exponential order statistic medians
            Regression reg = new Regression(this.exponentialOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.exponentialLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.exponentialLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.exponentialCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.exponentialOrderMedians;
            data[1] = this.sortedData;

            data[2] = exponentialOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.exponentialLine[0] + this.exponentialLine[1]*exponentialOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Exponential Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Exponential probability plot:   gradient = " + Fmath.truncate(this.exponentialLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.exponentialLine[0], 4) + ",  R = " + Fmath.truncate(this.exponentialCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.exponentialParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.exponentialParam[1], 4));

            // Plot
            pg.plot();

            this.exponentialDone = true;
            this.probPlotDone = true;
        }

        // Return Exponential mu
        public double exponentialMu(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialParam[0];
        }

        // Return Exponential mu error
        public double exponentialMuError(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialParamErrors[0];
        }

        // Return Exponential sigma
        public double exponentialSigma(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialParam[1];
        }

        // Return Exponential sigma error
        public double exponentialSigmaError(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialParamErrors[1];
        }


        // Return Exponential order statistic medians
        public double[] exponentialOrderStatisticMedians(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialOrderMedians;
        }

        // Return the Exponential gradient
        public double exponentialGradient(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialLine[1];
        }

        // Return the error of the Exponential gradient
        public double exponentialGradientError(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialLineErrors[1];
        }

        // Return the Exponential intercept
        public double exponentialIntercept(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialLine[0];
        }

        // Return the error of the Exponential intercept
        public double exponentialInterceptError(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialLineErrors[0];
        }

        // Return the Exponential correlation coefficient
        public double exponentialCorrelationCoefficient(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialCorrCoeff;
        }

        // Return the sum of squares at the Exponential minimum
        public double exponentialSumOfSquares(){
            if(!this.exponentialDone){
                this.supressDisplay();
                this.exponentialProbabilityPlot();
                this.restoreDisplay();
            }
            return this.exponentialSumOfSquares;
        }


        // FRECHET PROBABILITY PLOT
        public void frechetProbabilityPlot(){
            this.lastMethod = 7;

            // Check for suffient data points
            this.frechetNumberOfParameters = 3;
            if(this.numberOfDataPoints<4)throw new IllegalArgumentException("There must be at least four data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);

            // Calculate initial estimates
            double[] start = new double[3];
            start[0] = this.minimum - 0.1*Math.abs(this.minimum);
            start[1] = this.peakWidth()/3.0;
            if(start[1]<1.0)start[1] = 2.0;
            start[2] = 4.0;
            this.initialEstimates = start;
            double[] step = {Math.abs(0.3*start[0]), Math.abs(0.3*start[1]), Math.abs(0.3*start[2])};
            if(step[0]==0)step[0] = this.range*0.01;
            double tolerance = 1e-10;

            // Add constraint; mu<minimum, sigma>0, gamma>0
            min.addConstraint(0, +1, minimum);
            min.addConstraint(1, -1, 0);
            min.addConstraint(2, -1, 0);

            // Create an instance of FrechetProbPlotFunc
            FrechetProbPlotFunc fppf = new FrechetProbPlotFunc();
            fppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu, sigma and gamma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(fppf, Conv.copy(start), step, tolerance);

            // Obtain best estimates or first minimisation
            double[] firstBests = min.getBestEstimates();

            double ss = min.getSumOfSquares();

            //Calculate new initial estimates
            double[] start2 = new double[this.frechetNumberOfParameters];
            start2[0] = 2.0*firstBests[0] - start[0];
            if(start2[0]>minimum)start2[0] = minimum*(1.0 - Math.abs(minimum)*0.05);
            step[0] = Math.abs(start2[0]*0.1);
            if(step[0]==0)step[0] = this.range*0.01;
            start2[1] = 2.0*firstBests[1] - start[1];
            if(start2[1]<=0.0)start2[1] = Math.abs(2.0*firstBests[1] - 0.98*start[1]);
            step[1] = Math.abs(start2[1]*0.1);
            start2[2] = 2.0*firstBests[2] - start[2];
            if(start2[1]<=0.0)start2[2] = Math.abs(2.0*firstBests[2] - 0.98*start[2]);
            step[2] = Math.abs(start2[2]*0.1);

            min.simplex(fppf, Conv.copy(start2), step, tolerance);

            // Get mu, sigma and gamma for best correlation coefficient
            this.frechetParam = min.getBestEstimates();
            double ss2 = min.getSumOfSquares();
            if(ss<ss2)this.frechetParam = firstBests;

            // Calculate Frechet order statistic medians
            this.frechetOrderMedians = Stat.frechetOrderStatisticMedians(this.frechetParam[0], this.frechetParam[1], this.frechetParam[2], this.numberOfDataPoints);

            // Regression of the ordered data on the Frechet order statistic medians
            Regression reg = new Regression(this.frechetOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.frechetLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.frechetLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.frechetCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.frechetOrderMedians;
            data[1] = this.sortedData;

            data[2] = frechetOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.frechetLine[0] + this.frechetLine[1]*frechetOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Frechet Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Frechet probability plot:   gradient = " + Fmath.truncate(this.frechetLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.frechetLine[0], 4) + ",  R = " + Fmath.truncate(this.frechetCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.frechetParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.frechetParam[1], 4) + ", gamma = "  +  Fmath.truncate(this.frechetParam[2], 4));

            // Plot
            pg.plot();

            this.frechetDone = true;
            this.probPlotDone = true;
        }

        // Return Frechet mu
        public double frechetMu(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParam[0];
        }

        // Return Frechet mu error
        public double frechetMuError(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParamErrors[0];
        }

        // Return Frechet sigma
        public double frechetSigma(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParam[1];
        }

        // Return Frechet sigma error
        public double frechetSigmaError(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParamErrors[1];
        }

        // Return Frechet gamma
        public double frechetGamma(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParam[2];
        }

        // Return Frechet gamma error
        public double frechetGammaError(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetParamErrors[2];
        }

        // Return Frechet order statistic medians
        public double[] frechetOrderStatisticMedians(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetOrderMedians;
        }

        // Return the Frechet gradient
        public double frechetGradient(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetLine[1];
        }

        // Return the error of the Frechet gradient
        public double frechetGradientError(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetLineErrors[1];
        }

        // Return the Frechet intercept
        public double frechetIntercept(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetLine[0];
        }

        // Return the error of the Frechet intercept
        public double frechetInterceptError(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetLineErrors[0];
        }

        // Return the Frechet correlation coefficient
        public double frechetCorrelationCoefficient(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetCorrCoeff;
        }

        // Return the sum of squares at the Frechet minimum
        public double frechetSumOfSquares(){
            if(!this.frechetDone){
                this.supressDisplay();
                this.frechetProbabilityPlot();
                this.restoreDisplay();
            }
            return this.frechetSumOfSquares;
        }



        // GUMBEL (MINIMUM ORDER STATISTIC) PROBABILITY PLOT
        public void gumbelMinProbabilityPlot(){
            this.lastMethod = 5;

            // Check for suffient data points
            this.gumbelMinNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double muest = mean;
            if(muest==0.0)muest = this.standardDeviation/3.0;
            double sigmaest = this.standardDeviation;
            double[] start = {muest, sigmaest};
            double[] step = {0.3*muest, 0.3*sigmaest};
            this.initialEstimates = start;
            double tolerance = 1e-10;

             // Add constraint; sigma>0
            min.addConstraint(1, -1, 0);

            // Create an instance of Gumbel (minimum order statistic)ProbPlotFunc
            GumbelMinProbPlotFunc gmippf = new GumbelMinProbPlotFunc();
            gmippf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu and sigma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(gmippf, start, step, tolerance);

             // Get mu and sigma values
            this.gumbelMinParam = min.getBestEstimates();

            // Get mu and sigma value errors
            this.gumbelMinParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.gumbelMinSumOfSquares = min.getSumOfSquares();

            // Calculate Gumbel (minimum order statistic) order statistic medians
            this.gumbelMinOrderMedians = Stat.gumbelMinOrderStatisticMedians(this.gumbelMinParam[0], this.gumbelMinParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Gumbel (minimum order statistic) order statistic medians
            Regression reg = new Regression(this.gumbelMinOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.gumbelMinLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.gumbelMinLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.gumbelMinCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.gumbelMinOrderMedians;
            data[1] = this.sortedData;

            data[2] = gumbelMinOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.gumbelMinLine[0] + this.gumbelMinLine[1]*gumbelMinOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Gumbel (minimum order statistic) Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Gumbel (minimum order statistic) probability plot:   gradient = " + Fmath.truncate(this.gumbelMinLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.gumbelMinLine[0], 4) + ",  R = " + Fmath.truncate(this.gumbelMinCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.gumbelMinParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.gumbelMinParam[1], 4));

            // Plot
            pg.plot();

            this.gumbelMinDone = true;
            this.probPlotDone = true;
        }

        // Return Gumbel (minimum order statistic) mu
        public double gumbelMinMu(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinParam[0];
        }

        // Return Gumbel (minimum order statistic) mu error
        public double gumbelMinMuError(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinParamErrors[0];
        }

        // Return Gumbel (minimum order statistic) sigma
        public double gumbelMinSigma(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinParam[1];
        }

        // Return Gumbel (minimum order statistic) sigma error
        public double gumbelMinSigmaError(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinParamErrors[1];
        }

        // Return Gumbel (minimum order statistic) order statistic medians
        public double[] gumbelMinOrderStatisticMedians(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinOrderMedians;
        }

        // Return the Gumbel (minimum order statistic) gradient
        public double gumbelMinGradient(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinLine[1];
        }

        // Return the error of the Gumbel (minimum order statistic) gradient
        public double gumbelMinGradientError(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinLineErrors[1];
        }

        // Return the Gumbel (minimum order statistic) intercept
        public double gumbelMinIntercept(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinLine[0];
        }

        // Return the error of the Gumbel (minimum order statistic) intercept
        public double gumbelMinInterceptError(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinLineErrors[0];
        }

        // Return the Gumbel (minimum order statistic) correlation coefficient
        public double gumbelMinCorrelationCoefficient(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinCorrCoeff;
        }

        // Return the sum of squares at the Gumbel (minimum order statistic) minimum
        public double gumbelMinSumOfSquares(){
            if(!this.gumbelMinDone){
                this.supressDisplay();
                this.gumbelMinProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMinSumOfSquares;
        }


        // GUMBEL (MAXIMUM ORDER STATISTIC) PROBABILITY PLOT
        public void gumbelMaxProbabilityPlot(){
            this.lastMethod = 6;

            // Check for suffient data points
            this.gumbelMaxNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double muest = mean;
            if(muest==0.0)muest = this.standardDeviation/3.0;
            double sigmaest = this.standardDeviation;
            double[] start = {muest, sigmaest};
            this.initialEstimates = start;
            double[] step = {0.3*muest, 0.3*sigmaest};
            double tolerance = 1e-10;

             // Add constraint; sigma>0
            min.addConstraint(1, -1, 0);

            // Create an instance of Gumbel (maximum order statistic)ProbPlotFunc
            GumbelMaxProbPlotFunc gmappf = new GumbelMaxProbPlotFunc();
            gmappf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying mu and sigma
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(gmappf, start, step, tolerance);

              // Get mu and sigma values
            this.gumbelMaxParam = min.getBestEstimates();

            // Get mu and sigma value errors
            this.gumbelMaxParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.gumbelMaxSumOfSquares = min.getSumOfSquares();

            // Calculate Gumbel (maximum order statistic) order statistic medians
            this.gumbelMaxOrderMedians = Stat.gumbelMaxOrderStatisticMedians(this.gumbelMaxParam[0], this.gumbelMaxParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Gumbel (maximum order statistic) order statistic medians
            Regression reg = new Regression(this.gumbelMaxOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.gumbelMaxLine = reg.getBestEstimates();

            // Correlation coefficient
            this.gumbelMaxCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.gumbelMaxOrderMedians;
            data[1] = this.sortedData;

            data[2] = gumbelMaxOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.gumbelMaxLine[0] + this.gumbelMaxLine[1]*gumbelMaxOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Gumbel (maximum order statistic) Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Gumbel (maximum order statistic) probability plot:   gradient = " + Fmath.truncate(this.gumbelMaxLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.gumbelMaxLine[0], 4) + ",  R = " + Fmath.truncate(this.gumbelMaxCorrCoeff, 4));
            pg.setGraphTitle2("  mu = " + Fmath.truncate(this.gumbelMaxParam[0], 4) + ", sigma = "  +  Fmath.truncate(this.gumbelMaxParam[1], 4));

            // Plot
            pg.plot();

            this.gumbelMaxDone = true;
            this.probPlotDone = true;
        }

        // Return Gumbel (maximum order statistic) mu
        public double gumbelMaxMu(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxParam[0];
        }

        // Return Gumbel (maximum order statistic) mu error
        public double gumbelMaxMuError(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxParamErrors[0];
        }

        // Return Gumbel (maximum order statistic) sigma
        public double gumbelMaxSigma(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxParam[1];
        }

        // Return Gumbel (maximum order statistic) sigma error
        public double gumbelMaxSigmaError(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxParamErrors[1];
        }

        // Return Gumbel (maximum order statistic) order statistic medians
        public double[] gumbelMaxOrderStatisticMedians(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxOrderMedians;
        }

        // Return the Gumbel (maximum order statistic) gradient
        public double gumbelMaxGradient(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxLine[1];
        }

        // Return the error of the Gumbel (maximum order statistic) gradient
        public double gumbelMaxGradientError(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxLineErrors[1];
        }

        // Return the Gumbel (maximum order statistic) intercept
        public double gumbelMaxIntercept(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxLine[0];
        }

        // Return the error of the Gumbel (maximum order statistic) intercept
        public double gumbelMaxInterceptError(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxLineErrors[0];
        }

        // Return the Gumbel (maximum order statistic) correlation coefficient
        public double gumbelMaxCorrelationCoefficient(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxCorrCoeff;
        }

        // Return the sum of squares at the Gumbel (maximum order statistic) minimum
        public double gumbelMaxSumOfSquares(){
            if(!this.gumbelMaxDone){
                this.supressDisplay();
                this.gumbelMaxProbabilityPlot();
                this.restoreDisplay();
            }
            return this.gumbelMaxSumOfSquares;
        }


        // RAYLEIGH PROBABILITY PLOT
        public void rayleighProbabilityPlot(){
            this.lastMethod = 3;

            // Check for suffient data points
            this.rayleighNumberOfParameters = 1;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double sigmaest = this.standardDeviation;
            double[] start = {sigmaest};
            double[] step = {0.3*sigmaest};
            this.initialEstimates = start;
            double tolerance = 1e-10;

             // Add constraint; beta>0
            min.addConstraint(0, -1, 0);

            // Create an instance of RayleighProbPlotFunc
            RayleighProbPlotFunc rppf = new RayleighProbPlotFunc();
            rppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying beta
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(rppf, start, step, tolerance);

             // Get mu and sigma values
            this.rayleighParam = min.getBestEstimates();

            // Get mu and sigma value errors
            this.rayleighParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.rayleighSumOfSquares = min.getSumOfSquares();

            // Calculate Rayleigh order statistic medians (Weibull with mu = 0, sigma = sqrt(2).beta, gamma = 2)
            this.rayleighOrderMedians = Stat.weibullOrderStatisticMedians(0.0, this.rayleighParam[0]*Math.sqrt(2.0), 2.0, this.numberOfDataPoints);

            // Regression of the ordered data on the Rayleigh order statistic medians
            Regression reg = new Regression(this.rayleighOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.rayleighLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.rayleighLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.rayleighCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.rayleighOrderMedians;
            data[1] = this.sortedData;

            data[2] = rayleighOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.rayleighLine[0] + this.rayleighLine[1]*rayleighOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Rayleigh Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Rayleigh probability plot:   gradient = " + Fmath.truncate(this.rayleighLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.rayleighLine[0], 4) + ",  R = " + Fmath.truncate(this.rayleighCorrCoeff, 4));
            pg.setGraphTitle2("  beta = " + Fmath.truncate(this.rayleighParam[0], 4));

            // Plot
            pg.plot();

            this.rayleighDone = true;
            this.probPlotDone = true;
        }

        // Return Rayleigh beta
        public double rayleighBeta(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighParam[0];
        }

        // Return Rayleigh beta error
        public double rayleighBetaError(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighParamErrors[0];
        }

        // Return Rayleigh order statistic medians
        public double[] rayleighOrderStatisticMedians(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighOrderMedians;
        }

        // Return the Rayleigh gradient
        public double rayleighGradient(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighLine[1];
        }

        // Return the error of the Rayleigh gradient
        public double rayleighGradientError(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighLineErrors[1];
        }

        // Return the Rayleigh intercept
        public double rayleighIntercept(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighLine[0];
        }

        // Return the error of the Rayleigh intercept
        public double rayleighInterceptError(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighLineErrors[0];
        }

        // Return the Rayleigh correlation coefficient
        public double rayleighCorrelationCoefficient(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighCorrCoeff;
        }

        // Return the sum of squares at the Rayleigh minimum
        public double rayleighSumOfSquares(){
            if(!this.rayleighDone){
                this.supressDisplay();
                this.rayleighProbabilityPlot();
                this.restoreDisplay();
            }
            return this.rayleighSumOfSquares;
        }

        // PARETO PROBABILITY PLOT
        public void paretoProbabilityPlot(){
            this.lastMethod = 4;

            // Check for suffient data points
            this.paretoNumberOfParameters = 2;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Create instance of Regression
            Regression min = new Regression(this.sortedData, this.sortedData);
            double betaest = this.minimum;
            double alphaest = this.mean/(this.mean - betaest);
            double[] start = {alphaest, betaest};
            double[] step = {0.3*alphaest, 0.3*betaest};
            this.initialEstimates = start;
            double tolerance = 1e-10;

            // Create an instance of ParetoProbPlotFunc
            ParetoProbPlotFunc pppf = new ParetoProbPlotFunc();
            pppf.setDataArray(this.numberOfDataPoints);

            // Obtain best probability plot varying alpha and beta
            // by minimizing the sum of squares of the differences between the ordered data and the ordered statistic medians
            min.simplex(pppf, start, step, tolerance);

            // Get alpha and beta values
            this.paretoParam = min.getBestEstimates();

            // Get alpha and beta value errors
            this.paretoParamErrors = min.getBestEstimatesErrors();

            // Get sum of squares
            this.paretoSumOfSquares = min.getSumOfSquares();

            // Calculate Pareto order statistic medians
            this.paretoOrderMedians = Stat.paretoOrderStatisticMedians(this.paretoParam[0], this.paretoParam[1], this.numberOfDataPoints);

            // Regression of the ordered data on the Pareto order statistic medians
            Regression reg = new Regression(this.paretoOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.paretoLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.paretoLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.paretoCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.paretoOrderMedians;
            data[1] = this.sortedData;

            data[2] = paretoOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.paretoLine[0] + this.paretoLine[1]*paretoOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("Pareto Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("Pareto probability plot:   gradient = " + Fmath.truncate(this.paretoLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.paretoLine[0], 4) + ",  R = " + Fmath.truncate(this.paretoCorrCoeff, 4));
            pg.setGraphTitle2("  alpha = " + Fmath.truncate(this.paretoParam[0], 4) + ", beta = "  +  Fmath.truncate(this.paretoParam[1], 4));

            // Plot
            pg.plot();

            this.paretoDone = true;
            this.probPlotDone = true;
        }

        // Return Pareto alpha
        public double paretoAlpha(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoParam[0];
        }

        // Return Pareto alpha error
        public double paretoAlphaError(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoParamErrors[0];
        }

        // Return Pareto beta
        public double paretoBeta(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoParam[1];
        }

        // Return Pareto beta error
        public double paretoBetaError(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoParamErrors[1];
        }

        // Return Pareto order statistic medians
        public double[] paretoOrderStatisticMedians(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoOrderMedians;
        }

        // Return the Pareto gradient
        public double paretoGradient(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoLine[1];
        }

        // Return the error of the Pareto gradient
        public double paretoGradientError(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoLineErrors[1];
        }

        // Return the Pareto intercept
        public double paretoIntercept(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoLine[0];
        }

        // Return the error of the Pareto intercept
        public double paretoInterceptError(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoLineErrors[0];
        }

        // Return the Pareto correlation coefficient
        public double paretoCorrelationCoefficient(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoCorrCoeff;
        }

        // Return the sum of squares at the Pareto minimum
        public double paretoSumOfSquares(){
            if(!this.paretoDone){
                this.supressDisplay();
                this.paretoProbabilityPlot();
                this.restoreDisplay();
            }
            return this.paretoSumOfSquares;
        }


        // F-DISTRIBUTION PROBABILITY PLOT
        public void fDistributionProbabilityPlot(int nu1, int nu2){
            this.lastMethod = 15;

            // Check for suffient data points
            this.fDistributionNumberOfParameters = 0;
            if(this.numberOfDataPoints<3)throw new IllegalArgumentException("There must be at least three data points - preferably considerably more");

            // Calculate Exponential order statistic medians
            this.fDistributionOrderMedians = Stat.fDistributionOrderStatisticMedians(nu1, nu2, this.numberOfDataPoints);

            // Regression of the ordered data on the F-distribution order statistic medians
            Regression reg = new Regression(this.fDistributionOrderMedians, this.sortedData);
            reg.linear();

            // Intercept and gradient of best fit straight line
            this.fDistributionLine = reg.getBestEstimates();

            // Estimated erors of the intercept and gradient of best fit straight line
            this.fDistributionLineErrors = reg.getBestEstimatesErrors();

            // Correlation coefficient
            this.fDistributionCorrCoeff = reg.getSampleR();

            // Initialize data arrays for plotting
            double[][] data = PlotGraph.data(2,this.numberOfDataPoints);

            // Assign data to plotting arrays
            data[0] = this.fDistributionOrderMedians;
            data[1] = this.sortedData;

            data[2] = fDistributionOrderMedians;
            for(int i=0; i<this.numberOfDataPoints; i++){
                data[3][i] = this.fDistributionLine[0] + this.fDistributionLine[1]*fDistributionOrderMedians[i];
            }

            // Create instance of PlotGraph
            PlotGraph pg = new PlotGraph(data);
            int[] points = {4, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);
            pg.setXaxisLegend("F-distribution Order Statistic Medians");
            pg.setYaxisLegend("Ordered Data Values");
            pg.setGraphTitle("F-distribution probability plot:   gradient = " + Fmath.truncate(this.fDistributionLine[1], 4) + ", intercept = "  +  Fmath.truncate(this.fDistributionLine[0], 4) + ",  R = " + Fmath.truncate(this.fDistributionCorrCoeff, 4));
            pg.setGraphTitle2("  nu1 = " + nu1 + ", nu2 = " + nu2);

            // Plot
            pg.plot();

            this.fDistributionDone = true;
            this.probPlotDone = true;
        }

        // Return F-distribution order statistic medians
        public double[] fDistributionOrderStatisticMedians(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionOrderMedians;
        }

        // Return the F-distribution gradient
        public double fDistributionGradient(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionLine[1];
        }

        // Return the error of the F-distribution gradient
        public double fDistributionGradientError(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionLineErrors[1];
        }

        // Return the F-distribution intercept
        public double fDistributionIntercept(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionLine[0];
        }

        // Return the error of the F-distribution intercept
        public double fDistributionInterceptError(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionLineErrors[0];
        }

        // Return the F-distribution correlation coefficient
        public double fDistributionCorrelationCoefficient(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionCorrCoeff;
        }

        // Return the sum of squares at the F-distribution minimum
        public double fDistributionSumOfSquares(){
            if(!this.fDistributionDone)throw new IllegalArgumentException("F-distribution Probability Plot method has not been called");
            return this.fDistributionSumOfSquares;
        }



        // COMMON METHODS

        // Return the ordered data
        public double[] orderedData(){
            return this.sortedData;
        }

        // Return the number of data points
        public int numberOfDataPoints(){
            return this.numberOfDataPoints;
        }

        // Return the data mean
        public double mean(){
            return this.mean;
        }

        // Return the data standard deviation
        public double standardDeviation(){
            if(!this.probPlotDone)throw new IllegalArgumentException("no probability plot method has been called");
            return this.standardDeviation;
        }

        // Return the data minimum
        public double minimum(){
            return this.minimum;
        }

        // Return the data maximum
        public double maximum(){
             return this.maximum;
        }

        // Return the numerical differentiation step, delta
        public double delta(){
            return this.delta;
        }

        // Reset the numerical differentiation step, delta
        public void resetDelta(double delta){
            this.delta = delta;
        }

        // Set standard deviation denominator to n
        public void setDenominatorToN(){
            this.nFactorOptionI = true;
            this.arrayAsStat.setDenominatorToN();
            this.standardDeviation = arrayAsStat.standardDeviation();
            this.nFactorReset = true;
        }

        // Set standard deviation denominator to n-1
        public void setDenominatorToNminusOne(){
            this.nFactorOptionI = false;
            arrayAsStat.setDenominatorToNminusOne();
            this.standardDeviation = arrayAsStat.standardDeviation();
            this.nFactorReset = true;
        }

        // Return initial estimates used in last call to a probability plot method
        public double[] getInitialEstimates(){
            return this.initialEstimates;
        }

}



// PROBABILITY PLOT FUNCTIONS
// Gaussian Probabilty plot function
class GaussProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Gaussian order statistic medians
        if(index==0)medians = Stat.gaussianOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}



// Exponential Probabilty plot function
class ExponentialProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Exponential order statistic medians
        if(index==0)medians = Stat.exponentialOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}




// Frechet Probabilty plot function
class FrechetProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Frechet order statistic medians
        if(index==0)medians = Stat.frechetOrderStatisticMedians(p[0], p[1], p[2], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}


// Gumbel (minimum order statistic) Probabilty plot function
class GumbelMinProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;


    public double function(double[] p, double[] x){

        // Calculate Gumbel order statistic medians
        if(index==0)medians = Stat.gumbelMinOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}



// Gumbel (maximum order statistic) Probabilty plot function
class GumbelMaxProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

         // Calculate Gumbel order statistic medians
        if(index==0)medians = Stat.gumbelMaxOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}


// Logistic Probabilty plot function
class LogisticProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Logistic order statistic medians
        if(index==0)medians = Stat.logisticOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}



// Pareto Probabilty plot function
class ParetoProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Pareto order statistic medians
        if(index==0)medians = Stat.paretoOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}

// Rayleigh Probabilty plot function
class RayleighProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;


    public double function(double[] p, double[] x){

        // Calculate Rayleigh order statistic medians
        if(index==0)medians = Stat.rayleighOrderStatisticMedians(p[0], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}

// Weibull Probabilty plot function
// Three parameter
class WeibullProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Weibull order statistic medians
        if(index==0)medians = Stat.weibullOrderStatisticMedians(p[0], p[1], p[2], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;
    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}

// Weibull Probabilty plot function
// Two parameter
class WeibullTwoParProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Weibull order statistic medians
        if(index==0)medians = Stat.weibullOrderStatisticMedians(p[0], p[1], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;

    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}

// Weibull Probabilty plot function
// Standard (one parameter)
class WeibullStandardProbPlotFunc implements RegressionFunction{

    private int nPoints = 0;
    private int index = 0;
    private double[] medians = null;

    public double function(double[] p, double[] x){

        // Calculate Weibull order statistic medians
        if(index==0)medians = Stat.weibullOrderStatisticMedians(p[0], nPoints);

        // return median value
        double y = medians[index];
        index++;
        if(index==nPoints)index=0;
        return y;

    }

    public void setDataArray(int nPoints){
        this.nPoints = nPoints;
    }
}

