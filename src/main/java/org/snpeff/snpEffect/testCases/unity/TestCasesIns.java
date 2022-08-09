package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIns extends TestCasesBase {

    public static int N = 1000;

    public TestCasesIns() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        randSeed = 20100629;
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
            CodonTable codonTable = genome.codonTable();

            if (debug)
                System.out.println("INS Test iteration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());
            else if (verbose) Log.info("INS Test iteration: " + i + "\n" + transcript);
            else Gpr.showMark(i + 1, 1);

            int cdsBaseNum = 0;

            // For each exon...
            for (Exon exon : transcript.sortedStrand()) {
                int step = exon.isStrandPlus() ? 1 : -1;
                int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEndClosed();

                // For each base in this exon...
                for (int pos = beg; (exon.getStart() <= pos) && (pos <= exon.getEndClosed()); pos += step, cdsBaseNum++) {

                    // Get a random base different from 'refBase'
                    int insLen = rand.nextInt(10) + 1;
                    String insPlus = GprSeq.randSequence(rand, insLen); // Insertion (plus strand)
                    String ins = insPlus;

                    // Codon number
                    int cdsCodonNum = cdsBaseNum / 3;
                    int cdsCodonPos = cdsBaseNum % 3;

                    int minCodonPos = cdsCodonNum * 3;
                    int maxCodonPos = minCodonPos + 3;
                    if (maxCodonPos < transcript.cds().length()) {
                        String codonOld = transcript.cds().substring(minCodonPos, maxCodonPos);
                        codonOld = codonOld.toUpperCase();
                        String aaOld = codonTable.aa(codonOld);

                        // Codon change
                        String codonNew = "", aaNew = "";

                        // Create a SeqChange
                        if (exon.isStrandMinus()) ins = GprSeq.reverseWc(insPlus);
                        Variant variant = new Variant(chromosome, pos, "", "+" + ins, "");

                        // Is it an insertion?
                        assertEquals(true, variant.isIns());

                        // Codon change
                        int idx = cdsCodonPos;
                        if (transcript.isStrandMinus()) idx++; // Insert AFTER base (in negative strand)
                        codonNew = codonOld.substring(0, idx) + insPlus + codonOld.substring(idx);
                        aaNew = codonTable.aa(codonNew);

                        // Expected Effect
                        EffectType effectExpected = null;
                        String aaExpected = "";
                        if (insLen % 3 != 0) {
                            effectExpected = EffectType.FRAME_SHIFT;
                            aaExpected = aaOld + "/" + aaNew;
                        } else {
                            if (cdsCodonPos == 0) {
                                effectExpected = EffectType.CODON_INSERTION;
                                aaExpected = aaOld + "/" + aaNew;
                            } else {
                                if (codonNew.startsWith(codonOld)) {
                                    effectExpected = EffectType.CODON_INSERTION;
                                    aaExpected = aaOld + "/" + aaNew;
                                } else {
                                    effectExpected = EffectType.CODON_CHANGE_PLUS_CODON_INSERTION;
                                    aaExpected = aaOld + "/" + aaNew;
                                }
                            }

                            if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonOld) && !codonTable.isStartFirst(codonNew)) {
                                effectExpected = EffectType.START_LOST;
                                aaExpected = aaOld + "/" + aaNew;
                            } else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) {
                                effectExpected = EffectType.STOP_LOST;
                                aaExpected = aaOld + "/" + aaNew;
                            } else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) {
                                effectExpected = EffectType.STOP_GAINED;
                                aaExpected = aaOld + "/" + aaNew;
                            }
                        }

                        // Calculate effects
                        VariantEffects effects = snpEffectPredictor.variantEffect(variant);

                        // There should be at least one effect
                        assertTrue(effects.size() > 0);

                        // Show
                        boolean ok = false;
                        for (VariantEffect effect : effects) {
                            String effFullStr = effect.effect(true, true, false, false, false);
                            String effStr = effect.effect(true, false, false, false, false);
                            String aaStr = effect.getAaChangeOld();

                            if (debug) System.out.println("\tPos: " + pos //
                                    + "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
                                    + "\t" + variant //
                                    + "\tCodon: " + codonOld + " -> " + codonNew //
                                    + "\tAA: " + aaOld + " -> " + aaNew //
                                    + "\n\t\tEffect          : '" + effStr + "'\t'" + effFullStr + "'" //
                                    + "\n\t\tEffect expected : '" + effectExpected + "'" //
                                    + "\n\t\tAA              : '" + aaStr + "'" //
                                    + "\n\t\tAA expected     : '" + aaExpected + "'" //
                            );

                            // Check that there is a match
                            ok |= effect.hasEffectType(effectExpected);
                        }

                        // Check effect
                        assertTrue(ok, "Could not find effect '" + effectExpected + "' and AA '" + aaExpected + "'");
                    }
                }
            }
        }

        System.err.println("");
    }
}
