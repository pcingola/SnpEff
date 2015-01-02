package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Splice sites variants
 *
 *
 * The sample transcript used is:
 *Transcript:1:751-1139, strand: +, id:transcript_0, Protein
 *		Exons:
 *		1:751-810 'exon_0_0', rank: 1, frame: ., sequence: cgattgacctacatagtaatgagttttgttggtccgtaagacttcgcccaaaaccgcgca
 *		1:1013-1139 'exon_0_1', rank: 2, frame: ., sequence: cttcgactactcgggggtctaagcacgttttctgcagggaaagtaatatatgcttgtgcgcaaccatggtaacagggattcacggccccgttaatggtatgacctaagccccatacgagtcatccaa
 *		CDS     :	cgattgacctacatagtaatgagttttgttggtccgtaagacttcgcccaaaaccgcgcacttcgactactcgggggtctaagcacgttttctgcagggaaagtaatatatgcttgtgcgcaaccatggtaacagggattcacggccccgttaatggtatgacctaagccccatacgagtcatccaa
 *		Protein :	RLTYIVMSFVGP*DFAQNRALRLLGGLSTFSAGKVIYACAQPW*QGFTAPLMV*PKPHTSHP?
 *
 * @author pcingola
 */
public class TestCasesEffectCollapse extends TestCasesBase {

	public TestCasesEffectCollapse() {
		super();
	}

	void check(int pos, String effStrExpected) {
		if (verbose) Gpr.debug("Transcript:" + transcript);

		// Create a variant that hits splice_region and creates a non_syn
		Variant variant = new Variant(chromosome, pos, "A", "T");

		// Calculate variant
		VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

		// Check that there is only one effect
		if (verbose) {
			System.err.println("Variant: " + variant);
			System.err.println("Effects: " + veffs.size());
			for (VariantEffect veff : veffs)
				System.err.println("\tEff: '" + veff.effect(false, false, false, false) + "'");
		}

		// Check
		Assert.assertEquals(1, veffs.size());

		String effStr = veffs.get(0).effect(false, false, false, false);
		Assert.assertEquals(effStrExpected, effStr);
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20141205;
		minExons = 2;
		spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
		spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
		spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		check(809, "NON_SYNONYMOUS_CODING&SPLICE_SITE_REGION");
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		check(811, "SPLICE_SITE_DONOR&INTRON");
	}

	@Test
	public void test_03() {
		Gpr.debug("Test");
		check(1010, "SPLICE_SITE_REGION&INTRON");
	}

	@Test
	public void test_04() {
		Gpr.debug("Test");
		check(1012, "SPLICE_SITE_ACCEPTOR&INTRON");
	}

	@Test
	public void test_05() {
		Gpr.debug("Test");
		check(1013, "NON_SYNONYMOUS_CODING&SPLICE_SITE_REGION");
	}

}
