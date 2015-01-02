package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case for exon frames
 *
 * @author pcingola
 */
public class TestCasesExonFrame {

	boolean verbose = false;

	public TestCasesExonFrame() {
		super();
	}

	/**
	 * Test database: Build, check and annotate
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		//---
		// Build database
		//---
		String genomeName = "testLukas";
		String args[] = { "build", "-noLog", "-gff3", genomeName };

		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertTrue(ok);

		//---
		// Load database and check some numbers
		//---
		String configFile = Config.DEFAULT_CONFIG_FILE;
		Config config = new Config(genomeName, configFile);
		if (verbose) System.out.println("Loading database");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		// Find transcript (there is only one)
		Transcript transcript = null;
		for (Gene gene : snpEffectPredictor.getGenome().getGenes())
			for (Transcript tr : gene)
				transcript = tr;

		// Check parameters
		Assert.assertEquals(454126, transcript.getCdsStart());
		Assert.assertEquals(450599, transcript.getCdsEnd());

		//---
		// Check annotations
		//---
		String vcfFileName = "tests/testLukas.vcf";
		String argsEff[] = { "-classic", "-ud", "0", genomeName, vcfFileName };

		// Annotate
		SnpEff cmd = new SnpEff(argsEff);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Analyze annotations
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.toStringNoGt());

			EffectType expectedEffect = EffectType.valueOf(ve.getInfo("EXP_EFF"));
			String expectedAa = ve.getInfo("EXP_AA");
			String expectedCodon = ve.getInfo("EXP_CODON");

			boolean found = false;
			for (VcfEffect veff : ve.parseEffects()) {
				String eff = veff.getEffectType().toString();

				if (verbose) {
					System.out.println("\t" + veff);
					System.out.println("\t\tExpecing: '" + expectedEffect + "'\tFound: '" + eff + "'");
					System.out.println("\t\tExpecing: '" + expectedAa + "'\tFound: '" + veff.getAa() + "'");
					System.out.println("\t\tExpecing: '" + expectedCodon + "'\tFound: '" + veff.getCodon() + "'");
				}

				// Effect matches expected?
				if (veff.hasEffectType(expectedEffect) //
						&& ((veff.getAa() == null) || expectedAa.equals(veff.getAa())) //
						&& ((veff.getCodon() == null) || expectedCodon.equals(veff.getCodon())) //
				) //
					found = true;
			}

			if (!found) throw new RuntimeException("Cannot find expected effect '" + expectedEffect + "', amino acid change '" + expectedAa + "' and codon change '" + expectedCodon + "'");
		}
	}

	/**
	 * Build genome (no exceptions should be thrown)
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		// Build database
		String genomeName = "testMacuminata";
		String args[] = { "build", "-noLog", genomeName };

		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertTrue(ok);
	}
}
