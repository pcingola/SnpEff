package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void testZzz() {

		String vcfFile = Gpr.HOME + "/snpEff/d.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);

		int countVariants = 0;
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
			for (Variant v : ve.variants()) {
				System.out.println("\t" + v + "\tis Variant: " + v.isVariant());
				if (v.isVariant()) countVariants++;
			}
		}

		Assert.assertEquals(1, countVariants);
	}
}
