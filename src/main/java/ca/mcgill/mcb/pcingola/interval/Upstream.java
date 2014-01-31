package ca.mcgill.mcb.pcingola.interval;

import java.util.List;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Interval for a gene, as well as some other information: exons, utrs, cds, etc.
 * 
 * @author pcingola
 *
 */
public class Upstream extends Marker {

	private static final long serialVersionUID = 1636197649250882952L;

	public Upstream(Transcript parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.UPSTREAM;
	}

	/**
	 * Distance to transcript
	 * @param seqChange
	 * @return
	 */
	public int distanceToTr(SeqChange seqChange) {
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
	public List<ChangeEffect> seqChangeEffect(SeqChange seqChange, ChangeEffect changeEffect) {
		if (!intersects(seqChange)) return ChangeEffect.emptyResults(); // Sanity check

		// Note: We need to use the transcripts's strand
		int distance = distanceToTr(seqChange);
		changeEffect.set(this, EffectType.UPSTREAM, distance + " bases");
		changeEffect.setDistance(distance);

		return changeEffect.newList();
	}

}
