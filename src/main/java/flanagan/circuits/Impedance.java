/*      Class Impedance
*
*       Basic impedance methods
*
*       WRITTEN BY: Dr Michael Thomas Flanagan
*
*       DATE:    May 2007
*       UPDATE:  11, 12, 15, 22, 26 June and 15 July 2007
*
*       DOCUMENTATION:
*       See Michael T Flanagan's Java library on-line web pages:
*       http://www.ee.ucl.ac.uk/~mflanaga/java/Impedance.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecSimulation.html
*       http://www.ee.ucl.ac.uk/~mflanaga/java/ImpedSpecRegression.html

*       http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*       Copyright (c) May 2007, July 2007    Michael Thomas Flanagan
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

public class Impedance{

    // class variables
    protected static int numberOfModels = 44;   // number of circuit models included in this class

    // Constructor
    public Impedance(){
    }

    // Get the resistance complex impedance
    public static Complex resistanceImpedance(double resistance){
        return new Complex(resistance, 0.0D);
    }

    // Get the capacitance complex impedance for a given radial frequency
    public static Complex capacitanceImpedance(double capacitance, double omega){
        return new Complex(0.0D, -1.0D/(capacitance*omega));
    }

    // Get the inductance complex impedance for a given radial frequency
    public static Complex inductanceImpedance(double inductance, double omega){
        return new Complex(0.0D, inductance*omega);
    }

    // Get the 'infinite' Warburg impedance
    public static Complex infiniteWarburgImpedance(double sigma, double omega){
        double term = sigma/Math.sqrt(omega);
        return new Complex(term, -term);
    }

    // Get the 'finite' Warburg impedance
    public static Complex finiteWarburgImpedance(double sigma, double tanhConst, double omega){
        Complex zVal1 = new Complex(sigma*Math.sqrt(omega), 0.0D);
        Complex zVal2 = new Complex(tanhConst, 0.0D);
        Complex zVal3 = new Complex(0.0D, omega);
        Complex zVal4 = Complex.sqrt(zVal3);
        Complex zVal5 = zVal2.times(zVal4);
        Complex zVal6 = Complex.tanh(zVal5);
        Complex zVal7 = zVal1.times(zVal6);
        Complex zVal8 = Complex.plusOne().minus(Complex.plusJay());
        return zVal8.times(zVal7);
    }

    // Get the constant phase element impedance
    public static Complex constantPhaseElementImpedance(double cpeCoeff, double alpha, double omega){
        Complex jOmega = new Complex(0.0D, omega);
        Complex jOmegaAlpha = Complex.pow(jOmega,-alpha);
        Complex coeff = new Complex(cpeCoeff, 0.0D);
        return coeff.times(jOmegaAlpha);
    }

    // Calculate the impedance of two impedances in series
    public static Complex impedanceInSeries(Complex z1, Complex z2){
        return z1.plus(z2);
    }

    // Calculate the impedance of two impedances in series
    public static Complex impedanceInSeries(double z1, Complex z2){
        return z2.plus(z1);
    }

    // Calculate the impedance of two impedances in series
    public static Complex impedanceInSeries(Complex z1, double z2){
        return z1.plus(z2);
    }

    // Calculate the impedance of two impedances in series
    public static Complex impedanceInSeries(double z1, double z2){
        return new Complex(z1+z2, 0.0D);
    }

    // Calculate the impedance of a resistor and capacitor in series
    public static Complex rInSeriesWithC(double res, double cap, double omega){
        Complex ret = new Complex(res, -1.0D/(cap*omega));
        return ret;
    }

    // Calculate the impedance of a resistor and an inductor in series
    public static Complex rInSeriesWithL(double res, double ind, double omega){
        Complex ret = new Complex(res, ind*omega);
        return ret;
    }

    // Calculate the impedance of a capacitor and an inductor in series
    public static Complex cInSeriesWithL(double cap, double ind, double omega){
        Complex z1 = new Complex(0.0D, -1.0D/(cap*omega));
        Complex z2 = new Complex(0.0D, ind*omega);
        return z1.plus(z2);
    }

    // Calculate the impedance of two impedances in parallel
    public static Complex impedanceInParallel(Complex z1, Complex z2){
        Complex ret = z1.times(z2);
        return ret.over(z1.plus(z2));
    }

    // Calculate the impedance of two impedances in parallel
    public static Complex impedanceInParallel(Complex z1, double z2){
        Complex ret = z1.times(z2);
        return ret.over(z1.plus(z2));
    }

    // Calculate the impedance of two impedances in parallel
    public static Complex impedanceInParallel(double z1, Complex z2){
        Complex ret = z2.times(z1);
        return ret.over(z2.plus(z1));
    }

    // Calculate the impedance of two impedances in parallel
    public static Complex impedanceInParallel(double z1, double z2){
        return new Complex(z1*z2/(z1+z2), 0.0D);
    }

    // Calculate the impedance of a resistor and capacitor in parallel
    public static Complex rInParallelWithC(double res, double cap, double omega){
        Complex zC = new Complex(0.0D, -1.0D/(cap*omega));
        Complex zR = new Complex(res, 0.0D);
        Complex ret = zC.times(zR);
        return ret.over(zC.plus(zR));
    }

    // Calculate the impedance of a resistor and an inductor in parallel
    public static Complex rInParallelWithL(double res, double ind, double omega){
        Complex zL = new Complex(0.0D, ind*omega);
        Complex zR = new Complex(res, 0.0D);
        Complex ret = zL.times(zR);
        return ret.over(zL.plus(zR));
    }

    // Calculate the impedance of a capacitor and an inductor in parallel
    public static Complex cInParallelWithL(double cap, double ind, double omega){
        Complex z1 = new Complex(0.0D, -1.0D/(cap*omega));
        Complex z2 = new Complex(0.0D, ind*omega);
        Complex z3 = z1.plus(z2);
        Complex z4 = z1.times(z2);
        return z4.over(z3);
    }

    // return model circuit component list
    public static String[] modelComponents(int modelNumber){
        // Components within models
        String[][] compName  = {{" "},{"R1"},{"C1"},{"L1"},{"W1"},{"Fsigma1", "Fdelta1"},{"Qsigma1", "Qalpha1"},{"R1", "C1"},{"R1", "L1"},{"L1", "C1"},{"R1", "C1"},{"R1", "L1"},{"L1", "C1"},{"R1", "C1", "R2"},{"R1", "C1", "R2", "L1"},{"R1", "C1", "R2", "L1"},{"R1", "C1", "C2"},{"R1", "C1", "C2"},{"R1", "C1", "R2", "C2"},{"R1", "C1", "R2", "C2"},{"R1", "C1", "R2", "C2", "R3"},{"R1", "C1", "R2", "C2", "R3"},{"R1", "C1", "R2", "C2", "R3", "C3"},{"R1", "C1", "R2", "C2", "R3", "C3", "R4"},{"R1", "C1", "W1", "R2"},{"R1", "C1", "Fsigma1", "Fdelta1", "R2"},{"R1", "C1", "Qsigma1", "Qalpha1", "R2"},{"R1", "C1", "R2", "C2", "W1"},{"R1", "C1", "R2", "C2", "W3", "C3", "R4"}, {"R1", "C1", "R2", "Qsigma1", "Qalpha1"},{"R1", "C1", "R2", "Qsigma1", "Qalpha1", "R3"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "Qsigma3", "Qalpha3"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "Qsigma3", "Qalpha3", "R4"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "Qsigma3", "Qalpha3", "R4", "C1"},{"C1", "Qsigma1", "Qalpha1", "C2", "Qsigma2", "Qalpha2", "C3", "Qsigma3", "Qalpha3"},{"C1", "Qsigma1", "Qalpha1", "C2", "Qsigma2", "Qalpha2", "C3", "Qsigma3", "Qalpha3", "R1"},{"R1", "Qsigma1", "Qalpha1", "C1", "R2","Qsigma2", "Qalpha2", "C2", "R3", "Qsigma3", "Qalpha3", "C3"},{"R1", "Qsigma1", "Qalpha1", "C1", "R2","Qsigma2", "Qalpha2", "C2", "R3", "Qsigma3", "Qalpha3", "C3", "R4"},{"R1", "Qsigma1", "Qalpha1", "C1", "R2","Qsigma2", "Qalpha2", "C2"},{"R1", "Qsigma1", "Qalpha1", "C1", "R2","Qsigma2", "Qalpha2", "C2", "R3"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "Qsigma3", "Qalpha3", "R4", "C1"},{"R1", "Qsigma1", "Qalpha1", "R2", "C2", "R3", "Qsigma3", "Qalpha3", "R4", "C1"},{"R1", "Qsigma1", "Qalpha1", "R2", "C2", "R3", "Qsigma3", "Qalpha3", "R4"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "Qsigma3", "Qalpha3", "R4", "C1"},{"R1", "Qsigma1", "Qalpha1", "R2", "Qsigma2", "Qalpha2", "R3", "C1"}};
        return compName[modelNumber];
    }


    // Calculate the impedance of the listed model circuits
    public static Complex modelImpedance(double[] parameter, double omega, int modelNumber){

        Complex zVal = null;
        Complex z1 = null;
        Complex z2 = null;
        Complex z3 = null;
        Complex z4 = null;
        switch(modelNumber){
            case 1: zVal = resistanceImpedance(parameter[0]);
                    break;
            case 2: zVal = capacitanceImpedance(parameter[0], omega);
                    break;
            case 3: zVal = inductanceImpedance(parameter[0], omega);
                    break;
            case 4: zVal = infiniteWarburgImpedance(parameter[0], omega);
                    break;
            case 5: zVal = finiteWarburgImpedance(parameter[0], parameter[1], omega);
                    break;
            case 6: zVal = constantPhaseElementImpedance(parameter[0], parameter[1], omega);
                    break;
            case 7: zVal = rInSeriesWithC(parameter[0], parameter[1], omega);
                    break;
            case 8: zVal = new Complex(parameter[0], parameter[1]*omega);
                    break;
            case 9: z1 = new Complex(0.0D, -1.0D/(parameter[0]*omega));
                    z2 = new Complex(0.0D, parameter[1]*omega);
                    zVal = impedanceInSeries(z1, z2);
                    break;
            case 10:zVal = rInParallelWithC(parameter[0], parameter[1], omega);
                    break;
            case 11:z1 = new Complex(parameter[0], 0.0D);
                    z2 = new Complex(0.0D, parameter[1]*omega);
                    zVal = impedanceInParallel(z1, z2);
                    break;
            case 12:z1 = new Complex(0.0D, -1.0D/(parameter[0]*omega));
                    z2 = new Complex(0.0D, parameter[1]*omega);
                    zVal = impedanceInParallel(z1, z2);
                    break;
            case 13:zVal = rInParallelWithC(parameter[0], parameter[1], omega);
                    zVal = zVal.plus(parameter[2]);
                    break;
            case 14:zVal = rInParallelWithC(parameter[0], parameter[1], omega);
                    zVal = zVal.plus(parameter[2]);
                    z1 = new Complex(0.0D, parameter[3]*omega);
                    zVal = zVal.plus(z1);
                    break;
            case 15:zVal = rInParallelWithC(parameter[0], parameter[1], omega);
                    zVal = zVal.plus(parameter[2]);
                    z1 = new Complex(0.0D, parameter[3]*omega);
                    zVal = impedanceInParallel(zVal, z1);
                    break;
            case 16:zVal = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = new Complex(0.0D, -1.0D/(parameter[2]*omega));
                    zVal = zVal.plus(z2);
                    break;
            case 17:z1 = new Complex(parameter[0], -1.0D/(parameter[1]*omega));
                    z2 = new Complex(0.0D, -1.0D/(parameter[2]*omega));
                    zVal = (z1.times(z2)).over(z2.plus(z1));
                    break;
            case 18:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = new Complex(parameter[2], -1.0D/(parameter[3]*omega));
                    zVal = z1.plus(z2);
                    break;
            case 19:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    break;
            case 20:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    zVal = zVal.plus(parameter[4]);
                    break;
            case 21:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = z1.plus(parameter[2]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = z4.plus(parameter[4]);
                    break;
            case 22:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    z3 = rInParallelWithC(parameter[4], parameter[5], omega);
                    zVal = zVal.plus(z3);
                    break;
            case 23:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    z3 = rInParallelWithC(parameter[4], parameter[5], omega);
                    zVal = zVal.plus(z3);
                    zVal = zVal.plus(parameter[6]);
                    break;
            case 24:z1 = infiniteWarburgImpedance(parameter[2], omega);
                    z2 = impedanceInSeries(parameter[0], z1);
                    z3 = capacitanceImpedance(parameter[1], omega);
                    z4 = impedanceInParallel(z2, z3);
                    zVal = impedanceInSeries(z4, parameter[3]);
                    break;
            case 25:z1 = finiteWarburgImpedance(parameter[2], parameter[3], omega);
                    z2 = impedanceInSeries(parameter[0], z1);
                    z3 = capacitanceImpedance(parameter[1], omega);
                    z4 = impedanceInParallel(z2, z3);
                    zVal = impedanceInSeries(z4, parameter[4]);
                    break;
            case 26:z1 = constantPhaseElementImpedance(parameter[2], parameter[3], omega);
                    z2 = impedanceInSeries(parameter[0], z1);
                    z3 = capacitanceImpedance(parameter[1], omega);
                    z4 = impedanceInParallel(z2, z3);
                    zVal = impedanceInSeries(z4, parameter[4]);
                    break;
            case 27:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    z3 = infiniteWarburgImpedance(parameter[4], omega);
                    zVal = zVal.plus(z3);
                    break;
            case 28:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = rInParallelWithC(parameter[2], parameter[3], omega);
                    zVal = z1.plus(z2);
                    z3 = infiniteWarburgImpedance(parameter[4], omega);
                    z4 = new Complex(0.0D, -1.0D/(parameter[5]*omega));
                    z4 = impedanceInParallel(z3, z4);
                    zVal = zVal.plus(z4);
                    zVal = zVal.plus(parameter[6]);
                    break;
            case 29:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = constantPhaseElementImpedance(parameter[3], parameter[4], omega);
                    z3 = impedanceInParallel(z2, parameter[2]);
                    zVal = z1.plus(z3);
                    break;
            case 30:z1 = rInParallelWithC(parameter[0], parameter[1], omega);
                    z2 = constantPhaseElementImpedance(parameter[3], parameter[4], omega);
                    z3 = impedanceInParallel(z2, parameter[2]);
                    zVal = z1.plus(z3);
                    zVal = zVal.plus(parameter[5]);
                    break;
            case 31:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z2 = impedanceInParallel(z1, parameter[6]);
                    zVal = zVal.plus(z2);
                    break;
            case 32:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z2 = impedanceInParallel(z1, parameter[6]);
                    zVal = zVal.plus(z2);
                    zVal = zVal.plus(parameter[9]);
                    break;
            case 33:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z2 = impedanceInParallel(z1, parameter[6]);
                    zVal = zVal.plus(z2);
                    zVal = zVal.plus(parameter[9]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[10]*omega));
                    zVal = impedanceInParallel(zVal, z3);
                    break;
            case 34:z1 = new Complex(0.0D, -1.0D/(parameter[0]*omega));
                    z2 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, z2);
                    z1 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    z2 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z3 = impedanceInParallel(z1, z2);
                    zVal = zVal.plus(z3);
                    z1 = new Complex(0.0D, -1.0D/(parameter[6]*omega));
                    z2 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z3 = impedanceInParallel(z1, z2);
                    zVal = zVal.plus(z3);
                    break;
            case 35:z1 = new Complex(0.0D, -1.0D/(parameter[0]*omega));
                    z2 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, z2);
                    z1 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    z2 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z3 = impedanceInParallel(z1, z2);
                    zVal = zVal.plus(z3);
                    z1 = new Complex(0.0D, -1.0D/(parameter[6]*omega));
                    z2 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z3 = impedanceInParallel(z1, z2);
                    zVal = zVal.plus(z3);
                    zVal = zVal.plus(parameter[9]);
                    break;
            case 36:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    z2 = z1.plus(parameter[0]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    zVal = impedanceInParallel(z2, z3);
                    z1 = constantPhaseElementImpedance(parameter[5], parameter[6], omega);
                    z2 = z1.plus(parameter[4]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[7]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    z1 = constantPhaseElementImpedance(parameter[9], parameter[10], omega);
                    z2 = z1.plus(parameter[8]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[11]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    break;
            case 37:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    z2 = z1.plus(parameter[0]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    zVal = impedanceInParallel(z2, z3);
                    z1 = constantPhaseElementImpedance(parameter[5], parameter[6], omega);
                    z2 = z1.plus(parameter[4]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[7]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    z1 = constantPhaseElementImpedance(parameter[9], parameter[10], omega);
                    z2 = z1.plus(parameter[8]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[11]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    zVal = zVal.plus(parameter[12]);
                    break;
            case 38:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    z2 = z1.plus(parameter[0]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    zVal = impedanceInParallel(z2, z3);
                    z1 = constantPhaseElementImpedance(parameter[5], parameter[6], omega);
                    z2 = z1.plus(parameter[4]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[7]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    break;
            case 39:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    z2 = z1.plus(parameter[0]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[3]*omega));
                    zVal = impedanceInParallel(z2, z3);
                    z1 = constantPhaseElementImpedance(parameter[5], parameter[6], omega);
                    z2 = z1.plus(parameter[4]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[7]*omega));
                    z4 = impedanceInParallel(z2, z3);
                    zVal = zVal.plus(z4);
                    zVal = z4.plus(parameter[8]);
                    break;
            case 40:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z2 = impedanceInParallel(z1, parameter[6]);
                    zVal = zVal.plus(z2);
                    z3 = new Complex(0.0D, -1.0D/(parameter[10]*omega));
                    zVal = impedanceInParallel(zVal, z3);
                    zVal = zVal.plus(parameter[9]);
                    break;
            case 41:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = new Complex(0.0D, -1.0D/(parameter[4]*omega));
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[6], parameter[7], omega);
                    z2 = impedanceInParallel(z1, parameter[5]);
                    zVal = zVal.plus(z2);
                    z3 = new Complex(0.0D, -1.0D/(parameter[9]*omega));
                    zVal = impedanceInParallel(zVal, z3);
                    zVal = zVal.plus(parameter[8]);
                    break;
            case 42:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = new Complex(0.0D, -1.0D/(parameter[4]*omega));
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[6], parameter[7], omega);
                    z2 = impedanceInParallel(z1, parameter[5]);
                    zVal = zVal.plus(z2);
                    zVal = zVal.plus(parameter[8]);
                    break;
            case 43:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    z1 = constantPhaseElementImpedance(parameter[7], parameter[8], omega);
                    z2 = impedanceInParallel(z1, parameter[6]);
                    zVal = zVal.plus(z2);
                    zVal = zVal.plus(parameter[9]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[10]*omega));
                    zVal = impedanceInParallel(zVal, z3);
                    break;
            case 44:z1 = constantPhaseElementImpedance(parameter[1], parameter[2], omega);
                    zVal = impedanceInParallel(z1, parameter[0]);
                    z1 = constantPhaseElementImpedance(parameter[4], parameter[5], omega);
                    z2 = impedanceInParallel(z1, parameter[3]);
                    zVal = zVal.plus(z2);
                    zVal = zVal.plus(parameter[6]);
                    z3 = new Complex(0.0D, -1.0D/(parameter[7]*omega));
                    zVal = impedanceInParallel(zVal, z3);
                    break;
            default:throw new IllegalArgumentException("No model " + modelNumber + " exists");
        }

        return zVal;
    }

    // returns the Warburg coefficient, sigma, - infinite diffusion layer
    public static double warburgSigma(double electrodeArea, double oxidantDiffCoeff , double oxidantConcn, double reductantDiffCoeff, double reductantConcn, double tempCelsius, int electronsTransferred){
        double firstTerm = Fmath.R_GAS*(tempCelsius - Fmath.T_ABS)/(Fmath.square(electronsTransferred*Fmath.F_FARADAY)*electrodeArea*Math.sqrt(2));
        double secondTerm = 1.0D/(oxidantConcn*Math.sqrt(oxidantDiffCoeff));
        double thirdTerm = 1.0D/(reductantConcn*Math.sqrt(reductantDiffCoeff));
        return firstTerm*(secondTerm + thirdTerm);
    }


    // returns the capacitance of a parallel plate capacitor - plate width and length enterd
    public static double parallelPlateCapacitance(double plateArea, double plateSeparation, double relativePermittivity){
        return plateArea*relativePermittivity*Fmath.EPSILON_0/plateSeparation;
    }
    // returns the capacitance of a parallel plate capacitor - plate area entered
    public static double parallelPlateCapacitance(double plateLength, double plateWidth, double plateSeparation, double relativePermittivity){
        return plateLength*plateWidth*relativePermittivity*Fmath.EPSILON_0/plateSeparation;
    }

    // returns the capacitance of coaxial cylinders, e.g. model of a coaxial cable
    public static double coaxialCapacitance(double cylinderLength, double innerRadius, double outerRadius, double relativePermittivity){
        return 2.0D*Math.PI*relativePermittivity*Fmath.EPSILON_0*cylinderLength/Math.log(outerRadius/innerRadius);
    }

    // returns the capacitance of parallel wires
    public static double parallelWiresCapacitance(double wireLength, double wireRadius, double wireSeparation, double relativePermittivity){
        return Math.PI*relativePermittivity*Fmath.EPSILON_0*wireLength/Math.log((wireSeparation - wireRadius)/wireRadius);
    }

    // returns the inductance of parallel plates - plate width and length entered
    public static double parallelPlateInductance(double plateLength, double plateWidth, double plateSeparation, double relativePermeability){
        return relativePermeability*Fmath.MU_0*plateSeparation*plateLength/plateWidth;
    }

    // returns the inductance of coaxial cylinders, e.g. model of a coaxial cable
    public static double coaxialInductance(double cylinderLength, double innerRadius, double outerRadius, double relativePermeability){
        return relativePermeability*Fmath.MU_0*cylinderLength*Math.log(outerRadius/innerRadius)/(2.0D*Math.PI);
    }

    // returns the inductance of parallel wires
    public static double parallelWiresInductance(double wireLength, double wireRadius, double wireSeparation, double relativePermeability){
        return relativePermeability*Fmath.MU_0*wireLength*Math.log((wireSeparation - wireRadius)/wireRadius)/Math.PI;
    }

    // returns the magnitude of a rectangular complex variable
    public static double getMagnitude(Complex variable){
        return variable.abs();
    }

    // returns the argument (phase), in radians, of a rectangular complex variable
    public static double getPhaseRad(Complex variable){
        return variable.argRad();
    }

    // returns the argument (phase), in degrees, of a rectangular complex variable
    public static double getPhaseDeg(Complex variable){
        return variable.argDeg();
    }

    // converts magnitude and phase (radians) to a rectangular complex variable
    public static Complex polarRad(double magnitude, double phase){
        Complex aa = new Complex();
        aa.polarRad(magnitude, phase);
        return aa;
    }

    // converts magnitude and phase (degrees) to a rectangular complex variable
    public static Complex polarDeg(double magnitude, double phase){
        Complex aa = new Complex();
        aa.polarDeg(magnitude, phase);
        return aa;
    }

    // Converts frequency (Hz) to radial frequency
    public static double frequencyToRadialFrequency(double frequency){
        return  2.0D*Math.PI*frequency;
    }

    // Converts radial frequency to frequency (Hz)
    public static double radialFrequencyToFrequency(double omega){
        return  omega/(2.0D*Math.PI);
    }

    // METAL WIRE/CABLE/PLATE RESISTANCES
    // length = length in metres, area = cross-sectional
    // area = area in square metres
    // tempC = temptemperature in degrees Celsius
    // Data from  (1) Syed A Nasar, Electric Power Systems, Schaum's ouTlines, McGraw-Hill, 1990
    //            (2) Handbook of Chemistry and Physics, Chemical Rubber Publishing Company, Cleveland, Ohio, 37th Edition, 1955

    // Returns the resistance of aluminium
    public static double resistanceAluminium(double length, double area, double tempC){
        double rho = 2.824e-8;      // Al resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0039;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of aluminium
    public static double resistanceAluminum(double length, double area, double tempC){
        double rho = 2.824e-8;      // Al resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0039;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of hard drawn copper
    public static double resistanceHardDrawnCopper(double length, double area, double tempC){
        double rho = 1.771e-8;      // hard drawn Cu resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00382;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of annealed copper
    public static double resistanceAnnealedCopper(double length, double area, double tempC){
        double rho = 1.7241e-8;     // annealed Cu resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00393;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of 99.98% pure iron
    public static double resistanceIron(double length, double area, double tempC){
        double rho = 10.0e-8;      // Fe resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.005;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of manganese steel
    public static double resistanceManganeseSteel(double length, double area, double tempC){
        double rho = 70.0e-8;     // manganese steel resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.001;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of Siemens-Martin steel
    public static double resistanceSiemensMartinSteel(double length, double area, double tempC){
        double rho = 18.0e-8;     // Siemens-Martin steel resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.003;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of B. B. steel
    public static double resistanceBBSteel(double length, double area, double tempC){
        double rho = 11.9e-8;     // B. B. steel resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.004;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of E. B. B. steel
    public static double resistanceEBBSteel(double length, double area, double tempC){
        double rho = 10.4e-8;     // E. B. B. steel resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.005;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of brass
    // Average value of a range of resistivities from 6.4 to 8.4
    public static double resistanceBrass(double length, double area, double tempC){
        double rho = 7.4e-8;      // average brass resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.002;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of drawn tungsten
    public static double resistanceDrawnTunsten(double length, double area, double tempC){
        double rho = 5.6e-8;       // drawn tungsten resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0045;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of silver
    public static double resistanceSilver(double length, double area, double tempC){
        double rho = 1.59e-8;       // Ag resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0038;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of gold
    public static double resistanceGold(double length, double area, double tempC){
        double rho = 2.84e-8;       // Au resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0034;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of platinum
    public static double resistancePlatinum(double length, double area, double tempC){
        double rho = 10.0e-8;       // Pt resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.003;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of nickel
    public static double resistanceNickel(double length, double area, double tempC){
        double rho = 7.8e-8;        // Ni resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.006;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of drawn molybdenum
    public static double resistanceMolybdenum(double length, double area, double tempC){
        double rho = 5.7e-8;        // Mo resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.004;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of phosphor bronze
    public static double resistancePhosphorBronze(double length, double area, double tempC){
        double rho = 11.0e-8;       // phosphor bronze resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0033;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of tin
    public static double resistanceTin(double length, double area, double tempC){
        double rho = 11.5e-8;       // Sn resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0042;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of Nichrome
    public static double resistanceNichrome(double length, double area, double tempC){
        double rho = 100.0e-8;      // Nichrome resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0004;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of palladium
    public static double resistancePalladium(double length, double area, double tempC){
        double rho = 11.0e-8;       // Pd resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0033;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of tantalum
    public static double resistanceTantalum(double length, double area, double tempC){
        double rho = 15.5e-8;       // Ta resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0031;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of Therlo
    public static double resistanceTherlo(double length, double area, double tempC){
        double rho = 47.0e-8;       // Therlo resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00001;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of Monel metal
    public static double resistanceMonelMetal(double length, double area, double tempC){
        double rho = 42.0e-8;        // Monel metal resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.002;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of manganan
    public static double resistanceManganan(double length, double area, double tempC){
        double rho = 44.0e-8;        // Ni resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00001;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of constantan
    public static double resistanceConstantan(double length, double area, double tempC){
        double rho = 49.0e-8;       // Constantan resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00001;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }


    // Returns the resistance of antimony
    public static double resistanceAntimony(double length, double area, double tempC){
        double rho = 41.7e-8;       // Sb resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0036;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of cobalt
    public static double resistanceCobalt(double length, double area, double tempC){
        double rho = 9.8e-8;        // Co resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0033;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of magnesium
    public static double resistanceMagnesium(double length, double area, double tempC){
        double rho = 4.6e-8;        // Mg resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.004;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of zinc
    public static double resistanceZinc(double length, double area, double tempC){
        double rho = 5.8e-8;        // Zn resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0037;       // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of mercury
    public static double resistanceMercury(double length, double area, double tempC){
        double rho = 95.738e-8;     // Hg resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.00089;     // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of lead
    public static double resistanceLead(double length, double area, double tempC){
        double rho = 22.0e-8;       // Pb resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0039;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of German silver
    public static double resistanceGermanSilver(double length, double area, double tempC){
        double rho = 33.0e-8;       // German silver resistivity, ohms.metre, at 20 degrees Celsius
        double alpha = 0.0004;      // temperature coefficient, at 20 C, reciprocal degrees Celsius
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of material of restivity, rho, and temperature coefficient, alpha
    public static double resistivityToResistance(double rho, double alpha, double length, double area, double tempC){
        double resistance20 = rho*length/area;
        return resistance20*(1.0 + alpha*(tempC - 20.0));
    }

    // Returns the resistance of material of restivity, rho,
    public static double resistivityToResistance(double rho, double length, double area){
        return rho*length/area;
    }

    // Returns the resistivity of material of resistance, resistance
    public static double resistanceToResistivity(double resistance, double length, double area){
        return resistance*area/length;
    }
}
