package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test case for sequence ontology
 *
 * @author pcingola
 */
public class TestCasesIntegrationSequenceOntology extends TestCasesIntegrationBase {

    public TestCasesIntegrationSequenceOntology() {
        super();
    }

    @Test
    public void test_01_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000268124", path("testENST00000268124.SNP.vcf"), "ENST00000268124");
    }

    @Test
    public void test_02_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000268124", path("testENST00000268124.SNP.02.vcf"), "ENST00000268124");
    }

    @Test
    public void test_03_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000268124", path("testENST00000268124.Ins.03.vcf"), "ENST00000268124");
    }

    @Test
    public void test_04_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000398332", path("testENST00000398332.Ins.04.vcf"), "ENST00000398332");
    }

    @Test
    public void test_05_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000268124", path("testENST00000268124.Del.05.vcf"), "ENST00000268124");
    }

    @Test
    public void test_06_Vep() {
        Log.debug("Test");
        compareVepSO("testENST00000268124", path("testENST00000268124.Mnp.06.vcf"), "ENST00000268124");
    }

}
