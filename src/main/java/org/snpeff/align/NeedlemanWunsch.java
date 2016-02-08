package org.snpeff.align;

/**
 * Needleman-Wunsch (global sequence alignment) algorithm for sequence  alignment (short strings, since it's not memory optimized)
 *
 * @author pcingola
 */
public class NeedlemanWunsch {

	String alignment;
	char a[], b[];
	char alignmentA[], alignmentB[];
	int score[][];
	int match = 1; // Match score
	int missMatch = -1; // Mismatch score
	int deletion = -2; // Deletion score
	int offset = 0;
	int bestScore = 0;
	boolean useSpace; // Use spaces when calculating alignment

	public NeedlemanWunsch(String a, String b) {
		this.a = a.toCharArray();
		this.b = b.toCharArray();
		useSpace = true;
	}

	public String align() {
		try {
			scoreMatrix();
			calcAlignment();
		} catch (Throwable t) {
			throw new RuntimeException("Error aligning sequences:\n\tSequence 1: " + new String(a) + "\n\tSequence 2: " + new String(b), t);
		}
		return alignment;
	}

	/**
	 * Calculate alignment tracing back from score matrix
	 */
	void calcAlignment() {
		int maxLen = Math.max(a.length, b.length);
		alignmentA = new char[maxLen];
		alignmentB = new char[maxLen];

		for (int i = 0; i < maxLen; i++)
			alignmentA[i] = alignmentB[i] = ' ';

		int i = a.length;
		int j = b.length;
		int h = maxLen - 1;

		while ((i > 0) && (j > 0) && (h >= 0)) {
			int s = getScore(i, j);
			int scorediag = getScore(i - 1, j - 1);
			int scoreup = getScore(i, j - 1);
			int scoreleft = getScore(i - 1, j);

			if (s == scoreup + deletion) {
				alignmentA[h] = '-';
				alignmentB[h] = b[j - 1];
				j--;
			} else if (s == scoreleft + deletion) {
				alignmentA[h] = a[i - 1];
				alignmentB[h] = '-';
				i--;
			} else if (s == scorediag + simmilarity(i, j)) {
				if (useSpace) {
					alignmentA[h] = ' ';
					alignmentB[h] = ' ';
				} else {
					alignmentA[h] = a[i - 1];
					alignmentB[h] = b[j - 1];
				}
				i--;
				j--;
			} else {
				throw new RuntimeException("This should never happen!\n");
			}

			h--;
		}

		while ((i > 0) && (h >= 0)) {
			alignmentA[h] = a[i - 1];
			alignmentB[h] = '-';
			i--;
			h--;
		}

		while ((j > 0) && (h >= 0)) {
			alignmentA[h] = '-';
			alignmentB[h] = b[j - 1];
			j--;
			h--;
		}

		// Calculate offset from original position
		for (offset = 0; (offset < maxLen) && (alignmentA[offset] == ' '); offset++);

		// Create alignment string
		StringBuffer alsb = new StringBuffer();
		char prev = ' ';
		for (i = 0; i < maxLen; i++) {
			if (alignmentA[i] == '-') {
				if (prev != '-') alsb.append('-');
				alsb.append(alignmentB[i]);
				prev = '-';
			} else if (alignmentB[i] == '-') {
				if (prev != '+') alsb.append('+');
				alsb.append(alignmentA[i]);
				prev = '+';
			}
		}
		alignment = alsb.toString();

	}

	public String getAlignment() {
		return alignment;
	}

	public int getAlignmentScore() {
		return bestScore;
	}

	public int getOffset() {
		return offset;
	}

	int getScore(int i, int j) {
		return score[i][j];
	}

	/**
	 * Calculate score matrix
	 */
	void scoreMatrix() {
		score = new int[a.length + 1][b.length + 1];

		// Initialize
		for (int i = 0; i <= a.length; i++)
			setScore(i, 0, deletion * i);

		for (int j = 0; j <= b.length; j++)
			setScore(0, j, deletion * j);

		// Calculate
		bestScore = Integer.MIN_VALUE;
		for (int i = 1; i <= a.length; i++)
			for (int j = 1; j <= b.length; j++) {
				int match = getScore(i - 1, j - 1) + simmilarity(i, j);
				int del = getScore(i - 1, j) + deletion;
				int ins = getScore(i, j - 1) + deletion;
				int s = Math.max(match, Math.max(del, ins));
				setScore(i, j, s);

				bestScore = Math.max(bestScore, s);
			}
	}

	public void setDeletion(int deletion) {
		this.deletion = deletion;
	}

	public void setMatch(int match) {
		this.match = match;
	}

	public void setMissMatch(int missMatch) {
		this.missMatch = missMatch;
	}

	void setScore(int i, int j, int val) {
		score[i][j] = val;
	}

	public void setUseSpace(boolean useSpace) {
		this.useSpace = useSpace;
	}

	/**
	 * Similarity 'matrix' for bases
	 * @param a
	 * @param binningIndex
	 * @return
	 */
	int simmilarity(int i, int j) {
		if (a[i - 1] != b[j - 1]) return missMatch;
		return match;
	}

	@Override
	public String toString() {
		// Alignment not performed
		if (score == null) return "";

		char matching[] = new char[alignmentA.length];

		for (int i = 0; i < alignmentA.length; i++) {
			if ((alignmentA[i] == ' ') || (alignmentB[i] == ' ') || (alignmentA[i] == '-') || (alignmentB[i] == '-')) matching[i] = ' ';
			else if (alignmentA[i] == alignmentB[i]) matching[i] = '|';
			else matching[i] = '*';
		}

		return "\t" + new String(alignmentA) + "\n\t" + new String(matching) + "\n\t" + new String(alignmentB);
	}
}
