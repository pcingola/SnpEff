package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Calculate codon changes produced by a duplication
 *
 * @author pcingola
 */
public class CodonChangeDup extends CodonChange {

	public static boolean debug = false;

	protected boolean coding;

	public CodonChangeDup(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
		coding = transcript.isProteinCoding() || Config.get().isTreatAllAsProteinCoding();
	}

	/**
	 * Analyze whether the duplication is past transcript's coding region
	 *
	 * E.g.:   Transcript  = chr1:100-200
	 *         Duplication = chr1:150-999
	 *         The duplicated segment starts at base 1000, which is beyond's
	 *         transcript's end, so it probably has no effect on the amino
	 *         acid sequence
	 *
	 *  Rationale:
	 *  If we have two genes:
	 *
	 *     | gene_1 |                 |          gene_2            |
	 *  ---<<<<<<<<<<----------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-----
	 *        |___________dup____________|
	 *
	 *  Then this duplication seems to disrupt gene_2:
	 *
	 *     | gene_1 |                 |          gene_2                                        |
	 *  ---<<<<<<<<<<----------------->>>><<<<<<<----------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-----
	 *        |___________dup____________||___________dup____________|
	 *
	 *  Whereas this one does not, because the duplication affects the gene
	 *  after the gene's coding region:
	 *
	 *     | gene_1 |                 |          gene_2            |
	 *  ---<<<<<<<<<<----------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-----
	 *                                     |___________dup____________|
	 *
	 *     | gene_1 |                 |          gene_2            |
	 *  ---<<<<<<<<<<----------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>--->>>>>>>>>>>>>>>>>>>>>>>>>-----
	 *                                     |___________dup____________||___________dup____________|
	 *
	 * @return true if the duplication is beyond transcript's end
	 */
	boolean beyondTranscript() {
		if (coding) {
			if (transcript.isStrandPlus()) return variant.getEnd() > transcript.getCdsEnd();
			return variant.getEnd() > transcript.getCdsStart();
		}

		return variant.getEnd() > transcript.getEnd();
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

	void exons() {
		// Full and / or partial exons duplicated?
		boolean full = false, partial = false;
		for (Exon ex : transcript) {
			if (variant.includes(ex)) full = true;
			else if (variant.intersects(ex)) partial = true;
		}

		if (beyondTranscript()) {
			// Is the effect of a duplication beyond transcript's end?
			// Then it probably does not have much impact
			EffectImpact impact = coding ? EffectImpact.LOW : EffectImpact.MODIFIER;
			if (full) effectNoCodon(transcript, EffectType.EXON_DUPLICATION, impact);
			if (partial) effectNoCodon(transcript, EffectType.EXON_DUPLICATION_PARTIAL, impact);
			return;
		}

		if (coding) exonsCoding(full, partial);
		else exonsNoncoding(full, partial);

	}

	/**
	 * One or more exons fully included (no partial overlap)
	 */
	void exonsCoding(boolean full, boolean partial) {
		Transcript trNew = transcript.apply(variant);
		if (debug) Gpr.debug("Transcript after apply: " + trNew);

		String cdsAlt = trNew.cds();
		String cdsRef = transcript.cds();

		// Calculate differences: CDS
		cdsDiff(cdsRef, cdsAlt);

		if (full) effect(transcript, EffectType.EXON_DUPLICATION, false);
		if (partial) effect(transcript, EffectType.EXON_DUPLICATION_PARTIAL, false);

		// Is this duplication creating a frame-shift?
		int lenDiff = cdsAlt.length() - cdsRef.length();
		if (lenDiff % 3 != 0) effect(transcript, EffectType.FRAME_SHIFT, false);
	}

	/**
	 * Effects for non-coding transcripts
	 */
	void exonsNoncoding(boolean full, boolean partial) {
		if (full) effectNoCodon(transcript, EffectType.EXON_DUPLICATION, EffectImpact.MODIFIER);
		if (partial) effectNoCodon(transcript, EffectType.EXON_DUPLICATION_PARTIAL, EffectImpact.MODIFIER);
	}

	/**
	 * Inversion does not intersect any exon
	 */
	void intron() {
		effectNoCodon(transcript, EffectType.INTRON);
	}

}
