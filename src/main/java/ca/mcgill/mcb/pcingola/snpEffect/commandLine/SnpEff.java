package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ca.mcgill.mcb.pcingola.Pcingola;
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
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.logStatsServer.LogStats;
import ca.mcgill.mcb.pcingola.logStatsServer.VersionCheck;
import ca.mcgill.mcb.pcingola.motif.Jaspar;
import ca.mcgill.mcb.pcingola.motif.Pwm;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.spliceSites.SnpEffCmdSpliceAnalysis;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * SnpEff's main command line program
 *
 * @author pcingola
 */
public class SnpEff implements CommandLine {

	/**
	 *  Available gene database formats
	 */
	public enum GeneDatabaseFormat {
		// BED // http://genome.ucsc.edu/FAQ/FAQformat.html#format1
		BIOMART //
		, GFF3 // Obsolete GFF 3 format (http://www.sequenceontology.org/gff3.shtml)
		, GFF2 // Obsolete GFF 2 format
		, GTF22 // GTF 2.2 format (http://mblab.wustl.edu/GTF22.html)
		, REFSEQ // UCSC's format using RefSeq
		, KNOWN_GENES // UCSC's format using KnownGenes
		, GENBANK //  GeneBank file format
		, EMBL // EMBL file format
	}

	/**
	 *  Available input formats
	 */
	public enum InputFormat {
		// TXT, PILEUP,
		VCF, BED
	}

	/**
	 *  Available output formats
	 */
	public enum OutputFormat {
		VCF, BED, BEDANN, GATK
	}

	public static final int COMMAND_LINE_WIDTH = 40;

	// Version info
	public static final String SOFTWARE_NAME = "SnpEff";
	public static final String REVISION = "d";
	public static final String BUILD = "2014-09-04";
	public static final String VERSION_MAJOR = "4.0";
	public static final String VERSION_SHORT = VERSION_MAJOR + REVISION;
	public static final String VERSION_NO_NAME = VERSION_SHORT + " (build " + BUILD + "), by " + Pcingola.BY;
	public static final String VERSION = SOFTWARE_NAME + " " + VERSION_NO_NAME;

	protected String command = "";
	protected String[] args; // Arguments used to invoke this command
	protected String[] shiftArgs;
	protected boolean canonical = false; // Use only canonical transcripts
	protected boolean debug; // Debug mode
	protected boolean download = true; // Download genome, if not available
	protected boolean help; // Show command help and exit
	protected boolean log; // Log to server (statistics)
	protected boolean motif = false; // Annotate using motifs
	protected boolean multiThreaded = false; // Use multiple threads
	protected boolean nextProt = false; // Annotate using NextProt database
	protected boolean nextProtKeepAllTrs = false; // Keep all nextprot entries, even if the transcript doesn't exist
	protected boolean noGenome = false; // Do not load genome database
	protected boolean onlyProtein = false; // Only use protein coding transcripts
	protected boolean onlyRegulation = false; // Only build regulation tracks
	protected boolean quiet; // Be quiet
	protected boolean strict = false; // Only use transcript that have been validated
	protected boolean saveOutput = false; // Save output to buffer (instead of printing it to STDOUT)
	protected boolean suppressOutput = false; // Only used for debugging purposes
	protected boolean verbose; // Be verbose
	protected Boolean treatAllAsProteinCoding = null; // Only use coding genes. Default is 'null' which means 'auto'
	protected int numWorkers = Gpr.NUM_CORES; // Max number of threads (if multi-threaded version is available)
	protected int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE; // Splice site size default: 2 bases (canonical splice site)
	protected int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
	protected String configFile; // Config file
	protected String dataDir; // Override data_dir in config file
	protected String genomeVer; // Genome version
	protected String onlyTranscriptsFile = null; // Only use the transcripts in this file (Format: One transcript ID per line)
	protected StringBuilder output = new StringBuilder();
	protected Config config; // Configuration
	protected SnpEff snpEffCmd; // Real command to run
	protected ArrayList<String> customIntervalFiles; // Custom interval files (bed)
	protected ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	protected HashSet<String> regulationTracks = new HashSet<String>();

	/**
	 * Main
	 */
	public static void main(String[] args) {
		// Parse
		SnpEff snpEff = new SnpEff(args);

		// Run
		boolean ok = snpEff.run();
		System.exit(ok ? 0 : -1);
	}

	public static void warning(String warningType, String details) {
		Config.get().warning(warningType, details);
	}

	public SnpEff() {
		genomeVer = ""; // Genome version
		configFile = Config.DEFAULT_CONFIG_FILE; // Config file
		verbose = false; // Be verbose
		debug = false; // Debug mode
		quiet = false; // Be quiet
		log = true; // Log to server (statistics)
		multiThreaded = false; // Use multiple threads
		customIntervalFiles = new ArrayList<String>(); // Custom interval files
	}

	public SnpEff(String[] args) {
		genomeVer = ""; // Genome version
		configFile = Config.DEFAULT_CONFIG_FILE; // Config file
		verbose = false; // Be verbose
		debug = false; // Debug mode
		quiet = false; // Be quiet
		log = true; // Log to server (statistics)
		multiThreaded = false; // Use multiple threads
		customIntervalFiles = new ArrayList<String>(); // Custom interval files

		this.args = args;
	}

	/**
	 * Check if there is a new version of the program
	 */
	void checkNewVersion(Config config) {
		// Download command checks for versions, no need to do it twice
		if ((config != null) && !command.equalsIgnoreCase("download")) {
			// Check if a new version is available
			VersionCheck versionCheck = VersionCheck.version(SnpEff.SOFTWARE_NAME, SnpEff.VERSION_SHORT, config.getVersionsUrl(), verbose);
			if (!quiet && versionCheck.isNewVersion()) {
				System.err.println("\n\nNEW VERSION!\n\tThere is a new " + this.getClass().getSimpleName() + " version available: " //
						+ "\n\t\tVersion      : " + versionCheck.getLatestVersion() //
						+ "\n\t\tRelease date : " + versionCheck.getLatestReleaseDate() //
						+ "\n\t\tDownload URL : " + versionCheck.getLatestUrl() //
						+ "\n" //
				);
			}
		}
	}

	/**
	 * 	Command line argument list (try to fit it into COMMAND_LINE_WIDTH)
	 */
	String commandLineStr(boolean splitLines) {
		StringBuilder argsList = new StringBuilder();
		argsList.append("SnpEff " + command + " ");
		int size = argsList.length();

		for (String arg : args) {
			argsList.append(arg);
			size += arg.length();
			if (splitLines && (size > COMMAND_LINE_WIDTH)) {
				argsList.append(" \n");
				size = 0;
			} else {
				argsList.append(" ");
				size++;
			}
		}

		return argsList.toString();
	}

	/**
	 * Show an error (if not 'quiet' mode)
	 */
	public void error(Throwable e, String message) {
		if (verbose && (e != null)) e.printStackTrace();
		if (!quiet) System.err.println(message);
	}

	/**
	 * Show an error message and exit
	 */
	public void fatalError(String message) {
		System.err.println(message);
		System.exit(-1);
	}

	@Override
	public String[] getArgs() {
		return args;
	}

	public Config getConfig() {
		return config;
	}

	public String getOutput() {
		return output.toString();
	}

	/**
	 * Is this a command line option (e.g. "-tfam" is a command line option, but "-" means STDIN)
	 */
	protected boolean isOpt(String arg) {
		return arg.startsWith("-") && (arg.length() > 1);
	}

	/**
	 * Read config file
	 */
	protected void loadConfig() {
		// Read config file
		if (verbose) //
			Timer.showStdErr("Reading configuration file '" + configFile + "'" //
					+ ((genomeVer != null) && (!genomeVer.isEmpty()) ? ". Genome: '" + genomeVer + "'" : "") //
			);

		config = new Config(genomeVer, configFile, dataDir); // Read configuration
		if (verbose) Timer.showStdErr("done");

		// Set some parameters
		config.setDebug(debug);
		config.setVerbose(verbose);
	}

	/**
	 * Read a custom interval file
	 */
	protected int loadCustomFile(String fileName) {
		Markers markers = loadMarkers(fileName);

		// Add all markers to predictor
		for (Marker m : markers)
			config.getSnpEffectPredictor().add(m);

		// Number added
		return markers.size();
	}

	/**
	 * Load database
	 */
	public void loadDb() {
		// Read database (or create a new one)
		if (noGenome) {
			if (verbose) Timer.showStdErr("Creating empty database (no genome).");
			SnpEffectPredictor snpEffectPredictor = new SnpEffectPredictor(new Genome());
			config.setSnpEffectPredictor(snpEffectPredictor);
			config.setErrorChromoHit(false); // We don't have chromosomes, so we de-activate this error.
		} else if (onlyRegulation) {
			// Create predictor
			config.setSnpEffectPredictor(new SnpEffectPredictor(config.getGenome()));
			config.setOnlyRegulation(true);
			config.setErrorOnMissingChromo(false); // A chromosome might be missing (e.g. no regulation tracks available for 'MT')
			config.setErrorChromoHit(false); // A chromosome's length might be smaller than the real (it's calculated using regulation features, not real chromo data)
		} else {
			// Read
			if (verbose) Timer.showStdErr("Reading database for genome version '" + genomeVer + "' from file '" + config.getFileSnpEffectPredictor() + "' (this might take a while)");

			// Try to download database if it doesn't exists?
			if (download && !Gpr.canRead(config.getFileSnpEffectPredictor())) {
				if (verbose) Timer.showStdErr("Database not installed\n\tAttempting to download and install database '" + genomeVer + "'");

				// Run download command
				String downloadArgs[] = { genomeVer };
				SnpEffCmdDownload snpEffCmdDownload = new SnpEffCmdDownload();
				boolean ok = run(snpEffCmdDownload, downloadArgs, null);
				if (!ok) throw new RuntimeException("Genome download failed!");
				else if (verbose) Timer.showStdErr("Database installed.");
			}

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
			int count = loadCustomFile(intFile);
			if (verbose) Timer.showStdErr("done (" + count + " intervals loaded). ");
		}

		// Read regulation tracks
		for (String regTrack : regulationTracks)
			loadRegulationTrack(regTrack);

		// Set upstream-downstream interval length
		config.getSnpEffectPredictor().setUpDownStreamLength(upDownStreamLength);

		// Set splice site size
		config.getSnpEffectPredictor().setSpliceSiteSize(spliceSiteSize);

		// Filter canonical transcripts
		if (canonical) {
			if (verbose) Timer.showStdErr("Filtering out non-canonical transcripts.");
			config.getSnpEffectPredictor().removeNonCanonical();

			if (verbose) {
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

		// Filter verified transcripts
		if (strict) {
			if (verbose) Timer.showStdErr("Filtering out non-verified transcripts.");
			config.getSnpEffectPredictor().removeUnverified();
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

		// Use protein coding transcripts
		if (onlyProtein) {
			// Remove transcripts
			if (verbose) Timer.showStdErr("Filtering out non-protein coding transcripts.");
			int removed = config.getSnpEffectPredictor().retainTranscriptsProtein();
			if (verbose) Timer.showStdErr("Done: " + removed + " transcripts removed.");
		}

		// Try to load NextProt ad motif databases
		loadNextProt();
		loadMotif();

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
	 * Read markers file
	 * Supported formats: BED, TXT, BigBed, GFF
	 */
	protected Markers loadMarkers(String fileName) {
		Markers markersSeqChange = Markers.readMarkers(fileName);
		String label = Gpr.removeExt(Gpr.baseName(fileName));

		// Convert 'SeqChange' markers to 'Custom' markers
		Markers markers = new Markers();
		for (Marker m : markersSeqChange) {
			if (m instanceof Custom) {
				((Custom) m).setLabel(label);
				markers.add(m);
			} else {
				// Not a custom interval? Create one
				Custom custom = new Custom(m.getParent(), m.getStart(), m.getEnd(), m.isStrandMinus(), m.getId(), label);
				// custom.setScore(((Variant) m).getScore());
				markers.add(custom);
			}
		}

		// Number added
		return markers;
	}

	/**
	 * Read regulation motif files
	 */
	void loadMotif() {
		if (verbose) Timer.showStdErr("Loading Motifs and PWMs");

		//---
		// Sanity checks
		//---
		String pwmsFileName = config.getDirDataVersion() + "/pwms.bin";
		String motifBinFileName = config.getBaseFileNameMotif() + ".bin";

		if (!Gpr.exists(pwmsFileName) || !Gpr.exists(motifBinFileName)) {
			// We explicitly requested this annotations, if files are not there, it's an error
			if (motif) {
				if (!Gpr.exists(pwmsFileName)) fatalError("Warning: Cannot open PWMs file " + pwmsFileName);
				if (!Gpr.exists(motifBinFileName)) fatalError("Warning: Cannot open Motifs file " + motifBinFileName);
			}

			// OK, we don't have motif annotations, no problem
			if (debug) {
				if (!Gpr.exists(pwmsFileName)) warning("Warning: Cannot open PWMs file ", pwmsFileName);
				if (!Gpr.exists(motifBinFileName)) warning("Warning: Cannot open Motifs file ", motifBinFileName);
			}
			return;
		}

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
				} else if (debug) Timer.showStdErr("Cannot find PWM for motif '" + motif.getPwmId() + "'");
			}

		if (verbose) Timer.showStdErr("\tMotif database: " + countAddded + " markers loaded.");
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 */
	void loadNextProt() {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		//---
		// Read nextProt binary file
		//---
		String nextProtBinFile = config.getDirDataVersion() + "/nextProt.bin";
		if (!Gpr.canRead(nextProtBinFile)) {
			if (nextProt) fatalError("NextProt database '" + nextProtBinFile + "' doesn't exist");
			if (debug) Timer.showStdErr("NextProt database '" + nextProtBinFile + "' doesn't exist. Ignoring.");
			return;
		}
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

		// Add nextprot entries
		if (nextProtKeepAllTrs) {
			// Add all nextProt marker to predictor (even if the transcript doesn't exist)
			// WARNING: This is not recommended
			for (NextProt np : nextProts)
				snpEffectPredictor.add(np);
		} else {
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

			// Add all nextProt marker to predictor
			for (NextProt np : nextProtsToAdd)
				snpEffectPredictor.add(np);

			// Note: We might end up with more markers than we loaded (just because they map to multiple exons (although it would be highly unusual)
			if (verbose) Timer.showStdErr("NextProt database: " + nextProtsToAdd.size() + " markers added.");
		}

	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 */
	@SuppressWarnings("unchecked")
	void loadRegulationTrack(String regTrack) {
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
			if (genome.getChromosome(chr) == null) genome.add(new Chromosome(genome, 0, chrs.get(chr), chr));

		//---
		// Add all markers to predictor
		//---
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		for (Regulation r : regulation)
			snpEffectPredictor.add(r);
	}

	/**
	 * Parse command line arguments
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args.length <= 0) usage("Missing command");

		int argNum = 0;

		//---
		// Parse command
		//---
		if (args[0].equalsIgnoreCase("build") //
				|| args[0].equalsIgnoreCase("buildNextProt") //
				|| args[0].equalsIgnoreCase("dump") //
				|| args[0].equalsIgnoreCase("cds") //
				|| args[0].equalsIgnoreCase("eff") //
				|| args[0].equalsIgnoreCase("download") //
				|| args[0].equalsIgnoreCase("protein") //
				|| args[0].equalsIgnoreCase("closest") //
				|| args[0].equalsIgnoreCase("test") //
				|| args[0].equalsIgnoreCase("databases") //
				|| args[0].equalsIgnoreCase("spliceAnalysis") //
				|| args[0].equalsIgnoreCase("count") //
				|| args[0].equalsIgnoreCase("genes2bed") //
				|| args[0].equalsIgnoreCase("gsa") //
				|| args[0].equalsIgnoreCase("len") //
				|| args[0].equalsIgnoreCase("acat") //
		) {
			command = args[argNum++].toLowerCase();
		} else {
			command = "eff"; // Default command is 'eff'
		}

		//---
		// Copy and parse args, except initial 'command'
		//---
		ArrayList<String> argsList = new ArrayList<String>();
		for (int i = argNum; i < args.length; i++) {
			String arg = args[i];

			// These options are available for allow all commands
			// Is it a command line option?
			if (isOpt(arg)) {

				if ((arg.equals("-c") || arg.equalsIgnoreCase("-config"))) {
					if ((i + 1) < args.length) configFile = args[++i];
					else usage("Option '-c' without config file argument");
				} else if (arg.equalsIgnoreCase("-canon")) canonical = true; // Use canonical transcripts
				else if (arg.equals("-d") || arg.equalsIgnoreCase("-debug")) debug = verbose = true;
				else if (arg.equalsIgnoreCase("-dataDir")) {
					if ((i + 1) < args.length) dataDir = args[++i];
					else usage("Option '-dataDir' without data_dir argument");
				} else if (arg.equalsIgnoreCase("-download")) download = true; // Download genome if not locally available
				else if (arg.equals("-h") || arg.equalsIgnoreCase("-help")) help = true;
				else if (arg.equalsIgnoreCase("-interval")) {
					if ((i + 1) < args.length) customIntervalFiles.add(args[++i]);
					else usage("Option '-interval' without config interval_file argument");
				} else if (arg.equalsIgnoreCase("-motif")) motif = true; // Use motif database
				else if (arg.equalsIgnoreCase("-nextProt")) nextProt = true; // Use NextProt database
				else if (arg.equalsIgnoreCase("-nodownload")) download = false; // Do not download genome
				else if (arg.equalsIgnoreCase("-noLog")) log = false;
				else if (arg.equalsIgnoreCase("-noOut")) suppressOutput = true; // Undocumented option (only used for development & debugging)
				else if (arg.equalsIgnoreCase("-onlyReg")) onlyRegulation = true;
				else if (arg.equalsIgnoreCase("-onlyProtein")) onlyProtein = true;
				else if (arg.equalsIgnoreCase("-onlyTr")) {
					if ((i + 1) < args.length) onlyTranscriptsFile = args[++i]; // Only use the transcripts in this file
				} else if (arg.equals("-q") || arg.equalsIgnoreCase("-quiet")) {
					quiet = true;
					verbose = false;
				} else if (arg.equals("-reg")) {
					if ((i + 1) < args.length) regulationTracks.add(args[++i]); // Add this track to the list
				} else if ((arg.equals("-ss") || arg.equalsIgnoreCase("-spliceSiteSize"))) {
					if ((i + 1) < args.length) spliceSiteSize = Gpr.parseIntSafe(args[++i]);
				} else if (arg.equalsIgnoreCase("-strict")) strict = true;
				else if (arg.equals("-t")) multiThreaded = true;
				else if (arg.equalsIgnoreCase("-treatAllAsProteinCoding")) {
					if ((i + 1) < args.length) {
						i++;
						if (args[i].equalsIgnoreCase("auto")) treatAllAsProteinCoding = null;
						else treatAllAsProteinCoding = Gpr.parseBoolSafe(args[i]);
					}
				} else if ((arg.equals("-ud") || arg.equalsIgnoreCase("-upDownStreamLen"))) {
					if ((i + 1) < args.length) upDownStreamLength = Gpr.parseIntSafe(args[++i]);
				} else if (arg.equals("-v") || arg.equalsIgnoreCase("-verbose")) {
					verbose = true;
					quiet = false;
				} else {
					// Unrecognized option? may be it's command specific. Let command parse it
					argsList.add(arg);
				}
			} else {
				// Command specific argument: Let command parse it
				argsList.add(arg);
			}
		}

		shiftArgs = argsList.toArray(new String[0]);
	}

	/**
	 * Print to screen or save to output buffer
	 */
	void print(Object o) {
		if (saveOutput) output.append(o.toString() + "\n");
		else if (!suppressOutput) System.out.println(o.toString());
	}

	/**
	 * Additional values to be reported
	 */
	public HashMap<String, String> reportValues() {
		HashMap<String, String> reportValues = new HashMap<String, String>();
		return reportValues;
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		SnpEff snpEffCmd = snpEffCmd();
		if (snpEffCmd == null) return true;

		//---
		// Run
		//---
		boolean ok = false;
		StringBuilder err = new StringBuilder();
		try {
			ok = snpEffCmd.run();
		} catch (Throwable t) {
			ok = false;
			if (err != null) err.append(t.getMessage());
			t.printStackTrace();
		}

		// Report to server (usage statistics)
		if (log) {
			// Log to server
			LogStats.report(SOFTWARE_NAME, VERSION_SHORT, VERSION, ok, verbose, args, err.toString(), snpEffCmd.reportValues());

			// Check for new version (use config file from command, since this one doesn't load a config file)
			checkNewVersion(snpEffCmd.config);
		}

		return ok;
	}

	/**
	 * Run a SnpEff (usually a sub-class)
	 */
	protected boolean run(SnpEff snpEff, String args[], StringBuilder err) {
		boolean ok = false;
		try {
			snpEff.verbose = verbose;
			snpEff.help = help;
			snpEff.debug = debug;
			snpEff.quiet = quiet;
			snpEff.configFile = configFile;
			snpEff.dataDir = dataDir;

			if (help) snpEff.usage(null); // Show help message and exit
			else snpEff.parseArgs(args);
			ok = snpEff.run();
		} catch (Throwable t) {
			if (err != null) err.append(t.getMessage());
			t.printStackTrace();
		}
		return ok;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setGenomeVer(String genomeVer) {
		this.genomeVer = genomeVer;
	}

	public void setNextProtKeepAllTrs(boolean nextProtKeepAllTrs) {
		this.nextProtKeepAllTrs = nextProtKeepAllTrs;
	}

	public void setSupressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Create an appropriate SnpEffCmd* object
	 */

	public SnpEff snpEffCmd() {
		// Parse command line arguments (generic and database specific arguments)
		parseArgs(args);

		SnpEff snpEffCmd = null;

		// All commands are lower-case
		command = command.toLowerCase();
		if (command.equalsIgnoreCase("build")) snpEffCmd = new SnpEffCmdBuild();
		else if (command.equalsIgnoreCase("buildNextProt")) snpEffCmd = new SnpEffCmdBuildNextProt();
		else if (command.equalsIgnoreCase("dump")) snpEffCmd = new SnpEffCmdDump();
		else if (command.equalsIgnoreCase("download")) snpEffCmd = new SnpEffCmdDownload();
		else if (command.equalsIgnoreCase("cds")) snpEffCmd = new SnpEffCmdCds();
		else if (command.equalsIgnoreCase("eff")) snpEffCmd = new SnpEffCmdEff();
		else if (command.equalsIgnoreCase("protein")) snpEffCmd = new SnpEffCmdProtein();
		else if (command.equalsIgnoreCase("closest")) snpEffCmd = new SnpEffCmdClosest();
		else if (command.equalsIgnoreCase("databases")) snpEffCmd = new SnpEffCmdDatabases();
		else if (command.equalsIgnoreCase("genes2bed")) snpEffCmd = new SnpEffCmdGenes2Bed();
		else if (command.equalsIgnoreCase("spliceanalysis")) snpEffCmd = new SnpEffCmdSpliceAnalysis();
		else if (command.equalsIgnoreCase("count")) snpEffCmd = new SnpEffCmdCount();
		else if (command.equalsIgnoreCase("len")) snpEffCmd = new SnpEffCmdLen();
		else if (command.equalsIgnoreCase("gsa")) snpEffCmd = new SnpEffCmdGsa();
		else if (command.equalsIgnoreCase("acat")) snpEffCmd = new SnpEffCmdAcat();
		else throw new RuntimeException("Unknown command '" + command + "'");

		// Copy values to specific command
		snpEffCmd.canonical = canonical;
		snpEffCmd.configFile = configFile;
		snpEffCmd.customIntervalFiles = customIntervalFiles;
		snpEffCmd.dataDir = dataDir;
		snpEffCmd.debug = debug;
		snpEffCmd.download = download;
		snpEffCmd.filterIntervalFiles = filterIntervalFiles;
		snpEffCmd.genomeVer = genomeVer;
		snpEffCmd.help = help;
		snpEffCmd.log = log;
		snpEffCmd.motif = motif;
		snpEffCmd.multiThreaded = multiThreaded;
		snpEffCmd.nextProt = nextProt;
		snpEffCmd.numWorkers = numWorkers;
		snpEffCmd.onlyProtein = onlyProtein;
		snpEffCmd.onlyRegulation = onlyRegulation;
		snpEffCmd.onlyTranscriptsFile = onlyTranscriptsFile;
		snpEffCmd.quiet = quiet;
		snpEffCmd.regulationTracks = regulationTracks;
		snpEffCmd.spliceSiteSize = spliceSiteSize;
		snpEffCmd.strict = strict;
		snpEffCmd.suppressOutput = suppressOutput;
		snpEffCmd.treatAllAsProteinCoding = treatAllAsProteinCoding;
		snpEffCmd.upDownStreamLength = upDownStreamLength;
		snpEffCmd.verbose = verbose;

		// Help requested?
		if (help) {
			snpEffCmd.usage(null); // Show help message and exit
			return null;
		}

		// Parse command specific arguments
		snpEffCmd.parseArgs(shiftArgs);
		return snpEffCmd;
	}

	/**
	 * Show 'usage' message and exit with an error code '-1'
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff [command] [options] [files]");
		System.err.println("\nRun 'java -jar snpEff.jar command' for help on each specific command");
		System.err.println("\nAvailable commands: ");
		System.err.println("\t[eff]                        : Calculate effect of variants. Default: eff (no command or 'eff').");
		System.err.println("\tbuild                        : Build a SnpEff database.");
		System.err.println("\tbuildNextProt                : Build a SnpEff for NextProt (using NextProt's XML files).");
		System.err.println("\tcds                          : Compare CDS sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness.");
		System.err.println("\tclosest                      : Annotate the closest genomic region.");
		System.err.println("\tcount                        : Count how many intervals (from a BAM, BED or VCF file) overlap with each genomic interval.");
		System.err.println("\tdatabases                    : Show currently available databases (from local config file).");
		System.err.println("\tdownload                     : Download a SnpEff database.");
		System.err.println("\tdump                         : Dump to STDOUT a SnpEff database (mostly used for debugging).");
		System.err.println("\tgenes2bed                    : Create a bed file from a genes list.");
		System.err.println("\tlen                          : Calculate total genomic length for each marker type.");
		System.err.println("\tprotein                      : Compare protein sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness.");
		System.err.println("\tspliceAnalysis               : Perform an analysis of splice sites. Experimental feature.");

		usageGenericAndDb();

		System.exit(-1);
	}

	/**
	 * Show generic options
	 */
	protected void usageGenericAndDb() {
		System.err.println("\nGeneric options:");
		System.err.println("\t-c , -config                 : Specify config file");
		System.err.println("\t-d , -debug                  : Debug mode (very verbose).");
		System.err.println("\t-dataDir <path>              : Override data_dir parameter from config file.");
		System.err.println("\t-download                    : Download a SnpEff database, if not available locally. Default: " + download);
		System.err.println("\t-nodownload                  : Do not download a SnpEff database, if not available locally.");
		System.err.println("\t-h , -help                   : Show this help and exit");
		System.err.println("\t-noLog                       : Do not report usage statistics to server");
		System.err.println("\t-t                           : Use multiple threads (implies '-noStats'). Default 'off'");
		System.err.println("\t-q ,  -quiet                 : Quiet mode (do not show any messages or errors)");
		System.err.println("\t-v , -verbose                : Verbose mode");
		System.err.println("\nDatabase options:");
		System.err.println("\t-canon                       : Only use canonical transcripts.");
		System.err.println("\t-interval                    : Use a custom intervals in TXT/BED/BigBed/VCF/GFF file (you may use this option many times)");
		System.err.println("\t-motif                       : Annotate using motifs (requires Motif database).");
		System.err.println("\t-nextProt                    : Annotate using NextProt (requires NextProt database).");
		System.err.println("\t-onlyReg                     : Only use regulation tracks.");
		System.err.println("\t-onlyProtein                 : Only use protein coding transcripts. Default: " + onlyProtein);
		System.err.println("\t-onlyTr <file.txt>           : Only use the transcripts in this file. Format: One transcript ID per line.");
		System.err.println("\t-reg <name>                  : Regulation track to use (this option can be used add several times).");
		System.err.println("\t-ss , -spliceSiteSize <int>  : Set size for splice sites (donor and acceptor) in bases. Default: " + spliceSiteSize);
		System.err.println("\t-strict                      : Only use 'validated' transcripts (i.e. sequence has been checked). Default: " + strict);
		System.err.println("\t-ud , -upDownStreamLen <int> : Set upstream downstream interval length (in bases)");
	}

}
