package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.DnaAndQualitySequence;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.comparator.DnaQualSubsequenceComparator;
import org.snpeff.binseq.comparator.DnaSubsequenceComparator;
import org.snpeff.binseq.comparator.SequenceReference;
import org.snpeff.binseq.indexer.SequenceIndexer;
import org.snpeff.binseq.indexer.SuffixIndexerNmer;
import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqVariant;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.fileIterator.FastqFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCasesSequenceIndexer extends TestCasesBase {

    public static final int NMER_SIZE = 15;

    public static int NUMBER_OF_COMPARISSONS = 100000;

    SequenceIndexer<DnaAndQualitySequence> seqIndex;
    ArrayList<Long> references;

    /**
     * Reads a fastq file, create an indexer and create some random reference for those sequences
     */
    void readFile(String fastqFileName) {
        // Initialize
        seqIndex = new SequenceIndexer<>(new DnaQualSubsequenceComparator(false));
        references = new ArrayList<>();
        int referencesPerSequence = 10;
        int seqIdx = 0;

        // Read file
        for (Fastq fastq : new FastqFileIterator(fastqFileName, FastqVariant.FASTQ_ILLUMINA)) {
            String seq = fastq.getSequence();

            if (seq.indexOf('N') < 0) {
                // Create sequence and add it to indexer
                String qual = fastq.getQuality();
                DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq, qual, FastqVariant.FASTQ_ILLUMINA);
                seqIndex.add(bseq);

                // Create random references to this sequence
                for (int i = 0; i < referencesPerSequence; i++) {
                    int start = (int) (Math.random() * bseq.length());
                    long ref = SequenceReference.getReference(seqIdx, start);
                    references.add(ref);
                }

                seqIdx++;
            }
        }
    }

    /**
     * Sequence comparison test
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        readFile(path("indexer_test_01.fastq"));

        // Compare random references
        for (int i = 0; i < NUMBER_OF_COMPARISSONS; i++) {
            // Pick 2 random references and compare them
            long ref1 = references.get((int) (Math.random() * references.size()));
            long ref2 = references.get((int) (Math.random() * references.size()));
            int comp = seqIndex.compare(ref1, ref2);

            // Check that the comparison is correct
            DnaAndQualitySequence seq1 = seqIndex.get(SequenceReference.getSeqIdx(ref1));
            DnaAndQualitySequence seq2 = seqIndex.get(SequenceReference.getSeqIdx(ref2));

            int start1 = SequenceReference.getStart(ref1);
            int start2 = SequenceReference.getStart(ref2);

            String subseq1 = seq1.getSequence().substring(start1);
            String subseq2 = seq2.getSequence().substring(start2);
            int comp2 = subseq1.compareTo(subseq2);

            if (Math.signum(comp) != Math.signum(comp2))
                throw new RuntimeException("Comparing references: " + ref1 + ", " + ref2 + "\t" + comp + " != " + comp2 + "\n\tseq1:\t" + seq1 + "\n\tseq2:\t" + seq2 + "\n\tsubseq1:\t" + subseq1 + "\n\tsubseq2:\t" + subseq2);
        }
    }

    /**
     * Sequence ordering test
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        readFile("tests/indexer_test_01.fastq");

        // Sort all references
        String latest = "";
        Collections.sort(references, seqIndex);

        // Check that is was sorted correctly
        for (Long ref : references) {
            DnaAndQualitySequence seq = seqIndex.get(SequenceReference.getSeqIdx(ref));
            int start = SequenceReference.getStart(ref);
            Math.min(seq.length() - start, latest.length());
            String subseq = seq.getSequence().substring(start);

            if (subseq.compareTo(latest) < 0)
                throw new RuntimeException("References out of order!\n\t" + SequenceReference.getSeqIdx(ref) + ":" + SequenceReference.getStart(ref) + "\t" + ref + "\t" + subseq + "\n\tLatest: " + latest);
            latest = subseq;
        }
    }

    /**
     * Sequence indexer test (add sequences)
     */
    @Test
    public void test_03() {
        Log.debug("Test");
        String fastqFileName = "tests/short.fastq";

        // Create indexer
        SuffixIndexerNmer<DnaAndQualitySequence> seqIndexNmer = new SuffixIndexerNmer<>(new DnaQualSubsequenceComparator(true), 15);

        // Add all sequences from a file
        for (Fastq fastq : new FastqFileIterator(fastqFileName, FastqVariant.FASTQ_ILLUMINA)) {
            String seq = fastq.getSequence();

            if (seq.indexOf('N') < 0) {
                // Create sequence and add it to indexer
                String qual = fastq.getQuality();
                DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq, qual, FastqVariant.FASTQ_ILLUMINA);
                seqIndexNmer.add(bseq);
            }
        }

        // Sanity check
        seqIndexNmer.sanityCheck();
    }

    /**
     * Sequence indexer test (overlap sequences)
     */
    @Test
    public void test_04() {
        Log.debug("Test");
        String fastqFileName = "tests/short.fastq";

        // Create indexer
        SuffixIndexerNmer<DnaAndQualitySequence> seqIndexNmer = new SuffixIndexerNmer<>(new DnaQualSubsequenceComparator(true), 15);

        // Add & overlap (join) all sequences from a file
        for (Fastq fastq : new FastqFileIterator(fastqFileName, FastqVariant.FASTQ_ILLUMINA)) {
            String seq = fastq.getSequence();

            if (seq.indexOf('N') < 0) {
                // Create sequence and add it to indexer
                String qual = fastq.getQuality();
                DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq, qual, FastqVariant.FASTQ_ILLUMINA);
                seqIndexNmer.add(bseq);

                boolean joined = seqIndexNmer.overlap(bseq); // Try to find the best overlap
                if (!joined) seqIndexNmer.add(bseq); // Nothing found? => add sequence
            }
        }

        // Sanity check
        seqIndexNmer.sanityCheck();
    }

    /**
     * Sequence indexer test (trivial assembly)
     */
    @Test
    public void test_05() {
        Log.debug("Test");
        String fileName = path("a_thaliana_test/assembly_test.fa");
        String result = Gpr.readFile(path("a_thaliana_test/assembly_test.result"));

        // Create an index
        SuffixIndexerNmer<DnaAndQualitySequence> seqIndex = new SuffixIndexerNmer<>(new DnaQualSubsequenceComparator(true), NMER_SIZE);

        // Read all sequences and 'assemble' them
        for (String seq : new FastaFileIterator(fileName)) {
            DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq);
            boolean joined = seqIndex.overlap(bseq); // Try to find the best overlap
            if (!joined) seqIndex.add(bseq); // Nothing found? => add sequence
        }

        // There should be only one sequence in the index (for this test case)
        DnaAndQualitySequence bseq = seqIndex.get(1);
        assertEquals(result.trim().toUpperCase(), bseq.getSequence().trim().toUpperCase());
    }

    /**
     * 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' bug
     * Sequences with nmers '0' were not being indexed
     */
    @Test
    public void test_06() {
        Log.debug("Test");
        String[] seqStr = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}; // Two almost equal sequences (first one is longer)

        SuffixIndexerNmer<DnaSequence> seqIndex = new SuffixIndexerNmer<>(new DnaSubsequenceComparator<>(true, 0), NMER_SIZE);
        for (int i = 0; i < seqStr.length; i++) {
            DnaSequence bseq = new DnaSequence(seqStr[i]);
            if (!seqIndex.overlap(bseq)) seqIndex.add(bseq); // Add or overlap
        }

        assertEquals(seqStr[0], seqIndex.get(1).getSequence());
    }

    /**
     * Sequence length = Nmer size (bug)
     * Sequences with same length as nmers were not being indexed properly
     */
    @Test
    public void test_07() {
        Log.debug("Test");
        int nmerSize = 32;
        String[] seqStr = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Sequence length = 32 (same as Nmer size)
                , "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Same sequence
                , "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac" // Added a 'c' at the end
                , "caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Added a 'c' at the beginning
        }; // Two almost equal sequences (first one is longer)

        SuffixIndexerNmer<DnaSequence> seqIndex = new SuffixIndexerNmer<>(new DnaSubsequenceComparator<>(true, 0), nmerSize);
        for (int i = 0; i < seqStr.length; i++) {
            DnaSequence bseq = new DnaSequence(seqStr[i]);
            if (!seqIndex.overlap(bseq)) seqIndex.add(bseq); // Add or overlap
        }

        assertEquals("caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac", seqIndex.get(1).getSequence());
    }

    /**
     * Sequence "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac"
     */
    @Test
    public void test_08() {
        Log.debug("Test");
        int nmerSize = 32;
        String[] seqStr = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //
                , "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac" // Prepend 'c', append 'g'
        };

        SuffixIndexerNmer<DnaSequence> seqIndex = new SuffixIndexerNmer<>(new DnaSubsequenceComparator<>(true, 0), nmerSize);
        for (int i = 0; i < seqStr.length; i++) {
            DnaSequence bseq = new DnaSequence(seqStr[i]);
            if (!seqIndex.overlap(bseq)) seqIndex.add(bseq); // Add or overlap
        }

        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac", seqIndex.get(1).getSequence());
    }

    /**
     * Sequence "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
     */
    @Test
    public void test_09() {
        Log.debug("Test");
        int nmerSize = 32;
        String[] seqStr = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //
                , "caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Prepend 'c', append 'g'
        };

        SuffixIndexerNmer<DnaSequence> seqIndex = new SuffixIndexerNmer<>(new DnaSubsequenceComparator<>(true, 0), nmerSize);
        for (int i = 0; i < seqStr.length; i++) {
            DnaSequence bseq = new DnaSequence(seqStr[i]);
            if (!seqIndex.overlap(bseq)) seqIndex.add(bseq); // Add or overlap
        }

        assertEquals("caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", seqIndex.get(1).getSequence());
    }

    /**
     * Sequence "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaag"
     */
    @Test
    public void test_10() {
        Log.debug("Test");
        int nmerSize = 32;
        String[] seqStr = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" //
                , "caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaag" // Prepend 'c', append 'g'
        };

        SuffixIndexerNmer<DnaSequence> seqIndex = new SuffixIndexerNmer<>(new DnaSubsequenceComparator<>(true, 0), nmerSize);
        for (int i = 0; i < seqStr.length; i++) {
            DnaSequence bseq = new DnaSequence(seqStr[i]);
            if (!seqIndex.overlap(bseq)) seqIndex.add(bseq); // Add or overlap
        }

        assertEquals("caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaag", seqIndex.get(1).getSequence());
    }

}
