package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesIntegrationZzz2 extends TestCasesIntegrationBase {

    @Test
    public void test_01() {
        verbose = true;
        var vcfEntries = snpEffect("testHg3882Chr22", path("test_inv.vcf"));
        
        if (verbose) {
            for (var vcfEntry : vcfEntries) {
                System.out.println(vcfEntry);
                for (var vcfEffect : vcfEntry.getVcfEffects()) {
                    System.out.println("\t" + vcfEffect);
                }
            }
        }

        assertTrue(vcfEntries.size() > 0);
    }

}
