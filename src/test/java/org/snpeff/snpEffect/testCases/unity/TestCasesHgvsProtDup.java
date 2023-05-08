package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.HgvsDna;
import org.snpeff.snpEffect.HgvsProtein;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test case
 */
public class TestCasesHgvsProtDup extends TestCasesBase {

    public TestCasesHgvsProtDup() {
        super();
    }

    /**
     * Test case: p.Gly4_Gln6dup in the sequence MKMGHQQQCC denotes a duplication
     * of amino acids Glycine-4 (Gly, G) to Glutamine-6 (Gln, Q) (i.e. MKMGHQGHQQQCC)
     * <p>
     * Reference: http://www.hgvs.org/mutnomen/recs-prot.html#dup
     */
    @Test
    public void test_01() {
        Log.debug("Test");

        prependSequenceToFirstExon("atgaaaatgggccatcagcagcagtgctgc"); // This is 'MKMGHQQQCC' as a DNA sequence

        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 898, "", "ggccatcag", ""); // Add 'GHQ' amino acids
        if (verbose) Log.debug("Variant: " + variant);

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

        assertEquals("c.10_18dupGGCCATCAG", hgvsDna);
        assertEquals("p.Gly4_Gln6dup", hgvsProt);
    }

    /**
     * Reference: http://www.hgvs.org/mutnomen/recs-prot.html#dup
     * duplicating insertions in single amino acid stretches (or short tandem repeats) are
     * described as a duplication, e.g. a duplicating HQ insertion in the HQ-tandem repeat
     * sequence of MKMGHQHQCC to MKMGHQHQHQCC is described as p.His7_Gln8dup (not p.Gln8_Cys9insHisGln)
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        prependSequenceToFirstExon("atgaaaatgggccatcagcatcagcagcagtgctgc"); // This is 'MKMGHQQQCC' as a DNA sequence

        if (verbose) Log.debug(transcript);

        // Create variant
        Variant variant = new Variant(chromosome, 904, "", "catcag", ""); // Add 'HQ' amino acids
        if (verbose) Log.debug("Variant: " + variant);

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

        assertEquals("c.19_24dupCATCAG", hgvsDna);
        assertEquals("p.His7_Gln8dup", hgvsProt);
    }
}
