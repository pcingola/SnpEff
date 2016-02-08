package org.snpeff.interval;

/**
 * A variant respect to non-reference (e.g. comparing cancer vs. somatic tissue).
 *
 * @author pcingola
 */
public class VariantNonRef extends Variant {

	private static final long serialVersionUID = 1L;
	Variant variantRef;

	public VariantNonRef() {
		super();
	}

	public VariantNonRef(Variant variant, Variant variantRef) {
		super(variant.getParent(), variant.getStart(), variant.getReference(), variant.getAlt(), variant.getId());
		genotype = variant.getGenotype();
		this.variantRef = variantRef;
	}

	@Override
	public String getGenotype() {
		return genotype + "-" + variantRef.getGenotype();
	}

	public Variant getVariantRef() {
		return variantRef;
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
		this.variantRef = variantRef;
	}

	@Override
	public String toString() {
		String valt = super.toString();
		String vref = variantRef.toString();
		return valt + "-" + vref;
	}
}
