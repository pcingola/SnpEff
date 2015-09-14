package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;

/**
 * Test case
 */
public class TestCasesIntegrationCircularGenome extends TestCasesIntegrationBase {

	public TestCasesIntegrationCircularGenome() {
		super();
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

		//---
		// Create database & build interval forest
		//---
		String genomeName = "test_circular_GCA_000210475.1.22";
		SnpEffectPredictor sep = build(genomeName);
		sep.buildForest();

		if (verbose) {
			Genome genome = sep.getGenome();
			for (Chromosome chr : genome.getChromosomes())
				System.out.println(chr);
		}

		//---
		// Check variants in zero or negative coordiantes
		//---
		checkAnnotations(sep, "p948", 0, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
		checkAnnotations(sep, "p948", -3, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
		checkAnnotations(sep, "p948", -885, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");

		//---
		// Check variant after chromosome end (same variants as before)
		//---
		checkAnnotations(sep, "p948", 94797, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
		checkAnnotations(sep, "p948", 94794, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
		checkAnnotations(sep, "p948", 93912, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");
	}

	@Test
	public void testCase_02_CircularGenome_end() {
		Gpr.debug("Test");

		//---
		// Create database & build interval forest
		//---
		String genomeName = "test_circular_GCA_000210475.1.22_end";
		SnpEffectPredictor sep = build(genomeName);
		sep.buildForest();

		if (verbose) {
			Genome genome = sep.getGenome();
			for (Chromosome chr : genome.getChromosomes())
				System.out.println(chr);
		}

		//---
		// Check variants in zero or negative coordiantes
		//---
		checkAnnotations(sep, "p948", 0, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
		checkAnnotations(sep, "p948", -3, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
		checkAnnotations(sep, "p948", -885, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");

		//---
		// Check variant after chromosome end (same variants as before)
		//---
		checkAnnotations(sep, "p948", 94797, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
		checkAnnotations(sep, "p948", 94794, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
		checkAnnotations(sep, "p948", 93912, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");
	}

}
