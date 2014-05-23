package ca.mcgill.mcb.pcingola.interval;

import java.util.Collections;
import java.util.List;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;
import ca.mcgill.mcb.pcingola.util.GprSeq;

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

	public Utr5prime(Exon parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
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

	@Override
	public boolean seqChangeEffect(Variant seqChange, ChangeEffects changeEffects) {
		// Has the whole UTR been deleted?
		if (seqChange.includes(this) && (seqChange.getChangeType() == VariantType.DEL)) {
			changeEffects.add(this, EffectType.UTR_5_DELETED, ""); // A UTR was removed entirely
			return true;
		}

		// Is it START_GAINED?
		Transcript tr = (Transcript) findParent(Transcript.class);
		int dist = utrDistance(seqChange, tr);
		String gained = startGained(seqChange, tr);

		changeEffects.add(this, type, dist >= 0 ? dist + " bases from TSS" : "");
		if (dist >= 0) changeEffects.setDistance(dist);
		if (!gained.isEmpty()) changeEffects.add(this, EffectType.START_GAINED, gained);

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
	 * @param seqChange
	 * @return A new start codon (if gained)
	 */
	String startGained(Variant seqChange, Transcript tr) {
		if (!seqChange.isSnp()) return ""; // FIXME: Only SNPs supported! 

		// Calculate SNP position relative to UTRs
		int pos = seqChange.distanceBases(get5primeUtrs(), isStrandMinus());

		// Change base at SNP position
		String sequence = getSequence();
		char[] chars = sequence.toCharArray();
		char snpBase = seqChange.netChange(this).charAt(0);
		if (isStrandMinus()) snpBase = GprSeq.wc(snpBase);
		chars[pos] = snpBase;

		// Do we gain a new start codon?
		return startGained(chars, pos);
	}

	/**
	 * Calculate distance from the end of 5'UTRs
	 * 
	 * @param seqChange
	 * @param utr
	 * @return
	 */
	@Override
	int utrDistance(Variant seqChange, Transcript tr) {
		int cdsStart = tr.getCdsStart();
		if (cdsStart < 0) return -1;

		if (isStrandPlus()) return cdsStart - seqChange.getEnd();
		return seqChange.getStart() - cdsStart;
	}

}
