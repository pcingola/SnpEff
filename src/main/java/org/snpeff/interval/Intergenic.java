package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

/**
 * Interval for in intergenic region
 *
 * @author pcingola
 *
 */
public class Intergenic extends Marker {

	private static final long serialVersionUID = -2487664381262354896L;

	String name;

	public Intergenic() {
		super();
		type = EffectType.INTERGENIC;
		name = "";
	}

	public Intergenic(Chromosome parent, int start, int end, boolean strandMinus, String id, String name) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.INTERGENIC;
		this.name = name;
	}

	@Override
	public Intergenic cloneShallow() {
		Intergenic clone = (Intergenic) super.cloneShallow();
		clone.name = name;
		return clone;
	}

	public String getName() {
		return name;
	}

}
