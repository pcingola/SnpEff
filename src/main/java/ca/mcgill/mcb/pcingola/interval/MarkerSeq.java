package ca.mcgill.mcb.pcingola.interval;

import java.util.Arrays;

import ca.mcgill.mcb.pcingola.binseq.DnaNSequence;
import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.util.GprSeq;

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
		strandMinus = false;
		sequence = DnaSequence.empty();
	}

	public MarkerSeq(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		this.strandMinus = strandMinus;
		sequence = DnaSequence.empty();
	}

	/**
	 * Apply variant to exon
	 *
	 * WARNING: There might be conditions which change the exon type (e.g. an intron is deleted)
	 * 			Nevertheless ExonSpliceType s not updated since it reflects the exon type before a sequence change.
	 *
	 */
	@Override
	public MarkerSeq apply(Variant variant) {
		// Create new exon with updated coordinates
		MarkerSeq ms = (MarkerSeq) super.apply(variant);

		// Exon eliminated?
		if (ms == null) return null;

		// Sometimes 'apply' method return 'this'. Since we don't want to update the original marker, we have to create a clone
		if (ms == this) ms = (MarkerSeq) clone();

		if (variant.intersects(this)) {
			switch (variant.getVariantType()) {
			case SNP:
				applySnp(variant, ms);
				break;

			case INS:
				applyIns(variant, ms);
				break;

			case DEL:
				applyDel(variant, ms);
				break;

			case MNP:
				applyMnp(variant, ms);
				break;

			default:
				throw new RuntimeException("Unimplemented method for variant change type " + variant.getVariantType() + "\n\tVariant: " + variant);
			}
		} else ms.setSequence(getSequence());

		return ms;
	}

	/**
	 * Apply a change type deletion
	 */
	protected void applyDel(Variant variant, MarkerSeq markerSeq) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {

			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			// Apply change to sequence
			int idxStart = variant.getStart() - start;
			int idxEnd = idxStart + variant.size();

			StringBuilder newSeq = new StringBuilder();
			if (idxStart >= 0) newSeq.append(seq.substring(0, idxStart));
			if (idxEnd >= 0) newSeq.append(seq.substring(idxEnd));

			// Update sequence
			seq = newSeq.toString();
			markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Apply a change type insertion
	 */
	protected void applyIns(Variant variant, MarkerSeq markerSeq) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {

			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			String netChange = variant.netChange(this);
			// Apply change to sequence
			int idx = variant.getStart() - start - 1;
			if (idx >= 0) seq = seq.substring(0, idx + 1) + netChange + seq.substring(idx + 1);
			else seq = netChange + seq;

			// Update sequence
			markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Apply a change type MNP
	 */
	protected void applyMnp(Variant variant, MarkerSeq markerSeq) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {

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
	}

	/**
	 * Apply a change type SNP
	 */
	protected void applySnp(Variant variant, MarkerSeq markerSeq) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {
			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			// Apply change to sequence
			int idx = variant.getStart() - start;
			seq = seq.substring(0, idx) + variant.getAlt() + seq.substring(idx + 1);

			// Update sequence
			markerSeq.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
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
		int index = isStrandPlus() ? pos - start : end - pos;
		if (index < 0) return "";
		return basesAt(index, len);
	}

	/**
	 * Get  sequence
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
	 * Do we have a sequence for this exon?
	 */
	public boolean hasSequence() {
		if (size() <= 0) return true; // This interval has zero length, so sequence should be empty anyway (it is OK if its empty)
		return (sequence != null) && (!sequence.isEmpty());
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

}
