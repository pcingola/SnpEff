package org.snpeff.snpEffect.testCases.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Compare our results to ENSEML's Variant Effect predictor's output
 *
 * @author pcingola
 */
public class CompareToEnsembl {

	boolean throwException = false;
	boolean verbose = false;
	Random rand;
	Config config;
	Genome genome;
	SnpEffectPredictor snpEffectPredictor;

	/**
	 * Main
	 */
	public static void main(String args[]) {
		// Parse command line arguments
		if ((args.length != 2) && (args.length != 3)) {
			System.err.println("Usage: " + CompareToEnsembl.class.getSimpleName() + " genomeName ensemblFile [transcriptId]");
			System.exit(1);
		}

		String genomeName = args[0];
		String ensemblFile = args[1];
		String trName = null;
		if (args.length > 2) trName = args[2];

		// Run
		CompareToEnsembl compareToEnsembl = new CompareToEnsembl(genomeName, false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public CompareToEnsembl(String genomeName, boolean throwException) {
		this.throwException = throwException;
		if (verbose) Log.info("Loading predictor");
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		snpEffectPredictor = config.getSnpEffectPredictor();
		genome = config.getGenome();
		snpEffectPredictor.buildForest();
	}

	/**
	 * Transform 'change' into an ENSEMBL-like string
	 */
	String change2str(VariantEffect change) {
		String str = effTranslate(change.getEffectType());

		if (change.getCodonsRef().isEmpty() && change.getCodonsAlt().isEmpty()) str += " -";
		else str += " " + change.getCodonsRef() + "/" + change.getCodonsAlt();

		if (change.getAaRef().isEmpty() && change.getAaAlt().isEmpty()) str += " -";
		else if (change.getAaRef().equals(change.getAaAlt())) str += " " + change.getAaAlt();
		else str += " " + change.getAaRef() + "/" + change.getAaAlt();

		return str;
	}

	/**
	 * Compare our results to some ENSEMBL annotations
	 */
	public void compareEnsembl(String ensemblFile, String trName) {
		HashMap<Variant, String> variants = readEnsemblFile(ensemblFile);
		ArrayList<Variant> list = new ArrayList<>();
		list.addAll(variants.keySet());
		Collections.sort(list);

		for (Variant variant : list) {
			VariantEffects changes = snpEffectPredictor.variantEffect(variant);

			boolean ok = false;
			StringBuffer changesSb = new StringBuffer();
			StringBuffer changesAllSb = new StringBuffer();

			// Compare to all changes found by SnpEff
			for (VariantEffect change : changes) {
				Marker m = change.getMarker();

				// Find transcript
				Transcript tr = null;
				while ((m != null) && (tr == null)) {
					if (m instanceof Transcript) tr = (Transcript) m;
					m = m.getParent();
				}

				// Compare changes?
				if ((tr != null) && ((trName == null) || tr.getId().equals(trName))) {
					String id = change2str(change);
					changesAllSb.append("\tSnpEff  :\t" + change + "\n");

					if (id.equals(variant.getId())) {
						changesSb.append(id + "\t");
						ok = true;
					}
				}
			}

			// Was the change found?
			if (verbose) if (ok && verbose) System.out.println("OK   :\t" + variant + "\t'" + changesSb + "'\n\tEnsembl :\t" + variants.get(variant) + "\n" + changesAllSb);
			else {
				String line = "DIFF :\t" + variant + "\t'" + changesSb + "'\n\tEnsembl :\t" + variants.get(variant) + "\n" + changesAllSb;
				if (verbose) Log.info(line);
				if (throwException) throw new RuntimeException(line);
			}
		}
	}

	/**
	 * Translate an effect to make it compatible to ENSEMBL's outputS
	 */
	String effTranslate(EffectType eff) {
		switch (eff) {
			case UTR_5_PRIME:
			case START_GAINED:
				return "5PRIME_UTR";

			case UTR_3_PRIME:
				return "3PRIME_UTR";

			case NON_SYNONYMOUS_START:
			case START_LOST:
				return "NON_SYNONYMOUS_CODING";

			case INTRON:
				return "INTRONIC";
			
			default:
				return eff.toString();
		}
	}

	/**
	 * Find a transcript
	 */
	Transcript findTranscriptByName(String trName) {
		for (Gene gene : genome.getGenes()) {
			for (Transcript tr : gene)
				if (tr.getId().equals(trName)) return tr;
		}
		return null;
	}

	/**
	 * Read a file and create a list of SeqChanges
	 */
	HashMap<Variant, String> readEnsemblFile(String fileName) {
		String lines[] = Gpr.readFile(fileName).split("\n");

		if (lines.length <= 0) throw new RuntimeException("Cannot open file '" + fileName + "' (or it's empty).");

		HashMap<Variant, String> variants = new HashMap<>();

		for (String line : lines) {
			Variant variant = str2variant(line);
			variants.put(variant, line);
		}

		return variants;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Create a SeqChange from an ENSEMBL line
	 */
	Variant str2variant(String line) {
		try {
			String recs[] = line.split("\t");

			// Parse chomo, position, ref and alt from recs[1]
			String chrPos[] = recs[0].split("_");

			Chromosome chromo = genome.getChromosome(chrPos[0]);
			int pos = Gpr.parseIntSafe(chrPos[1]) - 1;

			// Parse 'ALT'
			String alt = chrPos[2];
			if (chrPos[2].indexOf('/') > 0) {
				String ra[] = chrPos[2].split("/");
				alt = ra[1];
			}

			// We don't care about the reference (as long as it's different that 'ALT'
			String ref = "A";
			for (char base : GprSeq.BASES) {
				ref = "" + base;
				if (!ref.equals(alt)) break;
			}

			// ID
			String eff = recs[6];
			if (eff.indexOf(',') > 0) eff = eff.split(",")[0];
			String id = eff + " " + recs[11] + " " + recs[10];

			// Create SeqChange
			Variant variant = new Variant(chromo, pos, ref, alt, id);
			return variant;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing line:\n" + line, e);
		}
	}
}
