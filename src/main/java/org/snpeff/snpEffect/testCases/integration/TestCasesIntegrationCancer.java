package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

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

	//	/**
	//	 * Test Somatic vs Germline
	//	 */
	//	@Test
	//	public void test_01() {
	//		Gpr.debug("Test");
	//		String file = path("test.cancer.snp.01.vcf");
	//		snpEffectCancer(file, null, "testHg3766Chr1", true, "p.Leu1?", "c.1C>G", "G-C", null);
	//	}
	//
	//	/**
	//	 * Test Somatic vs Germline (using TXT file)
	//	 */
	//	@Test
	//	public void test_02() {
	//		Gpr.debug("Test");
	//		String fileVcf = path("test.cancer_no_ped.vcf");
	//		String fileTxt = path("test.cancer_no_ped.txt");
	//		snpEffectCancer(fileVcf, fileTxt, "testHg3766Chr1", true, "p.Leu1?", "c.1C>G", "G-C", null);
	//	}
	//
	//	/**
	//	 * Concurrent modification issue on cancer samples (Intron.apply problem)
	//	 */
	//	@Test
	//	public void test_03_cancer_concurrent_modification() {
	//		Gpr.debug("Test");
	//		String args[] = { "-cancer"//
	//				, "-cancerSamples", path("test_cancer_concurrent_modification.txt") //
	//				, "-ud", "0" //
	//				, "-strict" //
	//				, "testHg3775Chr1"//
	//				, path("test_cancer_concurrent_modification.vcf") //
	//		};
	//
	//		SnpEff cmd = new SnpEff(args);
	//		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
	//		snpeff.setSupressOutput(!verbose);
	//		snpeff.setVerbose(verbose);
	//
	//		List<VcfEntry> vcfEnties = snpeff.run(true);
	//		Assert.assertFalse("Annotation finished with errors", snpeff.getTotalErrs() > 0);
	//
	//		int countCancer = 0, countCancerWarnings = 0;
	//		for (VcfEntry ve : vcfEnties) {
	//			if (verbose) System.out.println(ve);
	//
	//			// Get first effect (there should be only one)
	//			List<VcfEffect> veffs = ve.getVcfEffects();
	//
	//			for (VcfEffect veff : veffs) {
	//				if (verbose) System.out.println("\t" + veff.getAllele() + "\t" + veff);
	//				if (veff.getAllele().indexOf('-') > 0) {
	//					countCancer++;
	//					if (verbose) System.out.println("\t\t" + veff.getErrorsWarning());
	//					if ((veff.getErrorsWarning() != null) && (!veff.getErrorsWarning().isEmpty())) countCancerWarnings++;
	//				}
	//			}
	//		}
	//
	//		Assert.assertTrue("Cancer effects not found", countCancer > 0);
	//		Assert.assertTrue("There should be no warnings: countCancerWarnings = " + countCancerWarnings, countCancerWarnings == 0);
	//	}
	//
	//	/**
	//	 * Test Somatic vs Germline: Check HGVS notation "c."
	//	 */
	//	@Test
	//	public void test_04() {
	//		Gpr.debug("Test");
	//		String file = path("test_04.vcf");
	//		snpEffectCancer(file, null, "testHg19Chr22", false, "p.Gln133Leu", "c.398A>T", "A-T", null);
	//	}
	//
	//	/**
	//	 * Cancer mutation is reversion to the REF base
	//	 */
	//	@Test
	//	public void test_05() {
	//		Gpr.debug("Test");
	//		String file = path("test.cancer_05.vcf");
	//		snpEffectCancer(file, null, "testHg19Chr17", false, "p.Arg72Pro", "c.215G>C", "G-C", "NM_000546.5");
	//	}
	//
	//	/**
	//	 * Cancer mutation is reversion to the REF base
	//	 */
	//	@Test
	//	public void test_07() {
	//		Gpr.debug("Test");
	//		String file = path("test.cancer_07.vcf");
	//		snpEffectCancer(file, null, "testHg19Chr17", false, "p.His72Pro", "c.215A>C", "G-T", "NM_000546.5");
	//	}
	/**
	 * Cancer mutation is reversion to the REF base
	 */
	@Test
	public void test_06() {
		Gpr.debug("Test");
		debug = verbose = true;
		String file = path("test.cancer_06.vcf");
		snpEffectCancer(file, null, "testHg19Chr17", false, "p.His72Pro", "c.215A>C", "G-T", "NM_000546.5");
	}

}
