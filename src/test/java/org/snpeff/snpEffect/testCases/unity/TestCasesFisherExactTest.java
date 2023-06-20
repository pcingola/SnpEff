package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.probablility.Hypergeometric;
import org.snpeff.util.Log;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for Hypergeometric distribution and Fisher exact test
 *
 * @author pcingola
 */
public class TestCasesFisherExactTest {

    public static double MAX_DIFF = 0.00000000001;

    boolean verbose = false;
    double threshold = 0.01;
    int numTests = 100;
    int MAX = 1000;
    Random rand;

    public TestCasesFisherExactTest() {
        super();
        initRand();
    }

    void compareFisher(int k, int N, int D, int n, double result, double p) {
        double abs = Math.abs(p - result);
        double diff = abs / Math.min(p, result);

        if ((abs > 1E-300) && (diff > MAX_DIFF)) {
            String err = "\tDifference:" + diff //
                    + "\n\tpValue:\t" + p //
                    + "\n\tExpected:\t" + result //
                    + "\n\tR: " + FisherExactTest.get().toR(k, N, D, n, true);
            Log.debug("Error\n" + err);
            throw new RuntimeException(err);
        }
    }

    /**
     * Compare a result form a Fisher exact test (lower tail)
     */
    void compareFisherDown(int k, int N, int D, int n, double result) {
        double p = FisherExactTest.get().fisherExactTestDown(k, N, D, n, threshold);
        compareFisher(k, N, D, n, result, p);

        p = FisherExactTest.get().fisherExactTestDown(k, N, D, n);
        compareFisher(k, N, D, n, result, p);
    }

    /**
     * Compare a result form a Fisher exact test (upper tail)
     */
    void compareFisherUp(int k, int N, int D, int n, double result) {
        double p = FisherExactTest.get().pValueUp(k, N, D, n, threshold);
        compareFisher(k, N, D, n, result, p);

        p = FisherExactTest.get().fisherExactTestUp(k, N, D, n);
        compareFisher(k, N, D, n, result, p);
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
                    System.out.println();
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
    public void test_03_fisher() {
        Log.debug("Test");
        compareFisherUp(59, 545, 136, 95, 8.28958173422445e-18);
        compareFisherUp(36, 345, 190, 41, 2.40265087580901e-06);
        compareFisherUp(97, 202, 133, 105, 1.18466240918432e-17);
        compareFisherUp(152, 610, 181, 333, 9.77424907183547e-23);
        compareFisherUp(135, 444, 142, 138, 6.928747246508e-101);
        compareFisherUp(152, 738, 203, 446, 3.67088986300015e-07);
        compareFisherUp(247, 578, 388, 283, 3.79404002619577e-25);
        compareFisherUp(429, 888, 817, 446, 2.26456109642847e-06);
        compareFisherUp(269, 351, 311, 287, 1.82978544974049e-08);
        compareFisherUp(91, 523, 106, 234, 1.50955326758971e-22);
        compareFisherUp(35, 964, 146, 102, 2.21430371305547e-07);
        compareFisherUp(23, 470, 58, 108, 0.00174305424896066);
        compareFisherUp(65, 755, 119, 162, 1.25368328189431e-18);
        compareFisherUp(15, 380, 151, 21, 0.00257864863363782);
        compareFisherUp(17, 206, 36, 50, 0.000767632652092611);
        compareFisherUp(109, 521, 110, 472, 7.4260450370284e-05);
        compareFisherUp(30, 789, 310, 52, 0.00423623853376874);
        compareFisherUp(42, 726, 155, 45, 4.16785300462029e-27);
        compareFisherUp(293, 803, 481, 450, 0.000438131577741957);
        compareFisherUp(131, 375, 151, 249, 1.30995155177008e-12);
        compareFisherUp(26, 143, 33, 81, 0.00262084046420338);
        compareFisherUp(19, 146, 20, 71, 3.30381924570966e-06);
        compareFisherUp(35, 488, 97, 57, 1.20102019250494e-13);
        compareFisherUp(119, 983, 628, 145, 2.10926932575014e-07);
        compareFisherUp(45, 148, 53, 50, 5.81512027352164e-24);
        compareFisherUp(19, 446, 46, 26, 1.45759552943497e-15);
        compareFisherUp(607, 770, 734, 620, 2.4342390531863e-09);
        compareFisherUp(185, 546, 206, 407, 2.49420910249361e-11);
        compareFisherUp(136, 917, 169, 294, 8.2186630995667e-48);
        compareFisherUp(31, 321, 108, 43, 3.27189426625206e-08);
        compareFisherUp(97, 396, 167, 106, 1.48934787720076e-35);
        compareFisherUp(7, 305, 14, 51, 0.00331494622577636);
        compareFisherUp(71, 835, 75, 483, 7.2876824125444e-14);
        compareFisherUp(37, 461, 70, 181, 0.00876705910701063);
        compareFisherUp(198, 761, 318, 432, 0.00580370806914619);
        compareFisherUp(133, 936, 378, 195, 8.99332118627283e-19);
        compareFisherUp(23, 291, 65, 50, 3.31838151428692e-05);
        compareFisherUp(63, 560, 67, 190, 2.1131322965245e-28);
        compareFisherUp(44, 115, 71, 49, 3.89847129521515e-08);
        compareFisherUp(153, 800, 195, 527, 9.21709696267346e-06);
        compareFisherUp(37, 397, 67, 156, 0.00285215778284655);
        compareFisherUp(94, 433, 212, 130, 1.18080726821783e-10);
        compareFisherUp(629, 915, 873, 636, 5.48138806189002e-13);
        compareFisherUp(225, 687, 254, 282, 3.86407484833323e-91);
        compareFisherUp(187, 550, 221, 305, 2.28319162936954e-31);
        compareFisherUp(183, 812, 194, 725, 0.00473364047467412);
        compareFisherUp(75, 531, 142, 193, 1.99243069292124e-06);
        compareFisherUp(133, 897, 158, 661, 0.00044883940423697);
        compareFisherUp(168, 973, 227, 599, 5.28590254525447e-06);
        compareFisherUp(266, 492, 375, 276, 5.17872981915267e-35);
        compareFisherUp(142, 814, 161, 579, 1.1947296127053e-08);
        compareFisherUp(114, 713, 149, 177, 3.80835210298479e-54);
        compareFisherUp(49, 174, 61, 92, 5.76190646498642e-08);
        compareFisherUp(70, 361, 243, 88, 0.00300291746372154);
        compareFisherUp(11, 417, 31, 56, 0.00098764195265165);
        compareFisherUp(36, 537, 157, 41, 4.94220221304424e-16);
        compareFisherUp(115, 471, 182, 175, 1.56291175266919e-20);
        compareFisherUp(495, 881, 523, 763, 2.98889043133355e-17);
        compareFisherUp(241, 674, 307, 457, 3.14514846186333e-08);
        compareFisherUp(200, 443, 303, 242, 1.08062874506896e-12);
        compareFisherUp(353, 987, 554, 366, 3.06547860908313e-101);
        compareFisherUp(11, 300, 71, 20, 0.00172542325090638);
        compareFisherUp(42, 252, 204, 44, 0.00318184189489258);
        compareFisherUp(51, 305, 80, 53, 1.47808357334896e-34);
        compareFisherUp(34, 313, 176, 46, 0.0062078519294078);
        compareFisherUp(24, 373, 25, 268, 0.0021702822049755);
        compareFisherUp(47, 677, 141, 48, 3.36388120818521e-34);
        compareFisherUp(41, 328, 65, 89, 5.84598356867524e-12);
        compareFisherUp(65, 559, 190, 81, 2.05254736770733e-20);
        compareFisherUp(75, 984, 299, 165, 5.27613211166751e-06);
        compareFisherUp(92, 270, 95, 240, 0.00107752920684479);
        compareFisherUp(203, 969, 612, 220, 1.65237135169329e-28);
        compareFisherUp(168, 255, 194, 194, 3.59390288867635e-11);
        compareFisherUp(25, 395, 74, 31, 4.64779278697149e-15);
        compareFisherUp(154, 621, 171, 426, 3.32248893082949e-14);
        compareFisherUp(12, 327, 148, 15, 0.00569010303563112);
        compareFisherUp(80, 772, 97, 245, 1.60368435504478e-28);
        compareFisherUp(21, 654, 138, 25, 1.04540516516679e-11);
        compareFisherUp(119, 432, 121, 285, 8.46674711440474e-24);
        compareFisherUp(200, 672, 245, 202, 3.72458025046742e-123);
        compareFisherUp(13, 675, 19, 137, 5.33314581564644e-06);
        compareFisherUp(48, 299, 70, 119, 2.51027306645499e-08);
        compareFisherUp(197, 694, 250, 432, 4.58839930028976e-12);
        compareFisherUp(103, 823, 210, 295, 3.59407139405134e-06);
        compareFisherUp(73, 235, 108, 102, 3.222899986319e-12);
        compareFisherUp(565, 848, 684, 623, 2.88228387511532e-31);
        compareFisherUp(364, 582, 426, 384, 6.72059678081954e-61);
        compareFisherUp(39, 781, 42, 145, 2.07138402016061e-27);
        compareFisherUp(179, 371, 184, 302, 1.23670887007989e-16);
        compareFisherUp(159, 541, 459, 170, 4.77152383854613e-05);
        compareFisherUp(137, 675, 190, 233, 5.40690104095474e-37);
        compareFisherUp(360, 800, 406, 372, 1.17796424900086e-155);
        compareFisherUp(281, 736, 303, 620, 3.36267010147432e-08);
        compareFisherUp(504, 795, 644, 606, 0.0044212941820773);
        compareFisherUp(141, 450, 183, 209, 5.03306096804749e-28);
        compareFisherUp(247, 968, 390, 299, 2.01859193466352e-74);
        compareFisherUp(25, 172, 51, 54, 0.00131684344612187);
        compareFisherUp(54, 757, 220, 71, 6.58948512308768e-18);
        compareFisherUp(70, 301, 180, 97, 0.00172465566053411);
        compareFisherUp(194, 683, 500, 204, 1.56219065349161e-20);
    }

    @Test
    public void test_04_fisher() {
        Log.debug("Test");
        compareFisherDown(57, 470, 141, 281, 6.6866974987128e-09);
        compareFisherDown(152, 912, 754, 203, 0.000440144803784442);
        compareFisherDown(14, 873, 552, 42, 1.37118944858872e-05);
        compareFisherDown(16, 911, 728, 164, 5.41965575482104e-118);
        compareFisherDown(2, 152, 51, 41, 5.37265914345806e-08);
        compareFisherDown(117, 480, 274, 253, 1.05549713246985e-07);
        compareFisherDown(4, 236, 192, 10, 0.000393391860642947);
        compareFisherDown(27, 521, 155, 357, 7.1546179587792e-62);
        compareFisherDown(7, 461, 151, 184, 2.67218847522338e-33);
        compareFisherDown(29, 510, 305, 203, 1.10542140757286e-72);
        compareFisherDown(13, 237, 88, 156, 1.71630737398072e-42);
        compareFisherDown(89, 572, 403, 188, 1.58248960811606e-17);
        compareFisherDown(27, 335, 172, 173, 1.00022714178528e-47);
        compareFisherDown(1, 402, 343, 30, 3.55291842099265e-29);
        compareFisherDown(66, 306, 122, 206, 1.87164960370351e-05);
        compareFisherDown(16, 61, 40, 36, 4.57273382026378e-07);
        compareFisherDown(32, 554, 163, 301, 4.8225896336559e-28);
        compareFisherDown(8, 789, 388, 277, 3.18813457308328e-98);
        // compareFisherDown(260, 556, 539, 277, 0);
        compareFisherDown(2, 890, 10, 796, 1.04769809538842e-08);
        compareFisherDown(42, 770, 308, 367, 4.38024039390092e-59);
        compareFisherDown(6, 55, 19, 33, 0.000296863410300295);
        compareFisherDown(4, 171, 92, 47, 1.31758038623838e-15);
        compareFisherDown(310, 995, 327, 976, 1.92919081042295e-08);
        compareFisherDown(183, 868, 584, 305, 0.000313284475334555);
        compareFisherDown(13, 112, 93, 30, 5.09345032728386e-12);
        compareFisherDown(73, 392, 154, 304, 1.16596572422222e-33);
        compareFisherDown(1, 385, 30, 144, 3.82323826836252e-07);
        compareFisherDown(47, 554, 101, 407, 2.47909349507427e-11);
        compareFisherDown(18, 505, 326, 129, 9.10158234925755e-46);
        compareFisherDown(16, 515, 475, 39, 8.60004121044127e-21);
        compareFisherDown(91, 926, 340, 335, 1.56179857832206e-06);
        compareFisherDown(49, 953, 389, 335, 2.84436360391136e-37);
        compareFisherDown(2, 763, 61, 554, 7.7377635214529e-36);
        compareFisherDown(1, 40, 21, 18, 1.67577669149464e-10);
        compareFisherDown(12, 772, 45, 579, 2.42945701198204e-13);
        compareFisherDown(22, 347, 82, 279, 1.58907702327229e-41);
        compareFisherDown(2, 446, 273, 111, 1.52663791438916e-57);
        compareFisherDown(46, 391, 323, 91, 1.8448381164756e-18);
        compareFisherDown(52, 556, 247, 163, 3.76060089022952e-05);
        compareFisherDown(2, 282, 189, 85, 3.85835151779573e-60);
        compareFisherDown(29, 130, 62, 89, 3.17545503454762e-08);
        compareFisherDown(63, 629, 201, 264, 6.5881507974015e-05);
        compareFisherDown(200, 732, 471, 397, 5.29538922431954e-19);
        compareFisherDown(49, 517, 403, 105, 9.86612011417267e-17);
        compareFisherDown(12, 530, 38, 376, 3.04971603958179e-08);
        compareFisherDown(18, 656, 67, 587, 2.68818158736483e-44);
        compareFisherDown(16, 242, 40, 187, 1.93661820051305e-09);
        compareFisherDown(89, 698, 546, 149, 1.14195550706473e-09);
        compareFisherDown(8, 556, 440, 95, 3.14204784057095e-68);
        compareFisherDown(5, 431, 44, 152, 3.39929178807456e-05);
        //compareFisherDown(70, 251, 118, 203, 0);
        compareFisherDown(32, 915, 156, 574, 1.77058007272689e-33);
        compareFisherDown(26, 627, 461, 135, 3.93976550346058e-55);
        compareFisherDown(59, 619, 460, 131, 4.10650753885808e-17);
        compareFisherDown(10, 399, 72, 155, 7.996152169811e-08);
        compareFisherDown(77, 445, 250, 169, 0.000141929399394228);
        compareFisherDown(60, 821, 522, 197, 1.24193470803979e-28);
        compareFisherDown(15, 627, 29, 495, 0.000176997186877898);
        compareFisherDown(51, 362, 296, 85, 4.83200953355619e-09);
        compareFisherDown(65, 736, 240, 460, 1.68636888045595e-44);
        compareFisherDown(8, 551, 35, 419, 5.79043383485544e-13);
        compareFisherDown(31, 181, 52, 141, 6.371377257703e-05);
        compareFisherDown(155, 931, 546, 410, 1.57141011213516e-31);
        compareFisherDown(49, 370, 120, 200, 0.000131263672590605);
        compareFisherDown(19, 215, 168, 47, 4.78138299321588e-12);
        compareFisherDown(10, 567, 424, 29, 4.27769623511992e-07);
        compareFisherDown(51, 372, 313, 107, 1.36720312068375e-34);
        compareFisherDown(13, 393, 155, 97, 3.42880198036781e-11);
        compareFisherDown(28, 452, 192, 278, 3.07029863124219e-81);
        compareFisherDown(34, 812, 594, 112, 8.01635155599195e-26);
        compareFisherDown(100, 637, 474, 179, 1.91831046845406e-11);
        compareFisherDown(5, 425, 279, 115, 6.3490979346593e-65);
        compareFisherDown(54, 838, 243, 263, 7.04361587049744e-05);
        compareFisherDown(3, 825, 376, 55, 8.19330754927712e-13);
        compareFisherDown(15, 109, 80, 35, 1.65593752618442e-07);
        compareFisherDown(73, 674, 543, 118, 2.97539829045907e-08);
        compareFisherDown(309, 699, 344, 650, 0.000310818727006591);
        compareFisherDown(46, 469, 85, 335, 4.61477161937294e-05);
        compareFisherDown(115, 551, 515, 143, 1.62070097654381e-12);
        compareFisherDown(3, 162, 8, 144, 1.98324541560583e-05);
        compareFisherDown(72, 528, 260, 271, 1.87125928237604e-28);
        compareFisherDown(21, 589, 439, 84, 1.26356949684138e-26);
        compareFisherDown(8, 997, 545, 86, 1.79712562732956e-21);
        compareFisherDown(1, 153, 5, 120, 0.000362873598189044);
        compareFisherDown(155, 476, 237, 380, 6.80742086958635e-17);
        compareFisherDown(12, 243, 32, 173, 3.46251414041836e-06);
        compareFisherDown(2, 69, 30, 20, 1.84777897818333e-05);
        compareFisherDown(10, 621, 137, 319, 1.2359893210442e-36);
        compareFisherDown(13, 908, 418, 101, 9.79596645147574e-15);
        compareFisherDown(59, 519, 227, 260, 1.35995478816319e-23);
        compareFisherDown(81, 866, 215, 463, 2.63951613872026e-08);
        compareFisherDown(2, 669, 482, 20, 2.29978215266672e-10);
        compareFisherDown(1, 255, 28, 87, 3.61797690526272e-06);
        compareFisherDown(25, 509, 204, 94, 0.000867040874821994);
        compareFisherDown(1, 128, 40, 25, 2.40252924022306e-05);
        compareFisherDown(189, 964, 348, 621, 3.35004927065627e-07);
        compareFisherDown(11, 158, 133, 23, 7.06584015934966e-07);
        compareFisherDown(65, 358, 114, 267, 8.50412154506876e-08);
        compareFisherDown(179, 775, 414, 507, 6.06818418807189e-49);
    }

    @Test
    public void test_05_fisher() {
        Log.debug("Test");
        compareFisherDown(1, 100, 50, 0, 1);
        compareFisherDown(1, 100, 0, 20, 1);
        compareFisherDown(0, 100, 50, 0, 0);
        compareFisherDown(0, 100, 0, 20, 0);
    }

    /**
     * Compare Fisher exact test to Chi^2 approximation
     * <p>
     * From R:
     * > data <- matrix(c(25, 5, 15, 15), ncol=2, byrow=T)
     * > data
     * [,1] [,2]
     * [1,]   25    5
     * [2,]   15   15
     * <p>
     * > chisq.test(data,correct=FALSE)
     * <p>
     * Pearson's Chi-squared test
     * <p>
     * data:  data
     * X-squared = 7.5, df = 1, p-value = 0.00617
     * <p>
     * > fisher.test(data, alternative="greater")
     * <p>
     * Fisher's Exact Test for Count Data
     * <p>
     * data:  data
     * p-value = 0.006349
     * alternative hypothesis: true odds ratio is greater than 1
     * 95 percent confidence interval:
     * 1.587561      Inf
     * sample estimates:
     * odds ratio
     * 4.859427
     */
    @Test
    public void test_06_fisher_vs_chi2() {
        Log.debug("Test");
        int n11 = 25, n12 = 5;
        int n21 = 15, n22 = 15;

        int k = n11;
        int D = n11 + n12;
        int N = n11 + n12 + n21 + n22;
        int n = n11 + n21;

        // Chi square approximation (without Yates correction)
        double pChi = FisherExactTest.get().chiSquareApproximation(k, N, D, n);
        if (verbose) Log.info("Chi^2  p-value: " + pChi);
        assertEquals(pChi, 0.00617, 0.00001);

        // Fisher exact test
        double pFish = FisherExactTest.get().fisherExactTestUp(k, N, D, n);
        if (verbose) Log.info("Fisher p-value: " + pFish);
        assertEquals(pFish, 0.006349, 0.000001);

        double ratio = pFish / pChi;
        if (verbose) Log.info("Ratio: " + ratio);
    }

}
