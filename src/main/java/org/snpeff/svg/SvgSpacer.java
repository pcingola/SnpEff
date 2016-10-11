package org.snpeff.svg;

import org.snpeff.interval.Marker;

/**
 * Leave an empty vertical space
 */
public class SvgSpacer extends Svg {

	public SvgSpacer(Marker m, Svg svg) {
		super(m, svg);
	}

	@Override
	public String toString() {
		return "";
	}

}
