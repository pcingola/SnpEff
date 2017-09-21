package org.snpeff.snpEffect.testCases.unity;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEntry;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test_35_translocations_parsing() {
		Gpr.debug("Test");
		String vcfFile = "tests/vcf_sv_parsing.vcf";

		verbose = true;

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		VcfEntry ve = vcf.iterator().next();
		if (verbose) System.out.println(ve);
		boolean ok = false;
		for (Variant var : ve.variants()) {
			if (verbose) System.out.println("\t" + var.getVariantType() + "\t" + var);
			Assert.assertEquals("Variant type is not 'DEL'", VariantType.DEL, var.getVariantType());
			Assert.assertEquals("Variant's start does not match", 24538578, var.getStart());
			Assert.assertEquals("Variant's end does not match", 24538585, var.getEnd());
			ok = true;
		}
		Assert.assertTrue("No variants found!", ok);
	}
}
