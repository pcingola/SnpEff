package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

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

	@Test
	public void testCase_tfbs_ablation() {
		Gpr.debug("Test");
		String vcfFile = "tests/tfbs_ablation.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				System.out.println("\t" + veff);
			}
		}
	}

}
