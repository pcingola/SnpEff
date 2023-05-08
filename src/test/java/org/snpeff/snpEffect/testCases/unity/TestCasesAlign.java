package org.snpeff.snpEffect.testCases.unity;


import org.junit.jupiter.api.Test;
import org.snpeff.align.NeedlemanWunsch;
import org.snpeff.align.StringDiff;
import org.snpeff.align.VcfRefAltAlign;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test cases for Sequence alignment
 *
 * @author pcingola
 */
public class TestCasesAlign {

    boolean verbose = false;

    @Test
    public void test_01() {
        Log.debug("Test");
        String[] as = {"TTT", "TTTGTT", "GCG", "G"};
        String[] bs = {"TTTGTT", "TTT", "G", "GCG"};
        String[] res = {"-GTT", "+GTT", "+CG", "-CG"};
        int[] offset = {3, 3, 1, 1};

        for (int i = 0; i < as.length; i++) {
            String a = as[i];
            String b = bs[i];
            NeedlemanWunsch align = new NeedlemanWunsch(a, b);
            if (verbose)
                Log.info("---------------------------------------- " + align.getClass().getSimpleName() + ": " + i + " ----------------------------------------");
            align.align();
            if (verbose)
                Log.info("a    : '" + a + "'\nb    : '" + b + "'\nAlign: '" + align.getAlignment() + "'" + "\tOffset: " + align.getOffset() + "\n");

            assertEquals(res[i], align.getAlignment());
            assertEquals(offset[i], align.getOffset());
        }
    }

    @Test
    public void test_StringDiff_01() {
        var s1 = "acgtactgatagtcagtcatgctagtcagtcgatctagtcgacatcatgctagcgatcatg";
        var di = "                                                             ";
        var s2 = "acgtactgatagtcagtcatgctagtcagtcgatctagtcgacatcatgctagcgatcatg";
        var sdToStr = s1 + "\n" + di + "\n" + s2 + "\n";
        StringDiff sd = new StringDiff(s1, s2);
        assertEquals(0, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_StringDiff_02() {
        var s1 = "acgtactgatagtcagtcatgctagtcagtcgatctagtcgacatcatgctagcgatcatg";
        var di = "                              ";
        var s2 = "acgtactgatagtcagtcatgctagtcagt";
        var sdToStr = s1 + "\n" + di + "\n" + s2 + "\n";
        StringDiff sd = new StringDiff(s1, s2);
        assertEquals(0, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_StringDiff_03() {
        var s1 = "acgtactgatagtcagtcatgctagtcagtcgatctagtcgacatcatgctagcgatcatg";
        var di = "|                             ";
        var s2 = "Xcgtactgatagtcagtcatgctagtcagt";
        var sdToStr = s1 + "\n" + di + "\n" + s2 + "\n";
        StringDiff sd = new StringDiff(s1, s2);
        assertEquals(1, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_StringDiff_04() {
        var s1 = "acgtactgatagtcagtcatgctagtcXgtcgatctagtcgacatcatgctagcgatcatg";
        var di = "|                          | |";
        var s2 = "XcgtactgatagtcagtcatgctagtcagX";
        var sdToStr = s1 + "\n" + di + "\n" + s2 + "\n";
        StringDiff sd = new StringDiff(s1, s2);
        assertEquals(3, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_StringDiff_05() {
        var s1 = "XcgtactgatagtcagtcatgctagtcagX";
        var di = "|                          | |";
        var s2 = "acgtactgatagtcagtcatgctagtcXgtcgatctagtcgacatcatgctagcgatcatg";
        var sdToStr = s1 + "\n" + di + "\n" + s2 + "\n";
        StringDiff sd = new StringDiff(s1, s2);
        assertEquals(3, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_StringDiff_06() {
        var s1 = "acgtactgatagtcagtcatgctagtcXgtcgatctagtcgacatcatgctagcgatcatg";
        var di = "|                          | |";
        var s2 = "XcgtactgatagtcagtcatgctagtcagX";
        StringDiff sd = new StringDiff(s1, "Sequence 1", s2, "Name2");
        var sdToStr = "Sequence 1 : " + s1 + "\n      diff : " + di + "\n     Name2 : " + s2 + "\n";
        Log.debug("\n" + sd);
        assertEquals(3, sd.count(), "Mismatches count error");
        assertEquals(sdToStr, sd.toString(), "Mismatches count error");
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        String[] as = {"TTT", "TTTGTT", "GCG", "G"};
        String[] bs = {"TTTGTT", "TTT", "G", "GCG"};
        String[] res = {"-GTT", "+GTT", "+CG", "-CG"};
        int[] offset = {3, 3, 1, 1};

        for (int i = 0; i < as.length; i++) {
            String a = as[i];
            String b = bs[i];
            VcfRefAltAlign align = new VcfRefAltAlign(a, b);
            if (verbose)
                Log.info("---------------------------------------- " + align.getClass().getSimpleName() + ": " + i + " ----------------------------------------");
            align.align();
            if (verbose)
                Log.info("a    : '" + a + "'\nb    : '" + b + "'\nAlign: '" + align.getAlignment() + "'" + "\tOffset: " + align.getOffset() + "\n");

            assertEquals(res[i], align.getAlignment());
            assertEquals(offset[i], align.getOffset());
        }
    }

}
