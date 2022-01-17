package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesIntegrationDup extends TestCasesIntegrationBase {

	public TestCasesIntegrationDup() {
		super();
	}

	/**
	 */
	@Test
	public void test_01_dup() {
		Log.debug("Test");

		String genome = "testHg19Chr8";
		String vcf = path("dup_01.vcf");

		// Create SnpEff
		String[] args = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setFormatVersion(EffFormatVersion.FORMAT_ANN_1);

		// The problem appears when splice site is large (in this example)
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);

		// Make sure entries are annotated as expected
		VcfEntry ve = results.get(0);
		boolean found = false;
		for (VcfEffect veff : ve.getVcfEffects()) {
			if (verbose) Log.info("\t" + veff.getEffectType() + "\t" + veff);
			if (veff.getEffectType() == EffectType.CHROMOSOME_LARGE_DUPLICATION) {
				found |= veff.getGeneName().contains("FGFR1");
			}
		}

		assertTrue(found, "Gene FGFR1 not found in <DUP>");
	}

}
