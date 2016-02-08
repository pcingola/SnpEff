package org.snpeff.geneSets.algorithm;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSetsRanked;
import org.snpeff.probablility.RankSumNoReplacementPdf;

public class RankSumPValueAlgorithm extends EnrichmentAlgorithm {

	public RankSumPValueAlgorithm(GeneSetsRanked geneSets, int numberToSelect) {
		super(geneSets, numberToSelect);
	}

	/**
	 * Create a new gene set using all gene sets and calculate pValue
	 * @param geneSetList
	 * @return
	 */
	@Override
	Apfloat pValue(GeneSet geneSet) {
		long rankSum = geneSet.rankSum(); // Make sure rankSum is calculated
		Apfloat pValue = RankSumNoReplacementPdf.get().cdf(((GeneSetsRanked) geneSets).getMaxRank(), geneSet.getRankedGenesCount(), rankSum);
		return pValue;
	}
}
