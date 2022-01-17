package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for other 'effect' issues
 *
 * @author pcingola
 */
public class TestCasesIntegrationEff extends TestCasesIntegrationBase {

    public TestCasesIntegrationEff() {
        super();
    }

    /**
     * Test output order
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", path("eff_sort.vcf"), null);

        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            EffectImpact impPrev = EffectImpact.HIGH;
            for (VcfEffect veff : ve.getVcfEffects()) {
                EffectImpact imp = veff.getImpact();

                if (verbose) Log.info("\t" + imp + "\t" + impPrev + "\t" + imp.compareTo(impPrev) + "\t" + veff);
                assertTrue(impPrev.compareTo(imp) <= 0); // Higher impact go first
                impPrev = imp;
            }
        }
    }

    /**
     * Test output order: Canonical first
     */
    @Test
    public void test_01_canonical() {
        Log.debug("Test");
        List<VcfEntry> vcfEntries = snpEffect("testHg3775Chr8", path("eff_sort_canon.vcf"), null);

        // Only one entry in this file
        assertEquals(1, vcfEntries.size());

        VcfEntry ve = vcfEntries.get(0);
        VcfEffect veff = ve.getVcfEffects().get(0);

        assertEquals("ENST00000456015", veff.getTranscriptId());
    }

    /**
     * Test GATK option: At most one effect per VCF entry
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        String[] args = {"-o", "gatk"};
        List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", path("eff_sort.vcf"), args);

        for (VcfEntry ve : vcfEntries) {
            int numEffs = ve.getVcfEffects().size();
            if (verbose) Log.info("Num effects:" + numEffs + "\t" + ve);
            assertTrue(numEffs <= 1);
        }
    }

    /**
     * Make sure that empty VCF does not trigger an exception when creating the summary
     */
    @Test
    public void test_03_EmptyVcf() {
        Log.debug("Test");
        String[] args = {"eff", "-noLog"};
        snpEffect("testHg3770Chr22", path("empty_only_header.vcf"), args);
    }

    /**
     * Test that CSV summary does not throw any error
     */
    @Test
    public void test_04() {
        Log.debug("Test");
        String[] args = {"-csvStats", "test_04_TestCasesEff.csv"};
        snpEffect("testHg3770Chr22", path("eff_sort.vcf"), args);
    }

    /**
     * GATK mode should not have SPLICE_REGION (it is currently not supported)
     */
    @Test
    public void test_05() {
        Log.debug("Test");
        String genomeName = "testHg3775Chr1";
        String vcf = path("gatk_NO_splice_regions.vcf");
        String[] args = {"eff", "-noLog", "-o", "gatk"};
        List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t'" + veff.getEffectsStr() + "'\t" + veff);
                if (veff.getEffectsStr().contains("SPLICE_SITE_REGION"))
                    throw new RuntimeException("Splice region effects should not present in GATK compatible mode");
            }
        }
    }

    /**
     * Test an MNP at the end of the transcript: We should be able to annotate without throwing any error
     */
    @Test
    public void test_06() {
        Log.debug("Test");
        String[] args = {};
        List<VcfEntry> list = snpEffect("testHg3775Chr15", path("mnp_insertion_at_transcript_end.vcf"), args);

        // We should be able to annotate this entry (if INFO is empty, something went wrong)
        assertFalse(list.get(0).getInfoStr().isEmpty());
    }

    /**
     * Test an MNP at the end of the transcript: We should be able to annotate without throwing any error
     */
    @Test
    public void test_07() {
        Log.debug("Test");
        String[] args = {};
        List<VcfEntry> list = snpEffect("testHg3775Chr10", path("mnp_deletion.vcf"), args);

        // We should be able to annotate this entry (if INFO is empty, something went wrong)
        assertFalse(list.get(0).getInfoStr().isEmpty());
    }

    /**
     * Fixing bug: GATK does not annotate all VCF entries
     */
    @Test
    public void test_08_gatk_missing_annotations() {
        Log.debug("Test");

        String genomeName = "testMycobacterium_tuberculosis_CCDC5079_uid203790";
        String vcf = path("test_gatk_no_annotations.vcf");
        String[] args = {"-noLog", "-o", "gatk"};
        List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

        for (VcfEntry ve : vcfEntries) {
            int count = 0;
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t'" + veff.getEffectsStr() + "'\t" + veff);
                count++;
            }

            // Check that there is one and only one annotation
            assertEquals(1, count);
        }
    }

}
