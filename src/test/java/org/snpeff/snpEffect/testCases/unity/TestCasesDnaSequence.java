package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.DnaAndQualitySequence;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.DnaSequencePe;
import org.snpeff.binseq.coder.DnaCoder;
import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqVariant;
import org.snpeff.fileIterator.FastqFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestCasesDnaSequence extends TestCasesBase {

    /**
     * Create random changes in a sequence
     */
    String change(String sequence, int numChanges, Random rand) {
        HashSet<Integer> changedPos = new HashSet<>();
        char[] chars = sequence.toCharArray();

        for (int i = 0; i < numChanges; ) {
            int pos = rand.nextInt(chars.length);

            if (!changedPos.contains(pos)) { // Already changed?
                int newCode = rand.nextInt() & 0x03;
                char newBase = DnaCoder.get().toBase(newCode);

                if (chars[pos] != newBase) { // Base is different?
                    chars[pos] = newBase;
                    changedPos.add(pos);
                    i++;
                }
            }
        }

        return new String(chars);
    }

    /**
     * Create random sequences and store them in a DnaSequence.
     * Compare getting a few random bases from the original and DnaSequence sequences.
     */
    public void randDnaSeqGetBasesTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
        Random rand = new Random(seed);

        for (int t = 0; t < numTests; t++) {
            String seq = "";
            int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
            seq = randSeq(len, rand); // Create a random sequence

            DnaSequence bseq = new DnaSequence(seq);

            // Retrieve numTestsPerSeq random bases from the sequence
            for (int i = 0; i < numTestsPerSeq; i++) {
                int randPos = rand.nextInt(len);
                int randLen = rand.nextInt(len - randPos);
                String basesOri = seq.substring(randPos, randPos + randLen);
                String basesBin = bseq.getBases(randPos, randLen);
                assertEquals(basesOri, basesBin);
                if (verbose)
                    Log.info("randDnaSeqGetBasesTest:\tPos: " + randPos + "\t" + "Len: " + randLen + "\t'" + basesOri + "'\t=\t'" + basesBin + "'");
            }
        }
    }

    /**
     * Create random sequences and store them in a DnaSequence.
     * Compare getting a single random base from the original and DnaSequence sequences.
     */
    public void randDnaSeqGetBaseTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
        Random rand = new Random(seed);

        for (int t = 0; t < numTests; t++) {
            String seq = "";
            int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
            seq = randSeq(len, rand); // Create a random sequence

            if (verbose) Log.info("DnaSequence test:" + t + "\tlen:" + len + "\t" + seq);
            DnaSequence bseq = new DnaSequence(seq);

            // Retrieve numTestsPerSeq random bases from the sequence
            for (int i = 0; i < numTestsPerSeq; i++) {
                int randPos = rand.nextInt(len);
                char baseOri = seq.charAt(randPos);
                char baseBin = bseq.getBase(randPos);
                assertEquals(baseOri, baseBin);
            }
        }
    }

    /**
     * Create random sequences and compare to storing them in a DnaSequence
     */
    public void randDnaSeqTest(int numTests, int lenMask, long seed) {
        Random rand = new Random(seed);

        for (int t = 0; t < numTests; t++) {
            String seq = "";
            int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
            seq = randSeq(len, rand); // Create a random sequence

            if (verbose) Log.info("DnaSequence test:" + t + "\tlen:" + len + "\t" + seq);
            DnaSequence bseq = new DnaSequence(seq);
            assertEquals(seq, bseq.toString());
        }
    }

    /**
     * Create a random quality sequence of length 'len'
     */
    String randQual(int len, Random rand) {
        StringBuilder sb = new StringBuilder();
        // Create a random sequence
        for (int i = 0; i < len; i++) {
            int r = rand.nextInt() & 40;
            char qchar = (char) ('!' + r);
            sb.append(qchar);
        }
        return sb.toString();
    }

    /**
     * Create random sequences and store them in a DnaSequence.
     * Compare after replacing random bases from the original and DnaSequence sequences.
     */
    public void randReplaceBaseTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
        Random rand = new Random(seed);

        for (int t = 0; t < numTests; t++) {
            String seq = "";
            int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
            seq = randSeq(len, rand); // Create a random sequence

            DnaSequence bseq = new DnaSequence(seq);

            // Replace numTestsPerSeq random bases from the sequence
            if (verbose) Log.info("randReplaceBaseTest\nOri    :\t" + seq);
            for (int i = 0; i < numTestsPerSeq; i++) {
                // Random position
                int randPos = rand.nextInt(len);
                char baseOri = seq.charAt(randPos);

                // Random base (different than baseOri)
                char randBase = baseOri;
                while (randBase == baseOri) {
                    int r = rand.nextInt() & 0x03;
                    randBase = DnaCoder.get().toBase(r);
                }

                // Replace base in sequence (string)
                char[] seqChars = seq.toCharArray();
                seqChars[randPos] = randBase;
                seq = new String(seqChars);

                // Replace i DnaSequence
                bseq.setBase(randPos, randBase);
                if (verbose)
                    Log.info("Changed:\t" + seq + "\tpos: " + randPos + "\trandbase: " + randBase + "\n\t\t" + bseq);

                // Compare results
                assertEquals(seq, bseq.toString());
            }
        }
    }

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

    /**
     * Create random sequences (and qualities) and compare to storing them in a DnaAndQuality
     */
    public void randTestQual(int numTests, int lenMask, long seed) {
        Random rand = new Random(seed);

        for (int t = 0; t < numTests; t++) {
            String seq = "", qual = "";
            int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
            seq = randSeq(len, rand); // Create a random sequence
            qual = randQual(len, rand); // Create a random quality
            String fullSeq = seq + "\t" + qual;

            if (verbose) Log.info("DnaAndQualitySequence test:" + t + "\tlen:" + len + "\t" + seq);
            DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq, qual, FastqVariant.FASTQ_SANGER);
            if (!fullSeq.equals(bseq.toString()))
                throw new RuntimeException("Sequences do not match:\n\tOriginal:\t" + fullSeq + "\n\tDnaAndQSeq:\t" + bseq);
        }
    }

    @Test
    public void test_01_short() {
        Log.debug("Test");
        long seed = 20100615;
        int lenMask = 0xff;
        int numTests = 1000;
        randDnaSeqTest(numTests, lenMask, seed);
    }

    @Test
    public void test_01_short_getBase() {
        Log.debug("Test");
        long seed = 20110217;
        int lenMask = 0xff;
        int numTests = 1000;
        int numTestsPerSeq = 100;
        randDnaSeqGetBaseTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_01_short_getBases() {
        Log.debug("Test");
        long seed = 20110218;
        int lenMask = 0xff;
        int numTests = 1000;
        int numTestsPerSeq = 100;
        randDnaSeqGetBasesTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_01_short_replaceBase() {
        Log.debug("Test");
        long seed = 20110218;
        int lenMask = 0xff;
        int numTests = 1000;
        int numTestsPerSeq = 100;
        randReplaceBaseTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_02_long() {
        Log.debug("Test");
        long seed = 20100614;
        int lenMask = 0xffff;
        int numTests = 10;
        randDnaSeqTest(numTests, lenMask, seed);
    }

    @Test
    public void test_02_long_getBase() {
        Log.debug("Test");
        long seed = 20110217;
        int lenMask = 0xffff;
        int numTests = 10;
        int numTestsPerSeq = 1000;
        randDnaSeqGetBaseTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_02_long_getBases() {
        Log.debug("Test");
        long seed = 20110218;
        int lenMask = 0xffff;
        int numTests = 10;
        int numTestsPerSeq = 1000;
        randDnaSeqGetBasesTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_02_long_replaceBase() {
        Log.debug("Test");
        long seed = 20110217;
        int lenMask = 0xffff;
        int numTests = 10;
        int numTestsPerSeq = 1000;
        randReplaceBaseTest(numTests, numTestsPerSeq, lenMask, seed);
    }

    @Test
    public void test_04_Pe() {
        Log.debug("Test");
        int numTests = 1000;
        Random rand = new Random(20100617);

        for (int t = 0; t < numTests; t++) {

            // Create gap
            String gapStr = "";
            int gap = rand.nextInt(50) + 1;
            for (int i = 0; i < gap; i++)
                gapStr += "N";

            // Sequence 2
            int len1 = rand.nextInt(100) + 1;
            String seq1 = randSeq(len1, rand);

            // Sequence 2
            int len2 = rand.nextInt(100) + 1;
            String seq2 = randSeq(len2, rand);

            // Final sequence
            String seq = seq1 + gapStr + seq2;

            DnaSequencePe bseqpe = new DnaSequencePe(seq1, seq2, gap);
            if (verbose) Log.info("PE test: " + t + "\t" + bseqpe);

            if (!bseqpe.toString().equals(seq))
                throw new RuntimeException("Sequences do not match:\n\t" + seq + "\n\t" + bseqpe);
        }
    }

    @Test
    public void test_05_fastqReader() {
        Log.debug("Test");
        String fastqFileName = path("fastq_test.fastq");
        String txtFileName = path("fastq_test.txt");

        // Read fastq file
        StringBuilder sb = new StringBuilder();
        for (Fastq fq : new FastqFileIterator(fastqFileName, FastqVariant.FASTQ_ILLUMINA))
            sb.append(fq.getSequence() + "\t" + fq.getQuality() + "\n");
        if (verbose) Log.info("Fastq test:\n" + sb);

        // Read txt file
        String txt = Gpr.readFile(txtFileName);

        // Compare
        if (txt.equals(sb.toString()))
            throw new RuntimeException("Sequences from fastq file does not match expected results:\n----- Fastq file -----" + sb + "\n----- Txt file -----" + txt + "-----");
    }

    @Test
    public void test_05_quality_short() {
        Log.debug("Test");
        long seed = 20100804;
        int lenMask = 0xff;
        int numTests = 1000;
        randTestQual(numTests, lenMask, seed);
    }

    @Test
    public void test_06_quality_long() {
        Log.debug("Test");
        long seed = 20100804;
        int lenMask = 0xffff;
        int numTests = 10;
        randTestQual(numTests, lenMask, seed);
    }

}
