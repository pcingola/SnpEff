package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Interval for a gene, as well as some other information: exons, utrs, cds, etc.
 *
 * @author pcingola
 *
 */
public class Downstream extends Marker {

	private static final long serialVersionUID = 1636197649250882952L;

	public Downstream(Transcript parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.DOWNSTREAM;
	}

	/**
	 * Distance to transcript
	 * @param seqChange
	 * @return
	 */
	public int distanceToTr(Variant seqChange) {
		int dist = (parent.isStrandPlus() ? seqChange.getStart() - start : end - seqChange.getStart()) + 1;
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
	public boolean variantEffect(Variant seqChange, VariantEffects changeEffects) {
		if (!intersects(seqChange)) return false; // Sanity check
		int distance = distanceToTr(seqChange);
		changeEffects.add(this, EffectType.DOWNSTREAM, distance + " bases");
		changeEffects.setDistance(distance);
		return true;
	}

}
