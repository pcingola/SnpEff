/*
*   Class ImmunoAssay
*
*   Contains methods for fitting immunoassay results to a choice of equations,
*   for comparing the fit between equations and for obtaining the
*   analyte concentration for a given assay response
*
*   This class is a subclass of Regression
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    20 January - 7 February 2011
*   MODIFIED:   17-28 February 2011, 1 March 2011, 11 March 2011, 30 March 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ImmunoAssay.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
* Copyright (c) 2011 Michael Thomas Flanagan
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

package flanagan.physchem;

import flanagan.physchem.ImmunoChemistry;
import flanagan.math.Conv;
import flanagan.math.Fmath;
import flanagan.math.ArrayMaths;
import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;
import flanagan.analysis.Stat;
import flanagan.interpolation.CubicSpline;
import flanagan.interpolation.CubicInterpolation;
import flanagan.interpolation.LinearInterpolation;
import flanagan.plot.PlotGraph;
import flanagan.io.FileInput;
import flanagan.io.FileOutput;
import flanagan.io.FileChooser;
import flanagan.io.Db;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.io.File;


public class ImmunoAssay extends Regression{

    private double[] analyteConcns = null;                      // analyte concentrations
    private double[] log10AnalyteConcns = null;                 // log10(analyte concentrations)
    private double[] logeAnalyteConcns = null;                  // loge(analyte concentrations)
    private boolean analyteEntered = false;                     // = true when analyte concentrations have been entered
    private int analyteConcnFlag = 0;                           // = 0: analyte entered as analyte concentrations
                                                                // = 1: analyte entered as log10(analyte concentrations)
                                                                // = 2: analyte entered as loge(analyte concentrations)

    private int nAnalyteConcns = 0;                             // number of entered standard analyte concentrations

    private double[] responses = null;                          // standard curve responses
    private double[] log10Responses = null;                     // log10(standard curve responses)
    private double[] logeResponses = null;                      // loge(standard curve responses)
    private boolean responsesEntered = false;                   // = true when standard curve responses have been entered
    private int nResponses = 0;                                 // number of entered responses
    private int responsesFlag = 0;                              // = 0: responses entered as responses
                                                                // = 1: responses as log10(responses)
                                                                // = 2: responses as loge(responses)
    private boolean responsesPlot = true;                       // = false if log needed and responses are <= 0
    private double responsesMax = 0;                            // maximum response value
    private double responsesMin = 0;                            // minimum response value
    private double responsesRange = 0;                          // response value range

    private double[] weights = null;                            // standard curve weights as entered
    private boolean weightsEntered = false;                     // = true when standard curve weights have been entered
    private int nWeights = 0;                                   // number of entered weights
    private double weightsMean = 0.0;                           // mean of the weights
    private double weightsSD = 0.0;                             // standard deviation of the weights
    private int weightOption = 0;                               // = 0: no weights or weights as entered
                                                                // = 1: weights proportional to the responses
                                                                // = 2: weights proportional to the square root of the responses

    private boolean setDataOneDone = false;                     // = true when setDataOne has been successfully called

    private double[] interpolationConcns = null;                // analyte concentrations for standard curve responses after fitting or interpolation
    private double[] calculatedResponses = null;                // calculated standard curve responses after fitting or interpolation
    private String[] propagatedErrors = null;                   // propagated errors in the calculation of concentrations for given responses
    private int nInterp = 1000;                                 // number of points in the fitted or interpolation response curve

    private CubicSpline interp = null;                          // Cubic spline instance for getSampleConcn method
    private LinearInterpolation linterp = null;                 // LinearInterpolation instance for getSampleConcn method
    private CubicSpline errorp = null;                          // Cubic spline instance for getSampleConcn error method
    private CubicSpline cs = null;                              // Cubic spline instance for CubicSpline method
    private CubicInterpolation ci = null;                       // Cubic interpolation instance for CubicInterpolation method
    private LinearInterpolation li = null;                      // Linear interpolation instance for LinearInterpolation method

    private String titleZero = null;                            // Assay title
    private String titleOne = null;                             // Plot and print title - line two
    private String filename = "ImmunoAssayOutput.txt";          // Default output file name
    private String dataFilename = null;                         // Data input file name
    private boolean dataRead = false;                           // = true if data read from a file

    private double interpAnalyteStart = 0.0;                    // start of the non-ambiguous analyte concentration range of the fitted standard curve
    private double interpAnalyteEnd = 0.0;                      // end of the non-ambiguous analyte concentration range of the fitted standard curve
    private double interpResponseStart = 0.0;                   // start of the non-ambiguous range of the fitted standard curve
    private double interpResponseEnd = 0.0;                     // end of the non-ambiguous response range of the fitted standard curve
    private int interpStartIndex = 0;                           // index of the start of the non-ambiguous response range of the fitted standard curve
    private int interpEndIndex = 0;                             // index of the end of the non-ambiguous range of the fitted standard curve
    private boolean ambigCheck = false;                         // = true if the fitted standard curve is not monotonic
    private boolean curveDirection = true;                      // = true if the responses increase with increasing analyte concentration
                                                                // = false if the responses decrease with increasing analyte concentration
    private double workingResponseMin = 0.0;                    // minimum of the working range of the fitted standard curve
    private double workingResponseMax = 0.0;                    // maximum of the working range of the fitted standard curve
    private double minimumAerror = 0.0;                         // minimum propagated sample analyte concentration error
    private double maximumAerror = 0.0;                         // maximum propagated sample analyte concentration error
    private double meanAerror = 0.0;                            // mean propagated sample analyte concentration error
    private double sdAerror = 0.0;                              // standard deviation of the propagated sample analyte concentration error

    private int nTurningPoints = 0;                             // number of turning points in the standard curve
    private int[] turnIndices = null;                           // indices of turning points in the standard curve
    private int nWorking = 0;                                   // number of data points in the unambiguous range

    private int polyDegree = 0;                                 // degree of polynomial in polynomial fitting
    private int bestPolyDegree = 0;                             // degree of polynomial in best polynomial fitting
    private int polyNterms = 0;                                 // number of terms in the non-integer polynomial fitting

    private String compFilename = "ImmunoAssayComparison.txt";  // Default comparison output file name
    private int resultFlag = -1;                                // comparison result flag
    private double significance = 0.05;                         // probability significance level in statistical tests

    private double sampleResponse = 0.0;                        // last entered sample response of unkown concentration
    private double sampleConcn = 0.0;                           // analyte concn found for last entered sample response of unkown concentration
    private double sampleError = 0.0;                           // error of the analyte concn found for last entered sample response of unkown concentration

    private int nPlot = 0;                                      // Number of entered data points included in the plotting
    private int plotOptions = 0;                                // = 0: analyte concn v. response
                                                                // = 1: log10(analyte concn) v. response
                                                                // = 2: loge(analyte concn) v. response
                                                                // = 3: log10(analyte concn) v. log10(response)
    private boolean supressPlot = false;                        // = true: plot display supressed
                                                                // = 4: loge(analyte concn) v. loge(response)

    private int nMethods = 15;                                  // Number of methods available
    private String[] methodNames = new String[nMethods];        // Method names array
    private int[] methodIndices = new int[nMethods]; ;          // Indices linking methodUsed (below) to displayed names

    private int methodUsed = 8;                                 // Fitting or interpolation method used
                                                                // = 0:  Cubic spline
                                                                // = 1:  Cubic interpolation
                                                                // = 2:  Polynomial of user supplied degree n
                                                                // = 3:  Best fit polynomial
                                                                // = 4:  Non-integer polynomial
                                                                // = 5:  Sigmoid threshold function
                                                                // = 6:  Sips sigmoid function
                                                                // = 7:  Four parameter logistic function (EC50 dose response curve)
                                                                // = 8:  Five parameter logistic function
                                                                // = 9:  Shifted rectangular hyperbola
                                                                // = 10: Rectangular hyperbola
                                                                // = 11: Amersham equation
                                                                // = 12: Four parameter logistic function (top and bottom fixed)
                                                                // = 13: Five parameter logistic function (top and bottom fixed)
                                                                // = 14: Linear interpolation

    private boolean sampleErrorFlag = true;                    // = false if method cannot calculate a sample concentration err9or

    private boolean amershamFlag1 = true;                       // = true:  Amersham model initial estimates calculated
                                                                // = false: Amersham model initial estimates supplied

    private double bottom = 0.0;                                // Logistic fixed method bottom value
    private double top = 0.0;                                   // Logistic fixed method top value

    private boolean compWindow = false;                         // = true if comparison choice window used

    private String[] outliers = null;                           // list of outlier indicator messages
    private double residualsMean = 0.0;                         // mean of the unweighted residuals
    private double residualsSD = 0.0;                           // standard deviation of the unweighted residuals
    private double confidenceLevel = 0.95;                      // Confidence level of the confidence interval used on outlier testing
    // Outlier test critical values
    // Sample size
    private double[] critSize = {3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 30, 40, 50, 60, 100, 120};
    // 99% level
    private double[] critValuesOne = {1.15, 1.49, 1.75, 1.94, 2.10, 2.22, 2.32, 2.41, 2.55, 2.66, 2.71, 2.75, 2.82, 2.88, 3.10, 3.24, 3.34, 3.41, 3.60, 3.66};
    // 95% level
    private double[] critValuesFive = {1.15, 1.46, 1.67, 1.82, 1.94, 2.03, 2.11, 2.18, 2.29, 2.37, 2.41, 2.44, 2.50, 2.56, 2.74, 2.87, 2.96, 3.03, 3.21, 3.27};
    private CubicSpline critValues = null;                      // critical value interpolation function
    private double anscombeC = 0.0;                             // Calculated critical value
    private boolean outlierFlag = false;                        // = true if possible outliers found

    private boolean degSet = false;                             // = true if polynomial degree set
    private boolean nTermsSet = false;                          // = true if polynomial nterms set
    private boolean fourBotTopSet = false;                      // = true if four parameter top and bottom set
    private boolean fiveBotTopSet = false;                      // = true if five parameter top and bottom set


    // CONSTRUCTORS

    // Constructor without title
    public ImmunoAssay(){
        this.titleZero = "Program ImmunoAssay";
        super.supressErrorMessages = true;
        this.methodList();
    }

    // Constructor with title
    public ImmunoAssay(String title){
        this.titleZero = "Program ImmunoAssay: " + title;
        super.supressErrorMessages = true;
        this.methodList();
    }

    // Constructor without title
    // with total analyte concns and arbitrary unit responses entered
    public ImmunoAssay(double[] analytes, double[] responses){
        this.titleZero = "Program ImmunoAssay";
        this.nAnalyteConcns = analytes.length;
        this.analyteConcns = Conv.copy(analytes);
        this.analyteConcnFlag = 0;
        this.analyteEntered = true;
        this.nResponses = responses.length;
        this.responses = Conv.copy(responses);
        this.responsesFlag = 0;
        this.responsesEntered = true;
        super.supressErrorMessages = true;
        this.methodList();
    }

    // Constructor without title
    // with total analyte concns, arbitrary unit responses and weights entered
    public ImmunoAssay(double[] analytes, double[] responses, double[] weights){
        this.titleZero = "Program ImmunoAssay";
        this.nAnalyteConcns = analytes.length;
        this.analyteConcns = Conv.copy(analytes);
        this.analyteConcnFlag = 0;
        this.analyteEntered = true;
        this.nResponses = responses.length;
        this.responses = Conv.copy(responses);
        this.responsesFlag = 0;
        this.responsesEntered = true;
        this.nWeights = weights.length;
        this.weights = this.checkWeights(Conv.copy(weights));
        this.weightsEntered = true;
        super.supressErrorMessages = true;
        this.methodList();
    }


    // Constructor with title
    // with total analyte concns and arbitrary unit responses entered
    public ImmunoAssay(double[] analytes, double[] responses, String title){
        this.titleZero = "Program ImmunoAssay: " + title;
        this.nAnalyteConcns = analytes.length;
        this.analyteConcns = Conv.copy(analytes);
        this.analyteConcnFlag = 0;
        this.analyteEntered = true;
        this.nResponses = responses.length;
        this.responses = Conv.copy(responses);
        this.responsesFlag = 0;
        this.responsesEntered = true;
        super.supressErrorMessages = true;
        this.methodList();
    }

    // Constructor with title
    // with total analyte concns, arbitrary unit responses and weights entered
    public ImmunoAssay(double[] analytes, double[] responses, double[] weights, String title){
        this.titleZero = "Program ImmunoAssay: " + title;
        this.nAnalyteConcns = analytes.length;
        this.analyteConcns = Conv.copy(analytes);
        this.analyteEntered = true;
        this.analyteConcnFlag = 0;
        this.nResponses = responses.length;
        this.responses = Conv.copy(responses);
        this.responsesFlag = 0;
        this.responsesEntered = true;
        this.nWeights = weights.length;
        this.weights = this.checkWeights(Conv.copy(weights));
        this.weightsEntered = true;
        super.supressErrorMessages = true;
        this.methodList();
    }

    // Construct an indexed method list
    private void methodList(){

        // initialise method names and method name indices

        //      methodUsed
        //      = 0:  Cubic spline
        //      = 1:  Cubic interpolation
        //      = 2:  Polynomial of user supplied degree n
        //      = 3:  Best fit polynomial
        //      = 4:  Non-integer polynomial
        //      = 5:  Sigmoid threshold function
        //      = 6:  Sips sigmoid function
        //      = 7:  Four parameter logistic function (EC50 dose response curve)
        //      = 8:  Five parameter logistic function
        //      = 9:  Shifted rectangular hyperbola
        //      = 10: Rectangular hyperbola
        //      = 11: Amersham equation
        //      = 12: Four parameter logistic function (top and bottom fixed)
        //      = 13: Five parameter logistic function (top and bottom fixed)
        //      = 14: Linear interpolation

        this.methodNames[0] = "CubicSpline";
        this.methodIndices[0] = 0;
        this.methodNames[1] = "Five parameter logistic function";
        this.methodIndices[1] = 13;
        this.methodNames[2] = "Five parameter logistic function (top and bottom fixed)";
        this.methodIndices[2] = 6;
        this.methodNames[3] = "Four parameter logistic function";
        this.methodIndices[3] = 5;
        this.methodNames[4] = "Four parameter logistic function (top and bottom fixed)";
        this.methodIndices[4] = 7;
        this.methodNames[5] = "Best fit polynomial";
        this.methodIndices[5] = 8;
        this.methodNames[6] = "Polynomial of user supplied degree ";
        this.methodIndices[6] = 9;
        this.methodNames[7] = "Non-integer polynomial";
        this.methodIndices[7] = 3;
        this.methodNames[8] = "Sigmoid threshold function";
        this.methodIndices[8] = 1;
        this.methodNames[9] = "Sips sigmoid function";
        this.methodIndices[9] = 10;
        this.methodNames[10] = "Shifted rectangular hyperbola";
        this.methodIndices[10] = 11;
        this.methodNames[11] = "Rectangular hyperbola";
        this.methodIndices[11] = 12;
        this.methodNames[12] = "Amersham equation";
        this.methodIndices[12] = 4;
        this.methodNames[13] = "Cubic interpolation";
        this.methodIndices[13] = 2;
        this.methodNames[14] = "Linear interpolation";
        this.methodIndices[14] = 14;

    }

    // ENTER DATA METHODS

    // Enter standard analyte concentrations
    public void enterAnalyteConcns(double[] concn){
        this.setDataOneDone = false;
        this.nAnalyteConcns = concn.length;
        this.analyteConcns = Conv.copy(concn);
        this.analyteConcnFlag = 0;
        this.analyteEntered = true;
    }

    // Enter standard analyte concentrations as log10(concn)
    public void enterAnalyteConcnsAsLog10(double[] logconcn){
        this.setDataOneDone = false;
        this.nAnalyteConcns = logconcn.length;
        this.log10AnalyteConcns = Conv.copy(logconcn);
        this.analyteConcns = this.antiLog10(logconcn);
        this.analyteConcnFlag = 1;
        this.analyteEntered = true;
    }

    // Enter standard analyte concentrations as loge(concn)
    public void enterAnalyteConcnsAsLogE(double[] logconcn){
        this.setDataOneDone = false;
        this.nAnalyteConcns = logconcn.length;
        this.logeAnalyteConcns = Conv.copy(logconcn);
        this.analyteConcns = this.antiLoge(logconcn);
        this.analyteConcnFlag = 2;
        this.analyteEntered = true;
    }

    // Enter standard curve responses
    public void enterResponses(double[] responses){
        this.setDataOneDone = false;
        this.nResponses = responses.length;
        this.responses = Conv.copy(responses);
        this.responsesFlag = 0;
        this.responsesEntered = true;
    }

    // Enter standard curve responses as log10
    public void enterResponsesAsLog10(double[] responses){
        this.setDataOneDone = false;
        this.nResponses = responses.length;
        this.log10Responses = Conv.copy(responses);
        this.responses = this.antiLog10(responses);
        this.responsesFlag = 1;
        this.responsesEntered = true;
    }

    // Enter standard curve responses as loge
    public void enterResponsesAsLogE(double[] responses){
        this.setDataOneDone = false;
        this.nResponses = responses.length;
        this.logeResponses = Conv.copy(responses);
        this.responses = this.antiLoge(responses);
        this.responsesFlag = 2;
        this.responsesEntered = true;
    }

    // Enter weighting factors
    public void enterWeights(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = this.checkWeights(Conv.copy(weights));
        this.weightsEntered = true;
    }

    // Enter weighting factors as log10
    public void enterWeightsAslog10(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = this.antiLog10(this.weights = this.checkWeights(Conv.copy(weights)));
        this.weightsEntered = true;
    }

    // Enter weighting factors as loge
    public void enterWeightsAslogE(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = this.antiLoge(this.checkWeights(Conv.copy(weights)));
        this.weightsEntered = true;
    }

    // Convert multiplicative weighting factors to uncertainty estimate weights
    public void enterMultiplicativeWeights(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = Conv.copy(this.checkWeights(Conv.copy(weights)));
        for(int i=0; i<this.nWeights; i++)this.weights[i] = 1.0/Math.abs(this.weights[i]);
        this.weightsEntered = true;
    }

    // Convert multiplicative weighting factors to uncertainty estimate weights as log10
    public void enterMultiplicativeWeightsAsLog10(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = this.antiLog10(this.checkWeights(Conv.copy(weights)));
        for(int i=0; i<this.nWeights; i++)this.weights[i] = 1.0/Math.abs(this.weights[i]);
        this.weightsEntered = true;
    }

    // Convert multiplicative weighting factors to uncertainty estimate weights as loge
    public void enterMultiplicativeWeightsAsLogE(double[] weights){
        this.setDataOneDone = false;
        this.nWeights = weights.length;
        this.weights = this.antiLoge(this.checkWeights(Conv.copy(weights)));
        for(int i=0; i<this.nWeights; i++)this.weights[i] = 1.0/Math.abs(this.weights[i]);
        this.weightsEntered = true;
    }

    // Set weights propotional to the responss
    public void setWeightsAsResponses(){
        this.weightsEntered = false;
        this.weightOption = 1;
        if(this.nResponses>0){
            this.weights = new double[this.nResponses];
            for(int i=0; i<this.nResponses; i++){
                this.weights[i] = Math.abs(this.responses[i]);
                this.weightsEntered = true;
            }
        }
    }

    // Set weights propotional to the square root of the responses
    public void setWeightsAsSqrtResponses(){
        this.weightsEntered = false;
        this.weightOption = 2;
        if(this.nResponses>0){
            this.weights = new double[this.nResponses];
            for(int i=0; i<this.nResponses; i++){
                this.weights[i] = Math.sqrt(Math.abs(this.responses[i]));
                this.weightsEntered = true;
            }
        }
    }

    // Enter title
    public void enterTitle(String title){
        this.titleZero = title;
    }

    // Read from file
    // Choose file from file chooser window
    public void readFromFile(){
        this.setDataOneDone = false;
        FileChooser fin = new FileChooser();
        this.dataFilename = fin.selectFile();
        this.read(fin);
    }

    // Read from file
    // file name supplied
    public void readFromFile(String filename){
        this.setDataOneDone = false;
        this.dataFilename = filename;
        FileInput fin = new FileInput(filename);
        this.read(fin);
    }

    // Find first separator
    private int separatorPosition(String line){
        line = line.trim();
        int pos = line.indexOf(':');
        if(pos==-1)pos = line.indexOf(';');
        if(pos==-1)pos = line.indexOf(',');
        if(pos==-1)pos = line.indexOf('\t');
        if(pos==-1)line.indexOf(' ');

        return pos;
    }

    // private common read from file method
    private void read(FileInput fin){
        this.nAnalyteConcns = fin.numberOfLines()-1;
        this.titleZero = fin.readLine();
        this.nResponses = this.nAnalyteConcns;
        this.analyteConcns = new double[this.nAnalyteConcns];
        this.responses = new double[this.nAnalyteConcns];
        this.weights = new double[this.nAnalyteConcns];
        int weightCheck = 0;
        String line = null;
        String word = null;
        int wL = 0;
        int pos1 = -1;
        int pos2 = -1;
        int pos3 = -1;
        for(int i=0; i<this.nAnalyteConcns; i++){
            line = (fin.readLine()).trim();
            // analyte concn
            pos1 = this.separatorPosition(line);
            if(pos1==-1){
                throw new IllegalArgumentException("Input file line " + (i+1) + ": analyte concentration and response value required for all data points");
            }
            else{
                word = line.substring(0, pos1);
                this.analyteConcns[i] = Double.parseDouble(word);
                line = (line.substring(pos1+1)).trim();;
                wL = line.length();
                if(wL<1)throw new IllegalArgumentException("Input file line " + (i+1) + ": response value required for all data points");

                // response
                pos2 = this.separatorPosition(line);
                if(pos2==-1){
                    word = line;
                }
                else{
                    word = line.substring(0, pos2);
                }
                this.responses[i] = Double.parseDouble(word);
                if(pos2!=-1){
                    line = (line.substring(pos2+1)).trim();
                    wL = line.length();

                    // weight
                    if(wL>0){
                        pos3 = this.separatorPosition(line);
                        if(pos3==-1){
                            word = line.trim();
                        }
                        else{
                            word = (line.substring(0, pos3)).trim();
                        }
                        this.weights[i] = Double.parseDouble(word);
                        if(this.weights[i]==1.0)weightCheck++;
                     }
                }
            }
        }
        this.analyteConcnFlag = 0;
        this.analyteEntered = true;
        this.responsesEntered = true;
        if(weightCheck!=this.nAnalyteConcns){
            this.nWeights = this.nAnalyteConcns;
            this.weights = this.checkWeights(Conv.copy(this.weights));
            this.weightsEntered = true;
        }
        this.dataRead = true;
    }

    // Check weights for zero and negative values
    private double[] checkWeights(double[] weights){

        // Make all weights positive and check if all zero
        int nZeros = 0;
        for(int i=0; i<this.nWeights; i++){
            weights[i] = Math.abs(weights[i]);
            if(weights[i]==0.0)nZeros++;
        }
        // if all zeros replace all by unity
        if(nZeros==this.nWeights){
            for(int i=0; i<this.nWeights; i++){
                weights[i] = 1.0;
            }
        }

        // Calculate mean and sd
        Stat stat= new Stat(weights);
        this.weightsMean = stat.mean();
        this.weightsSD = stat.standardDeviation();

        // check for zero and replace by mean/100
        for(int i=0; i<this.nWeights; i++){
            if(weights[i]==0.0){
                weights[i] = this.weightsMean/100.0;
                System.out.println("Weight at point " + i + " is zero; replaced by a value " + this.weightsMean/100.0);
            }
        }

        return weights;
    }

    // Return entered analyte concentration
    public double[] getAnalyteConcns(){
        return this.analyteConcns;
    }

    // Return entered responses
    public double[] getResponses(){
        return this.responses;
    }


    // Return entered weights
    public double[] getWeights(){
        if(!this.weightsEntered && this.weightOption>0 && this.nResponses>0){
            switch(this.weightOption){
                case 1: for(int i=0; i<this.nResponses; i++)this.weights[i] = Math.abs(this.responses[i]);
                        this.weightsEntered = true;
                        break;
                case 2: for(int i=0; i<this.nResponses; i++)this.weights[i] = Math.sqrt(Math.abs(this.responses[i]));
                        this.weightsEntered = true;
                        break;
            }
        }
        return this.weights;
    }

    // Return entered title
    public String getTitle(){
        return this.titleZero;
    }


    // INTERPOLATION METHODS

    // CubicSpline interpolation
    public void cubicSpline(){
        if(this.nAnalyteConcns<3)throw new IllegalArgumentException("Method cubicSpline requres at least 3 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 0;
        this.sampleErrorFlag = false;
        this.titleOne = "Cubic spline ";
        if(!this.setDataOneDone)this.setDataOne();
        this.cs = new CubicSpline(this.analyteConcns, this.responses);
        for(int i=0; i<this.nInterp; i++)this.calculatedResponses[i] = cs.interpolate(interpolationConcns[i]);
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Cubic interpolation
    public void cubicInterpolation(){
        if(this.nAnalyteConcns<2)throw new IllegalArgumentException("Method cubicInterpolation requres at least 2 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 1;
        this.sampleErrorFlag = false;
        this.titleOne = "Cubic interpolation ";
        if(!this.setDataOneDone)this.setDataOne();
        this.ci = new CubicInterpolation(this.analyteConcns, this.responses, 0);
        for(int i=0; i<this.nInterp; i++)this.calculatedResponses[i] = ci.interpolate(this.interpolationConcns[i]);
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Linear interpolation
    public void linearInterpolation(){
        if(this.nAnalyteConcns<2)throw new IllegalArgumentException("Method cubicInterpolation requres at least 2 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 14;
        this.sampleErrorFlag = false;
        this.titleOne = "Linear interpolation ";
        if(!this.setDataOneDone)this.setDataOne();
        this.li = new LinearInterpolation(this.analyteConcns, this.responses);
        for(int i=0; i<this.nInterp; i++)this.calculatedResponses[i] = li.interpolate(this.interpolationConcns[i]);
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // FIT TO AN EQUATION METHODS

    // Fit to a polynomial of degree n
    public void polynomialFit(int n){
        if(this.nAnalyteConcns<(n+2))throw new IllegalArgumentException("Method polynomialFit(" + n +") requres at least " + (n+2) + " data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 2;
        this.sampleErrorFlag = true;
        this.degSet = true;
        this.polyDegree = n;
        this.titleOne = "Polynomial fitting: r = c[0] + c[1].a +  c[1].a^2 + ... + c[n].a^n; degree (n) = " +  n;
        if(!this.setDataOneDone)this.setDataOne();
        super.polynomial(n);
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = 0.0;
            for(int j=0; j<=n; j++){
                this.calculatedResponses[i] +=  super.best[j]*Math.pow(this.interpolationConcns[i], j);
            }
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }


    // Fit to the best polynomial - returns degree used
    public int bestPolynomialFit(){
        this.methodUsed = 3;
        this.sampleErrorFlag = true;
        this.titleOne = "Best polynomial fitting: r = c[0] + c[1].a +  c[1].a^2 + ... + c[n].a^n; best fit degree (n) = ";
        if(!this.setDataOneDone)this.setDataOne();
        ArrayList<Object> al = super.bestPolynomial();
        this.bestPolyDegree = ((Integer)al.get(0)).intValue();
        this.titleOne += " " + this.bestPolyDegree;
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = 0.0;
            for(int j=0; j<=this.bestPolyDegree; j++){
                this.calculatedResponses[i] +=  super.best[j]*Math.pow(interpolationConcns[i], j);
            }
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
        return super.bestPolynomialDegree;
    }

    // Fit to a non-integer polynomial
    public void nonIntegerPolynomialFit(int polyNterms){
        if(this.nAnalyteConcns<(polyNterms+1))throw new IllegalArgumentException("Method nonIntegerPolynomial requres at least " + (polyNterms+1) + " data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 4;
        this.polyNterms = polyNterms;
        this.nTermsSet = true;
        this.sampleErrorFlag = true;
        this.titleOne = "Non-integer polynomial fitting: r = c[0] + c[1].a^c[n] + c[2].a^c[n+1] + ... + c[n].a^c[2n-1]";
        if(!this.setDataOneDone)this.setDataOne();
        super.nonIntegerPolynomial(polyNterms);
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = super.best[0];
            for(int j=1; j<this.polyNterms; j++){

                this.calculatedResponses[i] += super.best[j]*Math.pow(this.interpolationConcns[i], super.best[polyNterms+j-1]);
            }
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    public void nonIntegerPolynomialFit(){
        this.nonIntegerPolynomialFit(3);
    }


    // Fit to a sigmoid threshold function
    public void sigmoidThresholdFit(){
        if(this.nAnalyteConcns<4)throw new IllegalArgumentException("Method sigmoidThresholdFit requres at least 4 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 5;
        this.sampleErrorFlag = true;
        this.titleOne = "Sigmoid threshold fitting: r = A/(1 + exp(-alpha(a - theta)))";
        if(!this.setDataOneDone)this.setDataOne();
        super.sigmoidThreshold();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = super.best[2]/(1.0 + Math.exp(-super.best[0]*(this.interpolationConcns[i] - super.best[1])));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to a Sips sigmoid function
    public void sipsSigmoidFit(){
        if(this.nAnalyteConcns<4)throw new IllegalArgumentException("Method sipsSigmoidFit requres at least 4 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 6;
        this.sampleErrorFlag = true;
        this.titleOne = "Sips sigmoid fitting: r = Aa^n/(theta^n + a^n)";
        if(!this.setDataOneDone)this.setDataOne();
        super.addConstraint(0, -1, 0.0);
        super.sigmoidHillSips();
        super.removeConstraints();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = super.best[2]*Math.pow(this.interpolationConcns[i], super.best[1])/(Math.pow(super.best[0], super.best[1]) + Math.pow(this.interpolationConcns[i],super.best[1]));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to a four parameter logistic function (an EC50 dose respone function)
    public void fourParameterLogisticFit(){
        if(this.nAnalyteConcns<5)throw new IllegalArgumentException("Method fourParameterLogisticFit requres at least 5 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 7;
        this.sampleErrorFlag = true;
        this.titleOne = "Four parameter logistic fitting: r = top + (bottom - top)/(1 + (a/C50)^HillSlope)";
        if(!this.setDataOneDone)this.setDataOne();
        super.addConstraint(2, -1, 0.0);
        super.ec50();
        super.removeConstraints();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = super.best[0] + (super.best[1] - super.best[0])/(1.0 + Math.pow(this.interpolationConcns[i]/super.best[2], super.best[3]));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to an EC50 dose respone function (four parameter logistic)
    public void ec50Fit(){
        this.fourParameterLogisticFit();
    }

    // Fit to a four parameter logistic function (an EC50 dose respone function)
    // top and bottom fixed
    public void fourParameterLogisticFit(double bottom, double top){
        if(this.nAnalyteConcns<3)throw new IllegalArgumentException("Method fourParameterLogisticFit requres at least 3 data points; only " + this.nAnalyteConcns + " were supplied");
        this.bottom = bottom;
        this.top = top;
        this.methodUsed = 13;
        this.fourBotTopSet = true;
        this.sampleErrorFlag = true;
        this.titleOne = "Four parameter logistic fitting: r = top + (bottom - top)/(1 + (a/C50)^HillSlope) [top and bottom fixed]";
        if(!this.setDataOneDone)this.setDataOne();
        super.addConstraint(0, -1, 0.0);
        super.ec50(bottom, top);
        super.removeConstraints();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = top + (bottom - top)/(1.0 + Math.pow(this.interpolationConcns[i]/super.best[0], super.best[1]));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to an EC50 dose respone function (four parameter logistic)
    public void ec50Fit(double bottom, double top){
        this.fourParameterLogisticFit(bottom, top);
    }

    // Fit to a five parameter logistic function
    public void fiveParameterLogisticFit(){
        if(this.nAnalyteConcns<6)throw new IllegalArgumentException("Method fiveParameterLogisticFit requres at least 6 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 8;
        this.sampleErrorFlag = true;
        this.titleOne = "Five parameter logistic fitting: r = top + (bottom - top)/((1 + (a/C50)^HillSlope)^asymm)";
        if(!this.setDataOneDone)this.setDataOne();
        super.addConstraint(2, -1, 0.0);
        super.fiveParameterLogistic();
        super.removeConstraints();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = super.best[0] + (super.best[1] - super.best[0])/(Math.pow((1.0 + Math.pow(this.interpolationConcns[i]/super.best[2], super.best[3])), super.best[4]));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

        // Fit to a five parameter logistic function
    public void fiveParameterLogisticFit(double bottom, double top){
        if(this.nAnalyteConcns<5)throw new IllegalArgumentException("Method fiveParameterLogisticFit requres at least 5 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 13;
        this.sampleErrorFlag = true;
        this.bottom = bottom;
        this.top = top;
        this.fiveBotTopSet = true;
        this.titleOne = "Five parameter logistic fitting: r = top + (bottom - top)/((1 + (a/C50)^HillSlope)^asymm) [top and bottom fixed]";
        if(!this.setDataOneDone)this.setDataOne();
        super.addConstraint(0, -1, 0.0);
        super.fiveParameterLogistic(bottom, top);
        super.removeConstraints();
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = top + (bottom - top)/(Math.pow((1.0 + Math.pow(this.interpolationConcns[i]/super.best[0], super.best[1])), super.best[2]));
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to a shifted rectangular hyperbola
    public void shiftedRectangularHyperbolaFit(){
        if(this.nAnalyteConcns<4)throw new IllegalArgumentException("Method shiftedRectangularHyperbolaFit requres at least 4 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 9;
        this.sampleErrorFlag = true;
        this.titleOne = "Rectangular hyperbola fitting: r = A.a/(theta + a) + alpha)";
        if(!this.setDataOneDone)this.setDataOne();
        super.shiftedRectangularHyperbola();
        for(int i=0; i<this.nInterp; i++){
                this.calculatedResponses[i] = best[2]*this.interpolationConcns[i]/(best[0] + this.interpolationConcns[i]) + best[1];
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Fit to a rectangular hyperbola
    public void rectangularHyperbolaFit(){
        if(this.nAnalyteConcns<3)throw new IllegalArgumentException("Method rectangularHyperbolaFit requres at least 3 data points; only " + this.nAnalyteConcns + " were supplied");
        this.methodUsed = 10;
        this.sampleErrorFlag = true;
        this.titleOne = "Rectangular hyperbola fitting: r = A.a/(theta + a))";
        if(!this.setDataOneDone)this.setDataOne();
        super.rectangularHyperbola();
        for(int i=0; i<this.nInterp; i++){
                this.calculatedResponses[i] = best[1]*this.interpolationConcns[i]/(best[0] + this.interpolationConcns[i]);
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // FIT TO A MASS ACTION EQUATION

    // Fit to Amersham Equation
    // Initial estimates calculated internally
    public void amershamFit(){
        this.methodUsed = 11;
        if(!this.setDataOneDone)this.setDataOne();

        // Check that it is a competitive assay signal
        this.isCompetitive();

        // call common Amersham method
        this.amershamFlag1 = true;
        double initEst[] = {0.0, 0.0, 0.0, 0.0, 0.0};

        this.fitAmersham(initEst);
    }

    // Fit to Amersham Equation
    // initial estimates supplied
    public void amershamFit(double[] estimates){

        this.methodUsed = 11;
        if(!this.setDataOneDone)this.setDataOne();

        // Check that it is a competitive assay signal
        this.isCompetitive();

        // call common Amersham method
        this.amershamFlag1 = false;
        this.fitAmersham(estimates);
    }

    // Common Amersham equation fitting method
    private void fitAmersham(double[] estimates){
        if(this.nAnalyteConcns<6)throw new IllegalArgumentException("Method amershamFit requres at least 6 data points; only " + this.nAnalyteConcns + " were supplied");
        this.sampleErrorFlag = true;
        this.titleOne = "Amersham equation fitting: r = S(2.P(1-N/L)/(K+P+L+a+[(K-P+L+a)^2+4KP]^0.5)+N/L)";

        // Create an instance of Amersham
        Amersham am = new Amersham();

        // Initial estimates
        double[] start = new double[5];             // initial estimates
        double[] step = new double[5];              // ionitial step sizes

        double kGuess = 0.0;                        // guess at K
        double labelGuess = 0.0;                    // guess at the labelled analyte concentration
        double pGuess = 0.0;                        // guess at the antibody concentration
        double nsbGuess = 0.0;                      // guess at the non-specific binding
        double amScaleGuess = this.responsesMax;    // guess at the scaling factor

        if(this.amershamFlag1){
            // Calculted initial estimates

            // Find half total signal concentration as a guess at the labelled analyte concentration
            boolean foundFlag = false;
            for(int i=0; i<this.nAnalyteConcns; i++){
                if(this.responses[i]<this.responsesMax/2.0){
                    if(i!=this.nAnalyteConcns-1){
                        labelGuess = (this.analyteConcns[i]+this.analyteConcns[i+1])/2.0;
                    }
                    else{
                        labelGuess = this.analyteConcns[i];
                    }
                    foundFlag = true;
                }
                if(foundFlag)break;
           }
            if(!foundFlag)labelGuess = this.analyteConcns[this.nAnalyteConcns-1];

            kGuess = labelGuess/100.0;           // guess at K
            pGuess = labelGuess/1000.0;          // guess at the antibody concentration

            start[0] = kGuess;
            start[1] = labelGuess;
            start[2] = pGuess;
            start[3] = nsbGuess;
            start[4] = amScaleGuess;
            for(int i=0; i<5; i++)step[i] = 0.1*start[i];
            step[3] = pGuess/100.0;
        }
        else{
            start[0] = estimates[0];
            start[1] = estimates[1];
            start[2] = estimates[2];
            start[3] = estimates[3];
            start[4] = estimates[4];
            for(int i=0; i<5; i++)step[i] = 0.1*start[i];
            if(step[3]==0.0)step[3] = step[2]/100.0;
        }

        // Constraints
        for(int i=0; i<5; i++){
            super.addConstraint(i, -1, 0.0);
        }

        // Perform non-linear regression
        super.simplex(am, start, step);
        super.removeConstraints();

        // Calculate responses for fitted curve
        for(int i=0; i<this.nInterp; i++){
            this.calculatedResponses[i] = am.calcResponse(super.best[0], super.best[1],  super.best[2],  super.best[3], this.interpolationConcns[i], super.best[4]);
        }
        if(!this.supressPlot)this.plott();
        this.curveCheck(this.methodIndices[this.methodUsed]);
    }

    // Check that data is of the form suitable for a competitive assay fit
    private void isCompetitive(){
        int wrongWay = 0;
        for(int i=1; i<this.nAnalyteConcns; i++){
            if(this.responses[i-1]<this.responses[i])wrongWay++;
            if(wrongWay>=this.nAnalyteConcns/2){
                if(this.responses[this.nAnalyteConcns]>=this.responses[0]){
                    throw new IllegalArgumentException("The data appears incompatible with a competitive assay");
                }
                else{
                    System.out.println("The data has been queried as that of a competitive assay but the fitting has not been aborted");
                }
            }
        }
    }


    // CHOOSE A FITTING EQUATION

    // Select a single fitting equation
    public void selectEquation(){

        ArrayList<Object> choice = chooseEquation(0);

        int choice0 = ((Integer)choice.get(0)).intValue();
        int choice1 = ((Integer)choice.get(1)).intValue();
        double choice2 = ((Double)choice.get(2)).doubleValue();
        double choice3 = ((Double)choice.get(3)).doubleValue();

        switch(choice0){
            case 1: this.fiveParameterLogisticFit();
                    break;
            case 2: this.fiveParameterLogisticFit(choice2, choice3);
                    break;
            case 3: this.fourParameterLogisticFit();
                    break;
            case 4: this.fourParameterLogisticFit(choice2, choice3);
                    break;
            case 5: this.bestPolynomialFit();
                    break;
            case 6: this.polynomialFit(choice1);
                    break;
            case 7: this.nonIntegerPolynomialFit(choice1);
                    break;
            case 8: this.sigmoidThresholdFit();
                    break;
            case 9: this.sipsSigmoidFit();
                    break;
            case 10: this.shiftedRectangularHyperbolaFit();
                    break;
            case 11: this.rectangularHyperbolaFit();
                    break;
            case 12: this.amershamFit();
                    break;
            case 13: this.cubicSpline();
                    break;
            case 14: this.linearInterpolation();
                    break;


        }
    }

    // Choose a fitting equation
    // flag = 0:  choice of a single equation
    // flag = 1:  first choice of a comparison
    // flag = 2:  second choice of a comparison
    private ArrayList<Object> chooseEquation(int flag){

        ArrayList<Object> ret = new ArrayList<Object>();
        String headerCommentP = null;
        String[] commentsP = null;
        String[] boxTitlesP = null;


        switch(flag){
            case 0: headerCommentP = "Choose a fitting equation";
                    commentsP = new String[15];
                    break;
            case 1: headerCommentP = "Choose the first equation of the comparison";
                    commentsP = new String[13];
                    break;
            case 2: headerCommentP = "Choose the second equation of the comparison";
                    commentsP = new String[13];
                    break;
        }
        commentsP[0] = "1.  Five paramater logistic equation";
        commentsP[1] = "2.  Five paramater logistic equation (top & bottom fixed)";
        commentsP[2] = "3.  Four paramater logistic equation";
        commentsP[3] = "4.  Four paramater logistic equation (top & bottom fixed)";
        commentsP[4] = "5.  Best fit polynomial";
        commentsP[5] = "6.  Polynomial of user supplied degree";
        commentsP[6] = "7.  Non-integer polynomial";
        commentsP[7] = "8.  Sigmoid threshold function";
        commentsP[8] = "9.  Sips sigmoid function";
        commentsP[9] = "10.  Shifted rectangular hyperbola";
        commentsP[10] = "11.  Rectangular hyperbola";
        commentsP[11] = "12.  Amersham mass action model";

        if(flag==0){
            commentsP[12] = "13.  Cubic spline";
            commentsP[13] = "14.  Linear interpolation\n\n";
            commentsP[14] = "Click on the appropriate button below";
            String[] hold1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
            boxTitlesP = hold1;
        }
        else{
            commentsP[12] = "\nClick on the appropriate button below";
            String[] hold2 = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
            boxTitlesP = hold2;
        }

        int defaultBoxP = 0;
        int ret0 = 1 + JOptionPane.showOptionDialog(null, commentsP, headerCommentP, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null, boxTitlesP, boxTitlesP[defaultBoxP]);
        ret.add(new Integer(ret0));
        int ret1 = 0;
        if(ret0==6)ret1 = Db.readInt("enter polynomial degree");
        if(ret0==7)ret1 = Db.readInt("enter non-integer polynomial number of terms");
        ret.add(new Integer(ret1));
        double ret2 = 0.0;
        double ret3 = 0.0;
        if(ret0==2){
            ret2 = Db.readDouble("Enter five parameter logistic fixed bottom value");
            ret3 = Db.readDouble("Enter five parameter logistic fixed top value");
        }
        if(ret0==4){
            ret2 = Db.readDouble("Enter four parameter logistic fixed bottom value");
            ret3 = Db.readDouble("Enter four parameter logistic fixed top value");
        }
        ret.add(new Double(ret2));
        ret.add(new Double(ret3));

        return ret;
    }

    // SET SUPERCLASS VARIABLES AND DATA PREPARATION METHODS

    // Take logs to the base 10 of the analyte concentrations
    private double[] log10(double[] array){

        int n = array.length;
        double[] ret = new double[n];
        this.nPlot = this.nAnalyteConcns;
        for(int i=0; i<n; i++){
            if(array[i]==0.0){
                ret[i] = Double.NaN;
                this.nPlot--;
            }
            else{
               ret[i] = Math.log10(array[i]);
            }
        }
        return ret;
    }

    // Take logs to the base e of the analyte concentrations
    private double[] loge(double[] array){

        int n = array.length;
        double[] ret = new double[n];
        this.nPlot = 0;
        this.nPlot = this.nAnalyteConcns;
        for(int i=0; i<n; i++){
            if(array[i]==0.0){
                ret[i] = Double.NaN;
                this.nPlot--;
            }
            else{
               ret[i] = Math.log(array[i]);
            }
        }
        return ret;
    }

    // Take the antilog of log10(analyte concentrations)
    private double[] antiLog10(double[] array){

        int n = array.length;
        double[] ret = new double[n];
        for(int i=0; i<n; i++){
            ret[i] = Math.pow(10.0, array[i]);
        }
        return ret;
    }

    // Take the exponential of loge(analyte concentrations)
    private double[] antiLoge(double[] array){

        int n = array.length;
        double[] ret = new double[n];
        for(int i=0; i<n; i++){
            ret[i] = Math.exp(array[i]);
        }
        return ret;
    }

    // Set the data
    // Only need to be called if a super class method is to be called before a subclaas fitting or interpolatiom method is called
    public void setData(){
        this.setDataOne();
    }

    // Assign data variables and set interpolation or fitted curve arrays
    private void setDataOne(){

        // Check relevant data has been entered correctly
        if(!this.analyteEntered)throw new IllegalArgumentException("No analyte concentrations have been entered");
        if(!this.responsesEntered)throw new IllegalArgumentException("No standard curve responses have been entered");
        if(this.nAnalyteConcns!=this.nResponses)throw new IllegalArgumentException("The number of analyte concentrations entered, " + this.nAnalyteConcns + ", must equal the number of standard curve responses entered, " + this.nResponses);

        // Check for set weights
        if(!this.weightsEntered && this.weightOption>0){
            switch(this.weightOption){
                case 1: for(int i=0; i<this.nResponses; i++)this.weights[i] = Math.abs(this.responses[i]);
                        this.weightsEntered = true;
                        break;
                case 2: for(int i=0; i<this.nResponses; i++)this.weights[i] = Math.sqrt(Math.abs(this.responses[i]));
                        this.weightsEntered = true;
                        break;
            }
        }

        // data point numbers and array lengths
        super.nData = this.nAnalyteConcns;
        super.nData0 = this.nAnalyteConcns;
        super.nXarrays = 1;
        super.nYarrays = 1;

        // Sort analyte concns into ascending order
        ArrayMaths am = new ArrayMaths(this.analyteConcns);
        am = am.sort();
        int[] indices = am.originalIndices();
        double[] hold = new double[this.nAnalyteConcns];
        hold = Conv.copy(this.analyteConcns);
        for(int i=0; i<this.nAnalyteConcns; i++)this.analyteConcns[i] = hold[indices[i]];
        hold = Conv.copy(this.responses);
        for(int i=0; i<this.nAnalyteConcns; i++)this.responses[i] = hold[indices[i]];
        if(this.weightsEntered){
            hold = Conv.copy(this.weights);
            for(int i=0; i<this.nAnalyteConcns; i++)this.weights[i] = hold[indices[i]];
        }
        if(this.analyteConcnFlag==1){
            hold = Conv.copy(this.log10AnalyteConcns);
            for(int i=0; i<this.nAnalyteConcns; i++)this.log10AnalyteConcns[i] = hold[indices[i]];
        }
        if(this.analyteConcnFlag==2){
            hold = Conv.copy(this.logeAnalyteConcns);
            for(int i=0; i<this.nAnalyteConcns; i++)this.logeAnalyteConcns[i] = hold[indices[i]];
        }

        // Check for duplicate analyte concentrations and average the response values
        for(int i=0; i<this.nAnalyteConcns-1; i++){
            int nIdent = 1;
            int iFirst = 0;
            ArrayList al = new ArrayList();
            for(int j=i+1; j<this.nAnalyteConcns; j++){
                if(this.analyteConcns[i]==this.analyteConcns[j]){
                    nIdent++;
                    iFirst = i;
                }
            }
            if(nIdent>1){
                double sum = 0.0;
                for(int j=iFirst; j<iFirst+nIdent; j++)sum += this.responses[j];
                this.responses[iFirst] = sum/nIdent;
                for(int j=iFirst+1; j<this.nAnalyteConcns-nIdent+1; j++){
                    this.analyteConcns[j] = this.analyteConcns[j+nIdent-1];
                    this.responses[j] = this.responses[j+nIdent-1];
                    if(this.weightsEntered)this.weights[j] = this.weights[j+nIdent-1];
                    if(this.analyteConcnFlag==1)this.log10AnalyteConcns[j] = this.log10AnalyteConcns[j+nIdent-1];
                    if(this.analyteConcnFlag==2)this.logeAnalyteConcns[j] = this.logeAnalyteConcns[j+nIdent-1];
                }
                this.nAnalyteConcns = this.nAnalyteConcns-nIdent+1;
            }
        }

        // log calculations
        // analyte concentrations
        this.nPlot = this.nAnalyteConcns;
        switch(this.analyteConcnFlag){
            case 0: this.log10AnalyteConcns = this.log10(this.analyteConcns);
                    this.logeAnalyteConcns = this.loge(this.analyteConcns);
                    break;
            case 1: this.logeAnalyteConcns = this.loge(this.analyteConcns);
                    break;
            case 2: this.log10AnalyteConcns = this.log10(this.analyteConcns);
                    break;
        }

        // check for response values <= 0
        int nNonPos = 0;
        this.responsesPlot = true;
        for(int i=0; i<this.nAnalyteConcns; i++){
            if(this.responses[i]<=0.0)nNonPos++;
        }

        if(nNonPos==1){
            if(this.responses[0]<=0.0){
                this.nPlot--;
                this.responsesPlot = true;
            }
            else{
                this.responsesPlot = false;
            }
        }
        else{
            if(nNonPos>1)this.responsesPlot = false;
        }

        // log calculations
        // responses
        if(this.responsesPlot){
            switch(this.responsesFlag){
                case 0: this.log10Responses = this.log10(this.responses);
                        this.logeResponses = this.loge(this.analyteConcns);
                        break;
                case 1: this.logeResponses = this.loge(this.responses);
                        break;
                case 2: this.log10Responses = this.log10(this.responses);
                        break;
            }
        }

        // Supress warning messages in CubicSpline instances
        CubicSpline.supress();

        // Check how weights will be handled
        if(this.weightsEntered){
            if(this.nAnalyteConcns!=this.nWeights)throw new IllegalArgumentException("The number of analyte concentrations entered, " + this.nAnalyteConcns + ", must equal the number of standard curve weights entered, " + this.nWeights);
            super.weightOpt = true;
            super.weightFlag = 0;
            this.weights = this.checkForZeroWeights(this.weights);
            if(super.weightOpt)super.weightFlag = 1;
        }
        else{
            super.weightOpt = false;
            super.weightFlag = 0;
            this.nWeights = this.nResponses;
            this.weights = new double[this.nWeights];
            for(int i=0; i<this.nWeights; i++)this.weights[i] = 1.0;
        }

        // Set arrays in super class
        double[][] xData = new double[1][this.nResponses];
        xData[0] = this.analyteConcns;
        super.setDefaultValues(xData, this.responses, this.weights);

        // Maximum and minimum response values and range
        this.responsesMax = Fmath.maximum(this.responses);
        this.responsesMin = Fmath.minimum(this.responses);
        this.responsesRange = this.responsesMax - this.responsesMin;

        // Set interpolation or fitted curve array dimensions and analyte values
        this.setInterpolationData();

        // Set outlier critical value
        if(this.nAnalyteConcns<=3){
            this.anscombeC = this.critValuesFive[0];
        }
        else{
            if(this.nAnalyteConcns>=120){
                this.anscombeC = this.critValuesFive[19];
            }
            else{
                if(this.confidenceLevel==0.95){
                    this.critValues = new CubicSpline(critSize, critValuesFive);
                }
                else{
                    this.critValues = new CubicSpline(critSize, critValuesOne);
                }
                this.anscombeC = this.critValues.interpolate((double)this.nAnalyteConcns);
            }
        }

        this.setDataOneDone = true;
    }

    // Set outlier confidence level to 99%
    public void setNintyNineLevel(){
        this.confidenceLevel = 0.99;
    }

    // Set outlier confidence level to 95%
    public void setNintyFiveLevel(){
        this.confidenceLevel = 0.95;
    }



    // Set interpolation data arrays
    private void setInterpolationData(){
        this.interpolationConcns = new double[this.nInterp];
        this.calculatedResponses = new double[this.nInterp];
        double incr = (this.analyteConcns[this.nAnalyteConcns-1] - this.analyteConcns[0])/(this.nInterp - 1);
        this.interpolationConcns[0] = this.analyteConcns[0];
        for(int i=1; i<this.nInterp-1; i++)this.interpolationConcns[i] = this.interpolationConcns[i-1] + incr;
        this.interpolationConcns[this.nInterp-1] = this.analyteConcns[this.nAnalyteConcns-1];
    }


    // PLOTTING METHODS

    // Reset plotting option
    // opt = 0: concn v. response
    // opt = 1: log10(concn) v. response
    // opt = 2: loge(concn) v. response
    // opt = 3: log10(concn) v. log10(response)
    // opt = 4: loge(concn) v. loge(response)
    public void resetPlotOption(int opt){
        if(opt<0 || opt>4)throw new IllegalArgumentException("The plot option, " + opt + ", must be greater than or equal to 0 and less than 5");
        this.plotOptions = opt;
    }

    // Supress plotting
    private void supressPlot(){
        this.supressPlot = true;
    }

    // Unsupress plotting
    private void unsupressPlot(){
        this.supressPlot = false;
    }

    // plot the standard curve
    private int plott(){
            int returnFlag = 1;

            double[][] plotData = new double[4][];
            PlotGraph pg = null;
            switch(this.plotOptions){
                case 0: plotData[0] = this.analyteConcns;
                        plotData[1] = this.responses;
                        plotData[2] = this.interpolationConcns;
                        plotData[3] = this.calculatedResponses;
                        pg = new PlotGraph(plotData);
                        pg.setXaxisLegend("Analyte concentration (a)");
                        pg.setYaxisLegend("Assay response (r) ");
                        break;
                case 1: int nLen1 = this.nAnalyteConcns;
                        int nLen2 = this.nInterp;
                        if(nLen1!=this.nPlot){
                            nLen1=this.nPlot;
                            nLen2=this.nInterp-1;
                        }
                        plotData[0] = new double[nLen1];
                        plotData[1] = new double[nLen1];
                        plotData[2] = new double[nLen2];
                        plotData[3] = new double[nLen2];
                        if(nLen1!=this.nAnalyteConcns){
                            for(int i=1; i<this.nAnalyteConcns; i++){
                                plotData[0][i-1] = this.log10AnalyteConcns[i];
                                plotData[1][i-1] = this.responses[i];
                            }
                            for(int i=1; i<this.nInterp; i++){
                                plotData[2][i-1] = Math.log10(this.interpolationConcns[i]);
                                plotData[3][i-1] = this.calculatedResponses[i];
                            }
                        }
                        else{
                            plotData[0] = this.log10AnalyteConcns;
                            plotData[1] = this.responses;
                            plotData[3] = this.calculatedResponses;
                            plotData[2] = new double[this.nInterp];
                            for(int i=0; i<this.nInterp; i++){
                                plotData[2][i] = Math.log10(this.interpolationConcns[i]);
                            }
                        }
                        pg = new PlotGraph(plotData);
                        pg.setXaxisLegend("Log10[ Analyte concentration (a) ]");
                        pg.setYaxisLegend("Assay response (r) ");
                        break;

                case 2: nLen1 = this.nAnalyteConcns;
                        nLen2 = this.nInterp;
                        if(nLen1!=this.nPlot){
                            nLen1=this.nPlot;
                            nLen2=this.nInterp-1;
                        }
                        plotData[0] = new double[nLen1];
                        plotData[1] = new double[nLen1];
                        plotData[2] = new double[nLen2];
                        plotData[3] = new double[nLen2];
                        if(nLen1!=this.nAnalyteConcns){
                            for(int i=1; i<this.nAnalyteConcns; i++){
                                plotData[0][i-1] = this.logeAnalyteConcns[i];
                                plotData[1][i-1] = this.responses[i];
                            }
                            for(int i=1; i<this.nInterp; i++){
                                plotData[2][i-1] = Math.log(this.interpolationConcns[i]);
                                plotData[3][i-1] = this.calculatedResponses[i];
                            }
                        }
                        else{
                            plotData[0] = this.logeAnalyteConcns;
                            plotData[1] = this.responses;
                            plotData[3] = this.calculatedResponses;
                            plotData[2] = new double[this.nInterp];
                            for(int i=0; i<this.nInterp; i++){
                                plotData[2][i] = Math.log(this.interpolationConcns[i]);
                            }
                        }
                        pg = new PlotGraph(plotData);
                        pg.setXaxisLegend("Loge[ Analyte concentration (a) ]");
                        pg.setYaxisLegend("Assay response (r) ");
                        break;

                case 3: if(!this.responsesPlot){
                            System.out.println("A log(concentration) v. log(responses) plot is not possible; zero or negative responses");
                            return -1;
                        }
                        nLen1 = this.nAnalyteConcns;
                        nLen2 = this.nInterp;
                        if(nLen1!=this.nPlot){
                            nLen1=this.nPlot;
                            nLen2=this.nInterp-1;
                        }
                        plotData[0] = new double[nLen1];
                        plotData[1] = new double[nLen1];
                        plotData[2] = new double[nLen2];
                        plotData[3] = new double[nLen2];
                        if(nLen1!=this.nAnalyteConcns){
                            for(int i=1; i<this.nAnalyteConcns; i++){
                                plotData[0][i-1] = this.log10AnalyteConcns[i];
                                plotData[1][i-1] = this.log10Responses[i];
                            }
                            for(int i=1; i<this.nInterp; i++){
                                plotData[2][i-1] = Math.log10(this.interpolationConcns[i]);
                                plotData[3][i-1] = Math.log10(this.calculatedResponses[i]);
                            }
                        }
                        else{
                            plotData[0] = this.log10AnalyteConcns;
                            plotData[1] = this.log10Responses;
                            plotData[2] = new double[this.nInterp];
                            plotData[3] = new double[this.nInterp];
                            for(int i=0; i<this.nInterp; i++){
                                plotData[2][i] = Math.log10(this.interpolationConcns[i]);
                                plotData[3][i] = Math.log10(this.calculatedResponses[i]);
                            }
                        }
                        pg = new PlotGraph(plotData);
                        pg.setXaxisLegend("Log10[ Analyte concentration (a) ]");
                        pg.setYaxisLegend("Log10[ Assay response (r) ]");
                        break;

                case 4: if(!this.responsesPlot){
                            System.out.println("A log(concentration) v. log(responses) plot is not possible; zero or negative responses");
                            return -1;
                        }
                        nLen1 = this.nAnalyteConcns;
                        nLen2 = this.nInterp;
                        if(nLen1!=this.nPlot){
                            nLen1=this.nPlot;
                            nLen2=this.nInterp-1;
                        }
                        plotData[0] = new double[nLen1];
                        plotData[1] = new double[nLen1];
                        plotData[2] = new double[nLen2];
                        plotData[3] = new double[nLen2];
                        if(nLen1!=this.nAnalyteConcns){
                            for(int i=1; i<this.nAnalyteConcns; i++){
                                plotData[0][i-1] = this.logeAnalyteConcns[i];
                                plotData[1][i-1] = this.logeResponses[i];
                            }
                            for(int i=1; i<this.nInterp; i++){
                                plotData[2][i-1] = Math.log(this.interpolationConcns[i]);
                                plotData[3][i-1] = Math.log(this.calculatedResponses[i]);
                            }
                        }
                        else{
                            plotData[0] = this.logeAnalyteConcns;
                            plotData[1] = this.logeResponses;
                            plotData[2] = new double[this.nInterp];
                            plotData[3] = new double[this.nInterp];
                            for(int i=0; i<this.nInterp; i++){
                                plotData[2][i] = Math.log(this.interpolationConcns[i]);
                                plotData[3][i] = Math.log(this.calculatedResponses[i]);
                            }
                        }
                        pg = new PlotGraph(plotData);
                        pg.setXaxisLegend("Loge[ Analyte concentration (a) ]");
                        pg.setYaxisLegend("Loge[ Assay response (r) ]");
                        break;
            }

            int[] points = {1, 0};
            pg.setPoint(points);
            int[] lines = {0, 3};
            pg.setLine(lines);

            pg.setGraphTitle(this.titleZero);
            pg.setGraphTitle2(this.titleOne);
            pg.plot();

            return returnFlag;
    }

    // CHECK THAT THE CURVE IS MONOTONIC

    // Check that the fitted or interpolated standard curve is monotonic
    private void curveCheck(int mFlag){

        this.interpStartIndex = 0;
        this.interpEndIndex = this.nInterp-1;

        // Outlier identification
        this.outlierFlag = false;
        this.outliers = new String[this.nAnalyteConcns];
        this.residualsMean = Stat.mean(super.residual);
        this.residualsSD = Stat.standardDeviation(super.residual);
        int maxOutlierIndex = 0;
        double maxAbsResidual = Math.abs(this.residual[0]);
        for(int i=0; i<this.nAnalyteConcns; i++){
            if(Math.abs(this.residual[i])>maxAbsResidual){
                maxAbsResidual = Math.abs(this.residual[i]);
                maxOutlierIndex = i;
            }
        }
        for(int i=0; i<this.nAnalyteConcns; i++){
            this.outliers[i] = "   ";
            if(Math.abs(this.residual[i] - this.residualsMean)>this.anscombeC*this.residualsSD){
                this.outliers[i] = "possible outlier";
                if(i==maxOutlierIndex) this.outliers[i] += " (***)";
                this.outlierFlag = true;
            }
        }

        // Count number of turning points in standard curve
        int direction = 1;
        this.nTurningPoints = 0;
        boolean checkFlag = false;
        ArrayList<Integer> turns = new ArrayList<Integer>();
        double gap = calculatedResponses[1] - calculatedResponses[0];
        if(gap < 0.0)direction= -1;
        if(gap==0.0)checkFlag = true;
        for(int i=2; i<this.nInterp; i++){
            if(direction==1){
                if(calculatedResponses[i]<=calculatedResponses[i-1])checkFlag = true;
            }
            else{
                if(calculatedResponses[i]>=calculatedResponses[i-1])checkFlag = true;
            }
            if(checkFlag){
                this.nTurningPoints++;
                turns.add(new Integer(i));
                direction = -direction;
                this.ambigCheck = true;
                checkFlag = false;
            }
        }

        // calculate limited range for one turning point
        if(this.nTurningPoints==1){
            this.turnIndices = new int[1];
            this.turnIndices[0] = ((Integer)turns.get(0)).intValue();
            if(this.turnIndices[0]<=this.nInterp/2){
                this.interpStartIndex = this.turnIndices[0];
            }
            else{
                this.interpEndIndex  = this.turnIndices[0];
            }
            System.out.println(methodNames[mFlag]);
            System.out.println("The fitted or interpolated standard curve is not monotonic");
            System.out.println("The useable analyte concentration range is " +  this.interpolationConcns[this.interpStartIndex] + " to " +  this.interpolationConcns[this.interpEndIndex]);
        }

        // calculate limited range for two or more turning points
        if(this.nTurningPoints>1){

            this.turnIndices = new int[this.nTurningPoints];
            for(int i= 0; i<this.nTurningPoints; i++)this.turnIndices[i] = ((Integer)turns.get(i)).intValue();

            int[] nts = new int[this.nTurningPoints+1];
            int[] nte = new int[this.nTurningPoints+1];
            int[] ntd = new int[this.nTurningPoints+1];

            ntd[0] = this.turnIndices[0];
            nts[0] = 0;
            nte[0] = this.turnIndices[0];
            for(int i=1; i<this.nTurningPoints; i++){
                ntd[i] = this.turnIndices[i] - this.turnIndices[i-1];
                nts[i] = this.turnIndices[i-1];
                nte[i] = this.turnIndices[i];
            }
            ntd[this.nTurningPoints] = this.nInterp - 1 - this.turnIndices[this.nTurningPoints-1];
            nts[this.nTurningPoints] = this.turnIndices[this.nTurningPoints-1];
            nte[this.nTurningPoints] = this.nInterp - 1;


            int testMax = 0;
            for(int i=0; i<=this.nTurningPoints; i++){
                for(int j=0; j<=this.nTurningPoints; j++){
                    if(i!=j){
                        if(ntd[i]>=ntd[j])testMax++;
                    }
                }
                if(testMax==this.nTurningPoints){
                    this.interpStartIndex = nts[i];
                    this.interpEndIndex = nte[i];
                }
                else{
                    testMax = 0;
                }
                if(testMax!=0)break;
            }
            System.out.println(methodNames[mFlag]);
            System.out.println("The fitted or interpolated standard curve is not monotonic");
            System.out.println("The useable analyte concentration range is " +  this.interpolationConcns[this.interpStartIndex] + " to " +  this.interpolationConcns[this.interpEndIndex]);
        }

        // define usable range in the standard curve
        this.interpAnalyteStart = this.interpolationConcns[this.interpStartIndex];
        this.interpAnalyteEnd = this.interpolationConcns[this.interpEndIndex];
        this.interpResponseStart = this.calculatedResponses[this.interpStartIndex];
        this.interpResponseEnd = this.calculatedResponses[this.interpEndIndex];


        // Prepare response concn interpolation data within the working range
        this.nWorking = this.interpEndIndex - this.interpStartIndex + 1;
        double[] xx = new double[this.nWorking];
        double[] yy = new double[this.nWorking];
        for(int i=0; i<this.nWorking; i++){
            xx[i] = this.interpolationConcns[this.interpStartIndex+i];
            yy[i] = this.calculatedResponses[this.interpStartIndex+i];
        }
        if(this.methodUsed==14){
            this.linterp = new LinearInterpolation(yy, xx);
        }
        else{
            this.interp = new CubicSpline(yy, xx);
        }

        // record working curve direction
        this.curveDirection = true;
        if(this.interpResponseStart>this.interpResponseEnd)this.curveDirection = false;

        // revisit working range
        this.workingResponseMin = Fmath.minimum(this.calculatedResponses);
        if(this.curveDirection){
            if(this.workingResponseMin<this.interpResponseStart)this.workingResponseMin = this.interpResponseStart;
        }
        else{
            if(this.workingResponseMin<this.interpResponseEnd)this.workingResponseMin = this.interpResponseEnd;
        }
        this.workingResponseMax = Fmath.maximum(this.calculatedResponses);
        if(this.curveDirection){
            if(this.workingResponseMax>this.interpResponseEnd)this.workingResponseMax = this.interpResponseEnd;
        }
        else{
            if(this.workingResponseMax>this.interpResponseStart)this.workingResponseMax = this.interpResponseStart;
        }

        // Prepare response error concn interpolation data within the working range
        double[] ee = new double[this.nWorking];
        double yError = Math.sqrt(super.sumOfSquaresError/(this.nAnalyteConcns - super.nParam));

        if(this.sampleErrorFlag){
            if(!this.weightsEntered){
                for(int i=0; i<this.nWorking; i++){
                    ee[i] = yError;
                }
            }
            else{
                // check if weights are monotonic within the working range
                boolean monoCheck = true;
                int iS = 0;
                int iF = this.nAnalyteConcns-1;
                boolean breakFlag = false;
                if(this.ambigCheck){
                    for(int i=0; i<this.nAnalyteConcns; i++){
                        if(this.interpAnalyteStart>=this.analyteConcns[i]){
                            iS = i;
                            breakFlag = true;
                        }
                        if(breakFlag)break;
                    }
                    breakFlag = false;
                    for(int i=this.nAnalyteConcns-1; i>=0; i--){
                        if(this.interpAnalyteEnd<=this.analyteConcns[i]){
                            iF = i;
                            breakFlag = true;
                        }
                        if(breakFlag)break;
                    }
                }
                int nwe = iF - iS + 1;
                double[] ew = new double[nwe];
                for(int i=0; i<nwe; i++)ew[i] = this.weights[i+iS];
                monoCheck = ImmunoAssay.isMonotonic(ew);

                // check for big jumps in the weights
                if(monoCheck){
                    double gap1 = Math.abs(ew[1] - ew[0]);
                    double gap0 = gap1;
                    for(int i=2; i<nwe; i++){
                        gap1 = Math.abs(ew[i] - ew[i-1]);
                        if(gap1>gap0)gap0 = gap1;
                    }
                    if(gap0>0.6*Math.abs(Fmath.maximum(ew) - Fmath.minimum(ew)))monoCheck = false;
                }

                // scale weights to overall fitting variance
                double[] sWeights = new double[this.nAnalyteConcns];
                double scale = yError/this.weightsMean;
                for(int i=0; i<this.nAnalyteConcns; i++)sWeights[i] = weights[i]*scale;
                if(this.weightsEntered && !monoCheck){
                    // linear interpolation if weights are non-monotonic or have big jumps
                    LinearInterpolation liee = new LinearInterpolation(this.analyteConcns, sWeights);
                    for(int i=0; i<this.nWorking; i++)ee[i] = Math.abs(liee.interpolate(xx[i]));
                }
                else{
                    // cubic spline interpolation if weights are monotonic without big jumps
                    CubicSpline csee = new CubicSpline(this.analyteConcns, sWeights);
                    for(int i=0; i<this.nWorking; i++)ee[i] = Math.abs(csee.interpolate(xx[i]));
                }
            }
            this.errorp = new CubicSpline(yy, ee);

            // Calculate the estimated errors in the estimated concentrations for each of the data points
            this.propagatedErrors = new String[this.nAnalyteConcns];
            double temp = 0.0;
            ArrayList<Double> alpe = new ArrayList<Double>();
            for(int i=0; i<this.nAnalyteConcns; i++){
                boolean checkFlag2 = false;
                if(this.curveDirection){
                    if(this.responses[i]<this.interpResponseStart || this.responses[i]>this.interpResponseEnd)checkFlag2 = true;
                }
                else{
                    if(this.responses[i]>this.interpResponseStart || this.responses[i]<this.interpResponseEnd)checkFlag2 = true;
                }
                if(checkFlag2){
                    this.propagatedErrors[i] = "**";
                }
                else{
                    alpe.add(new Double(this.responses[i]));
                    temp = this.getSampleConcn(this.responses[i]);
                    temp = this.getSampleConcnError();
                    alpe.add(new Double(temp));
                    temp = Fmath.truncate(temp, super.prec);
                    this.propagatedErrors[i] = (new Double(temp)).toString();
                }
            }

            // Calculate minimum, maximum and mean propagated errors
            int npe = alpe.size()/2;
            double[] resp = new double[npe];
            double[] xerr = new double[npe];
            for(int i=0; i<npe; i++){
                resp[i] = (alpe.get(2*i)).doubleValue();
                xerr[i] = (alpe.get(2*i+1)).doubleValue();
            }
            CubicSpline cspe = new CubicSpline(resp, xerr);
            double[] respe = new double[1001];
            double[] xerre = new double[1001];
            respe[0] = resp[0];
            respe[1000] = resp[npe-1];
            double incr = (resp[npe-1] - resp[0])/1000;
            for(int i=1; i<1000; i++){
                respe[i] = respe[i-1] + incr;
            }
            for(int i=0; i<1001; i++){
                xerre[i] = cspe.interpolate(respe[i]);
            }
            Stat stat = new Stat(xerre);
            this.minimumAerror = stat.minimum();
            this.maximumAerror = stat.maximum();
            this.meanAerror = stat.mean();
            this.sdAerror = stat.standardDeviation();
        }
    }


    // FIND A SAMPLE CONCENTRATION METHODS

    // Find unknown analyte concentration and its error
    public double getSampleConcn(double response){

        this.sampleResponse = response;
        double concn = Double.NaN;
        boolean checkFlag = false;

        // Check response is within range
        if(this.curveDirection){
            if(response<this.interpResponseStart || response>this.interpResponseEnd)checkFlag = true;
        }
        else{
            if(response>this.interpResponseStart || response<this.interpResponseEnd)checkFlag = true;
        }
        if(checkFlag){
            if(this.ambigCheck){
                System.out.println("The sample response, " + response + ", is outside the useable part of the standard curve:");
            }
            else{
                System.out.println("The sample response, " + response + ", is outside the limits of the standard curve:");
            }
            System.out.println(this.interpResponseStart + " to " + this.interpResponseEnd);
            System.out.println("NaN returned");
            return concn;
        }

        // interpolate sample concentration
        if(this.methodUsed==14){
            concn = linterp.interpolate(response);
        }
        else{
            concn = interp.interpolate(response);
        }
        this.sampleConcn = concn;

        // estimate sample concentration error
        this.sampleError = Double.NaN;
        if(this.sampleErrorFlag){
            double yError = this.errorp.interpolate(response);
            double yErrorMinus = response - yError;
            if(yErrorMinus<this.workingResponseMin)yErrorMinus = this.workingResponseMin;
            double fac = (response - yErrorMinus)/yError;
            double xErrorMinus = this.interp.interpolate(yErrorMinus);
            double yErrorPlus = response + yError;
            if(yErrorPlus>this.workingResponseMax)yErrorPlus=this.workingResponseMax;
            fac += (yErrorPlus - response)/yError;
            double xErrorPlus = this.interp.interpolate(yErrorPlus);
            this.sampleError = Math.abs(xErrorPlus - xErrorMinus)/fac;

        }

        return concn;
    }

    // return unknown analyte concentration error
    public double getSampleConcnError(){
        if(this.methodUsed==0){
            System.out.println("ImmunoAssay method: getSampleConcnError()");
            System.out.println("A sample concentration error is not meaningful in the case of a cubic spline interpolation");
            System.out.println("NaN returned");
        }
        if(this.methodUsed==1){
            System.out.println("ImmunoAssay method: getSampleConcnError()");
            System.out.println("A sample concentration error is not meaningful in the case of a cubic interpolation");
            System.out.println("NaN returned");
        }
        if(this.methodUsed==14){
            System.out.println("ImmunoAssay method: getSampleConcnError()");
            System.out.println("A sample concentration error is not meaningful in the case of a linear interpolation");
            System.out.println("NaN returned");
        }

        return this.sampleError;
    }

    // FITTED CURVE GET DETAILS AND RESET METHODS

    // Reset the number of points used in the interpolated standard curve
    public void resetNfittedCurve(int nInterp){
        this.nInterp = nInterp;
    }

    // Return the interpolated standard curve analyte concentrations
    public double[] getFittedStandardCurveConcns(){
        return Conv.copy(this.interpolationConcns);
    }

    // Return the interpolated standard curve analyte responses
    public double[] getFittedStandardCurveResponses(){
        return Conv.copy(this.calculatedResponses);
    }

    // Return the working analyte concentratio range of the standard curve
    public double[] getWorkingConcentrationRange(){
        double[] ret = {this.interpAnalyteStart, this.interpAnalyteEnd};
        return ret;
    }

    // Return the working response range of the standard curve
    public double[] getWorkingResponseRange(){
        double[] ret = {this.interpResponseStart, this.interpResponseEnd};
        return ret;
    }

    // Return the model parameter values
    public double[] getModelParameterValues(){
        return Conv.copy(super.best);
    }

    // Return the model parameter errors
    public double[] getModelParameterErrors(){
        return Conv.copy(super.bestSd);
    }

    // Return number of turning points in the standard curve
    public int getNumberOfTurningPoints(){
        return this.nTurningPoints;
    }

    // Return unweighted sum of squares
    public double getSumOfSquares(){
        return super.sumOfSquaresError;
    }

    // Return unweighted sum of squares
    public double getUnWeightedSumOfSquares(){
        return super.sumOfSquaresError;
    }

    // Return weighted sum of squares
    public double getWeightedSumOfSquares(){
        return super.chiSquare;
    }

    // Return analyte concentrations at the turning points in the standard curve
    public double[] getTurningPointConcns(){

        double[] ret = null;
        if(this.nTurningPoints>0){
            ret = new double[this.nTurningPoints];
            for(int i=0; i<this.nTurningPoints; i++)ret[i] = this.interpolationConcns[this.turnIndices[i]];
        }
        return ret;
    }

    // Return responses at the turning points in the standard curve
    public double[] getTurningPointResponses(){

        double[] ret = null;
        if(this.nTurningPoints>0){
            ret = new double[this.nTurningPoints];
            for(int i=0; i<this.nTurningPoints; i++)ret[i] = this.calculatedResponses[this.turnIndices[i]];
        }
        return ret;
    }


    // GOODNESS OF FIT ANALYSIS AND PRINT METHODS

    // Print details of last regression
    public void print(){
        this.print(this.filename);
    }

    // Print analysis of last regression
    public void print(String filename){

        boolean printFlag = true;
        switch(this.methodUsed){
            case 0: System.out.println("There is no text file associated with the cubic spline interpolation method");
                    printFlag = false;
                    break;
            case 1: System.out.println("There is no text file associated with the cubic interpolation method");
                    printFlag = false;
                    break;
            case 14: System.out.println("There is no text file associated with the linear interpolation method");
                    printFlag = false;
                    break;

        }
        if(printFlag){
            int pos = filename.indexOf(".");
            if(pos==-1)filename = filename + ".txt";
            this.filename = filename;
            FileOutput fout = new FileOutput(filename);
            fout.println(this.titleZero);
            if(this.dataRead)fout.println("Data input file name:   " + dataFilename);
            fout.dateAndTimeln(filename);

            this.commonPrint(fout);

            fout.println();
            fout.println("End of file");
            fout.close();
        }
    }

     // Common section of the two print methods
     public void commonPrint(FileOutput fout){

            fout.println();
            fout.println(this.titleOne);
            fout.println("r = assay response;  a = analyte concentration");

            boolean logLin = false;
            String[]  param = null;
            String[] paramT = null;
            switch(this.methodUsed){
                case 0: break;
                case 1: break;
                case 2: param = new String[this.polyDegree+1];
                        for(int i=0; i<=this.polyDegree; i++)param[i] = "c["+i+"]";
                        logLin = false;
                        break;
                case 3: param = new String[this.bestPolyDegree+1];
                        for(int i=0; i<=this.bestPolyDegree; i++)param[i] = "c["+i+"]";
                        logLin = false;
                        break;
                case 4: fout.println("n = " + (this.polyNterms-1));
                        param = new String[2*this.polyNterms-1];
                        for(int i=0; i<2*this.polyNterms-1; i++)param[i] = "c["+i+"]";
                        logLin = true;
                        break;
                case 5: param = new String[3];
                        param[0] = "alpha";
                        param[1] = "theta";
                        param[2] = "A";
                        logLin = true;
                        break;
                case 6: param = new String[3];
                        param[0] = "theta";
                        param[1] = "n";
                        param[2] = "A";
                        logLin = true;
                        break;
                case 7: param = new String[4];
                        param[0]="top";
	                    param[1]="bottom";
	                    param[2]="C50";
	                    param[3]="Hill Slope";
                        logLin = true;
                        break;
                case 8: param = new String[5];
                        param[0]="top";
	                    param[1]="bottom";
	                    param[2]="C50";
	                    param[3]="HillSlope";
	                    param[4]="Asymm";
                        logLin = true;
                        break;
                case 9: param = new String[3];
                        param[0]="theta";
                        param[1]="alpha";
	                    param[2]="A";
                        logLin = true;
                        break;
                case 10: param = new String[2];
                        param[0]="theta";
	                    param[1]="A";
                        logLin = true;
                        break;
                case 11: param = new String[5];
                        param[0]="K";
	                    param[1]="L";
	                    param[2]="P";
	                    param[3]="N";
                        param[4]="S";
                        logLin = true;
                        break;
                case 12: fout.println("top =    " + this.top);
                        fout.println("bottom = " + this.bottom);
                        param = new String[2];
	                    param[0]="C50";
	                    param[1]="Hill Slope";
                        logLin = true;
                        break;
                case 13: fout.println("top =    " + this.top);
                        fout.println("bottom = " + this.bottom);
                        param = new String[3];
	                    param[0]="C50";
	                    param[1]="HillSlope";
	                    param[2]="Asymm";
                        logLin = true;
                        break;
               case 14: break;
               default: throw new IllegalArgumentException("methodUsed " + this.methodUsed + " not recognised");
            }

            if(logLin){
                fout.println();
                fout.println("Non-linear regression (Nelder and Mead simplex procedure)");
                if(!super.nlrStatus){
                    fout.println("Convergence criterion was not satisfied");
                    fout.println("The following results are, or are derived from, the current estimates on exiting the regression method");
                    fout.println();
                }

                fout.println("Estimated parameters");
                fout.println("The statistics are obtained assuming that the model behaves as a linear model about the minimum.");
                fout.println("The Hessian matrix is calculated as the numerically derived second derivatives of chi square with respect to all pairs of parameters.");
                if(super.zeroCheck)fout.println("The best estimate/s equal to zero were replaced by the step size in the numerical differentiation!!!");
                fout.println("Consequentlty treat the statistics with great caution.");
                if(!super.posVarFlag){
                    fout.println("Covariance matrix contains at least one negative diagonal element");
                    fout.println(" - all variances are dubious");
                    fout.println(" - may not be at a minimum or the model may be so non-linear that the linear approximation in calculating the statisics is invalid");
                }
                if(!super.invertFlag){
                    fout.println("Hessian matrix is singular");
                    fout.println(" - variances cannot be calculated");
                    fout.println(" - may not be at a minimum  or the model may be so non-linear that the linear approximation in calculating the statisics is invalid");
                }
            }
            else{
                fout.println("Linear regression");
            }

            fout.println();
            fout.printtab(" ", super.field);
            if(super.invertFlag){
                fout.printtab("Best", super.field);
                fout.printtab("Estimate of", super.field);
                fout.printtab("Coefficient", super.field);
                fout.printtab("t-value", super.field);
                fout.println("p-value");
            }
            else{
                fout.println("Best");
            }

            if(super.invertFlag){
                fout.printtab(" ", super.field);
                fout.printtab("estimate", super.field);
                fout.printtab("the error", super.field);
                fout.printtab("of", super.field);
                fout.printtab("t", super.field);
                fout.println("P > |t|");
            }
            else{
                fout.printtab(" ", super.field);
                fout.println("estimate");
            }

            if(super.invertFlag){
                fout.printtab(" ", super.field);
                fout.printtab(" ", super.field);
                fout.printtab(" ", super.field);
                fout.println("variation (%)");
            }
            else{
                fout.println("   ");
            }

            for(int i=0; i<super.nParam; i++){
                if(super.invertFlag){
                    fout.printtab(param[i], super.field);
                    fout.printtab(Fmath.truncate(super.best[i],super.prec), super.field);
                    fout.printtab(Fmath.truncate(super.bestSd[i],super.prec), super.field);
                    fout.printtab(Fmath.truncate(Math.abs(super.bestSd[i]*100.0/super.best[i]),super.prec), super.field);
                    fout.printtab(Fmath.truncate(super.tValues[i],super.prec), super.field);
                    fout.println(Fmath.truncate(super.pValues[i],super.prec));
                }
                else{
                    fout.printtab(param[i], super.field);
                    fout.println(Fmath.truncate(super.best[i],super.prec));
                }
            }
            fout.println();

            if(logLin){
                // non-linear case only
                fout.printtab(" ", super.field);
                fout.printtab("Best", super.field);
                fout.printtab("Pre-minimum", super.field);
                fout.printtab("Post-minimum", super.field);
                fout.printtab("Initial", super.field);
                fout.println("Fractional");

                fout.printtab(" ", super.field);
                fout.printtab("estimate", super.field);
                fout.printtab("gradient", super.field);
                fout.printtab("gradient", super.field);
                fout.printtab("estimate", super.field);
                fout.println("step");

                for(int i=0; i<super.nParam; i++){
                    fout.printtab(param[i], super.field);
                    fout.printtab(Fmath.truncate(super.best[i],super.prec), super.field);
                    fout.printtab(Fmath.truncate(super.grad[i][0],super.prec), super.field);
                    fout.printtab(Fmath.truncate(super.grad[i][1],super.prec), super.field);
                    fout.printtab(Fmath.truncate(super.startH[i],super.prec), super.field);
                    fout.println(Fmath.truncate(super.stepH[i],super.prec));
                }

                fout.println();
            }

            int kk=0;
            fout.printtab("analyte", super.field);
            fout.printtab("observed", super.field);
            fout.printtab("calculated", super.field);
            fout.printtab("weight", super.field);
            fout.printtab("unweighted", super.field);
            fout.printtab("weighted", super.field);
            fout.printtab("estimated analyte", super.field);
            fout.println("outliers");

            fout.printtab("concn ", super.field);
            fout.printtab("response", super.field);
            fout.printtab("response", super.field);
            fout.printtab("     ", super.field);
            fout.printtab("residual", super.field);
            fout.printtab("residual", super.field);
            fout.printtab("concn error *", super.field);
            fout.println("   ");

            for(int i=0; i<this.nAnalyteConcns; i++){
                fout.printtab(Fmath.truncate(super.xData[0][kk],super.prec), super.field);
                fout.printtab(Fmath.truncate(super.yData[kk],super.prec), super.field);
                fout.printtab(Fmath.truncate(super.yCalc[kk],super.prec), super.field);
                fout.printtab(Fmath.truncate(super.weight[kk],super.prec), super.field);
                fout.printtab(Fmath.truncate(super.residual[kk],super.prec), super.field);
                fout.printtab(Fmath.truncate(super.residualW[kk],super.prec), super.field);
                fout.printtab(this.propagatedErrors[kk], super.field);
                fout.println(this.outliers[i]);
                kk++;
            }
            fout.println();
            fout.println("*  The estimated error in the estimated concentration on entering this response via getSampleConcn(response)");
            fout.println("** Outside the working range");
            fout.println("Mean of the unweighted residuals =               " + Fmath.truncate(this.residualsMean, super.prec));
            fout.println("Standard deviation of the unweighted residuals = " + Fmath.truncate(this.residualsSD, super.prec));
            fout.println("Outlier critical value at the " + this.confidenceLevel*100.0 + " confidence level = " + Fmath.truncate((this.anscombeC*this.residualsSD + this.residualsMean), super.prec));
            if(this.outlierFlag)fout.println("*** the most extreme possible outlier");
            fout.println();
            fout.println("Minimum estimated interpolated concentration error:                   " + Fmath.truncate(this.minimumAerror, super.prec));
            fout.println("Maximum estimated interpolated concentration error:                   " + Fmath.truncate(this.maximumAerror, super.prec));
            fout.println("Mean estimated interpolated concentration error:                      " + Fmath.truncate(this.meanAerror, super.prec));
            fout.println("Standard deviation of the estimated interpolated concentration error: " + Fmath.truncate(this.sdAerror, super.prec));
            fout.println();

            fout.printtab("Degrees of freedom");
		    fout.println(super.degreesOfFreedom);
            fout.printtab("Number of data points");
		    fout.println(super.nData);
            fout.printtab("Number of estimated paramaters");
		    fout.println(super.nParam);

            fout.printtab("Sum of squares of the unweighted residuals");
		    fout.println(Fmath.truncate(super.sumOfSquaresError,super.prec));
            if(super.weightOpt){
	            fout.printtab("Chi Square");
		        fout.println(Fmath.truncate(super.chiSquare,super.prec));
                fout.printtab("Reduced Chi Square");
		        fout.println(Fmath.truncate(super.reducedChiSquare,super.prec));
		    }

		    if(logLin){
		        // non-linear case
	            fout.println("Correlation: analyte concentration and responses");
		        fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient (R)");
	            fout.println(Fmath.truncate(super.xyR,super.prec));
	            if(Math.abs(super.xyR)<=1.0D){
		            fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient Probability");
		            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(super.xyR, super.nData-2),super.prec));
                }

    	        fout.println(" ");
    	        fout.println("Correlation: observed responses and calculated responses");
                fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient");
	            fout.println(Fmath.truncate(super.yyR, super.prec));
	            if(Math.abs(super.yyR)<=1.0D){
		            fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient Probability");
		            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(super.yyR, super.nData-2),super.prec));
                }

                fout.println();
                fout.printtab("Durbin-Watson d statistic");
                fout.println(Fmath.truncate(super.getDurbinWatsonD(), super.prec));
                fout.println();

                if(super.posVarFlag && super.invertFlag && super.chiSquare!=0.0D){
                    fout.println("Parameter - parameter correlation coefficients");
                    fout.printtab(" ", super.field);
                    for(int i=0; i<super.nParam;i++){
                        fout.printtab(param[i], super.field);
                    }
                    fout.println();
                    fout.println();

                    for(int j=0; j<super.nParam;j++){
                        fout.printtab(param[j], super.field);
                        for(int i=0; i<super.nParam;i++){
                            fout.printtab(Fmath.truncate(super.corrCoeff[i][j], super.prec), super.field);
                        }
                        fout.println();
                    }
                }

                fout.println();
                fout.println("Coefficient of determination, R =                   " + Fmath.truncate(super.multR, super.prec));
                fout.println("Adjusted Coefficient of determination, R' =         " + Fmath.truncate(super.adjustedR, super.prec));
                fout.println("Coefficient of determination, F-ratio =             " + Fmath.truncate(super.multipleF, super.prec));
                fout.println("Coefficient of determination, F-ratio probability = " + Fmath.truncate(super.multipleFprob, super.prec));
                fout.println("Total (weighted) sum of squares  =                  " + Fmath.truncate(super.sumOfSquaresTotal, super.prec));
                fout.println("Regression (weighted) sum of squares  =             " + Fmath.truncate(super.sumOfSquaresRegrn, super.prec));
                fout.println("Error (weighted) sum of squares  =                  " + Fmath.truncate(super.chiSquare, super.prec));

                fout.println();
                fout.printtab("Number of iterations taken");
                fout.println(super.nIter);
                fout.printtab("Maximum number of iterations allowed");
                fout.println(super.nMax);
                fout.printtab("Number of restarts taken");
                fout.println(super.kRestart);
                fout.printtab("Maximum number of restarts allowed");
                fout.println(super.konvge);
                fout.printtab("Standard deviation of the simplex at the minimum");
                fout.println(Fmath.truncate(super.simplexSd, super.prec));
                fout.printtab("Convergence tolerance");
                fout.println(super.fTol);
                switch(minTest){
                    case 0: fout.println("simplex sd < the tolerance times the mean of the absolute values of the y values");
                            break;
                    case 1: fout.println("simplex sd < the tolerance");
                            break;
                    case 2: fout.println("simplex sd < the tolerance times the square root(sum of squares/degrees of freedom");
                            break;
                }
                fout.println("Step used in numerical differentiation to obtain Hessian matrix");
                fout.println("d(parameter) = parameter*"+super.delta);
            }
            else{
                // linear case
	            fout.println(" ");
	            fout.println("Correlation: analyte concentrations and responses");
		        fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient (R)");
	            fout.println(Fmath.truncate(super.xyR,super.prec));
	            if(Math.abs(super.xyR)<=1.0D){
		            fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient Probability");
		            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(super.xyR, super.nData-2),super.prec));
                }

    	        fout.println(" ");
    	        fout.println("Correlation: observed responses and calculated responses");
                fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient");
	            fout.println(Fmath.truncate(super.yyR, super.prec));
	            if(Math.abs(super.yyR)<=1.0D){
		            fout.printtab(super.weightWord[super.weightFlag] + "Linear Correlation Coefficient Probability");
		            fout.println(Fmath.truncate(Stat.linearCorrCoeffProb(super.yyR, super.nData-2),super.prec));
                }

                fout.println();
                fout.println();

                if(super.chiSquare!=0.0D){
                    fout.println("Correlation coefficients");
                    fout.printtab(" ", super.field);
                    for(int i=0; i<super.nParam;i++){
                        fout.printtab(param[i], super.field);
                    }
                    fout.println();

                    for(int j=0; j<super.nParam;j++){
                        fout.printtab(param[j], super.field);
                        for(int i=0; i<super.nParam;i++){
                            fout.printtab(Fmath.truncate(super.corrCoeff[i][j], super.prec), super.field);
                        }
                        fout.println();
                    }
                }

                fout.println();
                fout.printtab("Durbin-Watson d statistic");
                fout.println(Fmath.truncate(super.getDurbinWatsonD(), super.prec));
                fout.println();

                if(super.bestPolyFlag){

                    fout.println("Method bestPolynomial search history");
                    fout.println("F-probability significance level: " + super.fProbSignificance + " (" + super.fProbSignificance*100.0 + " %)");
                    fout.println("Degree of best fit polynomial " + super.bestPolynomialDegree);
                    fout.println(" ");

                    fout.print("Polynomial degree", 2*super.field);
                    fout.print("chi square", 2*super.field);
                    fout.print("F-ratio", super.field);
                    fout.print("F-probability", super.field+2);
                    fout.println("F-value at the");

                    fout.print("comparison", 2*super.field);
                    fout.print("comparison", 2*super.field);
                    fout.print("   ", super.field);
                    fout.print("   ", super.field+2);
                    fout.println("significance level");

                    int nAttempts = (Integer)super.bestPolyArray.get(1);
                    int[] deg0s = (int[])super.bestPolyArray.get(2);
                    int[] deg1s = (int[])super.bestPolyArray.get(3);
                    double[] chi0s = (double[])super.bestPolyArray.get(4);
                    double[] chi1s = (double[])super.bestPolyArray.get(5);
                    double[] fRatios = (double[])super.bestPolyArray.get(6);
                    double[] fProbs = (double[])super.bestPolyArray.get(7);
                    double[] fSigns = (double[])super.bestPolyArray.get(8);

                    for(int i=0; i<nAttempts; i++){
                        fout.print(deg0s[i], super.field);
                        fout.print(deg1s[i], super.field);
                        fout.print(Fmath.truncate(chi0s[i], super.prec), super.field);
                        fout.print(Fmath.truncate(chi1s[i], super.prec), super.field);
                        fout.print(Fmath.truncate(fRatios[i], super.prec), super.field);
                        fout.print(Fmath.truncate(fProbs[i], super.prec), super.field+2);
                        fout.println(Fmath.truncate(fSigns[i], super.prec));

                    }

                }

                fout.println();
                fout.println("Coefficient of determination,   =                   " + Fmath.truncate(super.multR, super.prec));
                fout.println("Adjusted Coefficient of determination,    =         " + Fmath.truncate(super.adjustedR, super.prec));
                fout.println("Coefficient of determination, F-ratio =             " + Fmath.truncate(super.multipleF, super.prec));
                fout.println("Coefficient of determination, F-ratio probability = " + Fmath.truncate(super.multipleFprob, super.prec));
                fout.println("Total (weighted) sum of squares  =                  " + Fmath.truncate(super.sumOfSquaresTotal, super.prec));
                fout.println("Regression (weighted) sum of squares  =             " + Fmath.truncate(super.sumOfSquaresRegrn, super.prec));
                fout.println("Error (weighted) sum of squares  =                  " + Fmath.truncate(super.chiSquare, super.prec));

            }
    }

    // Two equation Comparison methods

    // Compare fitting of two methods to determine best fit
    // Use select windows to select equations
    // default significance level and default filename
    public void compare(){
        this.selectCompare(this.significance, this.compFilename);
    }

    // Compare fitting of two methods to determine best fit
    // Use select windows to select equations
    // ddefault filename
    public void compare(double significance){
        this.selectCompare(significance, this.compFilename);
    }

    // Compare fitting of two methods to determine best fit
    // Use select windows to select equations
    // default significance
    public void compare(String filename){
        this.selectCompare(this.significance, filename);
    }

    // Compare fitting of two methods to determine best fit
    // Use select windows to select equations
    public void compare(double significance, String filename){
        this.selectCompare(significance, filename);
    }

    // private common method for a comparison with a selection window
    private void selectCompare(double significance, String filename){
        this.compWindow = true;

        // select first equation
        ArrayList<Object> choice = this.chooseEquation(1);
        int model1index = ((Integer)choice.get(0)).intValue();
        int degree1 = 0;
        int nTerms1 = 0;
        double bottom1 = 0;
        double top1 = 0;
        if(model1index==6){
            degree1 = ((Integer)choice.get(1)).intValue();
            this.degSet = true;
        }
        if(model1index==7){
            nTerms1 = ((Integer)choice.get(1)).intValue();
            this.nTermsSet = true;
        }
        if(model1index==2){
            bottom1 = ((Double)choice.get(2)).doubleValue();
            top1 = ((Double)choice.get(3)).doubleValue();
            this.fiveBotTopSet = true;
        }
        if(model1index==4){
            bottom1 = ((Double)choice.get(2)).doubleValue();
            top1 = ((Double)choice.get(3)).doubleValue();
            this.fourBotTopSet = true;
        }



        // select second equation
        choice = this.chooseEquation(2);
        int model2index = ((Integer)choice.get(0)).intValue();
        int degree2 = 0;
        int nTerms2 = 0;
        double bottom2 = 0;
        double top2 = 0;
        if(model2index==6){
            degree2 = ((Integer)choice.get(1)).intValue();
            this.degSet = true;
        }
        if(model2index==7){
            nTerms2 = ((Integer)choice.get(1)).intValue();
            this.nTermsSet = true;
        }
        if(model2index==2){
            bottom2 = ((Double)choice.get(2)).doubleValue();
            top2 = ((Double)choice.get(3)).doubleValue();
            this.fiveBotTopSet = true;
        }
        if(model2index==4){
            bottom2 = ((Double)choice.get(2)).doubleValue();
            top2 = ((Double)choice.get(3)).doubleValue();
            this.fourBotTopSet = true;
        }
        this.compare(model1index, degree1, bottom1, top1, nTerms1, model2index, degree2, bottom2, top2, nTerms2, significance, filename);
    }

    // Compare fitting of two methods to determine best fit
    // default significance level and default filename, no degrees entered
    public void compare(int model1index, int model2index){
        this.compare(model1index, 0, 0, 0, 0, model2index, 0, 0, 0, 0, this.significance, this.compFilename);
    }

    // Compare fitting of two methods to determine best fit
    // default file name, no degrees entered
    public void compare(int model1index, int model2index, double significance){
        this.compare(model1index, 0, 0, 0, 0, model2index, 0, 0, 0, 0, significance, this.compFilename);
    }


    // Compare fitting of two methods to determine best fit
    // default file name, degrees entered
    public void compare(int model1index, int degree1, int model2index, int degree2, double significance){
        this.degSet = true;
        this.compare(model1index, degree1, 0, 0, 0, model2index, degree2, 0, 0, 0, significance, this.compFilename);
    }

    // Compare fitting of two methods to determine best fit
    // default significance level, no degrees entered
    public void compare(int model1index, int model2index, String filename){
        this.compare(model1index, 0, 0, 0, 0, model2index, 0, 0, 0, 0, this.significance, filename);
    }

    // Compare fitting of two methods to determine best fit
    // default significance level, degrees entered
    public void compare(int model1index, int degree1, int model2index, int degree2, String filename){
        this.degSet = true;
        this.compare(model1index, degree1, 0, 0, 0, model2index, degree2, 0, 0, 0, this.significance, filename);
    }

    // Compare fitting of two methods to determine best fit
    // default significance level and default filename, two degrees entered
    public void compare(int model1index, int degree1, int model2index, int degree2){
        this.degSet = true;
        this.compare(model1index, degree1, 0, 0, 0, model2index, degree2, 0, 0, 0, this.significance, this.compFilename);
    }

    // Compare fitting of two methods to determine best fit
    public void compare(int model1index, int degree1, double bottom1, double top1, int nTerms1, int model2index, int degree2, double bottom2, double top2, int nTerms2, double significance, String filename){
        if(!this.degSet){
            if(degree1>0 || degree2>0)this.degSet = true;
        }

        double[] sumOfSquaresComp = new double[2];
        int[] nParamComp = new int[2];
        String[] methodNameComp = new String[2];
        int[] modelXindices = {model1index, model2index};

        // Fit to first equation
        if(!this.compWindow){
            if(model1index==2){
                bottom1 = Db.readDouble("Enter five parameter logistic bottom value");
                top1 = Db.readDouble("Enter five parameter logistic top value");
            }
            if(model1index==4){
                bottom1 = Db.readDouble("Enter four parameter logistic bottom value");
                top1 = Db.readDouble("Enter four parameter logistic top value");
            }
            if(model1index==6 && !this.degSet){
                degree1 = Db.readInt("Enter the polynomial degree");
            }
            if(model1index==7){
                nTerms1 = Db.readInt("Enter the non-integer polynomial number of terms");
            }
        }

        ArrayList<Object> al1 = this.fittingForCompare(model1index, degree1, bottom1, top1, nTerms1, false);
        sumOfSquaresComp[0] = ((Double)al1.get(0)).doubleValue();
        nParamComp[0] =((Integer)al1.get(1)).intValue();
        methodNameComp[0] = (String)al1.get(2);

        // Fit to second equation
        if(!this.compWindow){
            if(model1index==2){
                bottom2 = Db.readDouble("Enter five parameter logistic bottom value");
                top2 = Db.readDouble("Enter five parameter logistic top value");
            }
            if(model1index==4){
                bottom2 = Db.readDouble("Enter four parameter logistic bottom value");
                top2 = Db.readDouble("Enter four parameter logistic top value");
            }
            if(model1index==6 && !this.degSet){
                degree2 = Db.readInt("Enter the polynomial degree");
            }
            if(model1index==7){
                nTerms2 = Db.readInt("Enter the non-integer polynomial number of terms");
            }
        }
        ArrayList<Object> al2 = this.fittingForCompare(model2index, degree2, bottom2, top2, nTerms2, true);
        sumOfSquaresComp[1] = ((Double)al2.get(0)).doubleValue();
        nParamComp[1] =((Integer)al2.get(1)).intValue();
        methodNameComp[1] = (String)al2.get(2);


        // Compare the goodness of fit to the two equations
        ArrayList<Object> al3 = ImmunoAssay.comparisonTest(modelXindices, sumOfSquaresComp[0], nParamComp[0], this.nAnalyteConcns, sumOfSquaresComp[1], nParamComp[1], this.nAnalyteConcns, significance);
        int resultFlag = ((Integer)al3.get(0)).intValue();
        int model1indexn = ((Integer)al3.get(1)).intValue();
        int model2indexn = ((Integer)al3.get(2)).intValue();
        double fRatio = ((Double)al3.get(3)).doubleValue();
        double fProb = ((Double)al3.get(4)).doubleValue();
        double fRatioAtSignificanceLevel = ((Double)al3.get(5)).doubleValue();
        int degreesOfFreedom1 = ((Integer)al3.get(6)).intValue();
        int degreesOfFreedom2 = ((Integer)al3.get(7)).intValue();
        if(model1indexn!=model1index){
            int holdI = nParamComp[0];
            nParamComp[0] = nParamComp[1];
            nParamComp[1] = holdI;
            double holdD = sumOfSquaresComp[0];
            sumOfSquaresComp[0] = sumOfSquaresComp[1];
            sumOfSquaresComp[1] = holdD;
            String holdS = methodNameComp[0];
            methodNameComp[0] = methodNameComp[1];
            methodNameComp[1] = holdS;
        }

        // Print the analysis to a text file
        int pos = filename.indexOf(".");
        if(pos==-1)filename = filename + ".txt";
        FileOutput fout = new FileOutput(filename);
        fout.println(this.titleZero);
        fout.println("Comparison of two fitting procedures");
        if(this.dataRead)fout.println("Data input file name " + dataFilename);
        fout.dateAndTimeln(filename);
        fout.println();
        fout.println("Equations compared:");
        fout.println("   Equation One: " + methodNameComp[0]);
        fout.println("   Equation Two: " + methodNameComp[1]);
        fout.println();

        fout.print("                    ", super.field);
        fout.print("Eqation", super.field);
        fout.println("Eqation");

        fout.print("                    ", super.field);
        fout.print("One", super.field);
        fout.println("Two");

        fout.print("Sum of squares      ", super.field);
        fout.print(Fmath.truncate(sumOfSquaresComp[0], super.prec), super.field);
        fout.println(Fmath.truncate(sumOfSquaresComp[1], super.prec));

        fout.print("Degrees of freedom  ", super.field);
        fout.print((this.nAnalyteConcns-nParamComp[0]), super.field);
        fout.println((this.nAnalyteConcns-nParamComp[1]));

        fout.println();

        switch(resultFlag){
            case 0: fout.printtab("Extra sum of squares F-ratio =                 ");
                    fout.println(Fmath.truncate(fRatio, super.prec));
                    fout.printtab("F-ratio probabilty =                           ");
                    fout.println(Fmath.truncate(fProb, super.prec));
                    fout.printtab("F value at the " + significance + " significance level = ");
                    fout.println(Fmath.truncate(fRatioAtSignificanceLevel, super.prec));
                    fout.println();
                    if(fProb<=significance){
                        fout.println("In terms of a best fit Equation Two is the preferred fit.");
                        if(Math.abs(nParamComp[1]-nParamComp[0])==1){
                            fout.println("The additional parameter has, given a " + significance + " significance level, significantly improved the fit.");
                        }
                        else{
                            fout.println("The additional parameters have, given a " + significance + " significance level, significantly improved the fit.");
                        }
                    }
                    else{
                        fout.println("In terms of a best fit Equation One is the preferred fit");
                        if(Math.abs(nParamComp[1]-nParamComp[0])==1){
                            fout.println("The additional parameter has not, given a " + significance + " significance level, significantly improved the fit.");
                        }
                        else{
                            fout.println("The additional parameters have not, given a " + significance + " significance level, significantly improved the fit.");
                        }
                    }
                   break;
            case 1: fout.println("The fittings to the two equations cannot be distinguished using an F-test analysis");
                    break;
            case 2: fout.printtab("Variance F-ratio =                            ");
                    fout.println(Fmath.truncate(fRatio, super.prec));
                    fout.printtab("F-ratio probabilty =                          ");
                    fout.println(Fmath.truncate(fProb, super.prec));
                    fout.printtab("F value at the " + significance + " significance level = ");
                    fout.println(Fmath.truncate(fRatioAtSignificanceLevel, super.prec));
                    fout.println();
                    if(fProb<=significance){
                        fout.println("In terms of a best fit Equation Two is the preferred fit");
                        fout.println("as indicated by the F-ratio analysis and a given significance level of " + significance);
                    }
                    else{
                        fout.println("The fittings to the two equations cannot be distinguished using an F-test analysis");
                    }
                    break;
        }
        fout.println("However, the choice of the model to be used as a standard curve should include, along with this comparison,");
        fout.println("observation of the displayed graphs and of the detailed analyses listed below");


        fout.println();
        fout.println();
        fout.println("Details of the two compared fitting exercises");
        fout.println();
        FileInput fin = new FileInput("ImmunoAssayTemp.txt");
        int n = fin.numberOfLines();
        String line = null;
        for(int i=0; i<n; i++){
            line = fin.readLine();
            fout.println(line);
        }
        fin.close();
        fout.println();
        fout.println("End of file");
        fout.close();

        // Delete the temporary file
        this.deleteFile("ImmunoAssayTemp.txt");
    }

    // Perform fitting for comparison method
    private ArrayList<Object> fittingForCompare(int index, int degree, double bottom, double top, int nTerms, boolean catFlag){

        ArrayList<Object> al = new ArrayList<Object>();
        String methodName = null;

        switch(index){
            case 1: this.fiveParameterLogisticFit();
                    methodName = this.methodNames[1];
                    break;
            case 2: this.fiveParameterLogisticFit(bottom, top);
                    methodName = methodNames[2];
                    break;
            case 3: this.fourParameterLogisticFit();
                    methodName = methodNames[3];
                    break;
            case 4: this.fourParameterLogisticFit(bottom, top);
                    methodName = methodNames[4];
                    break;
            case 5: this.bestPolynomialFit();
                    methodName = methodNames[5] + ": degree = " + this.bestPolyDegree;
                    break;
            case 6: this.polynomialFit(degree);
                    methodName = methodNames[6] + degree;
                    break;
            case 7: this.nonIntegerPolynomialFit(nTerms);
                    methodName = methodNames[7];
                    break;
            case 8: this.sigmoidThresholdFit();
                    methodName = methodNames[8];
                    break;
            case 9: this.sipsSigmoidFit();
                    methodName = methodNames[9];
                    break;
            case 10: this.shiftedRectangularHyperbolaFit();
                    methodName = methodNames[10];
                    break;
            case 11: this.rectangularHyperbolaFit();
                    methodName = methodNames[11];
                    break;
            case 12: this.amershamFit();
                    methodName = methodNames[12];
                    break;

            default: throw new IllegalArgumentException("Method number " + index + " not recognised");
        }

        // Output results to a temporary file
        FileOutput fout = null;
        String tempFileName = "ImmunoAssayTemp.txt";
        if(catFlag){
            fout = new FileOutput(tempFileName, 'a');
        }
        else{
            fout = new FileOutput(tempFileName);
        }
        this.commonPrint(fout);
        fout.println();
        fout.println();
        fout.close();

        // Return data to compare method
        al.add(new Double(super.chiSquare));
        al.add(new Integer(super.nParam));
        al.add(methodName);

        return al;
    }


    // Comparison test for two fitted models using F-ratio analysis
    public static ArrayList<Object> comparisonTest(int modelXindices[], double sumOfSquares1, int nParameters1, int nData1, double sumOfSquares2, int nParameters2, int nData2, double significance){

        ArrayList<Object>  al = new ArrayList<Object>();
        double fRatio = Double.NaN;
        double fProb = Double.NaN;
        double fRatioAtSignificanceLevel =  Double.NaN;
        double[] sumOfSquares = {sumOfSquares1, sumOfSquares2};
        int[] nParameters = {nParameters1, nParameters2};
        int[] nData = {nData1, nData2};
        int[] degreesOfFreedom = {(nData1 - nParameters1), (nData2 - nParameters2)};
        double[] variances = new double[2];
        double extraSumOfSquares = Double.NaN;
        int extraSumDegreesOfFreedom = 0;
        int extraDegreesOfFreedom = 0;
        int resultFlag = -1;

        if(nParameters1!=nParameters2 && nData1==nData2){
            // different number of parameters
            if(nParameters[0]>nParameters[1]){
                double holdD =sumOfSquares[0];
                sumOfSquares[0] = sumOfSquares[1];
                sumOfSquares[1] = holdD;
                int holdI = nParameters[0];
                nParameters[0] = nParameters[1];
                nParameters[1] = holdI;
                holdI = nData[0];
                nData[0] = nData[1];
                nData[1] = holdI;
                holdI = degreesOfFreedom[0];
                degreesOfFreedom[0] = degreesOfFreedom[1];
                degreesOfFreedom[1] = holdI;
                holdI = modelXindices[0];
                modelXindices[0] = modelXindices[1];
                modelXindices[1] = holdI;
            }
            if(sumOfSquares[0]>sumOfSquares[1]){
                // if sum of squares reduced with addition of extra parameters
                extraSumOfSquares = sumOfSquares[0] - sumOfSquares[1];
                extraSumDegreesOfFreedom = degreesOfFreedom[0] - degreesOfFreedom[1];
                fRatio =  extraSumOfSquares*degreesOfFreedom[1]/(extraSumDegreesOfFreedom*sumOfSquares[1]);
                fProb = Stat.fCompCDF(fRatio, extraSumDegreesOfFreedom, degreesOfFreedom[1]);
                fRatioAtSignificanceLevel =  Stat.fTestValueGivenFprob(significance, extraSumDegreesOfFreedom, degreesOfFreedom[1]);
                resultFlag = 0;
            }
        }
        if(resultFlag==-1){
            // identical number of parameters or non-reduced sum of squares on adding extra parameters or non-identical number of data points
            if(sumOfSquares[0]==sumOfSquares[1] && degreesOfFreedom[0]==degreesOfFreedom[1]){
                // equal number of degrees of freedom and sum of squares
                resultFlag = 1;
            }
            else{
                // different number of degrees of freedom and/or different sum of squares
                variances[0] = sumOfSquares[0]/degreesOfFreedom[0];
                variances[1] = sumOfSquares[1]/degreesOfFreedom[1];
                if(variances[0]<=variances[1]){
                    double holdD = variances[0];
                    variances[0] = variances[1];
                    variances[1] = holdD;
                    holdD =sumOfSquares[0];
                    sumOfSquares[0] = sumOfSquares[1];
                    sumOfSquares[1] = holdD;
                    int holdI = nParameters[0];
                    nParameters[0] = nParameters[1];
                    nParameters[1] = holdI;
                    holdI = nData[0];
                    nData[0] = nData[1];
                    nData[1] = holdI;
                    holdI = degreesOfFreedom[0];
                    degreesOfFreedom[0] = degreesOfFreedom[1];
                    degreesOfFreedom[1] = holdI;
                    holdI = modelXindices[0];
                    modelXindices[0] = modelXindices[1];
                    modelXindices[1] = holdI;
                }
                fRatio = variances[0]/variances[1];
                fProb = Stat.fCompCDF(fRatio, degreesOfFreedom[0], degreesOfFreedom[1]);
                fRatioAtSignificanceLevel =  Stat.fTestValueGivenFprob(significance, degreesOfFreedom[0], degreesOfFreedom[1]);
                resultFlag = 2;
            }
        }

        // return data to compare method
        al.add(new Integer(resultFlag));
        al.add(new Integer(modelXindices[0]));
        al.add(new Integer(modelXindices[1]));
        al.add(new Double(fRatio));
        al.add(new Double(fProb));
        al.add(new Double(fRatioAtSignificanceLevel));
        al.add(new Integer(degreesOfFreedom[0]));
        al.add(new Integer(degreesOfFreedom[1]));
        al.add(new Integer(nData[0]));
        al.add(new Integer(nData[1]));

        return al;
    }

    // Delete a file
    // used by compare method to delete a temporary file
    public void deleteFile(String fileName){

        // deletion flag
        boolean flag = true;

        // Create an instance of File representing the named file
        File file0 = new File(fileName);

        // Check that the file exists
        if(!file0.exists()){
            System.err.println("Method deleteFile: no file or directory of the name " + fileName + " found");
            flag = false;
        }

        // Check whether FileName is write protected
        if(flag && !file0.canWrite()){
            System.err.println("Method deleteFile: " + fileName + " is write protected and cannot be deleted");
            flag = false;
        }

        // Check, if fileName is a directory, that it is empty
        if(flag && file0.isDirectory()){
            String[] dirFiles = file0.list();
            if (dirFiles.length > 0){
                System.err.println("Method deleteFile: " + fileName + " is a directory which is not empty; no action was taken");
                flag = false;
            }
        }

        // Delete file
        if(flag){
            flag = file0.delete();
            if(!flag)System.err.println("Method deleteFile: deletion of the temporary file " + fileName + " failed");
        }
    }

    // ANCILLARY METHODS

    // Returns true if all x[i+1]>x[i] or all x[i+1]<x[i]
    public static boolean isMonotonic(double[] x){
        boolean monoFlag = true;
        int n = x.length;
        double gap1 = x[1] - x[0];
        double gap0 = gap1;
        if(gap1==0.0){
            monoFlag = false;
        }
        else{
            for(int i=2; i<n; i++){
                gap1 = x[i] - x[i-1];
                if(gap1==0.0){
                    monoFlag = false;
                }
                else{
                    if(Fmath.sign(gap1)!=Fmath.sign(gap0))monoFlag = false;
                }
                if(!monoFlag)break;
            }
        }

        return monoFlag;
    }

    // surface number concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns number of protein molecules per square metre
    public static double surfaceNumberConcn(double molWt, double specVol){
        return ImmunoChemistry.surfaceNumberConcn(molWt, specVol);
    }

    // surface number concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    //         specific volume = 0.74E-3 m/kg (0.74 ml/gm)
    // requires effective radius in m
    // returns number of protein molecules per square metre
    public static double surfaceNumberConcn(double effectiveRadius){
        return ImmunoChemistry.surfaceNumberConcn(effectiveRadius);
    }

    // surface molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns moles of protein molecules per square metre
    public static double surfaceMolarConcn(double molWt, double specVol){
        return ImmunoChemistry.surfaceMolarConcn(molWt, specVol);
    }

    // surface molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    //         specific volume = 0.74E-3 m/kg (0.74 ml/gm)
    // requires effective radius in m
    // returns moles of protein molecules per square metre
    public static double surfaceMolarConcn(double effectiveRadius){
        return ImmunoChemistry.surfaceMolarConcn(effectiveRadius);
    }

    // equivalent volume molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          surface area in m
    //          volume in m^3
    //          specific volume in m/kg
    // returns molar concentration
    public static double equivalentVolumeConcn(double molWt, double area, double volume, double specVol){
        return ImmunoChemistry.equivalentVolumeConcn(molWt, area, volume, specVol);
    }

    // equivalent volume molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires effective radius in m
    //          surface area in m
    //          volume in m^3
    // returns molar concentration
    public static double equivalentVolumeConcn(double effectiveRadius, double area, double volume){
        return ImmunoChemistry.equivalentVolumeConcn(effectiveRadius, area, volume);
    }

    // converts a surface concentration to an equivalent  volume molar concentration for an immobilised protein
    // requires surface concentration in moles per square metre
    //          surface area in m
    //          volume in m^3
    // returns molar concentration
    public static double convertSurfaceToVolumeConcn(double surfaceConcn, double area, double volume){
        return ImmunoChemistry.convertSurfaceToVolumeConcn(surfaceConcn, area, volume);
    }

    // molecular radius of a protein
    // assumes globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns molecular radius in metres
    public static double molecularRadius(double molWt, double specVol){
        return ImmunoChemistry.molecularRadius(molWt, specVol);
    }

    // molecular radius of a protein
    // assumes globular protein
    //         specific volume = 0.74E-3 m/kg (0.74 ml/gm)
    // requires molecular weight
    // returns molecular radius in metres
    public static double molecularRadius(double molWt){
        return ImmunoChemistry.molecularRadius(molWt, 0.74E-03);
    }

        // effective radius of a protein
    // assumes globular protein
    // requires solute diffusion coefficient im square metres per second
    //          solution viscosity in Pa s
    //          temperature in degrees Celsius
    // returns effective radius in metres
    public static double effectiveRadius(double diffusionCoefficient, double viscosity, double temperature){
	    return  ImmunoChemistry.effectiveRadius(diffusionCoefficient, viscosity, temperature);
    }


    // Return the molecular weight of IgG1
    public static double getMolWeightIgG1(){
        return ImmunoChemistry.molecularWeightIgG1;
    }

    // Return the molecular weight of IgG2
    public static double getMolWeightIgG2(){
        return ImmunoChemistry.molecularWeightIgG2;
    }

    // Return the molecular weight of IgG3
    public static double getMolWeightIgG3(){
        return ImmunoChemistry.molecularWeightIgG3;
    }

    // Return the molecular weight of IgG4
    public static double getMolWeightIgG4(){
        return ImmunoChemistry.molecularWeightIgG4;
    }

    // Return the molecular weight of IgM
    public static double getMolWeightIgM(){
        return ImmunoChemistry.molecularWeightIgM;
    }

    // Return the molecular weight of IgA1
    public static double getMolWeightIgA1(){
        return ImmunoChemistry.molecularWeightIgA1;
    }

    // Return the molecular weight of IgA2
    public static double getMolWeightIgA2(){
        return ImmunoChemistry.molecularWeightIgA2;
    }

    // Return the molecular weight of IgD
    public static double getMolWeightIgD(){
        return ImmunoChemistry.molecularWeightIgD;
    }

    // Return the molecular weight of IgE
    public static double getMolWeightIgE(){
        return ImmunoChemistry.molecularWeightIgE;
    }

}

// Class to evaluate Amersham equation
class Amersham implements RegressionFunction{

    // p; 0 assocK, 1 label, 2 antibody, 3 nsb, 4 scale
    // x; 0 analyte concentration
    public double function(double[] p, double[] x){

        return this.calcResponse(p[0], p[1],  p[2], p[3], x[0], p[4]);
    }

    // Calculate response
    public double calcResponse(double assocK, double labelF,  double antibodyF,  double nsbF, double analyteF, double scaleF){
        double term1 = assocK + labelF + analyteF;
        double numer = 2.0*antibodyF*(labelF - nsbF)/labelF;
        double denom = term1 + antibodyF + Math.sqrt(Fmath.square(term1 - antibodyF) + 4.0*assocK*antibodyF);
        double signal = scaleF*(numer/denom + nsbF/labelF);
        return signal;
    }
}



