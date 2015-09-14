package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Nonsense mediated decay prediction
 *
 * @author pcingola
 */
public class TestCasesIntegrationNmd extends TestCasesIntegrationBase {

	public TestCasesIntegrationNmd() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Load database
		String genomeVer = "testHg3766Chr1";
		Gpr.debug("Loading database '" + genomeVer + "'");
		Config config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
		config.setTreatAllAsProteinCoding(true); // For historical reasons...
		config.loadSnpEffectPredictor();

		// For each gene, transcript, check that NMD works
		int countTest = 1;
		for (Gene gene : config.getGenome().getGenes()) {
			if (verbose) System.err.println("NMD test\tGene ID:" + gene.getId());
			for (Transcript tr : gene) {
				if (debug) System.err.println(tr);
				checkNmd(config, gene, tr);

				if (verbose) System.err.print("\tTranscript " + tr.getId() + " " + (tr.isStrandPlus() ? '+' : '-') + " :");
				else Gpr.showMark(countTest++, SHOW_EVERY);

			}
		}
	}
}
