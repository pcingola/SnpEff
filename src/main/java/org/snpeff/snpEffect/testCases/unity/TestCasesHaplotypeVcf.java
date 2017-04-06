package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.annotate.AnnotateVcfHaplotypes;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEntry;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesHaplotypeVcf extends TestCasesBase {

	public static int N = 1000;

	public TestCasesHaplotypeVcf() {
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

	AnnotateVcfHaplotypes annotate(String vcfFileName) {
		minExons = 3;
		initSnpEffPredictor();

		if (debug) Gpr.debug("Transcript:\n" + transcript);
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		AnnotateVcfHaplotypes annhap = new AnnotateVcfHaplotypes();
		annhap.setConfig(config);
		annhap.setSnpEffectPredictor(snpEffectPredictor);
		annhap.setNoSummary();
		annhap.setSaveResults(true);
		annhap.setKeepAll(true);
		annhap.setVerbose(verbose);
		annhap.setSuppressOutput(!verbose);

		// Annotate all vcfEntries
		annhap.annotateInit(vcf);
		for (VcfEntry ve : vcf) {
			annhap.annotate(ve);
		}
		annhap.annotateFinish(vcf);
		return annhap;
	}

	//	/**
	//	 * Two SNPs affect same codon: Phased
	//	 */
	//	@Test
	//	public void test_01_phased() {
	//		Gpr.debug("Test");
	//		String vcfFileName = "tests/test_haplotype_vcf_01_phased.vcf";
	//		AnnotateVcfHaplotypes annhap = annotate(vcfFileName);
	//		List<VcfEntry> ves = annhap.getVcfEntries();
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), annhap.sameCodon(ves.get(0)));
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), annhap.sameCodon(ves.get(1)));
	//		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), annhap.sameCodon(ves.get(2)));
	//	}
	//
	//	/**
	//	 * Two SNPs affect same codon: Phased using phase group
	//	 */
	//	@Test
	//	public void test_01_phasegroup() {
	//		Gpr.debug("Test");
	//		String vcfFileName = "tests/test_haplotype_vcf_01_phasegroup.vcf";
	//		AnnotateVcfHaplotypes annhap = annotate(vcfFileName);
	//		List<VcfEntry> ves = annhap.getVcfEntries();
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), annhap.sameCodon(ves.get(0)));
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), annhap.sameCodon(ves.get(1)));
	//		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), annhap.sameCodon(ves.get(2)));
	//	}

	/**
	 * Two SNPs affect same codon: Implicit phasing
	 */
	@Test
	public void test_01_implicit() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_01_phase_implicit.vcf";
		AnnotateVcfHaplotypes annhap = annotate(vcfFileName);
		List<VcfEntry> ves = annhap.getVcfEntries();
		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), annhap.sameCodon(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), annhap.sameCodon(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), annhap.sameCodon(ves.get(2)));
	}

	//	/**
	//	 * Two SNPs affect one transcript: Exon edges
	//	 */
	//	@Test
	//	public void test_02() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two SNPs: Only one affects the coding part of the transcript
	//	 */
	//	@Test
	//	public void test_03() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two SNPs affect multiple transcripts
	//	 */
	//	@Test
	//	public void test_04() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two MNPs
	//	 */
	//	@Test
	//	public void test_05() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two frame-compensating INS nearby
	//	 */
	//	@Test
	//	public void test_06() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two frame-compensating INS far away
	//	 */
	//	@Test
	//	public void test_07() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two frame-compensating DEL nearby
	//	 */
	//	@Test
	//	public void test_08() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Two frame-compensating DEL far away
	//	 */
	//	@Test
	//	public void test_09() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Haplotype detection: Two phased variants
	//	 */
	//	@Test
	//	public void test_10() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Haplotype detection: Two variants implicitly phased (one of them is homozygous)
	//	 */
	//	@Test
	//	public void test_11() {
	//		Gpr.debug("Test");
	//	}
	//
	//	/**
	//	 * Haplotype detection: Two variants implicitly phased (both homozygous)
	//	 */
	//	@Test
	//	public void test_12() {
	//		Gpr.debug("Test");
	//	}

}
