package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRefSeq;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for GTF22 file parsing
 * 
 * @author pcingola
 */
public class TestCasesRefSeq extends TestCase {

	public TestCasesRefSeq() {
		super();
	}

	/**
	 * Build a genome from a RefSeq file and compare results to 'expected' results
	 * @param genome
	 * @param refSeqFile
	 * @param resultFile
	 */
	public SnpEffectPredictor buildAndCompare(String genome, String refSeqFile, String fastaFile, String resultFile) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryRefSeq factory = new SnpEffPredictorFactoryRefSeq(config);
		factory.setFileName(refSeqFile);

		// Set fasta file (or don't read sequences)
		if (fastaFile != null) factory.setFastaFile(fastaFile);
		else factory.setReadSequences(false);

		SnpEffectPredictor sep = factory.create();

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

	public void test_01() {
		/// Build SnpEffectPredictor using a RefSeq file
		String genome = "hg19";
		String refSeqFile = "tests/hg19_refSeq_OR4F16.txt";
		String fastaFile = null; // "tests/chrY.fa.gz";
		String resultFile = "tests/hg19_refSeq_OR4F16.dump.txt";
		SnpEffectPredictor sep = buildAndCompare(genome, refSeqFile, fastaFile, resultFile);

		// Check a SNP
		sep.buildForest();
		SeqChange seqChange = new SeqChange(sep.getGenome().getChromosome("1"), 521603, "A", "G", 1, "", 1, 1);
		List<ChangeEffect> effs = sep.seqChangeEffect(seqChange);
		for (ChangeEffect eff : effs) {
			System.out.println("\t" + eff);
			Assert.assertEquals(eff.getEffectType(), EffectType.INTERGENIC);
		}

	}
}
