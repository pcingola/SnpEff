package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.fileIterator.BedFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intergenic;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
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

	boolean bedFormat = false;
	boolean tss = false;
	String inFile;
	SnpEffectPredictor snpEffectPredictor;

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
		BedFileIterator bfi = new BedFileIterator(inFile, config.getGenome());
		bfi.setCreateChromos(true); // Any 'new' chromosome in the input file will be created (otherwise an error will be thrown)

		for (Variant bed : bfi) {
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
					int dist = reportDistance(closestMarkers, bed);
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
				Marker extended = new Marker(chr, start, end, false, "");

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
				else if (args[i].equals("-tss")) tss = true;
				else usage("Unknow option '" + args[i] + "'");
			} else if (genomeVer.isEmpty()) genomeVer = args[i];
			else if (inFile == null) inFile = args[i];
			else usage("Unknow parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if ((genomeVer == null) || genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if ((inFile == null) || inFile.isEmpty()) usage("Missing 'file' parameter");
	}

	/**
	 * Calculate distance to report
	 * @param closestMarkers
	 * @param queryMarker
	 * @return
	 */
	int reportDistance(Markers closestMarkers, Marker queryMarker) {
		Marker firstMarker = closestMarkers.getMarkers().get(0); // If there is more than one marker, they are at the same distance, so we just report the distance to the first one
		if (tss) {
			Transcript tr = findTranscript(firstMarker);
			Marker trTss = tr.getTss();
			int d = trTss.distance(queryMarker);
			return tr.intersects(queryMarker) ? -d : d; // Negative distance denotes the marker is actually intersecting the transcript's TSS
		}
		return firstMarker.distance(queryMarker);
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		// Load config
		if (config == null) loadConfig();

		// Load Db
		loadDb();

		// Build db
		if (verbose) Timer.showStdErr("Building interval forest...");
		snpEffectPredictor = config.getSnpEffectPredictor();
		snpEffectPredictor.buildForest();
		if (verbose) Timer.showStdErr("done");

		// Annotate
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
		System.err.println("\t-bed : Input format is BED. Default: VCF");
		System.err.println("\t-tss : Measure distance from TSS (transcription start site)");
		System.exit(-1);
	}

	/**
	 * Iterate over VCF file, find closest exons and annotate vcf lines
	 */
	void vcfIterate() {
		// Open file
		VcfFileIterator vcf = new VcfFileIterator(inFile, config.getGenome());
		vcf.setCreateChromos(true); // Any 'new' chromosome in the input file will be created (otherwise an error will be thrown)
		vcf.setDebug(debug);

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
					int dist = reportDistance(closestMarkers, ve);
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
