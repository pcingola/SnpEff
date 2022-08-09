package org.snpeff.interval;

import java.util.Collections;
import java.util.Iterator;

import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.KeyValue;

/**
 * This is a custom interval (i.e. intervals provided by the user)
 *
 * @author pcingola
 */
public class Custom extends Marker implements Iterable<KeyValue<String, String>> {

	private static final long serialVersionUID = -6843535415295857726L;

	String label;
	double score = Double.NaN;

	public Custom() {
		super();
		type = EffectType.CUSTOM;
		label = "";
	}

	public Custom(Marker parent, int start, int end, boolean strandMinus, String id, String label) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.CUSTOM;

		this.label = label;
		if (label == null || label.isEmpty()) label = id;
	}

	@Override
	public Custom cloneShallow() {
		Custom clone = (Custom) super.cloneShallow();
		clone.label = label;
		clone.score = score;
		return clone;
	}

	public String getLabel() {
		return label;
	}

	public double getScore() {
		return score;
	}

	/**
	 * Do we have additional annotations?
	 */
	public boolean hasAnnotations() {
		return false;
	}

	@Override
	public Iterator<KeyValue<String, String>> iterator() {
		// Nothing to iterate on
		return Collections.<KeyValue<String, String>> emptySet().iterator();
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getChromosomeName());
		sb.append("\t");
		sb.append(getStart());
		sb.append("-");
		sb.append(getEndClosed());
		sb.append(" ");
		sb.append(type);
		sb.append(((id != null) && (id.length() > 0) ? " '" + id + "'" : ""));

		if (hasAnnotations()) {
			for (KeyValue<String, String> kv : this)
				sb.append(kv.key + "=" + kv.value + ";");
		}

		return sb.toString();
	}

	@Override
	public boolean variantEffect(Variant variant, VariantEffects changeEffecs) {
		if (!intersects(variant)) return false; // Sanity check
		changeEffecs.add(variant, this, EffectType.CUSTOM, label);
		return true;
	}
}
