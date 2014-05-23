package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.binseq.DnaNSequence;
import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Marker with a DNA sequence
 * 
 * @author pcingola
 *
 */
public class MarkerSeq extends Marker {

	private static final long serialVersionUID = -8794533547221460207L;

	DnaSequence sequence;

	public MarkerSeq() {
		super();
		strand = 1;
		sequence = DnaSequence.empty();
	}

	public MarkerSeq(Marker parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
		this.strand = (byte) strand;
		sequence = DnaSequence.empty();
	}

	/**
	 * Apply seqChange to exon
	 * 
	 * WARNING: There might be conditions which change the exon type (e.g. an intron is deleted)
	 * 			Nevertheless ExonSpliceType s not updated since it reflects the exon type before a sequence change. 
	 * 
	 */
	@Override
	public MarkerSeq apply(Variant seqChange) {
		// Create new exon with updated coordinates
		MarkerSeq ex = (MarkerSeq) super.apply(seqChange);

		// Exon eliminated?
		if (ex == null) return null;

		// Sometimes 'apply' method return 'this'. Since we don't want to update the original exon, we have to create a clone
		if (ex == this) ex = (MarkerSeq) clone();

		if (seqChange.intersects(this)) {
			switch (seqChange.getChangeType()) {
			case SNP:
				applySnp(seqChange, ex);
				break;

			case INS:
				applyIns(seqChange, ex);
				break;

			case DEL:
				applyDel(seqChange, ex);
				break;

			case MNP:
				applyMnp(seqChange, ex);
				break;

			default:
				throw new RuntimeException("Unimplemented method for sequence change type " + seqChange.getChangeType());
			}
		} else ex.setSequence(getSequence());

		return ex;
	}

	/**
	 * Apply a change type deletion
	 * @param seqChange
	 * @param ex
	 */
	protected void applyDel(Variant seqChange, MarkerSeq ex) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {

			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			// Apply change to sequence
			int idxStart = seqChange.getStart() - start;
			int idxEnd = seqChange.getStart() - start + seqChange.size();

			StringBuilder newSeq = new StringBuilder();
			if (idxStart >= 0) newSeq.append(seq.substring(0, idxStart));
			if (idxEnd >= 0) newSeq.append(seq.substring(idxEnd));

			// Update sequence
			seq = newSeq.toString();
			ex.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Apply a change type insertion
	 * @param seqChange
	 * @param ex
	 */
	protected void applyIns(Variant seqChange, MarkerSeq ex) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {

			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			String netChange = seqChange.netChange(this);
			// Apply change to sequence
			int idx = seqChange.getStart() - start - 1;
			if (idx >= 0) seq = seq.substring(0, idx + 1) + netChange + seq.substring(idx + 1);
			else seq = netChange + seq;

			// Update sequence
			ex.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Apply a change type MNP
	 * @param seqChange
	 * @param ex
	 */
	protected void applyMnp(Variant seqChange, MarkerSeq ex) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {
			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			// Apply change to sequence
			int idxStart = seqChange.getStart() - start;
			int changeSize = seqChange.intersectSize(this);
			int idxEnd = seqChange.getStart() - start + changeSize;

			StringBuilder seqsb = new StringBuilder();
			seqsb.append(seq.substring(0, idxStart));
			seqsb.append(seqChange.getChange().substring(0, changeSize));
			seqsb.append(seq.substring(idxEnd));

			// Update sequence
			seq = seqsb.toString();
			ex.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Apply a change type SNP
	 * @param seqChange
	 * @param ex
	 */
	protected void applySnp(Variant seqChange, MarkerSeq ex) {
		// Update sequence
		if ((sequence != null) && (!sequence.isEmpty())) {
			// Get sequence in positive strand direction
			String seq = isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();

			// Apply change to sequence
			int idx = seqChange.getStart() - start;
			seq = seq.substring(0, idx) + seqChange.getChange() + seq.substring(idx + 1);

			// Update sequence
			ex.setSequence(isStrandPlus() ? seq : GprSeq.reverseWc(seq));
		}
	}

	/**
	 * Base in this marker at position 'index' (relative to marker start)
	 * @param idx
	 * @return
	 */
	public String basesAt(int index, int len) {
		if (strand < 0) {
			int idx = sequence.length() - index - len;
			return GprSeq.reverseWc(sequence.getBases(idx, len)); // Minus strand => Sequence has been reversed and WC-complemented
		}

		return sequence.getBases(index, len);
	}

	/**
	 * Base at position 'pos' (genomic coordinates)
	 * @param pos : Genomic coordinates
	 * @param len : Number of bases
	 * @return
	 */
	public String basesAtPos(int pos, int len) {
		int index = pos - start;
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
	 * 
	 * @param sequence
	 */
	public String getSequence() {
		return sequence.toString();
	}

	/**
	 * Do we have a sequence for this exon?
	 * @return
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
	 * 
	 * @param sequence
	 */
	public void setSequence(String sequence) {
		if ((sequence == null) || (sequence.length() <= 0)) this.sequence = DnaSequence.empty();

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
