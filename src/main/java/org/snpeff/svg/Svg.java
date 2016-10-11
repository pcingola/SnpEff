package org.snpeff.svg;

import org.snpeff.interval.Cds;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.IntervalAndSubIntervals;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;

/**
 * Create an SVG representation of a Marker
 */
public class Svg {

	public static final int DEFAULT_SIZE_X = 1000;
	public static final int DEFAULT_SIZE_Y = 1000;
	public static final int DEFAULT_BASE_Y = 10;

	public static final String LINE_COLOR_STROKE = "#000000";
	public static final int LINE_STROKE_WIDTH = 1;

	public static final String RECT_COLOR_FILL = "#FFFFFF";
	public static final String RECT_COLOR_STROKE = "#000000";
	public static final int RECT_HEIGHT = 20;
	public static final int RECT_STROKE_WIDTH = 1;

	public static final int TEXT_SIZE = 10;
	public static final String TEXT_STYLE = "font-family: Arial; font-size:" + TEXT_SIZE + ";";

	int baseY;
	int rectHeight;
	String rectColorFill;
	String rectColorStroke;
	int sizeX, sizeY; // Canvas size
	int posStart, posEnd; // Chromosome positions
	double scaleX;
	Marker m;
	int nextBaseY; // BaseY for next item

	public static Svg factory(Marker m, Svg svg) {
		switch (m.getType()) {
		case EXON:
			return new SvgExon((Exon) m, svg);

		case CDS:
			return new SvgCds((Cds) m, svg);

		case INTRON:
			return new SvgIntron((Intron) m, svg);

		case GENE:
			return new SvgGene((Gene) m, svg);

		case TRANSCRIPT:
			return new SvgTranscript((Transcript) m, svg);

		default:
			return new Svg(m, svg);
		}
	}

	public static void main(String[] args) {
		String fileName = Gpr.HOME + "/z.html";
		Config config = new Config("testHg19Chr22");
		SnpEffectPredictor sep = config.loadSnpEffectPredictor();

		for (Gene g : sep.getGenome().getGenes())
			if (g.isProteinCoding() && g.isStrandMinus()) Gpr.debug(g.getGeneName() + "\t" + g.subIntervals().size());

		//		Gene g = sep.getGene("SH3BP1");
		Gene g1 = sep.getGene("POLDIP3");
		Svg svgScale1 = new SvgScale(g1, null);
		Svg svg1 = Svg.factory(g1, svgScale1);

		Svg svgSpacer = new SvgSpacer(g1, svg1);

		Gene g2 = sep.getGene("INPP5J");
		Svg svgScale2 = new SvgScale(g2, svgSpacer);
		svgScale2.setScaleX();
		Svg svg2 = Svg.factory(g2, svgScale2);

		String svgStr = svg1.open() //
				+ svg1 + svgScale1 //
				+ svg2 + svgScale2 //
				+ svg1.close();

		Gpr.debug(svgStr);
		Gpr.toFile(fileName, svgStr);
	}

	public Svg(Marker m, Svg svg) {
		sizeX = DEFAULT_SIZE_X;
		sizeY = DEFAULT_SIZE_Y;
		baseY = DEFAULT_BASE_Y;
		rectColorFill = RECT_COLOR_FILL;
		rectColorStroke = RECT_COLOR_STROKE;
		rectHeight = RECT_HEIGHT;
		this.m = m;

		if (svg != null) {
			posStart = svg.posStart;
			posEnd = svg.posEnd;
			scaleX = svg.scaleX;
			baseY = svg.nextBaseY;
		} else setScaleX();

		nextBaseY = baseY + RECT_HEIGHT * 2;
	}

	public String close() {
		return "</svg>\n";
	}

	double end() {
		return (m.getEnd() - posStart) * scaleX;
	}

	public String hline(int y) {
		return line(start(), baseY + y, end(), baseY + y);
	}

	public String id() {
		double y = baseY - rectHeight / 2;
		return text(start(), y, m.getId());
	}

	String line(double x1, double y1, double x2, double y2) {
		String lineStyle = "stroke:" + LINE_COLOR_STROKE //
				+ ";stroke-width:" + LINE_STROKE_WIDTH //
		;

		return "<line"//
				+ " x1=" + x1 //
				+ " y1=" + y1 //
				+ " x2=" + x2 //
				+ " y2=" + y2 //
				+ " style=\"" + lineStyle + "\""//
				+ "/>\n";
	}

	@SuppressWarnings("rawtypes")
	public String marker() {
		StringBuilder sb = new StringBuilder();
		sb.append(rectangle());

		if (m instanceof IntervalAndSubIntervals) {
			IntervalAndSubIntervals msubs = (IntervalAndSubIntervals) m;
			for (Object o : msubs) {
				Marker sub = (Marker) o;
				Svg svg = factory(sub, this);
				sb.append("\t" + svg);
			}
		}

		return sb.toString();
	}

	public String markerId() {
		return marker() + id();
	}

	public String open() {
		return "<svg width=\"" + sizeX + "\" height=\"" + sizeY + "\">\n";
	}

	double pos2coord(int pos) {
		return (pos - posStart) * scaleX;
	}

	public String rectangle() {
		double w = size();
		double h = rectHeight;
		double x = start();
		double y = baseY;
		return rectangle(x, y, w, h, false);
	}

	public String rectangle(double x, double y, double w, double h, boolean empty) {
		String rectStyle = "fill:" + (empty ? "none" : rectColorFill)//
				+ ";stroke:" + rectColorStroke //
				+ ";stroke-width:" + RECT_STROKE_WIDTH //
		;

		return "<rect"//
				+ " x=" + x //
				+ " y=" + y //
				+ " width=" + w //
				+ " height=" + h//
				+ " style=\"" + rectStyle + "\""//
				+ "/>\n";
	}

	public void setBaseY(int baseY) {
		this.baseY = baseY;
	}

	void setScaleX() {
		posStart = m.getStart();
		posEnd = m.getEnd();
		if (posEnd <= posStart) throw new RuntimeException("Marker 'start' after 'end': Unsupported!");
		scaleX = sizeX / ((double) (posEnd - posStart));
	}

	double size() {
		return m.size() * scaleX;
	}

	double start() {
		return (m.getStart() - posStart) * scaleX;
	}

	public String text(double x, double y, String str) {
		return "<text" //
				+ " x=" + x //
				+ " y=" + y //
				+ " style=\"" + TEXT_STYLE + "\">" //
				+ str //
				+ "</text>\n" //
		;
	}

	@Override
	public String toString() {
		return marker();
	}

}
