package org.snpeff.snpEffect.factory;

import org.snpeff.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from a GFF3 file
 *
 * References:
 * 		- http://www.sequenceontology.org/gff3.shtml
 * 		- http://gmod.org/wiki/GFF3
 * 		- http://www.eu-sol.net/science/bioinformatics/standards-documents/gff3-format-description
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryGff3 extends SnpEffPredictorFactoryGff {

	public SnpEffPredictorFactoryGff3(Config config) {
		super(config);
		version = "GFF3";
	}

}
