package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test cases for other 'effect' issues
 *
 * @author pcingola
 */
public class TestCasesEff extends TestCase {

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

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	/**
	 * Test output order
	 */
	public void test_01() {
		Gpr.debug("Test");
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", null);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			EffectImpact impPrev = EffectImpact.HIGH;
			for (VcfEffect veff : ve.parseEffects()) {
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
	public void test_01_canonical() {
		Gpr.debug("Test");
		List<VcfEntry> vcfEntries = snpEffect("testHg3775Chr8", "tests/eff_sort_canon.vcf", null);

		// Only one entry in this file
		Assert.assertEquals(1, vcfEntries.size());

		VcfEntry ve = vcfEntries.get(0);
		VcfEffect veff = ve.parseEffects().get(0);

		Assert.assertEquals("ENST00000456015", veff.getTranscriptId());
	}

	/**
	 * Test GATK option: At most one effect per VCF entry
	 */
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", args);

		for (VcfEntry ve : vcfEntries) {
			int numEffs = ve.parseEffects().size();
			if (verbose) System.out.println("Num effects:" + numEffs + "\t" + ve);
			Assert.assertTrue(numEffs <= 1);
		}
	}

	/**
	 * Make sure that empty VCF does not trigger an exception when creating the summary
	 */
	public void test_03_EmptyVcf() {
		Gpr.debug("Test");
		String args[] = { "eff", "-noLog" };
		snpEffect("testHg3770Chr22", "tests/empty_only_header.vcf", args);
	}

	/**
	 * Test that CSV summary does not throw any error
	 */
	public void test_04() {
		Gpr.debug("Test");
		String args[] = { "-csvStats" };
		snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", args);
	}

	/**
	 * GATK mode should not have SPLICE_REGION (it is currently not supported)
	 */
	public void test_05() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/gatk_NO_splice_regions.vcf";
		String args[] = { "eff", "-noLog", "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t'" + veff.getEffectsStr() + "'\t" + veff);
				if (veff.getEffectsStr().indexOf("SPLICE_SITE_REGION") >= 0) throw new RuntimeException("Splice region effects should not present in GATK compatible mode");
			}
		}
	}

	/**
	 * Test an MNP at the end of the transcript: We should be able to annotate without throwing any error
	 */
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
	public void test_07() {
		Gpr.debug("Test");
		String args[] = {};
		List<VcfEntry> list = snpEffect("testHg3775Chr10", "tests/mnp_deletion.vcf", args);

		// We should be able to annotate this entry (if INFO is empty, something went wrong)
		Assert.assertFalse(list.get(0).getInfoStr().isEmpty());
	}

}
