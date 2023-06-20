package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test cases for HGVS notation
 */
public class TestCasesIntegrationHgvsUpDownStream extends TestCasesIntegrationBase {

    public TestCasesIntegrationHgvsUpDownStream() {
        super();
    }

    /**
     * Test for HGVS.C notation on upstream variants
     */
    @Test
    public void test_01_hgvs_upstream() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg19Chr2", path("hgvs_upstream.vcf"), null);

        for (VcfEntry ve : list) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (veff.getTranscriptId().equals("NM_000463.2")) {
                    if (verbose) {
                        System.out.println("\t" + veff);
                        System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
                    }

                    // Compare against expected result
                    String expectedHgvsC = ve.getInfo("HGVSC");
                    String actualHgvsC = veff.getHgvsC();
                    assertEquals(expectedHgvsC, actualHgvsC);
                }
            }
        }
    }

    /**
     * Test for HGVS.C notation on downstream variants
     */
    @Test
    public void test_02_hgvs_downstream() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg19Chr2", path("hgvs_downstream.vcf"), null);

        for (VcfEntry ve : list) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (veff.getTranscriptId().equals("NM_000463.2")) {
                    if (verbose) {
                        System.out.println("\t" + veff);
                        System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
                    }

                    // Compare against expected result
                    String expectedHgvsC = ve.getInfo("HGVSC");
                    String actualHgvsC = veff.getHgvsC();
                    assertEquals(expectedHgvsC, actualHgvsC);
                }
            }
        }
    }

    /**
     * Test that CSV summary does not throw any error
     */
    @Test
    public void test_03_hgvs_upstream_del() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg3765Chr22", path("hgvs_upstream_del.vcf"), null);

        for (VcfEntry ve : list) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (veff.getTranscriptId().equals("ENST00000404751")) {
                    if (verbose) {
                        System.out.println("\t" + veff);
                        System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
                    }

                    // Compare against expected result
                    String expectedHgvsC = ve.getInfo("HGVSC");
                    String actualHgvsC = veff.getHgvsC();
                    assertEquals(expectedHgvsC, actualHgvsC);
                }
            }
        }
    }

    /**
     * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
     */
    @Test
    public void test_04_hgvs_upstream_negative_strand() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg19Chr17", path("hgvs_upstream_negative_strand.vcf"), null);

        for (VcfEntry ve : list) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (veff.getTranscriptId().equals("NM_000199.3")) {
                    if (verbose) {
                        System.out.println("\t" + veff);
                        System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
                    }

                    // Compare against expected result
                    String expectedHgvsC = ve.getInfo("HGVSC");
                    String actualHgvsC = veff.getHgvsC();
                    assertEquals(expectedHgvsC, actualHgvsC);
                }
            }
        }
    }

    /**
     * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
     */
    @Test
    public void test_05_hgvs_downstream_negative_strand() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg19Chr17", path("hgvs_downstream_negative_strand.vcf"), null);

        for (VcfEntry ve : list) {
            if (verbose) Log.info(ve);

            for (VcfEffect veff : ve.getVcfEffects()) {
                if (veff.getTranscriptId().equals("NM_000199.3")) {
                    if (verbose) {
                        System.out.println("\t" + veff);
                        System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
                    }

                    // Compare against expected result
                    String expectedHgvsC = ve.getInfo("HGVSC");
                    String actualHgvsC = veff.getHgvsC();
                    assertEquals(expectedHgvsC, actualHgvsC);
                }
            }
        }
    }

    /**
     * Test HGVS upstream of a variant affecting a transcript on the negative strand
     * <p>
     * The result has annotations for a single variant at chr1:1230300 on 3 transcripts, broken out into separate lines here:
     * <p>
     * G|missense_variant|MODERATE|B3GALT6|B3GALT6|transcript|NM_080605.3|protein_coding|1/1|c.22T>G|p.Trp8Gly|52/2792|22/990|8/329||
     * G|upstream_gene_variant|MODIFIER|SDF4|SDF4|transcript|NM_016176.3|protein_coding||c.-3507A>C|||||233|
     * G|upstream_gene_variant|MODIFIER|SDF4|SDF4|transcript|NM_016547.2|protein_coding||c.-3507A>C|||||233|
     * <p>
     * For the second and third annotations on NM_016176.3 and NM_016547.2, the HGVS c. term is c.-3507A>C.
     * However, I believe the correct offset is c.-562A>C. Here's how I get -562 for NM_016176.3:
     * <p>
     * * NM_016176.3's CDS begins at base 330.  Base 329 is c.-1, 328 is c.-2, ... base 1 is c.-329.
     * Then, upstream of the transcription start,
     * * NM_016176.3's transcription start's genomic coord is 1232067.
     * g.1232067 is c.-329, g.1232068 is c.-330, ... g.1230300 is c.-562.
     * So if strand is '-' as for NM_016176.3, "genomicTxStart" being the rightmost tx coord:
     * cDotUpstream = -(cdsStart + variantPos - genomicTxStart)
     * <p>
     * It looks like you're using -(variantPos - genomicCdsStart): 1232300 - 1228793 = 3507.
     * I believe the method that stays in transcript space until extending beyond the transcript
     * is correct because of these statements on http://varnomen.hgvs.org/bg-material/numbering/ :
     * <p>
     * * nucleotides upstream (5') of the ATG-translation initiation
     * codon (start) are marked with a "-" (minus) and numbered c.-1,
     * c.-2, c.-3, etc. (i.e. going further upstream)
     * <p>
     * * Question: When the ATG translation initiation codon is in
     * exon 2, and we find a variant in exon 1, should we include
     * intron 1 (upstream of c.-14) in nucleotide
     * numbering? (Isabelle Touitou, Montpellier, France)
     * <p>
     * Answer: Nucleotides in introns 5' of the ATG translation
     * initiation codon (i.e. in the 5'UTR) are numbered as
     * introns in the protein coding sequence (see coding DNA
     * numbering). In your example, based on a coding DNA
     * reference sequence, the intron is present between
     * nucleotides c.-15 and c.-14. The nucleotides for this
     * intron are numbered as c.-15+1, c.-15+2, c.-15+3, ....,
     * c.-14-3, c.-14-2, c.-14-1. Consequently, regarding the
     * question, when a coding DNA reference sequence is used,
     * the intronic nucleotides are not counted.
     * <p>
     * And it seems that NCBI agrees -- the list of HGVS terms for rs794726955
     * https://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=794726955
     * at GRCh38 chr1:1232300 includes NM_016176.3:c.-562A>C and NM_016547.2:c.-562A>C.
     */
    @Test
    public void test_06_hgvs_upstream_negative_strand() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg38Chr1", path("hgvs_upstream_negative_strand_06.vcf"), null);
        checkHgvscForTr(list, "NM_016176.3");
    }

    @Test
    public void test_07_hgvs_downstream_negative_strand() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg38Chr1", path("hgvs_downstream_negative_strand_07.vcf"), null);
        checkHgvscForTr(list, "NM_016176.3");
    }

    @Test
    public void test_08_hgvs_downstream_negative_strand() {
        Log.debug("Test");
        List<VcfEntry> list = snpEffect("testHg38Chr1", path("hgvs_downstream_negative_strand_08.vcf"), null);
        checkHgvscForTr(list, "NM_002524.4");
    }

}
