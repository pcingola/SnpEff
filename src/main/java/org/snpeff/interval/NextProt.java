package org.snpeff.interval;

import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;

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

	@Override
	public NextProt cloneShallow() {
		NextProt clone = (NextProt) super.cloneShallow();
		clone.transcriptId = transcriptId;
		clone.highlyConservedAaSequence = highlyConservedAaSequence;
		return clone;
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

	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;

		// Assess effect impact
		EffectImpact effectImpact = EffectImpact.MODIFIER;
		if (isHighlyConservedAaSequence()) effectImpact = EffectImpact.MODERATE;
		else effectImpact = EffectImpact.LOW;

		variantEffects.add(variant, this, type, effectImpact, "");
		return true;
	}

}
