package org.snpeff.snpEffect.commandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.snpeff.SnpEff;
import org.snpeff.collections.AutoHashMap;
import org.snpeff.fileIterator.BedFileIterator;
import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.geneSets.GeneSets;
import org.snpeff.geneSets.GeneSetsRanked;
import org.snpeff.geneSets.GeneStats;
import org.snpeff.geneSets.algorithm.EnrichmentAlgorithm;
import org.snpeff.geneSets.algorithm.EnrichmentAlgorithm.EnrichmentAlgorithmType;
import org.snpeff.geneSets.algorithm.EnrichmentAlgorithmGreedyVariableSize;
import org.snpeff.geneSets.algorithm.FisherPValueAlgorithm;
import org.snpeff.geneSets.algorithm.FisherPValueGreedyAlgorithm;
import org.snpeff.geneSets.algorithm.LeadingEdgeFractionAlgorithm;
import org.snpeff.geneSets.algorithm.NoneAlgorithm;
import org.snpeff.geneSets.algorithm.RankSumPValueAlgorithm;
import org.snpeff.geneSets.algorithm.RankSumPValueGreedyAlgorithm;
import org.snpeff.gsa.ChrPosScoreList;
import org.snpeff.gsa.PvaluesList;
import org.snpeff.gsa.ScoreList;
import org.snpeff.gsa.ScoreList.ScoreSummary;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantWithScore;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

/**
 * Command line: Gene-Sets Analysis
 *
 * Perform gene set analysys
 *
 * @author pcingola
 */
public class SnpEffCmdGsa extends SnpEff {

	public static int READ_INPUT_SHOW_EVERY = 1000;
	public static int MAX_WARNS = 20;

	InputFormat inputFormat = InputFormat.VCF;
	boolean useClosestGene = false; // Map to 'any' closest gene?
	boolean useGeneId = false; // Use geneId instead of geneName
	boolean usePvalues = true;
	boolean removeUnusedSets = false;
	boolean orderDescending = false; // If 'true', high scores are better (sort descending and get the first values)
	int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH;
	int minGeneSetSize = 0;
	int maxGeneSetSize = Integer.MAX_VALUE;
	int numberofGeneSetsToSelect = 20;
	int initGeneSetSize = 100;
	int randIterations = 0;
	double maxPvalueAdjusted = 0.05;
	double maxPvalue = Double.NaN;
	double interestingPerc = 0.05;
	String inputFile = "";
	String infoName = "";
	String msigdb = "";
	String geneScoreFile = "";
	String geneScoreFileSave = null;
	String commandsFile = null;
	String geneInterestingFile = "";
	String saveFile = null;
	String correctionCmd = null;
	ScoreSummary scoreSummary = ScoreSummary.MIN;
	SnpEffectPredictor snpEffectPredictor;
	Genome genome;
	GeneSets geneSets;
	ChrPosScoreList chrPosScoreList; // List of <chr, pos, score>
	AutoHashMap<String, ScoreList> geneScores; // A map of geneId -> List[scores]
	HashMap<String, Double> geneScore; // A <gene, score> map
	HashSet<String> genesInteresting; // A set of interesting genes
	EnrichmentAlgorithmType enrichmentAlgorithmType = EnrichmentAlgorithmType.RANKSUM_GREEDY;

	public SnpEffCmdGsa() {
		super();
	}

	/**
	 * Read config file, load & build database
	 */
	protected void config() {
		loadConfig();

		// Read database (if gene level scores are provided, we don't need to map p_values to genes (we can skip this step)
		if (geneScoreFile.isEmpty() && geneInterestingFile.isEmpty()) {
			loadDb();

			// Set upstream-downstream interval length
			SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
			snpEffectPredictor.setUpDownStreamLength(upDownStreamLength);

			// Build tree
			if (verbose) Log.info("Building interval forest");
			snpEffectPredictor.buildForest();
			if (verbose) Log.info("done.");
		}
	}

	/**
	 * Correct scores (e.g. using covariates)
	 */
	void correctScores() {
		// Assign input and output file names (scores and residues files)
		String scoresFile = geneScoreFileSave;
		String residuesFile;
		try {
			if (geneScoreFileSave == null) {
				// No predefined name
				scoresFile = File.createTempFile("geneScoreFile_in_", ".txt").getCanonicalPath();
				residuesFile = File.createTempFile("geneScoreFile_out_", ".txt").getCanonicalPath();

				// Delete tmp files on exit
				new File(scoresFile).deleteOnExit();
				new File(residuesFile).deleteOnExit();
			} else {
				// Predefined name, use residues file accordingly
				residuesFile = Gpr.dirName(geneScoreFileSave) + "/" + Gpr.baseName(geneScoreFileSave) + ".corrected.txt";
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// No correction method? No scores?
		// Nothing to do
		if (correctionCmd == null || geneScores.isEmpty()) {
			if (geneScoreFileSave != null) {
				createScoresFile(geneScoreFileSave, false); // may be we were asked to write these files anyway...
				createScoresFile(residuesFile, true); // Only scores in this files (no additional information). Since we don't perform correction, residues and scores are the same
			}
			return;
		}

		// Create scores files
		createScoresFile(scoresFile, false);

		//---
		// Invoke method
		//---
		String commandLine = correctionCmd + " " + scoresFile + " " + residuesFile;
		try {
			if (verbose) Log.info("Correction: Invoking command " + commandLine);
			Process process = Runtime.getRuntime().exec(commandLine);
			process.waitFor();
			if (process.exitValue() > 0) throw new RuntimeException("Process execution error, exit value '" + process.exitValue() + "'\n\tCommand line:\t" + commandLine);
		} catch (Exception e) {
			throw new RuntimeException("Error executing command: " + commandLine, e);
		}

		//---
		// Read output
		//---
		if (verbose) Log.info("Correction: Reading results from file '" + residuesFile + "'");
		if (!Gpr.canRead(residuesFile)) throw new RuntimeException("Cannot read correction's results from file '" + residuesFile + "'");
		geneScore = new HashMap<String, Double>();
		String lines[] = Gpr.readFile(residuesFile).split("\n");
		for (String line : lines) {
			// Parse line
			String recs[] = line.split("\t");
			String geneId = recs[0];
			double score = Gpr.parseDoubleSafe(recs[1]);

			// Add to map
			geneScore.put(geneId, score);
		}

		if (verbose) Log.info("Correction: Done, " + lines.length + " values added.");
	}

	/**
	 * Create interesting genes
	 */
	void createInterestingGenes() {
		if (!geneInterestingFile.isEmpty()) createInterestingGenesFile();
		else createInterestingGenesScores();
	}

	/**
	 * Create interesting genes
	 */
	void createInterestingGenesFile() {
		int hasGene = 0;

		// Interesting genes from file
		for (String geneId : genesInteresting) {
			if (geneSets.hasGene(geneId)) hasGene++;
			geneSets.addInteresting(geneId);
		}

		if (verbose) {
			Log.info("Intereting genes from file" //
					+ "\n\tIntereting genes in file  : " + genesInteresting.size() //
					+ "\n\tFound genes               : " + hasGene //
			);
		}
	}

	/**
	 * Create interesting genes
	 */
	void createInterestingGenesScores() {
		// Get
		ScoreList scores = new ScoreList();
		for (double pval : geneScore.values())
			scores.add(pval);

		// Get p-value threshold
		double quantile = interestingPerc;
		if (orderDescending) quantile = 1 - interestingPerc;

		double scoreThreshold = scores.quantile(quantile);

		// Mark all scores lower than that as 'interesting'
		int count = 0, countAdded = 0;
		geneSets.setDoNotAddIfNotInGeneSet(true);
		for (String geneId : geneScore.keySet()) {

			if ((orderDescending && (geneScore.get(geneId) >= scoreThreshold)) //
					|| (!orderDescending && (geneScore.get(geneId) <= scoreThreshold)) //
			) {
				if (geneSets.addInteresting(geneId)) countAdded++; // Count added genes
				count++;
			}
		}
		// Show info
		if (verbose) {
			double realPerc = (100.0 * count) / geneScore.size();
			double realPercAdded = (100.0 * countAdded) / geneScore.size();
			Log.info(String.format("Score threshold:"//
					+ "\n\tRange                    : [ %f , %f ]"//
					+ "\n\tQuantile                 : %.2f%%"//
					+ "\n\tThreshold                : %f"//
					+ "\n\tInteresting genes        : %d  (%.2f%%)" //
					+ "\n\tInteresting genes added  : %d  (%.2f%%)" //
					, scores.min(), scores.max(), 100.0 * interestingPerc, scoreThreshold, count, realPerc, countAdded, realPercAdded));
		}
	}

	/**
	 * Creates a file with scores and several gene values.
	 * Users can create regression algorithms (e.g. in R) and return the residuals
	 *
	 * @param fileName
	 */
	void createScoresFile(String fileName, boolean scoresOnly) {
		StringBuilder scores = new StringBuilder();
		if (geneScoreFileSave != null) {
			scores.append("geneId\tscore");
			if (!scoresOnly) scores.append("\tscoreCount\t" + (new GeneStats()).title() + "\n");
			scores.append("\n");
		}

		// Calculate statistics on each gene
		AutoHashMap<String, GeneStats> genesStats = new AutoHashMap<String, GeneStats>(new GeneStats());
		for (Gene gene : genome.getGenes()) {
			String geneName = useGeneId ? gene.getId() : gene.getGeneName();
			genesStats.getOrCreate(geneName).add(gene, useGeneId);
		}

		// Add all genes to output buffer
		for (String geneId : geneScores.keySet()) {
			// Calculate aggregated score
			ScoreList gpl = geneScores.get(geneId);
			double score = gpl.score(scoreSummary);

			// Add to output
			scores.append(geneId + "\t" + score);
			if (!scoresOnly) scores.append("\t" + gpl.size() + "\t" + genesStats.getOrCreate(geneId));
			scores.append("\n");
		}

		// Save to file
		if (verbose) Log.info("Saving gene scores to file: '" + fileName + "'");
		Gpr.toFile(fileName, scores);
	}

	/**
	 * Perform enrichment analysis
	 */
	void enrichmentAnalysis() {
		GeneSetsRanked geneSetsRanked = null;

		// Initialize gene set values
		if (geneScore != null) {
			for (String geneId : geneScore.keySet())
				geneSets.setValue(geneId, geneScore.get(geneId));

			// Do we need to rank? Rank them by ascending p-value
			geneSets.setVerbose(verbose);
			if (enrichmentAlgorithmType.isRank()) {
				geneSetsRanked = new GeneSetsRanked(geneSets);
				geneSetsRanked.rankByValue(!orderDescending);

				if (removeUnusedSets) geneSetsRanked.removeUnusedSets(); // Remove unused gene sets
			} else {
				if (removeUnusedSets) geneSets.removeUnusedSets(); // Remove unused gene sets
			}
		}

		//---
		// Run enrichment algorithm
		//---
		EnrichmentAlgorithm algorithm = null;

		switch (enrichmentAlgorithmType) {
		case NONE:
			algorithm = new NoneAlgorithm(geneSets);
			break;

		case RANKSUM_GREEDY:
			algorithm = new RankSumPValueGreedyAlgorithm(geneSetsRanked, numberofGeneSetsToSelect);
			break;

		case RANKSUM:
			algorithm = new RankSumPValueAlgorithm(geneSetsRanked, numberofGeneSetsToSelect);
			break;

		case FISHER_GREEDY:
			algorithm = new FisherPValueGreedyAlgorithm(geneSets, numberofGeneSetsToSelect);
			break;

		case FISHER:
			algorithm = new FisherPValueAlgorithm(geneSets, numberofGeneSetsToSelect);
			break;

		case LEADING_EDGE_FRACTION:
			algorithm = new LeadingEdgeFractionAlgorithm(geneSets, numberofGeneSetsToSelect, orderDescending);
			break;

		default:
			throw new RuntimeException("Unimplemented algorithm!");
		}

		// Create 'interesting' genes
		if (enrichmentAlgorithmType.isBinary()) createInterestingGenes();

		// Initialize algorithm parameters
		algorithm.setMaxGeneSetSize(maxGeneSetSize);
		algorithm.setMinGeneSetSize(minGeneSetSize);
		algorithm.setMaxPValue(maxPvalue);
		algorithm.setMaxPvalueAdjusted(maxPvalueAdjusted);
		algorithm.setVerbose(verbose);
		algorithm.setDebug(debug);

		// if (enrichmentAlgorithmType.isRank() && enrichmentAlgorithmType.isGreedy()) {
		if (enrichmentAlgorithmType.isGreedy()) {
			if (debug) Log.debug("Setting initGeneSetSize:" + initGeneSetSize);
			((EnrichmentAlgorithmGreedyVariableSize) algorithm).setInitialSize(initGeneSetSize);
		}

		// Run algorithm
		algorithm.select();

		if (saveFile != null) {
			if (verbose) Log.info("Saving results to '" + saveFile + "'");
			Gpr.toFile(saveFile, algorithm.getOutput());
		}
	}

	/**
	 * Initialize: read config, database, etc.
	 */
	void initialize() {
		// Read config file
		if (config == null) config();

		// Read database (if gene level scores are provided, we don't neet to map p_values to genes (we can skip this step)
		if (geneScoreFile.isEmpty() && geneInterestingFile.isEmpty()) {
			snpEffectPredictor = config.getSnpEffectPredictor();
			genome = config.getGenome();
		}

		// Read gene set database
		if (verbose) Log.info("Reading MSigDb from file: '" + msigdb + "'");
		geneSets = enrichmentAlgorithmType.isRank() ? new GeneSetsRanked(msigdb) : new GeneSets(msigdb);
		if (verbose) Log.info("Done." //
				+ "\n\t\tGene sets added : " + geneSets.getGeneSetCount() //
				+ "\n\t\tGenes added     : " + geneSets.getGeneCount() //
		);
	}

	/**
	 * Map <chr,pos, score> to gene
	 */
	void mapToGenes() {
		if (verbose) Log.info("Mapping scores to genes.");

		// Create an auto-hash
		if (usePvalues) geneScores = new AutoHashMap<String, ScoreList>(new PvaluesList());
		else geneScores = new AutoHashMap<String, ScoreList>(new ScoreList());

		//---
		// Map every chr:pos
		//---
		int unmapped = 0, mappedMultiple = 0;
		for (int i = 0; i < chrPosScoreList.size(); i++) {
			List<String> geneIds = mapToGenes(chrPosScoreList.getChromosomeName(i), chrPosScoreList.getStart(i), chrPosScoreList.getEnd(i));

			// Update counters
			if (geneIds == null || geneIds.isEmpty()) {
				unmapped++;
				continue; // Nothing to do...
			} else if (geneIds.size() > 1) mappedMultiple++;

			// Add score to every geneId
			double score = chrPosScoreList.getScore(i);
			for (String geneId : geneIds) {
				ScoreList gpl = geneScores.getOrCreate(geneId);
				gpl.setGeneId(geneId);
				gpl.add(score);
			}
		}

		//---
		// Show a summary
		//---
		if (verbose) Log.info("Done:" //
				+ "\n\tNumber of scores         : " + chrPosScoreList.size() //
				+ "\n\tUnmapped                 : " + unmapped //
				+ "\n\tMapped to multiple genes : " + mappedMultiple //
		);

		if (debug) {
			System.err.println("Mapping Gene to Score:");
			ArrayList<String> geneIds = new ArrayList<String>(geneScores.keySet());
			Collections.sort(geneIds);
			for (String geneId : geneIds)
				System.err.println("\t" + geneScores.get(geneId));
		}
	}

	/**
	 * Map a position to a geneId
	 * @param chr
	 * @param start
	 * @return
	 */
	List<String> mapToGenes(String chr, int start, int end) {
		LinkedList<String> geneIds = new LinkedList<String>();

		// Query
		Marker m = new Marker(genome.getChromosome(chr), start, end, false, "");

		// Map only to closest gene?
		if (useClosestGene) {
			Gene gene = snpEffectPredictor.queryClosestGene(m);
			if (gene != null) geneIds.add(useGeneId ? gene.getId() : gene.getGeneName());
			return geneIds;
		}

		// Add all genes to list
		Markers hits = snpEffectPredictor.query(m);
		for (Marker mm : hits) {
			if (mm instanceof Gene) {
				Gene gene = (Gene) mm;
				geneIds.add(useGeneId ? gene.getId() : gene.getGeneName());
			}
		}

		return geneIds;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args.length == 0) usage(null);

		// Parse comamnd line
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Is it an option?
			if (isOpt(arg)) {

				if (arg.equals("-i")) {
					// Input format
					if ((i + 1) < args.length) inputFormat = InputFormat.valueOf(args[++i].toUpperCase());
					else usage("Missing input format in command line option '-i'");
				} else if (arg.equals("-info")) {
					// INFO field name
					if ((i + 1) < args.length) infoName = args[++i];
					else usage("Missing value in command line option '-info'");
				} else if (arg.equals("-ud") || arg.equalsIgnoreCase("-upDownStreamLen")) {
					// Up-downstream length
					if ((i + 1) < args.length) upDownStreamLength = Gpr.parseIntSafe(args[++i]);
					else usage("Missing value in command line option '-ud'");
				} else if (arg.equals("-geneScore")) {
					// Method for p-value scoring (gene level)
					if ((i + 1) < args.length) {
						String method = args[++i].toUpperCase();
						scoreSummary = ScoreSummary.valueOf(method);
					} else usage("Missing value in command line option '-geneScore'");
				} else if (arg.equals("-algo")) {
					// Algorithm to use
					if ((i + 1) < args.length) {
						String algo = args[++i].toUpperCase();
						enrichmentAlgorithmType = EnrichmentAlgorithmType.valueOf(algo);
					} else usage("Missing value in command line option '-algo'");
				} else if (arg.equals("-geneScoreFile")) {
					// Read gene scores from file
					if ((i + 1) < args.length) geneScoreFile = args[++i];
					else usage("Missing value in command line option '-geneScoreFile'");
				} else if (arg.equals("-saveGeneScoreFile")) {
					// Save gene scores to file
					if ((i + 1) < args.length) geneScoreFileSave = args[++i];
					else usage("Missing value in command line option '-saveGeneScoreFile'");
				} else if (arg.equals("-commands")) {
					// Load multiple commands from file
					if ((i + 1) < args.length) commandsFile = args[++i];
					else usage("Missing value in command line option '-commands'");
				} else if (arg.equals("-save")) {
					// Save results to file
					if ((i + 1) < args.length) saveFile = args[++i];
					else usage("Missing value in command line option '-save'");
				} else if (arg.equals("-correction")) {
					// Save results to file
					if ((i + 1) < args.length) correctionCmd = args[++i];
					else usage("Missing value in command line option '-correction'");
				} else if (arg.equals("-maxPvalue")) {
					// Save results to file
					if ((i + 1) < args.length) maxPvalue = Gpr.parseDoubleSafe(args[++i]);
					else usage("Missing value in command line option '-maxPvalue'");
				} else if (arg.equals("-maxPvalueAdj")) {
					// Save results to file
					if ((i + 1) < args.length) maxPvalueAdjusted = Gpr.parseDoubleSafe(args[++i]);
					else usage("Missing value in command line option '-maxPvalueAdj'");
				} else if (arg.equals("-geneInterestingFile")) {
					// Algorithm to use
					if ((i + 1) < args.length) geneInterestingFile = args[++i];
					else usage("Missing value in command line option '-geneScoreFile'");
				} else if (arg.equals("-minSetSize")) minGeneSetSize = Gpr.parseIntSafe(args[++i]);
				else if (arg.equals("-maxSetSize")) maxGeneSetSize = Gpr.parseIntSafe(args[++i]);
				else if (arg.equals("-initSetSize")) initGeneSetSize = Gpr.parseIntSafe(args[++i]);
				else if (arg.equals("-rand")) randIterations = Gpr.parseIntSafe(args[++i]);
				else if (arg.equals("-interesting")) interestingPerc = Gpr.parseDoubleSafe(args[++i]);
				else if (arg.equals("-mapClosestGene")) useClosestGene = true;
				else if (arg.equals("-geneId")) useGeneId = true;
				else if (arg.equals("-score")) usePvalues = false;
				else if (arg.equals("-desc")) orderDescending = true; // Sort descending: High scores are better
				else usage("Unknown option '" + arg + "'");

			} else if (genomeVer.isEmpty()) genomeVer = arg;
			else if (msigdb.isEmpty()) msigdb = arg;
			else if (inputFile.isEmpty()) inputFile = arg;
		}

		//---
		// Sanity checks
		//---
		if (genomeVer.isEmpty() && geneScoreFile.isEmpty() && geneInterestingFile.isEmpty()) usage("Missing genome version.");

		if (commandsFile == null) {
			// All these check are only performed when "commands" is not set
			if ((inputFormat == InputFormat.VCF) && infoName.isEmpty() && geneScoreFile.isEmpty() && geneInterestingFile.isEmpty()) usage("Missing '-info' comamnd line option.");

			if (inputFile.isEmpty()) inputFile = "-"; // Default is STDIN
			if (!Gpr.canRead(inputFile)) Log.fatalError("Cannot read input file '" + inputFile + "'");

			if (msigdb.isEmpty()) Log.fatalError("Missing Gene-Sets file");
			if (!Gpr.canRead(msigdb)) Log.fatalError("Cannot read Gene-Sets file '" + msigdb + "'");

			if (maxGeneSetSize <= 0) usage("MaxSetSize must be a positive number.");
			if (minGeneSetSize >= maxGeneSetSize) usage("MaxSetSize (" + maxGeneSetSize + ") must larger than MinSetSize (" + minGeneSetSize + ").");

			if ((interestingPerc < 0) || (interestingPerc > 1)) usage("Interesting percentile must be in the [0 , 1.0] range.");

			if (!geneInterestingFile.isEmpty() && !enrichmentAlgorithmType.isBinary()) usage("Cannot specify '-geneInterestingFile' using algorithm '" + enrichmentAlgorithmType + "'");
		} else {
			if (!Gpr.canRead(commandsFile)) Log.fatalError("Cannot read commands file '" + commandsFile + "'");
		}

	}

	/**
	 * Read interesting genes from file
	 * @param geneScoreFile
	 */
	void readGeneInteresting(String geneScoreFile) {
		if (verbose) Log.info("Reading interesting genes file '" + geneInterestingFile + "'");
		String lines[] = Gpr.readFile(geneScoreFile).split("\n");
		genesInteresting = new HashSet<String>();
		for (String g : lines)
			genesInteresting.add(g.trim());
		if (verbose) Log.info("Done. Added: " + genesInteresting.size());
	}

	/**
	 * Read gene-Score file
	 * Format: "geneId \t p_value \n"
	 *
	 * @param geneScoreFile
	 */
	void readGeneScores(String geneScoreFile) {
		if (verbose) Log.info("Reading gene scores file '" + geneScoreFile + "'");

		geneScore = new HashMap<String, Double>();

		// Read the whole file
		String lines[] = Gpr.readFile(geneScoreFile).split("\n");

		// Parse each line
		double minp = Double.POSITIVE_INFINITY, maxp = Double.NEGATIVE_INFINITY;
		for (String line : lines) {
			String rec[] = line.split("\\s");
			String geneId = rec[0].trim();
			double score = Gpr.parseDoubleSafe(rec[1]);

			if ((score > 0) && (score <= 1.0)) { // Assume that a p-value of zero is a parsing error
				geneScore.put(geneId, score);
				minp = Math.min(minp, score);
				maxp = Math.max(maxp, score);
			} else if (verbose) Log.info("\tWarning: Ignoring entry (zero p-value):\t'" + line + "'");
		}

		if (verbose) Log.info("Done."//
				+ "\n\tScores added        : " + geneScore.size() //
				+ "\n\tMin score (p-value) : " + minp //
				+ "\n\tMax score (p-value) : " + maxp //
		);
	}

	/**
	 * Read input file and populate 'chrPosScoreList'
	 */
	void readInput() {
		if (verbose) Log.info("Reading input file '" + inputFile + "' (format '" + inputFormat + "')");

		switch (inputFormat) {
		case VCF:
			chrPosScoreList = readInputVcf();
			break;

		case BED:
			chrPosScoreList = readInputBed();
			break;

		default:
			Log.fatalError("Input format '" + inputFormat + "' not supported!");
		}

		if (verbose) {
			System.err.println("");
			Log.info("Done.");
		}

		if (debug) {
			// Show data
			System.err.println("scores:\n\tchr\tstart\tend\tp_value");
			for (int i = 0; i < chrPosScoreList.size(); i++)
				System.err.println("\t" + chrPosScoreList.getChromosomeName(i) + "\t" + chrPosScoreList.getStart(i) + "\t" + chrPosScoreList.getEnd(i) + "\t" + chrPosScoreList.getScore(i));
		}
	}

	/**
	 * Read input in BED format
	 *
	 * Format: "chr \t start \t end \t id \t Score \n"
	 *         start : zero-based
	 *         end   : zero-based open
	 *
	 */
	ChrPosScoreList readInputBed() {
		ChrPosScoreList cppList = new ChrPosScoreList();

		int num = 1;
		BedFileIterator bfi = new BedFileIterator(inputFile);
		for (Variant sc : bfi) {
			cppList.add(sc.getChromosome(), sc.getStart(), sc.getEndClosed(), ((VariantWithScore) sc).getScore());
			if (verbose) Gpr.showMark(num++, READ_INPUT_SHOW_EVERY);
		}

		return cppList;
	}

	/**
	 * Read input in TXT format
	 *
	 * Format: "chr \t pos \t score \n"
	 *
	 * Note: BED format  + score (0-based open close interval)
	 */
	ChrPosScoreList readInputTxt() {
		ChrPosScoreList cppList = new ChrPosScoreList();
		Genome genome = config.getGenome();

		int num = 1;
		LineFileIterator lfi = new LineFileIterator(inputFile);
		for (String line : lfi) {
			if (line.startsWith("#")) continue; // Ignore lines that start with '#'
			String fields[] = line.split("\t");

			// Sanity check
			if (fields.length < 3) {
				System.err.println("Warning: Ignoring line number " + lfi.getLineNum() + "." //
						+ " Exepcting format 'chr \t pos \t score \n'.\n" //
						+ "\tLine:\t'" + line + "'" //
				);
				continue;
			}

			// Parse fields
			String chr = fields[0];
			int start = Gpr.parseIntSafe(fields[1]); // Input format is 0-based
			double score = Gpr.parseDoubleSafe(fields[2]);

			// Add data to list
			Chromosome chromo = genome.getOrCreateChromosome(chr);
			cppList.add(chromo, start, start, score);

			if (verbose) Gpr.showMark(num++, READ_INPUT_SHOW_EVERY);
		}

		return cppList;

	}

	/**
	 * Read input in VCF format
	 */
	ChrPosScoreList readInputVcf() {
		ChrPosScoreList cppList = new ChrPosScoreList();

		int num = 1, warns = 0;
		VcfFileIterator vcf = new VcfFileIterator(inputFile);
		vcf.setDebug(debug);

		for (VcfEntry ve : vcf) {
			double score = ve.getInfoFloat(infoName);

			if (Double.isNaN(score)) {
				// Error
				if (warns <= MAX_WARNS) {
					System.err.println("Warning: Cannot find INFO field '" + infoName + "'. Ignoring VCF entry " + vcf.getLineNum() + "\t" + ve);
					if (warns == MAX_WARNS) System.err.println("Too many warnings! No more warnings shown.");
				}
				warns++;
			} else {
				// Add to list
				cppList.add(ve.getChromosome(), ve.getStart(), ve.getEndClosed(), score);
			}

			if (verbose) Gpr.showMark(num++, READ_INPUT_SHOW_EVERY);
		}

		return cppList;
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		// Normal usage: Just run analysis
		if (commandsFile == null) return runAnalisis();

		// Run several commands (no need to reload genomic database each time)
		return runCommands();
	}

	/**
	 * Run command
	 */
	protected boolean runAnalisis() {
		initialize();

		if (geneScoreFile.isEmpty() && geneInterestingFile.isEmpty()) {
			// Perform 'normal' procedure
			readInput(); // Read input files (scores)
			mapToGenes(); // Map <chr,pos,Score> to gene
			scoreGenes(); // Get one score (Score) per gene
			correctScores(); // Correct gene scores
		} else if (!geneScoreFile.isEmpty()) {
			// Scores already mapped to genes, provided in a file
			readGeneScores(geneScoreFile);
			correctScores(); // Correct gene scores
		} else if (!geneInterestingFile.isEmpty()) {
			// Interesting genes from file (not calculated)
			readGeneInteresting(geneInterestingFile);
		}

		enrichmentAnalysis(); // Perform enrichment analysis
		if (randIterations > 0) runAnalisisRand(); // Perform random iterations

		if (verbose) Log.info("Done.");
		return true;
	}

	/**
	 * Run enrichment analysis using random scores
	 */
	protected boolean runAnalisisRand() {
		HashMap<String, Double> geneScoreOri = geneScore; // Save original scores

		for (int iter = 1; iter <= randIterations; iter++) {
			Log.info("Random scores. Iteration " + iter);

			// Create random Scores based on input
			geneScore = new HashMap<String, Double>();
			for (String gene : geneScoreOri.keySet())
				geneScore.put(gene, Math.random());

			// Perform enrichment analysis
			enrichmentAnalysis();
		}

		geneScore = geneScoreOri; // Restore original values
		if (verbose) Log.info("Done.");
		return true;
	}

	/**
	 * Read "command" lines from file.
	 * Loads database only once to save time
	 * @return
	 */
	protected boolean runCommands() {
		boolean ok = true;

		// Read config file, load & build database
		config();

		// Parse commands from file
		for (String commnadLine : Gpr.readFile(commandsFile).split("\n")) {
			if (verbose) {
				Log.info("COMMAND: " + commnadLine);
				System.out.println("COMMAND: " + commnadLine);
			}

			// Parse command line (tab-separated)
			String args[] = commnadLine.split("\t");

			// Create new 'SnpEffCmdGsa' and set database
			SnpEffCmdGsa snpEffCmdGsa = new SnpEffCmdGsa();

			// Set config (and database)
			snpEffCmdGsa.setConfig(config);

			// Run
			StringBuilder err = new StringBuilder();
			ok &= run(snpEffCmdGsa, args, err);
		}

		if (verbose) Log.info("Done!");
		return ok;
	}

	/**
	 * Get one score (Score) per gene
	 */
	void scoreGenes() {
		if (verbose) Log.info("Aggregating scores by gene (scoring genes)");

		// Create one Score per gene
		double scoreMin = Double.MAX_VALUE, scoreMax = Double.MIN_VALUE;

		geneScore = new HashMap<String, Double>();

		for (String geneId : geneScores.keySet()) {
			// Calculate aggregated score
			ScoreList gpl = geneScores.get(geneId);
			double score = gpl.score(scoreSummary);

			scoreMax = Math.max(score, scoreMax);
			scoreMin = Math.min(score, scoreMin);

			// Add to map
			geneScore.put(geneId, score);
		}

		if (verbose) Log.info("Done. Score range: [ " + scoreMin + " , " + scoreMax + " ]");
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff gsa [options] genome_version geneSets.gmt [input_file]");
		System.err.println("\n\tInput data options:");
		System.err.println("\t-commands <file>              : Read commands from file (allows multiple analysis loading the database only once).");
		System.err.println("\t-geneId                       : Use geneID instead of gene names. Default: " + useGeneId);
		System.err.println("\t-i <format>                   : Input format {vcf, bed, txt}. Default: " + inputFormat);
		System.err.println("\t-info <name>                  : INFO tag used for scores (in VCF input format).");
		System.err.println("\t-desc                         : Sort scores in descending order (high score are better then low scores). Default " + orderDescending);
		System.err.println("\t-save <file>                  : Save results to file.");
		System.err.println("\t-score                        : Treat input data as scores instead of p-values.");
		System.err.println("\n\tAlgorithm options:");
		System.err.println("\t-algo <name>                  : Gene set enrichment algorithm {FISHER_GREEDY, RANKSUM_GREEDY, FISHER, RANKSUM, LEADING_EDGE_FRACTION, NONE}. Default: " + enrichmentAlgorithmType);
		System.err.println("\t-correction <cmd>             : Correction of scores using command 'cmd' (e.g. an R script).");
		System.err.println("\t-geneScore                    : Method to summarize gene scores {MIN, MAX, AVG, AVG_MIN_10, AVG_MAX_10, FISHER_CHI_SQUARE, Z_SCORES, SIMES}. Default: " + scoreSummary);
		System.err.println("\t-geneScoreFile <file>         : Read gene score from file instead of calculating them. Format: 'geneId \\t score'");
		System.err.println("\t-mapClosestGene               : Map to closest gene. Default: " + useClosestGene);
		System.err.println("\t-maxPvalue <num>              : Maximum un-adjusted p-value to show result. Default: None");
		System.err.println("\t-maxPvalueAdj <num>           : Maximum adjusted p-value to show result. Default: " + maxPvalueAdjusted);
		System.err.println("\t-saveGeneScoreFile <file>     : Save gene scores to file.");
		System.err.println("\t-rand <num>                   : Perform 'num' iterations using random scores. Default: " + randIterations);
		System.err.println("\n\tAlgorithm specific options: FISHER and FISHER_GREEDY");
		System.err.println("\t-interesting <num>            : Consider a gene 'interesting' if the score is in the 'num' percentile. Default: " + interestingPerc);
		System.err.println("\t-geneInterestingFile <file>   : Use 'interesting' genes from file instead of calculating them.");
		System.err.println("\n\tGene Set options:");
		System.err.println("\t-minSetSize <num>             : Minimum number of genes in a gene set. Default: " + minGeneSetSize);
		System.err.println("\t-maxSetSize <num>             : Maximum number of genes in a gene set. Default: " + maxGeneSetSize);
		System.err.println("\t-initSetSize <num>            : Initial number of genes in a gene set (size range algorithm). Default: " + initGeneSetSize);
		System.exit(-1);
	}
}
