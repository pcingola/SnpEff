package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case for parsing ANN fields
 *
 */
public class TestCasesAnnParse {

	public TestCasesAnnParse() {
		super();
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
