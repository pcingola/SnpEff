package org.snpeff.snpEffect.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.CircularCorrection;
import org.snpeff.interval.Exon;
import org.snpeff.interval.FrameType;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.GffType;
import org.snpeff.interval.IntervalComparatorByEnd;
import org.snpeff.interval.IntervalComparatorByStart;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * This class creates a SnpEffectPredictor from a file (or a set of files) and a configuration
 *
 * @author pcingola
 */
public abstract class SnpEffPredictorFactory {

	// Show a mark every
	public static final int MARK = 100;
	public static int MIN_TOTAL_FRAME_COUNT = 10;

	// Debug mode?
	boolean circularCorrectLargeGap = false;
	boolean createRandSequences = false; // If sequences are not read frmo a file, create random sequences
	boolean debug = false;
	boolean frameCorrection;
	boolean readSequences = true; // Do not read sequences from GFF file (this is only used for debugging)
	boolean storeSequences = false; // Store full gene sequences (in separate 'sequence.chr*.bin' files)
	boolean verbose = false;
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
	Set<String> chromoNamesReference; // Chromosome names used in reference sequence file (e.g. FASTA)
	Map<String, Integer> exonsByChromo;
	Map<String, Marker> markersById;
	Map<String, Gene> genesById;
	Map<String, Transcript> transcriptsById;
	Random random = new Random(20140410); // Note: we want consistent results in our test cases, so we always initialize the random generator in the same way

	public SnpEffPredictorFactory(Config config, int inOffset) {
		this.config = config;
		this.inOffset = inOffset;

		genome = config.getGenome();
		snpEffectPredictor = new SnpEffectPredictor(config.getGenome());
		exonsByChromo = new HashMap<>();
		markersById = new HashMap<>();
		genesById = new HashMap<>();
		transcriptsById = new HashMap<>();
		chromoNamesReference = new HashSet<>();

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

	/**
	 * Add an exon
	 *
	 * @param exon
	 * @return exon added.
	 * Note: If the exon exists with the same ID, return old exon.
	 *       If exon exists with same ID and same coordiates, add a new exon with different ID.
	 */
	protected Exon add(Exon exon) {
		Transcript tr = (Transcript) exon.getParent();

		// Make sure the same exon was not added before
		Exon oldex = tr.get(exon.getId());
		if (oldex != null) {
			if (oldex.includes(exon)) return oldex; // Redundant, just ignore it.

			// Create a new exon with same info and different 'id'
			exon = new Exon(tr, exon.getStart(), exon.getEndClosed(), exon.isStrandMinus(), exon.getId() + "_" + tr.subIntervals().size(), exon.getRank());
		}

		// Add exon
		tr.add(exon);
		addMarker(exon, false);
		return exon;
	}

	/**
	 * Add a Gene
	 */
	protected void add(Gene gene) {
		if (debug) Log.debug("\tAdding gene\tID: '" + gene.getId() + "'\tname: '" + gene.getGeneName() + "'\t" + gene.toStr());
		snpEffectPredictor.add(gene);

		if (genesById.containsKey(gene.getId())) throw new RuntimeException("Gene  '" + gene.getId() + "' already exists");
		genesById.put(gene.getId(), gene);
	}

	/**
	 * Add a generic Marker
	 */
	protected void add(Marker marker) {
		if (debug) Log.debug("\tAdding " + marker.getClass().getSimpleName() + ":\tID: '" + marker.getId() + "'\t" + marker.toStr());
		addMarker(marker, false);
	}

	/**
	 * Add a transcript
	 */
	protected void add(Transcript tr) {
		Gene gene = (Gene) tr.getParent();
		if (debug) Log.debug("\tAdding transcript :\tID: '" + tr.getId() + "' to gene '" + gene.getId() + "'\t" + tr.toStr());
		gene.add(tr);

		if (transcriptsById.containsKey(tr.getId())) throw new RuntimeException("Transcript  '" + tr.getId() + "' already exists");
		transcriptsById.put(tr.getId(), tr);
	}

	/**
	 * Add a marker to the collection
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
		int chrLen = chrSeq.length();
		Chromosome chromo = getOrCreateChromosome(chr);
		chromo.setLength(chrLen);
		chromo.detectCircular();

		// Add sequences for each gene
		int seqsAdded = 0, seqsIgnored = 0;

		if (storeSequences) {
			if (verbose) Log.info("\t\tAdding genomic sequences to genes: ");
			int count = genome.getGenomicSequences().addGeneSequences(chr, chrSeq);
			if (verbose) Log.info("\tDone (" + count + " sequences added).");
		}

		if (verbose) Log.info("\t\tAdding genomic sequences to exons: ");

		// Find and add sequences for all exons in this chromosome
		for (Gene gene : genome.getGenes()) {
			// Different chromosome? Skip
			if (!gene.getChromosomeName().equalsIgnoreCase(chr)) continue;

			for (Transcript tr : gene) {
				// Circular chromosomes coordinates are corrected in this step
				CircularCorrection cc = new CircularCorrection(tr, chrLen);
				cc.setDebug(debug);
				cc.setCorrectLargeGap(circularCorrectLargeGap);
				cc.correct();

				for (Exon exon : tr) {
					int ssStart = exon.getStart();
					int ssEnd = exon.getEndClosed() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

					String seq = null;
					if ((ssStart >= 0) && (ssEnd <= chrLen)) {
						// Regular coordinates
						try {
							seq = chrSeq.substring(ssStart, ssEnd);
						} catch (Throwable t) {
							t.printStackTrace();
							throw new RuntimeException("Error trying to add sequence to exon:\n\tChromosome sequence length: " + chrSeq.length() + "\n\tExon: " + exon);
						}
					} else {
						// Sanity check
						if (!chromo.isCircular()) throw new RuntimeException("Coordinated out of bounds on a non-circular chromosome. This should never happen!Error trying to add sequence to exon:\n\tExon: " + exon);

						if ((ssStart < 0) && (ssEnd > 0)) {
							// Negative start coordinates? This is probably a circular genome
							// Convert to 2 intervals:
							//     i) Interval before zero: This gets mapped to the end of the chromosome
							//     ii) Interval after zero: This are "normal" coordinates
							// Then we concatenate both sequences
							ssStart += chrLen;
							seq = chrSeq.substring(ssStart, chrLen) + chrSeq.substring(0, ssEnd);
						} else if ((ssStart < 0) && (ssEnd < 0)) {
							// Negative start coordinates? This is probably a circular genome
							// Convert to 2 intervals:
							//     i) Interval before zero: This gets mapped to the end of the chromosome
							//     ii) Interval after zero: This are "normal" coordinates
							// Then we concatenate both sequences
							ssStart += chrLen;
							ssEnd += chrLen;
							seq = chrSeq.substring(ssStart, ssEnd);
						}
					}

					// Set sequence
					if (seq != null) {
						// Sanity check
						if (seq.length() != exon.size()) warning(ErrorWarningType.WARNING_EXON_SEQUENCE_LENGTH, "Exon sequence length does not match exon.size()\n" + exon);

						// Reverse strand? => reverse complement of the sequence
						if (exon.isStrandMinus()) seq = GprSeq.reverseWc(seq);
						seq = seq.toUpperCase();
						exon.setSequence(seq);
						seqsAdded++;
					}
				}
			}
		}

		if (verbose) Log.info("\tDone (" + seqsAdded + " sequences added, " + seqsIgnored + " ignored).");
		totalSeqsAdded += seqsAdded;
		totalSeqsIgnored += seqsIgnored;
	}

	/**
	 * Adjust chromosome length using gene information
	 * This is used when the sequence is not available (which makes sense on test-cases and debugging only)
	 */
	protected void adjustChromosomes() {
		if (verbose) Log.info("Adjusting chromosomes lengths: ");

		// Chromosome length should be longer than any gene's end coordinate
		HashMap<String, Integer> lenByChr = new HashMap<>();
		for (Gene gene : config.getGenome().getGenes()) {
			String chrName = gene.getChromosomeName();
			Integer len = lenByChr.get(chrName);

			Chromosome chr = gene.getChromosome();
			if (chr.getEndClosed() > 0 && gene.getEndClosed() > chr.getEndClosed()) Log.warning(ErrorWarningType.WARNING_CHROMOSOME_LENGTH, "Chromosome '" + chr.getChromosomeName() + "' has end coordinate " + chr.getEndClosed() + ", but gene ID '" + gene.getId() + "' has end coordiante " + gene.getEndClosed());

			int max = Math.max(gene.getEndClosed(), (len != null ? len : 0));
			lenByChr.put(chrName, max);
		}

		// Set length
		int adjusted = 0;
		for (String chrName : lenByChr.keySet()) {
			Chromosome chr = config.getGenome().getChromosome(chrName);
			int newEnd = lenByChr.get(chrName);
			if (chr.getEndClosed() < newEnd) {
				if (chr.size() <= 1) { // If start = end = 0, then size() is 1
					chr.setEndClosed(lenByChr.get(chrName));
					mark(adjusted++);
				} else if (verbose) Log.info("\t\tChromosome '" + chr.getId() + "' has length of " + chr.size() + ", but genes end at " + lenByChr.get(chrName));
			}
		}
	}

	/**
	 * Adjust genes: recalculate start, end, strand, etc.
	 */
	void adjustGenes() {
		int i = 1;
		if (verbose) Log.info("Adjusting genes: ");
		for (Gene gene : genome.getGenes())
			if (gene.adjust()) mark(i++);

	}

	/**
	 * Adjust transcripts: recalculate start, end, strand, etc.
	 */
	protected void adjustTranscripts() {
		int i = 1;
		if (verbose) Log.info("Adjusting transcripts: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.adjust()) mark(i++);

	}

	/**
	 * Perform some actions before reading sequences
	 */
	protected void beforeExonSequences() {
		if (debug) Log.info("Actions before creating sequences");

		// Sometimes we have to guess exon info from CDS info (not the best
		// case scenario, but there are a lot of crappy genome annotations
		// around)
		exonsFromCds();

		// Some annotation formats split exons in two parts (e.g. stop-codon
		// not part of exon in GTF).
		deleteRedundant();

		// Some annotations introduce zero size introns
		collapseZeroLenIntrons();
	}

	/**
	 *  Sanity check. Are all frames zero?
	 */
	void checkAllFramesAreZero() {
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
		if ((countByFrameTotal >= MIN_TOTAL_FRAME_COUNT) && (countByFrameNonZero <= 0)) Log.warning(ErrorWarningType.WARNING_FRAMES_ZERO, "All frames are zero! This seems rather odd, please check that 'frame' information in your 'genes' file is accurate.");
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
		if (verbose) Log.info("Marking as 'coding' from CDS information: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene) {
				if (tr.getCds() != null && !tr.getCds().isEmpty()) {
					// If transcript doesn't have protein coding flag set (and doesn't have biotype information), use CDS as a proxy for 'protein coding'
					if (!tr.isProteinCoding() && (tr.getBioType() == null)) {
						tr.setProteinCoding(true);
						i++;
						if (debug) System.err.println("\t\tMarking as protein coding transcript " + tr.getId());
					}
				}
			}
		if (verbose) Log.info("Done: " + i + " transcripts marked");
	}

	/**
	 * Collapse exons having zero size introns between them
	 */
	protected void collapseZeroLenIntrons() {
		if (verbose) Log.info("Collapsing zero length introns (if needed): ");

		int count = 0;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.collapseZeroGap()) mark(count++);

		if (verbose) Log.info("\tTotal collapsed transcripts: " + count);
	}

	/**
	 * Count number of exons by chromosome
	 */
	@SuppressWarnings("unused")
	void countExonsByChromo() {
		exonsByChromo = new HashMap<>();

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
		if (debug) Log.debug("\tCreating exon sequences");
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
		if (verbose) Log.info("Deleting redundant exons (if needed): ");
		int count = 0;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.deleteRedundant()) mark(count++);

		if (verbose) Log.info("\tTotal transcripts with deleted exons: " + count);
	}

	/**
	 * Create exons from CDS info
	 */
	protected void exonsFromCds() {
		if (verbose) Log.info("Create exons from CDS (if needed): ");

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
		if (verbose) Log.info("Exons created for " + count + " transcripts.");
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
			String id = GffType.EXON + "_" + cds.getChromosomeName() + "_" + cds.getStart() + "_" + cds.getEndClosed();
			if (tr.get(id) == null) { // Don't add an exon twice
				Exon exon = new Exon(tr, cds.getStart(), cds.getEndClosed(), trStrandMinus, id, rank);
				tr.add(exon);
			}

			rank++;
			if (verbose) System.out.print('.');
		}
	}

	protected Gene findGene(String id) {
		Gene gene = genesById.get(id);
		if (gene != null) return gene;
		return genesById.get(GffType.GENE + "_" + id); // Alternative gene ID
	}

	protected Gene findGene(String geneId, String id) {
		Gene gene = findGene(geneId);
		if (gene != null) return gene;
		return genesById.get(GffType.GENE + "_" + id); // Alternative gene ID
	}

	protected Marker findMarker(String id) {
		return markersById.get(id);
	}

	protected Transcript findTranscript(String id) {
		Transcript tr = transcriptsById.get(id);
		if (tr != null) return tr;
		return transcriptsById.get(GffType.TRANSCRIPT + "_" + id); // Alternative transcript ID
	}

	protected Transcript findTranscript(String trId, String id) {
		Transcript tr = findTranscript(trId);
		if (tr != null) return tr;
		return transcriptsById.get(GffType.TRANSCRIPT + "_" + id);
	}

	/**
	 * Finish up procedure to ensure consistency
	 */
	void finishUp() {
		if (verbose) Log.info("Finishing up genome");

		// Adjust
		adjustTranscripts(); // Adjust transcripts: recalculate start, end, strand, etc.
		adjustGenes(); // Adjust genes: recalculate start, end, strand, etc.
		adjustChromosomes(); // Adjust chromosome sizes

		// Adjust exons: Most file formats don't have exon rank information.
		rankExons();

		// If some UTRs are missing: calculate UTR information from CDS whenever possible
		if (verbose) Log.info("Create UTRs from CDS (if needed): ");
		utrFromCds();

		// Correct according to frame information
		if (frameCorrection) frameCorrection();

		// Remove empty chromosomes
		removeEmptyChromos();

		// Mark as coding if there is a CDS
		codingFromCds();

		// Check that exons have sequences
		if (readSequences) { // Note: In some test cases we ignore sequences
			boolean error = !config.getGenome().isMostExonsHaveSequence();
			if (error) Log.fatalError("Most Exons do not have sequences!\n" + showChromoNamesDifferences() + "\n\n");
		}

		// Done
		if (verbose) Log.info("");
	}

	/**
	 * Correct exon's coordinates, according to frame information
	 */
	void frameCorrection() {
		if (verbose) Log.info("Correcting exons based on frame information.\n\t");
		checkAllFramesAreZero();

		// Perform exon frame adjustment
		int i = 1;
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene) {
				boolean corrected = tr.frameCorrection();

				if (corrected) {
					if (debug) Log.debug("\tTranscript " + tr.getId() + " corrected using frame (exons: " + tr.numChilds() + ").");
					else if (verbose) Gpr.showMark(i++, 1);

				}
			}

		if (verbose) Log.info("");

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

	public Map<String, String> getProteinByTrId() {
		return null;
	}

	/**
	 * Does this chromosome have any exons?
	 */
	boolean hasExons(String chromoName) {
		Integer count = exonsByChromo.get(chromoName);
		return (count != null) && (count > 0);
	}

	/**
	 * Show a mark onthe screen (to show progress)
	 */
	void mark(int count) {
		if (verbose) Gpr.showMark(count, MARK, "\t\t");
	}

	/**
	 * Parse a string as a 'position'.
	 * Note: It subtracts 'inOffset' so that all coordinates are zero-based
	 */
	protected int parsePosition(String posStr) {
		return Gpr.parseIntSafe(posStr) - inOffset;
	}

	/**
	 * Rank exons
	 */
	void rankExons() {
		int i = 1;
		if (verbose) Log.info("Ranking exons: ");
		for (Gene gene : genome.getGenes())
			for (Transcript tr : gene)
				if (tr.rankExons()) mark(i++);
	}

	/**
	 * Read exon sequences from a FASTA file
	 */
	protected void readExonSequences() {
		if (debug) Log.info("Reading exon sequences from files");
		List<String> files = config.getFileListGenomeFasta();

		// Force a specific file?
		if (fastaFile != null) {
			files.clear();
			files.add(fastaFile);
		}

		// Try all files in the list until one is available
		for (String file : files) {

			if (Gpr.canRead(file)) {
				if (verbose) Log.info("\tReading FASTA file: '" + file + "'");

				// Read fasta sequence
				FastaFileIterator ffi = new FastaFileIterator(file);
				for (String seq : ffi) {
					String chromo = ffi.getName();
					chromoNamesReference.add(chromo);
					if (verbose) Log.info("\t\tReading sequence '" + chromo + "', length: " + seq.length());
					addSequences(chromo, seq); // Add all sequences
				}
				return;
			} else if (verbose) Log.info("\tFASTA file: '" + file + "' not found.");
		}

		throw new RuntimeException("Cannot find reference sequence.");
	}

	/**
	 * Remove empty chromosomes
	 */
	void removeEmptyChromos() {
		if (verbose) Log.info("Remove empty chromosomes: ");
		ArrayList<Chromosome> chrToDelete = new ArrayList<>();
		for (Chromosome chr : config.getGenome())
			if (chr.size() <= 1) chrToDelete.add(chr);

		for (Chromosome chr : chrToDelete) {
			if (verbose) Log.info("\t\tRemoving empty chromosome: '" + chr.getId() + "'");
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

	protected void replaceTranscript(Transcript trOld, Transcript trNew) {
		transcriptsById.remove(trOld.getId());
		transcriptsById.put(trNew.getId(), trNew);
	}

	public void setCircularCorrectLargeGap(boolean circularCorrectLargeGap) {
		this.circularCorrectLargeGap = circularCorrectLargeGap;
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

	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * Read sequences?
	 * Note: This is only used for debugging and testing
	 */
	public void setReadSequences(boolean readSequences) {
		this.readSequences = readSequences;
	}

	public void setStoreSequences(boolean storeSequences) {
		this.storeSequences = storeSequences;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Shw differences in chromosome names
	 */
	protected String showChromoNamesDifferences() {
		if (chromoNamesReference.isEmpty()) return "";

		// Get all chromosome names
		Set<String> chrs = new HashSet<>();
		for (Gene g : config.getGenome().getGenes())
			chrs.add(g.getChromosomeName());

		//---
		// Show chromosomes not present in reference sequence file
		//---
		int counMissinfRef = 0;
		StringBuilder sbMissingRef = new StringBuilder();
		ArrayList<String> chrsSorted = new ArrayList<>();
		chrsSorted.addAll(chrs);
		Collections.sort(chrsSorted);
		for (String chr : chrsSorted) {
			if (!chromoNamesReference.contains(chr)) {
				counMissinfRef++;
				if (sbMissingRef.length() > 0) sbMissingRef.append(", ");
				sbMissingRef.append("'" + chr + "'");
			}
		}

		//---
		// Show chromosomes not present in genes file
		//---
		int counMissinfGenes = 0;
		StringBuilder sbMissingGenes = new StringBuilder();
		ArrayList<String> chrsRefSorted = new ArrayList<>();
		chrsRefSorted.addAll(chromoNamesReference);
		Collections.sort(chrsRefSorted);
		for (String chr : chrsRefSorted) {
			if (!chrs.contains(chr)) {
				counMissinfGenes++;
				if (sbMissingGenes.length() > 0) sbMissingRef.append(", ");
				sbMissingGenes.append("'" + chr + "'");
			}
		}

		// Show differences
		String msg = "";
		if (counMissinfRef > 0 && counMissinfGenes > 0) {
			msg = "There might be differences in the chromosome names used in the genes file " //
					+ "('" + fileName + "')" //
					+ "\nand the chromosme names used in the 'reference sequence' file" //
					+ (fastaFile != null ? " ('" + fastaFile + "')" : "") + "." //
					+ "\nPlease check that chromosome names in both files match.\n";
		}
		return msg //
				+ (sbMissingRef.length() > 0 ? "\tChromosome names missing in 'reference sequence' file:\t" + sbMissingRef.toString() : "") //
				+ (sbMissingGenes.length() > 0 ? "\n\tChromosome names missing in 'genes' file             :\t" + sbMissingGenes.toString() : "")//
		;
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
	void warning(ErrorWarningType warnType, String msg) {
		Log.warning(warnType, msg + ". File '" + fileName + "' line " + lineNum + "\t'" + line + "'");
	}

}
