package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

    /**
     * Run SnpEff prediction and filter effects from results (VCF)
     */
    public List<EffectType> snpEffectFilter(String genome, String vcfInputFile, boolean useCanonical, int filterPos, EffectType filterEffType) {
        String[] argsCanon = {"-canon"};
        String[] args = (useCanonical ? argsCanon : null);

        // Annotate
        List<VcfEntry> vcfEntries = snpEffect(genome, vcfInputFile, args);
        if (verbose) vcfEntries.forEach(v -> System.out.println("VcfEffect:" + v));

        // Get variant effects at desired position
        Optional<VcfEffect> oeff = vcfEntries.stream() //
                .filter(v -> v.getStart() == filterPos) //
                .flatMap(v -> v.getVcfEffects().stream()) //
                .findFirst();

        // Sanity check
        if (verbose) Log.info("VcfEffect:" + oeff);
        assertTrue(oeff.isPresent(), "Could not find any variant effect at position " + filterPos);

        List<EffectType> effTypes = oeff.get().getEffectTypes();
        if (verbose) Log.info("effTypes:" + effTypes);
        assertTrue(effTypes.contains(filterEffType), "Effect type '" + filterEffType + "' not found");

        return effTypes;
    }

    /**
     * Splice region not found in some cases when there is a previous insertion in the VCF
     */
    @Test
    public void test_01() {
        verbose = true;
        Log.debug("Test");
        snpEffectFilter("testHg19Chr7", path("test.splice_region_01.vcf"), true, 117174416, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * SNV one base before splice acceptor (i.e. intron side): Should NOT find acceptor
     */
    @Test
    public void test_02_splice_region() {
        verbose = true;
        snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174326, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * SNV at first base of a splice acceptor
     */
    @Test
    public void test_03_splice_acceptor() {
        snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174327, EffectType.SPLICE_SITE_ACCEPTOR);
    }

    /**
     * SNV at second base of a splice acceptor
     */
    @Test
    public void test_04_splice_acceptor() {
        snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174328, EffectType.SPLICE_SITE_ACCEPTOR);
    }

    /**
     * SNV one base after splice acceptor (i.e. exon)
     */
    @Test
    public void test_05_splice_region() {
        snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174329, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * INS one base after splice acceptor (i.e. exon)
     */
    @Test
    public void test_06_splice_region_ins() {
//        verbose = true;
        // List<EffectType> effTypes = snpEffectFilter("test_ENST00000322652.10", path("test.splice_acceptor_ins_06.vcf"), true, 31966144, EffectType.SPLICE_SITE_REGION);
        throw new RuntimeException("UNIMPLEMENTED!!!");
    }

    /**
     * INS many bases before splice acceptor. Re-alignment changes the position to splice acceptor.
     */
    @Test
    public void test_02() {
        Log.debug("Test");
//        verbose = true;
//        String file = path("test.splice_acceptor_ins_02.vcf");
//        String[] args = {"-canon"};
//        int pos = 117174416;
//
//        // Annotate
//        List<VcfEntry> vcfEntries = snpEffect("test_ENST00000322652.10", file, args, EffFormatVersion.FORMAT_ANN_1);
//        if (verbose) vcfEntries.forEach(v -> System.out.println("VcfEffect:" + v));
//
//        // Get variant effects at desired position
//        Optional<VcfEffect> oeff = vcfEntries.stream() //
//                .filter(v -> v.getStart() == pos) //
//                .flatMap(v -> v.getVcfEffects().stream()) //
//                .findFirst();
//
//        // Sanity check
//        if (verbose) Log.info("VcfEffect:" + oeff);
//        assertNotNull(oeff.isPresent(), "Could not find any variant effect at position " + pos);
//
//        // Get effects
//        List<EffectType> effTypes = oeff.get().getEffectTypes();
//        if (verbose) Log.info("effTypes:" + effTypes);
//        assertTrue(effTypes.contains(EffectType.SPLICE_SITE_REGION), "Effect type 'SPLICE_SITE_REGION' not found");

        throw new RuntimeException("UNIMPLEMENTED !!!!!!! ");
    }

    /**
     * Deletion one base in splice site
     */
    @Test
    public void test_06_splice_region_del() {
        throw new RuntimeException("UNIMPLEMENTED !!!!!!! ");
    }

}
