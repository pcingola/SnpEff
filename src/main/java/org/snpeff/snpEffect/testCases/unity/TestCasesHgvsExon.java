package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsBase;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesHgvsExon extends TestCasesHgvsBase {

    @Test
    public void test_01_coding() {
        Log.debug("Test");

        int N = 1000;
        CodonTable codonTable = genome.codonTable();

        // Test N times
        //	- Create a random gene transcript, exons
        //	- Change each base in the exon
        //	- Calculate effect
        for (int i = 0; i < N; i++) {
            initSnpEffPredictor(false, true);
            if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
            else if (verbose)
                Log.info("HGSV Coding\titeration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());
            else Gpr.showMark(i + 1, 1);

            int cdsBaseNum = 0;

            // For each exon...
            for (Exon exon : transcript.sortedStrand()) {
                // For each base in this exon...
                for (int pos = exon.getStart(); (exon.getStart() <= pos) && (pos <= exon.getEndClosed()); pos++, cdsBaseNum++) {

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
                        String aa = codonTable.aaThreeLetterCode(codonTable.aa(codon));

                        // Get a random base different from 'refBase'
                        char snp = refBase;
                        while (snp == refBase)
                            snp = Character.toUpperCase(GprSeq.randBase(rand));

                        // Codon change
                        String newCodon = codon.substring(0, cdsCodonPos) + snp + codon.substring(cdsCodonPos + 1);
                        String newAa = codonTable.aaThreeLetterCode(codonTable.aa(newCodon));
                        String protHgvs = "";
                        String dnaHgvs = "c." + (cdsBaseNum + 1) + refBase + ">" + snp;

                        // Effect
                        if (newAa.equals(aa)) {
                            if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
                                if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
                                else protHgvs = "p." + aa + "1?";
                            } else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
                        } else {
                            if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
                                if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
                                else protHgvs = "p." + aa + "1?";
                            } else if (codonTable.isStop(codon))
                                protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa + "ext*?";
                            else if (codonTable.isStop(newCodon)) protHgvs = "p." + aa + (cdsCodonNum + 1) + "*";
                            else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
                        }

                        String effectExpected = protHgvs + "/" + dnaHgvs;

                        // Create a SeqChange
                        Variant variant = new Variant(chromosome, pos, refBase + "", snp + "", "");

                        if (!variant.isVariant()) protHgvs = "EXON";

                        // Calculate effects
                        VariantEffects effects = snpEffectPredictor.variantEffect(variant);

                        // There should be only one effect
						assertTrue(effects.size() <= 1);

                        // Show
                        if (effects.size() == 1) {
                            VariantEffect effect = effects.get();
                            String effStr = effect.getHgvs();

                            if (debug) System.out.println("\tPos: " + pos //
                                    + "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
                                    + "\t" + variant //
                                    + "\tCodon: " + codon + " -> " + newCodon //
                                    + "\tAA: " + aa + " -> " + newAa //
                                    + "\tEffect expected: " + effectExpected //
                                    + "\tEffect: " + effStr);

                            // Check effect
                            assertEquals(effectExpected, effStr);

                            // Check that effect can be inserted into a VCF field
                            if (!VcfEntry.isValidInfoValue(effStr)) {
                                String err = "No white-space, semi-colons, or equals-signs are permitted in INFO field. Value:\"" + effStr + "\"";
                                System.err.println(err);
                                throw new RuntimeException(err);
                            }

                        }
                    }
                }
            }
        }
        System.err.println();
    }

}
