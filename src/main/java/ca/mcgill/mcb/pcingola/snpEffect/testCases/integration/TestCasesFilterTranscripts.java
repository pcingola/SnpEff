package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 *
 * Filter transcripts
 *
 * @author pcingola
 */
public class TestCasesFilterTranscripts {

	boolean verbose = false;

	public TestCasesFilterTranscripts() {
		super();
	}

	/**
	 * Filter transcripts from a file
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { //
				"-noStats" //
				, "-i", "vcf" //
				, "-o", "vcf" //
				, "-classic" //
				, "-onlyTr", "tests/filterTranscripts_01.txt"//
				, "testHg3765Chr22" //
				, "tests/test_filter_transcripts_001.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			// Get effect string
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\ttrId:" + veff.getTranscriptId() + "\t" + veff);
				Assert.assertEquals("ENST00000400573", veff.getTranscriptId());
			}
		}
	}

	/**
	 * Filter transcripts from a file
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { //
				"-noStats" //
				, "-i", "vcf" //
				, "-o", "vcf" //
				, "-classic" //
				, "-onlyTr", "tests/filterTranscripts_02.txt"//
				, "testHg3765Chr22" //
				, "tests/test_filter_transcripts_001.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			// Get effect string
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\ttrId:" + veff.getTranscriptId() + "\t" + veff);

				if (veff.getTranscriptId().equals("ENST00000400573") || veff.getTranscriptId().equals("ENST00000262608")) {
					// OK
				} else throw new RuntimeException("This transcript should not be here! " + veff);
			}
		}
	}

}
