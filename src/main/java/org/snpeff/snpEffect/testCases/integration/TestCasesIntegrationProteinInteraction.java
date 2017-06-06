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
public class TestCasesIntegrationProteinInteraction extends TestCasesIntegrationBase {

	public TestCasesIntegrationProteinInteraction() {
	}

	/**
	 * Annotate within protein interaction
	 */
	@Test
	public void test_01_within_protein_interactions() {
		Gpr.debug("Test");

		List<VcfEntry> vcfEntries = snpEffect("testHg19Pdb", "tests/test_interaction_01.vcf", null, null);

		Map<String, Boolean> expectedIds = new HashMap<>();
		expectedIds.put("1A12:A_25-A_136:NM_001048199.2", false);
		expectedIds.put("1A12:B_25-B_136:NM_001048199.2", false);
		expectedIds.put("1A12:C_25-C_136:NM_001048199.2", false);
		expectedIds.put("1A12:A_25-A_136:NM_001269.4", false);
		expectedIds.put("1A12:B_25-B_136:NM_001269.4", false);
		expectedIds.put("1A12:C_25-C_136:NM_001269.4", false);

		// Parse and check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			int countPi = 0;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
				if (veff.getEffectType() == EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS) {
					if (verbose) System.out.println("FOUND\t" + veff.getEffectType() + "\t" + veff);

					String id = veff.getFeatureId();
					if (expectedIds.containsKey(id)) {
						countPi++;
						expectedIds.put(id, true); // Mark interaction as 'found'
					} else if (verbose) System.err.println("Found additional ID '" + id + "'"); // Show IDs we didn't include
				}
			}

			Assert.assertTrue("No protein interaction effect found", countPi > 0);

			// Check if we've found all interactions
			for (String id : expectedIds.keySet())
				Assert.assertTrue("Interaction not found: " + id, expectedIds.get(id));
		}
	}

	/**
	 * Annotate protein-protein interactions
	 */
	@Test
	public void test_02_protein_protein_interactions() {
		Gpr.debug("Test");

		List<VcfEntry> vcfEntries = snpEffect("testHg19Pdb", "tests/test_interaction_02.vcf", null, null);

		Map<String, Boolean> expectedIds = new HashMap<>();
		expectedIds.put("4OVU:A_7-B_479:NM_006218.2-NM_181523.2", false);

		// Parse and check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			int countPi = 0;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff.getEffectType() + "\t" + veff);
				if (verbose) System.out.println("EFF\t" + veff.getEffectType() + "\t" + veff);
				if (veff.getEffectType() == EffectType.PROTEIN_PROTEIN_INTERACTION_LOCUS) {
					if (verbose) System.out.println("FOUND\t" + veff.getEffectType() + "\t" + veff);

					String id = veff.getFeatureId();
					if (expectedIds.containsKey(id)) {
						countPi++;
						expectedIds.put(id, true); // Mark interaction as 'found'
					} else if (verbose) System.err.println("Found additional ID '" + id + "'"); // Show IDs we didn't include
				}
			}

			Assert.assertTrue("No protein interaction effect found", countPi > 0);

			// Check if we've found all interactions
			for (String id : expectedIds.keySet())
				Assert.assertTrue("Interaction not found: " + id, expectedIds.get(id));
		}
	}

	/**
	 * Annotate protein-structural interaction
	 * Make sure that the HGVS notation positions (DNA and Protein) are consistent.
	 * When the variant is realigned, the protein-interaction annotation should not appear
	 */
	@Test
	public void test_03_protein_protein_interactions_hgvs() {
		Gpr.debug("Test");
		String args[] = { "-canon" };
		List<VcfEntry> vcfEntries = snpEffect("testHg19Chr22", "tests/test_interaction_03.vcf", args, null);

		// Parse and check output
		boolean foundPi = false;
		boolean foundFs = false;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			int countPi = 0;
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\tEFF\t" + veff.getEffectType() + "\t" + veff);
				if (veff.getEffectType() == EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS) {
					countPi++;
					Assert.assertTrue("Expected HGVS.c possition does not match", veff.getHgvsDna().startsWith("c.679"));
					Assert.assertTrue("Expected HGVS.p possition does not match", //
							veff.getFeatureId().startsWith("1H4R:A_227") //
									|| veff.getFeatureId().startsWith("1H4R:B_227") //
					);
					foundPi = true;
				} else if (veff.getEffectType() == EffectType.FRAME_SHIFT) {
					Assert.assertTrue("Expected HGVS.c possition does not match", veff.getHgvsDna().startsWith("c.683"));
					Assert.assertTrue("Expected HGVS.p possition does not match", veff.getHgvsProt().startsWith("p.Lys228"));
					foundFs = true;
				}
			}

			if (ve.isSingleSnp()) {
				Assert.assertTrue("No interactions found", countPi > 0);
			} else {
				Assert.assertFalse("There should be no interactions", countPi > 0);
			}
		}

		Assert.assertTrue("Interaction term not found", foundPi);
		Assert.assertTrue("Frameshift term not found", foundFs);
	}

}
