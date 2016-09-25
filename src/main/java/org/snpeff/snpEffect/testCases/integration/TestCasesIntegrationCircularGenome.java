package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

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
		// Check variants in zero or negative coordinates
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
	public void testCase_03_CircularGenome() {
		Gpr.debug("Test");

		// Create database & build interval forest
		String genomeName = "test_Acetobacter_pasteurianus";
		SnpEffectPredictor sep = build(genomeName);
		Gene g = sep.getGene("DB34_00005");
		Transcript tr = g.subIntervals().iterator().next();
		Assert.assertEquals("Protein sequence differs", "MQTECSAGAYEFPASCGRRVVARFDGGRMSSDGGVILVKQADDILGLSRRFAACFRDKRHPGFVEYIPQSRDAAYRENRQQSGG*", tr.protein());
		Assert.assertEquals("CDS sequence differs", "ATGCAGACAGAGTGTAGCGCAGGCGCGTATGAGTTTCCAGCCTCCTGTGGACGGCGTGTTGTGGCCCGTTTTGACGGGGGTCGCATGAGTTCGGATGGGGGCGTCATTCTGGTGAAGCAGGCTGATGACATTCTGGGTCTCAGCCGCCGCTTTGCTGCCTGTTTTCGCGATAAGCGGCATCCCGGCTTTGTGGAATATATTCCACAAAGCCGGGATGCCGCTTATCGCGAAAACAGGCAGCAAAGCGGCGGCTGA", tr.cds().toUpperCase());
	}

}
