package org.snpeff.geneSets;

import java.util.Comparator;
import java.util.Map;

/**
 * Compare two elements in a Map (e.g. HashMap) accessing them by keys.
 * Useful to sort by values elements in a Map
 * 
 * Note: Sorts in descending order
 * @author Pablo Cingolani
 *
 */
@SuppressWarnings("rawtypes")
public class CompareByValue implements Comparator {

	Map map;
	int order = -1;

	public CompareByValue(Map map, boolean orderAscending) {
		this.map = map;
		order = (orderAscending ? 1 : -1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Object arg1, Object arg2) {
		if( !map.containsKey(arg1) || !map.containsKey(arg2) ) { return 0; }

		Comparable value1 = (Comparable) map.get(arg1);
		Comparable value2 = (Comparable) map.get(arg2);
		return order * value1.compareTo(value2);
	}
}
