package ca.mcgill.mcb.pcingola.diffChipSeq;

import java.util.HashMap;
import java.util.List;

import ca.mcgill.mcb.pcingola.Pcingola;
import ca.mcgill.mcb.pcingola.coverage.CountReadsOnMarkers;
import ca.mcgill.mcb.pcingola.fileIterator.BedFileIterator;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.CommandLine;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Analysis of differential Chip-Seq experiments
 * 
 * Input data:
 * 		- Chip-Seq experiment 1: BAM file (aligned reads)
 * 		- Chip-Seq experiment 1: BED file (peaks)
 * 
 * 		- Chip-Seq experiment 2: BAM file (aligned reads)
 * 		- Chip-Seq experiment 3: BED file (peaks)
 * 
 * Output: A set of peaks that are different in 'experiment 1' than 'experiment 2'.
 * 
 * Note: We are looking for statistically significance, so just 
 * 		comparing the intervals in BED files is not enough! 
 * 
 * @author pcingola
 */
public class DiffChipSeq implements CommandLine {

	public static int SHOW_EVERY = 10000;
	public static boolean debug = true;

	public static final String SOFTWARE_NAME = "DiffChipSeq";
	public static final String BUILD = "2012-12-12";
	public static final String VERSION_MAJOR = "0.1";
	public static final String REVISION = "epsilon";
	public static final String VERSION_SHORT = VERSION_MAJOR + REVISION;
	public static final String VERSION = SOFTWARE_NAME + " " + VERSION_SHORT + " (build " + BUILD + "), by " + Pcingola.BY;

	boolean verbose = true; // Be verbose
	String genomeVer;
	String bamFile1, bamInputFile1, bamFile2, bamInputFile2;
	String peaksFile1, peaksFile2;
	Genome genome;
	Config config;
	SnpEffectPredictor snpEffectPredictor;
	CountReadsOnMarkers countReadsOnMarkers;

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		DiffChipSeq diffChipSeq = new DiffChipSeq();
		diffChipSeq.parseArgs(args);
		diffChipSeq.run();
	}

	DiffChipSeq() {
	}

	/**
	 * Annotate peaks with closes gene
	 * @param peaks
	 */
	void annotatePeaks(Markers peaks) {
		if (verbose) Timer.showStdErr("Annotating peaks");
		for (Marker m : peaks) {
			Gene gene = snpEffectPredictor.queryClosestGene(m);
			if (gene != null) m.setId(gene.getGeneName() + ";" + gene.getId() + ";" + m.distance(gene));
		}
	}

	/**
	 * Collapse peaks: Grow until there are no more hits in interval forest
	 * @param peak
	 * @param forest
	 * @return
	 */
	Marker collapsePeak(Marker peak, IntervalForest forest) {
		Markers result = forest.query(peak);
		if (result.size() <= 1) return peak; // Only one hit (the same peak) so nothing to do

		// Get larger interval
		int start = peak.getStart();
		int end = peak.getEnd();
		for (Marker m : result) {
			start = Math.min(start, m.getStart());
			end = Math.min(end, m.getEnd());
		}

		// No change? => we are done
		if ((peak.getStart() <= start) || (end <= peak.getEnd())) return peak;

		// Create a larger interval and recurse
		Marker newPeak = new Marker(peak.getParent(), start, end, 1, "");
		return collapsePeak(newPeak, forest);
	}

	/**
	 * Collapse peaks
	 * @param peaks1
	 * @param peaks2
	 */
	Markers collapsePeaks(List<SeqChange> peaks1, List<SeqChange> peaks2) {
		if (verbose) Timer.showStdErr("Collapse peaks: Building interval forest");
		IntervalForest forest = new IntervalForest();
		forest.add(peaks1);
		forest.add(peaks2);
		forest.build();

		// Collapse all peaks
		if (verbose) Timer.showStdErr("Collapse peaks: Collapsing");
		HashMap<String, Marker> result = new HashMap<String, Marker>();
		for (SeqChange peak : peaks1) {
			Marker newPeak = new Marker(peak.getParent(), peak.getStart(), peak.getEnd(), 1, "");
			newPeak = collapsePeak(newPeak, forest);
			String key = newPeak.getChromosomeName() + ":" + newPeak.getStart() + "-" + newPeak.getEnd();
			result.put(key, newPeak); // Use a hash in order to avoid double insertions
		}

		if (verbose) Timer.showStdErr("Collapse peaks: done. Number of peaks before collapsing: " + (peaks1.size() + peaks2.size()) + ", after collapsing: " + result.size());

		// Create a sorted list
		Markers peaks = new Markers(result.values());
		return peaks.sort();
	}

	/**
	 * Load data
	 */
	void load() {
		if (verbose) Timer.showStdErr("Loading config: " + Config.DEFAULT_CONFIG_FILE);
		config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
		genome = config.getGenome();

		if (verbose) Timer.showStdErr("Loading database: " + config.getFileSnpEffectPredictor());
		snpEffectPredictor = config.loadSnpEffectPredictor();

		if (verbose) Timer.showStdErr("Building forest");
		snpEffectPredictor.buildForest();
	}

	@Override
	public void parseArgs(String[] args) {
		if (args.length != 7) usage(null);

		int i = 0;
		genomeVer = args[i++];
		bamFile1 = args[i++];
		bamInputFile1 = args[i++];
		peaksFile1 = args[i++];
		bamFile2 = args[i++];
		bamInputFile2 = args[i++];
		peaksFile2 = args[i++];
	}

	@Override
	public boolean run() {
		load();

		// Read BED files and collapse intervals into one set
		if (verbose) Timer.showStdErr("Reading peaks from file '" + peaksFile1 + "'");
		BedFileIterator bedFile1 = new BedFileIterator(peaksFile1);
		List<SeqChange> peaks1 = bedFile1.load();
		if (verbose) Timer.showStdErr("done. Number of peaks : " + peaks1.size());

		if (verbose) Timer.showStdErr("Reading peaks from file '" + peaksFile2 + "'");
		BedFileIterator bedFile2 = new BedFileIterator(peaksFile2);
		List<SeqChange> peaks2 = bedFile2.load();
		if (verbose) Timer.showStdErr("done. Number of peaks : " + peaks2.size());

		// Collapse peaks
		Markers peaks = collapsePeaks(peaks1, peaks2);

		// Annotate reads
		annotatePeaks(peaks);

		// Build predictor
		if (verbose) Timer.showStdErr("Building interval forest");
		snpEffectPredictor = new SnpEffectPredictor(genome);
		snpEffectPredictor.addAll(peaks);
		snpEffectPredictor.buildForest();
		if (verbose) Timer.showStdErr("done. Size: " + snpEffectPredictor.size());

		// Count reads on each interval
		config.setErrorChromoHit(false);
		config.setErrorOnMissingChromo(false);
		if (verbose) Timer.showStdErr("Counting reads");
		countReadsOnMarkers = new CountReadsOnMarkers(snpEffectPredictor);
		countReadsOnMarkers.setVerbose(verbose);
		countReadsOnMarkers.addFile(bamFile1);
		countReadsOnMarkers.addFile(bamInputFile1);
		countReadsOnMarkers.addFile(bamFile2);
		countReadsOnMarkers.addFile(bamInputFile2);
		countReadsOnMarkers.count();
		System.out.println(countReadsOnMarkers);

		return false;
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("DiffChipSeq version " + VERSION);
		System.err.println("Usage: DiffChipSeq genome reads_control.bam reads_control_input.bam peaks_control.bed reads_case.bam reads_case_input.bam peaks_case.bed");
		System.exit(-1);
	}

}
