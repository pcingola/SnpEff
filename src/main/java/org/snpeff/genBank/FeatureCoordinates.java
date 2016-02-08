package org.snpeff.genBank;

/**
 * A feature in a GenBank or EMBL file
 * 
 * @author pablocingolani
 */
public class FeatureCoordinates {

	public int start, end;
	public boolean complement;

	public FeatureCoordinates(int start, int end, boolean complement) {
		this.start = start;
		this.end = end;
		this.complement = complement;
	}

	@Override
	public String toString() {
		return (complement ? "complement" : "") + "[ " + start + ", " + end + " ]";
	}

}
