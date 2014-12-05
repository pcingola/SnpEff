package ca.mcgill.mcb.pcingola.interval;

import java.util.ArrayList;

import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Interval for an exon
 *
 * @author pcingola
 */
public class Exon extends MarkerSeq implements MarkerWithFrame {

	/**
	 * Characterize exons based on alternative splicing
	 * References: "Alternative splicing and evolution - diversification, exon definition and function"  (see Box 1)
	 */
	public enum ExonSpliceType {
		NONE, // Not spliced
		RETAINED, // All transcripts have this exon
		SKIPPED, // Some transcripts skip it
		ALTTENATIVE_3SS, // Some transcripts have and alternative 3' exon start
		ALTTENATIVE_5SS, // Some transcripts have and alternative 5' exon end
		MUTUALLY_EXCLUSIVE, // Mutually exclusive (respect to other exon)
		ALTTENATIVE_PROMOMOTER, // The first exon is different in some transcripts.
		ALTTENATIVE_POLY_A, // The last exon.
	}

	public static int ToStringVersion = 2;

	private static final long serialVersionUID = 5324352193278472543L;

	byte frame = -1; // Frame can be {-1, 0, 1, 2}, where '-1' means unknown
	int rank; // Exon rank in transcript
	int aaIdxStart = -1, aaIdxEnd = -1; // First and last AA indexes that intersect with this exon
	//	SpliceSiteAcceptor spliceSiteAcceptor;
	//	SpliceSiteDonor spliceSiteDonor;
	//	SpliceSiteRegion spliceSiteRegionStart, spliceSiteRegionEnd;
	ArrayList<SpliceSite> spliceSites;
	ExonSpliceType spliceType = ExonSpliceType.NONE;

	public Exon() {
		super();
		rank = 0;
		type = EffectType.EXON;
		spliceSites = new ArrayList<SpliceSite>();
	}

	public Exon(Transcript parent, int start, int end, boolean strandMinus, String id, int rank) {
		super(parent, start, end, strandMinus, id);
		this.strandMinus = strandMinus;
		this.rank = rank;
		type = EffectType.EXON;
		spliceSites = new ArrayList<SpliceSite>();
	}

	/**
	 * Add a splice site to the collection
	 */
	public void add(SpliceSite ss) {
		spliceSites.add(ss);
	}

	/**
	 * Apply variant to exon
	 *
	 * WARNING: There might be conditions which change the exon type (e.g. an intron is deleted)
	 * 			Nevertheless ExonSpliceType s not updated since it reflects the exon type before a sequence change.
	 *
	 */
	@Override
	public Exon apply(Variant variant) {
		// Create new exon with updated coordinates
		Exon ex = (Exon) super.apply(variant);

		// Update splice sites
		for (SpliceSite ss : spliceSites)
			ex.add((SpliceSite) ss.apply(variant));

		return ex;
	}

	/**
	 * Create a splice site acceptor of 'maxSize' length
	 */
	public SpliceSiteAcceptor createSpliceSiteAcceptor(int size) {
		size = size - 1;
		if (size < 0) return null;

		int ssstart, ssend;
		if (isStrandPlus()) {
			ssstart = start - 1 - size;
			ssend = start - 1;
		} else {
			ssstart = end + 1;
			ssend = end + 1 + size;
		}

		Intron intron = ((Transcript) parent).findIntron(ssstart);
		if (intron == null) return null;

		SpliceSiteAcceptor spliceSiteAcceptor = new SpliceSiteAcceptor(intron, ssstart, ssend, strandMinus, id);
		add(spliceSiteAcceptor);

		return spliceSiteAcceptor;
	}

	/**
	 * Create a splice site donor of 'maxSize' length
	 */
	public SpliceSiteDonor createSpliceSiteDonor(int size) {
		size = size - 1;
		if (size < 0) return null;

		int ssstart, ssend;
		if (isStrandPlus()) {
			ssstart = end + 1;
			ssend = end + 1 + size;
		} else {
			ssstart = start - 1 - size;
			ssend = start - 1;
		}

		Intron intron = ((Transcript) parent).findIntron(ssstart);
		if (intron == null) return null;

		SpliceSiteDonor spliceSiteDonor = new SpliceSiteDonor(intron, ssstart, ssend, strandMinus, id);
		add(spliceSiteDonor);

		return spliceSiteDonor;
	}

	/**
	 * Create splice site regions
	 */
	public SpliceSiteRegion createSpliceSiteRegionEnd(int size) {
		if (size > size()) size = size(); // Cannot be larger than this marker
		if (size <= 0) return null;

		SpliceSiteRegion spliceSiteRegionEnd = null;
		if (isStrandPlus()) spliceSiteRegionEnd = new SpliceSiteRegion(this, end - (size - 1), end, strandMinus, id);
		else spliceSiteRegionEnd = new SpliceSiteRegion(this, start, start + (size - 1), strandMinus, id);

		if (spliceSiteRegionEnd != null) add(spliceSiteRegionEnd);

		return spliceSiteRegionEnd;
	}

	/**
	 * Create splice site regions
	 */
	public SpliceSiteRegion createSpliceSiteRegionStart(int size) {
		if (size > size()) size = size(); // Cannot be larger than this marker
		if (size <= 0) return null;

		SpliceSiteRegion spliceSiteRegionStart = null;
		if (isStrandPlus()) spliceSiteRegionStart = new SpliceSiteRegion(this, start, start + (size - 1), strandMinus, id);
		else spliceSiteRegionStart = new SpliceSiteRegion(this, end - (size - 1), end, strandMinus, id);

		if (spliceSiteRegionStart != null) add(spliceSiteRegionStart);

		return spliceSiteRegionStart;
	}

	/**
	 * Correct exons according to frame information
	 * Shift the start position one base
	 */
	public boolean frameCorrection(int frameCorrection) {
		if (frameCorrection <= 0) return true; // Nothing to do

		// Can correct?
		if (size() <= frameCorrection) {
			Gpr.debug("Exon too short (size: " + size() + "), cannot correct frame!\n" + this);
			return false;
		}

		// Correct start or end coordinates
		if (isStrandPlus()) start += frameCorrection;
		else end -= frameCorrection;

		// Correct frame
		frame = (byte) ((frame - frameCorrection) % 3);
		while (frame < 0)
			frame += 3;

		// Correct sequence
		String sequence = getSequence();
		if (sequence.length() >= frameCorrection) sequence = sequence.substring(frameCorrection);
		setSequence(sequence);

		return true;
	}

	public int getAaIdxEnd() {
		return aaIdxEnd;
	}

	public int getAaIdxStart() {
		return aaIdxStart;
	}

	@Override
	public int getFrame() {
		return frame;
	}

	public int getRank() {
		return rank;
	}

	public ArrayList<SpliceSite> getSpliceSites() {
		return spliceSites;
	}

	//	public SpliceSiteAcceptor getSpliceSiteAcceptor() {
	//		return spliceSiteAcceptor;
	//	}
	//
	//	public SpliceSiteDonor getSpliceSiteDonor() {
	//		return spliceSiteDonor;
	//	}
	//
	//	public SpliceSiteRegion getSpliceSiteRegionEnd() {
	//		return spliceSiteRegionEnd;
	//	}
	//
	//	public SpliceSiteRegion getSpliceSiteRegionStart() {
	//		return spliceSiteRegionStart;
	//	}

	public ExonSpliceType getSpliceType() {
		return spliceType;
	}

	@Override
	protected boolean isAdjustIfParentDoesNotInclude(Marker parent) {
		return true;
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

	/**
	 * Check that the base in the exon corresponds with the one in the SNP
	 */
	public ErrorWarningType sanityCheck(Variant variant) {
		if (!intersects(variant)) return null;

		// Only makes sense for SNPs and MNPs
		if ((variant.getVariantType() != VariantType.SNP) && (variant.getVariantType() != VariantType.MNP)) return null;

		int mstart = Math.max(variant.getStart(), start);
		int idxStart = mstart - start;

		if (sequence.length() <= 0) return ErrorWarningType.WARNING_SEQUENCE_NOT_AVAILABLE;
		if (idxStart >= sequence.length()) return ErrorWarningType.ERROR_OUT_OF_EXON;

		int mend = Math.min(variant.getEnd(), end);
		int len = mend - mstart + 1;

		String realReference = basesAt(idxStart, len).toUpperCase();

		int chRefStart = mstart - variant.getStart();
		if (chRefStart < 0) return ErrorWarningType.ERROR_OUT_OF_EXON;

		int chRefEnd = mend - variant.getStart();
		String refStr = variant.reference();
		if (chRefEnd >= refStr.length()) return ErrorWarningType.ERROR_OUT_OF_EXON;

		String changeReference = refStr.substring(chRefStart, chRefEnd + 1);

		// Reference sequence different than expected?
		if (!realReference.equals(changeReference)) return ErrorWarningType.WARNING_REF_DOES_NOT_MATCH_GENOME;

		// OK
		return null;
	}

	/**
	 * Parse a line from a serialized file
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		frame = (byte) markerSerializer.getNextFieldInt();
		rank = markerSerializer.getNextFieldInt();
		setSequence(markerSerializer.getNextField());
		//		spliceSiteDonor = (SpliceSiteDonor) markerSerializer.getNextFieldMarker();
		//		spliceSiteAcceptor = (SpliceSiteAcceptor) markerSerializer.getNextFieldMarker();

		String exType = markerSerializer.getNextField();
		if ((exType != null) && !exType.isEmpty()) spliceType = ExonSpliceType.valueOf(exType);
	}

	/**
	 * Create a string to serialize to a file
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		// Note: We do not save splice sites any more, since they can be created "on demand"
		//		int ssdId = markerSerializer.save(spliceSiteDonor);
		//		int ssaId = markerSerializer.save(spliceSiteAcceptor);

		return super.serializeSave(markerSerializer) //
				+ "\t" + frame //
				+ "\t" + rank //
				+ "\t" + sequence //
				//				+ "\t" + ssdId //
				//				+ "\t" + ssaId //
				+ "\t" + (spliceType != null ? spliceType.toString() : "")//
				;
	}

	public void setAaIdx(int aaIdxStart, int aaIdxEnd) {
		this.aaIdxStart = aaIdxStart;
		this.aaIdxEnd = aaIdxEnd;
	}

	/**
	 * Frame can be {-1, 0, 1, 2}, where '-1' means unknown
	 */
	@Override
	public void setFrame(int frame) {
		if ((frame > 2) || (frame < -1)) throw new RuntimeException("Invalid frame value: " + frame);
		this.frame = (byte) frame;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		switch (ToStringVersion) {
		case 1:
			// Old format version: Used in some testCases
			return getChromosomeName() + ":" + start + "-" + end //
					+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
					+ " rank:" + rank //
					+ (sequence != null ? ", sequence: " + sequence : "");

		case 2:
			return getChromosomeName() + ":" + start + "-" + end //
					+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
					+ ", rank: " + rank //
					+ ", frame: " + (frame >= 0 ? "" + frame : ".") //
					+ (sequence != null ? ", sequence: " + sequence : "");

		default:
			throw new RuntimeException("Unknown format version: " + ToStringVersion);
		}
	}

	/**
	 * Note: This only adds spliceSites effects, for detailed
	 *       codon changes effects we use 'CodonChange' class
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {

		for (SpliceSite ss : spliceSites)
			if (ss.intersects(variant)) ss.variantEffect(variant, variantEffects);

		return true;
	}

}
