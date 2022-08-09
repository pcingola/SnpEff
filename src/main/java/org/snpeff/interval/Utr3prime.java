package org.snpeff.interval;

import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Interval for a UTR (5 prime UTR and 3 prime UTR
 *
 * @author pcingola
 *
 */
public class Utr3prime extends Utr {

	private static final long serialVersionUID = 5688641008301281991L;

	public Utr3prime() {
		super();
		type = EffectType.UTR_3_PRIME;
	}

	public Utr3prime(Exon parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.UTR_3_PRIME;
	}

	@Override
	public boolean isUtr3prime() {
		return true;
	}

	@Override
	public boolean isUtr5prime() {
		return false;
	}

	/**
	 * Calculate distance from beginning of 3'UTRs
	 */
	@Override
	int utrDistance(Variant variant, Transcript tr) {
		int cdsEnd = tr.getCdsEnd();
		if (cdsEnd < 0) return -1;

		if (isStrandPlus()) return variant.getStart() - cdsEnd;
		return cdsEnd - variant.getEndClosed();
	}

	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;

		if (variant.includes(this) && (variant.getVariantType() == VariantType.DEL)) {
			variantEffects.addEffectType(variant, this, EffectType.UTR_3_DELETED); // A UTR was removed entirely
			return true;
		}

		Transcript tr = (Transcript) findParent(Transcript.class);
		int distance = utrDistance(variant, tr);

		if ((distance == 1) && (variant.lengthChange() % 3 != 0)) {
			// Variant (insertion) starting right before CDS' left side
			// E.g.:
			//       [ ... P  D  E  E  M  D  D  P  N  P  *]
			//                                            ^Variant here right after stop codon
			variantEffects.add(variant, this, EffectType.FRAME_SHIFT_AFTER_CDS_END, "");
		}

		VariantEffect variantEffect = new VariantEffect(variant);
		variantEffect.set(this, type, type.effectImpact(), distance >= 0 ? distance + " bases from CDS" : "");
		variantEffect.setDistance(distance);
		variantEffects.add(variantEffect);

		return true;
	}
}
