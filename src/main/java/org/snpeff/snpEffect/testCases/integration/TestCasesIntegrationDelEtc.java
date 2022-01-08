package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases on deletions
 *
 * @author pcingola
 */
public class TestCasesIntegrationDelEtc extends TestCasesIntegrationBase {

    /**
     * A deletion having multiple splice_region effects (should show only one)
     */
    @Test
    public void test_01_del_repeated_effects() {
        Log.debug("Test");
        String[] args = {"-ud", "0", "testHg3775Chr1", path("del_multiple_splice_region.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
        snpeff.setSupressOutput(!verbose);
        snpeff.setVerbose(verbose);

        int countEffs = 0;
        boolean repeat = false;

        List<VcfEntry> vcfEnties = snpeff.run(true);
        for (VcfEntry ve : vcfEnties) {

            if (verbose) Log.info(ve);

            // Get first effect (there should be only one)
            List<VcfEffect> veffs = ve.getVcfEffects();

            for (VcfEffect veff : veffs) {
                Set<String> effs = new HashSet<>();
                if (verbose) Log.info("\t" + veff.getEffString());

                // Make sure each effect is unique
                for (String eff : veff.getEffString().split("\\&")) {
                    if (verbose) Log.info("\t\t" + eff);
                    if (!effs.add(eff)) repeat = true;
                    countEffs++;
                }
            }
        }

        assertTrue(countEffs > 0, "No effect annotated");
        assertFalse(repeat, "Duplicated effect");
    }

    /**
     * Insertion on minus strand
     */
    @Test
    public void test_02_del_repeated_effects_gatk() {
        Log.debug("Test");
        String[] args = {"-ud", "0", "-o", "gatk", "testHg3775Chr1", path("del_multiple_splice_region.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
        snpeff.setSupressOutput(!verbose);
        snpeff.setVerbose(verbose);

        int countEffs = 0;

        List<VcfEntry> vcfEnties = snpeff.run(true);
        for (VcfEntry ve : vcfEnties) {

            if (verbose) Log.info(ve);

            // Get first effect (there should be only one)
            List<VcfEffect> veffs = ve.getVcfEffects();

            for (VcfEffect veff : veffs) {
                if (verbose) Log.info("\t" + veff.getEffString());

                // Make sure each effect is unique
                countEffs = 0;
                for (String eff : veff.getEffString().split("\\+")) {
                    if (verbose) Log.info("\t\t" + eff);
                    countEffs++;
                }

                assertEquals(1, countEffs);
            }
        }
    }

}
