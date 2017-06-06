package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test case where VCF entries are huge (e.g. half chromosome deleted)
 *
 * @author pcingola
 */
public class TestCasesIntegrationHugeDeletions {

	boolean verbose = false;

	public TestCasesIntegrationHugeDeletions() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", "./tests/integration/hgvsHugeDeletions/huge_deletion_DEL.vcf" };

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
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", "./tests/integration/hgvsHugeDeletions/huge_deletion.vcf.gz" };

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
		String args[] = { "-classic", "-noOut", "testHg19Chr9", "./tests/integration/hgvsHugeDeletions/huge_deletion_chr9.vcf" };

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

}
