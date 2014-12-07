package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

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
	public void test_28_vcfInfoReplace() {
		Gpr.debug("Test");

		verbose = true;
		String vcfFileName = "tests/example_42.vcf";

		// Replace all 'DP' fields using this value
		String infoKey = "DP";
		String infoValue = "42";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			String infoValuePrev = ve.getInfo(infoKey);

			// Check that 'key=value' is in INFO
			String keyValPrev = infoKey + "=" + infoValuePrev;
			Assert.assertTrue("Old key=valu is not present", ve.getInfoStr().contains(keyValPrev));

			// Replace value
			ve.addInfo(infoKey, infoValue);
			if (verbose) System.out.println(ve);

			// Check that new 'key=value' is there
			String keyVal = infoKey + "=" + infoValue;
			Assert.assertTrue("New key=value is present", ve.toString().contains(keyVal));

			// Check that previous 'key=value' is no longer there
			Assert.assertTrue("Old key=value is still in INOF field", !ve.getInfoStr().contains(keyValPrev));
		}
	}
}
