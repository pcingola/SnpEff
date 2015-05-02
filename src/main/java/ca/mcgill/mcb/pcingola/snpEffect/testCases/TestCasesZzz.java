package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
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

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				Assert.assertEquals(ErrorWarningType.ERROR_CHROMOSOME_NOT_FOUND.toString(), veff.getErrorsWarning());
				count++;
			}

		}

		Assert.assertEquals(9, count);
	}
}
