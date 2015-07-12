package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Non-variant gVCF entries (i.e. ALT=<NON_REF>) 
	 */
	@Test
	public void test_30_gVCF_NON_REF() {
		Gpr.debug("Test");

		String vcfFileName = "tests/test_gVCF_NON_REF.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);

			// Check variants
			// The last variant is "<NON_REF>" which is interpreted as non-variant (it gives no information)
			int countNonVariants = 0;
			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var);
				if (!var.isVariant()) countNonVariants++;
			}
			Assert.assertEquals(1, countNonVariants);

			// Check that we can parse genotypes
			for (VcfGenotype vgt : ve.getVcfGenotypes()) {
				if (verbose) System.out.println("\t\tVCF_GT: " + vgt);
			}

			// Check GT score
			for (byte gt : ve.getGenotypesScores()) {
				if (verbose) System.out.println("\t\tGT    : " + gt);
				Assert.assertEquals(1, gt);
			}
		}
	}

}
