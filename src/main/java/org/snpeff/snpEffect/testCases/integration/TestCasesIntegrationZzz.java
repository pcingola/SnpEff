package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	public TestCasesIntegrationZzz() {
		super();
	}

	/**
	 * Annotate intron rank in gene_fusion
	 */
	@Test
	public void test_06_fusion() {
		verbose = true;
		Gpr.debug("Test");
		String genome = "testHg19Chr3";
		String vcf = "tests/test_fusion_intron_rank.vcf";

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
				if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
				if (veff.getEffectType() == EffectType.TRANSCRIPT_DELETED && veff.getTranscriptId().equals("NM_001777.3")) {
					if (verbose) System.err.println("VcfEffect: " + veff);
					Assert.assertEquals("Expected rank does not match", 7, veff.getRank());
					Assert.assertEquals("Expected rankMax does not match", 10, veff.getRankMax());
					checked = true;
				}
			}
		}
		Assert.assertTrue("No translocation found", checked);
	}

}
