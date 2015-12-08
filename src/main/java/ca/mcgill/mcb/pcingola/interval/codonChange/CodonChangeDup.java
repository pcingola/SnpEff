package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Calculate codon changes produced by a duplication
 *
 * @author pcingola
 */
public class CodonChangeDup extends CodonChange {

	public CodonChangeDup(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
	}

	/**
	 * Differences between two CDSs after removing equal codons from
	 * the beginning and from the end of both strings
	 */
	void cdsDiff(String cdsRef, String cdsAlt) {
		int min = Math.min(cdsRef.length(), cdsAlt.length()) / 3;

		// Removing codons form the beginning
		codonStartNum = 0;
		codonStartIndex = 0;
		for (int i = 0; i < min; i++) {
			codonStartNum = i;
			if (!codonEquals(cdsRef, cdsAlt, i, i)) break;
		}

		// Removing trailing codons
		int codonNumEndRef = cdsRef.length() / 3 + (cdsRef.length() % 3 == 0 ? 0 : 1);
		int codonNumEndAlt = cdsAlt.length() / 3 + (cdsAlt.length() % 3 == 0 ? 0 : 1);

		for (; codonNumEndRef >= codonStartNum && codonNumEndAlt >= codonStartNum; codonNumEndRef--, codonNumEndAlt--)
			if (!codonEquals(cdsRef, cdsAlt, codonNumEndRef, codonNumEndAlt)) break;

		// Codons Ref/Alt
		codonsRef = codons(cdsRef, codonStartNum, codonNumEndRef);
		codonsAlt = codons(cdsAlt, codonStartNum, codonNumEndAlt);
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Whole transcript inverted?
			effectNoCodon(transcript, EffectType.TRANSCRIPT_DUPLICATION);
		} else {
			// Part of the transcript is inverted

			// Does the inversion affect any exon?
			boolean intersectsExons = false;
			for (Exon ex : transcript) {
				if (variant.intersects(ex)) {
					intersectsExons = true;
					break;
				}
			}

			// Annotate
			if (intersectsExons) exons();
			else intron();
		}
	}

	/**
	 * Compare codons from cdsRef[codonNumRef] and cdsAlt[codonNumAlt]
	 */
	boolean codonEquals(String cdsRef, String cdsAlt, int codonNumRef, int codonNumAlt) {
		for (int h = 0, i = 3 * codonNumRef, j = 3 * codonNumAlt; (h < 3) && (i < cdsRef.length()) && (j < cdsAlt.length()); i++, j++, h++)
			if (cdsRef.charAt(i) != cdsAlt.charAt(j)) return false;

		return true;
	}

	/**
	 * Get codons from CDS
	 */
	String codons(String cds, int codonNumStart, int codonNumEnd) {
		int endBase = Math.min(cds.length(), 3 * codonNumEnd);
		int startBase = Math.max(0, 3 * codonNumStart);
		return cds.substring(startBase, endBase);
	}

	/**
	 * One or more exons fully included (no partial overlap)
	 */
	void exons() {
		if (transcript.isProteinCoding() || Config.get().isTreatAllAsProteinCoding()) {
			Transcript trNew = transcript.apply(variant);

			String cdsAlt = trNew.cds();
			String cdsRef = transcript.cds();

			Gpr.debug("Diff: " //
					+ "\n\tCDS Ref : " + cdsRef //
					+ "\n\tCDS Alt : " + cdsAlt //
			);

			// Calculate differences: CDS
			cdsDiff(cdsRef, cdsAlt);
			effect(transcript, EffectType.EXON_DUPLICATION, false);

			// Is this duplication creating a frame-shift?
			int lenDiff = cdsAlt.length() - cdsRef.length();
			if (lenDiff % 3 != 3) effect(transcript, EffectType.FRAME_SHIFT, false);
		}
	}

	/**
	 * Inversion does not intersect any exon
	 */
	void intron() {
		effectNoCodon(transcript, EffectType.INTRON);
	}

}
