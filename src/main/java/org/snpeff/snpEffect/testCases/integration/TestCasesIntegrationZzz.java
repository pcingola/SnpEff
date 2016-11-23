package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	public TestCasesIntegrationZzz() {
		super();
	}

	@Test
	public void testCase_04_CircularGenome() {
		Gpr.debug("Test");

		verbose = true;

		String prot = "MTNNIVIAGRLVADAELFFTNNGSAICNFTLANNKRYKDIEKSTFIEASIFGNYAESMNK" //
				+ "YLKKGVSIDVIGELVQESWSKDGKIYYKHKIKVKEIDFRTPKDNISEANFENEDTPSNHL" //
				+ "LYLVEDNMRAVTIPIIISEQTPNIAKFSNVISKECKLSLAICSMVLLLSSIIFNHIQPSY" //
				+ "SKSSIQIFVKTTPNKNAITYIARNPTNSSIINNSLLVLICKLCILWQFIIYYYIHSL*" //
		;

		String cds = "ATGACAAATAATATAGTAATTGCAGGAAGATTGGTGGCAGACGCTGAACTATTTTTTACA" //
				+ "AATAATGGCTCTGCTATTTGTAATTTTACTTTGGCGAATAATAAAAGATACAAAGACATA" //
				+ "GAAAAAAGCACTTTTATAGAAGCTAGTATTTTTGGCAACTATGCAGAATCTATGAATAAG" //
				+ "TATCTAAAAAAAGGCGTATCAATTGATGTAATAGGAGAGCTGGTTCAAGAAAGCTGGAGC" //
				+ "AAAGATGGAAAAATATATTATAAACATAAAATCAAAGTCAAAGAGATTGATTTTAGAACA" //
				+ "CCAAAAGATAATATTTCAGAAGCAAACTTTGAAAATGAAGATACACCCTCAAATCATCTG" //
				+ "CTTTATCTGGTGGAAGACAATATGAGAGCTGTAACTATTCCTATTATCATAAGTGAGCAA" //
				+ "ACGCCAAATATAGCAAAATTCTCAAATGTCATTTCTAAAGAATGCAAACTTTCTCTTGCT" //
				+ "ATTTGCTCTATGGTTTTATTACTTTCTTCTATCATTTTCAATCATATTCAACCAAGTTAT" //
				+ "TCAAAAAGCTCAATCCAAATTTTTGTAAAAACAACACCTAATAAGAATGCAATAACGTAC" //
				+ "ATTGCAAGAAATCCTACTAACTCGTCCATAATCAATAATTCCTTATTAGTCTTAATTTGT" //
				+ "AAGCTCTGTATTTTATGGCAATTTATTATTTATTATTATATCCATTCTCTATGA" //
		;

		// Create database & build interval forest
		String genomeName = "test_Campylobacter_fetus_subsp_venerealis_nctc_10354";
		SnpEffectPredictor sep = build(genomeName);
		Gene g = sep.getGene("CFV354_1968");
		Transcript tr = g.subIntervals().iterator().next();
		Assert.assertEquals("Protein sequence differs", prot, tr.protein());
		Assert.assertEquals("CDS sequence differs", cds, tr.cds().toUpperCase());
	}

}
