package org.snpeff.interval;

import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Log;

/**
 * NextProt annotation marker
 *
 * @author pcingola
 */
public class NextProt extends Marker {

    private static final long serialVersionUID = 8939301304881007289L;

    String transcriptId;
    boolean highlyConservedAaSequence;
    String name;

    public NextProt() {
        super();
        type = EffectType.NEXT_PROT;
    }

    public NextProt(Transcript transcript, int start, int end, String id) {
        super(transcript.getChromosome(), start, end, false, id);
        type = EffectType.NEXT_PROT;
        transcriptId = transcript.getId();
    }

    public NextProt(Transcript transcript, int start, int end, String id, String name) {
        super(transcript.getChromosome(), start, end, false, id);
        type = EffectType.NEXT_PROT;
        this.name = name;
        transcriptId = transcript.getId();
    }

    @Override
    public NextProt cloneShallow() {
        NextProt clone = (NextProt) super.cloneShallow();
        clone.transcriptId = transcriptId;
        clone.highlyConservedAaSequence = highlyConservedAaSequence;
        return clone;
    }

    public String getName() {
        return name;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    /**
     * Deferred analysis markers must be analyzed after 'standard' ones because their impact depends on other results
     * For instance, a NextProt marker's impact would be different if the variant is synonymous or non-synonymous
     */
    public boolean isDeferredAnalysis() {
        return true;
    }

    public boolean isHighlyConservedAaSequence() {
        return highlyConservedAaSequence;
    }

    public void setHighlyConservedAaSequence(boolean highlyConservedAaSequence) {
        this.highlyConservedAaSequence = highlyConservedAaSequence;
    }

    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        transcriptId = markerSerializer.getNextField();
        highlyConservedAaSequence = markerSerializer.getNextFieldBoolean();
        name = markerSerializer.getNextField();
    }

    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) + "\t" + transcriptId + "\t" + highlyConservedAaSequence + "\t" + name;
    }

    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false;

        // Assess effect impact
        // Impact depends on whether the effect is non-synonymous or other high impact effects
        EffectImpact effectImpact = EffectImpact.LOW;
        EffectImpact prevEffImpact = variantEffects.highestImpact(getTranscriptId());
        if (prevEffImpact == null)
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "NextProt '" + name + "' could not find previous effect impact for transcript '" + getTranscriptId() + "', ");
        else if (prevEffImpact == EffectImpact.HIGH)
            effectImpact = isHighlyConservedAaSequence() ? EffectImpact.HIGH : EffectImpact.MODERATE;
        else if (prevEffImpact == EffectImpact.MODERATE)
            effectImpact = isHighlyConservedAaSequence() ? EffectImpact.HIGH : EffectImpact.LOW;

        variantEffects.add(variant, this, type, effectImpact, name);
        return true;
    }

}
