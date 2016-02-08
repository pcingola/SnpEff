package org.snpeff.align;

/**
 * Smith-Waterman (local sequence alignment) algorithm for sequence alignment (short strings, since it's not memory optimized)
 * 
 * @author pcingola
 */
public class SmithWaterman extends NeedlemanWunsch {

	public SmithWaterman(String a, String b) {
		super(a, b);
		useSpace = false;
	}

	/**
	 * Calculate alignment tracing back from score matrix
	 */
	@Override
	void calcAlignment() {
		// Initialize arrays
		int maxLen = Math.max(a.length, b.length);
		alignmentA = new char[maxLen];
		alignmentB = new char[maxLen];
		for (int i = 0; i < maxLen; i++)
			alignmentA[i] = alignmentB[i] = ' ';

		// Find max score position
		int i = a.length;
		int j = b.length;
		int maxScore = 0;
		for (int ii = 0; ii <= a.length; ii++) {
			for (int jj = 0; jj <= b.length; jj++) {
				if (getScore(ii, jj) > maxScore) {
					maxScore = getScore(ii, jj);
					i = ii;
					j = jj;
				}
			}
		}
		int h = Math.max(i, j) - 1;

		// Traceback
		while ((i > 0) && (j > 0) && (h >= 0)) {
			int s = getScore(i, j);
			int scorediag = getScore(i - 1, j - 1);
			int scoreup = getScore(i, j - 1);
			int scoreleft = getScore(i - 1, j);

			if (s <= 0) {
				break;
			} else if (s == scoreup + deletion) {
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
				break; // Finished trace-back
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

	/**
	 * Calculate score matrix
	 */
	@Override
	void scoreMatrix() {
		score = new int[a.length + 1][b.length + 1];

		// Initialize
		for (int i = 0; i <= a.length; i++)
			setScore(i, 0, 0);

		for (int j = 0; j <= b.length; j++)
			setScore(0, j, 0);

		// Calculate
		bestScore = Integer.MIN_VALUE;
		for (int i = 1; i <= a.length; i++)
			for (int j = 1; j <= b.length; j++) {
				int match = getScore(i - 1, j - 1) + simmilarity(i, j);
				int del = getScore(i - 1, j) + deletion;
				int ins = getScore(i, j - 1) + deletion;

				int s = Math.max(0, Math.max(match, Math.max(del, ins)));

				setScore(i, j, s);
				bestScore = Math.max(bestScore, s);
			}
	}

}
