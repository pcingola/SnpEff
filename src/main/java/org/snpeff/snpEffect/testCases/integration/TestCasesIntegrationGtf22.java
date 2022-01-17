package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Exon;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Log;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationGtf22 extends TestCasesIntegrationBase {

    int exonToStringVersionOri;

    public TestCasesIntegrationGtf22() {
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
    public void testCaseHg37_61_ENST00000250838() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);
        Log.silenceWarning(ErrorWarningType.WARNING_FRAMES_ZERO);
        String genome = "testHg37.61";
        String gtfFile = path("ENST00000250838.gtf");
        String fastaFile = path("chrY.fa.gz");
        String resultFile = path("ENST00000250838.txt");
        buildGtfAndCompare(genome, gtfFile, fastaFile, resultFile);
    }

    @Test
    public void testCaseHg37_61_ENST00000331397() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);
        String genome = "testHg37.61";
        String gtfFile = path("ENST00000331397.gtf22");
        String fastaFile = path("chrY.fa.gz");
        String resultFile = path("ENST00000331397.txt");
        buildGtfAndCompare(genome, gtfFile, fastaFile, resultFile);
    }

    @Test
    public void testCaseMm37_61_ENSMUSG00000051951() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);
        String genome = "testMm37.61";
        String gtfFile = path("ENSMUSG00000051951.gtf");
        String resultFile = path("ENSMUSG00000051951.txt");
        buildGtfAndCompare(genome, gtfFile, null, resultFile);
    }

    @Test
    public void testCaseMm37_61_ENSMUST00000070533() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);
        String genome = "testMm37.61";
        String gtfFile = path("ENSMUST00000070533.gtf");
        String resultFile = path("ENSMUST00000070533.txt");
        buildGtfAndCompare(genome, gtfFile, null, resultFile);
    }

}
