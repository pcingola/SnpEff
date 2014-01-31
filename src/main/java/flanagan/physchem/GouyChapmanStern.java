/*
*   Classes GouyChapmanStern
*
*   Class GouyChapmanStern contains the methods for
*   calculating a surface potential, surface charge,
*   potential profiles and ionic profiles at an
*   electrolyte - charged surface interface
*   using the Gouy-Chapman or Gouy-Chapman-Stern models.
*
*   Class GouyChapmanStern requires Class GCSMinim that
*   implements an interface to the required minimisation method
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:       1 December 2004
*   UPDATE:     26 February 2005, 5-7 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/GouyChapmanStern.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) December 2004, February 2005
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

package flanagan.physchem;

import java.util.ArrayList;
import flanagan.physprop.IonicRadii;
import flanagan.io.*;
import flanagan.math.*;
import flanagan.integration.Integration;
import flanagan.integration.IntegralFunction;
import javax.swing.JOptionPane;

// Class to evaluate the function needed to evaluate the potential at any distance x
// Equation 32a in the documentation, GouyChapmanStern.html
// used in the method, getPotentialAtX(), in the class GouyChapmanStern
class FunctionPatX implements IntegralFunction{

    public int numOfIons = 0;
    public double termOne = 0.0D;
    public double expTerm = 0.0D;
    public double[] bulkConcn = null;
    public double[] charges = null;

    public double function(double x){
        double sigma = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigma += (this.bulkConcn[i]*this.termOne)*(Math.exp(-this.expTerm*this.charges[i]*x) - 1.0D);
        }
        return 1.0D/Math.sqrt(sigma);
    }
}

// GouyChapmanStern Class
public class GouyChapmanStern{

    private ArrayList<Object> vec = new ArrayList<Object>();
                                            //  vector holding:
                                            //  element 0:  name of first ion
                                            //  element 1:  initial concentration of the ion in the electrolyte (moles per cubic metre but entered as moles per cubic decimetre)
                                            //  element 2:  radius of first ion (metres)
                                            //  element 3:  charge of first ion
                                            //  element 4:  association constant of ion with surface sites (cubic metres per mole but entered as cubic decimetres per mole)
                                            //  element 5:  name of second ion
                                            //  etc
    private boolean unpackArrayList = false;   // = true when the above vector has been unpacked for the calculations
    private int numOfIons = 0;              //  number of ionic species
    private int numOfAnions = 0;            //  number of anionic species
    private int numOfCations = 0;           //  number of cationic species
    private String[] ionNames = null;       //  names of the ions
    private double[] initConcnM = null;     //  initial concentration of ions (Molar)
    private double[] initConcn = null;      //  initial concentration of ions (mol per cubic metre)
    private double[] siteConcn = null;      //  surface concentration of ions adsorbed
    private double[] sternConcn = null;     //  surface concentration of ions in Stern layer but not specifically adsorbed
    private double[] bulkConcn = null;      //  concentration of ions in bulk phase (mol per cubic metre)
    private double electrolyteConcn = 0.0D; //  electrolyte concentration (average if asymmetric)
    private double ionicStrength = 0.0D;    //  ionic strength of the electrolyte
    private int[] indexK = null;            //  ion index of ionic species with non zero assocConsts
    private int nonZeroAssocK = 0;          //  number of ionic species with affinity for surface sites
    private double[] radii = null;          //  radii of ions
    private boolean radiusType = true;      //  if = true - hydrated radii are taken from Class IonicRadii
                                            //  if = false - bare radii are taken from Class IonicRadii
    private double[] charges = null;        //  charge of ions
    private double tolNeutral = 1e-6;       //  fractional tolerance in checking for overall charge neutrality
                                            //  when multiplied by total concentration of species of lowest concentration
                                            //  gives limit for considering overall neurtality achieved
    private double[] assocConstsM = null;   //  association constants of the ions with surface sites (M^-1)
    private double[] assocConsts = null;    //  association constants of the ions with surface sites (cubic metres per mol)

    private double surfaceSiteDensity = 0.0D;       // surface site density - entered as number per square metre
                                                    //  - converted to moles per square metre
    private double freeSurfaceSiteDensity = 0.0D;   // surface site density minus all occupied site densities
    private boolean surfaceDensitySet = false;      // = true if the surface density is enterd
    private double epsilon = 0.0D;                  //  relative electrical permittivity of electrolyte
    private double epsilonStern = 0.0D;             //  relative electrical permittivity of the Stern layer
    private boolean epsilonSet = false;             //  = true when epsilon has been set
    private double temp = 25.0;                     //  Temperature (degrees Celcius) [Enter temperature in degrees Celsius]
    private double tempK = 25.0-Fmath.T_ABS;        //  Temperature (degrees Kelvin)
    private boolean tempSet = false;                //  = true when temperature has been set
    private double surfacePotential = 0.0D;         //  Surface potential relative to the bulk potential [volts]
    private boolean psi0set = false;                // = true when surface potential has been entered
    private double diffPotential = 0.0D;            //  diffuse layer potential difference [volts]
    private double sternPotential = 0.0D;           //  Stern potential difference  [volts]
    private double surfaceArea = 1.0D;              //  surface area in square metres
    private boolean surfaceAreaSet = false;         // = true if the surface area is enterd
    private double volume = 1.0D;                   //  electrolyte volume in cubic metres
    private boolean volumeSet = false;              // = true if the volume is enterd
    private double sternCap = 0.0D;                 //  Stern layer capacitance [F]
    private double diffCap = 0.0D;                  //  diffuse double layer capacitance  [F]
    private double totalCap = 0.0D;                 //  total surface capacitance [F]
    private double sternDelta = 0.0D;               //  Stern layer thickness  [m]
    private double chargeValue = 0;                 //  Absolute value of the charge valency if all are the same, i.e. symmetric electrolytes of the same valency
    private boolean chargeSame = true;              //  = false if charge value not the same for all ions
    private double averageCharge = 0;               //  number average absolute charge value of the ions
    private double surfaceChargeDensity = 0.0D;     //  surface charge Density [C per square metre]
    private double adsorbedChargeDensity = 0.0D;    //  adsorbed charge Density [C per square metre]
    private double diffuseChargeDensity = 0.0D;     //  diffuse layer charge Density [C per square metre]
    private boolean sigmaSet = false;               // = true when surface charge density has been set
    private double surfaceCharge = 0.0D;            //  surface charge [C]
    private boolean chargeSet = false;              //  = true when surface charge set
    private double recipKappa = 0.0D;               //  Debye length
    private boolean sternOption = true;             // = true: Guoy-Chapman-Stern theory used
                                                    // = false: Gouy-Chapman theory used
    private double expTerm = 0.0D;                  // common group of constants:   e/kT
    private double expTermOver2 = 0.0D;             // common group of constants:   e/2kT
    private double twoTerm = 0.0D;                  // common group of constants:   2.N.k.T.eps0.eps
    private double eightTerm = 0.0D;                // common group of constants:   8.N.k.T.eps0.eps

    // Constructor
    public GouyChapmanStern(){
    }

    // Method for setting ionic radii taken from Class IonicRadii to hydrated radii
    // This is the default option
    public void setHydratedRadii(){
        this.radiusType = true;
    }

    // Method for setting ionic radii taken from Class IonicRadii to bare radii
    public void setBareRadii(){
        this.radiusType = false;
    }

    // Method for ignoring the Stern layer
    // i.e. using Gouy-Chapman theory NOT Gouy-Chapman-Stern theory
    public void ignoreStern(){
        this.sternOption = false;
    }

    // Method for reinstating the Stern layer
    // i.e. using Gouy-Chapman-Stern theory NOT Gouy-Chapman-theory
    public void includeStern(){
        this.sternOption = true;
    }

    // Method to add an ionic species
    // Concentrations - Molar,  assocK - M^-1, radius - metres, charge - valency e.g. +1
    public void setIon(String ion, double concn, double radius, int charge, double assocK){
        this.vec.add(ion);
        this.vec.add(new Double(concn));
        this.vec.add(new Double(radius));
        this.vec.add(new Integer(charge));
        this.vec.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
        this.numOfIons++;
        this.unpackArrayList = false;
    }

    // Method to add an ionic species
    // Concentrations - Molar,  radius - metres, charge - valency e.g. +1
    // association constant = 0.0D
    public void setIon(String ion, double concn, double radius, int charge){
        this.vec.add(ion);
        this.vec.add(new Double(concn));
        this.vec.add(new Double(radius));
        this.vec.add(new Integer(charge));
        this.vec.add(new Double(0.0D));
        this.numOfIons++;
        this.unpackArrayList = false;
    }

    // Method to add an ionic species
    // default radii and charge taken from class IonicRadii
    // if radii not in Ionic Radii, Donnan potential calculated with interface charge neglected
    // Concentrations - Molar
    public void setIon(String ion, double concn, double assocK){
        IonicRadii ir = new IonicRadii();
        this.vec.add(ion);
        this.vec.add(new Double(concn));
        double rad = 0.0D;
        if(this.radiusType){
            rad = ir.hydratedRadius(ion);
        }
        else{
            rad = ir.radius(ion);
        }
        if(rad==0.0D){
            String mess1 = ion + " radius is not in the IonicRadii list\n";
            String mess2 = "Please enter radius in metres\n";
            rad = Db.readDouble(mess1+mess2);
        }
        this.vec.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge as, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.vec.add(new Integer(charg));
        this.vec.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
        this.numOfIons++;
        this.unpackArrayList = false;
    }

    // Method to add an ionic species
    // default radii and charge taken from class IonicRadii
    // association constant = 0.0D
    // Concentrations - Molar
    public void setIon(String ion, double concn){
        IonicRadii ir = new IonicRadii();
        this.vec.add(ion);
        this.vec.add(new Double(concn));
        double rad = 0.0D;
        if(this.radiusType){
            rad = ir.hydratedRadius(ion);
        }
        else{
            rad = ir.radius(ion);
        }
        if(rad==0.0D){
            String mess1 = ion + " radius is not in the IonicRadii list\n";
            String mess2 = "Please enter radius in metres\n";
            rad = Db.readDouble(mess1+mess2);
        }
        this.vec.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge as, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.vec.add(new Integer(charg));
        this.vec.add(new Double(0.0D));
        this.numOfIons++;
        this.unpackArrayList = false;
    }


    // Method to set the surface adsorption site density, moles per square metre
    public void setSurfaceSiteDensity(double density){
        this.surfaceSiteDensity = density/Fmath.N_AVAGADRO;
        this.surfaceDensitySet = true;
    }

    // Method to set the surface area
    public void setSurfaceArea(double area){
        this.surfaceArea = area;
        this.surfaceAreaSet = true;
    }

    // Method to set the electrolyte volume
    public void setVolume(double vol){
        this.volume = vol;
        this.volumeSet = true;
    }

    // Method to set relative permittivities
    public void setRelPerm(double epsilon, double epsilonStern){
        this.epsilon = epsilon;
        this.epsilonStern = epsilonStern;
        this.epsilonSet = true;
    }

    // Method to set  relative permittivity
    // No Stern layer permittivities included
    public void setRelPerm(double epsilon){
        this.epsilon = epsilon;
        this.epsilonSet = true;
    }

    // Method to set temperature (enter as degrees Celsius)
    public void setTemp(double temp){
        this.tempK = temp - Fmath.T_ABS;
        this.tempSet = true;
    }

    // Method to set Surface Charge Density (C per square metre)
    public void  setSurfaceChargeDensity(double sigma){
        if(this.psi0set){
            System.out.println("You have already entered a surface potential");
            System.out.println("This class allows the calculation of a surface charge density for a given surface potential");
            System.out.println("or the calculation of a surface potential for a given surface charge density");
            System.out.println("The previously entered surface potential will now be ignored");
            this.psi0set = false;
        }

        this.surfaceChargeDensity = sigma;
        this.sigmaSet = true;
        if(this.surfaceAreaSet){
            this.surfaceCharge = sigma*this.surfaceArea;
            this.chargeSet = true;
        }
    }

    // Method to set Surface Charge(C) and surface area
    public void  setSurfaceCharge(double charge, double area){
        if(this.psi0set){
            System.out.println("You have already entered a surface potential");
            System.out.println("This class allows the calculation of a surface charge density for a given surface potential");
            System.out.println("or the calculation of a surface potential for a given surface charge density");
            System.out.println("The previously entered surface potential will now be ignored");
            this.psi0set = false;
        }

        this.surfaceCharge = charge;
        this.chargeSet = true;
        this.surfaceArea = area;
        this.surfaceAreaSet = true;
        this.surfaceChargeDensity = charge/this.surfaceArea;
        this.sigmaSet = true;
    }

    // Method to set Surface Charge(C)
    public void  setSurfaceCharge(double charge){
        if(this.psi0set){
            System.out.println("You have already entered a surface potential");
            System.out.println("This class allows the calculation of a surface charge density for a given surface potential");
            System.out.println("or the calculation of a surface potential for a given surface charge density");
            System.out.println("The previously entered surface potential will now be ignored");
            this.psi0set = false;
        }

        this.surfaceCharge = charge;
        this.chargeSet = true;
        if(this.surfaceAreaSet){
            this.surfaceChargeDensity = charge/this.surfaceArea;
            this.sigmaSet = true;
        }
    }

    // Method to set Surface Potential (Volts)
    public void  setSurfacePotential(double psi0){
        if(this.sigmaSet){
            System.out.println("You have already entered a surface charge density");
            System.out.println("This class allows the calculation of a surface potential for a given surface charge density");
            System.out.println("or the calculation of a surface charge density for a given surface potential");
            System.out.println("The previously entered surface charge density will now be ignored");
            this.sigmaSet = false;
        }

        this.surfacePotential = psi0;
        this.psi0set = true;
    }

    // Method to get the diffuse double layer potential [volts]
    public double getDiffuseLayerPotential(){
        if(!this.sternOption){
            System.out.println("Class: GouyChapmanStern\nMethod: getDiffuseLayerPotential\nThe Stern modification was not included");
            System.out.println("The value of the diffuse layer potential has been set equal to the surface potential");
            return getSurfacePotential();
        }

        if(this.psi0set && this.sigmaSet){
            return this.diffPotential;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.diffPotential;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.diffPotential;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getDiffuseLayerPotential\nThe value of the diffuse layer potential has not been calculated\nzero returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return 0.0D;
                }
            }
        }
    }

    // Method to get the Stern layer potential  [volts]
    public double getSternLayerPotential(){
        if(!this.sternOption){
            System.out.println("Class: GouyChapmanStern\nMethod: getSternLayerPotential\nThe Stern modification has not been included");
            System.out.println("The value of zero has been returned");
            return 0.0D;
        }

        if(this.psi0set && this.sigmaSet){
            return this.sternPotential;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.sternPotential;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.sternPotential;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getSternLayerPotential\nThe value of the Stern layer potential has not been calculated\nzero returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return 0.0D;
                }
            }
        }
    }

    // Method to get the Stern Capacitance per square metre
    public double getSternCapPerSquareMetre(){
        if(!this.sternOption){
            System.out.println("Class: GouyChapmanStern\nMethod: getSternCapacitance\nThe Stern modification has not been included");
            System.out.println("A value of infinity has been returned");
            return Double.POSITIVE_INFINITY;
        }

        if(this.psi0set && this.sigmaSet){
            return this.sternCap;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.sternCap;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.sternCap;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getSternCap\nThe value of the Stern capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the Stern Capacitance
    public double getSternCapacitance(){
        if(!this.sternOption){
            System.out.println("Class: GouyChapmanStern\nMethod: getSternCapacitance\nThe Stern modification has not been included");
            System.out.println("A value of infinity has been returned");
            return Double.POSITIVE_INFINITY;
        }

        if(!this.surfaceAreaSet){
            System.out.println("Class: GouyChapmanStern\nMethod: getSternCapacitance\nThe surface area has not bee included");
            System.out.println("A value per square metre has been returned");
            return this.sternCap;
        }

        if(this.psi0set && this.sigmaSet){
            return this.sternCap*this.surfaceArea;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.sternCap*this.surfaceArea;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.sternCap*this.surfaceArea;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getSternCapacitance\nThe value of the Stern capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the diffuse layer Capacitance per square metre
    public double getDiffuseLayerCapPerSquareMetre(){
        if(this.psi0set && this.sigmaSet){
            return this.diffCap;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.diffCap;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.diffCap;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getDiffuseLayerCapPerSquareMetre\nThe value of the diffuse layer capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the diffuse layer Capacitance
    public double getDiffuseLayerCapacitance(){
        if(!this.surfaceAreaSet){
            System.out.println("Class: GouyChapmanStern\nMethod: getDiffuseLayerCapacitance\nThe surface area has not bee included");
            System.out.println("A value per square metre has been returned");
            return this.diffCap;
        }

        if(this.psi0set && this.sigmaSet){
            return this.diffCap*this.surfaceArea;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.diffCap*this.surfaceArea;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.diffCap*this.surfaceArea;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getDiffuseLayerCap\nThe value of the diffuse layer capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the total capacitance per square metre
    public double getTotalCapPerSquareMetre(){
        if(this.psi0set && this.sigmaSet){
            return this.totalCap;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.totalCap;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.totalCap;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getTotalCapPerSquareMetre\nThe value of the total capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the total capacitance
    public double getTotalCapacitance(){
        if(!this.surfaceAreaSet){
            System.out.println("Class: GouyChapmanStern\nMethod: getTotalCapacitance\nThe surface area has not bee included");
            System.out.println("A value per square metre has been returned");
            return this.diffCap;
        }

        if(this.psi0set && this.sigmaSet){
            return this.totalCap*this.surfaceArea;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.totalCap*this.surfaceArea;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.totalCap*this.surfaceArea;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getTotalCapacitance\nThe value of the total capacitance has not been calculated\ninfinity returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Method to get the Stern layer thickness
    public double getSternThickness(){
        if(!this.sternOption){
            System.out.println("Class: GouyChapmanStern\nMethod: getSternThickness");
            System.out.println("The Stern modification has not been included");
            System.out.println("A value of zero has been returned");
            return 0.0D;
        }

        if(this.psi0set && this.sigmaSet){
            return this.sternDelta;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.sternDelta;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.sternDelta;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getSternThickness\nThe value of the Stern thickness has not been calculated\nzero returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return 0.0D;
                }
            }
        }
    }

    // Method to get the Debye length
    public double getDebyeLength(){
        if(!this.unpackArrayList)unpack();
        return this.calcDebyeLength();
    }

    // Calculate Debye length
    private double calcDebyeLength(){
        if(!this.epsilonSet)throw new IllegalArgumentException("The relative permittivitie/s have not been entered");
        if(!this.tempSet)System.out.println("The temperature has not been entered\na value of 25 degrees Celsius has been used");

        double preterm = 2.0D*Fmath.square(Fmath.Q_ELECTRON)*Fmath.N_AVAGADRO/(Fmath.EPSILON_0*this.epsilon*Fmath.K_BOLTZMANN*this.tempK);
        this.recipKappa = 0.0;
        for(int i=0; i<this.numOfIons; i++){
            this.recipKappa += this.bulkConcn[i]*charges[i]*charges[i];
        }
        this.recipKappa = 1.0D/Math.sqrt(this.recipKappa*preterm);
        return this.recipKappa;
    }

    // Method to get the ionic strength
    public double getIonicStrength(){
        if(!this.unpackArrayList)unpack();
        return this.ionicStrength;
    }

    // unpacks the ion storage vector and fills relevant arrays
    private void unpack(){
        if(this.numOfIons==0)throw new IllegalArgumentException("No ions have been entered");

        // change concentrations to moles per cubic metre
        // fill primitive data arrays
        // calculate total moles
        // check if the electrolyte is charge symmetric
        this.ionNames = new String[this.numOfIons];
        this.siteConcn = new double[this.numOfIons];
        this.sternConcn = new double[this.numOfIons];
        this.initConcnM = new double[this.numOfIons];
        this.initConcn = new double[this.numOfIons];
        this.bulkConcn = new double[this.numOfIons];
        this.radii = new double[this.numOfIons];
        this.charges = new double[this.numOfIons];
        this.assocConsts = new double[this.numOfIons];
        this.assocConstsM = new double[this.numOfIons];
        this.indexK = new int[this.nonZeroAssocK];
        Double hold = null;
        Integer holi = null;
        int ii = 0;
        this.chargeValue = 0;
        this.chargeSame = true;

        for(int i=0; i<numOfIons; i++){
            this.ionNames[i]    = (String)this.vec.get(0+i*5);
            hold                = (Double)this.vec.get(1+i*5);
            this.initConcnM[i]  = hold.doubleValue();
            this.initConcn[i]   = this.initConcnM[i]*1e3;
            hold                = (Double)this.vec.get(2+i*5);
            this.radii[i]       = hold.doubleValue();
            holi                = (Integer)this.vec.get(3+i*5);
            this.charges[i]     = holi.intValue();
            hold                = (Double)this.vec.get(4+i*5);
            this.assocConstsM[i]= hold.doubleValue();
            this.assocConsts[i] = this.assocConstsM[i]*1e-3;
            if(this.assocConsts[i]>0.0D){
                indexK[ii] = i;
                ii++;
            }

            // Check for all ions having same absolute charge
            if(i==0){
                this.chargeValue = Math.abs(this.charges[0]);
            }
            else{
                if(Math.abs(this.charges[i])!=this.chargeValue)this.chargeSame=false;
            }

            // calculate number of anionic and cationic species
            if(charges[i]<0){
                numOfAnions++;
            }
            else{
                numOfCations++;
            }
            if(this.surfaceSiteDensity==0.0D)this.nonZeroAssocK = 0;
        }

        // Calculate overall charge, average absolute charge,
        //  ionic strength and electrolye concentration (only average if asymmetric)
        this.averageCharge = 0.0D;
        this.ionicStrength = 0.0D;
        double overallCharge = 0.0D;
        double positives = 0.0D;
        double negatives = 0.0D;
        for(int i=0; i<numOfIons; i++){
            if(charges[i]>0.0D){
                positives += this.initConcn[i]*charges[i];
            }
            else{
                negatives += this.initConcn[i]*charges[i];
            }
            overallCharge = positives + negatives;
        }
        if(Math.abs(overallCharge)>positives*this.tolNeutral){
            String quest0 = "Class: GouyChapmanStern, method: unpack()\n";
            String quest1 = "Total charge = " + overallCharge + " mol/dm, i.e. is not equal to zero\n";
            String quest2 = "Positive charge = " + positives + " mol/dm\n";
            String quest3 = "Do you wish to continue?";
            String quest = quest0 + quest1 + quest2 + quest3;
            int res = JOptionPane.showConfirmDialog(null, quest, "Neutrality check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(res==1)System.exit(0);
        }

        double numer = 0.0D;
        double denom = 0.0D;
        double anionConc = 0.0D;
        double cationConc = 0.0D;
        for(int i=0; i<numOfIons; i++){
            this.ionicStrength += initConcn[i]*charges[i]*charges[i];
            if(charges[i]>0){
                cationConc += initConcn[i];
            }
            else{
                anionConc += initConcn[i];
            }
            if(initConcn[i]>0.0D){
                numer += initConcn[i]*Math.abs(charges[i]);
                denom += initConcn[i];
            }
        }
        this.ionicStrength = this.ionicStrength*1e-3/2.0D;
        this.averageCharge = numer/denom;
        this.electrolyteConcn = (anionConc + cationConc)/2.0D;

        // initialise site and Stern concentrations
        for(int i=0; i<this.numOfIons; i++){
            bulkConcn[i] = initConcn[i];
            siteConcn[i] = 0.0D;
            sternConcn[i] = 0.0D;
        }

        // calculate commonly used combinations of constants
        this.expTerm = -Fmath.Q_ELECTRON/(Fmath.K_BOLTZMANN*this.tempK);    // |e|/kT
        this.expTermOver2 = this.expTerm/2.0D;                              // |e|/2kT
        this.twoTerm = 2.0D*(Fmath.N_AVAGADRO*Fmath.K_BOLTZMANN)*Fmath.EPSILON_0*this.epsilon*this.tempK;   // 2.N.k.T.eps0.eps
        this.eightTerm = 4.0D*this.twoTerm;                                                                 // 8.N.k.T.eps0.eps

        this.unpackArrayList = true;
    }

    // Calculate the surface charge density for the set surface potential
    public double getSurfaceChargeDensity(){
        if(this.sigmaSet && this.psi0set){
            return this.surfaceChargeDensity;
        }
        else{
            if(!this.psi0set)throw new IllegalArgumentException("No surface potential has been entered");
            return getSurfaceChargeDensity(this.surfacePotential);
        }
    }

    // Get the surface charge density for a given surface potential, psi
    public double getSurfaceChargeDensity(double psi){
        this.surfaceChargeDensity = this.calcSurfaceChargeDensity(psi);
        this.sigmaSet = true;
        if(this.surfaceAreaSet){
            this.surfaceCharge = this.surfaceChargeDensity*this.surfaceArea;
            this.chargeSet = true;
        }
        return this.surfaceChargeDensity;
    }

    // Calculate the surface charge density for a given surface potential, psi
    private double calcSurfaceChargeDensity(double psi){
        double surCharDen = 0.0D;    // temporary values of surface charge density
        if(!this.epsilonSet)throw new IllegalArgumentException("The relative permittivitie/s have not been entered");
        if(!this.tempSet)System.out.println("The temperature has not been entered\na value of 25 degrees Celsius has been used");

        if(!this.unpackArrayList)unpack();
        if(this.sternOption){
            if(!this.surfaceAreaSet)throw new IllegalArgumentException("The surface area has not been entered");
            if(!this.volumeSet)throw new IllegalArgumentException("The electrolyte volume has not been entered");

            // Gouy-Chapman-Stern
            if(this.nonZeroAssocK==0){
                // No specific adsorption
                if(this.chargeSame){
                    // symmetric electrolyte
                    surCharDen = surfaceChargeDensity0(psi);
                }
                else{
                    // asymmetric electrolyte
                    surCharDen = surfaceChargeDensity1(psi);
                }
            }
            else{
                // Specific Adsorption
                if(this.chargeSame){
                    // symmetric electrolyte
                    surCharDen = surfaceChargeDensity2(psi);
                }
                else{
                    // asymmetric electrolyte
                    surCharDen = surfaceChargeDensity3(psi);
                }
            }
        }
        else{
            // Gouy-Chapman
            if(this.chargeSame){
                // symmetric electrolyte
                surCharDen = Math.sqrt(this.eightTerm*this.electrolyteConcn)*Fmath.sinh(this.chargeValue*this.expTermOver2*psi);
            }
            else{
                // asymmetric electrolyte
                double sigmaSum = 0.0D;
                for(int i=0; i<this.numOfIons; i++){
                    sigmaSum += bulkConcn[i]*(Math.exp(-this.expTerm*psi*charges[i])-1.0D);
                }
                surCharDen = Fmath.sign(psi)*Math.sqrt(this.twoTerm*sigmaSum);
            }
        }

        this.totalCap = surCharDen/psi;
        if(!this.sternOption){
            this.diffPotential = psi;
            this.sternCap = Double.POSITIVE_INFINITY;
            this.sternPotential = 0.0D;
            this.diffCap = this.totalCap;
        }
        else{
            this.diffPotential = psi - surCharDen/this.sternCap;
            this.sternPotential = psi - this.diffPotential;
            this.diffCap = (surCharDen + this.adsorbedChargeDensity)/this.diffPotential;
        }
        return surCharDen;
    }

    // Calculate the surface charge for the set surface potential
    public double getSurfaceCharge(){
        return getSurfaceCharge(this.surfacePotential);
    }

    // Calculate the surface charge for a given surface potential, psi
    public double getSurfaceCharge(double psi){
        if(!this.surfaceAreaSet)throw new IllegalArgumentException("No surface area has been entered");
        if(this.sigmaSet){
            this.surfaceCharge = this.surfaceChargeDensity*this.surfaceArea;
        }
        else{
            if(!this.psi0set)throw new IllegalArgumentException("No surface potential has been entered");
            this.surfaceCharge = this.getSurfaceChargeDensity(psi)*this.surfaceArea;
        }

        return this.surfaceCharge;
    }

    // Calculate surface charge density for a symmetric electrolyte
    // Stern modification without specific adsorption
    private double surfaceChargeDensity0(double psi){
        double surCharDen = 0.0D;    // temporary values of surface charge density

        // bisection method
        double ionSum = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            if(this.charges[i]>0.0){
                ionSum += bulkConcn[i];
            }
        }
        double sigmaLow = 0.0D;
        double sFuncLow = this.sigmaFunction0(sigmaLow, psi);
        double sigmaHigh = Math.sqrt(this.eightTerm*ionSum)*Fmath.sinh(this.chargeValue*this.expTermOver2*psi);
        double sFuncHigh = this.sigmaFunction0(sigmaHigh, psi);
        if(sFuncHigh*sFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(sigmaHigh)*1e-6;
        boolean test = true;
        double sigmaMid = 0.0D;
        double sFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            sigmaMid = (sigmaLow + sigmaHigh)/2.0D;
            sFuncMid = this.sigmaFunction0(sigmaMid, psi);
            if(Math.abs(sFuncMid)<=check){
                surCharDen = sigmaMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfaceChargeDensity0\nnumber of iterations exceeded in bisection\ncurrent value of sigma returned");
                    surCharDen = sigmaMid;
                    test = false;
                }
                else{
                    if(sFuncMid*sFuncLow>0){
                        sigmaLow=sigmaMid;
                        sFuncLow=sFuncMid;
                    }
                    else{
                        sigmaHigh=sigmaMid;
                        sFuncHigh=sFuncMid;
                    }
                }

            }
        }
        return surCharDen;
    }

    // function to calculate sigma for surfaceChargeDensity0()
    private double sigmaFunction0(double sigma, double psi){

        // calculate psi(delta) Stern capacitance for current estimate of sigma
        this.calcSurfacePotential(sigma);
        return this.diffPotential - psi + sigma/this.sternCap;
    }


    // Calculate surface charge density for a asymmetric electrolyte
    // Stern modification without specific adsorption
    private double surfaceChargeDensity1(double psi){
        double surCharDen = 0.0D;    // temporary values of surface charge density

        // bisection method
        double sigmaLow = 0.0D;
        double sFuncLow = this.sigmaFunction0(sigmaLow, psi);
        double sigmaHigh = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigmaHigh += bulkConcn[i]*this.twoTerm*(Math.exp(-this.expTerm*charges[i]*psi) - 1.0D);
        }
        sigmaHigh = Fmath.sign(psi)*Math.sqrt(this.twoTerm*sigmaHigh);
        double sFuncHigh = this.sigmaFunction0(sigmaHigh, psi);
        if(sFuncHigh*sFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(sigmaHigh)*1e-6;
        boolean test = true;
        double sigmaMid = 0.0D;
        double sFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            sigmaMid = (sigmaLow + sigmaHigh)/2.0D;
            sFuncMid = this.sigmaFunction0(sigmaMid, psi);
            if(Math.abs(sFuncMid)<=check){
                surCharDen = sigmaMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfaceChargeDensity1\nnumber of iterations exceeded in outer bisection\ncurrent value of sigma returned");
                    surCharDen = sigmaMid;
                    test = false;
                }
                else{
                    if(sFuncLow*sFuncMid>0.0D){
                        sigmaLow = sigmaMid;
                        sFuncLow = sFuncMid;
                    }
                    else{
                        sigmaHigh = sigmaMid;
                        sFuncHigh = sFuncMid;
                    }
                }
            }
        }
        return surCharDen;
    }

    // Calculate the Stern thickness, adsorbed ion concentrations and Stern layer non-adsorbed ion concentrations
    //   for a given potential, psi
    private double calcDelta(double psi){
        double numer = 0.0D;
        double denom = 0.0D;
        double delta = 0.0;

        for(int i=0; i<this.numOfIons; i++){
            this.sternConcn[i] = this.bulkConcn[i]*Math.exp(-this.charges[i]*this.expTerm);

            numer += this.sternConcn[i]*this.radii[i];
            denom += this.sternConcn[i];
        }

        this.sternDelta = numer/denom;

        return this.sternDelta;
    }

    // Calculate surface charge density for a symmetric electrolyte
    // Stern modification with specific adsorption
    private double surfaceChargeDensity2(double psi){
        double surCharDen = 0.0D;    // temporary values of surface charge density

        // bisection method
        double sigmaAdsPos = 0.0D;
        double sigmaAdsNeg = 0.0D;
        for(int i=0; i<this.nonZeroAssocK; i++){
            if(this.charges[indexK[i]]>0){
                sigmaAdsPos=surfaceSiteDensity;
            }
            else{
                sigmaAdsNeg=-surfaceSiteDensity;
            }
        }

        double sigmaLow = 0.0D;
        double sigmaHigh = 0.0D;
        double ionSum = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            ionSum += bulkConcn[i];
        }
        ionSum /=2.0D;
        sigmaHigh = Math.sqrt(ionSum*this.eightTerm)*Fmath.sinh(this.expTermOver2*psi*this.chargeValue);
        if(sigmaHigh>0.0D){
            sigmaHigh += sigmaAdsPos;
            sigmaLow  -= sigmaAdsNeg;
        }
        else{
            sigmaHigh -= sigmaAdsNeg;
            sigmaLow  += sigmaAdsPos;
        }
        double sFuncLow = this.sigmaFunction2(sigmaLow, psi);
        double sFuncHigh = this.sigmaFunction2(sigmaHigh, psi);
        if(sFuncHigh*sFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(sigmaHigh)*1e-6;
        boolean test = true;
        double sigmaMid = 0.0D;
        double sFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            sigmaMid = (sigmaLow + sigmaHigh)/2.0D;
            sFuncMid = this.sigmaFunction2(sigmaMid, psi);
            if(Math.abs(sFuncMid)<=check){
                surCharDen = sigmaMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfaceChargeDensity2\nnumber of iterations exceeded in outer bisection\ncurrent value of sigma returned");
                    surCharDen = sigmaMid;
                    test = false;
                }
                else{
                    if(sFuncLow*sFuncMid>0.0D){
                        sigmaLow = sigmaMid;
                        sFuncLow = sFuncMid;
                    }
                    else{
                        sigmaHigh = sigmaMid;
                        sFuncHigh = sFuncMid;
                    }
                }

            }
        }
        return surCharDen;
    }

    // function to calculate sigma for surfaceChargeDensity2()
    private double sigmaFunction2(double sigma, double psi){
        // bisection method for psi(delta)
        double psiLow = -10*psi;
        double pFuncLow = this.psiFunctionQ(psiLow, psi, sigma);
        double psiHigh = 10*psi;
        double pFuncHigh = this.psiFunctionQ(psiHigh, psi, sigma);
        if(pFuncHigh*pFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(psi)*1e-6;
        boolean test = true;
        double psiMid = 0.0D;
        double pFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            psiMid = (psiLow + psiHigh)/2.0D;
            pFuncMid = this.psiFunctionQ(psiMid, psi, sigma);
            if(Math.abs(pFuncMid)<=check){
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfaceChargeDensity3\nnumber of iterations exceeded in inner bisection\ncurrent value of sigma returned");
                    test = false;
                }
                if(pFuncMid*pFuncHigh>0){
                    psiHigh = psiMid;
                    pFuncHigh = pFuncMid;
                }
                else{
                    psiLow = psiMid;
                    pFuncLow = pFuncMid;
                }
            }
        }

        double sigmaEst = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigmaEst += bulkConcn[i];
        }
        sigmaEst = Math.sqrt(this.eightTerm*sigmaEst/2.0D)*Fmath.sinh(this.expTermOver2*psi*this.chargeValue);

        return sigma + this.adsorbedChargeDensity - sigmaEst;
    }

    // Calculate surface charge density for a asymmetric electrolyte
    // Stern modification with specific adsorption
    private double surfaceChargeDensity3(double psi){
        double surCharDen = 0.0D;    // temporary values of surface charge density

        // bisection method
        double sigmaAdsPos = 0.0D;
        double sigmaAdsNeg = 0.0D;
        for(int i=0; i<this.nonZeroAssocK; i++){
            if(this.charges[indexK[i]]>0){
                sigmaAdsPos=surfaceSiteDensity;
            }
            else{
                sigmaAdsNeg=-surfaceSiteDensity;
            }
        }

        double sigmaLow = 0.0D;
        double sigmaHigh = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigmaHigh += bulkConcn[i]*this.twoTerm*(Math.exp(-this.expTerm*charges[i]*psi) - 1.0D);
        }
        sigmaHigh = Fmath.sign(psi)*Math.sqrt(sigmaHigh);
        if(sigmaHigh>0.0D){
            sigmaHigh += sigmaAdsPos;
            sigmaLow  -= sigmaAdsNeg;
        }
        else{
            sigmaHigh -= sigmaAdsNeg;
            sigmaLow  += sigmaAdsPos;
        }
        double sFuncLow = this.sigmaFunction3(sigmaLow, psi);
        double sFuncHigh = this.sigmaFunction3(sigmaHigh, psi);
        if(sFuncHigh*sFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(sigmaHigh)*1e-6;
        boolean test = true;
        double sigmaMid = 0.0D;
        double sFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            sigmaMid = (sigmaLow + sigmaHigh)/2.0D;
            sFuncMid = this.sigmaFunction3(sigmaMid, psi);
            if(Math.abs(sFuncMid)<=check){
                surCharDen = sigmaMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfaceChargeDensity3\nnumber of iterations exceeded in outer bisection\ncurrent value of sigma returned");
                    surCharDen = sigmaMid;
                    test = false;
                }
                else{
                    if(sFuncLow*sFuncMid>0.0D){
                        sigmaLow = sigmaMid;
                        sFuncLow = sFuncMid;
                    }
                    else{
                        sigmaHigh = sigmaMid;
                        sFuncHigh = sFuncMid;
                    }
                }

            }
        }
        return surCharDen;
    }

    // function to calculate sigma for surfaceChargeDensity2()
    private double sigmaFunction3(double sigma, double psi){
        // bisection method for psi(delta)
        double psiLow = 0.0D;
        double pFuncLow = this.psiFunctionQ(psiLow, psi, sigma);
        double psiHigh = psi;
        double pFuncHigh = this.psiFunctionQ(psiHigh, psi, sigma);
        if(pFuncHigh*pFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(psi)*1e-6;
        boolean test = true;
        double psiMid = 0.0D;
        double pFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            psiMid = (psiLow + psiHigh)/2.0D;
            pFuncMid = this.psiFunctionQ(psiMid, psi, sigma);
            if(Math.abs(pFuncMid)<=check){
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: sigmaFunction3\nnumber of iterations exceeded in inner bisection\ncurrent value of sigma returned");
                    test = false;
                }
                if(pFuncMid*pFuncHigh>0){
                    psiHigh = psiMid;
                    pFuncHigh = pFuncMid;
                }
                else{
                    psiLow = psiMid;
                    pFuncLow = pFuncMid;
                }
            }
        }

        double sigmaEst = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigmaEst += bulkConcn[i]*this.twoTerm*(Math.exp(-this.expTerm*charges[i]*psiMid) - 1.0D);
        }
        sigmaEst = Fmath.sign(psiMid)*Math.sqrt(sigmaEst);

        return sigma + this.adsorbedChargeDensity - sigmaEst;
    }

    // Function to calculate psi(delta) when specific adsorption occurs
    private double psiFunctionQ(double psiDelta, double psi0, double sigma){

        this.sternDelta = this.calcDeltaQ(psiDelta);
        this.diffPotential = psiDelta;
        this.sternCap = this.epsilonStern*Fmath.EPSILON_0/this.sternDelta;
        return psiDelta - psi0 + sigma/this.sternCap;
    }

    // Calculate the Stern thickness, adsorbed ion concentrations and Stern layer non-adsorbed ion concentrations
    //   for a given potential, psi
    //   and speicific adsorption occurring
    private double calcDeltaQ(double psi){
        double numer = 0.0D;
        double denom = 0.0D;
        double convFac = this.surfaceArea/this.volume;
        int ii = 0;

        // calculate adsorbed ion concentrations
        if(this.nonZeroAssocK==1){
            ii = indexK[0];
            double hold = this.assocConsts[ii]*Math.exp(-charges[ii]*psi*this.expTerm);
            double aa = hold*convFac;
            double bb = -(1.0D + this.initConcn[ii]*hold + this.surfaceSiteDensity*hold*convFac);
            double cc = this.initConcn[ii]*this.surfaceSiteDensity*hold;
            // solve quadratic equatiom
            double root = bb*bb - 4.0D*aa*cc;
            if(root<0.0D){
                System.out.println("Class: GouyChapmanStern\nMethod: calcDeltaQ\nthe square root term (b2-4ac) of the quadratic = "+root);
                System.out.println("this term was set to zero as the negative value MAY have arisen from rounding errors");
                root = 0.0D;
            }
            double qq = -0.5*(bb + Fmath.sign(bb)*Math.sqrt(root));
            double root1 = qq/aa;
            double root2 = cc/qq;
            double limit = this.surfaceSiteDensity*1.001D;

            if(root1>=0.0D && root1<=limit){
                if(root2<0.0D || root2>limit){
                    this.siteConcn[indexK[0]] = root1;
                    this.bulkConcn[indexK[0]] = this.initConcn[indexK[0]] - this.siteConcn[indexK[0]]*this.surfaceArea/this.volume;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: ionConcns");
                    System.out.println("error1: no physically meaningfull root");
                    System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                    System.exit(0);
                }
            }
            else{
                if(root2>=0.0D && root2<=limit){
                    if(root1<0.0D || root1>limit){
                        this.siteConcn[indexK[0]] = root2;
                        this.bulkConcn[indexK[0]] = this.initConcn[indexK[0]] - this.siteConcn[indexK[0]]*this.surfaceArea/this.volume;
                    }
                    else{
                        System.out.println("Class: GouyChapmanStern\nMethod: ionConcns");
                        System.out.println("error2: no physically meaningfull root");
                        System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                        System.exit(0);
                    }
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: ionConcns");
                    System.out.println("error3: no physically meaningfull root");
                    System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                    System.exit(0);
                }
            }
        }
        else{
            // More than one non-zero association constant
            // Minimisation procedure

            // Initial estimates
            double[] vec = new double[this.nonZeroAssocK];
            double[][] mat = new double[this.nonZeroAssocK][this.nonZeroAssocK];

            // set up simultaneous equations
            double expPsiTerm = -psi*this.expTerm;
            for(int i=0; i<this.nonZeroAssocK; i++){
                ii = indexK[i];
                vec[i] = this.assocConsts[ii]*this.initConcn[ii]*this.surfaceSiteDensity*Math.exp(charges[ii]*expPsiTerm);
                for(int j=0; j<this.nonZeroAssocK; j++){
                    mat[i][j] = this.assocConsts[ii]*this.initConcn[ii]*Math.exp(charges[ii]*expPsiTerm);
                    if(i==j)mat[i][j] += 1.0D;
                }
            }

            // solve simultaneous equations to obtain complex concentrations
            Matrix matrix = new Matrix(mat);
            vec = matrix.solveLinearSet(vec);
            for(int i=0; i<this.nonZeroAssocK; i++){
                this.siteConcn[indexK[i]] = vec[i];
            }
        }

        // Create an instances of Minimisation and the minimisation function, GCSminim
        Minimisation min = new Minimisation();
        GCSminim functA = new GCSminim();

        // set minimisation function variables
        functA.psiDelta             = psi;                      // current value of psi(delta) [diffuseLayerPotential]
        functA.tempK                = this.tempK;                // temperature
        functA.surfaceSiteDensity   = this.surfaceSiteDensity;  // surface adsorption site density
        functA.surfaceArea          = this.surfaceArea;         // surface ares
        functA.volume               = this.volume;              // electrolyte volume
        functA.nonZeroAssocK        = this.nonZeroAssocK;       // number of non-zero association constants
        functA.assocK               = this.assocConsts;          // surface site association constants
        functA.initConcn            = this.initConcn;           // initial ion concentrations
        functA.charges              = this.charges;             // ion charges
        functA.indexK               = this.indexK;              // indices of the non-zero associaton constants

        // set initial estimates, step sizes
        double[] start = new double[this.nonZeroAssocK];

        double[] step = new double[this.nonZeroAssocK];
        for(int i=0; i<this.nonZeroAssocK; i++){
            start[i] = this.surfaceSiteDensity/this.nonZeroAssocK;
            step[i] = start[i]*0.05D;
        }

        // covergence tolerance and maximum number of iteration allowed
        double tolerance = this.surfaceSiteDensity*1e-8;
        int maxIter = 100000;

        // Perform minimisation
        min.nelderMead(functA, start, step, tolerance, maxIter);

        // Get best estimates of the site concentrations
        double[] param = min.getParamValues();
        for(int i=0; i<this.nonZeroAssocK; i++){
            ii = indexK[i];
            this.siteConcn[ii] = param[i];
            this.bulkConcn[ii] = this.initConcn[ii] - param[i]*this.surfaceArea/this.volume;
        }

        // Calculte Stern delta and adsorbed charge density
        this.adsorbedChargeDensity = 0.0D;
        double factor1 = -Fmath.Q_ELECTRON*Fmath.N_AVAGADRO;
        double factor2 = this.surfaceArea/this.volume;
        for(int i=0; i<this.numOfIons; i++){
            this.sternConcn[i] = this.bulkConcn[i]*Math.exp(-this.charges[i]*this.expTerm);
            this.adsorbedChargeDensity += this.siteConcn[i]*charges[i]*factor1;
            numer += (this.sternConcn[i] + this.siteConcn[i]*factor2)*this.radii[i];
            denom += this.sternConcn[i] + this.siteConcn[i]*factor2;
        }

        double delta = numer/denom;

        return delta;
    }

    // Method to get the adsorbed ion charge density  [C per square metre]
    public double getAdsorbedChargeDensity(){
        if(!this.sternOption || this.nonZeroAssocK==0){
           return 0.0D;
        }

        if(this.psi0set && this.sigmaSet){
            return this.adsorbedChargeDensity;
        }
        else{
            if(this.sigmaSet){
                this.getSurfacePotential();
                return this.sternPotential;
            }
            else{
                if(this.psi0set){
                    this.getSurfaceChargeDensity();
                    return this.adsorbedChargeDensity;
                }
                else{
                    System.out.println("Class: GouyChapmanStern\nMethod: getAdsorbedChargeDensity\nThe value of the adsorbed ion charge density has not been calculated\nzero returned");
                    System.out.println("Neither a surface potential nor a surface charge density have been entered");
                    return 0.0D;
                }
            }
        }
    }

    // Method to get the diffuse layer charge density  [C per square metre]
    public double getDiffuseChargeDensity(){
        double ads = this.getAdsorbedChargeDensity();
        this.diffuseChargeDensity = -(this.surfaceChargeDensity + ads);
        return this.diffuseChargeDensity;
    }

     // Get the surface potential for a given surface charge density, sigma
    public double getSurfacePotential(double sigma){
        this.surfacePotential = calcSurfacePotential(sigma);
        this.psi0set = true;
        return this.surfacePotential;
    }

    // Calculate the surface potential for a given surface charge density, sigma
    private double calcSurfacePotential(double sigma){
        double surPot = 0.0D;   // temporary values of the surface potential

        if(!this.epsilonSet)throw new IllegalArgumentException("The relative permittivitie/s have not been entered");
        if(!this.tempSet)System.out.println("The temperature has not been entered\na value of 25 degrees Celsius has been used");

        if(this.psi0set && this.sigmaSet)return this.surfacePotential;

        if(!this.unpackArrayList)unpack();

        if(this.sternOption){
            // Gouy-Chapman-Stern
            if(this.nonZeroAssocK==0){
                // No specific adsorption
                if(this.chargeSame){
                    // symmetric electrolyte
                    this.diffPotential = Fmath.asinh(sigma/Math.sqrt(this.eightTerm*this.electrolyteConcn))/(this.chargeValue*this.expTermOver2);
                }
                else{
                    // asymmetric electrolyte
                    this.diffPotential = surfacePotential1(sigma);
                }
                this.sternCap = Fmath.EPSILON_0*this.epsilonStern/this.calcDelta(this.diffPotential);
                surPot = this.diffPotential + sigma/this.sternCap;
                this.totalCap = sigma/this.surfacePotential;
                this.diffCap = sigma/this.diffPotential;
            }
            else{
                // Specific absorption
                if(this.chargeSame){
                    // symmetric electrolyte
                    surPot = surfacePotential2(sigma);
                }
                else{
                    // asymmetric electrolyte
                    surPot = surfacePotential3(sigma);
                }
            }

        }
        else{
            // Gouy-Chapman
            if(this.chargeSame){
                // symmetric electrolyte
                double preterm = Math.sqrt(this.eightTerm*this.electrolyteConcn);
                this.surfacePotential = Fmath.asinh(this.surfaceChargeDensity/preterm)/(this.chargeValue*this.expTermOver2);
            }
            else{
                // asymmetric electrolyte
                surPot = surfacePotential4(sigma);
            }
            this.diffPotential = surPot;
            this.sternPotential = 0.0D;
            this.totalCap = sigma/surPot;
            this.diffCap = sigma/this.diffPotential;
            this.sternCap = Double.POSITIVE_INFINITY;
        }

        return surPot;
    }

   // Calculate the surface charge for the set surface potential
    public double getSurfacePotential(){
        if(!this.sigmaSet)throw new IllegalArgumentException("No surface charge density has been entered");
        return getSurfacePotential(this.surfaceChargeDensity);
    }

    // Calculate surface potential for a asymmetric electrolyte
    // Stern modification ignored
    private double surfacePotential4(double sigma){
        double surPot = 0.0D;   // temporary values of the surface potential

        // bisection method
        double psiLow = 0.0D;
        double pFuncLow = this.psiFunction4(psiLow, sigma);
        double asinhDenom = Math.sqrt(this.eightTerm*this.electrolyteConcn);
        double psiHigh = (10.0D/(this.averageCharge*this.expTerm))*Fmath.asinh(sigma/asinhDenom);
        double pFuncHigh = this.psiFunction4(psiHigh, sigma);
        if(pFuncHigh*pFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(psiHigh)*1e-6;
        boolean test = true;
        double psiMid = 0.0D;
        double pFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            psiMid = (psiLow + psiHigh)/2.0D;
            pFuncMid = this.psiFunction4(psiMid, sigma);
            if(Math.abs(pFuncMid)<=check){
                surPot = psiMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: getSurfacePotential\nnumber of iterations exceeded in outer bisection\ncurrent value of sigma returned");
                    surPot = psiMid;
                    test = false;
                }
                else{
                    if(pFuncLow*pFuncMid>0.0D){
                        psiLow = psiMid;
                        pFuncLow = pFuncMid;
                    }
                    else{
                        psiHigh = psiMid;
                        pFuncHigh = pFuncMid;
                    }
                }
            }
        }
        return surPot;
    }

    // function to calculate surfacePotential function for surfacePotential4()
    private double psiFunction4(double psi, double sigma){
        double sigmaEst = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            sigmaEst += bulkConcn[i]*this.twoTerm*(Math.exp(-this.expTerm*charges[i]*psi) - 1.0D);
        }
        sigmaEst = Fmath.sign(sigma)*Math.sqrt(sigmaEst);

        return sigma - sigmaEst;
    }

    // Calculate surface potential for a given charge density for a asymmetric electrolyte
    // Stern modification without specific adsorption
    private double surfacePotential1(double sigma){
        double difPot = 0.0D;   // temporary values of the diffuse potential

        // bisection method
        double psiLow = 0.0D;
        double pFuncLow = this.psiFunction1(psiLow, sigma);
        double psiHigh = (5.0D/(this.expTerm*this.chargeValue))*Fmath.asinh(sigma/Math.sqrt(this.eightTerm*this.electrolyteConcn));
        double pFuncHigh = this.psiFunction1(psiHigh, sigma);
        if(pFuncHigh*pFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
        double check = Math.abs(psiHigh)*1e-6;
        boolean test = true;
        double psiMid = 0.0D;
        double pFuncMid = 0.0D;
        int nIter = 0;
        while(test){
            psiMid = (psiLow + psiHigh)/2.0D;
            pFuncMid = this.psiFunction1(psiMid, sigma);
            if(Math.abs(pFuncMid)<=check){
                this.diffPotential = psiMid;
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: getSurfacePotential\nnumber of iterations exceeded in outer bisection\ncurrent value of sigma returned");
                    this.diffPotential = psiMid;
                    test = false;
                }
                else{
                    if(pFuncLow*pFuncMid>0.0D){
                        psiLow = psiMid;
                        pFuncLow = pFuncMid;
                    }
                    else{
                        psiHigh = psiMid;
                        pFuncHigh = pFuncMid;
                    }
                }

            }
        }
        return difPot;
    }

    // function to calculate diffPotential for surfacePotential1()
    private double psiFunction1(double psi, double sigma){

        double func = 0.0D;
        for(int i=0; i<this.numOfIons; i++){
            func += this.twoTerm*bulkConcn[i]*(Math.exp(-charges[i]*psi*this.expTerm) - 1.0D);
        }
        return sigma - Fmath.sign(sigma)*Math.sqrt(func);
    }

    // method to calculate the potential at a distance, x, from the surface
    public double getPotentialAtX(double xDistance){
        if(!this.psi0set && !this.sigmaSet)throw new IllegalArgumentException("Neither a surface potential nor a surface charge/density have been entered");
        if(this.sigmaSet && !this.psi0set)this.getSurfacePotential();
        if(this.psi0set && !this.sigmaSet)this.getSurfaceChargeDensity();

        double potAtX = 0.0D;

        if(xDistance==0.0D){
            // distance, x, is zero metres from charged surface
            potAtX = this.surfacePotential;
        }
        else{
            if(this.sternOption){
                 // distance, x, coincides with the Stern interface
                if(xDistance==this.sternDelta){
                    potAtX = this.diffPotential;
                }
                else{
                    if(xDistance<this.sternDelta){
                        // distance, x, is within the Stern layer
                        potAtX = this.surfacePotential - (xDistance/this.sternDelta)*(this.surfacePotential - this.diffPotential);
                    }
                    else{
                        // distance, x, is greater than the Stern layer thickness
                        potAtX = this.calcPotAtX(this.diffPotential, xDistance);
                    }
                }
            }
            else{
                // no Stern layer and distance, x, is greater than zero
                potAtX = this.calcPotAtX(this.surfacePotential, xDistance);
            }
        }
        return potAtX;
    }

    // calculates potential at x for getPotentialAtX()
    private double calcPotAtX(double psiL, double xDistance){
        double potAtX = 0.0D;
        if(this.chargeSame){
            //symmetric elctrolyte
            double kappa = Math.sqrt(2.0D*Fmath.square(Fmath.Q_ELECTRON*this.chargeValue)*Fmath.N_AVAGADRO*this.electrolyteConcn/(Fmath.EPSILON_0*this.epsilon*Fmath.K_BOLTZMANN*this.tempK));
            double expPart = Math.exp(this.expTerm*this.chargeValue*psiL/2.0D);
            double gamma = (expPart - 1.0D)/(expPart + 1.0D);
            double gammaExp = gamma*Math.exp(-kappa*xDistance);
            potAtX = 2.0D*Math.log((1.0D + gammaExp)/(1.0D - gammaExp))/(this.expTerm*this.chargeValue);
        }
        else{
            // asymmetric electrolye
            // Create instance of integration function
            FunctionPatX func = new FunctionPatX();
            func.numOfIons = this.numOfIons;
            func.termOne = 2.0D*Fmath.N_AVAGADRO*Fmath.K_BOLTZMANN*this.tempK/(Fmath.EPSILON_0*this.epsilon);
            func.expTerm = this.expTerm ;
            func.bulkConcn = this.bulkConcn;
            func.charges = this.charges;
            int nPointsGQ = 2000;   // number of points in the numerical integration

            // bisection procedure
            double psiXlow = 0.0;
            double pFuncLow = xDistance - Integration.trapezium(func, psiXlow, psiL, nPointsGQ);
            double psiXhigh = psiL;
            double pFuncHigh = xDistance - Integration.trapezium(func, psiXhigh, psiL, nPointsGQ);
            if(pFuncHigh*pFuncLow>0.0D)throw new IllegalArgumentException("root not bounded");
            double check = Math.abs(xDistance)*1e-2;
            boolean test = true;
            double psiXmid = 0.0D;
            double pFuncMid = 0.0D;
            int nIter = 0;

            while(test){
                psiXmid = (psiXlow + psiXhigh)/2.0D;
                pFuncMid = xDistance - Integration.trapezium(func, psiXmid, psiL, nPointsGQ);
                if(Math.abs(pFuncMid)<=check){
                    potAtX = psiXmid;
                    test = false;
                }
                else{
                    nIter++;
                    if(nIter>10000){
                        System.out.println("Class: GouyChapmanStern\nMethod: getPotentialAtX\nnumber of iterations exceeded in outer bisection\ncurrent value of psi(x) returned");
                        potAtX = psiXmid;
                        test = false;
                    }
                    else{
                        if(pFuncLow*pFuncMid>0.0D){
                            psiXlow = psiXmid;
                            pFuncLow = pFuncMid;
                        }
                        else{
                            psiXhigh = psiXmid;
                            pFuncHigh = pFuncMid;
                        }
                    }
                }
            }
        }
        return potAtX;
    }

    // Get concentrations at a distance, x, from the interface(M)
    public double[] getConcnsAtX(double xDistance){
        if(!this.psi0set && !this.sigmaSet)throw new IllegalArgumentException("Neither a surface potential nor a surface charge/density have been entered");
        if(this.sigmaSet && !this.psi0set)this.getSurfacePotential();
        if(this.psi0set && !this.sigmaSet)this.getSurfaceChargeDensity();

        double[] conc = new double[this.numOfIons];
        if(this.sternOption && xDistance<this.sternDelta){
            for(int i=0; i<this.numOfIons; i++)conc[i] = 0.0D;
        }
        else{
            double psi = this.getPotentialAtX(xDistance);
            for(int i=0; i<this.numOfIons; i++)conc[i] = bulkConcn[i]*Math.exp(-this.expTerm*this.charges[i]*psi);
        }
        return conc;
    }

    // Get initial concentrations (M)
    public double[] getInitConcns(){
        if(!this.psi0set && !this.sigmaSet)unpack();
        double[] conc = Conv.copy(this.initConcn);
        for(int i=0; i<this.numOfIons; i++)conc[i] *= 1e-3;
        return conc;
    }

    // Get equilibrium bulk concentrations (M)
    public double[] getBulkConcns(){
        if(!this.psi0set && !this.sigmaSet)throw new IllegalArgumentException("Neither a surface potential nor a surface charge/density have been entered");
        if(this.sigmaSet && !this.psi0set)this.getSurfacePotential();
        if(this.psi0set && !this.sigmaSet)this.getSurfaceChargeDensity();

        double[] conc = Conv.copy(this.bulkConcn);
        for(int i=0; i<this.numOfIons; i++)conc[i] *= 1e-3;
        return conc;
    }

    // Get adsorbed ion concentrations (mol m-2)
    public double[] getSiteConcns(){
        if(!this.psi0set && !this.sigmaSet)throw new IllegalArgumentException("Neither a surface potential nor a surface charge/density have been entered");
        if(this.sigmaSet && !this.psi0set)this.getSurfacePotential();
        if(this.psi0set && !this.sigmaSet)this.getSurfaceChargeDensity();

        return this.siteConcn;
    }

    // Calculate the surface potential
    // Gouy-Chapman-Stern - Specific absorption - symmetric electrolyte
    private double surfacePotential2(double sigma){
        double surPot = 0.0D;   // temporary values of the surface potential

        // INITIAL ESTIMATES
        // Ignore specific absorption
        double diffPot = Fmath.asinh(sigma/Math.sqrt(this.eightTerm*this.electrolyteConcn))/(this.chargeValue*this.expTermOver2);
        // Recalculate total charge with adsorption
        // double totCharge = getSurfaceChargeDensity(diffPot);
        double pLow = 0.0D;
        double pHigh = 0.0D;
        if(diffPot>0.0D){
            pHigh = 2.0D*diffPot;
        }
        else{
            pLow = 2.0D*diffPot;
        }
        // call bisection method
        surPot = surfacePotentialBisection(pLow, pHigh, sigma, 2);

        return surPot;
    }


    // Bisection method for surfacePotential2 and surfacePotential3
    // n = 2 for surfacePotential2 and n = 3 for surfacePotential3
    private double surfacePotentialBisection(double pLow, double pHigh, double sigma, int n){
        double surPot = 0.0D;   // temporary values of the surface potential

        // Bisection method converging on sigma calculated = true sigma
        boolean testC = true;
        int testCiter = 0;
        int testCmax = 10;
        double pDiff = pHigh-pLow;
        double cLow = cFunction(pLow, sigma);
        double cHigh = cFunction(pHigh, sigma);
        while(testC){
            if(pHigh*pLow>0.0D){
                testCiter++;
                if(testCiter>testCmax)throw new IllegalArgumentException("root not bounded after " + testCiter + "expansions");
                pLow  -= pDiff;
                pHigh += pDiff;
                cLow = cFunction(pLow, sigma);
                cHigh = cFunction(pHigh, sigma);
            }
            else{
                testC=false;
            }
        }
        double check = Math.abs(sigma)*1e-6;
        boolean test = true;
        double cMid = 0.0D;
        int nIter = 0;
        while(test){
            surPot = (pLow + pHigh)/2.0D;
            cMid = this.cFunction(surPot, sigma);
            if(Math.abs(cMid)<=check){
                test = false;
            }
            else{
                nIter++;
                if(nIter>10000){
                    System.out.println("Class: GouyChapmanStern\nMethod: surfacePotential"+n +"\nnumber of iterations exceeded in bisection\ncurrent value of sigma returned");
                    test = false;
                }
                if(cMid*cHigh>0){
                    pHigh = surPot;
                    cHigh = cMid;
                }
                else{
                    pLow = surPot;
                    cLow = cMid;
                }
            }
        }

        return surPot;
    }

    //  Function to calculate difference between set value of the charge density and the estimated value for a current estimate of the surface potential
    private double cFunction(double psi, double sigma){
        double sigmaEst = this.calcSurfaceChargeDensity(psi);
        return sigmaEst - sigma;
    }

    // Gouy-Chapman-Stern - Specific absorption - asymmetric electrolyte
    private double surfacePotential3(double sigma){
        double surPot = 0.0D;   // temporary values of the surface potential

        // INITIAL ESTIMATES
        // Ignore specific absorption
        double diffPot = Fmath.asinh(sigma/Math.sqrt(this.eightTerm*this.electrolyteConcn))/(this.chargeValue*this.expTermOver2);
        // Recalculate total charge with adsorption
        // double totCharge = getSurfaceChargeDensity(diffPot);
        double pLow = 0.0D;
        double pHigh = 0.0D;
        if(diffPot>0.0D){
            pHigh = 2.0D*diffPot;
        }
        else{
            pLow = 2.0D*diffPot;
        }
        // call bisection method
        surPot = surfacePotentialBisection(pLow, pHigh, sigma, 3);

        return surPot;
    }
}





