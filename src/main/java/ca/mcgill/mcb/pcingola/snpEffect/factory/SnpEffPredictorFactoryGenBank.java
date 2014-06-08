package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.genBank.GenBankFile;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from a GenBank file.
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryGenBank extends SnpEffPredictorFactoryFeatures {

	public static final String EXTENSION_GENBANK = ".gbk";

	public SnpEffPredictorFactoryGenBank(Config config) {
		super(config);
		fileName = config.getBaseFileNameGenes() + EXTENSION_GENBANK;
		featuresFile = new GenBankFile(fileName);
	}
}
