package org.snpeff.interval.tree;

import java.util.Arrays;
import java.util.Iterator;

import org.snpeff.interval.Genome;
import org.snpeff.interval.Interval;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.util.Log;

/**
 * Interval tree structure using arrays
 * This is slightly faster than the new IntervalTree implementation
 *
 * @author pcingola
 */
public class IntervalTreeArray implements Itree {

	public static final int MIN_MARKERS_PER_LEAF = 4; // This number cannot be less then 3 (see comment in code below)
	public static final int INITIAL_CAPACITY = 1024; // Initial capacity for arrays
	protected static final Marker[] EMPTY_MARKER_ARRAY = new Marker[0];

	protected boolean debug;
	protected boolean verbose;
	protected Markers markers; // Markers to add
	protected boolean inSync; // Do we nend to build the tree?
	protected int left[]; // Left subtree (index within this IntervalTreeFileChromo)
	protected int right[]; // Right subtree (index within this IntervalTreeFileChromo)
	protected int mid[]; // Middle position (genomic coordinate)
	protected Marker intersectMarkers[][]; // Markers intersecting this node's center
	protected int lastIdx; // Arrays size (index of first unused element in the arrays)

	public IntervalTreeArray() {
		this(null);
	}

	/**
	 * Instantiate an interval tree with a list of intervals
	 */
	public IntervalTreeArray(Markers markers) {
		this.markers = new Markers();
		if (markers != null) this.markers.add(markers);

		inSync = false;
		reset();
	}

	@Override
	public void add(Marker interval) {
		markers.add(interval);
		inSync = false;
	}

	@Override
	public void add(Markers markers) {
		markers.add(markers);
		inSync = false;
	}

	@Override
	public void build() {
		markers.sort();
		reset();
		build(markers);
	}

	/**
	 * Index intervals from 'start' to 'end' (index in 'markers')
	 * @return Index of added item (-1 if no item was added)
	 */
	protected int build(Markers markers) {
		if (markers.isEmpty()) return -1;

		int idx = nextEntry();

		// Calculate median point
		int center = markers.getMedian();

		// Split markers to the left, to the right and intersecting 'center'
		Markers left = new Markers();
		Markers right = new Markers();
		Markers intersecting = new Markers();

		for (Marker interval : markers) {
			if (interval.getEndClosed() < center) left.add(interval);
			else if (interval.getStart() > center) right.add(interval);
			else intersecting.add(interval);
		}

		// Convert markers to array
		Marker intMarkers[] = null;
		if (!intersecting.isEmpty()) intMarkers = intersecting.toArray();

		// Recurse
		int leftIdx = build(left);
		int rightIdx = build(right);
		set(idx, leftIdx, rightIdx, center, intMarkers);

		return idx;
	}

	int capacity() {
		if (left == null) return 0;
		return left.length;
	}

	@Override
	public Markers getIntervals() {
		return markers;
	}

	void grow() {
		int oldCapacity = capacity();
		int newCapacity = oldCapacity + (oldCapacity >> 1);

		left = Arrays.copyOf(left, newCapacity);
		right = Arrays.copyOf(right, newCapacity);
		mid = Arrays.copyOf(mid, newCapacity);
		intersectMarkers = Arrays.copyOf(intersectMarkers, newCapacity);
	}

	@Override
	public boolean isEmpty() {
		return markers.isEmpty();
	}

	@Override
	public boolean isInSync() {
		return inSync;
	}

	/**
	 * Is node 'idx' a leaf node?
	 */
	boolean isLeaf(int idx) {
		return (left[idx] == -1) && (right[idx] == -1);
	}

	@Override
	public Iterator<Marker> iterator() {
		return markers.iterator();
	}

	@Override
	public void load(String fileName, Genome genome) {
		throw new RuntimeException("Unimplemented!");
	}

	/**
	 * Get next index for entry and make sure there
	 * is enough capacity to store it
	 */
	int nextEntry() {
		if (lastIdx >= capacity()) grow();
		return lastIdx++;
	}

	/**
	 * Query index to find all VCF entries intersecting 'marker'
	 * Store VCF entries in 'results'
	 */
	@Override
	public Markers query(Interval marker) {
		Markers results = new Markers();
		query(marker, 0, results);
		return results;
	}

	/**
	 * Query index to find all VCF entries intersecting 'marker', starting from node 'idx'
	 * Store VCF entries in 'results'
	 */
	protected void query(Interval marker, int idx, Markers results) {
		if (debug) Log.debug("query( " + marker + ", " + idx + " )\t" + toString(idx));

		// Negative index? Nothing to do
		if (idx < 0) return;

		// Check all intervals intersecting
		queryIntersects(marker, idx, results);

		// Recurse left or right
		int midPos = mid[idx];
		if (debug) Log.debug("midPos:" + midPos);

		// Recurse left?
		if ((marker.getStart() < midPos) && (left[idx] >= 0)) {
			query(marker, left[idx], results);
		}

		// Recurse right?
		if ((midPos < marker.getEndClosed()) && (right[idx] >= 0)) {
			query(marker, right[idx], results);
		}
	}

	/**
	 * Query entries intersecting 'marker' at node 'idx'
	 */
	protected void queryIntersects(Interval marker, int idx, Markers results) {
		if (debug) Log.debug("queryIntersects( " + marker + ", " + idx + " )");

		// Null? Nothing ti check
		if (intersectMarkers[idx] == null) return;

		// Check all markers in the intersect
		Marker markers[] = intersectMarkers[idx];
		for (Marker m : markers) {
			if (m.intersects(marker)) {
				results.add(m);
				if (debug) Log.debug("\tMatches entry: " + m);
			}
		}
	}

	protected void reset() {
		left = new int[INITIAL_CAPACITY];
		right = new int[INITIAL_CAPACITY];
		mid = new int[INITIAL_CAPACITY];
		intersectMarkers = new Marker[INITIAL_CAPACITY][];
		lastIdx = 0;

		for (int i = 0; i < left.length; i++)
			left[i] = right[i] = -1;
	}

	/**
	 * Set all parameters in one 'row'
	 *
	 * WARNING: If we don't do it this way, we get strange errors
	 * due to array resizing (array appears to be filled with
	 * zeros after being set)
	 */
	void set(int idx, int leftIdx, int rightIdx, int midPos, Marker intMarkers[]) {
		left[idx] = leftIdx;
		right[idx] = rightIdx;
		mid[idx] = midPos;
		intersectMarkers[idx] = intMarkers;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public int size() {
		return markers.size();
	}

	@Override
	public Markers stab(int point) {
		Markers results = new Markers();
		stab(point, 0, results);
		return results;
	}

	protected void stab(int point, int idx, Markers results) {
		if (debug) Log.debug("stab( " + point + ", " + idx + " )\t" + toString(idx));

		// Negative index? Nothing to do
		if (idx < 0) return;

		// Check all intervals intersecting
		stabIntersects(point, idx, results);

		// Recurse left or right
		int midPos = mid[idx];
		if (debug) Log.debug("midPos:" + midPos);
		if ((point < midPos) && (left[idx] >= 0)) {
			stab(point, left[idx], results);
		}

		if ((midPos < point) && (right[idx] >= 0)) {
			stab(point, right[idx], results);
		}
	}

	/**
	 * Stab entries intersecting 'point' at node 'idx'
	 */
	protected void stabIntersects(int point, int idx, Markers results) {
		if (debug) Log.debug("stabIntersects( " + point + ", " + idx + " )");

		// Null? Nothing to check
		if (intersectMarkers[idx] == null) return;

		// Check all markers in the intersect
		Marker markers[] = intersectMarkers[idx];
		for (Marker m : markers) {
			if (m.intersects(point)) {
				results.add(m);
				if (debug) Log.debug("\tMatches entry: " + m);
			}
		}
	}

	@Override
	public String toString() {
		return "Size: " + lastIdx //
				+ ", capacity: " + capacity() //
		;
	}

	public String toString(int idx) {
		if (idx < 0) return "None";

		StringBuilder sb = new StringBuilder();
		sb.append(idx //
				+ "\tleftIdx: " + left[idx] //
				+ "\trightIdx: " + right[idx] //
				+ "\tmidPos: " + mid[idx] //
		);

		if (intersectMarkers[idx] != null) {
			sb.append("\tintersect: (" + intersectMarkers[idx].length + ")\n");
			for (int i = 0; i < intersectMarkers[idx].length; i++)
				sb.append("\t\t" + i + ":\t" + intersectMarkers[idx][i] + "\n");
		}

		return sb.toString();
	}

	public String toStringAll() {
		StringBuilder sb = new StringBuilder();
		sb.append(toString() + "\n");

		for (int i = 0; i < lastIdx; i++)
			sb.append("\t" + toString(i) + "\n");

		return sb.toString();
	}

}
