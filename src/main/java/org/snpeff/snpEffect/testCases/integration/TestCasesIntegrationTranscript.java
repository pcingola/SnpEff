package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Utr5prime;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationTranscript {

    boolean debug = false;
    boolean verbose = false || debug;

    @Test
    public void test_01_mRnaSequence() {
        Log.debug("Test");
        String genome = "testHg3766Chr1";
        Config config = new Config(genome);

        if (verbose) Log.info("Loading genome " + genome);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();
        if (verbose) Log.info("Building interval forest");
        sep.buildForest();
        if (verbose) Log.info("Done");

        int count = 1;
        for (Gene gene : sep.getGenome().getGenes()) {
            for (Transcript tr : gene) {

                if (!tr.isProteinCoding()) continue;
                if (tr.hasErrorOrWarning()) continue;

                String mRna = tr.mRna().toLowerCase();
                String cds = tr.cds().toLowerCase();

                // Get UTR sequence
                List<Utr5prime> utrs5 = tr.get5primeUtrs();
                if (utrs5.size() <= 0) continue;

                if (verbose) Gpr.showMark(count++, 1);

                Utr5prime utr5 = utrs5.get(0);
                String utr5Str = utr5.getSequence().toLowerCase();

                // Sanity check
                if (!mRna.startsWith(utr5Str)) throw new RuntimeException("ERROR mRna does not start with UTR5");
                if (!mRna.startsWith(utr5Str + cds))
                    throw new RuntimeException("ERROR mRna does not start with  UTR+CDS");
            }
        }
    }

    @Test
    public void test_02_mapping_mRna_Cds() {
        Log.debug("Test");
        String genome = "testHg3766Chr1";
        Config config = new Config(genome);

        if (verbose) Log.info("Loading genome " + genome);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();
        if (verbose) Log.info("Building interval forest");
        sep.buildForest();
        if (verbose) Log.info("Done");

        int count = 1;
        for (Gene gene : sep.getGenome().getGenes()) {
            for (Transcript tr : gene) {

                if (!tr.isProteinCoding()) continue;
                if (tr.hasErrorOrWarning()) continue;

                String mRna = tr.mRna().toLowerCase();
                String cds = tr.cds().toLowerCase();

                // Get UTR sequence
                List<Utr5prime> utrs5 = tr.get5primeUtrs();
                if (utrs5.size() <= 0) continue;

                if (verbose) Gpr.showMark(count++, 1);

                Utr5prime utr5 = utrs5.get(0);
                String utr5Str = utr5.getSequence().toLowerCase();

                // Sanity check
                if (!mRna.startsWith(utr5Str)) throw new RuntimeException("ERROR mRna does not start with UTR5");
                if (!mRna.startsWith(utr5Str + cds))
                    throw new RuntimeException("ERROR mRna does not start with  UTR+CDS");
            }
        }
    }

    @Test
    public void test_03_baseNumberCds2Codon() {
        Log.debug("Test");
        String genome = "testHg19Chr1";
        Config config = new Config(genome);

        if (verbose) Log.info("Loading genome " + genome);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();
        if (verbose) Log.info("Done");

        int countOk = 0;
        for (Gene gene : sep.getGenome().getGenes()) {
            for (Transcript tr : gene) {
                if (!tr.isProteinCoding()) continue;
                if (tr.hasErrorOrWarning()) continue;

                if (debug) Log.debug(tr);
                CodonTable codonTable = tr.codonTable();

                String cds = tr.cds().toLowerCase();
                String protein = tr.protein();

                // Check each AA <-> codon mapping
                for (int cdsBaseNum = 0, aaNum = 0; cdsBaseNum < cds.length(); cdsBaseNum++) {
                    String codon = tr.baseNumberCds2Codon(cdsBaseNum);
                    if (codon == null) continue;

                    String aa = codonTable.aa(codon);
                    String aaReal = "" + protein.charAt(aaNum);

                    if (debug)
                        Log.debug("CDS base: " + cdsBaseNum + "\taaNum: " + aaNum + "\tAA: " + aa + " / " + aaReal + "\tcodon: " + codon);

                    if (aaNum == 0 && aaReal.equals("M") && !aaReal.equals(aa)) {
                        // First AA not always codes to 'M', we allow this exception
                    } else {
                        // Check that coding AA is the same
                        if (!aaReal.equals(aa)) {
                            String msg = "Difference in expected codon/AA:" //
                                    + "\n\tCDS base  : " + cdsBaseNum //
                                    + "\n\tAA number : " + aaNum //
                                    + "\n\tCodon     : " + codon //
                                    + "\n\tAA        : " + aa //
                                    + "\n\tAA [real] : " + protein.charAt(aaNum) //
                                    + "\n\tCount OK  : " + countOk //
                                    + "\n\n" + tr //
                                    ;

                            Log.debug(msg);
                            assertEquals(msg, aa, aaReal);
                        }
                    }

                    if (cdsBaseNum % 3 == 2) aaNum++;
                    countOk++;
                }
            }
        }

        assertTrue(countOk > 1, "No codon/AA checked!");
    }

    @Test
    public void test_04_codonNumber2Pos() {
        Log.debug("Test");
        String genome = "testHg19Chr1";
        Config config = new Config(genome);

        if (verbose) Log.info("Loading genome " + genome);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();
        if (verbose) Log.info("Done");

        int countOk = 0;
        for (Gene gene : sep.getGenome().getGenes()) {
            for (Transcript tr : gene) {
                if (!tr.isProteinCoding()) continue;
                if (tr.hasErrorOrWarning()) continue;

                if (debug) Log.debug(tr);
                CodonTable codonTable = tr.codonTable();
                String protein = tr.protein();

                // Check each AA <-> codon mapping
                for (int aaNum = 0; aaNum < protein.length(); aaNum++) {
                    // Create a codon using 'codonNumber2Pos(i)' mapping and compare to protein sequence
                    // Get codon coordinates
                    int[] codon = tr.codonNumber2Pos(aaNum);

                    // Get bases for each coordinate and build codon sequence
                    StringBuilder codonStr = new StringBuilder();
                    for (int j = 0; j < 3; j++) {
                        if (codon[j] < 0) break; // Negative codon number is error
                        String base = tr.baseAt(codon[j]);
                        if (base == null) break;
                        codonStr.append(base);
                    }

                    // Skip incomplete codons
                    if (codonStr.length() != 3) continue;

                    // Get 'real' and
                    String aaReal = "" + protein.charAt(aaNum);
                    String cstr = codonStr.toString();
                    if (tr.isStrandMinus()) cstr = GprSeq.reverseWc(cstr);
                    String aa = codonTable.aa(cstr);

                    // First AA not always codes to 'M', we allow this exception
                    if (aaNum == 0 && aaReal.equals("M") && !aaReal.equals(aa)) continue;

                    // Check
                    if (!aaReal.equals(aa)) {
                        String msg = "Difference in expected codon/AA:" //
                                + "\n\tCodon numer: " + aaNum //
                                + "\n\tCodon      : " + codon[0] + ", " + codon[1] + ", " + codon[2] //
                                + "\n\tBases      : " + tr.baseAt(codon[0]) + ", " + tr.baseAt(codon[1]) + ", " + tr.baseAt(codon[2]) //
                                + "\n\tCodonStr   : " + codonStr + "\t" + cstr + "\t" + tr.baseNumberCds2Codon(3 * aaNum) //
                                + "\n\tAA [real]  : " + aaReal //
                                + "\n\tAA         : " + aa //
                                + "\n\n" + tr //
                                ;
                        Log.debug(msg);
                        assertEquals(msg, aaReal, aa);
                    }
                    countOk++;
                }
            }
        }

        assertTrue(countOk > 0, "No codon/AA checked!");
    }

    @Test
    public void test_05_codonNumber_aaNumber() {
        Log.debug("Test");
        String genome = "testHg19Chr1";
        Config config = new Config(genome);

        if (verbose) Log.info("Loading genome " + genome);
        SnpEffectPredictor sep = config.loadSnpEffectPredictor();
        if (verbose) Log.info("Done");

        int countOk = 0;
        for (Gene gene : sep.getGenome().getGenes()) {
            for (Transcript tr : gene) {
                if (!tr.isProteinCoding()) continue;
                if (tr.hasErrorOrWarning()) continue;

                if (debug) Log.debug(tr);
                String protein = tr.protein();
                int[] aanum2pos = tr.aaNumber2Pos();

                // Check each AA <-> codon mapping
                for (int aaNum = 0; aaNum < protein.length(); aaNum++) {
                    // Get codon coordinateshrow new RuntimeException("FINISH THIS TEST CASE!!!!" + aanum2pos);
                    int[] codon = tr.codonNumber2Pos(aaNum);
                    int codonPos = (tr.isStrandPlus() ? codon[0] : codon[2]);
                    assertEquals(codonPos, aanum2pos[aaNum], //
                            "Genomic locations do not matcn:" //
                                    + "\n\taaNum           : " + aaNum //
                                    + "\n\tcodonNumber2Pos : " + codonPos //
                                    + "\n\taanum2pos       : " + aanum2pos[aaNum] //
                    );

                    countOk++;
                    if (verbose) Gpr.showMark(countOk, 1);
                }
            }
        }

        assertTrue(countOk > 0, "No codon/AA checked!");
    }
}
