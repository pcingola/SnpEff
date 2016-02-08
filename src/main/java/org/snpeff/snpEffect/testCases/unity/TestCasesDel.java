package org.snpeff.snpEffect.testCases.unity;

import org.junit.Before;
import org.junit.Test;
import org.snpeff.interval.BioType;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.GprSeq;

import junit.framework.Assert;

/**
 * Test random DEL changes
 *
 * @author pcingola
 */
public class TestCasesDel {

	public static int N = 1000;

	boolean debug = false;
	boolean verbose = false || debug;

	Config config;
	String chrSeq;
	Genome genome;
	Chromosome chr;
	Gene gene;
	Transcript tr;
	SnpEffectPredictor snpEffectPredictor;

	@Before
	public void init() {
		config = new Config("test");

		chrSeq = "CGTGTTACCAAGCATTTGGGAACCGGAATTCTACGCTGGAGTTCGCCTAACAGCTAAAGCTCAAAACAGGAGTGGTTTAGTTCACAGGCAGACCTTTAACGGTACCTACCTTAATCCGGTCAGATTTAAATTCATGATAGATGGTCTAAGTCATGCACTACTCCTACCAGTTTTGGACGTGGCGCATTGGGCCCCGACACCAAATGCGTTGGAGCGACAGCACAAACGATATCTCAGATTAAGTGCATTGAAGGCCTTCTCACGGATAGATCTATAGGTCGCTGTGTGTCGGGAAGTCTTTCATCCGCTCGGGAGTCGGGTAAATTCTTTCTTCCAGTTGTGTGTTCTACGACTGCAGAGGACAGCTCTCGCTTAGTTTCCTTCCGTTCTTTGTAAGGCCCGATAGGAATACCCGCATAATTATCCCCACAGTCTAACTTGATCCACGTCATATGGGGCGATGGCTGGAATGAGGGGGGTTCAGTGGCAGACCACGATATCTCTGTTTATCTGATGAAGAATTTTGGATACCCCAATAACTAATGTGAGAAGAGTGTGGGGTATCCAACGTGATTGGAGGGAAGTATGGAAACGAATGAAGACAAGTTCGACAAGCCGTGGTAACGATCATATGCCACCGTATTCTAACGGGGGGGGGCCGCTGTTTGAATGCCAGATAGATATTTAAAGAGGATGGCCGCCCCCCAATGGGCACGAGGAACTGACCAGAGGTAGGTATGATGGTGTGAGAGAGAGATGTAGTGTAGTTACTATAAAGATTTGAAGATCGAAAGGCTTAGTGATACGAAACCTCCCGCTACTGCGGGCCGGTATAGCAGGTCTAATACTTGGTGATAGGGCTACATCGAGCCTCGTTCGACAAGAATACCCCAGTTGCAATGGGGACCGATTCAGTCTGGGGCTCTGTGGTCCGACATTATGCCGGAATGCCTAGTATGGGAATTAAAACGATTAATATTACCTCTGGGACTATCTTTGTGCCAAACTCTACTGAGAGAGGAACGTAATCGTATTAGGGGCCAAGATAGTTTACTCCGATCCACCGTAACACTTATTTTGGGCCGTGCGTACCCACCCACGGTCATGAACGCAAACTGGATAGGGCGCGTCATACGAGCTAAAGTGTTCTCTTGGGGCAAATAAGAAGGGACTAGCTGTATTCCCCTCCCATTGTGTGGGTACAATTCGGGGGAAAGGTGTCGAGCCCAAATGCTACGGCTCGTAGATATCAGGTCCTTTAGCCGCCCGTATGTAAGCACCTCCAACGTTTTGGAGGAAGATCCCTCTCCCACGGGTTATACCATTAATGACTCCGCACTAACAATATCTTACCAGGGCCCGCAAATACCACACCGGGATACTGGAATGAATTATTCTCCGACCCATTTGATGAAACGTATGGGAACTTTCCTGTTGTATTAAACCCGCGGATACACTCTCTTGCCCACGAGTAGCCTCTACTTACTAGATTGAGAGGTACACTACGCCATTATGAAGCATTTCGACTTTCACTCCAGTATATAGAAGGTATGTGTGGATCCTTCAAAATAGTGATCCGAACCTGGTTGTAGGGGCCACCGAAATTCCGATTACTGAAGATTAATGTTTTCAAATGCCCTATTTCACTGGTGATAGCGATGGAGCCGGGCTATCATCATGGCGAAGCACGTGGGAAAGCATTCCGTCAAGGCTAGTGGGAACCTCTGCCTTGCCATGTACGCGTTCTATATACACGAACATCGATACCGGTCTCGTCCTGGGGTAGAGCCATCCTCATCACGAGTGATGTAGGACGGCTAGCTCATTAATTCACTGCGTGTCAGATAGAATGTCTTCGTGCGCAAAAATTGTTTAGGAGACCGTCGGCGCTCCCTTGGAGAGATGCCCAACGTAGAGAGGCAATCCCCGGCGTATTGGTGGTTTCTGGAACGGAACGAGACTCTTTGTTGCGTCGACTCCCGGCAACACCGGCCCCCGCCG";
		genome = new Genome("test");

		chr = new Chromosome(genome, 0, chrSeq.length() - 1, "1");
		chr.setSequence(chrSeq);
		genome.add(chr);

		gene = new Gene(chr, 333, 408, true, "gene1", "gene1", BioType.protein_coding);

		tr = new Transcript(gene, 333, 408, true, "tr1");
		gene.add(tr);
		tr.setProteinCoding(true);

		Exon e1 = new Exon(tr, 333, 334, true, "exon1", 0);
		Exon e2 = new Exon(tr, 353, 364, true, "exon2", 0);
		Exon e3 = new Exon(tr, 388, 398, true, "exon3", 0);
		Exon e4 = new Exon(tr, 404, 408, true, "exon4", 0);
		Exon exons[] = { e1, e2, e3, e4 };

		for (Exon e : exons) {
			String seq = chrSeq.substring(e.getStart(), e.getEnd() + 1);
			seq = GprSeq.reverseWc(seq);
			e.setSequence(seq);
			tr.add(e);

			Cds cds = new Cds(tr, e.getStart(), e.getEnd(), e.isStrandMinus(), "");
			tr.add(cds);
		}
		tr.rankExons();

		if (verbose) System.out.println("Transcript:\n" + tr);
		Assert.assertEquals("FLPYKAVLCR", tr.protein());

		snpEffectPredictor = new SnpEffectPredictor(genome);
		snpEffectPredictor.setUpDownStreamLength(0);
		snpEffectPredictor.add(gene);
		snpEffectPredictor.buildForest();
	}

	@Test
	public void test_01() {
		// Create variant
		Variant var = new Variant(chr, 397, "GCCCGATAGGA", "", "");
		if (verbose) System.out.println("Variant: " + var);
		Assert.assertEquals("chr1:397_GCCCGATAGGA/", var.toString());

		// Calculate effects
		int countMatch = 0;
		VariantEffects effectsAll = snpEffectPredictor.variantEffect(var);
		for (VariantEffect eff : effectsAll) {
			if (eff.getEffectType() == EffectType.CODON_CHANGE_PLUS_CODON_DELETION) {
				if (verbose) System.out.println("\t" + eff.getEffectTypeString(false) + "\t" + eff.getCodonsRef() + "\t" + eff.getCodonsAlt());
				Assert.assertEquals("TCT", eff.getCodonsAlt().toUpperCase());
				countMatch++;
			}
		}

		Assert.assertTrue("No variant effects found", countMatch > 0);
	}

	@Test
	public void test_02() {
		// Create variant
		int start = 300;
		Variant var = new Variant(chr, start, chrSeq.substring(300, 450), "", "");
		if (verbose) System.out.println("Transcript:" + tr + "\nVariant: " + var);

		// Calculate effects
		int countMatch = 0;
		VariantEffects effectsAll = snpEffectPredictor.variantEffect(var);
		for (VariantEffect eff : effectsAll) {
			if (verbose) System.out.println("\t" + eff.getEffectTypeString(false) + "\tHGVS.p: '" + eff.getHgvsProt() + "'");
			if (eff.getEffectType() == EffectType.TRANSCRIPT_DELETED) {
				countMatch++;
				Assert.assertEquals("HGVS.p notation error", "p.0?", eff.getHgvsProt());
			}
		}
		Assert.assertTrue("No variant effects found", countMatch > 0);
	}
}
