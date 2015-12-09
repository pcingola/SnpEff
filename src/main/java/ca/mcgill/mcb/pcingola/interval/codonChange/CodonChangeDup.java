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

	public static boolean debug = false;

	public CodonChangeDup(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
	}

	/**
	 * Differences between two CDSs after removing equal codons from
	 * the beginning and from the end of both strings
	 */
	void cdsDiff(String cdsRef, String cdsAlt) {
		int min = Math.min(cdsRef.length(), cdsAlt.length()) / 3;

		// Removing form the beginning
		codonStartNum = 0;
		codonStartIndex = 0;
		for (int i = 0; i <= min; i++) {
			codonStartNum = i;
			if (debug) Gpr.debug("cdsDiff Start\tcodonEquals(" + i + " , " + i + "): " + codonEquals(cdsRef, cdsAlt, i, i) //
					+ "\n\tcodonsRef [" + codonStartNum + "]: " + codons(cdsRef, codonStartNum, -1) //
					+ "\n\tcodonsAlt [" + codonStartNum + "]: " + codons(cdsAlt, codonStartNum, -1) //
			);
			if (!codonEquals(cdsRef, cdsAlt, i, i)) break;
		}

		// Removing trailing codons
		int codonNumEndRef = cdsRef.length() / 3; //+ (cdsRef.length() % 3 == 0 ? 0 : 1);
		int codonNumEndAlt = cdsAlt.length() / 3; //+ (cdsAlt.length() % 3 == 0 ? 0 : 1);

		for (; codonNumEndRef >= codonStartNum && codonNumEndAlt >= codonStartNum; codonNumEndRef--, codonNumEndAlt--) {
			if (debug) Gpr.debug("cdsDiff End\tcodonEquals(" + codonNumEndRef + " , " + codonNumEndAlt + "): " + codonEquals(cdsRef, cdsAlt, codonNumEndRef, codonNumEndAlt) //
					+ "\n\tcodonsRef [" + codonStartNum + " , " + codonNumEndRef + "]: " + codons(cdsRef, codonStartNum, codonNumEndRef) //
					+ "\n\tcodonsAlt [" + codonStartNum + " , " + codonNumEndAlt + "]: " + codons(cdsAlt, codonStartNum, codonNumEndAlt) //
			);
			if (!codonEquals(cdsRef, cdsAlt, codonNumEndRef, codonNumEndAlt)) break;
		}

		// Codons
		codonsRef = codons(cdsRef, codonStartNum, codonNumEndRef);
		codonsAlt = codons(cdsAlt, codonStartNum, codonNumEndAlt);

		// No codon difference found?
		if (codonsRef.isEmpty() && codonsAlt.isEmpty()) codonStartNum = codonStartIndex = -1;
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Whole transcript duplicated?
			effectNoCodon(transcript, EffectType.TRANSCRIPT_DUPLICATION);
		} else {
			// Part of the transcript is duplicated

			// Does the duplication affect any exon?
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
		for (int h = 0, i = 3 * codonNumRef, j = 3 * codonNumAlt; h < 3; i++, j++, h++) {
			if ((i >= cdsRef.length()) || (j >= cdsAlt.length())) // Premature end of sequence? (i.e. sequence ends before codon end)
				return (i >= cdsRef.length()) && (j >= cdsAlt.length()); // We consider them equal only if both sequences reached the end at the same time

			// Same base?
			if (cdsRef.charAt(i) != cdsAlt.charAt(j)) return false;
		}

		return true;
	}

	/**
	 * Get codons from CDS
	 */
	String codons(String cds, int codonNumStart, int codonNumEnd) {
		if (codonNumEnd >= 0 && codonNumEnd < codonNumStart) return "";
		int endBase = codonNumEnd < 0 ? cds.length() : 3 * (codonNumEnd + 1);
		endBase = Math.min(cds.length(), endBase);
		int startBase = Math.max(0, 3 * codonNumStart);
		return cds.substring(startBase, endBase);
	}

	/**
	 * One or more exons fully included (no partial overlap)
	 */
	void exons() {
		if (transcript.isProteinCoding() || Config.get().isTreatAllAsProteinCoding()) {
			Transcript trNew = transcript.apply(variant);

			if (debug) Gpr.debug("trNew: " + trNew);

			String cdsAlt = trNew.cds();
			String cdsRef = transcript.cds();

			// Calculate differences: CDS
			cdsDiff(cdsRef, cdsAlt);

			boolean full = false, partial = false;
			for (Exon ex : transcript) {
				if (variant.includes(ex)) full = true;
				else if (variant.intersects(ex)) partial = true;
			}
			if (full) effect(transcript, EffectType.EXON_DUPLICATION, false);
			if (partial) effect(transcript, EffectType.EXON_DUPLICATION_PARTIAL, false);

			// Is this duplication creating a frame-shift?
			int lenDiff = cdsAlt.length() - cdsRef.length();
			if (lenDiff % 3 != 0) effect(transcript, EffectType.FRAME_SHIFT, false);
		}
	}

	/**
	 * Inversion does not intersect any exon
	 */
	void intron() {
		effectNoCodon(transcript, EffectType.INTRON);
	}

}
