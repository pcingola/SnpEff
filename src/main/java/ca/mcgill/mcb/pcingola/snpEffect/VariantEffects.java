package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * A sorted collection of variant effects
 *
 * @author pcingola
 */
public class VariantEffects implements Iterable<VariantEffect> {

	List<VariantEffect> effects;

	public VariantEffects(Variant variant) {
		effects = new ArrayList<VariantEffect>();
	}

	/**
	 * Add an effect
	 */
	public void add(Variant variant, Marker marker, EffectType effectType, EffectImpact effectImpact, String message) {
		VariantEffect effNew = new VariantEffect(variant);
		effNew.set(marker, effectType, effectImpact, message);
		add(effNew);
	}

	/**
	 * Add an effect
	 */
	public void add(Variant variant, Marker marker, EffectType effectType, String message) {
		add(variant, marker, effectType, effectType.effectImpact(), message);
	}

	/**
	 * Add an effect
	 */
	public void add(VariantEffect variantEffect) {
		effects.add(variantEffect);
	}

	public void addErrorWarning(ErrorWarningType errwarn) {
		VariantEffect veff = get();
		if (veff != null) veff.addErrorWarningInfo(errwarn);
		else Gpr.debug("Could not get latest " + VariantEffect.class.getSimpleName());
	}

	/**
	 * Get (or create) the latest ChangeEffect
	 */
	public VariantEffect get() {
		if (effects.isEmpty()) return null;
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

	public void setMarker(Marker marker) {
		VariantEffect veff = get();
		if (veff != null) veff.setMarker(marker);
		else Gpr.debug("Could not get latest VariantEffect");
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
