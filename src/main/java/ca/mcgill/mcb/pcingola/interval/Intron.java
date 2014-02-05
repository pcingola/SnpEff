package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Intron
 * 
 * @author pcingola
 */
public class Intron extends Marker {

	private static final long serialVersionUID = -8283322526157264389L;

	int rank; // Exon rank in transcript
	Exon exonBefore; // Exon before this intron
	Exon exonAfter; // Exon after this intron
	SpliceSiteRegion spliceSiteRegionStart;
	SpliceSiteRegion spliceSiteRegionEnd;

	public Intron(Transcript parent, int start, int end, int strand, String id, Exon exonBefore, Exon exonAfter) {
		super(parent, start, end, strand, id);
		type = EffectType.INTRON;
		this.exonAfter = exonAfter;
		this.exonBefore = exonBefore;
	}

	/**
	 * Create splice site region
	 * @return
	 */
	public SpliceSiteRegion createSpliceSiteRegionEnd(int sizeMin, int sizeMax) {
		if (spliceSiteRegionEnd != null) return spliceSiteRegionEnd;

		if (sizeMin < 0) return null;
		if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this marker
		if (sizeMax < sizeMin) return null; // Cannot be less than one base long
		spliceSiteRegionStart = new SpliceSiteRegion(this, start + sizeMin - 1, start + sizeMax - 1, strand, id);
		return spliceSiteRegionStart;
	}

	/**
	 * Create splice site region
	 * @return
	 */
	public SpliceSiteRegion createSpliceSiteRegionStart(int sizeMin, int sizeMax) {
		if (spliceSiteRegionStart != null) return spliceSiteRegionStart;

		if (sizeMin < 0) return null;
		if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this marker
		if (sizeMax < sizeMin) return null; // Cannot be less than one base long
		spliceSiteRegionEnd = new SpliceSiteRegion(this, start + sizeMin - 1, start + sizeMax - 1, strand, id);
		return spliceSiteRegionEnd;
	}

	public Exon getExonAfter() {
		return exonAfter;
	}

	public Exon getExonBefore() {
		return exonBefore;
	}

	public int getRank() {
		return rank;
	}

	public String getSpliceType() {
		return (exonBefore != null ? exonBefore.getSpliceType() : "") //
				+ "-" //
				+ (exonAfter != null ? exonAfter.getSpliceType() : "") //
		;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
