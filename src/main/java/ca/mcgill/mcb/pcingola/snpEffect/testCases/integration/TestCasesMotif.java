package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

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
 * Test Motif databases
 *
 * @author pcingola
 */
public class TestCasesMotif {

	public static boolean debug = false;
	public static boolean verbose = false || debug;
	public static int SHOW_EVERY = 10;

	public TestCasesMotif() {
		super();
	}

	void checkMotif(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact, boolean useAnn) {
		String args[] = { "-classic", "-motif", "-ud", "0", genomeVer, vcfFile };
		String argsAnn[] = { "-ud", "0", genomeVer, vcfFile };
		if (useAnn) args = argsAnn;

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
				if (verbose) System.out.println("\t" + veff.getVcfFieldString());

				// Is it motif?
				if (veff.getEffectType() == EffectType.MOTIF) {

					boolean ok = false;
					if (useAnn) {
						// Motif ID and impact match?
						ok = effectDetails.equals(veff.getFeatureId()) && (impact == veff.getImpact());
					} else {
						// Motif ID and impact match?
						ok = effectDetails.equals(veff.getEffectDetails()) && (impact == veff.getImpact());
					}

					if (ok) numNextProt++;
				}
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_01.vcf", "MA0099.2:AP1", EffectImpact.LOW, false);
	}

	@Test
	public void test_01_ann() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_01.vcf", "MA0099.2", EffectImpact.LOW, true);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_02.vcf", "MA0099.2:AP1", EffectImpact.MODIFIER, false);
	}

	@Test
	public void test_02_ann() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_02.vcf", "MA0099.2", EffectImpact.MODIFIER, true);
	}

	@Test
	public void test_03() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_03.vcf", "MA0099.2:AP1", EffectImpact.LOW, false);
	}

	@Test
	public void test_03_ann() {
		Gpr.debug("Test");
		checkMotif("testHg3770Chr22", "tests/test_motif_03.vcf", "MA0099.2", EffectImpact.LOW, true);
	}

	/**
	 * MNP outside Motif: Should not throw any exception
	 */
	@Test
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
	@Test
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
