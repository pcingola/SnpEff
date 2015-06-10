package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Empty fields should show '.' when printed
	 */
	@Test
	public void test_11_empty() {
		Gpr.debug("Test");
		String file = "./tests/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);
			Assert.assertEquals("1\t11169327\t.\tT\t.\t.\tPASS\tAC=0;AF=0.00;AN=176;DP=7756;MQ0=0;set=ReferenceInAll\tGT:DP\t0/0:115", vcfEntry.toString());
		}
	}

}
