package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;

import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

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

	public void checkApply(Variant variant, String expectedCds, String expectedProtein, int expectedExon1Start, int expectedExon1End) {
		Transcript newTr = transcript.apply(variant);

		if (debug) Gpr.debug("Variant [ " + variant.getStart() + " , " + variant.getEnd() + ", ALT.len: " + variant.getAlt().length() + " ]:" + variant + "\nBefore:\n" + transcript.toStringAsciiArt(true) + "\nAfter:\n" + newTr.toStringAsciiArt(true));
		else if (verbose) Gpr.debug("Variant [ " + variant.getStart() + " , " + variant.getEnd() + ", ALT.len: " + variant.getAlt().length() + " ]:" + variant + "\nBefore:\n" + Gpr.prependEachLine("\t", transcript) + "\nAfter:\n" + Gpr.prependEachLine("\t", newTr));

		// Check sequences
		Assert.assertEquals("CDS sequence should not change", expectedCds, newTr.cds());
		if (expectedProtein != null) Assert.assertEquals("Protein sequence should not change", expectedProtein, newTr.protein());

		// Check exon coordinates
		Exon newExons[] = newTr.subintervals().toArray(new Exon[0]);
		Exon newEx1 = newExons[1];
		Assert.assertEquals("Exon start coordinate", expectedExon1Start, newEx1.getStart());
		Assert.assertEquals("Exon end coordinate", expectedExon1End, newEx1.getEnd());
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

	/**
	 * Get file's format version
	 */
	protected EffFormatVersion formatVersion(String vcfFileName) {
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		VcfEntry ve = vcf.next();

		List<VcfEffect> effs = ve.getVcfEffects();
		if (effs.isEmpty()) throw new RuntimeException("Empty list of effects. Tis should never happen!");

		VcfEffect eff = effs.get(0);
		return eff.formatVersion();
	}

	/**
	 * Is effectExpected included in effStr (many effects delimited by '&'
	 */
	protected boolean hasEffect(String effectExpected, String effStr) {
		for (String eff : effStr.split(EffFormatVersion.EFFECT_TYPE_SEPARATOR))
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
		transcript.resetCache();

		// Change chromosome sequence
		chromoSequence = chromoSequence.substring(0, ex.getStart()) + ex.getSequence() + chromoSequence.substring(ex.getEnd() + 1);

		// Rebuild genomicSequences
		GenomicSequences gs = genome.getGenomicSequences();
		gs.reset();
		gs.addGeneSequences(chromosome.getId(), chromoSequence);
		gs.build();
	}

	/**
	 * Show a genome in a 'standard' way
	 */
	public String showTranscripts(Genome genome) {
		StringBuilder sb = new StringBuilder();

		// Genome
		sb.append(genome.getVersion() + "\n");

		// Chromosomes
		for (Chromosome chr : genome)
			sb.append(chr + "\n");

		// Show all genes
		for (Gene gene : genome.getGenes().sorted())
			sb.append(gene);

		return sb.toString();
	}

}
