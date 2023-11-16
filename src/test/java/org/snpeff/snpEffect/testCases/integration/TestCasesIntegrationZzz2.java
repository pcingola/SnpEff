package org.snpeff.snpEffect.testCases.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Test case
 */
public class TestCasesIntegrationZzz2 extends TestCasesIntegrationBase {

    // /**
    //  * Check proteins using MANE genome with RefSeq IDs
    //  */
    // @Test
    // public void test_01_build_mane_refseq_check_proteins() {
    //     var genome = "test_GRCh38.mane.1.0.refseq.chr21";
    //     var sep = build(genome);
    //     // Check that all transcripts have protein IDs
    //     var count = 0;
    //     for(Gene g:sep.getGenome().getGenes()) {
    //         for(Transcript tr: g) {
    //             if( tr.isProteinCoding() ) {
    //                 Assertions.assertNotNull(tr.getProteinId());
    //                 count++;
    //             }
    //         }
    //     }
    //     Assertions.assertEquals(count, 213);
    // }

    /**
     * Rare amino acids when building with MANE genome with RefSeq IDs
     */
    @Test
    public void test_01_build_mane_refseq_rare_aa() {
        verbose = true;
        var genome = "test_GRCh38.mane.1.0.refseq.chr19";
        var sep = build(genome);
        // Protein IDs
        Set<String> proteinIds = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for(Gene g:sep.getGenome().getGenes()) {
            for(Transcript tr: g) {
                if( tr.isProteinCoding() ) {
                    proteinIds.add(tr.getProteinId());
                    sb.append(tr.getProteinId() + "\n");
                }
            }
        }
        Gpr.toFile("proteins.txt", sb.toString());
        // Read fasta file
        sb = new StringBuilder();
        FastaFileIterator ffi = new FastaFileIterator(Gpr.HOME + "/snpEff/data/" + genome +"/protein.fa.gz");
        var count = 0;
        for(String seq : ffi) {
            var pid = ffi.getIdFromFastaHeader();
            if( proteinIds.contains(pid) ) {
                sb.append(">" + ffi.getHeader() + "\n" + seq + "\n");
                count++;
            } 
        }
        Gpr.toFile("protein.fa", sb.toString());
        System.err.println(count);
    }

}
