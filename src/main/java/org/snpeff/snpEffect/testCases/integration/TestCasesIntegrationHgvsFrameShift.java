package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

/**
 * Test case
 */
public class TestCasesIntegrationHgvsFrameShift extends TestCasesIntegrationBase {

	public TestCasesIntegrationHgvsFrameShift() {
		super();
	}

	/**
	 * Test HGVS.P in a synonymous frame shift
	 */
	@Test
	public void test_01_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr1", path("hgvs_frameshifts_syn_chr1.vcf"), 4);
	}

	/**
	 * Test HGVS.P in a synonymous frame shift
	 */
	@Test
	public void test_02_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr4", path("hgvs_frameshifts_syn_chr4.vcf"), 2);
	}

	/**
	 * Test HGVS.P in a synonymous frame shift
	 */
	@Test
	public void test_03_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr10", path("hgvs_frameshifts_syn_chr10.vcf"), 2);
	}

	/**
	 * Test HGVS.P in a synonymous frame shift
	 */
	@Test
	public void test_04_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr17", path("hgvs_frameshifts_syn_chr17.vcf"), 2);
	}

	/**
	 * Test HGVS.P in a synonymous frame shift
	 */
	@Test
	public void test_05_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr19", path("hgvs_frameshifts_syn_chr19.vcf"), 2);
	}

	/**
	 * Test HGVS frameshift at CDS end
	 */
	@Test
	public void test_06_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg3775Chr2", path("hgvs_frameshifts_cds_end.vcf"), 6);
	}

}
