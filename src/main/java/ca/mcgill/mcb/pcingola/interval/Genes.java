package ca.mcgill.mcb.pcingola.interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of genes (marker intervals)
 * Note: It is assumed that all genes belong to the same genome
 * 
 * @author pcingola
 */
public class Genes implements Iterable<Gene>, Serializable {

	private static final long serialVersionUID = 9022385501946879197L;

	public boolean debug = false;
	Genome genome;
	HashMap<String, Gene> genesById;

	public Genes(Genome genome) {
		genesById = new HashMap<String, Gene>();
		this.genome = genome;
	}

	/**
	 * Add a gene interval to this collection
	 * @param gene
	 */
	public void add(Gene gene) {
		genesById.put(gene.getId(), gene);
	}

	/**
	 * Creates a list of Intergenic regions 
	 */
	public List<Intergenic> createIntergenic() {
		ArrayList<Intergenic> intergenics = new ArrayList<Intergenic>(genesById.size());

		// Create a list of genes sorted by position
		ArrayList<Gene> genesSorted = new ArrayList<Gene>(genesById.size());
		genesSorted.addAll(genesById.values());
		Collections.sort(genesSorted);

		// For each gene, transcript
		Gene genePrev = null;
		Chromosome chrPrev = null;
		for (Gene gene : genesSorted) {

			// Chromosome change? Invaludate genePrev
			if (chrPrev != gene.getChromosome()) genePrev = null;

			// Intergenic region's [start, end] interval
			int start = (genePrev != null ? genePrev.getEnd() + 1 : 0);
			int end = gene.getStart() - 1;

			// Valid intergenic region?
			if (start < end) {
				String id = (genePrev != null ? genePrev.getGeneName() + "..." : "") + gene.getGeneName();
				Intergenic intergenic = new Intergenic(gene.getChromosome(), start, end, 1, id);
				intergenics.add(intergenic);
			}

			// Is it null or ends before this one? update 'genePrev'
			if ((genePrev == null) || (gene.getEnd() > genePrev.getEnd())) genePrev = gene;

			// Update chrPrev
			chrPrev = gene.getChromosome();
		}

		return intergenics;
	}

	/**
	 * Create splice sites.
	 * 
	 * @param createIfMissing : If true, create canonical splice sites if they are missing.
	 * 
	 * For a definition of splice site, see comments at the beginning of SpliceSite.java
	 */
	public Collection<Marker> createSpliceSites(int spliceSiteSize, int spliceRegionExonSize, int spliceRegionIntronMin, int spliceRegionIntronMax) {
		ArrayList<Marker> spliceSites = new ArrayList<Marker>();

		// For each gene, transcript
		for (Gene gene : this) {
			for (Transcript tr : gene) {
				List<SpliceSite> slist = tr.createSpliceSites(spliceSiteSize, spliceRegionExonSize, spliceRegionIntronMin, spliceRegionIntronMax); // Find (or create) splice sites
				spliceSites.addAll(slist); // Store all markers 
			}
		}

		return spliceSites;
	}

	/**
	 * Creates a list of UP/DOWN stream regions (for each transcript)
	 * Upstream (downstream) stream is defined as upDownLength before (after) transcript
	 * 
	 * Note: If upDownLength <=0 no interval is created
	 */
	public List<Marker> createUpDownStream(int upDownLength) {
		ArrayList<Marker> list = new ArrayList<Marker>();
		if (upDownLength <= 0) return list;

		// For each gene, transcript
		for (Gene gene : this) {
			for (Transcript tr : gene) {
				tr.createUpDownStream(upDownLength);
				list.add(tr.getUpstream());
				list.add(tr.getDownstream());
			}
		}
		return list;
	}

	/**
	 * Obtain a gene interval
	 * @param geneId
	 * @return
	 */
	public Gene get(String geneId) {
		return genesById.get(geneId);
	}

	@Override
	public Iterator<Gene> iterator() {
		return genesById.values().iterator();
	}

	public int size() {
		return genesById.size();
	}

	public Collection<Gene> sorted() {
		ArrayList<Gene> genes = new ArrayList<Gene>();
		genes.addAll(genesById.values());
		Collections.sort(genes, new IntervalComparatorByStart());
		return genes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Gene gint : this)
			sb.append(gint + "\n");
		return sb.toString();
	}

	public Collection<Gene> values() {
		return genesById.values();
	}

}
