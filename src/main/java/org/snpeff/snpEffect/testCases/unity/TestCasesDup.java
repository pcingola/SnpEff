package org.snpeff.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.HgvsDna;
import org.snpeff.snpEffect.HgvsProtein;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

/**
 * Test case for structural variants: Duplications
 */
public class TestCasesDup extends TestCasesBase {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	public TestCasesDup() {
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
	 * Duplication Whole gene
	 */
	@Test
	public void test01_dupGene() {
		Gpr.debug("Test");

		// Create variant
		Variant variant = new Variant(chromosome, 950, 1250, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.GENE_DUPLICATION };
		String expHgvsc[] = null;
		EffectImpact expectedImpact = EffectImpact.LOW;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication whole transcript
	 */
	@Test
	public void test01_dupTr() {
		Gpr.debug("Test");

		// Create variant
		// Note that Gene ends at 1216, so this variant covers transcript (but not gene)
		Variant variant = new Variant(chromosome, 950, 1200, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.TRANSCRIPT_DUPLICATION };
		String expHgvsc[] = null;
		EffectImpact expectedImpact = EffectImpact.LOW;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication One coding exon
	 */
	@Test
	public void test02() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1100, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION, EffectType.FRAME_SHIFT };
		String expHgvsc[] = { "c.33-5_45+43dup" };
		String expHgvsp[] = { "p.Arg16fs", "p.Arg16_Gly18delinsCysValAlaAsnTerThrTrp" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, expHgvsp, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication two coding exons (within the same gene)
	 */
	@Test
	public void test03() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1160, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION };
		String expHgvsc[] = { "c.33-5_*3dup" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Part of one coding exon
	 */
	@Test
	public void test04() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1040, 1050, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION_PARTIAL };
		String expHgvsc[] = { "c.33-5_38dup" };
		String expHgvsp[] = { "p.Val12_Leu13insPheVal" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, expHgvsp, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Part of two coding exons (within the same gene)
	 */
	@Test
	public void test05() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 1150, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION_PARTIAL };
		String expHgvsc[] = { "c.38_48dup" };
		String expHgvsp[] = { "p.Arg16_???19delinsCysTerLeuGluAspMetAsp" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, expHgvsp, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Two genes
	 */
	@Test
	public void test06() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 2150, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION //
		, EffectType.EXON_DUPLICATION_PARTIAL //
		, EffectType.TRANSCRIPT_DUPLICATION //
		, EffectType.GENE_FUSION //
		};
		String expHgvsc[] = { "n.1051_2151dup" };
		EffectImpact expectedImpact = EffectImpact.LOW;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication After gene's coding region (LOW impact)
	 */
	@Test
	public void test07() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1100, 2000, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION };
		String expHgvsc[] = { "n.1101_2001dup" };
		EffectImpact expectedImpact = EffectImpact.LOW;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Part of two genes cutting on introns
	 */
	@Test
	public void test08() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1100, 2075, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION //
		, EffectType.FRAME_SHIFT //
		, EffectType.GENE_FUSION //
		};
		String expHgvsc[] = { "n.1101_2076dup" };
		String expHgvsp[] = { "p.Ser2_Leu10delinsTyrPheProPheThrProThrSerAlaAla???", "p.Ser2fs" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, expHgvsp, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Part of two genes cutting exons
	 */
	@Test
	public void test09() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 1050, 2120, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.EXON_DUPLICATION //
		, EffectType.EXON_DUPLICATION_PARTIAL //
		, EffectType.GENE_FUSION //
		};
		String expHgvsc[] = { "n.1051_2121dup", "c.38_*963dup", "c.-1016_15dup" };
		String expHgvsp[] = { "p.Pro6_Arg7delinsTyrAlaHisValLeuProPhe" };
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, expHgvsp, expHgvsc, expectedImpact, null);
	}

	/**
	 * Duplication Intron
	 */
	@Test
	public void test10() {
		Gpr.debug("Test");

		Variant variant = new Variant(chromosome, 991, 1020, "");
		variant.setVariantType(VariantType.DUP);

		EffectType expEffs[] = { EffectType.INTRON };
		String expHgvsc[] = { "c.32+3_33-25dup" };
		EffectImpact expectedImpact = EffectImpact.MODIFIER;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

}
