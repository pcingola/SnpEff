package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;

/**
 * Base class for some test cases
 *
 * @author pcingola
 */
public class TestCasesBase extends TestCase {

	protected boolean debug = false;
	protected boolean verbose = false || debug;

	protected Random rand;
	protected Config config;
	protected Genome genome;
	protected Chromosome chromosome;
	protected Gene gene;
	protected Transcript transcript;
	protected SnpEffectPredictor snpEffectPredictor;
	protected String chromoSequence = "";
	protected char chromoBases[];
	protected boolean addUtrs = false;
	protected boolean onlyPlusStrand = true;

	void initRand() {
		rand = new Random(20141128);
	}

	/**
	 * Create a predictor
	 */
	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Initialize factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(onlyPlusStrand); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
		sepf.setAddUtrs(addUtrs);

		// Create predictor
		snpEffectPredictor = sepf.create();

		// Update config
		config.setSnpEffectPredictor(snpEffectPredictor);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(0);

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	@Override
	protected void setUp() {
		initRand();
		initSnpEffPredictor();
	}

}
