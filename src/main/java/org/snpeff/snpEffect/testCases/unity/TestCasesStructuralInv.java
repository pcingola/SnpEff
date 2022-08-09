package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for structural variants: Inversions
 * <p>
 * Gene models used in these test cases:
 * <p>
 * Gene: Gene_1:953-1216
 * 1:957-1157, strand: +, id:transcript_0, Protein
 * Exons:
 * 1:957-988 'exon_0_0', rank: 1, frame: ., sequence: gttgcttgaatactgtatagccttgccattgt
 * 1:1045-1057 'exon_0_1', rank: 2, frame: ., sequence: tgtgttgctaact
 * 1:1148-1157 'exon_0_2', rank: 3, frame: ., sequence: agacatggac
 * CDS     :	gttgcttgaatactgtatagccttgccattgttgtgttgctaactagacatggac
 * Protein :	VA*ILYSLAIVVLLTRHG?
 * <p>
 * 1
 * 0                                                                                                   1
 * 6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5
 * 789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567
 * gttgcttgaatactgtatagccttgccattgt........................................................tgtgttgctaact..........................................................................................agacatggac
 * V  A  *  I  L  Y  S  L  A  I                                                          V  V  L  L  T                                                                                            R  H  G
 * 01201201201201201201201201201201                                                        2012012012012                                                                                          0120120120
 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-------------------------------------------------------->>>>>>>>>>>>>------------------------------------------------------------------------------------------>>>>>>>>>>
 * |                              |                                                        |           |                                                                                          |        |
 * |                              |                                                        |           |                                                                                          |        ^1157
 * |                              |                                                        |           |                                                                                          ^1148
 * |                              |                                                        |           ^1057
 * |                              |                                                        ^1045
 * |                              ^988
 * ^957
 * <p>
 * <p>
 * Gene: Gene_1:2057-2157
 * 1:2066-2141, strand: +, id:transcript_1, Protein
 * Exons:
 * 1:2066-2069 'exon_1_0', rank: 1, frame: ., sequence: actt
 * 1:2084-2089 'exon_1_1', rank: 2, frame: ., sequence: cccttt
 * 1:2116-2126 'exon_1_2', rank: 3, frame: ., sequence: tacgcccacgt
 * 1:2133-2141 'exon_1_3', rank: 4, frame: ., sequence: ccgccgctg
 * CDS     :	acttcccttttacgcccacgtccgccgctg
 * Protein :	TSLLRPRPPL
 * <p>
 * 1
 * 7         8         9         0         1         2         3         4
 * 6789012345678901234567890123456789012345678901234567890123456789012345678901
 * actt..............cccttt..........................tacgcccacgt......ccgccgctg
 * T                S  L                            L  R  P  R        P  P  L
 * 0120              120120                          12012012012      012012012
 * >>>>-------------->>>>>>-------------------------->>>>>>>>>>>------>>>>>>>>>
 * |  |              |    |                          |         |      |       |
 * |  |              |    |                          |         |      |       ^2141
 * |  |              |    |                          |         |      ^2133
 * |  |              |    |                          |         ^2126
 * |  |              |    |                          ^2116
 * |  |              |    ^2089
 * |  |              ^2084
 * |  ^2069
 * ^2066
 */
public class TestCasesStructuralInv extends TestCasesBase {

    EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

    public TestCasesStructuralInv() {
        super();
    }

    Set<String> arrayToSet(String[] array) {
        Set<String> set = new HashSet<>();
        if (array != null) {
            for (String h : array)
                set.add(h);
        }
        return set;
    }

    protected void checkEffects(Variant variant, EffectType[] expEffs, String[] expHgvsc, EffectImpact expectedImpact, String[] expAnns) {
        checkEffects(variant, expEffs, expHgvsc, expectedImpact, expAnns, null);
    }

    protected void checkEffects(Variant variant, EffectType[] expEffs, String[] expHgvsc, EffectImpact expectedImpact, String[] expAnns, Gene[] genesToAdd) {
        // Convert to sets
        Set<EffectType> expectedEffs = new HashSet<>();
        if (expEffs != null) {
            for (EffectType et : expEffs)
                expectedEffs.add(et);
        }

        Set<String> expectedHgvsp = arrayToSet(null);
        Set<String> expectedHgvsc = arrayToSet(expHgvsc);
        Set<String> expectedAnns = arrayToSet(expAnns);

        // Initialize
        initSnpEffPredictor(genesToAdd);

        if (verbose) {
            Log.debug("Variant: " + variant);
            for (Gene g : genome.getGenes()) {
                Log.debug("\tGene: " + g.toStr());
                for (Transcript tr : g)
                    Log.debug(tr + "\n\n" + tr.toStringAsciiArt(true));
            }
        }

        // Calculate effects
        VariantEffects effects = snpEffectPredictor.variantEffect(variant);
        if (verbose) Log.debug("VariantEffects: " + effects);

        // Checknumber of results
        assertTrue(effects.size() >= 1);

        Set<EffectType> effs = new HashSet<>();
        Set<String> hgvscs = new HashSet<>();
        Set<String> hgvsps = new HashSet<>();
        Set<String> anns = new HashSet<>();
        boolean impactOk = false;
        for (VariantEffect varEff : effects) {
            effs.addAll(varEff.getEffectTypes());

            HgvsDna hgvsc = new HgvsDna(varEff);
            String hgvsDna = hgvsc.toString();
            hgvscs.add(hgvsDna);

            HgvsProtein hgvsp = new HgvsProtein(varEff);
            String hgvsProt = hgvsp.toString();
            hgvsps.add(hgvsProt);

            impactOk |= (varEff.getEffectImpact() == expectedImpact);

            // Create VcfEffect
            VcfEffect vcfEffect = new VcfEffect(varEff, formatVersion);
            String annField = vcfEffect.toString();
            anns.add(annField);

            if (verbose) Log.debug("Effect: " + varEff.toStr() //
                    + "\n\tHGVS.c: " + hgvsDna //
                    + "\n\tHGVS.p: " + hgvsProt //
                    + "\n\tANN   : " + annField //
            );
        }

        // Check effects
        assertTrue(effs.containsAll(expectedEffs), //
                "Effects do not match" //
                        + "\n\tExpected : " + expectedEffs //
                        + "\n\tFound    : " + effs//

        );

        // Check impact
        assertTrue(impactOk, "Effect impact '" + expectedImpact + "' not found");

        // Check HGVS.c
        assertTrue(hgvscs.containsAll(expectedHgvsc), //
                "HGVS.c do not match" //
                        + "\n\tExpected : " + expectedHgvsc //
                        + "\n\tFound    : " + hgvscs//
        );

        // Check HGVS.p
        assertTrue(hgvsps.containsAll(expectedHgvsp), //
                "HGVS.p do not match" //
                        + "\n\tExpected : " + expectedHgvsp //
                        + "\n\tFound    : " + hgvsps//
        );

        // Check ANN fields
        assertTrue(anns.containsAll(expectedAnns), //
                "ANN fields do not match" //
                        + "\n\tExpected : " + expectedAnns //
                        + "\n\tFound    : " + anns //
        );

    }

    @Override
    protected void init() {
        randSeed = 20151205;
        genomeName = "testCase";
        addUtrs = false;
        onlyPlusStrand = true;
        onlyMinusStrand = false;
        numGenes = 2;
        maxGeneLen = 1000;
        maxTranscripts = 1;
        maxExons = 5;
        minExons = 2;
        shiftHgvs = false;

        initRand();
    }

    /**
     * Inversion: Whole gene
     */
    @Test
    public void test01_invGene() {
        Log.debug("Test");

        // Create variant
        Variant variant = new Variant(chromosome, 950, 1250, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.GENE_INVERSION};
        EffectImpact expectedImpact = EffectImpact.MODERATE;

        checkEffects(variant, expEffs, null, expectedImpact, null);
    }

    /**
     * Inversion: whole transcript
     */
    @Test
    public void test01_invTr() {
        Log.debug("Test");

        // Create variant
        Variant variant = new Variant(chromosome, 950, 1200, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.TRANSCRIPT_INVERSION};
        String[] expHgvsc = {"c.-7_*43inv"};
        EffectImpact expectedImpact = EffectImpact.MODERATE;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: One coding exon
     */
    @Test
    public void test02() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1040, 1100, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.EXON_INVERSION};
        String[] expHgvsc = {"c.33-5_45+43inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: Two coding exons
     */
    @Test
    public void test03() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1040, 1160, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.EXON_INVERSION};
        String[] expHgvsc = {"c.33-5_*3inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: Part of one coding exon
     */
    @Test
    public void test04() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1040, 1050, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.EXON_INVERSION_PARTIAL};
        String[] expHgvsc = {"c.33-5_38inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: Part of two coding exon
     */
    @Test
    public void test05() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1050, 1150, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.EXON_INVERSION_PARTIAL};
        String[] expHgvsc = {"c.38_48inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: Two genes
     */
    @Test
    public void test06() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1050, 2160, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.GENE_INVERSION};
        String[] expHgvsc = {"n.1051_2161inv"};
        EffectImpact expectedImpact = EffectImpact.MODERATE;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion: Part of two genes (fusions) cutting on introns
     */
    @Test
    public void test07() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1100, 2075, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"n.1101_2076inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;
        String[] expAnns = {"|bidirectional_gene_fusion|HIGH|geneName1&geneName2|geneId1&geneId2|gene_variant|geneId1|||n.1101_2076inv||||||"};

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, expAnns);
    }

    /**
     * Inversion: Part of two genes (fusions) cutting exons
     */
    @Test
    public void test08() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1050, 2120, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"n.1051_2121inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;
        String[] expAnns = {"|bidirectional_gene_fusion|HIGH|geneName1&geneName2|geneId1&geneId2|gene_variant|geneId1|||n.1051_2121inv||||||"};

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, expAnns);
    }

    /**
     * Inversion: Intron
     */
    @Test
    public void test09() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 991, 1020, "");
        variant.setVariantType(VariantType.INV);

        EffectType[] expEffs = {EffectType.INTRON};
        String[] expHgvsc = {"c.32+3_33-25inv"};
        EffectImpact expectedImpact = EffectImpact.MODIFIER;

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, null);
    }

    /**
     * Inversion creating a fusion between two pairs (i.e. four genes)
     */
    @Test
    public void test10() {
        Log.debug("Test");

        Variant variant = new Variant(chromosome, 1050, 2120, "");
        variant.setVariantType(VariantType.INV);

        // Add another gene
        boolean strandMinus = false;
        Gene gene = new Gene(chromosome, 2000, 2200, strandMinus, "geneId3", "geneName3", BioType.protein_coding);
        Transcript tr = new Transcript(gene, 2000, 2200, strandMinus, "tr3");
        Exon ex = new Exon(tr, 2000, 2200, strandMinus, "ex1_tr3", 1);
        String seq = chromoSequence.substring(ex.getStart(), ex.getEndClosed() - 1);
        ex.setSequence(strandMinus ? GprSeq.reverseWc(seq) : seq);
        tr.add(ex);
        gene.add(tr);
        Gene[] genesToAdd = {gene};

        // Expected resutls
        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"n.1051_2121inv"};
        EffectImpact expectedImpact = EffectImpact.HIGH;
        String[] expAnns = {"|bidirectional_gene_fusion|HIGH|geneName1&geneName2|geneId1&geneId2|gene_variant|geneId1|||n.1051_2121inv||||||" //
                , "|bidirectional_gene_fusion|HIGH|geneName1&geneName3|geneId1&geneId3|gene_variant|geneId1|||n.1051_2121inv||||||" //
        };

        checkEffects(variant, expEffs, expHgvsc, expectedImpact, expAnns, genesToAdd);
    }
}
