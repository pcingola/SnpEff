package org.snpeff.snpEffect.testCases.unity;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.junit.jupiter.api.Test;
import org.snpeff.gsa.ScoreList;
import org.snpeff.gsa.ScoreList.ScoreSummary;
import org.snpeff.util.Log;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * GenePvalueList statistics test case
 *
 * @author pcingola
 */
public class TestCasesGenePvalueList {

    public static final double EPSILON = 1e-10;

    /**
     * Combined p-value : MIN
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        double[] pvals = {0.01, 0.2, 0.3};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.MIN);
        assertEquals(0.01, pvalue, EPSILON);
    }

    /**
     * Combined p-value : AVG
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        double[] pvals = {0.01, 0.2, 0.3};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.AVG);
        assertEquals(0.17, pvalue, EPSILON);
    }

    /**
     * Combined p-value : AVG10
     */
    @Test
    public void test_03() {
        Log.debug("Test");
        double[] pvals = {0.01, 0.9, 0.2, 0.9, 0.3, 0.9, 0.01, 0.9, 0.2, 0.9, 0.3, 0.9, 0.01, 0.9, 0.2, 0.9, 0.3, 0.9, 0.17, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9, 0.9};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.AVG_MIN_10);
        assertEquals(0.17, pvalue, EPSILON);
    }

    /**
     * Complementary CDF for Chi^2 distribution
     */
    @Test
    public void test_04() {
        Log.debug("Test");
        Random rand = new Random(20130609);

        // Create many random tests
        for (int i = 0; i < 100000; i++) {
            // Random degrees of freedom
            int degOfFreedom = rand.nextInt(20) + 1;

            // Random chi^2
            double chi2 = 0;
            for (int j = 0; j < degOfFreedom; j++) {
                double z = rand.nextGaussian();
                chi2 += z * z;
            }

            // Calculate complementary probabilities
            double pval = ScoreList.chiSquareCDFComplementary(chi2, degOfFreedom);
            double prob = new ChiSquaredDistribution(degOfFreedom).cumulativeProbability(chi2);

            // Assert that statistics add to 1.0
            assertEquals(1.0, pval + prob, EPSILON);
        }
    }

    /**
     * Combined p-value : FISHER_CHI_SQUARE
     */
    @Test
    public void test_05() {
        Log.debug("Test");
        double[] pvals = {0.01, 0.2, 0.3};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.FISHER_CHI_SQUARE);
        assertEquals(0.021561751324834642, pvalue, EPSILON);
    }

    /**
     * Combined p-value : Z_SCORES
     */
    @Test
    public void test_06() {
        Log.debug("Test");
        double[] pvals = {0.01, 0.2, 0.3};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.Z_SCORES);
        assertEquals(0.01651203260896289, pvalue, EPSILON);
    }

    /**
     * Combined p-value : FDR
     * <p>
     * Reference for this test: http://stat.ethz.ch/R-manual/R-devel/library/stats/html/p.adjust.html
     * <p>
     * R code:
     * set.seed(123)
     * x <- rnorm(50, mean = c(rep(0, 25), rep(3, 25)))
     * p <- 2*pnorm(sort(-abs(x)))
     * p.adjust(p, "fdr")
     */
    @Test
    public void test_07() {
        Log.debug("Test");

        double[] pvals = {2.354054e-07, 2.101590e-05, 2.576842e-05, 9.814783e-05, 1.052610e-04, 1.241481e-04, 1.325988e-04, 1.568503e-04, 2.254557e-04, 3.795380e-04, 6.114943e-04, 1.613954e-03, 3.302430e-03, 3.538342e-03, 5.236997e-03, 6.831909e-03, 7.059226e-03, 8.805129e-03, 9.401040e-03, 1.129798e-02, 2.115017e-02, 4.922736e-02, 6.053298e-02, 6.262239e-02, 7.395153e-02, 8.281103e-02, 8.633331e-02, 1.190654e-01, 1.890796e-01, 2.058494e-01, 2.209214e-01, 2.856000e-01, 3.048895e-01, 4.660682e-01, 4.830809e-01, 4.921755e-01, 5.319453e-01, 5.751550e-01, 5.783195e-01, 6.185894e-01, 6.363620e-01, 6.448587e-01, 6.558414e-01, 6.885884e-01, 7.189864e-01, 8.179539e-01, 8.274487e-01, 8.971300e-01, 9.118680e-01, 9.437890e-01};

        // Create p values
        ScoreList gpl = new ScoreList();
        for (double pval : pvals)
            gpl.add(pval);

        // Check pvalues
        double pvalue = gpl.score(ScoreSummary.FDR);
        assertEquals(0.028244949999999998, pvalue, EPSILON);

        // Check p-value for the second and third entries in the array
        /*
         * > cbind( p, p.adjust(p, "fdr"))
         * 			 [1,] 2.354054e-07 1.177027e-05
         * 			 [2,] 2.101590e-05 4.294736e-04
         * 			 [3,] 2.576842e-05 4.294736e-04		<-- Same as previous line (this is because an adjusted p-value cannot decrease)
         */
        pvalue = gpl.pValueFdr(0.0005);
        assertEquals(4.294736666666667E-4, pvalue, EPSILON);
        pvalue = gpl.pValueFdr(0.0006);
        assertEquals(4.294736666666667E-4, pvalue, EPSILON);

    }

    /**
     * Test quantile
     */
    @Test
    public void test_08() {
        Log.debug("Test");
        // Create pvalues
        ScoreList pvlist = new ScoreList();
        int max = 1000;
        for (int i = 0; i < max; i++) {
            double quantile = ((double) i) / max;
            pvlist.add(quantile);
        }

        // Test
        for (int i = 0; i < max; i++) {
            double quantile = ((double) i) / max;
            double pval = pvlist.quantile(quantile);
            assertEquals(quantile, pval, EPSILON); // Make sure they match
        }
    }

    /**
     * Test CDF (cumulative distribution function)
     */
    @Test
    public void test_09() {
        Log.debug("Test");
        // Create pvalues
        ScoreList pvlist = new ScoreList();
        int max = 1000;
        for (int i = 0; i < max; i++) {
            double quantile = ((double) i) / max;
            pvlist.add(quantile);
        }

        // Test
        for (int i = 0; i < max; i++) {
            double quantile = ((double) i) / max;
            double pval = pvlist.cdf(quantile);
            assertEquals(quantile, pval, EPSILON); // Make sure they match
        }
    }

}
