package org.snpeff.interval;

import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Log;

/**
 * CDS: The coding region of a gene, also known as the coding sequence or CDS (from Coding DNA Sequence), is
 * that portion of a gene's DNA or RNA, composed of exons, that codes for protein.
 *
 * @author pcingola
 */
public class Cds extends Marker implements MarkerWithFrame {

    private static final long serialVersionUID = 1636197649250882952L;

    byte frame = -1; // Frame can be {-1, 0, 1, 2}, where '-1' means unknown

    public Cds() {
        super();
        type = EffectType.CDS;
    }

    public Cds(Transcript parent, int start, int end, boolean strandMinus, String id) {
        super(parent, start, end, strandMinus, id);
        type = EffectType.CDS;
    }

    @Override
    public Cds cloneShallow() {
        Cds clone = (Cds) super.cloneShallow();
        clone.frame = frame;
        return clone;
    }

    /**
     * Correct coordinates according to frame differences
     */
    public boolean frameCorrection(int frameCorrection) {
        if (frameCorrection <= 0) return true; // Nothing to do

        // Can correct?
        if (size() <= frameCorrection) {
            Log.warning(ErrorWarningType.WARNING_CDS_TOO_SHORT, "CDS too short, cannot correct frame: frame size " + size() + ", frame correction " + frameCorrection + ", CDS: " + this);
            return false;
        }

        // Correct start or end coordinates
        if (isStrandPlus()) shiftStart(frameCorrection);
        else shiftEnd(-frameCorrection);

        // Correct frame
        frame = (byte) ((frame - frameCorrection) % 3);
        while (frame < 0)
            frame += 3;

        return true;
    }

    @Override
    public int getFrame() {
        return frame;
    }

    /**
     * Frame can be {-1, 0, 1, 2}, where '-1' means unknown
     */
    @Override
    public void setFrame(int frame) {
        if ((frame > 2) || (frame < -1)) throw new RuntimeException("Invalid frame value: " + frame);
        this.frame = (byte) frame;
    }

    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        frame = (byte) markerSerializer.getNextFieldInt();
    }

    /**
     * Create a string to serialize to a file
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + frame //
                ;
    }

    @Override
    public String toString() {
        return getChromosomeName() + "\t" + getStart() + "-" + getEndClosed() //
                + " " //
                + type //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
                + ", frame: " + frame //
                ;
    }

}
