package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnpEnsembl extends TestCasesIntegrationBase {

    public TestCasesIntegrationSnpEnsembl() {
        super();
    }

    @Test
    public void test_01_ENST00000369219() {
        Log.debug("Test");
        String trName = "ENST00000369219";
        String ensemblFile = path(trName + "_ensembl.txt");

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }

    @Test
    public void test_01_ENST00000369368() {
        Log.debug("Test");
        String trName = "ENST00000369368";
        String ensemblFile = path(trName + "_ensembl.txt");

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }

    @Test
    public void test_01_ENST00000415551() {
        Log.debug("Test");
        String ensemblFile = path("ENST00000415551_ensembl.txt");
        String trName = "ENST00000415551";

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }

    @Test
    public void test_01_ENST00000434489() {
        Log.debug("Test");
        String trName = "ENST00000434489";
        String ensemblFile = path(trName + "_ensembl.txt");

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        String ensemblFile = path("ENST00000430575_ensembl.txt");
        String trName = "ENST00000430575";

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }

    @Test
    public void test_03() {
        Log.debug("Test");
        String ensemblFile = path("ENST00000382896_ensembl.txt");
        String trName = "ENST00000382896";

        CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
        compareToEnsembl.compareEnsembl(ensemblFile, trName);
    }
}
