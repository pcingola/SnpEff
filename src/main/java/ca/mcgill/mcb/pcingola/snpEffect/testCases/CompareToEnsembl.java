package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.Timer;

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
		//---
		// Parse command line arguments
		//---
		if ((args.length != 2) && (args.length != 3)) {
			System.err.println("Usage: " + CompareToEnsembl.class.getSimpleName() + " genomeName ensemblFile [transcriptId]");
			System.exit(1);
		}

		String genomeName = args[0];
		String ensemblFile = args[1];
		String trName = null;
		if (args.length > 2) trName = args[2];

		//---
		// Run
		//---
		CompareToEnsembl compareToEnsembl = new CompareToEnsembl(genomeName, false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public CompareToEnsembl(String genomeName, boolean throwException) {
		this.throwException = throwException;
		if (verbose) Timer.showStdErr("Loading predictor");
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

		if (change.getCodonsOld().isEmpty() && change.getCodonsNew().isEmpty()) str += " -";
		else str += " " + change.getCodonsOld() + "/" + change.getCodonsNew();

		if (change.getAaOld().isEmpty() && change.getAaNew().isEmpty()) str += " -";
		else if (change.getAaOld().equals(change.getAaNew())) str += " " + change.getAaNew();
		else str += " " + change.getAaOld() + "/" + change.getAaNew();

		return str;
	}

	/**
	 * Compare our results to some ENSEMBL annotations
	 */
	public void compareEnsembl(String ensemblFile, String trName) {
		HashMap<Variant, String> seqChanges = readEnsemblFile(ensemblFile);
		ArrayList<Variant> list = new ArrayList<Variant>();
		list.addAll(seqChanges.keySet());
		Collections.sort(list);

		for (Variant seqChange : list) {
			VariantEffects changes = snpEffectPredictor.variantEffect(seqChange);

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

					if (id.equals(seqChange.getId())) {
						changesSb.append(id + "\t");
						ok = true;
					}
				}
			}

			// Was the change found?
			if (verbose) if (ok && verbose) System.out.println("OK   :\t" + seqChange + "\t'" + changesSb + "'\n\tEnsembl :\t" + seqChanges.get(seqChange) + "\n" + changesAllSb);
			else {
				String line = "DIFF :\t" + seqChange + "\t'" + changesSb + "'\n\tEnsembl :\t" + seqChanges.get(seqChange) + "\n" + changesAllSb;
				if (verbose) System.out.println(line);
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
		}
		return eff.toString();
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

		HashMap<Variant, String> seqChanges = new HashMap<Variant, String>();

		for (String line : lines) {
			Variant seqChange = str2seqChange(line);
			seqChanges.put(seqChange, line);
		}

		return seqChanges;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Create a SeqChange from an ENSEMBL line
	 */
	Variant str2seqChange(String line) {
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
			Variant seqChange = new Variant(chromo, pos, ref, alt, id);
			return seqChange;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing line:\n" + line, e);
		}
	}
}
