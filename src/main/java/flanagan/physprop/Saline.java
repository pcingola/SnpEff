/*
*   Saline Class
*
*   Methods for returning the physical properties of
*   aqueous sodium chloride solutions:
*   viscosity, density, refractive index
*   mole fraction and molecular weight.
*   Methods for interconverting molar - grams per litre.
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: July 2003
*   Updated: 1 July 2003 and 5 May 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Saline.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) May 2004    Michael Thomas Flanagan
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
import flanagan.interpolation.CubicSpline;

public class Saline{

    public static final double MOLWEIGHT = 58.45;  // NaCl molecular weight

    // METHODS
    // Returns the viscosity (Pa s) of saline at a given Molar NaCl concentration and
    // at a given temperature (Celsius)
    // Interpolation - natural cubic spline plus activation energy scaling
    // Data - Rubber Handbook
    public static double viscosity(double concentration, double temperature){

	    double[] conc = {0, 0.017, 0.034, 0.051, 0.069, 0.086, 0.103, 0.12, 0.137, 0.155, 0.172, 0.189, 0.207, 0.224, 0.241, 0.259, 0.276, 0.294, 0.311, 0.329, 0.346, 0.364, 0.382, 0.399, 0.418, 0.435, 0.452, 0.47, 0.488, 0.505, 0.523, 0.541, 0.559, 0.577, 0.595, 0.653, 0.631, 0.649, 0.667, 0.685, 0.703, 0.721, 0.739, 0.757, 0.775, 0.794, 0.812, 0.83, 0.848, 0.866, 0.885, 0.921, 0.958, 0.995, 1.032, 1.069, 1.106, 1.144, 1.181, 1.218, 1.256, 1.294, 1.331, 1.369, 1.407, 1.445, 1.484, 1.522, 1.56, 1.599, 1.637, 1.676, 1.715, 1.754, 1.193, 1.832, 1.93, 2.029, 2.129, 2.229, 2.33, 2.432, 2.534, 2.637, 2.741, 2.845, 3.056, 3.27, 3.486, 3.706, 3.928, 4.153, 4.382, 4.613, 4.848, 5.085, 5.326};
	    double[] visc = {0.00100219, 0.00100389, 0.0010057, 0.0010074, 0.00100911, 0.00101082, 0.00101353, 0.00101524, 0.00101695, 0.00101866, 0.00102038, 0.0010221, 0.00102392, 0.00102463, 0.00102635, 0.00102807, 0.00102979, 0.00103152, 0.00103324, 0.00103396, 0.00103579, 0.00103752, 0.00103925, 0.00104098, 0.00104271, 0.00104445, 0.00104527, 0.00104701, 0.00104875, 0.00105049, 0.00105223, 0.00105397, 0.0010548, 0.00105654, 0.00105829, 0.00106004, 0.00106178, 0.00106251, 0.00106437, 0.00106653, 0.00106787, 0.00106963, 0.00107138, 0.00107325, 0.00107501, 0.00107677, 0.00107853, 0.00107926, 0.00108113, 0.0010829, 0.00108467, 0.00108935, 0.00109289, 0.00109655, 0.00110011, 0.00110378, 0.00110735, 0.00111207, 0.00111566, 0.00111935, 0.0011241, 0.0011277, 0.00113247, 0.00113619, 0.00114086, 0.0011446, 0.00114939, 0.00115409, 0.00115891, 0.00116373, 0.00116857, 0.0011733, 0.00117815, 0.00118301, 0.00118788, 0.00119383, 0.00120655, 0.00122041, 0.00123553, 0.00125073, 0.00126588, 0.00128232, 0.00129885, 0.00131656, 0.00133337, 0.00135126, 0.00138744, 0.00142411, 0.00146355, 0.00150682, 0.00155756, 0.0016135, 0.00167732, 0.00174526, 0.00182018, 0.00190192, 0.00198975};
        double[] deriv = {0, 0.000768695, -0.000791042, 0.000111737, -0.00135411, 0.00742974, -0.0076036, 0.0022234, -0.00129002, 0.00105411, -0.000794614, 0.00212435, -0.00753267, 0.00782177, -0.00278555, 0.00157181, -0.00153892, 0.00281175, -0.00816056, 0.00868783, -0.0032574, 0.000617161, 0.000788751, -0.00190588, 0.00313379, -0.00715407, 0.00638216, -0.00191908, 0.00129416, -0.00129001, 0.00189902, -0.00630606, 0.00647338, -0.0027356, 0.0046542, -0.0182844, -0.0177707, 0.00963645, 0.000150791, -0.00468406, 0.00340026, -0.0011392, 0.000971365, -0.000524035, -0.000912263, 0.00242436, -0.00728848, 0.00765548, -0.00222235, -0.000617948, 0.00287778, -0.00232672, 0.000815016, -0.000407408, 0.000376341, -0.000615852, 0.00164879, -0.00153126, 0.000106328, 0.00154422, -0.00220911, 0.00251382, -0.00237932, 0.00204582, -0.00185658, 0.00151625, -0.00042451, 0.000300603, -0.000279286, 0.000309564, -0.00037128, 0.000228089, -6.77046e-005, 8.21768e-005, 0.00126799, -6.84692e-005, 0.000138594, 0.000134632, -1.04198e-006, -8.24638e-005, 0.000210442, -9.85469e-005, 0.000235649, -0.000264725, 0.000225783, -3.92966e-005, -9.59633e-006, 7.36642e-005, 2.83662e-005, 0.000199494, 3.90432e-005, 0.000183066, 2.35719e-005, 0.000125227, 0.000110721, 9.52208e-005, 0};
	    double viscWaterT, viscWater20, viscosity;
        int n = conc.length;

        // Calculate viscosity at 20 C
	    if(concentration>=conc[0] && concentration<=conc[n-1]){
            viscosity=CubicSpline.interpolate(concentration, conc, visc, deriv);
	    }
	    else{
			throw new IllegalArgumentException("concentration outside the experimental data limits");
	    }

	    if(temperature!=20.0){
	        // Scale temperature dependence to that of water,
	        // i.e. assume same activation energy
		    viscWater20 = Water.viscosity(20.0);
		    viscWaterT = Water.viscosity(temperature);
		    viscosity=viscosity*viscWaterT/viscWater20;
	    }
	    return viscosity;
    }

    // Returns the density (kg/m^3) of NaCl solutions as a function of M NaCl concentration
    // Interpolation - natural cubic spline
    // Data -  Rubber Handbook
    // Temp = 20C
    public static double density(double concentration){

	    double[] conc = {0, 0.017, 0.034, 0.051, 0.069, 0.086, 0.103, 0.12, 0.137, 0.155, 0.172, 0.189, 0.207, 0.224, 0.241, 0.259, 0.276, 0.294, 0.311, 0.329, 0.346, 0.364, 0.382, 0.399, 0.418, 0.435, 0.452, 0.47, 0.488, 0.505, 0.523, 0.541, 0.559, 0.577, 0.595, 0.653, 0.631, 0.649, 0.667, 0.685, 0.703, 0.721, 0.739, 0.757, 0.775, 0.794, 0.812, 0.83, 0.848, 0.866, 0.885, 0.921, 0.958, 0.995, 1.032, 1.069, 1.106, 1.144, 1.181, 1.218, 1.256, 1.294, 1.331, 1.369, 1.407, 1.445, 1.484, 1.522, 1.56, 1.599, 1.637, 1.676, 1.715, 1.754, 1.193, 1.832, 1.93, 2.029, 2.129, 2.229, 2.33, 2.432, 2.534, 2.637, 2.741, 2.845, 3.056, 3.27, 3.486, 3.706, 3.928, 4.153, 4.382, 4.613, 4.848, 5.085, 5.326};
	    double[] dens = {998.2, 998.9, 999.7, 1000.4, 1001.1, 1001.8, 1002.5, 1003.2, 1003.9, 1004.6, 1005.3, 1006, 1006.8, 1007.5, 1008.2, 1008.9, 1009.6, 1010.3, 1011, 1011.7, 1012.5, 1013.2, 1013.9, 1014.6, 1015.3, 1016, 1016.8, 1017.5, 1018.2, 1018.9, 1019.6, 1020.3, 1021.1, 1021.8, 1022.5, 1023.2, 1023.9, 1024.6, 1025.4, 1026.5, 1026.8, 1027.5, 1028.2, 1029, 1029.7, 1030.4, 1031.1, 1031.8, 1032.6, 1033.3, 1034, 1035.5, 1036.9, 1038.4, 1039.8, 1041.3, 1042.7, 1044.2, 1045.6, 1047.1, 1048.6, 1050, 1051.5, 1053, 1054.4, 1055.9, 1057.4, 1058.8, 1060.3, 1061.8, 1063.3, 1064.7, 1066.2, 1067.7, 1069.2, 1070.7, 1074.4, 1078.1, 1081.9, 1085.7, 1089.4, 1093.2, 1097, 1100.8, 1104.7, 1108.5, 1116.2, 1124, 1131.9, 1139.8, 1147.8, 1155.8, 1164, 1172.1, 1180.4, 1188.7, 1197.2};
        double[] deriv = {0, 685.75, -666.876, -94.3722, 234.303, -57.4711, -4.41831, 75.1444, -296.159, 318.233, -189.411, 439.409, -440.601, 195.58, -341.719, 381.664, -402.356, 441.731, -585.487, 1097.17, -1014.34, 185.143, 273.774, -515.957, 341.504, 660.063, -905.631, 175.193, 204.858, -221.651, -94.0302, 597.771, -445.203, -668.811, 3120.45, -10744.6, -14968.4, 3784.07, 1683.98, -4964.43, 3358.94, -1063.92, 896.753, -671.238, -63.6541, 237.473, -226.825, 669.828, -600.633, -119.146, 386.711, -314.64, 244.402, -224.691, 216.087, -201.381, 151.163, -142.321, 156.457, -45.2313, -142.246, 198.704, -59.7198, -126.19, 148.967, -54.1662, -86.975, 152.369, -106.99, 118.297, -209.797, 162.973, -47.6169, 27.4948, 385.473, -22.8199, -2.39438, 9.00548, 4.10437, -25.4229, 15.9561, -1.79451, -8.77808, 15.6492, -18.5946, 3.25592, -1.85219, 2.90469, -6.24447, 3.76304, -5.36519, 4.79201, -7.11856, 4.3037, -3.58034, 2.44792, 0};
        double density;
        int n = conc.length;

	    if(concentration>=conc[0] && concentration<=conc[n-1]){
            density=CubicSpline.interpolate(concentration, conc, dens, deriv);
	    }
	    else{
			throw new IllegalArgumentException("concentration outside the experimental data limits");
	    }
	    return density;
    }

    // Returns the refractive index of a NaCl solution for a given wavelength (in metres),
    // a given temperature (degrees Celcius) and a given NaCl concentration (M)
    // Interpolation - natural bicubic spline
    // Uses method in class RefrIndex
    // Data - Rubber Handbook
    public static double refractIndex(double concentration, double wavelength, double temperature){
        return  RefrIndex.saline(concentration, wavelength, temperature);
    }

    // Returns the mole fraction at 20 C of NaCl in an aqueous NaCl solution for a given NaCl concentration (M)
    public static double moleFraction(double concentration){
        double molesNacl, totalWeight, molesWater;
         molesNacl=concentration*1000;
         totalWeight=Saline.density(concentration)*1000.0;
         molesWater=(totalWeight-molesNacl*Saline.MOLWEIGHT)/Water.MOLWEIGHT;
         return molesNacl/(molesWater + molesNacl);
    }

    // Converts Molar NaCl to g/l NaCl
    public static double molarToGperl(double molar){
        return  molar*Saline.MOLWEIGHT;
    }

    // Converts g/l NaCl to Molar NaCl
    public static double gperlToMolar(double gperl){
        return  gperl/Saline.MOLWEIGHT;
    }
}