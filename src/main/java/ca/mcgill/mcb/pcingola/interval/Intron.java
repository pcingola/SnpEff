package ca.mcgill.mcb.pcingola.interval;

import java.util.ArrayList;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

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
	//	SpliceSiteRegion spliceSiteRegionStart;
	//	SpliceSiteRegion spliceSiteRegionEnd;
	ArrayList<SpliceSite> spliceSites;

	public Intron(Transcript parent, int start, int end, boolean strandMinus, String id, Exon exonBefore, Exon exonAfter) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.INTRON;
		this.exonAfter = exonAfter;
		this.exonBefore = exonBefore;
		spliceSites = new ArrayList<SpliceSite>();
	}

	/**
	 * Add a splice site to the collection
	 */
	public void add(SpliceSite ss) {
		spliceSites.add(ss);
	}

	@Override
	public Intron apply(Variant variant) {
		// Create new exon with updated coordinates
		Intron intr = (Intron) super.apply(variant);

		// Update splice sites
		for (SpliceSite ss : spliceSites)
			intr.add((SpliceSite) ss.apply(variant));

		return intr;
	}

	/**
	 * Create splice site region
	 */
	public SpliceSiteRegion createSpliceSiteRegionEnd(int sizeMin, int sizeMax) {
		if (sizeMin < 0) return null;
		if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this marker
		if (sizeMax <= sizeMin) return null; // Cannot be less than one base long

		SpliceSiteRegion spliceSiteRegionEnd = null;
		if (isStrandPlus()) spliceSiteRegionEnd = new SpliceSiteRegion(this, end - (sizeMax - 1), end - (sizeMin - 1), strandMinus, id);
		else spliceSiteRegionEnd = new SpliceSiteRegion(this, start + sizeMin - 1, start + sizeMax - 1, strandMinus, id);

		if (spliceSiteRegionEnd != null) add(spliceSiteRegionEnd);

		return spliceSiteRegionEnd;
	}

	/**
	 * Create splice site region
	 */
	public SpliceSiteRegion createSpliceSiteRegionStart(int sizeMin, int sizeMax) {
		if (sizeMin < 0) return null;
		if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this marker
		if (sizeMax <= sizeMin) return null; // Cannot be less than one base long

		SpliceSiteRegion spliceSiteRegionStart = null;
		if (isStrandPlus()) spliceSiteRegionStart = new SpliceSiteRegion(this, start + (sizeMin - 1), start + (sizeMax - 1), strandMinus, id);
		else spliceSiteRegionStart = new SpliceSiteRegion(this, end - (sizeMax - 1), end - (sizeMin - 1), strandMinus, id);

		if (spliceSiteRegionStart != null) add(spliceSiteRegionStart);

		return spliceSiteRegionStart;
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

	public ArrayList<SpliceSite> getSpliceSites() {
		return spliceSites;
	}

	//	public SpliceSiteRegion getSpliceSiteRegionEnd() {
	//		return spliceSiteRegionEnd;
	//	}
	//
	//	public SpliceSiteRegion getSpliceSiteRegionStart() {
	//		return spliceSiteRegionStart;
	//	}

	public String getSpliceType() {
		return (exonBefore != null ? exonBefore.getSpliceType() : "") //
				+ "-" //
				+ (exonAfter != null ? exonAfter.getSpliceType() : "") //
		;
	}

	/**
	 * Query all genomic regions that intersect 'marker'
	 */
	@Override
	public Markers query(Marker marker) {
		Markers markers = new Markers();

		for (SpliceSite ss : spliceSites)
			if (ss.intersects(marker)) markers.add(ss);

		return markers;
	}

	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		// Note: We do not save splice sites
		super.serializeParse(markerSerializer);
	}

	/**
	 * Create a string to serialize to a file
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		// Note: We do not save splice sites
		return super.serializeSave(markerSerializer);
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		for (SpliceSite ss : spliceSites)
			if (ss.intersects(variant)) ss.variantEffect(variant, variantEffects);

		// Add intron part
		variantEffects.addEffect(variant, this, EffectType.INTRON, "");

		return true;
	}

}
