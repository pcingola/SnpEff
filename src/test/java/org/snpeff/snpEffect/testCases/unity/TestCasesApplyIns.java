package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Test cases: apply a variant (INS) to a transcript
 */
public class TestCasesApplyIns extends TestCasesBaseApply {

    public TestCasesApplyIns() {
        super();
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_01() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 290, "", "ACG");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 303, 402);
    }

    /**
     * Variant before exon
     */
    @Test
    public void test_apply_variant_02() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 297, "", "ACG");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 303, 402);
    }

    /**
     * Variant overlapping exon start
     */
    @Test
    public void test_apply_variant_03() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 299, "", "ACG");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 303, 402);
    }

    /**
     * Variant at exon start
     */
    @Test
    public void test_apply_variant_04() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 300, "", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "ACGtgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_05() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 310, "", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaaACGttcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant in exon
     */
    @Test
    public void test_apply_variant_06() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 399, "", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacACGg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant overlapping exon end
     */
    @Test
    public void test_apply_variant_07() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 399, "", "ACG");

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacACGg".toLowerCase() // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 402);

    }

    /**
     * Variant right after exon end
     */
    @Test
    public void test_apply_variant_08() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 400, "", "ACG");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant after exon end
     */
    @Test
    public void test_apply_variant_09() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 410, "", "ACG");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon and overlaps the whole exon
     */
    @Test
    public void test_apply_variant_10() {
        Log.debug("Test");

        Variant variant = new Variant(transcript.getParent(), 290, "", "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT");
        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 420, 519);
    }

    /**
     * Variant over exon: variant is larger than exon and starts right at exons start and ends after exon end
     */
    @Test
    public void test_apply_variant_11() {
        Log.debug("Test");

        String seq = "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAGGTGAAGGCAT";
        Variant variant = new Variant(transcript.getParent(), 300, "", seq);

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + seq.toLowerCase() + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 519);

    }

    /**
     * Variant over exon: variant is larger than exon, starts before exon start and end right at exon end
     */
    @Test
    public void test_apply_variant_12() {
        Log.debug("Test");

        String seq = "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGACATCCGCAG";
        Variant variant = new Variant(transcript.getParent(), 290, "", seq);

        checkApplyIns(variant, transcript.cds(), transcript.protein(), 1, 410, 509);

    }

    /**
     * Variant over exon: variant is on the same coordiantes as exon
     */
    @Test
    public void test_apply_variant_13() {
        Log.debug("Test");

        String seq = "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATG";
        Variant variant = new Variant(transcript.getParent(), 300, "", seq);

        String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
                + seq.toLowerCase() + "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg" // Exon[1]
                + "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
                ;

        checkApplyIns(variant, expectedCds, null, 1, 300, 499);
    }

}
