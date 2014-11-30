package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test random Interval Variants (e.g. when reading a BED file)
 *
 * @author pcingola
 */
public class TestCasesIntervalVariant {

	public static int N = 1000;

	boolean debug = false;
	boolean verbose = false;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesIntervalVariant() {
		super();
		init();
	}

	void init() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(20120426);
	}

	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		if (config == null) config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);

		// Create predictor
		snpEffectPredictor = sepf.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		// Data
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();
		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		if (verbose) System.out.println("INTERVAL (Variant) Test iteration:\n");
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();
			if (debug) System.out.println("INTERVAL (Variant) Test iteration: " + i + "\n" + transcript);
			else Gpr.showMark(i + 1, 1);

			// For each base in the transcript
			// For each base in this exon...
			for (int pos = 0; pos < chromosome.size(); pos++) {
				//---
				// Create seqChange
				//---
				// Interval length
				int intLen = rand.nextInt(100) + 1;
				int start = pos;
				int end = Math.min(pos + intLen, chromosome.getEnd());

				// Create a SeqChange
				Variant variant = new Variant(chromosome, start, end, "");

				// Sanity checks
				Assert.assertEquals(true, variant.isInterval()); // Is it an interval?

				//---
				// Expected Effect
				//---
				String expectedEffect = null;
				if (transcript.intersects(variant)) {
					// Does it intersect any exon?
					for (Exon ex : transcript)
						if (ex.intersects(variant)) expectedEffect = "EXON";

					for (Intron intron : transcript.introns())
						if (intron.intersects(variant)) expectedEffect = "INTRON";
				} else if (gene.intersects(variant)) {
					// Gene intersects but transcript doesn't?
					if (expectedEffect == null) expectedEffect = "INTRAGENIC";
				} else expectedEffect = "INTERGENIC";

				//---
				// Calculate effects
				//---
				// Copy only some effect (other effects are not tested)
				VariantEffects effectsAll = snpEffectPredictor.variantEffect(variant);
				VariantEffects effects = new VariantEffects(variant);
				for (VariantEffect eff : effectsAll) {
					boolean copy = true;

					if (eff.getEffectType() == EffectType.SPLICE_SITE_ACCEPTOR) copy = false;
					if (eff.getEffectType() == EffectType.SPLICE_SITE_DONOR) copy = false;

					if (copy) effects.addEffect(eff.getMarker(), eff.getEffectType(), "");
				}

				// There should be only one effect in most cases
				Assert.assertEquals(false, effects.isEmpty()); // There should be at least one effect
				if (debug && (effects.size() > 1)) {
					System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
					for (VariantEffect eff : effects)
						System.out.println("\t" + eff);
				}

				//---
				// Check effect
				//---
				boolean isExpectedOK = false;
				StringBuilder effSb = new StringBuilder();
				for (VariantEffect effect : effects) {
					String effstr = effect.effect(true, true, true, false);
					isExpectedOK |= effstr.equals(expectedEffect);
					effSb.append(effstr + " ");

				}

				if (debug || !isExpectedOK) //
					System.out.println("SeqChange       : " + variant //
							+ "\nExpected Effect :\t" + expectedEffect //
							+ "\nEffects         :\t" + effSb //
							+ "\n--------------------------------------------------------------\n" //
					);
				Assert.assertEquals(true, isExpectedOK);
			}
		}
	}
}
