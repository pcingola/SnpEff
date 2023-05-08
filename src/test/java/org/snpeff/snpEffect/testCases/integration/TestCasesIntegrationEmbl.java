package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for EMBL file parsing (database creation)
 *
 * @author pcingola
 */
public class TestCasesIntegrationEmbl extends TestCasesIntegrationBase {

    public static boolean debug = false;
    int exonToStringVersionOri;

    public TestCasesIntegrationEmbl() {
        super();
    }

    @AfterEach
    public void after() {
        Exon.ToStringVersion = exonToStringVersionOri;
    }

    @Override
    @BeforeEach
    public void before() {
        super.before();
        exonToStringVersionOri = Exon.ToStringVersion;
        Exon.ToStringVersion = 1; // Set "toString()" version
    }

    @Test
    public void testCase_Exon_Simple() {
        Log.debug("Test");
        // Create SnpEff predictor
        String genome = "testEmblPberghei";
        String resultFile = path("testEmblPberghei.genes.embl");
        SnpEffectPredictor sep = buildEmbl(genome, resultFile);

        int pos = 4056 - 1;
        for (Gene g : sep.getGenome().getGenes()) {
            if (debug) System.out.println("Gene: '" + g.getGeneName() + "', '" + g.getId() + "'");
            for (Transcript tr : g) {
                if (debug) System.out.println("\tTranscript: '" + tr.getId() + "'");
                for (Exon e : tr) {
                    if (debug)
                        System.out.println("\t\tExon (" + e.getStrand() + "): '" + e.getId() + "'\t" + e.toStr());
                    if (e.intersects(pos)) {
                        String seq = e.getSequence();
                        String base = e.basesAtPos(pos, 1);
                        if (debug) System.out.println("Seq : " + seq + "\nBase: " + base);
                        assertEquals("g", base);
                    }
                }
            }
        }
    }
}
