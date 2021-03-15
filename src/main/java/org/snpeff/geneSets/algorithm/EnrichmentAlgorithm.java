package org.snpeff.geneSets.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSets;
import org.snpeff.geneSets.GeneSetsRanked;
import org.snpeff.geneSets.Result;
import org.snpeff.util.Log;

/**
 * A generic enrichment algorithm for selecting gene-sets from a collection of gene-sets
 *
 * @author pcingola
 */
public abstract class EnrichmentAlgorithm {

	public enum EnrichmentAlgorithmType {
		FISHER_GREEDY, RANKSUM_GREEDY, FISHER, RANKSUM, LEADING_EDGE_FRACTION, NONE;

		/**
		 * Is the algorithm base on "interesting / not-interesting" binary clasification?
		 * @return
		 */
		public boolean isBinary() {
			return (this == FISHER) || (this == FISHER_GREEDY);
		}

		/**
		 * Is this a greedy algorithm?
		 * @return
		 */
		public boolean isGreedy() {
			return (this == FISHER_GREEDY) || (this == RANKSUM_GREEDY);
		}

		/**
		 * Does the algorithm need a rank statistic?
		 * @return
		 */
		public boolean isRank() {
			return (this == RANKSUM) || (this == RANKSUM_GREEDY);
		}

	}

	public static final int HTML_TD_GENES_MAX_LEN = 40;
	public static final String HTML_BG_COLOR[] = { "dddddd", "eeeeee" };
	public static final String HTML_BG_COLOR_TITLE = "cccccc";

	public static long PRINT_SOMETHING_TIME = 5000; // Print something every X milliseconds
	boolean debug = false;
	boolean verbose = false;
	boolean htmlTable = false;
	int minGeneSetSize = 0;
	int maxGeneSetSize = Integer.MAX_VALUE;
	int numberToSelect;
	double maxPValue = Double.NaN;
	double maxPvalueAdjusted = 0.05;
	StringBuilder output = new StringBuilder();
	GeneSets geneSets;
	Set<String> filterOutputGeneSets;

	public EnrichmentAlgorithm(GeneSets geneSets, int numberToSelect) {
		this.geneSets = geneSets;
		this.numberToSelect = numberToSelect;
	}

	public double getMaxPvalueAdjusted() {
		return maxPvalueAdjusted;
	}

	public StringBuilder getOutput() {
		return output;
	}

	/**
	 * Showld we show this result or should the output be filtered?
	 * @param result
	 * @return
	 */
	protected boolean isShow(Result result) {
		// Filter by pValue
		if (Double.isNaN(maxPValue)) {
			// Use adjusted p-value
			if (result.getPvalueAdjusted() >= maxPvalueAdjusted) return false;
		} else {
			// Use un-adjusted p-value
			if (result.getPvalue().doubleValue() >= maxPValue) return false;
		}

		// Filter by Gene set name (show only if it is in this list)
		if (filterOutputGeneSets != null) {
			String gsName = result.getLatestGeneSet().getName();
			boolean show = filterOutputGeneSets.contains(gsName);
			if (verbose) Log.info("\tFilter output list. Show geneSet '" + gsName + "' : " + show);
			return show;
		}

		// OK
		return true;
	}

	protected void print(String str) {
		System.out.println(str);
		output.append(str + "\n");
	}

	/**
	 * Print after all genesets are shown
	 */
	void printEnd() {
		if (htmlTable) print("</table>");
	}

	/**
	 * Show a result
	 * @param it : Iteration or Rank
	 * @param result
	 */
	void printResult(int it, Result result) {
		if ((result != null) && (result.getLatestGeneSet() != null)) {

			if (isShow(result)) {
				GeneSet geneSet = result.getLatestGeneSet();

				//---
				// Get genes information
				//---
				StringBuffer interestingGenes = new StringBuffer();
				ArrayList<String> genesList = new ArrayList<String>();
				genesList.addAll(geneSet.getInterestingGenes());
				Collections.sort(genesList);
				for (String gene : genesList) {
					interestingGenes.append(gene);

					// Show score (value) and rank information?
					if (geneSets.hasValue(gene) || geneSets.isRanked()) {
						interestingGenes.append("[");

						// Append value
						if (geneSets.hasValue(gene)) interestingGenes.append(geneSets.getValue(gene));

						// Append rank
						if (geneSets.isRanked()) {
							GeneSetsRanked geneSetsRanked = ((GeneSetsRanked) geneSets);
							interestingGenes.append("/" + geneSetsRanked.getRank(gene));
						}

						interestingGenes.append("]");
					}

					interestingGenes.append(" ");
				}

				//---
				// Print output
				//---
				if (htmlTable) {
					// Show as HTML table
					String descr = geneSet.getDescription();
					String name = geneSet.getName();

					if (descr.startsWith("http://")) {
						name = "<a href=\"" + descr + "\">" + name + "</a>";
						descr = "<a href=\"" + descr + "\">link</a>";
					}

					String intGenes = interestingGenes.toString();
					if (interestingGenes.length() > HTML_TD_GENES_MAX_LEN) intGenes = "<textarea rows=1 cols=" + HTML_TD_GENES_MAX_LEN + ">" + interestingGenes + "</textarea>";

					String bgcolor = HTML_BG_COLOR[it % 2];

					print("\t<tr bgcolor=\"" + bgcolor + "\"> <td nowrap>" + it //
							+ "</td>\t<td nowrap>" + String.format("%.2e", result.getPvalue().doubleValue()) //
							+ "</td>\t<td nowrap>" + String.format("%.2e", result.getPvalueAdjusted()) //
							+ "</td>\t<td nowrap>" + name //
							+ "</td>\t<td nowrap>" + geneSet.size() //
							+ "</td>\t<td nowrap>" + descr //
							+ "</td>\t<td nowrap>" + intGenes //
							+ "</td>\t<td nowrap>" + (geneSets.isRanked() ? geneSet.rankSum() : 0) //
							+ "</td>\t</tr>");
				} else {
					// Show as "normal" TXT
					print(it //

							+ "\t" + result.getPvalue() //
							+ "\t" + result.getPvalueAdjusted() //
							+ "\t" + geneSet.getName() //
							+ "\t" + geneSet.size() //
							+ "\t" + geneSet.getDescription() //
							+ "\t" + result.getGeneSets() //
							+ "\t" + interestingGenes //
							+ "\t" + (geneSets.isRanked() ? geneSet.rankSum() : 0) //
					);
				}

			}
		} // else print("\t" + it + "\tNULL");
	}

	/**
	 * Print before all genesets are shown
	 */
	void printTitle() {
		if (htmlTable) print("<table border=0> <tr bgcolor=\"" + HTML_BG_COLOR_TITLE + "\"> <th>Rank</th>\t<th>p-value</th>\t<th>p-value adj</th>\t<th>Latest result</th>\t<th>Size</th>\t<th>Description</th>\t<th>Interesting genes </th>\t<th> Score </th> </tr>");
		else if (verbose) print("Iteration\tp-value\tp-value adj\tLatest result\tSize\tDescription\tResult\tInteresting genes");
	}

	/**
	 * Calculate the pValue for a given geneSet
	 * @param geneSetList
	 * @return
	 */
	abstract Apfloat pValue(GeneSet geneSet);

	/**
	 * Create a new gene set using all gene sets and calculate pValue
	 * @param geneSetList
	 * @return
	 */
	Apfloat pValue(List<GeneSet> geneSetList) {
		GeneSet newGeneSet = new GeneSet(geneSetList, geneSets);
		return pValue(newGeneSet);
	}

	/**
	 * Select the 'best' gene sets
	 * @return
	 */
	public Result select() {
		Result best = new Result();

		//---
		// Calculate pValues for each gene set matching our criteria
		//---
		List<Result> results = new ArrayList<Result>();
		for (GeneSet geneSet : geneSets) {
			if ((geneSet.getGeneCount() > 0) // This term is empty? => skip it
					&& (geneSet.getGeneCount() >= minGeneSetSize) // Use gene sets bigger than minGeneSetSize
					&& (geneSet.getGeneCount() <= maxGeneSetSize) // Use gene sets smaller than maxGeneSetSize
			) {
				// Calculate pValue
				Apfloat pValue = pValue(geneSet);
				Result result = new Result(geneSet, pValue, 0); // We'll update the geneSetCount later
				results.add(result);
			}
		}

		// Update the geneSetCount
		for (Result res : results)
			res.setGeneSetCountLast(results.size());

		//---
		// Show results
		//---
		if (htmlTable || verbose) {
			printTitle();

			// Sort by pValue
			Collections.sort(results);

			// Show them
			int rank = 1;
			for (Result r : results)
				printResult(rank++, r);

			printEnd();
		}

		return best;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setFilterOutputGeneSets(Set<String> filterOutputGeneSets) {
		this.filterOutputGeneSets = filterOutputGeneSets;
	}

	public void setHtmlTable(boolean htmlTable) {
		this.htmlTable = htmlTable;
	}

	public void setMaxGeneSetSize(int maxGeneSetSize) {
		this.maxGeneSetSize = maxGeneSetSize;
	}

	public void setMaxPValue(double maxPValue) {
		this.maxPValue = maxPValue;
	}

	public void setMaxPvalueAdjusted(double maxPvalueAdjusted) {
		this.maxPvalueAdjusted = maxPvalueAdjusted;
	}

	public void setMinGeneSetSize(int minGeneSetSize) {
		this.minGeneSetSize = minGeneSetSize;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
