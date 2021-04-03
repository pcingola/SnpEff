package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test SNP variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationStructural extends TestCasesIntegrationBase {

	/**
	 * Duplication creates a gene fusion
	 */
	@Test
	public void test_01_DUP_fusion() {
		Log.debug("Test");

		String genome = "testHg19Chr4";
		String vcf = path("test_fusion_FGFR3-TACC3.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
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
		Log.debug("Test");
		String genome = "testHg19Chr2";
		String vcf = path("test_fusion_EML4-ALK.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
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
		Log.debug("Test");
		String genome = "testHg19Chr21";
		String vcf = path("test_fusion_TTC3-DSCAM.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
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
		Log.debug("Test");
		String genome = "testHg19Chr10";
		String vcf = path("/test_fusion_CCDC6-RET.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
				if (veff.getEffectType() == EffectType.GENE_FUSION) {
					Assert.assertEquals(EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals(veff.getGeneId(), "CCDC6&RET");
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
	public void test_05_fusion() {
		Log.debug("Test");
		String genome = "testHg19Chr4Chr6";
		String vcf = path("test_fusion_ROS1-SLC34A2.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
				if (veff.getEffectType() == EffectType.GENE_FUSION) {
					Assert.assertEquals("Impact does not match", EffectImpact.HIGH, veff.getImpact());
					Assert.assertEquals("Affected genes do not match", "ROS1&SLC34A2", veff.getGeneId());
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

	/**
	 * Annotate intron rank in gene_fusion
	 */
	@Test
	public void test_06_fusion() {
		Log.debug("Test");
		String genome = "testHg19Chr3";
		String vcf = path("test_fusion_intron_rank.vcf");

		String args[] = { "-noLog", "-ud", "0", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		boolean checked = false;
		List<VcfEntry> vcfEntries = seff.run(true);
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t\t" + veff);
				if (veff.getEffectType() == EffectType.TRANSCRIPT_DELETED && veff.getTranscriptId().equals("NM_001777.3")) {
					if (verbose) Log.info("VcfEffect: " + veff);
					Assert.assertEquals("Expected rank does not match", 7, veff.getRank());
					Assert.assertEquals("Expected rankMax does not match", 10, veff.getRankMax());
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}
}
