package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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

		// Sort genes
		for (Gene gene : genome.getGenes())
			genes.add(gene);
		Collections.sort(genes);

		// Show genes
		for (Gene gene : genes) {
			sb.append(gene);
			for (Transcript tr : gene.sortedStrand())
				sb.append("\t\tCDS '" + tr.getId() + "': " + tr.cds() + "\n");
		}

		return sb.toString();
	}

	public void testCase_01_Exon_Simple() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonSimple.gff3";
		String resultFile = "tests/exonSimple.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_02_ExonIn() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonIn.gff3";
		String resultFile = "tests/exonIn.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_03_ExonOut() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonOut.gff3";
		String resultFile = "tests/exonOut.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_04_AthalianaTair10_AT5G66790() {
		Gpr.debug("Test");
		String genome = "athalianaTair10";
		String gff3File = "tests/AT5G66790.gff3";
		String resultFile = "tests/AT5G66790.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_05_PaeruPA14muccA() {
		Gpr.debug("Test");
		String genome = "paeru.PA14";
		String gff3File = "tests/paeru.PA14.muccA.gff";
		String resultFile = "tests/paeru.PA14.muccA.txt";
		SnpEffectPredictor sep = buildAndCompare(genome, gff3File, resultFile, true, false);

		// Make sure no splice site is added
		Gene gene = sep.getGenome().getGenes().iterator().next();
		Transcript tr = gene.iterator().next();
		List<SpliceSite> spliceSites = tr.createSpliceSites(SpliceSite.CORE_SPLICE_SITE_SIZE, 0, 0, 0);
		Assert.assertEquals(0, spliceSites.size());
	}

	public void testCase_06_Ppersica() {
		Gpr.debug("Test");
		String genome = "ppersica139";
		String gff3File = "tests/ppersica_139.gff";
		String resultFile = "tests/ppersica_139.txt";
		buildAndCompare(genome, gff3File, resultFile, false, false);
	}

	public void testCase_07_Rice5() {
		Gpr.debug("Test");
		String genome = "testRice5";
		String gff3File = "tests/Os03t0150600.gff";
		String resultFile = "tests/Os03t0150600.txt";
		buildAndCompare(genome, gff3File, resultFile, false, false);
	}

	public void testCase_08_Vibrio() {
		Gpr.debug("Test");
		String genome = "vibrio";
		String gff3File = "tests/vibrio.gff3";
		String resultFile = "tests/vibrio.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_09() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/testAP_genes.gff.gz";
		String resultFile = "tests/testAP.txt";
		buildAndCompare(genome, gff3File, resultFile, true, false);
	}

	public void testCase_09_AP() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/testAP_genes.gff.gz";
		String resultFile = "tests/testAP.txt";

		// Capture STDERR
		PrintStream errOri = System.err;
		final ByteArrayOutputStream myErr = new ByteArrayOutputStream();
		System.setErr(new PrintStream(myErr));

		// Test
		try {
			buildAndCompare(genome, gff3File, resultFile, true, false);
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

	public void testCase_10_MaizeZmB73() {
		Gpr.debug("Test");
		String genome = "testMaizeZmB73";
		String gff3File = "tests/testMaizeZmB73.gff3";
		String resultFile = "tests/testMaizeZmB73.txt";
		buildAndCompare(genome, gff3File, resultFile, false, true);
	}

}
