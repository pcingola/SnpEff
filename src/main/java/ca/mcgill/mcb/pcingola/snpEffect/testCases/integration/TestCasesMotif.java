package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

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
				if ((veff.getEffectType() == EffectType.MOTIF) // Is it motif?
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

	/**
	 * MNP outside Motif: Should not throw any exception
	 */
	public void test_04() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr11";
		String vcf = "tests/craig_chr11.vcf";

		String args[] = { "-noLog", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);
		snpEff.run();
	}

	/**
	 * Motif has 9 bases but ENSEMBL file marks it as a 10 base interval
	 * SNP affect last base (as marked by ENSEMBL), since there is no sequence for that base position, an exception is thrown.
	 */
	public void test_05() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr14";
		String vcf = "tests/craig_chr14.vcf";

		String args[] = { "-noLog", genome, vcf };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		snpEff.setDebug(debug);
		snpEff.run();
	}

}
