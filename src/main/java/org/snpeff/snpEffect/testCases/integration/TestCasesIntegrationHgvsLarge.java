package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationHgvsLarge extends TestCasesIntegrationBase {

    /**
     * Using non-standard splice size (15 instead of 2)
     * may cause some HGVS annotations issues
     */
    @Test
    public void test_13_large_Del_Hgvs() {
        Log.debug("Test");
        String genome = "testHg3775Chr22";
        String vcf = path("test_large_del_hgvs_13.vcf");

        // Create SnpEff
        String[] args = {genome, vcf};
        SnpEffCmdEff snpeff = new SnpEffCmdEff();
        snpeff.parseArgs(args);
        snpeff.setDebug(debug);
        snpeff.setVerbose(verbose);
        snpeff.setSupressOutput(!verbose);
        snpeff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

        // Run & get result (single line)
        List<VcfEntry> results = snpeff.run(true);
        VcfEntry ve = results.get(0);

        // Make sure HGVS string is not so long
        for (VcfEffect veff : ve.getVcfEffects()) {
            if (verbose) Log.info(veff);

            if (verbose) Log.info("\tAA change    : " + veff.getAa());
            assertTrue(veff.getAa() == null || veff.getAa().length() < 100);

            if (verbose) Log.info("\tCodon change : " + veff.getCodon());
            assertTrue(veff.getCodon() == null || veff.getCodon().length() < 100);

        }
    }

}
