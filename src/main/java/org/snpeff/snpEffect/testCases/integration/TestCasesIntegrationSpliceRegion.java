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
     * Splice region not found in some cases when there is a previous insertion in the VCF
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        List<EffectType> effTypes = snpEffectFilter("testHg19Chr7", path("test.splice_region_01.vcf"), true, 117174416, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * SNV one base before splice acceptor (i.e. intron side): Should NOT find acceptor
     */
    @Test
    public void test_02_splice_region() {
        verbose = true;
        List<EffectType> effTypes = snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174326, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * SNV at first base of a splice acceptor
     */
    @Test
    public void test_03_splice_acceptor() {
        List<EffectType> effTypes = snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174327, EffectType.SPLICE_SITE_ACCEPTOR);
    }

    /**
     * SNV at second base of a splice acceptor
     */
    @Test
    public void test_04_splice_acceptor() {
        List<EffectType> effTypes = snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174328, EffectType.SPLICE_SITE_ACCEPTOR);
    }

    /**
     * SNV one base after splice acceptor (i.e. exon)
     */
    @Test
    public void test_05_splice_region() {
        List<EffectType> effTypes = snpEffectFilter("testHg19Chr7", path("test.splice_acceptor_02.vcf"), true, 117174329, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * INS one base after splice acceptor (i.e. exon)
     */
    @Test
    public void test_06_splice_region_ins() {
        verbose = true;
        List<EffectType> effTypes = snpEffectFilter("test_ENST00000322652.10", path("test.splice_acceptor_ins_06.vcf"), true, 31966144, EffectType.SPLICE_SITE_REGION);
    }

    /**
     * INS many bases before splice acceptor. Re-alignment changes the position to splice acceptor.
     *
     * .   17:31966130-31966130    T   ENSG00000178691 ENST00000322652.10  Transcript  splice_polypyrimidine_tract_variant,intron_variant  -   -   -   -   -   rs79205882  IMPACT=LOW;SYMBOL=SUZ12;BIOTYPE=protein_coding;INTRON=4/15;STRAND=1;SYMBOL_SOURCE=HGNC;HGNC_ID=HGNC:17101;MANE_SELECT=NM_015355.4;TSL=1;APPRIS=P1
     * .   17:31966144-31966144    T   ENSG00000178691 ENST00000322652.10  Transcript  splice_polypyrimidine_tract_variant,splice_region_variant,intron_variant    -   -   -   -   -   -   IMPACT=LOW;SYMBOL=SUZ12;BIOTYPE=protein_coding;INTRON=4/15;STRAND=1;SYMBOL_SOURCE=HGNC;HGNC_ID=HGNC:17101;MANE_SELECT=NM_015355.4;TSL=1;APPRIS=P1
     * .   17:31966145-31966145    T   ENSG00000178691 ENST00000322652.10  Transcript  splice_acceptor_variant -   -   -   -   -   -   IMPACT=HIGH;SYMBOL=SUZ12;BIOTYPE=protein_coding;INTRON=4/15;STRAND=1;SYMBOL_SOURCE=HGNC;HGNC_ID=HGNC:17101;MANE_SELECT=NM_015355.4;TSL=1;APPRIS=P1
     * .   17:31966146-31966146    T   ENSG00000178691 ENST00000322652.10  Transcript  frameshift_variant,splice_region_variant    695-696 455-456 152 S/SX    agc/agTc    -   IMPACT=HIGH;SYMBOL=SUZ12;BIOTYPE=protein_coding;STRAND=1;SYMBOL_SOURCE=HGNC;HGNC_ID=HGNC:17101;MANE_SELECT=NM_015355.4;TSL=1;APPRIS=P1
     * .   17:31966147-31966147    T   ENSG00000178691 ENST00000322652.10  Transcript  frameshift_variant,splice_region_variant    696-697 456-457 152-153 -/X -/T -   IMPACT=HIGH;SYMBOL=SUZ12;BIOTYPE=protein_coding;EXON=5/16;STRAND=1;SYMBOL_SOURCE=HGNC;HGNC_ID=HGNC:17101;MANE_SELECT=NM_015355.4;TSL=1;APPRIS=P1
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        verbose = true;
        String file = path("test.splice_acceptor_ins_02.vcf");
        String[] args = {"-canon"};
        int pos = 117174416;

        // Annotate
        List<VcfEntry> vcfEntries = snpEffect("test_ENST00000322652.10", file, args, EffFormatVersion.FORMAT_ANN_1);
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

        throw new RuntimeException("IMPLEMENT !!!!!!! ");
    }

    /**
     * Deletion one base in splice site
     */
    @Test
    public void test_06_splice_region_del() {
        throw new RuntimeException("UNIMPLEMENTED !!!!!!! ");
    }

}
