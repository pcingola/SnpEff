package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.align.VariantRealign;
import org.snpeff.binseq.GenomicSequences;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for variant realignment
 */
public class TestCasesVariantRealignment extends TestCasesBase {

    public TestCasesVariantRealignment() {
        super();
    }

    void checkRealign(String chrName, String chrSequence, int pos, String ref, String alt, String expectedVariantRealign) {
        // Create genome & chromosome
        Genome genome = new Genome("zzz");
        genome.getOrCreateChromosome(chrName).setSequence(chrSequence);

        // Create genomicSequences
        GenomicSequences gs = genome.getGenomicSequences();
        gs.addChromosomeSequence(chrName, chrSequence);
        gs.build();

        // Create variant
        Variant variant = new Variant(genome.getOrCreateChromosome(chrName), pos, ref, alt);

        // Realign variant
        VariantRealign vr = new VariantRealign(variant);
        vr.realign();
        if (verbose) Log.debug("Realigned variant: " + vr);

        // Check results
        assertEquals(expectedVariantRealign, vr.getVariantRealigned().toString());
    }

    @Override
    protected void init() {
        super.init();
        minExons = 2;
        randSeed = 20141128;
        initRand();
    }

    /**
     * Shift by one position
     */
    @Test
    public void test_01() {
        Log.debug("Test");

        // Change exon's sequence
        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 754, "", "T", "");
        if (verbose) Log.debug("Variant: " + variant);

        // Shift variant
        if (verbose) Log.debug("Variant (before): " + variant);
        Variant variantShifted = variant.realignLeft();
        if (verbose) Log.debug("Variant (after): " + variantShifted);

        // Check that shifted variant is oK
        assertNotSame(variant, variantShifted);
        assertEquals(756, variantShifted.getStart());
        assertEquals("", variantShifted.getReference());
        assertEquals("T", variantShifted.getAlt());
    }

    /**
     * No shift
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        // Change exon's sequence
        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 754, "", "A", "");
        if (verbose) Log.debug("Variant: " + variant);

        // Shift variant
        if (verbose) Log.debug("Variant (before): " + variant);
        Variant variantShifted = variant.realignLeft();
        if (verbose) Log.debug("Variant (after): " + variantShifted);

        // Check that shifted variant is the same object
        assertSame(variant, variantShifted);
    }

    /**
     * Shift by one position
     */
    @Test
    public void test_03() {
        Log.debug("Test");

        // Change exon's sequence
        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 1025, "", "G", "");
        if (verbose) Log.debug("Variant: " + variant);

        // Shift variant
        if (verbose) Log.debug("Variant (before): " + variant);
        Variant variantShifted = variant.realignLeft();
        if (verbose) Log.debug("Variant (after): " + variantShifted);

        // Check that shifted variant is oK
        assertNotSame(variant, variantShifted);
        assertEquals(1030, variantShifted.getStart());
        assertEquals("", variantShifted.getReference());
        assertEquals("G", variantShifted.getAlt());
    }

    /**
     * Shift by one position in an intron
     */
    @Test
    public void test_04_Intron() {
        Log.debug("Test");

        // Change exon's sequence
        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 920, "", "C", "");
        if (verbose) Log.debug("Variant: " + variant);

        // Shift variant
        if (verbose) Log.debug("Variant (before): " + variant);
        Variant variantShifted = variant.realignLeft();
        if (verbose) Log.debug("Variant (after): " + variantShifted);

        // Check that shifted variant is oK
        assertNotSame(variant, variantShifted);
        assertEquals(925, variantShifted.getStart());
    }

    /**
     * Test case from Savant's poster
     * http://www.well.ox.ac.uk/savant
     * (Marton Munz, Elise Ruark, Nazneen Rahman, Gerton Lunter)
     */
    @Test
    public void test_05_savant() {
        Log.debug("Test");
        String seqRef = "AAACTGTATTT";
        String seqAlt = "AAACTATTT";

        VariantRealign vr = new VariantRealign();
        vr.setSequenceRef(seqRef);
        vr.setSequenceAlt(seqAlt);
        vr.realignSeqs();
        if (verbose) Log.debug(vr);

        // Check resutls
        assertEquals("GT", vr.getRefRealign());
        assertEquals("", vr.getAltRealign());
    }

    @Test
    public void test_05_savant_opposite() {
        Log.debug("Test");
        String seqRef = "AAACTATTT";
        String seqAlt = "AAACTGTATTT";

        VariantRealign vr = new VariantRealign();
        vr.setSequenceRef(seqRef);
        vr.setSequenceAlt(seqAlt);
        vr.realignSeqs();
        if (verbose) Log.debug(vr);

        // Check resutls
        assertEquals("", vr.getRefRealign());
        assertEquals("GT", vr.getAltRealign());
    }

    /**
     * Same as test Savant's test case, but using variant and GenomicSequences
     */
    @Test
    public void test_06_savant() {
        Log.debug("Test");
        checkRealign("1", "AAACTGTATTT", 4, "TG", "", "chr1:5_GT/");
    }

    /**
     * Another test case fmor Savant's poster
     */
    @Test
    public void test_07_savant() {
        Log.debug("Test");
        checkRealign("1", "TATGTTTAGGTTTATTGCATTCT", 8, "", "GGG", "chr1:10_/GGG");
    }

    /**
     * This test should trigger progressive realignment. I.e. requiring more
     * bases to the right in order to make a good alignment.
     */
    @Test
    public void test_08_reallyLongRealign() {
        Log.debug("Test");
        checkRealign("1", "tatgaccagcagcagcagcagcagcagcagcagcagcagcagcagcaagcccttcagag", 6, "CAG", "", "chr1:44_GCA/");
    }

    /**
     * This test should trigger (AND fail) to make a progressive realignment.
     * I.e. requiring more bases to the right in order to make a good
     * alignment, but there are no more bases available from GenomicSequences.
     */
    @Test
    public void test_09_reallyLongRealign() {
        Log.debug("Test");
        checkRealign("1", "tatgaccagcagcagcagcagcagcagcagcagcag", 6, "CAG", "", "chr1:33_CAG/");
    }

}
