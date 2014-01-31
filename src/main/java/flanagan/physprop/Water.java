/*
*   Water Class
*
*   Methods for returning the physical properties of water:
*   viscosity, density, refractive index,
*   electrical permittivity, molecular weight.
*
*   Author:  Dr Michael Thomas Flanagan.
*
*   Created: July 2003
*   Updated: 1 July 2003 and 6 May 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Water.html
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
import flanagan.complex.Complex;

public class Water{

    public static final double MOLWEIGHT = 18.02;

    // METHODS
    // Returns the viscosity (Pa s) of water at a given temperature (Celsius)
    // Interpolation - natural cubic spline
    // Data - Rubber Handbook
    public static double viscosity(double temperature){

	    double[] tempc = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
	    double[] visc  = {0.0017921, 0.0017313, 0.0016728, 0.0016191, 0.0015674, 0.0015188, 0.0014728, 0.0014284, 0.001386, 0.0013462, 0.0013077, 0.0012713, 0.0012363, 0.0012028, 0.0011709, 0.0011404, 0.0011111, 0.0010828, 0.0010559, 0.0010299, 0.001005, 0.000981, 0.0009579, 0.0009358, 0.0009142, 0.0008937, 0.0008737, 0.0008545, 0.000836, 0.000818, 0.0008007, 0.000784, 0.0007679, 0.0007523, 0.0007371, 0.0007225, 0.0007085, 0.0006947, 0.0006814, 0.0006685, 0.000656, 0.0006439, 0.0006321, 0.0006207, 0.0006097, 0.0005988, 0.0005883, 0.0005782, 0.0005683, 0.0005588, 0.0005494, 0.0005404, 0.0005315, 0.0005229, 0.0005146, 0.0005064, 0.0004985, 0.0004907, 0.0004832, 0.0004759, 0.0004688, 0.0004618, 0.000455, 0.0004483, 0.0004418, 0.0004355, 0.0004293, 0.0004233, 0.0004174, 0.0004117, 0.0004061, 0.0004006, 0.0003952, 0.00039, 0.0003849, 0.0003799, 0.000375, 0.0003702, 0.0003655, 0.000361, 0.0003565, 0.0003521, 0.0003478, 0.0003436, 0.0003395, 0.0003355, 0.0003315, 0.0003276, 0.0003239, 0.0003202, 0.0003165, 0.000313, 0.0003095, 0.000306, 0.0003027, 0.0002994, 0.0002962, 0.000293, 0.0002899, 0.0002868, 0.0002838};
	    double[] deriv  = {0, 1.78373e-006, 6.66507e-006, 3.55981e-007, 3.911e-006, 2.60001e-006, 1.28896e-006, 1.84413e-006, 3.3345e-006, 4.1785e-007, 2.7941e-006, 1.00576e-006, 1.58285e-006, 1.66282e-006, 1.36586e-006, 1.27374e-006, 7.39187e-007, 1.76951e-006, 5.82755e-007, 1.29947e-006, 8.1938e-007, 8.23013e-007, 1.28857e-006, 2.27218e-008, 1.62055e-006, 9.50918e-008, 9.99086e-007, 7.08563e-007, 3.66662e-007, 8.24789e-007, 5.34182e-007, 6.38482e-007, 5.1189e-007, 3.13959e-007, 6.32274e-007, 7.56944e-007, -6.00505e-008, 6.83258e-007, 3.27018e-007, 4.08669e-007, 4.38304e-007, 2.38113e-007, 4.09244e-007, 5.24909e-007, -1.08882e-007, 5.10619e-007, 4.66404e-007, 2.3763e-008, 6.38544e-007, -1.77938e-007, 6.73209e-007, -1.14896e-007, 3.86377e-007, 3.69389e-007, -6.39346e-008, 4.86349e-007, -8.14616e-008, 4.39497e-007, 1.23473e-007, 2.66612e-007, 1.00779e-008, 2.93076e-007, 1.76187e-008, 2.36449e-007, 2.36584e-007, 1.72156e-008, 2.94554e-007, 4.56925e-009, 2.87169e-007, 4.67538e-008, 1.25815e-007, 4.99845e-008, 2.74247e-007, 5.30292e-008, 1.13637e-007, 9.24242e-008, 1.16667e-007, 4.09095e-008, 3.19695e-007, -1.19691e-007, 1.59069e-007, 8.34135e-008, 1.07277e-007, 8.74801e-008, 1.42803e-007, -5.86918e-008, 9.19641e-008, 2.90835e-007, -5.53049e-008, -6.96157e-008, 3.33768e-007, -6.54552e-008, -7.19471e-008, 3.53244e-007, -1.41027e-007, 2.10865e-007, -1.02433e-007, 1.98866e-007, -9.30309e-008, 1.73258e-007, 0};
	    double viscosity;
        int n = tempc.length;

	    if(temperature>=tempc[0] && temperature<=tempc[n-1]){
            viscosity=CubicSpline.interpolate(temperature, tempc, visc, deriv);
	    }
	    else{
			throw new IllegalArgumentException("Temperature outside the experimental data limits");
	    }

	    return viscosity;
    }

    // Returns the density (kg m^-3) of water at a given temperature (Celsius)
    // Interpolation - natural cubic spline
    // Data - Rubber Handbook
    public static double density(double temperature){

	    double[] tempc = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
	    double[] dens  = {999.87, 999.93, 999.97, 999.99, 1000, 999.99, 999.97, 999.93, 999.88, 999.81, 999.73, 999.63, 999.52, 999.4, 999.27, 999.13, 998.97, 998.8, 998.62, 998.43, 998.23, 998.02, 997.8, 997.56, 997.32, 997.07, 996.81, 996.54, 996.26, 995.07, 995.67, 995.37, 995.05, 994.73, 994.4, 994.06, 993.71, 993.36, 992.99, 992.62, 992.24, 991.86, 991.47, 991.07, 990.66, 990.25, 989.82, 989.4, 988.96, 988.52, 988.07, 987.62, 987.15, 986.69, 986.21, 985.73, 983.24, 980.59, 977.81, 974.89, 971.83, 968.65, 965.34, 961.92, 958.38};
	    double[] deriv  = {0, -0.0241154, -0.0235383, -0.00173154, -0.0295356, -0.000126193, -0.0299597, -3.51591e-005, -0.0298997, -0.000366034, -0.0286362, -0.00508932, -0.0110066, -0.0108844, -0.00545578, -0.0272925, -0.00537436, -0.0112101, -0.00978527, -0.00964883, -0.0116194, -0.00387347, -0.0328867, 0.0154203, -0.0287945, 0.0397577, -0.190236, 0.661187, -2.51451, 3.93686, -2.49294, 0.634886, -0.166608, 0.0315471, -0.0195801, -0.0132268, 0.0124871, -0.0367217, 0.0143998, -0.0208776, 0.00911065, -0.015565, -0.0068508, -0.0170318, 0.0149782, -0.0428809, 0.0365453, -0.0433004, 0.0166564, -0.0233251, 0.0166439, -0.0432504, 0.0363578, -0.0421807, 0.0123651, -0.00727961, -0.00660195, -0.0047126, -0.00574764, -0.00589685, -0.00426496, -0.00584331, -0.00356178, -0.00630955, 0};
	    double density;
        int n = tempc.length;

	    if(temperature>=tempc[0] && temperature<=tempc[n-1]){
            density=CubicSpline.interpolate(temperature, tempc, dens, deriv);
	    }
	    else{
			throw new IllegalArgumentException("Temperature outside the experimental data limits");
	    }
	    return density;
    }

	// Calculates and returns the static relative electrical permittivity
	// (dielectric constant) of water as a function of temperature.
    // Data: CRC Handbook of Chemistry and Physics 56th edition Page E55
    public static double elecPerm(double tempc){
	    double	temp = tempc-25.0;

        return 78.54*(1.0-4.579e-3*temp + 1.19e-5*temp*temp - 2.8e-8*temp*temp*temp);
    }

    // Returns the refractive index  of water at a given wavelength (metres) and temperature (Celsius)
    // Interpolation - natural bicubic spline
    // Uses method in class RefrIndex
    // Data - Rubber Handbook
    public static double refractIndex(double wavelength, double temperature){
        return  RefrIndex.water(wavelength, temperature);
    }
}