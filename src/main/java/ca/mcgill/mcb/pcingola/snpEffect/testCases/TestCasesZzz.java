package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	void checkNextProt(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact, boolean useAnn) {
		String args[] = { "-classic", "-nextProt", genomeVer, vcfFile };
		String argsAnn[] = { genomeVer, vcfFile };
		if (useAnn) args = argsAnn;

		SnpEff cmd = new SnpEff(args);
		cmd.setVerbose(verbose);
		cmd.setSupressOutput(!verbose);

		// Run
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Check results
		int numNextProt = 0;
		for (VcfEntry ve : vcfEntries) {
			for (VcfEffect veff : ve.parseEffects()) {

				if ((veff.hasEffectType(EffectType.NEXT_PROT)) // Is it nextProt?
						&& (impact == veff.getImpact())) // Is impact OK?
				{
					// Are details OK?
					boolean match = false;
					if (!useAnn && effectDetails.equals(veff.getEffectDetails())) match = true;
					if (useAnn && effectDetails.equals(veff.getFeatureType())) match = true;

					if (match) numNextProt++;
				}

				if (verbose) //
					System.out.println("\t" + veff //
							+ "\n\t\tEffect            : " + veff.getVcfFieldString() //
							+ "\n\t\tEffect type       : " + veff.getEffectType() //
							+ "\n\t\tEffect details    : '" + veff.getEffectDetails() + "'" //
							+ "\n\t\tEffect impact     : '" + veff.getImpact() + "'" //
							+ "\n\t\tExpected details  : '" + effectDetails + "'" //
							+ "\n\t\tExpected impact   : '" + impact + "'" //
							+ "\n\t\tCount matches     : " + numNextProt //
							+ "\thasEffectType : " + veff.hasEffectType(EffectType.NEXT_PROT) //
							+ "\tmatch details : " + effectDetails.equals(veff.getEffectDetails()) //
							+ "\tmatch impact: " + (impact == veff.getImpact()) //
					);
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	@Test
	public void test_03_ann() {
		verbose = true;
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'MODERATE' impact, but since the database we build in test_01_build is small, there are not enough stats.
		checkNextProt("testHg3770Chr22", "tests/test_nextProt_03.vcf", "amino_acid_modification:Phosphoserine", EffectImpact.MODERATE, true);
	}

}
