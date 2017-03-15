package org.snpeff.util;

public class Diff {

	boolean ignoreCase;
	boolean ignoreSpaces;
	boolean showOnlyDifferentLines;
	String s1;
	String s2;

	public Diff(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
		ignoreCase = false;
		ignoreSpaces = true;
		showOnlyDifferentLines = true;
	}

	/**
	 * Multi-line diff
	 * @return A formatted string showing differences
	 */
	String diff() {
		String lines1[] = lines(s1);
		String lines2[] = lines(s2);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Math.max(lines1.length, lines2.length); i++) {
			String l1 = i < lines1.length ? lines1[i] : "";
			String l2 = i < lines2.length ? lines2[i] : "";
			int lineNum = i + 1;
			if (l1.equals(l2)) {
				if (!showOnlyDifferentLines) sb.append(lineNum + "\tOK   : " + l1 + "\n");
			} else {
				if (!showOnlyDifferentLines) {
					sb.append(lineNum + "\tDiff : " + l1 + "\n");
					sb.append(lineNum + "\t     : " + l2 + "\n");
					sb.append(lineNum + "\t     : " + diffLine(l1, l2) + "\n");
				} else {
					sb.append(lineNum + "\t: " + l1 + "\n");
					sb.append(lineNum + "\t: " + l2 + "\n");
					sb.append(lineNum + "\t: " + diffLine(l1, l2) + "\n\n");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Single line diff
	 * @param s1 : Single line string
	 * @param s2 : Single line string
	 * @return Difference line (using '^' to show the difference
	 */
	String diffLine(String s1, String s2) {
		char chars1[] = s1.toCharArray();
		char chars2[] = s2.toCharArray();
		int max = Math.max(chars1.length, chars2.length);
		char d[] = new char[max];
		for (int i = 0; i < max; i++) {
			char c1 = i < chars1.length ? chars1[i] : '\0';
			char c2 = i < chars2.length ? chars2[i] : '\0';
			d[i] = (c1 == c2 ? ' ' : '^');
		}
		return new String(d);
	}

	public boolean equals() {
		String lines1[] = lines(s1);
		String lines2[] = lines(s2);

		for (int i = 0; i < Math.max(lines1.length, lines2.length); i++) {
			String l1 = i < lines1.length ? lines1[i] : "";
			String l2 = i < lines2.length ? lines2[i] : "";
			if (!l1.equals(l2)) return false;
		}
		return true;
	}

	/**
	 * Split string into lines
	 */
	String[] lines(String str) {
		String l[] = str.split("\n");
		for (int i = 0; i < l.length; i++) {
			if (ignoreCase) l[i] = l[i].toLowerCase();
			if (ignoreSpaces) l[i] = Gpr.noSpaces(l[i]);
		}
		return l;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public void setIgnoreSpaces(boolean ignoreSpaces) {
		this.ignoreSpaces = ignoreSpaces;
	}

	public void setShowOnlyDifferentLines(boolean showOnlyDifferentLines) {
		this.showOnlyDifferentLines = showOnlyDifferentLines;
	}

	@Override
	public String toString() {
		return diff();
	}

}
