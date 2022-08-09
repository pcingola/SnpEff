package org.snpeff.svg;

import java.text.NumberFormat;
import java.util.Locale;

import org.snpeff.interval.Marker;

/**
 * Create an SVG representation of a "Scale and Chromsome labels
 */
public class SvgScale extends Svg {

	public static final int SCALE_TICK_HEIGHT = 20;
	public static final int SCALE_HEIGHT = SCALE_TICK_HEIGHT + TEXT_SIZE;

	public SvgScale(Marker m, Svg svg) {
		super(m, svg);
		nextBaseY = baseY + SCALE_HEIGHT * 2;
	}

	/**
	 * Display scale coordinates
	 */
	String scale() {
		StringBuilder sb = new StringBuilder();

		// Draw a scale
		sb.append(line(0, baseY + 10, sizeY, baseY + 10));
		int step = selectStep();
		int st = (m.getStart() / step) * step;
		for (int pos = st; pos < m.getEndClosed(); pos += step) {
			double x = pos2coord(pos);
			sb.append(line(x, baseY + 0, x, baseY + SCALE_TICK_HEIGHT));
			String posStr = NumberFormat.getNumberInstance(Locale.US).format(pos);
			sb.append(text(x, baseY + SCALE_TICK_HEIGHT + TEXT_SIZE, posStr));
		}

		// Show chromosome label
		sb.append(text(0, baseY + TEXT_SIZE, "chr" + m.getChromosomeName()));

		return sb.toString();
	}

	int selectStep() {
		for (int i = 1; i < 100000000; i *= 10)
			if (m.size() / i < 20) return i; // At least two marks in the next iteration?
		return 1000000; // Safety spacing
	}

	@Override
	public String toString() {
		return scale();
	}

}
