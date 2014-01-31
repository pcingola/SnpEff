/*      Class CoaxialLine
*
*       Models a coaxial transmission line
*       This is a subclass of the superclass TransmissionLine
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    July 2007
*       UPDATE:  7 April 2008
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/CoaxialLine.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/TransmissionLine.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*       Copyright (c) 2007 - 2008    Michael Thomas Flanagan
*
*       PERMISSION TO COPY:
*       Permission to use, copy and modify this software and its documentation for
*       NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*       to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*       Dr Michael Thomas Flanagan makes no representations about the suitability
*       or fitness of the software for any or for a particular purpose.
*       Michael Thomas Flanagan shall not be liable for any damages suffered
*       as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.circuits;

import flanagan.complex.Complex;

public class CoaxialLine extends TransmissionLine{

    private double innerRadius = -1.0D;         // inner radius - coaxial centre to outer surface of inner conductor
    private double outerRadius = -1.0D;         // outer radius - coaxial centre to inner surface of outer conductor
    private boolean radiiSet = false;           // = true when both inner and outer radii entered

    private double relativePermittivity = 1.0D; // relative electrical permittivity of the material between the conductors
    private double relativePermeability = 1.0D; // relative magnetic permeability of the material between the conductors


    // CONSTRUCTOR
    // default constructor
    public CoaxialLine(){
        super.title = "Coaxial Line";
    }

    // user provided title
    public CoaxialLine(String title){
        super.title = title;
    }

    // RADII
    // Set inner and outer radii
    // inner: coaxial centre to outer surface of inner conductor
    // outer: coaxial centre to inner surface of outer conductor
    public void setRadii(double innerRadius, double outerRadius){
        if(innerRadius<=0.0D)throw new IllegalArgumentException("The inner radius, " + innerRadius + ", must be greater than zero");
        if(outerRadius<=0.0D)throw new IllegalArgumentException("The outer radius, " + outerRadius + ", must be greater than zero");
        if(innerRadius >= outerRadius)throw new IllegalArgumentException("The inner radius, " + innerRadius + ", must be less than the outer radius, " + outerRadius);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.radiiSet = true;
        this.calculateDistributedCapacitanceAndInductance();
    }

    // Set inner radius - coaxial centre to outer surface of inner conductor
    public void setInnerRadius(double radius){
        if(radius<=0.0D)throw new IllegalArgumentException("The inner radius, " + radius + ", must be greater than zero");
        if(this.outerRadius!=-1.0D && this.outerRadius<=radius)throw new IllegalArgumentException("The inner radius, " + radius + ", must be less than the outer radius, " + this.outerRadius);
        this.innerRadius = radius;
        if(this.outerRadius!=-1.0)this.radiiSet = true;
        if(this.radiiSet)this.calculateDistributedCapacitanceAndInductance();
    }

    // Set outer radius - coaxial centre to inner surface of outer conductor
    public void setOuterRadius(double radius){
        if(radius<=0.0D)throw new IllegalArgumentException("The outer radius, " + radius + ", must be greater than zero");
        if(this.innerRadius!=-1.0D && this.innerRadius>=radius)throw new IllegalArgumentException("The outer radius, " + radius + ", must be greater than the inner radius, " + this.innerRadius);
        this.outerRadius = radius;
        if(this.innerRadius!=-1.0)this.radiiSet = true;
        if(this.radiiSet)this.calculateDistributedCapacitanceAndInductance();
    }

    // PERMITTIVITY
    // Set relative electrical permittivity of the material between the conductors
    public void setRelativePermittivity(double epsilonR){
        this.relativePermittivity = epsilonR;
        if(this.radiiSet)this.calculateDistributedCapacitanceAndInductance();
    }

    // PERMEABILTY
    // Set relative magnetic permeability of the material between the conductors
    public void setRelativePermeability(double muR){
        this.relativePermeability = muR;
        if(this.radiiSet)this.calculateDistributedCapacitanceAndInductance();
    }

    // CALCULATE DISTRIBUTED PARAMETERS
    private void calculateDistributedCapacitanceAndInductance(){
        super.distributedCapacitance = Impedance.coaxialCapacitance(1.0D, this.innerRadius, this.outerRadius, this.relativePermittivity);
        super.distributedInductance = Impedance.coaxialInductance(1.0D, this.innerRadius, this.outerRadius, this.relativePermeability);
    }

    // DEEP COPY
    public CoaxialLine copy(){

        if(this==null){
            return null;
        }
        else{
            CoaxialLine tl = new CoaxialLine();

            tl.innerRadius = this.innerRadius;
            tl.outerRadius = this.outerRadius;
            tl.radiiSet = this.radiiSet;
            tl.relativePermittivity = this.relativePermittivity;
            tl.relativePermeability = this.relativePermeability;

            tl.title = this.title;
            tl.distributedResistance = this.distributedResistance;
            tl.distributedConductance = this.distributedConductance;
            tl.distributedCapacitance = this.distributedCapacitance;
            tl.distributedInductance = this.distributedInductance;

            tl.distributedImpedance = this.distributedImpedance.copy();
            tl.distributedAdmittance = this.distributedAdmittance.copy();
            tl.loadImpedance = this.loadImpedance.copy();

            tl.lineLength = this.lineLength;
            tl.segmentLength = this.segmentLength;
            tl.frequency = this.frequency;
            tl.segmentLength = this.segmentLength;
            tl.omega = this.omega;

            tl.inputVoltage = this.inputVoltage.copy();
            tl.inputCurrent = this.inputCurrent.copy();
            tl.outputVoltage = this.outputVoltage.copy();
            tl.outputCurrent = this.outputCurrent.copy();

            tl.idealWavelength = this.idealWavelength;
            tl.generalWavelength = this.generalWavelength;
            tl.lowLossWavelength = this.lowLossWavelength;

            tl.idealPhaseVelocity = this.idealPhaseVelocity;
            tl.generalPhaseVelocity = this.generalPhaseVelocity;
            tl.lowLossPhaseVelocity = this.lowLossPhaseVelocity;

            tl.idealGroupVelocity = this.idealGroupVelocity;
            tl.generalGroupVelocity = this.generalGroupVelocity;
            tl.lowLossGroupVelocity = this.lowLossGroupVelocity;
            tl.delta = this.delta;

            tl.idealAttenuationConstant = this.idealAttenuationConstant;
            tl.generalAttenuationConstant = this.generalAttenuationConstant;
            tl.lowLossAttenuationConstant = this.lowLossAttenuationConstant;

            tl.idealPhaseConstant = this.idealPhaseConstant;
            tl.generalPhaseConstant = this.generalPhaseConstant;
            tl.lowLossPhaseConstant = this.lowLossPhaseConstant;

            tl.idealPropagationConstant = this.idealPropagationConstant.copy();
            tl.loadImpedance = this.loadImpedance.copy();
            tl.loadImpedance = this.loadImpedance.copy();
            tl.loadImpedance = this.loadImpedance.copy();

            tl.generalPropagationConstant = this.generalPropagationConstant.copy();
            tl.lowLossPropagationConstant = this.lowLossPropagationConstant.copy();
            tl.idealCharacteristicImpedance = this.idealCharacteristicImpedance.copy();
            tl.idealRealCharacteristicImpedance = this.idealRealCharacteristicImpedance;

            tl.generalCharacteristicImpedance = this.generalCharacteristicImpedance.copy();
            tl.lowLossCharacteristicImpedance = this.lowLossCharacteristicImpedance.copy();
            tl.idealInputImpedance = this.idealInputImpedance.copy();
            tl.generalInputImpedance = this.generalInputImpedance.copy();
            tl.lowLossInputImpedance = this.lowLossInputImpedance.copy();

            tl.idealShortedLineImpedance = this.idealShortedLineImpedance.copy();
            tl.generalShortedLineImpedance = this.generalShortedLineImpedance.copy();
            tl.lowLossShortedLineImpedance = this.lowLossShortedLineImpedance.copy();

            tl.idealOpenLineImpedance = this.idealOpenLineImpedance.copy();
            tl.generalOpenLineImpedance = this.generalOpenLineImpedance.copy();
            tl.lowLossOpenLineImpedance = this.lowLossOpenLineImpedance.copy();

            tl.idealQuarterWaveLineImpedance = this.idealQuarterWaveLineImpedance.copy();
            tl.generalQuarterWaveLineImpedance = this.generalQuarterWaveLineImpedance.copy();
            tl.lowLossQuarterWaveLineImpedance = this.lowLossQuarterWaveLineImpedance.copy();

            tl.idealHalfWaveLineImpedance = this.idealHalfWaveLineImpedance.copy();
            tl.generalHalfWaveLineImpedance = this.generalHalfWaveLineImpedance.copy();
            tl.lowLossHalfWaveLineImpedance = this.lowLossHalfWaveLineImpedance.copy();

            tl.idealRefectionCoefficient = this.idealRefectionCoefficient.copy();
            tl.generalRefectionCoefficient = this.generalRefectionCoefficient.copy();
            tl.lowLossRefectionCoefficient = this.lowLossRefectionCoefficient.copy();

            tl.idealStandingWaveRatio = this.idealStandingWaveRatio;
            tl.generalStandingWaveRatio = this.generalStandingWaveRatio;
            tl.lowLossStandingWaveRatio = this.lowLossStandingWaveRatio;

            tl.idealABCDmatrix = this.idealABCDmatrix.copy();
            tl.generalABCDmatrix = this.generalABCDmatrix.copy();
            tl.lowLossABCDmatrix = this.lowLossABCDmatrix.copy();

            tl.numberOfPoints = this.numberOfPoints;

            return tl;
        }
    }


    // Clone - overrides Java.Object method clone
    public Object clone(){

        Object ret = null;

        if(this!=null){

            CoaxialLine tl = new CoaxialLine();

            tl.innerRadius = this.innerRadius;
            tl.outerRadius = this.outerRadius;
            tl.radiiSet = this.radiiSet;
            tl.relativePermittivity = this.relativePermittivity;
            tl.relativePermeability = this.relativePermeability;

            tl.title = this.title;
            tl.distributedResistance = this.distributedResistance;
            tl.distributedConductance = this.distributedConductance;
            tl.distributedCapacitance = this.distributedCapacitance;
            tl.distributedInductance = this.distributedInductance;

            tl.distributedImpedance = this.distributedImpedance.copy();
            tl.distributedAdmittance = this.distributedAdmittance.copy();
            tl.loadImpedance = this.loadImpedance.copy();

            tl.lineLength = this.lineLength;
            tl.segmentLength = this.segmentLength;
            tl.frequency = this.frequency;
            tl.segmentLength = this.segmentLength;
            tl.omega = this.omega;

            tl.inputVoltage = this.inputVoltage.copy();
            tl.inputCurrent = this.inputCurrent.copy();
            tl.outputVoltage = this.outputVoltage.copy();
            tl.outputCurrent = this.outputCurrent.copy();

            tl.idealWavelength = this.idealWavelength;
            tl.generalWavelength = this.generalWavelength;
            tl.lowLossWavelength = this.lowLossWavelength;

            tl.idealPhaseVelocity = this.idealPhaseVelocity;
            tl.generalPhaseVelocity = this.generalPhaseVelocity;
            tl.lowLossPhaseVelocity = this.lowLossPhaseVelocity;

            tl.idealGroupVelocity = this.idealGroupVelocity;
            tl.generalGroupVelocity = this.generalGroupVelocity;
            tl.lowLossGroupVelocity = this.lowLossGroupVelocity;
            tl.delta = this.delta;

            tl.idealAttenuationConstant = this.idealAttenuationConstant;
            tl.generalAttenuationConstant = this.generalAttenuationConstant;
            tl.lowLossAttenuationConstant = this.lowLossAttenuationConstant;

            tl.idealPhaseConstant = this.idealPhaseConstant;
            tl.generalPhaseConstant = this.generalPhaseConstant;
            tl.lowLossPhaseConstant = this.lowLossPhaseConstant;

            tl.idealPropagationConstant = this.idealPropagationConstant.copy();
            tl.loadImpedance = this.loadImpedance.copy();
            tl.loadImpedance = this.loadImpedance.copy();
            tl.loadImpedance = this.loadImpedance.copy();

            tl.generalPropagationConstant = this.generalPropagationConstant.copy();
            tl.lowLossPropagationConstant = this.lowLossPropagationConstant.copy();
            tl.idealCharacteristicImpedance = this.idealCharacteristicImpedance.copy();
            tl.idealRealCharacteristicImpedance = this.idealRealCharacteristicImpedance;

            tl.generalCharacteristicImpedance = this.generalCharacteristicImpedance.copy();
            tl.lowLossCharacteristicImpedance = this.lowLossCharacteristicImpedance.copy();
            tl.idealInputImpedance = this.idealInputImpedance.copy();
            tl.generalInputImpedance = this.generalInputImpedance.copy();
            tl.lowLossInputImpedance = this.lowLossInputImpedance.copy();

            tl.idealShortedLineImpedance = this.idealShortedLineImpedance.copy();
            tl.generalShortedLineImpedance = this.generalShortedLineImpedance.copy();
            tl.lowLossShortedLineImpedance = this.lowLossShortedLineImpedance.copy();

            tl.idealOpenLineImpedance = this.idealOpenLineImpedance.copy();
            tl.generalOpenLineImpedance = this.generalOpenLineImpedance.copy();
            tl.lowLossOpenLineImpedance = this.lowLossOpenLineImpedance.copy();

            tl.idealQuarterWaveLineImpedance = this.idealQuarterWaveLineImpedance.copy();
            tl.generalQuarterWaveLineImpedance = this.generalQuarterWaveLineImpedance.copy();
            tl.lowLossQuarterWaveLineImpedance = this.lowLossQuarterWaveLineImpedance.copy();

            tl.idealHalfWaveLineImpedance = this.idealHalfWaveLineImpedance.copy();
            tl.generalHalfWaveLineImpedance = this.generalHalfWaveLineImpedance.copy();
            tl.lowLossHalfWaveLineImpedance = this.lowLossHalfWaveLineImpedance.copy();

            tl.idealRefectionCoefficient = this.idealRefectionCoefficient.copy();
            tl.generalRefectionCoefficient = this.generalRefectionCoefficient.copy();
            tl.lowLossRefectionCoefficient = this.lowLossRefectionCoefficient.copy();

            tl.idealStandingWaveRatio = this.idealStandingWaveRatio;
            tl.generalStandingWaveRatio = this.generalStandingWaveRatio;
            tl.lowLossStandingWaveRatio = this.lowLossStandingWaveRatio;

            tl.idealABCDmatrix = this.idealABCDmatrix.copy();
            tl.generalABCDmatrix = this.generalABCDmatrix.copy();
            tl.lowLossABCDmatrix = this.lowLossABCDmatrix.copy();

            tl.numberOfPoints = this.numberOfPoints;

            ret = (Object)tl;
        }
        return ret;
    }
}
