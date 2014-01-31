package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * A (putative) branch site.
 * 
 * @author pablocingolani
 */
public class SpliceSiteBranch extends SpliceSite {

	private static final long serialVersionUID = 7903892379174750342L;

	public SpliceSiteBranch() {
		super();
		type = EffectType.SPLICE_SITE_BRANCH;
	}

	public SpliceSiteBranch(Transcript parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.SPLICE_SITE_BRANCH;
	}

	/**
	 * These are NOT core splice sites
	 */
	@Override
	public boolean intersectsCoreSpliceSite(Marker marker) {
		return false;
	}

}
