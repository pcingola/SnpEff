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
	 * Add and replace an INFO header
	 */
	@Test
	public void test_vcfInfoHeaderReplace() {
		String infoFieldName = "NEW_INFO";
		String vcfFileName = "tests/example_42.vcf";

		verbose = true;

		// Add this header
		VcfHeaderInfo vhInfo = new VcfHeaderInfo(infoFieldName, VcfInfoType.Integer, VcfInfoNumber.UNLIMITED.toString(), "An arbitrary set of integer random numbers");
		String expectedHeader = "##INFO=<ID=" + infoFieldName + ", Number=., Type=Integer, Description=\"An arbitrary set of integer random numbers\">";

		// Replace using this header
		VcfHeaderInfo vhInfo2 = new VcfHeaderInfo(infoFieldName, VcfInfoType.Float, "1", "One float random numbers");
		String expectedHeader2 = "##INFO=<ID=" + infoFieldName + ", Number=1, Type=Float, Description=\"One float random numbers\">";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				// Add INFO field to header
				vcf.getVcfHeader().add(vhInfo);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader));

				// Add second INFO field to header (should replace first one)
				vcf.getVcfHeader().add(vhInfo2);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader2)); // New header 
				Assert.assertTrue(!vcf.getVcfHeader().toString().contains(expectedHeader)); // Old header should be gone
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
