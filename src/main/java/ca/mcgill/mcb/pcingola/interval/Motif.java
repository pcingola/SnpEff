package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.motif.Pwm;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Regulatory elements
 * 
 * @author pablocingolani
 */
public class Motif extends Marker {

	private static final long serialVersionUID = 8464487883781181867L;

	public static final double SCORE_THRESHOLD = 0.010;
	public static final boolean debug = false;

	String pwmId, pwmName;
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

	/**
	 * Calculate effect impact
	 * 
	 * Calculate the difference between the BEST possible score and the one produce by changing the BEST sequence using this 'seqChange'
	 * It would be better to use the real reference sequence, but at this moment, we do not have it.
	 * 
	 * 
	 * @param seqChange
	 * @return
	 */
	EffectImpact effectImpact(Variant seqChange) {
		if (pwm == null) return EffectImpact.MODIFIER;

		EffectImpact effectImpact = EffectImpact.MODIFIER;

		// Do we have PWM?
		if (pwm != null) {

			// Step 1: 
			//     Create a marker seq (we can 'apply' a change to it and see what the resulting sequence is 
			MarkerSeq mseq = new MarkerSeq((Marker) parent, start, end, false, id); // Notice: We use positive strand
			String seqBest = pwm.getBestSequenceStr();
			mseq.setSequence(isStrandPlus() ? seqBest : GprSeq.reverseWc(seqBest));
			if (seqChange.isStrandMinus()) throw new RuntimeException("SeqChange in minus strand not supported!\n\t" + seqChange);

			// Step 2:
			//     Calculate new sequence, by 'applying' seqChange to mseq.
			if (seqChange.isSnp() || seqChange.isMnp()) {
				MarkerSeq mseqNew = mseq.apply(seqChange);
				String seqChanged = mseqNew.getSequence();
				if (isStrandMinus()) seqChanged = GprSeq.reverseWc(seqChanged);

				// Calculate score difference
				double scoreBest = pwm.score(seqBest);
				double scoreNew = pwm.score(seqChanged);
				double diff = scoreBest - scoreNew;
				if (debug) Gpr.debug("Sequences: " + seqBest + "\t" + seqChanged + "\tScores: " + scoreBest + " + " + scoreNew + " = " + diff);

				// Over threshold?
				if (Math.abs(diff) > SCORE_THRESHOLD) effectImpact = EffectImpact.LOW;
			} else if (seqChange.isInDel()) effectImpact = EffectImpact.LOW;
		}

		return effectImpact;
	}

	public Pwm getPwm() {
		return pwm;
	}

	public String getPwmId() {
		return pwmId;
	}

	public String getPwmName() {
		return pwmName;
	}

	/**
	 * Calculate the effect of this seqChange
	 * @param seqChange
	 * @param changeEffects
	 * @return
	 */
	@Override
	public boolean seqChangeEffect(Variant seqChange, ChangeEffects changeEffects) {
		if (!intersects(seqChange)) return false;// Sanity check
		EffectType effType = EffectType.MOTIF;
		changeEffects.add(this, effType, "");

		// Calculate impact
		changeEffects.setEffectImpact(effectImpact(seqChange));

		return true;
	}

	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		pwmId = markerSerializer.getNextField();
		pwmName = markerSerializer.getNextField();
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + pwmId //
				+ "\t" + pwmName //
		;
	}

	public void setPwm(Pwm pwm) {
		this.pwm = pwm;
	}

}
