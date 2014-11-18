package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.FrameType;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.IntervalComparatorByEnd;
import ca.mcgill.mcb.pcingola.interval.IntervalComparatorByStart;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * This class creates a SnpEffectPredictor from a file (or a set of files) and a configuration
 *
 * @author pcingola
 */
public abstract class SnpEffPredictorFactory {

	// Show a mark every
	public static final int MARK = 100;

	// Debug mode?
	boolean debug = false;
	boolean verbose = false;
	boolean readSequences = true; // Do not read sequences from GFF file (this is only used for debugging)
	boolean createRandSequences = false; // If sequences are not read frmo a file, create random sequences
	boolean frameCorrection;
	int lineNum;
	int inOffset; // This amount is subtracted to all position coordinates
	int totalSeqsAdded = 0, totalSeqsIgnored = 0; // Number of sequences added and ignored
	String fileName;
	String fastaFile; // Only used for debugging or testing
	String line;
	Config config;
	Genome genome;
	SnpEffectPredictor snpEffectPredictor;
	FrameType frameType;
	HashMap<String, Integer> exonsByChromo;
	HashMap<String, Marker> markersById;
	HashMap<String, Gene> genesById;
	HashMap<String, Transcript> transcriptsById;
	Random random = new Random(20140410); // Note: we want consistent results in our test cases, so we always initialize the random generator in the same way

	public SnpEffPredictorFactory(Config config, int inOffset) {
		this.config = config;
		this.inOffset = inOffset;

		genome = config.getGenome();
		snpEffectPredictor = new SnpEffectPredictor(config.getGenome());
		exonsByChromo = new HashMap<String, Integer>();
		markersById = new HashMap<String, Marker>();
		genesById = new HashMap<String, Gene>();
		transcriptsById = new HashMap<String, Transcript>();

		frameCorrection = false;
		frameType = FrameType.UNKNOWN;
	}

	protected void add(Cds cds) {
		Transcript tr = (Transcript) cds.getParent();
		tr.add(cds);

		addMarker(cds, false);
	}

	protected void add(Chromosome chromo) {
		genome.add(chromo);
	}

	protected void add(Exon exon) {
		Transcript tr = (Transcript) exon.getParent();

		// Make sure the same exon was not added before
		Exon oldex = tr.get(exon.getId());
		if (oldex != null) {
			if (oldex.includes(exon)) return; // Redundant, just ignore it.

			// Create a new exon with same info and different 'id'
			exon = new Exon(tr, exon.getStart(), exon.getEnd(), exon.isStrandMinus(), exon.getId() + "_" + tr.subintervals().size(), exon.getRank());
		}

		// Add exon
		tr.add(exon);
		addMarker(exon, false);
	}

	/**
	 * Add a Gene
	 * @param gene
	 */
	protected void add(Gene gene) {
		snpEffectPredictor.add(gene);

		if (genesById.containsKey(gene.getId())) throw new RuntimeException("Gene  '" + gene.getId() + "' already exists");
		if (debug) System.out.println("\tAdding gene\tID: '" + gene.getId() + "'\tname: '" + gene.getGeneName() + "'\t" + gene.toStr());
		genesById.put(gene.getId(), gene);
	}

	/**
	 * Add a generic Marker
	 * @param marker
	 */
	protected void add(Marker marker) {
		addMarker(marker, false);
		if (debug) System.out.println("\tAdding " + marker.getClass().getSimpleName() + ":\tID: '" + marker.getId() + "'\t" + marker.toStr());
	}

	/**
	 * Add a transcript
	 * @param tr
	 */
	protected void add(Transcript tr) {
		Gene gene = (Gene) tr.getParent();
		gene.add(tr);

		if (transcriptsById.containsKey(tr.getId())) throw new RuntimeException("Transcript  '" + tr.getId() + "' already exists");
		if (debug) System.out.println("\tAdding transcript :\tID: '" + tr.getId() + "'\t" + tr.toStr());
		transcriptsById.put(tr.getId(), tr);
	}

	/**
	 * Add a marker to the collection
	 * @param marker
	 */
	protected void addMarker(Marker marker, boolean unique) {
		String key = marker.getId();
		if (unique && markersById.containsKey(key)) throw new RuntimeException("Marker '" + key + "' already exists");
		markersById.put(key, marker);
	}

	/**
	 * Add genomic reference sequences
	 */
	protected void addSequences(String chr, String chrSeq) {
		// Update chromosome length
		chromoLen(chr, chrSeq.length());

		// Add sequences for each exon
		int seqsAdded = 0, seqsIgnored = 0;
		if (verbose) System.out.print("\t\tAdding genomic sequences to exons: ");

		// Find and add sequences for all exons in this chromosome
		for (Gene gene : genome.getGenes()) {
			if (gene.getChromosomeName().equalsIgnoreCase(chr)) { // Same chromosome? => go on
				for (Transcript tr : gene) {
					for (Exon exon : tr) {
						int ssStart = exon.getStart();
						int ssEnd = exon.getEnd() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

						if ((ssStart < 0) || (ssEnd > chrSeq.length())) {
							warning("Ignoring exon outside chromosome range (chromo length: " + chrSeq.length() + "). Exon: " + exon);
							seqsIgnored++;
						} else {
							try {
								String seq = chrSeq.substring(ssStart, ssEnd).toUpperCase();
								// Reverse strand? => reverse complement of the sequence
								if (exon.isStrandMinus()) seq = GprSeq.reverseWc(seq);
								exon.setSequence(seq);
								seqsAdded++;
							} catch (Throwable t) {
								t.printStackTrace();
								throw new RuntimeException("Error trying to add sequence to exon:\n\tChromosome sequence length: " + chrSeq.length() + "\n\tExon: " + exon);
							}
						}
					}
				}
			}
		}

		if (verbose) System.out.println("\tDone (" + seqsAdded + " sequences added, " + seqsIgnored + " ignored).");
		totalSeqsAdded += seqsAdded;
		totalSeqsIgnored += seqsIgnored;
	}

	/**
	 * Adjust chromosome length using gene information
	 * This is used when the sequence is not available (which makes sense on test-cases and debugging only)
	 */
	protected void adjustChromosomes() {
		if (verbose) System.out.print("\n\tAdjusting chromosomes lengths: ");

		HashMap<String, Integer> lenByChr = new HashMap<String, Integer>();
		for (Gene gene : config.getGenome().getGenes()) {
			String chrName = gene.getChromosomeName();
			Integer len = lenByChr.get(chrName);
			int max = Math.max(gene.getEnd(), (len != null ? len : 0));
			lenByChr.put(chrName, max);
		}

		// Set length
		int adjusted = 0;
		for (String chrName : lenByChr.keySet()) {
			Chromosome chr = config.getGenome().getChromosome(chrName);
			int newEnd = lenByChr.get(chrName);
			if (chr.getEnd() < newEnd) {
				chr.setEnd(lenByChr.get(chrName));
				mark(adjusted++);
			}
		}
	}

	/**
	 * Adjust genes: recalculate start, end, strand, etc.
	 */
	void adjustGenes() {
		int i = 1;
		if (verbose) System.out.print("\n\tAdjusting genes: ");
		for (Gene gene : genome.getGenes())
			if (gene.adjust()) mark(i++);

	}

	/**
	 * Adjust transcripts: recalculate start, end, strand, etc.
	 */
	protected void adjustTranscripts() {
		int i = 1;
		if (verbose) System.out.print("\n\tAdjusting transcripts: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.adjust()) mark(i++);

	}

	/**
	 * Perform some actions before reading sequences
	 */
	protected void beforeExonSequences() {
		// Sometimes we have to guess exon info from CDS info (not the best case scenario, but there are a lot of crappy genome annotations around)
		exonsFromCds();

		// Some annotation formats split exons in two parts (e.g. stop-codon not part of exon in GTF).
		deleteRedundant();

		// Some annotations introduce zero size introns
		collapseZeroLenIntrons();
	}

	/**
	 * Get (or create) a chromosome and set it's length
	 */
	void chromoLen(String chromoName, int len) {
		Chromosome chromo = getOrCreateChromosome(chromoName);
		chromo.setLength(len);
	}

	/**
	 * Only coding transcripts have CDS: Make sure that transcripts having CDS are protein coding
	 *
	 * It might not be always "precise" though:
	 *
	 * 		$ grep CDS genes.gtf | cut -f 2 | ~/snpEff/scripts/uniqCount.pl
	 * 		113	IG_C_gene
	 * 		64	IG_D_gene
	 * 		24	IG_J_gene
	 * 		366	IG_V_gene
	 * 		21	TR_C_gene
	 * 		3	TR_D_gene
	 * 		82	TR_J_gene
	 * 		296	TR_V_gene
	 * 		461	non_stop_decay
	 * 		63322	nonsense_mediated_decay
	 * 		905	polymorphic_pseudogene
	 * 		34	processed_transcript
	 * 		1340112	protein_coding
	 */
	protected void codingFromCds() {
		int i = 0;
		if (verbose) System.out.print("\n\tMarking as 'coding' from CDS information: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene) {
				if (tr.getCds() != null && !tr.getCds().isEmpty()) {
					// If transcript doesn't have protein coding flag set (and doesn't have biotype information), use CDS as a proxy for 'protein coding'
					if (!tr.isProteinCoding() && (tr.getBioType() == null || tr.getBioType().isEmpty())) {
						tr.setProteinCoding(true);
						i++;
						if (debug) System.err.println("\t\tMarking as protein coding transcript " + tr.getId());
					}
				}
			}
		if (verbose) System.out.print("\n\tDone: " + i + " transcripts marked");
	}

	/**
	 * Collapse exons having zero size introns between them
	 */
	protected void collapseZeroLenIntrons() {
		if (verbose) System.out.print("\n\tCollapsing zero length introns (if needed): ");

		int count = 0;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.collapseZeroGap()) mark(count++);

		if (verbose) System.out.println("\n\t\tTotal collapsed transcripts: " + count);
	}

	/**
	 * Count number of exons by chromosome
	 */
	@SuppressWarnings("unused")
	void countExonsByChromo() {
		exonsByChromo = new HashMap<String, Integer>();

		for (Gene gint : genome.getGenes()) {
			Chromosome chromo = gint.getChromosome();
			for (Transcript tint : gint) {
				for (Exon eint : tint) {
					// Get current count
					String chromoName = chromo.getId();
					Integer count = exonsByChromo.get(chromoName);

					// Increment
					if (count == null) count = 1;
					else count++;

					// Store
					exonsByChromo.put(chromoName, count);
				}
			}
		}
	}

	public abstract SnpEffectPredictor create();

	/**
	 * Create random sequences for exons
	 *
	 * Note: This is only used for test cases!
	 */
	protected void createRandSequences() {
		// Find all exons and add a 'random' sequence to each of them
		for (Gene g : genome.getGenes())
			for (Transcript tr : g)
				for (Exon ex : tr) {
					String sequence = GprSeq.randSequence(random, ex.size());
					ex.setSequence(sequence);
				}
	}

	/**
	 * Consolidate transcripts:
	 * If two exons are one right next to the other, join them
	 * E.g. exon1:1234-2345, exon2:2346-2400 => exon:1234-2400
	 * This happens mostly in GTF files, where the stop-codon is specified separated from the exon info.
	 */
	protected void deleteRedundant() {
		if (verbose) System.out.print("\n\tDeleting redundant exons (if needed): ");
		int count = 0;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.deleteRedundant()) mark(count++);

		if (verbose) System.out.println("\n\t\tTotal transcripts with deleted exons: " + count);
	}

	/**
	 * Error: Throw a runtime exception (show some details)
	 */
	void error(String msg) {
		throw new RuntimeException("FATAL ERROR: " + msg + ". File '" + fileName + "' line " + lineNum + "\n\t'" + line + "'\n");
	}

	/**
	 * Error: Throw a runtime exception (show some details)
	 */
	void error(String msg, Throwable t) {
		throw new RuntimeException("FATAL ERROR: " + msg + ". File '" + fileName + "' line " + lineNum + "\n\t'" + line + "'\n", t);
	}

	/**
	 * Create exons from CDS info
	 */
	protected void exonsFromCds() {
		if (verbose) System.out.print("\n\tCreate exons from CDS (if needed): ");

		int count = 0;
		for (Gene gene : genome.getGenes()) {
			for (Transcript tr : gene) {
				// CDS length
				int lenCds = 0;
				for (Cds cds : tr.getCds())
					lenCds += cds.size();

				// Exon length
				int lenExons = 0;
				for (Exon ex : tr)
					lenExons += ex.size();

				// Cds length larger than exons? => something is missing
				if (lenCds > lenExons) {
					exonsFromCds(tr);
					count++;
				}
			}
		}
		if (verbose) System.out.println("\n\tExons created for " + count + " transcripts.");
	}

	/**
	 * Create exons from CDS info
	 * WARNING: We might end up with redundant exons if some exons existed before this process
	 *
	 * @param tr : Transcript with CDS info, but no exons
	 */
	protected void exonsFromCds(Transcript tr) {
		List<Cds> cdss = tr.getCds();

		// First: Check and adjust strand info
		boolean trStrandMinus = tr.isStrandMinus();
		int cdsStrandSum = 0;
		for (Cds cds : cdss)
			cdsStrandSum += cds.isStrandMinus() ? -1 : 1;
		boolean cdsStrandMinus = cdsStrandSum < 0;
		if (cdsStrandMinus != trStrandMinus) {
			if (verbose) System.out.print(cdsStrandMinus ? '-' : '+');
			tr.setStrandMinus(cdsStrandMinus);
		}

		// Sort CDS by strand
		if (tr.isStrandPlus()) Collections.sort(cdss, new IntervalComparatorByStart()); // Sort by start position
		else Collections.sort(cdss, new IntervalComparatorByEnd(true)); // Sort by end position (reversed)

		// Add cds as exons
		// WARNING: We might end up with redundant exons if some exons existed before this process
		int rank = 1;
		for (Cds cds : cdss) {
			// Create exon and add it to transcript
			String id = "Exon_" + cds.getChromosomeName() + "_" + cds.getStart() + "_" + cds.getEnd();
			if (tr.get(id) == null) { // Don't add an exon twice
				Exon exon = new Exon(tr, cds.getStart(), cds.getEnd(), trStrandMinus, id, rank);
				tr.add(exon);
			}

			rank++;
			if (verbose) System.out.print('.');
		}
	}

	protected Gene findGene(String id) {
		Gene gene = genesById.get(id);
		if (gene != null) return gene;
		return genesById.get("Gene_" + id); // Alternative gene ID
	}

	protected Gene findGene(String geneId, String id) {
		Gene gene = findGene(geneId);
		if (gene != null) return gene;
		return genesById.get("Gene_" + id); // Alternative gene ID
	}

	protected Marker findMarker(String id) {
		return markersById.get(id);
	}

	protected Transcript findTranscript(String id) {
		Transcript tr = transcriptsById.get(id);
		if (tr != null) return tr;
		return transcriptsById.get("Transcript_" + id); // Alternative transcript ID
	}

	protected Transcript findTranscript(String trId, String id) {
		Transcript tr = findTranscript(trId);
		if (tr != null) return tr;
		return transcriptsById.get("Transcript_" + id);
	}

	/**
	 * Finish up procedure to ensure consistency
	 */
	void finishUp() {
		// Adjust
		adjustTranscripts(); // Adjust transcripts: recalculate start, end, strand, etc.
		adjustGenes(); // Adjust genes: recalculate start, end, strand, etc.
		adjustChromosomes(); // Adjust chromosome sizes

		// Adjust exons: Most file formats don't have exon rank information.
		rankExons();

		// If some UTRs are missing: calculate UTR information from CDS whenever possible
		if (verbose) System.out.print("\n\tCreate UTRs from CDS (if needed): ");
		utrFromCds();

		// Correct according to frame information
		if (frameCorrection) frameCorrection();

		// Remove empty chromosomes
		removeEmptyChromos();

		// Mark as coding if there is a CDS
		codingFromCds();

		// Done
		if (verbose) System.out.println("");
	}

	/**
	 * Correct exon's coordinates, according to frame information
	 */
	void frameCorrection() {
		if (verbose) System.out.print("\n\tCorrecting exons based on frame information.\n\t");

		//---
		// Sanity check. Are all frames zero?
		//---
		int countByFrame[] = new int[3];
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene) {
				for (Exon ex : tr) {
					int frame = ex.getFrame();
					if (frame >= 0 && frame <= 2) countByFrame[frame]++; // Any value other than {0, 1, 2} is ignored (-1 means missing, other values are invalid)
				}

				for (Cds cds : tr.getCds()) {
					int frame = cds.getFrame();
					if (frame >= 0 && frame <= 2) countByFrame[frame]++; // Any value other than {0, 1, 2} is ignored (-1 means missing, other values are invalid)
				}
			}

		int countByFrameTotal = countByFrame[0] + countByFrame[1] + countByFrame[2];
		int countByFrameNonZero = countByFrame[1] + countByFrame[2];
		if ((countByFrameTotal > 0) && (countByFrameNonZero <= 0)) System.err.println("WARNING: All frames are zero! This seems rather odd, please check that 'frame' information in your 'genes' file is accurate.");

		//---
		// Perform exon frame adjustment
		//---
		int i = 1;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene) {
				boolean corrected = tr.frameCorrection();

				if (corrected) {
					if (debug) System.err.println("\tTranscript " + tr.getId() + " corrected using frame (exons: " + tr.numChilds() + ").");
					else if (verbose) Gpr.showMark(i++, 1);

				}
			}

		if (verbose) System.out.print("");

	}

	/**
	 * Get a chromosome. If it doesn't exist, create it
	 */
	protected Chromosome getOrCreateChromosome(String chromoName) {
		Chromosome chromo = genome.getChromosome(chromoName);

		// Not found? => Create a new one
		if (chromo == null) {
			chromo = new Chromosome(genome, 0, 0, chromoName);
			genome.add(chromo);
		}

		return chromo;
	}

	/**
	 * Does this chromosome have any exons?
	 * @param chromoName
	 * @return
	 */
	boolean hasExons(String chromoName) {
		Integer count = exonsByChromo.get(chromoName);
		return (count != null) && (count > 0);
	}

	/**
	 * Show a mark onthe screen (to show progress)
	 * @param count
	 */
	void mark(int count) {
		if (verbose) Gpr.showMark(count, MARK, "\t\t");
	}

	/**
	 * Parse a string as a 'position'.
	 * Note: It subtracts 'inOffset' so that all coordinates are zero-based
	 *
	 * @param posStr
	 * @return
	 */
	protected int parsePosition(String posStr) {
		return Gpr.parseIntSafe(posStr) - inOffset;
	}

	/**
	 * Rank exons
	 */
	void rankExons() {
		int i = 1;
		if (verbose) System.out.print("\n\tRanking exons: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.rankExons()) mark(i++);
	}

	/**
	 * Read exon sequences from a FASTA file
	 * @param fastaFile
	 */
	protected void readExonSequences() {
		List<String> files = config.getFileListGenomeFasta();

		// Force a specific file?
		if (fastaFile != null) {
			files.clear();
			files.add(fastaFile);
		}

		// Try all files in the list until one is available
		for (String file : files) {

			if (Gpr.canRead(file)) {
				if (verbose) System.out.println("\tReading FASTA file: '" + file + "'");
				// Read fasta sequence
				FastaFileIterator ffi = new FastaFileIterator(file);
				for (String seq : ffi) {
					String chromo = ffi.getName();
					if (verbose) System.out.println("\t\tReading sequence '" + chromo + "', length: " + seq.length());
					addSequences(chromo, seq); // Add all sequences
				}
				return;
			} else if (verbose) System.out.println("\tFASTA file: '" + file + "' not found.");
		}

		throw new RuntimeException("Cannot find reference sequence.");
	}

	/**
	 * Remove empty chromosomes
	 */
	void removeEmptyChromos() {
		if (verbose) System.out.println("\n\tRemove empty chromosomes: ");
		ArrayList<Chromosome> chrToDelete = new ArrayList<Chromosome>();
		for (Chromosome chr : config.getGenome())
			if (chr.size() <= 1) chrToDelete.add(chr);

		for (Chromosome chr : chrToDelete) {
			if (verbose) System.out.println("\t\tRemoving empty chromosome: '" + chr.getId() + "'");
			config.getGenome().remove(chr);
		}

		// Show remaining chromosomes
		if (verbose) {
			if (chrToDelete.size() > 0) {
				System.out.print("\t\tChromosome left: ");
				for (Chromosome chr : config.getGenome())
					System.out.print(chr.getId() + " ");
				System.out.println("");
			}
		}
	}

	public void setCreateRandSequences(boolean createRandSequences) {
		this.createRandSequences = createRandSequences;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setFastaFile(String fastaFile) {
		this.fastaFile = fastaFile;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Read sequences?
	 * Note: This is only used for debugging and testing
	 */
	public void setReadSequences(boolean readSequences) {
		this.readSequences = readSequences;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	String unquote(String qstr) {
		return qstr.replaceAll("\"", "");
	}

	/**
	 * Create missing UTRs from CDS information
	 */
	void utrFromCds() {
		int i = 1;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.utrFromCds(debug)) mark(i++);
	}

	/**
	 * Warning: Show a warning message (show some details)
	 * @param msg
	 */
	void warning(String msg) {
		if (verbose) System.err.println("WARNING: " + msg + ". File '" + fileName + "' line " + lineNum + "\t'" + line + "'");
	}

}
