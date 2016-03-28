package org.snpeff.interval;

import java.util.Arrays;

import org.snpeff.binseq.DnaNSequence;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.GprSeq;

/**
 * Marker with a DNA sequence
 *
 * @author pcingola
 */
public class MarkerSeq extends Marker {

	private static final long serialVersionUID = -8794533547221460207L;

	DnaSequence sequence;

	public MarkerSeq() {
		super();
		type = EffectType.SEQUENCE;
		strandMinus = false;
		sequence = DnaSequence.empty();
	}

	public MarkerSeq(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.SEQUENCE;
		this.strandMinus = strandMinus;
		sequence = DnaSequence.empty();
	}

	public MarkerSeq(Marker parent, int start, int end, String seq) {
		this(parent, start, end, false, "");
		if (seq != null && !seq.isEmpty()) setSequence(seq);
	}

	/**
	 * Apply variant
	 */
	@Override
	public MarkerSeq apply(Variant variant) {
		if (!shouldApply(variant)) return this;

		// Create new MarkerSeq with updated coordinates
		MarkerSeq newMarkerSeq = (MarkerSeq) super.apply(variant);
		if (newMarkerSeq == null) return null;

		if (variant.intersects(this) && (sequence != null) && (!sequence.isEmpty())) {
			switch (variant.getVariantType()) {
			case SNP:
				applySnp(variant, newMarkerSeq);
				break;

			case INS:
				applyIns(variant, newMarkerSeq);
				break;

			case DEL:
				applyDel(variant, newMarkerSeq);
				break;

			case DUP:
				applyDup(variant, newMarkerSeq);
				break;

			case MNP:
				applyMnp(variant, newMarkerSeq);
				break;

			case MIXED:
				// When applying a MIXED variant, it is decomposed into two
				// variants (MNP+InDel) and each of them is applied to the
				// marker. So at this point the variants have been fully
				// applied and there is no need for further processing.
				break;

			default:
				throw new RuntimeException("Unimplemented method for variant change type " + variant.getVariantType() + "\n\tVariant: " + variant);
			}
		} else newMarkerSeq.setSequence(getSequence());

		return newMarkerSeq;
	}

	/**
	 * Apply a change type deletion (update sequence)
	 */
	protected void applyDel(Variant variant, MarkerSeq markerSeq) {
		// Get sequence in positive strand direction
		String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

		// Apply change to sequence
		int idxStart = variant.getStart() - start;
		int idxEnd = idxStart + variant.size();

		StringBuilder newSeq = new StringBuilder();
		if (idxStart >= 0) newSeq.append(seq.substring(0, idxStart));
		if (idxEnd >= 0 && (idxEnd < seq.length())) newSeq.append(seq.substring(idxEnd));

		// Update sequence
		seq = newSeq.toString();
		markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
	}

	/**
	 * Apply a change type duplication (update sequence)
	 */
	protected void applyDup(Variant variant, MarkerSeq markerSeq) {
		// Get sequence in positive strand direction
		String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

		// Apply duplication to sequence
		String dupSeq = getSequence(intersect(variant));
		int idx = variant.getStart() - start - 1;
		if (idx >= 0) seq = seq.substring(0, idx + 1) + dupSeq + seq.substring(idx + 1);
		else seq = dupSeq + seq;

		// Update sequence
		markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
	}

	/**
	 * Apply a change type insertion (update sequence)
	 */
	protected void applyIns(Variant variant, MarkerSeq markerSeq) {
		// Get sequence in positive strand direction
		String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

		// Apply change to sequence
		String netChange = variant.netChange(this);
		int idx = variant.getStart() - start - 1;
		if (idx >= 0) seq = seq.substring(0, idx + 1) + netChange + seq.substring(idx + 1);
		else seq = netChange + seq;

		// Update sequence
		markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
	}

	/**
	 * Apply a change type MNP (update sequence)
	 */
	protected void applyMnp(Variant variant, MarkerSeq markerSeq) {
		// Calculate indexes
		int idxStart = variant.getStart() - start;
		int idxAlt = 0;

		// Variant starts before this marker (e.g. motif with sequence)
		if (idxStart < 0) {
			idxAlt = -idxStart; // Remove first 'idxStart' bases from ALT sequence
			idxStart = 0;
		}

		int changeSize = variant.intersectSize(this);
		int idxEnd = idxStart + changeSize;

		// Apply variant to sequence
		String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence(); // Get sequence in positive strand direction
		StringBuilder seqsb = new StringBuilder();
		seqsb.append(seq.substring(0, idxStart).toLowerCase());
		String seqAlt = variant.getAlt().substring(idxAlt, idxAlt + changeSize).toUpperCase();
		seqsb.append(seqAlt);
		seqsb.append(seq.substring(idxEnd).toLowerCase());

		// Update sequence
		seq = seqsb.toString();
		markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
	}

	/**
	 * Apply a change type SNP (update sequence)
	 */
	protected void applySnp(Variant variant, MarkerSeq markerSeq) {
		// Get sequence in positive strand direction
		String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

		// Apply change to sequence
		int idx = variant.getStart() - start;
		seq = seq.substring(0, idx) + variant.getAlt() + seq.substring(idx + 1);

		// Update sequence
		markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
	}

	/**
	 * Base in this marker at position 'index' (relative to marker start)
	 */
	public String basesAt(int index, int len) {
		if (isStrandMinus()) {
			int idx = sequence.length() - index - len;
			return GprSeq.reverseWc(sequence.getBases(idx, len)); // Minus strand => Sequence has been reversed and WC-complemented
		}

		return sequence.getBases(index, len);
	}

	/**
	 * Base at position 'pos' (genomic coordinates)
	 */
	public String basesAtPos(int pos, int len) {
		// int index = isStrandPlus() ? pos - start : end - pos;
		int index = pos - start;
		if (index < 0) return "";
		return basesAt(index, len);
	}

	@Override
	public MarkerSeq cloneShallow() {
		MarkerSeq clone = (MarkerSeq) super.cloneShallow();
		clone.sequence = sequence.clone();
		return clone;
	}

	/**
	 * Get sequence
	 *
	 * WARNING: Sequence is always according to coding
	 * strand. E.g. if the strand is negative, the sequence
	 * returned by this method is the reverse-WC that you see
	 * in the reference genome
	 */
	public String getSequence() {
		return sequence.toString();
	}

	/**
	 * Get sequence intersecting 'marker'
	 *
	 * WARNING: Sequence is always according to coding
	 * strand. E.g. if the strand is negative, the sequence
	 * returned by this method is the reverse-WC that you see
	 * in the reference genome
	 */
	public String getSequence(Marker marker) {
		if (!includes(marker)) return null; // Cannot provide full sequence for this marker, since it's not fully included in this MarkerSeq
		return basesAtPos(marker.getStart(), marker.size());
	}

	/**
	 * Do we have a sequence for this exon?
	 */
	public boolean hasSequence() {
		if (size() <= 0) return true; // This interval has zero length, so sequence should be empty anyway (it is OK if its empty)
		return (sequence != null) && (!sequence.isEmpty());
	}

	/**
	 * Parse a line from a serialized file
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		setSequence(markerSerializer.getNextField());
	}

	/**
	 * Create a string to serialize to a file
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + sequence.getSequence() //
				;
	}

	/**
	 * Set sequence
	 *
	 * WARNING: Sequence is always according to coding
	 * strand. So use you should use setSequence( GprSeq.reverseWc( seq ) )
	 * if the marker is in negative strand.
	 */
	public void setSequence(String sequence) {
		if ((sequence == null) || (sequence.length() <= 0)) this.sequence = DnaSequence.empty();

		// Sometimes sequence length doesn't match interval length
		if (sequence.length() != size()) {

			if (sequence.length() > size()) {
				// Sequence is longer? => Trim sequence
				sequence = sequence.substring(0, size());
			} else {
				// Sequence is shorter? Pad with 'N'
				char ns[] = new char[size() - sequence.length()];
				Arrays.fill(ns, 'N');
				sequence = sequence + new String(ns);
			}
		}

		if (GprSeq.isAmbiguous(sequence)) this.sequence = new DnaNSequence(sequence); // Use DnaNSequence which supports ambiguous sequences
		else this.sequence = new DnaSequence(sequence); // Use DnaSequence
	}

	@Override
	public String toString() {
		return getChromosomeName() + ":" + start + "-" + end //
				+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
				+ (sequence != null ? ", sequence: " + sequence : "");
	}

	/**
	 * Union of two markers
	 * @return A new marker which is the union of the two
	 */
	@Override
	public Marker union(Marker m) {
		if (!getChromosomeName().equals(m.getChromosomeName())) return null;
		MarkerSeq ms = (MarkerSeq) m;

		int ustart = Math.min(start, m.getStart());
		int uend = Math.max(end, m.getEnd());

		// Merge sequence (only of the union overlaps)
		String seq = null;
		if (includes(m)) {
			seq = getSequence();
		} else if (m.includes(this)) {
			seq = ms.getSequence();
		} else if (intersects(m)) {
			// This interval is first
			if (start < m.start) {
				int overlap = end - m.start + 1;
				seq = getSequence() + ms.getSequence().substring(overlap);
			} else {
				int overlap = m.end - start + 1;
				seq = ms.getSequence() + getSequence().substring(overlap);
			}
		}

		// Create new marker using new coordinates
		MarkerSeq msNew = (MarkerSeq) this.clone();
		msNew.start = ustart;
		msNew.end = uend;
		if (seq != null) msNew.setSequence(seq);

		return msNew;
	}

}
