package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case where VCF entries hit a transcript that has errors
 *
 * @author pcingola
 */
public class TestCasesTranscriptError {

	boolean verbose = false;

	public TestCasesTranscriptError() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3763Chr20", "./tests/short_codon_bug.vcf" };
		transcriptError(args, ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3763Chr20", "./tests/incorrect_ref.vcf" };
		transcriptError(args, ErrorWarningType.WARNING_REF_DOES_NOT_MATCH_GENOME);
	}

	/**
	 * Run a predictor and check if the expected warnings appear
	 * @param args
	 * @param warningType
	 */
	void transcriptError(String args[], ErrorWarningType warningType) {
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = snpeff.run(true);

		boolean hasWarning = false;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.parseEffects()) {
				EffectImpact imp = veff.getImpact();
				if (verbose) System.out.println("\t" + imp + "\t" + veff);

				// Check if the warning type we expect is there
				if (veff.getErrorsWarning() != null) hasWarning |= veff.getErrorsWarning().indexOf(warningType.toString()) >= 0;
			}
		}

		Assert.assertEquals(true, hasWarning);
	}
}
