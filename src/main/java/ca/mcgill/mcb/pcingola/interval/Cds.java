package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * CDS: The coding region of a gene, also known as the coding sequence or CDS (from Coding DNA Sequence), is 
 * that portion of a gene's DNA or RNA, composed of exons, that codes for protein.
 * 
 * @author pcingola
 *
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

	/**
	 * Correct coordinates according to frame differences
	 * @param frameCorrection
	 */
	public boolean frameCorrection(int frameCorrection) {
		if (frameCorrection <= 0) return true; // Nothing to do

		// Can correct?
		if (size() <= frameCorrection) {
			Gpr.debug("CDS too short (size: " + size() + "), cannot correct frame!\n" + this);
			return false;
		}

		// Correct start or end coordinates
		if (isStrandPlus()) start += frameCorrection;
		else end -= frameCorrection;

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

	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		frame = (byte) markerSerializer.getNextFieldInt();
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + frame //
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

	@Override
	public String toString() {
		return getChromosomeName() + "\t" + start + "-" + end //
				+ " " //
				+ type //
				+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
				+ ", frame: " + frame //
		;
	}

}
