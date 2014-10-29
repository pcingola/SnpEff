package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCase {

	boolean debug = true;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_13_large_Del_Hgvs() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr22";
		String vcf = "tests/test_large_del_hgvs_13.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure HGVS string is not so long
		for (VcfEffect veff : ve.parseEffects()) {
			System.out.println(veff);

			System.out.println("\tAA change    : " + veff.getAa());
			Assert.assertTrue(veff.getAa() == null || veff.getAa().length() < 100);

			System.out.println("\tCodon change : " + veff.getCodon());
			Assert.assertTrue(veff.getCodon() == null || veff.getCodon().length() < 100);

		}
	}

}
