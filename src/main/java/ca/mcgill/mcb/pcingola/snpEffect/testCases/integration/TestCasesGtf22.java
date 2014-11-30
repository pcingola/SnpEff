package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesGtf22 {

	boolean verbose = false;

	public TestCasesGtf22() {
		super();
		Exon.ToStringVersion = 1; // Show using old format
	}

	/**
	 * Build a genome from a GTF file and compare results to 'expected' results
	 */
	public void buildAndCompare(String genome, String gtf22, String fastaFile, String resultFile) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGtf22 fgtf22 = new SnpEffPredictorFactoryGtf22(config);
		fgtf22.setFileName(gtf22);
		fgtf22.setVerbose(verbose);

		// Set fasta file (or don't read sequences)
		if (fastaFile != null) fgtf22.setFastaFile(fastaFile);
		else fgtf22.setReadSequences(false);

		SnpEffectPredictor sep = fgtf22.create();

		// Compare result
		String result = show(sep.getGenome()).trim();
		if (verbose) System.out.println(result);
		Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));
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
	public void testCaseHg37_61_ENST00000250838() {
		Gpr.debug("Test");
		String genome = "testHg37.61";
		String gtfFile = "tests/ENST00000250838.gtf";
		String fastaFile = "tests/chrY.fa.gz";
		String resultFile = "tests/ENST00000250838.txt";
		buildAndCompare(genome, gtfFile, fastaFile, resultFile);
	}

	@Test
	public void testCaseHg37_61_ENST00000331397() {
		Gpr.debug("Test");
		String genome = "testHg37.61";
		String gtfFile = "tests/ENST00000331397.gtf22";
		String fastaFile = "tests/chrY.fa.gz";
		String resultFile = "tests/ENST00000331397.txt";
		buildAndCompare(genome, gtfFile, fastaFile, resultFile);
	}

	@Test
	public void testCaseMm37_61_ENSMUSG00000051951() {
		Gpr.debug("Test");
		String genome = "testMm37.61";
		String gtfFile = "tests/ENSMUSG00000051951.gtf";
		String resultFile = "tests/ENSMUSG00000051951.txt";
		buildAndCompare(genome, gtfFile, null, resultFile);
	}

	@Test
	public void testCaseMm37_61_ENSMUST00000070533() {
		Gpr.debug("Test");
		String genome = "testMm37.61";
		String gtfFile = "tests/ENSMUST00000070533.gtf";
		String resultFile = "tests/ENSMUST00000070533.txt";
		buildAndCompare(genome, gtfFile, null, resultFile);
	}

}
