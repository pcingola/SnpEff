package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test cases for annotation of protein interaction loci
 */
public class TestCasesIntegratioAnnInteract extends TestCasesIntegrationBase {

	public TestCasesIntegratioAnnInteract() {
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		verbose = true;
		List<VcfEntry> vcfEntries = snpEffect("testHg19Chr1", "tests/test_interaction_01.vcf", null, null);

		Set<String> expectedIds = new HashSet<>();
		expectedIds.add("NM_001048199.2:1A12_A:25_136");
		expectedIds.add("NM_001048199.2:1A12_B:25_136");
		expectedIds.add("NM_001048199.2:1A12_C:25_136");
		expectedIds.add("NM_001269.4:1A12_A:25_136");
		expectedIds.add("NM_001269.4:1A12_B:25_136");
		expectedIds.add("NM_001269.4:1A12_C:25_136");

		// Parse and check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			int countPi = 0;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (veff.getEffectType() == EffectType.PROTEIN_INTERACTION_LOCUS) {
					if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
					countPi++;

					String id = veff.getFeatureId();
					Assert.assertTrue("Unexcpedted ID" + id, expectedIds.contains(id));
				}
			}

			Assert.assertTrue("No PROTEIN_INTERACTION_LOCUS effect found", countPi > 0);
		}
	}

}
