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
	boolean verbose = false || debug;

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

	void checkAnnotations(SnpEffectPredictor sep, String chr, int pos, String ref, String alt, String hgvsP, String hgvsC, String eff) {
		Genome genome = sep.getGenome();
		Variant var = new Variant(genome.getChromosome(chr), pos, ref, alt, "");
		VariantEffects varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals(hgvsP, vcfEff.getHgvsProt());
			Assert.assertEquals(hgvsC, vcfEff.getHgvsDna());
			Assert.assertEquals(eff, vcfEff.getEffectsStrSo());
		}
	}

	@Test
	public void testCase_02_CircularGenome() {
		Gpr.debug("Test");

		//---
		// Create database & build interval forest
		//---
		String genomeName = "test_circular_GCA_000210475.1.22";
		SnpEffectPredictor sep = buildGtf(genomeName);
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
