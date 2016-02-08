package org.snpeff.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A Hash that can hold multiple values for each key
 *
 * @author pcingola
 */
public class MultivalueHashMap<K, V> extends HashMap<K, LinkedList<V>> {

	private static final long serialVersionUID = -8860279543165990227L;

	public MultivalueHashMap() {
		super();
	}

	/**
	 * Add multiple values
	 */
	public void add(K key, Collection<V> values) {
		getOrCreate(key).addAll(values); // Add all to the list
	}

	/**
	 * Add a single value
	 */
	public void add(K key, V value) {
		getOrCreate(key).add(value); // Add to the list
	}

	/**
	 * Get a list of values (or create it if not available)
	 */
	public List<V> getOrCreate(K key) {
		// Get list
		LinkedList<V> list = get(key);
		if (list == null) { // No list? Create one
			list = new LinkedList<V>();
			put(key, list);
		}
		return list;
	}
}
