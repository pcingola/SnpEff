package org.snpeff.svg;

import org.snpeff.interval.Exon;

/**
 * Create an SVG representation of a Marker
 */
public class SvgExon extends Svg {

	Exon ex;

	public SvgExon(Exon ex, Svg svg) {
		super(ex, svg);
		this.ex = ex;
		baseY = (int) (svg.baseY + 0.25 * RECT_HEIGHT);
		rectHeight = (int) (0.5 * RECT_HEIGHT);
		rectColorFill = "#0000ff";
	}

	@Override
	public String toString() {
		return marker();
	}

}
