package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

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

		String args[] = { "-noLog", "-noStats", genome, vcfFile };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);

		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		List<VcfEntry> vcfEntries = seff.run(true);

		Assert.assertTrue("Empty annotataions list!", !vcfEntries.isEmpty());
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
		Gpr.debug("Test");
		String vcfFile = "tests/test_ann_01.vcf";

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000472155");

		if (verbose) Gpr.debug(veff);

		//---
		// Check results
		//---

		// Allele
		Assert.assertEquals("A", veff.getAllele());

		// Annotataion
		Assert.assertEquals("stop_gained", veff.getEffectsStrSo());
		Assert.assertEquals("STOP_GAINED", veff.getEffectsStr());

		// Impact
		Assert.assertEquals("HIGH", veff.getImpact().toString());

		// Gene name / ID
		Assert.assertEquals("UBXN11", veff.getGeneName());
		Assert.assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		Assert.assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		Assert.assertEquals("ENST00000472155", veff.getFeatureId());
		Assert.assertEquals("ENST00000472155", veff.getTranscriptId());

		// Biotype
		Assert.assertEquals("protein_coding", veff.getBioType());

		// Rank
		Assert.assertEquals("10", "" + veff.getRank());
		Assert.assertEquals("14", "" + veff.getRankMax());

		// HGVS
		Assert.assertEquals("c.1915C>T", veff.getHgvsDna());
		Assert.assertEquals("p.Gln639*", veff.getHgvsProt());

		// cDNA position
		Assert.assertEquals("1915", "" + veff.getcDnaPos());
		Assert.assertEquals("2646", "" + veff.getcDnaLen());

		// CDS position
		Assert.assertEquals("1915", "" + veff.getCdsPos());
		Assert.assertEquals("2646", "" + veff.getCdsLen());

		// AA position
		Assert.assertEquals("639", "" + veff.getAaPos());
		Assert.assertEquals("881", "" + veff.getAaLen());

		// Warning
		Assert.assertEquals("WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS", veff.getErrorsWarning());
	}

	/**
	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	 */
	@Test
	public void test_01_VcfRawAlt() {
		Gpr.debug("Test");

		// Create command
		String args[] = { "testHg3775Chr1", "tests/test_ann_integration_01.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Expected results
		Set<String> allelesExpected = new HashSet<String>();
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");

		// Find AA change for a genotype
		Set<String> allelesReal = new HashSet<String>();
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);

			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
				String allele = eff.getAllele();
				if (verbose) System.err.println("\t" + eff + "\n\t\tAllele: " + allele);

				Assert.assertTrue("Unexpected allele '" + allele + "'", allelesExpected.contains(allele));
				allelesReal.add(allele);
			}
		}

		Assert.assertEquals(allelesExpected, allelesReal);
	}

	@Test
	public void test_02_Annotation_SpliceRegion() {
		Gpr.debug("Test");
		String vcfFile = "tests/test_ann_02.vcf";

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000374221");

		if (verbose) Gpr.debug(veff);

		//---
		// Check results
		//---

		// Allele
		Assert.assertEquals("A", veff.getAllele());

		// Annotataion
		Assert.assertEquals("splice_region_variant&intron_variant", veff.getEffectsStrSo());
		Assert.assertEquals("SPLICE_SITE_REGION&INTRON", veff.getEffectsStr());

		// Impact
		Assert.assertEquals("LOW", veff.getImpact().toString());

		// Gene name / ID
		Assert.assertEquals("UBXN11", veff.getGeneName());
		Assert.assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		Assert.assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		Assert.assertEquals("ENST00000374221", veff.getFeatureId());
		Assert.assertEquals("ENST00000374221", veff.getTranscriptId());

		// Biotype
		Assert.assertEquals("protein_coding", veff.getBioType());

		// Rank
		Assert.assertEquals("11", "" + veff.getRank());
		Assert.assertEquals("15", "" + veff.getRankMax());

		// HGVS
		Assert.assertEquals("c.853-3C>T", veff.getHgvsDna());
		Assert.assertEquals("", veff.getHgvsProt());

		// cDNA position
		Assert.assertEquals("-1", "" + veff.getcDnaPos());
		Assert.assertEquals("-1", "" + veff.getcDnaLen());

		// CDS position
		Assert.assertEquals("-1", "" + veff.getCdsPos());
		Assert.assertEquals("-1", "" + veff.getCdsLen());

		// AA position
		Assert.assertEquals("-1", "" + veff.getAaPos());
		Assert.assertEquals("-1", "" + veff.getAaLen());

		// Warning
		Assert.assertEquals("", veff.getErrorsWarning());
	}

	@Test
	public void test_03_Annotation_NonSyn() {
		Gpr.debug("Test");
		String vcfFile = "tests/test_ann_03.vcf";

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, "ENST00000374221");

		if (verbose) Gpr.debug(veff);

		//---
		// Check results
		//---

		// Allele
		Assert.assertEquals("A", veff.getAllele());

		// Annotataion
		Assert.assertEquals("missense_variant&splice_region_variant", veff.getEffectsStrSo());
		Assert.assertEquals("NON_SYNONYMOUS_CODING&SPLICE_SITE_REGION", veff.getEffectsStr());

		// Impact
		Assert.assertEquals("MODERATE", veff.getImpact().toString());

		// Gene name / ID
		Assert.assertEquals("UBXN11", veff.getGeneName());
		Assert.assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		Assert.assertEquals("transcript", veff.getFeatureType());

		// FeatureId / transcriptId
		Assert.assertEquals("ENST00000374221", veff.getFeatureId());
		Assert.assertEquals("ENST00000374221", veff.getTranscriptId());

		// Biotype
		Assert.assertEquals("protein_coding", veff.getBioType());

		// Rank
		Assert.assertEquals("12", "" + veff.getRank());
		Assert.assertEquals("16", "" + veff.getRankMax());

		// HGVS
		Assert.assertEquals("c.853C>T", veff.getHgvsDna());
		Assert.assertEquals("p.Val285Leu", veff.getHgvsProt());

		// cDNA position
		Assert.assertEquals("1067", "" + veff.getcDnaPos());
		Assert.assertEquals("1792", "" + veff.getcDnaLen());

		// CDS position
		Assert.assertEquals("853", "" + veff.getCdsPos());
		Assert.assertEquals("1563", "" + veff.getCdsLen());

		// AA position
		Assert.assertEquals("285", "" + veff.getAaPos());
		Assert.assertEquals("520", "" + veff.getAaLen());

		// Warning
		Assert.assertEquals("WARNING_REF_DOES_NOT_MATCH_GENOME", veff.getErrorsWarning());
	}

	@Test
	public void test_04_Annotation_Intergenic() {
		Gpr.debug("Test");
		String vcfFile = "tests/test_ann_04.vcf";

		// Annotate
		VcfEffect veff = annotateFirst(vcfFile, null);

		if (verbose) Gpr.debug(veff);

		//---
		// Check results
		//---

		// Allele
		Assert.assertEquals("A", veff.getAllele());

		// Annotataion
		Assert.assertEquals("intergenic_region", veff.getEffectsStrSo());
		Assert.assertEquals("INTERGENIC", veff.getEffectsStr());

		// Impact
		Assert.assertEquals("MODIFIER", veff.getImpact().toString());

		// Gene name / ID
		Assert.assertEquals("UBXN11", veff.getGeneName());
		Assert.assertEquals("ENSG00000158062", veff.getGeneId());

		// Feature type
		Assert.assertEquals("intergenic_region", veff.getFeatureType());

		// FeatureId / transcriptId
		Assert.assertEquals("ENSG00000158062", veff.getFeatureId());
		Assert.assertEquals("", veff.getTranscriptId());

		// Biotype
		Assert.assertEquals("", veff.getBioType());

		// Rank
		Assert.assertEquals("-1", "" + veff.getRank());
		Assert.assertEquals("-1", "" + veff.getRankMax());

		// HGVS
		Assert.assertEquals("n.26600818G>A", veff.getHgvsDna());
		Assert.assertEquals("", veff.getHgvsProt());

		// cDNA position
		Assert.assertEquals("-1", "" + veff.getcDnaPos());
		Assert.assertEquals("-1", "" + veff.getcDnaLen());

		// CDS position
		Assert.assertEquals("-1", "" + veff.getCdsPos());
		Assert.assertEquals("-1", "" + veff.getCdsLen());

		// AA position
		Assert.assertEquals("-1", "" + veff.getAaPos());
		Assert.assertEquals("-1", "" + veff.getAaLen());

		// Warning
		Assert.assertEquals("", veff.getErrorsWarning());
	}

	@Test
	public void test_05_Annotation_EndOfChromosome() {
		Gpr.debug("Test");

		// Create a variant: Insertion after last chromosome base
		Variant variant = new Variant(genome.getChromosome("1"), 2001, "", "TTT", "");
		VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

		// Check output
		if (verbose) System.out.println("Number of effects: " + veffs.size());
		Assert.assertEquals(1, veffs.size());

		VariantEffect veff = veffs.get(0);
		if (verbose) System.out.println("Effect type : " + veff.getEffectType());
		Assert.assertEquals(EffectType.CHROMOSOME_ELONGATION, veff.getEffectType());
	}

}
