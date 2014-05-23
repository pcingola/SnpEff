package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.ErrorWarningType;

/**
 * A sorted collection of change effects
 * 
 * @author pcingola
 */
public class ChangeEffects implements Iterable<ChangeEffect> {

	Variant seqChange, seqChangeRef;
	List<ChangeEffect> effects;

	/**
	 *  An empty list of results;
	 * @return
	 */
	public static ChangeEffects empty() {
		return new ChangeEffects();
	}

	public ChangeEffects() {
		effects = new ArrayList<ChangeEffect>();
	}

	public ChangeEffects(Variant seqChange) {
		effects = new ArrayList<ChangeEffect>();
		this.seqChange = seqChange;
	}

	public ChangeEffects(Variant seqChange, Variant seqChangeRef) {
		effects = new ArrayList<ChangeEffect>();
		this.seqChange = seqChange;
		this.seqChangeRef = seqChangeRef;
	}

	public void add(ChangeEffect changeEffect) {
		effects.add(changeEffect);
	}

	public void add(ChangeEffects changeEffects) {
		effects.addAll(changeEffects.effects);
	}

	/**
	 * Add an effect
	 * @param marker
	 * @param effectType
	 * @param message
	 */
	public void add(Marker marker, EffectType effectType, String message) {
		ChangeEffect effNew = new ChangeEffect(seqChange, seqChangeRef);
		effNew.set(marker, effectType, message);
		effects.add(effNew);
	}

	public void addErrorWarning(ErrorWarningType errwarn) {
		get().addErrorWarning(errwarn);
	}

	/**
	 * Get (or create) the latest ChangeEffect
	 * @return
	 */
	public ChangeEffect get() {
		if (effects.isEmpty()) effects.add(new ChangeEffect(seqChange, seqChangeRef));
		return effects.get(effects.size() - 1);

	}

	public boolean isEmpty() {
		return effects.isEmpty();
	}

	@Override
	public Iterator<ChangeEffect> iterator() {
		return effects.iterator();
	}

	public void setCodons(String codonsOld, String codonsNew, int codonNum, int codonIndex) {
		EffectType newEffectType = get().setCodons(codonsOld, codonsNew, codonNum, codonIndex);

		// Sometime a new effect arises from setting codons (e.g. FRAME_SHIFT disrupts a STOP codon)
		if (newEffectType != null) {
			ChangeEffect newEff = get().clone();
			newEff.setEffectType(newEffectType);
			add(newEff);
		}
	}

	public void setCodonsAround(String codonsLeft, String codonsRight) {
		get().setCodonsAround(codonsLeft, codonsRight);

	}

	public void setDistance(int distance) {
		get().setDistance(distance);
	}

	public void setEffectImpact(EffectImpact effectImpact) {
		get().setEffectImpact(effectImpact);
	}

	public void setEffectType(EffectType effectType) {
		get().setEffectType(effectType);
	}

	public void setMarker(Marker marker) {
		get().setMarker(marker);
	}

	public int size() {
		return effects.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ChangeEffect eff : this)
			sb.append(eff + "\n");
		return sb.toString();
	}
}
