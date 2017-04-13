package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
	}

	String effectStr(VariantEffect effect) {
		String effStr = effect.effect(true, true, true, false, false);
		String aaStr = effect.getAaChangeOld();
		int idx = effStr.indexOf('(');
		return effStr.substring(0, idx) + "(" + aaStr + ")";
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20100629;
	}

	/**
	 * Test MNP simplification
	 */
	@Test
	public void test_16_MNP() {
		Gpr.debug("Test");

		verbose = true;

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_MNP_16.vcf";

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

		// Check HGVS 'p.' notation
		boolean ok = false;
		for (VcfEffect veff : ve.getVcfEffects()) {
			if (verbose) Gpr.debug("\t" + veff + "\n\t\ttranscript: " + veff.getTranscriptId() + "\n\t\tHgvs (DNA): " + veff.getHgvsDna());
			if (veff.getTranscriptId().equals("NM_001042492.2")) {
				Assert.assertEquals("HGVS p. notation does not match", "p.Gln1055*", veff.getHgvsProt());
				ok = true;
			}
		}

		Assert.assertTrue("Transcript not found", ok);
	}

}
