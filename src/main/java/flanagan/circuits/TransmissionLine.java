/*      Class TransmisionLine
*
*       Models a generalised, an ideal and a low loss transmission line
*
*       Superclass for CoaxialLine, TwoWireline and ParallelPlateLine
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    July 2007
*       UPDATE   7 April 2008
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/TransmissionLine.html
*
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
import flanagan.complex.ComplexMatrix;
import flanagan.math.Fmath;
import flanagan.math.Matrix;
import flanagan.plot.PlotGraph;

public class TransmissionLine
{

    protected String title = "Transmission Line";           // instance title

    protected double distributedResistance = 0.0;           // distributed resistance
    protected double distributedConductance = 0.0;          // distributed conductance
    protected double distributedCapacitance = 0.0;          // distributed capacitance
    protected double distributedInductance = 0.0;           // distributed inductance
    protected Complex distributedImpedance = null;          // distributed impedance
    protected Complex distributedAdmittance = null;         // distributed admittance

    protected Complex loadImpedance = Complex.plusInfinity();   // load impedance
                                                                // default - open line

    protected double lineLength = -1.0;                     // line length
    protected double segmentLength = -1.0;                  // length of a segment of the line

    protected double frequency = 0.0;                       // frequency (Hz)
    protected double omega = 0.0;                           // radial frequency

    protected Complex inputVoltage = null;                  // segment input voltage - harmonic excitation
    protected Complex inputCurrent = null;                  // segment input current - harmonic excitation

    protected Complex outputVoltage = null;                 // segment output voltage - harmonic excitation
    protected Complex outputCurrent = null;                 // segment output current - harmonic excitation

    protected double idealWavelength = 0.0;                 // wavelength on an ideal line
    protected double generalWavelength = 0.0;               // wavelength on a general line
    protected double lowLossWavelength = 0.0;               // wavelength on a low loss line

    protected double idealPhaseVelocity = 0.0;              // phase velocity on an ideal line
    protected double generalPhaseVelocity = 0.0;            // phase velocity on a general line
    protected double lowLossPhaseVelocity = 0.0;            // phase velocity on a low loss line

    protected double idealGroupVelocity = 0.0;              // group velocity on an ideal line
    protected double generalGroupVelocity = 0.0;            // group velocity on a general line
    protected double lowLossGroupVelocity = 0.0;            // group velocity on a low loss line
    protected double delta = 1e-3;                          // increment in numerical differentiation

    protected double idealAttenuationConstant = 0.0;        // attenuation constant of an ideal line, alpha
    protected double generalAttenuationConstant = 0.0;      // attenuation constant of a general line, alpha
    protected double lowLossAttenuationConstant = 0.0;      // attenuation constant of a low loss line, alpha

    protected double idealPhaseConstant = 0.0;              // phase constant of an ideal line, beta
    protected double generalPhaseConstant = 0.0;            // phase constant of a general, beta
    protected double lowLossPhaseConstant = 0.0;            // phase constant of a low loss line, beta

    protected Complex idealPropagationConstant = null;      // propagation constant of an ideal line, gamma
    protected Complex generalPropagationConstant = null;    // propagation constant of a general line, gamma
    protected Complex lowLossPropagationConstant = null;    // propagation constant of a low loss line, gamma

    protected Complex idealCharacteristicImpedance = null;      // characteristic impedance, Zo, of an ideal line as Complex
    protected double idealRealCharacteristicImpedance = 0.0;    // characteristic impedance, Zo, of an ideal line as double
    protected Complex generalCharacteristicImpedance = null;    // characteristic impedance, Zo, of a general line
    protected Complex lowLossCharacteristicImpedance = null;    // characteristic impedance, Zo, of a low loss line

    protected Complex idealInputImpedance = null;           // input impedance, Zi, of an ideal line
    protected Complex generalInputImpedance = null;         // input impedance, Zi, of a general line
    protected Complex lowLossInputImpedance = null;         // input impedance, Zi, of a low loss line

    protected Complex idealShortedLineImpedance = null;     // shorted line impedance of an ideal line
    protected Complex generalShortedLineImpedance = null;   // shorted line impedance of a general line
    protected Complex lowLossShortedLineImpedance = null;   // shorted line impedance of a low loss line

    protected Complex idealOpenLineImpedance = null;        // open line impedance of an ideal line
    protected Complex generalOpenLineImpedance = null;      // open line impedance of a general line
    protected Complex lowLossOpenLineImpedance = null;      // open line impedance of a low loss line

    protected Complex idealQuarterWaveLineImpedance = null;   // quarter-wave line impedance of an ideal line
    protected Complex generalQuarterWaveLineImpedance = null; // quarter-waveline impedance of a general line
    protected Complex lowLossQuarterWaveLineImpedance = null; // quarter-wave line impedance of a low loss line

    protected Complex idealHalfWaveLineImpedance = null;    // half-wave line impedance of an ideal line
    protected Complex generalHalfWaveLineImpedance = null;  // half-waveline impedance of a general line
    protected Complex lowLossHalfWaveLineImpedance = null;  // half-wave line impedance of a low loss line

    protected Complex idealRefectionCoefficient = null;     // reflection coefficient of an ideal line, rho
    protected Complex generalRefectionCoefficient = null;   // reflection coefficient of a general line, rho
    protected Complex lowLossRefectionCoefficient = null;   // reflection coefficient of a low loss line, rho

    protected double idealStandingWaveRatio = 0.0;          // standing wave ratio of an ideal line
    protected double generalStandingWaveRatio = 0.0;        // standing wave ratio of a general line
    protected double lowLossStandingWaveRatio = 0.0;        // standing wave ratio of a low loss line

    protected ComplexMatrix idealABCDmatrix = null;         // ABCD matrix of an ideal line, as ComplexMatrix
    protected ComplexMatrix generalABCDmatrix = null;       // ABCD matrix of a general line, as ComplexMatrix
    protected ComplexMatrix lowLossABCDmatrix = null;       // ABCD matrix of a low loss line, as ComplexMatrix

    protected int numberOfPoints = 1000;                    // number of points used in plotting methods


    // CONSTRUCTORS
    // default title
    public TransmissionLine(){
    }

    // user provided title
    public TransmissionLine(String title){
        this.title = title;
    }

    // INSTANCE TITLE
    // reset title
    public void setTitle(String title){
        this.title = title;
    }

    // get title
    public String getTitle(){
        return this.title;
    }

    // FREQUENCY
    // set frequency
    public void setFrequency(double frequency){
        this.frequency = frequency;
        this.omega = this.frequency * 2.0D * Math.PI;
    }

    // get frequency
    public double getFrequency(){
        return this.frequency;
    }

    // get radial frequency
    public double getRadialFrequency(){
        return this.omega;
    }

    // LOAD IMPEDANCE
    // set load resistor
    public void setLoadImpedance(double impedance){
        this.loadImpedance = new Complex(impedance, 0.0D);
    }

    // set complex load impedance
    public void setLoadImpedance(Complex impedance){
        this.loadImpedance = impedance;
    }

    // get complex load impedance
    public Complex getLoadImpedance(){
        return this.loadImpedance;
    }

    // LINE DISTANCES
    // set line length
    public void setLineLength(double length){
        this.lineLength = length;
    }

    // get line length
    public double getLineLength(){
        return this.lineLength;
    }

    // set a distance along the line, z
    public void setSegmentLength(double length){
        this.segmentLength = length;
    }

    // SEGMENT OUTPUT VOLTAGE
    // set a segment output voltage - as phasor
    public void setOutputVoltage(Phasor voltage){
        this.outputVoltage = Phasor.toRectangular(voltage);
    }

    // set a segment output voltage -  as complex
    public void setOutputVoltage(Complex voltage){
        this.outputVoltage = voltage;
    }

   // set a segment output voltage - as magnitude and phase
    public void setOutputVoltage(double magnitude, double phase){
        this.outputVoltage = new Complex();
        this.outputVoltage.polar(magnitude, phase);
    }

    // SEGMENT OUTPUT CURRENT
    // set output current - as phasor
    public void setOutputCurrent(Phasor current){
        this.outputCurrent = Phasor.toRectangular(current);
    }

    // set output current - as complex
    public void setOutputCurrent(Complex current){
        this.outputCurrent = current;
    }

   // set input current - as magnitude and phase
    public void setOutputCurrent(double magnitude, double phase){
        this.outputCurrent = new Complex();
        this.outputCurrent.polar(magnitude, phase);
    }

    // DISTRIBUTED PARAMETERS
    // set distributed resistance
    public void setDistributedResistance(double resistance){
        this.distributedResistance = resistance;
    }

    // set distributed inductance
    public void setDistributedInductance(double inductance){
        this.distributedInductance = inductance;
    }

    // set distributed capacitance
    public void setDistributedCapacitance(double capacitance){
        this.distributedCapacitance = capacitance;
    }

    // set distributed conductance
    public void setDistributedConductance(double conductance){
        this.distributedConductance = conductance;
    }

    // get distributed resistance
    public double getDistributedResistance(){
        return this.distributedResistance;
    }

    // get distributed inductance
    public double getDistributedInductance(){
        return this.distributedInductance;
    }

    // get distributed capacitance
    public double getDistributedCapacitance(){
        return this.distributedCapacitance;
    }

    // get distributed conductance
    public double getDistributedConductance(){
        return this.distributedConductance;
    }

    // get distributed impedance
    public Complex getDistributedImpedance(){
        this.distributedImpedance = new Complex(this.distributedResistance, this.distributedInductance * this.omega);
        return this.distributedImpedance;
    }

    // get distributed admittance
    public Complex getDistributedAdmittance(){
        this.distributedAdmittance = new Complex(this.distributedConductance, this.distributedCapacitance * this.omega);
        return this.distributedAdmittance;
    }

    // WAVELENGTHS
    // get wavelegth on a general line
    public double getWavelength(){
        this.generalWavelength = this.getPhaseVelocity() / this.frequency;
        return this.generalWavelength;
    }

    // get wavelegth on an ideal line
    public double getIdealWavelength(){
        this.idealWavelength = this.getIdealPhaseVelocity() / this.frequency;
        return this.idealWavelength;
    }

    // get wavelegth on a low loss line
    public double getLowLossWavelength(){
        this.lowLossWavelength = this.getLowLossPhaseVelocity() / this.frequency;
        return this.lowLossWavelength;
    }

    // PHASE VELOCITIES
    // get phase velocity on a general line
    public double getPhaseVelocity(){
        this.generalPhaseVelocity = this.omega / this.getPhaseConstant();
        return this.generalPhaseVelocity;
    }

    // get phase velocity of a ideal line
    public double getIdealPhaseVelocity(){
        this.idealPhaseVelocity = this.omega / this.getIdealPhaseConstant();
        return this.idealPhaseVelocity;
    }

    // get phase velocity of an low loss line
    public double getLowLossPhaseVelocity(){
        this.lowLossPhaseVelocity = this.omega / this.getLowLossPhaseConstant();
        return this.lowLossPhaseVelocity;
    }

    // GROUP VELOCITIES
    // get group velocity on a general line
    public double getGroupVelocity(){
        if(this.distributedResistance==0.0D && this.distributedConductance==0.0D){
            this.generalPhaseVelocity = 1.0D/Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        }
        else{
            double omegaStored = this.omega;
            this.omega = omegaStored*(1.0D - this.delta);
            double betaLower = this.getPhaseConstant();
            this.omega = omegaStored*(1.0D + this.delta);
            double betaUpper = this.getPhaseConstant();
            this.omega = omegaStored;
            this.generalPhaseVelocity = 2.0D*this.omega*this.delta/(betaUpper - betaLower);
        }
        return this.generalGroupVelocity;
    }

    // Set increment, delta, used in numerical integration, e.g. og general line group velocity
    // default value = 1e-3
    public void setDelta(double delta){
        this.delta = delta;
    }

    // get group velocity of a ideal line
    public double getIdealGroupVelocity(){
        this.idealGroupVelocity = 1.0D/Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        return this.idealGroupVelocity;
    }

    // get group velocity of an low loss line
    public double getLowLossGroupVelocity(){
        double temp0 = this.omega * this.omega;
        double temp1 = Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        double temp2 = (this.distributedResistance * this.distributedConductance) / (4.0D * temp0 * this.distributedInductance * this.distributedCapacitance);
        double temp3 = (this.distributedConductance * this.distributedConductance) / (8.0D * temp0 * this.distributedCapacitance * this.distributedCapacitance);
        double temp4 = (this.distributedResistance * this.distributedResistance) / (8.0D * temp0 * this.distributedInductance * this.distributedInductance);
        this.lowLossPhaseConstant = 1.0/(temp1 * (1.0D + temp2 - temp3 - temp4));
        return this.lowLossGroupVelocity;
    }

    // ATTENUATION CONSTANTS
    // get attenuation constant, alpha, of a general line
    public double getAttenuationConstant(){
        if(this.distributedResistance==0.0D && this.distributedConductance==0.0D){
            this.generalAttenuationConstant = 0.0D;
        }
        else{
            this.generalAttenuationConstant = Complex.sqrt(this.getDistributedImpedance().times(this.getDistributedAdmittance())).getReal();
        }
        return this.generalAttenuationConstant;
    }

    // get attenuation constant, alpha, of a low loss line
    public double getLowLossAttenuationConstant(){
        double temp1 = Math.sqrt(this.distributedInductance / this.distributedCapacitance);
        double temp2 = this.distributedResistance / (2.0D * temp1);
        double temp3 = (this.distributedConductance * temp1) / 2.0D;
        this.lowLossAttenuationConstant = temp2 + temp3;
        return this.lowLossAttenuationConstant;
    }

    // get attenuation constant, alpha, of an ideal line
    public double getIdealAttenuationConstant(){
        this.idealAttenuationConstant = 0.0D;
        return this.idealAttenuationConstant;
    }

    // PHASE CONSTANTS
    // get phase constant, beta, on a general line
    public double getPhaseConstant(){
        if(this.distributedResistance==0.0D && this.distributedConductance==0.0D){
            this.generalPhaseConstant = this.omega * Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        }
        else{
            this.generalPhaseConstant = Complex.sqrt(this.getDistributedImpedance().times(this.getDistributedAdmittance())).getImag();
        }
        return this.generalPhaseConstant;
    }

    // get phase constant, beta, on a low loss line
    public double getLowLossPhaseConstant(){
        double temp0 = this.omega * this.omega;
        double temp1 = this.omega * Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        double temp2 = (this.distributedResistance * this.distributedConductance) / (4.0D * temp0 * this.distributedInductance * this.distributedCapacitance);
        double temp3 = (this.distributedConductance * this.distributedConductance) / (8.0D * temp0 * this.distributedCapacitance * this.distributedCapacitance);
        double temp4 = (this.distributedResistance * this.distributedResistance) / (8.0D * temp0 * this.distributedInductance * this.distributedInductance);
        this.lowLossPhaseConstant = temp1 * (1.0D - temp2 + temp3 + temp4);
        return this.lowLossPhaseConstant;
    }

    // get phase constant, beta, on an ideal line
    public double getIdealPhaseConstant(){
        this.idealPhaseConstant = this.omega * Math.sqrt(this.distributedInductance * this.distributedCapacitance);
        return this.idealPhaseConstant;
    }

    // PROPAGATION CONSTANTS
    // get propagation constant, gamma, on a general line
    public Complex getPropagationConstant(){
        if(this.distributedResistance==0.0D && this.distributedConductance==0.0D){
            this.generalPropagationConstant = new Complex(0.0D, this.omega * Math.sqrt(this.distributedInductance * this.distributedCapacitance));
        }
        else{
            this.generalPropagationConstant = Complex.sqrt(this.getDistributedImpedance().times(this.getDistributedAdmittance()));
        }
        return this.generalPropagationConstant;
    }

    // get propagation constant, gamma, on a low loss line
    public Complex getLowLossPropagationConstant(){
        this.lowLossPropagationConstant = new Complex(this.getLowLossAttenuationConstant(), this.getLowLossPhaseConstant());
        return this.lowLossPropagationConstant;
    }

    // get propagation constant, gamma, on an ideal line
    public Complex getIdealPropagationConstant(){
        this.idealPropagationConstant = new Complex(0.0D, this.omega * Math.sqrt(this.distributedInductance * this.distributedCapacitance));
        return this.idealPropagationConstant;
    }

    // CHARACTERISTIC IMPEDANCES
    // get characteristic impedance, Zo, of a general line
    public Complex getCharacteristicImpedance(){
        this.generalCharacteristicImpedance = Complex.sqrt(this.getDistributedImpedance().over(this.getDistributedAdmittance()));
        return this.generalCharacteristicImpedance;
    }

    // get characteristic impedance, Zo, of a low loss line
    public Complex getLowLossCharacteristicImpedance(){
        double temp0 = this.omega * this.omega;
        double temp1 = Math.sqrt(this.distributedInductance / this.distributedCapacitance);
        double temp2 = (this.distributedResistance * this.distributedResistance) / (8.0D * temp0 * this.distributedInductance * this.distributedInductance);
        double temp3 = (this.distributedConductance * this.distributedConductance) / (8.0D * temp0 * this.distributedCapacitance * this.distributedCapacitance);
        double temp4 = (this.distributedResistance * this.distributedConductance) / (4.0D * temp0 * this.distributedInductance * this.distributedCapacitance);
        double temp5 = this.distributedConductance / (2D * this.omega * this.distributedCapacitance);
        double temp6 = this.distributedResistance / (2D * this.omega * this.distributedInductance);
        this.lowLossCharacteristicImpedance = new Complex(temp1 * (1.0D + temp2 - temp3 + temp4), temp1 * (temp5 - temp6));
        return this.lowLossCharacteristicImpedance;
    }

    // get characteristic impedance, Zo, of an ideal line returned as Complex
    public Complex getIdealCharacteristicImpedance(){
        this.idealRealCharacteristicImpedance = Math.sqrt(this.distributedInductance / this.distributedCapacitance);
        this.idealCharacteristicImpedance = new Complex(this.idealRealCharacteristicImpedance, 0.0D);
        return this.idealCharacteristicImpedance;
    }

    // get characteristic impedance, Zo, of an ideal line returned as double
    public double getIdealCharacteristicImpedanceAsReal(){
        this.idealRealCharacteristicImpedance = Math.sqrt(this.distributedInductance / this.distributedCapacitance);
        this.idealCharacteristicImpedance = new Complex(this.idealRealCharacteristicImpedance, 0.0D);
        return this.idealRealCharacteristicImpedance;
    }

    // INPUT IMPEDANCES
    // get input impedance, Zo, of a general line
    public Complex getInputImpedance(){
        Complex gamma = this.getPropagationConstant();
        Complex zed0 = this.getCharacteristicImpedance();
        Complex temp0 = Complex.cosh(gamma.times(this.lineLength));
        Complex temp1 = Complex.sinh(gamma.times(this.lineLength));
        Complex temp2 = temp0.times(this.loadImpedance);
        Complex temp3 = temp1.times(zed0);
        Complex temp4 = temp0.times(zed0);
        Complex temp5 = temp1.times(this.loadImpedance);
        Complex temp6 = ( temp2.plus(temp3) ).over( temp4.plus(temp5) );
        this.generalInputImpedance = zed0.times(temp6);
        return this.generalInputImpedance;
    }

    // get input impedance, Zo, of a low loss line
    public Complex getLowLossInputImpedance(){
        Complex gamma = this.getLowLossPropagationConstant();
        Complex zed0 = this.getLowLossCharacteristicImpedance();
        Complex temp0 = Complex.cosh(gamma.times(this.lineLength));
        Complex temp1 = Complex.sinh(gamma.times(this.lineLength));
        Complex temp2 = temp0.times(this.loadImpedance);
        Complex temp3 = temp1.times(zed0);
        Complex temp4 = temp0.times(zed0);
        Complex temp5 = temp1.times(this.loadImpedance);
        Complex temp6 = ( temp2.plus(temp3) ).over( temp4.plus(temp5) );
        this.lowLossInputImpedance = zed0.times(temp6);
        return this.lowLossInputImpedance;
    }

    // get input impedance, Zo, of an ideal line
    public Complex getIdealInputImpedance(){
        double beta = this.getIdealPhaseConstant();
        double zed0 = this.getIdealCharacteristicImpedanceAsReal();
        double temp0 = Math.cos(beta*this.lineLength);
        double temp1 = Math.sin(beta*this.lineLength);
        Complex temp2 = ( new Complex(0.0D, temp1*zed0) ).plus(this.loadImpedance.times(temp0));
        Complex temp3 = ( new Complex(temp0*zed0, 0.0D) ).plus( Complex.plusJay().times( this.loadImpedance.times(temp1) ) );
        Complex temp4 = temp2.over(temp3);
        this.idealInputImpedance = temp4.times(zed0);
        return this.idealInputImpedance;
    }

    // SHORTED LINE IMPEDANCES
    // get shorted line impedance of a general line
    public Complex getShortedLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        this.generalShortedLineImpedance = this.getCharacteristicImpedance().times(Complex.tanh(this.getPropagationConstant().times(this.lineLength)));
        return this.generalShortedLineImpedance;
    }

    // get shorted line impedance of a low loss line
    public Complex getLowLossShortedLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        double temp0 = this.getLowLossAttenuationConstant() * this.lineLength;
        double temp1 = Math.cos(this.getLowLossPhaseConstant() * this.lineLength);
        double temp2 = Math.sin(this.getLowLossPhaseConstant() * this.lineLength);
        Complex temp3 = new Complex(temp0 * temp1, temp2);
        Complex temp4 = new Complex(temp1, temp0 * temp2);
        this.lowLossShortedLineImpedance = temp3.over(temp4);
        return this.lowLossShortedLineImpedance;
    }

    // get shorted line impedance of an ideal line
    public Complex getIdealShortedLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        this.idealShortedLineImpedance = new Complex(0.0D, this.getIdealCharacteristicImpedanceAsReal() * Math.tan(this.getIdealPhaseConstant() * this.lineLength));
        return this.idealShortedLineImpedance;
    }

    // OPEN LINE IMPEDANCES
    // get open line impedance of a general line
    public Complex getOpenLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        this.generalShortedLineImpedance = this.getCharacteristicImpedance().times(Complex.coth(this.getPropagationConstant().times(this.lineLength)));
        return this.generalShortedLineImpedance;
    }

    // get open line impedance of a low loss line
    public Complex getLowLossOpenLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        double temp0 = this.getLowLossAttenuationConstant() * this.lineLength;
        double temp1 = Math.cos(this.getLowLossPhaseConstant() * this.lineLength);
        double temp2 = Math.sin(this.getLowLossPhaseConstant() * this.lineLength);
        Complex temp3 = new Complex(temp1, temp0 * temp2);
        Complex temp4 = new Complex(temp0 * temp1, temp2);
        this.lowLossShortedLineImpedance = temp3.over(temp4);
        return this.lowLossShortedLineImpedance;
    }

    // get open line impedance of an ideal line
    public Complex getIdealOpenLineImpedance(){
        if(this.lineLength==-1)throw new IllegalArgumentException("No line length as been entered");
        this.idealShortedLineImpedance = new Complex(0.0D, -this.getIdealCharacteristicImpedanceAsReal() * Fmath.cot(this.getIdealPhaseConstant() * this.lineLength));
        return this.idealShortedLineImpedance;
    }

    // QUARTER-WAVE LINE IMPEDANCES
    // get quarter-wave line impedance of a general line
    public Complex getQuarterWaveLineImpedance(){
        Complex alpha = new Complex(this.getAttenuationConstant(), 0.0D);
        Complex zed0 = this.getCharacteristicImpedance();
        Complex temp0 = Complex.sinh(alpha.times(this.lineLength));
        Complex temp1 = Complex.cosh(alpha.times(this.lineLength));
        Complex temp2 = temp0.times(this.loadImpedance);
        Complex temp3 = temp1.times(zed0);
        Complex temp4 = temp0.times(zed0);
        Complex temp5 = temp1.times(this.loadImpedance);
        Complex temp6 = ( temp2.plus(temp3) ).over( temp4.plus(temp5) );
        this.generalQuarterWaveLineImpedance = zed0.times(temp6);
        return this.generalQuarterWaveLineImpedance;
    }

    // get quarter-wave line impedance of a low loss line
    public Complex getLowLossQuarterWaveLineImpedance(){
        Complex alpha = new Complex(this.getLowLossAttenuationConstant(), 0.0D);
        Complex zed0 = this.getLowLossCharacteristicImpedance();
        Complex temp0 = alpha.times(this.lineLength);
        Complex temp1 = zed0.plus(this.loadImpedance.times(temp0));
        Complex temp2 = this.loadImpedance.plus(zed0.times(temp0));
        Complex temp3 = temp1.over(temp2);
        this.lowLossQuarterWaveLineImpedance = zed0.times(temp3);
        return this.lowLossQuarterWaveLineImpedance;
    }

    // get quarter-wave line impedance of an ideal line
    public Complex getIdealQuarterWaveLineImpedance(){
        Complex zed02 = new Complex(Fmath.square(this.getIdealCharacteristicImpedanceAsReal()), 0.0D);
        this.idealQuarterWaveLineImpedance = zed02.over(this.loadImpedance);
        return this.idealQuarterWaveLineImpedance;
    }

    // HALF-WAVE LINE IMPEDANCES
    // get half-wave line impedance of a general line
    public Complex getHalfWaveLineImpedance(){
        Complex alpha = new Complex(this.getAttenuationConstant(), 0.0D);
        Complex zed0 = this.getCharacteristicImpedance();
        Complex temp0 = Complex.cosh(alpha.times(this.lineLength));
        Complex temp1 = Complex.sinh(alpha.times(this.lineLength));
        Complex temp2 = temp0.times(this.loadImpedance);
        Complex temp3 = temp1.times(zed0);
        Complex temp4 = temp0.times(zed0);
        Complex temp5 = temp1.times(this.loadImpedance);
        Complex temp6 = ( temp2.plus(temp3) ).over( temp4.plus(temp5) );
        this.generalHalfWaveLineImpedance = zed0.times(temp6);
        return this.generalHalfWaveLineImpedance;
    }

    // get half-wave line impedance of a low loss line
    public Complex getLowLossHalfWaveLineImpedance(){
        Complex alpha = new Complex(this.getLowLossAttenuationConstant(), 0.0D);
        Complex zed0 = this.getLowLossCharacteristicImpedance();
        Complex temp0 = alpha.times(this.lineLength);
        Complex temp1 = this.loadImpedance.plus(zed0.times(temp0));
        Complex temp2 = zed0.plus(this.loadImpedance.times(temp0));
        Complex temp3 = temp1.over(temp2);
        this.lowLossHalfWaveLineImpedance = zed0.times(temp3);
        return this.lowLossHalfWaveLineImpedance;
    }

    // get half-wave line impedance of an ideal line
    public Complex getIdealHalfWaveLineImpedance(){
        this.idealHalfWaveLineImpedance = this.loadImpedance;
        return this.idealHalfWaveLineImpedance;
    }

    // REFLECTION COEFFICIENTS
    // get the refection coefficient, rho, of a general line
    public Complex getRefectionCoefficient(){
        Complex complex1 = this.loadImpedance.minus(this.getCharacteristicImpedance());
        Complex complex2 = this.loadImpedance.plus(this.getCharacteristicImpedance());
        this.generalRefectionCoefficient = complex1.over(complex2);
        return this.generalRefectionCoefficient;
    }

    // get the refection coefficient, rho, of a low loss line
    public Complex getLowLossRefectionCoefficient(){
        Complex complex1 = this.loadImpedance.minus(this.getLowLossCharacteristicImpedance());
        Complex complex2 = this.loadImpedance.plus(this.getLowLossCharacteristicImpedance());
        this.lowLossRefectionCoefficient = complex1.over(complex2);
        return this.lowLossRefectionCoefficient;
    }

    // get the refection coefficient, rho, of an ideal line
    public Complex getIdealRefectionCoefficient(){
        Complex complex1 = this.loadImpedance.minus(this.getIdealCharacteristicImpedance());
        Complex complex2 = this.loadImpedance.plus(this.getIdealCharacteristicImpedance());
        this.idealRefectionCoefficient = complex1.over(complex2);
        return this.idealRefectionCoefficient;
    }

    // STANDING WAVE RATIOS
    // get the standing wave ratio of a general line
    public double getStandingWaveRatio(){
        double rho = this.getRefectionCoefficient().abs();
        this.generalStandingWaveRatio = (1.0D + rho) / (1.0D - rho);
        return this.generalStandingWaveRatio;
    }

    // get the standing wave ratio of a low loss line
    public double getLowLossStandingWaveRatio(){
        double rho = this.getLowLossRefectionCoefficient().abs();
        this.lowLossStandingWaveRatio = (1.0D + rho) / (1.0D - rho);
        return this.lowLossStandingWaveRatio;
    }

    // get the standing wave ratio of an ideal line
    public double getIdealStandingWaveRatio(){
        double rho = this.getIdealRefectionCoefficient().abs();
        this.idealStandingWaveRatio = (1.0D + rho) / (1.0D - rho);
        return this.idealStandingWaveRatio;
    }

    // ABCD MATRIX
    // calculate the ABCD matrix - general line
    public ComplexMatrix getABCDmatrix(){
        if(this.segmentLength==-1)throw new IllegalArgumentException("No distance along the line as been entered");
        if(this.distributedResistance==0.0D && this.distributedConductance==0.0D){
            this.generalABCDmatrix = this.getIdealABCDmatrix();
        }
        else{
            this.generalABCDmatrix = new ComplexMatrix(2,2);
            Complex gammal = this.getPropagationConstant().times(this.segmentLength);
            Complex zed0 = this.getCharacteristicImpedance();
            this.generalABCDmatrix.setElement(0, 0, Complex.cosh(gammal));
            this.generalABCDmatrix.setElement(0, 1, Complex.sinh(gammal).times(zed0));
            this.generalABCDmatrix.setElement(1, 0, Complex.sinh(gammal).over(zed0));
            this.generalABCDmatrix.setElement(1, 1, Complex.cosh(gammal));
        }
        return this.generalABCDmatrix;
    }

    // calculate the ABCD matrix - ideal line
    public ComplexMatrix getIdealABCDmatrix(){
        if(this.segmentLength==-1)throw new IllegalArgumentException("No distance along the line as been entered");

        this.idealABCDmatrix = new ComplexMatrix(2,2);
        double betal = this.getIdealPhaseConstant()*this.segmentLength;
        double zed0 = this.getIdealCharacteristicImpedanceAsReal();
        this.idealABCDmatrix.setElement(0, 0, new Complex(Math.cos(betal), 0.0D));
        this.idealABCDmatrix.setElement(0, 1, new Complex(0.0D, Math.sin(betal)*zed0));
        this.idealABCDmatrix.setElement(1, 0, new Complex(0.0D, Math.sin(betal)/zed0));
        this.idealABCDmatrix.setElement(1, 1, new Complex(Math.cos(betal), 0.0D));

        return this.idealABCDmatrix;
    }

    // calculate the ABCD matrix - low loss line
    public ComplexMatrix getLowLossABCDmatrix(){
        if(this.segmentLength==-1)throw new IllegalArgumentException("No distance along the line as been entered");

        this.lowLossABCDmatrix = new ComplexMatrix(2,2);
        Complex gammal = this.getLowLossPropagationConstant().times(this.segmentLength);
        Complex zed0 = this.getLowLossCharacteristicImpedance();
        this.lowLossABCDmatrix.setElement(0, 0, Complex.cosh(gammal));
        this.lowLossABCDmatrix.setElement(0, 1, Complex.sinh(gammal).times(zed0));
        this.lowLossABCDmatrix.setElement(1, 0, Complex.sinh(gammal).over(zed0));
        this.lowLossABCDmatrix.setElement(1, 1, Complex.cosh(gammal));
        return this.lowLossABCDmatrix;
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as Complex
    public Complex[] voltageAndCurrentAsComplex(double segLen ){
        this.segmentLength = segLen;
        return voltageAndCurrentAsComplex();
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as Complex
    // preset segment length
    public Complex[] voltageAndCurrentAsComplex(){
        Complex[] outputVector = {this.outputVoltage, this.outputCurrent};
        ComplexMatrix abcdMatrix = this.getABCDmatrix();
        Complex[] inputVector = abcdMatrix.solveLinearSet(outputVector);
        this.inputVoltage = inputVector[0];
        this.inputCurrent = inputVector[1];
        return inputVector;
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as phasor
    public Phasor[] voltageAndCurrentAsPhasor(double segLen){
        this.segmentLength = segLen;
        Complex[] outputVector = {this.outputVoltage, this.outputCurrent};
        ComplexMatrix abcdMatrix = this.getABCDmatrix();
        Complex[] inputVector = abcdMatrix.solveLinearSet(outputVector);
        this.inputVoltage = inputVector[0];
        this.inputCurrent = inputVector[1];
        Phasor[] input = {Phasor.toPhasor(this.inputVoltage), Phasor.toPhasor(this.inputCurrent)};
        return input;
    }


    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as Phasor
    // preset segment length
    public Phasor[] voltageAndCurrentAsPhasor(){
        Complex[] outputVector = {this.outputVoltage, this.outputCurrent};
        ComplexMatrix abcdMatrix = this.getABCDmatrix();
        Complex[] inputVector = abcdMatrix.solveLinearSet(outputVector);
        this.inputVoltage = inputVector[0];
        this.inputCurrent = inputVector[1];
        Phasor[] input = {Phasor.toPhasor(this.inputVoltage), Phasor.toPhasor(this.inputCurrent)};
        return input;
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as real, i.e. Magnitude.cos(phase)
    public double[] voltageAndCurrentAsReal(){
        Complex[] outputVector = {this.outputVoltage, this.outputCurrent};
        ComplexMatrix abcdMatrix = this.getABCDmatrix();
        Complex[] inputVector = abcdMatrix.solveLinearSet(outputVector);

        double[] input = {inputVector[0].abs()*Math.cos(inputVector[0].arg()), inputVector[1].abs()*Math.cos(inputVector[1].arg())};
        return input;
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as real, i.e. Magnitude.cos(phase)
    // preset segment length
    public double[] voltageAndCurrentAsReal(double segLen){
        this.segmentLength = segLen;
        return this.voltageAndCurrentAsReal();
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as magnitude and phase
    public double[] voltageAndCurrentAsMagnitudeAndPhase(){
        Complex[] outputVector = {this.outputVoltage, this.outputCurrent};
        ComplexMatrix abcdMatrix = this.getABCDmatrix();
        Complex[] inputVector = abcdMatrix.solveLinearSet(outputVector);

        double[] input = {inputVector[0].abs(), inputVector[0].arg(), inputVector[1].abs(), inputVector[1].arg()};
        return input;
    }

    // Voltage and current at start of a line segment of length segLen along the line
    // given voltage and current at the start of the segment
    // output as magnitude and phase
    // preset segment length
    public double[] voltageAndCurrentAsAsMagnitudeAndPhase(double segLen){
        this.segmentLength = segLen;
        return this.voltageAndCurrentAsMagnitudeAndPhase();
    }

  // Plot voltage and current along the line
    public void plotVandI(){
        // Fill data arrays
        double [][] data = PlotGraph.data(4, this.numberOfPoints);

        double increment = this.segmentLength/(double)(this.numberOfPoints-1);

        data[0][0] = 0.0D;
        data[2][0] = 0.0D;
        for(int i=1; i<this.numberOfPoints; i++){
            data[0][i] = data[0][i-1] + increment;
            data[2][i] = data[2][i-1] + increment;
        }
        for(int i=0; i<this.numberOfPoints; i++){
            double[] output = this.voltageAndCurrentAsReal(data[0][i]);
            data[1][i] = output[0];
            data[3][i] = output[1];
        }


        data[4][0] = 0.0D;
        data[6][0] = 0.0D;
        data[4][1] = data[0][this.numberOfPoints/2];
        data[6][1] = data[0][this.numberOfPoints/2];
        data[4][2] = data[0][this.numberOfPoints-1];
        data[6][2] = data[0][this.numberOfPoints-1];

        data[5][0] = data[1][0];
        data[7][0] = data[3][0];
        data[5][1] = data[1][this.numberOfPoints/2];
        data[7][1] = data[3][this.numberOfPoints/2];
        data[5][2] = data[1][this.numberOfPoints-1];
        data[7][2] = data[3][this.numberOfPoints-1];

        // Plot data
        PlotGraph pg = new PlotGraph(data);
        int[] lineOpt = {3, 3, 0, 0};
        pg.setLine(lineOpt);
        int[] pointOpt = {0, 0, 1, 2};
        pg.setPoint(pointOpt);
        pg.setXaxisLegend("distance / metres");
        pg.setYaxisLegend("Voltage / V and Current / A");
        pg.plot();
    }

    // Deep copy
    public TransmissionLine copy(){

        if(this==null){
            return null;
        }
        else{
            TransmissionLine tl = new TransmissionLine();

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

            TransmissionLine tl = new TransmissionLine();

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