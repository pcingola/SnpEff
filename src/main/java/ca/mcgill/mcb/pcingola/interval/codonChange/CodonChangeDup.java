package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a duplication
 *
 * @author pcingola
 */
public class CodonChangeDup extends CodonChange {

	public CodonChangeDup(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Whole transcript inverted?
			effect(transcript, EffectType.TRANSCRIPT_DUPLICATION, "", "", "", -1, -1, false);
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
	 * One or more exons fully included (no partial overlap)
	 */
	void exons() {
		Marker cdsMarker = null;
		if (transcript.isProteinCoding()) cdsMarker = transcript.cdsMarker();

		for (Exon ex : transcript)
			if (variant.intersects(ex)) {
				EffectImpact impact = EffectImpact.LOW;

				// Is the variant affecting a coding part of the exon?
				// If so, then this is a HIGH impact effect.
				if (cdsMarker != null && variant.intersect(ex).intersects(cdsMarker)) impact = EffectImpact.HIGH;

				// Is the whole exon inverted or just part of it?
				EffectType effType = variant.includes(ex) ? EffectType.EXON_DUPLICATION : EffectType.EXON_DUPLICATION_PARTIAL;

				effect(ex, effType, impact, "", "", "", -1, -1, false);
			}
	}

	/**
	 * Inversion does not intersect any exon
	 */
	void intron() {
		effect(transcript, EffectType.INTRON, "", "", "", -1, -1, false);
	}

}
