package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.genBank.EmblFile;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from an Embl file.
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryEmbl extends SnpEffPredictorFactoryFeatures {

	public static final String EXTENSION_EMBL = ".embl";

	public SnpEffPredictorFactoryEmbl(Config config) {
		super(config);
		fileName = config.getBaseFileNameGenes() + EXTENSION_EMBL;
		featuresFile = new EmblFile(fileName);
	}

	public SnpEffPredictorFactoryEmbl(Config config, String emblFileName) {
		super(config);
		fileName = emblFileName;
		featuresFile = new EmblFile(fileName);
	}
}
