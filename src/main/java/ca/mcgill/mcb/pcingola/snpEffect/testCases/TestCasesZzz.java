package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Test old effect separator '+' instead of '&'
	 */
	@Test
	public void test_29() {
		Gpr.debug("Test");
		String fileName = "./tests/test_vcf_ann_plus_sign.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);

		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t" + veff);

				// Check
				Assert.assertEquals(EffectType.UTR_5_DELETED.toString(), veff.getEffectTypes().get(0).toString());
				Assert.assertEquals(EffectType.EXON_DELETED.toString(), veff.getEffectTypes().get(1).toString());
			}
		}
	}

}
