package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for canonical transcript selection
 *
 * @author pcingola
 */
public class TestCasesIntegrationCanonical extends TestCasesIntegrationBase {

    public TestCasesIntegrationCanonical() {
        super();
    }

    /**
     * Test canonical transcripts
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        String geneId = "APOBEC3H";
        String trId = "NM_001166003.2";
        String[] args = {"-canon", "testHg19Chr22", path("empty.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        cmdEff.load();

        Gene gene = cmdEff.getConfig().getSnpEffectPredictor().getGene(geneId);
        Transcript tr = gene.subIntervals().iterator().next();
        assertEquals(trId, tr.getId(), "Expecting transcript ID does not match");
    }

    /**
     * Test Somatic vs Germline
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        String geneId = "APOBEC3H";
        String trId = "NM_181773.4";
        String[] args = {"-canonList", path("canon_geneId2trId_test02.txt"), "testHg19Chr22", path("empty.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        cmdEff.load();

        Gene gene = cmdEff.getConfig().getSnpEffectPredictor().getGene(geneId);
        Transcript tr = gene.subIntervals().iterator().next();
        assertEquals(trId, tr.getId(), "Expecting transcript ID does not match");
    }

}
