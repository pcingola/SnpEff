package org.snpeff.svg;

import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.VariantBnd;
import org.snpeff.snpEffect.SnpEffectPredictor;

/**
 * Create an SVG representation of a BND (translocation) variant
 *
 *
 * In a VCF file, there are four possible translocations (BND) entries:
 *
 * 				REF ALT Meaning
 * 	type 1:		s t[p[ piece extending to the right of p is joined after t
 * 	type 2:		s t]p] reverse comp piece extending left of p is joined after t
 * 	type 3:		s ]p]t piece extending to the left of p is joined before t
 * 	type 4:		s [p[t reverse comp piece extending right of p is joined before t
 *
 */
public class SvgTranslocation extends Svg {

	Transcript tr1, tr2;
	VariantBnd varBnd;
	SnpEffectPredictor snpEffPredictor;

	public SvgTranslocation(Transcript tr1, Transcript tr2, VariantBnd varBnd, SnpEffectPredictor snpEffPredictor) {
		this.tr1 = tr1;
		this.tr2 = tr2;
		this.varBnd = varBnd;
		this.snpEffPredictor = snpEffPredictor;
	}

	Svg nextProt(Transcript tr, Svg svgTr) {
		// Find nextprot markers for transcript 'tr'
		Markers results = snpEffPredictor.query(tr);
		Markers nextprotMarkers = new Markers();
		for (Marker m : results) {
			if (m instanceof NextProt) {
				NextProt np = (NextProt) m;
				if (tr.getId().equals(np.getTranscriptId())) nextprotMarkers.add(m);
			}
		}

		Svg svgNextProt = new SvgNextProt(tr, svgTr, nextprotMarkers);

		return svgNextProt;
	}

	/**
	 * Create transcript and variant Svgs
	 */
	@Override
	public String toString() {
		if (tr2.intersects(varBnd.getStart()) && tr1.intersects(varBnd.getEndPoint().getStart())) {
			// Swap transcripts
			Transcript trTmp = tr2;
			tr2 = tr1;
			tr1 = trTmp;
		}

		// Transcript 1, scale and NextProt domains
		Svg svgScale1 = new SvgScale(tr1, null);
		Svg svgTr1 = Svg.factory(tr1, svgScale1);
		Svg svgNextProt1 = nextProt(tr1, svgTr1);

		Svg svgSpacer = new SvgSpacer(tr1, svgNextProt1);

		// Transcript 2, scale and NextProt domains
		Svg svgScale2 = new SvgScale(tr2, svgSpacer);
		svgScale2.setScaleX();
		Svg svgTr2 = Svg.factory(tr2, svgScale2);
		Svg svgNextProt2 = nextProt(tr2, svgTr2);

		// Translocation
		Svg svgBnd;
		svgBnd = new SvgBnd(varBnd, svgTr1, svgTr2);

		String svgStr = svgTr1.open() //
				+ svgTr1 + svgScale1 //
				+ svgNextProt1 //
				+ svgTr2 + svgScale2 //
				+ svgNextProt2 //
				+ svgBnd //
				+ svgTr1.close();

		return svgStr;
	}

}
