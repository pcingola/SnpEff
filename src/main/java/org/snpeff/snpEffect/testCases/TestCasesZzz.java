package org.snpeff.snpEffect.testCases;

import org.junit.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean verbose = true;
	boolean debug = true;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test10() {
		Gpr.debug("Test");
		String vcfFile = "tests/vcf_translocation.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			//			if (verbose) System.out.println(ve.getVariantType() + "\t" + ve);
			//			Assert.assertEquals("Variant type is not 'BND'", VariantType.BND, ve.getVariantType());

			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var.getVariantType() + "\t" + var);
			}
		}
	}

}
