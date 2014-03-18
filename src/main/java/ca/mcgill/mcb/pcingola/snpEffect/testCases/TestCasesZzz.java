package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	Config config;
	Genome genome;

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	public void test_16_indels() {
		String vcfFile = "tests/1kg.indels.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			StringBuilder seqChangeResult = new StringBuilder();

			for (SeqChange sc : ve.seqChanges()) {
				if (seqChangeResult.length() > 0) seqChangeResult.append(",");
				seqChangeResult.append(sc.getReference() + "/" + sc.getChange());
			}

			String seqChangeExpected = ve.getInfo("SEQCHANGE");

			Assert.assertEquals(seqChangeExpected, seqChangeResult.toString());
		}
	}

}
