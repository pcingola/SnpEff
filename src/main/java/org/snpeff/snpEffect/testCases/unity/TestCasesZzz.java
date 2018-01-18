package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/large_deletion/";
	}

	/**
	 * TODO: Merge into 'TestCasesIntegrationLargeDeletion'
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
