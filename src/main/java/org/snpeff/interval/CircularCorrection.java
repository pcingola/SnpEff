package org.snpeff.interval;

/**
 * Correct circular genomic coordinates
 * 
 * Nomenclature: We use coordinates at the beginning of the chromosme and negative coordinates
 */
public class CircularCorrection {

	boolean debug;
	boolean corrected;
	boolean correctLargeGap = false;
	Transcript tr;
	int chrLen;

	public CircularCorrection(Transcript tr) {
		this.tr = tr;
		chrLen = tr.getChromosome().size();
	}

	public CircularCorrection(Transcript tr, int chrLen) {
		this.tr = tr;
		this.chrLen = chrLen;
	}

	/**
	 * Return a circular corrected transcript or null if no correction is needed
	 */
	public boolean correct() {
		// Correct CDSs?
		Markers cdss = new Markers(tr.getCds());
		if (correct(cdss)) {
			corrected = true;
			tr.sortCds();
		}

		// Correct exons?
		Markers exons = new Markers(tr.subIntervals());
		corrected |= correct(exons);

		// Some redundant exons might have to be deleted
		// E.g if one exon with corrected coordinates now overlaps another exon
		if (corrected) tr.deleteRedundant();

		// New transcript?
		return corrected;
	}

	/**
	 * Correct markers
	 * @return New set of markers or null if no correction is performed
	 */
	boolean correct(Markers markers) {
		boolean corr = false;
		if (isCorrectionStartAfterEnd(markers)) corr |= correctStartAfterEnd(markers);
		if (isCorrectionAfterChrEnd(markers)) corr |= correctAfterChrEnd(markers);
		if (isCorrectionLargeGap(markers)) corr |= correctLargeGap(markers);

		return corr;
	}

	/**
	 * Correct markers: Coordinates after chromosome end
	 * Markers after chromosome end
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:                                                                 [***]   [***] [****]
	 * Correction: Shift all coordinates to the left
	 */
	boolean correctAfterChrEnd(Markers markers) {
		boolean corr = false;
		for (Marker m : markers) {
			if (m.getStart() > 0) {
				m.shiftCoordinates(-chrLen);
				corr = true;
			}
		}
		return corr;
	}

	/**
	 * Correct markers: Large Gap
	 * Some markers have coordinates at the end while others at the beginning
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:    [***]                                                            [***]  [***]
	 * Correction: Only correct the markers at the end
	 */
	boolean correctLargeGap(Markers markers) {
		Marker rightGap = findLargeGap(markers);
		int gapPos = rightGap.getStart();

		boolean corr = false;
		for (Marker m : markers) {
			// Correct coordinates if the marker is to the right of the gap.
			// I.e.: Move from the end of the chromosome to the start of the chromosome
			if (m.getStart() >= gapPos) {
				m.shiftCoordinates(-chrLen);
				corr = true;
			}
		}

		return corr;
	}

	/**
	 * Correct markers: Start after end
	 * One marker has end after start (i.e. marker crosses "end-of-chr" boundary)
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:   >>]                                                          [***]   [***] [>>
	 * Correction: All coordinates at the end have to be shifted to the beginning
	 */
	boolean correctStartAfterEnd(Markers markers) {
		boolean corr = false;
		for (Marker m : markers) {
			if (isCorrectionStartAfterEnd(m)) {
				m.setStart(m.getStart() - chrLen);
				corr = true;
			}
		}
		return corr;
	}

	/**
	 * Find the marker to the right of the largest gap
	 */
	Marker findLargeGap(Markers markers) {
		int maxGap = 0;
		int endPrev = -1;
		boolean first = true;
		Marker rightGap = null;
		for (Marker m : markers.sort()) {
			// Calculate maximum gap between markers
			if (!first) {
				int gap = m.getStart() - endPrev;
				if (maxGap < gap) {
					maxGap = gap;
					rightGap = m;
				}
			}
			endPrev = m.getEndClosed();
			first = false;
		}

		return rightGap;
	}

	public boolean isCorrected() {
		return corrected;
	}

	/**
	 * Does marker has coordinates after chrEnd;
	 */
	boolean isCorrectionAfterChrEnd(Marker m) {
		return m.getStart() >= chrLen || m.getEndClosed() >= chrLen;
	}

	/**
	 * Does any marker has coordinates after chrEnd;
	 */
	boolean isCorrectionAfterChrEnd(Markers ms) {
		for (Marker m : ms)
			if (isCorrectionAfterChrEnd(m)) return true;
		return false;
	}

	/**
	 * Is there a large gap?
	 * This indicates some markers at the end and some markers at the beginning
	 */
	boolean isCorrectionLargeGap(Markers markers) {
		if (!correctLargeGap) return false;
		if (markers.size() <= 1) return false;

		int maxGap = 0;
		int endPrev = -1;
		boolean first = true;
		for (Marker m : markers.sort()) {
			// Calculate maximum gap between markers
			if (!first) {
				int gap = m.getStart() - endPrev;
				maxGap = Math.max(maxGap, gap);
			}
			endPrev = m.getEndClosed();
			first = false;
		}

		// Large gap between markers (e.g. one at the beginning and one at the end of the chromosome)
		return maxGap > chrLen / 2;
	}

	/**
	 * Does marker has coordinates start > end
	 */
	boolean isCorrectionStartAfterEnd(Marker m) {
		return m.getStart() > m.getEndClosed();
	}

	boolean isCorrectionStartAfterEnd(Markers ms) {
		for (Marker m : ms)
			if (isCorrectionStartAfterEnd(m)) return true;
		return false;
	}

	public boolean isDebug() {
		return debug;
	}

	boolean needsCorrection(Markers markers) {
		return isCorrectionAfterChrEnd(markers) //
				|| isCorrectionStartAfterEnd(markers) //
				|| isCorrectionLargeGap(markers);
	}

	public void setCorrectLargeGap(boolean correctLargeGap) {
		this.correctLargeGap = correctLargeGap;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Markers cdss = new Markers(tr.getCds());
		Markers exons = new Markers(tr.subIntervals());

		sb.append("Circular correction\n");
		sb.append("\n\tCorrected :" + corrected);
		sb.append("\n\tChr len   :" + chrLen);
		sb.append("\n\tCDS corrections :");
		sb.append("\n\t\tAfter Chr End   :" + isCorrectionAfterChrEnd(cdss));
		sb.append("\n\t\tLarge Gap       :" + isCorrectionLargeGap(cdss));
		sb.append("\n\t\tStart After End :" + isCorrectionStartAfterEnd(cdss));
		sb.append("\n\tExons corrections :");
		sb.append("\n\t\tAfter Chr End   :" + isCorrectionAfterChrEnd(exons));
		sb.append("\n\t\tLarge Gap       :" + isCorrectionLargeGap(exons));
		sb.append("\n\t\tStart After End :" + isCorrectionStartAfterEnd(exons));
		sb.append("\n\tTranscript :\n" + tr);
		sb.append("\n\tCDSs:\n" + cdss);
		return sb.toString();
	}
}
