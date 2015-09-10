package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

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
	 * Upstream region is completely removed by a deletion.
	 * Bug triggers a null pointer: Fixed
	 */
	@Test
	public void test_apply_07_delete_upstream() {
		Gpr.debug("Test");
		Transcript trNew = appyTranscript("testHg3775Chr11", "ENST00000379829", "tests/test_apply_07_delete_upstream.vcf");

		// Check expected sequence
		Assert.assertEquals("ttttttggggctgctgagtgctgcctcctggccaccatggcatatgaccgctacgtggccatctgtgaccccttgcactacccagtcatcatgggccacatatcctgtgcccagctggcagctgcctcttggttctcagggttttcagtggccactgtgcaaaccacatggattttcagtttccctttttgtggccccaacagggtgaaccacttcttctgtgacagccctcctgttattgcactggtctgtgctgacacctctgtgtttgaactggaggctctgacagccactgtcctattcattctctttcctttcttgctgatcctgggatcctatgtccgcatcctctccactatcttcaggatgccgtcagctgaggggaaacatcaggcattctccacctgttccgcccacctcttggttgtctctctcttctatagcactgccatcctcacgtatttccgaccccaatccagtgcctcttctgagagcaagaagctgctgtcactctcttccacagtggtgactcccatgttgaaccccatcatctacagctcaaggaataaagaagtgaaggctgcactgaagcggcttatccacaggaccctgggctctcagaaactatga" //
				, trNew.cds());
	}

}
