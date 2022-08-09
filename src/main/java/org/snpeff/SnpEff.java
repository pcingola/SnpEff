package org.snpeff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Custom;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Motif;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.ProteinInteractionLocus;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.TranscriptSupportLevel;
import org.snpeff.logStatsServer.LogStats;
import org.snpeff.logStatsServer.VersionCheck;
import org.snpeff.motif.Jaspar;
import org.snpeff.motif.Pwm;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.pdb.ProteinInteractions;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.CommandLine;
import org.snpeff.snpEffect.commandLine.SnpEffCmdAcat;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuildNextProt;
import org.snpeff.snpEffect.commandLine.SnpEffCmdCds;
import org.snpeff.snpEffect.commandLine.SnpEffCmdClosest;
import org.snpeff.snpEffect.commandLine.SnpEffCmdCount;
import org.snpeff.snpEffect.commandLine.SnpEffCmdDatabases;
import org.snpeff.snpEffect.commandLine.SnpEffCmdDownload;
import org.snpeff.snpEffect.commandLine.SnpEffCmdDump;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdGenes2Bed;
import org.snpeff.snpEffect.commandLine.SnpEffCmdGsa;
import org.snpeff.snpEffect.commandLine.SnpEffCmdLen;
import org.snpeff.snpEffect.commandLine.SnpEffCmdPdb;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.snpEffect.commandLine.SnpEffCmdSeq;
import org.snpeff.snpEffect.commandLine.SnpEffCmdShow;
import org.snpeff.snpEffect.commandLine.SnpEffCmdTranslocationsReport;
import org.snpeff.spliceSites.SnpEffCmdSpliceAnalysis;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * SnpEff's main command line program
 *
 * @author pcingola
 */
public class SnpEff implements CommandLine {

	/**
	 * Available gene database formats
	 */
	public enum GeneDatabaseFormat {
		// BED // http://genome.ucsc.edu/FAQ/FAQformat.html#format1
		BIOMART //
		, GFF3 // Discouraged GFF 3 format (http://www.sequenceontology.org/gff3.shtml)
		, GFF2 // Obsolete GFF 2 format
		, GTF22 // GTF 2.2 format (http://mblab.wustl.edu/GTF22.html)
		, REFSEQ // UCSC's format using RefSeq
		, KNOWN_GENES // UCSC's format using KnownGenes
		, GENBANK // GeneBank file format
		, EMBL // EMBL file format
	}

	/**
	 * Available input formats
	 */
	public enum InputFormat {
		// TXT, PILEUP,
		VCF, BED
	}

	/**
	 * Available output formats
	 */
	public enum OutputFormat {
		VCF, BED, BEDANN, GATK
	}

	public static final String DEFAULT_COMMAND = "ann";
	public static final int COMMAND_LINE_WIDTH = 40;

	// Version info
	public static final String SOFTWARE_NAME = "SnpEff";
	public static final String REVISION = "d";
	public static final String BUILD = Gpr.compileTimeStamp(SnpEff.class);
	public static final String BUILD_DATE = Gpr.compileDate(SnpEff.class);
	public static final String VERSION_MAJOR = "5.1";
	public static final String VERSION_SHORT = VERSION_MAJOR + REVISION;
	public static final String VERSION_BUILD = VERSION_SHORT + " (build " + BUILD + ")";
	public static final String VERSION_AUTHOR = VERSION_BUILD + ", by " + Pcingola.BY;
	public static final String VERSION = SOFTWARE_NAME + " " + VERSION_AUTHOR;

	protected String command = "";
	protected String[] args; // Arguments used to invoke this command
	protected String[] shiftArgs;
	protected boolean canonical = false; // Use only canonical transcripts
	protected boolean debug; // Debug mode
	protected boolean download = true; // Download genome, if not available
	protected boolean expandIub = true; // Expand IUB codes
	protected boolean help; // Show command help and exit
	protected boolean hgvs = true; // Use Hgvs notation
	protected boolean hgvsForce = false; // Use Hgvs notation even in classic mode?
	protected boolean hgvsOneLetterAa = false; // Use 1-letter AA codes in HGVS.p notation?
	protected boolean hgvsOld = false; // Old notation style notation: E.g. 'c.G123T' instead of 'c.123G>T' and 'X' instead of '*'
	protected boolean hgvsShift = true; // Shift variants towards the 3-prime end of the transcript
	protected boolean hgvsTrId = false; // Use full transcript version in HGVS notation?
	protected boolean interaction = true; // Use interaction loci information if available
	protected boolean log; // Log to server (statistics)
	protected boolean motif = true; // Annotate using motifs
	protected boolean multiThreaded = false; // Use multiple threads
	protected boolean nextProt = true; // Annotate using NextProt database
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
	protected int spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
	protected int spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
	protected int spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;
	protected int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
	protected String configFile; // Config file
	protected String dataDir; // Override data_dir in config file
	protected String genomeVer; // Genome version
	protected String onlyTranscriptsFile = null; // Only use the transcripts in this file (Format: One transcript ID per line)
	protected String canonicalFile = null; // Use cannonical transcripts changing the ones that are present in the file.
	protected TranscriptSupportLevel maxTranscriptSupportLevel = null; // Filter by maximum Transcript Support Level (TSL)
	protected StringBuilder output = new StringBuilder();
	protected Config config; // Configuration
	protected Genome genome;
	protected SnpEff snpEffCmd; // Real command to run
	protected ArrayList<String> customIntervalFiles; // Custom interval files (bed)
	protected ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	protected HashSet<String> regulationTracks = new HashSet<>();
	protected Map<String, String> configOverride = new HashMap<>();

	/**
	 * Main
	 */
	public static void main(String[] args) {
		SnpEff snpEff = new SnpEff(args);
		boolean ok = snpEff.run();
		System.exit(ok ? 0 : -1);
	}

	public SnpEff() {
		genomeVer = ""; // Genome version
		configFile = Config.DEFAULT_CONFIG_FILE; // Config file
		verbose = false; // Be verbose
		debug = false; // Debug mode
		quiet = false; // Be quiet
		log = true; // Log to server (statistics)
		multiThreaded = false; // Use multiple threads
		customIntervalFiles = new ArrayList<>(); // Custom interval files
	}

	public SnpEff(String[] args) {
		this();
		this.args = args;
	}

	public void addRegulationTrack(String cellType) {
		regulationTracks.add(cellType);
	}

	/**
	 * Filter canonical transcripts
	 */
	protected void canonical() {
		if (verbose) Log.info("Filtering out non-canonical transcripts.");
		config.getSnpEffectPredictor().removeNonCanonical(canonicalFile);

		if (verbose) {
			// Show genes and transcript (which ones are considered 'canonical')
			Log.info("Canonical transcripts:\n\t\tgeneName\tgeneId\ttranscriptId\tcdsLength");
			for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
				for (Transcript t : g) {
					String cds = t.cds();
					int cdsLen = (cds != null ? cds.length() : 0);
					System.err.println("\t\t" + g.getGeneName() + "\t" + g.getId() + "\t" + t.getId() + "\t" + cdsLen);
				}
			}
		}
		if (verbose) Log.info("done.");
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
	 * Create an appropriate SnpEffCmd* object
	 */
	public SnpEff cmd() {
		// Parse command line arguments (generic and database specific arguments)
		parseArgs(args);

		// Create a new command
		SnpEff cmd = cmdFactory(command);

		// Copy values to specific command
		copyValues(cmd);

		// Help requested?
		if (help) {
			cmd.usage(null); // Show help message and exit
			return null;
		}

		// Parse command specific arguments
		cmd.parseArgs(shiftArgs);
		return cmd;
	}

	SnpEff cmdFactory(String command) {
		// All commands are lower-case
		switch (command.trim().toLowerCase()) {
		case "ann":
		case "eff":
			return new SnpEffCmdEff();

		case "build":
			return new SnpEffCmdBuild();

		case "buildnextprot":
			return new SnpEffCmdBuildNextProt();

		case "cds":
			return new SnpEffCmdCds();

		case "closest":
			return new SnpEffCmdClosest();

		case "count":
			return new SnpEffCmdCount();

		case "databases":
			return new SnpEffCmdDatabases();

		case "download":
			return new SnpEffCmdDownload();

		case "dump":
			return new SnpEffCmdDump();

		case "gsa":
			return new SnpEffCmdGsa();

		case "genes2bed":
			return new SnpEffCmdGenes2Bed();

		case "len":
			return new SnpEffCmdLen();

		case "pdb":
			return new SnpEffCmdPdb();

		case "protein":
			return new SnpEffCmdProtein();

		case "seq":
			return new SnpEffCmdSeq();

		case "show":
			return new SnpEffCmdShow();

		case "translocreport":
			return new SnpEffCmdTranslocationsReport();

		// Obsolete stuff
		case "spliceanalysis":
			return new SnpEffCmdSpliceAnalysis();

		case "acat":
			return new SnpEffCmdAcat();

		default:
			throw new RuntimeException("Unknown command '" + command + "'");
		}

	}

	/**
	 * Command line argument list (try to fit it into COMMAND_LINE_WIDTH)
	 */
	protected String commandLineStr(boolean splitLines) {
		if (args == null) return "";

		StringBuilder argsList = new StringBuilder();
		argsList.append("SnpEff " + command + " ");
		int size = argsList.length();

		for (String arg : args) {
			argsList.append(arg.trim());
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
	 * Copy values to a new command
	 */
	void copyValues(SnpEff cmd) {
		cmd.canonical = canonical;
		cmd.canonicalFile = canonicalFile;
		cmd.configFile = configFile;
		cmd.customIntervalFiles = customIntervalFiles;
		cmd.dataDir = dataDir;
		cmd.debug = debug;
		cmd.download = download;
		cmd.expandIub = expandIub;
		cmd.filterIntervalFiles = filterIntervalFiles;
		cmd.genomeVer = genomeVer;
		cmd.help = help;
		cmd.hgvs = hgvs;
		cmd.hgvsForce = hgvsForce;
		cmd.hgvsOld = hgvsOld;
		cmd.hgvsOneLetterAa = hgvsOneLetterAa;
		cmd.hgvsShift = hgvsShift;
		cmd.hgvsTrId = hgvsTrId;
		cmd.interaction = interaction;
		cmd.log = log;
		cmd.motif = motif;
		cmd.maxTranscriptSupportLevel = maxTranscriptSupportLevel;
		cmd.multiThreaded = multiThreaded;
		cmd.nextProt = nextProt;
		cmd.noGenome = noGenome;
		cmd.numWorkers = numWorkers;
		cmd.onlyProtein = onlyProtein;
		cmd.onlyRegulation = onlyRegulation;
		cmd.onlyTranscriptsFile = onlyTranscriptsFile;
		cmd.quiet = quiet;
		cmd.regulationTracks = regulationTracks;
		cmd.spliceSiteSize = spliceSiteSize;
		cmd.spliceRegionExonSize = spliceRegionExonSize;
		cmd.spliceRegionIntronMax = spliceRegionIntronMax;
		cmd.spliceRegionIntronMin = spliceRegionIntronMin;
		cmd.strict = strict;
		cmd.suppressOutput = suppressOutput;
		cmd.treatAllAsProteinCoding = treatAllAsProteinCoding;
		cmd.upDownStreamLength = upDownStreamLength;
		cmd.verbose = verbose;
		cmd.configOverride = configOverride;
	}

	@Override
	public String[] getArgs() {
		return args;
	}

	public Config getConfig() {
		return config;
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getOutput() {
		return output.toString();
	}

	/**
	 * Is this a command line option (e.g. "-tfam" is a command line option, but "-"
	 * means STDIN)
	 */
	protected boolean isOpt(String arg) {
		return arg.startsWith("-") && (arg.length() > 1);
	}

	public void load() {
		loadConfig(); // Read config file
		loadDb(); // Load database
	}

	/**
	 * Read config file
	 */
	protected void loadConfig() {
		if (config == null) {

			// Read config file
			if (verbose) //
				Log.info("Reading configuration file '" + configFile + "'" //
						+ ((genomeVer != null) && (!genomeVer.isEmpty()) ? ". Genome: '" + genomeVer + "'" : "") //
				);

			config = new Config(genomeVer, configFile, dataDir, configOverride, verbose); // Read configuration
			if (verbose) Log.info("done");
		}

		// Command line options overriding configuration file
		config.setUseHgvs(hgvs);
		config.setHgvsOld(hgvsOld);
		config.setHgvsOneLetterAA(hgvsOneLetterAa);
		config.setHgvsShift(hgvsShift);
		config.setHgvsTrId(hgvsTrId);
		config.setExpandIub(expandIub);

		// Verbose & debug
		config.setDebug(debug);
		config.setVerbose(verbose);
		config.setQuiet(quiet);
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
		if (config.getSnpEffectPredictor() != null) {
			genome = config.getSnpEffectPredictor().getGenome();
			return; // Already loaded
		}

		// Read database (or create a new one)
		if (noGenome) {
			if (verbose) Log.info("Creating empty database (no genome).");
			SnpEffectPredictor snpEffectPredictor = new SnpEffectPredictor(new Genome());
			config.setSnpEffectPredictor(snpEffectPredictor);
			config.setErrorOnMissingChromo(false); // All chromosome will be missing (no genome)
			config.setErrorChromoHit(false); // We don't have chromosomes, so we de-activate this error.
		} else if (onlyRegulation) {
			// Create predictor
			config.setSnpEffectPredictor(new SnpEffectPredictor(config.getGenome()));
			config.setOnlyRegulation(true);
			config.setErrorOnMissingChromo(false); // A chromosome might be missing (e.g. no regulation tracks available  for 'MT')
			config.setErrorChromoHit(false); // A chromosome's length might be smaller than the real (it's calculated using regulation features, not real chromo data)
		} else {
			// Read
			if (verbose) Log.info("Reading database for genome version '" + genomeVer + "' from file '" + config.getFileSnpEffectPredictor() + "' (this might take a while)");

			// Try to download database if it doesn't exists?
			if (download && !Gpr.canRead(config.getFileSnpEffectPredictor())) {
				if (verbose) Log.info("Database not installed\n\tAttempting to download and install database '" + genomeVer + "'");

				// Run download command
				String downloadArgs[] = { genomeVer };
				SnpEffCmdDownload snpEffCmdDownload = new SnpEffCmdDownload();
				boolean ok = run(snpEffCmdDownload, downloadArgs, null);
				if (!ok) throw new RuntimeException("Genome download failed!");
				else if (verbose) Log.info("Database installed.");
			}

			config.loadSnpEffectPredictor(); // Read snpEffect predictor
			genome = config.getSnpEffectPredictor().getGenome();
			if (verbose) Log.info("done");
		}

		// Set 'treatAllAsProteinCoding'
		if (treatAllAsProteinCoding != null) config.setTreatAllAsProteinCoding(treatAllAsProteinCoding);
		else {
			// treatAllAsProteinCoding was set to 'auto'
			// I.e.: Use 'true' if there is protein coding info, otherwise use false.
			boolean tapc = !config.getGenome().hasCodingInfo();
			if (debug) Log.debug("Setting '-treatAllAsProteinCoding' to '" + tapc + "'");
			config.setTreatAllAsProteinCoding(tapc);
		}

		// Read custom interval files
		for (String intFile : customIntervalFiles) {
			if (verbose) Log.info("Reading interval file '" + intFile + "'");
			int count = loadCustomFile(intFile);
			if (verbose) Log.info("done (" + count + " intervals loaded). ");
		}

		// Read regulation tracks
		for (String regTrack : regulationTracks)
			loadRegulationTrack(regTrack);

		// Set upstream-downstream interval length
		config.getSnpEffectPredictor().setUpDownStreamLength(upDownStreamLength);

		// Set splice site/region sizes
		config.getSnpEffectPredictor().setSpliceSiteSize(spliceSiteSize);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(spliceRegionExonSize);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(spliceRegionIntronMin);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(spliceRegionIntronMax);

		// Filter canonical transcripts
		if (canonical || (canonicalFile != null && !canonicalFile.isEmpty())) canonical();

		// Filter transcripts by TSL
		if (maxTranscriptSupportLevel != null) {
			if (verbose) Log.info("Filtering transcripts by Transcript Support Level (TSL): " + maxTranscriptSupportLevel);
			config.getSnpEffectPredictor().filterTranscriptSupportLevel(maxTranscriptSupportLevel);

			if (verbose) {
				// Show genes and transcript (which ones are considered 'canonical')
				Log.info("Transcript:\n\t\tgeneName\tgeneId\ttranscriptId\tTSL");
				for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
					for (Transcript t : g)
						System.err.println("\t\t" + g.getGeneName() + "\t" + g.getId() + "\t" + t.getId() + "\t" + t.getTranscriptSupportLevel());
				}
			}
			if (verbose) Log.info("done.");
		}

		// Filter verified transcripts
		if (strict) {
			if (verbose) Log.info("Filtering out non-verified transcripts.");
			if (config.getSnpEffectPredictor().removeUnverified()) {
				Log.fatalError("All transcripts have been removed form every single gene!\nUsing strickt on this database leaves no information.");
			}
			if (verbose) Log.info("done.");
		}

		// Use transcripts set form input file
		if (onlyTranscriptsFile != null) {
			// Load file
			String onlyTr = Gpr.readFile(onlyTranscriptsFile);
			Set<String> trIds = new HashSet<>();
			for (String trId : onlyTr.split("\n"))
				trIds.add(trId.trim());

			// Remove transcripts
			if (verbose) Log.info("Filtering out transcripts in file '" + onlyTranscriptsFile + "'. Total " + trIds.size() + " transcript IDs.");
			int removed = config.getSnpEffectPredictor().retainAllTranscripts(trIds);
			int countTr = config.getSnpEffectPredictor().countTranscripts();
			if (verbose) Log.info("Done: " + removed + " transcripts removed, " + countTr + " transcripts left.");
			if (countTr <= 0) Log.fatalError("No transcripts left for analysis after filter using file '" + onlyTranscriptsFile + "'");
		}

		// Use protein coding transcripts
		if (onlyProtein) {
			// Remove transcripts
			if (verbose) Log.info("Filtering out non-protein coding transcripts.");
			int removed = config.getSnpEffectPredictor().keepTranscriptsProteinCoding();
			if (verbose) Log.info("Done: " + removed + " transcripts removed.");
		}

		// Load NextProt database
		if (nextProt) loadNextProt();

		// Load Motif databases
		if (motif) loadMotif();

		// Load Motif databases
		if (interaction) loadInteractions();

		// Build tree
		if (verbose) Log.info("Building interval forest");
		config.getSnpEffectPredictor().buildForest();
		if (verbose) Log.info("done.");

		// Show some genome stats. Chromosome names are shown, a lot of people has
		// problems with the correct chromosome names.
		if (verbose) {
			Log.info("Genome stats :");
			Genome genome = config.getGenome();

			// When in debug mode, try to show detailed errors
			StringBuilder errors = debug ? new StringBuilder() : null;
			System.err.println(genome.toString(errors));
			if (errors != null && (errors.length() > 0)) System.err.println(errors);
		}

		genome = config.getSnpEffectPredictor().getGenome();
		genome.getGenomicSequences().setVerbose(verbose);
	}

	/**
	 * Load protein interaction database
	 */
	void loadInteractions() {
		// Sanity checks
		String intFileName = config.getDirDataGenomeVersion() + "/" + ProteinInteractions.PROTEIN_INTERACTION_FILE;
		if (!Gpr.exists(intFileName)) {
			if (debug) if (!Gpr.exists(intFileName)) Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot open interactions file '" + intFileName + "'");
			return;
		}

		// Build transcript map
		HashMap<String, Transcript> id2tr = new HashMap<>();
		SnpEffectPredictor sep = config.getSnpEffectPredictor();
		Genome genome = sep.getGenome();
		for (Gene g : genome.getGenes())
			for (Transcript tr : g)
				id2tr.put(tr.getId(), tr);

		// Load all interactions
		if (verbose) Log.info("Loading interactions from : " + intFileName);
		String lines[] = Gpr.readFile(intFileName, true).split("\n");
		int count = 0, countSkipped = 0;
		for (String line : lines) {
			DistanceResult dres = new DistanceResult(line);
			Chromosome chr1 = genome.getChromosome(dres.chr1);
			Chromosome chr2 = genome.getChromosome(dres.chr2);
			Transcript tr1 = id2tr.get(dres.trId1);
			Transcript tr2 = id2tr.get(dres.trId2);

			String id = dres.getId();

			// All chromosomes and transcript found? => Add entries
			if (chr1 != null && chr2 != null && tr1 != null && tr2 != null) {

				// Gene1
				Gene gene1 = (Gene) tr1.getParent();
				gene1.getId();
				List<ProteinInteractionLocus> list = ProteinInteractionLocus.factory(tr1, dres.aaPos1, tr2, id);
				for (Marker m : list)
					gene1.addPerGene(m);

				// Since they act on different transcript (or different AAs within the
				// transcript), we
				// need to add two markers (one for each "side" of the interaction
				Gene gene2 = (Gene) tr2.getParent();
				gene2.getId();
				list = ProteinInteractionLocus.factory(tr2, dres.aaPos2, tr1, id);
				for (Marker m : list)
					gene2.addPerGene(m);
				count++;
			} else countSkipped++;
		}

		if (verbose) Log.info("\tInteractions: " + count + " added, " + countSkipped + " skipped.");
	}

	/**
	 * Read markers file Supported formats: BED, TXT, BigBed, GFF
	 */
	protected Markers loadMarkers(String fileName) {
		Markers markersSeqChange = Markers.readMarkers(fileName);
		String label = Gpr.removeExt(Gpr.baseName(fileName));

		// Convert markers to 'Custom' markers
		Markers markers = new Markers();
		for (Marker m : markersSeqChange) {
			if (m instanceof Custom) {
				((Custom) m).setLabel(label);
				markers.add(m);
			} else {
				// Not a custom interval? Create one
				Custom custom = new Custom(m.getParent(), m.getStart(), m.getEndClosed(), false, m.getId(), label);
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
		// Sanity checks
		String pwmsFileName = config.getDirDataGenomeVersion() + "/pwms.bin";
		String motifBinFileName = config.getBaseFileNameMotif() + ".bin";

		if (!Gpr.exists(pwmsFileName) || !Gpr.exists(motifBinFileName)) {
			if (verbose) Log.info("Loading Motifs and PWMs");

			// OK, we don't have motif annotations, no problem
			if (debug) {
				if (!Gpr.exists(pwmsFileName)) Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot open PWMs file '" + pwmsFileName + "'");
				if (!Gpr.exists(motifBinFileName)) Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot open Motifs file '" + motifBinFileName + "'");
			}
			return;
		}

		// Load all PWMs
		if (verbose) Log.info("Loading PWMs from : " + pwmsFileName);
		Jaspar jaspar = new Jaspar();
		jaspar.load(pwmsFileName);

		// Read motifs
		if (verbose) Log.info("Loading Motifs from file '" + motifBinFileName + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer(genome);
		Markers motifsDb = markerSerializer.load(motifBinFileName);

		// Add (only) motif markers. The original motifs has to be serialized with
		// Chromosomes, Genomes and other markers (otherwise it could have not been
		// saved)
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
				} else if (debug) Log.debug("Cannot find PWM for motif '" + motif.getPwmId() + "'");
			}

		if (verbose) Log.info("\tMotif database: " + countAddded + " markers loaded.");
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 */
	void loadNextProt() {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Read nextProt binary file
		String nextProtBinFile = config.getDirDataGenomeVersion() + "/nextProt.bin";
		if (!Gpr.canRead(nextProtBinFile)) {
			if (debug) Log.debug("NextProt database '" + nextProtBinFile + "' doesn't exist. Ignoring.");
			return;
		}
		if (verbose) Log.info("Reading NextProt database from file '" + nextProtBinFile + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer(genome);
		Markers nextProtDb = markerSerializer.load(nextProtBinFile);

		// Create a collection of (only) NextProt markers. The original nextProtDb has
		// Chromosomes, Genomes and other markers (otherwise it could have not been
		// saved)
		ArrayList<NextProt> nextProts = new ArrayList<>(nextProtDb.size());
		for (Marker m : nextProtDb)
			if (m instanceof NextProt) nextProts.add((NextProt) m);

		if (verbose) Log.info("NextProt database: " + nextProts.size() + " markers loaded.");

		// Connect nextProt annotations to transcripts and exons
		if (verbose) Log.info("Adding transcript info to NextProt markers.");

		// Create a list of all transcripts
		HashMap<String, Transcript> trs = new HashMap<>();
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
			// WARNING: The transcripts might be filtered out by the user
			// (e.g. '-cannon' command line option or user defined
			// sets). We only keep nextProt markers associated to found
			// transcripts. All others are discarded (the user doesn't
			// want that info).
			ArrayList<NextProt> nextProtsToAdd = new ArrayList<>();
			for (NextProt np : nextProts) {
				Transcript tr = trs.get(np.getTranscriptId());

				// Found transcript, now try to find an exon
				if (tr != null) {
					np.setParent(tr); // Set this transcript as parent
					nextProtsToAdd.add(np);
				}
			}

			// Add all nextProt marker to predictor
			for (NextProt np : nextProtsToAdd)
				snpEffectPredictor.add(np);

			// Note: We might end up with more markers than we loaded (just because they map
			// to multiple exons (although it would be highly unusual)
			if (verbose) Log.info("NextProt database: " + nextProtsToAdd.size() + " markers added.");
		}

	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 */
	void loadRegulationTrack(String regTrack) {
		// Read file
		if (verbose) Log.info("Reading regulation track '" + regTrack + "'");
		String regFile = config.getDirDataGenomeVersion() + "/regulation_" + regTrack + ".bin";
		Markers regulation = new Markers();
		regulation.load(regFile);

		// Are all chromosomes available?
		HashMap<String, Integer> chrs = new HashMap<>();
		for (Marker r : regulation) {
			String chr = r.getChromosomeName();
			int max = chrs.getOrDefault(chr, 0);
			max = Math.max(max, r.getEndClosed());
			chrs.put(chr, max);
		}

		// Add all chromosomes
		for (String chr : chrs.keySet())
			if (genome.getChromosome(chr) == null) genome.add(new Chromosome(genome, 0, chrs.get(chr), chr));

		// Add all markers to predictor
		config.getSnpEffectPredictor().addAll(regulation);
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args == null) {
			command = DEFAULT_COMMAND;
			return;
		}

		if (args.length <= 0) usage(null);

		int argNum = 0;

		// Parse command
		if (args[0].equalsIgnoreCase("ann") // Annotate: Same as 'eff'
				|| args[0].equalsIgnoreCase("build") //
				|| args[0].equalsIgnoreCase("buildNextProt") //
				|| args[0].equalsIgnoreCase("cds") //
				|| args[0].equalsIgnoreCase("closest") //
				|| args[0].equalsIgnoreCase("count") //
				|| args[0].equalsIgnoreCase("databases") //
				|| args[0].equalsIgnoreCase("download") //
				|| args[0].equalsIgnoreCase("dump") //
				|| args[0].equalsIgnoreCase("eff") //
				|| args[0].equalsIgnoreCase("genes2bed") //
				|| args[0].equalsIgnoreCase("gsa") //
				|| args[0].equalsIgnoreCase("len") //
				|| args[0].equalsIgnoreCase("pdb") //
				|| args[0].equalsIgnoreCase("protein") //
				|| args[0].equalsIgnoreCase("seq") //
				|| args[0].equalsIgnoreCase("show") //
				|| args[0].equalsIgnoreCase("test") //
				|| args[0].equalsIgnoreCase("translocreport") //
				// Obsolete stuff (from T2D projects)
				|| args[0].equalsIgnoreCase("acat") //
				|| args[0].equalsIgnoreCase("spliceAnalysis") //
		) {
			command = args[argNum++].trim().toLowerCase();
		}

		// Copy and parse arguments, except initial 'command'
		ArrayList<String> argsList = new ArrayList<>();
		for (int i = argNum; i < args.length; i++) {
			String arg = args[i];

			// These options are available for allow all commands
			// Is it a command line option?
			if (isOpt(arg)) {
				switch (arg.toLowerCase()) {
				case "-c":
				case "-config":
					if ((i + 1) < args.length) configFile = args[++i];
					else usage("Option '-c' without config file argument");
					break;

				case "-configoption":
					if ((i + 1) < args.length) {
						String nameValue = args[++i];
						String[] nv = nameValue.split("=", 2);
						if (nv.length > 0) configOverride.put(nv[0], nv[1]);
						else usage("Cannot parse config option (expected format 'name=value'): " + nameValue);
					} else usage("Option '-configOption' without argument");
					break;

				case "-canon":
					canonical = true; // Use canonical transcripts
					break;

				case "-canonlist":
					if ((i + 1) < args.length) canonicalFile = args[++i];
					else usage("Option '-canonList' without file argument");
					break;

				case "-d":
				case "-debug":
					debug = verbose = true;
					break;

				case "-datadir":
					if ((i + 1) < args.length) dataDir = args[++i];
					else usage("Option '-dataDir' without data_dir argument");
					break;

				case "-download":
					download = true; // Download genome if not locally available
					break;

				case "-h":
				case "-help":
					help = true;
					if (command.isEmpty()) usage(null); // Help was invoked without a specific command: Show generic help
					break;

				case "-interaction":
					interaction = true; // Use interaction database
					break;

				case "-hgvs":
					hgvs = hgvsForce = true; // Use HGVS notation
					break;

				case "-hgvsold":
					hgvsOld = true;
					break;

				case "-hgvs1letteraa":
				case "-hgvsoneletteraa":
					hgvsOneLetterAa = true;
					break;

				case "-hgvstrid":
					hgvsTrId = true;
					break;

				case "-interval":
					if ((i + 1) < args.length) customIntervalFiles.add(args[++i]);
					else usage("Option '-interval' without config interval_file argument");
					break;

				case "-maxtsl":
					if ((i + 1) < args.length) maxTranscriptSupportLevel = TranscriptSupportLevel.parse(args[++i]);
					else usage("Option '-maxTSL' without config transcript_support_level argument");
					break;

				case "-motif":
					motif = true; // Use motif database
					break;

				case "-noexpandiub":
					expandIub = false; // Do not expand IUB codes
					break;

				case "-nogenome":
					noGenome = true; // Do not load genome
					break;

				case "-nointeraction":
					interaction = false; // Do not use interaction database
					break;

				case "-nomotif":
					motif = false; // Disable use of motif database
					break;

				case "-nextprot":
					nextProt = true; // Use NextProt database
					break;

				case "-nonextprot":
					nextProt = false; // Disable use of NextProt database
					break;

				case "-nodownload":
					download = false; // Do not download genome
					break;

				case "-nolog":
					log = false;
					break;

				case "-noout":
					// Undocumented option (only used for development & debugging)
					suppressOutput = true;
					break;

				case "-onlyreg":
					onlyRegulation = true;
					break;

				case "-onlyprotein":
					onlyProtein = true;
					break;

				case "-onlytr":
					if ((i + 1) < args.length) onlyTranscriptsFile = args[++i]; // Only use the transcripts in this file
					else usage("Option '-onltTr' without file argument");
					break;

				case "-q":
				case "-quiet":
					quiet = true;
					verbose = false;
					break;

				case "-reg":
					if ((i + 1) < args.length) addRegulationTrack(args[++i]); // Add this track to the list
					else usage("Option '-reg' without file argument");
					break;

				case "-ss":
				case "-splicesitesize":
					if ((i + 1) < args.length) spliceSiteSize = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-spliceSiteSize' without argument");
					break;

				case "-spliceregionexonsize":
					if ((i + 1) < args.length) spliceRegionExonSize = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-spliceRegionExonSize' without argument");
					break;

				case "-spliceregionintronmin":
					if ((i + 1) < args.length) spliceRegionIntronMin = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-spliceRegionIntronMin' without argument");
					break;

				case "-spliceregionintronmax":
					if ((i + 1) < args.length) spliceRegionIntronMax = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-spliceRegionIntronMax' without argument");
					break;

				case "-strict":
					strict = true;
					break;

				//				case "-t":
				//					multiThreaded = true;
				//					break;

				case "-treatallasproteincoding":
					if ((i + 1) < args.length) {
						i++;
						if (args[i].equalsIgnoreCase("auto")) treatAllAsProteinCoding = null;
						else treatAllAsProteinCoding = Gpr.parseBoolSafe(args[i]);
					}
					break;

				case "-ud":
				case "-updownstreamlen":
					if ((i + 1) < args.length) upDownStreamLength = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-upDownstreamLen' without argument");
					break;

				case "-v":
				case "-verbose":
					verbose = true;
					quiet = false;
					break;

				case "-version":
					// Show version number and exit
					System.out.println(SOFTWARE_NAME + "\t" + VERSION_SHORT + "\t" + BUILD_DATE);
					System.exit(0);
					break;

				default:
					// Unrecognized option? may be it's command specific. Let command parse it
					argsList.add(arg);
				}
			} else {
				// Command specific argument: Let command parse it
				argsList.add(arg);
			}
		}

		shiftArgs = argsList.toArray(new String[0]);

		if (command.isEmpty()) command = DEFAULT_COMMAND; // Default command is 'ann'

		// Show version and command
		if (!help && (verbose || debug)) {
			Log.info("SnpEff version " + VERSION);
			Log.info("Command: '" + command + "'");
		}
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
		HashMap<String, String> reportValues = new HashMap<>();
		return reportValues;
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		SnpEff snpEffCmd = cmd();
		if (snpEffCmd == null) return true;

		// Run
		boolean ok = false;
		StringBuilder err = new StringBuilder();
		try {
			ok = snpEffCmd.run();
		} catch (Throwable t) {
			ok = false;
			err.append(t.getMessage());
			t.printStackTrace();
		}

		// Update config if needed
		if (config == null) config = snpEffCmd.getConfig();

		// Report to server (usage statistics)
		if (log) {
			// Log to server
			LogStats.report(SOFTWARE_NAME, VERSION_BUILD, VERSION, ok, verbose, args, err.toString(), snpEffCmd.reportValues());

			// Check for new version (use config file from command, since this one doesn't
			// load a config file)
			checkNewVersion(snpEffCmd.config);
		}

		if (verbose) Log.info("Done.");
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

	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setGenomeVer(String genomeVer) {
		this.genomeVer = genomeVer;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public void setNextProt(boolean nextProt) {
		this.nextProt = nextProt;
	}

	public void setNextProtKeepAllTrs(boolean nextProtKeepAllTrs) {
		this.nextProtKeepAllTrs = nextProtKeepAllTrs;
	}

	public void setShiftHgvs(boolean shiftHgvs) {
		hgvsShift = shiftHgvs;
	}

	public void setSpliceSiteSize(int spliceSiteSize) {
		this.spliceSiteSize = spliceSiteSize;
	}

	public void setSupressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	public void setUpDownStreamLength(int upDownStreamLength) {
		this.upDownStreamLength = upDownStreamLength;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Show 'usage' message and exit with an error code '-1'
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("SnpEff version " + VERSION);
		System.err.println("Usage: snpEff [command] [options] [files]");
		System.err.println("\nRun 'java -jar snpEff.jar command' for help on each specific command");
		System.err.println("\nAvailable commands: ");
		System.err.println("\t[eff|ann]                    : Annotate variants / calculate effects (you can use either 'ann' or 'eff', they mean the same). Default: ann (no command or 'ann').");
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
		System.err.println("\tpdb                          : Build interaction database (based on PDB data).");
		System.err.println("\tprotein                      : Compare protein sequences calculated form a SnpEff database to the one in a FASTA file. Used for checking databases correctness.");
		System.err.println("\tseq                          : Show sequence (from command line) translation.");
		System.err.println("\tshow                         : Show a text representation of genes or transcripts coordiantes, DNA sequence and protein sequence.");
		System.err.println("\ttranslocReport               : Create a translocations report (from VCF file).");
		// System.err.println("\tspliceAnalysis : Perform an analysis of splice sites.
		// Experimental feature.");

		usageGenericAndDb();

		System.exit(-1);
	}

	/**
	 * Show database load and build options
	 */
	protected void usageDb() {
		System.err.println("\nDatabase options:");
		System.err.println("\t-canon                       : Only use canonical transcripts.");
		System.err.println("\t-canonList <file>            : Only use canonical transcripts, replace some transcripts using the 'gene_id \t transcript_id' entries in <file>.");
		System.err.println("\t-interaction                 : Annotate using interactions (requires interaction database). Default: " + interaction);
		System.err.println("\t-interval <file>             : Use a custom intervals in TXT/BED/BigBed/VCF/GFF file (you may use this option many times)");
		System.err.println("\t-maxTSL <TSL_number>         : Only use transcripts having Transcript Support Level lower than <TSL_number>.");
		System.err.println("\t-motif                       : Annotate using motifs (requires Motif database). Default: " + motif);
		System.err.println("\t-nextProt                    : Annotate using NextProt (requires NextProt database).");
		System.err.println("\t-noGenome                    : Do not load any genomic database (e.g. annotate using custom files).");
		System.err.println("\t-noExpandIUB                 : Disable IUB code expansion in input variants");
		System.err.println("\t-noInteraction               : Disable inteaction annotations");
		System.err.println("\t-noMotif                     : Disable motif annotations.");
		System.err.println("\t-noNextProt                  : Disable NextProt annotations.");
		System.err.println("\t-onlyReg                     : Only use regulation tracks.");
		System.err.println("\t-onlyProtein                 : Only use protein coding transcripts. Default: " + onlyProtein);
		System.err.println("\t-onlyTr <file.txt>           : Only use the transcripts in this file. Format: One transcript ID per line.");
		System.err.println("\t-reg <name>                  : Regulation track to use (this option can be used add several times).");
		System.err.println("\t-ss , -spliceSiteSize <int>  : Set size for splice sites (donor and acceptor) in bases. Default: " + spliceSiteSize);
		System.err.println("\t-spliceRegionExonSize <int>  : Set size for splice site region within exons. Default: " + spliceRegionExonSize + " bases");
		System.err.println("\t-spliceRegionIntronMin <int> : Set minimum number of bases for splice site region within intron. Default: " + spliceRegionIntronMin + " bases");
		System.err.println("\t-spliceRegionIntronMax <int> : Set maximum number of bases for splice site region within intron. Default: " + spliceRegionIntronMax + " bases");
		System.err.println("\t-strict                      : Only use 'validated' transcripts (i.e. sequence has been checked). Default: " + strict);
		System.err.println("\t-ud , -upDownStreamLen <int> : Set upstream downstream interval length (in bases)");
	}

	/**
	 * Show generic options
	 */
	protected void usageGeneric() {
		System.err.println("\nGeneric options:");
		System.err.println("\t-c , -config                 : Specify config file");
		System.err.println("\t-configOption name=value     : Override a config file option");
		System.err.println("\t-d , -debug                  : Debug mode (very verbose).");
		System.err.println("\t-dataDir <path>              : Override data_dir parameter from config file.");
		System.err.println("\t-download                    : Download a SnpEff database, if not available locally. Default: " + download);
		System.err.println("\t-nodownload                  : Do not download a SnpEff database, if not available locally.");
		System.err.println("\t-h , -help                   : Show this help and exit");
		System.err.println("\t-noLog                       : Do not report usage statistics to server");
		System.err.println("\t-q , -quiet                  : Quiet mode (do not show any messages or errors)");
		//		System.err.println("\t-t                           : Use multiple threads (implies '-noStats'). Default 'off'");
		System.err.println("\t-v , -verbose                : Verbose mode");
		System.err.println("\t-version                     : Show version number and exit");
	}

	protected void usageGenericAndDb() {
		usageGeneric();
		usageDb();
	}

}
