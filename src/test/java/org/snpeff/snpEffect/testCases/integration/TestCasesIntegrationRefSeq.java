package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.align.SmithWaterman;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.util.Log;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationRefSeq extends TestCasesIntegrationBase {

    public TestCasesIntegrationRefSeq() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_FRAMES_ZERO);

        /// Build SnpEffectPredictor using a RefSeq file
        String genome = "hg19";
        String refSeqFile = path("hg19_refSeq_OR4F16.txt");
        String fastaFile = null;
        String resultFile = path("hg19_refSeq_OR4F16.dump.txt");
        SnpEffectPredictor sep = buildAndCompare(genome, refSeqFile, fastaFile, resultFile, true);

        // Check a SNP
        sep.buildForest();
        Variant variant = new Variant(sep.getGenome().getChromosome("1"), 521603, "A", "G");
        VariantEffects effs = sep.variantEffect(variant);
        for (VariantEffect eff : effs) {
            if (verbose) Log.info("\t" + eff);
            assertEquals(eff.getEffectType(), EffectType.INTERGENIC);
        }
    }

    /**
     * Test improved exon frame correction in UCSC references
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        /// Build SnpEffectPredictor using a RefSeq file
        String genome = "testNM_015296";
        String[] args = {"build", genome};
        SnpEff snpeff = new SnpEff(args);
        snpeff.setDebug(debug);
        snpeff.setVerbose(verbose);

        // Build database
        SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.cmd();
        snpeffBuild.setStoreAlignments(true);
        snpeffBuild.setCheckNumOk(false);
        snpeffBuild.run();

        // Make sure the alignment matches on most bases after exon rank 49
        HashMap<String, SmithWaterman> alignmentByTrId = snpeffBuild.getSnpEffCmdProtein().getAlignmentByTrId();
        SmithWaterman sw = alignmentByTrId.get("NM_015296.2");
        if (debug) Log.debug(sw.getAlignmentScore() + "\n" + sw);
        assertTrue(sw.getAlignmentScore() >= 2061);
    }
}
