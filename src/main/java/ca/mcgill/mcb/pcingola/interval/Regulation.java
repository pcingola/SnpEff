package ca.mcgill.mcb.pcingola.interval;

import java.util.List;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

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

	public Regulation(Marker parent, int start, int end, int strand, String id, String name, String cellType) {
		super(parent, start, end, strand, id);
		type = EffectType.REGULATION;
		this.name = name;
		this.cellType = cellType;
	}

	public String getCellType() {
		return cellType;
	}

	public String getName() {
		return name;
	}

	/**
	 * Calculate the effect of this seqChange
	 * @param seqChange
	 * @param changeEffect
	 * @return
	 */
	@Override
	public List<ChangeEffect> seqChangeEffect(SeqChange seqChange, ChangeEffect changeEffect) {
		if (!intersects(seqChange)) return ChangeEffect.emptyResults(); // Sanity check
		EffectType effType = EffectType.REGULATION;
		changeEffect.set(this, effType, "");
		return changeEffect.newList();
	}

	/**
	 * Parse a line from a serialized file
	 * @param line
	 * @return
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

}
