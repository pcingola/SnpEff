package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test case for parsing ANN fields
 */
public class TestCasesAnnParse extends TestCasesBase {

    public TestCasesAnnParse() {
        super();
    }

    /**
     * Make sure all effect_tpyes have appropriate impacts, regions, etc.
     */
    @Test
    public void test_EffectType() {
        Log.debug("Test");
        for (EffectType eff : EffectType.values()) {
            if (verbose) Log.info("\t" + eff);

            // None of these should throw an exception
            eff.effectImpact();
            eff.getGeneRegion();

            for (EffFormatVersion formatVersion : EffFormatVersion.values()) {
                eff.toSequenceOntology(formatVersion, null);
            }
        }
    }

    @Test
    public void test_old_SO() {
        Log.debug("Test");
        EffectType eff = EffectType.parse(EffFormatVersion.DEFAULT_FORMAT_VERSION, "non_coding_exon_variant");
        assertTrue(eff != null, "Effect type not found");
        assertEquals(eff, EffectType.EXON, "Effect type does not match");
    }

    @Test
    public void test_old_SO_vcf() {
        Log.debug("Test");
        String vcfFile = path("test_old_SO_01.vcf");

        VcfFileIterator vcf = new VcfFileIterator(vcfFile);
        for (VcfEntry ve : vcf) {
            if (verbose) Log.info(ve);
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info(veff.getEffectsStrSo() + "\t" + veff.getEffectType());
                assertEquals(veff.getEffectType(), EffectType.EXON, "Effect type does not match");
            }
        }
    }

    /**
     * Make sure there are no exceptions thrown when parsing TFBS_abalation SO term
     */
    @Test
    public void testCase_tfbs_ablation() {
        Log.debug("Test");
        String vcfFile = path("tfbs_ablation.vcf");
        VcfFileIterator vcf = new VcfFileIterator(vcfFile);

        boolean ok = false;
        for (VcfEntry ve : vcf) {
            if (verbose) Log.info(ve);
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t" + veff.getEffectsStrSo());
                ok |= veff.getEffectsStrSo().indexOf("TFBS_ablation") >= 0;
            }
        }

        assertTrue(ok, "SO term 'TFBS_ablation' not found");
    }

}
