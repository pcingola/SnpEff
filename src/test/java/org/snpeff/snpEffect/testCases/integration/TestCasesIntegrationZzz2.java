package org.snpeff.snpEffect.testCases.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;

/**
 * Test case
 */
public class TestCasesIntegrationZzz2 extends TestCasesIntegrationBase {

    /**
     * Check proteins using MANE genome with RefSeq IDs
     */
    @Test
    public void test_01_build_mane_refseq_check_proteins() {
        var genome = "test_GRCh38.mane.1.0.refseq.chr21";
        var sep = build(genome);
        // Check that all transcripts have protein IDs
        var count = 0;
        for(Gene g:sep.getGenome().getGenes()) {
            for(Transcript tr: g) {
                if( tr.isProteinCoding() ) {
                    Assertions.assertNotNull(tr.getProteinId());
                    count++;
                }
            }
        }
        Assertions.assertEquals(count, 213);
    }

    // /**
    //  * Rare amino acids when building with MANE genome with RefSeq IDs
    //  */
    // @Test
    // public void test_01_build_mane_refseq_rare_aa() {
        
    // }

}
