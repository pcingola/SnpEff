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
			// Part of the transcript is inverted

			// Count how many exons are fully and partially included
			int countExonsIncluded = 0, countExonsIntersect = 0;
			for (Exon ex : transcript) {
				if (variant.includes(ex)) countExonsIncluded++;
				else if (variant.intersects(ex)) countExonsIntersect++;
			}

			// Different cases based on exon coverage
			if (countExonsIncluded == 0) {
				if (countExonsIntersect == 0) intron(); // Inversion intersects no exon => Introns
				else partialExons(); // Inversion intersects one or two exons, but does not include any exon
			} else {
				if (countExonsIntersect == 0) fullExons(); // One or more exons fully included (no partial overlap)
				else fullAndPartialExons(); // A mixture of one or more full and one or more partial exons
			}

			// TODO: Which part is inverted?
			//       Coding exon:
			//       	One or more coding exon/s? (cutting at the introns / intergenic)
			//       	One or more coding exon/s? (cutting at the exon/s)
			//       Non-coding exon?
			//			Part of the 3'UTR
			//			Part of the 5'UTR
			//			A whole exon in the UTR
			//
		}
	}

	/**
	 * A mixture of one or more full and one or more partial exons
	 */
	void fullAndPartialExons() {
		if (Math.random() < 2) throw new RuntimeException("TODO!!!");
	}

	/**
	 * One or more exons fully included (no partial overlap)
	 */
	void fullExons() {
		if (Math.random() < 2) throw new RuntimeException("TODO!!!");
	}

	/**
	 * Inversion does not intersect any exon
	 */
	void intron() {
		effect(transcript, EffectType.INTRON, "", "", "", -1, -1, false);
	}

	/**
	 * Inversion intersects (cuts) one or two exons, but does
	 * not include any exon
	 */
	void partialExons() {
		// Coding exon:
		//	  Part of one one coding exon?
		//    One or more coding exon/s? (cutting at the exon/s)
		if (Math.random() < 2) throw new RuntimeException("TODO!!!");
	}

}
