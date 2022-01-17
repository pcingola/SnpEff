package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.genotypes.GenotypeVector;
import org.snpeff.util.Log;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test cases for GenotypeVector class
 *
 * @author pcingola
 */
public class TestCasesGenotypeVector {

    boolean verbose = false;

    @Test
    public void test_01() {
        Log.debug("Test");
        // Show masks (just to check they are OK)
        for (byte m : GenotypeVector.mask) {
            String line = "Mask          :" + m + "\t" + Integer.toBinaryString(m & 0xff);
            if (verbose) Log.info(line);
        }

        for (byte m : GenotypeVector.reverseMask) {
            String line = "Reverse Mask  :" + m + "\t" + Integer.toBinaryString(m & 0xff);
            if (verbose) Log.info(line);
        }

        for (int code = 0; code < 4; code++) {
            GenotypeVector gv = new GenotypeVector(2);

            for (int i = 0; i < 4; i++)
                gv.set(i, code);

            for (int i = 0; i < 4; i++)
                assertEquals(code, gv.get(i));
        }
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        Random rand = new Random(20121221);
        GenotypeVector gv = new GenotypeVector(1000);

        // Create random codes
        int[] codes = new int[gv.size()];
        for (int i = 0; i < gv.size(); i++) {
            int code = rand.nextInt(4);
            codes[i] = code;
            gv.set(i, code);
        }

        // Check that codes are stored OK
        for (int i = 0; i < gv.size(); i++) {
            assertEquals(codes[i], gv.get(i));
        }
    }
}
