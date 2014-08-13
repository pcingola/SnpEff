package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VariantTxtFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = true || debug;

	boolean createOutputFile = false;
	Random rand;
	Config config;
	Genome genome;

	public TestCasesZzz() {
		super();
		initRand();
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	/**
	 * Compare each result. If one matches, we consider it OK
	 */
	boolean anyResultMatches(String transcriptId, Variant variant, VariantEffects changeEffects, boolean useShort) {
		for (VariantEffect chEff : changeEffects) {
			String resStr = chEff.toStringSimple(useShort);

			Transcript tr = chEff.getTranscript();
			if (tr != null) {
				if ((transcriptId == null) || (transcriptId.equals(tr.getId()))) {
					if (resStr.indexOf(variant.getId()) >= 0) return true; // Matches one result in this transcript
				}
			} else if (resStr.indexOf(variant.getId()) >= 0) return true; // Matches any result (out of a transcript)
		}
		return false;
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		initSnpEffPredictor("testCase");
	}

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		config.setTreatAllAsProteinCoding(true); // For historical reasons we set this one to 'true'....
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Parse a variant file and return a list
	 *
	 * @param variantFile
	 * @return
	 */
	public List<Variant> parseSnpEffectFile(String variantFile) {
		ArrayList<Variant> variants = new ArrayList<Variant>();

		VariantTxtFileIterator variantFileIterator = new VariantTxtFileIterator(variantFile, config.getGenome());
		for (Variant sc : variantFileIterator)
			variants.add(sc);

		Collections.sort(variants);
		return variants;
	}

	/**
	 * Calculate snp effect for a list of snps
	 * @param snpEffFile
	 */
	public void snpEffect(List<Variant> variantList, String transcriptId, boolean useShort, boolean negate) {
		int num = 1;
		// Predict each variant
		for (Variant variant : variantList) {
			// Get results for each snp
			VariantEffects results = config.getSnpEffectPredictor().variantEffect(variant);

			String msg = "";
			msg += "Number : " + num + "\n";
			msg += "\tExpecting   : " + (negate ? "NOT " : "") + "'" + variant.getId() + "'\n";
			msg += "\tVariant     : " + variant + "\n";
			msg += "\tResultsList :\n";
			for (VariantEffect res : results)
				msg += "\t\t" + res + "\n";

			if (verbose) System.out.println(msg);

			// Compare each result. If one matches, we consider it OK
			// StringBuilder resultsSoFar = new StringBuilder();
			boolean ok = anyResultMatches(transcriptId, variant, results, useShort);
			ok = negate ^ ok; // Negate? (i.e. when we are looking for effects that should NOT be matched)

			if (!ok) {
				if (createOutputFile) {
					for (VariantEffect res : results) {
						Variant sc = res.getVariant();
						System.out.println(sc.getChromosomeName() //
								+ "\t" + (sc.getStart() + 1) //
								+ "\t" + sc.getReference() //
								+ "\t" + sc.getAlt() //
								+ "\t+\t0\t0" //
								+ "\t" + res.effect(true, true, true, false) //
						);
					}
				} else {
					Gpr.debug(msg);
					throw new RuntimeException(msg);
				}
			}
			num++;
		}
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure at least one effect matched the 'id' in the input TXT file
	 */
	public void snpEffect(String snpEffFile, String transcriptId, boolean useShort) {
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, false); // Predict each snp
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure NOT A SINGLE effect matched the 'id' in the input TXT file, i.e. the opposite of snpEffect...) method.
	 */
	public void snpEffectNegate(String snpEffFile, String transcriptId, boolean useShort) {
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, true); // Predict each snp
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21() {
		Gpr.debug("Test");
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_InDels.out", trId, false);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_24_delete_exon_utr() {
		Gpr.debug("Test");
		initSnpEffPredictor();
		snpEffect("tests/delete_exon_utr.txt", null, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_zzz() {
		Gpr.debug("Test");
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/z.txt", trId, false);
	}

}
