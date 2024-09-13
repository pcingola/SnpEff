package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Test cases for Inversions
 */
public class TestCasesIntegrationInv extends TestCasesIntegrationBase {


    void hasInv(List<VcfEntry> vcfEntries) {
        assertTrue(vcfEntries.size() > 0);
        var found = false;
        for (var vcfEntry : vcfEntries) {
            System.out.println(vcfEntry);
            for (var vcfEffect : vcfEntry.getVcfEffects()) {
                System.out.println("\t" + vcfEffect.getEffectType() + "\t" + vcfEffect);
                if( vcfEffect.getEffectType() == EffectType.CHROMOSOME_LARGE_INVERSION 
                    || vcfEffect.getEffectType() == EffectType.EXON_INVERSION
                    || vcfEffect.getEffectType() == EffectType.EXON_INVERSION_PARTIAL
                    || vcfEffect.getEffectType() == EffectType.TRANSCRIPT_INVERSION
                ) {
                    assertEquals(EffectImpact.HIGH, vcfEffect.getImpact());
                    found = true;
                }
            }
        }
        assertTrue(found);
    }

    @Test
    public void test_01() {
        verbose = true;
        var vcfEntries = snpEffect("testHg3882Chr22", path("test_inv.vcf"));
        hasInv(vcfEntries);
    }

    @Test
    public void test_02() {
        verbose = true;
        var vcfEntries = snpEffect("testHg3882Chr22", path("test_inv_02.vcf"));
        hasInv(vcfEntries);
    }

}
