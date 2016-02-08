package org.snpeff.interval.tree;

import java.io.Serializable;
import java.util.Iterator;

import org.snpeff.interval.Genome;
import org.snpeff.interval.Interval;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;

/**
 * An Interval Tree is essentially a map from intervals to objects, which
 * can be queried for all data associated with a particular interval of
 * point
 */
public class IntervalTree implements Itree, Serializable {

	private static final long serialVersionUID = 1808077263026999072L;

	protected IntervalNode head;
	protected Markers intervals;
	protected boolean inSync;

	/**
	 * Instantiate a new interval tree with no intervals
	 */
	public IntervalTree() {
		head = new IntervalNode();
		intervals = new Markers();
		inSync = true;
	}

	/**
	 * Instantiate an interval tree with a list of intervals
	 */
	public IntervalTree(Markers intervals) {
		head = new IntervalNode(intervals);
		this.intervals = new Markers();
		this.intervals.add(intervals);
		inSync = false;
	}

	/**
	 * Add an interval object to the interval tree's list
	 *
	 * Note: Marks the tree as 'not inSync', but will not rebuild
	 * the tree until the next query or call to build
	 *
	 * @param interval the interval object to add
	 */
	@Override
	public void add(Marker interval) {
		intervals.add(interval);
		inSync = false;
	}

	/**
	 * Add all intervals to interval tree's list
	 * Note: Marks the tree as 'not inSync', but will not rebuild
	 * the tree until the next query or call to build
	 */
	@Override
	public void add(Markers markers) {
		intervals.add(markers);
		inSync = false;
	}

	/**
	 * Build the interval tree to reflect the list of intervals,
	 * Will not run if this is currently in sync
	 */
	@Override
	public void build() {
		if (!inSync) {
			head = new IntervalNode(intervals);
			inSync = true;
		}
	}

	@Override
	public Markers getIntervals() {
		return intervals;
	}

	@Override
	public boolean isEmpty() {
		return intervals.isEmpty();
	}

	/**
	 * Determine whether this interval tree is currently a reflection of all intervals in the interval list
	 * @return true if no changes have been made since the last build
	 */
	@Override
	public boolean isInSync() {
		return inSync;
	}

	@Override
	public Iterator<Marker> iterator() {
		return intervals.iterator();
	}

	@Override
	public void load(String fileName, Genome genome) {
		intervals.load(fileName, genome);
		inSync = false;
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
	 * @return All intervals that intersect 'interval'
	 */
	@Override
	public Markers query(Interval interval) {
		if (!inSync) throw new RuntimeException("Interval tree needs to be updated!");
		return head.query(interval);
	}

	/**
	 * Size: number of entries in the interval list
	 */
	@Override
	public int size() {
		return intervals.size();
	}

	/**
	 * Perform a stabbing query, returning the interval objects
	 * @return All intervals intersecting 'point'
	 */
	@Override
	public Markers stab(int point) {
		if (!inSync) throw new RuntimeException("Interval tree needs to be updated!");
		return head.stab(point);
	}

	@Override
	public String toString() {
		return nodeString(head, 0);
	}
}
