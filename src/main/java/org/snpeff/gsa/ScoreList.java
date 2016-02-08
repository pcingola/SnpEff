package org.snpeff.gsa;

import gnu.trove.list.array.TDoubleArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Gamma;

/**
 * A list of scores 
 * 
 * @author pcingola
 */
public class ScoreList {

	public enum ScoreSummary {
		MIN, AVG, AVG_MIN_10, FISHER_CHI_SQUARE, Z_SCORES, SIMES, BONFERRONI, FDR, MAX, AVG_MAX_10, SUM
	}

	public static final double SIGNIFICANCE_LEVEL_95 = 0.05;

	String geneId;
	TDoubleArrayList scores;
	boolean sorted = false;

	/**
	 * Upper tail 1 - ChiSquareCDF(p)
	 * @param chiSquare
	 * @param nu
	 * @return
	 */
	public static double chiSquareCDFComplementary(double chiSquare, int nu) {
		if (nu <= 0) throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
		return Gamma.regularizedGammaQ(nu / 2.0D, chiSquare / 2.0D);
	}

	public ScoreList() {
		scores = new TDoubleArrayList();
	}

	/**
	 * Add a p-value to the list
	 * @param score
	 */
	public void add(double score) {
		scores.add(score);
	}

	/**
	 * Get average 
	 * @return
	 */
	public double avg() {
		if (size() <= 0) return getDefaultValue();

		double sum = 0.0;
		for (int i = 0; i < size(); i++)
			sum += getScore(i);

		return sum / size();
	}

	/**
	 * Get average pvalue (largest N)
	 * @return
	 */
	public double avgLargestTop(int topN) {
		if (size() <= 0) return getDefaultValue();

		sort(); // Sort collection

		// Sum smallest values
		double sum = 0.0;
		int min = Math.max(0, size() - topN);
		int count = 0;
		for (int i = min; i < size(); i++, count++)
			sum += getScore(i);

		return sum / count;
	}

	/**
	 * Get average score (smallest N)
	 * @return
	 */
	public double avgSmallestTop(int topN) {
		if (size() <= 0) return getDefaultValue();

		sort(); // Sort collection

		// Sum smallest values
		double sum = 0.0;
		int max = Math.min(size(), topN);
		for (int i = 0; i < max; i++)
			sum += getScore(i);

		return sum / max;
	}

	/**
	 * Cumulative distribution function of p-values: 
	 * 
	 * 			P[ pValues <= p ]		(i.e. lower tail).
	 * 
	 * @param p
	 * @return
	 */
	public double cdf(double p) {
		if (size() <= 0) return 1;

		sort();
		int idx = scores.binarySearch(p);
		if (idx < 0) idx = -(idx + 1); // If 'p' is not found, idx is (-insertion_point - 1);

		return ((double) idx) / size();
	}

	/**
	 * Cumulative distribution function of p-values: 
	 * 
	 * 			P[ pValues > p ]		(i.e. upper tail).
	 * 
	 * @param p
	 * @return
	 */
	public double cdfUpper(double p) {
		if (size() <= 0) return 1;

		sort();
		int idx = scores.binarySearch(p);
		if (idx < 0) idx = -(idx + 1); // If 'p' is not found, idx is (-insertion_point - 1);

		return ((double) (size() - idx)) / size();
	}

	protected double getDefaultValue() {
		return 0.0;
	}

	public String getGeneId() {
		return geneId;
	}

	public double getScore(int index) {
		return scores.get(index);
	}

	/**
	 * Get minimum pvalue
	 * @return
	 */
	public double max() {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < size(); i++)
			max = Math.max(max, getScore(i));
		return max;
	}

	/**
	 * Get minimum pvalue
	 * @return
	 */
	public double min() {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < size(); i++)
			min = Math.min(min, getScore(i));
		return min;
	}

	/**
	 * Minimum p-value corrected using Bonferroni
	 * @return
	 */
	public double pValueBonferroni() {
		if (scores.size() <= 0) return 1.0;
		return Math.min(1.0, min() * scores.size());
	}

	/**
	 * Combine p-values using FDR procedure
	 * 
	 * References: http://en.wikipedia.org/wiki/False_discovery_rate
	 * 
	 * @return A combined p-value
	 */
	public double pValueFdr(double alpha) {
		if (size() <= 0) return 1.0;

		// Count non-zero p-values (we treat zero p-values as errors, so we skip them) 
		int total = 0;
		for (int i = 0; i < size(); i++)
			if (getScore(i) > 0) total++;
		double tot = total;

		// No p-value? => We are done
		if (total <= 0) return 1.0;

		sort(); // Sort collection

		// Perform Simes's method
		int count = 0;
		double pFdrMax = 0.0;
		for (int i = 0; i < size(); i++) {
			double pvalue = getScore(i);

			// We treat zero p-values as errors, so we skip them 
			if (pvalue > 0) {
				count++;
				double pFdr = tot * pvalue / count;
				if ((pFdr <= alpha) || (pFdr < pFdrMax)) pFdrMax = pFdr;
			}
		}

		return pFdrMax;
	}

	/**
	 * Combine p-values using Fisher's method
	 * 
	 * References: http://en.wikipedia.org/wiki/Fisher's_method
	 * 
	 * @return
	 */
	public double pValueFisherChi2() {
		if (size() <= 0) return 1.0;

		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < size(); i++) {
			double pvalue = getScore(i);

			// If pvalue == 0, it produces an error (log will be -Inf)
			if (pvalue > 0) {
				sum += Math.log(pvalue);
				count++;
			}
		}

		// Nothing added? 
		if (count <= 0) return 1.0;

		// Get new p-value
		double chi2 = -2.0 * sum;
		int k = 2 * count;
		double pValue = chiSquareCDFComplementary(chi2, k); // 1 - ChiSquareCDF_{k}( chi2 )

		return pValue;
	}

	/**
	 * Combine p-values using Simes's procedure
	 * 
	 * References: http://biomet.oxfordjournals.org/content/73/3/751
	 * 
	 * @return A combined p-value
	 */
	public double pValueSimes() {
		if (size() <= 0) return 1.0;

		// Count non-zero p-values (we treat zero p-values as errors, so we skip them) 
		int total = 0;
		for (int i = 0; i < size(); i++)
			if (getScore(i) > 0) total++;
		double tot = total;

		// No p-value? => We are done
		if (total <= 0) return 1.0;

		sort(); // Sort collection

		// Perform Simes's method
		int count = 0;
		double pSimesMin = 1.0;
		for (int i = 0; i < size(); i++) {
			double pvalue = getScore(i);

			// We treat zero p-values as errors, so we skip them 
			if (pvalue > 0) {
				count++;
				double pSimes = (pvalue * tot) / count;
				pSimesMin = Math.min(pSimesMin, pSimes);
			}
		}

		return pSimesMin;
	}

	/**
	 * Combine p-values using Stouffer's Z-score method
	 * 
	 * References: http://en.wikipedia.org/wiki/Fisher's_method  (scroll down to Stouffer's method)
	 * 
	 * @return A combined p-value
	 */
	public double pValueZScore() {
		if (size() <= 0) return 1.0;

		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < size(); i++) {
			double pvalue = getScore(i);

			// If pvalue == 0, it produces an error (normal inverse is -Inf)
			if (pvalue > 0) {
				double z = new NormalDistribution().inverseCumulativeProbability(pvalue);
				sum += z;

				count++;
			}
		}

		// Nothing added? 
		if (count <= 0) return 1.0;

		// Get new p-value
		double zsum = sum / Math.sqrt(count);
		double pValue = new org.apache.commons.math3.distribution.NormalDistribution(0.0, 1.0).cumulativeProbability(zsum);

		return pValue;
	}

	/**
	 * Get pvalue quantile
	 * @return
	 */
	public double quantile(double quantile) {
		if (quantile < 0.0 || quantile > 1.0) throw new RuntimeException("Quantile out of range: " + quantile + " .Expected range [0.0 , 1.0].");
		if (size() <= 0) return 1.0;

		sort(); // Sort collection
		int num = (int) (quantile * size());
		return scores.get(num);
	}

	/**
	 * Create a single pValue representing the gene
	 * @return
	 */
	public double score(ScoreSummary pvalueSummary) {
		switch (pvalueSummary) {
		case MIN:
			return min();

		case MAX:
			return max();

		case AVG:
			return avg();

		case AVG_MIN_10:
			return avgSmallestTop(10);

		case AVG_MAX_10:
			return avgLargestTop(10);

		case FISHER_CHI_SQUARE:
			return pValueFisherChi2();

		case Z_SCORES:
			return pValueZScore();

		case SIMES:
			return pValueSimes();

		case BONFERRONI:
			return pValueBonferroni();

		case FDR:
			return pValueFdr(SIGNIFICANCE_LEVEL_95);

		case SUM:
			return sum();

		default:
			throw new RuntimeException("Unimplemented method for summary '" + pvalueSummary + "'");
		}
	}

	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	public int size() {
		return scores.size();
	}

	void sort() {
		if (!sorted) {
			scores.sort();
			sorted = true;
		}
	}

	/**
	 * Get sum of scores
	 * @return
	 */
	public double sum() {
		double sum = 0.0;
		for (int i = 0; i < size(); i++)
			sum += getScore(i);

		return sum;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(geneId + "\t" + size() + "\t");
		for (int i = 0; i < size(); i++)
			sb.append(String.format(" %.2e", getScore(i)));

		return sb.toString();
	}

}
