package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Log;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Protein translation test case
 *
 * @author pcingola
 */
public class TestCasesIntegrationProtein extends TestCasesIntegrationBase {

    @Test
    public void test_01() throws IOException {
        Log.debug("Test");
        String[] args = {"testHg3763ChrY", path("proteins_testHg3763ChrY.txt")};

        SnpEffCmdProtein cmd = new SnpEffCmdProtein();
        cmd.parseArgs(args);
        cmd.run();

        // Check that it is OK
        assertEquals(0, cmd.getTotalErrors());
        assertTrue(cmd.getTotalOk() >= 167);
    }

    @Test
    public void test_02_start_codon_translate() {
        Log.debug("Test");

        // Initialize
        String genomeName = "testHg19ChrM";
        Config config = new Config(genomeName);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();

        // Find transcript and make sure start codon is 'M'
        boolean checked = false;
        for (Gene g : sep.getGenome().getGenes()) {
            if (verbose) Log.info(g);
            if (g.getId().equals("ENSG00000198763")) {
                Transcript tr = g.iterator().next();
                checked = true;
                assertEquals("MNPLAQPVIYSTIFAGTLITALSSHWFFTWVGLEMNMLAFIPVLTKKMNPRSTEAAIKYFLTQATASMILLMAILFNNMLSGQWTMTNTTNQYSSLMIMMAMAMKLGMAPFHFWVPEVTQGTPLTSGLLLLTWQKLAPISIMYQISPSLNVSLLLTLSILSIMAGSWGGLNQTQLRKILAYSSITHMGWMMAVLPYNPNMTILNLTIYIILTTTAFLLLNLNSSTTTLLLSRTWNKLTWLTPLIPSTLLSLGGLPPLTGFLPKWAIIEEFTKNNSLIIPTIMATITLLNLYFYLRLIYSTSITLLPMSNNVKMKWQFEHTKPTPFLPTLIALTTLLLPISPFMLMIL?", tr.protein());
            }
        }
        assertTrue(checked);
    }

    /**
     * Check proteins using MANE genome with RefSeq IDs and 'NP_*' identifiers in the protein fasta file
     */
    @Test
    public void test_03_build_mane_refseq_check_proteins() {
        var genome = "test_GRCh38.mane.1.0.refseq.chr21";
        var sep = build(genome);
        // Check that all transcripts have protein IDs
        var count = 0;
        for(Gene g:sep.getGenome().getGenes()) {
            for(Transcript tr: g) {
                if( tr.isProteinCoding() ) {
                    Assertions.assertNotNull(tr.getProteinId());
                    count++;
                }
            }
        }
        Assertions.assertEquals(count, 213);
    }


}
