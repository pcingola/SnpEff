package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	public TestCasesIntegrationZzz() {
		super();
	}

	@Test
	public void test_11_ExonRank() {
		Gpr.debug("Test");

		String vcfFileName = "tests/mixed_11.vcf";
		String args[] = { "testHg19Chr20", vcfFileName };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		VcfEntry ve = vcfEnties.get(0);

		// Get first effect (there should be only one)
		List<VcfEffect> veffs = ve.getVcfEffects();
		VcfEffect veff = veffs.get(0);

		Assert.assertEquals("Exon rank does not match", 12, veff.getRank());

	}

}
