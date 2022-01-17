package org.snpeff.snpEffect.testCases.integration;

import org.snpeff.SnpEff;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryRand;
import org.snpeff.util.GprSeq;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesHgvsBase {

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
    protected char[] chromoBases;

    public TestCasesHgvsBase() {
        super();
        init();
    }

    /**
     * Count how many bases are there until the exon
     */
    protected int exonBase(char[] bases, int pos, int direction) {
        int countAfter = 0, countBefore = 0;
        int posBefore, posAfter;
        for (posAfter = pos; (posAfter >= 0) && (posAfter < bases.length); countAfter++, posAfter += direction)
            if (bases[posAfter] != '-') break;

        for (posBefore = pos; (posBefore >= 0) && (posBefore < bases.length); countBefore++, posBefore -= direction)
            if (bases[posBefore] != '-') break;

        if (countBefore <= countAfter) return posBefore;
        return posAfter;
    }

    protected void init() {
        initRand();
        initSnpEffPredictor(false, true);
    }

    protected void initRand() {
        rand = new Random(20130708);
    }

    /**
     * Create a predictor
     */
    protected void initSnpEffPredictor(boolean addUtrs, boolean onlyPlusStrand) {
        // Create a config and force out snpPredictor for hg37 chromosome Y
        if (config == null) config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

        // Initialize factory
        int maxGeneLen = 1000;
        int maxTranscripts = 1;
        int maxExons = 5;
        SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
        sepf.setForcePositiveStrand(onlyPlusStrand); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
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

    /**
     * Intronic HGS notation
     */
    protected String intronHgsv(char[] bases, int j, int pos, String refStr, String altStr) {
        if (transcript.isStrandMinus()) {
            refStr = GprSeq.wc(refStr);
            altStr = GprSeq.wc(altStr);
        }

        // Closest exon base
        int exonBase = exonBase(bases, j, transcript.isStrandMinus() ? -1 : 1);
        int exonDist = (j - exonBase) * (transcript.isStrandMinus() ? -1 : 1);

        char type = bases[exonBase];
        String typeStr = "";
        int basesCount = 0;
        int step = transcript.isStrandPlus() ? 1 : -1;
        if (type == '5') {
            typeStr = "-";

            // Count UTR5 bases until TSS
            for (int i = exonBase; (i >= 0) && (i < bases.length); i += step) {
                if (bases[i] == type) basesCount++;
                else if (bases[i] != '-') break;
            }

        } else if (type == '3') {
            typeStr = "*";

            // Count UTR3 bases until end of coding
            for (int i = exonBase; (i >= 0) && (i < bases.length); i -= step) {
                if (bases[i] == type) basesCount++;
                else if (bases[i] != '-') break;
            }
        } else if ((type == '>') || (type == '<')) {
            // Count coding bases until TSS
            for (int i = exonBase; (i >= 0) && (i < bases.length); i -= step) {
                if (bases[i] == type) basesCount++;
                else if ((bases[i] != '-') && (bases[i] != '>') && (bases[i] != '<')) break;
            }
        } else throw new RuntimeException("Unexpected base type '" + bases[exonBase] + "'");

        return "c." //
                + typeStr //
                + basesCount //
                + (exonDist >= 0 ? "+" : "") + exonDist //
                + refStr + ">" + altStr;
    }

    /**
     * Run SnpEff on VCF file
     */
    public void snpEffect(String genomeVer, String vcfFile) {
        // Create command
        String[] args = {"-classic", "-hgvs", "-ud", "0", genomeVer, vcfFile};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
        cmdEff.setVerbose(verbose);
        cmdEff.setSupressOutput(!verbose);

        // Run command
        List<VcfEntry> list = cmdEff.run(true);
        assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

        // Find HGVS in any 'EFF' field
        int entryNum = 1;
        for (VcfEntry vcfEntry : list) {
            boolean found = false;

            // Load hgvs expexcted annotations into set
            String hgvsStr = vcfEntry.getInfo("HGVS");
            String trId = vcfEntry.getInfo("TR");
            HashSet<String> hgvsExpected = new HashSet<>();
            for (String h : hgvsStr.split(",")) {
                if (h.indexOf(':') > 0) h = h.substring(h.indexOf(':') + 1);
                hgvsExpected.add(h);
            }

            if (debug) System.err.println(entryNum + "\t" + vcfEntry);

            // Find if HGVS predicted by SnpEff matches expected annotations
            StringBuilder sb = new StringBuilder();
            for (VcfEffect eff : vcfEntry.getVcfEffects()) {
                if (trId != null && !trId.isEmpty() && trId.equals(eff.getTranscriptId())) {
                    String hgvsReal = eff.getAa();
                    String line = "\tHGVS: " + hgvsExpected.contains(hgvsReal) + "\tExpected: " + hgvsExpected + "\tSnpEFf: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff;
                    sb.append(line + "\n");

                    if (debug) System.err.println(line);
                    if (hgvsExpected.contains(hgvsReal)) found = true;
                }
            }

            // Not found? Error
            if (!found) {
                System.err.println("HGVS not found in variant\n" + vcfEntry + "\n" + sb);
                throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
            }
            entryNum++;
        }
    }

}
