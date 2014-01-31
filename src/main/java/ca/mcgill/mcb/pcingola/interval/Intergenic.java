package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Interval for in intergenic region
 * 
 * @author pcingola
 *
 */
public class Intergenic extends Marker {

	private static final long serialVersionUID = -2487664381262354896L;

	public Intergenic(Chromosome parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.INTERGENIC;
	}

}
