package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test_start_codon_translate() {
		verbose = true;

		// Initialize
		String genomeName = "testHg19ChrM";
		Config config = new Config(genomeName);
		SnpEffectPredictor sep = config.loadSnpEffectPredictor();

		// Find transcript and make sure start codon is 'M'
		boolean checked = false;
		for (Gene g : sep.getGenome().getGenes()) {
			if (g.getId().equals("ENSG00000198763")) {
				Transcript tr = g.iterator().next();
				checked = true;
				Assert.assertEquals("MNPLAQPVIYSTIFAGTLITALSSHWFFTWVGLEMNMLAFIPVLTKKMNPRSTEAAIKYFLTQATASMILLMAILFNNMLSGQWTMTNTTNQYSSLMIMMAMAMKLGMAPFHFWVPEVTQGTPLTSGLLLLTWQKLAPISIMYQISPSLNVSLLLTLSILSIMAGSWGGLNQTQLRKILAYSSITHMGWMMAVLPYNPNMTILNLTIYIILTTTAFLLLNLNSSTTTLLLSRTWNKLTWLTPLIPSTLLSLGGLPPLTGFLPKWAIIEEFTKNNSLIIPTIMATITLLNLYFYLRLIYSTSITLLPMSNNVKMKWQFEHTKPTPFLPTLIALTTLLLPISPFMLMIL?", tr.protein());
			}
		}

		Assert.assertEquals(true, checked);
	}
}
