package org.snpeff.snpEffect.testCases;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.snpeff.interval.BioType;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantTranslocation;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.HgvsDna;
import org.snpeff.snpEffect.HgvsProtein;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

import junit.framework.Assert;

/**
 * Test case for structural variants: Translocation (fusions)
 * 
 * We create two genes (one transcript each). Each gene is in one different chromosome 
 * 
 * Transcripts:
 * 1:10-90, strand: +, id:tr1, Protein
 *      Exons:
 *      1:10-30 'exon1', rank: 1, frame: ., sequence: tatttgtatgaggatttgagt
 *      1:40-90 'exon2', rank: 2, frame: ., sequence: tactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      CDS     :   tatttgtatgaggatttgagttactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      Protein :   YLYEDLSYSVLGNPLAVAPLTLLF
 *
 * 2:110-190, strand: +, id:tr2, Protein
 *      Exons:
 *      2:110-125 'exon3', rank: 1, frame: ., sequence: gttaatgggatttcac
 *      2:150-190 'exon4', rank: 2, frame: ., sequence: atgggaacggagtgtcgacagcaccttatggggagctatat
 *      CDS     :   gttaatgggatttcacatgggaacggagtgtcgacagcaccttatggggagctatat
 *      Protein :   VNGISHGNGVSTAPYGELY */
public class TestCasesZzz {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	boolean debug = false;
	boolean verbose = false || debug;

	Random rand = new Random(20160229);
	Config config;
	String chr1Seq;
	String chr2Seq;
	Genome genome;
	Chromosome chr1, chr2;
	Gene gene1, gene2;
	Transcript tr1, tr2;
	SnpEffectPredictor snpEffectPredictor;

	public void init(boolean gene1NegativeStrand, boolean gene2NegativeStrand) {
		config = new Config("test");

		chr1Seq = "TGCTTGTCGATATTTGTATGAGGATTTGAGTACTACGCACTACTCAGTGCTGGGCAATCCCTTAGCTGTCGCGCCGCTTACCCTACTATTCAGGAGTAGGCCCTATCTCCACAGTGACTGTAGTACCAGCCATCTCTCTCGTTGCCGTCTGCGGTGCCGTCACACACGCTCCAGTCCCAGCTACGTTTCGCCAGGCTCAG";
		chr2Seq = "GCGATTGGTTGAATAAGCATAAGGTAGTTATCCGCCTGCACCTTGTTGAAAGATTGGACTTAATCCACCCCGTTAACAAAGGAATCGATCATGTTGCGCATATCGTCTAGGTTAATGGGATTTCACCGCTTACCCACTTAGCGGGCTGGAATGGGAACGGAGTGTCGACAGCACCTTATGGGGAGCTATATTCCCCCTAT";
		genome = new Genome("test");

		chr1 = new Chromosome(genome, 0, chr1Seq.length() - 1, "1");
		chr2 = new Chromosome(genome, 0, chr2Seq.length() - 1, "2");
		chr1.setSequence(chr1Seq);
		chr2.setSequence(chr2Seq);
		genome.add(chr1);
		genome.add(chr2);

		gene1 = new Gene(chr1, 10, 90, gene1NegativeStrand, "gene1", "gene1", BioType.protein_coding);
		gene2 = new Gene(chr2, 110, 190, gene2NegativeStrand, "gene2", "gene2", BioType.protein_coding);

		tr1 = new Transcript(gene1, gene1.getStart(), gene1.getEnd(), gene1.isStrandMinus(), "tr1");
		tr2 = new Transcript(gene2, gene2.getStart(), gene2.getEnd(), gene2.isStrandMinus(), "tr2");
		gene1.add(tr1);
		gene2.add(tr2);
		tr1.setProteinCoding(true);
		tr2.setProteinCoding(true);

		Exon e11 = new Exon(tr1, 10, 30, tr1.isStrandMinus(), "exon1", 0);
		Exon e12 = new Exon(tr1, 40, 90, tr1.isStrandMinus(), "exon2", 0);
		Exon e21 = new Exon(tr2, 110, 125, tr2.isStrandMinus(), "exon3", 0);
		Exon e22 = new Exon(tr2, 150, 190, tr2.isStrandMinus(), "exon4", 0);
		Exon exons[] = { e11, e12, e21, e22 };

		for (Exon e : exons) {
			String seq = e.getChromosome().getSequence().substring(e.getStart(), e.getEnd() + 1);
			if (e.isStrandMinus()) seq = GprSeq.reverseWc(seq);
			e.setSequence(seq);

			Transcript tr = (Transcript) e.getParent();
			tr.add(e);

			Cds cds = new Cds(tr, e.getStart(), e.getEnd(), e.isStrandMinus(), "");
			tr.add(cds);
		}
		tr1.rankExons();
		tr2.rankExons();

		if (verbose) System.out.println("Transcripts:\n" + tr1 + "\n" + tr2);

		snpEffectPredictor = new SnpEffectPredictor(genome);
		snpEffectPredictor.setUpDownStreamLength(0);
		snpEffectPredictor.add(gene1);
		snpEffectPredictor.add(gene2);
		snpEffectPredictor.buildForest();
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

		if (verbose) {
			Gpr.debug("Variant: " + variant);
			for (Gene g : genome.getGenes()) {
				Gpr.debug("\tGene: " + g.getId() + "\t" + gene1.getStart() + " - " + gene1.getEnd());
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

	public TestCasesZzz() {
		super();
	}

	/**
	 * Translocation in the same direction (both genes in positive strand)
	 */
	@Test
	public void test01() {
		Gpr.debug("Test");

		verbose = true;
		init(false, false);

		// Create variant
		VariantTranslocation variant = new VariantTranslocation(chr1, 35, "N", "N", chr2, 140, false, false);

		EffectType expEffs[] = { EffectType.GENE_DELETED };
		String expHgvsc[] = null;
		EffectImpact expectedImpact = EffectImpact.HIGH;

		checkEffects(variant, expEffs, null, expHgvsc, expectedImpact, null);
	}

}
