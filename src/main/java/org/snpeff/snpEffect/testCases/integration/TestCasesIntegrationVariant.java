package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.fileIterator.VariantFileIterator;
import org.snpeff.fileIterator.VariantTxtFileIterator;
import org.snpeff.interval.*;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationVariant extends TestCasesIntegrationBase {

    long randSeed = 20100629;
    String genomeName = "testCase";

    public TestCasesIntegrationVariant() {
        super();
    }

    /**
     * CDS test (CDS = CoDing Sequences)
     * Build CDS form exon sequences
     */
    @Test
    public void test_08() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.initSnpEffPredictor();

        // Read CDS (hg37, chromosome Y) from a file and store them indexed by transcript ID
        HashMap<String, String> cdsByTrId = new HashMap<>();
        String cdsY = Gpr.readFile(path("cds_hg37_chrY.txt"));
        String[] lines = cdsY.split("\n");
        for (String line : lines) {
            String[] recs = line.split("\t");
            cdsByTrId.put(recs[0], recs[1]);
        }

        // Calculate CDS from chromosome Y and compare
        int totalOk = 0;
        for (Gene gint : comp.config.getGenome().getGenes()) {
            for (Transcript tint : gint) {
                String seqOri = cdsByTrId.get(tint.getId());

                if (seqOri != null) {
                    String seq = tint.cds();
                    // Compare CDS sequences
                    if (!seqOri.equalsIgnoreCase(seq))
                        throw new RuntimeException("CDS do not match:\nTranscipt:" + tint.getId() + " " + tint.isStrandMinus() + "\n\t" + seq + "\n\t" + seqOri + "\n");
                    else {
                        if (verbose) Log.info("CDS compare:\n\t" + seqOri + "\n\t" + seq);
                        totalOk++;
                    }
                }
            }
        }
        if (totalOk == 0) throw new RuntimeException("No sequences compared!");
    }

    /**
     * Test SNP effect predictor for a transcript
     */
    @Test
    public void test_09() {
        String trId = "ENST00000250823";
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.setUseAaNoNum(true);
        comp.snpEffect(path(trId + ".out"), trId, true);
    }

    /**
     * Test SNP effect predictor: Test UTR distances, Up/Downstream distances
     */
    @Test
    public void test_11() {
        String trId = "ENST00000250823";
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.setUseAaNoNum(true);
        comp.snpEffect(path(trId + "_all.out"), trId, false);
    }

    /**
     * Test SNP effect predictor: Test Splice sites
     */
    @Test
    public void test_12() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("splice_site.out"), null, true);
    }

    /**
     * Test SNP effect predictor: Test Splice sites (make sure they are only 2 bases long)
     */
    @Test
    public void test_12_2() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("splice_site_2.out"), null, true);
    }

    /**
     * Test SNP effect predictor: Test start codon gained
     */
    @Test
    public void test_19() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000439108";
        comp.snpEffect(path(trId + ".snps"), trId, true);
    }

    /**
     * Test SNP effect predictor: Test start codon gained (reverse strand)
     */
    @Test
    public void test_20() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000382673";
        comp.snpEffect(path(trId + ".snps"), trId, true);
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_21() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000250823";
        comp.snpEffect(path(trId + "_InDels.out"), trId, true);
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_21_2() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000250823";
        comp.snpEffect(path(trId + "_InDels_2.out"), trId, true);
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_21_3() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000250823";
        comp.setUseAaNoNum(true);
        comp.snpEffect(path(trId + "_InDels_3.out"), trId, true);
    }

    /**
     * Read file test: Should throw an exception (chromosome not found)
     */
    @Test
    public void test_22() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);

        VariantFileIterator snpFileIterator;
        snpFileIterator = new VariantTxtFileIterator(path("chr_not_found.out"), comp.getConfig().getGenome());
        snpFileIterator.setIgnoreChromosomeErrors(false);
        snpFileIterator.setCreateChromos(false);

        boolean trown = false;
        try {
            // Read all SNPs from file. Note: This should throw an exception "Chromosome not found"
            for (Variant variant : snpFileIterator) {
                Log.debug(variant);
            }
        } catch (RuntimeException e) {
            trown = true;
            String expectedMessage = "ERROR: Chromosome 'chrZ' not found! File '" + path("chr_not_found.out") + "', line 1";
            if (e.getMessage().equals(expectedMessage)) ; // OK
            else
                throw new RuntimeException("This is not the exception I was expecting!\n\tExpected message: '" + expectedMessage + "'\n\tMessage: '" + e.getMessage() + "'", e);
        }

        // If no exception => error
        if (!trown)
            throw new RuntimeException("This should have thown an exception 'Chromosome not found!' but it didn't");
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_23_MNP_on_exon_edge() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        String trId = "ENST00000250823";
        comp.setUseAaNoNum(true);
        comp.snpEffect(path(trId + "_mnp_out_of_exon.txt"), trId, true);
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_24_delete_exon_utr() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("delete_exon_utr.txt"), null, true);
    }

    @Test
    public void test_25_exon_bases() {
        Log.debug("Test");
        Config config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
        config.loadSnpEffectPredictor();

        String fastaFile = path("testCase.fa");
        String seq = GprSeq.fastaSimpleRead(fastaFile);

        // Test all bases in all exons
        int countOk = 0, countErr = 0;
        for (Gene gint : config.getGenome().getGenes()) {
            for (Transcript tr : gint) {
                if (verbose) Log.info("Transcript: " + tr.getId());
                List<Exon> exons = tr.sortedStrand();
                for (Exon exon : exons) {
                    for (int i = exon.getStart(); i <= exon.getEndClosed(); i++) {
                        String base = seq.substring(i, i + 1);
                        String exonBase = exon.basesAt(i - exon.getStart(), 1);

                        if (base.equalsIgnoreCase(exonBase)) {
                            countOk++;
                        } else {
                            countErr++;
                            String msg = "ERROR:\tPosition: " + i + "\tExpected: " + base + "\tGot: " + exonBase;
                            if (verbose) Log.debug(msg);
                            throw new RuntimeException(msg);
                        }
                    }
                }
            }
        }

        if (verbose) Log.info("Count OK: " + countOk + "\tCount Err: " + countErr);
    }

    /**
     * Test SNP effect predictor for a transcript (Insertions)
     */
    @Test
    public void test_26_chr15_78909452() {
        Log.debug("Test");
        String genomeName = "testHg3761Chr15";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("chr15_78909452.txt"), null, true);
    }

    /**
     * Splice site: Bug reported by Wang, Xusheng
     */
    @Test
    public void test_28_Splice_mm37_ENSMUSG00000005763() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        //---
        // Build snpEffect
        //---
        String gtfFile = path("ENSMUSG00000005763.gtf");
        String genome = "testMm37.61";

        Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
        SnpEffPredictorFactoryGtf22 fgtf22 = new SnpEffPredictorFactoryGtf22(config);
        fgtf22.setFileName(gtfFile);
        fgtf22.setReadSequences(false); // Don't read sequences
        SnpEffectPredictor snpEffectPredictor = fgtf22.create();
        config.setSnpEffectPredictor(snpEffectPredictor);

        // Set chromosome size (so that we don't get an exception)
        for (Chromosome chr : config.getGenome())
            chr.setEndClosed(1000000000);

        //---
        // Calculate effect
        //---
        CompareEffects comp = new CompareEffects(snpEffectPredictor, randSeed, verbose);
        comp.snpEffect(path("ENSMUSG00000005763.out"), null, true);
    }

    /**
     * Test effect when hits a gene, but not any transcript within a gene.
     * This is an extremely weird case, might be an annotation problem.
     */
    @Test
    public void test_29_Intergenic_in_Gene() {
        Log.debug("Test");
        String genomeName = "testHg3763Chr20";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("warren.eff.missing.chr20.txt"), null, true);
    }

    /**
     * Rare Amino acid
     */
    @Test
    public void test_30_RareAa() {
        Log.debug("Test");
        String genomeName = "testHg3765Chr22";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("rareAa.txt"), null, true);
    }

    /**
     * MT chromo
     * Effectusing an alternative codon table:
     * Codon change            : atA / atG
     * Standard codon table    : Ile / Met => NON_SYNONYMOUS
     * Mithocondria codon table: Met / Met => SYNONYMOUS
     */
    @Test
    public void test_31_CodonTable() {
        Log.debug("Test");
        String genomeName = "testHg3767ChrMt";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("mt.txt"), null, true);
    }

    /**
     * Start gained
     */
    @Test
    public void test_32_StartGained() {
        Log.debug("Test");
        String genomeName = "testHg3769Chr12";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("start_gained_test.txt"), null, true);
    }

    /**
     * Not start gained
     */
    @Test
    public void test_33_StartGained_NOT() {
        Log.debug("Test");
        String genomeName = "testHg3769Chr12";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffectNegate(path("start_gained_NOT_test.txt"), null, true);
    }

    /**
     * Start gained
     */
    @Test
    public void test_34_StartGained() {
        Log.debug("Test");
        String genomeName = "testHg3766Chr1";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("start_gained_test_2.txt"), null, true);
    }

    /**
     * Not start gained
     */
    @Test
    public void test_35_StartGained_NOT() {
        Log.debug("Test");
        String genomeName = "testHg3766Chr1";
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffectNegate(path("start_gained_NOT_test_2.txt"), null, true);
    }

    /**
     * Make sure all variant effects have appropriate impacts
     */
    @Test
    public void test_36_EffectImpact() {
        Log.debug("Test");
        Chromosome chr = new Chromosome(null, 0, 1, "1");
        Variant var = new Variant(chr, 1, "A", "C");
        var.setVariantType(VariantType.SNP);

        if (verbose) Log.info(var);
        for (EffectType eff : EffectType.values()) {
            VariantEffect varEff = new VariantEffect(var);
            varEff.setEffectType(eff);
            if (verbose) Log.info(var.isVariant() + "\t" + eff + "\t" + varEff.getEffectImpact());
        }
    }

    /**
     * Test
     */
    @Test
    public void test_38_NON_SYNONYMOUS_START() {
        Log.debug("Test");
        CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
        comp.snpEffect(path("nonSynStart.out"), "ENST00000250823", true);
    }

}
