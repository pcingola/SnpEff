package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGff3;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for GFF3 file parsing
 * 
 * @author pcingola
 */
public class TestCasesGff3 extends TestCase {

	public TestCasesGff3() {
		super();
		Exon.ToStringVersion = 1; // Set "toString()" version
	}

	/**
	 * Build a genome from a GFF3 file and compare results to 'expected' results
	 * @param genome
	 * @param gff3File
	 * @param resultFile
	 */
	public SnpEffectPredictor buildAndCompare(String genome, String gff3File, String resultFile, boolean readSeqs) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 fgff3 = new SnpEffPredictorFactoryGff3(config);
		fgff3.setFileName(gff3File);
		fgff3.setReadSequences(readSeqs);
		SnpEffectPredictor sep = fgff3.create();

		// Compare result
		String result = show(sep.getGenome()).trim();
		System.out.println(result);
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

	public void testCase_Exon_Simple() {
		String genome = "testCase";
		String gff3File = "tests/exonSimple.gff3";
		String resultFile = "tests/exonSimple.txt";
		buildAndCompare(genome, gff3File, resultFile, true);
	}

	public void testCase_ExonIn() {
		String genome = "testCase";
		String gff3File = "tests/exonIn.gff3";
		String resultFile = "tests/exonIn.txt";
		buildAndCompare(genome, gff3File, resultFile, true);
	}

	public void testCase_ExonOut() {
		String genome = "testCase";
		String gff3File = "tests/exonOut.gff3";
		String resultFile = "tests/exonOut.txt";
		buildAndCompare(genome, gff3File, resultFile, true);
	}

	public void testCaseAthalianaTair10_AT5G66790() {
		String genome = "athalianaTair10";
		String gff3File = "tests/AT5G66790.gff3";
		String resultFile = "tests/AT5G66790.txt";
		buildAndCompare(genome, gff3File, resultFile, true);
	}

	public void testCasePaeruPA14muccA() {
		String genome = "paeru.PA14";
		String gff3File = "tests/paeru.PA14.muccA.gff";
		String resultFile = "tests/paeru.PA14.muccA.txt";
		SnpEffectPredictor sep = buildAndCompare(genome, gff3File, resultFile, true);

		// Make sure no splice site is added
		Gene gene = sep.getGenome().getGenes().iterator().next();
		Transcript tr = gene.iterator().next();
		List<SpliceSite> spliceSites = tr.createSpliceSites(SpliceSite.CORE_SPLICE_SITE_SIZE, 0, 0, 0);
		Assert.assertEquals(0, spliceSites.size());
	}

	public void testCasePpersica() {
		String genome = "ppersica139";
		String gff3File = "tests/ppersica_139.gff";
		String resultFile = "tests/ppersica_139.txt";
		buildAndCompare(genome, gff3File, resultFile, false);
	}

	public void testCaseRice5() {
		String genome = "testRice5";
		String gff3File = "tests/Os03t0150600.gff";
		String resultFile = "tests/Os03t0150600.txt";
		buildAndCompare(genome, gff3File, resultFile, false);
	}

	public void testCaseVibrio() {
		String genome = "vibrio";
		String gff3File = "tests/vibrio.gff3";
		String resultFile = "tests/vibrio.txt";
		buildAndCompare(genome, gff3File, resultFile, true);
	}
}
