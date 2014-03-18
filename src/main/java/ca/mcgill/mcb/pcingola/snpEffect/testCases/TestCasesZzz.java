package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
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

	public SnpEffectPredictor buildAndCompare(String genome, String gff3File, String resultFile, boolean readSeqs) {
		String expectedResult = (resultFile == null ? "" : Gpr.readFile(resultFile).trim());

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 fgff3 = new SnpEffPredictorFactoryGff3(config);
		fgff3.setFileName(gff3File);
		fgff3.setReadSequences(readSeqs);
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

	public void testCase_09_AP() {
		String genome = "testAP";
		String gff3File = "tests/testAP_genes.gff.gz";
		String resultFile = "tests/testAP.txt";

		// Capture STDERR
		PrintStream errOri = System.err;
		final ByteArrayOutputStream myErr = new ByteArrayOutputStream();
		System.setErr(new PrintStream(myErr));

		// Test
		try {
			buildAndCompare(genome, gff3File, resultFile, true);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Restore STDERR
			System.setErr(errOri);
		}

		// Show stderr and check message
		System.err.println("STDERR:\n" + myErr);
		Assert.assertTrue(myErr.toString().indexOf("WARNING: All frames are zero!") >= 0);
	}

}
