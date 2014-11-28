package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
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
	//	public void test_zzz() {
	//		Gpr.debug("Test");
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

	void prependSequenceToFirstExon(String prepend) {
		Exon firstEx = transcript.sortedStrand().get(0);
		String seq = firstEx.getSequence();
		firstEx.setSequence(prepend + seq);
	}

	//	public void test_01() {
	//		Gpr.debug("Test");
	//
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 881, "", "T", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Analyze variant
	//		VariantEffects effs = snpEffectPredictor.variantEffect(variant);
	//
	//		// Calculate HGVS
	//		VariantEffect eff = effs.get();
	//		HgvsDna hgsvDna = new HgvsDna(eff);
	//		String hgvsDna = hgsvDna.toString();
	//
	//		// Check result
	//		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
	//		Assert.assertEquals("c.1dupT", hgvsDna);
	//	}
	//
	//	public void test_02() {
	//		Gpr.debug("Test");
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 883, "", "A", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Analyze variant
	//		VariantEffects effs = snpEffectPredictor.variantEffect(variant);
	//
	//		// Calculate HGVS
	//		VariantEffect eff = effs.get();
	//		HgvsDna hgsvDna = new HgvsDna(eff);
	//		String hgvsDna = hgsvDna.toString();
	//
	//		// Check result
	//		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
	//		Assert.assertEquals("c.3dupA", hgvsDna);
	//	}

	public void test_03() {
		Gpr.debug("Test");
		String prepend = "ACTCTGTGCC";

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 883, "", "TAA", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgsvDna = new HgvsDna(eff);
		String hgvsDna = hgsvDna.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.3dupA", hgvsDna);
	}

}
