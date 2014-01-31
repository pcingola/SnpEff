/*
*   PlanarWaveguide Class
*
*   Methods for:
*       determining the refractive index of a waveguiding thin film
*       in an asymmetric slab waveguide from the effective refractive index, i.e.
*       the normalised propagation vector, and the core layer thickness
*
*       determinimg the refractive index of the superstrate [waveguide coupler sensor]
*
*       obtaining the normalised propagation vector versus guiding layer thickness
*       dispersion curve for an asymmetric slab waveguide
*
*
*   This is the superclass for the subclasses PrismCoupler and GratingCoupler
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: March 2006
*   Revised: 29 April 2006, 5-7 July 2008, 9 November 2009
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PlanarWaveguide.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2006 - 2009   Michael Thomas Flanagan
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

 package flanagan.optics;

import flanagan.math.*;
import flanagan.analysis.Stat;
import flanagan.roots.*;
import flanagan.plot.*;
import flanagan.optics.RefractiveIndex;

import java.util.ArrayList;

public class PlanarWaveguide{

    // CLASS VARIABLES

    protected double[][] measurementsTE = null;     // experimental TE mode measurements
                                                    // [film thickness][effective refractive index][weight][mode number]
    protected int numberOfTEmeasurements = 0;       // number of TE mode experimental measurements
    protected double[] thicknessesUsedTE = null;    // TE mode thicknesses used in calculation
    protected double[] calcEffectRefrIndicesTE = null;    // TE mode calculated effective refractive indices for mean core refractive index
    protected boolean setMeasurementsTE = false;    // = true when TE mode measurements entered
    protected boolean setErrorsTE = false;          // = true when TE mode errors entered
    protected double maximumTEmodeEffectiveRefractiveIndex = 0.0D;    // Maximum TE mode effective refractive index value
    protected double minimumTEmodeEffectiveRefractiveIndex = 0.0D;    // Minimum TE mode effective refractive index value
    protected double[][] measurementsTM = null;     // experimental TM mode measurements
                                                    // [film thickness][effective refractive index][weight][mode number]
    protected int numberOfTMmeasurements = 0;       // number of TM mode experimental measurements
    protected double[] thicknessesUsedTM = null;    // TM mode thicknesses used in calculation
    protected double[] calcEffectRefrIndicesTM = null;    // TM mode calculated effective refractive indices for mean core refractive index
    protected boolean setMeasurementsTM = false;    // = true when TM mode measurements entered
    protected boolean setErrorsTM = false;          // = true when TM mode errors entered
    protected double maximumTMmodeEffectiveRefractiveIndex = 0.0D;    // Maximum TM mode effective refractive index value
    protected double minimumTMmodeEffectiveRefractiveIndex = 0.0D;    // Minimum TM mode effective refractive index value
    protected double maximumEffectiveRefractiveIndex = 0.0D;    // Maximum overall effective refractive index value
    protected double minimumEffectiveRefractiveIndex = 0.0D;    // Minimum overall effective refractive index value
    protected int numberOfMeasurements = 0;         // total number of experimental measurements
    protected boolean setMeasurements = false;      // = true when measurements entered
    protected boolean setWeights = false;           // = true when weights entered
    protected boolean[] eliminatedTE = null;        // = true when TE point eliminated if effective refractive index lies below physical limit (Max[sub or superstrate]
    protected boolean[] eliminatedTM = null;        // = true when TM point eliminated if effective refractive index lies below physical limit (Max[sub or superstrate]
    protected double wavelength = 0;                    // wavelength of the exciting light
    protected boolean setWavelength= false;             // = true when wavelength entered
    protected double ko = 0.0D;                         // wave vector, 2pi/lambda

    protected double superstrateRefractiveIndex = 0.0D; // superstrate refractive index
                                                        // default value: air
    protected double superstrateRefractiveIndex2 = 0.0D;// superstrate refractive index squared
    protected double[] calcSuperstrateTEmodeRI = null;  // calculated  TE mode superstrate refractive index values
    protected double[] calcSuperstrateTMmodeRI = null;  // calculated  TM mode superstrate refractive index values
    protected double meanTEmodeSuperstrateRefractiveIndex = Double.NaN; // mean of the TE mode superstrate refractive indices
    protected double meanTMmodeSuperstrateRefractiveIndex = Double.NaN; // mean of the TM mode superstrate refractive indices

    protected double sdTEmodeSuperstrateRefractiveIndex = Double.NaN;   // standard deviation of the TE mode superstrate refractive indices
    protected double sdTMmodeSuperstrateRefractiveIndex = Double.NaN;   // standard deviation of the TM mode superstrate refractive indices
    protected double sdSuperstrateRefractiveIndex = Double.NaN;      // standard deviation of the superstrate refractive indices
    protected boolean setSuperstrate = false;           // = true when superstrate refractive index entered
    protected boolean superCalculationDone = false;     // = true when superstrate refractive index has been calculated

    protected double substrateRefractiveIndex = 0.0D;   // substrate refractive index
    protected double substrateRefractiveIndex2 = 0.0D;  // substrate refractive index squared
    protected boolean setSubstrate = false;             // = true when substrate refractive index entered
    protected double coreFilmRefractiveIndex = 0.0D;    // guiding layer thin film refractive index
    protected double coreFilmRefractiveIndex2 = 0.0D;   // guiding layer thin film refractive index squared
    protected boolean setCore = false;                  // = true when guiding layer refractive index entered or calculated
    protected double[] coreFilmTEmodeRefractiveIndices = null;          // core film TE mode refractive indices
    protected double[] coreFilmTMmodeRefractiveIndices = null;          // core film TM mode refractive indices
    protected double meanTEmodeCoreFilmRefractiveIndex = Double.NaN;    // mean of the TE mode core film refractive indices
    protected double meanTMmodeCoreFilmRefractiveIndex = Double.NaN;    // mean of the TM mode core film refractive indices
    protected double meanCoreFilmRefractiveIndex = Double.NaN;          // mean of the TE and TM mode core film refractive indices
    protected double meanCoreFilmRefractiveIndex2 = Double.NaN;         // square of the mean of the TE and TM mode core film refractive indices
    protected double sdTEmodeCoreFilmRefractiveIndex = Double.NaN;      // standard deviation of the TE mode core film refractive indices
    protected double sdTMmodeCoreFilmRefractiveIndex = Double.NaN;      // standard deviation of the TM mode core film refractive indices
    protected double sdCoreFilmRefractiveIndex = Double.NaN;            // standard deviation of the TE and TM mode core film refractive indices
    protected double lowerBound = 0.0D;     // lower bound in a root search
    protected double upperBound = 0.0D;     // upper bound in a root search
    protected double tolerance = 1e-9;      // root search tolerance
    protected boolean calculationDone = false;  // = true when calculation of core film refractive index/indices completed

    protected double prismToWaveguideGap = Double.POSITIVE_INFINITY;    // prism to waveguide gap (subclass PrismCoupler)
    protected boolean setPrismToWaveguideGap = false;   // = true when prism to waveguide gap set (subclass PrismCoupler)
    protected boolean fixedPrismToWaveguideGap = true;  // = true when prism to waveguide gap set at a fixed value (subclass PrismCoupler)
                                                        // = false when prism to waveguide gap is to be estimated (subclass PrismCoupler)
    protected double prismRefractiveIndex = 0.0D;   // substrate refractive index
    protected double prismRefractiveIndex2 = 0.0D;  // substrate refractive index squared


    // CONSTRUCTOR
    public PlanarWaveguide(){
    }

    // THICKNESS (metres), EFFECTIVE REFRACTIVE INDEX AND MODE NUMBER DATA
    // Enter TE mode data for a single measurement with no weights
    public void enterTEmodeData(double thickness, double effectiveRI, double modeNumber){
        if(setMeasurementsTE){
            if(setErrorsTE)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurements + 1;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTE[i][j];
            }
            hold[this.numberOfTEmeasurements][0] = thickness;
            hold[this.numberOfTEmeasurements][1] = effectiveRI;
            hold[this.numberOfTEmeasurements][2] = 1.0D;
            hold[this.numberOfTEmeasurements][3] = modeNumber;
            this.measurementsTE = hold;
            this.numberOfTEmeasurements = nNew;
        }
        else{
            this.measurementsTE = new double[1][4];
            this.measurementsTE[0][0] = thickness;
            this.measurementsTE[0][1] = effectiveRI;
            this.measurementsTE[0][2] = 1.0D;
            this.measurementsTE[0][3] = modeNumber;
            this.numberOfTEmeasurements = 1;
        }
        this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTE = true;
        this.setMeasurements = true;
    }

    // Enter TE mode data for a single measurement with weights
    public void enterTEmodeData(double thickness, double effectiveRI, double weight, double modeNumber){
        if(setMeasurementsTE){
            if(!setErrorsTE)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurements + 1;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTE[i][j];
            }
            hold[this.numberOfTEmeasurements][0] = thickness;
            hold[this.numberOfTEmeasurements][1] = effectiveRI;
            hold[this.numberOfTEmeasurements][2] = weight;
            hold[this.numberOfTEmeasurements][3] = modeNumber;
            this.measurementsTE = hold;
            this.numberOfTEmeasurements = nNew;
        }
        else{
            this.measurementsTE = new double[1][4];
            this.measurementsTE[0][0] = thickness;
            this.measurementsTE[0][1] = effectiveRI;
            this.measurementsTE[0][2] = weight;
            this.measurementsTE[0][3] = modeNumber;
            this.numberOfTEmeasurements = 1;
        }
        this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTE = true;
        this.setMeasurements = true;
        this.setErrorsTE = true;
    }

    // Enter TE mode data for a range of measurements with no weights
    public void enterTEmodeData(double[]thicknesses, double[] effectiveRIs, double[] modeNumbers){
        int o = thicknesses.length;
        int n = effectiveRIs.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of effective refractive indices, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(setMeasurementsTE){
            if(setErrorsTE)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurements + o;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTE[i][j];
            }
            for(int i=0; i<o; i++){
                hold[this.numberOfTEmeasurements + i][0] = thicknesses[i];
                hold[this.numberOfTEmeasurements + i][1] = effectiveRIs[i];
                hold[this.numberOfTEmeasurements + i][2] = 1.0D;
                hold[this.numberOfTEmeasurements + i][3] = modeNumbers[i];
            }
            this.measurementsTE = hold;
            this.numberOfTEmeasurements = nNew;
        }
        else{
            this.numberOfTEmeasurements = o;
            this.measurementsTE = new double[this.numberOfTEmeasurements][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                this.measurementsTE[i][0] = thicknesses[i];
                this.measurementsTE[i][1] = effectiveRIs[i];
                this.measurementsTE[i][2] = 1.0D;
                this.measurementsTE[i][3] = modeNumbers[i];
            }

        }
        this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTE = true;
        this.setMeasurements = true;
    }

    // Enter TE mode data for a range of measurements with weights
    public void enterTEmodeData(double[]thicknesses, double[] effectiveRIs, double[] weights, double[] modeNumbers){
        int o = thicknesses.length;
        int n = effectiveRIs.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of effective refractive indices, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(setMeasurementsTE){
            if(!setErrorsTE)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurements + o;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTE[i][j];
            }
            for(int i=0; i<o; i++){
                hold[this.numberOfTEmeasurements + i][0] = thicknesses[i];
                hold[this.numberOfTEmeasurements + i][1] = effectiveRIs[i];
                hold[this.numberOfTEmeasurements + i][2] = weights[i];
                hold[this.numberOfTEmeasurements + i][3] = modeNumbers[i];
            }
            this.measurementsTE = hold;
            this.numberOfTEmeasurements = nNew;
        }
        else{
            this.numberOfTEmeasurements = o;
            this.measurementsTE = new double[this.numberOfTEmeasurements][4];
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                this.measurementsTE[i][0] = thicknesses[i];
                this.measurementsTE[i][1] = effectiveRIs[i];
                this.measurementsTE[i][2] = weights[i];
                this.measurementsTE[i][3] = modeNumbers[i];
            }
        }
        this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTE = true;
        this.setMeasurements = true;
        this.setErrorsTE = true;
    }



    // Enter TM mode data for a single measurement with no weights
    public void enterTMmodeData(double thickness, double effectiveRI, double modeNumber){
        if(setMeasurementsTM){
            if(setErrorsTM)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurements + 1;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTM[i][j];
            }
            hold[this.numberOfTMmeasurements][0] = thickness;
            hold[this.numberOfTMmeasurements][1] = effectiveRI;
            hold[this.numberOfTMmeasurements][2] = 1.0D;
            hold[this.numberOfTMmeasurements][3] = modeNumber;
            this.measurementsTM = hold;
            this.numberOfTMmeasurements = nNew;
        }
        else{
            this.measurementsTM = new double[1][4];
            this.measurementsTM[0][0] = thickness;
            this.measurementsTM[0][1] = effectiveRI;
            this.measurementsTM[0][2] = 1.0D;
            this.measurementsTM[0][3] = modeNumber;
            this.numberOfTMmeasurements = 1;
        }
        this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTM = true;
        this.setMeasurements = true;
    }

    // Enter TM mode data for a single measurement with weights
    public void enterTMmodeData(double thickness, double effectiveRI, double weight, double modeNumber){
        if(setMeasurementsTM){
            if(!setErrorsTM)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurements + 1;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTM[i][j];
            }
            hold[this.numberOfTMmeasurements][0] = thickness;
            hold[this.numberOfTMmeasurements][1] = effectiveRI;
            hold[this.numberOfTMmeasurements][2] = weight;
            hold[this.numberOfTMmeasurements][3] = modeNumber;
            this.measurementsTM = hold;
            this.numberOfTMmeasurements = nNew;
        }
        else{
            this.measurementsTM = new double[1][4];
            this.measurementsTM[0][0] = thickness;
            this.measurementsTM[0][1] = effectiveRI;
            this.measurementsTM[0][2] = weight;
            this.measurementsTM[0][3] = modeNumber;
            this.numberOfTMmeasurements = 1;
        }
        this.numberOfMeasurements = this.numberOfTMmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTM = true;
        this.setMeasurements = true;
        this.setErrorsTM = true;
    }

    // Enter TM mode data for a range of measurements without weights
    public void enterTMmodeData(double[]thicknesses, double[] effectiveRIs, double[] modeNumbers){
        int o = thicknesses.length;
        int n = effectiveRIs.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of effective refractive indices, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(setMeasurementsTM){
            if(setErrorsTM)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurements + o;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTM[i][j];
            }
            for(int i=0; i<o; i++){
                hold[this.numberOfTMmeasurements + i][0] = thicknesses[i];
                hold[this.numberOfTMmeasurements + i][1] = effectiveRIs[i];
                hold[this.numberOfTMmeasurements + i][2] = 1.0D;
                hold[this.numberOfTMmeasurements + i][3] = modeNumbers[i];
            }
            this.measurementsTM = hold;
            this.numberOfTMmeasurements = nNew;
        }
        else{
            this.numberOfTMmeasurements = o;
            this.measurementsTM = new double[this.numberOfTMmeasurements][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                this.measurementsTM[i][0] = thicknesses[i];
                this.measurementsTM[i][1] = effectiveRIs[i];
                this.measurementsTM[i][2] = 1.0D;
                this.measurementsTM[i][3] = modeNumbers[i];
            }
        }
        this.numberOfMeasurements = this.numberOfTMmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTM = true;
        this.setMeasurements = true;
    }

    // Enter TM mode data for a range of measurements with weights
    public void enterTMmodeData(double[]thicknesses, double[] effectiveRIs, double[] weights, double[] modeNumbers){
        int o = thicknesses.length;
        int n = effectiveRIs.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of effective refractive indices, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(setMeasurementsTM){
            if(!setErrorsTM)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurements + o;
            double[][] hold = new double[nNew][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                for(int j=0; j<4; j++)hold[i][j] = this.measurementsTM[i][j];
            }
            for(int i=0; i<o; i++){
                hold[this.numberOfTMmeasurements + i][0] = thicknesses[i];
                hold[this.numberOfTMmeasurements + i][1] = effectiveRIs[i];
                hold[this.numberOfTMmeasurements + i][2] = weights[i];
                hold[this.numberOfTMmeasurements + i][3] = modeNumbers[i];
            }
            this.measurementsTM = hold;
            this.numberOfTMmeasurements = nNew;
        }
        else{
            this.numberOfTMmeasurements = o;
            this.measurementsTM = new double[this.numberOfTMmeasurements][4];
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                this.measurementsTM[i][0] = thicknesses[i];
                this.measurementsTM[i][1] = effectiveRIs[i];
                this.measurementsTM[i][2] = weights[i];
                this.measurementsTM[i][3] = modeNumbers[i];
            }
        }
        this.numberOfMeasurements = this.numberOfTMmeasurements + this.numberOfTMmeasurements;
        this.setMeasurementsTM = true;
        this.setMeasurements = true;
        this.setErrorsTM = true;
    }

    // Clear entered thickness, effective refractive index and mode number data
    //  so new dat may be entered without it being appended to the existing data
    public void clearData(){
        this.numberOfMeasurements = 0;
        this.setMeasurements = false;
        this.setWeights = false;

        this.numberOfTEmeasurements = 0;
        this.setMeasurementsTE = false;
        this.setErrorsTE = false;

        this.numberOfTMmeasurements = 0;
        this.setMeasurementsTM = false;
        this.setErrorsTM = false;
    }

    // WAVELENGTH
    // Enter the wavelength (metres)
    public void setWavelength(double wavelength){
        this.wavelength = wavelength;
        this.setWavelength = true;
        this.ko = 2.0D*Math.PI/this.wavelength;
        if(!this.setSuperstrate)this.superstrateRefractiveIndex = RefractiveIndex.air(this.wavelength);
    }

    // SUBSTRATE REFRACTIVE INDEX
    // Enter the substrate refractive index
    public void setSubstrateRefractiveIndex(double refIndex){
        this.substrateRefractiveIndex = refIndex;
        this.substrateRefractiveIndex2 = refIndex*refIndex;
        this.setSubstrate= true;
    }

    // SUPERSTRATE REFRACTIVE INDEX
    // Enter the superstrate refractive index
    public void setSuperstrateRefractiveIndex(double refIndex){
        this.superstrateRefractiveIndex = refIndex;
        this.superstrateRefractiveIndex2 = refIndex*refIndex;
        this.setSuperstrate = true;
    }

    // RETURN SUPERSTRATE REFRACTIVE INDEX
    // Return superstrate refractive index
    public double getSuperstrateRefractiveIndex(){
        if(!this.superCalculationDone && this.setCore)this.calcSuperstrateRefractiveIndex();
        return this.superstrateRefractiveIndex;
    }

    // Return standard deviation of the superstrate refractive index
    public double getStandardDeviationSuperstrateRefractiveIndex(){
        if(!this.superCalculationDone && this.setCore)this.calcSuperstrateRefractiveIndex();
        if(this.setCore){
            if((this.numberOfTMmeasurements+this.numberOfTEmeasurements)==1)System.out.println("Method: getStandardDeviationSuperstrateRefractiveIndex - Only one measurement entered - NO standard deviation returned");
        }
        else{
            System.out.println("Method: getStandardDeviationSuperstrateRefractiveIndex - Superstrate refractive index was entered and NOT calculated - NO standard deviation returned");
        }
        return this.sdCoreFilmRefractiveIndex;
    }

    // CORE FILM REFRACTIVE INDEX
    // Enter the core film refractive index
    public void setCoreLayerRefractiveIndex(double refIndex){
        this.coreFilmRefractiveIndex = refIndex;
        this.coreFilmRefractiveIndex2 = refIndex*refIndex;
        this.setCore = true;
    }

    // RETURN CORE FILM REFRACTIVE INDICES
    // Return TE mode core film refractive indices
    public double[] getTEmodeCoreFilmRefractiveIndices(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTEmeasurements==0)System.out.println("Method: getTEmodeCoreFilmRefractiveIndices - NO TE mode data entered - NO refractive indices returned");
        return this.coreFilmTEmodeRefractiveIndices;
    }

    // Return TM mode core film refractive indices
    public double[] getTMmodeCoreFilmRefractiveIndices(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTMmeasurements==0)System.out.println("Method: getTMmodeCoreFilmRefractiveIndices - NO TM mode data entered - NO refractive indices returned");
        return this.coreFilmTMmodeRefractiveIndices;
    }

    // Return TE mode average core film refractive index
    public double getMeanTEmodeCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTEmeasurements==0)System.out.println("Method: getMeanTEmodeCoreFilmRefractiveIndices - NO TE mode data entered - NO refractive index returned");
        return this.meanTEmodeCoreFilmRefractiveIndex;
    }

    // Return TM mode average core film refractive index
    public double getMeanTMmodeCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTMmeasurements==0)System.out.println("Method: getMeanTMmodeCoreFilmRefractiveIndices - NO TM mode data entered - NO refractive index returned");
        return this.meanTMmodeCoreFilmRefractiveIndex;
    }

    // Return overall average core film refractive index
    public double getMeanCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        return this.meanCoreFilmRefractiveIndex;
    }

    // Return  core film refractive index
    public double getCoreFilmRefractiveIndex(){
        if(!this.calculationDone && !this.setCore)this.calcCoreFilmRefractiveIndices();
        return this.coreFilmRefractiveIndex;
    }

    // Return standard deviation of the core film TE mode refractive index
    public double getStandardDeviationTEmodeCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTEmeasurements==0)System.out.println("Method: getStandardDeviationTEmodeCoreFilmRefractiveIndex - NO TE mode data entered - NO standard deviation returned");
        if(this.numberOfTEmeasurements==1)System.out.println("Method: getStandardDeviationTEmodeCoreFilmRefractiveIndex - Only one measurement entered - NO standard deviation returned");
        return this.sdTEmodeCoreFilmRefractiveIndex;
    }

    // Return standard deviation of the core film TM mode refractive index
    public double getStandardDeviationTMmodeCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTMmeasurements==0)System.out.println("Method: getStandardDeviationTMmodeCoreFilmRefractiveIndex - NO TM mode data entered - NO standard deviation returned");
        if(this.numberOfTMmeasurements==1)System.out.println("Method: getStandardDeviationTMmodeCoreFilmRefractiveIndex - Only one measurement entered - NO standard deviation returned");
        return this.sdTMmodeCoreFilmRefractiveIndex;
    }

    // Return standard deviation of the overall core film refractive index
    public double getStandardDeviationCoreFilmRefractiveIndex(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if((this.numberOfTMmeasurements+this.numberOfTEmeasurements)==1)System.out.println("Method: getStandardDeviationCoreFilmRefractiveIndex - Only one measurement entered - NO standard deviation returned");
        return this.sdCoreFilmRefractiveIndex;
    }

    // EFFECTIVE REFRACTIVE INDICES
    // Return experimental TE mode effective refractive indices
    public double[][] getTEmodeExperimentalEffectiveRefractiveIndices(){
        double[][] returnedArray = null;
        if(this.numberOfTEmeasurements==0){
            System.out.println("Method: getTEmodeExperimentalEffectiveRefractiveIndices - NO TE mode data entered - NO effective refractive indices returned");
        }
        else{
            returnedArray = new double[2][this.numberOfTEmeasurements];
            returnedArray[0] = this.thicknessesUsedTE;
            for(int i=0; i<this.numberOfTEmeasurements; i++){
                returnedArray[1][i] = this.measurementsTE[i][1];
            }
        }
        return returnedArray;
    }

    // Return errors in the experimental TE mode effective refractive indices
    public double[][] getTEmodeEffectiveRefractiveIndicesErrors(){
        double[][] returnedArray = null;
        if(this.numberOfTEmeasurements==0){
            System.out.println("Method: getTEmodeExperimentalEffectiveRefractiveIndices - NO TE mode data entered - NO errors returned");
        }
        else{
            if(!this.setErrorsTE){
                System.out.println("Method: getTEmodeExperimentalEffectiveRefractiveIndices - NO TE mode errors entered - NO errors returned");
            }
            else{
                returnedArray = new double[2][this.numberOfTEmeasurements];
                returnedArray[0] = this.thicknessesUsedTE;
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    returnedArray[1][i] = this.measurementsTE[i][2];
                }
            }
        }
        return returnedArray;
    }

    // Return experimental TM mode effective refractive indices
    public double[][] getTMmodeExperimentalEffectiveRefractiveIndices(){
        double[][] returnedArray = null;
        if(this.numberOfTMmeasurements==0){
            System.out.println("Method: getTMmodeExperimentalEffectiveRefractiveIndices - NO TM mode data entered - NO effective refractive indices returned");
        }
        else{
            returnedArray = new double[2][this.numberOfTMmeasurements];
            returnedArray[0] = this.thicknessesUsedTM;
            for(int i=0; i<this.numberOfTMmeasurements; i++){
                returnedArray[1][i] = this.measurementsTM[i][1];
            }
        }
        return returnedArray;
    }

    // Return errors in the experimental TM mode effective refractive indices
    public double[][] getTMmodeEffectiveRefractiveIndicesErrors(){
        double[][] returnedArray = null;
        if(this.numberOfTMmeasurements==0){
            System.out.println("Method: getTMmodeExperimentalEffectiveRefractiveIndices - NO TM mode data entered - NO errors returned");
        }
        else{
            if(!this.setErrorsTM){
                System.out.println("Method: getTMmodeExperimentalEffectiveRefractiveIndices - NO TM mode errors entered - NO errors returned");
            }
            else{
                returnedArray = new double[2][this.numberOfTMmeasurements];
                returnedArray[0] = this.thicknessesUsedTM;
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    returnedArray[1][i] = this.measurementsTM[i][2];
                }
            }
        }
        return returnedArray;
    }

    // Return calculated TE mode effective refractive indices for the calculated mean core refractive index
    public double[][] getTEmodeCalculatedEffectiveRefractiveIndices(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTEmeasurements==0)System.out.println("Method: getStandardDeviationTEmodeCoreFilmRefractiveIndices - NO TE mode data entered - NO effective refractive indices returned");
        double[][] returnedArray = new double[2][this.numberOfTEmeasurements];

        // Create instance of the class holding the TE mode effective refractive index function
        FunctTEplot func = new FunctTEplot();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;

        this.lowerBound = Math.max(this.substrateRefractiveIndex, this.superstrateRefractiveIndex);
        this.upperBound = Math.min(this.coreFilmRefractiveIndex, this.prismRefractiveIndex);

        for(int i=0; i<this.numberOfTEmeasurements; i++){

            // set further function parameter
            func.thickness = this.measurementsTE[i][0];
            func.modeNumber = this.measurementsTE[i][3];

            // call root searching method, bisection
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            this.calcEffectRefrIndicesTE[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }
        returnedArray[0] = this.thicknessesUsedTE;
        returnedArray[1] = this.calcEffectRefrIndicesTE;

        return returnedArray;
    }

    // Return calculated TM mode effective refractive indices for the calculated mean core refractive index
    public double[][] getTMmodeCalculatedEffectiveRefractiveIndices(){
        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();
        if(this.numberOfTMmeasurements==0)System.out.println("Method: getStandardDeviationTMmodeCoreFilmRefractiveIndices - NO TM mode data entered - NO effective refractive indices returned");
        double[][] returnedArray = new double[2][this.numberOfTMmeasurements];

        // Create instance of the class holding the TM mode effective refractive index function
        FunctTMplot func = new FunctTMplot();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;

        this.lowerBound = Math.max(this.substrateRefractiveIndex, this.superstrateRefractiveIndex);
        this.upperBound = Math.min(this.coreFilmRefractiveIndex, this.prismRefractiveIndex);

        for(int i=0; i<this.numberOfTMmeasurements; i++){

            // set further function parameter
            func.thickness = this.measurementsTM[i][0];
            func.modeNumber = this.measurementsTM[i][3];

            // call root searching method, bisection
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            this.calcEffectRefrIndicesTM[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }
        returnedArray[0] = this.thicknessesUsedTM;
        returnedArray[1] = this.calcEffectRefrIndicesTM;

        return returnedArray;
    }

    // CALCULATION OF THE GUIDING CORE FILM REFRACTIVE INDEX/INDICES
    public void calcCoreFilmRefractiveIndices(){
        if(!this.setMeasurements)throw new IllegalArgumentException("Either no thickness, angle/effective refractive index, mode number data has been entered or a key subclass variable, e.g. coupling prism corner angle has not been entered");
        if(!this.setWavelength)throw new IllegalArgumentException("No wavelength has been entered");
        if(!this.setSubstrate)throw new IllegalArgumentException("No substrate refractive index has been entered");

        // Set the bounds and eliminate points where effective ref. index < substrate or superstrate ref. index
        this.lowerBound = Math.max(this.substrateRefractiveIndex, this.superstrateRefractiveIndex);
        this.upperBound = 0.0D;

        if(this.numberOfTEmeasurements>0)this.eliminatedTE = new boolean[this.numberOfTEmeasurements];
        int elimNumberTE = 0;
        for(int i=0; i<this.numberOfTEmeasurements; i++){
            this.eliminatedTE[i] = false;
            if(this.measurementsTE[i][1]<this.lowerBound){
                System.out.println("TE mode measurement point, " + i + ", eliminated as the effective refractive index, " +  this.measurementsTE[i][1] + ", lies below the physical limit, " + this.lowerBound);
                this.eliminatedTE[i] = true;
                elimNumberTE++;
            }
            else{
                if(this.upperBound<this.measurementsTE[i][1])this.upperBound = this.measurementsTE[i][1];
            }
        }
        if(elimNumberTE>0){
            int newNumber = this.numberOfTEmeasurements - elimNumberTE;
            if(newNumber==0){
                this.numberOfTEmeasurements = 0;
            }
            else{
                double[][] temp = new double[newNumber][3];
                int nIndex = 0;
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    if(!this.eliminatedTE[i]){
                        temp[nIndex][0] = this.measurementsTE[i][0];
                        temp[nIndex][1] = this.measurementsTE[i][1];
                        temp[nIndex][2] = this.measurementsTE[i][2];
                        temp[nIndex][3] = this.measurementsTE[i][3];
                        nIndex++;
                    }
                }
                this.measurementsTE = temp;
                this.numberOfTEmeasurements = newNumber;
                this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
            }
        }
        this.thicknessesUsedTE = new double[this.numberOfTEmeasurements];
        this.calcEffectRefrIndicesTE = new double[this.numberOfTEmeasurements];
        for(int i=0; i<this.numberOfTEmeasurements; i++)this.thicknessesUsedTE[i] = this.measurementsTE[i][0];
        this.maximumTEmodeEffectiveRefractiveIndex = this.upperBound;

        this.upperBound = 0.0D;
        if(this.numberOfTMmeasurements>0)this.eliminatedTM = new boolean[this.numberOfTMmeasurements];
        int elimNumberTM = 0;
        for(int i=0; i<this.numberOfTMmeasurements; i++){
            this.eliminatedTM[i] = false;
            if(this.measurementsTM[i][1]<this.lowerBound){
                System.out.println("TM mode measurement point, " + i + ", eliminated as the effective refractive index, " +  this.measurementsTM[i][1] + ", lies below the physical limit, " + this.lowerBound);
                this.eliminatedTM[i] = true;
                elimNumberTM++;
            }
            else{
                if(this.upperBound<this.measurementsTM[i][1])this.upperBound = this.measurementsTM[i][1];
            }
        }
        if(elimNumberTM>0){
            int newNumber = this.numberOfTMmeasurements - elimNumberTM;
            if(newNumber==0){
                this.numberOfTMmeasurements = 0;
            }
            else{
                double[][] temp = new double[newNumber][3];
                int nIndex = 0;
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    if(!this.eliminatedTM[i]){
                        temp[nIndex][0] = this.measurementsTM[i][0];
                        temp[nIndex][1] = this.measurementsTM[i][1];
                        temp[nIndex][2] = this.measurementsTM[i][2];
                        temp[nIndex][3] = this.measurementsTM[i][3];
                        nIndex++;
                    }
                }
                this.measurementsTM = temp;
                this.numberOfTMmeasurements = newNumber;
                this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
            }
        }
        this.thicknessesUsedTM = new double[this.numberOfTMmeasurements];
        this.calcEffectRefrIndicesTM = new double[this.numberOfTMmeasurements];
        for(int i=0; i<this.numberOfTMmeasurements; i++)this.thicknessesUsedTM[i] = this.measurementsTM[i][0];
        this.maximumTMmodeEffectiveRefractiveIndex = this.upperBound;

        if(this.numberOfMeasurements==0)throw new IllegalArgumentException("All data points rejected as lying outside the physically meaningful bounds");

        if(this.fixedPrismToWaveguideGap){
            this.calcCoreFilmRefractiveIndicesFixedGap();
        }
        else{
            this.calcCoreFilmRefractiveIndicesEstimatedGap();
        }
    }

    // Calculates core refractive index and the prism to waveguide gap
    public void calcCoreFilmRefractiveIndicesEstimatedGap(){

        // ArrayList to store sum of squares on each gap distance decrement
        ArrayList<Double> arrayl = new ArrayList<Double>();

        // initial gap distance
        this.prismToWaveguideGap = 1.e1;

        // set calculation to fixed gap for each gap decrement calculation
        this.fixedPrismToWaveguideGap = true;

        // arrays to store experimental and, at each gap decrement, the calculated effective refractive indices
        double[] effectExpl = new double[this.numberOfMeasurements];
        double[] effectCalc = new double[this.numberOfMeasurements];

        // Collect experimental effective refractive indices
        for(int i=0; i<this.numberOfTEmeasurements; i++)effectExpl[i] = this.measurementsTE[i][1];
        for(int i=0; i<this.numberOfTMmeasurements; i++)effectExpl[i + this.numberOfTEmeasurements] = this.measurementsTM[i][1];

        // Sum of squares of experimental - calculated effective refractive indices at each gap decrement
        double sumOfSquares = 0.0D;

        // Sum of squares at preceding gap decrement
        double sumOfSquaresLast = Double.POSITIVE_INFINITY;

        // Number of decrements
        int numberOfDecrements = 0;

        // Decrementing loop in which sum of squares calculted
        boolean test = true;
        while(test){

            // Set flags to allow new calculation of core refractive index
            this.setCore = false;
            this.calculationDone = false;
            this.fixedPrismToWaveguideGap = true;
            this.setPrismToWaveguideGap=true;

            // Get mean core refractive index at current gap value
            double coreRI = this.getMeanCoreFilmRefractiveIndex();

            // Check whether physically meaningful root found
            if(coreRI!=coreRI){
                System.out.println("NaN");
                test = false;
            }
            else{
                // Calculate sum of squares
                double[][] effectTECalc = this.getTEmodeCalculatedEffectiveRefractiveIndices();
                for(int i=0; i<this.numberOfTEmeasurements; i++)effectCalc[i] = effectTECalc[1][i];
                double[][] effectTMCalc = this.getTMmodeCalculatedEffectiveRefractiveIndices();
                for(int i=0; i<this.numberOfTMmeasurements; i++)effectCalc[i + this.numberOfTEmeasurements] = effectTMCalc[1][i];
                sumOfSquares = 0.0D;
                for(int i=0; i<this.numberOfMeasurements; i++)sumOfSquares += Fmath.square(effectExpl[i] - effectCalc[i]);

                // store values
                System.out.println(this.prismToWaveguideGap + " " + coreRI + " " + sumOfSquares);
                arrayl.add(new Double(coreRI));
                arrayl.add(new Double(sumOfSquares));
                numberOfDecrements++;

                // Decrement gap distance and check for termination gap distance
                this.prismToWaveguideGap /= 2.0;
                if(this.prismToWaveguideGap<1.0e-10)test = false;
            }
        }
    }


    // Calculates core refractive index for either fixed prism to waveguide gap or on ignoring prism perturbation
    public void calcCoreFilmRefractiveIndicesFixedGap(){
        // call the root search methods to obtain the core refractive index/indices
        if(this.numberOfTEmeasurements>0)this.calcTEmodeCoreFilmRefractiveIndices();
        if(this.numberOfTMmeasurements>0)this.calcTMmodeCoreFilmRefractiveIndices();

        // Calculate the overall mean and standard deviation of the core refractive indices
        if(this.numberOfTEmeasurements>0 && this.numberOfTMmeasurements==0){
            this.meanCoreFilmRefractiveIndex = this.meanTEmodeCoreFilmRefractiveIndex;
            this.coreFilmRefractiveIndex = this.meanCoreFilmRefractiveIndex;
            this.sdCoreFilmRefractiveIndex = this.sdTEmodeCoreFilmRefractiveIndex;
        }
        else{
            if(this.numberOfTMmeasurements>0 && this.numberOfTEmeasurements==0){
                this.meanCoreFilmRefractiveIndex = this.meanTMmodeCoreFilmRefractiveIndex;
                this.coreFilmRefractiveIndex = this.meanCoreFilmRefractiveIndex;
                this.sdCoreFilmRefractiveIndex = this.sdTMmodeCoreFilmRefractiveIndex;
            }
            else{
                double[] values = new double[this.numberOfMeasurements];
                double[] weights = new double[this.numberOfMeasurements];
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    values[i] = this.coreFilmTEmodeRefractiveIndices[i];
                    weights[i] = this.measurementsTE[i][2];
                }
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    values[i+this.numberOfTEmeasurements] = this.coreFilmTMmodeRefractiveIndices[i];
                    weights[i+this.numberOfTEmeasurements] = this.measurementsTM[i][2];
                }
                this.meanCoreFilmRefractiveIndex = Stat.mean(values, weights);
                this.sdCoreFilmRefractiveIndex = Stat.standardDeviation(values, weights);
                this.coreFilmRefractiveIndex = this.meanCoreFilmRefractiveIndex;
            }
        }

        this.meanCoreFilmRefractiveIndex2 = this.meanCoreFilmRefractiveIndex*this.meanCoreFilmRefractiveIndex;
        this.coreFilmRefractiveIndex2 = this.meanCoreFilmRefractiveIndex2;
        this.maximumEffectiveRefractiveIndex = Math.max(this.maximumTEmodeEffectiveRefractiveIndex, this.maximumTMmodeEffectiveRefractiveIndex);
        this.setCore = true;
        this.calculationDone = true;
    }

    // Calculate TE mode refractive indices
    public void calcTEmodeCoreFilmRefractiveIndices(){

        this.coreFilmTEmodeRefractiveIndices = new double[this.numberOfTEmeasurements];

        // Create instance of the class holding the TE mode core film refractive indexfunction
        FunctTE func = new FunctTE();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;

        double[] weights = new double[this.numberOfTEmeasurements];
        this.lowerBound = this.maximumTEmodeEffectiveRefractiveIndex;
        this.upperBound = 2.0D*this.lowerBound;
        for(int i=0; i<this.numberOfTEmeasurements; i++){
            weights[i] = this.measurementsTE[i][2];

            // set further function parameters
            func.thickness = this.measurementsTE[i][0];
            func.effectiveRefractiveIndex2 = this.measurementsTE[i][1]*this.measurementsTE[i][1];
            func.modeNumber = this.measurementsTE[i][3];

            // call root searching method, bisection, to obtain core refractive index
            RealRoot rr = new RealRoot();
            rr.noLowerBoundExtension();
            rr.setTolerance(this.tolerance);
            this.coreFilmTEmodeRefractiveIndices[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }

        // Calculate mean and sd
        if(this.numberOfTEmeasurements>1){
            this.meanTEmodeCoreFilmRefractiveIndex = Stat.mean(this.coreFilmTEmodeRefractiveIndices, weights);
            this.sdTEmodeCoreFilmRefractiveIndex = Stat.standardDeviation(this.coreFilmTEmodeRefractiveIndices, weights);
        }
        else{
            this.meanTEmodeCoreFilmRefractiveIndex = this.coreFilmTEmodeRefractiveIndices[0];
        }
    }

     // Calculate TM mode refractive indices
    public void calcTMmodeCoreFilmRefractiveIndices(){

        this.coreFilmTMmodeRefractiveIndices = new double[this.numberOfTMmeasurements];

        // Create instance of the class holding the TE mode core film refractive index function
        FunctTM func = new FunctTM();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;

        double[] weights = new double[this.numberOfTMmeasurements];
        this.lowerBound = this.maximumTMmodeEffectiveRefractiveIndex;
        this.upperBound = 2.0D*this.lowerBound;
        for(int i=0; i<this.numberOfTMmeasurements; i++){
            weights[i] = this.measurementsTM[i][2];

            // set further function parameters
            func.thickness = this.measurementsTM[i][0];
            func.effectiveRefractiveIndex2 = this.measurementsTM[i][1]*this.measurementsTM[i][1];
            func.modeNumber = this.measurementsTM[i][3];

            // call root searching method, bisection, to obtain core refractive index
            RealRoot rr = new RealRoot();
            rr.noLowerBoundExtension();
            rr.setTolerance(this.tolerance);
            this.coreFilmTMmodeRefractiveIndices[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }

        // Calculate mean and sd
        if(this.numberOfTMmeasurements>1){
            this.meanTMmodeCoreFilmRefractiveIndex = Stat.mean(this.coreFilmTMmodeRefractiveIndices, weights);
            this.sdTMmodeCoreFilmRefractiveIndex = Stat.standardDeviation(this.coreFilmTMmodeRefractiveIndices, weights);
        }
        else{
            this.meanTMmodeCoreFilmRefractiveIndex = this.coreFilmTMmodeRefractiveIndices[0];
        }
    }

    // Calculate a TE mode dispersion curve
    public double[][] dispersionCurveTE(double lowThickness, double highThickness, int numberOfPoints, double modeNumber){
        if(!this.setWavelength)throw new IllegalArgumentException("No wavelength has been entered");
        if(!this.setSubstrate)throw new IllegalArgumentException("No substrate refractive index has been entered");
        if(!this.setCore)throw new IllegalArgumentException("No core film refractive index has been calculated or entered");

        // Create arrays
        double[] thickness = new double[numberOfPoints];
        double[] effective = new double[numberOfPoints];
        double[][] returnedArray = new double[2][numberOfPoints];
        double incr = (Fmath.log10(highThickness) - Fmath.log10(lowThickness))/(numberOfPoints - 1);
        thickness[0] = Fmath.log10(lowThickness);
        thickness[numberOfPoints-1] = Fmath.log10(highThickness);
        for(int i=1; i<numberOfPoints-1; i++)thickness[i] = thickness[i-1] + incr;
        returnedArray[0] = thickness;

        // Create instance of the class holding the TE mode effective refractive index function
        FunctTEplot func = new FunctTEplot();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;
        func.modeNumber = modeNumber;

        this.lowerBound = Math.max(this.substrateRefractiveIndex, this.superstrateRefractiveIndex);
        this.upperBound = Math.min(this.coreFilmRefractiveIndex, this.prismRefractiveIndex);

        for(int i=0; i<numberOfPoints; i++){
            // set further function parameter
            func.thickness = Math.pow(10.0D, thickness[i]);

            // call root searching method, bisection
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            effective[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }
        returnedArray[1] = effective;
        return returnedArray;
    }

    // Calculate a TM mode dispersion curve
    public double[][] dispersionCurveTM(double lowThickness, double highThickness, int numberOfPoints, double modeNumber){
        if(!this.setWavelength)throw new IllegalArgumentException("No wavelength has been entered");
        if(!this.setSubstrate)throw new IllegalArgumentException("No substrate refractive index has been entered");
        if(!this.setCore)throw new IllegalArgumentException("No core film refractive index has been calculated or entered");

        // Create arrays
        double[] thickness = new double[numberOfPoints];
        double[] effective = new double[numberOfPoints];
        double[][] returnedArray = new double[2][numberOfPoints];
        double incr = (Fmath.log10(highThickness) - Fmath.log10(lowThickness))/(numberOfPoints - 1);
        thickness[0] = Fmath.log10(lowThickness);
        thickness[numberOfPoints-1] = Fmath.log10(highThickness);
        for(int i=1; i<numberOfPoints-1; i++)thickness[i] = thickness[i-1] + incr;
        returnedArray[0] = thickness;

        // Create instance of the class holding the TM mode effective refractive index function
        FunctTMplot func = new FunctTMplot();

        // Set function parameters
        func.substrateRefractiveIndex2 = this.substrateRefractiveIndex2;
        func.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex2;
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.prismRefractiveIndex2 = this.prismRefractiveIndex2;
        func.prismToWaveguideGap = this.prismToWaveguideGap;
        func.setPrismToWaveguideGap = this.setPrismToWaveguideGap;
        func.ko = this.ko;
        func.modeNumber = modeNumber;

        this.lowerBound = Math.max(this.substrateRefractiveIndex, this.superstrateRefractiveIndex);
        this.upperBound = Math.min(this.coreFilmRefractiveIndex, this.prismRefractiveIndex);
        for(int i=0; i<numberOfPoints; i++){
            // set further function parameter
            func.thickness = Math.pow(10.0D, thickness[i]);

            // call root searching method, bisection
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            effective[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }
        returnedArray[1] = effective;
        return returnedArray;
    }

    // Calculate and plot a TE dispersion curve
    // Graph title not provided
    public double[][] plotDispersionCurveTE(double lowThickness, double highThickness, int numberOfPoints, double modeNumber){
        String legend1 = " ";
        return this.plotDispersionCurveTE(lowThickness, highThickness, numberOfPoints, modeNumber, legend1);
    }

    // Calculate and plot a TE dispersion curve
    // Graph title provided
    public double[][] plotDispersionCurveTE(double lowThickness, double highThickness, int numberOfPoints, double modeNumber, String legend1){

        //Calculate curve
        double[][] curve = dispersionCurveTE(lowThickness, highThickness, numberOfPoints, modeNumber);

        // Create instance of PlotGraph
        PlotGraph pg1 = new PlotGraph(curve);
        int lineOption = 3;
        if(numberOfPoints<100)lineOption = 1;
        pg1.setLine(lineOption);
        pg1.setPoint(0);
        String legend0 = "Dispersion curve: TE mode  -  mode number " + (int)modeNumber;
        pg1.setGraphTitle(legend0);
        pg1.setGraphTitle2(legend1);
        pg1.setXaxisLegend("Log10( Core Film Thickness / metres )");
        pg1.setYaxisLegend("Effective Refractive Index (kz/ko)");

        // Plot graph
        pg1.plot();

        // Return calculated curve values
        return curve;
    }

    // Calculate and plot a TM dispersion curve
    // Graph title not provided
    public double[][] plotDispersionCurveTM(double lowThickness, double highThickness, int numberOfPoints, double modeNumber){
        String legend1 = " ";
        return this.plotDispersionCurveTM(lowThickness, highThickness, numberOfPoints, modeNumber, legend1);
    }

    // Calculate and plot a TM dispersion curve
    // Graph title provided
    public double[][] plotDispersionCurveTM(double lowThickness, double highThickness, int numberOfPoints, double modeNumber, String legend1){

        //Calculate curve
        double[][] curve = dispersionCurveTM(lowThickness, highThickness, numberOfPoints, modeNumber);

        // Create instance of PlotGraph
        PlotGraph pg2 = new PlotGraph(curve);
        int lineOption = 3;
        if(numberOfPoints<100)lineOption = 1;
        pg2.setLine(lineOption);
        pg2.setPoint(0);
        String legend0 = "Dispersion curve: TM mode  -  mode number " + (int)modeNumber;
        pg2.setGraphTitle(legend0);
        pg2.setGraphTitle2(legend1);
        pg2.setXaxisLegend("Log10( Core Film Thickness / metres )");
        pg2.setYaxisLegend("Effective Refractive Index (kz/ko)");

        // Plot graph
        pg2.plot();

        // Return calculated curve values
        return curve;
    }

    // PLOT FITTED DISPERSION CURVE
    // Graph title not provided
    public void plotFittedDispersionCurves(){
        String legend = "PlanarWaveguide.plotDispersion - Dispersion Plot";
        this.plotFittedDispersionCurve(legend);
    }

    // Graph title provided
    public void plotFittedDispersionCurve(String legend){

        if(!this.calculationDone)this.calcCoreFilmRefractiveIndices();

        // separate TE mode orders
        ArrayList<Object> arraylTE = null;
        int pOrderNumberTE = 0;
        int pOrdersCheckedTE = 0;
        int maximumNumberOfPoints = 0;
        if(this.numberOfTEmeasurements>0){
            arraylTE = new ArrayList<Object>();
            boolean testModes = true;
            int pOrder = 0;
            int numberTestedPositive = 0;
            while(testModes){
                int pNumber = 0;
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    if(this.measurementsTE[i][3]==pOrder){
                        pNumber++;
                        numberTestedPositive++;
                        arraylTE.add(new Double(this.measurementsTE[i][0]));
                        arraylTE.add(new Double(this.measurementsTE[i][1]));
                    }
                }
                arraylTE.add(2*pOrder, new Integer(pOrder));
                arraylTE.add(2*pOrder+1, new Integer(pNumber));
                if(pNumber>0)pOrderNumberTE++;
                if(pNumber>maximumNumberOfPoints)maximumNumberOfPoints = pNumber;
                if(numberTestedPositive==this.numberOfTEmeasurements){
                    testModes=false;
                }
                else{
                    pOrder++;
                }
            }
            pOrdersCheckedTE = pOrder;
        }
        int numberOfCurves = pOrderNumberTE;

        // separate TM mode orders
        ArrayList<Object> arraylTM = null;
        int pOrderNumberTM = 0;
        int pOrdersCheckedTM = 0;
        if(this.numberOfTMmeasurements>0){
            arraylTM = new ArrayList<Object>();
            boolean testModes = true;
            int pOrder = 0;
            int numberTestedPositive = 0;
            while(testModes){
                int pNumber = 0;
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    if(this.measurementsTM[i][3]==pOrder){
                        pNumber++;
                        numberTestedPositive++;
                        arraylTM.add(new Double(this.measurementsTM[i][0]));
                        arraylTM.add(new Double(this.measurementsTM[i][1]));
                    }
                }
                arraylTM.add(2*pOrder, new Integer(pOrder));
                arraylTM.add(2*pOrder+1, new Integer(pNumber));
                if(pNumber>0)pOrderNumberTM++;
                if(pNumber>maximumNumberOfPoints)maximumNumberOfPoints = pNumber;
                if(numberTestedPositive==this.numberOfTMmeasurements){
                    testModes=false;
                }
                else{
                    pOrder++;
                }
            }
            pOrdersCheckedTM = pOrder;
        }
        numberOfCurves += pOrderNumberTM;
        numberOfCurves *= 2;
        if(maximumNumberOfPoints<200)maximumNumberOfPoints = 200;


        // Set up plotting data arrays
        double[][] plotData = PlotGraph.data(numberOfCurves, maximumNumberOfPoints);
        double[] modeNumber = new double[numberOfCurves];
        String[] modeType = new String[numberOfCurves];

        int atCurveNumber = 0;
        int plotNumber = 0;
        // TE mode curves
        int arraylIndex = 2*(pOrdersCheckedTE+1);
        int arraylHeaderIndex = 1;
        double tempD = 0.0D;
        int tempI = 0;

        if(this.numberOfTEmeasurements>0){
            int testVec = 0;
            int arraylSize = arraylTE.size();
            while(testVec<arraylSize){
                // Check mode number has associated experimental data
                tempI = ((Integer)arraylTE.get(arraylHeaderIndex)).intValue();
                testVec++;
                if(tempI>0){
                    modeType[atCurveNumber] = "TE";
                    modeType[atCurveNumber+1] = "TE";
                    modeNumber[atCurveNumber] = ((Integer)arraylTE.get(arraylHeaderIndex-1)).intValue();
                    modeNumber[atCurveNumber+1] = modeNumber[atCurveNumber];
                    testVec++;

                    // experimental data curve
                    double[] tempThick = new double[tempI];
                    double[] tempRefra = new double[tempI];
                    for(int i=0; i<tempI; i++){
                        tempThick[i] = ((Double)arraylTE.get(arraylIndex++)).doubleValue();
                        tempRefra[i] = ((Double)arraylTE.get(arraylIndex++)).doubleValue();
                        testVec += 2;
                    }
                    double[] log10TempThick = Conv.copy(tempThick);
                    for(int i=0; i<tempI; i++)log10TempThick[i] = Fmath.log10(tempThick[i]);

                    plotData[plotNumber++] = log10TempThick;
                    plotData[plotNumber++] = tempRefra;

                    // sort into ascending thicknesses
                    Fmath.selectionSort(tempThick, tempRefra, tempThick, tempRefra);

                    // calculated curve
                    double[][] curveTE = dispersionCurveTE(tempThick[0], tempThick[tempI-1], maximumNumberOfPoints, modeNumber[atCurveNumber]);
                    plotData[plotNumber++] = curveTE[0];
                    plotData[plotNumber++] =  curveTE[1];

                    atCurveNumber += 2;
               }
               arraylHeaderIndex =+ 2;
            }
        }

        // TM mode curves
        arraylIndex = 2*(pOrdersCheckedTM+1);
        arraylHeaderIndex = 1;
        tempD = 0.0D;
        tempI = 0;

        if(this.numberOfTMmeasurements>0){
            int testVec = 0;
            int arraylSize = arraylTM.size();
            while(testVec<arraylSize){
                // Check mode number has associated experimental data
                tempI = ((Integer)arraylTM.get(arraylHeaderIndex)).intValue();
                testVec++;
                if(tempI>0){
                    modeType[atCurveNumber] = "TM";
                    modeType[atCurveNumber+1] = "TM";
                    modeNumber[atCurveNumber] = ((Integer)arraylTM.get(arraylHeaderIndex-1)).intValue();
                    testVec++;
                    modeNumber[atCurveNumber+1] = modeNumber[atCurveNumber];

                    // experimental data curve
                    double[] tempThick = new double[tempI];
                    double[] tempRefra = new double[tempI];
                    for(int i=0; i<tempI; i++){
                        tempThick[i] = ((Double)arraylTM.get(arraylIndex++)).doubleValue();
                        tempRefra[i] = ((Double)arraylTM.get(arraylIndex++)).doubleValue();
                        testVec += 2;
                    }
                    double[] log10TempThick = Conv.copy(tempThick);
                    for(int i=0; i<tempI; i++)log10TempThick[i] = Fmath.log10(tempThick[i]);

                    plotData[plotNumber++] = log10TempThick;
                    plotData[plotNumber++] = tempRefra;

                    // sort into ascending thicknesses
                    Fmath.selectionSort(tempThick, tempRefra, tempThick, tempRefra);

                    // calculated curve
                    double[][] curveTM = dispersionCurveTM(tempThick[0], tempThick[tempI-1], maximumNumberOfPoints, modeNumber[atCurveNumber]);
                    plotData[plotNumber++] = curveTM[0];
                    plotData[plotNumber++] = curveTM[1];

                    atCurveNumber += 2;
               }
               arraylHeaderIndex =+ 2;
            }
        }

        // Create instance of PlotGraph
        PlotGraph pg0 = new PlotGraph(plotData);

        int[] lineOptions = new int[numberOfCurves];
        for(int i=0; i<numberOfCurves; i+=2){
            lineOptions[i] = 0;
            lineOptions[i+1] = 3;
            if(maximumNumberOfPoints<100)lineOptions[i+1] = 1;
        }
        pg0.setLine(lineOptions);

        int[] pointOptions = new int[numberOfCurves];
        int jj = 1;
        for(int i=0; i<numberOfCurves; i+=2){
            pointOptions[i] = jj;
            pointOptions[i+1] = 0;
            jj++;
        }
        pg0.setPoint(pointOptions);

        pg0.setGraphTitle(legend);
        pg0.setXaxisLegend("Log10( Core Film Thickness / metres )");
        pg0.setYaxisLegend("Effective Refractive Index (kz/ko)");

        // Plot graphs
        pg0.plot();
    }

    // CALCULATION OF THE SUPERSTRATE REFRACTIVE INDEX
    public void calcSuperstrateRefractiveIndex(){
        if(!this.setMeasurements)throw new IllegalArgumentException("Either no thickness, angle/effective refractive index, mode number data has been entered or a key subclass variable, e.g. coupling prism corner angle has not been entered");
        if(!this.setWavelength)throw new IllegalArgumentException("No wavelength has been entered");
        if(!this.setSubstrate)throw new IllegalArgumentException("No substrate refractive index has been entered");
        if(!this.setCore)throw new IllegalArgumentException("No core layer refractive index has been entered");

        // Set the bounds and eliminate points where effective ref. index < substrate or superstrate ref. index
        this.lowerBound = 1.0D;
        this.upperBound = this.coreFilmRefractiveIndex;

        if(this.numberOfTEmeasurements>0)this.eliminatedTE = new boolean[this.numberOfTEmeasurements];
        int elimNumberTE = 0;
        for(int i=0; i<this.numberOfTEmeasurements; i++){
            this.eliminatedTE[i] = false;
            if(this.measurementsTE[i][1]>this.coreFilmRefractiveIndex){
                System.out.println("TE mode measurement point, " + i + ", eliminated as the effective refractive index, " +  this.measurementsTE[i][1] + ", lies above the physical limit, " + this.coreFilmRefractiveIndex);
                this.eliminatedTE[i] = true;
                elimNumberTE++;
            }
            else{
                if(this.upperBound>this.measurementsTE[i][1])this.upperBound = this.measurementsTE[i][1];
            }
        }
        if(elimNumberTE>0){
            int newNumber = this.numberOfTEmeasurements - elimNumberTE;
            if(newNumber==0){
                this.numberOfTEmeasurements = 0;
            }
            else{
                double[][] temp = new double[newNumber][3];
                int nIndex = 0;
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    if(!this.eliminatedTE[i]){
                        temp[nIndex][0] = this.measurementsTE[i][0];
                        temp[nIndex][1] = this.measurementsTE[i][1];
                        temp[nIndex][2] = this.measurementsTE[i][2];
                        temp[nIndex][3] = this.measurementsTE[i][3];
                        nIndex++;
                    }
                }
                this.measurementsTE = temp;
                this.numberOfTEmeasurements = newNumber;
                this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
            }
        }
        this.thicknessesUsedTE = new double[this.numberOfTEmeasurements];
        this.calcEffectRefrIndicesTE = new double[this.numberOfTEmeasurements];
        for(int i=0; i<this.numberOfTEmeasurements; i++)this.thicknessesUsedTE[i] = this.measurementsTE[i][0];
        this.minimumTEmodeEffectiveRefractiveIndex = this.upperBound;

        this.upperBound = 0.0D;
        if(this.numberOfTMmeasurements>0)this.eliminatedTM = new boolean[this.numberOfTMmeasurements];
        int elimNumberTM = 0;
        for(int i=0; i<this.numberOfTMmeasurements; i++){
            this.eliminatedTM[i] = false;
            if(this.measurementsTM[i][1]>this.coreFilmRefractiveIndex){
                System.out.println("TM mode measurement point, " + i + ", eliminated as the effective refractive index, " +  this.measurementsTM[i][1] + ", lies above the physical limit, " + this.coreFilmRefractiveIndex);
                this.eliminatedTM[i] = true;
                elimNumberTM++;
            }
            else{
                if(this.upperBound>this.measurementsTM[i][1])this.upperBound = this.measurementsTM[i][1];
            }
        }
        if(elimNumberTM>0){
            int newNumber = this.numberOfTMmeasurements - elimNumberTM;
            if(newNumber==0){
                this.numberOfTMmeasurements = 0;
            }
            else{
                double[][] temp = new double[newNumber][3];
                int nIndex = 0;
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    if(!this.eliminatedTM[i]){
                        temp[nIndex][0] = this.measurementsTM[i][0];
                        temp[nIndex][1] = this.measurementsTM[i][1];
                        temp[nIndex][2] = this.measurementsTM[i][2];
                        temp[nIndex][3] = this.measurementsTM[i][3];
                        nIndex++;
                    }
                }
                this.measurementsTM = temp;
                this.numberOfTMmeasurements = newNumber;
                this.numberOfMeasurements = this.numberOfTEmeasurements + this.numberOfTMmeasurements;
            }
        }
        this.thicknessesUsedTM = new double[this.numberOfTMmeasurements];
        this.calcEffectRefrIndicesTM = new double[this.numberOfTMmeasurements];
        for(int i=0; i<this.numberOfTMmeasurements; i++)this.thicknessesUsedTM[i] = this.measurementsTM[i][0];
        this.minimumTMmodeEffectiveRefractiveIndex = this.upperBound;

        if(this.numberOfMeasurements==0)throw new IllegalArgumentException("All data points rejected as lying outside the physically meaningful bounds");

        // call the root search methods to obtain the superstrate refractive index/indices
        if(this.numberOfTEmeasurements>0)this.calcTEmodeSuperstrateRefractiveIndices();
        if(this.numberOfTMmeasurements>0)this.calcTMmodeSuperstrateRefractiveIndices();

        // Calculate the overall mean and standard deviation of the superstrate refractive index
        if(this.numberOfTEmeasurements>0 && this.numberOfTMmeasurements==0){
            this.superstrateRefractiveIndex = this.meanTEmodeSuperstrateRefractiveIndex;
            this.sdSuperstrateRefractiveIndex = this.sdTEmodeSuperstrateRefractiveIndex;
        }
        else{
            if(this.numberOfTMmeasurements>0 && this.numberOfTEmeasurements==0){
                this.superstrateRefractiveIndex = this.meanTMmodeSuperstrateRefractiveIndex;
                this.sdSuperstrateRefractiveIndex = this.sdTMmodeSuperstrateRefractiveIndex;
            }
            else{
                double[] values = new double[this.numberOfMeasurements];
                double[] weights = new double[this.numberOfMeasurements];
                for(int i=0; i<this.numberOfTEmeasurements; i++){
                    values[i] = this.calcSuperstrateTEmodeRI[i];
                    weights[i] = this.measurementsTE[i][2];
                }
                for(int i=0; i<this.numberOfTMmeasurements; i++){
                    values[i+this.numberOfTEmeasurements] = this.calcSuperstrateTMmodeRI[i];
                    weights[i+this.numberOfTEmeasurements] = this.measurementsTM[i][2];
                }
                this.superstrateRefractiveIndex = Stat.mean(values, weights);
                this.sdSuperstrateRefractiveIndex = Stat.standardDeviation(values, weights);
            }
        }

        this.superstrateRefractiveIndex2 = this.superstrateRefractiveIndex*this.superstrateRefractiveIndex;
        this.minimumEffectiveRefractiveIndex = Math.min(this.minimumTEmodeEffectiveRefractiveIndex, this.minimumTMmodeEffectiveRefractiveIndex);
        this.superCalculationDone = true;
    }

    // Calculate TE mode refractive indices
    public void calcTEmodeSuperstrateRefractiveIndices(){

        this.calcSuperstrateTEmodeRI = new double[this.numberOfTEmeasurements];

        // Create instance of the class holding the TE mode core film refractive indexfunction
        FunctTEsuper func = new FunctTEsuper();

        // Set function parameters
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.ko = this.ko;

        double[] weights = new double[this.numberOfTEmeasurements];
        this.lowerBound = 1.0D;
        this.upperBound = this.minimumTEmodeEffectiveRefractiveIndex;

        for(int i=0; i<this.numberOfTEmeasurements; i++){
            weights[i] = this.measurementsTE[i][2];

            // set further function parameters
            func.thickness = this.measurementsTE[i][0];
            func.effectiveRefractiveIndex2 = this.measurementsTE[i][1]*this.measurementsTE[i][1];
            func.modeNumber = this.measurementsTE[i][3];

            // call root searching method, bisection, to obtain core refractive index
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            this.calcSuperstrateTEmodeRI[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }

        // Calculate mean and sd
        if(this.numberOfTEmeasurements>1){
            this.meanTEmodeSuperstrateRefractiveIndex = Stat.mean(this.calcSuperstrateTEmodeRI, weights);
            this.sdTEmodeSuperstrateRefractiveIndex = Stat.standardDeviation(this.calcSuperstrateTEmodeRI, weights);
        }
        else{
            this.meanTEmodeSuperstrateRefractiveIndex = this.calcSuperstrateTEmodeRI[0];
        }
    }

    // Calculate TM mode refractive indices
    public void calcTMmodeSuperstrateRefractiveIndices(){

        this.calcSuperstrateTMmodeRI = new double[this.numberOfTMmeasurements];

        // Create instance of the class holding the TM mode core film refractive indexfunction
        FunctTMsuper func = new FunctTMsuper();

        // Set function parameters
        func.coreFilmRefractiveIndex2 = this.coreFilmRefractiveIndex2;
        func.ko = this.ko;

        double[] weights = new double[this.numberOfTMmeasurements];
        this.lowerBound = 1.0D;
        this.upperBound = this.minimumTMmodeEffectiveRefractiveIndex;

        for(int i=0; i<this.numberOfTMmeasurements; i++){
            weights[i] = this.measurementsTM[i][2];

            // set further function parameters
            func.thickness = this.measurementsTM[i][0];
            func.effectiveRefractiveIndex2 = this.measurementsTM[i][1]*this.measurementsTM[i][1];
            func.modeNumber = this.measurementsTM[i][3];

            // call root searching method, bisection, to obtain core refractive index
            RealRoot rr = new RealRoot();
            rr.noBoundsExtensions();
            rr.setTolerance(this.tolerance);
            this.calcSuperstrateTMmodeRI[i] = rr.bisect(func, this.lowerBound, this.upperBound);
        }

        // Calculate mean and sd
        if(this.numberOfTMmeasurements>1){
            this.meanTMmodeSuperstrateRefractiveIndex = Stat.mean(this.calcSuperstrateTMmodeRI, weights);
            this.sdTMmodeSuperstrateRefractiveIndex = Stat.standardDeviation(this.calcSuperstrateTMmodeRI, weights);
        }
        else{
            this.meanTMmodeSuperstrateRefractiveIndex = this.calcSuperstrateTMmodeRI[0];
        }
    }
}

// Class containing function with the root search for the TE mode core film refractive index
class FunctTE implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double superstrateRefractiveIndex2 = 0.0D;
    public double effectiveRefractiveIndex2 = 0.0D;
    public double prismRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double prismToWaveguideGap = 0.0D;
    public boolean setPrismToWaveguideGap = false;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        // function calculation
        double coreFilmRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(this.effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(this.effectiveRefractiveIndex2 - this.superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(coreFilmRefractiveIndex2 - this.effectiveRefractiveIndex2);
        double zetaPrism = Math.sqrt(this.prismRefractiveIndex2 - this.effectiveRefractiveIndex2);
        double gammaSuper = Math.atan2(zetaSuper, zetaFilm);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (gammaSuper +  Math.atan2(zetaSub, zetaFilm));
        if(this.setPrismToWaveguideGap) y += (Math.sin(gammaSuper)*Math.cos( Math.atan2(zetaSuper, zetaPrism))*Math.exp(-2.0D*this.prismToWaveguideGap*this.ko*zetaSuper));

        return y;
    }
}

// Class containing function with the root search for the TM mode core film refractive index
class FunctTM implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double superstrateRefractiveIndex2 = 0.0D;
    public double effectiveRefractiveIndex2 = 0.0D;
    public double prismRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double prismToWaveguideGap = 0.0D;
    public boolean setPrismToWaveguideGap = false;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        // function calculation
        double coreFilmRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(this.effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(this.effectiveRefractiveIndex2 - this.superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(coreFilmRefractiveIndex2 - this.effectiveRefractiveIndex2);
        double zetaPrism = Math.sqrt(this.prismRefractiveIndex2 - this.effectiveRefractiveIndex2);
        double gammaSuper = Math.atan2(coreFilmRefractiveIndex2*zetaSuper, this.superstrateRefractiveIndex2*zetaFilm);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (gammaSuper +  Math.atan2(coreFilmRefractiveIndex2*zetaSub, this.substrateRefractiveIndex2*zetaFilm));
        if(this.setPrismToWaveguideGap) y += (Math.sin(gammaSuper)*Math.cos( Math.atan2(zetaSuper*this.prismRefractiveIndex2, zetaPrism*this.superstrateRefractiveIndex2))*Math.exp(-2.0D*this.prismToWaveguideGap*zetaSuper));

        return y;
    }
}

// Class containing function with the root search for the TE mode effective refractive index
class FunctTEplot implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double superstrateRefractiveIndex2 = 0.0D;
    public double coreFilmRefractiveIndex2 = 0.0D;
    public double prismRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double prismToWaveguideGap = 0.0D;
    public boolean setPrismToWaveguideGap = false;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        // function calculation
        double effectiveRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(effectiveRefractiveIndex2 - this.superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(this.coreFilmRefractiveIndex2 - effectiveRefractiveIndex2);
        double zetaPrism = Math.sqrt(this.prismRefractiveIndex2 - effectiveRefractiveIndex2);
        double gammaSuper = Math.atan2(zetaSuper, zetaFilm);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (gammaSuper +  Math.atan2(zetaSub, zetaFilm));
        if(this.setPrismToWaveguideGap) y += (Math.sin(gammaSuper)*Math.cos( Math.atan2(zetaSuper, zetaPrism))*Math.exp(-2.0D*this.prismToWaveguideGap*this.ko*zetaSuper));

        return y;
    }
}

// Class containing function with the root search for the TM mode effective refractive index
class FunctTMplot implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double superstrateRefractiveIndex2 = 0.0D;
    public double coreFilmRefractiveIndex2 = 0.0D;
    public double prismRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double prismToWaveguideGap = 0.0D;
    public boolean setPrismToWaveguideGap = false;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        double effectiveRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(effectiveRefractiveIndex2 - this.superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(this.coreFilmRefractiveIndex2 - effectiveRefractiveIndex2);
        double zetaPrism = Math.sqrt(this.prismRefractiveIndex2 - effectiveRefractiveIndex2);
        double gammaSuper = Math.atan2(this.coreFilmRefractiveIndex2*zetaSuper, this.superstrateRefractiveIndex2*zetaFilm);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (gammaSuper +  Math.atan2(this.coreFilmRefractiveIndex2*zetaSub, this.substrateRefractiveIndex2*zetaFilm));
        if(this.setPrismToWaveguideGap) y += (Math.sin(gammaSuper)*Math.cos( Math.atan2(zetaSuper*this.prismRefractiveIndex2, zetaPrism*this.superstrateRefractiveIndex2))*Math.exp(-2.0D*this.prismToWaveguideGap*zetaSuper));

        return y;
    }
}

// Class containing function with the root search for the TE mode superstrate refractive index
class FunctTEsuper implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double effectiveRefractiveIndex2 = 0.0D;
    public double coreFilmRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        // function calculation
        double superstrateRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(this.effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(effectiveRefractiveIndex2 - superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(this.coreFilmRefractiveIndex2 - this.effectiveRefractiveIndex2);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (Math.atan2(zetaSuper, zetaFilm) +  Math.atan2(zetaSub, zetaFilm));

        return y;
    }
}

// Class containing function with the root search for the TM mode superstrate refractive index
class FunctTMsuper implements RealRootFunction{

    public double substrateRefractiveIndex2 = 0.0D;
    public double effectiveRefractiveIndex2 = 0.0D;
    public double coreFilmRefractiveIndex2 = 0.0D;
    public double ko = 0.0D;
    public double thickness = 0.0D;
    public double modeNumber = 0;

    public double function(double x){
        double y = 0.0D;

        double superstrateRefractiveIndex2 = x*x;
        double zetaSub = Math.sqrt(this.effectiveRefractiveIndex2 - this.substrateRefractiveIndex2);
        double zetaSuper = Math.sqrt(effectiveRefractiveIndex2 - superstrateRefractiveIndex2);
        double zetaFilm = Math.sqrt(this.coreFilmRefractiveIndex2 - this.effectiveRefractiveIndex2);
        y = Math.PI*modeNumber - this.thickness*this.ko*zetaFilm;
        y += (Math.atan2(this.coreFilmRefractiveIndex2*zetaSuper, superstrateRefractiveIndex2*zetaFilm) +  Math.atan2(this.coreFilmRefractiveIndex2*zetaSub, this.substrateRefractiveIndex2*zetaFilm));

        return y;
    }
}
