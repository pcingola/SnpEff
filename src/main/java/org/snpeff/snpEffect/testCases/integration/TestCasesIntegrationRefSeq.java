package org.snpeff.snpEffect.testCases.integration;

import java.util.HashMap;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.align.SmithWaterman;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationRefSeq extends TestCasesIntegrationBase {

	public TestCasesIntegrationRefSeq() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		/// Build SnpEffectPredictor using a RefSeq file
		String genome = "hg19";
		String refSeqFile = "tests/integration/refSeq/hg19_refSeq_OR4F16.txt";
		String fastaFile = null; // "tests/chrY.fa.gz";
		String resultFile = "tests/integration/refSeq/hg19_refSeq_OR4F16.dump.txt";
		SnpEffectPredictor sep = buildAndCompare(genome, refSeqFile, fastaFile, resultFile, true);

		// Check a SNP
		sep.buildForest();
		Variant seqChange = new Variant(sep.getGenome().getChromosome("1"), 521603, "A", "G");
		VariantEffects effs = sep.variantEffect(seqChange);
		for (VariantEffect eff : effs) {
			if (verbose) System.out.println("\t" + eff);
			Assert.assertEquals(eff.getEffectType(), EffectType.INTERGENIC);
		}
	}

	/**
	 * Test improved exon frame correction in UCSC references
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		//---
		/// Build SnpEffectPredictor using a RefSeq file
		//---
		String genome = "testNM_015296";
		String args[] = { "build", genome };
		SnpEff snpeff = new SnpEff(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);

		// Build database
		SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.cmd();
		snpeffBuild.setStoreAlignments(true);
		snpeffBuild.run();

		//---
		// Make sure the alignment matches on most bases after exon rank 49
		//---
		HashMap<String, SmithWaterman> alignmentByTrId = snpeffBuild.getSnpEffCmdProtein().getAlignmentByTrId();
		SmithWaterman sw = alignmentByTrId.get("NM_015296.2");
		if (debug) Gpr.debug(sw.getAlignmentScore() + "\n" + sw);
		Assert.assertTrue(sw.getAlignmentScore() >= 2061);
	}
}
