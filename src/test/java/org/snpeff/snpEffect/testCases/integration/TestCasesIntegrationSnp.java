package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test SNP variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnp extends TestCasesIntegrationBase {

    /**
     * Change of start codon to an alternative start codon
     */
    @Test
    public void test_02_Start_NonSyn() {
        Log.debug("Test");
        String genome = "testHg19ChrM";
        String vcf = path("test_chrM_start_codon_nonSyn.vcf");

        String[] args = {"-noLog", "-classic", "-ud", "0", genome, vcf};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        snpEff.setDebug(debug);

        SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
        boolean checked = false;
        List<VcfEntry> vcfEntries = seff.run(true);
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t\t" + veff);
                if (veff.getEffectType() == EffectType.NON_SYNONYMOUS_START) {
                    assertEquals(EffectImpact.LOW, veff.getImpact());
                    checked = true;
                }
            }
        }
        assertTrue(checked);

    }

    /**
     * Stop gained should have 'HIGH' impact
     */
    @Test
    public void test_02_StopGained_HighImpact() {
        Log.debug("Test");
        String genome = "testHg3775Chr2";
        String vcf = path("stop_gained_chr2.vcf");

        String[] args = {"-noLog", "-classic", genome, vcf};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        snpEff.setDebug(debug);

        SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
        boolean checked = false;
        List<VcfEntry> vcfEntries = seff.run(true);
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t\t" + veff);
                if (veff.getEffectType() == EffectType.STOP_GAINED) {
                    assertEquals(EffectImpact.HIGH, veff.getImpact());
                    checked = true;
                }
            }
        }

        assertTrue(checked);
    }

    /**
     * Change of start codon to an alternative start codon
     */
    @Test
    public void test_03_Start_Loss() {
        Log.debug("Test");
        String genome = "testHg19ChrM";
        String vcf = path("test_chrM_start_codon.vcf");

        String[] args = {"-noLog", "-classic", "-ud", "0", genome, vcf};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        snpEff.setDebug(debug);

        SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
        boolean checked = false;
        List<VcfEntry> vcfEntries = seff.run(true);
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t\t" + veff);
                if (veff.getEffectType() == EffectType.START_LOST) {
                    assertEquals(EffectImpact.HIGH, veff.getImpact());
                    checked = true;
                }
            }
        }
        assertTrue(checked);

    }

}
