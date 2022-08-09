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
	public static final String CHROMOSOME_START = "CHR_START";
	public static final String CHROMOSOME_END = "CHR_END";

	String name;

	/**
	 * Creates an intergenic marker based on the "space" between two genes
	 *
	 * @return null if the marker cannot be created
	 */
	public static Intergenic createIntergenic(Gene geneLeft, Gene geneRight) {
		if ((geneLeft == null) && (geneRight == null)) return null;

		// Left gene
		int start = 0;
		String gidLeft = CHROMOSOME_START;
		String gnameLeft = CHROMOSOME_START;
		Chromosome chr = null;

		if (geneLeft != null) {
			start = geneLeft.getEndClosed() + 1;
			gidLeft = geneLeft.getId();
			gnameLeft = geneLeft.getGeneName();
			chr = geneLeft.getChromosome();
		}

		// Right gene
		int end = -1;
		String gidRight = CHROMOSOME_END;
		String gnameRight = CHROMOSOME_END;
		if (geneRight != null) {
			gidRight = geneRight.getId();
			gnameRight = geneRight.getGeneName();
			end = geneRight.getStart() - 1;
			chr = geneRight.getChromosome();
		} else {
			end = chr.getEndClosed();
		}

		// Do not create marker if the coordinates are negative
		return start <= end ? new Intergenic(chr, start, end, false, gidLeft + "-" + gidRight, gnameLeft + "-" + gnameRight) : null;
	}

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
