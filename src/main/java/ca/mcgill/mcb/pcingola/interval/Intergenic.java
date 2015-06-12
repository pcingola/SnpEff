package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;

/**
 * Interval for in intergenic region
 *
 * @author pcingola
 *
 */
public class Intergenic extends Marker {

	private static final long serialVersionUID = -2487664381262354896L;

	String name;

	public Intergenic(Chromosome parent, int start, int end, boolean strandMinus, String id, String name) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.INTERGENIC;
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
