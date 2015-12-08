package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsDna;
import ca.mcgill.mcb.pcingola.snpEffect.HgvsProtein;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;

/**
 * Test case
 */
public class TestCasesInv extends TestCasesBase {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	public TestCasesInv() {
		super();
	}

	Set<String> arrayToSet(String array[]) {
		Set<String> set = new HashSet<>();
		if (array != null) {
			for (String h : array)
				set.add(h);
		}
		return set;
	}

	protected void checkEffects(Variant variant, EffectType expEffs[], String expHgvsp[], String expHgvsc[], EffectImpact expectedImpact, String expAnns[]) {
		// Convert to sets
		Set<EffectType> expectedEffs = new HashSet<>();
		if (expEffs != null) {
			for (EffectType et : expEffs)
				expectedEffs.add(et);
		}

		Set<String> expectedHgvsp = arrayToSet(expHgvsp);
		Set<String> expectedHgvsc = arrayToSet(expHgvsc);
		Set<String> expectedAnns = arrayToSet(expAnns);

		// Initialize
		initSnpEffPredictor();

		if (verbose) {
			Gpr.debug("Variant: " + variant);
			for (Gene g : genome.getGenes()) {
				Gpr.debug("\tGene: " + g.getId());
				for (Transcript tr : g)
					Gpr.debug(tr + "\n\n" + tr.toStringAsciiArt(true));
			}
		}

		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);
		if (verbose) Gpr.debug("VariantEffects: " + effects);

		// Checknumber of results
		Assert.assertEquals(true, effects.size() >= 1);

		Set<EffectType> effs = new HashSet<>();
		Set<String> hgvscs = new HashSet<>();
		Set<String> hgvsps = new HashSet<>();
		Set<String> anns = new HashSet<>();
		boolean impactOk = false;
		for (VariantEffect varEff : effects) {
			effs.addAll(varEff.getEffectTypes());

			HgvsDna hgvsc = new HgvsDna(varEff);
			String hgvsDna = hgvsc.toString();
			hgvscs.add(hgvsDna);

			HgvsProtein hgvsp = new HgvsProtein(varEff);
			String hgvsProt = hgvsp.toString();
			hgvsps.add(hgvsProt);

			impactOk |= (varEff.getEffectImpact() == expectedImpact);

			// Create VcfEffect
			VcfEffect vcfEffect = new VcfEffect(varEff, formatVersion);
			String annField = vcfEffect.toString();
			anns.add(annField);

			if (verbose) Gpr.debug("Effect: " + varEff.toStr() //
					+ "\n\tHGVS.c: " + hgvsDna //
					+ "\n\tHGVS.p: " + hgvsProt //
					+ "\n\tANN   : " + annField //
			);
		}

		// Check effects
		Assert.assertTrue("Effects do not match" //
				+ "\n\tExpected : " + expectedEffs //
				+ "\n\tFound    : " + effs//
				, effs.containsAll(expectedEffs) //
		);

		// Check impact
		Assert.assertTrue("Effect impact '" + expectedImpact + "' not found", impactOk);

		// Check HGVS.c
		Assert.assertTrue("HGVS.c do not match" //
				+ "\n\tExpected : " + expectedHgvsc //
				+ "\n\tFound    : " + hgvscs//
				, hgvscs.containsAll(expectedHgvsc) //
		);

		// Check HGVS.p
		Assert.assertTrue("HGVS.p do not match" //
				+ "\n\tExpected : " + expectedHgvsp //
				+ "\n\tFound    : " + hgvsps//
				, hgvsps.containsAll(expectedHgvsp) //
		);

		// Check ANN fields
		Assert.assertTrue("ANN fields do not match" //
				+ "\n\tExpected : " + expectedAnns //
				+ "\n\tFound    : " + anns //
				, anns.containsAll(expectedAnns) //
		);

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
	 * Inversion: Whole gene / whole transcript
	 */
	@Test
	public void test01() {
		Gpr.debug("Test");

		// Create variant
		Variant variant = new Variant(chromosome, 950, 1200, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.TRANSCRIPT_INVERSION };
		String expHgvsc[] = { "c.-7_*43inv" };
		EffectImpact expectedImpact = EffectImpact.LOW;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Inversion: One coding exon
	 */
	@Test
	public void test02() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1100, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.EXON_INVERSION };
		String expHgvsc[] = { "c.33-5_45+43inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Inversion: Two coding exons
	 */
	@Test
	public void test03() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1160, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.EXON_INVERSION };
		String expHgvsc[] = { "c.33-5_*3inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Inversion: Part of one coding exon
	 */
	@Test
	public void test04() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1050, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.EXON_INVERSION_PARTIAL };
		String expHgvsc[] = { "c.33-5_38inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Inversion: Part of two coding exon
	 */
	@Test
	public void test05() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 1150, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.EXON_INVERSION_PARTIAL };
		String expHgvsc[] = { "c.38_48inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Inversion: Part of two genes (fusions) cutting exons
	 */
	@Test
	public void test09() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 991, 1020, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.INTRON };
		String expHgvsc[] = { "c.32+3_33-25inv" };
		EffectImpact expectedImpact = EffectImpact.MODIFIER;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);

	}

	/**
	 * Inversion: Two genes
	 */
	@Test
	public void test06() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 2150, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.GENE_INVERSION, EffectType.GENE_FUSION };
		String expHgvsc[] = { "n.1051_2151inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;
		String expAnns[] = { //
				"|inversion|LOW|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1051_2151inv||||||" //
				, "|gene_fusion|HIGH|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1051_2151inv||||||" //
		};

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, expAnns);
	}

	/**
	 * Inversion: Part of two genes (fusions) cutting on introns
	 */
	@Test
	public void test07() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1100, 2075, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.GENE_INVERSION, EffectType.GENE_FUSION };
		String expHgvsc[] = { "n.1101_2076inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;
		String expAnns[] = { //
				"|inversion|LOW|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1101_2076inv||||||" //
				, "|gene_fusion|HIGH|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1101_2076inv||||||" //
		};

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, expAnns);
	}

	/**
	 * Inversion: Part of two genes (fusions) cutting exons
	 */
	@Test
	public void test08() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 2120, "");
		variant.setVariantType(VariantType.INV);

		EffectType expEffs[] = { EffectType.GENE_INVERSION, EffectType.GENE_FUSION };
		String expHgvsc[] = { "n.1051_2121inv" };
		EffectImpact expectedImpact = EffectImpact.HIGH;
		String expAnns[] = { //
				"|inversion|LOW|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1051_2121inv||||||" //
				, "|gene_fusion|HIGH|gene1&gene2|gene1&gene2|gene_variant|gene1|||n.1051_2121inv||||||" //
		};

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, expAnns);

	}

}
