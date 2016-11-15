package org.snpeff.snpEffect.factory;

import org.snpeff.genBank.GenBankFile;
import org.snpeff.snpEffect.Config;

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
		this.fileName = fileName;
		featuresFile = new GenBankFile(fileName);
	}
}
