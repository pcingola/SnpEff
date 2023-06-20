package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for Hypergeometric distribution and Fisher exact test
 *
 * @author pcingola
 */
public class TestCasesChiSquare {

    public static double MAX_DIFF = 1e-6;

    boolean verbose = false;

    public TestCasesChiSquare() {
        super();
    }

    @Test
    public void test_1() {
        Log.debug("Test");
        // Data calculated using R:
        // 		x <- 1:100
        // 		pchisq(x,1,lower.tail=F)
        double[] chiSqPcomp = {3.173105e-01, 1.572992e-01, 8.326452e-02, 4.550026e-02, 2.534732e-02, 1.430588e-02, 8.150972e-03, 4.677735e-03, 2.699796e-03, 1.565402e-03, 9.111189e-04, 5.320055e-04, 3.114910e-04, 1.828106e-04, 1.075112e-04, 6.334248e-05, 3.737982e-05, 2.209050e-05, 1.307185e-05, 7.744216e-06, 4.592834e-06, 2.726505e-06, 1.620014e-06, 9.633570e-07, 5.733031e-07, 3.414174e-07, 2.034555e-07, 1.213155e-07, 7.237830e-08, 4.320463e-08, 2.580284e-08, 1.541726e-08, 9.215887e-09, 5.511207e-09, 3.297053e-09, 1.973175e-09, 1.181292e-09, 7.074463e-10, 4.238055e-10, 2.539629e-10, 1.522292e-10, 9.127342e-11, 5.473986e-11, 3.283759e-11, 1.970344e-11, 1.182530e-11, 7.098670e-12, 4.262192e-12, 2.559625e-12, 1.537460e-12, 9.236597e-13, 5.550063e-13, 3.335484e-13, 2.004896e-13, 1.205298e-13, 7.247102e-14, 4.358119e-14, 2.621178e-14, 1.576720e-14, 9.485738e-15, 5.707481e-15, 3.434573e-15, 2.067066e-15, 1.244192e-15, 7.489807e-16, 4.509230e-16, 2.715071e-16, 1.634955e-16, 9.846344e-17,
                5.930446e-17, 3.572249e-17, 2.151974e-17, 1.296498e-17, 7.811703e-18, 4.707141e-18, 2.836647e-18, 1.709580e-18, 1.030406e-18, 6.210993e-19, 3.744097e-19, 2.257177e-19, 1.360867e-19, 8.205339e-20, 4.947748e-20, 2.983651e-20, 1.799356e-20, 1.085212e-20, 6.545447e-21, 3.948125e-21, 2.381600e-21, 1.436721e-21, 8.667648e-22, 5.229434e-22, 3.155239e-22, 1.903853e-22, 1.148835e-22, 6.932733e-23, 4.183826e-23, 2.525018e-23, 1.523971e-23};

        // Let's see if our ChiSquare is OK
        for (int i = 0; i < chiSqPcomp.length; i++) {
            double x = i + 1;
            double p = FisherExactTest.get().chiSquareCDFComplementary(x, 1);
            double diff = Math.abs(p - chiSqPcomp[i]) / chiSqPcomp[i];
            if (verbose) Log.debug("pvalue: " + p + "\tpvalue (from R): " + chiSqPcomp[i] + "\tdifference: " + diff);
            assertTrue(diff < MAX_DIFF);
        }
    }
}
