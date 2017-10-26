package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/hgvsUpDownStream/";
	}

	//	/**
	//	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	//	 */
	//	@Test
	//	public void test_01_VcfRawAlt() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//
	//		// Create command
	//		String args[] = { "testHg3775Chr1", "tests/test_ann_integration_01.vcf" };
	//
	//		SnpEff cmd = new SnpEff(args);
	//		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
	//		cmdEff.setVerbose(verbose);
	//		cmdEff.setSupressOutput(!verbose);
	//
	//		// Run command
	//		List<VcfEntry> list = cmdEff.run(true);
	//		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);
	//
	//		// Expected results
	//		Set<String> allelesExpected = new HashSet<>();
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
	//		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");
	//
	//		// Find AA change for a genotype
	//		Set<String> allelesReal = new HashSet<>();
	//		for (VcfEntry vcfEntry : list) {
	//			if (debug) System.err.println(vcfEntry);
	//
	//			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
	//				String allele = eff.getAllele();
	//				if (verbose) System.err.println("\t" + eff + "\n\t\tAllele: " + allele);
	//
	//				Assert.assertTrue("Unexpected allele '" + allele + "'", allelesExpected.contains(allele));
	//				allelesReal.add(allele);
	//			}
	//		}
	//
	//		Assert.assertEquals(allelesExpected, allelesReal);
	//	}

	/**
	 * Test HGVS upstream of a variant affecting a transcript on the negative strand
	 * 
	 * The result has annotations for a single variant at chr1:1230300 on 3 transcripts, broken out into separate lines here:
	 *
	 * G|missense_variant|MODERATE|B3GALT6|B3GALT6|transcript|NM_080605.3|protein_coding|1/1|c.22T>G|p.Trp8Gly|52/2792|22/990|8/329||
	 * G|upstream_gene_variant|MODIFIER|SDF4|SDF4|transcript|NM_016176.3|protein_coding||c.-3507A>C|||||233|
	 * G|upstream_gene_variant|MODIFIER|SDF4|SDF4|transcript|NM_016547.2|protein_coding||c.-3507A>C|||||233|
	 *
	 * For the second and third annotations on NM_016176.3 and NM_016547.2, the HGVS c. term is c.-3507A>C.  However, I believe the correct offset is c.-562A>C.  Here's how I get -562 for NM_016176.3:
	 *
	 * * NM_016176.3's CDS begins at base 330.  Base 329 is c.-1, 328 is c.-2, ... base 1 is c.-329.
	 *   Then, upstream of the transcription start,
	 * * NM_016176.3's transcription start's genomic coord is 1232067.
	 *   g.1232067 is c.-329, g.1232068 is c.-330, ... g.1230300 is c.-562.
	 *   So if strand is '-' as for NM_016176.3, "genomicTxStart" being the rightmost tx coord:
	 *     cDotUpstream = -(cdsStart + variantPos - genomicTxStart)
	 *
	 * It looks like you're using -(variantPos - genomicCdsStart): 1232300 - 1228793 = 3507.  I believe the method that stays in transcript space until extending beyond the transcript is correct because of these statements on http://varnomen.hgvs.org/bg-material/numbering/ :
	 *
	 *     * nucleotides upstream (5') of the ATG-translation initiation
	 *       codon (start) are marked with a "-" (minus) and numbered c.-1,
	 *       c.-2, c.-3, etc. (i.e. going further upstream)
	 *
	 *     * Question: When the ATG translation initiation codon is in
	 *       exon 2, and we find a variant in exon 1, should we include
	 *       intron 1 (upstream of c.-14) in nucleotide
	 *       numbering? (Isabelle Touitou, Montpellier, France)
	 *
	 *       Answer: Nucleotides in introns 5' of the ATG translation
	 *       initiation codon (i.e. in the 5'UTR) are numbered as
	 *       introns in the protein coding sequence (see coding DNA
	 *       numbering). In your example, based on a coding DNA
	 *       reference sequence, the intron is present between
	 *       nucleotides c.-15 and c.-14. The nucleotides for this
	 *       intron are numbered as c.-15+1, c.-15+2, c.-15+3, ....,
	 *       c.-14-3, c.-14-2, c.-14-1. Consequently, regarding the
	 *       question, when a coding DNA reference sequence is used,
	 *       the intronic nucleotides are not counted.
	 *
	 * And it seems that NCBI agrees -- the list of HGVS terms for rs794726955
	 * https://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=794726955
	 * at GRCh38 chr1:1232300 includes NM_016176.3:c.-562A>C and NM_016547.2:c.-562A>C.
	 *
	 */
	@Test
	public void test_06_hgvs_upstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg38Chr1", testsDir + "hgvs_upstream_negative_strand_05.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getTranscriptId().equals("NM_000199.3")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}
}
