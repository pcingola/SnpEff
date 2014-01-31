/*
*   Class ImmunoChemistry
*
*   Contains constants and general methods associated with immunochemistry
*   This is a superclass for the ImmunoKinetics class and its methods are called by ImmunoAssay class methods
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	    10-11 March 2011, 26 March 2011

*   MODIFIED:
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/ImmunoChemistry.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
* Copyright (c) 2011 Michael Thomas Flanagan
*
* PERMISSION TO COPY:
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

package flanagan.physchem;

import flanagan.math.Fmath;
import flanagan.analysis.Stat;

public class ImmunoChemistry{

    protected static double molecularWeightIgG1 = 146000.0;       // Molecular weight of IgG1
    protected static double molecularWeightIgG2 = 146000.0;       // Molecular weight of IgG2
    protected static double molecularWeightIgG3 = 170000.0;       // Molecular weight of IgG3
    protected static double molecularWeightIgG4 = 146000.0;       // Molecular weight of IgG4
    protected static double molecularWeightIgM = 970000.0;        // Molecular weight of IgM
    protected static double molecularWeightIgA1 = 160000.0;       // Molecular weight of IgGA1
    protected static double molecularWeightIgA2 = 160000.0;       // Molecular weight of IgGA2
    protected static double molecularWeightIgD = 184000.0;        // Molecular weight of IgD
    protected static double molecularWeightIgE = 188000.0;        // Molecular weight of IgE


    // CONSTRUCTOR
    public ImmunoChemistry(){
    }

    // DIFFUSION COEFFICIENT
    // diffusion coefficient of a globular protein
    // assumes globular protein
    // requires
    //  effectiveRadius      Solute effective radius, e.g. hydrodynamic radius, in m
    //  viscosity            Solution viscosity (Pa s)
    //  temperature          Temperature (degree Celsius)
    // returns the solute diffusion coefficient (m^2 s^-1)

    public static double diffusionCoefficient(double effectiveRadius, double viscosity, double temperature){

	    double tempK = temperature - Fmath.T_ABS;
	    double fTerm = 6.0*Math.PI*viscosity*effectiveRadius;
	    return  Fmath.K_BOLTZMANN*tempK/fTerm;
    }

    // diffusion coefficient of a globular protein
    // assumes globular protein
    // requires
    //  molecularWeight     Molecular weight of the solute(Daltons)
    //  specificVolume      Specific volume (m^3 kg^-1)
    //  viscosity           Solution viscosity (Pa s)
    //  temperature         Temperature (degree Celsius)
    // returns the solute diffusion coefficient (m^2 s^-1)
    public static double diffusionCoefficient(double molecularWeight, double specificVolume, double viscosity, double temperature){

	    double tempK = temperature - Fmath.T_ABS;
	    double molecularVolume = molecularWeight*specificVolume/(Fmath.N_AVAGADRO*1000.0);
	    double molecularRadius = Math.pow(3.0*molecularVolume/(4.0*Math.PI),1.0/3.0);
	    double fTerm = 6.0*Math.PI*viscosity*molecularRadius;
	    return  Fmath.K_BOLTZMANN*tempK/fTerm;
    }

    // PLANAR DIFFUSION

    // Returns the concentration at a distance x from a boundary at time t assuming:
    //  one dimensional diffusion
    //  an unchanging concentration at the boundary
    //  zero concentration elsewhere at time zero
    // diffusionCoefficient         Diffusion coefficient (m^2 s^-1)
    // ZeroDistanceConcentration    Concentration at the boundary [x = 0]
    // distance                     Distance, x, from the boundary [x = 0] (m)
    // time                         Time (s)
    public static double oneDimensionalDiffusion(double diffusionCoefficient, double ZeroDistanceConcentration, double distance, double time){

        double arg = distance/(2.0*Math.sqrt(diffusionCoefficient*time));
        return ZeroDistanceConcentration*Stat.erfc(arg);
    }


    // DIFFUSION CONTROLLED REACTION RATE
    // diffusion controlled reaction rate of a globular protein collision A + B -> .....
    // assumes globular proteins
    // requires
    //  diffusion coefficients of A and B (m^2 s^-1)
    //  effective radii of A and B (m)
    //  effective reactive fraction of the surfaces of A and B
    // returns a second order rate constant (M^-1 s^-1)
    public static double diffusionControlledRate(double diffCoeffA, double diffCoeffB, double radiusA, double radiusB, double fractA, double fractB){
	    return  4.0*Math.PI*Fmath.N_AVAGADRO*fractA*fractB*(diffCoeffA + diffCoeffB)*(radiusA + radiusB)*1e-3;
    }


    // MOLECULAR RADIUS
    // molecular radius of a protein
    // assumes globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns molecular radius in metres
    public static double molecularRadius(double molWt, double specVol){
        double molecularMass = molWt/Fmath.N_AVAGADRO;
        double molecularVolume = molecularMass*1.0E-3*specVol;
        return Math.pow(molecularVolume*3.0/(4.0*Math.PI), 1.0/3.0);
    }

    // molecular radius of a protein
    // assumes globular protein
    //         specific volume = 0.74E-3 m/kg (0.74 ml/gm)
    // requires molecular weight
    // returns molecular radius in metres
    public static double molecularRadius(double molWt){
        return molecularRadius(molWt, 0.74E-03);
    }

    // effective radius of a protein
    // assumes globular protein
    // requires solute diffusion coefficient im square metres per second
    //          solution viscosity in Pa s
    //          temperature in degrees Celsius
    // returns effective radius in metres
    public static double effectiveRadius(double diffusionCoefficient, double viscosity, double temperature){
        double tempK = temperature - Fmath.T_ABS;
        double dTerm = 6.0*Math.PI*viscosity*diffusionCoefficient;
	    return  Fmath.K_BOLTZMANN*tempK/dTerm;
    }

    // SURFACE CONCENTRATIONS
    // surface number concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires effective radius in m
    // returns number of protein molecules per square metre
    public static double surfaceNumberConcn(double effectiveRadius){
        double molecularArea = 2.0*effectiveRadius*Math.sqrt(3.0);
        double numberPerSquareMetre = 1.0/molecularArea;
        return numberPerSquareMetre;
    }

    // surface number concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns number of protein molecules per square metre
    public static double surfaceNumberConcn(double molWt, double specVol){
        double molecularMass = molWt/Fmath.N_AVAGADRO;
        double molecularVolume = molecularMass*1.0E-3*specVol;
        double molecularRadius = Math.pow(molecularVolume*3.0/(4.0*Math.PI), 1.0/3.0);
        double molecularArea = 2.0*molecularRadius*Math.sqrt(3.0);
        double numberPerSquareMetre = 1.0/molecularArea;
        return numberPerSquareMetre;
    }

    // surface molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires effective radius in m
    // returns moles of protein molecules per square metre
    public static double surfaceMolarConcn(double effectiveRadius){
        double surfaceNumberConcn = ImmunoChemistry.surfaceNumberConcn(effectiveRadius);
        return surfaceNumberConcn/Fmath.N_AVAGADRO;
    }

    // surface molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          specific volume in m/kg
    // returns moles of protein molecules per square metre
    public static double surfaceMolarConcn(double molWt, double specVol){
        double surfaceNumberConcn = ImmunoChemistry.surfaceNumberConcn(molWt, specVol);
        return surfaceNumberConcn/Fmath.N_AVAGADRO;
    }

    // converts a surface concentration to an equivalent  volume molar concentration for an immobilised protein
    // requires surface concentration in moles per square metre
    //          surface area in m
    //          volume in m^3
    // returns molar concentration
    public static double convertSurfaceToVolumeConcn(double surfaceConcn, double area, double volume){
        return surfaceConcn*area*1E-3/volume;
    }

    // equivalent volume molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires effective radius in m
    //          surface area in m
    //          volume in m^3
    // returns molar concentration
    public static double equivalentVolumeConcn(double effectiveRadius, double area, double volume){
        double surfaceMolarConcn = ImmunoChemistry.surfaceMolarConcn(effectiveRadius);
        return surfaceMolarConcn*area*1E-3/volume;
    }

    // equivalent volume molar concentration for an immobilised protein
    // assumes hexagonal close packing
    //         globular protein
    // requires molecular weight
    //          surface area in m
    //          volume in m^3
    //          specific volume in m/kg
    // returns molar concentration
    public static double equivalentVolumeConcn(double molWt, double area, double volume, double specVol){
        double surfaceMolarConcn = ImmunoChemistry.surfaceMolarConcn(molWt, specVol);
        return surfaceMolarConcn*area*1E-3/volume;
    }


    // MOLECULAR WEIGHTS
    // Return the molecular weight of IgG1
    public static double getMolWeightIgG1(){
        return ImmunoChemistry.molecularWeightIgG1;
    }

    // Return the molecular weight of IgG2
    public static double getMolWeightIgG2(){
        return ImmunoChemistry.molecularWeightIgG2;
    }

    // Return the molecular weight of IgG3
    public static double getMolWeightIgG3(){
        return ImmunoChemistry.molecularWeightIgG3;
    }

    // Return the molecular weight of IgG4
    public static double getMolWeightIgG4(){
        return ImmunoChemistry.molecularWeightIgG4;
    }

    // Return the molecular weight of IgM
    public static double getMolWeightIgM(){
        return ImmunoChemistry.molecularWeightIgM;
    }

    // Return the molecular weight of IgA1
    public static double getMolWeightIgA1(){
        return ImmunoChemistry.molecularWeightIgA1;
    }

    // Return the molecular weight of IgA2
    public static double getMolWeightIgA2(){
        return ImmunoChemistry.molecularWeightIgA2;
    }

    // Return the molecular weight of IgD
    public static double getMolWeightIgD(){
        return ImmunoChemistry.molecularWeightIgD;
    }

    // Return the molecular weight of IgE
    public static double getMolWeightIgE(){
        return ImmunoChemistry.molecularWeightIgE;
    }

}
