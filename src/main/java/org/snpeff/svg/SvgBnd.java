package org.snpeff.svg;

import org.snpeff.interval.VariantBnd;

/**
 * Create an SVG representation of a Marker
 */
public class SvgBnd extends Svg {

	public static final int BND_CURL_SIZE = 50;

	VariantBnd varBnd;
	Svg svgTr1, svgTr2;

	public SvgBnd(VariantBnd varBnd, Svg svgTr1, Svg svgTr2) {
		super();
		this.varBnd = varBnd;
		this.svgTr1 = svgTr1;
		this.svgTr2 = svgTr2;
		lineStrokeWidth = 10;
		lineColor = "#ff00ff";
	}

	String bndW() {
		StringBuilder sb = new StringBuilder();
		int pos1 = varBnd.getStart();
		double x1 = svgTr1.pos2coord(pos1);
		double y1 = svgTr1.baseY + svgTr1.rectHeight / 2;

		int pos2 = varBnd.getEndPoint().getStart();
		double x2 = svgTr2.pos2coord(pos2);
		double y2 = svgTr2.baseY + svgTr2.rectHeight / 2;

		double x3 = Math.max(x1, x2) + BND_CURL_SIZE;

		sb.append(line(x1, y1, x3, y1));
		sb.append(line(x3, y1 - lineStrokeWidth / 2, x3, y2 + lineStrokeWidth / 2));
		sb.append(line(x2, y2, x3, y2));
		return sb.toString();
	}

	@Override
	public String toString() {
		return bndW();
	}

}
