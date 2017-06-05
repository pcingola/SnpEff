package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 *
 * Test cases for cancer effect (difference betwee somatic an germline tissue)
 *
 * @author pcingola
 */
public class TestCasesIntegrationCancer extends TestCasesIntegrationBase {

	public TestCasesIntegrationCancer() {
		super();
	}

	/**
	 * Test Somatic vs Germline
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String file = "tests/integration/cancer/test.cancer.snp.01.vcf";
		snpEffectCancer(file, null, "testHg3766Chr1", true, "p.Leu1?", "c.1C>G", "G-C");
	}

	/**
	 * Test Somatic vs Germline (using TXT file)
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		String fileVcf = "tests/integration/cancer/test.cancer_no_ped.vcf";
		String fileTxt = "tests/integration/cancer/test.cancer_no_ped.txt";
		snpEffectCancer(fileVcf, fileTxt, "testHg3766Chr1", true, "p.Leu1?", "c.1C>G", "G-C");
	}

	/**
	 * Concurrent modification issue on cancer samples (Intron.apply problem)
	 */
	@Test
	public void test_03_cancer_concurrent_modification() {
		Gpr.debug("Test");
		String args[] = { "-cancer"//
				, "-cancerSamples", "tests/integration/cancer/test_cancer_concurrent_modification.txt" //
				, "-ud", "0" //
				, "-strict" //
				, "testHg3775Chr1"//
				, "tests/integration/cancer/test_cancer_concurrent_modification.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		Assert.assertFalse("Annotation finished with errors", snpeff.getTotalErrs() > 0);

		int countCancer = 0, countCancerWarnings = 0;
		for (VcfEntry ve : vcfEnties) {
			if (verbose) System.out.println(ve);

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.getVcfEffects();

			for (VcfEffect veff : veffs) {
				if (verbose) System.out.println("\t" + veff.getAllele() + "\t" + veff);
				if (veff.getAllele().indexOf('-') > 0) {
					countCancer++;
					if (verbose) System.out.println("\t\t" + veff.getErrorsWarning());
					if ((veff.getErrorsWarning() != null) && (!veff.getErrorsWarning().isEmpty())) countCancerWarnings++;
				}
			}
		}

		Assert.assertTrue("Cancer effects not found", countCancer > 0);
		Assert.assertTrue("There should be no warnings: countCancerWarnings = " + countCancerWarnings, countCancerWarnings == 0);
	}

	/**
	 * Test Somatic vs Germline: Check HGVS notation "c."
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
		String file = "tests/integration/cancer/test_04.vcf";
		snpEffectCancer(file, null, "testHg19Chr22", false, "p.Gln133Leu", "c.398A>T", "A-T");
	}

}
