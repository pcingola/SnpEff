package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.probablility.Hypergeometric;
import org.snpeff.util.Log;

import java.util.Random;

/**
 * Test for Hypergeometric distribution and Fisher exact test
 *
 * @author pcingola
 */
public class TestCasesHypergeometric {

    public static double MAX_DIFF = 0.00000000001;

    boolean verbose = true;
    double threshold = 0.01;
    int numTests = 100;
    int MAX = 1000;
    Random rand;

    public TestCasesHypergeometric() {
        super();
        initRand();
    }

    /**
     * Compare hypergeometric results
     *
     * @param k
     * @param N
     * @param D
     * @param n
     * @param result
     */
    void compareHypergeometric(int k, int N, int D, int n, double result) {
        double p = Hypergeometric.get().hypergeometric(k, N, D, n);

        double abs = Math.abs(p - result);
        double diff = abs / Math.min(p, result);
        if ((abs > 1E-300) && (diff > 0.00001))
            throw new RuntimeException("Difference:" + diff + "\t\t" + p + " != " + result);
    }

    /**
     * Create commands in R to run and get test results
     */
    public void generate_test() {
        boolean lowerTail = true;
        for (int i = 0; i < numTests; ) {
            int N = rand.nextInt(MAX) + 1;
            int D = rand.nextInt(N) + 1;
            int n = rand.nextInt(N) + 1;
            int k = Math.max(rand.nextInt(Math.min(n, D)), 1);

            if (Hypergeometric.get().checkHypergeometricParams(k, N, D, n)) {
                double p = FisherExactTest.get().pValueDown(k, N, D, n, threshold);

                if (p < threshold) {
                    System.out.print("\t print ( paste( 'compareFisher( " + k + ", " + N + ", " + D + ", " + n + ", ' , " + FisherExactTest.get().toR(k, N, D, n, lowerTail) + " , ');' ) );");
                    System.out.println("");
                    i++;
                }
            }
        }
    }

    void initRand() {
        rand = new Random(20110124);
    }

    @Test
    public void test_0() {
        Log.debug("Test");
        // generate_test();
    }

    @Test
    public void test_01_hyper() {
        Log.debug("Test");
        // Compare to values calculated using other programs (R)
        compareHypergeometric(1, 19, 2, 12, 0.4912281);
        compareHypergeometric(1, 70, 51, 1, 0.7285714);
        compareHypergeometric(7, 73, 9, 38, 0.0773475);
        compareHypergeometric(4, 45, 11, 5, 0.00918348);
        compareHypergeometric(4, 33, 17, 14, 0.02327653);
        compareHypergeometric(3, 28, 5, 7, 0.07478632);
        compareHypergeometric(1, 73, 31, 2, 0.4954338);
        compareHypergeometric(1, 2, 1, 2, 1);
        compareHypergeometric(1, 10, 1, 3, 0.3);
        compareHypergeometric(1, 78, 57, 11, 2.584604e-06);
        compareHypergeometric(3, 45, 17, 23, 0.0005133975);
        compareHypergeometric(4, 86, 59, 11, 0.01652058);
        compareHypergeometric(1, 28, 5, 22, 0.003357753);
        compareHypergeometric(1, 42, 4, 30, 0.05896542);
        compareHypergeometric(12, 95, 13, 49, 0.001216176);
        compareHypergeometric(1, 45, 2, 27, 0.4909091);
        compareHypergeometric(1, 38, 3, 14, 0.458037);
        compareHypergeometric(1, 43, 22, 5, 0.1367861);
        compareHypergeometric(1, 25, 2, 6, 0.38);
        compareHypergeometric(7, 80, 34, 13, 0.1598987);
        compareHypergeometric(1, 78, 34, 3, 0.4227877);
        compareHypergeometric(1, 88, 9, 70, 5.361089e-06);
        compareHypergeometric(1, 13, 11, 2, 0.2820513);
        compareHypergeometric(1, 36, 4, 22, 0.1359477);
        compareHypergeometric(1, 20, 6, 7, 0.2324303);
        compareHypergeometric(1, 21, 9, 4, 0.3308271);
        compareHypergeometric(1, 39, 6, 1, 0.1538462);
        compareHypergeometric(5, 19, 9, 12, 0.3000714);
        compareHypergeometric(1, 1, 1, 1, 1);
        compareHypergeometric(1, 10, 5, 3, 0.4166667);
        compareHypergeometric(1, 86, 2, 67, 0.34829);
        compareHypergeometric(1, 21, 4, 6, 0.4561404);
        compareHypergeometric(29, 92, 43, 55, 0.06424561);
        compareHypergeometric(1, 23, 2, 21, 0.1660079);
        compareHypergeometric(5, 72, 7, 24, 0.03254646);
        compareHypergeometric(19, 64, 29, 54, 0.0001322347);
        compareHypergeometric(1, 40, 3, 35, 0.0354251);
        compareHypergeometric(6, 18, 7, 13, 0.2696078);
        compareHypergeometric(1, 8, 2, 5, 0.5357143);
        compareHypergeometric(15, 80, 31, 21, 0.0004160716);
        compareHypergeometric(3, 32, 18, 13, 0.002351405);
        compareHypergeometric(13, 35, 22, 18, 0.1410843);
        compareHypergeometric(7, 63, 45, 15, 0.01625885);
        compareHypergeometric(1, 17, 14, 4, 0.005882353);
        compareHypergeometric(2, 20, 6, 15, 0.01354489);
        compareHypergeometric(1, 10, 8, 2, 0.3555556);
        compareHypergeometric(1, 2, 1, 2, 1);
        compareHypergeometric(1, 94, 2, 93, 0.02127660);
        compareHypergeometric(11, 49, 22, 25, 0.2238699);
        compareHypergeometric(11, 95, 20, 27, 0.003754185);
        compareHypergeometric(1, 21, 9, 2, 0.5142857);
        compareHypergeometric(1, 6, 2, 2, 0.5333333);
        compareHypergeometric(2, 18, 9, 7, 0.1425339);
        compareHypergeometric(16, 61, 40, 36, 1.500428e-05);
        compareHypergeometric(2, 54, 7, 5, 0.1076724);
        compareHypergeometric(1, 89, 1, 47, 0.5280899);
        compareHypergeometric(2, 26, 5, 13, 0.3391304);
        compareHypergeometric(1, 75, 1, 24, 0.32);
        compareHypergeometric(2, 21, 10, 12, 0.001684074);
        compareHypergeometric(1, 64, 8, 7, 0.4181274);
        compareHypergeometric(16, 89, 45, 48, 0.0003430746);
        compareHypergeometric(26, 56, 39, 37, 0.2367166);
        compareHypergeometric(10, 30, 16, 11, 0.002052307);
        compareHypergeometric(11, 92, 30, 68, 4.373004e-08);
        compareHypergeometric(17, 61, 46, 19, 0.06186141);
        compareHypergeometric(15, 70, 28, 17, 4.092152e-06);
        compareHypergeometric(6, 55, 19, 33, 0.001963553);
        compareHypergeometric(18, 71, 57, 31, 3.427275e-05);
        compareHypergeometric(48, 95, 62, 66, 0.01405199);
        compareHypergeometric(3, 46, 8, 34, 0.01816302);
        compareHypergeometric(3, 26, 8, 20, 0.004378230);
        compareHypergeometric(4, 69, 9, 47, 0.08288145);
        compareHypergeometric(13, 78, 57, 28, 0.0001087879);
        compareHypergeometric(20, 68, 24, 37, 0.000332827);
        compareHypergeometric(5, 89, 7, 23, 0.01047522);
        compareHypergeometric(3, 12, 5, 10, 0.1515152);
        compareHypergeometric(16, 65, 29, 47, 0.005136288);
        compareHypergeometric(5, 76, 12, 6, 0.0002318555);
        compareHypergeometric(2, 43, 15, 8, 0.2727957);
        compareHypergeometric(3, 46, 12, 35, 9.25133e-06);
        compareHypergeometric(3, 92, 18, 4, 0.02161083);
        compareHypergeometric(2, 87, 47, 7, 0.1217291);
        compareHypergeometric(1, 26, 6, 11, 0.1434783);
        compareHypergeometric(1, 36, 3, 28, 0.1098039);
        compareHypergeometric(1, 34, 1, 24, 0.7058824);
        compareHypergeometric(1, 88, 1, 81, 0.9204545);
        compareHypergeometric(1, 2, 2, 1, 1);
        compareHypergeometric(5, 23, 12, 7, 0.1776821);
        compareHypergeometric(1, 21, 3, 2, 0.2571429);
        compareHypergeometric(4, 83, 30, 55, 3.705065e-15);
        compareHypergeometric(1, 63, 1, 61, 0.968254);
        compareHypergeometric(1, 99, 20, 5, 0.420144);
        compareHypergeometric(1, 73, 2, 36, 0.5068493);
        compareHypergeometric(1, 65, 38, 2, 0.4932692);
        compareHypergeometric(1, 54, 1, 51, 0.9444444);
        compareHypergeometric(1, 5, 1, 4, 0.8);
        compareHypergeometric(1, 1, 1, 1, 1);
        compareHypergeometric(1, 48, 1, 10, 0.2083333);
        compareHypergeometric(1, 71, 63, 2, 0.2028169);
        compareHypergeometric(1, 46, 6, 20, 0.1404532);
        compareHypergeometric(1, 70, 1, 65, 0.9285714);
        compareHypergeometric(31, 70, 34, 60, 0.1259187);
        compareHypergeometric(1, 17, 1, 5, 0.2941176);
        compareHypergeometric(5, 30, 9, 13, 0.2140930);
        compareHypergeometric(1, 26, 4, 15, 0.1655518);
        compareHypergeometric(30, 93, 51, 57, 0.1471864);
        compareHypergeometric(11, 17, 13, 13, 0.1966387);
        compareHypergeometric(1, 25, 4, 2, 0.28);
        compareHypergeometric(1, 5, 4, 1, 0.8);
        compareHypergeometric(27, 89, 40, 28, 5.527519e-12);
        compareHypergeometric(9, 53, 25, 25, 0.06875583);
        compareHypergeometric(2, 18, 12, 6, 0.05332902);
        compareHypergeometric(1, 40, 21, 18, 3.167218e-08);
    }

    @Test
    public void test_02_hyper() {
        Log.debug("Test");
        // Compare to values calculated using other programs (R)
        compareHypergeometric(57, 470, 141, 281, 1.507456e-08);
        compareHypergeometric(152, 912, 754, 203, 0.0004373848);
        compareHypergeometric(44, 682, 324, 82, 0.04638736);
        compareHypergeometric(21, 373, 32, 294, 0.03056803);
        compareHypergeometric(59, 545, 136, 95, 7.273835e-18);
        compareHypergeometric(36, 345, 190, 41, 2.082751e-06);
        compareHypergeometric(46, 770, 68, 535, 0.1018642);
        compareHypergeometric(129, 833, 418, 241, 0.02855101);
        compareHypergeometric(3, 28, 5, 7, 0.07478632);
        compareHypergeometric(14, 873, 552, 42, 4.038633e-05);
        compareHypergeometric(97, 202, 133, 105, 1.128978e-17);
        compareHypergeometric(152, 610, 181, 333, 8.438052e-23);
        compareHypergeometric(135, 444, 142, 138, 6.925182e-101);
        compareHypergeometric(7, 486, 25, 51, 0.00781056);
        compareHypergeometric(1, 42, 4, 30, 0.05896542);
        compareHypergeometric(41, 145, 82, 72, 0.1323862);
        compareHypergeometric(152, 738, 203, 446, 2.215361e-07);
        compareHypergeometric(16, 911, 728, 164, 1.023263e-115);
        compareHypergeometric(37, 243, 54, 187, 0.03645426);
        compareHypergeometric(2, 152, 51, 41, 8.419947e-07);
        compareHypergeometric(117, 480, 274, 253, 1.791619e-07);
        compareHypergeometric(247, 578, 388, 283, 3.29638e-25);
        compareHypergeometric(429, 888, 817, 446, 1.650871e-06);
        compareHypergeometric(1, 913, 749, 4, 0.01879589);
        compareHypergeometric(4, 236, 192, 10, 0.003181798);
        compareHypergeometric(1, 620, 6, 207, 0.2627278);
        compareHypergeometric(269, 351, 311, 287, 1.610554e-08);
        compareHypergeometric(27, 521, 155, 357, 3.076794e-60);
        compareHypergeometric(7, 461, 151, 184, 7.18484e-32);
        compareHypergeometric(5, 924, 236, 23, 0.1826543);
        compareHypergeometric(30, 601, 236, 60, 0.0224787);
        compareHypergeometric(29, 510, 305, 203, 5.85835e-71);
        compareHypergeometric(29, 92, 43, 55, 0.06424561);
        compareHypergeometric(12, 634, 42, 295, 0.006764663);
        compareHypergeometric(13, 237, 88, 156, 2.39519e-40);
        compareHypergeometric(91, 523, 106, 234, 1.382574e-22);
        compareHypergeometric(89, 572, 403, 188, 6.492409e-17);
        compareHypergeometric(35, 964, 146, 102, 1.617078e-07);
        compareHypergeometric(23, 470, 58, 108, 0.001111836);
        compareHypergeometric(65, 755, 119, 162, 1.070822e-18);
        compareHypergeometric(15, 380, 151, 21, 0.002024582);
        compareHypergeometric(24, 173, 63, 55, 0.0544751);
        compareHypergeometric(29, 707, 33, 649, 0.1576592);
        compareHypergeometric(27, 335, 172, 173, 4.587644e-46);
        compareHypergeometric(8, 117, 36, 18, 0.08555023);
        compareHypergeometric(17, 206, 36, 50, 0.0005816399);
        compareHypergeometric(1, 10, 8, 2, 0.3555556);
        compareHypergeometric(33, 149, 48, 87, 0.03005922);
        compareHypergeometric(1, 402, 343, 30, 1.218651e-26);
        compareHypergeometric(109, 521, 110, 472, 6.95748e-05);
        compareHypergeometric(66, 306, 122, 206, 3.471764e-05);
        compareHypergeometric(16, 61, 40, 36, 1.500428e-05);
        compareHypergeometric(32, 554, 163, 301, 3.947078e-27);
        compareHypergeometric(30, 789, 310, 52, 0.002491050);
        compareHypergeometric(42, 726, 155, 45, 4.110391e-27);
        compareHypergeometric(293, 803, 481, 450, 0.0001804299);
        compareHypergeometric(131, 375, 151, 249, 1.094179e-12);
        compareHypergeometric(210, 321, 274, 246, 0.1475397);
        compareHypergeometric(8, 789, 388, 277, 3.078242e-96);
        compareHypergeometric(260, 556, 539, 277, 5.566594e-06);
        compareHypergeometric(82, 130, 116, 91, 0.2064143);
        compareHypergeometric(3, 461, 374, 7, 0.02295504);
        compareHypergeometric(2, 890, 10, 796, 4.312254e-07);
        compareHypergeometric(42, 770, 308, 367, 6.205572e-58);
        compareHypergeometric(6, 55, 19, 33, 0.001963553);
        compareHypergeometric(4, 171, 92, 47, 3.491095e-14);
        compareHypergeometric(310, 995, 327, 976, 3.646995e-07);
        compareHypergeometric(2, 184, 34, 28, 0.05393918);
        compareHypergeometric(82, 669, 183, 338, 0.01339627);
        compareHypergeometric(1, 278, 3, 152, 0.3379183);
        compareHypergeometric(183, 868, 584, 305, 0.0002243882);
        compareHypergeometric(13, 112, 93, 30, 2.834419e-10);
        compareHypergeometric(26, 143, 33, 81, 0.001992269);
        compareHypergeometric(19, 146, 20, 71, 3.193124e-06);
        compareHypergeometric(73, 392, 154, 304, 4.244224e-32);
        compareHypergeometric(2, 87, 47, 7, 0.1217291);
        compareHypergeometric(13, 336, 15, 256, 0.1726304);
        compareHypergeometric(1, 385, 30, 144, 7.79075e-06);
        compareHypergeometric(35, 488, 97, 57, 1.079242e-13);
        compareHypergeometric(2, 302, 168, 7, 0.1104994);
        compareHypergeometric(5, 23, 12, 7, 0.1776821);
        compareHypergeometric(119, 983, 628, 145, 1.421082e-07);
        compareHypergeometric(324, 663, 403, 541, 0.05047842);
        compareHypergeometric(1, 99, 20, 5, 0.420144);
        compareHypergeometric(1, 741, 671, 2, 0.1713171);
        compareHypergeometric(1, 73, 2, 36, 0.5068493);
        compareHypergeometric(47, 554, 101, 407, 8.9411e-11);
        compareHypergeometric(1, 152, 59, 4, 0.3582004);
        compareHypergeometric(18, 505, 326, 129, 2.489987e-44);
        compareHypergeometric(45, 148, 53, 50, 5.759724e-24);
        compareHypergeometric(19, 446, 46, 26, 1.422818e-15);
        compareHypergeometric(1, 178, 3, 142, 0.09679974);
        compareHypergeometric(607, 770, 734, 620, 2.162936e-09);
        compareHypergeometric(185, 546, 206, 407, 1.97752e-11);
        compareHypergeometric(136, 917, 169, 294, 7.691036e-48);
        compareHypergeometric(31, 321, 108, 43, 2.813547e-08);
        compareHypergeometric(16, 515, 475, 39, 3.418161e-19);
        compareHypergeometric(91, 926, 340, 335, 1.576175e-06);
        compareHypergeometric(97, 396, 167, 106, 1.446200e-35);
        compareHypergeometric(7, 305, 14, 51, 0.002821931);
    }
}
