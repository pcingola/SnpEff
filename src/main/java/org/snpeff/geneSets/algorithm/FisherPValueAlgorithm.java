package org.snpeff.geneSets.algorithm;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSets;
import org.snpeff.probablility.FisherExactTest;
import org.snpeff.util.Log;

public class FisherPValueAlgorithm extends EnrichmentAlgorithm {

	double threshold = 0.5;

	public FisherPValueAlgorithm(GeneSets geneSets, int numberToSelect) {
		super(geneSets, numberToSelect);
	}

	@Override
	Apfloat pValue(GeneSet geneSet) {
		// GeneSets geneSets = geneSet.getGeneSets();

		// Notes:
		//     White: Interesting
		//     Drawn: In this gene set
		int k = geneSet.getInterestingGenesCount(); // white marbles drawn
		int N = geneSets.getGeneCount(); // Total marbles
		int D = geneSets.getInterestingGenesCount(); // White marbles
		int n = geneSet.getGeneCount(); // marbles drawn

		double pValue = FisherExactTest.get().fisherExactTestUp(k, N, D, n, threshold);
		if (debug) Log.debug("k: " + k + "\tN: " + N + "\tD: " + D + "\tn: " + n + "\tpValue: " + pValue + "\t" + geneSet.getName());
		return new Apfloat(pValue);
	}
}
