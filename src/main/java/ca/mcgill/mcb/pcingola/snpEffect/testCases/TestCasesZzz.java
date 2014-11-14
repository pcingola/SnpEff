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
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_15_walk_and_roll_1() {
		Gpr.debug("Test");
		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_walk_and_roll.1.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// The problem appears when splice site is large (in this example)
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);

		// Make sure the spleice site is annotatted as "c.1909+12delT" (instead of "c.1910delT")
		boolean ok = false;
		for (VcfEntry ve : results) {
			System.out.println(ve);
			System.out.println("\tHGVS_C: " + ve.getInfo("HGVS_C"));
			System.out.println("\tHGVS_P: " + ve.getInfo("HGVS_P"));

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t" + veff + "\t" + veff.getEffectsStr() + "\t" + veff.getHgvsDna() + "\t" + veff.getHgvsProt() + "\t");
			}
		}

		Assert.assertTrue(ok);
	}

	//	/**
	//	 * Compare to SAVANT's case (poster on BRCA2 mutations)
	//	 */
	//	public void test_16_savant() {
	//		Gpr.debug("Test");
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/hgvs_savant.vcf";
	//
	//		// Create SnpEff
	//		String args[] = { genome, vcf };
	//		SnpEffCmdEff snpeff = new SnpEffCmdEff();
	//		snpeff.parseArgs(args);
	//		snpeff.setDebug(debug);
	//		snpeff.setVerbose(verbose);
	//		snpeff.setSupressOutput(!verbose);
	//
	//		// The problem appears when splice site is large (in this example)
	//		snpeff.setUpDownStreamLength(0);
	//
	//		// Run & get result (single line)
	//		List<VcfEntry> results = snpeff.run(true);
	//		VcfEntry ve = results.get(0);
	//
	//		// Make sure the spleice site is annotatted as "c.1909+12delT" (instead of "c.1910delT")
	//		boolean ok = false;
	//		for (VcfEffect veff : ve.parseEffects()) {
	//			if (verbose) System.out.println("\t" + veff + "\t" + veff.getEffectsStr() + "\t" + veff.getHgvsDna());
	//			ok |= veff.getEffectsStr().equals("SPLICE_SITE_REGION") //
	//					&& veff.getTranscriptId().equals("NM_001232.3") //
	//					&& veff.getHgvsDna().equals("c.420+6T>C");
	//		}
	//
	//		Assert.assertTrue(ok);
	//	}
}
