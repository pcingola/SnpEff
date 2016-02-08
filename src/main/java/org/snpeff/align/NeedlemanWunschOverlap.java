package org.snpeff.align;

/**
 * Needleman-Wunsch algorithm for string alignment (short strings, since it's not memory optimized)
 * 
 * @author pcingola
 */
public class NeedlemanWunschOverlap {

	String alignment;
	char a[], b[];
	int score[][];
	int besti = -1, bestj = -1;
	int alignmentScore = -1;

	int scoreMatch = 1; // Match score
	int scoreMissmatch = -5; // Missmatch score
	int scoreGap = -2; // Deletion score

	public NeedlemanWunschOverlap(String a, String b) {
		score = new int[a.length() + 1][b.length() + 1];
		this.a = a.toCharArray();
		this.b = b.toCharArray();
	}

	public String align() {
		calcAlignmentScore();
		return calcAlignment();
	}

	/**
	 * Find best alignment score
	 * @return
	 */
	int bestScore() {
		alignmentScore = Integer.MIN_VALUE;
		int maxj = b.length, maxi = a.length;
		for (int i = 1; i <= a.length; i++) {
			if (alignmentScore < score[i][maxj]) {
				alignmentScore = score[i][maxj];
				besti = i;
				bestj = maxj;
			}
		}

		for (int j = 1; j <= b.length; j++) {
			if (alignmentScore < score[maxi][j]) {
				alignmentScore = score[maxi][j];
				bestj = j;
				besti = maxi;
			}
		}

		return alignmentScore;
	}

	public String calcAlignment() {
		int maxLen = Math.max(a.length, b.length);
		char alignmentA[] = new char[maxLen];
		char match[] = new char[maxLen];
		char alignmentB[] = new char[maxLen];

		int h = maxLen - 1;
		int i = besti;
		int j = bestj;

		while ((i > 0) && (j > 0) && (h >= 0)) {
			int s = score[i][j];
			int scorediag = score[i - 1][j - 1];
			int scoreup = score[i][j - 1];
			int scoreleft = score[i - 1][j];

			if (s == scoreup + scoreGap) {
				alignmentA[h] = '-';
				alignmentB[h] = b[j - 1];
				match[h] = '-';
				j--;
			} else if (s == scoreleft + scoreGap) {
				alignmentA[h] = a[i - 1];
				alignmentB[h] = '-';
				match[h] = '-';
				i--;
			} else if (s == scorediag + simmilarity(i, j)) {
				alignmentA[h] = a[i - 1];
				alignmentB[h] = b[j - 1];
				match[h] = a[i - 1] == b[j - 1] ? '|' : '*';
				i--;
				j--;
			} else throw new RuntimeException("This should never happen!");

			h--;
		}

		while ((i > 0) && (h >= 0)) {
			alignmentA[h] = a[i - 1];
			alignmentB[h] = '-';
			match[h] = '-';
			i--;
			h--;
		}

		while ((j > 0) && (h >= 0)) {
			alignmentA[h] = '-';
			alignmentB[h] = b[j - 1];
			match[h] = '-';
			j--;
			h--;
		}

		for (; h >= 0; h--)
			match[h] = alignmentA[h] = alignmentB[h] = ' ';

		StringBuilder sb = new StringBuilder();
		sb.append((new String(alignmentA)).trim());
		sb.append("\n");
		sb.append((new String(match)).trim());
		sb.append("\n");
		sb.append((new String(alignmentB)).trim());
		sb.append("\n");
		alignment = sb.toString();
		return alignment;
	}

	/**
	 * Calculate score matrix
	 */
	public int calcAlignmentScore() {
		// Initialize
		for (int i = 0; i <= a.length; i++)
			score[i][0] = 0;

		for (int j = 0; j <= b.length; j++)
			score[0][j] = 0;

		// Calculate
		for (int i = 1; i <= a.length; i++)
			for (int j = 1; j <= b.length; j++) {
				int match = score[i - 1][j - 1] + simmilarity(i, j);
				int del = score[i - 1][j] + scoreGap;
				int ins = score[i][j - 1] + scoreGap;
				int s = Math.max(match, Math.max(del, ins));
				score[i][j] = s;
			}
		return bestScore();
	}

	public int getAligmentScore() {
		return alignmentScore;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setDeletion(int deletion) {
		scoreGap = deletion;
	}

	public void setMatch(int match) {
		scoreMatch = match;
	}

	public void setMissMatch(int missMatch) {
		scoreMissmatch = missMatch;
	}

	void setScore(int i, int j, int val) {
		score[i][j] = val;
	}

	/**
	 * Similarity 'matrix' for bases
	 * @param a
	 * @param binningIndex
	 * @return
	 */
	int simmilarity(int i, int j) {
		if (a[i - 1] != b[j - 1]) return scoreMissmatch;
		return scoreMatch;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Title row
		sb.append(String.format("%4s|", ""));
		for (int j = 0; j <= b.length; j++)
			sb.append(String.format("%4s|", (j > 0 ? b[j - 1] : "")));
		sb.append("\n");

		for (int i = 0; i <= a.length; i++) {
			sb.append(String.format("%4s|", (i > 0 ? a[i - 1] : ""))); // Column title
			for (int j = 0; j <= b.length; j++)
				sb.append(String.format("%4d|", score[i][j]));
			sb.append("\n");
		}
		return sb.toString();
	}

}
