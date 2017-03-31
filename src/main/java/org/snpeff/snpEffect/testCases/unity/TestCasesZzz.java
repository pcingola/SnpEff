package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variants;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
	}

	String effectStr(VariantEffect effect) {
		String effStr = effect.effect(true, true, true, false, false);
		String aaStr = effect.getAaChangeOld();
		int idx = effStr.indexOf('(');
		return effStr.substring(0, idx) + "(" + aaStr + ")";
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20170331;
	}

	/**
	 * Two SNPs affect one transcript
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		minExons = 3;
		initSnpEffPredictor();

		Variant snp1 = new Variant(chromosome, 374, "G", "C");
		Variant snp2 = new Variant(chromosome, 375, "C", "A");
		Variants vars = new Variants();
		vars.add(snp1);
		vars.add(snp2);
		Gpr.debug("Transcript:" + transcript);
		Gpr.debug("Variants:" + vars);

		VariantEffects variantEffects = new VariantEffects();
		transcript.variantEffect(vars, variantEffects);
		Gpr.debug("Variant effects" + variantEffects);

	}

	/**
	 * Two SNPs affect one transcript: Exon edges
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
	}

	/**
	 * Two SNPs: Only one affects the coding part of the transcript
	 */
	@Test
	public void test_03() {
		Gpr.debug("Test");
	}

	/**
	 * Two SNPs affect multiple transcripts
	 */
	@Test
	public void test_04() {
		Gpr.debug("Test");
	}

	/**
	 * Two MNPs 
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");
	}

	/**
	 * Two frame-compensating INS nearby  
	 */
	@Test
	public void test_06() {
		Gpr.debug("Test");
	}

	/**
	 * Two frame-compensating INS far away  
	 */
	@Test
	public void test_07() {
		Gpr.debug("Test");
	}

	/**
	 * Two frame-compensating DEL nearby  
	 */
	@Test
	public void test_08() {
		Gpr.debug("Test");
	}

	/**
	 * Two frame-compensating DEL far away
	 */
	@Test
	public void test_09() {
		Gpr.debug("Test");
	}

	/**
	 * Haplotype detection: Two phased variants
	 */
	@Test
	public void test_10() {
		Gpr.debug("Test");
	}

	/**
	 * Haplotype detection: Two variants implicitly phased (one of them is homozygous)
	 */
	@Test
	public void test_11() {
		Gpr.debug("Test");
	}

	/**
	 * Haplotype detection: Two variants implicitly phased (both homozygous)
	 */
	@Test
	public void test_12() {
		Gpr.debug("Test");
	}

}
