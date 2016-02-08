package org.snpeff.filter;

/**
 * A Generic filter interface
 * 
 * @author pcingola
 * @param <T>
 */
public interface Filter<T> {

	/**
	 * Has any of the paremeters been set?
	 * @return
	 */
	public boolean anythingSet();

	/**
	 * Does 'objectToTest' satisfy the filter?
	 * @param objectToTest
	 * @return true if 'objectToTest' satisfies the filter
	 */
	public abstract boolean filter(T objectToTest);

}
