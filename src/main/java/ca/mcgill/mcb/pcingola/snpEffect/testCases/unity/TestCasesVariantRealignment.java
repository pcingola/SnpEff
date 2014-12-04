package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.align.VariantRealign;
import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test cases for variant realignment
 */
public class TestCasesVariantRealignment extends TestCasesBase {

	public TestCasesVariantRealignment() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		minExons = 2;
		randSeed = 20141128;
		initRand();
	}

	//
	//	/**
	//	 * Shift by one position
	//	 */
	//	@Test
	//	public void test_01() {
	//		Gpr.debug("Test");
	//
	//		// Change exon's sequence
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 754, "", "T", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Shift variant
	//		if (verbose) Gpr.debug("Variant (before): " + variant);
	//		Variant variantShifted = variant.realignLeft();
	//		if (verbose) Gpr.debug("Variant (after): " + variantShifted);
	//
	//		// Check that shifted variant is oK
	//		Assert.assertFalse(variant == variantShifted);
	//		Assert.assertEquals(756, variantShifted.getStart());
	//		Assert.assertEquals("", variantShifted.getReference());
	//		Assert.assertEquals("T", variantShifted.getAlt());
	//	}
	//
	//	/**
	//	 * No shift
	//	 */
	//	@Test
	//	public void test_02() {
	//		Gpr.debug("Test");
	//
	//		// Change exon's sequence
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 754, "", "A", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Shift variant
	//		if (verbose) Gpr.debug("Variant (before): " + variant);
	//		Variant variantShifted = variant.realignLeft();
	//		if (verbose) Gpr.debug("Variant (after): " + variantShifted);
	//
	//		// Check that shifted variant is the same object
	//		Assert.assertTrue(variant == variantShifted);
	//	}
	//
	//	/**
	//	 * Shift by one position
	//	 */
	//	@Test
	//	public void test_03() {
	//		Gpr.debug("Test");
	//
	//		// Change exon's sequence
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 1025, "", "G", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Shift variant
	//		if (verbose) Gpr.debug("Variant (before): " + variant);
	//		Variant variantShifted = variant.realignLeft();
	//		if (verbose) Gpr.debug("Variant (after): " + variantShifted);
	//
	//		// Check that shifted variant is oK
	//		Assert.assertFalse(variant == variantShifted);
	//		Assert.assertEquals(1030, variantShifted.getStart());
	//		Assert.assertEquals("", variantShifted.getReference());
	//		Assert.assertEquals("G", variantShifted.getAlt());
	//	}
	//
	//	/**
	//	 * Shift by one position in an intron
	//	 */
	//	@Test
	//	public void test_04_Intron() {
	//		Gpr.debug("Test");
	//
	//		// Change exon's sequence
	//		if (verbose) Gpr.debug(transcript);
	//
	//		// Create variant
	//		Variant variant = new Variant(chromosome, 920, "", "C", "");
	//		if (verbose) Gpr.debug("Variant: " + variant);
	//
	//		// Shift variant
	//		if (verbose) Gpr.debug("Variant (before): " + variant);
	//		Variant variantShifted = variant.realignLeft();
	//		if (verbose) Gpr.debug("Variant (after): " + variantShifted);
	//
	//		// Check that shifted variant is oK
	//		Assert.assertFalse(variant == variantShifted);
	//		Assert.assertEquals(925, variantShifted.getStart());
	//	}

	/**
	 * Test case from Savant's poster 
	 * http://www.well.ox.ac.uk/savant
	 * (Márton Münz, Elise Ruark, Nazneen Rahman, Gerton Lunter)
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");
		String seqRef = "AAACTGTATTT";
		String seqAlt = "AAACTATTT";

		VariantRealign vr = new VariantRealign();
		vr.setSequenceRef(seqRef);
		vr.setSequenceAlt(seqAlt);
		vr.realignSeqs();
		if (verbose) Gpr.debug(vr);

		// Check resutls
		Assert.assertEquals("GT", vr.getRefRealign());
		Assert.assertEquals("", vr.getAltRealign());
	}

	@Test
	public void test_05_opposite() {
		Gpr.debug("Test");
		String seqRef = "AAACTATTT";
		String seqAlt = "AAACTGTATTT";

		VariantRealign vr = new VariantRealign();
		vr.setSequenceRef(seqRef);
		vr.setSequenceAlt(seqAlt);
		vr.realignSeqs();
		if (verbose) Gpr.debug(vr);

		// Check resutls
		Assert.assertEquals("", vr.getRefRealign());
		Assert.assertEquals("GT", vr.getAltRealign());
	}

	/**
	 * Same as test Savant's test case, but using variant and GenomicSequences
	 */
	@Test
	public void test_06() {
		Gpr.debug("Test");
		//		verbose = true;
		String chr = "1";
		String seqRef = "AAACTGTATTT";

		// Create genome & chromosome 
		Genome genome = new Genome("zzz");
		genome.getOrCreateChromosome(chr).setSequence(seqRef);

		// Create genomicSequences
		GenomicSequences gs = genome.getGenomicSequences();
		gs.addChromosomeSequence(chr, seqRef);
		gs.build();

		// Create variant
		Variant variant = new Variant(genome.getOrCreateChromosome("1"), 4, "TG", "");

		// Realign variant
		VariantRealign vr = new VariantRealign(gs, variant);
		vr.realign();
		if (verbose) Gpr.debug(vr);

		// Check results
		Assert.assertEquals("GT", vr.getRefRealign());
		Assert.assertEquals("", vr.getAltRealign());
	}
}
