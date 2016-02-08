package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

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

	public SpliceSiteRegion(Exon parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.SPLICE_SITE_REGION;
	}

	public SpliceSiteRegion(Intron parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.SPLICE_SITE_REGION;
	}

	@Override
	public boolean intersectsCoreSpliceSite(Marker marker) {
		return false;
	}

	public boolean isExonPart() {
		return parent instanceof Exon;
	}

	public boolean isIntronPart() {
		return parent instanceof Intron;
	}
}
