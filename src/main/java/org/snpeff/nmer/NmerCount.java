package org.snpeff.nmer;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TLongIntProcedure;

import java.io.Serializable;
import java.util.Random;

import org.snpeff.spliceSites.AcgtTree;
import org.snpeff.stats.Counter;
import org.snpeff.stats.CounterDouble;

/**
 * Mark if an Nmer has been 'seen'
 * It only count up to 255 (one byte per counter)
 * 
 * @author pcingola
 */
public class NmerCount implements Serializable {

	private static final long serialVersionUID = 1L;
	public static boolean debug = false;

	int nmerSize;
	TLongIntHashMap hash;

	public NmerCount(int nmerSize) {
		this.nmerSize = nmerSize;
		hash = new TLongIntHashMap();
	}

	/**
	 * Average number of nmers
	 * @param threshold
	 * @return
	 */
	public double avg() {
		double total = total();
		double size = size();
		return size > 0 ? total / size : 0;
	}

	/**
	 * Count an instance of this Nmer
	 * @param nmer
	 */
	public void count(Nmer nmer) {
		long key = nmer.getNmer();
		int count = hash.get(key) + 1;
		hash.put(key, count);
	}

	/**
	 * Count how many nmers are below a given threshold
	 * @param threshold
	 * @return
	 */
	public long countLessThan(final int threshold) {
		final Counter counter = new Counter();
		hash.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int value) {
				if (value > threshold) counter.inc();
				return true;
			}
		});
		return counter.count;
	}

	protected int get(long key) {
		return hash.get(key);
	}

	/**
	 * Get kmer count
	 * @param nmer
	 * @return
	 */
	public int get(Nmer nmer) {
		long key = nmer.getNmer();
		return hash.get(key);
	}

	/**
	 * Max nmer count
	 * @param threshold
	 * @return
	 */
	public long max() {
		final Counter counter = new Counter();
		hash.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int value) {
				if (value > counter.get()) counter.set(value);
				return true;
			}
		});
		return counter.count;
	}

	/**
	 * Max nmer count
	 * @param threshold
	 * @return
	 */
	public long max(final NmerCount nullDistribution) {
		final CounterDouble max = new CounterDouble();
		final Counter counter = new Counter();

		hash.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int value) {
				int countNull = nullDistribution.get(key);
				double d = ((double) value) / countNull;
				if (d > max.get()) {
					max.set(d);
					counter.set(value);

				}
				return true;
			}
		});
		return counter.count;
	}

	/**
	 * Create random sequences and count nmers
	 * This is used to create a null distribution
	 * 
	 * @param iterations
	 */
	public void random(int iterations) {
		Nmer nmer = new Nmer(nmerSize);
		NmerCount nmerCount = new NmerCount(nmerSize);

		Random rand = new Random();
		for (int i = 0; i < iterations; i++) {
			char base = AcgtTree.BASES[rand.nextInt(AcgtTree.BASES.length)];
			nmer.rol(base);
			nmerCount.count(nmer);
		}
	}

	public int size() {
		return hash.size();
	}

	@Override
	public String toString() {
		return "Size: " + hash.size() + "\tTotal: " + total() + "\tAvg: " + avg() + "\tMax: " + max();
	}

	public String toStringAll() {
		return toStringAll(0);
	}

	public String toStringAll(final int minCount) {
		final StringBuilder sb = new StringBuilder();
		final Nmer nmer = new Nmer(nmerSize);
		hash.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int value) {
				nmer.setNmer(key);
				if (value >= minCount) sb.append(nmer + "\t" + value + "\n");
				return true;
			}
		});
		return sb.toString();
	}

	/**
	 * Total number of nmers
	 * @param threshold
	 * @return
	 */
	public long total() {
		final Counter sum = new Counter();
		hash.forEachEntry(new TLongIntProcedure() {

			@Override
			public boolean execute(long key, int value) {
				sum.set(sum.get() + value);
				return true;
			}
		});
		return sum.get();
	}
}
