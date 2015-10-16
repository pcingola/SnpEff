package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test cases: apply a variant (SNP) to a transcript
 *
 */
public class TestCasesApplySnp extends TestCasesBaseApply {

	public TestCasesApplySnp() {
		super();
	}

	/**
	 * Variant before exon
	 */
	@Test
	public void test_apply_variant_01() {
		Gpr.debug("Test");
		verbose = true;

		Variant variant = new Variant(transcript.getParent(), 290, "T", "A");
		checkApplySnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
	}

	/**
	 * Variant ending right before exon start
	 */
	@Test
	public void test_apply_variant_02() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 299, "C", "A");
		checkApplySnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
	}

	/**
	 * Variant at exon start
	 */
	@Test
	public void test_apply_variant_04() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 300, "T", "A");

		String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
				+ "Agtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
				+ "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
				;

		checkApplySnp(variant, expectedCds, null, 1, 300, 399);

	}

	/**
	 * Variant in exon
	 */
	@Test
	public void test_apply_variant_05() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 310, "T", "A");

		String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
				+ "tgtttgggaaAtcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg".toLowerCase() // Exon[1]
				+ "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
				;

		checkApplySnp(variant, expectedCds, null, 1, 300, 399);

	}

	/**
	 * Variant right before exon end
	 */
	@Test
	public void test_apply_variant_06() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 399, "G", "A");

		String expectedCds = "atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg" // Exon[0]
				+ "tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacA".toLowerCase() // Exon[1]
				+ "ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg" // Exon[2]
				;

		checkApplySnp(variant, expectedCds, null, 1, 300, 399);

	}

	/**
	 * Variant right after exon end
	 */
	@Test
	public void test_apply_variant_08() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 400, "A", "C");
		checkApplySnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
	}

	/**
	 * Variant after exon end
	 */
	@Test
	public void test_apply_variant_09() {
		Gpr.debug("Test");

		Variant variant = new Variant(transcript.getParent(), 410, "A", "T");
		checkApplySnp(variant, transcript.cds(), transcript.protein(), 1, 300, 399);
	}

}
