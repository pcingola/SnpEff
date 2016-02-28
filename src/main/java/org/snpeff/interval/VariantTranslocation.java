package org.snpeff.interval;

/**
 * A translocation consisting of two endpoints
 *
 * @author pcingola
 */
public class VariantTranslocation extends Variant {

	private static final long serialVersionUID = 1L;
	Variant variantEndPoint;

	public VariantTranslocation() {
		super();
	}

	public VariantTranslocation(Variant variant, Variant variantRef) {
		super(variant.getParent(), variant.getStart(), variant.getReference(), variant.getAlt(), variant.getId());
		genotype = variant.getGenotype();
		variantEndPoint = variantRef;
	}

	@Override
	public String getGenotype() {
		return genotype + "-" + variantEndPoint.getGenotype();
	}

	public Variant getVariantRef() {
		return variantEndPoint;
	}

	@Override
	public boolean isNonRef() {
		return true;
	}

	@Override
	public Variant realignLeft() {
		// Realigning in cancer samples is not trivial: What happens if one realigns and the other doesn't?
		// For now, do not realign
		return this;
	}

	public void setVariantRef(Variant variantRef) {
		variantEndPoint = variantRef;
	}

	@Override
	public String toString() {
		String valt = super.toString();
		String vref = variantEndPoint.toString();
		return valt + "-" + vref;
	}
}
