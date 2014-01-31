/*
*   Classes Donnan
*
*   Class Donnan contains the primary methods for
*   calculating a Donnan Potential between two
*   partitions of different electrical permittivity
*   (dielectric constant) between which any number of
*   ionic species may be partitioned and, in one
*   partition only, may bind to a neutral ionophore.
*
*   Class Donnan requires Class DonnanMinim and Class DonnanConcn
*   that implement interfaces to the required minimisation methods
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:           November 2004
*   LAST UPDATE:    1 December 2004, 5-7 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Donnan.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) November 2004
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
import javax.swing.JOptionPane;

public class Donnan{


    private ArrayList<Object> arrayl = new ArrayList<Object>();      //  vector holding:
                                            //  element 0:  name of first ion
                                            //  element 1:  initial concentration of first ion in partition A (enter as M)
                                            //  element 2:  initial concentration of first ion in partition B (enter as M)
                                            //  element 3:  association constant with ionophore (enter as M^-1)
                                            //  element 4:  radius of first ion (metres)
                                            //  element 5:  charge of first ion
                                            //  element 6:  partition coefficient if provided by the user
                                            //              -1 if not; partition coefficient to be calculated later
                                            //  element 7:  name of second ion
                                            //  etc
    private int numOfIons = 0;              //  number of ionic species
    private int numOfAnions = 0;            //  number of anionic species
    private int numOfCations = 0;           //  number of cationic species
    private String[] ionNames = null;       //  names of the ions
    private double[] concnA = null;         //  concentration of ions in partition A (moles per cubic metre)
    private double[] concnB = null;         //  concentration of ions in partition B (moles per cubic metre)
    private double[] molesT = null;         //  total moles of an ion
    private double[] complex = null;        //  concentration of complex in partition B (moles per cubic metre)
    private double[] excessConcnA = null;   //  excess concentration of ions in partition A in interfacial charge (moles per cubic metre)
    private double[] excessConcnB = null;   //  excess concentration of ions in partition B in interfacial charge (moles per cubic metre)
    private double[] excessComplex = null;  //  excess concentration of complex in partition B in interfacial charge (moles per cubic metre)
    private int[] indexC = null;            //  ion index of ionic species with non zero total concentrations
    private int nonZeroConcns = 0;          //  number of ionic species with a non-zero total concentration
    private double[] assocConsts = null;    //  association constants of the ions with the ionophore (cubic metres per mole)
    private int[] indexK = null;            //  ion index of ionic species with non zero assocConsts
    private int nonZeroAssocK = 0;          //  number of ionic species with affinity for the ionophore
    private double[] radii = null;          //  radii of ions
    private boolean radiusType = true;      //  if = true - hydrated radii are taken from Class IonicRadii
                                            //  if = false - bare radii are taken from Class IonicRadii
    private double[] charges = null;        //  charge of ions
    private double tol = 1e-6;              //  fractional tolerance in checking for overall charge neutrality
                                            //  when multiplied by total concentration of species of lowest concentration
                                            //  gives limit for considering overall neurtality achieved
    private String ionophore = "ionophore"; //  name ionophore for output texts
    private double ionophoreConcn = 0.0D;   //  ionophore concentration - entered as molar, converted to moles per cubic metre
    private double freeIonophoreConcn = 0.0D;   // ionophore concentration - all complex concentrations
    private double ionophoreRad = 0.0D;     //  ionophore radius (metres)
    private boolean ionophoreSet = false;   //  = true when ionophore concentration and radius have been set
    private double volumeA = 0.0D;          //  volume of partition A (cubic metres)
    private double volumeB = 0.0D;          //  volume of partition B (cubic metres)
    private double interfacialArea = 0.0D;  //  onterfacial area between Partitions A and B(square metres)
    private boolean volumeSet = false;      //  = true when volumeA and volumeB have been set
    private double epsilonA = 0.0D;         //  relative electrical permittivity of partition A
    private double epsilonB = 0.0D;         //  relative electrical permittivity of partition B
    private double epsilonSternA = 0.0D;    //  relative electrical permittivity of partition A Stern layer
    private double epsilonSternB = 0.0D;    //  relative electrical permittivity of partition B SternLayer
    private boolean epsilonSet = false;     //  = true when epsilonA and epsilonB have been set
    private double temp = 25.0-Fmath.T_ABS; //  Temperature (degrees Kelvin) [Enter temperature in degrees Celsius]
    private boolean tempSet = false;        //  = true when temperature has been set
    private double[] deltaMu0 = null;       //  difference in Born charging energy (partitionB - partitionA)
    private double[] partCoeff = null;      //  partition coefficients in absence of inter-partition, e.g. Donnan, potential
    private double[] partCoeffPot = null;   //  partition coefficients in presence of inter-partition, e.g. Donnan, potential
    private boolean[] indexPC = null;       //  partition coefficient (pc) index
                                            //  = true if pc provided by user
                                            //  = false if pc calculated from Born charging
    private double donnanPotential = 0.0D;  //  Donanan potential with respect to partition A (psiB - psiA) [volts]
    private double diffPotentialA = 0.0D;   //  Double layer potential difference - compartment A [volts]
    private double diffPotentialB = 0.0D;   //  Double layer potential difference - compartment B [volts]
    private double sternPotential = 0.0D;   //  Stern potential difference  [volts]
    private double estimate = 0.0D;         //  Initial Estimate of the Donanan potential with respect to partition A (psiB - psiA) [volts]
    private double step = 0.0D;             //  Initial step size in the estimation of the Donanan potential with respect to partition A (psiB - psiA) [volts]
    private double tolerance = 1.0e-20;     //  tolerance in exiting estimation of the Donanan potential with respect to partition A (psiB - psiA)
    private int nMaxIter = 10000;           //  maximum number of iterations allowed in estimation of the Donanan potential with respect to partition A (psiB - psiA)
    private int numIterations = 0;          //  number of iterations taken
    private double minimum = 1.0e300;       //  value of function to be minimised at the minimum
    private double sternCap = 0.0D;         //  Stern layer capacitance [F]
    private double diffCapA = 0.0D;         //  diffuse double layer capacitance in partition A [F]
    private double diffCapB = 0.0D;         //  diffuse double layer capacitance in partition B [F]
    private double donnanCap = 0.0D;        //  total interfacial capacitance [F]
    private double sternDeltaA = 0.0D;      //  Stern layer thickness in partition A [m]
    private double sternDeltaB = 0.0D;      //  Stern layer thickness in partition B [m]
    private double chargeValue = 0;         //  Absolute value of the charge valency if all are the same, i.e. symmetric electrolytes of the same valency
    private boolean chargeSame = true;      //  = false if chargeValue not the same for all ions
    private double interfacialChargeDensity = 0.0D;// interface Charge Density [C per square metre]
    private double interfacialCharge = 0.0D;       // interface Charge [C]
    private boolean includeIc = true;       //  = true - interface charge included in the calculation of the Donnan potential
                                            //  = false - interface charge ignored in the calculation of the Donnan Poptential
    private double[] ratioA = null;         //  ratio of excess to bulk concentrations - compartment A
    private double[] ratioB = null;         //  ratio of excess to bulk concentrations - compartment B
    private double[] ratioC = null;         //  ratio of excess to bulk concentrations - complex
    private double recipKappaA = 0.0D;      //  Debye length - compartment A
    private double recipKappaB = 0.0D;      //  Debye length - compartment B

    // Constructor
    public Donnan(){
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

    // Method for setting Donnan Potential calculatiom method to ignore interfacial charge
    public void ignoreInterfaceCharge(){
        this.includeIc = false;
    }

     // Method for setting Donnan Potential calculatiom method to include interfacial charge
     // This is the default option
    public void includeInterfaceCharge(){
        this.includeIc = true;
    }


    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient to be calculated from Born charging equation
    // Concentrations - Molar,  assocK - M^-1, radius - metres, charge - valency e.g. +1
    public void setIon(String ion, double concnA, double concnB, double assocK, double radius, int charge){
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
        this.arrayl.add(new Double(radius));
        this.arrayl.add(new Integer(charge));
        this.arrayl.add(new Double(-1.0D));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient supplied by user
    // Concentrations - Molar,  assocK - M^-1, radius - metres, charge - valency e.g. +1
    public void setIon(double partCoeff, String ion, double concnA, double concnB, double assocK, double radius, int charge){
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
        this.arrayl.add(new Double(radius));
        this.arrayl.add(new Integer(charge));
        this.arrayl.add(new Double(partCoeff));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient to be calculated from Born charging equation
    // Concentrations - Molar,  radius - metres, charge - valency e.g. +1
    // association constant = 0.0D
    public void setIon(String ion, double concnA, double concnB, double radius, int charge){
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(0.0D));
        this.arrayl.add(new Double(radius));
        this.arrayl.add(new Integer(charge));
        this.arrayl.add(new Double(-1.0D));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient to be supplied by user
    // Concentrations - Molar,  radius - metres, charge - valency e.g. +1
    // association constant = 0.0D
    public void setIon(double partCoeff, String ion, double concnA, double concnB, double radius, int charge){
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(0.0D));
        this.arrayl.add(new Double(radius));
        this.arrayl.add(new Integer(charge));
        this.arrayl.add(new Double (partCoeff));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient to be calculated from Born charging equation
    // default radii and charge taken from class IonicRadii
    // if radii not in Ionic Radii, Donnan potential calculated with interface charge neglected
    // Concentrations - Molar
    public void setIon(String ion, double concnA, double concnB, double assocK){
        IonicRadii ir = new IonicRadii();
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
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
        this.arrayl.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.arrayl.add(new Integer(charg));
        this.arrayl.add(new Double(-1.0D));
        this.numOfIons++;
    }

   // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficient to be supplied by user
    // default radii and charge taken from class IonicRadii
    // Concentrations - Molar
    public void setIon(double partCoeff, String ion, double concnA, double concnB, double assocK){
        IonicRadii ir = new IonicRadii();
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(assocK));
        if(assocK!=0.0D)this.nonZeroAssocK++;
        double rad = 0.0D;
        if(this.includeIc){
            if(this.radiusType){
                rad = ir.hydratedRadius(ion);
            }
            else{
                rad = ir.radius(ion);
            }

            if(rad==0.0D){
                String mess1 = ion + " radius is not in the IonicRadii list\n";
                String mess2 = "Please enter radius in metres\n";
                String mess3 = "Enter 0.0 if you wish interfacial charge to be neglected";
                rad = Db.readDouble(mess1+mess2+mess3);
                if(rad==0.0D)this.includeIc = false;
            }
        }
        this.arrayl.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.arrayl.add(new Integer(charg));
        this.arrayl.add(new Double(partCoeff));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficients to be calculated from Born charging equation
    // default radii and charge taken from class IonicRadii
    // association constant = 0.0D
    // Concentrations - Molar
    public void setIon(String ion, double concnA, double concnB){
        IonicRadii ir = new IonicRadii();
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(0.0D));
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
            if(rad==0.0D)this.includeIc = false;
        }
        this.arrayl.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.arrayl.add(new Integer(charg));
        this.arrayl.add(new Double(-1.0D));
        this.numOfIons++;
    }

    // Method to add an ionic species to the  Donnan Equilibrium
    // Partition coefficients to be supplied by the user
    // default radii and charge taken from class IonicRadii
    // association constant = 0.0D
    // Concentrations - Molar
    public void setIon(double partCoeff, String ion, double concnA, double concnB){
        IonicRadii ir = new IonicRadii();
        this.arrayl.add(ion);
        this.arrayl.add(new Double(concnA));
        this.arrayl.add(new Double(concnB));
        if(concnA>0.0D || concnB>0.0)this.nonZeroConcns++;
        this.arrayl.add(new Double(0.0D));
        double rad = 0.0D;
        if(this.includeIc){
            if(this.radiusType){
                rad = ir.hydratedRadius(ion);
            }
            else{
                rad = ir.radius(ion);
            }

            if(rad==0.0D){
                String mess1 = ion + " radius is not in the IonicRadii list\n";
                String mess2 = "Please enter radius in metres\n";
                String mess3 = "Enter 0.0 if you wish interfacial charge to be neglected";
                rad = Db.readDouble(mess1+mess2+mess3);
                if(rad==0.0D)this.includeIc = false;
            }
        }
        this.arrayl.add(new Double(rad));
        int charg = 0;
        charg = ir.charge(ion);
        if(charg==0){
            String mess1 = ion + " charge is not in the IonicRadii list\n";
            String mess2 = "Please enter charge, e.g +2";
            charg = Db.readInt(mess1+mess2);
        }
        this.arrayl.add(new Integer(charg));
        this.arrayl.add(new Double(partCoeff));
        this.numOfIons++;
    }

    // Method to add an ionophore to the partition B
    // Concentration = Molar, radius = metres
    public void setIonophore(double concn, double radius){
        this.ionophoreConcn = concn*1e3;
        this.ionophoreRad = radius;
        this.ionophoreSet = true;
    }

    // Method to add an ionophore to the partition B
    // Concentration = Molar, radius = metres
    // Ionophore name can be added for output text
    public void setIonophore(String ionophore, double concn, double radius){
        this.ionophore = ionophore;
        this.ionophoreConcn = concn*1e3;
        this.ionophoreRad = radius;
        this.ionophoreSet = true;
    }

    // Method to add an ionophore to the partition B
    // No radius added - calculation of Donnan potential will neglect interface charge
    // Concentration = Molar
    // Ionophore name can be added for output text
    public void setIonophore(String ionophore, double concn){
        this.ionophore = ionophore;
        this.ionophoreConcn = concn*1e3;
        this.includeIc = false;
        this.ionophoreSet = true;
    }

    // Method to add an ionophore to the partition B
    // No radius added - calculation of Donnan potential will neglect interface charge
    // Concentration = Molar
    public void setIonophore(double concn){
        this.ionophoreConcn = concn*1e3;
        this.includeIc = false;
        this.ionophoreSet = true;
    }

    // Method to set partition volumes (m^3) and interfacial area
    public void setVolumes(double volA, double volB, double area){
        this.volumeA = volA;
        this.volumeB = volB;
        this.interfacialArea = area;
        this.volumeSet = true;
    }

    // Method to set partition volumes (m^3)
    // No interfacial area entered - calculation of Donnan potential will neglect interface charge
    public void setVolumes(double volA, double volB){
        this.volumeA = volA;
        this.volumeB = volB;
        this.includeIc = false;
        this.volumeSet = true;
    }

    // Method to set partition relative permittivities
    public void setRelPerm(double epsA, double epsB, double epsSternA, double epsSternB){
        this.epsilonA = epsA;
        this.epsilonB = epsB;
        this.epsilonSternA = epsSternA;
        this.epsilonSternB = epsSternB;
        this.epsilonSet = true;
    }

    // Method to set partition relative permittivities
    // No Stern layer permittivities included - interface charge cannot be calculated
    public void setRelPerm(double epsA, double epsB){
        this.epsilonA = epsA;
        this.epsilonB = epsB;
        this.includeIc = false;
        this.epsilonSet = true;
    }

    // Method to set temperature (enter as degrees Celsius)
    public void setTemp(double temp){
        this.temp = temp - Fmath.T_ABS;
        this.tempSet = true;
    }

    // Method to set initial estimate of the Donnan potential (V)
    public void setEstimate(double pot){
        this.estimate = pot;
    }

    // Method to set initial step size of the Donnan potential Estimations (V)
    public void setStep(double step){
        this.step = step;
    }

    // Method to set tolerance in exiting the Donnan potential calculation
    public void setTolerance(double tol){
        this.tolerance = tol;
    }

    // Method to set maximum number of iterations in calculating the Donnan potential calculation
    public void setMaxIterations(int nMax){
        this.nMaxIter = nMax;
    }

    // Method to get the Donnan potential [volts]
    public double getDonnanPotential(){
        return this.donnanPotential;
    }

    // Method to get the diffuse double layer potential in partition A [volts]
    public double getDiffuseLayerPotentialA(){
        return this.diffPotentialA;
    }

    // Method to get the diffuse double layer potential in partition B [volts]
    public double getDiffuseLayerPotentialB(){
        return this.diffPotentialB;
    }

    // Method to get the Stern layer potential [volts]
    public double getSternLayerPotential(){
        return this.sternPotential;
    }

    // Method to get the bulk concentrations of the ionic species, in partition A, at the minimum [M]
    public double[] getConcnA(){
        double[] concn = Conv.copy(this.concnA);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the bulk concentrations of the ionic species, in partition B, at the minimum [M]
    public double[] getConcnB(){
        double[] concn = Conv.copy(this.concnB);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the bulk concentrations of the complex species, in partition B, at the minimum [M]
    public double[] getComplex(){
        double[] concn = Conv.copy(this.complex);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the excess concentrations of the ionic species, in partition A, at the minimum [M]
    public double[] getExcessConcnA(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getExcessConcnA\nThe values of the excess concentrations have not been calculated\nzeros returned");
        }
        double[] concn = Conv.copy(this.excessConcnA);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the excessConcentrations of the ionic species, in partition B, at the minimum [M]
    public double[] getExcessConcnB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getExcessConcnA\nThe values of the excess concentrations have not been calculated\nzeros returned");
        }
        double[] concn = Conv.copy(this.excessConcnB);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the excess concentrations of the complex species, in partition B, at the minimum [M]
    public double[] getExcessComplex(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getExcessConcnA\nThe values of the excess concentrations have not been calculated\nzeros returned");
        }
        double[] concn = Conv.copy(this.excessComplex);
        for(int i=0; i<this.numOfIons; i++)concn[i] *= 1e-3;
        return concn;
    }

    // Method to get the ratio of excess concentrations over bulk concentrations of the ionic in partition A
    public double[] getRatioA(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getRatioA\nThe values of the excess to bulk concentrations have not been calculated\nzeros returned");
        }
      return this.ratioA;
    }

    // Method to get the ratio of excess concentrations over bulk concentrations of the ionic but not complex species, in partition B
    public double[] getRatioB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getRatioB\nThe values of the excess to bulk concentrations have not been calculated\nzeros returned");
        }
      return this.ratioB;
    }

    // Method to get the ratio of excess concentrations over bulk concentrations of the complex species
    public double[] getRatioComplex(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getRatioComplex\nThe values of the excess to bulk concentrations have not been calculated\nzeros returned");
        }
      return this.ratioC;
    }

    // Method to get the partition coefficients at equilibrium
    public double[] getPartitionCoefficients(){
      return this.partCoeffPot;
    }

    // Method to get the partition coefficients in the absence of a Donnan Potential
    public double[] getPartitionCoefficientsZero(){
      return this.partCoeff;
    }

    // Method to get the difference in Born Charging energy between partitions A and B
    public double[] getDeltaMu0(){
        return this.deltaMu0;
    }

     // Method to get the total interfacial charge
    public double getInterfaceCharge(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getInterfaceCharge\nThe value of the interface charge has not been calculated\nzero returned");
        }
        return this.interfacialCharge;
    }

    // Method to get the interfacial charge density
    public double getInterfaceChargeDensity(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getInterfaceChargeDensity\nThe value of the interface charge density has not been calculated\nzero returned");
        }
        return this.interfacialCharge;
    }

    // Method to get the Stern Capacitance
    public double getSternCapacitance(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getSternCapacitance\nThe value of the Stern capacitance has not been calculated\nzero returned");
        }
        return this.sternCap*this.interfacialArea;
    }


    // Method to get the diffuse layer Capacitance in Partition A
    public double getDiffuseLayerCapacitanceA(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getDiffuseLayerCapacitanceA\nThe values of the diffuse layer capacitances have not been calculated\nzero returned");
        }
        return this.diffCapA*this.interfacialArea;
    }


    // Method to get the diffuse layer Capacitance in Partition B
    public double getDiffuseLayerCapacitanceB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getDiffuseLayerCapacitanceB\nThe values of the diffuse layer capacitances have not been calculated\nzero returned");
        }
        return this.diffCapB*this.interfacialArea;
    }

    // Method to get the Donnan Capacitance
    public double getDonnanCapacitanceB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getDonnanCapacitance\nThe value of the Donnan capacitance has not been calculated\nzero returned");
        }
        return this.donnanCap*this.interfacialArea;
    }

    // Method to get the Stern layer thickness in Partition A
    public double getSternThicknessA(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getSternThicknessA\nThe values of the Stern layer thicknesses have not been calculated\nzero returned");
        }
        return this.sternDeltaA;
    }

    // Method to get the Stern layer thickness in Partition B
    public double getSternThicknessB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getSternThicknessB\nThe values of the Stern layer thicknesses have not been calculated\nzero returned");
        }
        return this.sternDeltaB;
    }

    // Method to get the Debye length in Partition A
    public double getDebyeLengthA(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getDebyeLengthA\nThe values of the Debye lengths have not been calculated\nzero returned");
        }
        return this.recipKappaA;
    }

    // Method to get the Debye length in Partition A
    public double getDebyeLengthB(){
        if(!this.includeIc){
            System.out.println("Class: Donnan\nMethod: getDebyeLengthB\nThe values of the Debye lengths have not been calculated\nzero returned");
        }
        return this.recipKappaB;
    }

    // Method to get the minimised function (square of charge in B) value at the minimum
    public double getMinimum(){
        return this.minimum;
    }

    // Method to calculate the Donnan potential (V)
    public double calcPotential(){
        // check all information needed is present and check overall charge neutrality
        unpack();

        // Repack to eliminate zero concentration ions
        double numOfIonsHold = this.numOfIons;
        double[] assocConstshold = null;
        double[] radiihold = null;
        double[] chargeshold = null;
        double[] deltaMu0hold = null;
        double[] partCoeffhold = null;

        if(this.nonZeroConcns<this.numOfIons){
            assocConstshold = Conv.copy(this.assocConsts);
            radiihold = Conv.copy(this.radii);
            chargeshold = Conv.copy(this.charges);
            deltaMu0hold = Conv.copy(this.deltaMu0);
            partCoeffhold = Conv.copy(this.partCoeff);
            boolean test = true;
            int jj = 0;
            while(test){
                if(indexC[jj]==0){
                    for(int k=jj+1; k<this.numOfIons; k++){
                        this.concnA[k-1] = this.concnA[k];
                        this.concnB[k-1] = this.concnB[k];
                        this.complex[k-1] = this.complex[k];
                        this.molesT[k-1] = this.molesT[k];
                        this.assocConsts[k-1] = this.assocConsts[k];
                        this.radii[k-1] = this.radii[k];
                        this.charges[k-1] = this.charges[k];
                        this.deltaMu0[k-1] = this.deltaMu0[k];
                        this.partCoeff[k-1] = this.partCoeff[k];
                    }
                    this.numOfIons--;
                }
                else{
                    jj++;
                }
                if(this.numOfIons==this.nonZeroConcns)test=false;
            }
        }

        // Obtain Donnan potential by miminimising
        // the square of the net charge in partition B

        // Check if interface charge is to be include
        // if it is - perform calculation first without interface charge
        // to obtain workable initial estimates for calculation with interface charge included
        boolean includeIcHold = false;
        if(this.includeIc){
            includeIcHold = true;
            this.includeIc = false;
        }

        //Create instance of Minimisation
        Minimisation minPot = new Minimisation();

        // Create instace of class holding function to be minimised
        DonnanMinim functD = new DonnanMinim(this.numOfIons);

        // Initialise function functD variable

        functD.numOfIons        = this.numOfIons;
        functD.concnA           = this.concnA;
        functD.concnB           = this.concnB;
        functD.molesT           = this.molesT;
        functD.complex          = this.complex;
        functD.excessConcnA     = this.excessConcnA;
        functD.excessConcnB     = this.excessConcnB;
        functD.excessComplex    = this.excessComplex;
        functD.assocConsts      = this.assocConsts;
        functD.indexK           = this.indexK;
        functD.nonZeroAssocK    = this.nonZeroAssocK;
        functD.radii            = this.radii;
        functD.charges          = this.charges;
        functD.ionophoreConcn   = this.ionophoreConcn;
        functD.ionophoreRad     = this.ionophoreRad;
        functD.volumeA          = this.volumeA;
        functD.volumeB          = this.volumeB;
        functD.interfacialArea  = this.interfacialArea;
        functD.epsilonA         = this.epsilonA;
        functD.epsilonB         = this.epsilonB;
        functD.epsilonSternA    = this.epsilonSternA;
        functD.epsilonSternB    = this.epsilonSternB;
        functD.temp             = this.temp;
        functD.partCoeff        = this.partCoeff;
        functD.partCoeffPot     = this.partCoeffPot;
        functD.sternCap         = this.sternCap;
        functD.sternDeltaA      = this.sternDeltaA;
        functD.sternDeltaB      = this.sternDeltaB;
        functD.chargeValue      = this.chargeValue;
        functD.chargeSame       = this.chargeSame;
        functD.interfacialCharge = this.interfacialCharge;
        functD.interfacialChargeDensity = this.interfacialChargeDensity;
        functD.includeIc        = this.includeIc;


        // initial estimate
        double[] start = {this.estimate};
        // initial step size
        double[] step = {this.step};

        // Nelder and Mead minimisation procedure
        minPot.nelderMead(functD, start, step, this.tolerance, this.nMaxIter);

        // get values of the Donnan potential at minimum
        double[] param = minPot.getParamValues();
        this.donnanPotential = param[0];

        // Repeat with interface charge included if this is required
        if(includeIcHold){
            this.includeIc = true;

            // initial estimate
            start[0] = this.donnanPotential;
            // initial step size
            step[0] = this.step;

            // Nelder and Mead minimisation procedure
            minPot.nelderMead(functD, start, step, this.tolerance, this.nMaxIter);

            // get values of the Donnan potential at minimum
            param = minPot.getParamValues();
            this.donnanPotential = param[0];
        }

        // Calculate concentartions at the minimum
        ionConcns(this.donnanPotential);

        // Repack if zero concentration ions were present
        if(this.nonZeroConcns!=numOfIonsHold){
            boolean test = true;
            int jj = 0;
            while(test){
                if(indexC[jj]==0){
                    for(int k=jj; k<this.numOfIons; k++){
                        this.concnA[k+1] = this.concnA[k];
                        this.concnB[k+1] = this.concnB[k];
                        this.complex[k+1] = this.complex[k];
                        this.excessConcnA[k+1] = this.excessConcnA[k];
                        this.excessConcnB[k+1] = this.excessConcnB[k];
                        this.excessComplex[k+1] = this.excessComplex[k];
                        this.molesT[k+1] = this.molesT[k];
                        this.assocConsts[k+1] = this.assocConsts[k];
                        this.radii[k+1] = this.radii[k];
                        this.charges[k+1] = this.charges[k];
                        this.deltaMu0[k+1] = this.deltaMu0[k];
                        this.partCoeff[k+1] = this.partCoeff[k];
                        this.partCoeffPot[k+1] = this.partCoeffPot[k];
                    }
                    this.numOfIons++;
                    this.concnA[jj] = 0.0D;
                    this.concnB[jj] = 0.0D;
                    this.complex[jj] = 0.0D;
                    this.excessConcnA[jj] = 0.0D;
                    this.excessConcnB[jj] = 0.0D;
                    this.excessComplex[jj] = 0.0D;
                    this.molesT[jj] = 0.0D;
                    this.assocConsts[jj] = assocConstshold[jj];
                    this.radii[jj] = radiihold[jj];
                    this.charges[jj] = chargeshold[jj];
                    this.deltaMu0[jj] = deltaMu0hold[jj];
                    this.partCoeff[jj] = partCoeffhold[jj];
                }
                else{
                    jj++;
                }
                if(this.numOfIons==this.nonZeroConcns)test=false;
            }
        }

        // get the minimum value
        this.minimum = minPot.getMinimum();

        // get the number of iterations
        this.numIterations = minPot.getNiter();

        if(this.includeIc){
            // Calculate excess to bulk ratios
            for(int i=0; i<this.numOfIons; i++){
                this.ratioA[i] = this.excessConcnA[i]/this.concnA[i];
                this.ratioB[i] = this.excessConcnB[i]/this.concnB[i];
                this.ratioC[i] = this.excessComplex[i]/this.complex[i];
            }

            // Calculate overall and diffuse layer capacitances
            this.diffCapA = Math.abs(this.interfacialCharge/this.diffPotentialA);
            this.diffCapB = Math.abs(this.interfacialCharge/this.diffPotentialB);
            this.donnanCap = Math.abs(this.interfacialCharge/this.donnanPotential);

            // Calculate Debye lengths
            double preterm = 2.0D*Fmath.square(Fmath.Q_ELECTRON)*Fmath.N_AVAGADRO/(Fmath.EPSILON_0*Fmath.K_BOLTZMANN*this.temp);
            double pretermA = preterm/this.epsilonA;
            double pretermB = preterm/this.epsilonB;
            this.recipKappaA = 0.0;
            this.recipKappaB = 0.0;
            for(int i=0; i<this.numOfIons; i++){
                this.recipKappaA += this.concnA[i]*charges[i]*charges[i];
                this.recipKappaB += (this.concnB[i] + this.complex[i])*charges[i]*charges[i];
            }
            this.recipKappaA = 1.0D/Math.sqrt(this.recipKappaA*pretermA);
            this.recipKappaB = 1.0D/Math.sqrt(this.recipKappaB*pretermB);

            // Calculate deltaMu0s for user supplied partition coefficients
            // and scale all deltaMu0s to per mole
            for(int ii=0; ii<this.numOfIons; ii++){
                if(this.indexPC[ii]){
                    this.deltaMu0[ii] = Math.log(this.partCoeff[ii])*(Fmath.N_AVAGADRO*Fmath.K_BOLTZMANN)*this.temp;
                }
                else{
                    this.deltaMu0[ii] *= Fmath.N_AVAGADRO;
                }
            }

            // Calculate partition coefficients at equilibrium
            for(int ii=0; ii<this.numOfIons; ii++){
                this.partCoeffPot[ii] = this.partCoeff[ii]*Math.exp((-this.donnanPotential*this.charges[ii]*Fmath.Q_ELECTRON)/(Fmath.K_BOLTZMANN*this.temp));
            }

        }

        return this.donnanPotential;
    }

    // Unpacks the ion storage ArrayList and fills the appropriate arrays
    // Checks all relevant data has been entered
    private void unpack(){
        if(!this.volumeSet)throw new IllegalArgumentException("The volumes of the partitions have not been set");
        if(this.numOfIons==0)throw new IllegalArgumentException("No ions have been entered");
        if(this.nonZeroConcns==0)throw new IllegalArgumentException("No non-zero ionic concentrations have been entered");
        if(!this.epsilonSet)throw new IllegalArgumentException("The relative permittivities of the partitions have not been set");
        if(!this.tempSet)System.out.println("The temperature has not been entered\na value of 25 degrees Celsius has been used");
        if(!this.ionophoreSet)System.out.println("The ionophore has not been entered\na concentration value of zero has been used");


        // change concentrations to moles per cubic metre
        // fill primitive data arrays
        // calculate total moles
        // check if the electrolyte is charge symmetric
        this.ionNames = new String[this.numOfIons];
        this.concnA = new double[this.numOfIons];
        this.concnB = new double[this.numOfIons];
        this.molesT = new double[this.numOfIons];
        this.complex = new double[this.numOfIons];
        this.excessConcnA = new double[this.numOfIons];
        this.excessConcnB = new double[this.numOfIons];
        this.excessComplex = new double[this.numOfIons];
        this.ratioA = new double[this.numOfIons];
        this.ratioB = new double[this.numOfIons];
        this.ratioC = new double[this.numOfIons];
        this.assocConsts = new double[this.numOfIons];
        this.radii = new double[this.numOfIons];
        this.charges = new double[this.numOfIons];
        this.deltaMu0 = new double[this.numOfIons];
        this.partCoeff = new double[this.numOfIons];
        this.partCoeffPot = new double[this.numOfIons];
        this.indexK = new int[this.nonZeroAssocK];
        this.indexC = new int[this.numOfIons];
        this.indexPC = new boolean[this.numOfIons];
        Double hold = null;
        Integer holi = null;
        int ii = 0;
        this.chargeValue = 0;
        this.chargeSame = true;

        for(int i=0; i<numOfIons; i++){
            // ion name
            this.ionNames[i]= (String)this.arrayl.get(0+i*7);
            // concentration in compartment A
            hold            = (Double)this.arrayl.get(1+i*7);
            this.concnA[i]  = hold.doubleValue()*1e3;
            hold            = (Double)this.arrayl.get(2+i*7);
            // concentration in compartment B
            this.concnB[i]  = hold.doubleValue()*1e3;
            // total moles of ion i
            this.molesT[i]  = this.concnA[i]*volumeA + this.concnB[i]*volumeB;
            if(this.molesT[i]>0.0D){
                indexC[i] = 1;
            }
            else{
                indexC[i] = 0;
            }
            // association constant
            hold            = (Double)this.arrayl.get(3+i*7);
            this.assocConsts[i] = hold.doubleValue()*1e-3;
            if(this.assocConsts[i]>0.0D){
                indexK[ii] = i;
                ii++;
            }
            // ion radius
            hold            = (Double)this.arrayl.get(4+i*7);
            this.radii[i]   = hold.doubleValue();
            // ion charge
            holi            = (Integer)this.arrayl.get(5+i*7);
            this.charges[i] = holi.intValue();
            // running check for all ions having same absolute charge
            if(i==0){
                this.chargeValue = Math.abs(this.charges[0]);
            }
            else{
                if(Math.abs(this.charges[i])!=this.chargeValue)this.chargeSame=false;
            }

            // partition coefficient
            hold        = (Double)this.arrayl.get(6+i*7);
            this.partCoeff[i]= hold.doubleValue();
            this.indexPC[i] = true;
            if(this.partCoeff[i]==-1.0D){
                this.indexPC[i] = false;
                // calculate partition coefficient from Born charging equation
                // calculate differences in Born charging energies between the two partitions
                this.deltaMu0[i] = this.BornChargingEnergy(this.radii[i], this.charges[i], this.epsilonB) - this.BornChargingEnergy(this.radii[i], this.charges[i], this.epsilonA);

                // calculate partition coefficients
                this.partCoeff[i] = Math.exp((this.deltaMu0[i])/(Fmath.K_BOLTZMANN*this.temp));
            }
            // calculate number of anionic and cationic species
            if(charges[i]<0){
                numOfAnions++;
            }
            else{
                numOfCations++;
            }
            if(this.ionophoreConcn==0.0D)this.nonZeroAssocK = 0;
        }


        // Calculate overall charge
        double overallCharge = 0.0D;
        double positives = 0.0D;
        double negatives = 0.0D;
        for(int i=0; i<numOfIons; i++){
            if(charges[i]>0.0D){
                positives += this.molesT[i]*charges[i];
            }
            else{
                negatives += this.molesT[i]*charges[i];
            }
            overallCharge = positives + negatives;
        }
        if(Math.abs(overallCharge)>positives*this.tol){
            String quest0 = "Class: Donnan, method: unpack()\n";
            String quest1 = "Total charge = " + overallCharge + " mol/dm, i.e. is not equal to zero\n";
            String quest2 = "Positive charge = " + positives + " mol/dm\n";
            String quest3 = "Do you wish to continue?";
            String quest = quest0 + quest1 + quest2 + quest3;
            int res = JOptionPane.showConfirmDialog(null, quest, "Neutrality check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(res==1)System.exit(0);
        }
    }

    // Calculate Born charging energy
    public double BornChargingEnergy(double radius, double charge, double epsilon){
        return Fmath.square(Fmath.Q_ELECTRON*charge)/(8.0*Math.PI*radius*Fmath.EPSILON_0*epsilon);
    }

    // Method that calculate partitions A and B and ionophore complex concentrations
    // of the iith ion for a given trans partition, e.g. Donnan potential
    public void ionConcns(double potential){

        // calculate partition coefficients for given potential
        for(int ii=0; ii<this.numOfIons; ii++){
            this.partCoeffPot[ii] = this.partCoeff[ii]*Math.exp((-potential*this.charges[ii]*Fmath.Q_ELECTRON)/(Fmath.K_BOLTZMANN*this.temp));
        }

        if(!this.includeIc){
            //  Interface charge ignored
            if(this.nonZeroAssocK<2){
                // Only one or none of the ions with an affinity for the ionophore
                calcConcnsSingleK(potential);
            }
            else{
                // More than one ion competes for the ionophore

                // obtain ionic and complex concentrations for a given Donnan potential
                // by miminimising the sum of the squares of the equations describing each equilibrium
                calcConcnsMultiK(potential);
            }
        }
        else{
            // interface charge included

            // obtain ionic and complex concentrations for a given Donnan potential
            // by miminimising the sum of the squares of the equations describing each equilibrium
            calcConcnsMultiK(potential);
        }
    }

    //  Method to calculate ionic concentrations when only one ion or no ions bind to the ionophore
    public void calcConcnsSingleK(double potential){

        for(int ii=0; ii<this.numOfIons; ii++){
            if(this.assocConsts[ii]==0.0D  || this.ionophoreConcn==0.0D){
                if(molesT[ii]==0.0D){
                    // ion ii not present
                    this.concnB[ii] = 0.0D;
                    this.concnA[ii] = 0.0D;
                    this.complex[ii] = 0.0D;
                }
                else{
                    // No ionophore present or ion ii has no affinity for the ionophore
                        this.concnB[ii] = this.molesT[ii]/(this.volumeA*this.partCoeffPot[ii] + this.volumeB);
                        this.concnA[ii] = this.concnB[ii]*this.partCoeffPot[ii];
                        this.complex[ii] = 0.0D;
                }
            }
            else{
                // ion ii is the single ionic species with an affinity for the ionophore which is present
                // solve quadratic equilibrium equation
                // calculate quadratic terms
                double aa = this.assocConsts[ii]*(this.volumeB + this.volumeA*this.partCoeffPot[ii]);
                double bb = this.volumeB + this.volumeA*this.partCoeffPot[ii] + this.volumeB*this.assocConsts[ii]*ionophoreConcn - this.assocConsts[ii]*this.molesT[ii];
                double cc = -this.molesT[ii];
                // solve quadratic equatiom
                double root = bb*bb - 4.0D*aa*cc;
                if(root<0.0D){
                    System.out.println("Class: DonnanMinim\nMethod: ionConcns\nthe square root term (b2-4ac) of the quadratic = "+root);
                    System.out.println("this term was set to zero as the negative value MAY have arisen from rounding errors");
                    root = 0.0D;
                }
                double qq = -0.5*(bb + Fmath.sign(bb)*Math.sqrt(root));
                double root1 = qq/aa;
                double root2 = cc/qq;
                double limit = this.molesT[ii]/(this.volumeA*this.partCoeffPot[ii] + this.volumeB);
                if(root1>=0.0D && root1<=limit){
                    if(root2<0.0D || root2>limit){
                        this.concnB[ii] = root1;
                        this.concnA[ii] = this.concnB[ii]*this.partCoeffPot[ii];
                        this.complex[ii] = this.assocConsts[ii]*this.ionophoreConcn*this.concnB[ii]/(1.0D + this.assocConsts[ii]*this.concnB[ii]);
                        //this.complex[ii] = (this.molesT[ii] - this.concnA[ii]*this.volumeA - this.concnB[ii]*this.volumeB)/this.volumeB;
                    }
                    else{
                        System.out.println("Class: DonnanMinim\nMethod: ionConcns");
                        System.out.println("error1: no physically meaningfull root");
                        System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                        System.exit(0);
                    }
                }
                else{
                    if(root2>=0.0D && root2<=limit){
                        if(root1<0.0D || root1>limit){
                            this.concnB[ii] = root2;
                            this.concnA[ii] = this.concnB[ii]*this.partCoeffPot[ii];
                            //this.complex[ii] = (this.molesT[ii] - this.concnA[ii]*this.volumeA - this.concnB[ii]*this.volumeB)/this.volumeB;
                            this.complex[ii] = this.assocConsts[ii]*this.ionophoreConcn*this.concnB[ii]/(1.0D + this.assocConsts[ii]*this.concnB[ii]);
                        }
                        else{
                            System.out.println("Class: DonnanMinim\nMethod: ionConcns");
                            System.out.println("error2: no physically meaningfull root");
                            System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                            System.exit(0);
                        }
                    }
                    else{
                        System.out.println("Class: DonnanMinim\nMethod: ionConcns");
                        System.out.println("error3: no physically meaningfull root");
                        System.out.println("root1 = " + root1 + " root2 = " + root2 + " limit = " + limit);
                        System.exit(0);
                    }
                }
            }
        }
    }

    // Method to obtain ionic and complex concentrations for a given Donnan potential
    // when more than one ion competes for the ionophore
    // by miminimising the sum of the squares of the equations describing each equilibrium
    public void calcConcnsMultiK(double potential){


        // calculate initial estimates of ionic concentration
        double[] start = new double[this.numOfIons];
        double[] step = new double[this.numOfIons];


        for(int ii=0; ii<this.numOfIons; ii++){
            if(this.molesT[ii]==0.0D){
                // ion ii not present
                this.concnB[ii] = 0.0D;
                this.concnA[ii] = 0.0D;
                this.complex[ii] = 0.0D;
                this.excessConcnA[ii] = 0.0D;
                this.excessConcnB[ii] = 0.0D;
                this.excessComplex[ii] = 0.0D;
            }
            else{
                // ion ii present
                this.concnB[ii] = this.molesT[ii]/(this.volumeA*this.partCoeffPot[ii] + this.volumeB);
                this.concnA[ii] = this.concnB[ii]*this.partCoeffPot[ii];
                this.complex[ii] = 0.0D;
                this.excessConcnA[ii] = 0.0D;
                this.excessConcnB[ii] = 0.0D;
                this.excessComplex[ii] = 0.0D;
            }
            start[ii] = concnB[ii];
            step[ii] = 0.05*start[ii];
        }

        // obtain ionic and complex concentrations for a given Donnan potential
        // by miminimising the sum of the squares of the equations describing each equilibrium

        //Create instance of Minimisation
        Minimisation minConcn = new Minimisation();

        // Create instace of class holding function to be minimised
        DonnanConcn functC = new DonnanConcn();

        // Initialise function functC variable
        functC.numOfIons        = this.numOfIons;
        functC.concnA           = this.concnA;
        functC.concnB           = this.concnB;
        functC.molesT           = this.molesT;
        functC.complex          = this.complex;
        functC.excessConcnA     = this.excessConcnA;
        functC.excessConcnB     = this.excessConcnB;
        functC.excessComplex    = this.excessComplex;
        functC.assocConsts      = this.assocConsts;
        functC.indexK           = this.indexK;
        functC.nonZeroAssocK    = this.nonZeroAssocK;
        functC.radii            = this.radii;
        functC.charges          = this.charges;
        functC.ionophoreConcn   = this.ionophoreConcn;
        functC.ionophoreRad     = this.ionophoreRad;
        functC.volumeA          = this.volumeA;
        functC.volumeB          = this.volumeB;
        functC.interfacialArea  = this.interfacialArea;
        functC.epsilonA         = this.epsilonA;
        functC.epsilonB         = this.epsilonB;
        functC.epsilonSternA    = this.epsilonSternA;
        functC.epsilonSternB    = this.epsilonSternB;
        functC.temp             = this.temp;
        functC.partCoeffPot     = this.partCoeffPot;
        functC.sternCap         = this.sternCap;
        functC.sternDeltaA      = this.sternDeltaA;
        functC.sternDeltaB      = this.sternDeltaB;
        functC.chargeValue      = this.chargeValue;
        functC.chargeSame       = this.chargeSame;
        functC.interfacialCharge = this.interfacialCharge;
        functC.interfacialChargeDensity = this.interfacialChargeDensity;
        functC.potential        = potential;
        functC.includeIc        = this.includeIc;

        // Nelder and Mead minimisation procedure
        minConcn.nelderMead(functC, start, step, 1e-20, 10000);

        // get values of the Partition B concentrations at minimum
        double[] param = minConcn.getParamValues();

        this.freeIonophoreConcn = this.ionophoreConcn;
        for(int i=0; i<this.numOfIons; i++){
            this.concnB[i] = param[i];
            this.concnA[i] = this.concnB[i]*this.partCoeffPot[i];
            this.freeIonophoreConcn -= this.complex[i];
        }

        this.interfacialCharge = functC.interfacialCharge;
        this.interfacialChargeDensity = functC.interfacialChargeDensity;
        this.sternCap = functC.sternCap;
        this.sternDeltaA = functC.sternDeltaA;
        this.sternDeltaB = functC.sternDeltaB;
        this.sternPotential = functC.sternPotential;
        this.diffPotentialA = functC.diffPotentialA;
        this.diffPotentialB = functC.diffPotentialB;
    }


    // Print calculated potential and concentrations to a text file
    // File title provided by user
    public void printToFile(String title){

        FileOutput fout = new  FileOutput(title);
        fout.dateAndTimeln(title);
        fout.println();
        fout.print("Donnan potential = ");
        fout.printsp(Fmath.truncate(this.donnanPotential,7));
        fout.println("volts");
        if(this.includeIc){
            fout.print("Compartment A double layer potential difference = ");
            fout.printsp(Fmath.truncate(this.diffPotentialA,7));
            fout.println("volts");
            fout.print("Compartment B double layer potential difference = ");
            fout.printsp(Fmath.truncate(this.diffPotentialB,7));
            fout.println("volts");
            fout.print("Stern potential difference = ");
            fout.printsp(Fmath.truncate(this.sternPotential,7));
            fout.println("volts");
        }
        fout.println();

        fout.println("Ionic concentrations expressed as mol per cubic decimetre (M)");
        fout.println("Total = equivalent concentration with all ions in compartment A");
        if(this.includeIc){
            fout.printtab("Ion");
            fout.println("Bulk concentrations / M                         Excess concentrations / M                       total / M");
            fout.printtab(" ");
            fout.println("A               B               complex         A               B               complex         ");

            for(int i=0; i<this.numOfIons; i++){
                fout.printtab(this.ionNames[i]);
                fout.printtab(Fmath.truncate(this.concnA[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.concnB[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.complex[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.excessConcnA[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.excessConcnB[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.excessComplex[i]*1e-3,7));
                fout.println(Fmath.truncate(this.molesT[i]*1e-3/this.volumeA,7));
            }
        }
        else{
            fout.printtab("Ion");
            fout.println("A               B               complex         total");
            for(int i=0; i<this.numOfIons; i++){
                fout.printtab(this.ionNames[i]);
                fout.printtab(Fmath.truncate(this.concnA[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.concnB[i]*1e-3,7));
                fout.printtab(Fmath.truncate(this.complex[i]*1e-3,7));
                fout.println(Fmath.truncate(this.molesT[i]*1e-3/this.volumeA,7));
            }
        }


        fout.println();
        fout.println("mols of each ionic species");
        if(this.includeIc){
            fout.printtab("Ion");
            fout.println("Bulk mols                                       Excess mols                                 total mols");
            fout.printtab(" ");
            fout.println("A               B               complex         A               B               complex         ");
            for(int i=0; i<this.numOfIons; i++){
                fout.printtab(this.ionNames[i]);
                fout.printtab(Fmath.truncate(this.concnA[i]*this.volumeA,7));
                fout.printtab(Fmath.truncate(this.concnB[i]*this.volumeB,7));
                fout.printtab(Fmath.truncate(this.complex[i]*this.volumeB,7));
                fout.printtab(Fmath.truncate(this.excessConcnA[i]*this.volumeA,7));
                fout.printtab(Fmath.truncate(this.excessConcnB[i]*this.volumeB,7));
                fout.printtab(Fmath.truncate(this.excessComplex[i]*this.volumeB,7));
                fout.println(Fmath.truncate(this.molesT[i],7));
            }
        }
        else{
            fout.printtab("Ion");
            fout.println("A               B               complex         total mols");
            for(int i=0; i<this.numOfIons; i++){
                fout.printtab(this.ionNames[i]);
                fout.printtab(Fmath.truncate(this.concnA[i]*this.volumeA,7));
                fout.printtab(Fmath.truncate(this.concnB[i]*this.volumeB,7));
                fout.printtab(Fmath.truncate(this.complex[i]*this.volumeB,7));
                fout.println(Fmath.truncate(this.molesT[i],7));
            }
        }
        fout.println();

        if(this.includeIc){
            fout.println("Ratios of excess concentration over bulk concentration");
            fout.printtab("Ion");
            fout.println("A               B               complex");
            for(int i=0; i<this.numOfIons; i++){
                fout.printtab(this.ionNames[i]);
                fout.printtab(Fmath.truncate(this.ratioA[i],7));
                fout.printtab(Fmath.truncate(this.ratioB[i],7));
                fout.println(Fmath.truncate(this.ratioC[i],7));
            }
            fout.println();
        }

        fout.print("Total ionophore concentration = ");
        fout.printsp(Fmath.truncate(this.ionophoreConcn*1e-3,7));
        fout.println("M");
        fout.print("Free ionophore concentration = ");
        fout.printsp(Fmath.truncate(this.freeIonophoreConcn*1e-3,7));
        fout.println("M");
        fout.print("Total ionophore moles = ");
        fout.printsp(Fmath.truncate(this.ionophoreConcn*this.volumeB,7));
        fout.println("mol");
        fout.print("Ionophore radius = ");
        fout.printsp(Fmath.truncate(this.ionophoreRad,7));
        fout.println("m");
        fout.println();
        if(this.includeIc){
            fout.print("Interface charge density = ");
            fout.printsp(Fmath.truncate(this.interfacialChargeDensity,7));
            fout.printsp("C per square metre   ->   ");
            fout.printsp(Fmath.truncate(this.interfacialChargeDensity/Math.abs(Fmath.Q_ELECTRON),7));
            fout.println("unit charges per square metre ");
            fout.print("Total interface charge = ");
            fout.printsp(Fmath.truncate(this.interfacialCharge,7));
            fout.printsp("unit charges   ->   ");
            fout.printsp(Fmath.truncate(this.interfacialCharge/Math.abs(Fmath.Q_ELECTRON),7));
            fout.println("unit charges ");

            fout.print("Overall interfacial capacitance = ");
            fout.printsp(Fmath.truncate(this.donnanCap*this.interfacialArea,7));
            fout.printsp("F ");
            fout.print("  ->  ");
            fout.printsp(Fmath.truncate(this.donnanCap,7));
            fout.println("Farads per square metre");
            fout.print("Diffuse double layer capacitance (Compartment A) = ");
            fout.printsp(Fmath.truncate(this.diffCapA*this.interfacialArea,7));
            fout.printsp("F ");
            fout.print("  ->  ");
            fout.printsp(Fmath.truncate(this.diffCapA,7));
            fout.println("Farads per square metre");
            fout.print("Diffuse double layer capacitance (Compartment B) = ");
            fout.printsp(Fmath.truncate(this.diffCapB*this.interfacialArea,7));
            fout.printsp("F ");
            fout.print("  ->  ");
            fout.printsp(Fmath.truncate(this.diffCapB,7));
            fout.println("Farads per square metre");
            fout.print("Stern capacitance = ");
            fout.printsp(Fmath.truncate(this.sternCap*this.interfacialArea,7));
            fout.printsp("F ");
            fout.print("  ->  ");
            fout.printsp(Fmath.truncate(this.sternCap,7));
            fout.println("Farads per square metre");
            fout.print("Stern thickness (Compartment A) = ");
            fout.printsp(Fmath.truncate(this.sternDeltaA,7));
            fout.println("m");
            fout.print("Stern thickness (Compartment B) = ");
            fout.printsp(Fmath.truncate(this.sternDeltaB,7));
            fout.println("m");
            fout.print("Debye length (Compartment A) = ");
            fout.printsp(Fmath.truncate(this.recipKappaA,7));
            fout.println("m");
            fout.print("Debye length (Compartment B) = ");
            fout.printsp(Fmath.truncate(this.recipKappaB,7));
            fout.println("m");
            fout.println("Compartment thicknesses assuming cubes with one side equal to the interfacial area");
            fout.print("Compartment A thickness = ");
            fout.printsp(Fmath.truncate(this.volumeA/this.interfacialArea,7));
            fout.println("m");
            fout.print("Compartment B thickness = ");
            fout.printsp(Fmath.truncate(this.volumeB/this.interfacialArea,7));
            fout.println("m");
            fout.println();
        }
        fout.print("Volume of compartment A = ");
        fout.printsp(this.volumeA);
        fout.println("cubic metres");
        fout.print("Volume of compartment B = ");
        fout.printsp(this.volumeB);
        fout.println("cubic metres");
        fout.print("Interfacial area = ");
        fout.printsp(this.interfacialArea);
        fout.println("square metres");
        fout.print("Relative electrical permittivity of compartment A = ");
        fout.println(this.epsilonA);
        fout.print("Relative electrical permittivity of compartment B = ");
        fout.println(this.epsilonB);
        fout.print("Relative electrical permittivity of compartment A Stern layer= ");
        fout.println(this.epsilonSternA);
        fout.print("Relative electrical permittivity of compartment B Stern layer= ");
        fout.println(this.epsilonSternB);
        fout.print("Temperature= ");
        fout.printsp(this.temp+Fmath.T_ABS);
        fout.println("degrees Celsius");
        fout.println();

        fout.printtab("Ion");
        fout.printtab("Radius   ");
        fout.printtab("Charge");
        fout.printtab("Partition");
        fout.printtab("Partition");
        fout.printtab("Delta(mu0)");
        fout.println("Ion-Ionophore ");

        fout.printtab("  ");
        fout.printtab(" m       ");
        fout.printtab(" ");
        fout.printtab("Coefficient ");
        fout.printtab("Coefficient ");
        fout.printtab("/ J per mol");
        fout.println("associaion ");

        fout.printtab("  ");
        fout.printtab("         ");
        fout.printtab(" ");
        fout.printtab("at      ");
        fout.printtab("at zero ");
        fout.printtab("       ");
        fout.println("constant");

        fout.printtab("  ");
        fout.printtab("          ");
        fout.printtab(" ");
        fout.printtab("equilibrium ");
        fout.printtab("potential ");
        fout.printtab("          ");
        fout.println("mol per cubic dm");

        for(int i=0; i<this.numOfIons; i++){
            fout.printtab(this.ionNames[i]);
            fout.printtab(Fmath.truncate(this.radii[i],4));
            fout.printtab(this.charges[i]);
            fout.printtab(Fmath.truncate(this.partCoeffPot[i],4));
            fout.printtab(Fmath.truncate(this.partCoeff[i],4));
            fout.printtab(Fmath.truncate(this.deltaMu0[i],4));
            fout.println(Fmath.truncate(this.assocConsts[i]*1e3,4));

        }

        fout.close();
    }

    // Print calculated potential and concentrations to a text file
    // Default file title
    public void printToFile(){
        String title = "DonnanOutputFile.txt";
        printToFile(title);
    }
}





