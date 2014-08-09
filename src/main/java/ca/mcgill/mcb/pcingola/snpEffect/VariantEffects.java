package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;

/**
 * A sorted collection of variant effects
 *
 * @author pcingola
 */
public class VariantEffects implements Iterable<VariantEffect> {

	Variant variant, variantRef;
	List<VariantEffect> effects;

	/**
	 *  An empty list of results;
	 */
	public static VariantEffects empty() {
		return new VariantEffects();
	}

	public VariantEffects() {
		effects = new ArrayList<VariantEffect>();
	}

	public VariantEffects(Variant variant) {
		effects = new ArrayList<VariantEffect>();
		this.variant = variant;
	}

	public VariantEffects(Variant variant, Variant variantRef) {
		effects = new ArrayList<VariantEffect>();
		this.variant = variant;
		this.variantRef = variantRef;
	}

	/**
	 * Add an effect
	 */
	public void add(Marker marker, EffectType effectType, String message) {
		VariantEffect effNew = new VariantEffect(variant, variantRef);
		effNew.set(marker, effectType, message);
		effects.add(effNew);
	}

	public void add(VariantEffect variantEffect) {
		effects.add(variantEffect);
	}

	public void add(VariantEffects variantEffects) {
		effects.addAll(variantEffects.effects);
	}

	public void addErrorWarning(ErrorWarningType errwarn) {
		get().addErrorWarning(errwarn);
	}

	/**
	 * Get (or create) the latest ChangeEffect
	 */
	public VariantEffect get() {
		if (effects.isEmpty()) effects.add(new VariantEffect(variant, variantRef));
		return effects.get(effects.size() - 1);

	}

	public VariantEffect get(int index) {
		return effects.get(index);
	}

	public boolean isEmpty() {
		return effects.isEmpty();
	}

	@Override
	public Iterator<VariantEffect> iterator() {
		return effects.iterator();
	}

	public void setCodons(String codonsOld, String codonsNew, int codonNum, int codonIndex) {
		EffectType newEffectType = get().setCodons(codonsOld, codonsNew, codonNum, codonIndex);

		// Sometime a new effect arises from setting codons (e.g. FRAME_SHIFT disrupts a STOP codon)
		if (newEffectType != null) {
			VariantEffect newEff = get().clone();
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

	public void sort() {
		Collections.sort(effects);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (VariantEffect eff : this)
			sb.append(eff + "\n");
		return sb.toString();
	}
}
