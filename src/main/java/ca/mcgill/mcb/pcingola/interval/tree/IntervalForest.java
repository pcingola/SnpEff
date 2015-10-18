package ca.mcgill.mcb.pcingola.interval.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;

/**
 * A set of interval trees (one per chromosome)
 *
 * @author pcingola
 */
public class IntervalForest implements Serializable, Iterable<Itree> {

	private static final long serialVersionUID = 1L;
	HashMap<String, Itree> forest;

	public IntervalForest() {
		forest = new HashMap<String, Itree>();
	}

	public IntervalForest(Markers markers) {
		forest = new HashMap<String, Itree>();
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
		getOrCreateTree(chName).add(interval); // Add interval to tree
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
		for (Itree tree : forest.values())
			tree.build();
	}

	/**
	 * Get (or create) an interval tree
	 */
	public Itree getOrCreateTree(String chromo) {
		chromo = Chromosome.simpleName(chromo);

		// Retrieve (or create) interval tree
		Itree itree = forest.get(chromo);
		if (itree == null) {
			itree = newItree();
			itree.build();
			forest.put(chromo, itree);
		}

		return itree;
	}

	/**
	 * Get an interval tree
	 */
	public Itree getTree(String chromo) {
		return forest.get(Chromosome.simpleName(chromo));
	}

	public Collection<String> getTreeNames() {
		return forest.keySet();
	}

	/**
	 * Is the tree 'chromo' available?
	 */
	public boolean hasTree(String chromo) {
		return getTree(chromo) != null;
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
					int end = Math.max(mq.getEnd(), mm.getEnd());
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

	/**
	 * Create new tree.
	 * In oder to change the implementation, only this method should be changed.
	 */
	protected Itree newItree() {
		return new IntervalTreeOri();
	}

	/**
	 * Query all intervals that intersect with 'interval'
	 */
	public Markers query(Marker marker) {
		return getOrCreateTree(marker.getChromosomeName()).query(marker);
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
		HashSet<Marker> uniqueMarkers = new HashSet<Marker>();

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
		return getOrCreateTree(chromo).stab(point);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		ArrayList<String> chrs = new ArrayList<>();
		chrs.addAll(forest.keySet());
		Collections.sort(chrs);

		for (String chromo : chrs) {
			Itree tree = getOrCreateTree(chromo);
			sb.append("chr" + chromo + "\tsize:" + tree.size() + "\tin_sync: " + tree.isInSync() + "\n");
		}

		return sb.toString();
	}
}
