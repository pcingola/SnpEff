package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Assert;
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
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
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
			List<VcfEffect> veffs = ve.parseEffects();

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
