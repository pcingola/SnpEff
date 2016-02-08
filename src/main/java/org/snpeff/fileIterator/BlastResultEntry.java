package org.snpeff.fileIterator;

public class BlastResultEntry implements Comparable<BlastResultEntry> {
	public String qseqid;
	public String sseqid;
	public double pident;
	public int length;
	public int mismatch;
	public int gapopen;
	public int qstart;
	public int qend;
	public int sstart;
	public int send;
	public double evalue;
	public int bitscore;

	@Override
	public int compareTo(BlastResultEntry bre) {
		// Compare 'query' ID
		int res = qseqid.compareTo(bre.qseqid);
		if (res != 0) return res;

		// Compare 'chromosome' 
		res = sseqid.compareTo(bre.sseqid);
		if (res != 0) return res;

		//  Compare start position 
		return sstart - bre.sstart;
	}

	/**
	 * Swap start- end positions if they are not in order
	 */
	public void fixStartEnd() {
		if (sstart > send) {
			int t = send;
			send = sstart;
			sstart = t;
		}

		if (qstart > qend) {
			int t = qend;
			qend = qstart;
			qstart = t;
		}
	}

	@Override
	public String toString() {
		return qseqid + ":" + qstart + "-" + qend + "\t->\t" + sseqid + ":" + sstart + "-" + send;
	}

}
