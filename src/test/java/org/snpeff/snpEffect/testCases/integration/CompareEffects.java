package org.snpeff.snpEffect.testCases.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.snpeff.fileIterator.VariantTxtFileIterator;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Log;

/**
 * Compare effects in tests cases
 *
 * @author pcingola
 */
public class CompareEffects {

	boolean debug = false;
	boolean verbose = false || debug;
	boolean createOutputFile = false;
	boolean useAaNoNum = false;
	boolean shiftHgvs = false;
	Config config;
	Genome genome;
	String genomeName;
	SnpEffectPredictor snpEffectPredictor;

	public CompareEffects(SnpEffectPredictor snpEffectPredictor, boolean verbose) {
		genomeName = "test";
		this.verbose = verbose;
		this.snpEffectPredictor = snpEffectPredictor;
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	public CompareEffects(String genomeName, boolean verbose) {
		this.genomeName = genomeName;
		this.verbose = verbose;
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	/**
	 * Compare each result. If one matches, we consider it OK
	 */
	boolean anyResultMatches(String transcriptId, Variant variant, VariantEffects variantEffects, boolean useShort) {
		for (VariantEffect varEff : variantEffects) {
			String expectedEff = variant.getId();

			Transcript tr = varEff.getTranscript();
			if (tr != null) {
				if ((transcriptId == null) || (transcriptId.equals(tr.getId()))) {
					if (compareOK(varEff, expectedEff)) return true; // Matches one result in this transcript
				}
			} else if (compareOK(varEff, expectedEff)) return true; // Matches any result (out of a transcript)
		}
		return false;
	}

	boolean compareAa(VariantEffect varEff, String expEffs) {
		String expAa = findAa(expEffs);
		if (expAa.isEmpty()) return true; // We don't have AA information to compare

		String aa = useAaNoNum ? varEff.getAaChangeOld() : varEff.getAaChange();
		if (debug) Log.debug("AA compare: '" + aa + "'\tExpected AA: '" + expAa + "'");

		return aa.equals(expAa);
	}

	/**
	 * Compare effects
	 */
	boolean compareEff(VariantEffect varEff, String expEffs) {
		if (verbose) Log.debug("Variant effect: " + varEff);
		for (EffectType effType : varEff.getEffectTypes())
			for (String realEff : findEffTypes(expEffs)) {
				if (debug) Log.debug("Compare effect\texp:" + effType + "\treal:" + realEff);
				if (effType.toString().equals(realEff)) return true;
			}

		return false;
	}

	boolean compareOK(VariantEffect varEff, String expEffs) {
		String varEffStr = varEff.effect(false, true, false, false, false);
		if (varEffStr.equals(expEffs)) { return true; }

		return compareEff(varEff, expEffs) && compareAa(varEff, expEffs);
	}

	String findAa(String eff) {
		int aaStartIdx = eff.indexOf('(');
		int aaStopIdx = eff.indexOf(')');
		if (aaStartIdx < 0 || aaStopIdx < 0) return "";
		return eff.substring(aaStartIdx + 1, aaStopIdx);
	}

	String[] findEffTypes(String eff) {
		int aaidx = eff.indexOf('(');
		if (aaidx < 0) return eff.split("\\+");
		return eff.substring(0, aaidx).split("\\+");
	}

	public Config getConfig() {
		return config;
	}

	public void initSnpEffPredictor() {
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
		config.setHgvsShift(shiftHgvs);
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

	public void setUseAaNoNum(boolean useAaNoNum) {
		this.useAaNoNum = useAaNoNum;
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

			if (verbose) Log.info(msg);

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
								+ "\t" + res.effect(true, true, true, false, false) //
						);
					}
				} else {
					Log.debug(msg);
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
