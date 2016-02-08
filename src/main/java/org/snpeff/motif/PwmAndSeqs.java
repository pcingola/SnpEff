package org.snpeff.motif;

import org.snpeff.nmer.Nmer;
import org.snpeff.stats.Counter;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

/**
 * Create a DNA motif count matrix and also 
 * count the number of sequences in that contribute 
 * to this motif.
 * 
 * @author pcingola
 */
public class PwmAndSeqs extends Pwm {

	TLongIntHashMap countSeqs; // Count Nmer sequences
	TLongIntHashMap countRsIds; // Count rsId (from dbSnp)
	Nmer nmer;

	public PwmAndSeqs(int len) {
		super(len);
		countSeqs = new TLongIntHashMap();
		countRsIds = new TLongIntHashMap();
		nmer = new Nmer(len);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final Nmer nmer = new Nmer(length);
		sb.append(super.toString());
		sb.append("Sequence\tCount\n");

		final Counter counter = new Counter();

		// Show Nmer table
		countSeqs.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int count) {
				nmer.setNmer(key);
				sb.append(nmer.toString() + "\t" + count + "\n");
				counter.add(count);
				return true;
			}
		});
		sb.append("\tTotal\t" + counter.get() + "\n");

		// Show rsId table
		counter.reset();
		sb.append("\nRsId\tCount\n");
		countRsIds.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long rsNum, int count) {
				sb.append("rs" + rsNum + "\t" + count + "\n");
				counter.add(count);
				return true;
			}
		});
		sb.append("\tTotal\t" + counter.get() + "\n");

		return sb.toString();
	}

	/**
	 * Update counts matrix.
	 * @param dna
	 */
	public void updateCounts(String dna, int inc, long rsId) {
		updateCounts(dna, inc);
		nmer.set(dna);

		// Add to Nmer entry
		long nmerLong = nmer.getNmer(); // Nmer coded as long
		int count = countSeqs.get(nmerLong);
		countSeqs.put(nmerLong, count + inc); // Update count

		// Add to rsId entry
		count = countRsIds.get(rsId);
		countRsIds.put(rsId, count + inc); // Update count
	}
}
