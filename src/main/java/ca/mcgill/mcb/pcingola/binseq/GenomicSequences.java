package ca.mcgill.mcb.pcingola.binseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.MarkerSeq;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalTree;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * This class stores all "relevant" sequences in a genome
 *
 * This class is able to:
 * 		i) Add all regions of interest
 * 		ii) Store genomic sequences for those regions of interest
 * 		iii) Retrieve genomic sequences by interval
 *
 *
 * @author pcingola
 */
public class GenomicSequences implements Iterable<MarkerSeq> {

	boolean verbose = false;
	Genome genome; // Reference genome
	IntervalForest intervalForest; // This is an interval forest of 'MarkerSeq' (genomic markers that have sequences)
	Config config;

	public GenomicSequences(Config config, Genome genome) {
		this.genome = genome;
		this.config = config;
		intervalForest = new IntervalForest();
	}

	public GenomicSequences(Genome genome) {
		this(Config.get(), genome);
	}

	/**
	 * Add sequences for each gene in the genome
	 */
	public int addGeneSequences(String chr, String chrSeq) {
		int seqsAdded = 0;

		// Get all genes in this chromosome
		Markers markers = genesMarkers(chr, chrSeq.length());

		// Collapse overlapping markers
		Gpr.debug("Size before merge: " + markers.size());
		markers = markers.merge();
		Gpr.debug("Size after merge: " + markers.size());

		// Find and add sequences for all markers
		for (Marker genes : markers) {
			if (!genes.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = genes.getStart();
			int ssEnd = genes.getEnd() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrSeq.length())) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrSeq.length() + "). Sequence (merged genes): " + genes.toStr());
			} else {
				try {
					String seq = chrSeq.substring(ssStart, ssEnd).toUpperCase();
					seqsAdded++;

					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(genes.getChromosome(), genes.getStart(), genes.getEnd(), false, genes.getChromosomeName() + ":" + genes.getStart() + "-" + genes.getEnd());
					m.setSequence(seq);
					intervalForest.add(m);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error trying to add sequence for gene:\n\tChromosome sequence length: " + chrSeq.length() + "\n\tGene: " + genes.toStr());
				}
			}
		}

		Gpr.debug(this);

		return seqsAdded;
	}

	/**
	 * Create a list of markers
	 */
	Markers genesMarkers(String chr, int chrLen) {
		Markers markers = new Markers();
		for (Gene gene : genome.getGenes()) {
			if (!gene.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = gene.getStart();
			int ssEnd = gene.getEnd() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrLen)) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrLen + "). Gene: " + gene.toStr());
			} else {
				try {
					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(gene.getChromosome(), gene.getStart(), gene.getEnd(), false, gene.getId());
					markers.add(m);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error trying to add sequence for gene:\n\tChromosome sequence length: " + chrLen + "\n\tGene: " + gene.toStr());
				}
			}
		}

		return markers;
	}

	/**
	 * Get sequence for a marker
	 */
	public String getSequence(Marker marker) {
		String chr = marker.getChromosomeName();

		// Get or load interval tree
		if (!intervalForest.hasTree(chr)) load(chr);
		IntervalTree tree = intervalForest.getTree(chr);

		// Nothing available
		if (tree.isEmpty()) return null;

		// Find marker sequence
		Markers res = tree.query(marker);
		if (res.isEmpty()) return null;

		// Get first marker (ideally, there should be only one that fully includes 'marker')
		MarkerSeq ms = null;
		for (Marker m : res) {
			if (m.includes(marker) && !(m instanceof Chromosome)) {
				ms = (MarkerSeq) m;
				break;
			}
		}

		if (ms == null) {
			// Nothing found? Ideally, this should not happen
			Gpr.debug("No MarkerSeq found for '" + marker.toStr() + "'. This should never happen!");
			return null;
		}

		// Calculate start and end coordiantes
		int sstart = marker.getStart() - ms.getStart();
		int ssend = marker.size() + sstart;
		String seq = ms.getSequence().substring(sstart, ssend);

		// Return sequence in same direction as 'marker'
		if (marker.isStrandMinus()) seq = GprSeq.reverseWc(seq);
		return seq;

	}

	public boolean isEmpty() {
		for (IntervalTree tree : intervalForest)
			if (!tree.getIntervals().isEmpty()) return false;

		return true;
	}

	@Override
	public Iterator<MarkerSeq> iterator() {
		ArrayList<MarkerSeq> all = new ArrayList<MarkerSeq>();

		for (IntervalTree tree : intervalForest)
			for (Marker m : tree.getIntervals())
				all.add((MarkerSeq) m);

		return all.iterator();
	}

	/**
	 * Load sequences from genomic sequence file
	 */
	public synchronized void load(String chr) {
		String fileName = config.getFileNameSequence(chr);
		IntervalTree tree = intervalForest.getTree(chr);
		if (!tree.isEmpty()) return; // Already loaded

		// F=No 'sequences' file? Cannot load...
		if (!Gpr.exists(fileName)) {
			if (config.isDebug()) Timer.show("Attempting to load sequences for chromosome '" + chr + "' from file '" + fileName + "' failed, nothing done.");
			return;
		}

		// Load markers
		if (verbose) Timer.show("Loading sequences for chromosome '" + chr + "' from file '" + fileName + "'");
		tree.load(fileName);
		if (verbose) Timer.show("Building sequence tree for chromosome '" + chr + "'");
		tree.build();
		if (verbose) Timer.show("Done. Loaded " + tree.getIntervals().size() + " sequences.");
	}

	/**
	 * Save genomic sequence into separate files (per chromosome)
	 */
	public void save() {
		if (isEmpty()) return; // Nothing to do

		ArrayList<String> chrNames = new ArrayList<String>();
		chrNames.addAll(intervalForest.getTreeNames());
		Collections.sort(chrNames);

		for (String chr : chrNames)
			save(chr);
	}

	/**
	 * Save sequences from chromosome 'chr' to a binary file
	 */
	void save(String chr) {
		IntervalTree tree = intervalForest.getTree(chr);

		if (tree == null) {
			if (verbose) Timer.showStdErr("No tree found for chromosome '" + chr + "'");
			return;
		}

		if (tree.getIntervals().isEmpty()) {
			if (verbose) Timer.showStdErr("No sequences found for chromosome '" + chr + "'");
			return;
		}

		// OK, there is something to save => Save markers to file
		String fileName = config.getFileNameSequence(chr);
		if (verbose) Timer.showStdErr("Saving sequences for chromosome '" + chr + "' to file '" + fileName + "'");
		tree.getIntervals().save(fileName);
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Genomic sequences '" + genome.getId() + "'\n");

		int sumMarkers = 0;
		long sumLen = 0;
		for (String chr : intervalForest.getTreeNames()) {
			IntervalTree tree = intervalForest.getTree(chr);

			// Calculate total sequence length stored
			int len = 0;
			for (Marker m : tree.getIntervals()) {
				len += m.size();
				sumLen += m.size();
			}

			sumMarkers += tree.getIntervals().size();

			sb.append("\t" + chr + "\t" + tree.size() + "\t" + len + "\n");
		}
		sb.append("\tTOTAL\t" + sumMarkers + "\t" + sumLen + "\n");

		return sb.toString();
	}

}
