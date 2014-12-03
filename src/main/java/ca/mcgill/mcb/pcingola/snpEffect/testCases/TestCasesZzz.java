package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	@Override
	protected void init() {
		super.init();

		minExons = 1;

		randSeed = 20141128;
		initRand();
	}

	//	public void test_hgvs_walk_and_roll_2() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'tests/hgvs_jeremy_1.vcf'");
	//	}
	//
	//	public void test_hgvs_walk_and_roll_1() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'tests/hgvs_walk_and_roll.1.vcf'");
	//	}
	//
	//	public void test_hgvs_walk_and_roll_3() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'tests/hgvs_savant.vcf'");
	//	}
	//
	//	public void test_hgvs_md() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'hgvs_md.chr1.vcf'");
	//	}
	//
	//	public void test_hgvs_md_2() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'hgvs_md.chr13.vcf'");
	//	}
	//
	//	public void test_hgvs_md_3() {
	//		throw new RuntimeException("ADD TEST CASE FOR 'hgvs_md.chr17.vcf'");
	//	}

	//	/**
	//	 * Using non-standard splice size (15 instead of 2)
	//	 * may cause some HGVS annotations issues
	//	 */
	//	@Test
	//	public void test_zzz() {
	//		Gpr.debug("Test");
	//
	//		verbose = true;
	//		HgvsDna.debug = true;
	//
	//		String genome = "testHg19Chr17";
	//		String vcf = "tests/hgvs_dup.vcf";
	//
	//		// Create SnpEff
	//		String args[] = { genome, vcf };
	//		SnpEffCmdEff snpeff = new SnpEffCmdEff();
	//		snpeff.parseArgs(args);
	//		snpeff.setDebug(debug);
	//		snpeff.setVerbose(verbose);
	//		snpeff.setSupressOutput(!verbose);
	//
	//		// The problem appears when splice site is large (in this example)
	//		snpeff.setUpDownStreamLength(0);
	//
	//		// Run & get result (single line)
	//		List<VcfEntry> results = snpeff.run(true);
	//
	//		// Make sure entries are annotated as expected
	//		for (VcfEntry ve : results) {
	//			// Extract expected HGVS values
	//			String hgvsCexp = ve.getInfo("HGVS_C") != null ? ve.getInfo("HGVS_C") : "";
	//			String trIdC = Hgvs.parseTranscript(hgvsCexp);
	//			hgvsCexp = Hgvs.removeTranscript(hgvsCexp);
	//
	//			String hgvsPexp = ve.getInfo("HGVS_P") != null ? ve.getInfo("HGVS_P") : "";
	//			String trIdP = Hgvs.parseTranscript(hgvsPexp);
	//			hgvsPexp = Hgvs.removeTranscript(hgvsPexp);
	//
	//			if (verbose) {
	//				System.out.println(ve);
	//				if (trIdC != null) System.out.println("\tExpected HGVS_C: " + trIdC + ":" + hgvsCexp);
	//				if (trIdP != null) System.out.println("\tExpected HGVS_P: " + trIdP + ":" + hgvsPexp + "\n");
	//			}
	//
	//			// Check all effects
	//			for (VcfEffect veff : ve.parseEffects()) {
	//				// Parse calculated HGVS values
	//				String trId = veff.getTranscriptId();
	//				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
	//				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";
	//				if (verbose) System.out.println("\t" + veff //
	//						+ "\n\t\tEFF    : " + veff.getEffectsStr() //
	//						+ "\n\t\tHGVS_C : " + hgvsCactual //
	//						+ "\n\t\tHGVS_P : " + hgvsPactual //
	//						+ "\n");
	//
	//				// Compare results
	//				if (trId != null && trId.equals(trIdC)) Assert.assertEquals(hgvsCexp, hgvsCactual);
	//				if (trId != null && trId.equals(trIdP)) Assert.assertEquals(hgvsPexp, hgvsPactual);
	//			}
	//		}
	//	}

	/**
	 * Test markerSeq.getSequence(marker) method
	 * Compare to trivial implementation
	 */
	@Test
	public void test_zzz() {
		Gpr.debug("Test");

		int maxSize = 20; // Max subsequence size to test

		//		verbose = true;
		//		HgvsDna.debug = true;

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_dup.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// The problem appears when splice site is large (in this example)
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		snpeff.load();
		Config config = snpeff.getConfig();
		for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes())
			for (Transcript tr : g) {

				for (Exon ex : tr) {

					String sequence = ex.getSequence();
					if (ex.isStrandMinus()) sequence = GprSeq.reverseWc(sequence);
					if (verbose) Gpr.debug("Transcript: " + tr.getId() + "\tExon: " + ex.getId() + ", strand: " + (ex.isStrandPlus() ? "+" : "-") + "\tSequence: " + sequence);

					for (int i = 0, pos = ex.getStart(); pos <= ex.getEnd(); i++, pos++) {
						// Random size
						int size = rand.nextInt(maxSize);
						if (size > (ex.getEnd() - pos)) size = 0;

						// Get subsequence
						String seqExpected = sequence.substring(i, i + size + 1);

						// Retrieve same sequence using a marker query
						Marker m = new Marker(ex.getChromosome(), pos, pos + size, false, "");
						String seq = ex.getSequence(m);

						// Check that sequences match
						if (!seq.equalsIgnoreCase(seqExpected)) {
							String msg = "Expecting '" + seqExpected + "', got '" + seq + "'";
							System.err.println("ERROR:\t" + msg);
							throw new RuntimeException(msg);
						}
					}
				}
			}
	}
}
