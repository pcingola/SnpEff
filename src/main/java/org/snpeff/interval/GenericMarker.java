package org.snpeff.interval;

/**
 * An interval intended as a mark
 *
 * @author pcingola
 *
 */
public class GenericMarker extends Marker {

	private static final long serialVersionUID = -1645062778820144533L;
	String line;

	public GenericMarker() {
		super();
	}

	public GenericMarker(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
	}

	@Override
	public GenericMarker cloneShallow() {
		GenericMarker clone = (GenericMarker) super.cloneShallow();
		clone.line = line;
		return clone;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

}
