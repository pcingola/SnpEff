package org.snpeff.snpEffect.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.snpeff.interval.BioType;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.IntervalComparatorByEnd;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Utr3prime;
import org.snpeff.interval.Utr5prime;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.GprSeq;

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
	int minExons = 1;
	int numGenes = 1;
	int maxGeneLen;
	int minGeneSize = 100;
	String chromoSequence = "";
	Chromosome chromo;
	boolean forcePositiveStrand = false; // Force positive strand (used for debugging & test cases)
	boolean forceNegativeStrand = false; // Force negative strand (used for debugging & test cases)

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
		chromo = new Chromosome(genome, 0, numGenes * 2 * maxGeneLen, "chr1");
		genome.add(chromo);

		// Create sequence
		chromoSequence = GprSeq.randSequence(random, chromo.size());

		// Create gene
		int trNum = 0;
		for (int geneNum = 1; geneNum <= numGenes; geneNum++) {
			int start = 2 * (geneNum - 1) * maxGeneLen + random.nextInt(geneNum * maxGeneLen);
			int end = start + Math.max(minGeneSize, random.nextInt(maxGeneLen));

			// Strand
			boolean strandMinus = !random.nextBoolean();
			if (forcePositiveStrand && forceNegativeStrand) throw new RuntimeException("Cannot force both positive and negative strands!");
			if (forcePositiveStrand) strandMinus = false;
			if (forceNegativeStrand) strandMinus = true;

			String geneId = "geneId" + geneNum;
			String geneName = "geneName" + geneNum;
			Gene gene = new Gene(chromo, start, end, strandMinus, geneId, geneName, BioType.protein_coding);
			add(gene);

			// Create transcripts
			int numTr = Math.max(random.nextInt(maxTranscripts), 1);
			for (int nt = 0; nt < numTr; nt++, trNum++)
				createTranscript(gene, "" + trNum);
		}

		return snpEffectPredictor;
	}

	/**
	 * Create a transcript
	 */
	Transcript createTranscript(Gene gene, String trId) {
		int start = gene.getStart(), end = gene.getEndClosed();
		Transcript tr = new Transcript(gene, gene.getStart(), gene.getEndClosed(), gene.isStrandMinus(), "transcript_" + trId);
		tr.setProteinCoding(true);
		add(tr);

		// Add exons
		int numEx = Math.max(random.nextInt(maxExons), minExons);

		for (int ne = 0; ne < numEx; ne++) {
			// Non-overlapping exons
			int size = tr.size() / numEx;
			start = tr.getStart() + size * ne + random.nextInt(size / 2);
			end = start + random.nextInt(size / 2);

			Exon exon = new Exon(tr, start, end, gene.isStrandMinus(), "exon_" + trId + "_" + ne, ne + 1);

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
	 */
	void createUtrs(Transcript tr) {
		// Exonic bases
		int size = 0;
		for (Exon ex : tr)
			size += ex.size();

		//---
		// Create UTR5
		//---
		if (size < 4) return;
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
					if (tr.isStrandPlus()) utr5 = new Utr5prime(ex, ex.getStart(), ex.getStart() + (utr5size - 1), ex.isStrandMinus(), ex.getId());
					else utr5 = new Utr5prime(ex, ex.getEndClosed() - (utr5size - 1), ex.getEndClosed(), ex.isStrandMinus(), ex.getId());
					tr.add(utr5);
					utr5size = -1;
				} else {
					// Create a full exon UTR5
					Utr5prime utr5 = new Utr5prime(ex, ex.getStart(), ex.getEndClosed(), ex.isStrandMinus(), ex.getId());
					tr.add(utr5);
					utr5size -= ex.size();
				}
			}
		}

		//---
		// Create UTR3
		//---
		int utr3size = random.nextInt(size / 4);
		if (utr3size > 0) {
			// Create UTR3
			List<Exon> exons = new ArrayList<>();
			exons.addAll(tr.subIntervals());
			Collections.sort(exons, new IntervalComparatorByEnd(tr.isStrandPlus()));

			for (Exon ex : exons) {
				if (utr3size < 0) {
					// Nothing to do
				} else if (ex.size() >= utr3size) {
					// Create a partial exon UTR3
					Utr3prime utr3;
					if (tr.isStrandMinus()) utr3 = new Utr3prime(ex, ex.getStart(), ex.getStart() + (utr3size - 1), ex.isStrandMinus(), ex.getId());
					else utr3 = new Utr3prime(ex, ex.getEndClosed() - (utr3size - 1), ex.getEndClosed(), ex.isStrandMinus(), ex.getId());
					tr.add(utr3);
					utr3size = -1;
				} else {
					// Create a full exon UTR3
					Utr3prime utr3 = new Utr3prime(ex, ex.getStart(), ex.getEndClosed(), ex.isStrandMinus(), ex.getId());
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
	protected boolean parse(String line) {
		return false;
	}

	public void setAddUtrs(boolean addUtrs) {
		this.addUtrs = addUtrs;
	}

	public void setChromo(Chromosome chromo) {
		this.chromo = chromo;
	}

	public void setForceNegativeStrand(boolean forceNegativeStrand) {
		this.forceNegativeStrand = forceNegativeStrand;
	}

	public void setForcePositiveStrand(boolean forcePositive) {
		forcePositiveStrand = forcePositive;
	}

	public void setMinExons(int minExons) {
		this.minExons = minExons;
	}

	public void setNumGenes(int numGenes) {
		this.numGenes = numGenes;
	}
}
