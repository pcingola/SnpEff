package org.snpeff.interval;

/**
 * Interval for a UTR (5 prime UTR and 3 prime UTR
 *
 * @author pcingola
 *
 */
public abstract class Utr extends Marker {

	private static final long serialVersionUID = 1636197649250882952L;

	public Utr() {
		super();
	}

	public Utr(Exon parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
	}

	public abstract boolean isUtr3prime();

	public abstract boolean isUtr5prime();

	abstract int utrDistance(Variant snp, Transcript tint);

}
