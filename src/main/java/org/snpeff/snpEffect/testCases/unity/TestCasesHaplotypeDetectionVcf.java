package org.snpeff.snpEffect.testCases.unity;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.annotate.HaplotypeAnnotationDetector;
import org.snpeff.annotate.SameCodonHaplotypeDetector;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEntry;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesHaplotypeDetectionVcf extends TestCasesBase {

	class DetectorAndVcfEntries {
		public HaplotypeAnnotationDetector hapDet;
		public List<VcfEntry> vcfEntries;

		public DetectorAndVcfEntries() {
			vcfEntries = new ArrayList<>();
		}
	}

	public static int N = 1000;

	public TestCasesHaplotypeDetectionVcf() {
		super();
	}

	DetectorAndVcfEntries detectSameCodon(String vcfFileName) {
		minExons = 3;
		initSnpEffPredictor();

		if (debug) Gpr.debug("Transcript:\n" + transcript);
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		SameCodonHaplotypeDetector sameCodonHaplotypeDetector = new SameCodonHaplotypeDetector();
		sameCodonHaplotypeDetector.setVerbose(verbose);

		// Annotate all variants and add them o the detector
		DetectorAndVcfEntries dv = new DetectorAndVcfEntries();
		dv.hapDet = sameCodonHaplotypeDetector;
		for (VcfEntry ve : vcf) {
			dv.vcfEntries.add(ve);
			for (Variant var : ve.variants()) {
				VariantEffects variantEffects = snpEffectPredictor.variantEffect(var);
				for (VariantEffect veff : variantEffects)
					sameCodonHaplotypeDetector.add(ve, var, veff);
			}
		}
		return dv;
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20170331;
	}

	/**
	 * Two SNPs affect same codon: Implicit phasing
	 */
	@Test
	public void test_01_implicit() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_01_phase_implicit.vcf";
		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
		List<VcfEntry> ves = dv.vcfEntries;

		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));

		Assert.assertTrue("Variant should be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
		Assert.assertTrue("Variant should be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	}

	/**
	 * Two SNPs affect same codon: Phased
	 */
	@Test
	public void test_01_phased() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_01_phased.vcf";
		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
		List<VcfEntry> ves = dv.vcfEntries;

		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));

		Assert.assertTrue("Variant should be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
		Assert.assertTrue("Variant should be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	}

	/**
	 * Two SNPs affect same codon: Phased using phase group
	 */
	@Test
	public void test_01_phasegroup() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_01_phasegroup.vcf";
		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
		List<VcfEntry> ves = dv.vcfEntries;

		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));

		Assert.assertFalse("Variant should NOT be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	}

	/**
	 * Two SNPs affect same codon: Exons edges, implicit phasing
	 */
	@Test
	public void test_02_implicit() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_02_phase_implicit.vcf";
		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
		List<VcfEntry> ves = dv.vcfEntries;

		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));

		Assert.assertTrue("Variant should be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
		Assert.assertTrue("Variant should be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	}

	/**
	 * Two SNPs affect same codon: Exon edges, phase groups
	 */
	@Test
	public void test_02_phased() {
		Gpr.debug("Test");
		String vcfFileName = "tests/test_haplotype_vcf_02_phased.vcf";
		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
		List<VcfEntry> ves = dv.vcfEntries;

		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));

		Assert.assertTrue("Variant should be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
		Assert.assertTrue("Variant should be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	}

	//
	//	/**
	//	 * Two SNPs affect same codon: Exon edges: Phased using phase group
	//	 */
	//	@Test
	//	public void test_02_phasegroup() {
	//		Gpr.debug("Test");
	//		String vcfFileName = "tests/test_haplotype_vcf_02_phasegroup.vcf";
	//		DetectorAndVcfEntries dv = detectSameCodon(vcfFileName);
	//		List<VcfEntry> ves = dv.vcfEntries;
	//
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(0), dv.hapDet.hasHaplotypeAnnotation(ves.get(0)));
	//		Assert.assertTrue("Variant should be in same codon:" + ves.get(1), dv.hapDet.hasHaplotypeAnnotation(ves.get(1)));
	//		Assert.assertFalse("Variant should NOT be in same codon:" + ves.get(2), dv.hapDet.hasHaplotypeAnnotation(ves.get(2)));
	//
	//		Assert.assertFalse("Variant should NOT be free:" + ves.get(0), dv.hapDet.isFree(ves.get(0)));
	//		Assert.assertFalse("Variant should NOT be free:" + ves.get(1), dv.hapDet.isFree(ves.get(1)));
	//		Assert.assertFalse("Variant should NOT be free:" + ves.get(2), dv.hapDet.isFree(ves.get(2)));
	//	}

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
