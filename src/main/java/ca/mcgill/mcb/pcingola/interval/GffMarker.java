package ca.mcgill.mcb.pcingola.interval;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.util.KeyValue;

/**
 * An interval intended as a mark
 * 
 * @author pcingola
 *
 */
public class GffMarker extends Custom {

	private static final long serialVersionUID = -164502778854644537L;

	List<KeyValue<String, String>> keyValues;

	public GffMarker(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id, "");
	}

	/**
	 * Add key value pair
	 * @param keyValue
	 */
	public void add(KeyValue<String, String> keyValue) {
		if (keyValues == null) keyValues = new LinkedList<KeyValue<String, String>>();
		keyValues.add(keyValue);
	}

	/**
	 * Add key value pair
	 * @param keyValue
	 */
	public void add(String key, String value) {
		KeyValue<String, String> kv = new KeyValue<String, String>(key, value);
		add(kv);
	}

	/**
	 * Do we have additional annotations?
	 * @return
	 */
	@Override
	public boolean hasAnnotations() {
		return (keyValues != null) && (!keyValues.isEmpty());
	}

	@Override
	public Iterator<KeyValue<String, String>> iterator() {
		// Nothing to iterate on
		if (keyValues == null) return Collections.<KeyValue<String, String>> emptySet().iterator();
		return keyValues.iterator();
	}

}
