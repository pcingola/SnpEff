package org.snpeff.interval.codonChange;

import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a duplication
 *
 * @author pcingola
 */
public class CodonChangeDup extends CodonChangeStructural {

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
			if (transcript.isStrandPlus()) return variant.getEndClosed() > transcript.getCdsEnd();
			return variant.getEndClosed() > transcript.getCdsStart();
		}

		return variant.getEndClosed() > transcript.getEndClosed();
	}

	@Override
	protected void effectTranscript() {
		effectNoCodon(transcript, EffectType.TRANSCRIPT_DUPLICATION);
	}

	@Override
	protected void exons() {
		if (beyondTranscript()) {
			// Is the effect of a duplication beyond transcript's end?
			// Then it probably does not have much impact
			EffectImpact impact = coding ? EffectImpact.LOW : EffectImpact.MODIFIER;
			if (exonFull > 0) effectNoCodon(transcript, EffectType.EXON_DUPLICATION, impact);
			if (exonPartial > 0) effectNoCodon(transcript, EffectType.EXON_DUPLICATION_PARTIAL, impact);
			return;
		}

		if (coding) exonsCoding();
		else exonsNoncoding();

	}

	/**
	 * One or more exons fully included (no partial overlap)
	 */
	@Override
	protected void exonsCoding() {
		codonsRefAlt();

		if (exonFull > 0) effect(transcript, EffectType.EXON_DUPLICATION, false);
		if (exonPartial > 0) effect(transcript, EffectType.EXON_DUPLICATION_PARTIAL, false);

		// Is this duplication creating a frame-shift?
		int lenDiff = cdsAlt.length() - cdsRef.length();
		if (lenDiff % 3 != 0) effect(transcript, EffectType.FRAME_SHIFT, false);
	}

	/**
	 * Effects for non-coding transcripts
	 */
	@Override
	protected void exonsNoncoding() {
		if (exonFull > 0) effectNoCodon(transcript, EffectType.EXON_DUPLICATION, EffectImpact.MODIFIER);
		if (exonPartial > 0) effectNoCodon(transcript, EffectType.EXON_DUPLICATION_PARTIAL, EffectImpact.MODIFIER);
	}

	/**
	 * Inversion does not intersect any exon
	 */
	@Override
	protected void intron() {
		effectNoCodon(transcript, EffectType.INTRON);
	}

}
