package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsProtein;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	@Override
	protected void init() {
		randSeed = 20151205;
		genomeName = "testCase";
		addUtrs = false;
		onlyPlusStrand = true;
		onlyMinusStrand = false;
		numGenes = 2;
		maxGeneLen = 1000;
		maxTranscripts = 1;
		maxExons = 5;
		minExons = 2;
		shiftHgvs = false;

		initRand();
	}

	/**
	 * Inversion: Whole gene
	 */
	@Test
	public void test01() {
		initSnpEffPredictor();

		for (Gene g : genome.getGenes())
			Gpr.debug("\tGene: " + g);

		Variant variant = new Variant(chromosome, 950, 1200, "");
		variant.setVariantType(VariantType.INV);

		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

		// Checknumber of results
		Assert.assertEquals(true, effects.size() >= 1);

		for (VariantEffect eff : effects) {
			Gpr.debug("\t" + eff);
			HgvsDna hgvsc = new HgvsDna(eff);
			String hgvsDna = hgvsc.toString();
			HgvsProtein hgvsp = new HgvsProtein(eff);
			String hgvsProt = hgvsp.toString();

			Gpr.debug("Effect: " + eff.getEffectTypeString(false) //
					+ "\n\tHGVS.c: " + hgvsDna //
					+ "\n\tHGVS.p: " + hgvsProt //
			);
			//			if (expectedHgvsC != null) Assert.assertEquals("HGVS.c notation does not match", expectedHgvsC, hgvsDna);
			//			if (expectedHgvsP != null) Assert.assertEquals("HGVS.p notation does not match", expectedHgvsP, hgvsProt);
		}
	}

	//	/**
	//	 * Inversion: One coding exon
	//	 */
	//	@Test
	//	public void test02() {
	//		initSnpEffPredictor();
	//
	//		Variant variant = new Variant(chromosome, 1040, 1100, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//	 * Inversion: Two coding exons
	//	 */
	//	@Test
	//	public void test03() {
	//		initSnpEffPredictor();
	//
	//		Variant variant = new Variant(chromosome, 1040, 1160, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//	 * Inversion: Part of one coding exon
	//	 */
	//	@Test
	//	public void test04() {
	//		initSnpEffPredictor();
	//
	//		Variant variant = new Variant(chromosome, 1040, 1050, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//	 * Inversion: Part of two coding exon
	//	 */
	//	@Test
	//	public void test05() {
	//		initSnpEffPredictor();
	//
	//		Variant variant = new Variant(chromosome, 1050, 1150, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//	 * Inversion: Two genes
	//	 */
	//	@Test
	//	public void test06() {
	//		initSnpEffPredictor();
	//
	//		Variant variant = new Variant(chromosome, 1050, 2150, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//		 * Inversion: Part of two genes (fusions) cutting on introns
	//		 */
	//	@Test
	//	public void test07() {
	//		initSnpEffPredictor();
	//		Variant variant = new Variant(chromosome, 1100, 2075, "");
	//		variant.setVariantType(VariantType.INV);
	//	}
	//
	//	/**
	//		 * Inversion: Part of two genes (fusions) cutting exons
	//		 */
	//	@Test
	//	public void test08() {
	//		initSnpEffPredictor();
	//		Variant variant = new Variant(chromosome, 1050, 2120, "");
	//		variant.setVariantType(VariantType.INV);
	//	}

}
