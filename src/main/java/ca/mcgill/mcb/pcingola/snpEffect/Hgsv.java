package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;

/**
 * HGSV notation
 *
 * References: http://www.hgvs.org/
 *
 * @author pcingola
 */
public class Hgsv {

	protected ChangeEffect changeEffect;
	SeqChange seqChange;
	Marker marker;
	Transcript tr;

	public Hgsv(ChangeEffect changeEffect) {
		this.changeEffect = changeEffect;
		seqChange = changeEffect.getSeqChange();
		marker = changeEffect.getMarker();
		tr = changeEffect.getTranscript();
	}

}
