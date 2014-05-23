package ca.mcgill.mcb.pcingola.interval;

/**
 * A variant that has a numeric score.
 *
 * @author pcingola
 */
public class VariantWithScore extends Variant {

	private static final long serialVersionUID = 1L;

	double score;

	public VariantWithScore(Marker parent, int start, int end, String id, double score) {
		super(parent, start, end, id);
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
