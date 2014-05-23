package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;

/**
 * Calculate codon changes produced by an insertion
 * @author pcingola
 */
public class CodonChangeIns extends CodonChange {

	public CodonChangeIns(Variant seqChange, Transcript transcript, ChangeEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = true; // An insertion can only affect one exon
	}

	/**
	 * Analyze insertions in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	boolean codonChangeSingle(Exon exon) {
		String netChange = seqChange.netChange(transcript.getStrand());

		codonsOld = codonsOld();
		codonsNew = codonsNew();

		if (netChange.length() % CodonChange.CODON_SIZE != 0) {
			/**
			 * Length not multiple of CODON_SIZE => FRAME_SHIFT
			 * 	E.g. : 
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TT' pos 0:	TTA AAC CCG GGA AAC CCG GGA AAC CCG GG
			 * 		Insert 'TT' pos 1:	ATT AAC CCG GGA AAC CCG GGA AAC CCG GG
			 * 		Insert 'TT' pos 2:	AAT TAC CCG GGA AAC CCG GGA AAC CCG GG
			 */
			changeEffects.add(exon, EffectType.FRAME_SHIFT, "");
			changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
		} else if (codonIndex == 0) {
			/**
			 * Length multiple of CODON_SIZE and insertion happens at codon boundary => CODON_INSERTION
			 * 	E.g. : 
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 0:	TTT AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			changeEffects.add(exon, EffectType.CODON_INSERTION, "");
			changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
		} else {
			/**
			 * Length multiple of CODON_SIZE and insertion does not happen at codon boundary => CODON_CHANGE_PLUS_CODON_INSERTION
			 * 	E.g. : 
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 1:	ATT TAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Insert 'TTT' pos 2:	AAT TTA CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			if (codonsNew.toUpperCase().startsWith(codonsOld.toUpperCase())) {
				/**
				 *  May be the inserted base are equal to the old ones.
				 *  E.g.
				 *  	Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
				 *  	Insert 'AAA' pos 1:	AAA AAA CCC GGG AAA CCC GGG AAA CCC GGG
				 */
				changeEffects.add(exon, EffectType.CODON_INSERTION, "");
				changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
			} else {
				changeEffects.add(exon, EffectType.CODON_CHANGE_PLUS_CODON_INSERTION, "");
				changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
			}
		}

		return true;
	}

	/**
	 * Get new (modified) codons 
	 * @return
	 */
	@Override
	String codonsNew() {
		// Inserts BEFORE base:
		//		- In positive strand that is BEFORE pos
		//		- In negative strand, that is AFTER pos
		int idx = codonIndex + (transcript.isStrandMinus() ? 1 : 0);

		// Insertion: Concatenate...
		String codonsNew = codonsOld.substring(0, idx) // the first part of the codon
				+ seqChange.netChange(transcript.getStrand()) // insertion
				+ codonsOld.substring(idx) // the last part of the codon
		;

		return codonsNew;
	}

}
