package org.snpeff.interval;

import org.snpeff.motif.Pwm;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Regulatory elements
 *
 * @author pablocingolani
 */
public class Motif extends Marker {

    public static final double SCORE_THRESHOLD = 0.010;
    public static final boolean debug = false;
    private static final long serialVersionUID = 8464487883781181867L;
    String pwmId;
    String pwmName;
    Pwm pwm;

    public Motif() {
        super();
        type = EffectType.MOTIF;
    }

    public Motif(Marker parent, int start, int end, boolean strandMinus, String id, String pwmName, String pwmId) {
        super(parent, start, end, strandMinus, id);
        type = EffectType.MOTIF;
        this.pwmName = pwmName;
        this.pwmId = pwmId;
    }

    @Override
    public Motif cloneShallow() {
        Motif clone = (Motif) super.cloneShallow();
        clone.pwmId = pwmId;
        clone.pwmName = pwmName;
        return clone;
    }

    /**
     * Calculate effect impact
     * <p>
     * Calculate the difference between the BEST possible score and the one produce by changing the BEST sequence using this 'variant'
     * It would be better to use the real reference sequence, but at this moment, we do not have it.
     */
    EffectImpact effectImpact(Variant variant) {
        EffectImpact effectImpact = EffectImpact.MODIFIER;

        // Do we have PWM?
        if (pwm != null) {
            // Step 1:
            //     Create a MarkerSeq (we can 'apply' a change to it and see what the resulting sequence is
            MarkerSeq mseq = new MarkerSeq((Marker) parent, getStart(), getEndClosed(), false, id); // Notice: We use positive strand
            String seqBest = pwm.getBestSequenceStr();
            mseq.setSequence(isStrandPlus() ? seqBest : GprSeq.reverseWc(seqBest));

            // Step 2:
            //     Calculate new sequence, by 'applying' variant to mseq.
            if (variant.isSnp() || variant.isMnp()) {
                MarkerSeq mseqNew = (MarkerSeq) mseq.apply(variant);
                String variantd = mseqNew.getSequence();
                if (isStrandMinus()) variantd = GprSeq.reverseWc(variantd);

                // Calculate score difference
                double scoreBest = pwm.score(seqBest);
                double scoreNew = pwm.score(variantd);
                double diff = scoreBest - scoreNew;
                if (debug) Log.debug("Sequences: " + seqBest + "\t" + variantd + "\tScores: " + scoreBest + " + " + scoreNew + " = " + diff);

                // Over threshold?
                if (Math.abs(diff) > SCORE_THRESHOLD) effectImpact = EffectImpact.LOW;
            } else if (variant.isInDel()) effectImpact = EffectImpact.LOW;
        }

        return effectImpact;
    }

    public Pwm getPwm() {
        return pwm;
    }

    public void setPwm(Pwm pwm) {
        this.pwm = pwm;
    }

    public String getPwmId() {
        return pwmId;
    }

    public String getPwmName() {
        return pwmName;
    }

    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        pwmId = markerSerializer.getNextField();
        pwmName = markerSerializer.getNextField();
    }

    /**
     * Create a string to serialize to a file
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + pwmId //
                + "\t" + pwmName //
                ;
    }

    /**
     * Calculate the effect of this variant
     */
    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false;// Sanity check

        if (variant.isDel() && variant.includes(this)) {
            // Site deleted?
            variantEffects.add(variant, this, EffectType.MOTIF_DELETED, EffectImpact.LOW, "");
        } else {
            variantEffects.add(variant, this, type, effectImpact(variant), "");
        }

        return true;
    }

}
