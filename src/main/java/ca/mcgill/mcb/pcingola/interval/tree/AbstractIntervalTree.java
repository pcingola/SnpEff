package ca.mcgill.mcb.pcingola.interval.tree;

import ca.mcgill.mcb.pcingola.interval.Interval;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;

/**
 * An Interval Tree is essentially a map from intervals to objects, which
 * can be queried for all data associated with a particular interval of
 * point
 *
 * Adapted from Kevin Dolan's implementation
 */
public interface AbstractIntervalTree {

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
