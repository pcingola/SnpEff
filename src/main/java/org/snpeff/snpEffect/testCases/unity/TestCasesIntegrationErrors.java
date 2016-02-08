package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.snpeff.snpEffect.VariantEffect.ErrorWarningType;
import org.snpeff.snpEffect.commandLine.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test cases for error reporting
 *
 * @author pcingola
 */
public class TestCasesIntegrationErrors {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesIntegrationErrors() {
		super();
	}

	@Test
	public void test_01_ERROR_CHROMOSOME_NOT_FOUND() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr1";
		String vcf = "tests/missing_chromo.vcf";

		String args[] = { "-noLog", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
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
		String vcf = "tests/missing_chromo.vcf";

		String args[] = { "-noLog", "-classic", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
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
