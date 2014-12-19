package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.Hgvs;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Base class for some test cases
 *
 * @author pcingola
 */
public class TestCasesBase {

	protected boolean debug = false;
	protected boolean verbose = false || debug;
	protected boolean ignoreErrors = false;

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
	protected int spliceRegionExonSize = 0;
	protected int spliceRegionIntronMin = 0;
	protected int spliceRegionIntronMax = 0;

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

	protected void checkEffect(Variant variant, EffectType effectExpected) {
		checkEffect(variant, effectExpected, null, null);
	}

	protected void checkEffect(Variant variant, EffectType effectExpected, EffectType effectNotExpected, EffectImpact impact) {
		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

		boolean found = false;
		for (VariantEffect effect : effects) {
			String effStr = effect.getEffectTypeString(false);

			// Check effect
			if (verbose) System.out.println(effect.toStringSimple(true) + "\n\tEffect type: '" + effStr + "'\tExpected: '" + effectExpected + "'");
			found |= effect.hasEffectType(effectExpected);

			// Check that 'effectNotExpected' is not present
			if (effectNotExpected != null && effect.hasEffectType(effectNotExpected)) throw new RuntimeException("Effect '" + effectNotExpected + "' should not be here");

			// Check impact
			if (impact != null && effect.getEffectImpact() != impact) throw new RuntimeException("Effect '" + effectExpected + "' should have impact '" + impact + "', but impct was '" + effect.getEffectImpact() + "'.");
		}

		Assert.assertTrue("Effect not found: '" + effectExpected + "' in variant " + variant, found);
	}

	public void compareHgvs(String genome, String vcfFileName) {
		compareHgvs(genome, vcfFileName, true);
	}

	public void compareHgvs(String genome, String vcfFileName, boolean compareProt) {
		// Create SnpEff
		String args[] = { genome, vcfFileName };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setUpDownStreamLength(0);
		snpeff.setShiftHgvs(shiftHgvs);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		Set<String> trNotFoundSet = new HashSet<String>();

		// Make sure entries are annotated as expected
		int countOkC = 0, countErrC = 0, countOkP = 0, countErrP = 0, countTrFound = 0;
		for (VcfEntry ve : results) {
			// Extract expected HGVS values
			String hgvsCexp = ve.getInfo("HGVS_C") != null ? ve.getInfo("HGVS_C") : "";
			String trIdC = Hgvs.parseTranscript(hgvsCexp);
			hgvsCexp = Hgvs.removeTranscript(hgvsCexp);

			String hgvsPexp = "";
			String trIdP = "";
			if (compareProt) {
				hgvsPexp = ve.getInfo("HGVS_P") != null ? ve.getInfo("HGVS_P") : "";
				trIdP = Hgvs.parseTranscript(hgvsPexp);
				hgvsPexp = Hgvs.removeTranscript(hgvsPexp);
			}

			if (verbose) {
				System.out.println(ve);
				if (trIdC != null) System.out.println("\tExpected HGVS_C: " + trIdC + ":" + hgvsCexp);
				if (trIdP != null) System.out.println("\tExpected HGVS_P: " + trIdP + ":" + hgvsPexp + "\n");
			}

			// Check all effects
			boolean okC = false, okP = false, trFound = false;
			for (VcfEffect veff : ve.parseEffects()) {
				// Parse calculated HGVS values
				String trId = veff.getTranscriptId();
				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";

				// Compare results for HGVS_DNA
				boolean foundC = false, foundP = false;
				if (trId != null && trId.equals(trIdC)) {
					trFound = true;
					if (!hgvsCexp.equals(hgvsCactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsCexp, hgvsCactual);
						countErrC++;
					} else {
						okC = foundC = true;
						countOkC++;
					}
				}

				// Compare results for HGVS_PROT
				if (compareProt && trId != null && trId.equals(trIdP)) {
					if (!hgvsPexp.equals(hgvsPactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsPexp, hgvsPactual);
						countErrP++;
					} else {
						okP = foundP = true;
						countOkP++;
					}
				}

				if (verbose) {
					System.out.println("\t" + veff //
							+ "\n\t\tEFF    : " + veff.getEffectsStr() //
							+ "\n\t\tHGVS_C : " + trId + ":" + hgvsCactual + "\t\tExpected: " + trIdC + ":" + hgvsCexp + "\t" + (foundC ? "OK" : "NO") //
							+ (compareProt ? "\n\t\tHGVS_P : " + trId + ":" + hgvsPactual + "\t\tExpected: " + trIdP + ":" + hgvsPexp + "\t" + (foundP ? "OK" : "NO") : "") //
							+ "\n");
				}

			}

			if (!trFound) {
				System.out.println("Transcript '" + trIdC + "' not found.");
				countTrFound++;
				trNotFoundSet.add(trIdC);
			}

			if (!ignoreErrors) {
				Assert.assertTrue("HGVS (DNA) not found: '" + hgvsCexp + "'", okC);
				if (!hgvsPexp.isEmpty()) Assert.assertTrue("HGVS (Protein) not found: '" + hgvsPexp + "'", okP);
			} else {
				// Show errors
				if (!okC) System.err.println("HGVS (DNA) not found : '" + hgvsCexp + "', vcf entry:\t" + ve);
				if (compareProt && !okP) System.err.println("HGVS (Prot) not found: '" + hgvsPexp + "', vcf entry:\t" + ve);
			}
		}

		if (verbose || ignoreErrors) {
			System.out.println("Count OKs   :\tHGVS (DNA): " + countOkC + "\tHGVS (Protein): " + countOkP);
			System.out.println("Count Errors:\tHGVS (DNA): " + countErrC + "\tHGVS (Protein): " + countErrP);
			System.out.println("Transcripts not found:\t" + countTrFound + ", unique: " + trNotFoundSet.size() + "\n" + trNotFoundSet);
		}
	}

	/**
	 * Is effectExpected included in effStr (many effects delimited by '&'
	 */
	protected boolean hasEffect(String effectExpected, String effStr) {
		for (String eff : effStr.split(VcfEffect.EFFECT_TYPE_SEPARATOR))
			if (eff.equals(effectExpected)) return true;

		return false;
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
		snpEffectPredictor.setSpliceRegionExonSize(spliceRegionExonSize);
		snpEffectPredictor.setSpliceRegionIntronMin(spliceRegionIntronMin);
		snpEffectPredictor.setSpliceRegionIntronMax(spliceRegionIntronMax);
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
		// Change coding sequence
		Exon ex = transcript.sortedStrand().get(0);
		String seq = ex.getSequence();
		ex.setSequence(prepend + seq);

		// Reset transcript
		transcript.resetCdsCache();

		// Change chromosome sequence
		chromoSequence = chromoSequence.substring(0, ex.getStart()) + ex.getSequence() + chromoSequence.substring(ex.getEnd() + 1);

		// Rebuild genomicSequences
		GenomicSequences gs = genome.getGenomicSequences();
		gs.reset();
		gs.addGeneSequences(chromosome.getId(), chromoSequence);
		gs.build();
	}

}
