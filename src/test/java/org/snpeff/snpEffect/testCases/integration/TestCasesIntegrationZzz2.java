package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Log;

/**
 * Test case
 */
public class TestCasesIntegrationZzz2 extends TestCasesIntegrationBase {

    /**
     * Check proteins using MANE genome with RefSeq IDs
     */
    @Test
    public void test_01_build_mane_refseq_check_proteins() {
        verbose = true;
        var genome = "test_GRCh38.mane.1.0.refseq.chr21";
        var sep = build(genome);
        for(Gene g:sep.getGenome().getGenes()) {
            for(Transcript tr: g) {
                Log.debug(tr.getId() +"\t"+tr.getProteinId());
            }
        }
    }

    // /**
    //  * Rare amino acids when building with MANE genome with RefSeq IDs
    //  */
    // @Test
    // public void test_01_build_mane_refseq_rare_aa() {
        
    // }

}
