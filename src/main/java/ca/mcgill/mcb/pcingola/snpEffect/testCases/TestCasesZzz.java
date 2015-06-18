package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test_missing_exon_number() {
		verbose = true;

		Gpr.debug("Test");
		String args[] = { "testHg3775Chr7", "./tests/missing_exon_number.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				String trId = veff.getTranscriptId();
				if (trId != null && trId.equals("ENST00000288602")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\trank / rank_max: " + veff.getRank() + " / " + veff.getRankMax());

						Assert.assertEquals(15, veff.getRank());
						Assert.assertEquals(18, veff.getRankMax());
					}
				}
			}
		}
	}
}
