package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

/**
 * Interval for a conserved intergenic region
 *
 * @author pcingola
 *
 */
public class IntergenicConserved extends Marker {

	private static final long serialVersionUID = -1816568396090993792L;

	public IntergenicConserved() {
		super();
		type = EffectType.INTERGENIC_CONSERVED;
	}

	public IntergenicConserved(Chromosome parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.INTERGENIC_CONSERVED;
	}

}
