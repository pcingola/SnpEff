package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Transcript;

/**
 * HGSV notation
 *
 * References: http://www.hgvs.org/
 *
 * @author pcingola
 */
public class Hgvs {

	protected VariantEffect variantEffect;
	Variant variant;
	Marker marker;
	Transcript tr;

	public Hgvs(VariantEffect changeEffect) {
		this.variantEffect = changeEffect;
		variant = changeEffect.getVariant();
		marker = changeEffect.getMarker();
		tr = changeEffect.getTranscript();
	}

}
