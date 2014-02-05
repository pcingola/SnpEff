package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.ErrorType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.WarningType;
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
	SpliceSiteAcceptor spliceSiteAcceptor;
	SpliceSiteDonor spliceSiteDonor;
	SpliceSiteRegion spliceSiteRegionStart, spliceSiteRegionEnd;
	ExonSpliceType spliceType = ExonSpliceType.NONE;

	public Exon() {
		super();
		rank = 0;
		type = EffectType.EXON;
	}

	public Exon(Transcript parent, int start, int end, int strand, String id, int rank) {
		super(parent, start, end, strand, id);
		this.strand = (byte) strand;
		this.rank = rank;
		type = EffectType.EXON;
	}

	/**
	 * Apply seqChange to exon
	 * 
	 * WARNING: There might be conditions which change the exon type (e.g. an intron is deleted)
	 * 			Nevertheless ExonSpliceType s not updated since it reflects the exon type before a sequence change. 
	 * 
	 */
	@Override
	public Exon apply(SeqChange seqChange) {
		// Create new exon with updated coordinates
		Exon ex = (Exon) super.apply(seqChange);

		// Update sites
		if (spliceSiteAcceptor != null) ex.spliceSiteAcceptor = (SpliceSiteAcceptor) spliceSiteAcceptor.apply(seqChange);
		if (spliceSiteDonor != null) ex.spliceSiteDonor = (SpliceSiteDonor) spliceSiteDonor.apply(seqChange);

		return ex;
	}

	/**
	 * Check that the base in the exon corresponds with the one in the SNP
	 * @param seqChange
	 * @param results
	 */
	public void check(SeqChange seqChange, ChangeEffect results) {
		// Only makes sense for SNPs and MNPs
		if ((seqChange.getChangeType() != ChangeType.SNP) && (seqChange.getChangeType() != ChangeType.MNP)) return;

		int mstart = Math.max(seqChange.getStart(), start);
		int idxStart = mstart - start;
		if (sequence.length() <= 0) results.addWarning(WarningType.WARNING_SEQUENCE_NOT_AVAILABLE);
		else if (idxStart >= sequence.length()) results.addError(ErrorType.ERROR_OUT_OF_EXON);
		else {
			int mend = Math.min(seqChange.getEnd(), end);
			int len = mend - mstart + 1;

			String realReference = basesAt(idxStart, len).toUpperCase();
			String changeReference = seqChange.reference().substring(mstart - seqChange.getStart(), mend - seqChange.getStart() + 1);

			// Reference sequence different than expected?
			if (!realReference.equals(changeReference)) results.addWarning(WarningType.WARNING_REF_DOES_NOT_MATCH_GENOME);
		}
	}

	/**
	 * Create a splice site acceptor of 'maxSize' length
	 * @param size
	 * @return
	 */
	public SpliceSiteAcceptor createSpliceSiteAcceptor(int size) {
		if (spliceSiteAcceptor != null) return spliceSiteAcceptor;

		size = size - 1;
		if (size < 0) return null;

		if (strand >= 0) spliceSiteAcceptor = new SpliceSiteAcceptor(this, start - 1 - size, start - 1, strand, id);
		else spliceSiteAcceptor = new SpliceSiteAcceptor(this, end + 1, end + 1 + size, strand, id);

		return spliceSiteAcceptor;
	}

	/**
	 * Create a splice site donor of 'maxSize' length
	 * @param size
	 * @return
	 */
	public SpliceSiteDonor createSpliceSiteDonor(int size) {
		if (spliceSiteDonor != null) return spliceSiteDonor;

		size = size - 1;
		if (size < 0) return null;

		if (strand >= 0) spliceSiteDonor = new SpliceSiteDonor(this, end + 1, end + 1 + size, strand, id);
		else spliceSiteDonor = new SpliceSiteDonor(this, start - 1 - size, start - 1, strand, id);

		return spliceSiteDonor;
	}

	/**
	 * Create splice site regions
	 * @param sizeExon
	 * @param sizeIntron
	 * @return
	 */
	public SpliceSiteRegion createSpliceSiteRegionEnd(int size) {
		if (spliceSiteRegionEnd != null) return spliceSiteRegionEnd;

		if (size > size()) size = size(); // Cannot be larger than this marker
		if (size < 0) return null;

		if (isStrandPlus()) spliceSiteRegionEnd = new SpliceSiteRegion(this, end - (size - 1), end, strand, id);
		else spliceSiteRegionEnd = new SpliceSiteRegion(this, start, start + (size - 1), strand, id);

		return spliceSiteRegionEnd;
	}

	/**
	 * Create splice site regions
	 * @param sizeExon
	 * @param sizeIntron
	 * @return
	 */
	public SpliceSiteRegion createSpliceSiteRegionStart(int size) {
		if (spliceSiteRegionStart != null) return spliceSiteRegionStart;

		if (size > size()) size = size(); // Cannot be larger than this marker
		if (size < 0) return null;

		if (isStrandPlus()) spliceSiteRegionStart = new SpliceSiteRegion(this, start, start + (size - 1), strand, id);
		else spliceSiteRegionStart = new SpliceSiteRegion(this, end - (size - 1), end, strand, id);

		return spliceSiteRegionStart;
	}

	/**
	 * Correct exons according to frame information
	 * Shift the start position one base
	 */
	public void frameCorrection(int frameCorrection) {
		if (frameCorrection <= 0) return; // Nothing to do

		// Can correct?
		if (size() <= frameCorrection) Gpr.debug("Exon too short (size: " + size() + "), cannot correct frame!\n" + this);

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
	}

	@Override
	public int getFrame() {
		return frame;
	}

	public int getRank() {
		return rank;
	}

	public SpliceSiteAcceptor getSpliceSiteAcceptor() {
		return spliceSiteAcceptor;
	}

	public SpliceSiteDonor getSpliceSiteDonor() {
		return spliceSiteDonor;
	}

	public SpliceSiteRegion getSpliceSiteRegionEnd() {
		return spliceSiteRegionEnd;
	}

	public SpliceSiteRegion getSpliceSiteRegionStart() {
		return spliceSiteRegionStart;
	}

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
		if ((spliceSiteAcceptor != null) && marker.intersects(spliceSiteAcceptor)) markers.add(spliceSiteAcceptor);
		if ((spliceSiteDonor != null) && marker.intersects(spliceSiteDonor)) markers.add(spliceSiteDonor);
		return markers;
	}

	/**
	 * Parse a line from a serialized file
	 * @param line
	 * @return
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		frame = (byte) markerSerializer.getNextFieldInt();
		rank = markerSerializer.getNextFieldInt();
		setSequence(markerSerializer.getNextField());
		spliceSiteDonor = (SpliceSiteDonor) markerSerializer.getNextFieldMarker();
		spliceSiteAcceptor = (SpliceSiteAcceptor) markerSerializer.getNextFieldMarker();

		String exType = markerSerializer.getNextField();
		if ((exType != null) && !exType.isEmpty()) spliceType = ExonSpliceType.valueOf(exType);
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		int ssdId = markerSerializer.save(spliceSiteDonor);
		int ssaId = markerSerializer.save(spliceSiteAcceptor);

		return super.serializeSave(markerSerializer) //
				+ "\t" + frame //
				+ "\t" + rank //
				+ "\t" + sequence //
				+ "\t" + ssdId //
				+ "\t" + ssaId //
				+ "\t" + (spliceType != null ? spliceType.toString() : "")//				
		;
	}

	/**
	 * Frame can be {-1, 0, 1, 2}, where '-1' means unknown
	 * @param frame
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

}
