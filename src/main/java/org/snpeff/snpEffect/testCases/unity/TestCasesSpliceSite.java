package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Intron;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Test Splice sites variants
 *
 * @author pcingola
 */
public class TestCasesSpliceSite extends TestCasesBase {

    public static int N = 1000;

    public TestCasesSpliceSite() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        randSeed = 20141205;
        minExons = 2;
    }

    @Test
    public void test_01() {
        Log.debug("Test");

        // Test N times
        //	- Create a random gene transcript, exons
        //	- Change each base in the exon's splice sites
        //	- Calculate effect and check
        for (int i = 0; i < N; i++) {
            initSnpEffPredictor();
            if (verbose) Log.info("Splice Test iteration: " + i + "\n" + transcript);
            else Gpr.showMark(i + 1, 1);

            for (Intron intron : transcript.introns()) {
                int ssBases = Math.min(SpliceSite.CORE_SPLICE_SITE_SIZE - 1, intron.size());

                // Splice site donor
                EffectType effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? EffectType.SPLICE_SITE_ACCEPTOR : null);
                for (int pos = intron.getStart(); pos <= intron.getStart() + ssBases; pos++) {
                    Variant variant = new Variant(chromosome, pos, "A", "T");
                    checkEffect(variant, EffectType.SPLICE_SITE_DONOR, effectNotExpected, EffectImpact.HIGH);
                }

                // Splice site acceptor
                effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? EffectType.SPLICE_SITE_DONOR : null);
                for (int pos = intron.getEndClosed() - ssBases; pos <= intron.getEndClosed(); pos++) {
                    Variant variant = new Variant(chromosome, pos, "A", "T");
                    checkEffect(variant, EffectType.SPLICE_SITE_ACCEPTOR, effectNotExpected, EffectImpact.HIGH);
                }
            }
        }

        System.err.println();
    }
}
