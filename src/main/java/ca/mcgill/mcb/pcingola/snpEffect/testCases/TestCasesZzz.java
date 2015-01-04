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
	public void test_02_Annotation() {
		Gpr.debug("Test");
		String vcfFile = "tests/test_ann_01.vcf";

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

	//	// cDNA position
	//	Assert.assertEquals("1915", "" + veff.getcDnaPos());
	//	Assert.assertEquals("2646", "" + veff.getcDnaLen());
	//
	//	// CDS position
	//	Assert.assertEquals("1915", "" + veff.getCdsPos());
	//	Assert.assertEquals("2646", "" + veff.getCdsLen());

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
	//	public void test_04_FeatureType() {
	//		throw new RuntimeException("ANN: Check feature type 'transcript' & parsed correctly");
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
	//	public void test_07_cDnaPos_cDnaLen() {
	//		throw new RuntimeException("ANN: Check that cDna pos / len are added & parse correctly");
	//	}
	//
	//	@Test
	//	public void test_08_CDS_CdsLen() {
	//		throw new RuntimeException("ANN: Check that CDS pos / len are added & parse correctly");
	//	}
	//
	//	@Test
	//	public void test_09_ProteinPos_ProteinLen() {
	//		throw new RuntimeException("ANN: Check that protein pos / len are added & parse correctly");
	//	}
	//
	//	@Test
	//	public void test_10_ProteinPos_ProteinLen() {
	//		throw new RuntimeException("ANN: Check -o GATK works OK and no '&' are added into effect field");
	//	}

}
