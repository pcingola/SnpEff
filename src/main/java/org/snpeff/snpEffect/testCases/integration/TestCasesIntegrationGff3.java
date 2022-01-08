package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactory;
import org.snpeff.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test case for GFF3 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationGff3 extends TestCasesIntegrationBase {

    int exonToStringVersionOri;

    public TestCasesIntegrationGff3() {
        super();
    }

    @AfterEach
    public void after() {
        Exon.ToStringVersion = exonToStringVersionOri;
    }

    @Override
    @BeforeEach
    public void before() {
        super.before();
        exonToStringVersionOri = Exon.ToStringVersion;
        Exon.ToStringVersion = 1; // Set "toString()" version
    }

    @Test
    public void testCase_01_Exon_Simple() {
        Log.debug("Test");
        String genome = "testCase";
        String gff3File = path("exonSimple.gff3");
        String resultFile = path("exonSimple.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_02_ExonIn() {
        Log.debug("Test");
        String genome = "testCase";
        String gff3File = path("exonIn.gff3");
        String resultFile = path("exonIn.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_03_ExonOut() {
        Log.debug("Test");
        String genome = "testCase";
        String gff3File = path("exonOut.gff3");
        String resultFile = path("exonOut.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_04_AthalianaTair10_AT5G66790() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        Exon.ToStringVersion = exonToStringVersionOri;
        String genome = "testAthalianaTair10"; //"athalianaTair10";
        String gff3File = path("AT5G66790.gff3");
        String resultFile = path("AT5G66790.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_05_PaeruPA14muccA() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        Exon.ToStringVersion = exonToStringVersionOri;
        String genome = "testPaeru.PA14";
        String gff3File = path("paeru.PA14.muccA.gff");
        String resultFile = path("paeru.PA14.muccA.txt");
        SnpEffectPredictor sep = buildGff3AndCompare(genome, gff3File, resultFile, true, false);

        // Make sure no splice site is added
        Gene gene = sep.getGenome().getGenes().iterator().next();
        Transcript tr = gene.iterator().next();
        tr.createSpliceSites(SpliceSite.CORE_SPLICE_SITE_SIZE, 0, 0, 0);
        List<SpliceSite> spliceSites = tr.spliceSites();
        assertEquals(0, spliceSites.size());
    }

    @Test
    public void testCase_06_Ppersica() {
        Log.debug("Test");
        Exon.ToStringVersion = exonToStringVersionOri;
        String genome = "testPpersica139";
        String gff3File = path("ppersica_139.gff");
        String resultFile = path("ppersica_139.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, false, false);
    }

    @Test
    public void testCase_07_Rice5() {
        Log.debug("Test");
        String genome = "testRice5";
        String gff3File = path("Os03t0150600.gff");
        String resultFile = path("Os03t0150600.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, false, false);
    }

    @Test
    public void testCase_08_Vibrio() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        Exon.ToStringVersion = exonToStringVersionOri;
        String genome = "testVibrio";
        String gff3File = path("vibrio.gff3");
        String resultFile = path("vibrio.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_09() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_FRAMES_ZERO);
        String genome = "testAP";
        String gff3File = path("testAP_genes.gff.gz");
        String resultFile = path("testAP.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    @Test
    public void testCase_09_AP() {
        Log.debug("Test");
        String genome = "testAP";
        String gff3File = path("testAP_genes.gff.gz");
        String resultFile = path("testAP.txt");

        // Capture STDERR
        PrintStream errOri = System.err;
        final ByteArrayOutputStream myErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(myErr));

        // Test
        try {
            SnpEffPredictorFactory.MIN_TOTAL_FRAME_COUNT = 1; // Force warning even for one gene
            buildGff3AndCompare(genome, gff3File, resultFile, true, false);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // Restore STDERR
            System.setErr(errOri);
        }

        // Show stderr and check message
        if (verbose) System.err.println("STDERR:\n" + myErr);

        assertEquals(1, (int) Log.getWarnCount().get(ErrorWarningType.WARNING_FRAMES_ZERO), "Expecting one warning 'WARNING_FRAMES_ZERO'");
    }

    /**
     * Exon.frameCorrection: Exon too short (size: 1), cannot correct frame!
     */
    @Test
    public void testCase_10_MaizeZmB73() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_EXON_TOO_SHORT);
        Log.silenceWarning(ErrorWarningType.WARNING_CDS_TOO_SHORT);
        String genome = "testMaizeZmB73";
        String gff3File = path("testMaizeZmB73.gff3");
        String resultFile = path("testMaizeZmB73.txt");
        buildGff3AndCompare(genome, gff3File, resultFile, true, false);
    }

    /**
     * Test for bug: Infinite loop when looking up "Parent ID" in some GFF3 files
     */
    @Test
    public void testCase_11_mita() {
        Log.debug("Test");
        String genome = "testMita";
        String gff3File = path("testMita.gff");
        String resultFile = null; // We only check that there is no "Out of memory" error due to infinite loop
        buildGff3AndCompare(genome, gff3File, resultFile, false, false);
    }

}
