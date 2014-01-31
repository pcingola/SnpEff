/*
*   CLASS:      Scores
*
*   USAGE:      Class for entering scores (responses) for several items,
*               e.g. questionnaire questions, examination questions
*               This is superclass for several educational statistics classes,
*               e.g. Cronbach
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       October 2008
*   AMENDED:    12 October 2008, 1-18 November 2010, 27 November 2010, 3-4 December 2010
*               7 December 2011 
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Cronbach.html
*
*   Copyright (c) 2008-2010 Michael Thomas Flanagan
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

import flanagan.math.*;
import flanagan.io.*;
import flanagan.analysis.*;
import flanagan.plot.*;


public class Scores{

    protected String[] title =null;                                                 // title
    protected int titleLines = 0;                                                   // number of lines in the title

    protected String inputFilename = null;                                          // input file name if input data read from file
    protected String outputFilename = null;                                         // output file name if output written to file
    protected int fileOption = 1;                                                   // type of file option
                                                                                    // option = 1 - text file  (.txt)
                                                                                    // option = 2 - MS Excel file (.xls)
    protected boolean fileOptionSet = false;                                        // = true if fileOption changed by user
    protected String[] fileExtensions = {".txt", ".xls"};                           // Output file extensions
    protected boolean fileNumberingSet = false;                                     // = output file of identical name to existing file overwrites existing file
                                                                                    // = true incremented number added to output file name to prevent overwriting
    protected int trunc = 6;                                                        // number of decimal places in output data
                                                                                    // overriden by the precision of the input data if this is greater
    protected boolean truncAll = false;                                             // if true - the above truncation is not overriden by the precision of the input data if this is greater

    protected int originalDataType = -1;                                            // = 1 - String[][]  (including read from file);
                                                                                    // = 2 - double[][]
                                                                                    // = 3 - Matrix
                                                                                    // = 4 - float[][]
                                                                                    // = 5 - int[][]
                                                                                    // = 6 - char[][]
                                                                                    // = 7 - boolean[][]

    protected int originalDataOrder = -1;                                           // = 0 - matrix columns = responses of a person
                                                                                    // = 1 - matrix rows = responses of a person to each item

    protected Object originalData = null;                                           // Original data as entered

    protected double[][]scores0 = null;                                             // individual scores -  after any 'no response' deletions or replacements
                                                                                    // arranged as rows of scores for each item
                                                                                    // e.g. scores0[0][0] to scores0[0][nIndividuals-1] =  scores for each person in turn for the first item
                                                                                    //      scores0[1][0] to scores0[1][nIndividuals-1] =  scores for each person in turn for the second item
                                                                                    // etc.
    protected double[][]originalScores0 = null;                                     // scores0 before any 'no response' deletions or replacements
    protected double[][]standardizedScores0 = null;                                 // standardized scores0

    protected double[][]scores1 = null;                                             // individual scores -  after any 'no response' deletions or replacements
                                                                                    // arranged as rows of scores for each person
                                                                                    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
                                                                                    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
                                                                                    // etc.
    protected double[][]originalScores1 = null;                                     // scores1 before any 'no response' deletions or replacements
    protected double[][]standardizedScores1 = null;                                 // standardized scores1

    protected boolean dataEntered = false;                                          // = true when scores entered
    protected boolean dataPreprocessed = false;                                     // = true when scores have been preprocessed

    protected int nItems = 0;                                                       // number of items, after any deletions
    protected int originalNitems = 0;                                               // original number of items
    protected String[] itemNames = null;                                            // names of the items
    protected String[] originalItemNames = null;                                    // list of item names before any deletions
    protected boolean itemNamesSet = false;                                         // = true when item names entered

    protected int nPersons = 0;                                                     // number of persons, after any deletions
    protected int originalNpersons = 0;                                             // original number of persons
    protected String[] personNames = null;                                          // names of the persons

    protected int nScores = 0;                                                      // total number of scores, after any deletions
    protected int originalNscores = 0;                                              // original total number of scores

    protected String otherFalse = null;                                             // false value for dichotomous data if one of the default values
    protected String otherTrue = null;                                              // true value for dichotomous data if one of the default values
                                                                                    // default values: a numeral, true (ignoring case), false (ignoring case), yes (ignoring case), no (ignoring case)
    protected boolean otherDichotomousDataSet = false;                              // = true if user sets an alternative dichotomous pair
    protected boolean[] dichotomous = null;                                         // true if the data in an item is dichotomous
    protected double[] dichotomousPercentage = null;                                // percentage of responses in an item that are dichotomous
    protected boolean dichotomousOverall = false;                                   // true if all the data is dichotomous
    protected boolean dichotomousCheckDone = false;                                 // true if check for dichotomous data performed

    protected boolean letterToNumeralSet = true;                                    // = true if user set the letter to numeral option allowing alphabetic response input

    protected boolean ignoreNoResponseRequests = false;                             // = true - requests for 'no resonse' options are not displayed

    protected double itemDeletionPercentage = 100.0;                                // percentage of no responses allowed within an item before the item is deleted
    protected boolean itemDeletionPercentageSet = false;                            // = true when this percentage is reset

    protected double personDeletionPercentage = 100.0;                              // percentage of no responses allowed within a person's responses before the person is deleted
    protected boolean personDeletionPercentageSet = false;                          // = true when this percentage is reset

    protected int replacementOption = 3;                                            // option flag for a missing response if deletion not carried out
                                                                                    // option = 1 - score replaced by zero
                                                                                    // option = 2 - score replaced by person's mean
                                                                                    // option = 3 - score replaced by item mean (default option)
                                                                                    // option = 4 - score replaced by overall mean
                                                                                    // option = 5 - user supplied score for each 'no response'

    protected String[] replacementOptionNames = {"score replaced by zero", "score replaced by person's mean", "score replaced by item mean", "score replaced by overall mean", "user supplied score for each 'no response'"};

    protected boolean replacementOptionSet = false;                                 // = true when replacementOption set
    protected boolean allNoResponseOptionsSet = false;                              // = true when personDeletionPercentageSet, itemDeletionPercentageSet and replacementOptionSet are all true
    protected boolean noResponseHandlingSet = false;                                // = true when 'no response' handling options are all set

    protected int nNaN = 0;                                                         // number of 'no responses' (initially equated to NaN)
    protected boolean[] deletedItems = null;                                        // = true if item corresponding to the deletedItems array index has been deleted, false otherwise
    protected int nDeletedItems = 0;                                                // number of deleted items
    protected int[] deletedItemsIndices = null;                                     // indices of the deleted items
    protected int[] itemIndices = null;                                             // indices of items in original data before deletions

    protected boolean[] deletedPersons = null;                                      // = true if person corresponding to the deletedItems array index has been deleted, false otherwise
                                                                                    //   person deleted if no response in all items,then deleted irrespective of missing response option choice
    protected int nDeletedPersons = 0;                                              // number of deleted persons
    protected int[] deletedPersonsIndices = null;                                   // indices of the deleted persons
    protected int[] personIndices = null;                                           // indices of persons in original data before deletions

    protected int nReplacements = 0;                                                // number of 'no response' replacements
    protected String[] replacementIndices = null;                                   // indices of 'no response' replacements

    protected double[] rawItemMeans = null;                                         // means of the responses in each item (raw data)
    protected double rawItemMeansMean = Double.NaN;                                 // mean of the means of the responses in each item (raw data)
    protected double rawItemMeansSd = Double.NaN;                                   // standard deviation of the means of the responses in each item (raw data)
    protected double rawItemMeansVar = Double.NaN;                                  // variance of the means of the responses in each item (raw data)
    protected double rawItemMeansMin = Double.NaN;                                  // minimum of the means of the responses in each item (raw data)
    protected double rawItemMeansMax = Double.NaN;                                  // maximum of the means of the responses in each item (raw data)
    protected double rawItemMeansRange = Double.NaN;                                // range of the means of the responses in each item (raw data)

    protected double[] rawItemStandardDeviations = null;                            // standard deviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsMean = Double.NaN;                    // mean of the StandardDeviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsSd = Double.NaN;                      // standard deviation of the Standard Deviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsVar = Double.NaN;                     // variance of the Standard Deviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsMin = Double.NaN;                     // minimum of the Standard Deviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsMax = Double.NaN;                     // maximum of the Standard Deviations of the responses in each item (raw data)
    protected double rawItemStandardDeviationsRange = Double.NaN;                   // range of the Standard Deviations of the responses in each item (raw data)

    protected double[] rawItemVariances = null;                                     // variances of the responses in each item (raw data)
    protected double rawItemVariancesMean = Double.NaN;                             // mean of the Variances of the responses in each item (raw data)
    protected double rawItemVariancesSd = Double.NaN;                               // standard deviation of the Variances of the responses in each item (raw data)
    protected double rawItemVariancesVar = Double.NaN;                              // variance of the Variances of the responses in each item (raw data)
    protected double rawItemVariancesMin = Double.NaN;                              // minimum of the Variances of the responses in each item (raw data)
    protected double rawItemVariancesMax = Double.NaN;                              // maximum of the Variances of the responses in each item (raw data)
    protected double rawItemVariancesRange = Double.NaN;                            // range of the Variances of the responses in each item (raw data)

    protected double[] rawItemMinima = null;                                        // minima of the responses in each item (raw data)
    protected double rawItemMinimaMean = Double.NaN;                                // mean of the Minima of the responses in each item (raw data)
    protected double rawItemMinimaSd = Double.NaN;                                  // standard deviation of the Minima of the responses in each item (raw data)
    protected double rawItemMinimaVar = Double.NaN;                                 // variance of the Minima of the responses in each item (raw data)
    protected double rawItemMinimaMin = Double.NaN;                                 // minimum of the Minima of the responses in each item (raw data)
    protected double rawItemMinimaMax = Double.NaN;                                 // maximum of the Minima of the responses in each item (raw data)
    protected double rawItemMinimaRange = Double.NaN;                               // range of the Minima of the responses in each item (raw data)

    protected double[] rawItemMaxima = null;                                        // maxima of the responses in each item (raw data)
    protected double rawItemMaximaMean = Double.NaN;                                // mean of the Maxima of the responses in each item (raw data)
    protected double rawItemMaximaSd = Double.NaN;                                  // standard deviation of the Maxima of the responses in each item (raw data)
    protected double rawItemMaximaVar = Double.NaN;                                 // variance of the Maxima of the responses in each item (raw data)
    protected double rawItemMaximaMin = Double.NaN;                                 // minimum of the Maxima of the responses in each item (raw data)
    protected double rawItemMaximaMax = Double.NaN;                                 // maximum of the Maxima of the responses in each item (raw data)
    protected double rawItemMaximaRange = Double.NaN;                               // range of the Maxima of the responses in each item (raw data)

    protected double[] rawItemRanges = null;                                        // Ranges of the responses in each item (raw data)
    protected double rawItemRangesMean = Double.NaN;                                // mean of the Ranges of the responses in each item (raw data)
    protected double rawItemRangesSd = Double.NaN;                                  // standard deviation of the Ranges of the responses in each item (raw data)
    protected double rawItemRangesVar = Double.NaN;                                 // variance of the Ranges of the responses in each item (raw data)
    protected double rawItemRangesMin = Double.NaN;                                 // minimum of the Ranges of the responses in each item (raw data)
    protected double rawItemRangesMax = Double.NaN;                                 // maximum of the Ranges of the responses in each item (raw data)
    protected double rawItemRangesRange = Double.NaN;                               // range of the Ranges of the responses in each item (raw data)

    protected double[] rawItemTotals = null;                                        // totals of the responses in each item (raw data)
    protected double rawItemTotalsMean = Double.NaN;                                // mean of the Totals of the responses in each item (raw data)
    protected double rawItemTotalsSd = Double.NaN;                                  // standard deviation of the Totals of the responses in each item (raw data)
    protected double rawItemTotalsVar = Double.NaN;                                 // variance of the Totals of the responses in each item (raw data)
    protected double rawItemTotalsMin = Double.NaN;                                 // minimum of the Totals of the responses in each item (raw data)
    protected double rawItemTotalsMax = Double.NaN;                                 // maximum of the Totals of the responses in each item (raw data)
    protected double rawItemTotalsRange = Double.NaN;                               // range of the Totals of the responses in each item (raw data)

    protected double[] rawItemMedians = null;                                       // medians of the sorted responses in each item (raw data)
    protected double rawItemMediansMean = Double.NaN;                               // mean of the Medians of the responses in each item (raw data)
    protected double rawItemMediansSd = Double.NaN;                                 // standard deviation of the Medians of the responses in each item (raw data)
    protected double rawItemMediansVar = Double.NaN;                                // variance of the Medians of the responses in each item (raw data)
    protected double rawItemMediansMin = Double.NaN;                                // minimum of the Medians of the responses in each item (raw data)
    protected double rawItemMediansMax = Double.NaN;                                // maximum of the Medians of the responses in each item (raw data)
    protected double rawItemMediansRange = Double.NaN;                              // range of the Medians of the responses in each item (raw data)

    protected double[] rawItemMomentSkewness = null;                                // Moment skewness of the  responses in each item (raw data)
    protected double[] rawItemMedianSkewness = null;                                // Median skewness of the  responses in each item (raw data)
    protected double[] rawItemQuartileSkewness = null;                              // Quartile skewness of the  responses in each item (raw data)
    protected double[] rawItemKurtosisExcess = null;                                // Kurtosis excess of the  responses in each item (raw data)

    protected double[] rawPersonMeans = null;                                       // means of the responses for each person  (raw data)
    protected double[] rawPersonStandardDeviations = null;                          // standard deviations of the responses for each person (raw data)
    protected double[] rawPersonVariances = null;                                   // variances of the responses for each person (raw data)
    protected double[] rawPersonMinima = null;                                      // minima of the responses for each person (raw data)
    protected double[] rawPersonMaxima = null;                                      // maxima of the responses for each person (raw data)
    protected double[] rawPersonRanges = null;                                      // ranges of the responses for each person (raw data)
    protected double[] rawPersonTotals = null;                                      // totals of the responses for each person (raw data)

    protected double rawAllResponsesMean = Double.NaN;                              // mean of all the responses  (raw data)
    protected double rawAllResponsesStandardDeviation = Double.NaN;                 // standard deviation of all the responses  (raw data)
    protected double rawAllResponsesVariance = Double.NaN;                          // variance of all the responses (raw data)
    protected double rawAllResponsesMinimum = Double.NaN;                           // minimum of all the responses (raw data)
    protected double rawAllResponsesMaximum = Double.NaN;                           // maximum of all the responses (raw data)
    protected double rawAllResponsesRange = Double.NaN;                             // ranges of all the responses (raw data)
    protected double rawAllResponsesTotal = Double.NaN;                             // total of all the responses (raw data)

    protected double[][] rawCovariances = null;                                     // covariances between items  (raw data)
    protected double[][] rawCorrelationCoefficients = null;                         // correlation coefficients between items  (raw data)
    protected double[] rawRhosWithTotal = null;                                     // correlation coefficient of an item with the itemtotals  (raw data)
    protected double rawMeanRhoWithTotals = Double.NaN;                             // average inter-item correlation coeffecient including totals (raw data)
    protected double rawStandardDeviationRhoWithTotals = Double.NaN;                // standard deviation of inter-item correlation coeffecient including totals (raw data)
    protected double rawMeanRhoWithoutTotals = Double.NaN;                          // average inter-item correlation coeffecient excluding totals (raw data)
    protected double rawStandardDeviationRhoWithoutTotals = Double.NaN;             // standard deviation of inter-item correlation coeffecient excluding totals (raw data)

    protected double[] standardizedItemMeans = null;                                // means of the responses in each item (standardized data)
    protected double standardizedItemMeansMean = Double.NaN;                        // mean of the means of the responses in each item (standardized data)
    protected double standardizedItemMeansSd = Double.NaN;                          // standard deviation of the means of the responses in each item (standardized data)
    protected double standardizedItemMeansVar = Double.NaN;                         // variance of the means of the responses in each item (standardized data)
    protected double standardizedItemMeansMin = Double.NaN;                         // minimum of the means of the responses in each item (standardized data)
    protected double standardizedItemMeansMax = Double.NaN;                         // maximum of the means of the responses in each item (standardized data)
    protected double standardizedItemMeansRange = Double.NaN;                       // range of the means of the responses in each item (standardized data)

    protected double[] standardizedItemStandardDeviations = null;                   // standard deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsMean = Double.NaN;           // mean of the Standard Deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsSd = Double.NaN;             // standard deviation of the Standard Deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsVar = Double.NaN;            // variance of the Standard Deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsMin = Double.NaN;            // minimum of the Standard Deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsMax = Double.NaN;            // maximum of the Standard Deviations of the responses in each item (standardized data)
    protected double standardizedItemStandardDeviationsRange = Double.NaN;          // range of the Standard Deviations of the responses in each item (standardized data)

    protected double[] standardizedItemVariances = null;                            // variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesMean = Double.NaN;                    // mean of the Variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesSd = Double.NaN;                      // standard deviation of the Variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesVar = Double.NaN;                     // variance of the Variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesMin = Double.NaN;                     // minimum of the Variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesMax = Double.NaN;                     // maximum of the Variances of the responses in each item (standardized data)
    protected double standardizedItemVariancesRange = Double.NaN;                   // range of the Variances of the responses in each item (standardized data)

    protected double[] standardizedItemMinima = null;                               // minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaMean = Double.NaN;                       // mean of the Minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaSd = Double.NaN;                         // standard deviation of the Minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaVar = Double.NaN;                        // variance of the Minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaMin = Double.NaN;                        // minimum of the Minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaMax = Double.NaN;                        // maximum of the Minima of the responses in each item (standardized data)
    protected double standardizedItemMinimaRange = Double.NaN;                      // range of the Minima of the responses in each item (standardized data)

    protected double[] standardizedItemMaxima = null;                               // maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaMean = Double.NaN;                       // mean of the Maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaSd = Double.NaN;                         // standard deviation of the Maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaVar = Double.NaN;                        // variance of the Maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaMin = Double.NaN;                        // minimum of the Maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaMax = Double.NaN;                        // maximum of the Maxima of the responses in each item (standardized data)
    protected double standardizedItemMaximaRange = Double.NaN;                      // range of the Maxima of the responses in each item (standardized data)

    protected double[] standardizedItemRanges = null;                               // Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesMean = Double.NaN;                       // mean of the Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesSd = Double.NaN;                         // standard deviation of the Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesVar = Double.NaN;                        // variance of the Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesMin = Double.NaN;                        // minimum of the Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesMax = Double.NaN;                        // maximum of the Ranges of the responses in each item (standardized data)
    protected double standardizedItemRangesRange = Double.NaN;                      // range of the Ranges of the responses in each item (standardized data)

    protected double[] standardizedItemTotals = null;                               // totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsMean = Double.NaN;                       // mean of the Totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsSd = Double.NaN;                         // standard deviation of the Totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsVar = Double.NaN;                        // variance of the Totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsMin = Double.NaN;                        // minimum of the Totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsMax = Double.NaN;                        // maximum of the Totals of the responses in each item (standardized data)
    protected double standardizedItemTotalsRange = Double.NaN;                      // range of the Totals of the responses in each item (standardized data)

    protected double[] standardizedItemMedians = null;                              // medians of the sorted responses in each item (standardized  data)
    protected double standardizedItemMediansMean = Double.NaN;                      // mean of the Medians of the responses in each item (standardized data)
    protected double standardizedItemMediansSd = Double.NaN;                        // standard deviation of the Medians of the responses in each item (standardized data)
    protected double standardizedItemMediansVar = Double.NaN;                       // variance of the Medians of the responses in each item (standardized data)
    protected double standardizedItemMediansMin = Double.NaN;                       // minimum of the Medians of the responses in each item (standardized data)
    protected double standardizedItemMediansMax = Double.NaN;                       // maximum of the Medians of the responses in each item (standardized data)
    protected double standardizedItemMediansRange = Double.NaN;                     // range of the Medians of the responses in each item (standardized data)

    protected double[] standardizedItemMomentSkewness = null;                       // Moment skewness of the  responses in each item (standardized data)
    protected double[] standardizedItemMedianSkewness = null;                       // Median skewness of the  responses in each item (standardized data)
    protected double[] standardizedItemQuartileSkewness = null;                     // Quartile skewness of the  responses in each item (standardized data)
    protected double[] standardizedItemKurtosisExcess = null;                       // Kurtosis excess of the  responses in each item (standardized data)

    protected double[] standardizedPersonMeans = null;                              // mean of the responses for each person  (standardized data)
    protected double[] standardizedPersonStandardDeviations = null;                 // standard deviation of the responses for each person (standardized data)
    protected double[] standardizedPersonVariances = null;                          // variance of the responses for each person (standardized data)
    protected double[] standardizedPersonMinima = null;                             // minima of the responses for each person (standardized data)
    protected double[] standardizedPersonMaxima = null;                             // maxima of the responses for each person (standardized data)
    protected double[] standardizedPersonRanges = null;                             // ranges of the responses for each person (standardized data)
    protected double[] standardizedPersonTotals = null;                             // totals of the responses for each person (standardized data)

    protected double standardizedAllResponsesMean = Double.NaN;                     // means of all the responses  (standardized data)
    protected double standardizedAllResponsesStandardDeviation = Double.NaN;        // standard deviations of all the responses  (standardized data)
    protected double standardizedAllResponsesVariance = Double.NaN;                 // variances of all the responses (standardized data)
    protected double standardizedAllResponsesMinimum = Double.NaN;                  // minimum of all the responses (standardized data)
    protected double standardizedAllResponsesMaximum = Double.NaN;                  // maximum of all the responses (standardized data)
    protected double standardizedAllResponsesRange = Double.NaN;                    // range of all the responses (standardized data)
    protected double standardizedAllResponsesTotal = Double.NaN;                    // total of all the responses (standardized data)

    protected double[][] standardizedCovariances = null;                            // covariances between items  (standardized data)
    protected double[][] standardizedCorrelationCoefficients = null;                // correlation coefficients between items  (standardized data)
    protected double[] standardizedRhosWithTotal = null;                            // correlation coefficient of an item with the itemtotals  (standardized data)
    protected double standardizedMeanRhoWithTotals = Double.NaN;                    // average inter-item correlation coeffecient including totals (standardized data)
    protected double standardizedStandardDeviationRhoWithTotals = Double.NaN;       // standard deviation of nter-item correlation coeffecient including totals (standardized data)
    protected double standardizedMeanRhoWithoutTotals = Double.NaN;                 // average inter-item correlation coeffecient excluding totals (standardized data)
    protected double standardizedStandardDeviationRhoWithoutTotals = Double.NaN;    // standard deviation of inter-item correlation coeffecient excluding totals (standardized data)

    protected boolean variancesCalculated = false;                                  // = true when means, variances and standard deviations calculated
    protected boolean covariancesCalculated = false;                                // = true when covariances and correlation coefficients calculated

    protected boolean nFactorOption = false;                                        // = true  varaiance, covariance and standard deviation denominator = n
                                                                                    // = false varaiance, covariance and standard deviation denominator = n-1

    protected int sameCheck = 0;                                                    // = 0;     no row or column with identical elements in the data matrix
                                                                                    // = 1;     row/s of identical elements found
                                                                                    // = 2;     column/s of identical elements found
                                                                                    // = 3;     row/s and column/s of identical elements found

    // CONSTRUCTOR
    public Scores(){
    }

    // TITLE
    // Enter title (optional)
    public void enterTitle(String title){
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Title: " + title;
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }
        else{
            this.title[0] =  title;
        }
    }

    // MISSING RESPONSE
    // Set percentage of no responses allowed within a person's responses before deletion of the person performed
    public void setPersonDeletionPercentage(double perCent){
        this.personDeletionPercentage = perCent;
        this.personDeletionPercentageSet = true;
        if(this.itemDeletionPercentageSet && this.replacementOptionSet){
            this.allNoResponseOptionsSet = true;
            if(this.dataEntered){
                this.preprocessData();
            }
        }
    }

    // Set percentage of no responses allowed within an item before deletion of the item performed
    public void setItemDeletionPercentage(double perCent){
        this.itemDeletionPercentage = perCent;
        this.itemDeletionPercentageSet = true;
        if(this.personDeletionPercentageSet && this.replacementOptionSet){
            this.allNoResponseOptionsSet = true;
            if(this.dataEntered){
                this.preprocessData();
            }
        }
    }

    // Set missing response option if deletion of item not carried out
    // option = 1 - score replaced by zero
    // option = 2 - score replaced by person's mean
    // option = 3 - score replaced by item mean (default option)
    // option = 4 - score replaced by overall mean
    // option = 5 - user supplied score for each 'no response'
    // requires data to be entered as a String matrix or from text file
    // default option = 2
    public void setMissingDataOption(int option){
        if(option<1 || option>5)throw new IllegalArgumentException("The missing response option entered is " + option + "; the option must be 1, 2, 3, 4 or 5");
        this.replacementOption = option;
        this.replacementOptionSet = true;
        if(this.personDeletionPercentageSet && this.itemDeletionPercentageSet){
            this.allNoResponseOptionsSet = true;
            if(this.dataEntered){
                this.preprocessData();
            }
        }
    }

    // Ignore requests for deletion percentages and replacement options
    public void ignoreMissingDataOptionRequests(){
        this.ignoreNoResponseRequests = true;
        this.allNoResponseOptionsSet = true;
        this.itemDeletionPercentageSet = true;
        this.personDeletionPercentageSet = true;
        this.allNoResponseOptionsSet = true;
    }


    // Handling of 'no responses'
    // Checks for and carries out item and/or person deletion
    // checks for and carries out no response replacements
    protected void noResponseHandling(){
        if(this.nNaN>0 && !this.noResponseHandlingSet){

            // Check for person deletion
            // Check whether any person has offered no responses at all
            // If so - delete person irrespective of replacementOption choice
            this.nDeletedPersons = 0;

            for(int j=0;j<this.nPersons; j++){
                int nIndNaN = 0;
                this.deletedPersons[j] = false;
                for(int i=0; i<this.nItems; i++){
                    if(Double.isNaN(scores0[i][j])){
                        nIndNaN++;
                    }
                }
                if(nIndNaN==this.nItems){
                    this.deletedPersons[j] = true;
                }
            }

            for(int i=0; i<this.nPersons; i++){
                if(!this.deletedPersons[i]){
                    int deletedSum = 0;
                    for(int j=0; j<this.nItems; j++){
                        if(Double.isNaN(scores0[j][i])){
                            deletedSum++;
                            double pc = (double)deletedSum*100.0/this.nItems;
                            if(pc>this.personDeletionPercentage){
                                this.deletedPersons[i] = true;
                            }
                        }
                    }
                }
            }

            for(int i=0; i<this.nPersons; i++)if(this.deletedPersons[i])this.nDeletedPersons++;
            if(this.nDeletedPersons>0){
                int counter = 0;
                this.deletedPersonsIndices = new int[nDeletedPersons];
                for(int j=0;j<this.nPersons; j++){
                    if(this.deletedPersons[j]){
                        this.deletedPersonsIndices[counter] = j;
                        counter++;
                    }
                }
                double[][] scoreTemp = new double[this.nItems][this.nPersons - nDeletedPersons];
                this.personIndices = new int[this.nPersons - nDeletedPersons];
                counter = 0;
                for(int i=0; i<this.nPersons; i++){
                    if(!this.deletedPersons[i]){
                        for(int j=0;j<this.nItems; j++){
                            scoreTemp[j][counter] = this.scores0[j][i];
                        }
                        this.personIndices[counter] = i;
                        counter++;
                     }
                }
                this.nPersons = this.nPersons - this.nDeletedPersons;
                this.nScores = this.nPersons*this.nItems;
                this.scores0 = scoreTemp;
            }
            if(this.nDeletedPersons==0){
                this.personIndices = new int[this.nPersons];
                for(int i=0; i<this.nPersons; i++)this.personIndices[i]=i;
            }

            // Check for item deletion
            // Check whether any item contains no responses at all
            // If so - delete item irrespective of replacementOption choice
            this.deletedItems = new boolean[this.nItems];
            this.nDeletedItems = 0;
            for(int i=0;i<this.nItems; i++){
                int nItemNaN = 0;
                deletedItems[i] = false;
                for(int j=0; j<this.nPersons; j++){
                    if(Double.isNaN(scores0[i][j]))nItemNaN++;
                }
                if(nItemNaN==this.nPersons){
                    this.deletedItems[i] = true;
                }
            }

            for(int i=0; i<this.nItems; i++){
                this.deletedItems[i] = false;
                int deletedSum = 0;
                for(int j=0; j<this.nPersons; j++){
                    if(Double.isNaN(scores0[i][j])){
                        deletedSum++;
                        double pc = (double)deletedSum*100.0/this.nPersons;
                        if(pc>this.itemDeletionPercentage){
                            this.deletedItems[i] = true;
                        }
                    }
                }
            }
            for(int i=0; i<this.nItems; i++)if(this.deletedItems[i])this.nDeletedItems++;

            if(this.nDeletedItems>0){

                int counter = 0;
                this.deletedItemsIndices = new int[this.nDeletedItems];
                for(int i=0;i<this.nItems; i++){
                    if(this.deletedItems[i]){
                        this.deletedItemsIndices[counter] = i;
                        counter++;
                    }
                }

                if(this.nItems-this.nDeletedItems<=1)throw new IllegalArgumentException("You have deleted " + nDeletedItems + " items leaving "  + (this.nItems-this.nDeletedItems) + " items and hence no possibility calculation of alpha") ;
                double[][] scoreTemp = new double[this.nItems-this.nDeletedItems][this.nPersons];
                String[] nameTemp = new String[this.nItems-this.nDeletedItems];
                this.itemIndices = new int[this.nItems-this.nDeletedItems];
                counter = 0;
                for(int i=0; i<this.nItems; i++){
                    if(!this.deletedItems[i]){
                        nameTemp[counter] = this.itemNames[i];
                        for(int j=0; j<this.nPersons; j++){
                            scoreTemp[counter][j] = this.scores0[i][j];
                        }
                        this.itemIndices[counter] = i;
                        counter++;
                    }
                }
                this.nItems = this.nItems - this.nDeletedItems;
                this.nScores = this.nPersons*this.nItems;
                this.scores0 = scoreTemp;
                this.scores1 = this.transpose0to1(this.scores0);
                this.itemNames = nameTemp;
            }
            if(this.nDeletedItems==0){
                this.itemIndices = new int[this.nItems];
                for(int i=0; i<this.nItems; i++){
                    this.itemIndices[i]=i;
                }
            }


            // Number of remaining NaN
            int newNaNn = 0;
            for(int i=0; i<this.nPersons; i++){
                for(int j=0; j<this.nItems; j++){
                    if(!Double.isNaN(scores0[j][i])){
                        newNaNn++;
                    }
                }
            }
            //Check for non-deleted 'no responses' and handle as dictated by replacementOption choice
            if(newNaNn>0){
                // current item means and total means
                double[] tItemMeans = new double[this.nItems];
                double tTotalMean = 0.0;
                int counter2 = 0;
                for(int i=0; i<this.nItems; i++){
                    tItemMeans[i] = 0.0;
                    int counter = 0;
                    for(int j=0; j<this.nPersons; j++){
                        if(!Double.isNaN(scores0[i][j])){
                            tItemMeans[i] += scores0[i][j];
                            counter++;
                            tTotalMean += scores0[i][j];
                            counter2++;
                        }
                    }
                    tItemMeans[i] /= counter;
                    tTotalMean /= counter2;
                }

                // current person means
                double[] tIndivMeans = new double[this.nPersons];
                for(int i=0; i<this.nPersons; i++){
                    tIndivMeans[i] = 0.0;
                    int counter = 0;
                    for(int j=0; j<this.nItems; j++){
                        if(!Double.isNaN(scores0[j][i])){
                            tIndivMeans[i] += scores0[j][i];
                            counter++;
                        }
                    }
                    tIndivMeans[i] /= counter;
                }



                // replacements
                this.replacementIndices = new String[newNaNn];
                int rcounter = 0;
                switch(replacementOption){
                    case 1: // replace missing response by zero
                            for(int i=0; i<this.nItems; i++){
                                for(int j=0; j<this.nPersons; j++){
                                    if(Double.isNaN(scores0[i][j])){
                                        scores0[i][j]  = 0.0;
                                        this.replacementIndices[rcounter] = this.itemNames[i] + ", " + (j+1) + ";";
                                        rcounter++;
                                    }
                                }
                            }
                            break;
                    case 2: // replace missing response by mean of the person's responses
                            for(int i=0; i<this.nItems; i++){
                                for(int j=0; j<this.nPersons; j++){
                                    if(Double.isNaN(scores0[i][j])){
                                        scores0[i][j]  = tIndivMeans[i];
                                        this.replacementIndices[rcounter] = this.itemNames[i] + ", " + (j+1) + ";";
                                        rcounter++;
                                    }
                                }
                            }
                            break;
                    case 3: // replace missing response by mean of the item
                            for(int i=0; i<this.nItems; i++){
                                for(int j=0; j<this.nPersons; j++){
                                    if(Double.isNaN(scores0[i][j])){
                                        scores0[i][j]  = tItemMeans[i];
                                        this.replacementIndices[rcounter] = this.itemNames[i] + ", " + (j+1) + ";";
                                        rcounter++;
                                    }
                               }
                            }
                            break;
                    case 4: // replace missing response by mean of the total responses
                            for(int i=0; i<this.nItems; i++){
                                for(int j=0; j<this.nPersons; j++){
                                    if(Double.isNaN(scores0[i][j])){
                                        scores0[i][j]  = tTotalMean;
                                        this.replacementIndices[rcounter] = this.itemNames[i] + ", " + (j+1) + ";";
                                        rcounter++;
                                    }
                                }
                            }
                            break;
                    case 5: // replace missing response by user supplied value for each 'no resonse' occurence
                            for(int i=0; i<this.nItems; i++){
                                for(int j=0; j<this.nPersons; j++){
                                    if(Double.isNaN(scores0[i][j])){
                                        String message1 = "Missing response:";
                                        String message2 = "\nItem index = " + i + ",    item mean = " + Fmath.truncate(tItemMeans[i],4);
                                        String message3 = "\nPerson index = " + j + ",    person's responses mean = " + Fmath.truncate(tIndivMeans[j], 4) ;
                                        String message4 = "\nTotal mean = " + Fmath.truncate(tTotalMean, 4) ;
                                        String message5 = "\nEnter the replacement value" ;
                                        String message = message1 + message2 + message3 + message4 + message5;
                                        scores0[i][j]  = Db.readDouble(message);
                                        this.replacementIndices[rcounter] = this.itemNames[i] + ", " + (j+1) + ";";
                                        rcounter++;
                                    }
                                }
                            }
                            break;
                    default: throw new IllegalArgumentException("!! It should not be possible to have an option choice (replacementOption) = " + replacementOption);
                }
                this.nReplacements = rcounter--;
            }
        }
        this.scores1 = this.transpose0to1(this.scores0);
        this.noResponseHandlingSet = true;
    }

    // Check that all 'no response' options are set
    protected void noResponseRequests(){

        if(!this.allNoResponseOptionsSet){
            if(!this.ignoreNoResponseRequests){

                // Options for no responses at a level that leads to item deletion
                if(this.personDeletionPercentage!=0.0){
                    if(!this.itemDeletionPercentageSet){
                        String message0 = "There are missing responses in this data set";
                        String message1 = "\nYou have not set the percentage of no responses at which you will delete an item";
                        String message2 = "\n(0% = item deleted if a single 'no response' present in the item)";
                        String message3 = "\n(100% = item never deleted)";
                        String message4 = "\nEnter the required value and click OK ";
                        String message5 = "\nor simply click OK for default value";
                        String message6 = message0 + message1 + message2 + message3 + message4 + message5;
                        this.itemDeletionPercentage = Db.readDouble(message6, this.itemDeletionPercentage);
                    }
                }
                this.itemDeletionPercentageSet = true;

                if(this.itemDeletionPercentage!=0.0){
                    // Options for no responses at a level that leads to a person's deletion
                    if(!this.personDeletionPercentageSet){
                        String message0 = "There are missing responses in this data set";
                        String message1 = "\nYou have not set the percentage of no responses at which you will delete a person";
                        String message2 = "\n(0% = person deleted if gives a single 'no response')";
                        String message3 = "\n(100% = person never deleted)";
                        String message4 = "\nEnter the required value and click OK ";
                        String message5 = "\nor simply click OK for default value";
                        String message6 = message0 + message1 + message2 + message3 + message4 + message5;
                        this.personDeletionPercentage = Db.readDouble(message6, this.personDeletionPercentage);
                    }
                }
                this.personDeletionPercentageSet = true;

                if(this.itemDeletionPercentage!=0.0 && this.personDeletionPercentage!=0.0){
                    // Options for no responses at a level that leads to a person's deletion
                    if(!this.replacementOptionSet){
                        String message0 = "There are missing responses in this data set";
                        String message1 = "\nYou have not set the option flag for replacing a missing score";
                        String message2 = "\n  option = 1 - score replaced by zero";
                        String message3 = "\n  option = 2 - score replaced by person's mean";
                        String message4 = "\n  option = 3 - score replaced by item mean (default option)";
                        String message5 = "\n  option = 4 - score replaced by overall mean";
                        String message6 = "\n  option = 5 - user supplied score for each 'no response'";
                        String message7 = "\nEnter the required value and click OK ";
                        String message8 = "\nor simply click OK for default value";
                        String message9 = message0 + message1 + message2 + message3 + message4 + message5 + message6 + message7 + message8;
                        this.replacementOption = Db.readInt(message9, this.replacementOption);
                    }
                }
                this.replacementOptionSet = true;
            }
            this.allNoResponseOptionsSet = true;
        }
    }

    // Set standard deviation, variance and covariance denominators to n
    public void setDenominatorToN(){
        this.nFactorOption = true;
   }

    // Set standard deviation, variance and covariance denominators to n
    public void setDenominatorToNminusOne(){
        this.nFactorOption = false;
    }

    // return indices of deleted persons
    public int[] deletedPersonsIndices(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(this.nDeletedPersons==0){
            System.out.println("Method - deletedPersonsIndices: there are no deleted persons; null returned");
            return null;
        }
        else{
            ArrayMaths am1 = new ArrayMaths(this.deletedPersonsIndices);
            ArrayMaths am2 = am1.plus(1);
            return am2.array_as_int();
        }
    }

    // return number of deleted persons
    public int numberOfDeletedPersons(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nDeletedPersons;
    }

    // return indices of deleted items
    public int[] deletedItemsIndices(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(this.nDeletedItems==0){
            System.out.println("Method - deletedItemsIndices: there are no deleted items; null returned");
            return null;
        }
        else{
            ArrayMaths am1 = new ArrayMaths(this.deletedItemsIndices);
            ArrayMaths am2 = am1.plus(1);
            return am2.array_as_int();
        }
    }

    // return names of deleted items
    public String[] deletedItemsNames(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(this.nDeletedItems==0){
            System.out.println("Method - deletedItemsIndices: there are no deleted items; null returned");
            return null;
        }
        else{
            String[] nam = new String[this.nDeletedItems];
            for(int i=0; i<this.nDeletedItems; i++){
                nam[i] = originalItemNames[this.deletedItemsIndices[i]];
            }
            return nam;
        }
    }


    // return number of deleted items
    public int numberOfDeletedItems(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nDeletedItems;
    }


    // ENTER SCORES
    // 1.   ENTER AS ROWS OF INDIVIDUAL SCORES FOR EACH ITEM
    // Read scores from a text file as a matrix with rows of scores for each item
    // File selected using a dialog window
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // scores may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter(if a letter to numeral conversion as beeen set - see letterToNumeral())
    // 'no responses' may be represented by any one word text except those used to represent a response
    public void readScoresAsRowPerItem(){
        //Select file
        int lineNumber = 1;
        FileChooser fin = new FileChooser();

        // Read in file name
        this.inputFilename = fin.selectFile();
        if(fin.eol())lineNumber++;

        // Set title
        this.title = new String[3];
        this.titleLines = 3;
        this.title[0] = fin.readLine();
        this.title[1] = "Data read from file: " + this.inputFilename;
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        this.title[2] = "Program execution initiated at " + tim + " on " + day;

        // Read in number of items
        this.nItems = fin.readInt();
        if(fin.eol())lineNumber++;

        // Read in number of persons
        this.nPersons = fin.readInt();
        if(fin.eol())lineNumber++;
        this.nScores = this.nItems*this.nPersons;

        // Read in person names
        this.itemNames = new String[this.nItems+1];
        this.personNames = new String[this.nPersons+1];
        for(int i=0; i<this.nItems; i++){
            this.itemNames[i] = fin.readWord();
            if(fin.eol())lineNumber++;
        }
        this.itemNames[this.nItems] = "total";
        this.originalItemNames = this.itemNames;
        this.itemNamesSet = true;

        // Read in data to a String matrix
        String[][] scores = new String[this.nItems][this.nPersons];
        for(int i=0; i<this.nItems; i++){
            int wordsPerLine = 1;
            for(int j=0; j<this.nPersons; j++){
                scores[i][j] = fin.readWord();
                if(fin.eol()){
                    if(wordsPerLine!=this.nPersons)throw new IllegalArgumentException("Line " + lineNumber + ": the number of scores in this row, " + wordsPerLine + ", does not equal the total number of persons, " + this.nPersons);
                    lineNumber++;
                }
                else{
                    wordsPerLine++;
                }
            }
        }
        fin.close();

        // Store entered data
        this.originalData = (Object)scores;
        this.originalDataType = 1;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Read scores from a text file as a matrix with rows of scores for each item
    // File name entered as argument
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // scores may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter(if a letter to numeral conversion as beeen set - see letterToNumeral())
    // 'no responses' may be represented by any one word text except those used to represent a response
    public void readScoresAsRowPerItem(String filename){

        //Select file and read in data
        int lineNumber = 1;
        this.inputFilename = filename;
        FileInput fin = new FileInput(filename);
        if(fin.eol())lineNumber++;

        // Set title
        this.title = new String[3];
        this.titleLines = 3;
        this.title[0] = fin.readLine();
        this.title[1] = "Data read from file: " + filename;
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        this.title[2] = "Program execution initiated at " + tim + " on " + day;

        // Read in number of items
        this.nItems = fin.readInt();
        if(fin.eol())lineNumber++;
        this.itemNames = new String[this.nItems+1];
        for(int i=0; i<this.nItems; i++){
            this.itemNames[i] = "item " + i;
            System.out.println(itemNames[i]);
        }

        // Read in number of persons
        this.nPersons = fin.readInt();
        if(fin.eol())lineNumber++;
        this.personNames = new String[this.nPersons];
        this.nScores = this.nItems*this.nPersons;

        // Read in person names
        for(int i=0; i<this.nPersons; i++){
            this.personNames[i] = fin.readWord();
            if(fin.eol())lineNumber++;

        }
        this.itemNames[this.nItems] = "total";
        this.originalItemNames = this.itemNames;
        this.itemNamesSet = true;

        // Read in data to a String matrix
        String[][] scores = new String[this.nItems][this.nPersons];
        for(int i=0; i<this.nItems; i++){
            int wordsPerLine = 1;
            for(int j=0; j<this.nPersons; j++){
                scores[i][j] = fin.readWord();
                System.out.println("w " + i + " " + j + " " + scores[i][j] + " " + lineNumber);
                if(fin.eol()){
                    if(wordsPerLine!=this.nPersons)throw new IllegalArgumentException("Line " + lineNumber + ": the number of scores in this row, " + wordsPerLine + ", does not equal the total number of persons, " + this.nPersons);
                    lineNumber++;
                }
                else{
                    wordsPerLine++;
                }

            }
        }
        fin.close();

        // Store entered data
        this.originalData = (Object)scores;
        this.originalDataType = 1;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each item - matrix of scores entered as String[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' may be represented by any text except that that corresponds to a number
    public void enterScoresAsRowPerItem(String[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 1;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each item - matrix of scores entered as double[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' must be represenred by Double.NaN
    public void enterScoresAsRowPerItem(double[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 2;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each item - matrix of scores entered as Matrix
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' must be represented by Double.NaN
    public void enterScoresAsRowPerItem(Matrix scores){
        double[][] scoresdd = scores.getArrayCopy();

        // Determine number of items, persons and scores
        this.nItems = scoresdd.length;
        this.nPersons = scoresdd[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)scores.copy();
        this.originalDataType = 3;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }



    // Enter scores as a matrix with rows of scores for each item - matrix of scores entered as float[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' must be represenred by Float.NaN
    public void enterScoresAsRowPerItem(float[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 4;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each item   -  matrix of scores entered as int[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' cannot be entered - see above for methods, e.g. matrix entered as String[][] or double[][], that allow no responses to be entered
    public void enterScoresAsRowPerItem(int[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 5;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }

    // Enter scores as a matrix with rows of scores for each item   -  matrix of scores entered as char[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // responses may be represented by a numeral (0 to 9), y, Y, n, N or a letter(if letter input as been set - see letterToNumeral())
    // 'no responses' may be represented by any character except those used to indicate a response
    public void enterScoresAsRowPerItem(char[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 6;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each item - scores either true  or false  -  matrix of scores entered as boolean[][]
    // e.g. scores0[0][0] to scores0[0][nPersons-1] =  scores for each person in turn for the first item
    //      scores0[1][0] to scores0[1][nPersons-1] =  scores for each person in turn for the second item
    // etc.
    // 'no responses' cannot be entered - see above for methods, e.g. matrix entered as String[][] or double[][], that allow no responses to be entered
    public void enterScoresAsRowPerItem(boolean[][] scores){

        // Determine number of items, persons and scores
        this.nItems = scores.length;
        this.nPersons = scores[0].length;
        this.nScores = this.nItems*this.nPersons;
        this.dichotomous = new boolean[this.nItems];
        this.dichotomousPercentage = new double[this.nItems];
        for(int i=0; i<this.nItems; i++){
            this.dichotomous[i]=true;
            this.dichotomousPercentage[i]=100.0;
        }
        this.dichotomousOverall = true;
        this.dichotomousCheckDone = true;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 7;
        this.originalDataOrder = 0;
        this.dataEntered = true;
    }


    // 2.   ENTER AS ROWS OF SCORES FOR EACH PERSON
    // Read scores from a text file as a matrix with rows of scores for each person
    // File selected using a dialog window
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // scores may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter(if a letter to numeral conversion as beeen set - see letterToNumeral())
    // 'no responses' may be represented by any one word text except those used to represent a response
   public void readScoresAsRowPerPerson(){
        //Select file
        int lineNumber = 1;
        FileChooser fin = new FileChooser();

        // Read in file name
        this.inputFilename = fin.selectFile();
        if(fin.eol())lineNumber++;

        // Set title
        this.title = new String[3];
        this.titleLines = 3;
        this.title[0] = "Title: " + fin.readLine();
        this.title[1] = "Data read from file: " + this.inputFilename;
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        this.title[2] = "Program execution initiated at " + tim + " on " + day;

        // Read in number of items
        this.nItems = fin.readInt();
        if(fin.eol())lineNumber++;

        // Read in number of persons
        this.nPersons = fin.readInt();
        if(fin.eol())lineNumber++;
        this.nScores = this.nItems*this.nPersons;

        // Read in item names
        this.itemNames = new String[this.nItems+1];
        for(int i=0; i<this.nItems; i++){
            this.itemNames[i] = fin.readWord();
            if(fin.eol())lineNumber++;
        }
        this.itemNames[this.nItems] = "total";
        this.originalItemNames = this.itemNames;
        this.itemNamesSet = true;

        // Read in data to a String matrix
        String[][] scores = new String[this.nPersons][this.nItems];
        for(int i=0; i<this.nPersons; i++){
            int wordsPerLine = 1;
            for(int j=0; j<this.nItems; j++){
                scores[i][j] = fin.readWord();
                if(fin.eol()){
                    if(wordsPerLine!=this.nItems)throw new IllegalArgumentException("Line " + lineNumber + ": the number of scores in this row, " + wordsPerLine + ", does not equal the total number of items, " + this.nItems);
                    lineNumber++;
                }
                else{
                    wordsPerLine++;
                }
            }
        }
        fin.close();

        // Store entered data
        this.originalData = (Object)scores;
        this.originalDataType = 1;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Read scores from a text file as a matrix with rows of scores for each person
    // File name entered as argument
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // scores may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False, FALSE or a letter(if a letter to numeral conversion as beeen set - see letterToNumeral())
    // 'no responses' may be represented by any one word text except those used to represent a response
    public void readScoresAsRowPerPerson(String filename){

        //Select file and read in data
        int lineNumber = 1;
        this.inputFilename = filename;
        FileInput fin = new FileInput(filename);
        if(fin.eol())lineNumber++;

        // Set title
        this.title = new String[3];
        this.titleLines = 3;
        this.title[0] = "Title: " + fin.readLine();
        this.title[1] = "Data read from file: " + filename;
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        this.title[2] = "Program execution initiated at " + tim + " on " + day;

        // Read in number of items
        this.nItems = fin.readInt();
        if(fin.eol())lineNumber++;

        // Read in number of persons
        this.nPersons = fin.readInt();
        if(fin.eol())lineNumber++;
        this.nScores = this.nItems*this.nPersons;

        // Read in item names
        this.itemNames = new String[this.nItems+1];
        for(int i=0; i<this.nItems; i++){
            this.itemNames[i] = fin.readWord();
            if(fin.eol())lineNumber++;
        }
        this.itemNames[this.nItems] = "total";
        this.originalItemNames = this.itemNames;
        this.itemNamesSet = true;

        // Read in data to a String matrix
        String[][] scores = new String[this.nPersons][this.nItems];
        for(int i=0; i<this.nPersons; i++){
            int wordsPerLine = 1;
            for(int j=0; j<this.nItems; j++){
                scores[i][j] = fin.readWord();
                if(fin.eol()){
                    if(wordsPerLine!=this.nItems)throw new IllegalArgumentException("Line " + lineNumber + ": the number of scores in this row, " + wordsPerLine + ", does not equal the total number of items, " + this.nItems);
                    lineNumber++;
                }
                else{
                    wordsPerLine++;
                }

            }
        }
        fin.close();

        // Store entered data
        this.originalData = (Object)scores;
        this.originalDataType = 1;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each person - matrix of scores entered as String[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // scores may be represented by a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False or FALSE
    // 'no responses' may be represented by any one word text except that that corresponds to a number, yes, Yes, YES, no, No, NO, true, True, TRUE, false, False or FALSE
    public void enterScoresAsRowPerPerson(String[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

       // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 1;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }

    // A mistaken title retained for compatibility
    public void enterScoresAsRowPerIperson(String[][] scores){
        this.enterScoresAsRowPerPerson(scores);
    }



    // Enter scores as a matrix with rows of scores for each person - matrix of scores entered as double[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // 'no responses' must be represenred by Double.NaN
    public void enterScoresAsRowPerPerson(double[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 2;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each person - matrix of scores entered as Matrix
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // 'no responses' must be represenred by Double.NaN
    public void enterScoresAsRowPerPerson(Matrix scores){
        double[][] scoresdd = scores.getArrayCopy();

        // Determine number of items, persons and scores
        this.nPersons = scoresdd.length;
        this.nItems = scoresdd[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)scores.copy();
        this.originalDataType = 3;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }

    // Enter scores as a matrix with rows of scores for each person - matrix of scores entered as float[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // 'no responses' must be represenred by Float.NaN
    public void enterScoresAsRowPerPerson(float[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 4;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each person   -  matrix of scores entered as int[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // 'no responses' cannot be entered - see above for methods, e.g. matrix entered as String[][] or double[][], that allow no responses to be entered
    public void enterScoresAsRowPerPerson(int[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 5;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each person   -  matrix of scores entered as char[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // responses may be represented by a numeral (0 to 9), y, Y, n, N or a letter(if letter input as been set - see letterToNumeral())
    // 'no responses' may be represented by any character except those used to indicate a response
    public void enterScoresAsRowPerPerson(char[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 6;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }


    // Enter scores as a matrix with rows of scores for each person - scores either true  or false  -  matrix of scores entered as boolean[][]
    // e.g. scores1[0][0] to scores1[0][nItems-1] =  scores for each item in turn for the first person
    //      scores1[1][0] to scores1[1][nItems-1] =  scores for each item in turn for the second person
    // etc.
    // 'no responses' cannot be entered - see above for methods, e.g. matrix entered as String[][] or double[][], that allow no responses to be entered
    public void enterScoresAsRowPerPerson(boolean[][] scores){

        // Determine number of items, persons and scores
        this.nPersons = scores.length;
        this.nItems = scores[0].length;
        this.nScores = this.nItems*this.nPersons;
        this.dichotomous = new boolean[this.nItems];
        this.dichotomousPercentage = new double[this.nItems];
        for(int i=0; i<this.nItems; i++){
            this.dichotomous[i]=true;
            this.dichotomousPercentage[i]=100.0;
        }
        this.dichotomousOverall = true;
        this.dichotomousCheckDone = true;

        // Set title
        if(this.title==null){
            this.title = new String[2];
            this.title[0] = "Untitled Scores Analysis";
            Date d = new Date();
            String day = DateFormat.getDateInstance().format(d);
            String tim = DateFormat.getTimeInstance().format(d);
            this.title[1] = "Program execution initiated at " + tim + " on " + day;
        }

        // Store entered data
        this.originalData = (Object)Conv.copy(scores);
        this.originalDataType = 7;
        this.originalDataOrder = 1;
        this.dataEntered = true;
    }

    // Enter item names, i.e. one word item titles
    // default values are item1, item2, item3 etc.
    public void enterItemNames(String[] itemNames){
        int len = itemNames.length;
        this.itemNames = new String[len+1];
        for(int i=0; i<len; i++)this.itemNames[i] = itemNames[i];
        this.itemNames[len]="total";
        this.itemNamesSet = true;
    }

    //  Allow alphabetic responses, i.e. A, B, C, to be converted to numerical responses, i.e. 1, 2, 3, 4
    //  This is the default option (17 November 2010)
    // Need only be called to restore after suspensin - see immediately below
    public void letterToNumeral(){
        this.letterToNumeralSet = true;
    }

    //  Suspend alphabetic responses, i.e. A, B, C, conversion to numerical responses, i.e. 1, 2, 3, 4
    public void suspendLetterToNumeral(){
        this.letterToNumeralSet = false;
    }

    //  Allow other dichotomous data pairs than the default pairs,
    //  i.e. other than a numeral, (true, True, TRUE; false, Flase, FALSE), (Y, y, yes, Yes, YES;  N, n, no, No, NO)
    public void otherDichotomousData(String falseSign, String trueSign){
        this.otherFalse = falseSign;
        this.otherTrue = trueSign;
        this.otherDichotomousDataSet = true;
    }


    // SCORE MATRIX TRANSPOSTIONS
    // Transpose scores0 to scores1 - double[][]
    protected double[][] transpose0to1(double[][]scores00){
        int n0 = scores00.length;
        int n1 = scores00[0].length;
        double[][] scores11 = new double[n1][n0];
        for(int i=0; i<n0; i++){
            for(int j=0; j<n1; j++){
                scores11[j][i] = scores00[i][j];
            }
        }
        return scores11;
    }

    // Transpose scores0 to scores1 - String[][]
    protected String[][] transpose0to1(String[][]scores00){
        int n0 = scores00.length;
        int n1 = scores00[0].length;
        String[][] scores11 = new String[n1][n0];
        for(int i=0; i<n0; i++){
            for(int j=0; j<n1; j++){
                scores11[j][i] = scores00[i][j];
            }
        }
        return scores11;
    }

     // Transpose scores1 to scores0 - double[][]
    protected double[][] transpose1to0(double[][]scores11){
        int n0 = scores11.length;
        int n1 = scores11[0].length;
        double[][] scores00 = new double[n1][n0];
        for(int i=0; i<n0; i++){
            for(int j=0; j<n1; j++){
                scores00[j][i] = scores11[i][j];
            }
        }
        return scores00;
    }

    // Transpose scores1 to scores0 - String[][]
    protected String[][] transpose1to0(String[][]scores11){
        int n0 = scores11.length;
        int n1 = scores11[0].length;
        String[][] scores00 = new String[n1][n0];
        for(int i=0; i<n0; i++){
            for(int j=0; j<n1; j++){
                scores00[j][i] = scores11[i][j];
            }
        }
        return scores00;
    }

    // Transpose scores1 to scores0 - boolean[][]
    protected boolean[][] transpose1to0(boolean[][]scores11){
        int n0 = scores11.length;
        int n1 = scores11[0].length;
        boolean[][] scores00 = new boolean[n1][n0];
        for(int i=0; i<n0; i++){
            for(int j=0; j<n1; j++){
                scores00[j][i] = scores11[i][j];
            }
        }
        return scores00;
    }


    // CHECK FOR LENGTH CONSISTENCY IN ENTERED SCORES

    // check lengths of String[][]
    protected void checkLengths(String[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }

    // check lengths of double[][]
    protected void checkLengths(double[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }

    // check lengths of char[][]
    protected void checkLengths(char[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }

    // check lengths of float[][]
    protected void checkLengths(float[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }

    // check lengths of int[][]
    protected void checkLengths(int[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }

    // check lengths of boolean[][]
    protected void checkLengths(boolean[][] scores){
        int n0 = scores.length;
        int n1 = scores[0].length;
        for(int i=1; i<n0; i++){
            if(scores[i].length!=n1){
                throw new IllegalArgumentException("The length of each item and of each person's responses must be identical (missing responses must be included - see documentation web page)");
            }
        }
    }


    // TRIM SCORES ELEMENTS
    // Trim all elements of leading and trailing spaces
    protected void trimScores(String[][] scores){
        int n = scores.length;
        int m = scores[0].length;
        for(int i=0; i<n; i++){
            for(int j=0; j<m; j++){
                scores[i][j].trim();
            }
        }
    }


    // PREPROCESS DATA
    // Delete persons and items if required
    // Make substititions for any  non-deleted 'no responses'
    // Assign data to scores1 array and orginal scores array
    // Calculate standardized data
    // Call calculation of means, sums and variances
    protected void preprocessData(){

        if(!this.dataPreprocessed){
            if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");

            // Array initialization
            this.scores0 = new double[this.nItems][this.nPersons];
            this.originalScores0 = new double[this.nItems][this.nPersons];
            this.scores1 = new double[this.nPersons][this.nItems];
            this.originalScores1 = new double[this.nPersons][this.nItems];
            this.deletedPersons = new boolean[this.nPersons];
            this.deletedItems = new boolean[this.nItems];
            this.personIndices = new int[this.nPersons];
            for(int i=0; i<this.nPersons; i++)this.personIndices[i] = i;
            this.itemIndices = new int[this.nItems];
            for(int i=0; i<this.nItems; i++)this.itemIndices[i] = i;


            // instance variable initialization
            this.nNaN = 0;
            this.nDeletedPersons = 0;
            this.nDeletedItems = 0;

            // Title names
            if(this.itemNamesSet){
                if((this.nItems+1)!=this.itemNames.length)throw new IllegalArgumentException("The number of item names, " + this.itemNames.length + ", does not equal the number of items, " + this.nItems);
            }
            else{
                this.itemNames = new String[this.nItems+1];
                for(int i=0; i<this.nItems; i++)this.itemNames[i] = "item" + i;
                this.itemNames[this.nItems] = "total";
            }

            // Recover entered data as String, double or boolean arrays and transpose entered scores1 format to scores0 format
            String[][] holdingArrayS = null;
            double[][] holdingArrayD = null;
            boolean[][] holdingArrayB = null;
            int m = 0;
            int n = 0;
            switch(this.originalDataType){
                case 1: holdingArrayS = (String[][])originalData;
                        this.checkLengths(holdingArrayS);
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayS = this.transpose1to0(holdingArrayS);
                        }
                        this.trimScores(holdingArrayS);
                        break;
                case 2: holdingArrayD = (double[][])originalData;
                        this.checkLengths(holdingArrayD);
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayD = this.transpose1to0(holdingArrayD);
                        }
                        holdingArrayS = this.dataToString(holdingArrayD);
                        break;
                case 3: holdingArrayD = ((Matrix)originalData).getArrayCopy();
                        this.checkLengths(holdingArrayD);
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayD = this.transpose1to0(holdingArrayD);
                        }
                        holdingArrayS = this.dataToString(holdingArrayD);
                        break;
                case 4: float[][] holdingArrayF = (float[][])originalData;
                        this.checkLengths(holdingArrayF);
                        // convert to double[][]
                        m = holdingArrayF.length;
                        n = holdingArrayF[0].length;
                        for(int i=0; i<m; i++){
                            for(int j=0; j<n; j++){
                                holdingArrayD[i][j] = (new Float(holdingArrayF[i][j])).doubleValue();
                            }
                        }
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayD = this.transpose1to0(holdingArrayD);
                        }
                        holdingArrayS = this.dataToString(holdingArrayD);
                        break;
                case 5: int[][] holdingArrayI = (int[][])originalData;
                        this.checkLengths(holdingArrayI);
                        // convert to double[][]
                        m = holdingArrayI.length;
                        n = holdingArrayI[0].length;
                        for(int i=0; i<m; i++){
                            for(int j=0; j<n; j++){
                                holdingArrayD[i][j] = (new Integer(holdingArrayI[i][j])).doubleValue();
                            }
                        }
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayD = this.transpose1to0(holdingArrayD);
                        }
                        holdingArrayS = this.dataToString(holdingArrayD);
                        break;
                case 6: char[][] holdingArrayC = (char[][])originalData;
                        this.checkLengths(holdingArrayC);
                        // convert to String[][]
                        m = holdingArrayC.length;
                        n = holdingArrayC[0].length;
                        holdingArrayS = new String[m][n];
                        for(int i=0; i<m; i++){
                            for(int j=0; j<n; j++){
                                holdingArrayS[i][j] = Character.toString(holdingArrayC[i][j]);
                            }
                        }
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayS = this.transpose1to0(holdingArrayS);
                        }
                        this.trimScores(holdingArrayS);
                        break;
                case 7: holdingArrayB = (boolean[][])originalData;
                        this.checkLengths(holdingArrayB);
                        // transpose to scores0 format
                        if(this.originalDataOrder==1){
                            holdingArrayB = this.transpose1to0(holdingArrayB);
                        }
                        holdingArrayS = this.dataToString(holdingArrayB);
                        break;
                default: throw new IllegalArgumentException("Original data type, " + originalDataType + ", not recognised");
            }

            // Check for y-n dichotomous pair
            if(this.letterToNumeralSet){
                for(int i=0; i<this.nItems; i++){
                    int nYN = 0;
                    for(int j=0; j<this.nPersons; j++){
                        char elem = holdingArrayS[i][j].charAt(0);
                        if((elem=='y' || elem=='n' || elem=='Y' || elem=='N' || elem==' ') && holdingArrayS[i][j].length()==1){
                            nYN++;
                        }
                    }
                    if(nYN==this.nPersons){
                        for(int j=0; j<this.nPersons; j++){
                            char elem = holdingArrayS[i][j].charAt(0);
                            if((elem=='y' || elem=='Y') && holdingArrayS[i][j].length()==1){
                                holdingArrayS[i][j] = "1";
                            }
                            else{
                                if((elem=='n' || elem=='N') && holdingArrayS[i][j].length()==1){
                                    holdingArrayS[i][j] = "-1";
                                }
                            }
                        }
                    }
                }
            }

            // Convert letters to numbers (modification added on 1 November 2010)
            if(this.letterToNumeralSet){
                for(int i=0; i<this.nItems; i++){
                    for(int j=0; j<this.nPersons; j++){
                        char elem = holdingArrayS[i][j].charAt(0);
                        if((int)elem>64 && elem<91 && holdingArrayS[i][j].length()==1){
                            holdingArrayS[i][j] = "" + ((int)elem - 63);
                        }
                        else{
                            if((int)elem>96 && elem<123 && holdingArrayS[i][j].length()==1){
                                holdingArrayS[i][j] = "" + ((int)elem - 96);
                            }
                        }
                    }
                }
                this.letterToNumeralSet = false;
            }


            // Identify 'no responses' (->NaN)and convert all to double[][]
            switch(this.originalDataType){
                case 1:
                case 6: for(int i=0; i<this.nItems; i++){
                            for(int j=0; j<this.nPersons; j++){
                                boolean elementSet = false;
                                if(this.otherDichotomousDataSet){
                                        if(holdingArrayS[i][j].equalsIgnoreCase(this.otherTrue)){
                                            this.scores0[i][j]=1;
                                            elementSet = true;
                                        }
                                        else{
                                            if(holdingArrayS[i][j].equalsIgnoreCase(this.otherFalse)){
                                                this.scores0[i][j]=-1;
                                                elementSet = true;
                                            }
                                            else{
                                               this.scores0[i][j] = Double.NaN;
                                               elementSet = true;
                                            }
                                        }
                                }
                                if(!elementSet){
                                    if(holdingArrayS[i][j].equalsIgnoreCase("yes") || holdingArrayS[i][j].equalsIgnoreCase("y") || holdingArrayS[i][j].equalsIgnoreCase("true")){
                                        this.scores0[i][j]=1;
                                        elementSet = true;
                                    }
                                    else{
                                        if(holdingArrayS[i][j].equalsIgnoreCase("no") || holdingArrayS[i][j].equalsIgnoreCase("n") || holdingArrayS[i][j].equalsIgnoreCase("false")){
                                            this.scores0[i][j]=-1;
                                            elementSet = true;
                                        }
                                    }
                                }
                                if(!elementSet){
                                    try{
                                        this.scores0[i][j] = Double.valueOf(holdingArrayS[i][j]);
                                    }
                                    catch (Exception e){
                                        this.scores0[i][j] = Double.NaN;
                                        this.nNaN++;
                                    }
                                }
                            }
                        }
                        break;
                case 2:
                case 3:
                case 4:
                case 5: for(int i=0; i<this.nItems; i++){
                            for(int j=0; j<this.nPersons; j++){
                                try{
                                    this.scores0[i][j] = Double.valueOf(holdingArrayD[i][j]);
                                    }
                                catch (Exception e){
                                    this.scores0[i][j] = Double.NaN;
                                    this.nNaN++;
                                }
                            }
                        }
                        break;
                case 7: for(int i=0; i<this.nItems; i++){
                            for(int j=0; j<this.nPersons; j++){
                                if(holdingArrayB[i][j]){
                                    this.scores0[i][j] = 1.0;
                                }
                                else{
                                    this.scores0[i][j] = 0.0;
                                }
                            }
                        }
                        break;

            }

            // Check maximum precision of entered data
            int maxPrec = 0;
            int prec0 = 0;
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nPersons; j++){
                    prec0 = Fmath.checkPrecision(scores0[i][j]);
                    if(prec0>maxPrec)maxPrec = prec0;
                }
            }
            if(maxPrec>this.trunc && !this.truncAll)this.trunc = maxPrec;

            // assign original scores to instance variable
            this.originalScores0 = Conv.copy(scores0);
            this.originalScores1 = this.transpose0to1(scores0);
            this.originalNitems = this.nItems;
            this.originalNpersons = this.nPersons;
            this.originalNscores = this.originalNitems*this.originalNpersons;

            // Handle no responses
            // Check for and carry out item deletion
            // check for and carry out no response replacement
            if(this.nNaN>0){
                this.noResponseHandling();
                this.scores1 = this.transpose0to1(this.scores0);
            }


            // Create row - column transposed matrix
            this.scores1 = new double[this.nPersons][this.nItems];
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nPersons; j++){
                    this.scores1[j][i] = this.scores0[i][j];
                }
            }

            // Check which original raw data items are dichotomous
            this.checkWhetherRawItemsDichotomous();

            // Standardize data in all items
            this.standardizedScores0 = new double[this.nItems][this.nPersons];
            this.standardizedScores1 = new double[this.nPersons][this.nItems];
            for(int i=0; i<this.nItems; i++){
                Stat st = new Stat(this.scores0[i]);
                this.standardizedScores0[i] = st.standardize();
            }
            this.standardizedScores1 = this.transpose0to1(this.standardizedScores0);

            // Check for row or column containing identical elements
            this.checkForIdenticalElements();

            // Calculate means, standard deviations and variances of all items and all persons sets
            this.meansAndVariances();

            // Calculate covariances and correlation coefficients
            this.covariancesAndCorrelationCoefficients();

            this.dataPreprocessed = true;
        }
    }

    // Check if row or column of all zeros in the data
    private void checkForIdenticalElements(){

        boolean test = false;
        for(int i=0; i<this.nItems; i++){
            int sum = 0;
            double check = this.scores0[i][0];
            for(int j=0; j<this.nPersons; j++)if(this.scores0[i][j]==check)sum++;
            if(sum==this.nPersons){
                this.sameCheck = 1;
                test = true;
            }
        }

        for(int i=0; i<this.nPersons; i++){
            int sum = 0;
            double check = this.scores0[0][i];
            for(int j=0; j<this.nItems; j++)if(this.scores0[j][i]==check)sum++;
            if(sum==this.nItems){
                this.sameCheck = 2;
                if(test)this.sameCheck = 3;
            }
        }
    }

    // Convert double data to String data
    private String[][] dataToString(double[][] ddata){
            int nn = ddata.length;
            int mm = ddata[0].length;
            String[][] sdata = new String[nn][mm];
            for(int i=0; i<nn; i++){
                for(int j=0; j<mm; j++){
                    sdata[i][j] = (new Double(ddata[i][j])).toString();
                }
            }
            return sdata;
    }

    // Convert boolean data to String data
    private String[][] dataToString(boolean[][] ddata){
            int nn = ddata.length;
            int mm = ddata[0].length;
            String[][] sdata = new String[nn][mm];
            for(int i=0; i<nn; i++){
            for(int j=0; j<mm; j++){
                    sdata[i][j] = (new Boolean(ddata[i][j])).toString();
                }
            }
            return sdata;
    }

    // RETURN RESPONSES
    // Return responses as entered
    public Object originalResponses(){
        return originalScores();
    }

    public Object originalScores(){
        if(!this.dataEntered)throw new IllegalArgumentException("No data has been entered");
        return this.originalData;
    }

    // Return original data as rows per person
    public double[][] originalResponsesAsRowPerPerson(){
        return originalScoresAsRowPerPerson();
    }

    public double[][] originalScoresAsRowPerPerson(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalScores1;
    }

    // Return original data as rows per item
    public double[][] originalResponsesAsRowPerItem(){
        return originalScoresAsRowPerItem();
    }

    public double[][] originalScoresAsRowPerItem(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalScores0;
    }

    // Return used data as rows per person, i.e. data after any deletions
    public double[][] usedresponsesAsRowPerPerson(){
        return usedScoresAsRowPerPerson();
    }

    public double[][] usedScoresAsRowPerPerson(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.scores1;
    }

    // Return used data as rows per item, i.e. data after any deletions
    public double[][] usedScoresAsRowPerItem(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.scores0;
    }

    // Return standardized data as rows per person
    public double[][] standardizedScoresAsRowPerPerson(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.standardizedScores1;
    }

    public double[][] standardisedScoresAsRowPerPerson(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.standardizedScores1;
    }

    // Return standardized data as rows per item
    public double[][] standardizedScoresAsRowPerItem(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.standardizedScores0;
    }

    public double[][] standardisedScoresAsRowPerItem(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.standardizedScores0;
    }

    // Return original number of items
    public int originalNumberOfItems(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalNitems;
    }

    // Return original number of persons
    public int originalNumberOfPersons(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalNpersons;
    }

    // Return used number of items, i.e. after any deletions
    public int usedNumberOfItems(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nItems;
    }

    // Return used number of persons, i.e. after any deletions
    public int usedNumberOfPersons(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nPersons;
    }


    // Return original total number of responses
    public int originalTotalNumberOfScores(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalNscores;
    }

    // Return original total number of responses, i.e. after any deletions
    public int usedTotalNumberOfScores(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nScores;
    }

    // Return number of responses deleted
    public int numberOfDeletedScores(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.originalNscores - this.nScores;
    }

    // Return number of responses replaced
    public int numberOfReplacedScores(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.nReplacements;
    }

    // Return item name and person index of all replaced responses
    public String[] indicesOfReplacedScores(){
        if(!this.dataPreprocessed)this.preprocessData();
        return this.replacementIndices;
    }

    //  Get item names
    public String[] itemNames(){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        String[] ret = new String[this.nItems];
        for(int i=0; i<this.nItems; i++)ret[i] = this.itemNames[i];
        return ret;
    }

    public String[] originalItemNames(){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        String[] ret = new String[this.originalNitems];
        for(int i=0; i<this.originalNitems; i++)ret[i] = this.originalItemNames[i];
        return ret;
    }


    //  Get index for a given item name
    public int itemIndex(String itemName){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        int index = -1;
        int jj=0;
        boolean test = true;
        while(test){
            if(itemName.trim().equalsIgnoreCase(this.itemNames[jj].trim())){
                index = jj;
                test = false;
            }
            else{
                jj++;
                if(jj>this.nItems)throw new IllegalArgumentException("Item name, " + itemName + ", is not present in the list of entered item names");
            }
        }
        return index+1;
    }

    //  Get item name for a given index
    public String itemName(int index){
        if(!this.dataEntered)throw new IllegalArgumentException("no data has been entered");
        return this.itemNames[index-1];
    }


    // SUMS, MEANS, VARIANCES, STANDARD DEVIATION, MEDIANS, MAXIMA AND MINIMA
    // Calculate item and person sums, means, variances, standard deviations, mimima and maxima
    // plus same for total responses
    protected void meansAndVariances(){

        // ITEMS
        this.rawItemMeans = new double[this.nItems];
        this.rawItemMedians = new double[this.nItems];
        this.rawItemStandardDeviations = new double[this.nItems];
        this.rawItemVariances = new double[this.nItems];
        this.rawItemMinima = new double[this.nItems];
        this.rawItemMaxima = new double[this.nItems];
        this.rawItemRanges = new double[this.nItems];
        this.rawItemTotals = new double[this.nItems];
        this.rawItemMomentSkewness = new double[this.nItems];
        this.rawItemMedianSkewness = new double[this.nItems];
        this.rawItemQuartileSkewness = new double[this.nItems];
        this.rawItemKurtosisExcess = new double[this.nItems];

        for(int i=0; i<this.nItems; i++){
            Stat am0 = new Stat(this.scores0[i]);
            if(this.nFactorOption){
                am0.setDenominatorToN();
            }
            else{
                am0.setDenominatorToNminusOne();
            }

            this.rawItemMeans[i] = am0.mean_as_double();
            this.rawItemVariances[i] = am0.variance_as_double();
            this.rawItemStandardDeviations[i] = Math.sqrt(this.rawItemVariances[i]);
            this.rawItemMinima[i] = am0.minimum_as_double();
            this.rawItemMaxima[i] = am0.maximum_as_double();
            this.rawItemRanges[i] = this.rawItemMaxima[i] - this.rawItemMinima[i];
            this.rawItemTotals[i] = am0.sum_as_double();
            ArrayMaths ams0 = am0.sort();
            Stat ss = new Stat(ams0.array());
            this.rawItemMedians[i] = ss.median_as_double();
            this.rawItemMomentSkewness[i] = am0.momentSkewness_as_double();
            this.rawItemMedianSkewness[i] = am0.medianSkewness_as_double();
            this.rawItemQuartileSkewness[i] = am0.quartileSkewness_as_double();
            this.rawItemKurtosisExcess[i] = am0.kurtosisExcess_as_double();
        }

        Stat st = new Stat(this.rawItemMeans);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemMeansMean = st.mean_as_double();
        this.rawItemMeansVar = st.variance_as_double();
        this.rawItemMeansSd = Math.sqrt(this.rawItemMeansVar);
        this.rawItemMeansMin = st.minimum_as_double();
        this.rawItemMeansMax = st.maximum_as_double();
        this.rawItemMeansRange = this.rawItemMeansMax - this.rawItemMeansMin;

        st = new Stat(this.rawItemStandardDeviations);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemStandardDeviationsMean = st.mean_as_double();
        this.rawItemStandardDeviationsVar = st.variance_as_double();
        this.rawItemStandardDeviationsSd = Math.sqrt(this.rawItemStandardDeviationsVar);
        this.rawItemStandardDeviationsMin = st.minimum_as_double();
        this.rawItemStandardDeviationsMax = st.maximum_as_double();
        this.rawItemStandardDeviationsRange = this.rawItemStandardDeviationsMax - this.rawItemStandardDeviationsMin;

        st = new Stat(this.rawItemVariances);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemVariancesMean = st.mean_as_double();
        this.rawItemVariancesVar = st.variance_as_double();
        this.rawItemVariancesSd = Math.sqrt(this.rawItemVariancesVar);
        this.rawItemVariancesMin = st.minimum_as_double();
        this.rawItemVariancesMax = st.maximum_as_double();
        this.rawItemVariancesRange = this.rawItemVariancesMax - this.rawItemVariancesMin;

        st = new Stat(this.rawItemMinima);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemMinimaMean = st.mean_as_double();
        this.rawItemMinimaVar = st.variance_as_double();
        this.rawItemMinimaSd = Math.sqrt(this.rawItemMinimaVar);
        this.rawItemMinimaMin = st.minimum_as_double();
        this.rawItemMinimaMax = st.maximum_as_double();
        this.rawItemMinimaRange = this.rawItemMinimaMax - this.rawItemMinimaMin;

        st = new Stat(this.rawItemMaxima);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemMaximaMean = st.mean_as_double();
        this.rawItemMaximaVar = st.variance_as_double();
        this.rawItemMaximaSd = Math.sqrt(this.rawItemMaximaVar);
        this.rawItemMaximaMin = st.minimum_as_double();
        this.rawItemMaximaMax = st.maximum_as_double();
        this.rawItemMaximaRange = this.rawItemMaximaMax - this.rawItemMaximaMin;

        st = new Stat(this.rawItemRanges);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemRangesMean = st.mean_as_double();
        this.rawItemRangesVar = st.variance_as_double();
        this.rawItemRangesSd = Math.sqrt(this.rawItemRangesVar);
        this.rawItemRangesMin = st.minimum_as_double();
        this.rawItemRangesMax = st.maximum_as_double();
        this.rawItemRangesRange = this.rawItemRangesMax - this.rawItemRangesMin;

        st = new Stat(this.rawItemTotals);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemTotalsMean = st.mean_as_double();
        this.rawItemTotalsVar = st.variance_as_double();
        this.rawItemTotalsSd = Math.sqrt(this.rawItemTotalsVar);
        this.rawItemTotalsMin = st.minimum_as_double();
        this.rawItemTotalsMax = st.maximum_as_double();
        this.rawItemTotalsRange = this.rawItemTotalsMax - this.rawItemTotalsMin;

        st = new Stat(this.rawItemMedians);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawItemMediansMean = st.mean_as_double();
        this.rawItemMediansVar = st.variance_as_double();
        this.rawItemMediansSd = Math.sqrt(this.rawItemMediansVar);
        this.rawItemMediansMin = st.minimum_as_double();
        this.rawItemMediansMax = st.maximum_as_double();
        this.rawItemMediansRange = this.rawItemMediansMax - this.rawItemMediansMin;

        this.standardizedItemMeans = new double[this.nItems];
        this.standardizedItemMedians = new double[this.nItems];
        this.standardizedItemStandardDeviations = new double[this.nItems];
        this.standardizedItemVariances = new double[this.nItems];
        this.standardizedItemMinima = new double[this.nItems];
        this.standardizedItemMaxima = new double[this.nItems];
        this.standardizedItemRanges = new double[this.nItems];
        this.standardizedItemTotals = new double[this.nItems];
        this.standardizedItemMomentSkewness = new double[this.nItems];
        this.standardizedItemMedianSkewness = new double[this.nItems];
        this.standardizedItemQuartileSkewness = new double[this.nItems];
        this.standardizedItemKurtosisExcess = new double[this.nItems];

        for(int i=0; i<this.nItems; i++){
            Stat ams0 = new Stat(this.standardizedScores0[i]);
            if(this.nFactorOption){
                ams0.setDenominatorToN();
            }
            else{
                ams0.setDenominatorToNminusOne();
            }
            this.standardizedItemMeans[i] = 0.0;
            this.standardizedItemVariances[i] = 1.0;
            this.standardizedItemStandardDeviations[i] = 1.0;
            this.standardizedItemMinima[i] = ams0.minimum_as_double();
            this.standardizedItemMaxima[i] = ams0.maximum_as_double();
            this.standardizedItemRanges[i] = this.standardizedItemMaxima[i] - this.standardizedItemMinima[i];
            this.standardizedItemTotals[i] = 0.0;            
            ArrayMaths amss0 = ams0.sort();
            Stat ss = new Stat(amss0.array());           
            this.standardizedItemMedians[i] = ss.median_as_double();
            this.standardizedItemMomentSkewness[i] = ams0.momentSkewness_as_double();
            this.standardizedItemMedianSkewness[i] = ams0.medianSkewness_as_double();
            this.standardizedItemQuartileSkewness[i] = ams0.quartileSkewness_as_double();
            this.standardizedItemKurtosisExcess[i] = ams0.kurtosisExcess_as_double();
        }


        st = new Stat(this.standardizedItemMeans);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemMeansMean = st.mean_as_double();
        this.standardizedItemMeansVar = st.variance_as_double();
        this.standardizedItemMeansSd = Math.sqrt(this.standardizedItemMeansVar);
        this.standardizedItemMeansMin = st.minimum_as_double();
        this.standardizedItemMeansMax = st.maximum_as_double();
        this.standardizedItemMeansRange = this.standardizedItemMeansMax - this.standardizedItemMeansMin;

        st = new Stat(this.standardizedItemStandardDeviations);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemStandardDeviationsMean = st.mean_as_double();
        this.standardizedItemStandardDeviationsVar = st.variance_as_double();
        this.standardizedItemStandardDeviationsSd = Math.sqrt(this.standardizedItemStandardDeviationsVar);
        this.standardizedItemStandardDeviationsMin = st.minimum_as_double();
        this.standardizedItemStandardDeviationsMax = st.maximum_as_double();
        this.standardizedItemStandardDeviationsRange = this.standardizedItemStandardDeviationsMax - this.standardizedItemStandardDeviationsMin;

        st = new Stat(this.standardizedItemVariances);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemVariancesMean = st.mean_as_double();
        this.standardizedItemVariancesVar = st.variance_as_double();
        this.standardizedItemVariancesSd = Math.sqrt(this.standardizedItemVariancesVar);
        this.standardizedItemVariancesMin = st.minimum_as_double();
        this.standardizedItemVariancesMax = st.maximum_as_double();
        this.standardizedItemVariancesRange = this.standardizedItemVariancesMax - this.standardizedItemVariancesMin;

        st = new Stat(this.standardizedItemMinima);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemMinimaMean = st.mean_as_double();
        this.standardizedItemMinimaVar = st.variance_as_double();
        this.standardizedItemMinimaSd = Math.sqrt(this.standardizedItemMinimaVar);
        this.standardizedItemMinimaMin = st.minimum_as_double();
        this.standardizedItemMinimaMax = st.maximum_as_double();
        this.standardizedItemMinimaRange = this.standardizedItemMinimaMax - this.standardizedItemMinimaMin;

        st = new Stat(this.standardizedItemMaxima);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemMaximaMean = st.mean_as_double();
        this.standardizedItemMaximaVar = st.variance_as_double();
        this.standardizedItemMaximaSd = Math.sqrt(this.standardizedItemMaximaVar);
        this.standardizedItemMaximaMin = st.minimum_as_double();
        this.standardizedItemMaximaMax = st.maximum_as_double();
        this.standardizedItemMaximaRange = this.standardizedItemMaximaMax - this.standardizedItemMaximaMin;

        st = new Stat(this.standardizedItemRanges);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemRangesMean = st.mean_as_double();
        this.standardizedItemRangesVar = st.variance_as_double();
        this.standardizedItemRangesSd = Math.sqrt(this.standardizedItemRangesVar);
        this.standardizedItemRangesMin = st.minimum_as_double();
        this.standardizedItemRangesMax = st.maximum_as_double();
        this.standardizedItemRangesRange = this.standardizedItemRangesMax - this.standardizedItemRangesMin;

        this.standardizedItemTotalsMean = 0.0;
        this.standardizedItemTotalsVar = 0.0;
        this.standardizedItemTotalsSd = 0.0;
        this.standardizedItemTotalsMin = 0.0;
        this.standardizedItemTotalsMax = 0.0;
        this.standardizedItemTotalsRange = 0.0;

        st = new Stat(this.standardizedItemMedians);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedItemMediansMean = st.mean_as_double();
        this.standardizedItemMediansVar = st.variance_as_double();
        this.standardizedItemMediansSd = Math.sqrt(this.standardizedItemMediansVar);
        this.standardizedItemMediansMin = st.minimum_as_double();
        this.standardizedItemMediansMax = st.maximum_as_double();
        this.standardizedItemMediansRange = this.standardizedItemMediansMax - this.standardizedItemMediansMin;

        // INDIVIDUALS
        this.rawPersonMeans = new double[this.nPersons];
        this.rawPersonStandardDeviations = new double[this.nPersons];
        this.rawPersonVariances = new double[this.nPersons];
        this.rawPersonMinima = new double[this.nPersons];
        this.rawPersonMaxima = new double[this.nPersons];
        this.rawPersonRanges = new double[this.nPersons];
        this.rawPersonTotals = new double[this.nPersons];
        Stat[] am1 = new Stat[this.nPersons];
        for(int i=0; i<this.nPersons; i++){
            am1[i] = new Stat(this.scores1[i]);
            if(this.nFactorOption){
                am1[i].setDenominatorToN();
            }
            else{
                am1[i].setDenominatorToNminusOne();
            }
            this.rawPersonMeans[i] = am1[i].mean_as_double();
            this.rawPersonVariances[i] = am1[i].variance_as_double();
            this.rawPersonStandardDeviations[i] = Math.sqrt(this.rawPersonVariances[i]);
            this.rawPersonMinima[i] = am1[i].minimum_as_double();
            this.rawPersonMaxima[i] = am1[i].maximum_as_double();
            this.rawPersonRanges[i] = this.rawPersonMaxima[i] - this.rawPersonMinima[i];
            this.rawPersonTotals[i] = am1[i].sum_as_double();
        }

        this.standardizedPersonMeans = new double[this.nPersons];
        this.standardizedPersonStandardDeviations = new double[this.nPersons];
        this.standardizedPersonVariances = new double[this.nPersons];
        this.standardizedPersonMinima = new double[this.nPersons];
        this.standardizedPersonMaxima = new double[this.nPersons];
        this.standardizedPersonRanges = new double[this.nPersons];
        this.standardizedPersonTotals = new double[this.nPersons];
        Stat[] ams1 = new Stat[this.nPersons];
        for(int i=0; i<this.nPersons; i++){
            ams1[i] = new Stat(this.standardizedScores1[i]);
            if(this.nFactorOption){
                ams1[i].setDenominatorToN();
            }
            else{
                ams1[i].setDenominatorToNminusOne();
            }
            this.standardizedPersonMeans[i] = ams1[i].mean_as_double();
            this.standardizedPersonVariances[i] = ams1[i].variance_as_double();
            this.standardizedPersonStandardDeviations[i] = Math.sqrt(this.standardizedPersonVariances[i]);
            this.standardizedPersonMinima[i] = ams1[i].minimum_as_double();
            this.standardizedPersonMaxima[i] = ams1[i].maximum_as_double();
            this.standardizedPersonRanges[i] = this.standardizedPersonMaxima[i] - this.standardizedPersonMinima[i];
            this.standardizedPersonTotals[i] = ams1[i].sum_as_double();
        }


        // TOTAL
        ArrayMaths am = new ArrayMaths(this.scores0[0]);
        for(int i=1; i<this.nItems; i++){
            am = am.concatenate(this.scores0[i]);
        }
        
        Stat ams = new Stat(am.array());
        if(this.nFactorOption){
            ams.setDenominatorToN();
        }
        else{
            ams.setDenominatorToNminusOne();
        }
        this.rawAllResponsesMean = ams.mean_as_double();
        this.rawAllResponsesVariance = ams.variance_as_double();
        this.rawAllResponsesStandardDeviation = Math.sqrt(this.rawAllResponsesVariance);
        this.rawAllResponsesMinimum = ams.minimum_as_double();
        this.rawAllResponsesMaximum = ams.maximum_as_double();
        this.rawAllResponsesRange = this.rawAllResponsesMaximum - this.rawAllResponsesMinimum;
        this.rawAllResponsesTotal = ams.sum_as_double();

        ArrayMaths amm = new ArrayMaths(this.standardizedScores0[0]);
        for(int i=1; i<this.nItems; i++){
            amm = amm.concatenate(this.standardizedScores0[i]);
        }
        Stat amss = new Stat(amm.array());
        if(this.nFactorOption){
            amss.setDenominatorToN();
        }
        else{
            amss.setDenominatorToNminusOne();
        }
        this.standardizedAllResponsesMean = amss.mean_as_double();
        this.standardizedAllResponsesVariance = amss.variance_as_double();
        this.standardizedAllResponsesStandardDeviation = Math.sqrt(this.standardizedAllResponsesVariance);
        this.standardizedAllResponsesMinimum = amss.minimum_as_double();
        this.standardizedAllResponsesMaximum = amss.maximum_as_double();
        this.standardizedAllResponsesRange = this.standardizedAllResponsesMaximum - this.standardizedAllResponsesMinimum;
        this.standardizedAllResponsesTotal = 0.0;

        this.variancesCalculated = true;
    }

    // Get raw data item means
    public double[] rawItemMeans(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMeans;
    }

    // Get standardized data item means
    public double[] standardizedItemMeans(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMeans;
    }

    public double[] standardisedItemMeans(){
        return this.standardizedItemMeans();
    }

    // Get a raw data item mean
    public double rawItemMean(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMeans[index-1];
    }

    // Get a raw data item mean
    public double rawItemMean(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMeans[index-1];
    }

    // Get mean of the raw data item means
    public double rawMeanOfItemMeans(){
        return rawItemMeansMean;
    }

    // Get standard deviation of the raw data item means
    public double rawStandardDeviationOfItemMeans(){
        return rawItemMeansSd;
    }


    // Get variance of the raw data item means
    public double rawVarianceOfItemMeans(){
        return rawItemMeansVar;
    }

    // Get maximum of the raw data item means
    public double rawMaximumOfItemMeans(){
        return rawItemMeansMax;
    }

    // Get minimum of the raw data item means
    public double rawMinimumOfItemMeans(){
        return rawItemMeansMin;
    }

    // Get range of the raw data item means
    public double rawRangeOfItemMeans(){
        return rawItemMeansRange;
    }

    // Get a standardized data item mean
    public double standardizedItemMean(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMeans[index-1];
    }

    public double standardisedItemMean(String itemName){
        return this.standardizedItemMean(itemName);
    }

    // Get a standardized data item mean
    public double standardizedItemMean(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMeans[index-1];
    }

    public double standardisedItemMean(int index){
        return this.standardizedItemMean(index);
    }

    // Get mean of the standardized data item means
    public double standardizedMeanOfItemMeans(){
        return standardizedItemMeansMean;
    }

    public double standardisedMeanOfItemMeans(){
        return standardizedItemMeansMean;
    }

    // Get standard deviation of the standardized data item means
    public double standardizedStanadarDeviationOfItemMeans(){
        return standardizedItemMeansSd;
    }

    public double standardisedStanadarDeviationOfItemMeans(){
        return standardizedItemMeansSd;
    }

    // Get variance of the standardized data item means
    public double standardizedVarianceOfItemMeans(){
        return standardizedItemMeansVar;
    }

    // Get maximum of the standardized data item means
    public double standardizedMaximumOfItemMeans(){
        return standardizedItemMeansMax;
    }

    public double standardisedVarianceOfItemMeans(){
        return standardizedItemMeansVar;
    }

    // Get minimum of the standardized data item means
    public double standardizedMinimumOfItemMeans(){
        return standardizedItemMeansMin;
    }

    public double standardisedMinimumOfItemMeans(){
        return standardizedItemMeansMin;
    }

    // Get range of the standardized data item means
    public double standardizedRangeOfItemMeans(){
        return standardizedItemMeansRange;
    }

    public double standardisedRangeOfItemMeans(){
        return standardizedItemMeansRange;
    }

    // Get raw data item standard deviations
    public double[] rawItemStandardDeviations(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemStandardDeviations;
    }

    // Get a raw data item standard deviation
    public double rawItemStandardDeviation(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemStandardDeviations[index-1];
    }

    // Get a raw data item standard deviation
    public double rawItemStandardDeviation(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemStandardDeviations[index-1];
    }

    // Get mean of the raw data item standard deviation
    public double rawMeanOfItemStandardDeviations(){
        return rawItemStandardDeviationsMean;
    }

    // Get standard deviation of the raw data item standard deviations
    public double rawStanadarDeviationOfItemStandardDeviations(){
        return rawItemStandardDeviationsSd;
    }

    // Get variance of the raw data item standard deviations
    public double rawVarianceOfItemStandardDeviations(){
        return rawItemStandardDeviationsVar;
    }

    // Get maximum of the raw data item standard deviations
    public double rawMaximumOfItemStandardDeviations(){
        return rawItemStandardDeviationsMax;
    }

    // Get minimum of the raw data item standard deviations
    public double rawMinimumOfItemStandardDeviations(){
        return rawItemStandardDeviationsMin;
    }

    // Get range of the raw data item standard deviations
    public double rawRangeOfItemStandardDeviations(){
        return rawItemStandardDeviationsRange;
    }

    // Get standardized data item standard deviations
    public double[] standardizedItemStandardDeviations(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemStandardDeviations;
    }

    public double[] standardisedItemStandardDeviations(){
        return this.standardizedItemStandardDeviations();
    }

    // Get a standardized data item standard deviation
    public double standardizedItemStandardDeviation(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemStandardDeviations[index-1];
    }

    public double standardisedItemStandardDeviation(String itemName){
        return this.standardizedItemStandardDeviation(itemName);
    }

    // Get a standardized data item standard deviation
    public double standardizedItemStandardDeviation(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemStandardDeviations[index-1];
    }

    public double standardisedItemStandardDeviation(int index){
        return this.standardizedItemStandardDeviation(index);
    }

    // Get mean of the standardized data item standard deviations
    public double standardizedMeanOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMean;
    }

    public double standardisedMeanOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMean;
    }

    // Get standard deviation of the standardized data item standard deviations
    public double standardizedStanadarDeviationOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsSd;
    }

    public double standardisedStanadarDeviationOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsSd;
    }

    // Get variance of the standardized data item standard deviations
    public double standardizedVarianceOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsVar;
    }

    public double standardisedVarianceOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsVar;
    }

    // Get maximum of the standardized data item standard deviations
    public double standardizedMaximumOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMax;
    }

    public double standardisedMaximumOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMax;
    }

    // Get minimum of the standardized data item standard deviations
    public double standardizedMinimumOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMin;
    }

    public double standardisedMinimumOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsMin;
    }

    // Get range of the standardized data item standard deviations
    public double standardizedRangeOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsRange;
    }

    public double standardisedRangeOfItemStandardDeviations(){
        return standardizedItemStandardDeviationsRange;
    }

    // Get raw data item variances
    public double[] rawItemVariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemVariances;
    }

    // Get standardized data item variances
    public double[] standardizedItemVariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemVariances;
    }

    // Get standardized data item variances
    public double[] standardisedItemVariances(){
        return this.standardizedItemVariances();
    }

    // Get a raw data item variance
    public double rawItemVariance(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemVariances[index-1];
    }

    // Get a raw data item variance
    public double rawItemVariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemVariances[index-1];
    }

    // Get a standardized data item variance
    public double standardizedItemVariance(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemVariances[index-1];
    }

    public double standardisedItemVariance(String itemName){
        return this.standardizedItemVariance(itemName);
    }

    // Get a standardized data item variance
    public double standardizedItemVariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemVariances[index-1];
    }

    public double standardisedItemVariance(int index){
        return this.standardizedItemVariance(index);
    }

    // Get mean of the raw data item variances
    public double rawMeanOfItemVariances(){
        return rawItemVariancesMean;
    }

    // Get standard deviation of the raw data item variances
    public double rawStanadarDeviationOfItemVariances(){
        return rawItemVariancesSd;
    }

    // Get variance of the raw data item variances
    public double rawVarianceOfItemVariances(){
        return rawItemVariancesVar;
    }

    // Get maximum of the raw data item variances
    public double rawMaximumOfItemVariances(){
        return rawItemVariancesMax;
    }

    // Get minimum of the raw data item variances
    public double rawMinimumOfItemVariances(){
        return rawItemVariancesMin;
    }

    // Get range of the raw data item variances
    public double rawRangeOfItemVariances(){
        return rawItemVariancesRange;
    }

    // Get mean of the standardized data item variances
    public double standardizedMeanOfItemVariances(){
        return standardizedItemVariancesMean;
    }

    public double standardisedMeanOfItemVariances(){
        return standardizedItemVariancesMean;
    }

    // Get standard deviation of the standardized data item variances
    public double standardizedStanadarDeviationOfItemVariances(){
        return standardizedItemVariancesSd;
    }

    public double standardisedStanadarDeviationOfItemVariances(){
        return standardizedItemVariancesSd;
    }

    // Get variance of the standardized data item variances
    public double standardizedVarianceOfItemVariances(){
        return standardizedItemVariancesVar;
    }

    public double standardisedVarianceOfItemVariances(){
        return standardizedItemVariancesVar;
    }

    // Get maximum of the standardized data item variances
    public double standardizedMaximumOfItemVariances(){
        return standardizedItemVariancesMax;
    }

    public double standardisedMaximumOfItemVariances(){
        return standardizedItemVariancesMax;
    }

    // Get minimum of the standardized data item variances
    public double standardizedMinimumOfItemVariances(){
        return standardizedItemVariancesMin;
    }

    public double standardisedMinimumOfItemVariances(){
        return standardizedItemVariancesMin;
    }

    // Get range of the standardized data item variances
    public double standardizedRangeOfItemVariances(){
        return standardizedItemVariancesRange;
    }

    public double standardisedRangeOfItemVariances(){
        return standardizedItemVariancesRange;
    }

    // Get raw data item minima
    public double[] rawItemMinima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMinima;
    }

    // Get standardized data item minima
    public double[] standardizedItemMinima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMinima;
    }

    public double[] standardisedItemMinima(){
        return this.standardizedItemMinima();
    }

    // Get a raw data item minimum
    public double rawItemMinimum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMinima[index-1];
    }

    // Get a standardized data item minimum
    public double standardizedItemMinimum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMinima[index-1];
    }

    public double standardisedItemMinimum(int index){
        return this.standardizedItemMinimum(index);
    }

    // Get mean of the raw data item minima
    public double rawMeanOfItemMinima(){
        return rawItemMinimaMean;
    }

    // Get standard deviation of the raw data item minima
    public double rawStanadarDeviationOfItemMinima(){
        return rawItemMinimaSd;
    }

    // Get variance of the raw data item minima
    public double rawVarianceOfItemMinima(){
        return rawItemMinimaVar;
    }

    // Get maximum of the raw data item minima
    public double rawMaximumOfItemMinima(){
        return rawItemMinimaMax;
    }

    // Get minimum of the raw data item minima
    public double rawMinimumOfItemMinima(){
        return rawItemMinimaMin;
    }

    // Get range of the raw data item minima
    public double rawRangeOfItemMinima(){
        return rawItemMinimaRange;
    }

   // Get mean of the standardized data item minima
    public double standardizedMeanOfItemMinima(){
        return standardizedItemMinimaMean;
    }

    public double standardisedMeanOfItemMinima(){
        return standardizedItemMinimaMean;
    }

    // Get standard deviation of the standardized data item minima
    public double standardizedStanadarDeviationOfItemMinima(){
        return standardizedItemMinimaSd;
    }

    public double standardisedStanadarDeviationOfItemMinima(){
        return standardizedItemMinimaSd;
    }

    // Get variance of the standardized data item minima
    public double standardizedVarianceOfItemMinima(){
        return standardizedItemMinimaVar;
    }

    public double standardisedVarianceOfItemMinima(){
        return standardizedItemMinimaVar;
    }

    // Get maximum of the standardized data item minima
    public double standardizedMaximumOfItemMinima(){
        return standardizedItemMinimaMax;
    }

    public double standardisedMaximumOfItemMinima(){
        return standardizedItemMinimaMax;
    }

    // Get minimum of the standardized data item minima
    public double standardizedMinimumOfItemMinima(){
        return standardizedItemMinimaMin;
    }

    public double standardisedMinimumOfItemMinima(){
        return standardizedItemMinimaMin;
    }

    // Get range of the standardized data item minima
    public double standardizedRangeOfItemMinima(){
        return standardizedItemMinimaRange;
    }

    public double standardisedRangeOfItemMinima(){
        return standardizedItemMinimaRange;
    }

    // Get raw data item maxima
    public double[] rawItemMaxima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMaxima;
    }

    // Get standardized data item maxima
    public double[] standardizedItemMaxima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMaxima;
    }

    public double[] standardisedItemMaxima(){
        return this.standardizedItemMaxima();
    }

    // Get a raw data item maximum
    public double rawItemMaximum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMaxima[index-1];
    }

    // Get a standardized data item maximum
    public double standardizedItemMaximum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMaxima[index-1];
    }

    public double standardisedItemMaximum(int index){
         return this.standardizedItemMaximum(index);
    }

    // Get mean of the raw data item maxima
    public double rawMeanOfItemMaxima(){
        return rawItemMaximaMean;
    }

    // Get standard deviation of the raw data item maxima
    public double rawStanadarDeviationOfItemMaxima(){
        return rawItemMaximaSd;
    }

    // Get variance of the raw data item maxima
    public double rawVarianceOfItemMaxima(){
        return rawItemMaximaVar;
    }

    // Get maximum of the raw data item maxima
    public double rawMaximumOfItemMaxima(){
        return rawItemMaximaMax;
    }

    // Get minimum of the raw data item maxima
    public double rawMinimumOfItemMaxima(){
        return rawItemMaximaMin;
    }

    // Get range of the raw data item maxima
    public double rawRangeOfItemMaxima(){
        return rawItemMaximaRange;
    }

    // Get mean of the standardized data item maxima
    public double standardizedMeanOfItemMaxima(){
        return standardizedItemMaximaMean;
    }

    public double standardisedMeanOfItemMaxima(){
        return standardizedItemMaximaMean;
    }

    // Get standard deviation of the standardized data item maxima
    public double standardizedStanadarDeviationOfItemMaxima(){
        return standardizedItemMaximaSd;
    }

    public double standardisedStanadarDeviationOfItemMaxima(){
        return standardizedItemMaximaSd;
    }

    // Get variance of the standardized data item maxima
    public double standardizedVarianceOfItemMaxima(){
        return standardizedItemMaximaVar;
    }

    public double standardisedVarianceOfItemMaxima(){
        return standardizedItemMaximaVar;
    }

    // Get maximum of the standardized data item maxima
    public double standardizedMaximumOfItemMaxima(){
        return standardizedItemMaximaMax;
    }

    public double standardisedMaximumOfItemMaxima(){
        return standardizedItemMaximaMax;
    }

    // Get minimum of the standardized data item maxima
    public double standardizedMinimumOfItemMaxima(){
        return standardizedItemMaximaMin;
    }

    public double standardisedMinimumOfItemMaxima(){
        return standardizedItemMaximaMin;
    }

    // Get range of the standardized data item maxima
    public double standardizedRangeOfItemMaxima(){
        return standardizedItemMaximaRange;
    }

    public double standardisedRangeOfItemMaxima(){
        return standardizedItemMaximaRange;
    }

    // Get raw data item ranges
    public double[] rawItemRanges(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemRanges;
    }

    // Get standardized data item ranges
    public double[] standardizedItemRanges(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemRanges;
    }

    public double[] standardisedItemRanges(){
        return standardizedItemRanges();
    }

    // Get a raw data item range
    public double rawItemRange(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemRanges[index-1];
    }

    // Get a standardized data item range
    public double standardizedItemRange(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemRanges[index-1];
    }

    public double standardisedItemRange(int index){
        return this.standardizedItemRange(index);
    }

    // Get mean of the raw data item ranges
    public double rawMeanOfItemRanges(){
        return rawItemRangesMean;
    }

    // Get standard deviation of the raw data item ranges
    public double rawStanadarDeviationOfItemRanges(){
        return rawItemRangesSd;
    }

    // Get variance of the raw data item ranges
    public double rawVarianceOfItemRanges(){
        return rawItemRangesVar;
    }

    // Get maximum of the raw data item ranges
    public double rawMaximumOfItemRanges(){
        return rawItemRangesMax;
    }

    // Get minimum of the raw data item ranges
    public double rawMinimumOfItemRanges(){
        return rawItemRangesMin;
    }

    // Get range of the raw data item ranges
    public double rawRangeOfItemRanges(){
        return rawItemRangesRange;
    }

    // Get mean of the standardized data item ranges
    public double standardizedMeanOfItemRanges(){
        return standardizedItemRangesMean;
    }

    public double standardisedMeanOfItemRanges(){
        return standardizedItemRangesMean;
    }

    // Get standard deviation of the standardized data item ranges
    public double standardizedStanadarDeviationOfItemRanges(){
        return standardizedItemRangesSd;
    }

    public double standardisedStanadarDeviationOfItemRanges(){
        return standardizedItemRangesSd;
    }

    // Get variance of the standardized data item ranges
    public double standardizedVarianceOfItemRanges(){
        return standardizedItemRangesVar;
    }

    public double standardisedVarianceOfItemRanges(){
        return standardizedItemRangesVar;
    }

    // Get maximum of the standardized data item ranges
    public double standardizedMaximumOfItemRanges(){
        return standardizedItemRangesMax;
    }

    public double standardisedMaximumOfItemRanges(){
        return standardizedItemRangesMax;
    }

    // Get minimum of the standardized data item ranges
    public double standardizedMinimumOfItemRanges(){
        return standardizedItemRangesMin;
    }

    public double standardisedMinimumOfItemRanges(){
        return standardizedItemRangesMin;
    }

    // Get range of the standardized data item ranges
    public double standardizedRangeOfItemRanges(){
        return standardizedItemRangesRange;
    }

    public double standardisedRangeOfItemRanges(){
        return standardizedItemRangesRange;
    }

    // Get raw data item totals
    public double[] rawItemTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemTotals;
    }

    // Get standardized data item totals
    public double[] standardizedItemTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemTotals;
    }

    public double[] standardisedItemTotals(){
        return this.standardizedItemTotals();
    }

    // Get a raw data item total
    public double rawItemTotal(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemTotals[index-1];
    }

    // Get a raw data item total
    public double rawItemTotal(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemTotals[index-1];
    }

    // Get a standardized data item total
    public double standardizedItemTotal(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemTotals[index-1];
    }

    public double standardisedItemTotal(String itemName){
       return standardizedItemTotal(itemName);
    }

    // Get a standardized data item total
    public double standardizedItemTotal(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemTotals[index-1];
    }

    public double standardisedItemTotal(int index){
        return this.standardizedItemTotal(index);
    }

    // Get mean of the raw data item totals
    public double rawMeanOfItemTotals(){
        return rawItemTotalsMean;
    }

    // Get standard deviation of the raw data item totals
    public double rawStanadarDeviationOfItemTotals(){
        return rawItemTotalsSd;
    }

    // Get variance of the raw data item totals
    public double rawVarianceOfItemTotals(){
        return rawItemTotalsVar;
    }

    // Get maximum of the raw data item totals
    public double rawMaximumOfItemTotals(){
        return rawItemTotalsMax;
    }

    // Get minimum of the raw data item totals
    public double rawMinimumOfItemTotals(){
        return rawItemTotalsMin;
    }

    // Get range of the raw data item totals
    public double rawRangeOfItemTotals(){
        return rawItemTotalsRange;
    }

    // Get mean of the standardized data item totals
    public double standardizedMeanOfItemTotals(){
        return standardizedItemTotalsMean;
    }

    public double standardisedMeanOfItemTotals(){
        return standardizedItemTotalsMean;
    }

    // Get standard deviation of the standardized data item totals
    public double standardizedStanadarDeviationOfItemTotals(){
        return standardizedItemTotalsSd;
    }

    public double standardisedStanadarDeviationOfItemTotals(){
        return standardizedItemTotalsSd;
    }

    // Get variance of the standardized data item totals
    public double standardizedVarianceOfItemTotals(){
        return standardizedItemTotalsVar;
    }

    public double standardisedVarianceOfItemTotals(){
        return standardizedItemTotalsVar;
    }

    // Get maximum of the standardized data item totals
    public double standardizedMaximumOfItemTotals(){
        return standardizedItemTotalsMax;
    }

    public double standardisedMaximumOfItemTotals(){
        return standardizedItemTotalsMax;
    }

    // Get minimum of the standardized data item totals
    public double standardizedMinimumOfItemTotals(){
        return standardizedItemTotalsMin;
    }

    public double standardisedMinimumOfItemTotals(){
        return standardizedItemTotalsMin;
    }

    // Get range of the standardized data item totals
    public double standardizedRangeOfItemTotals(){
        return standardizedItemTotalsRange;
    }

    public double standardisedRangeOfItemTotals(){
        return standardizedItemTotalsRange;
    }

    // Get raw data person means
    public double[] rawPersonMeans(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMeans;
    }

    // Get standardized data person means
    public double[] standardizedPersonMeans(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMeans;
    }

    public double[] standardisedPersonMeans(){
        return this.standardizedPersonMeans();
    }

    // Get a raw data person mean
    public double rawPersonMean(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMeans[index-1];
    }

    // Get a standardized data person mean
    public double standardizedPersonMean(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMeans[index-1];
    }

    public double standardisedPersonMean(int index){
        return this.standardizedPersonMean(index);
    }

    // Get raw data person standard deviations
    public double[] rawPersonStandardDeviations(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonStandardDeviations;
    }

    // Get standardized data person standard deviations
    public double[] standardizedPersonStandardDeviations(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonStandardDeviations;
    }

    public double[] standardisedPersonStandardDeviations(){
        return this.standardizedPersonStandardDeviations();
    }

    // Get a raw data person standard deviation
    public double rawPersonStandardDeviation(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonStandardDeviations[index-1];
    }

    // Get a standardized data person standard deviation
    public double standardizedPersonStandardDeviation(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonStandardDeviations[index-1];
    }

     public double standardisedPersonStandardDeviation(int index){
        return this.standardizedPersonStandardDeviation(index);
    }

    // Get raw data person variances
    public double[] rawPersonVariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonVariances;
    }

    // Get standardized data person variances
    public double[] standardizedPersonVariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonVariances;
    }

    public double[] standardisedPersonVariances(){
        return this.standardizedPersonVariances();
    }

    // Get a raw data person variance
    public double rawPersonVariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonVariances[index-1];
    }

    // Get a standardized data person variance
    public double standardizedPersonVariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonVariances[index-1];
    }

    public double standardisedPersonVariance(int index){
        return this.standardizedPersonVariance(index);
    }

    // Get raw data person minima
    public double[] rawPersonMinima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMinima;
    }

    // Get standardized data person minima
    public double[] standardizedPersonMinima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMinima;
    }

    public double[] standardisedPersonMinima(){
       return this.standardisedPersonMinima();
    }

    // Get a raw data person minimum
    public double rawPersonMinimum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMinima[index-1];
    }

    // Get a standardized data person minimum
    public double standardizedPersonMinimum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMinima[index-1];
    }

    public double standardisedPersonMinimum(int index){
        return this.standardizedPersonMinimum(index);
    }

    // Get raw data person maxima
    public double[] rawPersonMaxima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMaxima;
    }

    // Get standardized data person maxima
    public double[] standardizedPersonMaxima(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMaxima;
    }

    public double[] standardisedPersonMaxima(){
        return this.standardizedPersonMaxima();
    }

    // Get a raw data person maximum
    public double rawPersonMaximum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonMaxima[index-1];
    }

    // Get a standardized data person maximum
    public double standardizedPersonMaximum(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonMaxima[index-1];
    }

    public double standardisedPersonMaximum(int index){
        return this.standardizedPersonMaximum(index);
    }

    // Get raw data person ranges
    public double[] rawPersonRanges(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonRanges;
    }

    // Get standardized data person ranges
    public double[] standardizedPersonRanges(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonRanges;
    }

    public double[] standardisedPersonRanges(){
        return this.standardizedPersonRanges();
    }

    // Get a raw data person range
    public double rawPersonRange(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonRanges[index-1];
    }

    // Get a standardized data person range
    public double standardizedPersonRange(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonRanges[index-1];
    }

    public double standardisedPersonRange(int index){
        return this.standardizedPersonRange(index);
    }

    // Get raw data item medians
    public double[] rawItemMedians(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMedians;
    }

    // Get standardized data item medians
    public double[] standardizedItemMedians(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMedians;
    }

    public double[] standardisedItemMedians(){
        return this.standardizedItemMedians();
    }

    // Get a raw data item median
    public double rawItemMedian(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMedians[index-1];
    }

    // Get a raw data item median
    public double rawItemMedian(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawItemMedians[index-1];
    }

    // Get a standardized data item median
    public double standardizedItemMedian(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        int index = this.itemIndex(itemName);
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMedians[index-1];
    }

    public double standardisedItemMedian(String itemName){
       return standardizedItemMedian(itemName);
    }

    // Get a standardized data item median
    public double standardizedItemMedian(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The item index, " + index + ", must lie between 1 and the number of items," + this.nItems + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedItemMedians[index-1];
    }

    public double standardisedItemMedian(int index){
        return this.standardizedItemMedian(index);
    }

    // Get mean of the raw data item medians
    public double rawMeanOfItemMedians(){
        return rawItemMediansMean;
    }

    // Get standard deviation of the raw data item medians
    public double rawStanadarDeviationOfItemMedians(){
        return rawItemMediansSd;
    }

    // Get variance of the raw data item medians
    public double rawVarianceOfItemMedians(){
        return rawItemMediansVar;
    }

    // Get maximum of the raw data item medians
    public double rawMaximumOfItemMedians(){
        return rawItemMediansMax;
    }

    // Get minimum of the raw data item medians
    public double rawMinimumOfItemMedians(){
        return rawItemMediansMin;
    }

    // Get range of the raw data item medians
    public double rawRangeOfItemMedians(){
        return rawItemMediansRange;
    }

    // Get mean of the standardized data item medians
    public double standardizedMeanOfItemMedians(){
        return standardizedItemMediansMean;
    }

    public double standardisedMeanOfItemMedians(){
        return standardizedItemMediansMean;
    }

    // Get standard deviation of the standardized data item medians
    public double standardizedStanadarDeviationOfItemMedians(){
        return standardizedItemMediansSd;
    }

    public double standardisedStanadarDeviationOfItemMedians(){
        return standardizedItemMediansSd;
    }

    // Get variance of the standardized data item medians
    public double standardizedVarianceOfItemMedians(){
        return standardizedItemMediansVar;
    }

    public double standardisedVarianceOfItemMedians(){
        return standardizedItemMediansVar;
    }

    // Get maximum of the standardized data item medians
    public double standardizedMaximumOfItemMedians(){
        return standardizedItemMediansMax;
    }

    public double standardisedMaximumOfItemMedians(){
        return standardizedItemMediansMax;
    }

    // Get minimum of the standardized data item medians
    public double standardizedMinimumOfItemMedians(){
        return standardizedItemMediansMin;
    }

    public double standardisedMinimumOfItemMedians(){
        return standardizedItemMediansMin;
    }

    // Get range of the standardized data item medians
    public double standardizedRangeOfItemMedians(){
        return standardizedItemMediansRange;
    }

    public double standardisedRangeOfItemMedians(){
        return standardizedItemMediansRange;
    }

    // Get raw data person totals
    public double[] rawPersonTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonTotals;
    }

    // Get standardized data person totals
    public double[] standardizedPersonTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonTotals;
    }

    public double[] standardisedPersonTotals(){
        return this.standardizedPersonTotals();
    }

    // Get a raw data person total
    public double rawPersonTotal(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawPersonTotals[index-1];
    }

    // Get a standardized data person total
    public double standardizedPersonTotal(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nPersons)throw new IllegalArgumentException("The person index, " + index + ", must lie between 1 and the number of persons," + this.nPersons + ", inclusive");
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedPersonTotals[index-1];
    }

    public double standardisedPersonTotal(int index){
        return this.standardizedPersonTotal(index);
    }

    // Get raw data total mean
    public double rawAllResponsesMean(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesMean;
    }

    // Get standardized data total mean
    public double standardizedAllResponsesMean(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesMean;
    }

    public double standardisedTotalMean(){
        return this.standardizedAllResponsesMean();
    }

    // Get raw data total standard deviation
    public double rawAllResponsesStandardDeviation(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesStandardDeviation;
    }

    // Get standardized data total standard deviation
    public double standardizedAllResponsesStandardDeviation(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesStandardDeviation;
    }

    public double standardisedTotalStandardDeviation(){
        return this.standardizedAllResponsesStandardDeviation();
    }

    // Get raw data total variance
    public double rawAllResponsesVariance(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesVariance;
    }

    // Get standardized data total variance
    public double standardizedAllResponsesVariance(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesVariance;
    }

    public double standardisedTotalVariance(){
        return this.standardizedAllResponsesVariance();
    }

    // Get raw data total minimum
    public double rawAllResponsesMinimum(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesMinimum;
    }

    // Get standardized data total minimum
    public double standardizedAllResponsesMinimum(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesMinimum;
    }

    public double standardisedTotalMinimum(){
        return this.standardizedAllResponsesMinimum();
    }

    // Get raw data total maximum
    public double rawAllResponsesMaximum(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesMaximum;
    }

    // Get standardized data total maximum
    public double standardizedAllResponsesMaximum(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesMaximum;
    }

    public double standardisedTotalMaximum(){
        return this.standardizedAllResponsesMaximum();
    }

    // Get raw data total range
    public double rawAllResponsesRange(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesRange;
    }

    // Get standardized data total range
    public double standardizedAllResponsesRange(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesRange;
    }

    public double standardisedTotalRange(){
        return this.standardizedAllResponsesRange();
    }

    // Get raw data total total
    public double rawAllResponsesTotal(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.rawAllResponsesTotal;
    }

    // Get standardized data total total
    public double standardizedAllResponsesTotal(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.variancesCalculated)this.meansAndVariances();
        return this.standardizedAllResponsesTotal;
    }

    public double standardisedTotalTotal(){
        return this.standardizedAllResponsesTotal();
    }

    // COVARIANCES AND CORRELATION COEFFICIENTS
    // Calculate covariances and correlation coefficints between items
    protected void covariancesAndCorrelationCoefficients(){
        // Covariances
        this.rawCovariances = new double[this.nItems+1][this.nItems+1];
        // raw data item-item covariance
        for(int i=0; i<this.nItems; i++){
            for(int j=i; j<this.nItems; j++){
                this.rawCovariances[i][j] = Stat.covariance(this.scores0[i], this.scores0[j]);
                if(i!=j)this.rawCovariances[j][i] = this.rawCovariances[i][j];
            }
        }

        // raw data item-(item total) covariances
        for(int i=0; i<this.nItems; i++){
            this.rawCovariances[i][this.nItems] = Stat.covariance(this.scores0[i], this.rawPersonTotals);
            this.rawCovariances[this.nItems][i] = this.rawCovariances[i][this.nItems];
        }
        this.rawCovariances[this.nItems][this.nItems] = Stat.covariance(this.rawPersonTotals, this.rawPersonTotals);

        // standardized data item-item covariances
        this.standardizedCovariances = new double[this.nItems+1][this.nItems+1];
        for(int i=0; i<this.nItems; i++){
            for(int j=i; j<this.nItems; j++){
                this.standardizedCovariances[i][j] = Stat.covariance(this.scores0[i], this.scores0[j]);
                if(i!=j)this.standardizedCovariances[j][i] = this.standardizedCovariances[i][j];
            }
        }

        // standardized data item-(item totals) covariances
        for(int i=0; i<this.nItems; i++){
            this.standardizedCovariances[i][this.nItems] = Stat.covariance(this.scores0[i], this.standardizedPersonTotals);
            this.standardizedCovariances[this.nItems][i] = this.standardizedCovariances[i][this.nItems];
        }
        this.standardizedCovariances[this.nItems][this.nItems] = Stat.covariance(this.standardizedPersonTotals, this.standardizedPersonTotals);


        // Correlation coefficients
        this.rawCorrelationCoefficients = new double[this.nItems+1][this.nItems+1];

        // Raw data inter-item correlation coefficients
        for(int i=0; i<this.nItems; i++){
            this.rawCorrelationCoefficients[i][i] = 1.0;
            for(int j=i+1; j<this.nItems; j++){
                this.rawCorrelationCoefficients[i][j] = this.rawCovariances[i][j]/Math.sqrt(this.rawCovariances[i][i]*this.rawCovariances[j][j]);
                if(Fmath.isNaN(this.rawCorrelationCoefficients[i][j]))this.rawCorrelationCoefficients[i][j] = 0.0;
                this.rawCorrelationCoefficients[j][i] = this.rawCorrelationCoefficients[i][j];
            }
        }

        // Raw data item-(item totals) correlation coefficients
        for(int i=0; i<this.nItems; i++){
            this.rawCorrelationCoefficients[i][this.nItems] = this.rawCovariances[i][this.nItems]/Math.sqrt(this.rawCovariances[i][i]*this.rawCovariances[this.nItems][this.nItems]);
            if(Fmath.isNaN(this.rawCorrelationCoefficients[i][this.nItems]))this.rawCorrelationCoefficients[i][this.nItems] = 0.0;
            this.rawCorrelationCoefficients[this.nItems][i] = this.rawCorrelationCoefficients[i][this.nItems];
        }
        this.rawCorrelationCoefficients[this.nItems][this.nItems] = 1.0;

        // Average of the raw data inter-item correlation coefficients
        double[] rhoArray = new double[this.nItems*(this.nItems-1)/2];
        int kk=0;
        for(int i=0; i<this.nItems; i++){
            for(int j=i+1; j<this.nItems; j++){
                rhoArray[kk] = this.rawCorrelationCoefficients[i][j];
                kk++;
            }
        }
        Stat st = new Stat(rhoArray);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawMeanRhoWithoutTotals = st.mean_as_double();
        this.rawStandardDeviationRhoWithoutTotals = st.standardDeviation_as_double();

        rhoArray = new double[this.nItems*(this.nItems+1)/2];
        kk=0;
        for(int i=0; i<=this.nItems; i++){
            for(int j=i+1; j<=this.nItems; j++){
                rhoArray[kk] = this.rawCorrelationCoefficients[i][j];
                kk++;
            }
        }
        st = new Stat(rhoArray);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.rawMeanRhoWithTotals = st.mean_as_double();
        this.rawStandardDeviationRhoWithTotals = st.standardDeviation_as_double();

        // Standardized data inter-item correlation coefficients
        this.standardizedCorrelationCoefficients = new double[this.nItems+1][this.nItems+1];

        for(int i=0; i<this.nItems; i++){
            this.standardizedCorrelationCoefficients[i][i] = 1.0;
            for(int j=i+1; j<this.nItems; j++){
                this.standardizedCorrelationCoefficients[i][j] = this.standardizedCovariances[i][j]/Math.sqrt(this.standardizedCovariances[i][i]*this.standardizedCovariances[j][j]);
                if(Fmath.isNaN(this.standardizedCorrelationCoefficients[i][j]))this.standardizedCorrelationCoefficients[i][j] = 0.0;
                this.standardizedCorrelationCoefficients[j][i] = this.standardizedCorrelationCoefficients[i][j];
            }
        }

        // Standardized data item-(item totals) correlation coefficients
        for(int i=0; i<this.nItems; i++){
            this.standardizedCorrelationCoefficients[i][this.nItems] = this.standardizedCovariances[i][this.nItems]/Math.sqrt(this.standardizedCovariances[i][i]*this.standardizedCovariances[this.nItems][this.nItems]);
            if(Fmath.isNaN(this.standardizedCorrelationCoefficients[i][this.nItems]))this.standardizedCorrelationCoefficients[i][this.nItems] = 0.0;
            this.standardizedCorrelationCoefficients[this.nItems][i] = this.standardizedCorrelationCoefficients[i][this.nItems];
        }
        this.standardizedCorrelationCoefficients[this.nItems][this.nItems] = 1.0;

        // Average of the standardized data inter-item correlation coefficients
        rhoArray = new double[this.nItems*(this.nItems-1)/2];
        kk=0;
        for(int i=0; i<this.nItems; i++){
            for(int j=i+1; j<this.nItems; j++){
                rhoArray[kk] = this.standardizedCorrelationCoefficients[i][j];
                kk++;
            }
        }
        st = new Stat(rhoArray);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedMeanRhoWithoutTotals = st.mean_as_double();
        this.standardizedStandardDeviationRhoWithoutTotals = st.standardDeviation_as_double();

        rhoArray = new double[this.nItems*(this.nItems+1)/2];
        kk=0;
        for(int i=0; i<=this.nItems; i++){
            for(int j=i+1; j<=this.nItems; j++){
                rhoArray[kk] = this.standardizedCorrelationCoefficients[i][j];
                kk++;
            }
        }
        st = new Stat(rhoArray);
        if(this.nFactorOption){
            st.setDenominatorToN();
        }
        else{
            st.setDenominatorToNminusOne();
        }
        this.standardizedMeanRhoWithTotals = st.mean_as_double();
        this.standardizedStandardDeviationRhoWithTotals = st.standardDeviation_as_double();

        this.covariancesCalculated = true;
    }

    // Get the raw data covariances
    public double[][] rawCovariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCovariances;
    }

    // Get the standardized data covariances
    public double[][] standardizedCovariances(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCovariances;
    }

    public double[][] standardisedCovariances(){
        return this.standardizedCovariances();
    }

    // Get the raw data covariance of two items
    public double rawCovariance(String itemName1, String itemName2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        return this.rawCovariances[index1-1][index2-1];
    }

    // Get the raw data covariance of two items
    public double rawCovariance(int index1, int index2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index1<1 || index1>this.nItems)throw new IllegalArgumentException("The first item index, " + index1 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(index2<1 || index2>this.nItems)throw new IllegalArgumentException("The second item index, " + index2 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCovariances[index1-1][index2-1];
    }

    // Get the raw data covariance of an item and the toals of the items
    public double rawCovariance(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index = this.itemIndex(itemName);
        return this.rawCovariances[index-1][this.nItems];
    }

    // Get the raw data covariance of an item and the toals of the items
    public double rawCovariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index< 1 || index>this.nItems)throw new IllegalArgumentException("The first item index, " + index + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCovariances[index-1][this.nItems];
    }

    // Get the standardized data covariance of two items
    public double standardizedCovariance(String itemName1, String itemName2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        return this.standardizedCovariances[index1+1][index2+1];
    }

    public double standardisedCovariance(String itemName1, String itemName2){
        return this.standardizedCovariance(itemName1, itemName2);
    }

    // Get the standardized data covariance of two items
    public double standardizedCovariance(int index1, int index2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index1<1 || index1>this.nItems)throw new IllegalArgumentException("The first item index, " + index1 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(index2<1 || index2>this.nItems)throw new IllegalArgumentException("The second item index, " + index2 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCovariances[index1+1][index2+1];
    }

    public double standardisedCovariance(int index1, int index2){
        return this.standardizedCovariance(index1, index2);
    }

    // Get the standardized data covariance of an item and the toals of the items
    public double standardizedCovariance(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index = this.itemIndex(itemName);
        return this.standardizedCovariances[index+1][this.nItems];
    }

    public double standardisedCovariance(String itemName){
        return this.standardizedCovariance(itemName);
    }

    // Get the standardized data covariance of an item and the totals of the items
    public double standardizedCovariance(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The first item index, " + index + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCovariances[index+1][this.nItems];
    }

    public double standardisedCovariance(int index){
        return this.standardizedCovariance(index);
    }

    // Get the raw data average correlation coefficient excluding totals
    public double rawAverageCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawMeanRhoWithoutTotals;
    }

    // Get the raw data standardard deviation of the correlation coefficient excluding totals
    public double rawStandardDeviationCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawStandardDeviationRhoWithoutTotals;
    }

    // Get the standardized data average correlation coefficient excluding totals
    public double standardizedAverageCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedMeanRhoWithoutTotals;
    }

    public double standardisedAverageCorrelationCoefficients(){
        return this.standardizedAverageCorrelationCoefficients();
    }

    // Get the standardized data standardard deviation of the correlation coefficient excluding totals
    public double standardizedStandardDeviationCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedStandardDeviationRhoWithoutTotals;
    }

    public double standardisedStandardDeviationCorrelationCoefficients(){
        return this.standardizedStandardDeviationCorrelationCoefficients();
    }

    // Get the raw data average correlation coefficient including totals
    public double rawAverageCorrelationCoefficientsWithTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawMeanRhoWithTotals;
    }

    // Get the raw data standardard deviation of the correlation coefficient including totals
    public double rawStandardDeviationCorrelationCoefficientsWithTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawStandardDeviationRhoWithTotals;
    }

    // Get the standardized data average correlation coefficient including totals
    public double standardizedAverageCorrelationCoefficientsWithTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedMeanRhoWithTotals;
    }

    public double standardisedAverageCorrelationCoefficientsWithTotals(){
        return this.standardizedAverageCorrelationCoefficientsWithTotals();
    }

    // Get the standardized data standardard deviation of the correlation coefficient including totals
    public double standardizedStandardDeviationCorrelationCoefficientsWithTotals(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedStandardDeviationRhoWithTotals;
    }

    public double standardisedStandardDeviationCorrelationCoefficientsWithTotals(){
        return this.standardizedStandardDeviationCorrelationCoefficientsWithTotals();
    }

    // Get the raw data correlation coefficients
    public double[][] rawCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCorrelationCoefficients;
    }


    // Get the standardized data correlation coefficients
    public double[][] standardizedCorrelationCoefficients(){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCorrelationCoefficients;
    }

    public double[][] standardisedCorrelationCoefficients(){
        return this.standardizedCorrelationCoefficients();
    }

    // Get the raw data correlation coefficient of two items
    public double rawCorrelationCoefficient(String itemName1, String itemName2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        return this.rawCorrelationCoefficients[index1-1][index2-1];
    }

    // Get the raw data correlation coefficient of two items
    public double rawCorrelationCoefficient(int index1, int index2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index1<1 || index1>this.nItems)throw new IllegalArgumentException("The first item index, " + index1 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(index2<1 || index2>this.nItems)throw new IllegalArgumentException("The second item index, " + index2 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCorrelationCoefficients[index1-1][index2-1];
    }

        // Get the raw data correlation coefficient of an item and the totals of the items`
    public double rawCorrelationCoefficient(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index = this.itemIndex(itemName);
        return this.rawCorrelationCoefficients[index-1][this.nItems];
    }

    // Get the raw data correlation coefficient of an item and the totals of the items`
    public double rawCorrelationCoefficient(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The first item index, " + index + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.rawCorrelationCoefficients[index-1][this.nItems];
    }

    // Get the standardized data correlation coefficient of two items
    public double standardizedCorrelationCoefficient(String itemName1, String itemName2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        return this.standardizedCorrelationCoefficients[index1+1][index2+1];
    }

    public double standardisedCorrelationCoefficient(String itemName1, String itemName2){
        return this.standardizedCorrelationCoefficient(itemName1, itemName2);
    }

    // Get the standardized data correlation coefficient of two items
    public double standardizedCorrelationCoefficient(int index1, int index2){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index1<1 || index1>this.nItems)throw new IllegalArgumentException("The first item index, " + index1 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(index2<1 || index2>this.nItems)throw new IllegalArgumentException("The second item index, " + index2 + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCorrelationCoefficients[index1+1][index2+1];
    }

    public double standardisedCorrelationCoefficient(int index1, int index2){
        return this.standardizedCorrelationCoefficient(index1, index2);
    }

    // Get the standardized data correlation coefficient of an item and the totals of the items`
    public double standardizedCorrelationCoefficient(String itemName){
        if(!this.dataPreprocessed)this.preprocessData();
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        int index = this.itemIndex(itemName);
        return this.standardizedCorrelationCoefficients[index+1][this.nItems];
    }

    public double standardisedCorrelationCoefficient(String itemName){
        return this.standardizedCorrelationCoefficient(itemName);
    }

    // Get the standardized data correlation coefficient of an item and the totals of the items`
    public double standardizedCorrelationCoefficient(int index){
        if(!this.dataPreprocessed)this.preprocessData();
        if(index<1 || index>this.nItems)throw new IllegalArgumentException("The first item index, " + index + ", must lie between 1 and the number of items plus one (for totals)," + (this.nItems+1) + ", inclusive");
        if(!this.covariancesCalculated)this.covariancesAndCorrelationCoefficients();
        return this.standardizedCorrelationCoefficients[index+1][this.nItems];
    }

    public double standardisedCorrelationCoefficient(int index){
        return this.standardizedCorrelationCoefficient(index);
    }


    // Check which of the original items are dichotomous
    protected double[] checkWhetherRawItemsDichotomous(){

        if(!this.dichotomousCheckDone){
            this.dichotomous = new boolean[this.nItems];
            this.dichotomousPercentage = new double[this.nItems];
            int nDich = 0;
            for(int k=0; k<this.nItems; k++){
                this.dichotomousPercentage[k] = this.checkWhetherDichotomous(this.scores0[k]);
                if(this.dichotomousPercentage[k]==100.0){
                    this.dichotomous[k]=true;
                    nDich++;
                }
            }
            if(nDich==this.nItems)this.dichotomousOverall = true;

            this.dichotomousCheckDone = false;
        }
        return this.dichotomousPercentage;
    }

    // Check wheteher an array of data is dichotomous
    // returns percetage responses that are dichotomous
    protected double checkWhetherDichotomous(double[] array){

        int n = array.length;
        double[] responseMatching = new double[n];
        boolean[] matchingCheck = new boolean[n];

        for(int i=0; i<n; i++){
            responseMatching[i] = 0.0;
            matchingCheck[i] = false;
        }

        for(int i=0; i<n; i++){
            responseMatching[i] = 0;
            for(int j=0; j<n; j++){
                if(array[i]==array[j] && !matchingCheck[j]){
                    responseMatching[i] += 1.0;
                    matchingCheck[j] = true;
                }
            }
        }

        ArrayMaths am0 = new ArrayMaths(responseMatching);
        ArrayMaths am1 = am0.sort();
        double[] sorted = am1.array();
        double max = (sorted[n-1] + sorted[n-2])*100.0/((double)n);
        return max;
    }





    // DELETION OF AN ITEM
    // Delete an item - item name supplied
    public double[][] deleteItem(String name){
        int index = this.itemIndex(name);
        return this.deleteItem(index);
    }

    // Delete an item - index supplied (indices starting at 1 NOT 0)
    public double[][] deleteItem(int index){
        index--;
        int jj = 0;

        double[][] array1 = new double[this.nItems-1][this.nPersons];
        for(int i=0; i<this.nItems; i++){
            if(i!=index){
                array1[jj] = scores0[i];
                jj++;
            }
        }
        return this.transpose0to1(array1);
    }


    // SCATTER PLOTS
    // Plot of item - item responses  -  raw data
    public void rawItemItemPlot(String itemName1, String itemName2){
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        this.rawItemItemPlot(index1, index2);
    }


    // Plot of item - item responses  -  raw data
    public void rawItemItemPlot(int itemIndex1, int itemIndex2){
        itemIndex1--;
        itemIndex2--;

        PlotGraph pg = new PlotGraph(this.scores0[itemIndex1], this.scores0[itemIndex2]);
        String graphTitle = "Scores: plot of responses to the item, " + this.itemNames[itemIndex1] + ", against those to the item, " + this.itemNames[itemIndex2];
        pg.setGraphTitle(graphTitle);
        pg.setXaxisLegend("Responses to the item, " + this.itemNames[itemIndex1]);
        pg.setYaxisLegend("Responses to the item, " + this.itemNames[itemIndex2]);
        pg.setLine(0);
        pg.setPoint(4);
        pg.plot();
    }

    // Plot of item - means responses  -  raw data
    public void rawItemMeansPlot(String itemName){
        int index = this.itemIndex(itemName);
        this.rawItemMeansPlot(index);
    }

    // Plot of item - means responses  -  raw data
    public void rawItemMeansPlot(int itemIndex){
        itemIndex--;

        PlotGraph pg = new PlotGraph(this.rawPersonMeans, this.scores0[itemIndex]);
        String graphTitle = "Scores: plot of responses to the item, " + this.itemNames[itemIndex] + ", against the means of the responses to all items";
        pg.setGraphTitle(graphTitle);
        pg.setXaxisLegend("Mean of the responses to all the items, ");
        pg.setYaxisLegend("Responses to the item, " + this.itemNames[itemIndex]);
        pg.setLine(0);
        pg.setPoint(4);
        pg.plot();
    }



    // Plot of item - item responses  -  standardized data
    public void standardizedItemItemPlot(String itemName1, String itemName2){
        int index1 = this.itemIndex(itemName1);
        int index2 = this.itemIndex(itemName2);
        this.standardizedItemItemPlot(index1, index2);
    }

    public void standardisedItemItemPlot(String itemName1, String itemName2){
        this.standardizedItemItemPlot(itemName1, itemName2);
    }

    // Plot of item - item responses  -  standardized data
    public void standardizedItemItemPlot(int itemIndex1, int itemIndex2){
        itemIndex1--;
        itemIndex2--;

        PlotGraph pg = new PlotGraph(this.standardizedScores0[itemIndex1], this.standardizedScores0[itemIndex2]);
        String graphTitle = "Scores: plot of responses to the item, " + this.itemNames[itemIndex1] + ", against those to the item, " + this.itemNames[itemIndex2];
        pg.setGraphTitle(graphTitle);
        pg.setXaxisLegend("Responses to the item, " + this.itemNames[itemIndex1]);
        pg.setYaxisLegend("Responses to the item, " + this.itemNames[itemIndex2]);
        pg.setLine(0);
        pg.setPoint(4);
        pg.plot();
    }

    public void standardisedItemItemPlot(int itemIndex1, int itemIndex2){
        this.standardizedItemItemPlot(itemIndex1, itemIndex2);
    }


    // Plot of item - means responses  -  standardized data
    public void standardizedItemMeansPlot(String itemName){
        int index = this.itemIndex(itemName);
        this.standardizedItemMeansPlot(index);
    }

    public void standardisedItemMeansPlot(String itemName){
        this.standardizedItemMeansPlot(itemName);
    }


    // Plot of item - means responses  -  standardized data
    public void standardizedItemMeansPlot(int itemIndex){
        itemIndex--;

        PlotGraph pg = new PlotGraph(this.standardizedPersonMeans, this.standardizedScores0[itemIndex]);
        String graphTitle = "Scores: plot of responses to the item, " + this.itemNames[itemIndex] + ", against the means of the responses to all items";
        pg.setGraphTitle(graphTitle);
        pg.setXaxisLegend("Mean of the responses to all the items, ");
        pg.setYaxisLegend("Responses to the item, " + this.itemNames[itemIndex]);
        pg.setLine(0);
        pg.setPoint(4);
        pg.plot();
    }

    public void standardisedItemMeansPlot(int itemIndex){
        this.standardizedItemMeansPlot(itemIndex);
    }

    // OUTPUT METHODS

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

    // Type of output file
    // option = 1 - text file  (.txt)
    // option = 2 - MS Excel readable file (.xls) [default option]
    public void setOutputFileType(int option){
        this.fileOption = option;
        this.fileOptionSet = true;
    }

    // Set numbering of output filename to prevent overwriting
    // default value = false
    public void setFileNumbering(){
        this.fileNumberingSet = true;
    }

    // Remove numbering of output filename thus allowing overwriting
    // default value = false
    public void removeFileNumbering(){
        this.fileNumberingSet = false;
    }

    // Return data file title
    public String getTitle(){
        return this.title[0];
    }

    // Return input file name
    public String getInputFileName(){
        return this.inputFilename;
    }

    // OUTPUT THE PROCESSED DATA

    // Output the processed date in the same item/person/row/column format as entered data
    // No input file name entered via method argument list
    public void outputProcessedData(){
        if(!this.dataPreprocessed)this.preprocessData();
        this.outputFilename = "ScoresOutput";
        if(this.fileOption==1){
            this.outputFilename += ".txt";
        }
        else{
            this.outputFilename += ".xls";
        }
        String message1 = "Output file name for the processes scores:";
        String message2 = "\nEnter the required name (as a single word) and click OK ";
        String message3 = "\nor simply click OK for default value";
        String message = message1 + message2 + message3;
        String defaultName = this.outputFilename;
        this.outputFilename = Db.readLine(message, defaultName);
        this.outputProcessedData(this.outputFilename);
    }


    // Output the processed date in the same item/person/row/column format as entered data
    // input file name via method argument list
    public void outputProcessedData(String filename){
        this.outputProcessedDataCommon(filename, this.originalDataOrder);
    }


    // Output the processed date as the alternative item/person/row/column format to that of the entered data
    // No input file name entered via method argument list
    public void outputProcessedDataAlternate(){
        if(!this.dataPreprocessed)this.preprocessData();
        this.outputFilename = "ScoresOutput";
        if(this.fileOption==1){
            this.outputFilename += ".txt";
        }
        else{
            this.outputFilename += ".xls";
        }
        String message1 = "Output file name for the processes scores:";
        String message2 = "\nEnter the required name (as a single word) and click OK ";
        String message3 = "\nor simply click OK for default value";
        String message = message1 + message2 + message3;
        String defaultName = this.outputFilename;
        this.outputFilename = Db.readLine(message, defaultName);
        this.outputProcessedDataAlternate(this.outputFilename);
    }

        // Output the processed date in the same item/person/row/column format as entered data
    // input file name via method argument list
    public void outputProcessedDataAlternate(String filename){
        int orderChoice = 0;
        if(this.originalDataOrder==0)orderChoice = 1;
        this.outputProcessedDataCommon(filename, orderChoice);
    }


    // Output the processed date of either order format
    // input file name via method argument list
    private void outputProcessedDataCommon(String filename, int orderChoice){
        if(!this.dataPreprocessed)this.preprocessData();
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

        if(orderChoice==0){
            title[0] += "   (output: row per item)";
        }
        else{
            title[0] += "   (output: row per person)";
        }

        if(this.fileOption==1){
            this.outputText(orderChoice);
        }
        else{
            this.outputExcel(orderChoice);
        }
    }


    // Output processed data as a text file
    private void outputText(int orderChoice){


        FileOutput fout = new FileOutput(this.outputFilename);

        fout.println(title[0]);
        fout.println((this.nItems));
        fout.println(this.nPersons);
        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
        }
        fout.println();
        if(orderChoice==0){
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nPersons; j++){
                    fout.printtab(Fmath.truncate(this.scores0[i][j], this.trunc));
                }
                fout.println();
            }
        }
        else{
            for(int j=0; j<this.nPersons; j++){
                for(int i=0; i<this.nItems; i++){
                    fout.printtab(Fmath.truncate(this.scores1[j][i], trunc));
                }
                fout.println();
            }
        }
        fout.close();
    }


    // Output processed data as a text file
    private void outputExcel(int orderChoice){

        FileOutput fout = new FileOutput(this.outputFilename);
        fout.println(title[0]);
        fout.println((this.nItems));
        fout.println(this.nPersons);
        for(int i=0; i<this.nItems; i++){
            fout.printtab(this.itemNames[i]);
        }
        fout.println();
        if(orderChoice==0){
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nPersons; j++){
                    fout.printtab(Fmath.truncate(this.scores0[i][j], this.trunc));
                }
                fout.println();
            }
        }
        else{
            for(int j=0; j<this.nPersons; j++){
                for(int i=0; i<this.nItems; i++){
                    fout.printtab(Fmath.truncate(this.scores1[j][i], this.trunc));
                }
                fout.println();
            }
        }
        fout.close();
    }

    // CONVERSIONS
    // Convert to PCA
    public PCA toPCA(){
        PCA pca = new PCA();

        pca.title = this.title;
        pca.titleLines = this.titleLines;
        pca.inputFilename = this.inputFilename;
        pca.outputFilename = this.outputFilename;
        pca.fileOption = this.fileOption;
        pca.fileOptionSet = this.fileOptionSet;
        pca.fileExtensions = this.fileExtensions;
        pca.fileNumberingSet = this.fileNumberingSet;

        pca.originalDataType = this.originalDataType;                                               // - String[][]  (including read from file);
        pca.originalDataOrder = this.originalDataOrder;                                             // - matrix columns = responses of a person
        pca.originalData = this.originalData;                                                       // Original data as entered
        pca.scores0 = Conv.copy(this.scores0);                                                         // individual scores -  after any 'no response' deletions or replacements
        pca.originalScores0 = Conv.copy(this.originalScores0);                                         // scores0 before any 'no response' deletions or replacements
        pca.standardizedScores0 = Conv.copy(this.standardizedScores0);                                 // standardized scores0
        pca.scores1 = Conv.copy(this.scores1);                                                         // individual scores -  after any 'no response' deletions or replacements
        pca.originalScores1 = Conv.copy(this.originalScores1);                                         // scores1 before any 'no response' deletions or replacements
        pca.standardizedScores1 = Conv.copy(this.standardizedScores1);                                 // standardized scores1
        pca.dataEntered = this.dataEntered;                                                         // = true when scores entered
        pca.nItems = this.nItems;                                                                   // number of items, after any deletions
        pca.originalNitems = this.originalNitems;                                                   // original number of items
        pca.itemNames = Conv.copy(this.itemNames);                                                     // names of the items
        pca.originalItemNames = Conv.copy(this.originalItemNames);                                     // list of item names before any deletions
        pca.itemNamesSet = this.itemNamesSet;                                                       // = true when item names entered
        pca.nPersons = this.nPersons;                                                               // number of persons, after any deletions
        pca.originalNpersons = this.originalNpersons;                                               // original number of persons
        pca.nScores = this.nScores;
        pca.originalNscores = this.originalNscores;                                                 // original total number of scores
        pca.otherFalse = this.otherFalse;                                                           // false value for dichotomous data if one of the default values
        pca.otherTrue = this.otherTrue;                                                             // true value for dichotomous data if one of the default values
        pca.otherDichotomousDataSet = this.otherDichotomousDataSet;                                 // = true if user sets an alternative dichotomous pair
        pca.dichotomous = Conv.copy(this.dichotomous);                                                 // true if the data in an item is dichotomous
        pca.dichotomousPercentage = Conv.copy(this.dichotomousPercentage);                             // percentage of responses in an item that are dichotomous
        pca.dichotomousOverall = this.dichotomousOverall;                                           // true if all the data is dichotomous
        pca.dichotomousCheckDone = this.dichotomousCheckDone;                                       // true if check for dichotomous data performed
        pca.letterToNumeralSet = this.letterToNumeralSet;                                           // = true if user set the letter to numeral option allowing alphabetic response input
        pca.ignoreNoResponseRequests = this.ignoreNoResponseRequests;                               // = true - requests for 'no resonse' options are not displayed
        pca.itemDeletionPercentage = this.itemDeletionPercentage;                                   // percentage of no responses allowed within an item before the item is deleted
        pca.itemDeletionPercentageSet = this.itemDeletionPercentageSet;                             // = true when this percentage is reset
        pca.personDeletionPercentage = this.personDeletionPercentage;                               // percentage of no responses allowed within a person's responses before the person is deleted
        pca.personDeletionPercentageSet = this.personDeletionPercentageSet;                         // = true when this percentage is reset
        pca.replacementOption = this.replacementOption;                                             // option flag for a missing response if deletion not carried out
        pca.replacementOptionNames = Conv.copy(this.replacementOptionNames);
        pca.replacementOptionSet = this.replacementOptionSet;                                       // = true when replacementOption set
        pca.allNoResponseOptionsSet = this.allNoResponseOptionsSet;                                 // = true when personDeletionPercentageSet, itemDeletionPercentageSet and replacementOptionSet are all true

        pca.noResponseHandlingSet = this.noResponseHandlingSet;                                     // = true when 'no response' handling options are all set
        pca.nNaN = this.nNaN;                                                                       // number of 'no responses' (initially equated to NaN)
        pca.deletedItems = Conv.copy(this.deletedItems);                                               // = true if item corresponding to the deletedItems array index has been deleted, false otherwise
        pca.nDeletedItems = this.nDeletedItems;                                                     // number of deleted items
        pca.deletedItemsIndices = Conv.copy(this.deletedItemsIndices);                                 // indices of the deleted items
        pca.itemIndices = Conv.copy(this.itemIndices);                                                 // indices of items in original data before deletions
        pca.deletedPersons = Conv.copy(this.deletedPersons);                                           // = true if person corresponding to the deletedItems array index has been deleted, false otherwise                                                                                   //   person deleted if no response in all items,then deleted irrespective of missing response option choice
        pca.nDeletedPersons = this.nDeletedPersons;                                                 // number of deleted persons
        pca.deletedPersonsIndices = Conv.copy(this.deletedPersonsIndices);                             // indices of the deleted persons
        pca.personIndices = Conv.copy(this.personIndices);                                             // indices of persons in original data before deletions
        pca.nReplacements = this.nReplacements;                                                     // number of 'no response' replacements
        pca.replacementIndices = Conv.copy(replacementIndices);                                        // indices of 'no response' replacements

        pca.nFactorOption = this.nFactorOption;
        if(this.dataEntered){
            pca.dataPreprocessed = false;                                                           // = true when scores have been preprocessed
            pca.preprocessData();
        }

        return pca;
    }

    // Convert to Cronbach
    public Cronbach toCronbach(){
        Cronbach cr = new Cronbach();

        cr.title = this.title;
        cr.titleLines = this.titleLines;
        cr.inputFilename = this.inputFilename;
        cr.outputFilename = this.outputFilename;
        cr.fileOption = this.fileOption;
        cr.fileOptionSet = this.fileOptionSet;
        cr.fileExtensions = this.fileExtensions;
        cr.fileNumberingSet = this.fileNumberingSet;

        cr.originalDataType = this.originalDataType;                                                // - String[][]  (including read from file);
        cr.originalDataOrder = this.originalDataOrder;                                              // - matrix columns = responses of a person
        cr.originalData = this.originalData;                                                        // Original data as entered
        cr.scores0 = Conv.copy(this.scores0);                                                          // individual scores -  after any 'no response' deletions or replacements
        cr.originalScores0 = Conv.copy(this.originalScores0);                                          // scores0 before any 'no response' deletions or replacements
        cr.standardizedScores0 = Conv.copy(this.standardizedScores0);                                  // standardized scores0
        cr.scores1 = Conv.copy(this.scores1);                                                          // individual scores -  after any 'no response' deletions or replacements
        cr.originalScores1 = Conv.copy(this.originalScores1);                                          // scores1 before any 'no response' deletions or replacements
        cr.standardizedScores1 = Conv.copy(this.standardizedScores1);                                  // standardized scores1
        cr.dataEntered = this.dataEntered;                                                          // = true when scores entered
        cr.nItems = this.nItems;                                                                    // number of items, after any deletions
        cr.originalNitems = this.originalNitems;                                                    // original number of items
        cr.itemNames = Conv.copy(this.itemNames);                                                      // names of the items
        cr.originalItemNames = Conv.copy(this.originalItemNames);                                      // list of item names before any deletions
        cr.itemNamesSet = this.itemNamesSet;                                                        // = true when item names entered
        cr.nPersons = this.nPersons;                                                                // number of persons, after any deletions
        cr.originalNpersons = this.originalNpersons;                                                // original number of persons
        cr.nScores = this.nScores;
        cr.originalNscores = this.originalNscores;                                                  // original total number of scores
        cr.otherFalse = this.otherFalse;                                                            // false value for dichotomous data if one of the default values
        cr.otherTrue = this.otherTrue;                                                              // true value for dichotomous data if one of the default values
        cr.otherDichotomousDataSet = this.otherDichotomousDataSet;                                  // = true if user sets an alternative dichotomous pair
        cr.dichotomous = Conv.copy(this.dichotomous);                                                  // true if the data in an item is dichotomous
        cr.dichotomousPercentage = Conv.copy(this.dichotomousPercentage);                              // percentage of responses in an item that are dichotomous
        cr.dichotomousOverall = this.dichotomousOverall;                                            // true if all the data is dichotomous
        cr.dichotomousCheckDone = this.dichotomousCheckDone;                                        // true if check for dichotomous data performed
        cr.letterToNumeralSet = this.letterToNumeralSet;                                            // = true if user set the letter to numeral option allowing alphabetic response input
        cr.ignoreNoResponseRequests = this.ignoreNoResponseRequests;                                // = true - requests for 'no resonse' options are not displayed
        cr.itemDeletionPercentage = this.itemDeletionPercentage;                                    // percentage of no responses allowed within an item before the item is deleted
        cr.itemDeletionPercentageSet = this.itemDeletionPercentageSet;                              // = true when this percentage is reset
        cr.personDeletionPercentage = this.personDeletionPercentage;                                // percentage of no responses allowed within a person's responses before the person is deleted
        cr.personDeletionPercentageSet = this.personDeletionPercentageSet;                          // = true when this percentage is reset
        cr.replacementOption = this.replacementOption;                                              // option flag for a missing response if deletion not carried out
        cr.replacementOptionNames = Conv.copy(this.replacementOptionNames);
        cr.replacementOptionSet = this.replacementOptionSet;                                        // = true when replacementOption set
        cr.allNoResponseOptionsSet = this.allNoResponseOptionsSet;                                  // = true when personDeletionPercentageSet, itemDeletionPercentageSet and replacementOptionSet are all true

        cr.noResponseHandlingSet = this.noResponseHandlingSet;                                      // = true when 'no response' handling options are all set
        cr.nNaN = this.nNaN;                                                                        // number of 'no responses' (initially equated to NaN)
        cr.deletedItems = Conv.copy(this.deletedItems);                                                // = true if item corresponding to the deletedItems array index has been deleted, false otherwise
        cr.nDeletedItems = this.nDeletedItems;                                                      // number of deleted items
        cr.deletedItemsIndices = Conv.copy(this.deletedItemsIndices);                                  // indices of the deleted items
        cr.itemIndices = Conv.copy(this.itemIndices);                                                  // indices of items in original data before deletions
        cr.deletedPersons = Conv.copy(this.deletedPersons);                                            // = true if person corresponding to the deletedItems array index has been deleted, false otherwise                                                                                   //   person deleted if no response in all items,then deleted irrespective of missing response option choice
        cr.nDeletedPersons = this.nDeletedPersons;                                                  // number of deleted persons
        cr.deletedPersonsIndices = Conv.copy(this.deletedPersonsIndices);                              // indices of the deleted persons
        cr.personIndices = Conv.copy(this.personIndices);                                              // indices of persons in original data before deletions
        cr.nReplacements = this.nReplacements;                                                      // number of 'no response' replacements
        cr.replacementIndices = Conv.copy(replacementIndices);                                         // indices of 'no response' replacements

        cr.nFactorOption = this.nFactorOption;
        if(this.dataEntered){
            cr.dataPreprocessed = false;                                                            // = true when scores have been preprocessed
            cr.preprocessData();
        }

        return cr;
    }
}






