package ca.mcgill.mcb.pcingola.interval;

/**
 * An interval intended as a mark
 * 
 * @author pcingola
 *
 */
public class GenericMarker extends Marker {

	private static final long serialVersionUID = -1645062778820144533L;
	String line;

	public GenericMarker(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

}
