/*
*   Classes DonnanMinim    (for Class Donnan)
*
*   Class Donnan contains the primary methods for
*   calculating a Donnan Potential between two
*   partitions of different electrical permittivity
*   (dielectric constant) between which any number of
*   ionic species may be partitioned and, in one
*   partition only, may bind to a neutral ionophore.
*
*   Class DonnanMinim impliments the interface
*   MinimisationFunction to the Class Minimisation
*   and contains a function that calculates the net
*   charge in the partition containing the ionophore.
*   This net charge is minimised by the minimisation
*   function in Minimisation as it should equal to
*   zero at equilibrium.
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:       November 2004
*   UPDATE:     6 December 2004
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

import flanagan.math.*;

class DonnanMinim implements MinimisationFunction{

    public int numOfIons = 0;               //  number of ionic species
    public double[] concnA = null;          //  concentration of ions in partition A (moles per cubic metre)
    public double[] concnB = null;          //  concentration of ions in partition B (moles per cubic metre)
    public double[] molesT = null;          //  total moles of an ion
    public double[] complex = null;         //  concentration of complex in partition B (moles per cubic metre)
    public double[] excessConcnA = null;    //  excess concentration of ions in partition A in interfacial charge (moles per cubic metre)
    public double[] excessConcnB = null;    //  excess concentration of ions in partition B in interfacial charge (moles per cubic metre)
    public double[] excessComplex = null;   //  excess concentration of complex in partition B in interfacial charge (moles per cubic metre)
    public double[] assocConsts = null;     //  association constants of the ions with the ionophore (cubic metres per mole)
    public int[] indexK = null;             //  ion index of ionic species with non zero assocConsts
    public int nonZeroAssocK = 0;           //  number of ionic species with affinity for the ionophore
    public double[] radii = null;           //  radii of ions
    public double[] charges = null;         //  charge of ions
    public double ionophoreConcn = 0.0D;    // ionophore concentration - entered as molar, converted to moles per cubic metre
    public double ionophoreRad = 0.0D;      //  ionophore radius (metres)
    public double volumeA = 0.0D;           //  volume of partition A (cubic metres)
    public double volumeB = 0.0D;           //  volume of partition B (cubic metres)
    public double interfacialArea = 0.0D;   //  onterfacial area between Partitions A and B(square metres)
    public double epsilonA = 0.0D;          //  relative electrical permittivity of partition A
    public double epsilonB = 0.0D;          //  relative electrical permittivity of partition B
    public double epsilonSternA = 0.0D;     //  relative electrical permittivity of partition A Stern layer
    public double epsilonSternB = 0.0D;     //  relative electrical permittivity of partition B SternLayer
    public double temp = 25.0-Fmath.T_ABS;  // Temperature (degrees Kelvin) [Enter temperature in degrees Celsius]
    public double[] partCoeff = null;       //  partition coefficients in absence of inter-partition, e.g. Donnan, potential
    public double[] partCoeffPot = null;    //  partition coefficients in presence of inter-partition, e.g. Donnan, potential
    public double diffPotentialA = 0.0D;    // Double layer potential difference - compartment A [volts]
    public double diffPotentialB = 0.0D;    // Double layer potential difference - compartment B [volts]
    public double sternPotential = 0.0D;    // Stern potential difference [volts]
    public double sternCap = 0.0D;          //  Stern layer capacitance [F]
    public double sternDeltaA = 0.0D;       //  Stern layer thickness in partition A [m]
    public double sternDeltaB = 0.0D;       //  Stern layer thickness in partition B [m]
    public double chargeValue = 0;          //  Absolute value of the charge valency if all are the same, i.e. symmetric electrolytes of the same valency
    public boolean chargeSame = true;       //  = false if chargeValue not the same for all ions
    public double interfacialChargeDensity = 0.0D;// interface Charge Density [C per square metre]
    public double interfacialCharge = 0.0D;       // interface Charge [C]
    public boolean includeIc = true;        // = true - interface charge included in the calculation of the Donnan potential
                                            // = false - interface charge ignored in the calculation of the Donnan Poptential

    private double[] start = null;          // Minimisation initial estimates
    private double[] step = null;           // Minimisation step sizes
    private double[] param = null;          // Minimisation returned values of concnB at minimum

    // Constructor
    public DonnanMinim(int noIons){
        this.numOfIons = noIons;
        this.start  = new double[this.numOfIons];
        this.step   = new double[this.numOfIons];
        this.param  = new double[this.numOfIons];
    }


    // method that calculates the net charge in the partition B
    // x[0] transfers the current estimate of the Donnan Potential.
    public double function(double[] x){
        double chargeBsum = 0.0D;

        // calculate ion and complex concentrations for potential estimate x[0]
        this.ionConcns(x[0]);

        for(int i=0; i<this.numOfIons; i++){
            chargeBsum += (this.concnB[i] + this.complex[i])*this.charges[i];
        }
        return chargeBsum*chargeBsum;
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
        // calculate initial estimates of ionic concentrations
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
            this.start[ii] = concnB[ii];
            this.step[ii] = 0.05*start[ii];
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
        minConcn.nelderMead(functC, this.start, this.step, 1e-20, 10000);

        // get and calculate values of the Partition B concentrations at minimum
        param = minConcn.getParamValues();

        for(int i=0; i<this.numOfIons; i++){
            this.concnB[i] = param[i];
            this.concnA[i] = this.concnB[i]*this.partCoeffPot[i];
        }

        // get values of the interfacial charge
        this.interfacialCharge = functC.interfacialCharge;
        this.interfacialChargeDensity = functC.interfacialChargeDensity;

    }
 }
