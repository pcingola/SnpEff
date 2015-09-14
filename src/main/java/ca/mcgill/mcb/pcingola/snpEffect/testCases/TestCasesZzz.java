package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Apply a variant to a transcript
	 */
	public Transcript appyTranscript(String genome, String trId, String vcfFileName) {
		// Load database
		SnpEffectPredictor sep = loadSnpEffectPredictorAnd(genome, false);

		int len = Integer.MAX_VALUE;
		for (Gene g : sep.getGenome().getGenes()) {
			for (Transcript tr : g) {
				if (tr.isProteinCoding() //
						&& tr.isStrandPlus() //
						&& !tr.hasError() //
						&& (tr.subintervals().size() == 1) //
						&& (tr.cds().length() < len) //
				) {
					len = tr.cds().length();
					Gpr.debug(tr);
				}
			}
		}
		// Find transcript
		Transcript tr = sep.getGenome().getGenes().findTranscript(trId);
		if (tr == null) throw new RuntimeException("Could not find transcript ID '" + trId + "'");

		// Apply first variant
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			for (Variant var : ve.variants()) {
				Transcript trNew = tr.apply(var);
				if (debug) Gpr.debug(trNew);
				return trNew;
			}
		}

		throw new RuntimeException("Could not apply any variant!");

	}

	/**
	 * Mixed variants
	 */
	@Test
	public void test_apply_11_mixed_variant() {
		Gpr.debug("Test");
		Transcript trNew = appyTranscript("testHg19Chr22", "NM_005318.3", "tests/test_apply_11_mixed_variant.vcf");

		// Check expected sequence
		Assert.assertEquals(
				"ggaagaaacacagatggcggcggcgcagcgccattccgggccgggagcaggcagccagcagccctgtcctcaccgcggtccgcccgccgccgctaaatacccggatgcgccgcccaagcgccagacgcggagctgggaaaagggaggcagaggaggcggaggcagaggcagaggcagaggcagagcccgagcccggtgccgagaccaagcgacagaccggcggggctgggcctcgcaaagccggctcggcgagctctcccgacacccgagccggggaggaaaagcagcgactcctcgctcgcatccccgggagccgcactccagactggcccggtagtcaggggctcaggagcagatcccgaggcaggctttgctcagcctccgacgagggctggccctttggaaggcgccttcaacagccggaccagacaggccaccatgaccgagaattccacgtccgcccctgcggccaagcccaagcgggccaaggcctccaagaagtccacagaccaccccaagtattcagacatgatcgtggctgccatccaggccgagaagaaccgcgctggctcctcgcgccagtccattcagaagtatatcaagagccactacaaggtgggtgagaacgctgactcgcagatcaagttgtccatcaagcgcctggtcaccaccggtgtcctcaagcagaccaaaggggtgggggcctcggggtccttccggctagccaagagcgacgaacccaagaagtcagtggccttcaagaagaccaagaaggaaatcaagaaggtagccacgccaaagaaggcatccaagcccaagaaggctgcctccaaagccccaaccaagaaacccaaagccaccccggtcaagaaggccaagaagaagctggctgccacgcccaagaaagccaaaaaacccaagactgtcaaagccaagccggtcaaggcatccaagcccaaaaaggccaaaccagtgaaacccaaagcaaagtccagtgccaagagggccggcaagaagaagtgacaatgaagtcttttcttgcggacactccctcctgtctcctattttctgtaaataattttctccttttttctctcttgatgctcaccaccaccttttgcccccttctgttctgactttataagagacaggatttggattcttcagaaattacagaataattcatttttccttaaccagttgtgcaaggacagcaacaaccaatctaatgatgagaatgtacttatattttgttttgctattaacctacttacggggttagggatttgcggggggggcttgtgtgttttgttggcttgtttgccatgaaggtagatgtgggtggggagaagacacaaggcagtttgttctggctagatgagagggaacccaggaattgtgaggttagcaggaatatctttagggtgagtgagttttctttgagttgggcacccgttgtgagagtttcagaacctttggccagcaggagagaggtggtagggagcagccagccggcaaaggaaggagggggaaaaaaaccgccaccgggctgacttccacctcccagtggtgagcagtgggggcccaaacccagtttccttctcatttttgttagtttgcgctttcggcctccctattttcttagggaaggggagtggggtccaagtgacagctggatgggagaagccatagtttctcccagtcagctaggatgtagccattgggggatctttgtggcttcagcaaattctcttgttaaaccggagtgaaaacttcaggggaagggtggggagtcagccaagtgcctcagtgtgccctgttgaaacttaggtttttccacgcaatcgatggattgtgtcctaggaagacttttcttttcctctggatttttgttcctcctgtacaagaggtgtctttgcttggtttggtggggctgcggccacttaaaacctcccgatctctttttgagtcctttattataagtagttgtagctgcgggagggggagggggagtgggcgggcagtggatagtaagacttactgcagtcgatttgggatttgctaagtagttttacagagctagatctgtgtgcatgtgtgtgtttgtgtatatatacatatctagggctagtacttagtttcacacccgggagctgggagaaaaaacctgtacagttgtctttctcttatttttaataaaatagaaaaatcgcgcacttgcgcgtcccccccccacccccttttttaaacaagtgttacttgtgccgggaaaattttgctgtctttgtaattttaaaactttaaaataaattggaaaagggagaaacgcg" //
				, trNew.mRna());

	}

}
