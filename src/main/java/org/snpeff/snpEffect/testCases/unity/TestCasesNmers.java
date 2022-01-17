package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.coder.DnaCoder;
import org.snpeff.nmer.Nmer;
import org.snpeff.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class TestCasesNmers {

    public static boolean verbose = false;

    /**
     * Create a random sequence of length 'len'
     */
    String randSeq(int len, Random rand) {
        StringBuilder sb = new StringBuilder();
        // Create a random sequence
        for (int i = 0; i < len; i++) {
            int r = rand.nextInt() & 0x03;
            sb.append(DnaCoder.get().toBase(r));
        }
        return sb.toString();
    }

    @Test
    public void test_03_Nmers() {
        Log.debug("Test");
        long seed = 20100615;
        Random rand = new Random(seed);

        int numTests = 100;

        // Test all lengths
        for (int len = 1; len <= 32; len++) {
            for (int t = 0; t < numTests; t++) {
                String seq = randSeq(len, rand); // Create a random sequence
                if (verbose) Log.info("Nmer test:" + t + "\tlen:" + len + "\t" + seq);
                Nmer nmer = new Nmer(seq);
                if (!seq.equals(nmer.toString()))
                    throw new RuntimeException("Sequences do not match:\n\tSeq    :\t" + seq + "\n\tBinSeq :\t" + nmer);
            }
        }
    }

    @Test
    public void test_20_Nmers_read_write() {
        Log.debug("Test");
        String testFile = "/tmp/nmer_test.bin";
        int nmerSize = 32;
        int numNmers = 100000;
        try {
            // Initialize
            FileOutputStream os = new FileOutputStream(testFile);
            Random rand = new Random(20100825);
            ArrayList<Nmer> list = new ArrayList<>();

            // Create Nmers and write them to file
            for (int i = 0; i < numNmers; i++) {
                Nmer nmer = new Nmer(nmerSize);
                nmer.setNmer(rand.nextLong());
                list.add(nmer);
                nmer.write(os);
            }
            os.close();

            // Read nmers from file and compare to originals
            Nmer nmer = new Nmer(nmerSize);
            FileInputStream is = new FileInputStream(testFile);
            for (int i = 0; nmer.read(is) >= 0; i++) {
                Nmer nmerOri = list.get(i);
                if (nmerOri.getNmer() != nmer.getNmer())
                    throw new RuntimeException("Nmers differ:\n\t" + nmerOri + "\t" + Long.toHexString(nmerOri.getNmer()) + "\n\t" + nmer + "\t" + Long.toHexString(nmer.getNmer()));
            }
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
