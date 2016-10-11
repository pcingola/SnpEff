package org.snpeff.svg;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;

/**
 * Create an SVG representation of a Marker
 */
public class SvgGene extends Svg {

	Gene gene;

	public SvgGene(Gene gene, Svg svg) {
		super(gene, svg);
		this.gene = gene;
		nextBaseY = baseY + RECT_HEIGHT + gene.subIntervals().size() * RECT_HEIGHT * 2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id());

		Svg svgPrev = this;
		int endY = baseY;
		for (Transcript tr : gene) {
			Svg svg = factory(tr, svgPrev);
			sb.append(svg);
			svgPrev = svg;
			endY = svg.nextBaseY;
		}
		sb.append(rectangle(start(), baseY, sizeX, endY - baseY, true));
		return sb.toString();
	}

}
