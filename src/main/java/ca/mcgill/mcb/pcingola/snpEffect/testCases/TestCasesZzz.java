package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	public void test_10_MixedVep_HGVS() {
		Gpr.debug("Test");
		String genome = "testHg19Hgvs";
		String vcf = "tests/hgvs_counsyl.vcf";
		CompareToVep comp = new CompareToVep(genome, verbose);
		comp.setCompareHgvs();
		comp.setCompareHgvsProt(false);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

}
