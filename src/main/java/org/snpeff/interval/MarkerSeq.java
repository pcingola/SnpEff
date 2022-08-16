package org.snpeff.interval;

import org.snpeff.binseq.DnaNSequence;
import org.snpeff.binseq.DnaSequence;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.GprSeq;

import java.util.Arrays;

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
     * Apply a change type deletion (update sequence)
     */
    protected MarkerSeq applyDel(Variant variant) {
        MarkerSeq newMarker = (MarkerSeq) super.applyDel(variant);
        if (!shouldChangeSeq(variant)) return newMarker;

        // Get sequence in positive strand direction
        String seq = getSequencePositive();

        // Apply change to sequence
        int idxStart = variant.getStart() - getStart();
        int idxEnd = idxStart + variant.size();

        StringBuilder newSeq = new StringBuilder();
        if (idxStart >= 0) newSeq.append(seq.substring(0, idxStart));
        if (idxEnd >= 0 && (idxEnd < seq.length())) newSeq.append(seq.substring(idxEnd));

        // Update sequence
        newMarker.setSequencePositive(newSeq.toString());
        return newMarker;
    }

    /**
     * Apply a change type duplication (update sequence)
     */
    protected MarkerSeq applyDup(Variant variant) {
        MarkerSeq newMarker = (MarkerSeq) super.applyDup(variant);
        if (!shouldChangeSeq(variant)) return newMarker;

        // Get sequence in positive strand direction
        String seq = getSequencePositive();

        // Apply duplication to sequence
        String dupSeq = getSequence(intersect(variant));
        int idx = variant.getStart() - getStart() - 1;
        if (idx >= 0) seq = seq.substring(0, idx + 1) + dupSeq + seq.substring(idx + 1);
        else seq = dupSeq + seq;

        // Update sequence
        newMarker.setSequencePositive(seq);
        return newMarker;
    }

    /**
     * Apply a change type insertion (update sequence)
     */
    protected MarkerSeq applyIns(Variant variant) {
        MarkerSeq newMarker = (MarkerSeq) super.applyIns(variant);
        if (!shouldChangeSeq(variant)) return newMarker;

        // Get sequence in positive strand direction
        String seq = getSequencePositive();

        // Apply change to sequence
        String netChange = variant.netChange(this);
        int idx = variant.getStart() - getStart() - 1;
        if (idx >= 0) seq = seq.substring(0, idx + 1) + netChange + seq.substring(idx + 1);
        else seq = netChange + seq;

        // Update sequence
        newMarker.setSequencePositive(seq);
        return newMarker;
    }

    /**
     * Apply a change type MNP (update sequence)
     */
    protected MarkerSeq applyMnp(Variant variant) {
        MarkerSeq newMarker = (MarkerSeq) super.applyMnp(variant);
        if (!shouldChangeSeq(variant)) return newMarker;

        // Calculate indexes
        int idxStart = variant.getStart() - getStart();
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
        newMarker.setSequencePositive(seqsb.toString());
        return newMarker;
    }

    /**
     * Apply a change type SNP (update sequence)
     */
    protected MarkerSeq applySnp(Variant variant) {
        MarkerSeq newMarker = (MarkerSeq) super.applyMnp(variant);
        if (!shouldChangeSeq(variant)) return newMarker;

        // Get sequence in positive strand direction
        String seq = getSequencePositive();

        // Apply change to sequence
        int idx = variant.getStart() - getStart();
        seq = seq.substring(0, idx) + variant.getAlt() + seq.substring(idx + 1);

        // Update sequence
        newMarker.setSequencePositive(seq);
        return newMarker;
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
        int index = pos - getStart();
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
     * <p>
     * WARNING: Sequence is always according to coding
     * strand. E.g. if the strand is negative, the sequence
     * returned by this method is the reverse-WC that you see
     * in the reference genome
     */
    public String getSequence() {
        return sequence.toString();
    }

    /**
     * Set sequence
     * <p>
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

    /**
     * Get sequence always translated as "positive strand"
     */
    public String getSequencePositive() {
        return isStrandPlus() ? sequence.getSequence() : sequence.reverseWc().getSequence();
    }

    /**
     * Set sequence. The sequence is given in the positive strand
     */
    public void setSequencePositive(String sequence) {
        if (isStrandPlus()) setSequence(sequence);
        else setSequence(GprSeq.reverseWc(sequence));
    }

    /**
     * Get sequence intersecting 'marker'
     * <p>
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
        return (size() <= 0) // The marker has zero length, so sequence should be empty anyway (it is OK if its empty)
                || ((sequence != null) && (!sequence.isEmpty())) // Do we have a valid sequence?
                ;
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
     * Do we need to change the sequence?
     *
     * @return True if the variant intersects this marker, we need to change the sequence
     */
    boolean shouldChangeSeq(Variant variant) {
        return variant.intersects(this) && hasSequence();
    }


    @Override
    public String toString() {
        return getChromosomeName() + ":" + getStart() + "-" + getEndClosed() //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
                + (sequence != null ? ", sequence: " + sequence : "");
    }

    /**
     * Union of two markers
     */
    @Override
    public Marker union(Marker m) {
        if (!getChromosomeName().equals(m.getChromosomeName())) return null;
        MarkerSeq ms = (MarkerSeq) m;

        int ustart = Math.min(getStart(), m.getStart());
        int uend = Math.max(getEndClosed(), m.getEndClosed());

        // Merge sequence (only of the union overlaps)
        String seq = null;
        if (includes(m)) {
            seq = getSequence();
        } else if (m.includes(this)) {
            seq = ms.getSequence();
        } else if (intersects(m)) {
            // This interval is first
            if (getStart() < m.getStart()) {
                int overlap = getEndClosed() - m.getStart() + 1;
                seq = getSequence() + ms.getSequence().substring(overlap);
            } else {
                int overlap = m.getEndClosed() - getStart() + 1;
                seq = ms.getSequence() + getSequence().substring(overlap);
            }
        }

        // Create new marker using new coordinates
        MarkerSeq msNew = (MarkerSeq) this.clone();
        msNew.setStart(ustart);
        msNew.setEndClosed(uend);
        if (seq != null) msNew.setSequence(seq);

        return msNew;
    }

}
