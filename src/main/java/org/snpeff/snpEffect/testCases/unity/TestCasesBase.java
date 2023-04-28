package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.snpeff.align.StringDiff;
import org.snpeff.binseq.GenomicSequences;
import org.snpeff.codons.CodonTable;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.*;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryRand;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for some test cases
 *
 * @author pcingola
 */
public class TestCasesBase {

    public static final String BASE_DIR = "tests";

    protected boolean debug = false;
    protected boolean verbose = false || debug;

    // Parameters for creating fake genome
    protected int randSeed;
    protected String genomeName;
    protected boolean addUtrs;
    protected boolean onlyPlusStrand;
    protected boolean onlyMinusStrand;
    protected boolean shiftHgvs; // Do or do not shift variants according to HGVS notation (for test cases that
    // were created before the feature was implemented)

    protected int numGenes = 1;
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

    protected String testType;
    protected List<String> prefixes;

    public TestCasesBase() {
        testType = "unity";

        prefixes = new LinkedList<>();
        prefixes.add("TestCases");
        prefixes.add("TestsCase");
        prefixes.add("Unit");
        prefixes.add("Integration");

        Log.setFatalErrorBehabiour(Log.FatalErrorBehabiour.EXCEPTION);
    }

    @AfterEach
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

    @BeforeEach
    public void before() {
        init();
        initSnpEffPredictor();
    }

    /**
     * Apply a variant to a transcript and check resulting CDS sequence, protein
     * sequence and exon coordinates
     */
    public void checkApply(Variant variant, VariantType varType, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        Transcript newTr = transcript.apply(variant);

        if (verbose || debug) {
            Log.debug("Apply:\nVariant " + variant.getVariantType() //
                    + " [ " + variant.getStart() + " , " + variant.getEndClosed() + "]" //
                    + ", REF len: " + variant.getReference().length() //
                    + ", ALT len: " + variant.getAlt().length() + ":" //
                    + variant //
                    + "\nBefore:\n" + Gpr.prependEachLine("\t", transcript) //
                    + "\nAfter:\n" + Gpr.prependEachLine("\t", newTr) //
            );

            if (debug) {
                Log.debug("\nTranscript:\n" + transcript.toStringAsciiArt(true) + "\n\nNew transcript:\n" + newTr.toStringAsciiArt(true));
            }
        }

        // Check that reference sequence matches chromosome
        if (!variant.getReference().isEmpty()) {
            String chrSeq = chromoSequence.substring(variant.getStart(), variant.getEndClosed() + 1);
            assertEquals(chrSeq, variant.getReference(), "Reference sequence does not match: " + chrSeq + " vs " + variant.getReference());
        }

        // Check variant type
        assertEquals(varType, variant.getVariantType(), "Variant type does not match: " + varType + " vs " + variant.getVariantType());

        // Check sequences
        if (!expectedCds.equals(newTr.cds())) {
            StringDiff diff = new StringDiff(expectedCds, "Expected CDS", newTr.cds(), "New CDS");
            assertEquals(expectedCds, newTr.cds(), "CDS sequence should not change:\n" + diff);
        }
        if (expectedProtein != null && !expectedProtein.equals(newTr.protein())) {
            StringDiff diff = new StringDiff(expectedCds, "Expected CDS", newTr.cds(), "New CDS");
            assertEquals(expectedProtein, newTr.protein(), "Protein sequence should not change:\n" + diff);
        }

        // Check exon coordinates
        Exon newEx1 = newTr.sorted().get(1);
        assertEquals(expectedExon1Start, newEx1.getStart(), "Exon start coordinate");
        assertEquals(expectedExon1End, newEx1.getEndClosed(), "Exon end coordinate");
    }

    public void checkApplyDel(Variant variant, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        checkApply(variant, VariantType.DEL, expectedCds, expectedProtein, exonRank, expectedExon1Start, expectedExon1End);
    }

    public void checkApplyIns(Variant variant, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        checkApply(variant, VariantType.INS, expectedCds, expectedProtein, exonRank, expectedExon1Start, expectedExon1End);
    }

    public void checkApplyMixed(Variant variant, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        checkApply(variant, VariantType.MIXED, expectedCds, expectedProtein, exonRank, expectedExon1Start, expectedExon1End);
    }

    public void checkApplyMnp(Variant variant, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        checkApply(variant, VariantType.MNP, expectedCds, expectedProtein, exonRank, expectedExon1Start, expectedExon1End);
    }

    public void checkApplySnp(Variant variant, String expectedCds, String expectedProtein, int exonRank, int expectedExon1Start, int expectedExon1End) {
        checkApply(variant, VariantType.SNP, expectedCds, expectedProtein, exonRank, expectedExon1Start, expectedExon1End);
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
            if (verbose)
                Log.info(effect.toStringSimple(true) + "\n\tEffect type: '" + effStr + "'\tExpected: '" + effectExpected + "'");
            found |= effect.hasEffectType(effectExpected);

            // Check that 'effectNotExpected' is not present
            if (effectNotExpected != null && effect.hasEffectType(effectNotExpected))
                throw new RuntimeException("Effect '" + effectNotExpected + "' should not be here");

            // Check impact
            if (impact != null && effect.getEffectImpact() != impact)
                throw new RuntimeException("Effect '" + effectExpected + "' should have impact '" + impact + "', but impct was '" + effect.getEffectImpact() + "'.");
        }

        assertTrue(found, "Effect not found: '" + effectExpected + "' in variant " + variant);
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
        numGenes = 1;
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

    protected void initSnpEffPredictor() {
        initSnpEffPredictor(null);
    }

    /**
     * Create a predictor For the default parameters the first predictor created has
     * only one transcript: 1:880-1001, strand: +, id:transcript_0, Protein Exons:
     * 1:880-1001 'exon_0_0', rank: 1, frame: ., sequence:
     * taaccccatatgattagtacggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgatgaccgatttatttgattccccacatttatttcatcggga
     * CDS :
     * taaccccatatgattagtacggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgatgaccgatttatttgattccccacatttatttcatcgggac
     * Protein : *PHMISTVEEKHLTPIEQDLFRNTLYR*PIYLIPHIYFIG?
     */
    protected void initSnpEffPredictor(Gene[] genesToAdd) {
        // Create a config and force out snpPredictor
        if (config == null || config.getGenome() == null || !config.getGenome().getGenomeName().equals(genomeName)) {
            config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
        }

        // Initialize factory
        SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
        sepf.setNumGenes(numGenes);
        sepf.setForcePositiveStrand(onlyPlusStrand);
        sepf.setForceNegativeStrand(onlyMinusStrand);
        sepf.setAddUtrs(addUtrs);
        sepf.setMinExons(minExons);

        // Add some genes to predictor?
        if (genesToAdd != null) {
            for (Gene g : genesToAdd)
                snpEffectPredictor.add(g);
        }

        // Create predictor
        snpEffectPredictor = sepf.create();

        // Update config
        config.setSnpEffectPredictor(snpEffectPredictor);
        config.setHgvsShift(shiftHgvs);

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
        genome.getGenomicSequences().clear();
        genome.getGenomicSequences().addGeneSequences(chromosome.getId(), chromoSequence);
    }

    public String path(String fileName) {
        return BASE_DIR + "/" + testType + "/" + pathClassName() + "/" + fileName;
    }

    protected String pathClassName() {
        String sname = this.getClass().getSimpleName();
        for (String prefix : prefixes)
            if (sname.startsWith(prefix)) sname = sname.substring(prefix.length());
        return sname.substring(0, 1).toLowerCase() + sname.substring(1);
    }

//	public String pathMigrate(String fileName) {
//		String dir = BASE_DIR + "/" + testType + "/" + pathClassName();
//		String path = dir + "/" + fileName;
//		String oldPath = BASE_DIR + "/old/" + fileName;
//
//		if (!Gpr.exists(dir)) {
//			Log.debug("File migration: Creating dir:" + dir);
//			File d = new File(dir);
//			d.mkdir();
//		}
//
//		if (!Gpr.exists(oldPath)) {
//			Log.debug("File migration: Cannot find original file:" + oldPath);
//		} else if (!Gpr.exists(path)) {
//			Log.debug("File migration: Moving file:" + path);
//			try {
//				FileUtils.moveFile(new File(oldPath), new File(path));
//			} catch (IOException e) {
//				throw new RuntimeException("Cannot copy files:\n\tsrc: " + oldPath + "\n\tdst: " + path, e);
//
//			}
//		}
//		return path;
//	}

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
        chromoSequence = chromoSequence.substring(0, ex.getStart()) + ex.getSequence() + chromoSequence.substring(ex.getEndClosed() + 1);

        // Rebuild genomicSequences
        GenomicSequences gs = genome.getGenomicSequences();
        gs.reset();
        gs.addGeneSequences(chromosome.getId(), chromoSequence);
        gs.build();
    }

//	/**
//	 * Show a genome in a 'standard' way
//	 */
//	public String showTranscripts(Genome genome) {
//		StringBuilder sb = new StringBuilder();
//
//		// Genome
//		sb.append(genome.getVersion() + "\n");
//
//		// Chromosomes
//		for (Chromosome chr : genome)
//			sb.append(chr + "\n");
//
//		// Show all genes
//		for (Gene gene : genome.getGenes().sorted())
//			sb.append(gene);
//
//		return sb.toString();
//	}

}
