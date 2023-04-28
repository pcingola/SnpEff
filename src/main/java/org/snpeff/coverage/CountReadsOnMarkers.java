package org.snpeff.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.probablility.Binomial;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.stats.CountByType;
import org.snpeff.stats.CoverageByType;
import org.snpeff.stats.plot.GoogleBarChart;
import org.snpeff.stats.plot.GoogleGeneRegionChart;
import org.snpeff.stats.plot.GoogleGeneRegionNumExonsChart;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Count how many reads map (from many SAM/BAM files) onto markers
 * @author pcingola
 */
public class CountReadsOnMarkers {

	public static final int SHOW_EVERY = 10000;
	public static final int MAX_EXONS_CHART = 10;
	public static boolean debug = true;

	boolean verbose = false; // Be verbose
	List<String> fileNames;
	List<String> names;
	Genome genome;
	SnpEffectPredictor snpEffectPredictor;
	ArrayList<CountReads> countReadsByFile;
	MarkerTypes markerTypes;
	CountByType readsByFile;

	public CountReadsOnMarkers(SnpEffectPredictor snpEffectPredictor) {
		init(snpEffectPredictor);
	}

	/**
	 * Add a SAM/BAM file to be processed
	 * @param samFileName
	 */
	public void addFile(String samFileName) {
		fileNames.add(samFileName);
		names.add(Gpr.removeExt(Gpr.baseName(samFileName)));
	}

	public void addMarkerType(Marker marker, String type) {
		markerTypes.addType(marker, type);
	}

	/**
	 * Create a collection of all markers
	 * @return
	 */
	List<Marker> allMarkers() {
		// Retrieve all possible keys, sort them
		HashSet<Marker> keys = new HashSet<Marker>();
		for (CountReads cr : countReadsByFile)
			keys.addAll(cr.allMarkers());

		ArrayList<Marker> keysSorted = new ArrayList<Marker>(keys.size());
		keysSorted.addAll(keys);
		Collections.sort(keysSorted);
		return keysSorted;
	}

	/**
	 * Count markers from all files
	 */
	public void count() {
		genome = snpEffectPredictor.getGenome();

		// Iterate over all BAM/SAM files
		for (String fileName : fileNames) {
			CountReads countReads = new CountReads(fileName, snpEffectPredictor);
			countReads.setMarkerTypes(markerTypes);
			countReads.setVerbose(verbose);
			countReads.count();

			countReadsByFile.add(countReads); // Add count to list
		}

		if (verbose) Log.info("Done.");
	}

	/**
	 * Count how many of each marker type are there
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

	public MarkerTypes getMarkerTypes() {
		return markerTypes;
	}

	/**
	 * Average read length
	 * @return
	 */
	public int getReadLengthAvg() {
		long readLengthCount = 0;
		long readLengthSum = 0;

		for (CountReads cr : countReadsByFile) {
			readLengthSum += cr.getReadLengthSum();
			readLengthCount += cr.getReadLengthCount();
		}

		if (readLengthCount <= 0) return 0;
		double rl = ((double) readLengthSum) / readLengthCount;
		return (int) Math.round(rl);
	}

	/**
	 * Show charts in html
	 * @return
	 */
	public String html() {
		StringBuilder sbHead = new StringBuilder();
		StringBuilder sbBody = new StringBuilder();

		//---
		// Barchart: By Marker types (all files toghether)
		//---
		ArrayList<CountByType> countTypesByFile = new ArrayList<CountByType>();
		for (CountReads cr : countReadsByFile)
			countTypesByFile.add(cr.getCountTypes());

		// Create 3 charts: One for all intervals, one for Exons and one for Introns.
		HashSet<String> keySetAll = new HashSet<String>();
		HashSet<String> keySetExon = new HashSet<String>();
		HashSet<String> keySetIntron = new HashSet<String>();
		for (CountByType ct : countTypesByFile)
			for (String key : ct.keySet())
				if (key.startsWith("Exon:")) keySetExon.add(key);
				else if (key.startsWith("Intron:")) keySetIntron.add(key);
				else keySetAll.add(key);

		keySetAll.remove("Chromosome"); // We don't want this number in the chart (usually it's too big)
		HashMap<String, HashSet<String>> keySets = new HashMap<String, HashSet<String>>();
		keySets.put("", keySetAll);
		keySets.put("Exons", keySetExon);
		keySets.put("Introns", keySetIntron);

		// Sort names
		ArrayList<String> keySetNames = new ArrayList<String>();
		keySetNames.addAll(keySets.keySet());
		Collections.sort(keySetNames);

		// Create one barchart for each keySet
		for (String ksname : keySetNames) {
			HashSet<String> keySet = keySets.get(ksname);
			// Sort keys
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(keySet);
			Collections.sort(keys);

			// Do not create empty charts
			if (keys.size() <= 0) continue;

			// Add all columns
			GoogleBarChart barchart = new GoogleBarChart("Count by file " + ksname);
			barchart.setxLables(keys);

			GoogleBarChart barchartPercent = new GoogleBarChart("Count by file " + ksname + " [Percent]");
			barchartPercent.setxLables(keys);

			// Add all files
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				CountByType ct = countTypesByFile.get(i);

				// Add all values
				ArrayList<String> columnValues = new ArrayList<String>();
				for (String key : keys)
					if (keys.contains(key)) columnValues.add(ct.get(key) + "");

				// Add column to chart
				barchart.addColumn(name, columnValues);
				barchartPercent.addColumn(name, columnValues);
			}

			// Add header and body
			sbHead.append(barchart.toStringHtmlHeader());
			sbBody.append(barchart.toStringHtmlBody());

			barchartPercent.percentColumns();
			sbHead.append(barchartPercent.toStringHtmlHeader());
			sbBody.append(barchartPercent.toStringHtmlBody());

		}

		//---
		// Barchart: By Marker types (one by file)
		//---

		// Add all files
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);

			// Create one barchart for each keySet
			for (String ksname : keySetNames) {
				// Sort keys
				HashSet<String> keySet = keySets.get(ksname);
				ArrayList<String> keys = new ArrayList<String>();
				keys.addAll(keySet);
				Collections.sort(keys);

				// Do not create empty charts
				if (keys.size() <= 0) continue;

				GoogleBarChart barchart = new GoogleBarChart("Count by file " + name + " " + ksname);
				barchart.setxLables(keys);

				// Add all columns
				CountByType ct = countTypesByFile.get(i);

				// Add all values
				ArrayList<String> columnValues = new ArrayList<String>();
				for (String key : keys)
					if (keys.contains(key)) columnValues.add(ct.get(key) + "");

				// Create chart
				barchart.addColumn(name, columnValues);
				sbHead.append(barchart.toStringHtmlHeader());
				sbBody.append(barchart.toStringHtmlBody());
			}
		}

		//---
		// Genomic region charts
		//---
		ArrayList<GoogleGeneRegionChart> genRegCharts = new ArrayList<GoogleGeneRegionChart>();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			CoverageByType cvt = countReadsByFile.get(i).getCoverageByType();

			GoogleGeneRegionChart grc = new GoogleGeneRegionChart(cvt, name);
			genRegCharts.add(grc);

			// Show for each transcript length
			ArrayList<CoverageByType> coverageByExons = countReadsByFile.get(i).getCoverageByExons();
			for (int exons = 1; exons < coverageByExons.size(); exons++) {
				CoverageByType cbt = coverageByExons.get(exons);

				if (!cbt.isEmpty() && (exons <= MAX_EXONS_CHART)) {
					GoogleGeneRegionNumExonsChart grcCbt = new GoogleGeneRegionNumExonsChart(cbt, name + " [ " + exons + " exons ]", exons);
					genRegCharts.add(grcCbt);
				}
			}
		}

		// Add all headers
		for (GoogleGeneRegionChart grc : genRegCharts)
			sbHead.append(grc.toStringHtmlHeader());

		// Add all bodies
		for (GoogleGeneRegionChart grc : genRegCharts)
			sbBody.append(grc.toStringHtmlBody());

		// Return all html code
		return "<head>\n" + sbHead.toString() + "\n</head>\n" + sbBody.toString();
	}

	/**
	 * Initialize
	 * @param snpEffectPredictor
	 */
	void init(SnpEffectPredictor snpEffectPredictor) {
		fileNames = new ArrayList<String>();
		names = new ArrayList<String>();
		countReadsByFile = new ArrayList<CountReads>();
		markerTypes = new MarkerTypes();
		readsByFile = new CountByType();

		if (snpEffectPredictor != null) this.snpEffectPredictor = snpEffectPredictor;
		else this.snpEffectPredictor = new SnpEffectPredictor(new Genome());
	}

	/**
	 * Show probabilities
	 *
	 * @param prob : Probabilities for each
	 *
	 * @return A string showing a tab delimited table
	 */
	public String probabilityTable(CountByType prob) {
		StringBuilder sb = new StringBuilder();

		// Create title line
		sb.append("type\tp.binomial"); // Show 'type' information in first columns
		for (int j = 0; j < countReadsByFile.size(); j++)
			sb.append("\treads." + names.get(j) + "\texpected." + names.get(j) + "\tpvalue." + names.get(j));
		sb.append("\n");

		String chrType = Chromosome.class.getSimpleName();

		// Show counts by type
		CountByType countByType = countMarkerTypes(allMarkers());
		for (String type : countByType.keysSorted()) {
			sb.append(type); // Show 'type' information in first columns

			// Binomial probability model
			double p = 0;
			if ((prob != null) && prob.contains(type)) {
				p = prob.getScore(type);
				sb.append("\t" + p);
			} else sb.append("\t\t");

			// Show counts for each file
			for (int idx = 0; idx < countReadsByFile.size(); idx++) {
				CountByType countTypesFile = countReadsByFile.get(idx).getCountTypes();

				// Stats
				int n = (int) countTypesFile.get(chrType); // Number of reads in the file
				int k = (int) countTypesFile.get(type); // Number of reads hitting this marker type

				long expected = Math.round(countTypesFile.get(chrType) * p);
				double pvalue = Binomial.get().cdfUpEq(p, k, n);

				long countType = countTypesFile.get(type);
				if ((prob != null) && prob.contains(type)) sb.append("\t" + countType + "\t" + expected + "\t" + pvalue);
				else sb.append("\t" + countType + "\t\t");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Print table to STDOUT
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Show title
		sb.append("chr\tstart\tend\ttype\tIDs");
		for (int j = 0; j < countReadsByFile.size(); j++)
			sb.append("\tReads:" + names.get(j) + "\tBases:" + names.get(j));
		sb.append("\n");

		//---
		// Show counts by marker
		//---
		// Show counts for each marker
		for (Marker key : allMarkers()) {
			// Show 'key' information in first columns
			sb.append(key.getChromosomeName() //
					+ "\t" + (key.getStart() + 1) //
					+ "\t" + (key.getEndClosed() + 1) //
					+ "\t" + key.idChain() //
			);

			// Show counts for each file
			for (int idx = 0; idx < countReadsByFile.size(); idx++)
				sb.append("\t" + countReadsByFile.get(idx).getCountReads().get(key) //
						+ "\t" + countReadsByFile.get(idx).getCountBases().get(key) //
				);
			sb.append("\n");
		}
		sb.append("\n");

		return sb.toString();
	}

}
