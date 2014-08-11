package ca.mcgill.mcb.pcingola.filter;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;

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
	 * @return
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
	public boolean filter(VariantEffect changeEffectResut) {
		return filterOut.contains(changeEffectResut.getEffectType());
	}

}
