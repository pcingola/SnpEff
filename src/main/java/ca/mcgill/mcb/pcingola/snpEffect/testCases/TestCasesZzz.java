package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGff3;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	Config config;
	Genome genome;

	public TestCasesZzz() {
		super();
		Exon.ToStringVersion = 1; // Set "toString()" version
	}

	/**
	 * Build a genome from a GFF3 file and compare results to 'expected' results
	 * @param genome
	 * @param gff3File
	 * @param resultFile
	 */
	public SnpEffectPredictor buildAndCompare(String genome, String gff3File, String resultFile, boolean readSeqs, boolean createRandSequences) {
		String expectedResult = (resultFile == null ? "" : Gpr.readFile(resultFile).trim());

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 fgff3 = new SnpEffPredictorFactoryGff3(config);
		fgff3.setFileName(gff3File);
		fgff3.setReadSequences(readSeqs);
		fgff3.setCreateRandSequences(createRandSequences);
		SnpEffectPredictor sep = fgff3.create();

		// Compare result
		String result = show(sep.getGenome()).trim();
		System.out.println("Result:\n----------\n" + result + "\n----------\n");
		Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));

		return sep;
	}

	/**
	 * Show a genome in a 'standard' way
	 * @param genome
	 * @return
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
			// Sort genes
			genes.add(gene);

		for (Gene gene : genes) {
			sb.append(gene);
			for (Transcript tr : gene.sortedStrand())
				sb.append("\t\tCDS '" + tr.getId() + "': " + tr.cds() + "\n");
		}

		return sb.toString();
	}

	public void testCase_10_MaizeZmB73() {
		String genome = "testMaizeZmB73";
		String gff3File = "tests/testMaizeZmB73.gff3";
		String resultFile = "tests/testMaizeZmB73.txt";
		buildAndCompare(genome, gff3File, resultFile, false, true);
	}

}
