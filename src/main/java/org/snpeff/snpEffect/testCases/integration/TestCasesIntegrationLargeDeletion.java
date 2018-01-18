package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test case where VCF entries are huge (e.g. half chromosome deleted)
 *
 * @author pcingola
 */
public class TestCasesIntegrationLargeDeletion extends TestCasesIntegrationBase {

	public TestCasesIntegrationLargeDeletion() {
		super();
		testsDir = "tests/integration/large_deletion/";
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", testsDir + "huge_deletion_DEL.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getEnd() + "\tsize:" + ve.size());

			boolean ok = false;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println(veff);
				ok |= (veff.getEffectType() == EffectType.CHROMOSOME_LARGE_DELETION);
			}

			if (!ok) Assert.assertTrue("Expecting 'CHROMOSOME_LARGE_DELETION', not found", ok);
		}
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", testsDir + "huge_deletion.vcf.gz" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());
			Assert.assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));
		}
	}

	@Test
	public void test_03() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg19Chr9", testsDir + "huge_deletion_chr9.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		boolean okCdkn2a = false;
		boolean okCdkn2aTr = false;
		boolean okCdkn2b = false;

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());

			Assert.assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff);

				EffectType eff = veff.getEffectType();
				String geneName = veff.getGeneName();
				String trId = veff.getTranscriptId();

				if (eff == EffectType.GENE_DELETED && geneName.equals("CDKN2A")) okCdkn2a = true;
				if (eff == EffectType.GENE_DELETED && geneName.equals("CDKN2B")) okCdkn2b = true;
				if (eff == EffectType.TRANSCRIPT_DELETED && trId.equals("NM_000077.4")) okCdkn2aTr = true;
			}
		}

		Assert.assertTrue("GENE_DELETED CDKN2A: Not found", okCdkn2a);
		Assert.assertTrue("GENE_DELETED CDKN2B: Not found", okCdkn2b);
		Assert.assertTrue("TRANSCRIPT_DELETED CDKN2A (NM_000077.4): Not found", okCdkn2aTr);
	}

	/**
	 * Show transcripts deleted when there is a gene-fusion due to deletion
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
		String genome = "testHg19Chr9";
		String vcfFile = testsDir + "huge_deletion_fusion_chr9.vcf";
		List<VcfEntry> vcfs = snpEffect(genome, vcfFile, null, EffFormatVersion.FORMAT_ANN_1);

		// Sanity check
		Assert.assertEquals(1, vcfs.size());

		// Find effects
		boolean foundFusion = false, foundTrDel = false, foundExDel = false;
		for (VcfEffect veff : vcfs.get(0).getVcfEffects()) {
			if (verbose) System.out.println(veff);

			// Fusion
			if (veff.getEffectType() == EffectType.GENE_FUSION_REVERESE //
					&& veff.getGeneName().equals("CDKN2A&CDKN2B-AS1") //
			) {
				if (verbose) System.out.println("FOUND:\t" + veff);
				foundFusion = true;
			}

			// Transcript deletion
			if (veff.getEffectType() == EffectType.TRANSCRIPT_DELETED //
					&& veff.getTranscriptId().equals("NM_004936.3") //
			) {
				if (verbose) System.out.println("FOUND:\t" + veff);
				foundTrDel = true;
			}

			// Exon deletion
			if (veff.getEffectType() == EffectType.EXON_DELETED //
					&& veff.getTranscriptId().equals("NM_001195132.1") //
			) {
				if (verbose) System.out.println("FOUND EXON LOSS:\t" + veff);
				foundExDel = true;
			}

		}

		// All three must be present
		Assert.assertTrue("Could not find expected gene fusion", foundFusion);
		Assert.assertTrue("Could not find expected transcript deletion", foundTrDel);
		Assert.assertTrue("Could not find expected exon deletion", foundExDel);
	}

}
