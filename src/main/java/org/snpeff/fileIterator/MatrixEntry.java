package org.snpeff.fileIterator;

/**
 * A simple entry in a 'Matrix' file
 * 
 * @author pablocingolani
 */
public class MatrixEntry {
	public String chr;
	public int pos;
	public String id;
	public String ref;
	public String alt;
	public String matrix;

	/**
	 * Get an array of values
	 * @return
	 */
	public int[] getValues() {
		char chars[] = matrix.toCharArray();
		int values[] = new int[chars.length];
		for (int i = 0; i < chars.length; i++)
			values[i] = chars[i] - '0';
		return values;
	}

	@Override
	public String toString() {
		return chr + ":" + pos + "\tID:" + id + "\tRef:" + ref + "\tAlt:" + alt + "\tMatrix:" + matrix;
	}
}
