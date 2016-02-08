package org.snpeff.stats;

import org.snpeff.interval.Markers;
import org.snpeff.interval.tree.IntervalForest;

/**
 * Mine marker intervals: I.e. perform some statistics based on a collection of intervals
 * 
 * @author pcingola
 *
 */
public class MineTwoMarkerIntervals {

	Markers markersA, markersB;

	MineMarkerIntervals setAinterctB; // A intersect B (calculate intersection, don't keep original markers)
	MineMarkerIntervals doesAintersectB; // Keep all markers in 'A' that intersect with any marker in 'B' (keep original markers, not the intersection)
	MineMarkerIntervals doesBintersectA; // Keep all markers in 'B' that intersect with any marker in 'A' (keep original markers, not the intersection)

	public MineTwoMarkerIntervals(Markers markersA, Markers markersB) {
		this.markersA = markersA;
		this.markersB = markersB;
	}

	public MineMarkerIntervals getAintersectB() {
		return setAinterctB;
	}

	public MineMarkerIntervals getDoesAintersectB() {
		return doesAintersectB;
	}

	public MineMarkerIntervals getDoesBintersectA() {
		return doesBintersectA;
	}

	public void stats() {
		//---
		// Perform intersection
		//---
		Markers small = markersA, big = markersB;
		if( markersA.size() >= markersB.size() ) {
			small = markersB;
			big = markersA;
		}

		// Build interval forest on 'small' set
		IntervalForest forestBig = new IntervalForest(big);
		Markers aIntB = forestBig.intersect(small);
		setAinterctB = new MineMarkerIntervals(aIntB);
		setAinterctB.stats();

		//---
		// Keep all markers of 'A' that intersect with any marker in 'B'
		//---
		IntervalForest forestA = new IntervalForest(markersA);
		Markers keepAifIntB = forestA.queryUnique(markersB);
		doesAintersectB = new MineMarkerIntervals(keepAifIntB);
		doesAintersectB.stats();

		//---
		// Keep all markers of 'B' that intersect with any marker in 'A'
		//---
		IntervalForest forestB = new IntervalForest(markersB);
		Markers keepBifIntA = forestB.queryUnique(markersA);
		doesBintersectA = new MineMarkerIntervals(keepBifIntA);
		doesBintersectA.stats();
	}
}
