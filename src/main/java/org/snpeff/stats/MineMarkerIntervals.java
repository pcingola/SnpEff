package org.snpeff.stats;

import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;

/**
 * Mine marker intervals: I.e. perform some statistics based on a collection of intervals
 * 
 * @author pcingola
 *
 */
public class MineMarkerIntervals {

	Markers markers;
	IntStats lengthStats;
	IntStats distanceStats;

	public MineMarkerIntervals(Markers markers) {
		this.markers = markers;
		lengthStats = new IntStats();
		distanceStats = new IntStats();
	}

	public IntStats getDistanceStats() {
		return distanceStats;
	}

	public IntStats getLengthStats() {
		return lengthStats;
	}

	public void stats() {
		// Sort by start
		markers.sort(false, false);

		Marker prev = null;
		for( Marker m : markers ) {
			lengthStats.sample(m.size());

			// Distance to previous interval
			if( (prev != null) //
					&& (m.getChromosome().getId().equals(prev.getChromosome().getId())) // Same chromosome?
			) {
				if( m.intersects(prev) ) distanceStats.sample(0); // Intersect? => Distance is zero
				else distanceStats.sample(m.getStart() - prev.getEndClosed()); // Perform distance stats
			}

			prev = m;
		}
	}
}
