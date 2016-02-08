package org.snpeff.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.snpEffect.commandLine.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test cases on deletions
 *
 * @author pcingola
 */
public class TestCasesIntegrationDelEtc {

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	/**
	 * A deletion having multiple splice_region effects (should show only one)
	 */
	@Test
	public void test_01_del_repeated_effects() {
		Gpr.debug("Test");
		String args[] = { "-ud", "0", "testHg3775Chr1", "tests/del_multiple_splice_region.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		int countEffs = 0;
		boolean repeat = false;

		List<VcfEntry> vcfEnties = snpeff.run(true);
		for (VcfEntry ve : vcfEnties) {

			if (verbose) System.out.println(ve);

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.getVcfEffects();

			for (VcfEffect veff : veffs) {
				Set<String> effs = new HashSet<String>();
				if (verbose) System.out.println("\t" + veff.getEffString());

				// Make sure each effect is unique
				for (String eff : veff.getEffString().split("\\&")) {
					if (verbose) System.out.println("\t\t" + eff);
					if (!effs.add(eff)) repeat = true;
					countEffs++;
				}
			}
		}

		Assert.assertTrue("No effect annotated", countEffs > 0);
		Assert.assertFalse("Duplicated effect", repeat);
	}

	/**
	 * Insertion on minus strand
	 */
	@Test
	public void test_02_del_repeated_effects_gatk() {
		Gpr.debug("Test");
		String args[] = { "-ud", "0", "-o", "gatk", "testHg3775Chr1", "tests/del_multiple_splice_region.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		int countEffs = 0;

		List<VcfEntry> vcfEnties = snpeff.run(true);
		for (VcfEntry ve : vcfEnties) {

			if (verbose) System.out.println(ve);

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.getVcfEffects();

			for (VcfEffect veff : veffs) {
				if (verbose) System.out.println("\t" + veff.getEffString());

				// Make sure each effect is unique
				countEffs = 0;
				for (String eff : veff.getEffString().split("\\+")) {
					if (verbose) System.out.println("\t\t" + eff);
					countEffs++;
				}

				Assert.assertEquals(1, countEffs);
			}
		}
	}

}
