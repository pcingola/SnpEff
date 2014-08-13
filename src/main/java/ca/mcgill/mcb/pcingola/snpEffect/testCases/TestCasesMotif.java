package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test Motif databases
 *
 * @author pcingola
 */
public class TestCasesMotif extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false || debug;
	public static int SHOW_EVERY = 10;

	public TestCasesMotif() {
		super();
	}

	void checkMotif(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact) {
		String args[] = { "-classic", "-motif", "-ud", "0", genomeVer, vcfFile };
		SnpEff cmd = new SnpEff(args);

		// Run
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Check results
		int numNextProt = 0;
		for (VcfEntry ve : vcfEntries) {
			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t" + veff);
				if ((veff.getEffect() == EffectType.MOTIF) // Is it motif?
						&& effectDetails.equals(veff.getEffectDetails()) // Are details OK?
						&& (impact == veff.getImpact())) // Is impact OK?
					numNextProt++;
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	public void test_01() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_01.vcf", "MA0099.2:AP1", EffectImpact.LOW);
	}

	public void test_02() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_02.vcf", "MA0099.2:AP1", EffectImpact.MODIFIER);
	}

	public void test_03() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_03.vcf", "MA0099.2:AP1", EffectImpact.LOW);
	}

}
