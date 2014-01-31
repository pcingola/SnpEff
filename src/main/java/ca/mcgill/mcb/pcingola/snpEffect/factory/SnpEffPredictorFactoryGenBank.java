package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.genBank.GenBankFile;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from a GenBank file.
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryGenBank extends SnpEffPredictorFactoryFeatures {

	public SnpEffPredictorFactoryGenBank(Config config) {
		super(config);
		fileName = config.getBaseFileNameGenes() + ".gb";
		featuresFile = new GenBankFile(fileName);
	}
}
