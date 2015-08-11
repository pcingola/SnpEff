package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

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
	 * Concurrent modification issue on cancer samples (Intron.apply problem)
	 */
	@Test
	public void test_03_cancer_concurrent_modification() {
		Gpr.debug("Test");
		String args[] = { "-cancer"//
				, "-cancerSamples", "tests/test_cancer_concurrent_modification.txt" //
				, "-ud", "0" //
				, "-strict" //
				, "testHg3775Chr1"//
				, "tests/test_cancer_concurrent_modification.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		Assert.assertFalse("Annotation finished with errors", snpeff.getTotalErrs() > 0);

		int countCancer = 0, countCancerWarnings = 0;
		for (VcfEntry ve : vcfEnties) {
			if (verbose) System.out.println(ve);

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.parseEffects();

			for (VcfEffect veff : veffs) {
				if (verbose) System.out.println("\t" + veff.getAllele() + "\t" + veff);

				if (veff.getAllele().indexOf('-') > 0) {
					countCancer++;
					System.out.println("\t\t" + veff.getErrorsWarning());
					if ((veff.getErrorsWarning() != null) && (!veff.getErrorsWarning().isEmpty())) countCancerWarnings++;

					if (veff.getTranscriptId().equals("ENST00000369339")) Assert.assertEquals("p.Thr101Ser", veff.getHgvsP());
					if (veff.getTranscriptId().equals("ENST00000342960")) Assert.assertEquals("p.Thr372Ser", veff.getHgvsP());
				}
			}
		}

		Assert.assertTrue("Cancer effects not found", countCancer > 0);
		Assert.assertTrue("There should be no warnings: countCancerWarnings = " + countCancerWarnings, countCancerWarnings == 0);
	}
}
