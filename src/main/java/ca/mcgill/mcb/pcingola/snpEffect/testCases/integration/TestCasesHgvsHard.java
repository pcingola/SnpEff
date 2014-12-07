package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesHgvsHard extends TestCasesBase {

	public TestCasesHgvsHard() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		shiftHgvs = true;
	}

	/**
	 * Test some 'dup' form chr17
	 */
	@Test
	public void test_hgvs_dup() {
		Gpr.debug("Test");

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_dup.vcf";

		compareHgvs(genome, vcf);
	}

	@Test
	public void test_hgvs_md_chr17() {
		Gpr.debug("Test");
		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_md.chr17.vcf";
		compareHgvs(genome, vcf, false);
	}

	@Test
	public void test_hgvs_walk_and_roll_1() {
		Gpr.debug("Test");

		String genome = "testHg19Chr1";
		String vcf = "tests/hgvs_jeremy_1.vcf";

		compareHgvs(genome, vcf);
	}

	@Test
	public void test_hgvs_walk_and_roll_2() {
		Gpr.debug("Test");

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_walk_and_roll.1.vcf";

		compareHgvs(genome, vcf, true);
	}

	@Test
	public void test_hgvs_walk_and_roll_3() {
		Gpr.debug("Test");
		String genome = "testHg19Chr13";
		String vcf = "tests/hgvs_savant.vcf";
		compareHgvs(genome, vcf, true);
	}

}
