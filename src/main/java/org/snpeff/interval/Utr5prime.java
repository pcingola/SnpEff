package org.snpeff.interval;

import java.util.Collections;
import java.util.List;

import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.GprSeq;

/**
 * Interval for a UTR (5 prime UTR and 3 prime UTR
 *
 * @author pcingola
 *
 */
public class Utr5prime extends Utr {

	private static final long serialVersionUID = 3710420226746056364L;
	List<Utr5prime> utrs;

	public Utr5prime() {
		super();
		type = EffectType.UTR_5_PRIME;
	}

	public Utr5prime(Exon parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.UTR_5_PRIME;
	}

	synchronized List<Utr5prime> get5primeUtrs() {
		if (utrs == null) {
			Transcript tr = (Transcript) findParent(Transcript.class);

			// Get UTRs and sort them
			utrs = tr.get5primeUtrs();
			if (isStrandPlus()) Collections.sort(utrs, new IntervalComparatorByStart()); // Sort by start position
			else Collections.sort(utrs, new IntervalComparatorByEnd(true)); // Sort by end position (reversed)
		}

		return utrs;
	}

	public String getSequence() {
		// Create UTR sequence
		StringBuffer sb = new StringBuffer();
		for (Utr5prime utr : get5primeUtrs()) {
			Exon ex = (Exon) utr.getParent();
			String utrSeq = ex.getSequence();
			if (utr.size() < utrSeq.length()) utrSeq = utrSeq.substring(0, utr.size()); // UTR5' may stop before end of exon
			sb.append(utrSeq);
		}

		return sb.toString();
	}

	@Override
	public boolean isUtr3prime() {
		return false;
	}

	@Override
	public boolean isUtr5prime() {
		return true;
	}

	/**
	 * Is a new start codon produced?
	 * @param chars
	 * @param pos
	 * @return New start codon (or empty string if there is no new start codon)
	 */
	String startGained(char[] chars, int pos) {
		CodonTable ctable = CodonTables.getInstance().getTable(getGenome(), getChromosomeName());

		// Analyze all frames
		for (int i = Math.max(0, pos - 2); (i <= pos) && ((i + 2) < chars.length); i++) {
			String codon = "" + chars[i] + chars[i + 1] + chars[i + 2];
			if (ctable.isStart(codon)) return codon.toUpperCase(); // This frame has a start codon?
		}
		return "";
	}

	/**
	 * Did we gain a start codon in this 5'UTR interval?
	 * @param variant
	 * @return A new start codon (if gained)
	 */
	String startGained(Variant variant, Transcript tr) {
		if (!variant.isSnp()) return ""; // Only SNPs supported.

		// Calculate SNP position relative to UTRs
		int pos = variant.distanceBases(get5primeUtrs(), isStrandMinus());

		// Change base at SNP position
		String sequence = getSequence();
		char[] chars = sequence.toCharArray();
		char snpBase = variant.netChange(this).charAt(0);
		if (isStrandMinus()) snpBase = GprSeq.wc(snpBase);
		chars[pos] = snpBase;

		// Do we gain a new start codon?
		return startGained(chars, pos);
	}

	/**
	 * Calculate distance from the end of 5'UTRs
	 */
	@Override
	int utrDistance(Variant variant, Transcript tr) {
		int cdsStart = tr.getCdsStart();
		if (cdsStart < 0) return -1;

		if (isStrandPlus()) return cdsStart - variant.getEndClosed();
		return variant.getStart() - cdsStart;
	}

	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		// Has the whole UTR been deleted?
		if (variant.includes(this) && (variant.getVariantType() == VariantType.DEL)) {
			variantEffects.add(variant, this, EffectType.UTR_5_DELETED, ""); // A UTR was removed entirely
			return true;
		}

		// Add distance
		Transcript tr = (Transcript) findParent(Transcript.class);
		int distance = utrDistance(variant, tr);
		VariantEffect variantEffect = new VariantEffect(variant);
		variantEffect.set(this, type, type.effectImpact(), distance >= 0 ? distance + " bases from TSS" : "");
		variantEffect.setDistance(distance);
		variantEffects.add(variantEffect);

		if ((distance == 1) && (variant.lengthChange() % 3 != 0)) {
			// Variant (insertion) starting right before CDS' left side
			// E.g.:
			//      [M  P  D  E  E  M  D  D  P  N  P  A ... ]
			//      ^Variant here just before start codon
			variantEffects.add(variant, this, EffectType.FRAME_SHIFT_BEFORE_CDS_START, "");
		}

		// Start gained?
		String gained = startGained(variant, tr);
		if (!gained.isEmpty()) variantEffects.add(variant, this, EffectType.START_GAINED, gained);

		return true;
	}

}
