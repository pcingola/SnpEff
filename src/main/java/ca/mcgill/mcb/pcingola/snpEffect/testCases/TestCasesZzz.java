package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	Config config;
	Genome genome;

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Basic parsing
	 */
	public void test_01() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (Variant seqChange : vcfEntry.variants()) {
				System.out.println(seqChange);
				String seqChangeStr = "chr" + seqChange.getChromosomeName() + ":" + seqChange.getStart() + "_" + seqChange.getReference() + "/" + seqChange.getChange();
				Assert.assertEquals(seqChangeStr, seqChange.getId());
			}
		}
	}
}
