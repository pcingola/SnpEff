package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 *
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	boolean verbose = true;

	public TestCasesZzz() {

	}

	/**
	 * Test output order
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		verbose = true;
		List<VcfEntry> vcfEntries = snpEffect("testHg19Chr1", "tests/test_interaction_01.vcf", null, null);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff);
			}
		}
	}

}
