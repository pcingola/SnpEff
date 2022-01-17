package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationSpliceRegion extends TestCasesIntegrationBase {

    public TestCasesIntegrationSpliceRegion() {
        super();
    }

    /**
     * Splice region not found in some cases when there is an insertion
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        String file = path("test.splice_region_01.vcf");
        String[] args = {"-canon"};
        int pos = 117174416;

        // Annotate
        List<VcfEntry> vcfEntries = snpEffect("testHg19Chr7", file, args, EffFormatVersion.FORMAT_ANN_1);
        if (verbose) vcfEntries.forEach(v -> System.out.println("VcfEffect:" + v));

        // Get variant effects at desired position
        Optional<VcfEffect> oeff = vcfEntries.stream() //
                .filter(v -> v.getStart() == pos) //
                .flatMap(v -> v.getVcfEffects().stream()) //
                .findFirst();

        // Sanity check
        if (verbose) Log.info("VcfEffect:" + oeff);
        assertNotNull(oeff.isPresent(), "Could not find any variant effect at position " + pos);

        // Get effects
        List<EffectType> effTypes = oeff.get().getEffectTypes();
        if (verbose) Log.info("effTypes:" + effTypes);
        assertTrue(effTypes.contains(EffectType.SPLICE_SITE_REGION), "Effect type 'SPLICE_SITE_REGION' not found");
    }

}
