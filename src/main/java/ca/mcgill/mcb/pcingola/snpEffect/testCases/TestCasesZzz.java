package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfHeaderInfo;
import ca.mcgill.mcb.pcingola.vcf.VcfHeaderInfo.VcfInfoNumber;
import ca.mcgill.mcb.pcingola.vcf.VcfInfoType;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	/**
	 * Add a new INFO and the respective header
	 */
	@Test
	public void test_vcfInfoHeaderAdd() {
		String vcfFileName = "tests/example_42.vcf";

		verbose = true;
		// Create a new INFO field
		String infoFieldName = "NEW_INFO";
		VcfHeaderInfo vhInfo = new VcfHeaderInfo(infoFieldName, VcfInfoType.Integer, VcfInfoNumber.UNLIMITED.toString(), "An arbitrary set of random numbers");
		String expectedHeader = "##INFO=<ID=" + infoFieldName + ", Number=., Type=Integer, Description=\"An arbitrary set of random numbers\">";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				// Add INFO field to header
				vcf.getVcfHeader().add(vhInfo);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader));
			}

			// Add INFO field values
			String value = "" + ((int) (1000 * Math.random()));
			ve.addInfo(infoFieldName, value);
			if (verbose) System.out.println(ve);

			// Check that 'info=value' is there
			Assert.assertTrue(ve.toString().contains(infoFieldName + "=" + value));
		}
	}
}
