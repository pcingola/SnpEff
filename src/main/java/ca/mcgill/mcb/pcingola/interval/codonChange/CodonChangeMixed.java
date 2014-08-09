package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a 'mixed' variant
 *
 * @author pcingola
 */
public class CodonChangeMixed extends CodonChange {

	int oldCodonCdsStart = -1;
	int oldCodonCdsEnd = -1;

	public CodonChangeMixed(Variant variant, Transcript transcript, VariantEffects changeEffects) {
		super(variant, transcript, changeEffects);
		returnNow = false;
		requireNetCdsChange = true;
	}

	/**
	 * Analyze mixed variants in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	boolean codonChangeSingle(Exon exon) {
		// Is there any net effect?
		if (netCdsChange.isEmpty()) return false;

		if (variant.includes(exon) && variant.isTruncation()) {
			/**
			 * An exon has been entirely removed
			 */
			variantEffects.add(exon, EffectType.EXON_DELETED, "");
			variantEffects.setCodons("", "", -1, -1);
		} else if (netCdsChange.length() % CodonChange.CODON_SIZE != 0) {
			/**
			 * Length not multiple of CODON_SIZE => FRAME_SHIFT
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AA' pos 0:	ACC CGG GAA ACC CGG GAA ACC CGG G
			 * 		Delete 'AA' pos 1:	ACC CGG GAA ACC CGG GAA ACC CGG G
			 * 		Delete 'AC' pos 2:	AAC CGG GAA ACC CGG GAA ACC CGG G
			 */
			codonsOld = codonsOld();
			variantEffects.add(exon, EffectType.FRAME_SHIFT, "");
			variantEffects.setCodons(codonsOld, "", codonNum, codonIndex);
		} else if (codonIndex == 0) {
			/**
			 * Length multiple of CODON_SIZE and insertion happens at codon boundary => CODON_INSERTION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AAA' pos 0:	CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			codonsOld = codonsOld();
			variantEffects.add(exon, EffectType.CODON_DELETION, "");
			variantEffects.setCodons(codonsOld, "", codonNum, codonIndex);
		} else {
			/**
			 * Length multiple of CODON_SIZE and insertion does not happen at codon boundary => CODON_CHANGE_PLUS_CODON_DELETION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AAC' pos 1:	ACC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'ACC' pos 2:	AAC GGG AAA CCC GGG AAA CCC GGG
			 */
			codonsOld = codonsOld();
			codonsNew = codonsNew();
			if (codonsNew.isEmpty() || codonsOld.startsWith(codonsNew)) {
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
				variantEffects.add(exon, EffectType.CODON_DELETION, "");
				variantEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
			} else {
				codonsOld = codonsOld();
				codonsNew = codonsNew();
				variantEffects.add(exon, EffectType.CODON_CHANGE_PLUS_CODON_DELETION, "");
				variantEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
			}
		}

		return true;
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	String codonsNew() {
		int after = netCdsChange.length() + codonIndex;
		String codonsNew = codonsOld.substring(0, codonIndex) //
				+ (codonsOld.length() > after ? codonsOld.substring(after) : "");

		return codonsNew;
	}

	/**
	 * Get original codons in CDS
	 */
	@Override
	public String codonsOld() {
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
