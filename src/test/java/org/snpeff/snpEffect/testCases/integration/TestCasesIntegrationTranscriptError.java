package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Log;

/**
 * Test case where VCF entries hit a transcript that has errors
 *
 * @author pcingola
 */
public class TestCasesIntegrationTranscriptError extends TestCasesIntegrationBase {

    public TestCasesIntegrationTranscriptError() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String[] args = {"-classic", "testHg3763Chr20", path("short_codon_bug.vcf")};
        checkTranscriptError(args, ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE);
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        String[] args = {"-classic", "testHg3763Chr20", path("incorrect_ref.vcf")};
        checkTranscriptError(args, ErrorWarningType.WARNING_REF_DOES_NOT_MATCH_GENOME);
    }

}
