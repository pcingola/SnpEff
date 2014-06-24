package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Interval for a conserved non-coding region in an intron
 * 
 * @author pcingola
 *
 */
public class IntronConserved extends Marker {

	private static final long serialVersionUID = -3148108162409498012L;

	public IntronConserved(Gene parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.INTRON_CONSERVED;
	}

}
