package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Interval for a splice site acceptor
 * 
 * From Sequence Ontology: A sequence variant in which a change has occurred 
 * within the region of the splice site, either within 1-3 bases of the exon 
 * or 3-8 bases of the intron.
 * 
 * @author pcingola
 */
public class SpliceSiteRegion extends SpliceSite {

	private static final long serialVersionUID = -7416687954435361328L;

	public SpliceSiteRegion() {
		super();
		type = EffectType.SPLICE_SITE_REGION;
	}

	public SpliceSiteRegion(Exon parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.SPLICE_SITE_REGION;
		Gpr.debug(this);
	}

	public SpliceSiteRegion(Intron parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		type = EffectType.SPLICE_SITE_REGION;
	}

	@Override
	public boolean intersectsCoreSpliceSite(Marker marker) {
		return false;
	}
}
