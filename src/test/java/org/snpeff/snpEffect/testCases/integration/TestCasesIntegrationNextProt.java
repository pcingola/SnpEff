package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test NextProt databases
 *
 * @author pcingola
 */
public class TestCasesIntegrationNextProt extends TestCasesIntegrationBase {

	@Test
	public void test_01_build() {
		Log.debug("Test");
		String[] args = { "buildNextProt", "testHg3770Chr22", path("nextProt") };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		assertTrue(ok);
	}

	@Test
	public void test_02_ann() {
		Log.debug("Test");
		// Gene               : YWHAH
		// Transcript         : ENST00000248975
		// Variant            : NON_SYNONYMOUS_CODING
		// Variant Impact     : MODERATE
		// NextProt           : modified-residue_N-acetylglycine
		// NextProt conserved : false (not enough statistics in database)
		// Expected impact    : LOW
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_02.vcf")//
				, "modified-residue_N-acetylglycine"//
				, VariantEffect.EffectImpact.LOW //
				, true //
				, "ENST00000248975" //
		);
	}

	@Test
	public void test_02_eff() {
		Log.debug("Test");
		// Gene               : YWHAH
		// Transcript         : ENST00000248975
		// Variant            : NON_SYNONYMOUS_CODING
		// Variant Impact     : MODERATE
		// NextProt           : modified-residue_N-acetylglycine
		// NextProt conserved : false (not enough statistics in database)
		// Expected impact    : LOW
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_02.vcf") //
				, "modified-residue_N-acetylglycine" //
				, VariantEffect.EffectImpact.LOW //
				, false //
				, "ENST00000248975" //
		);
	}

	@Test
	public void test_03_ann() {
		Log.debug("Test");
		// Gene               : YWHAH
		// Transcript         : ENST00000248975
		// Variant            : NON_SYNONYMOUS_CODING
		// Variant Impact     : MODERATE
		// NextProt           : modified-residue_Phosphoserine
		// NextProt conserved : true
		// Expected impact    : HIGH
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_03.vcf") //
				, "modified-residue_Phosphoserine" //
				, VariantEffect.EffectImpact.HIGH //
				, true //
				, "ENST00000248975" //
		);
	}

	@Test
	public void test_03_eff() {
		Log.debug("Test");
		// Gene               : YWHAH
		// Transcript         : ENST00000248975
		// Variant            : NON_SYNONYMOUS_CODING
		// Variant Impact     : MODERATE
		// NextProt           : modified-residue_Phosphoserine
		// NextProt conserved : true
		// Expected impact    : HIGH
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_03.vcf") //
				, "modified-residue_Phosphoserine" //
				, VariantEffect.EffectImpact.HIGH //
				, false //
				, "ENST00000248975" //
		);
	}

	@Test
	public void test_04_parse() {
		Log.debug("Test");
		String vcfFile = path("test.nextProt_paren.vcf");
		int count = 0;
		for (VcfEntry ve : new VcfFileIterator(vcfFile)) {
			for (VcfEffect eff : ve.getVcfEffects()) {
				if (verbose) Log.info(eff);
				if (eff.hasEffectType(EffectType.NEXT_PROT)) count++;
			}
		}

		if (verbose) Log.info("Count: " + count);
		assertTrue(count > 0);
	}

	@Test
	public void test_05_ann_disulphide_bond() {
		Log.debug("Test");
		// Disufide bond spanning across an intron
		//
		// Gene               : GGT1
		// Variant            : 22:25016879 G / A
		// Variant Effect     : stop_gained&splice_region_variant
		// Variant Impact     : HIGH
		// NextProt           : disulfide-bond
		// NextProt conserved : true
		// Expected impact    : HIGH
		//
		// Transcript: ENST00000400382
		//		1:	22	25016879	25016879	NextProt	null
		//		2:	22	25016485	25016486	NextProt	null
		//		3:	22	25016889	25016891	NextProt	null
		//
		// NextProt: LocationTargetIsoformInteraction(NX_P19440-1, 191, 195)
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_05.vcf") //
				, "disulfide-bond" //
				, VariantEffect.EffectImpact.HIGH //
				, true //
				, "ENST00000400382" //
		);
	}

	@Test
	public void test_06_ann_disulphide_bond() {
		Log.debug("Test");
		// Disufide bond spanning across an intron
		//
		// Gene               : GGT1
		// Variant            : chr22:25016486 G / A
		// Variant Effect     : missense_variant&splice_region_variant|
		// Variant Impact     : MODERATE
		// NextProt           : disulfide-bond
		// NextProt conserved : true
		// Expected impact    : HIGH
		//
		// Transcript: ENST00000400382
		//		1:	22	25016879	25016879	NextProt	null
		//		2:	22	25016485	25016486	NextProt	null
		//		3:	22	25016889	25016891	NextProt	null
		//
		// NextProt: LocationTargetIsoformInteraction(NX_P19440-1, 191, 195)
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_06.vcf") //
				, "disulfide-bond" //
				, VariantEffect.EffectImpact.HIGH //
				, true //
				, "ENST00000400382" //
		);
	}

	@Test
	public void test_07() {
		// Test annotation not highly conserved (synonymous change) => EffectImpact.LOW;
		//
		// Gene               : ODF3B
		// Variant            : chr22:50969624 G / A
		// Variant Effect     : synonymous_variant
		// Variant Impact     : LOW
		// NextProt           : modified-residue_Phosphoserine
		// NextProt conserved : true
		// Expected impact    : LOW
		//
		// Transcript: ENST00000329363
		// NextProt: LocationTargetIsoformInteraction(NX_P19440-1, 191, 195)
		Log.debug("Test");
		checkNextProt("testHg3770Chr22" //
				, path("test_nextProt_07.vcf") //
				, "modified-residue_Phosphoserine" //
				, VariantEffect.EffectImpact.LOW //
				, true //
				, "ENST00000329363" //
		);
	}
}
