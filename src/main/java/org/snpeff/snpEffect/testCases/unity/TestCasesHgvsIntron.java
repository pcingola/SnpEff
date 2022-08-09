package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsBase;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesHgvsIntron extends TestCasesHgvsBase {

    @Test
    public void test_01_intron() {
        Log.debug("Test");
        int N = 1000;

        int testIter = -1;
        int testPos = -1;

        // Test N times
        //	- Create a random gene transcript, exons
        //	- Change each base in the exon
        //	- Calculate effect
        for (int checked = 0, it = 1; checked < N; it++) {
            initSnpEffPredictor(true, false);
            boolean tested = false;

            // Skip test?
            if (testIter >= 0 && it < testIter) {
                Log.debug("Skipping iteration: " + it);
                continue;
            }

            // No introns? Nothing to test
            if (transcript.introns().size() < 1) continue;

            // Character representation
            String trstr = transcript.toStringAsciiArt(false);
            char bases[] = trstr.toCharArray();

            // Show data
            if (verbose) {
                System.out.println("HGSV Intron\titeration:" + checked + "\t" + (transcript.isStrandPlus() ? "+" : "-"));
                System.out.println(trstr);
                System.out.println("Length   : " + transcript.size());
                System.out.println("CDS start: " + transcript.getCdsStart());
                System.out.println("CDS end  : " + transcript.getCdsEnd());
                System.out.println(transcript);
            } else Gpr.showMark(it, 1);

            // Check each intronic base
            for (int j = 0, pos = transcript.getStart(); pos < transcript.getEndClosed(); j++, pos++) {
                // Intron?
                if (bases[j] == '-') {
                    tested = true;

                    // Skip base?
                    if (testPos >= 0 && pos < testPos) {
                        Log.debug("\tSkipping\tpos: " + pos + " [" + j + "]");
                        continue;
                    }

                    // Ref & Alt
                    String refStr = "A", altStr = "T";

                    // Calculate expected hgsv string
                    String hgsv = intronHgsv(bases, j, pos, refStr, altStr);

                    // Calculate effect and compare to expected
                    Variant sc = new Variant(transcript.getChromosome(), pos, refStr, altStr, "");
                    VariantEffects ceffs = snpEffectPredictor.variantEffect(sc);
                    VariantEffect ceff = ceffs.get();
                    String hgsvEff = ceffs.get().getHgvs();
                    if (debug)
                        System.out.println("\tpos: " + pos + " [" + j + "]\thgsv: '" + hgsv + "'\tEff: '" + hgsvEff + "'\t" + ceff.getEffectType());

                    // Is this an intron? (i.e. skip other effects, such as splice site)
                    // Compare expected to real HGSV strings
                    if (ceff.getEffectType() == EffectType.INTRON) assertEquals(hgsv, hgsvEff);
                }
            }

            if (tested) checked++;
        }
        System.err.println();
    }

}
