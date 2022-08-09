package org.snpeff.interval.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.util.Log;

/**
 * A set of interval trees (e.g. one per chromosome, one per transcript ID, etc)
 *
 * @author pcingola
 */
public class IntervalForest implements Serializable, Iterable<Itree> {

	private static final long serialVersionUID = 1L;

	boolean debug;
	String name;
	HashMap<String, Itree> forest;

	public IntervalForest() {
		forest = new HashMap<>();
	}

	public IntervalForest(Markers markers) {
		forest = new HashMap<>();
		add(markers);
	}

	/**
	 * Add all intervals
	 */
	public void add(Collection<? extends Marker> intervals) {
		for (Marker i : intervals)
			add(i);
	}

	/**
	 * Add an interval
	 */
	public void add(Marker interval) {
		if (interval == null) return;
		String chName = Chromosome.simpleName(interval.getChromosomeName());
		getOrCreateTreeChromo(chName).add(interval); // Add interval to tree
	}

	/**
	 * Add all intervals
	 */
	public void add(Markers intervals) {
		for (Marker i : intervals)
			add(i);
	}

	/**
	 * Build all trees
	 */
	public void build() {
		for (String key : forest.keySet()) {
			if (debug) Log.debug("Building interval tree for '" + key + "'");
			Itree tree = forest.get(key);
			tree.build();
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Get (or create) an interval tree for ID
	 */
	public Itree getOrCreateTree(String id) {
		// Retrieve (or create) interval tree
		Itree itree = forest.get(id);
		if (itree == null) {
			itree = newItree();
			itree.build();
			forest.put(id, itree);
		}

		return itree;
	}

	/**
	 * Get (or create) an interval tree based for "chromo" (chromosome name)
	 */
	public Itree getOrCreateTreeChromo(String chromo) {
		return getOrCreateTree(Chromosome.simpleName(chromo));
	}

	/**
	 * Get an interval tree using an ID
	 */
	public Itree getTree(String key) {
		return forest.get(key);
	}

	/**
	 * Get an interval tree using a chromosome name
	 */
	public Itree getTreeChromo(String chromo) {
		return forest.get(Chromosome.simpleName(chromo));
	}

	/**
	 * Is the tree 'chromo' available?
	 */
	public boolean hasTree(String chromo) {
		return getTreeChromo(chromo) != null;
	}

	/**
	 * Return the intersection of 'markers' and this IntervalForest
	 *
	 * For each marker 'm' in 'markers'
	 * 		- query the tree to get all markers intersecting 'm'
	 * 		- create a new interval which is the intersection of 'm' with all the resutls from the previous query.
	 */
	public Markers intersect(Markers markers) {
		Markers result = new Markers();

		// Add all intersecting intervals
		for (Marker mm : markers) {
			Markers query = query(mm);
			if (query != null) {
				for (Marker mq : query) {
					// Intersection between 'mm' and 'mq'
					int start = Math.max(mq.getStart(), mm.getStart());
					int end = Math.max(mq.getEndClosed(), mm.getEndClosed());
					Marker mintq = new Marker(mq.getParent(), start, end, mq.isStrandMinus(), "");

					// Add intersection result
					result.add(mintq);
				}
			}
		}

		return result;
	}

	@Override
	public Iterator<Itree> iterator() {
		return forest.values().iterator();
	}

	public Collection<String> keySet() {
		return forest.keySet();
	}

	/**
	 * Create new tree.
	 * In oder to change the implementation, only this method should be changed.
	 */
	protected Itree newItree() {
		return new IntervalTree();
	}

	/**
	 * Query all intervals that intersect with 'interval'
	 */
	public Markers query(Marker marker) {
		return getOrCreateTreeChromo(marker.getChromosomeName()).query(marker);
	}

	/**
	 * Query all intervals that intersect with any interval in 'intervals'
	 */
	public Markers query(Markers marker) {
		Markers ints = new Markers();

		// Add all intersecting intervals
		for (Marker i : marker)
			ints.add(query(i));

		return ints;
	}

	/**
	 * Query unique intervals that intersect with any interval in 'markers'
	 * I.e.: Return a set of intervals that intersects (at least once) with any interval in 'markers'
	 */
	public Markers queryUnique(Markers markers) {
		HashSet<Marker> uniqueMarkers = new HashSet<>();

		// Add all intersecting intervals
		for (Marker q : markers) {
			Markers results = query(q); // Query

			for (Marker r : results)
				// Add all results
				uniqueMarkers.add(r);
		}

		// Create markers
		Markers ints = new Markers();
		for (Marker r : uniqueMarkers)
			ints.add(r);

		return ints;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int size() {
		int size = 0;
		for (Itree it : forest.values())
			size += it.size();
		return size;
	}

	/**
	 * Obtain all intervals that intersect with 'marker.start'
	 */
	public Markers stab(Marker marker) {
		return stab(marker.getChromosomeName(), marker.getStart());
	}

	/**
	 * Obtain all intervals that intersect with 'point'
	 */
	public Markers stab(String chromo, int point) {
		return getOrCreateTreeChromo(chromo).stab(point);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(forest.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			Itree tree = getOrCreateTreeChromo(key);
			sb.append(key + "\tsize:" + tree.size() + "\tin_sync: " + tree.isInSync() + "\n");
		}

		return sb.toString();
	}

}
