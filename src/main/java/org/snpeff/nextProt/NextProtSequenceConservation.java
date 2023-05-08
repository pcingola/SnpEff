package org.snpeff.nextProt;

import java.util.ArrayList;
import java.util.Collections;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.snpEffect.Config;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Log;

/**
 * A simple analysis of sequence conservation for each entry type
 *
 * Why? Many NextProt annotations are only a few amino acids long (or only 1 AA) and often
 * they only involve very specific sequences
 *
 * If the sequence is highly conserved and a non-synonymous mutation occurs, then
 * this might be disruptive (i.e. HIGH IMPACT)
 *
 * @author Pablo Cingolani
 *
 */
public class NextProtSequenceConservation {

	public static final double HIGHLY_CONSERVED_AA_PERCENT = 0.99;
	public static final int HIGHLY_CONSERVED_AA_COUNT = 5;

	boolean debug;
	boolean verbose;
	AutoHashMap<String, CountByType> countAaSequenceByType;

	public NextProtSequenceConservation() {
		countAaSequenceByType = new AutoHashMap<>(new CountByType());
		verbose = Config.get().isVerbose();
		debug = Config.get().isDebug();
	}

	public void add(String category, String aaSequence) {
		CountByType cbt = countAaSequenceByType.getOrCreate(category);
		cbt.inc(aaSequence);
	}

	/**
	 * Sequence conservation analysis
	 */
	void analyzeSequenceConservation(Markers markers) {
		if (verbose) Log.info("Sequence conservation analysis."//
				+ "\n\tAA sequence length  : " + 1 //
				+ "\n\tMin AA count        : " + HIGHLY_CONSERVED_AA_COUNT //
				+ "\n\tMin AA conservation : " + HIGHLY_CONSERVED_AA_PERCENT //
		);

		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(countAaSequenceByType.keySet());
		Collections.sort(keys);

		// Show AA counts for each 'key'
		for (String key : keys) {
			analyzeSequenceConservation(key, markers);
		}
	}

	/**
	 * Sequence conservation analysis for 'category'
	 */
	void analyzeSequenceConservation(String category, Markers markers) {
		// Mark highly conserved
		if (isHighlyConserved(category)) {
			int count = 0;
			// Mark as highly conserved
			for (Marker m : markers) {
				NextProt nextProt = (NextProt) m;
				if (nextProt.getName().equals(category)) {
					nextProt.setHighlyConservedAaSequence(true);
					count++;
				}
			}

			if (verbose) Log.info("NextProt " + count + " markers type '" + category + "' marked as highly conserved AA sequence");
		}
	}

	/**
	 * Calculate average sequence length
	 * Note: CountByType has sequences as keys
	 */
	int averageAaSeqLength(CountByType cbt) {
		int totlen = 0;
		for (String aas : cbt.keySet())
			totlen += aas.length();
		return cbt.keySet().size() > 0 ? totlen / cbt.keySet().size() : 0;
	}

	/**
	 * Is 'category' highly conserved?
	 */
	boolean isHighlyConserved(String category) {
		CountByType cbt = countAaSequenceByType.get(category);
		long total = cbt.sum();

		var avgLen = averageAaSeqLength(cbt);
		if(verbose) Log.info("Category '" + category + "', count: " + cbt.getTotalCount() + ", different sequences: " + cbt.keySet().size() + ", AA sequence length: " + avgLen);

		boolean highlyConservedAaSequence = false;
		int rank = 1;
		for (String aas : cbt.keysRanked(false)) {
			long count = cbt.get(aas);
			double perc = ((double) count) / total;
			if(verbose) Log.info("\t" + count + "\t" + 100 * perc + "%\t" + "\t" + aas);
			if ((perc > HIGHLY_CONSERVED_AA_PERCENT) && (total >= HIGHLY_CONSERVED_AA_COUNT)) highlyConservedAaSequence = true;
			rank++;

			// List too long
			if (rank > 50 && aas.length() > 2 && perc < 0.001) break;
		}
		return highlyConservedAaSequence;
	}

}
