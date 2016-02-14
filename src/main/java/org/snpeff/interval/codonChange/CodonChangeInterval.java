package org.snpeff.interval.codonChange;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a Interval
 *
 * Note: An interval does not produce any effect.
 *
 * @author pcingola
 */
public class CodonChangeInterval extends CodonChange {

	public CodonChangeInterval(Variant seqChange, Transcript transcript, VariantEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = false; // An interval may affect more than one exon
	}

	/**
	 * Interval is not a variant, nothing to do
	 */
	@Override
	protected boolean codonChange(Exon exon) {
		return false;
	}

}
