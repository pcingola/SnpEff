package org.snpeff.snpEffect.factory;

import org.snpeff.genBank.Feature;
import org.snpeff.genBank.Feature.Type;
import org.snpeff.genBank.FeatureCoordinates;
import org.snpeff.genBank.Features;
import org.snpeff.genBank.FeaturesFile;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;

/**
 * This class creates a SnpEffectPredictor from a 'features' file.
 * This includes derived formats as GenBank and Embl.
 *
 * References:
 * 		http://www.ebi.ac.uk/embl/Documentation/User_manual/printable.html
 * 		http://www.ebi.ac.uk/embl/Documentation/FT_definitions/feature_table.html
 *
 *
 * @author pcingola
 */
public abstract class SnpEffPredictorFactoryFeatures extends SnpEffPredictorFactory {

	public static final int OFFSET = 1;
	Chromosome chromosome; // It is assumed that there is only one 'Chromosome' (i.e. only one 'SOURCE' feature)
	FeaturesFile featuresFile;

	public SnpEffPredictorFactoryFeatures(Config config) {
		super(config, OFFSET);
	}

	/**
	 * Add CDS and protein coding information
	 */
	Transcript addCds(Feature f, Gene geneLatest, Transcript trLatest) {
		// Add CDS and protein coding information

		// Convert coordinates to zero-based
		int start = f.getStart() - inOffset;
		int end = f.getEnd() - inOffset;

		// Find corresponding transcript
		Transcript tr = null;
		String trId = null;
		if ((trLatest != null) && trLatest.intersects(start, end)) {
			tr = trLatest;
			trId = tr.getId();
		} else {
			// Try to find transcript
			trId = f.getTranscriptId();
			tr = findTranscript(trId);
		}

		if (tr == null) {
			// Not found? => Create gene and transcript

			// Find or create gene
			Gene gene = null;
			if ((geneLatest != null) && geneLatest.intersects(start, end)) gene = geneLatest;
			else gene = findOrCreateGene(f, chromosome, false);

			if (debug) System.err.println("Transcript '" + trId + "' not found. Creating new transcript for gene '" + gene.getId() + "'.\n" + f);

			if (trId == null) trId = "Tr_" + start + "_" + end;
			tr = findTranscript(trId);
			if (tr == null) {
				tr = new Transcript(gene, start, end, f.isComplement(), trId);
				add(tr);
			}
		}

		// Mark transcript as protein coding
		if (f.getAasequence() != null) tr.setProteinCoding(true);

		// Check and set ribosomal slippage
		if (f.get("ribosomal_slippage") != null) tr.setRibosomalSlippage(true);

		// Add exons?
		if (f.hasMultipleCoordinates()) {
			int cdsEndPrev = -1;
			boolean pastChrEnd = false;
			for (FeatureCoordinates fc : f) {
				int cdsStart = fc.start - inOffset;
				int cdsEnd = fc.end - inOffset;

				// Need to account for genes at the edge of the chromosome in case of circular genomes 
				pastChrEnd |= (cdsStart < cdsEndPrev);
				if (pastChrEnd) {
					cdsStart += chromosome.getEnd() + 1;
					cdsEnd += chromosome.getEnd() + 1;
				}

				Cds cds = new Cds(tr, cdsStart, cdsEnd, f.isComplement(), "CDS_" + trId);
				add(cds);
				cdsEndPrev = cdsEnd;
			}
		} else {
			Cds cds = new Cds(tr, f.getStart() - inOffset, f.getEnd() - inOffset, f.isComplement(), "CDS_" + trId);
			add(cds);
		}

		return tr;
	}

	/**
	 *	Add all features
	 */
	protected void addFeatures(Features features) {
		//---
		// Add chromosome
		//---
		for (Feature f : features.getFeatures()) {
			// Convert coordinates to zero-based
			int start = f.getStart() - inOffset;
			int end = f.getEnd() - inOffset;

			// Add chromosome
			if (f.getType() == Type.SOURCE) {
				if (chromosome == null) {
					String chrName = chromoName(features, f);
					chromosome = new Chromosome(genome, start, end, chrName);
					add(chromosome);
				} else {
					if (debug) System.err.println("Warnign: 'SOURCE' already assigned to chromosome. Ignoring feature:\n" + f);
				}
			}
		}

		// No SOURCE found? may be locusName is available.
		if (chromosome == null) {
			String chrName = chromoName(features, null);
			int chrSize = sequence(features).length();
			chromosome = new Chromosome(genome, 0, chrSize, chrName);
			add(chromosome);
		}

		// Sanity check
		if (chromosome == null) throw new RuntimeException("Could not find SOURCE feature");
		if (verbose) System.err.println("Chromosome: '" + chromosome.getId() + "'\tlength: " + chromosome.size());

		//---
		// Add a genes, transcripts and CDSs
		//---
		Gene geneLatest = null;
		Transcript trLatest = null;
		for (Feature f : features.getFeatures()) {
			if (f.getType() == Type.GENE) {
				// Add gene
				geneLatest = findOrCreateGene(f, chromosome, false);
				trLatest = null;
			} else if (f.getType() == Type.MRNA) {
				trLatest = addMrna(f, geneLatest);
			} else if (f.getType() == Type.CDS) {
				trLatest = addCds(f, geneLatest, trLatest);
			}
		}
	}

	/**
	 * Add transcript information
	 */
	Transcript addMrna(Feature f, Gene geneLatest) {
		// Convert coordinates to zero-based
		int start = f.getStart() - inOffset;
		int end = f.getEnd() - inOffset;

		// Get gene: Make sure the transcript actually refers to the latest gene.
		Gene gene = null;
		if ((geneLatest != null) && geneLatest.intersects(start, end)) gene = geneLatest;
		else gene = findOrCreateGene(f, chromosome, false); // Find or create gene

		// Add transcript
		String trId = f.getTranscriptId();
		Transcript tr = new Transcript(gene, start, end, f.isComplement(), trId);

		// Add exons?
		if (f.hasMultipleCoordinates()) {
			int exNum = 1;
			for (FeatureCoordinates fc : f) {
				Exon e = new Exon(tr, fc.start - inOffset, fc.end - inOffset, fc.complement, tr.getId() + "_" + exNum, exNum);
				tr.add(e);
				exNum++;
			}
		}

		add(tr);
		return tr;
	}

	/**
	 * Find or create a chromosome name for a feature
	 */
	String chromoName(Features features, Feature sourceFeature) {
		// Try 'chromosome' from SOURCE feature
		if (sourceFeature != null) {
			if (sourceFeature.getType() != Type.SOURCE) throw new RuntimeException("Cannot find chromosome name in a non-SOURCE feature");
			String chrName = sourceFeature.get("chromosome");
			if (chrName != null) return chrName;
		}

		// Try locusName
		String chrName = features.getLocusName();
		if (chrName != null) return chrName;

		return genome.getId();
	}

	@Override
	public SnpEffectPredictor create() {
		// Read gene intervals from a file
		try {
			// Iterate over all features
			for (Features features : featuresFile) {
				chromosome = null; // Make sure we create a new source for each file
				addFeatures(features);

				// Some clean-up before reading exon sequences
				beforeExonSequences();

				// Get exon sequences
				String sequence = sequence(features);
				addSequences(chromosome.getId(), sequence);
			}

			// Finish up (fix problems, add missing info, etc.)
			finishUp();
		} catch (Exception e) {
			if (verbose) e.printStackTrace();
			throw new RuntimeException("Error reading file '" + fileName + "'\n" + e);
		}

		return snpEffectPredictor;
	}

	/**
	 * Find (or create) a gene from a feature
	 */
	Gene findOrCreateGene(Feature f, Chromosome chr, boolean warn) {
		int start = f.getStart() - inOffset;
		int end = f.getEnd() - inOffset;

		String geneId = geneId(f, start, end);
		String geneName = geneName(f, start, end);

		Gene gene = findGene(geneId);
		if (gene == null) {
			gene = new Gene(chr, start, end, f.isComplement(), geneId, geneName, null);
			add(gene);
			if (debug) System.err.println("WARNING: Gene '" + geneId + "' not found: created.");
		}

		return gene;
	}

	/**
	 * Try to get geneIDs
	 */
	protected String geneId(Feature f, int start, int end) {
		// Try 'locus'...
		String geneId = f.getGeneId();
		if (geneId != null) return geneId;

		return "Gene_" + start + "_" + end;
	}

	/**
	 * Get gene name from feature
	 */
	protected String geneName(Feature f, int start, int end) {
		// Try 'gene'...
		String geneName = f.getGeneName();
		if (geneName != null) return geneName;

		return "Gene_" + start + "_" + end;
	}

	/**
	 * Get sequence either from features or from FASTA file
	 */
	String sequence(Features features) {
		String seq = features.getSequence();
		if ((seq != null) && !seq.isEmpty()) return seq;
		if (verbose) System.out.println("No sequence found in feature file.");

		// No sequence information in 'features' file? => Try to read a sequence from a fasta file
		for (String fastaFile : config.getFileListGenomeFasta()) {
			if (verbose) System.out.println("\tTrying fasta file '" + fastaFile + "'");

			if (Gpr.canRead(fastaFile)) {
				seq = GprSeq.fastaSimpleRead(fastaFile);
				if ((seq != null) && !seq.isEmpty()) return seq;
			}
		}

		throw new RuntimeException("Cannot find sequence for '" + config.getGenome().getVersion() + "'");
	}
}
