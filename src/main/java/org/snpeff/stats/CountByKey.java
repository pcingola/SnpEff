package org.snpeff.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Counters indexed by key.
 * It's a generic version of CountByType
 * 
 * @author pcingola
 */
public class CountByKey<T> implements Serializable {

	HashMap<T, Long> countByKey;

	public CountByKey() {
		countByKey = new HashMap<T, Long>();
	}

	/**
	 * How many counts of this type?
	 * @param key
	 * @return
	 */
	public long get(T key) {
		return getCount(countByKey, key);
	}

	long getCount(HashMap<T, Long> hash, T key) {
		Long count = hash.get(key);
		return count != null ? count : 0;
	}

	/**
	 * Increment counter in a hash
	 * @param hash
	 * @param key
	 */
	void inc(HashMap<T, Long> hash, T key, int toAdd) {
		Long count = hash.get(key);
		if (count == null) count = 0L;
		count += toAdd;
		hash.put(key, count);
	}

	/**
	 * Increment (by 1)
	 * @param key
	 */
	public void inc(T key) {
		inc(countByKey, key, 1);
	}

	/**
	 * Increment counter for a given type
	 * @param key
	 */
	public void inc(T key, int increment) {
		inc(countByKey, key, increment);
	}

	public boolean isEmpty() {
		return countByKey.isEmpty();
	}

	public Set<T> keySet() {
		return countByKey.keySet();
	}

	/**
	 * Maximum count
	 */
	public long max() {
		long max = Long.MIN_VALUE;
		for (Long count : countByKey.values())
			max = Math.max(max, count);
		return max;
	}

	/**
	 * Minimum count
	 */
	public long min() {
		long min = Long.MAX_VALUE;
		for (Long count : countByKey.values())
			min = Math.min(min, count);
		return min;
	}
	
	public 	int size() { return countByKey.size();}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		for (T type : keySet())
			out.append(type + "\t" + get(type) + "\n");

		return out.toString();
	}
}
