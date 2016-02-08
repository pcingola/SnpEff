package org.snpeff.motif;

import org.snpeff.util.Gpr;

/**
 * Create a DNA motif count matrix
 *
 * Refrence http://en.wikipedia.org/wiki/Position-specific_scoring_matrix
 *
 * @author pcingola
 */
public class Pwm {

	public static final int SCALE = 100;
	static final double LOG2 = Math.log(2);

	public static final char BASES[] = { 'A', 'C', 'G', 'T' };
	int countMatrix[][]; // Keep counts for each base and position: countMatrix[base][position]
	int count[]; // Keep counts for each base
	double logOdds[][];
	int length;
	int totalCount;
	String name, id;
	char bestSequence[];

	public Pwm(int length) {
		this.length = length;
		countMatrix = new int[BASES.length][length];
		count = new int[BASES.length];
		logOdds = null;
	}

	public Pwm(String file) {
		String data = Gpr.readFile(file);
		String lines[] = data.split("\n");

		length = lines.length;
		countMatrix = new int[BASES.length][length];
		count = new int[BASES.length];
		logOdds = new double[BASES.length][length];

		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			String val[] = lines[lineNum].trim().split("\\s+");
			for (int baseNum = 0; baseNum < BASES.length; baseNum++)
				logOdds[baseNum][lineNum] = Gpr.parseDoubleSafe(val[baseNum]);
		}
	}

	/**
	 * Transform a base into a code
	 */
	int base2int(char base) {
		switch (base) {
		case 'a':
		case 'A':
			return 0;
		case 'c':
		case 'C':
			return 1;
		case 'g':
		case 'G':
			return 2;
		case 't':
		case 'T':
		case 'u':
		case 'U':
			return 3;
		}

		return -1;
	}

	/**
	 * Calculate log odds matrix from counts
	 * Reference: http://en.wikipedia.org/wiki/Position-specific_scoring_matrix
	 */
	public void calcLogOddsWeight() {
		logOdds = new double[BASES.length][length];
		double b[] = new double[BASES.length];

		// Update counts
		int total = 0;
		for (int baseNum = 0; baseNum < BASES.length; baseNum++) {
			count[baseNum] = 0;
			for (int i = 0; i < length; i++) {
				count[baseNum] += countMatrix[baseNum][i];
				total += countMatrix[baseNum][i];
			}
		}

		// Calculate b[i]
		for (int baseNum = 0; baseNum < BASES.length; baseNum++)
			b[baseNum] = ((double) (count[baseNum] + 1)) / (total);

		for (int i = 0; i < length; i++) {
			for (int baseNum = 0; baseNum < BASES.length; baseNum++) {
				double p = ((double) (countMatrix[baseNum][i] + 1)) / ((double) total);
				logOdds[baseNum][i] = -p * Math.log(p / b[baseNum]) / LOG2;
			}
		}
	}

	/**
	 * Get best matching sequence (highest score)
	 * @return
	 */
	public char[] getBestSequence() {
		if (bestSequence == null) {
			bestSequence = new char[length];

			for (int i = 0; i < countMatrix[0].length; i++) {
				int max = 0, maxb = 0;
				for (int b = 0; b < BASES.length; b++) {
					if (max < countMatrix[b][i]) {
						max = countMatrix[b][i];
						maxb = b;
					}
				}
				bestSequence[i] = BASES[maxb];
			}
		}

		return bestSequence;
	}

	public String getBestSequenceStr() {
		return new String(getBestSequence());
	}

	/**
	 * Get counts for a given position
	 */
	public int getCount(char base, int position) {
		return countMatrix[base2int(base)][position];
	}

	public String getId() {
		return id;
	}

	public double getLogOdds(char base, int position) {
		int baseIdx = base2int(base);
		if (baseIdx < 0 || position >= size()) return 0; // Unknown base
		return logOdds[baseIdx][position];
	}

	public String getName() {
		return name;
	}

	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Is position 'pos' conserved (only one base has non-zero counts)
	 */
	public boolean isConserved(int pos) {
		int countNonZero = 0;
		for (int i = 0; i < countMatrix.length; i++)
			if (countMatrix[i][pos] > 0) countNonZero++;

		return countNonZero == 1;
	}

	public int length() {
		return length;
	}

	/**
	 * Calculate PWM score for a string
	 */
	public double score(String dna) {
		if (logOdds == null) calcLogOddsWeight();

		char bases[] = dna.toCharArray();
		double score = 0;
		for (int i = 0; i < bases.length; i++)
			score += getLogOdds(bases[i], i);

		return score / length;
	}

	/**
	 * Set PWM as a perfect match to a dna sequence
	 */
	public void set(String dna) {
		char bases[] = dna.toCharArray();
		for (int i = 0; i < bases.length; i++) {
			// Fake count
			for (int j = 0; j < BASES.length; j++)
				countMatrix[j][i] = 1;

			countMatrix[base2int(bases[i])][i] = SCALE;
		}
	}

	/**
	 * Set counts for one base
	 */
	public void setCounts(char base, int counts[]) {
		int rowIdx = base2int(base);
		for (int i = 0; i < counts.length; i++)
			countMatrix[rowIdx][i] = counts[i];
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Matrix size
	 */
	int size() {
		return countMatrix[0].length;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Name: " + name + "\tId: " + id + "\n");

		if (countMatrix != null) {
			sb.append("Counts:\n");
			for (int b = 0; b < BASES.length; b++) {
				sb.append(BASES[b] + "\t");
				for (int i = 0; i < countMatrix[b].length; i++)
					sb.append(String.format("%10d  ", countMatrix[b][i]));
				sb.append("\n");
			}

			sb.append("Max:\t");
			char best[] = getBestSequence();
			for (int i = 0; i < countMatrix[0].length; i++)
				sb.append(String.format("%10s  ", best[i]));
			sb.append("\n");
		}

		if (logOdds != null) {
			sb.append("\nWeights:\n");
			for (int b = 0; b < BASES.length; b++) {
				sb.append(BASES[b] + "\t");
				for (int i = 0; i < logOdds[b].length; i++)
					sb.append(String.format("%10.2f  ", logOdds[b][i]));
				sb.append("\n");
			}

			sb.append("Max:\t");
			for (int i = 0; i < countMatrix[0].length; i++) {
				int maxb = 0;
				double max = Double.NEGATIVE_INFINITY;
				for (int b = 0; b < BASES.length; b++) {
					if (max < logOdds[b][i]) {
						max = logOdds[b][i];
						maxb = b;
					}
				}
				sb.append(String.format("%10s  ", BASES[maxb]));
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	public void updateCounts(String dna) {
		updateCounts(dna, 1);
	}

	/**
	 * Update counts matrix.
	 */
	public void updateCounts(String dna, int inc) {
		totalCount += inc;
		char bases[] = dna.toCharArray();

		for (int i = 0; i < bases.length; i++) {
			int code = base2int(bases[i]);
			if (code >= 0) countMatrix[code][i] += inc;
		}
	}
}
