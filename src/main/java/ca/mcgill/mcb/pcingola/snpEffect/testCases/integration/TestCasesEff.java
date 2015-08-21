package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 *
 * Test cases for other 'effect' issues
 *
 * @author pcingola
 */
public class TestCasesEff {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesEff() {
		super();
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		// Arguments
		ArrayList<String> args = new ArrayList<String>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		cmdEff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		return list;
	}

	/**
	 * Test output order
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", null);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			EffectImpact impPrev = EffectImpact.HIGH;
			for (VcfEffect veff : ve.getVcfEffects()) {
				EffectImpact imp = veff.getImpact();

				if (verbose) System.out.println("\t" + imp + "\t" + impPrev + "\t" + imp.compareTo(impPrev) + "\t" + veff);
				Assert.assertTrue(impPrev.compareTo(imp) <= 0); // Higher impact go first
				impPrev = imp;
			}
		}
	}

	/**
	 * Test output order: Canonical first
	 */
	@Test
	public void test_01_canonical() {
		Gpr.debug("Test");
		List<VcfEntry> vcfEntries = snpEffect("testHg3775Chr8", "tests/eff_sort_canon.vcf", null);

		// Only one entry in this file
		Assert.assertEquals(1, vcfEntries.size());

		VcfEntry ve = vcfEntries.get(0);
		VcfEffect veff = ve.getVcfEffects().get(0);

		Assert.assertEquals("ENST00000456015", veff.getTranscriptId());
	}

	/**
	 * Test GATK option: At most one effect per VCF entry
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", args);

		for (VcfEntry ve : vcfEntries) {
			int numEffs = ve.getVcfEffects().size();
			if (verbose) System.out.println("Num effects:" + numEffs + "\t" + ve);
			Assert.assertTrue(numEffs <= 1);
		}
	}

	/**
	 * Make sure that empty VCF does not trigger an exception when creating the summary
	 */
	@Test
	public void test_03_EmptyVcf() {
		Gpr.debug("Test");
		String args[] = { "eff", "-noLog" };
		snpEffect("testHg3770Chr22", "tests/empty_only_header.vcf", args);
	}

	/**
	 * Test that CSV summary does not throw any error
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
		String args[] = { "-csvStats", "test_04_TestCasesEff.csv" };
		snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", args);
	}

	/**
	 * GATK mode should not have SPLICE_REGION (it is currently not supported)
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/gatk_NO_splice_regions.vcf";
		String args[] = { "eff", "-noLog", "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t'" + veff.getEffectsStr() + "'\t" + veff);
				if (veff.getEffectsStr().indexOf("SPLICE_SITE_REGION") >= 0) throw new RuntimeException("Splice region effects should not present in GATK compatible mode");
			}
		}
	}

	/**
	 * Test an MNP at the end of the transcript: We should be able to annotate without throwing any error
	 */
	@Test
	public void test_06() {
		Gpr.debug("Test");
		String args[] = {};
		List<VcfEntry> list = snpEffect("testHg3775Chr15", "tests/mnp_insertion_at_transcript_end.vcf", args);

		// We should be able to annotate this entry (if INFO is empty, something went wrong)
		Assert.assertFalse(list.get(0).getInfoStr().isEmpty());
	}

	/**
	 * Test an MNP at the end of the transcript: We should be able to annotate without throwing any error
	 */
	@Test
	public void test_07() {
		Gpr.debug("Test");
		String args[] = {};
		List<VcfEntry> list = snpEffect("testHg3775Chr10", "tests/mnp_deletion.vcf", args);

		// We should be able to annotate this entry (if INFO is empty, something went wrong)
		Assert.assertFalse(list.get(0).getInfoStr().isEmpty());
	}

	/**
	 * Fixing bug: GATK does not annotate all VCF entries
	 */
	@Test
	public void test_08_gatk_missing_annotations() {
		Gpr.debug("Test");

		String genomeName = "testMycobacterium_tuberculosis_CCDC5079_uid203790";
		String vcf = "tests/test_gatk_no_annotations.vcf";
		String args[] = { "-noLog", "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

		for (VcfEntry ve : vcfEntries) {
			int count = 0;
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t'" + veff.getEffectsStr() + "'\t" + veff);
				count++;
			}

			// Check that there is one and only one annotation
			Assert.assertEquals(1, count);
		}
	}

}
