/*
*   CLASS:      ANOVA
*
*   USAGE:      Class for performing a one way ANOVA
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       December 2011
*   AMENDED:     
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ANOVA.html
*
*   Copyright (c) 2011 Michael Thomas Flanagan
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
import java.text.*;

import flanagan.math.*;
import flanagan.io.*;

public class ANOVA{
 
    private String[] title = new String[3];                                         // title
    private int nTitle = 0;                                                         // number of title lines
    private boolean titleCheck = false;                                             // = true when title entered
  
    private String inputFilename = null;                                            // input file name if input data read from file
    private String outputFilename = null;                                           // output file name if output written to file
    private int fileOption = 1;                                                     // type of file option
                                                                                    // option = 1 - text file  (.txt)
                                                                                    // option = 2 - MS Excel file (.xls)
    private boolean fileOptionSet = false;                                          // = true if fileOption changed by user
  
    private int trunc = 6;                                                          // number of decimal places in output data
    private int fieldD = 13;                                                        // decimal and String field length in output files
                                                                                    // overriden by the precision of the input data if this is greater
    private boolean truncAll = false;                                               // if true - the above truncation is not overriden by the precision of the input data if this is greater

    private int originalDataType = -1;                                              // = 1  - String[][]  (including read from file);
                                                                                    // = 2  - double[][]
                                                                                    // = 3  - Matrix
                                                                                    // = 4  - float[][]
                                                                                    // = 5  - int[][]
                                                                                    // = 6  - char[][]
                                                                                    // = 7  - boolean[][]
                                                                                    // = 8  - long[][]
                                                                                    // = 9  - BigDecimal[][]
                                                                                    // = 10 = BigInteger[][]

    private Object originalData = null;                                             // Original data as entered

    private double[][]responses0 = null;                                            // individual responses as double
    private BigDecimal[][]responsesBD = null;                                       // individual responses as BigDecimal                                                                                   // arranged as rows of responses for each group
                                                                                    // e.g. responses0[0][0] to responses0[0][nIndividuals-1] =  responses in turn for the first group
                                                                                    //      responses0[1][0] to responses0[1][nIndividuals-1] =  responses in turn for the second group
                                                                                    // etc.
    
    private boolean dataEntered = false;                                            // = true when responses entered
    private boolean bigDecimal = false;                                             // = true if data entered a BigDecimal or BigInteger
   
    private int nGroups = 0;                                                        // number of groups
    private String[] groupNames = null;                                             // names of the groups
    private boolean groupNamesSet = false;                                          // = true when group names entered

    private int nTotalResponses = 0;                                                // total number of responses
    private int[] nResponsesPerGroup = null;                                        // number of responses for each group
    private int[] typePerGroup = null;                                              // data type for each group
    
    private String[] dichotomousS = {"yes", "no", "y", "n", "true", "false"};       // dichotomous options as words
    private char[] dichotomousC = {'y', 'n'}; 
    private double dichotTrue = 1.0;                                                // value of true, yes, y ... in a dichotomous pait 
    private double dichotFalse = -1.0;// dichotomous options as char
    private double[] dichotomousDoubleS = {dichotTrue, dichotFalse, dichotTrue, dichotFalse, dichotTrue, dichotFalse};  // double equivalents of the word pairs    
    private double[] dichotomousDoubleC = {dichotTrue, dichotFalse};                // double equivalents of the char pairs
    private int nDichotomousS = 6;                                                  // number of dichotomous words
    private int nDichotomousC = 2;                                                  // number of dichotomous char    private double dichotFalse = -1.0;                                              // value of flase, no, n ... in a dichotomous pait 
      
    private boolean nFactorOption = false;                                          // = true  varaiance, covariance and standard deviation denominator = n
                                                                                    // = false varaiance, covariance and standard deviation denominator = n-1
    private double[] groupMeans = null;                                             // group means as double
    private BigDecimal[] groupMeansBD = null;                                       // group means as bigDecimal
    private double[] groupSD = null;                                                // group standard deviations
    private double[] groupSE = null;                                                // group standard errors
    private double[] groupClb = null;                                               // lower bound of the group mean entered confidence limit
    private double[] groupCub = null;                                               // upper bound of the group mean entered confidence limit
    private double[] groupMinimum = null;                                           // group minimum as double
    private BigDecimal[] groupMinimumBD = null;                                     // group minimum as BigDecimal
    private double[] groupMaximum = null;                                           // group maximum as double
    private BigDecimal[] groupMaximumBD = null;                                     // group maximum as BigDecimal
    private double[] groupSS = null;                                                // group sum of squares as double
    private BigDecimal[] groupSSBD = null;                                          // group sum of squares as BigDecimal
    private double[] groupMedians = null;                                           // group medians as double
    private BigDecimal[] groupMediansBD = null;                                     // group medians as BigDecimal
    private double[] groupMomentSkewness = null;                                    // group moment skewnesses as double 
    private double[] groupMedianSkewness = null;                                    // group median skewnesses as double 
    private double[] groupQuartileSkewness = null;                                  // group quartile skewnesses as double 
    private BigDecimal[] groupQuartileSkewnessBD = null;                            // group quartile skewnesses as BigDecimal 
    private double[] groupKurtosis = null;                                          // group kurtoses as double 
    private BigDecimal[] groupKurtosisBD = null;                                    // group kurtoses as BigDecimal  
    private double[] groupExcessKurtosis = null;                                    // group excess kurtoses as double 
    private BigDecimal[] groupExcessKurtosisBD = null;                              // group excess kurtoses as BigDecimal 
    private double[] groupProbPlotR = null;                                         // group gaussian probability plot correlation coefficients
    private double[] groupProbPlotGradient = null;                                  // group gaussian probability plot gradients
    private double[] groupProbPlotIntercept = null;                                 // group gaussian probability plot correlation intercepts
    private double[] groupProbPlotMu = null;                                        // group gaussian probability plot mu
    private double[] groupProbPlotSigma = null;                                     // group gaussian probability plot sigma
    private boolean groupStatsDone = false;                                         // = true when group statistics have been performed
    
    private double totalMean = 0.0;                                                 // total mean as double
    private BigDecimal totalMeanBD = new BigDecimal(0.0);                           // total mean as BigDecimal
    private double totalSD = 0.0;                                                   // total standard deviation
    private double totalSE = 0.0;                                                   // total error
    private double totalClb = 0.0;                                                  // lower bound of the total mean entered confidence limit
    private double totalCub = 0.0;                                                  // upper bound of the total mean entered confidence limit
    private double totalMinimum = 0.0;                                              // total minimum as double
    private BigDecimal totalMinimumBD = new BigDecimal("0.0");                      // total minimum as BigDecimal
    private double totalMaximum = 0.0;                                              // total maximum as double
    private BigDecimal totalMaximumBD = new BigDecimal("0.0");                      // total maximum as BigDecimal
    private double totalSS = 0.0;                                                   // total sum of squares as double
    private BigDecimal totalSSBD = new BigDecimal("0.0");                           // total sum of squares as BigDecimal
    private double totalMedian = 0.0;                                               // total medians as double
    private BigDecimal totalMedianBD = new BigDecimal("0.0");                       // total medians as BigDecimal
    private double totalMomentSkewness = 0.0;                                       // total moment skewnesses as double 
    private double totalMedianSkewness = 0.0;                                       // total median skewnesses as double 
    private double totalQuartileSkewness = 0.0;                                     // total quartile skewnesses as double 
    private BigDecimal totalQuartileSkewnessBD = new BigDecimal("0.0");             // total quartile skewnesses as BigDecimal 
    private double totalKurtosis = 0.0;                                             // total kurtoses as double 
    private BigDecimal totalKurtosisBD = new BigDecimal("0.0");                     // total kurtoses as BigDecimal  
    private double totalExcessKurtosis = 0.0;                                       // total excess kurtoses as double 
    private BigDecimal totalExcessKurtosisBD = new BigDecimal("0.0");               // total excess kurtoses as BigDecimal 
    private double totalProbPlotR = 0.0;                                            // total gaussian probability plot correlation coefficient
    private double totalProbPlotGradient = 0.0;                                     // total gaussian probability plot gradient
    private double totalProbPlotIntercept = 0.0;                                    // total gaussian probability plot correlation intercept
    private double totalProbPlotMu = 0.0;                                           // total gaussian probability plot mu
    private double totalProbPlotSigma = 0.0;                                        // total gaussian probability plot sigma
 
                                             
    private int dofTotal = 0;                                                       // total degrees of freedom
    private int dofWithinGroups = 0;                                                // degrees of freedom within groups
    private int dofBetweenGroups = 0;                                               // degrees of freedom between groups
  
    private double ssTotal = 0.0;                                                   // total sum of squares as double
    private double ssWithin = 0.0;                                                  // sum of squares within groups as double
    private double ssBetween = 0.0;                                                 // sum of squares between groups as double
    private BigDecimal ssTotalBD = new BigDecimal("0.0");                           // total sum of squares as BigDecimal
    private BigDecimal ssWithinBD = new BigDecimal("0.0");                          // sum of squares within groups as BigDecimal
    private BigDecimal ssBetweenBD = new BigDecimal("0.0");                         // sum of squares between groups as BigDecimal
   
    private double meanSquareTotal = 0.0;                                           // total  mean square as double
    private double meanSquareWithin = 0.0;                                          // mean square within groups as double
    private double meanSquareBetween = 0.0;                                         // mean square between groups as double
    private BigDecimal meanSquareTotalBD = new BigDecimal("0.0");                   // total mean square as BigDecimal
    private BigDecimal meanSquareWithinBD = new BigDecimal("0.0");                  // mean square within groups as BigDecimal
    private BigDecimal meanSquareBetweenBD = new BigDecimal("0.0");                 // mean square between groups as BigDecimal
    
    private double fRatio = 0.0;                                                    // one way ANOVA F-ratio as double
    private BigDecimal fRatioBD = new BigDecimal("0.0");                            // one way ANOVA F-ratio as BigDecimal
    private double fRatioP = 0.0;                                                   // one way ANOVA F-ratio probability
    private boolean oneWayDone = false;                                             // = true when one way ANOVA complete
    private double criticalSignificance = 0.95;                                      // critical probability
    private String criticalSignificanceS = "95%";                                    // critical probability as percentage as String;
    private String criticalSignificanceF = "5%";                                     // 1 - critical probability as percentage as String;
    private double criticalFratio = 0.0;                                            // critical F-ratio
    
    private boolean[] compGroups = null;                                            // element = true if group to be included in the group comparisons
    private boolean comparison = false;                                             // = true if comparison between pairs of groups required
    private int nComparisons = 0;                                                   // number of pair comparisons
    private int[][] pairIndices = null;                                             // indices of comparison pairs
    private double[] meanSquareTotalG = null;                                       // total  mean square of pair comparisons as double
    private double[] meanSquareWithinG = null;                                      // mean square within groups of pair comparisons as double
    private double[] meanSquareBetweenG = null;                                     // mean square between groups of pair comparisons as double
    private BigDecimal[] meanSquareTotalBDG = null;                                 // total mean square of pair comparisons as BigDecimal
    private BigDecimal[] meanSquareWithinBDG = null;                                // mean square within groups of pair comparisons as BigDecimal
    private BigDecimal[] meanSquareBetweenBDG =null;                                // mean square between groups of pair comparisons as BigDecimal
    private double[] fRatioG = null;                                                // one way ANOVA F-ratio of pair comparisons as double
    private BigDecimal[] fRatioBDG = null;                                          // one way ANOVA F-ratio of pair comparisons as BigDecimal
    private double[] fRatioPG = null;                                               // one way ANOVA F-ratio probability of pair comparisons
    private double[] criticalFratioG = null;                                        // critical F-ratio of pair comparisons
    private double[] ssTotalG = null;                                               // total sum of squares of pair comparisons                  
    private double[] ssWithinG = null;                                              // within groups sum of squares of pair comparisons   
    private double[] ssBetweenG = null;                                             // between groups sum of squares of pair comparisons   
    private BigDecimal[] ssTotalBDG = null;                                         // total sum of squares of pair comparisons as BigDecimal                 
    private BigDecimal[] ssWithinBDG = null;                                        // within groups sum of squares of pair comparisons as BigDecimal  
    private BigDecimal[] ssBetweenBDG = null;                                       // between groups sum of squares of pair comparisons as BigDecimal     
    private int[] dofTotalG = null;                                                 // total degrees of freedom of pair comparisons
    private int[] dofBetweenG = null;                                               // between groups degrees of freedom of pair comparisons
    private int[] dofWithinG = null;                                                // within groups degrees of freedom of pair comparisons
    
    private boolean allCalcn = true;                                                // = false if only pair comparisons required
    
    // CONSTRUCTOR
    public ANOVA(){
        this.setTitle("Untitled data", false);
    }
    
    // Constuctor setting data title
    public ANOVA(String dataTitle){
        this.setTitle(dataTitle, true);
    }
    
    // TITLE
    // Enter title (optional)
    public void enterTitle(String title){
        this.setTitle(title, true);
    }
    
    // Set title 
    private void setTitle(String title, boolean check){
        if(!check){
            this.title[0] = title;
            this.nTitle++;
            this.titleCheck = check;
        }
        else{
            if(!this.titleCheck){
                this.title[0] = title;
                this.nTitle++;
                this.titleCheck = check;
            }
            else{
                this.titleCheck = check;
                this.title[this.nTitle] = title;
                this.nTitle++;
            }
        }
    }

    
    // CRITICAL PROBABILITY
    // entered as a fraction
    public void setCriticalSignificance(double critProb){
        this.criticalSignificance = 1.0 - critProb;
        this.criticalSignificanceF = Double.toString(100.0*critProb) + "%";
        this.criticalSignificanceS = Double.toString(100.0*(1.0-critProb)) + "%";
    }
    
    // Return the criticalSignificance
    public double getCriticalSignificance(){
        return 1.0 - this.criticalSignificance;
    }

    // DICHOTOMOUS PAIRS    
    // Add dichotomous word pairs to default list
    public void addDichotomousPair(String trueSign, String falseSign){
        String[] holdS = (String[])this.dichotomousS.clone();
        double[] holdD = (double[])this.dichotomousDoubleS.clone();
        this.dichotomousS = new String[this.nDichotomousS+2];
        this.dichotomousDoubleS = new double[this.nDichotomousS+2];
        for(int i=0; i<this.nDichotomousS; i++){
           this.dichotomousS[i] = holdS[i];
           this.dichotomousDoubleS[i] = holdD[i];
        }
        this.dichotomousS[this.nDichotomousS] = trueSign;
        this.dichotomousDoubleS[this.nDichotomousS] = this.dichotTrue;
        this.nDichotomousS++;
        this.dichotomousS[this.nDichotomousS] = falseSign;
        this.dichotomousDoubleS[this.nDichotomousS] = this.dichotFalse;
        this.nDichotomousS++;  
        
        if(trueSign.length()==1 && falseSign.length()==1){
            char[] holdC = (char[])this.dichotomousC.clone();
            holdD = (double[])this.dichotomousDoubleC.clone();
            this.dichotomousC = new char[this.nDichotomousC+2];
            this.dichotomousDoubleC = new double[this.nDichotomousC+2];
            for(int i=0; i<this.nDichotomousC; i++){
                this.dichotomousC[i] = holdC[i];
                this.dichotomousDoubleC[i] = holdD[i];
            }
            this.dichotomousC[this.nDichotomousC] = trueSign.charAt(0);
            this.dichotomousDoubleC[this.nDichotomousC] = this.dichotTrue;
            this.nDichotomousC++;
            this.dichotomousC[this.nDichotomousC] = falseSign.charAt(0);
            this.dichotomousDoubleC[this.nDichotomousC] = this.dichotFalse;
            this.nDichotomousC++;
        }
    }
    
    // Add dichotomous char pairs to default list
    public void addDichotomousPair(char trueSign, char falseSign){
        char[] holdC = (char[])this.dichotomousC.clone();
        double[] holdD = (double[])this.dichotomousDoubleC.clone();
        this.dichotomousC = new char[this.nDichotomousC+2];
        this.dichotomousDoubleC = new double[this.nDichotomousC+2];
        for(int i=0; i<this.nDichotomousC; i++){
           this.dichotomousC[i] = holdC[i];
           this.dichotomousDoubleC[i] = holdD[i];
        }
        this.dichotomousC[this.nDichotomousC] = trueSign;
        this.dichotomousDoubleC[this.nDichotomousC] = this.dichotTrue;
        this.nDichotomousC++;
        this.dichotomousC[this.nDichotomousC] = falseSign;
        this.dichotomousDoubleC[this.nDichotomousC] = this.dichotFalse;
        this.nDichotomousC++; 
        
        String[] holdS = (String[])this.dichotomousS.clone();
        holdD = (double[])this.dichotomousDoubleS.clone();
        this.dichotomousS = new String[this.nDichotomousS+2];
        this.dichotomousDoubleS = new double[this.nDichotomousS+2];
        for(int i=0; i<this.nDichotomousS; i++){
           this.dichotomousS[i] = holdS[i];
           this.dichotomousDoubleS[i] = holdD[i];
        }
        this.dichotomousS[this.nDichotomousS] = String.valueOf(trueSign);
        this.dichotomousDoubleS[this.nDichotomousS] = this.dichotTrue;
        this.nDichotomousS++;
        this.dichotomousS[this.nDichotomousS] = String.valueOf(falseSign);
        this.dichotomousDoubleS[this.nDichotomousS] = this.dichotFalse;
        this.nDichotomousS++;
    }
    
    // Set the numerical values of all dichotomous pairs
    public void setDichotomousPairValues(double first, double second){
        this.dichotTrue = first;
        this.dichotFalse = second;
    }
    
    // Get the numerical values of all dichotomous pairs
    public double[] getDichotomousPairValues(){
        double[] ret = {this.dichotTrue, this.dichotFalse};
        return ret;
    }
    
    // SET ALL OR PAIR CALCULATION OPTION
    private void setAllCalcn(boolean check){
        this.allCalcn = check;
    }
    
    // ENTER RESPONSES
    // 1.   ENTER AS ROWS OF INDIVIDUAL RESPONSES FOR EACH GROUP
    // Read responses from a text file as a matrix with rows of responses for each group
    // File selected using a dialog window
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    // responses may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter
    public void readResponseData(){
        //Select file 
        FileChooser fin = new FileChooser();

        // Read in file name
        String filename = fin.selectFile();
        fin.close();
        
        this.readResponseData(filename);
        
    }
        
    // Read responses from a text file as a matrix with rows of responses for each group
    // File name entered as argument
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    // responses may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter
    public void readResponseData(String filename){
               
       //Select file
        this.inputFilename = filename;
        FileInput fin = new FileInput(filename);

        // Read in data title
        String hold0 = fin.readLine();
        this.setTitle(hold0, true);
        
        // Read in number of groups
        this.nGroups = fin.readInt();

        // Read in group names
        this.groupNames = new String[this.nGroups+1];
        for(int i=0; i<this.nGroups; i++){
            this.groupNames[i] = fin.readWord();
        }
        this.groupNames[this.nGroups] = "total";
        this.groupNamesSet = true;

        // Read in number of responses in each group
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = fin.readInt();
        }
        
        // Read in data to a String 2D array
        String[][] responses = new String[this.nGroups][];
        String[] hold = null;
        for(int i=0; i<this.nGroups;i++){
            hold = new String[this.nResponsesPerGroup[i]];
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                hold[j] = fin.readWord();
            }
            responses[i] = Conv.copy(hold);
        }
        fin.close();

        // Set variables
        this.originalDataType = 1;
        this.setVariables((Object)responses, this.originalDataType);
        
    }
    
    
    // Enter responses as a matrix with rows of responses for each group - matrix of responses entered as String[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(String[][] responses){

        // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 1;
        this.setVariables((Object)responses, this.originalDataType);
    }

    // Enter responses as a matrix with rows of responses for each group - matrix of responses entered as double[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(double[][] responses){

        // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
    
        // Set variables
        this.originalDataType = 2;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
    // Enter responses as a matrix with rows of responses for each group - matrix of responses entered as Matrix
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(Matrix responses){
        double[][] responsesdd = responses.getArrayCopy();
        
        // Determine number of groups and responses
        this.nGroups = responsesdd.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responsesdd[i].length;
        }

        // Set variables
        this.originalDataType = 3;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
    // Enter responses as a matrix with rows of responses for each group   -  matrix of responses entered as BigDecimal[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(BigDecimal[][] responses){

       // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 9;
        this.setVariables((Object)responses, this.originalDataType);
    }

    // Enter responses as a matrix with rows of responses for each group - matrix of responses entered as float[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(float[][] responses){

        // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 4;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
     // Enter responses as a matrix with rows of responses for each group   -  matrix of responses entered as long[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(long[][] responses){

       // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 8;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
    // Enter responses as a matrix with rows of responses for each group   -  matrix of responses entered as int[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(int[][] responses){

       // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 5;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
    // Enter responses as a matrix with rows of responses for each group   -  matrix of responses entered as char[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    // responses may be represented by a numeral (0 to 9), y, Y, n, N or a letter
    public void enterResponseData(char[][] responses){

       // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 6;
        this.setVariables((Object)responses, this.originalDataType);
    }


    // Enter responses as a matrix with rows of responses for each group - responses either true  or false  -  matrix of responses entered as boolean[][]
    // e.g. responses0[0][0] to responses0[0][nPersons-1] =  responses for each person in turn for the first group
    //      responses0[1][0] to responses0[1][nPersons-1] =  responses for each person in turn for the second group
    // etc.
    public void enterResponseData(boolean[][] responses){

       // Determine number of groups and responses
        this.nGroups = responses.length;
        this.nResponsesPerGroup = new int[this.nGroups];
        for(int i=0; i<this.nGroups; i++){
            this.nResponsesPerGroup[i] = responses[i].length;
        }
        
        // Set variables
        this.originalDataType = 7;
        this.setVariables((Object)responses, this.originalDataType);
    }
    
    // DATA PREPROCESSING
    // Set variables
    private void setVariables(Object responses, int type){
        
        // Set group names
        if(!this.groupNamesSet){
            this.groupNames = new String[this.nGroups];
            for(int i=0; i<this.nGroups; i++){
                this.groupNames[i] = "group " + (i+1);
            }
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(responses);
        this.originalDataType = type;
        this.dataEntered = true;
        
        // Convert data type to double and/or BigDecimal
        this.convertDataType(type);
    }
    
    // Convert data type to double
    private void convertDataType(int type){
       
        this.bigDecimal = false;
        
        // Convert to doubles  or BigDecimal         
        switch(type){    
            case 1: // String[][]
                    this.responses0 = new double[this.nGroups][];
                    String[][] responses = (String[][])this.originalData;
                    responses = this.trimResponses(responses);
                    for(int i=0; i<this.nGroups; i++){
                        // test for dichotomous data
                        boolean testDichot = this.testIfDichotomous(responses[i]);
                        if(testDichot){
                            this.responses0[i]  = this.dichotStringToDouble(responses[i]);
                        }
                        else{
                            // test for alphabetic data
                            boolean testAlphaBetic = this.testIfAlphabetic(responses[i]);
                            if(testAlphaBetic){
                                this.responses0[i] =  this.alphabeticToDouble(responses[i]);
                            }
                            else{
                                this.responses0[i] =  this.stringToDouble(responses[i]);
                            }
                        }                 
                    }
                    this.doubleToBD();
                    break;
            case 2: // double[][]
                    this.responses0 = new double[this.nGroups][];
                    double[][] responsesD = (double[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = (double[])responsesD[i].clone();
                    } 
                    this.doubleToBD();
                    break;
            case 3: // Matrix
                    this.responses0 = new double[this.nGroups][];
                    double[][] responsesDD = ((Matrix)this.originalData).getArrayCopy();
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = (double[])responsesDD[i].clone();
                    } 
                    this.doubleToBD();
                    break;
            case 4: // float[][]
                    this.responses0 = new double[this.nGroups][];
                    float[][] responsesF = (float[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = new double[this.nResponsesPerGroup[i]];
                        for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                            this.responses0[i][j] = (double)responsesF[i][j];
                        }
                    } 
                    this.doubleToBD();
                    break;
            case 5: // int[][]
                    this.responses0 = new double[this.nGroups][];
                    int[][] responsesI = (int[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = new double[this.nResponsesPerGroup[i]];
                        for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                            this.responses0[i][j] = (double)responsesI[i][j];
                        }
                    } 
                    this.doubleToBD();
                    break;
            case 6: // char[][]
                    this.responses0 = new double[this.nGroups][];
                    char[][] responsesC = (char[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        // test for dichotomous data
                        boolean testDichotC = this.testIfDichotomous(responsesC[i]);
                        if(testDichotC){
                            this.responses0[i]  = this.dichotCharToDouble(responsesC[i]);
                        }
                        else{
                            // test for alphabetic data
                            boolean testAlphaBetic = this.testIfAlphabetic(responsesC[i]);
                            if(testAlphaBetic){
                                this.responses0[i] =  this.alphabeticToDouble(responsesC[i]);
                            }
                            else{
                                this.responses0[i] =  this.charToDouble(responsesC[i]);
                            }
                        }                 
                    }
                    this.doubleToBD();
                    break;
            case 7: // boolean[][]
                    this.responses0 = new double[this.nGroups][];
                    boolean[][] responsesB = (boolean[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = new double[this.nResponsesPerGroup[i]];
                        for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                            if(responsesB[i][j]){
                                this.responses0[i][j] = 1.0;
                            }
                            else{
                                this.responses0[i][j] = -1.0;
                            }
                        }
                    } 
                    this.doubleToBD();
                    break;
            case 8: // long[][]
                    this.responses0 = new double[this.nGroups][];
                    long[][] responsesL = (long[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responses0[i] = new double[this.nResponsesPerGroup[i]];
                        for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                            this.responses0[i][j] = (double)responsesL[i][j];
                        }
                    } 
                    this.doubleToBD();
                    break;
            case 9: // BigDecimal[][]
                    this.responsesBD = new BigDecimal[this.nGroups][];
                    BigDecimal[][] responsesBDH = (BigDecimal[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responsesBD[i] = (BigDecimal[])responsesBDH[i].clone();      
                    } 
                    this.bDtodouble();
                    this.bigDecimal = true;
                    break;
           case 10: // BigInteger[][]
                    this.responsesBD = new BigDecimal[this.nGroups][];
                    BigInteger[][] responsesBI = (BigInteger[][])this.originalData;
                    for(int i=0; i<this.nGroups; i++){
                        this.responsesBD[i] = new BigDecimal[this.nResponsesPerGroup[i]];
                        for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                            this.responsesBD[i][j] = new BigDecimal(responsesBI[i][j]);
                        }                             
                    } 
                    this.bDtodouble();
                    this.bigDecimal = true;
                    break;
        }
        
        // total responses                                     
        this.nTotalResponses = 0;
        for(int i=0; i<this.nGroups; i++){
            this.nTotalResponses += this.nResponsesPerGroup[i];
        }
        
    }
    
    // Convert responses as double to responses as BigDecimal
    private void doubleToBD(){
    
        this.responsesBD = new BigDecimal[this.nGroups][];
        for(int i=0; i<this.nGroups; i++){
            this.responsesBD[i] = new BigDecimal[this.nResponsesPerGroup[i]];
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                this.responsesBD[i][j] =  new BigDecimal(responses0[i][j]);
           }
        }
    }
    
    // Convert responses as BigDecimal to responses as double
    private void bDtodouble(){
        this.responses0 = new double[this.nGroups][];
        for(int i=0; i<this.nGroups; i++){
            this.responses0[i] = new double[this.nResponsesPerGroup[i]];
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                this.responses0[i][j] =  this.responsesBD[i][j].doubleValue();
           }
        }
    }  
                       
    // Test for dichotomous String data
    private boolean testIfDichotomous(String[] responses){
        boolean test0 = false;
        int n = responses.length;
        int nD = 0;
        for(int i=0; i<n; i++){
            boolean test1= false;
            for(int j=0; j<this.nDichotomousS; j++){
                if(responses[i].equalsIgnoreCase(this.dichotomousS[j]))test1 = true;
            }
            if(test1)nD++;
        }
        if(nD==n)test0 = true;
        return test0;
    }
    
    // Test for dichotomous char data
    private boolean testIfDichotomous(char[] responses){
        boolean test0 = false;
        int n = responses.length;
        int nD = 0;
        for(int i=0; i<n; i++){
            boolean test1= false;
            for(int j=0; j<this.nDichotomousC; j++){
                if(responses[i]==this.dichotomousC[j])test1 = true;
            }
            if(test1)nD++;
        }
        if(nD==n)test0 = true;
        return test0;
    }
    
    // Test for alphabetic String data
    private boolean testIfAlphabetic(String[] responses){
        boolean test0 = false;
        int n = responses.length;
        int nA = 0;
        
        for(int i=0; i<n; i++){
            boolean test1 = false;
            if(responses[i].length()==1){
                int iC = (int)responses[i].charAt(0);
                if(iC>64 && iC<91){
                    test1 = true;
                }
                else{
                    if(iC>96 && iC<123){
                        test1 = true;
                    }
                }
            }
            if(test1)nA++;
        }
        if(nA==n)test0 = true;
        return test0;
    }
    
    // Test for alphabetic char data
    private boolean testIfAlphabetic(char[] responses){
        boolean test0 = false;
        int n = responses.length;
        int nA = 0;
        
        for(int i=0; i<n; i++){
            boolean test1 = false;
            int iC = (int)responses[i];
            if(iC>64 && iC<91){
                test1 = true;
            }
            else{
                if(iC>96 && iC<123){
                    test1 = true;
                }
            }
            if(test1)nA++;
        }
        if(nA==n)test0 = true;
        return test0;
    }
    
    // Convert dichotomous Strings to double
    private double[] dichotStringToDouble(String[] responses){
        boolean test0 = false;
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            boolean test1= false;
            for(int j=0; j<this.nDichotomousS; j=j+2){
                if(responses[i].equalsIgnoreCase(this.dichotomousS[j]))test1 = true;
            }
            if(test1){
                converted[i] = 1.0;
            }
            else{
                converted[i] = -1.0;
            }
        }     
        return converted;
    }
    
    // Convert dichotomous char to double
    private double[] dichotCharToDouble(char[] responses){
        boolean test0 = false;
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            boolean test1= false;
            for(int j=0; j<this.nDichotomousC; j=j+2){
                if(responses[i]==this.dichotomousC[j])test1 = true;
            }
            if(test1){
                converted[i] = 1.0;
            }
            else{
                converted[i] = -1.0;
            }
        }     
        return converted;
    }
    
    // Convert single letter alphabetic String to double
    private double[] alphabeticToDouble(String[] responses){
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            double holdi = (double)((int)responses[i].charAt(0));
            if(holdi>96.0){
                converted[i] = holdi-96.0;
            }
            else{
                converted[i] = holdi-64.0;
            }
        }     
        return converted;
    }

    // Convert single letter alphabetic String to double
    private double[] alphabeticToDouble(char[] responses){
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            double holdi = (double)((int)responses[i]);
            if(holdi>96.0){
                converted[i] = holdi-96.0;
            }
            else{
                converted[i] = holdi-64.0;
            }
        }     
        return converted;
    }
    
    // Convert 'numeric' String to double
    private double[] stringToDouble(String[] responses){
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            converted[i] = Double.parseDouble(responses[i]);
        }     
        return converted;
    }
    
        // Convert 'numeric' char to double
    private double[] charToDouble(char[] responses){
        int n = responses.length;
        double[] converted = new double[n];
        for(int i=0; i<n; i++){
            double holdi = (double)((int)responses[i]);
            if(holdi>96.0){
                converted[i] = holdi-96.0;
            }
            else{
                converted[i] = holdi-64.0;
            }
        }     
        return converted;
    }

    // TRIM RESPONSES
    // Trim all elements of leading and trailing spaces
    private String[][] trimResponses(String[][] responses){
        String[][] responsesT = (String[][])responses.clone();
        int n = responses.length;
        int m = responses[0].length;
        for(int i=0; i<n; i++){
            for(int j=0; j<m; j++){
                responsesT[i][j].trim();
            }
        }
        return responsesT;
    }
    
    
    // RETURN RESPONSES
    // Return responses as entered
    public Object getResponsesAsEntered(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return Conv.copy(this.originalData);
    }

    // Return converted data
    // returned as double
    public double[][] getResponses(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return Conv.copy(this.responses0);
    }
    
    public double[][] getResponsesAsdouble(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return Conv.copy(this.responses0);
    }
    
    // returned as double
    public BigDecimal[][] getResponsesAsBigDecimal(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return Conv.copy(responsesBD);
    }
     
    // Return data file title
    public String[] getTitle(){
        String[] ret = new String[this.nTitle];
        for(int i=0; i<this.nTitle; i++){
            ret[i] = this.title[i];
        }
        return ret;
    }

    // Return input file name
    public String getInputFileName(){
        return this.inputFilename;
    }
        
    // GROUPS
    // Enter group names, i.e. one word group titles
    // default values are group1, group2, group3 etc.
    public void enterGroupNames(String[] groupNames){
        int len = groupNames.length;
        this.groupNames = new String[len+1];
        for(int i=0; i<len; i++)this.groupNames[i] = groupNames[i];
        this.groupNames[len]="total";
        this.groupNamesSet = true;
    }
    
    // Return number of groups
    public int getNumberOfGroups(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return this.nGroups;
    }

    //  Return group names
    public String[] getGroupNames(){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        String[] ret = new String[this.nGroups];
        for(int i=0; i<this.nGroups; i++)ret[i] = this.groupNames[i];
        return ret;
    }

    //  Return group name for a given index
    public String getGroupName(int index){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        return this.groupNames[index-1];
    }
    
    // GROUP STATISTICS
    // Group statistics in double precision
    private void groupStatistics(){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        
        this.groupMeansBD = new BigDecimal[this.nGroups];               // group means as BigDecimal
        this.groupMeans = new double[this.nGroups];                     // group means as double
        this.groupSD = new double[this.nGroups];                        // group standard deviations
        this.groupSE = new double[this.nGroups];                        // group standard errors
        this.groupClb = new double[this.nGroups];                       // lower bound of the group mean critical probability confidence limit
        this.groupCub = new double[this.nGroups];                       // upper bound of the group mean critical probability confidence limit
        this.groupMinimumBD = new BigDecimal[this.nGroups];             // group minimum nas BigDecimal
        this.groupMinimum = new double[this.nGroups];                   // group minimum nas double
        this.groupMaximumBD = new BigDecimal[this.nGroups];             // group maximum as BigDecimal
        this.groupMaximum = new double[this.nGroups];                   // group maximum as double
        this.groupSSBD = new BigDecimal[this.nGroups];                  // group sum of squares as BigDecimal
        this.groupSS = new double[this.nGroups];                        // group sum of squares as double
        
        this.groupMedians = new double[this.nGroups];                   // group medians as double
        this.groupMediansBD = new BigDecimal[this.nGroups];             // group medians as BigDecimal
        this.groupMomentSkewness = new double[this.nGroups];            // group moment skewnesses as double 
        this.groupMedianSkewness = new double[this.nGroups];            // group median skewnesses as double 
        this.groupQuartileSkewness = new double[this.nGroups];          // group quartile skewnesses as double 
        this.groupQuartileSkewnessBD = new BigDecimal[this.nGroups];    // group quartile skewnesses as BigDecimal 
        this.groupKurtosis = new double[this.nGroups];                  // group kurtoses as double 
        this.groupKurtosisBD = new BigDecimal[this.nGroups];            // group kurtoses as BigDecimal  
        this.groupExcessKurtosis = new double[this.nGroups];            // group excess kurtoses as double 
        this.groupExcessKurtosisBD = new BigDecimal[this.nGroups];      // group excess kurtoses as BigDecimal
        
        this.groupProbPlotR = new double[this.nGroups];                 // group Gaussian probability plot correlation coefficients
        this.groupProbPlotGradient = new double[this.nGroups];          // group Gaussian probability plot gradient
        this.groupProbPlotIntercept = new double[this.nGroups];         // group Gaussian probability plot intercept
        this.groupProbPlotMu = new double[this.nGroups];                // group Gaussian probability plot mu
        this.groupProbPlotSigma = new double[this.nGroups];             // group Gaussian probability plot sigma
                    
        Stat st = null;
        ProbabilityPlot pp = null;
        for(int i=0; i<this.nGroups; i++){
            st = new Stat(this.responses0[i]);
            this.groupMeans[i] = st.mean();
            this.groupMeansBD[i] = new BigDecimal(this.groupMeans[i]); 
            this.groupSD[i] = st.standardDeviation();
            this.groupSE[i] = st.standardError();
            double[] limits = st.meanConfidenceLimits(this.criticalSignificance);
            this.groupClb[i] = limits[0];                     
            this.groupCub[i] = limits[1];                     
            this.groupMinimum[i] = st.minimum();
            this.groupMinimumBD[i] = new BigDecimal(this.groupMinimum[i]);
            this.groupMaximum[i] = st.maximum();
            this.groupMaximumBD[i] = new BigDecimal(this.groupMaximum[i]);
            double ss = 0.0;
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                double hold = this.responses0[i][j] - this.groupMeans[i];
                ss += hold*hold;
            }
            this.groupSS[i] = ss;
            this.groupSSBD[i] = new BigDecimal(ss);
            this.groupMedians[i] = st.median_as_double();
            this.groupMediansBD[i] = new BigDecimal(this.groupMedians[i]);            
            this.groupMomentSkewness[i] = st.momentSkewness_as_double(); 
            this.groupMedianSkewness[i] = st.medianSkewness_as_double(); 
            this.groupQuartileSkewness[i] = st.quartileSkewness_as_double();  
            this.groupQuartileSkewnessBD[i] = new BigDecimal(this.groupQuartileSkewness[i]);
            this.groupKurtosis[i] = st.kurtosis_as_double(); 
            this.groupKurtosisBD[i] = new BigDecimal(this.groupKurtosis[i]);  
            this.groupExcessKurtosis[i] = st.excessKurtosis_as_double();  
            this.groupExcessKurtosisBD[i] = new BigDecimal(this.groupExcessKurtosis[i]);
            pp = new ProbabilityPlot(this.responses0[i]);
            pp.supressDisplay();
            pp.gaussianProbabilityPlot();
            this.groupProbPlotR[i] = pp.gaussianCorrelationCoefficient();
            this.groupProbPlotGradient[i] = pp.gaussianGradient();
            this.groupProbPlotIntercept[i] = pp.gaussianIntercept();
            this.groupProbPlotMu[i] = pp.gaussianMu();
            this.groupProbPlotSigma[i] = pp.gaussianSigma();
        }
        
        double [] allAsOne = new double[this.nTotalResponses];
        int k = 0;
        for(int i=0; i<this.nGroups; i++){
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                allAsOne[k++] = this.responses0[i][j];
            }
        }
        Stat st1 = new Stat(allAsOne);
        this.totalMean = st1.mean();
        this.totalMeanBD = new BigDecimal(this.totalMean); 
        this.totalSD = st1.standardDeviation();
        this.totalSE = st1.standardError();
        double[] limits = st1.meanConfidenceLimits(this.criticalSignificance);
        this.totalClb = limits[0];                     
        this.totalCub = limits[1];                     
        this.totalMinimum = st1.minimum();
        this.totalMinimumBD = new BigDecimal(this.totalMinimum);
        this.totalMaximum = st1.maximum();
        this.totalMaximumBD = new BigDecimal(this.totalMaximum);
        double ss = 0.0;
        for(int j=0; j<this.nTotalResponses; j++){
        double hold = allAsOne[j] - this.totalMean;
            ss += hold*hold;
        }
        this.totalSS = ss;
        this.totalSSBD = new BigDecimal(ss);
        this.totalMedian = st1.median_as_double();
        this.totalMedianBD = new BigDecimal(this.totalMedian);            
        this.totalMomentSkewness = st1.momentSkewness_as_double(); 
        this.totalMedianSkewness = st1.medianSkewness_as_double(); 
        this.totalQuartileSkewness = st1.quartileSkewness_as_double();  
        this.totalQuartileSkewnessBD = new BigDecimal(this.totalQuartileSkewness);
        this.totalKurtosis = st1.kurtosis_as_double(); 
        this.totalKurtosisBD = new BigDecimal(this.totalKurtosis);  
        this.totalExcessKurtosis = st1.excessKurtosis_as_double();  
        this.totalExcessKurtosisBD = new BigDecimal(this.totalExcessKurtosis);
        ProbabilityPlot pp1 = new ProbabilityPlot(allAsOne);
        pp1.supressDisplay();
        pp1.gaussianProbabilityPlot();
        this.totalProbPlotR = pp1.gaussianCorrelationCoefficient();
        this.totalProbPlotGradient = pp1.gaussianGradient();
        this.totalProbPlotIntercept = pp1.gaussianIntercept();
        this.totalProbPlotMu = pp1.gaussianMu();
        this.totalProbPlotSigma = pp1.gaussianSigma();
            
        this.groupStatsDone = true;
    }
    
    // Group statistics in arbitrary precision
    private void groupStatisticsBD(){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        
        this.groupMeansBD = new BigDecimal[this.nGroups];               // group means as BigDecimal
        this.groupMeans = new double[this.nGroups];                     // group means as double
        this.groupSD = new double[this.nGroups];                        // group standard deviations
        this.groupSE = new double[this.nGroups];                        // group standard errors
        this.groupClb = new double[this.nGroups];                       // lower bound of the group mean critical probability confidence limit
        this.groupCub = new double[this.nGroups];                       // upper bound of the group mean critical probability confidence limit
        this.groupMinimumBD = new BigDecimal[this.nGroups];             // group minimum nas BigDecimal
        this.groupMinimum = new double[this.nGroups];                   // group minimum nas double
        this.groupMaximumBD = new BigDecimal[this.nGroups];             // group maximum as BigDecimal
        this.groupMaximum = new double[this.nGroups];                   // group maximum as double
        this.groupSSBD = new BigDecimal[this.nGroups];                  // group sum of squares as BigDecimal
        this.groupSS = new double[this.nGroups];                        // group sum of squares as double
         
        this.groupMedians = new double[this.nGroups];                   // group medians as double
        this.groupMediansBD = new BigDecimal[this.nGroups];             // group medians as BigDecimal
        this.groupMomentSkewness = new double[this.nGroups];            // group moment skewnesses as double 
        this.groupMedianSkewness = new double[this.nGroups];            // group median skewnesses as double 
        this.groupQuartileSkewness = new double[this.nGroups];          // group quartile skewnesses as double 
        this.groupQuartileSkewnessBD = new BigDecimal[this.nGroups];    // group quartile skewnesses as BigDecimal 
        this.groupKurtosis = new double[this.nGroups];                  // group kurtoses as double 
        this.groupKurtosisBD = new BigDecimal[this.nGroups];            // group kurtoses as BigDecimal  
        this.groupExcessKurtosis = new double[this.nGroups];            // group excess kurtoses as double 
        this.groupExcessKurtosisBD = new BigDecimal[this.nGroups];      // group excess kurtoses as BigDecimal
        
        this.groupProbPlotR = new double[this.nGroups];                 // group Gaussian probability plot correlation coefficients
        this.groupProbPlotGradient = new double[this.nGroups];          // group Gaussian probability plot gradient
        this.groupProbPlotIntercept = new double[this.nGroups];         // group Gaussian probability plot intercept
        this.groupProbPlotMu = new double[this.nGroups];                // group Gaussian probability plot mu
        this.groupProbPlotSigma = new double[this.nGroups];             // group Gaussian probability plot sigma
        
        Stat st = null;
        ProbabilityPlot pp = null;
        for(int i=0; i<this.nGroups; i++){
            st = new Stat(this.responsesBD[i]);
            this.groupMeansBD[i] = st.mean_as_BigDecimal();
            this.groupMeans[i] = this.groupMeansBD[i].doubleValue();
            this.groupSD[i] = st.standardDeviation();
            this.groupSE[i] = st.standardError();
            double[] limits = st.meanConfidenceLimits(this.criticalSignificance);
            this.groupClb[i] = limits[0];                     
            this.groupCub[i] = limits[1];                     
            this.groupMinimumBD[i] = st.minimum_as_BigDecimal(); 
            this.groupMinimum[i] = st.minimum_as_double(); 
            this.groupMaximumBD[i] = st.maximum_as_BigDecimal();
            this.groupMaximum[i] = st.maximum_as_double();
            BigDecimal ss = new BigDecimal("0.0");
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                BigDecimal hold = this.responsesBD[i][j].subtract(this.groupMeansBD[i]);
                ss = ss.add(hold.multiply(hold));
            }
            this.groupSSBD[i] = ss;
            this.groupSS[i] = ss.doubleValue();
            this.groupMediansBD[i] = st.median_as_BigDecimal();
            this.groupMedians[i] = this.groupMediansBD[i].doubleValue();            
            this.groupMomentSkewness[i] = st.momentSkewness_as_double(); 
            this.groupMedianSkewness[i] = st.medianSkewness_as_double(); 
            this.groupQuartileSkewnessBD[i] = st.quartileSkewness_as_BigDecimal();  
            this.groupQuartileSkewness[i] = this.groupQuartileSkewnessBD[i].doubleValue();
            this.groupKurtosis[i] = st.kurtosis_as_double(); 
            this.groupKurtosisBD[i] = new BigDecimal(this.groupKurtosis[i]);  
            this.groupExcessKurtosisBD[i] = st.excessKurtosis_as_BigDecimal();  
            this.groupExcessKurtosis[i] = this.groupExcessKurtosisBD[i].doubleValue();
            pp = new ProbabilityPlot(this.responses0[i]);
            pp.supressDisplay();
            pp.gaussianProbabilityPlot();
            this.groupProbPlotR[i] = pp.gaussianCorrelationCoefficient();
            this.groupProbPlotGradient[i] = pp.gaussianGradient();
            this.groupProbPlotIntercept[i] = pp.gaussianIntercept();
            this.groupProbPlotMu[i] = pp.gaussianMu();
            this.groupProbPlotSigma[i] = pp.gaussianSigma();
        }
        
        BigDecimal[] allAsOne = new BigDecimal[this.nTotalResponses];
        int k = 0;
        for(int i=0; i<this.nGroups; i++){
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                allAsOne[k++] = this.responsesBD[i][j];
            }
        }
        Stat st1 = new Stat(allAsOne);
        this.totalMeanBD = st1.mean_as_BigDecimal();
        this.totalMean = this.totalMeanBD.doubleValue(); 
        this.totalSD = st1.standardDeviation();
        this.totalSE = st1.standardError();
        double[] limits = st1.meanConfidenceLimits(this.criticalSignificance);
        this.totalClb = limits[0];                     
        this.totalCub = limits[1];                     
        this.totalMinimumBD = st1.minimum_as_BigDecimal();
        this.totalMinimum = this.totalMinimumBD.doubleValue();
        this.totalMaximumBD = st1.maximum_as_BigDecimal();
        this.totalMaximum = this.totalMaximumBD.doubleValue();
        BigDecimal ss = new BigDecimal("0.0");
        for(int j=0; j<this.nTotalResponses; j++){
        BigDecimal hold = allAsOne[j].subtract(this.totalMeanBD);
            ss = ss.add(hold.multiply(hold));
        }
        this.totalSSBD = ss;
        this.totalSS = this.totalSSBD.doubleValue();
        this.totalMedianBD = st1.median_as_BigDecimal();
        this.totalMedian = this.totalMedianBD.doubleValue();            
        this.totalMomentSkewness = st1.momentSkewness_as_double(); 
        this.totalMedianSkewness = st1.medianSkewness_as_double(); 
        this.totalQuartileSkewnessBD = st1.quartileSkewness_as_BigDecimal();  
        this.totalQuartileSkewness = this.totalQuartileSkewnessBD.doubleValue();
        this.totalKurtosis = st1.kurtosis_as_double(); 
        this.totalKurtosisBD = new BigDecimal(this.totalKurtosis);  
        this.totalExcessKurtosisBD = st1.excessKurtosis_as_BigDecimal();  
        this.totalExcessKurtosis = this.totalExcessKurtosisBD.doubleValue();
        ProbabilityPlot pp1 = new ProbabilityPlot(allAsOne);
        pp1.supressDisplay();
        pp1.gaussianProbabilityPlot();
        this.totalProbPlotR = pp1.gaussianCorrelationCoefficient();
        this.totalProbPlotGradient = pp1.gaussianGradient();
        this.totalProbPlotIntercept = pp1.gaussianIntercept();
        this.totalProbPlotMu = pp1.gaussianMu();
        this.totalProbPlotSigma = pp1.gaussianSigma();
        this.groupStatsDone = true;
    }

    // Return number of responses per group
    public int[] numberOfResponsesPerGroup(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return this.nResponsesPerGroup;
    }
    
    // Return group means
    // return as double
    public double[] groupMeans(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMeans);
    }
    
    // Return group means as BigDecimal
    public BigDecimal[] groupMeans_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupMeansBD);
    }
    
    // Return group standard deviations
    public double[] groupStandardDeviations(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupSD);
    }
    
    // Return group standard derrors
    public double[] groupStandardErrors(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupSE);
    }
    
    // Return group mean lower 95% confidence limit
    public double[] groupMeanLowerConfidenceLimits(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupClb);
    }
    
    // Return group mean upper 95% confidence limit
    public double[] groupMeanUpperConfidenceLimits(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupCub);
    }
    
    // Return group maxima as double
    public double[] groupMaxima(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMaximum);
    }
    
    // Return group maxima as BigDecimal
    public BigDecimal[] groupMaxima_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupMaximumBD);
    }
    
    // Return group minima as double
    public double[] groupMinima(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMinimum);
    }
    
    // Return group minima as BigDecimal
    public BigDecimal[] groupMinima_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupMinimumBD);
    }
    
    // Return group medians as double
    public double[] groupMedians(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMedians);
    }
    
    // Return group medians as BigDecimal
    public BigDecimal[] groupMedians_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupMediansBD);
    }
    
    // Return group moment skewnesses as double
    public double[] groupMomentSkewnesses(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMomentSkewness);
    }
    
   // Return group median skewnesses as double
    public double[] groupMedianSkewnesses(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupMedianSkewness);
    }
    
    // Return group quartile skewnesses as double
    public double[] groupQuartileSkewnesses(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupQuartileSkewness);
    }
    
    // Return group quartile skewnesses as BigDecimal
    public BigDecimal[] groupQuartileSkewnesses_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupQuartileSkewnessBD);
    }
    
    // Return group kurtoses as double
    public double[] groupKurtoses(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupKurtosis);
    }
    
    // Return group kurtoses as BigDecimal
    public BigDecimal[] groupKurtoses_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupKurtosisBD);
    }
    
    // Return group excess kurtoses as double
    public double[] groupExcessKurtoses(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupExcessKurtosis);
    }
    
    // Return group excess kurtoses as BigDecimal
    public BigDecimal[] groupExcessKurtoses_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupExcessKurtosisBD);
    }
    
    // Return group Gaussian probability plot correlation coefficient
    public double[] groupGPPcorrelationCoefficient(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupProbPlotR);
    }
    
    // Return group Gaussian probability plot gradient
    public double[] groupGPPgradient(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupProbPlotGradient);
    }
    
    // Return group Gaussian probability plot intercept
    public double[] groupGPPintercept(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupProbPlotIntercept);
    }
    
    // Return group Gaussian probability plot mu
    public double[] groupGPPmu(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupProbPlotMu);
    }
    
    // Return group Gaussian probability plot sigma
    public double[] groupGPPsigma(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupProbPlotSigma);
    }
    
    // Return group sum of squares as double
    public double[] groupSumOfSquares(){
        if(!this.groupStatsDone)this.groupStatistics();
        return Conv.copy(this.groupSS);
    }
    
    // Return group sum of squares as BigDecimal
    public BigDecimal[] groupSumOfSquares_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return Conv.copy(this.groupSSBD);
    }
    
    // TOTALS
    // Return total mean as double
    public double totalMean(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMean;
    }
    
    // Return total mean as BigDecimal
    public BigDecimal totalMean_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalMeanBD;
    }
    
    // Return total standard deviation
    public double totalStandardDeviation(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalSD;
    }
    
    // Return total standard error
    public double totalStandardError(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalSE;
    }
    
    // Return total mean lower 95% confidence limit
    public double totalMeanLowerConfidenceLimit(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalClb;
    }
    
    // Return total mean upper 95% confidence limit
    public double totalMeanUpperConfidenceLimit(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalCub;
    }
    
    // Return total maxima as double
    public double totalMaximum(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMaximum;
    }
    
    // Return total maximum as BigDecimal
    public BigDecimal totalMaxima_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalMaximumBD;
    }
    
    // Return total minimum as double
    public double totalMinimum(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMinimum;
    }
    
    // Return total minimum as BigDecimal
    public BigDecimal totalMinimum_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalMinimumBD;
    }
    
    // Return total median as double
    public double totalMedian(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMedian;
    }
    
    // Return total median as BigDecimal
    public BigDecimal totalMedian_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalMedianBD;
    }
    
    // Return total moment skewness as double
    public double totalMomentSkewness(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMomentSkewness;
    }
    
    // Return total median skewness as double
    public double totalMedianSkewness(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalMedianSkewness;
    }
    
    // Return total quartile skewness as double
    public double totalQuartileSkewness(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalQuartileSkewness;
    }
    
    // Return total quartile skewness as BigDecimal
    public BigDecimal totalQuartileSkewness_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalQuartileSkewnessBD;
    }
    
    // Return total kurtosis as double
    public double totalKurtosis(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalKurtosis;
    }
    
    // Return total kurtosis as BigDecimal
    public BigDecimal totalKurtosis_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalKurtosisBD;
    }
    
    // Return total excess kurtosis as double
    public double totalExcessKurtosis(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalExcessKurtosis;
    }
    
    // Return total excess kurtosis as BigDecimal
    public BigDecimal totalExcessKurtosis_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalExcessKurtosisBD;
    }
    
    // Return total Gaussian probability plot correlation coefficient
    public double totalGPPcorrelationCoefficient(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalProbPlotR;
    }
    
    // Return total Gaussian probability plot gradient
    public double totalGPPgradient(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalProbPlotGradient;
    }
    
    // Return total Gaussian probability plot intercept
    public double totalGPPintercept(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalProbPlotIntercept;
    }
    
    // Return total Gaussian probability plot mu
    public double totalGPPmu(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalProbPlotMu;
    }
    
    // Return total Gaussian probability plot sigma
    public double totalGPPsigma(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalProbPlotSigma;
    }
    
    // Return total sum of squares as double
    public double totalSumOfSquares(){
        if(!this.groupStatsDone)this.groupStatistics();
        return this.totalSS;
    }
    
    // Return total sum of squares as BigDecimal
    public BigDecimal totalSumOfSquares_as_BD(){
        if(!this.groupStatsDone)this.groupStatisticsBD();
        return this.totalSSBD;
    }
    
    
    // ANOVA
    // One way ANOVA analysis plus file output
    // No output filename supplied
    public void oneWayAnalysis(){
      if(!this.bigDecimal){
            this.oneWayAnalysis_d();
        }
        else{
           this.oneWayAnalysis_BD();
        }
    }
    
    // One way ANOVA analysis plus file output
    // with comparison of all pairs
    // No output filename supplied
    public void oneWayAnalysisWithPairComparison(){
        this.comparison = true;
        this.nComparisons = (this.nGroups*(this.nGroups - 1))/2;
        this.compGroups = new boolean[this.nGroups];
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayAnalysis_d();
        }
        else{
           this.oneWayAnalysis_BD();
        }
    }
    
    // One way ANOVA analysis plus file output
    // with comparison of pair ii, jj
    // No output filename supplied
    public void oneWayAnalysisWithPairComparison(int ii, int jj){
        if(ii==jj)throw new IllegalArgumentException("the two groups must be different");
        if(ii<1 || ii>this.nGroups)throw new IllegalArgumentException("The group index, " + ii + ", must be greater than 0 and less than " + (this.nGroups+1));
        if(jj<1 || jj>this.nGroups)throw new IllegalArgumentException("The group index, " + jj + ", must be greater than 0 and less than " + (this.nGroups+1));
        this.comparison = true;
        this.compGroups = new boolean[this.nGroups];
        this.nComparisons = 1;
        ii--;
        jj--;
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = false;
            if(i==ii)this.compGroups[i] = true;
            if(i==jj)this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayAnalysis_d();
        }
        else{
           this.oneWayAnalysis_BD();
        }
    }
    
    // One way ANOVA analysis plus file output in double precision
    // No output filename supplied
    private void oneWayAnalysis_d(){
        this.oneWayANOVA_d();
        this.outputAnalysis();
    }
    
    // One way ANOVA analysis plus file output in aribtrary precision
    // No output filename supplied
    private void oneWayAnalysis_BD(){
        this.oneWayANOVA_d();
        this.outputAnalysis();
    }
    
    // One way ANOVA analysis plus file output
    // filename supplied
    public void oneWayAnalysis(String filename){
      if(!this.bigDecimal){
            this.oneWayAnalysis_d(filename);
        }
        else{
           this.oneWayAnalysis_BD(filename);
        }
    }
    
    // One way ANOVA analysis plus file output
    // with comparison of all pairs
    // filename supplied
    public void oneWayAnalysisWithPairComparison(String filename){
        this.comparison = true;
        this.nComparisons = (this.nGroups*(this.nGroups - 1))/2;
        this.compGroups = new boolean[this.nGroups];
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayAnalysis_d(filename);
        }
        else{
           this.oneWayAnalysis_BD(filename);
        }
    }
    
    // One way ANOVA analysis plus file output
    // with comparison of pair ii, jj
    // filename supplied
    public void oneWayAnalysisWithPairComparison(int ii, int jj, String filename){
        if(ii==jj)throw new IllegalArgumentException("the two groups must be different");
        if(ii<1 || ii>this.nGroups)throw new IllegalArgumentException("The group index, " + ii + ", must be greater than 0 and less than " + (this.nGroups+1));
        if(jj<1 || jj>this.nGroups)throw new IllegalArgumentException("The group index, " + jj + ", must be greater than 0 and less than " + (this.nGroups+1));
        this.comparison = true;
        this.compGroups = new boolean[this.nGroups];
        this.nComparisons = 1;
        ii--;
        jj--;
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = false;
            if(i==ii)this.compGroups[i] = true;
            if(i==jj)this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayAnalysis_d(filename);
        }
        else{
           this.oneWayAnalysis_BD(filename);
        }
    }
    
    // One way ANOVA analysis plus file output in double precision
    // Output filename supplied
    private void oneWayAnalysis_d(String filename){
        this.oneWayANOVA_d();
        this.outputAnalysis(filename);
    }
    
    // One way ANOVA analysis plus file output in arbitrary precision
    // Output filename supplied
    private void oneWayAnalysis_BD(String filename){
        this.oneWayANOVA_BD();
        this.outputAnalysis(filename);
    }
    
    // One way ANOVA
    public void oneWayANOVA(){
        if(!this.bigDecimal){
            this.oneWayANOVA_d();
        }
        else{
           this.oneWayANOVA_BD();
        }
    }
    
    // One way ANOVA
    // with comparison of all pairs
    public void oneWayANOVAwithPairComparison(){
        this.comparison = true;
        this.nComparisons = (this.nGroups*(this.nGroups - 1))/2;
        this.compGroups = new boolean[this.nGroups];
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayANOVA_d();
        }
        else{
           this.oneWayANOVA_BD();
        }
    }
    
    // One way ANOVA
    // with comparison of the pairs, ii and jj
    public void oneWayANOVAwithPairComparison(int ii, int jj){
        if(ii==jj)throw new IllegalArgumentException("the two groups must be different");
        if(ii<1 || ii>this.nGroups)throw new IllegalArgumentException("The group index, " + ii + ", must be greater than 0 and less than " + (this.nGroups+1));
        if(jj<1 || jj>this.nGroups)throw new IllegalArgumentException("The group index, " + jj + ", must be greater than 0 and less than " + (this.nGroups+1));
        this.comparison = true;
        this.compGroups = new boolean[this.nGroups];
        this.nComparisons = 1;
        ii--;
        jj--;
        for(int i=0;i<this.nGroups;i++){
            this.compGroups[i] = false;
            if(i==ii)this.compGroups[i] = true;
            if(i==jj)this.compGroups[i] = true;
        }
        if(!this.bigDecimal){
            this.oneWayANOVA_d();
        }
        else{
           this.oneWayANOVA_BD();
        }
    }
    
    //  One way ANOVA in double precision
    private void oneWayANOVA_d(){
        if(!this.groupStatsDone){
            if(this.allCalcn){
                this.groupStatistics();
            }
            else{
                // total mean
                this.totalMean = 0.0;
                for(int i=0; i<this.nGroups; i++){
                    for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                        this.totalMean += this.responses0[i][j];
                    }
                }
                this.totalMean /= this.nTotalResponses;
                this.totalMeanBD = new BigDecimal(this.totalMean); 
                // group means
                this.groupMeans = new double[this.nGroups];
                this.groupMeansBD = new BigDecimal[this.nGroups];
                for(int i=0; i<this.nGroups; i++){
                    this.groupMeans[i] = 0.0;
                    for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                        this.groupMeans[i] += this.responses0[i][j];
                    }
                    this.groupMeans[i] /= this.nResponsesPerGroup[i];
                    this.groupMeansBD[i] = new BigDecimal(this.groupMeans[i]);
                }               
            }
        }
        
        // Degrees of freedom
        this.dofTotal = this.nTotalResponses - 1;
        this.dofBetweenGroups = this.nGroups - 1;
        this.dofWithinGroups =  this.nTotalResponses - this.nGroups;
        

        //  Sums of squares
        double sst = 0.0;  // total
        double ssb = 0.0;  // between groups
        double ssw = 0.0;  // within groups
        double hold = 0.0;
        for(int i=0; i<this.nGroups; i++){
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                hold = this.responses0[i][j] - this.totalMean;
                sst += hold*hold;
                hold = this.groupMeans[i] - this.totalMean;
                ssb += hold*hold;
                hold = this.responses0[i][j] - this.groupMeans[i];
                ssw += hold*hold;
            }
        }
        this.ssTotal = sst;
        this.ssTotalBD = new BigDecimal(sst);
        this.meanSquareTotal = sst/dofTotal;
        this.meanSquareTotalBD = new BigDecimal(this.meanSquareTotal);
        
        this.ssBetween = ssb;
        this.ssBetweenBD = new BigDecimal(ssb);
        this.meanSquareBetween = ssb/dofBetweenGroups;
        this.meanSquareBetweenBD = new BigDecimal(this.meanSquareBetween);
        
        this.ssWithin = ssw;
        this.ssWithinBD = new BigDecimal(ssw);
        this.meanSquareWithin = ssw/dofWithinGroups;
        this.meanSquareWithinBD = new BigDecimal(this.meanSquareWithin); 

  
        // All groups F-ratio
        this.fRatio = this.meanSquareBetween/this.meanSquareWithin;
        this.fRatioBD = new BigDecimal(this.fRatio); 
        this.fRatioP = Stat.fCompCDF(this.fRatio, this.dofBetweenGroups, this.dofWithinGroups);
        this.criticalFratio = Stat.fDistributionInverseCDF(this.dofBetweenGroups, this.dofWithinGroups, this.criticalSignificance);
        
        // Group pair comparisons
        if(this.nGroups>2 && this.comparison){
            this.meanSquareTotalG = new double[this.nComparisons];                          // total  mean square of pair comparisons as double
            this.meanSquareWithinG = new double[this.nComparisons];                         // mean square within groups of pair comparisons as double
            this.meanSquareBetweenG  = new double[this.nComparisons];                       // mean square between groups of pair comparisons as double
            this.meanSquareTotalBDG  = new BigDecimal[this.nComparisons];                   // total mean square of pair comparisons as BigDecimal
            this.meanSquareWithinBDG  = new BigDecimal[this.nComparisons];                  // mean square within groups of pair comparisons as BigDecimal
            this.meanSquareBetweenBDG  = new BigDecimal[this.nComparisons];                 // mean square between groups of pair comparisons as BigDecimal
            this.fRatioG  = new double[this.nComparisons];                                  // one way ANOVA F-ratio of pair comparisons as double
            this.fRatioBDG  = new BigDecimal[this.nComparisons];                            // one way ANOVA F-ratio of pair comparisons as BigDecimal
            this.fRatioPG = new double[this.nComparisons];                                  // one way ANOVA F-ratio probability of pair comparisons
            this.criticalFratioG  = new double[this.nComparisons];                          // critical F-ratio of pair comparisons
            this.ssTotalG = new double[this.nComparisons];                       
            this.ssWithinG = new double[this.nComparisons];
            this.ssBetweenG = new double[this.nComparisons];
            this.ssTotalBDG = new BigDecimal[this.nComparisons];                       
            this.ssWithinBDG = new BigDecimal[this.nComparisons]; 
            this.ssBetweenBDG = new BigDecimal[this.nComparisons]; 
            this.dofTotalG = new int[this.nComparisons];  
            this.dofBetweenG = new int[this.nComparisons]; 
            this.dofWithinG = new int[this.nComparisons];
            this.pairIndices = new int[this.nComparisons][2];
                    
            double[][] comp = new double[2][]; 
            int counter = 0;
            for(int i=0;  i<this.nGroups-1; i++){
                for(int j=i+1;  j<this.nGroups; j++){
                    if(this.compGroups[i] && this.compGroups[j]){
                        comp[0] = this.responses0[i];
                        comp[1] = this.responses0[j];
                        this.pairIndices[counter][0] = i;
                        this.pairIndices[counter][1] = j;
                        ANOVA av = new ANOVA();
                        av.setAllCalcn(false);
                        av.enterResponseData(comp);
                        av.oneWayANOVA();
                        this.ssTotalG[counter] = av.totalSumOfSquares();                          
                        this.ssWithinG[counter] = av.withinGroupsSumOfSquares();
                        this.ssBetweenG[counter] = av.betweenGroupsSumOfSquares();
                        this.ssTotalBDG[counter] = new BigDecimal(this.ssTotalG[counter]);                        
                        this.ssWithinBDG[counter] = new BigDecimal(this.ssWithinG[counter]);
                        this.ssBetweenBDG[counter] = new BigDecimal(this.ssBetweenG[counter]);
                        this.meanSquareTotalG[counter] = av.totalMeanSquare();                          
                        this.meanSquareWithinG[counter] = av.withinGroupsMeanSquare();
                        this.meanSquareBetweenG[counter] = av.betweenGroupsMeanSquare();
                        this.meanSquareTotalBDG[counter] = new BigDecimal(this.meanSquareTotalG[counter]);           
                        this.meanSquareWithinBDG[counter] = new BigDecimal(this.meanSquareWithinG[counter]);  
                        this.meanSquareBetweenBDG[counter] = new BigDecimal(this.meanSquareBetweenG[counter]);
                        this.fRatioG[counter] = av.oneWayFratio();
                        this.fRatioBDG[counter]  = new BigDecimal(this.fRatioG[counter]);                            
                        this.fRatioPG[counter] = av.oneWaySignificance();
                        this.criticalFratioG[counter]  = av.criticalFratio();
                        this.dofTotalG[counter] = av.totalDoF();
                        this.dofBetweenG[counter] = av.betweenGroupsDoF();
                        this.dofWithinG[counter] = av.withinGroupsDoF();
                        counter++;
                    }
                }
            }
        }
        this.oneWayDone = true;
    }
    
    
    //  One way ANOVA in arbitrary precision
    private void oneWayANOVA_BD(){
        if(!this.groupStatsDone){
            if(this.allCalcn){
                this.groupStatisticsBD();
            }
            else{
                // total mean
                this.totalMeanBD = new BigDecimal("0.0");
                for(int i=0; i<this.nGroups; i++){
                    for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                        this.totalMeanBD = this.totalMeanBD.add(this.responsesBD[i][j]);
                    }
                }
                this.totalMeanBD = this.totalMeanBD.divide(new BigDecimal(this.nTotalResponses), BigDecimal.ROUND_HALF_UP);
                this.totalMean = this.totalMeanBD.doubleValue(); 
                // group means
                this.groupMeans = new double[this.nGroups];
                this.groupMeansBD = new BigDecimal[this.nGroups];
                for(int i=0; i<this.nGroups; i++){
                    this.groupMeansBD[i] = new BigDecimal("0.0");
                    for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                        this.groupMeansBD[i] = this.groupMeansBD[i].add(this.responsesBD[i][j]);
                    }
                    this.groupMeansBD[i] = this.groupMeansBD[i].divide(new BigDecimal(this.nResponsesPerGroup[i]), BigDecimal.ROUND_HALF_UP);
                    this.groupMeans[i] = this.groupMeansBD[i].doubleValue();
                }               
            }
        }
        
        // Degrees of freedom
        this.dofTotal = this.nTotalResponses - 1;
        this.dofBetweenGroups = this.nGroups - 1;
        this.dofWithinGroups =  this.nTotalResponses - this.nGroups;
        
        //  Sums of squares
        BigDecimal sst = new BigDecimal("0.0");  // total
        BigDecimal ssb = new BigDecimal("0.0");  // between groups
        BigDecimal ssw = new BigDecimal("0.0");  // within groups
        BigDecimal hold = new BigDecimal("0.0");
        for(int i=0; i<this.nGroups; i++){
            for(int j=0; j<this.nResponsesPerGroup[i]; j++){
                hold = this.responsesBD[i][j].subtract(this.totalMeanBD);
                sst = sst.add(hold.multiply(hold));
                hold = this.groupMeansBD[i].subtract(this.totalMeanBD);
                ssb = ssb.add(hold.multiply(hold));
                hold = this.responsesBD[i][j].subtract(this.groupMeansBD[i]);
                ssw = ssw.add(hold.multiply(hold));
            }
        }
        this.ssTotalBD = sst;
        this.ssTotal = sst.doubleValue();
        this.meanSquareTotalBD = sst.divide(new BigDecimal((double)dofTotal), BigDecimal.ROUND_HALF_UP);
        this.meanSquareTotal = this.meanSquareTotalBD.doubleValue(); 
        this.totalSD = Math.sqrt(this.meanSquareTotalBD.doubleValue());
        
        this.ssBetweenBD = ssb;
        this.ssBetween = ssb.doubleValue();
        this.meanSquareBetweenBD = ssb.divide(new BigDecimal((double)dofBetweenGroups), BigDecimal.ROUND_HALF_UP);
        this.meanSquareBetween = this.meanSquareBetweenBD.doubleValue();
        
        this.ssWithinBD = ssw;
        this.ssWithin = ssw.doubleValue();
        this.meanSquareWithinBD = ssw.divide(new BigDecimal((double)dofWithinGroups), BigDecimal.ROUND_HALF_UP);
        this.meanSquareWithin = this.meanSquareWithinBD.doubleValue();
  
        // All Groups F-ratio
        this.fRatioBD = this.meanSquareBetweenBD.divide(this.meanSquareWithinBD, BigDecimal.ROUND_HALF_UP);
        this.fRatio = this.fRatioBD.doubleValue();
        this.fRatioP = Stat.fCompCDF(this.fRatio, this.dofBetweenGroups, this.dofWithinGroups);
        this.criticalFratio = Stat.fDistributionInverseCDF(this.dofBetweenGroups, this.dofWithinGroups, this.criticalSignificance);
        
        // Group pair comparisons
        if(this.nGroups>2 && this.comparison){
            this.meanSquareTotalG = new double[this.nComparisons];                          // total  mean square of pair comparisons as double
            this.meanSquareWithinG = new double[this.nComparisons];                         // mean square within groups of pair comparisons as double
            this.meanSquareBetweenG  = new double[this.nComparisons];                       // mean square between groups of pair comparisons as double
            this.meanSquareTotalBDG  = new BigDecimal[this.nComparisons];                   // total mean square of pair comparisons as BigDecimal
            this.meanSquareWithinBDG  = new BigDecimal[this.nComparisons];                  // mean square within groups of pair comparisons as BigDecimal
            this.meanSquareBetweenBDG  = new BigDecimal[this.nComparisons];                 // mean square between groups of pair comparisons as BigDecimal
            this.fRatioG  = new double[this.nComparisons];                                  // one way ANOVA F-ratio of pair comparisons as double
            this.fRatioBDG  = new BigDecimal[this.nComparisons];                            // one way ANOVA F-ratio of pair comparisons as BigDecimal
            this.fRatioPG = new double[this.nComparisons];                                  // one way ANOVA F-ratio probability of pair comparisons
            this.criticalFratioG  = new double[this.nComparisons];                          // critical F-ratio of pair comparisons
            this.ssTotalG = new double[this.nComparisons];                       
            this.ssWithinG = new double[this.nComparisons];
            this.ssBetweenG = new double[this.nComparisons];
            this.ssTotalBDG = new BigDecimal[this.nComparisons];                       
            this.ssWithinBDG = new BigDecimal[this.nComparisons]; 
            this.ssBetweenBDG = new BigDecimal[this.nComparisons]; 
            this.dofTotalG = new int[this.nComparisons];  
            this.dofBetweenG = new int[this.nComparisons]; 
            this.dofWithinG = new int[this.nComparisons];
            this.pairIndices = new int[this.nComparisons][2];
                    
            BigDecimal[][] comp = new BigDecimal[2][]; 
            int counter = 0;
            for(int i=0;  i<this.nGroups-1; i++){
                for(int j=i+1;  j<this.nGroups; j++){
                    if(this.compGroups[i] && this.compGroups[j]){
                        comp[0] = this.responsesBD[i];
                        comp[1] = this.responsesBD[j];
                        this.pairIndices[counter][0] = i;
                        this.pairIndices[counter][1] = j;
                        ANOVA av = new ANOVA();
                        av.setAllCalcn(false);
                        av.enterResponseData(comp);
                        av.oneWayANOVA();
                        this.ssTotalBDG[counter] = av.totalSumOfSquares_as_BD();                          
                        this.ssWithinBDG[counter] = av.withinGroupsSumOfSquares_as_BD();
                        this.ssBetweenBDG[counter] = av.betweenGroupsSumOfSquares_as_BD();
                        this.ssTotalG[counter] = this.ssTotalBDG[counter].doubleValue();                        
                        this.ssWithinG[counter] = this.ssWithinBDG[counter].doubleValue();
                        this.ssBetweenG[counter] = this.ssBetweenBDG[counter].doubleValue();                        
                        this.meanSquareTotalBDG[counter] = av.totalMeanSquare_as_BD();                          
                        this.meanSquareWithinBDG[counter] = av.withinGroupsMeanSquare_as_BD();
                        this.meanSquareBetweenBDG[counter] = av.betweenGroupsMeanSquare_as_BD();
                        this.meanSquareTotalG[counter] = this.meanSquareTotalBDG[counter].doubleValue();           
                        this.meanSquareWithinG[counter] = this.meanSquareWithinBDG[counter].doubleValue();  
                        this.meanSquareBetweenG[counter] = this.meanSquareBetweenBDG[counter].doubleValue();                       
                        this.fRatioBDG[counter] = av.oneWayFratio_as_BD();
                        this.fRatioG[counter] = this.fRatioBDG[counter].doubleValue();                            
                        this.fRatioPG[counter] = av.oneWaySignificance();
                        this.criticalFratioG[counter] = av.criticalFratio();
                        this.dofTotalG[counter] = av.totalDoF();
                        this.dofBetweenG[counter] = av.betweenGroupsDoF();
                        this.dofWithinG[counter] = av.withinGroupsDoF();
                        counter++;
                    }
                }
            }
        }
 
        this.oneWayDone = true;
    }
    
    // One way ANOVA F-ratio as double
    // All groups
    public double oneWayFratio(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.fRatio;
    }
    
    // One way ANOVA F-ratio as double
    // groups ii and jj
    public double oneWayFratio(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.fRatioG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.fRatioG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.fRatioG[0];
        }
        return ret;
    }
    
    // One way ANOVA F-ratio as BigDecimal
    public BigDecimal oneWayFratio_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.fRatioBD;
    }
    
    // One way ANOVA F-ratio as BigDecimal
    // groups ii and jj
    public BigDecimal oneWayFratio_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.fRatioBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.fRatioBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.fRatioBDG[0];
        }
        return ret;
    }
    
    // One way ANOVA significance
    // All groups
    public double oneWaySignificance(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.fRatioP;
    }
    
    // One way ANOVA significance
    // groups ii and jj
    public double oneWaySignificance(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.fRatioPG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.fRatioPG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.fRatioPG[0];
        }
        return ret;
    }
    
    // Critical F-ratio
    // All groups
    public double criticalFratio(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.criticalFratio;
    }
    
    // Critical F-ratio 
    // groups ii and jj
    public double criticalFratio(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.criticalFratioG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.criticalFratioG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.criticalFratioG[0];
        }
        return ret;
    }

    // Total Mean Square as double
    // All groups
    public double totalMeanSquare(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.meanSquareTotal;
    }
    
    // Total Mean Square as double 
    // groups ii and jj
    public double totalMeanSquare(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareTotalG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareTotalG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareTotalG[0];
        }
        return ret;
    }
    
    // Total Mean Square as BigDecimal
    // All groups
    public BigDecimal totalMeanSquare_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.meanSquareTotalBD;
    }
    
    // Total Mean Square as BigDecimal
    // groups ii and jj
    public BigDecimal totalMeanSquare_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareTotalBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareTotalBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareTotalBDG[0];
        }
        return ret;
    }

    // Mean Square between groups as double
    // All groups
    public double betweenGroupsMeanSquare(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.meanSquareBetween;
    }
    
    // Mean Square between groups as double 
    // groups ii and jj
    public double betweenGroupsMeanSquare(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareBetweenG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareBetweenG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareBetweenG[0];
        }
        return ret;
    }
    
    // Mean Square between groups as BigDecimal
    // All groups
    public BigDecimal betweenGroupsMeanSquare_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.meanSquareBetweenBD;
    }
    
    // Mean Square between groups as BigDecimal
    // groups ii and jj
    public BigDecimal betweenGroupsMeanSquare_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareBetweenBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareBetweenBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareBetweenBDG[0];
        }
        return ret;
    }
    
    // Mean Square within groups as double
    // All groups
    public double withinGroupsMeanSquare(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.meanSquareWithin;
    }
    
    // Mean Square within groups as double 
    // groups ii and jj
    public double withinGroupsMeanSquare(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareWithinG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareWithinG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareWithinG[0];
        }
        return ret;
    }

    // Mean Square within groups as BigDecimal
    // All groups
    public BigDecimal withinGroupsMeanSquare_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.meanSquareWithinBD;
    }
    
    // Mean Square within groups as BigDecimal
    // groups ii and jj
    public BigDecimal withinGroupsMeanSquare_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.meanSquareWithinBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.meanSquareWithinBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.meanSquareWithinBDG[0];
        }
        return ret;
    }
    
    // Sum of Squares between groups as double
    // All groups
    public double betweenGroupsSumOfSquares(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.ssBetween;
    }
    
    // Sum of Squares between groups as double
    // groups ii and jj
    public double  betweenGroupsSumOfSquares(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssBetweenG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssBetweenG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssBetweenG[0];
        }
        return ret;
    }
    
    // Sum of Squares between groups as BigDecimal
    // All groups
    public BigDecimal betweenGroupsSumOfSquares_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.ssBetweenBD;
    }
    
    // Sum of Squares between groups as BigDecimal
    // groups ii and jj
    public BigDecimal betweenGroupsSumOfSquares_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssBetweenBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssBetweenBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssBetweenBDG[0];
        }
        return ret;
    }
    
    // Sum of Squares within groups as double
    // All groups
    public double withinGroupsSumOfSquares(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.ssWithin;
    }
    
    // Sum of Squares within groups as double
    // groups ii and jj
    public double  withinGroupsSumOfSquares(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssWithinG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssWithinG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssWithinG[0];
        }
        return ret;
    }
    
    // Sum of Squares within groups as BigDecimal
    // All groups
    public BigDecimal withinGroupsSumOfSquares_as_BD(){
        if(!this.oneWayDone)this.oneWayANOVA_BD();
        return this.ssWithinBD;
    }
    
    // Sum of Squares within groups as BigDecimal
    // groups ii and jj
    public BigDecimal withinGroupsSumOfSquares_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssWithinBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssWithinBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssWithinBDG[0];
        }
        return ret;
    }
    
    // Return total sum of squares as double
    // groups ii and jj
    public double totalSumOfSquares(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        double ret = 0.0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssTotalG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssTotalG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssTotalG[0];
        }
        return ret;
    }
    
    // Return total sum of squares as BigDecimal
    // Groups ii and jj
    public BigDecimal totalSumOfSquares_as_BD(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        BigDecimal ret = new BigDecimal("0.0");
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.ssTotalBDG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.ssTotalBDG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.ssTotalBDG[0];
        }
        return ret;
    }
    
    // Total degrees of freedom
    // All groups
    public int totalDoF(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.dofTotal;
    }
    
    // Within groups degrees of freedom
    // groups ii and jj
    public int totalDoF(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        int ret = 0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.dofTotalG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.dofTotalG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.dofTotalG[0];
        }
        return ret;
    }
    
    
    // Within groups degrees of freedom
    // all groups
    public int withinGroupsDoF(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.dofWithinGroups;
    }
    
    // Within groups degrees of freedom
    // groups ii and jj
    public int withinGroupsDoF(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        int ret = 0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.dofWithinG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.dofWithinG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.dofWithinG[0];
        }
        return ret;
    }
    
    // Between groups degrees of freedom
    // all groups
    public int betweenGroupsDoF(){
        if(!this.oneWayDone)this.oneWayANOVA();
        return this.dofBetweenGroups;
    }
    
    // Between groups degrees of freedom
    // groups ii and jj
    public int betweenGroupsDoF(int ii, int jj){
        int iii = ii-1;
        int jjj = jj-1;
        int index = 0;
        int ret = 0;
        if(this.comparison){
            if(this.compGroups[iii] && this.compGroups[jjj]){
                index = this.pairIndex(iii, jjj);
                ret = this.dofBetweenG[index];
            }
            else{
                ANOVA av = new ANOVA();
                av.enterResponseData(responses0);
                av.oneWayANOVAwithPairComparison(ii, jj);
                ret = av.dofBetweenG[0];
            }
        }
        else{
            this.oneWayANOVAwithPairComparison(ii, jj);
            ret = this.dofBetweenG[0];
        }
        return ret;
    }
    
    // identify pair comparison index
    private int pairIndex(int ii, int jj){
        int ret = -1;
        int i = 0;
        boolean test = true;
        while(test){
            if(this.pairIndices[i][0]==ii && this.pairIndices[i][1]==jj){
                ret = i;
                test = false;
            }
            else{
                if(this.pairIndices[i][0]==jj && this.pairIndices[i][1]==ii){
                    ret = i;
                    test = false;
                }
                else{
                    i++;
                    if(i>=this.nComparisons){
                        throw new IllegalArgumentException("No index found " + ii + " and " + jj);
                    }
                }
            }
        }
        return ret;
    }
    
    
    // OUTPUT THE ANALYSIS
    
    // Output the one way analysis
    // No input file name entered via method argument list
    public void outputAnalysis(){
        if(!this.oneWayDone){
            System.out.println("Method outputAnalysis: As no ANOVA peforming method had been called a ANOVA of all groups, withour pair comparison, has now been performed");
            this.oneWayANOVA();
        }
        this.outputFilename = "OneWayANOVAoutput";
        switch(this.fileOption){
            case 1: this.outputFilename += ".txt";
                    break;
            case 2: this.outputFilename += ".xls";
                    break;
        }
   
        String message1 = "Output file name for the one way ANOVA:";
        String message2 = "\nEnter the required name (as a single word) and click OK ";
        String message3 = "\nor simply click OK for default value";
        String message = message1 + message2 + message3;
        String defaultName = this.outputFilename;
        this.outputFilename = Db.readLine(message, defaultName);
        this.outputAnalysis(this.outputFilename);
    }


    // Output one way analysis
    // input file name via method argument list
    public void outputAnalysis(String filename){
        if(!this.oneWayDone){
            System.out.println("Method outputAnalysis: As no ANOVA peforming method had been called a ANOVA of all groups, withour pair comparison, has now been performed");
            this.oneWayANOVA();
        }
        
        // Open output file
        this.outputFilename = filename;
        String outputFilenameWithoutExtension = null;
        String extension = null;
        int pos = filename.indexOf('.');
        if(pos==-1){
            outputFilenameWithoutExtension = filename;
            switch(this.fileOption){
                case 1: this.outputFilename += ".txt";
                        break;
                case 2: this.outputFilename += ".xls";
                        break;
            }
        }
        else{    
            extension = (filename.substring(pos)).trim();
            outputFilenameWithoutExtension = (filename.substring(0, pos)).trim();
            if(extension.equalsIgnoreCase(".xlsx")){
                extension = ".xls";
                this.outputFilename = outputFilenameWithoutExtension + extension;
            }
            if(extension.equalsIgnoreCase(".xls")){
                if(this.fileOption==1){
                    if(this.fileOptionSet){
                        String message1 = "Your entered output file type is " + extension;
                        String message2 = "\nbut you have chosen a .txt output";
                        String message = message1 + message2;
                        String headerComment = "Your output file name extension";
                        String[] comments = {message, "replace it with .txt [text file]"};
                        String[] boxTitles = {"Retain", ".txt"};
                        int defaultBox = 1;
                        int opt =  Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                        if(opt==2)this.outputFilename = outputFilenameWithoutExtension + ".txt";
                    }
                    else{
                        this.fileOption=2;
                    }
                }
            }
            if(extension.equalsIgnoreCase(".txt")){
                if(this.fileOption==2){
                    if(this.fileOptionSet){
                        String message1 = "Your entered output file type is .txt";
                        String message2 = "\nbut you have chosen a .xls output";
                        String message = message1 + message2;
                        String headerComment = "Your output file name extension";
                        String[] comments = {message, "replace it with .xls [Excel file]"};
                        String[] boxTitles = {"Retain", ".xls"};
                        int defaultBox = 1;
                        int opt =  Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                        if(opt==2)this.outputFilename = outputFilenameWithoutExtension + ".xls";
                    }
                    else{
                        this.fileOption=1;
                    }
                }
             }
             if(!extension.equalsIgnoreCase(".txt") && !extension.equalsIgnoreCase(".xls")){
                    String message1 = "Your extension is " + extension;
                    String message2 = "\n    Do you wish to retain it:";
                    String message = message1 + message2;
                    String headerComment = "Your output file name extension";
                    String[] comments = {message, "replace it with .txt [text file]", "replace it with .xls [MS Excel file]"};
                    String[] boxTitles = {"Retain", ".txt", ".xls"};
                    int defaultBox = 1;
                    int opt =  Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                    switch(opt){
                        case 1: this.fileOption=1;
                                break;
                        case 2: this.outputFilename = outputFilenameWithoutExtension + ".txt";
                                this.fileOption=1;
                                break;
                        case 3: this.outputFilename = outputFilenameWithoutExtension + ".xls";
                                this.fileOption=2;
                                break;
                    }
            }
        }
        
        // Set output precision
        if(!this.truncAll)this.setOutputPrecision();
        
        // Write to output file
        if(this.fileOption==1){
            this.outputText();
        }
        else{
            this.outputExcel();
        }
        
        System.out.println("The ANOVA summary has been written to the file " + this.outputFilename);
    }

            
    // Set precision of the output data
    private void setOutputPrecision(){
        int maxPrec = Fmath.checkPrecision(this.responses0[0][0]);
        int prech = maxPrec;
        for(int i=0; i<this.nGroups; i++){
            for(int j=0; j<this.nResponsesPerGroup[i];j++){
                prech = Fmath.checkPrecision(this.responses0[i][j]);
                if(prech>maxPrec)maxPrec = prech;
            }
        }
        if(!this.truncAll && maxPrec>this.trunc)this.trunc = maxPrec;
    }

    // Output processed data as a text file
    private void outputText(){

        FileOutput fout = new FileOutput(this.outputFilename);
        fout.println("PROGRAM ANOVA");
        for(int i=0; i<this.nTitle; i++)fout.println(this.title[i]);
        if(this.inputFilename!=null)fout.println("Input file name: " + this.inputFilename);
        fout.println("This output file name: " + this.outputFilename);        
        fout.println();
        
 
        fout.println("ALL GROUPS: One-Way Analysis of Variance");
        fout.print(" ", 16);
        fout.print(" ", this.fieldD);
        fout.print("  ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print("F-ratio at", this.fieldD);
        fout.println(" ");
        
        fout.print(" ", 16);
        fout.print("Sum of", this.fieldD);
        fout.print("Degrees of ", this.fieldD);
        fout.print("Mean ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(this.criticalSignificanceF + " critical", this.fieldD);
        fout.println(" ");
        
        fout.print(" ", 16);
        fout.print("squares", this.fieldD);
        fout.print("freedom ", this.fieldD);
        fout.print("square ", this.fieldD);
        fout.print("F-ratio ", this.fieldD);
        fout.print("level", this.fieldD);
        fout.println("Significance");
        
        fout.print("Between groups", 16);
        fout.print(Fmath.truncate(this.ssBetween, this.trunc), this.fieldD);
        fout.print(this.dofBetweenGroups, this.fieldD);
        fout.print(Fmath.truncate(this.meanSquareBetween, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.fRatio, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.criticalFratio, this.trunc), this.fieldD);
        fout.println(Fmath.truncate(this.fRatioP, this.trunc));
        
        fout.print("Within groups", 16);
        fout.print(Fmath.truncate(this.ssWithin, this.trunc), this.fieldD);
        fout.print(this.dofWithinGroups, this.fieldD);
        fout.println(Fmath.truncate(this.meanSquareWithin, this.trunc));
     
        fout.print("Total", 16);
        fout.print(Fmath.truncate(this.ssTotal, this.trunc), this.fieldD);
        fout.print(this.dofTotal, this.fieldD);
        fout.println(Fmath.truncate(this.meanSquareTotal, this.trunc));
     
        fout.println();
        fout.println();
        
        fout.println("ALL GROUPS: Individual Group Statistics");    
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.println(this.criticalSignificanceS + " confidence interval");
        
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.println("of the mean");
                
        fout.print(" ", this.fieldD);
        fout.print("Number of", this.fieldD);
        fout.print(" ", this.fieldD);
        fout.print("Standard", this.fieldD);
        fout.print("Standard", this.fieldD);
        fout.print("lower", this.fieldD);
        fout.println("upper");
        
        fout.print(" ", this.fieldD);
        fout.print("responses", this.fieldD);
        fout.print("Mean", this.fieldD);
        fout.print("deviation", this.fieldD);
        fout.print("error", this.fieldD);
        fout.print("bound ", this.fieldD);
        fout.println("bound ");
        
        for(int i=0; i<this.nGroups; i++){
            fout.print(this.groupNames[i], this.fieldD);
            fout.print(this.nResponsesPerGroup[i], this.fieldD);
            fout.print(Fmath.truncate(this.groupMeans[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupSD[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupSE[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupClb[i], this.trunc), this.fieldD);
            fout.println(Fmath.truncate(this.groupCub[i], this.trunc));

        }
        fout.println(" ");
        fout.print("Total", this.fieldD);
        fout.print(this.nTotalResponses, this.fieldD);
        fout.print(Fmath.truncate(this.totalMean, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalSD, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalSE, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalClb, this.trunc), this.fieldD);
        fout.println(Fmath.truncate(this.totalCub, this.trunc));
        
        fout.println();
        fout.print("       ", this.fieldD);
        fout.print("minimum", this.fieldD);
        fout.print("median ", this.fieldD);
        fout.print("maximum ", this.fieldD);
        fout.print("moment ", this.fieldD);
        fout.print("median ", this.fieldD);
        fout.print("quartile ", this.fieldD);
        fout.print("kurtosis ", this.fieldD);
        fout.println("excess ");
        
        fout.print("       ", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.print("skewness", this.fieldD);
        fout.print("skewness", this.fieldD);
        fout.print("skewness", this.fieldD);
        fout.print("        ", this.fieldD);
        fout.println("kurtosis");
                
        for(int i=0; i<this.nGroups; i++){
            fout.print(this.groupNames[i], this.fieldD);
            fout.print(Fmath.truncate(this.groupMinimum[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupMedians[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupMaximum[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupMomentSkewness[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupMedianSkewness[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupQuartileSkewness[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupKurtosis[i], this.trunc), this.fieldD);
            fout.println(Fmath.truncate(this.groupExcessKurtosis[i], this.trunc));
            
        }
        fout.println(" ");
        fout.print("Total", this.fieldD);
        fout.print(Fmath.truncate(this.totalMinimum, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalMedian, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalMaximum, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalMomentSkewness, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalMedianSkewness, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalQuartileSkewness, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalKurtosis, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalExcessKurtosis, this.trunc), this.fieldD);
        fout.println(Fmath.truncate(this.totalProbPlotR, this.trunc));
    
        fout.println();
        fout.println("Gaussian Probability Plot");
        fout.print("       ", this.fieldD);
        fout.print("Correlation", this.fieldD);
        fout.print("Gradient", this.fieldD);
        fout.print("Intercept", this.fieldD);
        fout.print("mu", this.fieldD);
        fout.println("sigma");
   
        fout.print("       ", this.fieldD);
        fout.print("coefficient", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.print("       ", this.fieldD);
        fout.println("     ");
        
        for(int i=0; i<this.nGroups; i++){
            fout.print(this.groupNames[i], this.fieldD);
            fout.print(Fmath.truncate(this.groupProbPlotR[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupProbPlotGradient[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupProbPlotIntercept[i], this.trunc), this.fieldD);
            fout.print(Fmath.truncate(this.groupProbPlotMu[i], this.trunc), this.fieldD);
            fout.println(Fmath.truncate(this.groupProbPlotSigma[i], this.trunc));
        }
        fout.println(" ");
        fout.print("total", this.fieldD);
        fout.print(Fmath.truncate(this.totalProbPlotR, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalProbPlotGradient, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalProbPlotIntercept, this.trunc), this.fieldD);
        fout.print(Fmath.truncate(this.totalProbPlotMu, this.trunc), this.fieldD);
        fout.println(Fmath.truncate(this.totalProbPlotSigma, this.trunc));
        
        if(this.nGroups>2 && this.comparison){
            fout.println(" ");
            fout.println(" ");
            fout.println("COMPARISON OF PAIRS OF GROUPS: One-Way Analysis of Variance");
            int counter = 0;
            for(int i=0; i<this.nGroups-1; i++){
                for(int j=i+1; j<this.nGroups; j++){
                    if(this.compGroups[i] && this.compGroups[j]){
                        fout.println("Group"+(i+1)+" and Group"+(j+1));
                        fout.print(" ", 16);
                        fout.print(" ", this.fieldD);
                        fout.print("  ", this.fieldD);
                        fout.print(" ", this.fieldD);
                        fout.print(" ", this.fieldD);
                        fout.print("F-ratio at", this.fieldD);
                        fout.println(" ");
        
                        fout.print(" ", 16);
                        fout.print("Sum of", this.fieldD);
                        fout.print("Degrees of ", this.fieldD);
                        fout.print("Mean ", this.fieldD);
                        fout.print(" ", this.fieldD);
                        fout.print(this.criticalSignificanceF + " critical", this.fieldD);
                        fout.println(" ");
        
                        fout.print(" ", 16);
                        fout.print("squares", this.fieldD);
                        fout.print("freedom ", this.fieldD);
                        fout.print("square ", this.fieldD);
                        fout.print("F-ratio ", this.fieldD);
                        fout.print("level", this.fieldD);
                        fout.println("Significance");
                        
                        fout.print("Between groups", 16);
                        fout.print(Fmath.truncate(this.ssBetweenG[counter], this.trunc), this.fieldD);
                        fout.print(this.dofBetweenG[counter], this.fieldD);
                        fout.print(Fmath.truncate(this.meanSquareBetweenG[counter], this.trunc), this.fieldD);
                        fout.print(Fmath.truncate(this.fRatioG[counter], this.trunc), this.fieldD);
                        fout.print(Fmath.truncate(this.criticalFratioG[counter], this.trunc), this.fieldD);
                        fout.println(Fmath.truncate(this.fRatioPG[counter], this.trunc));
                        
                        fout.print("Within groups", 16);
                        fout.print(Fmath.truncate(this.ssWithinG[counter], this.trunc), this.fieldD);
                        fout.print(this.dofWithinG[counter], this.fieldD);
                        fout.println(Fmath.truncate(this.meanSquareWithinG[counter], this.trunc));
                        
                        fout.print("Total", 16);
                        fout.print(Fmath.truncate(this.ssTotalG[counter], this.trunc), this.fieldD);
                        fout.print(this.dofTotalG[counter], this.fieldD);
                        fout.println(Fmath.truncate(this.meanSquareTotalG[counter], this.trunc));
                        counter++;
                        fout.println();
                        fout.println();
                    }
                }
            }
        }
        fout.close();
    }


    // Output processed data as a text file
    private void outputExcel(){

        FileOutput fout = new FileOutput(this.outputFilename);
        fout.println("PROGRAM ANOVA");
        for(int i=0; i<this.nTitle; i++)fout.println(this.title[i]);
        if(this.inputFilename!=null)fout.println("Input file name: " + this.inputFilename);
        fout.println("This output file name: " + this.outputFilename);        
        fout.println();
        
        fout.println("ALL GROUPS: One-Way Analysis of Variance");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("  ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("F-ratio at");
        fout.println(" ");
        
        fout.printtab(" ");
        fout.printtab("Sum of");
        fout.printtab("Degrees of ");
        fout.printtab("Mean ");
        fout.printtab(" ");
        fout.printtab(this.criticalSignificanceF + " critical");
        fout.println(" ");
        
        fout.printtab(" ");
        fout.printtab("squares");
        fout.printtab("freedom ");
        fout.printtab("square ");
        fout.printtab("F-ratio ");
        fout.printtab("level");
        fout.println("Significance");
        
        fout.printtab("Between groups");
        fout.printtab(Fmath.truncate(this.ssBetween, this.trunc));
        fout.printtab(this.dofBetweenGroups);
        fout.printtab(Fmath.truncate(this.meanSquareBetween, this.trunc));
        fout.printtab(Fmath.truncate(this.fRatio, this.trunc));
        fout.printtab(Fmath.truncate(this.criticalFratio, this.trunc));
        fout.println(Fmath.truncate(this.fRatioP, this.trunc));
        
        fout.printtab("Within groups");
        fout.printtab(Fmath.truncate(this.ssWithin, this.trunc));
        fout.printtab(this.dofWithinGroups);
        fout.println(Fmath.truncate(this.meanSquareWithin, this.trunc));
     
        fout.printtab("Total");
        fout.printtab(Fmath.truncate(this.ssTotal, this.trunc));
        fout.printtab(this.dofTotal);
        fout.println(Fmath.truncate(this.meanSquareTotal, this.trunc));
     
        fout.println();
        fout.println();
        fout.println("ALL GROUPS: Individual Group Statistics"); 
        
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.println(this.criticalSignificanceS + " confidence interval");
        
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.println("of the mean");
                
        fout.printtab(" ");
        fout.printtab("Number of");
        fout.printtab(" ");
        fout.printtab("Standard");
        fout.printtab("Standard");
        fout.printtab("lower");
        fout.println("upper");
        
        fout.printtab(" ");
        fout.printtab("responses");
        fout.printtab("Mean");
        fout.printtab("deviation");
        fout.printtab("error");
        fout.printtab("bound ");
        fout.println("bound ");
        
        for(int i=0; i<this.nGroups; i++){
            fout.printtab(this.groupNames[i]);
            fout.printtab(this.nResponsesPerGroup[i]);
            fout.printtab(Fmath.truncate(this.groupMeans[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupSD[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupSE[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupClb[i], this.trunc));
            fout.println(Fmath.truncate(this.groupCub[i], this.trunc));

        }
        fout.println(" ");
        fout.printtab("Total");
        fout.printtab(this.nTotalResponses);
        fout.printtab(Fmath.truncate(this.totalMean, this.trunc));
        fout.printtab(Fmath.truncate(this.totalSD, this.trunc));
        fout.printtab(Fmath.truncate(this.totalSE, this.trunc));
        fout.printtab(Fmath.truncate(this.totalClb, this.trunc));
        fout.println(Fmath.truncate(this.totalCub, this.trunc));
        
        fout.println();
        fout.printtab("       ");
        fout.printtab("minimum");
        fout.printtab("median ");
        fout.printtab("maximum ");
        fout.printtab("moment ");
        fout.printtab("median ");
        fout.printtab("quartile ");
        fout.printtab("kurtosis ");
        fout.println("excess ");
        
        fout.printtab("       ");
        fout.printtab("       ");
        fout.printtab("       ");
        fout.printtab("       ");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.printtab("        ");
        fout.println("kurtosis");
                
        for(int i=0; i<this.nGroups; i++){
            fout.printtab(this.groupNames[i]);
            fout.printtab(Fmath.truncate(this.groupMinimum[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupMedians[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupMaximum[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupMomentSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupMedianSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupQuartileSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupKurtosis[i], this.trunc));
            fout.println(Fmath.truncate(this.groupExcessKurtosis[i], this.trunc));
            
        }
        fout.println(" ");
        fout.printtab("Total");
        fout.printtab(Fmath.truncate(this.totalMinimum, this.trunc));
        fout.printtab(Fmath.truncate(this.totalMedian, this.trunc));
        fout.printtab(Fmath.truncate(this.totalMaximum, this.trunc));
        fout.printtab(Fmath.truncate(this.totalMomentSkewness, this.trunc));
        fout.printtab(Fmath.truncate(this.totalMedianSkewness, this.trunc));
        fout.printtab(Fmath.truncate(this.totalQuartileSkewness, this.trunc));
        fout.printtab(Fmath.truncate(this.totalKurtosis, this.trunc));
        fout.printtab(Fmath.truncate(this.totalExcessKurtosis, this.trunc));
        fout.println(Fmath.truncate(this.totalProbPlotR, this.trunc));
    
        fout.println();
        fout.println("Gaussian Probability Plot");
        fout.printtab("       ");
        fout.printtab("Correlation");
        fout.printtab("Gradient");
        fout.printtab("Intercept");
        fout.printtab("mu");
        fout.println("sigma");
   
        fout.printtab("       ");
        fout.printtab("coefficient");
        fout.printtab("       ");
        fout.printtab("       ");
        fout.printtab("       ");
        fout.println("     ");
        
        for(int i=0; i<this.nGroups; i++){
            fout.printtab(this.groupNames[i]);
            fout.printtab(Fmath.truncate(this.groupProbPlotR[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupProbPlotGradient[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupProbPlotIntercept[i], this.trunc));
            fout.printtab(Fmath.truncate(this.groupProbPlotMu[i], this.trunc));
            fout.println(Fmath.truncate(this.groupProbPlotSigma[i], this.trunc));
        }
        fout.println(" ");
        fout.printtab("total");
        fout.printtab(Fmath.truncate(this.totalProbPlotR, this.trunc));
        fout.printtab(Fmath.truncate(this.totalProbPlotGradient, this.trunc));
        fout.printtab(Fmath.truncate(this.totalProbPlotIntercept, this.trunc));
        fout.printtab(Fmath.truncate(this.totalProbPlotMu, this.trunc));
        fout.println(Fmath.truncate(this.totalProbPlotSigma, this.trunc));
        
        
        if(this.nGroups>2 && this.comparison){
            fout.println(" ");
            fout.println(" ");
            fout.println("COMPARISON OF PAIRS OF GROUPS: One-Way Analysis of Variance");
            int counter = 0;
            for(int i=0; i<this.nGroups-1; i++){
                for(int j=i+1; j<this.nGroups; j++){
                    if(this.compGroups[i] && this.compGroups[j]){
                        fout.println("Group"+(i+1)+" and Group"+(j+1));
                        fout.printtab(" ");
                        fout.printtab(" ");
                        fout.printtab("  ");
                        fout.printtab(" ");
                        fout.printtab(" ");
                        fout.printtab("F-ratio at");
                        fout.println(" ");
        
                        fout.printtab(" ");
                        fout.printtab("Sum of");
                        fout.printtab("Degrees of ");
                        fout.printtab("Mean ");
                        fout.printtab(" ");
                        fout.printtab(this.criticalSignificanceF + " critical");
                        fout.println(" ");
        
                        fout.printtab(" ");
                        fout.printtab("squares");
                        fout.printtab("freedom ");
                        fout.printtab("square ");
                        fout.printtab("F-ratio ");
                        fout.printtab("level");
                        fout.println("Significance");
                        
                        fout.printtab("Between groups");
                        fout.printtab(Fmath.truncate(this.ssBetweenG[counter], this.trunc));
                        fout.printtab(this.dofBetweenG[counter]);
                        fout.printtab(Fmath.truncate(this.meanSquareBetweenG[counter], this.trunc));
                        fout.printtab(Fmath.truncate(this.fRatioG[counter], this.trunc));
                        fout.printtab(Fmath.truncate(this.criticalFratioG[counter], this.trunc));
                        fout.println(Fmath.truncate(this.fRatioPG[counter], this.trunc));
                        
                        fout.printtab("Within groups");
                        fout.printtab(Fmath.truncate(this.ssWithinG[counter], this.trunc));
                        fout.printtab(this.dofWithinG[counter]);
                        fout.println(Fmath.truncate(this.meanSquareWithinG[counter], this.trunc));
                        
                        fout.printtab("Total");
                        fout.printtab(Fmath.truncate(this.ssTotalG[counter], this.trunc));
                        fout.printtab(this.dofTotalG[counter], this.fieldD);
                        fout.println(Fmath.truncate(this.meanSquareTotalG[counter], this.trunc));
                        counter++;
                        fout.println();
                        fout.println();
                    }
                }
            }
        }
        fout.close();
    }

    // Set number of decimal places in the output data
    // default value = 6
    // trunc is overriden by the precision of the input data if this is greater than trunc
    public void numberOfDecimalPlaces(int trunc){
        this.trunc = trunc;
    }

    // Set number of decimal places in the output data
    // default value = 6
    // trunc is NOT overriden by the precision of the input data if this is greater than trunc
    public void numberOfDecimalPlacesAll(int trunc){
        this.trunc = trunc;
        this.truncAll = true;
    }
 }






