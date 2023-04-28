package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case where VCF entries are huge (e.g. half chromosome deleted)
 *
 * @author pcingola
 */
public class TestCasesIntegrationLargeDeletion extends TestCasesIntegrationBase {

    public TestCasesIntegrationLargeDeletion() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String[] args = {"-classic", "-noOut", "testHg3766Chr1", path("huge_deletion_DEL.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);

        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        // Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
        for (VcfEntry ve : vcfEntries) {
            if (verbose)
                Log.info(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getEndClosed() + "\tsize:" + ve.size());

            boolean ok = false;
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info(veff);
                ok |= (veff.getEffectType() == EffectType.CHROMOSOME_LARGE_DELETION);
            }

            assertTrue(ok, "Expecting 'CHROMOSOME_LARGE_DELETION', not found");
        }
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        String[] args = {"-classic", "-noOut", "testHg3766Chr1", path("huge_deletion.vcf.gz")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        // Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());
            assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));
        }
    }

    @Test
    public void test_03() {
        Log.debug("Test");
        String[] args = {"-classic", "-noOut", "testHg19Chr9", path("huge_deletion_chr9.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        boolean okCdkn2a = false;
        boolean okCdkn2aTr = false;
        boolean okCdkn2b = false;

        // Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());

            assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t" + veff);

                EffectType eff = veff.getEffectType();
                String geneName = veff.getGeneName();
                String trId = veff.getTranscriptId();

                if (eff == EffectType.GENE_DELETED && geneName.equals("CDKN2A")) okCdkn2a = true;
                if (eff == EffectType.GENE_DELETED && geneName.equals("CDKN2B")) okCdkn2b = true;
                if (eff == EffectType.TRANSCRIPT_DELETED && trId.equals("NM_000077.4")) okCdkn2aTr = true;
            }
        }

        assertTrue(okCdkn2a, "GENE_DELETED CDKN2A: Not found");
        assertTrue(okCdkn2b, "GENE_DELETED CDKN2B: Not found");
        assertTrue(okCdkn2aTr, "TRANSCRIPT_DELETED CDKN2A (NM_000077.4): Not found");
    }

    /**
     * Show transcripts deleted when there is a gene-fusion due to deletion
     */
    @Test
    public void test_04() {
        Log.debug("Test");
        String genome = "testHg19Chr9";
        String vcfFile = path("huge_deletion_fusion_chr9.vcf");
        List<VcfEntry> vcfs = snpEffect(genome, vcfFile, null, EffFormatVersion.FORMAT_ANN_1);

        // Sanity check
        assertEquals(1, vcfs.size());

        // Find effects
        boolean foundFusion = false, foundTrDel = false, foundExDel = false;
        for (VcfEffect veff : vcfs.get(0).getVcfEffects()) {
            if (verbose) Log.info(veff);

            // Fusion
            if (veff.getEffectType() == EffectType.GENE_FUSION_REVERESE //
                    && veff.getGeneName().equals("CDKN2A&CDKN2B-AS1") //
            ) {
                if (verbose) Log.info("FOUND:\t" + veff);
                foundFusion = true;
            }

            // Transcript deletion
            if (veff.getEffectType() == EffectType.TRANSCRIPT_DELETED //
                    && veff.getTranscriptId().equals("NM_004936.3") //
            ) {
                if (verbose) Log.info("FOUND:\t" + veff);
                foundTrDel = true;
            }

            // Exon deletion
            if (veff.getEffectType() == EffectType.EXON_DELETED //
                    && veff.getTranscriptId().equals("NM_001195132.1") //
            ) {
                if (verbose) Log.info("FOUND EXON LOSS:\t" + veff);
                foundExDel = true;
            }

        }

        // All three must be present
        assertTrue(foundFusion, "Could not find expected gene fusion");
        assertTrue(foundTrDel, "Could not find expected transcript deletion");
        assertTrue(foundExDel, "Could not find expected exon deletion");
    }

}
