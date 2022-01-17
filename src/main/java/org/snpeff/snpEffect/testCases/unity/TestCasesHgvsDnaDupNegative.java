package org.snpeff.snpEffect.testCases.unity;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.HgvsDna;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for HGVS's 'dup' on the negative strand
 */
public class TestCasesHgvsDnaDupNegative extends TestCasesBase {

	public TestCasesHgvsDnaDupNegative() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		onlyMinusStrand = true;
		onlyPlusStrand = false;
		shiftHgvs = true;
	}

	@Test
	public void test_01() {
		Log.debug("Test");

		if (verbose) {
			Exon exFirst = transcript.sorted().get(0);
			String exFirstSeq = exFirst.isStrandPlus() ? exFirst.getSequence() : GprSeq.reverseWc(exFirst.getSequence());
			Log.debug(transcript + "\n\tSequence: " + exFirstSeq);
		}

		// Create variant
		Variant variant = new Variant(chromosome, 1001, "", "C", "");
		if (verbose) Log.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Log.debug("HGVS (DNA): '" + hgvsDna + "'");
		assertEquals("c.1dupG", hgvsDna);
	}

	@Test
	public void test_02() {
		Log.debug("Test");

		if (verbose) {
			Exon exFirst = transcript.sorted().get(0);
			String exFirstSeq = exFirst.isStrandPlus() ? exFirst.getSequence() : GprSeq.reverseWc(exFirst.getSequence());
			Log.debug(transcript + "\n\tSequence: " + exFirstSeq);
		}

		// Create variant
		Variant variant = new Variant(chromosome, 997, "", "G", "");
		if (verbose) Log.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Log.debug("HGVS (DNA): '" + hgvsDna + "'");
		assertEquals("c.5dupC", hgvsDna);
	}

	/**
	 * Dup on the reverse strand: More than one base
	 */
	@Test
	public void test_03() {
		Log.debug("Test");
		if (verbose) Log.debug(transcript);

		if (verbose) {
			Exon exFirst = transcript.sorted().get(0);
			String exFirstSeq = exFirst.isStrandPlus() ? exFirst.getSequence() : GprSeq.reverseWc(exFirst.getSequence());
			Log.debug(transcript + "\n\tSequence: " + exFirstSeq);
		}

		// Create variant
		Variant variant = new Variant(chromosome, 996, "", "CG", "");
		if (verbose) Log.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Log.debug("HGVS (DNA): '" + hgvsDna + "'");
		assertEquals("c.5_6dupCG", hgvsDna);
	}

	/**
	 * Dup on the reverse strand: More than one base
	 */
	@Test
	public void test_04() {
		Log.debug("Test");
		if (verbose) Log.debug(transcript);

		if (verbose) {
			Exon exFirst = transcript.sorted().get(0);
			String exFirstSeq = exFirst.isStrandPlus() ? exFirst.getSequence() : GprSeq.reverseWc(exFirst.getSequence());
			Log.debug(transcript + "\n\tSequence: " + exFirstSeq);
		}

		// Create variant
		Variant variant = new Variant(chromosome, 984, "", "CAT", "");
		if (verbose) Log.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Log.debug("HGVS (DNA): '" + hgvsDna + "'");
		assertEquals("c.16_18dupATG", hgvsDna);
	}

}
