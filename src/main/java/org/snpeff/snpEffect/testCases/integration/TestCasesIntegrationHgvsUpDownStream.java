package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test cases for HGVS notation
 */
public class TestCasesIntegrationHgvsUpDownStream extends TestCasesIntegrationBase {

	public TestCasesIntegrationHgvsUpDownStream() {
		super();
	}

	/**
	 * Test for HGVS.C notation on upstream variants
	 */
	@Test
	public void test_01_hgvs_upstream() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr2", "tests/integration/hgvsUpDownStream/hgvs_upstream.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("NM_000463.2")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test for HGVS.C notation on downstream variants
	 */
	@Test
	public void test_02_hgvs_downstream() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr2", "tests/integration/hgvsUpDownStream/hgvs_downstream.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("NM_000463.2")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test that CSV summary does not throw any error
	 */
	@Test
	public void test_03_hgvs_upstream_del() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg3765Chr22", "tests/integration/hgvsUpDownStream/hgvs_upstream_del.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("ENST00000404751")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
	 */
	@Test
	public void test_04_hgvs_upstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr17", "tests/integration/hgvsUpDownStream/hgvs_upstream_negative_strand.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("NM_000199.3")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
	 */
	@Test
	public void test_05_hgvs_downstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr17", "tests/integration/hgvsUpDownStream/hgvs_downstream_negative_strand.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("NM_000199.3")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

}
