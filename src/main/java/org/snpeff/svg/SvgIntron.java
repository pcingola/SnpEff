package org.snpeff.svg;

import org.snpeff.interval.Intron;

import scala.collection.mutable.StringBuilder;

/**
 * Create an SVG representation of a Marker
 */
public class SvgIntron extends Svg {

	public static final int SIGN_WIDTH = 5;

	Intron intron;

	public SvgIntron(Intron intron, Svg svg) {
		super(intron, svg);
		this.intron = intron;
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
		double y1 = baseY - h / 2;
		double y2 = baseY + h / 2;

		if (intron.getParent().isStrandPlus()) { //
			return line(pos, y1, pos + SIGN_WIDTH, baseY) //
					+ line(pos + SIGN_WIDTH, baseY, pos, y2) //
					; //
		}

		return line(pos + SIGN_WIDTH, y1, pos, baseY) //
				+ line(pos, baseY, pos + SIGN_WIDTH, y2) //
				;

	}

	@Override
	public String toString() {
		return strand();
	}

}
