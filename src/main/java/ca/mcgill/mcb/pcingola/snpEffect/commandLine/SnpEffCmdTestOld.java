package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import ca.mcgill.mcb.pcingola.collections.AutoHashMap;
import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.probablility.FisherExactTest;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Command line: Test
 * 
 * Note: Used for testing weird stuff
 * 
 * @author pcingola
 */
public class SnpEffCmdTestOld extends SnpEff {

	public static final double P_VALUE_LIMIT = 0.5;

	SnpEffectPredictorLoader sepLoader;
	SnpEffectPredictor snpEffectPredictor;
	String inputFile;
	double maxPvalue = 1.0;

	public SnpEffCmdTestOld() {
		super();
		sepLoader = new SnpEffectPredictorLoader();
	}

	/**
	 * Create a key for this marker
	 * @param r
	 * @return
	 */
	void inc(AutoHashMap<String, CountByType> coutnByType, Marker r) {
		String id;

		if (r instanceof Motif) {
			Motif motif = (Motif) r;
			if (debug) System.out.println("\t" + motif.toString() + "\t" + motif.getPwmId() + "\t" + motif.getPwmName());
			id = motif.getPwmId();
		} else if (r instanceof Custom) {
			if (debug) Gpr.debug("Found custom interval: " + r.getId());
			id = r.getId();
		} else return;

		String type = r.getClass().getSimpleName();
		coutnByType.getOrCreate(type).inc(id);
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args.length < 2) usage(null);

		// Parse command line args in SnpEffectPredictorLoader
		sepLoader.setVerbose(verbose);
		sepLoader.setDebug(debug);
		sepLoader.setQuiet(quiet);

		// Parse all other command line options
		for (int idx = 0; idx < args.length; idx++) {
			String arg = args[idx];

			if (isOpt(arg)) {
				int newIdx = sepLoader.parseArg(args, idx); // Try to parse it in SnpEffectPredictorLoader

				if (newIdx >= 0) idx = newIdx; // Parse in SnpEffectPredictorLoader? 
				else if (arg.equalsIgnoreCase("-maxP")) {
					maxPvalue = Gpr.parseDoubleSafe(args[++idx]);
				} else usage("Unknown opton '" + arg + "'");
			} else if ((genomeVer == null) || genomeVer.isEmpty()) genomeVer = arg;
			else if (inputFile == null) inputFile = arg;
		}

		// Sanity check
		if (inputFile == null) usage("Missing input file");
		sepLoader.setGenomeVer(genomeVer);
	}

	/**
	 * Print a counter using a label on each line
	 * @param label
	 * @param countByType
	 */
	void print(String label, CountByType countByType) {
		System.out.println(label + "\teff\tcount");
		for (String type : countByType.keysSorted())
			System.out.println(label + "\t" + type + "\t" + countByType.get(type));

	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		//---
		// Initialize
		//---
		genomeVer = sepLoader.getGenomeVer();
		readConfig(); // Read config file

		sepLoader.load(config); // Load database, build forest

		//---
		// Load intervals
		//---
		if (verbose) Timer.showStdErr("Loading intervals from '" + inputFile + "'");
		Markers intervals = Markers.readMarkers(inputFile);
		if (verbose) Timer.showStdErr("Done. Intervals added : " + intervals.size());

		//---
		// Process input file
		//---
		sepLoader.build();
		run(intervals);

		if (verbose) Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Intersect 'intervals'
	 * @param intervals
	 */
	void run(Markers intervals) {
		if (verbose) Timer.showStdErr("Runnig");
		SnpEffectPredictor sep = config.getSnpEffectPredictor();

		//---
		// Count for each motif (or custom marker
		//---
		AutoHashMap<String, CountByType> countByTypeId = new AutoHashMap<String, CountByType>(new CountByType());
		for (Marker m : intervals) {
			if (debug) System.out.println(m);

			Markers results = sep.query(m);
			for (Marker r : results)
				inc(countByTypeId, r);
		}

		//---
		// Show results
		//---

		// Count totals by marker
		AutoHashMap<String, CountByType> totalByTypeId = new AutoHashMap<String, CountByType>(new CountByType());
		for (Marker m : sep.getMarkers())
			inc(totalByTypeId, m);

		// Count motifs in intervals
		for (String type : totalByTypeId.keySet()) {
			System.out.println("Type : " + type);
			CountByType countById = countByTypeId.get(type);
			CountByType totalById = totalByTypeId.get(type);

			if (countById == null || totalById == null) continue; // Information missing? (may be no motif was hit? or input file was empty?)

			long totalHits = countById.sum();
			long total = totalById.sum();
			System.out.println("\tNumber of motifs  : " + totalById.keySet().size());
			System.out.println("\tTotal motif sites : " + total);
			System.out.println("\tTotal intervals   : " + intervals.size());
			System.out.println("\tTotal hits        : " + totalHits);

			// Calculate p-values and show each motif
			System.out.println("\n\tid\tcount.hits\tcount.motif.sites\tpvalue");
			for (String id : countById.keysSorted()) {
				long count = countById.get(id);

				int k = (int) count;
				int N = (int) total;
				int D = (int) totalById.get(id);
				int n = (int) totalHits;
				double pvalue = FisherExactTest.get().pValueUp(k, N, D, n, P_VALUE_LIMIT);

				if (pvalue <= maxPvalue) System.out.println("\t" + id + "\t" + count + "\t" + totalById.get(id) + "\t" + pvalue);
			}
		}
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff test [options] genomeVer inputFile");
		System.err.println("Where : ");
		System.err.println("\tinputFile : Input intervals file. Can be BED, TXT, BigBed or VCF");
		System.err.println("Options : ");
		System.err.println("\t-maxP <num>  : Filter out results with p-valie over 'num'. Default : " + maxPvalue);
		sepLoader.usage(null); // Show SnpEffect predictor loader options

		System.exit(-1);
	}

}
