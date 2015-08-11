package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Regulatory elements
 *
 * @author pablocingolani
 */
public class Regulation extends Marker {

	private static final long serialVersionUID = -5607588295343642199L;

	String cellType = "";
	String name = "";

	public Regulation() {
		super();
		type = EffectType.REGULATION;
	}

	public Regulation(Marker parent, int start, int end, boolean strandMinus, String id, String name, String cellType) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.REGULATION;
		this.name = name;
		this.cellType = cellType;
	}

	@Override
	public Regulation cloneShallow() {
		Regulation clone = (Regulation) super.cloneShallow();
		clone.cellType = cellType;
		clone.name = name;
		return clone;
	}

	public String getCellType() {
		return cellType;
	}

	public String getName() {
		return name;
	}

	/**
	 * Parse a line from a serialized file
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		cellType = markerSerializer.getNextField();
		name = markerSerializer.getNextField();
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + cellType //
				+ "\t" + name //
				;
	}

	@Override
	public String toString() {
		return getChromosomeName() + "\t" + start + "-" + end //
				+ " " //
				+ type + ((name != null) && (!name.isEmpty()) ? " '" + name + "'" : "");
	}

	/**
	 * Calculate the effect of this seqChange
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false; // Sanity check
		EffectType effType = EffectType.REGULATION;
		variantEffects.add(variant, this, effType, "");
		return true;
	}

}
