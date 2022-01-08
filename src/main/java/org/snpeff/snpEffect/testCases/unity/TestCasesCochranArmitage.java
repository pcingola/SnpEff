package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.probablility.CochranArmitageTest;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cochran-Armitage test statistic test case
 *
 * @author pcingola
 */
public class TestCasesCochranArmitage {

    @Test
    public void test_01() {
        Log.debug("Test");
        int[] N1 = {20, 20, 20};
        int[] N2 = {10, 20, 30};

        double test = CochranArmitageTest.get().test(N1, N2, CochranArmitageTest.WEIGHT_DOMINANT);
        assertEquals(1.851, test, 0.001);
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        int[] N1 = {20, 20, 20};
        int[] N2 = {10, 20, 30};
        double test = CochranArmitageTest.get().test(N1, N2, CochranArmitageTest.WEIGHT_RECESSIVE);
        assertEquals(-2.108, test, 0.001);
    }

    @Test
    public void test_03() {
        Log.debug("Test");
        int[] N1 = {20, 20, 20};
        int[] N2 = {10, 20, 30};
        double test = CochranArmitageTest.get().test(N1, N2, CochranArmitageTest.WEIGHT_TREND);
        assertEquals(-2.284, test, 0.001);
    }

    @Test
    public void test_04() {
        Log.debug("Test");
        int[] n1 = {17066, 14464, 788, 126, 37};
        int[] n2 = {48, 38, 5, 1, 1};
        double[] w = {1, 2, 3, 4, 5};

        double p = CochranArmitageTest.get().p(n1, n2, w);
        assertEquals(0.088, p, 0.001);
    }

    @Test
    public void test_05() {
        Log.debug("Test");
        int[] n1 = {17066, 14464, 788, 126, 37};
        int[] n2 = {48, 38, 5, 1, 1};
        double[] w = {0, 0.5, 1.5, 4, 8};

        double p = CochranArmitageTest.get().p(n1, n2, w);
        assertEquals(0.0039, p, 0.000001);
    }
}
