package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.util.Gpr;

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
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3763Chr20", "./tests/short_codon_bug.vcf" };
		checkTranscriptError(args, ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3763Chr20", "./tests/incorrect_ref.vcf" };
		checkTranscriptError(args, ErrorWarningType.WARNING_REF_DOES_NOT_MATCH_GENOME);
	}

}
