package ca.mcgill.mcb.pcingola.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;

/**
 * Interval that contains sub intervals.
 *
 * @author pcingola
 *
 */
public class IntervalAndSubIntervals<T extends Marker> extends Marker implements Iterable<T> {

	private static final long serialVersionUID = 1636197649250882952L;
	HashMap<String, T> subIntervals;
	ArrayList<T> sorted;
	ArrayList<T> sortedStrand;

	public IntervalAndSubIntervals() {
		super();
		reset();
	}

	public IntervalAndSubIntervals(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		reset();
	}

	/**
	 * Add a subinterval
	 */
	public void add(T t) {
		if (subIntervals.put(t.getId(), t) != null) throw new RuntimeException(t.getClass().getSimpleName() + " '" + t.getId() + "' is already in " + this.getClass().getSimpleName() + " '" + id + "'");
		invalidateSorted();
	}

	/**
	 * Add all intervals
	 */
	public void addAll(Iterable<T> ts) {
		for (T t : ts)
			add(t);
		invalidateSorted();
	}

	/**
	 * Add all markers
	 * @param ts
	 */
	@SuppressWarnings("unchecked")
	public void addAll(Markers markers) {
		for (Marker m : markers)
			add((T) m);
		invalidateSorted();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IntervalAndSubIntervals<T> clone() {
		IntervalAndSubIntervals<T> copy = (IntervalAndSubIntervals<T>) super.clone();
		copy.reset();

		for (T m : this) {
			T mcopy = (T) m.clone();
			mcopy.setParent(copy);
			copy.add(mcopy);
		}

		return copy;
	}

	/**
	 * Is 'id' in the subintervals?
	 */
	public boolean containsId(String id) {
		return subIntervals.containsKey(id);
	}

	/**
	 * Obtain a subinterval
	 */
	public T get(String id) {
		return subIntervals.get(id);
	}

	/**
	 * Invalidate sorted collections
	 */
	protected void invalidateSorted() {
		sorted = sortedStrand = null;
	}

	@Override
	public Iterator<T> iterator() {
		return subIntervals.values().iterator();
	}

	/**
	 * A list of all markers in this transcript
	 */
	public Markers markers() {
		Markers markers = new Markers();
		markers.addAll(subIntervals.values());
		return markers;
	}

	public int numChilds() {
		return (subIntervals != null ? subIntervals.size() : 0);
	}

	/**
	 * Query all genomic regions that intersect 'marker'
	 */
	@Override
	public Markers query(Marker marker) {
		Markers markers = new Markers();

		for (Marker m : this) {
			if (m.intersects(marker)) {
				markers.add(m);

				Markers subMarkers = m.query(marker);
				if (subMarkers != null) markers.add(subMarkers);
			}
		}

		return markers;
	}

	/**
	 * Remove a subinterval
	 */
	public void remove(T t) {
		subIntervals.remove(t.getId());
		invalidateSorted();
	}

	/**
	 * Remove all intervals
	 */
	public void reset() {
		subIntervals = new HashMap<String, T>();
		invalidateSorted();
	}

	/**
	 * Parse a line from a serialized file
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);

		Markers markers = markerSerializer.getNextFieldMarkers();
		for (Marker m : markers)
			add((T) m);
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + markerSerializer.save((Collection<Marker>) subIntervals.values()) //
				;
	}

	@Override
	public void setStrandMinus(boolean strandMinus) {
		this.strandMinus = strandMinus;

		// Change all subintervals
		for (T t : this)
			t.setStrandMinus(strandMinus);

		invalidateSorted(); // These are no longer correct
	}

	@Override
	public void shiftCoordinates(int shift) {
		super.shiftCoordinates(shift);

		for (T m : subIntervals.values())
			m.shiftCoordinates(shift);
	}

	/**
	 * Return a collection of sub intervals sorted by natural order
	 */
	public synchronized List<T> sorted() {
		if (sorted != null) return sorted;
		sorted = new ArrayList<T>();
		sorted.addAll(subIntervals.values());
		Collections.sort(sorted);
		return sorted;
	}

	/**
	 * Return a collection of sub intervals sorted by start position (if strand is >= 0) or
	 * by reverse end position (if strans < 0)
	 */
	public synchronized List<T> sortedStrand() {
		if (sortedStrand != null) return sortedStrand;

		sortedStrand = new ArrayList<T>();
		sortedStrand.addAll(subIntervals.values());

		if (isStrandPlus()) Collections.sort(sortedStrand, new IntervalComparatorByStart()); // Sort by start position
		else Collections.sort(sortedStrand, new IntervalComparatorByEnd(true)); // Sort by end position (reversed)

		return sortedStrand;
	}

	/**
	 * Return a collection of sub intervals
	 */
	public Collection<T> subintervals() {
		return subIntervals.values();
	}

}
