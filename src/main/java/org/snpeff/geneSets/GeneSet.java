package org.snpeff.geneSets;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An set of genes (that belongs to a collection of gene-sets)
 * 
 * @author Pablo Cingolani
 *
 */
public class GeneSet implements Comparable<GeneSet>, Iterable<String>, Serializable {

	public static boolean debug = false;
	private static final long serialVersionUID = 1L;

	String description;
	GeneSets geneSets; // Parent collection of GeneSets (this node belongs to a GeneSets)
	int maxRank; // Maximum rank in this gene set
	String name; // GeneSet name (gene set ID)
	long rankSum; // Rank sum in this GeneSet
	int rankedGenesCount; // How many genes are ranked
	int interestingGenesCount; // How many interesting genes do we have
	HashSet<String> genes;// All genes in this set

	/**
	 * Create a new GeneSet by joining all GeneSets in the list
	 * @param geneSetList
	 */
	public GeneSet(List<GeneSet> geneSetList, GeneSets geneSets) {
		this.geneSets = geneSets;
		maxRank = Integer.MIN_VALUE; // Mark it a not calculated
		rankSum = Integer.MIN_VALUE; // Mark it a not calculated
		interestingGenesCount = Integer.MIN_VALUE; // Mark it a not calculated
		rankedGenesCount = 0;

		// Fill up genes set
		genes = new HashSet<String>();

		// For each GeneSet in the list: join them
		String namesAll = "";
		for (GeneSet geneSet : geneSetList) {
			// Add symbols and interestingSymbols to new Set
			genes.addAll(geneSet.genes);

			// Append names
			namesAll += geneSet.getName() + " ";
		}

		// Name
		setName(namesAll);
	}

	/**
	 * Create an empty gene set (that belongs to a collection of gene sets 'geneSets')
	 * @param name
	 * @param geneSets
	 */
	public GeneSet(String name, String description, GeneSets geneSets) {
		this.geneSets = geneSets;
		setName(name);
		this.description = description;
		maxRank = Integer.MIN_VALUE; // Mark it a not calculated
		rankSum = Integer.MIN_VALUE; // Mark it a not calculated
		interestingGenesCount = Integer.MIN_VALUE; // Mark it a not calculated
		rankedGenesCount = 0;

		// Fill up genes set
		genes = new HashSet<String>();
	}

	/**
	 * Add one gene to genesSet
	 * @param gene
	 */
	public void addGene(String gene) {
		if (gene.isEmpty()) return;
		geneSets.add(gene); // Add to 'parent' collection of gene sets
		genes.add(gene);
	}

	/**
	 * Comparable interface (to order terms)
	 */
	@Override
	public int compareTo(GeneSet geneSet) {
		return getName().compareTo(geneSet.getName());
	}

	/**
	 * Compare 2 numbers and 2 'names' (if both numbers are equal)
	 * 		- NaN sorted in a way that they remain at the end of a sorted list
	 * 		- When two numbers are equal, the sort is done alphabetically by name
	 * 
	 * @param n1
	 * @param n2
	 * @param name1
	 * @param name2
	 * @return
	 */
	public int compareToNumbers(double n1, double n2, String name1, String name2, boolean descending) {
		int order = (descending ? -1 : 1);

		// Both are NaN => compare strings (always in alphabetical order)
		if (Double.isNaN(n1) && Double.isNaN(n2)) { return name1.compareTo(name2); }

		// One of them is NaN? (when sorted, push them to the end of the list)
		if (Double.isNaN(n1)) return +1;
		if (Double.isNaN(n2)) return -1;

		// Both are 'normal' numbers, just compare them
		if (n1 < n2) return -1 * order;
		if (n1 > n2) return +1 + order;

		// If both numbers are equal, sort by 'name'
		return name1.compareTo(name2);
	}

	/**
	 * COunt how many genes are in both GeneSets
	 * @param gs
	 * @return
	 */
	public int countOverlap(GeneSet gs) {
		GeneSet gsSmall = this, gsLarge = gs;
		if (size() > gs.size()) {
			gsSmall = gs;
			gsLarge = this;
		}

		// Iterate on smaller gene set. Count overlap
		int count = 0;
		for (String gene : gsSmall)
			if (gsLarge.hasGene(gene)) count++;

		return count;
	}

	/**
	 * Return a sorted list of genes
	 * @return
	 */
	public List<String> genesSorted() {
		LinkedList<String> ll = new LinkedList<String>(genes);
		Collections.sort(ll);
		return ll;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Number of symbols in this node (total = interesting + not-interesting)
	 * @return
	 */
	public int getGeneCount() {
		return genes.size();
	}

	public HashSet<String> getGenes() {
		return genes;
	}

	public GeneSets getGeneSets() {
		return geneSets;
	}

	/**
	 * Get 'interesting' genes
	 * @return
	 */
	public HashSet<String> getInterestingGenes() {
		HashSet<String> g = new HashSet<String>(genes);
		g.retainAll(geneSets.getInterestingGenes());
		return g;
	}

	/**
	 * Number of 'interesting' symbols
	 * @return
	 */
	public int getInterestingGenesCount() {
		if (interestingGenesCount < 0) interestingGenesCount = getInterestingGenes().size();
		return interestingGenesCount;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public String getName() {
		return name;
	}

	public int getRankedGenesCount() {
		return rankedGenesCount;
	}

	public long getRankSum() {
		return rankSum;
	}

	public boolean hasGene(String gene) {
		return genes.contains(gene);
	}

	/**
	 * Intersects this term with 'geneSet' (intersects 'geneSet' and 'interestingGeneSet').
	 * 
	 * @param geneSet : GeneSet to intercept
	 */
	public void intersection(GeneSet geneSet) {
		if (geneSet != null) genes.retainAll(geneSet.genes);
	}

	@Override
	public Iterator<String> iterator() {
		return genes.iterator();
	}

	/**
	 * Calculate rankSum, rankedSymbols and maxRank
	 * @return rankSum
	 */
	public long rankSum() {
		// Sanity check
		if (!geneSets.isRanked()) throw new RuntimeException("Cannot calculate rank: This is not a ranked gene set ('" + name + "')!");

		if (rankSum >= 0) return rankSum;
		rankSum = 0;
		rankedGenesCount = 0;
		maxRank = 0;
		GeneSetsRanked geneSetsRanked = (GeneSetsRanked) geneSets;
		for (String gene : getInterestingGenes()) {
			int r = geneSetsRanked.getRank(gene);
			if (r > 0) {
				rankedGenesCount++;
				rankSum += r;
			} else throw new RuntimeException("This should never happen!!! Ranked symbol " + gene + " has rank = 0");

			if (maxRank < r) maxRank = r;
		}

		return rankSum;
	}

	/**
	 * Reset 'interesting' genes
	 */
	void reset() {
		maxRank = Integer.MIN_VALUE;
		rankSum = Integer.MIN_VALUE;
		interestingGenesCount = Integer.MIN_VALUE;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGeneSets(GeneSets geneSets) {
		this.geneSets = geneSets;
	}

	public void setMaxRank(int maxRank) {
		this.maxRank = maxRank;
	}

	/**
	 * Perform a 'set minus' between this term and 'geneSet' (set minus for 'geneSet' and 'interestingGeneSet').
	 * @param geneSet : GeneSet to use for 'set minus' operation
	 */
	public void setMinus(GeneSet geneSet) {
		if (geneSet != null) genes.removeAll(geneSet.genes);
	}

	public void setName(String geneSetName) {
		//		name = geneSetName.toUpperCase();
		name = geneSetName;
	}

	public int size() {
		return genes.size();
	}

	/**
	 * Number of genes that have a value
	 * @return
	 */
	public int sizeEffective() {
		int count = 0;
		for (String gene : this)
			if (geneSets.hasValue(gene)) count++;
		return count;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String toStringAll() {
		StringBuffer sb = new StringBuffer();

		sb.append(getName() + "\t" + description + "\tsize: " + size() + "\t");
		for (String gene : genesSorted())
			sb.append(gene + "\t");
		sb.deleteCharAt(sb.length() - 1); // Delete last "\t";

		return sb.toString();
	}

	/**
	 * Union this term with 'geneSet' (union for 'geneSet' and 'interestingGeneSet').
	 * 
	 * @param geneSet : GeneSet to use for union
	 */
	public void union(GeneSet geneSet) {
		if (geneSet != null) genes.addAll(geneSet.genes);
	}
}
