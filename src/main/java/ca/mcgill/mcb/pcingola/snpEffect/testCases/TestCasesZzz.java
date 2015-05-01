package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
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
	 * Non-variant VCF entries should be skipped (i.e. no annotation should be added) 
	 */
	@Test
	public void test_03_do_not_annotate_non_variants() {
		String vcfFileName = "tests/test_non_variants.vcf";
		String genomeName = "testHg3775Chr1";

		// Prepare a command line
		String args[] = { "-noLog", genomeName, vcfFileName };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setSupressOutput(!verbose);
		snpEff.setVerbose(verbose);
		snpEff.setDebug(debug);

		// Run command
		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		List<VcfEntry> vcfEntries = seff.run(true);
		Assert.assertFalse("SnpEff run failed, returned an empty list", vcfEntries.isEmpty());

		// Check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			if (ve.hasInfo(EffFormatVersion.VCF_INFO_ANN_NAME) || ve.hasInfo(EffFormatVersion.VCF_INFO_EFF_NAME)) //
				throw new RuntimeException("Effect field should not be annotated on non-variant entries!\n" + ve);

		}
	}
}
