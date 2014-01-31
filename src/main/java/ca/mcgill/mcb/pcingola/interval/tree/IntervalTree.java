package ca.mcgill.mcb.pcingola.interval.tree;

import java.io.Serializable;
import java.util.Iterator;

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
public class IntervalTree implements Serializable, Iterable<Marker> {

	private static final long serialVersionUID = 1808077263026999072L;

	private IntervalNode head;
	private final Markers intervals;
	private boolean inSync;
	private int size;

	/**
	 * Instantiate a new interval tree with no intervals
	 */
	public IntervalTree() {
		head = new IntervalNode();
		intervals = new Markers();
		inSync = true;
		size = 0;
	}

	/**
	 * Instantiate and build an interval tree with a preset list of intervals
	 * @param intervals the list of intervals to use
	 */
	public IntervalTree(Markers intervals) {
		head = new IntervalNode(intervals);
		this.intervals = new Markers();
		this.intervals.add(intervals);
		inSync = true;
		size = intervals.size();
	}

	/**
	 * Add an interval object to the interval tree's list
	 * Will not rebuild the tree until the next query or call to build
	 * @param interval the interval object to add
	 */
	public void add(Marker interval) {
		intervals.add(interval);
		inSync = false;
	}

	/**
	 * Build the interval tree to reflect the list of intervals,
	 * Will not run if this is currently in sync
	 * 
	 * WARNING: This method is not thread safe 
	 * 
	 */
	public void build() {
		if (!inSync) {
			head = new IntervalNode(intervals);
			inSync = true;
			size = intervals.size();
		}
	}

	/**
	 * @return the number of entries in the currently built interval tree
	 */
	public int currentSize() {
		return size;
	}

	/**
	 * Determine whether this interval tree is currently a reflection of all intervals in the interval list
	 * @return true if no changes have been made since the last build
	 */
	public boolean inSync() {
		return inSync;
	}

	@Override
	public Iterator<Marker> iterator() {
		return head.iterator();
	}

	private String nodeString(IntervalNode node, int level) {
		if (node == null) return "";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++)
			sb.append("\t");
		sb.append(node + "\n");
		sb.append(nodeString(node.getLeft(), level + 1));
		sb.append(nodeString(node.getRight(), level + 1));
		return sb.toString();
	}

	/**
	 * Perform an interval query, returning the intervals that intersect with 'interval'
	 * Will rebuild the tree if out of sync
	 * 
	 * WARNING: This method is not thread safe if the interval tree is not fully built
	 * 
	 * @return All intervals that intersect 'interval'
	 */
	public Markers query(Interval interval) {
		build();
		return head.query(interval);
	}

	/**
	 * @return the number of entries in the interval list, equal to .size() if inSync()
	 */
	public int size() {
		return intervals.size();
	}

	/**
	 * Perform a stabbing query, returning the interval objects
	 * Will rebuild the tree if out of sync
	 * 
	 * WARNING: This method is not thread safe if the interval tree is not fully built
	 * 
	 * @param point the time to stab
	 * @return	   all intervals that contain time
	 */
	public Markers stab(int point) {
		build();
		return head.stab(point);
	}

	@Override
	public String toString() {
		return nodeString(head, 0);
	}
}
