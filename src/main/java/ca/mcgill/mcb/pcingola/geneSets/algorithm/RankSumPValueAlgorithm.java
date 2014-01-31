package ca.mcgill.mcb.pcingola.geneSets.algorithm;

import org.apfloat.Apfloat;

import ca.mcgill.mcb.pcingola.geneSets.GeneSet;
import ca.mcgill.mcb.pcingola.geneSets.GeneSetsRanked;
import ca.mcgill.mcb.pcingola.probablility.RankSumNoReplacementPdf;

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
