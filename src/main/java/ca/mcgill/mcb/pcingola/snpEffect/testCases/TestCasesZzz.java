package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
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
		for (VcfEffect veff : ve.parseEffects()) {
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
			if (veff.getTranscriptId().equals(trId)) return veff;
		}

		throw new RuntimeException("Transcriot '" + trId + "' not found");
	}

	@Test
	public void test_03_Annotation() {
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

	//	@Test
	//	public void test_02_Allele_Cancer() {
	//		throw new RuntimeException("ANN: Check Allele in cancer sample & parsed correctly");
	//	}
	//
	//	@Test
	//	public void test_03_GeneName_Intergenic() {
	//		throw new RuntimeException("ANN: Check that closest gene name is correctly added & parsed correctly");
	//	}
	//
	//	@Test
	//	public void test_05_FeatureType() {
	//		throw new RuntimeException("ANN: Check feature type 'custom' & parsed correctly");
	//	}
	//
	//	@Test
	//	public void test_06_FeatureType() {
	//		throw new RuntimeException("ANN: Check feature type 'regulation', check that 'cell_type' is added & parsed correctly");
	//	}
	//
	//	@Test
	//	public void test_10_GATK() {
	//		throw new RuntimeException("ANN: Check -o GATK works OK and no '&' are added into effect field");
	//	}

}
