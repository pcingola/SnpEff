package org.snpeff.geneSets.algorithm;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSets;
import org.snpeff.gsa.ScoreList;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.util.Log;

/**
 * Leading edge fraction algorithm
 *
 * References: "Common Inherited Variation in Mitochondrial Genes Is Not Enriched for Associations with Type 2 Diabetes or Related Glycemic Traits"
 * 				http://www.plosgenetics.org/article/info%3Adoi%2F10.1371%2Fjournal.pgen.1001058
 * 				See page 12, "Step 4"
 *
 * @author pablocingolani
 */
public class LeadingEdgeFractionAlgorithm extends FisherPValueGreedyAlgorithm {

	public static final double SCORE_CUTOFF_QUANTILE_DEFAULT = 0.05; // This it the value used in the paper (top 95%)
	public static final Apfloat ONE = new Apfloat(1.0);

	boolean orderDescending = false; // If 'true', high scores are better (sort descending and get the first values)
	double scoreCutOff;
	double scoreCutOffQuantile = SCORE_CUTOFF_QUANTILE_DEFAULT;
	int N, D;

	public LeadingEdgeFractionAlgorithm(GeneSets geneSets, int numberToSelect, boolean orderDescending) {
		super(geneSets, numberToSelect);
		this.orderDescending = orderDescending;
		init();
	}

	/**
	 * Initialize parameters
	 */
	void init() {
		// Calculate pvalue cutoff
		scoreCutOff = scoreCutOff();

		//---
		// Calculate Fisher parameters
		//---
		N = D = 0;
		for (String geneId : geneSets.getGenes())
			if (geneSets.hasValue(geneId)) {
				N++;
				if (isTopScore(geneId)) D++;
			}

		if (verbose) Log.info("Fisher Exact test parameters:\n\tN : " + N + "\n\tD : " + D);
	}

	/**
	 * Is this a 'top' score (better than cutoff)
	 * @param geneId
	 * @return
	 */
	boolean isTopScore(String geneId) {
		double val = geneSets.getValue(geneId);
		return (!orderDescending && (val <= scoreCutOff)) //
				|| (orderDescending && (scoreCutOff <= val));

	}

	/**
	 * Create a new gene set using all gene sets and calculate pValue
	 * @param geneSetList
	 * @return
	 */
	@Override
	Apfloat pValue(GeneSet geneSet) {
		// Count number of p-values less or equal to 'pValueCutOff'
		int count = 0, tot = 0;
		for (String geneId : geneSet) {
			if (geneSets.hasValue(geneId)) {
				if (isTopScore(geneId)) count++;
				tot++;
			}
		}

		// No genes have values? We are done
		if (tot <= 0) return ONE;

		if (debug) {
			// Calculate and show 'leading edge fraction'
			double leadingEdgeFraction = ((double) count) / ((double) tot);
			Log.info("Gene set: " + geneSet.getName() + "\tsize: " + geneSet.size() + "\tsize (eff): " + geneSet.sizeEffective() + "\t" + count + "\tleadingEdgeFraction: " + leadingEdgeFraction);
		}

		// Calculate p-value
		double pvalueFisher = FisherExactTest.get().fisherExactTestUp(count, N, D, tot);
		return new Apfloat(pvalueFisher);
	}

	/**
	 * Calculate 'scoreCutOff' (see paper methods)
	 * @return
	 */
	double scoreCutOff() {
		// Create a list of p-values
		ScoreList scoreList = new ScoreList();
		for (String geneId : geneSets.getGenes())
			if (geneSets.hasValue(geneId)) scoreList.add(geneSets.getValue(geneId));

		double quantile = scoreCutOffQuantile;
		if (orderDescending) quantile = 1 - scoreCutOffQuantile;

		double pco = scoreList.quantile(quantile);

		// Show
		if (verbose) Log.info("Calculate pValue_CutOff: " //
				+ "\n\tSize (effective) : " + scoreList.size() //
				+ "\n\tQuantile         : " + scoreCutOffQuantile //
				+ "\n\tScore CutOff     : " + pco //
		);
		if (debug) Log.debug("\tp-values: " + scoreList);

		return pco;
	}

	public void setOrderDescending(boolean orderDescending) {
		this.orderDescending = orderDescending;
	}

	public void setpValueCutOffQuantile(double pValueCutOffQuantile) {
		scoreCutOffQuantile = pValueCutOffQuantile;
		scoreCutOff = scoreCutOff();
	}
}
