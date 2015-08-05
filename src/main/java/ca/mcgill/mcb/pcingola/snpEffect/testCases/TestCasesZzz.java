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
	 * Issue on cancer samples
	 */
	@Test
	public void test_cancer_concurrent_modification() {
		Gpr.debug("Test");
		String args[] = { "-cancer"//	
				, "-cancerSamples", "tests/test_cancer_concurrent_modification.txt" //
				, "testHg3775Chr1"//
				, "tests/test_cancer_concurrent_modification.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		Assert.assertFalse("Annotation finished with errors", snpeff.getTotalErrs() > 0);

		int countEffs = 0;
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
