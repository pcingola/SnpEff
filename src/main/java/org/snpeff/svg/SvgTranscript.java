package org.snpeff.svg;

import org.snpeff.interval.Cds;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Transcript;

/**
 * Create an SVG representation of a transcript
 */
public class SvgTranscript extends Svg {

	Transcript tr;

	public SvgTranscript(Transcript tr, Svg svg) {
		super(tr, svg);
		this.tr = tr;
		if (svg != null && svg instanceof SvgGene) {
			baseY = svg.baseY + RECT_HEIGHT;
			nextBaseY = baseY + 2 * RECT_HEIGHT;
		}
		rectColorStroke = "#ffffff";
	}

	@Override
	public String id() {
		double y = baseY - rectHeight / 2;
		Gene g = (Gene) tr.getParent();
		return text(start(), y, g.getGeneName() + " " + tr.getId());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(markerId());
		sb.append(hline(rectHeight / 2));

		for (Cds cds : tr.getCds()) {
			Svg svg = factory(cds, this);
			sb.append("\t" + svg);
		}

		for (Intron intron : tr.introns()) {
			Svg svg = factory(intron, this);
			sb.append("\t" + svg);
		}

		return sb.toString();
	}

}
