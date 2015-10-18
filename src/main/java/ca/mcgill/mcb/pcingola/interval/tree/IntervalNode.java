package ca.mcgill.mcb.pcingola.interval.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ca.mcgill.mcb.pcingola.interval.Interval;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;

/**
 * The Node class contains the interval tree information for one single node
 */
public class IntervalNode implements Serializable, Iterable<Marker> {

	private static final long serialVersionUID = -444656480302906048L;
	private final SortedMap<Marker, List<Marker>> intervals;
	private Integer center;
	private IntervalNode leftNode;
	private IntervalNode rightNode;

	public IntervalNode() {
		intervals = new TreeMap<Marker, List<Marker>>();
		center = 0;
		leftNode = null;
		rightNode = null;
	}

	public IntervalNode(Markers markers) {
		intervals = new TreeMap<Marker, List<Marker>>();

		SortedSet<Integer> endpoints = new TreeSet<Integer>();

		for (Interval interval : markers) {
			endpoints.add(interval.getStart());
			endpoints.add(interval.getEnd());
		}

		if (endpoints.isEmpty()) {
			center = 0;
			return;
		}

		int median = getMedian(endpoints);
		center = median;

		Markers left = new Markers();
		Markers right = new Markers();

		for (Marker interval : markers) {
			if (interval.getEnd() < median) left.add(interval);
			else if (interval.getStart() > median) right.add(interval);
			else {
				List<Marker> posting = intervals.get(interval);
				if (posting == null) {
					posting = new ArrayList<Marker>();
					intervals.put(interval, posting);
				}
				posting.add(interval);
			}
		}

		if (left.size() > 0) leftNode = new IntervalNode(left);
		if (right.size() > 0) rightNode = new IntervalNode(right);
	}

	/**
	 * Add all intervals to the 'allIntervals' list
	 * @param allIntervals
	 */
	void addAllIntervals(List<Marker> allIntervals) {
		for (List<Marker> list : intervals.values())
			allIntervals.addAll(list);

		if (leftNode != null) leftNode.addAllIntervals(allIntervals);
		if (rightNode != null) rightNode.addAllIntervals(allIntervals);
	}

	public Integer getCenter() {
		return center;
	}

	public IntervalNode getLeft() {
		return leftNode;
	}

	/**
	 * @param set the set to look on
	 * @return	  the median of the set, not interpolated
	 */
	private Integer getMedian(SortedSet<Integer> set) {
		int i = 0;
		int middle = set.size() / 2;
		for (Integer point : set) {
			if (i == middle) return point;
			i++;
		}
		return null;
	}

	public IntervalNode getRight() {
		return rightNode;
	}

	@Override
	public Iterator<Marker> iterator() {
		ArrayList<Marker> allIntervals = new ArrayList<Marker>();
		addAllIntervals(allIntervals);
		return allIntervals.iterator();
	}

	/**
	 * Perform an interval intersection query on the node
	 * @param target: the interval to intersect
	 * @return all intervals containing 'target'
	 */
	public Markers query(Interval target) {
		Markers result = new Markers();

		for (Entry<Marker, List<Marker>> entry : intervals.entrySet()) {
			if (entry.getKey().intersects(target)) {
				for (Marker interval : entry.getValue())
					result.add(interval);
			} else if (entry.getKey().getStart() > target.getEnd()) break;
		}

		if (target.getStart() < center && leftNode != null) result.add(leftNode.query(target));
		if (target.getEnd() > center && rightNode != null) result.add(rightNode.query(target));
		return result;
	}

	/**
	 * Perform a stabbing query on the node
	 * @param point the time to query at
	 * @return	   all intervals containing time
	 */
	public Markers stab(Integer point) {
		Markers result = new Markers();

		for (Entry<Marker, List<Marker>> entry : intervals.entrySet()) {
			if (entry.getKey().intersects(point)) { //
				for (Marker interval : entry.getValue())
					result.add(interval);
			} else if (entry.getKey().getStart() > point) break;
		}

		if (point < center && leftNode != null) result.add(leftNode.stab(point));
		else if (point > center && rightNode != null) result.add(rightNode.stab(point));
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(center + ": ");
		for (Entry<Marker, List<Marker>> entry : intervals.entrySet()) {
			sb.append("[" + entry.getKey().getStart() + "," + entry.getKey().getEnd() + "]:{");
			for (Interval interval : entry.getValue()) {
				sb.append("(" + interval + ")");
			}
			sb.append("} ");
		}
		return sb.toString();
	}
}
