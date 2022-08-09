package org.snpeff.binseq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.MarkerSeq;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.interval.tree.Itree;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

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
public class GenomicSequences implements Iterable<MarkerSeq>, Serializable {

	private static final long serialVersionUID = 2339867422366567569L;
	public static final int MAX_ITERATIONS = 1000000;
	public static final int CHR_LEN_SEPARATE_FILE = 1000 * 1000; // Minimum chromosome length to be saved to a separate file

	boolean debug = false;
	boolean verbose = false;
	boolean allSmallLoaded; // Have all "small" chromosomes been loaded? (i.e. have we already loaded 'sequence.bin' file?)
	boolean disableLoad = false; // Do not load sequences from disk. Used minly for test cases
	Genome genome; // Reference genome
	IntervalForest intervalForest; // This is an interval forest of 'MarkerSeq' (genomic markers that have sequences)

	public GenomicSequences(Genome genome) {
		this.genome = genome;
		intervalForest = new IntervalForest();
	}

	/**
	 * Create a sequence for the whole chromsome (mostly used in test cases)
	 */
	public void addChromosomeSequence(String chr, String chrSeq) {
		MarkerSeq ms = new MarkerSeq(genome.getOrCreateChromosome(chr), 0, chrSeq.length() - 1, chrSeq);
		intervalForest.add(ms);
		build();
	}

	/**
	 * Add sequences from genome's exons
	 */
	boolean addExonSequences(String chr) {
		if (verbose) Log.info("Creating sequences from exon information '" + chr + "'");
		Itree tree = intervalForest.getOrCreateTreeChromo(chr);

		// Add all exon sequences. Collapse them if possible
		Markers exonMarkers = exonMarkers(chr);
		if (debug) Log.debug("Before union: " + exonMarkers.size());
		exonMarkers = exonMarkers.union();
		if (debug) Log.debug("After union: " + exonMarkers.size());
		tree.add(exonMarkers);

		// Build tree
		if (verbose) Log.info("Building sequence tree for chromosome '" + chr + "'");
		build();
		if (verbose) Log.info("Done. Loaded " + tree.getIntervals().size() + " sequences.");

		return !tree.isEmpty();
	}

	/**
	 * Add sequences for each gene in the genome
	 */
	public int addGeneSequences(String chr, String chrSeq) {
		int seqsAdded = 0;

		// Get all genes in this chromosome
		Markers markers = genesMarkers(chr, chrSeq.length());

		// Merge (collapse) overlapping markers
		markers = markers.merge();

		// Find and add sequences for all markers
		for (Marker genes : markers) {
			if (!genes.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = genes.getStart();
			int ssEnd = genes.getEndClosed() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrSeq.length())) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrSeq.length() + "). Sequence (merged genes): " + genes.toStr());
			} else {
				try {
					String seq = chrSeq.substring(ssStart, ssEnd).toUpperCase();
					seqsAdded++;

					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(genes.getChromosome(), genes.getStart(), genes.getEndClosed(), false, genes.getChromosomeName() + ":" + genes.getStart() + "-" + genes.getEndClosed());
					m.setSequence(seq);
					intervalForest.add(m);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error trying to add sequence for gene:\n\tChromosome sequence length: " + chrSeq.length() + "\n\tGene: " + genes.toStr());
				}
			}
		}

		build();
		return seqsAdded;
	}

	/**
	 * Build interval forest
	 */
	public void build() {
		if (verbose) Log.info("Building sequence tree for genome sequences");
		intervalForest.build();
		if (verbose) Log.info("Done.");
	}

	public void clear() {
		intervalForest = new IntervalForest();
	}

	/**
	 * List of all exons
	 */
	Markers exonMarkers(String chr) {
		Markers markers = new Markers();

		// Add exons sequences
		for (Gene g : genome.getGenes()) {
			if (g.getChromosomeName().equals(chr)) {
				for (Transcript tr : g)
					for (Exon ex : tr) {
						String seq = ex.getSequence();

						// Only add exons that have full sequences
						if (seq != null && seq.length() >= ex.size()) {

							if (ex.isStrandPlus()) {
								markers.add(ex);
							} else {
								// We must reverse complement the sequence, since it's on the other strand
								Exon exRwc = (Exon) ex.clone();
								exRwc.setSequence(GprSeq.reverseWc(ex.getSequence()));
								markers.add(exRwc);
							}
						}
					}
			}
		}

		return markers;
	}

	/**
	 * Create a list of markers
	 */
	Markers genesMarkers(String chr, int chrLen) {
		Markers markers = new Markers();
		for (Gene gene : genome.getGenes()) {
			if (!gene.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = gene.getStart();
			int ssEnd = gene.getEndClosed() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrLen)) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrLen + "). Gene: " + gene.toStr());
			} else {
				try {
					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(gene.getChromosome(), gene.getStart(), gene.getEndClosed(), false, gene.getId());
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
	 * Do we have sequence information for this chromosome?
	 */
	public boolean hasChromosome(String chr) {
		if (!intervalForest.hasTree(chr)) return false;

		// Tried to load tree and it's empty?
		Itree tree = intervalForest.getTreeChromo(chr);
		if (tree != null && tree.isEmpty()) return false; // Tree is empty, means we could not load any sequence from 'database'

		return true;
	}

	public boolean isEmpty() {
		for (Itree tree : intervalForest)
			if (!tree.getIntervals().isEmpty()) return false;

		return true;
	}

	@Override
	public Iterator<MarkerSeq> iterator() {
		ArrayList<MarkerSeq> all = new ArrayList<MarkerSeq>();

		for (Itree tree : intervalForest)
			for (Marker m : tree.getIntervals())
				all.add((MarkerSeq) m);

		return all.iterator();
	}

	/**
	 * Load sequences for all 'small chromosomes" (from "sequence.bin" file)
	 */
	public synchronized boolean load() {
		if (disableLoad) return false; // Loading form database disabled?
		if (allSmallLoaded) return false;

		// File does not exists?  Cannot load...
		String fileName = Config.get().getFileNameSequence();
		if (!Gpr.exists(fileName)) {
			if (Config.get().isDebug()) Log.info("Attempting to load sequences from file '" + fileName + "' failed, nothing done.");
			return false;
		}

		// Load markers
		if (verbose) Log.info("Loading sequences from file '" + fileName + "'");
		Markers markers = new Markers();
		Set<Itree> toBuild = new HashSet<>();
		markers.load(fileName, genome);
		for (Marker m : markers) {
			if (m instanceof Genome || m instanceof Chromosome) continue;
			Itree tree = intervalForest.getOrCreateTreeChromo(m.getChromosomeName());
			tree.add(m);
			toBuild.add(tree);
		}

		// Build all trees
		for (Itree itree : toBuild) {
			if (itree.getIntervals().size() > 0 && verbose) Log.info("Building sequence tree for chromosome '" + itree.getIntervals().get(0).getChromosomeName() + "'");
			itree.build();

		}

		allSmallLoaded = true;
		return true;
	}

	/**
	 * Load sequences for a single chromosome (from "sequence.chr.bin" file)
	 */
	public synchronized boolean load(String chr) {
		// Already loaded?
		if (hasChromosome(chr)) return true;
		if (disableLoad) return false; // Loading form database disabled?

		// File does not exists?  Cannot load...
		String fileName = Config.get().getFileNameSequence(chr);
		if (!Gpr.exists(fileName)) {
			if (Config.get().isDebug()) Log.info("Attempting to load sequences for chromosome '" + chr + "' from file '" + fileName + "' failed, nothing done.");
			return false;
		}

		// Load markers
		if (verbose) Log.info("Loading sequences for chromosome '" + chr + "' from file '" + fileName + "'");
		Itree tree = intervalForest.getOrCreateTreeChromo(chr);
		tree.load(fileName, genome);
		if (verbose) Log.info("Building sequence tree for chromosome '" + chr + "'");
		tree.build();
		if (verbose) Log.info("Done. Loaded " + tree.getIntervals().size() + " sequences.");
		return !tree.isEmpty();
	}

	/**
	 * Load sequences from genomic sequence file or (if not file is available) generate some sequences from exons.
	 */
	public synchronized boolean loadOrCreateFromGenome(String chr) {
		if (hasChromosome(chr)) return true;

		if (load(chr)) return true; // Loaded form 'separate' file
		else {
			// Try loading form bundled file (small chromosomes)
			load();
		}

		return addExonSequences(chr);
	}

	/**
	 * Find a marker (with sequence) containing query 'marker'
	 * Could trigger loading sequences form database
	 *
	 * @return A markerSeq containing 'marker' or null if nothing is found
	 */
	public synchronized MarkerSeq queryMarkerSequence(Marker marker) {
		String chr = marker.getChromosomeName();

		// Get or load interval tree
		if (!intervalForest.hasTree(chr)) loadOrCreateFromGenome(chr);
		Itree tree = intervalForest.getTreeChromo(chr);

		// Nothing available
		if (tree == null || tree.isEmpty()) return null;

		// Find marker sequence
		Markers res = tree.query(marker);
		if (res.isEmpty()) return null;

		// Return the first markerSeq containing 'marker'
		// Note: We should look for the 'best'. But the sequences are
		//       be maximal by construction (when the database is built).
		//       So we can just return the first one (and only one) we
		//       find. The loop is necessary to filter out 'Chromosome'.
		for (Marker m : res)
			if (m.includes(marker) && (m instanceof MarkerSeq)) return (MarkerSeq) m;

		return null;
	}

	/**
	 * Get sequence for a marker
	 */
	public String querySequence(Marker marker) {
		MarkerSeq ms = queryMarkerSequence(marker);
		if (ms == null) return null;

		// Calculate start and end coordiantes
		int sstart = marker.getStart() - ms.getStart();
		int ssend = marker.size() + sstart;
		String seq = ms.getSequence().substring(sstart, ssend);

		// Return sequence in same direction as 'marker'
		if (marker.isStrandMinus()) seq = GprSeq.reverseWc(seq);
		return seq;
	}

	public void reset() {
		intervalForest = new IntervalForest();
	}

	/**
	 * Save genomic sequence into separate files (per chromosome)
	 */
	public void save(Config config) {
		if (isEmpty()) return; // Nothing to do

		// Sort chromomse names
		ArrayList<String> chrNames = new ArrayList<String>();
		chrNames.addAll(intervalForest.keySet());
		Collections.sort(chrNames);

		// Save 'long' chromsomes in separate files
		Genome genome = config.getGenome();
		ArrayList<String> toSaveOneFile = new ArrayList<String>();
		for (String chrName : chrNames) {
			int seqLen = sequenceLen(chrName);
			if (seqLen >= CHR_LEN_SEPARATE_FILE) save(chrName); // Save in separate file
			else toSaveOneFile.add(chrName); // Save all small chromosomes in one file
		}

		// Save all remaining ones in one file
		if (!toSaveOneFile.isEmpty()) {
			Markers markers = new Markers();
			markers.add(genome);

			for (String chrName : toSaveOneFile) {
				if (intervalForest.hasTree(chrName)) {
					Itree tree = intervalForest.getTreeChromo(chrName);
					markers.addAll(tree.getIntervals());
				}
			}

			// Save to file
			String fileName = Config.get().getFileNameSequence();
			if (verbose) Log.info("Saving sequences for small chromosmes to file '" + fileName + "'");
			markers.save(fileName);
		}
	}

	/**
	 * Save sequences from chromosome 'chr' to a binary file
	 */
	void save(String chr) {
		if (!intervalForest.hasTree(chr)) {
			if (verbose) Log.info("No tree found for chromosome '" + chr + "'");
			return;
		}

		// OK, there is something to save => Save markers to file
		Itree tree = intervalForest.getTreeChromo(chr);
		String fileName = Config.get().getFileNameSequence(chr);
		if (verbose) Log.info("Saving sequences for chromosome '" + chr + "' to file '" + fileName + "'");
		tree.getIntervals().save(fileName, chr);
	}

	/**
	 * Length of all sequences in chromosome 'chr'
	 */
	int sequenceLen(String chr) {
		Itree tree = intervalForest.getTreeChromo(chr);
		if (tree == null) return 0;

		int size = 0;
		for (Marker m : tree.getIntervals()) {
			if (m instanceof MarkerSeq) {
				MarkerSeq ms = (MarkerSeq) m;
				size += ms.getSequence().length();
			}
		}

		return size;
	}

	public void setDisableLoad(boolean disableLoad) {
		this.disableLoad = disableLoad;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Genomic sequences '" + genome.getId() + "'\n");

		long sumMarkers = 0;
		long sumLen = 0;
		for (String chr : intervalForest.keySet()) {
			Itree tree = intervalForest.getTreeChromo(chr);

			// Calculate total sequence length stored
			long len = 0;
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
