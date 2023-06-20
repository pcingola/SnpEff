package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.*;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test case for basic HGV annotations
 */
public class TestCasesHgvs extends TestCasesBase {

    public TestCasesHgvs() {
        super();
    }

    void checkHgvsProt(Variant variant, String expectedHgvsC, String expectedHgvsP) {
        prependSequenceToFirstExon("atgaaaatgggccatcagcagcagtgctgc"); // This is 'MKMGHQQQCC' as a DNA sequence

        if (verbose) {
            Log.debug("\nChromsome : " + chromoSequence //
                    + "\nTranscript:\n" + transcript //
                    + "\nVariant   : " + variant //
            );
        }

        // Analyze variant
        VariantEffects effs = snpEffectPredictor.variantEffect(variant);

        // Calculate HGVS
        VariantEffect eff = effs.get();
        HgvsDna hgvsc = new HgvsDna(eff);
        String hgvsDna = hgvsc.toString();
        HgvsProtein hgvsp = new HgvsProtein(eff);
        String hgvsProt = hgvsp.toString();

        // Check result
        if (verbose) {
            Log.debug("Eff        : " + eff);
            Log.debug("HGVS (DNA) : '" + hgvsDna + "'");
            Log.debug("HGVS (Prot): '" + hgvsProt + "'");
        }

        if (expectedHgvsC != null) assertEquals(expectedHgvsC, hgvsDna, "HGVS.c notation does not match");
        if (expectedHgvsP != null) assertEquals(expectedHgvsP, hgvsProt, "HGVS.p notation does not match");
    }

    /**
     * Test case: Use 1-letter AA change
     */
    @Test
    public void test_01() {
        Log.debug("Test");

        Config.get().setHgvsOneLetterAA(true);
        Variant variant = new Variant(chromosome, 898, "C", "A", ""); // Add 'GHQ' amino acids
        checkHgvsProt(variant, null, "p.Q7K");
        Config.get().setHgvsOneLetterAA(false);
    }

    /**
     * Test case: Use transcript ID
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        Config.get().setHgvsTrId(true);
        Variant variant = new Variant(chromosome, 898, "C", "A", ""); // Add 'GHQ' amino acids
        checkHgvsProt(variant, "transcript_0:c.19C>A", "transcript_0:p.Gln7Lys");
        Config.get().setHgvsTrId(false);
    }

    /**
     * Test case: Use old HGVS.C nomenclature
     * E.g. : c.G123T instead of c.123G>T
     */
    @Test
    public void test_03() {
        Log.debug("Test");

        Config.get().setHgvsOld(true);
        Variant variant = new Variant(chromosome, 898, "C", "A", ""); // Add 'GHQ' amino acids
        checkHgvsProt(variant, "c.C19A", null);
        Config.get().setHgvsOld(false);
    }

}
