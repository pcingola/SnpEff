package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test case HGSV for MNPs
 */
public class TestCasesIntegrationHgvsMnps extends TestCasesIntegrationBase {

    public TestCasesIntegrationHgvsMnps() {
        super();
        shiftHgvs = true;
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String genome = "testHg19Chr17";
        String vcf = path("hgvs_mnps_01.vcf");
        compareHgvs(genome, vcf, false);
    }

    /**
     * Test MNP simplification
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        String genome = "testHg19Chr17";
        String vcf = path("hgvs_mnps_02.vcf");

        // Create SnpEff
        String[] args = {genome, vcf};
        SnpEffCmdEff snpeff = new SnpEffCmdEff();
        snpeff.parseArgs(args);
        snpeff.setDebug(debug);
        snpeff.setVerbose(verbose);
        snpeff.setSupressOutput(!verbose);
        snpeff.setFormatVersion(EffFormatVersion.FORMAT_ANN_1);

        // Run & get result (single line)
        List<VcfEntry> results = snpeff.run(true);
        VcfEntry ve = results.get(0);

        // Check HGVS 'p.' notation
        boolean ok = false;
        for (VcfEffect veff : ve.getVcfEffects()) {
            if (verbose)
                Log.debug("\t" + veff + "\n\t\ttranscript: " + veff.getTranscriptId() + "\n\t\tHgvs (DNA): " + veff.getHgvsDna());
            if (veff.getTranscriptId().equals("NM_001042492.2")) {
                assertEquals("p.Gln1055*", veff.getHgvsProt(), "HGVS p. notation does not match");
                ok = true;
            }
        }

        assertTrue(ok, "Transcript not found");
    }

}
