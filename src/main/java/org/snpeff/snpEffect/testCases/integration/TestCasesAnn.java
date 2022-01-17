package org.snpeff.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.snpEffect.testCases.unity.TestCasesBase;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test case
 */
public class TestCasesAnn extends TestCasesBase {

	public TestCasesAnn() {
		super();
	}

	/**
	 * Annotate a VCF file and check that corresponding annotataion
	 */
	List<VcfEntry> annotate(String vcfFile) {
		String genome = "test_ENSG00000158062";

		String[] args = { "-noLog", "-noStats", genome, vcfFile };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
		List<VcfEntry> vcfEntries = seff.run(true);

		assertFalse(vcfEntries.isEmpty(), "Empty annotataions list!");
		return vcfEntries;
	}

	VcfEntry annotateFirst(String vcfFile) {
		List<VcfEntry> vcfEntries = annotate(vcfFile);
		return vcfEntries.get(0);
	}

	VcfEffect annotateFirst(String vcfFile, String trId) {
		// Annotate
		List<VcfEntry> vcfEntries = annotate(vcfFile);

		// Find annotation for a given transcript
		VcfEntry ve = vcfEntries.get(0);
		for (VcfEffect veff : ve.getVcfEffects()) {
			if (verbose) {
				System.out.println("\t" + veff //
						+ "\n\t\tveff.getEffectString() : " + veff.getVcfFieldString() //
						+ "\n\t\tveff.getEffectsStr()   : " + veff.getEffectsStr() //
						+ "\n\t\tveff.getEffectsStrSo() : " + veff.getEffectsStrSo() //
						+ "\n\t\tveff.getEffectType()   : " + veff.getEffectType() //
						+ "\n\t\tveff.getEffectTypes()  : " + veff.getEffectTypes() //
				);
			}

			// Check for a specific transcript
			if (trId == null || veff.getTranscriptId().equals(trId)) return veff;
		}

		throw new RuntimeException("Transcript '" + trId + "' not found");
	}

	@Test
	public void test_01_Annotation_Stop() {
		Log.debug("Test");
		String vcfFile = path("test_ann_01.vcf");

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000472155");

		if (verbose) Log.debug(veff);

		//---
		// Check results
		//---

		// Allele
		assertEquals("A", veff.getAllele());

		// Annotataion
		assertEquals("stop_gained", veff.getEffectsStrSo());
		assertEquals("STOP_GAINED", veff.getEffectsStr());

		// Impact
		assertEquals("HIGH", veff.getImpact().toString());

		// Gene name / ID
		assertEquals("UBXN11", veff.getGeneName());
		assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		assertEquals("ENST00000472155", veff.getFeatureId());
		assertEquals("ENST00000472155", veff.getTranscriptId());

		// Biotype
		assertEquals("protein_coding", veff.getBioType().toString());

		// Rank
		assertEquals("10", "" + veff.getRank());
		assertEquals("14", "" + veff.getRankMax());

		// HGVS
		assertEquals("c.1915C>T", veff.getHgvsDna());
		assertEquals("p.Gln639*", veff.getHgvsProt());

		// cDNA position
		assertEquals("1915", "" + veff.getcDnaPos());
		assertEquals("2646", "" + veff.getcDnaLen());

		// CDS position
		assertEquals("1915", "" + veff.getCdsPos());
		assertEquals("2646", "" + veff.getCdsLen());

		// AA position
		assertEquals("639", "" + veff.getAaPos());
		assertEquals("881", "" + veff.getAaLen());

		// Warning
		assertEquals("WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS", veff.getErrorsWarning());
	}

	/**
	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	 */
	@Test
	public void test_01_VcfRawAlt() {
		Log.debug("Test");

		// Create command
		String[] args = { "testHg3775Chr1", path("test_ann_integration_01.vcf") };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		assertTrue(cmdEff.getTotalErrs() <= 0, "Errors while executing SnpEff");

		// Expected results
		Set<String> allelesExpected = new HashSet<>();
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");

		// Find AA change for a genotype
		Set<String> allelesReal = new HashSet<>();
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);

			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
				String allele = eff.getAllele();
				if (verbose) Log.info("\t" + eff + "\n\t\tAllele: " + allele);

				assertTrue(allelesExpected.contains(allele), "Unexpected allele '" + allele + "'");
				allelesReal.add(allele);
			}
		}

		assertEquals(allelesExpected, allelesReal);
	}

	@Test
	public void test_02_Annotation_SpliceRegion() {
		Log.debug("Test");
		String vcfFile = path("test_ann_02.vcf");

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000374221");

		if (verbose) Log.debug(veff);

		//---
		// Check results
		//---

		// Allele
		assertEquals("A", veff.getAllele());

		// Annotataion
		assertEquals("splice_region_variant&intron_variant", veff.getEffectsStrSo());
		assertEquals("SPLICE_SITE_REGION&INTRON", veff.getEffectsStr());

		// Impact
		assertEquals("LOW", veff.getImpact().toString());

		// Gene name / ID
		assertEquals("UBXN11", veff.getGeneName());
		assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		assertEquals("ENST00000374221", veff.getFeatureId());
		assertEquals("ENST00000374221", veff.getTranscriptId());

		// Biotype
		assertEquals("protein_coding", veff.getBioType().toString());

		// Rank
		assertEquals("11", "" + veff.getRank());
		assertEquals("15", "" + veff.getRankMax());

		// HGVS
		assertEquals("c.853-3C>T", veff.getHgvsDna());
		assertEquals("", veff.getHgvsProt());

		// cDNA position
		assertEquals("-1", "" + veff.getcDnaPos());
		assertEquals("-1", "" + veff.getcDnaLen());

		// CDS position
		assertEquals("-1", "" + veff.getCdsPos());
		assertEquals("-1", "" + veff.getCdsLen());

		// AA position
		assertEquals("-1", "" + veff.getAaPos());
		assertEquals("-1", "" + veff.getAaLen());

		// Warning
		assertEquals("", veff.getErrorsWarning());
	}

	@Test
	public void test_03_Annotation_NonSyn() {
		Log.debug("Test");
		String vcfFile = path("test_ann_03.vcf");

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000374221");

		if (verbose) Log.debug(veff);

		//---
		// Check results
		//---

		// Allele
		assertEquals("A", veff.getAllele());

		// Annotataion
		assertEquals("missense_variant&splice_region_variant", veff.getEffectsStrSo());
		assertEquals("NON_SYNONYMOUS_CODING&SPLICE_SITE_REGION", veff.getEffectsStr());

		// Impact
		assertEquals("MODERATE", veff.getImpact().toString());

		// Gene name / ID
		assertEquals("UBXN11", veff.getGeneName());
		assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		assertEquals("ENST00000374221", veff.getFeatureId());
		assertEquals("ENST00000374221", veff.getTranscriptId());

		// Biotype
		assertEquals("protein_coding", veff.getBioType().toString());

		// Rank
		assertEquals("12", "" + veff.getRank());
		assertEquals("16", "" + veff.getRankMax());

		// HGVS
		assertEquals("c.853C>T", veff.getHgvsDna());
		assertEquals("p.Val285Leu", veff.getHgvsProt());

		// cDNA position
		assertEquals("1067", "" + veff.getcDnaPos());
		assertEquals("1792", "" + veff.getcDnaLen());

		// CDS position
		assertEquals("853", "" + veff.getCdsPos());
		assertEquals("1563", "" + veff.getCdsLen());

		// AA position
		assertEquals("285", "" + veff.getAaPos());
		assertEquals("520", "" + veff.getAaLen());

		// Warning
		assertEquals("WARNING_REF_DOES_NOT_MATCH_GENOME", veff.getErrorsWarning());
	}

	@Test
	public void test_04_Annotation_Intergenic() {
		Log.debug("Test");
		String vcfFile = path("test_ann_04.vcf");

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, null);

		if (verbose) Log.debug(veff);

		//---
		// Check results
		//---

		// Allele
		assertEquals("A", veff.getAllele());

		// Annotataion
		assertEquals("intergenic_region", veff.getEffectsStrSo());
		assertEquals("INTERGENIC", veff.getEffectsStr());

		// Impact
		assertEquals("MODIFIER", veff.getImpact().toString());

		// Gene name / ID
		assertEquals("CHR_START-UBXN11", veff.getGeneName());
		assertEquals("CHR_START-ENSG00000158062", veff.getGeneId());

		// Feature type
		assertEquals("intergenic_region", veff.getFeatureType());

		// FeatureId / transcriptId
		assertEquals("CHR_START-ENSG00000158062", veff.getFeatureId());
		assertEquals("", veff.getTranscriptId());

		// Biotype
		assertNull(veff.getBioType());

		// Rank
		assertEquals("-1", "" + veff.getRank());
		assertEquals("-1", "" + veff.getRankMax());

		// HGVS
		assertEquals("n.26600818G>A", veff.getHgvsDna());
		assertEquals("", veff.getHgvsProt());

		// cDNA position
		assertEquals("-1", "" + veff.getcDnaPos());
		assertEquals("-1", "" + veff.getcDnaLen());

		// CDS position
		assertEquals("-1", "" + veff.getCdsPos());
		assertEquals("-1", "" + veff.getCdsLen());

		// AA position
		assertEquals("-1", "" + veff.getAaPos());
		assertEquals("-1", "" + veff.getAaLen());

		// Warning
		assertEquals("", veff.getErrorsWarning());
	}

	@Test
	public void test_05_Annotation_EndOfChromosome() {
		Log.debug("Test");

		// Create a variant: Insertion after last chromosome base
		Variant variant = new Variant(genome.getChromosome("1"), 2001, "", "TTT", "");
		VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

		// Check output
		if (verbose) Log.info("Number of effects: " + veffs.size());
		assertEquals(1, veffs.size());

		VariantEffect veff = veffs.get(0);
		if (verbose) Log.info("Effect type : " + veff.getEffectType());
		assertEquals(EffectType.CHROMOSOME_ELONGATION, veff.getEffectType());
	}

}
