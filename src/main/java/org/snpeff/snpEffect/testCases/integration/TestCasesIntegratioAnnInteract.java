package org.snpeff.snpEffect.testCases.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test cases for annotation of protein interaction loci
 */
public class TestCasesIntegratioAnnInteract extends TestCasesIntegrationBase {

	public TestCasesIntegratioAnnInteract() {
	}

	/**
	 * Annotate within protein interaction
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		List<VcfEntry> vcfEntries = snpEffect("testHg19Pdb", "tests/test_interaction_01.vcf", null, null);

		Map<String, Boolean> expectedIds = new HashMap<>();
		expectedIds.put("NM_001048199.2:1A12_A:25_136", false);
		expectedIds.put("NM_001048199.2:1A12_B:25_136", false);
		expectedIds.put("NM_001048199.2:1A12_C:25_136", false);
		expectedIds.put("NM_001269.4:1A12_A:25_136", false);
		expectedIds.put("NM_001269.4:1A12_B:25_136", false);
		expectedIds.put("NM_001269.4:1A12_C:25_136", false);

		// Parse and check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			int countPi = 0;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
				if (veff.getEffectType() == EffectType.PROTEIN_INTERACTION_LOCUS) {
					if (verbose) System.out.println("FOUND\t" + veff.getEffectType() + "\t" + veff);
					countPi++;

					String id = veff.getFeatureId();
					Assert.assertTrue("Unexpected ID" + id, expectedIds.containsKey(id));
					expectedIds.put(id, true); // Mark interaction as 'found'
				}
			}

			Assert.assertTrue("No PROTEIN_INTERACTION_LOCUS effect found", countPi > 0);

			// Check if we've found all interactions
			for (String id : expectedIds.keySet())
				Assert.assertTrue("Interaction not found: " + id, expectedIds.get(id));
		}
	}

}
