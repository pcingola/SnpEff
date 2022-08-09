package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesSnps extends TestCasesBase {

    public static int N = 1000;

    public TestCasesSnps() {
        super();
    }

    String effectStr(VariantEffect effect) {
        String effStr = effect.effect(true, true, true, false, false);
        String aaStr = effect.getAaChangeOld();
        int idx = effStr.indexOf('(');
        return effStr.substring(0, idx) + "(" + aaStr + ")";
    }

    @Override
    protected void init() {
        super.init();
        randSeed = 20100629;
    }

    @Test
    public void test_01() {
        Log.debug("Test");

        CodonTable codonTable = genome.codonTable();

        // Test N times
        //	- Create a random gene transcript, exons
        //	- Change each base in the exon
        //	- Calculate effect
        for (int i = 0; i < N; i++) {
            initSnpEffPredictor();
            if (debug) System.out.println("SNP Test iteration: " + i + "\n" + transcript);
            else if (verbose)
                Log.info("SNP Test iteration: " + i + "\t" + transcript.getStrand() + "\t" + transcript.cds());
            else Gpr.showMark(i + 1, 1);

            int cdsBaseNum = 0;

            // For each exon...
            for (Exon exon : transcript.sortedStrand()) {
                int step = exon.isStrandPlus() ? 1 : -1;
                int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEndClosed();

                // For each base in this exon...
                for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEndClosed()); pos += step, cdsBaseNum++) {
                    // Reference base
                    char refBase = chromoBases[pos]; // exon.basesAt(pos - exon.getStart(), 1).charAt(0);
                    refBase = Character.toUpperCase(refBase);
                    // Codon number
                    int cdsCodonNum = cdsBaseNum / 3;
                    int cdsCodonPos = cdsBaseNum % 3;

                    int minCodonPos = cdsCodonNum * 3;
                    int maxCodonPos = minCodonPos + 3;
                    if (maxCodonPos < transcript.cds().length()) {
                        String codon = transcript.cds().substring(minCodonPos, maxCodonPos);
                        codon = codon.toUpperCase();
                        String aa = codonTable.aa(codon);

                        // Get a random base different from 'refBase'
                        char snp = refBase;
                        while (snp == refBase) {
                            snp = Character.toUpperCase(GprSeq.randBase(rand));
                        }

                        // Codon change
                        String newCodon = codon.substring(0, cdsCodonPos) + snp + codon.substring(cdsCodonPos + 1);
                        String newAa = codonTable.aa(newCodon);
                        String effectExpected = "";

                        // Effect
                        if (newAa.equals(aa)) {
                            if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
                                if (codonTable.isStart(newCodon)) effectExpected = "SYNONYMOUS_START(" + aa + ")";
                                else effectExpected = "START_LOST(" + aa + ")";
                            } else if (aa.equals("*")) effectExpected = "SYNONYMOUS_STOP(" + aa + ")";
                            else effectExpected = "SYNONYMOUS_CODING(" + aa + ")";
                        } else {
                            if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
                                if (codonTable.isStart(newCodon))
                                    effectExpected = "NON_SYNONYMOUS_START(" + aa + "/" + newAa + ")";
                                else effectExpected = "START_LOST(" + aa + "/" + newAa + ")";
                            } else if (codonTable.isStop(codon)) effectExpected = "STOP_LOST(" + aa + "/" + newAa + ")";
                            else if (codonTable.isStop(newCodon))
                                effectExpected = "STOP_GAINED(" + aa + "/" + newAa + ")";
                            else effectExpected = "NON_SYNONYMOUS_CODING(" + aa + "/" + newAa + ")";
                        }

                        // Create a variant
                        if (exon.isStrandMinus()) {
                            snp = GprSeq.wc(snp);
                            refBase = GprSeq.wc(refBase);
                        }
                        Variant variant = new Variant(chromosome, pos, refBase + "", snp + "", "");

                        if (!variant.isVariant()) effectExpected = "EXON";

                        // Calculate effects
                        VariantEffects effects = snpEffectPredictor.variantEffect(variant);

                        // Checknumber of results
                        assertEquals(true, effects.size() == 1);
                        if (debug) System.out.println(effects);

                        // Check effect
                        VariantEffect effect = effects.get();
                        String effStr = effectStr(effect);
                        if (debug) System.out.println("\tPos: " + pos //
                                + "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
                                + "\t" + variant //
                                + "\tCodon: " + codon + " -> " + newCodon //
                                + "\tAA: " + aa + " -> " + newAa //
                                + "\tEffect: " + effStr);

                        // Check effect
                        assertEquals(effectExpected, effStr);
                    }
                }
            }
        }

        System.err.println();
    }

}
