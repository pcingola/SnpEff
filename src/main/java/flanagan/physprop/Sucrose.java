/*
*   Sucrose Class
*
*   Methods for returning the physical properties of
*   aqueous sucrose solutions:
*   viscosity, density, refractive index, solubility, specific volume,
*   diffusion coefficient, mole fraction and molecular weight.
*   Methods for interconverting molar - grams per litre - percent weight by weight.
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: July 2003
*   Updated: 1 July 2003,  May 2004, 29-31 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Sucrose.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2003 - 2008   Michael Thomas Flanagan
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

package flanagan.physprop;

import flanagan.math.Fmath;
import flanagan.math.ArrayMaths;
import flanagan.interpolation.CubicSpline;
import flanagan.interpolation.BiCubicSpline;

public class Sucrose{

    public static final double MOLWEIGHT = 342.3;  // Sucrose molecular weight

    // METHODS

    // Returns the viscosity (Pa s) of sucrose solutions as a function of
    // the g/l sucrose concentration and temperature.
    // Interpolation - natural cubic spline and bicubic spline - log of the viscosity
    // Data - Rubber Handbook
    public static double viscosity(double gPerL, double temperature){


        if(temperature<0.0)throw new IllegalArgumentException("Temperature, " + temperature + ", out of range");
        if(gPerL<0.0)throw new IllegalArgumentException("Concentration, " + gPerL + " g/l, out of range");

        double[] visc20 = {1.000, 1.013, 1.026, 1.039, 1.053, 1.067, 1.082, 1.097, 1.112, 1.128, 1.144, 1.160, 1.177, 1.195, 1.213, 1.232, 1.251, 1.271, 1.291, 1.312, 1.333, 1.378, 1.426, 1.477, 1.531, 1.589, 1.650, 1.716, 1.786, 1.861, 1.941, 2.120, 2.326, 2.568, 2.849, 3.181, 3.754, 4.044, 4.612, 5.304, 6.150, 7.220, 8.579, 10.28, 12.49, 15.40, 19.30, 24.63, 32.06, 42.69, 58.37, 82.26, 119.9, 181.7, 287.9, 480.6, 853.2, 1628};
        int viscLength = visc20.length;
        for(int i=0; i<viscLength; i++)visc20[i] *= 1.005;
        double[] concnWA = {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84};
	    double[] concnGA = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
        ArrayMaths amC = new ArrayMaths(concnGA);
        double[] zero = {1.7921, 1.5188, 1.3077, 1.1404, 1.0050, 0.8937, 0.8007, 0.7225, 0.6560, 0.5988, 0.5454, 0.5064, 0.4688, 0.4355, 0.4061, 0.3799, 0.3565, 0.3355, 0.3165, 0.2994,  0.2838};
        int length0 = zero.length;

        double[] twenty = {3.804, 3.154, 2.652, 2.267, 1.960, 1.704, 1.504, 1.331, 1.193, 1.070, 0.970, 0.884, 0.808, 0.742, 0.685, 0.635, 0.590, 0.550};
        int length20 = twenty.length;
        double[] forty = {14.77, 11.56, 9.794, 7.468, 6.200, 5.187, 4.382, 3.762, 3.249, 2.847, 2.497, 2.219, 1.982, 1.778, 1.608, 1.462, 1.334, 1.221, 1.123, 1.037, 0.960};
        int length40 = forty.length;
        double[] sixty = {238, 156, 109.8, 74.6, 56.5, 43.86, 33.78, 26.52, 21.28, 17.18, 14.01, 11.67, 9.83, 8.34, 7.15, 6.20, 5.40, 4.73, 4.15, 3.72, 3.34};
        int length60 = sixty.length;
        double[] temp = new double[length0];
        temp[0] = 0;
        for(int i=1; i<length60; i++)temp[i] = temp[i-1] + 5.0;
        ArrayMaths am0 = new ArrayMaths(zero);
        ArrayMaths am20 = new ArrayMaths(twenty);
        ArrayMaths am40 = new ArrayMaths(forty);
        ArrayMaths am60 = new ArrayMaths(sixty);
        ArrayMaths amT = new ArrayMaths(temp);

        double[] concn85 = {0.0, 216.2, 470.6, 771.9};
        double[] concn100 = {0.0, 470.6, 771.9};

        double ret = Double.NaN;

        if(gPerL<=771.9){
            if(temperature<=85){

                int lengthT85 = length20;
                int lengthC85 =  amC.indexOf(771.9);
                double[] temp85 = amT.subarray_as_double(0, length20-1);
                double[] conc85 = amC.subarray_as_double(0, lengthC85);

                lengthC85++;
                double[][] array = new double[lengthT85][lengthC85];
                for(int i=0; i<lengthT85; i++){
                    double[] lvisc = {Math.log(zero[i]), Math.log(twenty[i]), Math.log(forty[i]), Math.log(sixty[i])};
                    CubicSpline cs = new CubicSpline(concn85, lvisc);
                    for(int j=0; j<lengthC85; j++){
                        array[i][j] = cs.interpolate(conc85[j]);
                    }
                }
                BiCubicSpline bcs = new BiCubicSpline(temp85, conc85, array);
                ret = Math.exp(bcs.interpolate(temperature, gPerL));
            }
            else{
                if(temperature>100.0){
                    throw new IllegalArgumentException("Temperature, " + temperature + ", out of range");
                }
                else{
                    int lengthT100 = length60;
                    int lengthC100 =  amC.indexOf(771.9);
                    double[] temp100 = amT.subarray_as_double(0, lengthT100-1);
                    double[] conc100 = amC.subarray_as_double(0, lengthC100);
                    lengthC100++;

                    double[][] array = new double[lengthT100][lengthC100];
                    for(int i=0; i<lengthT100; i++){
                        double[] lvisc = {Math.log(zero[i]), Math.log(forty[i]), Math.log(sixty[i])};
                        CubicSpline cs = new CubicSpline(concn100, lvisc);
                        for(int j=0; j<lengthC100; j++){
                            array[i][j] = cs.interpolate(conc100[j]);

                        }
                    }
                    BiCubicSpline bcs = new BiCubicSpline(temp100, conc100, array);
                    ret = Math.exp(bcs.interpolate(temperature, gPerL));
                }
            }
        }
        else{
            if(temperature==20 && gPerL<=1208.2){
                double[] concM = amC.subarray_as_double(0, viscLength-1);
                CubicSpline cs = new CubicSpline(concM, visc20);
                ret = cs.interpolate(gPerL);
            }
            else{
                throw new IllegalArgumentException("Concentration, " + gPerL + " g/l,  for this temperature, " + temperature + " C,is out of range");
            }
        }

        return ret;
    }


    // Returns the refractive index of sucrose solutions as a function of g/l sucrose concentration
    // Wavelength - sodium D line 589.3 nm
    // Interpolation - natural cubic spline
    // Extrapolation above 1208.2g/l Lorenz-lorenz  equation based on
    //  average refraction of sucrose calculated from experimental data
    // Data -  Rubber Handbook
    // Uses method in class RefrIndex
    public static double refractIndex(double concentration, double temperature){
        return  RefrIndex.sucrose(concentration, temperature);
    }

    // Returns the solubility of sucrose in water as g/l solution as a function of temperature
    // Data - Rubber handbook
    // interpolation - cubic spline
    public static double solubility(double temperature){

    	double[]	tempc = {0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 70.0, 75.0, 80.0, 85.0, 90.0, 95.0, 100.0};
	    double[]	solub = {64.18, 64.87, 65.58, 66.53, 67.09, 67.89, 68.8, 69.55, 70.42, 71.32, 72.25, 73.2, 74.18, 75.88, 76.22, 77.27, 78.36, 79.46, 80.61, 81.77, 82.97};
	    double[]    deriv = {0, -0.00489357, 0.0243743, -0.0350036, 0.0220399, 0.00444386, -0.0134154, 0.0108176, -0.00105492, 0.000602123, 0.00584643, -0.0191879, 0.078105, -0.120432, 0.0772233, -0.0180611, 0.00462116, 0.00197648, -0.00052706, 0.00253177, 0};

	    double  solubility;

	    int		n=tempc.length;

	    if(temperature>=tempc[0] && temperature<=tempc[n-1]){
	        solubility = CubicSpline.interpolate(temperature, tempc, solub, deriv);
	    }
	    else{
	        throw new IllegalArgumentException("The temperatue is outside the experimental data limits");
	    }
	    solubility=weightpercentToGperl(solubility, temperature);

	    return solubility;
    }

    // Returns the density (kg/m^3) of sucrose solutions as a function of g/l sucrose concentration
    // Interpolation - natural cubic spline
    // Data -  Rubber Handbook
    // concentration g/l concentration of sucrose for which the density is required
    // temperature     temperature (degree Celsius)
    public static double density(double concentration, double temperature){

	    double[]    concnG = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
	    double[]	dens = {998.2, 1000.2, 1002.1, 1004, 1006, 1007.9, 1009.9, 1011.9, 1013.9, 1015.8, 1017.8, 1019.8, 1021.8, 1023.8, 1025.9, 1027.9, 1029.9, 1032, 1034, 1036.1, 1038.1, 1042.3, 1046.5, 1050.7, 1054.9, 1059.2, 1063.5, 1067.8, 1072.2, 1076.6, 1081, 1089.9, 1099, 1108.2, 1117.5, 1127, 1136.6, 1146.4, 1156.2, 1166.3, 1176.5, 1186.8, 1197.2, 1207.9, 1218.6, 1229.5, 1240.6, 1251.8, 1263.2, 1274.7, 1286.4, 1298.3, 1310.3, 1322.4, 1334.8, 1347.2, 1359.9, 1372.6, 1385.5, 1398.6, 1411.7, 1425, 1438.3};
	    double[]    deriv = {0, -0.00495764, -0.00416945, 0.0126089, -0.0137458, 0.00978735, -0.00233552, -0.000445268, -0.00464715, 0.00502912, -0.00144255, 0.000741061, -0.0015217, 0.00534573, -0.00654364, -0.0005311, 0.00866803, -0.0127811, 0.0134147, -0.0118716, 0.00515152, -0.0012962, 3.32699e-005, -0.000758744, 0.00112898, -0.000770477, 0.000145246, 0.000189494, 0.000189925, -0.000949193, 0.000223635, 6.85836e-005, 2.005e-005, -0.000287156, 0.000246234, -0.000222108, 0.000174076, -0.000361684, 0.000296689, -0.000183935, 4.13755e-005, -0.000362702, 0.000485174, -0.000459014, 4.51875e-005, 0.000142595, -0.00025712, 0.000125835, -0.000132083, -8.03786e-005, 0.000151752, -0.000212951, -0.000110921, 0.00024994, -0.000447536, 0.00033998, -0.00032882, 2.20206e-005, 5.04017e-005, -0.000253039, 0.000115168, -0.000224291, 0,};
	    double[]    concfit = {0, 200, 400, 600, 800, 1000, 1200};
	    double[]    coeff0 = {0.000998363, 0.000926078, 0.000864741, 0.000812717, 0.000766724, 0.000727182, 0.000692587};
	    double[]    deriv0 = {0, 3.53118e-010, 2.29729e-010, 1.24915e-010, 1.75259e-010, 1.41698e-010, 0};
	    double[]    coeff1 = {2.42825e-007, 2.60851e-007, 2.61522e-007, 1.52504e-007, 2.35274e-007, 2.1465e-007, 1.91079e-007};
	    double[]    deriv1 = {0, 1.11593e-012, -7.06697e-012, 1.06986e-011, -6.95925e-012, 1.6293e-012, 0};
	    double  density, coefficient0, coefficient1;
	    int	n=concfit.length;
		if(concentration>=concfit[0] && concentration<=concfit[n-1] && temperature>=0 && temperature<=50){
		        coefficient0 = CubicSpline.interpolate(concentration, concfit, coeff0, deriv0);
			    coefficient1 = CubicSpline.interpolate(concentration, concfit, coeff1, deriv1);

			    density=1.0/(coefficient0+coefficient1*temperature);
		}
		else{
		        throw new IllegalArgumentException("Either Temperature or Concentration is outside the experimental data limits");
		}
	    return density;
    }

    // Returns the specific volume (m^3/kg) of sucrose in aqueous solution at 20 C
    //  as a function of g/l sucrose concentration
    // Average value = 0.000621903 sd = 6.16153e-006  This value is used outside the
    //  limits of the experimental concentration and temperature data
    // Interpolation - natural cubic spline
    // Data - calculated from density of water and displaced water volume in the Rubber Handbook
    public static double specificVolume(double concentration){

	    double[]	concnG = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
	    double[]	specv = {0.000621118, 0.000621118, 0.000621118, 0.000617005, 0.000618028, 0.000616189, 0.000614968, 0.00061693, 0.000614406, 0.000615988, 0.00061604, 0.000616082, 0.000616117, 0.000616147, 0.000616709, 0.000615895, 0.000616401, 0.000616846, 0.000616577, 0.000616964, 0.000616717, 0.000616629, 0.000617353, 0.00061751, 0.000617225, 0.000617222, 0.000617445, 0.000618193, 0.000618211, 0.000618228, 0.000618597, 0.000618712, 0.000619007, 0.000619651, 0.000619844, 0.000620164, 0.000620584, 0.000620923, 0.000621494, 0.000621832, 0.000622455, 0.000622911, 0.00062337, 0.000623873, 0.000624478, 0.000624905, 0.00062537, 0.000625979, 0.000626516, 0.000627126, 0.000627766, 0.000628414, 0.000628964, 0.000629685, 0.000630377, 0.00063108, 0.000631819, 0.000632624, 0.000633334, 0.000634105, 0.000635019, 0.00063589, 0.000636886};
	    double[]    deriv = {0, 9.54239e-008, -3.81696e-007, 4.69472e-007, -2.94053e-007, 3.9486e-008, 2.78669e-007, -4.19907e-007, 3.86217e-007, -1.95897e-007, 5.08841e-008, -9.858e-009, -1.30053e-008, 6.07699e-008, -1.14515e-007, 1.03377e-007, -1.70413e-008, -4.82412e-008, 5.92625e-008, -5.10779e-008, 1.22998e-008, 9.25257e-009, -8.30337e-009, -4.55102e-009, 4.7569e-009, -8.20925e-010, 9.1751e-009, -1.12103e-008, 1.71621e-009, 4.30015e-009, -3.32497e-009, 1.03549e-009, 1.01682e-009, -1.55614e-009, 6.3652e-010, 1.76082e-010, -4.56375e-010, 8.49619e-010, -9.64276e-010, 9.59006e-010, -6.2015e-010, 1.50421e-010, -3.91772e-011, 2.78811e-010, -4.15371e-010, 9.3832e-011, 2.48046e-010, -2.27533e-010, 1.60667e-010, -3.36646e-011, 6.9795e-011, -2.4342e-010, 3.04857e-010, -1.35319e-010, 3.06608e-011, -2.68355e-012, 1.01252e-010, -1.58595e-010, 5.29862e-011, 1.52913e-010, -1.29603e-010, 1.39208e-010, 0};
	    double  specvol, average=0.000621903;
	    int		n=concnG.length;

	    if(concentration>=concnG[0] && concentration<=concnG[n-1]){
		    specvol = CubicSpline.interpolate(concentration, concnG, specv, deriv);
	    }
	    else{
		    specvol=average;
	    }
        return specvol;
    }


    // Returns the diffusion coefficient (m^2 s^-1) of sucrose in aqueous solution
    // as a function of the g/l sucrose concentration
    // concentration	g/l concentration of sucrose for which a diffusion coefficient is required
    // temperature      temperature in degree Celsius

    public static double diffCoeff(double concentration, double temperature){

	    double diffcoef, f, viscosity, specvol, vol, radius, tempa;

	    tempa=temperature - Fmath.T_ABS;

	    viscosity=Sucrose.viscosity(concentration, temperature);
	    specvol=Sucrose.specificVolume(concentration);
	    vol=Sucrose.MOLWEIGHT*specvol/(Fmath.N_AVAGADRO*1000);
	    radius=Math.pow(3.0*vol/(4.0*Math.PI),1.0/3.0);

	    f=6.0*Math.PI*viscosity*radius;

	    diffcoef=Fmath.K_BOLTZMANN*tempa/f;

	    return diffcoef;
    }

    // Returns the mole fraction of sucrose in an aqueous sucrose solution
    // for a given sucrose concentration (g/l) and temperature
    public static double moleFraction(double concentration, double temperature){
        double weightSucrose, totalWeight, molesWater, molesSucrose;
         weightSucrose=concentration*1000;
         molesSucrose=weightSucrose/Sucrose.MOLWEIGHT;
         totalWeight=Sucrose.density(concentration, temperature)*1000.0;
         molesWater=(totalWeight-weightSucrose)/Water.MOLWEIGHT;
         return molesSucrose/(molesWater + molesSucrose);
    }

    // Converts Molar sucrose to g/l sucrose
    public static double molarToGperl(double molar){
        return  molar*Sucrose.MOLWEIGHT;
    }

    // Converts Molar sucrose to weight per cent sucrose
    public static double molarToWeightpercent(double molar, double temperature){
        double weight;
        weight = Sucrose.molarToGperl(molar);
        weight = Sucrose.gperlToWeightpercent(weight, temperature);
        return  weight;
    }

    // Converts g/l sucrose to Molar sucrose
    public static double gperlToMolar(double gperl){
        return  gperl/Sucrose.MOLWEIGHT;
    }

    // Converts a g/l aqueous sucrose concentration to the % weight concentration
    //  (g sucrose / 100g solution)at a gven temperature (Celsius)
    // Interpolation - cubic spline (20C) and calculation from density
    // Data -  Rubber Handbook
    public static double gperlToWeightpercent(double concentration, double temperature){

	    double[]	concnG = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
	    double[]	concnW = {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84};
	    double[]    deriv = {0, 0.000224325, -0.0008973, 0.00102728, -0.000882025, 0.00017957, 0.000163747, -0.000834558, 0.000970111, -0.000849482, 0.000238375, -0.000104019, 0.000177701, -0.000606786, 0.000176058, -9.74476e-005, 0.000213732, -0.00075748, 0.000850948, -0.000686359, -5.6318e-005, 4.08684e-005, -0.000107156, -6.87474e-005, -6.27622e-005, -0.000112854, 9.20828e-005, -0.000255477, 0.000119556, -0.000222749, 3.59124e-006, -8.44627e-005, -2.16033e-005, -8.47265e-005, -4.4802e-005, -4.36141e-005, -7.34483e-005, -1.02994e-005, -8.43266e-005, -3.04826e-005, -3.26613e-005, -6.69242e-005, -2.42674e-005, -4.13785e-005, -5.47191e-005, -1.76386e-005, -5.07722e-005, -3.04569e-005, -2.61186e-005, -5.41411e-005, -7.97549e-006, -5.00124e-005, -1.83509e-005, -3.00707e-005, -3.65588e-005, -1.72617e-005, -2.57937e-005, -2.9995e-005, -2.0797e-005, -2.23338e-005, -1.90539e-005, -2.47041e-005, 0};
	    double  weight;

	    weight=concentration*0.1/(Sucrose.density(concentration, temperature)/1000);

        return weight;
    }

    // Converts weight percent sucrose to Molar sucrose
    public static double weightpercentToMolar(double weight, double temperature){
        double molar;
        molar = weightpercentToGperl(weight, temperature);
        molar = gperlToMolar(molar);
        return  molar;
    }

    // Converts the % weight concentration (g sucrose / 100g solution)
    //  to a g/l aqueous sucrose concentration
    // interpolation - natural cubic spline (20C) and calculation
    // Data -  Rubber Handbook
    // concentration    % weight concentration of sucrose
    // temperature      temperature (degrees Celsius)
    public static double weightpercentToGperl(double concentration, double temperature){
        double[]    concnW = {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5, 8, 8.5, 9, 9.5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84};
	    double[]	concnG = {0, 5, 10, 15.1, 20.1, 25.2, 30.3, 35.4, 40.6, 45.7, 50.9, 56.1, 61.3, 66.5, 71.8, 77.1, 82.4, 87.7, 93.1, 98.4, 103.8, 114.7, 125.6, 136.6, 147.7, 158.9, 170.2, 181.5, 193, 204.5, 216.2, 239.8, 263.8, 288.1, 312.9, 338.1, 363.7, 389.8, 416.2, 443.2, 470.6, 498.4, 526.8, 555.6, 584.9, 614.8, 645.1, 676, 707.4, 739.3, 771.9, 804.9, 838.6, 872.8, 907.6, 943.1, 979.1, 1015.7, 1053, 1090.9, 1129.4, 1168.5, 1208.2};
	    double[]    deriv = {0, -0.230536, 0.922143, -1.05804, 0.909999, -0.181959, -0.182163, 0.910611, -1.06028, 0.930517, -0.261788, 0.116633, -0.204746, 0.702351, -0.204657, 0.116276, -0.260447, 0.925512, -1.0416, 0.840888, 0.078047, -0.0545852, 0.140294, 0.0934103, 0.0860649, 0.16233, -0.135385, 0.37921, -0.181457, 0.346616, -0.00500719, 0.141714, 0.0381529, 0.155675, 0.0891471, 0.0877366, 0.159906, 0.0226375, 0.199544, 0.0791879, 0.0837046, 0.185994, 0.0723209, 0.124723, 0.178788, 0.0601258, 0.180709, 0.117039, 0.101137, 0.228414, 0.0352078, 0.230755, 0.0917725, 0.152155, 0.199608, 0.0994148, 0.152733, 0.189652, 0.138658, 0.155717, 0.138476, 0.190381, 0};
	    double  gperl=0.0, f1, f2, f3, g1, g2, g3, tol;
	    int n=concnW.length, j=-1, itermax=1000;
        boolean test=true;

	    if(concentration<concnW[0] || concentration>concnW[n-1]){
	        throw new IllegalArgumentException("concentration is outside the experimental data limits");
	    }

		// obtains g/l by bisection using g/l=weight%*density
		tol=concentration*1e-4;
		g1=0.0;
		g2=1200;
		f1=g1-concentration*Sucrose.density(g1,temperature)/100;
		f2=g2-concentration*Sucrose.density(g2,temperature)/100;
		if ((f1 > 0.0 && f2 > 0.0) || (f1 < 0.0 && f2 < 0.0)){
		        throw new IllegalArgumentException("Root must be bracketed in the bisection");
	    }
		test=true;
		while(test){
			    if(f1==0.0){
				    gperl= g1;
				    test=false;
			    }
			    if(f2==0.0){
				    gperl=g2;
				    test=false;
			    }
			    if(test){
				    g3=0.5*(g1+g2);
				    f3=g3-concentration*Sucrose.density(g3,temperature)/100;
				    if(f3==0.0){
					    gperl=g3;
					    test=false;
				    }
				    else{
					    if(f3*f1>0){
						    g1=g3;
					    }
					    else{
						    g2=g3;
					    }
					    f1=g1-concentration*Sucrose.density(g1,temperature)/100;
					    f2=g2-concentration*Sucrose.density(g2,temperature)/100;
					    if(Math.abs(g1-g2)<tol){
						    gperl=0.5*(g1+g2);
						    test=false;
					    }
				    }
				    j++;
				    if(j>itermax){
					    throw new IllegalArgumentException("number of iteractions in bisection exceeded");
				    }
			    }
		}

	    return gperl;
    }
}
