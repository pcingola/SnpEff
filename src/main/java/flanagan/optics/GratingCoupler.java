/*
*   GratingCoupler Class
*
*   Methods for:
*       determining the refractive index of a waveguiding thin film
*       in an asymmetric slab waveguide from the grating coupling angle and the core layer thickness
*       and for obtaining the normalised propagation vector versus guiding layer thickness
*
*       determinimg the refractive index of the superstrate [analyte solution in a grating coupler sensor]
*
*       obtaining the normalised propagation vector versus guiding layer thickness
*       dispersion curve for an asymmetric slab waveguide
*
*   This is a subclass of the superclasses PlanarWaveguideCoupler
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: 28 April 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/GratingCoupler.html
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

public class GratingCoupler extends PlanarWaveguide{

    // CLASS VARIABLES

    private double[] thicknessesTE = null;      // thicknesses for experimental TE mode thimeasurements
    private double[] anglesDegTE = null;        // coupling angles, in degrees, for experimental TE mode measurements
    private double[] anglesRadTE = null;        // coupling angles, in radians, for experimental TE mode measurements
    private double[] errorsDegTE = null;        // errors in coupling angles, in degrees, for experimental TE mode measurements
    private double[] errorsRadTE = null;        // errors in coupling angles, in radians, for experimental TE mode measurements
    private double[] modeNumbersTE = null;      // mode numbers for experimental TE mode measurements
    private double[] effectiveRefractiveIndicesTE = null;   // effective refractive indices for TE mode measurements
    private double[] effectiveErrorsTE = null;  // propagated errors for effective refractive indices for TE mode measurements
    private int numberOfTEmeasurementsGrating = 0; // number of TE mode thickness measurement
    private boolean setMeasurementsTEgrating = false; // = true when TE mode measurements entered
    private boolean setTEerrors = false;        // = true if TE mode errors are set

    private boolean calcEffectiveDone = false;  // = true when effective refractive indices calculated

    private double[] thicknessesTM = null;      // thicknesses for experimental TM mode thimeasurements
    private double[] anglesDegTM = null;        // coupling angles, in degrees, for experimental TM mode measurements
    private double[] anglesRadTM = null;        // coupling angles, in radians, for experimental TM mode measurements
    private double[] errorsDegTM = null;        // errors in coupling angles, in degrees, for experimental TM mode measurements
    private double[] errorsRadTM = null;        // errors in coupling angles, in radians, for experimental TM mode measurements
    private double[] modeNumbersTM = null;      // mode numbers for experimental TM mode measurements
    private double[] effectiveRefractiveIndicesTM = null;   // effective refractive indices for TM mode measurements
    private double[] effectiveErrorsTM = null;  // propagated errors for effective refractive indices for TM mode measurements
    private int numberOfTMmeasurementsGrating = 0; // number of TM mode thickness measurement
    private boolean setMeasurementsTMgrating = false;    // = true when TM mode measurements entered
    private boolean setTMerrors = false;        // = true if TM mode errors are set

    private int numberOfMeasurementsGrating = 0;  // total number of thickness measurements entered
    private boolean setMeasurementsGrating = false;    // = true when TE and/or TM mode measurements entered

    private double gratingPitch = 0.0D;         // Grating pitch, metres
    private boolean setGratingPitch = false;    // = true when grating pitch entered
    private int[] gratingOrderTE = null;        // Grating order for each TE mode measurement, default value = 1
    private boolean setGratingOrderTE = false;  // = true when TE mode grating orders entered
    private int[] gratingOrderTM = null;        // Grating order for each TM mode measurement, default value = 1
    private boolean setGratingOrderTM = false;  // = true when TM mode grating orders entered

    private double superstrateRI = 0.0;         // Superstrate refractive index
    private boolean setSuperstrateRI = false;   // equals true when superstrate refractice index entered

    // CONSTRUCTOR
    public GratingCoupler(){
    }


    // GRATING
    // Enter pitch
    public void setGratingPitch(double pitch){
        this.gratingPitch = pitch;
        this.setGratingPitch = true;
        if(this.setMeasurementsGrating && super.setWavelength)this.calcEffectiveRefractiveIndices();
    }

    // Enter grating orders for TE measurements
    public void setSetTEmodeGratingOrder(int[] order){
        this.gratingOrderTE = order;
        int m = order.length;
        if(this.setMeasurementsTEgrating)if(m!=this.numberOfTEmeasurementsGrating)throw new IllegalArgumentException("Number of grating orders entered, " + m + ", is not equal to the number of measurements previously entered, " + this.numberOfTEmeasurementsGrating);
        if(this.setMeasurementsGrating && this.setGratingPitch && super.setWavelength)this.calcEffectiveRefractiveIndices();
    }

    // Enter grating orders for TM measurements
    public void setSetTMmodeGratingOrder(int[] order){
        this.gratingOrderTM = order;
        int m = order.length;
        if(this.setMeasurementsTMgrating)if(m!=this.numberOfTMmeasurementsGrating)throw new IllegalArgumentException("Number of grating orders entered, " + m + ", is not equal to the number of measurements previously entered, " + this.numberOfTEmeasurementsGrating);
        if(this.setMeasurementsGrating && this.setGratingPitch && super.setWavelength)this.calcEffectiveRefractiveIndices();
    }

    // Enter superstrate refractive index (overrides superclass method)
    public void setSuperstrateRefractiveIndex(double index){
        if(this.calcEffectiveDone)this.clearData();
        this.superstrateRI = index;
        super.superstrateRefractiveIndex = index;
        this.setSuperstrateRI = true;
        if(this.setMeasurementsGrating && this.setGratingPitch && super.setWavelength)this.calcEffectiveRefractiveIndices();
    }


    // Return analyte solution refractive index
    public double getAnalyteSolutionRefractiveIndex(){
        return super.getSuperstrateRefractiveIndex();
    }

    // Return analyte solution refractive index
    public double getStandardDeviationAnalyteSolutionRefractiveIndex(){
        return super.getStandardDeviationSuperstrateRefractiveIndex();
    }


    // THICKNESS (metres), COUPLING ANGLE (degrees) AND MODE NUMBER DATA
    // Enter TE mode data for a single measurement without error
    public void enterTEmodeData(double thickness, double angle, double modeNumber){
        if(this.setMeasurementsTEgrating){
            if(this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsGrating + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.thicknessesTE[i];
            hold[this.numberOfTEmeasurementsGrating] = thickness;
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.anglesDegTE[i];
            hold[this.numberOfTEmeasurementsGrating] = angle;
            this.anglesDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsDegTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsDegTE[i] = 0.0D;
                this.errorsRadTE[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.modeNumbersTE[i];
            hold[this.numberOfTEmeasurementsGrating] = modeNumber;
            this.numberOfTEmeasurementsGrating = nNew;
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
            this.numberOfTEmeasurementsGrating = 1;
        }
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTEgrating = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTEmodeEffectiveRefractiveIndices();
    }



    // Enter TM mode data for a single measurement without error
    public void enterTMmodeData(double thickness, double angle, double modeNumber){
        if(this.setMeasurementsTMgrating){
            if(this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsGrating + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.thicknessesTM[i];
            hold[this.numberOfTMmeasurementsGrating] = thickness;
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.anglesDegTM[i];
            hold[this.numberOfTMmeasurementsGrating] = angle;
            this.anglesDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsDegTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsDegTM[i] = 0.0D;
                this.errorsRadTM[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.modeNumbersTM[i];
            hold[this.numberOfTMmeasurementsGrating] = modeNumber;
            this.numberOfTMmeasurementsGrating = nNew;
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
            this.numberOfTMmeasurementsGrating = 1;
        }
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTMgrating = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTMmodeEffectiveRefractiveIndices();

    }

    // Enter TE mode data for a range of measurements without errors
    public void enterTEmodeData(double[] thicknesses, double[] angles, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTEgrating){
            if(this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsGrating + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.thicknessesTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = thicknesses[i];
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.anglesDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = angles[i];
            this.anglesDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsDegTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsDegTE[i] = 0.0D;
                this.errorsRadTE[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.modeNumbersTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = modeNumbers[i];
            this.numberOfTEmeasurementsGrating = nNew;
        }
        else{
            this.numberOfTEmeasurementsGrating = o;
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
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTEgrating = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTEmodeEffectiveRefractiveIndices();

    }

    // Enter TM mode data for a range of measurements without errors
    public void enterTMmodeData(double[] thicknesses, double[] angles, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTMgrating){
            if(this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsGrating + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.thicknessesTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = thicknesses[i];
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.anglesDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = angles[i];
            this.anglesDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsDegTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsDegTM[i] = 0.0D;
                this.errorsRadTM[i] = 0.0D;
            }
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.modeNumbersTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = modeNumbers[i];
            this.numberOfTMmeasurementsGrating = nNew;
        }
        else{
            this.numberOfTMmeasurementsGrating = o;
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
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTMgrating = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Enter TE mode data for a single measurement with error
    public void enterTEmodeData(double thickness, double angle, double error, double modeNumber){
        if(this.setMeasurementsTEgrating){
            if(!this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsGrating + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.thicknessesTE[i];
            hold[this.numberOfTEmeasurementsGrating] = thickness;
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.anglesDegTE[i];
            hold[this.numberOfTEmeasurementsGrating] = angle;
            this.anglesDegTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.errorsDegTE[i];
            hold[this.numberOfTEmeasurementsGrating] = error;
            this.errorsDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsRadTE[i] = Math.toRadians(this.errorsDegTE[i]);
            }
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.modeNumbersTE[i];
            hold[this.numberOfTEmeasurementsGrating] = modeNumber;
            this.numberOfTEmeasurementsGrating = nNew;
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
            this.numberOfTEmeasurementsGrating = 1;
        }
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTEgrating = true;
        this.setTEerrors = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTEmodeEffectiveRefractiveIndices();
    }



    // Enter TM mode data for a single measurement with error
    public void enterTMmodeData(double thickness, double angle, double error, double modeNumber){
        if(this.setMeasurementsTMgrating){
            if(!this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsGrating + 1;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.thicknessesTM[i];
            hold[this.numberOfTMmeasurementsGrating] = thickness;
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.anglesDegTM[i];
            hold[this.numberOfTMmeasurementsGrating] = angle;
            this.anglesDegTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.errorsDegTM[i];
            hold[this.numberOfTMmeasurementsGrating] = error;
            this.errorsDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsRadTM[i] = Math.toRadians(this.errorsDegTM[i]);
            }
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.modeNumbersTM[i];
            hold[this.numberOfTMmeasurementsGrating] = modeNumber;
            this.numberOfTMmeasurementsGrating = nNew;
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
            this.numberOfTMmeasurementsGrating = 1;
        }
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTMgrating = true;
        this.setTMerrors = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Enter TE mode data for a range of measurements with errors
    public void enterTEmodeData(double[] thicknesses, double[] angles, double[] errors, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTEgrating){
            if(!this.setTEerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTEmeasurementsGrating + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.thicknessesTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = thicknesses[i];
            this.thicknessesTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.anglesDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = angles[i];
            this.anglesDegTE = hold;
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.errorsDegTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = errors[i];
            this.errorsDegTE = hold;
            this.anglesRadTE = hold;
            this.errorsRadTE = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTE[i] = Math.toRadians(this.anglesDegTE[i]);
                this.errorsRadTE[i] = Math.toRadians(this.errorsDegTE[i]);
            }
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++)hold[i] = this.modeNumbersTE[i];
            for(int i=0; i<o; i++)hold[this.numberOfTEmeasurementsGrating+i] = modeNumbers[i];
            this.numberOfTEmeasurementsGrating = nNew;
        }
        else{
            this.numberOfTEmeasurementsGrating = o;
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
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTEgrating = true;
        this.setTEerrors = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTEmodeEffectiveRefractiveIndices();
    }

    // Enter TM mode data for a range of measurements without errors
    public void enterTMmodeData(double[] thicknesses, double[] angles, double[] errors, double[] modeNumbers){
        int o = thicknesses.length;
        int n = angles.length;
        if(n!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of coupling angles, " + n);
        int m = modeNumbers.length;
        if(m!=o)throw new IllegalArgumentException("number of thicknesses, " + o + ", does not equal the number of mode numbers, " + m);

        if(this.setMeasurementsTMgrating){
            if(!this.setTMerrors)throw new IllegalArgumentException("All Entered data must either all have associated errors entered or all have no associated errors entered");
            int nNew = this.numberOfTMmeasurementsGrating + o;
            double[] hold = new double[nNew];
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.thicknessesTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = thicknesses[i];
            this.thicknessesTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.anglesDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = angles[i];
            this.anglesDegTM = hold;
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.errorsDegTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = errors[i];
            this.errorsDegTM = hold;
            this.anglesRadTM = hold;
            this.errorsRadTM = hold;
            for(int i=0; i<nNew; i++){
                this.anglesRadTM[i] = Math.toRadians(this.anglesDegTM[i]);
                this.errorsRadTM[i] = Math.toRadians(this.errorsDegTM[i]);
            }
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++)hold[i] = this.modeNumbersTM[i];
            for(int i=0; i<o; i++)hold[this.numberOfTMmeasurementsGrating+i] = modeNumbers[i];
            this.numberOfTMmeasurementsGrating = nNew;
        }
        else{
            this.numberOfTMmeasurementsGrating = o;
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
        this.numberOfMeasurementsGrating = this.numberOfTEmeasurementsGrating + this.numberOfTMmeasurementsGrating;
        this.setMeasurementsTMgrating = true;
        this.setTMerrors = true;
        this.setMeasurementsGrating = true;
        if(this.setGratingPitch && super.setWavelength)this.calcTMmodeEffectiveRefractiveIndices();
    }


    // Clear entered thickness, effective refractive index and mode number data
    //  so new dat may be entered without it being appended to the existing data
    public void clearData(){
        this.numberOfTEmeasurementsGrating = 0;
        this.setMeasurementsTEgrating = false;

        this.numberOfTMmeasurementsGrating = 0;
        this.setMeasurementsTMgrating = false;

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
        if(this.setMeasurementsTEgrating)this.calcTEmodeEffectiveRefractiveIndices();
        if(this.setMeasurementsTMgrating)this.calcTMmodeEffectiveRefractiveIndices();
    }

    // Calculate TE mode effective refractive indices
    public void calcTEmodeEffectiveRefractiveIndices(){
        this.effectiveRefractiveIndicesTE = new double[this.numberOfTEmeasurementsGrating];
        this.effectiveErrorsTE = new double[this.numberOfTEmeasurementsGrating];

        if(!this.setSuperstrateRI){
            this.superstrateRI = RefractiveIndex.air(super.wavelength);
            super.superstrateRefractiveIndex = RefractiveIndex.air(super.wavelength);
        }

        if(this.setTEerrors){
            ErrorProp superRI = new ErrorProp(super.superstrateRefractiveIndex, 0.0D);
            ErrorProp pitch = new ErrorProp(this.gratingPitch, 0.0D);
            ErrorProp lambda = new ErrorProp(super.wavelength, 0.0D);

           for(int i=0; i<this.numberOfTEmeasurementsGrating; i++){
                ErrorProp theta = new ErrorProp(this.anglesRadTM[i], this.errorsRadTM[i]);
                ErrorProp order = new ErrorProp((double)this.gratingOrderTE[i], 0.0D);
                ErrorProp calc = ErrorProp.sin(theta);
                calc = calc.times(superRI);
                calc = calc.plus(lambda.times(order).over(pitch));
                this.effectiveRefractiveIndicesTE[i] = calc.getValue();
                this.effectiveErrorsTE[i] = calc.getError();
           }
           super.enterTEmodeData(this.thicknessesTE, this.effectiveRefractiveIndicesTE, this.effectiveErrorsTE, this.modeNumbersTE);
        }
        else{
            for(int i=0; i<this.numberOfTEmeasurementsGrating; i++){
                this.effectiveRefractiveIndicesTE[i] = this.superstrateRI*Math.sin(this.anglesRadTE[i]) + super.wavelength*this.gratingOrderTE[i]/this.gratingPitch;
            }
            super.enterTEmodeData(this.thicknessesTE, this.effectiveRefractiveIndicesTE, this.modeNumbersTE);
        }
        this.calcEffectiveDone = true;
    }

    // Calculate TM mode effective refractive indices
     public void calcTMmodeEffectiveRefractiveIndices(){
        this.effectiveRefractiveIndicesTM = new double[this.numberOfTMmeasurementsGrating];
        this.effectiveErrorsTM = new double[this.numberOfTMmeasurementsGrating];

        if(!this.setSuperstrateRI){
            this.superstrateRI = RefractiveIndex.air(super.wavelength);
            super.superstrateRefractiveIndex = RefractiveIndex.air(super.wavelength);
        }

        if(this.setTMerrors){
            ErrorProp superRI = new ErrorProp(super.superstrateRefractiveIndex, 0.0D);
            ErrorProp pitch = new ErrorProp(this.gratingPitch, 0.0D);
            ErrorProp lambda = new ErrorProp(super.wavelength, 0.0D);

            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++){
                ErrorProp theta = new ErrorProp(this.anglesRadTM[i], this.errorsRadTM[i]);
                ErrorProp order = new ErrorProp((double)this.gratingOrderTM[i], 0.0D);
                ErrorProp calc = ErrorProp.sin(theta);
                calc = calc.times(superRI);
                calc = calc.plus(lambda.times(order).over(pitch));
                this.effectiveRefractiveIndicesTM[i] = calc.getValue();
                this.effectiveErrorsTM[i] = calc.getError();
           }
           super.enterTMmodeData(this.thicknessesTM, this.effectiveRefractiveIndicesTM, this.effectiveErrorsTM, this.modeNumbersTM);
        }
        else{
            for(int i=0; i<this.numberOfTMmeasurementsGrating; i++){
                this.effectiveRefractiveIndicesTM[i] = this.superstrateRI*Math.sin(this.anglesRadTM[i]) + super.wavelength*this.gratingOrderTM[i]/this.gratingPitch;
            }
            super.enterTMmodeData(this.thicknessesTM, this.effectiveRefractiveIndicesTM, this.modeNumbersTM);
        }
        this.calcEffectiveDone = true;
    }
}