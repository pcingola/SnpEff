/*
*   Classes DonnanConcn
*
*   Class DonnanConcn impliments the interface
*   MinimisationFunction to the Class Minimisation
*   and contains a function that calculates the ionic
*   concentrations if the ionophore binds more than
*   one species of ion. The sum of the squares of the
*   equilibrium functions is minimised.
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

class DonnanConcn implements MinimisationFunction{

    public int numOfIons = 0;               //  number of ionic species
    public double[] concnA = null;          //  concentration of ions in partition A (moles per cubic metre)
    public double[] concnB = null;          //  concentration of ions in partition B (moles per cubic metre)
    public double[] molesT = null;          //  total moles of an ion
    public double[] complex = null;         //  concentration of complex in partition B (moles per cubic metre)
    public double[] excessConcnA = null;    //  excess concentration of ions in partition A in interfacial charge (moles per cubic metre)
    public double[] excessConcnB = null;    //  excess concentration of ions in partition B in interfacial charge (moles per cubic metre)
    public double[] excessComplex = null;   //  excess concentration of complex in partition B in interfacial charge (moles per cubic metre)
    public double[] assocConsts = null;     //  association constants of the ions with the ionophore (cubic metres per mole)
    public double[] partCoeffPot = null;    //  partition coefficients in presence of inter-partition, e.g. Donnan, potential
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
    public double diffPotentialA = 0.0D;    // Double layer potential difference - compartment A [volts]
    public double diffPotentialB = 0.0D;    // Double layer potential difference - compartment B [volts]
    public double sternPotential = 0.0D;    // Stern potential difference  [volts]
    public double sternCap = 0.0D;          //  Stern layer capacitance [F]
    public double sternDeltaA = 0.0D;       //  Stern layer thickness in partition A [m]
    public double sternDeltaB = 0.0D;       //  Stern layer thickness in partition B [m]
    public double chargeValue = 0;          //  Absolute value of the charge valency if all are the same, i.e. symmetric electrolytes of the same valency
    public boolean chargeSame = true;       //  = false if chargeValue not the same for all ions
    public double interfacialChargeDensity = 0.0D;// interface Charge Density [C per square metre]
    public double interfacialCharge = 0.0D;       // interface Charge [C]
    public boolean includeIc = true;        // = true - interface charge included in the calculation of the Donnan potential
                                            // = false - interface charge ignored in the calculation of the Donnan Poptential
    public double potential = 0;            //  current estimate of Donnan potential
    private double penalty = 1.0E+50;       // penalty function multiplier invoked if an estimated ion concentration is negative

    // method that returns sum of squares of equilibrium equations
    // x[0] transfers the current estimate of the concentrations in partition B.
    public double function(double[] x){

        double sumOfSquares = 0.0D;

        // Calculate estimate of complex concentration
        if(this.nonZeroAssocK>0 && this.ionophoreConcn>0.0D){
            if(this.nonZeroAssocK==1){
                complex[indexK[0]] = this.assocConsts[indexK[0]]*x[indexK[0]]*this.ionophoreConcn/(1.0D + this.assocConsts[indexK[0]]*x[indexK[0]]);
            }
            else{
                double[] vec = new double[this.nonZeroAssocK];
                double[][] mat = new double[this.nonZeroAssocK][this.nonZeroAssocK];

                // set up simultaneous equations
                for(int i=0; i<this.nonZeroAssocK; i++){
                    vec[i] = this.assocConsts[indexK[i]]*x[indexK[i]]*this.ionophoreConcn;
                    for(int j=0; j<this.nonZeroAssocK; j++){
                        mat[i][j] = this.assocConsts[indexK[i]]*x[indexK[i]];
                        if(i==j)mat[i][j] += 1.0D;
                    }
                }

                // solve simultaneous equations to obtain complex concentrations
                Matrix matrix = new Matrix(mat);
                vec = matrix.solveLinearSet(vec);
                for(int i=0; i<this.nonZeroAssocK; i++){
                    this.complex[indexK[i]] = vec[i];
                }
            }
        }

        // Test to check whether interface charge is included in the calculation of the Donnan potential
        if(this.includeIc){
            // Calculate the excess charge in the interface region
            double excess = Math.abs(this.interfaceCharge(x, this.potential));

            // Calculate concentrations of ions involved in excess interfacial charge region
            excessConcentrations(x, excess, this.potential);

            // Calculate sum of squares of extended equilibrium functions
            for(int i=0; i<this.numOfIons; i++){
                double aa = x[i]*(this.volumeB + this.partCoeffPot[i]*this.volumeA) +  this.excessConcnA[i]*this.volumeA + (this.excessConcnB[i] + this.complex[i] +this.excessComplex[i])*this.volumeB - this.molesT[i];
                sumOfSquares += aa*aa;
                if(x[i]<0.0D)sumOfSquares += x[i]*x[i]*this.penalty;
            }
        }
        else{
            // Calculate sum of squares of equilibrium functions
            for(int i=0; i<this.numOfIons; i++){
                double aa = x[i]*(this.volumeB + this.partCoeffPot[i]*this.volumeA) + this.complex[i]*this.volumeB - this.molesT[i];
                sumOfSquares += aa*aa;
                if(x[i]<0.0D)sumOfSquares += x[i]*x[i]*this.penalty;
            }
        }
        return sumOfSquares;
    }


    // Calculates the  concentrations of ions involved in excess interfacial charge region
    public void excessConcentrations(double[] x, double excess, double potential){

        if(potential==0.0D){
            for(int i=0; i<this.numOfIons; i++){
                this.excessConcnA[i] = 0.0D;
                this.excessConcnB[i] = 0.0D;
                this.excessComplex[i] = 0.0D;
            }
        }
        else{
            double sumA = 0.0D;
            double sumB = 0.0D;
            double sumC = 0.0D;
            for(int i=0; i<this.numOfIons; i++){
                if(potential>0.0D){
                    if(this.charges[i]>0.0D){
                        sumB += x[i]*Math.abs(charges[i]);
                        sumC += this.complex[i]*Math.abs(charges[i]);

                    }
                    else{
                        sumA += x[i]*partCoeffPot[i]*Math.abs(charges[i]);
                    }
                }
                else{
                    if(this.charges[i]<0.0D){
                        sumB += x[i]*Math.abs(charges[i]);
                        sumC += this.complex[i]*Math.abs(charges[i]);
                    }
                    else{
                        sumA += x[i]*partCoeffPot[i]*Math.abs(charges[i]);
                   }
                }
            }
            double factorA = excess/(sumA*this.volumeA);
            double factorB = excess/((sumB+sumC)*this.volumeB);
            for(int i=0; i<this.numOfIons; i++){
                if(potential>0.0D){
                    if(this.charges[i]>0.0D){
                        this.excessConcnB[i] = Math.abs(this.concnB[i]*factorB);
                        this.excessComplex[i] = Math.abs(this.complex[i]*factorB);

                    }
                    else{
                        this.excessConcnA[i] = Math.abs(this.concnA[i]*factorA);
                    }
                }
                else{
                    if(this.charges[i]<0.0D){
                        this.excessConcnB[i] = Math.abs(this.concnB[i]*factorB);
                        this.excessComplex[i] = Math.abs(this.complex[i]*factorB);
                    }
                    else{
                        this.excessConcnA[i] = Math.abs(this.concnA[i]*factorA);
                   }
                }
            }
        }
    }


    // calculates the excess charge as (surface charge density) in the interfacial region on the Patrition B side
    public double interfaceCharge(double[] ions, double potential){

        if(potential==0){
            this.interfacialCharge=0.0D;
            this.interfacialChargeDensity=0.0D;
            this.diffPotentialA = 0.0D;
            this.diffPotentialB = 0.0D;
            this.sternPotential = 0.0D;
        }
        else{
            // bisection method
            double sigmaM = 0.0D;
            double funcM = 0.0D;
            double sigmaL = 0.0D, funcL = 0.0D;
            double sumAions = 0.0D;
            double sumBions = 0.0D;
            double aveCharge = 0.0D;
            for(int i=0; i<this.numOfIons; i++){
                sumBions += Math.abs(ions[i]*this.charges[i]);
                sumAions += Math.abs(ions[i]*this.charges[i]*this.partCoeffPot[i]);
                aveCharge += Math.abs(charges[i]);
            }
            aveCharge /= this.numOfIons;
            sumBions /= (2.0D*aveCharge);
            sumAions /= (2.0D*aveCharge);
            double maxQ = 1.2D*Math.sqrt(8.0D*Fmath.N_AVAGADRO*sumBions*Fmath.K_BOLTZMANN*this.temp*Fmath.EPSILON_0*this.epsilonB)*Fmath.sinh(-aveCharge*Fmath.Q_ELECTRON*Math.abs(potential)/(2.0D*Fmath.K_BOLTZMANN*this.temp));
            double sigmaH = maxQ, funcH = 0.0D;
            double tolQ = Math.abs(potential)*1e-8;
            int nIterQ = 10000;
            boolean testQ = true;
            int iExpandQ = 0, iBisectQ = 0;
            double diffQ = 0.0D;

            while(testQ){
                funcL = icFunct(sigmaL, potential, ions);
                funcH = icFunct(sigmaH, potential, ions);
                if(funcH*funcL>0.0D){
                    iExpandQ++;
                    if(iExpandQ>10)throw new IllegalArgumentException("iExpandQ has reached its limit");
                    diffQ = sigmaH - sigmaL;
                    sigmaH += diffQ;
                }
                else{
                    testQ=false;
                }
            }
            if(Math.abs(funcL)<=tolQ){
                sigmaM = sigmaL;
            }
            else{
                if(Math.abs(funcH)<=tolQ){
                    sigmaM = sigmaH;
                }
                else{
                    testQ=true;
                    while(testQ){
                        sigmaM = (sigmaL + sigmaH)/2.0D;
                        funcM = icFunct(sigmaM, potential, ions);
                        if(Math.abs(funcM)<=tolQ){
                            testQ = false;
                        }
                        else{
                            if(funcL*funcM>0.0D){
                                funcL = funcM;
                                sigmaL = sigmaM;
                            }
                            else{
                                funcH = funcM;
                                sigmaH = sigmaM;
                            }
                        }
                        iBisectQ++;
                        if(iBisectQ>nIterQ){
                            System.out.println("Class: DonnanConcn\nMethod: interfaceCharge");
                            System.out.println("Maximum iterations in bisection procedure exceeded\nCurrent value of interface charge returned");
                            testQ = false;
                        }
                    }
                }
            }
            this.interfacialCharge = sigmaM;
            this.interfacialChargeDensity = sigmaM/this.interfacialArea;
        }

        // return equivalent moles of total excess charge
        return this.interfacialCharge/(-Fmath.Q_ELECTRON*Fmath.N_AVAGADRO);
    }

    // function for interfaceCharge bisection procedure
    // returns the present estimate of the Donnan potential minus the sum of the potential
    // differences calculated for the Stern layer and both ionic double layers
    public double icFunct(double sigma, double potential, double[] ions){
        double sigmaAbs = Math.abs(sigma);
        double sgn = Fmath.sign(potential);

        if(this.chargeSame){
            double NtotalA = 0.0D;
            double NtotalB = 0.0D;
            for(int i=0; i<this.numOfIons; i++){
                NtotalA += this.concnA[i];
                NtotalB += this.concnB[i] + this.complex[i];
            }
            NtotalA /= 2;
            NtotalB /= 2;
            double preterm = (2.0D*Fmath.K_BOLTZMANN*this.temp)/(-this.chargeValue*Fmath.Q_ELECTRON);
            this.diffPotentialA = sgn*preterm*Fmath.asinh(sigmaAbs/Math.sqrt(8.0D*Fmath.N_AVAGADRO*NtotalA*Fmath.K_BOLTZMANN*this.temp*Fmath.EPSILON_0*this.epsilonA));
            this.diffPotentialB = sgn*preterm*Fmath.asinh(sigmaAbs/Math.sqrt(8.0D*Fmath.N_AVAGADRO*NtotalB*Fmath.K_BOLTZMANN*this.temp*Fmath.EPSILON_0*this.epsilonB));
        }
        else{
            // bisection method for double layer potentials
            // partition A
            double phiAm = 0.0D;
            double funcM = 0.0D;
            double phiAl = 0.0D;
            double funcL = 0.0D;
            double maxPhiA = 1.1*potential;
            double phiAh = maxPhiA;
            double funcH = 0.0D;
            double tolP = Math.abs(sigma)*1e-1;
            int nIterP = 1000;
            boolean testP = true;
            int iExpandP = 0, iBisectP = 0;
            double diffP = 0.0D;

            while(testP){
                funcL = phiAfunct(sigma, phiAl, ions);
                funcH = phiAfunct(sigma, phiAh, ions);
                if(funcH*funcL>0.0D){
                    iExpandP++;
                    if(iExpandP>10)throw new IllegalArgumentException("iExpandP (partition A) has reached its limit");
                    diffP = phiAh - phiAl;
                    phiAh += diffP;
                }
                else{
                    testP=false;
                }
            }

            testP=true;
            while(testP){
                phiAm = (phiAl + phiAh)/2.0D;
                funcM = phiAfunct(sigma, phiAm, ions);
                if(Math.abs(funcM)<=tolP){
                    this.diffPotentialA = sgn*phiAm;
                    testP = false;
                }
                else{
                    if(funcL*funcM>0.0D){
                        funcL = funcM;
                        phiAl = phiAm;
                    }
                    else{
                        funcH = funcM;
                        phiAh = phiAm;
                    }
                }
                iBisectP++;
                if(iBisectP>nIterP){
                    //System.out.println("Class: DonnanConcn\nMethod: icFunct");
                    //System.out.println("Maximum iterations in bisection A procedure exceeded\nCurrent value of interface charge returned");
                    System.out.println("phiA = " + phiAm + " sigma = " + sigma + " funcM = " + funcM + " tol = " + tolP);
                    this.diffPotentialA = sgn*phiAm;
                    testP = false;
                }
           }

            // partition B
            double phiBm = 0.0D;
            double phiBl = 0.0D;
            double maxPhiB = -1.1*potential;
            double phiBh = maxPhiB;
            tolP = Math.abs(potential)*1e-5;
            if(tolP==0.0D)tolP=1e-6;
            nIterP = 100000;
            testP = true;
            iExpandP = 0;
            iBisectP = 0;
            diffP = 0.0D;

            while(testP){
                funcL = phiAfunct(sigma, phiBl, ions);
                funcH = phiAfunct(sigma, phiBh, ions);
                if(funcH*funcL>0.0D){
                    iExpandP++;
                    if(iExpandP>10)throw new IllegalArgumentException("iExpandP (partition B) has reached its limit");
                    diffP = phiBh - phiBl;
                    phiBh += diffP;
                }
                else{
                    testP=false;
                }
            }

            if(Math.abs(funcH)<=tolP){
                phiBm = phiBh;
            }
            else{
                testP=true;
                while(testP){
                    phiBm = (phiBl + phiBh)/2.0D;
                    funcM = phiAfunct(sigma, phiBm, ions);
                    if(Math.abs(funcM)<=tolP){
                        testP = false;
                    }
                    else{
                        if(funcL*funcM>0.0D){
                            funcL = funcM;
                            phiBl = phiBm;
                        }
                        else{
                            funcH = funcM;
                            phiBh = phiBm;
                        }
                    }
                    iBisectP++;
                    if(iBisectP>nIterP){
                        System.out.println("Class: DonnanConcn\nMethod: icFunct");
                        System.out.println("Maximum iterations in bisection B procedure exceeded\nCurrent value of interface charge returned");
                        System.out.println("phiB = " + phiBm + " maxPhiB = " + maxPhiB + " funcM = " + funcM + " tol = " + tolP);
                        testP = false;
                    }
                }
            }
            this.diffPotentialB = sgn*phiBm;
        }

        // Calculate Stern capacitance
        sternCapacitance(ions, sigma, this.diffPotentialA, -this.diffPotentialB);
        this.sternPotential = sgn*sigmaAbs/this.sternCap;

        return potential - (this.diffPotentialA + this.diffPotentialB + this.sternPotential);
    }

    // Function returns estimated interface charge - interface charge calculated for passed potential
    // partition A
    public double phiAfunct(double sigma, double potential, double[] ions){
        double sumAsigma = 0.0D;
        double sgns = Fmath.sign(sigma);
        double preterm1 = 2.0D*Fmath.EPSILON_0*this.epsilonA*Fmath.K_BOLTZMANN*this.temp*Fmath.N_AVAGADRO;
        double preterm2 = potential*Fmath.Q_ELECTRON/(Fmath.K_BOLTZMANN*this.temp);

        for(int i=0; i<this.numOfIons; i++){

            sumAsigma += (preterm1*ions[i]*this.partCoeffPot[i])*(Math.exp(charges[i]*preterm2) - 1.0D);
       }
       if(sumAsigma<0.0){
            sgns = - sgns;
            sumAsigma = -sumAsigma;
        }
        double diffSigma = sigma - sgns*Math.sqrt(sumAsigma);
        return diffSigma;
    }

    // Function returns estimated interface charge - interface charge calculated for passed potential
    // partition B
    public double phiBfunct(double sigma, double potential, double[] ions){
        double sumBsigma = 0.0D;
        double sgns = Fmath.sign(sigma);
        double preterm1 = 2.0D*Fmath.EPSILON_0*this.epsilonB*Fmath.K_BOLTZMANN*this.temp*Fmath.N_AVAGADRO;
        double preterm2 = potential*Fmath.Q_ELECTRON/(Fmath.K_BOLTZMANN*this.temp);

        for(int i=0; i<this.numOfIons; i++){
            sumBsigma += (preterm1*(ions[i]+this.complex[i]))*(Math.exp(charges[i]*preterm2) - 1.0D);
        }
        if(sumBsigma<0.0){
            sgns = - sgns;
            sumBsigma = -sumBsigma;
        }
        double diffSigma = sigma - sgns*Math.sqrt(sumBsigma);
        return diffSigma;
    }

    // calculates the Stern capacitances and the Stern potential
    // given a surface charge and given diffuse layer potentials
    public void sternCapacitance(double[] ions, double sigma, double psiA, double psiB){

        double denomA = 0.0D;
        double denomB = 0.0D;
        this.sternDeltaA = 0.0D;
        this.sternDeltaB = 0.0D;
        double preterm = -Fmath.Q_ELECTRON/(Fmath.K_BOLTZMANN*this.temp);
        for(int i=0; i<this.numOfIons; i++){
            this.sternDeltaA  += radii[i]*ions[i]*this.partCoeffPot[i]*Math.exp(preterm*psiA*charges[i]);
            this.sternDeltaB += (this.radii[i]*ions[i] + this.ionophoreRad*this.complex[i])*Math.exp(-preterm*psiB*charges[i]);
            denomA += ions[i]*this.partCoeffPot[i]*Math.exp(preterm*psiA*charges[i]);
            denomB += (ions[i] + this.complex[i])*Math.exp(-preterm*psiB*charges[i]);
        }
        this.sternDeltaA /= denomA;
        this.sternDeltaB /= denomB;
        this.sternCap = Fmath.EPSILON_0*this.epsilonSternA*this.epsilonSternB/(this.sternDeltaA*this.epsilonSternB + this.sternDeltaB*this.epsilonSternA);
    }
}


