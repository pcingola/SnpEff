package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 *
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
		Gpr.debug("Test");

		String genome = "testHg19Chr8";
		String vcf = "tests/dup_01.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
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
			if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
			if (veff.getEffectType() == EffectType.CHROMOSOME_LARGE_DUPLICATION) {
				found |= veff.getGeneName().contains("FGFR1");
			}
		}

		Assert.assertTrue("Gene FGFR1 not found in <DUP>", found);
	}

}
