package org.snpeff.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.RareAminoAcid;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Test case
 */
public class TestCasesIntegrationRareAa extends TestCasesIntegrationBase {

    /**
     * This is used to create the test case for one chromomsome out of a genome.
     * Extracts protein sequences only for this "genome version" (which is one chromosome in the genome)
     */
    void extractProteins(String genome, SnpEffectPredictor sep) {
        // Protein IDs
        Set<String> proteinIds = new HashSet<>();
        for(Gene g:sep.getGenome().getGenes()) {
            for(Transcript tr: g) {
                if( tr.isProteinCoding() && tr.hasProteinId() ) 
                    proteinIds.add(tr.getProteinId());
            }
        }
        // Read protein fasta file and extract the sequences to a new file 'protein.fa'
        StringBuilder sb = new StringBuilder();
        FastaFileIterator ffi = new FastaFileIterator(Gpr.HOME + "/snpEff/data/" + genome +"/protein.fa.gz");
        var count = 0;
        for(String seq : ffi) {
            var pid = ffi.getIdFromFastaHeader();
            if( proteinIds.contains(pid) ) {
                sb.append(">" + ffi.getHeader() + "\n" + seq + "\n");
                count++;
            } 
        }
        // Write protein sequences to file
        Gpr.toFile("protein.fa", sb.toString());
        System.err.println(count);
    }

    /**
     * Rare Amino acid
     */
    @Test
    public void test_01_RareAa() {
        Log.debug("Test");
        String genomeName = "testHg3765Chr22";
        CompareEffects comp = new CompareEffects(genomeName, verbose);
        comp.snpEffect(path("rareAa.txt"), null, true);
    }


    /**
     * Rare amino acids when building with MANE genome with RefSeq IDs
     */
    @Test
    public void test_02_build_mane_refseq_rare_aa() {
        var genome = "test_GRCh38.mane.1.0.refseq.chr19";
        var sep = build(genome);
        // Expected transcripts with rare amino acids
        var expectedTrIds = new HashSet<>();
        expectedTrIds.add("NM_002085.5");
        expectedTrIds.add("NM_182704.2");
        expectedTrIds.add("NM_003009.4");
        // Find all rare amino acid makers
        var trIds = new HashSet<>();
        for(Marker m: sep.getMarkers()) {
            if( m instanceof RareAminoAcid ) {
                var tr = m.getParent();
                trIds.add(tr.getId());
            }
        }
        // Check that we have all the expected transcripts
        Assertions.assertEquals(expectedTrIds, trIds);
    }

}
