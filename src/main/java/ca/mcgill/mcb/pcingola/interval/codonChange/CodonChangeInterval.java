package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;

/**
 * Calculate codon changes produced by a Interval
 * 
 * Note: An interval does not produce any effect.
 * 
 * @author pcingola
 */
public class CodonChangeInterval extends CodonChange {

	public CodonChangeInterval(SeqChange seqChange, Transcript transcript, ChangeEffect changeEffect) {
		super(seqChange, transcript, changeEffect);
		returnNow = false; // An interval may affect more than one exon
	}

	/**
	 * Analyze
	 */
	@Override
	boolean codonChangeSingle(ChangeEffect changeEffect, Exon exon) {
		return false;
	}

}
