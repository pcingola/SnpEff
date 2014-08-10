package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;

/**
 * NextProt annotation marker
 * 
 * @author pcingola
 */
public class NextProt extends Marker {

	private static final long serialVersionUID = 8939301304881007289L;

	String transcriptId;
	boolean highlyConservedAaSequence;

	public NextProt() {
		super();
		type = EffectType.NEXT_PROT;
	}

	public NextProt(Transcript transcript, int start, int end, String id) {
		super(transcript.getChromosome(), start, end, false, id);
		type = EffectType.NEXT_PROT;
		transcriptId = transcript.getId();
	}

	public String getTranscriptId() {
		return transcriptId;
	}

	public boolean isHighlyConservedAaSequence() {
		return highlyConservedAaSequence;
	}

	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		transcriptId = markerSerializer.getNextField();
		highlyConservedAaSequence = markerSerializer.getNextFieldBoolean();
	}

	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) + "\t" + transcriptId + "\t" + highlyConservedAaSequence;
	}

	public void setHighlyConservedAaSequence(boolean highlyConservedAaSequence) {
		this.highlyConservedAaSequence = highlyConservedAaSequence;
	}

}
