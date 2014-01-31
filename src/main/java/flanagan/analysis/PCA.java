/*
*   CLASS:      PCA
*
*   USAGE:      Principlal Component Analysis
*
*   This is a subclass of the superclass Scores
*
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       October 2008
*   AMENDED:    17-18 October 2008, 4 January 2010, 13 November 2010, 29-30 November 2010, 4 December 2010, 18 January 2011, 17-18 July 2011
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PCA.html
*
*   Copyright (c) 2008 - 2011 Michael Thomas Flanagan
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
import java.awt.*;

import flanagan.math.*;
import flanagan.io.*;
import flanagan.analysis.*;
import flanagan.plot.*;


public class PCA extends Scores{

    private Matrix data = null;                                         // data as row per item as a Matrix
    private Matrix dataMinusMeans = null;                               // data with row means subtracted as row per item as a Matrix
    private Matrix dataMinusMeansTranspose = null;                      // data with row means subtracted as row per item as a Matrix
    private Matrix covarianceMatrix = null;                             // variance-covariance Matrix
    private Matrix correlationMatrix = null;                            // correlation Matrix
    private Matrix partialCorrelationMatrix = null;                     // partial correlation Matrix

    private double kmo = 0.0;                                           // overall Kaiser-Meyer-Olkin (KMO) statistic
    private double[] itemKMOs = null;                                   // individual item KMOs
    private double chiSquareBartlett = 0.0;                             // Bartlett test of sphericity chi square
    private int dfBartlett = 0;                                         // Bartlett test of sphericity degrees of freedom
    private double probBartlett = 0.0;                                  // Bartlett test of sphericity chi square probability
    private double sign10Bartlett = 0.0;                                // Bartlett test of sphericity chi square value at 10% significance level
    private double sign05Bartlett = 0.0;                                // Bartlett test of sphericity chi square value at 5% significance level

    private double[] eigenValues = null;                                // eigenvalues
    private double[] orderedEigenValues = null;                         // eigenvalues sorted into a descending order
    private int[] eigenValueIndices = null;                             // indices of the eigenvalues before sorting into a descending order
    private double eigenValueTotal = 0.0;                               // total of all eigen values;
    private int[] rotatedIndices = null;                                // rearranged indices on ordering after rotation

    private double[] rotatedEigenValues = null;                         // scaled rotated eigenvalues
    private double[] usRotatedEigenValues = null;                       // unscaled rotated eigenvalues


    private int nMonteCarlo = 200;                                      // number of Monte Carlo generated eigenvalue calculations
    private double[][] randomEigenValues = null;                        // Monte Carlo generated eigenvalues
    private double[] randomEigenValuesMeans = null;                     // means of the Monte Carlo generated eigenvalues
    private double[] randomEigenValuesSDs = null;                       // standard deviations of the Monte Carlo generated eigenvalues
    private double[] randomEigenValuesPercentiles = null;               // percentiles of the Monte Carlo generated eigenvalues
    private double percentile = 95.0;                                   // percentile used in parallel analysis
    private boolean gaussianDeviates = false;                           // = false: uniform random deviates used in Monte Carlo
                                                                        // = true:  Gaussian random deviates used in Monte Carlo
    private double[] proportionPercentage = null;                       // eigenvalues expressesed as percentage of total
    private double[] cumulativePercentage = null;                       // cumulative values of the eigenvalues expressesed as percentage of total
    private double[] rotatedProportionPercentage = null;                // scaled rotated eigenvalues expressesed as percentage of unrotated total
    private double[] rotatedCumulativePercentage = null;                // scaled rotated cumulative values of the eigenvalues expressesed as percentage of unrotated total

    private double[][] eigenVectorsAsColumns = null;                    // eigenvectors as columns
    private double[][] eigenVectorsAsRows = null;                       // eigenvectors as rows

    private double[][] orderedEigenVectorsAsColumns = null;             // eigenvectors, as columns, arranged to match a descending order of eigenvalues
    private double[][] orderedEigenVectorsAsRows = null;                // eigenvectors, as rows, arranged to match a descending order of eigenvalues

    private double[][] loadingFactorsAsColumns = null;                  // loading factors as column per eigenvalue
    private double[][] loadingFactorsAsRows = null;                     // loading factors as row per eigenvalue

    private double[][] rotatedLoadingFactorsAsColumns = null;           // scaled rotated loading factors as column per eigenvalue
    private double[][] rotatedLoadingFactorsAsRows = null;              // scaled rotated loading factors as row per eigenvalue
    private double[][] usRotatedLoadingFactorsAsColumns = null;         // unscaled rotated loading factors as column per eigenvalue
    private double[][] usRotatedLoadingFactorsAsRows = null;            // unscaled rotated loading factors as row per eigenvalue

    private double[] communalities = null;                              // communalities
    private double[] communalityWeights = null;                         // communality weights

    private boolean covRhoOption = false;                               // = true:   covariance matrix used
                                                                        // = false:  correlation matrix used

    private int greaterThanOneLimit = 0;                                // number of components extracted using eigenvalue greater than one
    private int percentileCrossover = 0;                                // number of components extracted using percentile scree crossover
    private int meanCrossover = 0;                                      // number of components extracted using mean scree crossover

    private int nVarimaxMax = 1000;                                     // maximum iterations allowed by the varimax criterion
    private int nVarimax = 0;                                           // number of iterations taken by the varimax criterion
    private double varimaxTolerance = 1.0E-8;                           // tolerance for terminatiing 2 criterion iteration

    private boolean varimaxOption = true;                               // = true:  normal varimax, i.e. comunality weighted varimax
                                                                        // = false: raw varimax
    private boolean pcaDone = false;                                    // = true when PCA performed
    private boolean monteCarloDone = false;                             // = true when parallel monte Carlo simultaion performed
    private boolean rotationDone = false;                               // = true when rotation performed



    // CONSTRUCTOR
    public PCA(){
        super.trunc = 4;
    }

    // CHOICE OF MATRIX
    // Use covariance matrix (default option)
    public void useCovarianceMatrix(){
        this.covRhoOption = true;
    }

    // Use correlation matrix (default option)
    public void useCorrelationMatrix(){
        this.covRhoOption = false;
    }

    // CHOICE OF VARIMAX CRITERION
    // Use normal varimax rotation
    public void useNormalVarimax(){
        this. varimaxOption = true;
    }

    // Use raw varimax rotation
    public void useRawVarimax(){
        this. varimaxOption = false;
    }

    // Return varimax rotation option
    public String getVarimaxOption(){
        if(this. varimaxOption){
            return "normal varimax option";
        }
        else{
            return "raw varimax option";
        }
    }

    // PARALLEL ANALYSIS OPTIONS
    // Reset number of Monte Carlo simulations
    public void setNumberOfSimulations(int nSimul){
        this.nMonteCarlo = nSimul;
    }

    // Return number of Monte Carlo simulations
    public int getNumberOfSimulations(){
        return this.nMonteCarlo;
    }

    // Use Gaussian random deviates in MontMonte Carlo simulations
    public void useGaussianDeviates(){
        this.gaussianDeviates = true;
    }

    // Use uniform random deviates in MontMonte Carlo simulations
    public void useUniformDeviates(){
        this.gaussianDeviates = false;
    }

    // Reset percentile percentage in parallel analysis (defalut option = 95%)
    public void setParallelAnalysisPercentileValue(double percent){
        this.percentile = percent;
    }

    // Return percentile percentage in parallel analysis (defalut option = 95%)
    public double getParallelAnalysisPercentileValue(){
        return this.percentile;
    }



    // PRINCIPAL COMPONENT ANALYSIS
    public void pca(){

        if(!this.pcaDone){

            if(this.nItems==1)throw new IllegalArgumentException("You have entered only one item - PCA is not meaningful");
            if(this.nPersons==1)throw new IllegalArgumentException("You have entered only one score or measurement source - PCA is not meaningful");

            // Check that data is preprocessed
            if(!this.dataPreprocessed)this.preprocessData();

            // Store data as an instance of matrix
            this.data = new Matrix(super.scores0);

            // Subtract row means
            this.dataMinusMeans = this.data.subtractRowMeans();

            // Transpose
            this.dataMinusMeansTranspose = this.dataMinusMeans.transpose();

            // Covariance matrix
            this.covarianceMatrix = this.dataMinusMeans.times(this.dataMinusMeansTranspose);
            double denom = this.nPersons;
            if(!super.nFactorOption)denom -= 1.0;
            this.covarianceMatrix = this.covarianceMatrix.times(1.0/denom);



            // Correlation matrix
            boolean tinyCheck = false;
            double[][] cov = this.covarianceMatrix.getArrayCopy();
            double[][] corr = new double[this.nItems][this.nItems];
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nItems; j++){
                    if(i==j){
                        corr[i][j] = 1.0;
                    }
                    else{
                        corr[i][j] = cov[i][j]/Math.sqrt(cov[i][i]*cov[j][j]);
                        if(Fmath.isNaN(corr[i][j])){
                            corr[i][j] = 0.0;
                        }
                    }
                }
            }
            this.correlationMatrix = new Matrix(corr);

            // Partial Correlation matrix
            double[][] cofactors = new double[this.nItems][this.nItems];
            double[][] partialCorr = new double[this.nItems][this.nItems];
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nItems; j++){
                    cofactors[i][j] = this.correlationMatrix.cofactor(i,j);
                }
            }
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nItems; j++){
                    if(cofactors[i][j]==0.0 && cofactors[i][i]==0.0 && cofactors[j][j]==0.0){
                        partialCorr[i][j] = 1.0;
                    }
                    else{
                        if(i==j){
                            partialCorr[i][j] = 1.0;
                        }
                        else{
                            partialCorr[i][j] = -cofactors[i][j]/Math.sqrt(cofactors[i][i]*cofactors[j][j]);
                        }
                    }
                }
            }
            this.partialCorrelationMatrix = new Matrix(partialCorr);

            // KMO (Kaiser-Meyer-Olkin) Statistic
            // Overall KMO
            double kmor = 0.0;
            double kmoa = 0.0;
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nItems; j++){
                    if(i!=j){
                        kmor += corr[i][j]*corr[i][j];
                        kmoa += partialCorr[i][j]*partialCorr[i][j];
                    }
                }
            }
            if(kmor==0.0 && kmoa==0.0){
                this.kmo = 0.5;
            }
            else{
                this.kmo = kmor/(kmor + kmoa);
            }

            // individual item KMOs
            this.itemKMOs = new double[this.nItems];
            for(int i=0; i<this.nItems; i++){
                kmor = 0.0;
                kmoa = 0.0;
                for(int j=0; j<this.nItems; j++){
                    if(i!=j){
                        kmor += corr[i][j]*corr[i][j];
                        kmoa += partialCorr[i][j]*partialCorr[i][j];
                    }
                }
                if(kmor==0.0 && kmoa==0.0){
                    this.itemKMOs[i] = 0.5;
                }
                else{
                    this.itemKMOs[i] = kmor/(kmor + kmoa);
                }
            }

            // Bartlett Sphericity Test
            double correlationDeterminant = this.correlationMatrix.determinant();
            this.chiSquareBartlett = -((double)(this.nPersons -1 ) - ((double)(2*this.nItems + 5)/(double)6))*Math.log(correlationDeterminant);
            this.dfBartlett = this.nItems*(this.nItems - 1)/2;
            this.probBartlett = 1.0 - Stat.chiSquareCDF(this.chiSquareBartlett, this.dfBartlett);
            this.sign10Bartlett = Stat.chiSquareInverseCDF(this.dfBartlett, 0.9);
            this.sign05Bartlett = Stat.chiSquareInverseCDF(this.dfBartlett, 0.95);

            // Choose matrix
            Matrix forEigen = null;
            if(covRhoOption){
                forEigen = this.covarianceMatrix;
            }
            else{
                forEigen = this.correlationMatrix;
            }

            // Calculate eigenvalues
            this.eigenValues = forEigen.getEigenValues();

            // Calculate ordered eigenvalues
            this.orderedEigenValues = forEigen.getSortedEigenValues();

            // Store indices of the eigenvalues before sorting into escending order
            this.eigenValueIndices = forEigen.eigenValueIndices();

            // Calculate eigenvectors
            this.eigenVectorsAsColumns = forEigen.getEigenVectorsAsColumns();
            this.eigenVectorsAsRows = forEigen.getEigenVectorsAsRows();

            // Calculate ordered eigenvectors
            this.orderedEigenVectorsAsColumns = forEigen.getSortedEigenVectorsAsColumns();
            this.orderedEigenVectorsAsRows = forEigen.getSortedEigenVectorsAsRows();

            // Express eigenvalues as percentage of total
            ArrayMaths am = new ArrayMaths(this.orderedEigenValues);
            double total = am.sum();
            am = am.times(100.0/total);
            this.proportionPercentage = am.array();

            // Calculate cumulative percentage
            this.cumulativePercentage = new double[this.nItems];
            this.cumulativePercentage[0] = this.proportionPercentage[0];
            this.eigenValueTotal = 0.0;
            for(int i=1; i<this.nItems; i++){
                this.cumulativePercentage[i] = this.cumulativePercentage[i-1] + this.proportionPercentage[i];
                this.eigenValueTotal += this.eigenValues[i];
            }


            // Calculate 'eigenvalue less than or equal to one' extraction limit
            boolean test = true;
            int counter = 0;
            while(test){
                if(this.orderedEigenValues[counter]<1.0){
                    this.greaterThanOneLimit = counter;
                    test = false;
                }
                else{
                    counter++;
                    if(counter==this.nItems){
                        this.greaterThanOneLimit = counter;
                        test = false;
                    }
                }
            }

            // Calculate loading factors
            this.loadingFactorsAsColumns = new double[this.nItems][this.nItems];
            this.loadingFactorsAsRows = new double[this.nItems][this.nItems];
            for(int i=0; i<this.nItems; i++){
                for(int j=0; j<this.nItems; j++){
                    this.loadingFactorsAsColumns[i][j] = this.orderedEigenVectorsAsColumns[i][j]*Math.sqrt(Math.abs(this.orderedEigenValues[j]));
                    this.loadingFactorsAsRows[i][j] = this.orderedEigenVectorsAsRows[i][j]*Math.sqrt(Math.abs(this.orderedEigenValues[i]));
                }
            }

            // Calculate communalities
            this.communalities = new double[this.nItems];
            this.communalityWeights = new double[this.nItems];
            for(int k=0; k<this.nItems; k++){
                double sum = 0.0;
                for(int j=0; j<this.nItems; j++)sum += loadingFactorsAsRows[j][k]*loadingFactorsAsRows[j][k];
                this.communalities[k] = sum;
                this.communalityWeights[k] = Math.sqrt(this.communalities[k]);
            }

        }

        this.pcaDone = true;

    }

    // MonteCarlo Eigenvalues
    public void monteCarlo(){
        if(!pcaDone)this.pca();
        double[] rowMeans = super.rawItemMeans();
        double[] rowSDs = super.rawItemStandardDeviations();
        double[][] randomData = new double[super.nItems][super.nPersons];
        this.randomEigenValues = new double[this.nMonteCarlo][super.nItems];
        PsRandom rr = new PsRandom();
        for(int i=0; i<this.nMonteCarlo; i++){
            for(int j=0; j<this.nItems; j++){
                if(this.gaussianDeviates){
                    randomData[j] = rr.gaussianArray(rowMeans[j], rowSDs[j], super.nPersons);
                }
                else{
                    randomData[j] = rr.doubleArray(super.nPersons);
                    randomData[j] = Stat.scale(randomData[j], rowMeans[j], rowSDs[j]);
                }
            }
            PCA pca = new PCA();
            if(this.covRhoOption){
                pca.useCovarianceMatrix();
            }
            else{
                pca.useCorrelationMatrix();
            }
            pca.enterScoresAsRowPerItem(randomData);
            this.randomEigenValues[i] = pca.orderedEigenValues();

        }
        Matrix mat = new Matrix(randomEigenValues);
        this.randomEigenValuesMeans = mat.columnMeans();
        this.randomEigenValuesSDs = mat.columnStandardDeviations();
        this.randomEigenValuesPercentiles = new double[this.nItems];

        int pIndex1 = (int)Math.ceil(this.nMonteCarlo*this.percentile/100.0);
        int pIndex2 = pIndex1-1;
        double factor = (this.percentile*this.nMonteCarlo/100.0 - pIndex2);
        pIndex1--;
        pIndex2--;
        for(int j=0; j<this.nItems; j++){
            double[] ordered = new double[this.nMonteCarlo];
            for(int k=0; k<this.nMonteCarlo; k++)ordered[k] = this.randomEigenValues[k][j];
            ArrayMaths am = new ArrayMaths(ordered);
            am = am.sort();
            ordered = am.array();
            this.randomEigenValuesPercentiles[j] = ordered[pIndex2] + factor*(ordered[pIndex1] - ordered[pIndex2]);
        }

        // Calculate percentile crossover extraction limit
        boolean test = true;
        int counter = 0;
        while(test){
            if(this.orderedEigenValues[counter]<=this.randomEigenValuesPercentiles[counter]){
                this.percentileCrossover = counter;
                test = false;
            }
            else{
                counter++;
                if(counter==this.nItems){
                    this.percentileCrossover = counter;
                    test = false;
                }
            }
        }

        // Calculate mean crossover extraction limit
        test = true;
        counter = 0;
        while(test){
            if(this.orderedEigenValues[counter]<=this.randomEigenValuesMeans[counter]){
                this.meanCrossover = counter;
                test = false;
            }
            else{
                counter++;
                if(counter==this.nItems){
                    this.meanCrossover = counter;
                    test = false;
                }
            }
        }

        this.monteCarloDone = true;

    }

    // SCREE PLOTS
    // Scree plot of data alone
    public void screePlotDataAlone(){
        if(!this.pcaDone)this.pca();

        // Create X-axis data array
        double[] components = new double[super.nItems];
        for(int i=0; i<this.nItems; i++)components[i] = i+1;

        // Create instance of PlotGraph
        PlotGraph pg = new PlotGraph(components, this.orderedEigenValues);
        pg.setGraphTitle("Principal Component Analysis Scree Plot");
        pg.setXaxisLegend("Component");
        pg.setYaxisLegend("Eigenvalues");
        pg.setLine(3);
        pg.setPoint(1);
        pg.plot();
    }


    // Scree plot eigenvalues plus plot of Monte Carlo percentiles, means and standard deviations
    public void screePlot(){
        if(!this.pcaDone)this.pca();
        if(!this.monteCarloDone)this.monteCarlo();

        // Create plotting data array
        double[][] plotData = new double[6][super.nItems];
        double[] components = new double[super.nItems];
        for(int i=0; i<this.nItems; i++)components[i] = i+1;
        plotData[0] = components;
        plotData[1] = this.orderedEigenValues;
        plotData[2] = components;
        plotData[3] = this.randomEigenValuesPercentiles;
        plotData[4] = components;
        plotData[5] = this.randomEigenValuesMeans;

        // Create instance of PlotGraph
        PlotGraph pg = new PlotGraph(plotData);
        pg.setErrorBars(2, this.randomEigenValuesSDs);
        if(this.gaussianDeviates){
            pg.setGraphTitle("Principal Component Analysis Scree Plot with Parallel Analysis using Gaussian deviates (" + nMonteCarlo + " simulations)");
        }
        else{
            pg.setGraphTitle("Principal Component Analysis Scree Plot with Parallel Analysis using uniform deviates (" + nMonteCarlo + " simulations)");
        }
        pg.setGraphTitle2("Closed squares - data eigenvalues; open circles = Monte Carlo eigenvalue " + this.percentile + "% percentiles; error bars = standard deviations about the Monte carlo means (crosses)");
        pg.setXaxisLegend("Component");
        pg.setYaxisLegend("Eigenvalue");
        int[] line = {3, 0, 3};
        pg.setLine(line);
        int point[] = {5, 1, 7};
        pg.setPoint(point);
        pg.plot();
    }


    // VARIMAX ROTATION
    // Set varimax tolerance
    public void setVarimaxTolerance(double tolerance){
        this.varimaxTolerance = tolerance;
    }

    // Set varimax maximum number of iterations
    public void setVarimaxMaximumIterations(int max){
        this.nVarimaxMax = max;
    }

    // Get varimax number of iterations
    public int getVarimaxIterations(){
        return this.nVarimax;
    }


    // Varimax rotation: option set by default
    public void varimaxRotation(int nFactors){
       if(!this.pcaDone)this.pca();
        if(this.varimaxOption){
            this.normalVarimaxRotation(nFactors);
        }
        else{
            this.rawVarimaxRotation(nFactors);
        }
     }

     // Varimax rotation: option set by default
     // only raw option possible
     public void varimaxRotation(double[][] loadingFactorMatrix){
        if(this.varimaxOption)System.out.println("Method varimaxRotation: communality weights not supplied - raw varimax option used");
        this.rawVarimaxRotationInHouse(loadingFactorMatrix);
     }

     // Varimax rotation: option set by default
     public void varimaxRotation(double[][] loadingFactorMatrix,  double[] communalityWeights){
        if(this.varimaxOption){
            this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
        }
        else{
            System.out.println("Method varimaxRotation: raw varimax option chosen, supplied communality weights ignored");
            this.rawVarimaxRotationInHouse(loadingFactorMatrix);
        }
     }


    // Raw varimax rotation
    public void rawVarimaxRotation(int nFactors){
        if(!this.pcaDone)this.pca();
        double[][] loadingFactorMatrix = new double[nFactors][this.nItems];
        for(int i = 0; i<nFactors; i++)loadingFactorMatrix[i] = this.loadingFactorsAsRows[i];
        double[] communalityWeights = new double[this.nItems];
        for(int i = 0; i<this.nItems; i++)communalityWeights[i] = 1.0;
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    // Raw varimax rotation
    private void rawVarimaxRotationInHouse(double[][] loadingFactorMatrix){
        double[] communalityWeights = new double[this.nItems];
        for(int i = 0; i<this.nItems; i++)communalityWeights[i] = 1.0;
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    // Normal varimax rotation
    public void normalVarimaxRotation(int nFactors){
        if(!this.pcaDone)this.pca();
        double[][] loadingFactorMatrix = new double[nFactors][this.nItems];
        for(int i = 0; i<nFactors; i++)loadingFactorMatrix[i] = this.loadingFactorsAsRows[i];
        double[] communalityWeights = new double[this.nItems];
        for(int i = 0; i<nItems; i++){
            communalityWeights[i] = 0.0;
            for(int j = 0; j<nFactors; j++)communalityWeights[i] +=  loadingFactorMatrix[j][i]*loadingFactorMatrix[j][i];
        }
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    // Normal varimax rotation  - also used by raw varimax rotation with weights set to unity
    private void normalVarimaxRotationInHouse(double[][] loadingFactorMatrix, double[] communalityWeights){
        if(!this.pcaDone)this.pca();
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        this.usRotatedLoadingFactorsAsRows = new double[nRows][nColumns];
        this.rotatedLoadingFactorsAsRows = new double[nRows][nColumns];
        this.usRotatedEigenValues = new double[nRows];
        this.rotatedEigenValues = new double[nRows];
        this.rotatedProportionPercentage= new double[nRows];
        this.rotatedCumulativePercentage= new double[nRows];

        // Calculate weights and normalize the loading factors
        for(int j = 0; j<nColumns; j++)communalityWeights[j] = Math.sqrt(communalityWeights[j]);
        for(int i = 0; i<nRows; i++){
            for(int j = 0; j<nColumns; j++){
                if(loadingFactorMatrix[i][j]==0.0 && communalityWeights[j]==0){
                    loadingFactorMatrix[i][j] = 1.0;
                }
                else{
                    loadingFactorMatrix[i][j] /= communalityWeights[j];
                }
                this.usRotatedLoadingFactorsAsRows[i][j] = loadingFactorMatrix[i][j];
            }
        }

        // Loop through pairwise rotations until varimax function maximised
        double va = PCA.varimaxCriterion(this.usRotatedLoadingFactorsAsRows);
        double vaLast = 0;
        double angle = 0;
        boolean test = true;
        this.nVarimax = 0;
        while(test){
             for(int i=0; i<nRows-1; i++){
                for(int j=i+1; j<nRows; j++){
                    angle = PCA.varimaxAngle(this.usRotatedLoadingFactorsAsRows, i, j);
                    this.usRotatedLoadingFactorsAsRows = PCA.singleRotation(this.usRotatedLoadingFactorsAsRows, i, j, angle);
                    va = PCA.varimaxCriterion(this.usRotatedLoadingFactorsAsRows);
                }
            }
            if(Math.abs(va - vaLast)<this.varimaxTolerance){
                test=false;
            }
            else{
                vaLast = va;
                this.nVarimax++;
                if(this.nVarimax>nVarimaxMax){
                    test=false;
                    System.out.println("Method varimaxRotation: maximum iterations " + nVarimaxMax + " exceeded");
                    System.out.println("Tolerance = " + this.varimaxTolerance + ",     Comparison value = " + Math.abs(va - vaLast));
                    System.out.println("Current values returned");
                    if(super.sameCheck>0){
                        System.out.println("Presence of identical element row/s and/or column/s in the data probably impeding convergence");
                        System.out.println("Returned values are likely to be correct");
                    }
                }
            }
        }

        // undo normalization of rotated loading factors
        this.usRotatedLoadingFactorsAsColumns = new double[nColumns][nRows];
        for(int i=0; i<nRows; i++){
            for(int j=0; j<nColumns; j++){
                this.usRotatedLoadingFactorsAsRows[i][j] *= communalityWeights[j];
                this.usRotatedLoadingFactorsAsColumns[j][i] = this.usRotatedLoadingFactorsAsRows[i][j];
                loadingFactorMatrix[i][j] *= communalityWeights[j];
            }
        }

        // Rotated eigenvalues
       double usRotatedEigenValueTotal = 0.0;
       double unRotatedEigenValueTotal = 0.0;
       for(int i=0; i<nRows; i++){
            this.usRotatedEigenValues[i] = 0.0;
            for(int j=0; j<nColumns; j++){
                this.usRotatedEigenValues[i] += this.usRotatedLoadingFactorsAsRows[i][j]*this.usRotatedLoadingFactorsAsRows[i][j];
            }
            usRotatedEigenValueTotal += this.usRotatedEigenValues[i];
            unRotatedEigenValueTotal += this.orderedEigenValues[i];
        }

        // Order unscaled rotated eigenvalues
        ArrayMaths amrot = new ArrayMaths(this.usRotatedEigenValues);
        amrot = amrot.sort();
        this.usRotatedEigenValues = amrot.array();
        int[] sortedRotIndices = amrot.originalIndices();

        // reverse order
        int nh = nRows/2;
        double holdD = 0.0;
        int holdI = 0;
        for(int i=0; i<nh; i++){
            holdD = this.usRotatedEigenValues[i];
            this.usRotatedEigenValues[i] = this.usRotatedEigenValues[nRows - 1 - i];
            this.usRotatedEigenValues[nRows - 1 - i] = holdD;
            holdI = sortedRotIndices[i];
            sortedRotIndices[i] = sortedRotIndices[nRows - 1 - i];
            sortedRotIndices[nRows - 1 - i] = holdI;
        }

        // order rotated power factors to match ordered rotated eigenvalues
        int nn = this.usRotatedLoadingFactorsAsRows.length;
        int mm = this.usRotatedLoadingFactorsAsRows[0].length;
        double[][] holdDA = new double[nn][mm];
        for(int i=0; i<nn; i++){
            for(int j=0; j<mm; j++){
                holdDA[i][j] = this.usRotatedLoadingFactorsAsRows[sortedRotIndices[i]][j];
            }
        }
        this.usRotatedLoadingFactorsAsRows = Conv.copy((double[][])holdDA);

        nn = sortedRotIndices.length;
        this.rotatedIndices = new int[nn];
        int[]holdIA = new int[nn];
        for(int i=0; i<nn; i++){
            holdIA[i] = this.eigenValueIndices[sortedRotIndices[i]];
        }
        this.rotatedIndices = Conv.copy((int[])this.eigenValueIndices);
        for(int i=0; i<nn; i++){
            this.rotatedIndices[i] = holdIA[i];
        }

        // Scale rotated loading factors and eigenvalues to the unrotated variance percentage for the sum of the extracted eigenvalues
        double scale0 = Math.abs(unRotatedEigenValueTotal/usRotatedEigenValueTotal);
        double scale1 = Math.sqrt(scale0);
        for(int i=0; i<nRows; i++){
            this.rotatedEigenValues[i] = scale0*this.usRotatedEigenValues[i];
            this.rotatedProportionPercentage[i] = this.rotatedEigenValues[i]*100.0/this.eigenValueTotal;
            for(int j=0; j<nColumns; j++){
                this.rotatedLoadingFactorsAsRows[i][j] = scale1*this.usRotatedLoadingFactorsAsRows[i][j];
            }
        }
        this.rotatedCumulativePercentage[0] = this.rotatedProportionPercentage[0];
        for(int i=1; i<nRows; i++)this.rotatedCumulativePercentage[i] = this.rotatedCumulativePercentage[i-1] + this.rotatedProportionPercentage[i];

        this.rotationDone = true;

    }

    // Raw varimax rotation
    // Static method  - default tolerance and maximum iterations
    public static double[][] rawVarimaxRotation(double[][] loadingFactorMatrix){
        double tolerance = 0.0001;
        int nIterMax = 1000;
        return PCA.rawVarimaxRotation(loadingFactorMatrix, tolerance, nIterMax);
    }

    // Raw varimax rotation
    // Static method  - user supplied tolerance and maximum iterations
    public static double[][] rawVarimaxRotation(double[][] loadingFactorMatrix, double tolerance, int nIterMax){
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double[] communalityWeights = new double[nColumns];
        for(int i = 0; i<nColumns; i++){
            communalityWeights[i] = 1.0;
        }
        return PCA.normalVarimaxRotation(loadingFactorMatrix, communalityWeights, tolerance, nIterMax);
    }

    // Normal varimax rotation  - also used by raw varimax rotation with weights set to unity
    // Static method  - default tolerance and maximum iterations
    public static double[][] normalVarimaxRotation(double[][] loadingFactorMatrix, double[] communalityWeights){
        double tolerance = 0.0001;
        int nIterMax = 1000;
        return normalVarimaxRotation(loadingFactorMatrix, communalityWeights, tolerance, nIterMax);
    }

    // Normal varimax rotation  - also used by raw varimax rotation with weights set to unity
    // Static method  - tolerance and maximum iterations provided by the user
    public static double[][] normalVarimaxRotation(double[][] loadingFactorMatrix, double[] communalityWeights, double tolerance, int nIterMax){
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        for(int i=1; i<nRows; i++)if(loadingFactorMatrix[i].length!=nColumns)throw new IllegalArgumentException("All rows must be the same length");
        double[][] rotatedLoadingFactorsAsRows = new double[nRows][nColumns];

        // Calculate weights and normalize the loading factors
        for(int i = 0; i<nRows; i++){
            for(int j = 0; j<nColumns; j++){
                loadingFactorMatrix[i][j] /= communalityWeights[j];
                rotatedLoadingFactorsAsRows[i][j] = loadingFactorMatrix[i][j];
            }
        }

        // Loop through pairwise rotations until varimax function maximised
        double va = PCA.varimaxCriterion(rotatedLoadingFactorsAsRows);
        double vaLast = 0;
        double angle = 0;
        boolean test = true;
        int nIter = 0;
        while(test){
             for(int i=0; i<nRows-1; i++){
                for(int j=i+1; j<nRows; j++){
                    angle = PCA.varimaxAngle(rotatedLoadingFactorsAsRows, i, j);
                    rotatedLoadingFactorsAsRows = PCA.singleRotation(rotatedLoadingFactorsAsRows, i, j, angle);
                    va = PCA.varimaxCriterion(rotatedLoadingFactorsAsRows);
                }
            }
            if(Math.abs(va - vaLast)<tolerance){
                test=false;
            }
            else{
                vaLast = va;
                nIter++;
                if(nIter>nIterMax){
                    test=false;
                    System.out.println("Method varimaxRotation: maximum iterations " + nIterMax + " exceeded");
                    System.out.println("Current values returned");
                }
            }
        }

        // undo normalization of loading factors
        for(int i=0; i<nRows; i++){
            for(int j=0; j<nColumns; j++){
                rotatedLoadingFactorsAsRows[i][j] *= communalityWeights[j];
                loadingFactorMatrix[i][j] *= communalityWeights[j];
            }
        }

        return rotatedLoadingFactorsAsRows;
    }

    // Transpose a matrix (as a possible aide to the use of the static methods)
    public static double[][] transposeMatrix(double[][] matrix){
        int nRows = matrix.length;
        int nColumns = matrix[0].length;
        for(int i=1; i<nRows; i++)if(matrix[i].length!=nColumns)throw new IllegalArgumentException("All rows must be the same length");
        double[][] transpose = new double[nColumns][nRows];
        for(int i=0; i<nRows; i++){
            for(int j=0; j<nColumns; j++){
                transpose[j][i] = matrix[i][j];
            }
        }
        return transpose;
    }

    // Varimax criterion calculation
    public static double varimaxCriterion(double[][] loadingFactorMatrix){
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double va1 = 0.0;
        double va2 = 0.0;
        double va3 = 0.0;
        for(int j=0; j<nRows; j++){
            double sum1 = 0.0;
            for(int k=0; k<nColumns; k++){
                sum1 += Math.pow(loadingFactorMatrix[j][k], 4);
            }
            va1 += sum1;
        }
        //Db.show("STOP");
        va1 *= nColumns;
        for(int j=0; j<nRows; j++){
            double sum2 = 0.0;
            for(int k=0; k<nColumns; k++)sum2 += Math.pow(loadingFactorMatrix[j][k], 2);
            va2 += sum2*sum2;
        }
        va3 = va1 - va2;

        return va3;
    }

    // Varimax rotation angle calculation
    // Kaiset maximization procedure
    public static double varimaxAngle(double[][] loadingFactorMatrix, int k, int l){
            int nColumns = loadingFactorMatrix[0].length;
            double uTerm = 0.0;
            double vTerm = 0.0;
            double bigA = 0.0;
            double bigB = 0.0;
            double bigC = 0.0;
            double bigD = 0.0;

            for(int j=0; j<nColumns; j++){
                double lmjk = loadingFactorMatrix[k][j];
                double lmjl = loadingFactorMatrix[l][j];
                uTerm = lmjk*lmjk - lmjl*lmjl;
                vTerm = 2.0*lmjk*lmjl;
                bigA += uTerm;
                bigB += vTerm;
                bigC += uTerm*uTerm - vTerm*vTerm;
                bigD += 2.0*uTerm*vTerm;
            }
            double bigE = bigD - 2.0*bigA*bigB/nColumns;
            double bigF = bigC - (bigA*bigA - bigB*bigB)/nColumns;
            double angle = 0.25*Math.atan2(bigE, bigF);
            return angle;
    }

    // Single rotation
    public static double[][] singleRotation(double[][] loadingFactorMatrix, int k, int l, double angle){
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double[][] rotatedMatrix = new double[nRows][nColumns];
        for(int i=0; i<nRows; i++){
            for(int j=0; j<nColumns; j++){
                rotatedMatrix[i][j] = loadingFactorMatrix[i][j];
            }
        }

        double sinphi = Math.sin(angle);
        double cosphi = Math.cos(angle);
        for(int j=0; j<nColumns; j++){
            rotatedMatrix[k][j] = loadingFactorMatrix[k][j]*cosphi + loadingFactorMatrix[l][j]*sinphi;
            rotatedMatrix[l][j] = -loadingFactorMatrix[k][j]*sinphi + loadingFactorMatrix[l][j]*cosphi;
        }
        return rotatedMatrix;
    }


    // RETURN DATA

    // Return eigenvalues as calculated
    public double[] eigenValues(){
        if(!this.pcaDone)this.pca();
        return this.eigenValues;
    }

    // Return eigenvalues ordered into a descending order
    public double[] orderedEigenValues(){
        if(!this.pcaDone)this.pca();
        return this.orderedEigenValues;
    }

    // Return indices of the eigenvalues before ordering into a descending order
    public int[] eigenValueIndices(){
        if(!this.pcaDone)this.pca();
        return this.eigenValueIndices;
    }

    // Return sum of the eigenvalues
    public double eigenValueTotal(){
        if(!this.pcaDone)this.pca();
        return this.eigenValueTotal;
    }


    // Return eigenvalues ordered into a descending order and expressed as a percentage of total
    public double[] proportionPercentage(){
        if(!this.pcaDone)this.pca();
        return this.proportionPercentage;
    }

    // Return cumulative values of the eigenvalues ordered into a descending order and expressed as a percentage of total
    public double[] cumulativePercentage(){
        if(!this.pcaDone)this.pca();
        return this.cumulativePercentage;
    }

    // Return scaled rotated eigenvalues
    public double[] rotatedEigenValues(){
        if(!this.rotationDone)throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedEigenValues;
    }

    // Return scaled rotated eigenvalues as proportion of total variance
    public double[] rotatedProportionPercentage(){
        if(!this.rotationDone)throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedProportionPercentage;
    }

    // Return scaled rotated eigenvalues as cumulative percentages
    public double[] rotatedCumulativePercentage(){
        if(!this.rotationDone)throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedCumulativePercentage;
    }



    // Return eigenvectors as calculated
    // Each column is the eigenvector for an eigenvalue
    public double[][] eigenVectors(){
        if(!this.pcaDone)this.pca();
        return this.eigenVectorsAsColumns;
    }

    // Return eigenvectors as calculated
    // Each row is the eigenvector for an eigenvalue
    public double[][] eigenVectorsAsRows(){
        if(!this.pcaDone)this.pca();
        return this.eigenVectorsAsRows;
    }

    // Return eigenvector ordered to match the eigenvalues sorted into a descending order
    // Each column is the eigenvector for an eigenvalue
    public double[][] orderedEigenVectorsAsColumns(){
        if(!this.pcaDone)this.pca();
        return this.orderedEigenVectorsAsColumns;
    }

    // Return eigenvector ordered to match the eigenvalues sorted into a descending order
    // Each column is the eigenvector for an eigenvalue
    public double[][] orderedEigenVectors(){
        if(!this.pcaDone)this.pca();
        return this.orderedEigenVectorsAsColumns;
    }

    // Return eigenvector ordered to match the eigenvalues sorted into a descending order
    // Each rowis the eigenvector for an eigenvalue
    public double[][] orderedEigenVectorsAsRows(){
        if(!this.pcaDone)this.pca();
        return this.orderedEigenVectorsAsRows;
    }

    // Return loading factors ordered to match the eigenvalues sorted into a descending order
    // Each column is the loading factors for an eigenvalue
    public double[][] loadingFactorsAsColumns(){
        if(!this.pcaDone)this.pca();
        return this.loadingFactorsAsColumns;
    }

    // Return loading factors ordered to match the eigenvalues sorted into a descending order
    // Each row is the loading factors for an eigenvalue
    public double[][] loadingFactorsAsRows(){
        if(!this.pcaDone)this.pca();
        return this.loadingFactorsAsRows;
    }

    // Return rotated loading factors as columns
    public double[][] rotatedLoadingFactorsAsColumns(){
        if(!this.rotationDone)throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedLoadingFactorsAsColumns;
    }

    // Return rotated loading factors as rows
    public double[][] rotatedLoadingFactorsAsRows(){
        if(!this.rotationDone)throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedLoadingFactorsAsRows;
    }

    // Return communalities
    public double[] communalities(){
        if(!this.pcaDone)this.pca();
        return this.communalities;
    }

    // Return communality weights
    public double[] communalityWeights(){
        if(!this.pcaDone)this.pca();
        return this.communalityWeights;
    }

    // Return covariance matrix
    public Matrix covarianceMatrix(){
        if(!this.pcaDone)this.pca();
        return this.covarianceMatrix;
    }

    // Return correlation matrix
    public Matrix correlationMatrix(){
        if(!this.pcaDone)this.pca();
        return this.correlationMatrix;
    }

    // Return partial correlation matrix
    public Matrix partialCorrelationMatrix(){
        if(!this.pcaDone)this.pca();
        return this.partialCorrelationMatrix;
    }

    // Return Monte Carlo means
    public double[] monteCarloMeans(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.randomEigenValuesMeans;
    }

    // Return Monte Carlo standard deviations
    public double[] monteCarloStandardDeviations(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.randomEigenValuesSDs;
    }

    // Return Monte Carlo percentiles
    public double[] monteCarloPercentiles(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.randomEigenValuesPercentiles;
    }

    // Return Monte Carlo eigenvalue matrix
    public double[][] monteCarloEigenValues(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.randomEigenValues;
    }

    // Return original data matrix
    public Matrix originalData(){
        if(!this.pcaDone)this.pca();
        return this.data;
    }

    // Return data minus row means divided by n-1 or n
    public Matrix xMatrix(){
        if(!this.pcaDone)this.pca();
        double denom = this.nItems;
        if(!super.nFactorOption)denom -= 1.0;
        Matrix mat = dataMinusMeans.times(1.0/Math.sqrt(denom));
        return mat;
    }

    // Return transpose of data minus row means divided by n-1 or n
    public Matrix xMatrixTranspose(){
        if(!this.pcaDone)this.pca();
        double denom = this.nItems;
        if(!super.nFactorOption)denom -= 1.0;
        Matrix mat = dataMinusMeansTranspose.times(1.0/Math.sqrt(denom));
        return mat;
    }

    // Return number of extracted components with eigenvalues greater than or equal to one
    public int nEigenOneOrGreater(){
        if(!this.pcaDone)this.pca();
        return this.greaterThanOneLimit;
    }

    // Return number of extracted components with eigenvalues greater than the corresponding Monte Carlo mean
    public int nMeanCrossover(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.meanCrossover;
    }

    // Return number of extracted components with eigenvalues greater than the corresponding Monte Carlo percentile
    public int nPercentileCrossover(){
        if(!this.monteCarloDone)this.monteCarlo();
        return this.percentileCrossover;
    }

    // Return overall Kaiser-Meyer-Olkin (KMO) statistic
    public double overallKMO(){
        if(!this.pcaDone)this.pca();
        return this.kmo;
    }

    public double kmo(){
        if(!this.pcaDone)this.pca();
        return this.kmo;
    }

    // Return all individual item Kaiser-Meyer-Olkin (KMO) statistics
    public double[] itemKMOs(){
        if(!this.pcaDone)this.pca();
        return this.itemKMOs;
    }

    // Return Bartlett Sphericity Test Chi Square
    public double chiSquareBartlett(){
        if(!this.pcaDone)this.pca();
        return this.chiSquareBartlett;
    }

    // Return Bartlett Sphericity Test degrees of freedom
    public int dofBartlett(){
        if(!this.pcaDone)this.pca();
        return this.dfBartlett;
    }

    // Return Bartlett Sphericity Test degrees of freedom
    public double probabilityBartlett(){
        if(!this.pcaDone)this.pca();
        return this.probBartlett;
    }


    // OUTPUT THE ANALYSIS

    // Full analysis without output of input data
    // no input file name entered via method argument list
    public void analysis(){


        this.outputFilename = "PCAOutput";
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

        // Scree Plot
        this.screePlot();

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

        System.out.println("The analysis has been written to the file " + this.outputFilename);
    }

    // Output analysis to a text (.txt) file
    private void analysisText(){

        FileOutput fout = null;
        if(this.fileNumberingSet){
            fout = new FileOutput(this.outputFilename, 'n');
        }
        else{
            fout = new FileOutput(this.outputFilename);
        }

        // perform PCA if not already performed
        if(!pcaDone)this.pca();
        if(!this.monteCarloDone)this.monteCarlo();

        // output title
        fout.println("PRINCIPAL COMPONENT ANALYSIS");
        fout.println("Program: PCA - Analysis Output");
        for(int i=0; i<this.titleLines; i++)fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();
        if(this.covRhoOption){
            fout.println("Covariance matrix used");
        }
        else{
            fout.println("Correlation matrix used");
        }
        fout.println();

        // output eigenvalue table
        // field width
        int field1 = 10;
        int field2 = 12;
        int field3 = 2;

        fout.println("ALL EIGENVALUES");

        fout.print("Component ", field1);
        fout.print("Unordered ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.println("Difference ");

        fout.print(" ", field1);
        fout.print("index", field1);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.println(" ");



        for(int i=0; i<this.nItems; i++){
            fout.print(i+1, field1);
            fout.print((this.eigenValueIndices[i]+1), field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.proportionPercentage[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.cumulativePercentage[i], this.trunc), field2);
            if(i<this.nItems-1){
                fout.print(Fmath.truncate((this.orderedEigenValues[i] - this.orderedEigenValues[i+1]), this.trunc), field2);
            }
            else{
                fout.print(" ", field2);
            }
            fout.print(" ", field3);

            fout.println();
        }
        fout.println();


        // Extracted components
        int nMax = this.greaterThanOneLimit;
        if(nMax<this.meanCrossover)nMax=this.meanCrossover;
        if(nMax<this.percentileCrossover)nMax=this.percentileCrossover;
        fout.println("EXTRACTED EIGENVALUES");
        fout.print(" ", field1);
        fout.print("Greater than unity", 3*field2 + field3);
        fout.print("Greater than Monte Carlo Mean ", 3*field2 + field3);
        fout.println("Greater than Monte Carlo Percentile");

        fout.print("Component ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);

        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);

        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.println(" ");

        fout.print(" ", field1);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);

        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);

        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.println(" ");

        int ii=0;
        while(ii<nMax){
            fout.print(ii+1, field1);

            if(ii<this.greaterThanOneLimit){
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc), (field2+field3));
            }

            if(ii<this.meanCrossover){
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc), (field2+field3));
            }

            if(ii<this.percentileCrossover){
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
            }
            fout.println();
            ii++;
        }
        fout.println();


        fout.println("PARALLEL ANALYSIS");
        fout.println("Number of simulations = " + this.nMonteCarlo);
        if(this.gaussianDeviates){
            fout.println("Gaussian random deviates used");
        }
        else{
            fout.println("Uniform random deviates used");
        }
        fout.println("Percentile value used = " + this.percentile + " %");

        fout.println();
        fout.print("Component ", field1);
        fout.print("Data ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);
        fout.print("Data ", field2);
        fout.print("Monte Carlo ", field2);
        fout.print("Monte Carlo ", field2);
        fout.println("Monte Carlo ");

        fout.print(" ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);
        fout.print("Eigenvalue ", field2);
        fout.print("Eigenvalue ", field2);
        fout.print("Eigenvalue ", field2);
        fout.println("Eigenvalue ");

        fout.print(" ", field1);
        fout.print(" ", field2);
        fout.print(" ", field2);
        fout.print(" ", field2);
        fout.print(" ", field3);
        fout.print(" ", field2);
        fout.print("Percentile ", field2);
        fout.print("Mean ", field2);
        fout.println("Standard Deviation ");

        for(int i=0; i<this.nItems; i++){
            fout.print(i+1, field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.proportionPercentage[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.cumulativePercentage[i], this.trunc), field2);
            fout.print(" ", field3);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.randomEigenValuesPercentiles[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.randomEigenValuesMeans[i], this.trunc), field2);
            fout.println(Fmath.truncate(this.randomEigenValuesSDs[i], this.trunc));
        }
        fout.println();

        // Correlation Matrix
        fout.println("CORRELATION MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("item", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.correlationMatrix.getElement(j,i), this.trunc), field2);
            fout.println();
        }
        fout.println();

        // Partial Correlation Matrix
        fout.println("PARTIAL CORRELATION MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("item", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.partialCorrelationMatrix.getElement(j,i), this.trunc), field2);
            fout.println();
        }
        fout.println();

        // Covariance Matrix
        fout.println("COVARIANCE MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("item", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.covarianceMatrix.getElement(j,i), this.trunc), field2);
            fout.println();
        }

        fout.println();

        // Eigenvectors
        fout.println("EIGENVECTORS");
        fout.println("Original component indices in parenthesis");
        fout.println("Vector corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.println();
        fout.println("component");
        for(int i=0; i<this.nItems; i++){
            fout.print((i+1) + " (" + (this.eigenValueIndices[i]+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.orderedEigenVectorsAsRows[i][j], this.trunc), field2);
            fout.println();
        }
        fout.println();

        // Loading factors
        fout.println("LOADING FACTORS");
        fout.println("Original  indices in parenthesis");
        fout.println("Loading factors corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.print(" ", field1);
        fout.print("Eigenvalue", field2);
        fout.print("Proportion", field2);
        fout.println("Cumulative %");
        fout.println("factor");
        for(int i=0; i<this.nItems; i++){
            fout.print((i+1) + " (" + (this.eigenValueIndices[i]+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.loadingFactorsAsRows[i][j], this.trunc), field2);
            fout.print(" ", field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(proportionPercentage[i], this.trunc), field2);
            fout.println(Fmath.truncate(cumulativePercentage[i], this.trunc));
        }
        fout.println();

        // Rotated loading factors
        fout.println("ROTATED LOADING FACTORS");
        if(this.varimaxOption){
            fout.println("NORMAL VARIMAX");
        }
        else{
            fout.println("RAW VARIMAX");
        }

        String message = "The ordered eigenvalues with Monte Carlo means and percentiles in parenthesis";
        message += "\n (Total number of eigenvalues = " + this.nItems + ")";
        int nDisplay = this.nItems;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int nDisplayLimit = 20*screenHeight/800;
        if(nDisplay>nDisplay)nDisplay = nDisplayLimit;
        for(int i=0; i<nDisplay; i++){
            message += "\n " + Fmath.truncate(this.orderedEigenValues[i], 4) + " (" + Fmath.truncate(this.randomEigenValuesMeans[i], 4) + "  " + Fmath.truncate(this.randomEigenValuesPercentiles[i], 4) + ")";
        }
        if(nDisplay<this.nItems)message += "\n . . . ";
        message += "\nEnter number of eigenvalues to be extracted";
        int nExtracted = this.greaterThanOneLimit;
        nExtracted = Db.readInt(message, nExtracted);
        this.varimaxRotation(nExtracted);

        fout.println("Varimax rotation for " + nExtracted + " extracted factors");
        fout.println("Rotated loading factors and eigenvalues scaled to ensure total 'rotated variance' matches unrotated variance for the extracted factors");
        fout.println("Original  indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for(int i=0; i<this.nItems; i++)fout.print((this.rotatedIndices[i]+1) + " (" + (i+1) + ")", field2);
        fout.print(" ", field1);
        fout.print("Eigenvalue", field2);
        fout.print("Proportion", field2);
        fout.println("Cumulative %");
        fout.println("factor");

        for(int i=0; i<nExtracted; i++){
            fout.print((i+1) + " (" + (rotatedIndices[i]+1) + ")", 2*field1);
            for(int j=0; j<this.nItems; j++)fout.print(Fmath.truncate(this.rotatedLoadingFactorsAsRows[i][j], this.trunc), field2);
            fout.print(" ", field1);
            fout.print(Fmath.truncate(rotatedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(rotatedProportionPercentage[i], this.trunc), field2);
            fout.println(Fmath.truncate(rotatedCumulativePercentage[i], this.trunc));
        }
        fout.println();

        fout.println("Kaiser-Meyer-Olkin (KMO) statistic");
        fout.println("   Overall KMO statistic =   " + Fmath.truncate(this.kmo, this.trunc));
        fout.println("   KMO values for each item");
        fout.print("   ");
        for(int i=0; i<nItems; i++)fout.print(i+1, field2);
        fout.println();
        fout.print("   ");
        for(int i=0; i<nItems; i++)fout.print(Fmath.truncate(this.itemKMOs[i], this.trunc), field2);
        fout.println();

        fout.println();
        fout.println("Bartlett Sphericity Test");
        fout.println("   Chi-Square =         " + Fmath.truncate(this.chiSquareBartlett, this.trunc));
        fout.println("   Probability value =  " + Fmath.truncate(this.probBartlett, this.trunc));
        fout.println("   Degrees of freedom = " + this.dfBartlett);
        fout.println("   Chi-Square value at the 5% significance level  (p = 0.05) = " + Fmath.truncate(this.sign05Bartlett, this.trunc));
        fout.println("   Chi-Square value at the 10% significance level (p = 0.10) = " + Fmath.truncate(this.sign10Bartlett, this.trunc));
        fout.println();

        fout.println("DATA USED");
        fout.println("Number of items = " + this.nItems);
        fout.println("Number of persons = " + this.nPersons);


        if(this.originalDataType==0){
            fout.printtab("Item");
            for(int i=0; i<this.nPersons; i++){
                fout.printtab(i+1);
            }
            fout.println();
            for(int i=0; i<this.nItems; i++){
                fout.printtab(this.itemNames[i]);
                for(int j=0; j<this.nPersons; j++){
                    fout.printtab(Fmath.truncate(this.scores0[i][j], this.trunc));
                }
                fout.println();
            }
        }
        else{
            fout.printtab("Person");
            for(int i=0; i<this.nItems; i++){
                fout.printtab(this.itemNames[i]);
            }
            fout.println();
            for(int i=0; i<this.nPersons; i++){
                fout.printtab(i+1);
                for(int j=0; j<this.nItems; j++){
                    fout.printtab(Fmath.truncate(this.scores1[i][j], this.trunc));
                }
                fout.println();
            }
        }

        fout.close();
    }

    // Output to an Excel readable file
    private void analysisExcel(){

        FileOutput fout = null;
        if(this.fileNumberingSet){
            fout = new FileOutput(this.outputFilename, 'n');
        }
        else{
            fout = new FileOutput(this.outputFilename);
        }

        // perform PCA if not already performed
        if(!pcaDone)this.pca();
        if(!this.monteCarloDone)this.monteCarlo();

        // output title
        fout.println("PRINCIPAL COMPONENT ANALYSIS");
        fout.println("Program: PCA - Analysis Output");
        for(int i=0; i<this.titleLines; i++)fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();
        if(this.covRhoOption){
            fout.println("Covariance matrix used");
        }
        else{
            fout.println("Correlation matrix used");
        }
        fout.println();

        // output eigenvalue table
        fout.println("ALL EIGENVALUES");

        fout.printtab("Component ");
        fout.printtab("Unordered ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.println("Difference ");

        fout.printtab(" ");
        fout.printtab("index");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.println(" ");



        for(int i=0; i<this.nItems; i++){
            fout.printtab(i+1);
            fout.printtab((this.eigenValueIndices[i]+1));
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.proportionPercentage[i], this.trunc));
            fout.printtab(Fmath.truncate(this.cumulativePercentage[i], this.trunc));
            if(i<this.nItems-1){
                fout.printtab(Fmath.truncate((this.orderedEigenValues[i] - this.orderedEigenValues[i+1]), this.trunc));
            }
            else{
                fout.printtab(" ");
            }
            fout.printtab(" ");

            fout.println();
        }
        fout.println();


        // Extracted components
        int nMax = this.greaterThanOneLimit;
        if(nMax<this.meanCrossover)nMax=this.meanCrossover;
        if(nMax<this.percentileCrossover)nMax=this.percentileCrossover;
        fout.println("EXTRACTED EIGENVALUES");
        fout.printtab(" ");
        fout.printtab("Greater than unity");
        fout.printtab(" ");fout.printtab(" ");fout.printtab(" ");
        fout.printtab("Greater than Monte Carlo Mean ");
        fout.printtab(" ");fout.printtab(" ");fout.printtab(" ");
        fout.println("Greater than Monte Carlo Percentile");

        fout.printtab("Component ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");

        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");

        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.println(" ");

        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");

        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");

        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.println(" ");

        int ii=0;
        while(ii<nMax){
            fout.printtab(ii+1);

            if(ii<this.greaterThanOneLimit){
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
                fout.printtab(" ");
            }

            if(ii<this.meanCrossover){
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
                fout.printtab(" ");
            }

            if(ii<this.percentileCrossover){
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
            }
            fout.println();
            ii++;
        }
        fout.println();


        fout.println("PARALLEL ANALYSIS");
        fout.println("Number of simulations = " + this.nMonteCarlo);
        if(this.gaussianDeviates){
            fout.println("Gaussian random deviates used");
        }
        else{
            fout.println("Uniform random deviates used");
        }
        fout.println("Percentile value used = " + this.percentile + " %");

        fout.println();
        fout.printtab("Component ");
        fout.printtab("Data ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");
        fout.printtab("Data ");
        fout.printtab("Monte Carlo ");
        fout.printtab("Monte Carlo ");
        fout.println("Monte Carlo ");

        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Eigenvalue ");
        fout.println("Eigenvalue ");

        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("Percentile ");
        fout.printtab("Mean ");
        fout.println("Standard Deviation ");

        for(int i=0; i<this.nItems; i++){
            fout.printtab(i+1);
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.proportionPercentage[i], this.trunc));
            fout.printtab(Fmath.truncate(this.cumulativePercentage[i], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.randomEigenValuesPercentiles[i], this.trunc));
            fout.printtab(Fmath.truncate(this.randomEigenValuesMeans[i], this.trunc));
            fout.println(Fmath.truncate(this.randomEigenValuesSDs[i], this.trunc));
        }
        fout.println();

        // Correlation Matrix
        fout.println("CORRELATION MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("item");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.correlationMatrix.getElement(j,i), this.trunc));
            fout.println();
        }
        fout.println();

        // Partial Correlation Matrix
        fout.println("PARTIAL CORRELATION MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("item");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.partialCorrelationMatrix.getElement(j,i), this.trunc));
            fout.println();
        }
        fout.println();

        // Covariance Matrix
        fout.println("COVARIANCE MATRIX");
        fout.println("Original item indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("item");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
        fout.println();
        fout.println("item");
        for(int i=0; i<this.nItems; i++){
            fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.covarianceMatrix.getElement(j,i), this.trunc));
            fout.println();
        }
        fout.println();

        // Eigenvectors
        fout.println("EIGENVECTORS");
        fout.println("Original component indices in parenthesis");
        fout.println("Vector corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
        fout.println();
        fout.println("component");

        for(int i=0; i<this.nItems; i++){
            fout.printtab((i+1) + " (" + (this.eigenValueIndices[i]+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.orderedEigenVectorsAsRows[i][j], this.trunc));
            fout.println();
        }
        fout.println();

        // Loading factors
        fout.println("LOADING FACTORS");
        fout.println("Original  indices in parenthesis");
        fout.println("Loading factors corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.eigenValueIndices[i]+1) + " (" + (i+1) + ")");
        fout.printtab(" ");
        fout.printtab("Eigenvalue");
        fout.printtab("% Proportion");
        fout.println("Cumulative %");
        fout.println("factor");
        for(int i=0; i<this.nItems; i++){
            fout.printtab((i+1) + " (" + (this.eigenValueIndices[i]+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.loadingFactorsAsRows[i][j], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(proportionPercentage[i], this.trunc));
            fout.println(Fmath.truncate(cumulativePercentage[i], this.trunc));
        }
        fout.println();

        // Rotated loading factors
        fout.println("ROTATED LOADING FACTORS");
        if(this.varimaxOption){
            fout.println("NORMAL VARIMAX");
        }
        else{
            fout.println("RAW VARIMAX");
        }

        String message = "The ordered eigenvalues with Monte Carlo means and percentiles in parenthesis";
        message += "\n (Total number of eigenvalues = " + this.nItems + ")";
        int nDisplay = this.nItems;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int nDisplayLimit = 20*screenHeight/800;
        if(nDisplay>nDisplay)nDisplay = nDisplayLimit;
        for(int i=0; i<nDisplay; i++){
            message += "\n " + Fmath.truncate(this.orderedEigenValues[i], 4) + " (" + Fmath.truncate(this.randomEigenValuesMeans[i], 4) + "  " + Fmath.truncate(this.randomEigenValuesPercentiles[i], 4) + ")";
        }
        if(nDisplay<this.nItems)message += "\n . . . ";
        message += "\nEnter number of eigenvalues to be extracted";
        int nExtracted = this.greaterThanOneLimit;
        nExtracted = Db.readInt(message, nExtracted);
        this.varimaxRotation(nExtracted);

        fout.println("Varimax rotation for " + nExtracted + " extracted factors");
        fout.println("Rotated loading factors and eigenvalues scaled to ensure total 'rotated variance' matches unrotated variance for the extracted factors");
        fout.println("Original  indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for(int i=0; i<this.nItems; i++)fout.printtab((this.rotatedIndices[i]+1) + " (" + (i+1) + ")");
        fout.printtab(" ");
        fout.printtab("Eigenvalue");
        fout.printtab("% Proportion");
        fout.println("Cumulative %");
        fout.println("factor");
        for(int i=0; i<nExtracted; i++){
            fout.printtab((i+1) + " (" + (this.rotatedIndices[i]+1) + ")");
            fout.printtab(" ");
            for(int j=0; j<this.nItems; j++)fout.printtab(Fmath.truncate(this.rotatedLoadingFactorsAsRows[i][j], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(rotatedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(rotatedProportionPercentage[i], this.trunc));
            fout.println(Fmath.truncate(rotatedCumulativePercentage[i], this.trunc));
        }
        fout.println();

        fout.println("Kaiser-Meyer-Olkin (KMO) statistic");
        fout.println("   Overall KMO statistic =   " + Fmath.truncate(this.kmo, this.trunc));
        fout.println("   KMO values for each item");
        fout.printtab("   ");
        for(int i=0; i<nItems; i++)fout.printtab(i+1);
        fout.println();
        fout.printtab("   ");
        for(int i=0; i<nItems; i++)fout.printtab(Fmath.truncate(this.itemKMOs[i], this.trunc));
        fout.println();

        fout.println();
        fout.println("Bartlett Sphericity Test");
        fout.println("   Chi-Square =         " + Fmath.truncate(this.chiSquareBartlett, this.trunc));
        fout.println("   Probability value =  " + Fmath.truncate(this.probBartlett, this.trunc));
        fout.println("   Degrees of freedom = " + this.dfBartlett);
        fout.println("   Chi-Square value at the 5% significance level  (p = 0.05) = " + Fmath.truncate(this.sign05Bartlett, this.trunc));
        fout.println("   Chi-Square value at the 10% significance level (p = 0.10) = " + Fmath.truncate(this.sign10Bartlett, this.trunc));
        fout.println();

        fout.println("DATA USED");
        fout.println("Number of items = " + this.nItems);
        fout.println("Number of persons = " + this.nPersons);

        if(this.originalDataType==0){
            fout.printtab("Item");
            for(int i=0; i<this.nPersons; i++){
                fout.printtab(i+1);
            }
            fout.println();
            for(int i=0; i<this.nItems; i++){
                fout.printtab(this.itemNames[i]);
                for(int j=0; j<this.nPersons; j++){
                    fout.printtab(Fmath.truncate(this.scores0[i][j], this.trunc));
                }
                fout.println();
            }
        }
        else{
            fout.printtab("Person");
            for(int i=0; i<this.nItems; i++){
                fout.printtab(this.itemNames[i]);
            }
            fout.println();
            for(int i=0; i<this.nPersons; i++){
                fout.printtab(i+1);
                for(int j=0; j<this.nItems; j++){
                    fout.printtab(Fmath.truncate(this.scores1[i][j], this.trunc));
                }
                fout.println();
            }
        }

        fout.close();
    }
}

