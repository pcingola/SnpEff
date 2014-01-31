/*      Class SurfaceStripLine
*
*       Models a parallel plate transmision line
*       This is a subclass of the superclass TransmissionLine
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    August 2011
*       UPDATE:
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/SurfaceStripLine.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/StripLine.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/TransmissionLine.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*       Copyright (c) 2011    Michael Thomas Flanagan
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
import flanagan.math.Fmath;
import flanagan.circuits.TransmissionLine;

public class SurfaceStripLine extends TransmissionLine{

    private double trackWidth = -1.0D;                  // surface plate (track) width
    private double effectiveTrackWidth = -1.0D;         // effective track thickness
    private double trackThickness = -1.0D;              // metal track thickness
    private double plateSeparation = -1.0D;             // plate separation - inner surface to inner surface

    private boolean widthSet = false;                   // = true when track width entered
    private boolean separationSet = false;              // = true when plate separation entered
    private boolean thicknessSet = false;               // = true when track thickness entered

    private double relativePermittivity = 1.0D;         // relative electrical permittivity of the material between the conductors
    private double effectivePermittivity = 1.0D;        // effective electrical permittivity of the material between the conductors
    private boolean permittivitySet = false;            // = true when the permittivity has been entered entered

    private double relativePermeability = 1.0D;         // relative magnetic permeability of the material between the conductors

    private int formulaOption = 0;                      // Characteristic Impedance Formula option
                                                        //      = 0; Edwards/Hoffmann/York Collection
                                                        //      = 1; Wadell/Wheeler/Schneider
                                                        //      = 2; IPC-D-317A

    private boolean z0calculated = false;               // = true when the characteristic impedance has been calculated


    // CONSTRUCTOR
    // Default constructor
    public SurfaceStripLine(){
        super.title = "Surface Strip Line";
    }

    // Constructor with user suppled title
    public SurfaceStripLine(String title){
        super.title = title;
    }

    // CHARACTERISTIC IMPEDANCE FORMULA OPTIONS
    // Set option
    // opt = 0 (entered as 1): Edwards Hoffmann (Bob York (Santa Barbara) collection)
    // opt = 1 (entered as 2): Wadell (Wheeler) Formula with Schneider effective permittivity
    // opt = 2 (entered as 3): IPC-D-317A Formula
    public void setFormulae(int opt){
        if(opt<1 || opt>3)throw new IllegalArgumentException("The option value, " + opt + ", must be 1,2 or 3");
        this.formulaOption = opt-1;
        if(this.permittivitySet && this.separationSet && this.widthSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // Use Edwards Hoffmann (Bob York (Santa Barbara) collection) formulae
    public void useEdwardsHoffmannFormulae(){
        this.formulaOption = 0;
        if(this.permittivitySet && this.separationSet && this.widthSet){
            this.calcIdealCharacteristicImpedance();
        }

    }

    // Use Wadell (Wheeler) Formula with Schneider effective permittivity
    // Default option
    public void useWadellWheelerSchneiderFormulae(){
        this.formulaOption = 1;
        if(this.permittivitySet && this.separationSet && this.widthSet && this.thicknessSet){
            this.calcIdealCharacteristicImpedance();
        }

    }

    // Use IPC-D-317A Formula
    public void useIPCD317AFormulae(){
        this.formulaOption = 2;
        if(this.permittivitySet && this.separationSet && this.widthSet && this.thicknessSet){
            this.calcIdealCharacteristicImpedance();
        }
    }

    // SURFACE PLATE (TRACK) WIDTH
    // Set surface plate (track) width
    public void setSurfaceTrackWidth(double width){
        if(width<=0.0D)throw new IllegalArgumentException("The plate width, " + width + ", must be greater than zero");
        this.trackWidth = width;
        this.effectiveTrackWidth = width;
        this.widthSet = true;
        if(this.permittivitySet && this.separationSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // Return effective track width
    public double getEffectiveTrackWidth(){
        if(!this.z0calculated)this.calcIdealCharacteristicImpedance();
        if(formulaOption==2){
            this.effectiveTrackWidth = this.trackWidth;
            System.out.println("Method: getEffectiveTrackWidth()");
            System.out.println("The effective track width is not calculated explicitely for the IPC-D-317A formula.");
            System.out.println("The unadjusted track width has been returned.");
        }
        return this.effectiveTrackWidth;
    }

    // SURFACE PLATE (TRACK) THICKNESS
    // Set surface plate (track) thickness
    public void setSurfaceTrackThickness(double thickness){
        if(thickness<=0.0D)throw new IllegalArgumentException("The plate thickness, " + thickness + ", must be greater than zero");
        this.trackThickness = thickness;
        this.thicknessSet = true;
        if(this.permittivitySet && this.separationSet && this.widthSet){
            this.calcIdealCharacteristicImpedance();
        }
    }

    // PLATE SEPARATION
    // Set plate separation - inner surface to inner surface
    public void setPlateSeparation(double separation){
        if(separation<=0.0D)throw new IllegalArgumentException("The plate separation, " + separation + ", must be greater than zero");
        this.plateSeparation = separation;
        this.separationSet = true;
        if(this.permittivitySet && this.widthSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // PERMITTIVITY
    // Set relative electrical permittivity of the material between the conductors
    public void setRelativePermittivity(double epsilonR){
        this.relativePermittivity = epsilonR;
        this.permittivitySet = true;
        if(this.widthSet && this.separationSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // Return effective permittivity
    public double getEffectiveRelativePermittivity(){
        if(!this.permittivitySet)throw new IllegalArgumentException("The relative permittivity has not been entered");
        if(!this.z0calculated)this.calcIdealCharacteristicImpedance();
        if(formulaOption==2){
            this.effectivePermittivity = this.relativePermittivity;
            System.out.println("Method: getEffectiveRelativePermittivity()");
            System.out.println("The effective permittivity is not calculated explicitely for the IPC-D-317A formula.");
            System.out.println("The unadjusted relative permittivity has been returned.");
        }
        return this.effectivePermittivity;
    }

    // PERMEABILTY
    // Set relative magnetic permeability of the material between the conductors
    public void setRelativePermeability(double muR){
        this.relativePermeability = muR;
        if(this.permittivitySet && this.widthSet && this.separationSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // FREQUENCY
    // set frequency
    public void setFrequency(double frequency){
        super.frequency = frequency;
        super.omega = this.frequency * 2.0D * Math.PI;
        if(this.permittivitySet && this.widthSet && this.separationSet){
            if(this.formulaOption==0){
                this.calcIdealCharacteristicImpedance();
            }
            else{
                if(this.thicknessSet)this.calcIdealCharacteristicImpedance();
            }
        }
    }

    // IMPEDANCES
    // Calculate ideal characteristic impedance
    private Complex calcIdealCharacteristicImpedance(){
        if(!this.separationSet)throw new IllegalArgumentException("The strip line plate separation has not been entered");
        if(!this.widthSet)throw new IllegalArgumentException("The strip line track width has not been entered");
        if(!this.permittivitySet)throw new IllegalArgumentException("The relative permittivity has not been entered");

        double wOverH = this.trackWidth/this.plateSeparation;
        double hOverW = 1.0D/wOverH;
        double tOverH = this.trackThickness/this.plateSeparation;
        double eplus1 = this.relativePermittivity + 1.0D;
        double eminus1 = this.relativePermittivity - 1.0D;
        double fh = this.plateSeparation*super.frequency*1.0E-6;

        switch(formulaOption){
            case 2: // IPC-D-317A formula
                    if(!this.thicknessSet)throw new IllegalArgumentException("The strip line track thickness has not been entered");
                    if(wOverH<0.1D || wOverH>3.0D){
                        System.out.println("WARNING!");
                        System.out.println("IPC-D-317A FORMULA RESTRICTION");
                        System.out.println("For this model the width over separation ratio, " + wOverH + ", must lie between 0.1 and 3.0 inclusive");
                        System.out.println("The results are likely to be inaccurate");
                    }
                    if(tOverH>0.25D){
                        System.out.println("WARNING!");
                        System.out.println("IPC-D-317A FORMULA RESTRICTION");
                        System.out.println("For this model the thickness over separation ratio, " + tOverH + ", must be less than 0.25");
                        System.out.println("The results are likely to be inaccurate");
                    }
                    super.idealRealCharacteristicImpedance = 87.0D/Math.sqrt(this.relativePermittivity + 1.41D);
                    super.idealRealCharacteristicImpedance *= Math.log(5.98D*this.plateSeparation/(0.8D*this.trackWidth + this.trackThickness));
                    break;
            case 1: // Wadell (Wheelen) Schneider formula
                    if(!this.thicknessSet)throw new IllegalArgumentException("The strip line track thickness has not been entered");

                    // effective relative permittivity
                    double hold = 1.0D/Math.sqrt(1.0D + 12.0D*hOverW);
                    if(wOverH<1.0D)hold += 0.04D*Fmath.square(1.0D - wOverH);
                    this.effectivePermittivity = (eplus1 + eminus1*hold)/2.0D;

                    // Effective track width
                    double denom = tOverH*tOverH;
                    denom += Fmath.square((1.0D/Math.PI)/(this.trackWidth/this.trackThickness + 1.1D));
                    double deltaW = (this.trackThickness/Math.PI)*Math.log(4.0D*Math.E/Math.sqrt(denom));
                    double deltaWdash = deltaW*((1.0D + 1.0D/this.effectivePermittivity)/2.0D);
                    this.effectiveTrackWidth = this.trackWidth + deltaWdash;

                    // Zo
                    super.idealRealCharacteristicImpedance = (Fmath.ETA_0/(2.0D*Math.PI*Math.sqrt(2.0D*eplus1)));
                    //double aA = (14.0D + 8.0D/this.effectivePermittivity)*4.0D*this.plateSeparation/(11.0D*this.effectiveTrackWidth);
                    //double bB = Math.sqrt(aA*aA + (1.0D + 1.0D/this.effectivePermittivity)*Math.sqrt(Math.PI)/2.0D);
                    //super.idealRealCharacteristicImpedance *= Math.log(1.0D + 4.0D*this.plateSeparation*Math.sqrt(aA + bB)/this.effectiveTrackWidth);
                    double term1 = (1.0D + 1.0D/this.effectivePermittivity)*Math.PI*Math.PI/2.0D;
                    term1 += Fmath.square(4.0D*this.plateSeparation/this.effectiveTrackWidth);
                    term1 += Fmath.square((14.0D + 8.0D/this.effectivePermittivity)/11.0D);
                    term1 = Math.sqrt(term1);
                    term1 += ((14.0D + 8.0D/this.effectivePermittivity)/11.0D)*(4.0D*this.plateSeparation/this.effectiveTrackWidth);
                    term1 *= (4.0D*this.plateSeparation/this.effectiveTrackWidth);
                    term1 += 1.0D;
                    term1 = Math.log(term1);
                    super.idealRealCharacteristicImpedance *= term1;
                    break;
            case 0: // Edwards Hoffmann formula
                    if(!this.separationSet)throw new IllegalArgumentException("The strip line plate separation has not been entered");
                    if(!this.widthSet)throw new IllegalArgumentException("The strip line track width has not been entered");
                    if(!this.permittivitySet)throw new IllegalArgumentException("The relative permittivity has not been entered");

                    // Static Zo
                    double staticZo = 0.0;
                    if(wOverH<3.3D){
                        staticZo = Fmath.ETA_0/(Math.PI*Math.sqrt(2.0D*eplus1));
                        double term = Math.log(4.0D*hOverW + Math.sqrt(16.0D*hOverW*hOverW + 2.0D));
                        term -= 0.5*(eminus1/eplus1)*(Math.log(Math.PI/2.0D) + Math.log(4.0D/Math.PI)/this.relativePermittivity);
                        staticZo *= term;
                    }
                    else{
                        staticZo = Fmath.ETA_0/(2.0D*Math.sqrt(this.relativePermittivity));
                        double term = wOverH/2.0D + Math.log(4.0)/Math.PI + (Math.log(Math.E*Math.PI*Math.PI/16.0D)/(2.0D*Math.PI))*eminus1/(this.relativePermittivity*this.relativePermittivity);
                        term += (eplus1/(2.0D*Math.PI*this.relativePermittivity))*(Math.log(Math.PI*Math.E/2.0D) + Math.log(wOverH/2.0D + 0.94D));
                        staticZo /= term;
                    }

                    // Static effective relative permittivity
                    double staticEpsr = 0.0D;
                    double hdash = staticZo*Math.sqrt(2.0D*eplus1)*Math.PI/Fmath.ETA_0;
                    hdash += 0.5*(eminus1/eplus1)*(Math.log(Math.PI/2.0D) + Math.log(4.0D/Math.PI)/this.relativePermittivity);
                    if(wOverH<1.3D){
                        staticEpsr = (eplus1/2.0D)*Math.pow((1.0D - (1.0D/2.0D*hdash)*(eminus1/eplus1)*(Math.log(Math.PI/2.0D) + Math.log(4.0D/Math.PI)/this.relativePermittivity)), -2);
                    }
                    else{
                        staticEpsr = eplus1/2.0D + (eminus1/2.0D)*Math.pow((1.0D + 10.0D*hOverW), -0.555);
                    }

                    // Static effective width
                    double staticEffWidth = this.plateSeparation*Fmath.ETA_0/(staticZo*Math.sqrt(staticEpsr));

                    // Frequency dependency
                    if(super.frequency==0.0D){
                        super.idealRealCharacteristicImpedance = staticZo;
                        this.effectivePermittivity = staticEpsr;
                        this.effectiveTrackWidth = staticEffWidth;
                    }
                    else{
                        System.out.println("QQQ");
                        // Effective permittivity
                        double[] pp = new double[4];
                        pp[0] = 0.27488D + (0.6315D + 0.525D/Math.pow((1.0D + 0.157D*fh),20))*wOverH - 0.065683D*Math.exp(-8.7513D*wOverH);
                        pp[1] = 0.33622D*(1.0D - Math.exp(-0.03442D*this.relativePermittivity));
                        pp[2] = 0.0363D*Math.exp(-4.6D*wOverH)*(1.0D - Math.exp(-Math.pow(fh/3.87D,4.97D)));
                        pp[3] = 1.0D + 2.751D*(1.0D - Math.exp(-Math.pow(this.relativePermittivity/15.916D,8)));
                        double pf = pp[0]*pp[1]*Math.pow((0.1844D + pp[2]*pp[3])*10.0D*fh, 1.5763);
                        this.effectivePermittivity = this.relativePermittivity - (this.relativePermittivity - staticEpsr)/(1.0D + pf);

                        // Frequency dependent ideal characteristic impedance
                        double[] rr = new double[12];
                        rr[0] = 4.766D*Math.exp(-3.228D*Math.pow(wOverH,0.641D));
                        rr[1] = 0.016D + Math.pow((0.0514D*this.relativePermittivity),4.524D);
                        rr[2] = 1.206D - 0.3144D*Math.exp(-0.0389D*Math.pow(this.relativePermittivity, 1.4D))*(1.0D - Math.exp(-0.267D*Math.pow(wOverH,7)));
                        rr[3] = 1.0D + 1.275D*(1.0D - Math.exp(-0.00463D*rr[0]*Math.pow(this.relativePermittivity,1.674D)*Math.pow(fh/18.37D,2.745D)));
                        rr[4] = (5.086D*pp[1]*Math.pow(fh/28.84D,12))/(0.384D+ 0.386D*rr[1]);
                        rr[4] *= Math.exp(-22.2D*Math.pow(wOverH,1.92D))/(1.0D + 1.3D*Math.pow(fh/28.84D,12));
                        rr[4] *= Math.pow(eminus1,6)/(1.0D + 10.0D*Math.pow(eminus1,6));
                        rr[5] = 1.0D/(0.0962D + Math.pow(19.47/(fh),6));
                        rr[6] = 1.0D/(1.0D + 0.00245D*Math.pow(wOverH,2));
                        rr[7] = 0.9408D*Math.pow(this.effectivePermittivity, rr[3]) - 0.9603D;
                        rr[8] = (0.9408D - rr[4])*Math.pow(staticEpsr, rr[3]) - 0.9603D;
                        rr[9] = 0.707D*(0.00044D*Math.pow(this.relativePermittivity, 2.136D) + 0.0184D)*Math.pow(fh/12.3D,1.097D);
                        rr[10] = 1.0D + 0.0503D*Math.pow(this.relativePermittivity, 2)*rr[5]*(1.0D - Math.exp(-Math.pow(wOverH/15.0D,6)));
                        rr[11] = rr[2]*(1.0D - 1.1241D*(rr[6]/rr[10])*Math.exp(-0.026D* Math.pow(fh, 1.1566D) - rr[9]));
                        this.idealRealCharacteristicImpedance = staticZo*Math.pow(rr[7]/rr[8],rr[11]);
                        System.out.println(rr[7] + " " + rr[8] + " " + rr[11]);

                        // Effective width
                        this.effectiveTrackWidth = this.plateSeparation*Fmath.ETA_0/(this.idealRealCharacteristicImpedance*Math.sqrt(this.effectivePermittivity));
                    }
                    break;
            default: throw new IllegalArgumentException("Formulae option, " + formulaOption + ", must lie between 0 and 2 inclusive");
        }

        super.idealRealCharacteristicImpedance *= Math.sqrt(relativePermeability);
        super.idealCharacteristicImpedance = new Complex(super.idealRealCharacteristicImpedance, 0.0D);
        this.z0calculated = true;
        this.calculateDistributedCapacitanceAndInductance();

        return super.idealCharacteristicImpedance;

    }

    // Return the ideal characteristic impedance as a Complex
    public Complex getIdealCharacteristicImpedance(){
        if(!this.z0calculated)this.calcIdealCharacteristicImpedance();
        return super.idealCharacteristicImpedance;
    }

    // Return the characteristic impedance, Zo, of an ideal line as a double
    public double getIdealCharacteristicImpedanceAsReal(){
        if(!this.z0calculated)this.calcIdealCharacteristicImpedance();
        return super.idealRealCharacteristicImpedance;
    }




    // CALCULATE DISTRIBUTED PARAMETERS
    private void calculateDistributedCapacitanceAndInductance(){
        if(!this.z0calculated)this.calcIdealCharacteristicImpedance();
        switch(formulaOption){
            case 2: // IPC-D-317A formula
                    double hold = Math.log(5.98D*this.plateSeparation/(0.8D*this.trackWidth + this.trackThickness));
                    super.distributedCapacitance = 0.67D*25.4D*1.0E-15*(this.relativePermittivity + 1.41D)/hold;
                    break;
            case 1: // Wadell (Wheelen) Schneider formula
            case 0: // York formulae collection
                    super.distributedCapacitance = this.effectiveTrackWidth*this.effectivePermittivity*Fmath.EPSILON_0/this.plateSeparation;
                    break;
            default: throw new IllegalArgumentException("Formulae option, " + formulaOption + ", must lie between 0 and 2 inclusive");
        }
        super.distributedInductance = super.distributedCapacitance*this.idealRealCharacteristicImpedance*this.idealRealCharacteristicImpedance;

    }


    // DEEP COPY
    public SurfaceStripLine copy(){

        if(this==null){
            return null;
        }
        else{
            SurfaceStripLine tl = new SurfaceStripLine();

            tl.formulaOption = this.formulaOption;
            tl.trackWidth = this.trackWidth;
            tl.effectiveTrackWidth = this.effectiveTrackWidth;
            tl.trackThickness = this.trackThickness;
            tl.plateSeparation = this.plateSeparation;
            tl.separationSet = this.separationSet;
            tl.thicknessSet = this.thicknessSet;
            tl.widthSet = this.widthSet;
            tl.z0calculated = this.z0calculated;

            tl.relativePermittivity = this.relativePermittivity;
            tl.effectivePermittivity = this.effectivePermittivity;
            tl.relativePermeability = this.relativePermeability;
            tl.permittivitySet = this.permittivitySet;

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

            SurfaceStripLine tl = new SurfaceStripLine();

            tl.formulaOption = this.formulaOption;
            tl.trackWidth = this.trackWidth;
            tl.effectiveTrackWidth = this.effectiveTrackWidth;
            tl.trackThickness = this.trackThickness;
            tl.plateSeparation = this.plateSeparation;
            tl.separationSet = this.separationSet;
            tl.thicknessSet = this.thicknessSet;
            tl.widthSet = this.widthSet;
            tl.z0calculated = this.z0calculated;

            tl.relativePermittivity = this.relativePermittivity;
            tl.effectivePermittivity = this.effectivePermittivity;
            tl.relativePermeability = this.relativePermeability;
            tl.permittivitySet = this.permittivitySet;
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
