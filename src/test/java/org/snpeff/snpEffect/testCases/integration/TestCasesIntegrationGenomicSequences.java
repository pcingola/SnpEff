package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.GenomicSequences;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test case for genomic sequences
 */
public class TestCasesIntegrationGenomicSequences {

    boolean debug = false;
    boolean verbose = false || debug;

    public TestCasesIntegrationGenomicSequences() {
        super();
    }

    /**
     * Check that we can recover sequences from all exons using GneomicSequence class
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        String genome = "testHg3775Chr1";

        // Create SnpEff
        String[] args = {genome};
        SnpEffCmdEff snpeff = new SnpEffCmdEff();
        snpeff.parseArgs(args);
        snpeff.setDebug(debug);
        snpeff.setVerbose(verbose);
        snpeff.setSupressOutput(!verbose);

        // Load genome
        snpeff.load();

        GenomicSequences genomicSequences = snpeff.getConfig().getGenome().getGenomicSequences();
        int i = 1;
        for (Gene g : snpeff.getConfig().getGenome().getGenes()) {
            for (Transcript tr : g) {
                for (Exon ex : tr) {
                    Gpr.showMark(i++, 100);

                    String seq = genomicSequences.querySequence(ex);
                    if (verbose)
                        Log.info(g.getGeneName() + "\t" + tr.getId() + "\t" + ex.getId() + "\n\t" + ex.getSequence() + "\n\t" + seq);

                    // Sanity checks
                    assertNotNull(seq);
                    assertEquals(seq, ex.getSequence());
                }
            }
        }

        System.err.println("\n");
    }

    /**
     * Check that we can recover sequences from all exons using GenomicSequences
     * class, WITHOUT loading sequence form databases
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        String genome = "testHg3775Chr22";

        // Create SnpEff
        String[] args = {genome};
        SnpEffCmdEff snpeff = new SnpEffCmdEff();
        snpeff.parseArgs(args);
        snpeff.setDebug(debug);
        snpeff.setVerbose(verbose);
        snpeff.setSupressOutput(!verbose);

        // Load genome
        snpeff.load();

        // Disable loading
        GenomicSequences genomicSequences = snpeff.getConfig().getGenome().getGenomicSequences();
        genomicSequences.setDisableLoad(true);

        int i = 1;
        for (Gene g : snpeff.getConfig().getGenome().getGenes()) {
            for (Transcript tr : g) {
                for (Exon ex : tr) {
                    Gpr.showMark(i++, 100);
                    String seq = genomicSequences.querySequence(ex);
                    if (verbose)
                        Log.info(g.getGeneName() + "\t" + tr.getId() + "\t" + ex.getId() + "\n\t" + ex.getSequence() + "\n\t" + seq);

                    // Sanity checks
                    assertNotNull(seq);
                    assertEquals(seq, ex.getSequence());
                }
            }
        }

        System.err.println("\n");
    }

}
