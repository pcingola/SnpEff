package org.snpeff.svg;

import org.snpeff.interval.Intron;

/**
 * Create an SVG representation of a Marker
 */
public class SvgIntron extends Svg {

	public static final int SIGN_WIDTH = 5;

	Intron intron;

	public SvgIntron(Intron intron, Svg svg) {
		super(intron, svg);
		this.intron = intron;
		baseY = (int) (svg.baseY + 0.25 * RECT_HEIGHT);
		rectHeight = (int) (0.5 * RECT_HEIGHT);
	}

	String strand() {
		StringBuilder sb = new StringBuilder();
		int start = (int) start() + SIGN_WIDTH;
		int end = (int) end() - SIGN_WIDTH;
		for (int i = start; i < end; i += 2 * SIGN_WIDTH)
			sb.append(strand(i));
		return sb.toString();
	}

	String strand(int pos) {
		double h = rectHeight;
		double y1 = baseY;
		double y2 = baseY + h / 2;
		double y3 = baseY + h;

		if (intron.getParent().isStrandPlus()) { //
			return line(pos, y1, pos + SIGN_WIDTH, y2) //
					+ line(pos + SIGN_WIDTH, y2, pos, y3) //
			; //
		}

		return line(pos + SIGN_WIDTH, y1, pos, y2) //
				+ line(pos, y2, pos + SIGN_WIDTH, y3) //
		;

	}

	@Override
	public String toString() {
		return strand();
	}

}
