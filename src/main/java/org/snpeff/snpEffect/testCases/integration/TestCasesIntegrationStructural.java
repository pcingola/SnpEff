package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test SNP variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationStructural {

	boolean debug = false;
	boolean verbose = false || debug;

	/**
	 * Duplication creates a gene fusion
	 */
	@Test
	public void test_01_DUP_fusion() {
		Gpr.debug("Test");
		String genome = "hg19";
		String vcf = "tests/test_fusion_FGFR3-TACC3.vcf";

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
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
				if (veff.getEffectType() == EffectType.GENE_FUSION) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals(veff.getGeneId(), "FGFR3&TACC3");
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

	/**
	 * Duplication creates a gene fusion
	 */
	@Test
	public void test_02_INV_fusion() {
		Gpr.debug("Test");
		String genome = "testHg19Chr2";
		String vcf = "tests/test_fusion_EML4-ALK.vcf";

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
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
				if (veff.getEffectType() == EffectType.GENE_FUSION) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals(veff.getGeneId(), "ALK&EML4");
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

	/**
	 * Deletion creates a gene fusion
	 */
	@Test
	public void test_03_DEL_fusion() {
		Gpr.debug("Test");
		String genome = "testHg19Chr21";
		String vcf = "tests/test_fusion_TTC3-DSCAM.vcf";

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
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
				if (veff.getEffectType() == EffectType.GENE_FUSION_REVERESE) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals(veff.getGeneId(), "DSCAM&TTC3");
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

	/**
	 * Deletion creates a gene fusion
	 */
	@Test
	public void test_04_fusion() {
		Gpr.debug("Test");
		String genome = "testHg19Chr10";
		String vcf = "tests//test_fusion_CCDC6-RET.vcf";

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
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
				if (veff.getEffectType() == EffectType.GENE_FUSION) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals(veff.getGeneId(), "CCDC6&RET");
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

}
