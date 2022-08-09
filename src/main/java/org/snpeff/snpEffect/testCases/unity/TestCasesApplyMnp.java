package org.snpeff.snpEffect.testCases.unity;


import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Test cases: apply a variant (MNP) to a transcript
 */
public class TestCasesApplyMnp extends TestCasesBaseApply {

    public TestCasesApplyMnp() {
        super();
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_01() {
        Variant variant = new Variant(transcript.getParent(), 290, "TTT", "AAA");
        checkApplyMnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant right before exon start
     */
    @Test
    public void test_apply_variant_02() {
        Variant variant = new Variant(transcript.getParent(), 297, "TCC", "ACG");
        checkApplyMnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant overlapping exon start
     */
    @Test
    public void test_apply_variant_03() {
        Variant variant = new Variant(transcript.getParent(), 299, "CTG", "GAC");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ACtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1], changed bases 'AC'
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant at exon start
     */
    @Test
    public void test_apply_variant_04() {
        Variant variant = new Variant(transcript.getParent(), 300, "TGT", "ACA");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ACAttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_05() {
        Variant variant = new Variant(transcript.getParent(), 310, "TTC", "AAG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaaAAGacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);

    }

    /**
     * Variant right before exon end
     */
    @Test
    public void test_apply_variant_06() {
        Variant variant = new Variant(transcript.getParent(), 397, "ACG", "TGC");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaTGC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);

    }

    /**
     * Variant overlapping exon end
     */
    @Test
    public void test_apply_variant_07() {
        Variant variant = new Variant(transcript.getParent(), 398, "CGA", "GCT");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaaGC".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);

    }

    /**
     * Variant right after exon end
     */
    @Test
    public void test_apply_variant_08() {
        Variant variant = new Variant(transcript.getParent(), 400, "AAA", "TTT");
        checkApplyMnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant after exon end
     */
    @Test
    public void test_apply_variant_09() {
        Variant variant = new Variant(transcript.getParent(), 410, "AGC", "TCG");
        checkApplyMnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon and overlaps the whole exon
     */
    @Test
    public void test_apply_variant_10() {
        Variant variant = new Variant(transcript.getParent() //
                , 290 //
                , "tttatcgtcctgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacgaaagggagct" //
                , "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "CGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAG".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant over exon: variant is larger than exon and starts right at exons start and ends after exon end
     */
    @Test
    public void test_apply_variant_11() {
        Variant variant = new Variant(transcript.getParent() //
                , 300 //
                , "TGTTTGGGAATTCACGGGCACGGTTCTGCAGCAAGCTGAATTGGCAGCTCGGCATAAATCCCGACCCCATCGTCACGCACGGATCAATTCATCCTCAACGAAAGGGAGCTAGCGCTGTAC" //
                , "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT" //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATG".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);
    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon start and end right at exon end
     */
    @Test
    public void test_apply_variant_12() {
        Variant variant = new Variant(transcript.getParent()//
                , 280 //
                , "aaccgctaactttatcgtcctgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toUpperCase() //
                , "attggctcgacgctcattcaCTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT".toUpperCase() //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "CTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);

    }

    /**
     * Variant over exon: variant is on the same coordiantes as exon
     */
    @Test
    public void test_apply_variant_13() {
        Variant variant = new Variant(transcript.getParent() //
                , 300 //
                , "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toUpperCase() //
                , "CTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT".toUpperCase() //
        );

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "CTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyMnp(variant, expectedCds, null, 1, 300, 399);
    }

}
