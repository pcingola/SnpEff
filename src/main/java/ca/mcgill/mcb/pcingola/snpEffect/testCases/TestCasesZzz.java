package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
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

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	@Test
	public void test_hgvs_INS_intergenic() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr22";
		String vcf = "tests/test_hgvs_INS_intergenic.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setFormatVersion(EffFormatVersion.FORMAT_ANN_1);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure the HCVGs annotaion is correct
		boolean ok = false;
		for (VcfEffect veff : ve.parseEffects()) {
			if (verbose) System.out.println("\t" + veff + "\t" + veff.getEffectsStr() + "\t" + veff.getHgvsDna());
			ok |= veff.hasEffectType(EffectType.INTERGENIC) //
					&& veff.getHgvsDna().equals("n.15069999_15070000insT") //
			;
		}

		Assert.assertTrue("Error in HGVS annotaiton", ok);
	}

}
