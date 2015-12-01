package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by an inversion
 * 
 * @author pcingola
 */
public class CodonChangeInv extends CodonChange {

	public CodonChangeInv(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Whole transcript inverted?
			effect(transcript, EffectType.TRANSCRIPT_INVERSION, "", "", "", -1, -1, false);
		} else {
			// Partial inclussion
			for (Exon ex : transcript) {
				effect(transcript, EffectType.TRANSCRIPT_INVERSION, "", "", "", -1, -1, false);
			}
		}
	}

}
