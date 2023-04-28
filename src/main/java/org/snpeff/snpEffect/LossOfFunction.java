package org.snpeff.snpEffect;

import java.util.Collection;
import java.util.HashSet;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfLof;
import org.snpeff.vcf.VcfNmd;

/**
 * Analyze if a set of effects are can create a "Loss Of Function"
 * and "Nonsense mediated decays" effects.
 *
 * Of course, this is a prediction based on analysis
 * of groups of "putative effects". Proper wet-lab
 * validation is required to infer "real" LOF.
 *
 * References: I used the LOF definition used in the
 * following paper "A Systematic Survey of Loss-of-Function
 * Variants in Human Protein-Coding Genes", Science, 2012
 *
 * From the paper:
 * 		We adopted a definition for LoF variants
 * 		expected to correlate with complete loss of function
 * 		of the affected transcripts: stop codon-introducing
 * 		(nonsense) or splice site-disrupting single-nucleotide
 * 		variants (SNVs), insertion/deletion (indel) variants
 * 		predicted to disrupt a transcript's reading frame, or
 * 		larger deletions removing either the first exon or more
 * 		than 50% of the protein-coding sequence of the affected
 * 		transcript.
 *
 * 		Both nonsense SNVs and frameshift indels are enriched toward the 3' end
 * 		of the affected gene, consistent with a greater tolerance to truncation
 * 		close to the end of the coding sequence (Fig. 1C); putative LoF variants
 * 		identified in the last 5% of the coding region were thus systematically
 * 		removed from our high-confidence set.
 *
 *
 * @author pcingola
 */
public class LossOfFunction {

	public static final String VCF_INFO_NMD_NAME = "NMD";
	public static final String VCF_INFO_LOF_NAME = "LOF";

	/**
	 * Number of bases before last exon-exon junction that nonsense
	 * mediated decay is supposed to occur
	 */
	public static final int MND_BASES_BEFORE_LAST_JUNCTION = 50;

	/**
	 * It is assumed that even with a protein coding change at the
	 * last 5% of the protein, the protein could still be functional.
	 */
	public static final double DEFAULT_IGNORE_PROTEIN_CODING_AFTER = 0.95;
	/**
	 *  It is assumed that even with a protein coding change at the
	 *  first 5% of the protein:
	 *  	"..suggesting some disrupted transcripts are
	 *  	rescued by transcriptional reinitiation at an
	 *  	alternative start codon."
	 */
	public static final double DEFAULT_IGNORE_PROTEIN_CODING_BEFORE = 0.05;

	/**
	 * Larger deletions removing either the first exon or more than
	 * 50% of the protein-coding sequence of the affected transcript
	 */
	public static final double DEFAULT_DELETE_PROTEIN_CODING_BASES = 0.50;
	public double ignoreProteinCodingAfter;

	public double ignoreProteinCodingBefore;
	public double deleteProteinCodingBases;

	Config config;
	HashSet<Transcript> transcriptsLof;
	HashSet<Gene> genesLof;
	HashSet<Transcript> transcriptsNmd;
	HashSet<Gene> genesNmd;
	Collection<VariantEffect> variantEffects;
	int lofCount = -1; // Number of loss of function effects
	int nmdCount = -1; // Number of nonsense mediated decay effects

	public LossOfFunction(Config config, Collection<VariantEffect> variantEffects) {
		this.variantEffects = variantEffects;
		transcriptsLof = new HashSet<>();
		genesLof = new HashSet<>();
		transcriptsNmd = new HashSet<>();
		genesNmd = new HashSet<>();

		this.config = config;
		ignoreProteinCodingBefore = config.getLofIgnoreProteinCodingBefore();
		ignoreProteinCodingAfter = config.getLofIgnoreProteinCodingAfter();
		deleteProteinCodingBases = config.getLofDeleteProteinCodingBases();
	}

	/**
	 * Can this collection of effects produce a "Loss of function"
	 */
	public boolean isLof() {
		// Need to calculate?
		if (lofCount < 0) {
			lofCount = nmdCount = 0;

			// Iterate over all variantEffects
			for (VariantEffect variantEffect : variantEffects)
				if (isLof(variantEffect)) lofCount++;
		}

		return lofCount > 0;
	}

	/**
	 * Is this single change a LOF?
	 *
	 * Criteria:
	 * 		1) Core splice sites acceptors or donors (only CORE ones)
	 * 		2) Stop gained (if this happens at the last part of the protein, we assume it has no effect)
	 * 		3) Frame shifts
	 */
	protected boolean isLof(VariantEffect variantEffect) {
		// Not a sequence change? => Not LOF
		if ((variantEffect.getVariant() != null) && (!variantEffect.getVariant().isVariant())) return false;

		// Is this change affecting a protein coding gene?
		Gene gene = variantEffect.getGene();
		Transcript tr = variantEffect.getTranscript();
		if ((gene == null) // No gene affected?
				|| (tr == null) // No transcript affected?
				|| (!gene.isProteinCoding() && !config.isTreatAllAsProteinCoding()) // Not a protein coding gene?
				|| (!tr.isProteinCoding() && !config.isTreatAllAsProteinCoding()) // Not a protein coding transcript?
		) return false;

		//---
		// Is this variant a LOF?
		//---
		boolean lof = false;

		// Frame shifts
		if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) {
			// It is assumed that even with a protein coding change at the last 5% of the protein, the protein could still be functional.
			double perc = percentCds(variantEffect);
			lof |= (ignoreProteinCodingBefore <= perc) && (perc <= ignoreProteinCodingAfter);
		}

		// Deletion? Is another method to check
		if (variantEffect.getVariant().isDel()) lof |= isLofDeletion(variantEffect);

		//---
		// The following effect types can be considered LOF
		//---
		if (variantEffect.hasEffectType(EffectType.SPLICE_SITE_ACCEPTOR) //
				|| variantEffect.hasEffectType(EffectType.SPLICE_SITE_DONOR) //
		) {
			// Core splice sites are considered LOF
			if ((variantEffect.getMarker() != null) && (variantEffect.getMarker() instanceof SpliceSite)) {
				// Get splice site marker and check if it is 'core'
				SpliceSite spliceSite = (SpliceSite) variantEffect.getMarker();

				// Does it intersect the CORE splice site?
				if (spliceSite.intersectsCoreSpliceSite(variantEffect.getVariant())) {
					lof = true;
				}
			}
		} else if (variantEffect.hasEffectType(EffectType.STOP_GAINED)) {
			lof |= isNmd(variantEffect);
		} else if (variantEffect.hasEffectType(EffectType.RARE_AMINO_ACID)) {
			// This one is not in the referenced papers, but we assume that RARE AA are damaging.
			lof = true;
		} else if (variantEffect.hasEffectType(EffectType.START_LOST)) {
			// This one is not in the referenced papers, but we assume that START_LOSS changes are damaging.
			lof = true;
		} else {
			// All others are not considered LOF
		}

		// Update sets
		if (lof) {
			transcriptsLof.add(variantEffect.getTranscript()); // Unique transcripts affected (WARNING: null will be added)
			genesLof.add(variantEffect.getGene()); // Unique genes affected (WARNING: null will be added)
		}

		return lof;
	}

	/**
	 * Is this deletion a LOF?
	 *
	 * Criteria:
	 * 		1) First (coding) exon deleted
	 * 		2) More than 50% of coding sequence deleted
	 */
	protected boolean isLofDeletion(VariantEffect variantEffect) {
		Transcript tr = variantEffect.getTranscript();
		if (tr == null) throw new RuntimeException("Transcript not found for change:\n\t" + variantEffect);

		//---
		// Criteria:
		// 		1) The whole transcript or the first (coding) exon deleted
		//---
		if (variantEffect.hasEffectType(EffectType.TRANSCRIPT_DELETED)) return true;

		if (variantEffect.hasEffectType(EffectType.EXON_DELETED)) {
			Variant variant = variantEffect.getVariant();
			if (variant == null) throw new RuntimeException("Cannot retrieve 'variant' from EXON_DELETED effect!");
			Exon firstExon = tr.getFirstCodingExon();
			if (firstExon != null && variant.includes(firstExon)) return true;
		}

		// Fusion are loss of functions
		if (variantEffect.hasEffectType(EffectType.GENE_FUSION) //
				|| variantEffect.hasEffectType(EffectType.GENE_FUSION_HALF) //
				|| variantEffect.hasEffectType(EffectType.GENE_FUSION_REVERESE) //
		) return true;

		//---
		// Criteria:
		// 		2) More than 50% of coding sequence deleted
		//---

		// Find coding part of the transcript (i.e. no UTRs)
		Variant variant = variantEffect.getVariant();
		int cdsStart = tr.isStrandPlus() ? tr.getCdsStart() : tr.getCdsEnd();
		int cdsEnd = tr.isStrandPlus() ? tr.getCdsEnd() : tr.getCdsStart();
		Marker coding = new Marker(variant.getChromosome(), cdsStart, cdsEnd, false, "");

		// Create an interval intersecting the CDS and the deletion
		int start = Math.max(cdsStart, variant.getStart());
		int end = Math.min(cdsEnd, variant.getEndClosed());
		if (start >= end) return false; // No intersections with coding part of the exon? => not LOF
		Marker codingDeleted = new Marker(variant.getChromosome(), start, end, false, "");

		// Count:
		//   - number of coding bases deleted
		//   - number of coding bases
		int codingBasesDeleted = 0, codingBases = 0;
		for (Exon exon : tr) {
			codingBasesDeleted += codingDeleted.intersectSize(exon);
			codingBases += coding.intersectSize(exon);
		}

		// More than a threshold? => It is a LOF
		double percDeleted = codingBasesDeleted / ((double) codingBases);
		return (percDeleted > deleteProteinCodingBases);
	}

	/**
	 * Can this collection of effects produce a "Nonsense mediated decay"?
	 */
	public boolean isNmd() {
		if (nmdCount < 0) isLof(); // Need to calculate?
		return nmdCount > 0;
	}

	/**
	 * Is this single change a LOF?
	 *
	 * Criteria:
	 * 		1) Core splice sites acceptors or donors (only CORE ones)
	 * 		2) Stop gained (if this happens at the last part of the protein, we assume it has no effect)
	 * 		3) Frame shifts
	 */
	protected boolean isNmd(VariantEffect variantEffect) {
		Transcript tr = variantEffect.getTranscript();
		if (tr == null) throw new RuntimeException("Transcript not found for change:\n\t" + variantEffect);

		// Only one exon? Nothing to do (there is no exon-exon junction)
		if (tr.numChilds() <= 1) return false;

		// Find last valid NMD position
		int lastNmdPos = lastNmdPos(tr);
		if (lastNmdPos < 0) return false; // No valid 'lastNmdPos'? => There is no NMD event.

		// Does this change affect the region 'before' this last NMD position? => It is assumed to be NMD
		Variant variant = variantEffect.getVariant();

		boolean nmd;
		if (tr.isStrandPlus()) nmd = variant.getStart() <= lastNmdPos;
		else nmd = lastNmdPos <= variant.getEndClosed();

		// Update sets and counters
		if (nmd) {
			transcriptsNmd.add(variantEffect.getTranscript()); // Unique transcripts affected (WARNING: null will be added)
			genesNmd.add(variantEffect.getGene()); // Unique genes affected (WARNING: null will be added)
			nmdCount++;
		}

		return nmd;
	}

	/**
	 * Find the last position where a nonsense mediated decay is supposed to occurr
	 * This is 50 bases (MND_BASES_BEFORE_LAST_JUNCTION bases) before the last exon-exon junction.
	 */
	public int lastNmdPos(Transcript tr) {
		//---
		// Get last exon
		//---
		int cdsEnd = tr.getCdsEnd();
		int cdsStart = tr.getCdsStart();
		Marker cds = new Marker(tr.getChromosome(), Math.min(cdsStart, cdsEnd), Math.max(cdsStart, cdsEnd), tr.isStrandMinus(), ""); // Create a cds marker
		Exon lastExon = null;
		int countCodingExons = 0;
		for (Exon exon : tr.sortedStrand()) {
			if (exon.intersects(cdsEnd)) lastExon = exon;
			if (cds.intersects(exon)) countCodingExons++;
		}

		// Only one coding exon? => No NMD
		// Note: I'm assuming that we should have a splice event in a coding part of the transcript for NMD to happen.
		if (countCodingExons <= 1) return -1;

		// Sanity check
		if (lastExon == null) throw new RuntimeException("Cannot find last coding exon for transcript '" + tr.getId() + "' (cdsEnd: " + cdsEnd + ")\n\t" + tr);

		//---
		// Find that position of MND_BASES_BEFORE_LAST_JUNCTION before the last exon-exon junction
		//---
		int lastExonJunction = tr.isStrandPlus() ? lastExon.getStart() : lastExon.getEndClosed();
		int chrPos[] = tr.baseNumberCds2Pos();
		int lastNmdPos = -1;
		for (int cdsi = chrPos.length - 1; cdsi >= 0; cdsi--) {
			if (chrPos[cdsi] == lastExonJunction) {
				if (cdsi > MND_BASES_BEFORE_LAST_JUNCTION) lastNmdPos = chrPos[cdsi - MND_BASES_BEFORE_LAST_JUNCTION - 1];
				else return tr.isStrandPlus() ? 0 : Integer.MAX_VALUE; // Out of CDS range
				return lastNmdPos;
			}
		}

		throw new RuntimeException("Cannot find last exon junction position for transcript '" + tr.getId() + "'\n\t" + tr);
		// return -1;
	}

	/**
	 * Parse NMD from VcfEntry
	 */
	void parseNmd(VcfEntry vcfEntry) {
	}

	/**
	 * Which percentile of the protein does this effect hit?
	 */
	double percentCds(VariantEffect variantEffect) {
		int cdsLen = variantEffect.getAaLength();
		int codonNum = variantEffect.getCodonNum();
		if ((cdsLen >= 0) && (codonNum >= 0)) return codonNum / ((double) cdsLen);
		return Double.NaN;
	}

	/**
	 * What percentile of the transcripts in this gene are affected?
	 */
	double percentOfTranscriptsAffected(Gene gene, HashSet<Transcript> transcripts) {
		if (gene == null) return 0;

		// Count how many transcript are affected in each gene
		int countAffected = 0;
		for (Transcript tr : gene)
			if (transcripts.contains(tr)) countAffected++;

		return countAffected / ((double) gene.numChilds());
	}

	@Override
	public String toString() {
		return (isLof() ? "LOF=" + toStringVcfLof() + " " : "") //
				+ (isNmd() ? "NMD=" + toStringVcfNmd() : "") //
		;
	}

	/**
	 * Get LOF value for VCF info field
	 */
	public String toStringVcfLof() {
		StringBuilder sb = new StringBuilder();

		for (Gene gene : genesLof) {
			if (sb.length() > 0) sb.append(','); // Separate by comma

			double perc = percentOfTranscriptsAffected(gene, transcriptsLof);
			VcfLof lofent = new VcfLof(gene, perc);

			sb.append(lofent.toString());
		}

		return sb.toString();
	}

	/**
	 * Get NMD value for VCF info field
	 */
	public String toStringVcfNmd() {
		StringBuilder sb = new StringBuilder();

		for (Gene gene : genesNmd) {
			if (sb.length() > 0) sb.append(','); // Separate by comma

			double perc = percentOfTranscriptsAffected(gene, transcriptsNmd);
			VcfNmd nmdent = new VcfNmd(gene, perc);

			sb.append(nmdent.toString());
		}

		return sb.toString();
	}

}
