package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.util.Random;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * This class creates a random set of chromosomes, genes, transcripts and exons
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryRand extends SnpEffPredictorFactoryGff {

	Random random;
	int maxTranscripts;
	int maxExons;
	int maxGeneLen;
	int minGeneSize = 100;
	String chromoSequence = "";
	Chromosome chromo;
	boolean forcePositive = false; // Force positive strand (used for debugging)

	public SnpEffPredictorFactoryRand(Config config, int inOffset, Random random, int maxGeneLen, int maxTranscripts, int maxExons) {
		super(config, inOffset);
		this.random = random;
		this.maxGeneLen = maxGeneLen;
		this.maxTranscripts = maxTranscripts;
		this.maxExons = maxExons;

		frameCorrection = false;
	}

	@Override
	public SnpEffectPredictor create() {
		// Create chromo
		chromo = new Chromosome(genome, 0, 2 * maxGeneLen, 1, "chr1");
		genome.add(chromo);

		// Create sequence
		chromoSequence = GprSeq.randSequence(random, chromo.size());

		// Create gene
		int start = random.nextInt(maxGeneLen);
		int end = start + Math.max(minGeneSize, random.nextInt(maxGeneLen));
		int strand = random.nextBoolean() ? 1 : -1;

		if (forcePositive) strand = 1;

		Gene gene = new Gene(chromo, start, end, strand, "gene1", "gene1", "gene");
		add(gene);

		// Create transcripts
		int numTr = Math.max(random.nextInt(maxTranscripts), 1);
		for (int nt = 0; nt < numTr; nt++) {
			Transcript tr = new Transcript(gene, gene.getStart(), gene.getEnd(), strand, "transcript_" + nt);
			tr.setProteinCoding(true);
			add(tr);

			// Add exons
			int numEx = Math.max(random.nextInt(maxExons), 1);
			for (int ne = 0; ne < numEx; ne++) {
				// Non-overlapping exons
				int size = tr.size() / numEx;
				start = tr.getStart() + size * ne + random.nextInt(size / 2);
				end = start + random.nextInt(size / 2);

				Exon exon = new Exon(tr, start, end, strand, "exon_" + nt + "_" + ne, ne + 1);

				// Set exon sequence
				String seq = chromoSequence.substring(start, end + 1);
				if (exon.isStrandMinus()) seq = GprSeq.reverseWc(seq); // Reverse strand? => reverse complement of the sequence
				exon.setSequence(seq);
				add(exon);
			}

			tr.rankExons();
		}

		return snpEffectPredictor;
	}

	public Chromosome getChromo() {
		return chromo;
	}

	public String getChromoSequence() {
		return chromoSequence;
	}

	@Override
	protected boolean parse(String line, String typeToRead) {
		return false;
	}

	public void setChromo(Chromosome chromo) {
		this.chromo = chromo;
	}

	public void setForcePositive(boolean forcePositive) {
		this.forcePositive = forcePositive;
	}
}
