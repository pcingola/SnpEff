package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.DnaSequenceByte;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCasesDnaSequenceByte {

    public static boolean verbose = false;

    @Test
    public void test_01() {
        Log.debug("Test");
        Random random = new Random(20120907);
        for (int len = 1; len < 1000; len++) {
            for (int i = 0; i < 10; i++) {
                String seq = GprSeq.randSequence(random, len);
                DnaSequenceByte dna = new DnaSequenceByte(seq);

                if (verbose) Log.info("Len: " + len + "\t" + seq + "\t" + dna);

                assertEquals(seq, dna.toString());
            }
        }
    }

}
