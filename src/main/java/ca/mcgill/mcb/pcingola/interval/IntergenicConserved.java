package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Interval for a conserved intergenic region
 * 
 * @author pcingola
 *
 */
public class IntergenicConserved extends Marker {

	private static final long serialVersionUID = -1816568396090993792L;

	public IntergenicConserved(Chromosome parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.INTERGENIC_CONSERVED;
	}

}
