package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Test cases: apply a variant (DEL) to a transcript
 */
public class TestCasesApplyDel extends TestCasesBaseApply {

    public TestCasesApplyDel() {
        super();
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_01() {
        Log.debug("Test");
        Variant variant = new Variant(transcript.getParent(), 290, "TTT", "");
        checkApplyDel(variant, transcript.cds(), transcript.protein(), 1, 297, 396);
    }

    /**
     * Variant right before exon start
     */
    @Test
    public void test_apply_variant_02() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 297, "TCC", "");
        checkApplyDel(variant, transcript.cds(), transcript.protein(), 1, 297, 396);
    }

    /**
     * Variant overlapping exon start
     */
    @Test
    public void test_apply_variant_03() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 299, "CTG", "");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyDel(variant, expectedCds, null, 1, 299, 396);
    }

    /**
     * Variant at exon start
     */
    @Test
    public void test_apply_variant_04() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 300, "TGT", "");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyDel(variant, expectedCds, null, 1, 300, 396);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_05() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 310, "TTC", "");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaaacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyDel(variant, expectedCds, null, 1, 300, 396);

    }

    /**
     * Variant right before exon end
     */
    @Test
    public void test_apply_variant_06() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 397, "ACG", "");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctca" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyDel(variant, expectedCds, null, 1, 300, 396);

    }

    /**
     * Variant overlapping exon end
     */
    @Test
    public void test_apply_variant_07() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 398, "CGA", "");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaa".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyDel(variant, expectedCds, null, 1, 300, 397);

    }

    /**
     * Variant right after exon end
     */
    @Test
    public void test_apply_variant_08() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 400, "AAA", "");
        checkApplyDel(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant after exon end
     */
    @Test
    public void test_apply_variant_09() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 410, "AGC", "");
        checkApplyDel(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon and overlaps the whole exon
     */
    @Test
    public void test_apply_variant_10() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent() //
                , 290 //
                , "tttatcgtcctgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacgaaagggagct" //
                , "" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        // Note: Since the original exon 1 is deleted, we check coordinates
        // for exon 2 (that becomes exon 1 in the new transcript)
        checkApplyDel(variant, expectedCds, null, 1, 780, 879);
    }

    /**
     * Variant over exon: variant is larger than exon and starts right at exons start and ends after exon end
     */
    @Test
    public void test_apply_variant_11() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent() //
                , 300 //
                , "TGTTTGGGAATTCACGGGCACGGTTCTGCAGCAAGCTGAATTGGCAGCTCGGCATAAATCCCGACCCCATCGTCACGCACGGATCAATTCATCCTCAACGAAAGGGAGCTAGCGCTGTAC" //
                , "" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        // Note: Since the original exon 1 is deleted, we check coordinates
        // for exon 2 (that becomes exon 1 in the new transcript)
        checkApplyDel(variant, expectedCds, null, 1, 780, 879);
    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon start and end right at exon end
     */
    @Test
    public void test_apply_variant_12() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent()//
                , 280 //
                , "aaccgctaactttatcgtcctgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toUpperCase() //
                , "" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        // Note: Since the original exon 1 is deleted, we check coordinates
        // for exon 2 (that becomes exon 1 in the new transcript)
        checkApplyDel(variant, expectedCds, null, 1, 780, 879);

    }

    /**
     * Variant over exon: variant is on the same coordiantes as exon
     */
    @Test
    public void test_apply_variant_13() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent() //
                , 300 //
                , "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toUpperCase() //
                , "" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        // Note: Since the original exon 1 is deleted, we check coordinates
        // for exon 2 (that becomes exon 1 in the new transcript)
        checkApplyDel(variant, expectedCds, null, 1, 800, 899);
    }

}
