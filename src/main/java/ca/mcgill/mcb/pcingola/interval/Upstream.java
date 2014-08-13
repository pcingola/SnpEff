package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Interval for a gene, as well as some other information: exons, utrs, cds, etc.
 *
 * @author pcingola
 *
 */
public class Upstream extends Marker {

	private static final long serialVersionUID = 1636197649250882952L;

	public Upstream(Transcript parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.UPSTREAM;
	}

	/**
	 * Distance to transcript
	 * @param seqChange
	 * @return
	 */
	public int distanceToTr(Variant seqChange) {
		int dist = (parent.isStrandPlus() ? end - seqChange.getStart() : seqChange.getStart() - start) + 1;
		return Math.max(0, dist);
	}

	/**
	 * Upstream sites are no included in transcript (by definition).
	 */
	@Override
	protected boolean isShowWarningIfParentDoesNotInclude() {
		return false;
	}

	@Override
	public boolean variantEffect(Variant variants, VariantEffects variantEffects) {
		if (!intersects(variants)) return false; // Sanity check
		int distance = distanceToTr(variants);

		VariantEffect variantEffect = variantEffects.newVariantEffect();
		variantEffect.set(this, type, type.effectImpact(), distance + " bases");
		variantEffect.setDistance(distance);
		variantEffects.addEffect(variantEffect);

		return true;
	}
}
