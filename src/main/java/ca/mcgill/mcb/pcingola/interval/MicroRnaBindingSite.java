package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * miRna binding site (usually this was predicted by some algorithm)
 * 
 * @author pcingola
 */
public class MicroRnaBindingSite extends Marker {

	private static final long serialVersionUID = -9089500641817245554L;

	double pValue;

	public MicroRnaBindingSite(Marker parent, int start, int end, boolean strandMinus, String id, double pValue) {
		super(parent, start, end, strandMinus, id);
		this.pValue = pValue;
		type = EffectType.MICRO_RNA;
	}

	@Override
	public boolean variantEffect(Variant seqChange, VariantEffects changeEffects) {
		if (!intersects(seqChange)) return false; // Sanity check
		changeEffects.add(this, EffectType.MICRO_RNA, "" + pValue);
		return true;
	}

}
