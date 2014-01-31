package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.fileIterator.BedFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intergenic;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Command line: Find closes marker to each variant
 * 
 * @author pcingola
 */
public class SnpEffCmdClosest extends SnpEff {

	public static final String CLOSEST = "CLOSEST";
	public static final String INFO_LINE = "##INFO=<ID=" + CLOSEST + ",Number=4,Type=String,Description=\"Closest exon: Distance (bases), exons Id, transcript Id, gene name\">";

	boolean canonical; // Use only canonical transcripts
	boolean bedFormat;
	String inFile;
	SnpEffectPredictor snpEffectPredictor;
	int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
	int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE; // Splice site size default: 2 bases (canonical splice site)

	public SnpEffCmdClosest() {
		super();
		command = "closestExon";
	}

	public SnpEffCmdClosest(Config config) {
		super();
		command = "closestExon";
		this.config = config;
		inFile = config.getFileNameProteins();
	}

	/**
	 * Update header
	 * @param vcf
	 */
	void addHeaderLines(VcfFileIterator vcf) {
		vcf.getVcfHeader().addLine("##SnpEffVersion=\"" + SnpEff.VERSION + "\"");
		vcf.getVcfHeader().addLine("##SnpEffCmd=\"" + commandLineStr(false) + "\"");
		vcf.getVcfHeader().addLine(INFO_LINE);
	}

	/**
	 * Iterate over VCF file, find closest exons and annotate vcf lines
	 */
	void bedIterate() {
		// Open file
		BedFileIterator bfi = new BedFileIterator(inFile, config.getGenome(), 0);
		bfi.setCreateChromos(true); // Any 'new' chromosome in the input file will be created (otherwise an error will be thrown)

		for (SeqChange bed : bfi) {
			try {
				// Find closest exon
				Markers closestMarkers = findClosestMarker(bed);

				String id = bed.getId();

				// Update ID field if any marker found
				if (closestMarkers != null) {
					StringBuilder idsb = new StringBuilder();

					// Previous ID
					idsb.append(bed.getId());
					if (idsb.length() > 0) idsb.append(";");

					// Distance
					Marker firstMarker = closestMarkers.getMarkers().get(0);
					int dist = firstMarker.distance(bed);
					idsb.append(dist);

					// Append all closest markers
					for (Marker closestMarker : closestMarkers)
						idsb.append(";" + closestMarker.idChain(",", false));

					id = idsb.toString();
				}

				// Show output
				System.out.println(bed.getChromosomeName() //
						+ "\t" + bed.getStart() // BED format: Zero-based position
						+ "\t" + (bed.getEnd() + 1) // BED format: End base is not included
						+ "\t" + id //
				);

			} catch (Exception e) {
				e.printStackTrace(); // Show exception and move on...
			}
		}
	}

	/**
	 * Find closest marker
	 * @param queryMarker
	 */
	Markers findClosestMarker(Marker queryMarker) {
		int initialExtension = 1000;

		Chromosome chr = queryMarker.getChromosome();
		if ((chr != null) && (chr.size() > 0)) {

			// Extend interval to capture 'close' markers
			for (int extend = initialExtension; extend < chr.size(); extend *= 2) {
				int start = Math.max(queryMarker.getStart() - extend, 0);
				int end = queryMarker.getEnd() + extend;
				Marker extended = new Marker(chr, start, end, 1, "");

				// Find all markers that intersect with 'extended interval'
				Markers markers = snpEffectPredictor.queryDeep(extended);

				// Find minimum distance
				int minDistance = minDistance(queryMarker, markers);
				if (minDistance < Integer.MAX_VALUE) {
					// All markers that are at minimum distance 
					Markers closest = findClosestMarkers(queryMarker, markers, minDistance);
					return closest;
				}
			}
		}

		// Nothing found
		return null;
	}

	/**
	 * Find closest marker to query (in markers collection)
	 * @param queryMarker
	 * @param markers
	 * @return
	 */
	Markers findClosestMarkers(Marker queryMarker, Markers markers, int maxDistance) {
		Markers closest = new Markers();
		HashSet<String> done = new HashSet<String>();

		for (Marker m : markers) {
			// We don't care about these
			if ((m instanceof Chromosome) || (m instanceof Intergenic) || (m instanceof Gene) || (m instanceof Transcript)) continue;

			// Find marker at distance less than 'distance'
			int dist = m.distance(queryMarker);
			if ((dist <= maxDistance) && (findTranscript(m) != null)) {
				String idChain = m.idChain();
				if (!done.contains(idChain)) { // Do not repeat information
					closest.add(m);
					done.add(idChain);
				}
			}
		}

		return closest;
	}

	/**
	 * Find a transcript
	 * @param m
	 * @return
	 */
	Transcript findTranscript(Marker m) {
		if (m instanceof Transcript) return (Transcript) m;
		return (Transcript) m.findParent(Transcript.class);
	}

	/**
	 * Find minimum distance
	 * @param queryMarker
	 * @param markers
	 * @return minumum distance
	 */
	int minDistance(Marker queryMarker, Markers markers) {
		int minDist = Integer.MAX_VALUE;
		for (Marker m : markers) {
			// We don't care about these
			if ((m instanceof Chromosome) || (m instanceof Intergenic) || (m instanceof Gene) || (m instanceof Transcript)) continue;

			// Find closest marker that has a transcript 
			int dist = m.distance(queryMarker);
			if ((dist <= minDist) && (findTranscript(m) != null)) minDist = dist;
		}
		return minDist;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			// Argument starts with '-'?
			if (isOpt(args[i])) {
				if (args[i].equals("-bed")) bedFormat = true;
				else if (args[i].equalsIgnoreCase("-canon")) canonical = true; // Use canonical transcripts
				else if ((args[i].equals("-ss") || args[i].equalsIgnoreCase("-spliceSiteSize"))) {
					if ((i + 1) < args.length) spliceSiteSize = Gpr.parseIntSafe(args[++i]);
				} else if ((args[i].equals("-ud") || args[i].equalsIgnoreCase("-upDownStreamLen"))) {
					if ((i + 1) < args.length) upDownStreamLength = Gpr.parseIntSafe(args[++i]);
				} else usage("Unknow option '" + args[i] + "'");
			} else if (genomeVer.isEmpty()) genomeVer = args[i];
			else if (inFile == null) inFile = args[i];
			else usage("Unknow parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if ((genomeVer == null) || genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if ((inFile == null) || inFile.isEmpty()) usage("Missing 'file' parameter");
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		// Load config
		if (config == null) readConfig();

		if (verbose) Timer.showStdErr("Loading predictor...");
		config.loadSnpEffectPredictor();
		if (verbose) Timer.showStdErr("done");

		if (verbose) Timer.showStdErr("Building interval forest...");
		snpEffectPredictor = config.getSnpEffectPredictor();

		// Set upstream-downstream interval length
		config.getSnpEffectPredictor().setUpDownStreamLength(upDownStreamLength);

		// Set splice site size
		config.getSnpEffectPredictor().setSpliceSiteSize(spliceSiteSize);

		// Filter canonical transcripts
		if (canonical) {
			if (verbose) Timer.showStdErr("Filtering out non-canonical transcripts.");
			config.getSnpEffectPredictor().removeNonCanonical();

			if (debug) {
				// Show genes and transcript (which ones are considered 'cannonica')
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

		snpEffectPredictor.buildForest();
		if (verbose) Timer.showStdErr("done");

		if (verbose) Timer.showStdErr("Reading file '" + inFile + "'");
		if (bedFormat) bedIterate();
		else vcfIterate();
		if (verbose) Timer.showStdErr("done");

		return true;
	}

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff closestExon [options] genome_version file.vcf");
		System.err.println("\nOptions:");
		System.err.println("\t-canon                      : Only use canonical transcripts.");
		System.err.println("\t-bed                        : Input format is BED. Default: VCF");
		System.err.println("\t-ss, -spliceSiteSize <int>  : Set size for splice sites (donor and acceptor) in bases. Default: " + spliceSiteSize);
		System.err.println("\t-ud, -upDownStreamLen <int> : Set upstream downstream interval length (in bases)");
		System.exit(-1);
	}

	/**
	 * Iterate over VCF file, find closest exons and annotate vcf lines
	 */
	void vcfIterate() {
		// Open file
		VcfFileIterator vcf = new VcfFileIterator(inFile, config.getGenome());
		vcf.setCreateChromos(true); // Any 'new' chromosome in the input file will be created (otherwise an error will be thrown)

		boolean header = true;
		for (VcfEntry ve : vcf) {
			try {
				if (header) {
					// Update and show header
					addHeaderLines(vcf);
					String headerStr = vcf.getVcfHeader().toString();
					if (!headerStr.isEmpty()) System.out.println(headerStr);
					header = false;
				}

				// Find closest exon
				Markers closestMarkers = findClosestMarker(ve);

				// Update INFO fields if any marker was found
				if (closestMarkers != null) {
					StringBuilder closestsb = new StringBuilder();

					// Distance
					Marker firstMarker = closestMarkers.getMarkers().get(0);
					int dist = firstMarker.distance(ve);
					closestsb.append(dist);

					// Append all closest markers
					for (Marker closestMarker : closestMarkers)
						closestsb.append("|" + closestMarker.idChain(",", false));

					ve.addInfo(CLOSEST, closestsb.toString());
				}

				// Show output
				System.out.println(ve);
			} catch (Exception e) {
				e.printStackTrace(); // Show exception and move on...
			}
		}
	}

}
