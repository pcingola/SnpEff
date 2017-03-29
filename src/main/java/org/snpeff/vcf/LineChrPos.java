package org.snpeff.vcf;

import org.snpeff.util.Gpr;

/**
 * A simple chr:pos parser
 * Stores using bytes instead of chars
 */
public class LineChrPos implements Comparable<LineChrPos> {

	String chr;
	int chrNum;
	int pos;
	byte line[];

	public LineChrPos(String str) {
		line = str.getBytes();
		parse(str);
	}

	@Override
	public int compareTo(LineChrPos lp) {
		int cmp = 0;

		if ((chrNum > 0) && (lp.chrNum > 0)) {
			// Both have numbers? Compare numbers
			cmp = chrNum - lp.chrNum;
		} else if (chrNum > 0) {
			// Only this one has chr number? This one is first
			return -1;
		} else if (lp.chrNum > 0) {
			// The other entry has number? Then the other entry is first
			return 1;
		} else {
			// Neither has number? Compare strings
			cmp = chr.compareTo(lp.chr);
		}

		// Are we done?
		if (cmp != 0) return cmp;

		// Same chromosome? Compare positions
		return pos - lp.pos;
	}

	public String getLine() {
		return new String(line);
	}

	void parse(String str) {
		// Parse line using "chr\tpos\t...."
		String fields[] = str.split("\t", 3);
		if (fields.length < 2) throw new RuntimeException("Cannot parse line:\t" + str);
		chr = fields[0];
		chrNum = Gpr.parseIntSafe(fields[0]);
		pos = Gpr.parseIntSafe(fields[1]);
	}

	@Override
	public String toString() {
		return (chrNum > 0 ? chrNum + "[number]" : chr + "[string]") //
				+ ":" + pos //
				+ "\t" + new String(line) //
		;
	}

}
