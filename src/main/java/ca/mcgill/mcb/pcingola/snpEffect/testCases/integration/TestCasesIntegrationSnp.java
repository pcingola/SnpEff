package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test SNP variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnp {

	boolean debug = false;
	boolean verbose = false || debug;

	/**
	 * Change of start codon to an alternative start codon
	 */
	@Test
	public void test_02_Start_NonSyn() {
		Gpr.debug("Test");
		String genome = "testHg19ChrM";
		String vcf = "tests/test_chrM_start_codon_nonSyn.vcf";

		String args[] = { "-noLog", "-classic", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				if (veff.getEffectType() == EffectType.NON_SYNONYMOUS_START) {
					Assert.assertEquals(EffectImpact.LOW, veff.getImpact());
					checked = true;
				}
			}
		}
		Assert.assertEquals(true, checked);

	}

	/**
	 * Stop gained should have 'HIGH' impact
	 */
	@Test
	public void test_02_StopGained_HighImpact() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr2";
		String vcf = "tests/stop_gained_chr2.vcf";

		String args[] = { "-noLog", "-classic", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				if (veff.getEffectType() == EffectType.STOP_GAINED) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					checked = true;
				}
			}
		}

		Assert.assertEquals(true, checked);
	}

	/**
	 * Change of start codon to an alternative start codon
	 */
	@Test
	public void test_03_Start_Loss() {
		Gpr.debug("Test");
		String genome = "testHg19ChrM";
		String vcf = "tests/test_chrM_start_codon.vcf";

		String args[] = { "-noLog", "-classic", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t\t" + veff);
				if (veff.getEffectType() == EffectType.START_LOST) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					checked = true;
				}
			}
		}
		Assert.assertEquals(true, checked);

	}

}
