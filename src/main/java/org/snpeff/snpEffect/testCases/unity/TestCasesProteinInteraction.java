package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.interval.ProteinInteractionLocus;
import org.snpeff.util.Log;

import junit.framework.Assert;

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
		int codonsPos[] = transcript.codonNumber2Pos(codonNum);
		if (verbose) Log.debug("AA[" + codonNum + "]: " + protein.charAt(codonNum) + "\t" + codonsPos[0] + "\t" + codonsPos[1] + "\t" + codonsPos[2]);

		// Create list o interactions
		List<ProteinInteractionLocus> list = ProteinInteractionLocus.factory(transcript, codonNum, transcript, "POS_" + codonNum);
		if (verbose) {
			for (ProteinInteractionLocus pil : list)
				Log.debug("Interaction locus: " + pil);
		}

		Assert.assertEquals("Number of loci do not match", 2, list.size());

		ProteinInteractionLocus pil = list.get(0);
		Assert.assertTrue("Interactions coordinates do not match: " + pil, (pil.getStart() == 199) && (pil.getEnd() == 199));

		pil = list.get(1);
		Assert.assertTrue("Interactions coordinates do not match: " + pil, (pil.getStart() == 300) && (pil.getEnd() == 301));
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
		int codonsPos[] = transcript.codonNumber2Pos(codonNum);
		if (verbose) Log.debug("AA[" + codonNum + "]: " + protein.charAt(codonNum) + "\t" + codonsPos[0] + "\t" + codonsPos[1] + "\t" + codonsPos[2]);

		// Create list o interactions
		List<ProteinInteractionLocus> list = ProteinInteractionLocus.factory(transcript, codonNum, transcript, "POS_" + codonNum);
		if (verbose) {
			for (ProteinInteractionLocus pil : list)
				Log.debug("Interaction locus: " + pil);
		}

		Assert.assertEquals("Number of loci do not match", 2, list.size());

		ProteinInteractionLocus pil = list.get(0);
		Assert.assertTrue("Interactions coordinates do not match: " + pil, (pil.getStart() == 398) && (pil.getEnd() == 399));

		pil = list.get(1);
		Assert.assertTrue("Interactions coordinates do not match: " + pil, (pil.getStart() == 900) && (pil.getEnd() == 900));
	}

}
