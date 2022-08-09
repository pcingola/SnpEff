package org.snpeff.stats;

import java.util.HashSet;

import org.snpeff.coverage.MarkerTypes;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.probablility.RandMarker;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Calculate the maximum interval length by type, for all markers in a genome
 * Create a probability model based on binomial ditribution.
 *
 * @author pcingola
 */
public class ReadsOnMarkersModel {

	boolean verbose;
	int readLength, numIterations, numReads;
	CountByType countBases; // Number of bases covered by each marker type
	CountByType countMarkers; // Number of markers (for each marker type)
	CountByType rawCountMarkers; // Number of markers (before join or overlap)
	CountByType rawCountBases; // Number of bases covered by each marker type (befoew join or overlap)
	CountByType prob; // Binomial probability (for each marker type)
	SnpEffectPredictor snpEffectPredictor;
	MarkerTypes markerTypes;

	public ReadsOnMarkersModel(SnpEffectPredictor snpEffectPredictor) {
		super();
		this.snpEffectPredictor = snpEffectPredictor;
		countBases = new CountByType();
		countMarkers = new CountByType();
		rawCountMarkers = new CountByType();
		rawCountBases = new CountByType();
		prob = null;
		markerTypes = new MarkerTypes();
	}

	/**
	 * Count bases covered for each marker type
	 */
	public void countBases() {
		//---
		// Add all markers
		//---
		Markers markers = new Markers();
		markers.add(snpEffectPredictor.getMarkers());
		for (Gene gene : snpEffectPredictor.getGenome().getGenes()) {
			markers.add(gene);
			markers.add(gene.markers());
		}

		for (Chromosome chr : snpEffectPredictor.getGenome())
			markers.add(chr);

		//---
		// Calculate raw counts
		//---
		for (Marker m : markers) {
			String mtype = markerTypes.getType(m);
			String msubtype = markerTypes.getSubType(m);

			rawCountMarkers.inc(mtype);
			rawCountBases.inc(mtype, m.size());

			// Count sub-types (if any)
			if (msubtype != null) {
				rawCountMarkers.inc(msubtype);
				rawCountBases.inc(msubtype, m.size());
			}
		}

		//---
		// Count number of bases for each marker type (overlap and join)
		//---
		for (String mtype : rawCountMarkers.keysSorted()) {
			if (mtype.equals(Chromosome.class.getSimpleName())) continue; // We calculate chromosomes later (it's faster)

			if (verbose) System.err.print(mtype + ":");

			if (countMarkers.get(mtype) == 0) {
				for (Chromosome chr : snpEffectPredictor.getGenome())
					countBases(mtype, chr, markers);
			}

			if (verbose) Log.info("");
		}

		// Show chromosomes length
		String mtype = Chromosome.class.getSimpleName();
		for (Chromosome chr : snpEffectPredictor.getGenome()) {
			countBases.inc(mtype, chr.size());
			countMarkers.inc(mtype);
		}
	}

	/**
	 * Count number of bases, for a given chromosome and marker type
	 * @param mtype
	 * @param chr
	 * @param markers
	 * @return
	 */
	void countBases(String mtype, Chromosome chr, Markers markers) {
		String chrName = chr.getChromosomeName();
		if (verbose) System.err.print(" " + chrName);

		// Initialize
		byte busy[] = new byte[chr.size()];
		for (int i = 0; i < busy.length; i++)
			busy[i] = 0;

		for (Marker m : markers) {
			// Same marker type & same chromo? Count bases
			if (m.getChromosomeName().equals(chrName) && markerTypes.isType(m, mtype)) {
				for (int i = m.getStart(); i <= m.getEndClosed(); i++)
					busy[i] = 1;
			}
		}

		int latest = 0;
		for (int i = 0; i < busy.length; i++) {
			// Transition? Count another marker
			if ((i > 0) && (busy[i] != 0) && (busy[i - 1] == 0)) {
				if ((i - latest) <= readLength) countBases.inc(mtype, i - latest); // Intervals are less than one read away? Unify them
				else countMarkers.inc(mtype);
			}

			// Base busy? Count another base
			if (busy[i] != 0) {
				countBases.inc(mtype);
				latest = i;
			}
		}
	}

	public CountByType getCountBases() {
		return countBases;
	}

	public CountByType getCountMarkers() {
		return countMarkers;
	}

	public CountByType getProb() {
		return prob;
	}

	public CountByType getRawCountBases() {
		return rawCountBases;
	}

	public CountByType getRawCountMarkers() {
		return rawCountMarkers;
	}

	/**
	 * Load data from a file
	 * @param fileName
	 */
	public void load(String fileName) {

		boolean header = true;
		for (String line : Gpr.readFile(fileName).split("\n")) {
			if (header) {
				header = false;
				continue;
			}

			// Split line and parse data
			String recs[] = line.split("\t");

			String mtype = recs[0];
			countBases.inc(mtype, Gpr.parseIntSafe(recs[1]));
			countMarkers.inc(mtype, Gpr.parseIntSafe(recs[2]));
		}
	}

	/**
	 * Calculate probabilities
	 */
	void probabilities() {
		// Already done, nothing to do
		if (prob != null) return;

		// Get total length and count for chromosomes (chromosome size is total genome length)
		String chrType = Chromosome.class.getSimpleName();
		long chrSize = countBases.get(chrType);
		long chrCount = countMarkers.get(chrType);
		if (chrCount <= 0) return; // Zero length genome? Forgot to count bases?

		// Correct readLength
		int readLength = this.readLength;
		if (readLength < 1) readLength = 1;

		// Probabilities for each marker
		prob = new CountByType();
		for (String mtype : countMarkers.keysSorted()) {
			long size = countBases.get(mtype);
			long count = countMarkers.get(mtype);

			// Calculate and cap probability value
			double p = ((double) (size + (readLength - 1) * count)) / ((double) (chrSize - (readLength - 1) * chrCount));
			p = Math.min(1.0, p);
			p = Math.max(0.0, p);

			prob.setScore(mtype, p);
		}
	}

	/**
	 * Sample and calculate the probability of hitting each type
	 * of marker (marker.class). Creates 'numReads' reads of
	 * size 'readLen' and count how many of them hit each marker
	 * type.
	 */
	CountByType randomSampling(int readLen, int numReads) {
		CountByType countReads = new CountByType();
		RandMarker randMarker = new RandMarker(snpEffectPredictor.getGenome());

		for (int i = 0; i < numReads; i++) {
			// Random read
			Marker read = randMarker.rand(readLen);

			// Where does it hit?
			Markers regions = snpEffectPredictor.queryDeep(read);
			HashSet<String> doneRegion = new HashSet<String>();
			for (Marker m : regions) {
				String mtype = markerTypes.getType(m);
				String msubtype = markerTypes.getSubType(m);

				if (!doneRegion.contains(mtype)) {
					countReads.inc(mtype); // Count reads
					doneRegion.add(mtype); // Do not count twice
				}

				if ((msubtype != null) && !doneRegion.contains(msubtype)) {
					countReads.inc(msubtype); // Count reads
					doneRegion.add(msubtype); // Do not count twice
				}

			}
		}

		return countReads;
	}

	/**
	 * Sample and calculate the probability of hitting each type
	 * of marker (marker.class). Creates 'numReads' reads of
	 * size 'readLen' and count how many of them hit each marker
	 * type. Iterate 'iterations' times to obtain a distribution.
	 */
	public void randomSampling(int iterations, int readLen, int numReads) {
		System.out.print("Iteration");
		for (String type : rawCountMarkers.keysSorted())
			System.out.print("\t" + type);
		System.out.println("");

		for (int it = 0; it < iterations; it++) {
			CountByType count = randomSampling(readLen, numReads);
			System.out.print(it);
			for (String type : rawCountMarkers.keysSorted())
				System.out.print("\t" + count.get(type));
			System.out.println("");
		}
	}

	/**
	 * Run
	 * @return
	 */
	public boolean run() {
		countBases(); // Count
		probabilities(); // Calculate probabilities
		return true;
	}

	/**
	 * Save model to file
	 * @param fileName
	 */
	public void save(String fileName) {
		StringBuilder sb = new StringBuilder();
		sb.append("marker_type\tsize\tcount\tbinomial_p\n");

		probabilities();
		for (String mtype : markerTypes.markerTypesClass())
			sb.append(mtype + "\t" + countBases.get(mtype) + "\t" + countMarkers.get(mtype) + "\n");

		Gpr.toFile(fileName, sb.toString());
	}

	public void setMarkerTypes(MarkerTypes markerTypes) {
		this.markerTypes = markerTypes;
	}

	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	public void setNumReads(int numReads) {
		this.numReads = numReads;
	}

	public void setReadLength(int readLength) {
		this.readLength = readLength;
	}

	public void setSnpEffectPredictor(SnpEffectPredictor snpEffectPredictor) {
		this.snpEffectPredictor = snpEffectPredictor;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("marker_type\tsize\tcount\traw_size\traw_count\tbinomial_p\n");

		probabilities();
		for (String mtype : countMarkers.keysSorted())
			sb.append(mtype + "\t" + countBases.get(mtype) + "\t" + countMarkers.get(mtype) + "\t" + rawCountBases.get(mtype) + "\t" + rawCountMarkers.get(mtype) + "\t" + prob.getScore(mtype) + "\n");

		return sb.toString();
	}

}
