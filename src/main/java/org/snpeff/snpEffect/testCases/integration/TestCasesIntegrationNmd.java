package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Test Nonsense mediated decay prediction
 *
 * @author pcingola
 */
public class TestCasesIntegrationNmd extends TestCasesIntegrationBase {

    public TestCasesIntegrationNmd() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");

        // Load database
        String genomeVer = "testHg3766Chr1";
        Log.debug("Loading database '" + genomeVer + "'");
        Config config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
        config.setTreatAllAsProteinCoding(true); // For historical reasons...
        config.loadSnpEffectPredictor();

        // For each gene, transcript, check that NMD works
        int countTest = 1;
        for (Gene gene : config.getGenome().getGenes()) {
            if (verbose) Log.info("NMD test\tGene ID:" + gene.getId());
            for (Transcript tr : gene) {
                if (debug) System.err.println(tr);
                checkNmd(config, gene, tr);

                if (verbose)
                    System.err.print("\tTranscript " + tr.getId() + " " + (tr.isStrandPlus() ? '+' : '-') + " :");
                else Gpr.showMark(countTest++, SHOW_EVERY);

            }
        }
    }
}
