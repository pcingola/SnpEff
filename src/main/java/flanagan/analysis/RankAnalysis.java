/*
*   Class   RankAnalysis
*
*   USAGE:  Matrix Rank Analysis
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    August -  September 2008
*   UPDATE:  12 October 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RankAnalysis.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
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

import flanagan.analysis.Stat;
import flanagan.analysis.Cronbach;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.math.Matrix;
import flanagan.math.ArrayMaths;
import flanagan.io.FileOutput;

import java.util.ArrayList;
import java.util.Vector;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.text.*;

public class RankAnalysis{

    private double[][] values = null;                   // matrix of values whose rank is required
    private double[][] errors = null;                   // matrix of errors of the values

    private double[] valuesDiagonal = null;             // diagonal of values whose rank is required
    private double[] errorsDiagonal = null;             // diagonal of errors of the values

    private double[][] reducedValues = null;            // reduced matrix of values whose rank is required
    private double[][] reducedErrors = null;            // reduced matrix of standard deviations of the values

    private double[] reducedValuesDiagonal = null;      // diagonal of reduced values
    private double[] reducedErrorsDiagonal = null;      // diagonal of reduved errors
    private double[] reducedValueOverError = null;      // ratio of reduced value diagonal over reduced error diagonal
    private double[] probabilityValues = null;          // P-values for above ratios
    private double[] mcMullen = null;                   // Criteria of McMullen, Jaskunas and Tinoco

    private int numberOfRows = 0;                       // number of rows
    private int numberOfColumns = 0;                    // number of columns
    private int diagonalLength = 0;                     // length of diagonal

    private int errorType = 3;                          // = 0  matrix of individual errors supplied
                                                        // = 1  common error for all elements in each each row supplied
                                                        // = 2  single common error for all elements in the matrix supplied
                                                        // = 3  no error/s supplied

    private double[] errorRowMeans = null;              // means of the rows of errors
    private double[] errorColumnMeans = null;           // means of the columns of errors

    private int numberOfMissingErrors = 0;              // number of missing errors (entered as NaN)
    private boolean rowOption = true;                   // = true - missing errors replaced by the appropriate row mean
                                                        // = false - missing errors replaced by the appropriate column mean

    private boolean rankAnalysisDone = false;           // = true when rank analysis performed


    // CONSTRUCTORS
    // Individual error for each value
    public RankAnalysis(double[][] values, double[][] errors){
        this.values = Conv.copy(values);
        this.errors = Conv.copy(errors);
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(float[][] values, float[][] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(long[][] values, long[][] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(int[][] values, int[][] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(BigDecimal[][] values, BigDecimal[][] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(BigInteger[][] values, BigInteger[][] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(ArrayMaths[] values, ArrayMaths[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(ArrayList<Object>[] values, ArrayList<Object>[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(Vector<Object>[] values, Vector<Object>[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        Matrix mate = new Matrix(errors);
        this.errors = mate.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }

    public RankAnalysis(Matrix values, Matrix errors){
        this.values = values.getArrayCopy();
        this.errors = errors.getArrayCopy();
        this.errorType = 0;
        this.preprocessDataOne();
    }



    // Common error for each row
    public RankAnalysis(double[][] values, double[] errors){
        this.values = Conv.copy(values);
        this.errors = this.oneToTwo(Conv.copy(errors), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(float[][] values, float[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }


    public RankAnalysis(long[][] values, long[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(int[][] values, int[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(BigDecimal[][] values, BigDecimal[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(BigInteger[][] values, BigInteger[] errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(ArrayMaths[] values, ArrayMaths errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errors = this.oneToTwo(errors.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(ArrayList<Object>[] values, ArrayList<Object> errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(Vector<Object>[] values, Vector<Object> errors){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(Scores values){
        this.values = values.usedScoresAsRowPerItem();
        Matrix mat = new Matrix(this.values);
        double[] errors = mat.rowStandardDeviations();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(Cronbach values){
        this.values = values.usedScoresAsRowPerItem();
        Matrix mat = new Matrix(this.values);
        double[] errors = mat.rowStandardDeviations();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    public RankAnalysis(PCA values){
        this.values = values.usedScoresAsRowPerItem();
        Matrix mat = new Matrix(this.values);
        double[] errors = mat.rowStandardDeviations();
        ArrayMaths ame = new ArrayMaths(errors);
        this.errors = this.oneToTwo(ame.array(), this.values[0].length);
        this.errorType = 1;
        this.preprocessDataOne();
    }

    // Common error for all values
    public RankAnalysis(double[][] values, double commonError){
        this.values = Conv.copy(values);
        this.errorType = 2;
        this.preprocessDataTwo(commonError);
    }

    public RankAnalysis(float[][] values, float commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo((double)commonError);
    }

    public RankAnalysis(long[][] values, long commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo((double)commonError);
    }

    public RankAnalysis(int[][] values, int commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo((double)commonError);
    }

    public RankAnalysis(BigDecimal[][] values, BigDecimal commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError.doubleValue());
    }

    public RankAnalysis(BigInteger[][] values, BigInteger commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError.doubleValue());
    }

    public RankAnalysis(ArrayMaths[] values, double commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError);
    }

    public RankAnalysis(ArrayList<Object>[] values, double commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError);
    }

    public RankAnalysis(Vector<Object>[] values, double commonError){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError);
    }

    public RankAnalysis(Matrix values, double commonError){
        this.values = values.getArrayCopy();
        this.errorType = 2;
        this.preprocessDataTwo(commonError);
    }



    // No errors supplied
    public RankAnalysis(double[][] values){
        this.values = Conv.copy(values);
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(float[][] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(long[][] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(int[][] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(BigDecimal[][] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(BigInteger[][] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(ArrayMaths[] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(ArrayList<Object>[] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(Vector<Object>[] values){
        Matrix matv = new Matrix(values);
        this.values = matv.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }

    public RankAnalysis(Matrix values){
        this.values = values.getArrayCopy();
        this.errorType = 3;
        this.preprocessDataThree();
    }




    // Convets common error per row to individual errors for all rows
    private double[][] oneToTwo(double[] errors, int nCols){
        int nRows = errors.length;
        double[][] ret = new double[nRows][nCols];
        for(int i=0; i<nRows; i++){
            for(int j=0; j<nCols; j++){
                ret[i][j] = errors[i];
            }
        }
        return ret;
    }


    // Preprocess data with individual errors supplied directly
    // or after common row error converted to individual errors by private method oneToTwo()
    private void preprocessDataOne(){
        // Check row and column lengths
        this.numberOfRows = this.values.length;
        this.numberOfColumns = this.values[0].length;
        for(int i=1; i<this.numberOfRows; i++){
                if(this.values[i].length!=this.numberOfColumns)throw new IllegalArgumentException("All rows of the value matrix must be of the same length");
        }
        for(int i=0; i<this.numberOfRows; i++){
                if(this.errors[i].length!=this.numberOfColumns)throw new IllegalArgumentException("All rows of the error matrix must be of the same length as those of the value matrix");
        }
        this.diagonalLength = this.numberOfRows;
        if(this.numberOfRows>this.numberOfColumns)this.diagonalLength = this.numberOfColumns;

        // Convert errors to variances
        for(int i=0; i<this.numberOfRows; i++){
            for(int j=0; j<this.numberOfColumns; j++){
                this.errors[i][j] *= this.errors[i][j];
            }
        }
    }

    // Preprocess data witha single common error supplied
    private void preprocessDataTwo(double commonError){
        // Check row and column lengths
        this.numberOfRows = this.values.length;
        this.numberOfColumns = this.values[0].length;
        for(int i=1; i<this.numberOfRows; i++){
                if(this.values[i].length!=this.numberOfColumns)throw new IllegalArgumentException("All rows of the value matrix must be of the same length");
        }
        this.diagonalLength = this.numberOfRows;
        if(this.numberOfRows>this.numberOfColumns)this.diagonalLength = this.numberOfColumns;

        // Fill errors matrix
        this.errors = new double[this.numberOfRows][this.numberOfColumns];
        for(int i=0; i<this.numberOfRows; i++){
            for(int j=0; j<this.numberOfColumns; j++){
                this.errors[i][j] = commonError*commonError;
            }
        }
    }

    // Preprocess data with no errors supplied
    // Each error substituted by a very rough estimate of the potential rounding error
    private void preprocessDataThree(){
        // Check row and column lengths
        this.numberOfRows = this.values.length;
        this.numberOfColumns = this.values[0].length;
        for(int i=1; i<this.numberOfRows; i++){
                if(this.values[i].length!=this.numberOfColumns)throw new IllegalArgumentException("All rows of the value matrix must be of the same length");
        }
        this.diagonalLength = this.numberOfRows;
        if(this.numberOfRows>this.numberOfColumns)this.diagonalLength = this.numberOfColumns;

        // Fill errors matrix
        this.errors = new double[this.numberOfRows][this.numberOfColumns];
        double error = 0.0;
        for(int i=0; i<this.numberOfRows; i++){
            for(int j=0; j<this.numberOfColumns; j++){
                error = Math.pow(10.0, Math.floor(Math.log10(Math.abs(this.values[i][j]))))*5.0E-16;
                this.errors[i][j] = error*error;
            }
        }
    }

    // Missing error options
    // Use error row mean to replace a missing error
    // This is the default option
    public void useErrorRowMean(){
        this.rowOption = true;
    }

    // Use error column mean to replace a missing error
    public void useErrorColumnMean(){
        this.rowOption = false;
    }

    // Return number of replaced missing errors
    public int nMissingErrors(){
        return this.numberOfMissingErrors;
    }


    // Rank analysis
    private void rankAnalysis(){

        // Check errors for negative values and missing values (entered as NaN)

        // Row means and negative errors multiplied by -1
        this.errorRowMeans = new double[this.numberOfRows];
        this.errorColumnMeans = new double[this.numberOfColumns];
        this.numberOfMissingErrors = 0;
        for(int i=0; i<this.numberOfRows; i++){
            int counter = 0;
             for(int j=0; j<this.numberOfColumns; j++){
                if(!Double.isNaN(this.errors[i][j])){
                    if(this.errors[i][j]<0.0)this.errors[i][j] *= -1.0;
                    this.errorRowMeans[i] += this.errors[i][j];
                    counter++;
                }
                else{
                    this.numberOfMissingErrors++;
                }
             }
             this.errorRowMeans[i] /= counter;
        }

        // Column means
        for(int i=0; i<this.numberOfColumns; i++){
            int counter = 0;
             for(int j=0; j<this.numberOfRows; j++){
                if(!Double.isNaN(this.errors[j][i])){
                    this.errorColumnMeans[i] += this.errors[j][i];
                    counter++;
                }
             }
             this.errorColumnMeans[i] /= counter;
        }

        // missing errors replaced by the row or column mean excluding the missing values (see missingErrorOption);
        if(this.numberOfMissingErrors>0){
            for(int i=0; i<this.numberOfRows; i++){
                for(int j=0; j<this.numberOfColumns; j++){
                    if(Double.isNaN(this.errors[i][j])){
                        if(this.rowOption){
                            this.errors[i][j] = errorRowMeans[i];
                        }
                        else{
                            this.errors[i][j] = errorColumnMeans[i];
                        }
                    }
                }
            }
        }

        // Matrix reduction

        this.reducedValues = this.values;
        this.reducedErrors = this.errors;
        Matrix matv0 = new Matrix(this.reducedValues);
        Matrix mate0 = new Matrix(this.reducedErrors);
        int nn = this.diagonalLength - 1;

        for(int i=0; i<nn; i++){
            matv0 = new Matrix(this.reducedValues);

            // Isolate sub-matrix
    	    int nrow = this.numberOfRows-i;
    	    int ncol = this.numberOfColumns - i;
            Matrix mat1 = matv0.getSubMatrix(i, i, this.numberOfRows-1, numberOfColumns-1);
            double[][] subv = mat1.getArrayCopy();

    	    // Get pivot indices
    	    int[] max = mat1.pivot();
    	    int pivotI = max[0]+i;
    	    int pivotJ = max[1]+i;

    	    // Swap rows
    	    double[] holdv1 = this.reducedValues[i];
    	    double[] holde1 = this.reducedErrors[i];
    	    this.reducedValues[i] = this.reducedValues[pivotI];
    	    this.reducedErrors[i] = this.reducedErrors[pivotI];
    	    this.reducedValues[pivotI] = holdv1;
    	    this.reducedErrors[pivotI] = holde1;

    	    // Swap columns
    	    double holdv2 = 0.0;
    	    double holde2 = 0.0;
    	    for(int j=0; j<numberOfRows; j++){
    	        holdv2 = this.reducedValues[j][i];
    	        holde2 = this.reducedErrors[j][i];
    	        this.reducedValues[j][i] = this.reducedValues[j][pivotJ];
    	        this.reducedErrors[j][i] = this.reducedErrors[j][pivotJ];
    	        this.reducedValues[j][pivotJ] = holdv2;
    	        this.reducedErrors[j][pivotJ] = holde2;
    	    }

    	    // Reduce sub-matrix
    	    Matrix matValueHold = new Matrix(this.reducedValues);
    	    Matrix matErrorHold = new Matrix(this.reducedErrors);
    	    double[][] valueHold = matValueHold.getArrayCopy();
    	    double[][] errorHold = matErrorHold.getArrayCopy();

    	    for(int j=i+1; j<this.numberOfRows; j++){
    	        for(int k=i; k<this.numberOfColumns; k++){
    	            double ratio1 = 1.0;
    	            if(this.reducedValues[j][i]!=this.reducedValues[i][i])ratio1 = this.reducedValues[j][i]/this.reducedValues[i][i];
    	            valueHold[j][k] = this.reducedValues[j][k] - ratio1*this.reducedValues[i][k];
    	            double hold = this.reducedErrors[j][k] + this.reducedErrors[i][k]*ratio1*ratio1;
    	            double ratio2 = 1.0;
    	            if(this.reducedValues[i][k]!=this.reducedValues[i][i])ratio2 = this.reducedValues[i][k]/this.reducedValues[i][i];
    	            hold += this.reducedErrors[j][i]*ratio2*ratio2;
    	            errorHold[j][k] = hold + this.reducedErrors[i][i]*ratio1*ratio1*ratio2*ratio2;
    	        }
    	    }
    	    matValueHold = new Matrix(valueHold);
    	    matErrorHold = new Matrix(errorHold);
    	    this.reducedValues = matValueHold.getArrayCopy();
    	    this.reducedErrors = matErrorHold.getArrayCopy();
      	}

      	// Convert errors to standard deviations
      	for(int i=0; i<this.numberOfRows; i++){
    	    for(int j=0; j<this.numberOfColumns; j++){
    	        this.reducedErrors[i][j] = Math.sqrt(this.reducedErrors[i][j]);
    	    }
    	}

      	// Fill zero elements
    	for(int i=1; i<this.diagonalLength; i++){
    	    for(int j=0; j<i; j++){
    	        this.reducedValues[i][j] = 0.0;
    	        this.reducedErrors[i][j] = 0.0;
    	    }
    	}

    	if(this.diagonalLength<this.numberOfRows){
    	    for(int i=this.diagonalLength; i<this.numberOfRows; i++){
    	        for(int j=0; j<this.numberOfColumns; j++){
    	            this.reducedValues[i][j] = 0.0;
    	            this.reducedErrors[i][j] = 0.0;
    	        }
    	    }
    	}

        // store diagonals and calculate probability values
        this.reducedValuesDiagonal = new double[this.diagonalLength];
        this.reducedErrorsDiagonal = new double[this.diagonalLength];
        this.reducedValueOverError = new double[this.diagonalLength];
        this.probabilityValues = new double[this.diagonalLength];
        this.mcMullen = new double[this.numberOfRows];

        for(int i=0; i<this.diagonalLength; i++){
            this.reducedValuesDiagonal[i] = this.reducedValues[i][i];
            this.reducedErrorsDiagonal[i] = this.reducedErrors[i][i];
            this.reducedValueOverError[i] = Math.abs(this.reducedValuesDiagonal[i]/this.reducedErrorsDiagonal[i]);
            this.probabilityValues[i] = 1.0 - Stat.gaussianCDF(0.0, 1.0, -this.reducedValueOverError[i], this.reducedValueOverError[i]);
        }

        // calculate criteria of McMullen, Jaskunas and Tinoco
        for(int i=0; i<this.numberOfRows; i++){
            double sum = 0.0;
    	    for(int j=i; j<this.numberOfColumns; j++){
    	        sum += this.reducedValues[i][j]*this.reducedValues[i][j];
    	    }
    	    this.mcMullen[i] = Math.sqrt(sum)/(this.numberOfColumns-i);
    	}

    	this.rankAnalysisDone = true;
    }

    //  Return an analysis to a text file
    public void analysis(){
        this.analysis("RankAnalysisOutput.txt");
    }

    public void analysis(String fileName){
        if(!this.rankAnalysisDone)this.rankAnalysis();

        int posdot = fileName.indexOf(".");
        if(posdot==-1)fileName = fileName + ".txt";

        FileOutput fout = new FileOutput(fileName);
        fout.println("Rank Analysis");
        fout.println("File name:   " + fileName);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();

        fout.println("Number of rows    " + this.numberOfRows);
        fout.println("Number of columns " + this.numberOfColumns);
        if(this.numberOfMissingErrors>0){
            fout.println("Number of substituted missing errors" + this.numberOfMissingErrors);
            if(this.rowOption){
                fout.println("Row means used as the substituted value/s");
            }
            else{
                fout.println("Column means used as the substituted value/s");
            }
        }
        fout.println();

        switch(this.errorType){
            case 0: fout.println("Matrix of individual errors supplied");
                    break;
            case 1: fout.println("Common error for all elements in each each row supplied");
                    break;
            case 2: fout.println("Single common error for all elements in the matrix supplied");
                    break;
            case 3: fout.println("No errors supplied - estimate of the rounding errors used");
        }
        fout.println();

        int field1 = 30;
        int field2 = 15;
        int trunc = 4;
        if(this.errorType!=3){
            fout.print("Reduced", field2);
            fout.print("Reduced", field2);
            fout.print("V/E Ratio", field2);
            fout.print("P-value", field2);
            fout.println("McMullen");

            fout.print("Value", field2);
            fout.print("Error", field2);
            fout.print("    ", field2);
            fout.print("    ", field2);
            fout.println("rms");

            fout.print("Diagonal (V)", field2);
            fout.print("Diagonal (E)", field2);
            fout.print("   ", field2);
            fout.print("   ", field2);
            fout.println("   ");
        }
        else{
            fout.print("Reduced", field2);
            fout.print("Reduced", field2);
            fout.print("V/E Ratio", field2);
            fout.print("P-value", field2);
            fout.println("McMullen");

            fout.print("Value", field2);
            fout.print("Estimated", field2);
            fout.print("    ", field2);
            fout.print("    ", field2);
            fout.println("rms");

            fout.print("Diagonal (V)", field2);
            fout.print("Rounding", field2);
            fout.print("   ", field2);
            fout.print("   ", field2);
            fout.println("   ");

            fout.print("   ", field2);
            fout.print("Error (E)", field2);
            fout.print("   ", field2);
            fout.print("   ", field2);
            fout.println("   ");
        }

        for(int i=0; i<this.diagonalLength; i++){
            fout.print(Fmath.truncate(this.reducedValuesDiagonal[i], trunc), field2);
            fout.print(Fmath.truncate(this.reducedErrorsDiagonal[i], trunc), field2);
            fout.print(Fmath.truncate(this.reducedValueOverError[i], trunc), field2);
            fout.print(Fmath.truncate(this.probabilityValues[i], trunc), field2);
            fout.println(Fmath.truncate(this.mcMullen[i], trunc));
        }

        System.out.println("Analysis written to text file " + fileName);

        fout.close();
    }


    //  Return original values matrix
    public double[][] originalValues(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.values;
    }

    //  Return original error matrix
    public double[][] originalErrors(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.errors;
    }

    //  Return reduced values matrix
    public double[][] reducedValues(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.reducedValues;
    }

    //  Return reduced errors matrix
    public double[][] reducedErrors(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.reducedErrors;
    }

    //  Return reduced values diagonal
    public double[] reducedValuesDiagonal(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.reducedValuesDiagonal;
    }

    //  Return reduced errors diagonal
    public double[] reducedErrorsDiagonal(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.reducedErrorsDiagonal;
    }

    //  Return reduced value over reduced errors diagonal
    public double[] reducedRatiosDiagonal(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.reducedValueOverError;
    }

    //  Return probabilty values diagonal
    public double[] probabilityValues(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.probabilityValues;
    }

    //  Return McMullen values
    public double[] mcMullenValues(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.mcMullen;
    }

    // return number of rows
    public int nRows(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.numberOfRows;
    }

    // return number of columns
    public int nColumns(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.numberOfColumns;
    }

    // return number of diagonal elements
    public int nDiagonalElements(){
        if(!this.rankAnalysisDone)this.rankAnalysis();
        return this.diagonalLength;
    }

}