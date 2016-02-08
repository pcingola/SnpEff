package org.snpeff.geneSets.algorithm;

import org.apfloat.Apfloat;
import org.snpeff.geneSets.GeneSet;
import org.snpeff.geneSets.GeneSets;

/**
 * An algorithm that does nothing
 *
 * @author pcingola
 */
public class NoneAlgorithm extends EnrichmentAlgorithm {

	public NoneAlgorithm(GeneSets geneSets) {
		super(geneSets, 0);
	}

	/**
	 * Create a new gene set using all gene sets and calculate pValue
	 * @param geneSetList
	 * @return
	 */
	@Override
	Apfloat pValue(GeneSet geneSet) {
		return Apfloat.ONE;
	}

}
