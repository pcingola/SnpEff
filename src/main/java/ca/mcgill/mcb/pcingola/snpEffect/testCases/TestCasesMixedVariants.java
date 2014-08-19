package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test mixed variants
 *
 * @author pcingola
 */
public class TestCasesMixedVariants extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesMixedVariants() {
		super();
		init();
	}

	public void compareVep(String genome, String vcf) {
		compareVep(genome, vcf, false);

	}

	/**
	 * Compare with results from ENSEMBL's VEP to SnpEff
	 * Use VCF having VEP's results
	 */
	public void compareVep(String genome, String vcf, boolean compareHgsv) {
		CompareToVep comp = new CompareToVep(genome, verbose);
		if (compareHgsv) comp.setCompareHgvs();
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	void init() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(20140808);
	}

	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// Create predictor
		snpEffectPredictor = sepf.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	//	/**
	//	 * Make sure we can read VCF and parse variants without producing any exception
	//	 */
	//	public void test_01_MixedVep() {
	//		Gpr.debug("Test");
	//		String vcfFile = "tests/mixed_01.vcf";
	//
	//		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
	//		for (VcfEntry ve : vcf) {
	//			if (verbose) System.out.println(ve);
	//			for (Variant var : ve.variants()) {
	//				if (verbose) System.out.println("\t" + var);
	//			}
	//		}
	//	}
	//
	//	public void test_02_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr22", "tests/mixed_chr22.vcf");
	//	}
	//
	//	public void test_03_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr14", "tests/mixed_chr14.vcf");
	//	}
	//
	//	public void test_04_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr12", "tests/mixed_chr12.vcf");
	//	}
	//
	//	public void test_05_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr22", "tests/mixed_chr22.vcf");
	//	}
	//
	//	public void test_06_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr7", "tests/mixed_chr7.vcf");
	//	}
	//
	//	public void test_07_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr6", "tests/mixed_chr6.vcf");
	//	}
	//
	//	public void test_08_MixedVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3775Chr1", "tests/mixed_chr1.vcf");
	//	}
	//
	//	public void test_09_MixedVep() {
	//		Gpr.debug("Test");
	//
	//		String vcfFileName = "tests/mixed_09.vcf";
	//		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
	//		for (VcfEntry ve : vcf) {
	//			if (verbose) System.out.println(ve);
	//			for (Variant v : ve.variants()) {
	//				if (verbose) System.out.println("\t\t" + v);
	//				Assert.assertTrue("Variant is not MIXED", v.getVariantType() == VariantType.MIXED);
	//			}
	//		}
	//	}

	public void test_10_MixedVep_HGVS() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr1", "tests/mixed_10_hgvs.vep.vcf", true);
	}

}
