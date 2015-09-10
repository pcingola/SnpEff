package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Base class: Provides common methods used for testing
 */
public class TestCasesBase {

	public boolean debug = false;
	public boolean verbose = false || debug;

	public TestCasesBase() {
		super();
	}

	/**
	 * Load predictor and create regions
	 */
	public SnpEffectPredictor loadSnpEffectPredictorAnd(String genome, boolean build) {
		Config config = new Config(genome);
		SnpEffectPredictor sep = SnpEffectPredictor.load(config);
		sep.createGenomicRegions();
		if (build) sep.buildForest();
		return sep;
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		// Arguments
		ArrayList<String> args = new ArrayList<String>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		cmdEff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		return list;
	}

}
