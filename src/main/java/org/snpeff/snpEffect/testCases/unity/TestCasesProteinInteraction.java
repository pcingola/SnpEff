package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.ProteinInteractionLocus;
import org.snpeff.util.Log;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test cases for protein interaction
 */
public class TestCasesProteinInteraction extends TestCasesBaseApply {

    public TestCasesProteinInteraction() {
    }

    /**
     * Get protein interaction loci when a codon is split by an intron
     */
    @Test
    public void test_01_split_codon_base() {
        Log.debug("Test");

        initSnpEffPredictor();

        if (verbose) Log.debug(transcript);
        String protein = transcript.protein();
        int codonNum = 33;
        int[] codonsPos = transcript.codonNumber2Pos(codonNum);
        if (verbose)
            Log.debug("AA[" + codonNum + "]: " + protein.charAt(codonNum) + "\t" + codonsPos[0] + "\t" + codonsPos[1] + "\t" + codonsPos[2]);

        // Create list o interactions
        List<ProteinInteractionLocus> list = ProteinInteractionLocus.factory(transcript, codonNum, transcript, "POS_" + codonNum);
        if (verbose) {
            for (ProteinInteractionLocus pil : list)
                Log.debug("Interaction locus: " + pil);
        }

        assertEquals(2, list.size(), "Number of loci do not match");

        ProteinInteractionLocus pil = list.get(0);
        assertTrue((pil.getStart() == 199) && (pil.getEndClosed() == 199), "Interactions coordinates do not match: " + pil);

        pil = list.get(1);
        assertTrue((pil.getStart() == 300) && (pil.getEndClosed() == 301), "Interactions coordinates do not match: " + pil);
    }

    /**
     * Get protein interaction loci when a codon is split by an intron
     */
    @Test
    public void test_02_split_codon_base() {
        Log.debug("Test");

        initSnpEffPredictor();

        if (verbose) Log.debug(transcript);
        String protein = transcript.protein();
        int codonNum = 66;
        int[] codonsPos = transcript.codonNumber2Pos(codonNum);
        if (verbose)
            Log.debug("AA[" + codonNum + "]: " + protein.charAt(codonNum) + "\t" + codonsPos[0] + "\t" + codonsPos[1] + "\t" + codonsPos[2]);

        // Create list o interactions
        List<ProteinInteractionLocus> list = ProteinInteractionLocus.factory(transcript, codonNum, transcript, "POS_" + codonNum);
        if (verbose) {
            for (ProteinInteractionLocus pil : list)
                Log.debug("Interaction locus: " + pil);
        }

        assertEquals(2, list.size(), "Number of loci do not match");

        ProteinInteractionLocus pil = list.get(0);
        assertTrue((pil.getStart() == 398) && (pil.getEndClosed() == 399), "Interactions coordinates do not match: " + pil);

        pil = list.get(1);
        assertTrue((pil.getStart() == 900) && (pil.getEndClosed() == 900), "Interactions coordinates do not match: " + pil);
    }

    public void test_03_split_codon_base_alphafold() {
        Log.debug("Test");
        assertTrue(false, "UNIMPLEMENTED!!!");
    }
}
