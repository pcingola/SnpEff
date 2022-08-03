package org.snpeff.align;

public class StringDiff {

    String s1, s2; // Strings to compare
    String name1, name2; // String names. Only used to represent in 'toString'.
    int count = -1; // Number of mismatches
    double error = -1.0; // Error rate: number_of_missmatches / total_length

    public StringDiff(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public StringDiff(String s1, String name1, String s2, String name2) {
        this.s1 = s1;
        this.s2 = s2;
        this.name1 = name1;
        this.name2 = name2;
    }

    /**
     * Count number of differences between strings
     */
    public int count() {
        if (count >= 0) return count;
        int minLen = minLen();
        count = 0;
        for (int j = 0; j < minLen; j++)
            if (s1.charAt(j) != s2.charAt(j)) count++;
        return count;
    }

    /**
     * Create a string that shows the difference between two strings
     */
    public String diffString() {
        // Create a string indicating differences
        int minLen = minLen();
        char diff[] = new char[minLen];
        for (int j = 0; j < minLen; j++) {
            if (s1.charAt(j) != s2.charAt(j)) {
                diff[j] = '|';
            } else diff[j] = ' ';

        }
        return new String(diff);
    }

    /**
     * Error ratio (number of mismatches divided by string length
     */
    public double error() {
        var minLen = minLen();
        return minLen > 0 ? count() / ((double) minLen) : 0.0;
    }

    /**
     * Minimum string length
     */
    int minLen() {
        return Math.min(s1.length(), s2.length());
    }

    public String toString() {
        var nameDiff = "diff";

        if (name1 != null && name2 != null) {
            // Use string "names". Calculate max name length
            var maxTitleLen = Math.max(Math.max(name1.length(), name2.length()), nameDiff.length());
            var format = "%" + maxTitleLen + "s : %s";
            format = format + "\n" + format + "\n" + format + "\n";
            return String.format(format, name1, s1, nameDiff, diffString(), name2, s2);
        }

        // Without titles
        return String.format("%s\n%s\n%s\n", s1, diffString(), s2);
    }


}
