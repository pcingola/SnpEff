/*      Class ImpedSpecSimulation
*
*        Calculates and displays the impedance spectra
*        for a user supplied circuit or for one of a
*        range of class supplied circuits
*
*       User supplied circuit models require the interface ImpedSpecModel
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    25 May 2007  (Derived from impedance spectroscopy programs, 2004 - 2007)
*       UPDATED: 1 June 2007, 7 June 2007, 8 June 2007, 5 July 2008
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecSimulation.html
*
*       Copyright (c) May 2007    Michael Thomas Flanagan
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

import flanagan.circuits.Impedance;
import flanagan.io.*;
import flanagan.complex.Complex;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.plot.*;

import java.text.*;
import java.util.*;


public class ImpedSpecSimulation{

    private double lowestFrequency = 0.0D;              // lowest frequency (Hz)
    private double lowestOmega = 0.0D;                  // lowest frequency (radians)
    private boolean lowestSet = false;                  // = true when lowest frequency entered
    private double highestFrequency = 0.0D;             // highest frequency (Hz)
    private double highestOmega = 0.0D;                 // highest frequency (radians)
    private boolean highestSet = false;                 // = true when highest frequency entered
    private boolean logOrLinear = true;                 // = true;  log plot
                                                        // = false; linear plot
    private double increment = 0.0D;                    // plotting increment
                                                        // if logOrLinear = true  - log10[Hz] increment
                                                        // if logOrLinear = false - linear [Hz] increment
    private double[] frequencies = null;                // frequencies [Hz]
    private double[] omegas = null;                     // radial frequencies
    private double[] log10frequencies = null;           // log10[frequencies/Hz]
    private double[] log10omegas = null;                // log10[radial frequencies]

    private int numberOfFrequencies = 800;              // number of points in the simulation
    private boolean numberSet = true;                   // = true when number of points entered
    private boolean frequenciesSet = false;             // = true when frequencies entered

    private int modelNumber = 0;                        // model number
    private double[] parameters = null;                 // model parameters
    private int numberOfParameters = 0;                 // number of model parameters
    private String[] modelParameterSymbols = null;      // model parameter symbols
    private boolean parametersSet = false;              // = true when parameters entered
    private boolean modelSet = false;                   // = true when a model number is set

    private Complex[] impedances = null;                // impedances
    private double[] magnitudesZ = null;                // magnitudes
    private double[] phasesRadZ = null;                 // impedance phases [radians]
    private double[] phasesDegZ = null;                 // impedance phases [degrees]
    private double[] realZ = null;                      // real part of the impedance
    private double[] imagZ = null;                      // imaginary part of the impedance

    private boolean impedancesSet = false;              // = true when impedances calculated

    private Complex[] voltages = null;                  // voltages across test circuit
    private double[] magnitudesV = null;                // voltage magnitudes
    private double[] phasesRadV = null;                 // voltage  phases [radians]
    private double[] phasesDegV = null;                 // voltage phases [degrees]
    private double[] realV = null;                      // real part of the voltage
    private double[] imagV = null;                      // imaginary part of the voltage

    private ImpedSpecModel userModel = null;            // user supplied model

    private String simulationTitle = null;              // Title for output graphs and text file
    private boolean fileType = false;                   // = true if 'n' number to be added to file name

    private Complex appliedVoltage = null;              // magnitude of the applied voltage as complex
    private boolean voltageSet = false;                 // = true when applied voltage entered

    private Complex referenceImpedance = null;          // reference impedance
    private boolean referenceSet = false;               // = true when reference impedance entered


    // Constructor
    public ImpedSpecSimulation(){
        this.simulationTitle = "  ";
    }

    // Constructor
    public ImpedSpecSimulation(String simulationTitle){
        this.simulationTitle = simulationTitle;
    }

    // Set scan range in Hz
    public void setScanRangeHz(double low, double high){
        this.lowestFrequency = low;
        this.lowestOmega = 2.0D*Math.PI*low;
        this.highestFrequency = high;
        this.highestOmega = 2.0D*Math.PI*high;
        this.calculateFrequencies();
    }

    // Set scan range in radians
    public void setScanRangeRadians(double low, double high){
        this.lowestFrequency = low/(2.0D*Math.PI);
        this.lowestOmega = low;
        this.highestFrequency = high/(2.0D*Math.PI);
        this.highestOmega = high;
        this.calculateFrequencies();
    }

    // Set lowest frequency in Hz
    public void setLowFrequency(double low){
        this.lowestFrequency = low;
        this.lowestOmega = 2.0D*Math.PI*low;
        this.lowestSet = true;
        if(this.highestSet && this.numberSet)this.calculateFrequencies();
    }

    // Set lowest radial frequency
    public void setLowRadialFrequency(double low){
        this.lowestOmega = low;
        this.lowestFrequency = low/(2.0D*Math.PI);
        this.lowestSet = true;
        if(this.highestSet && this.numberSet)this.calculateFrequencies();
    }

    // Set highest frequency in Hz
    public void setHighFrequency(double high){
        this.highestFrequency = high;
        this.highestOmega = 2.0D*Math.PI*high;
        this.highestSet = true;
        if(this.lowestSet && this.numberSet)this.calculateFrequencies();
    }

    // Set highest radial frequency
    public void setHighRadialFrequency(double high){
        this.highestOmega = high;
        this.highestFrequency = high/(2.0D*Math.PI);
        this.highestSet = true;
        if(this.lowestSet && this.numberSet)this.calculateFrequencies();
    }


    // Calculate frequencies
    private void calculateFrequencies(){
        if(this.logOrLinear){
            double logLow = Fmath.log10(this.lowestFrequency);
            double logHigh = Fmath.log10(this.highestFrequency);
            this.increment = (logHigh - logLow)/(this.numberOfFrequencies - 1);
            this.frequencies = new double[this.numberOfFrequencies];
            this.log10frequencies = new double[this.numberOfFrequencies];
            this.omegas = new double[this.numberOfFrequencies];
            this.log10omegas = new double[this.numberOfFrequencies];
            this.log10frequencies[0] = logLow;
            this.log10frequencies[this.numberOfFrequencies-1] = logHigh;
            for(int i=1; i<this.numberOfFrequencies-1; i++)this.log10frequencies[i] = this.log10frequencies[i-1] + this.increment;
            for(int i=0; i<this.numberOfFrequencies; i++){
                this.frequencies[i] = Math.pow(10.0D, this.log10frequencies[i]);
                this.omegas[i] = this.frequencies[i]*2.0D*Math.PI;
                this.log10omegas[i] = Fmath.log10(this.omegas[i]);
            }
        }
        else{
            this.increment = (this.highestFrequency - this.lowestFrequency)/(this.numberOfFrequencies - 1);
            this.frequencies = new double[this.numberOfFrequencies];
            this.frequencies[0] = this.lowestFrequency;
            this.log10frequencies = new double[this.numberOfFrequencies];
            this.omegas = new double[this.numberOfFrequencies];
            this.log10omegas = new double[this.numberOfFrequencies];
            this.frequencies[this.numberOfFrequencies-1] = this.highestFrequency;
            for(int i=1; i<this.numberOfFrequencies-1; i++)this.frequencies[i] = this.frequencies[i-1] + this.increment;
            for(int i=0; i<this.numberOfFrequencies; i++){
                this.log10frequencies[i] = Fmath.log10(this.frequencies[i]);
                this.omegas[i] = this.frequencies[i]*2.0D*Math.PI;
                this.log10omegas[i] = Fmath.log10(this.omegas[i]);
            }
        }
        this.frequenciesSet = true;
    }

    // Set linear option
    public void setLinearPlot(){
        this.logOrLinear = false;
        if(this.lowestSet && this.highestSet && this.numberSet)this.calculateFrequencies();

    }

    // Set log10 option
    public void setLog10Plot(){
        this.logOrLinear = true;
        if(this.lowestSet && this.highestSet && this.numberSet)this.calculateFrequencies();
    }

    // Enter the applied voltage
    public void setAppliedVoltage(double voltage){
        this.appliedVoltage = new Complex(voltage, 0.0D);
        this.voltageSet = true;
    }

        // Enter the reference impedance  - resistive
    public void setReferenceImpedance(double resistance){
        this.referenceImpedance = new Complex(resistance, 0.0D);
        this.referenceSet = true;
    }

    // Enter the reference impedance  - reactive
    public void setReferenceImpedance(double real, double imag){
        this.referenceImpedance = new Complex(real, imag);
        this.referenceSet = true;
    }

    // Enter the reference impedance  - reactive
    public void setReferenceImpedance(Complex impedance){
        this.referenceImpedance = impedance;
        this.referenceSet = true;
    }

    // set model number -  parameters supplied as an array
    public void setModel(int modelNumber, double[] parameters){
        if(modelNumber == 0 || modelNumber>Impedance.numberOfModels)throw new IllegalArgumentException("The model number, " + modelNumber + ", must lie between 1 and " + Impedance.numberOfModels + " inclusive");
        this.modelNumber = modelNumber;
        this.parameters = parameters;
        this.modelParameterSymbols = Impedance.modelComponents(modelNumber);
        this.numberOfParameters = modelParameterSymbols.length;
        if(this.numberOfParameters != this.parameters.length)throw new IllegalArgumentException("The number of model parametes passed, " + parameters.length + ", does not match the number required, " + this.numberOfParameters + ", by model number " + modelNumber);
        this.parametersSet = true;
        this.modelSet = true;
    }

    // set model number - parameters supplied as an array - parameter symbols also supplied
    public void setModel(int modelNumber, double[] parameters, String[] symbols){
        if(modelNumber == 0 || modelNumber>Impedance.numberOfModels)throw new IllegalArgumentException("The model number, " + modelNumber + ", must lie between 1 and " + Impedance.numberOfModels + " inclusive");
        this.modelNumber = modelNumber;
        this.parameters = parameters;
        this.modelParameterSymbols = Impedance.modelComponents(modelNumber);
        this.numberOfParameters = modelParameterSymbols.length;
        if(this.numberOfParameters != this.parameters.length)throw new IllegalArgumentException("The number of model parametes passed, " + parameters.length + ", does not match the numbber required, " + this.numberOfParameters + ", by model number " + modelNumber);
        if(this.numberOfParameters != symbols.length)throw new IllegalArgumentException("The number of model symbols passed, " + symbols.length + ", does not match the number required, " + this.numberOfParameters + ", by model number " + modelNumber);
        this.modelParameterSymbols = symbols;
        this.parametersSet = true;
        this.modelSet = true;
    }

    // set model number  - parameters read in through a dialog box
    public void setModel(int modelNumber){
        if(modelNumber == 0 || modelNumber>Impedance.numberOfModels)throw new IllegalArgumentException("The model number, " + modelNumber + ", must lie between 1 and " + Impedance.numberOfModels + " inclusive");
        this.modelNumber = modelNumber;
        this.modelSet = true;
        this.modelParameterSymbols = Impedance.modelComponents(modelNumber);
        this.numberOfParameters = modelParameterSymbols.length;
        this.parameters = new double[this.numberOfParameters];

        // Read in parameter values
        int ii = 0; // counter
        String symbol = null;
        while(ii<this.numberOfParameters){
            symbol = modelParameterSymbols[ii];
            if(symbol.trim().charAt(0)=='R'){
                parameters[ii] = Db.readDouble("Enter resistance " + symbol.trim() + " [ohms]");
                ii++;
            }
            else{
                if(symbol.trim().charAt(0)=='C'){
                    parameters[ii] = Db.readDouble("Enter capacitance " + symbol.trim() + " [farads]");
                    ii++;
                }
                else{
                    if(symbol.trim().charAt(0)=='L'){
                        parameters[ii] = Db.readDouble("Enter inductance " + symbol.trim() + " [henries]");
                        ii++;
                    }
                    else{
                        if(symbol.trim().charAt(0)=='W'){
                            parameters[ii] = Db.readDouble("Enter 'infinite' Warburg constant, sigma, " + symbol.trim() + " [ohms*sqrt(radians)]");
                            ii++;
                        }
                        else{
                            if(symbol.trim().charAt(0)=='F'){
                                parameters[ii] = Db.readDouble("Enter 'finite' Warburg constant, sigma, " + symbol.trim() + " [SI units]");
                                ii++;

                                parameters[ii] = Db.readDouble("Enter 'finite' Warburg power, alpha, " + symbol.trim());
                                ii++;

                            }
                            else{
                                if(symbol.trim().charAt(0)=='Q'){
                                    parameters[ii] = Db.readDouble("Enter CPE constant, sigma, " + symbol.trim() + " [SI units]");
                                    ii++;

                                    parameters[ii] = Db.readDouble("Enter CPE power, alpha, " + symbol.trim());
                                    ii++;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.parametersSet = true;
    }


    // set user supplied model parameters - supplied as an array - symbols automatically listed as P1, P2 etc
    public void setModel(ImpedSpecModel userModel, double[] parameters){
        this.userModel = userModel;
        this.parameters = parameters;
        this.numberOfParameters = parameters.length;
        this.modelParameterSymbols = new String[this.numberOfParameters];
        for(int i=0; i<numberOfParameters; i++)this.modelParameterSymbols[i] = "P" + (i+1);
        this.parametersSet = true;
    }

    // set user supplied model parameters - supplied as an array - parameter symbols also supplied
    public void setModel(ImpedSpecModel userModel, double[] parameters, String[] symbols){
        this.userModel = userModel;
        this.parameters = parameters;
        this.modelParameterSymbols = symbols;
        this.numberOfParameters = parameters.length;
        this.parametersSet = true;
    }

    // Calculate impedances
    public Complex[] calculateImpedances(){
        if(!parametersSet)throw new IllegalArgumentException("model parameters values have not been entered");
        if(!frequenciesSet)throw new IllegalArgumentException("frequency values have not been entered");
        this.impedances = Complex.oneDarray(this.numberOfFrequencies);
        if(this.modelSet){
            for(int i=0; i<this.numberOfFrequencies; i++){
                this.impedances[i] = Impedance.modelImpedance(this.parameters, this.omegas[i], this.modelNumber);
            }
        }
        else{
            for(int i=0; i<this.numberOfFrequencies; i++){
                this.impedances[i] = this.userModel.modelImpedance(this.parameters, this.omegas[i]);
            }
        }
        this.magnitudesZ = new double[this.numberOfFrequencies];
        this.phasesRadZ = new double[this.numberOfFrequencies];
        this.phasesDegZ = new double[this.numberOfFrequencies];
        this.realZ = new double[this.numberOfFrequencies];
        this.imagZ = new double[this.numberOfFrequencies];
        this.magnitudesV = new double[this.numberOfFrequencies];
        this.phasesRadV = new double[this.numberOfFrequencies];
        this.phasesDegV = new double[this.numberOfFrequencies];
        this.realV = new double[this.numberOfFrequencies];
        this.imagV = new double[this.numberOfFrequencies];
        this.voltages = Complex.oneDarray(this.numberOfFrequencies);
        for(int i=0; i<this.numberOfFrequencies; i++){
            this.magnitudesZ[i] = Complex.abs(this.impedances[i]);
            this.phasesRadZ[i]  = Complex.arg(this.impedances[i]);
            this.phasesDegZ[i]  = Math.toDegrees(this.phasesRadZ[i]);
            this.realZ[i]      = this.impedances[i].getReal();
            this.imagZ[i]      = this.impedances[i].getImag();
            if(this.voltageSet && this.referenceSet){
                this.voltages[i] = this.appliedVoltage.times(this.impedances[i].over(this.impedances[i].plus(this.referenceImpedance)));
                this.magnitudesV[i] = Complex.abs(this.voltages[i]);
                this.phasesRadV[i]  = Complex.arg(this.voltages[i]);
                this.phasesDegV[i]  = Math.toDegrees(this.phasesRadV[i]);
                this.realV[i]      = this.voltages[i].getReal();
                this.imagV[i]      = this.voltages[i].getImag();
            }
        }
        this.impedancesSet = true;
        return this.impedances;
    }

    // Get the simulation results as ArrayList
    // complex impedances, real parts of the complex impedances, the imaginary parts of the complex impedances
    // magnitudes, // phases (degrees), phases (radians),
    // frequencies [Hz], log10(frequencies/Hz), radial frequencies (radians]),
    public ArrayList<Object> getSimulationResultsAsArrayList(int nPoints){

        if(!this.impedancesSet)calculateImpedances();

        // determine points to be printed
        if(nPoints>this.numberOfFrequencies)nPoints = this.numberOfFrequencies;
        int increment = (int)Math.round((double)this.numberOfFrequencies/(double)nPoints);
        int[] points = new int[nPoints];
        points[0] = 0;
        for(int i=1; i<nPoints; i++)points[i] = points[i-1] + increment;
        if(points[nPoints-1] != (this.numberOfFrequencies-1))points[nPoints-1] = this.numberOfFrequencies-1;

        // Load ArrayList with selected data
        ArrayList<Object> selectedData = new ArrayList<Object>();

        Complex[] imp = Complex.oneDarray(nPoints);
        for(int i=0; i<nPoints; i++)imp[i] = impedances[points[i]];
        selectedData.add(Complex.copy(imp));

        double[] hold = new double[nPoints];
        for(int i=0; i<nPoints; i++)hold[i] = realZ[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = imagZ[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = magnitudesZ[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = phasesDegZ[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = phasesRadZ[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = frequencies[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = log10frequencies[points[i]];
        selectedData.add(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = omegas[points[i]];
        selectedData.add(Conv.copy(hold));

        if(this.voltageSet && this.referenceSet){
            selectedData.add(new Double(this.appliedVoltage.getReal()));

            selectedData.add(this.referenceImpedance);

            for(int i=0; i<nPoints; i++)imp[i] = voltages[points[i]];
            selectedData.add(Complex.copy(imp));

            for(int i=0; i<nPoints; i++)hold[i] = realV[points[i]];
            selectedData.add(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = imagV[points[i]];
            selectedData.add(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = magnitudesV[points[i]];
            selectedData.add(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = phasesDegV[points[i]];
            selectedData.add(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = phasesRadV[points[i]];
            selectedData.add(Conv.copy(hold));
        }
        else{
            for(int i=0; i<8; i++)selectedData.add(null);
        }

        return selectedData;
    }



    // Get the simulation results as Vector
    // complex impedances, real parts of the complex impedances, the imaginary parts of the complex impedances
    // magnitudes, // phases (degrees), phases (radians),
    // frequencies [Hz], log10(frequencies/Hz), radial frequencies (radians]),
    public Vector<Object> getSimulationResultsAsVector(int nPoints){

        if(!this.impedancesSet)calculateImpedances();

        // determine points to be printed
        if(nPoints>this.numberOfFrequencies)nPoints = this.numberOfFrequencies;
        int increment = (int)Math.round((double)this.numberOfFrequencies/(double)nPoints);
        int[] points = new int[nPoints];
        points[0] = 0;
        for(int i=1; i<nPoints; i++)points[i] = points[i-1] + increment;
        if(points[nPoints-1] != (this.numberOfFrequencies-1))points[nPoints-1] = this.numberOfFrequencies-1;

        // Load Vector with selected data
        Vector<Object> vec = new Vector<Object>();

        Complex[] imp = Complex.oneDarray(nPoints);
        for(int i=0; i<nPoints; i++)imp[i] = impedances[points[i]];
        vec.addElement(Complex.copy(imp));

        double[] hold = new double[nPoints];
        for(int i=0; i<nPoints; i++)hold[i] = realZ[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = imagZ[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = magnitudesZ[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = phasesDegZ[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = phasesRadZ[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = frequencies[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = log10frequencies[points[i]];
        vec.addElement(Conv.copy(hold));

        for(int i=0; i<nPoints; i++)hold[i] = omegas[points[i]];
        vec.addElement(Conv.copy(hold));

        if(this.voltageSet && this.referenceSet){
            vec.addElement(new Double(this.appliedVoltage.getReal()));

            vec.addElement(this.referenceImpedance);

            for(int i=0; i<nPoints; i++)imp[i] = voltages[points[i]];
            vec.addElement(Complex.copy(imp));

            for(int i=0; i<nPoints; i++)hold[i] = realV[points[i]];
            vec.addElement(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = imagV[points[i]];
            vec.addElement(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = magnitudesV[points[i]];
            vec.addElement(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = phasesDegV[points[i]];
            vec.addElement(Conv.copy(hold));

            for(int i=0; i<nPoints; i++)hold[i] = phasesRadV[points[i]];
            vec.addElement(Conv.copy(hold));
        }
        else{
            for(int i=0; i<8; i++)vec.addElement(null);
        }

        return vec;
    }

    // Get the simulation results as Vector
    // complex impedances, real parts of the complex impedances, the imaginary parts of the complex impedances
    // magnitudes, // phases (degrees), phases (radians),
    // frequencies [Hz], log10(frequencies/Hz), radial frequencies (radians]),
    public Vector<Object> getSimulationResults(int nPoints){
        return this.getSimulationResultsAsVector(nPoints);
    }

    // Plot impedance magnitude versus frequency
    public void plotImpedanceMagnitudes(){
        this.plotImpedanceMagnitudeVersusFrequency();
    }

    // Plot impedance magnitude versus frequency
    public void plotImpedanceMagnitudeVersusFrequency(){
        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecSimulation program:  Impedance Magnitude versus Frequency   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.simulationTitle;
        if(logOrLinear){
            this.impedanceMagnitudeVersusLogFrequencyPlot(graphTitle1, graphTitle2);
        }
        else{
            this.impedanceMagnitudeVersusFrequencyPlot(graphTitle1, graphTitle2);
        }
    }

    // Plot impedance magnitude versus frequency (old signature)
    public void plotMagnitudeVersusFrequency(){
        this.plotImpedanceMagnitudeVersusFrequency();
    }

    // Plot magnitude versus log10 frequency
    private void impedanceMagnitudeVersusLogFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.log10frequencies;
        data[1] = this.magnitudesZ;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Log10[Frequency / Hz]");
        pg.setYaxisLegend("Impedance Magnitude");
        pg.plot();
    }

    // Plot magnitude versus frequency
    private void impedanceMagnitudeVersusFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.frequencies;
        data[1] = this.magnitudesZ;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Frequency");
        pg.setXaxisUnitsName("Hz");
        pg.setYaxisLegend("Impedance Magnitude");
        pg.plot();
    }

    // Plot impedance phase versus frequency
    public void plotImpedancePhases(){
        this.plotImpedancePhaseVersusFrequency();
    }

    // Plot impedance phase versus frequency
    public void plotImpedancePhaseVersusFrequency(){
        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecSimulation program:  Impedance Phase versus Frequency   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.simulationTitle;
        if(logOrLinear){
            this.impedancePhaseVersusLogFrequencyPlot(graphTitle1, graphTitle2);
        }
        else{
            this.impedancePhaseVersusFrequencyPlot(graphTitle1, graphTitle2);
        }
    }

    // Plot impedance phase versus frequency (old signature)
    public void plotPhaseVersusFrequency(){
        this.plotImpedancePhaseVersusFrequency();
    }

    // Plot phase versus log10 frequency
    private void impedancePhaseVersusLogFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.log10frequencies;
        data[1] = this.phasesDegZ;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Log10[Frequency / Hz]");
        pg.setYaxisLegend("Impedance Phase");
        pg.setYaxisUnitsName("degrees");
        pg.plot();
    }

    // Plot phase versus frequency
    private void impedancePhaseVersusFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.frequencies;
        data[1] = this.phasesDegZ;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Frequency");
        pg.setXaxisUnitsName("Hz");
        pg.setYaxisLegend("Impedance Phase");
        pg.setYaxisUnitsName("degrees");
        pg.plot();
    }

    // Cole-Cole Plot
    public void plotColeCole(){
        String[] dAndT= this.dateAndTime();
        String graphTitle1 = "ImpedSpecSimulation program:  Cole - Cole plot   [" + dAndT[0] + "    " + dAndT[1] + "]";
        String graphTitle2 = this.simulationTitle;
        this.coleColePlot(graphTitle1, graphTitle2);
    }


    // Cole-Cole Plot
    private void coleColePlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        for(int i=0; i<this.numberOfFrequencies; i++){
            data[0][i] = this.realZ[this.numberOfFrequencies - i - 1];
            data[1][i] = -this.imagZ[this.numberOfFrequencies - i - 1];
        }
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Real[Impedance / ohms]");
        pg.setYaxisLegend("-Imag[Impedance / ohms]");
        pg.plot();
    }


    // Plot voltage magnitude versus frequency
    public void plotVoltageMagnitudes(){
        this.plotVoltageMagnitudeVersusFrequency();
    }

    // Plot voltage magnitude versus frequency
    public void plotVoltageMagnitudeVersusFrequency(){

        if(this.voltageSet && this.referenceSet){
            String[] dAndT= this.dateAndTime();
            String graphTitle1 = "ImpedSpecSimulation program:  Voltage Magnitude versus Frequency   [" + dAndT[0] + "    " + dAndT[1] + "]";
            String graphTitle2 = this.simulationTitle;
            if(logOrLinear){
                this.voltageMagnitudeVersusLogFrequencyPlot(graphTitle1, graphTitle2);
            }
            else{
                this.voltageMagnitudeVersusFrequencyPlot(graphTitle1, graphTitle2);
            }
        }
        else{
            System.out.println("A Voltage phase plot cannot be displayed, either no applied");
            System.out.println("voltage and/or reference impedance has been entered");
        }

    }


    // Plot voltage magnitude versus log10 frequency
    private void voltageMagnitudeVersusLogFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.log10frequencies;
        data[1] = this.magnitudesV;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Log10[Frequency / Hz]");
        pg.setYaxisLegend("Voltage Magnitude");
        pg.plot();
    }

    // Plot voltage magnitude versus frequency
    private void voltageMagnitudeVersusFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.frequencies;
        data[1] = this.magnitudesV;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Frequency");
        pg.setXaxisUnitsName("Hz");
        pg.setYaxisLegend("Voltage Magnitude");
        pg.plot();
    }


    // Plot voltage phase versus frequency
    public void plotVoltagePhases(){
        this.plotVoltagePhaseVersusFrequency();
    }

    // Plot voltage phase versus frequency
    public void plotVoltagePhaseVersusFrequency(){

        if(this.voltageSet && this.referenceSet){
            String[] dAndT= this.dateAndTime();
            String graphTitle1 = "ImpedSpecSimulation program:  Voltage Phase versus Frequency   [" + dAndT[0] + "    " + dAndT[1] + "]";
            String graphTitle2 = this.simulationTitle;
            if(logOrLinear){
                this.voltagePhaseVersusLogFrequencyPlot(graphTitle1, graphTitle2);
            }
            else{
                this.voltagePhaseVersusFrequencyPlot(graphTitle1, graphTitle2);
            }
        }
        else{
            System.out.println("A Voltage phase plot cannot be displayed, either no applied");
            System.out.println("voltage and/or reference impedance has been entered");
        }
    }


    // Plot voltage phase versus log10 frequency
    private void voltagePhaseVersusLogFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.log10frequencies;
        data[1] = this.phasesDegV;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Log10[Frequency / Hz]");
        pg.setYaxisLegend("Voltage Phase");
        pg.setYaxisUnitsName("degrees");
        pg.plot();
    }

    // Plot phase versus frequency
    private void voltagePhaseVersusFrequencyPlot(String graphTitle1, String graphTitle2){

        if(!this.impedancesSet)calculateImpedances();

        double[][] data = new double[2][this.numberOfFrequencies];
        data[0] = this.frequencies;
        data[1] = this.phasesDegV;
        PlotGraph pg = new PlotGraph(data);
        pg.setLine(3);
        pg.setPoint(0);
        pg.setGraphTitle(graphTitle1);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Frequency");
        pg.setXaxisUnitsName("Hz");
        pg.setYaxisLegend("Voltage Phase");
        pg.setYaxisUnitsName("degrees");
        pg.plot();
    }

    // Returns date and time
    public String[] dateAndTime(){
        Date d = new Date();
        String[] ret = new String[2];
        ret[0] = DateFormat.getDateInstance().format(d);
        ret[1] = DateFormat.getTimeInstance().format(d);
        return ret;
    }

    // Print simulation to text file - nPoints is the number of points to be printed
    public void printToTextFile(int nPoints){
        String fileName = "ImpedSpecSimulationOutput.txt";
        this.fileType = true;
        this.printToTextFile(fileName, nPoints);
    }

    // Print simulation to text file - nPoints is the number of points to be printed
    public void print(int nPoints){
        String fileName = "ImpedSpecSimulationOutput.txt";
        this.fileType = true;
        this.printToTextFile(fileName, nPoints);
    }

    // Print simulation to text file - nPoints is the number of points to be printed
    public void print(String fileName, int nPoints){
        this.printToTextFile(fileName, nPoints);
    }

    // Print simulation to text file - nPoints is the number of points to be printed
    public void printToTextFile(String fileName, int nPoints){

        if(!this.impedancesSet)calculateImpedances();

        int field = 10;   // output field length
        int trunc = 4;    // truncation length

        // Check extension
        fileName = fileName.trim();
        int dotPosition = fileName.indexOf('.');
        if(dotPosition==-1)fileName += ".txt";

        // determine points to be printed
        if(nPoints>this.numberOfFrequencies)nPoints = this.numberOfFrequencies;
        int increment = (int)Math.round((double)this.numberOfFrequencies/(double)nPoints);
        int[] points = new int[nPoints];
        points[0] = 0;
        for(int i=1; i<nPoints; i++)points[i] = points[i-1] + increment;
        if(points[nPoints-1] != (this.numberOfFrequencies-1))points[nPoints-1] = this.numberOfFrequencies-1;

        // instantiate a FileOutput
        FileOutput fout = null;
        if(this.fileType){
            fout = new FileOutput(fileName, 'n');
        }
        else{
            fout = new FileOutput(fileName);
        }

        // print header
        fout.println("ImpedSpecSimulation Program Output File:  " + this.simulationTitle);
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
        fout.printtab("Parameters");
        fout.println("Value (SI unit)");
        for(int i=0; i<this.numberOfParameters; i++){
            fout.printtab(this.modelParameterSymbols[i], field);
            fout.println(this.parameters[i]);
        }
        fout.println();

        // Print impedance data for each frequency
        field=14;
        fout.println("Frequecy - Impedance data");

        fout.print("Frequency", field);
        fout.print("Magnitude", field);
        fout.print("Phase", field);
        fout.print("Phase", field);
        fout.print("Real[Z]", field);
        fout.print("Imag[Z]", field);
        fout.print("Log10(freq)", field);
        fout.println("Radial frequency");

        fout.print("/Hz [freq]", field);
        fout.print("  ", field);
        fout.print("/degrees", field);
        fout.print("/radians", field);
        fout.print("/ohms", field);
        fout.print("/ohms", field);
        fout.print("  ", field);
        fout.println("/radians");

        for(int i=0; i<nPoints; i++){
            fout.print(Fmath.truncate(this.frequencies[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.magnitudesZ[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.phasesDegZ[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.phasesRadZ[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.realZ[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.imagZ[points[i]], trunc), field);
            fout.print(Fmath.truncate(this.log10frequencies[points[i]], trunc), field);
            fout.println(Fmath.truncate(this.omegas[points[i]], trunc));
        }
        fout.println();

        if(this.voltageSet && this.referenceSet){
            fout.println("Aplied voltage: " + this.appliedVoltage.getReal() + " volts");
            fout.println();

            fout.println("Reference impedance: " + this.referenceImpedance + " ohms");
            fout.println();

            // Print voltage data for each frequency
            field=14;
            fout.println("Frequecy - Voltage data");

            fout.print("Frequency", field);
            fout.print("Magnitude", field);
            fout.print("Phase", field);
            fout.print("Phase", field);
            fout.print("Real[V]", field);
            fout.print("Imag[V]", field);
            fout.print("Log10(freq)", field);
            fout.println("Radial frequency");

            fout.print("/Hz [freq]", field);
            fout.print("  ", field);
            fout.print("/degrees", field);
            fout.print("/radians", field);
            fout.print("/volts", field);
            fout.print("/volts", field);
            fout.print("  ", field);
            fout.println("/radians");

            for(int i=0; i<nPoints; i++){
                fout.print(Fmath.truncate(this.frequencies[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.magnitudesV[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.phasesDegV[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.phasesRadV[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.realV[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.imagV[points[i]], trunc), field);
                fout.print(Fmath.truncate(this.log10frequencies[points[i]], trunc), field);
                fout.println(Fmath.truncate(this.omegas[points[i]], trunc));
            }
        }

        // close file
        fout.close();
    }

    // Print simulation to text file that can be sensibly read by MS Excel - nPoints is the number of points to be printed
    public void printToExcelFile(int nPoints){
        String fileName = "ImpedSpecSimulationOutput.xls";
        this.fileType = true;
        this.printToExcelFile(fileName, nPoints);
    }

    // Print simulation to text file that can be sensibly read by MS Excel - nPoints is the number of points to be printed
    public void printForExcel(int nPoints){
        String fileName = "ImpedSpecSimulationOutput.xls";
        this.fileType = true;
        this.printToExcelFile(fileName, nPoints);
    }

    // Print simulation to text file that can be sensibly read by MS Excel - nPoints is the number of points to be printed
    public void printForExcel(String fileName, int nPoints){
        this.printToExcelFile(fileName, nPoints);
    }

    // Print simulation to text file that can be sensibly read by MS Excel - nPoints is the number of points to be printed
    public void printToExcelFile(String fileName, int nPoints){

        if(!this.impedancesSet)calculateImpedances();

        int field = 10;   // output field length
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

        // determine points to be printed
        if(nPoints>this.numberOfFrequencies)nPoints = this.numberOfFrequencies;
        int increment = (int)Math.round((double)this.numberOfFrequencies/(double)nPoints);
        int[] points = new int[nPoints];
        points[0] = 0;
        for(int i=1; i<nPoints; i++)points[i] = points[i-1] + increment;
        if(points[nPoints-1] != (this.numberOfFrequencies-1))points[nPoints-1] = this.numberOfFrequencies-1;

        // instantiate a FileOutput
        FileOutput fout = null;
        if(this.fileType){
            fout = new FileOutput(fileName, 'n');
        }
        else{
            fout = new FileOutput(fileName);
        }

        // print header
        fout.println("ImpedSpecSimulation Program Output File:  " + this.simulationTitle);
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
        fout.printtab("Parameters");
        fout.println("Value (SI unit)");
        for(int i=0; i<this.numberOfParameters; i++){
            fout.printtab(this.modelParameterSymbols[i], field);
            fout.println(this.parameters[i]);
        }
        fout.println();

        // Print impedance data for each frequency
        field=10;
        fout.println("Frequecy - Impedance data");

        fout.printtab("Frequency", field);
        fout.printtab("Magnitude", field);
        fout.printtab("Phase", field);
        fout.printtab("Phase", field);
        fout.printtab("Real[Z]", field);
        fout.printtab("Imag[Z]", field);
        fout.printtab("Log10(freq)", field);
        fout.println("Radial frequency");

        fout.printtab("/Hz [freq]", field);
        fout.printtab("  ", field);
        fout.printtab("/degrees", field);
        fout.printtab("/radians", field);
        fout.printtab("/ohms", field);
        fout.printtab("/ohms", field);
        fout.printtab("  ", field);
        fout.println("/radians");

        for(int i=0; i<nPoints; i++){
            fout.printtab(Fmath.truncate(this.frequencies[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.magnitudesZ[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.phasesDegZ[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.phasesRadZ[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.realZ[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.imagZ[points[i]], trunc), field);
            fout.printtab(Fmath.truncate(this.log10frequencies[points[i]], trunc), field);
            fout.println(Fmath.truncate(this.omegas[points[i]], trunc));
        }
        fout.println();

        if(this.voltageSet && this.referenceSet){
            fout.println("Aplied voltage: " + this.appliedVoltage.getReal() + " volts");
            fout.println();

            fout.println("Reference impedance: " + this.referenceImpedance + " ohms");
            fout.println();

            // Print voltage data for each frequency
            field=14;
            fout.println("Frequecy - Voltage data");

            fout.printtab("Frequency", field);
            fout.printtab("Magnitude", field);
            fout.printtab("Phase", field);
            fout.printtab("Phase", field);
            fout.printtab("Real[V]", field);
            fout.printtab("Imag[V]", field);
            fout.printtab("Log10(freq)", field);
            fout.println("Radial frequency");

            fout.printtab("/Hz [freq]", field);
            fout.printtab("  ", field);
            fout.printtab("/degrees", field);
            fout.printtab("/radians", field);
            fout.printtab("/volts", field);
            fout.printtab("/volts", field);
            fout.printtab("  ", field);
            fout.println("/radians");

            for(int i=0; i<nPoints; i++){
                fout.printtab(Fmath.truncate(this.frequencies[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.magnitudesV[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.phasesDegV[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.phasesRadV[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.realV[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.imagV[points[i]], trunc), field);
                fout.printtab(Fmath.truncate(this.log10frequencies[points[i]], trunc), field);
                fout.println(Fmath.truncate(this.omegas[points[i]], trunc));
            }
        }

        // close file
        fout.close();
    }
}




