package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	//	boolean debug = false;
	//	boolean verbose = false || debug;

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
			if (trId == null || veff.getTranscriptId().equals(trId)) return veff;
		}

		throw new RuntimeException("Transcript '" + trId + "' not found");
	}

	//	@Test
	//	public void test_04_Annotation_Intergenic() {
	//		Gpr.debug("Test");
	//		String vcfFile = "tests/test_ann_04.vcf";
	//
	//		// Annotate
	//		VcfEffect veff = annotateFirst(vcfFile, null);
	//
	//		if (verbose) Gpr.debug(veff);
	//
	//		//---
	//		// Check results
	//		//---
	//
	//		// Allele
	//		Assert.assertEquals("A", veff.getAllele());
	//
	//		// Annotataion
	//		Assert.assertEquals("intergenic_region", veff.getEffectsStrSo());
	//		Assert.assertEquals("INTERGENIC", veff.getEffectsStr());
	//
	//		// Impact
	//		Assert.assertEquals("MODIFIER", veff.getImpact().toString());
	//
	//		// Gene name / ID
	//		Assert.assertEquals("UBXN11", veff.getGeneName());
	//		Assert.assertEquals("ENSG00000158062", veff.getGeneId());
	//
	//		// Feature type
	//		Assert.assertEquals("intergenic_region", veff.getFeatureType());
	//
	//		// FeatureId / transcriptId
	//		Assert.assertEquals("ENSG00000158062", veff.getFeatureId());
	//		Assert.assertEquals("", veff.getTranscriptId());
	//
	//		// Biotype
	//		Assert.assertEquals("", veff.getBioType());
	//
	//		// Rank
	//		Assert.assertEquals("-1", "" + veff.getRank());
	//		Assert.assertEquals("-1", "" + veff.getRankMax());
	//
	//		// HGVS
	//		Assert.assertEquals("n.26600818G>A", veff.getHgvsDna());
	//		Assert.assertEquals("", veff.getHgvsProt());
	//
	//		// cDNA position
	//		Assert.assertEquals("-1", "" + veff.getcDnaPos());
	//		Assert.assertEquals("-1", "" + veff.getcDnaLen());
	//
	//		// CDS position
	//		Assert.assertEquals("-1", "" + veff.getCdsPos());
	//		Assert.assertEquals("-1", "" + veff.getCdsLen());
	//
	//		// AA position
	//		Assert.assertEquals("-1", "" + veff.getAaPos());
	//		Assert.assertEquals("-1", "" + veff.getAaLen());
	//
	//		// Warning
	//		Assert.assertEquals("", veff.getErrorsWarning());
	//	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	@Test
	public void test_15_hgvs_INS_intergenic() {
		verbose = true;

		Gpr.debug("Test");
		String genome = "testHg3775Chr22";
		String vcf = "tests/test_hgvs_INS_intergenic.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setFormatVersion(EffFormatVersion.FORMAT_ANN_1);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure the HCVGs annotaion is correct
		boolean ok = false;
		for (VcfEffect veff : ve.parseEffects()) {
			if (verbose) System.out.println("\t" + veff + "\t" + veff.getEffectsStr() + "\t" + veff.getHgvsDna());
			ok |= veff.hasEffectType(EffectType.INTERGENIC) //
					&& veff.getHgvsDna().equals("n.15070000_15070001insT") //
			;
		}

		Assert.assertTrue("Error in HGVS annotaiton", ok);
	}

}
