package org.snpeff.coverage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snpeff.fileIterator.SamFileIterator;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Marker;
import org.snpeff.sam.SamEntry;
import org.snpeff.sam.SamHeaderRecord;
import org.snpeff.sam.SamHeaderRecordSq;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Base by base coverage (one chromsome)
 *
 * @author pcingola
 */
public class CountFragments implements Serializable {

	private static int SHOW_EVERY = 10 * 1000;
	private static final long serialVersionUID = 1150158182247576450L;

	HashMap<String, CoverageChr> coverageByName;

	/**
	 * Calculate coverage from a SAM file
	 * @param samFile
	 * @param verbose
	 * @return
	 */
	public static CountFragments calculateFromSam(String samFile, boolean verbose) {
		int i = 1;
		boolean header = true;
		String chrPrev = "";
		SamFileIterator sfi = new SamFileIterator(samFile);
		CountFragments coverage = new CountFragments();
		Pattern patternCigar = Pattern.compile("(\\d+)([A-Z])");

		if (verbose) Log.info("Processing file '" + samFile + "'");

		for (SamEntry se : sfi) {
			if (header) {
				header = false;
				// Create coverage records
				for (SamHeaderRecord rec : sfi.getHeaders().getRecords("SQ")) {
					SamHeaderRecordSq sq = (SamHeaderRecordSq) rec;
					coverage.createChr(sq.getSequenceName(), sq.getLength());
				}
			}

			// Find coverage object
			String chrName = se.getRname();

			// Get start position
			int start = se.getPos() - 1; // One-based coordinates
			String cigar = se.getCigar();
			Matcher matcher = patternCigar.matcher(cigar);
			while (matcher.find()) {
				int len = Gpr.parseIntSafe(matcher.group(1));
				String op = matcher.group(2);
				if (op.equals("M")) coverage.inc(chrName, start, start + len - 1);
				if (op.equals("M") || op.equals("D") || op.equals("N") || op.equals("EQ") || op.equals("X") || op.equals("P")) start += len;
			}

			// Show mark
			if (verbose) {
				if (!chrName.equals(chrPrev)) {
					System.err.println("");
					Log.info(chrName + "\t");
				}
				chrPrev = chrName;
				Gpr.showMark(i++, SHOW_EVERY);
			}
		}

		return coverage;
	}

	public CountFragments() {
		coverageByName = new HashMap<String, CoverageChr>();
	}

	/**
	 * Calculate Fragments Per Kilobase of exon per Million fragments mapped (FPKM)
	 *
	 * @param m : A marker interval
	 * @return
	 */
	public double avgCoverage(Marker m) {
		String chr = m.getChromosomeName();
		CoverageChr cchr = get(chr);
		if (cchr == null) throw new RuntimeException("Chromosome '" + chr + "' not found!");
		return cchr.avgCoverage(m.getStart(), m.getEndClosed());
	}

	/**
	 * Create new chromosome coverage
	 * @param chr
	 * @param len
	 */
	public void createChr(String chr, int len) {
		coverageByName.put(Chromosome.simpleName(chr), new CoverageChr(len));
	}

	public CoverageChr get(String chr) {
		return coverageByName.get(Chromosome.simpleName(chr));
	}

	/**
	 * Increment a region
	 * @param start
	 * @param end
	 */
	public void inc(String chr, int start, int end) {
		// Increment a part of the chromosome
		chr = Chromosome.simpleName(chr);
		get(chr).inc(start, end);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}
}
