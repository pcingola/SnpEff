package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdBuild;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGenBank;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;

/**
 * Test case
 */
public class TestCasesCircularGenome {

	boolean verbose = false;

	public TestCasesCircularGenome() {
		super();
	}

	/**
	 * Build a genome from a genbank file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildGeneBank(String genome, String genBankFile) {
		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGenBank sepfg = new SnpEffPredictorFactoryGenBank(config, genBankFile);
		sepfg.setVerbose(verbose);

		// Build
		SnpEffectPredictor sep = sepfg.create();
		return sep;
	}

	public SnpEffectPredictor buildGtf(String genome) {
		// Build
		String args[] = { "build", genome };

		SnpEff snpeff = new SnpEff(args);
		snpeff.setVerbose(verbose);

		SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.snpEffCmd();
		snpeffBuild.run();
		return snpeffBuild.getConfig().getSnpEffectPredictor();
	}

	@Test
	public void testCase_01_CircularGenome() {
		Gpr.debug("Test");

		// Create database & build interval forest
		String genomeName = "testCase";
		String genBankFile = "tests/genes_circular.gbk";
		SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile);
		sep.buildForest();

		// Create variant
		Genome genome = sep.getGenome();
		Variant var = new Variant(genome.getChromosome("chr"), 2, "", "TATTTTTCAG", "");

		// Calculate effect
		// This should NOT throw an exception ("Interval has negative coordinates.")
		VariantEffects varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);
		}
	}

	@Test
	public void testCase_02_CircularGenome() {
		Gpr.debug("Test");

		// Create database & build interval forest
		String genomeName = "test_circular_GCA_000210475.1.22";
		SnpEffectPredictor sep = buildGtf(genomeName);
		sep.buildForest();

		// Create variant
		Genome genome = sep.getGenome();
		Variant var = new Variant(genome.getChromosome("chr"), 2, "", "TATTTTTCAG", "");

		// Calculate effect
		// This should NOT throw an exception ("Interval has negative coordinates.")
		VariantEffects varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);
		}
	}

}
