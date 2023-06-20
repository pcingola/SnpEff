package org.snpeff.snpEffect.commandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.snpeff.SnpEff;
import org.snpeff.coverage.CountReadsOnMarkers;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.stats.ReadsOnMarkersModel;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import htsjdk.samtools.BAMIndex;
import htsjdk.samtools.BAMIndexMetaData;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReader.Indexing;
import htsjdk.samtools.SamReaderFactory;

/**
 * Count reads from a BAM file given a list of intervals
 *
 * @author pablocingolani
 */
public class SnpEffCmdCount extends SnpEff {

	boolean calcProbModel;
	String outputBaseNames;
	CountReadsOnMarkers countReadsOnMarkers;
	SnpEffectPredictor snpEffectPredictor;
	List<String> fileNames; // Files to count (can be BAM, SAM)

	public SnpEffCmdCount() {
		fileNames = new ArrayList<String>();
	}

	/**
	 * Count all reads in a BAM file Note: It uses the BAM index
	 *
	 * @param samReader
	 * @return
	 */
	int countTotalReads(String samFileName) {
		try {
			int count = 0;
			if (verbose) Log.info("Counting reads on file: " + samFileName);
			// SAMFileReader samReader = new SAMFileReader(new File(samFileName));
			// AbstractBAMFileIndex index = (AbstractBAMFileIndex) samReader.getIndex();
			SamReader samReader = SamReaderFactory.makeDefault().open(new File(samFileName));
			int numRefs = samReader.getFileHeader().getSequenceDictionary().size();
			Indexing index = samReader.indexing();
			BAMIndex bai = index.getIndex();
			for (int i = 0; i < numRefs; i++) {
				// BAMIndexMetaData meta = index.getMetaData(i);
				BAMIndexMetaData meta = bai.getMetaData(i);
				count += meta.getAlignedRecordCount();
			}
			samReader.close();
			if (verbose) Log.info("Total " + count + " reads.");
			return count;
		} catch (Exception e) {
			// Error? (e.g. no index)
			System.err.println("ERROR! BAM file not indexed?");
			return -1;
		}
	}

	/**
	 * Parse
	 *
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		// Parse command line arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-p")) calcProbModel = true;
			else if (args[i].equals("-n")) outputBaseNames = args[++i];
			else if ((genomeVer == null) || genomeVer.isEmpty()) genomeVer = args[i];
			else fileNames.add(args[i]);
		}

		// Sanity check
		if ((genomeVer == null) || genomeVer.isEmpty()) usage("Missing genome version");
		if (fileNames.size() < 1) usage("Missing input file/s");

		for (String file : fileNames)
			if (!Gpr.canRead(file)) Log.fatalError("Cannot read input file '" + file + "'");

		for (String file : customIntervalFiles)
			if (!Gpr.canRead(file)) Log.fatalError("Cannot read custom intervals file '" + file + "'");

		if (noGenome && customIntervalFiles.isEmpty()) usage("No user defined intervals were defined (mandatory if '-noGenome' option is enabled)");
	}

	/**
	 * Calculate p-values for
	 */
	ReadsOnMarkersModel pvalues() {
		int readLength = countReadsOnMarkers.getReadLengthAvg();
		if (verbose) Log.info("Calculating probability model for read length " + readLength);

		// Cannot load from file: Create model and save it
		ReadsOnMarkersModel readsOnMarkersModel = new ReadsOnMarkersModel(snpEffectPredictor);

		// Calculate model
		readsOnMarkersModel.setReadLength(readLength);
		readsOnMarkersModel.setVerbose(verbose);
		readsOnMarkersModel.setMarkerTypes(countReadsOnMarkers.getMarkerTypes());

		// Run model
		readsOnMarkersModel.run();
		Log.info("Probability model:\n" + readsOnMarkersModel.toString());

		return readsOnMarkersModel;
	}

	/**
	 * Run
	 */
	@Override
	public boolean run() {
		// ---
		// Initialize
		// ---

		loadConfig(); // Read config file

		// Load database
		loadDb();
		snpEffectPredictor = config.getSnpEffectPredictor();

		// Build forest
		if (verbose) Log.info("Building interval forest");
		snpEffectPredictor.buildForest();
		if (verbose) Log.info("done");

		// ---
		// Count reads
		// ---

		// Initialize counter
		countReadsOnMarkers = new CountReadsOnMarkers(snpEffectPredictor);

		countReadsOnMarkers.setVerbose(verbose);
		for (String file : fileNames)
			countReadsOnMarkers.addFile(file);
		countReadsOnMarkers.count();

		// ---
		// Show & save results
		// ---
		if (!quiet) {
			// Show results : Details marker by marker counts
			if (outputBaseNames != null) {
				String detailsFile = outputBaseNames + ".txt";
				if (verbose) Log.info("Saving counts by marker to file '" + detailsFile + "'");
				Gpr.toFile(detailsFile, countReadsOnMarkers);
			} else System.out.println(countReadsOnMarkers);

			// Calculate p-values
			ReadsOnMarkersModel readsOnMarkersModel = new ReadsOnMarkersModel(snpEffectPredictor);
			if (calcProbModel) readsOnMarkersModel = pvalues();

			// Show results (summary)
			if (outputBaseNames != null) {
				String summaryFile = outputBaseNames + ".summary.txt";
				if (verbose) Log.info("Saving summary to file '" + summaryFile + "'");
				Gpr.toFile(summaryFile, "# Summary\n" + countReadsOnMarkers.probabilityTable(readsOnMarkersModel.getProb()));
			} else System.err.println("# Summary\n" + countReadsOnMarkers.probabilityTable(readsOnMarkersModel.getProb()));

			// Save HTML file
			String htmlFile = "snpeff.count.html";
			if (outputBaseNames != null) htmlFile = outputBaseNames + ".summary.html";
			if (verbose) Log.info("Saving charts to file : " + htmlFile);
			Gpr.toFile(htmlFile, countReadsOnMarkers.html());
		}

		return true;
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff count [options] genome file_1 file_2 ...  file_N");
		System.err.println("\t-n name          : Output file base name. ");
		System.err.println("\t-p               : Calculate probability model (binomial). Default: " + calcProbModel);
		System.err.println("\tfile             : A file contianing intervals or reads. Either BAM, SAM, VCF, BED or BigBed format.");

		usageGenericAndDb();

		System.exit(-1);
	}
}
