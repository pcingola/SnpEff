package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
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
	protected int randSeed;
	protected String genomeName;
	protected boolean addUtrs;
	protected boolean onlyPlusStrand;
	protected boolean onlyMinusStrand;
	protected boolean shiftHgvs; // Do or do not shift variants according to HGVS notation (for test cases that were created before the feature was implemented)

	protected int maxGeneLen;
	protected int maxTranscripts;
	protected int maxExons;
	protected int minExons;

	protected Random rand;
	protected Config config;
	protected Genome genome;
	protected Chromosome chromosome;
	protected Gene gene;
	protected Transcript transcript;
	protected SnpEffectPredictor snpEffectPredictor;
	protected String chromoSequence = "";
	protected char chromoBases[];
	protected CodonTable codonTable;

	@After
	public void after() {
		config = null;
		codonTable = null;
		genome = null;
		chromosome = null;
		gene = null;
		transcript = null;
		snpEffectPredictor = null;
		chromoBases = null;
		chromoSequence = null;
	}

	@Before
	public void before() {
		init();
		initSnpEffPredictor();
	}

	protected void init() {
		randSeed = 20141128;
		genomeName = "testCase";
		addUtrs = false;
		onlyPlusStrand = true;
		onlyMinusStrand = false;
		maxGeneLen = 1000;
		maxTranscripts = 1;
		maxExons = 5;
		minExons = 1;
		shiftHgvs = false;

		initRand();
	}

	protected void initRand() {
		rand = new Random(randSeed);
	}

	/**
	 * Create a predictor
	 * For the default parameters the first predictor 
	 * created has only one transcript:
	 * 		1:880-1001, strand: +, id:transcript_0, Protein
	 * 		Exons:
	 * 			1:880-1001 'exon_0_0', rank: 1, frame: ., sequence: taaccccatatgattagtacggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgatgaccgatttatttgattccccacatttatttcatcggga
	 * 			CDS     :   taaccccatatgattagtacggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgatgaccgatttatttgattccccacatttatttcatcgggac
	 * 			Protein :   *PHMISTVEEKHLTPIEQDLFRNTLYR*PIYLIPHIYFIG?
	 */
	protected void initSnpEffPredictor() {
		// Create a config and force out snpPredictor
		if (config == null || config.getGenome() == null || !config.getGenome().getGenomeName().equals(genomeName)) {
			config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		}

		// Initialize factory
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositiveStrand(onlyPlusStrand);
		sepf.setForceNegativeStrand(onlyMinusStrand);
		sepf.setAddUtrs(addUtrs);
		sepf.setMinExons(minExons);

		// Create predictor
		snpEffectPredictor = sepf.create();

		// Update config
		config.setSnpEffectPredictor(snpEffectPredictor);
		config.setShiftHgvs(shiftHgvs);

		// Set predictor parameters
		snpEffectPredictor.setSpliceRegionExonSize(0);
		snpEffectPredictor.setSpliceRegionIntronMin(0);
		snpEffectPredictor.setSpliceRegionIntronMax(0);
		snpEffectPredictor.setUpDownStreamLength(0);

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// Build forest
		snpEffectPredictor.buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		codonTable = genome.codonTable();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();

		// Create genomic sequences
		genome.getGenomicSequences().addGeneSequences(chromosome.getId(), chromoSequence);
	}

	/**
	 * Prepend first's exons sequence with a given one
	 */
	protected void prependSequenceToFirstExon(String prepend) {
		Exon firstEx = transcript.sortedStrand().get(0);
		String seq = firstEx.getSequence();
		firstEx.setSequence(prepend + seq);
		transcript.resetCdsCache();
	}

}
