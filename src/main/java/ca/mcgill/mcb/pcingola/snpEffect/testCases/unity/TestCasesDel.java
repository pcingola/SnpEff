package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Test random DEL changes
 *
 * @author pcingola
 */
public class TestCasesDel {

	public static int N = 1000;

	boolean debug = false;
	boolean verbose = false || debug;

	@Test
	public void test_02() {
		//---
		// Create a transcript
		//---
		Config conf = new Config("test");

		String chrSeq = "CGTGTTACCAAGCATTTGGGAACCGGAATTCTACGCTGGAGTTCGCCTAACAGCTAAAGCTCAAAACAGGAGTGGTTTAGTTCACAGGCAGACCTTTAACGGTACCTACCTTAATCCGGTCAGATTTAAATTCATGATAGATGGTCTAAGTCATGCACTACTCCTACCAGTTTTGGACGTGGCGCATTGGGCCCCGACACCAAATGCGTTGGAGCGACAGCACAAACGATATCTCAGATTAAGTGCATTGAAGGCCTTCTCACGGATAGATCTATAGGTCGCTGTGTGTCGGGAAGTCTTTCATCCGCTCGGGAGTCGGGTAAATTCTTTCTTCCAGTTGTGTGTTCTACGACTGCAGAGGACAGCTCTCGCTTAGTTTCCTTCCGTTCTTTGTAAGGCCCGATAGGAATACCCGCATAATTATCCCCACAGTCTAACTTGATCCACGTCATATGGGGCGATGGCTGGAATGAGGGGGGTTCAGTGGCAGACCACGATATCTCTGTTTATCTGATGAAGAATTTTGGATACCCCAATAACTAATGTGAGAAGAGTGTGGGGTATCCAACGTGATTGGAGGGAAGTATGGAAACGAATGAAGACAAGTTCGACAAGCCGTGGTAACGATCATATGCCACCGTATTCTAACGGGGGGGGGCCGCTGTTTGAATGCCAGATAGATATTTAAAGAGGATGGCCGCCCCCCAATGGGCACGAGGAACTGACCAGAGGTAGGTATGATGGTGTGAGAGAGAGATGTAGTGTAGTTACTATAAAGATTTGAAGATCGAAAGGCTTAGTGATACGAAACCTCCCGCTACTGCGGGCCGGTATAGCAGGTCTAATACTTGGTGATAGGGCTACATCGAGCCTCGTTCGACAAGAATACCCCAGTTGCAATGGGGACCGATTCAGTCTGGGGCTCTGTGGTCCGACATTATGCCGGAATGCCTAGTATGGGAATTAAAACGATTAATATTACCTCTGGGACTATCTTTGTGCCAAACTCTACTGAGAGAGGAACGTAATCGTATTAGGGGCCAAGATAGTTTACTCCGATCCACCGTAACACTTATTTTGGGCCGTGCGTACCCACCCACGGTCATGAACGCAAACTGGATAGGGCGCGTCATACGAGCTAAAGTGTTCTCTTGGGGCAAATAAGAAGGGACTAGCTGTATTCCCCTCCCATTGTGTGGGTACAATTCGGGGGAAAGGTGTCGAGCCCAAATGCTACGGCTCGTAGATATCAGGTCCTTTAGCCGCCCGTATGTAAGCACCTCCAACGTTTTGGAGGAAGATCCCTCTCCCACGGGTTATACCATTAATGACTCCGCACTAACAATATCTTACCAGGGCCCGCAAATACCACACCGGGATACTGGAATGAATTATTCTCCGACCCATTTGATGAAACGTATGGGAACTTTCCTGTTGTATTAAACCCGCGGATACACTCTCTTGCCCACGAGTAGCCTCTACTTACTAGATTGAGAGGTACACTACGCCATTATGAAGCATTTCGACTTTCACTCCAGTATATAGAAGGTATGTGTGGATCCTTCAAAATAGTGATCCGAACCTGGTTGTAGGGGCCACCGAAATTCCGATTACTGAAGATTAATGTTTTCAAATGCCCTATTTCACTGGTGATAGCGATGGAGCCGGGCTATCATCATGGCGAAGCACGTGGGAAAGCATTCCGTCAAGGCTAGTGGGAACCTCTGCCTTGCCATGTACGCGTTCTATATACACGAACATCGATACCGGTCTCGTCCTGGGGTAGAGCCATCCTCATCACGAGTGATGTAGGACGGCTAGCTCATTAATTCACTGCGTGTCAGATAGAATGTCTTCGTGCGCAAAAATTGTTTAGGAGACCGTCGGCGCTCCCTTGGAGAGATGCCCAACGTAGAGAGGCAATCCCCGGCGTATTGGTGGTTTCTGGAACGGAACGAGACTCTTTGTTGCGTCGACTCCCGGCAACACCGGCCCCCGCCG";
		Genome genome = new Genome("test");

		Chromosome chr = new Chromosome(genome, 0, chrSeq.length() - 1, "1");
		chr.setSequence(chrSeq);
		genome.add(chr);

		Gene gene = new Gene(chr, 333, 408, true, "gene1", "gene1", "protein_coding");

		Transcript tr = new Transcript(gene, 333, 408, true, "tr1");
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

		//---
		// Create variant
		//---
		Variant var = new Variant(chr, 397, "GCCCGATAGGA", "", "");
		if (verbose) System.out.println("Variant: " + var);
		Assert.assertEquals("chr1:397_GCCCGATAGGA/", var.toString());

		//---
		// Calculate effects
		//---
		SnpEffectPredictor snpEffectPredictor = new SnpEffectPredictor(genome);
		snpEffectPredictor.setUpDownStreamLength(0);
		snpEffectPredictor.add(gene);
		snpEffectPredictor.buildForest();

		VariantEffects effectsAll = snpEffectPredictor.variantEffect(var);
		for (VariantEffect eff : effectsAll) {
			if (eff.getEffectType() == EffectType.CODON_CHANGE_PLUS_CODON_DELETION) {
				if (verbose) System.out.println("\t" + eff.getEffectTypeString(false) + "\t" + eff.getCodonsRef() + "\t" + eff.getCodonsAlt());
				Assert.assertEquals("TCT", eff.getCodonsAlt().toUpperCase());
			}
		}
	}
}
