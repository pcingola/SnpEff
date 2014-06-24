package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;

/**
 * Calculate codon changes produced by a deletion
 * @author pcingola
 */
public class CodonChangeDel extends CodonChange {

	int oldCodonCdsStart = -1;
	int oldCodonCdsEnd = -1;

	public CodonChangeDel(Variant seqChange, Transcript transcript, ChangeEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = false;
		requireNetCdsChange = true;
	}

	/**
	 * Analyze deletions in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	boolean codonChangeSingle(Exon exon) {
		// Is there any net effect?
		if (netCdsChange.isEmpty()) return false;

		if (seqChange.includes(exon)) {
			/**
			 * An exon has been entirely removed
			 */
			changeEffects.add(exon, EffectType.EXON_DELETED, "");
			changeEffects.setCodons("", "", -1, -1);
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
			changeEffects.add(exon, EffectType.FRAME_SHIFT, "");
			changeEffects.setCodons(codonsOld, "", codonNum, codonIndex);
		} else if (codonIndex == 0) {
			/**
			 * Length multiple of CODON_SIZE and insertion happens at codon boundary => CODON_INSERTION
			 * 	E.g. :
			 * 		Original:			AAA CCC GGG AAA CCC GGG AAA CCC GGG
			 * 		Delete 'AAA' pos 0:	CCC GGG AAA CCC GGG AAA CCC GGG
			 */
			codonsOld = codonsOld();
			changeEffects.add(exon, EffectType.CODON_DELETION, "");
			changeEffects.setCodons(codonsOld, "", codonNum, codonIndex);
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
				changeEffects.add(exon, EffectType.CODON_DELETION, "");
				changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
			} else {
				codonsOld = codonsOld();
				codonsNew = codonsNew();
				changeEffects.add(exon, EffectType.CODON_CHANGE_PLUS_CODON_DELETION, "");
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
		int after = netCdsChange.length() + codonIndex;
		String codonsNew = codonsOld.substring(0, codonIndex) //
				+ (codonsOld.length() > after ? codonsOld.substring(after) : "");

		return codonsNew;
	}

	/**
	 * Get original codons in CDS
	 * @param codonNum
	 * @return
	 */
	@Override
	public String codonsOld() {
		int min = seqChange.getStart();
		int max = seqChange.getEnd();
		int cdsBaseMin = cdsBaseNumber(min);
		int cdsBaseMax = cdsBaseNumber(max);

		// Swap?
		if (transcript.isStrandMinus()) {
			int swap = cdsBaseMin;
			cdsBaseMin = cdsBaseMax;
			cdsBaseMax = swap;
		}

		if (cdsBaseMax < cdsBaseMin) throw new RuntimeException("This should never happen!\n\tcdsBaseMin: " + cdsBaseMin + "\n\tcdsBaseMax: " + cdsBaseMax + "\n\tmin: " + min + "\n\tmax: " + max + "\n\tSeqChange: " + seqChange + "\n\ttranscript: " + transcript + "\n\tCDS.len: " + transcript.cds().length());

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
