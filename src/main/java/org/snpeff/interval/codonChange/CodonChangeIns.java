package org.snpeff.interval.codonChange;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by an insertion
 * @author pcingola
 */
public class CodonChangeIns extends CodonChange {

	public CodonChangeIns(Variant seqChange, Transcript transcript, VariantEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = true; // An insertion can only affect one exon
	}

	@Override
	public void codonChange() {
		// Special case: Is the insertion at the edge of the CDS?
		int pos = variant.getEnd();
		if (pos == transcript.getCdsStart() || pos == transcript.getCdsEnd()) {
			codonChangeCdsEdge();
			return;
		}

		// OK, use the 'normal' codon change processing
		super.codonChange();
	}

	/**
	 * Analyze insertions in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	protected boolean codonChange(Exon exon) {
		String netChange = variant.netChange(transcript.isStrandMinus());

		codonsRef = codonsRef();
		codonsAlt = codonsAlt();

		EffectType effType = null;

		if (netChange.length() % CodonChange.CODON_SIZE != 0) {
			/**
			 * Length not multiple of CODON_SIZE => FRAME_SHIFT
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TT' pos 0:	TTA AAC CCG GGA AAC CCG GGA AAC CCG GG
			 * 		Insert 'TT' pos 1:	ATT AAC CCG GGA AAC CCG GGA AAC CCG GG
			 * 		Insert 'TT' pos 2:	AAT TAC CCG GGA AAC CCG GGA AAC CCG GG
			 */
			effType = EffectType.FRAME_SHIFT;
		} else if (codonStartIndex == 0) {
			/**
			 * Length multiple of CODON_SIZE and insertion happens at codon boundary => CODON_INSERTION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 0:	TTT AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			effType = EffectType.CODON_INSERTION;
		} else {
			/**
			 * Length multiple of CODON_SIZE and insertion does not happen at codon boundary => CODON_CHANGE_PLUS_CODON_INSERTION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 1:	ATT TAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 2:	AAT TTA CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			if (codonsAlt.toUpperCase().startsWith(codonsRef.toUpperCase())) {
				/**
				 *  May be the inserted base are equal to the old ones.
				 *  E.g.
				 *  	Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
				 *  	Insert 'AAA' pos 1:	AAA AAA CCC GGG AAA CCC GGG AAA CCC GGG
				 */
				effType = EffectType.CODON_INSERTION;
			} else {
				effType = EffectType.CODON_CHANGE_PLUS_CODON_INSERTION;
			}
		}

		effect(exon, effType, false);

		return true;
	}

	/**
	 * Variant (insertion) starting right before CDS' left side
	 * or right after CDS's right side?
	 *
	 * E.g.:
	 *      [M  P  D  E  E  M  D  D  P  N  P  A ...
	 *       ^Insertion here just before start codon
	 *
	 *       ... P  D  E  E  M  D  D  P  N  P  *]
	 *                                          ^Insertion here right after stop codon
	 *
	 * In either case, the insertion has no coding effect.
	 */
	protected void codonChangeCdsEdge() {
		// Is the insertion at the edge of the CDS?
		int cdsStart = transcript.getCdsStart();
		int cdsEnd = transcript.getCdsEnd();
		int cdsLeft = Math.min(cdsStart, cdsEnd);
		int cdsRight = Math.max(cdsStart, cdsEnd);

		int pos = variant.getStart(); // Insertions have coordinated 'start = end'

		Exon ex = transcript.findExon(pos);
		if (pos == cdsLeft) {
			// Insertion on CDS' left side
			if (transcript.isStrandPlus()) {
				// Left side = CDS start
				codonStartNum = 0;
				effect(ex, EffectType.FRAME_SHIFT_BEFORE_CDS_START, false);
			} else {
				// Left side = CDS end
				codonStartNum = transcript.protein().length();
				effect(ex, EffectType.FRAME_SHIFT_AFTER_CDS_END, false);
			}
			return;
		} else if (pos == cdsRight) {
			// Insertion on CDS' right side
			if (transcript.isStrandPlus()) {
				// Right side = CDS end
				codonStartNum = transcript.protein().length();
				effect(ex, EffectType.FRAME_SHIFT_AFTER_CDS_END, false);
			} else {
				// Right side = CDS start
				codonStartNum = 0;
				effect(ex, EffectType.FRAME_SHIFT_BEFORE_CDS_START, false);
			}
			return;
		}

		throw new RuntimeException("This should never happen!");
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	protected String codonsAlt() {
		// Inserts BEFORE base:
		//		- In positive strand that is BEFORE pos
		//		- In negative strand, that is AFTER pos
		int idx = codonStartIndex + (transcript.isStrandMinus() ? 1 : 0);

		// Insertion: Concatenate...
		String prefix = codonsRef.length() >= idx ? codonsRef.substring(0, idx) : codonsRef; // First part of the codon
		String netChange = variant.netChange(transcript.isStrandMinus()); // Insertion
		String suffix = codonsRef.length() >= idx ? codonsRef.substring(idx) : ""; // last part of the codon

		// New codon
		String codonsNew = prefix + netChange + suffix;

		return codonsNew;
	}
}
