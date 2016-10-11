package org.snpeff.svg;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;

/**
 * Create an SVG representation of a Marker
 */
public class SvgGene extends Svg {

	Gene gene;

	public SvgGene(Gene gene, Svg svg) {
		super(gene, svg);
		this.gene = gene;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(rectangle());
		sb.append(id());

		int base = baseY;
		for (Transcript tr : gene) {
			base += 2 * rectHeight;
			Gpr.debug("BASE: " + base);
			Svg svg = factory(tr, this);
			svg.setBaseY(base);
			sb.append(svg);
		}
		return sb.toString();
	}

}
