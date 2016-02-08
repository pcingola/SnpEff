package org.snpeff.osCmd;

/**
 * Filter a line before processing
 * 
 * @author pcingola
 */
public interface LineFilter {

	/**
	 * Filter and prossible change a line
	 * @param line
	 * @return A new string replacing the line, or null if the line does not pass the filter (e.g. should not be processed any further
	 */
	public String filter(String line);
}
