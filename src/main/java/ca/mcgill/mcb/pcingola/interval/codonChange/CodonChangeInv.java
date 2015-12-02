package ca.mcgill.mcb.pcingola.interval.codonChange;

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
			// Part of the transcript is inverted

			if (Math.random() < 2) throw new RuntimeException("TODO!!!");
			// TODO: Which part is inverted? 
			//       Coding exon: 
			//			Part of one one coding exon?
			//       	One or more coding exon/s? (cutting at the introns / intergenic)
			//       	One or more coding exon/s? (cutting at the exon/s)
			//       Non-coding exon? 
			//			Part of the 3'UTR
			//			Part of the 5'UTR
			//			A whole exon in the UTR
			//
		}
	}

}
