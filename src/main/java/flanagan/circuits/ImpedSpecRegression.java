/*      Class ImpedSpecRegression
*
*       Non-linear regression procedures for fitting impedance
*       spectroscopy and electrochemical impedance spectroscopy
*       data to user supplied circuit or one of a range of precompiled
*       circuit models.
*
*       User supplied circuit models require the interface ImpedSpecModel
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    9 June 2007  (Derived from impedance spectroscopy programs, 2004 - 2007)
*       UPDATE:  16 October 2007, 5 July 2008
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecRegression.html
*
*       Copyright (c)  June 2007    Michael Thomas Flanagan
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
****************************************************************************************/

package flanagan.circuits;

import flanagan.io.*;
import flanagan.complex.Complex;
import flanagan.complex.ComplexErrorProp;
import flanagan.analysis.ErrorProp;
import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction2;
import flanagan.analysis.Stat;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.plot.*;

import java.lang.reflect.Array;
import java.text.*;
import java.util.*;
import java.lang.Object;


public class ImpedSpecRegression extends Regression{

    private String regressionTitle = null;              // Title for output graphs and text file
    private boolean fileType = false;                   // = true if 'n' number to be added to file name

    private Complex appliedVoltage = null;              // magnitude of the applied voltage as complex
    private boolean appliedVoltageSet = false;          // = true when applied voltage entered
    private Complex appliedVoltageError = null;         // error of the applied voltage as complex
    private boolean voltageErrorSet = false;            // = true when applied voltage error entered

    private Complex referenceImpedance = null;          // reference impedance
    private boolean referenceSet = false;               // = true when reference impedance entered

    private double[] frequencies = null;                // frequencies [Hz]
    private double[] omegas = null;                     // radial frequencies
    private double[] log10frequencies = null;           // log10[frequencies/Hz]
    private double[] log10omegas = null;                // log10[radial frequencies]
    private int numberOfFrequencies = 0;                // number of points in the simulation
    private boolean frequenciesSet = false;             // = true when frequencies entered

    private Complex[] voltages = null;                  // voltages
    private Complex[] voltageWeights = null;            // voltage weights
    private double[] voltageMagnitudes = null;          // magnitude of the voltages
    private double[] voltageMagnitudeWeights = null;    // voltage magnitude weights
    private double[] voltagePhasesRad = null;           // voltage phases [radians]of the voltages
    private double[] voltagePhaseWeightsRad = null;     // voltage phase weights [radians]
    private double[] voltagePhasesDeg = null;           // voltage phases [degrees]of the voltages
    private double[] voltagePhaseWeightsDeg = null;     // voltage phase weights [degrees]
    private double[] realV = null;                      // real part of the voltage
    private double[] realVweights = null;               // real part of the voltage - weights
    private double[] imagV = null;                      // imaginary part of the voltages
    private double[] imagVweights = null;               // imaginary part of the voltage - weights

    private boolean weightsSet = true;                  // = false if no weights provided

    private int dataEnteredTypePointer = -1;            // = 0; real and imag voltage
                                                        // = 1: complex voltage
                                                        // = 2: voltage magnitude and radians
                                                        // = 3: voltage magnitude and degreees
                                                        // = 4; real and imag impedance
                                                        // = 5: complex mpedance
                                                        // = 6: mpedance magnitude and radians
                                                        // = 7: mpedance magnitude and degreees
    // Entered data type
    private String[] dataEnteredType = {"Complex voltage (as real and imaginary parts)", "Complex voltage (as Complex)",  "Voltage Magnitude and phase (in radians)", "Voltage Magnitude and phase (in degrees)", "Complex impedance (as real and imaginary parts)", "Complex impedance (as Complex)", "Magnitude and phase (in radians)", "Magnitude and phase (in degrees)"};
    private boolean voltageOrImpedance = true;          // = true:  data entered as test circuit voltages
                                                        // = false: data entered as test circuit impedances

    private Complex[] impedances = null;                // model impedances
    private Complex[] impedanceWeights = null;          // model impedance weights
    private double[] impedanceMagnitudes = null;        // magnitude of the impedances
    private double[] impedanceMagnitudeWeights = null;  // impedance magnitude weights
    private double[] impedancePhasesRad = null;         // impedance phases [radians]of the impedances
    private double[] impedancePhaseWeightsRad = null;   // impedance phase weights [radians]
    private double[] impedancePhasesDeg = null;         // impedance phases [degrees]of the impedances
    private double[] impedancePhaseWeightsDeg = null;   // impedance phase weights [degrees]
    private double[] realZ = null;                      // real part of the model impedance
    private double[] realZweights = null;               // real part of the model impedance - weights
    private double[] imagZ = null;                      // imaginary part of the model impedance
    private double[] imagZweights = null;               // imaginary part of the model impedance - weights
    private boolean impedancesSet = false;              // = true when impedances calculated


    private double[] xRegression = null;                // regression x-axis data
    private double[][] yRegression = null;              // regression y-axis data
    private double[][] wRegression = null;              // regression weights

    private int modelNumber = 0;                        // model number
    private int numberOfParameters = 0;                 // number of model parameters
    private String[] parameterSymbols = null;           // model parameter symbols
    private boolean modelSet = false;                   // = true when a model number is entered
    private boolean estimatesNeeded = false;            // = true when a no estimates are to be entered and they are yet to be calculated

    private boolean supressDefaultConstraints = false;  // = true when in-built constraints on parameters supressed
    private boolean supressAddedConstraints = false;    // = true when added constraints on parameters supressed
    private boolean supressAllConstraints = false;      // = true when all constraints on parameters supressed

    private ArrayList<Object> constraints = null;       // user added constraints
    private int numberOfAddedConstraints = -1;          // number of user added constraints

    private boolean constraintsAdded = false;           // = true when user added constraints on parameters entered

    private double[] initialEstimates = null;           // initial estimates of parameter values
    private double[] initialSteps = null;               // initial steps of parameter values

    private double[] bestEstimates = null;              // best estimates of parameter values
    private double[] standardDeviations = null;         // standard deviations of the best estimates
    private double[] coefficientsOfVariation = null;    // coefficients of variation of the best estimates
    private double[][] correlationCoefficients = null;  // correlation coefficients of the best estimates
    private double[] preMinimumGradients = null;        // gradient before the minimum for each parameter
    private double[] postMinimumGradients = null;       // gradient after the minimum for each parameter

    private int degreesOfFreedom = 0;                   // degrees of freedom
    private double sumOfSquares = 0.0D;                 // sum of squares at minimum
    private double reducedSumOfSquares = 0.0D;          // reduced sum of squares at minimum
    private double chiSquare = Double.NaN;              // chiSquare
    private double reducedChiSquare = Double.NaN;       // reducedChiSquare
    private double[] realZresiduals = null;             // Real[Z] residuals
    private double[] imagZresiduals = null;             // Imag[Z] residuals

    private double[] calculatedRealZ = null;            // calculated Real[Z]
    private double[] calculatedImagZ = null;            // calculated Imag[Z]
    private Complex[] calculatedImpedances = null;      // calculated model impedances
    private double[] calculatedImpedanceMagnitudes = null;      // calculated impedance magnitudes
    private double[] calculatedImpedancePhasesRad = null;       // calculated impedance phases (radians)
    private double[] calculatedImpedancePhasesDeg = null;       // calculated impedance phases (degrees)

    private double[] calculatedRealV = null;            // calculated Real[voltage]
    private double[] calculatedImagV = null;            // calculated Imag[voltage]
    private Complex[] calculatedVoltages = null;        // calculated voltages
    private double[] calculatedVoltageMagnitudes = null;       // calculated voltage magnitudes
    private double[] calculatedVoltagePhasesRad = null;        // calculated voltage phases (radians)
    private double[] calculatedVoltagePhasesDeg = null;        // calculated voltage phases (degrees)

    ArrayList<Object> results = null;                      // ArrayList with elements
                                                        // 0:   number of frequencies
                                                        // 1:   number of parameters
                                                        // 2:   degrees of freedom
                                                        // 3:   initial estimates
                                                        // 4:   initial step sizes
                                                        // 5:   best estimates
                                                        // 6:   standard deviations
                                                        // 7:   coefficients of variation
                                                        // 8:   gradients about the minimum
                                                        // 9:   reduced sum of squares
                                                        // 10:  chi square
                                                        // 11:  reduced chi square


    private boolean estimatesSet = false;               // = true when parameter estimates entered

    private ImpedSpecModel userModel = null;            // supplied user model
    private boolean userModelSet = false;               // = true if user model supplied
    private RegressionFunction2 regressionFunction = null;  // Regression function
    private double tolerance = 1e-9;                    // tolerance in regression exit test
    private int maximumIterations = 10000;              // maximum iterations in regression procedure
    private int numberOfIterations1 = -1;               // number of iterations taken in the first regression
    private int numberOfIterations2 = -1;               // number of iterations taken in the second regression
    private boolean regressionDone = false;             // = true when regression completed

    private int numberOfLineFrequencies = 8000;         // number of points on calculated line plots
    private boolean logOrLinear = true;                 // = true - log plot
                                                        // = false - linear plot
    private double[] lineFrequencies = null;            // frequencies for clculating theoretical lines
    private double[] log10lineFrequencies = null;       // log10  of the frequencies for clculating theoretical lines


    // CONSTRUCTORS

    // Constructor
    public ImpedSpecRegression(){
        this.regressionTitle = "  ";
    }

    // Constructor setting title
    public ImpedSpecRegression(String regressionTitle){
        this.regressionTitle = regressionTitle;
    }

    // ENTER DATA

    // Enter the applied voltage
    public void setAppliedVoltage(double voltage){
        this.appliedVoltage = new Complex(voltage, 0.0D);
        this.appliedVoltageError = new Complex(0.0D, 0.0D);
        this.appliedVoltageSet = true;
        if(this.referenceSet && this.frequenciesSet)this.calculateExperimentalImpedances();
    }

    // Enter the setApplied voltage with error
    public void appliedVoltage(double voltage, double voltageError){
        this.appliedVoltage = new Complex(voltage, 0.0D);
        this.appliedVoltageSet = true;
        this.appliedVoltage = new Complex(voltageError, 0.0D);
        this.voltageErrorSet = true;
        if(this.referenceSet && this.frequenciesSet)this.calculateExperimentalImpedances();
    }

    // Enter the reference impedance  - resistive
    public void setReferenceImpedance(double resistance){
        this.referenceImpedance = new Complex(resistance, 0.0D);
        this.referenceSet = true;
        if(this.appliedVoltageSet && this.frequenciesSet)this.calculateExperimentalImpedances();
    }

    // Enter the reference impedance  - reactive
    public void setReferenceImpedance(double real, double imag){
        this.referenceImpedance = new Complex(real, imag);
        this.referenceSet = true;
        if(this.appliedVoltageSet && this.frequenciesSet)this.calculateExperimentalImpedances();
    }

    // Enter the reference impedance  - reactive
    public void setReferenceImpedance(Complex impedance){
        this.referenceImpedance = impedance;
        this.referenceSet = true;
        if(this.appliedVoltageSet && this.frequenciesSet)this.calculateExperimentalImpedances();
    }

      // Enter data as frequencies and real and imaginary parts of the test circuit voltages - no weights
    public void voltageDataAsComplex(double[] frequencies, double[] real, double[] imag){

        double[] realWeight = new double[frequencies.length];
        double[] imagWeight = new double[frequencies.length];
        this.weightsSet = false;
        this.voltageDataAsComplex(frequencies, real, imag, realWeight, imagWeight);
    }


    // Enter data as frequencies and real and imaginary parts of the test circuit voltages - weights provided
    public void voltageDataAsComplex(double[] frequencies, double[] real, double[] imag, double[] realWeight, double[] imagWeight){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=real.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of Real[voltages], " + real.length);
        if(this.numberOfFrequencies!=imag.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of Imag[voltages], " + imag.length);
        if(this.numberOfFrequencies!=realWeight.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of real weights, " + realWeight.length);
        if(this.numberOfFrequencies!=imagWeight.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of imag weights, " + imagWeight.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.realV = Conv.copy(real);
        this.imagV = Conv.copy(imag);
        this.realVweights = Conv.copy(realWeight);
        this.imagVweights = Conv.copy(imagWeight);
        this.voltageMagnitudes = new double[this.numberOfFrequencies];
        this.voltagePhasesDeg = new double[this.numberOfFrequencies];
        this.voltagePhasesRad = new double[this.numberOfFrequencies];
        this.voltages = Complex.oneDarray(this.numberOfFrequencies);
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.voltages[i] = new Complex(realV[i], imagV[i]);
            this.voltageMagnitudes[i] = this.voltages[i].abs();
            this.voltagePhasesRad[i] = this.voltages[i].arg();
            this.voltagePhasesDeg[i] = Math.toDegrees(this.voltagePhasesRad[i]);
        }
        this.frequenciesSet = true;

        this.setImpedanceArrayLengths();
        this.calculateExperimentalImpedances();
        this.dataEnteredTypePointer = 4;
        this.voltageOrImpedance = true;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and Complex test circuit voltages - no weights provided
    public void voltageDataAsComplex(double[] frequencies, Complex[] voltages){

        Complex[] weights = Complex.oneDarray(voltages.length, 0.0D, 0.0D);
        this.weightsSet = false;
        this.voltageDataAsComplex(frequencies, voltages, weights);
    }

    // Enter data as frequencies and Complex voltages - weights provided
    // reference - voltage
    public void voltageDataAsComplex(double[] frequencies, Complex[] voltages, Complex[] weights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=voltages.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of voltages, " + voltages.length);
        if(this.numberOfFrequencies!=weights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of weights, " + weights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.voltages = Complex.copy(voltages);
        this.voltageWeights = Complex.copy(weights);
        this.voltageMagnitudes = new double[this.numberOfFrequencies];
        this.voltagePhasesDeg = new double[this.numberOfFrequencies];
        this.voltagePhasesRad = new double[this.numberOfFrequencies];
        this.realV = new double[this.numberOfFrequencies];
        this.imagV = new double[this.numberOfFrequencies];
        this.realVweights = new double[this.numberOfFrequencies];
        this.imagVweights = new double[this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.realV[i] = this.voltages[i].getReal();
            this.imagV[i] = this.voltages[i].getImag();
            this.realVweights[i] = weights[i].getReal();
            this.imagVweights[i] = weights[i].getImag();
            this.voltageMagnitudes[i] = this.voltages[i].abs();
            this.voltagePhasesRad[i] = this.voltages[i].arg();
            this.voltagePhasesDeg[i] = Math.toDegrees(this.voltagePhasesRad[i]);
        }
        this.frequenciesSet = true;

        this.setImpedanceArrayLengths();
        this.calculateExperimentalImpedances();
        this.voltageOrImpedance = true;
        this.dataEnteredTypePointer = 1;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and magnitudes and phases (radians) of the test circuit voltages - no weights
    public void voltageDataAsPhasorRad(double[] frequencies, double[] voltageMagnitudes, double[] voltagePhasesRad){

        double[] voltageMagWeights = new double[frequencies.length];
        double[] voltagePhaseWeights = new double[frequencies.length];
        this.weightsSet = false;
        this.voltageDataAsPhasorRad(frequencies, voltageMagnitudes, voltagePhasesRad, voltageMagWeights, voltagePhaseWeights);
    }

    // Enter data as frequencies and magnitudes and phases (radians) of the voltages - weights provided
    public void voltageDataAsPhasorRad(double[] frequencies, double[] voltageMagnitudes, double[] voltagePhasesRad, double[] voltageMagWeights, double[] voltagePhaseWeights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=voltageMagnitudes.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitudes, " + voltageMagnitudes.length);
        if(this.numberOfFrequencies!=voltagePhasesRad.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phases, " + voltagePhasesRad.length);
        if(this.numberOfFrequencies!=voltageMagWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitude weights, " + voltageMagWeights.length);
        if(this.numberOfFrequencies!=voltagePhaseWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phase weights, " + voltagePhaseWeights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.voltageMagnitudes = Conv.copy(voltageMagnitudes);
        this.voltageMagnitudeWeights = Conv.copy(voltageMagWeights);
        this.voltagePhaseWeightsRad = Conv.copy(voltagePhaseWeights);
        this.voltages= Complex.oneDarray(this.numberOfFrequencies);
        this.voltagePhasesDeg = new double[this.numberOfFrequencies];
        this.realV = new double[this.numberOfFrequencies];
        this.imagV = new double[this.numberOfFrequencies];
        this.realVweights = new double[this.numberOfFrequencies];
        this.imagVweights = new double[this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.voltagePhasesDeg[i] = Math.toDegrees(this.voltagePhasesRad[i]);
            this.voltages[i].polar(voltageMagnitudes[i], voltagePhasesRad[i]);
            this.realV[i] = this.voltages[i].getReal();
            this.imagV[i] = this.voltages[i].getImag();
            ErrorProp mag = new ErrorProp(voltageMagnitudes[i], voltageMagnitudeWeights[i]);
            ErrorProp phase = new ErrorProp(voltagePhasesRad[i], voltagePhaseWeights[i]);
            ComplexErrorProp volt = new ComplexErrorProp();
            volt.polar(mag, phase);
            this.realVweights[i] = volt.getRealError();
            this.imagVweights[i] = volt.getImagError();
        }
        this.frequenciesSet = true;

        this.setImpedanceArrayLengths();
        this.calculateExperimentalImpedances();
        this.voltageOrImpedance = true;
        this.dataEnteredTypePointer = 2;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and magnitudes and phases (degrees) of the test circuit voltages - no weights
    public void voltageDataAsPhasorDeg(double[] frequencies, double[] voltageMagnitudes, double[] voltagePhasesRad){

        double[] voltageMagWeights = new double[frequencies.length];
        double[] voltagePhaseWeights = new double[frequencies.length];
        this.weightsSet = false;
        this.voltageDataAsPhasorDeg(frequencies, voltageMagnitudes, voltagePhasesRad, voltageMagWeights, voltagePhaseWeights);
    }

    // Enter data as frequencies and magnitudes and phases of the voltages (degrees)
    public void voltageDataAsPhasorDeg(double[] frequencies, double[] voltageMagnitudes, double[] voltagePhasesDeg, double[] voltageMagWeights, double[] voltagePhaseWeights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=voltageMagnitudes.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitudes, " + voltageMagnitudes.length);
        if(this.numberOfFrequencies!=voltagePhasesDeg.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phases, " + voltagePhasesDeg.length);
        if(this.numberOfFrequencies!=voltageMagWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitude weights, " + voltageMagWeights.length);
        if(this.numberOfFrequencies!=voltagePhaseWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phase weights, " + voltagePhaseWeights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.voltageMagnitudes = Conv.copy(voltageMagnitudes);
        this.voltagePhasesDeg = Conv.copy(voltagePhasesDeg);
        this.voltages = Complex.oneDarray(this.numberOfFrequencies);
        this.voltagePhasesRad = new double[this.numberOfFrequencies];
        this.voltagePhaseWeightsRad = new double[this.numberOfFrequencies];
        this.voltageMagnitudeWeights = Conv.copy(voltageMagWeights);
        this.voltagePhaseWeightsDeg = Conv.copy(voltagePhaseWeights);
        this.realV = new double[this.numberOfFrequencies];
        this.imagV = new double[this.numberOfFrequencies];
        this.realVweights = new double[this.numberOfFrequencies];
        this.imagVweights = new double[this.numberOfFrequencies];
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.voltagePhasesRad[i] = Math.toRadians(this.voltagePhasesDeg[i]);
            this.voltagePhaseWeightsRad[i] = Math.toRadians(voltagePhaseWeights[i]);
            this.voltages[i].polar(voltageMagnitudes[i], voltagePhasesRad[i]);
            this.realV[i] = this.voltages[i].getReal();
            this.imagV[i] = this.voltages[i].getImag();
            ErrorProp mag = new ErrorProp(voltageMagnitudes[i], voltageMagnitudeWeights[i]);
            ErrorProp phase = new ErrorProp(voltagePhasesRad[i], this.voltagePhaseWeightsRad[i]);
            ComplexErrorProp volt = new ComplexErrorProp();
            volt.polar(mag, phase);
            this.realVweights[i] = volt.getRealError();
            this.imagVweights[i] = volt.getImagError();

        }
        this.frequenciesSet = true;

        this.setImpedanceArrayLengths();
        this.calculateExperimentalImpedances();
        this.voltageOrImpedance = true;
        this.dataEnteredTypePointer = 3;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and real and imaginary parts of the impedances - no weights
    public void impedanceDataAsComplex(double[] frequencies, double[] real, double[] imag){

        double[] realWeight = new double[frequencies.length];
        double[] imagWeight = new double[frequencies.length];
        this.weightsSet = false;
        this.impedanceDataAsComplex(frequencies, real, imag, realWeight, imagWeight);
    }


    // Enter data as frequencies and real and imaginary parts of the impedances - weights provided
    public void impedanceDataAsComplex(double[] frequencies, double[] real, double[] imag, double[] realWeight, double[] imagWeight){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=real.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of Real[impedances], " + real.length);
        if(this.numberOfFrequencies!=imag.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of Imag[impedances], " + imag.length);
        if(this.numberOfFrequencies!=realWeight.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of real weights, " + realWeight.length);
        if(this.numberOfFrequencies!=imagWeight.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of imag weights, " + imagWeight.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.realZ = Conv.copy(real);
        this.imagZ = Conv.copy(imag);
        this.realZweights = Conv.copy(realWeight);
        this.imagZweights = Conv.copy(imagWeight);
        this.impedanceMagnitudes = new double[this.numberOfFrequencies];
        this.impedancePhasesDeg = new double[this.numberOfFrequencies];
        this.impedancePhasesRad = new double[this.numberOfFrequencies];
        this.impedances = Complex.oneDarray(this.numberOfFrequencies);
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.impedances[i] = new Complex(realZ[i], imagZ[i]);
            this.impedanceMagnitudes[i] = this.impedances[i].abs();
            this.impedancePhasesRad[i] = this.impedances[i].arg();
            this.impedancePhasesDeg[i] = Math.toDegrees(this.impedancePhasesRad[i]);
        }
        this.frequenciesSet = true;
        this.impedancesSet = true;

        this.dataEnteredTypePointer = 4;
        this.voltageOrImpedance = false;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and Complex impedances - no weights provided
    public void impedanceDataAsComplex(double[] frequencies, Complex[] impedances){

        Complex[] weights = Complex.oneDarray(impedances.length, 0.0D, 0.0D);
        this.weightsSet = false;
        this.impedanceDataAsComplex(frequencies, impedances, weights);
    }

    // Enter data as frequencies and Complex impedances - weights provided
    public void impedanceDataAsComplex(double[] frequencies, Complex[] impedances, Complex[] weights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=impedances.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of impedances, " + impedances.length);
        if(this.numberOfFrequencies!=weights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of weights, " + weights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.impedances = Complex.copy(impedances);
        this.impedanceWeights = Complex.copy(weights);
        this.impedanceMagnitudes = new double[this.numberOfFrequencies];
        this.impedancePhasesDeg = new double[this.numberOfFrequencies];
        this.impedancePhasesRad = new double[this.numberOfFrequencies];
        this.realZ = new double[this.numberOfFrequencies];
        this.imagZ = new double[this.numberOfFrequencies];
        this.realZweights = new double[this.numberOfFrequencies];
        this.imagZweights = new double[this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.realZ[i] = this.impedances[i].getReal();
            this.imagZ[i] = this.impedances[i].getImag();
            this.realZweights[i] = weights[i].getReal();
            this.imagZweights[i] = weights[i].getImag();
            this.impedanceMagnitudes[i] = this.impedances[i].abs();
            this.impedancePhasesRad[i] = this.impedances[i].arg();
            this.impedancePhasesDeg[i] = Math.toDegrees(this.impedancePhasesRad[i]);
        }
        this.frequenciesSet = true;
        this.impedancesSet = true;

        this.voltageOrImpedance = false;
        this.dataEnteredTypePointer = 5;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and magnitudes and phases (radians) of the impedances - no weights
    public void impedanceDataAsPhasorRad(double[] frequencies, double[] impedanceMagnitudes, double[] impedancePhasesRad){

        double[] impedanceMagWeights = new double[frequencies.length];
        double[] impedancePhaseWeights = new double[frequencies.length];
        this.weightsSet = false;
        this.impedanceDataAsPhasorRad(frequencies, impedanceMagnitudes, impedancePhasesRad, impedanceMagWeights, impedancePhaseWeights);
    }

    // Enter data as frequencies and magnitudes and phases (radians) of the impedances - weights provided
    public void impedanceDataAsPhasorRad(double[] frequencies, double[] impedanceMagnitudes, double[] impedancePhasesRad, double[] impedanceMagWeights, double[] impedancePhaseWeights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=impedanceMagnitudes.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitudes, " + impedanceMagnitudes.length);
        if(this.numberOfFrequencies!=impedancePhasesRad.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phases, " + impedancePhasesRad.length);
        if(this.numberOfFrequencies!=impedanceMagWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitude weights, " + impedanceMagWeights.length);
        if(this.numberOfFrequencies!=impedancePhaseWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phase weights, " + impedancePhaseWeights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.impedanceMagnitudes = Conv.copy(impedanceMagnitudes);
        this.impedanceMagnitudeWeights = Conv.copy(impedanceMagWeights);
        this.impedancePhaseWeightsRad = Conv.copy(impedancePhaseWeights);
        this.impedances= Complex.oneDarray(this.numberOfFrequencies);
        this.impedancePhasesDeg = new double[this.numberOfFrequencies];
        this.realZ = new double[this.numberOfFrequencies];
        this.imagZ = new double[this.numberOfFrequencies];
        this.realZweights = new double[this.numberOfFrequencies];
        this.imagZweights = new double[this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.impedancePhasesDeg[i] = Math.toDegrees(this.impedancePhasesRad[i]);
            this.impedances[i].polar(impedanceMagnitudes[i], impedancePhasesRad[i]);
            this.realZ[i] = this.impedances[i].getReal();
            this.imagZ[i] = this.impedances[i].getImag();
            ErrorProp mag = new ErrorProp(impedanceMagnitudes[i], impedanceMagnitudeWeights[i]);
            ErrorProp phase = new ErrorProp(impedancePhasesRad[i], impedancePhaseWeights[i]);
            ComplexErrorProp volt = new ComplexErrorProp();
            volt.polar(mag, phase);
            this.realZweights[i] = volt.getRealError();
            this.imagZweights[i] = volt.getImagError();
        }
        this.frequenciesSet = true;
        this.impedancesSet = true;

        this.voltageOrImpedance = false;
        this.dataEnteredTypePointer = 6;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Enter data as frequencies and magnitudes and phases (degrees) of the impedances - no weights
    public void impedanceDataAsPhasorDeg(double[] frequencies, double[] impedanceMagnitudes, double[] impedancePhasesRad){

        double[] impedanceMagWeights = new double[frequencies.length];
        double[] impedancePhaseWeights = new double[frequencies.length];
        this.weightsSet = false;
        this.impedanceDataAsPhasorDeg(frequencies, impedanceMagnitudes, impedancePhasesRad, impedanceMagWeights, impedancePhaseWeights);
    }

    // Enter data as frequencies and magnitudes and phases of the impedances (degrees)
    public void impedanceDataAsPhasorDeg(double[] frequencies, double[] impedanceMagnitudes, double[] impedancePhasesDeg, double[] impedanceMagWeights, double[] impedancePhaseWeights){

        this.numberOfFrequencies = frequencies.length;
        if(this.numberOfFrequencies!=impedanceMagnitudes.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitudes, " + impedanceMagnitudes.length);
        if(this.numberOfFrequencies!=impedancePhasesDeg.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phases, " + impedancePhasesDeg.length);
        if(this.numberOfFrequencies!=impedanceMagWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of magnitude weights, " + impedanceMagWeights.length);
        if(this.numberOfFrequencies!=impedancePhaseWeights.length)throw new IllegalArgumentException("The number of frequencies, " + this.numberOfFrequencies + ", does not equal the number of phase weights, " + impedancePhaseWeights.length);

        this.frequencies = Conv.copy(frequencies);
        this.setAllFrequencyArrays();
        this.setCalculatedArrayLengths();

        this.impedanceMagnitudes = Conv.copy(impedanceMagnitudes);
        this.impedancePhasesDeg = Conv.copy(impedancePhasesDeg);
        this.impedances = Complex.oneDarray(this.numberOfFrequencies);
        this.impedancePhasesRad = new double[this.numberOfFrequencies];
        this.impedancePhaseWeightsRad = new double[this.numberOfFrequencies];
        this.impedanceMagnitudeWeights = Conv.copy(impedanceMagWeights);
        this.impedancePhaseWeightsDeg = Conv.copy(impedancePhaseWeights);
        this.realZ = new double[this.numberOfFrequencies];
        this.imagZ = new double[this.numberOfFrequencies];
        this.realZweights = new double[this.numberOfFrequencies];
        this.imagZweights = new double[this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.impedancePhasesRad[i] = Math.toRadians(this.impedancePhasesDeg[i]);
            this.impedancePhaseWeightsRad[i] = Math.toRadians(impedancePhaseWeights[i]);
            this.impedances[i].polar(impedanceMagnitudes[i], impedancePhasesRad[i]);
            this.realZ[i] = this.impedances[i].getReal();
            this.imagZ[i] = this.impedances[i].getImag();
            ErrorProp mag = new ErrorProp(impedanceMagnitudes[i], impedanceMagnitudeWeights[i]);
            ErrorProp phase = new ErrorProp(impedancePhasesRad[i], this.impedancePhaseWeightsRad[i]);
            ComplexErrorProp volt = new ComplexErrorProp();
            volt.polar(mag, phase);
            this.realZweights[i] = volt.getRealError();
            this.imagZweights[i] = volt.getImagError();

        }
        this.frequenciesSet = true;
        this.impedancesSet = true;

        this.voltageOrImpedance = false;
        this.dataEnteredTypePointer = 7;
        if(this.estimatesNeeded)this.setInitialEstimates();
    }

    // Set all frequency
    private void setAllFrequencyArrays(){

        this.log10frequencies = new double[this.numberOfFrequencies];
        this.omegas = new double[this.numberOfFrequencies];
        this.log10omegas = new double[this.numberOfFrequencies];
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.log10frequencies[i] = Math.log10(frequencies[i]);
            this.omegas[i] = 2.0D*Math.PI*frequencies[i];
            this.log10omegas[i] = Math.log10(omegas[i]);
        }
        this.frequenciesSet = true;
    }

     // Set all calculted array lengths
    private void setCalculatedArrayLengths(){

        this.realZresiduals = new double[this.numberOfFrequencies];
        this.imagZresiduals = new double[this.numberOfFrequencies];
        this.calculatedRealZ = new double[this.numberOfFrequencies];
        this.calculatedImagZ = new double[this.numberOfFrequencies];
        this.calculatedImpedances = Complex.oneDarray(this.numberOfFrequencies);
        this.calculatedImpedanceMagnitudes = new double[this.numberOfFrequencies];
        this.calculatedImpedancePhasesRad = new double[this.numberOfFrequencies];
        this.calculatedImpedancePhasesDeg = new double[this.numberOfFrequencies];

        if(this.appliedVoltageSet && this.referenceSet){
            this.calculatedRealV = new double[this.numberOfFrequencies];
            this.calculatedImagV = new double[this.numberOfFrequencies];
            this.calculatedVoltages = Complex.oneDarray(this.numberOfFrequencies);
            this.calculatedVoltageMagnitudes = new double[this.numberOfFrequencies];
            this.calculatedVoltagePhasesRad = new double[this.numberOfFrequencies];
            this.calculatedVoltagePhasesDeg = new double[this.numberOfFrequencies];
        }
    }

    // Set the impedance array lengths
    private void setImpedanceArrayLengths(){

        this.realZ = new double[this.numberOfFrequencies];
        this.imagZ = new double[this.numberOfFrequencies];
        this.realZweights = new double[this.numberOfFrequencies];
        this.imagZweights = new double[this.numberOfFrequencies];
        this.impedances = Complex.oneDarray(this.numberOfFrequencies);
        this.impedanceMagnitudes = new double[this.numberOfFrequencies];
        this.impedancePhasesRad = new double[this.numberOfFrequencies];
        this.impedancePhasesDeg = new double[this.numberOfFrequencies];
    }

    // Calculate the experimental impedances if voltages have been entered
    private void calculateExperimentalImpedances(){
        if(this.referenceSet && this.appliedVoltageSet){
            for(int i=0; i<this.numberOfFrequencies; i++){
                // voltage divider calculation

                this.impedances[i] = (this.referenceImpedance.times(this.voltages[i])).over(this.appliedVoltage.minus(this.voltages[i]));

                this.realZ[i] = this.impedances[i].getReal();
                this.imagZ[i] = this.impedances[i].getImag();

                this.impedanceMagnitudes[i] = this.impedances[i].abs();
                this.impedancePhasesRad[i] = this.impedances[i].arg();
                this.impedancePhasesDeg[i] = Math.toDegrees(this.impedancePhasesRad[i]);

                if(this.weightsSet && this.voltageErrorSet){
                    ComplexErrorProp appliedV = new ComplexErrorProp(this.appliedVoltage.getReal(), this.appliedVoltageError.getReal(), this.appliedVoltage.getImag(), this.appliedVoltageError.getImag());
                    ComplexErrorProp expertlV = new ComplexErrorProp(this.realV[i], this.realVweights[i], this.imagV[i], this.imagVweights[i]);
                    ComplexErrorProp refImped = new ComplexErrorProp(this.referenceImpedance.getReal(), 0.0D, this.referenceImpedance.getImag(), 0.0D);
                    ComplexErrorProp eVoverAv = (expertlV.over(appliedV)).times(refImped);
                    this.realZweights[i] = eVoverAv.getRealError();
                    this.imagZweights[i] = eVoverAv.getImagError();
                }
                this.impedancesSet = true;
            }
        }
    }


    // ENTER THE MODEL

    // Enter user supplied model, parameter symbols, initial estimates and initial steps
    public void setModel(ImpedSpecModel userModel,  String[] symbols, double[] initialEstimates, double[] initialSteps){

        this.userModel = userModel;
        this.parameterSymbols = symbols;
        this.numberOfParameters = symbols.length;
        if(this.numberOfParameters!=initialEstimates.length)throw new IllegalArgumentException("The number of parameter symbols, " + this.numberOfParameters + ", does not equal the number of initial estimates, "  + initialEstimates.length);
        if(this.numberOfParameters!=initialSteps.length)throw new IllegalArgumentException("The number of parameter symbols, " + this.numberOfParameters + ", does not equal the number of initial steps, "  + initialSteps.length);
        this.initialEstimates = initialEstimates;
        this.initialSteps = initialSteps;
        this.setEstimateArrayDimensions();
        this.estimatesSet = true;
        this.userModelSet = true;
    }

    // Enter user supplied model, parameter symbols, initial estimates and initial steps calculated as 10% of initial estimates
    public void setModel(ImpedSpecModel userModel, String[] symbols, double[] initialEstimates){

        this.userModel = userModel;
        this.parameterSymbols = symbols;
        this.numberOfParameters = symbols.length;
        if(this.numberOfParameters!=initialEstimates.length)throw new IllegalArgumentException("The number of parameter symbols, " + this.numberOfParameters + ", does not equal the number of initial estimates, "  + initialEstimates.length);
        this.initialEstimates = initialEstimates;
        this.initialSteps = new double[this.numberOfParameters];
        for(int i=0; i<this.numberOfParameters; i++)this.initialSteps[i] = Math.abs(this.initialEstimates[i])*0.1D;
        this.setEstimateArrayDimensions();
        this.estimatesSet = true;
        this.userModelSet = true;
    }

    // Enter the model number, initial estimates and initial steps
    public void setModel(int modelNumber, double[] initialEstimates, double[] initialSteps){

        this.numberOfParameters = initialEstimates.length;
        if(this.numberOfParameters!=Impedance.modelComponents(modelNumber).length)throw new IllegalArgumentException("The number of parameter estimates, " + this.numberOfParameters + ", does not equal the number of parameters, "  + Impedance.modelComponents(modelNumber).length + ", in model number " + modelNumber);
        if(this.numberOfParameters!=initialSteps.length)throw new IllegalArgumentException("The number of parameter estimates, " + this.numberOfParameters + ", does not equal the number of parameter steps, "  + initialSteps.length);

        this.modelNumber = modelNumber;
        this.initialEstimates = initialEstimates;
        this.initialSteps = initialSteps;
        this.parameterSymbols = Impedance.modelComponents(modelNumber);
        this.setEstimateArrayDimensions();
        this.estimatesSet = true;
        this.modelSet = true;
    }

    // Enter the model number and initial estimates - parameter steps calculated as 10% of initial estimated
    public void setModel(int modelNumber, double[] initialEstimates){

        this.numberOfParameters = initialEstimates.length;
        if(this.numberOfParameters!=Impedance.modelComponents(modelNumber).length)throw new IllegalArgumentException("The number of parameter estimates, " + this.numberOfParameters + ", does not equal the number of parameters, "  + Impedance.modelComponents(modelNumber).length + ", in model number " + modelNumber);

        this.modelNumber = modelNumber;
        this.initialEstimates = initialEstimates;
        this.parameterSymbols = Impedance.modelComponents(modelNumber);
        this.initialSteps = new double[this.numberOfParameters];
        for(int i=0; i<this.numberOfParameters; i++)this.initialSteps[i] = Math.abs(this.initialEstimates[i])*0.1D;
        this.setEstimateArrayDimensions();
        this.estimatesSet = true;
        this.modelSet = true;
    }

    // Enter the model number - parameters estimated by the method
    public void setModel(int modelNumber){

        this.modelNumber = modelNumber;
        this.parameterSymbols = Impedance.modelComponents(modelNumber);
        this.numberOfParameters = this.parameterSymbols.length;

        this.setEstimateArrayDimensions();

        // initial estimates
        this.setInitialEstimates();
        this.estimatesSet = true;

        this.modelSet = true;
    }

    // Set dimensions of best estimates and associated arrays
    private void setEstimateArrayDimensions(){

        this.bestEstimates = new double[this.numberOfParameters];
        this.standardDeviations = new double[this.numberOfParameters];
        this.coefficientsOfVariation = new double[this.numberOfParameters];
        this.preMinimumGradients = new double[this.numberOfParameters];
        this.postMinimumGradients = new double[this.numberOfParameters];

        this.correlationCoefficients = new double[this.numberOfParameters][this.numberOfParameters];
    }

    // Provide initial estimates for a given model number
    private void setInitialEstimates(){
        if(this.impedancesSet && this.frequenciesSet){

            this.degreesOfFreedom = this.numberOfFrequencies - this.numberOfParameters;
            if(this.degreesOfFreedom<=0)throw new IllegalArgumentException("Degrees of freedom, " + this.degreesOfFreedom + ", are less than 1");

            double meanRealZ = Stat.mean(this.realZ);
            double minRealZ = Fmath.minimum(this.realZ);
            int indexMinRealZ = Fmath.indexOf(this.realZ, minRealZ);
            double maxRealZ = Fmath.maximum(this.realZ);
            int indexMaxRealZ = Fmath.indexOf(this.realZ, maxRealZ);

            double meanImagZ = Stat.mean(this.imagZ);
            double minImagZ = Fmath.minimum(this.imagZ);
            int indexMinImagZ = Fmath.indexOf(this.imagZ, minImagZ);
            double maxImagZ = Fmath.maximum(this.imagZ);
            int indexMaxImagZ = Fmath.indexOf(this.imagZ, maxImagZ);

            double imagBig = Math.max(Math.abs(minImagZ),Math.abs(maxImagZ));
            int bigIndex = Fmath.indexOf(this.imagZ, imagBig);
            if(bigIndex==-1)bigIndex = Fmath.indexOf(this.imagZ, -imagBig);
            if(bigIndex==-1)bigIndex = this.numberOfFrequencies/2;

            double geometricFreqMean = Stat.geometricMean(this.log10frequencies);

            switch(this.modelNumber){
                case 1: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = meanRealZ;
                        break;
                case 2: this.initialEstimates = new double[this.numberOfParameters];
                        double sumC = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[0] = sumC/this.numberOfFrequencies;
                        break;
                case 3: this.initialEstimates = new double[this.numberOfParameters];
                        double sumL = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumL += Math.abs(this.imagZ[i]/this.omegas[i]);
                        this.initialEstimates[0] = sumL/this.numberOfFrequencies;
                        break;
                case 4: this.initialEstimates = new double[this.numberOfParameters];
                        double sumW = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++){
                            sumW += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                            sumW += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                        }
                        this.initialEstimates[0] = sumW/(2.0D*this.numberOfFrequencies);
                        break;
                case 5: this.initialEstimates = new double[this.numberOfParameters];
                        double sumF = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++){
                            sumF += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                            sumF += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                        }
                        this.initialEstimates[0] = sumF/(2.0D*this.numberOfFrequencies);
                        this.initialEstimates[1] = Math.abs(meanRealZ/this.initialEstimates[0]);
                        break;
                case 6: this.initialEstimates = new double[this.numberOfParameters];
                        double sumQ = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumQ +=  this.imagZ[i]/this.realZ[i];
                        sumQ /= this.numberOfFrequencies;
                        double theta = Math.abs(Math.atan(sumQ));
                        double cosTheta = Math.cos(theta);
                        double sinTheta = Math.sin(theta);
                        this.initialEstimates[1] = theta/(Math.PI/2.0);
                        double sigmaQ = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++){
                            sigmaQ += Math.abs(realZ[i]/(cosTheta*Math.pow(this.omegas[i], this.initialEstimates[1])));
                            sigmaQ += Math.abs(imagZ[i]/(sinTheta*Math.pow(this.omegas[i], this.initialEstimates[1])));
                        }
                        this.initialEstimates[0] = sigmaQ/(2.0D*this.numberOfFrequencies);
                        break;
                case 7: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = meanRealZ;
                        double sumC7 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC7 += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[1] = sumC7/this.numberOfFrequencies;
                        break;
                case 8: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = meanRealZ;
                        double sumL8 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumL8 += Math.abs(this.imagZ[i]/this.omegas[i]);
                        this.initialEstimates[1] = sumL8/this.numberOfFrequencies;
                        break;
                case 9: this.initialEstimates = new double[this.numberOfParameters];
                        double sumL9 = 0.0;
                        double sumC9 = 0.0;
                        for(int i=1; i<this.numberOfFrequencies; i++){
                            double cC9 = ((this.frequencies[i] - this.frequencies[i-1])/this.frequencies[i])/(this.imagZ[i]*this.frequencies[i-1] - this.imagZ[i-1]*this.frequencies[i]);
                            double lL9 = (this.imagZ[i] + 1.0D/(cC9*this.frequencies[i]))/this.frequencies[i];
                            sumL9 += lL9;
                            sumC9 += cC9;
                        }
                        this.initialEstimates[0] = sumL9/(this.numberOfFrequencies - 1);
                        this.initialEstimates[1] = sumC9/(this.numberOfFrequencies - 1);
                        break;
               case 10: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ;
                        this.initialEstimates[1] = 1.0D/(maxRealZ*this.frequencies[indexMinImagZ]);
                        break;
               case 11: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ;
                        this.initialEstimates[1] = maxRealZ/this.frequencies[indexMaxImagZ];
                        break;
               case 12: this.initialEstimates = new double[this.numberOfParameters];
                        double cL12 = 1/this.frequencies[indexMinImagZ];
                        double sumL12 = 0.0;
                        double sumC12 = 0.0;
                        for(int i=1; i<this.numberOfFrequencies; i++){
                            double c12 = this.imagZ[i]*(this.frequencies[i]*cL12 - 1.0/this.frequencies[i]);
                            sumL12 += c12;
                            sumC12 += cL12/c12;
                        }
                        this.initialEstimates[0] = sumL12/this.numberOfFrequencies;
                        this.initialEstimates[1] = sumC12/this.numberOfFrequencies;
                        break;
               case 13: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[2] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                        break;
               case 14: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[2] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        double sumL14 = 0.0;
                        double sumC14 = 0.0;
                        for(int i=1; i<this.numberOfFrequencies; i++){
                            double cC14 = ((this.frequencies[i] - this.frequencies[i-1])/this.frequencies[i])/(this.imagZ[i]*this.frequencies[i-1] - this.imagZ[i-1]*this.frequencies[i]);
                            double lL14 = (this.imagZ[i] + 1.0D/(cC14*this.frequencies[i]))/this.frequencies[i];
                            sumL14 += lL14;
                            sumC14 += cC14;
                        }
                        this.initialEstimates[3] = sumL14/(this.numberOfFrequencies - 1);
                        this.initialEstimates[1] = sumC14/(this.numberOfFrequencies - 1);
                        break;
               case 15: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[2] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        double cL15 = 1/this.frequencies[indexMinImagZ];
                        double sumL15 = 0.0;
                        double sumC15 = 0.0;
                        for(int i=1; i<this.numberOfFrequencies; i++){
                            double c15 = this.imagZ[i]*(this.frequencies[i]*cL15 - 1.0/this.frequencies[i]);
                            sumL15 += c15;
                            sumC15 += cL15/c15;
                        }
                        this.initialEstimates[3] = sumL15/this.numberOfFrequencies;
                        this.initialEstimates[1] = sumC15/this.numberOfFrequencies;
                        break;
               case 16: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ;
                        double sumC16 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC16 += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[1] = 2.0D*sumC16/this.numberOfFrequencies;
                        this.initialEstimates[2] = this.initialEstimates[1];
                        break;
               case 17: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ;
                        double sumC17 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC17 += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[1] = sumC17/(2.0D*this.numberOfFrequencies);
                        this.initialEstimates[2] = this.initialEstimates[1];
               case 18: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = minRealZ;
                        this.initialEstimates[2] = maxRealZ - minRealZ;
                        double sumC18 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC18 += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[1] = 2.0D*sumC18/this.numberOfFrequencies;
                        this.initialEstimates[3] = this.initialEstimates[1];
                        break;
               case 19: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ/2.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        this.initialEstimates[1] = 2.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                        this.initialEstimates[3] = this.initialEstimates[1];
                        break;
               case 20: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[4] = minRealZ;
                        this.initialEstimates[0] = (maxRealZ - minRealZ)/2.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        this.initialEstimates[1] = 2.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                        this.initialEstimates[3] = this.initialEstimates[1];
                        break;
               case 21: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[4] = minRealZ;
                        this.initialEstimates[0] = (maxRealZ - minRealZ)/2.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        double sumC21 = 0.0;
                        for(int i=0; i<this.numberOfFrequencies; i++)sumC21 += 1.0D/Math.abs(this.imagZ[i]*this.omegas[i]);
                        this.initialEstimates[1] = sumC21/(2.0D*this.numberOfFrequencies);
                        this.initialEstimates[3] = this.initialEstimates[1];
                        break;
               case 22: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ/3.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        this.initialEstimates[4] = this.initialEstimates[0];
                        this.initialEstimates[1] = 3.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                        this.initialEstimates[3] = this.initialEstimates[1];
                        this.initialEstimates[5] = this.initialEstimates[1];
                        break;
               case 23: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[6] = minRealZ;
                        this.initialEstimates[0] = (maxRealZ - minRealZ)/3.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        this.initialEstimates[4] = this.initialEstimates[0];
                        this.initialEstimates[1] = 3.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                        this.initialEstimates[3] = this.initialEstimates[1];
                        this.initialEstimates[5] = this.initialEstimates[1];
                        break;
               case 24: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[3] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        double sumW24 = 0.0;
                        if(indexMinImagZ<this.numberOfFrequencies-3){
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumW24 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW24 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[2] = sumW24/(2.0D*(this.numberOfFrequencies - indexMinImagZ));
                        }
                        else{
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*geometricFreqMean);
                            for(int i=0; i<this.numberOfFrequencies; i++){
                                sumW24 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW24 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[2] = sumW24/(2.0D*this.numberOfFrequencies);
                        }
                        break;
               case 25: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[4] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        double sumF25 = 0.0;
                        if(indexMinImagZ<this.numberOfFrequencies-3){
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumF25 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumF25 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[2] = sumF25/(2.0D*(this.numberOfFrequencies - indexMinImagZ));
                            this.initialEstimates[3] = Math.abs(meanRealZ/this.initialEstimates[2]);
                        }
                        else{
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*geometricFreqMean);
                            for(int i=0; i<this.numberOfFrequencies; i++){
                                sumF25 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumF25 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[2] = sumF25/(2.0D*this.numberOfFrequencies);
                            this.initialEstimates[3] = Math.abs(meanRealZ/this.initialEstimates[2]);
                        }
                        break;
               case 26: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[4] = minRealZ;
                        this.initialEstimates[0] = maxRealZ - minRealZ;
                        double sumQ26 = 0.0;
                        if(indexMinImagZ<this.numberOfFrequencies-3){
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++)sumQ26 +=  this.imagZ[i]/this.realZ[i];
                            sumQ26 /= (this.numberOfFrequencies - indexMinImagZ);
                            double theta26 = Math.abs(Math.atan(sumQ26));
                            double cosTheta26 = Math.cos(theta26);
                            double sinTheta26 = Math.sin(theta26);
                            this.initialEstimates[3] = theta26/(Math.PI/2.0);
                            double sigmaQ26 = 0.0;
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sigmaQ26 += Math.abs(realZ[i]/(cosTheta26*Math.pow(this.omegas[i], this.initialEstimates[1])));
                                sigmaQ26 += Math.abs(imagZ[i]/(sinTheta26*Math.pow(this.omegas[i], this.initialEstimates[1])));
                            }
                            this.initialEstimates[2] = sigmaQ26/(2.0D*(this.numberOfFrequencies - indexMinImagZ));
                        }
                        else{
                            this.initialEstimates[1] = 1.0D/(this.initialEstimates[0]*geometricFreqMean);
                            for(int i=0; i<this.numberOfFrequencies; i++)sumQ26 +=  this.imagZ[i]/this.realZ[i];
                            sumQ26 /= this.numberOfFrequencies;
                            double theta26 = Math.abs(Math.atan(sumQ26));
                            double cosTheta26 = Math.cos(theta26);
                            double sinTheta26 = Math.sin(theta26);
                            this.initialEstimates[3] = theta26/(Math.PI/2.0);
                            double sigmaQ26 = 0.0;
                            for(int i=0; i<this.numberOfFrequencies; i++){
                                sigmaQ26 += Math.abs(realZ[i]/(cosTheta26*Math.pow(this.omegas[i], this.initialEstimates[1])));
                                sigmaQ26 += Math.abs(imagZ[i]/(sinTheta26*Math.pow(this.omegas[i], this.initialEstimates[1])));
                            }
                            this.initialEstimates[2] = sigmaQ26/(2.0D*this.numberOfFrequencies);
                        }
                        break;
               case 27: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[0] = maxRealZ/2.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        double sumW27 = 0.0;
                        if(indexMinImagZ<this.numberOfFrequencies-3){
                            this.initialEstimates[1] = 2.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                            this.initialEstimates[3] = this.initialEstimates[1];
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumW27 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW27 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[4] = sumW27/(2.0D*(this.numberOfFrequencies - indexMinImagZ));
                        }
                        else{
                            this.initialEstimates[1] = 2.0D/(this.initialEstimates[0]*geometricFreqMean);
                            this.initialEstimates[3] = this.initialEstimates[1];
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumW27 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW27 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[4] = sumW27/(2.0D*this.numberOfFrequencies);
                        }
                        break;
               case 28: this.initialEstimates = new double[this.numberOfParameters];
                        this.initialEstimates[6] = minRealZ;
                        this.initialEstimates[0] = (maxRealZ - minRealZ)/2.0D;
                        this.initialEstimates[2] = this.initialEstimates[0];
                        double sumW28 = 0.0;
                        if(indexMinImagZ<this.numberOfFrequencies-3){
                            this.initialEstimates[1] = 3.0D/(this.initialEstimates[0]*this.frequencies[indexMinImagZ]);
                            this.initialEstimates[3] = this.initialEstimates[1];
                            this.initialEstimates[5] = this.initialEstimates[1];
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumW28 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW28 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[4] = sumW28/(2.0D*(this.numberOfFrequencies - indexMinImagZ));
                        }
                        else{
                            this.initialEstimates[1] = 3.0D/(this.initialEstimates[0]*geometricFreqMean);
                            this.initialEstimates[3] = this.initialEstimates[1];
                            this.initialEstimates[5] = this.initialEstimates[1];
                            for(int i=indexMinImagZ; i<this.numberOfFrequencies; i++){
                                sumW28 += Math.abs(this.realZ[i]*Math.sqrt(this.omegas[i]));
                                sumW28 += Math.abs(this.imagZ[i]*Math.sqrt(this.omegas[i]));
                            }
                            this.initialEstimates[4] = sumW28/(2.0D*this.numberOfFrequencies);
                        }
                        break;
               default: throw new IllegalArgumentException("Automatically calculated initial estimates are only presntly available for models 1 to 28");
            }

            // initial steps
            this.initialSteps = new double[this.numberOfParameters];
            for(int i=0; i<this.numberOfParameters; i++)this.initialSteps[i] = Math.abs(this.initialEstimates[i])*0.1D;

            // check for zero step
            for(int i=0; i<this.numberOfParameters; i++){
                if(this.initialSteps[i]==0.0D){
                    if(this.parameterSymbols[i].trim().substring(0,1).equals("R"))this.initialSteps[i] = maxRealZ*0.01D;
                    if(this.parameterSymbols[i].trim().substring(0,1).equals("C"))this.initialSteps[i] = 0.01D/(imagBig*this.frequencies[bigIndex]);
                    if(this.parameterSymbols[i].trim().substring(0,1).equals("L"))this.initialSteps[i] = imagBig*0.01D/this.frequencies[bigIndex];
                    if(this.parameterSymbols[i].trim().substring(0,1).equals("W"))this.initialSteps[i] = 0.01D/(imagBig*Math.sqrt(this.frequencies[bigIndex]));
                    if(this.parameterSymbols[i].trim().substring(0,2).equals("Fs"))this.initialSteps[i] = 0.01D/(imagBig*Math.sqrt(this.frequencies[bigIndex]));
                    if(this.parameterSymbols[i].trim().substring(0,2).equals("Fd"))this.initialSteps[i] = 0.05D;
                    if(this.parameterSymbols[i].trim().substring(0,2).equals("Qs"))this.initialSteps[i] = 0.01D/(imagBig*Math.sqrt(this.frequencies[bigIndex]));
                    if(this.parameterSymbols[i].trim().substring(0,2).equals("Qa"))this.initialSteps[i] = 0.005D;
                }
            }

            this.estimatesSet = true;
        }
        else{
            this.estimatesNeeded = true;
        }
    }

    // get the initial estimates
    public double[] getInitialEstimates(){
        if(!this.estimatesSet)throw new IllegalArgumentException("No initial estimates have been entered or calculated");
        return this.initialEstimates;
    }

    // get the model parameter symbols
    public String[] getCircuitComponents(){
        return this.parameterSymbols;
    }

    // REGRESSION

    // user addition of constraints
    public void addNewConstraint(int parameter, int direction, double boundary){
        this.constraints.add(new Integer(parameter));
        this.constraints.add(new Integer(direction));
        this.constraints.add(new Double(boundary));
        this.numberOfAddedConstraints++;
        this.constraintsAdded = true;
    }

    public void addNewConstraint(String parameterSymbol, int direction, double boundary){
        if(this.numberOfParameters==0)throw new IllegalArgumentException("No model number or model parameters entered");
        int parameterNumber = -1;
        for(int i=0; i<this.numberOfParameters; i++){
            if( this.parameterSymbols[i].trim().equals( parameterSymbol.trim() ) )parameterNumber = i;
        }
        if(parameterNumber == -1)throw new IllegalArgumentException("Parameter symbol, " + parameterSymbol + ", not found");

        this.constraints.add(new Integer(parameterNumber));
        this.constraints.add(new Integer(direction));
        this.constraints.add(new Double(boundary));
        this.numberOfAddedConstraints++;
        this.constraintsAdded = true;
    }

    // remove default constraints on parameters
    public void removeDefaultConstraints(){
        this.supressDefaultConstraints = true;
    }

    // restore default constraints on parameters
    public void restoreDefaultConstraints(){
        this.supressDefaultConstraints = false;
    }

    // remove added constraints on parameters
    public void removeAddedConstraints(){
        this.supressAddedConstraints = true;
        this.constraintsAdded = false;
    }

    // remove all constraints on parameters
    public void removeAllConstraints(){
        this.supressDefaultConstraints = true;
        this.supressAddedConstraints = true;
        this.constraintsAdded = false;
    }

    // reset maximum number of iterations
    public void resetMaximumNumberOfIterations(int max){
        this.maximumIterations = max;
    }

    // reset the regression tolerance
    public void resetTolerance(double tol){
        this.tolerance = tol;
    }

    // get the regression results as ArrayList
    public ArrayList<Object> getRegressionResultsAsArrayList(){
        if(!this.regressionDone)this.regression();
        return this.results;
    }

    // get the regression results as Vector
    public Vector<Object> getRegressionResultsAsVector(){
        if(!this.regressionDone)this.regression();
        int n = this.results.size();
        Vector<Object> res = new Vector<Object>(n);
        for(int i=0; i<n; i++)res.add(this.results.get(i));
        return res;
    }

    // get the regression results as Vector
    public Vector<Object> getRegressionResults(){
        return this.getRegressionResults();
    }

    // Set Regression data arrays
    private void setRegressionArrays(){

        this.xRegression = new double[this.numberOfFrequencies];
        this.yRegression = new double[2][this.numberOfFrequencies];
        if(this.weightsSet)this.wRegression = new double[2][this.numberOfFrequencies];

        for(int i=0; i<this.numberOfFrequencies; i++){
            xRegression[i] = this.omegas[i];
            yRegression[0][i] = this.realZ[i];
            yRegression[1][i] = this.imagZ[i];
        }

        if(this.weightsSet){
            for(int i=0; i<this.numberOfFrequencies; i++){
                wRegression[0][i] = this.realZweights[i];
                wRegression[1][i] = this.imagZweights[i];
            }
        }
    }

    // fit the data to the chosen model
    public ArrayList<Object> regression(){

        // check data
        this.degreesOfFreedom = this.numberOfFrequencies - this.numberOfParameters;
        if(this.degreesOfFreedom<=0)throw new IllegalArgumentException("Degrees of freedom, " + this.degreesOfFreedom + ", are less than 1");
        if(!this.impedancesSet)throw new IllegalArgumentException("No impedances or voltages have been entered");

       // check initial estimates have been provided
        if(!this.estimatesSet && !this.userModelSet)this.setInitialEstimates();

        // Set regression arrays
        this.setRegressionArrays();

        // store initial estimates and associated data
        this.results = new ArrayList<Object>();
        this.results.add(new Integer(this.numberOfFrequencies));
        this.results.add(new Integer(this.numberOfParameters));
        this.results.add(new Integer(this.degreesOfFreedom));
        this.results.add(this.parameterSymbols);
        this.results.add(Conv.copy(this.initialEstimates));
        this.results.add(Conv.copy(this.initialSteps));

        // Enter regression data
        if(this.weightsSet){
            this.enterData(xRegression, yRegression, wRegression);
        }
        else{
            this.enterData(xRegression, yRegression);
        }

        // Create instance of regression function
        if(this.userModelSet){
            ImpedSpecRegressionFunction2 function = new ImpedSpecRegressionFunction2();
            function.numberOfFrequencies = this.numberOfFrequencies;
            function.isModel = this.userModel;
            this.regressionFunction = function;
        }else{
            ImpedSpecRegressionFunction1 function = new ImpedSpecRegressionFunction1();
            function.numberOfFrequencies = this.numberOfFrequencies;
            function.modelNumber = this.modelNumber;
            this.regressionFunction = function;
        }

        // Enter user added constraints
        int[] param = null;
        int[] direct = null;
        double[] bound = null;
        if(this.constraintsAdded){
            param  = new int[this.numberOfAddedConstraints];
            direct = new int[this.numberOfAddedConstraints];
            bound  = new double[this.numberOfAddedConstraints];
            int index = 0;
            for(int i=0; i<this.numberOfAddedConstraints; i++){
                int parameter = ((Integer)constraints.get(index)).intValue();
                param[i] = parameter;
                index++;
                int direction = ((Integer)constraints.get(index)).intValue();
                direct[i] = direction;
                index++;
                double boundary = ((Double)constraints.get(index)).doubleValue();
                bound[i] = boundary;
                index++;
                this.addConstraint(parameter, direction, boundary);
            }
        }

        // enter in-built constraints (if not set to be supressed)
        if(!this.supressDefaultConstraints){

            for(int i=0; i<this.numberOfParameters; i++){
                double lower = 0.0;
                double upper = 1.0;
                if(this.constraintsAdded){
                    for(int j=0; j<this.numberOfAddedConstraints; j++){
                        if(param[j]==i){
                            if(direct[j]==1){
                                upper = bound[j];
                            }
                            else{
                                lower = bound[j];
                            }
                        }
                    }
                }
                this.addConstraint(i, -1, lower);
                if(this.parameterSymbols[i].trim().substring(0,1).equals("Qa"))this.addConstraint(i, 1, upper);
            }
        }

        // perform regression
        this.simplex2(this.regressionFunction, Conv.copy(this.initialEstimates), Conv.copy(this.initialSteps), this.tolerance, this.maximumIterations);

        // repeat regression with best estimates as new initial estimates
        this.numberOfIterations1 = this.getNiter();
        double[] estimates = this.getCoeff();
        double[] steps = new double[this.numberOfParameters];
        for(int i=0; i<this.numberOfParameters; i++)steps[i] = Math.abs(estimates[i])*0.1D;

        this.simplex2(this.regressionFunction, estimates, steps, this.tolerance, this.maximumIterations);

        // store the regression results
        this.bestEstimates = this.getCoeff();
        this.results.add(this.bestEstimates);
        this.standardDeviations = this.getCoeffSd();
        this.results.add(this.standardDeviations);
        this.coefficientsOfVariation = this.getCoeffVar();
        this.results.add(this.coefficientsOfVariation);
        this.correlationCoefficients = this.getCorrCoeffMatrix();
        this.results.add(this.correlationCoefficients);
        double[][] gradients = new double[this.numberOfParameters][2];
        if(this.getGrad()==null){
            for(int i=0; i<this.numberOfParameters; i++){
                this.preMinimumGradients[i] = Double.NaN;
                this.postMinimumGradients[i] = Double.NaN;
            }
        }
        else{
            gradients = this.getGrad();
            for(int i=0; i<this.numberOfParameters; i++){
                this.preMinimumGradients[i] = gradients[i][0];
                this.postMinimumGradients[i] = gradients[i][1];
            }
        }

        this.results.add(this.preMinimumGradients);
        this.results.add(this.postMinimumGradients);
        this.sumOfSquares = this.getSumOfSquares();
        this.results.add(new Double(this.sumOfSquares));
        this.reducedSumOfSquares = this.sumOfSquares/this.degreesOfFreedom;
        this.results.add(new Double(this.reducedSumOfSquares));
        if(this.weightsSet){
            this.chiSquare = this.getChiSquare();
            this.results.add(new Double(this.chiSquare));
            this.reducedChiSquare = this.getReducedChiSquare();
            this.results.add(new Double(this.reducedChiSquare));
        }
        else{
           this.results.add(null);
           this.results.add(null);
        }
        this.numberOfIterations2 = this.getNiter();
        this.results.add(new Integer(this.numberOfIterations1));
        this.results.add(new Integer(this.numberOfIterations2));
        this.results.add(new Integer(this.maximumIterations));
        this.results.add(this.dataEnteredType[this.dataEnteredTypePointer]);

        this.results.add(this.frequencies);
        this.results.add(this.log10frequencies);
        this.results.add(this.omegas);
        this.results.add(this.log10omegas);
        this.results.add(this.impedanceMagnitudes);
        this.results.add(this.impedancePhasesRad);
        this.results.add(this.impedancePhasesDeg);
        this.results.add(this.impedances);
        this.results.add(this.realZ);
        this.results.add(this.imagZ);

        double[] calculatedY = this.getYcalc();
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.calculatedRealZ[i] = calculatedY[i];
            this.calculatedImagZ[i] = calculatedY[i + this.numberOfFrequencies];
        }
        this.results.add(this.calculatedRealZ);
        this.results.add(this.calculatedImagZ);


        double[] residuals = this.getResiduals();
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.realZresiduals[i] = residuals[i];
            this.imagZresiduals[i] = residuals[i + this.numberOfFrequencies];
        }
        this.results.add(this.realZresiduals);
        this.results.add(this.imagZresiduals);

       if(this.weightsSet){
            switch(this.dataEnteredTypePointer){
                case 0:     this.results.add(this.realVweights);
                            this.results.add(this.imagVweights);
                            break;
                case 1:     this.results.add(this.voltageWeights);
                            this.results.add(null);
                            break;
                case 2:     this.results.add(this.voltageMagnitudeWeights);
                            this.results.add(this.voltagePhaseWeightsRad);
                            break;
                case 3:     this.results.add(this.voltageMagnitudeWeights);
                            this.results.add(this.voltagePhaseWeightsDeg);
                            break;
                case 4:     this.results.add(this.realZweights);
                            this.results.add(this.imagZweights);
                            break;
                case 5:     this.results.add(this.impedanceWeights);
                            this.results.add(null);
                            break;
                case 6:     this.results.add(this.impedanceMagnitudeWeights);
                            this.results.add(this.impedancePhaseWeightsRad);
                            break;
                case 7:     this.results.add(this.impedanceMagnitudeWeights);
                            this.results.add(this.impedancePhaseWeightsDeg);
                            break;
                default:    this.results.add(null);
                            this.results.add(null);
            }
            this.results.add(this.realZweights);
            this.results.add(this.imagZweights);
        }
        else{
            for(int i=0; i<4; i++)this.results.add(null);
        }

        for(int i=0; i<this.numberOfFrequencies; i++){
            this.calculatedImpedances[i] = new Complex(this.calculatedRealZ[i], this.calculatedImagZ[i]);
            this.calculatedImpedanceMagnitudes[i] = this.calculatedImpedances[i].abs();
            this.calculatedImpedancePhasesRad[i] = this.calculatedImpedances[i].arg();
            this.calculatedImpedancePhasesDeg[i] = Math.toDegrees(this.calculatedImpedancePhasesRad[i]);
        }
        this.results.add(this.calculatedImpedances);
        this.results.add(this.calculatedImpedanceMagnitudes);
        this.results.add(this.calculatedImpedancePhasesRad);
        this.results.add(this.calculatedImpedancePhasesDeg);

        if(this.appliedVoltageSet && this.referenceSet){
            for(int i=0; i<this.numberOfFrequencies; i++){
                this.calculatedVoltages[i] = this.appliedVoltage.times(this.calculatedImpedances[i]).over(this.calculatedImpedances[i].plus(this.referenceImpedance));
                this.calculatedRealV[i] = this.calculatedVoltages[i].getReal();
                this.calculatedImagV[i] = this.calculatedVoltages[i].getImag();
                this.calculatedVoltageMagnitudes[i] = this.calculatedVoltages[i].abs();
                this.calculatedVoltagePhasesRad[i] = this.calculatedVoltages[i].arg();
                this.calculatedVoltagePhasesDeg[i] = Math.toDegrees(this.calculatedVoltagePhasesRad[i]);
            }
            this.results.add(this.calculatedVoltages);
            this.results.add(this.calculatedRealV);
            this.results.add(this.calculatedImagV);
            this.results.add(this.calculatedVoltageMagnitudes);
            this.results.add(this.calculatedVoltagePhasesRad);
            this.results.add(this.calculatedVoltagePhasesDeg);
        }
        else{
            for(int i=0; i<6; i++)this.results.add(null);
        }

        this.regressionDone = true;

        return this.results;
    }

    // get the best estimates
    public double[] getBestEstimates(){
        if(!this.regressionDone)this.regression();
        return this.bestEstimates;
    }

    // get the best estimates standard deviations
    public double[] getStandardDeviations(){
        if(!this.regressionDone)this.regression();
        return this.standardDeviations;
    }

    // get the number of iterations taken in the first regression
    public int getFirstNumberOfIterations(){
        return this.numberOfIterations1;
    }

    // get the number of iterations taken in the second regression
    public int getSecondNumberOfIterations(){
        return this.numberOfIterations2;
    }

    // get the number of iterations taken
    public double getTolerance(){
        return this.tolerance;
    }

    // PLOT

    // Set linear option
    public void setLinearPlot(){
        this.logOrLinear = false;

    }

    // Set log10 option
    public void setLog10Plot(){
        this.logOrLinear = true;
    }

    // Calculate line frequencies
    private void calculateLineFrequencies(){
        double lowestFrequency = Fmath.minimum(this.frequencies);
        double highestFrequency = Fmath.maximum(this.frequencies);
        if(this.logOrLinear){
            double logLow = Fmath.log10(lowestFrequency);
            double logHigh = Fmath.log10(highestFrequency);
            double increment = (logHigh - logLow)/(this.numberOfLineFrequencies - 1);
            this.lineFrequencies = new double[this.numberOfLineFrequencies];
            this.log10lineFrequencies = new double[this.numberOfLineFrequencies];
            this.log10lineFrequencies[0] = logLow;
            this.log10lineFrequencies[this.numberOfLineFrequencies-1] = logHigh;
            for(int i=1; i<this.numberOfLineFrequencies-1; i++)this.log10lineFrequencies[i] = this.log10lineFrequencies[i-1] + increment;
            for(int i=0; i<this.numberOfLineFrequencies; i++)this.lineFrequencies[i] = Math.pow(10.0D, this.log10lineFrequencies[i]);

        }
        else{
            double increment = (highestFrequency - lowestFrequency)/(this.numberOfLineFrequencies - 1);
            this.lineFrequencies = new double[this.numberOfLineFrequencies];
            this.lineFrequencies[0] = lowestFrequency;
            this.log10lineFrequencies = new double[this.numberOfLineFrequencies];
            this.lineFrequencies[this.numberOfLineFrequencies-1] = highestFrequency;
            for(int i=1; i<this.numberOfLineFrequencies-1; i++)this.lineFrequencies[i] = this.lineFrequencies[i-1] + increment;
            for(int i=0; i<this.numberOfLineFrequencies; i++)this.log10lineFrequencies[i] = Fmath.log10(this.lineFrequencies[i]);
        }
    }

    // Returns date and time
    private String[] dateAndTime(){
        Date d = new Date();
        String[] ret = new String[2];
        ret[0] = DateFormat.getDateInstance().format(d);
        ret[1] = DateFormat.getTimeInstance().format(d);
        return ret;
    }


    // Display Cole-Cole Plot
    public ArrayList<Object> plotColeCole(){
        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecRegression program:  Cole - Cole plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.regressionTitle;

        if(!this.regressionDone)this.regression();

        this.calculateLineFrequencies();

        double[][] data = PlotGraph.data(2, this.numberOfLineFrequencies);

        for(int i=0; i<this.numberOfFrequencies; i++){
            data[0][i] = this.realZ[this.numberOfFrequencies - i - 1];
            data[1][i] = -this.imagZ[this.numberOfFrequencies - i - 1];
        }

        if(this.userModelSet){
            for(int i=0; i<this.numberOfLineFrequencies; i++){
                data[2][i] = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[this.numberOfLineFrequencies - i - 1]*2.0D*Math.PI).getReal();
                data[3][i] = -userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[this.numberOfLineFrequencies - i - 1]*2.0D*Math.PI).getImag();
            }
        }
        else{
            for(int i=0; i<this.numberOfLineFrequencies; i++){
                data[2][i] = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[this.numberOfLineFrequencies - i - 1]*2.0D*Math.PI, this.modelNumber).getReal();
                data[3][i] = -Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[this.numberOfLineFrequencies - i - 1]*2.0D*Math.PI, this.modelNumber).getImag();
            }
        }

        PlotGraph pg = new PlotGraph(data);
        int[] lineOpt = {0, 3};
        pg.setLine(lineOpt);
        int[] pointOpt = {1, 0};
        pg.setPoint(pointOpt);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Real[Impedance / ohms]");
        pg.setYaxisLegend("-Imag[Impedance / ohms]");
        pg.plot();

        return this.results;
    }

    // Plot impedance magnitude versus frequency
    public ArrayList<Object> plotImpedanceMagnitudes(){

        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecRegression program:  Impedance magnitude versus frequency plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.regressionTitle;

        if(!this.regressionDone)this.regression();

        this.calculateLineFrequencies();

        // Magnitude versus frequency
        double[][] data = PlotGraph.data(2, this.numberOfLineFrequencies);

        if(this.logOrLinear){
            for(int i=0; i<this.numberOfFrequencies; i++){
                data[0][i] = this.log10frequencies[i];
                data[1][i] = this.impedanceMagnitudes[i];
            }
        }
        else{
            for(int i=0; i<this.numberOfFrequencies; i++){
                data[0][i] = this.frequencies[i];
                data[1][i] = this.impedanceMagnitudes[i];
            }
        }

        if(this.logOrLinear){
            if(this.userModelSet){
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.log10lineFrequencies[i];
                    Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                    data[3][i] = imped.abs();
                }
            }
            else{
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.log10lineFrequencies[i];
                    Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                    data[3][i] = imped.abs();
                }
            }
        }
        else{
             if(this.userModelSet){
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.lineFrequencies[i];
                    Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                    data[3][i] = imped.abs();
                }
            }
            else{
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.lineFrequencies[i];
                    Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                    data[3][i] = imped.abs();
                }
            }
        }

        PlotGraph pg = new PlotGraph(data);
        int[] lineOpt = {0, 3};
        pg.setLine(lineOpt);
        int[] pointOpt = {1, 0};
        pg.setPoint(pointOpt);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        if(this.logOrLinear){
            pg.setXaxisLegend("Log10[Frequency / Hz]");
        }
        else{
            pg.setXaxisLegend("Frequency / Hz");
        }
        pg.setYaxisLegend("Impedance Magnitude");
        pg.plot();

        return this.results;
    }

    // Plot impedance phase versus frequency
    public ArrayList<Object> plotImpedancePhases(){

        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecRegression program:  Impedance phase versus frequency plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.regressionTitle;

        if(!this.regressionDone)this.regression();

        this.calculateLineFrequencies();

        // Magnitude versus frequency
        double[][] data = PlotGraph.data(2, this.numberOfLineFrequencies);

        if(this.logOrLinear){
            for(int i=0; i<this.numberOfFrequencies; i++){
                data[0][i] = this.log10frequencies[i];
                data[1][i] = this.impedancePhasesDeg[i];
            }
        }
        else{
            for(int i=0; i<this.numberOfFrequencies; i++){
                data[0][i] = this.frequencies[i];
                data[1][i] = this.impedancePhasesDeg[i];
            }
        }

        if(this.logOrLinear){
            if(this.userModelSet){
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.log10lineFrequencies[i];
                    Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                    data[3][i] = Math.toDegrees(imped.arg());
                }
            }
            else{
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.log10lineFrequencies[i];
                    Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                    data[3][i] = Math.toDegrees(imped.arg());
                }
            }
        }
        else{
            if(this.userModelSet){
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.lineFrequencies[i];
                    Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                    data[3][i] = Math.toDegrees(imped.arg());
                }
            }
            else{
                for(int i=0; i<this.numberOfLineFrequencies; i++){
                    data[2][i] = this.lineFrequencies[i];
                    Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                    data[3][i] = Math.toDegrees(imped.arg());
                }
            }
        }

        PlotGraph pg = new PlotGraph(data);
        int[] lineOpt = {0, 3};
        pg.setLine(lineOpt);
        int[] pointOpt = {1, 0};
        pg.setPoint(pointOpt);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        if(this.logOrLinear){
            pg.setXaxisLegend("Log10[Frequency / Hz]");
        }
        else{
            pg.setXaxisLegend("Frequency / Hz");
        }
        pg.setYaxisLegend("Impedance Phase / degrees");
        pg.plot();

        return this.results;
    }


    // Plot voltage magnitude versus frequency
    public ArrayList<Object> plotVoltageMagnitudes(){

        if(!this.regressionDone)this.regression();

        if(this.referenceSet && this.appliedVoltageSet){
            String[] dAndT= this.dateAndTime();
            String graphTitle1 = "ImpedSpecRegression program:  Voltage magnitude versus frequency plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
            String graphTitle2 = this.regressionTitle;

            this.calculateLineFrequencies();

            double[][] data = PlotGraph.data(2, this.numberOfLineFrequencies);

            if(this.logOrLinear){
                for(int i=0; i<this.numberOfFrequencies; i++){
                    data[0][i] = this.log10frequencies[i];
                    data[1][i] = this.voltageMagnitudes[i];
                }
            }
            else{
                for(int i=0; i<this.numberOfFrequencies; i++){
                    data[0][i] = this.frequencies[i];
                    data[1][i] = this.voltageMagnitudes[i];
                }
            }

            if(this.logOrLinear){
                if(this.userModelSet){
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.log10lineFrequencies[i];
                        Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = volt.abs();
                    }
                }
                else{
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.log10lineFrequencies[i];
                        Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = volt.abs();
                    }
                }
            }
            else{
               if(this.userModelSet){
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.lineFrequencies[i];
                        Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = volt.abs();
                    }
                }
                else{
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.lineFrequencies[i];
                        Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = volt.abs();
                    }
                }
            }

            PlotGraph pg = new PlotGraph(data);
            int[] lineOpt = {0, 3};
            pg.setLine(lineOpt);
            int[] pointOpt = {1, 0};
            pg.setPoint(pointOpt);
            pg.setGraphTitle(graphTitle1);
            pg.setGraphTitle2(graphTitle2);
            if(this.logOrLinear){
                pg.setXaxisLegend("Log10[Frequency / Hz]");
            }
            else{
                pg.setXaxisLegend("Frequency / Hz");
            }
            pg.setYaxisLegend("Voltage Magnitude");
            pg.plot();
        }
        else{
            System.out.println("The voltage magnitudes cannot be plotted as no reference impedance or applied voltage has been entered");
        }

        return this.results;
    }

    // Plot voltage phase versus frequency
    public ArrayList<Object> plotVoltagePhases(){

        if(!this.regressionDone)this.regression();

        if(this.referenceSet && this.appliedVoltageSet){
            String[] dAndT= this.dateAndTime();
            String graphTitle1 = "ImpedSpecRegression program:  Voltage phase versus frequency plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
            String graphTitle2 = this.regressionTitle;

            this.calculateLineFrequencies();

            double[][] data = PlotGraph.data(2, this.numberOfLineFrequencies);

            if(this.logOrLinear){
                for(int i=0; i<this.numberOfFrequencies; i++){
                    data[0][i] = this.log10frequencies[i];
                    data[1][i] = this.voltagePhasesDeg[i];
                }
            }
            else{
                for(int i=0; i<this.numberOfFrequencies; i++){
                    data[0][i] = this.frequencies[i];
                    data[1][i] = this.voltagePhasesDeg[i];
                }
            }

            if(this.logOrLinear){
                if(this.userModelSet){
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.log10lineFrequencies[i];
                        Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = Math.toDegrees(volt.arg());
                    }
                }
                else{
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.log10lineFrequencies[i];
                        Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = Math.toDegrees(volt.arg());
                    }
                }
            }
            else{
                if(this.userModelSet){
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.lineFrequencies[i];
                        Complex imped = userModel.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = Math.toDegrees(volt.arg());
                    }
                }
                else{
                    for(int i=0; i<this.numberOfLineFrequencies; i++){
                        data[2][i] = this.lineFrequencies[i];
                        Complex imped = Impedance.modelImpedance(this.bestEstimates, this.lineFrequencies[i]*2.0D*Math.PI, this.modelNumber);
                        Complex volt = imped.times(this.appliedVoltage).over(this.referenceImpedance.plus(imped));
                        data[3][i] = Math.toDegrees(volt.arg());
                    }
                }
            }

            PlotGraph pg = new PlotGraph(data);
            int[] lineOpt = {0, 3};
            pg.setLine(lineOpt);
            int[] pointOpt = {1, 0};
            pg.setPoint(pointOpt);
            pg.setGraphTitle(graphTitle1);
            pg.setGraphTitle2(graphTitle2);
            if(this.logOrLinear){
                pg.setXaxisLegend("Log10[Frequency / Hz]");
            }
            else{
                pg.setXaxisLegend("Frequency / Hz");
            }
            pg.setYaxisLegend("Voltage Phases / degrees");
            pg.plot();
        }
        else{
            System.out.println("The voltage magnitudes cannot be plotted as no reference impedance or applied voltage has been entered");
        }

        return this.results;
    }

    // PRINT

    // Print regression results to text file
    public ArrayList<Object> printToTextFile(){
        String fileName = "ImpedSpecRegressionOutput.txt";
        this.fileType = true;
        return this.printToTextFile(fileName);
    }

    // Print regression results to text file
    public ArrayList<Object> printToTextFile(String fileName){

        if(!this.regressionDone)regression();

        int field = 11;   // output field length
        int trunc = 4;    // truncation length

        // Check extension
        fileName = fileName.trim();
        int dotPosition = fileName.indexOf('.');
        if(dotPosition==-1)fileName += ".txt";

        // instantiate a FileOutput
        FileOutput fout = null;
        if(this.fileType){
            fout = new FileOutput(fileName, 'n');
        }
        else{
            fout = new FileOutput(fileName);
        }

        // print header
        fout.println("ImpedSpecRegression Program Output File:  " + this.regressionTitle);
        fout.dateAndTimeln(fileName);
        fout.println();
        if(this.modelSet){
             fout.println("Circuit - model number " + this.modelNumber);
        }
        else{
             fout.println("Circuit supplied by the user");
        }
        fout.println();


        // print circuit parameters
        fout.println("Circuit Parameters");
        fout.println("Best Estimates");

        fout.print("Parameter", field);
        fout.print("Best", field);
        fout.print("Standard", field);
        fout.print("Coeff. of", field);
        fout.print("Pre-", field);
        fout.println("Post-");

        fout.print("   ", field);
        fout.print("estimate", field);
        fout.print("deviation", field);
        fout.print("variation", field);
        fout.print("gradient", field);
        fout.println("gradient");

        for(int i=0; i<this.numberOfParameters; i++){
            fout.print(this.parameterSymbols[i], field);
            fout.print(Fmath.truncate(this.bestEstimates[i], trunc), field);
            fout.print(Fmath.truncate(this.standardDeviations[i], trunc), field);
            fout.print(Fmath.truncate(this.coefficientsOfVariation[i], trunc), field);
            fout.print(Fmath.truncate(this.preMinimumGradients[i], trunc), field);
            fout.println(Fmath.truncate(this.postMinimumGradients[i], trunc));
        }
        fout.println();

        fout.println("Initial Estimates");

        fout.print("Parameter", field);
        fout.print("Initial", field);
        fout.println("initial");

        fout.print("   ", field);
        fout.print("estimate", field);
        fout.println("step size");

        for(int i=0; i<this.numberOfParameters; i++){
            fout.print(this.parameterSymbols[i], field);
            fout.print(Fmath.truncate(this.initialEstimates[i], trunc), field);
            fout.println(Fmath.truncate(this.initialSteps[i], trunc));
        }
        fout.println();

        // Print summary of regression statistics
        fout.println("Sum of squares of the Real[Z] and Imag[Z] residuals:         " + Fmath.truncate(this.sumOfSquares, trunc));
        fout.println("Reduced sum of squares of the Real[Z] and Imag[Z] residuals: " + Fmath.truncate(this.sumOfSquares/this.degreesOfFreedom, trunc));
        fout.println("Degrees of freedom: " + this.degreesOfFreedom);
        if(this.weightsSet){
            fout.println("Chi square:         " + Fmath.truncate(this.chiSquare, trunc));
            fout.println("Reduced chi square: " + Fmath.truncate(this.reducedChiSquare, trunc));
        }
        fout.println("Number of iterations taken in the first regression:      " + this.numberOfIterations1);
        fout.println("Number of iterations taken in the second regression:     " + this.numberOfIterations2);
        fout.println("Maximum number of iterations allowed in each regression: " + this.maximumIterations);
        fout.println();

        // print aplied voltage and reference impedance if entered
        if(this.appliedVoltageSet)fout.println("Applied voltage: " + this.appliedVoltage.getReal());
        if(this.referenceSet)fout.println("Reference impedance: " + this.referenceImpedance);
        fout.println();

        // Print impedance data for each frequency
        field=14;
        fout.println("Fitted and entered data [frequencies, calculated impedances, data as entered]");
        fout.print("Entered data type:  ");
        fout.println(dataEnteredType[dataEnteredTypePointer]);
        fout.println();

        fout.print("Frequency", field);
        fout.print("Experimental", field);
        fout.print("Calculated", field);
        fout.print("Experimental", field);
        fout.print("Calculated", field);

        switch(this.dataEnteredTypePointer){
            case 0: fout.print("Real", field);
                    fout.print("Imag", field);
                    break;
            case 1: fout.print("Complex", field);
                    break;
            case 2: fout.print("Magnitude", field);
                    fout.print("Phase (rad)", field);
                    break;
            case 3: fout.print("Magnitude", field);
                    fout.print("Phase (deg)", field);
                    break;
            case 4: fout.print("Real", field);
                    fout.print("Imag", field);
                    break;
            case 5: fout.print("Complex", field);
                    break;
            case 6: fout.print("Magnitude", field);
                    fout.print("Phase (rad)", field);
                    break;
            case 7: fout.print("Magnitude", field);
                    fout.print("Phase (deg)", field);
                    break;
        }
        fout.println();

        fout.print("Frequency", field);
        fout.print("Real[Z]", field);
        fout.print("Real[Z]", field);
        fout.print("Imag[Z]", field);
        fout.print("Imag[Z]", field);
        switch(this.dataEnteredTypePointer){
            case 0: fout.print("[voltage]", field);
                    fout.print("[voltage]", field);
                    break;
            case 1: fout.print("voltage", field);
                    break;
            case 2: fout.print("[voltage]", field);
                    fout.print("[voltage]", field);
                    break;
            case 3: fout.print("[voltage]", field);
                    fout.print("[voltage]", field);
                    break;
            case 4: fout.print("[impedance]", field);
                   fout.print("[impedance]", field);
                    break;
            case 5: fout.print("impedance", field);
                    break;
            case 6: fout.print("[impedance]", field);
                    fout.print("[impedance]", field);
                    break;
            case 7: fout.print("[impedance]", field);
                    fout.print("[impedance]", field);
                    break;
        }
        fout.println();

        for(int i=0; i<this.numberOfFrequencies; i++){
                fout.print(Fmath.truncate(this.frequencies[i], trunc), field);
                fout.print(Fmath.truncate(this.realZ[i], trunc), field);
                fout.print(Fmath.truncate(this.calculatedRealZ[i], trunc), field);
                fout.print(Fmath.truncate(this.imagZ[i], trunc), field);
                fout.print(Fmath.truncate(this.calculatedImagZ[i], trunc),field);

                switch(this.dataEnteredTypePointer){
                case 0: fout.print(Fmath.truncate(this.realV[i], trunc), field);
                        fout.print(Fmath.truncate(this.imagV[i], trunc), field);
                        break;
                case 1: fout.print(Complex.truncate(this.voltages[i], trunc), field);
                        break;
                case 2: fout.print(Fmath.truncate(this.voltageMagnitudes[i], trunc), field);
                        fout.print(Fmath.truncate(this.voltagePhasesRad[i], trunc), field);
                        break;
                case 3: fout.print(Fmath.truncate(this.voltageMagnitudes[i], trunc), field);
                        fout.print(Fmath.truncate(this.voltagePhasesDeg[i], trunc), field);
                        break;
                case 4: fout.print(Fmath.truncate(this.realZ[i], trunc), field);
                        fout.print(Fmath.truncate(this.imagZ[i], trunc), field);
                        break;
                case 5: fout.print(Complex.truncate(this.impedances[i], trunc), field);
                        break;
                case 6: fout.print(Fmath.truncate(this.impedanceMagnitudes[i], trunc), field);
                        fout.print(Fmath.truncate(this.impedancePhasesRad[i], trunc), field);
                        break;
                case 7: fout.print(Fmath.truncate(this.impedanceMagnitudes[i], trunc), field);
                        fout.print(Fmath.truncate(this.impedancePhasesDeg[i], trunc), field);
                        break;
                }
                fout.println();
        }
        fout.close();

        return this.results;
    }


    // Print regression results to a .xls (MS Excel) file
    public ArrayList<Object> printToExcelFile(){
        String fileName = "ImpedSpecRegressionOutput.txt";
        this.fileType = true;
        return this.printToExcelFile(fileName);
    }

    // Print regression results to a .xls (MS Excel) file
    public ArrayList<Object> printToExcelFile(String fileName){

        if(!this.regressionDone)regression();

        int field = 11;   // output field length
        int trunc = 4;    // truncation length

        // Check extension
        fileName = fileName.trim();
        int dotPosition = fileName.indexOf('.');
        if(dotPosition==-1){
            fileName += ".xls";
        }
        else{
            fileName = fileName.substring(0, dotPosition) + ".xls";
        }

        // instantiate a FileOutput
        FileOutput fout = null;
        if(this.fileType){
            fout = new FileOutput(fileName, 'n');
        }
        else{
            fout = new FileOutput(fileName);
        }

        // print header
        fout.println("ImpedSpecRegression Program Output File:  " + this.regressionTitle);
        fout.dateAndTimeln(fileName);
        fout.println();
        if(this.modelSet){
             fout.println("Circuit - model number " + this.modelNumber);
        }
        else{
             fout.println("Circuit supplied by the user");
        }
        fout.println();


        // print circuit parameters
        fout.println("Circuit Parameters");
        fout.println("Best Estimates");

        fout.printtab("Parameter", field);
        fout.printtab("Best", field);
        fout.printtab("Standard", field);
        fout.printtab("Coeff. of", field);
        fout.printtab("Pre-", field);
        fout.println("Post-");

        fout.printtab("   ", field);
        fout.printtab("estimate", field);
        fout.printtab("deviation", field);
        fout.printtab("variation", field);
        fout.printtab("gradient", field);
        fout.println("gradient");

        for(int i=0; i<this.numberOfParameters; i++){
            fout.printtab(this.parameterSymbols[i], field);
            fout.printtab(Fmath.truncate(this.bestEstimates[i], trunc), field);
            fout.printtab(Fmath.truncate(this.standardDeviations[i], trunc), field);
            fout.printtab(Fmath.truncate(this.coefficientsOfVariation[i], trunc), field);
            fout.printtab(Fmath.truncate(this.preMinimumGradients[i], trunc), field);
            fout.println(Fmath.truncate(this.postMinimumGradients[i], trunc));
        }
        fout.println();

        fout.println("Initial Estimates");

        fout.printtab("Parameter", field);
        fout.printtab("Initial", field);
        fout.println("initial");

        fout.printtab("   ", field);
        fout.printtab("estimate", field);
        fout.println("step size");

        for(int i=0; i<this.numberOfParameters; i++){
            fout.printtab(this.parameterSymbols[i], field);
            fout.printtab(Fmath.truncate(this.initialEstimates[i], trunc), field);
            fout.println(Fmath.truncate(this.initialSteps[i], trunc));
        }
        fout.println();

        // Print summary of regression statistics
        fout.println("Sum of squares of the Real[Z] and Imag[z] residuals:         " + Fmath.truncate(this.sumOfSquares, trunc));
        fout.println("Reduced sum of squares of the Real[Z] and Imag[z] residuals: " + Fmath.truncate(this.sumOfSquares/this.degreesOfFreedom, trunc));
        fout.println("Degrees of freedom: " + this.degreesOfFreedom);
        if(this.weightsSet){
            fout.println("Chi square:         " + Fmath.truncate(this.chiSquare, trunc));
            fout.println("Reduced chi square: " + Fmath.truncate(this.reducedChiSquare, trunc));
        }
        fout.println("Number of iterations taken in the first regression:      " + this.numberOfIterations1);
        fout.println("Number of iterations taken in the second regression:     " + this.numberOfIterations2);
        fout.println("Maximum number of iterations allowed in each regression: " + this.maximumIterations);
        fout.println();

        // Print impedance data for each frequency
        field=14;
        fout.println("Fitted and entered data [frequencies, calculated impedances, data as entered]");
        fout.print("Entered data type:  ");
        fout.println(dataEnteredType[dataEnteredTypePointer]);
        fout.println();

        fout.printtab("Frequency", field);
        fout.printtab("Experimental", field);
        fout.printtab("Calculated", field);
        fout.printtab("Experimental", field);
        fout.printtab("Calculated", field);

        switch(this.dataEnteredTypePointer){
            case 0: fout.printtab("Real", field);
                    fout.printtab("Imag", field);
                    break;
            case 1: fout.printtab("Complex", field);
                    break;
            case 2: fout.printtab("Magnitude", field);
                    fout.printtab("Phase (rad)", field);
                    break;
            case 3: fout.printtab("Magnitude", field);
                    fout.printtab("Phase (deg)", field);
                    break;
            case 4: fout.printtab("Real", field);
                    fout.printtab("Imag", field);
                    break;
            case 5: fout.printtab("Complex", field);
                    break;
            case 6: fout.printtab("Magnitude", field);
                    fout.printtab("Phase (rad)", field);
                    break;
            case 7: fout.printtab("Magnitude", field);
                    fout.printtab("Phase (deg)", field);
                    break;
        }
        fout.println();

        fout.printtab("Frequency", field);
        fout.printtab("Real[Z]", field);
        fout.printtab("Real[Z]", field);
        fout.printtab("Imag[Z]", field);
        fout.printtab("Imag[Z]", field);
        switch(this.dataEnteredTypePointer){
            case 0: fout.printtab("[voltage]", field);
                    fout.printtab("[voltage]", field);
                    break;
            case 1: fout.printtab("voltage", field);
                    break;
            case 2: fout.printtab("[voltage]", field);
                    fout.printtab("[voltage]", field);
                    break;
            case 3: fout.printtab("[voltage]", field);
                    fout.printtab("[voltage]", field);
                    break;
            case 4: fout.printtab("[impedance]", field);
                   fout.printtab("[impedance]", field);
                    break;
            case 5: fout.printtab("impedance", field);
                    break;
            case 6: fout.printtab("[impedance]", field);
                    fout.printtab("[impedance]", field);
                    break;
            case 7: fout.printtab("[impedance]", field);
                    fout.printtab("[impedance]", field);
                    break;
        }
        fout.println();

        for(int i=0; i<this.numberOfFrequencies; i++){
                fout.printtab(Fmath.truncate(this.frequencies[i], trunc), field);
                fout.printtab(Fmath.truncate(this.realZ[i], trunc), field);
                fout.printtab(Fmath.truncate(this.calculatedRealZ[i], trunc), field);
                fout.printtab(Fmath.truncate(this.imagZ[i], trunc), field);
                fout.printtab(Fmath.truncate(this.calculatedImagZ[i], trunc),field);

                switch(this.dataEnteredTypePointer){
                case 0: fout.printtab(Fmath.truncate(this.realV[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.imagV[i], trunc), field);
                        break;
                case 1: fout.printtab(Complex.truncate(this.voltages[i], trunc), field);
                        break;
                case 2: fout.printtab(Fmath.truncate(this.voltageMagnitudes[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.voltagePhasesRad[i], trunc), field);
                        break;
                case 3: fout.printtab(Fmath.truncate(this.voltageMagnitudes[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.voltagePhasesDeg[i], trunc), field);
                        break;
                case 4: fout.printtab(Fmath.truncate(this.realZ[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.imagZ[i], trunc), field);
                        break;
                case 5: fout.printtab(Complex.truncate(this.impedances[i], trunc), field);
                        break;
                case 6: fout.printtab(Fmath.truncate(this.impedanceMagnitudes[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.impedancePhasesRad[i], trunc), field);
                        break;
                case 7: fout.printtab(Fmath.truncate(this.impedanceMagnitudes[i], trunc), field);
                        fout.printtab(Fmath.truncate(this.impedancePhasesDeg[i], trunc), field);
                        break;

            }
            fout.println();
        }

        // close file
        fout.close();

        return this.results;
    }
}




