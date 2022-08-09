package org.snpeff.interval;

import java.util.Comparator;

/**
 * Compare intervals by end position
 * @author pcingola
 *
 */
public class IntervalComparatorByEnd implements Comparator<Marker> {

	int order = 1;

	public IntervalComparatorByEnd() {
		super();
	}

	public IntervalComparatorByEnd(boolean reverse) {
		super();
		if (reverse) order = -1;
	}

	@Override
	public int compare(Marker i1, Marker i2) {
		// Compare chromosome
		if ((i1.getChromosomeNum() == 0) || (i2.getChromosomeNum() == 0)) { // Use string version?
			// Chromosome by string
			int c = i1.getChromosomeName().compareTo(i2.getChromosomeName());
			if (c != 0) return order * c;
		} else {
			// Use numeric version
			if (i1.getChromosomeNum() > i2.getChromosomeNum()) return order;
			if (i1.getChromosomeNum() < i2.getChromosomeNum()) return -order;
		}

		// End
		if (i1.getEndClosed() > i2.getEndClosed()) return order;
		if (i1.getEndClosed() < i2.getEndClosed()) return -order;

		// Start
		if (i1.getStart() > i2.getStart()) return order;
		if (i1.getStart() < i2.getStart()) return -order;

		// Compare by ID
		if ((i1.getId() == null) && (i2.getId() == null)) return 0;
		if ((i1.getId() != null) && (i2.getId() == null)) return -1;
		if ((i1.getId() == null) && (i2.getId() != null)) return 1;
		return i1.getId().compareTo(i2.getId());
	}
}
