package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test cases for HGVS notation on insertions
 */
public class TestCasesIntegrationHgvsDel extends TestCasesIntegrationBase {

    public TestCasesIntegrationHgvsDel() {
        super();
    }

    /**
     * This frameshift caused an exception while processing HGVS protein notation
     */
    @Test
    public void test_01_hgvs_deletions_chr11() {
        Log.debug("Test");

        String genomeName = "testHg19Chr11";
        String vcf = path("test_01_hgvs_deletions_chr11.vcf");

        snpEffect(genomeName, vcf, null);

    }

}
