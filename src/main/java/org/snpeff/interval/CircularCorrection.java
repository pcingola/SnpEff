package org.snpeff.interval;

import org.snpeff.util.Gpr;

/**
 * Correct circular genomic coordinates
 * Nomenclature: We use coordinates at the beginning of the chromosme and negative coordinates
 *
 *
 */
public class CircularCorrection {

	boolean debug;
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
	public Transcript correct() {
		// Correct CDSs?
		Markers cdss = new Markers(tr.getCds());
		cdss = correct(cdss);

		// Correct exons?
		Markers exons = new Markers(tr.subIntervals());
		exons = correct(exons);

		// New transcript?
		if (cdss != null || exons != null) return replaceCdsExons(tr, cdss, exons);

		return null;
	}

	/**
	 * Correct markers
	 * @return New set of markers or null if no correction is performed
	 */
	Markers correct(Markers markers) {
		if (isCorrectionLargeGap(markers)) return correctLargeGap(markers);
		else if (isCorrectionAfterChrEnd(markers)) return correctAfterChrEnd(markers);
		else if (isCorrectionStartAfterEnd(markers)) return correctStartAfterEnd(markers);

		return null;
	}

	/**
	 * Correct markers: Coordinates after chromosome end
	 * Markers after chromosome end
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:                                                                 [***]   [***] [****]
	 * Correction: Shift all coordinates to the begininig
	 */
	Markers correctAfterChrEnd(Markers markers) {
		Markers ms = new Markers();
		for (Marker m : markers) {
			m.shiftCoordinates(-chrLen);
			ms.add(m);
		}
		return ms;
	}

	/**
	 * Correct markers: Large Gap
	 * Some markers have coordinates at the end while others at the beginning
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:    [***]                                                            [***]  [***]
	 * Correction: Only correct the markers at the end
	 */
	Markers correctLargeGap(Markers markers) {
		Marker rightGap = findLargeGap(markers);
		int gapPos = rightGap.getStart();

		Markers ms = new Markers();
		for (Marker m : markers) {
			// Correct coordinates if the marker is to the right of the gap.
			// I.e.: Move from the end of the chromosome to the beggining of the chromosome
			if (m.getStart() >= gapPos) {
				m.shiftCoordinates(-chrLen);
			}
			ms.add(m);
		}

		return ms;
	}

	/**
	 * Correct markers: Start after end
	 * One marker has end after start (i.e. marker croses "end-of-chr" boundary)
	 * Chr:    |------------------------------------------------------------------------------|
	 * Exons:   >>]                                                          [***]   [***] [>>
	 * Correction: All coordinates at the end have to be shifted to the beginning
	 */
	Markers correctStartAfterEnd(Markers markers) {
		Markers ms = new Markers();
		for (Marker m : markers) {
			if (isCorrectionStartAfterEnd(m)) {
				m.setStart(m.getStart() - chrLen);
			}
			ms.add(m);
		}
		return ms;
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
			endPrev = m.getEnd();
			first = false;
		}

		return rightGap;
	}

	/**
	 * Does marker has coordinates after chrEnd;
	 */
	boolean isCorrectionAfterChrEnd(Marker m) {
		return m.getStart() >= chrLen || m.getEnd() >= chrLen;
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
			endPrev = m.getEnd();
			first = false;
		}

		// Large gap between markers (e.g. one at the beginning and one at the end of the chromosome)
		return maxGap > chrLen / 2;
	}

	/**
	 * Does marker has coordinates start > end
	 */
	boolean isCorrectionStartAfterEnd(Marker m) {
		return m.getStart() > m.getEnd();
	}

	boolean isCorrectionStartAfterEnd(Markers ms) {
		for (Marker m : ms)
			if (isCorrectionStartAfterEnd(m)) return true;
		return false;
	}

	public boolean needsCorrection() {
		return false;
	}

	boolean needsCorrection(Markers markers) {
		return isCorrectionAfterChrEnd(markers) //
				|| isCorrectionStartAfterEnd(markers) //
				|| isCorrectionLargeGap(markers);
	}

	Transcript replaceCdsExons(Transcript tr, Markers cdss, Markers exons) {
		if (debug) Gpr.debug("Before replacing:" + tr + "\n\tCDS:" + tr.getCds());
		Transcript trNew = tr.cloneShallow();

		// Add new CDSs
		if (cdss == null) {
			for (Cds cds : tr.getCds())
				trNew.add(cds);
		} else {
			// Use new CDSs
			for (Marker cds : cdss.sort())
				trNew.add((Cds) cds);
		}

		// Add all exons
		if (exons == null) {
			for (Exon ex : tr)
				trNew.add(ex);
		} else {
			// Use new exons
			for (Marker ex : exons)
				trNew.add((Exon) ex);

		}

		if (debug) Gpr.debug("After replacing:" + trNew + "\n\tCDS:" + trNew.getCds());
		return trNew;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	//	/**
	//	 * Correct coordinates for circular chromosomes
	//	 *
	//	 * WARNING: This method assumes the the markers are sorted according to genomic order
	//	 *          E.g. if these are CDS, they are assumed to be sorted according to how they
	//	 *          are translated, which might differ from genomic coordinates (particularly
	//	 *          in case of circular chromosomes.
	//	 *
	//	 * WARNING: This corrects the coordinates of the markers (it does not clone them)
	//	 *
	//	 * WARNIGN: It returns the same Markers if no correction is needed
	//	 */
	//	public Markers circularCorrect() {
	//		Markers corrected = new Markers();
	//		if (isEmpty()) return corrected;
	//
	//		// Assume all markers are on the same strand
	//		Marker first = markers.get(0);
	//		boolean strandPlus = first.isStrandPlus();
	//
	//		// Assume all markers in the same chromosome
	//		Chromosome chr = first.getChromosome();
	//		int chrEnd = chr.getEnd();
	//
	//		// Chromosome length not set? We cannot perform this correction
	//		if (chrEnd <= 0) return this;
	//
	//		// Sanity check
	//		int countAfterChrEnd = 0, countBeforeZero = 0, countCircularRight = 0, countCircularLeft = 0;
	//		int endPrev = -1, startPrev = Integer.MAX_VALUE;
	//		for (Marker m : this) {
	//			if (m.isStrandPlus() != strandPlus) throw new RuntimeException("Fatal error:All markers must be in the same strand!");
	//			if (m.getChromosomeName() != chr.getId()) throw new RuntimeException("Fatal error:All markers must be in the same chromosome!");
	//
	//			if (m.getStart() < 0) countBeforeZero++;
	//			else if (m.getEnd() > chrEnd) countAfterChrEnd++;
	//			else if (strandPlus && m.getStart() < endPrev) countCircularRight++;
	//			else if (!strandPlus && m.getEnd() > startPrev) countCircularLeft++;
	//
	//			endPrev = m.getEnd();
	//			startPrev = m.getStart();
	//		}
	//
	//		// Some intervals before 'zero' coordinate. OK this is the nomenclature we want
	//		if (countBeforeZero > 0) {
	//			// No adjustment needed
	//			return this;
	//		}
	//
	//		// Some intervals after 'chr.end' coordinate.
	//		int len = chr.size();
	//		if (countAfterChrEnd > 0) {
	//			// We need to shift them on chr.length
	//			for (Marker m : this) {
	//				m.shiftCoordinates(-len);
	//				corrected.add(m);
	//			}
	//			return corrected;
	//		}
	//
	//		// Some markers are "before" in coordantes than the previous ones.
	//		// We assume they are passing the 'chr.end' boundary and starting from
	//		// zero in positive stranded 'gene'
	//		if (countCircularRight > 0) {
	//			// We need to shift some markers before zero
	//			corrected.addAll(this);
	//			for (Marker m : corrected) {
	//				if (strandPlus && m.getStart() < endPrev) break; // We stop when we find the first one after passing the "zero coordinate"
	//				m.shiftCoordinates(-len);
	//			}
	//			return corrected.sort(false, false);
	//		}
	//
	//		// Some markers are "after" in coordinates than the previous ones.
	//		// We assume they are passing the 'zero' boundary and starting from
	//		// chr.end in negative stranded 'gene'
	//		if (countCircularLeft > 0) {
	//			// We need to shift some markers before zero
	//			corrected.addAll(this);
	//			boolean correct = false;
	//			for (Marker m : corrected) {
	//				if (!strandPlus && m.getEnd() > startPrev) correct = true; // We stop when we find the first one after passing the "chr.end"
	//				if (correct) m.shiftCoordinates(-len);
	//			}
	//			return corrected.sort(false, true);
	//		}
	//
	//		return this;
	//	}
	//
	//	/**
	//	 * Correct circular coordinates
	//	 *
	//	*/
	//	public void circularCorrection(int chrLen) {
	//		if (circularCorrectionExon(chrLen)) {
	//			circularCorrectionCds(chrLen);
	//		}
	//	}
	//
	//	void circularCorrectionCds(int chrLen) {
	//		for (Cds cds : getCds())
	//			cds.shiftCoordinates(-chrLen);
	//	}
	//
	//	/**
	//	 * Correct circular exon coordinates
	//	 *
	//	 * Note: Nomenclature for circular chromosomes we use negative coordinates
	//	 *       spanning over "zero". This is arbitrary, we could have used
	//	 *       coordinates spanning over chromo.length
	//	 *
	//	 * Note: This method only "corrects" coordiantes for exons having [start, end] after
	//	 *       the chromsome end. No correction is applied to exons having negative
	//	 *       coordinates (since that's the nomenclature that we want to use).
	//	 */
	//	boolean circularCorrectionExon(int chrLen) {
	//		boolean corrected = false;
	//
	//		int maxExonLen = 0;
	//		for (Exon exon : this) {
	//			int exStart = exon.getStart();
	//			int exEnd = exon.getEnd();
	//			maxExonLen = Math.max(maxExonLen, exon.size());
	//
	//			// Coordinates spanning over 'chrLen': We need to correct them
	//			if (exStart > exEnd) {
	//				// End coordinate before Start? This must be a circular chromosome
	//				// Correct by subtracting chromosome length to start
	//				exon.setStart(exon.getStart() - chrLen);
	//			} else if ((exStart >= chrLen) || (exEnd >= chrLen)) {
	//				exon.shiftCoordinates(-chrLen);
	//				corrected = true;
	//			}
	//		}
	//
	//		// Not corrected? Nothing else to do
	//		if (!corrected) return false;
	//
	//		// Do we need to correct other exons that have coordinates within limits?
	//		// A typical case would be when start/stop codons are expressed in other
	//		// coordinates
	//		// E.g.:
	//		//		ChrLen     : 10,000
	//		// 		exonStart  : [ 9900, 9902 ]		// Start codon expressed in "span-over-end" coordinates (it should have been [-100, -98]
	//		// 		exon1      : [ -100, 100 ]		// Exon expressed in "span-over-zero" coordinates
	//		// 		exonStop   : [ 10098, 10100 ]	// Stop Codon expressed in "after-chr-end" coordinates (it should have been [98, 100])
	//		//
	//		do {
	//			corrected = false;
	//			for (Exon exon : this) {
	//				// We analyze exons on the left half of the chromosome (which are candidates to be
	//				if ((exon.getStart() >= chrLen / 2) //
	//						&& (exon.getEnd() <= chrLen)) {
	//					// Do we need to correct these coordinates?
	//					Exon exShifted = exon.cloneShallow();
	//					exShifted.shiftCoordinates(-chrLen);
	//
	//					// Minimum distance to other exons expressed in negative coordiantes
	//					int minDist = Integer.MAX_VALUE;
	//					for (Exon ex : this) {
	//						if (ex.getStart() < 0) {
	//							minDist = Math.min(minDist, exShifted.distance(ex));
	//						}
	//					}
	//
	//					// Distance is "small" => we assume that we need to correct this exon as well.
	//					if (minDist >= 0 && minDist < maxExonLen) {
	//						exon.shiftCoordinates(-chrLen);
	//						corrected = true;
	//					}
	//				}
	//			}
	//		} while (corrected);
	//
	//		deleteRedundant();
	//		resetCache();
	//		return true;
	//	}

}
