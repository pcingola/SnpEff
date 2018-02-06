package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.VariantEffect.ErrorWarningType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test cases for error reporting
 *
 * @author pcingola
 */
public class TestCasesIntegrationErrors extends TestCasesIntegrationBase {

	public TestCasesIntegrationErrors() {
		super();
	}

	@Test
	public void test_01_ERROR_CHROMOSOME_NOT_FOUND() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr1";
		String vcf = path("missing_chromo.vcf");

		String args[] = { "-noLog", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		List<VcfEntry> vcfEntries = seff.run(true);
		int count = 0;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				Assert.assertEquals(ErrorWarningType.ERROR_CHROMOSOME_NOT_FOUND.toString(), veff.getErrorsWarning());
				count++;
			}

		}

		Assert.assertEquals(9, count);
	}

	@Test
	public void test_02_ERROR_CHROMOSOME_NOT_FOUND() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr1";
		String vcf = path("missing_chromo.vcf");

		String args[] = { "-noLog", "-classic", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		List<VcfEntry> vcfEntries = seff.run(true);
		int count = 0;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				Assert.assertEquals(ErrorWarningType.ERROR_CHROMOSOME_NOT_FOUND.toString(), veff.getErrorsWarning());
				count++;
			}

		}

		Assert.assertEquals(9, count);
	}

}
