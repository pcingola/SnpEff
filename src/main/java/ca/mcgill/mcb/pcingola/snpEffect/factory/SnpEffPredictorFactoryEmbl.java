package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.genBank.EmblFile;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from an Embl file.
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryEmbl extends SnpEffPredictorFactoryFeatures {

	public SnpEffPredictorFactoryEmbl(Config config) {
		super(config);
		fileName = config.getBaseFileNameGenes() + ".embl";
		featuresFile = new EmblFile(fileName);
	}

	public SnpEffPredictorFactoryEmbl(Config config, String emblFileName) {
		super(config);
		fileName = emblFileName;
		featuresFile = new EmblFile(fileName);
	}
}
