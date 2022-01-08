package org.snpeff.snpEffect.testCases.unity;


import org.junit.jupiter.api.Test;
import org.snpeff.align.NeedlemanWunsch;
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
