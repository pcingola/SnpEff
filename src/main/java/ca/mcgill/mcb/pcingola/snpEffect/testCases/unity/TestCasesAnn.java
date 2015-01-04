package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test cases for 'ANN' format
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
	public void test_01_Annotation() {
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

}
