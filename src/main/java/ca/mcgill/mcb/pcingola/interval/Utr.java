package ca.mcgill.mcb.pcingola.interval;

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

	public Utr(Exon parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
	}

	public abstract boolean isUtr3prime();

	public abstract boolean isUtr5prime();

	abstract int utrDistance(SeqChange snp, Transcript tint);

}
