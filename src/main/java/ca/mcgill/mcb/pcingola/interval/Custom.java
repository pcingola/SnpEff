package ca.mcgill.mcb.pcingola.interval;

import java.util.Collections;
import java.util.Iterator;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;
import ca.mcgill.mcb.pcingola.util.KeyValue;

/**
 * This is a custom interval (i.e. intervals provided by the user)
 * 
 * @author pcingola
 */
public class Custom extends Marker implements Iterable<KeyValue<String, String>> {

	private static final long serialVersionUID = -6843535415295857726L;

	String label;
	double score = Double.NaN;

	public Custom(Marker parent, int start, int end, int strand, String id, String label) {
		super(parent, start, end, strand, id);
		type = EffectType.CUSTOM;

		this.label = label;
		if (label == null || label.isEmpty()) label = id;
	}

	public String getLabel() {
		return label;
	}

	public double getScore() {
		return score;
	}

	/**
	 * Do we have additional annotations?
	 * @return
	 */
	public boolean hasAnnotations() {
		return false;
	}

	@Override
	public Iterator<KeyValue<String, String>> iterator() {
		// Nothing to iterate on
		return Collections.<KeyValue<String, String>> emptySet().iterator();
	}

	@Override
	public boolean seqChangeEffect(Variant seqChange, ChangeEffects changeEffecs) {
		if (!intersects(seqChange)) return false; // Sanity check
		changeEffecs.add(this, EffectType.CUSTOM, label);
		return true;
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
		sb.append(start);
		sb.append("-");
		sb.append(end);
		sb.append(" ");
		sb.append(type);
		sb.append(((id != null) && (id.length() > 0) ? " '" + id + "'" : ""));

		if (hasAnnotations()) {
			for (KeyValue<String, String> kv : this)
				sb.append(kv.key + "=" + kv.value + ";");
		}

		return sb.toString();
	}
}
