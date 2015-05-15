package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdBuild;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
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
	public void testCase_02_CircularGenome() {
		Gpr.debug("Test");

		//---
		// Create database & build interval forest
		//---
		String genomeName = "test_circular_GCA_000210475.1.22";
		SnpEffectPredictor sep = buildGtf(genomeName);
		// sep.setUpDownStreamLength(0);
		sep.buildForest();

		//---
		// Check variant:1
		//---
		Genome genome = sep.getGenome();
		for (Chromosome chr : genome.getChromosomes())
			System.out.println(chr);

		Variant var = new Variant(genome.getChromosome("p948"), 0, "T", "A", "");
		VariantEffects varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Phe297Ile", vcfEff.getHgvsProt());
			Assert.assertEquals("c.889T>A", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

		//---
		// Check variant: 2
		//---
		var = new Variant(genome.getChromosome("p948"), -3, "T", "A", "");
		varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Trp296Arg", vcfEff.getHgvsProt());
			Assert.assertEquals("c.886T>A", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

		//---
		// Check variant: 3
		//---
		var = new Variant(genome.getChromosome("p948"), -885, "G", "T", "");
		varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Asp2Tyr", vcfEff.getHgvsProt());
			Assert.assertEquals("c.4G>T", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

		//---
		// Check variant: 4 (same as variant 1)
		//---
		var = new Variant(genome.getChromosome("p948"), 94797, "T", "A", "");
		varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Phe297Ile", vcfEff.getHgvsProt());
			Assert.assertEquals("c.889T>A", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

		//---
		// Check variant: 5 (same as variant 2)
		//---
		var = new Variant(genome.getChromosome("p948"), 94794, "T", "A", "");
		varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Trp296Arg", vcfEff.getHgvsProt());
			Assert.assertEquals("c.886T>A", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

		//---
		// Check variant: 4 (same as variant 3)
		//---
		var = new Variant(genome.getChromosome("p948"), 93912, "G", "T", "");
		varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals("p.Asp2Tyr", vcfEff.getHgvsProt());
			Assert.assertEquals("c.4G>T", vcfEff.getHgvsDna());
			Assert.assertEquals("missense_variant", vcfEff.getEffectsStrSo());
		}

	}

}
