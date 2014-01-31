/*
*   CLASS:      Cronbach
*
*   USAGE:      Cronbach raw data alpha reliability
*               Cronbach standardized data alpha reliability
*               Correlation between items
*               Deletion of items
*
*   This is a subclass of the superclass Scores
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       July and Agust 2008
*   AMENDED:    22 August 2008, 29 August 2008, 1-8 October 2008, 1-4 November 2010
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Cronbach.html
*
*   Copyright (c) 2008 Michael Thomas Flanagan
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
import java.text.*;

import flanagan.math.Fmath;
import flanagan.math.ArrayMaths;
import flanagan.io.Db;
import flanagan.io.FileOutput;
import flanagan.analysis.Scores;
import flanagan.analysis.Stat;
import flanagan.plot.PlotGraph;
import flanagan.plot.Plot;


public class Cronbach extends Scores{

    private double rawAlpha = Double.NaN;                               // Cronbach raw data alpha reliability coefficient
    private boolean rawAlphaCalculated = false;                         // = true when raw data alpha reliability coefficient calculated
    private double standardizedAlpha = Double.NaN;                      // Cronbach standardized data alpha reliability coefficient
    private boolean standardizedAlphaCalculated = false;                // = true when standardized alpha reliability coefficient calculated

    private int deletedItemIndex = -1;                                  // the index of the least consistent item
    private String deletedFilename = null;                              // File name of new data file with least consistent item deleted


    // CONSTRUCTOR
    public Cronbach(){
        super();
    }

    // CRONBACH RAW DATA ALPHA
    public double rawAlpha(){

        if(!this.rawAlphaCalculated){

            if(this.nItems==1){
                System.out.println("Method rawAlpha: only one item - alpha cannot be calculated - NaN returned");
                this.rawAlpha = Double.NaN;
            }
            else{

                // Check that data is preprocessed
                if(!this.dataPreprocessed)this.preprocessData();

                // (Sum of scores1) squared
                double rawAllResponsesTotalAllSquared = this.rawAllResponsesTotal*this.rawAllResponsesTotal;

                // Sum of (scores1 squared)
                double sumOfEachScoreSquared = 0.0;
                for(int i=0;i<this.nItems;i++){
                    for(int j=0;j<this.nPersons;j++)sumOfEachScoreSquared += scores1[j][i]*scores1[j][i];
                }


                // Reduced sum of column totals squared
                double reducedItemTotalsSquared = 0.0;
                for(int i=0;i<this.nItems;i++)reducedItemTotalsSquared += rawItemTotals[i]*rawItemTotals[i]/this.nPersons;

                // Reduced sum of row totals squared
                double reducedPersonTotalsSquared = 0.0;
                for(int i=0;i<this.nPersons;i++)reducedPersonTotalsSquared += rawPersonTotals[i]*rawPersonTotals[i]/this.nItems;

                //Sum of squares within persons
                double sumOfSquaresWithinPersons = reducedPersonTotalsSquared - rawAllResponsesTotalAllSquared/this.nScores;

                //Sum of square errors
                double sumOfSquareErrors = sumOfEachScoreSquared - reducedItemTotalsSquared - reducedPersonTotalsSquared + rawAllResponsesTotalAllSquared/this.nScores;

                //Degrees of freedom
                //iiems
                int dfItems = this.nItems- 1;
                //persons
                int dfPersons = this.nPersons - 1;
                // errors
                int dfErrors = dfItems*dfPersons;

                // Mean Squares
                double reducedSquarePersons = sumOfSquaresWithinPersons/dfPersons;
                double reducedSquareErrors = sumOfSquareErrors/dfErrors;

                // Cronbach raw alpha reliability coefficient
                this.rawAlpha  = (reducedSquarePersons - reducedSquareErrors)/reducedSquarePersons;
                this.rawAlphaCalculated = true;
            }
        }
        return  this.rawAlpha;
    }



    // CRONBACH STANDARDIZED DATA ALPHA
    public double standardizedAlpha(){

        if(!this.standardizedAlphaCalculated){

            if(this.nItems==1){
                System.out.println("Method standardizedAlpha: only one item - alpha cannot be calculated - NaN returned");
                this.rawAlpha = Double.NaN;
            }
            else{

                // Check that data is preprocessed
                if(!this.dataPreprocessed)this.preprocessData();

                // Calculate correlation coefficients
                if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();

                // Cronbach standardized alpha reliability coefficient
                this.standardizedAlpha = this.nItems*this.rawMeanRhoWithoutTotals/(1.0 +  (this.nItems - 1)*this.rawMeanRhoWithoutTotals);
                this.standardizedAlphaCalculated = true;
            }
        }

        return this.standardizedAlpha;
    }

    public double standardisedAlpha(){
        return this.standardizedAlpha();
    }





    // OUTPUT THE ANALYSIS

    // Full analysis without output of input data
    // no input file name entered via method argument list
    public void analysis(){

        this.outputFilename = "CronbachOutput";
        if(this.fileOption==1){
            this.outputFilename += ".txt";
        }
        else{
            this.outputFilename += ".xls";
        }
        String message1 = "Output file name for the analysis details:";
        String message2 = "\nEnter the required name (as a single word) and click OK ";
        String message3 = "\nor simply click OK for default value";
        String message = message1 + message2 + message3;
        String defaultName = this.outputFilename;
        this.outputFilename = Db.readLine(message, defaultName);
        this.analysis(this.outputFilename);
    }

    // Full analysis without output of input data
    // input file name via method argument list
    public void analysis(String filename){
        // Open output file
        this.outputFilename = filename;
        String outputFilenameWithoutExtension = null;
        String extension = null;
        int pos = filename.indexOf('.');
        if(pos==-1){
            outputFilenameWithoutExtension = filename;
            if(this.fileOption==1){
                this.outputFilename += ".txt";
            }
            else{
                this.outputFilename += ".xls";
            }
        }
        else{
            extension = (filename.substring(pos)).trim();

            outputFilenameWithoutExtension = (filename.substring(0, pos)).trim();
            if(extension.equalsIgnoreCase(".xls")){
                if(this.fileOption==1){
                    if(this.fileOptionSet){
                        String message1 = "Your entered output file type is .xls";
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

        if(this.fileOption==1){
            this.analysisText();
        }
        else{
            this.analysisExcel();
        }
    }

    private void analysisExcel(){

        FileOutput fout = null;
        if(this.fileNumberingSet){
            fout = new FileOutput(this.outputFilename, 'n');
        }
        else{
            fout = new FileOutput(this.outputFilename);
        }

        // calculate alphas if not already calculated
        if(!rawAlphaCalculated)this.rawAlpha();
        if(!standardizedAlphaCalculated)this.standardizedAlpha();

        // output title information

        fout.println("CRONBACH'S ALPHA RELIABILITY ESTIMATOR");
        fout.println("Program: Cronbach - Analysis Output");
        for(int i=0; i<this.titleLines; i++)fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();

        // output reliability estimators
        fout.println("RELIABILITY ESTIMATORS");
        fout.println("Cronbach's coefficient alpha");
        fout.printtab("Raw data                  ");
        fout.println(Fmath.truncate(this.rawAlpha, this.trunc));
        fout.printtab("Standardized data           ");
        fout.println(Fmath.truncate(this.standardizedAlpha, this.trunc));
        fout.println();

        fout.println("Average of the inter-item correlation coefficients, excluding item totals");
        fout.printtab("Raw data                  ");
        fout.println(Fmath.truncate(this.rawMeanRhoWithoutTotals, this.trunc));
        fout.printtab("Standardized data           ");
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, this.trunc));
        fout.println();

        fout.println("Average of the inter-item correlation coefficients, including item totals");
        fout.printtab("Raw data                  ");
        fout.println(Fmath.truncate(this.rawMeanRhoWithTotals, this.trunc));
        fout.printtab("Standardized data           ");
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, this.trunc));
        fout.println();

        // output any deletions or replacements
        fout.println("'NO RESPONSE' DELETIONS AND REPLACEMENTS");
        // deleted persons
        boolean deletionFlag = false;
        if(this.nDeletedPersons!=0){
            deletionFlag = true;
            fout.printtab("Number of persons deleted ");
            fout.println(this.nDeletedPersons);
            fout.printtab("Indices of deleted persons: ");
            for(int i=0; i<this.nDeletedPersons; i++)fout.printtab(this.deletedPersonsIndices[i]+1);
            fout.println();
        }
        else{
            fout.println("No persons were deleted ");
        }

        // deleted items
        if(this.nDeletedItems!=0){
            deletionFlag = true;
            fout.printtab("Number of items deleted ");
            fout.println(this.nDeletedItems);
            fout.printtab("Names of deleted items: ");
            for(int i=0; i<this.nDeletedItems; i++)fout.printtab(this.originalItemNames[this.deletedItemsIndices[i]]);
            fout.println();
        }
        else{
            fout.println("No items were deleted ");
        }

        // replacements
        if(this.nReplacements!=0){
            fout.printtab("Number of 'no responses' replaced ");
            fout.println(this.nReplacements);
            fout.printtab("Item name and person index of replacements: ");
            for(int i=0; i<this.nReplacements; i++)fout.printtab(this.replacementIndices[i] + " ");
            fout.println();
            fout.printtab("Replacement option: ");
            fout.println(this.replacementOptionNames[this.replacementOption-1]);
            fout.println();
        }
        else{
            if(deletionFlag){
                fout.println("No 'no response' replacements, other than any above deletions, were made ");
            }
            else{
                fout.println("No 'no response' replacements were made ");
            }
        }
        fout.println();
        fout.printtab("Number of items used         ");
        fout.println(this.nItems);
        fout.printtab("Number of persons used   ");
        fout.println(this.nPersons);
        fout.println();

        // Correlation coefficients
        fout.println("CORRELATION COEFFICIENTS");
        fout.println("Correlation coefficients between items  -  raw data");
        fout.printtab("    ");
        for(int i=0; i<=this.nItems; i++)fout.printtab(this.itemNames[i]);
        fout.println();
        for(int i=0; i<=this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            for(int j=0; j<=this.nItems; j++)fout.printtab(Fmath.truncate(this.rawCorrelationCoefficients[i][j], this.trunc));
            fout.println();
        }
        fout.println();

        fout.print("Average inter-item correlation coefficient (excluding total)                    ");
        fout.println(Fmath.truncate(this.rawMeanRhoWithoutTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (excluding total)  ");
        fout.println(Fmath.truncate(this.rawStandardDeviationRhoWithoutTotals, this.trunc));
        fout.print("Average inter-item correlation coefficient (including total)                    ");
        fout.println(Fmath.truncate(this.rawMeanRhoWithTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (including total)  ");
        fout.println(Fmath.truncate(this.rawStandardDeviationRhoWithTotals, this.trunc));
        fout.println();


        fout.println("Correlation coefficients between items  -  standardized data");
        fout.printtab("    ");
        for(int i=0; i<=this.nItems; i++)fout.printtab(this.itemNames[i]);
        fout.println();
        for(int i=0; i<=this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            for(int j=0; j<=this.nItems; j++)fout.printtab(Fmath.truncate(this.standardizedCorrelationCoefficients[i][j], this.trunc));
            fout.println();
        }
        fout.println();

        fout.print("Average inter-item correlation coefficient (excluding total)                    ");
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (excluding total)  ");
        fout.println(Fmath.truncate(this.standardizedStandardDeviationRhoWithoutTotals, this.trunc));
        fout.print("Average inter-item correlation coefficient (including total)                    ");
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (including total)  ");
        fout.println(Fmath.truncate(this.standardizedStandardDeviationRhoWithTotals, this.trunc));
        fout.println();


        // Item statistics
        fout.println("ITEMS: MEANS, STANDARD DEVIATIONS, SKEWNESS AND KURTOSIS");
        fout.println("Raw data");
        fout.printtab("item ");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("moment");
        fout.printtab("median");
        fout.printtab("quartile");
        fout.printtab("kurtosis");
        fout.println("dichotomous");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.printtab("excess  ");
        fout.println("percentage");

        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            fout.printtab(Fmath.truncate(this.rawItemMeans[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemStandardDeviations[i], this.trunc));
           fout.printtab(Fmath.truncate(this.rawItemMomentSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemMedianSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemQuartileSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemKurtosisExcess[i], this.trunc));
            fout.println(Fmath.truncate(this.dichotomousPercentage[i], 1));
        }
        fout.println();

      fout.println("ITEMS: MINIMA, MAXIMA, MEDIANS, RANGES AND TOTALS");
        fout.println("raw data");
        fout.printtab("item ");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("median");
        fout.printtab("range");
        fout.printtab("total");
        fout.println("dichotomous");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("percentage");

        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            fout.printtab(Fmath.truncate(this.rawItemMinima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemMaxima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemMedians[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemRanges[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawItemTotals[i], this.trunc));
            fout.println(Fmath.truncate(this.dichotomousPercentage[i], 1));
        }
        fout.println();

        fout.printtab("item");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("variance");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.println("range");
        fout.printtab("statistic    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("     ");

        fout.printtab("item means");
        fout.printtab(Fmath.truncate(this.rawItemMeansMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMeansSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMeansVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMeansMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMeansMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemMeansRange, this.trunc));

        fout.printtab("item standard deviations");
        fout.printtab(Fmath.truncate(this.rawItemStandardDeviationsMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemStandardDeviationsSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemStandardDeviationsVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemStandardDeviationsMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemStandardDeviationsMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemStandardDeviationsRange, this.trunc));

        fout.printtab("item variances");
        fout.printtab(Fmath.truncate(this.rawItemVariancesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemVariancesSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemVariancesVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemVariancesMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemVariancesMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemVariancesRange, this.trunc));

        fout.printtab("item mimima");
        fout.printtab(Fmath.truncate(this.rawItemMinimaMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMinimaSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMinimaVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMinimaMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMinimaMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemMinimaRange, this.trunc));

        fout.printtab("item maxima");
        fout.printtab(Fmath.truncate(this.rawItemMaximaMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMaximaSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMaximaVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMaximaMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMaximaMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemMaximaRange, this.trunc));

        fout.printtab("item medians");
        fout.printtab(Fmath.truncate(this.rawItemMediansMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMediansSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMediansVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMediansMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemMediansMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemMediansRange, this.trunc));

        fout.printtab("item ranges");
        fout.printtab(Fmath.truncate(this.rawItemRangesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemRangesSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemRangesVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemRangesMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemRangesMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemRangesRange, this.trunc));

        fout.printtab("item totals");
        fout.printtab(Fmath.truncate(this.rawItemTotalsMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemTotalsSd, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemTotalsVar, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemTotalsMin, this.trunc));
        fout.printtab(Fmath.truncate(this.rawItemTotalsMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemTotalsRange, this.trunc));

        fout.println();

        fout.println("Standardized data");
        fout.println("ITEMS: MEANS, STANDARD DEVIATIONS, SKEWNESS AND KURTOSIS");
        fout.printtab("item ");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("moment");
        fout.printtab("median");
        fout.printtab("quartile");
        fout.println("kurtosis");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.printtab("skewness");
        fout.println("excess  ");

        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            fout.printtab(Fmath.truncate(this.standardizedItemMeans[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviations[i], this.trunc));
           fout.printtab(Fmath.truncate(this.standardizedItemMomentSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemMedianSkewness[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemQuartileSkewness[i], this.trunc));
            fout.println(Fmath.truncate(this.standardizedItemKurtosisExcess[i], this.trunc));
        }
        fout.println();

      fout.println("ITEMS: MINIMA, MAXIMA, MEDIANS, RANGES AND TOTALS");
        fout.println("Standardized data");
        fout.printtab("item ");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("median");
        fout.printtab("range");
        fout.println("total");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("     ");

        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
            fout.printtab(Fmath.truncate(this.standardizedItemMinima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemMaxima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemMedians[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedItemRanges[i], this.trunc));
            fout.println(Fmath.truncate(this.standardizedItemTotals[i], this.trunc));
        }
        fout.println();



        fout.printtab("item");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("variance");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.println("range");

        fout.printtab("statistic    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("     ");

        fout.printtab("item means");
        fout.printtab(Fmath.truncate(this.standardizedItemMeansMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMeansSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMeansVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMeansMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMeansMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemMeansRange, this.trunc));

        fout.printtab("item standard deviations");
        fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviationsMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviationsSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviationsVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviationsMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemStandardDeviationsMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemStandardDeviationsRange, this.trunc));

        fout.printtab("item variances");
        fout.printtab(Fmath.truncate(this.standardizedItemVariancesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemVariancesSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemVariancesVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemVariancesMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemVariancesMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemVariancesRange, this.trunc));

        fout.printtab("item mimima");
        fout.printtab(Fmath.truncate(this.standardizedItemMinimaMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMinimaSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMinimaVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMinimaMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMinimaMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemMinimaRange, this.trunc));

        fout.printtab("item maxima");
        fout.printtab(Fmath.truncate(this.standardizedItemMaximaMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMaximaSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMaximaVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMaximaMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemMaximaMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemMaximaRange, this.trunc));

        fout.print("item medians");
        fout.print(Fmath.truncate(this.rawItemMediansMean, this.trunc));
        fout.print(Fmath.truncate(this.rawItemMediansSd, this.trunc));
        fout.print(Fmath.truncate(this.rawItemMediansVar, this.trunc));
        fout.print(Fmath.truncate(this.rawItemMediansMin, this.trunc));
        fout.print(Fmath.truncate(this.rawItemMediansMax, this.trunc));
        fout.println(Fmath.truncate(this.rawItemMediansRange, this.trunc));

        fout.printtab("item ranges");
        fout.printtab(Fmath.truncate(this.standardizedItemRangesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemRangesSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemRangesVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemRangesMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemRangesMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemRangesRange, this.trunc));

        fout.printtab("item totals");
        fout.printtab(Fmath.truncate(this.standardizedItemTotalsMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemTotalsSd, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemTotalsVar, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemTotalsMin, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedItemTotalsMax, this.trunc));
        fout.println(Fmath.truncate(this.standardizedItemTotalsRange, this.trunc));

        fout.println();

        fout.println("DELETION OF ITEMS");

        fout.printtab("                ");
        fout.printtab("Raw data        ");
        fout.printtab("                ");
        fout.printtab("                ");
        fout.printtab("                ");
        fout.println("Standardized data");

        fout.printtab("Deleted item");
        fout.printtab("Alpha       ");
        fout.printtab("Correlation ");
        fout.printtab("Average     ");
        fout.printtab("Average     ");
        fout.printtab("Alpha       ");
        fout.printtab("Correlation ");
        fout.printtab("Average     ");
        fout.println("Average     ");


        fout.printtab("           ");
        fout.printtab("           ");
        fout.printtab("coefficient");
        fout.printtab("inter-item ");
        fout.printtab("inter-item ");
        fout.printtab("           ");
        fout.printtab("coefficient ");
        fout.printtab("inter-item ");
        fout.println("inter-item ");


        fout.printtab("           ");
        fout.printtab("           ");
        fout.printtab("with total ");
        fout.printtab("correlation");
        fout.printtab("correlation");
        fout.printtab("           ");
        fout.printtab("with total ");
        fout.printtab("correlation");
        fout.println("correlation");


        fout.printtab("           ");
        fout.printtab("           ");
        fout.printtab("           ");
        fout.printtab("coefficient");
        fout.printtab("coefficient");
        fout.printtab("           ");
        fout.printtab("           ");
        fout.printtab("coefficient");
        fout.println("coefficient");


        fout.printtab("              ");
        fout.printtab("              ");
        fout.printtab("              ");
        fout.printtab("without totals");
        fout.printtab("with totals   ");
        fout.printtab("              ");
        fout.printtab("              ");
        fout.printtab("without totals");
        fout.println("with totals   ");

        double[] newRawAlpha = new double[this.nItems];
        double[] newStandardizedAlpha = new double[this.nItems];
        double[] newRawRho = new double[this.nItems];
        double[] newStandardizedRho = new double[this.nItems];
        for(int i=0; i<this.nItems; i++){
            int index = i+1;
            double[][] newScore1 = this.deleteItem(index);
            Cronbach cr = new Cronbach();
            cr.enterScoresAsRowPerPerson(newScore1);
            double rawAlphaD = cr.rawAlpha();
            newRawAlpha[i] = rawAlphaD;
            double rawMeanRhoWithTotalsD = cr.rawAverageCorrelationCoefficientsWithTotals();
            double rawMeanRhoWithoutTotalsD = cr.rawAverageCorrelationCoefficients();
            double[] rawPersonTotalsD = cr.rawPersonTotals();
            double rawRhoAgainstTotalsD = Stat.corrCoeff(rawPersonTotalsD, this.scores0[i]);
            newRawRho[i] = rawRhoAgainstTotalsD;

            double standardizedAlphaD = cr.standardizedAlpha();
            newStandardizedAlpha[i] = standardizedAlphaD;
            double standardizedMeanRhoWithTotalsD = cr.standardizedAverageCorrelationCoefficientsWithTotals();
            double standardizedMeanRhoWithoutTotalsD = cr.standardizedAverageCorrelationCoefficients();
            double[] standardizedPersonTotalsD = cr.standardizedPersonTotals();
            double standardizedRhoAgainstTotalsD = Stat.corrCoeff(standardizedPersonTotalsD, this.scores0[i]);
            newStandardizedRho[i] = standardizedRhoAgainstTotalsD;

            fout.printtab(this.itemNames[i]);
            fout.printtab(Fmath.truncate(rawAlphaD, trunc));
            fout.printtab(Fmath.truncate(rawRhoAgainstTotalsD, trunc));
            fout.printtab(Fmath.truncate(rawMeanRhoWithoutTotalsD, trunc));
            fout.printtab(Fmath.truncate(rawMeanRhoWithTotalsD, trunc));

            fout.printtab(Fmath.truncate(standardizedAlphaD, trunc));
            fout.printtab(Fmath.truncate(standardizedRhoAgainstTotalsD, trunc));
            fout.printtab(Fmath.truncate(standardizedMeanRhoWithoutTotalsD, trunc));
            fout.println(Fmath.truncate(standardizedMeanRhoWithTotalsD, trunc));
        }
        fout.println();

        fout.printtab("No item deleted");
        fout.printtab(Fmath.truncate(this.rawAlpha, trunc));
        fout.printtab("   ");
        fout.printtab(Fmath.truncate(this.rawMeanRhoWithoutTotals, trunc));
        fout.printtab(Fmath.truncate(this.rawMeanRhoWithTotals, trunc));

        fout.printtab(Fmath.truncate(this.standardizedAlpha, trunc));
        fout.printtab("   ");
        fout.printtab(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, trunc));
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, trunc));
        fout.println();

        // output a deleted item data file
        this.deletedItemDataFile(newRawAlpha, newRawRho, newStandardizedAlpha, newStandardizedRho);


        fout.println("INDIVIDUALS - raw data");
        fout.printtab("person ");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("range");
        fout.printtab("total");
        fout.println("scores:");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        for(int i=0; i<this.nItems; i++)fout.printtab(this.itemNames[i]);
        fout.println();

        for(int i=0; i<this.nPersons; i++){
            fout.printtab((this.personIndices[i]+1));
            fout.printtab(Fmath.truncate(this.rawPersonMeans[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawPersonStandardDeviations[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawPersonMinima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawPersonMaxima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawPersonRanges[i], this.trunc));
            fout.printtab(Fmath.truncate(this.rawPersonTotals[i], this.trunc));
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.scores1[i][j], this.trunc));
            fout.println();
        }
        fout.println();

        fout.println("INDIVIDUALS - standardized data");
        fout.printtab("person ");
        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("range");
        fout.printtab("total");
        fout.println("scores:");

        fout.printtab("    ");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        for(int i=0; i<this.nItems; i++)fout.printtab(this.itemNames[i]);
        fout.println();


        for(int i=0; i<this.nPersons; i++){
            fout.printtab((this.personIndices[i]+1));
            fout.printtab(Fmath.truncate(this.standardizedPersonMeans[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedPersonStandardDeviations[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedPersonMinima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedPersonMaxima[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedPersonRanges[i], this.trunc));
            fout.printtab(Fmath.truncate(this.standardizedPersonTotals[i], this.trunc));
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.standardizedScores1[i][j], this.trunc));
            fout.println();
        }
        fout.println();

        fout.println("ALL SCORES - raw data");

        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("range");
        fout.println("overall");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("total");

        fout.printtab(Fmath.truncate(this.rawAllResponsesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.rawAllResponsesStandardDeviation, this.trunc));
        fout.printtab(Fmath.truncate(this.rawAllResponsesMinimum, this.trunc));
        fout.printtab(Fmath.truncate(this.rawAllResponsesMaximum, this.trunc));
        fout.printtab(Fmath.truncate(this.rawAllResponsesRange, this.trunc));
        fout.println(Fmath.truncate(this.rawAllResponsesTotal, this.trunc));
        fout.println();

        fout.println("ALL SCORES - standardized data");

        fout.printtab("mean");
        fout.printtab("standard");
        fout.printtab("minimum");
        fout.printtab("maximum");
        fout.printtab("range");
        fout.println("overall");
        fout.printtab("    ");
        fout.printtab("deviation");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.printtab("     ");
        fout.println("total");

        fout.printtab(Fmath.truncate(this.standardizedAllResponsesMean, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedAllResponsesStandardDeviation, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedAllResponsesMinimum, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedAllResponsesMaximum, this.trunc));
        fout.printtab(Fmath.truncate(this.standardizedAllResponsesRange, this.trunc));
        fout.println(Fmath.truncate(this.standardizedAllResponsesTotal, this.trunc));
        fout.println();

        // close output file
        fout.close();
    }


    private void analysisText(){

        FileOutput fout = null;
        if(this.fileNumberingSet){
            fout = new FileOutput(this.outputFilename, 'n');
        }
        else{
            fout = new FileOutput(this.outputFilename);
        }

        // calculate alphas if not already calculated
        if(!rawAlphaCalculated)this.rawAlpha();
        if(!standardizedAlphaCalculated)this.standardizedAlpha();

        // output title information
        fout.println("CRONBACH'S ALPHA RELIABILITY ESTIMATOR");
        fout.println("Program: Cronbach - Analysis Output");
        for(int i=0; i<this.titleLines; i++)fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();

        // output reliability estimators
        int field = 36;     // field width
        fout.println("RELIABILITY ESTIMATORS");
        fout.println("Cronbach's coefficient alpha");
        fout.print("Raw data ", field);
        fout.println(Fmath.truncate(this.rawAlpha, this.trunc));
        fout.print("Standardized data ", field);
        fout.println(Fmath.truncate(this.standardizedAlpha, this.trunc));
        fout.println();

        fout.println("Average of the inter-item correlation coefficients, excluding item totals");
        fout.print("Raw data ", field);
        fout.println(Fmath.truncate(this.rawMeanRhoWithoutTotals, this.trunc));
        fout.print("Standardized data ", field);
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, this.trunc));
        fout.println();

        fout.println("Average of the inter-item correlation coefficients, including item totals");
        fout.print("Raw data ", field);
        fout.println(Fmath.truncate(this.rawMeanRhoWithTotals, this.trunc));
        fout.print("Standardized data ", field);
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, this.trunc));
        fout.println();

        // output any deletions or replacements
        fout.println("'NO RESPONSE' DELETIONS AND REPLACEMENTS");
        // deleted persons
        field = 34;
        int fieldInt = 6;
        boolean deletionFlag = false;
        if(this.nDeletedPersons!=0){
            deletionFlag = true;
            fout.print("Number of persons deleted ", field);
            fout.println(this.nDeletedPersons);
            fout.print("Indices of deleted persons: ", field);
            for(int i=0; i<this.nDeletedPersons; i++)fout.print((this.deletedPersonsIndices[i]+1), fieldInt);
            fout.println();
        }
        else{
            fout.println("No persons were deleted ");
        }

        // deleted items
        if(this.nDeletedItems!=0){
            deletionFlag = true;
            fout.print("Number of items deleted ", field);
            fout.println(this.nDeletedItems);
            fout.print("Names of deleted items: ", field);
            for(int i=0; i<this.nDeletedItems; i++)fout.print(this.originalItemNames[this.deletedItemsIndices[i]], fieldInt);
            fout.println();
        }
        else{
            fout.println("No items were deleted ");
        }

        // replacements
        if(this.nReplacements!=0){
            fout.printtab("Number of 'no responses' replaced ");
            fout.println(this.nReplacements);
            fout.print("Item name and person index of replacements: ", 50);
            for(int i=0; i<this.nReplacements; i++)fout.print((this.replacementIndices[i]+" "), fieldInt);
            fout.println();
            fout.print("Replacement option: ", field);
            fout.println(this.replacementOptionNames[this.replacementOption-1]);
            fout.println();
        }
        else{
            if(deletionFlag){
                fout.println("No 'no response' replacements, other than any above deletions, were made ");
            }
            else{
                fout.println("No 'no response' replacements were made ");
            }
        }
        fout.println();
        fout.print("Number of items used", 35);
        fout.println(this.nItems);
        fout.print("Number of persons used", 35);
        fout.println(this.nPersons);
        fout.println();

        // Correlation coefficients
        int len = this.trunc+8;
        int fieldItemName = 0;
        for(int i=0; i<=this.nItems; i++)if(this.itemNames[i].length()>fieldItemName)fieldItemName = this.itemNames[i].length();
        int fieldItemNumber = fieldItemName;
        if(len>fieldItemNumber)fieldItemNumber = len;
        fieldItemName++;
        fieldItemNumber++;

        fout.println("CORRELATION COEFFICIENTS");
        fout.println("Correlation coefficients between items  -  raw data");
        fout.print("    ", fieldItemName);

        for(int i=0; i<=this.nItems; i++)fout.print(this.itemNames[i], fieldItemNumber);
        fout.println();
        for(int i=0; i<=this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            for(int j=0; j<=this.nItems; j++)fout.print(Fmath.truncate(this.rawCorrelationCoefficients[i][j], this.trunc), fieldItemNumber);
            fout.println();
        }
        fout.println();

        fout.print("Average inter-item correlation coefficient (excluding total) ", 80);
        fout.println(Fmath.truncate(this.rawMeanRhoWithoutTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (excluding total) ", 80);
        fout.println(Fmath.truncate(this.rawStandardDeviationRhoWithoutTotals, this.trunc));
        fout.print("Average inter-item correlation coefficient (including total) ", 80);
        fout.println(Fmath.truncate(this.rawMeanRhoWithTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (including total) ", 80);
        fout.println(Fmath.truncate(this.rawStandardDeviationRhoWithTotals, this.trunc));

        fout.println();


        fout.println("Correlation coefficients between items  -  standardized data");
        fout.print("    ", fieldItemName);
        for(int i=0; i<=this.nItems; i++)fout.print(this.itemNames[i], fieldItemNumber);
        fout.println();
        for(int i=0; i<=this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            for(int j=0; j<=this.nItems; j++)fout.print(Fmath.truncate(this.standardizedCorrelationCoefficients[i][j], this.trunc), fieldItemNumber);
            fout.println();
        }
        fout.println();

        fout.print("Average inter-item correlation coefficient (excluding total) ", 80);
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (excluding total) ", 80);
        fout.println(Fmath.truncate(this.standardizedStandardDeviationRhoWithoutTotals, this.trunc));
        fout.print("Average inter-item correlation coefficient (including total) ", 80);
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, this.trunc));
        fout.print("Standard deviation of the inter-item correlation coefficient (including total) ", 80);
        fout.println(Fmath.truncate(this.standardizedStandardDeviationRhoWithTotals, this.trunc));
        fout.println();

        // item statistics
        if(fieldItemNumber<12)fieldItemNumber = 12;

        fout.println("ITEMS: MEANS, STANDARD DEVIATIONS, SKEWNESS AND KURTOSIS");
        fout.println("Raw data");
        fout.print("item ", fieldItemName);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("moment", fieldItemNumber);
        fout.print("median", fieldItemNumber);
        fout.print("quartile", fieldItemNumber);
        fout.print("kurtosis", fieldItemNumber);
        fout.println("dichotomous");

        fout.print("    ", fieldItemName);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.print("excess  ", fieldItemNumber);
        fout.println("percentage");

        for(int i=0; i<this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            fout.print(Fmath.truncate(this.rawItemMeans[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemStandardDeviations[i], this.trunc), fieldItemNumber);
           fout.print(Fmath.truncate(this.rawItemMomentSkewness[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemMedianSkewness[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemQuartileSkewness[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemKurtosisExcess[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.dichotomousPercentage[i], 1));
        }
        fout.println();

      fout.println("ITEMS: MINIMA, MAXIMA, MEDIANS, RANGES AND TOTALS");
        fout.println("Raw data");
        fout.print("item ", fieldItemName);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("median", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.print("total", fieldItemNumber);
        fout.println("dichotomous");

        fout.print("    ", fieldItemName);
        fout.print("    ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("percentage");

        for(int i=0; i<this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            fout.print(Fmath.truncate(this.rawItemMinima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemMaxima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemMedians[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemRanges[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawItemTotals[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.dichotomousPercentage[i], 1));
        }
        fout.println();

        int fieldItemSName = 25;
        fout.print("item", fieldItemSName);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("variance", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.println("range");
        fout.print("statistic    ", fieldItemSName);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("     ");

        fout.print("item means", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemMeansMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMeansSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMeansVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMeansMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMeansMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemMeansRange, this.trunc));

        fout.print("item standard deviations", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemStandardDeviationsMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemStandardDeviationsSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemStandardDeviationsVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemStandardDeviationsMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemStandardDeviationsMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemStandardDeviationsRange, this.trunc));

        fout.print("item variances", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemVariancesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemVariancesSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemVariancesVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemVariancesMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemVariancesMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemVariancesRange, this.trunc));

        fout.print("item mimima", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemMinimaMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMinimaSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMinimaVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMinimaMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMinimaMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemMinimaRange, this.trunc));

        fout.print("item maxima", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemMaximaMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMaximaSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMaximaVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMaximaMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMaximaMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemMaximaRange, this.trunc));

        fout.print("item medians", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemMediansMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMediansSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMediansVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMediansMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemMediansMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemMediansRange, this.trunc));

        fout.print("item ranges", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemRangesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemRangesSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemRangesVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemRangesMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemRangesMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemRangesRange, this.trunc));

        fout.print("item totals", fieldItemSName);
        fout.print(Fmath.truncate(this.rawItemTotalsMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemTotalsSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemTotalsVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemTotalsMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawItemTotalsMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawItemTotalsRange, this.trunc));

        fout.println();

        fout.println("standardized data");
                fout.print("item ", fieldItemName);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("moment", fieldItemNumber);
        fout.print("median", fieldItemNumber);
        fout.print("quartile", fieldItemNumber);
        fout.println("kurtosis");

        fout.print("    ", fieldItemName);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.print("skewness", fieldItemNumber);
        fout.println("excess  ");

        for(int i=0; i<this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            fout.print(Fmath.truncate(this.standardizedItemMeans[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemStandardDeviations[i], this.trunc), fieldItemNumber);
           fout.print(Fmath.truncate(this.standardizedItemMomentSkewness[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemMedianSkewness[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemQuartileSkewness[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.standardizedItemKurtosisExcess[i], this.trunc));
        }
        fout.println();

        fout.print("item ", fieldItemName);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("median", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.println("total");

        fout.print("    ", fieldItemName);
        fout.print("    ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("     ");

        for(int i=0; i<this.nItems; i++){
            fout.print(this.itemNames[i], fieldItemName);
            fout.print(Fmath.truncate(this.standardizedItemMinima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemMaxima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemMedians[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedItemRanges[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.standardizedItemTotals[i], this.trunc));
        }
        fout.println();


        fout.print("item", fieldItemSName);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("variance", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.println("range");
        fout.print("statistic    ", fieldItemSName);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("     ");

        fout.print("item means", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemMeansMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMeansSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMeansVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMeansMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMeansMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemMeansRange, this.trunc));

        fout.print("item standard deviations", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemStandardDeviationsMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemStandardDeviationsSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemStandardDeviationsVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemStandardDeviationsMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemStandardDeviationsMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemStandardDeviationsRange, this.trunc));

        fout.print("item variances", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemVariancesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemVariancesSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemVariancesVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemVariancesMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemVariancesMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemVariancesRange, this.trunc));

        fout.print("item mimima", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemMinimaMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMinimaSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMinimaVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMinimaMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMinimaMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemMinimaRange, this.trunc));

        fout.print("item maxima", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemMaximaMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMaximaSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMaximaVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMaximaMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMaximaMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemMaximaRange, this.trunc));

        fout.print("item medians", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemMediansMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMediansSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMediansVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMediansMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemMediansMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemMediansRange, this.trunc));

        fout.print("item ranges", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemRangesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemRangesSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemRangesVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemRangesMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemRangesMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemRangesRange, this.trunc));

        fout.print("item totals", fieldItemSName);
        fout.print(Fmath.truncate(this.standardizedItemTotalsMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemTotalsSd, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemTotalsVar, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemTotalsMin, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedItemTotalsMax, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedItemTotalsRange, this.trunc));

        fout.println();


        fout.println("DELETION OF ITEMS");
        int fieldDitem = 16;
        if(fieldItemName>fieldDitem)fieldDitem=fieldItemName;

        fout.print("   ", fieldDitem);
        fout.print("Raw data", fieldItemNumber);
        fout.print("   ", fieldItemNumber);
        fout.print("   ", fieldItemNumber);
        fout.print("   ", fieldItemNumber);
        fout.println("Standardized data");

        fout.print("Deleted item", fieldDitem);
        fout.print("Alpha", fieldItemNumber);
        fout.print("Correlation", fieldItemNumber);
        fout.print("Average", fieldItemNumber);
        fout.print("Average", fieldItemNumber);

        fout.print("Alpha", fieldItemNumber);
        fout.print("Correlation", fieldItemNumber);
        fout.print("Average", fieldItemNumber);
        fout.println("Average");


        fout.print("       ", fieldDitem);
        fout.print("       ", fieldItemNumber);
        fout.print("coefficient", fieldItemNumber);
        fout.print("inter-item", fieldItemNumber);
        fout.print("inter-item", fieldItemNumber);

        fout.print("      ", fieldItemNumber);
        fout.print("coefficient", fieldItemNumber);
        fout.print("inter-item", fieldItemNumber);
        fout.println("inter-item");


        fout.print("       ", fieldDitem);
        fout.print("       ", fieldItemNumber);
        fout.print("with total", fieldItemNumber);
        fout.print("correlation", fieldItemNumber);
        fout.print("correlation", fieldItemNumber);

        fout.print("      ", fieldItemNumber);
        fout.print("with total", fieldItemNumber);
        fout.print("correlation", fieldItemNumber);
        fout.println("correlation");


        fout.print("       ", fieldDitem);
        fout.print("       ", fieldItemNumber);
        fout.print("       ", fieldItemNumber);
        fout.print("coefficient", fieldItemNumber);
        fout.print("coefficient", fieldItemNumber);

        fout.print("        ", fieldItemNumber);
        fout.print("        ", fieldItemNumber);
        fout.print("coefficient", fieldItemNumber);
        fout.println("coefficient");


        fout.print("       ", fieldDitem);
        fout.print("       ", fieldItemNumber);
        fout.print("       ", fieldItemNumber);
        fout.print("without totals", fieldItemNumber);
        fout.print("with totals", fieldItemNumber);

        fout.print("        ", fieldItemNumber);
        fout.print("        ", fieldItemNumber);
        fout.print("without totals", fieldItemNumber);
        fout.println("with totals");

        double[] newRawAlpha = new double[this.nItems];
        double[] newStandardizedAlpha = new double[this.nItems];
        double[] newRawRho = new double[this.nItems];
        double[] newStandardizedRho = new double[this.nItems];
        for(int i=0; i<this.nItems; i++){
            int index = i+1;
            double[][] newScore1 = this.deleteItem(index);
            Cronbach cr = new Cronbach();
            cr.enterScoresAsRowPerPerson(newScore1);
            double rawAlphaD = cr.rawAlpha();
            newRawAlpha[i] = rawAlphaD;
            double rawMeanRhoWithTotalsD = cr.rawAverageCorrelationCoefficientsWithTotals();
            double[] rawPersonTotalsD = cr.rawPersonTotals();
            double rawRhoAgainstTotalsD = Stat.corrCoeff(rawPersonTotalsD, this.scores0[i]);
            double rawMeanRhoWithoutTotalsD = cr.rawAverageCorrelationCoefficients();
            newRawRho[i] = rawRhoAgainstTotalsD;

            double standardizedAlphaD = cr.standardizedAlpha();
            newStandardizedAlpha[i] = standardizedAlphaD;
            double standardizedMeanRhoWithTotalsD = cr.standardizedAverageCorrelationCoefficients();
            double[] standardizedPersonTotalsD = cr.standardizedPersonTotals();
            double standardizedRhoAgainstTotalsD = Stat.corrCoeff(standardizedPersonTotalsD, this.scores0[i]);
            double standardizedMeanRhoWithoutTotalsD = cr.standardizedAverageCorrelationCoefficients();
            newStandardizedRho[i] = standardizedRhoAgainstTotalsD;

            fout.print(this.itemNames[i], fieldDitem);
            fout.print(Fmath.truncate(rawAlphaD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(rawRhoAgainstTotalsD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(rawMeanRhoWithoutTotalsD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(rawMeanRhoWithTotalsD, trunc), fieldItemNumber);

            fout.print(Fmath.truncate(standardizedAlphaD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(standardizedRhoAgainstTotalsD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(standardizedMeanRhoWithoutTotalsD, trunc), fieldItemNumber);
            fout.print(Fmath.truncate(standardizedMeanRhoWithTotalsD, trunc), fieldItemNumber);
            fout.println();
        }
        fout.println();

        fout.print("No item deleted", fieldDitem);
        fout.print(Fmath.truncate(this.rawAlpha, trunc), fieldItemNumber);
        fout.print("   ", fieldItemNumber);
        fout.print(Fmath.truncate(this.rawMeanRhoWithoutTotals, trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawMeanRhoWithTotals, trunc), fieldItemNumber);

        fout.print(Fmath.truncate(this.standardizedAlpha, trunc), fieldItemNumber);
        fout.print("   ", fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedMeanRhoWithoutTotals, trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedMeanRhoWithTotals, trunc));
        fout.println();

        // output a deleted item data file
        this.deletedItemDataFile(newRawAlpha, newRawRho, newStandardizedAlpha, newStandardizedRho);

        int fieldInd = 12;
        fout.println("INDIVIDUALS - raw data");
        fout.print("person", fieldInd);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.println("total");

        fout.print("    ", fieldInd);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("     ");

        int fieldScore = 0;
        for(int i=0; i<this.nItems; i++){
            for(int j=0; j<this.nPersons; j++){
                int sl = Double.toString(scores0[i][j]).length();
                if(sl>fieldScore)fieldScore = sl;
            }
        }
        fieldScore++;
        if(fieldScore<fieldItemName)fieldScore = fieldItemName;
        for(int i=0; i<this.nPersons; i++){
            fout.print((this.personIndices[i]+1), fieldInd);
            fout.print(Fmath.truncate(this.rawPersonMeans[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawPersonStandardDeviations[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawPersonMinima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawPersonMaxima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.rawPersonRanges[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.rawPersonTotals[i], this.trunc));
        }
        fout.println();

        fout.println("scores:");
        fout.print("person ", fieldInd);
        for(int i=0; i<this.nItems; i++)fout.print(this.itemNames[i], fieldItemNumber);
        fout.println();

        for(int i=0; i<this.nPersons; i++){
            fout.print((this.personIndices[i]+1), fieldInd);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.scores1[i][j], this.trunc), fieldItemNumber);
            fout.println();
        }
        fout.println();

        fout.println("INDIVIDUALS - standardized data");
        fout.print("person ", fieldInd);
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.println("total");


        fout.print("    ", fieldInd);
        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("     ");

        for(int i=0; i<this.nPersons; i++){
            fout.print((this.personIndices[i]+1), fieldInd);
            fout.print(Fmath.truncate(this.standardizedPersonMeans[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedPersonStandardDeviations[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedPersonMinima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedPersonMaxima[i], this.trunc), fieldItemNumber);
            fout.print(Fmath.truncate(this.standardizedPersonRanges[i], this.trunc), fieldItemNumber);
            fout.println(Fmath.truncate(this.standardizedPersonTotals[i], this.trunc));
        }
        fout.println();

        fout.println("scores:");
        fout.print("person ", fieldInd);
        for(int i=0; i<this.nItems; i++)fout.print(this.itemNames[i], fieldItemNumber);
        fout.println();

        for(int i=0; i<this.nPersons; i++){
            fout.print((this.personIndices[i]+1), fieldInd);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.standardizedScores1[i][j], trunc), fieldItemNumber);
            fout.println();
        }
        fout.println();

        fout.println("ALL SCORES - raw data");
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.println("overall");

        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("total");

        fout.print(Fmath.truncate(this.rawAllResponsesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawAllResponsesStandardDeviation, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawAllResponsesMinimum, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawAllResponsesMaximum, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.rawAllResponsesRange, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.rawAllResponsesTotal, this.trunc));
        fout.println();

        fout.println("ALL SCORES - standardized data");
        fout.print("mean", fieldItemNumber);
        fout.print("standard", fieldItemNumber);
        fout.print("minimum", fieldItemNumber);
        fout.print("maximum", fieldItemNumber);
        fout.print("range", fieldItemNumber);
        fout.println("overall");

        fout.print("    ", fieldItemNumber);
        fout.print("deviation", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.print("     ", fieldItemNumber);
        fout.println("total");

        fout.print(Fmath.truncate(this.standardizedAllResponsesMean, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedAllResponsesStandardDeviation, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedAllResponsesMinimum, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedAllResponsesMaximum, this.trunc), fieldItemNumber);
        fout.print(Fmath.truncate(this.standardizedAllResponsesRange, this.trunc), fieldItemNumber);
        fout.println(Fmath.truncate(this.standardizedAllResponsesTotal, this.trunc));
        fout.println();


        // close output file
        fout.close();
    }

    // Creation of a data file facilitating a full analysis of the data minus the least consitent item
    private void deletedItemDataFile(double[] newRawAlpha, double[] newRawRho, double[] newStandardizedAlpha, double[] newStandardizedRho){

        // Find maximum alpha and minimum correlation with totals
        ArrayMaths am = new ArrayMaths(newRawAlpha);
        int index1 =  am.maximumIndex();
        am = new ArrayMaths(newStandardizedAlpha);
        int index2 =  am.maximumIndex();
        am = new ArrayMaths(newRawRho);
        int index3 =  am.minimumIndex();
        am = new ArrayMaths(newStandardizedRho);
        int index4 =  am.minimumIndex();


        // majority voting on least consistent item
        this.deletedItemIndex = index3;
        if(index1==index2 && index1==index3 && index1==index4){
            this.deletedItemIndex = index1;
        }
        else{
            if(index1==index2 && (index1==index3 || index1==index4)){
                this.deletedItemIndex = index1;
            }
            else{
                if(index4==index3 && (index4==index1 || index4==index2)){
                    this.deletedItemIndex = index4;
                }
                else{
                    if(index1==index2 && index3==index4){
                        this.deletedItemIndex = index3;
                    }
                    else{
                        if(index1==index3 && index2==index4){
                            this.deletedItemIndex = index1;
                        }
                        else{
                            if(index1!=index2 && index2!=index3 && index3!=index4){
                                this.deletedItemIndex = index3;
                            }
                        }
                    }
                }
            }
        }

        this.deletedFilename = null;
        if(this.inputFilename!=null){
            this.deletedFilename = this.inputFilename;
            int pos = this.deletedFilename.indexOf(".");
            if(pos!=-1)this.deletedFilename = this.deletedFilename.substring(0,pos);
            this.deletedFilename = this.deletedFilename + "_" + this.itemNames[this.deletedItemIndex]+"_deleted";
            this.deletedFilename = this.deletedFilename + ".txt";
        }
        else{
            this.deletedFilename = "DeletedItemFile.txt";
        }

        FileOutput dfout = new FileOutput(this.deletedFilename);
        String newTitle = title[0] + " - Item " + this.itemNames[this.deletedItemIndex] + " deleted";
        dfout.println(newTitle);
        dfout.println((this.nItems-1));
        dfout.println(this.nPersons);
        for(int i=0; i<this.nItems; i++){
            if(i!=this.deletedItemIndex)dfout.printtab(this.itemNames[i]);
        }
        dfout.println();
        if(this.originalDataType==0){
            for(int i=0; i<this.nItems; i++){
                if(i!=this.deletedItemIndex){
                    for(int j=0; j<this.nPersons; j++){
                        dfout.printtab(this.scores0[i][j]);
                    }
                    dfout.println();
                }
            }
        }
        else{
            for(int j=0; j<this.nPersons; j++){
                for(int i=0; i<this.nItems; i++){
                    if(i!=this.deletedItemIndex){
                        dfout.printtab(this.scores1[j][i]);
                    }
                }
                dfout.println();
            }
        }
        dfout.close();
    }

    // Return deleted item new data file name
    public String getDeletionFileName(){
        return this.deletedFilename;
    }

    // Return least consistent item name
    public String getLeastConsistentItemName(){
        return this.itemNames[this.deletedItemIndex];
    }


}