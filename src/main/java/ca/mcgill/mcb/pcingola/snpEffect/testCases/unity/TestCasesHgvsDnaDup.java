package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 */
public class TestCasesHgvsDnaDup extends TestCasesBase {

	public TestCasesHgvsDnaDup() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 881, "", "T", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.1dupT", hgvsDna);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 883, "", "A", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.3dupA", hgvsDna);
	}

	/**
	 * Test case from http://www.hgvs.org/mutnomen/recs-DNA.html
	 * 		g.5dupT (or g.5dup, not g.5_6insT) denotes a duplication ("insertion") of the T nucleotide
	 *      at position 5 in the genomic reference sequence changing ACTCTGTGCC to ACTCTTGTGCC
	 */
	@Test
	public void test_03() {
		Gpr.debug("Test");
		String prepend = "ACTCTGTGCC";

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 885, "", "T", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.5dupT", hgvsDna);
	}

	/**
	 * Test case from http://www.hgvs.org/mutnomen/recs-DNA.html
	 * 		g.7dupT (or g.7dup, not g.5dupT, not g.7_8insT) denotes a duplication ("insertion") of
	 * 		the T nucleotide at position 7 in the genomic reference sequence changing AGACTTTGTGCC to AGACTTTTGTGCC
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
		String prepend = "AGACTTTGTGCC";

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 887, "", "T", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.7dupT", hgvsDna);
	}

	/**
	 * Test case from http://www.hgvs.org/mutnomen/recs-DNA.html
	 * 		g.7_8dup (or g.7_8dupTG, not g.5_6dup, not g.8_9insTG) denotes a TG duplication
	 * 		in the TG-tandem repeat sequence changing ACTTTGTGCC to ACTTTGTGTGCC
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");
		String prepend = "ACTTTGTGCC";

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

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

	@Test
	public void test_06() {
		Gpr.debug("Test");
		String prepend = "ACTTTGTGCC";

		prependSequenceToFirstExon(prepend);
		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 888, "", "GTG", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.6_8dupGTG", hgvsDna);
	}

	/**
	 * Duplication with variant shifted towards 3-prime
	 */
	@Test
	public void test_07() {
		Gpr.debug("Test");

		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 882, "", "A", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.3dupA", hgvsDna);
	}

	/**
	 * Duplication with variant shifted towards 3-prime
	 */
	@Test
	public void test_08() {
		Gpr.debug("Test");

		if (verbose) Gpr.debug(transcript);

		// Create variant
		Variant variant = new Variant(chromosome, 884, "", "C", "");
		if (verbose) Gpr.debug("Variant: " + variant);

		// Analyze variant
		VariantEffects effs = snpEffectPredictor.variantEffect(variant);

		// Calculate HGVS
		VariantEffect eff = effs.get();
		HgvsDna hgvsc = new HgvsDna(eff);
		String hgvsDna = hgvsc.toString();

		// Check result
		if (verbose) Gpr.debug("HGVS (DNA): '" + hgvsDna + "'");
		Assert.assertEquals("c.7dupC", hgvsDna);
	}

}
