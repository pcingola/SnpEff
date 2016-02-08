package org.snpeff.probablility;

import java.util.Random;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;

/**
 * Create random markers using a uniform distribution
 * 
 * @author pcingola
 */
public class RandMarker {

	Random rand;
	Genome genome;
	long genomeSize;

	public RandMarker(Genome genome) {
		rand = new Random();
		this.genome = genome;
		init();
	}

	public RandMarker(Genome genome, int seed) {
		rand = new Random(seed);
		this.genome = genome;
		init();
	}

	/**
	 * Initialize
	 */
	void init() {
		genomeSize = 0;
		for (Chromosome chr : genome)
			genomeSize += chr.size();
	}

	/**
	 * Create a random marker of size readLen
	 * @param readLen
	 * 
	 * WARNIIG: It will loop forever if there is no chromosome longer than readLen
	 * 
	 * @return A marker at a random chromosome & position.
	 */
	public Marker rand(int readLen) {
		while (true) {
			Marker m = random(readLen);
			if (m != null) return m;
		}
	}

	/**
	 * Create a random marker of size readLen
	 * @param readLen
	 * @return A marker at a random chromosome & position. Can be null sometimes
	 */
	Marker random(int readLen) {
		// Random number
		long chrPos = Math.abs(rand.nextLong()) % genomeSize;

		//---
		// Select a chromosome
		//---
		long pos = chrPos;
		Chromosome chromo = null;
		for (Chromosome chr : genome) {
			if (pos <= chr.size()) {
				chromo = chr;
				break;
			}
			pos -= chr.size();
		}
		if (chromo == null) throw new RuntimeException("Selected null chromosome. This should never happen!");
		if (pos < 0) throw new RuntimeException("Negative position. This should never happen!");
		if ((pos + readLen) > chromo.size()) return null; // Read pass chromosome length (can happen)

		return new Marker(chromo, (int) pos, (int) (pos + readLen - 1), false, "");
	}
}
