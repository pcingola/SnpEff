package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test case for structural variants: Translocation (fusions)
 * <p>
 * We create two genes (one transcript each). Each gene is in one different chromosome
 * <p>
 * Transcripts:
 * 1:10-90, strand: +, id:tr1, Protein
 * Exons:
 * 1:10-30 'exon1', rank: 1, frame: ., sequence: tatttgtatgaggatttgagt
 * 1:40-90 'exon2', rank: 2, frame: ., sequence: tactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 * CDS     :   tatttgtatgaggatttgagttactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 * Protein :   YLYEDLSYSVLGNPLAVAPLTLLF
 * <p>
 * 2:110-190, strand: +, id:tr2, Protein
 * Exons:
 * 2:110-125 'exon3', rank: 1, frame: ., sequence: gttaatgggatttcac
 * 2:150-190 'exon4', rank: 2, frame: ., sequence: atgggaacggagtgtcgacagcaccttatggggagctatat
 * CDS     :   gttaatgggatttcacatgggaacggagtgtcgacagcaccttatggggagctatat
 * Protein :   VNGISHGNGVSTAPYGELY
 * <p>
 * <p>
 * Genes diagram:
 * <p>
 * [ Chr1: Gene1                                                                   ]
 * >>>>>>>>>>>>>>>>>>>>>--------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 * |                   |         |                                                 |
 * ^10                 ^30       ^40                                               ^90
 * <p>
 * <p>
 * [ Chr2: Gene2                                                                   ]
 * >>>>>>>>>>>>>>>>------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 * |              |                        |                                       |
 * ^110           ^125                     ^150                                    ^190
 */
public class TestCasesStructuralTranslocations {

    EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

    boolean debug = false;
    boolean verbose = false || debug;

    Random rand = new Random(20160229);
    Config config;
    String chr1Seq;
    String chr2Seq;
    Genome genome;
    Chromosome chr1, chr2;
    Gene gene1, gene2;
    Transcript tr1, tr2;
    SnpEffectPredictor snpEffectPredictor;

    public TestCasesStructuralTranslocations() {
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

    protected void checkEffects(Variant variant, EffectType[] expEffs, EffectType[] notExpEffs, String[] expHgvsp, String[] expHgvsc, EffectImpact expectedImpact) {
        // Convert to sets
        Set<EffectType> expectedEffs = new HashSet<>();
        if (expEffs != null) {
            for (EffectType et : expEffs)
                expectedEffs.add(et);
        }

        Set<EffectType> notExpectedEffs = new HashSet<>();
        if (notExpEffs != null) {
            for (EffectType et : notExpEffs)
                notExpectedEffs.add(et);
        }

        Set<String> expectedHgvsp = arrayToSet(expHgvsp);
        Set<String> expectedHgvsc = arrayToSet(expHgvsc);
        Set<String> expectedAnns = arrayToSet(null);

        if (verbose) {
            Log.debug("Variant: " + variant);
            for (Gene g : genome.getGenes()) {
                Log.debug("\tGene: " + g.getId() + "\t" + gene1.getStart() + " - " + gene1.getEndClosed());
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

        // Check effects that should NOT be present
        assertFalse(effs.removeAll(notExpectedEffs), // Returns true if the set has changed (i.e. there was an element removed)
                "Effects should NOT be present: " + notExpectedEffs //
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

    public void init(boolean gene1NegativeStrand, boolean gene2NegativeStrand) {
        config = new Config("test");

        chr1Seq = "TGCTTGTCGATATTTGTATGAGGATTTGAGTACTACGCACTACTCAGTGCTGGGCAATCCCTTAGCTGTCGCGCCGCTTACCCTACTATTCAGGAGTAGGCCCTATCTCCACAGTGACTGTAGTACCAGCCATCTCTCTCGTTGCCGTCTGCGGTGCCGTCACACACGCTCCAGTCCCAGCTACGTTTCGCCAGGCTCAG";
        chr2Seq = "GCGATTGGTTGAATAAGCATAAGGTAGTTATCCGCCTGCACCTTGTTGAAAGATTGGACTTAATCCACCCCGTTAACAAAGGAATCGATCATGTTGCGCATATCGTCTAGGTTAATGGGATTTCACCGCTTACCCACTTAGCGGGCTGGAATGGGAACGGAGTGTCGACAGCACCTTATGGGGAGCTATATTCCCCCTAT";
        genome = new Genome("test");

        chr1 = new Chromosome(genome, 0, chr1Seq.length() - 1, "1");
        chr2 = new Chromosome(genome, 0, chr2Seq.length() - 1, "2");
        chr1.setSequence(chr1Seq);
        chr2.setSequence(chr2Seq);
        genome.add(chr1);
        genome.add(chr2);

        gene1 = new Gene(chr1, 10, 90, gene1NegativeStrand, "gene1", "gene1", BioType.protein_coding);
        gene2 = new Gene(chr2, 110, 190, gene2NegativeStrand, "gene2", "gene2", BioType.protein_coding);

        tr1 = new Transcript(gene1, gene1.getStart(), gene1.getEndClosed(), gene1.isStrandMinus(), "tr1");
        tr2 = new Transcript(gene2, gene2.getStart(), gene2.getEndClosed(), gene2.isStrandMinus(), "tr2");
        gene1.add(tr1);
        gene2.add(tr2);
        tr1.setProteinCoding(true);
        tr2.setProteinCoding(true);

        Exon e11 = new Exon(tr1, 10, 30, tr1.isStrandMinus(), "exon1", 0);
        Exon e12 = new Exon(tr1, 40, 90, tr1.isStrandMinus(), "exon2", 0);
        Exon e21 = new Exon(tr2, 110, 125, tr2.isStrandMinus(), "exon3", 0);
        Exon e22 = new Exon(tr2, 150, 190, tr2.isStrandMinus(), "exon4", 0);
        Exon[] exons = {e11, e12, e21, e22};

        for (Exon e : exons) {
            String seq = e.getChromosome().getSequence().substring(e.getStart(), e.getEndClosed() + 1);
            if (e.isStrandMinus()) seq = GprSeq.reverseWc(seq);
            e.setSequence(seq);

            Transcript tr = (Transcript) e.getParent();
            tr.add(e);

            Cds cds = new Cds(tr, e.getStart(), e.getEndClosed(), e.isStrandMinus(), "");
            tr.add(cds);
        }
        tr1.rankExons();
        tr2.rankExons();

        if (verbose) Log.info("Transcripts:\n" + tr1 + "\n" + tr2);

        snpEffectPredictor = new SnpEffectPredictor(genome);
        snpEffectPredictor.setUpDownStreamLength(0);
        snpEffectPredictor.add(gene1);
        snpEffectPredictor.add(gene2);
        snpEffectPredictor.buildForest();

        // Create fake cytobands
        CytoBands cytoBands = genome.getCytoBands();
        cytoBands.add(new Marker(chr1, chr1.getStart(), 99, false, "p1"));
        cytoBands.add(new Marker(chr1, 100, chr1.getEndClosed(), false, "q1"));
        cytoBands.add(new Marker(chr2, chr2.getStart(), 99, false, "q2"));
        cytoBands.add(new Marker(chr2, 100, chr2.getEndClosed(), false, "p2"));
        cytoBands.build();
    }

    /**
     * Translocation in the same direction (both genes in positive strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2                   ---->>>>>>>>>
     */
    @Test
    public void test01_0() {
        Log.debug("Test");
        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:His6_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction (both genes in positive strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2              ---->>>>>>>>>>>>---
     */
    @Test
    public void test01_0_nonFs() {
        Log.debug("Test");

        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 152, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:Gly7_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2                   ----<<<<<<<<<<<<---
     */
    @Test
    public void test01_1() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:Ile1_Met14)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   <<<<<<<<<<<<----
     * |
     * gene2                   ---->>>>>>>>>----
     */
    @Test
    public void test01_2() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:His6_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction (both genes in negative strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   <<<<<<<<<<<<----
     * |
     * gene2                   ----<<<<<<<<<<<<---
     */
    @Test
    public void test01_3() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:Ile1_Met14)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction (both genes in negative strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N[chr2:140[
     * <p>
     * gene1:   <<<<<<<<<<<<----
     * |
     * gene2                --<<<<<<<<<<<<---
     */
    @Test
    public void test01_3_noFs() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 152, false, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:Ile1_Pro13)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions (both genes in positive strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2    >>>>>>>>>>>----
     */
    @Test
    public void test02_0() {
        Log.debug("Test");

        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:Val1_His6)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2    <<<<<<<<<<<----
     */
    @Test
    public void test02_1() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:Met14_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   >>>>>>>>>>>----
     * |
     * gene2    -----<<<<<<<<<<<----
     */
    @Test
    public void test02_1_nonFs() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 124, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr1_Ser7;tr2:Ter15_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   <<<<<<<<<<<----
     * |
     * gene2    >>>>>>>>>>>----
     */
    @Test
    public void test02_2() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:Val1_His6)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   <<<<<<<<<<<----
     * |
     * gene2    ----->>>>>>>>>>>----
     */
    @Test
    public void test02_2_nonFs() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 124, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:Val1_Ser5)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      N]chr2:140]
     * <p>
     * gene1:   <<<<<<<<<<<----
     * |
     * gene2    <<<<<<<<<<<----
     */
    @Test
    public void test02_3() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, false);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Thr18_Ile24;tr2:Met14_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:                  --->>>>>>>>>>>----
     * |
     * gene2                   --->>>>>>>>>>>----
     */
    @Test
    public void test03_0() {
        Log.debug("Test");

        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:His6_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:                  --->>>>>>>>>>>----
     * |
     * gene2                   ---<<<<<<<<<<----
     */
    @Test
    public void test03_1() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:Ile1_Met14)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:               --->>>>>>>>>>>----
     * |
     * gene2                   ---<<<<<<<<<<----
     */
    @Test
    public void test03_1_nonFs() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 152, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:Ile1_Pro13)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:                  ---<<<<<<<<<<<----
     * |
     * gene2                   --->>>>>>>>>>>----
     */
    @Test
    public void test03_2() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:His6_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:                  ---<<<<<<<<<<<----
     * |
     * gene2                   >>>>>>>>>>>----
     */
    @Test
    public void test03_2_nonFs() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 152, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:Gly7_Tyr19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      [chr2:140[N
     * <p>
     * gene1:                  ---<<<<<<<<<<<----
     * |
     * gene2                   ---<<<<<<<<<<<----
     */
    @Test
    public void test03_3() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, false, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:Ile1_Met14)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction (both genes in positive strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  --->>>>>>>>>>>----
     * |
     * gene2  --->>>>>>>>>>>----
     */
    @Test
    public void test04_0() {
        Log.debug("Test");

        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:Val1_His6)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction (both genes in positive strand)
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  --->>>>>>>>>>>----
     * |
     * gene2      --->>>>>>>>>>>
     */
    @Test
    public void test04_0_nonFs() {
        Log.debug("Test");

        init(false, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 124, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:Val1_Ser5)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  --->>>>>>>>>>>----
     * |
     * gene2  ---<<<<<<<<<<<----
     */
    @Test
    public void test04_1() {
        Log.debug("Test");

        init(false, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.21+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Tyr8_Phe24;tr2:Met14_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in opposite directions
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  ---<<<<<<<<<<<<<<----
     * |
     * gene2  --->>>>>>>>>>>----
     */
    @Test
    public void test04_2() {
        Log.debug("Test");

        init(true, false);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_REVERESE};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:Val1_His6)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  ---<<<<<<<<<<<<----
     * |
     * gene2  ---<<<<<<<<<<<----
     */
    @Test
    public void test04_3() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 140, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION, EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:Met14_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, null, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation in the same direction
     * <p>
     * #CHROM   POS    ID    REF    ALT
     * chr1     35     .     N      ]chr2:140]N
     * <p>
     * gene1:                  ---<<<<<<<<<<<<----
     * |
     * gene2      ---<<<<<<<<<<<----
     */
    @Test
    public void test04_3_nonFs() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 124, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION};
        EffectType[] notExpEffs = {EffectType.FRAME_SHIFT};
        String[] expHgvsc = {"t(1;2)(p1;p2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;tr2:Ter15_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation affecting a gene and an intergenic region
     * <p>
     * [ Chr1: Gene1                                                                   ]
     * ......>>>>>>>>>>>>>>>>>>>>>--------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..............................................................................................................
     * ^10                 ^30  |    ^40                                               ^90                                          |
     * |>
     * ------------------
     * <|
     * |                                                                        |
     * ..........................................................................................................>>>>>>>>>>>>>>>>------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..........
     * ^110           ^125                     ^150                                    ^190
     * [ Chr2: Gene2                                                                   ]
     */
    @Test
    public void test05_1_one_gene() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 35, "N", "N", chr2, 50, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_HALF};
        EffectType[] notExpEffs = {};
        String[] expHgvsc = {"t(1;2)(p1;q2)(c.51+5)"};
        String[] expHgvsp = {"t(1;2)(tr1:Glu1_Val17;)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation affecting a gene and an intergenic region
     * <p>
     * [ Chr1: Gene1                                                                   ]
     * ......>>>>>>>>>>>>>>>>>>>>>--------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..............................................................................................................
     * ^10                 ^30       ^40                                               ^90                                          |
     * |>
     * ------------
     * <|
     * |
     * ..........................................................................................................>>>>>>>>>>>>>>>>------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..........
     * ^110           ^125                     ^150                                    ^190
     * [ Chr2: Gene2                                                                   ]
     */
    @Test
    public void test05_2_one_gene() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 135, "N", "N", chr2, 124, true, true);

        EffectType[] expEffs = {EffectType.GENE_FUSION_HALF};
        EffectType[] notExpEffs = {};
        String[] expHgvsc = {"t(1;2)(q1;p2)(c.42-10)"};
        String[] expHgvsp = {"t(1;2)(;tr2:Ter15_Asn19)"};
        EffectImpact expectedImpact = EffectImpact.HIGH;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

    /**
     * Translocation not affecting any gene (intergenic regions)
     * <p>
     * [ Chr1: Gene1                                                                   ]
     * ......>>>>>>>>>>>>>>>>>>>>>--------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..............................................................................................................
     * ^10                 ^30       ^40                                               ^90                                          |
     * <|------------------------------------------------------------------------------------|>
     * |
     * ..........................................................................................................>>>>>>>>>>>>>>>>------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..........
     * ^110           ^125                     ^150                                    ^190
     * [ Chr2: Gene2                                                                   ]
     */
    @Test
    public void test06_no_gene() {
        Log.debug("Test");

        init(true, true);

        // Create variant
        VariantBnd variant = new VariantBnd(chr1, 135, "N", "N", chr2, 50, true, true);

        EffectType[] expEffs = {EffectType.FEATURE_FUSION};
        EffectType[] notExpEffs = {};
        String[] expHgvsc = {"t(1;2)(q1;q2)(n.136)"};
        String[] expHgvsp = {""};
        EffectImpact expectedImpact = EffectImpact.LOW;

        checkEffects(variant, expEffs, notExpEffs, expHgvsp, expHgvsc, expectedImpact);
    }

}
