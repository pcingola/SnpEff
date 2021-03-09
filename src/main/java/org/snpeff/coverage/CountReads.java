package org.snpeff.coverage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.snpeff.fileIterator.BedFileIterator;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.stats.CountByKey;
import org.snpeff.stats.CountByType;
import org.snpeff.stats.CoverageByType;
import org.snpeff.stats.PosStats;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

/**
 * Count how many reads map (from many SAM/BAM files) onto markers
 *
 * @author pcingola
 */
public class CountReads {

	public static int SHOW_EVERY = 10000;

	boolean verbose = false; // Be verbose
	int countTotalReads;
	int readLengthCount;
	int countExceptions = 0;
	long readLengthSum;
	String fileName;
	Genome genome;
	SnpEffectPredictor snpEffectPredictor;
	CountByType countTypes;
	CountByKey<Marker> countReads;
	CountByKey<Marker> countBases;
	MarkerTypes markerTypes;
	CoverageByType coverageByType;
	ArrayList<CoverageByType> coverageByExons;

	public CountReads(String fileName, SnpEffectPredictor snpEffectPredictor) {
		this.fileName = fileName;

		if (snpEffectPredictor != null) this.snpEffectPredictor = snpEffectPredictor;
		else this.snpEffectPredictor = new SnpEffectPredictor(new Genome());

		markerTypes = new MarkerTypes();
		coverageByExons = new ArrayList<>();
	}

	public void addMarkerType(Marker marker, String type) {
		markerTypes.addType(marker, type);
	}

	/**
	 * Create a collection of all markers
	 *
	 * @return
	 */
	Collection<Marker> allMarkers() {
		return countReads.keySet();
	}

	/**
	 * Count markers from a file
	 */
	public void count() {
		genome = snpEffectPredictor.getGenome();

		readLengthSum = 0;
		readLengthCount = 0;

		// Iterate over all BAM/SAM files
		try {
			if (verbose) Log.info("Reading file '" + fileName + "'");
			countReads = new CountByKey<>();
			countBases = new CountByKey<>();
			countTypes = new CountByType();
			coverageByType = new CoverageByType();
			countFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (verbose) {
			System.err.println("");
			Log.info("Finished reding file " + fileName + "\n\tTotal reads: " + countTotalReads);
		}
		if (verbose) Log.info("Done.");
	}

	/**
	 * Count all markers from a BED file
	 */
	void countBedFile(String fileName) {
		// Open file
		int readNum = 1;

		for (Variant read : new BedFileIterator(fileName)) {
			try {
				readLengthCount++;
				readLengthSum += read.size();
				countMarker(fileName, read);
				if (verbose) Gpr.showMark(readNum, SHOW_EVERY);
				readNum++;
			} catch (Exception e) {
				countExceptions++;
				if (countExceptions < 10) e.printStackTrace();
				else if (countExceptions == 10) System.err.println("Not showing more exceptions!");
			}
		}
	}

	/**
	 * Count all markers from a SAM/BAM file
	 */
	void countFile(String fileName) {
		String fl = fileName.toLowerCase();

		if (fl.endsWith(".bam") || fl.endsWith(".sam")) countSamFile(fileName);
		else if (fl.endsWith(".vcf") || fl.endsWith(".vcf.gz")) countVcfFile(fileName);
		else if (fl.endsWith(".bed") || fl.endsWith(".bed.gz")) countBedFile(fileName);
		else throw new RuntimeException("Unrecognized file extention. Supported types: BAM, SAM, BED, VCF.");
	}

	/**
	 * Count one marker
	 */
	void countMarker(String fileName, Marker read) {
		// Find all intersects
		Markers regions = snpEffectPredictor.queryDeep(read);

		// Count total reads
		countTotalReads++;

		// Count each marker
		HashSet<Marker> done = new HashSet<>();
		for (Marker m : regions) {
			// Make sure we count only once each marker
			if (!done.contains(m)) {
				done.add(m);

				countReads.inc(m); // Count reads
				countBases.inc(m, m.intersectSize(read)); // Count number bases that intersect

				// Count by marker type (make sure we only count once per read)
				String type = markerTypes.getType(m);
				String subtype = markerTypes.getSubType(m);
				String typeRank = markerTypes.getTypeRank(m);

				countTypes.inc(type); // Count reads

				// Coverage by type
				PosStats posStats = coverageByType.getOrCreate(type);
				posStats.sample(read, m);

				// Coverage by number of exons
				coverageByExons(read, m, typeRank);

				// Count sub-type if any
				if (subtype != null) {
					countTypes.inc(subtype); // Count reads

					posStats = coverageByType.getOrCreate(subtype);
					posStats.sample(read, m);
				}
			}
		}
	}

	/**
	 * Count how many of each marker type are there
	 *
	 * @return
	 */
	CountByType countMarkerTypes(Collection<Marker> markersToCount) {
		CountByType countByMarkerType = new CountByType();
		for (Marker marker : markersToCount) {
			String type = markerTypes.getType(marker);
			String subtype = markerTypes.getSubType(marker);
			countByMarkerType.inc(type);
			if (subtype != null) countByMarkerType.inc(subtype);
		}
		return countByMarkerType;
	}

	/**
	 * Count all markers from a SAM/BAM file
	 */
	void countSamFile(String fileName) {
		// Open file
		int readNum = 1;
		// SAMFileReader sam = new SAMFileReader(new File(fileName));
		// sam.setValidationStringency(ValidationStringency.SILENT);
		SamReader sam = SamReaderFactory.makeDefault().open(new File(fileName));

		for (SAMRecord samRecord : sam) {
			try {
				if (!samRecord.getReadUnmappedFlag()) { // Mapped?
					Chromosome chr = genome.getOrCreateChromosome(samRecord.getReferenceName());
					if (chr != null) {
						// Create a marker from read
						Marker read = new Marker(chr, samRecord.getAlignmentStart(), samRecord.getAlignmentEnd(), false, "");
						readLengthCount++;
						readLengthSum += read.size();

						countMarker(fileName, read);
					}
				}

				if (verbose) Gpr.showMark(readNum, SHOW_EVERY);
				readNum++;
			} catch (Exception e) {
				countExceptions++;
				if (countExceptions < 10) e.printStackTrace();
				else if (countExceptions == 10) System.err.println("Not showing more exceptions!");
			}

		}
		try {
			sam.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing SAM/BAM file '" + fileName + "'", e);
		}
	}

	/**
	 * Count all markers from a VCF file
	 */
	void countVcfFile(String fileName) {
		// Open file
		int readNum = 1;

		for (VcfEntry read : new VcfFileIterator(fileName)) {
			try {
				readLengthCount++;
				readLengthSum += read.size();
				countMarker(fileName, read);
				if (verbose) Gpr.showMark(readNum, SHOW_EVERY);
				readNum++;
			} catch (Exception e) {
				countExceptions++;
				if (countExceptions < 10) e.printStackTrace();
				else if (countExceptions == 10) System.err.println("Not showing more exceptions!");
			}
		}
	}

	/**
	 * Coverage by number of exons in a transcript
	 *
	 * @param m
	 * @param typeRank
	 */
	void coverageByExons(Marker read, Marker m, String typeRank) {
		// Find corresponfing transcript
		Transcript tr = (Transcript) m.findParent(Transcript.class);
		if (tr == null) return;

		// Number of exons
		int exons = tr.numChilds();

		// Do we need to add new counters?
		if (coverageByExons.size() <= exons) {
			for (int i = coverageByExons.size(); i <= exons; i++)
				coverageByExons.add(new CoverageByType());
		}

		// Perform stats
		CoverageByType cbe = coverageByExons.get(exons);
		PosStats posStats = cbe.getOrCreate(typeRank);
		posStats.sample(read, m);
	}

	public CountByKey<Marker> getCountBases() {
		return countBases;
	}

	public int getCountExceptions() {
		return countExceptions;
	}

	public CountByKey<Marker> getCountReads() {
		return countReads;
	}

	public int getCountTotalReads() {
		return countTotalReads;
	}

	public CountByType getCountTypes() {
		return countTypes;
	}

	public ArrayList<CoverageByType> getCoverageByExons() {
		return coverageByExons;
	}

	public CoverageByType getCoverageByType() {
		return coverageByType;
	}

	public MarkerTypes getMarkerTypes() {
		return markerTypes;
	}

	/**
	 * Average read length
	 *
	 * @return
	 */
	public int getReadLengthAvg() {
		if (readLengthCount <= 0) return 0;
		double rl = ((double) readLengthSum) / readLengthCount;
		return (int) Math.round(rl);
	}

	public int getReadLengthCount() {
		return readLengthCount;
	}

	public long getReadLengthSum() {
		return readLengthSum;
	}

	/**
	 * Initialize
	 *
	 * @param snpEffectPredictor
	 */
	void init(SnpEffectPredictor snpEffectPredictor) {
	}

	public void setMarkerTypes(MarkerTypes markerTypes) {
		this.markerTypes = markerTypes;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
