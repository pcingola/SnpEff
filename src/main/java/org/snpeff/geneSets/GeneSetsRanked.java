package org.snpeff.geneSets;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A collection of GeneSets
 * Genes are ranked (usually by 'value')
 * 
 * @author Pablo Cingolani
 */
public class GeneSetsRanked extends GeneSets {

	private static final long serialVersionUID = -7922357470081599803L;
	int maxRank; // Maximum rank in this collection
	HashMap<String, Integer> rankByGene; // Ranked genes

	/**
	 * Default constructor
	 */
	public GeneSetsRanked() {
		super();
		rankByGene = new HashMap<String, Integer>();
		maxRank = 0;
	}

	public GeneSetsRanked(GeneSets geneSets) {
		super(geneSets);
		rankByGene = new HashMap<String, Integer>();
		maxRank = 0;
	}

	public GeneSetsRanked(String msigDb) {
		super();
		rankByGene = new HashMap<String, Integer>();
		maxRank = 0;
		loadMSigDb(msigDb, false);
	}

	/**
	 * Add a 'ranked' gene (to every corresponding GeneSet in this collection) 
	 * @param gene : symbol's ID
	 * @param rank : symbol's rank
	 * @returns : true if it was added OK, false on error.
	 */
	public boolean add(String gene, int rank) {
		if (rank <= 0) throw new RuntimeException("Rank must be a possitive number: " + rank + ", gene '" + gene + "'");

		boolean ok = true;

		// Sanity check
		if (!genes.contains(gene)) {
			if (debug) System.err.println("WARNING: Trying to add ranked gene. Gene  '" + gene + "' does not exist in GeneSets. " + (doNotAddIfNotInGeneSet ? "Ignored." : "Added anyway."));
			ok = false;
			if (doNotAddIfNotInGeneSet) return ok;
			add(gene);
		}

		rankByGene.put(gene, rank); // Add gene -> rank pair 
		interestingGenes.add(gene);
		if (maxRank < rank) maxRank = rank;

		return ok;
	}

	/**
	 * Checks that every symboolID is in the set (as 'interesting' genes)
	 * @param intGenes : A set of interesting genes
	 * Throws an exception on error
	 */
	@Override
	public void checkInterestingGenes(Set<String> intGenes) {
		super.checkInterestingGenes(intGenes);

		// Are ranks being used? => Check them
		if ((rankByGene != null) && (rankByGene.keySet().size() > 0)) {
			int maxRankTmp = intGenes.size();

			// Check that every rank is being used
			int ranksUsed[] = new int[maxRankTmp + 1];
			for (int i = 0; i < maxRankTmp; i++)
				ranksUsed[i] = 0;

			// Check that every interestingSymbolId is ranked (if ranks are being used)
			for (String gene : intGenes) {
				Integer rank = rankByGene.get(gene);
				if ((rank == null) || (rank <= 0) || (rank > maxRankTmp)) { throw new RuntimeException("Invalid rank for gene:" + gene + ", rank:" + rank + "(should be [1," + maxRankTmp + "]"); }
				ranksUsed[rank]++;
			}

			for (int rank = 1; rank < maxRankTmp; rank++) {
				if (ranksUsed[rank] != 1) { throw new RuntimeException("Rank number " + rank + " is used " + ranksUsed[rank] + " times (should be used exactly 1 time)"); }
			}
		}
	}

	/**
	 * Get maximum rank
	 * @return
	 */
	public int getMaxRank() {
		// Calculate it if needed
		if (maxRank <= 0) { // Find max rank used 

			for (String gene : rankByGene.keySet()) {
				int rank = rankByGene.get(gene);
				if (rank > maxRank) maxRank = rank;
			}
		}
		return maxRank;
	}

	/**
	 * Get gene's rank
	 * @param gene
	 * @return
	 */
	public int getRank(String gene) {
		Integer rank = rankByGene.get(gene);
		if (rank == null) { return 0; }
		return rank;
	}

	/**
	 * Get geneId <-> Rank mapping
	 * @return
	 */
	public HashMap<String, Integer> getRankByGene() {
		return rankByGene;
	}

	/**
	 * How many gene sets have ranked genes (i.e. rank sum > 0)
	 * 
	 * @return Number of gene set such that rankSum > 0
	 */
	public int getRankedSetsCount() {
		int count = 0;
		for (GeneSet gs : this)
			if (gs.rankSum() > 0) count++;
		return count;
	}

	@Override
	public boolean isRanked() {
		return true;
	}

	public boolean isRanked(String geneName) {
		return rankByGene.containsKey(geneName);
	}

	/**
	 * Is this gene set used? I.e. is there at least one gene 'used'? (e.g. interesting or ranked)
	 * @param gs
	 * @return
	 */
	@Override
	protected boolean isUsed(GeneSet gs) {
		for (String gene : gs) {
			if (isInteresting(gene)) return true;
			if (isRanked(gene)) return true;
		}
		return true;
	}

	@Override
	protected boolean isUsed(String geneName) {
		return isRanked(geneName);
	}

	/**
	 * Reads a file with a list of genes and experimental values.
	 * Format: "gene \t value \n"
	 * @param fileName 
	 * @return A list of genes not found
	 */
	@Override
	public List<String> loadExperimentalValues(String fileName, boolean maskException) {
		List<String> notFound = super.loadExperimentalValues(fileName, maskException);
		rankByValue(false);
		return notFound;
	}

	/**
	 * Rank genes by value
	 */
	@SuppressWarnings("unchecked")
	public int rankByValue(boolean orderAscending) {
		// Sort by experimental value then rank them
		LinkedList<String> geneNames = new LinkedList<String>(valueByGene.keySet());
		Collections.sort(geneNames, new CompareByValue(valueByGene, orderAscending));
		int rank = 1, errorsRank = 0;
		for (String gene : geneNames)
			if (!add(gene, rank++)) errorsRank++;

		if (verbose && (errorsRank > 0)) System.err.println(String.format("Misisng %d genes, out of %d genes ( %.1f %% ).", errorsRank, geneNames.size(), ((errorsRank * 100.0) / geneNames.size())));
		return errorsRank;
	}

	/**
	 * Reset every 'interesting' gene or ranked gene (on every single GeneSet in this GeneSets)
	 */
	@Override
	public void reset() {
		super.reset();
		rankByGene = new HashMap<String, Integer>();
		maxRank = 0;
	}
}
