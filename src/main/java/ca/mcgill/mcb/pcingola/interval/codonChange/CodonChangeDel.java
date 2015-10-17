package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a deletion
 * @author pcingola
 */
public class CodonChangeDel extends CodonChange {

	int oldCodonCdsStart = -1;
	int oldCodonCdsEnd = -1;

	public CodonChangeDel(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
		returnNow = false;
		requireNetCdsChange = true;
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Large deletion removing the whole transcript?
			effect(transcript, EffectType.TRANSCRIPT_DELETED, "", "", "", -1, -1, false);
		} else {
			// Normal cases
			super.codonChange();
		}
	}

	/**
	 * Analyze deletions in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	protected boolean codonChangeSingle(Exon exon) {
		// Is there any net effect?
		if (netCdsChange.isEmpty()) return false;

		EffectType effType = null;

		if (variant.includes(exon)) {
			/**
			 * An exon has been entirely removed
			 */
			codonsRef = "";
			codonsAlt = "";
			codonStartNum = codonStartIndex = -1;
			effType = EffectType.EXON_DELETED;
		} else if (netCdsChange.length() % CodonChange.CODON_SIZE != 0) {
			/**
			 * Length not multiple of CODON_SIZE => FRAME_SHIFT
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AA' pos 0:	ACC CGG GAA ACC CGG GAA ACC CGG G
			 * 		Delete 'AA' pos 1:	ACC CGG GAA ACC CGG GAA ACC CGG G
			 * 		Delete 'AC' pos 2:	AAC CGG GAA ACC CGG GAA ACC CGG G
			 */
			codonsRef = codonsRef();
			codonsAlt = "";
			effType = EffectType.FRAME_SHIFT;
		} else if (codonStartIndex == 0) {
			/**
			 * Length multiple of CODON_SIZE and insertion happens at codon boundary => CODON_INSERTION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AAA' pos 0:	CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			codonsRef = codonsRef();
			codonsAlt = "";
			effType = EffectType.CODON_DELETION;
		} else {
			/**
			 * Length multiple of CODON_SIZE and insertion does not happen at codon boundary => CODON_CHANGE_PLUS_CODON_DELETION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AAC' pos 1:	ACC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'ACC' pos 2:	AAC GGG AAA CCC GGG AAA CCC GGG
			 */
			codonsRef = codonsRef();
			codonsAlt = codonsAlt();

			if (codonsAlt.isEmpty() || codonsRef.startsWith(codonsAlt)) {
				/**
				 * Note: It might happen that the last codon of the exon was deleted.
				 *       In this case there is no 'CODON_CHANGE'
				 * E.g.
				 * 		Original:				AAA CCC GGG AAA CCC GGG AAA CCC GGG
				 * 		Delete 'GGG' pos 24:	ACC CCC GGG AAA CCC GGG AAA CCC
				 *
				 * Note2: It may also be the case that the deleted bases are equal to the following ones.
				 *  E.g.
				 *  	Original:			ACG TCG TCC GGG AAA CCC GGG AAA CCC GGG
				 *  	Delete 'CGT' pos 1:	ACG TCC GGG AAA CCC GGG AAA CCC GGG
				 */
				effType = EffectType.CODON_DELETION;
			} else {
				effType = EffectType.CODON_CHANGE_PLUS_CODON_DELETION;
			}
		}

		effect(exon, effType, "", codonsRef, codonsAlt, codonStartNum, codonStartIndex, false);

		return true;
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	public String codonsAlt() {
		if (netCdsChange.isEmpty()) return "";

		int after = netCdsChange.length() + codonStartIndex;

		String prefix = codonsRef.length() >= codonStartIndex ? codonsRef.substring(0, codonStartIndex) : codonsRef;
		String suffix = codonsRef.length() > after ? codonsRef.substring(after) : "";

		String codonsAlt = prefix + suffix;
		return codonsAlt;
	}

	/**
	 * Get original codons in CDS
	 */
	@Override
	public String codonsRef() {
		if (netCdsChange.isEmpty()) return "";

		int min = variant.getStart();
		int max = variant.getEnd();
		int cdsBaseMin = cdsBaseNumber(min);
		int cdsBaseMax = cdsBaseNumber(max);

		// Swap?
		if (transcript.isStrandMinus()) {
			int swap = cdsBaseMin;
			cdsBaseMin = cdsBaseMax;
			cdsBaseMax = swap;
		}

		if (cdsBaseMax < cdsBaseMin) throw new RuntimeException("This should never happen!\n\tcdsBaseMin: " + cdsBaseMin + "\n\tcdsBaseMax: " + cdsBaseMax + "\n\tmin: " + min + "\n\tmax: " + max + "\n\tSeqChange: " + variant + "\n\ttranscript: " + transcript + "\n\tCDS.len: " + transcript.cds().length());

		int maxCodon = cdsBaseMax / CodonChange.CODON_SIZE;
		int minCodon = cdsBaseMin / CodonChange.CODON_SIZE;
		oldCodonCdsStart = (CodonChange.CODON_SIZE * minCodon);
		oldCodonCdsEnd = (CodonChange.CODON_SIZE * (maxCodon + 1)) - 1;

		String codons = "";
		if (oldCodonCdsEnd >= transcript.cds().length()) codons = transcript.cds().substring(oldCodonCdsStart);
		else codons = transcript.cds().substring(oldCodonCdsStart, oldCodonCdsEnd + 1);

		return codons;
	}

}
