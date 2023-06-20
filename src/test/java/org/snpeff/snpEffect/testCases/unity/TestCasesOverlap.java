package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.overlap.OverlapDnaSeq;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCasesOverlap {

    /**
     * Overlap two sequences and check results
     */
    void checkOverlap(String seq1, String seq2, int score, int offset) {
        DnaSequence bs1 = new DnaSequence(seq1), bs2 = new DnaSequence(seq2);
        OverlapDnaSeq obs = new OverlapDnaSeq();
        obs.setMinOverlap(1); // At least 1 base overlap!
        obs.overlap(bs1, bs2);
        assertEquals(score, obs.getBestScore());
        assertEquals(offset, obs.getBestOffset());
    }

    @Test
    public void test_binSeq_01() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
    }

    @Test
    public void test_binSeq_02() {
        Log.debug("Test");
        checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
    }

    @Test
    public void test_binSeq_03() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
    }

    @Test
    public void test_binSeq_04() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
    }

    @Test
    public void test_binSeq_05() {
        Log.debug("Test");
        checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
    }

    @Test
    public void test_binSeq_06() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
    }

    @Test
    public void test_binSeq_07() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "ttttacgt", 0, 4);
    }

    @Test
    public void test_binSeq_08() {
        Log.debug("Test");
        checkOverlap("ttttacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, -4);
    }

    @Test
    public void test_binSeq_09() {
        Log.debug("Test");
        checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgttttt", 0, -60);
    }

    @Test
    public void test_binSeq_10() {
        Log.debug("Test");
        checkOverlap("acgttttt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 60);
    }

    // Empty sequence
    @Test
    public void test_binSeq_11() {
        Log.debug("Test");
        checkOverlap("", "", 0, 0);
    }

    // No possible overlap (using minOverlap=1)
    @Test
    public void test_binSeq_12() {
        Log.debug("Test");
        checkOverlap("aaaaaaaaaaaaaaaa", "tttttttttttttttt", 1, 15);
    }

    @Test
    public void test_binSeq_13() {
        Log.debug("Test");
        checkOverlap("ttttttttttttttttttttttttttttttttaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccccccccccccccccccccccccccccccccggggggggggggggggggggggggggggggggtttttttttttttttttttttttttttttttt", 0, -32);
    }

}
