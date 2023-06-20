package org.snpeff.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.snpeff.util.GprHtml;

/**
 * Counters indexed by 'type' (type is a generic string that can mean anything)
 *
 * @author pcingola
 */
public class CountByType implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String TOTAL_TYPE = "Total";

	HashMap<String, Long> countByType;
	HashMap<String, Double> scoreByType;

	public CountByType() {
		countByType = new HashMap<String, Long>();
		scoreByType = new HashMap<String, Double>();
	}

	/**
	 * Add score for a type
	 */
	public long addScore(String type, double score) {
		Double currScore = scoreByType.get(type);
		if (currScore == null) currScore = 0.0;
		scoreByType.put(type, currScore + score);
		return inc(type);
	}

	public boolean contains(String key) {
		return countByType.containsKey(key) || scoreByType.containsKey(key);
	}

	/**
	 * How many counts of this type?
	 */
	public long get(String type) {
		return getCount(countByType, type);
	}

	/**
	 * Background color used for table (heatmap)
	 * @return An html coded color
	 */
	public String getColorHtml(String type) {
		if (countByType.get(type) == null) return "ffffff"; // Not found? => White

		long count = get(type);

		Long max = Long.MIN_VALUE, min = Long.MAX_VALUE;
		for (String key : countByType.keySet()) {
			long v = get(key);
			max = Math.max(max, v);
			min = Math.min(min, v);
		}

		return GprHtml.heatMapColor(count, max, min, 0xff0000, 0x00ff00);
	}

	long getCount(HashMap<String, Long> hash, String type) {
		// We have a special type called 'Total'
		if (type.equalsIgnoreCase(TOTAL_TYPE)) {
			long total = 0;
			for (Long count : hash.values())
				total += (count != null ? count : 0);
			return total;
		}

		// OK get change by effect
		Long count = hash.get(type);
		return count != null ? count : 0;
	}

	/**
	 * Count for this type
	 */
	public long getCount(String type) {
		Long count = countByType.get(type);
		return count != null ? count : 0;
	}

	/**
	 * Score for this type
	 */
	public double getScore(String type) {
		Double score = scoreByType.get(type);
		return score != null ? score : 0.0;
	}

	public long getTotalCount() {
		long total = 0;
		for (Long count : countByType.values())
			total += (count != null ? count : 0);
		return total;
	}

	/**
	 * List all types (alphabetically sorted)
	 * We need it as a getter for summary page (freemarker)
	 */
	public List<String> getTypeList() {
		return keysSorted();
	}

	public boolean hasCount(String type) {
		return countByType.containsKey(type);
	}

	public boolean hasData() {
		return !countByType.isEmpty();
	}

	public boolean hasScore(String type) {
		return scoreByType.containsKey(type);
	}

	/**
	 * Increment counter in a hash
	 */
	long inc(HashMap<String, Long> hash, String type, int toAdd) {
		Long count = hash.get(type);
		if (count == null) count = 0L;
		count += toAdd;
		hash.put(type, count);
		return count;
	}

	public long inc(String type) {
		return inc(countByType, type, 1);
	}

	/**
	 * Increment counter for a given type
	 */
	public long inc(String type, int increment) {
		return inc(countByType, type, increment);
	}

	/**
	 * Is this empty
	 */
	public boolean isEmpty() {
		return countByType.isEmpty() && scoreByType.isEmpty();
	}

	public Set<String> keySet() {
		return countByType.keySet();
	}

	/**
	 * List all types (sorted by count)
	 */
	public List<String> keysRanked(final boolean reverse) {
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(countByType.keySet());
		Collections.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return (int) (reverse ? get(arg1) - get(arg0) : get(arg1) - get(arg0));
			}
		});
		return keys;
	}

	/**
	 * List all types (alphabetically sorted)
	 */
	public List<String> keysSorted() {
		ArrayList<String> list = new ArrayList<String>();

		if (!countByType.keySet().isEmpty()) list.addAll(countByType.keySet());
		else list.addAll(scoreByType.keySet());

		Collections.sort(list);
		return list;
	}

	/**
	 * Maximum count
	 */
	public long max() {
		long max = Long.MIN_VALUE;
		for (Long count : countByType.values())
			max = Math.max(max, count);
		return max;
	}

	/**
	 * Minimum count
	 */
	public long min() {
		long min = Long.MAX_VALUE;
		for (Long count : countByType.values())
			min = Math.min(min, count);
		return min;
	}

	/**
	 * Percentage by type
	 */
	public double percent(String type) {
		long total = get(TOTAL_TYPE);
		long meth = get(type);
		return toProb(meth, total);
	}

	/**
	 * A map: key -> rank(counts)
	 */
	public Map<String, Integer> ranks(boolean reverse) {
		List<String> keys = keysRanked(reverse);
		HashMap<String, Integer> rank = new HashMap<>(keys.size());

		int rankNum = 0;
		for (String key : keys)
			rank.put(key, rankNum++);

		return rank;
	}

	/**
	 * Remove this entry type
	 */
	public void remove(String type) {
		countByType.remove(type);
		scoreByType.remove(type);
	}

	public void setScore(String type, double score) {
		scoreByType.put(type, score);
	}

	/**
	 * Sum all counts.
	 */
	public long sum() {
		return getTotalCount();
	}

	double toProb(long num, long total) {
		double p = 0;
		if (total > 0) p = ((double) num) / ((double) total);
		return p;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean showScores) {
		StringBuffer out = new StringBuffer();
		for (String type : keysSorted())
			out.append(toString(type) + "\n");

		return out.toString();
	}

	public String toString(String type) {
		return type + "\t" + get(type) + (hasScore(type) ? "\t" + getScore(type) + "\t" + (getScore(type) / getCount(type)) : "");
	}

	public String toStringLine() {
		StringBuffer out = new StringBuffer();
		for (String type : keysSorted())
			out.append(type + ":" + get(type) + "\t");

		return out.toString();
	}

	public String toStringSort() {
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(countByType.keySet());
		Collections.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return (int) (get(arg1) - get(arg0));
			}
		});

		StringBuffer out = new StringBuffer();
		for (String type : keys)
			out.append(toString(type) + "\n");

		return out.toString();
	}

	public String toStringTop(int n) {
		List<String> keys = keysRanked(true);

		StringBuffer out = new StringBuffer();
		int i = 0;
		for (String type : keys) {
			out.append(toString(type) + "\n");
			if (++i >= n) break;
		}
		out.append(TOTAL_TYPE + "\t" + get(TOTAL_TYPE) + "\n");

		return out.toString();
	}
}
