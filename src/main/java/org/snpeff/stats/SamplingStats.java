package org.snpeff.stats;

/**
 * Perform stats by analyzing some samples
 */
public interface SamplingStats<T> {

	/**
	 * Does this statistic have any data?
	 * @return
	 */
	public boolean hasData();

	/**
	 * Analyze one sample
	 * @param sample
	 */
	public void sample(T sample);
}
