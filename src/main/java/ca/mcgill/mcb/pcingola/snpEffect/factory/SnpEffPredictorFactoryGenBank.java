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
		this(config, config.getBaseFileNameGenes() + EXTENSION_GENBANK);
	}

	public SnpEffPredictorFactoryGenBank(Config config, String fileName) {
		super(config);
		featuresFile = new GenBankFile(fileName);
	}
}
