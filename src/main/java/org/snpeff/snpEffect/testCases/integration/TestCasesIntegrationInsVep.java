package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationInsVep extends TestCasesIntegrationBase {

    @Test
    public void test_03_InsVep() {
        Log.debug("Test");
        compareVep("testENST00000268124", path("testENST00000268124_ins_vep.vcf"), "ENST00000268124");
    }

    @Test
    public void test_04_InsVep() {
        Log.debug("Test");
        compareVep("testHg3770Chr22", path("testENST00000445220_ins_vep.vcf"), "ENST00000445220");
    }

}
