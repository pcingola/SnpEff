package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases: apply a variant (MIXED) to a transcript
 */
public class TestCasesVariantDecompose {

    protected Genome genome;
    protected Chromosome chr;

    public TestCasesVariantDecompose() {
        super();
    }

    @BeforeEach
    public void before() {
        genome = new Genome();
        chr = new Chromosome(genome, 0, 1000, "1");
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_00_decomposeVariant_01() {
        Log.debug("Test");
        Variant variant = new Variant(chr, 300, "TTTATC", "ACG", "MIXED");
        Variant[] variants = variant.decompose();

        assertEquals("chr1:300_TTT/ACG 'MIXED_MNP'", variants[0].toString(), "Variant decomposition MNP part failed");
        assertEquals("chr1:303_ATC/ 'MIXED_DEL'", variants[1].toString(), "Variant decomposition InDel part failed");
    }

    @Test
    public void test_00_decomposeVariant_02() {
        Log.debug("Test");
        Variant variant = new Variant(chr, 300, "TTT", "ACGATC", "MIXED");
        Variant[] variants = variant.decompose();

        assertEquals("chr1:300_TTT/ACG 'MIXED_MNP'", variants[0].toString(), "Variant decomposition MNP part failed");
        assertEquals("chr1:303_/ATC 'MIXED_INS'", variants[1].toString(), "Variant decomposition InDel part failed");
    }

}
