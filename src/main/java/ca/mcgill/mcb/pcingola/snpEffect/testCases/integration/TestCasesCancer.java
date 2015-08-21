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
 * Test cases for cancer effect (difference betwee somatic an germline tissue)
 *
 * @author pcingola
 */
public class TestCasesCancer {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesCancer() {
		super();
	}

	/**
	 * Calculate snp effect for a list of snps
	 */
	public void snpEffect(String vcfFile, String txtFile, String aaHgsv, String genotype) {
		// Create command
		String argsVcf[] = { "-classic", "-cancer", "-hgvs", "testHg3766Chr1", vcfFile };
		String argsTxt[] = { "-classic", "-cancer", "-cancerSamples", txtFile, "-hgvs", "testHg3766Chr1", vcfFile };
		String args[] = (txtFile == null ? argsVcf : argsTxt);

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Find AA change for a genotype
		boolean found = false;
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);
			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
				if (debug) System.err.println("\t" + eff + "\n\t\tAA : " + eff.getAa() + "\n\t\tGenotype: " + eff.getGenotype());
				if (genotype.equals(eff.getGenotype())) {
					Assert.assertEquals(aaHgsv, eff.getAa());
					found = true;
				}
			}
		}

		// Not found? Error
		if (!found) throw new RuntimeException("Genotype '" + genotype + "' not found.");
	}

	/**
	 * Test Somatic vs Germline
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String file = "tests/test.cancer.snp.01.vcf";
		snpEffect(file, null, "p.Leu1?/c.1A>G", "G-C");
	}

	/**
	 * Test Somatic vs Germline (using TXT file)
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		String fileVcf = "tests/test.cancer_no_ped.vcf";
		String fileTxt = "tests/test.cancer_no_ped.txt";
		snpEffect(fileVcf, fileTxt, "p.Leu1?/c.1A>G", "G-C");
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
			List<VcfEffect> veffs = ve.getVcfEffects();

			for (VcfEffect veff : veffs) {
				if (verbose) System.out.println("\t" + veff.getAllele() + "\t" + veff);
				if (veff.getAllele().indexOf('-') > 0) {
					countCancer++;
					System.out.println("\t\t" + veff.getErrorsWarning());
					if ((veff.getErrorsWarning() != null) && (!veff.getErrorsWarning().isEmpty())) countCancerWarnings++;
				}
			}
		}

		Assert.assertTrue("Cancer effects not found", countCancer > 0);
		Assert.assertTrue("There should be no warnings: countCancerWarnings = " + countCancerWarnings, countCancerWarnings == 0);
	}

}
