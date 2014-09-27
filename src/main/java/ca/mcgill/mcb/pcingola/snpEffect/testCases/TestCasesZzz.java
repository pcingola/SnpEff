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

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_12_BRCA_Splice_15_Hgvs() {
		Gpr.debug("Test");
		int spliceSize = 15;
		String genome = "test_BRCA";
		String vcf = "tests/test_BRCA_splice_15.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// The problem appears when splice site is large (in this example)
		snpeff.setSpliceSiteSize(spliceSize);
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure the spleice site is annotatted as "c.1909+12delT" (instead of "c.1910delT")
		boolean ok = false;
		for (VcfEffect veff : ve.parseEffects())
			ok |= veff.getTranscriptId().equals("ENST00000544455") && veff.getHgvsDna().equals("c.1909+12delT");

		Assert.assertTrue(ok);
	}
}
