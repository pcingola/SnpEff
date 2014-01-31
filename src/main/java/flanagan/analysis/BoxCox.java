/*
*   Class   BoxCox
*
*   USAGE:  Box-Cox Transformation
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       July - August 2008
*   AMENDED:    2-12 September 2008, 15-19 December 2010, 
*               2-5 January 2011, 20 January 2011, 7 December 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/BoxCox.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2008 - 2011  Michael Thomas Flanagan
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.text.*;

import flanagan.analysis.Stat;
import flanagan.analysis.Regression;
import flanagan.math.ArrayMaths;
import flanagan.math.Maximization;
import flanagan.math.MaximizationFunction;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.plot.*;
import flanagan.io.FileOutput;


public class BoxCox{

    private double[] originalData = null;                           // original data to be analysed as Gaussian
    private double[] sortedOriginalData = null;                     // ordered original data
    private double[] standardizedOriginalData = null;               // standardized original data
    private Stat sod = null;                                        // Stat instance of the standardizedOriginalData
    private double[] shiftedStandardizedOriginalData = null;        // data shifted to ensure all points are positive
    private int[] originalIndices = null;                           // original indices before data are ordered into ascending order
    private int nData = 0;                                          // number of data points

    private double originalRange = 0.0;                             // original data range
    private double originalMinimum = 0.0;                           // original data minimum
    private double originalMaximum = 0.0;                           // original data maximum
    private double originalMean = 0.0;                              // original data mean
    private double originalMedian = 0.0;                            // original original data median
    private double originalStandardDeviation = 0.0;                 // original data standard deviation
    private double originalVariance = 0.0;                          // original data variance
    private double originalMomentSkewness = 0.0;                    // original data moment skewness
    private double originalMedianSkewness = 0.0;                    // original data median skewness
    private double originalQuartileSkewness = 0.0;                  // original data quartile skewness
    private double originalExcessKurtosis = 0.0;                    // original data excess kurtosis

    private double standardizedOriginalRange = 0.0;                 // standardized original data range
    private double standardizedOriginalMinimum = 0.0;               // standardized original data minimum
    private double standardizedOriginalMaximum = 0.0;               // standardized original data maximum
    private double standardizedOriginalMean = 0.0;                  // standardized original original data mean
    private double standardizedOriginalMedian = 0.0;                  // standardized original original data median
    private double standardizedOriginalStandardDeviation = 1.0;     // standardized original data standard deviation
    private double standardizedOriginalVariance = 1.0;              // standardized original data variance
    private double standardizedOriginalMomentSkewness = 0.0;        // standardized original data moment skewness
    private double standardizedOriginalMedianSkewness = 0.0;        // standardized original data median skewness
    private double standardizedOriginalQuartileSkewness = 0.0;      // standardized original data quartile skewness
    private double standardizedOriginalExcessKurtosis = 0.0;        // standardized original data excess kurtosis

    private double originalSampleR = 0.0;                           // Probabilty plot correlation coefficient for the original data
    private double originalIntercept = 0.0;                         // Probabilty plot intercept for the original data
    private double originalGradient = 0.0;                          // Probabilty plot gradient for the original data
    private double originalInterceptError = 0.0;                    // Estimated error of the probabilty plot intercept for the original data
    private double originalGradientError = 0.0;                     // Estimated error of the probabilty plot gradient for the original data

    private boolean initializationDone = false;                     // = true when initialization of`the data is complete

    private double[] transformedData = null;                        // Box-Cox transformed data
    private double[] standardizedTransformedData = null;            // standardized  Box-Cox transformed data
    private double[] scaledTransformedData = null;                  // Box-Cox transformed data scaled to original mean and standard deviation
    private double[] sortedScaledTransformedData = null;            // Box-Cox transformed data scaled to original mean and standard deviation sorted into ascending order

    private double transformedRange = 0.0;                          // scaled transformed data range
    private double transformedMinimum = 0.0;                        // scaled transformed data minimum
    private double transformedMaximum = 0.0;                        // scaled transformed data maximum
    private double transformedMean = 0.0;                           // scaled transformed data mean
    private double transformedStandardDeviation = 0.0;              // scaled transformed data standard deviation
    private double transformedMedian = 0.0;                         // scaled transformed data median
    private double transformedVariance = 0.0;                       // scaled transformed data variance
    private double transformedMomentSkewness = 0.0;                 // scaled transformed data moment skewness
    private double transformedMedianSkewness = 0.0;                 // scaled transformed data median skewness
    private double transformedQuartileSkewness = 0.0;               // scaled transformed data quartile skewness
    private double transformedExcessKurtosis = 0.0;                 // scaled transformed data excess kurtosis

    private double standardizedTransformedRange = 0.0;              // standardized transformed data range
    private double standardizedTransformedMinimum = 0.0;            // standardized transformed data minimum
    private double standardizedTransformedMaximum = 0.0;            // standardized transformed data maximum
    private double standardizedTransformedMean = 0.0;               // standardized transformed data mean
    private double standardizedTransformedMedian = 0.0;             // standardized transformed data median
    private double standardizedTransformedStandardDeviation = 1.0;  // standardized transformed data standard deviation
    private double standardizedTransformedVariance = 1.0;           // standardized transformed data variance
    private double standardizedTransformedMomentSkewness = 0.0;     // standardized transformed data moment skewness
    private double standardizedTransformedMedianSkewness = 0.0;     // standardized transformed data median skewness
    private double standardizedTransformedQuartileSkewness = 0.0;   // standardized transformed data quartile skewness
    private double standardizedTransformedExcessKurtosis = 0.0;     // standardized transformed data excess kurtosis

    private double[] inverseData = null;                            // inverse transformed data
    private double lambdaThree = 0.0;                               // shift, if needed, to allow inverse transform

    private double lambdaOne = 0.0;                                 // Box-Cox lambdaOne
    private double lambdaTwo = 0.0;                                 // Box-Cox lambdaTwo

    private double transformedSampleR = 0.0;                        // Probabilty plot correlation coefficient for the transformed data
    private double transformedIntercept = 0.0;                      // Probabilty plot intercept for the transformed data
    private double transformedGradient = 0.0;                       // Probabilty plot gradient for the transformed data
    private double transformedInterceptError = 0.0;                 // Estimated error of the probabilty plot intercept for the transformed data
    private double transformedGradientError = 0.0;                  // Estimated error of the probabilty plot gradient for the transformed data

    private double[] uniformOrderMedians = null;                    // uniform order statistic medians
    private double[] gaussianOrderMedians = null;                   // Gaussian order statistic medians

    private boolean transformDone = false;                          // = true when Box-Cox transform performed
    private boolean inverseDone = false;                            // = true when Box-Cox inverse transform performed



    // CONSTRUCTORS
    public BoxCox(double[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(float[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(int[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(long[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(short[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(byte[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(BigDecimal[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(BigInteger[] originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(Stat originalData){
        this.sod = originalData;
    }

    public BoxCox(ArrayMaths amoriginalData){
        double[] ama = amoriginalData.array();
        this.sod = new Stat(ama);
    }

    public BoxCox(ArrayList<Object> originalData){
        this.sod = new Stat(originalData);
    }

    public BoxCox(Vector<Object> originalData){
        this.sod = new Stat(originalData);
    }

    // set denominator of variances to n
    public void setDenominatorToN(){
        Stat.setStaticDenominatorToN();
    }

    // Initialise original data arrays
    private void initialize(){

        // store entered data as instance variable
        this.originalData = this.sod.array_as_double();

        // Calculate original data statistics
        this.originalMinimum = this.sod.minimum();
        this.originalMaximum = this.sod.maximum();
        this.originalMedian = this.sod.median();
        if(this.originalMinimum==this.originalMaximum)throw new IllegalArgumentException("A Box-Cox transformation cannot be performed on a data array of identical values");
        this.originalRange = this.originalMaximum - this.originalMinimum;
        this.originalMean = this.sod.mean();
        this.originalStandardDeviation = this.sod.standardDeviation();
        this.originalVariance = this.sod.variance();
        this.originalMomentSkewness = this.sod.momentSkewness();
        this.originalMedianSkewness = this.sod.medianSkewness();
        this.originalQuartileSkewness = this.sod.quartileSkewness();
        this.originalExcessKurtosis = this.sod.excessKurtosis();

        // Store original data sorted into ascending order
        ArrayMaths sorted = this.sod.sort();
        this.sortedOriginalData = sorted.array();
        this.originalIndices = sorted.originalIndices();

        // Standardize and store standardized data
        this.standardizedOriginalData = this.sod.standardize();

        // Calculate standardized original data statistics
        this.standardizedOriginalMinimum = this.sod.minimum();
        this.standardizedOriginalMaximum = this.sod.maximum();
        this.standardizedOriginalMedian = this.sod.median();
        this.standardizedOriginalRange = this.standardizedOriginalMaximum - this.standardizedOriginalMinimum;
        this.standardizedOriginalMean = 0.0;
        this.standardizedOriginalStandardDeviation = 1.0;
        this.standardizedOriginalVariance = 1.0;
        this.standardizedOriginalMomentSkewness = this.sod.momentSkewness();
        this.standardizedOriginalMedianSkewness = this.sod.medianSkewness();
        this.standardizedOriginalQuartileSkewness = this.sod.quartileSkewness();
        this.standardizedOriginalExcessKurtosis = this.sod.excessKurtosis();

        // Numbet of data points
        this.nData = this.originalData.length;

        //  Calculate uniform order statistic medians
        this.uniformOrderMedians = Stat.uniformOrderStatisticMedians(this.nData);

        // Calculate Gaussian N[0,1] order statistic medians
        this.gaussianOrderMedians = Stat.gaussianOrderStatisticMedians(this.nData);

        // calculate the correlation coefficient of the probability plot for the untransformed data
        Regression reg = new Regression(this.gaussianOrderMedians, ((new ArrayMaths(this.standardizedOriginalData)).sort().array()));
        reg.linear();
        this.originalSampleR = reg.getSampleR();
        double[] coeff = reg.getBestEstimates();
        this.originalIntercept = coeff[0];
        this.originalGradient = coeff[1];
        coeff = reg.getBestEstimatesErrors();
        this.originalInterceptError = coeff[0];
        this.originalGradientError = coeff[1];

        this.initializationDone = true;
    }

    // BOX-COX TRANSFORMATION
    private double[] transform(){

        // initialize arrays and analyse original data
        if(!this.initializationDone)this.initialize();

        // Shift data to ensure all values are greater than zero
        this.lambdaTwo = 0.1*this.standardizedOriginalRange - this.standardizedOriginalMinimum;
        ArrayMaths st1 = this.sod.plus(this.lambdaTwo);
        this.shiftedStandardizedOriginalData = st1.getArray_as_double();

        // Create an instance of the BoxCoxFunction for Maximization
        BoxCoxFunction bcf = new BoxCoxFunction();
        bcf.shiftedData = this.shiftedStandardizedOriginalData;
        bcf.nData = this.nData;
        bcf.yTransform = new double[this.nData];
        bcf.gaussianOrderMedians = this.gaussianOrderMedians;

        // Create an instance of Maximization
        Maximization max = new Maximization();

        // Initial estimate of lambdaOne
        double[] start = {1.0};

        // Initial step size in maximization search
        double[] step = {0.3};

        // Tolerance for maximization search termination
        double maxzTol = 1e-9;

        // Maximiztaion of the Gaussian probabilty plot correlation coefficient varying lambdaOne
        max.nelderMead(bcf, start, step, maxzTol);

        // coeff[0] = value of lambdaOne for a maximum Gaussian probabilty plot correlation coefficient
        double[] coeff = max.getParamValues();
        double lambda1 = coeff[0];

        //maximum Gaussian probabilty plot correlation coefficient
        double sampleR1 = max.getMaximum();

        // Repeat maximization starting equidistant from the final value of lambdaOne on the opposite side from the starting estimate
        start[0] = lambda1 - (start[0] - lambda1);
        max.nelderMead(bcf, start, step, maxzTol);
        coeff = max.getParamValues();
        this.lambdaOne = coeff[0];
        this.transformedSampleR =  max.getMaximum();

        // Choose solution with the largest Gaussian probabilty plot correlation coefficient
        if(sampleR1>this.transformedSampleR){
            this.transformedSampleR = sampleR1;
            this.lambdaOne = lambda1;
        }

        // Store transformed data
        this.transformedData = new double[this.nData];
        if(this.lambdaOne==0.0){
            for(int i=0; i<this.nData; i++){
                this.transformedData[i] = Math.exp(this.shiftedStandardizedOriginalData[i]);
            }
        }
        else{
            for(int i=0; i<this.nData; i++){
                this.transformedData[i] = (Math.pow(this.shiftedStandardizedOriginalData[i], this.lambdaOne) - 1.0)/this.lambdaOne;
            }
        }

        // Standardize transformed data
        this.standardizedTransformedData =  (new Stat(this.transformedData)).standardize();

        // Calculate standardized transformed data statistics
        this.standardizedTransformedDataStatistics(this.standardizedTransformedData);

        // Obtain the intercept and gradient of the Gaussian probabilty plot
        ArrayMaths st4 = new ArrayMaths(this.standardizedTransformedData);
        st4 = st4.sort();
        double[] ordered = st4.array();
        Regression reg = new Regression(this.gaussianOrderMedians, ordered);
        reg.linear();
        coeff = reg.getBestEstimates();
        this.transformedIntercept = coeff[0];
        this.transformedGradient = coeff[1];
        coeff = reg.getBestEstimatesErrors();
        this.transformedInterceptError = coeff[0];
        this.transformedGradientError = coeff[1];

        // Adust mean and standard deviation of the transformed data to match those of the entered data
        this.scaledTransformedData = Stat.scale(this.standardizedTransformedData, this.originalMean, this.originalStandardDeviation);

        // Calculate transformed data statistics
        transformedDataStatistics(this.scaledTransformedData);

        // end of method and return transformed data
        this.transformDone = true;
        return this.transformedData;

    }

    // Calculate transformed data statistics
    private void transformedDataStatistics(double[] data){
        // Calculate transformed data statistics
        Stat st2 = new Stat(data);
        this.transformedMinimum = st2.minimum();
        this.transformedMaximum = st2.maximum();
        this.transformedMedian = st2.median();
        this.transformedRange = this.transformedMaximum - this.transformedMinimum;
        this.transformedMean = st2.mean();
        this.transformedStandardDeviation = st2.standardDeviation();
        this.transformedVariance = st2.variance();
        this.transformedMomentSkewness = st2.momentSkewness();
        this.transformedMedianSkewness = st2.medianSkewness();
        this.transformedQuartileSkewness = st2.quartileSkewness();
        this.transformedExcessKurtosis = st2.excessKurtosis();

        // Arrange scaled transformed data into ascending order
        Stat st5 = new Stat(data);
        this.sortedScaledTransformedData = (st5.sort()).array();
    }

   // Calculate standardized transformed data statistics
   private void standardizedTransformedDataStatistics(double[] data){
       // Calculate standardized transformed data statistics
        Stat st3 = new Stat(data);
        this.standardizedTransformedMinimum = st3.minimum();
        this.standardizedTransformedMaximum = st3.maximum();
        this.standardizedTransformedMedian = st3.median();
        this.standardizedTransformedRange = this.standardizedTransformedMaximum - this.standardizedTransformedMinimum;
        this.standardizedTransformedMean = 0.0;
        this.standardizedTransformedStandardDeviation = 1.0;
        this.standardizedTransformedVariance = 1.0;
        this.standardizedTransformedMomentSkewness = st3.momentSkewness();
        this.standardizedTransformedMedianSkewness = st3.medianSkewness();
        this.standardizedTransformedQuartileSkewness = st3.quartileSkewness();
        this.standardizedTransformedExcessKurtosis = st3.excessKurtosis();
    }


    // INVERSE BOX-COX TRANSFORMATION
    // Inverse transform, lambdaOne and lambdaTwo provided
    public double[] inverseTransform(double lambdaOne, double lambdaTwo){
        this.lambdaOne = lambdaOne;
        this.lambdaTwo = lambdaTwo;

        // initialize arrays and analyse original data
        if(!this.initializationDone)this.initialize();
        if(this.originalData==null)throw new IllegalArgumentException("No data has been entered (via a constructor)");
        this.inverseData = new double[this.nData];

        // Check for negative values tat will not inverse transform
        double[] shiftedData = Conv.copy(this.originalData);
        if(this.originalMinimum<0.0){
            if(Fmath.isNaN(Math.pow((this.originalMinimum*this.lambdaOne + 1.0), 1.0/this.lambdaOne))){
                this.lambdaThree = -0.999/this.lambdaOne - this.originalMinimum;
                for(int i=0; i<this.nData; i++)shiftedData[i] += this.lambdaThree;
            }
        }

        // inverse transform
        if(this.lambdaOne==0.0){
            for(int i=0; i<this.nData; i++){
                this.inverseData[i] = Math.exp(shiftedData[i]) - this.lambdaTwo;
            }
        }
        else{
            for(int i=0; i<this.nData; i++){
                this.inverseData[i] = Math.pow((shiftedData[i]*this.lambdaOne + 1.0), 1.0/this.lambdaOne) - this.lambdaTwo;
            }
        }

        // Calculate inverse transformed data statistics
        this.transformedData = Conv.copy(this.inverseData);
        transformedDataStatistics(inverseData);

        // Standardize transformed data
        this.standardizedTransformedData =  (new Stat(this.transformedData)).standardize();

        // Calculate standardized transformed data statistics
        this.standardizedTransformedDataStatistics(this.standardizedTransformedData);

        this.inverseDone = true;

        return this.inverseData;
    }

    // Inverse transform, lambdaOne provided, lambdaTwo = 0
    public double[] inverseTransform(double lambdaOne){
        return this.inverseTransform(lambdaOne, 0.0);
    }



    // FIXED VALUE BOX-COX TRANSFORMATION
    // Fixed value transform, lambdaOne and lambdaTwo provided
    public double[] fixedValueTransform(double lambdaOne, double lambdaTwo){
        this.lambdaOne = lambdaOne;
        this.lambdaTwo = lambdaTwo;

        // initialize arrays and analyse original data
        if(!this.initializationDone)this.initialize();
        if(this.originalData==null)throw new IllegalArgumentException("No data has been entered (via a constructor)");

        // check for negative values
        if((this.originalMinimum + this.lambdaTwo)<0.0)throw new IllegalArgumentException("Negative (data plus lambdaTwo) value, " +  (this.originalMinimum + this.lambdaTwo));

        // transform
        this.transformedData = new double[this.nData];
        if(this.lambdaOne==0.0){
            for(int i=0; i<this.nData; i++){
                this.transformedData[i] = Math.exp(this.shiftedStandardizedOriginalData[i]);
            }
        }
        else{
            for(int i=0; i<this.nData; i++){
                this.transformedData[i] = (Math.pow(this.shiftedStandardizedOriginalData[i], this.lambdaOne) - 1.0)/this.lambdaOne;
            }
        }

        // Standardize transformed data
        this.standardizedTransformedData =  (new Stat(this.transformedData)).standardize();

        // Calculate standardized transformed data statistics
        this.standardizedTransformedDataStatistics(this.standardizedTransformedData);

        // Obtain the intercept and gradient of the Gaussian probabilty plot
        ArrayMaths st4 = new ArrayMaths(this.standardizedTransformedData);
        st4 = st4.sort();
        double[] ordered = st4.array();
        Regression reg = new Regression(this.gaussianOrderMedians, ordered);
        reg.linear();
        double[] coeff = reg.getBestEstimates();
        this.transformedIntercept = coeff[0];
        this.transformedGradient = coeff[1];

        // Adust mean and standard deviation of the transformed data to match those of the entered data
        this.scaledTransformedData = Stat.scale(this.standardizedTransformedData, this.originalMean, this.originalStandardDeviation);

        // Calculate transformed data statistics
        this.transformedDataStatistics(this.scaledTransformedData);

        // end of method and return transformed data
        this.transformDone = true;
        return this.transformedData;

    }

    // Fixed value transform, lambdaOne and lambdaTwo provided
    public double[] fixedValueTransform(double lambdaOne){
        return this.fixedValueTransform(lambdaOne, 0.0);
    }



    // RETURN RESULTS
    // Return lambdaOne
    public double lambdaOne(){
        if(!this.transformDone)this.transform();
        return this.lambdaOne;
    }

    // Return lambdaTwo
    public double lambdaTwo(){
        if(!this.transformDone)this.transform();
        return this.lambdaTwo;
    }

    // Return lambdaThree
    public double lambdaThree(){
        if(!this.inverseDone){
            System.out.println("BoxCox: method lambdaThree: no inverse transform has been performed, zero returned");
        }
        return this.lambdaThree;
    }

    // Return Gaussian probabilty plot correlation coefficient of the transformed data
    public double transformedCorrelationCoefficient(){
        if(!this.transformDone)this.transform();
        return this.transformedSampleR;
    }

    // Return Gaussian probabilty plot gradient of the transformed data
    public double transformedGradient(){
        if(!this.transformDone)this.transform();
        return this.transformedGradient;
    }

    // Return estimated error of the Gaussian probabilty plot gradient of the transformed data
    public double transformedGradientError(){
        if(!this.transformDone)this.transform();
        return this.transformedGradientError;
    }

    // Return Gaussian probabilty plot intercept  of the transformed data
    public double transformedIntercept(){
        if(!this.transformDone)this.transform();
        return this.transformedIntercept;
    }

    // Return estimated error of the Gaussian probabilty plot intercept of the transformed data
    public double transformedInterceptError(){
        if(!this.transformDone)this.transform();
        return this.transformedInterceptError;
    }

    // Return Gaussian probabilty plot correlation coefficient of the original data
    public double originalCorrelationCoefficient(){
        if(!this.initializationDone)this.initialize();
        return this.originalSampleR;
    }

    // Return Gaussian probabilty plot gradient of the orioginal data
    public double originalGradient(){
        if(!this.initializationDone)this.initialize();
        return this.originalGradient;
    }

    // Return estimated error of the Gaussian probabilty plot gradient of the orioginal data
    public double originalGradientError(){
        if(!this.initializationDone)this.initialize();
        return this.originalGradientError;
    }

    // Return Gaussian probabilty plot intercept  of the original data
    public double originalIntercept(){
        if(!this.initializationDone)this.initialize();
        return this.originalIntercept;
    }

        // Return estimated error of the Gaussian probabilty plot intercept of the orioginal data
    public double originalInterceptError(){
        if(!this.initializationDone)this.initialize();
        return this.originalInterceptError;
    }

    // Return original data
    public double[] originalData(){
        if(!this.initializationDone)this.initialize();
        return this.originalData;
    }

    // Return standardized original data
    public double[] standardizedOriginalData(){
        if(!this.initializationDone)this.initialize();
        return this.standardizedOriginalData;
    }

    // Return ordered original data
    public double[] sortedOriginalData(){
        if(!this.initializationDone)this.initialize();
        return this.sortedOriginalData;
    }

    // Return shifted standardized original data
    public double[] shiftedStandardizedOriginalata(){
        if(!this.initializationDone)this.initialize();
        return this.shiftedStandardizedOriginalData;
    }

    // Return transformed data
    public double[] transformedData(){
        if(!this.transformDone)this.transform();
        return this.transformedData;
    }

    // Return scaled transformed data
    public double[] scaledTransformedData(){
        if(!this.transformDone)this.transform();
        return this.scaledTransformedData;
    }

    // Return standardized transformed data
    public double[] standardizedTransformedData(){
        if(!this.transformDone)this.transform();
        return this.standardizedTransformedData;
    }

    // Return ordered transformed data
    public double[] orderedTransformedData(){
        if(!this.transformDone)this.transform();
        ArrayMaths am = new ArrayMaths(this.transformedData);
        double[] ordered = (am.sort()).array();
        return ordered;
    }

    // Return ordered scaled transformed data
    public double[] orderedScaledTransformedData(){
        if(!this.transformDone)this.transform();
        return this.sortedScaledTransformedData;

    }

    // Return original data mean
    public double originalMean(){
        if(!this.initializationDone)this.initialize();
        return this.originalMean;
    }

    // Return original data median
    public double originalMedian(){
        if(!this.initializationDone)this.initialize();
        return this.originalMedian;
    }

    // Return original data standard deviation
    public double originalStandardDeviation(){
        if(!this.initializationDone)this.initialize();
        return this.originalStandardDeviation;
    }

    // Return original data standard error of the mean
    public double originalStandardError(){
        if(!this.initializationDone)this.initialize();
        return this.originalStandardDeviation/Math.sqrt(this.nData);
    }

    // Return original data variance
    public double originalVariance(){
        if(!this.initializationDone)this.initialize();
        return this.originalVariance;
    }

    // Return original data moment skewness
    public double originalMomentSkewness(){
        if(!this.initializationDone)this.initialize();
        return this.originalMomentSkewness;
    }

    // Return original data median skewness
    public double originalMedianSkewness(){
        if(!this.initializationDone)this.initialize();
        return this.originalMedianSkewness;
    }

    // Return original data quartile skewness
    public double originalQuartiletSkewness(){
        if(!this.initializationDone)this.initialize();
        return this.originalQuartileSkewness;
    }

    // Return original data excess kurtosis
    public double originalExcessKurtosis(){
        if(!this.initializationDone)this.initialize();
        return this.originalExcessKurtosis;
    }

    // Return original data maximum
    public double originalMaximum(){
        if(!this.initializationDone)this.initialize();
        return this.originalMaximum;
    }

    // Return original data minimum
    public double originalMinimum(){
        if(!this.initializationDone)this.initialize();
        return this.originalMinimum;
    }

    // Return original data range
    public double originalRange(){
        if(!this.initializationDone)this.initialize();
        return this.originalRange;
    }


    // Return transformed data mean
    public double transformedMean(){
        if(!this.transformDone)this.transform();
        return this.transformedMean;
    }

    // Return transformed data median
    public double transformedMedian(){
        if(!this.transformDone)this.transform();
        return this.transformedMedian;
    }


    // Return transformed data standard deviation
    public double transformedStandardDeviation(){
        if(!this.transformDone)this.transform();
        return this.transformedStandardDeviation;
    }

    // Return transformed data standard error of the mean
    public double transformedStandardError(){
        if(!this.transformDone)this.transform();
        return this.transformedStandardDeviation/Math.sqrt(this.nData);
    }

    // Return transformed data variance
    public double transformedVariance(){
        if(!this.transformDone)this.transform();
        return this.transformedVariance;
    }

    // Return transformed data moment skewness
    public double transformedMomentSkewness(){
        if(!this.transformDone)this.transform();
        return this.transformedMomentSkewness;
    }

    // Return transformed data median skewness
    public double transformedMedianSkewness(){
        if(!this.transformDone)this.transform();
        return this.transformedMedianSkewness;
    }

    // Return transformed data quartile skewness
    public double transformedQuartileSkewness(){
        if(!this.transformDone)this.transform();
        return this.transformedQuartileSkewness;
    }

    // Return transformed data excess kurtosis
    public double transformedExcessKurtosis(){
        if(!this.transformDone)this.transform();
        return this.transformedExcessKurtosis;
    }

    // Return transformed data maximum
    public double transformedMaximum(){
        if(!this.transformDone)this.transform();
        return this.transformedMaximum;
    }

    // Return transformed data minimum
    public double transformedMinimum(){
        if(!this.transformDone)this.transform();
        return this.transformedMinimum;
    }

    // Return transformed data range
    public double transformedRange(){
        if(!this.transformDone)this.transform();
        return this.transformedRange;
    }

    // PLOTS
    // Gaussian probabilty plot of the standardized transformed data
    public void transformedProbabilityPlot(){
        if(!this.transformDone)this.transform();

        double[][] data = PlotGraph.data(2,this.nData);
        data[0] = this.gaussianOrderMedians;
        data[1] = ((new ArrayMaths(this.standardizedTransformedData)).sort()).array();

        data[2] = this.gaussianOrderMedians;
        for(int i=0; i<this.nData; i++){
            data[3][i] = this.transformedIntercept + this.transformedGradient*this.gaussianOrderMedians[i];
        }
        PlotGraph pg = new PlotGraph(data);
        int[] points = {4, 0};
        pg.setPoint(points);
        int[] lines = {0, 3};
        pg.setLine(lines);
        pg.setXaxisLegend("Gaussian [0,1] Order Statistic Medians");
        pg.setYaxisLegend("Ordered Response Values");
        String legend1 = "Gausssian probability plot:  Box-Cox transformed data";
        String legend2 = "lambdaOne = " + Fmath.truncate(this.lambdaOne, 4) + ",  lambdaTwo = " + Fmath.truncate(this.lambdaTwo, 4) + ",   gradient = " + Fmath.truncate(this.transformedGradient, 4) + ", intercept = "  +  Fmath.truncate(this.transformedIntercept, 4) + ",  R = " + Fmath.truncate(this.transformedSampleR, 4);
        pg.setGraphTitle(legend1);
        pg.setGraphTitle2(legend2);
        pg.plot();
    }

    // Gaussian probabilty plot of the standardized original data
    public void originalProbabilityPlot(){
        if(!this.initializationDone)this.initialize();

        double[][] data = PlotGraph.data(2,this.nData);
        data[0] = this.gaussianOrderMedians;
        data[1] = ((new ArrayMaths(this.standardizedOriginalData)).sort()).array();
        data[2] = this.gaussianOrderMedians;
        for(int i=0; i<this.nData; i++){
            data[3][i] = this.originalIntercept + this.originalGradient*this.gaussianOrderMedians[i];
        }
        PlotGraph pg = new PlotGraph(data);
        int[] points = {4, 0};
        pg.setPoint(points);
        int[] lines = {0, 3};
        pg.setLine(lines);
        pg.setXaxisLegend("Gaussian [0,1] Order Statistic Medians");
        pg.setYaxisLegend("Ordered Response Values");
        String legend1 = "Gausssian probability plot: original data for a Box-Cox transformation";
        String legend2 = "gradient = " + Fmath.truncate(this.originalGradient, 4) + ", intercept = "  +  Fmath.truncate(this.originalIntercept, 4) + ",  R = " + Fmath.truncate(this.originalSampleR, 4);
        pg.setGraphTitle(legend1);
        pg.setGraphTitle2(legend2);
        pg.plot();
    }

    // Output analysis to text
    public void analysis(){
        this.analysis("BoxCoxAnalysis.txt");
    }

    public void analysis(String title){
        if(!this.transformDone)this.transform();

        this.originalProbabilityPlot();
        this.transformedProbabilityPlot();

        int posdot = title.indexOf(".");
        if(posdot==-1)title = title + ".txt";

        FileOutput fout = new FileOutput(title);
        fout.println("Box-Cox Analysis");
        fout.println("File name:   " + title);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();

        int field1 = 30;
        int field2 = 15;
        fout.print("Box-Cox lambda one", field1);
        fout.println(Fmath.truncate(this.lambdaOne, 4));
        fout.print("Box-Cox lambda two", field1);
        fout.println(Fmath.truncate(this.lambdaTwo, 4));
        fout.println();

        fout.print("  ", field1);
        fout.print("Transformed", field2);
        fout.print("  ", field2);
        fout.println("Original   ");
        fout.print("  ", field1);
        fout.print("scaled data", field2);
        fout.print("  ", field2);
        fout.println("data   ");
        fout.println();

        fout.print("                            ", field1);
        fout.print("Value", field2);
        fout.print("Error", field2);
        fout.print("Value", field2);
        fout.println("Error");
        fout.println();

        fout.println("Gaussian Probability plot ");
        fout.print("  Correlation coefficient", field1);
        fout.print(Fmath.truncate(this.transformedSampleR, 4), field2);
        fout.print(" ", field2);
        fout.println(Fmath.truncate(this.originalSampleR, 4));

        fout.print("  Gradient", field1);
        fout.print(Fmath.truncate(this.transformedGradient, 4), field2);
        fout.print(Fmath.truncate(this.transformedGradientError, 4), field2);
        fout.print(Fmath.truncate(this.originalGradient, 4), field2);
        fout.println(Fmath.truncate(this.originalGradientError, 4));

        fout.print("  Intercept", field1);
        fout.print(Fmath.truncate(this.transformedIntercept, 4), field2);
        fout.print(Fmath.truncate(this.transformedInterceptError, 4), field2);
        fout.print(Fmath.truncate(this.originalIntercept, 4), field2);
        fout.println(Fmath.truncate(this.originalInterceptError, 4));
        fout.println();

        fout.print("Data ");
        fout.println();
        fout.print("  Mean", field1);
        fout.print(Fmath.truncate(this.transformedMean, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMean, 4));

        fout.print("  Median", field1);
        fout.print(Fmath.truncate(this.transformedMedian, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMedian, 4));

        fout.print("  Standard deviation", field1);
        fout.print(Fmath.truncate(this.transformedStandardDeviation, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalStandardDeviation, 4));

        fout.print("  Standard error", field1);
        fout.print(Fmath.truncate(this.transformedStandardDeviation/Math.sqrt(this.nData), 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalStandardDeviation/Math.sqrt(this.nData), 4));

        fout.print("  Moment skewness", field1);
        fout.print(Fmath.truncate(this.transformedMomentSkewness, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMomentSkewness, 4));

        fout.print("  Median skewness", field1);
        fout.print(Fmath.truncate(this.transformedMedianSkewness, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMedianSkewness, 4));

        fout.print("  Quartile skewness", field1);
        fout.print(Fmath.truncate(this.transformedQuartileSkewness, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalQuartileSkewness, 4));

        fout.print("  Excess kurtosis", field1);
        fout.print(Fmath.truncate(this.transformedExcessKurtosis, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalExcessKurtosis, 4));

        fout.print("  Minimum", field1);
        fout.print(Fmath.truncate(this.transformedMinimum, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMinimum, 4));

        fout.print("  Maximum", field1);
        fout.print(Fmath.truncate(this.transformedMaximum, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalMaximum, 4));

        fout.print("  Range", field1);
        fout.print(Fmath.truncate(this.transformedRange, 4), field2);
        fout.print("  ", field2);
        fout.println(Fmath.truncate(this.originalRange, 4));

        fout.close();
    }

}

// Function for Box-Cox transform maximization
class BoxCoxFunction implements MaximizationFunction{

    public double[] shiftedData = null;
    public int nData = 0;
    public double[] yTransform = null;
    public double[] gaussianOrderMedians = null;
    public Regression reg = null;
    public ArrayMaths am = null;
    public Stat st = null;

    public double function( double[] x){

        // Box-Cox transform
        if(x[0]==0){
            for(int i=0; i<nData; i++)yTransform[i] = Math.log(shiftedData[i]);
        }
        else{
            for(int i=0; i<nData; i++)yTransform[i] = (Math.pow(shiftedData[i], x[0]) - 1.0)/x[0];
        }

        // Sort transformed array into ascending order
        am = new ArrayMaths(yTransform);
        am = am.sort();
        double[] yTrans = am.array();

        // Standardize
        st = new Stat(yTrans);
        yTransform = st.standardize();

        // Calculate and return probability plot correlation coefficient
        reg = new Regression(gaussianOrderMedians, yTransform);
        reg.linear();
        return reg.getSampleR();

    }

}
