package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;

/**
 * miRna binding site (usually this was predicted by some algorithm)
 *
 * @author pcingola
 */
public class MicroRnaBindingSite extends Marker {

	private static final long serialVersionUID = -9089500641817245554L;

	double pValue;

	public MicroRnaBindingSite() {
		super();
		type = EffectType.MICRO_RNA;
	}

	public MicroRnaBindingSite(Marker parent, int start, int end, boolean strandMinus, String id, double pValue) {
		super(parent, start, end, strandMinus, id);
		this.pValue = pValue;
		type = EffectType.MICRO_RNA;
	}

	@Override
	public MicroRnaBindingSite cloneShallow() {
		MicroRnaBindingSite clone = (MicroRnaBindingSite) super.cloneShallow();
		clone.pValue = pValue;
		return clone;
	}

	@Override
	public boolean variantEffect(Variant variant, VariantEffects changeEffects) {
		if (!intersects(variant)) return false; // Sanity check
		changeEffects.add(variant, this, EffectType.MICRO_RNA, "" + pValue);
		return true;
	}

}
