package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/hgvsUpDownStream/";
	}

	//	/**
	//	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	//	 */
	//	@Test
	//	public void test_01_VcfRawAlt() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//
	//		// Create command
	//		String args[] = { "testHg3775Chr1", "tests/test_ann_integration_01.vcf" };
	//
	//		SnpEff cmd = new SnpEff(args);
	//		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
	//		cmdEff.setVerbose(verbose);
	//		cmdEff.setSupressOutput(!verbose);
	//
	//		// Run command
	//		List<VcfEntry> list = cmdEff.run(true);
	//		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);
	//
	//		// Expected results
	//		Set<String> allelesExpected = new HashSet<>();
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");
	//
	//		// Find AA change for a genotype
	//		Set<String> allelesReal = new HashSet<>();
	//		for (VcfEntry vcfEntry : list) {
	//			if (debug) System.err.println(vcfEntry);
	//
	//			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
	//				String allele = eff.getAllele();
	//				if (verbose) System.err.println("\t" + eff + "\n\t\tAllele: " + allele);
	//
	//				Assert.assertTrue("Unexpected allele '" + allele + "'", allelesExpected.contains(allele));
	//				allelesReal.add(allele);
	//			}
	//		}
	//
	//		Assert.assertEquals(allelesExpected, allelesReal);
	//	}

	// Check variant's HGVS.c nomenclature
	void checkHgvscForTr(List<VcfEntry> list, String trId) {
		boolean found = false;
		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals(trId)) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
					found = true;
				}
			}
		}

		Assert.assertTrue("No annotations found for transcript " + trId, found);
	}

	@Test
	public void test_08_hgvs_downstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg38Chr1", testsDir + "hgvs_downstream_negative_strand_08.vcf", null);
		checkHgvscForTr(list, "NM_002524.4");
	}
}
