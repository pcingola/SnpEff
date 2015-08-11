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

	public Upstream() {
		super();
		type = EffectType.UPSTREAM;
	}

	public Upstream(Transcript parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.UPSTREAM;
	}

	/**
	 * Distance to transcript
	 */
	public int distanceToTr(Variant variant) {
		int dist = (parent.isStrandPlus() ? end - variant.getStart() : variant.getStart() - start) + 1;
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
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false; // Sanity check
		int distance = distanceToTr(variant);

		VariantEffect variantEffect = new VariantEffect(variant);
		variantEffect.set(this, type, type.effectImpact(), distance + " bases");
		variantEffect.setDistance(distance);
		variantEffects.add(variantEffect);

		return true;
	}
}
