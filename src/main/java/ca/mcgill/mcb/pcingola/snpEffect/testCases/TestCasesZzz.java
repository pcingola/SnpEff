package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Hgvs;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	public void compareHgvs(String genome, String vcfFileName, boolean compareProt) {
		// Create SnpEff
		String args[] = { genome, vcfFileName };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setUpDownStreamLength(0);
		snpeff.setShiftHgvs(shiftHgvs);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);

		// Make sure entries are annotated as expected
		int countOkC = 0, countOkP = 0;
		for (VcfEntry ve : results) {
			// Extract expected HGVS values
			String hgvsCexp = ve.getInfo("HGVS_C") != null ? ve.getInfo("HGVS_C") : "";
			String trIdC = Hgvs.parseTranscript(hgvsCexp);
			hgvsCexp = Hgvs.removeTranscript(hgvsCexp);

			String hgvsPexp = "";
			String trIdP = "";
			if (compareProt) {
				hgvsPexp = ve.getInfo("HGVS_P") != null ? ve.getInfo("HGVS_P") : "";
				hgvsPexp = Hgvs.removeTranscript(hgvsPexp);
				trIdP = Hgvs.parseTranscript(hgvsPexp);
			}

			if (verbose) {
				System.out.println(ve);
				if (trIdC != null) System.out.println("\tExpected HGVS_C: " + trIdC + ":" + hgvsCexp);
				if (trIdP != null) System.out.println("\tExpected HGVS_P: " + trIdP + ":" + hgvsPexp + "\n");
			}

			// Check all effects
			boolean okC = false, okP = false;
			for (VcfEffect veff : ve.parseEffects()) {
				// Parse calculated HGVS values
				String trId = veff.getTranscriptId();
				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";
				if (verbose) System.out.println("\t" + veff //
						+ "\n\t\tEFF    : " + veff.getEffectsStr() //
						+ "\n\t\tHGVS_C : " + hgvsCactual + "\t\tExpected: " + hgvsCexp //
						+ (compareProt ? "\n\t\tHGVS_P : " + hgvsPactual + "\t\tExpected: " + hgvsPexp : "") //
						+ "\n");

				// Compare results
				if (trId != null && trId.equals(trIdC)) {
					Assert.assertEquals(hgvsCexp, hgvsCactual);
					okC = true;
					countOkC++;
				}

				if (compareProt && trId != null && trId.equals(trIdP)) {
					Assert.assertEquals(hgvsPexp, hgvsPactual);
					okP = true;
					countOkP++;
				}
			}

			Assert.assertTrue("HGVS (DNA) not found: '" + hgvsCexp + "'", okC);
			if (!hgvsPexp.isEmpty()) Assert.assertTrue("HGVS (Protein) not found: '" + hgvsPexp + "'", okP);
		}

		System.out.println("Count OKs:\tHGVS (DNA): " + countOkC + "\tHGVS (Protein): " + countOkP);
	}

	@Override
	protected void init() {
		super.init();
		shiftHgvs = true;
	}

	//	/**
	//	 * Test some 'dup' form chr17
	//	 */
	//	@Test
	//	public void test_hgvs_dup() {
	//		Gpr.debug("Test");
	//
	//		String genome = "testHg19Chr17";
	//		String vcf = "tests/hgvs_dup.vcf";
	//
	//		compareHgvs(genome, vcf);
	//	}

	//	@Test
	//	public void test_hgvs_md() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//		String genome = "testHg19Chr1";
	//		String vcf = "tests/hgvs_md.chr1.vcf";
	//		compareHgvs(genome, vcf, false);
	//	}

	//	@Test
	//	public void test_hgvs_md_2() {
	//		Gpr.debug("Test");
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/hgvs_md.chr13.vcf";
	//		compareHgvs(genome, vcf);
	//	}
	//
	//	@Test
	//	public void test_hgvs_md_3() {
	//		Gpr.debug("Test");
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/hgvs_md.chr17.vcf";
	//		compareHgvs(genome, vcf);
	//	}
	//
	//	@Test
	//	public void test_hgvs_walk_and_roll_1() {
	//		Gpr.debug("Test");
	//
	//		String genome = "testHg19Chr1";
	//		String vcf = "tests/hgvs_jeremy_1.vcf";
	//
	//		compareHgvs(genome, vcf);
	//	}

	//	@Test
	//	public void test_hgvs_walk_and_roll_2() {
	//		Gpr.debug("Test");
	//		!!!!!!!!! BROKEN TEST CASE (original data from another transcript version ????????????
	//		verbose = true;
	//		String genome = "testHg19Chr17";
	//		String vcf = "tests/hgvs_walk_and_roll.1.vcf";
	//
	//		compareHgvs(genome, vcf, true);
	//	}

	//	@Test
	//	public void test_hgvs_walk_and_roll_3() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/hgvs_savant.vcf";
	//		compareHgvs(genome, vcf, true);
	//	}

	//	@Test
	//	public void test_zzz() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//		HgvsDna.debug = true;
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/zzz.vcf";
	//		compareHgvs(genome, vcf, true);
	//	}

	/**
	 * Test case from http://www.hgvs.org/mutnomen/recs-DNA.html
	 * 		g.7_8dup (or g.7_8dupTG, not g.5_6dup, not g.8_9insTG) denotes a TG duplication
	 * 		in the TG-tandem repeat sequence changing ACTTTGTGCC to ACTTTGTGTGCC
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");

		String prepend = "ACTTTGTGCC";

		verbose = true;

		GenomicSequences gs = genome.getGenomicSequences();
		Exon ex = transcript.sorted().get(0);
		String seq = gs.getSequence(ex);
		System.out.println("Seq (before): " + seq //
				+ "\nExon seq    : " + ex.getSequence() //
		);

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

		seq = gs.getSequence(ex);
		System.out.println("Seq (after) : " + seq //
				+ "\nExon seq    : " + ex.getSequence() //
		);

		// Create variant
		Variant variant = new Variant(chromosome, 888, "", "TG", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.7_8dupTG", hgvsDna);
	}

}
