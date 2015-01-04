package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
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
			if (trId == null || veff.getTranscriptId().equals(trId)) return veff;
		}

		throw new RuntimeException("Transcript '" + trId + "' not found");
	}

	@Test
	public void test_10_GATK() {
		throw new RuntimeException("ANN: Check -o GATK works OK and no '&' are added into effect field");
	}

}
