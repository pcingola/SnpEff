package org.snpeff.snpEffect.testCases.unity;


import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Test cases: apply a variant (MIXED) to a transcript
 */
public class TestCasesApplyMixed extends TestCasesBaseApply {

    public TestCasesApplyMixed() {
        super();
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_01_longer() {
        Log.debug("Test");
        Variant variant = new Variant(transcript.getParent(), 290, "TTT", "GCATTA");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 303, 402);
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_01_shorter() {
        Log.debug("Test");
        Variant variant = new Variant(transcript.getParent(), 290, "TTTATC", "ACG");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 297, 396);
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_02_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 294, "TCG", "AGGACG");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 303, 402);
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_02_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 294, "TCGTC", "CG");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 297, 396);
    }

    /**
     * Variant overlapping exon start
     */
    @Test
    public void test_apply_variant_03_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 298, "CCT", "ACGCAA");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "GCAAgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 402);
    }

    /**
     * Variant overlapping exon start
     */
    @Test
    public void test_apply_variant_03_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 298, "CCTGTT", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "Gtgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 396);
    }

    /**
     * Variant at exon start
     */
    @Test
    public void test_apply_variant_04_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 300, "TGT", "ACGAAC");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ACGAACttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant at exon start
     */
    @Test
    public void test_apply_variant_04_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 300, "TGTTTG", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ACGggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 396);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_05_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 310, "TTC", "ACTACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaaACTACGacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_05_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 310, "TTCACG", "ACT");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaaACTggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 396);
    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_06_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 397, "ACG", "GACAGT");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaGAC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_06_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 394, "TCAACG", "GAC");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatccGAC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 396);
    }

    /**
     * Variant overlapping exon end
     */
    @Test
    public void test_apply_variant_07_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 397, "ACG", "TGCATG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaTGC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant overlapping exon end
     */
    @Test
    public void test_apply_variant_07_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 397, "ACGAAA", "TGC");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaTGC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMixed(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant right after exon end
     */
    @Test
    public void test_apply_variant_08_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 400, "AAA", "CATTGC");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant right after exon end
     */
    @Test
    public void test_apply_variant_08_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 400, "AAAGGG", "CAT");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant after exon end
     */
    @Test
    public void test_apply_variant_09_longer() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 410, "AGC", "TCGACT");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant after exon end
     */
    @Test
    public void test_apply_variant_09_shorter() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 410, "AGCGCT", "TCG");
        checkApplyMixed(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

}
