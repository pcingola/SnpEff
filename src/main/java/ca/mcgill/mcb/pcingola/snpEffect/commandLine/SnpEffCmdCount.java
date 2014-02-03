package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.AbstractBAMFileIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileReader;
import ca.mcgill.mcb.pcingola.coverage.CountReadsOnMarkers;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.stats.ReadsOnMarkersModel;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Count reads from a BAM file given a list of intervals
 * 
 * @author pablocingolani
 */
public class SnpEffCmdCount extends SnpEff {

	boolean noGenome;
	boolean calcProbModel;
	boolean canonical = false; // Use only canonical transcripts
	String outputBaseNames;
	CountReadsOnMarkers countReadsOnMarkers;
	SnpEffectPredictor snpEffectPredictor;
	List<String> fileNames; // Files to count (can be BAM, SAM) 
	List<String> customIntervals; // Custom intervals

	public SnpEffCmdCount() {
		fileNames = new ArrayList<String>();
		customIntervals = new ArrayList<String>();
	}

	/**
	 * Count all reads in a BAM file 
	 * Note: It uses the BAM index
	 * 
	 * @param samReader
	 * @return
	 */
	int countTotalReads(String samFileName) {
		try {
			if (verbose) Timer.showStdErr("Counting reads on file: " + samFileName);
			SAMFileReader samReader = new SAMFileReader(new File(samFileName));
			AbstractBAMFileIndex index = (AbstractBAMFileIndex) samReader.getIndex();
			int count = 0;
			for (int i = 0; i < index.getNumberOfReferences(); i++) {
				BAMIndexMetaData meta = index.getMetaData(i);
				count += meta.getAlignedRecordCount();
			}
			samReader.close();
			if (verbose) Timer.showStdErr("Total " + count + " reads.");
			return count;
		} catch (Exception e) {
			// Error? (e.g. no index)
			System.err.println("ERROR! BAM file not indexed?");
			return -1;
		}
	}

	/**
	 * Parse
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		// Parse command line arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-i")) customIntervals.add(args[++i]);
			else if (args[i].equals("-p")) calcProbModel = true;
			else if (args[i].equals("-n")) outputBaseNames = args[++i];
			else if (args[i].equalsIgnoreCase("-nogenome")) noGenome = true;
			else if (args[i].equalsIgnoreCase("-canon")) canonical = true;
			else if ((genomeVer == null) || genomeVer.isEmpty()) genomeVer = args[i];
			else fileNames.add(args[i]);
		}

		// Sanity check
		if ((genomeVer == null) || genomeVer.isEmpty()) usage("Missing genome version");
		if (fileNames.size() < 1) usage("Missing input file/s");

		for (String file : fileNames)
			if (!Gpr.canRead(file)) fatalError("Cannot read input file '" + file + "'");

		for (String file : customIntervals)
			if (!Gpr.canRead(file)) fatalError("Cannot read custom intervals file '" + file + "'");

		if (noGenome && customIntervals.isEmpty()) usage("No user defined intervals were defined (mandatory if '-noGenome' option is enabled)");
	}

	/**
	 * Calculate p-values for 
	 */
	ReadsOnMarkersModel pvalues() {
		int readLength = countReadsOnMarkers.getReadLengthAvg();
		if (verbose) Timer.showStdErr("Calculating probability model for read length " + readLength);

		// Cannot load from file: Create model and save it
		ReadsOnMarkersModel readsOnMarkersModel = new ReadsOnMarkersModel(snpEffectPredictor);

		// Calculate model
		readsOnMarkersModel.setReadLength(readLength);
		readsOnMarkersModel.setVerbose(verbose);
		readsOnMarkersModel.setMarkerTypes(countReadsOnMarkers.getMarkerTypes());

		// Run model
		readsOnMarkersModel.run();
		Timer.showStdErr("Probability model:\n" + readsOnMarkersModel.toString());

		return readsOnMarkersModel;
	}

	/**
	 * Run
	 */
	@Override
	public boolean run() {
		//---
		// Initialize
		//---

		// Load Config
		readConfig(); // Read config file

		// Load database
		if (noGenome) {
			if (verbose) Timer.showStdErr("Creating empty database (no genome).");
			snpEffectPredictor = new SnpEffectPredictor(new Genome());
			config.setSnpEffectPredictor(snpEffectPredictor);
			config.setErrorChromoHit(false); // We don't have chromosomes, so we de-activate this error.
		} else {
			if (verbose) Timer.showStdErr("Reading database for genome '" + genomeVer + "'");
			config.loadSnpEffectPredictor(); // Read snpEffect predictor
			snpEffectPredictor = config.getSnpEffectPredictor(); // Read snpEffect predictor
			if (verbose) Timer.showStdErr("done");
		}
		countReadsOnMarkers = new CountReadsOnMarkers(snpEffectPredictor);

		// Load custom interval files
		for (String markersFile : customIntervals) {
			// Load file
			if (verbose) Timer.showStdErr("Reading intervals from file '" + markersFile + "'");
			String baseName = Gpr.removeExt(Gpr.baseName(markersFile));
			Markers markers = readMarkers(markersFile);

			// Set marker type
			for (Marker marker : markers) {
				marker.setId(baseName + ":" + marker.getId());
				snpEffectPredictor.add(marker);
				countReadsOnMarkers.addMarkerType(marker, baseName);
			}
			if (verbose) Timer.showStdErr("Done. Intervals added : " + markers.size());
		}

		// Build forest
		if (verbose) Timer.showStdErr("Building interval forest");
		snpEffectPredictor.buildForest();
		if (verbose) Timer.showStdErr("done");

		//---
		// Count reads
		//---

		countReadsOnMarkers.setVerbose(verbose);
		for (String file : fileNames)
			countReadsOnMarkers.addFile(file);
		countReadsOnMarkers.count();

		//---
		// Show & save results
		//---
		if (!quiet) {
			// Show results : Details marker by marker counts
			if (outputBaseNames != null) {
				String detailsFile = outputBaseNames + ".txt";
				if (verbose) Timer.showStdErr("Saving counts by marker to file '" + detailsFile + "'");
				Gpr.toFile(detailsFile, countReadsOnMarkers);
			} else System.out.println(countReadsOnMarkers);

			// Calculate p-values
			ReadsOnMarkersModel readsOnMarkersModel = new ReadsOnMarkersModel(snpEffectPredictor);
			if (calcProbModel) readsOnMarkersModel = pvalues();

			// Show results (summary)
			if (outputBaseNames != null) {
				String summaryFile = outputBaseNames + ".summary.txt";
				if (verbose) Timer.showStdErr("Saving summary to file '" + summaryFile + "'");
				Gpr.toFile(summaryFile, "# Summary\n" + countReadsOnMarkers.probabilityTable(readsOnMarkersModel.getProb()));
			} else System.err.println("# Summary\n" + countReadsOnMarkers.probabilityTable(readsOnMarkersModel.getProb()));

			// Save HTML file
			String htmlFile = "snpeff.count.html";
			if (outputBaseNames != null) htmlFile = outputBaseNames + ".summary.html";
			if (verbose) Timer.showStdErr("Saving charts to file : " + htmlFile);
			Gpr.toFile(htmlFile, countReadsOnMarkers.html());
		}

		return true;
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff count [options] genome file_1 file_2 ...  file_N");
		System.err.println("\t-i intervals.bed : User defined intervals. Mutiple '-i' commands are allowed.");
		System.err.println("\t-n name          : Output file base name. ");
		System.err.println("\t-noGenome        : Do not count genomic intervals, only user provided intervals are used.");
		System.err.println("\t-canon           : Use only canonical transcripts.");
		System.err.println("\t-p               : Calculate probability model (binomial). Default: " + calcProbModel);
		System.err.println("\tfile             : A file contianing intervals or reads. Either BAM, SAM, VCF, BED or BigBed format.");
		System.exit(-1);
	}
}
