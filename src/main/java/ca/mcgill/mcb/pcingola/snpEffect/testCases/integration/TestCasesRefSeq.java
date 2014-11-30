package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.SmithWaterman;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdBuild;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRefSeq;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesRefSeq {

	boolean debug = false;
	boolean verbose = false;

	public TestCasesRefSeq() {
		super();
	}

	/**
	 * Build a genome from a RefSeq file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildAndCompare(String genome, String refSeqFile, String fastaFile, String resultFile) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryRefSeq factory = new SnpEffPredictorFactoryRefSeq(config);
		factory.setFileName(refSeqFile);
		factory.setVerbose(verbose);

		// Set fasta file (or don't read sequences)
		if (fastaFile != null) factory.setFastaFile(fastaFile);
		else factory.setReadSequences(false);

		SnpEffectPredictor sep = factory.create();

		// Compare result
		String result = show(sep.getGenome()).trim();
		if (verbose) System.out.println(result);
		Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));

		return sep;
	}

	/**
	 * Show a genome in a 'standard' way
	 */
	String show(Genome genome) {
		StringBuilder sb = new StringBuilder();

		// Genome
		sb.append(genome.getVersion() + "\n");

		// Chromosomes
		for (Chromosome chr : genome)
			sb.append(chr + "\n");

		// Genes
		ArrayList<Gene> genes = new ArrayList<Gene>();
		for (Gene gene : genome.getGenes())
			genes.add(gene);
		Collections.sort(genes);

		for (Gene gene : genes) {
			// We don't compare protein codding in this test
			for (Transcript tr : gene.sortedStrand())
				tr.setProteinCoding(false);

			sb.append(gene);
			for (Transcript tr : gene.sortedStrand())
				sb.append("\t\tCDS '" + tr.getId() + "': " + tr.cds() + "\n");
		}

		return sb.toString();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		/// Build SnpEffectPredictor using a RefSeq file
		String genome = "hg19";
		String refSeqFile = "tests/hg19_refSeq_OR4F16.txt";
		String fastaFile = null; // "tests/chrY.fa.gz";
		String resultFile = "tests/hg19_refSeq_OR4F16.dump.txt";
		SnpEffectPredictor sep = buildAndCompare(genome, refSeqFile, fastaFile, resultFile);

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
		SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.snpEffCmd();
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
