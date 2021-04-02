package org.snpeff.geneOntology;

import java.util.Comparator;
import java.util.Map;

/**
 * Compare two elements in a Map (e.g. HashMap) accessing them by keys.
 * Useful to sort by values elements in a Map
 *
 * Note: Decreasing order
 *
 * @author pcingola
 *
 */
@SuppressWarnings("rawtypes")
public class CompareByValue implements Comparator {

	Map map;

	public CompareByValue(Map map) {
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Object arg1, Object arg2) {
		if (!map.containsKey(arg1) || !map.containsKey(arg2)) { return 0; }

		Comparable value1 = (Comparable) map.get(arg1);
		Comparable value2 = (Comparable) map.get(arg2);
		return -value1.compareTo(value2); // Note: We use a minus because we want decreasing order (high values first)
	}
}
