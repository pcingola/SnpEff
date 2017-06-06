package org.snpeff.snpEffect.testCases.integration;

import java.io.IOException;

import org.junit.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Protein translation test case
 *
 * @author pcingola
 */
public class TestCasesIntegrationProtein {

	public static boolean verbose = false;

	@Test
	public void test_01() throws IOException {
		Gpr.debug("Test");
		String args[] = { "testHg3763ChrY", "./tests/integration/proteinInteraction/proteins_testHg3763ChrY.txt" };

		SnpEffCmdProtein cmd = new SnpEffCmdProtein();
		cmd.parseArgs(args);
		cmd.run();

		// Check that it is OK
		Assert.assertEquals(0, cmd.getTotalErrors());
		Assert.assertEquals(true, cmd.getTotalOk() >= 167);
	}

	@Test
	public void test_start_codon_translate() {
		Gpr.debug("Test");

		// Initialize
		String genomeName = "testHg19ChrM";
		Config config = new Config(genomeName);
		SnpEffectPredictor sep = config.loadSnpEffectPredictor();

		// Find transcript and make sure start codon is 'M'
		boolean checked = false;
		for (Gene g : sep.getGenome().getGenes()) {
			if (verbose) System.out.println(g);
			if (g.getId().equals("ENSG00000198763")) {
				Transcript tr = g.iterator().next();
				checked = true;
				Assert.assertEquals("MNPLAQPVIYSTIFAGTLITALSSHWFFTWVGLEMNMLAFIPVLTKKMNPRSTEAAIKYFLTQATASMILLMAILFNNMLSGQWTMTNTTNQYSSLMIMMAMAMKLGMAPFHFWVPEVTQGTPLTSGLLLLTWQKLAPISIMYQISPSLNVSLLLTLSILSIMAGSWGGLNQTQLRKILAYSSITHMGWMMAVLPYNPNMTILNLTIYIILTTTAFLLLNLNSSTTTLLLSRTWNKLTWLTPLIPSTLLSLGGLPPLTGFLPKWAIIEEFTKNNSLIIPTIMATITLLNLYFYLRLIYSTSITLLPMSNNVKMKWQFEHTKPTPFLPTLIALTTLLLPISPFMLMIL?", tr.protein());
			}
		}

		Assert.assertEquals(true, checked);
	}

}
