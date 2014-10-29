package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;

/**
 * HGSV notation
 *
 * References: http://www.hgvs.org/
 *
 * @author pcingola
 */
public class Hgvs {

	// Don't show sequences that are too long
	public static final int MAX_SEQUENCE_LEN_HGVS = 100;

	protected VariantEffect variantEffect;
	Variant variant;
	Marker marker;
	Transcript tr;

	public Hgvs(VariantEffect changeEffect) {
		variantEffect = changeEffect;
		variant = changeEffect.getVariant();
		marker = changeEffect.getMarker();
		tr = changeEffect.getTranscript();
	}

}
