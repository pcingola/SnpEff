package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Log.debug("Test");

        List<VcfEntry> vcfEntries = snpEffect("testHg19Pdb", path("test_interaction_01.vcf"), null, null);

        Map<String, Boolean> expectedIds = new HashMap<>();
        expectedIds.put("1A12:A_25-A_136:NM_001048199.2", false);
        expectedIds.put("1A12:B_25-B_136:NM_001048199.2", false);
        expectedIds.put("1A12:C_25-C_136:NM_001048199.2", false);
        expectedIds.put("1A12:A_25-A_136:NM_001269.4", false);
        expectedIds.put("1A12:B_25-B_136:NM_001269.4", false);
        expectedIds.put("1A12:C_25-C_136:NM_001269.4", false);

        // Parse and check output
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            int countPi = 0;
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t" + veff.getEffectType() + "\t" + veff);
                if (veff.getEffectType() == EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS) {
                    if (verbose) Log.info("FOUND\t" + veff.getEffectType() + "\t" + veff);

                    String id = veff.getFeatureId();
                    if (expectedIds.containsKey(id)) {
                        countPi++;
                        expectedIds.put(id, true); // Mark interaction as 'found'
                    } else if (verbose) Log.info("Found additional ID '" + id + "'"); // Show IDs we didn't include
                }
            }

            assertTrue(countPi > 0, "No protein interaction effect found");

            // Check if we've found all interactions
            for (String id : expectedIds.keySet())
                assertTrue(expectedIds.get(id), "Interaction not found: " + id);
        }
    }

    /**
     * Annotate protein-protein interactions
     */
    @Test
    public void test_02_protein_protein_interactions() {
        Log.debug("Test");

        List<VcfEntry> vcfEntries = snpEffect("testHg19Pdb", path("test_interaction_02.vcf"), null, null);

        Map<String, Boolean> expectedIds = new HashMap<>();
        expectedIds.put("4OVU:A_7-B_479:NM_006218.2-NM_181523.2", false);

        // Parse and check output
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            int countPi = 0;
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\t" + veff.getEffectType() + "\t" + veff);
                if (verbose) Log.info("EFF\t" + veff.getEffectType() + "\t" + veff);
                if (veff.getEffectType() == EffectType.PROTEIN_PROTEIN_INTERACTION_LOCUS) {
                    if (verbose) Log.info("FOUND\t" + veff.getEffectType() + "\t" + veff);

                    String id = veff.getFeatureId();
                    if (expectedIds.containsKey(id)) {
                        countPi++;
                        expectedIds.put(id, true); // Mark interaction as 'found'
                    } else if (verbose) Log.info("Found additional ID '" + id + "'"); // Show IDs we didn't include
                }
            }

            assertTrue(countPi > 0, "No protein interaction effect found");

            // Check if we've found all interactions
            for (String id : expectedIds.keySet())
                assertTrue(expectedIds.get(id), "Interaction not found: " + id);
        }
    }

    /**
     * Annotate protein-structural interaction
     * Make sure that the HGVS notation positions (DNA and Protein) are consistent.
     * When the variant is realigned, the protein-interaction annotation should not appear
     */
    @Test
    public void test_03_protein_protein_interactions_hgvs() {
        Log.debug("Test");

        String[] args = {"-canon"};
        List<VcfEntry> vcfEntries = snpEffect("testHg19Chr22", path("test_interaction_03.vcf"), args, null);

        // Parse and check output
        boolean foundPi = false;
        boolean foundFs = false;
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            int countPi = 0;
            for (VcfEffect veff : ve.getVcfEffects()) {
                if (verbose) Log.info("\tEFF\t" + veff.getEffectType() + "\t" + veff);
                if (veff.getEffectType() == EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS) {
                    countPi++;
                    assertTrue(veff.getHgvsDna().startsWith("c.679"), "Expected HGVS.c possition does not match");
                    assertTrue(veff.getFeatureId().startsWith("1H4R:A_227") //
                                    || veff.getFeatureId().startsWith("1H4R:B_227"), //
                            "Expected HGVS.p possition does not match" //
                    );
                    foundPi = true;
                } else if (veff.getEffectType() == EffectType.FRAME_SHIFT) {
                    assertTrue(veff.getHgvsDna().startsWith("c.683"), "Expected HGVS.c possition does not match");
                    assertTrue(veff.getHgvsProt().startsWith("p.Lys228"), "Expected HGVS.p possition does not match");
                    foundFs = true;
                }
            }

            if (ve.isSingleSnp()) {
                assertTrue(countPi > 0, "No interactions found");
            } else {
                assertFalse(countPi > 0, "There should be no interactions");
            }
        }

        assertTrue(foundPi, "Interaction term not found");
        assertTrue(foundFs, "Frameshift term not found");
    }

}
