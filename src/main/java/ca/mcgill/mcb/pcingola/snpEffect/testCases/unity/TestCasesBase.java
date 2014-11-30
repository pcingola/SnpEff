package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import org.junit.Before;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
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
public class TestCasesBase {

	protected boolean debug = false;
	protected boolean verbose = false || debug;

	// Parameters for creating fake genome
	protected int randSeed = 20141128;
	protected String genomeName = "testCase";
	protected boolean addUtrs = false;
	protected boolean onlyPlusStrand = true;
	protected int maxGeneLen = 1000;
	protected int maxTranscripts = 1;
	protected int maxExons = 5;
	protected int minExons = 1;

	protected Random rand;
	protected Config config;
	protected Genome genome;
	protected Chromosome chromosome;
	protected Gene gene;
	protected Transcript transcript;
	protected SnpEffectPredictor snpEffectPredictor;
	protected String chromoSequence = "";
	protected char chromoBases[];

	@Before
	public void before() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(randSeed);
	}

	/**
	 * Create a predictor
	 */
	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor
		if (config == null || config.getGenome() == null || config.getGenome().getGenomeName().equals(genomeName)) //
			config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);

		// Initialize factory
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(onlyPlusStrand); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
		sepf.setAddUtrs(addUtrs);
		sepf.setMinExons(minExons);

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

	protected void prependSequenceToFirstExon(String prepend) {
		Exon firstEx = transcript.sortedStrand().get(0);
		String seq = firstEx.getSequence();
		firstEx.setSequence(prepend + seq);
		transcript.resetCdsCache();
	}

}
