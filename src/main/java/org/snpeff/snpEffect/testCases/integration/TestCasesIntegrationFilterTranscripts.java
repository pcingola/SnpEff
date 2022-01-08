package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Filter transcripts
 *
 * @author pcingola
 */
public class TestCasesIntegrationFilterTranscripts extends TestCasesIntegrationBase {

    public TestCasesIntegrationFilterTranscripts() {
        super();
    }

    /**
     * Filter transcripts from a file
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        String[] args = { //
                "-noStats" //
                , "-i", "vcf" //
                , "-o", "vcf" //
                , "-classic" //
                , "-onlyTr", path("filterTranscripts_01.txt")//
                , "testHg3765Chr22" //
                , path("test_filter_transcripts_001.vcf") //
        };

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            // Get effect string
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\ttrId:" + veff.getTranscriptId() + "\t" + veff);
                assertEquals("ENST00000400573", veff.getTranscriptId());
            }
        }
    }

    /**
     * Filter transcripts from a file
     */
    @Test
    public void test_02() {
        Log.debug("Test");
        String[] args = { //
                "-noStats" //
                , "-i", "vcf" //
                , "-o", "vcf" //
                , "-classic" //
                , "-onlyTr", path("filterTranscripts_02.txt")//
                , "testHg3765Chr22" //
                , path("test_filter_transcripts_001.vcf") //
        };

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);

        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            // Get effect string
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\ttrId:" + veff.getTranscriptId() + "\t" + veff);

                if (veff.getTranscriptId().equals("ENST00000400573") || veff.getTranscriptId().equals("ENST00000262608")) {
                    // OK
                } else throw new RuntimeException("This transcript should not be here! " + veff);
            }
        }
    }

}
