package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Test case
 *
 */
public class TestCasesZzz {

	boolean verbose = true;

	public TestCasesZzz() {

	}

	@Test
	public void test_23_VcfUnsorted() {
		Gpr.debug("Test");
		String vcfFile = "tests/out_of_order.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		vcf.setErrorIfUnsorted(true);

		boolean errorFound = false;
		String expectedErrorMessage = "VCF file 'tests/out_of_order.vcf' is not sorted, genomic position 20:2622038 is before 20:2621729";
		try {
			for (VcfEntry ve : vcf) {
				if (verbose) System.out.println(ve);
			}
		} catch (Throwable e) {
			errorFound = e.getMessage().startsWith(expectedErrorMessage);
			if (verbose) e.printStackTrace();

			if (!errorFound) {
				Gpr.debug("Error messages differ:" //
						+ "\n\tExpected : '" + expectedErrorMessage + "'" //
						+ "\n\tActual   : '" + e.getMessage() + "'" //
				);
			}
		}

		Assert.assertTrue(errorFound);
	}

}
