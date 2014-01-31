package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.motif.Jaspar;
import ca.mcgill.mcb.pcingola.motif.Pwm;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * A helper class that interprets command line options, then 
 * loads the SnpEffect predictor and builds the interval 
 * forest.
 * 
 * @author Pablo Cingolani
 */
public class SnpEffectPredictorLoader {

	protected boolean verbose; // Be verbose
	protected boolean debug; // Debug mode
	protected boolean quiet; // Be quiet

	boolean canonical = false; // Use only canonical transcripts
	boolean onlyRegulation = false; // Only build regulation tracks
	boolean nextProt = false; // Annotate using NextProt database
	boolean motif = false; // Annotate using motifs
	Boolean treatAllAsProteinCoding = null; // Only use coding genes. Default is 'null' which means 'auto'
	int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
	int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE; // Splice site size default: 2 bases (canonical splice site)
	String genomeVer;
	String onlyTranscriptsFile = null; // Only use the transcripts in this file (Format: One transcript ID per line)
	ArrayList<String> customIntervalFiles; // Custom interval files (bed)
	HashSet<String> regulationTracks = new HashSet<String>(); // Regulation tracks to load
	Config config;

	//	ArrayList<String> remainingArgs;

	public SnpEffectPredictorLoader() {
		customIntervalFiles = new ArrayList<String>();
		regulationTracks = new HashSet<String>();
	}

	/**
	 * Build interval forest
	 */
	public void build() {
		// Build tree
		if (verbose) Timer.showStdErr("Building interval forest");
		config.getSnpEffectPredictor().buildForest();
		if (verbose) Timer.showStdErr("done.");
	}

	/**
	 * Show an error (if not 'quiet' mode)
	 * @param message
	 */
	public void error(Throwable e, String message) {
		if (verbose && (e != null)) e.printStackTrace();
		if (!quiet) System.err.println(message);
	}

	/**
	 * Show an error message and exit
	 * @param message
	 */
	public void fatalError(String message) {
		System.err.println(message);
		System.exit(-1);
	}

	public Config getConfig() {
		return config;
	}

	public ArrayList<String> getCustomIntervalFiles() {
		return customIntervalFiles;
	}

	public String getGenomeVer() {
		return genomeVer;
	}

	public String getOnlyTranscriptsFile() {
		return onlyTranscriptsFile;
	}

	public HashSet<String> getRegulationTracks() {
		return regulationTracks;
	}

	//	public String[] getRemainingArgs() {
	//		return remainingArgs.toArray(new String[0]);
	//	}
	//
	public int getSpliceSiteSize() {
		return spliceSiteSize;
	}

	public Boolean getTreatAllAsProteinCoding() {
		return treatAllAsProteinCoding;
	}

	public int getUpDownStreamLength() {
		return upDownStreamLength;
	}

	public boolean isCanonical() {
		return canonical;
	}

	public boolean isMotif() {
		return motif;
	}

	public boolean isNextProt() {
		return nextProt;
	}

	public boolean isOnlyRegulation() {
		return onlyRegulation;
	}

	/**
	 * Is this a command line option (e.g. "-tfam" is a command line option, but "-" means STDIN)
	 * @param arg
	 * @return
	 */
	protected boolean isOpt(String arg) {
		return arg.startsWith("-") && (arg.length() > 1);
	}

	/**
	 * Run according to command line options
	 */
	public void load(Config config) {
		this.config = config;

		//---
		// Prepare to run
		//---

		// Read database (or create a new one)
		if (onlyRegulation) {
			// Create predictor
			config.setSnpEffectPredictor(new SnpEffectPredictor(config.getGenome()));
			config.setOnlyRegulation(true);
			config.setErrorOnMissingChromo(false); // A chromosome might be missing (e.g. no regulation tracks available for 'MT')
			config.setErrorChromoHit(false); // A chromosome's length might be smaller than the real (it's calculated using regulation features, not real chromo data)
		} else {
			// Read
			if (verbose) Timer.showStdErr("Reading database for genome version '" + genomeVer + "' from file '" + config.getFileSnpEffectPredictor() + "' (this might take a while)");
			config.loadSnpEffectPredictor(); // Read snpEffect predictor
			if (verbose) Timer.showStdErr("done");
		}

		// Set 'treatAllAsProteinCoding'
		if (treatAllAsProteinCoding != null) config.setTreatAllAsProteinCoding(treatAllAsProteinCoding);
		else {
			// treatAllAsProteinCoding was set to 'auto'
			// I.e.: Use 'true' if there is protein coding info, otherwise use false.
			boolean tapc = !config.getGenome().hasCodingInfo();
			if (debug) Timer.showStdErr("Setting '-treatAllAsProteinCoding' to '" + tapc + "'");
			config.setTreatAllAsProteinCoding(tapc);
		}

		// Read custom interval files
		for (String intFile : customIntervalFiles) {
			if (verbose) Timer.showStdErr("Reading interval file '" + intFile + "'");
			int count = readCustomIntFile(intFile);
			if (verbose) Timer.showStdErr("done (" + count + " intervals loaded). ");
		}

		// Read regulation tracks
		for (String regTrack : regulationTracks)
			readRegulationTrack(regTrack);

		// Set upstream-downstream interval length
		config.getSnpEffectPredictor().setUpDownStreamLength(upDownStreamLength);

		// Set splice site size
		config.getSnpEffectPredictor().setSpliceSiteSize(spliceSiteSize);

		// Filter canonical transcripts
		if (canonical) {
			if (verbose) Timer.showStdErr("Filtering out non-canonical transcripts.");
			config.getSnpEffectPredictor().removeNonCanonical();

			if (debug) {
				// Show genes and transcript (which ones are considered 'canonical')
				Timer.showStdErr("Canonical transcripts:\n\t\tgeneName\tgeneId\ttranscriptId\tcdsLength");
				for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
					for (Transcript t : g) {
						String cds = t.cds();
						int cdsLen = (cds != null ? cds.length() : 0);
						System.err.println("\t\t" + g.getGeneName() + "\t" + g.getId() + "\t" + t.getId() + "\t" + cdsLen);
					}
				}
			}
			if (verbose) Timer.showStdErr("done.");
		}

		// Use transcripts set form input file
		if (onlyTranscriptsFile != null) {
			// Load file
			String onlyTr = Gpr.readFile(onlyTranscriptsFile);
			HashSet<String> trIds = new HashSet<String>();
			for (String trId : onlyTr.split("\n"))
				trIds.add(trId.trim());

			// Remove transcripts
			if (verbose) Timer.showStdErr("Filtering out transcripts in file '" + onlyTranscriptsFile + "'. Total " + trIds.size() + " transcript IDs.");
			int removed = config.getSnpEffectPredictor().retainAllTranscripts(trIds);
			if (verbose) Timer.showStdErr("Done: " + removed + " transcripts removed.");
		}

		// Read nextProt database?
		if (nextProt) readNextProt();
		if (motif) readMotif();

		// Build tree
		if (verbose) Timer.showStdErr("Building interval forest");
		config.getSnpEffectPredictor().buildForest();
		if (verbose) Timer.showStdErr("done.");

		// Show some genome stats. Chromosome names are shown, a lot of people has problems with the correct chromosome names.
		if (verbose) {
			Timer.showStdErr("Genome stats :");
			System.err.println(config.getGenome());
		}
	}

	/**
	 * Parse command line arguments relevant to SnpEffPredictor 
	 * loading options
	 * 
	 * @param args
	 * @returns All command line options that remain (i.e. the ones that were not related to loading or building the predictor)
	 */
	public int parseArg(String[] args, int i) {
		String arg = args[i];

		// Is it a command line option?
		if (isOpt(arg)) {
			//---
			// Annotation options
			//---
			if (arg.equalsIgnoreCase("-canon")) canonical = true; // Use canonical transcripts
			else if (arg.equalsIgnoreCase("-onlyTr")) {
				if ((i + 1) < args.length) onlyTranscriptsFile = args[++i]; // Only use the transcripts in this file
			} else if (arg.equalsIgnoreCase("-treatAllAsProteinCoding")) {
				if ((i + 1) < args.length) {
					i++;
					if (args[i].equalsIgnoreCase("auto")) treatAllAsProteinCoding = null;
					else treatAllAsProteinCoding = Gpr.parseBoolSafe(args[i]);
				}
			}
			//---
			// Input options
			//---
			else if (arg.equalsIgnoreCase("-interval")) {
				if ((i + 1) < args.length) customIntervalFiles.add(args[++i]);
				else usage("Option '-interval' without config interval_file argument");
			}
			//---
			// Regulation options
			//---
			else if (arg.equals("-onlyReg")) onlyRegulation = true;
			else if (arg.equals("-reg")) {
				if ((i + 1) < args.length) regulationTracks.add(args[++i]); // Add this track to the list
			}
			//---
			// NextProt database
			//---
			else if (arg.equalsIgnoreCase("-nextProt")) nextProt = true; // Use NextProt database
			else if (arg.equalsIgnoreCase("-motif")) motif = true; // Use motif database
			else return -1;
		} else return -1;

		return i;
	}

	/**
	 * Read a custom interval file
	 * @param intFile
	 */
	protected int readCustomIntFile(String intFile) {
		Markers markers = readMarkers(intFile);

		// Add all markers to predictor
		for (Marker m : markers)
			config.getSnpEffectPredictor().add(m);

		// Number added
		return markers.size();
	}

	//	/**
	//	 * Parse command line arguments relevant to SnpEffPredictor 
	//	 * loading options
	//	 * 
	//	 * @param args
	//	 * @returns All command line options that remain (i.e. the ones that were not related to loading or building the predictor)
	//	 */
	//	public String[] parseArgs(String[] args) {
	//		remainingArgs = new ArrayList<String>();
	//
	//		for (int i = 0; i < args.length; i++) {
	//
	//			String arg = args[i];
	//
	//			// Is it a command line option?
	//			if (isOpt(arg)) {
	//
	//				//---
	//				// Annotation options
	//				//---
	//				if (arg.equalsIgnoreCase("-canon")) canonical = true; // Use canonical transcripts
	//				else if (arg.equalsIgnoreCase("-onlyTr")) {
	//					if ((i + 1) < args.length) onlyTranscriptsFile = args[++i]; // Only use the transcripts in this file
	//				} else if (arg.equalsIgnoreCase("-treatAllAsProteinCoding")) {
	//					if ((i + 1) < args.length) {
	//						i++;
	//						if (args[i].equalsIgnoreCase("auto")) treatAllAsProteinCoding = null;
	//						else treatAllAsProteinCoding = Gpr.parseBoolSafe(args[i]);
	//					}
	//				}
	//				//---
	//				// Input options
	//				//---
	//				else if (arg.equalsIgnoreCase("-interval")) {
	//					if ((i + 1) < args.length) customIntervalFiles.add(args[++i]);
	//					else usage("Option '-interval' without config interval_file argument");
	//				}
	//				//---
	//				// Regulation options
	//				//---
	//				else if (arg.equals("-onlyReg")) onlyRegulation = true;
	//				else if (arg.equals("-reg")) {
	//					if ((i + 1) < args.length) regulationTracks.add(args[++i]); // Add this track to the list
	//				}
	//				//---
	//				// NextProt database
	//				//---
	//				else if (arg.equalsIgnoreCase("-nextProt")) nextProt = true; // Use NextProt database
	//				else if (arg.equalsIgnoreCase("-motif")) motif = true; // Use motif database
	//				//---
	//				// Argument not used? Add to 'remainingArgs'
	//				//---
	//				else remainingArgs.add(arg);
	//			} else if (genomeVer == null) genomeVer = arg;
	//			else remainingArgs.add(arg); // Argument not used? Add to 'remainingArgs'
	//		}
	//
	//		return getRemainingArgs();
	//	}

	/**
	 * Read a file after checking for some common error conditions
	 * @param fileName
	 * @return
	 */
	String readFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) fatalError("No such file '" + fileName + "'");
		if (!file.canRead()) fatalError("Cannot open file '" + fileName + "'");
		return Gpr.readFile(fileName);
	}

	/**
	 * Read markers file
	 * 
	 * Supported formats: BED, TXT, BigBed
	 * 
	 * @param fileName
	 * @return
	 */
	protected Markers readMarkers(String fileName) {
		Markers markersSeqChange = Markers.readMarkers(fileName);
		String label = Gpr.removeExt(Gpr.baseName(fileName));

		// Convert 'SeqChange' markers to 'Custom' markers
		Markers markers = new Markers();
		for (Marker m : markersSeqChange) {
			Custom custom = new Custom(m.getParent(), m.getStart(), m.getEnd(), m.getStrand(), m.getId(), label);
			custom.setScore(((SeqChange) m).getScore());
			markers.add(custom);
		}

		// Number added
		return markers;
	}

	/**
	 * Read regulation motif files
	 */
	void readMotif() {
		if (verbose) Timer.showStdErr("Loading Motifs and PWMs");

		//---
		// Sanity checks
		//---
		String pwmsFileName = config.getDirDataVersion() + "/pwms.bin";
		if (!Gpr.exists(pwmsFileName)) fatalError("Warning: Cannot open PWMs file " + pwmsFileName);

		String motifBinFileName = config.getBaseFileNameMotif() + ".bin";
		if (!Gpr.exists(motifBinFileName)) fatalError("Warning: Cannot open Motifs file " + motifBinFileName);

		//---
		// Load all PWMs
		//---
		if (verbose) Timer.showStdErr("\tLoading PWMs from : " + pwmsFileName);
		Jaspar jaspar = new Jaspar();
		jaspar.load(pwmsFileName);

		//---
		// Read motifs
		//---		
		if (verbose) Timer.showStdErr("\tLoading Motifs from file '" + motifBinFileName + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer();
		Markers motifsDb = markerSerializer.load(motifBinFileName);

		// Add (only) motif markers. The original motifs has to be serialized with Chromosomes, Genomes and other markers (otherwise it could have not been saved)
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		int countAddded = 0;
		for (Marker m : motifsDb)
			if (m instanceof Motif) {
				Motif motif = (Motif) m;

				// Connect motifs to their respective PWMs
				Pwm pwm = jaspar.getPwm(motif.getPwmId());
				if (pwm != null) {
					// Set PWM and add to snpEffPredictor
					motif.setPwm(pwm);
					snpEffectPredictor.add(motif);
					countAddded++;
				} else Timer.showStdErr("Cannot find PWM for motif '" + motif.getId() + "'");
			}

		if (verbose) Timer.showStdErr("\tMotif database: " + countAddded + " markers loaded.");
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 * @param regTrack
	 */
	void readNextProt() {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		//---
		// Read nextProt binary file
		//---
		String nextProtBinFile = config.getDirDataVersion() + "/nextProt.bin";
		if (verbose) Timer.showStdErr("Reading NextProt database from file '" + nextProtBinFile + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer();
		Markers nextProtDb = markerSerializer.load(nextProtBinFile);

		// Create a collection of (only) NextProt markers. The original nextProtDb has Chromosomes, Genomes and other markers (otherwise it could have not been saved)
		ArrayList<NextProt> nextProts = new ArrayList<NextProt>(nextProtDb.size());
		for (Marker m : nextProtDb)
			if (m instanceof NextProt) nextProts.add((NextProt) m);

		if (verbose) Timer.showStdErr("NextProt database: " + nextProts.size() + " markers loaded.");

		//---
		// Connect nextProt annotations to transcripts and exons 
		//---
		if (verbose) Timer.showStdErr("Adding transcript info to NextProt markers.");

		// Create a list of all transcripts
		HashMap<String, Transcript> trs = new HashMap<String, Transcript>();
		for (Gene g : snpEffectPredictor.getGenome().getGenes())
			for (Transcript tr : g)
				trs.put(tr.getId(), tr);

		// Find the corresponding transcript for each nextProt marker 
		// WARNING: The transcripts might be filtered out by the user (e.g. '-cannon' command line option or user defined sets). 
		//          We only keep nextProt markers associated to found transcripts. All others are discarded (the user doesn't want that info).
		ArrayList<NextProt> nextProtsToAdd = new ArrayList<NextProt>();
		for (NextProt np : nextProts) {
			Transcript tr = trs.get(np.getTranscriptId());

			// Found transcript, now try to find an exon
			if (tr != null) {
				boolean assignedToExon = false;
				for (Exon ex : tr) {
					if (ex.intersects(np)) {
						NextProt npEx = (NextProt) np.clone(); // The nextProt marker might cover more than one Exon 
						npEx.setParent(ex);
						nextProtsToAdd.add(npEx);
						assignedToExon = true;
					}
				}

				// Not assigned to an exon? Add transcript info
				if (!assignedToExon) {
					np.setParent(tr); // Set this transcript as parent
					nextProtsToAdd.add(np);
				}
			}
		}

		//---
		// Add all nextProt marker to predictor
		//---
		for (NextProt np : nextProtsToAdd)
			snpEffectPredictor.add(np);

		// Note: We might end up with more markers than we loaded (just because they map to multiple exons (although it would be highly unusual)
		if (verbose) Timer.showStdErr("NextProt database: " + nextProtsToAdd.size() + " markers added.");
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 * @param regTrack
	 */
	@SuppressWarnings("unchecked")
	void readRegulationTrack(String regTrack) {
		//---
		// Read file
		//---
		if (verbose) Timer.showStdErr("Reading regulation track '" + regTrack + "'");
		String regFile = config.getDirDataVersion() + "/regulation_" + regTrack + ".bin";
		ArrayList<Regulation> regulation = (ArrayList<Regulation>) Gpr.readFileSerializedGz(regFile);

		//---
		// Are all chromosomes available?
		//---
		Genome genome = config.getGenome();
		HashMap<String, Integer> chrs = new HashMap<String, Integer>();
		for (Regulation r : regulation) {
			String chr = r.getChromosomeName();
			int max = chrs.containsKey(chr) ? chrs.get(chr) : 0;
			max = Math.max(max, r.getEnd());
			chrs.put(chr, max);
		}

		// Add all chromos
		for (String chr : chrs.keySet())
			if (genome.getChromosome(chr) == null) genome.add(new Chromosome(genome, 0, chrs.get(chr), 1, chr));

		//---
		// Add all markers to predictor
		//---
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		for (Regulation r : regulation)
			snpEffectPredictor.add(r);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setGenomeVer(String genomeVer) {
		this.genomeVer = genomeVer;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Print command line options relevant to loading a SnpEffect predictor and creating an interval forest.
	 * @param message

	 */
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		else {
			System.err.println("\t-interval <file>                : Use a custom interval file (you may use this option many times). Formats accepted: BED, BigBed TXT and VCF");
			System.err.println("\t-canon                          : Only use canonical transcripts.");
			System.err.println("\t-motif                          : Annotate using motifs (requires Motif database).");
			System.err.println("\t-nextProt                       : Annotate using NextProt (requires NextProt database).");
			System.err.println("\t-reg <name>                     : Regulation track to use (this option can be used add several times).");
			System.err.println("\t-onlyReg                        : Only use regulation tracks.");
			System.err.println("\t-onlyTr <file.txt>              : Only use the transcripts in this file. Format: One transcript ID per line.");
			System.err.println("\t-ss, -spliceSiteSize <int>      : Set size for splice sites (donor and acceptor) in bases. Default: " + spliceSiteSize);
			System.err.println("\t-ud, -upDownStreamLen <int>     : Set upstream downstream interval length (in bases)");
			System.err.println("\t-treatAllAsProteinCoding        : Force all genes as protein coding. Very dangerous option, never use unless you are absolutely sure.");
		}
		System.exit(-1);
	}
}
