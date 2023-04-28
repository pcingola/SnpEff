package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test random Interval Variants (e.g. when reading a BED file)
 *
 * @author pcingola
 */
public class TestCasesIntervalVariant extends TestCasesBase {

    public static int N = 1000;

    public TestCasesIntervalVariant() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        randSeed = 20120426;
    }

    @Test
    public void test_01() {
        Log.debug("Test");

        // Test N times
        //	- Create a random gene transcript, exons
        //	- Create a random Insert at each position
        //	- Calculate effect
        for (int i = 0; i < N; i++) {
            initSnpEffPredictor();
            if (debug) System.out.println("INTERVAL (Variant) Test iteration: " + i + "\n" + transcript);
            else Gpr.showMark(i + 1, 1);

            // For each base in the transcript
            // For each base in this exon...
            for (int pos = 0; pos < chromosome.size(); pos++) {
                //---
                // Create variant
                //---
                // Interval length
                int intLen = rand.nextInt(100) + 1;
                int start = pos;
                int end = Math.min(pos + intLen, chromosome.getEndClosed());

                // Create a variant
                Variant variant = new Variant(chromosome, start, end, "");

                // Sanity checks
                assertEquals(true, variant.isInterval()); // Is it an interval?

                //---
                // Expected Effect
                //---
                EffectType expectedEffect = null;
                if (transcript.intersects(variant)) {
                    // Does it intersect any exon?
                    for (Exon ex : transcript)
                        if (ex.intersects(variant)) expectedEffect = EffectType.EXON;

                    for (Intron intron : transcript.introns())
                        if (intron.intersects(variant)) expectedEffect = EffectType.INTRON;
                } else if (gene.intersects(variant)) {
                    // Gene intersects but transcript doesn't?
                    if (expectedEffect == null) expectedEffect = EffectType.INTRAGENIC;
                } else expectedEffect = EffectType.INTERGENIC;

                //---
                // Calculate Effect
                //---
                VariantEffects effects = snpEffectPredictor.variantEffect(variant);

                //---
                // Check effect
                //---
                // There should be only one effect in most cases
                assertEquals(false, effects.isEmpty()); // There should be at least one effect
                if (debug && (effects.size() > 1)) {
                    System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
                    for (VariantEffect eff : effects)
                        System.out.println("\t" + eff);
                }

                boolean isExpectedOK = false;
                StringBuilder effSb = new StringBuilder();
                for (VariantEffect effect : effects) {
                    String effstr = effect.effect(true, true, true, false, false);

                    isExpectedOK |= effect.hasEffectType(expectedEffect);
                    effSb.append(effstr + " ");

                }

                if (debug || !isExpectedOK) //
                    System.out.println("\nVariant         : " + variant //
                            + "\nExpected Effect : '" + expectedEffect + "'" //
                            + "\nEffects         : '" + effSb + "'" //
                            + "\n--------------------------------------------------------------\n" //
                    );
                assertEquals(true, isExpectedOK);
            }
        }
    }
}
