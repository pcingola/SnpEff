package org.snpeff.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.snpeff.fileIterator.LineFileIterator;

/**
 * Generic utility methods for Markers
 *
 * @author pcingola
 */
public class MarkerUtil {

	/**
	 * Collapse adjacent intervals (i.e. intervals separated by a gap of zero length
	 * E.g.: The markers [1-100] and [101-200] are collapsed into one single marker [1-200]
	 *
	 * @return A set of new markers that can replace the old ones, or the same set if no change is required.
	 */
	public static Map<Marker, Marker> collapseZeroGap(Markers markersOri) {
		Map<Marker, Marker> collapse = new HashMap<Marker, Marker>();

		// Sort markers by start
		Markers sorted = new Markers();
		sorted.add(markersOri);
		sorted.sort(false, false);

		// Create new set of markers
		Marker markerPrev = null; // Previous marker in the list
		Marker markerToAdd = null;
		int countCollapsed = 0;
		for (Marker m : sorted) {
			if (markerToAdd == null) markerToAdd = m.clone();

			if (markerPrev != null) {
				// Find start, end and gap size
				int start = markerPrev.getEndClosed() + 1;
				int end = m.getStart() - 1;
				int gapSize = end - start + 1;

				if (gapSize <= 0) {
					countCollapsed++;
					if (markerToAdd.getEndClosed() < m.getEndClosed()) markerToAdd.setEndClosed(m.getEndClosed()); // Set new end for this marker (we are collapsing it with the previous one)

					// Do we need to correct frame information?
					if (markerToAdd.isStrandMinus() && (markerToAdd instanceof MarkerWithFrame) && (m instanceof MarkerWithFrame)) {
						MarkerWithFrame markerToAddWf = (MarkerWithFrame) markerToAdd;
						MarkerWithFrame mwf = (MarkerWithFrame) m;
						markerToAddWf.setFrame(mwf.getFrame());

					}
				} else markerToAdd = m.clone(); // Get ready for next iteration

			}
			collapse.put(m, markerToAdd);
			markerPrev = m;
		}

		// Sanity check
		HashSet<Marker> collapsed = new HashSet<Marker>();
		collapsed.addAll(collapse.values());
		if ((markersOri.size() - countCollapsed) != collapsed.size()) throw new RuntimeException("Sanitycheck failed. This should never happen!\n\tmarkers.size: " + markersOri.size() + "\n\tcountCollapsed: " + countCollapsed + "\n\treplaced.size : " + collapsed.size());

		return collapse;
	}

	/**
	 * Read intervals from a file using a simplt TXT format
	 * Format:
	 * 		chr \t start \t end \t id
	 *
	 * Note: Zero-based positions
	 *
	 * @param fileName : Path to file
	 * @param genome : Genome to use. Can be null (a new one will be created)
	 * @param positionBase : Position offset. Use '1' for one-based coordinates and '0' for zero-based coordinates.
	 */
	public static Markers readTxt(String fileName, Genome genome, int positionBase) {
		if (genome == null) genome = new Genome();
		Markers markers = new Markers();

		// Parse lines
		LineFileIterator lfi = new LineFileIterator(fileName);
		int lineNum = 1;
		for (String line : lfi) {
			Marker interval = new Marker();
			interval.readTxt(line, lineNum, genome, positionBase);
			markers.add(interval);
			lineNum++;
		}
		return markers;
	}

	/**
	 * Redundant markers in a list: Find intervals that are totally included in other intervals in the list
	 * @param markersOri
	 * @return A map  markerIncluded -> markerLarge, where  markerIncluded in completely included in markerLarge
	 *
	 * WARNING: Markers having start > end (i.e. circular chromosome with
	 *          uncorrected coordinates) are not processed correctly by this
	 *          method (they are explicitly ignored)
	 */
	public static Map<Marker, Marker> redundant(Collection<? extends Marker> markersOri) {
		Map<Marker, Marker> redundant = new HashMap<Marker, Marker>();

		// Find which markers are redundant?
		ArrayList<Marker> markers = new ArrayList<Marker>();
		// Add all markers. Exception: Markers form circular chromosomes
		// whose coordinates have not been corrected (we take care of
		// these when we correct circular coordinates)
		for (Marker m : markersOri)
			if (m.getStart() <= m.getEndClosed()) {
				markers.add(m);
			}
		int size = markers.size();

		// Iterate on all markers
		for (int i = 0; i < size; i++) {
			Marker mi = markers.get(i);

			// Is marker 'mi' included in any other marker?
			Marker markerLarge = null;
			for (int j = 0; (j < size) && (markerLarge == null); j++) {
				Marker mj = markers.get(j);
				if ((i != j) && (mj.includes(mi))) { // Not the same interval and it is fully included?
					if (mi.includes(mj) && (i > j)) {
						// If they are included both ways, it means that they are exactly the same.
						// We have to avoid deleting both of them twice, so we arbitrarely don't add them if (i > j)
					} else markerLarge = mj;
				}
			}

			// Add to redundant marker
			if (markerLarge != null) redundant.put(mi, markerLarge);
		}

		return redundant;
	}

}
