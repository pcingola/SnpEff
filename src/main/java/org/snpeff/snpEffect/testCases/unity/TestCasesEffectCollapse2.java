package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesEffectCollapse2 extends TestCasesBase {

    public TestCasesEffectCollapse2() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        randSeed = 20150202;
        spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
        spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
        spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;
        maxExons = 10;
        minExons = 5;
        addUtrs = true;
        onlyPlusStrand = true;
    }

    @Test
    public void test_01() {
        Log.debug("Test");

        // Show gene(s)
        Genome genome = snpEffectPredictor.getGenome();
        if (verbose) {
            for (Gene g : genome.getGenes())
                System.out.println(g);
        }

        // Create variant
        Variant variant = new Variant(genome.getChromosome("1"), 695, "ACGTACGTACGTACGT", "");
        if (verbose)
            Log.info(variant.getVariantType() + "\t" + variant.getStart() + "-" + variant.getEndClosed() + "\t" + variant);

        // Calculate effect
        VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

        // Look for expected annotations
        String expectedAnn = "5_prime_UTR_truncation&exon_loss_variant";
        boolean foundSo = false;
        boolean foundAnn = false;
        for (VariantEffect veff : veffs) {
            VcfEffect vcfEff = new VcfEffect(veff, EffFormatVersion.FORMAT_ANN_1);
            if (verbose) Log.info(vcfEff.getEffectsStrSo() + "\t" + vcfEff);

            foundSo |= vcfEff.getEffectsStrSo().equalsIgnoreCase(expectedAnn);
            foundAnn |= vcfEff.toString().contains(expectedAnn);
        }

        // Annotation found?
        assertTrue(foundSo, "Annotation (SO) '" + expectedAnn + "' not found");
        assertTrue(foundAnn, "Annotation '" + expectedAnn + "' not found in 'ANN' field");
    }

}
