package org.snpeff.geneSets.algorithm;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSets;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.util.Log;

public class FisherPValueGreedyAlgorithm extends EnrichmentAlgorithmGreedyVariableSize {

	// double threshold = 0.1;

	public FisherPValueGreedyAlgorithm(GeneSets geneSets, int numberToSelect) {
		super(geneSets, numberToSelect);
	}

	@Override
	Apfloat pValue(GeneSet geneSet) {
		// Notes:
		//     White: Interesting
		//     Drawn: In this gene set
		int k = geneSet.getInterestingGenesCount(); // white marbles drawn
		int N = geneSets.getGeneCount(); // Total marbles
		int D = geneSets.getInterestingGenesCount(); // White marbles
		int n = geneSet.getGeneCount(); // marbles drawn

		//double pValue = FisherExactTest.get().fisherExactTestUpThreshold(k, N, D, n, threshold);
		double pValue = FisherExactTest.get().fisherExactTestUp(k, N, D, n);
		if (debug) Log.debug("Fisher exact test\tk: " + k + "\tN: " + N + "\tD: " + D + "\tn: " + n + "\tpValue: " + pValue);
		return new Apfloat(pValue);
	}
}
