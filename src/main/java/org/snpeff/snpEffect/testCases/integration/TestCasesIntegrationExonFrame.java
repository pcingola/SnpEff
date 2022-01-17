package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for exon frames
 *
 * @author pcingola
 */
public class TestCasesIntegrationExonFrame extends TestCasesIntegrationBase {

    public TestCasesIntegrationExonFrame() {
        super();
    }

    /**
     * Test database: Build, check and annotate
     */
    @Test
    public void test_01() {
        Log.debug("Test");
        //---
        // Build database
        //---
        String genomeName = "testLukas";
        String[] args = {"build", "-noLog", "-noCheckCds", "-noCheckProtein", "-gff3", genomeName};

        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setDebug(debug);
        snpEff.setSupressOutput(!verbose);
        boolean ok = snpEff.run();
        assertTrue(ok);

        //---
        // Load database and check some numbers
        //---
        String configFile = Config.DEFAULT_CONFIG_FILE;
        Config config = new Config(genomeName, configFile);
        if (verbose) Log.info("Loading database");
        SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

        // Find transcript (there is only one)
        Transcript transcript = null;
        for (Gene gene : snpEffectPredictor.getGenome().getGenes())
            for (Transcript tr : gene)
                transcript = tr;

        if (verbose) Log.debug("Trasncript:" + transcript);

        // Check parameters
        assertEquals(454127, transcript.getCdsStart());
        assertEquals(450599, transcript.getCdsEnd());

        //---
        // Check annotations
        //---
        String vcfFileName = path("testLukas.vcf");
        String[] argsEff = {"-classic", "-noHgvs", "-ud", "0", genomeName, vcfFileName};

        // Annotate
        SnpEff cmd = new SnpEff(argsEff);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);
        List<VcfEntry> vcfEntries = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        // Analyze annotations
        ok = false;
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve.toStringNoGt());

            EffectType expectedEffect = EffectType.valueOf(ve.getInfo("EXP_EFF"));
            String expectedAa = ve.getInfo("EXP_AA");
            String expectedCodon = ve.getInfo("EXP_CODON");

            boolean found = false;
            for (VcfEffect veff : ve.getVcfEffects()) {
                String eff = veff.getEffectType().toString();

                if (verbose) {
                    System.out.println("\t" + veff);
                    System.out.println("\t\tExpecing: '" + expectedEffect + "'\tFound: '" + eff + "'");
                    System.out.println("\t\tExpecing: '" + expectedAa + "'\tFound: '" + veff.getAa() + "'");
                    System.out.println("\t\tExpecing: '" + expectedCodon + "'\tFound: '" + veff.getCodon() + "'");
                }

                // Effect matches expected?
                if (veff.hasEffectType(expectedEffect) //
                        && ((veff.getAa() == null) || veff.getAa().isEmpty() || expectedAa.equals(veff.getAa())) //
                        && ((veff.getCodon() == null) || veff.getCodon().isEmpty() || expectedCodon.equals(veff.getCodon())) //
                ) //
                    found = ok = true;
            }

            if (!found)
                throw new RuntimeException("Cannot find expected effect '" + expectedEffect + "', amino acid change '" + expectedAa + "' and codon change '" + expectedCodon + "'");
        }
        assertTrue(ok, "No match found");
    }

    /**
     * Build genome (no exceptions should be thrown)
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        // Build database
        String genomeName = "testMacuminata";
        String[] args = {"build", "-noCheckCds", "-noCheckProtein", "-noLog", genomeName};

        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        boolean ok = snpEff.run();
        assertTrue(ok);
    }
}
