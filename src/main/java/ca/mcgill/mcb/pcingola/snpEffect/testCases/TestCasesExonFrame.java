package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
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
public class TestCasesExonFrame extends TestCase {

	public TestCasesExonFrame() {
		super();
	}

	/**
	 * Test database: Build, check and annotate
	 */
	public void test_01() {
		Gpr.debug("Test");
		//---
		// Build database
		//---
		String genomeName = "testLukas";
		String args[] = { "build", "-v", "-noLog", "-gff3", genomeName };

		SnpEff snpEff = new SnpEff(args);
		boolean ok = snpEff.run();

		Assert.assertTrue(ok);

		//---
		// Load database and check some numbers
		//---
		String configFile = Config.DEFAULT_CONFIG_FILE;
		Config config = new Config(genomeName, configFile);
		System.out.println("Loading database");
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
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Analyze annotations
		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve.toStringNoGt());

			String expectedEffect = ve.getInfo("EXP_EFF");
			String expectedAa = ve.getInfo("EXP_AA");
			String expectedCodon = ve.getInfo("EXP_CODON");

			boolean found = false;
			for (VcfEffect veff : ve.parseEffects()) {
				System.out.println("\t" + veff);
				String eff = veff.getEffect().toString();

				System.out.println("\t\tExpecing: '" + expectedEffect + "'\tFound: '" + eff + "'");
				System.out.println("\t\tExpecing: '" + expectedAa + "'\tFound: '" + veff.getAa() + "'");
				System.out.println("\t\tExpecing: '" + expectedCodon + "'\tFound: '" + veff.getCodon() + "'");

				// Effect matches expected?
				if (expectedEffect.equals(eff) //
						&& ((veff.getAa() == null) || expectedAa.equals(veff.getAa())) //
						&& ((veff.getCodon() == null) || expectedCodon.equals(veff.getCodon())) //
				) //
					found = true;
			}

			if (!found) throw new RuntimeException("Cannot find expected effect '" + expectedEffect + "', amino acid change '" + expectedAa + "' and codon change '" + expectedCodon + "'");
		}
	}

}
