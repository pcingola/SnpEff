package org.snpeff.gsa;

/**
 * A list of pvalues (i.e. values in the range [0, 1])
 * 
 * @author pcingola
 */
public class PvaluesList extends ScoreList {

	public PvaluesList() {
		super();
	}

	/**
	 * Add a p-value to the list
	 * @param score
	 */
	@Override
	public void add(double score) {
		if ((score < 0) || (score > 1)) throw new RuntimeException("p-value out of range: " + score);
		scores.add(score);
	}

	@Override
	protected double getDefaultValue() {
		return 1.0;
	}

}
