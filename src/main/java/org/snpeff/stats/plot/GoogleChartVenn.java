package org.snpeff.stats.plot;

/**
 * A simple wrapper to goolge charts API (from charts4j)
 * Plots integer data
 * 
 * @author pcingola
 */
public class GoogleChartVenn {

	String legendA;
	String legendB;
	String legendC;
	int aSize;
	int bSize;
	int cSize;
	int abSize;
	int acSize;
	int bcSize;
	int abcSize;

	public GoogleChartVenn(String legendA, String legendB, String legendC, int aSize, int bSize, int cSize, int abSize, int acSize, int bcSize, int abcSize) {
		this.legendA = legendA;
		this.legendB = legendB;
		this.legendC = legendC;

		this.aSize = aSize;
		this.bSize = bSize;
		this.cSize = cSize;

		this.abSize = abSize;
		this.acSize = acSize;
		this.bcSize = bcSize;

		this.abcSize = abcSize;
	}

	/**
	 * Create a histogram plot using Google charts
	 * @return
	 */
	public String toURLString() {
		int max = Math.max(Math.max(aSize, bSize), cSize);
		String url = "http://chart.apis.google.com/chart?"//
				+ "chs=600x400"//
				+ "&cht=v" //
				+ "&chco=FF6342,ADDE63,63C6DE" //
				+ "&chds=0," + max + "" //
				+ "&chd=t:" + aSize + "," + bSize + "," + cSize + "," + abSize + "," + acSize + "," + bcSize + "," + abcSize //
				+ "&chdl=" + legendA + "|" + legendB + "|" + legendC;
		return url;
	}
}
