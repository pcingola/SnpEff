package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.Hgvs;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 *
 * Test case
 */
public class TestCasesHgvsDnaDupIntegration extends TestCasesBase {

	public TestCasesHgvsDnaDupIntegration() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		minExons = 1;
		randSeed = 20141128;
		initRand();
	}

	/**
	 */
	@Test
	public void test_01_dup() {
		Gpr.debug("Test");

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_dup.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// The problem appears when splice site is large (in this example)
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);

		// Make sure entries are annotated as expected
		for (VcfEntry ve : results) {
			// Extract expected HGVS values
			String hgvsCexp = ve.getInfo("HGVS_C") != null ? ve.getInfo("HGVS_C") : "";
			String trIdC = Hgvs.parseTranscript(hgvsCexp);
			hgvsCexp = Hgvs.removeTranscript(hgvsCexp);

			String hgvsPexp = ve.getInfo("HGVS_P") != null ? ve.getInfo("HGVS_P") : "";
			String trIdP = Hgvs.parseTranscript(hgvsPexp);
			hgvsPexp = Hgvs.removeTranscript(hgvsPexp);

			if (verbose) {
				System.out.println(ve);
				if (trIdC != null) System.out.println("\tExpected HGVS_C: " + trIdC + ":" + hgvsCexp);
				if (trIdP != null) System.out.println("\tExpected HGVS_P: " + trIdP + ":" + hgvsPexp + "\n");
			}

			// Check all effects
			boolean okC = false, okP = false;
			for (VcfEffect veff : ve.getVcfEffects()) {
				// Parse calculated HGVS values
				String trId = veff.getTranscriptId();
				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";
				if (verbose) System.out.println("\t" + veff //
						+ "\n\t\tEFF    : " + veff.getEffectsStr() //
						+ "\n\t\tHGVS_C : " + hgvsCactual //
						+ "\n\t\tHGVS_P : " + hgvsPactual //
						+ "\n");

				// Compare results
				if (trId != null && trId.equals(trIdC)) {
					Assert.assertEquals(hgvsCexp, hgvsCactual);
					okC = true;
				}

				if (trId != null && trId.equals(trIdP)) {
					Assert.assertEquals(hgvsPexp, hgvsPactual);
					okP = true;
				}
			}

			Assert.assertTrue("HGVS (DNA) not found: '" + hgvsCexp + "'", okC);
			if (!hgvsPexp.isEmpty()) Assert.assertTrue("HGVS (Protein) not found: '" + hgvsPexp + "'", okP);
		}
	}

}
