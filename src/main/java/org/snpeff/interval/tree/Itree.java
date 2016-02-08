package org.snpeff.interval.tree;

import org.snpeff.interval.Genome;
import org.snpeff.interval.Interval;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;

/**
 * Interval tree interface
 */
public interface Itree extends Iterable<Marker> {

	/**
	 * Add an interval object to the interval tree's list
	 */
	public void add(Marker interval);

	/**
	 * Add all intervals to interval tree's list
	 */
	public void add(Markers markers);

	/**
	 * Build the interval tree to reflect the list of intervals.
	 * Must not run if this is currently in sync
	 */
	public void build();

	public Markers getIntervals();

	public boolean isEmpty();

	/**
	 * Is the tree 'in sync'?
	 * If false, the tree must be 'build()' before the next query
	 */
	public boolean isInSync();

	/**
	 * Load intervals from file
	 */
	public void load(String fileName, Genome genome);

	/**
	 * Perform an interval query, returning the intervals that
	 * intersect with 'interval'
	 *
	 * @return All intervals that intersect 'interval'
	 */
	public Markers query(Interval interval);

	/**
	 * Size: number of entries in this tree
	 */
	public int size();

	/**
	 * Perform a stabbing query, returning the interval objects
	 * @return All intervals intersecting 'point'
	 */
	public Markers stab(int point);

}
