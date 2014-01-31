/*
*   Class   IonicRadii
*
*   Methods for returning the non-hydrated ionic radius
*   or the hydrated ionic radius of an entered ion
*
*   Bare radii taken from:
*   Indiana University Molecular Structure Center, Retrieved on 1st November 2004 from the World Wide Web:
*   http://www.iumsc.indiana.edu/radii.html
*   Their source is:
*   Shannon,R.D. (1976) `Revised effective ionic radii in halides and chalcogenides', Acta Cryst. A32, 751.
*
*   WRITTEN BY: Michael Thomas Flanagan
*
*   DATE:       November 2004
*   UPDATE:     22 May 2005,  11 December 2007
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's JAVA library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/IonicRadii.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2004 - 2008 Michael Thomas Flanagan
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

import javax.swing.*;

public class IonicRadii{

    // ion symbols for non-hydrated ion radii
    private static String[] ions1 = {"Ag+", "Al+++", "Au+", "Au+++", "Ba++", "Be++", "Bi+++", "Ca++", "Cd++", "Ce+++", "Ce++++", "Co++ ls", "Co++ hs", "Co+++ ls", "Co+++ hs", "Cr++ ls", "Cr++ hs", "Cr+++", "Cs+", "Cu+", "Cu++", "Dy+++", "Er+++", "Eu++", "Eu+++", "Fe++ ls", "Fe++ hs", "Fe+++ ls", "Fe+++ hs", "Ga+++", "Gd+++", "Hf++++", "Hg+", "Hg++", "Ho+++", "In+++", "Ir+++", "K+", "La+++", "Li+", "Lu+++", "Mg++", "Mn++ ls", "Mn++ hs", "Mn+++ ls", "Mn+++ hs", "Mo+++", "Na+", "Nb+++", "Nd+++", "Ni++", "Pb++", "Pd++", "Pm+++", "Pr+++", "Pt++", "Rb+", "Rh+++", "Ru+++", "Sb+++", "Sc+++", "Sm+++", "Sr++", "Ta+++", "Tb+++", "Th++++", "Ti++", "Ti+++", "Ti++++", "Tl+", "Tl+++", "Tm+++", "U+++", "U++++", "V++", "V+++", "Y+++", "Yb++", "Yb+++", "Zn++", "Zr++++", "Br-",  "Cl-", "F-", "H-", "I-", "O--", "S--", "Se--", "Te--", "OH-"};
    private static String[] ions2 = {"Ag1+", "Al3+", "Au1+", "Au3+", "Ba2+", "Be2+", "Bi3+", "Ca2+", "Cd2+", "Ce3+", "Ce4+", "Co2+ ls", "Co2+ hs", "Co3+ ls", "Co3+ hs", "Cr2+ ls", "Cr2+ hs", "Cr3+", "Cs1+", "Cu1+", "Cu2+", "Dy3+", "Er3+", "Eu2+", "Eu3+", "Fe2+ ls", "Fe2+ hs", "Fe3+ ls", "Fe3+ hs", "Ga3+", "Gd3+", "Hf4+", "Hg1+", "Hg2+", "Ho3+", "In3+", "Ir3+", "K1+", "La3+", "Li1+", "Lu3+", "Mg2+", "Mn2+ ls", "Mn2+ hs", "Mn3+ ls", "Mn3+ hs", "Mo3+", "Na1+", "Nb3+", "Nd3+", "Ni2+", "Pb2+", "Pd2+", "Pm3+", "Pr3+", "Pt2+", "Rb1+", "Rh3+", "Ru3+", "Sb3+", "Sc3+", "Sm3+", "Sr2+", "Ta3+", "Tb3+", "Th4+", "Ti2+", "Ti3+", "Ti4+", "Tl1+", "Tl3+", "Tm3+", "U3+", "U4+", "V2+", "V3+", "Y3+", "Yb2+", "Yb3+", "Zn2+", "Zr4+", "Br1-",  "Cl1-", "F1-", "H1-", "I1-", "O2-", "S2-", "Se2-", "Te2-", "OH1-"};
    private static String[] ions3 = {"Ag+1", "Al+3", "Au+1", "Au+3", "Ba+2", "Be+2", "Bi+3", "Ca+2", "Cd+2", "Ce+3", "Ce+4", "Co+2 ls", "Co+2 hs", "Co+3 ls", "Co+3 hs", "Cr+2 ls", "Cr+2 hs", "Cr+3", "Cs+1", "Cu+1", "Cu+2", "Dy+3", "Er+3", "Eu+2", "Eu+3", "Fe+2 ls", "Fe+2 hs", "Fe+3 ls", "Fe+3 hs", "Ga+3", "Gd+3", "Hf+4", "Hg+1", "Hg+2", "Ho+3", "In+3", "Ir+3", "K+1", "La+3", "Li+1", "Lu+3", "Mg+2", "Mn+2 ls", "Mn+2 hs", "Mn+3 ls", "Mn+3 hs", "Mo+3", "Na+1", "Nb+3", "Nd+3", "Ni+2", "Pb+2", "Pd+2", "Pm+3", "Pr+3", "Pt+2", "Rb+1", "Rh+3", "Ru+3", "Sb+3", "Sc+3", "Sm+3", "Sr+2", "Ta+3", "Tb+3", "Th+4", "Ti+2", "Ti+3", "Ti+4", "Tl+1", "Tl+3", "Tm+3", "U+3", "U+4", "V+2", "V+3", "Y+3", "Yb+2", "Yb+3", "Zn+2", "Zr+4", "Br-1",  "Cl-1", "F-1", "H-1", "I-1", "O-2", "S-2", "Se-2", "Te-2", "OH-1"};
    private static String[] ions4 = {"Ag(+)", "Al(+++)", "Au(+)", "Au(+++)", "Ba(++)", "Be(++)", "Bi(+++)", "Ca(++)", "Cd(++)", "Ce(+++)", "Ce(++++)", "Co(++) ls", "Co(++) hs", "Co(+++) ls", "Co(+++) hs", "Cr(++) ls", "Cr(++) hs", "Cr(+++)", "Cs(+)", "Cu(+)", "Cu(++)", "Dy(+++)", "Er(+++)", "Eu(++)", "Eu(+++)", "Fe(++) ls", "Fe(++) hs", "Fe(+++) ls", "Fe(+++) hs", "Ga(+++)", "Gd(+++)", "Hf(++++)", "Hg(+)", "Hg(++)", "Ho(+++)", "In(+++)", "Ir(+++)", "K(+)", "La(+++)", "Li(+)", "Lu(+++)", "Mg(++)", "Mn(++) ls", "Mn(++) hs", "Mn(+++) ls", "Mn(+++) hs", "Mo(+++)", "Na(+)", "Nb(+++)", "Nd(+++)", "Ni(++)", "Pb(++)", "Pd(++)", "Pm(+++)", "Pr(+++)", "Pt(++)", "Rb(+)", "Rh(+++)", "Ru(+++)", "Sb(+++)", "Sc(+++)", "Sm(+++)", "Sr(++)", "Ta(+++)", "Tb(+++)", "Th(++++)", "Ti(++)", "Ti(+++)", "Ti(++++)", "Tl(+)", "Tl(+++)", "Tm(+++)", "U(+++)", "U(++++)", "V(++)", "V(+++)", "Y(+++)", "Yb(++)", "Yb(+++)", "Zn(++)", "Zr(++++)", "Br(-)",  "Cl(-)", "F(-)", "H(-)", "I(-)", "O(--)", "S(--)", "Se(--)", "Te(--)", "OH(-)"};
    private static String[] ions5 = {"Ag(1+)", "Al(3+)", "Au(1+)", "Au(3+)", "Ba(2+)", "Be(2+)", "Bi(3+)", "Ca(2+)", "Cd(2+)", "Ce(3+)", "Ce(4+)", "Co(2+) ls", "Co(2+) hs", "Co(3+) ls", "Co(3+) hs", "Cr(2+) ls", "Cr(2+) hs", "Cr(3+)", "Cs(1+)", "Cu(1+)", "Cu(2+)", "Dy(3+)", "Er(3+)", "Eu(2+)", "Eu(3+)", "Fe(2+) ls", "Fe(2+) hs", "Fe(3+) ls", "Fe(3+) hs", "Ga(3+)", "Gd(3+)", "Hf(4+)", "Hg(1+)", "Hg(2+)", "Ho(3+)", "In(3+)", "Ir(3+)", "K(1+)", "La(3+)", "Li(1+)", "Lu(3+)", "Mg(2+)", "Mn(2+) ls", "Mn(2+) hs", "Mn(3+) ls", "Mn(3+) hs", "Mo(3+)", "Na(1+)", "Nb(3+)", "Nd(3+)", "Ni(2+)", "Pb(2+)", "Pd(2+)", "Pm(3+)", "Pr(3+)", "Pt(2+)", "Rb(1+)", "Rh(3+)", "Ru(3+)", "Sb(3+)", "Sc(3+)", "Sm(3+)", "Sr(2+)", "Ta(3+)", "Tb(3+)", "Th(4+)", "Ti(2+)", "Ti(3+)", "Ti(4+)", "Tl(1+)", "Tl(3+)", "Tm(3+)", "U(3+)", "U(4+)", "V(2+)", "V(3+)", "Y(3+)", "Yb(2+)", "Yb(3+)", "Zn(2+)", "Zr(4+)", "Br(1-)",  "Cl(1-)", "F(1-)", "H(1-)", "I(1-)", "O(2-)", "S(2-)", "Se(2-)", "Te(2-)", "OH(1-)"};
    private static String[] ions6 = {"Ag(+1)", "Al(+3)", "Au(+1)", "Au(+3)", "Ba(+2)", "Be(+2)", "Bi(+3)", "Ca(+2)", "Cd(+2)", "Ce(+3)", "Ce(+4)", "Co(+2) ls", "Co(+2) hs", "Co(+3) ls", "Co(+3) hs", "Cr(+2) ls", "Cr(+2) hs", "Cr(+3)", "Cs(+1)", "Cu(+1)", "Cu(+2)", "Dy(+3)", "Er(+3)", "Eu(+2)", "Eu(+3)", "Fe(+2) ls", "Fe(+2) hs", "Fe(+3) ls", "Fe(+3) hs", "Ga(+3)", "Gd(+3)", "Hf(+4)", "Hg(+1)", "Hg(+2)", "Ho(+3)", "In(+3)", "Ir(+3)", "K(+1)", "La(+3)", "Li(+1)", "Lu(+3)", "Mg(+2)", "Mn(+2) ls", "Mn(+2) hs", "Mn(+3) ls", "Mn(+3) hs", "Mo(+3)", "Na(+1)", "Nb(+3)", "Nd(+3)", "Ni(+2)", "Pb(+2)", "Pd(+2)", "Pm(+3)", "Pr(+3)", "Pt(+2)", "Rb(+1)", "Rh(+3)", "Ru(+3)", "Sb(+3)", "Sc(+3)", "Sm(+3)", "Sr(+2)", "Ta(+3)", "Tb(+3)", "Th(+4)", "Ti(+2)", "Ti(+3)", "Ti(+4)", "Tl(+1)", "Tl(+3)", "Tm(+3)", "U(+3)", "U(+4)", "V(+2)", "V(+3)", "Y(+3)", "Yb(+2)", "Yb(+3)", "Zn(+2)", "Zr(+4)", "Br(-1)",  "Cl(-1)", "F(-1)", "H(-1)", "I(-1)", "O(-2)", "S(-2)", "Se(-2)", "Te(-2)", "OH(-1)"};

    // spin state indicators
    private static boolean[] spins = {false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true,false, false, false, false, false, false, false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true,false, false, false, false, false, false, false, false, false, false, false, false, false, false,false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    // charges
    private static int[] ionCharge = {1, 3,  1, 3, 2, 2, 3, 2, 2, 3, 4, 2, 2, 3, 3, 2, 2, 3, 1, 1, 2, 3, 3, 2, 3, 2, 2, 3, 3, 3, 3, 4, 1, 2, 3, 3, 3, 1, 3, 1, 3, 2, 2, 2, 3, 3, 3, 1, 3, 3, 2, 2, 2, 3, 3, 2, 1, 3, 3, 3, 3, 3, 2, 3, 3, 4, 2, 3, 4, 1, 3, 3, 3, 4, 2, 3, 3, 2, 3, 2, 4, -1, -1, -1, -1, -1, -2, -2, -2, -2, -1};

    // non-hydrated ionic radii (pm)
    private static double radii[] = {129, 67.5, 151, 99, 149, 59, 117, 114, 109, 115, 101, 79, 88.5, 68.5, 75, 87, 94, 75.5, 181, 91, 87, 105.2, 103, 131, 108.7, 75, 92, 69, 78.5, 76, 107.8, 85, 133, 116, 104.1, 94, 82, 152, 117.2, 90, 100.1, 86, 81, 97, 72, 78.5, 83, 116, 86, 112.3, 83, 133, 100, 111, 113, 94, 166, 80.5, 82, 90, 88.5, 109.8, 132, 86, 106.3, 108, 100, 81, 74.5, 164, 102.5, 102, 116.5, 103 ,93, 78, 104, 116, 100.8, 88, 86, 167, 182, 119, 139, 206, 126, 170, 184, 207, 120};

    // ion symbols for hydrated ion radii
    private static String[] hydratedIons1 = {"Ag+", "Al+++", "Be++", "Ca++", "Cd++", "Cs+", "K+", "Li+", "Mg++", "Na+", "Pb++", "Rb+", "Tl+", "Zn++", "H30+", "NH4+", "Cl-", "Br-", "F-", "I-", "NO3-", "OH-"};
    private static String[] hydratedIons2 = {"Ag1+", "Al3+", "Be2+", "Ca2+", "Cd2+", "Cs1+", "K1+", "Li1+", "Mg2+", "Na1+", "Pb2+", "Rb1+", "Tl1+", "Zn2+", "H301+", "NH41+", "Cl1-", "Br1-", "F1-", "I1-", "NO31-", "OH1-"};
    private static String[] hydratedIons3 = {"Ag+1", "Al+3", "Be+2", "Ca+2", "Cd+2", "Cs+1", "K+1", "Li+1", "Mg+2", "Na+1", "Pb+2", "Rb+1", "Tl+1", "Zn+2", "H30+1", "NH4+1", "Cl-1", "Br-1", "F-1", "I-1", "NO3-1", "OH-1"};
    private static String[] hydratedIons4 = {"Ag(+)", "Al(+++)", "Be(++)", "Ca(++)", "Cd(++)", "Cs(+)", "K(+)", "Li(+)", "Mg(++)", "Na(+)", "Pb(++)", "Rb(+)", "Tl(+)", "Zn(++)", "H30(+)", "NH4(+)", "Cl(-)", "Br(-)", "F(-)", "I(-)", "NO3(-)", "OH(-)"};
    private static String[] hydratedIons5 = {"Ag(1+)", "Al(3+)", "Be(2+)", "Ca(2+)", "Cd(2+)", "Cs(1+)", "K(1+)", "Li(1+)", "Mg(2+)", "Na(1+)", "Pb(2+)", "Rb(1+)", "Tl(1+)", "Zn(2+)", "H30(1+)", "NH4(1+)", "Cl(1-)", "Br(1-)", "F(1-)", "I(1-)", "NO3(1-)", "OH(1-)"};
    private static String[] hydratedIons6 = {"Ag(+1)", "Al(+3)", "Be(+2)", "Ca(+2)", "Cd(+2)", "Cs(+1)", "K(+1)", "Li(+1)", "Mg(+2)", "Na(+1)", "Pb(+2)", "Rb(+1)", "Tl(+1)", "Zn(+2)", "H30(+1)", "NH4(+1)", "Cl(-1)", "Br(-1)", "F(-1)", "I(-1)", "NO3(-1)", "OH(-1)"};

    // hydated ionic radii (pm)
    private static double hydratedRadii[] = {341.0, 480.0, 459.0, 412.0, 426.0, 329.0, 331.0, 382.0, 428.0, 358.0, 401.0, 329.0, 330.0, 430.0, 280.0, 331.0, 332.0, 332.0, 330.0, 352.0, 340.0, 300.0};

    private static int nIons = 91;             // number of ions listed for non-hydrated radii
    private static int nHydratedIons = 22;     // number of ions listed for hydrated radii

    // variables used in methods
    // String ion - symbol of current ion without spin indicator
    // String fullIon - symbol of current ion with spin indicator if available
    // String spin - spin value of current ion if option available
    // boolean spinSet = true when spin state is set

    // Method to return an ionic radius for ion with low and high spin states
    public static double radius(String ion, String spin){
        boolean spinSet = false;
        spin = spin.trim();

        if(spin.equals("ls") || spin.equals("low") || spin.equals("low spin") || spin.equals("LS")){
            spin = "ls";
        }
        else{
            if(spin.equals("hs") || spin.equals("high") || spin.equals("high spin") || spin.equals("HS")){
                spin = "hs";
            }
            else{
                throw new IllegalArgumentException("spin state must be entered as ls or hs not as " + spin);
            }
        }
        spinSet = true;
        ion = ion.trim();
        String fullIon = ion + " " + spin;
        return radiusCalc(fullIon, spinSet);
    }

    // Method to return an ionic radius - no spin state set
    public static double radius(String ion){
        boolean spinSet = false;
        return radiusCalc(ion, spinSet);
    }

    // Private method to retrieve an ionic radius
    private static double radiusCalc(String ion, boolean spinSet){
        String fullIon = ion.trim();
        if(!spinSet)ion = fullIon;

        boolean test0 = true;
        boolean test1 = false;
        int ii = 0;
        double radius = 0.0D;

        // check entered ion against ion list
        while(test0){
            if(IonicRadii.compareBare(fullIon, ii)){
                test0 = false;
                test1 = true;
                radius = IonicRadii.radii[ii]*1e-12;
            }
            else{
                ii++;
                if(ii>=IonicRadii.nIons)test0=false;
            }
        }

        // check if hs or ls is needed and is missing
        if(!test1 && !spinSet){
            test0=true;
            ii=0;
            while(test0){
                if(compareSubstringBare(ion, ii) && IonicRadii.spins[ii]){
                    test0 = false;
                    test1 = true;
                    boolean test2 = true;
                    String enqTitle = ion+" may be low spin or high spin";
                    Object[] options = { "low spin", "high spin" };
                    while(test2){
                        int opt = JOptionPane.showOptionDialog(null, "Click appropriate box", enqTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null, options, options[0]);

                        if(opt==0){
                            radius = IonicRadii.radii[ii]*1e-12;
                            test2 = false;
                        }
                        else{
                            if(opt==1){
                                radius = IonicRadii.radii[ii+1]*1e-12;
                                test2 = false;
                            }
                            else{
                                System.out.println("You must click either low spin or high spin");
                            }
                        }
                    }
                }
                else{
                    ii++;
                    if(ii>=IonicRadii.nIons)test0=false;
                }
            }
        }

        // check if hs or ls has been entered and is not appropriate to the ion entered
        if(!test1){
            if(spinSet){
                test0 = true;
                ii = 0;
                while(test0){
                    if(IonicRadii.compareBare(ion, ii)){
                        test0 = false;
                        test1 = true;
                        radius = IonicRadii.radii[ii]*1e-12;
                        System.out.println(ion + " does not have low and high spin states listed");
                        System.out.println("Single availabe listed radius was used");
                    }
                    else{
                        ii++;
                        if(ii>=IonicRadii.nIons)test0=false;
                    }
                }
            }
        }

        // Abort if entered ion was not found in lists
        if(!test1){
            System.out.println("Class: IonicRadii\nMethod: radius\n" + fullIon + " was not found in the lists of non-hydrated radii");
            System.out.println("0.0D returned");
        }
        spinSet = false;
        return radius;
    }

    // Compares entered ion with all lists of ions for non-hydrated radii values
    public static boolean compareBare(String ion, int ii){
        boolean test = false;
        if(ion.equals(IonicRadii.ions1[ii])||ion.equals(IonicRadii.ions2[ii])||ion.equals(IonicRadii.ions3[ii])||ion.equals(IonicRadii.ions4[ii])||ion.equals(IonicRadii.ions5[ii])||ion.equals(IonicRadii.ions6[ii])){
            test = true;
        }
        return test;
    }

    // Compares entered ion as a substring, e.g. without ls or hs indicator, with all lists of ions for non-hydrated radii values
    public static boolean compareSubstringBare(String ion, int ii){
        boolean test = false;
        if(IonicRadii.ions1[ii].indexOf(ion)>-1||IonicRadii.ions2[ii].indexOf(ion)>-1||IonicRadii.ions3[ii].indexOf(ion)>-1||IonicRadii.ions4[ii].indexOf(ion)>-1||IonicRadii.ions5[ii].indexOf(ion)>-1||IonicRadii.ions6[ii].indexOf(ion)>-1){
            test = true;
        }
        return test;
    }

    // Method to return a hydrated ionic radius
    public static double hydratedRadius(String ion){
        ion = ion.trim();

        boolean test0 = true;
        boolean test1 = false;
        int i = 0;
        double radius = 0.0D;

        // check entered ion against ion list
        while(test0){
            if(IonicRadii.compareHydrated(ion, i)){
                test0 = false;
                test1 = true;
                radius = hydratedRadii[i]*1e-12;
            }
            else{
                i++;
                if(i>=nHydratedIons)test0=false;
            }
        }

        // Abort if entered ion was not found in lists
        if(!test1){
            System.out.println("Class: IonicRadii\nMethod: hydratedRadius\n"+ion + " was not found in the lists of hydrated radii");
            System.out.println("0.0D returned");
        }

        return radius;
    }


    // Compares entered ion with all lists of ions for hydrated radii values
    public static boolean compareHydrated(String ion, int ii){
        boolean test = false;
        if(ion.equals(IonicRadii.hydratedIons1[ii])||ion.equals(IonicRadii.hydratedIons2[ii])||ion.equals(IonicRadii.hydratedIons3[ii])||ion.equals(IonicRadii.hydratedIons4[ii])||ion.equals(IonicRadii.hydratedIons5[ii])||ion.equals(IonicRadii.hydratedIons6[ii])){
            test = true;
        }
        return test;
    }

    // Compares entered ion as a substring, e.g. without ls or hs indicator, with all lists of ions for hydrated radii values
    public static boolean compareSubstringHydrated(String ion, int ii){
        boolean test = false;
        if(IonicRadii.hydratedIons1[ii].indexOf(ion)>-1||IonicRadii.hydratedIons2[ii].indexOf(ion)>-1||IonicRadii.hydratedIons3[ii].indexOf(ion)>-1||IonicRadii.hydratedIons4[ii].indexOf(ion)>-1||IonicRadii.hydratedIons5[ii].indexOf(ion)>-1||IonicRadii.hydratedIons6[ii].indexOf(ion)>-1){
            test = true;
        }
        return test;
    }

    // Method to return the charge on the ion
    public static int charge(String ion){
        ion = ion.trim();

        boolean test0 = true;
        boolean test1 = false;
        int i = 0;
        int charge = 0;

        // check entered ion against ion list
        while(test0){
            if(IonicRadii.compareBare(ion, i)){
                test0 = false;
                test1 = true;
                charge = ionCharge[i];
            }
            else{
                i++;
                if(i>=nIons)test0=false;
            }
        }

        // Abort if entered ion was not found in lists
        if(!test1){
            System.out.println("Class: IonicRadii\nMethod: charge\n"+ion + " was not found in the lists of non-hydrated ions");
            System.out.println("0 returned");
        }

        return charge;
    }
}





