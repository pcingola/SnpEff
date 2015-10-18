package ca.mcgill.mcb.pcingola.interval.tree;

import java.io.Serializable;
import java.util.Arrays;

import ca.mcgill.mcb.pcingola.interval.Interval;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;

/**
 * The Node class contains the interval tree information for one single node
 */
public class IntervalNode2 implements Serializable {

	private static final long serialVersionUID = -444656480302906048L;

	public static boolean USE_NEW = true;

	Marker[] intervals;
	private Integer center;
	private IntervalNode2 leftNode;
	private IntervalNode2 rightNode;

	public IntervalNode2() {
		center = 0;
		leftNode = null;
		rightNode = null;
	}

	public IntervalNode2(Markers markers) {
		// Empty markers?
		if (markers.size() == 0) {
			center = 0;
			return;
		}

		// Calculate median point
		center = getMedian(markers);

		// Split markers to the left, to the right and intersecting 'center'
		Markers left = new Markers();
		Markers right = new Markers();
		Markers intersecting = new Markers();

		for (Marker interval : markers) {
			if (interval.getEnd() < center) left.add(interval);
			else if (interval.getStart() > center) right.add(interval);
			else intersecting.add(interval);
		}

		// Convert markers to array
		if (intersecting.isEmpty()) intervals = null;
		else intervals = intersecting.toArray();

		// Recurse
		if (left.size() > 0) leftNode = new IntervalNode2(left);
		if (right.size() > 0) rightNode = new IntervalNode2(right);
	}

	public Integer getCenter() {
		return center;
	}

	public IntervalNode2 getLeft() {
		return leftNode;
	}

	/**
	 * Calculate the median point in this set of markers
	 */
	protected int getMedian(Markers markers) {
		// Add all start & end coordinates
		int i = 0;
		int endpoints[] = new int[2 * markers.size()];
		for (Interval interval : markers) {
			endpoints[i++] = interval.getStart();
			endpoints[i++] = interval.getEnd();
		}

		// Calculate median by sorting and selecting middle element
		Arrays.sort(endpoints);
		int middle = endpoints.length / 2;
		return endpoints[middle];
	}

	public IntervalNode2 getRight() {
		return rightNode;
	}

	/**
	 * Perform an interval intersection query on the node
	 * @param queryMarker: The interval to intersect
	 * @return All intervals containing 'target'
	 */
	public Markers query(Interval queryInterval) {
		Markers results = new Markers();

		if (intervals != null) {
			for (Marker marker : intervals)
				if (marker.intersects(queryInterval)) results.add(marker);
		}

		if (queryInterval.getStart() < center && leftNode != null) results.add(leftNode.query(queryInterval));
		if (queryInterval.getEnd() > center && rightNode != null) results.add(rightNode.query(queryInterval));

		return results;
	}

	/**
	 * Perform a stabbing query on the node
	 * @param point the time to query at
	 * @return All intervals containing time
	 */
	public Markers stab(Integer point) {
		Markers result = new Markers();

		for (Marker marker : intervals)
			if (marker.intersects(point)) result.add(marker);

		if (point < center && leftNode != null) result.add(leftNode.stab(point));
		else if (point > center && rightNode != null) result.add(rightNode.stab(point));
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(center + ":\n");

		for (Marker marker : intervals)
			sb.append("\t" + marker + "\n");

		return sb.toString();
	}
}
