package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ca.mcgill.mcb.pcingola.fileIterator.VariantTxtFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Compare effects in tests cases
 *
 * @author pcingola
 */
public class CompareEffects {

	boolean verbose = false;
	boolean debug = false;
	boolean createOutputFile = false;
	Config config;
	Genome genome;
	Random rand;
	long randSeed;
	String genomeName;
	SnpEffectPredictor snpEffectPredictor;

	public CompareEffects(SnpEffectPredictor snpEffectPredictor, long randSeed, boolean verbose) {
		genomeName = "test";
		this.randSeed = randSeed;
		this.verbose = verbose;
		this.snpEffectPredictor = snpEffectPredictor;
		initRand();
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	public CompareEffects(String genomeName, long randSeed, boolean verbose) {
		this.genomeName = genomeName;
		this.randSeed = randSeed;
		this.verbose = verbose;
		initRand();
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	/**
	 * Compare each result. If one matches, we consider it OK
	 */
	boolean anyResultMatches(String transcriptId, Variant variant, VariantEffects variantEffects, boolean useShort) {
		for (VariantEffect chEff : variantEffects) {
			String resStr = chEff.toStringSimple(useShort);
			String expectedEff = variant.getId();

			Transcript tr = chEff.getTranscript();
			if (tr != null) {
				if ((transcriptId == null) || (transcriptId.equals(tr.getId()))) {
					if (compareOK(expectedEff, resStr)) return true; // Matches one result in this transcript
				}
			} else if (compareOK(expectedEff, resStr)) return true; // Matches any result (out of a transcript)
		}
		return false;
	}

	boolean compareAa(String expectedEffs, String realEffs) {
		String expAa = findAa(expectedEffs);
		if (expAa.isEmpty()) return true; // We don't have AA information to compare

		String realAa = findAa(realEffs);
		return expAa.equals(realAa);
	}

	/**
	 * Compare effects
	 */
	boolean compareEff(String expectedEffs, String realEffs) {
		for (String expEff : findEffTypes(expectedEffs))
			for (String realEff : findEffTypes(realEffs)) {
				if (debug) Gpr.debug("Compare effect\texp:" + expEff + "\treal:" + realEff);
				if (expEff.equals(realEff)) return true;
			}

		return false;
	}

	boolean compareOK(String expectedEffs, String realEffs) {
		return compareEff(expectedEffs, realEffs) && compareAa(expectedEffs, realEffs);
	}

	String findAa(String eff) {
		int aaidx = eff.indexOf('(');
		if (aaidx < 0) return "";
		return eff.substring(aaidx);
	}

	String[] findEffTypes(String eff) {
		int aaidx = eff.indexOf('(');
		if (aaidx < 0) return eff.split("\\+");
		return eff.substring(0, aaidx).split("\\+");
	}

	void initRand() {
		rand = new Random(randSeed);
	}

	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);

		if (snpEffectPredictor == null) {
			config.loadSnpEffectPredictor();
			genome = config.getGenome();
		} else {
			config.setSnpEffectPredictor(snpEffectPredictor);
			genome = snpEffectPredictor.getGenome();
		}

		config.setTreatAllAsProteinCoding(true); // For historical reasons we set this one to 'true'....
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Parse a variant file and return a list
	 */
	public List<Variant> parseSnpEffectFile(String variantFile) {
		ArrayList<Variant> variants = new ArrayList<Variant>();

		VariantTxtFileIterator variantFileIterator = new VariantTxtFileIterator(variantFile, genome);
		for (Variant sc : variantFileIterator)
			variants.add(sc);

		Collections.sort(variants);
		return variants;
	}

	/**
	 * Calculate snp effect for a list of snps
	 */
	public void snpEffect(List<Variant> variantList, String transcriptId, boolean useShort, boolean negate) {
		int num = 1;
		// Predict each variant
		for (Variant variant : variantList) {
			// Get results for each snp
			VariantEffects results = config.getSnpEffectPredictor().variantEffect(variant);

			String msg = "";
			msg += "Number : " + num + "\n";
			msg += "\tVariant     : " + variant + "\n";
			msg += "\tExpecting   : " + (negate ? "NOT " : "") + "'" + variant.getId() + "'\n";
			msg += "\tResultsList :\n";
			for (VariantEffect res : results)
				msg += "\t\t'" + res.toStringSimple(useShort) + "'\n";

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
		initSnpEffPredictor();
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, false); // Predict each snp
	}

	public void snpEffectNegate(String snpEffFile, String transcriptId, boolean useShort) {
		initSnpEffPredictor();
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, true); // Predict each snp
	}

}
