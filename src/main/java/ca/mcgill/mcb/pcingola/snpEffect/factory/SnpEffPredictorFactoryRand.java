package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.IntervalComparatorByEnd;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr3prime;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * This class creates a random set of chromosomes, genes, transcripts and exons
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryRand extends SnpEffPredictorFactoryGff {

	boolean addUtrs;
	Random random;
	int maxTranscripts;
	int maxExons;
	int maxGeneLen;
	int minGeneSize = 100;
	String chromoSequence = "";
	Chromosome chromo;
	boolean forcePositive = false; // Force positive strand (used for debugging)

	public SnpEffPredictorFactoryRand(Config config, Random random, int maxGeneLen, int maxTranscripts, int maxExons) {
		super(config);
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
			createTranscript(gene, "" + nt);
		}

		return snpEffectPredictor;
	}

	/**
	 * Create a transcript
	 * @param gene
	 * @param trId
	 * @return
	 */
	Transcript createTranscript(Gene gene, String trId) {
		int start = gene.getStart(), end = gene.getEnd();
		Transcript tr = new Transcript(gene, gene.getStart(), gene.getEnd(), gene.getStrand(), "transcript_" + trId);
		tr.setProteinCoding(true);
		add(tr);

		// Add exons
		int numEx = Math.max(random.nextInt(maxExons), 1);
		for (int ne = 0; ne < numEx; ne++) {
			// Non-overlapping exons
			int size = tr.size() / numEx;
			start = tr.getStart() + size * ne + random.nextInt(size / 2);
			end = start + random.nextInt(size / 2);

			Exon exon = new Exon(tr, start, end, gene.getStrand(), "exon_" + trId + "_" + ne, ne + 1);

			// Set exon sequence
			String seq = chromoSequence.substring(start, end + 1);
			if (exon.isStrandMinus()) seq = GprSeq.reverseWc(seq); // Reverse strand? => reverse complement of the sequence
			exon.setSequence(seq);
			add(exon);
		}

		// Add UTRs
		if (addUtrs) createUtrs(tr);

		tr.adjust();
		tr.rankExons();
		return tr;
	}

	/**
	 * Create UTRs for transcript
	 * @param tr
	 */
	void createUtrs(Transcript tr) {
		// Exonic bases
		int size = 0;
		for (Exon ex : tr)
			size += ex.size();

		// UTR5 size
		if (size < 3) return;
		int utr5size = random.nextInt(size / 4);
		size -= utr5size;
		if (utr5size > 0) {
			// Create UTR5
			for (Exon ex : tr.sortedStrand()) {
				if (utr5size < 0) {
					// Nothing to do
				} else if (ex.size() >= utr5size) {
					// Create a partial exon UTR5
					Utr5prime utr5;
					if (tr.isStrandPlus()) utr5 = new Utr5prime(ex, ex.getStart(), ex.getStart() + utr5size, ex.getStrand(), ex.getId());
					else utr5 = new Utr5prime(ex, ex.getEnd() - utr5size, ex.getEnd(), ex.getStrand(), ex.getId());
					tr.add(utr5);
					utr5size = -1;
				} else {
					// Create a full exon UTR5
					Utr5prime utr5 = new Utr5prime(ex, ex.getStart(), ex.getEnd(), ex.getStrand(), ex.getId());
					tr.add(utr5);
					utr5size -= ex.size();
				}
			}
		}

		int utr3size = random.nextInt(size / 4);
		if (utr3size > 0) {
			// Create UTR3
			List<Exon> exons = new ArrayList<Exon>();
			exons.addAll(tr.subintervals());
			Collections.sort(exons, new IntervalComparatorByEnd(tr.isStrandPlus()));

			for (Exon ex : exons) {
				if (utr3size < 0) {
					// Nothing to do
				} else if (ex.size() >= utr3size) {
					// Create a partial exon UTR3
					Utr3prime utr3;
					if (tr.isStrandMinus()) utr3 = new Utr3prime(ex, ex.getStart(), ex.getStart() + utr3size, ex.getStrand(), ex.getId());
					else utr3 = new Utr3prime(ex, ex.getEnd() - utr3size, ex.getEnd(), ex.getStrand(), ex.getId());
					tr.add(utr3);
					utr3size = -1;
				} else {
					// Create a full exon UTR3
					Utr3prime utr3 = new Utr3prime(ex, ex.getStart(), ex.getEnd(), ex.getStrand(), ex.getId());
					tr.add(utr3);
					utr3size -= ex.size();
				}
			}
		}

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

	public void setAddUtrs(boolean addUtrs) {
		this.addUtrs = addUtrs;
	}

	public void setChromo(Chromosome chromo) {
		this.chromo = chromo;
	}

	public void setForcePositive(boolean forcePositive) {
		this.forcePositive = forcePositive;
	}
}
