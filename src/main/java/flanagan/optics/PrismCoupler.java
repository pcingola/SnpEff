/*
*   PrismCoupler Class
*
*   Methods for:
*       determining the refractive index of a waveguiding thin film
*       in an asymmetric slab waveguide from the prism coupling angle and the core layer thickness
*
*       obtaining the normalised propagation vector versus guiding layer thickness
*       dispersion curve for an asymmetric slab waveguide
*
*   This is a subclass of the superclasses PlanarWaveguideCoupler
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: April 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/PrismCoupler.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) April 2006   Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.optics;

import flanagan.math.Fmath;
import flanagan.analysis.ErrorProp;
import flanagan.plot.*;
import flanagan.optics.*;

public class PrismCoupler extends PlanarWaveguide{

    // CLASS VARIABLES

    private double[] thicknessesTE = null;      // thicknesses for experimental TE mode thimeasurements
    private double[] anglesDegTE = null;        // coupling angles, in degrees, for experimental TE mode measurements
    private double[] anglesRadTE = null;        // coupling angles, in radians, for experimental TE mode measurements
    private double[] errorsDegTE = null;        // errors in coupling angles, in degrees, for experimental TE mode measurements
    private double[] errorsRadTE = null;        // errors in coupling angles, in radians, for experimental TE mode measurements
    private double[] modeNumbersTE = null;      // mode numbers for experimental TE mode measurements
    private double[] effectiveRefractiveIndicesTE = null;   // effective refractive indices for TE mode measurements
    private double[] effectiveErrorsTE = null;  // propagated errors for effective refractive indices for TE mode measurements
    private int numberOfTEmeasurementsPrism = 0; // number of TE mode thickness measurement
    private boolean setMeasurementsTEprism = false; // = true when TE mode measurements entered
    private boolean setTEerrors = false;        // = true if TE mode errors are set

    private double[] thicknessesTM = null;      // thicknesses for experimental TM mode thimeasurements
    private double[] anglesDegTM = null;        // coupling angles, in degrees, for experimental TM mode measurements
    private double[] anglesRadTM = null;        // coupling angles, in radians, for experimental TM mode measurements
    private double[] errorsDegTM = null;        // errors in coupling angles, in degrees, for experimental TM mode measurements
    private double[] errorsRadTM = null;        // errors in coupling angles, in radians, for experimental TM mode measurements
    private double[] modeNumbersTM = null;      // mode numbers for experimental TM mode measurements
    private double[] effectiveRefractiveIndicesTM = null;   // effective refractive indices for TM mode measurements
    private double[] effectiveErrorsTM = null;  // propagated errors for effective refractive indices for TM mode measurements
    private int numberOfTMmeasurementsPrism = 0; // number of TM mode thickness measurement
    private boolean setMeasurementsTMprism = false;    // = true when TM mode measurements entered
    private boolean setTMerrors = false;        // = true if TM mode errors are set


    private int numberOfMeasurementsPrism = 0;  // total number of thickness measurements entered
    private boolean setMeasurementsPrism = false;    // = true when TE and/or TM mode measurements entered

    private boolean setPrismRI = false;         // = true when prism refractive index entered
    private double prismAngleAlphaDeg = 0.0D;   // Coupling prism angle alpha (in degrees)
    private double prismAngleAlphaRad = 0.0D;   // Coupling prism angle alpha (in radians)
    private boolean setPrismAlpha = false;      // = true when prism angle, alpha, entered


    // CONSTRUCTOR
    public PrismCoupler(){
    }

    // COUPLING PRISM
    // Enter refractive index
    public void setPrismRefractiveIndex(double refInd){
        super.prismRefractiveIndex = refInd;
        super.prismRefractiveIndex2 = refInd*refInd;
        this.setPrismRI = true;
        if(this.setMeasurementsPrism && this.setPrismAlpha)this.calcEffectiveRefractiveIndices();
    }

    // Enter coupling prism angle, alpha, in degrees
    public void setPrismAngleAlpha(double angle){
        this.prismAngleAlphaDeg = angle;
        this.prismAngleAlphaRad = Math.toRadians(angle);
        this.setPrismAlpha = true;
        if(this.setMeasurementsPrism && this.setPrismRI)this.calcEffectiveRefractiveIndices();
    }

    // Enter coupling prism to waveguide gap distance in metres
    public void setPrismToWaveguideGap(double gap){
        super.prismToWaveguideGap = gap;
        super.setPrismToWaveguideGap = true;
    }

    // THICKNESS (metres), COUPLING ANGLE (degrees) AND MODE NUMBER DATA
    // Enter TE mode data for a single measurement without error
    public void enterTEmodeData(double thickness, double angle, double modeNumber){
        if(this.setMeasurementsTEprism){
            if(setErrorsTE)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsPrism + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.thicknessesTE[i];
            hold[this.numberOfTEmeasurementsPrism] = thickness;
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.anglesDegTE[i];
            hold[this.numberOfTEmeasurementsPrism] = angle;
            this.anglesDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsDegTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsDegTE[i] = 0.0D;
                this.errorsRadTE[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.modeNumbersTE[i];
            hold[this.numberOfTEmeasurementsPrism] = modeNumber;
            this.numberOfTEmeasurementsPrism = nNew;
        }
        else{
            this.thicknessesTE = new double[1];
            this.thicknessesTE[0] = thickness;
            this.anglesDegTE = new double[1];
            this.anglesDegTE[0] = angle;
            this.anglesRadTE = new double[1];
            this.anglesRadTE[0] = Math.toRadians(angle);
            this.errorsDegTE = new double[1];
            this.errorsDegTE[0] = 0.0D;
            this.errorsRadTE = new double[1];
            this.errorsRadTE[0] = 0.0D;
            this.modeNumbersTE = new double[1];
            this.modeNumbersTE[0] = modeNumber;
            this.numberOfTEmeasurementsPrism = 1;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTEprism = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismAlpha)this.calcTEmodeEffectiveRefractiveIndices();
    }



    // Enter TM mode data for a single measurement without error
    public void enterTMmodeData(double thickness, double angle, double modeNumber){
        if(this.setMeasurementsTMprism){
            if(this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsPrism + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.thicknessesTM[i];
            hold[this.numberOfTMmeasurementsPrism] = thickness;
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.anglesDegTM[i];
            hold[this.numberOfTMmeasurementsPrism] = angle;
            this.anglesDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsDegTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsDegTM[i] = 0.0D;
                this.errorsRadTM[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.modeNumbersTM[i];
            hold[this.numberOfTMmeasurementsPrism] = modeNumber;
            this.numberOfTMmeasurementsPrism = nNew;
        }
        else{
            this.thicknessesTM = new double[1];
            this.thicknessesTM[0] = thickness;
            this.anglesDegTM = new double[1];
            this.anglesDegTM[0] = angle;
            this.anglesRadTM = new double[1];
            this.anglesRadTM[0] = Math.toRadians(angle);
            this.errorsDegTM = new double[1];
            this.errorsDegTM[0] = 0.0D;
            this.errorsRadTM = new double[1];
            this.errorsRadTM[0] = 0.0D;
            this.modeNumbersTM = new double[1];
            this.modeNumbersTM[0] = modeNumber;
            this.numberOfTMmeasurementsPrism = 1;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTMprism = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Enter TE mode data for a range of measurements without errors
    public void enterTEmodeData(double[] thicknesses, double[] angles, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTEprism){
            if(this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsPrism + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.thicknessesTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = thicknesses[i];
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.anglesDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = angles[i];
            this.anglesDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsDegTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsDegTE[i] = 0.0D;
                this.errorsRadTE[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.modeNumbersTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = modeNumbers[i];
            this.numberOfTEmeasurementsPrism = nNew;
        }
        else{
            this.numberOfTEmeasurementsPrism = o;
            this.thicknessesTE = thicknesses;
            this.anglesDegTE = angles;
            this.anglesRadTE = new double[o];
            this.errorsDegTE = new double[o];
            this.errorsRadTE = new double[o];
            for(int i=0; i<o; i++){
                this.anglesRadTE[i] = Math.toRadians(angles[i]);
                this.errorsDegTE[i] = 0.0D;
                this.errorsRadTE[i] = 0.0D;
            }
            this.modeNumbersTE = modeNumbers;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTEprism = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTEmodeEffectiveRefractiveIndices();
    }

    // Enter TM mode data for a range of measurements without errors
    public void enterTMmodeData(double[] thicknesses, double[] angles, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTMprism){
            if(this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsPrism + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.thicknessesTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = thicknesses[i];
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.anglesDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = angles[i];
            this.anglesDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsDegTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsDegTM[i] = 0.0D;
                this.errorsRadTM[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.modeNumbersTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = modeNumbers[i];
            this.numberOfTMmeasurementsPrism = nNew;
        }
        else{
            this.numberOfTMmeasurementsPrism = o;
            this.thicknessesTM = thicknesses;
            this.anglesDegTM = angles;
            this.anglesRadTM = new double[o];
            this.errorsDegTM = new double[o];
            this.errorsRadTM = new double[o];
            for(int i=0; i<o; i++){
                this.anglesRadTM[i] = Math.toRadians(angles[i]);
                this.errorsDegTM[i] = 0.0D;
                this.errorsRadTM[i] = 0.0D;
            }
            this.modeNumbersTM = modeNumbers;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTMprism = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Enter TE mode data for a single measurement with error
    public void enterTEmodeData(double thickness, double angle, double error, double modeNumber){
        if(this.setMeasurementsTEprism){
            if(!this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsPrism + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.thicknessesTE[i];
            hold[this.numberOfTEmeasurementsPrism] = thickness;
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.anglesDegTE[i];
            hold[this.numberOfTEmeasurementsPrism] = angle;
            this.anglesDegTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.errorsDegTE[i];
            hold[this.numberOfTEmeasurementsPrism] = error;
            this.errorsDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsRadTE[i] = Math.toRadians(this.errorsDegTE[i]);
            }
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.modeNumbersTE[i];
            hold[this.numberOfTEmeasurementsPrism] = modeNumber;
            this.numberOfTEmeasurementsPrism = nNew;
        }
        else{
            this.thicknessesTE = new double[1];
            this.thicknessesTE[0] = thickness;
            this.anglesDegTE = new double[1];
            this.anglesDegTE[0] = angle;
            this.anglesRadTE = new double[1];
            this.anglesRadTE[0] = Math.toRadians(angle);
            this.errorsDegTE = new double[1];
            this.errorsDegTE[0] = error;
            this.errorsRadTE = new double[1];
            this.errorsRadTE[0] = Math.toRadians(error);
            this.modeNumbersTE = new double[1];
            this.modeNumbersTE[0] = modeNumber;
            this.numberOfTEmeasurementsPrism = 1;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTEprism = true;
        this.setTEerrors = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTEmodeEffectiveRefractiveIndices();
    }



    // Enter TM mode data for a single measurement with error
    public void enterTMmodeData(double thickness, double angle, double error, double modeNumber){
        if(this.setMeasurementsTMprism){
            if(!this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsPrism + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.thicknessesTM[i];
            hold[this.numberOfTMmeasurementsPrism] = thickness;
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.anglesDegTM[i];
            hold[this.numberOfTMmeasurementsPrism] = angle;
            this.anglesDegTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.errorsDegTM[i];
            hold[this.numberOfTMmeasurementsPrism] = error;
            this.errorsDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsRadTM[i] = Math.toRadians(this.errorsDegTM[i]);
            }
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.modeNumbersTM[i];
            hold[this.numberOfTMmeasurementsPrism] = modeNumber;
            this.numberOfTMmeasurementsPrism = nNew;
        }
        else{
            this.thicknessesTM = new double[1];
            this.thicknessesTM[0] = thickness;
            this.anglesDegTM = new double[1];
            this.anglesDegTM[0] = angle;
            this.anglesRadTM = new double[1];
            this.anglesDegTM[0] = Math.toRadians(angle);
            this.errorsDegTM = new double[1];
            this.errorsDegTM[0] = error;
            this.errorsRadTM = new double[1];
            this.errorsDegTM[0] = Math.toRadians(error);
            this.modeNumbersTM = new double[1];
            this.modeNumbersTM[0] = modeNumber;
            this.numberOfTMmeasurementsPrism = 1;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTMprism = true;
        this.setTMerrors = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Enter TE mode data for a range of measurements with errors
    public void enterTEmodeData(double[] thicknesses, double[] angles, double[] errors, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTEprism){
            if(!this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsPrism + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.thicknessesTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = thicknesses[i];
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.anglesDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = angles[i];
            this.anglesDegTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.errorsDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = errors[i];
            this.errorsDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsRadTE[i] = Math.toRadians(this.errorsDegTE[i]);
            }
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++)hold[i] = this.modeNumbersTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsPrism+i] = modeNumbers[i];
            this.numberOfTEmeasurementsPrism = nNew;
        }
        else{
            this.numberOfTEmeasurementsPrism = o;
            this.thicknessesTE = thicknesses;
            this.anglesDegTE = angles;
            this.anglesRadTE = new double[o];
            this.errorsDegTE = errors;
            this.errorsRadTE = new double[o];
            for(int i=0; i<o; i++){
                this.anglesRadTE[i] = Math.toRadians(angles[i]);
                this.errorsRadTE[i] = Math.toRadians(errors[i]);
            }
            this.modeNumbersTE = modeNumbers;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTEprism = true;
        this.setTEerrors = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTEmodeEffectiveRefractiveIndices();
    }

    // Enter TM mode data for a range of measurements without errors
    public void enterTMmodeData(double[] thicknesses, double[] angles, double[] errors, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTMprism){
            if(!this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsPrism + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.thicknessesTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = thicknesses[i];
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.anglesDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = angles[i];
            this.anglesDegTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.errorsDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = errors[i];
            this.errorsDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsRadTM[i] = Math.toRadians(this.errorsDegTM[i]);
            }
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++)hold[i] = this.modeNumbersTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsPrism+i] = modeNumbers[i];
            this.numberOfTMmeasurementsPrism = nNew;
        }
        else{
            this.numberOfTMmeasurementsPrism = o;
            this.thicknessesTM = thicknesses;
            this.anglesDegTM = angles;
            this.errorsDegTM = errors;
            this.anglesRadTM = new double[o];
            this.errorsRadTM = new double[o];
            for(int i=0; i<o; i++){
                this.anglesRadTM[i] = Math.toRadians(angles[i]);
                this.errorsRadTM[i] = Math.toRadians(errors[i]);
            }
            this.modeNumbersTM = modeNumbers;
        }
        this.numberOfMeasurementsPrism = this.numberOfTEmeasurementsPrism + this.numberOfTMmeasurementsPrism;
        this.setMeasurementsTMprism = true;
        this.setTMerrors = true;
        this.setMeasurementsPrism = true;
        if(this.setPrismAlpha && this.setPrismRI)this.calcTMmodeEffectiveRefractiveIndices();
    }


    // Clear entered thickness, effective refractive index and mode number data
    //  so new dat may be entered without it being appended to the existing data
    public void clearData(){
        this.numberOfTEmeasurementsPrism = 0;
        this.setMeasurementsTEprism = false;

        this.numberOfTMmeasurementsPrism = 0;
        this.setMeasurementsTMprism = false;

        super.numberOfMeasurements = 0;
        super.setMeasurements = false;
        super.setWeights = false;

        super.numberOfTEmeasurements = 0;
        super.setMeasurementsTE = false;

        super.numberOfTMmeasurements = 0;
        super.setMeasurementsTM = false;
    }

    // CALCULATION OF THE EFFECTIVE REFRACTIVE INDEX/INDICES
    // Calculate all effective refractive indices
    public void calcEffectiveRefractiveIndices(){
        if(this.setMeasurementsTEprism)this.calcTEmodeEffectiveRefractiveIndices();
        if(this.setMeasurementsTMprism)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Calculate TE mode effective refractive indices
    public void calcTEmodeEffectiveRefractiveIndices(){
        this.effectiveRefractiveIndicesTE = new double[this.numberOfTEmeasurementsPrism];
        this.effectiveErrorsTE = new double[this.numberOfTEmeasurementsPrism];

        if(this.setTEerrors){
           ErrorProp alpha = new ErrorProp(this.prismAngleAlphaRad, 0.0D);
           ErrorProp prismRI = new ErrorProp(super.prismRefractiveIndex, 0.0D);
           ErrorProp airRI = new ErrorProp(RefractiveIndex.air(super.wavelength), 0.0D);
           ErrorProp phi = new ErrorProp();
           ErrorProp angle = new ErrorProp();
           for(int i=0; i<this.numberOfTEmeasurementsPrism; i++){
                angle.reset(this.anglesRadTE[i], this.errorsRadTE[i]);
                phi = (angle.over(prismRI)).times(airRI);
                phi = ErrorProp.asin(phi);
                phi = alpha.plus(phi);
                phi = prismRI.times(ErrorProp.sin(phi));
                this.effectiveRefractiveIndicesTE[i] = phi.getValue();
                this.effectiveErrorsTE[i] = phi.getError();
           }
           super.enterTEmodeData(this.thicknessesTE, this.effectiveRefractiveIndicesTE, this.effectiveErrorsTE, this.modeNumbersTE);
        }
        else{
            for(int i=0; i<this.numberOfTEmeasurementsPrism; i++){
                double phi = this.prismAngleAlphaRad + Math.asin(RefractiveIndex.air(super.wavelength)*this.anglesRadTE[i]/super.prismRefractiveIndex);
                this.effectiveRefractiveIndicesTE[i] = super.prismRefractiveIndex*Math.sin(phi);
            }
            super.enterTEmodeData(this.thicknessesTE, this.effectiveRefractiveIndicesTE, this.modeNumbersTE);
        }
    }

    // Calculate TM mode effective refractive indices
    public void calcTMmodeEffectiveRefractiveIndices(){
        this.effectiveRefractiveIndicesTM = new double[this.numberOfTMmeasurementsPrism];
        this.effectiveErrorsTM = new double[this.numberOfTMmeasurementsPrism];

        if(this.setTMerrors){
           ErrorProp alpha = new ErrorProp(this.prismAngleAlphaRad, 0.0D);
           ErrorProp prismRI = new ErrorProp(super.prismRefractiveIndex, 0.0D);
           ErrorProp airRI = new ErrorProp(RefractiveIndex.air(super.wavelength), 0.0D);
           ErrorProp phi = new ErrorProp();
           ErrorProp angle = new ErrorProp();
           for(int i=0; i<this.numberOfTMmeasurementsPrism; i++){
                angle.reset(this.anglesRadTM[i], this.errorsRadTM[i]);
                phi = (angle.over(prismRI)).times(airRI);
                phi = ErrorProp.asin(phi);
                phi = alpha.plus(phi);
                phi = prismRI.times(ErrorProp.sin(phi));
                this.effectiveRefractiveIndicesTM[i] = phi.getValue();
                this.effectiveErrorsTM[i] = phi.getError();
           }
           super.enterTMmodeData(this.thicknessesTM, this.effectiveRefractiveIndicesTM, this.effectiveErrorsTM, this.modeNumbersTM);
        }
        else{
            for(int i=0; i<this.numberOfTMmeasurementsPrism; i++){
                double phi = this.prismAngleAlphaRad + Math.asin(RefractiveIndex.air(super.wavelength)*this.anglesRadTM[i]/super.prismRefractiveIndex);
                this.effectiveRefractiveIndicesTM[i] = super.prismRefractiveIndex*Math.sin(phi);
            }
            super.enterTMmodeData(this.thicknessesTM, this.effectiveRefractiveIndicesTM, this.modeNumbersTM);
        }
    }
}

