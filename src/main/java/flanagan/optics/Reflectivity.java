/*
*   Reflectivity Class
*
*   Methods for calculating the reflection of light from
*   the surface of a multilayer of dielectic and/or metal layers
*   and of fitting experimental data to a reflectivity, transmissivity
*   or evanescent field strength scan against angle or wavelength
*
*   Methods for fitting data to a reflectivty, transmissivitty or evanescent field
*   scan over a range of incident angles at a single wavelength
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: February/March/April 2006
*   Developed from earlier C++ and Fortran programs
*   Revised  26 April 2006, 5-7 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Refflectivity.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) March 2006   Michael Thomas Flanagan
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

import flanagan.complex.*;
import flanagan.math.Fmath;
import flanagan.math.Conv;
import flanagan.plot.*;
import flanagan.analysis.*;
import java.util.ArrayList;
import java.lang.reflect.Array;

public class Reflectivity{

    // CLASS VARIABLES

    private int numberOfLayers = 0;                 // number of layers including the semi-infinite layer at both outer faces
    private int numberOfInterfaces = 0;             // number of interlayer interfaces, i.e. number of layers minus one

    private Complex[][] refractiveIndices = null;   // refractive indices of the layers at all wavelengths (number of wavelength, number of layers)
    private Complex[] meanRefractiveIndices = null; // mean refractive indices of each layer
    private boolean refractSet = false;             // = true when refractive indices entered
    private boolean[] refractLayerSet = null;       // = true for layer i when refractive indices entered for layer i
    private boolean meanRefractUsed = false;        // = true when mean refractive indices used

    private Complex[][] relativeMagneticPermeabilities = null;  // relative magnetic permeabilities of the layers
                                                                // default values = 1.0
    private Complex[] meanRelativeMagneticPermeabilities = null;// mean relative magnetic permeabilities of the layers
    private boolean magneticSet = false;             // = true when relative magnetic permeabilities entered
    private boolean meanMagneticUsed = false;        // = true when mean relative magnetic permeabilities used

    private double[][] absorptionCoefficients = null;   // absorption coefficents of the layers at all wavelengths
                                                        // default values = 0.0;
    private boolean absorbSet = false;              // = true when absorbtivity or extinction coefficients entered

    private double[] thicknesses = null;            // thicknesses (in metres) of the layers
    private double[] distances = null;              // cumulative thicknesses (in metres)
    private boolean thickSet = false;               // = true when thicknesses entered
    private boolean[] thickLayerSet = null;         // = true for layer i when thicknesses entered for layer i

    private int numberOfWavelengths = 0;            // number of wavelengths in wavelength scan
    private double[] wavelengths = null;            // wavelengths (in metres) in scan
    private double[] frequencies = null;            // frequencies (in Hz) in scan
    private double[] omega = null;                  // radial frequencies (in radians) in scan
    private int[] origWavelIndices = null;          // indices of the wavelengths as entered before sort into lowest to highest
    private boolean wavelSet = false;               // = true when wavelengths entered
    private boolean freqSet = false;                // = true when frequencies entered
    private boolean wavelNumberSet = false;         // = true when number of wavelengths set

    private double[] incidentAngleDeg = null;       // incident angles measured from the normal in degrees
    private double[] incidentAngleRad = null;       // incident angles measured from the normal in radians
    private int[] incidentAngleIndices = null;      // indices of original incident angles after sorting into ascending order
    private int numberOfIncidentAngles = 0;         // number of incident angles in angular scan
    private boolean incidentAngleSet = false;       // = true when the incident angles have been entered

    private String mode = null;                     // polarisation mode: TE, TM,  unpolarised or mixed
    private double eVectorAngleDeg = 0.0D;          // angle between the electric vector of the incident light and
                                                    // the plane normal to the reflecting surfac in degrees
    private double eVectorAngleRad = 0.0D;          // angle between the electric vector of the incident light and
                                                    // the plane normal to the reflecting surfac in radians
    private double teFraction = 0.0D;               // fraction of light in the TE mode
    private double tmFraction = 0.0D;               // fraction of light in the TM mode
    private boolean modeSet = false;                // = true when mode set

    private Complex[][][] koVector = null;          // ko vector [wavelength] [incident angle] [layer]
    private Complex[][][] kVector = null;           // k vector  [wavelength] [incident angle] [layer]
    private Complex[][][] kxVector = null;          // kx vector [wavelength] [incident angle] [layer]
    private Complex[][][] kzVector = null;          // kz vector [wavelength] [incident angle] [layer]

    private double[][] reflectivities = null;       // reflectivities   [wavelength] [incident angle]
    private double[][] transmissivities = null;     // transmissivities [wavelength] [incident angle]
    private double[][] powerLosses = null;          // power loss on transmission relative to an input power of 1mW over 1 sq. metre [wavelength] [incident angle]
    private Complex[][] reflectCoeffTE = null;      // TE reflection coefficient [wavelength] [incident angle]
    private Complex[][] reflectCoeffTM = null;      // TM reflection coefficient [wavelength] [incident angle]
    private Complex[][] transmitCoeffTE = null;     // TE transmission coefficient [wavelength] [incident angle]
    private Complex[][] transmitCoeffTM = null;     // TM transmissiom coefficient [wavelength] [incident angle]

    private double[][] reflectPhaseShiftRadTE = null;   // TE reflection phase shift (radians) [wavelength] [incident angle]
    private double[][] reflectPhaseShiftRadTM = null;   // TM reflection phase shift (radians) [wavelength] [incident angle]
    private double[][] transmitPhaseShiftRadTE = null;  // TE transmission phase shift (radians) [wavelength] [incident angle]
    private double[][] transmitPhaseShiftRadTM = null;  // TM transmissiom phase shift (radians) [wavelength] [incident angle]
    private double[][] reflectPhaseShiftDegTE = null;   // TE reflection phase shift (degrees) [wavelength] [incident angle]
    private double[][] reflectPhaseShiftDegTM = null;   // TM reflection phase shift (degrees) [wavelength] [incident angle]
    private double[][] transmitPhaseShiftDegTE = null;  // TE transmission phase shift (degrees) [wavelength] [incident angle]
    private double[][] transmitPhaseShiftDegTM = null;  // TM transmissiom phase shift (degrees) [wavelength] [incident angle]

    private double[][] evanescentFields = null;     // integrated evanescent fields [wavelength] [incident angle]
    private double fieldDistance = Double.POSITIVE_INFINITY;    // distance into evanescent field over which intensity is integrated
    private boolean fieldIntensityCalc= false;      // = true when field intensity calculated for non-infinite field distance
    private double[][] penetrationDepths= null;     // Evanescent field penetration depth [wavelength] [incident angle]
    private double[][] transmitAnglesRad = null;    // transmitted angles in radians [wavelength] [incident angle]
    private double[][] transmitAnglesDeg = null;    // transmitted angles in degrees [wavelength] [incident angle]

    private boolean singleReflectCalculated = false;            // = true when only a single angular relectivity has been calculated
    private boolean angularReflectCalculated = false;           // = true when an angular relectivity scan has been calculated
    private boolean wavelengthReflectCalculated = false;        // = true when an wavelength relectivity scan has been calculated
    private boolean wavelengthAndAngularReflectCalculated = false;  // = true when angular for each wavelength relectivity scan has been calculated

    private double mu0overEps0 = Fmath.MU_0/Fmath.EPSILON_0;    // permeability of free space over permittivity of free space
    private double impedance = Math.sqrt(mu0overEps0);          // characteristic impedance of free space

    private int wavelengthAxisOption = 1;           // = 1 when wavelength/frequency x-axis in plotting methods = wavelength
                                                    // = 2 when wavelength/frequency x-axis in plotting methods = frequency, Hz
                                                    // = 3 when wavelength/frequency x-axis in plotting methods = radians

    private double[] experimentalData = null;       // experimental data [incident angle]
    private double[] experimentalWeights = null;    // error in each experimental point [incident angle]
    private double[] calculatedData = null;         // calculated data [incident angle]

    private int numberOfDataPoints = 0;             // number of experimental data points
    private boolean experimentalDataSet = false;    // = true when experimental data entered
    private boolean weightingOption = false;        // = true if experimental weightings (other than unity) entered

    private int numberOfEstimatedParameters = 0;            // number of parameters to be estimated in non-linear regression method
    private int[] thicknessEstimateIndices = null;          // indices of the thicknesses to be estimated by non-linear regression
    private int[] refractIndexRealEstimateIndices = null;   // indices of the Real[refractive indices] to be estimated by non-linear regression
    private int[] refractIndexImagEstimateIndices = null;   // indices of the Imag[refractive indices] to be estimated by non-linear regression
    private int[] absorptionCoeffEstimateIndices = null;    // indices of the absorption coefficients to be estimated by non-linear regression
    private int[] magneticPermRealEstimateIndices = null;   // indices of the Real[relative magnetic permeability] to be estimated by non-linear regression
    private int[] magneticPermImagEstimateIndices = null;   // indices of the Imag[relative magnetic permeability] to be estimated by non-linear regression

    private boolean refractIndexImagEstimateSet = false;    // = true when indices of the Imag[refractive indices] set
    private boolean absorptionCoeffEstimateSet = false;     // = true when indices of the absorption coefficients set

    private int thicknessEstimateNumber = 0;        // number of the thicknesses to be estimated by non-linear regression
    private int refractIndexRealEstimateNumber = 0; // number of the Real[refractive indices] to be estimated by non-linear regression
    private int refractIndexImagEstimateNumber = 0; // number of the Imag[refractive indices] to be estimated by non-linear regression
    private int absorptionCoeffEstimateNumber = 0;  // number of the absorption coefficients to be estimated by non-linear regression
    private int magneticPermRealEstimateNumber = 0; // number of the Real[relative magnetic permeabilities] to be estimated by non-linear regression
    private int magneticPermImagEstimateNumber = 0; // number of the Imag[relative magnetic permeabilities] to be estimated by non-linear regression

    private double fieldScalingFactor = 0.0D;       // scaling factor between calculated and experimental field values

    public int regressionOption = 0;                // Regression option
                                                    // = 1; reflectivity versus angle
                                                    // = 2; transmissivity versus angle
                                                    // = 3; evanescent field versus angle
    public int degreesOfFreedom = 0;                // degrees of freedom (non-linear regression method)

    // CONSTRUCTOR

    public Reflectivity(int n){
        this.numberOfLayers = n;
        this.numberOfInterfaces = n-1;
        if(n<2)throw new IllegalArgumentException("There must be at least two layers, i.e. at least one interface");

        this.meanRelativeMagneticPermeabilities = Complex.oneDarray(this.numberOfLayers, 1.0D, 0.0D);

        this.meanRefractiveIndices = Complex.oneDarray(this.numberOfLayers);
        this.refractLayerSet = new boolean[this.numberOfLayers];
        for(int i=0; i<this.numberOfLayers; i++)this.refractLayerSet[i] = false;

        this.thicknesses = new double[this.numberOfLayers];
        this.thicknesses[0] = Double.NEGATIVE_INFINITY;
        this.thicknesses[this.numberOfLayers-1] = Double.POSITIVE_INFINITY;
        this.thickLayerSet = new boolean[this.numberOfLayers];
        this.thickLayerSet[0] = true;
        for(int i=1; i<this.numberOfLayers-2; i++)this.thickLayerSet[i] = false;
        this.thickLayerSet[this.numberOfLayers-1] = true;

        this.distances = new double[this.numberOfInterfaces];
    }

    // POLARISATION MODE

    // Enter polarisation mode - TE or TM or unpolarised
    public void setMode(String mode){
        if(mode.equalsIgnoreCase("TE")  ||  mode.equalsIgnoreCase("transverse electric")){
            this.mode = "TE";
            this.teFraction = 1.0D;
            this.tmFraction = 0.0D;
            this.eVectorAngleDeg = 0.0D;
            this.eVectorAngleRad = 0.0D;
        }
        else{
            if(mode.equalsIgnoreCase("TM")  ||  mode.equalsIgnoreCase("transverse magnetic")){
                this.mode = "TM";
                this.teFraction = 0.0D;
                this.tmFraction = 1.0D;
                this.eVectorAngleDeg = 90.0D;
                this.eVectorAngleRad = Math.PI/2.0D;
            }
            else{
                if(mode.equalsIgnoreCase("unpolarised")  ||  mode.equalsIgnoreCase("unpolarized")  ||  mode.equalsIgnoreCase("none")){
                    this.mode = "unpolarised";
                    this.teFraction = 0.5D;
                    this.tmFraction = 0.5D;
                    this.eVectorAngleDeg = 45.0D;
                    this.eVectorAngleRad = Math.PI/4.0D;
                }
                else{
                    throw new IllegalArgumentException("mode must be TE, TM or unpolarised; it cannot be " + mode);
                }
            }
        }
        this.modeSet = true;
    }

    // Enter angle between incident light electric vector and the plane normal to the reflecting plane  in degrees
    public void setMode(double modeAngle){
        this.mode = "mixed";
        this.eVectorAngleDeg = modeAngle;
        this.eVectorAngleRad = Math.toRadians(modeAngle);
        this.teFraction = Math.sin(this.eVectorAngleRad);
        this.teFraction *= this.teFraction;
        this.tmFraction = 1.0D - this.teFraction;
        this.modeSet = true;
    }

    // Return the fractional TE component
    public double fractionInTEmode(){
        return this.teFraction;
    }

    // Return the fractional TM component
    public double fractionInTMmode(){
        return this.tmFraction;
    }

    // INCIDENT ANGLES

    // Enter a single incident angle (degrees)
    public void setIncidentAngle(double incidentAngle){
        double[] incident = {incidentAngle};
        this.setIncidentAngle(incident);
    }

    // Enter an array of incident angles (degrees)
    public void setIncidentAngle(double[] incidentAngle){
        this.numberOfIncidentAngles = incidentAngle.length;
        this.incidentAngleIndices = new int[this.numberOfIncidentAngles];
        this.incidentAngleDeg = new double[this.numberOfIncidentAngles];
        Fmath.selectionSort(incidentAngle, this.incidentAngleDeg, this.incidentAngleIndices);
        if(this.experimentalDataSet){
            if(this.numberOfDataPoints!=this.numberOfIncidentAngles)throw new IllegalArgumentException("Number of experimental reflectivities " + this.numberOfDataPoints + " does not equal the number of incident angles " + this.numberOfIncidentAngles);
            double[] temp = Conv.copy(this.experimentalData);
            for(int i=0; i<this.numberOfIncidentAngles;i++)this.experimentalData[i] = temp[this.incidentAngleIndices[i]];
        }
        this.incidentAngleRad = new double[this.numberOfIncidentAngles];
        for(int i=0; i<this.numberOfIncidentAngles;i++)this.incidentAngleRad[i] = Math.toRadians(this.incidentAngleDeg[i]);
        this.incidentAngleSet = true;
    }

    // Enter a range of nAngles incident angles [degrees] ranging, in equal increments from angleLow to angleHigh
    public void setIncidentAngle(double angleLow, double angleHigh, int nAngles){
        this.numberOfIncidentAngles = nAngles;
        double increment = (angleHigh - angleLow)/(nAngles - 1);
        double[] incidentAngles = new double[nAngles];
        incidentAngles[0] = angleLow;
        for(int i=1; i<nAngles-1; i++)incidentAngles[i] = incidentAngles[i-1] + increment;
        incidentAngles[nAngles-1] = angleHigh;
        this.setIncidentAngle(incidentAngles);
    }

    // Return the incident angles (in degrees)
    public double[] getIncidentAngles(){
        return this.incidentAngleDeg;
    }

    // THICKNESSES

    // Enter layer thicknesses (metres) excluding outer semi-infinite layers
    public void setThicknesses(double[] thick){
        int n = thick.length;
        if(n!=this.numberOfLayers-2)throw new IllegalArgumentException("Number of thicknesses, " + n + ", does not match the number of layers minus the outer two semi-finite layers, " + (this.numberOfLayers-2));
        for(int i=1; i<this.numberOfLayers-1; i++)this.thicknesses[i] = thick[i-1];

        // Calculate distances
        this.distances[0] = 0.0D;
        for(int i=1; i<this.numberOfInterfaces; i++)this.distances[i] = this.distances[i-1] + this.thicknesses[i];

        for(int i=1; i<this.numberOfLayers-2; i++)this.thickLayerSet[i] = true;
        this.thickSet = true;
    }

    // Enter layer thicknesses (metres) for individual layer
    public void setThicknesses(double thickness, int layerNumber){
        if(layerNumber<1 || layerNumber>this.numberOfLayers)throw new IllegalArgumentException("Layer number, " + layerNumber + ", must be in the range 1 to "+this.numberOfLayers);
        this.thicknesses[layerNumber-1] = thickness;

        // Recalculate distances
        this.distances[0] = 0.0D;
        for(int i=1; i<this.numberOfInterfaces; i++)this.distances[i] = this.distances[i-1] + this.thicknesses[i];

        this.thickLayerSet[layerNumber-1] = true;
        int check = 0;
        for(int i=0; i<this.numberOfLayers-i; i++)if(this.thickLayerSet[i])check++;
        if(check==this.numberOfLayers)this.thickSet = true;
    }

    // Return the layer thicknesses
    public double[] getThicknesses(){
        return this.thicknesses;
    }

    // WAVELENGTH AND FREQUENCIES

    // Enter wavelengths (metres)
    public void setWavelength(double[] wavelengths){
        // set wavelengths
        int n = wavelengths.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("The number of wavelengths entered, " + n + ", does not equal that previously set," + this.numberOfWavelengths);
        }
        this.numberOfWavelengths = n;
        this.wavelengths = wavelengths;
        this.wavelSet = true;

        // set refractive indices array dimensions
        if(!refractSet)this.refractiveIndices = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);

        // fill out mean refractive indices if necessary
        if(!this.wavelNumberSet){
            // Fill out refractive index arrays if mean values have been entered
            if(this.meanRefractUsed){
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.refractiveIndices[j][i] = this.meanRefractiveIndices[i];
                    }
                }
                for(int i=0; i<this.numberOfLayers; i++)this.refractLayerSet[i] = true;
                this.refractSet = true;
            }

            // Calculate imaginary refractive indices arrays if absorption coefficients have been entered
            // or otherwise set all absorption coefficients to zero
            if(this.absorptionCoefficients!=null){
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        if(this.refractiveIndices[i][j].getImag()==0.0D)this.refractiveIndices[j][i].setImag(absorptionCoefficients[j][i]*this.wavelengths[j]/(4.0D*Math.PI));
                    }
                }
            }
            else{
                this.absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];
            }

            // Fill out relative magnetic permeability arrays if empty
            this.relativeMagneticPermeabilities = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
            if(this.meanMagneticUsed){
                // if mean values have been entered
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.relativeMagneticPermeabilities[j][i] = this.meanRelativeMagneticPermeabilities[i];
                    }
                }
                this.magneticSet = true;
            }
            else{
                // if no values have been entered set all to unity
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.relativeMagneticPermeabilities[j][i] = Complex.plusOne();
                    }
                }
            }
        }

        // calculate frequencies
        if(!freqSet){
            this.frequencies = new double[this.numberOfWavelengths];
            for(int i=0; i<this.numberOfWavelengths; i++)this.frequencies[this.numberOfWavelengths-1-i] = Fmath.C_LIGHT/wavelengths[i];
        }

        // calculate radial frequencies
        this.omega = new double[this.numberOfWavelengths];
        for(int i=0; i<this.numberOfWavelengths; i++)this.omega[i] = 2.0D*Math.PI*frequencies[i];

        this.wavelNumberSet = true;
    }

    // Enter frequencies (Hz)
    public void setFrequency(double[] frequency){
        // set frequencies
        int n = frequency.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("The number of frequencies entered, " + n + ", does not equal that previously set," + this.numberOfWavelengths);
        }
        this.frequencies = frequency;
        this.freqSet = true;
        this.wavelengthAxisOption = 2;

        // set wavelengths
        double[] wavelength = new double[n];
        for(int i=0; i<n; i++)wavelength[i] = Fmath.C_LIGHT/frequencies[n-1-i];
        this.setWavelength(wavelength);
    }

    // Enter a range of nLambda wavelengths [metres] ranging, in equal increments from lambdaLow to lambdaHigh
    public void setWavelength(double lambdaLow, double lambdaHigh, int nLambda){
        double increment = (lambdaHigh - lambdaLow)/(nLambda - 1);
        double[] wavelength = new double[nLambda];
        wavelength[0] = lambdaLow;
        for(int i=1; i<nLambda-1; i++)wavelength[i] = wavelength[i-1] + increment;
        wavelength[nLambda-1] = lambdaHigh;
        this.setWavelength(wavelength);
    }

    // Enter a range of nFreq frequencies [Hz] ranging, in equal increments from freqLow to freqHigh
    public void setFrequency(double freqLow, double freqHigh, int nFreq){
        double increment = (freqHigh - freqLow)/(nFreq - 1);
        double[] frequency = new double[nFreq];
        frequency[0] = freqLow;
        for(int i=1; i<nFreq-1; i++)frequency[i] = frequency[i-1] + increment;
        frequency[nFreq-1] = freqHigh;
        this.setFrequency(frequency);
    }

    // Enter a single wavelength [metres]
    public void setWavelength(double wavelength){
        double[] wavelengths = {wavelength};
        this.setWavelength(wavelengths);
    }

    // Enter a single frequency [Hz]
    public void setFrequency(double frequency){
        double[] frequencies = {frequency};
        this.setFrequency(frequencies);
    }

    // Return the wavelengths
    public double[] getWavelengths(){
        return this.wavelengths;
    }

    // Return the radial frequencies
    public double[] getRadialFrequencies(){
        return this.omega;
    }

    // Sort wavelengths into increasing order
    // with accompanying matchingrearrangement of the magnetic permeabilities and the absorption coefficients
    private void sortWavelengths(){
        this.origWavelIndices = new int[this.numberOfWavelengths];
        for(int i=0; i<this.numberOfWavelengths; i++)this.origWavelIndices[i] = i;
        if(this.numberOfWavelengths>1){
            // test if not in order
            boolean test0 = true;
            boolean test1 = false;
            int ii = 1;
            while(test0){
                if(this.wavelengths[ii]<this.wavelengths[ii-1]){
                    test0 = false;
                    test1 = true;
                }
                else{
                    ii++;
                    if(ii>=this.numberOfWavelengths)test0 = false;
                }
            }
            if(test1){
                // reorder
                ArrayList arrayl = Fmath.selectSortArrayList(wavelengths);
                this.wavelengths = (double[])arrayl.get(1);
                this.origWavelIndices = (int[])arrayl.get(2);

                Complex[][] tempC = new Complex[this.numberOfWavelengths][this.numberOfLayers];
                for(int i=0; i<this.numberOfWavelengths; i++){
                    for(int j=0; j<this.numberOfLayers; j++){
                        tempC[i][j] = this.refractiveIndices[this.origWavelIndices[i]][j];
                    }
                }
                this.refractiveIndices = Complex.copy(tempC);

                for(int i=0; i<this.numberOfWavelengths; i++){
                    for(int j=0; j<this.numberOfLayers; j++){
                        tempC[i][j] = this.relativeMagneticPermeabilities[this.origWavelIndices[i]][j];
                    }
                }
                this.relativeMagneticPermeabilities = Complex.copy(tempC);

                double[][] tempD = new double[this.numberOfWavelengths][this.numberOfLayers];
                for(int i=0; i<this.numberOfWavelengths; i++){
                    for(int j=0; j<this.numberOfLayers; j++){
                        tempD[i][j] = this.absorptionCoefficients[this.origWavelIndices[i]][j];
                    }
                }
                this.absorptionCoefficients = tempD;
            }
        }
    }

    // REFRACTIVE INDICES

    // Enter all individual refractive indices, as complex numbers
    public void setRefractiveIndices(Complex[][] refractiveIndices){
        int n = refractiveIndices[0].length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of refractive indices layers, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        int m = refractiveIndices.length;
        if(this.wavelSet){
            if(m!=this.numberOfWavelengths)throw new IllegalArgumentException("Number of refractive indices wavelength sets, " + m + ", does not match the number of wavelengths already set, " + this.numberOfWavelengths);
        }

        // set refractive indices
        this.refractiveIndices = refractiveIndices;
        for(int i=0; i<this.numberOfLayers; i++)this.refractLayerSet[i] = true;
        this.refractSet = true;
        this.wavelNumberSet = true;

        // calculate mean refractive index per layer
        for(int i=0; i<this.numberOfLayers; i++){
            Complex sum = Complex.zero();
            for(int j=0; j<this.numberOfWavelengths; j++){
                sum.plusEquals(this.refractiveIndices[j][i]);
            }
            this.meanRefractiveIndices[i] = sum.over(this.numberOfWavelengths);
        }

        // enter imaginary refractive indices if absorption coefficients have been entered
        if(this.wavelSet && this.absorptionCoefficients!=null){
            for(int i=0; i<this.numberOfLayers; i++){
                for(int j=0; j<this.numberOfWavelengths; j++){
                    if(this.refractiveIndices[j][i].getImag()==0.0D)this.refractiveIndices[j][i].setImag(absorptionCoefficients[j][i]*this.wavelengths[i]/(4.0D*Math.PI));
                }
            }
        }
        // otherwise set absorption coefficients to zero
        if(!this.absorbSet)absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];

        // Fill out relative magnetic permeability arrays
        if(!this.magneticSet){
            if(this.meanMagneticUsed){
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.relativeMagneticPermeabilities[j][i] = this.meanRelativeMagneticPermeabilities[i];
                    }
                }
                this.magneticSet = true;
            }
            else{
                this.relativeMagneticPermeabilities = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers, 1.0D, 0.0D);
            }
        }
    }

    // Enter all individual refractive indices, as real numbers
    public void setRefractiveIndices(double[][] refractiveIndices){
        int n = refractiveIndices[0].length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of refractive indices layers, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        int m = refractiveIndices.length;
        if(this.wavelSet){
            if(m!=this.numberOfWavelengths)throw new IllegalArgumentException("Number of refractive indices wavelength sets, " + m + ", does not match the number of wavelengths already set, " + this.numberOfWavelengths);
        }
        Complex[][] complexRefractiveIndices = Complex.twoDarray(m, n);
        for(int i=0; i<m; i++){
            for(int j=0; j<n; j++){
                complexRefractiveIndices[i][j].setReal(refractiveIndices[i][j]);
            }
        }
        this.setRefractiveIndices(complexRefractiveIndices);
    }

    // Enter mean refractive index for each layer, as complex numbers
    public void setRefractiveIndices(Complex[] refractiveIndices){

        // set refractive indices
        int n = refractiveIndices.length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of refrative indices layers, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        this.meanRefractiveIndices = refractiveIndices;
        this.meanRefractUsed = true;

        if(this.wavelNumberSet){
            // Fill out individual refractive index arrays
            for(int i=0; i<this.numberOfLayers; i++){
                for(int j=0; j<this.numberOfWavelengths; j++){
                    this.refractiveIndices[j][i] = this.meanRefractiveIndices[i];
                }
            }
            for(int i=0; i<this.numberOfLayers; i++)this.refractLayerSet[i] = true;
            this.refractSet = true;
        }

        // enter imaginary refractive indices if absorption coefficients have been entered
        if(this.absorptionCoefficients!=null && this.wavelSet){
            for(int i=0; i<this.numberOfLayers; i++){
                for(int j=0; j<this.numberOfWavelengths; j++){
                    if(this.refractiveIndices[j][i].getImag()==0.0D)this.refractiveIndices[j][i].setImag(absorptionCoefficients[j][i]*this.wavelengths[j]/(4.0D*Math.PI));
                }
            }
        }
        // otherwise set absorption coefficients to zero
        if(this.absorptionCoefficients==null)absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];


        // Fill out relative magnetic permeability arrays
        if(!this.magneticSet){
            if(!this.meanMagneticUsed){
                if(this.wavelNumberSet){
                    this.relativeMagneticPermeabilities = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers, 1.0D, 0.0D);
                }
            }
            else{
                this.relativeMagneticPermeabilities = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.relativeMagneticPermeabilities[j][i] = this.meanRelativeMagneticPermeabilities[i];
                    }
                }
                this.magneticSet = true;
            }
        }
    }

    // Enter mean refractive index for each layer, as real numbers
    public void setRefractiveIndices(double[] refractiveIndices){
        int n = refractiveIndices.length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of refrative indices, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        Complex[] complexRefractiveIndices = Complex.oneDarray(n);
        for(int i=0; i<n; i++){
            complexRefractiveIndices[i].setReal(refractiveIndices[i]);
        }
        this.setRefractiveIndices(complexRefractiveIndices);

    }

    // Set refractive indices for an individual layer, as complex numbers
    public void setRefractiveIndices(Complex[] refractiveIndices, int layerNumber){
        if(layerNumber<0 || layerNumber>this.numberOfLayers)throw new IllegalArgumentException("Layer number, " + layerNumber + ", must be in the range 1 to " + this.numberOfLayers);
        int n = refractiveIndices.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("The number of refractive index wavelength values, " + n + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);

        }
        else{
            // Give dimensions to refractive index arrays - fill with mean if known
            this.numberOfWavelengths = n;
            this.wavelNumberSet = true;
            this.refractiveIndices = Complex.twoDarray(this.numberOfLayers, this.numberOfWavelengths);
            if(this.meanRefractUsed){
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.refractiveIndices[j][i]=this.meanRefractiveIndices[i];
                    }
                }
                for(int i=0; i<this.numberOfLayers; i++)this.refractLayerSet[i] = true;
                this.refractSet = true;
            }
            // Give dimensions to relative permeabilities arrays - fill with mean if known
            this.relativeMagneticPermeabilities = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers, 1.0D, 0.0D);

            if(this.meanMagneticUsed){
                for(int i=0; i<this.numberOfLayers; i++){
                    for(int j=0; j<this.numberOfWavelengths; j++){
                        this.relativeMagneticPermeabilities[j][i]=this.meanRelativeMagneticPermeabilities[i];
                    }
                }
                this.magneticSet = true;
            }
        }

        // fill layer values for layer identified in this method's argument list
        layerNumber--;
        this.refractiveIndices[layerNumber] = refractiveIndices;
        this.refractLayerSet[layerNumber] = true;
        int check = 0;
        for(int i=0; i<this.numberOfLayers; i++)if(this.refractLayerSet[i])check++;
        if(check==this.numberOfLayers)this.refractSet=true;

        // set imaginary refractive indices for this layer if some absorption coefficients already entered
        if(this.absorptionCoefficients!=null){
            for(int i=0; i<this.numberOfLayers; i++){
                for(int j=0; j<this.numberOfWavelengths; j++){
                    if(this.refractiveIndices[j][i].getImag()==0.0D)this.refractiveIndices[j][i].setImag(absorptionCoefficients[j][i]*this.wavelengths[j]/(4.0D*Math.PI));
                }
            }
        }
        // otherwise set all absorption coefficients to zero
        if(this.absorptionCoefficients==null)absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];

    }

    // Set refractive indices for an individual layer, as real numbers
    public void setRefractiveIndices(double[] refractiveIndices, int layerNumber){
        if(layerNumber<0 || layerNumber>this.numberOfLayers)throw new IllegalArgumentException("Layer number, " + layerNumber + ", must be in the range 1 to " + this.numberOfLayers);
        int n = refractiveIndices.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("The number of refractive index wavelength values, " + n + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        }
        Complex[] complexRefractiveIndices = Complex.oneDarray(n);
        for(int i=0; i<n; i++){
            complexRefractiveIndices[i].setReal(refractiveIndices[i]);
        }
        this.setRefractiveIndices(complexRefractiveIndices, layerNumber);
    }

    // Set mean refractive indices for an individual layer, as a complex number
    public void setRefractiveIndices(Complex refractiveIndex, int layerNumber){
        if(this.wavelNumberSet){
            Complex[] complexRefractiveIndices = Complex.oneDarray(this.numberOfWavelengths);
            for(int i=0; i<this.numberOfWavelengths; i++){
                complexRefractiveIndices[i] = refractiveIndex;
            }
            this.setRefractiveIndices(complexRefractiveIndices, layerNumber);
        }
        else{
            this.meanRefractiveIndices[layerNumber-1] = refractiveIndex;
            this.meanRefractUsed = true;
        }
    }

    // Set mean refractive indices for an individual layer, as a real number
    public void setRefractiveIndices(double refractiveIndex, int layerNumber){
        Complex complexRefractiveIndex = new Complex(refractiveIndex, 0.0D);
        this.setRefractiveIndices(complexRefractiveIndex, layerNumber);
    }

    // Return refractive indices
    public Object getRefractiveIndices(){
        if(this.numberOfWavelengths==1){
            Complex[] ret = this.refractiveIndices[0];
            return (Object)ret;
        }
        else{
            return (Object)this.refractiveIndices;
        }
    }

    // ABSORPTION COEFFICIENTS

    // Enter absorption coefficients  [default = 0], single wavelength
    public void setAbsorptionCoefficients(double[] absorptionCoefficients){
        // set absorption coefficients arrays
        int n = absorptionCoefficients.length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of absorption coefficients sets, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        this.absorptionCoefficients = new double[1][n];
        this.absorptionCoefficients[0] = absorptionCoefficients;
        this.absorbSet = true;

        if(this.refractSet){
            for(int i=0; i<this.numberOfLayers; i++){
                if(this.refractiveIndices[0][i].getImag()==0.0D)this.refractiveIndices[0][i].setImag(this.absorptionCoefficients[0][i]*this.wavelengths[0]/(4.0D*Math.PI));
            }
        }
    }


    // Enter absorption coefficients  [default = 0],  range of wavelengths
    public void setAbsorptionCoefficients(double[][] absorptionCoefficients){
        // set absorption coefficients arrays
        int n = absorptionCoefficients[0].length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of absorption coefficients sets, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        int m = absorptionCoefficients.length;
        if(this.wavelNumberSet && m!=this.numberOfWavelengths)throw new IllegalArgumentException("Number of absorption coefficients wavelengths, " + m + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        this.absorptionCoefficients = absorptionCoefficients;
        this.absorbSet = true;

        if(this.refractSet && this.wavelSet){
            for(int i=0; i<this.numberOfLayers; i++){
                for(int j=0; j<this.numberOfWavelengths; j++){
                    if(this.refractiveIndices[j][i].getImag()==0.0D)this.refractiveIndices[j][i].setImag(absorptionCoefficients[j][i]*this.wavelengths[j]/(4.0D*Math.PI));
                }
            }
        }
    }

    // Enter absorption coefficients  for a single layer [default = 0], range of wavelengths
    public void setAbsorptionCoefficients(double[] absorptionCoefficients, int layerNumber){
        // set absorption coefficients array
        int n = absorptionCoefficients.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("Layer " + layerNumber + ": number of absorption coefficients wavelengths, " + n + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        }
        else{
            this.numberOfWavelengths = n;
            this.refractiveIndices = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
            this.absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];
        }
        layerNumber--;
        this.absorptionCoefficients[layerNumber] = absorptionCoefficients;
        for(int j=0; j<this.numberOfWavelengths; j++){
            if(this.refractiveIndices[j][layerNumber].getImag()==0.0D)this.refractiveIndices[j][layerNumber].setImag(absorptionCoefficients[j]*this.wavelengths[j]/(4.0D*Math.PI));
        }
        this.absorbSet = true;
    }

    // Enter absorption coefficients  for a single layer [default = 0], single wavelength
    public void setAbsorptionCoefficients(double absorptionCoefficient, int layerNumber){
        // set absorption coefficients array
        if(this.wavelNumberSet){
            if(this.numberOfWavelengths!=1)throw new IllegalArgumentException("Layer " + layerNumber + ": number of absorption coefficients wavelengths, " + 1 + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        }
        else{
            this.numberOfWavelengths = 1;
            this.refractiveIndices = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
            this.absorptionCoefficients = new double[this.numberOfWavelengths][this.numberOfLayers];
        }
        layerNumber--;
        this.absorptionCoefficients[0][layerNumber] = absorptionCoefficient;
        if(this.refractiveIndices[0][layerNumber].getImag()==0.0D)this.refractiveIndices[0][layerNumber].setImag(absorptionCoefficient*this.wavelengths[0]/(4.0D*Math.PI));

        this.absorbSet = true;
    }

    // Return absorption coefficients
    public Object getAbsorptionCoefficients(){

        double [][] absC = this.absorptionCoefficients;
        for(int i=0; i<this.numberOfLayers; i++){
            for(int j=0; j<this.numberOfWavelengths; j++){
                absC[i][j] = 4.0D*Math.PI*this.wavelengths[j]*this.refractiveIndices[i][j].getImag();
            }
        }

        if(this.numberOfWavelengths==1){
            double[] ret = absC[0];
            return (Object)ret;
        }
        else{
            return (Object)absC;
        }
    }

    // RELATIVE MAGNETIC PERMEABILITIES

    // Enter magnetic permeabilities as complex numbers  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(Complex[][] relativeMagneticPermeabilities){
        int n = relativeMagneticPermeabilities[0].length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of relative magnetic permeabilities, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        int m = relativeMagneticPermeabilities.length;
        if(this.wavelNumberSet)if(m!=this.numberOfWavelengths)throw new IllegalArgumentException("Number of relative magnetic permeabilities associated wavelengths, " + m + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        this.relativeMagneticPermeabilities = relativeMagneticPermeabilities;
        this.magneticSet = true;

        // calculate mean permeabilities
        for(int i=0; i<this.numberOfLayers; i++){
            Complex sum = Complex.zero();
            for(int j=0; j<this.numberOfWavelengths; j++){
                sum.plusEquals(this.relativeMagneticPermeabilities[j][i]);
            }
            this.meanRelativeMagneticPermeabilities[i] = sum.over(this.numberOfWavelengths);
        }
    }

    // Enter magnetic permeabilities as real numbers [default values = 1.0]
    public void relativeMagneticPermeabilities(double[][] relativeMagneticPermeabilities){
        int n = relativeMagneticPermeabilities[0].length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of relative magnetic permeabilities, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        int m = relativeMagneticPermeabilities.length;
        if(this.wavelNumberSet)if(m!=this.numberOfWavelengths)throw new IllegalArgumentException("Number of relative magnetic permeabilities associated wavelengths, " + m + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        this.relativeMagneticPermeabilities = Complex.twoDarray(m, n);
        for(int i=0; i<this.numberOfLayers; i++){
            for(int j=0; j<this.numberOfWavelengths; j++){
                this.relativeMagneticPermeabilities[j][i].setReal(relativeMagneticPermeabilities[j][i]);
            }
        }
        this.magneticSet = true;

        // calculate mean permeabilities
        for(int i=0; i<this.numberOfLayers; i++){
            Complex sum = Complex.zero();
            for(int j=0; j<this.numberOfWavelengths; j++){
                sum.plusEquals(this.relativeMagneticPermeabilities[j][i]);
            }
            this.meanRelativeMagneticPermeabilities[i] = sum.over(this.numberOfWavelengths);
        }
    }

    // Enter magnetic permeabilities as a mean value for each layer, complex numbers  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(Complex[] relativeMagneticPermeabilities){
        int n = relativeMagneticPermeabilities.length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of relative magnetic permeabilities, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        this.meanRelativeMagneticPermeabilities = relativeMagneticPermeabilities;
        this.meanMagneticUsed = true;
        if(this.wavelNumberSet)for(int i=0; i<this.numberOfWavelengths; i++)this.relativeMagneticPermeabilities[i] = Complex.copy(relativeMagneticPermeabilities);
    }

    // Enter magnetic permeabilities as a mean value for each layer, real numbers  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(double[] relativeMagneticPermeabilities){
        int n = relativeMagneticPermeabilities.length;
        if(n!=this.numberOfLayers)throw new IllegalArgumentException("Number of relative magnetic permeabilities, " + n + ", does not match the number of layers, " + this.numberOfLayers);
        for(int i=0; i<n; i++)this.meanRelativeMagneticPermeabilities[i].setReal(relativeMagneticPermeabilities[i]);
        this.meanMagneticUsed = true;
        if(this.wavelNumberSet)for(int i=0; i<this.numberOfWavelengths; i++)this.relativeMagneticPermeabilities[i] = Complex.copy(this.meanRelativeMagneticPermeabilities);

    }

    // Enter magnetic permeabilities for a single layer, complex numbers  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(Complex[] relativeMagneticPermeabilities, int layerNumber){
        int n = relativeMagneticPermeabilities.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("Layer " + layerNumber + ": number of relative magnetic permeabilities associated wavelengths, " + n + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        }
        if(this.relativeMagneticPermeabilities==null)this.relativeMagneticPermeabilities = Complex.twoDarray(n, this.numberOfLayers);
        this.relativeMagneticPermeabilities[layerNumber-1] = relativeMagneticPermeabilities;
        Complex sum = Complex.zero();
        for(int i=0; i<n; i++)sum.plusEquals(this.relativeMagneticPermeabilities[i][layerNumber-1]);
        this.meanRelativeMagneticPermeabilities[layerNumber-1] = sum.over(n);
    }

    // Enter magnetic permeabilities for a single layer, real numbers  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(double[] relativeMagneticPermeabilities, int layerNumber){
        int n = relativeMagneticPermeabilities.length;
        if(this.wavelNumberSet){
            if(n!=this.numberOfWavelengths)throw new IllegalArgumentException("Layer " + layerNumber + ": number of relative magnetic permeabilities associated wavelengths, " + n + ", does not match the number of wavelengths already entered, " + this.numberOfWavelengths);
        }
        if(this.relativeMagneticPermeabilities==null)this.relativeMagneticPermeabilities = Complex.twoDarray(n, this.numberOfLayers);
        for(int i=0; i<n; i++)this.relativeMagneticPermeabilities[i][layerNumber-1].setReal(relativeMagneticPermeabilities[i]);
        Complex sum = Complex.zero();
        for(int i=0; i<n; i++)sum.plusEquals(this.relativeMagneticPermeabilities[i][layerNumber-1]);
        this.meanRelativeMagneticPermeabilities[layerNumber-1] = sum.over(n);
    }

    // Enter mean magnetic permeability for a single layer, complex number  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(Complex relativeMagneticPermeability, int layerNumber){
        this.meanRelativeMagneticPermeabilities[layerNumber-1] = relativeMagneticPermeability;
        this.meanMagneticUsed = true;
        if(this.relativeMagneticPermeabilities!=null){
            int n = this.relativeMagneticPermeabilities[0].length;
            for(int i=0; i<n;i++)this.relativeMagneticPermeabilities[i][layerNumber-1]=relativeMagneticPermeability;
        }
    }

    // Enter mean magnetic permeability for a single layer, real number  [default values = 1.0]
    public void setRelativeMagneticPermeabilities(double relativeMagneticPermeability, int layerNumber){
        this.meanRelativeMagneticPermeabilities[layerNumber-1].setReal(relativeMagneticPermeability);
        this.meanMagneticUsed = true;
        if(this.relativeMagneticPermeabilities!=null){
            int n = this.relativeMagneticPermeabilities[0].length;
            for(int i=0; i<n;i++)this.relativeMagneticPermeabilities[i][layerNumber-1]=this.meanRelativeMagneticPermeabilities[layerNumber-1];
        }
    }

    // Return relative magnetic permeabilities
    public Object getRelativeMagneticPermeabilities(){
        if(this.numberOfWavelengths==1){
            Complex[] ret = this.relativeMagneticPermeabilities[0];
            return (Object)ret;
        }
        else{
            return (Object)this.relativeMagneticPermeabilities;
        }
    }

    // REFLECTIVITIES AND REFLECTION COEFFICIENTS

    // Return the reflectivities
    public Object getReflectivities(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectivities[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectivities[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectivities[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectivities;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the TE mode reflection coefficients
    public Object getTEreflectionCoefficients(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectCoeffTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectCoeffTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectCoeffTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectCoeffTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the TM mode reflection coefficients
    public Object getTMreflectionCoefficients(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectCoeffTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectCoeffTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectCoeffTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectCoeffTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // TRANSMISSIVITIES, TRANSMISSION COEFFICIENTS AND TRANSMISSION ANGLES

    // Return the transmissivities
    public Object getTransmissivities(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmissivities[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmissivities[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmissivities[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmissivities;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the power loss on transmission as decibels
    public Object getPowerLoss(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.powerLosses[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.powerLosses[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.powerLosses[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.powerLosses;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the angle of transmitted beam with respect to normal (radians)
    public Object getTransmissionAnglesInRadians(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitAnglesRad[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitAnglesRad[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitAnglesRad[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitAnglesRad;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the angle of transmitted beam with respect to normal (degrees)
    public Object getTransmissionAnglesInDegrees(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitAnglesDeg[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitAnglesDeg[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitAnglesDeg[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitAnglesDeg;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the TE mode transmission coefficients
    public Object getTEtransmissionCoefficients(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitCoeffTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitCoeffTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitCoeffTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitCoeffTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the TM mode transmission coefficients
    public Object getTMtransmissionCoefficients(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitCoeffTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitCoeffTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitCoeffTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitCoeffTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }


    // PHASE SHIFTS

    // Return the phase shifts on reflection (TE mode)in degrees
    public Object getTEreflectionPhaseShiftDeg(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectPhaseShiftDegTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectPhaseShiftDegTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectPhaseShiftDegTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectPhaseShiftDegTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on reflection (TE mode)in radians
    public Object getTEreflectionPhaseShiftRad(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectPhaseShiftRadTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectPhaseShiftRadTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectPhaseShiftRadTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectPhaseShiftRadTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on reflection (TM mode)in degrees
    public Object getTMreflectionPhaseShiftDeg(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectPhaseShiftDegTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectPhaseShiftDegTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectPhaseShiftDegTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectPhaseShiftDegTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on reflection (TM mode)in radians
    public Object getTMreflectionPhaseShiftRad(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.reflectPhaseShiftRadTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.reflectPhaseShiftRadTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.reflectPhaseShiftRadTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.reflectPhaseShiftRadTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on transmission (TE mode)in degrees
    public Object getTEtransmissionPhaseShiftDeg(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitPhaseShiftDegTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitPhaseShiftDegTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitPhaseShiftDegTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitPhaseShiftDegTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on transmission (TE mode)in radians
    public Object getTEtransmissionPhaseShiftRad(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitPhaseShiftRadTE[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitPhaseShiftRadTE[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitPhaseShiftRadTE[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitPhaseShiftRadTE;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on transmission (TM mode)in degrees
    public Object getTMtransmissionPhaseShiftDeg(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitPhaseShiftDegTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitPhaseShiftDegTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitPhaseShiftDegTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitPhaseShiftDegTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the phase shifts on transmission (TM mode)in radians
    public Object getTMtransmissionPhaseShiftRad(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.transmitPhaseShiftRadTM[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.transmitPhaseShiftRadTM[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.transmitPhaseShiftRadTM[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.transmitPhaseShiftRadTM;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }



    // EVANESCENT FIELDS

    // Return the integrated evanescent fields
    public Object getEvanescentFields(double fieldDistance){
        this.fieldDistance = fieldDistance;
        return getEvanescentFields();
    }

    // Return the integrated evanescent fields - default field depth (POSITIVE_INFINITY)
    public Object getEvanescentFields(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.evanescentFields[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.evanescentFields[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.evanescentFields[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.evanescentFields;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the evanescent field penetration depths
    public Object getPenetrationDepths(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.penetrationDepths[0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.penetrationDepths[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    double[] ret = new double[this.numberOfWavelengths];
                    for(int i=0; i<this.numberOfWavelengths; i++)ret[i] = this.penetrationDepths[i][0];
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.penetrationDepths;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // WAVE VECTORS   ko, k, kx and kz

    // Return the ko vectors
    public Object getKoVectors(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.koVector[0][0][0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.koVector[0][0][0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++){
                        ret[i] = this.koVector[i][0][0];
                    }
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                        for(int i=0; i<this.numberOfWavelengths; i++){
                            ret[i] = this.koVector[i][0][0];
                        }
                        return (Object)ret;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the kz vectors
    public Object getKzVectors(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.kzVector[0][0][0];
        }
        else{
            if(this.angularReflectCalculated){
                Complex[] ret = Complex.oneDarray(this.numberOfIncidentAngles);
                for(int i=0; i<this.numberOfIncidentAngles; i++){
                    ret[i] = this.kzVector[0][i][0];
                }
                return (Object)ret;
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[] ret = Complex.oneDarray(this.numberOfWavelengths);
                    for(int i=0; i<this.numberOfWavelengths; i++){
                        ret[i] = this.kzVector[i][0][0];
                    }
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        Complex[][] ret = Complex.twoDarray(this.numberOfWavelengths, this.numberOfIncidentAngles);
                        for(int i=0; i<this.numberOfWavelengths; i++){
                            for(int j=0; j<this.numberOfIncidentAngles; j++){
                                ret[i][j] = this.kzVector[i][j][0];
                            }
                        }
                        return (Object)ret;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the k vectors
    public Object getKvectors(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.kVector[0][0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.kVector[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[][] ret = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
                    for(int i=0; i<this.numberOfWavelengths; i++){
                        for(int j=0; i<this.numberOfLayers; i++){
                            ret[i][j] = this.kVector[i][0][j];
                        }
                    }
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        Complex[][] ret = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
                        for(int i=0; i<this.numberOfWavelengths; i++){
                            for(int j=0; i<this.numberOfLayers; i++){
                                ret[i][j] = this.kVector[i][0][j];
                            }
                        }
                        return (Object)ret;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // Return the kx vectors
    public Object getKxVectors(){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated){
            return (Object)this.kxVector[0][0];
        }
        else{
            if(this.angularReflectCalculated){
                return (Object)this.kxVector[0];
            }
            else{
                if(this.wavelengthReflectCalculated){
                    Complex[][] ret = Complex.twoDarray(this.numberOfWavelengths, this.numberOfLayers);
                    for(int i=0; i<this.numberOfWavelengths; i++){
                        for(int j=0; i<this.numberOfLayers; i++){
                            ret[i][j] = this.kxVector[i][0][j];
                        }
                    }
                    return (Object)ret;
                }
                else{
                    if(this.wavelengthAndAngularReflectCalculated){
                        return (Object)this.kxVector;
                    }
                    else{
                        return null;
                    }
                }
            }
        }
    }

    // METHODS THAT PLOT THE SIMULATIONS

    // Reset wavelength axis to frequency axis
    public void resetPlotAxisAsFrequency(){
        this.wavelengthAxisOption = 2;
    }

    // Reset wavelength axis to frequency axis
    public void resetPlotAxisAsRadians(){
        this.wavelengthAxisOption = 3;
    }

    // Reset wavelength axis to frequency axis
    public void resetPlotAxisAsWavelength(){
        this.wavelengthAxisOption = 1;
    }

    // Calculation and plotting of the reflectivities for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotReflectivities(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotReflectivities(legend);
    }

    // Calculation and plotting of the reflectivities for a single or multiple wavelengths and a range of incident angles entered
    public void plotReflectivities(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");


        String graphLegendExtra = " Reflectivities";
        String yLegend = "Reflectivity";
        String yUnits = " ";
        plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.reflectivities);
    }

    // Calculation and plotting of the transmissivities for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTransmissivities(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTransmissivities(legend);
    }

    // Calculation and plotting of the transmissivities for a single or multiple wavelengths and a range of incident angles entered
    public void plotTransmissivities(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        String graphLegendExtra = " Transmissivities";
        String yLegend = "Transmissivity";
        String yUnits = " ";
        plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmissivities);
    }

        // Calculation and plotting of the power losses on transmission for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotPowerLosses(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotPowerLosses(legend);
    }

    // Calculation and plotting of the power losses on transmission for a single or multiple wavelengths and a range of incident angles entered
    public void plotPowerLosses(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        String graphLegendExtra = " Power Losses in decibels relative to an incident power of 1 mW";
        String yLegend = "Power Losses";
        String yUnits = "dBm";

        plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.powerLosses);
    }

    // Calculation and plotting of the transmission angles for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTransmissionAngles(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTransmissionAngles(legend);
    }

    // Calculation and plotting of the transmission angles for a single or multiple wavelengths and a range of incident angles entered
    public void plotTransmissionAngles(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        String graphLegendExtra = " Transmission angles (degrees)";
        String yLegend = "Transmission angle";
        String yUnits = "degrees";
        plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmitAnglesDeg);
    }

    // Calculation and plotting of the absolute values of TE reflection coefficients for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotAbsTEreflectionCoefficients(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotAbsTEreflectionCoefficients(legend);
    }

    // Calculation and plotting of the absolute values of TE reflection coefficients for a single or multiple wavelengths and a range of incident angles entered
    public void plotAbsTEreflectionCoefficients(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE transmission coefficient plot displayed as no light in the TE mode");
        }
        else{
            double[][] absTEr = new double[numberOfWavelengths][this.numberOfIncidentAngles];
            for(int i=0; i<this.numberOfWavelengths; i++){
                for(int j=0; j<this.numberOfIncidentAngles; j++){
                    absTEr[i][j] = this.reflectCoeffTE[i][j].abs();
            }
            }

            String graphLegendExtra = " Absolute values of the TE reflection coefficients";
            String yLegend = "|TE Reflection Coefficient|";
            String yUnits = " ";

            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) absTEr);
        }
    }

    // Calculation and plotting of the absolute values of TM reflection coefficients for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotAbsTMreflectionCoefficients(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotAbsTMreflectionCoefficients(legend);
    }

    // Calculation and plotting of the absolute values of TM reflection coefficients for a single or multiple wavelengths and a range of incident angles entered
    public void plotAbsTMreflectionCoefficients(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM transmission coefficient plot displayed as no light in the TM mode");
        }
        else{
            double[][] absTMr = new double[numberOfWavelengths][this.numberOfIncidentAngles];
            for(int i=0; i<this.numberOfWavelengths; i++){
                for(int j=0; j<this.numberOfIncidentAngles; j++){
                    absTMr[i][j] = this.reflectCoeffTM[i][j].abs();
            }
            }

            String graphLegendExtra = " Absolute values of the TM reflection coefficients";
            String yLegend = "|TM Reflection Coefficient|";
            String yUnits = " ";

            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) absTMr);
        }
    }

    // Calculation and plotting of the absolute values of TE transmission coefficients for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotAbsTEtransmissionCoefficients(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotAbsTEtransmissionCoefficients(legend);
    }

    // Calculation and plotting of the absolute values of TE transmission coefficients for a single or multiple wavelengths and a range of incident angles entered
    public void plotAbsTEtransmissionCoefficients(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE transmission coefficient plot displayed as no light in the TE mode");
        }
        else{
            double[][] absTEt = new double[numberOfWavelengths][this.numberOfIncidentAngles];
            for(int i=0; i<this.numberOfWavelengths; i++){
                for(int j=0; j<this.numberOfIncidentAngles; j++){
                    absTEt[i][j] = this.transmitCoeffTE[i][j].abs();
                }
            }

            String graphLegendExtra = " Absolute values of the TE transmission coefficients";
            String yLegend = "|TE Transmission Coefficient|";
            String yUnits = " ";

            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) absTEt);
        }
    }

    // Calculation and plotting of the absolute values of TM transmission coefficients for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotAbsTMtransmissionCoefficients(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotAbsTMtransmissionCoefficients(legend);
    }

    // Calculation and plotting of the absolute values of TM transmission coefficients for a single or multiple wavelengths and a range of incident angles entered
    public void plotAbsTMtransmissionCoefficients(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM transmission coefficient plot displayed as no light in the TM mode");
        }
        else{
            double[][] absTMt = new double[numberOfWavelengths][this.numberOfIncidentAngles];
            for(int i=0; i<this.numberOfWavelengths; i++){
                for(int j=0; j<this.numberOfIncidentAngles; j++){
                    absTMt[i][j] = this.transmitCoeffTM[i][j].abs();
                }
            }

            String graphLegendExtra = " Absolute values of the TM transmission coefficients";
            String yLegend = "|TM Transmission Coefficient|";
            String yUnits = " ";

            plotSimulation(legend, graphLegendExtra, yLegend, yUnits,  (Object) absTMt);
        }
    }

    // Calculation and plotting of the integrated evanescent field for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotEvanescentFields(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotEvanescentFields(legend);
    }

    // Calculation and plotting of the integrated evanescent field for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided, resetting distnce over which field is integrated
    public void plotEvanescentFields(double distanceIntoField){
        this.fieldDistance = fieldDistance;
        String legend = "Polarisation mode: " + this.mode;
        this.plotEvanescentFields(legend);
    }

    // Calculation and plotting of the integrated evanescent fields for a single or multiple wavelengths and a range of incident angles entered
    // Resetting distnce over which field is integrated
    public void plotEvanescentFields(double fieldDistance, String legend){
        this.fieldDistance = fieldDistance;
        this.plotEvanescentFields(legend);
    }

    // Calculation and plotting of the integrated evanescent fields for a single or multiple wavelengths and a range of incident angles entered
    public void plotEvanescentFields(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        String graphLegendExtra = " Integrated Evanescent Field Intensities to a depth of " + this.fieldDistance + " metres";
        String yLegend = "Evanescent Field intensity";
        String yUnits = " ";

        plotSimulation(legend, graphLegendExtra, yLegend, yUnits,  (Object) this.evanescentFields);
    }

    // Calculation and plotting of the evanescent field penetration depths for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotPenetrationDepths(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotPenetrationDepths(legend);
    }

    // Calculation and plotting of the evanescent field penetration depths for a single or multiple wavelengths and a range of incident angles entered
    public void plotPenetrationDepths(String graphLegend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        String graphLegendExtra = " Evanescent Field Penetration Depths";
        String yLegend = "Penetration Depth";
        String yUnits = "metres";

        plotSimulation(graphLegend, graphLegendExtra, yLegend, yUnits,  (Object) this.penetrationDepths);
    }

    // Calculation and plotting of the phase shift on reflection (TE mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTEreflectionPhaseShiftDeg(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTEreflectionPhaseShiftDeg(legend);
    }

    // Calculation and plotting of the phase shift on reflection (TE mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTEreflectionPhaseShiftDeg(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE phase shift plot displayed as no light in the TE mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Reflection (TE mode)";
            String yLegend = "Phase shift";
            String yUnits = "degrees ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.reflectPhaseShiftDegTE);
        }
    }

    // Calculation and plotting of the phase shift on reflection (TM mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTMreflectionPhaseShiftDeg(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTMreflectionPhaseShiftDeg(legend);
    }

    // Calculation and plotting of the phase shift on reflection (TM mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTMreflectionPhaseShiftDeg(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM phase shift plot displayed as no light in the TM mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Reflection (TM mode)";
            String yLegend = "Phase shift";
            String yUnits = "degrees ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.reflectPhaseShiftDegTM);
        }
    }

    // Calculation and plotting of the phase shift on reflection (TE mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTEreflectionPhaseShiftRad(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTEreflectionPhaseShiftRad(legend);
    }

    // Calculation and plotting of the phase shift on reflection (TE mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTEreflectionPhaseShiftRad(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE phase shift plot displayed as no light in the TE mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Reflection (TE mode)";
            String yLegend = "Phase shift";
            String yUnits = "radians ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.reflectPhaseShiftRadTE);
        }
    }


    // Calculation and plotting of the phase shift on reflection (TM mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTMreflectionPhaseShiftRad(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTMreflectionPhaseShiftRad(legend);
    }

    // Calculation and plotting of the phase shift on reflection (TM mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTMreflectionPhaseShiftRad(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM phase shift plot displayed as no light in the TM mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Reflection (TM mode)";
            String yLegend = "Phase shift";
            String yUnits = "radians ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.reflectPhaseShiftRadTM);
        }
    }

    // Calculation and plotting of the phase shift on transmission (TE mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTEtransmissionPhaseShiftDeg(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTEtransmissionPhaseShiftDeg(legend);
    }

    // Calculation and plotting of the phase shift on transmission (TE mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTEtransmissionPhaseShiftDeg(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE phase shift plot displayed as no light in the TE mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Transmission (TE mode)";
            String yLegend = "Phase shift";
            String yUnits = "degrees ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmitPhaseShiftDegTE);
        }
    }

    // Calculation and plotting of the phase shift on transmission (TM mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTMtransmissionPhaseShiftDeg(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTMtransmissionPhaseShiftDeg(legend);
    }

    // Calculation and plotting of the phase shift on transmission (TM mode), in degrees, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTMtransmissionPhaseShiftDeg(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM phase shift plot displayed as no light in the TM mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Transmission (TM mode)";
            String yLegend = "Phase shift";
            String yUnits = "degrees ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmitPhaseShiftDegTM);
        }
    }

    // Calculation and plotting of the phase shift on transmission (TE mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTEtransmissionPhaseShiftRad(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTEtransmissionPhaseShiftRad(legend);
    }

    // Calculation and plotting of the phase shift on transmission (TE mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTEtransmissionPhaseShiftRad(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(teFraction==0.0D){
            System.out.println("No TE phase shift plot displayed as no light in the TE mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Transmission (TE mode)";
            String yLegend = "Phase shift";
            String yUnits = "radians ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmitPhaseShiftRadTE);
        }
    }

    // Calculation and plotting of the phase shift on transmission (TM mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    // No user legend provided
    public void plotTMtransmissionPhaseShiftRad(){
        String legend = "Polarisation mode: " + this.mode;
        this.plotTMtransmissionPhaseShiftRad(legend);
    }

    // Calculation and plotting of the phase shift on transmission (TM mode), in radians, for a single or multiple wavelengths and a range of incident angles entered
    public void plotTMtransmissionPhaseShiftRad(String legend){
        this.checkWhichCalculation();
        if(this.singleReflectCalculated)throw new IllegalArgumentException("Plot methods require more than one data point");

        if(tmFraction==0.0D){
            System.out.println("No TM phase shift plot displayed as no light in the TM mode");
        }
        else{
            String graphLegendExtra = " Phase Shift on Transmission (TM mode)";
            String yLegend = "Phase shift";
            String yUnits = "radians ";
            plotSimulation(legend, graphLegendExtra, yLegend, yUnits, (Object) this.transmitPhaseShiftRadTM);
        }
    }

    // Plotting of the simulation curves
    public void plotSimulation(String graphLegend, String graphLegendExtra, String yLegend, String yUnits,  Object yValuesObject){

    	// Calculate yValuesObject internal array dimensions and fill yValues array
    	Object internalArray = yValuesObject;
    	int nCurves = 1;
        while(!((internalArray  =  Array.get(internalArray, 0)) instanceof Double))nCurves++;
        double[][] yValues = new double[nCurves][];
        if(nCurves==1){
            double[] temp = (double[])yValuesObject;
            yValues[0] = temp;
        }
        else{
            yValues = (double[][])yValuesObject;
        }
        int nPoints = yValues.length;

        int[] pointOptions = null;
        double[][] plotData = null;
        String xLegend = null;
        String xUnits = null;

        if(this.angularReflectCalculated){
            pointOptions = new int[1];
            pointOptions[0] = 1;
            plotData = new double[2][nPoints];
            plotData[0] = this.incidentAngleDeg;
            plotData[1] = yValues[0];
            xLegend = "Incident Angle";
            xUnits = "degrees";
        }

        if(this.wavelengthReflectCalculated){
            pointOptions = new int[1];
            pointOptions[0] = 1;
            plotData = new double[2][nPoints];
            plotData[0] = this.wavelengths;
            double[] temp = new double[this.numberOfWavelengths];
            for(int i=0; i<this.numberOfWavelengths; i++)temp[i]=yValues[i][0];
            switch(wavelengthAxisOption){
                case 1: plotData[0] = this.wavelengths;
                        plotData[1] = temp;
                        xLegend = "Wavelength";
                        xUnits = "metres";
                        break;
                case 2: plotData[0] = this.frequencies;
                        for(int i=0; i<this.numberOfWavelengths; i++)plotData[1][this.numberOfWavelengths-1-i] = temp[i];
                        xLegend = "Frequency";
                        xUnits = "Hz";
                        break;
                case 3: plotData[0] = this.omega;
                        for(int i=0; i<this.numberOfWavelengths; i++)plotData[1][this.numberOfWavelengths-1-i] = temp[i];
                        xLegend = "Radial Frequency";
                        xUnits = "radians";
                        break;
            }
        }

        if(this.wavelengthAndAngularReflectCalculated){
            pointOptions = new int[nCurves];
            plotData = new double[2*nCurves][nPoints];
            for(int i=0; i<nCurves; i++){
                pointOptions[i] = i+1;
                plotData[2*i] = this.incidentAngleDeg;
                plotData[2*i+1] = yValues[i];
            }
            xLegend = "Incident Angle";
            xUnits = "degrees";
        }

        PlotGraph pg = new PlotGraph(plotData);
        pg.setGraphTitle("Class Reflectivity: Simulation Plot - " + graphLegendExtra);
        pg.setGraphTitle2(graphLegend);
        pg.setXaxisLegend(xLegend);
        pg.setYaxisLegend(yLegend);
        pg.setXaxisUnitsName(xUnits);
        if(!yUnits.equals(" "))pg.setYaxisUnitsName(yUnits);
        pg.setLine(3);
        pg.setPoint(pointOptions);
        pg.plot();
    }


    // CORE CALCULATION METHODS

    // Check whether reflectivity calculation has been performed
    // and perform calculation if not
    public void checkWhichCalculation(){
        boolean test=false;
        if(this.singleReflectCalculated)test=true;
        if(this.angularReflectCalculated)test=true;
        if(this.wavelengthReflectCalculated)test=true;
        if(this.wavelengthAndAngularReflectCalculated)test=true;

        if(test){
            if(this.fieldDistance!=Double.POSITIVE_INFINITY && !this.fieldIntensityCalc){
                int nkouter = this.numberOfLayers - 1;
                double integratedEvanescentField = 0.0D;
                for(int i=0; i<this.numberOfWavelengths; i++){
                    for(int j=0; j<this.numberOfIncidentAngles; j++){
                        if(this.kxVector[i][j][nkouter].getReal()==0.0D){
                            double penetrationDepth = 1.0D/this.kxVector[i][j][nkouter].getImag();
                            integratedEvanescentField += this.teFraction*Fmath.square(this.transmitCoeffTE[i][j].abs())*(1.0D - Math.exp(-2.0D*this.fieldDistance/penetrationDepth))*penetrationDepth/2.0D;
                            double refrTerm = this.refractiveIndices[i][0].getReal()/this.refractiveIndices[i][j].getReal();
                            double magnTerm = Math.sqrt(this.relativeMagneticPermeabilities[i][nkouter].getReal()/this.relativeMagneticPermeabilities[i][0].getReal());
                            integratedEvanescentField += this.teFraction*Fmath.square(this.transmitCoeffTM[i][j].abs())*magnTerm*refrTerm*(1.0D - Math.exp(-2.0D*this.fieldDistance/penetrationDepth))*penetrationDepth/2.0D;
                        }
                    }
                }
                this.fieldIntensityCalc = true;
            }
        }
        else{
            if(this.numberOfIncidentAngles==0)throw new IllegalArgumentException("No incident angle/s has/have been entered");
            if(this.numberOfWavelengths==0)throw new IllegalArgumentException("No wavelength/s has/have been entered");

            if(this.numberOfWavelengths>1)this.sortWavelengths();

            // Calculate ko, k, kx and kz vectors
            // redundant arrays included for ease of programming
            this.koVector = Complex.threeDarray(this.numberOfWavelengths, this.numberOfIncidentAngles, this.numberOfLayers);
            this.kzVector = Complex.threeDarray(this.numberOfWavelengths, this.numberOfIncidentAngles, this.numberOfLayers);
            this.kVector  = Complex.threeDarray(this.numberOfWavelengths, this.numberOfIncidentAngles, this.numberOfLayers);
            this.kxVector = Complex.threeDarray(this.numberOfWavelengths, this.numberOfIncidentAngles, this.numberOfLayers);

            for(int i=0; i<this.numberOfWavelengths; i++){
                for(int j=0; j<this.numberOfIncidentAngles; j++){
                    for(int k=0; k<this.numberOfLayers; k++){
                        // Calculate ko values
                        this.koVector[i][j][k].reset(2.0D*Math.PI/this.wavelengths[i], 0.0D);

                        // Calculate k vector
                        this.kVector[i][j][k] = this.koVector[i][j][k].times(this.refractiveIndices[i][k]).times(Complex.sqrt(this.relativeMagneticPermeabilities[i][k]));

                        // Calculate kz vector
                        this.kzVector[i][j][k] = this.koVector[i][j][k].times(this.refractiveIndices[i][0]).times(Complex.sqrt(this.relativeMagneticPermeabilities[i][0]));
                        this.kzVector[i][j][k] = this.kzVector[i][j][k].times(Math.sin(this.incidentAngleRad[j]));

                        // Calculate kx vector
                        this.kxVector[i][j][k] = (Complex.square(this.kVector[i][j][k])).minus(Complex.square(this.kzVector[i][j][k]));
                        this.kxVector[i][j][k] = Complex.sqrt(this.kxVector[i][j][k]);
                        // if(this.kxVector[i][j][k].getImag()>0.0D)this.kxVector[i][j][k] = this.kxVector[i][j][k].times(Complex.minusOne());

                    }
                }
            }

            // Arrays for calculated parameters
            this.reflectivities = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmissivities = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.powerLosses = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.reflectCoeffTE = Complex.twoDarray(this.numberOfWavelengths, this.numberOfIncidentAngles);
            this.reflectCoeffTM = Complex.twoDarray(this.numberOfWavelengths, this.numberOfIncidentAngles);
            this.transmitCoeffTE = Complex.twoDarray(this.numberOfWavelengths, this.numberOfIncidentAngles);
            this.transmitCoeffTM = Complex.twoDarray(this.numberOfWavelengths, this.numberOfIncidentAngles);
            this.evanescentFields = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.penetrationDepths = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitAnglesRad = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitAnglesDeg = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.reflectPhaseShiftRadTE = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.reflectPhaseShiftRadTM = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.reflectPhaseShiftDegTE = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.reflectPhaseShiftDegTM = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitPhaseShiftRadTE = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitPhaseShiftRadTM = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitPhaseShiftDegTE = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];
            this.transmitPhaseShiftDegTM = new double[this.numberOfWavelengths][this.numberOfIncidentAngles];

            // Perform scan over angles and wavelengths
            this.scan();
        }
    }

    // Calculation of the reflection coefficient for a single or multiple wavelengths and a single or range of incident angles entered
    public void scan(){
        if(!this.wavelSet)throw new IllegalArgumentException("No wavelength has been entered");
        if(!this.refractSet)throw new IllegalArgumentException("No, or not all, refractive indices have been entered");
        if(!this.thickSet)throw new IllegalArgumentException("No, or not all, layer thicknesses have been entered");
        if(!this.incidentAngleSet)throw new IllegalArgumentException("No incident angle has been entered");
        if(!this.modeSet)throw new IllegalArgumentException("No polaristaion mode (TE, TM, unpolarised or mixed[angle to be entered]) has been entered");

        this.singleReflectCalculated = false;
        this.angularReflectCalculated = false;
        this.wavelengthReflectCalculated = false;
        this.wavelengthAndAngularReflectCalculated = false;

        for(int i=0; i<this.numberOfWavelengths; i++){
            for(int j=0; j<this.numberOfIncidentAngles; j++){
                this.calcReflectivity(i, j);
            }
        }

        if(this.numberOfWavelengths==1){
            if(this.numberOfIncidentAngles==1){
                this.singleReflectCalculated = true;        // = true when only a single angular relectivity has been calculated
            }
            else{
                this.angularReflectCalculated = true;       // = true when an angular relectivity scan has been calculated
            }
        }
        else{
            if(this.numberOfIncidentAngles==1){
                this.wavelengthReflectCalculated = true;   // = true when a wavelength relectivity scan has been calculated
            }
            else{
                this.wavelengthAndAngularReflectCalculated = true;  // = true when angular for each wavelength relectivity scan has been calculated
            }
        }
    }

    // Calculate the reflectivity at a given incident angle and wavelength
    public void calcReflectivity(int wavelengthIndex, int angleIndex){

        double[] ret1 = new double[6];

        if(this.teFraction>0.0D){
            ret1 = this.calcTEreflectivity(wavelengthIndex, angleIndex);
        }
        if(this.tmFraction>0.0D){
            double[] ret2 = this.calcTMreflectivity(wavelengthIndex, angleIndex);
            ret1[0] = this.teFraction*ret1[0] + this.tmFraction*ret2[0];
            ret1[1] = this.teFraction*ret1[1] + this.tmFraction*ret2[1];
            ret1[2] = this.teFraction*ret1[2] + this.tmFraction*ret2[2];
            ret1[3] = this.teFraction*ret1[3] + this.tmFraction*ret2[3];
            ret1[4] = this.teFraction*ret1[4] + this.tmFraction*ret2[4];
            ret1[5] = this.teFraction*ret1[5] + this.tmFraction*ret2[5];
        }

        this.reflectivities[wavelengthIndex][angleIndex] = ret1[0];
        this.transmissivities[wavelengthIndex][angleIndex]= ret1[1];
        this.transmitAnglesRad[wavelengthIndex][angleIndex] = ret1[2];
        this.transmitAnglesDeg[wavelengthIndex][angleIndex] = Math.toDegrees(ret1[2]);
        this.evanescentFields[wavelengthIndex][angleIndex] = ret1[3];
        this.penetrationDepths[wavelengthIndex][angleIndex] = ret1[4];
        this.powerLosses[wavelengthIndex][angleIndex] = ret1[5];

    }

    // Calculate the reflectivities for the TE mode
    public double[] calcTEreflectivity(int wavelengthIndex, int angleIndex){

        Complex tempc1 = Complex.zero();  // temporary variable for calculations
        Complex tempc2 = Complex.zero();  // temporary variable for calculations
        Complex tempc3 = Complex.zero();  // temporary variable for calculations
        Complex tempc4 = Complex.zero();  // temporary variable for calculations

        double penetrationDepth = 0.0D;

        if(this.numberOfLayers==2){
            tempc1 = this.relativeMagneticPermeabilities[wavelengthIndex][1].times(this.kxVector[wavelengthIndex][angleIndex][0]);
            tempc2 = this.relativeMagneticPermeabilities[wavelengthIndex][0].times(this.kxVector[wavelengthIndex][angleIndex][1]);
            tempc3 = tempc1.minus(tempc2);
            tempc4 = tempc1.plus(tempc2);
            this.reflectCoeffTE[wavelengthIndex][angleIndex] = tempc3.over(tempc4);

            tempc3 = tempc1.times(2.0D);
            this.transmitCoeffTE[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
        }
        else{
            // Create instance of Matrix Mi
            ComplexMatrix mati = new ComplexMatrix(2, 2);

            // Create instance of Complex array Mi
            Complex[][] matic = Complex.twoDarray(2, 2);

            // Calculate cos(theta[1]), beta[1], cos[beta[1]], sin[beta[1]], p[1]
            Complex costheta = this.kxVector[wavelengthIndex][angleIndex][1].over(this.kVector[wavelengthIndex][angleIndex][1]);
            Complex pTerm = (this.refractiveIndices[wavelengthIndex][1].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][1]));
            pTerm = pTerm.times(costheta);
            Complex beta = this.kxVector[wavelengthIndex][angleIndex][1].times(this.thicknesses[1]);
            matic[0][0] = Complex.cos(beta);
            matic[1][1] = matic[0][0];
            tempc1 = Complex.sin(beta);
            tempc1 = tempc1.times(Complex.minusJay());
            matic[0][1] = tempc1.over(pTerm);
            matic[1][0] = tempc1.times(pTerm);

            if(this.numberOfLayers>3){

                // Create instance of Matrix M
                ComplexMatrix mat = new ComplexMatrix(Complex.copy(matic));

                for(int i=2; i<this.numberOfLayers-1;i++){
                    costheta = this.kxVector[wavelengthIndex][angleIndex][i].over(this.kVector[wavelengthIndex][angleIndex][i]);
                    pTerm = (this.refractiveIndices[wavelengthIndex][i].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][i]));
                    pTerm = pTerm.times(costheta);
                    beta = this.kxVector[wavelengthIndex][angleIndex][i].times(this.thicknesses[i]);
                    matic[0][0] = Complex.cos(beta);
                    matic[1][1] = matic[0][0];
                    tempc1 = Complex.sin(beta);
                    tempc1 = tempc1.times(Complex.minusJay());
                    matic[0][1] = tempc1.over(pTerm);
                    matic[1][0] = tempc1.times(pTerm);
                    mati.setTwoDarray(Complex.copy(matic));
                    mat = mat.times(mati);
                    matic = mat.getArrayCopy();
                }
            }

            costheta = this.kxVector[wavelengthIndex][angleIndex][0].over(this.kVector[wavelengthIndex][angleIndex][0]);
            Complex pTerm0 = (this.refractiveIndices[wavelengthIndex][0].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][0]));
            pTerm0 = pTerm0.times(costheta);

            costheta = this.kxVector[wavelengthIndex][angleIndex][this.numberOfLayers-1].over(this.kVector[wavelengthIndex][angleIndex][this.numberOfLayers-1]);
            Complex pTermN = (this.refractiveIndices[wavelengthIndex][this.numberOfLayers-1].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][this.numberOfLayers-1]));
            pTermN = pTermN.times(costheta);

            tempc1 = matic[0][0].plus(matic[0][1].times(pTermN));
            tempc1 = tempc1.times(pTerm0);
            tempc2 = matic[1][0].plus(matic[1][1].times(pTermN));
            tempc3 = tempc1.minus(tempc2);
            tempc4 = tempc1.plus(tempc2);
            this.reflectCoeffTE[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
            this.reflectPhaseShiftRadTE[wavelengthIndex][angleIndex] = this.reflectCoeffTE[wavelengthIndex][angleIndex].arg();
            this.reflectPhaseShiftDegTE[wavelengthIndex][angleIndex] = Math.toDegrees(this.reflectPhaseShiftRadTE[wavelengthIndex][angleIndex]);

            tempc3 = pTerm0.times(2.0D);
            this.transmitCoeffTE[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
            this.transmitPhaseShiftRadTE[wavelengthIndex][angleIndex] = this.transmitCoeffTE[wavelengthIndex][angleIndex].arg();
            this.transmitPhaseShiftDegTE[wavelengthIndex][angleIndex] = Math.toDegrees(this.transmitPhaseShiftRadTE[wavelengthIndex][angleIndex]);
        }

        // Calculate and return reflectivity, transmissivity, transmitted angle, evanescent field
        double reflectivity = Fmath.square(this.reflectCoeffTE[wavelengthIndex][angleIndex].getReal()) +  Fmath.square(this.reflectCoeffTE[wavelengthIndex][angleIndex].getImag());

        int nkouter = this.numberOfLayers - 1;
        double tempd1 = Fmath.square(this.transmitCoeffTE[wavelengthIndex][angleIndex].getReal()) +  Fmath.square(this.transmitCoeffTE[wavelengthIndex][angleIndex].getImag());
        tempc2 = (this.relativeMagneticPermeabilities[wavelengthIndex][0].over(this.relativeMagneticPermeabilities[wavelengthIndex][nkouter])).times(tempd1);
        tempc3 = this.kxVector[wavelengthIndex][angleIndex][nkouter].conjugate().over(this.kxVector[wavelengthIndex][angleIndex][0]);
        Complex complexTransmissivity = tempc2.times(tempc3);

        double transmissivity = 0.0D;
        double reflectedAngleRad = Math.PI/2.0D;
        double integratedEvanescentField = 0.0D;
        if(this.kxVector[wavelengthIndex][angleIndex][nkouter].getReal()==0.0D){
            penetrationDepth = 1.0D/this.kxVector[wavelengthIndex][angleIndex][nkouter].getImag();
            integratedEvanescentField = Fmath.square(this.transmitCoeffTE[wavelengthIndex][angleIndex].abs())*(1.0D - Math.exp(-2.0D*this.fieldDistance/penetrationDepth))*penetrationDepth/2.0D;
            if(this.fieldDistance!=Double.POSITIVE_INFINITY)this.fieldIntensityCalc = true;
        }
        else{
            transmissivity = complexTransmissivity.getReal();
            reflectedAngleRad = Math.atan2(this.kzVector[wavelengthIndex][angleIndex][nkouter].getReal(), this.kxVector[wavelengthIndex][angleIndex][nkouter].getReal());
        }

        double powerLoss = 10.0D*Fmath.log10((1.0D - transmissivity)*1e-3);

        double[] ret = new double[6];
        ret[0] = reflectivity;
        ret[1] = transmissivity;
        ret[2] = reflectedAngleRad;
        ret[3] = integratedEvanescentField;
        ret[4] = penetrationDepth;
        ret[5] = powerLoss;
        return ret;
    }

    // Calculate the reflectivities for the TM mode
    public double[] calcTMreflectivity(int wavelengthIndex, int angleIndex){

        Complex tempc1 = Complex.zero();  // temporary variable for calculations
        Complex tempc2 = Complex.zero();  // temporary variable for calculations
        Complex tempc3 = Complex.zero();  // temporary variable for calculations
        Complex tempc4 = Complex.zero();  // temporary variable for calculations

        double penetrationDepth = 0.0D;

        if(this.numberOfLayers==2){
            tempc1 = Complex.square(this.refractiveIndices[wavelengthIndex][1]).times(this.kxVector[wavelengthIndex][angleIndex][0]);
            tempc2 = Complex.square(this.refractiveIndices[wavelengthIndex][0]).times(this.kxVector[wavelengthIndex][angleIndex][1]);
            tempc3 = tempc1.minus(tempc2);
            tempc4 = tempc1.plus(tempc2);
            this.reflectCoeffTM[wavelengthIndex][angleIndex] = tempc3.over(tempc4);

            tempc3 = tempc1.times(2.0D);
            this.transmitCoeffTM[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
        }
        else{
            // Create instance of Matrix Mi
            ComplexMatrix mati = new ComplexMatrix(2, 2);

            // Create instance of Complex array Mi
            Complex[][] matic = Complex.twoDarray(2, 2);

            // Calculate cos(theta[1]), beta[1], cos[beta[1]], sin[beta[1]], p[1]
            Complex costheta = this.kxVector[wavelengthIndex][angleIndex][1].over(this.kVector[wavelengthIndex][angleIndex][1]);
            Complex pTerm = (this.refractiveIndices[wavelengthIndex][1].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][1]));
            pTerm = pTerm.over(costheta);
            Complex beta = this.kxVector[wavelengthIndex][angleIndex][1].times(this.thicknesses[1]);
            matic[0][0] = Complex.cos(beta);
            matic[1][1] = matic[0][0];
            tempc1 = Complex.sin(beta);
            tempc1 = tempc1.times(Complex.minusJay());
            matic[0][1] = tempc1.over(pTerm);
            matic[1][0] = tempc1.times(pTerm);

            if(this.numberOfLayers>3){
                // Create instance of Matrix M
                ComplexMatrix mat = new ComplexMatrix(Complex.copy(matic));

                for(int i=2; i<this.numberOfLayers-1;i++){
                    costheta = this.kxVector[wavelengthIndex][angleIndex][i].over(this.kVector[wavelengthIndex][angleIndex][i]);
                    pTerm = (this.refractiveIndices[wavelengthIndex][i].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][i]));
                    pTerm = pTerm.over(costheta);
                    beta = this.kxVector[wavelengthIndex][angleIndex][i].times(this.thicknesses[i]);
                    matic[0][0] = Complex.cos(beta);
                    matic[1][1] = matic[0][0];
                    tempc1 = Complex.sin(beta);
                    tempc1 = tempc1.times(Complex.minusJay());
                    matic[0][1] = tempc1.over(pTerm);
                    matic[1][0] = tempc1.times(pTerm);
                    mati.setTwoDarray(Complex.copy(matic));
                    mat = mat.times(mati);
                    matic = mat.getArrayReference();
                }
            }
            costheta = this.kxVector[wavelengthIndex][angleIndex][0].over(this.kVector[wavelengthIndex][angleIndex][0]);
            Complex pTerm0 = (this.refractiveIndices[wavelengthIndex][0].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][0]));
            pTerm0 = pTerm0.over(costheta);

            costheta = this.kxVector[wavelengthIndex][angleIndex][this.numberOfLayers-1].over(this.kVector[wavelengthIndex][angleIndex][this.numberOfLayers-1]);
            Complex pTermN = (this.refractiveIndices[wavelengthIndex][this.numberOfLayers-1].over(this.impedance)).over(Complex.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][this.numberOfLayers-1]));
            pTermN = pTermN.over(costheta);

            tempc1 = matic[0][0].plus(matic[0][1].times(pTermN));
            tempc1 = tempc1.times(pTerm0);
            tempc2 = matic[1][0].plus(matic[1][1].times(pTermN));
            tempc3 = tempc1.minus(tempc2);
            tempc4 = tempc1.plus(tempc2);
            this.reflectCoeffTM[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
            this.reflectPhaseShiftRadTM[wavelengthIndex][angleIndex] = this.reflectCoeffTM[wavelengthIndex][angleIndex].arg();
            this.reflectPhaseShiftDegTM[wavelengthIndex][angleIndex] = Math.toDegrees(this.reflectPhaseShiftRadTM[wavelengthIndex][angleIndex]);

            tempc3 = pTerm0.times(2.0D);
            this.transmitCoeffTM[wavelengthIndex][angleIndex] = tempc3.over(tempc4);
            this.transmitPhaseShiftRadTM[wavelengthIndex][angleIndex] = this.transmitCoeffTM[wavelengthIndex][angleIndex].arg();
            this.transmitPhaseShiftDegTM[wavelengthIndex][angleIndex] = Math.toDegrees(this.transmitPhaseShiftRadTM[wavelengthIndex][angleIndex]);
        }

        // Calculate and return reflectivity, transmissivity, transmitted angle, evanescent field
        double reflectivity = Fmath.square(this.reflectCoeffTM[wavelengthIndex][angleIndex].getReal()) +  Fmath.square(this.reflectCoeffTM[wavelengthIndex][angleIndex].getImag());

        int nkouter = this.numberOfLayers - 1;
        double tempd1 = Fmath.square(this.transmitCoeffTM[wavelengthIndex][angleIndex].getReal()) +  Fmath.square(this.transmitCoeffTM[wavelengthIndex][angleIndex].getImag());
        tempc2 = Complex.square(this.refractiveIndices[wavelengthIndex][0].over(this.refractiveIndices[wavelengthIndex][nkouter])).times(tempd1);
        tempc3 = this.kxVector[wavelengthIndex][angleIndex][nkouter].conjugate().over(this.kxVector[wavelengthIndex][angleIndex][0]);
        Complex complexTransmissivity = tempc2.times(tempc3);

        double transmissivity = 0.0D;
        double reflectedAngleRad = Math.PI/2.0D;
        double integratedEvanescentField = 0.0D;
        if(this.kxVector[wavelengthIndex][angleIndex][nkouter].getReal()==0.0D){
            penetrationDepth = 1.0D/this.kxVector[wavelengthIndex][angleIndex][nkouter].getImag();
            double refrTerm = this.refractiveIndices[wavelengthIndex][0].getReal()/this.refractiveIndices[wavelengthIndex][nkouter].getReal();
            double magnTerm = Math.sqrt(this.relativeMagneticPermeabilities[wavelengthIndex][nkouter].getReal()/this.relativeMagneticPermeabilities[wavelengthIndex][0].getReal());
            integratedEvanescentField = Fmath.square(this.transmitCoeffTM[wavelengthIndex][angleIndex].abs())*magnTerm*refrTerm*(1.0D - Math.exp(-2.0D*this.fieldDistance/penetrationDepth))*penetrationDepth/2.0D;
            if(this.fieldDistance!=Double.POSITIVE_INFINITY)this.fieldIntensityCalc = true;
        }
        else{
            transmissivity = complexTransmissivity.getReal();
            reflectedAngleRad = Math.atan2(this.kzVector[wavelengthIndex][angleIndex][nkouter].getReal(), this.kxVector[wavelengthIndex][angleIndex][nkouter].getReal());
        }

        double powerLoss = 10.0D*Fmath.log10((1.0D - transmissivity)*1e-3);

        double[] ret = new double[6];
        ret[0] = reflectivity;
        ret[1] = transmissivity;
        ret[2] = reflectedAngleRad;
        ret[3] = integratedEvanescentField;
        ret[4] = penetrationDepth;
        ret[5] = powerLoss;
        return ret;
    }

    // NON-LINEAR REGRESSION METHODS

    // ENTER INDICES OF PARAMETERS TO BE ESTIMATED BY NON-LINEAR REGRESSION

    // Enter indices of thicknesses to be estimated
    public void setThicknessEstimatesIndices(int[] indices){
        this.thicknessEstimateIndices = indices;
        this.thicknessEstimateNumber = indices.length;
    }

    // Enter indices of real parts of the refractive indices to be estimated
    public void setRealRefractIndexEstimateIndices(int[] indices){
        this.refractIndexRealEstimateIndices = indices;
        this.refractIndexRealEstimateNumber = indices.length;
    }

    // Enter indices of imaginary parts of the refractive indices to be estimated
    public void setImagRefractIndexEstimateIndices(int[] indices){
        this.refractIndexImagEstimateIndices = indices;
        this.refractIndexImagEstimateNumber = indices.length;
        this.refractIndexImagEstimateSet = true;

        // Transfer absorption coefficient estimate indices to Imag[refractive index] estimate indices list
        if(this.absorptionCoeffEstimateSet){
            int[] temp0 = new int[this.absorptionCoeffEstimateNumber];
            int newIndex = 0;
            for(int i=0; i<this.numberOfLayers; i++){
                boolean testR = false;
                for(int j=0; j<this.refractIndexImagEstimateNumber; j++){
                    if(i==this.refractIndexImagEstimateIndices[j])testR=true;
                }
                boolean testA = false;
                for(int j=0; j<this.absorptionCoeffEstimateNumber; j++){
                    if(i==this.absorptionCoeffEstimateIndices[j])testA=true;
                }
                if(!testR && testA){
                    temp0[newIndex] = i;
                    newIndex++;
                }
            }
            int newRefrNumber = this.refractIndexImagEstimateNumber  + newIndex;
            int[] temp1 = new int[newRefrNumber];
            for(int j=0; j<this.refractIndexImagEstimateNumber; j++){
                temp1[j] = this.refractIndexImagEstimateIndices[j];
            }
            for(int j=0; j<this.absorptionCoeffEstimateNumber; j++){
                temp1[this.refractIndexImagEstimateNumber + j] =  this.absorptionCoeffEstimateIndices[j];
            }
            this.refractIndexImagEstimateIndices = Fmath.selectionSort(temp1);

        }
    }

    // Enter indices of absorption coefficients to be estimated
    public void setAbsorptionCoefficientEstimateIndices(int[] indices){
        this.absorptionCoeffEstimateIndices = indices;
        this.absorptionCoeffEstimateNumber = indices.length;
        this.absorptionCoeffEstimateSet = true;

        // Transfer absorption coefficient estimate indices to Imag[refractive index] estimate indices list
        if(this.refractIndexImagEstimateSet){
            int[] temp0 = new int[this.absorptionCoeffEstimateNumber];
            int newIndex = 0;
            for(int i=0; i<this.numberOfLayers; i++){
                boolean testR = false;
                for(int j=0; j<this.refractIndexImagEstimateNumber; j++){
                    if(i==this.refractIndexImagEstimateIndices[j])testR=true;
                }
                boolean testA = false;
                for(int j=0; j<this.absorptionCoeffEstimateNumber; j++){
                    if(i==this.absorptionCoeffEstimateIndices[j])testA=true;
                }
                if(!testR && testA){
                    temp0[newIndex] = i;
                    newIndex++;
                }
            }
            int newRefrNumber = this.refractIndexImagEstimateNumber  + newIndex;
            int[] temp1 = new int[newRefrNumber];
            for(int j=0; j<this.refractIndexImagEstimateNumber; j++){
                temp1[j] = this.refractIndexImagEstimateIndices[j];
            }
            for(int j=0; j<this.absorptionCoeffEstimateNumber; j++){
                temp1[this.refractIndexImagEstimateNumber + j] =  this.absorptionCoeffEstimateIndices[j];
            }
            this.refractIndexImagEstimateIndices = Fmath.selectionSort(temp1);
        }
        else{
            this.refractIndexImagEstimateIndices = this.absorptionCoeffEstimateIndices;
            this.refractIndexImagEstimateNumber = this.absorptionCoeffEstimateNumber;
        }
    }

    // Enter indices of real parts of the relative magnetic permeabilities to be estimated
    public void setRealRelativeMagneticPermeabilityEstimateIndices(int[] indices){
        this.magneticPermRealEstimateIndices = indices;
        this.magneticPermRealEstimateNumber = indices.length;
    }

    // Enter indices of imaginary parts of the relative magnetic permeabilities to be estimated
    public void setImagRelativeMagneticPermeabilityEstimateIndices(int[] indices){
        this.magneticPermImagEstimateIndices = indices;
        this.magneticPermImagEstimateNumber = indices.length;
    }

    // FIT AND PLOT FIT - REFLECTIVITIES

    // Fit reflectivities against incident angles
    // Errors (weights) not provided
    public void fitReflectivities(double[] experimentalReflectivities){
        int n = experimentalReflectivities.length;
        double[] errors = new double[n];
        for(int i=0; i<n; i++)errors[i] = 1.0D;
        fitReflectivities(experimentalReflectivities, errors);
    }

    // Fit reflectivities against incident angles
    // Errors (weights) provided
    public void fitReflectivities(double[] experimentalReflectivities, double[] errors){
        this.numberOfDataPoints = experimentalReflectivities.length;
        if(this.numberOfDataPoints!=errors.length)throw new IllegalArgumentException("Number of data points, " + this.numberOfDataPoints + " is not equal to the number of errors (weights), " + errors.length + ".");
        if(this.incidentAngleSet){
            if(this.numberOfDataPoints!=this.numberOfIncidentAngles)throw new IllegalArgumentException("Number of experimental reflectivities " + this.numberOfDataPoints + " does not equal the number of incident angles " + this.numberOfIncidentAngles);
            double[] temp0 = Conv.copy(experimentalReflectivities);
            double[] temp1 = Conv.copy(errors);
            for(int i=0; i<this.numberOfIncidentAngles;i++){
                this.experimentalData[i] = temp0[this.incidentAngleIndices[i]];
                this.experimentalWeights[i] = temp1[this.incidentAngleIndices[i]];
            }
        }
        this.regressionOption = 1;
        this.experimentalDataSet = true;

        this.nonLinearRegression();
    }

    // Fit and plot reflectivities against incident angles
    // Errors (weights) not provided
    // Graph title not provided
    public void fitAndPlotReflectivities(double[] experimentalReflectivities){
        fitReflectivities(experimentalReflectivities);
        String graphTitle = " ";
        plotFit(graphTitle);
    }

    // Fit and plot reflectivities against incident angles
    // Errors (weights) not provided
    // Graph title provided
    public void fitAndPlotReflectivities(double[] experimentalReflectivities, String graphTitle){
        fitReflectivities(experimentalReflectivities);
        plotFit(graphTitle);
    }

    // Fit and plot reflectivities against incident angles
    // Errors (weights) provided
    // Graph title not provided
    public void fitAndPlotReflectivities(double[] experimentalReflectivities, double[] errors){
        fitReflectivities(experimentalReflectivities, errors);
        String graphTitle = " ";
        plotFit(graphTitle);
    }

    // Fit and plot reflectivities against incident angles
    // Errors (weights) provided
    // Graph title provided
    public void fitAndPlotReflectivities(double[] experimentalReflectivities,  double[] errors, String graphTitle){
        fitReflectivities(experimentalReflectivities, errors);
        plotFit(graphTitle);
    }

    // FIT AND PLOT FIT - TRANSMISSIVITIES

    // Fit transmissivities against incident angles
    // Errors (weights) not provided
    public void fitTransmissivities(double[] experimentalTransmissivities){
        int n = experimentalTransmissivities.length;
        double[] errors = new double[n];
        for(int i=0; i<n; i++)errors[i] = 1.0D;
        fitTransmissivities(experimentalTransmissivities, errors);
    }

    // Fit transmissivities against incident angles
    // Errors (weights) provided
    public void fitTransmissivities(double[] experimentalTransmissivities, double[] errors){
        this.numberOfDataPoints = experimentalTransmissivities.length;
        if(this.numberOfDataPoints!=errors.length)throw new IllegalArgumentException("Number of data points, " + this.numberOfDataPoints + " is not equal to the number of errors (weights), " + errors.length + ".");
        if(this.incidentAngleSet){
            if(this.numberOfDataPoints!=this.numberOfIncidentAngles)throw new IllegalArgumentException("Number of experimental transmissivities " + this.numberOfDataPoints + " does not equal the number of incident angles " + this.numberOfIncidentAngles);
            double[] temp0 = Conv.copy(experimentalTransmissivities);
            double[] temp1 = Conv.copy(errors);
            for(int i=0; i<this.numberOfIncidentAngles;i++){
                this.experimentalData[i] = temp0[this.incidentAngleIndices[i]];
                this.experimentalWeights[i] = temp1[this.incidentAngleIndices[i]];
            }
        }
        this.regressionOption = 1;
        this.experimentalDataSet = true;

        this.nonLinearRegression();
    }

    // Fit and plot transmissivities against incident angles
    // Errors (weights) not provided
    // Graph title not provided
    public void fitAndPlotTransmissivities(double[] experimentalTransmissivities){
        fitTransmissivities(experimentalTransmissivities);
        String graphTitle = " ";
        plotFit(graphTitle);
    }

    // Fit and plot transmissivities against incident angles
    // Errors (weights) not provided
    // Graph title provided
    public void fitAndPlotTransmissivities(double[] experimentalTransmissivities, String graphTitle){
        fitTransmissivities(experimentalTransmissivities);
        plotFit(graphTitle);
    }

    // Fit and plot transmissivities against incident angles
    // Errors (weights) provided
    // Graph title not provided
    public void fitAndPlotTransmissivities(double[] experimentalTransmissivities, double[] errors){
        fitTransmissivities(experimentalTransmissivities, errors);
        String graphTitle = " ";
        plotFit(graphTitle);
    }

    // Fit and plot transmissivities against incident angles
    // Errors (weights) provided
    // Graph title provided
    public void fitAndPlotTransmissivities(double[] experimentalTransmissivities,  double[] errors, String graphTitle){
        fitTransmissivities(experimentalTransmissivities, errors);
        plotFit(graphTitle);
    }

    // FIT AND PLOT FIT - EVANESCENT FIELDS

    // Fit total evanescent field against incident angles
    // Errors (weights) not provided
    // Distance into field not provided
    public void fitEvanescentField(double[] experimentalEvanescentFieldIntensities){
        int n = experimentalEvanescentFieldIntensities.length;
        double[] errors = new double[n];
        for(int i=0; i<n; i++)errors[i] = 1.0D;
        double fieldDistance = Double.POSITIVE_INFINITY;
        fitEvanescentField(experimentalEvanescentFieldIntensities, errors, fieldDistance);
    }

    // Fit total evanescent field against incident angles
    // Errors (weights) provided
    // Distance into field not provided
    public void fitEvanescentField(double[] experimentalEvanescentFieldIntensities, double[] errors){
        double fieldDistance = Double.POSITIVE_INFINITY;
        this.fitEvanescentField(experimentalEvanescentFieldIntensities, errors, fieldDistance);
    }

    // Fit total evanescent field against incident angles
    // Errors (weights) not provided
    // Distance into field provided
    public void fitEvanescentField(double[] experimentalEvanescentFieldIntensities, double fieldDistance){
        int n = experimentalEvanescentFieldIntensities.length;
        double[] errors = new double[n];
        for(int i=0; i<n; i++)errors[i] = 1.0D;
        fitEvanescentField(experimentalEvanescentFieldIntensities, errors, fieldDistance);
    }

    // Fit evanescent field to a depth of fieldDistance against incident angles
    // Errors (weights) provided
    // Distance into field provided
    public void fitEvanescentField(double[] experimentalEvanescentFieldIntensities, double[] errors, double fieldDistance){
        this.numberOfDataPoints = experimentalEvanescentFieldIntensities.length;
        if(this.numberOfDataPoints!=errors.length)throw new IllegalArgumentException("Number of data points, " + this.numberOfDataPoints + " is not equal to the number of errors (weights), " + errors.length + ".");

        if(this.incidentAngleSet){
            if(this.numberOfDataPoints!=this.numberOfIncidentAngles)throw new IllegalArgumentException("Number of experimental transmissivities " + this.numberOfDataPoints + " does not equal the number of incident angles " + this.numberOfIncidentAngles);
            double[] temp0 = Conv.copy(experimentalEvanescentFieldIntensities);
            double[] temp1 = Conv.copy(errors);
            for(int i=0; i<this.numberOfIncidentAngles;i++){
                this.experimentalData[i] = temp0[this.incidentAngleIndices[i]];
                this.experimentalWeights[i] = temp1[this.incidentAngleIndices[i]];
            }
        }
        this.regressionOption = 3;
        this.fieldDistance = fieldDistance;
        this.experimentalDataSet = true;

        this.nonLinearRegression();
    }

    // NELDER AND MEAD SIMPLEX NON-LINEAR REGRESSION

    // Fit experimental data against incident angles
    public void nonLinearRegression(){

        // Weighting option
        int ii=0;
        boolean test = true;
        while(test){
            if(this.experimentalWeights[ii]!=1.0D){
                this.weightingOption = true;
                test=false;
            }
            else{
                ii++;
                if(ii>=this.numberOfDataPoints)test=false;
            }
        }

        // Create an instance of Regression
        Regression regr = null;
        if(this.weightingOption){
            regr = new Regression(this.incidentAngleDeg, this.experimentalData, this.experimentalWeights);
        }
        else{
            regr = new Regression(this.incidentAngleDeg, this.experimentalData);
        }

        // Create instance of regression function
        RegressFunct funct0 = new RegressFunct();

        // Transfer values to function
        funct0.numberOfLayers = this.numberOfLayers;
        funct0.mode = this.mode;
        funct0.eVectorAngleDeg = this.eVectorAngleDeg;
        funct0.thicknesses = this.thicknesses;
        funct0.refractiveIndices = this.refractiveIndices;
        funct0.relativeMagneticPermeabilities = this.relativeMagneticPermeabilities;
        funct0.regressionOption = this.regressionOption;
        funct0.thicknessEstimateIndices = this.thicknessEstimateIndices;
        funct0.refractIndexRealEstimateIndices = this.refractIndexRealEstimateIndices;
        funct0.refractIndexImagEstimateIndices = this.refractIndexImagEstimateIndices;
        funct0.magneticPermRealEstimateIndices = this.magneticPermRealEstimateIndices;
        funct0.magneticPermImagEstimateIndices = this.magneticPermImagEstimateIndices;

        // Number of estimated parameters
        this.numberOfEstimatedParameters = this.thicknessEstimateNumber;
        this.numberOfEstimatedParameters += this.refractIndexRealEstimateNumber;
        this.numberOfEstimatedParameters += this.refractIndexImagEstimateNumber;
        this.numberOfEstimatedParameters += this.magneticPermRealEstimateNumber;
        this.numberOfEstimatedParameters += this.magneticPermImagEstimateNumber;
        if(this.regressionOption==3)this.numberOfEstimatedParameters++;

        this.degreesOfFreedom = this.numberOfDataPoints - this.numberOfEstimatedParameters;
        if(this.degreesOfFreedom<1)throw new IllegalArgumentException("Number of parameters to be estimated, " + this.numberOfEstimatedParameters + ", is greater than or equal to the number of data points, " + this.numberOfDataPoints + ".");

        // Fill initial estimate arrays
        double[] start = new double[this.numberOfEstimatedParameters];
        double[] init = new double[this.numberOfEstimatedParameters];
        double[] step = new double[this.numberOfEstimatedParameters];

        int pIndex = 0;
        for(int i=0; i<this.thicknessEstimateNumber; i++){
            init[pIndex] = this.thicknesses[this.thicknessEstimateIndices[pIndex]];
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=1e-9;
            pIndex++;
        }
        for(int i=0; i<this.refractIndexRealEstimateNumber; i++){
            init[pIndex] = this.refractiveIndices[0][this.refractIndexRealEstimateIndices[pIndex]].getReal();
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=0.1D;
            pIndex++;
        }
        for(int i=0; i<this.refractIndexImagEstimateNumber; i++){
            init[pIndex] = this.refractiveIndices[0][this.refractIndexImagEstimateIndices[pIndex]].getImag();
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=0.1D;
            pIndex++;
        }
        for(int i=0; i<this.magneticPermRealEstimateNumber; i++){
            init[pIndex] = this.relativeMagneticPermeabilities[0][this.magneticPermRealEstimateIndices[pIndex]].getReal();
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=0.1D;
            pIndex++;
        }
        for(int i=0; i<this.magneticPermImagEstimateNumber; i++){
            init[pIndex] = this.relativeMagneticPermeabilities[0][this.magneticPermImagEstimateIndices[pIndex]].getImag();
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=0.1D;
            pIndex++;
        }

        // calculate scaling factor estimate if evanescent field fitting option chosen
        if(this.regressionOption==3){
            double[] evanFields = (double[])getEvanescentFields(this.fieldDistance);
            double calcFieldMean = 0.0D;
            double explFieldMean = 0.0D;
            for(int i=0; i<this.numberOfDataPoints; i++){
                if(evanFields[i]!=0.0D){
                    calcFieldMean += evanFields[i];
                    explFieldMean += this.experimentalData[i];
                }
            }
            if(explFieldMean==0.0D)throw new IllegalArgumentException("All entered field values are zero or sum to zero");
            if(calcFieldMean==0.0D)throw new IllegalArgumentException("All calculated field values are zero or sum to zero");
            init[pIndex] = explFieldMean/calcFieldMean;
            start[pIndex] = init[pIndex];
            step[pIndex] = init[pIndex]*0.1D;
            if(step[pIndex]==0.0D)step[pIndex]=0.1D;
            pIndex++;
        }

        // Set tolerance for exiting regression
        double ftol = 1e-6;

        // Set maximum iterations in regression
        int nmax = 1000;

        // Call non-linear regression method
        regr.simplex(funct0, start, step, ftol, nmax);

        // Get best estimates
        double[] bestEstimates = regr.getCoeff();

        // Load best estimates into appropriate arrays
        pIndex = 0;
        for(int i=0; i<this.thicknessEstimateNumber; i++){
            this.thicknesses[this.thicknessEstimateIndices[pIndex]] = bestEstimates[pIndex];
            pIndex++;
        }
        for(int i=0; i<this.refractIndexRealEstimateNumber; i++){
            this.refractiveIndices[0][this.refractIndexRealEstimateIndices[pIndex]].setReal(bestEstimates[pIndex]);
            pIndex++;
        }
        for(int i=0; i<this.refractIndexImagEstimateNumber; i++){
            this.refractiveIndices[0][this.refractIndexImagEstimateIndices[pIndex]].setImag(bestEstimates[pIndex]);
            pIndex++;
        }
        for(int i=0; i<this.magneticPermRealEstimateNumber; i++){
            this.relativeMagneticPermeabilities[0][this.magneticPermRealEstimateIndices[pIndex]].setReal(bestEstimates[pIndex]);
            pIndex++;
        }
        for(int i=0; i<this.magneticPermImagEstimateNumber; i++){
            this.relativeMagneticPermeabilities[0][this.magneticPermImagEstimateIndices[pIndex]].setImag(bestEstimates[pIndex]);
            pIndex++;
        }
        if(this.regressionOption==3)this.fieldScalingFactor = bestEstimates[pIndex];

        // Get calculated data at best estimate values
        switch(this.regressionOption){
            case 1: // transmissivity fitting
                    this.calculatedData = (double[])this.getReflectivities();
                    break;
            case 2: // reflectivity fitting
                    this.calculatedData = (double[])this.getTransmissivities();
                    break;
            case 3: // evanescent field fitting
                    this.calculatedData = (double[])this.getEvanescentFields();
                    for(int i=0; i<this.numberOfDataPoints; i++)this.calculatedData[i] *= this.fieldScalingFactor;
                    break;
            default: throw new IllegalArgumentException("Regresion option " + regressionOption + " does not exist");
        }
    }

    // Return calculated data
    public double[] getCalculatedData(){
        return this.calculatedData;
    }

    // PLOT THE RESULTS OF THE NON-LINEAR REGRESSION

    // Plot experimetal and calculated data
    public void plotFit(String graphTitle2){

        // Create data arrays to be plotted
        int numberOfCalculatedDataPoints = 200;
        double[][] data = PlotGraph.data(numberOfCalculatedDataPoints, 2);

        // experimental data
        for(int i=0; i<this.numberOfDataPoints; i++){
            data[0][i] = this.incidentAngleDeg[i];
            data[1][i] = this.experimentalData[i];
        }

        // calculated data
        double angleIncrement = (this.incidentAngleDeg[this.numberOfIncidentAngles-1] - this.incidentAngleDeg[0])/(numberOfCalculatedDataPoints - 1);
        data[2][0] = this.incidentAngleDeg[0];
        for(int i=1; i<numberOfCalculatedDataPoints-1; i++)data[2][i] = data[2][i-1] + angleIncrement;
        data[2][numberOfCalculatedDataPoints-1] = this.incidentAngleDeg[this.numberOfIncidentAngles-1];

        // Create an instance of Reflectivity
        Reflectivity refl2 = new Reflectivity(this.numberOfLayers);

        // Set mode
        if(this.mode.equals("mixed")){
            refl2.setMode(eVectorAngleDeg);
        }
        else{
            refl2.setMode(this.mode);
        }
        // Set thicknesses to fixed values
        refl2.setThicknesses(this.thicknesses);
        // Set refractive index
        refl2.setRefractiveIndices(this.refractiveIndices);
        // Set relative magnetic permeability
        refl2.setRelativeMagneticPermeabilities(this.relativeMagneticPermeabilities);
        // Set incident angles
        refl2.setIncidentAngle(data[2]);

        // Calculate values and plot legends
        String titleEnd = null;
        String yAxis = null;
        switch(regressionOption){
            case 1: // transmissivity fitting
                    data[3] = (double[])refl2.getReflectivities();
                    titleEnd = "Plot of reflectivities versus incident angle";
                    yAxis = "Reflectivity";
                    break;
            case 2: // reflectivity fitting
                    data[3] = (double[])refl2.getTransmissivities();
                    titleEnd = "Plot of transmissivities versus incident angle";
                    yAxis = "Transmissivity";
                    break;
            case 3: // evanescent field fitting
                    data[3] = (double[])refl2.getEvanescentFields();
                    for(int i=0; i<numberOfCalculatedDataPoints; i++)data[3][i] *= this.fieldScalingFactor;
                    titleEnd = "Plot of evanescent fields versus incident angle";
                    yAxis = "Evanescent Field";
                    break;
            default: throw new IllegalArgumentException("Regresion option " + regressionOption + " does not exist");
        }

        // Create instance of PlotGraph
        PlotGraph pg = new PlotGraph(data);

        pg.setGraphTitle("Reflectivity class: " + titleEnd);
        pg.setGraphTitle2(graphTitle2);
        pg.setXaxisLegend("Incident angle");
        pg.setXaxisUnitsName("degrees");
        pg.setYaxisLegend(yAxis);

        int[] pointsOptions = {1, 0};
        pg.setPoint(pointsOptions);

        int[] lineOptions = {0, 3};
        pg.setLine(lineOptions);

        pg.plot();
    }

 }

// REGRESSION FUNCTION CLASS

// Class providing function for fitting reflectivities, transmissivities or evanescent fields over a range of angles
class RegressFunct implements RegressionFunction{

    public int numberOfLayers = 0;                  // number of layers
    public String mode = null;                      // polarisation mode: TE, TM,  unpolarised or mixed
    public double eVectorAngleDeg = 0.0D;           // the electric vector angle
    public double[] thicknesses = null;             // the electric vector angle
    public double[] incidentAnglesDeg = null;       // the incident angles
    public Complex[][] refractiveIndices = null;    // refractive indices
    public Complex[][] relativeMagneticPermeabilities = null;  // relative magnetic permeabilities
    public int regressionOption = 0;                // Regression option
                                                    // = 1; reflectivity versus angle
                                                    // = 2; transmissivity versus angle
                                                    // = 3; evanescent field versus angle
    public int[] thicknessEstimateIndices = null;           // indices of the thicknesses to be estimated by non-linear regression
    public int[] refractIndexRealEstimateIndices = null;   // indices of the Real[refractive indices] to be estimated by non-linear regression
    public int[] refractIndexImagEstimateIndices = null;   // indices of the Imag[refractive indices] to be estimated by non-linear regression
    public int[] magneticPermRealEstimateIndices = null;   // indices of the Real[relative magnetic permeability] to be estimated by non-linear regression
    public int[] magneticPermImagEstimateIndices = null;   // indices of the Imag[relative magnetic permeability] to be estimated by non-linear regression

    public double function(double[ ] p, double[ ] x){

        // Create instance oF Reflectivity for single angle calculation
        Reflectivity refl = new Reflectivity(this.numberOfLayers);

        // set polarisation mode
        if(this.mode.equals("mixed")){
            refl.setMode(eVectorAngleDeg);
        }
        else{
            refl.setMode(this.mode);
        }

        // Add estimates of thicknesses to fixed values
        int pIndex =0;
        int n = this.thicknessEstimateIndices.length;
        for(int i=0; i<n; i++){
            this.thicknesses[thicknessEstimateIndices[i]] = p[pIndex];
            pIndex++;
        }
        // Set thicknesses to fixed values
        refl.setThicknesses(this.thicknesses);

        // Add estimates of Real[refractive index] to fixed values
        n = this.refractIndexRealEstimateIndices.length;
        for(int i=0; i<n; i++){
            this.refractiveIndices[0][this.refractIndexRealEstimateIndices[i]].setReal(p[pIndex]);
            pIndex++;
        }

        // Add estimates of Imag[refractive index] to fixed values
        n = this.refractIndexImagEstimateIndices.length;
        for(int i=0; i<n; i++){
            this.refractiveIndices[0][this.refractIndexImagEstimateIndices[i]].setImag(p[pIndex]);
            pIndex++;
        }

        // Set refractive index
        refl.setRefractiveIndices(this.refractiveIndices);

        // Add estimates of Real[relative magnetic permeability] to fixed values
        n = this.magneticPermRealEstimateIndices.length;
        for(int i=0; i<n; i++){
            this.relativeMagneticPermeabilities[0][this.magneticPermRealEstimateIndices[i]].setReal(p[pIndex]);
            pIndex++;
        }

        // Add estimates of Imag[relative magnetic permeability] to fixed values
        n = this.magneticPermImagEstimateIndices.length;
        for(int i=0; i<n; i++){
            this.relativeMagneticPermeabilities[0][this.magneticPermImagEstimateIndices[i]].setImag(p[pIndex]);
            pIndex++;
        }

        // Set relative magnetic permeability
        refl.setRelativeMagneticPermeabilities(this.relativeMagneticPermeabilities);

        // Set incident angle for this function calculation
        refl.setIncidentAngle(x[0]);

        // Calculate value returned by this function
        double returnValue = 0.0;
        switch(regressionOption){
            case 1: // transmissivity fitting
                    returnValue = ((double[])refl.getReflectivities())[0];
                    break;
            case 2: // reflectivity fitting
                    returnValue = ((double[])refl.getTransmissivities())[0];
                    break;
            case 3: // evanescent field fitting
                    returnValue = p[pIndex]*((double[])refl.getEvanescentFields())[0];
                    break;
            default: throw new IllegalArgumentException("Regresion option " + regressionOption + " does not exist");
        }

        return returnValue;

    }
}
