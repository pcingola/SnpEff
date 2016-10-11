package org.snpeff.svg;

import org.snpeff.interval.Cds;

/**
 * Create an SVG representation of a Marker
 */
public class SvgCds extends Svg {

	Cds cds;

	public SvgCds(Cds cds, Svg svg) {
		super(cds, svg);
		this.cds = cds;
		baseY = svg.baseY;
		rectColorFill = "#00ff00";
	}

	@Override
	public String toString() {
		return marker();
	}

}
