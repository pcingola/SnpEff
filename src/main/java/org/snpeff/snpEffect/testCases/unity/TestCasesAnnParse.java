package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test case for parsing ANN fields
 *
 */
public class TestCasesAnnParse {

	boolean verbose = false;

	public TestCasesAnnParse() {
		super();
	}

	/**
	 * Make sure all effect_tpyes have appropriate impacts, regions, etc.
	 */
	@Test
	public void test_EffectType() {
		for (EffectType eff : EffectType.values()) {
			if (verbose) System.out.println("\t" + eff);

			// None of these should throw an exception
			eff.effectImpact();
			eff.getGeneRegion();

			for (EffFormatVersion formatVersion : EffFormatVersion.values()) {
				eff.toSequenceOntology(formatVersion, null);
			}
		}
	}

	/**
	 * Make sure there are no exceptions thrown when parsing TFBS_abalation SO term
	 */
	@Test
	public void testCase_tfbs_ablation() {
		Gpr.debug("Test");
		String vcfFile = "tests/tfbs_ablation.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);

		boolean ok = false;
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff.getEffectsStrSo());
				ok |= veff.getEffectsStrSo().indexOf("TFBS_ablation") >= 0;
			}
		}

		Assert.assertTrue("SO term 'TFBS_ablation' not found", ok);
	}

}
