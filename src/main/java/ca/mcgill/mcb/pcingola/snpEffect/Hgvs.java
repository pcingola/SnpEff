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
	protected Variant variant;
	protected Marker marker;
	protected Transcript tr;
	protected boolean duplication;

	public static String parseTranscript(String hgvs) {
		int idxTr = hgvs.indexOf(':');
		if (idxTr < 0) return null;
		return hgvs.substring(0, idxTr);
	}

	public static String removeTranscript(String hgvs) {
		int idxTr = hgvs.indexOf(':');
		if (idxTr < 0) return hgvs;
		return hgvs.substring(idxTr + 1);
	}

	public Hgvs(VariantEffect changeEffect) {
		variantEffect = changeEffect;
		variant = changeEffect.getVariant();
		marker = changeEffect.getMarker();
		tr = changeEffect.getTranscript();
	}

}
