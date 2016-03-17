package org.snpeff.filter;

import java.util.HashSet;

import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;

/**
 * A Generic ChangeEffect filter
 *
 * @author pcingola
 */
public class VariantEffectFilter implements Filter<VariantEffect> {

	HashSet<EffectType> filterOut;

	public VariantEffectFilter() {
		filterOut = new HashSet<EffectType>();
	}

	public void add(EffectType effType) {
		filterOut.add(effType);
	}

	/**
	 * Is any of the options set?
	 */
	@Override
	public boolean anythingSet() {
		return !filterOut.isEmpty();
	}

	/**
	 * Filter a sequence change
	 * @return true if it passes the filter
	 */
	@Override
	public boolean filter(VariantEffect variantEffect) {
		for (EffectType et : filterOut)
			if (variantEffect.hasEffectType(et)) return true;
		return false;
	}
}
