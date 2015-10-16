package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Before;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;
import junit.framework.Assert;

/**
 * Test cases: apply a variant (MIXED) to a transcript
 *
 */
public class TestCasesVariantDecompose {

	protected Genome genome;
	protected Chromosome chr;

	public TestCasesVariantDecompose() {
		super();
	}

	@Before
	public void before() {
		genome = new Genome();
		chr = new Chromosome(genome, 0, 1000, "1");
	}

	/**
	 * Variant before exon
	 */
	@Test
	public void test_00_decomposeVariant_01() {
		Gpr.debug("Test");
		Variant variant = new Variant(chr, 300, "TTTATC", "ACG", "MIXED");
		Variant variants[] = variant.decompose();

		Assert.assertEquals("Variant decomposition MNP part failed", "chr1:300_TTT/ACG 'MIXED_MNP'", variants[0].toString());
		Assert.assertEquals("Variant decomposition InDel part failed", "chr1:303_ATC/ 'MIXED_DEL'", variants[1].toString());
	}

	@Test
	public void test_00_decomposeVariant_02() {
		Gpr.debug("Test");
		Variant variant = new Variant(chr, 300, "TTT", "ACGATC", "MIXED");
		Variant variants[] = variant.decompose();

		Assert.assertEquals("Variant decomposition MNP part failed", "chr1:300_TTT/ACG 'MIXED_MNP'", variants[0].toString());
		Assert.assertEquals("Variant decomposition InDel part failed", "chr1:303_/ATC 'MIXED_INS'", variants[1].toString());
	}

}
