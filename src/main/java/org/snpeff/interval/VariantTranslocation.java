package org.snpeff.interval;

/**
 * A translocation consisting of two endpoints
 *
 * @author pcingola
 */
public class VariantTranslocation extends Variant {

	private static final long serialVersionUID = 1L;
	Marker endPoint;
	boolean left; // Is endPoint oriented to the left?
	boolean before; // Is endPoint before ALT?

	public VariantTranslocation() {
		super();
	}

	public VariantTranslocation(Marker parent, int start, String ref, String alt, Chromosome chrTr, int startTr, boolean right, boolean after) {
		super(parent, start, ref, alt);
		endPoint = new Marker(chrTr, startTr, startTr);
		endPoint.setStrandMinus(left);
		left = right;
		before = after;
		variantType = VariantType.BND;
	}

	@Override
	public Variant realignLeft() {
		// Do not realign translocations
		return this;
	}

	@Override
	public String toString() {
		String sep = left ? "]" : "[";
		String trPos = sep + endPoint.getChromosomeName() + ":" + endPoint.getStart() + sep;

		return "chr" + getChromosomeName() //
				+ ":" + start //
				+ "_" + getReference() //
				+ "/" //
				+ (before ? getAlt() + trPos : trPos + getAlt()) //
				;
	}
}
